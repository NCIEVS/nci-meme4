#!/bin/csh -f
#
# File:    load_section.csh
# Author:  Brian Carlsen
#
# REMARKS: This script is used to perform all steps up through
#          merging.
#
#          To perform pre-insert merge steps, set s1=1,s2=0
#          then run the script, then do preinsert merging
#          then set s1=0,s2=1 and run the script.
#
# Dependencies: 
#   requires $MEME_HOME,$ORACLE_HOME to be set
#   $MEME_HOME/bin/load_src.csh must exist
#   $MEME_HOME/bin/atts_to_stringtab.csh must exist
#   $MEME_HOME/bin/raw_to_contexts.csh must exist -- not anymore
#   $MEME_HOME/bin/classes_to_strings.csh must exist
#   MEME_SOURCE_PROCESSING must be loaded into the database
#
# Changes
# 02/24/2009 BAC (1-GCLNT): parallelize some parts.
# 03/24/2008 BAC (1-GE1Q9): Moved qaStats load section up with sources/termgroups.
# 03/18/2008 TK (1-GE1Q9) : Added the call to load qaStats.src
# 01/31/2008 BAC (1-GCLNT): Call load_src.csh with max ids.
# 01/22/2008 TK (1-G9SHX) : Added the store procedure call to sample QA data.
# 09/25/2007 JFW (noticket): relabel output to log for "Assign AUIs"
# 05/31/2007 BAC (1-DKO45): improve insertion performance for contexts
# 12/29/2006 BAC (1-D57UF): clean up comments, call load_mrdoc.csh instead of duplicating logic
# 08/07/2006 JFW (1-BV4F5): move source_replacement for classes to load section part 1
# 07/24/2006 JFW (1-BQVYJ): fix tty_class handling ("tty_class being removed")
# 06/15/2006 BAC (1-AVWUX): fix to tty_class handling.
# 05/26/2006 BAC (1-BAMGR): create_insertion_indexes, drop_insertion_indexes
# 05/11/2006 BAC (1-B6MB1): double-check termgroups.src
# 04/11/2006 JFW (1-AVWUX): Handle tty_class when processing MRDOC.RRF.
#
# 03/09/2006 BAC (1-AMQF3): re-order the source_replacement('C'...) call and the
#    core_table_insert('CS',...) to allow only those "new" concepts to be inserted
#    that will make it through to the core_table_insert('C'...) call.
#

#
# Version history
# 12/30/2004 (4.5.0): Manages log_operation calls better
# 11/19/2004 (4.4.0): Released, runs on electra
# 08/09/2004 (4.3.1): Create indexes after truncating source tables
# 03/08/2004 (4.3.0): Makes use of source_replacement, parallelizes index creation
# 10/01/2003 (4.2.0): Better Index management, better performancey
# 03/18/2003 (4.1.0): Ported to MEME4
#
set release=4
set version=5.0
set authority="BAC"
set date="12/30/2004"

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

set s1=0
set s2=0

if ($#argv != 7) then
    echo "Usage: $0 <authority> <old source> <new source> <stripped source>"
    echo "              <database> <work_id> <startop 1,2,3>"
    exit 1
endif

set authority=$1
set old_source=$2
set new_source=$3
set stripped_source=$4
set db=$5

if ($7 == "1") then
    set s1=1
else if ($7 == "2") then
    set s2=1
else if ($7 == "3") then
    set s1=1
    set s2=1
endif

if ($?MEME_HOME == 0) then
    echo '$MEME_HOME must be set'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

set ct=`ls |fgrep -c .src`
if ($ct == 0) then
    echo "You must run this script from the directory containing the src files"
    exit 1
endif

if ((! -e sources.src) || (! -e termgroups.src)) then
    echo "The files sources.src and termgroups.src must exist"
    exit 1
endif

set ct=`$PATH_TO_PERL -ne 'split /\|/; @f=split/\//,$_[0]; print if $_[5] ne $f[1];' termgroups.src | wc -l`
if ($ct != 0) then
    echo "Error: termgroups do not match TTY in termgroups.src"
	$PATH_TO_PERL -ne 'split /\|/; @f=split/\//,$_[0]; print if $_[5] ne $f[1];' termgroups.src
    exit 1
endif

###################################################################
# Get username/password and work_id
#
# If you are running this script by hand and not as a script
# you have to run the following commands to set $user and $work_id
###################################################################
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
set work_id=$6

echo "-------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "-------------------------------------------------"
echo "authority:    $authority"
echo "old_source:   $old_source"
echo "new_source:   $new_source"
echo "stripped_source: $stripped_source"
echo "db:           $db"
echo "work_id:      $work_id"
echo ""

#################################################################
# 
#  Start RECIPE
#
#################################################################

#################################################################
# Load Section
#################################################################

if ($s1 == 1) then

#################################################################
# Load .src files into the source tables
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
      l_start	INTEGER;
      l_end	INTEGER;

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
    meme_system.rebuild_table('source_relationships');
    meme_system.truncate('source_context_relationships');
    meme_system.rebuild_table('source_context_relationships');
    meme_system.truncate('source_source_rank');
    meme_system.truncate('source_termgroup_rank');
    meme_system.reindex('molecular_actions','N',' ');
    meme_system.reindex('atomic_actions','N',' ');

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
# Generate strings.src and stringtab.src
#
set start_t=`$PATH_TO_PERL -e 'print time'`
echo "    Generating strings.src and stringtab.src ... `/bin/date`"
if (-e classes_atoms.src) then
    # generate strings file, norm strings
    $MEME_HOME/bin/classes_to_strings.csh . ENG >&! /tmp/t.a.$$.log &
endif

if (-e attributes.src) then
    # generate stringtab rows
    $MEME_HOME/bin/atts_to_stringtab.csh . >&! /tmp/t.b.$$.log &
endif

#
# Wait for parallel conversion processes
#
set ef = 0
wait
if ($status != 0) then
    set ef = 1
endif
if (`grep -c ERROR /tmp/t.a.$$.log` > 0) then
    cat /tmp/t.b.$$.log
    echo "Error norming atoms"
    set ef = 1
endif
$MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Generate strings.src" $work_id 0 $start_t >> /dev/null
if (`grep -c ERROR /tmp/t.b.$$.log` > 0) then
    cat /tmp/t.a.$$.log
    echo "Error making stringtab"
    set ef = 1
endif
$MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Generate stringtab.src" $work_id 0 $start_t >> /dev/null

if ($ef == 1) then
    exit 1
endif


echo "    Loading src files ... `/bin/date`"
set start_t=`$PATH_TO_PERL -e 'print time'`

#
# Load classes
#
if (-e classes_atoms.src) then
    # load classes rows
    set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select max_id from max_tab where table_name='ATOMS'"`
    $MEME_HOME/bin/load_src.csh $db classes_atoms.src $max_id >&! /tmp/t.c.$$.log &

    # load strings
    $MEME_HOME/bin/load_src.csh $db strings.src 0 >&! /tmp/t.c2.$$.log &
endif

#
# Load attributes
#
if (-e attributes.src) then
    # use -1 to adjust for what load_src.csh does
    set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select row_sequence-1 max_id from stringtab where string_id=-1"`
    $MEME_HOME/bin/load_src.csh $db stringtab.src $max_id >&! /tmp/t.a.$$.log &

    set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select max_id from max_tab where table_name='ATTRIBUTES'"`
    $MEME_HOME/bin/load_src.csh $db attributes.src $max_id >&! /tmp/t.a2.$$.log &

endif

#
# Load relationships
#
if (-e relationships.src) then
    set max_rel_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select max_id from max_tab where table_name='RELATIONSHIPS'"`
    $MEME_HOME/bin/load_src.csh $db relationships.src $max_rel_id >&! /tmp/t.r.$$.log &
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


#
# Load qaStats
#
if (-e qaStats.src) then
    echo "    Loading inversion statistics... `/bin/date`"
    # load inversion statistics
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db qaStats.src 0 >&! /tmp/t.$$.log
    if ($status != 0) then
            cat /tmp/t.$$.log
            echo "Error loading inversion statistics"
            exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load qaStats.src" $work_id 0 $start_t >> /dev/null
endif

#
# Load MRDOC.RRF data
#
if (-e MRDOC.RRF) then
    echo "    Load MRDOC.RRF ... `/bin/date`"
    $MEME_HOME/bin/load_mrdoc.csh $db >&! /tmp/t.$$.log
    if ($status != 0) then
      echo "Error running load_mrdoc.csh"
      cat /tmp/t.$$.log
      exit 1
    endif
endif

#
# WAIT for parallel loads
#
set ef = 0
wait
if ($status != 0) then
    set ef = 1
endif

#
# Error checking for loads
#
if (-e classes_atoms.src) then
    if (`grep -c ERROR /tmp/t.c.$$.log` > 0) then
        cat /tmp/t.c.$$.log
        echo "Error loading atoms"
        set ef = 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load classes_atoms.src" $work_id 0 $start_t >> /dev/null

    if (`grep -c ERROR /tmp/t.c2.$$.log` > 0) then
        cat /tmp/t.c2.$$.log
        echo "Error loading strings"
        set ef = 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load strings.src" $work_id 0 $start_t >> /dev/null
endif 

if (-e attributes.src) then
    if (`grep -c ERROR /tmp/t.a.$$.log` > 0) then
        cat /tmp/t.a.$$.log
        echo "Error loading stringtab! "
        set ef = 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load stringtab.src" $work_id 0 $start_t >> /dev/null

    if (`grep -c ERROR /tmp/t.a2.$$.log` > 0) then
        cat /tmp/t.a2.$$.log
        echo "Error loading attributes"
        set ef = 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load attributes.src" $work_id 0 $start_t >> /dev/null
endif

if (-e relationships.src) then
    if (`grep -c ERROR /tmp/t.r.$$.log` > 0) then
        cat /tmp/t.r.$$.log
        echo "Error loading relationships"
        set ef = 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load relationships.src" $work_id 0 $start_t >> /dev/null
endif

if ($ef == 1) then
    exit 1
endif

#################################################################
# Insert ranks
#################################################################
#
# This step inserts the new termgroups/sources into the corresponding
# ranks tables.
#
echo "    Inserting source/termgroup ranks ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    exec MEME_SOURCE_PROCESSING.insert_ranks ( -
    authority => '$new_source', -
    work_id => $work_id );
EOF
if ($status != 0) then
    echo "Error inserting ranks"
    exit 1
endif

#################################################################
# Assign ids
#################################################################
#
# This step assigns LUI, SUI, and ISUI values to the new atoms
# It also assigns meme_ids
#
echo "    Assign SUI, LUI, ISUI ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.suis.$$.log
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.assign_string_uis ( -
    authority => '$new_source', -
    work_id => $work_id );
EOF
cat /tmp/t.suis.$$.log
if (`grep -c ORA- /tmp/t.suis.$$.log` > 0) then
    echo "Error assigning string ui"
    exit 1
endif


echo "    Assign MEME ids ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.ids.$$.log
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.assign_meme_ids ( -
        table_name => 'ALL', -
        authority => '$new_source', -
        work_id => $work_id );
EOF
cat /tmp/t.ids.$$.log
if (`grep -c ORA- /tmp/t.ids.$$.log` > 0) then
    echo "Error assigning MEME IDs"
    exit 1
endif

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
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.insert_source_ids ( -
    table_name => 'ALL', -
    authority => '$new_source', -
    work_id => $work_id );
EOF
#if (`grep -c ORA- /tmp/t.sids.$$.log` > 0) then
if ($status != 0) then
    echo "Error inserting source ids"
    #set ef = 1
    exit 1
endif

#
# assign auis
#
if (-e classes_atoms.src) then
    echo "    Assign AUIs... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.assign_auis ( -
        table_name => 'SC', -
        authority => '$authority', -
        work_id => $work_id );

EOF
    if ($status != 0) then
        cat /tmp/t.auis.$$.log
        echo "Error assigning AUIs"
        set ef = 1
    endif
endif


#################################################################
# Load Contexts
#################################################################
#
# We do this after insert_source_ids and assignJ_auis
# so that we can map the parent_treenum to AUIs
#
if (-e contexts.src) then
    echo "    Loading contexts ... `/bin/date`"
    # load context rels
    set start_t=`$PATH_TO_PERL -e 'print time'`
    set max_id=`$MEME_HOME/bin/dump_table.pl -u mth -d $db -q "select max_id from max_tab where table_name='RELATIONSHIPS'"`
    set rel_ct=`cat relationships.src | wc -l`
    set max_id=`expr $max_id + $rel_ct`
    $MEME_HOME/bin/load_src.csh $db contexts.src $max_id >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading contexts"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load contexts.src" $work_id 0 $start_t >> /dev/null

echo "    Assign MEME ids (CR)... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.cr.$$.log &
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.assign_meme_ids ( -
        table_name => 'CR', -
        authority => '$new_source', -
        work_id => $work_id );
EOF

endif

#################################################################
# Generate Monster QA Counts
#################################################################
#
# We want to do this before calls to source_replacement
# because then we will get an accurate count of everything
# in the current version and we can track changes from those counts
#
echo "    Generate Monster QA Counts ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.a.$$.log &
    WHENEVER SQLERROR EXIT -2
    set serveroutput on size 100000
    set feedback off
    ALTER session SET sort_area_size=33554432;
    ALTER session SET hash_area_size=33554432;
    exec MEME_INTEGRITY.src_monster_qa;
EOF

#################################################################
# Sampling QA Counts
#################################################################
echo "    Sampling SRC data ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.b.$$.log &
    WHENEVER SQLERROR EXIT -2
    set serveroutput on size 100000
    set feedback off
    ALTER session SET sort_area_size=33554432;
    ALTER session SET hash_area_size=33554432;
    exec MEME_INTEGRITY.src_qa_sampling;
EOF

#
# Wait for parallel QA processes
#
set ef = 0
wait
if ($status != 0) then
    set ef = 1
endif

cat /tmp/t.a.$$.log
if (`grep -c ORA /tmp/t.a.$$.log` > 0) then
    echo "Error generating QA counts"
    set ef = 1
endif

cat /tmp/t.b.$$.log
if (`grep -c ORA /tmp/t.b.$$.log` > 0) then
    echo "Error generating QA samples"
    set ef = 1
endif

if (-e contexts.src) then
    cat /tmp/t.cr.$$.log
    if (`grep -c ORA /tmp/t.cr.$$.log` > 0) then
        echo "Error assigning CR meme ids"
        set ef = 1
    endif
endif

if ($ef == 1) then
    exit 1
endif

#
# compute atom source replacement
#
if (-e classes_atoms.src) then
    echo "    Computing atom source replacement... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.source_replacement ( -
        table_name => 'C', -
        authority => '$authority', -
        work_id => $work_id );
EOF
    if ($status != 0) exit 1
endif

    #
    # Exit if running only load section part 1
    #
    if ($s2 == 0) then
       exit 0
    endif

endif

#################################################################
# Insert Data
#################################################################
#
# This step inserts the data from the source* core tables into
# the actual core tables.
#
# Classes & Concepts
#
if ($s2 == 1) then

if (-e classes_atoms.src) then
    echo "    Inserting concepts ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;

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
# Perform all map_to_meme_ids in parallel
#

if (-e relationships.src) then
    echo "    Map relationship atom/concept ids ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.r.$$.log &
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.map_to_meme_ids ( -
        table_name => 'R', -
        authority => '$authority', -
        work_id => $work_id );

    exec MEME_SOURCE_PROCESSING.assign_ruis ( -
        table_name => 'SR', -
        authority => '$authority', -
        work_id => $work_id );
EOF
endif

#
# Context relationships
#
if (-e contexts.src) then
    echo "    Map cxt relationship atom/concept ids ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.cr.$$.log &
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;

    exec MEME_SOURCE_PROCESSING.map_to_meme_ids ( -
        table_name => 'CR', -
        authority => '$authority', -
        work_id => $work_id );

EOF
endif

#
# Wait for parallel QA processes
#
set ef = 0
wait
if ($status != 0) then
    set ef = 1
endif

cat /tmp/t.r.$$.log
if (`grep -c ORA- /tmp/t.r.$$.log` > 0) then
    echo "Error mapping meme ids or assigning RUIs for R"
    set ef = 1
endif

cat /tmp/t.cr.$$.log
if (`grep -c ORA- /tmp/t.cr.$$.log` > 0) then
    echo "Error mapping meme ids CR"
    set ef = 1
endif

if ($ef == 1) then
    exit 1
endif


#
# Overlap some R and CR operations
#

if (-e relationships.src) then
    echo "    Map relationship atom/concept ids ... `/bin/date`"
    echo "    Insert relationships ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.r.$$.log &
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;

    exec MEME_SOURCE_PROCESSING.source_replacement ( -
        table_name => 'R', -
        authority => '$authority', -
        work_id => $work_id );

    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
        table_name => 'R', -
        authority => '$authority', -
        work_id => $work_id );
EOF
endif

if (-e contexts.src) then
    echo "    Assign CR RUIs ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.cr.$$.log &
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;

    exec MEME_SOURCE_PROCESSING.assign_ruis ( -
        table_name => 'SCR', -
        authority => '$authority', -
        work_id => $work_id );

EOF
endif

#
# Wait for parallel QA processes
#
set ef = 0
wait
if ($status != 0) then
    set ef = 1
endif

cat /tmp/t.r.$$.log
if (`grep -c ORA- /tmp/t.r.$$.log` > 0) then
    echo "Error computing replacement and inserting R"
    set ef = 1
endif

cat /tmp/t.cr.$$.log
if (`grep -c ORA- /tmp/t.cr.$$.log` > 0) then
    echo "Error assigning RUIs for CR"
    set ef = 1
endif

if ($ef == 1) then
    exit 1
endif

#
# Context relationships
#
if (-e contexts.src) then
    echo "    Map cxt relationship atom/concept ids ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;

    exec MEME_SOURCE_PROCESSING.source_replacement ( -
        table_name => 'CR', -
        authority => '$authority', -
        work_id => $work_id );

EOF
    if ($status != 0) exit 1

    echo "    Inserting context relationships ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
        table_name => 'CR', -
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
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;

    exec MEME_SOURCE_PROCESSING.map_to_meme_ids ( -
        table_name => 'A', -
        authority => '$authority', -
        work_id => $work_id );

    exec MEME_SOURCE_PROCESSING.assign_atuis ( -
        table_name => 'SA', -
        authority => '$authority', -
        work_id => $work_id );

    exec MEME_SOURCE_PROCESSING.source_replacement ( -
        table_name => 'A', -
        authority => '$authority', -
        work_id => $work_id );
EOF
    if ($status != 0) exit 1

    echo "    Inserting attributes ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
        table_name => 'A', -
        authority => '$authority', -
        work_id => $work_id );
EOF
    if ($status != 0) exit 1



endif

echo "-------------------------------------------------"
echo "Finished (work_id=$work_id) ... `/bin/date`"
echo "-------------------------------------------------"
