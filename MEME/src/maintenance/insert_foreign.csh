#!/bin/csh -f
#
# File:    insert_foreign.csh
# Author:  David Hernandez/Brian Carlsen
#
# Version Information:
#  12/10/2004 (4.2.1): No more report table change
#  08/11/2003 (4.2.0): Upgraded to work with MEME4
#
set release=4
set version=2.1
set version_date="12/10/2004"
set version_authority="BAC"

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif


set i=1
while ($i <= $#argv)
    switch ($argv[$i])
        case '-*help':
	    cat <<EOF
    Usage: $0 [-t] <database>

    This script is used to insert .src files for
    non english sources. Run this script from the directory
    containing the .src files, and make sure the SRC
    content for this source has already been inserted.

EOF
            exit 0
        case '-v':
            echo $version
            exit 0
        case '-version':
            echo "Version $version, $version_date ($version_authority)"
            exit 0
    endsw
    set i=`expr $i + 1`
end

if ($#argv != 5) then
    echo "Usage: $0 <language> <db> <new_source> <old_source> <stripped_source>"
    exit -1
endif

set language=CZE
set db=mrd
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
set authority=MSHCZE2003
set new_source=MSHCZE2003
set old_source=
set stripped_source=MSHCZE

set language=$1
set db=$2
set authority=$3
set new_source=$3
set old_source=$4
set stripped_source=$5

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
set host=`$MIDSVCS_HOME/bin/midsvcs.pl -s insertion-meme-server-host`
set port=`$MIDSVCS_HOME/bin/midsvcs.pl -s insertion-meme-server-port`

#################################################################
# 
#  Start RECIPE
#
#################################################################

#################################################################
# 7. Load Section
#################################################################

#################################################################
# 7a. Load .src files into the source tables
#################################################################
#
# The NLS variable must be correctly set to load classes. 
# This section checks to see if the requisite files exist before
# attempting to load them.
#
# Start by clearing all of the source tables
#
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    exec meme_system.truncate('source_attributes');
    exec meme_system.truncate('source_stringtab');
    exec meme_system.truncate('source_classes_atoms');
    exec meme_system.truncate('source_concept_status');
    exec meme_system.truncate('source_relationships');
    exec meme_system.truncate('source_context_relationships');
    exec meme_system.truncate('source_source_rank');
    exec meme_system.truncate('source_termgroup_rank');
EOF
if ($status != 0) then
   echo "Error truncating source tables"
   cat /tmp/t.$$.log
   exit 1
endif

# i. load classes
#
if (-e classes_atoms.src) then
    $MEME_HOME/bin/load_src.csh $db classes_atoms.src >&! /tmp/t.$$.log
    if ($status != 0) then
        echo "Error"
        cat /tmp/t.$$.log
        exit 1
    endif

    $MEME_HOME/bin/classes_to_strings.csh . $language >&! /tmp/t.$$.log
    if ($status != 0) then
        echo "Error"                         
        cat /tmp/t.$$.log
        exit 1
    endif

    $MEME_HOME/bin/load_src.csh $db strings.src >&! /tmp/t.$$.log
    if ($status != 0) then
        echo "Error"                         
        cat /tmp/t.$$.log
        exit 1
    endif

else
    echo "ERROR: missing classes file"
    exit 1
endif

#
# ii. load mergefacts
#
if (-e mergefacts.src) then
    $MEME_HOME/bin/load_src.csh $db mergefacts.src >&! /tmp/t.$$.log
    if ($status != 0) then
        echo "Error"                         
        cat /tmp/t.$$.log
        exit 1
    endif
else
    echo "ERROR: missing mergefacts file"
    exit 1
endif

#
# v. load termgroups/sources
#
if (-e sources.src && -e termgroups.src) then
    # load termgroups
    $MEME_HOME/bin/load_src.csh $db termgroups.src >&! /tmp/t.$$.log
    if ($status != 0) then
        echo "Error"                         
        cat /tmp/t.$$.log
        exit 1
    endif

    # load sources
    $MEME_HOME/bin/load_src.csh $db sources.src >&! /tmp/t.$$.log
    if ($status != 0) then
        echo "Error"                         
        cat /tmp/t.$$.log
        exit 1
    endif

endif

#################################################################
# 7b. Insert ranks
#################################################################
#
# This step inserts the new termgroups/sources into the corresponding
# ranks tables.
#
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    set feedback off
    exec MEME_SOURCE_PROCESSING.insert_ranks ( -
	authority => '$authority', -
	work_id => 0 );
EOF
if ($status != 0) then
   echo "Error inserting ranks"
   cat /tmp/t.$$.log
   exit 1
endif


#################################################################
# 7c. Assign ids
#################################################################
#
# This step assigns LUI, SUI, and ISUI values to the new atoms
# It also assigns meme_ids 
#
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF 
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    set feedback off
    exec MEME_SOURCE_PROCESSING.assign_string_uis ( -
	authority => '$authority', -
	work_id => 0 );

    exec MEME_SOURCE_PROCESSING.assign_auis ( -
	authority => '$authority', -
	work_id => 0 );

    exec MEME_SOURCE_PROCESSING.assign_meme_ids ( -
        table_name => 'C', -
	authority => '$authority', -
	work_id => 0 );

EOF
if ($status != 0) then
   echo "Error"
   exit 1
endif

#################################################################
# 7d. Insert source_ids
#################################################################
#
# This step inserts source_id-meme_id map into source_id_map
# for everything we are loading.
#
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 100000
    set feedback off
    WHENEVER SQLERROR EXIT -1
    exec MEME_SOURCE_PROCESSING.insert_source_ids ( -
	table_name => 'C', -
	authority => '$authority', -
	work_id => 0 );
EOF
if ($status != 0) then
   echo "Error"
   exit 1
endif

#################################################################
# 7f. Insert Data
#################################################################
#
# This step inserts the data from the source* core tables into
# the actual core tables.
#
# 7f.a. Classes 
#
if (-e classes_atoms.src) then
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    set feedback off

    exec MEME_SOURCE_PROCESSING.prepare_src_mergefacts( -
        authority => '$authority', -
	work_id => 0);

    exec MEME_SOURCE_PROCESSING.foreign_classes_insert ( -
	authority => '$authority', -
	work_id => 0 );

EOF
if ($status != 0) then
   echo "Error"
   exit 1
endif
endif

#
#################################################################
# 11. Post Merge Section
#################################################################
#
#
# Generate QA counts for the new insertion
#
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    set feedback off
    exec MEME_INTEGRITY.src_monster_qa;
EOF
if ($status != 0) then
   echo "Error"
   exit 1
endif


#
# Make old source unreleasable
#
if (-e classes_atoms.src) then
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    set feedback off

    exec meme_utility.drop_it('table','tbac');
    create table tbac as
    select '$old_source' as source, 'n' as tobereleased from dual;
   
    update foreign_classes set tobereleased='n' where source='$old_source';

    exec meme_utility.drop_it('table','tbac');
    create table tbac as
    select atom_id as row_id
    from classes where source='SRC'
    and code = 'V-$old_source';

EOF
if ($status != 0) then
   echo "Error"
   exit 1
endif
endif

$MEME_HOME/bin/batch.pl -host $host -port $port -n N -a T -t C -s t tbac $db $authority >&! batch.unapprove.log

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    set serveroutput on size 100000
    set feedback off

    exec MEME_UTILITY.drop_it('table','t_$old_source');    
    CREATE TABLE t_$old_source (
	row_id NUMBER );

    INSERT INTO t_$old_source
    SELECT atom_id FROM classes
    WHERE source = 'SRC'
      AND concept_id IN
      (SELECT concept_id FROM classes a, atoms b
       WHERE source='SRC'
 	 AND termgroup = 'SRC/VAB'
	 AND a.atom_id = b.atom_id
	 AND atom_name = '$old_source' );

    exec MEME_UTILITY.drop_it('table','t_rel_$old_source');
    CREATE TABLE t_rel_$old_source AS
    SELECT  concept_id_1,concept_id_2,atom_id_1,atom_id_2,
                   relationship_name,relationship_attribute,
                   source, source_of_label,status,generated_status,
                   relationship_level,released,tobereleased,
                   relationship_id, suppressible,
		   sg_id_1, sg_type_1, sg_qualifier_1,
		   sg_id_2, sg_type_2, sg_qualifier_2
    FROM relationships WHERE 1=0;

    INSERT INTO t_rel_$old_source
    SELECT concept_id_1,concept_id_2,0,0,
                   'BRT','',
                   '$new_source', '$new_source','R','Y',
                   'C', 'N', 'Y', 0, 'N','','','','','',''
    FROM relationships 
    WHERE atom_id_1 IN (SELECT * FROM t_$old_source) 
      AND relationship_attribute = 'has_version'
      AND relationship_level = 'S'
    UNION
    SELECT concept_id_2,concept_id_1,0,0,
                   'BRT','',
                   '$new_source', '$new_source','R','Y',
                   'C', 'N', 'Y', 0, 'N','','','','','',''
    FROM relationships 
    WHERE atom_id_2 IN (SELECT * FROM t_$old_source) 
      AND relationship_attribute = 'version_of'
      AND relationship_level = 'S';

EOF
if ($status != 0) then
    echo 'Error preparing bequeathal rels'
    cat /tmp/t.$$.log
    exit 1
endif

$MEME_HOME/bin/insert.pl -host $host -port $port -rels t_rel_$old_source $db $new_source >&! insert.bequeathal.log 
if ($status != 0) then
   echo "Error"
   cat insert.bequeathal.log
   exit 1
endif

#
# What about safe-replacement?  We should set the last_release_rank
# flag, otherwise ranks will change?  SHould we just match to previous
# version? or find previous version through safe-replacement facts?!
#
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    WHENEVER SQLERROR EXIT -1
    -- create a map of atom_ids in foreign_classes (new source)
    -- to atom_ids in foreign_classes (with lrr field).
    exec meme_utility.drop_it('table','t_lrr_$$');
    CREATE TABLE t_lrr_$$ as
    SELECT a.atom_id as old_atom_id, b.atom_id as new_atom_id,
	a.last_release_rank, a.last_release_cui, e.rank
    FROM foreign_classes a, foreign_classes b,
         classes c, classes d, mom_safe_replacement e
    WHERE a.eng_atom_id = c.atom_id AND b.eng_atom_id = d.atom_id
      AND c.atom_id = e.old_atom_id and d.atom_id=new_atom_id
      AND a.source = '$old_source'
      AND b.source = '$new_source'
      AND a.tty=b.tty
    -- For update sources like MSH where we
    -- preserve old atom ids, we need an additional query
    -- FIX THIS RANKING ALGORITHM!
    UNION
    SELECT a.atom_id as old_atom_id, b.atom_id as new_atom_id,
      	a.last_release_rank, a.last_release_cui, 
        '9' || a.atom_id || b.atom_id as rank
    FROM foreign_classes a, foreign_classes b
    WHERE a.eng_atom_id = b.eng_atom_id
      AND a.tty = b.tty
      AND a.source = '$old_source'
      AND b.source = '$new_source'
    UNION
    SELECT a.atom_id as old_atom_id, b.atom_id as new_atom_id,
      	a.last_release_rank, a.last_release_cui, 
        '9' || a.atom_id || b.atom_id as rank
    FROM foreign_classes a, foreign_classes b
    WHERE a.aui = b.aui
      AND a.source = '$old_source'
      AND b.source = '$new_source';


    -- get rid of non highest ranking ones
    -- if there are ties, keep highest last_release_rank,
    -- if there are still ties, keep the highest old_atom_id
    DELETE FROM t_lrr_$$ 
    WHERE (new_atom_id,rank||last_release_rank||old_atom_id) IN
    (SELECT new_atom_id, rank||last_release_rank||old_atom_id FROM t_lrr_$$
     MINUS
     SELECT new_atom_id, max(rank||last_release_rank||old_atom_id) 
     FROM t_lrr_$$ GROUP BY new_atom_id);     

    analyze table t_lrr_$$ compute statistics;

    -- fix foreign classes
    DECLARE

       CURSOR cur IS
         SELECT b.last_release_rank, b.last_release_cui, a.atom_id
	 FROM foreign_classes a, t_lrr_$$ b
	 WHERE a.atom_id = new_atom_id;

       cv   cur%ROWTYPE;
       ct   INTEGER;
    BEGIN
       ct := 0;
       OPEN cur;
       LOOP
           FETCH cur INTO cv;
	   EXIT WHEN cur%NOTFOUND;

	   UPDATE foreign_classes 
	   SET last_release_rank=cv.last_release_rank,
	       last_release_cui=cv.last_release_cui
	   WHERE atom_id = cv.atom_id;

	   ct := ct + 1;
       END LOOP;
       MEME_UTILITY.put_message(ct || ' rows updated.');
    END;
/

    drop table t_lrr_$$;

EOF
if ($status != 0) then
   echo "Error"
   exit 1
endif

#1. SELECT count(*) from foreign_classes where source='$new_source'
#   SELECT count(*) from source_classes_atoms
#  These queries should return the same count.
#2. Select sui,lui,isui from foreign_classes minus
#   select sui,lui,isui from string_ui;
#  This query should return no rows.



