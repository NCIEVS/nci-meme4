#!/bin/csh -f
#
# File:   insert_attributes.csh
# Author: David Hernandez, Brian Carlsen
#
# Remarks: This script loads attributes.
# 
# CHANGES
# 02/24/2009 BAC (1-GCLNT): Explicit assign atuis call.
# 08/12/2008 JFW (1-IDJXN): Fix $max_id call for stringtab.src.
# 01/31/2008 BAC (1-GCLNT): Call load_src.csh with max ids. Handle QA.
#  10/29/2007 JFW (1-FMOG9): Disallow removal of SRC attributes.
#  06/04/2007 BAC (1-EDYH3): Improve error handling.
#  03/22/2007 BAC (1-DTESH): Fixed typo: attributess
#  12/07/2006 BAC (1-CX8DH): Fix to "set replace=...."
#  11/28/2006 BAC (1-CX8DH): fix to replace section table name passed to MBA.
#
# Check required variables
#
set required_vars = ("MEME_HOME" "ORACLE_HOME")
foreach rv ($required_vars)
    if (`eval 'echo $?'$rv` == 0) then
        echo '$'$rv' must be set.'
    endif
end

#
# Parse arguments
#
if ($#argv == 2) then
	set db=$1
	set source=$2
	set replace=0
else if ($#argv == 3) then
	set db=$1
	set source=$2
	set replace=$3
else
    echo "   Usage: $0 <database> <VSAB> [<replace flag 1|0>]"
    exit 1
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

#
# Check that attributes.src exists 
#
if (! (-e ./attributes.src)) then
    echo "There must be an attributes.src file in the current directory."
    exit 1
endif

echo "Get work id ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! /tmp/work_id
    set feedback off
    set serveroutput on size 100000
    exec dbms_output.put_line ( -
         meme_utility.new_work( -
	      authority => '$source', -
              type => 'MAINTENANCE', -
	      description => 'Insert attributes for $source.'));
EOF
set work_id=`cat /tmp/work_id`
\rm -f /tmp/work_id

echo "-----------------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "-----------------------------------------------------------"
echo "DB:         $db"
echo "source:     $source"
echo "work_id:    $work_id"
echo ""

echo "    Truncate source tables and prep indexes... `/bin/date`"

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1;
    set serveroutput on size 100000
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;

    BEGIN
      meme_system.truncate('source_attributes');
      meme_system.rebuild_table('source_attributes');
      meme_system.truncate('source_stringtab');
      meme_system.rebuild_table('source_stringtab');
      meme_system.truncate('source_classes_atoms');
      meme_system.rebuild_table('source_classes_atoms');
      meme_system.truncate('source_concept_status');
      meme_system.rebuild_table('source_concept_Status');
      meme_system.truncate('source_relationships');
      meme_system.rebuild_table('source_relationships');
      meme_system.truncate('source_context_relationships');
      meme_system.rebuild_table('source_context_relationships');
      meme_system.truncate('source_source_rank');
      meme_system.truncate('source_termgroup_rank');
      meme_system.reindex('molecular_actions','N',' ');
      meme_system.reindex('atomic_actions','N',' ');
    END;
/

    exec MEME_SOURCE_PROCESSING.create_insertion_indexes (-
        authority => '$source', -
        work_id => $work_id );

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error truncating source tables and creating insertion indexes"
    exit 1
endif

echo "    Loading attributes... `/bin/date`"
# generate stringtab rows
$MEME_HOME/bin/atts_to_stringtab.csh . >&! /tmp/t.$$.log
if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error converting attributes to stringtab"
    exit 1
endif
# load stringtab rows into source_stringtab
set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select row_sequence-1 max_id from stringtab where string_id=-1"`
$MEME_HOME/bin/load_src.csh $db stringtab.src $max_id >&! /tmp/t.$$.log
if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error converting attributes.src to stringtab.src"
    exit 1
endif
# load attributes
set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select max_id from max_tab where table_name='ATTRIBUTES'"`
$MEME_HOME/bin/load_src.csh $db attributes.src $max_id >&! /tmp/t.$$.log
if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading attributes.src"
    exit 1
endif

if ($replace == 1) then
    echo "    Removing old attributes... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus $user@$db <<EOF 

    WHENEVER SQLERROR EXIT -1;
    alter session set sort_area_size=200000000;   
    alter session set hash_area_size=200000000;
    
    exec MEME_UTILITY.drop_it('table','tatt_$$');

    CREATE TABLE tatt_$$ NOLOGGING AS 
    SELECT /*+ PARALLEL(a) */ attribute_id AS row_id 
    FROM attributes a WHERE (source, attribute_name) IN 
      (SELECT source, attribute_name FROM source_attributes)
    AND source != 'SRC';

    DECLARE
      retval NUMBER;
    BEGIN
        retval := 
          meme_batch_actions.batch_action (
            action => 'D', 
            id_type => 'A', 
            table_name => 'tatt_$$', 
            status => 'R', 
            authority => '$source', 
            work_id => $work_id );
        IF retval < 0 THEN
          RAISE_APPLICATION_ERROR(-20000,
			    'Bad Return Value');
        END IF;
    END;
/
    DROP TABLE tatt_$$;
EOF

    if ($status != 0) then
    	echo "deleting attributes failed."
    	exit 1
	endif

endif

echo "    Assign MEME ids... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF 
    set serveroutput on size 100000
    set feedback off
    WHENEVER SQLERROR EXIT -1;
    exec MEME_SOURCE_PROCESSING.assign_meme_ids ( -
        table_name => 'A', -
        authority => '$source', -
        work_id => $work_id );
EOF
if ($status != 0) then
    echo "Assign meme ids failed"
    exit 1
endif

echo "    Map & insert A... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 100000
    set feedback off
    WHENEVER SQLERROR EXIT -1;
    exec MEME_SOURCE_PROCESSING.map_to_meme_ids ( -
        table_name => 'A', -
        authority => '$source', -
        work_id => $work_id );
    exec MEME_SOURCE_PROCESSING.assign_atuis ( -
        table_name => 'SA', -
        authority => '$source', -
        work_id => $work_id );
    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
        table_name => 'A', -
        authority => '$source', -
        work_id => $work_id );
EOF
if ($status != 0) then
    echo "Map and insert failed"
    exit 1
endif

if (`grep -c SEMANTIC_TYPE attributes.src` != 0) then
echo "   Resolve STYs (if any) ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    set serveroutput on size 100000
    set feedback off
    ALTER SESSION SET sort_area_size=200000000;
    ALTER SESSION SET hash_area_size=200000000;
    exec MEME_SOURCE_PROCESSING.resolve_stys ( -
        source => '$source', -
        sty_fate => 'L', -
        authority => '$source', -
        work_id => $work_id );
EOF
if ($status != 0) then
    echo "Error resolving STYs"
    cat /tmp/t.$$.log
    exit 1
endif
endif

$MEME_HOME/bin/matrixinit.pl -I $db >&! matrixinit.log & 

echo "    Drop insertion indexes... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1;
    set serveroutput on size 100000
    alter session set sort_area_size=200000000;   
    alter session set hash_area_size=200000000;

    -- Regenerate QA counts, will produce counts with same name but different timestamp.
    exec MEME_INTEGRITY.src_monster_qa;
    -- Remove samples relating to duplicate name,value
    DELETE FROM src_qa_samples 
    WHERE value like '$source%' AND (name,value) IN
     (SELECT name,value FROM src_qa_results 
      WHERE value like '$source%'
      GROUP BY name,value having count(distinct timestamp)>1);

	-- Remove older QA counts
    DELETE FROM src_qa_results 
    WHERE value like '$source%' AND (name,value,timestamp) IN
     (SELECT name, value, min(timestamp) FROM src_qa_results 
      WHERE value like '$source%'
      GROUP BY name,value having count(distinct timestamp)>1);
      
    -- Regenerate samples
    exec MEME_INTEGRITY.src_qa_sampling;

    exec MEME_SOURCE_PROCESSING.drop_insertion_indexes('$source',$work_id);

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error dropping insertion indexes"
    exit 1
endif

echo "-----------------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "-----------------------------------------------------------"

