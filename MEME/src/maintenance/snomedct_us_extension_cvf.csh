#!/bin/csh -f
#
# Compute AUI content view for SNOMEDCT_US Extension CVF=8192
#
# CHANGES
# 02/07/2013 BAC: First version
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
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

echo "----------------------------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "----------------------------------------------------------------------"
echo "db   = $db"


#
# 3. Load new data set
#
echo "    Process CVF 8192 ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >> /tmp/t.$$.log
  ALTER SESSION SET sort_area_size=200000000;
  ALTER SESSION SET hash_area_size=200000000;
  WHENEVER SQLERROR EXIT -1
  
  -- Determine SNOMEDCT US extension data
  exec MEME_UTILITY.drop_it('table','sct_usx_data');
  
  CREATE table sct_usx_data as
   SELECT distinct a.aui
     FROM classes a
    WHERE     EXISTS
                 (SELECT 1
                    FROM attributes b
                   WHERE     a.concept_id = b.concept_id
                         AND a.atom_id = b.atom_id
                         AND b.attribute_name = 'MODULE_ID'
                         AND b.attribute_value = '731000124108'
                         AND b.source LIKE 'SNOMEDCT_US%'
                         and b.sg_type not like '%RUI%'
                         AND b.tobereleased IN ('Y', 'y'))
          AND a.tobereleased IN ('Y', 'y');

  -- dump old ones
  UPDATE content_view_members SET code = code - 8192 WHERE BITAND(code,8192) = 8192;
  DELETE FROM content_view_members WHERE code = 0;
  commit;

  -- Add CV row
  INSERT INTO sct_usx_data
  SELECT aui FROM classes a, attributes b 
  WHERE a.concept_id = b.concept_id AND b.attribute_name='CV_CODE'
    AND b.attribute_value = '8192'
    AND a.tobereleased in ('Y','y') and tty='CV';

  -- Load new rows
  UPDATE content_view_members
  SET code = code + 8192
  WHERE meta_ui IN (SELECT AUI from sct_usx_data)
    AND cascade = 'Y';

  INSERT INTO content_view_members (meta_ui,code, cascade)
  SELECT aui, 8192, 'Y' FROM sct_usx_data
  MINUS
  SELECT meta_ui, 8192, cascade FROM content_view_members
  WHERE cascade='Y';

  -- Rebuild table
  exec MEME_SYSTEM.rebuild_table('content_view_members','Y',' ');

EOF
if ($status != 0) then
    echo "Error preparing CVF 8192"
    cat /tmp/t.$$.log
    exit 1
endif


#
# 5. Cleanup
#
echo "    Cleanup ... `/bin/date`"

/bin/rm -f /tmp/t.$$.log

$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >&! /tmp/t.$$.log
  WHENEVER SQLERROR EXIT -1
  exec MEME_UTILITY.drop_it('table','sct_usx_data');
  EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error cleaning up sct_usx_data"
    exit 1
endif


echo "----------------------------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "----------------------------------------------------------------------"
