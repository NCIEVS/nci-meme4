#!/bin/csh -f
#
# Script:    qa_report.csh
# Author:    BAC
#
# Runs the qa script and generates comparison reports
# The exact reports generated are based on the target.
# The available reports are:
#
# . Compare current with gold script (versionless)
# . Compare current with gold script (with version)
# . Compare current with previous minor release (versionless)
# . Compare current with previous minor release (with version)
# . Compare current with previous major release (versionless)
# . Compare current with previous major release (with version)
#
# Version Information
# 1.0.5 09/09/2002: Re-organized to work with MRD App server
set release=1
set version=0.5
set authority=BAC
set version_date="09/09/2002"

source $ENV_HOME/bin/env.csh

#
# Check required variables
#
set required_vars = ("MRD_HOME" "ORACLE_HOME")
foreach rv ($required_vars)
    if (`eval 'echo $?'$rv` == 0) then
        echo 'ERROR: $'$rv' must be set.'
    endif
end

#
# Set Environment, Aliases
# 
set usage="Usage: $0 <dir> <db> <release> <target> <prev_major_release> <prev_minor_release> <previous_release_dir>"
set bin=$MRD_HOME/bin

#
# Parse arguments
#
if ($#argv == 7) then
    set dir=$1
    set db=$2
    set release=$3
    set target=$4
    set major_release=$5
    set minor_release=$6
    set previous_release_dir=$7
else if ($#argv == 6) then
    set dir=$1
    set db=$2
    set release=$3
    set target=$4
    set major_release=$5
    set minor_release=$6
    set previous_release_dir=$MRD_HOME/${minor_release}/META
else
    echo "ERROR: Wrong number of arguments"
    echo "$usage"
    exit 1
endif

set x=`$PATH_TO_PERL -e 'print 1 if "MRAUI DOC MRDOC MRCONSO MRX MRRANK MRSTY MRDEF AMBIG MRCUI MRCOC MRHIER MRHIST MRCXT MRREL MRSAT MRLO MRMAP MRHIST MRSAB MRFILESCOLS MetaMorphoSys ORF" =~ /$ARGV[0]/;' $target` 
if ($x != 1) then
    echo "ERROR: Illegal target name ($target)"
    exit 1
endif

echo "-------------------------------------------------------" 
echo "Starting $0 ... `/bin/date`" 
echo "-------------------------------------------------------" 
echo "Database:       $db" 
echo "Directory:      $dir"
echo "Release:        $release"
echo "Target:         $target"
echo "Previous:       $minor_release"
echo "Previous Major: $major_release"
echo "" 

#
# Generate target QA counts
#
$MRD_HOME/bin/qa_counts.csh $dir $db $release $target
if ($status != 0) exit 1

echo "<QAReport target="'"'"$target"'"'" release="'"'"$release"'"'" previous="'"'"$minor_release"'"'" previousMajor="'"'"$major_release"'"'" database="'"'"$db"'"'" directory="'"'"$dir"'"'">" >! qa_$target.xml


#
# Perform QA checks
#
$MRD_HOME/bin/qa_checks.csh $dir $db $target full $previous_release_dir >! /tmp/$release.$target
$PATH_TO_PERL -e "%escapes = ('&'  => '&amp;','<'  => '&lt;','>'  => '&gt;','"'"'"'  => '&quot;','--' => '&#45;&#45;');"'while(<>) {chomp; s/(\-\-|\"|\&|\>|\<)/$escapes{$1}/eg;if (s/^\s+Verify\s/<Check name="/ ){ print "</Error>\n" if($inerror); print "</Check>\n" if $check; $check = 1; print "$_\">\n"; $inerror=0; }  elsif (s/^ERROR\:\s/<Error name="/ ){print "$_\">\n"; $inerror=1; } elsif (s/^WARNING\:\s/<Warning value="/ ){print "$_\"/>";} elsif($inerror){ print "<Value>$_</Value>\n";}} print "</Error>\n" if($inerror); print "</Check>\n";' /tmp/$release.$target >> qa_$target.xml

cat /tmp/$release.$target

/bin/rm -f /tmp/$release.$target

#
# Generate Reports
#
echo "" 
echo "    Compute differences ... `/bin/date`" 
echo "" 

#
# Log GOLD-REAL differences (versionless)
#
if ($target == "DOC" || $target == "ORF") then
    echo "    Report $target real-gold differences (versionless)"
    echo "    ----------------------------------------------------"
    echo "<CompareTo name="'"'"Gold"'"'">" >> qa_$target.xml
    echo "No gold script checks for $target" >> qa_$target.xml
    echo "</CompareTo>" >> qa_$target.xml
else
set x=`$PATH_TO_PERL -e 'print 1 if "MRAUI MRDOC MRCONSO MRX MRRANK MRSTY MRDEF AMBIG MRCUI MRCOC MRHIER MRHIST MRCXT MRREL MRSAT MRLO MRMAP MRHIST MRSAB MRFILESCOLS MetaMorphoSys" =~ /$ARGV[0]/;' $target` 
if ($x) then
    echo "    Report $target real-gold differences (versionless)" 
    echo "    ----------------------------------------------------" 
    echo "<CompareTo name="'"'"Gold"'"'">" >> qa_$target.xml
    $bin/report_qa_diff.csh $db qa_${target}_$release qa_${target}_${release}_gold >! /tmp/qa_diff_$target
    $PATH_TO_PERL -ne 'print if /^</;' /tmp/qa_diff_$target >> qa_$target.xml
    echo "</CompareTo>" >> qa_$target.xml
    $PATH_TO_PERL -ne 'print unless /^</;'  /tmp/qa_diff_$target
    echo "" 
    /bin/rm -f /tmp/qa_diff_$target
endif
endif
#
# Log REAL-MINOR_PREF differences (Versionless)
#
set x=`$PATH_TO_PERL -e 'print 1 if "MRAUI MRDOC MRCONSO MRX MRRANK MRSTY MRDEF AMBIG MRCUI MRCOC MRHIER MRHIST MRCXT MRREL MRSAT MRLO MRMAP MRHIST MRSAB MRFILESCOLS MetaMorphoSys" =~ /$ARGV[0]/;' $target` 
if ($x) then

    echo "    Report $target real-minor differences (versionless)" 
    echo "    -----------------------------------------------------" 
    echo "<CompareTo name="'"'"Previous"'"'">" >> qa_$target.xml
    $bin/report_qa_diff.csh $db qa_${target}_$release qa_${target}_$minor_release >! /tmp/qa_diff_$target
    $PATH_TO_PERL -ne 'print if /^</;' /tmp/qa_diff_$target >> qa_$target.xml
    echo "</CompareTo>" >> qa_$target.xml
    $PATH_TO_PERL -ne 'print unless /^</;'  /tmp/qa_diff_$target
    echo "" 
    /bin/rm -f /tmp/qa_diff_$target

endif

if ($target == "DOC" || $target == "ORF") then
    echo "    Report $target real-minor differences (versionless)"
    echo "    -----------------------------------------------------"
    echo "<CompareTo name="'"'"Previous"'"'">" >> qa_$target.xml
    echo "No checks for $target" >> qa_$target.xml
    echo "</CompareTo>" >> qa_$target.xml
endif
#
# Only perform these comparisons if minor!=major
#
if ($major_release != $minor_release) then

    #
    # Log REAL-MAJOR_PREF differences (Versionless)
    #
    set x=`$PATH_TO_PERL -e 'print 1 if "MRAUI MRDOC MRCONSO MRX MRRANK MRSTY MRDEF AMBIG MRCUI MRCOC MRHIER MRHIST MRCXT MRREL MRSAT MRLO MRMAP MRHIST MRSAB MRFILESCOLS MetaMorphoSys" =~ /$ARGV[0]/;' $target` 
    
if ($x) then
    
	echo "    Report $target real-major differences (versionless)" 
	echo "    -----------------------------------------------------" 
	echo "<CompareTo name="'"'"PreviousMajor"'"'">" >> qa_$target.xml
	$bin/report_qa_diff.csh $db qa_${target}_$release qa_${target}_$major_release  >! /tmp/qa_diff_$target
	$PATH_TO_PERL -ne 'print if /^</;' /tmp/qa_diff_$target >> qa_$target.xml
	echo "</CompareTo>" >> qa_$target.xml
	$PATH_TO_PERL -ne 'print unless /^</;'  /tmp/qa_diff_$target
	echo "" 
	/bin/rm -f /tmp/qa_diff_$target
endif
if ($target == "DOC" || $target == "ORF") then
        echo "    Report $target real-major differences (versionless)"
        echo "    -----------------------------------------------------"
        echo "<CompareTo name="'"'"PreviousMajor"'"'">" >> qa_$target.xml
        echo "No checks for $target" >> qa_$target.xml
        echo "</CompareTo>" >> qa_$target.xml
endif
endif

echo "</QAReport>" >> qa_$target.xml
echo "" 
echo "-------------------------------------------------------" 
echo "Finished $0 ... `/bin/date`" 
echo "-------------------------------------------------------" 
