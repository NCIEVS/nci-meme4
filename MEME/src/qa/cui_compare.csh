#!/bin/csh -f
#
# Script: cui_compare.csh
# Author: Brian Carlsen
# Uses: cui_report.csh and showrel.pl should be in the path
#
if ($#argv == "--help") then
    cat << EOF
    This script takes a CUI and two release directories and generates
    an XML report of the differences between the two.  This can be
    fed in to the MRD system to generate an editable QA report.
EOF
else if ($#argv != 5) then
    echo "Usage: $0 <cui> <old_release> <new_release> <old meta dir> <new meta dir>"
    exit 1
endif
set cui=$1
set old_release=$2
set new_release=$3
set old_dir=$4
set new_dir=$5

source $ENV_HOME/bin/env.csh

#
# Header
#
echo "<QAReport target="'"'"CUI"'"'" release="'"'"$new_release"'"'" previous="'"'"$old_release"'"'" previousMajor="'"'"$old_release"'"'" directory="'"'"$new_dir"'"'">"
 echo "<CompareTo name="'"'"Gold"'"'"></CompareTo>" 
 echo "<CompareTo name="'"'"Previous"'"'">"
 echo "<Differences></Differences>"

pushd $old_dir >>& /dev/null
$MRD_HOME/bin/cui_report.csh $1 >! /tmp/$1.old
cd $new_dir
$MRD_HOME/bin/cui_report.csh $1 >! /tmp/$1.new
popd >>& /dev/null
echo "<NotInOther>"
/bin/diff /tmp/$1.old /tmp/$1.new | /bin/grep '>' | sed 's/^> //' | $PATH_TO_PERL -e "%escapes = ('&'  => '&amp;','<'  => '&lt;','>'  => '&gt;','"'"'"'  => '&quot;','--' => '&#45;&#45;'); "'while(<>) {chomp; s/(\-\-|\"|\&|\>|\<)/$escapes{$1}/eg; print qq{  <QAResult name="cui_report" value="$_" count="1" />\n}; } '
echo "</NotInOther>"

echo "<Missing>"
/bin/diff /tmp/$1.old /tmp/$1.new | /bin/grep '<' | sed 's/^< //' | $PATH_TO_PERL -e "%escapes = ('&'  => '&amp;','<'  => '&lt;','>'  => '&gt;','"'"'"'  => '&quot;','--' => '&#45;&#45;'); "'while(<>) {chomp; s/(\-\-|\"|\&|\>|\<)/$escapes{$1}/eg; print qq{  <QAResult name="cui_report" value="$_" count="1" />\n}; }  '
echo "</Missing>"

#
# Footer
#
echo "</CompareTo>" 
 echo "<CompareTo name="'"'"PreviousMajor"'"'"></CompareTo>" 
echo "</QAReport>"

/bin/rm -f /tmp/$1.old /tmp/$1.new




