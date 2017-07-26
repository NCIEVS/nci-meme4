#!/bin/csh -f
#
# Compute AUI content view for RXNORM CVF=4096
#
# CHANGES
# 02/07/2013 BAC: First version
#

if ($?ORACLE_HOME != 1) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($#argv != 2) then
    echo "Usage: $0 <database> <rxnconso file>"
    exit 1
endif

set db=$1
set file=$2
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

echo "----------------------------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "----------------------------------------------------------------------"
echo "db   = $db"
echo "file = $file"

#
# Check file
#
if (! -e $file) then
    echo "ERROR: RXCONSO file $file does not exist"
    exit 1
endif

#
# 1. Prep table/control file
#
echo "    Prepare table, control file ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >&! /tmp/t.$$.log
  WHENEVER SQLERROR EXIT -1
  exec meme_utility.drop_it('table','rxnorm_cvf');
  create table rxnorm_cvf as select source_aui from classes where 1=0;
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error preparing rxnorm_cvf"
    exit 1
endif

$MEME_HOME/bin/dump_mid.pl -t rxnorm_cvf $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping control file"
    exit 1
endif

echo "    Prepare data file ... `/bin/date`"
$PATH_TO_PERL -ne 'split /\|/; print "$_[7]|\n" if ($_[17] & 4096) != 0;' $file | sort -u -o rxnorm_cvf.dat
if ($status != 0) then
    echo "Error preparing .dat file"
    exit 1
endif
if (`cat rxnorm_cvf.dat | wc -l` == 0) then
    echo "ERROR: $file contains no entries with CVF & 4096 != 0"
    exit 1
endif

#
# 2. Load data
#
echo "    Load data ... `/bin/date`"
$ORACLE_HOME/bin/sqlldr $user@$db control="rxnorm_cvf.ctl" >>& /dev/null
if ($status != 0) then
    echo "Error loading .dat file"
    exit 1
endif

#
# 3. Load new data set
#
echo "    Process CVF 4096 ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >> /tmp/t.$$.log
  ALTER SESSION SET sort_area_size=200000000;
  ALTER SESSION SET hash_area_size=200000000;
  WHENEVER SQLERROR EXIT -1

  -- dump old ones
  UPDATE content_view_members SET code = code - 4096 WHERE BITAND(code,4096) = 4096;
  DELETE FROM content_view_members WHERE code = 0;
  commit;

  -- Create list of AUIs
  exec MEME_UTILITY.drop_it('table','rxnorm_cvf_1');
  CREATE TABLE rxnorm_cvf_1 AS
  SELECT aui, source_aui FROM classes
  WHERE source_aui in (SELECT * FROM rxnorm_cvf)
  UNION
  SELECT a.aui, b.attribute_value FROM classes a, attributes b, rxnorm_cvf c
  WHERE a.atom_id = b.atom_id
    AND b.source in (SELECT current_name FROM source_version where source='RXNORM')
    AND b.attribute_name='RXAUI' AND b.attribute_value = c.source_aui;

  -- Add CV row
  INSERT INTO rxnorm_cvf_1
  SELECT aui, 'CV_CODE' FROM classes a, attributes b 
  WHERE a.concept_id = b.concept_id AND b.attribute_name='CV_CODE'
    AND b.attribute_value = '4096'
    AND a.tobereleased in ('Y','y') and tty='CV';

  -- Load new rows
  -- Avoid non-ENG and suppressible
  UPDATE content_view_members
  SET code = code + 4096
  WHERE meta_ui IN (SELECT AUI from rxnorm_cvf_1)
    AND cascade = 'Y';

  INSERT INTO content_view_members (meta_ui,code, cascade)
  SELECT aui, 4096, 'Y' FROM rxnorm_cvf_1
  MINUS
  SELECT meta_ui, 4096, cascade FROM content_view_members
  WHERE cascade='Y';

  -- Rebuild table
  exec MEME_SYSTEM.rebuild_table('content_view_members','Y',' ');

EOF
if ($status != 0) then
    echo "Error preparing CVF 4096"
    cat /tmp/t.$$.log
    exit 1
endif

echo "    Confirm all SAUIs present in content view ... `/bin/date`"
$MEME_HOME/bin/dump_mid.pl -t rxnorm_cvf_1 $db . >>& /dev/null
if ($status != 0) then
    echo "Error dumping control file"
    exit 1
endif

#
# 4. Confirm 4096 set matches initial list
#
cut -d\| -f 2 rxnorm_cvf_1.dat | grep -v CV_CODE |\
   sed 's/$/\|/' | sort -u -o rxnorm_cvf_2.dat
set ct=`diff rxnorm_cvf.dat rxnorm_cvf_2.dat | wc -l`
if ($ct != 0) then
    echo "    ERROR: Some SAUIs not matching"
    diff rxnorm_cvf.dat rxnorm_cvf_2.dat | sed 's/^/      /' | head -100
    echo "      ..."
endif


#
# 5. Cleanup
#
echo "    Cleanup ... `/bin/date`"
/bin/rm -f rxnorm_cvf*.ctl
/bin/rm -f /tmp/t.$$.log

$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >&! /tmp/t.$$.log
  WHENEVER SQLERROR EXIT -1
  exec MEME_UTILITY.drop_it('table','rxnorm_cvf');
  exec MEME_UTILITY.drop_it('table','rxnorm_cvf_1');
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error cleaning up sct_cvf"
    exit 1
endif


echo "----------------------------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "----------------------------------------------------------------------"
