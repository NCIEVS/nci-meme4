#!/bin/csh -f
#
# File:    initialize_mrd.csh
# Author:  Stephanie Halbeisen, Tun Tun Naing, Brian Carlsen
#
# REMARKS: 
#   This script is used to prepare the data in an MRD for release
#   It also serves to initialze a "new" MRD by populating tables
#   like "registered_handlers"
#
#
# This program expect that the following steps are already done:
# 1. All MRD tables exist (empty or not).
#   This means the following were run at some point
#    @$MRD_HOME/etc/sql/mrd_tables
#    @$MRD_HOME/etc/sql/mrd_indexes
#    @$MRD_HOME/etc/sql/mrd_views
#    @$MRD_HOME/etc/sql/mrd_packages
#
# Make sure that when you run them, you do not DROP any
# tables that contain data to be kept (e.g. coc tables, release_history, etc)
#
# Changes
# 08/31/2010 BAC (1-RDJQ1): added "Optimization" release handler
# 05/25/2006 TTN (1-BACBH): remove the restrictions for coc_headings and coc_subheadings 
#					 		to generate auxiliary data states
# 05/03/2006 BAC (1-B2U36): Changed order of MRMAP in "registered_handlers" table.
#
set release="2"
set version="0.1"
set version_authority="BBAC"
set version_date="04/22/2005"

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
    echo '$'"ORACLE_HOME must be set in $ENV_FILE"
    exit 1
endif
if ($?MRD_HOME == 0) then
    echo '$'"MRD_HOME must be set in $ENV_FILE"
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
   Usage: initialize_mrd.csh <user@mrd_database>                              
    This script requires that the tables be created already.

EOF
    exit 0
    endif
endif

#
# get arguments
#
if ($#argv == 1) then
    set mrd_db = $1;
    set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $mrd_db`
else
   echo "Usage: initialize_mrd.csh <mrd_database>"
    exit 1
endif

echo "------------------------------------------------------------------------"
echo "Starting...`/bin/date`"
echo "------------------------------------------------------------------------"
echo "  Release $release, version $version, $version_date ($version_authority)"
echo "  ORACLE_HOME:      $ORACLE_HOME"
echo "  MRD_HOME:          $MRD_HOME"
echo "  MRD database:      $user@$mrd_db"
echo ""


#
#    Get the names of all MRD tables from the MRD tables.sql script 
#    and writes these names into a file called "meme_tables.dat".
#
echo "Load meme_tables.dat ... `/bin/date`"
grep -i 'create *table' $MRD_HOME/etc/sql/mrd_tables.sql |\
$PATH_TO_PERL -ne 's/create\s*table//i; s/\(//; s/\s{1,}as//i; s/\s//g; print uc ($_),"\n";' \
  >! meme_tables.dat

# 
#    Load these names, that is meme_tables.dat into the MRD table meme_tables. # 
cat >! meme_tables.ctl <<EOF
    load data
    infile 'meme_tables'
    badfile 'meme_tables.bad'
    discardfile 'meme_tables.dsc'
    truncate
    into table meme_tables
    fields terminated by '|'
    trailing nullcols
    (table_name		  char
    )
EOF
$ORACLE_HOME/bin/sqlldr $user@$mrd_db control = "meme_tables.ctl" >! /tmp/t.$$.log
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error loading table meme_tables."
    exit 1
endif
/bin/rm -f meme_tables.{dat,log,ctl}


#
# truncate tables which should be empty for a new MRD but get used later. 
# initialize MRD-only tables
#
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE unlimited

    -- The following tables should always be empty in the MRD.
    TRUNCATE TABLE extraction_history;
    exec MEME_UTILITY.put_message('initialize extraction_history');
    INSERT INTO extraction_history ( 
        work_id, authority, timestamp,first_mid_event_id,
	last_mid_event_id, row_count, valid_extraction )
    SELECT 0, 'MRD_INIT', sysdate, 0, NVL(max(action_id),0), 0, 'Y'
    FROM (SELECT action_id FROM action_log);

    -- The following tables should be empty at the time 
    -- the MRD gets initialized. But they will not stay empty.
    TRUNCATE TABLE events_processed;
    TRUNCATE TABLE feedback_queue;
    --TRUNCATE TABLE release_history;
    TRUNCATE TABLE connected_set;
    TRUNCATE TABLE connected_sets;
    TRUNCATE TABLE tmp_classes;
    TRUNCATE TABLE tmp_relationships;
    TRUNCATE TABLE tmp_attributes;
    TRUNCATE TABLE tmp_concepts;
		
    --
    -- initialize registered_handlers table
    --
    exec MEME_UTILITY.put_message('initialize registered_handlers');
    TRUNCATE TABLE registered_handlers;

    --
    -- Prepare release handlers
    --
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRDOCReleaseHandler', 'RELEASE', 'Full', 1, 'Y', 
     '', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRSABReleaseHandler', 'RELEASE', 'Full', 2, 
     'Y', '', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRRANKReleaseHandler', 'RELEASE', 'Full', 3, 
     'Y', 'MRSAB,MRDOC', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRCONSOReleaseHandler', 'RELEASE', 'Full', 4, 
     'Y', 'MRDOC,MRSAB,MRRANK', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRSTYReleaseHandler', 'RELEASE', 'Full', 5, 'Y', 
     'MRCONSO', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRDEFReleaseHandler', 'RELEASE', 'Full', 6, 
     'Y', 'MRCONSO,MRSAB', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullAMBIGReleaseHandler', 'RELEASE', 'Full', 7, 
     'Y', 'MRCONSO', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRXReleaseHandler', 'RELEASE', 'Full', 8, 
     'Y', 'MRCONSO,MRDOC', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRHISTReleaseHandler', 'RELEASE', 'Full', 9,
     'Y', 'MRCONSO,MRSAB', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRCUIReleaseHandler', 'RELEASE', 'Full', 10,
     'Y', 'MRCONSO,MRDOC', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRAUIReleaseHandler', 'RELEASE', 'Full', 11,
     'Y', 'MRCONSO,MRDOC', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRCOCReleaseHandler', 'RELEASE', 'Full', 12, 
     'Y', 'MRCONSO,MRSAB,MRDOC,MRSAT', 'L-BAC', sysdate);  
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRHIERReleaseHandler', 'RELEASE', 'Full', 14, 
     'Y', 'MRCONSO,MRSAB,MRDOC', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRRELReleaseHandler', 'RELEASE', 'Full', 15, 
     'Y', 'MRCONSO,MRSAB,MRDOC', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRSATReleaseHandler', 'RELEASE', 'Full', 16, 
     'Y', 'MRCONSO,MRSAB,MRDOC,MRREL', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRMAPReleaseHandler', 'RELEASE', 'Full', 17, 
     'Y', 'MRCONSO,MRSAB,MRDOC,MRSTY,MRSAT,MRREL', 'L-BAC', sysdate);

    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMetaMorphoSysReleaseHandler', 'RELEASE', 'Full', 18, 
     'Y', 'MRMAP,MRCUI,MRX,MRHIER', 'L-BAC', sysdate);
     
         INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullMRFILESCOLSReleaseHandler', 'RELEASE', 'Full', 19, 
     'Y', 'MRMAP', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullORFReleaseHandler', 'RELEASE', 'Full', 20, 
     'Y', 'MetaMorphoSys', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullDOCReleaseHandler', 'RELEASE', 'Full', 21, 
     'Y', 'ORF', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullActiveSubsetReleaseHandler', 'RELEASE', 'Full', 22, 
     'N', 'ORF', 'L-BAC', sysdate);
    INSERT INTO registered_handlers 
      (handler_name, process, type, row_sequence, 
       activated, dependencies, authority, timestamp) VALUES
    ('FullOptimizationReleaseHandler', 'RELEASE', 'Full', 23, 
     'N', 'ORF', 'L-BAC', sysdate);

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error initializing MRD secondary tables ...`/bin/date`"
    exit 1
endif

#
# Fix meme_indexes/meme_ind_columns
#
echo "Reindex MRD ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE unlimited
    ALTER SESSION SET sort_area_size=400000000;
    ALTER SESSION SET hash_area_size=400000000;

    exec MEME_UTILITY.drop_it('index','x_classes_lrc');
    CREATE INDEX x_classes_lrc ON classes(last_release_cui)
    PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MRDI 
    COMPUTE STATISTICS PARALLEL;

    exec MEME_UTILITY.drop_it('index','x_classes_aui');
    CREATE INDEX x_classes_aui ON classes(aui)
    PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MRDI 
    COMPUTE STATISTICS PARALLEL;
    
    exec MEME_UTILITY.drop_it('index','x_classes_lac');
    CREATE INDEX x_classes_lac ON classes(last_assigned_cui)
    PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MRDI 
    COMPUTE STATISTICS PARALLEL;

    -- Build meme_ind* tables from user_ind* tables
    exec MEME_SYSTEM.refresh_meme_indexes;
EOF
if ($status != 0) then
    echo "Error refreshing index metadata MRD"
    exit 1
endif

#
# Initialize clean_concepts table, load core and auxiliary MRD tables.
# At the moment, it assumes that the MID is clean. Later there should be 
# an option wether the database is clean or not. 
#
# Not yet resolved: How to initialize the connected_concepts table if
# the MID data are not clean.
#
echo "Initialize mrd states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE unlimited
    SET AUTOCOMMIT ON
    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;

    truncate table mrd_source_rank;
    truncate table mrd_termgroup_rank;
    truncate table mrd_contexts;
    truncate table mrd_properties;
    truncate table mrd_stringtab;
    truncate table mrd_coc_headings;
    truncate table mrd_coc_subheadings;
    truncate table mrd_column_statistics;
    truncate table mrd_file_statistics;
    truncate table mrd_cui_history;

    --
    -- Prepare MRD states
    --
    exec mrd_operations.generate_auxiliary_data_states('source_rank');
    exec mrd_operations.generate_auxiliary_data_states('termgroup_rank');
    exec meme_system.reindex('mrd_source_rank');
    exec meme_system.analyze('mrd_source_rank');
    exec meme_system.reindex('mrd_termgroup_rank');
    exec meme_system.analyze('mrd_termgroup_rank');

    exec MEME_SYSTEM.truncate('connected_set');
    INSERT INTO connected_set (concept_id,cui)
      	SELECT DISTINCT a.concept_id, a.cui
	FROM concept_status a, classes b
	WHERE a.concept_id = b.concept_id
	  AND b.tobereleased NOT IN ('n', 'N');    
    exec MEME_SYSTEM.analyze('connected_set');

    exec MEME_SYSTEM.truncate('tmp_concepts');
    exec MEME_SYSTEM.truncate('tmp_classes');
    exec MEME_SYSTEM.truncate('tmp_relationships');
    exec MEME_SYSTEM.truncate('tmp_attributes');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error initializing MRD states."
    exit 1
endif

echo "     Generate MRD_CONCEPTS states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE unlimited
    SET AUTOCOMMIT ON
    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;
    --
    -- Handle MRD Concepts
    -- 
    exec MEME_SYSTEM.truncate('mrd_concepts');
    exec MEME_SYSTEM.drop_indexes('mrd_concepts');
    exec MEME_SYSTEM.analyze('mrd_concepts');
    exec MRD_OPERATIONS.generate_core_data_states( table_name => 'CS');
    exec MEME_SYSTEM.rebuild_table('mrd_concepts','N',' ');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error generating concept MRD states."
    exit 1
endif

echo "     Generate MRD_classes states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE unlimited
    SET AUTOCOMMIT ON
    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;
    --
    -- Handle MRD Classes
    -- 
    exec MEME_SYSTEM.truncate('mrd_classes');
    exec MEME_SYSTEM.drop_indexes('mrd_classes');
    exec MEME_SYSTEM.analyze('mrd_classes');
    exec MRD_OPERATIONS.generate_core_data_states( table_name => 'C');
    exec MEME_SYSTEM.rebuild_table('mrd_classes','N',' ');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error generating atom MRD states."
    exit 1
endif

echo "     Generate MRD_relationships states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE unlimited
    SET AUTOCOMMIT ON
    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;
    --
    -- Handle MRD Relationships
    -- 
    exec MEME_SYSTEM.truncate('mrd_relationships');
    exec MEME_SYSTEM.drop_indexes('mrd_relationships');
    exec MEME_SYSTEM.analyze('mrd_relationships');
    exec MRD_OPERATIONS.generate_core_data_states( table_name => 'R');
    exec MEME_SYSTEM.rebuild_table('mrd_relationships','N',' ');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error generating relationship MRD states."
    exit 1
endif

echo "     Generate MRD_attributes states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE unlimited
    SET AUTOCOMMIT ON
    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;
    --
    -- Handle MRD Attributes
    -- 
    exec MEME_SYSTEM.truncate('mrd_attributes');
    exec MEME_SYSTEM.drop_indexes('mrd_attributes');
    exec MEME_SYSTEM.analyze('mrd_attributes');
    exec MRD_OPERATIONS.generate_core_data_states( table_name => 'A');
    exec MEME_SYSTEM.rebuild_table('mrd_attributes','N',' ');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error generating attribute MRD states."
    exit 1
endif

echo "     Generate auxiliary MRD states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE unlimited
    SET AUTOCOMMIT ON
    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;
 
    -- gen aux data states
    DECLARE
        CURSOR cur IS SELECT table_name FROM meme_tables;
        cvar        cur%rowtype;
    BEGIN
        OPEN cur;
        LOOP
            FETCH cur INTO cvar;
            EXIT WHEN cur%NOTFOUND;
               IF upper(cvar.table_name) != 'FOREIGN_CLASSES' AND
                upper(cvar.table_name) not like '%COC%' THEN
                DBMS_OUTPUT.PUT_LINE('Processing Table =' || cvar.table_name); 
                MRD_OPERATIONS.generate_auxiliary_data_states(
                    cvar.table_name);
            END IF;
            COMMIT;
        END LOOP;
    END;
/
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error generating auxiliary MRD states."
    exit 1
endif

set db=`echo $user@$mrd_db | $PATH_TO_PERL -ne 'split /\@/; print $_[1];'`
$MEME_HOME/bin/reindex_mid.pl -p 5 $db
if ($status != 0) then
    echo "Error reindexing $db"
    exit 1
endif

$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
	WHENEVER SQLERROR EXIT -2
	PURGE RECYCLEBIN
	exec umlsdbstats.enable
	COMMIT;

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error initializing MRD Purging the recylebin ...`/bin/date`"
    exit 1
endif
/bin/rm -f /tmp/t.$$.log

echo "------------------------------------------------------------------------"
echo "Finished $0 ...`/bin/date`"
echo "------------------------------------------------------------------------"
