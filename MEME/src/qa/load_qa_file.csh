#!/bin/csh -f 
#
# Script:    load_qa_file.csh
# Author:    Brian Carlsen
#
# Remarks:   This script loads a ~ separated file containing
#            test_name,test_value,test_count fields into a table
#            with the same structure.
#
#            This is a helper script used by QA.MR*.csh
#
# Version Information:
#   01/22/2002 (1.0):  Ported to MRD
set version=1.0
set version_auth="BAC"
set version_date="01/22/2002"

source $ENV_HOME/bin/env.csh

#
# Check required variables
#
set required_vars = ("ORACLE_HOME")
foreach rv ($required_vars)
    if (`eval 'echo $?'$rv` == 0) then
        echo 'ERROR: $'$rv' must be set.'
	exit 1
    endif
end

set usage="Usage: $0 <database> <qa file> <qa table>"


#
# Parse arguments
#
if ($#argv == 1) then
    if ($argv[1] == "-v") then
        echo "$version"
        exit 0
    else if ($argv[1] == "-version") then
        echo "Version $version, $version_date ($version_auth)"
        exit 0
    else if ($argv[1] == "-help" || $argv[1] == "--help" || $argv[1] == "-h") then
	cat <<EOF
 $usage

 This script is used by the MR file QA scripts to load a
 qa table from a file.  The file should have three fields
 separated by ~ characters.

EOF
	exit 0
    else
        echo "$usage"
	exit 1
    endif


else if ($#argv != 3) then
    echo "ERROR: Wrong number of arguments"
    echo $usage
    exit 1
endif

set db=$1
set file=$2
set table=$3
set mu=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

#
# Create & load QA table
# - Note that this works for ingres but will have
# - to be ported for oracle
#
$ORACLE_HOME/bin/sqlplus $mu@$db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1
   
    exec MEME_UTILITY.drop_it('table','$table');
    CREATE TABLE $table (
	test_name     VARCHAR2(100) NOT NULL,
	test_value    VARCHAR2(3000),
        test_count    NUMBER(12)); 
EOF

if ($status != 0) then
    cat /tmp/t.$$.log
    echo 'ERROR creating qa table $table'
    exit 1
endif

#
# Load the qa_table
# - Create SQLLDR control file
# - prep the data file
# - load the table
#
if (! (-e $file)) then
    echo "ERROR: The qa file '$file' does not exist"
    exit 1
endif

/bin/cat >! $table.ctl <<EOF
load data
infile '$table'
badfile '$table'
discardfile '$table'
truncate
into table $table 
fields terminated by '~' 
trailing nullcols
(
 test_name              char,
 test_value             char,
 test_count		integer external
)
EOF

/bin/cp -f $file $table.dat

/bin/rm -f $table.bad

$ORACLE_HOME/bin/sqlldr $mu@$db control="$table.ctl" >&! /tmp/t.$$.log

if ($status != 0 || (-e $table.bad)) then
    cat /tmp/t.$$.log $table.log
    echo "ERROR: SQL*Loader failed for $file, $table"
    exit 1
endif

#
# Clean up
#
/bin/rm -f $table.ctl $table.log $table.dsc $table.bad $table.dat
/bin/rm -f /tmp/t.$$.log

exit 0
