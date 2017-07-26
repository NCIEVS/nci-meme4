#!/bin/csh -f
#
# File:   confirm_cxt_sql.csh
# Author: Tun Tun Naing
#
# Changes:
# 10/28/2006 TTN (1-CDMK9): suppress sql output in confirm_cxt_sql.csh
# 10/28/2006 TTN (1-CDMK9): EOF indentaion bug fix in confirm_cxt_sql.csh 
# Version info

set release=4
set version="5.1"
set authority="BAC";
set date="11/01/2006";

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

#
# Check required variables
#
if ($?INV_HOME == 0) then
    echo '$INV_HOME must be set.'
    exit 1
endif
if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set.'
    exit 1
endif

if ($#argv > 0) then
    if ("-version" == $argv[1]) then
	echo "Release ${release}: version $version, $date ($authority)"
	exit 0
    else if ("$argv[1]" == "-v") then
	echo "$version"
	exit 0
    else if ("$argv[1]" == "--help" || "$argv[1]" == "-help") then
    cat <<EOF
 This script has the following usage:
   Usage: confirm_cxt_sql.csh <database>

    This script verifies the required sql infrastructure for inversion process in specified database.
EOF
    exit 0
    endif
endif

if ($#argv != 1) then
    echo "   Usage: confirm_cxt_sql.csh <database>"
    exit 1
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $1`
set db=$1
set dbc="@$db"

echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "ORACLE_HOME:    $ORACLE_HOME"
echo "database:       $db"

set reload = 0

foreach table (`perl -ne 's/CREATE\s+TABLE\s+(.*)\(/$1/ && print if /^CREATE/;' $INV_HOME/etc/sql/cxt_tables.sql`)

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF 
	WHENEVER SQLERROR EXIT -1;
	SET FEEDBACK OFF;
declare
   FoundObject varchar2(3);
begin
    select decode(max(table_name),null,'NO','YES')
      into FoundObject from user_tables
     where table_name = upper('$table');
   if FoundObject = 'NO' then
        RAISE NO_DATA_FOUND;
   end if;
end;
/
EOF
if ($status != 0) then
	echo "Table $table doesn't exist"
	set reload = 1
endif

end

if ($reload == 1) then
    echo "Reload cxt_tables"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
		@$INV_HOME/etc/sql/cxt_tables.sql;
		EXIT;
EOF
endif

set reload = 0

foreach index (`perl -ne 's/CREATE\s+INDEX\s+(.*)\s+ON\s+.*/$1/ && print if /^CREATE/;' $INV_HOME/etc/sql/cxt_indexes.sql`)

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF 
	WHENEVER SQLERROR EXIT -1;
	SET FEEDBACK OFF;
declare
   FoundObject varchar2(3);
begin
    select decode(max(index_name),null,'NO','YES')
      into FoundObject from user_indexes
     where index_name = upper('$index');
   if FoundObject = 'NO' then
        RAISE NO_DATA_FOUND;
   end if;
end;
/
EOF
if ($status != 0) then
    echo "Index $index doesn't exist"
	set reload = 1
endif

end

if ($reload == 1) then
    echo "Reload cxt_indexes"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
		@$INV_HOME/etc/sql/cxt_indexes.sql;
		EXIT;
EOF
	
endif

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF 
	WHENEVER SQLERROR EXIT -1;
	SET FEEDBACK OFF;
declare
   FoundObject varchar2(3);
begin
    select decode(max(object_name),null,'NO','YES')
      into FoundObject from user_objects
     where object_name = 'MEME_CONTEXTS' and object_type = 'PACKAGE';
   if FoundObject = 'NO' then
        RAISE NO_DATA_FOUND;
   end if;
end;
/
EOF
if ($status != 0) then
	echo "Reload MEME_CONTEXTS"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
	SET FEEDBACK OFF;
	@$INV_HOME/etc/sql/MEME_CONTEXTS.sql;
EOF
endif

echo "--------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "--------------------------------------------------------------"

exit 0
