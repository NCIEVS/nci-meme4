#!/bin/csh -f
#
# Input: <input dir> <output dir>
# Output: .RRF files
#

if ($?JAVA_HOME == 0) then
    echo '$JAVA_HOME must be set'
    exit 1
endif

if ($?PATH_TO_PERL == 0) then
    echo '$PATH_TO_PERL must be set'
    exit 1
endif

set usage="$0 <input dir> <output dir> <mmsys dir>"
if ($#argv == 3) then
    if (-e `pwd`/$1) then 
       set input=`pwd`/$1
    else
       set input=$1
    endif
    if (-e `pwd`/$2) then
       set dest=`pwd`/$2
    else
       set dest=$2
    endif
    if (-e `pwd`/$3) then
       set mmsys=`pwd`/$3
    else
       set mmsys=$3
    endif
else
    echo "Wrong number of arguments"
    echo $usage
    exit 1
endif

echo "------------------------------------------------------------"
echo "Starting .. `/bin/date`"
echo "------------------------------------------------------------"
echo "JAVA_HOME:          $JAVA_HOME"
echo "PATH_TO_PERL:       $PATH_TO_PERL"
echo "IMAGE dir:          $input"
echo "Output dir:         $dest"
echo "MMSYS Dir:          $mmsys"
echo ""

#
# get sampleimage filename
#
echo "    Get version information ...`/bin/date`"
set version=`grep umls.release.name $mmsys/release.dat | sed 's/.*=//'`
set lc_version=`echo $version | $PATH_TO_PERL -ne 'print lc("'$version'")'`
set uc_version=`echo $version | $PATH_TO_PERL -ne 'print uc("'$version'")'`
echo "      version=$version"

#
# Create config file
#
echo "   Create properties file ... `/bin/date`"
$PATH_TO_PERL -pe ' \
       s/^(.*)\.remove_utf8=true/$1.remove_utf8=false/; \
       s/^(mmsys_input_stream)=.*/$1=gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysInputStream/; \
       s/^(mmsys_output_stream)=.*/$1=gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream/; \
       s/^(.*)\.selected_sources=.*/$1.selected_sources=RADLEX|RADLEX/; \
       s/^(.*)\.remove_selected_sources=.*/$1.remove_selected_sources=false/; ' \
       $mmsys/config/$uc_version/user.a.prop >! /tmp/full.$$.prop

#
# Generate full subset
#
echo "    Generating subset - remove nothing - RRF format ... `/bin/date`"
echo "      in $dest"
echo "      input = $input"
echo "      output = $dest"
echo "      config = /tmp/full.$$.prop"
pushd $mmsys >>& /dev/null
setenv CLASSPATH ".:lib/jpf-boot.jar"

#
# Try Linux
#
$mmsys/jre/linux/bin/java -version >>& /dev/null
if ($status == 0) then
    $mmsys/jre/linux/bin/java -Djava.awt.headless=true \
     -Djpf.boot.config=$mmsys/etc/subset.boot.properties \
     -Dlog4j.configuration="file://$mmsys/etc/subset.log4j.properties" \
     -Dfile.encoding=UTF-8 -Xms300M -Xmx1000M  \
     -Dinput.uri=$input -Doutput.uri=$dest \
     -Dmmsys.config.uri=/tmp/full.$$.prop \
     -Dscript_type=.sh \
     -Dunzip.native=true -Dunzip.path=/usr/bin/unzip \
     org.java.plugin.boot.Boot | sed 's/^/      /'
else
    $mmsys/jre/solaris/bin/java -Djava.awt.headless=true \
     -Djpf.boot.config=$mmsys/etc/subset.boot.properties \
     -Dlog4j.configuration="file://$mmsys/etc/subset.log4j.properties" \
     -Dfile.encoding=UTF-8 -Xms300M -Xmx1000M  \
     -Dinput.uri=$input -Doutput.uri=$dest \
     -Dmmsys.config.uri=/tmp/full.$$.prop \
     -Dscript_type=.sh \
     -Dunzip.native=true -Dunzip.path=/usr/bin/unzip \
     org.java.plugin.boot.Boot | sed 's/^/      /'
endif

popd


echo "    Cleanup ...`/bin/date`"
/bin/rm -f /tmp/full.$$.prop

echo "------------------------------------------------------------"
echo "Finished .. `/bin/date`"
echo "------------------------------------------------------------"
