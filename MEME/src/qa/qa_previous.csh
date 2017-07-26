#!/bin/csh -f
#
# Script:    qa_previous.csh
# Author:    BAC
#
# Generates QA counts for a previous release
# them to GOLD and previous counts
#
# Version Info
# 09/09/2002:  Reworked for MRD Application server
#
set release="1"
set version=".5"
set version_authority="BAC"
set version_date="08/10/2004"

source $ENV_HOME/bin/env.csh

#
# Parse arguments
#
if ($#argv != 6) then
    echo "ERROR: Wrong number of arguments"
    echo "Usage: $0 <db> <minor dir> <minor release> <major dir> <major release> <target>"
    exit 1
endif

set db=$1
set minor_dir = $2
set minor_release = $3
set major_dir = $4
set major_release = $5
set target = $6

echo "----------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "----------------------------------------------"
echo "db:                     $db"
echo "previous minor dir:     $minor_dir"
echo "previous minor release: $minor_release" 
echo "previous major dir:     $major_dir"
echo "previous major release: $major_release" 
echo "target:                 $target"
echo ""

echo "    Generating counts for $minor_release $target ... `/bin/date`"
$MRD_HOME/bin/qa_counts.csh $minor_dir $db $minor_release $target &

if ($minor_release != $major_release) then
    echo "    Generating counts for $major_release $target ... `/bin/date`"
    $MRD_HOME/bin/qa_counts.csh $major_dir $db $major_release $target &
endif
wait

echo ""
echo "----------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "----------------------------------------------"


