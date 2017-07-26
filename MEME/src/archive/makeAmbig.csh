#!/bin/csh -f
#
# makes ORF or RRF AMBIG SUI and LUI files.
#
# input: <orf/rrf flag> <dir>
# output: Updated AMBIG files in <dir>
#
if ($#argv != 2) then
    echo "Usage: $0 -(rrf|orf) <dir>"
    exit 1
endif

set dir=$2
if ("x-rrf" == "x$1") then
  set mode = rrf
  set ambiglui = AMBIGLUI.RRF
  set ambigsui = AMBIGSUI.RRF
  set file = MRCONSO.RRF
else if ("x-orf" == "x$1") then
  set mode = orf
  set ambiglui = AMBIG.LUI
  set ambigsui = AMBIG.SUI
  set file = MRCON
endif

if ($?mode == 0) then
    echo "You must specify either -rrf or -orf"
    exit 1
endif

if (! (-e $dir/$file)) then
  echo "$dir/$file must exist! "
  exit 1
endif

echo "------------------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "------------------------------------------------------------------------"
echo "dir: $dir"
echo "sui: $ambigsui"
echo "lui: $ambigsui"

echo "    Make $dir/$ambigsui"
(/bin/cut -d\| -f 1,6 $dir/$file; echo "C999999999|S99999999") |\
  /bin/sort -u | /bin/sort -t\| -k 2,2 |\
  $PATH_TO_PERL -ne ' chop; \
    ($cui, $sui) = split /\|/; \
    if ($psui && $psui ne $sui) { \
      if (scalar(@cuis)>1) { \
        foreach $cui (sort @cuis) {print "$psui|$cui|\n"; } \
      } \
      @cuis = ($cui); \
    } else { \
      push @cuis, $cui; \
    } \
    $psui = $sui; ' | /bin/sort -u >&! $dir/$ambigsui

echo "    Make $dir/$ambiglui"
(/bin/cut -d\| -f 1,4 $dir/$file; echo "C999999999|L99999999") |\
  /bin/sort -u | /bin/sort -t\| -k 2,2 |\
  $PATH_TO_PERL -ne ' chop; \
    ($cui, $lui) = split /\|/; \
    if ($plui && $plui ne $lui) { \
      if (scalar(@cuis)>1) { \
        foreach $cui (sort @cuis) {print "$plui|$cui|\n"; } \
      } \
      @cuis = ($cui); \
    } else { \
      push @cuis, $cui; \
    } \
    $plui = $lui; ' | /bin/sort -u >&! $dir/$ambiglui


echo "------------------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "------------------------------------------------------------------------"
