#!/bin/csh -f
#
# File:    umlscui.csh
# Author:  Brian Carlsen
#
# REMARKS: 
#
#  This script is used to prep UMLS CUI assignments during an MTH update
#
set release="4"
set version="1"
set version_authority="BAC"
set version_date="07/07/2009"

#
# Set environment (if configured)
#
if ($?ENV_HOME == 0) then
    echo '$'"ENV_HOME must be set"
    exit 1
endif
if ($?ENV_FILE == 0) then
    echo '$'"ENV_HOME must be set"
    exit 1
endif
source $ENV_HOME/bin/env.csh

#
# Check Environment Variables
#
if ($?ORACLE_HOME == 0) then
    echo '$'"ORACLE_HOME must be set"
    exit 1
endif

#
# parse arguments
#
if ($#argv > 0) then
    if ("$argv[1]" == "-version") then
        echo "Version $version, $version_date ($version_authority)"
        exit 0
    else if ("$argv[1]" == "-v") then
        echo "$version"
        exit 0
    else if ("$argv[1]" == "--help" || "$argv[1]" == "-help") then
    cat <<EOF
 This script has the following usage:
   Usage: umlscui.csh <db> <umlscui file> <MTH version>

    This script prepares performs a series of steps to ensure that atoms
    from the current UMLS version have correct UMLS CUI values.  Where needed
    it also adds UMLSCUI attributes.
EOF
    exit 0
    endif
endif

#
# get arguments
#
if ($#argv == 4) then
    set db = $1;
    set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
    set file = $2;
    set work_id = $4
    if (-e $file) then
        set new_source = $3
    else
        echo "$file does not exist"
        exit 1
    endif
else
    echo "Usage: umlscui.csh <db> <umlscui file> <MTH version> <work_id>"
    exit 1
endif

echo "------------------------------------------------------------------------"
echo "Starting...`/bin/date`"
echo "------------------------------------------------------------------------"
echo "  ORACLE_HOME:      $ORACLE_HOME"
echo "  database:         $db"
echo "  file:             $file"
echo "  SAB:              $new_source"
echo ""

#
# $file contains
# CUI, AUI, SAUI, SCUI, SDUI, SAB, TTY, CODE, SUI
# perl -ne 'split /\|/; print "$_[0]||$_[8]|$_[9]|$_[10]|$_[11]|$_[12]|$_[13]|$_[14]|SUI|\n" if $_[1] eq "ENG";' MRCONSO.RRF
# Prep table for umlscui file
#
echo "    Prep table for $file ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT 2;
    SET SERVEROUTPUT ON SIZE 100000;
    EXEC MEME_UTILITY.drop_it('table','t_lrc_$$');
    CREATE TABLE t_lrc_$$ AS SELECT aui as cui, aui, source_aui, source_cui, 
           source_dui, source, tty, code, atom_name string, sui
    FROM classes a, atoms b WHERE 1=0;
EOF
if ($status != 0) then
    echo "ERROR creating t_lrc_$$"
    cat /tmp/t.$$.log
    exit 1
endif
$MEME_HOME/bin/dump_mid.pl -t t_lrc_$$ $db . >&! /tmp/t.$$.log
if ($status != 0) then
    echo "ERROR preparing t_lrc_$$ load files"
    cat /tmp/t.$$.log
    exit 1
endif
/bin/cp $file t_lrc_$$.dat
$ORACLE_HOME/bin/sqlldr $user@$db control=t_lrc_$$.ctl >&! /tmp/t.$$.log
if ($status != 0) then
    echo "ERROR loading t_lrc_$$"
    cat /tmp/t.$$.log
    exit 1
endif

#
# Assign SUIs and AUIs
#
echo "    Prep table for $file ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT 2;
    SET SERVEROUTPUT ON SIZE 100000;
    EXEC MEME_UTILITY.drop_it('table','t_lrc2_$$');
    CREATE TABLE t_lrc2_$$ AS 
    SELECT cui, aui, source_aui, source_cui, source_dui, code, source, tty, 
           a.sui as string, b.sui 
    FROM t_lrc_$$ a, string_ui b 
    WHERE b.language='ENG' AND a.string=b.string;

    -- Only apply UMLSCUI assignments to LRC for releasable (current version atoms)
    -- This runs AFTER update_releasability, so only applies to update sources.
    EXEC MEME_UTILITY.drop_it('table','t_lrc3_$$');
    CREATE TABLE t_lrc3_$$ AS 
    SELECT DISTINCT b.aui, a.cui, c.atom_id, c.concept_id, a.source
    FROM t_lrc2_$$ a, atoms_ui b, classes c 
    where a.sui = b.sui AND b.stripped_source = a.source
      AND nvl(a.source_aui, 'n')= nvl(b.source_aui, 'n')
      AND nvl(a.source_cui, 'n')= nvl(b.source_cui, 'n')
      AND nvl(a.source_dui, 'n')= nvl(b.source_dui, 'n')
      AND a.code = b.code AND a.tty = b.tty
      AND b.aui = c.aui
      AND c.tobereleased in ('Y','y');
    ALTER TABLE t_lrc3_$$ ADD PRIMARY KEY (atom_id);
EOF
if ($status != 0) then
    echo "ERROR assigning SUIs and AUIs"
    cat /tmp/t.$$.log
    exit 1
endif

#
# Set LRC
#
echo "    Set new last_release_cui values ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT 2;
    SET SERVEROUTPUT ON SIZE 100000;
    -- Only update releasable atoms
    UPDATE 
       (SELECT a.last_release_cui, b.cui, a.released 
        FROM classes a, t_lrc3_$$ b WHERE a.tobereleased in 'Y' 
        AND a.atom_id = b.atom_id AND a.last_release_cui != b.cui)
    SET last_release_cui = cui, released='A' where last_release_cui != cui;

    COMMIT;

    -- Remove cases from t_lrc3_$$ where atoms have a correct 
    -- current version UMLSCUI attribute
    EXEC MEME_UTILITY.drop_it('table','t_lrc3b_$$');
    CREATE TABLE t_lrc3b_$$ AS
    SELECT * FROM t_lrc3_$$
    WHERE (atom_id,cui) IN
      (SELECT atom_id,cui FROM t_lrc3_$$
       MINUS SELECT atom_id,attribute_value FROM attributes b 
       WHERE attribute_name = 'UMLSCUI' 
         AND source='$new_source');
    EXEC MEME_UTILITY.drop_it('table','t_lrc3_$$');
    CREATE TABLE t_lrc3_$$ AS
    SELECT * FROM t_lrc3b_$$;
    EXEC MEME_UTILITY.drop_it('table','t_lrc3b_$$');
    ALTER TABLE t_lrc3_$$ ADD PRIMARY KEY (atom_id);

EOF
if ($status != 0) then
    echo "ERROR assigning LRCs"
    cat /tmp/t.$$.log
    exit 1
endif

echo "    Cases of new UMLSCUI attributes by SAB"
$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select count(*) ct,source from t_lrc3_$$ group by source" | sed 's/^/      /'
if ($status != 0) then
    echo "ERROR dumping new UMLSCUI attribute cases"
    exit 1
endif

#
# Update to current LRC
#
echo "    Update old version UMLSCUI attributes to $new_source ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT 2;
    SET SERVEROUTPUT ON SIZE 100000;
    EXEC MEME_UTILITY.drop_it('table','t_lrc4_$$');
    CREATE TABLE t_lrc4_$$ AS
    SELECT a.attribute_id, a.atom_id, b.cui
    FROM attributes a, t_lrc3_$$ b, source_version c
    WHERE c.current_name = '$new_source'
      AND a.atom_id = b.atom_id
      AND a.source = c.previous_name
      AND a.attribute_name = 'UMLSCUI'
      AND a.attribute_value = b.cui;

    -- Remove cases that already have a current UMLSCUI attribute   
    -- NO NEED - already handled by delete in previous section

    ALTER TABLE t_lrc4_$$ ADD PRIMARY KEY (attribute_id);
    UPDATE (SELECT source FROM attributes a, t_lrc4_$$ b
           WHERE a.attribute_id = b.attribute_id)
    SET source = '$new_source';
EOF
if ($status != 0) then
    echo "ERROR assigning updating UMLSCUI attributes to $new_source"
    cat /tmp/t.$$.log
    exit 1
endif

#
# Insert new UMLSCUI attributes
#
echo "    Insert new UMLSCUI attributes ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT 2;
    SET SERVEROUTPUT ON SIZE 100000;
    -- Remove cases that after prior step now have correct UMLSCUI attribute
    DELETE FROM t_lrc3_$$ a WHERE (atom_id,cui) IN 
      (SELECT atom_id,attribute_value FROM attributes b 
       WHERE attribute_name = 'UMLSCUI' 
         AND source='$new_source');

    -- Prep table for needed "new" UMLSCUI attributes
    EXEC MEME_UTILITY.drop_it('table','t_lrc_insert');
    CREATE TABLE t_lrc_insert AS
    SELECT concept_id,atom_id,attribute_id, attribute_level,
                   attribute_name,attribute_value,source,status,
                   generated_status, released,tobereleased, suppressible,
                   sg_id, sg_type, sg_qualifier, source_atui
    FROM source_attributes WHERE 1=0
    UNION
    SELECT concept_id, atom_id, 0, 'S', 'UMLSCUI', cui, '$new_source', 'R',
           'Y', 'N', 'n', 'N', aui, 'AUI','',''
    FROM t_lrc3_$$;
EOF
if ($status != 0) then
    echo "ERROR preparing table for UMLSCUI attribute insert"
    cat /tmp/t.$$.log
    exit 1
endif

$MEME_HOME/bin/insert.pl -w $work_id -atts t_lrc_insert $db L-MEME >&! insert.a.log
if ($status != 0) then
    echo "ERROR inserting UMLSCUI attributes"
    cat insert.a.log
    exit 1
endif


#
# Insert unreleasable NCIMTH/PN to preserve old CUI assignments
#
echo "    Insert new UMLSCUI placeholder atoms ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT 2;
    SET SERVEROUTPUT ON SIZE 100000;

    -- CREATE NCIMTH/PN where there are no remaining
    -- releasable atoms with the prior last release cui left 
    -- in the concept.  Not strictly necessary for CUI assignment
    -- but good for retaining MGV_A4 semantics based on initial CUI assignments.
    EXEC MEME_UTILITY.drop_it('table','t_lrc4_$$');
    CREATE TABLE t_lrc4_$$ AS
    SELECT b.concept_id, a.last_release_cui
    FROM last_release_cuis a, classes b WHERE a.atom_id=b.atom_id
    MINUS select concept_id, last_release_cui
    FROM classes WHERE tobereleased in ('Y','y');

    -- Don't include cases that have only unreleasable atoms.y
    DELETE FROM t_lrc4_$$ a WHERE NOT EXISTS
       (SELECT concept_id FROM classes b 
        WHERE a.concept_id = b.concept_id AND tobereleased in ('Y','y') AND released='A');
 
    EXEC MEME_UTILITY.drop_it('table','t_lrc_insert');
    CREATE TABLE t_lrc_insert AS
    SELECT concept_id,a.atom_id,atom_name,termgroup,source,code,
                   status,generated_status,released,tobereleased, suppressible,
                   source_aui, source_cui, source_dui
    FROM classes a, atoms b WHERE 1=0
    UNION
    SELECT concept_id, 0, 'LRC Placeholder for (see CODE)',
       'NCIMTH/PN','NCIMTH',last_release_cui,
       'R','Y','A','N','N','','',''
    FROM t_lrc4_$$;

EOF
if ($status != 0) then
    echo "ERROR preparing table for NCIMTH/PN atom insert"
    cat /tmp/t.$$.log
    exit 1
endif

$MEME_HOME/bin/insert.pl -w $work_id -atoms t_lrc_insert $db L-MEME >&! insert.c.log
if ($status != 0) then
    echo "ERROR inserting NCIMTH/PN atoms"
    cat insert.c.log
    exit 1
endif


#
# Cleanup
#
echo "    Cleanup...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT 2;
    SET SERVEROUTPUT ON SIZE 100000;
    -- Set last_release_cui for these atoms
    UPDATE classes SET last_release_cui = code
    WHERE source='NCIMTH' AND tty='PN' AND code LIKE 'C%' 
      AND last_release_cui is null;

    -- drop tables
    EXEC MEME_UTILITY.drop_it('table','t_lrc_$$');
    EXEC MEME_UTILITY.drop_it('table','t_lrc2_$$');
    EXEC MEME_UTILITY.drop_it('table','t_lrc3_$$');
    EXEC MEME_UTILITY.drop_it('table','t_lrc4_$$');
    EXEC MEME_UTILITY.drop_it('table','t_lrc_insert');
EOF
if ($status != 0) then
    echo "ERROR cleaning up"
    cat /tmp/t.$$.log
    exit 1
endif

echo "------------------------------------------------------------------------"
echo "Finished $0 ...`/bin/date`"
echo "------------------------------------------------------------------------"



