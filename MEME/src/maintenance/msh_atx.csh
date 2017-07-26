#!/bin/csh -f
#
# File:    msh_atx.csh
# Author:  Brian Carlsen
#
# This script updates the MSH ATXs for a new version.
# The ATXs are based on strings and when MSH strings
# change, then ATXs need to be updated to reflect this
# also, self-referential MSH ATXs are removed.
#
# CHANGES: 
# 02/29/2008 BAC (1-GCLNT): for insertion improvements, manage assign_meme_ids call properly.
# 06/07/2007 JFW (1-EF42B): OMS MAPSETVERSION attribute was being incorrectly turned off
                            instead of previous version MSH MAPSETVERSION
# 05/08/2007 BAC (1-E6ZXN): MAPSETVERSION attribute value was not being set properly.
# 05/08/2007 BAC (1-E6ZXN): Fix typo (reuse ticket), ensure source_stringtab starts with string_id=1
#         b.source instead of just "source" when creating new atom
# 02/07/2007 BAC (1-DG8SR): handle MAPSETVERSION and update atom name for XM atom.
# 
if ($?MEME_HOME == 0) then
    echo '$MEME_HOME must be set'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($#argv != 3) then
    echo "Usage: $0 <database> <authority> <work id>"
    exit 1
else if ($#argv == 3) then
    set db=$1
    set authority=$2
    set work_id=$3
endif 

echo "--------------------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "--------------------------------------------------------------"
echo "db:         $db"
echo "pid:        $$"
echo ""
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

#
# Fix MSH ATX
#
echo "    Fix MSH ATX Mappings ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 1000000
    set feedback off
    whenever sqlerror exit -2

    --
    -- Truncate source tables
    --
    exec MEME_UTILITY.drop_it('table','source_attributes_$$');
    CREATE table source_attributes_$$ AS SELECT * FROM source_attributes;
    exec MEME_UTILITY.drop_it('table','source_stringtab_$$');
    CREATE table source_stringtab_$$ AS SELECT * FROM source_stringtab;

    exec MEME_UTILITY.put_message('Truncate source tables');
    exec MEME_SYSTEM.truncate('source_attributes');
    exec MEME_SYSTEM.truncate('source_stringtab');

    --
    -- Get XMAPTO attributes for previous MSH
    --
    exec MEME_UTILITY.put_message('Get XMAPTO attributes');
    INSERT INTO source_attributes
	  (switch, source_attribute_id, attribute_id, atom_id,
	   concept_id, sg_id, sg_type, sg_qualifier, 
	   attribute_level, attribute_name, attribute_value,
	   generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui,
	   source_atui, hashcode)
    SELECT 'U', 0, attribute_id, 0, concept_id, sg_id, sg_type, sg_qualifier,
	   attribute_level, attribute_name, attribute_value,
	   generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, '',
	   '', ''
    FROM attributes WHERE attribute_name='XMAPTO'
      AND tobereleased in ('Y','y')
      AND concept_id IN
	  (SELECT concept_id FROM attributes a, source_version b
	   WHERE attribute_name='TOVSAB'
	     AND attribute_value = b.previous_name
	     AND b.source = 'MSH'
             AND a.source = 'MTH')
      AND attribute_value not like '<>Long_Attribute<>:%';

    --
    -- Get XMAPTO attributes for previous MSH with long attributes
    --
    exec MEME_UTILITY.put_message('Get long XMAPTO attributes');
    INSERT INTO source_attributes
	  (switch, source_attribute_id, attribute_id, atom_id,
	   concept_id, sg_id, sg_type, sg_qualifier, 
	   attribute_level, attribute_name, attribute_value,
	   generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT 'U', 0, attribute_id, 0, concept_id, sg_id, sg_type, sg_qualifier,
	   attribute_level, attribute_name, '<>Long_Attribute<>:'||rownum,
	   generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, '', '', ''
    FROM
    (SELECT * FROM attributes 
       WHERE attribute_name='XMAPTO'
         AND tobereleased in ('Y','y')
         AND concept_id IN
        (SELECT concept_id FROM attributes a, source_version b
         WHERE attribute_name='TOVSAB'
           AND attribute_value = b.previous_name
           AND b.source = 'MSH'
           AND a.source = 'MTH')
       AND attribute_value like '<>Long_Attribute<>:%'
     ORDER BY attribute_value );

    --
    -- Get XMAPTO attributes stringtab data
    --
    INSERT INTO source_stringtab
	  (string_id, row_sequence, text_total, text_value)
    SELECT rownum, row_sequence, text_total, text_value
    FROM
    (SELECT s.* FROM stringtab s, attributes a
     WHERE substr(attribute_value,20) = s.string_id
       AND attribute_name='XMAPTO'
       AND tobereleased in ('Y','y')
       AND concept_id IN
        (SELECT concept_id FROM attributes a, source_version b
         WHERE attribute_name='TOVSAB'
           AND attribute_value = b.previous_name
           AND b.source = 'MSH'
           AND a.source = 'MTH')
       AND attribute_value like '<>Long_Attribute<>:%' 
     ORDER BY string_id);

    --
    -- Build table of xmap_id, cui, atx, pre_atx, post_atx
    --
    exec MEME_UTILITY.put_message('Create ATX table');
    exec MEME_UTILITY.drop_it('table','t_atxs_$$');
    CREATE TABLE t_atxs_$$ AS
         SELECT xmapto.attribute_id, xmap.attribute_id as xmap_id,
                SUBSTR(xmap.attribute_value,INSTR(xmap.attribute_value,'~',1,2)+1,8) cui, 
                SUBSTR(xmapto.attribute_value,INSTR(xmapto.attribute_value,'~',1,2)+1,
                       instr(xmapto.attribute_value,'~',1,3) -
                       instr(xmapto.attribute_value,'~',1,2)-1) atx,
                SUBSTR(xmapto.attribute_value,1,INSTR(xmapto.attribute_value,'~',1,2)) as pre_atx,
                SUBSTR(xmapto.attribute_value,INSTR(xmapto.attribute_value,'~',1,3)) as post_atx
         FROM source_attributes xmapto, attributes xmap
         WHERE xmap.tobereleased in ('Y','y')
           AND xmap.attribute_name='XMAP'
           AND xmap.attribute_value like '%~ATX~%'
           AND xmap.concept_id IN
             (SELECT concept_id FROM attributes a, source_version b
              WHERE attribute_name='TOVSAB'
                AND attribute_value = b.previous_name
                AND b.source = 'MSH'
                AND a.source = 'MTH')
           AND SUBSTR(xmapto.attribute_value,1,INSTR(xmapto.attribute_value,'~')-1) =
               SUBSTR(xmap.attribute_value,INSTR(xmap.attribute_value,'~',1,5)+1,
                INSTR(xmap.attribute_value,'~',1,6) -
                INSTR(xmap.attribute_value,'~',1,5) - 1)
           AND xmapto.attribute_value not like '<>Long_Attribute<>%'
         UNION ALL
         SELECT xmapto.attribute_id, xmap.attribute_id as xmap_id,
                SUBSTR(xmap.attribute_value,INSTR(xmap.attribute_value,'~',1,2)+1,8) cui, 
                SUBSTR(text_value,INSTR(text_value,'~',1,2)+1,
                       instr(text_value,'~',1,3) -
                       instr(text_value,'~',1,2)-1) atx,
                SUBSTR(text_value,1,INSTR(text_value,'~',1,2)) as pre_atx,
                SUBSTR(text_value,INSTR(text_value,'~',1,3)) as post_atx
         FROM source_attributes xmapto, attributes xmap, source_stringtab c
         WHERE xmap.tobereleased in ('Y','y')
           AND xmap.attribute_name='XMAP'
           AND xmap.attribute_value like '%~ATX~%'
           AND xmap.concept_id IN
             (SELECT concept_id FROM attributes a, source_version b
              WHERE attribute_name='TOVSAB'
                AND attribute_value = b.previous_name
                AND b.source = 'MSH'
                AND a.source = 'MTH')
           AND SUBSTR(text_value,1,INSTR(text_value,'~')-1) =
               SUBSTR(xmap.attribute_value,INSTR(xmap.attribute_value,'~',1,5)+1,
                INSTR(xmap.attribute_value,'~',1,6) -
                INSTR(xmap.attribute_value,'~',1,5) - 1)
           AND xmapto.attribute_value like '<>Long_Attribute<>%' 
           AND substr(xmapto.attribute_value,20) = string_id;

    --
    -- Prep table to remove obsolete attributes
    --
    exec MEME_UTILITY.drop_it('table','t_todelete_$$');
    CREATE TABLE t_todelete_$$ AS
    SELECT attribute_id as row_id FROM attributes WHERE 1=0;

    --
    -- In this section we deal with the strings in the MSH ATXs.
    -- ATX terms consist of headings and optional qualifiers
    -- separated by boolean operators. 
    --
    -- For MSH ATXs, the qualifier terms must match in a case
    -- insensitive way to MSH%/TQ atoms.  If they do not, these
    -- ATX rows are removed.  If the match in a case-insensitive but not
    -- a case-sensitive way, then the qualifier term is replaced by
    -- the current MSH%/TQ term.
    --
    -- The heading terms must match in a case insensitive way to
    -- either MSH%/MH or MSH%/EN or MSH%/EP termgroups.  As with TQs
    -- if they do not, the ATX rows are removed.  If they match in a
    -- case-insensitive but not case-sensitive way, the heading term
    -- is replaced by the best MSH MH or "entry term" that matches it.
    -- MH terms are checked first, and then the EN/EP terms.
    --
    exec MEME_UTILITY.put_message('Resolve ATX MSH names');
    DECLARE
        row_count                     INTEGER;
        row_multiplier                INTEGER;
        chunk_size                    INTEGER := 2000;

        TYPE curvar_type IS REF CURSOR;
        curvar              curvar_type;
        cui                 VARCHAR2(10);
        atx                 VARCHAR2(500);
        atx_work            VARCHAR2(500);
        fixed_atx           VARCHAR2(500):= NULL;
        term                VARCHAR2(500);
        location            VARCHAR2(500);
        msh_term            VARCHAR2(500);
        attribute_id        INTEGER;
        xmap_id             INTEGER;
        i                   INTEGER;
        j                   INTEGER;
        slash_index         INTEGER;
        tq_flag             BOOLEAN := FALSE;
        del                 BOOLEAN := FALSE;
        match_count         INTEGER;
    BEGIN
        row_count := 0;
        row_multiplier := 0;

        location := '81';
        OPEN curvar FOR 
          'SELECT cui,atx,attribute_id,xmap_id FROM t_atxs_$$';
        LOOP
            location := '82';
            FETCH curvar INTO cui, atx, attribute_id, xmap_id;
            EXIT WHEN curvar%NOTFOUND;

            atx_work := atx;
            location := '83';
            WHILE atx_work IS NOT NULL LOOP
                i:= INSTR(atx_work, '<');
                j:= INSTR(atx_work, '>');

                -- Determine whether or not the next
                -- term is a qualifier, if so tq_flag is true
                -- We look for terms that begin immediately
                -- after a slash character
                tq_flag := FALSE;
                slash_index := INSTR(atx_work, '/');
                IF slash_index != 0 AND slash_index = (i-1) THEN
                    tq_flag := TRUE;
                END IF;

                location := '84';
                -- Is the ATX just a string?
                IF atx_work = atx AND i = 0 THEN
                      term := atx_work;
                       --MEME_UTILITY.put_message('mratx_prepare: term = '|| term);
                      j := LENGTH(atx_work) + 1;

                -- If not, extract the current term
                ELSE
                      term := SUBSTR(atx_work, i+1, j-i-1);
                       --MEME_UTILITY.put_message('mratx_prepare: term = '|| term);
                END IF;

                -- First step, does the string exist in a case-sensitive way
                -- as a TQ or as a MH
                --
                -- IF tq_flag is set, check TQ terms, 
                -- otherwise check MH
                location := '84.5';
                IF tq_flag THEN
                    EXECUTE IMMEDIATE        
                        'SELECT count(*) FROM string_ui a, classes b
                         WHERE string = :x
                           AND string_pre = SUBSTR(:x,1,10)
                           AND source = (SELECT current_name FROM source_version 
                                         WHERE source = ''MSH'')
                           AND tty = ''TQ''
                           AND a.sui = b.sui
                           AND tobereleased in (''Y'',''y'')'
                    INTO match_count
                    USING term, term;
                ELSE
                    EXECUTE IMMEDIATE        
                        'SELECT count(*) FROM string_ui a, classes b
                         WHERE string = :x
                           AND string_pre = SUBSTR(:x,1,10)
                           AND source = (SELECT current_name FROM source_version 
                                         WHERE source = ''MSH'')
                           AND tty = ''MH''
                           AND a.sui = b.sui
                           AND tobereleased in (''Y'',''y'')'
                    INTO match_count
                    USING term, term;
                END IF;

                msh_term := term;

                -- If we did not find an exact match, 
                -- look for case-insensitive match
                -- If tq_flag is on, try TQ
                IF match_count = 0 AND tq_flag THEN
                    location := '85';
                    EXECUTE IMMEDIATE
                        'SELECT min(string) FROM string_ui a, classes b
                         WHERE LOWER(string) = :x
                           AND lowercase_string_pre = SUBSTR(:x,1,10)
                           AND source = (SELECT current_name FROM source_version 
                                         WHERE source = ''MSH'')
                           AND tty = ''TQ''
                           AND a.sui = b.sui
                           AND tobereleased in (''Y'',''y'')'
                        INTO msh_term
                        USING LOWER(term), LOWER(term);
                
                END IF;

                -- Try MH first if tq_flag is off
                IF match_count = 0 AND NOT tq_flag THEN
                    location := '85';
                    EXECUTE IMMEDIATE
                        'SELECT min(string) FROM string_ui a, classes b
                         WHERE LOWER(string) = :x
                           AND lowercase_string_pre = SUBSTR(:x,1,10)
                           AND source = (SELECT current_name FROM source_version 
                                         WHERE source = ''MSH'')
                           AND tty = ''MH''
                           AND a.sui = b.sui
                           AND tobereleased in (''Y'',''y'')'
                        INTO msh_term
                        USING LOWER(term), LOWER(term);
                END IF;


                -- If it does not match MSH/MH
                -- Find the MH for any matching EN/EP atoms
                IF msh_term IS NULL AND match_count = 0 AND NOT tq_flag THEN
        
                    location := '87';
                    EXECUTE IMMEDIATE
                        'SELECT min(c.string)
                         FROM string_ui a, classes b, string_ui c, classes d
                         WHERE LOWER(a.string) = :x
                           AND a.lowercase_string_pre = SUBSTR(:x,1,10)
                           AND b.concept_id = d.concept_id and b.code=d.code
                           AND b.source = (SELECT current_name FROM source_version 
                                           WHERE source = ''MSH'')
                           AND b.tty IN ( ''EP'', ''EN'')
                           AND d.source = (SELECT current_name FROM source_version 
                                           WHERE source = ''MSH'')
                           AND d.tty = ''MH''
                           AND c.sui = d.sui
                               AND a.sui = b.sui
                               AND b.tobereleased in (''Y'',''y'')
                               AND d.tobereleased in (''Y'',''y'')'
                    INTO msh_term
                    USING LOWER(term), LOWER(term);

                END IF;
        
                    location := '88';
                -- If no matches were found, delete the row
                IF msh_term IS NULL AND match_count = 0 THEN
                    location := '88.5';
                    del := TRUE;
                END IF;


            	IF msh_term != term THEN
                    MEME_UTILITY.put_message('term: ' || term);
                    MEME_UTILITY.put_message('msh_term: ' || msh_term);
		END IF;
  
                -- Prepare msh_term for fixed_atx
                -- Change ' to '', like  s/'/''/g in Perl
                -- msh_term := REPLACE(msh_term,'''', '''''');

                -- Append current term to fixed_atx and prepare next atx_frag

                -- Either we are at the end of the string o
                IF INSTR(atx_work, '<', j+1) = 0 THEN
                    fixed_atx := fixed_atx || SUBSTR(atx_work,1,i) || msh_term || SUBSTR(atx_work, j);
                    atx_work := NULL;

                -- or still in the middle of the string
                ELSE
                    fixed_atx := fixed_atx || SUBSTR(atx_work,1,i) || msh_term || '>';
                    atx_work := SUBSTR(atx_work, j + 1);
                END IF;

            END LOOP;

            location := '89';
            IF del THEN
		MEME_UTILITY.put_message('DELETE: ' || atx);
                EXECUTE IMMEDIATE
                    'INSERT INTO t_todelete_$$ VALUES (:x)'
                USING attribute_id;
                EXECUTE IMMEDIATE
                    'INSERT INTO t_todelete_$$ VALUES (:x)'
                USING xmap_id;
            ELSIF fixed_atx != atx THEN
		MEME_UTILITY.put_message('FIX ' || attribute_id || ': ' || atx || ',' || fixed_atx);
                EXECUTE IMMEDIATE
                    'UPDATE source_attributes a SET switch = ''R''
                     WHERE attribute_id = :x'
                USING attribute_id;
                EXECUTE IMMEDIATE
                    'UPDATE t_atxs_$$ SET atx = :x
                     WHERE attribute_id = :x'
                USING fixed_atx, attribute_id;
                EXECUTE IMMEDIATE
                    'INSERT INTO t_todelete_$$ VALUES (:x)'
                USING attribute_id;
            END IF;
            del := FALSE;
            fixed_atx := NULL;

            row_count := row_count + 1;
            IF row_count = chunk_size THEN
                row_multiplier := row_multiplier + 1;
                row_count := 0;
           END IF;
        END LOOP;

        CLOSE curvar;
    END;
/

    --
    -- Switch = 'R' for ATXs that have changed.  Set their ATX values
    --
    exec MEME_UTILITY.put_message('Prepare "fixed" ATXs to insert');
    UPDATE source_attributes a SET attribute_value =
	 (SELECT DISTINCT pre_atx || atx || post_atx FROM t_atxs_$$ b
	  where a.attribute_id=b.attribute_id)
    WHERE attribute_value not like '<>Long_Attribute<>:%'
      and switch='R';

    -- 
    -- Do the same in source_stringtab for long ATX values
    --
    UPDATE source_stringtab a SET text_value =
	 (SELECT DISTINCT pre_atx || atx || post_atx FROM t_atxs_$$ b, source_attributes c
	  WHERE  c.attribute_value like '<>Long_Attribute<>:%'
	    AND a.string_id=to_number(substr(c.attribute_value,20)) 
	    AND b.attribute_id = c.attribute_id and switch='R')
    WHERE string_id IN 
      (SELECT to_number(substr(attribute_value,20)) FROM source_attributes
       WHERE attribute_value like '<>Long_Attribute<>:%'
         AND switch = 'R');

    --
    -- Remove candidate ATXs that did not change
    --
    DELETE FROM source_attributes WHERE switch != 'R';
    DELETE FROM source_stringtab WHERE string_id not in
   	(SELECT to_number(substr(attribute_value,20)) FROM source_attributes
	 WHERE attribute_value like '<>Long_Attribute<>:%');

    --
    -- Get new TOVSAB map set attribute
    --
    exec MEME_UTILITY.put_message('Prepare new TOVSAB attribute to insert');
    INSERT INTO source_attributes
	  (switch, source_attribute_id, attribute_id, atom_id,
	   concept_id, sg_id, sg_type, sg_qualifier, 
	   attribute_level, attribute_name, attribute_value,
	   generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R',0,0,atom_id, concept_id, sg_id, sg_type, sg_qualifier,
	attribute_level, attribute_name, 
	  (select current_name FROM source_version WHERE source='MSH'),
	generated_status, source, 'R','N', 'Y', source_rank, 'N', null, null, 'hashcode'
    FROM attributes
    WHERE concept_id IN
	  (SELECT concept_id FROM attributes a, source_version b
	   WHERE attribute_name='TOVSAB'
	     AND attribute_value = b.previous_name
	     AND b.source = 'MSH'
             AND a.source = 'MTH'
	     AND tobereleased in ('y','Y'))
     AND attribute_name='TOVSAB'
     AND tobereleased in ('Y','y');

    --
    -- Get new MAPSETVERSION attribute
    --
    exec MEME_UTILITY.put_message('Prepare new MAPSETVERSION attribute to insert');
    INSERT INTO source_attributes
	  (switch, source_attribute_id, attribute_id, atom_id,
	   concept_id, sg_id, sg_type, sg_qualifier, 
	   attribute_level, attribute_name, attribute_value,
	   generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R',0,0,atom_id, concept_id, sg_id, sg_type, sg_qualifier,
	attribute_level, 'MAPSETVERSION', 
	  (select version FROM source_rank WHERE source = 
       (SELECT current_name FROM source_version WHERE source='MSH')),
	generated_status, source, 'R','N', 'Y', source_rank, 'N', null, null, 'hashcode'
    FROM attributes a
    WHERE concept_id IN
	  (SELECT concept_id FROM attributes a, source_version b
	   WHERE attribute_name='TOVSAB'
	     AND attribute_value = b.previous_name
	     AND b.source = 'MSH'
             AND a.source = 'MTH'
	     AND tobereleased in ('y','Y'))
     AND attribute_name='MAPSETVERSION'
     AND tobereleased in ('Y','y');

    --
    -- Compute hashcodes, and null ATUI values in preparation for insert 
    --
    UPDATE source_attributes
    SET hashcode = 
	MEME_UTILITY.md5(attribute_value),
     atui = null
    WHERE attribute_value not like '<>Long_Attribute<>:%';

    UPDATE source_attributes
    SET hashcode =
        MEME_UTILITY.md5((select text_value from source_stringtab
                         where string_id=to_number(substr(attribute_value,20)))),
     atui = null   
    WHERE attribute_value like '<>Long_Attribute<>:%';   


    --
    -- Insert the attributes using source insertion calls
    --
    exec MEME_UTILITY.put_message('Insert attributes');
    UPDATE source_attributes SET switch = 'U' WHERE switch = 'R';
    exec MEME_UTILITY.drop_it('table','t_map_$$');
    CREATE TABLE t_map_$$ AS SELECT string_id,rownum rn FROM source_stringtab;
    UPDATE t_map_$$ SET rn = rn + (SELECT row_sequence FROM stringtab WHERE string_id=-1);

    UPDATE source_attributes a SET attribute_value =
      (SELECT '<>Long_Attribute<>:'||rn
       FROM t_map_$$
       where string_id = to_number(substr(a.attribute_value,20)))
    WHERE attribute_value like '<>Long_Attribute<>:%';
    UPDATE source_stringtab a SET string_id = 
      (SELECT rn FROM t_map_$$ b WHERE a.string_id = b.string_id);
    UPDATE source_attributes SET attribute_id = rownum +
       (SELECT max_id FROM max_tab WHERE table_name='ATTRIBUTES');
    exec MEME_SOURCE_PROCESSING.assign_meme_ids('A','MTH',0);
    exec MEME_SOURCE_PROCESSING.map_to_meme_ids('A','MTH',0);
    exec MEME_SOURCE_PROCESSING.assign_atuis('SA','MTH',0);
    exec MEME_SOURCE_PROCESSING.core_table_insert('A','MTH',0);

    --
    -- Mark self-referential MSH XMAP attributes as obsolete
    --
    exec MEME_UTILITY.put_message('Mark self-referential MSH ATXs as obsolete');
    INSERT INTO t_todelete_$$
    SELECT DISTINCT attribute_id FROM attributes
    WHERE attribute_name = 'XMAPTO'
      AND SUBSTR(attribute_value,INSTR(attribute_value,'~',1,2)+1,8) IN
	  (SELECT last_release_cui FROM classes b, source_version c
           WHERE b.source = c.current_name
	     AND c.source = 'MSH' AND b.tobereleased in ('Y','y'))
      AND attribute_id IN (SELECT xmap_id FROM t_atxs_$$);

    --
    -- Mark non-self-referential OMS XMAP attributes as obsolete
    --
    exec MEME_UTILITY.put_message('Mark non-self-referential OMS ATXs as obsolete');
    INSERT INTO t_todelete_$$
    SELECT DISTINCT attribute_id FROM attributes
    WHERE SUBSTR(attribute_value,INSTR(attribute_value,'~',1,2)+1,8) NOT IN
	  (SELECT last_release_cui FROM classes b, source_version c
	   WHERE b.source = c.current_name
	     AND c.source = 'OMS' AND b.tobereleased in ('Y','y'))
      AND tobereleased in ('Y','y')
      AND attribute_name='XMAP'
      AND concept_id IN
             (SELECT concept_id FROM attributes a, source_version b
	      WHERE attribute_name='TOVSAB'
	        AND attribute_value = b.current_name
		AND tobereleased in ('Y','y')
	        AND b.source = 'OMS');

    --
    -- Mark old TOVSAB attribute as obsolete
    --
    exec MEME_UTILITY.put_message('Mark old TOVSAB attribute as obsolete');
    INSERT INTO t_todelete_$$
    SELECT DISTINCT attribute_id FROM attributes a , source_version b
    WHERE attribute_name='TOVSAB'
	AND attribute_value = b.previous_name
	and a.source not in (select current_name from source_version where source='MSH' union select 'MTH' from dual)
	AND b.source = 'MSH'
        AND tobereleased in ('Y','y');

    --
    -- Mark old MAPSETVERSION attribute as obsolete
    --
    exec MEME_UTILITY.put_message('Mark old MAPSETVERSION attribute as obsolete');
    INSERT INTO t_todelete_$$
    SELECT DISTINCT attribute_id FROM attributes a
    WHERE attribute_name='MAPSETVERSION'
      AND tobereleased in ('Y','y')
      AND concept_id IN
             (SELECT concept_id FROM attributes a, source_version b
	      WHERE attribute_name='TOVSAB'
	        AND attribute_value = b.current_name
		AND tobereleased in ('Y','y')
	        AND b.source = 'MSH')
	  AND attribute_id NOT IN (SELECT attribute_id FROM source_attributes);

    --
    -- Remove obsolete ATX and TOVSAB attributes
    --
    delete from t_todelete_$$ where row_id in
       (select row_id from t_todelete_$$
        GROUP BY row_id HAVING count(*)>1)
    AND rowid NOT IN
       (select min(rowid) from t_todelete_$$
        GROUP BY row_id HAVING count(*)>1);
    exec MEME_UTILITY.put_message('Remove obsolete attributes');
    exec dbms_output.put_line(MEME_BATCH_ACTIONS.macro_action( -
	action => 'T', new_value => 'N', -
	id_type => 'A', authority => '$authority', -
	work_id=> $work_id, status => 'R', -
	table_name => 't_todelete_$$'));

    --
    -- Mark dangling XMAP attributes as obsolete
    --
    exec MEME_UTILITY.put_message('Remove dangling XMAP attributes.');
    TRUNCATE TABLE t_todelete_$$;
    INSERT INTO t_todelete_$$
    SELECT a.attribute_id  
    FROM attributes a  
    WHERE a.attribute_name='XMAP'
      AND a.tobereleased in ('Y','y')  
    MINUS  
     (SELECT attribute_id FROM  
	(SELECT/*+ USE_HASH(xmap,xmapto) */ xmap.attribute_id 
         FROM attributes xmap, attributes xmapto
         WHERE xmap.attribute_name='XMAP'     
	   AND xmap.tobereleased in ('Y','y')
	   AND xmap.attribute_value NOT LIKE '<>Long_Attribute<>:%'
	   AND xmap.atom_id = xmapto.atom_id
           AND substr(xmap.attribute_value,instr(xmap.attribute_value,'~',1,5)+1,
		      instr(xmap.attribute_value,'~',1,6)-instr(xmap.attribute_value,'~',1,5)-1) =
	       substr(xmapto.attribute_value,1,instr(xmapto.attribute_value,'~')-1)     
	   AND xmapto.attribute_name='XMAPTO'
	   AND xmapto.tobereleased in ('Y','y')
	   AND xmapto.attribute_value NOT LIKE '<>Long_Attribute<>:%')   
         UNION ALL   
	 SELECT /*+ USE_HASH(xmap,xmapto) */ xmap.attribute_id   
	 FROM attributes xmap, attributes xmapto, stringtab c
	 WHERE xmap.attribute_name='XMAP'
	   AND xmap.tobereleased in ('Y','y')
	   AND xmap.attribute_value NOT LIKE '<>Long_Attribute<>:%'
	   AND xmap.atom_id = xmapto.atom_id
           AND substr(xmap.attribute_value,instr(xmap.attribute_value,'~',1,5)+1,
		       instr(xmap.attribute_value,'~',1,6)-instr(xmap.attribute_value,'~',1,5)-1) =
	       substr(c.text_value,1,instr(c.text_value,'~')-1)
	   AND xmapto.attribute_name='XMAPTO'     
	   AND xmapto.tobereleased in ('Y','y')
	   AND xmapto.attribute_value LIKE '<>Long_Attribute<>:%'
	   AND c.string_id = to_number(substr(xmapto.attribute_value,20))  
      );

    --
    -- Remove all XMAP and XMAPFROM attributes
    --
    exec dbms_output.put_line(MEME_BATCH_ACTIONS.macro_action( -
	action => 'T', new_value => 'N', -
	id_type => 'A', authority => '$authority', -
	work_id=> $work_id, status => 'R', -
	table_name => 't_todelete_$$'));

    --
    -- Mark dangling XMAPFROM attributes as obsolete
    --
    exec MEME_UTILITY.put_message('Remove dangling XMAPFROM attributes.');
    TRUNCATE TABLE t_todelete_$$;
    INSERT INTO t_todelete_$$
    SELECT attribute_id 
    FROM attributes a 
    WHERE attribute_name='XMAPFROM'
      AND tobereleased in ('Y','y')
      AND attribute_value NOT LIKE '<>Long_Attribute<>:%'
    MINUS
    SELECT /*+ USE_HASH(xmapfrom,xmap) */ xmapfrom.attribute_id  
    FROM attributes xmapfrom, attributes xmap
    WHERE xmapfrom.attribute_name='XMAPFROM'
      AND xmapfrom.tobereleased in ('Y','y')
      AND xmapfrom.attribute_value NOT LIKE '<>Long_Attribute<>:%'
      AND xmapfrom.atom_id=xmap.atom_id
      AND substr(xmapfrom.attribute_value,1,instr(xmapfrom.attribute_value,'~')-1) =
          substr(xmap.attribute_value,instr(xmap.attribute_value,'~',1,2)+1,
	                instr(xmap.attribute_value,'~',1,3)-instr(xmap.attribute_value,'~',1,2)-1)
      AND xmap.attribute_name='XMAP'
      AND xmap.tobereleased in ('Y','y')
      AND xmap.attribute_value not like '<>Long_Attribute<>:%' 
      AND xmapfrom.concept_id=xmap.concept_id;

    --
    -- Remove all XMAP and XMAPFROM attributes
    --
    exec dbms_output.put_line(MEME_BATCH_ACTIONS.macro_action( -
	action => 'T', new_value => 'N', -
	id_type => 'A', authority => '$authority', -
	work_id=> $work_id, status => 'R', -
	table_name => 't_todelete_$$'));

    --
    -- Cleanup
    --
    exec MEME_UTILITY.put_message('Cleanup');
    DROP TABLE t_todelete_$$;
    DROP TABLE t_atxs_$$;

    truncate table source_attributes;
    truncate table source_stringtab;
    exec MEME_SYSTEM.drop_indexes('source_attributes');
    exec MEME_SYSTEM.drop_indexes('source_stringtab');
    insert into source_attributes select * from source_attributes_$$;
    insert into source_stringtab select * from source_stringtab_$$;
    exec MEME_SYSTEM.reindex('source_attributes','Y',' ');
    exec MEME_SYSTEM.reindex('source_stringtab','Y',' ');
 
    exec MEME_UTILITY.put_message('Finished');
EOF
if ($status != 0) then
    echo "Error mapping ATX to current"
    exit 1
endif

#
# Prep table to insert new atom
#
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 1000000
    set feedback off
    whenever sqlerror exit -2

    exec MEME_UTILITY.put_message('Create new ATX atom');
    exec meme_utility.drop_it('table','source_atoms_$$');
    CREATE TABLE source_atoms_$$ AS
    SELECT concept_id, a.atom_id, atom_name, termgroup, source, code,
        status, generated_status, released, tobereleased, suppressible,
        source_aui, source_cui, source_dui
    FROM classes a, atoms b where 1=0;

    INSERT INTO source_atoms_$$
    SELECT DISTINCT a.concept_id, 0, b.attribute_value || ' Associated Expressions', termgroup, a.source, code,
        'R', 'Y', 'N', 'Y', 'N', 
        source_aui, source_cui, source_dui 
    FROM classes a, attributes b, source_version c
	WHERE a.concept_id = b.concept_id
	  AND b.attribute_name='TOVSAB'
      AND b.attribute_value = c.current_name
	  AND b.tobereleased in ('Y','y') AND a.tobereleased in ('Y','y')
	  AND c.source = 'MSH'and a.source='MTH';		   

    exec MEME_UTILITY.put_message('Mark old ATX atom as unreleasable');
    exec meme_utility.drop_it('table','old_atoms_$$');
    CREATE TABLE old_atoms_$$ AS
    SELECT DISTINCT a.atom_id as row_id 
    FROM classes a, attributes b, source_version c
	WHERE a.concept_id = b.concept_id
	  AND b.attribute_name='TOVSAB'
      AND b.attribute_value = c.previous_name
	  AND a.tobereleased in ('Y','y')
          AND a.source = 'MTH'
	  AND c.source = 'MSH';		   
    
EOF
if ($status != 0) then
    echo "Error mapping ATX to current"
    exit 1
endif

$MEME_HOME/bin/insert.pl -w $work_id -atoms source_atoms_$$ $db $authority >&! insert.mshatxatom.log
if ($status != 0) then
    echo "Error insering new ATX atom"
    exit 1
endif

$MEME_HOME/bin/batch.pl -w $work_id -a T -n n -t C -s t old_atoms_$$ $db $authority >&! insert.mshatxatom.log
if ($status != 0) then
    echo "Error making old ATX atom non releasable"
    exit 1
endif

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 1000000
    set feedback off
    whenever sqlerror exit -2

    --
    -- move attributes from old XM to newly created XM
    --
    exec MEME_UTILITY.put_message('Move attributes from old to new atom');
    exec meme_utility.drop_it('table','source_attributes_$$');
    CREATE TABLE source_attributes_$$ AS
    SELECT DISTINCT attribute_id as row_id, 
       a.atom_id as old_value, b.atom_id as new_value
    FROM attributes a, classes b
    WHERE a.concept_id = b.concept_id
      AND a.tobereleased in ('Y','y')
      AND b.tobereleased in ('Y','y')
      AND a.atom_id != b.atom_id
      and b.source='MTH'
      AND b.concept_id IN
         (SELECT concept_id FROM attributes a, source_version b
          WHERE attribute_name='TOVSAB'
            AND attribute_value = b.current_name
            AND tobereleased in ('Y','y')
            AND a.source = 'MTH'
            AND b.source = 'MSH');		   

    exec meme_utility.drop_it('index','x_sa_msh');

    CREATE INDEX x_sa_msh ON source_attributes_$$ (row_id) COMPUTE STATISTICS;

    UPDATE attributes a set atom_id =
      (SELECT new_value FROM source_attributes_$$ where attribute_id = row_id)
    WHERE attribute_id IN (SELECT row_id FROM source_attributes_$$);

    exec MEME_UTILITY.put_message('Finished');
EOF
if ($status != 0) then
    echo "Error mapping ATX to current"
    exit 1
endif

echo "--------------------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "--------------------------------------------------------------"
