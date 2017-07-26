#!/bin/csh -f
#
# File:   insert_relationships.csh
# Author: David Hernandez, Brian Carlsen
#
# Remarks: This script reloads relationships from relationships.ssrc.
#
# CHANGES
# 02/24/2009 BAC (1-GCLNT): Explicit assign ruis call.
# 08/12/2008 JFW (1-IDJXN): Fix $max_id setting for relationships.src.
# 01/31/2008 BAC (1-GCLNT): Call load_src.csh with max ids. Handle QA.
#  10/29/2007 JFW (1-FMOG9): Disallow removal of SRC relationships..
#  06/04/2007 BAC (1-EDYH3): when using replace mode drop correct
#           tables.  Improve error handling.
#  12/07/2006 BAC (1-CX8DH): Fix to "set replace=....".  Also
#               fix to "if ($replace)" => "if ($replace == 1)"
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
# Check that relationships.src exists 
#
if (! (-e ./relationships.src)) then
    echo "There must be an relationships.src file in the current directory."
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
	      description => 'Insert Relationships for $source.'));
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

echo "    Load relationships ...`/bin/date`"
set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select max_id from max_tab where table_name='RELATIONSHIPS'"`
$MEME_HOME/bin/load_src.csh $db relationships.src $max_id >&! /tmp/t.$$.log
if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading relationships.src"
    exit 1
endif

if ($replace == 1) then
    echo "    Removing old relationships... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus $user@$db <<EOF 
    WHENEVER SQLERROR EXIT -1;
    alter session set sort_area_size=200000000;   
    alter session set hash_area_size=200000000;
    
    exec MEME_UTILITY.drop_it('table','trel_$$');

    CREATE TABLE trel_$$ NOLOGGING AS 
    SELECT /*+ PARALLEL(a) */ relationship_id AS row_id 
    FROM relationships a WHERE (source,source_of_label) IN 
      (SELECT source,source_of_label FROM source_relationships)
    AND source != 'SRC';

    -- Need to check return value and fail
    DECLARE
      retval NUMBER;
    BEGIN
        retval := 
            meme_batch_actions.batch_action (
              action => 'D', 
              id_type => 'R', 
              table_name => 'trel_$$', 
              status => 'R', 
              authority => '$source', 
              work_id => $work_id );
        IF retval < 0 THEN
          RAISE_APPLICATION_ERROR(-20000,
			    'Bad Return Value');
        END IF;
    END;
/
    DROP TABLE trel_$$;
EOF
    if ($status != 0) then
    	echo "deleting relationships failed."
    	exit 1
	endif

endif

echo "    Assign MEME ids... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF 
    set serveroutput on size 100000
    set feedback off
    WHENEVER SQLERROR EXIT -1;
    exec MEME_SOURCE_PROCESSING.assign_meme_ids ( -
        table_name => 'R', -
        authority => '$source', -
        work_id => $work_id );

EOF
if ($status != 0) then
    echo "Assigning meme ids failed."
    exit 1
endif
	
echo "    Map & insert R... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 100000
    set feedback off
    WHENEVER SQLERROR EXIT -1;
    exec MEME_SOURCE_PROCESSING.map_to_meme_ids ( -
        table_name => 'R', -
        authority => '$source', -
        work_id => $work_id );
    exec MEME_SOURCE_PROCESSING.assign_ruis ( -
        table_name => 'SR', -
        authority => '$source', -
        work_id => $work_id );
    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
        table_name => 'R', -
        authority => '$source', -
        work_id => $work_id );
EOF
if ($status != 0) then
    echo "Map and insert failed."
    exit 1
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

