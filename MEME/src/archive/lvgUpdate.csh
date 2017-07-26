#!/bin/csh -f
#
# Updates a comparable ORF data set to use current version LVG
#  - updates LUIs
#  - Recomputes index files
#
# input: <orf/rrf flag> <dir>
# output: Updated files in <dir> with current LUI values
#

set required_vars = (ARCHIVE_HOME UMLS_ARCHIVE_ROOT JAVA_HOME)
foreach rv ($required_vars)
    if (`eval 'echo $?'$rv` == 0) then
        echo '$'$rv' must be set.'
    endif
end

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
echo "" 

#
# Check sort order of $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt
#
echo "    Check sort order of $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt ...`/bin/date`"
/bin/sort -c $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt >>& /dev/null
if ($status != 0) then
    echo "    Fix sort order of $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt ...`/bin/date`"
    /bin/sort -u -o $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt
endif

if ($mode == "orf") then

    #
    # Update MRCON LUIs
    #   CUI,LAT,TS,LUI,STT,SUI,STR,LRL
    #
    echo "    Fix LUIs for $dir/MRCON ...`/bin/date`"
    /bin/sort -t\| -k 6,6 -o $dir/MRCON.tmp1 $dir/MRCON
	if ($status != 0) then
		echo "ERROR sorting MRCON"; exit 1
	endif
    join -t\| -j1 6 -j2 1 -o 1.1 1.2 1.3 2.2 1.5 1.6 1.7 1.8 1.9 \
       $dir/MRCON.tmp1 $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt |\
       /bin/sort >! $dir/MRCON.fix
	if ($status != 0) then
		echo "ERROR joining MRCON"; exit 1
	endif
    /bin/mv -f $dir/MRCON.fix $dir/MRCON
	if ($status != 0) then
		echo "ERROR moving MRCON"; exit 1
	endif
    /bin/rm -f $dir/MRCON.tmp1

    #
    # Update MRSO LUIs
    #   CUI,LUI,SUI,SAB,TTY,CODE,SRL
    #
    echo "    Fix LUIs for $dir/MRSO ...`/bin/date`"
    /bin/sort -t\| -k 3,3 -o $dir/MRSO.tmp1 $dir/MRSO
	if ($status != 0) then
		echo "ERROR sorting MRSO"; exit 1
	endif
    join -t\| -j1 3 -j2 1 -o 1.1 2.2 1.3 1.4 1.5 1.6 1.7 1.8 \
       $dir/MRSO.tmp1 $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt |\
       /bin/sort >! $dir/MRSO.fix
	if ($status != 0) then
		echo "ERROR joining MRSO"; exit 1
	endif
    /bin/mv -f $dir/MRSO.fix $dir/MRSO
	if ($status != 0) then
		echo "ERROR moving MRSO"; exit 1
	endif
    /bin/rm -f $dir/MRSO.tmp1
    
    echo "    Set TS,STT ...`/bin/date`"
    setenv CLASSPATH "$ARCHIVE_HOME/lib/archive.jar:$ARCHIVE_HOME/lib/mms.jar:$ARCHIVE_HOME/lib/objects.jar:."
    $JAVA_HOME/bin/java -server -Xms100M -Xmx400M \
      gov.nih.nlm.umls.archive.ConceptRanker \
      -i ORF -o ORF $dir >&! /tmp/t.$$.log
    if ($status != 0) then
    	echo "ERROR"; cat /tmp/t.$$.log; exit 1
    endif
	/bin/mv -f $dir/MRCON.out $dir/MRCON
	/bin/mv -f $dir/MRSO.out $dir/MRSO
	
    echo "    Rebuild MRJOIN ...`/bin/date`"
    $ARCHIVE_HOME/bin/make_mrjoin.csh $dir >&! /tmp/t.$$.log
    if ($status != 0) then
    	echo "ERROR"; cat /tmp/t.$$.log; exit 1
    endif
    
    #
    # Update MRSAT LUIs
    #   CUI,LUI,SUI,CODE,ATN,SAB,ATV
    #
    echo "    Fix LUIs for $dir/MRSAT ...`/bin/date`"
    /bin/sort -t\| -k 3,3 -o $dir/MRSAT.tmp1 $dir/MRSAT
	if ($status != 0) then
		echo "ERROR sorting MRSAT"; exit 1
	endif
    join -t\| -j1 3 -j2 1 -o 1.1 2.2 1.3 1.4 1.5 1.6 1.7 1.8 \
       $dir/MRSAT.tmp1 $UMLS_ARCHIVE_ROOT/global/Global_SUILUI.txt \
       >! $dir/MRSAT.fix
	if ($status != 0) then
		echo "ERROR joining MRSAT"; exit 1
	endif
    $PATH_TO_PERL -ne 'split /\|/; print unless $_[2];' \
       $dir/MRSAT.tmp1 >> $dir/MRSAT.fix
	if ($status != 0) then
		echo "ERROR joining MRSAT 2"; exit 1
	endif
    /bin/sort -u -o $dir/MRSAT.fix{,}
	if ($status != 0) then
		echo "ERROR joining MRSAT"; exit 1
	endif
    /bin/mv -f $dir/MRSAT.fix $dir/MRSAT
	if ($status != 0) then
		echo "ERROR moving MRSAT"; exit 1
	endif
    /bin/rm -f $dir/MRSAT.tmp1

    #
    # Rebuild index files
    #
    echo "    Rebuild index files ...`/bin/date`"
    $ARCHIVE_HOME/bin/make_mrx.csh -$mode $dir >&! /tmp/t.$$.log
    if ($status != 0) then
    	echo "ERROR"; cat /tmp/t.$$.log; exit 1
    endif
    
    #
    # Recompute AMBIG files
    #
    echo "    Recompute AMBIG files ...`/bin/date`"
    $ARCHIVE_HOME/bin/make_ambig.csh -$mode $dir >&! /tmp/t.$$.log
    if ($status != 0) then
    	echo "ERROR"; cat /tmp/t.$$.log; exit 1
    endif
    
    #
    # Recompute MRCOLS/MRFILES
    #
    echo "    Recompute MRCOLS/MRFILES files ...`/bin/date`"
    $ARCHIVE_HOME/bin/make_mrcolsfiles.csh -$mode $dir >&! /tmp/t.$$.log
    if ($status != 0) then
    	echo "ERROR"; cat /tmp/t.$$.log; exit 1
    endif

endif

if ($mode == "rrf") then

    echo "Not yet supported..."
    exit 1

endif

echo "------------------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "------------------------------------------------------------------------"

