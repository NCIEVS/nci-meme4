#!/bin/csh -f
#
# Download SNOMEDCT_US FDA problem list subset file and compute it as AUIs.
#
# CHANGES
# 02/07/2008 BAC (1-GLEBT): Add non-SNOMEDCT_US SRL=4 sources back in.
# 01/07/2008 BAC (1-G5CUX): Added more QA checks (suggested by KWF)
# 11/28/2007 BAC (1-FVDAH): Use only SNOMEDCT_US and SRL=0 (+mdr), add QA check.
#                           Ensure only SNOMEDCT_US SCUIs in the list are kept.
# 10/26/2007 BAC (1-FM4Q9): Changes to exclude non-ENG rows.
# 06/08/2007 BAC (1-EFITF): First version (tested with 2007AB data)
#

if ($?ORACLE_HOME != 1) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($#argv != 1) then
    echo "Usage: $0 <database>"
    exit 1
endif

set db=$1
set file=ProblemListSubset.txt
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

echo "----------------------------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "----------------------------------------------------------------------"
echo "db   = $db"
echo "file = $file"

#
# Obtain File
#
echo "    Obtain file ...`/bin/date`"
wget ftp://ftp1.nci.nih.gov/pub/cacore/EVS/FDA/ProblemList/ProblemListSubset.txt
if ($status != 0) then
    echo "Failed to download file"
    exit 1
endif

#
# 1. Prep table/control file
#
echo "    Prepare table, control file ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >&! /tmp/t.$$.log
  WHENEVER SQLERROR EXIT -1
  exec meme_utility.drop_it('table','sct_cvf');
  create table sct_cvf as select source_cui from classes where 1=0;
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error preparing sct_cvf"
    exit 1
endif

$MEME_HOME/bin/dump_mid.pl -t sct_cvf $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping control file"
    exit 1
endif

echo "    Prepare data file ... `/bin/date`"
grep -v RETIRED ProblemListSubset.txt | grep -v SCTID | $PATH_TO_PERL -ne 'split /\t/; print "$_[0]|\n";' >&! sct_cvf.dat
if ($status != 0) then
    echo "Error preparing .dat file"
    exit 1
endif

#
# 2. Load data
#
echo "    Load data ... `/bin/date`"
$ORACLE_HOME/bin/sqlldr $user@$db control="sct_cvf.ctl" >>& /dev/null
if ($status != 0) then
    echo "Error loading .dat file"
    exit 1
endif

#
# 3. Load new data set
#
echo "    Process CVF 512 ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >> /tmp/t.$$.log
  ALTER SESSION SET sort_area_size=200000000;
  ALTER SESSION SET hash_area_size=200000000;
  WHENEVER SQLERROR EXIT -1

  -- dump old ones
  UPDATE content_view_members SET code = code - 512 WHERE BITAND(code,512) = 512;
  DELETE FROM content_view_members WHERE code = 0;
  commit;

  -- Create list of AUIs
  -- SL: Added the 512 CV atom to be included in this CVF
  exec MEME_UTILITY.drop_it('table','sct_cvf_1');
  CREATE TABLE sct_cvf_1 AS
  WITH concepts AS
      (SELECT concept_id FROM classes
       WHERE source_cui in (SELECT source_cui FROM sct_cvf)
         AND tobereleased = 'Y'
         AND source = (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US'))
  SELECT aui FROM classes
  WHERE concept_id in (SELECT concept_id FROM concepts)
    AND source IN (SELECT source FROM source_rank WHERE restriction_level = 0
                   UNION SELECT source FROM source_rank WHERE restriction_level = 9)
    AND source NOT IN (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US')
    AND (suppressible = 'N' or source_cui in (SELECT source_cui FROM sct_cvf))
    AND tobereleased in ('Y','y')
    AND language = 'ENG'
    AND aui IS NOT NULL
  UNION
  SELECT aui FROM classes
  WHERE concept_id in (SELECT concept_id FROM concepts)
    AND source IN (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US')
    AND (suppressible = 'N'  or source_cui in (SELECT source_cui FROM sct_cvf))
    AND tobereleased in ('Y','y')
    AND source_cui IN (SELECT source_cui FROM sct_cvf)
    AND aui IS NOT NULL
 UNION
  SELECT aui from classes
  where concept_id in ( select concept_id from attributes where attribute_name = 'CV_CODE' and attribute_value = '512' and tobereleased in ('y','Y'))
  and termgroup = 'MTH/CV' and tobereleased in ('Y','y');
  
  -- Load new rows
  -- Avoid non-ENG and suppressible
  UPDATE content_view_members
  SET code = code + 512
  WHERE meta_ui IN (SELECT AUI from sct_cvf_1)
    AND cascade = 'N';

  INSERT INTO content_view_members (meta_ui,code, cascade)
  SELECT aui, 512, 'N' FROM sct_cvf_1
  MINUS
  SELECT meta_ui, 512, cascade FROM content_view_members
  WHERE cascade='N';

  -- Rebuild table
  exec MEME_SYSTEM.rebuild_table('content_view_members','Y',' ');

EOF
if ($status != 0) then
    echo "Error preparing CVF 512"
    cat /tmp/t.$$.log
    exit 1
endif


#
# 4. Load new data set
#
echo "    Process CVF 1024 ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >> /tmp/t.$$.log
  ALTER SESSION SET sort_area_size=200000000;
  ALTER SESSION SET hash_area_size=200000000;

  -- dump old ones
  UPDATE content_view_members SET code = code - 1024 WHERE BITAND(code,1024) = 1024;
  DELETE FROM content_view_members WHERE code = 0;
  commit;

  -- Create list of AUIs
  -- SL: Adding the 1024 AUI to include in the subset.
  exec MEME_UTILITY.drop_it('table','sct_cvf_1');
  CREATE TABLE sct_cvf_1 AS
  WITH concepts AS
      (SELECT concept_id FROM classes
       WHERE source_cui in (SELECT source_cui FROM sct_cvf)
         AND tobereleased = 'Y' 
         AND source = (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US'))
  SELECT aui FROM classes
  WHERE concept_id in (SELECT concept_id FROM concepts)
    AND source IN (SELECT source FROM source_rank WHERE restriction_level = 0
                   UNION SELECT source FROM source_rank WHERE restriction_level = 9
                   UNION SELECT current_name FROM source_version WHERE source='MDR')
    AND source NOT IN (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US')
    AND suppressible = 'N'
    AND tobereleased in ('Y','y')
    AND language = 'ENG'
    AND aui IS NOT NULL
  UNION
  SELECT aui FROM classes
  WHERE concept_id in (SELECT concept_id FROM concepts)
    AND source IN (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US')
    AND (suppressible = 'N' or source_cui in (SELECT source_cui FROM sct_cvf))
    AND tobereleased in ('Y','y')
    AND source_cui IN (SELECT source_cui FROM sct_cvf)
    AND aui IS NOT NULL
  UNION
  SELECT aui from classes
  where concept_id in ( select concept_id from attributes where attribute_name = 'CV_CODE' and attribute_value = '1024' and tobereleased in ('y','Y'))
  and termgroup = 'MTH/CV' and tobereleased in ('Y','y');
 
  -- Load new rows
  UPDATE content_view_members
  SET code = code + 1024
  WHERE meta_ui IN (SELECT aui FROM sct_cvf_1)
    AND cascade = 'N';

  INSERT INTO content_view_members (meta_ui,code, cascade)
  SELECT aui, 1024, 'N' FROM sct_cvf_1
  MINUS
  SELECT meta_ui, 1024, cascade FROM content_view_members
  WHERE cascade='N';

  -- Rebuild table
  exec MEME_SYSTEM.rebuild_table('content_view_members','Y',' ');

  -- Create a QA tables
  exec MEME_UTILITY.drop_it('table','sct_cvf_1');
  CREATE TABLE sct_cvf_1 AS
  SELECT DISTINCT source_cui FROM classes a 
  WHERE source = (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US')
    AND aui IN (SELECT meta_ui FROM content_view_members WHERE bitand(code,512) != 0);

  exec MEME_UTILITY.drop_it('table','sct_cvf_2');
  CREATE TABLE sct_cvf_2 AS
  SELECT DISTINCT source_cui FROM classes a 
  WHERE source = (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US')
    AND aui IN (SELECT meta_ui FROM content_view_members WHERE bitand(code,1024) != 0);

  exec MEME_UTILITY.drop_it('table','sct_cvf_3');
  CREATE TABLE sct_cvf_3 AS
  SELECT DISTINCT a.source, restriction_level, language, suppressible 
  FROM classes a, source_rank b
  WHERE a.source = b.source
    AND aui IN (SELECT meta_ui FROM content_view_members WHERE bitand(code,512) != 0);

  exec MEME_UTILITY.drop_it('table','sct_cvf_4');
  CREATE TABLE sct_cvf_4 AS
  SELECT DISTINCT a.source, restriction_level, language, suppressible
  FROM classes a, source_rank b
  WHERE a.source = b.source
    AND aui IN (SELECT meta_ui FROM content_view_members WHERE bitand(code,1024) != 0);
    

EOF
if ($status != 0) then
    echo "Error preparing CVF 1024"
    cat /tmp/t.$$.log
    exit 1
endif

echo "    Confirm all SCUIs present in content view ... `/bin/date`"
$MEME_HOME/bin/dump_mid.pl -t sct_cvf_1 $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping control file"
    exit 1
endif
/bin/sort -u -o sct_cvf_1.dat sct_cvf_1.dat

$MEME_HOME/bin/dump_mid.pl -t sct_cvf_2 $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping control file"
    exit 1
endif
/bin/sort -u -o sct_cvf_2.dat sct_cvf_2.dat
/bin/sort -u -o sct_cvf.dat sct_cvf.dat

#
# Confirm 512 set matches initial list
#
set ct=`diff sct_cvf.dat sct_cvf_1.dat | wc -l`
if ($ct != 0) then
    echo "    ERROR: Some SCUIs not matching"
    diff sct_cvf.dat sct_cvf_1.dat | sed 's/^/      /' | head -100
    echo "      ..."
endif

#
# Confirm 1024 set matches initial list
#
set ct=`diff sct_cvf.dat sct_cvf_2.dat | wc -l`
if ($ct != 0) then
    echo "    ERROR: Some SCUIs not matching"
    diff sct_cvf.dat sct_cvf_2.dat | sed 's/^/      /' | head -100
    echo "      ..."
endif

#
# Confirm only LEVEL 0,4 sources in 512 set (except SNOMEDCT_US)
#
$MEME_HOME/bin/dump_mid.pl -t sct_cvf_3 $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping sct_cvf_3 file"
    exit 1
endif
set ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[1] !~ /^(0|9)$/ && $_[0] !~ "SNOMEDCT_US";' sct_cvf_3.dat | wc -l`
if ($ct != 0) then
    echo "    ERROR: Unexpected source (bad restriction level)"
    $PATH_TO_PERL -ne 'split /\|/; print if $_[1] !~ /^(0|9)$/ && $_[0] !~ "SNOMEDCT_US";' sct_cvf_3.dat
    exit 1
endif

#
# Confirm only ENG sources in 512 set
#
set ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[2] ne "ENG"' sct_cvf_3.dat | wc -l`
if ($ct != 0) then
    echo "    ERROR: Unexpected source (non-English)"
    $PATH_TO_PERL -ne 'split /\|/; print if $_[2] ne "ENG"' sct_cvf_3.dat
    exit 1
endif

#
# Confirm only non-suppressible atoms in 512 set
#
#set ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[3] ne "N"' sct_cvf_3.dat | wc -l`
#if ($ct != 0) then
#    echo "    ERROR: Unexpected source (non-suppressible)"
#    $PATH_TO_PERL -ne 'split /\|/; print if $_[3] ne "N"' sct_cvf_3.dat
#    exit 1
#endif

#
# Confirm only LEVEL 0 sources in 1024 set (except SNOMEDCT_US)
#
$MEME_HOME/bin/dump_mid.pl -t sct_cvf_4 $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping sct_cvf_4 file"
    exit 1
endif
set ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[1] !~ /^(0|9)$/ && $_[0] !~ "SNOMEDCT_US" && $_[0] !~ "MDR";' sct_cvf_4.dat | wc -l`
if ($ct != 0) then
    echo "    ERROR: Unexpected source (bad restriction level)"
    $PATH_TO_PERL -ne 'split /\|/; print if $_[1] !~ /^(0|9)$/ && $_[0] !~ "SNOMEDCT_US" && $_[0] !~ "MDR";' sct_cvf_4.dat
    exit 1
endif

#
# Confirm only ENG sources in 1024 set
#
set ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[2] ne "ENG"' sct_cvf_4.dat | wc -l`
if ($ct != 0) then
    echo "    ERROR: Unexpected source (non-English)"
    $PATH_TO_PERL -ne 'split /\|/; print if $_[2] ne "ENG"' sct_cvf_4.dat
    exit 1
endif

#
# Confirm only non-suppressible atoms in 1024 set
#
#set ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[3] ne "N"' sct_cvf_4.dat | wc -l`
#if ($ct != 0) then
#    echo "    ERROR: Unexpected source (non-suppressible)"
#    $PATH_TO_PERL -ne 'split /\|/; print if $_[3] ne "N"' sct_cvf_4.dat
#    exit 1
#endif

#echo "    Cleanup ... `/bin/date`"
#/bin/rm -f ProblemListSubset.txt sct_cvf*.dat sct_cvf*.ctl
#/bin/rm -f /tmp/t.$$.log

$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >&! /tmp/t.$$.log
  WHENEVER SQLERROR EXIT -1
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error cleaning up sct_cvf"
    exit 1
endif


echo "----------------------------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "----------------------------------------------------------------------"
