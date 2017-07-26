#!/bin/csh -f
#
# Script:    qa_counts.csh
# Author:    BK, BAC
#
# Generates QA counts for the specified target.
#
# Changes
# 08/05/2010 BAC (1-RDJQ1): add code for Optimization target
# 03/04/2009 BAC (noticket): small refinement to attr/rel suppr tallie
# 03/02/2009 SL:Added Count by SAB|REL|RELA|SUPPRESSIBLE (sab_rel_rela_supp_tally)
# 02/12/2009 SL (Per TTN): Fixed the SAB, SATUI query for MRSAT
# 01/07/2009 TTN (1-K2M9X): use the list of MRXW* files for languages
# 12/10/2007 TTN (1-F7U5S): fix extra " in  SAB,SATUI tally
# 09/10/2007 TTN (1-F7U5S): the patterns used in sed tools doesn't work in linux and also change it to use tallyfield.pl
# 07/17/2007 SL ( 1-EG2V7  -- MRSAB gold build difference in CXTY
# 07/17/2007 SL  (1-EG3HV/EG3IT)  -- Error in MRSAT validation (sab_satui_tally)
# 01/25/2007 BAC (1-DCSRV): Update -Xmx parameter in java calls
# 08/01/2006 BAC (1-BTEW5): If jre/solaris fails, try jre/linux
# 03/09/2006 TTN (1-AM5U9): add ts,stt,ispref counts in MetaMorphoSys
# 09/26/2012 MAJ: Added ActiveSubset processing
#
# Version Information
# 1.0.5 09/17/2002: Harvested from QA.AMBIG.csh
# 1.0.6 03/08/2005: Add MRAUI counts

set release=1
set version=0.6
set authority=BAC
set version_date="03/08/2005"

source $ENV_HOME/bin/env.csh

#
# Check required variables
#
set required_vars = ("MRD_HOME")
foreach rv ($required_vars)
    if (`eval 'echo $?'$rv` == 0) then
        echo 'ERROR: $'$rv' must be set.'
    endif
end

#
# Set Environment, Aliases
#
set usage="Usage: $0 <dir> <db> <release> <target>"
set bin=$MRD_HOME/bin
setenv PATH "/bin:/usr/bin:/usr/local/bin"
alias sort sort -T .
alias tallyfield.pl $MRD_HOME/bin/tallyfield.pl

#
# Parse arguments
#
if ($#argv != 4) then
    echo "ERROR: Wrong number of arguments"
    echo "$usage"
    exit 1
endif

set dir=$1
set db=$2
set release=$3
set target=$4

if ($target == "DOC") then
   set qa_file = qa_doc_$release
   touch $qa_file

else if ($target == "ActiveSubset") then
   set qa_file = qa_activesubset_$release
   touch $qa_file

else if ($target == "Optimization") then
   set qa_file = qa_optimization_$release
   touch $qa_file

else if ($target == "MRAUI") then
   #
   # Handle environment
   #
   set mraui = $dir/MRAUI.RRF
   if (!(-e $mraui)) then
       echo "ERROR: required file $mraui cannot be found"
       exit 1
   endif
   set qa_file = qa_mraui_$release

   #
   # Count all lines
   #
   set cnt = `cat $mraui | wc -l`
   if ($status != 0) exit 1
   echo "row_cnt~~$cnt" >! $qa_file

   #
   # Unique AUI1 count
   #
   set a1cnt  = `cut -d\| -f1 $mraui | sort -u | wc -l`
   if ($status != 0) exit 1
   echo "aui1_cnt~~$a1cnt" >> $qa_file

   #
   # Unique AUI2 count
   #
   set a2cnt  = `cut -d\| -f7 $mraui | sort -u | wc -l`
   if ($status != 0) exit 1
   echo "aui2_cnt~~$a2cnt" >> $qa_file

   #
   # Unique CUI1 count
   #
   set c1cnt  = `cut -d\| -f2 $mraui | sort -u | wc -l`
   if ($status != 0) exit 1
   echo "cui1_cnt~~$c1cnt" >> $qa_file

   #
   # Unique CUI2 count
   #
   set c2cnt  = `cut -d\| -f8 $mraui | sort -u | wc -l`
   if ($status != 0) exit 1
   echo "cui2_cnt~~$c2cnt" >> $qa_file

   #
   # Unique AUI1,CUI1,AUI2,CUI2 count
   #
   set cnt  = `cut -d\| -f1,2,7,8 $mraui | sort -u | wc -l`
   if ($status != 0) exit 1
   echo "a1_c1_a2_c2_cnt~~$cnt" >> $qa_file

   #
   # rel Tally
   #
   tallyfield.pl '$4' $mraui |\
   $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/rel_tally\~/' >> $qa_file
   if ($status != 0) exit 1

   #
   # rela Tally
   #
   tallyfield.pl '$5' $mraui |\
   $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/rela_tally\~/' >> $qa_file
   if ($status != 0) exit 1

   #
   # ver Tally
   #
   tallyfield.pl '$3' $mraui |\
   $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/ver_tally\~/' >> $qa_file
   if ($status != 0) exit 1

   #
   # ver,rel,rela Tally
   #
   tallyfield.pl '$3$4$5' $mraui |\
   $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/ver_rel_rela_tally\~/' >> $qa_file
   if ($status != 0) exit 1

else if ($target == "AMBIG") then
    #
    # Handle environment
    #
    set ambig_sui=$dir/AMBIGSUI.RRF
    set ambig_lui=$dir/AMBIGLUI.RRF
    if (! -e $ambig_sui) then
	echo "ERROR: required file $ambig_sui cannot be found"
	exit 1
    endif
    if (! -e $ambig_lui) then
	echo "ERROR: required file $ambig_lui cannot be found"
	exit 1
    endif
    set qa_file = qa_ambig_$release

    #
    # AMBIG.SUI sui_cnt
    #
    #set sui_cnt=(`wc -l $ambig_sui`)
    set sui_cnt=(`cut -d\| -f 1 $ambig_sui | sort -u | wc -l`)
    if ($status != 0) exit 1
    echo "sui_cnt~AMBIGSUI~$sui_cnt[1]" >! $qa_file

    #
    # AMBIG.LUI lui_cnt
    #
    #set lui_cnt=(`wc -l $ambig_lui`)
    set lui_cnt=(`cut -d\| -f 1 $ambig_lui | sort -u | wc -l`)
    if ($status != 0) exit 1
    echo "lui_cnt~AMBIGLUI~$lui_cnt[1]" >> $qa_file

    #
    # AMBIG.SUI CUI,SUI count
    #
    set cs_cnt=`$PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; foreach $c (@c) { print "$_[0]|$c\n";}' $ambig_sui | wc -l`
    if ($status != 0) exit 11
    echo "cs_cnt~AMBIGSUI~$cs_cnt" >> $qa_file


    #
    # AMBIG.LUI CUI,LUI count
    #
    set cl_cnt=`$PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; foreach $c (@c) { print "$_[0]|$c\n";}' $ambig_lui | wc -l`
    if ($status != 0) exit 1
    echo "cl_cnt~AMBIGLUI~$cl_cnt" >> $qa_file


else if ($target == "MRMAP") then

    #
    # Handle environment
    #
    set mrmap = $dir/MRMAP.RRF
    if (!(-e $mrmap)) then
	echo "ERROR: required file $mrmap cannot be found"
	exit 1
    endif
    set qa_file = qa_mrmap_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrmap | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique MAPSETCUI,MAPSUBSETID,MAPRANK,FROMID,TOID
    #
    set keycnt=`cut -d\| -f 1,3,4,7,15 $mrmap | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "key_cnt~~$keycnt" >> $qa_file

    #
    # SAB,FROMEXPR Tally
    #
    cut -d\| -f2,9 $mrmap | sort -u | tallyfield.pl '$1' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_fromexpr_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,TOEXPR Tally
    #
    cut -d\| -f2,17 $mrmap | sort -u | tallyfield.pl '$1' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_toexpr_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB Tally
    #
    tallyfield.pl '$2' $mrmap |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # REL Tally
    #
    tallyfield.pl '$13' $mrmap |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/rel_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # RELA Tally
    #
    tallyfield.pl '$14' $mrmap |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/rela_tally\~/' >> $qa_file
    if ($status != 0) exit 1



else if ($target == "MRCOC") then

    #
    # Handle environment
    #
    set mrcoc = $dir/MRCOC.RRF
    if (!(-e $mrcoc)) then
	echo "ERROR: required file $mrcoc cannot be found"
	exit 1
    endif
    set qa_file = qa_mrcoc_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrcoc | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique CUI1,AUI1 Count
    #
    set cui1_aui1_cnt=`cut -d\| -f 1,2 $mrcoc | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui1_aui1_cnt~~$cui1_aui1_cnt" >> $qa_file

    #
    # Unique CUI2,AUI2 Count
    #
    set cui2_aui2_cnt=`cut -d\| -f 3,4 $mrcoc | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui2_aui2_cnt~~$cui2_aui2_cnt" >> $qa_file

    #
    # Unique CUI1,AUI1,CUI2,AUI2 Count
    #
    set c1_a1_c2_a2_cnt=`awk -F\| '$2 != "" {print $1"|"$2"|"$3"|"$4}' $mrcoc | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "c1_a1_c2_a2_cnt~~$c1_a1_c2_a2_cnt" >> $qa_file

    #
    # Count by SAB,COT
    #
    tallyfield.pl '$5$6' $mrcoc |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_cot_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count by COT
    #
    tallyfield.pl '$6' $mrcoc |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/cot_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count by SAB
    #
    tallyfield.pl '$5' $mrcoc |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count Total COA
    #
    awk -F\| '$8!=""{print $8}' $mrcoc |\
    $PATH_TO_PERL -e 'while(<>) { map(((($q,$f) = split /=/) && ($coa{$q} += $f)), split/,/);} \
	map( print ("coa_tally~$_~$coa{$_}\n"), sort keys %coa);' >> $qa_file
    if ($status != 0) exit 1

else if ($target == "MRCONSO" || $target == "MetaMorphoSys" ) then

    
    #
    # Convert target into lower case
    #
    set lc_target = `echo $target | tr "[A-Z]" "[a-z]"`
    #
    # Handle environment
    #
    set mrconso=$dir/MRCONSO.RRF
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    set qa_file = qa_${lc_target}_$release

    #
    # Build intermediate files
    #
    cut -d\| -f 1,4,6 $mrconso | sort -u -T . >! MRCONSO.uis.cls.$$
    cut -d\| -f1 MRCONSO.uis.cls.$$ | sort -u -T . >! MRCONSO.uis.c.$$
    cut -d\| -f2 MRCONSO.uis.cls.$$ | sort -u -T . >! MRCONSO.uis.l.$$
    cut -d\| -f3 MRCONSO.uis.cls.$$ | sort -u -T . >! MRCONSO.uis.s.$$
    cut -d\| -f1,2 MRCONSO.uis.cls.$$ | sort -u -T . >! MRCONSO.uis.cl.$$
    cut -d\| -f1,3 MRCONSO.uis.cls.$$ | sort -u -T . >! MRCONSO.uis.cs.$$
    cut -d\| -f2,3 MRCONSO.uis.cls.$$ | sort -u -T . >! MRCONSO.uis.ls.$$

    cut -d\| -f 1,2,3,5,7 $mrconso >! MRCONSO.tmp1.$$
    cut -d\| -f 2,4,6  $mrconso >! MRCONSO.tmp2.$$

    #
    # Count all lines
    #
    set rcnt = `cat $mrconso | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique CUI count
    #
    set ccnt = `cat MRCONSO.uis.c.$$ | wc -l `
    if ($status != 0) exit 1
    echo "cui_cnt~~$ccnt" >> $qa_file

    #
    # Unique LUI Count
    #
    set lcnt = `cat MRCONSO.uis.l.$$ | wc -l`
    if ($status != 0) exit 1
    echo "lui_cnt~~$lcnt" >> $qa_file

    #
    # Unique SUI Count
    #
    set scnt = `cat MRCONSO.uis.s.$$ | wc -l`
    if ($status != 0) exit 1
    echo "sui_cnt~~$scnt" >> $qa_file

    #
    # Unique CUI, LUI, SUI Count
    #
    set cls_cnt = `cat MRCONSO.uis.cls.$$ | wc -l`
    if ($status != 0) exit 1
    echo "cls_cnt~~$cls_cnt" >> $qa_file

    #
    # Unique CUI, LUI Count
    #
    set cl_cnt = `cat MRCONSO.uis.cl.$$ | wc -l`
    if ($status != 0) exit 1
    echo "cl_cnt~~$cl_cnt" >> $qa_file

    #
    # Unique CUI, SUI Count
    #
    set cs_cnt = `cat MRCONSO.uis.cs.$$ | wc -l`
    if ($status != 0) exit 1
    echo "cs_cnt~~$cs_cnt" >> $qa_file

    #
    # Unique CUI,LUI Count
    #
    set cl_cnt=`cat MRCONSO.uis.cl.$$ | wc -l`
    if ($status != 0) exit 1
    echo "cl_cnt~~$cl_cnt" >> $qa_file

    #
    # Unique LUI, SUI Count
    #
    set ls_cnt = `cat MRCONSO.uis.ls.$$ | wc -l`
    if ($status != 0) exit 1
    echo "ls_cnt~~$ls_cnt" >> $qa_file

    #
    # Counts by LAT
    #
    tallyfield.pl '$2' MRCONSO.tmp1.$$ | \
	$PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
	sed 's/|/\//; s/^/lang_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # LAT,SAB,TTY tally
    #
    tallyfield.pl '$12$2$13' $mrconso | \
	$PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
	sed 's/^/sab_lat_tty_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    if ($target == "MetaMorphoSys" ) then
    #
    # Counts by LAT|TS|STT|ISPREF
    #
    $PATH_TO_PERL -ne 'split(/\|/);$_[3] =~ s/V.*/V/;print join("|",@_);' MRCONSO.tmp1.$$ |\
        tallyfield.pl '$2$3$4$5' |\
	$PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
	sed 's/^/lat_ts_stt_ispref_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Counts by ISPREF
    #
    tallyfield.pl '$5' MRCONSO.tmp1.$$ |\
	$PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
	sed 's/^/ispref_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count STT as VU also, to compare against gold script
    #
    #grep 'lat_ts_stt_ispref_tally' $qa_file |\
    #$PATH_TO_PERL -e 'while (<>) { split /~/; $_[1] =~ s/(.*\|.*)\|V.*$/$1\|VU/; \
	#$ct{$_[1]} += $_[2]; } \
	#foreach $key (keys %ct) { \
	#print "lat_ts_stt_ispref_tally~$key~$ct{$key}\n" }' >! tmp.$qa_file.$$
    #if ($status != 0) exit 1
    #cat tmp.$qa_file.$$ >> $qa_file
    #rm -f tmp.$qa_file.$$

    #
    # Counts by TS
    #
    tallyfield.pl '$3' $mrconso |\
	$PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
	sed 's/^/ts_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Counts by STT
    #
    $PATH_TO_PERL -ne 'split(/\|/);$_[4] =~ s/V.*/V/;print "$_[4]\n";' $mrconso | tallyfield.pl '$1' |\
	$PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
	sed 's/^/stt_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # count STT as VU also, to compare against gold script
    #
    #egrep '^stt_tally' $qa_file |\
    #$PATH_TO_PERL -e 'while (<>) { split /~/; $_[1] =~ s/^V.*$/VU/; \
	#$ct{$_[1]} += $_[2]; } \
	#foreach $key (keys %ct) { \
	 # print "stt_tally~$key~$ct{$key}\n" }' >! tmp.$qa_file.$$
    #if ($status != 0) exit 1
    #cat tmp.$qa_file.$$ >> $qa_file
    #rm -f tmp.$qa_file.$$

    #
    # Counts by TS|STT
    #
    #tallyfield.pl '$3$5' $mrconso |\
	#$PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
	#sed 's/^/ts_stt_tally\~/' >> $qa_file
    #if ($status != 0) exit 1

    #
    # count STT as VU also, to compare against gold script
    #
    #egrep '^ts_stt_tally' $qa_file |\
	#$PATH_TO_PERL -e 'while (<>) { split /~/; $_[1] =~ s/(.*)\|V.*$/$1\|VU/; \
	#$ct{$_[1]} += $_[2]; } \
	#foreach $key (keys %ct) { \
	 # print "ts_stt_tally~$key~$ct{$key}\n" }' >! tmp.$qa_file.$$
    #if ($status != 0) exit 1
    #cat tmp.$qa_file.$$ >> $qa_file
    #rm -f tmp.$qa_file.$$

    endif

    #
    # SAB, SUPPRESS Tally
    #
    tallyfield.pl '$12$17' $mrconso |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_suppress_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Restriction level Tally
    #
    tallyfield.pl '$16' $mrconso |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/srl_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB Tally
    #
    tallyfield.pl '$12' $mrconso |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # TTY Tally
    #
    tallyfield.pl '$13' $mrconso |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/tty_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB by Unique SCD tally
    #
    cut -d\| -f 12,14 $mrconso | sort -u |\
      tallyfield.pl '$1' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_scd_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB by Unique SCUI tally
    #
    cut -d\| -f 12,10 $mrconso | sort -u |\
      $PATH_TO_PERL -ne 'print unless /^\|/' | tallyfield.pl '$2' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/sab_scui_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,TTY (suppressible) tally
    #
    tallyfield.pl '$12$13$17' $mrconso |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/suppr_termgrp_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count Ambig SUIs
    #
    set ambig_suis_cnt = ` cut -d\| -f1,3 MRCONSO.uis.cls.$$ | sort -u | cut -d \| -f2 | sort | uniq -d | wc -l`
    if ($status != 0) exit 1
    echo "ambig_suis_cnt~~$ambig_suis_cnt" >> $qa_file

    #
    # Count Ambig LUIs
    #
    set ambig_luis_cnt = ` cut -d\| -f1,2 MRCONSO.uis.cls.$$ | sort -u | cut -d \| -f2 | sort | uniq -d | wc -l`
    if ($status != 0) exit 1
    echo "ambig_luis_cnt~~$ambig_luis_cnt" >> $qa_file

    #
    # Write (uppercased) ambiguous strings
    #
#   $PATH_TO_PERL -F'\|' -lane '$s = uc($F[14]);print "$F[0]|$s" if $F[1] eq "ENG"' $mrconso |\
#    sort -u -T . | cut -d\| -f2 | sort | uniq -c |\
#    $PATH_TO_PERL -ne '/(\d+) (.*)/;print "ambig_str_tally~".substr($2,0,200)."~$1\n" if $1 != 1' >> $qa_file
#    if ($status != 0) exit 1

    #
    # Find min/max length of STR field (field 15)
    #
    $PATH_TO_PERL -ne 'split /\|/; print length($_[14]),"\n";' $mrconso |\
    sort -u -T . -n -o MRCONSO.minmax.$$
    if ($status != 0) exit 1
    set min_length=`head -1 MRCONSO.minmax.$$`
    set max_length=`tail -1 MRCONSO.minmax.$$`
    echo "min_length~~$min_length" >> $qa_file
    echo "max_length~~$max_length" >> $qa_file
    rm -f MRCONSO.minmax.$$
    rm -f MRCONSO.uis.{c,l,s,cl,cs,ls,cls}{.}$$
    rm -f MRCONSO.tmp[1,2].$$

else if ($target == "MRCUI") then

    #
    # Handle environment
    #
    set mrcui = $dir/MRCUI.RRF
    set deleted_cui = $dir/CHANGE/DELETEDCUI.RRF
    set deleted_lui = $dir/CHANGE/DELETEDLUI.RRF
    set deleted_sui = $dir/CHANGE/DELETEDSUI.RRF
    set merged_cui = $dir/CHANGE/MERGEDCUI.RRF
    set merged_lui = $dir/CHANGE/MERGEDLUI.RRF

    if (!(-e $mrcui)) then
	echo "ERROR: required file $mrcui cannot be found"
	exit 1
    endif
    if (!(-e $deleted_cui)) then
	echo "ERROR: required file $deleted_cui cannot be found"
	exit 1
    endif
    if (!(-e $deleted_lui)) then
	echo "ERROR: required file $deleted_lui cannot be found"
	exit 1
    endif
    if (!(-e $deleted_sui)) then
	echo "ERROR: required file $deleted_sui cannot be found"
	exit 1
    endif
    if (!(-e $merged_cui)) then
	echo "ERROR: required file $merged_cui cannot be found"
	exit 1
    endif
    if (!(-e $merged_lui)) then
	echo "ERROR: required file $merged_lui cannot be found"
	exit 1
    endif
    set qa_file = qa_mrcui_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrcui | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique CUI1 Count
    #
    set cui1_cnt=`cut -d\| -f 1 $mrcui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui1_cnt~~$cui1_cnt" >> $qa_file

    #
    # Unique CUI2 Count
    #
    set cui2_cnt=`cut -d\| -f 6 $mrcui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui2_cnt~~$cui2_cnt" >> $qa_file

    #
    # Count by REL
    #
    tallyfield.pl '$3' $mrcui |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/rel_tally\~/' >> $qa_file
    if ($status != 0) exit 1

   #
   # ver,rel,rela Tally
   #
   tallyfield.pl '$2$3$4' $mrcui |\
   $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/ver_rel_rela_tally\~/' >> $qa_file
   if ($status != 0) exit 1

    #
    # Unique DELETED.CUI Count
    #
    set dcui_cnt=`cut -d\| -f 1 $deleted_cui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "pcui_cnt~DELETEDCUI~$dcui_cnt" >> $qa_file

    #
    # Unique DELETED.LUI Count
    #
    set dlui_cnt=`cut -d\| -f 1 $deleted_lui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "plui_cnt~DELETEDLUI~$dlui_cnt" >> $qa_file

    #
    # Unique DELETED.SUI Count
    #
    set dsui_cnt=`cut -d\| -f 1 $deleted_sui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "psui_cnt~DELETEDSUI~$dsui_cnt" >> $qa_file

    #
    # Unique MERGED.CUI1 Count
    #
    set mcui1_cnt=`cut -d\| -f 1 $merged_cui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "pcui_cnt~MERGEDCUI~$mcui1_cnt" >> $qa_file

    #
    # Unique MERGED.CUI2 Count
    #
    set mcui2_cnt=`cut -d\| -f 2 $merged_cui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_cnt~MERGEDCUI~$mcui2_cnt" >> $qa_file

    #
    # Unique MERGED.CUI1,CUI2 Count
    #
    set mcui1_cui2_cnt=`sort -u $merged_cui | wc -l`
    if ($status != 0) exit 1
    echo "pcui_cui_cnt~MERGEDCUI~$mcui1_cui2_cnt" >> $qa_file

    #
    # Unique MERGED.LUI1 Count
    #
    set mlui1_cnt=`cut -d\| -f 1 $merged_lui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "plui_cnt~MERGEDLUI~$mlui1_cnt" >> $qa_file

    #
    # Unique MERGED.LUI2 Count
    #
    set mlui2_cnt=`cut -d\| -f 2 $merged_lui | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "lui_cnt~MERGEDLUI~$mlui2_cnt" >> $qa_file

    #
    # Unique MERGED.LUI1,LUI2 Count
    #
    set mlui1_lui2_cnt=`sort -u $merged_lui | wc -l`
    if ($status != 0) exit 1
    echo "plui_lui_cnt~MERGEDLUI~$mlui1_lui2_cnt" >> $qa_file


else if ($target == "MRCXT") then

    #
    # Handle environment
    #
    set mrcxt = $dir/MRCXT.RRF
    if (!(-e $mrcxt)) then
	echo "ERROR: required file $mrcxt cannot be found"
	exit 1
    endif
    set qa_file = qa_mrcxt_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrcxt | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique CUI Count
    # Unique SUI Count
    # Unique AUI Count
    # Unique CUI,AUI Count
    # Unique CUI2 Count
    # Unique AUI2 Count
    #
    set cui_cnt=`awk -F\| '{print $1}' $mrcxt | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_cnt~~$cui_cnt" >> $qa_file

    set sui_cnt=`awk -F\| '{print $2}' $mrcxt | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "sui_cnt~~$sui_cnt" >> $qa_file

    set aui_cnt=`awk -F\| '{print $3}' $mrcxt | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "aui_cnt~~$aui_cnt" >> $qa_file

    set ca_cnt=`awk -F\| '{print $1"|"$3}' $mrcxt | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "ca_cnt~~$ca_cnt" >> $qa_file

    set cui2_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[9]\n";' $mrcxt | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui2_cnt~~$cui2_cnt" >> $qa_file

    set aui2_cnt=`awk -F\| '{print $11}' $mrcxt | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "aui2_cnt~~$aui2_cnt" >> $qa_file

    #
    # Unique Context count
    #
    set cxt_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]|$_[2]|$_[3]|$_[4]|$_[5]\n" if $_[6] eq "CCP";' $mrcxt | wc -l`
    if ($status != 0) exit 1
    echo "cxt_cnt~~$cxt_cnt" >> $qa_file

    #
    # SAB Tally
    #
    cat $mrcxt | tallyfield.pl '$4' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # CXL Tally
    #
    cat $mrcxt | tallyfield.pl '$7' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/cxl_tally\~/' >> $qa_file
    if ($status != 0) exit 1


    #
    # SAB,CXL Tally
    #
    cat $mrcxt | tallyfield.pl '$4$7' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_cxl_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,CXN Tally
    #
    fgrep '|CCP|' $mrcxt |\
    tallyfield.pl '$4$6' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_cxn_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,REL Tally
    #
    cat $mrcxt | tallyfield.pl '$4$13' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_rel_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,CXL,REL Tally
    #
    cat $mrcxt | tallyfield.pl '$4$7$13' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_cxl_rel_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # REL Tally
    #
    cat $mrcxt | tallyfield.pl '$13' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/rel_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # XC,CXL Tally
    #
    $PATH_TO_PERL -ne 'split /\|/; print "$_[6]\n" if $_[13] eq "+";' $mrcxt |\
    tallyfield.pl '$1' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/xc_cxl_tally\~/' >> $qa_file
    if ($status != 0) exit 1


else if ($target == "MRDEF") then

    #
    # Handle environment
    #
    set mrdef=$dir/MRDEF.RRF
    set qa_file = qa_mrdef_$release

    #
    # Row Count
    #
    set rcnt=(`wc -l $mrdef`)
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt[1]" >! $qa_file

    #
    # Unique CUI Count
    #
    set ccnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n";' $mrdef | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_cnt~~$ccnt" >> $qa_file

    #
    # Unique AUI Count
    #
    set acnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[1]\n";' $mrdef | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "aui_cnt~~$acnt" >> $qa_file

    #
    # SAB tally
    #
    tallyfield.pl '$5' $mrdef |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;' |\
    sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # MIN,MAX Definition
    #
    $PATH_TO_PERL -ne 'split /\|/; print length($_[5]),"\n";' $mrdef |\
       sort -u -T . -n -o MRDEF.minmax.$$
    if ($status != 0) exit 1
    set min_length=`head -1 MRDEF.minmax.$$`
    set max_length=`tail -1 MRDEF.minmax.$$`
    echo "min_length~~$min_length" >> $qa_file
    echo "max_length~~$max_length" >> $qa_file
    rm -f MRDEF.minmax.$$


else if ($target == "MRHIST") then

    #
    # Handle environment
    #
    set mrhist = $dir/MRHIST.RRF
    if (!(-e $mrhist)) then
	echo "ERROR: required file $mrhist cannot be found"
	exit 1
    endif
    set qa_file = qa_mrhist_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrhist | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique CUI Count
    #
    set cui_cnt=`awk -F\| '{print $1}' $mrhist | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_cnt~~$cui_cnt" >> $qa_file

    #
    # Unique UI Count
    #
    set ui_cnt=`awk -F\| '{print $2}' $mrhist | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "ui_cnt~~$ui_cnt" >> $qa_file

    #
    # SAB tally
    #
    tallyfield.pl '$3' $mrhist |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;' |\
    sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count by SAB,SVER
    #
    tallyfield.pl '$3$4' $mrhist |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;' |\
    sed 's/^/sab_sver_tally\~/' >> $qa_file
    if ($status != 0) exit 1


else if ($target == "MRHIER") then

    #
    # Handle environment
    #
    set mrhier = $dir/MRHIER.RRF
    if (!(-e $mrhier)) then
	echo "ERROR: required file $mrhier cannot be found"
	exit 1
    endif
    set qa_file = qa_mrhier_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrhier | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique CUI Count
    # Unique AUI Count
    # Unique CUI,AUI Count
    # Unique CUI,AUI,CXN Count
    #
    set cui_cnt=`awk -F\| '{print $1}' $mrhier | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_cnt~~$cui_cnt" >> $qa_file

    set aui_cnt=`awk -F\| '{print $2}' $mrhier | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "aui_cnt~~$aui_cnt" >> $qa_file

    set ca_cnt=`awk -F\| '{print $1"|"$2}' $mrhier | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_aui_cnt~~$ca_cnt" >> $qa_file

    set cax_cnt=`awk -F\| '{print $1"|"$2"|"$3}' $mrhier | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_aui_cxn_cnt~~$cax_cnt" >> $qa_file


    #
    # Min CXN count by CUI|AUI
    # Max CXN count by CUI|AUI
    #
    cut -d\| -f1,2 $mrhier | sort | uniq -c | sort -n | \
    $PATH_TO_PERL -pe 's/(\d+).*/$1/;' >! MRHIER.minmax.$$
    if ($status != 0) exit 1
    set min_cnt=`head -1 MRHIER.minmax.$$`
    set max_cnt=`tail -1 MRHIER.minmax.$$`
    echo "min_cxn_cnt~~$min_cnt" >> $qa_file
    echo "max_cxn_cnt~~$max_cnt" >> $qa_file
    rm -f MRHIER.minmax.$$

    #
    # SAB Tally
    #
    tallyfield.pl '$5' $mrhier |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,HCD Tally
    #
	cut -d\| -f 5,8 $mrhier | sort -u |\
      tallyfield.pl '$1' |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_hcd_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,RELA Tally
    #
	awk -F\| '$4 != "" {print $5"|"$6}' $mrhier | tallyfield.pl '$1$2'  |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_rela_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # RELA Tally
    #
    tallyfield.pl '$6' $mrhier |\
     $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/rela_tally\~/' >> $qa_file
    if ($status != 0) exit 1

else if ($target == "MRDEF") then

    #
    # Handle environment
    #
    set mrdef=$dir/MRDEF.RRF
    set qa_file = qa_mrdef_$release

    #
    # Row Count
    #
    set rcnt=(`wc -l $mrdef`)
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt[1]" >! $qa_file

    #
    # Unique CUI Count
    #
    set ccnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n";' $mrdef | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_cnt~~$ccnt" >> $qa_file

    #
    # Unique AUI Count
    #
    set acnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[1]\n";' $mrdef | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "aui_cnt~~$acnt" >> $qa_file

    #
    # SAB tally
    #
    tallyfield.pl '$5' $mrdef |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;' |\
    sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # MIN,MAX Definition
    #
    $PATH_TO_PERL -ne 'split /\|/; print length($_[5]),"\n";' $mrdef |\
       sort -u -T . -n -o MRDEF.minmax.$$
    if ($status != 0) exit 1
    set min_length=`head -1 MRDEF.minmax.$$`
    set max_length=`tail -1 MRDEF.minmax.$$`
    echo "min_length~~$min_length" >> $qa_file
    echo "max_length~~$max_length" >> $qa_file
    rm -f MRDEF.minmax.$$


else if ($target == "MRFILESCOLS") then

    #
    # Handle environment
    #
    set mrfiles = $dir/MRFILES.RRF
    set mrcols = $dir/MRCOLS.RRF
    if (!(-e $mrfiles)) then
	echo "ERROR: required file $mrfiles cannot be found"
	exit 1
    endif
    if (!(-e $mrcols)) then
	echo "ERROR: required file $mrcols cannot be found"
	exit 1
    endif
    set qa_file = qa_mrfilescols_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrfiles | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Count sum BTS column
    #
    foreach f (`cut -f1 -d\| $mrfiles`)
         set  size = `grep $f $mrfiles |cut -f6 -d\|`
         echo "file_bts_cnt~$f~$size" >> $qa_file
    end
    #awk -F\| '{ total += $6 } END {print "bts_cnt~~",total}' $mrfiles >> $qa_file
    #awk -F\| '{print "file_bts_cnt~$1~$6"}' $mrfiles >> $qa_file
    if ($status != 0) exit 1

    #
    # Row Count
    #
    set colcnt=`cat $mrcols | wc -l`
    if ($status != 0) exit 1
    echo "col_cnt~~$colcnt" >> $qa_file

    #
    # Count distinct FIL
    #
    set filecnt=`cut -d\| -f7 $mrcols | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "file_cnt~~$filecnt" >> $qa_file

    #
    # Count by COL
    #
    tallyfield.pl '$1' $mrcols |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/col_tally\~/' >> $qa_file
    if ($status != 0) exit 1


else if ($target == "MRLO") then

    #
    # Handle environment
    #
    set mrlo = $dir/MRLO.RRF
    if (!(-e $mrlo)) then
	echo "ERROR: required file $mrlo cannot be found"
	exit 1
    endif
    set qa_file = qa_mrlo_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrlo | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique CUI Count
    #
    set cui_cnt=`cut -d\| -f 1 $mrlo | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_cnt~~$cui_cnt" >> $qa_file

    #
    # Unique AUI Count
    #
    set aui_cnt=`cut -d\| -f 2 $mrlo | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "aui_cnt~~$aui_cnt" >> $qa_file

    #
    # Count by ISN
    #
    tallyfield.pl '$3' $mrlo |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/isn_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count by ISN,UN
    #
    tallyfield.pl '$3$5' $mrlo |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/isn_un_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Unique SNA Count
    #
    set sna_cnt=`cut -d\| -f 7 $mrlo | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "sna_cnt~~$sna_cnt" >> $qa_file

    #
    # Unique SOUI Count
    #
    set soui_cnt=`cut -d\| -f 8 $mrlo | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "soui_cnt~~$soui_cnt" >> $qa_file


else if ($target == "MRRANK") then

    #
    # Handle environment
    #
    set mrrank = $dir/MRRANK.RRF
    if (!(-e $mrrank)) then
	echo "ERROR: required file $mrrank cannot be found"
	exit 1
    endif
    set qa_file = qa_mrrank_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrrank | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # SAB Tally
    #
    tallyfield.pl '$2' $mrrank |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
    sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # TTY Tally
    #
    tallyfield.pl '$3' $mrrank |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
    sed 's/^/tty_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SUPR Tally
    #
    tallyfield.pl '$4' $mrrank |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
    sed 's/^/suppress_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,TTY cnt
    #
    set sab_tty_cnt=`cut -d\| -f 2,3 $mrrank | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "sab_tty_cnt~~$sab_tty_cnt" >> $qa_file


else if ($target == "MRREL") then

    #
    # Handle environment
    #
    set mrrel = $dir/MRREL.RRF
    if (!(-e $mrrel)) then
	echo "ERROR: required file $mrrel cannot be found"
	exit 1
    endif
    set qa_file = qa_mrrel_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrrel | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # CUI,AUI Counts
    #
    set cui1_cnt=`cut -d\| -f 1 $mrrel | sort -u | wc -l`
    if ($status != 0) exit 1
    set aui1_cnt=`cut -d\| -f 2 $mrrel | sort -u | wc -l`
    if ($status != 0) exit 1
    set cui2_cnt=`cut -d\| -f 5 $mrrel | sort -u | wc -l`
    if ($status != 0) exit 1
    set aui2_cnt=`cut -d\| -f 6 $mrrel | sort -u | wc -l`
    if ($status != 0) exit 1
    set cui12_cnt=`cut -d\| -f 1,5 $mrrel | sort -u | wc -l`
    if ($status != 0) exit 1
    set c_a_12_cnt=`awk -F\| '$2 != "" {print $1"|"$2"|"$5"|"$6}' $mrrel | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui1_cnt~~$cui1_cnt" >> $qa_file
    echo "aui1_cnt~~$aui1_cnt" >> $qa_file
    echo "cui2_cnt~~$cui2_cnt" >> $qa_file
    echo "aui2_cnt~~$aui2_cnt" >> $qa_file
    echo "cui1_cui2_cnt~~$cui12_cnt" >> $qa_file
    echo "c1_a1_c2_a2_cnt~~$c_a_12_cnt" >> $qa_file

    #
    # REL tally
    #
    tallyfield.pl '$4' $mrrel |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/rel_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # RELA tally
    #
    awk -F\| '($8!=""){print $8}' $mrrel |\
    tallyfield.pl '$1' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/rela_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB tally where SRUI is not null
    #
    awk -F\| '($10!=""){print $11}' $mrrel |\
    tallyfield.pl '$1' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/sab_srui_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB tally where RG is not null
    #
    awk -F\| '($13!=""){print $11}' $mrrel |\
    tallyfield.pl '$1' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/sab_rg_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,DIR tally where DIR is not null
    #
    awk -F\| '($14!=""){print $11"|"$14}' $mrrel |\
    tallyfield.pl '$1$2' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/sab_dir_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB tally
    #
    tallyfield.pl '$11' $mrrel |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB,REL,RELA,STYPE1,STYPE2 tally
    #
    tallyfield.pl '$11$4$8$3$7' $mrrel|\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/s_r_r_t1_t2_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Self-referenital REL,RELA,SAB,SL tally
    #
    awk -F\| '($1 == $5){print }' $mrrel |\
    tallyfield.pl '$4$8$11$12' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/rrss_selfref_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Self-referential count (CUI1 = CUI2)
    #
    set cui_selfref_cnt=`awk -F\| '($1 == $5){print }' $mrrel | wc -l`
    if ($status != 0) exit 1
    echo "cui_selfref_cnt~~$cui_selfref_cnt" >> $qa_file

    #
    # Self-referential count (AUI1 = AUI2)
    #
    set aui_selfref_cnt=`awk -F\| '($2 != "" && $2 == $6){print }' $mrrel | wc -l`
    if ($status != 0) exit 1
    echo "aui_selfref_cnt~~$aui_selfref_cnt" >> $qa_file

    #
    # Count by STYPE1|STYPE2|SAB (t1_t2_sab_tally)
    #
    tallyfield.pl '$3$7$11' $mrrel |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/t1_t2_sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SL: Count by SAB|REL|RELA|SUPPRESSIBLE (sab_rel_rela_supp_tally)
    #
    $PATH_TO_PERL -ne 'split /\|/; print if $_[14] ne "N";' $mrrel |\
    tallyfield.pl '$11$4$8$15' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/suppr_rel_rela_tally\~/' >> $qa_file
    if ($status != 0) exit 1

else if ($target == "MRSAB") then

    #
    # Handle environment
    #
    set mrsab = $dir/MRSAB.RRF
    if (!(-e $mrsab)) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif
    set qa_file = qa_mrsab_$release
    touch $qa_file

    #
    # Row Count
    #
    set rcnt=`cat $mrsab | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # CXTY tally
    #

    #	$PATH_TO_PERL -ne 'split /\|/; print "$_[16]\n" if $_[10] eq ""   ;' $mrsab |sort |uniq -c |sed 's/^[ \t]*//g'|sed 's/\([^ ]*\) \([^ ]*\)/\2~\1/' |\
    awk -F\| '$11==""  && $22=="Y"{print $17}' $mrsab | tallyfield.pl '$1' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/cxty_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # LAT Count
    #
    set lat_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[19]\n";' $mrsab | sort -u | wc -l`;
    if ($status != 0) exit 1
    echo "lat_cnt~~$lat_cnt" >> $qa_file

    #
    # LAT tally
    #
    awk -F\| '$11==""  && $22=="Y"{print $20}' $mrsab | tallyfield.pl '$1' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/lat_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # VSAB Count
    #
    set vsab_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]\n";' $mrsab | sort -u | wc -l`;
    if ($status != 0) exit 1
    echo "vsab_cnt~~$vsab_cnt" >> $qa_file

    #
    # RSAB Count
    #
    set rsab_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[3]\n";' $mrsab | sort -u | wc -l`;
    if ($status != 0) exit 1
    echo "rsab_cnt~~$rsab_cnt" >> $qa_file

    #
    # Source Family Count
    #
    set sf_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[5]\n";' $mrsab | sort -u | wc -l`;
    if ($status != 0) exit 1
    echo "sf_cnt~~$sf_cnt" >> $qa_file


else if ($target == "MRSAT") then

    #
    # Handle environment
    #
    set mrsat = $dir/MRSAT.RRF
    if (!(-e $mrsat)) then
	echo "ERROR: required file $mrsat cannot be found"
	exit 1
    endif
    set qa_file = qa_mrsat_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrsat | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique CUI Count
    #
    set cui_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n"' $mrsat | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cui_cnt~~$cui_cnt" >> $qa_file

    #
    # Unique SUI Count
    #
    set sui_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]\n" if $_[2]' $mrsat | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "sui_cnt~~$sui_cnt" >> $qa_file

    #
    # Unique UI Count
    #
    set ui_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[3]\n" if $_[3]' $mrsat | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "metaui_cnt~~$ui_cnt" >> $qa_file

    #
    # Unique CUI,LUI,SUI Count
    #
    set cls_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]$_[1]$_[2]\n" if $_[2]' $mrsat | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cls_cnt~~$cls_cnt" >> $qa_file

    #
    # Unique CUI,UI Count
    #
    set cu_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]$_[3]\n" if $_[3]' $mrsat | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cm_cnt~~$cu_cnt" >> $qa_file

    #
    # SAB,SATUI tally
    #
    $PATH_TO_PERL -ne 'split /\|/; print "$_[7]|$_[9]\n" if $_[7]' $mrsat | sort -u | tallyfield.pl '$2' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_satui_tally\~/' >> $qa_file
    if ($status != 0) exit 1
    
    #
    # ATN tally
    #
   # Because of the changes in atn names ( can have spaces) below is modified
     cut -f9 -d\| $mrsat | sed 's/ /\@/' |sort |uniq -c |awk '{print $2"~"$1}' |sed 's/^/atn_tally\~/' |sed 's/\@/ /' >> $qa_file
 #    tallyfield.pl '$9' $mrsat |\
 #    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
 #     sed 's/^/atn_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # SAB tally
    #
    tallyfield.pl '$10' $mrsat |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
     sed 's/^/sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count by STYPE|SAB
    #
    tallyfield.pl '$5$10' $mrsat |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/stype_sab_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # Count by SAB|ATN|STYPE
    #
    # Because of the changes in atn names ( can have spaces) below is modified

      cut -f5,9,10 -d\|  $mrsat |awk -F\| '{print $3"|"$2"|"$1}' | sed 's/ /\@/' |sort |uniq -c | awk '{print $2"~"$1}' |sed 's/\@/ /g'|sed 's/^/sab_atn_stype_tally\~/' >> $qa_file
 #    tallyfield.pl '$10$9$5' $mrsat |\
 #    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
 #    sed 's/^/sab_atn_stype_tally\~/' >> $qa_file
    if ($status != 0) exit 1
    
    
    #
    # Count by SAB|ATN|SUPPRESSIBLE
    #
    $PATH_TO_PERL -ne 'split /\|/; print if $_[11] ne "N"' $mrsat |\
    tallyfield.pl '$10$9$12' |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|total/i;' |\
    sed 's/^/suppr_atn_tally\~/' >> $qa_file
    if ($status != 0) exit 1

else if ($target == "MRSTY") then

    #
    # Handle environment
    #
    set mrsty = $dir/MRSTY.RRF
    if (!(-e $mrsty)) then
	echo "ERROR: required file $mrsty cannot be found"
	exit 1
    endif
    set qa_file = qa_mrsty_$release

    #
    # Row Count
    #
    set rcnt=(`wc -l $mrsty`)
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt[1]" >! $qa_file

    #
    # Unique STY Count
    #
    set sty_cnt=`cut -d\| -f 4 $mrsty | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "sty_cnt~~$sty_cnt" >> $qa_file

    #
    # Unique TUI Count
    #
    set tui_cnt=`cut -d\| -f 2 $mrsty | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "tui_cnt~~$tui_cnt" >> $qa_file

    #
    # Unique STN Count
    #
    set stn_cnt=`cut -d\| -f 3 $mrsty | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "stn_cnt~~$stn_cnt" >> $qa_file

    #
    # STY Tally
    #
    tallyfield.pl '$4' $mrsty |\
    $PATH_TO_PERL -ne 's/(.*)[\t ]+/$1\~/; print unless /=======|TOTAL/i;'|\
    sed 's/^/sty_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # TUI Tally
    #
    tallyfield.pl '$2' $mrsty |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
    sed 's/^/tui_tally\~/' >> $qa_file
    if ($status != 0) exit 1

    #
    # STN Tally
    #
    tallyfield.pl '$3' $mrsty |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
    sed 's/^/stn_tally\~/' >> $qa_file
    if ($status != 0) exit 1


else if ($target == "MRDOC") then

    #
    # Handle environment
    #
    set mrdoc = $dir/MRDOC.RRF
    if (!(-e $mrdoc)) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    set qa_file = qa_mrdoc_$release

    #
    # Row Count
    #
    set rcnt=`cat $mrdoc | wc -l`
    if ($status != 0) exit 1
    echo "row_cnt~~$rcnt" >! $qa_file

    #
    # Unique Type Count
    #
    set type_cnt=`cut -d\| -f3 $mrdoc | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "type_cnt~~$type_cnt" >> $qa_file

    #
    # Unique Key Count
    #
    set key_cnt=`cut -d\| -f1 $mrdoc | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "dockey_cnt~~$key_cnt" >> $qa_file

    #
    # Unique Type,Key Count
    #
    set type_dockey_cnt=`cut -d\| -f3,1 $mrdoc | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "type_dockey_cnt~~$type_dockey_cnt" >> $qa_file

    #
    # Type,Key Tally
    #
    tallyfield.pl '$3$1' $mrdoc |\
    $PATH_TO_PERL -ne 's/[\t ]+/\~/g; print unless /=======|TOTAL/i;'|\
    sed 's/^/type_dockey_tally\~/' >> $qa_file
    if ($status != 0) exit 1


else if ($target == "MRX") then

    #
    # Handle environment
    #
    set mrxw=$dir/MRXW_
    set mrxns=$dir/MRXNS_ENG.RRF
    set mrxnw=$dir/MRXNW_ENG.RRF
    if (!(-e $mrxns)) then
	echo "ERROR: required file $mrxns cannot be found"
	exit 1
    endif
    if (!(-e $mrxnw)) then
	echo "Error: required file $mrxnw cannot be found"
	exit 1
    endif
    set qa_file = qa_mrx_$release
    rm -f $qa_file
    touch $qa_file

    #
    # Get language
    #
    ls $mrxw* | sed 's/MRXW_//' | sed 's/\.RRF//' | sed 's/.*\///' | sort -u -o mrx.lats.$$

    #
    # Unique CUI,LUI,SUI Count for MRXNS_ENG.RRF, MRXNW_ENG.RRF, MRXW.*.RRF
    #
    set ns_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxns | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cls_file_cnt~MRXNS_ENG~$ns_cnt" >> $qa_file

    set nw_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxnw | sort -u | wc -l`
    if ($status != 0) exit 1
    echo "cls_file_cnt~MRXNW_ENG~$nw_cnt" >> $qa_file

    foreach f (`cat mrx.lats.$$`)
	if (!(-e $mrxw$f.RRF)) then
	    echo "ERROR: required file $mrxw$f.RRF cannot be found"
	    exit 1
	endif
	set cls_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxw$f.RRF | sort -u | wc -l`
	if ($status != 0) exit 1
	echo "cls_file_cnt~MRXW_$f~$cls_cnt" >> $qa_file
    end
    rm -f mrx.lats.$$

else if ($target == "ORF") then

    #
    # Handle Environment
    #
    set qa_file = qa_orf_$release
    rm -f $qa_file
    touch $qa_file

else if ($target == "DOC") then

    #
    # Handle Environment
    #
    set qa_file = qa_doc_$release
    rm -f $qa_file
    touch $qa_file

    #
    # file_cnt
    #
    set file_cnt=`ls $dir/../HTML/*HTML | wc`
    if ($status != 0) exit 11
    echo "file_cnt~~$file_cnt" >> $qa_file

else

    echo "ERROR: Invalid target $target"
    exit 1

endif

#
# Load counts into database
#
$bin/load_qa_file.csh $db $qa_file $qa_file

# let it be OK that this fails.
#if ($status != 0) exit 1

exit 0

