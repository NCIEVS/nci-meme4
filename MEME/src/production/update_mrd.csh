#!/bin/csh -f
#
# File:    update_mrd.csh
# Author:  Brian Carlsen
#
# REMARKS: 
#
#  This script is used to prep the MRD for a release.
#
# This program expects that the following steps are already done:
# 1. MID and MIDI tablespaces have been transported from clean production-db
# 2. All MRD tables exist, are loaded, and indexed
# 3. All MRD packages are loaded
#
#
# Version Info
#
# 04/22/2005 (2.0.1): Prepared for transition
#
set release="2"
set version="0.1"
set version_authority="BAC"
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
    echo '$'"ORACLE_HOME must be set"
    exit 1
endif

if ($?MRD_HOME == 0) then
    echo '$MRD_HOME must be set'
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
   Usage: update_mrd.csh <db>

    This script prepares the MRD for a release by synchronizing all
    of the MID tables.

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
   echo "Usage: update_mrd.csh <mrd_database>"
    exit 1
endif

echo "------------------------------------------------------------------------"
echo "Starting...`/bin/date`"
echo "------------------------------------------------------------------------"
echo "  Release $release, version $version, $version_date ($version_authority)"
echo "  ORACLE_HOME:      $ORACLE_HOME"
echo "  MRD_HOME:          $MRD_HOME"
echo "  MRD database:      $mrd_db"
echo ""

########################################################################
#
# Load MEME Tables
#
########################################################################

#
#    Get the names of all MRD tables from the MRD tables.sql script 
#    and writes these names into a file called "meme_tables.dat".
#
echo "    Load meme_tables.dat ... `/bin/date`"
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

########################################################################
#
# Prepare indexes 
#
########################################################################

#
# Fix meme_indexes/meme_ind_columns
# Reindex & Analyze the database
#
echo "Reindex MRD ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF

    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE 100000

    ALTER SESSION SET sort_area_size=400000000;
    ALTER SESSION SET hash_area_size=400000000;

    exec MEME_UTILITY.drop_it('index','x_classes_lrc_cui');
    CREATE INDEX x_classes_lrc_cui ON classes(last_release_cui)
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
    echo "Error reindexing/analyzing MRD"
    exit 1
endif

########################################################################
#
# Configure synchronization tracking tables 
#
########################################################################

echo "    Synchronize tracking tables ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE 100000

    --
    -- Set extraction history to show synchronization up to current state.
    --
    TRUNCATE TABLE extraction_history;
    exec MEME_UTILITY.put_message('initialize extraction_history');
    INSERT INTO extraction_history ( 
        work_id, authority, timestamp,first_mid_event_id,
	last_mid_event_id, row_count, valid_extraction )
    SELECT 0, 'MRD_INIT', sysdate, 0, NVL(max(molecule_id),0), 0, 'Y'
    FROM (SELECT molecule_id FROM molecular_actions);

    --
    -- The following tables should be empty at the time 
    -- the MRD gets initialized. But they will not stay empty.
    --
    -- TRUNCATE TABLE events_processed;
    TRUNCATE TABLE feedback_queue;
    TRUNCATE TABLE connected_set;
    TRUNCATE TABLE connected_sets;
    TRUNCATE TABLE tmp_classes;
    TRUNCATE TABLE tmp_relationships;
    TRUNCATE TABLE tmp_attributes;
    TRUNCATE TABLE tmp_concepts;

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error preparing MRD secondary tables ...`/bin/date`"
    exit 1
endif

########################################################################
#
# Generate MRD states for current MID data
#
########################################################################
echo "   Generate MRD states ... `/bin/date`"
echo "     Prepare SR, TR, connected_set ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE 100000
    SET AUTOCOMMIT ON

    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;

    --
    -- Handle sources and termgroups first
    --
    exec mrd_operations.generate_auxiliary_data_states('source_rank');
    exec mrd_operations.generate_auxiliary_data_states('termgroup_rank');

    --
    -- Connected set is considered to be everything releasable
    --
    exec MEME_SYSTEM.truncate('connected_set');
    INSERT INTO connected_set (concept_id,cui)
      	SELECT DISTINCT a.concept_id, a.cui
	FROM concept_status a, classes b
	WHERE a.concept_id = b.concept_id
	  AND b.tobereleased NOT IN ('n', 'N');    
    exec MEME_SYSTEM.analyze('connected_set');

    --
    --  Truncate tmp tables
    --
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
    SET SERVEROUTPUT ON SIZE 100000
    SET AUTOCOMMIT ON

    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;

    --
    -- Handle MRD Concepts
    -- 
    exec MRD_OPERATIONS.generate_core_data_states( table_name => 'CS');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error initializing MRD states."
    exit 1
endif

echo "     Generate MRD_CLASSES states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE 100000
    SET AUTOCOMMIT ON

    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;


    --
    -- Handle MRD Classes
    -- 
    exec MRD_OPERATIONS.generate_core_data_states( table_name => 'C');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error initializing MRD states."
    exit 1
endif

echo "     Generate MRD_RELATIONSHIPS states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE 100000
    SET AUTOCOMMIT ON

    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;


    --
    -- Handle MRD Relationships
    -- 
    exec MRD_OPERATIONS.generate_core_data_states( table_name => 'R');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error initializing MRD states."
    exit 1
endif

echo "     Generate MRD_ATTRIBUTES states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE 100000
    SET AUTOCOMMIT ON

    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;


    --
    -- Handle MRD Attributes
    -- 
    exec MRD_OPERATIONS.generate_core_data_states( table_name => 'A');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error initializing MRD states."
    exit 1
endif

echo "   Generate auxiliary MRD states ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $user@$mrd_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE 100000
    SET AUTOCOMMIT ON

    ALTER session SET sort_area_size=400000000;
    ALTER session SET hash_area_size=400000000;

    --
    -- Handle Auxiliary MRD states
    --
    DECLARE
        CURSOR cur IS SELECT table_name FROM meme_tables;
        cvar        cur%rowtype;
    BEGIN
        OPEN cur;
        LOOP
            FETCH cur INTO cvar;
            EXIT WHEN cur%NOTFOUND;
               IF upper(cvar.table_name) != 'FOREIGN_CLASSES' AND
               upper(cvar.table_name) != 'COC_SUBHEADINGS' AND
               upper(cvar.table_name) != 'SOURCE_COC_HEADINGS' AND
               upper(cvar.table_name) != 'SOURCE_COC_SUBHEADINGS' AND
               upper(cvar.table_name) != 'COC_HEADINGS' THEN
                MRD_OPERATIONS.generate_auxiliary_data_states(
                    cvar.table_name
	        );
            END IF;
            COMMIT;
        END LOOP;
    END;
/
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error initializing MRD states."
    exit 1
endif

#
# Reindex all tables
#
set db=`echo $mrd_db | $PATH_TO_PERL -ne 'split /\@/; print $_[-1];'`
$MEME_HOME/bin/reindex_mid.pl -p 5 $db
if ($status != 0) then
    echo "Error reindexing $db"
    exit 1
endif

/bin/rm -f /tmp/t.$$.log

echo "------------------------------------------------------------------------"
echo "Finished $0 ...`/bin/date`"
echo "------------------------------------------------------------------------"
