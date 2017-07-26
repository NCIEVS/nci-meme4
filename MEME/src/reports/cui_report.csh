#!/bin/csh -f
#
#
#
source $ENV_HOME/bin/env.csh

set name=$0

if ($#argv != 1) then
    echo "usage: $0 CUI"
    exit 1
endif

set pid=$$

echo "-------------MRCONSO--------------------------------"
$MRD_HOME/bin/look.pl $1 MRCONSO.RRF | sed 's/^/MRCONSO: /'
echo "-------------MRSTY-------------------------------------"
$MRD_HOME/bin/look.pl $1 MRSTY.RRF | sed 's/^/MRSTY: /'
echo "-------------MRDEF-------------------------------------"
$MRD_HOME/bin/look.pl $1 MRDEF.RRF | sed 's/^/MRDEF: /'
echo "-------------MRHIER-------------------------------------"
$MRD_HOME/bin/look.pl $1 MRHIER.RRF | sed 's/^/MRHIER: /'
echo "-------------MRREL-------------------------------------"
$MRD_HOME/bin/look.pl $1 MRREL.RRF | $MRD_HOME/bin/showrel.pl | sed 's/^/MRREL: /'
echo "-------------MRSAT-------------------------------------"
$MRD_HOME/bin/look.pl $1 MRSAT.RRF | sed 's/^/MRSAT: /'
echo "-------------MRHIST--------------------------------------"
$MRD_HOME/bin/look.pl $1 MRHIST.RRF | sed 's/^/MRHIST: /'
