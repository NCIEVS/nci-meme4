#!/bin/csh -f
#
# This script generates the chronology.html
#
# File:    gen_chronology.csh
# Author:  Tim Kao 
#
# Usage: gen_chronology.csh <release>
#
# Options:
#     <release>: Required
#
source $ENV_HOME/bin/env.csh

#
# Parse arguments
#
if ($#argv != 1) then
    echo "Error: Bad argument"
    echo "Usage: $0 <release>"
    exit 1
endif

set release=$1

echo "<html><head>"
echo "<meta http-equiv="Content-Type" content="text/html">"
echo "<title>MRD - Generating a Release - Meta$release Chronology</title>"
echo "</head>"
echo "<body bgcolor='#ffffff'>"
echo "<center><h2><b>Meta$release Chronology</b></h2></center>"
echo "<font size="-1">Last Updated: `/bin/date +"%e-%b-%Y"`</font>"
echo "<hr width="100%"><i>Following is the chronology of file production in the MRD with the accompanying logs.</i>"
echo "<blockquote><dl>"

foreach f (`ls -rt /d5/MRD/$release/log | grep -v publish`)
  set base_file = $f:r
  echo "<pre><dt>$base_file<pre><dt>"
  echo "<dd>"
  cat /d5/MRD/$release/log/$f
  echo "</dd></pre>"
  echo ""
end

echo "</dl></blockquote><br></body></html>"
