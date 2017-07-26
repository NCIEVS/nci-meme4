#!/bin/csh -f

#
# Download CORE problem list subset file and compute it as AUIs.
#
# CHANGES
# 
# 08/19/2009  : First version
# Developed same as sct_cvf.csh ( BAC script ) 
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
set file=SNOMEDCT_CORE_SUBSET.txt
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

echo "----------------------------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "----------------------------------------------------------------------"
echo "db   = $db"
echo "file = $file"

#
# 1. Prep table/control file
#
echo "    Prepare table, control file ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >&! /tmp/t.$$.log
  WHENEVER SQLERROR EXIT -1
  exec meme_utility.drop_it('table','core_cvf');
  CREATE TABLE core_cvf 
   (SNOMED_CID VARCHAR2(50),
    SNOMED_FSN        VARCHAR2(3000),
    SNOMED_CONCEPT_STATUS VARCHAR2(50),
    UMLS_CUI	VARCHAR2(50),
    OCCURRENCE VARCHAR2(50),
    USAGE VARCHAR2(50),
    FIRST_IN_SUBSET VARCHAR2(50),
    IS_RETIRED_FROM_SUBSET VARCHAR2(50),
    LAST_IN_SUBSET VARCHAR2(50),
    REPLACED_BY_SNOMED_CID VARCHAR2(50));
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error preparing core_cvf"
    exit 1
endif

$MEME_HOME/bin/dump_mid.pl -t core_cvf $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping control file"
    exit 1
endif

echo "    Prepare data file ... `/bin/date`"
grep -v SNOMED_CID $file >! core_cvf.dat
grep -v SNOMED_CID $file | cut -d\| -f1 |  sed 's/$/\|/g' | sort -u -o core_cvf_cid.dat
if ($status != 0) then
    echo "Error preparing .dat file"
    exit 1
endif

set ct = `$PATH_TO_PERL -ne 'split /\|/; print if scalar(@_) != 11;' core_cvf.dat | wc -l`
if ($ct > 0) then
	echo "ERROR: you need to add a pipe character to the end of the file for this to work"
	echo '    sed 's/$/\|/' $file >! x.$$'
	echo '    /bin/mv x.$$ $file'
	exit 1
endif

#
# 2. Load data
#
echo "    Load data ... `/bin/date`"
$ORACLE_HOME/bin/sqlldr $user@$db control="core_cvf.ctl" >>& /dev/null
if ($status != 0) then
    echo "Error loading .dat file"
    exit 1
endif

#
# 3. Load new data set
#
echo "    Process CVF 2048 ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >> /tmp/t.$$.log
  ALTER SESSION SET sort_area_size=200000000;
  ALTER SESSION SET hash_area_size=200000000;
  WHENEVER SQLERROR EXIT -1

  -- dump old ones
  UPDATE content_view_members SET code = code - 2048 WHERE BITAND(code,2048) = 2048;
  DELETE FROM content_view_members WHERE code = 0;
  commit;

  -- Create list of AUIs
  -- CORE content is SNOMED SCUI level
  exec MEME_UTILITY.drop_it('table','core_cvf_1');
  CREATE TABLE core_cvf_1 AS
  SELECT aui
   FROM classes a, core_cvf b
   WHERE a.SOURCE = (SELECT current_name FROM source_version WHERE SOURCE = 'SNOMEDCT_US')
   AND a.source_cui = b.snomed_cid;
  
  -- Add CV row
  INSERT INTO core_cvf_1
  SELECT aui FROM classes a, attributes b 
  WHERE a.concept_id = b.concept_id AND b.attribute_name='CV_CODE'
    AND b.attribute_value = '2048'
    AND a.tobereleased in ('Y','y') and tty='CV';

  -- Load new rows
  UPDATE content_view_members
  SET code = code + 2048 
  WHERE meta_ui IN (SELECT AUI from core_cvf_1)
    AND cascade = 'N';
  
  INSERT INTO content_view_members (meta_ui,code, cascade)
  SELECT aui, 2048, 'N' FROM core_cvf_1
  MINUS
  SELECT meta_ui, 2048, cascade FROM content_view_members
  WHERE cascade='N';
  
  commit;

  -- Rebuild table
  exec MEME_SYSTEM.rebuild_table('content_view_members','Y',' ');
 
  -- Load cvf attributes 
  -- Create list of content view attributes   
  exec MEME_UTILITY.drop_it('table','core_cvf_2'); 
  CREATE TABLE core_cvf_2 AS 
  SELECT snomed_cid, '2048~OCCURRENCE~' || occurrence attribute_value
  FROM core_cvf
  WHERE occurrence IS NOT NULL
  UNION
  SELECT snomed_cid, '2048~USAGE~' || USAGE
  FROM core_cvf
  WHERE USAGE IS NOT NULL
  UNION
  SELECT snomed_cid, '2048~FIRST_IN_SUBSET~' || first_in_subset
  FROM core_cvf
  WHERE first_in_subset IS NOT NULL
  UNION
  SELECT snomed_cid, '2048~IS_RETIRED_FROM_SUBSET~' || is_retired_from_subset
  FROM core_cvf
  WHERE is_retired_from_subset IS NOT NULL
  UNION
  SELECT snomed_cid, '2048~REPLACED_BY_SNOMED_CID~' || replaced_by_snomed_cid
  FROM core_cvf
  WHERE replaced_by_snomed_cid IS NOT NULL;


  --content view attributes are at SCUI level
  exec MEME_UTILITY.drop_it('table','core_cvf_3');
  CREATE TABLE core_cvf_3 AS
  SELECT * FROM source_attributes where 1 = 0;

  INSERT INTO core_cvf_3
  SELECT 'U',0,0,0,0,
          snomed_cid,'ROOT_SOURCE_CUI','SNOMEDCT_US','',
          0,'S','CV_MEMBER',attribute_value,'N','MTH','N','N','Y',9999,'N','','',''
  FROM core_cvf_2 a;

  COMMIT; 
  
  -- This is shouldn't happen but femur_midtest has old snomed for testing
  -- Delete START
  DELETE FROM core_cvf_3 a
  WHERE NOT EXISTS (
           SELECT code
           FROM classes b
           WHERE b.SOURCE = (SELECT current_name
                                    FROM source_version
                                   WHERE SOURCE = 'SNOMEDCT_US')
                 AND b.code = a.sg_id); 
  COMMIT;
  -- Delete END
  
  --get preferred atom for SNOMED SCUI 
  exec MEME_SOURCE_PROCESSING.map_sg_fields_all(table_name   => 'core_cvf_3');

  -- Find new and removed CORE content view attributes
  exec MEME_UTILITY.drop_it('table','core_cvf_newattr'); 
  CREATE TABLE core_cvf_newattr AS
  SELECT concept_id,atom_id,attribute_id, attribute_level,
                   attribute_name,attribute_value,source,status,
                   generated_status, released,tobereleased, suppressible,
                   sg_id, sg_type, sg_qualifier, source_atui
  FROM attributes where 1= 0; 
  
  INSERT INTO core_cvf_newattr
  SELECT 0 concept_id, atom_id, 0 attribute_id, 'S' attribute_level,
    attribute_name, attribute_value, source,status,
    generated_status,released,tobereleased,suppressible, sg_id,  sg_type, sg_qualifier, source_atui 
  FROM core_cvf_3 c;
  
  -- Remove CV_MEMBER attributes connected to current version SNOMEDCT_US
  exec MEME_UTILITY.drop_it('table','core_cvf_removeattr');
  CREATE TABLE core_cvf_removeattr AS
  SELECT attribute_id row_id
  FROM classes a, attributes b
  WHERE a.SOURCE = (SELECT current_name FROM source_version where source = 'SNOMEDCT_US')
   AND a.tobereleased IN ('y', 'Y')
   AND a.atom_id = b.atom_id
   AND b.attribute_name = 'CV_MEMBER'
   AND b.tobereleased IN ('y', 'Y');
  
  -- Remove CV_MEMBER attributes connected to old version SNOMEDCT_US atoms.
  INSERT INTO core_cvf_removeattr
  SELECT attribute_id row_id
  FROM classes a, attributes b
  WHERE a.SOURCE = (SELECT previous_name FROM source_version where source = 'SNOMEDCT_US')
   AND a.atom_id = b.atom_id
   AND b.attribute_name = 'CV_MEMBER'
   AND b.tobereleased IN ('y', 'Y');
       
  COMMIT;
  
  -- Create a QA tables (add not null to keep CV atom out of the mix)
  exec MEME_UTILITY.drop_it('table','core_cvf_qa_1');
  CREATE TABLE core_cvf_qa_1 AS
  SELECT DISTINCT source_cui FROM classes a
  WHERE source = (SELECT current_name FROM source_version WHERE source='SNOMEDCT_US')
    AND aui IN (SELECT meta_ui FROM content_view_members WHERE bitand(code,2048) != 0)
	AND source_cui is not null;

EOF
if ($status != 0) then
    echo "Error preparing CVF 2048"
    cat /tmp/t.$$.log
    exit 1
endif


echo "    Remove deleted content view attributes ... `/bin/date`"
$MEME_HOME/bin/batch.pl -w 0 -a=T -n=n -t=A -s=t core_cvf_removeattr $db MTH >&! /tmp/t.$$.log
if ($status != 0) then
   cat /tmp/t.$$.log
   echo "Error removing content view attributes"
   exit 1
endif

echo "    Loading content view attributes  ... `/bin/date`"
$MEME_HOME/bin/insert.pl -w=0 -atts core_cvf_newattr $db MTH >&! /tmp/t.$$.log
if ($status != 0) then
   cat /tmp/t.$$.log
   echo "Error loading new content view attributes"
   exit 1
endif

echo "    Confirm all SCUIs present in content view ... `/bin/date`"
$MEME_HOME/bin/dump_mid.pl -t core_cvf_qa_1 $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping control file"
    exit 1
endif
/bin/sort -u -o core_cvf_qa_1.dat core_cvf_qa_1.dat

#
# Confirm 2048 set matches initial list
#
set ct=`diff core_cvf_cid.dat core_cvf_qa_1.dat | wc -l`
if ($ct != 0) then
    echo "    ERROR: Some SCUIs not matching"
    diff core_cvf_cid.dat core_cvf_qa_1.dat | sed 's/^/      /' | head -100
    echo "      ..."
	exit 1
endif

echo "    Cleanup ... `/bin/date`"
/bin/rm -f core_cvf*.dat core_cvf*.ctl
/bin/rm -f /tmp/t.$$.log
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >&! /tmp/t.$$.log
  WHENEVER SQLERROR EXIT -1
  exec MEME_UTILITY.drop_it('table','core_cvf');
  exec MEME_UTILITY.drop_it('table','core_cvf_1');
  exec MEME_UTILITY.drop_it('table','core_cvf_2');
  exec MEME_UTILITY.drop_it('table','core_cvf_3');
  exec MEME_UTILITY.drop_it('table','core_cvf_qa_1');
  exec MEME_UTILITY.drop_it('table','core_cvf_newattr');
  exec MEME_UTILITY.drop_it('table','core_cvf_removeattr');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error cleaning up core_cvf"
    exit 1
endif

echo "----------------------------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "----------------------------------------------------------------------"
