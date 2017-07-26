#!/bin/csh -f
#
# Remakes MRX index files from current LVG
#
# input: <rrf/orf switch> <dir>
# output: new MRX files
#

set required_vars = (LVG_HOME)
foreach rv ($required_vars)
    if (`eval 'echo $?'$rv` == 0) then
        echo '$'$rv' must be set.'
    endif
end

if ($#argv != 2) then
    echo "Usage: $0 -(rrf|orf) <dir>"
    exit 1
endif

set dir=$2
if ("x-rrf" == "x$1") then
  set mode = rrf
  set file = MRCONSO.RRF
  if (! (-e $dir/MRCONSO.RRF)) then
    echo "$dir/MRCONSO.RRF must exist! "
    exit 1
  endif
else if ("x-orf" == "x$1") then
  set mode = orf
  set file = MRJOIN  
  if (! (-e $dir/MRJOIN)) then
    echo "$dir/MRJOIN must exist! "
    exit 1
  endif
endif
if ($?mode == 0) then
    echo "You must specify either -rrf or -orf"
    exit 1
endif

echo "------------------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "------------------------------------------------------------------------"
echo "dir:     $dir"
echo "file:    $file"
echo "mode:    $mode"

if ($mode == "orf") then
    #
    # Make MRXNS.ENG
    #
    echo "    Make MRXNS.ENG from MRJOIN ... `/bin/date`"
    /bin/rm -f $dir/MRXNS.ENG
    $PATH_TO_PERL -ne 'split /\|/; print if $_[7] eq "ENG"' $dir/MRJOIN |\
       $LVG_HOME/bin/norm -t:11 |\
       $PATH_TO_PERL -ne 'chomp; split /\|/; print "$_[7]|$_[12]|$_[0]|$_[1]|$_[2]|\n";' |\
       /bin/sort -T . -u -o $dir/MRXNS.ENG
    if ($status != 0) then
        echo "Error creating MRXNS.ENG from MRJOIN"
        exit 1
    endif

    #
    # Make MRXNW.ENG from MRXNS.ENG
    #
    echo "    Make MRXNW.ENG from MRXNS.ENG ... `/bin/date`"
    /bin/rm -f $dir/MRXNW.ENG
    $LVG_HOME/bin/wordInd -t:2 -F:1 -F:3 -F:4 -F:5 < $dir/MRXNS.ENG |\
       $PATH_TO_PERL -ne 'chomp; split /\|/; print "$_[0]|$_[4]|$_[1]|$_[2]|$_[3]|\n";' |\
       /bin/sort -T . -u -o $dir/MRXNW.ENG
    if ($status != 0) then
        echo "Error creating MRXNW.ENG from MRXNS.ENG"
        exit 1
    endif

    #
    # Make MRXW.<lat> from MRJOIN
    #
    echo "    Make MRXW.<lat> from MRJOIN ... `/bin/date`"
    foreach lat (`/bin/cut -d\| -f 8 $dir/MRJOIN | /bin/sort -u`)
       echo "      $lat ... `/bin/date`"
       $PATH_TO_PERL -ne 'chomp; split /\|/; print "$_[10]|$_[7]|$_[0]|$_[1]|$_[2]|\n" if $_[7] eq "'$lat'"; ' $dir/MRJOIN |\
          $LVG_HOME/bin/wordInd -t:1 -F:2 -F:3 -F:4 -F:5 |\
          $PATH_TO_PERL -ne 'chomp; split /\|/; print "$_[0]|$_[4]|$_[1]|$_[2]|$_[3]|\n";' |\
          /bin/sort -T . -u -o $dir/MRXW.$lat
        if ($status != 0) then
            echo "Error creating MRXW.$lat"
            exit 1
        endif
end
endif

if ($mode == "rrf") then
    #
    # Make MRXNS_ENG.rrf
    #
    echo "    Make MRXNS_ENG from MRCONSO.RRF ... `/bin/date`"
    /bin/rm -f $dir/MRXNS_ENG.RRF
    /bin/awk -F\| '$2 == "ENG" {print $2"|"$1"|"$4"|"$6"|"$15}' $dir/MRCONSO.RRF |\
       $LVG_HOME/bin/norm -t:5 |\
       /bin/awk -F\| '{print $1"|"$6"|"$2"|"$3"|"$4"|"}' |\
       /bin/sort -T . -u -o $dir/MRXNS_ENG.RRF
    if ($status != 0) then
        echo "Error creating MRXNS_ENG.RRF"
        exit 1
    endif

    #
    # Make MRXNW_ENG.RRF from MRXNS_ENG.RRF
    #
    echo "    Make MRXNW_ENG.RRF from MRXNS_ENG.RRF ... `/bin/date`"
    /bin/rm -f $dir/MRXNW_ENG.RRF
    $LVG_HOME/bin/wordInd -t:2 -F:1 -F:3 -F:4 -F:5 < $dir/MRXNS_ENG.RRF |\
       /bin/awk -F\| '{print $1"|"$5"|"$2"|"$3"|"$4"|"}' |\
       /bin/sort -T . -u -o $dir/MRXNW_ENG.RRF
    if ($status != 0) then
        echo "Error creating MRXNW_ENG.RRF"
        exit 1
    endif

    #
    # Make MRXW.<lat> from MRCONSO
    #
    echo "    Make MRXW.<lat> from MRJOIN ... `/bin/date`"
    foreach lat (`/bin/cut -d\| -f 8 $dir/MRJOIN | /bin/sort -u`)
       echo "      $lat ... `/bin/date`"
       /bin/awk -F\| '$2 == "ENG" {print $14"|"$2"|"$1"|"$4"|"$6}' $dir/MRCONSO.RRF |\
          $LVG_HOME/bin/wordInd -t:1 -F:2 -F:3 -F:4 -F:5 |\
          /bin/awk -F\| '{print $1"|"$5"|"$2"|"$3"|"$4"|"}' |\
          /bin/sort -T . -u -o $dir/MRXW_$lat.RRF
        if ($status != 0) then
            echo "Error creating MRXW_$lat.RRF"
            exit 1
        endif
end
endif

echo "------------------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "------------------------------------------------------------------------"

