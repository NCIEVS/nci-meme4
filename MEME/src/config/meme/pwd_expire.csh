#!/bin/csh -f

# Get the Available Database
foreach db (`$MIDSVCS_HOME/bin/midsvcs-server.pl|grep databases |cut -f2 -d\| | awk -F\, '{for(i=1;i<=NF;i++)print $i}';`)
   # get the user and password 
     set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
     set DATA="`$ORACLE_HOME/bin/sqlplus -s $user@$db << EOF \
     set heading off; \
     set feed off; \
      select a.USERNAME,expiry_date from dba_users a where a.USERNAME in ('MTH', 'MEOW') and (a.EXPIRY_DATE - sysdate) < 15; \
EOF`"
if ("$DATA" != "") then
# echo "Password is going to expire for $DATA on $db Please reset." 
 echo "Password is going to expire for $DATA on $db. Please reset." | Mail -s "Password expiry" "mpriya@nlm.nih.gov, chanr@nlm.nih.gov, lankas@nlm.nih.gov, yaoh@nlm.nih.gov, gururajs@nlm.nih.gov"
endif
end
echo "Completed the processing"
