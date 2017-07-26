#!/bin/csh -f
#
# File:   atts_to_stringtab.csh
# Author: Brian Carlsen
#
# Remarks:  converts attributes.src. Call with -help for more info
#
# Changes:
# 02/24/2009 BAC (1-GCLNT): Better error reporting (for parallelization)
# 12/22/2005 BAC (1-719SM): use open ":utf8"  added to perl call
#
# Version info
set release=4
set version="2.0"
set authority="BAC";
set date="02/06/2004";

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

#
# Change on release to NLM
#
set perl=$PATH_TO_PERL
set HUGETMP=/tmp

if ($#argv > 0) then
    if ("-version" == $argv[1]) then
	echo "Release ${release}: version $version, $date ($authority)"
	exit 0
    else if ("$argv[1]" == "-v") then
	echo "$version"
	exit 0
    else if ("$argv[1]" == "--help" || "$argv[1]" == "-help") then
    cat <<EOF
 This script has the following usage:
   Usage: $0 <directory>

    This script takes a directory containing an attributes.src
    file and produces a stringtab.src file from it. 
    It numbers string_ids starting with 1.  Additionally,
    it converts the attributes.src file to contain
    <>Long_Attribute<>: references instead of the long
    strings.  Note:: DO NOT use the original attributes.src file
   
EOF
    exit 0
    endif
endif

if ($#argv != 1) then
    echo "Usage: $0 <directory>"
    exit 1
endif

# get variables
set dir=$1
if (! (-d $dir)) then
    echo "ERROR $dir is not a directory."
    exit 1
endif
set file=$dir/attributes.src
if (!(-e $file)) then
    echo "ERROR $file does not exist."
    exit 1
endif
set cur_dir=`pwd`

set offset=0
#if ($offset !~ [0-9]*) then
#    echo "offset must be an integer ($offset)."
#    exit 1
#endif

echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "file:         $file"
echo "dir:          $dir"
echo "cur_dir:      $cur_dir"


# move attributes to a backup
if (! -e ${file}.bak) then
    echo "Copy attributes file to ${file}.bak ...`/bin/date`"
    mv $file ${file}.bak
else
    echo "${file}.bak already exists, using it ...`/bin/date`"
endif

# Generate short attributes
echo "Generate new attributes.src and stringtab.src...`/bin/date`"
cd $dir
$perl -e ' \
  unshift @INC,"$ENV{ENV_HOME}/bin"; require "env.pl"; \
  use open ":utf8"; \
  $sid = $ARGV[0]; \
  open(A,"$ARGV[1]") || die "could not open $ARGV[0]: $! $?\n"; \
  open(STR,">stringtab.src") || \
    die "could not open stringtab.src: $! $?\n"; \
  while (<A>) {  \
    chop; \
    ($aid,$id,$level,$an,$av,$so,$st,$tbr,$rel,$supp,$type,$qual,$satui,$hash) = split /\|/; \
    if (length($av) <= 100) { \
       print "$_\n"; \
    } else { \
       $sid++; $rs = 1; $size = length($av); \
       while (length($av) > 1786) { \
	   print STR "$sid|",$rs++,"|$size|",substr($av,0,1786),"\n"; \
	   $av = substr($av,1786); \
       } \
       print STR "$sid|$rs|$size|$av\n"; \
       print "$aid|$id|$level|$an|<>Long_Attribute<>:$sid|$so|$st|$tbr|$rel|$supp|$type|$qual|$satui|$hash|\n"; \
    } \
  }; \
  close(STR);' $offset ${file}.bak >! $file
if ($status != 0) then
    echo "ERROR: something went wrong"
    exit 1
endif

cd $cur_dir

# Cleanup
#\rm -f ${file}.bak

echo "--------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "--------------------------------------------------------------"

