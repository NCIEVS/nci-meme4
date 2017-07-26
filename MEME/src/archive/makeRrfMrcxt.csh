#!/bin/csh -f

set build_sibs=true
set build_children=true
set compute_xc=true
set versioned_sabs=false
set add_unicode_bom=false
set write_stats=true
set max_contexts=false
set max_contexts_count=100

if ($#argv != 1) then
    echo "Usage: $0 <dir>"
    exit 1
endif

set source_dir=$1
set subset_dir=$1
#set source_list=

if ($?MMSYS_HOME != 1) then
    echo 'ERROR: $MMSYS_HOME must be set'
    exit 1   
endif

setenv CLASSPATH "${MMSYS_HOME}:$MMSYS_HOME/lib/objects.jar:$MMSYS_HOME/lib/mms.jar"

setenv JAVA_HOME $MMSYS_HOME/jre/solaris
$JAVA_HOME/bin/java -version >>& /dev/null
if ($status != 0) then
    setenv JAVA_HOME $MMSYS_HOME/jre/linux
endif
echo "-------------------------------------------------------"
echo "Starting ... `/bin/date`"
echo "-------------------------------------------------------"
echo "MMSYS_HOME:       $MMSYS_HOME"
echo "JAVA_HOME:        $JAVA_HOME"
echo "CLASSPATH:        $CLASSPATH"
echo "build_sibs        $build_sibs"
echo "build_children    $build_children"
echo "compute_xc        $compute_xc"
echo "versioned_sabs    $versioned_sabs"
echo "add_unicode_bom   $add_unicode_bom"
echo "write_stats:      $write_stats"
echo "max_contexts:     $max_contexts"
echo "max_contexts_count: $max_contexts_count"
echo "source_dir:       $source_dir"

set source_list_prop=
if ($?source_list == 1) then
    echo "source_list:      $source_list"
    set source_list_prop="-Dsource.list=$source_list"
endif
echo ""

echo -Xms200M -Xmx800M -Dbuild.sibs=$build_sibs \
 -Dbuild.children=$build_children \
 -Dcompute.xc=$compute_xc \
 -Dversioned.sabs=$versioned_sabs \
 -Dadd.unicode.bom=$add_unicode_bom \
 -Dwrite.mrcxt.file.statistics=$write_stats \
 -Dmax.contexts=$max_contexts \
 -Dmax.contexts.count=$max_contexts_count \
 -Dsource.dir=$source_dir \
 $source_list_prop gov.nih.nlm.mms.cxt.BatchMRCXTBuilder

$JAVA_HOME/bin/java -Xms200M -Xmx800M -Dbuild.sibs=$build_sibs \
 -Dbuild.children=$build_children \
 -Dcompute.xc=$compute_xc \
 -Dversioned.sabs=$versioned_sabs \
 -Dadd.unicode.bom=$add_unicode_bom \
 -Dwrite.mrcxt.file.statistics=$write_stats \
 -Dmax.contexts=$max_contexts \
 -Dmax.contexts.count=$max_contexts_count \
 -Dsource.dir=$source_dir \
 $source_list_prop gov.nih.nlm.mms.cxt.BatchMRCXTBuilder

echo "-------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "-------------------------------------------------------"