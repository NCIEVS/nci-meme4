#!/bin/csh -f
#
# File:    copy_qa_reasons.csh
# Author:  Brian Carlsen
#
# REMARKS: 
#
#  This script is used to copy the qa reasons to the MID production database.
#
#
# Version Info
#
# 02/23/2007 (2.0.1): Initial version
#
set release="2"
set version="0.1"
set version_authority="BAC"
set version_date="02/23/2007"

#
# Set environment (if configured)
#
if ($?ENV_HOME == 0) then
    echo '$'"ENV_HOME must be set"
    exit 1
endif
if ($?ENV_FILE == 0) then
    echo '$'"ENV_HOME must be set"
    exit 1
endif
source $ENV_HOME/bin/env.csh

#
# Check Environment Variables
#
if ($?ORACLE_HOME == 0) then
    echo '$'"ORACLE_HOME must be set"
    exit 1
endif

if ($?MRD_HOME == 0) then
    echo '$MRD_HOME must be set'
    exit 1
endif
#
# parse arguments
#
if ($#argv > 0) then
    if ("$argv[1]" == "-version") then
        echo "Version $version, $version_date ($version_authority)"
        exit 0
    else if ("$argv[1]" == "-v") then
        echo "$version"
        exit 0
    else if ("$argv[1]" == "--help" || "$argv[1]" == "-help") then
    cat <<EOF
 This script has the following usage:
   Usage: copy_qa_reasons.csh <db>

    This script copies the qa reasons back to MID production db from MRD.

EOF
    exit 0
    endif
endif

#
# get arguments
#
if ($#argv == 1) then
    set mrd_db = $1;
    set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $mrd_db`
else
   echo "Usage: copy_qa_reasons.csh <mrd_database>"
    exit 1
endif

set mid_db=`$MIDSVCS_HOME/bin/midsvcs.pl -s production-db`
set mid_user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $mid_db`

echo "------------------------------------------------------------------------"
echo "Starting...`/bin/date`"
echo "------------------------------------------------------------------------"
echo "  ORACLE_HOME:      $ORACLE_HOME"
echo "  MID database:     $mid_db"
echo "  MRD database:     $mrd_db"
echo ""


########################################################################
#
# Copy qa reasons tables 
#
########################################################################

echo "    Copy qa reasons tables ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus $mid_user@$mid_db <<EOF >! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    SET SERVEROUTPUT ON SIZE 100000
	SET ARRAYSIZE 2000
	
	COPY FROM $user@$mrd_db REPLACE qa_result_reasons USING -
	SELECT * FROM qa_result_reasons;
	
	COPY FROM $user@$mrd_db REPLACE qa_comparison_reasons USING -
	SELECT * FROM qa_comparison_reasons;

EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error copying qa reasons tables ...`/bin/date`"
    exit 1
endif


/bin/rm -f /tmp/t.$$.log

echo "------------------------------------------------------------------------"
echo "Finished $0 ...`/bin/date`"
echo "------------------------------------------------------------------------"
