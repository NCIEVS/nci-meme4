#!/bin/csh -f
#
# Author: Tun Tun Naing, Brian Carlsen
# Check required environment variables
#
#
# Parse arguments
#
if ($#argv == 0) then
    echo "Usage: $0 <old meta> <new meta> <output dir>"
    exit 1
endif

if ($#argv == 3 ) then
    set old=$1
    set new=$2
    set dir=$3
else
    echo "Error: Bad argument"
    echo "Usage: $0 <old meta> <new meta> <output dir>"
    exit 1
endif

compute_update.csh $old $new MRCONSO.RRF CUI,AUI,SUPPRESS $dir
compute_update.csh $old $new MRSTY.RRF CUI $dir
compute_update.csh $old $new MRSAT.RRF CUI,ATUI $dir
compute_update.csh $old $new MRREL.RRF CUI1,RUI,RG $dir
compute_update.csh $old $new MRHIER.RRF CUI,AUI $dir
