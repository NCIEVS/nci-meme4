#!/bin/sh -f
#
# Loads an RRF data set into an Oracle database
#
# input: <database>
# output: loaded database
#
# For useful information on loading your Metathesaurus subset
# into a Oracle database, please consult the on-line
# documentation at:
#
# http://www.nlm.nih.gov/research/umls/load_scripts.html
#

#
# Database connection parameters
# Please edit these variables to reflect your environment
#
if [ ! -n "$ORACLE_HOME" ]; then
   echo '$ORACLE_HOME must be set';
   exit 1;
fi

if [ ! -n "$MIDSVCS_HOME" ]; then
   echo '$MIDSVCS_HOME must be set';
   exit 1;
fi

if [$# != 1]; then
  echo "Usage: $0 <db>
fi
user=mth
password=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $1 -u mth`
tns_name=$1
NLS_LANG=AMERICAN_AMERICA.UTF8
export NLS_LANG

/bin/rm -f oracle.log
touch oracle.log
ef=0
mrcxt_flag=0

echo "See oracle.log for output"
echo "----------------------------------------" >> oracle.log 2>&1
echo "Starting ... `/bin/date`" >> oracle.log 2>&1
echo "----------------------------------------" >> oracle.log 2>&1
echo "ORACLE_HOME = $ORACLE_HOME" >> oracle.log 2>&1
echo "user =        $user" >> oracle.log 2>&1
echo "tns_name =    $tns_name" >> oracle.log 2>&1

# Create empty mrcxt if it doesn't exist, expected by oracle_tables.sql scriptif [ ! -f MRCXT.RRF ]; then mrcxt_flag=1; fi
if [ ! -f MRCXT.RRF ]; then `touch MRCXT.RRF`; fi

echo "    Create tables ... `/bin/date`" >> oracle.log 2>&1
echo "@oracle_tables.sql"|$ORACLE_HOME/bin/sqlplus $user/$password@$tns_name  >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi

echo "    Load content table data ... `/bin/date`" >> oracle.log 2>&1
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCOC.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCOC.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCOLS.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCOLS.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCONSO.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCONSO.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCUI.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCUI.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCXT.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCXT.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRDEF.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRDEF.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRDOC.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRDOC.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRFILES.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRFILES.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRHIER.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRHIER.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRHIST.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRHIST.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRMAP.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRMAP.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRRANK.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRRANK.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRREL.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRREL.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRSAB.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRSAB.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRSAT.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRSAT.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRSMAP.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRSMAP.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRSTY.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRSTY.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXNS_ENG.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXNS_ENG.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXNW_ENG.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXNW_ENG.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRAUI.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRAUI.log >> oracle.log

echo "    Load word index tables... `/bin/date`" >> oracle.log 2>&1
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_BAQ.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_BAQ.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_CZE.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_CZE.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_DAN.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_DAN.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_DUT.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_DUT.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_ENG.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_ENG.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_FIN.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_FIN.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_FRE.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_FRE.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_GER.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_GER.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_HEB.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_HEB.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_HUN.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_HUN.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_ITA.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_ITA.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_JPN.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_JPN.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_NOR.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_NOR.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_POR.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_POR.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_RUS.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_RUS.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_SPA.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_SPA.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_SWE.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_SWE.log >> oracle.log

echo "    Load auxiliary tables... `/bin/date`" >> oracle.log 2>&1
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="AMBIGSUI.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat AMBIGSUI.log >> oracle.log
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="AMBIGLUI.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat AMBIGLUI.log >> oracle.log
cd CHANGE
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="DELETEDCUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat DELETEDCUI.log >> ../oracle.log
cd ..
cd CHANGE
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="DELETEDLUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat DELETEDLUI.log >> ../oracle.log
cd ..
cd CHANGE
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="DELETEDSUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat DELETEDSUI.log >> ../oracle.log
cd ..
cd CHANGE
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MERGEDCUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MERGEDCUI.log >> ../oracle.log
cd ..
cd CHANGE
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MERGEDLUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MERGEDLUI.log >> ../oracle.log
cd ..

echo "    Create indexes ... `/bin/date`" >> oracle.log 2>&1
echo "@oracle_indexes.sql"|$ORACLE_HOME/bin/sqlplus $user/$password@$tns_name  >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi

if [ $mrcxt_flag -eq 1 ]
then
rm -f MRCXT.RRF
echo "DROP TABLE MRCXT;" >> drop_mrcxt.sql
echo "@drop_mrcxt.sql"|$ORACLE_HOME/bin/sqlplus $user/$password@$tns_name  >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
rm -f drop_mrcxt.sql
fi

echo "----------------------------------------" >> oracle.log 2>&1
if [ $ef -eq 1 ]
then
  echo "There were one or more errors.  Please reference the oracle.log file for details." >> oracle.log 2>&1
  retval=-1
else
  echo "Completed without errors." >> oracle.log 2>&1
  retval=0
fi
echo "Finished ... `/bin/date`" >> oracle.log 2>&1
echo "----------------------------------------" >> oracle.log 2>&1
exit $retval
