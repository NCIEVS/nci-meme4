#!/bin/csh -f
#
# Take lan's data and produce AUI list
#

if ($?ORACLE_HOME != 1) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($#argv != 2) then
    echo "Usage: $0 <database> <file>"
    exit 1
endif

set db=$1
set file=$2
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

#
# 1. Prep table/control file
#
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >> /dev/null
  drop table lan_data;
  create table lan_data as select last_release_cui cui, sui from classes
  where 1=0;
EOF

$MEME_HOME/bin/dump_mid.pl -t lan_data $db .

# 
# 2. Make CUI,SUI list
# 
/bin/cut -d\| -f 1,6 $file | /bin/sort -u | sed 's/$/\|/' >! lan_data.dat

#
# 3. Load data
#
$ORACLE_HOME/bin/sqlldr $user@$db control="lan_data.ctl"

#
# 4. Load new data set
#
$ORACLE_HOME/bin/sqlplus  $user@$db <<EOF >> /dev/null
  ALTER SESSION SET sort_area_size=200000000;
  ALTER SESSION SET hash_area_size=200000000;

  -- dump old ones
  UPDATE content_view_members SET code = code - 256 WHERE BITAND(code,256) = 256;
  DELETE FROM content_view_members WHERE code = 0;
  commit;

  -- map through CUI data
  update lan_data set cui = (select cui2 from cui_history where cui = cui1)
  where cui in (select cui1 from cui_history where relationship_name='SY');
  commit;
  
  -- Load new rows
  UPDATE content_view_members
  SET code = code + 256 
  WHERE meta_ui IN
    (SELECT aui FROM classes 
     WHERE (last_release_cui,sui) IN
       (SELECT cui,sui FROM lan_data)
       AND aui IS NOT NULL)
    AND cascade = 'N';

  INSERT INTO content_view_members (meta_ui,code, cascade) 
  SELECT aui, 256, 'N' FROM
  (SELECT aui FROM classes 
   WHERE (last_release_cui,sui) IN
     (SELECT cui,sui FROM lan_data)
     AND aui IS NOT NULL
   MINUS
   SELECT meta_ui FROM content_view_members
   WHERE cascade='N');


     

  -- Rebuild table
  exec MEME_SYSTEM.rebuild_table('content_view_members','N',' ');

EOF
