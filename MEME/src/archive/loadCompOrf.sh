#!/bin/sh -f
# 
# Loads a comparable ORF data set into an Oracle database
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

echo "----------------------------------------" >> oracle.log 2>&1
echo "Starting ... `/bin/date`" >> oracle.log 2>&1
echo "----------------------------------------" >> oracle.log 2>&1
echo "ORACLE_HOME = $ORACLE_HOME" >> oracle.log 2>&1
echo "user =        $user" >> oracle.log 2>&1
echo "tns_name =    $tns_name" >> oracle.log 2>&1

# Create empty mrcxt if it doesn't exist, expected by oracle_tables.sql scriptif [ ! -f MRCXT ]; then mrcxt_flag=1; fi
if [ ! -f MRCXT ]; then `touch MRCXT`; fi

echo "    Create tables ... `/bin/date`" >> oracle.log 2>&1
echo "@oracle_tables.sql"|$ORACLE_HOME/bin/sqlplus $user/$password@$tns_name  >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi

echo "    Load content tables ... `/bin/date`" >> oracle.log 2>&1
/bin/mv -f MRATX MRATX.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRATX.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRATX.log >> oracle.log
/bin/mv -f MRATX.dat MRATX
/bin/mv -f MRCOC MRCOC.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCOC.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCOC.log >> oracle.log
/bin/mv -f MRCOC.dat MRCOC
/bin/mv -f MRCOLS MRCOLS.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCOLS.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCOLS.log >> oracle.log
/bin/mv -f MRCOLS.dat MRCOLS
/bin/mv -f MRCON MRCON.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCON.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCON.log >> oracle.log
/bin/mv -f MRCON.dat MRCON
/bin/mv -f MRCXT MRCXT.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCXT.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCXT.log >> oracle.log
/bin/mv -f MRCXT.dat MRCXT
/bin/mv -f MRCUI MRCUI.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRCUI.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRCUI.log >> oracle.log
/bin/mv -f MRCUI.dat MRCUI
/bin/mv -f MRDEF MRDEF.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRDEF.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRDEF.log >> oracle.log
/bin/mv -f MRDEF.dat MRDEF
/bin/mv -f MRDOC MRDOC.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRDOC.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRDOC.log >> oracle.log
/bin/mv -f MRDOC.dat MRDOC
/bin/mv -f MRFILES MRFILES.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRFILES.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRFILES.log >> oracle.log
/bin/mv -f MRFILES.dat MRFILES
/bin/mv -f MRRANK MRRANK.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRRANK.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRRANK.log >> oracle.log
/bin/mv -f MRRANK.dat MRRANK
/bin/mv -f MRREL MRREL.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRREL.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRREL.log >> oracle.log
/bin/mv -f MRREL.dat MRREL
/bin/mv -f MRSAB MRSAB.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRSAB.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRSAB.log >> oracle.log
/bin/mv -f MRSAB.dat MRSAB
/bin/mv -f MRSAT MRSAT.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRSAT.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRSAT.log >> oracle.log
/bin/mv -f MRSAT.dat MRSAT
/bin/mv -f MRSO MRSO.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRSO.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRSO.log >> oracle.log
/bin/mv -f MRSO.dat MRSO
/bin/mv -f MRSTY MRSTY.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRSTY.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRSTY.log >> oracle.log
/bin/mv -f MRSTY.dat MRSTY
/bin/mv -f MRXNS.ENG MRXNS_ENG.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXNS_ENG.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXNS_ENG.log >> oracle.log
/bin/mv -f MRXNS_ENG.dat MRXNS.ENG
/bin/mv -f MRXNW.ENG MRXNW_ENG.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXNW_ENG.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXNW_ENG.log >> oracle.log
/bin/mv -f MRXNW_ENG.dat MRXNW.ENG

echo "    Load word index tables ... `/bin/date`" >> oracle.log 2>&1
/bin/mv -f MRXW.BAQ MRXW_BAQ.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_BAQ.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_BAQ.log >> oracle.log
/bin/mv -f MRXW_BAQ.dat MRXW.BAQ
/bin/mv -f MRXW.CZE MRXW_CZE.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_CZE.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_CZE.log >> oracle.log
/bin/mv -f MRXW_CZE.dat MRXW.CZE
/bin/mv -f MRXW.DAN MRXW_DAN.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_DAN.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_DAN.log >> oracle.log
/bin/mv -f MRXW_DAN.dat MRXW.DAN
/bin/mv -f MRXW.DUT MRXW_DUT.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_DUT.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_DUT.log >> oracle.log
/bin/mv -f MRXW_DUT.dat MRXW.DUT
/bin/mv -f MRXW.ENG MRXW_ENG.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_ENG.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_ENG.log >> oracle.log
/bin/mv -f MRXW_ENG.dat MRXW.ENG
/bin/mv -f MRXW.FIN MRXW_FIN.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_FIN.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_FIN.log >> oracle.log
/bin/mv -f MRXW_FIN.dat MRXW.FIN
/bin/mv -f MRXW.FRE MRXW_FRE.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_FRE.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_FRE.log >> oracle.log
/bin/mv -f MRXW_FRE.dat MRXW.FRE
/bin/mv -f MRXW.GER MRXW_GER.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_GER.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_GER.log >> oracle.log
/bin/mv -f MRXW_GER.dat MRXW.GER
/bin/mv -f MRXW.HEB MRXW_HEB.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_HEB.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_HEB.log >> oracle.log
/bin/mv -f MRXW_HEB.dat MRXW.HEB
/bin/mv -f MRXW.HUN MRXW_HUN.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_HUN.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_HUN.log >> oracle.log
/bin/mv -f MRXW_HUN.dat MRXW.HUN
/bin/mv -f MRXW.ITA MRXW_ITA.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_ITA.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_ITA.log >> oracle.log
/bin/mv -f MRXW_ITA.dat MRXW.ITA
/bin/mv -f MRXW.JPN MRXW_JPN.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_JPN.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_JPN.log >> oracle.log
/bin/mv -f MRXW_JPN.dat MRXW.JPN
/bin/mv -f MRXW.NOR MRXW_NOR.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_NOR.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_NOR.log >> oracle.log
/bin/mv -f MRXW_NOR.dat MRXW.NOR
/bin/mv -f MRXW.POR MRXW_POR.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_POR.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_POR.log >> oracle.log
/bin/mv -f MRXW_POR.dat MRXW.POR
/bin/mv -f MRXW.RUS MRXW_RUS.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_RUS.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_RUS.log >> oracle.log
/bin/mv -f MRXW_RUS.dat MRXW.RUS
/bin/mv -f MRXW.SPA MRXW_SPA.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_SPA.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_SPA.log >> oracle.log
/bin/mv -f MRXW_SPA.dat MRXW.SPA
/bin/mv -f MRXW.SWE MRXW_SWE.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MRXW_SWE.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MRXW_SWE.log >> oracle.log
/bin/mv -f MRXW_SWE.dat MRXW.SWE

echo "    Load auxiliary tables ... `/bin/date`" >> oracle.log 2>&1
/bin/mv -f AMBIG.SUI AMBIG_SUI.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="AMBIG_SUI.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat AMBIG_SUI.log >> oracle.log
/bin/mv -f AMBIG_SUI.dat AMBIG.SUI
/bin/mv -f AMBIG.LUI AMBIG_LUI.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="AMBIG_LUI.ctl" >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat AMBIG_LUI.log >> oracle.log
/bin/mv -f AMBIG_LUI.dat AMBIG.LUI
cd CHANGE
/bin/mv -f DELETED.CUI DELETED_CUI.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="DELETED_CUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat DELETED_CUI.log >> ../oracle.log
/bin/mv -f DELETED_CUI.dat DELETED.CUI
cd ..
cd CHANGE
/bin/mv -f DELETED.LUI DELETED_LUI.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="DELETED_LUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat DELETED_LUI.log >> ../oracle.log
/bin/mv -f DELETED_LUI.dat DELETED.LUI
cd ..
cd CHANGE
/bin/mv -f DELETED.SUI DELETED_SUI.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="DELETED_SUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat DELETED_SUI.log >> ../oracle.log
/bin/mv -f DELETED_SUI.dat DELETED.SUI
cd ..
cd CHANGE
/bin/mv -f MERGED.CUI MERGED_CUI.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MERGED_CUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MERGED_CUI.log >> ../oracle.log
/bin/mv -f MERGED_CUI.dat MERGED.CUI
cd ..
cd CHANGE
/bin/mv -f MERGED.LUI MERGED_LUI.dat
$ORACLE_HOME/bin/sqlldr $user/$password@$tns_name control="MERGED_LUI.ctl" >> ../oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi
cat MERGED_LUI.log >> ../oracle.log
/bin/mv -f MERGED_LUI.dat MERGED.LUI
cd ..

echo "    Create indexes ... `/bin/date`" >> oracle.log 2>&1
echo "@oracle_indexes.sql"|$ORACLE_HOME/bin/sqlplus $user/$password@$tns_name  >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi

echo "    Create and populate MRCONSO ... `/bin/date`" >> oracle.log 2>&1
echo "@mrconso_oracle.sql"|$ORACLE_HOME/bin/sqlplus $user/$password@$tns_name  >> oracle.log 2>&1
if [ $? -ne 0 ]; then ef=1; fi

if [ $mrcxt_flag -eq 1 ]
then
rm -f MRCXT
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
