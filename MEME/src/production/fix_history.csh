#!/bin/csh -f

#
# UMLS insertions can create a situation where CUI history
# records for CUIs never in NCI-META get created.
# This script compares the CUI history list against the previous
# release and dumps any entries that do not belong
#

if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

set s1=0
set s2=0

if ($#argv != 3) then
    echo "Usage: $0 <old dir> <new dir> <db>"
    exit 1
endif

if ($?MRD_HOME == 0) then
    echo '$MRD_HOME must be set'
    exit 1
endif

set new=$2
set old=$1
set db=$3

echo "-------------------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "-------------------------------------------------------------------------"
echo "new: $new"
echo "old: $old"

#
# Load old CUIs into DB
#
echo "    Load old CUIs into DB ...`/bin/date`"
set userpwd=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
$ORACLE_HOME/bin/sqlplus -s $userpwd@$db<<EOF | sed 's/^/      /'
    WHENEVER SQLERROR EXIT -2
    drop table t1;
    create table t1 as select cui from concept_status where 1=0;
EOF
if ($status != 0) then
    echo "Error creating table"
    exit 1
endif

echo "    Create t1.ctl ...`/bin/date`"
$MEME_HOME/bin/dump_mid.pl -t t1 $db . | sed 's/^/      /'
if ($status != 0) then
    echo "Error creating t1.ctl"
    exit 1
endif

echo "   Look up old CUIs ...`/bin/date`"
/bin/cut -d\| -f 1 $old/MRCUI.RRF | sort -u -o oldcuis.txt
/bin/cut -d\| -f 1 $old/MRCONSO.RRF | sort -u >> oldcuis.txt
/bin/sort -u -o oldcuis.txt oldcuis.txt
sed 's/$/\|/' oldcuis.txt >! t1.dat
/bin/rm -rf oldcuis.txt

$ORACLE_HOME/bin/sqlldr $userpwd@$db control="t1.ctl" | sed 's/^/      /'
if ($status != 0) then
    echo "Error loading t1 data"
    exit 1
endif

#
# Remove records from cui_history (keep cui_history_bak unchanged)
#
echo "    Delete from cui_history where CUI1 not in old cuis list ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $userpwd@$db <<EOF | sed 's/^/      /'
    WHENEVER SQLERROR EXIT -2
    delete from cui_history where cui1 in
      (select cui1 from cui_history minus select cui from t1);
EOF
if ($status != 0) then
    echo "Error cleaning up cui history"
    exit 1
endif

#
# Cleanup
#
/bin/rm -f t1.dat t1.ctl t1.log

echo "-------------------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "-------------------------------------------------------------------------"
