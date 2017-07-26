#!/bin/csh -f
#
# This script computes the gold script counts
# and creates the various qa_* tables in the MRD.
#
# Changes
#   04/06/2012 TPM : Split the query of Q# MED attributes into smaller tables.
#   08/05/2010 BAC (1-RDJQ1): add code for Optimization target
#   09/27/2007 BAC (1-DBSLY): Remove hard-coded references to L0028429 (null LUI)
#   07/17/2007  SL: 1-EG2YN  -- Modified the select statemnt to include the cui1 (ver_rel_rela_tally)
#   07/17/2007  SL: 1-EIJRP  -- MRCUI gold build difference
#   07/17/2007  SL: 1-EIJT5  -- MRHIER sab_rela_tally select statement is modified   
#   07/17/2007  SL: 1-EIJQJ  -- MRMAP  sab_toexpr_tally select statement is modified                
#   07/12/2006: SL: 1-BNL00: Modifying the Merged cui logic to caluclate the delete cui for MRCUI gold count.
#   06/09/2006 TTN (1-BFPCX): remove CVF entries from mrdoc
#   04/27/2006 TK (1-B02GR): corrected ts,stt,ispref algorithm for MetaMorphoSys
#   03/09/2006 TTN (1-AM5U9): add ts,stt,ispref counts in MetaMorphoSys
#   01/24/2006 BAC (1-7558C): remove classes_feedback entries
#   09/27/2012 MAJ : Added section for active subsets
#
# Version Information
# 2.2.0 03/10/2005: MRAUI counts added
# 2.1.0 10/17/2003: Adapted for MR+ data, 2003AC
# 1.0.5 09/17/2002: Re-organized to work with MRD App server
#
set release=2
set version=2.0
set authority=BAC
set version_date="03/10/2005"

source $ENV_HOME/bin/env.csh

set usage="Usage: $0 [-(mrconso|mrdef|mrrank|...)] <db> <current_release> <previous_release>"
set db=""
set mode="all"


if ($#argv == 1) then
    if ($argv[1] == "-v") then
        echo "$version"
        exit 0
    else if ($argv[1] == "-version") then
        echo "Version $version, $version_date ($version_auth)"
        exit 0
    else if ($argv[1] == "-help" || $argv[1] == "--help" || $argv[1] == "-h") then
        cat <<EOF
 $usage

 This script generates gold counts for a release and loads the
 various qa_*gold tables (e.g. qa_mrconso_2003ac_gold).  The gold script should
 be run before the QA script.

EOF
        exit 0
endif
else if ($#argv == 3) then
    set db=$1
    set release=$2
    set previous_release=$3
else if ($#argv == 4) then
    set mode=`echo $1 | sed 's/^-//'`
    set db=$2
    set release=$3
    set previous_release=$4
else
    echo "ERROR: Wrong number of arguments"
    echo "$usage"
    exit 1
endif


if ($#ORACLE_HOME == 0) then
    echo 'ERROR: $ORACLE_HOME must be set'
    exit 1
endif

echo "-----------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "-----------------------------------------------------"
echo "database:                 $db"
echo "mode:                     $mode"
echo "release:                  $release"
echo "previous_release:         $previous_release"
echo ""
set mu=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
#
# MRCONSO counts
# Count number of rows (row_cnt)
# Count distinct SUIs (sui_cnt)
# Count distinct LUIs (lui_cnt)
# Count distinct CUIs (cui_cnt)
# Count distinct CUI|LUI|SUI (cls_cnt)
# Count distinct CUI|LUI (cl_cnt)
# Count distinct CUI|SUI (cs_cnt)
# Count distinct LUI|SUI (ls_cnt)
# Count distinct NOCODE rows (nocode_cnt)
# Count by TS  (ts_tally)
# Count by LAT (lang_tally)
# Count by STT (stt_tally)
# Count by ISPREF (ispref_tally)
# Count by TS|STT (tsstt_tally)
# Count by LAT|TS|STT|ISPREF (lat_ts_stt_ispref_tally)
# Count by SUPPRESS (suppress_tally)
# Count by SAB (sab_tally)
# Count by TTY (tty_tally)
# Count by SAB/TTY (termgrp_tally)
# Count by SRL (srl_tally)
# Count distinct SCD by SAB (sab_scd_tally)
# Count by SAB/TTY with SUPPRESS=Y,E (suppr_termgrp_tally)
# Count ambiguous suis (CUI|SUI) (ambig_suis_cnt)
#    This should equal AMBIG.SUI.cs_cnt
# Count ambiguous luis (CUI|LUI) (ambig_luis_cnt)
#    This should equal AMBIG.LUI.cl_cnt
# Find min/max length of strings
#     (min_length, max_length)
# Count by ambiguous string (ambig_str_tally)
#
if ($mode == "all" || $mode == "mrconso") then
    echo "Generating MRCONSO QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrconso_${release}_gold');
    CREATE TABLE qa_mrconso_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- Row, cui, lui, sui & combination counts
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'row_cnt','',
        count(distinct cui||aui)
    FROM mrd_classes WHERE expiration_date IS NULL;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'sui_cnt','',count(distinct sui)
    FROM mrd_classes WHERE expiration_date IS NULL;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'lui_cnt','',count(distinct lui)
    FROM mrd_classes WHERE expiration_date IS NULL;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'cui_cnt','',count(distinct cui)
    FROM mrd_classes WHERE expiration_date IS NULL;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'cls_cnt','',count(distinct sui||lui||cui)
    FROM mrd_classes WHERE expiration_date IS NULL;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'cl_cnt','',count(distinct cui||lui)
    FROM mrd_classes WHERE expiration_date IS NULL;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'cs_cnt','',count(distinct cui||sui)
    FROM mrd_classes WHERE expiration_date IS NULL;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'ls_cnt','',count(distinct lui||sui)
    FROM mrd_classes WHERE expiration_date IS NULL;

    exec meme_utility.drop_it('table','qa_mrconso_${release}_gold_t1');
    CREATE TABLE qa_mrconso_${release}_gold_t1 (
       lat   VARCHAR2(100) NOT NULL,
       ts  VARCHAR2(3000),
       stt  VARCHAR2(3),
       ispref CHAR(1),
       cui VARCHAR2(10),
       aui VARCHAR2(10),
       sui VARCHAR2(10),
       lui VARCHAR2(10));

    -- Assume defaults
    INSERT INTO qa_mrconso_${release}_gold_t1
    SELECT /*+ parallel(c) */  distinct language,'S','VU','N',cui,aui,sui,lui
    FROM mrd_classes c WHERE expiration_date IS NULL;

    commit;

    -- Set TS=P
    --UPDATE /*+ parallel(a) */ qa_mrconso_${release}_gold_t1 a
    --SET ts = 'P'
    --WHERE (cui,lui) IN
    -- (SELECT cui,lui FROM mrd_classes
    --  WHERE expiration_date IS NULL
    -- AND pflag_c='C');

    -- Set STT=PF
    -- UPDATE /*+ parallel(a) */ qa_mrconso_${release}_gold_t1 a
    -- SET stt = 'PF'
    -- WHERE (cui,sui) IN
    -- (SELECT cui,sui FROM mrd_classes
    --  WHERE expiration_date IS NULL
    --    AND pflag_l = 'L');

    -- SET ISPREF='Y'
    -- UPDATE /*+ parallel(a) */ qa_mrconso_${release}_gold_t1 a
    -- SET ispref = 'Y'
    -- WHERE (cui,aui) IN
    -- (SELECT cui,aui FROM mrd_classes
    --  WHERE expiration_date IS NULL
    --    AND pflag_s = 'S');

    --INSERT INTO qa_mrconso_${release}_gold
    --SELECT 'ts_tally',ts,count(*)
    --FROM qa_mrconso_${release}_gold_t1 a
    --GROUP BY ts;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'lang_tally',lat,count(*)
    FROM qa_mrconso_${release}_gold_t1 a
    GROUP BY lat;

    --INSERT INTO qa_mrconso_${release}_gold
    --SELECT 'stt_tally',stt,count(*)
    --FROM qa_mrconso_${release}_gold_t1 a
    --GROUP BY stt;

    --INSERT INTO qa_mrconso_${release}_gold
    --SELECT 'ispref_tally',ispref,count(*)
    --FROM qa_mrconso_${release}_gold_t1 a
    --GROUP BY ispref;

    --INSERT INTO qa_mrconso_${release}_gold
    --SELECT 'ts_stt_tally',ts||'|'||stt,count(*)
    --FROM qa_mrconso_${release}_gold_t1 a
    --GROUP BY ts,stt;

    --INSERT INTO qa_mrconso_${release}_gold
    --SELECT 'lat_ts_stt_ispref_tally',
    --   lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    --FROM qa_mrconso_${release}_gold_t1 a
    --GROUP BY lat,ts,stt,ispref;

    DROP TABLE qa_mrconso_${release}_gold_t1;

    -- LAT/SAB/TTY count
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'sab_lat_tty_tally', root_source||'|'||language||'|'||tty,
         count(distinct aui)
    FROM mrd_classes WHERE expiration_date IS NULL
    GROUP BY root_source,language,tty;

    -- SAB/SUPPRESS count
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'sab_suppress_tally', root_source || '|' || suppressible,
         count(distinct aui)
    FROM mrd_classes WHERE expiration_date IS NULL
    GROUP BY root_source,suppressible;

    -- SRL count
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'srl_tally', restriction_level,
           count(distinct aui)
    FROM mrd_classes a, mrd_source_rank b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.root_source=b.root_source
      AND is_current = 'Y'
    GROUP BY restriction_level;

    -- distinct SCD by SAB count
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'sab_scd_tally', root_source,
        count(distinct code)
    FROM mrd_classes
    WHERE expiration_date IS NULL
    GROUP BY root_source;

    -- distinct SCUI by SAB count
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'sab_scui_tally', root_source,
    count(distinct source_cui)
    FROM mrd_classes
    WHERE expiration_date IS NULL
    HAVING count(distinct source_cui) > 0
    GROUP BY root_source;

    -- SAB count
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'sab_tally', root_source, count(distinct aui)
    FROM mrd_classes
    WHERE expiration_date IS NULL
    GROUP BY root_source;

    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'srl_tally', restriction_level,
           count(distinct aui)
    FROM mrd_classes a, mrd_source_rank b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.root_source=b.root_source
      AND is_current = 'Y'
    GROUP BY restriction_level;

    -- TTY Count
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'tty_tally', tty, count(distinct aui)
    FROM mrd_classes WHERE expiration_date IS NULL
    GROUP BY tty;

    -- suppressible termgroups
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'suppr_termgrp_tally',root_source||'|'||tty||'|'||suppressible,
       count(distinct aui)
    FROM mrd_classes WHERE expiration_date IS NULL
    GROUP BY root_source,tty,suppressible;

    -- Count ambiguous SUIs, LUIs
    -- should this be by language?
     exec meme_utility.drop_it('table','t_mrd_ambig_suis');    
     create table t_mrd_ambig_suis as
         SELECT sui FROM mrd_classes
                  WHERE expiration_date IS NULL
                  GROUP BY sui HAVING count(distinct cui)>1;   
                   
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'ambig_suis_cnt', '', count(distinct sui)
    FROM mrd_classes WHERE expiration_date IS NULL
    AND sui IN (SELECT sui FROM t_mrd_ambig_suis);

	exec meme_utility.drop_it('table','t_mrd_ambig_luis');    
    create table t_mrd_ambig_luis as
         SELECT lui FROM mrd_classes
                  WHERE expiration_date IS NULL
                  GROUP BY lui HAVING count(distinct cui)>1;
                    	
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'ambig_luis_cnt', '', count(distinct lui)
    FROM mrd_classes WHERE expiration_date IS NULL
    AND lui IN (SELECT lui FROM t_mrd_ambig_luis);

--    -- ambiguous strings
--    INSERT INTO qa_mrconso_${release}_gold
--    SELECT 'ambig_str_tally', substr(str,0,200), count(distinct cui) FROM
--      (SELECT DISTINCT upper(string) as str, cui
--       FROM string_ui a, mrd_classes b
--       WHERE expiration_date IS NULL
--         AND a.sui=b.sui
--       AND b.language='ENG'
--       AND b.isui IN
--       (SELECT isui FROM mrd_classes
--        WHERE expiration_date IS NULL
--        GROUP BY isui HAVING count(distinct cui)>1))
--    GROUP by str;

    -- min(length(STR))
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'min_length', '', min(length(string))
    FROM mrd_classes a, string_ui b
    WHERE a.sui=b.sui
      AND expiration_date IS NULL;

    -- max(length(STR))
    INSERT INTO qa_mrconso_${release}_gold
    SELECT 'max_length', '', max(length(string))
    FROM mrd_classes a, string_ui b
    WHERE a.sui=b.sui
      AND expiration_date IS NULL;

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mraui") then
echo "Generating MRAUI QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mraui_${release}_gold');
    CREATE TABLE qa_mraui_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'row_cnt','',count(*)
    FROM mrd_aui_history WHERE expiration_date IS NULL;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'aui1_cnt','',count(distinct aui1)
    FROM mrd_aui_history WHERE expiration_date IS NULL;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'aui2_cnt','',count(distinct aui2)
    FROM mrd_aui_history WHERE expiration_date IS NULL;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'cui1_cnt','',count(distinct cui1)
    FROM mrd_aui_history WHERE expiration_date IS NULL;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'cui2_cnt','',count(distinct cui2)
    FROM mrd_aui_history WHERE expiration_date IS NULL;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'a1_c1_a2_c2_cnt','',count(distinct aui1||cui1||aui2||cui2)
    FROM mrd_aui_history WHERE expiration_date IS NULL;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'rel_tally', relationship_name, count(distinct aui1||cui1)
    FROM mrd_aui_history WHERE expiration_date IS NULL
    GROUP BY relationship_name;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'rela_tally', relationship_attribute, count(distinct aui1||cui1)
    FROM mrd_aui_history WHERE expiration_date IS NULL
    GROUP BY relationship_attribute;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'ver_rel_rela_tally', ver||'|'||relationship_name||'|'||relationship_attribute, count(distinct aui1||cui1)
    FROM mrd_aui_history WHERE expiration_date IS NULL
    GROUP BY ver,relationship_name,relationship_attribute;

    INSERT INTO qa_mraui_${release}_gold
    SELECT 'ver_tally', ver, count(distinct aui1||cui1)
    FROM mrd_aui_history WHERE expiration_date IS NULL
    GROUP BY ver;


EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif
if ($mode == "all" || $mode == "ambig") then
    echo "Generating AMBIG.{SUI,LUI} QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_ambig_${release}_gold');
    CREATE TABLE qa_ambig_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- AMBIG.SUI counts
    INSERT INTO qa_ambig_${release}_gold
    SELECT 'sui_cnt', 'AMBIGSUI', count(distinct sui)
    FROM mrd_classes
    WHERE expiration_date IS NULL
    AND sui IN
     (SELECT sui FROM mrd_classes WHERE expiration_date IS NULL
      GROUP BY sui HAVING count(distinct cui)>1);

    INSERT INTO qa_ambig_${release}_gold
    SELECT 'cs_cnt', 'AMBIGSUI', count(distinct cui||sui)
    FROM mrd_classes
    WHERE expiration_date IS NULL
    AND sui IN
     (SELECT sui FROM mrd_classes WHERE expiration_date IS NULL
      GROUP BY sui HAVING count(distinct cui)>1);

    -- AMBIG.LUI counts
    INSERT INTO qa_ambig_${release}_gold
    SELECT 'lui_cnt', 'AMBIGLUI', count(distinct lui)
    FROM mrd_classes
    WHERE expiration_date IS NULL
    AND lui IN
     (SELECT lui FROM mrd_classes WHERE expiration_date IS NULL
      GROUP BY lui HAVING count(distinct cui)>1);

    INSERT INTO qa_ambig_${release}_gold
    SELECT 'cl_cnt', 'AMBIGLUI', count(distinct cui||lui)
    FROM mrd_classes
    WHERE expiration_date IS NULL
    AND lui IN
     (SELECT lui FROM mrd_classes WHERE expiration_date IS NULL
      GROUP BY lui HAVING count(distinct cui)>1);

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif


if ($mode == "all" || $mode == "mrcui") then
    echo "Generating MERGED{CUI,LUI} QA Counts ... `/bin/date`"
    echo "Generating DELETED{CUI,LUI,SUI} QA Counts ... `/bin/date`"
    echo "Generating MRCUI QA Counts ... `/bin/date`"

    #
    # merged counts are part of the MRCUI
    # target, so they must be counted together
    #

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrcui_${release}_gold');
    CREATE TABLE qa_mrcui_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    --
    -- Handle old LUIs
    --

    -- Look up old luis
    -- a. Identify LUIs from previous version tha are no longer active.
    exec MEME_UTILITY.drop_it('table','t_old_luis_$$');
    CREATE TABLE t_old_luis_$$ (lui) AS
    (((SELECT lui
     FROM classes
     WHERE last_release_cui IS NOT NULL
       AND released != 'N'
     UNION
     SELECT lui
     FROM dead_classes
     WHERE last_release_cui IS NOT NULL
       AND released != 'N'
     UNION
     SELECT lui
     FROM foreign_classes
     WHERE last_release_cui IS NOT NULL
       AND released != 'N')
     MINUS SELECT new_lui FROM lui_assignment)
     UNION
     SELECT old_lui FROM lui_assignment
      WHERE sui in (SELECT sui FROM classes
                    WHERE last_release_cui IS NOT NULL
                    AND released != 'N'
                    UNION SELECT sui FROM dead_classes
                    WHERE last_release_cui IS NOT NULL
                    AND released != 'N'
                    UNION SELECT sui FROM foreign_classes
                    WHERE last_release_cui IS NOT NULL
                    AND released != 'N'))
    MINUS
    SELECT lui
    FROM mrd_classes
    WHERE expiration_date IS NULL;

    exec meme_utility.drop_it('table','t_merged_luis_$$');
    CREATE TABLE t_merged_luis_$$ (
        old_lui VARCHAR2(10) NOT NULL,
        new_lui VARCHAR2(10) NOT NULL );

    -- b. Identify which old LUIs are merged.
    INSERT INTO t_merged_luis_$$ (old_lui, new_lui)
    SELECT DISTINCT old_lui, new_lui
    FROM lui_assignment
    WHERE old_lui != new_lui;

    -- "live" LUIs cannot be merged luis
    DELETE FROM t_merged_luis_$$ WHERE old_lui IN
           (SELECT lui FROM mrd_classes WHERE expiration_date IS NULL);

    -- Only "live" luis can be "new" luis
    DELETE FROM t_merged_luis_$$ WHERE new_lui IN
           (SELECT new_lui FROM t_merged_luis_$$
            MINUS SELECT lui FROM mrd_classes WHERE expiration_date IS NULL);
    
        -- c. Look up preferred names for deleted LUIs
    exec MEME_UTILITY.drop_it('table','t_dead_luis_$$');
    CREATE TABLE t_dead_luis_$$ AS
    (SELECT DISTINCT lui FROM t_old_luis_$$
    MINUS SELECT old_lui FROM t_merged_luis_$$);

        -- Old luis can not be merged luis
    -- DELETE FROM t_merged_luis_$$ WHERE old_lui IN
        --    (SELECT lui FROM t_old_luis_$$);
    --
    -- Handle old SUIs
    --

    -- Look up old suis
    -- d. Identify SUIs from pervious version that are no longer active.
    exec MEME_UTILITY.drop_it('table','t_dead_suis_$$');
    CREATE TABLE t_dead_suis_$$ (sui) AS
    (SELECT sui
     FROM classes
     WHERE last_release_cui IS NOT NULL
       AND released != 'N'
     UNION
     SELECT sui
     FROM dead_classes
     WHERE last_release_cui IS NOT NULL
       AND released != 'N'
     UNION
     SELECT sui
     FROM foreign_classes
     WHERE last_release_cui IS NOT NULL
       AND released != 'N'
--     UNION SELECT sui FROM classes_feedback)
    )
    MINUS
    SELECT sui
    FROM mrd_classes a
    WHERE expiration_date IS NULL;

    --
    -- QA counts for DELETED files
    --

    -- cui count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'pcui_cnt','DELETEDCUI',count(distinct cui1)
    FROM mrd_cui_history
    WHERE relationship_name in ('DEL','RB','RN','RO')
    AND ver = (SELECT max(ver) FROM mrd_cui_history)
    AND expiration_date is null;

    -- lui count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'plui_cnt','DELETEDLUI',count(distinct lui)
    FROM t_dead_luis_$$;

    -- sui count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'psui_cnt','DELETEDSUI',count(distinct sui)
    FROM t_dead_suis_$$;

    --
    -- QA counts for MERGED files
    --

    -- cui1 count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'pcui_cnt','MERGEDCUI',count(distinct cui1)
    FROM mrd_cui_history
    WHERE relationship_name in ('SY')
    AND ver = (SELECT max(ver) FROM mrd_cui_history)
    AND expiration_date is null;

    -- cui2 count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'cui_cnt','MERGEDCUI',count(distinct cui2)
    FROM mrd_cui_history
    WHERE relationship_name in ('SY')
    AND ver = (SELECT max(ver) FROM mrd_cui_history)
    AND expiration_date is null;

    -- cui1,cui2 count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'pcui_cui_cnt','MERGEDCUI',count(distinct cui1||cui2)
    FROM mrd_cui_history
    WHERE relationship_name in ('SY')
    AND ver = (SELECT max(ver) FROM mrd_cui_history)
    AND expiration_date is null;

    -- lui1 count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'plui_cnt','MERGEDLUI',count(distinct old_lui)
    FROM t_merged_luis_$$;

    -- lui2 count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'lui_cnt','MERGEDLUI',count(distinct new_lui)
    FROM t_merged_luis_$$;

    -- lui1,lui2 count
    INSERT INTO qa_mrcui_${release}_gold
    SELECT 'plui_lui_cnt','MERGEDLUI',count(distinct old_lui||new_lui)
    FROM t_merged_luis_$$;

    --
    -- QA Counts for MRCUI
    --

    -- ROW count
    INSERT into qa_mrcui_${release}_gold
    SELECT 'row_cnt','',count(*)
    FROM mrd_cui_history
    WHERE expiration_date is null;

    -- CUI1 count
    INSERT into qa_mrcui_${release}_gold
    SELECT 'cui1_cnt','',count(distinct cui1)
    FROM mrd_cui_history
    WHERE expiration_date is null;

    -- CUI2 count
    -- to account for the empty field
    INSERT into qa_mrcui_${release}_gold
    SELECT 'cui2_cnt','',count(distinct cui2) + 1
    FROM mrd_cui_history
    WHERE expiration_date is null;

    -- REL tally
    INSERT into qa_mrcui_${release}_gold
    SELECT 'rel_tally',relationship_name,count(distinct cui1||ver||relationship_name||cui2)
    FROM mrd_cui_history
    WHERE expiration_date is null
    GROUP BY relationship_name;

    -- VER,REL,RELA tally
    INSERT into qa_mrcui_${release}_gold
    SELECT 'ver_rel_rela_tally',ver||'|'||relationship_name||'|'||relationship_attribute,count(distinct cui1||ver||relationship_name||cui2)
    FROM mrd_cui_history
    WHERE expiration_date is null
        GROUP BY ver,relationship_name,relationship_attribute;

    --
    -- Cleanup
    --
    exec meme_utility.drop_it('index','x_mrcui_gold');
    exec meme_utility.drop_it('table','t_old_luis_$$');
    exec meme_utility.drop_it('table','t_old_suis_$$');
    exec meme_utility.drop_it('table','t_merged_luis_$$');
    exec meme_utility.drop_it('table','t_merged_suis_$$');
    exec meme_utility.drop_it('table','t_dead_luis_$$');
    exec meme_utility.drop_it('table','t_dead_suis_$$');
EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif


if ($mode == "all" || $mode == "mrmap") then
    echo "Generating MRMAP QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrmap_${release}_gold');
    CREATE TABLE qa_mrmap_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );


    --
    -- QA Counts for MRMAP
    --

    -- ROW count
    INSERT into qa_mrmap_${release}_gold
    SELECT 'row_cnt','',count(*)
    FROM mrd_attributes
    WHERE attribute_name = 'XMAP'
    AND expiration_date is null;

    -- SAB tally
    INSERT into qa_mrmap_${release}_gold
    SELECT 'sab_tally',root_source,count(distinct atui)
    FROM mrd_attributes
    WHERE expiration_date is null
    AND attribute_name = 'XMAP'
    GROUP BY root_source;

    -- SAB/FROMEXPR tallys
    INSERT into qa_mrmap_${release}_gold
    SELECT 'sab_fromexpr_tally',root_source,count(distinct hashcode)
    FROM mrd_attributes
    WHERE attribute_name = 'XMAPFROM'
    AND expiration_date is null
    GROUP BY root_source;

    -- SAB/TOEXPR tally
    INSERT into qa_mrmap_${release}_gold
    SELECT 'sab_toexpr_tally', root_source, count(x)
    FROM (select
                nvl(SUBSTR(attribute_value, instr(attribute_value, '~', 1, 2) + 1,
                (INSTR(attribute_value, '~', 1, 3) -
                INSTR(attribute_value, '~', 1, 2)) -1 ),'null') x, root_source
    FROM mrd_attributes
    WHERE attribute_name = 'XMAPTO'
    AND expiration_date is null
    AND attribute_value not like '<>Long_Attribute<>:%'
    UNION
    select
                SUBSTR(text_value, instr(text_value, '~', 1, 2) + 1,
                (INSTR(text_value, '~', 1, 3) -
                INSTR(text_value, '~', 1, 2)) -1 ) x, root_source
    FROM mrd_attributes a, mrd_stringtab b
    WHERE attribute_name = 'XMAPTO'
    AND a.expiration_date is null
    AND b.expiration_date is null
    AND attribute_value like '<>Long_Attribute<>:%'
    AND a.hashcode = b.hashcode)
    GROUP BY root_source;

    UPDATE   qa_mrmap_${release}_gold
    set test_count = test_count - (select count(distinct SUBSTR(attribute_value, 1, instr(attribute_value, '~') - 1)) from mrd_attributes
    where attribute_name = 'XMAPTO'
    and expiration_date is null
    and SUBSTR(attribute_value, instr(attribute_value, '~', 1, 2) + 1,
                (INSTR(attribute_value, '~', 1, 3) -
                INSTR(attribute_value, '~', 1, 2)) -1 ) is null)
    where test_name = 'toexpr_cnt';

    -- distinct MAPSETCUI, MAPSUBSETID, MAPRANK, FROMID, TOUI
    -- key count
    INSERT into qa_mrmap_${release}_gold
    SELECT 'key_cnt','',count(*)
        FROM ( (select distinct cui,
                 SUBSTR(attribute_value, 1, instr(attribute_value, '~') - 1),
                 SUBSTR(attribute_value, instr(attribute_value, '~') + 1,
                        (INSTR(attribute_value, '~', 1, 2) -
                            INSTR(attribute_value, '~', 1)) -1 ),
                 SUBSTR(attribute_value, instr(attribute_value, '~', 1, 2) + 1,
                        (INSTR(attribute_value, '~', 1, 3) -
                            INSTR(attribute_value, '~', 1, 2)) -1 ),
                 SUBSTR(attribute_value, instr(attribute_value, '~', 1, 5) + 1,
                        (INSTR(attribute_value, '~', 1, 6) -
                            INSTR(attribute_value, '~', 1, 5)) -1 )
                from mrd_attributes
                where attribute_value not like  '<>Long_Attribute<>:%'
                and attribute_name = 'XMAP'
                and expiration_date is null)
       union
        (select distinct cui,
                 SUBSTR(text_value, 1, instr(text_value, '~') - 1),
                 SUBSTR(text_value, instr(text_value, '~') + 1,
                        (INSTR(text_value, '~', 1, 2) -
                            INSTR(text_value, '~', 1)) -1 ),
                 SUBSTR(text_value, instr(text_value, '~', 1, 2) + 1,
                        (INSTR(text_value, '~', 1, 3) -
                            INSTR(text_value, '~', 1, 2)) -1 ),
                 SUBSTR(text_value, instr(text_value, '~', 1, 5) + 1,
                        (INSTR(text_value, '~', 1, 6) -
                            INSTR(text_value, '~', 1, 5)) -1 )
                from mrd_attributes a, mrd_stringtab b
                where attribute_value like  '<>Long_Attribute<>:%'
                and a.hashcode=b.hashcode
                and attribute_name = 'XMAP'
                and a.expiration_date is null) );

    -- rel_tally
    -- Get all the Rels with proper release names
    INSERT into qa_mrmap_${release}_gold
  SELECT 'rel_tally' test_name, RELEASE_NAME TEST_VALUE, COUNT(*) TEST_COUNT FROM (
    SELECT  a.hashcode,c.release_name
    FROM   mrd_attributes a, inverse_relationships c
    WHERE attribute_name = 'XMAP'
    and attribute_value not like '<>Long_Attribute<>:%'
    and (SUBSTR(attribute_value, instr(attribute_value, '~', 1, 3) + 1,
                (INSTR(attribute_value, '~', 1, 4) -
                INSTR(attribute_value, '~', 1, 3)) -1 ) = c.relationship_name
                or
                SUBSTR(attribute_value, instr(attribute_value, '~', 1, 3) + 1,
                (INSTR(attribute_value, '~', 1, 4) -
                INSTR(attribute_value, '~', 1, 3)) -1 ) = c.release_name)
    AND a.expiration_date is null
 Union           
    SELECT  a.hashcode, c.release_name
    FROM   mrd_attributes a, mrd_stringtab b, inverse_relationships c
    WHERE attribute_name = 'XMAP'
    and attribute_value like '<>Long_Attribute<>:%'
    and (SUBSTR(text_value, instr(text_value, '~', 1, 3) + 1,
                (INSTR(text_value, '~', 1, 4) -
                INSTR(text_value, '~', 1, 3)) -1 ) = c.relationship_name
                or
                SUBSTR(text_value, instr(text_value, '~', 1, 3) + 1,
                (INSTR(text_value, '~', 1, 4) -
                INSTR(text_value, '~', 1, 3)) -1 ) = c.release_name)
    and a.hashcode=b.hashcode
    AND a.expiration_date is null
    )
    GROUP BY release_name;

    -- rela_tally
    -- calculate the 'mapped_to' first
    -- where rel is not xr and rela is not null
    INSERT into qa_mrmap_${release}_gold
        SELECT 'rela_tally', test_name, count(*) test_value from (
    select a.hashcode, 
            SUBSTR(attribute_value, instr(attribute_value, '~', 1, 4) + 1,
                (INSTR(attribute_value, '~', 1, 5) -
                INSTR(attribute_value, '~', 1, 4)) -1 ) test_name
               FROM   mrd_attributes a
    WHERE attribute_name = 'XMAP'
    and attribute_value not like '<>Long_Attribute<>:%'
    AND a.expiration_date is null
    AND SUBSTR(attribute_value, instr(attribute_value, '~', 1, 3) + 1,
                (INSTR(attribute_value, '~', 1, 4) -
                INSTR(attribute_value, '~', 1, 3)) -1 ) != 'XR'
    AND SUBSTR(attribute_value, instr(attribute_value, '~', 1, 4) + 1,
                (INSTR(attribute_value, '~', 1, 5) -
                INSTR(attribute_value, '~', 1, 4)) -1 ) is not null
union                
        select a.hashcode, 
            SUBSTR(text_value, instr(text_value, '~', 1, 4) + 1,
                (INSTR(text_value, '~', 1, 5) -
                INSTR(text_value, '~', 1, 4)) -1 ) test_name
               FROM   mrd_attributes a, mrd_stringtab b
    WHERE attribute_name = 'XMAP'
    and attribute_value like '<>Long_Attribute<>:%'
    and a.hashcode=b.hashcode
    AND a.expiration_date is null
    AND SUBSTR(text_value, instr(text_value, '~', 1, 3) + 1,
                (INSTR(text_value, '~', 1, 4) -
                INSTR(text_value, '~', 1, 3)) -1 ) != 'XR'
    AND SUBSTR(text_value, instr(text_value, '~', 1, 4) + 1,
                (INSTR(text_value, '~', 1, 5) -
                INSTR(text_value, '~', 1, 4)) -1 ) is not null)
    GROUP BY test_name;

    -- rela_tally
    INSERT into qa_mrmap_${release}_gold
    SELECT 'rela_tally',
            '' test_name,
           count(*) test_value from (
           (select hashcode
    FROM   mrd_attributes a
    WHERE attribute_name = 'XMAP' and
    a.attribute_value not like '<>Long_Attribute<>:%'
    AND a.expiration_date is null
    AND (SUBSTR(attribute_value, instr(attribute_value, '~', 1, 3) + 1,
                (INSTR(attribute_value, '~', 1, 4) -
                INSTR(attribute_value, '~', 1, 3)) -1 ) = 'XR'
    OR SUBSTR(attribute_value, instr(attribute_value, '~', 1, 4) + 1,
                (INSTR(attribute_value, '~', 1, 5) -
                INSTR(attribute_value, '~', 1, 4)) -1 ) is null))
union
(select a.hashcode
    FROM   mrd_attributes a, mrd_stringtab b 
    WHERE attribute_name = 'XMAP' and
    a.attribute_value like '<>Long_Attribute<>:%'
    and a.hashcode=b.hashcode
    AND a.expiration_date is null
    AND (SUBSTR(text_value, instr(text_value, '~', 1, 3) + 1,
                (INSTR(text_value, '~', 1, 4) -
                INSTR(text_value, '~', 1, 3)) -1 ) = 'XR'
    OR SUBSTR(text_value, instr(text_value, '~', 1, 4) + 1,
                (INSTR(text_value, '~', 1, 5) -
                INSTR(text_value, '~', 1, 4)) -1 ) is null) ));

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrcxt") then
    echo "Generating MRCXT QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrcxt_${release}_gold');
    CREATE TABLE qa_mrcxt_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- CUI count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'cui_cnt','',count(distinct cui)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui;

    -- CUI2 count (should match CUI count)
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'cui2_cnt','',test_count
    FROM qa_mrcxt_${release}_gold
    WHERE test_name='cui_cnt';

    -- SUI count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'sui_cnt','',count(distinct sui)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui;

    -- AUI count
    -- IF off, there are probably AUI in mrd_context not in mrd_classes
    --
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'aui_cnt','',count(distinct aui)
    FROM mrd_contexts a
    WHERE a.expiration_date IS NULL;

    -- AUI2 count (should match CUI count)
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'aui2_cnt','',test_count
    FROM qa_mrcxt_${release}_gold
    WHERE test_name='aui_cnt';

    -- CUI,AUI count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'ca_cnt','',count(distinct a.cui||b.aui)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui;

    -- Distinct context count
    -- should equal row count of mrd_contexts
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'cxt_cnt','',
        count(distinct parent_treenum||'|'||aui||
                       relationship_attribute)
    FROM mrd_contexts
    WHERE expiration_date IS NULL
      AND aui IN (SELECT aui FROM mrd_classes WHERE expiration_date IS NULL);

    -- Count SAB where SCD is null
    -- ? Not sure about this one

    -- Row count
    -- ANC rows = count all tree-tops
    -- CCP rows = # rows in mrd_contexts
    -- SIB rows = # rows in mrd_contexts,mrd_contexts with same parent tn
    -- CHD rows = # rows in mrd_contexts, mrd_contexts where one is parent
    exec meme_utility.drop_it('table','qa_mrcxt_rc');
    CREATE TABLE qa_mrcxt_rc (
        cxl    CHAR(3),
        sab    VARCHAR2(40),
        rela   VARCHAR2(100),
        ct     NUMBER(12)
    );

    -- CCP
    INSERT INTO qa_mrcxt_rc
    SELECT /*+ parallel(a) */
         'CCP',root_source,relationship_attribute,count(*) as ct
    FROM mrd_contexts a WHERE expiration_date IS NULL
    GROUP BY root_source, relationship_attribute;

    COMMIT;

    -- ANC|1
    -- Every context has a ANC|1 except for tree-tops
    --
    INSERT INTO qa_mrcxt_rc
    SELECT /*+ parallel(a) */
         'ANC',root_source,'',count(*)-1
    FROM mrd_contexts a
    WHERE expiration_date IS NULL
     -- AND parent_treenum IS NULL
    GROUP BY root_source;

    COMMIT;

    exec meme_utility.drop_it('table','qa_mrcxt_contexts_ignore_rela');
    CREATE TABLE qa_mrcxt_contexts_ignore_rela AS
    SELECT distinct root_source FROM mrd_source_rank
        WHERE context_type like '%IGNORE-RELA%'
      AND expiration_date IS NULL;

    -- ANC (level 2 and up )
    -- Every context with *.* has ANC|2
    -- Every context with *.*.* has ANC|3, etc.
    DECLARE
        ptn  VARCHAR2(1000);
    BEGIN
        ptn := '%.%';
        LOOP
            --
            -- This captures the RELA of the CCP row, not the ANC row
            -- Fine for all sources except for ignore rela sources
            --
            INSERT INTO qa_mrcxt_rc
            SELECT /*+ PARALLEL(a) */
              'ANC',a.root_source,a.relationship_attribute,count(*)
            FROM mrd_contexts a
            WHERE a.expiration_date IS NULL
              AND a.parent_treenum like ptn
              AND root_source NOT IN
                (select distinct root_source from qa_mrcxt_contexts_ignore_rela)
            GROUP BY a.root_source, a.relationship_attribute;
            EXIT WHEN SQL%ROWCOUNT = 0;

            COMMIT;

            ptn := ptn || '.%';
        END LOOP;
    END;
/

    -- ANC (level 2 and up )
    -- Every context with *.* has ANC|2
    -- Every context with *.*.* has ANC|3, etc.
    DECLARE
        ptn  VARCHAR2(1000);
    BEGIN
        ptn := '%.%';
        LOOP
            --
            -- This captures the RELA of the ANC rows
            --
            EXECUTE IMMEDIATE
            'INSERT INTO qa_mrcxt_rc
            SELECT
              ''ANC'',a.root_source,anc.relationship_attribute,count(*)
            FROM mrd_contexts a,
                     mrd_contexts anc
            WHERE a.expiration_date IS NULL
              AND a.parent_treenum like ''' || ptn || '''
              AND a.root_source in (select distinct root_source from qa_mrcxt_contexts_ignore_rela)
              AND anc.root_source in (select distinct root_source from qa_mrcxt_contexts_ignore_rela)
              AND a.root_source = anc.root_source
              AND length(anc.parent_treenum) =
                (((length(''' || ptn || ''')-1)/2)*9)-1
              AND substr(a.parent_treenum,1,
                         (((length(''' || ptn || ''')-1)/2)*9)+8) =
                anc.parent_treenum || ''.'' || anc.aui
            GROUP BY a.root_source, anc.relationship_attribute';
            EXIT WHEN SQL%ROWCOUNT = 0;
            ptn := ptn || '.%';
        END LOOP;
    END;
/

    -- SIB
    INSERT INTO qa_mrcxt_rc
    SELECT 'SIB',a.root_source,a.relationship_attribute,count(*)
    FROM mrd_contexts a, mrd_contexts b
    WHERE a.parent_treenum = b.parent_treenum
      AND a.aui != b.aui
      AND (NVL(a.relationship_attribute,'null') =
           NVL(b.relationship_attribute,'null') OR
           a.root_source in (select distinct root_source from qa_mrcxt_contexts_ignore_rela) )
      AND substr(a.release_mode,2,1) = '1'
      AND substr(b.release_mode,2,1) = '1'
      AND a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.parent_treenum IS NOT NULL
      AND b.parent_treenum IS NOT NULL
    GROUP BY a.root_source, a.relationship_attribute;

    -- MSH has null rela for SIB
    UPDATE qa_mrcxt_rc
    SET rela='' WHERE sab = 'MSH' and cxl='SIB';

    -- CHD (par is not treetop)
    INSERT INTO qa_mrcxt_rc
    SELECT 'CHD',b.root_source,a.relationship_attribute,count(*)
    FROM mrd_contexts a, mrd_contexts b
    WHERE a.parent_treenum = b.parent_treenum||'.'||b.aui
      AND (NVL(a.relationship_attribute,'null') =
           NVL(b.relationship_attribute,'null') OR
           a.root_source in (select distinct root_source from qa_mrcxt_contexts_ignore_rela) )
      AND a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.parent_treenum IS NOT NULL
      AND b.parent_treenum IS NOT NULL
    GROUP BY b.root_source, a.relationship_attribute;

    -- CHD (par is treetop)
    INSERT INTO qa_mrcxt_rc
    SELECT 'CHD',b.root_source,a.relationship_attribute,count(*)
    FROM mrd_contexts a, mrd_contexts b
    WHERE a.parent_treenum = b.aui
      AND a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.parent_treenum IS NOT NULL
    GROUP BY b.root_source, a.relationship_attribute;

    -- Row count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'row_cnt','',sum(ct)
    FROM qa_mrcxt_rc;

    -- SAB count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'sab_tally',sab,sum(ct)
    FROM qa_mrcxt_rc
    GROUP BY sab;

    -- SAB,REL count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'sab_rel_tally',sab||'|'||rela,sum(ct)
    FROM qa_mrcxt_rc
    GROUP BY sab,rela;

    -- REL count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'rel_tally',rela,sum(ct)
    FROM qa_mrcxt_rc
    GROUP BY rela;

    -- CXL count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'cxl_tally',cxl,sum(ct)
    FROM qa_mrcxt_rc
    GROUP BY cxl;

    -- SAB,CXL count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'sab_cxl_tally',sab||'|'||cxl,sum(ct)
    FROM qa_mrcxt_rc
    GROUP BY sab,cxl;

    -- SAB,CXL,REL count
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'sab_cxl_rel_tally',sab||'|'||cxl||'|'||rela,sum(ct)
    FROM qa_mrcxt_rc
    GROUP BY sab,cxl,rela;

    exec meme_utility.drop_it('table','qa_mrcxt_rc');

    exec meme_utility.drop_it('table','qa_mrcxt_${release}_tmp1');
    CREATE TABLE qa_mrcxt_${release}_tmp1 AS
    SELECT /*+ parallel(a) */ count(*) ct,aui, root_source
    FROM mrd_contexts a
    WHERE expiration_date IS NULL
    GROUP BY aui,root_source;

    -- Distinct SAB,CXN count
    -- Loop until no further found
    DECLARE
        ct   NUMBER(12);
        mx   NUMBER(12);
    BEGIN
        SELECT max(ct) INTO mx
        FROM qa_mrcxt_${release}_tmp1;
        ct := 1;
        LOOP
            EXIT WHEN mx IS NULL;
            EXECUTE IMMEDIATE
            'INSERT INTO qa_mrcxt_${release}_gold
             SELECT ''sab_cxn_tally'', root_source||''|' || ct || ''',
                    count(*) FROM
               (SELECT /*+ parallel(a) */ *
                FROM qa_mrcxt_${release}_tmp1 a
                WHERE ct >= ' || ct || ')
             GROUP BY root_source HAVING count(*)>0';
            ct := ct + 1;
            COMMIT;
            EXIT WHEN ct > mx;
        END LOOP;
    END;
/

    DROP TABLE qa_mrcxt_${release}_tmp1;

    -- SAB,CXN,CXL count
    -- Is this useful?  it's difficult and the previous
    -- one is more useful

    -- XC count
    -- Only CCP, SIB, CHD can have XC flag

    -- CCP (both tree-top and non-tree top)
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'xc_cxl_tally','CCP',count(*)
    FROM mrd_contexts
    WHERE expiration_date IS NULL
      AND parent_treenum || '.' || aui IN
       (SELECT parent_treenum FROM mrd_contexts
        WHERE expiration_date IS NULL
        UNION
        SELECT '.'||parent_treenum FROM mrd_contexts
        WHERE expiration_date IS NULL);


    -- SIB (cannot be tree-top)
    INSERT INTO qa_mrcxt_${release}_gold
    SELECT 'xc_cxl_tally','SIB', count(*)
    FROM mrd_contexts a, mrd_contexts b
    WHERE a.parent_treenum = b.parent_treenum
      AND a.aui != b.aui
      AND (NVL(a.relationship_attribute,'null') =
           NVL(b.relationship_attribute,'null') OR
           a.root_source in (select distinct root_source from qa_mrcxt_contexts_ignore_rela) )
      AND substr(a.release_mode,2,1) = '1'
      AND substr(b.release_mode,2,1) = '1'
      AND a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.parent_treenum IS NOT NULL
      AND b.parent_treenum IS NOT NULL
      AND a.parent_treenum || '.' || a.aui IN
      (SELECT parent_treenum FROM mrd_contexts
       WHERE expiration_date IS NULL);

     -- CHD (CHD cannot be tree-top, PAR is not treetop)
     INSERT INTO qa_mrcxt_${release}_gold SELECT 'xc_cxl_tally','CHD',sum(ct) FROM
     (SELECT count(*) as ct
      FROM mrd_contexts a, mrd_contexts b
      WHERE a.parent_treenum = b.parent_treenum||'.'||b.aui
        AND (NVL(b.relationship_attribute,'null') =
           NVL(a.relationship_attribute,'null') OR
           a.root_source in (select distinct root_source from qa_mrcxt_contexts_ignore_rela) )
        AND a.expiration_date IS NULL
        AND b.expiration_date IS NULL
        AND a.parent_treenum IS NOT NULL
        AND b.parent_treenum IS NOT NULL
        AND a.parent_treenum || '.' || a.aui IN
      (SELECT parent_treenum FROM mrd_contexts
       WHERE expiration_date IS NULL)
     UNION ALL
     -- CHD (CHD cannot be tree-top, PAR is tree-top)
     SELECT count(*)
     FROM mrd_contexts a, mrd_contexts b
     WHERE a.parent_treenum = b.aui
      AND a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.parent_treenum IS NOT NULL
      AND a.parent_treenum || '.' || a.aui IN
      (SELECT parent_treenum FROM mrd_contexts
       WHERE expiration_date IS NULL));

    exec meme_utility.drop_it('table','qa_mrcxt_contexts_ignore_rela');

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif


if ($mode == "all" || $mode == "mrhier") then
    echo "Generating MRHIER QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrhier_${release}_gold');
    CREATE TABLE qa_mrhier_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- Row count
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'row_cnt','',
        count(distinct b.aui||parent_treenum||relationship_attribute)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui;

    -- CUI count
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'cui_cnt','',count(distinct cui)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui;

    -- AUI count
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'aui_cnt','',count(distinct b.aui)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui;

    -- CUI,AUI count
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'cui_aui_cnt','',count(distinct a.cui||b.aui)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui;

    -- CUI,AUI,CXN count
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'cui_aui_cxn_cnt','', sum(mx)
    FROM
     (SELECT aui, max(ct) mx FROM
      (SELECT /*+ PARALLEL(cr) */ aui, root_source,
        count(distinct aui||parent_treenum||relationship_attribute) ct
       FROM mrd_contexts cr
       WHERE expiration_date is null
       GROUP BY aui, root_source)
      GROUP BY aui)
    WHERE aui IN
     (SELECT aui FROM mrd_classes WHERE expiration_date IS NULL);

    COMMIT;

    -- Min CXN count
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'min_cxn_cnt','',1 from dual;

    -- Max CXN count
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'max_cxn_cnt','',max(ct) FROM
     (SELECT count(*) as ct FROM mrd_contexts
      WHERE expiration_date is null
      GROUP BY aui);

    -- SAB Tally
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'sab_tally',b.root_source,count(distinct b.aui||parent_treenum)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui
      AND a.root_source !='UWDA'
        GROUP BY b.root_source
        UNION
    SELECT 'sab_tally',b.root_source,count(distinct b.aui||parent_treenum||relationship_attribute)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui
      AND a.root_source='UWDA'
    GROUP BY b.root_source;

    -- SAB/HCD Tally
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'sab_hcd_tally',b.root_source,count(distinct nvl(b.hierarchical_code,'null'))
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui
        GROUP BY b.root_source;

    -- SAB/RELA Tally
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'sab_rela_tally',b.root_source||'|'||b.relationship_attribute,count(distinct b.aui||b.parent_treenum||b.relationship_attribute)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui
      AND b.parent_treenum IS NOT NULL
        GROUP BY b.root_source,relationship_attribute;

    -- RELA Tally
    INSERT INTO qa_mrhier_${release}_gold
    SELECT 'rela_tally',relationship_attribute,count(distinct a.aui||parent_treenum)
    FROM mrd_classes a, mrd_contexts b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.aui = b.aui
    GROUP BY relationship_attribute;

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrhist") then
    echo "Generating MRHIST QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrhist_${release}_gold');
    CREATE TABLE qa_mrhist_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    INSERT INTO qa_mrhist_${release}_gold
    SELECT 'row_cnt','',count(distinct attribute_value)
    FROM mrd_attributes a
    WHERE attribute_name='COMPONENTHISTORY'
      AND expiration_date IS NULL;

    INSERT INTO qa_mrhist_${release}_gold
    SELECT 'cui_cnt','',count(distinct cui)
    FROM mrd_attributes
    WHERE attribute_name='COMPONENTHISTORY'
      AND expiration_date IS NULL;

    INSERT INTO qa_mrhist_${release}_gold
    SELECT 'ui_cnt','',count(distinct ui)
    FROM
      (SELECT substr(attribute_value,1,instr(attribute_value,'~')) ui
       FROM mrd_attributes
       WHERE attribute_name='COMPONENTHISTORY'
       AND expiration_date IS NULL);

    INSERT INTO qa_mrhist_${release}_gold
    SELECT 'sab_tally',root_source,count(distinct attribute_value)
    FROM mrd_attributes
    WHERE attribute_name = 'COMPONENTHISTORY'
      AND expiration_date IS NULL
    GROUP BY root_source;

    INSERT INTO qa_mrhist_${release}_gold
    SELECT 'sab_sver_tally',sab||'|'||sver,count(*)
    FROM
      (
    SELECT root_source sab,
        SUBSTR(attribute_value, instr(attribute_value, '~') + 1,
             (INSTR(attribute_value, '~', 1, 2) -
              INSTR(attribute_value, '~', 1)) -1 ) sver
       FROM mrd_attributes
       WHERE attribute_name='COMPONENTHISTORY'
       AND attribute_value not like '%Long%'
       AND expiration_date IS NULL
       union all
       SELECT root_source sab,
        SUBSTR(text_value, instr(text_value, '~') + 1,
             (INSTR(text_value, '~', 1, 2) -
              INSTR(text_value, '~', 1)) -1 ) sver
       FROM mrd_attributes a, mrd_stringtab b
       WHERE a.attribute_name='COMPONENTHISTORY'
       AND a.expiration_date IS NULL
       and a.attribute_value like '%Long%'
       and a.hashcode = b.hashcode)
       group by sab, sver;



EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrdoc") then
    echo "Generating MRDOC QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrdoc_${release}_gold');
    CREATE TABLE qa_mrdoc_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    INSERT INTO qa_mrdoc_${release}_gold
    SELECT  'type_dockey_tally',key||'|'||key_qualifier,count(*)
    FROM mrd_properties
    WHERE key_qualifier not in ('MRSAT','MRCOLS','MRFILES','MEDLINE')
    AND key != 'rel_inverse'
    AND expiration_date is null
    GROUP BY key,key_qualifier;

    -- take in account of SUBX and DEL
    -- A new Empty relationship added 2006AB should be deleted change the count to 3 from 2
    INSERT INTO qa_mrdoc_${release}_gold
    SELECT 'type_dockey_tally', 'rel_inverse|REL', test_count - 2
    FROM qa_mrdoc_${release}_gold
    WHERE test_value = 'expanded_form|REL';

    INSERT INTO qa_mrdoc_${release}_gold
    SELECT 'type_dockey_tally', 'rela_inverse|RELA', test_count
    FROM qa_mrdoc_${release}_gold
    WHERE test_value = 'expanded_form|RELA';

    INSERT INTO qa_mrdoc_${release}_gold
    SELECT 'type_dockey_tally','snomedct_rel_mapping|REL', count(distinct code)
    FROM mrd_attributes
    WHERE attribute_name = 'UMLSREL'
    AND expiration_date is null;

    INSERT INTO qa_mrdoc_${release}_gold
    SELECT 'type_dockey_tally','snomedct_rela_mapping|RELA', test_count
    FROM qa_mrdoc_${release}_gold
    WHERE test_value = 'snomedct_rel_mapping|REL';

    INSERT INTO qa_mrdoc_${release}_gold
    SELECT 'row_cnt','', sum(test_count)
    FROM qa_mrdoc_${release}_gold
    WHERE test_name = 'type_dockey_tally';

    INSERT INTO qa_mrdoc_${release}_gold
    SELECT 'type_cnt','',
        count(distinct substr(test_value,1,instr(test_value,'|')-1))
    FROM qa_mrdoc_${release}_gold
    WHERE test_name = 'type_dockey_tally';

    INSERT INTO qa_mrdoc_${release}_gold
    SELECT 'dockey_cnt','',
        count(distinct substr(test_value,instr(test_value,'|')+1))
    FROM qa_mrdoc_${release}_gold
    WHERE test_name = 'type_dockey_tally';

    INSERT INTO qa_mrdoc_${release}_gold
    SELECT 'type_dockey_cnt','',count(distinct test_value)
    FROM qa_mrdoc_${release}_gold
    WHERE test_name = 'type_dockey_tally';



EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif


if ($mode == "all" || $mode == "mrdef") then
    echo "Generating MRDEF QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrdef_${release}_gold');
    CREATE TABLE qa_mrdef_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- Row Count.  Tricky because of stringtab
    -- manipulations.  First, get DEF data
    exec meme_utility.drop_it('table','qa_mrdef_${release}_gold_data');
    CREATE TABLE qa_mrdef_${release}_gold_data (
       sab         VARCHAR2(40),
       cui         VARCHAR2(10),
       aui         VARCHAR2(10),
       def         VARCHAR2(3600),
       suppressible VARCHAR2(10),
       hashcode    VARCHAR2(200));

    INSERT INTO qa_mrdef_${release}_gold_data
    SELECT /*+ PARALLEL(a) */ root_source, cui, ui, attribute_value, suppressible, null
    FROM mrd_attributes a
    WHERE expiration_date IS NULL
      AND attribute_name='DEFINITION';

    COMMIT;

    UPDATE qa_mrdef_${release}_gold_data a
    SET hashcode =
      substr(def,20)
    WHERE def like '<>Long_Attribute<>:%';

    -- Get 2 levels of stringtab data, assume anything
    -- longer than that can't be duplicated
    UPDATE qa_mrdef_${release}_gold_data a SET def =
      (SELECT text_value
       FROM mrd_stringtab b WHERE row_sequence=1
         AND a.hashcode = b.hashcode)
    WHERE hashcode is not null

    UPDATE qa_mrdef_${release}_gold_data a SET def =
      (SELECT def || text_value
       FROM mrd_stringtab b WHERE row_sequence=2
         AND a.hashcode = b.hashcode)
    WHERE hashcode is not null
      AND length(def)=1786;

    -- Row Count
    INSERT INTO qa_mrdef_${release}_gold
    SELECT 'row_cnt','',count(*)
    FROM qa_mrdef_${release}_gold_data;

    -- CUI Count
    INSERT INTO qa_mrdef_${release}_gold
    SELECT 'cui_cnt','',count(distinct cui)
    FROM qa_mrdef_${release}_gold_data;

    -- AUI Count
    INSERT INTO qa_mrdef_${release}_gold
    SELECT 'aui_cnt','',count(distinct aui)
    FROM qa_mrdef_${release}_gold_data;

    -- SAB Count
    INSERT INTO qa_mrdef_${release}_gold
    SELECT 'sab_tally',sab,count(*)
    FROM qa_mrdef_${release}_gold_data
    GROUP BY sab;

    -- Min Length
    INSERT INTO qa_mrdef_${release}_gold
    SELECT 'min_length', '',min(length(attribute_value))
    FROM mrd_attributes a
    WHERE attribute_name='DEFINITION'
      AND attribute_value not like '<>Long_Attribute<>%'
      AND expiration_date IS NULL;

    -- Max Length
    INSERT INTO qa_mrdef_${release}_gold
    SELECT 'max_length', '',max(text_total)
    FROM mrd_stringtab a, qa_mrdef_${release}_gold_data b
    WHERE a.hashcode = b.hashcode;

    exec meme_utility.drop_it('table','qa_mrdef_${release}_gold_data');

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrlo") then
    echo "Generating MRLO QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrlo_${release}_gold');
    CREATE TABLE qa_mrlo_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- First get the MRLO data, then we'll compare it
    exec meme_utility.drop_it('table','qa_mrlo_${release}_gold_data');
    CREATE TABLE qa_mrlo_${release}_gold_data (
       cui         VARCHAR2(10),
       aui         VARCHAR2(10),
       sab         VARCHAR2(40),
       fr          NUMBER(12),
       un          VARCHAR2(20),
       sui         VARCHAR2(10),
       sna         VARCHAR2(1200),
       soui        VARCHAR2(100),
       cvf         VARCHAR2(10));

    -- Get MRLO attributes first (short)
    INSERT INTO qa_mrlo_${release}_gold_data
    SELECT /*+ parallel(a) */ cui,null, root_source,null,null,
        substr(attribute_value, 1, instr(attribute_value, '~') - 1),
        substr(attribute_value, instr(attribute_value, '~') + 1,
                             (instr(attribute_value, '~', 1, 2) -
                              instr(attribute_value, '~', 1)) -1 ),
        substr(attribute_value, instr(attribute_value, '~', -1) + 1),
        null
    FROM mrd_attributes a
    WHERE attribute_name='MRLO'
      AND attribute_value not like '<>Long_Attribute<>:%'
      AND expiration_date IS NULL;

    COMMIT;

    -- Get MRLO attributes first (long)
    INSERT INTO qa_mrlo_${release}_gold_data
    SELECT /*+ parallel(a) */ cui,null,root_source,null,null,
        substr(text_value, 1, instr(text_value, '~') - 1),
        substr(text_value, instr(text_value, '~') + 1,
                             (instr(text_value, '~', 1, 2) -
                              instr(text_value, '~', 1)) -1 ),
        substr(text_value, instr(text_value, '~', -1) + 1), null
    FROM mrd_attributes a, mrd_stringtab b
    WHERE attribute_name='MRLO'
      AND attribute_value like '<>Long_Attribute<>:%'
      AND b.hashcode = substr(attribute_value,20)
      AND a.expiration_date IS NULL
      AND b.expiration_date IS NULL;

    COMMIT;

    -- Get HDA/HPC attributes next
    INSERT INTO qa_mrlo_${release}_gold_data
    SELECT /*+ PARALLEL(a) */ cui,ui,b.source,null,null,sui,null,null,null
    FROM mrd_attributes a, mrd_source_rank b
    WHERE a.attribute_name IN ('HDA','HPC')
      AND a.attribute_name= b.root_source
      AND a.root_source = 'UMD'
      AND a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND is_current = 'Y';

    COMMIT;

    -- Get DXP, AIR, PDQ, QMR "atom" Locators
    INSERT INTO qa_mrlo_${release}_gold_data
    SELECT DISTINCT cui,aui,root_source,null,null,sui,null,null,null
    FROM mrd_classes
    WHERE expiration_date IS NULL
    AND (root_source = 'DXP' OR
         root_source = 'PDQ' OR
         root_source = 'QMR' OR
         root_source = 'AIR');

    -- The MED/MBD  get confused for the AA release
    -- because we use SYSDATE but the release year is the following
    -- year.  For the AA release, use -9 and -4 instead of -10 and -5.

    -- Get *CITATIONS (mbd)
    INSERT INTO qa_mrlo_${release}_gold_data
    SELECT /*+ parallel(a) */
           cui, heading_aui, 'MBD', count(*) as ct, '*CITATIONS',
           sui , null, null,null
    FROM mrd_coc_headings a, mrd_classes b
    WHERE publication_date >=
         to_date('01-jan-'||
            (to_char(sysdate,'YYYY')-decode(to_char(sysdate,'MM'),12,9,10)))
        AND publication_date <
           to_date('01-jan-'||
            (to_char(sysdate,'YYYY')-decode(to_char(sysdate,'MM'),12,4,5)))
        AND major_topic='Y'
        AND a.root_source='NLM-MED'
        AND aui = heading_aui
        AND a.expiration_date IS NULL
        AND b.expiration_date IS NULL
    GROUP BY cui, heading_aui, sui;

    COMMIT;

    -- Get *CITATIONS (med)
    INSERT INTO qa_mrlo_${release}_gold_data
    SELECT /*+ parallel(a) */
         cui, heading_aui, 'MED',
        count(*) as ct, '*CITATIONS', sui, null, null, null
    FROM mrd_coc_headings a, mrd_classes b
    WHERE publication_date >=
        to_date('01-jan-'||
          (to_char(sysdate,'YYYY')-decode(to_char(sysdate,'MM'),12,4,5)))
        AND major_topic='Y'
        AND a.root_source='NLM-MED'
        AND aui = heading_aui
        AND a.expiration_date IS NULL
        AND b.expiration_date IS NULL
    GROUP BY cui, sui, heading_aui;

    COMMIT;

    -- Fix cases where SUI is no longer valid (i.e. in mrd_classes)
    UPDATE qa_mrlo_${release}_gold_data a SET (sna,sui) =
      (SELECT string,null
       FROM string_ui b
       WHERE b.sui = a.sui)
    WHERE (cui,sui) IN
      (SELECT cui,sui FROM qa_mrlo_${release}_gold_data
       MINUS
       SELECT cui,sui FROM mrd_classes
       WHERE expiration_date IS NULL)
      AND sui IS NOT NULL;

    -- Fix cases where SNA matches a sui in mrd_classes;
    UPDATE qa_mrlo_${release}_gold_data a SET (sna,sui) =
      (SELECT null, b.sui
       FROM string_ui b
       WHERE string = sna
         AND language='ENG'
         AND string_pre = substr(sna,0,10))
    WHERE (sna,cui) IN
      (SELECT string,a.cui
       FROM mrd_classes a, string_ui b
       WHERE a.sui=b.sui
         AND a.language='ENG'
         AND expiration_date IS NULL)
      AND sna IS NOT NULL;

    -- Get rid of cases where SUI/SNA redundant
    DELETE FROM qa_mrlo_${release}_gold_data a WHERE upper(sna) IN
    (SELECT upper(string)
     FROM string_ui b, qa_mrlo_${release}_gold_data c
     WHERE b.sui = c.sui and a.cui = c.cui
      AND language = 'ENG'
      AND a.sab = c.sab)
      AND sna IS NOT NULL;

    -- Get rid of cases where cui,sui is not in MRCONSO anymore
    -- These are SUI values that could not be mapped to SNAs
    DELETE FROM qa_mrlo_${release}_gold_data a
    WHERE sui IS NOT NULL
      AND (cui,sui) IN (SELECT cui,sui FROM qa_mrlo_${release}_gold_data
                        MINUS SELECT cui,sui FROM mrd_classes);

    -- Make sna all uppercase for comparison sake
    -- This is instead of removing duplicate
    -- case-insensitive SNA rows
    UPDATE qa_mrlo_${release}_gold_data set sna=upper(sna)
    WHERE sna IS NOT NULL;

    -- Now we are ready to go!

    -- Row Count
    INSERT INTO qa_mrlo_${release}_gold
    SELECT 'row_cnt','',count(*)
    FROM (SELECT DISTINCT * FROM qa_mrlo_${release}_gold_data);

    -- Distinct CUI Count
    INSERT INTO qa_mrlo_${release}_gold
    SELECT 'cui_cnt','',count(distinct cui)
    FROM (SELECT DISTINCT * FROM qa_mrlo_${release}_gold_data);

    -- Distinct AUI Count
    INSERT INTO qa_mrlo_${release}_gold
    SELECT 'aui_cnt','',count(distinct aui)+1
    FROM (SELECT DISTINCT * FROM qa_mrlo_${release}_gold_data);

    -- Distinct SNA Count
    INSERT INTO qa_mrlo_${release}_gold
    SELECT 'sna_cnt','',count(distinct nvl(sna,'null'))+1
    FROM (SELECT DISTINCT * FROM qa_mrlo_${release}_gold_data);

    -- Distinct SOUI Count
    INSERT INTO qa_mrlo_${release}_gold
    SELECT 'soui_cnt','',count(distinct nvl(soui,'null'))
    FROM (SELECT DISTINCT * FROM qa_mrlo_${release}_gold_data);

    -- Count by ISN
    INSERT INTO qa_mrlo_${release}_gold
    SELECT 'isn_tally',sab,count(*)
    FROM (SELECT DISTINCT * FROM qa_mrlo_${release}_gold_data)
    GROUP BY sab;

    -- Count by ISN,UN
    INSERT INTO qa_mrlo_${release}_gold
    SELECT 'isn_un_tally',sab||'|'||un,count(*)
    FROM (SELECT DISTINCT * FROM qa_mrlo_${release}_gold_data)
    GROUP BY sab,un;

    exec meme_utility.drop_it('table','qa_mrlo_${release}_gold_data');

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrrank") then
    echo "Generating MRRANK QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrrank_${release}_gold');
    CREATE TABLE qa_mrrank_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- Row Count
    INSERT INTO qa_mrrank_${release}_gold
    SELECT /*+ parallel(c) */ 'row_cnt','',count(distinct root_source||tty)
    FROM mrd_classes c WHERE expiration_date IS NULL;

    COMMIT;

    -- SUPR Count
    INSERT INTO qa_mrrank_${release}_gold
    SELECT 'suppress_tally',suppressible,count(*)
    FROM mrd_termgroup_rank a, mrd_source_rank b
    WHERE a.expiration_date IS NULL
     AND b.expiration_date IS NULL
     AND is_current='Y'
     AND substr(normalized_termgroup, 1,
            instr(normalized_termgroup,'/')-1) = b.source
     AND (root_source,tty) IN
        (SELECT DISTINCT root_source,tty FROM mrd_classes
            WHERE expiration_date IS NULL)
        GROUP BY suppressible;

    -- SAB Count
    INSERT INTO qa_mrrank_${release}_gold
    SELECT name, decode(root_source,'NLM02','RXNORM',root_source),ct FROM
      (SELECT /*+ parallel(c) */ 'sab_tally' as name,
            root_source,count(distinct root_source||tty) as ct
       FROM mrd_classes c WHERE expiration_date IS NULL
       GROUP BY root_source);

    COMMIT;

    -- TTY Count
    INSERT INTO qa_mrrank_${release}_gold
    SELECT /*+ parallel(c) */ 'tty_tally',tty,count(distinct root_source||tty)
    FROM mrd_classes c WHERE expiration_date IS NULL
    GROUP BY tty;

    COMMIT;

    -- SAB,TTY Count
    INSERT INTO qa_mrrank_${release}_gold
    SELECT /*+ parallel(c) */ 'sab_tty_cnt','',count(distinct root_source||tty)
    FROM mrd_classes c WHERE expiration_date IS NULL;

    COMMIT;

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrrel") then
    echo "Generating MRREL QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrrel_${release}_gold');
    CREATE TABLE qa_mrrel_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- We have Regular rels and context rels
    -- Create projection of data
    -- with SAB,SL,REL,RELA, count(*)
    exec meme_utility.drop_it('table','qa_mrrel_${release}_gold_data');
    CREATE TABLE qa_mrrel_${release}_gold_data (
       rui         VARCHAR2(20),
       sab         VARCHAR2(40),
       sl          VARCHAR2(40),
       rel         VARCHAR2(20),
       rela        VARCHAR2(100),
       stype_1       VARCHAR2(10),
       stype_2       VARCHAR2(10),
       cui_1       VARCHAR2(10),
       cui_2       VARCHAR2(10),
       aui_1       VARCHAR2(10),
       aui_2       VARCHAR2(10),
       suppressible VARCHAR2(10));

    -- mrd rels
    INSERT INTO qa_mrrel_${release}_gold_data
        (rui,sab,sl,rel,rela,stype_1,stype_2,cui_1,cui_2,aui_1,aui_2,suppressible)
    SELECT /*+ parallel(r) */ DISTINCT rui, root_source, root_source_of_label,
           relationship_name, relationship_attribute,
           sg_type_1, sg_type_2, cui_1, cui_2,aui_1, aui_2, suppressible
    FROM mrd_relationships r
    WHERE expiration_date IS NULL
      AND relationship_name not like 'X%'
      AND relationship_name NOT IN ('BBT','BNT','BRT');

    COMMIT;

    -- XR rels
    INSERT INTO qa_mrrel_${release}_gold_data
        (rui,sab,sl,rel,rela,stype_1,stype_2,cui_1,cui_2,aui_1,aui_2, suppressible)
    SELECT /*+ parallel(r) */ DISTINCT rui, root_source, root_source_of_label,
           relationship_name, relationship_attribute,
           sg_type_1, sg_type_2, cui_1, cui_2,aui_1, aui_2, suppressible
    FROM mrd_relationships r
    WHERE expiration_date IS NULL
      AND relationship_name = 'XR' AND relationship_level = 'S';

    COMMIT;

    -- AQ relationships
    exec MEME_UTILITY.drop_it('table','t_aq_rels');
    CREATE TABLE t_aq_rels AS SELECT * FROM mrd_attributes WHERE 1=0;
    INSERT INTO t_aq_rels SELECT * FROM mrd_attributes
    WHERE attribute_name in ('ATN','AQL')
      AND expiration_date IS NULL
      AND root_source = 'MSH';

    exec MEME_UTILITY.drop_it('table','t_aq_${release}_gold_data');
    CREATE TABLE t_aq_${release}_gold_data AS
    SELECT * FROM qa_mrrel_${release}_gold_data WHERE 1=0;

    INSERT INTO t_aq_${release}_gold_data
       (sab,sl,rel,rela,cui_1,cui_2,aui_1,aui_2, suppressible)
    SELECT a.root_source, a.root_source, 'AQ', '', a.cui,
        substr(attribute_value,0,2),
        a.ui, substr(attribute_value,0,2), suppressible
    FROM t_aq_rels a WHERE attribute_value not like '<>Long%';

    INSERT INTO t_aq_${release}_gold_data
      (sab,sl,rel,rela,cui_1,cui_2,aui_1,aui_2, suppressible)
    SELECT /*+ RULE */ a.root_source, a.root_source, 'AQ', '',
      a.cui, substr(text_value,0,2), a.ui, substr(text_value,0,2), suppressible
    FROM t_aq_rels a, mrd_stringtab b
    WHERE substr(attribute_value,20)=b.hashcode
      AND attribute_value like '<>Long%';

    DECLARE
      ct   INTEGER;
    BEGIN
        ct := 0;
        LOOP
            ct := ct + 1;
            INSERT INTO t_aq_${release}_gold_data
              (sab,sl,rel,rela,cui_1,cui_2,aui_1,aui_2, suppressible)
            SELECT a.root_source, a.root_source, 'AQ', '', a.cui,
                substr(attribute_value,INSTR(attribute_value,' ',1,ct)+1,2),
                a.ui, substr(attribute_value,INSTR(attribute_value,' ',1,ct)+1,2), suppressible
            FROM t_aq_rels a
            WHERE attribute_value not like '<>Long_Attribute<>:%'
              AND INSTR(attribute_value,' ',1,ct) != 0;

            INSERT INTO t_aq_${release}_gold_data
              (sab,sl,rel,rela,cui_1,cui_2,aui_1,aui_2, suppressible)
            SELECT a.root_source, a.root_source, 'AQ', '', a.cui,
                substr(text_value,INSTR(text_value,' ',1,ct)+1,2),
                a.ui, substr(text_value,INSTR(text_value,' ',1,ct)+1,2), suppressible
            FROM t_aq_rels a, mrd_stringtab b
            WHERE substr(a.attribute_value,20) = b.hashcode
              AND b.expiration_date IS NULL
              AND attribute_value like '<>Long_Attribute<>:%'
              AND INSTR(text_value,' ',1,ct) != 0;
            EXIT WHEN SQL%ROWCOUNT = 0;

        END LOOP;
    END;
/
    ANALYZE TABLE t_aq_${release}_gold_data COMPUTE STATISTICS;

    -- Create table of CUI,QA
    exec meme_utility.drop_it('table','t_qa_cui');
    CREATE TABLE t_qa_cui AS
    SELECT a.cui, a.aui ui, b.string as qa
    FROM mrd_classes a, string_ui b
    WHERE expiration_date IS NULL
      AND a.tty='QAB'
      AND a.sui = b.sui
      AND a.root_source = 'MSH';
 
    -- Add AQ relationships
    INSERT INTO qa_mrrel_${release}_gold_data
      (sab,sl,rel,rela,stype_1,stype_2,cui_1,cui_2,aui_1,aui_2, suppressible)
    SELECT DISTINCT sab, sl, 'AQ', '', 'SDUI','SDUI',cui_1, b.cui, aui_1, b.ui, a.suppressible
    FROM t_aq_${release}_gold_data a, t_qa_cui b
    WHERE cui_2=qa;

    -- Add QB relationships
    INSERT INTO qa_mrrel_${release}_gold_data
      (sab,sl,rel,rela,stype_1,stype_2,cui_1,cui_2,aui_1,aui_2,suppressible )
    SELECT DISTINCT sab, sl, 'QB', '', 'SDUI','SDUI', b.cui, cui_1, b.ui, aui_1, a.suppressible
    FROM t_aq_${release}_gold_data a, t_qa_cui b
    WHERE cui_2=qa;

    DROP TABLE t_qa_cui;
    DROP TABLE t_aq_${release}_gold_data;
    DROP TABLE t_aq_rels;

    -- Fix rel names (no longer needed)
    --UPDATE qa_mrrel_${release}_gold_data
    --SET rel =
    --  DECODE(rel,'BT','RB','NT','RN','RT','RO','RT?','RQ','LK','RL',rel)
    --WHERE rel !=
    --  DECODE(rel,'BT','RB','NT','RN','RT','RO','RT?','RQ','LK','RL',rel);

    -- Now we are READY

    -- Row Count (also AQ,QB, etc)
    INSERT into qa_mrrel_${release}_gold
    SELECT 'row_cnt','', count(*) FROM qa_mrrel_${release}_gold_data;

    -- CUI1 count
    INSERT into qa_mrrel_${release}_gold
    SELECT 'cui1_cnt','',count(distinct cui_1)
    FROM qa_mrrel_${release}_gold_data;

    -- CUI2 count
    INSERT into qa_mrrel_${release}_gold
    SELECT 'cui2_cnt','',test_count
    FROM qa_mrrel_${release}_gold WHERE test_name='cui1_cnt';

    -- AUI1 count
    INSERT into qa_mrrel_${release}_gold
    SELECT 'aui1_cnt','',count(distinct aui_1)+1
    FROM qa_mrrel_${release}_gold_data;

    -- AUI2 count
    INSERT into qa_mrrel_${release}_gold
    SELECT 'aui2_cnt','',test_count
    FROM qa_mrrel_${release}_gold WHERE test_name='aui1_cnt';

    -- CUI1, CUI2 count
    INSERT into qa_mrrel_${release}_gold
    SELECT 'cui1_cui2_cnt','',count(distinct cui_1||cui_2)
    FROM qa_mrrel_${release}_gold_data;

    -- CUI1, CUI2, AUI1, AUI2 count
    INSERT into qa_mrrel_${release}_gold
    SELECT 'c1_a1_c2_a2_cnt','',count(distinct aui_1||aui_2)
    FROM qa_mrrel_${release}_gold_data;

    -- by SAB,DIR where DIR not null
    INSERT into qa_mrrel_${release}_gold
    SELECT 'sab_dir_tally',
       root_source||'|'||rel_directionality_flag,count(*)
    FROM mrd_relationships
    WHERE rel_directionality_flag is not null
      AND expiration_date is null
    GROUP BY root_source,rel_directionality_flag;

    -- by REL count
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'rel_tally',rel,count(*)
    FROM qa_mrrel_${release}_gold_data GROUP BY rel;

    -- by RELA count
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'rela_tally',rela,count(*)
    FROM qa_mrrel_${release}_gold_data
    WHERE rela is not null GROUP BY rela;

    -- by SAB count
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'sab_tally',sab,count(*)
    FROM qa_mrrel_${release}_gold_data GROUP BY sab;

    -- STYPE1, STYPE2, SL count
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 't1_t2_sab_tally',stype_1||'|'||stype_2||'|'||sab,count(*)
    FROM qa_mrrel_${release}_gold_data GROUP BY stype_1,stype_2,sab;

    -- CUI Selfref count
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'cui_selfref_cnt','',count(*)
    FROM qa_mrrel_${release}_gold_data WHERE cui_1=cui_2;

    -- AUI Selfref count
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'aui_selfref_cnt','',count(*)
    FROM qa_mrrel_${release}_gold_data WHERE aui_1=aui_2;

    -- Selfref by RELA, REL, SAB, SL count
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'rrss_selfref_tally',rel||'|'||rela||'|'||sab||'|'||sl,count(*)
    FROM qa_mrrel_${release}_gold_data
    WHERE cui_1 = cui_2
    GROUP BY rel,rela,sab,sl;
    
    -- SAB,REL,RELA,STYPE1,STYPE2 tally
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 's_r_r_t1_t2_tally',sab||'|'||rel||'|'||rela||'|'||stype_1||'|'||stype_2,count(*)
    FROM qa_mrrel_${release}_gold_data GROUP BY sab,rel,rela,stype_1,stype_2;

    -- SAB (with RG) tally
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'sab_rg_tally',root_source,count(distinct rui)
    FROM mrd_relationships r
    WHERE relationship_group IS NOT NULL
      AND expiration_date IS NULL
    GROUP BY root_source;

    -- SAB (with SRUI) tally
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'sab_srui_tally',root_source,count(*)
    FROM mrd_relationships r
    WHERE source_rui IS NOT NULL
      AND expiration_date IS NULL
    GROUP BY root_source;
    
    -- SAB,REL,RELA,SUPPRESSIBLE tally
    INSERT INTO qa_mrrel_${release}_gold
    SELECT 'suppr_rel_rela_tally',sab||'|'||rel||'|'||rela||'|'||suppressible,count(*)
    FROM qa_mrrel_${release}_gold_data where suppressible !='N' GROUP BY sab,rel,rela,suppressible;    

    exec meme_utility.drop_it('table','qa_mrrel_${release}_gold_data');

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrsab") then
    echo "Generating MRSAB QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrsab_${release}_gold');
    exec meme_utility.drop_it('table','qa_mrsab_${release}_gold_pre');

    CREATE TABLE qa_mrsab_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    CREATE TABLE qa_mrsab_${release}_gold_pre as
    SELECT count(distinct source) as ct FROM mrd_source_rank a, mrd_attributes b
    WHERE a.expiration_date IS NULL AND is_current = 'N'
         AND source = normalized_source
         AND a.source = b.attribute_value
         AND b.attribute_name in ('TOVSAB','FROMVSAB')
         AND b.expiration_date IS NULL;

    INSERT INTO qa_mrsab_${release}_gold
    SELECT 'row_cnt','',ct + (select ct from qa_mrsab_${release}_gold_pre)
    FROM (SELECT count(distinct source) ct FROM mrd_source_rank
    WHERE expiration_date IS NULL AND is_current = 'Y'
    AND source = normalized_source);

    INSERT INTO qa_mrsab_${release}_gold
    SELECT 'vsab_cnt','',ct + (select ct from qa_mrsab_${release}_gold_pre)
    FROM (SELECT count(distinct source) ct FROM mrd_source_rank
    WHERE expiration_date IS NULL AND is_current = 'Y'
    AND source = normalized_source);

    INSERT INTO qa_mrsab_${release}_gold
    SELECT 'rsab_cnt','',count(distinct root_source)
    FROM mrd_source_rank
    WHERE expiration_date IS NULL AND is_current = 'Y'
    AND source = normalized_source;

    INSERT INTO qa_mrsab_${release}_gold
    SELECT 'sf_cnt','',count(distinct source_family)
    FROM mrd_source_rank
    WHERE expiration_date IS NULL AND is_current = 'Y'
    AND source = normalized_source;

    INSERT INTO qa_mrsab_${release}_gold
    SELECT 'lat_cnt','',count(distinct nvl(language,0))
    FROM mrd_source_rank
    WHERE expiration_date IS NULL AND is_current = 'Y'
    AND source = normalized_source;

    INSERT INTO qa_mrsab_${release}_gold
    SELECT 'lat_tally',language,count(*)
    FROM mrd_source_rank
    WHERE expiration_date IS NULL AND is_current = 'Y'
    AND source = normalized_source
    GROUP BY language;

    INSERT INTO qa_mrsab_${release}_gold
    SELECT 'cxty_tally',context_type,count(*)
    FROM mrd_source_rank
    WHERE expiration_date IS NULL AND is_current = 'Y'
    AND source = normalized_source
    GROUP BY context_type;

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrsat") then
    echo "Generating MRSAT QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrsat_${release}_gold');
    CREATE TABLE qa_mrsat_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- Get all S level attributes
    -- But exclude rows that go into MRDEF, MRMAP, and MRLO
    exec meme_utility.drop_it('table','qa_mrsat_${release}_gold_data');
    CREATE TABLE qa_mrsat_${release}_gold_data AS
        SELECT /*+ PARALLEL(a) */ DISTINCT cui, lui, sui, ui, code, sg_type,
            attribute_name, root_source, attribute_value, suppressible, source_atui
        FROM mrd_attributes a
        WHERE expiration_date IS NULL
          AND attribute_level = 'S'
          AND attribute_name not IN
            ('XMAP','XMAPTO','XMAPFROM','NON_HUMAN',
             'DEFINITION','ATX_REL','MRLO','HDA',
             'COMPONENTHISTORY','HPC','COC','LEXICAL_TAG');

    COMMIT;

    INSERT INTO qa_mrsat_${release}_gold_data
        (cui, lui, sui, ui, code,sg_type, attribute_name,
         root_source, attribute_value, suppressible)
        SELECT /*+ PARALLEL(a) */ DISTINCT cui, lui, sui, ui, code, sg_type,
            attribute_name, root_source, attribute_value, suppressible
        FROM mrd_attributes a
        WHERE expiration_date IS NULL
          AND attribute_level = 'C'
          AND attribute_name not IN
            ('DEFINITION','ATX_REL','MRLO','HDA',
             'COMPONENTHISTORY','HPC','COC','LEXICAL_TAG',
             'SEMANTIC_TYPE','XMAP','XMAPTO','XMAPFROM','NON_HUMAN');

    COMMIT;

    -- Get the lexical tag attributes
    -- Soma Changing to include a.root_source != MSH to a.root_source = 'MTH'
    INSERT INTO qa_mrsat_${release}_gold_data
        (cui, lui, sui, ui, code, attribute_name,
         root_source, attribute_value, suppressible, sg_type)
    SELECT DISTINCT a.cui, b.lui, b.sui, b.aui, b.code, 'LT', 'MTH','TRD','N','AUI'
    FROM mrd_attributes a, mrd_classes b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND attribute_level = 'S'
      AND attribute_name = 'LEXICAL_TAG'
      AND attribute_value = 'TRD'
      AND a.sui = b.sui
      AND a.cui = b.cui;

    INSERT INTO qa_mrsat_${release}_gold_data
        (cui, lui, sui, ui, code, attribute_name,
         root_source, attribute_value, suppressible,sg_type)
    SELECT DISTINCT cui, lui, sui, ui, code, 'LT', root_source,'TRD','N','AUI'
    FROM mrd_attributes
    WHERE expiration_date IS NULL
      AND attribute_level = 'S'
      AND attribute_name = 'LEXICAL_TAG'
      AND attribute_value = 'TRD';

        -- Row Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'row_cnt','',count(*)
    FROM qa_mrsat_${release}_gold_data;

    -- Distinct CUI Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'cui_cnt','',count(distinct cui)
    FROM qa_mrsat_${release}_gold_data;

    -- Distinct SUI Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'sui_cnt','',count(distinct sui)
    FROM qa_mrsat_${release}_gold_data;

    -- Distinct UI Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'metaui_cnt','',count(distinct ui)
    FROM qa_mrsat_${release}_gold_data;

    -- Distinct CUI|LUI|SUI Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'cls_cnt','',count(distinct sui||lui||cui)
    FROM qa_mrsat_${release}_gold_data WHERE sui is not null;

    -- Distinct CUI|UI Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'cm_cnt','',count(distinct cui||ui)
    FROM qa_mrsat_${release}_gold_data WHERE ui is not null;

    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'sab_satui_tally',root_source,count(distinct source_atui)
    FROM qa_mrsat_${release}_gold_data
    WHERE source_atui IS NOT NULL
    group by root_source;

    -- ATN Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'atn_tally',attribute_name,count(*)
    FROM qa_mrsat_${release}_gold_data
    GROUP BY attribute_name;
    
    -- SAB ATN SUPP Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'suppr_atn_tally', root_source||'|'||attribute_name||'|'||suppressible, count(*) from
    qa_mrsat_${release}_gold_data where suppressible != 'N'
    GROUP by root_source,attribute_name,suppressible;

    -- SAB Count
    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'sab_tally',root_source,count(*)
    FROM qa_mrsat_${release}_gold_data
    GROUP BY root_source;

    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'stype_sab_tally',sg_type||'|'||root_source,count(*)
    FROM qa_mrsat_${release}_gold_data
    GROUP BY sg_type,root_source;

    INSERT INTO qa_mrsat_${release}_gold
    SELECT 'sab_atn_stype_tally',root_source||'|'||attribute_name||'|'||sg_type,count(*)
    FROM qa_mrsat_${release}_gold_data
    GROUP BY root_source,attribute_name,sg_type;

    exec meme_utility.drop_it('table','qa_mrsat_${release}_gold_data');

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrsty") then
    echo "Generating MRSTY QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrsty_${release}_gold');
    CREATE TABLE qa_mrsty_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- Row Count
    INSERT into qa_mrsty_${release}_gold
    SELECT /*+ PARALLEL(a) */
       'row_cnt','', count(distinct cui||a.ui||attribute_value)
    FROM mrd_attributes a, srdef b
    WHERE attribute_value = sty_rl
    AND attribute_name = 'SEMANTIC_TYPE'
    AND expiration_date IS NULL;

    COMMIT;

    -- STY Count
    INSERT into qa_mrsty_${release}_gold
    SELECT /*+ PARALLEL(a) */  'sty_cnt','', count(distinct attribute_value)
    FROM mrd_attributes a, srdef
    WHERE attribute_value = sty_rl
    AND attribute_name = 'SEMANTIC_TYPE'
    AND expiration_date IS NULL;

    COMMIT;

    -- TUI Count
    INSERT into qa_mrsty_${release}_gold
    SELECT /*+ PARALLEL(a) */ 'tui_cnt','', count(distinct b.ui)
    FROM mrd_attributes a, srdef b
    WHERE attribute_value = sty_rl
    AND attribute_name = 'SEMANTIC_TYPE'
    AND expiration_date IS NULL;

    COMMIT;

    -- STN Count
    INSERT into qa_mrsty_${release}_gold
    SELECT /*+ PARALLEL(a) */ 'stn_cnt','', count(distinct stn_rtn)
    FROM mrd_attributes a, srdef
    WHERE attribute_value = sty_rl
    AND attribute_name = 'SEMANTIC_TYPE'
    AND expiration_date IS NULL;

    COMMIT;

    -- STY tally
    INSERT into qa_mrsty_${release}_gold
    SELECT /*+ PARALLEL(a) */  'sty_tally',attribute_value, count(distinct cui)
    FROM mrd_attributes a, srdef
    WHERE attribute_value = sty_rl
    AND attribute_name = 'SEMANTIC_TYPE'
    AND expiration_date IS NULL
        GROUP BY attribute_value;

    COMMIT;

    -- TUI tally
    INSERT into qa_mrsty_${release}_gold
    SELECT /*+ PARALLEL(a) */ 'tui_tally',b.ui, count(distinct cui)
    FROM mrd_attributes a, srdef b
    WHERE attribute_value = sty_rl
    AND attribute_name = 'SEMANTIC_TYPE'
    AND expiration_date IS NULL
    GROUP BY b.ui;

    COMMIT;

    -- STN tally
    INSERT into qa_mrsty_${release}_gold
    SELECT /*+ PARALLEL(a) */ 'stn_tally',stn_rtn, count(distinct cui)
    FROM mrd_attributes a, srdef
    WHERE attribute_value = sty_rl
    AND attribute_name = 'SEMANTIC_TYPE'
    AND expiration_date IS NULL
        GROUP BY stn_rtn;

    COMMIT;

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrx") then
    echo "Generating MRXNS, MRXNW, MRXW.<lat> QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrx_${release}_gold');
    CREATE TABLE qa_mrx_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    INSERT INTO qa_mrx_${release}_gold
    SELECT 'cls_file_cnt','MRXNS_ENG',count(distinct cui||sui)
    FROM mrd_classes WHERE language='ENG'
    AND expiration_date IS NULL
    AND lui != (SELECT min(lui) FROM string_ui
       WHERE language='ENG' AND norm_string is null);

    INSERT INTO qa_mrx_${release}_gold
    SELECT 'cls_file_cnt','MRXNW_ENG',test_count
    FROM qa_mrx_${release}_gold;

-- 1-748XJ - MRXW_XXX files should ignore the some of the strings attached to null LUI (e.g. L0028429).  -- Soma Lanka
    INSERT INTO qa_mrx_${release}_gold
    SELECT 'cls_file_cnt','MRXW_'||b.lat, count(distinct a.cui||a.sui)
 FROM mrd_classes a, language b, string_ui c
    WHERE a.language=b.lat
        AND a.sui = c.sui
        AND c.string not in ('''',';','=','<=','>=','+','++','+++','++++','<','>','%')
        and c.sui != 'S9297292'
      AND expiration_date IS NULL
      GROUP BY b.lat;

    UPDATE qa_mrx_${release}_gold
    SET test_count = test_count
    WHERE test_value = 'MRXW.ENG';

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "mrfilescols") then
    echo "Generating MRFILES/MRCOLS QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_mrfilescols_${release}_gold');
    CREATE TABLE qa_mrfilescols_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- BTS cnt
    INSERT INTO qa_mrfilescols_${release}_gold
    SELECT 'file_bts_cnt',file_name,byte_count
    FROM mrd_file_statistics
    WHERE expiration_date IS NULL;

    -- COL cnt
    INSERT INTO qa_mrfilescols_${release}_gold
    SELECT 'col_cnt','',count(*)
    FROM mrd_column_statistics
    WHERE expiration_date IS NULL;

    -- FILE cnt
    INSERT INTO qa_mrfilescols_${release}_gold
    SELECT 'file_cnt','',count(*)
    FROM mrd_file_statistics
    WHERE expiration_date IS NULL;

    -- ROW cnt
    INSERT INTO qa_mrfilescols_${release}_gold
    SELECT 'row_cnt','',count(*)
    FROM mrd_file_statistics
    WHERE expiration_date IS NULL;

    -- COL tally
    INSERT INTO qa_mrfilescols_${release}_gold
    SELECT 'col_tally',column_name,count(*)
    FROM mrd_column_statistics
    WHERE expiration_date IS NULL
    GROUP by column_name;

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif

if ($mode == "all" || $mode == "metamorphosys") then
    echo "Generating MetamorphoSys QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_metamorphosys_${release}_gold');
    CREATE TABLE qa_metamorphosys_${release}_gold AS
    SELECT * FROM qa_mrconso_${release}_gold;

    exec meme_utility.drop_it('table','qa_mmsys_${release}_gold_t1');
    CREATE TABLE qa_mmsys_${release}_gold_t1 (
       lat  VARCHAR2(100) NOT NULL,
       rank VARCHAR2(40),
       cui  VARCHAR2(10),
       aui  VARCHAR2(10),
       sui  VARCHAR2(10),
       lui  VARCHAR2(10));

    INSERT INTO qa_mmsys_${release}_gold_t1
    SELECT a.language,
       -- LPAD(c.rank,4,'0')||sui||LPAD(SUBSTR(aui,INSTR(aui,(SELECT value FROM code_map WHERE code --= 'AUI' AND type = 'ui_prefix'))+1),
          -- (SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_length'),'0') as rank,
           --cui, aui, sui, lui
          MEME_RANKS.get_atom_release_rank(c.rank, a.last_release_rank, a.sui, a.aui) as rank, cui, aui, sui, lui
      FROM mrd_classes a, mrd_source_rank b, mrd_termgroup_rank c
      WHERE a.root_source = b.root_source
        AND b.is_current = 'Y'
        AND b.source = substr(normalized_termgroup, 1, instr(normalized_termgroup,'/')-1)
        AND a.tty = c.tty
        AND a.expiration_date IS NULL
        AND b.expiration_date IS NULL
        AND c.expiration_date IS NULL;

    exec meme_utility.drop_it('table','pref_lui_for_cui_lat');
    CREATE TABLE pref_lui_for_cui_lat AS
    SELECT lui, cui, lat
    FROM qa_mmsys_${release}_gold_t1
    WHERE (rank) IN
    (SELECT max(rank)
     FROM qa_mmsys_${release}_gold_t1
     GROUP BY cui, lat);

    exec meme_utility.drop_it('table','pref_sui_for_cui_lat_lui');
    CREATE TABLE pref_sui_for_cui_lat_lui AS
    SELECT sui, cui, lat
    FROM qa_mmsys_${release}_gold_t1
    WHERE (rank) IN
    (SELECT max(rank)
     FROM qa_mmsys_${release}_gold_t1
     GROUP BY cui, lat, lui);

    exec meme_utility.drop_it('table','pref_aui_for_cui_lat_lui_sui');
    CREATE TABLE pref_aui_for_cui_lat_lui_sui AS
    SELECT aui, cui, lat
    FROM qa_mmsys_${release}_gold_t1
    WHERE (rank) IN
    (SELECT max(rank)
     FROM qa_mmsys_${release}_gold_t1
     GROUP BY cui, lat, lui, sui);

    exec meme_utility.drop_it('table','tmp_pref_cui_lat_aui');
    CREATE TABLE tmp_pref_cui_lat_aui AS
    SELECT cui,language,aui FROM mrd_classes WHERE expiration_date IS NULL
            MINUS
            SELECT cui,lat,aui FROM pref_aui_for_cui_lat_lui_sui;


    -- lat_ts_stt_ispref count
    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'lat_ts_stt_ispref_tally',
           lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    FROM
    (SELECT language lat, 'P' ts, 'PF' stt, 'Y' ispref
     FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,lat,lui FROM pref_lui_for_cui_lat)
       AND (cui,language,sui) IN
           (SELECT cui,lat,sui FROM pref_sui_for_cui_lat_lui)
       AND (cui,language,aui) IN
           (SELECT cui,lat,aui FROM pref_aui_for_cui_lat_lui_sui)
    ) GROUP BY lat, ts, stt, ispref;

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'lat_ts_stt_ispref_tally',
           lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    FROM
    (SELECT language lat, 'P' ts, 'PF' stt, 'N' ispref
     FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,lat,lui FROM pref_lui_for_cui_lat)
       AND (cui,language,sui) IN
           (SELECT cui,lat,sui FROM pref_sui_for_cui_lat_lui)
       AND (cui,language,aui) IN
            (SELECT cui,language,aui from tmp_pref_cui_lat_aui)
    ) GROUP BY lat, ts, stt, ispref;
    
    exec meme_utility.drop_it('table','tmp_pref_lui_for_cui_lat');
    
    CREATE TABLE tmp_pref_lui_for_cui_lat AS
    SELECT cui,language,lui FROM mrd_classes WHERE expiration_date IS NULL
            MINUS
            SELECT cui,lat,lui FROM pref_lui_for_cui_lat;


    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'lat_ts_stt_ispref_tally',
           lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    FROM
    (SELECT language lat, 'S' ts, 'PF' stt, 'Y' ispref
     FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,language,lui FROM tmp_pref_lui_for_cui_lat)
       AND (cui,language,sui) IN
           (SELECT cui,lat,sui FROM pref_sui_for_cui_lat_lui)
       AND (cui,language,aui) IN
           (SELECT cui,lat,aui FROM pref_aui_for_cui_lat_lui_sui)
    ) GROUP BY lat, ts, stt, ispref;

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'lat_ts_stt_ispref_tally',
           lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    FROM
    (SELECT language lat, 'S' ts, 'PF' stt, 'N' ispref
     FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,language,lui FROM tmp_pref_lui_for_cui_lat)
       AND (cui,language,sui) IN
           (SELECT cui,lat,sui FROM pref_sui_for_cui_lat_lui)
       AND (cui,language,aui) IN
           (SELECT cui,language,aui FROM tmp_pref_cui_lat_aui)
    ) GROUP BY lat, ts, stt, ispref;

    exec meme_utility.drop_it('table','tmp_pref_sui_for_cui_lat_lui');
    
    CREATE TABLE tmp_pref_sui_for_cui_lat_lui AS
    SELECT cui,language,sui FROM mrd_classes WHERE expiration_date IS NULL
            MINUS
            SELECT cui,lat,sui FROM pref_sui_for_cui_lat_lui;

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'lat_ts_stt_ispref_tally',
           lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    FROM
    (SELECT language lat, 'P' ts, 'V' stt, 'Y' ispref
     FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,lat,lui FROM pref_lui_for_cui_lat)
       AND (cui,language,sui) IN
           (SELECT cui,language,sui FROM tmp_pref_sui_for_cui_lat_lui)
       AND (cui,language,aui) IN
           (SELECT cui,lat,aui FROM pref_aui_for_cui_lat_lui_sui)
    ) GROUP BY lat, ts, stt, ispref;

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'lat_ts_stt_ispref_tally',
           lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    FROM
    (SELECT language lat, 'P' ts, 'V' stt, 'N' ispref
     FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,lat,lui FROM pref_lui_for_cui_lat)
       AND (cui,language,sui) IN
           (SELECT cui,language,sui FROM tmp_pref_sui_for_cui_lat_lui)
       AND (cui,language,aui) IN
           (SELECT cui,language,aui FROM tmp_pref_cui_lat_aui)
    ) GROUP BY lat, ts, stt, ispref;

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'lat_ts_stt_ispref_tally',
           lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    FROM
    (SELECT language lat, 'S' ts, 'V' stt, 'Y' ispref
     FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,language,lui FROM tmp_pref_lui_for_cui_lat)
       AND (cui,language,sui) IN
           (SELECT cui,language,sui FROM tmp_pref_sui_for_cui_lat_lui)
       AND (cui,language,aui) IN
           (SELECT cui,lat,aui FROM pref_aui_for_cui_lat_lui_sui)
    ) GROUP BY lat, ts, stt, ispref;

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'lat_ts_stt_ispref_tally',
           lat||'|'||ts||'|'||stt||'|'||ispref,count(*)
    FROM
    (SELECT language lat, 'S' ts, 'V' stt, 'N' ispref
     FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,language,lui FROM tmp_pref_lui_for_cui_lat)
       AND (cui,language,sui) IN
           (SELECT cui,language,sui FROM tmp_pref_sui_for_cui_lat_lui)
       AND (cui,language,aui) IN
           (SELECT cui,language,aui FROM tmp_pref_cui_lat_aui)
    ) GROUP BY lat, ts, stt, ispref;

    -- ts count
    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'ts_tally','P',count(*)
    FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,lat,lui FROM pref_lui_for_cui_lat);

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'ts_tally','S',count(*)
    FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,lui) IN
           (SELECT cui,language,lui FROM tmp_pref_lui_for_cui_lat);

    -- stt count
    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'stt_tally','PF',count(*)
    FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,sui) IN
           (SELECT cui,lat,sui FROM pref_sui_for_cui_lat_lui);

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'stt_tally','V',count(*)
    FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,sui) IN
           (SELECT cui,language,sui FROM tmp_pref_sui_for_cui_lat_lui);

    -- ispref count
    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'ispref_tally','Y',count(*)
    FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,aui) IN
           (SELECT cui,lat,aui FROM pref_aui_for_cui_lat_lui_sui);

    INSERT INTO qa_metamorphosys_${release}_gold
    SELECT 'ispref_tally','N',count(*)
    FROM mrd_classes
     WHERE expiration_date IS NULL
       AND (cui,language,aui) IN
           (SELECT cui,language,aui FROM tmp_pref_cui_lat_aui);

    DROP TABLE qa_mmsys_${release}_gold_t1;

EOF
    if ($status != 0) then
        cat /tmp/sql.$$.log
        echo "ERROR: SQL Error"
        exit 0
    endif
endif


if ($mode == "all" || $mode == "orf") then
    echo "Generating ORF QA Counts ... `/bin/date`"

    echo "Gold script for ORF process has been deprecated."

endif

if ($mode == "all" || $mode == "doc") then
    echo "Generating DOC QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_doc_${release}_gold');
    CREATE TABLE qa_doc_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- file_cnt
    INSERT INTO qa_doc_${release}_gold (test_name, test_value, test_count)
    SELECT 'file_cnt','',6 FROM dual;

EOF
    if ($status != 0) then
    cat /tmp/sql.$$.log
    echo "ERROR: SQL Error"
    exit 0
    endif
endif

if ($mode == "all" || $mode == "activesubset") then
    echo "Generating source Active subset... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_activesubset_${release}_gold');
    CREATE TABLE qa_activesubset_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

EOF
    if ($status != 0) then
    cat /tmp/sql.$$.log
    echo "ERROR: SQL Error"
    exit 0
    endif
endif

if ($mode == "all" || $mode == "optimization") then
    echo "Generating Optimization QA Counts ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >&! /tmp/sql.$$.log
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec meme_utility.drop_it('table','qa_optimization_${release}_gold');
    CREATE TABLE qa_optimization_${release}_gold (
       test_name   VARCHAR2(100) NOT NULL,
       test_value  VARCHAR2(3000),
       test_count  NUMBER(12) );

    -- file_cnt
    INSERT INTO qa_optimization_${release}_gold (test_name, test_value, test_count)
    SELECT 'file_cnt','',6 FROM dual;

EOF
    if ($status != 0) then
    cat /tmp/sql.$$.log
    echo "ERROR: SQL Error"
    exit 0
    endif
endif



echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"

