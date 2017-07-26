#!/bin/csh -f
#
# Script:    report_qa_diff.csh
# Author:    Brian Carlsen
#
# Remarks:   This script is used to generate diffs between
#            two QA tables with the following fields
#                test_name,test_value,test_count
#
#            This is a helper script used by QA.MR*.csh
#
# Version Information:
#   01/22/2002 (1.0):  Ported to MRD
#
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
    endif
end

set usage="Usage: $0 <database> <qa table 1> <qa table 2>"

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

 This script is used by the MR file QA scripts to compare
 two QA tables in the MRD.

EOF
	exit 0
    else
        echo "ERROR: $usage"
	exit 1
    endif

else if ($#argv != 3) then
    echo $usage
    exit 1
endif

set db=$1
set qa1=$2
set qa2=$3
set mu=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`


#
# Generate diff table
#

$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select a.test_name||'~'||a.test_value||'~'|| a.test_count||'~'|| b.test_count ||'~'|| (a.test_count-b.test_count) as col from $qa1 a, $qa2 b where a.test_name=b.test_name and nvl(a.test_value,'null')=nvl(b.test_value,'null') and a.test_count!=b.test_count" >! /tmp/${qa1}.diff
if ($status != 0) then
    echo "ERROR dumping shared keys"
    exit 1
endif

$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select test_name||'~'|| test_value||'~'|| test_count as col from $qa1 a where test_value not in (select test_value from $qa2 b where a.test_name=b.test_name)" \
    >! /tmp/${qa1}.12
if ($status != 0) then
    echo "ERROR dumping keys in $qa1 not in $qa2"
    exit 1
endif

$MEME_HOME/bin/dump_table.pl -u $mu -d $db -q \
  "select test_name||'~'|| test_value||'~'|| test_count as col from $qa2 a where test_value not in (select test_value from $qa1 b where a.test_name=b.test_name)" \
    >! /tmp/${qa1}.21
if ($status != 0) then
    echo "ERROR dumping keys in $qa2 not in $qa1"
    exit 1
endif

#
# Report differences, 
# unless we are dealing with the ORF target (then only if count > 10K)
#
echo ""
echo ""
echo "<Differences>"
if (`echo $qa1 | grep -c -i _orf_` != 0) then
    set ct=`$PATH_TO_PERL -ne 'split /~/; print if $_[4]>10000' /tmp/$qa1.diff | wc -l`
    if ($ct == 0) then
	echo "    There are no diffs between $qa1 and $qa2 where keys match"
    else
	echo "     Differences between $qa1 and $qa2 - ORF"
	echo ""
	printf "    %25s%35s%10s%10s%10s\n" "Test" "Value" "#1" "#2" "diff"
	printf "    %25s%35s%10s%10s%10s\n" "------------------------" "----------------------------------" "---------" "---------" "---------"
        $PATH_TO_PERL -ne 'chop; @f=split/\~/; \
            (printf "    %25s%35s%10s%10s%10s\n",@f) if $f[4]>10000;' /tmp/$qa1.diff
	$PATH_TO_PERL -ne "%escapes = ('&'  => '&amp;','<'  => '&lt;','>'  => '&gt;','"'"'"'  => '&quot;','--' => '&#45;&#45;');"'chop; s/(\-\-|\"|\&|\>|\<)/$escapes{$1}/eg;@f=split/\~/; \
		print "<QAComparison name=\"$f[0]\" value=\"$f[1]\" count=\"$f[2]\" comparisoncount=\"$f[3]\" />\n" if $f[4]>10000' /tmp/$qa1.diff
    endif
else
    set ct=(`wc -l /tmp/$qa1.diff`)
    if ($ct[1] == 0) then
	echo "    There are no diffs between $qa1 and $qa2 where keys match"
    else
	echo "     Differences between $qa1 and $qa2"
	echo ""
	printf "    %25s%35s%10s%10s%10s\n" "Test" "Value" "#1" "#2" "diff"
	printf "    %25s%35s%10s%10s%10s\n" "------------------------" "----------------------------------" "---------" "---------" "---------"
	$PATH_TO_PERL -ne 'chop; @f=split/\~/; \
	    printf "    %25s%35s%10s%10s%10s\n",@f;' /tmp/$qa1.diff
	$PATH_TO_PERL -ne "%escapes = ('&'  => '&amp;','<'  => '&lt;', \
            '>'  => '&gt;','"'"'"'  => '&quot;','--' => '&#45;&#45;');"' \
            chop; s/(\-\-|\"|\&|\>|\<)/$escapes{$1}/eg;@f=split/\~/; \
	    print "<QAComparison name=\"$f[0]\" value=\"$f[1]\" count=\"$f[2]\" comparisoncount=\"$f[3]\" />\n";' /tmp/$qa1.diff
    endif

endif
echo "</Differences>"


#
# Report entries in 1 not 2
#
echo ""
echo ""
echo "<NotInOther>"
set ct=(`wc -l /tmp/$qa1.12`)
if ($ct[1] == 0) then
    echo "    There are no keys in $qa1 not found in $qa2"
else
    echo "     Keys in $qa1 not in $qa2"
    echo ""
    printf "    %25s%35s%10s\n" "Test" "Value" "Count"
    printf "    %25s%35s%10s%10s%10s\n" "------------------------" "----------------------------------" "---------"
    $PATH_TO_PERL -ne 'chop; @f=split/\~/; \
	printf "    %25s%35s%10s\n",@f;' /tmp/$qa1.12
    $PATH_TO_PERL -ne "%escapes = ('&'  => '&amp;','<'  => '&lt;','>'  => '&gt;','"'"'"'  => '&quot;','--' => '&#45;&#45;');"'chop; s/(\-\-|\"|\&|\>|\<)/$escapes{$1}/eg;@f=split/\~/; \
	print "<QAResult name=\"$f[0]\" value=\"$f[1]\" count=\"$f[2]\" />\n"'  /tmp/$qa1.12
endif
echo "</NotInOther>"

#
# Report entries in 2 not 1
#
echo ""
echo ""
echo "<Missing>"
set ct=(`wc -l /tmp/$qa1.21`)
if ($ct[1] == 0) then
    echo "    There are no keys in $qa2 not found in $qa1"
else
    echo "     Keys in $qa2 not in $qa1"
    echo ""
    printf "    %25s%35s%10s\n" "Test" "Value" "Count"
    printf "    %25s%35s%10s%10s%10s\n" "------------------------" "----------------------------------" "---------"
    $PATH_TO_PERL -ne 'chop; @f=split/\~/; \
	printf "    %25s%35s%10s\n",@f;' /tmp/$qa1.21
    $PATH_TO_PERL -ne "%escapes = ('&'  => '&amp;','<'  => '&lt;','>'  => '&gt;','"'"'"'  => '&quot;','--' => '&#45;&#45;');"'chop; s/(\-\-|\"|\&|\>|\<)/$escapes{$1}/eg;@f=split/\~/; \
	print "<QAResult name=\"$f[0]\" value=\"$f[1]\" count=\"$f[2]\" />\n"'  /tmp/$qa1.21
endif
echo "</Missing>"

echo ""

#
# Clean up
# 
/bin/rm /tmp/$qa1.{diff,12,21}
