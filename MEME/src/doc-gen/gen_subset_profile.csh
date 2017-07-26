#!/bin/csh -f

#
# This script generates a profile report for a subset
#
# File:    gen_subset_profile.csh
# Author:  Tim Kao 
#
# Usage: gen_subset_profile.csh <release_dir>
#
# Options:
#     <release_dir>: Required
#
source $ENV_HOME/bin/env.csh

#
# Parse arguments
#
if ($#argv != 1) then
    echo "Error: Bad argument"
    echo "Usage: $0 <release_dir>"
    exit 1
endif

set release_dir=$1

echo "Profile Report for $1"
echo "Generated on `date`"
echo ""
echo "-----------------------------------------------------"

foreach f (`ls $release_dir | grep RRF`)
  set base_file = $f:r
  set md5 = `cat $release_dir/$f | md5`
  set line_cnt = `wc -l $release_dir/$f`

  echo "File:        $base_file"
  echo "MD5:         $md5"
  echo "Line Count:  $line_cnt[1]"      
  
  set sab_cnt = `grep $base_file $release_dir/MRFILES.RRF | grep -c SAB`
  if ($sab_cnt > 0) then
    echo "Tally By SAB:"
    set sab_string = `grep $base_file $release_dir/MRFILES.RRF | cut -d\| -f 3 - | sed "s/,/ /g"`

    set sab_position = 1
    foreach v ($sab_string)
      if ($v == "SAB" || $v == "MAPSETSAB" || $v == "RSAB") then
        break
       endif
      @ sab_position++
    end
    
    cat $release_dir/$f | tallyfield.pl '''$'$sab_position''  
  endif
  echo ""
  echo "-----------------------------------------------------"
end

