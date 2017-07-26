#!/bin/csh -f
#
# Remakes MRCOLS/MRFILES based on current data.
#
# input: <orf/rrf flag> <dir>
# output: Updated MRCOLS/MRFILES files in <dir>
#

set required_vars = (ARCHIVE_HOME JAVA_HOME)
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
  if (! (-e $dir/MRCONSO.RRF)) then
    echo "$dir/MRCONSO.RRF must exist! "
    exit 1
  endif
else if ("x-orf" == "x$1") then
  set mode = orf  
  if (! (-e $dir/MRCON)) then
    echo "$dir/MRCON must exist! "
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
echo "config file:    $ARCHIVE_HOME/etc/mrcolsfiles.dat"
echo "mr dir:         $dir"
echo "mode:           $mode"
echo ""

if ($mode == "orf") then
    echo "  Make MRCOLS/MRFILES ... `/bin/date`"
    setenv CLASSPATH "$ARCHIVE_HOME/lib/archive.jar:$ARCHIVE_HOME/lib/objects.jar:$ARCHIVE_HOME/lib/mms.jar"
    $JAVA_HOME/bin/java -server -Xms100M -Xmx400M -Dstats.config.file="$ARCHIVE_HOME/etc/mrcolsfiles.dat" gov.nih.nlm.umls.archive.OriginalMRCOLSFILESGenerator $dir
    if ($status != 0) then
        echo "Failed! "
        exit 1
    endif
endif

if ($mode == "rrf") then
    echo "  Make MRCOLS.RRF/MRFILES.RRF ... `/bin/date`"
    setenv CLASSPATH "$ARCHIVE_HOME/lib/archive.jar:$ARCHIVE_HOME/lib/objects.jar:$ARCHIVE_HOME/lib/mms.jar"
    $JAVA_HOME/bin/java -server -Xms100M -Xmx400M -Dstats.config.file="$ARCHIVE_HOME/etc/mrpluscolsfiles.dat" gov.nih.nlm.umls.archive.RichMRCOLSFILESGenerator $dir
    if ($status != 0) then
        echo "Failed! "
        exit 1
    endif
endif
    
echo "------------------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "------------------------------------------------------------------------"

