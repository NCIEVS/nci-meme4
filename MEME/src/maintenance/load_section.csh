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
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
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
# 1. Convert the old src files to the new format
# NO LONGER NECESSARY
#################################################################
#
# This involves running the convert_src.csh scripts on the files
# Note, this template tries to convert ALL files. for many insertions
# only some of the five src files described below exist.
#
#if (! -e $MEME_HOME/bin/convert_src.csh) then
#    echo 'The script $MEME_HOME/bin/convert_src.csh must exist!'
#    exit 1
#endif
#$MEME_HOME/bin/convert_src.csh attributes.src
#$MEME_HOME/bin/convert_src.csh termgroups.src
#$MEME_HOME/bin/convert_src.csh classes_atoms.src
#$MEME_HOME/bin/convert_src.csh relationships.src
#$MEME_HOME/bin/convert_src.csh mergefacts.src

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
# A. Load .src files into the source tables
#################################################################
#
# This section checks to see if the requisite files exist before
# attempting to load them.
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

    exec MEME_SOURCE_PROCESSING.create_insertion_indexes;

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error truncating source tables"
    exit 1
endif

#
# i. load classes
#
if (-e classes_atoms.src) then
    echo "    Loading atoms ... `/bin/date`"
    # load classes rows
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db classes_atoms.src >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading atoms"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load classes_atoms.src" $work_id 0 $start_t >> /dev/null

    echo "    Converting atoms to strings ... `/bin/date`"
    # generate strings file, norm strings
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/classes_to_strings.csh . ENG >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error norming atoms"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Generate strings.src" $work_id 0 $start_t >> /dev/null

    echo "    Loading strings ... `/bin/date`"
    # load strings
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db strings.src >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading strings"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load strings.src" $work_id 0 $start_t >> /dev/null
endif

#
# ii. load attributes
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
    $MEME_HOME/bin/load_src.csh $db stringtab.src >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading stringtab! "
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load stringtab.src" $work_id 0 $start_t >> /dev/null

    echo "    Loading attributes ... `/bin/date`"
    # load attributes
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db attributes.src >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading attributes"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load attributes.src" $work_id 0 $start_t >> /dev/null

endif


# iii. load contexts

if (-e contexts.src) then

#set ct=`ls | fgrep -c '.raw'`
#if ($ct > 0) then
#    echo "    Converting contexts ... `/bin/date`"
#    # generate context relationships to load
#    set start_t=`$PATH_TO_PERL -e 'print time'`
#    $MEME_HOME/bin/raw_to_contexts.csh . >&! /tmp/t.$$.log
#    if ($status != 0) then
#	cat /tmp/t.$$.log
#	echo "Error converting .raw files"
#	exit 1
#    endif
#    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Generate contexts.src" $work_id 0 $start_t >> /dev/null

    echo "    Loading contexts ... `/bin/date`"
    # load context rels
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db contexts.src >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading contexts"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load contexts.src" $work_id 0 $start_t >> /dev/null

endif

#
# iv. load rels
#
if (-e relationships.src) then
    echo "    Loading relationships ... `/bin/date`"
    # load rels
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db relationships.src >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading relationships"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load relationships.src" $work_id 0 $start_t >> /dev/null
endif

#
# v. load termgroups/sources
#
if (-e sources.src && -e termgroups.src) then
    echo "    Loading termgroup metadata ... `/bin/date`"
    # load termgroups
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db termgroups.src >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading termgroups"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load termgroups.src" $work_id 0 $start_t >> /dev/null

    echo "    Loading source metadata ... `/bin/date`"
    # load sources
    set start_t=`$PATH_TO_PERL -e 'print time'`
    $MEME_HOME/bin/load_src.csh $db sources.src >&! /tmp/t.$$.log
    if ($status != 0) then
	cat /tmp/t.$$.log
	echo "Error loading sources"
	exit 1
    endif
    $MEME_HOME/bin/log_operation.pl $db $authority "Load Section" "Load sources.src" $work_id 0 $start_t >> /dev/null

endif

# vi. load MRDOC.RRF data

if (-e MRDOC.RRF) then

    echo "    Load MRDOC.RRF ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -1;
        set serveroutput on size 100000
        alter session set sort_area_size=200000000;
        alter session set hash_area_size=200000000;
    
        exec MEME_UTILITY.drop_it('table','t1');
        
        CREATE TABLE t1 (
            dockey    VARCHAR2(100),
            value     VARCHAR2(100),
            type      VARCHAR2(100),
            expl      VARCHAR2(4000)
        );
EOF
    if ($status != 0) then
      echo "Error creating staging table for MRDOC.RRF"
      cat /tmp/t.$$.log
      exit 1
    endif

    $MEME_HOME/bin/dump_mid.pl -t t1 $db .
    if ($status != 0) then
      echo "Error dumping staging table for MRDOC.RRF"
      exit 1
    endif
    
    /bin/cp -f MRDOC.RRF t1.dat
    $ORACLE_HOME/bin/sqlldr $user@$db control="t1.ctl"
    if ($status != 0) then
      echo "Error loading staging table for MRDOC.RRF"
      exit 1
    endif

    echo "    Load MRDOC.RRF data into inverse_rel_attributes ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -1;
        set serveroutput on size 100000
        alter session set sort_area_size=200000000;
        alter session set hash_area_size=200000000;
        
        exec MEME_UTILITY.drop_it('table','t2');    
        CREATE TABLE t2 AS
        SELECT value AS relationship_attribute,
           expl AS inverse_rel_attribute_1,
           inverse_rel_attribute AS inverse_rel_attribute_2
        FROM t1 a, inverse_rel_attributes b
        WHERE a.value=b.relationship_attribute AND type='rela_inverse'
        AND expl != inverse_rel_attribute;
        
        BEGIN
            IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
                MEME_UTILITY.put_message('Error: existing inverse_rel_attributes');
                RAISE_APPLICATION_ERROR(-20000,'ERROR: duplicate relationship_attribute in t1');
            END IF;
        END;
/

        exec MEME_UTILITY.drop_it('table','t2');    
        CREATE TABLE t2 AS
        SELECT a.value as value_1, b.value as value_2, 
		   a.expl as expl_1, b.expl as expl_2
        FROM t1 a, t1 b
        WHERE a.type='rela_inverse' and b.type='rela_inverse'
          AND ((a.value = b.value AND a.expl != b.expl) OR
               (a.expl = b.expl AND a.value != b.value) OR
               (a.expl = b.value AND a.value != b.expl));

        BEGIN
            IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
                MEME_UTILITY.put_message('Error: duplicate relationship_attribute within t1');
                RAISE_APPLICATION_ERROR(-20000,'ERROR: duplicate relationship_attribute within t1');
            END IF;
        END;
/
            
        INSERT INTO inverse_rel_attributes
            (relationship_attribute,inverse_rel_attribute,rank)
        (SELECT value, expl, 1 FROM t1 
         WHERE type='rela_inverse'
         UNION
         SELECT expl, value, 1 FROM t1 
         WHERE type='rela_inverse')
        MINUS
        SELECT relationship_attribute,inverse_rel_attribute,rank
        FROM inverse_rel_attributes;
EOF
    if ($status != 0) then
      echo "Error loading MRDOC.RRF data into inverse_rel_attributes"
      cat /tmp/t.$$.log
      exit 1
    endif


    echo "    Load MRDOC.RRF data into meme_properties ... `/bin/date`"
    
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -1;
        set serveroutput on size 100000
        alter session set sort_area_size=200000000;
        alter session set hash_area_size=200000000;
        
        exec MEME_UTILITY.drop_it('table','t2');    
        CREATE TABLE t2 AS
        SELECT a.value AS value,
           expl AS description_1,
           description AS description_2
        FROM t1 a, meme_properties b
        WHERE nvl(a.value,'null') = nvl(b.value,'null')
          AND a.type = b.key
          AND a.dockey = b.key_qualifier
          AND a.type = 'expanded_form'
          AND nvl(a.expl,'null') != nvl(b.description,'null');
        
        BEGIN
            IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
                MEME_UTILITY.put_message('Error: existing metadata');
                RAISE_APPLICATION_ERROR(-20001,'ERROR: existing metadata');
            END IF;
        END;
/
            
        exec MEME_UTILITY.drop_it('table','t2');    
        CREATE TABLE t2 AS
        SELECT dockey, nvl(value,'null') value, expl
        FROM t1
        GROUP BY dockey, nvl(value,'null'), expl HAVING count(distinct expl)>1;
        BEGIN
            IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
                MEME_UTILITY.put_message('Error: duplicate metadata within t1');
                RAISE_APPLICATION_ERROR(-20001,'ERROR: duplicate metadata');
            END IF;
        END;
/

    exec MEME_UTILITY.drop_it('table','t2');    
    CREATE TABLE t2 AS
    SELECT dockey,value,type,expl FROM t1 WHERE type='tty_class'
    MINUS
    SELECT key_qualifier,value,key,description FROM meme_properties WHERE key='tty_class';
    BEGIN
        IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
           MEME_UTILITY.put_message('Error: tty_class being added');
            RAISE_APPLICATION_ERROR(-20001,'ERROR: tty_class being added');
        END IF;
    END;
/

    exec MEME_UTILITY.drop_it('table','t2');    
    CREATE TABLE t2 AS
    SELECT key_qualifier,value,key,description FROM meme_properties WHERE key='tty_class'
    AND value in (select value from t1 where type='tty_class')
    MINUS
    SELECT dockey,value,type,expl FROM t1 WHERE type='tty_class';
    BEGIN
        IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
           MEME_UTILITY.put_message('Error: tty_class being removed');
            RAISE_APPLICATION_ERROR(-20001,'ERROR: tty_class being removed');
        END IF;
    END;
/

        INSERT INTO meme_properties
            (key_qualifier,value,key,description)
        SELECT DISTINCT dockey, value, type, expl FROM t1 
        WHERE type in ('expanded_form','tty_class')
        MINUS
        SELECT key_qualifier,value,key,description
        FROM meme_properties;

EOF
    if ($status != 0) then
      echo "Error loading MRDOC.RRF data into meme_properties"
      cat /tmp/t.$$.log
      exit 1
    endif

    
endif

#################################################################
# B. Insert ranks
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

if ($status != 0) exit 1

#################################################################
# C. Assign ids
#################################################################
#
# This step assigns LUI, SUI, and ISUI values to the new atoms
# It also assigns meme_ids 
#
echo "    Assign SUI, LUI, ISUI ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF 
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    BEGIN
      IF MEME_UTILITY.object_exists('index','x_ssui_string') = 0 THEN
        EXECUTE IMMEDIATE
	  'CREATE INDEX x_ssui_string on source_string_ui(string) COMPUTE STATISTICS PARALLEL';
      END IF;
    END;
/
    exec MEME_SOURCE_PROCESSING.assign_string_uis ( -
	authority => '$new_source', -
	work_id => $work_id );
EOF
if ($status != 0) exit 1

echo "    Assign MEME ids ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF 
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

if ($status != 0) exit 1

#################################################################
# D. Insert source_ids
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
    if ($status != 0) then
	print "Error inserting source ids"
	exit 1
    endif


#
# Generate Monster QA Counts
#
# We want to do this before calls to source_replacement
# because then we will get an accurate count of everything
# in the current version and we can track changes from those counts
#
echo "    Generate Monster QA Counts ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    set serveroutput on size 100000
    set feedback off
    ALTER session SET sort_area_size=33554432;
    ALTER session SET hash_area_size=33554432;
    exec MEME_INTEGRITY.src_monster_qa;
EOF
if ($status != 0) then
    echo "Error generating QA counts"
    cat /tmp/t.$$.log
    exit 1
endif

    if ($s2 == 0) then
       exit 0
    endif

endif

#################################################################
# E. Insert Data
#################################################################
#
# This step inserts the data from the source* core tables into
# the actual core tables.
#
# i. Classes & Concepts
#
if ($s2 == 1) then

if (-e classes_atoms.src) then
    echo "    Computing atom source replacement and inserting concepts ... `/bin/date`"
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

    exec MEME_SOURCE_PROCESSING.source_replacement ( -
	table_name => 'C', -
	authority => '$authority', -
	work_id => $work_id );

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
# ii. relationships
#
if (-e relationships.src) then
    echo "    Map relationship atom/concept ids ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
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

    exec MEME_SOURCE_PROCESSING.source_replacement ( -
	table_name => 'R', -
	authority => '$authority', -
	work_id => $work_id );

EOF
    if ($status != 0) exit 1

    echo "    Insert relationships ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;
    exec MEME_SOURCE_PROCESSING.core_table_insert ( -
	table_name => 'R', -
	authority => '$authority', -
	work_id => $work_id );
EOF
    if ($status != 0) exit 1

endif

#
# iii. Context relationships
#
if (-e contexts.src) then
    echo "    Map cxt relationship atom/concept ids ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    whenever sqlerror exit -1
    set serveroutput on size 100000
    set feedback off
    alter session set sort_area_size=67108864;
    alter session set hash_area_size=67108864;

    exec MEME_SOURCE_PROCESSING.map_to_meme_ids ( -
	table_name => 'CR', -
	authority => '$authority', -
	work_id => $work_id );

    exec MEME_SOURCE_PROCESSING.assign_ruis ( -
	table_name => 'SCR', -
	authority => '$authority', -
	work_id => $work_id );

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
# iv. Attributes
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
