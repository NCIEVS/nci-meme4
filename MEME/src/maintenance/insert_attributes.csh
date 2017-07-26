#!/bin/csh -f
#
# File:    insert_attributes.csh
# Author:  David Ray Hernandez
#
# REMARKS: This script functions to insert attributes only
#          
#          This script should be run from the directory containing
#          an attributes.src file.
#
# Changes:
#   05/10/2006 BAC (1-B6CEZ): initial release
#   10/07/2004 Edited to check for attributes.src, show progress
#                     Should add error handling  -jfw

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

if ($?MEME_HOME == 0) then
    echo '$MEME_HOME must be set'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($#argv == 2) then
    set mode="run"
    set db=$1
	set authority=$2
	set new_source=$2
else if ($#argv == 3 && "$1" == "-t") then
    set mode="test"
    set db=$2
	set authority=$3
	set new_source=$3
else
    echo "Usage: $0 [-t] <database> <source>"
    exit 1
endif 

set ct=`ls |fgrep -c attributes.src`
if ($ct == 0) then
    echo "You must run this script from the directory containing the attributes.src file"
    exit 1
endif

###################################################################
# 5. Get username/password and work_id
#
# If you are running this script by hand and not as a script
# you have to run the following commands to set $user and $work_id
###################################################################
set user=`/site/umls/scripts/get-oracle-pwd.pl`

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! /tmp/work_id
    set feedback off
    set serveroutput on size 100000
    exec dbms_output.put_line ( -
         meme_utility.new_work( authority => '$new_source',  type => 'INSERTION', description => 'Add attributes for $new_source'));
EOF
set work_id=`cat /tmp/work_id`
\rm -f /tmp/work_id

echo "-------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "-------------------------------------------------"
echo "authority:    $authority"
echo "new_source:   $new_source"
echo "db:           $db"
echo "work_id:      $work_id"
echo ""

#
# load attributes into the source table
#
if (-e attributes.src) then
    echo "    Generating/loading strings and attributes... `/bin/date`"
    # generate stringtab rows
    $MEME_HOME/bin/atts_to_stringtab.csh .
    # load stringtab rows into source_stringtab
    $MEME_HOME/bin/load_src.csh $db stringtab.src
    # load attributes
    $MEME_HOME/bin/load_src.csh $db attributes.src
endif

#
# Prep insertion indexes (run in background)
#
echo "    Prep indexes... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log &
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    alter session set sort_area_size=200000000;   
    alter session set hash_area_size=200000000;

	exec MEME_SOURCE_PROCESSING.create_insertion_indexes (-
	   authority => '$authority', -
	   work_id => $work_id);

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error preparing and indexes"
    exit 1
endif

#
# Assign attribute ids
#
echo "    Assign MEME ids (A)... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    set serveroutput on size 100000
    set feedback off
    WHENEVER SQLERROR EXIT -1

    exec MEME_SOURCE_PROCESSING.assign_meme_ids ( -
        table_name => 'A', -
        authority => '$new_source', -
        work_id => $work_id );
EOF
if ($status != 0) then
   echo "Error assigning MEME ids (A)"
   exit 1
endif

#
# Wait for background process to catch-up
#
wait

#
# Map attributes to meme ids
#
echo "    Map to MEME ids (A)... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    set feedback off
 
    exec MEME_SOURCE_PROCESSING.map_to_meme_ids ( -
        table_name => 'A', -
        authority => '$authority', -
        work_id => $work_id );
EOF
if($status != 0) then
    echo "Error mapping MEME ids"
    exit 1
endif

#
# Insert attributes
#
echo "    Insert attributes... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 100000
    set feedback off
    WHENEVER SQLERROR EXIT -1
    --exec MEME_SOURCE_PROCESSING.assign_atuis ( -
    --    table_name => 'SA', -
    --    authority => '$authority', -
    --    work_id => $work_id );
    --exec MEME_SOURCE_PROCESSING.source_replacement ( -
    --    table_name => 'A', -
    --    authority => '$authority', -
    --    work_id => $work_id );
    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
	table_name => 'A', -
	authority => '$authority', -
	work_id => $work_id );
EOF
if($status != 0) then
  echo "Error"
  exit 1
endif

#
# Cleanup (background)
#
echo "    Removing temporary indexes ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF &
    WHENEVER SQLERROR EXIT -1
    set serveroutput on size 100000
    set feedback off
    exec MEME_SOURCE_PROCESSING.drop_insertion_indexes;
EOF

#
# Run Matrix initializer
#
echo "    Running matrix initializer ... `/bin/date`"
$MEME_HOME/bin/matrixinit.pl -I $db >&! matrixinit.log

echo "-------------------------------------------------"
echo "Finished (work_id=$work_id) ... `/bin/date`"
echo "-------------------------------------------------"
