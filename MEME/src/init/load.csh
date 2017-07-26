#!/bin/csh -f

source $ENV_HOME/bin/env.csh

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
#set db=`$MIDSVCS_HOME/bin/midsvcs.pl -s editing-db`

echo "------------------------------------------------------"
echo "Starting ... `/bin/date`"
echo "------------------------------------------------------"
echo "    Loading data files"

foreach f (`ls *.ctl`)
  echo "      $f"
  $ORACLE_HOME/bin/sqlldr $user@$db control=$f >&! /tmp/sql.$$.log
  if ($status != 0) then
    echo "Error loading $f"
  endif
end

echo "    Re-generate indexes"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/sql.$$.log
@sql/$MEME_HOME/etc/sql/meme_indexes
EOF
if ($status != 0) then
    echo "Regenerating indexes"
endif

echo "    Generate testing data"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/sql.$$.log
@sql/gen_qa_data
EOF
if ($status != 0) then
    echo "Generating test data"
endif

echo "    Compute meme_tables"
$ORACLE_HOME/bin/sqlplus $user@$db <<EOF >&! /tmp/sql.$$.log
@sql/compute_meme_tables
EOF
if ($status != 0) then
    echo "Computing MEME_TABLES"
endif

/bin/rm -f /tmp/sql.$$.log
echo "------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "------------------------------------------------------"
  
