#!/bin/csh -f
#
# File:   load_atom_notes.csh
# Author: Priya Mathur
#
# Remarks: This script loads atom notes - these are non releasable ATOM_NOTE type attributes
#          generated for a source upon Editor's request. This is not the norm. Only sources
#          for which this is being done are RXNORM, OMIM and HGNC.
# CHANGES
# 05/29/2015: Added
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
if ($#argv == 3) then
        set db=$1
        set new_source=$2
        set authority=$3
    set host=`$MIDSVCS_HOME/bin/midsvcs.pl -s insertion-meme-server-host`
    set port=`$MIDSVCS_HOME/bin/midsvcs.pl -s insertion-meme-server-port`
else
    echo "   Usage: $0 <database> <source> <authority> "
    exit 1
endif


if ($?MEME_HOME == 0) then
    echo '$MEME_HOME must be set'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif


set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
set source=$new_source
echo "Get work id ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! /tmp/work_id
    set feedback off
    set serveroutput on size 100000
    exec dbms_output.put_line ( -
         meme_utility.new_work( -
              authority => '$new_source', -
              type => 'MAINTENANCE', -
              description => 'Insert attributes for $new_source.'));
EOF
set work_id=`cat /tmp/work_id`
\rm -f /tmp/work_id


echo "-------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "-------------------------------------------------"
echo "authority:    $authority"
echo "source:   $source"
echo "db:           $db"
echo "work_id:      $work_id"
echo ""

#################################################################
#
# Start by clearing all of the source tables
#
echo "    Truncate source tables and prep indexes... `/bin/date`"

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1;
    set serveroutput on size 100000
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;

    DECLARE
      l_start   INTEGER;
      l_end     INTEGER;

    BEGIN
      l_start := DBMS_UTILITY.get_time;
    meme_system.truncate('source_attributes');
    meme_system.rebuild_table('source_attributes');
    meme_system.truncate('source_stringtab');
    meme_system.rebuild_table('source_stringtab');
    meme_system.truncate('source_classes_atoms');
    meme_system.rebuild_table('source_classes_atoms');
    meme_system.truncate('source_concept_status');
    meme_system.rebuild_table('source_concept_Status');
    meme_system.truncate('source_relationships');
    meme_system.truncate('source_context_relationships');
    meme_system.rebuild_table('source_context_relationships');
    meme_system.truncate('source_termgroup_rank');

      l_end := DBMS_UTILITY.get_time;
      MEME_UTILITY.log_operation(
        authority => '$authority',
        activity => 'Load Section',
        detail => 'Truncate source tables',
        work_id => $work_id,
        transaction_id => 0,
        elapsed_time => (l_end - l_start) * 10);
    END;
/

    exec MEME_SOURCE_PROCESSING.create_insertion_indexes (-
        authority => '$authority', -
        work_id => $work_id );

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error truncating source tables"
    exit 1
endif

#
# Load classes
#
#
# Load attributes
#
if (-e attributes.src) then
    echo "    Processing long attributes ... `/bin/date`"
    # generate stringtab rows
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/atts_to_stringtab.csh . >&! /tmp/t.$$.log
    if ($status != 0) then
        cat /tmp/t.$$.log
        echo "Error converting attributes"
        exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Generate stringtab.src" $work_id 0 $start_t >> /dev/null


    echo "    Loading long attributes ... `/bin/date`"
    # load stringtab rows into source_stringtab
    set start_t=`$PATH_TO_PERL -e 'print time'`
    # use -1 to adjust for what load_src.csh does
    set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select row_sequence-1 max_id from stringtab where string_id=-1"`

    $MEME_HOME/bin/load_src.csh $db stringtab.src $max_id >&! /tmp/t.$$.log
    if ($status != 0) then
        cat /tmp/t.$$.log
        echo "Error loading stringtab! "
        exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load stringtab.src" $work_id 0 $start_t >> /dev/null

    echo "    Loading attributes ... `/bin/date`"
    # load attributes
    set start_t=`$PATH_TO_PERL -e 'print time'`
    set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select max_id from max_tab where table_name='ATTRIBUTES'"`
    $MEME_HOME/bin/load_src.csh $db attributes.src $max_id >&! /tmp/t.$$.log
    if ($status != 0) then
        cat /tmp/t.$$.log
        echo "Error loading attributes"
        exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load attributes.src" $work_id 0 $start_t >> /dev/null

endif

#
# Load termgroups/sources
#
if (-e sources.src && -e termgroups.src) then
    echo "    Loading termgroup metadata ... `/bin/date`"
    # load termgroups
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db termgroups.src 0 >&! /tmp/t.$$.log
    if ($status != 0) then
        cat /tmp/t.$$.log
        echo "Error loading termgroups"
        exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load termgroups.src" $work_id 0 $start_t >> /dev/null

    echo "    Loading source metadata ... `/bin/date`"
    # load sources
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db sources.src 0 >&! /tmp/t.$$.log
    if ($status != 0) then
        cat /tmp/t.$$.log
        echo "Error loading sources"
        exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load sources.src" $work_id 0 $start_t >> /dev/null

endif

#################################################################
# Assign ids
#################################################################
#
# This step assigns LUI, SUI, and ISUI values to the new atoms
# It also assigns meme_ids
#
echo "    Assign MEME ids ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;
    exec MEME_SOURCE_PROCESSING.assign_meme_ids ( -
        table_name => 'ALL', -
        authority => '$new_source', -
        work_id => $work_id );
EOF

if ($status != 0) exit 1

#################################################################
# Insert source_ids
#################################################################
#
# This step inserts source_id-meme_id map into source_id_map
# for everything we are loading.
#
echo "    Inserting source ids ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;
    exec MEME_SOURCE_PROCESSING.insert_source_ids ( -
        table_name => 'ALL', -
        authority => '$new_source', -
        work_id => $work_id );
EOF
    if ($status != 0) then
        print "Error inserting source ids"
        exit 1
    endif

#
# assign auis
#

#################################################################
# Insert Data
#################################################################
#
# This step inserts the data from the source* core tables into
# the actual core tables.
#
# Classes & Concepts
#
if (-e classes_atoms.src) then
    echo "    Inserting concepts ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;

    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
        table_name => 'CS', -
        authority => '$authority', -
        work_id => $work_id );
EOF
    if ($status != 0) exit 1

    echo "    Inserting classes ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;

    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
        table_name => 'C', -
        authority => '$authority', -
        work_id => $work_id );
EOF
    if ($status != 0) exit 1
endif

#
# Attributes
#
if (-e attributes.src) then
    echo "    Mapping attribute atom/concept_id ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;
    exec MEME_SOURCE_PROCESSING.map_to_meme_ids ( -
        table_name => 'A', -
        authority => '$authority', -
        work_id => $work_id );

    exec MEME_SOURCE_PROCESSING.assign_atuis ( -
        table_name => 'SA', -
        authority => '$authority', -
        work_id => $work_id );

EOF
    if ($status != 0) exit 1

    echo "    Inserting attributes ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;
    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
        table_name => 'A', -
        authority => '$authority', -
        work_id => $work_id );
EOF


echo "-----------------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "-----------------------------------------------------------"