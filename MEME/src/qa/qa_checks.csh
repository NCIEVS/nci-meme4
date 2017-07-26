#!/bin/csh -f
#
# Script:    qa_checks.csh
# Author:    BK, BAC, TK
#
# Validates file semantics.  Any kind of error
# should be reported in a line with the word ERROR at
# the beginning of the line.
#
# CHANGES
#  03/09/2006 TTN (1-AM5U9): add ts,stt,ispref checks in MetaMorphoSys validation
#  01/24/2006 BAC (1-754WO): when validating MetaMorphoSys, ignore release_info
#    differences in MRDOC comparison
#  01/24/2006 BAC (1-754X9): AUI patterns should support 7 or 8 digits
#  01/12/2006 BAC (1-73QW8): Better support for Linux.
#    minor change to CUI format for CL* style CUIs.
#
# Version Information
# 1.0.5 09/17/2002: Re-organized to work with MRD App server
# 1.0.6 04/29/2004: Add subset mode
# 1.0.7 03/07/2005: Add MRAUI check

set release=1
set version=0.7
set authority=BAC
set version_date="03/07/2005"

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
set usage="Usage: $0 <dir> <db> <target> <mode> <previous_released_dir>"
set bin=$MRD_HOME/bin
setenv LC_COLLATE C

setenv PATH "/bin:/usr/bin:/usr/local/bin"
alias sort sort -T .

#
# Parse arguments
#
if ($#argv != 5) then
	echo "ERROR: Wrong number of arguments"
	echo "$usage"
	exit 1
endif

set dir=$1
set db=$2
set target=$3
set mode=$4
set prev_released_dir=$5

set ambig_sui=$dir/AMBIGSUI.RRF
set ambig_lui=$dir/AMBIGLUI.RRF
set mrmap=$dir/MRMAP.RRF
set mrsmap=$dir/MRSMAP.RRF
set mrhist=$dir/MRHIST.RRF
set mrcoc=$dir/MRCOC.RRF
set mrconso=$dir/MRCONSO.RRF
set mrcui=$dir/MRCUI.RRF
set old_mrcui=$prev_released_dir/MRCUI.RRF
set old_mrconso=$prev_released_dir/MRCONSO.RRF
set deleted_cui=$dir/CHANGE/DELETEDCUI.RRF
set deleted_lui=$dir/CHANGE/DELETEDLUI.RRF
set deleted_sui=$dir/CHANGE/DELETEDSUI.RRF
set merged_cui=$dir/CHANGE/MERGEDCUI.RRF
set merged_lui=$dir/CHANGE/MERGEDLUI.RRF
set mrcxt=$dir/MRCXT.RRF
set mrhier=$dir/MRHIER.RRF
set mrdef=$dir/MRDEF.RRF
set mrdoc=$dir/MRDOC.RRF
set mrfiles=$dir/MRFILES.RRF
set mrcols=$dir/MRCOLS.RRF
#set mrlo=$dir/MRLO.RRF
set mrrank=$dir/MRRANK.RRF
set mrrel=$dir/MRREL.RRF
set mrsab=$dir/MRSAB.RRF
set mrsat=$dir/MRSAT.RRF
set mrsty=$dir/MRSTY.RRF
set mrxw=$dir/MRXW
set mrxnw=$dir/MRXNW_ENG.RRF
set mrxns=$dir/MRXNS_ENG.RRF
set mraui=$dir/MRAUI.RRF

if ($target == "DOC") then
    echo "    Verify nothing for DOC"
else if ($target == "MRAUI") then
    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mraui) then
		echo "ERROR: required file $mraui cannot be found"
		exit 1
	    endif
    if (! -e $mrdoc) then
        echo "ERROR: required file $mrdoc cannot be found"
		exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $mraui"
    $PATH_TO_PERL -ne 'print unless /^A\d{7,8}\|C.\d{6}\|\d\d\d\d..\|[^\|]*\|[^\|]*\|[^\|]+\|A\d{7,8}\|C.\d{6}\|[YN]\|/;' $mraui >! MRAUI.badfields.$$
    set cnt = `cat MRAUI.badfields.$$ | wc -l`
    if ($cnt != 0) then
        echo "ERROR: The following rows have bad field formats"
        cat MRAUI.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRAUI.badfields.$$

    #
    #   Verify REL in MRDOC.SUBKEY where MRDOC.DOCKEY=REL
    #
    echo "    Verify REL in MRDOC.SUBKEY where MRDOC.DOCKEY=REL"
    cut -d\| -f4 $mraui | sort -u >! MRAUI.REL.$$
    set empty = `$PATH_TO_PERL -ne 'print /.+/;' MRAUI.REL.$$ | wc -l`
    if ($empty != 0) then
        set cnt = `awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRAUI.REL.$$ | wc -l`
        if ($cnt != 0) then
            echo "ERROR:   REL not in MRDOC.SUBKEY where MRDOC.DOCKEY=REL"
            awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRAUI.REL.$$ | sed 's/^/  /'
        endif
    endif
    rm -f MRAUI.REL.$$

    #
    #   Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA
    #
    echo "    Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    cut -d\| -f5 $mraui | sort -u >! MRAUI.RELA.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRAUI.RELA.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  RELA not in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
	awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRAUI.RELA.$$ | sed 's/^/  /'
    endif
    rm -f MRAUI.RELA.$$

    #
    #   Verify AUI1, VER unique
    #
    echo "    Verify AUI1, VER unique"
    set ct=`cut -d\| -f1,3 $mraui | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: AUI1, VER is not unique"
        cut -d\| -f1,3 $mraui |\
           sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify CUI2|AUI2 in MRCONSO for mapreason = move
    #
    echo "    Verify CUI2|AUI2 in MRCONSO for mapreason = move"
    cut -d\| -f1,8 $mrconso | sort -u >! mrconso.cuiaui.$$
    awk -F\| '$6=="move" {print $8"|"$7}' $mraui | sort -u >! mraui.cuiaui.$$
    set ct=`comm -13 mrconso.cuiaui.$$ mraui.cuiaui.$$ | wc -l`
    if ($ct != 0) then
	echo "ERROR: AUI2, CUI2 does not exist in MRCONSO"
	comm -13 mrconso.cuiaui.$$ mraui.cuiaui.$$
    endif
    rm -f mrconso.cuiaui.$$ mraui.cuiaui.$$


    #
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mraui >> /dev/null
    if ($status != 0) then
	echo "ERROR: MRAUI has incorrect sort order"
    endif



else if ($target == "AMBIG") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $ambig_sui) then
	echo "ERROR: required file $ambig_sui cannot be found"
	exit 1
    endif
    if (! -e $ambig_lui) then
	echo "ERROR: required file $ambig_lui cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $ambig_sui"
    $PATH_TO_PERL -ne 'print unless /^S\d{7}\|(,{0,1}C.\d{6})+\|/;' $ambig_sui >! AMBIG.badfields.$$
    #$PATH_TO_PERL -ne 'print unless /^S\d{7}\|C.\d{6}\|/;' $ambig_sui >! AMBIG.badfields.$$
    set cnt = `cat AMBIG.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat AMBIG.badfields.$$ | sed 's/^/  /'
    endif
    rm -f AMBIG.badfields.$$

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $ambig_lui"
    $PATH_TO_PERL -ne 'print unless /^L\d{7}\|(,{0,1}C.\d{6})+\|/;' $ambig_lui >! AMBIG.badfields.$$
    #$PATH_TO_PERL -ne 'print unless /^L\d{7}\|C.\d{6}\|/;' $ambig_lui >! AMBIG.badfields.$$
    set cnt = `cat AMBIG.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat AMBIG.badfields.$$ | sed 's/^/  /'
    endif
    rm -f AMBIG.badfields.$$

    #
    #   Verify cs_cnt equals the ambiguous SUI count from MRCONSO
    #
    echo "    Verify cs_cnt equals the ambiguous SUI count from MRCONSO"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[5]\n";' $mrconso |\
	sort -t\| -k 2,2 -o MRCONSO.uis.cs.tmp.$$
    join -t\| -j1 2 -j2 2 -o 1.1 2.1 1.2 \
	MRCONSO.uis.cs.tmp.$$ MRCONSO.uis.cs.tmp.$$ |\
 	awk -F\| '$1!=$2 {print $1"|"$3}' |\
	sort -u >! MRCONSO.ambig.sui.$$
    set ct=`wc -l MRCONSO.ambig.sui.$$`
    set cs_cnt=`$PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; foreach $c (@c) { print "$_[0]|$c\n";}' $ambig_sui | wc -l`
    if ($ct[1] != $cs_cnt) then
	echo "ERROR: Ambiguous SUI count from MRCONSO does not match AMBIG.SUI"
	echo "ERROR:  MRCONSO($ct), AMBIG.SUI($cs_cnt)"
    endif

    #
    #   Verify CUI|SUI in MRCONSO.CUI|SUI
    #
    echo "    Verify CUI|SUI in MRCONSO.CUI|SUI "
    cut -d\| -f1,6 $mrconso | sort -u >! MRCONSO.uis.cs.$$
    $PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; \
	  foreach $c (@c) { print "$c|$_[0]\n";}' $ambig_sui |\
	sort -u >! AMBIG.cuisui.$$
    set ct=(`comm -23 AMBIG.cuisui.$$ MRCONSO.uis.cs.$$ | wc -l`)
    if ($ct[1] != 0) then
	echo "ERROR: CUI,SUI combinations in AMBIG.SUI not in MRCONSO"
	comm -23 AMBIG.cuisui.$$ MRCONSO.uis.cs.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.uis.cs.tmp.$$ MRCONSO.ambig.sui.$$ AMBIG.cuisui.$$ MRCONSO.uis.cs.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $ambig_sui >> /dev/null
    if ($status != 0) then
	echo "ERROR: $ambig_sui has incorrect sort order"
    endif

    #
    #   Verify cl_cnt equals the ambiguous LUI count from MRCONSO
    #
    echo "    Verify cl_cnt equals the ambiguous LUI count from MRCONSO"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]\n";' $mrconso |\
	sort -t\| -k 2,2 -o MRCONSO.uis.cl.tmp.$$
    join -t\| -j1 2 -j2 2 -o 1.1 2.1 1.2 \
	MRCONSO.uis.cl.tmp.$$ MRCONSO.uis.cl.tmp.$$ |\
	awk -F\| '$1!=$2 {print $1"|"$3}' |\
	sort -u >! MRCONSO.ambig.lui.$$
    set ct=(`wc -l MRCONSO.ambig.lui.$$`)
    set cl_cnt=`$PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; foreach $c (@c) { print "$_[0]|$c\n";}' $ambig_lui | wc -l`
    if ($ct[1] != $cl_cnt) then
	echo "ERROR: Ambiguous LUI count from MRCONSO does not match AMBIG.LUI"
	echo "ERROR:   MRCONSO($ct), AMBIG.LUI($cl_cnt)"
    endif

    #
    #   Verify CUI|LUI in MRCONSO.CUI|LUI
    #
    echo "    Verify CUI|LUI in MRCONSO.CUI|LUI"
    cut -d\| -f 1,4 $mrconso | sort -u -T . >! MRCONSO.uis.cl.$$
    $PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; \
	    foreach $c (@c) { print "$c|$_[0]\n";}' $ambig_lui |\
	sort -u >! AMBIG.cuilui.$$
    set ct=`comm -23 AMBIG.cuilui.$$ MRCONSO.uis.cl.$$ | wc -l`
    if ($ct != 0) then
	echo "ERROR: CUI,SUI combinations in AMBIG.SUI not in MRCONSO"
	comm -23 AMBIG.cuilui.$$ MRCONSO.uis.cl.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.uis.cl.tmp.$$ MRCONSO.ambig.lui.$$ AMBIG.cuilui.$$ MRCONSO.uis.cl.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $ambig_lui >> /dev/null
    if ($status != 0) then
	echo "ERROR: $ambig_lui has incorrect sort order"
    endif

else if ($target == "MRHIST") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrhist) then
	echo "ERROR: required file $mrhist cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
if ($mode != "submission") then
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|[^\|]+\|[^\|]*\|[^\|]+\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|\d*\|/;' $mrhist >! MRHIST.badfields.$$
    set cnt = `cat MRHIST.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRHIST.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRHIST.badfields.$$
else
   $PATH_TO_PERL -ne 'print unless /^*\|[^\|]+\|[^\|]*\|[^\|]+\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|*\|/;' $mrhist >! MRHIST.badfields.$$
    set cnt = `cat MRHIST.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRHIST.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRHIST.badfields.$$
endif


    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f3 $mrhist | sort -u >! MRHIST.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRHIST.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRHIST.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRHIST.SAB.$$

    #
    #   Verify CUI,SAB in MRCONSO.CUI,SAB
    #
    if ($mode != "subset") then
	echo "    Verify CUI,SAB in in MRCONSO.CUI,SAB"
	cut -d\| -f1,12 $mrconso | sort -u -o MRCONSO.sabs.$$
	$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[2]\n";' $mrhist | sort -u -o MRHIST.sabs.$$
	set ct=(`comm -23 MRHIST.sabs.$$ MRCONSO.sabs.$$ | wc -l`)
	if ($ct[1] != 0) then
	    echo "ERROR: CUI,SAB in MRHIST not in MRCONSO"
	    comm -23 MRHIST.sabs.$$ MRCONSO.sabs.$$ | sed 's/^/  /'
	endif
	rm -f MRHIST.sabs.$$ MRCONSO.sabs.$$
    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrhist >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRHIST has incorrect sort order"
    endif

else if ($target == "MRMAP") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrmap) then
	echo "ERROR: required file $mrmap cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $mrsty) then
	echo "ERROR: required file $mrsty cannot be found"
	exit 1
    endif
    if (! -e $mrsat) then
	echo "ERROR: required file $mrsat cannot be found"
	exit 1
    endif
    if (! -e $mrrel) then
	echo "ERROR: required file $mrrel cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #  MAPSETCUI,MAPSETSAB,MAPSUBSETID,MAPRANK,MAPID,MAPSID,FROMID,FROMSID,FROMEXPR,FROMTYPE,
    #  FROMRULE,FROMRES,REL,RELA,TOID,TOSID,TOEXPR,TOTYPE,TORULE,TORES,
    #  MAPRULE,MAPRES,MAPTYPE,MAPATN,MAPATV,CVF
    #
    echo "    Verify field formats"
	$PATH_TO_PERL -ne 'split /\|/; print unless /^C.\d{6}\|[^\|]+\|[^\|]*\|[^\|]*\|AT\d*\|[^\|]*\|[^\|]+\|[^\|]*\|[^\|]+\|[^\|]+\|.*\|\d*\|/; if ($_[12] ne "XR") { print unless ($_[14] =~ /.+/ && $_[16] =~ /.+/ && $_[17] =~ /.+/); } ' $mrmap >! MRMAP.badfields.$$
    #note: this is added to address the SNOMEDCT mapping to empty code, may be removed later: begin
    $PATH_TO_PERL -ne 'split /\|/; print unless /^C.\d{6}\|[^\|]+\|[^\|]*\|[^\|]*\|AT\d*\|[^\|]*\|[^\|]+\|[^\|]*\|[^\|]+\|[^\|]+\|.*\|\d*\|/; ' MRMAP.badfields.$$ >! tmp.MRMAP.badfields.$$
    awk -F\| '$2!="SNOMEDCT"&&$15=="100051"{print}' MRMAP.badfields.$$ >> tmp.MRMAP.badfields.$$
    sort -u tmp.MRMAP.badfields.$$ >! MRMAP.badfields.$$
    rm -f tmp.MRMAP.badfields.$$
    #end
    set cnt = `cat MRMAP.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRMAP.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRMAP.badfields.$$

    #
    #   Verify MAPSETSAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify MAPSETSAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f2 $mrmap | sort -u >! MRMAP.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRMAP.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  MAPSETSAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRMAP.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRMAP.SAB.$$

    #
    #   Verify SNOMEDCT TOSID not null
    #
    echo "    Verify SNOMEDCT TOSID not null"
    set ct=`cut -d\| -f16 $mrmap | sort -u | wc -l`
    if ($cnt == 1) then
	echo "ERROR:  SNOMEDCT TOSID should not be null"
	awk -F\| '$2 == "SNOMEDCT" && $16=="" { print $0 };' $mrmap | head | sed 's/^/  /'
    endif

    #
    #   Verify FROMTYPE in MRDOC.VALUE where MRDOC.DOCKEY=FROMTYPE
    #
    echo "    Verify FROMTYPE in MRDOC.VALUE where MRDOC.DOCKEY=FROMTYPE"
    cut -d\| -f10 $mrmap | sort -u >! MRMAP.TYPE.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="FROMTYPE"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.TYPE.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  FROMTYPE not in MRDOC.VALUE where MRDOC.DOCKEY=FROMTYPE"
	awk -F\| '$3=="expanded_form"&&$1=="FROMTYPE"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.TYPE.$$ | sed 's/^/  /'
    endif
    rm -f MRMAP.TYPE.$$

    #
    #   Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL
    #
    echo "    Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    cut -d\| -f13 $mrmap | sort -u >! MRMAP.REL.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.REL.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  REL not in MRDOC.VALUE where MRDOC.DOCKEY=REL"
	awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.REL.$$ | sed 's/^/  /'
    endif
    rm -f MRMAP.REL.$$

    #
    #   Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA
    #
    echo "    Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    cut -d\| -f14 $mrmap | sort -u >! MRMAP.RELA.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.RELA.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  RELA not in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
	awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.RELA.$$ | sed 's/^/  /'
    endif
    rm -f MRMAP.RELA.$$

    #
    #   Verify TOTYPE in MRDOC.VALUE where MRDOC.DOCKEY=TOTYPE"
    #
    echo "    Verify TOTYPE in MRDOC.VALUE where MRDOC.DOCKEY=TOTYPE"
    cut -d\| -f18 $mrmap | sort -u >! MRMAP.TYPE.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="TOTYPE"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.TYPE.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  TOTYPE not in MRDOC.VALUE where MRDOC.DOCKEY=TOTYPE"
	awk -F\| '$3=="expanded_form"&&$1=="TOTYPE"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.TYPE.$$ | sed 's/^/  /'
    endif
    rm -f MRMAP.TYPE.$$

    #
    #   Verify MAPATN in MRDOC.VALUE where MRDOC.DOCKEY=ATN"
    #
    echo "    Verify MAPATN in MRDOC.VALUE where MRDOC.DOCKEY=MAPATN"
    cut -d\| -f24 $mrmap | sort -u >! MRMAP.ATN.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="MAPATN"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.ATN.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  MAPATN not in MRDOC.VALUE where MRDOC.DOCKEY=ATN"
	awk -F\| '$3=="expanded_form"&&$1=="MAPATN"{print $2}' $mrdoc | sort -u | comm -13 - MRMAP.ATN.$$ | sed 's/^/  /'
    endif
    rm -f MRMAP.ATN.$$

    #
    #  Verify MAPSETCUI in MRCONSO.CUI
    #
    echo "    Verify MAPSETCUI in MRCONSO.CUI"
    cut -d\| -f1 $mrconso | sort -u >! MRCONSO.uis.c.$$
    set ct=`cut -d\| -f1 $mrmap | sort -u | join -v 1 -t\| -j 1 - MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: MAPSETCUI in MRMAP not in MRCONSO"
	cut -d\| -f 1 $mrmap | sort -u |\
           join -v 1 -t\| -j 1 - MRCONSO.uis.c.$$  >> /dev/null
    endif

    #
    #  Verify FROMEXPR in MRCONSO.CUI, WHERE MAPTYPE="ATX"
    #
    echo "    Verify FROMEXPR in MRCONSO.CUI for MAPTYPE=ATX"
    set ct=`awk -F\| '$23=="ATX" { print $9 }' $mrmap | sort -u | join -v 1 -t\| -j 1 - MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: FROMEXPR in MRMAP not in MRCONSO.CUI"
	awk -F\| '$23=="ATX" { print $9 }' $mrmap | sort -u | join -v 1 -t\| -j 1 - MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.uis.c.$$

    #
    #  Validate TOPEXPR syntax (parse it) if MAPTYPE = ATX
    #
    echo "    Validate TOPEXPR syntax (parse it) if MAPTYPE = ATX"
    $PATH_TO_PERL -e 'while (<>) { \
	split /\|/; \
	  if($_[22] eq "ATX") { \
	  print "ERROR parsing: $_" unless (&parse($_[16])); \
	  } \
	} \
	exit 0;  \
	sub parse {  \
	  my($expr) = @_;  \
	  $mode = 1; # looking for term/parens \
	  while (1) { \
	    # empty expression is fine \
	    return 1 unless  $expr; \
	    # find innermost parenthetical expr \
	    if ($mode == 1 && $expr =~ /^(.*)\(([^\(\)]*)\)(.*)$/) { \
	      # if the paren expr is part of a term e.g. "Inversion (Genetics)" \
	      ($pre,$post) = ($1,$3); \
	      return &parse("${pre}pw$post") if ($expr =~ /.*<[^>]*\($2\)[^>]*>.*/);  \
	      return &parse("$2") && &parse("$1<pe>$3"); \
	    } \
	    # term \
	    elsif ($mode == 1 && $expr =~ s/^<(.*?)>//) { \
	      while ($expr =~ s/^\/<(.*?)>//) { } \
	      $mode = 2; next; \
	    } \
    	    # AND,OR, NOT \
	    elsif ($mode == 2 &&  $expr =~ s/^ (OR|AND NOT|AND|NOT) //) { \
	      return 0 unless $expr; \
	      $mode = 1; next; \
	    }  \
	  # did not match, error  \
	  return 0; } }' $mrmap >! MRMAP.parse.errors.$$
    set ct=`cat MRMAP.parse.errors.$$ | wc -l`
    if ($ct != 0) then
        cat MRMAP.parse.errors.$$
    endif
    rm -f MRMAP.parse.errors.$$

    #
    #  Verify MRSMAP matches fields from MRMAP
    #
    echo "    Verify MRSMAP matches fields from MRMAP"
    set cnt = `awk -F\| '(($3=="" && $4=="") || ($3=="0" && $4=="0")) { print $1"|"$2"|"$5"|"$6"|"$9"|"$10"|"$13"|"$14"|"$17"|"$18"|"$26"|" }' $mrmap | sort -u | comm -3 - $mrsmap | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  MRSMAP does not match fields from MRMAP"
	awk -F\| '(($3=="" && $4=="") || ($3=="0" && $4=="0")) {  print $1"|"$2"|"$5"|"$6"|"$9"|"$10"|"$13"|"$14"|"$17"|"$18"|"$26"|"}' $mrmap | sort -u | comm -3 - $mrsmap | sed 's/^/  /'
    endif

    #
    #  Verify MAPSETCUI in MRSAT.CUI
    #
    echo "    Verify MAPSETCUI in MRSAT.CUI"
    cut -d\| -f1 $mrsat | sort -u >! MRSAT.uis.c.$$
    set ct=`cut -d\| -f1 $mrmap | sort -u | join -v 1 -t\| -j 1 - MRSAT.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: MAPSETCUI in MRMAP not in MRSAT"
	cut -d\| -f1 $mrmap | sort -u |\
           join -v 1 -t\| -j 1 - MRSAT.uis.c.$$  >> /dev/null
    endif
    rm -f MRSAT.uis.c.$$

    #
    #  Verify MAPSETCUI in MRSTY.CUI
    #
    echo "    Verify MAPSETCUI in MRSTY.CUI"
    cut -d\| -f1 $mrsty | sort -u >! MRSTY.uis.c.$$
    set ct=`cut -d\| -f1 $mrmap | sort -u | join -v 1 -t\| -j 1 - MRSTY.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: MAPSETCUI in MRMAP not in MRSTY"
	cut -d\| -f1 $mrmap | sort -u |\
           join -v 1 -t\| -j 1 - MRSTY.uis.c.$$  >> /dev/null
    endif
    rm -f MRSTY.uis.c.$$

    #
    #  Verify MAPSETSAB count is the same as SAB count in MRREL.SAB
    #
    if ($mode != "subset") then
    echo "    Verify MAPSETSAB in MRREL.SAB"
    awk -F\| '$18=="CODE"&&$10=="SCUI"&&$17!~/,/&&$13!="XR" {print}' $mrmap | tallyfield.pl '$11,$2' >! MRMAP.mapsetsab.$$
    awk -F\| '$8=="mapped_to" {print}' $mrrel | tallyfield.pl '$4,$11' >! MRREL.mapsetsab.$$
    set ct=`join MRMAP.mapsetsab.$$ MRREL.mapsetsab.$$ | grep -v TOTAL | awk '$2!=$3 {print}' | wc -l`
    if ($ct != 0) then
	echo "ERROR: REL,MAPSETSAB count differs than REL,SAB count in MRREL"
        join -v 1 -j 1 MRMAP.mapsetsab.$$ MRREL.mapsetsab.$$ | awk '$2!=$3 {print $1" MRMAP count:"$2" MRREL count:"$3}'
    endif
    rm -f MRMAP.mapsetsab.$$ MRREL.mapsetsab.$$
    endif

    #
    #   Verify MAPID unique
    #
    echo "    Verify MAPID unique"
    set ct=`cut -d\| -f5 $mrmap | sort | uniq -d | wc -l`
    if ($ct != 0) then
       echo "ERROR: MAPID is not unique"
       cut -d\| -f5 $mrmap |\
	  sort | uniq -d | sed 's/^/  /'
    endif

    #
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrmap >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRMAP has incorrect sort order"
    endif

else if ($target == "MRCOC") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrcoc) then
	echo "ERROR: required file $mrcoc cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $mrsat) then
	echo "ERROR: required file $mrsat cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|A\d{7,8}\|(C.\d{6})?\|(A\d{7,8})?\|[^\|]*\|[^\|]*\|\d*\|(,{0,1}[A-Z<][A-Z>]=\d+)*\|\d*\|/;' $mrcoc >! MRCOC.badfields.$$
    set cnt = `cat MRCOC.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRCOC.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRCOC.badfields.$$

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f5 $mrcoc | sort -u >! MRCOC.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCOC.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCOC.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRCOC.SAB.$$

    #
    #   Verify COT in MRDOC.VALUE where MRDOC.DOCKEY=COT"
    #
    echo "    Verify COT in MRDOC.VALUE where MRDOC.DOCKEY=COT"
    cut -d\| -f6 $mrcoc | sort -u >! MRCOC.COT.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="COT"{print $2}' $mrdoc | sort -u | comm -13 - MRCOC.COT.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  COT not in MRDOC.VALUE where MRDOC.DOCKEY=COT"
	awk -F\| '$3=="expanded_form"&&$1=="COT"{print $2}' $mrdoc | sort -u | comm -13 - MRCOC.COT.$$ | sed 's/^/  /'
    endif
    rm -f MRCOC.COT.$$

    #
    #  Verify CUI1!=CUI2 (for MED,MBD)
    #
    echo "    Verify CUI1!=CUI2"
    set ct=`awk -F\| '$1==$3 {print $0}' $mrcoc | egrep 'MED|MBD' | wc -l`
    if ($ct != 0) then
        echo "WARNING: Self-referential COC Rows"
	awk -F\| '$1==$3 {print $0}' $mrcoc | egrep 'MED|MBD|' | sed 's/^/  /'
    endif

    #
    #  Verify AUI1!=AUI2
    #
    echo "    Verify AUI1!=AUI2"
    set ct=`awk -F\| '$2==$4 {print $0}' $mrcoc | wc -l`
    if ($ct != 0) then
        echo "WARNING: Self-referential AUI COC Rows"
	awk -F\| '$2==$4 {print $0}' $mrcoc | sed 's/^/  /'
    endif

    #
    #  Verify CUI1,AUI1 in MRCONSO.CUI,AUI
    #
    echo "    Verify CUI1,AUI1 in MRCONSO.CUI,AUI"
    cut -d\| -f1,8 $mrconso | sort -u -T . >! MRCONSO.uis.ca.$$
    cut -d\| -f 1,2 $mrcoc | sort -u >! mrcoc.tmp1.$$
    set ct=`comm -23 mrcoc.tmp1.$$ MRCONSO.uis.ca.$$ | wc -l`
     if ($ct != 0) then
        echo "ERROR: CUI1,AUI1 not in MRCONSO.CUI,AUI"
	comm -23 mrcoc.tmp1.$$ MRCONSO.uis.ca.$$  | sed 's/^/  /'
    endif

    #
    #  Verify CUI2,AUI2 in MRCONSO.CUI,AUI
    #
    echo "    Verify CUI2,AUI2 in MRCONSO.CUI,AUI"
    awk -F\| '$3!="" {print $3"|"$4}' $mrcoc | sort -u >! mrcoc.tmp1.$$
    set ct=`comm -23 mrcoc.tmp1.$$ MRCONSO.uis.ca.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI2,AUI2 not in MRCONSO.CUI,AUI"
	comm -23 mrcoc.tmp1.$$ MRCONSO.uis.ca.$$  | sed 's/^/  /'
    endif
    rm -f mrcoc.tmp1.$$ MRCONSO.uis.ca.$$

    #
    #  Verify COA sort order (sorted by frequency, then alphabetic)
    #
    echo "    Verify COA sort order"
    $PATH_TO_PERL -ne 'split /\|/; \
        $prev_qa = ""; $prev_f = ""; \
	foreach $p (split /,/,$_[7]) { \
	   ($qa,$f) = split /=/,$p; \
	   print "ERROR: Incorrect COA sort order: $_" \
   	     if ($f > $prev_f && $prev_f); \
	   print "ERROR: Incorrect COA sort order: $_" \
	     if ($f eq $prev_f && $qa le $prev_qa);  \
	   $prev_f=$f; $prev_qa=$qa; } ' $mrcoc >! MRCOC.COAsort.errors.$$

    set ct=`cat MRCOC.COAsort.errors.$$ | wc -l`
    if ($ct != 0) then
        cat MRCOC.COAsort.errors.$$
    endif
    rm -f MRCOC.COAsort.errors.$$

    #
    #  Verify COA are valid in MRSAT.ATV (where ATN=QA) (except for '<>')
    #

    echo "    Verify COA are valid in MRSAT.ATV (where ATN=QA) (except for '<>')"
    (cat $mrcoc; echo "END") |\
    $PATH_TO_PERL -ne 'split /\|/; \
	if ($_[0] eq "END\n") { print join("\n",keys %qa),"\n"; } \
	foreach $p (split /,/,$_[7]) { \
	($q,$f) = split /=/, $p; $qa{$q} = 1 if $q ne "<>"; } ' | sort -u >! mrcoc.qa.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[10]\n" if $_[8] eq "QA" && $_[9] =~ /MSH/;' $mrsat | \
	sort -u >! mrsat.qa.$$
    set empty=(`wc -l mrsat.qa.$$`);
    if ($empty[1] == 0) then
	echo "WARNING: There are no ATN=QA in MRSAT"
    else
	set ct=`comm -23 mrcoc.qa.$$ mrsat.qa.$$ | wc -l`
	if ($ct != 0) then
	    echo "ERROR: COA value in MRCOC is not a QA attribute in MRSAT"
	    comm -23 mrcoc.qa.$$ mrsat.qa.$$ | sed 's/^/  /'
	endif
    endif
    rm -f mrcoc.qa.$$ mrsat.qa.$$

    #
    #  Verify MRSAT.CUI in MRCOC.CUI1 (where ATN like MED% and COT=L,LQ,LQB)
    #
    # we only want MED attributes in the MBD/MED range
    # that also have *'d counts
    # skip this step if MRCOC is empty
    set empty=(`wc -l $mrcoc`)
    if ($empty[1] != 0) then
    echo "    Verify MRSAT.CUI in MRCOC.CUI1 (where ATN like MED% and COT=L,LQ,LQB)"
    $PATH_TO_PERL -ne 'split /\|/; \
        unless ($y) { ($d,$d,$d,$d,$mon,$y) = localtime; \
          if ($mon == 11) { $y++; } $y+=1890;} \
	$year = 0; \
	if ($_[8] =~ /^MED(\d\d\d\d)$/) { $year=$1; } \
	print "$_[0]\n" \
	  if ($year && $_[9] eq "NLM-MED" && $year >= $y && $_[10] =~ /^\*/)' $mrsat |\
	sort -u >! mrsat.tmp1.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[2]\n" \
        if ($_[5] eq "L" || $_[5] eq "LQ" || $_[5] eq "LQB")' $mrcoc |\
	sort -u >! mrsat.tmp2.$$
    set ct=`join -t\| -v 1 -j 1 mrsat.tmp1.$$ mrsat.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI in MRSAT with MED attribute not in MRCOC"
	join -v 1 -t\| -j 1 mrsat.tmp1.$$ mrsat.tmp2.$$ | sed 's/^/  /'
    endif

    #
    #  Verify MRSAT.CUI in MRCOC.CUI2 (where ATN like MED% and COT=L,LQ,LQB)
    #
    #  skip this step if MRCOC is empty
    echo "    Verify CUI in MRCOC.CUI2 (where ATN like MED% and COT=L,LQ,LQB)"
    cut -d\| -f 2 mrsat.tmp2.$$ | sort -u >! mrsat.tmp3.$$
    set ct=`join -v 1 -t\| -j 1 mrsat.tmp1.$$ mrsat.tmp3.$$ | wc -l`
    if ($ct != 0) then
        # it is possible that the problem CUIs are just
        # represented as CUI1 LQ rows with null CUI2.
        # ignore these cases  (the join on MRCOC checks,
        # and the egrep filters out the null CUI2-LQ cases.
        join -v 1 -t\| -j 1 mrsat.tmp1.$$ mrsat.tmp3.$$ >! mrsat.tmp4.$$
	set ct=`join -t\| -j 1 -o 2.2 2.4 mrsat.tmp4.$$ $mrcoc | egrep -v '\|\|(MED|MBD).*\|LQ\|' | wc -l`
	if ($ct != 0) then
	    echo "WARNING: CUI in MRSAT with MED attribute not in MRCOC.CUI2"
	    echo ""
	    echo "    These are not probably not errors, they most likely"
	    echo "    have blank CUI2 entries in MRCOC."
	    echo ""
	    join -t\| -j 1 -o 1.1 mrsat.tmp4.$$ $mrcoc | sed 's/^/  /'
	endif
    endif
    rm -f mrsat.tmp[1234].$$
endif

    #
    #  Verify this rule:
    #        AUI1 is DMeSH where COT=L,LQ  (tmp1)
    #        AUI1 is QMeSH where COT=LQB   (tmp2)
    #        AUI2 is DMeSH where COT=L,LQB (tmp3)
    #        AUI2 is QMeSH where COT=LQ    (tmp4)
    #
    #        DMeSH means MRCONSO.SCD starts with D and MRSAT has an AQL attribute
    #            (if COT has qualifiers)
    #        QMeSH means MRCONSO.SCD starts with Q and MRSAT has an QA attribute
    #
    echo "    Verify COT Semantics"
    # get dmesh AUIs and allowable qualifiers
    $PATH_TO_PERL -ne 'split /\|/; \
	if ($_[9] =~ /^MSH/ && $_[5] =~ /^D/ && $_[8] eq "AQL") { \
	  foreach $qa (split / /, $_[10]) { print "$_[3]|$qa\n" } \
	  print "$_[3]|\n" } ' $mrsat >! dmesh.$$
    # also get dmesh that has no AQL attributes
    $PATH_TO_PERL -ne 'split /\|/; print "$_[7]|\n" \
	if ($_[11] =~ /^MSH/ && $_[12] eq "MH" && $_[13] =~ /^D/) ' $mrconso >> dmesh.$$
    sort -u -o dmesh.$$ dmesh.$$

    # get qmesh
    $PATH_TO_PERL -ne 'split /\|/; \
	if ($_[9] =~ /^MSH/ && $_[5] =~ /^Q/ && $_[8] eq "QA") { \
	  print "$_[3]\n"; } ' $mrsat | sort -u >! qmesh.$$

    # get AUI1 where COT=L,LQ, also get COA's
    $PATH_TO_PERL -ne 'split /\|/; if ($_[5] eq "L" || $_[5] eq "LQ") { \
	foreach $p (split /,/, $_[7]) { \
	  ($q,$f) = split /=/, $p; print "$_[1]|$q\n"; } } ' $mrcoc |\
        sed 's/<>//' | sort -u >! mrcoc.tmp1.$$

    # get AUI1 where COT=LQB
    awk -F\| '$6=="LQB" { print $2 }' $mrcoc | sort -u >! mrcoc.tmp2.$$

    # get AUI2 where COT=L,LQB.  COA only applies to AUI1, so don't get it here
    awk -F\| '$6=="L" || $6=="LQB" { print $4"|" }' $mrcoc | sort -u >! mrcoc.tmp3.$$

    # get AUI2 where COT=LQB
    awk -F\| '$6=="LQ" && $4!="" { print $4 }' $mrcoc | sort -u >! mrcoc.tmp4.$$

    set ct=`comm -23 mrcoc.tmp1.$$ dmesh.$$ | wc -l`
    if ($ct != 0) then
        echo "WARNING: There are AUI1 with COT=L,LQ and no matching DMeSH AQL in MRSAT"
        echo ""
        echo "  These are most likely inconsistencies between qualifiers used"
        echo "  in MEDLINE and those officially allowed by the most recent MeSH"
	# comm -23 mrcoc.tmp1.$$ dmesh.$$ | sed 's/^/  /'
    endif

    set ct=`comm -23 mrcoc.tmp2.$$ qmesh.$$ | wc -l`
    if ($ct != 0) then
        echo "Error: There are AUI1 with COT=LQB and no matching QMeSH QA in MRSAT"
	comm -23 mrcoc.tmp2.$$ qmesh.$$ | sed 's/^/  /'
    endif

    set ct=`comm -23 mrcoc.tmp3.$$ dmesh.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are AUI2 with COT=L,LQB and no matching DMeSH AQL in MRSAT"
	comm -23 mrcoc.tmp3.$$ dmesh.$$ | sed 's/^/  /'
    endif

    set ct=`comm -23 mrcoc.tmp4.$$ qmesh.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are AUI2 with COT=LQ and no matching QMeSH QA in MRSAT"
	comm -23 mrcoc.tmp4.$$ qmesh.$$ | sed 's/^/  /'
    endif
    rm -f dmesh.$$ qmesh.$$ mrcoc.tmp[1234].$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrcoc >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRCOC has incorrect sort order"
    endif


else if ($target == "MRCONSO") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif
    if (! -e $mrrank) then
	echo "ERROR: required file $mrrank cannot be found"
	exit 1
    endif
    #
    #   Verify field formats
    #
    echo "    Verify field formats"
if ($mode == "submission") then
    checkfields.pl $mrconso
else
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|[^\|]*\|[^\|]*\|L\d{7}\|[^\|]*\|S\d{7}\|.\|A\d{7,8}\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]+\|[^\|]+\|[0-4]\|[YNEO]\|\d*\|/;' $mrconso >! MRCONSO.badfields.$$
    set cnt = `cat MRCONSO.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRCONSO.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.badfields.$$
endif

    #
    #   Verify SUPPRESS in MRDOC.VALUE
    #
    cut -d\| -f 17 $mrconso | sort -u >! MRCONSO.suppress.$$
    cut -d\| -f 2 $mrdoc | sort -u >! MRDOC.value.$$
    set cnt = `comm -23 MRCONSO.suppress.$$ MRDOC.value.$$ | wc -l`
    if ($cnt != 0) then
        echo "ERROR: Suppressible value in MRCONSO not in MRDOC"
        comm -23 MRCONSO.suppress.$$ MRDOC.value.$$
    endif
    rm -f MRCONSO.suppress.$$ MRDOC.value.$$

    #
    #   Verify SAB|TTY|SUPPRESS in MRRANK.SAB|TTY|SUPPRESS
    #
    if ($mode != "subset") then
	echo "    Verify SAB|TTY|SUPPRESS in MRRANK.SAB|TTY|SUPPRESS"
	cut -d\| -f 2,3,4 $mrrank | sort -u -o MRRANK.sts.$$
	awk -F\| '$17!="O"&&$17!="E" {print $12"|"$13"|"$17}' $mrconso | sort -u -o MRCONSO.sts.$$
	set cnt=(`comm -13 MRRANK.sts.$$ MRCONSO.sts.$$ | wc -l `)
	if ($cnt[1] != 0) then
	    echo "ERROR: SAB,TTY,SUPPRESS from MRRANK not in MRCONSO"
	    comm -13 MRRANK.sts.$$ MRCONSO.sts.$$ | sed 's/^/  /'
	endif
	rm -f MRRANK.sts.$$ MRCONSO.sts.$$
    endif

    #
    #   Verify SAB|TTY in MRRANK.SAB|TTY
    #

    echo "    Verify SAB|TTY in MRRANK.SAB|TTY"
    cut -d\| -f 2,3 $mrrank | sort -u -o MRRANK.sabtty.$$
    cut -d\| -f 12,13 $mrconso | sort -u -o MRCONSO.sabtty.$$
    set cnt=(`comm -13 MRRANK.sabtty.$$ MRCONSO.sabtty.$$ | wc -l`)
    if ($cnt[1] != 0) then
	echo "ERROR: SAB,TTY from MRCONSO not in MRRANK"
	comm -13 MRRANK.sabtty.$$ MRCONSO.sabtty.$$ | sed 's/^/  /'
    endif
    rm -f MRRANK.sabtty.$$ MRCONSO.sabtty.$$

if ($mode != "subset") then
    #
    # Verify CUI,STR where TTY=VPT IN MRSAB.VCUI,SON
    #
    # Verify that the row has a VCUI (MTH,NLM-MED,SRC do not)
    #

    echo "    Verify CUI,STR where TTY=VPT IN MRSAB.VCUI,SON"
    awk -F\| '($13=="VPT"){print $1"|"$15}' $mrconso | sort -u >! mrconso.tmp1.$$
    set empty=(`wc -l mrconso.tmp1.$$`)
    if ($empty[1] == 0) then
	echo "WARNING:  There are no VPT in MRCONSO"
    else
	set ct=`cut -d\| -f 1,5 $mrsab | grep '^C' | sort -u | comm -23 - mrconso.tmp1.$$ | wc -l`
	if ($ct != 0) then
	    echo "ERROR:  MRSAB.VCUI,SON not in CUI,STR"
	    cut -d\| -f 1,5 $mrsab | sort -u | comm -23 -  mrconso.tmp1.$$ | sed 's/^/  /'
	endif
    endif

    #
    # Verify CUI,STR where TTY=SSN IN MRSAB.RCUI,SSN
    #
    echo "    Verify CUI,STR where TTY=SSN IN MRSAB.RCUI,SSN"
    awk -F\| '($13=="SSN"){print $1"|"$15}' $mrconso | sort -u >! mrconso.tmp1.$$
    set empty=(`grep -vc "Metathesaurus Names" mrconso.tmp1.$$`)
    if ($empty == 0) then
	echo "WARNING: Only Metathesaurus Names entry exist in MRCONSO"
    else
	set ct=`cut -d\| -f 2,24 $mrsab | sort -u | comm -23 - mrconso.tmp1.$$ | wc -l`
	if ($ct != 0) then
	    echo "ERROR:  MRSAB.RCUI,SSN not in CUI,STR"
	    cut -d\| -f 2,24 $mrsab | sort -u | comm -23 -  mrconso.tmp1.$$ | sed 's/^/  /'
	endif
    endif


    #
    # Verify CUI,STR where TTY=VAB IN MRSAB.VCUI,VSAB
    #
    # Verify that the row has a VCUI (MTH,NLM-MED,SRC do not)
    #
    echo "    Verify CUI,STR where TTY=VAB IN MRSAB.VCUI,VSAB"
    awk -F\| '($13=="VAB"){print $1"|"$15}' $mrconso | sort -u >! mrconso.tmp1.$$
    set empty=(`wc -l mrconso.tmp1.$$`)
    if ($empty[1] == 0) then
	echo "WARNING: There are no VAB's in MRCONSO"
    else
	set ct=`cut -d\| -f 1,3 $mrsab | grep '^C' | sort -u | comm -23 - mrconso.tmp1.$$ | wc -l`
	if ($ct != 0) then
	    echo "ERROR:  MRSAB.VCUI,VSAB not in CUI,STR"
	    cut -d\| -f 1,3 $mrsab | sort -u | comm -23 -  mrconso.tmp1.$$ | sed 's/^/  /'
	endif
    endif

    #
    # Verify CUI,STR where TTY=RAB IN MRSAB.RCUI,RSAB
    #
    echo "    Verify CUI,STR where TTY=RAB IN MRSAB.RCUI,RSAB"
    awk -F\| '($13=="RAB"){print $1"|"$15}' $mrconso | sort -u >! mrconso.tmp1.$$
    set empty=(`wc -l mrconso.tmp1.$$`)
    if ($empty[1] == 0) then
	echo "WARNING: There are no RAB's in MRCONSO"
    else
	set ct=`cut -d\| -f 2,4 $mrsab | sort -u | comm -23 - mrconso.tmp1.$$ | wc -l`
	if ($ct != 0) then
	    echo "ERROR:  CUI,STR not in MRSAB.RCUI,RSAB"
	    cut -d\| -f 2,4 $mrsab | sort -u | comm -23 -  mrconso.tmp1.$$ | sed 's/^/  /'
	endif
    endif
    rm -f mrconso.tmp1.$$
endif

    #
    #   Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT
    #
    echo "    Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
    cut -d\| -f2 $mrconso | sort -u >! MRCONSO.LAT.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="LAT"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.LAT.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  LAT not in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
	awk -F\| '$3=="expanded_form"&&$1=="LAT"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.LAT.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.LAT.$$

if ($mode == "subset") then
    #
    #   Verify TS in MRDOC.VALUE where MRDOC.DOCKEY=TS
    #
    echo "    Verify TS in MRDOC.VALUE where MRDOC.DOCKEY=TS"
    cut -d\| -f3 $mrconso | sort -u >! MRCONSO.TS.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="TS"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.TS.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  TS not in MRDOC.VALUE where MRDOC.DOCKEY=TS"
	awk -F\| '$3=="expanded_form"&&$1=="TS"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.TS.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.TS.$$

    #
    #   Verify STT in MRDOC.VALUE where MRDOC.DOCKEY=STT
    #
    echo "    Verify STT in MRDOC.VALUE where MRDOC.DOCKEY=STT"
    cut -d\| -f5 $mrconso | sort -u >! MRCONSO.STT.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="STT"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.STT.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  STT not in MRDOC.VALUE where MRDOC.DOCKEY=STT"
	awk -F\| '$3=="expanded_form"&&$1=="STT"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.STT.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.STT.$$
endif


    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f12 $mrconso | sort -u >! MRCONSO.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCONSO.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCONSO.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.SAB.$$

    #
    #   Verify TTY in MRDOC.VALUE where MRDOC.DOCKEY=TTY
    #
    echo "    Verify TTY in MRDOC.VALUE where MRDOC.DOCKEY=TTY"
    cut -d\| -f13 $mrconso | sort -u >! MRCONSO.TTY.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="TTY"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.TTY.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  TTY not in MRDOC.VALUE where MRDOC.DOCKEY=TTY"
	awk -F\| '$3=="expanded_form"&&$1=="TTY"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.TTY.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.TTY.$$

    #
    #   Verify CUI|SUI count = CUI|LUI|SUI count
    #
    echo "    Verify CUI|SUI count = CUI|LUI|SUI count"
    cut -d\| -f 1,4,6 $mrconso | sort -u -T . >! MRCONSO.uis.cls.$$
    set cs_cnt = `cut -d\| -f1,3 MRCONSO.uis.cls.$$ | sort -u -T . | wc -l`
    set cls_cnt = `cat MRCONSO.uis.cls.$$ | wc -l`
    if ($cs_cnt != $cls_cnt) then
	echo "ERROR: CUI,SUI count does not match CUI,LUI,SUI count"
	echo "ERROR: ($cs_cnt, $cls_cnt)"
    endif

    #
    #   Verify LUI|SUI count = SUI count
    #
    echo "    Verify LUI|SUI count = SUI count"
    cut -d\| -f2,3 MRCONSO.uis.cls.$$ | sort -u -T . >! MRCONSO.uis.ls.$$
    set ls_cnt = `cat MRCONSO.uis.ls.$$ | wc -l`
    set scnt = `cut -d\| -f3 MRCONSO.uis.cls.$$ | sort -u -T .  | wc -l`
    if ($ls_cnt != $scnt) then
	echo "ERROR: LUI,SUI count does not match SUI count"
	echo "ERROR: ($ls_cnt, $scnt)"
    endif

    #
    #   Verify SUI is unique across LUI
    #
    echo "    Verify SUI is unique across LUI"
    join -t\| -j1 2 -j2 2 -o 1.1 2.1 MRCONSO.uis.ls.$$ MRCONSO.uis.ls.$$ |\
	awk -F\| '$1!=$2 {print $0}' >! badsui.$$
    set cnt=(`wc badsui.$$`)
    if ($cnt[1] != 0) then
	echo "ERROR: The following SUIs have more than 1 LUI"
	cat badsui.$$ | sed 's/^/  /'
    endif
    rm -f badsui.$$ MRCONSO.uis.{cls,ls}.$$

    #
    #   Verify LUI is unique across LAT (2 is LUI, 1 is LAT)
    #
    if ($mode != "submission") then
	    echo "    Verify LUI is unique across LAT (2 is LUI, 1 is LAT)"
	    cut -d\| -f 2,4,6 $mrconso >! MRCONSO.tmp2.$$
	    sort -t\| -k 2,2 -o MRCONSO.tmp2.$$ MRCONSO.tmp2.$$
	    join -t\| -j1 2 -j2 2 -o 1.1 2.1 1.2 MRCONSO.tmp2.$$ MRCONSO.tmp2.$$ |\
		awk -F\| '$1!=$2 {print $0}' >! badlui.$$
	    set cnt=(`wc badlui.$$`)
	    if ($cnt[1] != 0) then
		echo "ERROR: The following LUIs have more than 1 LAT"
		cat badlui.$$ | sed 's/^/  /'
	    endif
	    rm -f badlui.$$

	    #
	    #   Verify SUI is unique across LAT
	    #
	    echo "    Verify SUI is unique across LAT"
	    sort -t\| -k 3,3 -o MRCONSO.tmp2.$$ MRCONSO.tmp2.$$
	    join -t\| -j1 3 -j2 3 -o 1.1 2.1 1.3 MRCONSO.tmp2.$$ MRCONSO.tmp2.$$ |\
		awk -F\| '$1!=$2 {print $0}' >! badsui.$$
	    set cnt=(`wc badsui.$$`)
	    if ($cnt[1] != 0) then
		echo "ERROR: The following SUIs have more than 1 LAT"
		cat badsui.$$ | sed 's/^/  /'
	    endif
	    rm -f badsui.$$
	    rm -f MRCONSO.tmp2.$$
    endif
    #
    #   Verify min(length(str)) > 0
    #
    echo "    Verify min(length(str)) > 0"
    $PATH_TO_PERL -ne 'split /\|/; print length($_[14]),"\n";' $mrconso |\
        sort -u -T . -n -o MRCONSO.minmax.$$
    set min_length=`head -1 MRCONSO.minmax.$$`
    if ($min_length == 0) then
	echo "ERROR: The minimum STR length is zero"
    endif
    rm -f MRCONSO.minmax.$$

if ($mode == "subset") then
    #
    #   Verify that there is one P|PF per CUI,LAT
    #
    echo "    Verify that there is one [P]|PF per CUI,LAT"
    set ppf_ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[6] eq "Y" && $_[2] eq "P" && $_[4] eq "PF"' $mrconso | wc -l`
    set cuilat_ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]\n"' $mrconso | sort -u | wc -l`
    if ($ppf_ct != $cuilat_ct) then
	echo "ERROR: The P|PF count ($ppf_ct) does not equal the CUI|LAT ($cuilat_ct) count"
    endif

    #
    #   Distinct CUI/LAT count should match distinct CUI/LAT/LUI
    #   count where TS = P
    #
    echo "    Verify distinct CUI/LAT count should match distinct CUI/LAT/LUI where TS = P"
     set cl_cnt = `cut -d\| -f1,2 $mrconso | sort -u | wc -l`
     set cll_cnt = `awk -F\| '$3=="P"{print $1"|"$2"|"$4}' $mrconso | sort -u | wc -l`
     if ($cl_cnt != $cll_cnt) then
	echo "ERROR: The distinct CUI/LAT count ($cl_cnt) does not equal the CUI/LAT/LUI count ($cll_cnt)"
     endif

    #
    #   Distinct CUI/LAT/LUI count should match distinct CUI/LAT/SUI
    #   count where STT = PF
    #
    echo "    Verify distinct CUI/LAT/LUI count should match distinct CUI/LAT/SUI where STT = PF"
     set cll_cnt = `cut -d\| -f1,2,4 $mrconso | sort -u | wc -l`
     set cls_cnt = `awk -F\| '$5=="PF"{print $1"|"$2"|"$6}' $mrconso | sort -u | wc -l`
     if ($cll_cnt != $cls_cnt) then
	echo "ERROR: The distinct CUI/LAT/LUI count ($cll_cnt) does not equal the CUI/LAT/SUI count ($cls_cnt)"
     endif

    #
    #   Distinct CUI/LAT/SUI count should match distinct CUI/LAT/AUI
    #   count where ISPREF = Y
    #
    echo "    Verify distinct CUI/LAT/SUI count should match distinct CUI/LAT/AUI where ISPREF = Y"
     set cls_cnt = `cut -d\| -f1,2,6 $mrconso | sort -u | wc -l`
     set cla_cnt = `awk -F\| '$7=="Y"{print $1"|"$2"|"$8}' $mrconso | sort -u | wc -l`
     if ($cls_cnt != $cla_cnt) then
	echo "ERROR: The distinct CUI/LAT/SUI count ($cls_cnt) does not equal the CUI/LAT/AUI count ($cla_cnt)"
     endif

    #
    #   Verify that there is one PF SUI per CUI,LUI
    #
    echo "    Verify that there is one PF SUI per CUI,LUI"
    set pf_ct=`$PATH_TO_PERL -ne 'split /\|/; print "x\n" if $_[6] eq "Y" && $_[4] eq "PF"' $mrconso | wc -l`
    set cuilui_ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]\n"' $mrconso | sort -u | wc -l`
    if ($pf_ct != $cuilui_ct) then
	echo "ERROR: The S|PF count does not equal the CUI|LUI count"
    endif

endif

    #
    #   Verify CUI is unique (where tty=PN,sab=MTH)
    #
    echo "    Verify CUI is unique (where tty=PN,sab=MTH)"
    $PATH_TO_PERL -ne 'split /\|/; print $_[0],"\n" if $_[11] eq "MTH" && $_[12] eq "PN"' $mrconso |\
    sort | uniq -d >! MRCONSO.mult.pn.$$
    set ct=(`wc -l MRCONSO.mult.pn.$$`)
    if ($ct[1] != 0) then
        echo "ERROR: Multiple MTH/PNs in the following CUIs"
        cat MRCONSO.mult.pn.$$ | $PATH_TO_PERL -pe 's/^/\t/' | sed 's/^/  /'
    endif
    rm -f MRCONSO.mult.pn.$$

    #
    #   Verify AUI unique
    #
    echo "    Verify AUI unique"
    set ct=`cut -d\| -f8 $mrconso | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: AUI is not unique"
	cut -d\| -f8 $mrconso |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify SUI is unique across STR
    #
    echo "    Verify SUI is unique across STR"
    cut -d\| -f2,6,15 $mrconso | sort -u -t\| -k 2,2 >! MRCONSO.tmp3.$$
    set cnt=`cut -d\| -f2 MRCONSO.tmp3.$$ | uniq -d | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following SUIs have more than 1 STR"
	cut -d\| -f2 MRCONSO.tmp3.$$ | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify STR is unique across SUI
    #
    echo "    Verify STR is unique across SUI"
    sort -t\| -k 3,3 -k 1,1 -o MRCONSO.tmp3.$$ MRCONSO.tmp3.$$
    set cnt=`cut -d\| -f1,3 MRCONSO.tmp3.$$ | uniq -d | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following STRs have more than 1 SUI"
	cut -d\| -f1,3 MRCONSO.tmp3.$$ | uniq -d | sed 's/^/  /'
    endif

    rm -f MRCONSO.tmp3.$$

    #
    # Verify SAB in MRSAB.RSAB (where TFR,CFR != null,0)
    #
    if ($mode != "subset") then
	echo "    Verify SAB in MRSAB.RSAB"
	$PATH_TO_PERL -ne 'split /\|/; print "$_[3]\n" if $_[14] != "" && $_[15] != "";' \
	    $mrsab | sort -u >! mrsab.rsab.$$
	cut -d\| -f 12 $mrconso | sort -u >! mrconso.sab.$$
	set ct=`comm -23 mrsab.rsab.$$ mrconso.sab.$$ | wc -l`
	if ($ct > 0) then
	    echo "ERROR: MRCONSO.SAB not in MRSAB.RSAB.$$"
	    comm -23 mrsab.rsab.$$ mrconso.sab.$$ | sed 's/^/  /'
	endif
	rm -f mrsab.rsab.$$ mrconso.sab.$$

    #
    # Verify SAB|LAT in MRSAB.RSAB,LAT & v.v.
    #
    echo "    Verify MRCONSO.SAB,LAT IN MRSAB.RSAB,LAT"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[11]|$_[1]\n"' $mrconso |\
       sort -u >! sab.lat.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[3]|$_[19]\n" if $_[19] ne "";' $mrsab |\
	sort -u >! rsab.lat.$$
    set ct=`diff sab.lat.$$ rsab.lat.$$ | wc -l`
    if ($ct > 0) then
        echo "ERROR: MRCONSO.SAB,LA does not match MRSAB.RSAB,LAT"
	    diff sab.lat.$$ rsab.lat.$$ | sed 's/^/  /'
    endif
    rm -f rsab.lat.$$ sab.lat.$$

    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrconso >> /dev/null
    if ($status != 0) then
	echo "ERROR: MRCONSO has incorrect sort order"
    endif

else if ($target == "MRCUI") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrcui) then
	echo "ERROR: required file $mrcui cannot be found"
	exit 1
    endif
    if (! -e $old_mrcui) then
	echo "ERROR: required file $old_mrcui cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $old_mrconso) then
	echo "ERROR: required file $old_mrconso cannot be found"
	exit 1
    endif
    if (! -e $deleted_cui) then
	echo "ERROR: required file $deleted_cui cannot be found"
	exit 1
    endif
    if (! -e $deleted_lui) then
	echo "ERROR: required file $deleted_lui cannot be found"
	exit 1
    endif
    if (! -e $deleted_sui) then
	echo "ERROR: required file $deleted_sui cannot be found"
	exit 1
    endif
    if (! -e $merged_cui) then
	echo "ERROR: required file $merged_cui cannot be found"
	exit 1
    endif
    if (! -e $merged_lui) then
	echo "ERROR: required file $merged_lui cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $mrcui"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|\d{4}..\|[^\|]*\|[^\|]*\||[^\|]*\|C.\d{6}\|[YN]?\|/;' $mrcui >! MRCUI.badfields.$$
    set cnt = `cat MRCUI.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRCUI.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRCUI.badfields.$$

    cut -d\| -f1 $mrconso | sort -u >! MRCONSO.uis.c.$$
    cut -d\| -f4 $mrconso | sort -u >! MRCONSO.uis.l.$$
    cut -d\| -f6 $mrconso | sort -u >! MRCONSO.uis.s.$$

    #
    #   Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    #
    echo "    Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    cut -d\| -f3 $mrcui | sort -u >! MRCUI.REL.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRCUI.REL.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  REL not in MRDOC.VALUE where MRDOC.DOCKEY=REL"
	awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRCUI.REL.$$ | sed 's/^/  /'
    endif
    rm -f MRCUI.REL.$$

    #
    #   Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    #
    echo "    Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    cut -d\| -f4 $mrcui | sort -u >! MRCUI.RELA.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRCUI.RELA.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  RELA not in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
	awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRCUI.RELA.$$ | sed 's/^/  /'
    endif
    rm -f MRCUI.RELA.$$

    #
    #  Verify CUI1 does not have REL=SY and REL!=SY rows
    #
    echo "    Verify CUI1 does not have REL=SY and non-REL=SY rows"
    cut -d\| -f 1,3 $mrcui | sort -u >! mrcui.tmp.$$
    set ct=`join -t\| -j 1 -o 1.1 1.2 2.2 mrcui.tmp.$$ mrcui.tmp.$$ | fgrep '|SY|' | fgrep -v '|SY|SY' | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 has REL=SY and REL!=SY rows"
	join -t\| -j 1 -o 1.1 1.2 2.2 mrcui.tmp.$$ mrcui.tmp.$$ | fgrep '|SY|' | fgrep -v '|SY|SY'  | sed 's/^/  /'
    endif
    rm -f mrcui.tmp.$$

    #
    #  Verify CUI1 does not have REL=DEL and REL!=DEL rows
    #
    echo "    Verify CUI1 does not have REL=DEL and REL!=DEL rows"
    cut -d\| -f 1,3 $mrcui | sort -u >! mrcui.tmp.$$
    set ct=`join -t\| -j 1 -o 1.1 1.2 2.2 mrcui.tmp.$$ mrcui.tmp.$$ | fgrep '|DEL|' | fgrep -v '|DEL|DEL' | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 has REL=DEL and REL!=DEL"
	join -t\| -j 1 -o 1.1 1.2 2.2 mrcui.tmp.$$ mrcui.tmp.$$ |\
	    fgrep '|DEL|' | fgrep -v '|DEL|DEL' | sed 's/^/  /'
    endif
    rm -f mrcui.tmp.$$

    #
    #  Verify CUI1 and CUI2 are distinct sets
    #
    echo "    Verify CUI1 and CUI2 are distinct sets"
    cut -d\| -f 1 $mrcui | sort -u >! mrcui.tmp1.$$
    cut -d\| -f 6 $mrcui | grep '^C' | sort -u >! mrcui.tmp2.$$
    set ct=`comm -12 mrcui.tmp1.$$ mrcui.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 and CUI2 are not distinct sets"
	comm -12 mrcui.tmp1.$$ mrcui.tmp2.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI1 not in MRCONSO.CUI
    #
    echo "    Verify CUI1 not in MRCONSO.CUI"
    set ct=`comm -12 mrcui.tmp1.$$ MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 in MRCONSO.CUI"
	comm -12 mrcui.tmp1.$$ MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI2 in MRCONSO.CUI (where REL!=DEL)
    #
    echo "    Verify CUI2 in MRCONSO.CUI (where REL!=DEL)"
    set ct=`comm -23 mrcui.tmp2.$$ MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI2 not in MRCONSO.CUI"
	comm -23 mrcui.tmp2.$$ MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif
    rm -f mrcui.tmp[12].$$

    #
    #  Verify CUI1 is unique (where REL=SY) (only merged with one thing)
    #
    echo "    Verify CUI1 is unique (where REL=SY) (only merged with one thing)"
    set ct=`fgrep '|SY|' $mrcui | cut -d\| -f 1 | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: Non-unique CUI1 where REL=SY"
	fgrep '|SY|' $mrcui | cut -d\| -f 1 | sort | uniq -d | sed 's/^/  /'
    endif

    #
    #  Verify CUI1|CUI2 unique
    #
    echo "    Verify CUI1|CUI2 unique"
    set ct=`awk -F\| '$6!=""{print $1"|"$6}' $mrcui | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: Non-unique CUI1,CUI2"
	awk -F\| '$6!=""{print $1"|"$6}' $mrcui | sort | uniq -d
    endif

    #
    #  Verify VER is not null
    #
    echo "    Verify VER is not null"
    set ct=`awk -F\| '$2=="" {print $0}' $mrcui | wc -l`
    if ($ct != 0) then
        echo "ERROR: MRCUI rows with VER=null"
	awk -F\| '$2=="" {print $0}' $mrcui | sed 's/^/  /'
    endif


    #
    #  Verify all MRCONSO.CUI, MRCUI.CUI1 from previous version
    #  in MRCONSO.CUI from current version
    #
    echo "    Verify all old CUIs in current version"
    (cut -d\| -f 1 $old_mrconso; cut -d\| -f 1 $old_mrcui) |\
      sort -u -o old.cuis.$$
    (cut -d\| -f 1 $mrconso; cut -d\| -f 1 $mrcui) |\
      sort -u -o new.cuis.$$
    set ct=`comm -23 old.cuis.$$ new.cuis.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: Old CUIs not in current version"
	comm -23 old.cuis.$$ new.cuis.$$ | sed 's/^/  /'
    endif
    rm -r -f new.cuis.$$ old.cuis.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrcui >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRCUI has incorrect sort order"
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $deleted_cui"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|[^\|]*\|/;' $deleted_cui >! DELETED_CUI.badfields.$$
    set cnt = `cat DELETED_CUI.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat DELETED_CUI.badfields.$$ | sed 's/^/  /'
    endif
    rm -f DELETED_CUI.badfields.$$

    #
    #  Verify CUI not in MRCONSO.CUI
    #
    echo "    Verify CUI not in MRCONSO.CUI"
    set ct=`cut -d\| -f1 $deleted_cui | comm -12 - MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 in MRCONSO.CUI"
	cut -d\| -f1 $deleted_cui | comm -12 - MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $deleted_cui >> /dev/null
    if ($status != 0) then
        echo "ERROR: DELETED.CUI has incorrect sort order"
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $deleted_lui"
    $PATH_TO_PERL -ne 'print unless /^L\d{7}\|[^\|]*\|/;' $deleted_lui >! DELETED_LUI.badfields.$$
    set cnt = `cat DELETED_LUI.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat DELETED_LUI.badfields.$$ | sed 's/^/  /'
    endif
    rm -f DELETED_LUI.badfields.$$

    #
    #  Verify LUI not in MRCONSO.LUI
    #
    echo "    Verify LUI not in MRCONSO.LUI"
    set ct=`cut -d\| -f1 $deleted_lui | comm -12 - MRCONSO.uis.l.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: LUI1 in MRCONSO.LUI"
	cut -d\| -f1 $deleted_lui | comm -12 - MRCONSO.uis.l.$$ | sed 's/^/  /'
    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $deleted_lui >> /dev/null
    if ($status != 0) then
        echo "ERROR: DELETED.LUI has incorrect sort order"
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $deleted_sui"
    $PATH_TO_PERL -ne 'print unless /^S\d{7}\|[^\|]*\|/;' $deleted_sui >! DELETED_SUI.badfields.$$
    set cnt = `cat DELETED_SUI.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat DELETED_SUI.badfields.$$ | sed 's/^/  /'
    endif
    rm -f DELETED_SUI.badfields.$$

    #
    #  Verify SUI not in MRCONSO.SUI
    #
    echo "    Verify SUI not in MRCONSO.SUI"
    set ct=`cut -d\| -f1 $deleted_sui | comm -12 - MRCONSO.uis.s.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: SUI1 in MRCONSO.SUI"
	cut -d\| -f1 $deleted_sui | comm -12 - MRCONSO.uis.s.$$ | sed 's/^/  /'
    endif

    #
    #  Verify SSTR not in MRCONSO.SUI
    #
    echo "    Verify SSTR not in MRCONSO.SUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[1]|$_[14]\n";' $mrconso |\
        sort -u -o MRCONSO.latstr.$$
    set ct=(`cut -d\| -f2,3 $deleted_sui | sort -u | comm -12 - MRCONSO.latstr.$$ | wc -l)`
    if ($ct[1] != 0) then
        echo "ERROR: SSTR in MRCONSO.SUI"
	cut -d\| -f2,3 $deleted_sui | sort -u | comm -12 - MRCONSO.latstr.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.latstr.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $deleted_sui >> /dev/null
    if ($status != 0) then
        echo "ERROR: DELETED.SUI has incorrect sort order"
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $merged_cui"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|C.\d{6}\|/;' $merged_cui >! MERGED_CUI.badfields.$$
    set cnt = `cat MERGED_CUI.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MERGED_CUI.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MERGED_CUI.badfields.$$

    #
    #  Verify CUI1 not in MRCONSO.CUI
    #
    echo "    Verify CUI1 not in MRCONSO.CUI"
    set ct=`cut -d\| -f1 $merged_cui | comm -12 - MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 in MRCONSO.CUI"
	cut -d\| -f1 $merged_cui | comm -12 - MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI2 in MRCONSO.CUI
    #
    echo "    Verify CUI2 in MRCONSO.CUI"
    set ct=`cut -d\| -f2 $merged_cui | sort -u | comm -23 - MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI2 not in MRCONSO.CUI"
	cut -d\| -f2 $merged_cui | sort -u | comm -23 - MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI1,CUI2 in MRCUI.CUI1,CUI2
    #
    echo "    Verify  CUI1,CUI2 in MRCUI.CUI1,CUI2"
    set ct=`awk -F\| '$6!=""{print $1"|"$6"|"}' $mrcui | sort -u | comm -13 - $merged_cui | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1,CUI2 not in MRCONSO.CUI"
	awk -F\| '$6!=""{print $1"|"$6"|"}' $mrcui |  sort -u | comm -13 - $merged_cui | sed 's/^/  /'
    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $merged_cui >> /dev/null
    if ($status != 0) then
        echo "ERROR: MERGED.CUI has incorrect sort order"
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $merged_lui"
    $PATH_TO_PERL -ne 'print unless /^L\d{7}\|L\d{7}\|/;' $merged_lui >! MERGED_LUI.badfields.$$
    set cnt = `cat MERGED_LUI.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MERGED_LUI.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MERGED_LUI.badfields.$$

    #
    #  Verify LUI1 not in MRCONSO.LUI
    #
    echo "    Verify LUI1 not in MRCONSO.LUI"
    set ct=`cut -d\| -f1 $merged_lui | comm -12 - MRCONSO.uis.l.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: LUI1 in MRCONSO.LUI"
	cut -d\| -f1 $merged_lui | comm -12 - MRCONSO.uis.l.$$ | sed 's/^/  /'
    endif

    #
    #  Verify LUI2 in MRCONSO.LUI
    #
    echo "    Verify LUI2 in MRCONSO.LUI"
    set ct=`cut -d\| -f 2 $merged_lui | sort -u | comm -23 - MRCONSO.uis.l.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: LUI2 not in MRCONSO.LUI"
	cut -d\| -f 2 $merged_lui | sort -u | comm -23 - MRCONSO.uis.l.$$ | sed 's/^/  /'
    endif

    rm -f MRCONSO.uis.{c,l,s}.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $merged_lui >> /dev/null
    if ($status != 0) then
        echo "ERROR: MERGED.LUI has incorrect sort order"
    endif


else if ($target == "MRCXT") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrcxt) then
	echo "ERROR: required file $mrcxt cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|S\d{7}\|A\d{7,8}\|[^\|]*\|[^\|]*\|\d+\|(ANC|CCP|SIB|CHD)\|\d*\|[^\|]+\|C.\d{6}\|A\d{7,8}\|[^\|]*\|[^\|]*\|\+*\|\d*\|/;' $mrcxt >! MRCXT.badfields.$$
    set cnt = `cat MRCXT.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRCXT.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRCXT.badfields.$$

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f4 $mrcxt | sort -u >! MRCXT.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCXT.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCXT.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRCXT.SAB.$$

    #
    #   Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    #
    echo "    Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    cut -d\| -f13 $mrcxt | sort -u >! MRCXT.RELA.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRCXT.RELA.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  RELA not in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
	awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRCXT.RELA.$$ | sed 's/^/  /'
    endif
    rm -f MRCXT.RELA.$$

    #
    #  Verify no null codes
    #
    echo "    Verify no null codes"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print unless ($_[3]);' $mrcxt | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are $ct contexts connected to atoms with null codes"
    endif

    #
    #  Verify CUI,AUI in MRCONSO.CUI,AUI
    #
    echo "    Verify CUI,AUI in MRCONSO.CUI,AUI "
    cut -d\| -f1,8 $mrconso | sort -u -T . >! MRCONSO.uis.ca.$$
    cut -d\| -f 1,3 $mrcxt | sort -u >! mrcxt.tmp1.$$
    set ct=`comm -23 mrcxt.tmp1.$$ MRCONSO.uis.ca.$$ | wc -l`
     if ($ct != 0) then
        echo "ERROR: CUI,AUI not in MRCONSO.CUI,AUI"
	comm -23 mrcxt.tmp1.$$ MRCONSO.uis.ca.$$  | sed 's/^/  /'
    endif
    rm -f mrcxt.tmp1.$$ MRCONSO.uis.ca.$$

    #
    #  Verify CUI|SUI|AUI|SAB|SCD in MRCONSO.CUI|SUI|AUI|SAB|SCD
    #
    # Note: there are some cases where contexts connected
    #       to a particular atom are actually asserted by
    #       an SAB other than that atom's SAB.  The known
    #       exceptions are handled below.
    #
    echo "    Verify CUI|SUI|AUI|SAB|SCD in MRCONSO.CUI|SUI|AUI|SAB|SCD"
    cut -d\| -f 1,6,8,12,14 $mrconso | sort -u >! mrcxt.tmp1.$$
    cut -d\| -f 1-5 $mrcxt | sort -u >! mrcxt.tmp2.$$
    comm -13 mrcxt.tmp1.$$ mrcxt.tmp2.$$ >! mrcxt.tmp3.$$
    join -t\| -j 1 -o 1.1 1.2 2.2 1.3 2.3 1.4 2.4 1.5 2.5 mrcxt.tmp1.$$ mrcxt.tmp3.$$ |\
	$PATH_TO_PERL -ne 'chop; split /\|/; print "$_[0]|$_[2]|$_[4]|$_[6]|$_[8]\n" \
            if ($_[1] eq $_[2] && $_[7] eq $_[8] && $_[3] eq $_[4] && $_[5] ne $_[6] && \
	        !($_[5] eq "SRC" || \
  	         ($_[5] =~ /MTHCH/ && $_[6] =~ /CPT/) || \
		 ($_[5] =~ /MTHHH/ && $_[6] =~ /HCPCS/) || \
		 ($_[5] =~ /HCPT/ && $_[6] =~ /HCPCS/) || \
		 ($_[5] =~ /HCDT/ && $_[6] =~ /HCPCS/) || \
		 ($_[5] =~ /CDT/ && $_[6] =~ /HCPCS/) || \
		 ($_[5] =~ /ICPC/ && $_[6] =~ /ICPC2P/) || \
		 ($_[5] =~ /ICPC2E/ && $_[6] =~ /ICPC2P/)))' >! mrcxt.tmp4.$$
    set ct=`cat mrcxt.tmp4.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUI,SUI,AUI,SAB,SCD combinations in MRCXT not in MRCONSO"
	cat mrcxt.tmp4.$$ | sed 's/^/  /'
    endif
    rm -f mrcxt.tmp[1234].$$

    #
    #  Verify ANC ordering
    #
    echo "    Verify ANC ordering"
    $PATH_TO_PERL -ne '($cui,$sui,$aui,$sab,$scd,$cxn,$cxl,$rnk,$str,$cui2,$aui2,$hcd,$rel,$xc) =split /\|/; \
        $key = "$cui|$sui|$sab|$scd|$cxn"; \
	if ($key ne $prev_key && $prev_key) { \
           print "ERROR: Incorrect number of CCP rows ($ccp) for key\n$prev_key" if ($ccp != 1); \
	   $i = 1; foreach $k (sort {$a - $b} keys %anc) { \
		print "ERROR: Out of order ANC for key\n$prev_key" unless ($k == $i++) } \
	   $ccp=0; %anc=(); \
	} \
	$ccp++ if $cxl eq "CCP"; \
	$anc{$rnk}++ if $cxl eq "ANC"; \
        $prev_key = $key; ' $mrcxt >! MRCXT.ANC.errors.$$

    set ct=`cat MRCXT.ANC.errors.$$ | wc -l`
    if ($ct != 0) then
        cat MRCXT.ANC.errors.$$
    endif
    rm -f MRCXT.ANC.errors.$$

    #
    #  Verify XC flag is set to '+'
    #
    if ($mode != "subset") then
    set empty=(`wc -l $mrcxt`)
    if ($empty[1] != 0) then
	echo "    Verify XC flag is set to '+'"
	set ct=`awk -F\| '$14=="+"{print $14}' $mrcxt | wc -l`
	if ($ct == 0) then
	    echo "ERROR: There is no XC flag set to '+'"
	endif
    endif
endif
    #
    #  Verify no self-referential SIB rels (AUI1!=AUI2)
    #
    echo "    Verify no self-referential SIB rels (AUI1!=AUI2)"
    set ct=`awk -F\| '$7=="SIB" && $3==$11 {print $0}' $mrcxt | wc -l`
    if ($ct != 0) then
        echo "WARNING: Self-referential AUI COC Rows"
	awk -F\| '$7=="SIB" && $3==$11 {print $0}' $mrcxt | sed 's/^/  /'
    endif

    #  Verify sort order
    echo "    Verify sort order"
    sort -c -u $mrcxt >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRCXT has incorrect sort order"
    endif

else if ($target == "MRHIER") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrhier) then
	echo "ERROR: required file $mrhier cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|A\d{7,8}\|\d+\|(A\d{7,8})?\|[^\|]*\|[^\|]*\|(\.{0,1}A\d{7,8})*\|[^\|]*\|\d*\|/;' $mrhier >! MRHIER.badfields.$$
    set cnt = `cat MRHIER.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRHIER.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRHIER.badfields.$$

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f5 $mrhier | sort -u >! MRHIER.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRHIER.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRHIER.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRHIER.SAB.$$

    #
    #   Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    #
    echo "    Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    cut -d\| -f6 $mrhier | sort -u >! MRHIER.RELA.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRHIER.RELA.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  RELA not in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
	awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRHIER.RELA.$$ | sed 's/^/  /'
    endif
    rm -f MRHIER.RELA.$$

    #
    #  Verify CUI,AUI,CXN,SAB unique
    #
    echo "    Verify CUI,AUI,CXN,SAB unique"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]|$_[2]|$_[4]\n";' $mrhier | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI,AUI,CXN,SAB is not unique"
	$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]|$_[2]|$_[4]\n";' $mrhier |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #  Verify CUI,AUI in MRCONSO.CUI,AUI
    #
    echo "    Verify CUI,AUI in MRCONSO.CUI,AUI "
    cut -d\| -f 1,2 $mrhier | sort -u >! mrhier.tmp1.$$
    cut -d\| -f1,8 $mrconso | sort -u -T . >! MRCONSO.uis.ca.$$
    set ct=`comm -23 mrhier.tmp1.$$ MRCONSO.uis.ca.$$ | wc -l`
     if ($ct != 0) then
        echo "ERROR: CUI,AUI,SAB not in MRCONSO.CUI,AUI"
	comm -23 mrhier.tmp1.$$ MRCONSO.uis.ca.$$  | sed 's/^/  /'
    endif
    rm -f mrhier.tmp1.$$ MRCONSO.uis.ca.$$

    #
    #  Validate against MRCXT
    #
 if ($mode != "subset") then
  if (-e $mrcxt) then
    echo "    Validate against MRCXT"
    egrep '\|(ANC|CCP)\|' $mrcxt |\
       (sort -t \| -k1,1 -k3,3 -k4,4 -k6,6n -k7,7 -k8,8n;echo "") | \
    $PATH_TO_PERL -ne '($cui,$sui,$aui,$sab,$scd,$cxn,$cxl,$rnk,$str,$cui2,$aui2,$hcd,$rela,$xc,$cvf) =split /\|/; \
        $key = "$cui|$aui|$sab|$cxn"; \
        if ($cxl eq "ANC") { \
            $treenum .= "." if $treenum; \
            $treenum .= $aui2;} \
        if ($key ne $prev_key && $prev_key) { \
           print $line ; \
           $treenum= ($cxl eq "ANC" ? $aui2 : ""); \
           $prev_aui2 = $treenum; }\
        $line="$cui|$aui|$cxn|$prev_aui2|$sab|$rela|$treenum|$hcd|$cvf|\n"; \
        $prev_aui2 = $aui2 ; \
        $prev_key = $key; ' | sort  -u >! MRCXT.tmp.$$

    set ct=`diff MRCXT.tmp.$$ $mrhier | wc -l`
    if ($ct != 0) then
        echo "ERROR: MRHIER does not match with MRCXT"
	diff MRCXT.tmp.$$ $mrhier | sed 's/^/  /'
    endif
    rm -f MRCXT.tmp.$$
  endif
  endif
    #  Verify sort order
    echo "    Verify sort order"
    sort -c -u $mrhier >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRHIER has incorrect sort order"
    endif

else if ($target == "MRDEF") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrdef) then
	echo "ERROR: required file $mrdef cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|A\d{7,8}\|AT\d{8}\|[^\|]*\|[^\|]*\|[^\|]+\|[YNEO]\|\d*\|/;' $mrdef >! MRDEF.badfields.$$
    set cnt = `cat MRDEF.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRDEF.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.badfields.$$

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f5 $mrdef | sort -u >! MRDEF.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRDEF.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRDEF.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.SAB.$$

    #
    #   Verify CUI|ATUI unique
    #
    echo "    Verify CUI|ATUI unique"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[2]\n";' $mrdef | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI,ATUI is not unique"
	$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[2]\n";' $mrdef |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify ATUI unique
    #
    echo "    Verify ATUI unique"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]\n";' $mrdef | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: ATUI is not unique"
	$PATH_TO_PERL -ne 'split /\|/; print "$_[2]\n";' $mrdef |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify min(length(DEF))>10
    #
    echo "    Verify min(length(DEF))>10"
    $PATH_TO_PERL -ne 'split /\|/; print length($_[5]),"\n";' $mrdef |\
        sort -u  -n -o MRDEF.minmax.$$
    set min_length=`head -1 MRDEF.minmax.$$`
    if ($min_length < 10) then
	echo "WARNING: The minimum DEF length is less than 10"
    endif
    rm -f MRDEF.minmax.$$

    #
    #   Verify CUI in MRCONSO.CUI
    #
    echo "    Verify CUI in MRCONSO.CUI"
    cut -d\| -f1 $mrconso | sort -u >! MRCONSO.uis.c.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n"' $mrdef | sort -u >! MRDEF.uis.c.$$
    set ct=(`comm -23 MRDEF.uis.c.$$ MRCONSO.uis.c.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUIs in MRDEF not in MRCONSO"
	comm -23 MRDEF.uis.c.$$ MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.uis.c.$$ MRCONSO.uis.c.$$

    #
    #   Verify CUI,SAB in MRCONSO.CUI,SAB
    #
    echo "    Verify CUI,SAB in in MRCONSO.CUI,SAB"
    cut -d\| -f1,12 $mrconso | sort -u -o MRCONSO.sabs.$$
    cut -d\| -f1,5 $mrdef | grep -v "MTH" | sort -u -o MRDEF.sabs.$$
    set ct=(`comm -23 MRDEF.sabs.$$ MRCONSO.sabs.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUI,SAB in MRDEF not in MRCONSO"
	comm -23 MRDEF.sabs.$$ MRCONSO.sabs.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.sabs.$$ MRCONSO.sabs.$$

    #
    #   Verify ATUI unique
    #
    echo "    Verify ATUI unique"
    set ct=`cut -d\| -f3 $mrdef | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: ATUI is not unique"
	cut -d\| -f3 $mrdef |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify long attribute expansion
    #
    echo "    Verify long attribute expansion"
    set ct=`grep -c '<>Long_Attribute<>' $mrdef`
    if ($ct != 0) then
        echo "ERROR: MRDEF has unexpanded long attributes"
	grep '<>Long_Attribute<>' $mrdef | sed 's/^/  /'
    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrdef >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRDEF has incorrect sort order"
    endif

else if ($target == "MRFILESCOLS") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrfiles) then
	echo "ERROR: required file $mrfiles cannot be found"
	exit 1
    endif
    if (! -e $mrcols) then
	echo "ERROR: required file $mrcols cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^(MR|CHANGE|AMBIG)[^\|]*\|[^\|]+\|[A-Z0-9,]{1,}\|\d+\|\d+\|\d+\|/;' $mrfiles >! MRFILES.badfields.$$
    set cnt = `cat MRFILES.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRFILES.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRFILES.badfields.$$

    #
    #   Verify FIL,FMT matches MRCOLS.FILCOL
    #
    echo "    Verify FIL,FMT matches MRCOLS.FIL,COL"
    $PATH_TO_PERL -ne 'split/\|/; map((print "$_[0]|$_|\n"), split(/,/,$_[2]))' $mrfiles | sort -u >! MRFILES.tmp.$$
    set cnt = `awk -F\| '{print $7"|"$1"|"}' $mrcols | sort -u | comm -3 - MRFILES.tmp.$$ | wc -l `
    if ($cnt != 0) then
	echo "ERROR:  FIL,FMT does not match MRCOLS.FIL,COL"
	awk -F\| '{print $7"|"$1"|"}' $mrcols | sort -u | comm -3 - MRFILES.tmp.$$| sed 's/^/  /'
    endif
    rm -f MRFILES.tmp.$$

    #
    #   Verify FIL in MRCOLS.FIL
    #
    echo "    Verify FIL in MRCOLS.FIL"
    cut -d\| -f1 $mrfiles | sort -u >! MRFILES.tmp.$$
    set cnt = `cut -d\| -f7 $mrcols | sort -u | comm -13 - MRFILES.tmp.$$ | wc -l `
    if ($cnt != 0) then
	echo "ERROR:  FIL not in MRCOLS.FIL"
	cut -d\| -f7 $mrcols | sort -u | comm -13 - MRFILES.tmp.$$ | sed 's/^/  /'
    endif
    rm -f MRFILES.tmp.$$

    #
    #   Verify BTS > 0
    #
    echo "    Verify BTS > 0"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/;print unless $_[5] > 0;' $mrfiles | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  BTS <= 0"
	$PATH_TO_PERL -ne 'split/\|/;print "$_[0]\n" unless $_[5] > 0;' $mrfiles | sed 's/^/  /'
    endif

    #
    #   Verify RWS > 0
    #
    echo "    Verify RWS > 0"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/;print unless $_[4] > 0;' $mrfiles | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  RWS <= 0"
	$PATH_TO_PERL -ne 'split/\|/;print "$_[0]\n" unless $_[4] > 0;' $mrfiles | sed 's/^/  /'
    endif

    #
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrfiles >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRFILES has incorrect sort order"
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^[^\|]+\|[^\|]+\|[^\|]*\|\d+\|\d+\.\d\d\|\d+\|[^\|]*\|(?:char|varchar|integer|numeric)(?:\([\d,]+\))?\|/;' $mrcols >! MRCOLS.badfields.$$
    set cnt = `cat MRCOLS.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRCOLS.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRCOLS.badfields.$$

    #
    #   Verify FIL in MRFILES.FIL
    #
    echo "    Verify FIL in MRFILES.FIL"
    cut -d\| -f1 $mrfiles | sort -u >! MRFILES.tmp.$$
    set cnt = `cut -d\| -f7 $mrcols | sort -u | comm -23 - MRFILES.tmp.$$ | wc -l `
    if ($cnt != 0) then
	echo "ERROR:  FIL not in MRFILES.FIL"
	cut -d\| -f7 $mrcols | sort -u | comm -23 - MRFILES.tmp.$$ | sed 's/^/  /'
    endif
    rm -f MRFILES.tmp.$$

    #
    #   Verify MIN=AV=MAX=8 where COL=CUI
    #
    echo "    Verify MIN=AV=MAX=8 where COL=CUI"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "CUI") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN,AV,MAX != 8 where COL=CUI"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "CUI") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=AV=MAX=8 where COL=CUI1
    #
    echo "    Verify MIN=AV=MAX=8 where COL=CUI1"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "CUI1") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN,AV,MAX != 8 where COL=CUI1"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "CUI1") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=0, MAX=8 where COL=CUI2 and FIL=MRCOC
    #
    echo "    Verify MIN=0, MAX=8 where COL=CUI2 and FIL=MRCOC"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "CUI2" && $_[6] eq "MRCOC") { print unless $_[3] ==0 && $_[5] == 8 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN != 0 or MAX != 8 where COL=CUI2 and FIL=MRCOC"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "CUI2") { print unless $_[3] == 0 && $_[5] == 8 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=8, MAX=8 where COL=CUI2 and FIL!=MRCOC
    #
    echo "    Verify MIN=8, MAX=8 where COL=CUI2 and FIL!=MRCOC,CUI"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "CUI2" && $_[6] ne "MRCOC.RRF" && $_[6] ne "MRCUI.RRF") { print unless $_[3] ==8 && $_[5] == 8 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN != 8 or MAX != 8 where COL=CUI2 and FIL!=MRCOC,CUI"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "CUI2" && $_[6] ne "MRCOC.RRF" && $_[6] ne "MRCUI.RRF") { print unless $_[3] == 8 && $_[5] == 8 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=AV=MAX=8 where COL=LUI and FIL!=MRSAT
    #
    echo "    Verify MIN=AV=MAX=8 where COL=LUI and FIL!=MRSAT,MERGEDLUI"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "LUI" && $_[6] ne "MRSAT.RRF" && $_[6] ne "CHANGE/MERGEDLUI.RRF") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN,AV,MAX != 8 where COL=LUI and FIL!=MRSAT,MERGEDLUI"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "LUI" && $_[6] ne "MRSAT.RRF" && $_[6] ne "CHANGE/MERGEDLUI.RRF") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=AV=MAX=8 where COL=SUI and FIL!=MRSAT
    #
    echo "    Verify MIN=AV=MAX=8 where COL=SUI and FIL!=MRSAT"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "SUI" && $_[6] ne "MRSAT.RRF") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN,AV,MAX != 8 where COL=SUI and FIL!=MRSAT"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "SUI" && $_[6] ne "MRSAT.RRF") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=AV=MAX=8 where COL=AUI and FIL!=MRLO
    #
    echo "    Verify MIN=AV=MAX=8 where COL=AUI and FIL!=MRLO"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "AUI" && $_[6] ne "MRLO.RRF") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN,AV,MAX != 8 where COL=AUI and FIL!=MRLO"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "AUI" && $_[6] ne "MRLO.RRF") { print unless $_[3] ==8 && $_[4] eq "8.00" && $_[5] == 8 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=AV=MAX=10 where COL=ATUI
    #
    echo "    Verify MIN=AV=MAX=10 where COL=ATUI"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "ATUI") { print unless $_[3] ==10 && $_[4] eq "10.00" && $_[5] == 10 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN,AV,MAX != 10 where COL=ATUI"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "ATUI") { print unless $_[3] ==10 && $_[4] eq "10.00" && $_[5] == 10 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=AV=MAX=9 where COL=RUI
    #
    echo "    Verify MIN=AV=MAX=9 where COL=RUI"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "RUI") { print unless $_[3] ==9 && $_[4] eq "9.00" && $_[5] == 9 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN,AV,MAX != 9 where COL=RUI"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "RUI") { print unless $_[3] ==9 && $_[4] eq "9.00" && $_[5] == 9 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MIN=0, MAX=1 where COL=XC
    #
    echo "    Verify MIN=0, MAX=1 where COL=XC"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "XC") { print unless $_[3] ==0 && $_[5] == 1 ;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "WARNING:  MIN != 0 or MAX != 1 where COL=XC"
	$PATH_TO_PERL -ne 'split/\|/; if($_[0] eq "XC") { print unless $_[3] == 0 && $_[5] == 1 ;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #   Verify MAX<=DTY where DTY specifies precision
    #
    echo "    Verify MAX<=DTY where DTY specifies precision"
    set cnt = `$PATH_TO_PERL -ne 'split/\|/; if($_[7] =~ /(?:char|varchar|integer|numeric)\((\d+)[\d,]?\)/) { print if $_[5] > $1;} ' $mrcols | wc -l `
    if ($cnt != 0) then
	echo "ERROR:  MAX>DTY where DTY specifies precision"
	$PATH_TO_PERL -ne 'split/\|/; if($_[7] =~ /(?:char|varchar|integer|numeric)\((\d+)[\d,]?\)/) { print if $_[5] > $1;} ' $mrcols | sed 's/^/  /'
    endif

    #
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrcols >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRCOLS has incorrect sort order"
    endif


else if ($target == "MRRANK") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrrank) then
	echo "ERROR: required file $mrrank cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^\d{4}\|[^\|]*\|[^\|]*\|[YN]\|/;' $mrrank >! MRRANK.badfields.$$
    set cnt = `cat MRRANK.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRRANK.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRRANK.badfields.$$

    #
    # Verify MRSAB.TTYL values in MRRANK.TTY
    #
    echo "    Verify MRSAB.TTYL values in MRRANK.TTY"
    $PATH_TO_PERL -ne 'split /\|/; foreach $x (split /,/,$_[17]) {print "$_[3]|$x\n";};' $mrsab | sort -u >! mrsab.rsab.tty.$$
    cut -d\| -f 2,3 $mrrank | sort -u >! mrrank.sab.tty.$$
    set ct=`diff mrsab.rsab.tty.$$ mrrank.sab.tty.$$ | wc -l`
    if ($ct > 0) then
        echo "ERROR: MRSAB.RSAB,TTYL does not match MRRANK.SAB,TTY"
	diff mrsab.rsab.tty.$$ mrrank.sab.tty.$$ | sed 's/^/  /'
    endif
    rm -f mrrank.sab.tty.$$ mrsab.rsab.tty.$$

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f2 $mrrank | sort -u >! MRRANK.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRRANK.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRRANK.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRRANK.SAB.$$

    #
    #   Verify TTY in MRDOC.VALUE where MRDOC.DOCKEY=TTY
    #
    echo "    Verify TTY in MRDOC.VALUE where MRDOC.DOCKEY=TTY"
    cut -d\| -f3 $mrrank | sort -u >! MRRANK.TTY.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="TTY"{print $2}' $mrdoc | sort -u | comm -13 - MRRANK.TTY.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  TTY not in MRDOC.VALUE where MRDOC.DOCKEY=TTY"
	awk -F\| '$3=="expanded_form"&&$1=="TTY"{print $2}' $mrdoc | sort -u | comm -13 - MRRANK.TTY.$$ | sed 's/^/  /'
    endif
    rm -f MRRANK.TTY.$$

    #
    #   Verify SAB|TTY unique
    #
    echo "    Verify SAB|TTY unique"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[1]|$_[2]\n";' $mrrank | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: SAB,TTY is not unique"
	$PATH_TO_PERL -ne 'split /\|/; print "$_[1]|$_[2]\n";' $mrrank |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify RNK is unique
    #
    echo "    Verify RNK is unique"
    cut -d\| -f 1 $mrrank | sort -u -o rnk.sortu.$$
    cut -d\| -f 1 $mrrank | sort -o rnk.sort.$$
    set cnt=(`diff rnk.sortu.$$ rnk.sort.$$ | wc -l`)
    if ($cnt[1] != 0) then
        echo "ERROR: RNK in MRRANK is not unique"
	diff rnk.sortu.$$ rnk.sort | sed 's/^/  /'
    endif
    rm -f rnk.sortu.$$ rnk.sort.$$

    #
    #   Verify sort order (its in reverse)
    #
    echo "    Verify sort order (its in reverse)"
    sort -c -u -r $mrrank >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRRANK has incorrect sort order"
    endif

else if ($target == "MRREL") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrrel) then
	echo "ERROR: required file $mrrel cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|(?:A\d{7,8})*\|[^\|]*\|[^\|]*\|C.\d{6}\|(?:A\d{7,8})*\|[^\|]*\|[^\|]*\|R\d{8}\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|[YN]*\|[YNEO]\|\d*\|/;' $mrrel >! MRREL.badfields.$$
    set cnt = `cat MRREL.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRREL.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.badfields.$$

    #
    #   Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    #
    echo "    Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    cut -d\| -f4 $mrrel | sort -u >! MRREL.REL.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.REL.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  REL not in MRDOC.VALUE where MRDOC.DOCKEY=REL"
	awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.REL.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.REL.$$

    #
    #   Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    #
    echo "    Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    cut -d\| -f8 $mrrel | sort -u >! MRREL.RELA.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.RELA.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  RELA not in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
	awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.RELA.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.RELA.$$

    #
    #   Verify STYPE1 in MRDOC.VALUE where MRDOC.DOCKEY=STYPE1
    #
    echo "    Verify STYPE1 in MRDOC.VALUE where MRDOC.DOCKEY=STYPE1"
    cut -d\| -f3 $mrrel | sort -u >! MRREL.STYPE1.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="STYPE1"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.STYPE1.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  STYPE1 in MRDOC.VALUE where MRDOC.DOCKEY=STYPE1"
	awk -F\| '$3=="expanded_form"&&$1=="STYPE1"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.STYPE1.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.STYPE1.$$

    #
    #   Verify STYPE2 in MRDOC.VALUE where MRDOC.DOCKEY=STYPE2
    #
    echo "    Verify STYPE2 in MRDOC.VALUE where MRDOC.DOCKEY=STYPE2"
    cut -d\| -f7 $mrrel | sort -u >! MRREL.STYPE2.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="STYPE2"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.STYPE2.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  STYPE2 in MRDOC.VALUE where MRDOC.DOCKEY=STYPE2"
	awk -F\| '$3=="expanded_form"&&$1=="STYPE2"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.STYPE2.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.STYPE2.$$

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f11 $mrrel | sort -u >! MRREL.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRREL.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRREL.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.SAB.$$

    #
    # Verify SL in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SL in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f12 $mrrel | sort -u >! MRREL.SL.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRREL.SL.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SL not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRREL.SL.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.SL.$$

    #
    #  Verify SL count = SAB|SL count
    #
    echo "    Verify SL count = SAB|SL count"
    set sab_cnt=`cut -d\| -f 11 $mrrel | sort -u | wc -l`
    set sab_sl_cnt=`cut -d\| -f 11,12 $mrrel | sort -u | wc -l`
    if ($sab_cnt != $sab_sl_cnt) then
        echo "ERROR: The unique SAB count does not match the unique SAB,SL count"
	echo "    SAB ($sab_cnt),  SAB|SL ($sab_sl_cnt)"
    endif

    #
    #  Verify SAB count = SL count
    #
    echo "    Verify SAB count = SL count"
    set sl_cnt=`cut -d\| -f 12 $mrrel | sort -u | wc -l`
    if ($sab_cnt != $sl_cnt) then
        echo "ERROR: The unique SAB count does not match the unique SL count"
	echo "    SAB ($sab_cnt),  SL ($sl_cnt)"
    endif

    #
    #  Verify CUI1 in MRCONSO.CUI
    #
    echo "    Verify CUI1 in MRCONSO.CUI"
    cut -d\| -f1 $mrconso | sort -u >! MRCONSO.uis.c.$$
    set ct=`join -v 1 -t\| -j1 1 -j2 1 $mrrel MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 in MRREL not in MRCONSO.CUI"
	join -v 1 -t\| -j1 1 -j2 1 -o 1.1 $mrrel MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI2 in MRCONSO.CUI
    #
    echo "    Verify CUI2 in MRCONSO.CUI"
    set ct=`cut -d\| -f5 $mrrel | sort -u | join -v 1 -t\| -j1 1 -j2 1 - MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI2 in MRREL not in MRCONSO.CUI"
	cut -d\| -f5 $mrrel | sort -u |\
	    join -v 1 -t\| -j1 1 -j2 1 - MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI1,AUI1 in MRCONSO.CUI,AUI
    #
    echo "    Verify CUI1,AUI1 in MRCONSO.CUI,AUI"
    awk -F\| '($2!=""){print $1"|"$2}' $mrrel | sort -u >! mrrel.tmp1.$$
    cut -d\| -f1,8 $mrconso | sort -u  >! MRCONSO.uis.ca.$$
    set ct=`comm -23 mrrel.tmp1.$$ MRCONSO.uis.ca.$$ | wc -l`
     if ($ct != 0) then
        echo "ERROR: CUI1,AUI1 not in MRCONSO.CUI,AUI"
	comm -23 mrrel.tmp1.$$ MRCONSO.uis.ca.$$  | sed 's/^/  /'
    endif

    #
    #  Verify CUI2,AUI2 in MRCONSO.CUI,AUI
    #
    echo "    Verify CUI2,AUI2 in MRCONSO.CUI,AUI"
    awk -F\| '($6!=""){print $5"|"$6}' $mrrel | sort -u >! mrrel.tmp1.$$
    set ct=`comm -23 mrrel.tmp1.$$ MRCONSO.uis.ca.$$ | wc -l`
     if ($ct != 0) then
        echo "ERROR: CUI2,AUI2 not in MRCONSO.CUI,AUI"
	comm -23 mrrel.tmp1.$$ MRCONSO.uis.ca.$$  | sed 's/^/  /'
    endif
    rm -f mrrel.tmp1.$$ MRCONSO.uis.{c,ca}.$$

    #
    #  Verify REL=RB count = REL=RN count
    #
    echo "    Verify REL=RB count = REL=RN count"
    set rb_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "x\n" if $_[3] eq "RB";' $mrrel | wc -l`
    set rn_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "x\n" if $_[3] eq "RN";' $mrrel | wc -l`
    if ($rb_cnt != $rn_cnt) then
        echo "ERROR: RB count does not match RN count"
	echo "      RB ($rb_cnt),  RN ($rn_cnt)"
    endif

    #
    #  Verify REL=PAR count = REL=CHD count
    #
    echo "    Verify REL=PAR count = REL=CHD count"
    set par_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "x\n" if $_[3] eq "PAR";' $mrrel | wc -l`
    set chd_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "x\n" if $_[3] eq "CHD";' $mrrel | wc -l`
    if ($par_cnt != $chd_cnt) then
        echo "ERROR: PAR count does not match CHD count"
	echo "      PAR ($par_cnt),  CHD ($chd_cnt)"
    endif

    #
    #  Verify CUI1|AUI1|CUI2|AUI2 list is the same as CUI2|AUI2|CUI1|AUI1
    #
    echo "    Verify CUI1|AUI1|CUI2|AUI2 list is the same as CUI2|AUI2|CUI1|AUI1"
    cut -d\| -f 1,2,5,6 $mrrel | sort -u >! MRREL.cui12.$$
    awk -F\| '{print $5"|"$6"|"$1"|"$2}' $mrrel | sort -u >! MRREL.cui21.$$
    set ct=`diff MRREL.cui12.$$ MRREL.cui21.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1|AUI1|CUI2|AUI2 does not match CUI2|AUI2|CUI1|AUI1"
	diff MRREL.cui12.$$ MRREL.cui21.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.cui12.$$ MRREL.cui21.$$

if (($mode != "submission" || $mode != "subset") && -e $mrcxt) then
    #
    #  Validate against MRCXT
    #
    set empty = `wc -l $mrcxt`
    if ($empty[1] != 0) then
	echo "    Validate against MRCXT (check later)"
	grep -v '|CHD|' $mrcxt | \
	(sort  -t \| -k1,1 -k3,3 -k4,4 -k6,6n -k7,7 -k8,8n;echo "") | \
	$PATH_TO_PERL -ne '($cui,$sui,$aui,$sab,$scd,$cxn,$cxl,$rnk,$str,$cui2,$aui2,$hcd,$rela,$xc,$cvf) = split /\|/; \
	    $key = "$cui|$aui|$sab|$cxn"; \
        if ($cxl eq "SIB") { \
           print "$cui|$aui|$cxl|$cui2|$aui2|$sab\n" ; }\
        elsif ($key eq $prev_key && $prev_key) { \
           print "$prev_line|CHD|$cui2|$aui2|$sab\n" ; \
           print "$cui2|$aui2|PAR|$prev_line|$sab\n" ; }\
        $prev_line="$cui2|$aui2"; \
        $prev_key = $key; ' | sort  -u >! MRCXT.tmp.$$
	egrep '\|(PAR|CHD|SIB)\|' $mrrel | cut -d\| -f1-2,4-6,11 | sort -u >! MRREL.tmp.$$


	set ct=`comm -23 MRCXT.tmp.$$ MRREL.tmp.$$ | wc -l`
	if ($ct != 0) then
	    echo "ERROR: MRREL do not match with MRCXT"
	    diff MRCXT.tmp.$$ MRREL.tmp.$$ | sed 's/^/  /'
	endif
	rm -f MRCXT.tmp.$$ MRREL.tmp.$$
    endif
endif

    #
    #   Verify RUI unique
    #
    echo "    Verify RUI unique"
    set ct=`cut -d\| -f9 $mrrel | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: RUI is not unique"
	cut -d\| -f9 $mrrel |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrrel >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRREL has incorrect sort order"
    endif

else if ($target == "MRSAB") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif

    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|C.\d{6}\|[^\|]+\|[^\|]+\|[^\|]+\|[^\|]+\|[^\|]*\|(?:\d{4}_\d{2}_\d{2})*\|(?:\d{4}_\d{2}_\d{2})*\|(?:\d{4}..[^\|]*)*\|(?:\d{4}..[^\|]*)*\|[^\|]*\|[^\|]*\|[0-3]\|\d*\|\d*\|(?:FULL(?:-(?:MULTIPLE|NOSIB)*)?)?\|(?:,{0,1}[A-Z]{2,})*\|(?:,{0,1}[a-zA-Z0-9]+)*|[^\|]*\|[^\|]*\|[YN]\|[YN]\|[^\|]+\|[^\|]*\|/;' $mrsab >! MRSAB.badfields.$$
    set cnt = `cat MRSAB.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRSAB.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRSAB.badfields.$$

    #
    # Gather counts
    set rcnt=`cat $mrsab | wc -l`
    set vcui_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n" if $_[0] ne "";' $mrsab | sort -u | wc -l`;
    set rcui_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[1]\n";' $mrsab | sort -u | wc -l`;
    set vsab_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]\n";' $mrsab | sort -u | wc -l`;
    set rsab_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[3]\n";' $mrsab | sort -u | wc -l`;
    set sf_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[5]\n";' $mrsab | sort -u | wc -l`;


    #
    # Verify SF in MRSAB.RSAB (the SF must also be a SAB)
    #
    echo "    Verify SF in MRSAB.RSAB (the SF must also be a SAB)"
    cut -d\| -f6 $mrsab | sort -u >! mrsab.tmp1.$$
    set ct=`cut -d\| -f 4 $mrsab | sort -u | comm -13 - mrsab.tmp1.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR:  SF not in MRSAB.RSAB"
	cut -d\| -f 4 $mrsab | sort -u | comm -13 - mrsab.tmp1.$$ | sed 's/^/  /'
    endif
    rm -f mrsab.tmp1.$$

    #
    # Verify RCUI count equals RSAB count
    #
    echo "    Verify RCUI count = RSAB count Unique"
    if ($rcui_cnt != $rsab_cnt) then
        echo "ERROR: VCUI count ($vcui_cnt) != VSAB count ($vsab_cnt)"
    endif

    #
    # Verify RSAB|SF is unique
    #
    echo "    Verify RSAB,SF Unique"
    set ct=`awk -F\| '$22=="Y"{print $4"|"$6}' $mrsab | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: RSAB,SF is not unique"
	awk -F\| '$22=="Y"{print $4"|"$6}' $mrsab | sort | uniq -d
    endif

    #
    # Verify RMETA is null or RMETA<IMETA
    #
    echo "    Verify RMETA is null or RMETA less than IMETA"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print "IMETA:$_[9], RMETA: $_[10]\n" unless $_[10] eq "" || ($_[10] ge $_[9] && $_[9] ne "")' $mrsab | wc -l `
    if ($ct != 0) then
        echo "ERROR: RMETA must be > IMETA"
	$PATH_TO_PERL -ne 'split /\|/; print "IMETA:$_[9], RMETA: $_[10]\n" unless $_[10] eq "" || ($_[10] ge $_[9] && $_[9] ne "")' $mrsab | sed 's/^/  /'
    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrsab >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRSAB has incorrect sort order"
    endif


else if ($target == "MRSAT") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrsat) then
	echo "ERROR: required file $mrsat cannot be found"
	exit 1
    endif
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif
    if (! -e $mrsab) then
	echo "ERROR: required file $mrsab cannot be found"
	exit 1
    endif
    if (! -e $mrrel) then
	echo "ERROR: required file $mrrel cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
if ($mode != "submission") then
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|(L\d{7})*\|(S\d{7})?\|([AR]\d{7,8})?\|[^\|]*\|[^\|]*\|AT\d{8}\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|[YNEO]\|\d*\|/;' $mrsat >! MRSAT.badfields.$$
    set cnt = `cat MRSAT.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRSAT.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRSAT.badfields.$$
else
    checkfields.pl $mrsat
endif

    #
    # Verify ATNL in MRSAB.ATNL
    #
    if ($mode != "subset") then
	echo "    Verify ATNL values in MRSAB.ATN"
	$PATH_TO_PERL -ne 'split /\|/; foreach $x (split /,/,$_[18]) {print "$_[3]|$x\n";};' $mrsab | sort -u >! mrsab.rsab.atn.$$
	$PATH_TO_PERL -ne 'split /\|/; print "$_[9]|$_[8]\n";' $mrsat | sort -u >! mrsat.sab.atn.$$
	set ct=`diff mrsab.rsab.atn.$$ mrsat.sab.atn.$$ | wc -l`
	if ($ct > 0) then
	    echo "ERROR: MRSAT.RSAB,ATNL does not match MRSAB.SAB,ATNL"
	    diff mrsab.rsab.atn.$$ mrsat.sab.atn.$$ | sed 's/^/  /'
	endif
	rm -f mrsat.sab.atn.$$ mrsab.rsab.atn.$$
     endif

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f10 $mrsat | sort -u >! MRSAT.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRSAT.SAB.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
	awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRSAT.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRSAT.SAB.$$

    #
    #   Verify STYPE in MRDOC.VALUE where MRDOC.DOCKEY=STYPE"
    #
    echo "    Verify STYPE in MRDOC.VALUE where MRDOC.DOCKEY=STYPE"
    cut -d\| -f5 $mrsat | sort -u >! MRSAT.STYPE.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="STYPE"{print $2}' $mrdoc | sort -u | comm -13 - MRSAT.STYPE.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  STYPE not in MRDOC.VALUE where MRDOC.DOCKEY=STYPE"
	awk -F\| '$3=="expanded_form"&&$1=="STYPE"{print $2}' $mrdoc | sort -u | comm -13 - MRSAT.STYPE.$$ | sed 's/^/  /'
    endif
    rm -f MRSAT.STYPE.$$

    #
    #   Verify ATN in MRDOC.VALUE where MRDOC.DOCKEY=ATN"
    #
    echo "    Verify ATN in MRDOC.VALUE where MRDOC.DOCKEY=ATN"
    cut -d\| -f9 $mrsat | sort -u >! MRSAT.ATN.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="ATN"{print $2}' $mrdoc | sort -u | comm -13 - MRSAT.ATN.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  ATN not in MRDOC.VALUE where MRDOC.DOCKEY=ATN"
	awk -F\| '$3=="expanded_form"&&$1=="ATN"{print $2}' $mrdoc | sort -u | comm -13 - MRSAT.ATN.$$ | sed 's/^/  /'
    endif
    rm -f MRSAT.ATN.$$

    #
    #   Verify ATUI unique
    #
    echo "    Verify ATUI unique (for non-LT attributes)"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[6]\n" if $_[8] ne "LT"' $mrsat | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: ATUI is not unique"
	$PATH_TO_PERL -ne 'split /\|/; print "$_[6]\n" if $_[8] ne "LT"' $mrsat |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #  Verify cls_cnt = cs_cnt
    #
    echo "    Verify cls_cnt = cs_cnt"
    set cls_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]$_[1]$_[2]\n" if $_[2]' $mrsat | sort -u | wc -l`
    set cs_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]$_[2]\n" if $_[2]' $mrsat | sort -u | wc -l`
    if ($cls_cnt != $cs_cnt) then
        echo "ERROR: The CUI|LUI|SUI count does not match the CUI|SUI count"
	echo "        cls_cnt ($cls_cnt)     cs_cnt ($cs_cnt)"
    endif

    #
    #  Verify CUI|LUI|SUI in MRCONSO.CUI|LUI|SUI where sui!='' and UI =~ /A*/
    #
    echo "    Verify CUI|LUI|SUI in MRCON.CUI|LUI|SUI where sui!=''"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]|$_[2]\n" if $_[2] && $_[3] =~ /A*/ ' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    cut -d\| -f 1,4,6 $mrconso | sort -u  >! MRCONSO.uis.cls.$$
    set ct=`comm -23 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUI|LUI|SUIs in MRSAT not in MRCONSO"
	comm -23 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$

    #
    #  Verify CUI in MRCONSO.CUI where SUI=''
    #
    echo "    Verify CUI in MRCONSO.CUI where SUI=''"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n" unless $_[2]' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    set ct=`join -t\| -j 1 -v 1 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUIs in MRSAT not in MRCONSO"
	join -t\| -j 1 -v 1 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$

    #
    #  Verify MRCONSO.CUI in MRSAT.CUI
    #
    echo "    Verify MRCONSO.CUI in MRSAT.CUI"
    cut -d\| -f 1 $mrsat | sort -u >! mrsat.tmp1.$$
    set ct=`join -t\| -j 1 -v 2 -o 2.1 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUIs in MRCONSO not in MRSAT"
	join -t\| -j 1 -v 2 -o 2.1 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ |\
	    sort -u | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$ MRCONSO.uis.cls.$$

if ($mode != "submission") then
    #
    #  Verify each CUI has DA,MR,ST rows
    #
    echo "    Verify each CUI has DA,MR,ST rows"
    (cat $mrsat; echo "") |\
     $PATH_TO_PERL -ne 'split /\|/; \
        $key = $_[0]; \
	if ($key ne $prev_key && $prev_key) { \
	    print "ERROR: DA attribute missing for $prev_key\n" unless $da; \
	    print "ERROR: MR attribute missing for $prev_key\n" unless $mr; \
	    print "ERROR: ST attribute missing for $prev_key\n" unless $st; \
	} \
	$da=1 if $_[8] eq "DA"; $mr=1 if $_[8] eq "MR"; $st=1 if $_[8] eq "ST"; \
	$prev_key = $key; ' >! MRSAT.CUI.errors.$$
    set ct=`cat MRSAT.CUI.errors.$$ | wc -l`
    if ($ct != 0) then
        cat MRSAT.CUI.errors.$$
    endif
    rm -f MRSAT.CUI.errors.$$
endif

    #
    #  Verify AM flag matches ambig strings from MRCONSO
    #
    # DO NOT CHECK "AM" FLAGS ANYMORE
    ##
    if (1 == 0 && $mode != "subset") then
	echo "    Verify AM flag matches ambig strings from MRCONSO"
	# Uppercase strings and only look at ENG
	$PATH_TO_PERL -ne 'split /\|/; print uc("$_[14]|$_[0]|$_[5]\n") if $_[1] eq "ENG"' $mrconso |\
	    sort -t\| -k 1,1 >! MRCONSO.str.$$
	    join -t\| -j1 1 -j2 1 -o 1.1 1.2 1.3 2.2 2.3 MRCONSO.str.$$ MRCONSO.str.$$ |\
	awk -F\| '$2!=$4 {print $2"|"$3 }' | sort -u >! mrsat.tmp.$$
	# extract AM rows from MRSAT
	$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[2]\n" if $_[8] eq "AM" && $_[9] eq "MTH"' \
	    $mrsat | sort -u >! mrsat.am.$$
	# count and compare
	set ct=`diff mrsat.tmp.$$ mrsat.am.$$ | wc -l`
	if ($ct != 0) then
	    echo "ERROR: AM flags do not match ambiguious SUIs in MRCONSO"
	    diff mrsat.tmp.$$ mrsat.am.$$ | sed 's/^/  /'
	endif
	rm -f mrsat.{am,tmp}.$$
	rm -f MRCONSO.str.$$
    endif

    #
    #  Verify MED<year> exists for all years 1960 through present
    #
    if ($mode != "subset") then
	echo "    Verify MED<year> exists for all years 1960 through present"
	(cat $mrsat; echo "END") | \
	$PATH_TO_PERL -ne 'chop; split /\|/; \
	if ($_[0] eq "END") { \
	    ($d,$d,$d,$d,$d,$y) = localtime; $|=1; \
	    foreach $year (1960 .. ($y + 1900)) { \
		print "ERROR: Missing MED$year attributes\n" unless $done{$year} } \
		} \
		if ($_[8] =~ /^MED(\d\d\d\d)$/ && $_[9] eq "NLM-MED") { \
		    $done{$1} = 1; } ' >! MRSAT.MED.errors.$$
		    set ct=`cat MRSAT.MED.errors.$$ | wc -l`
	 if ($ct != 0) then
	    cat MRSAT.MED.errors.$$
	 endif
	 rm -f MRSAT.MED.errors.$$
    endif

    #
    #   Verify ST attributes are R,U
    #
    echo "    Verify ST attributes are R,U"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[8] eq "ST" && $_[9] eq "MTH" && $_[10] ne "R" && $_[10] ne "U";' $mrsat | wc -l`
    if ($ct != 0) then
        echo "ERROR: Invalid ST values"
        $PATH_TO_PERL -ne 'split /\|/; print if $_[8] eq "ST" && $_[9] eq "MTH" \
	    && $_[10] ne "R" && $_[10] ne "U";' $mrsat | sed 's/^/  /'
    endif

    #
    #  Verify CUI|METAUI in MRCONSO.CUI|AUI where METAUI =~ /^A/
    #
    echo "    Verify CUI|METAUI in MRCONSO.CUI|AUI where METAUI =~ /^A/"
    cut -d\| -f1,8 $mrconso | sort -u  >! MRCONSO.uis.ca.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]\n" if $_[3] =~ /^A/' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    set ct=`comm -23  mrsat.tmp1.$$ MRCONSO.uis.ca.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUI|AUI in MRSAT not in MRCONSO.CUI|AUI"
	comm -23 mrsat.tmp1.$$ MRCONSO.uis.ca.$$ | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$ MRCONSO.uis.ca.$$

    #
    #  Verify CUI|METAUI in MRREL.CUI1|RUI where METAUI =~ /^R/
    #
    echo "    Verify CUI|METAUI in MRREL.CUI1|RUI where METAUI =~ /^R/"
    cut -d\| -f1,9 $mrrel | sort -u  >! MRREL.uis.cr.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]\n" if $_[3] =~ /^R/' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    set ct=`comm -23  mrsat.tmp1.$$ MRREL.uis.cr.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUI|AUI in MRSAT not in MRREL.CUI1|RUI"
	comm -23 mrsat.tmp1.$$ MRREL.uis.cr.$$ | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$ MRREL.uis.cr.$$

    #
    #   Verify long attribute expansion
    #
    echo "    Verify long attribute expansion"
    set ct=`grep -c '<>Long_Attribute<>' $mrsat`
    if ($ct != 0) then
        echo "ERROR: MRDEF has unexpanded long attributes"
	grep '<>Long_Attribute<>' $mrdef | sed 's/^/  /'
    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrsat >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRSAT has incorrect sort order"
    endif

else if ($target == "MRSTY") then

    #
    # Handle environment
    #
    echo "    Verify required files"

    if (! -e $mrsty) then
	echo "ERROR: required file $mrsty cannot be found"
	exit 1
    endif
    if (! -e $mrconso) then
	echo "ERROR: required file $mrconso cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^C.\d{6}\|T\d{3}\|[A-Z][\d\.]*\|[A-Za-z]+[^\|]*\|AT\d{8}\|\d*\|/;' $mrsty >! MRSTY.badfields.$$
    set cnt = `cat MRSTY.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRSTY.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRSTY.badfields.$$

    #
    #   Verify CUI|ATUI unique
    #
    echo "    Verify CUI|ATUI unique"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[4]\n";' $mrsty | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI,ATUI is not unique"
	$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[4]\n";' $mrsty |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify ATUI unique
    #
    echo "    Verify ATUI unique"
    set ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[4]\n";' $mrsty | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: ATUI is not unique"
	$PATH_TO_PERL -ne 'split /\|/; print "$_[4]\n";' $mrsty |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify STY count = TUI|STY count
    #
    echo "    Verify STY count = TUI|STY count"
    set sty_cnt=`cut -d\| -f 4 $mrsty | sort -u | wc -l`
    set tui_sty_cnt=`cut -d\| -f 2,4 $mrsty | sort -u | wc -l`
    if ($sty_cnt != $tui_sty_cnt) then
        echo "ERROR: the STY count does not match the TUI,STY count"
    endif

    #
    #   Verify STY count = TUI count
    #
    echo "    Verify STY count = TUI count"
    set tui_cnt=`cut -d\| -f 2 $mrsty | sort -u | wc -l`
    if ($sty_cnt != $tui_cnt) then
        echo "ERROR: the STY count does not match the TUI count"
    endif

    #
    #   Verify STY count = STN count
    #
    echo "    Verify STY count = STN count"
    set stn_cnt=`cut -d\| -f 3 $mrsty | sort -u | wc -l`
    if ($sty_cnt != $stn_cnt) then
        echo "ERROR: the STY count does not match the STN count"
    endif

    #
    #   Verify STY count = TUI|STN count
    #
    echo "    Verify STY count = TUI|STN count"
    set tui_stn_cnt=`cut -d\| -f 2,3 $mrsty | sort -u | wc -l`
    if ($sty_cnt != $tui_stn_cnt) then
        echo "ERROR: the STY count does not match the TUI,STN count"
    endif

    #
    #   Verify CUI in MRCONSO.CUI
    #
    echo "    Verify CUI in MRCONSO.CUI"
    cut -d\| -f1 $mrsty | sort -u >! MRSTY.uis.c.$$
    cut -d\| -f1 $mrconso | sort -u >! MRCONSO.uis.c.$$
    set ct=(`comm -23 MRSTY.uis.c.$$ MRCONSO.uis.c.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUIs in MRSTY not in MRCONSO"
	comm -23 MRSTY.uis.c.$$ MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #   Verify MRCONSO.CUI in CUI
    #
    echo "    Verify MRCONSO.CUI in CUI"
    set ct=(`comm -13 MRSTY.uis.c.$$ MRCONSO.uis.c.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUIs in MRCONSO not in MRSTY"
	comm -13 MRSTY.uis.c.$$ MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif
    rm -f MRSTY.uis.c.$$ MRCONSO.uis.c.$$

    #
    #   Verify ATUI unique
    #
    echo "    Verify ATUI unique"
    set ct=`cut -d\| -f5 $mrsty | sort | uniq -d | wc -l`
    if ($ct != 0) then
        echo "ERROR: ATUI is not unique"
	cut -d\| -f5 $mrsty |\
	    sort | uniq -d | sed 's/^/  /'
    endif

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrsty >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRSTY has incorrect sort order"
    endif

else if ($target == "MRDOC") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif

    #
    #   Verify field formats
    #
    echo "    Verify field formats"
    $PATH_TO_PERL -ne 'print unless /^(?:ATN|COA|COT|LAT|REL|RELA|STT|SUPPRESS|TS|TTY|MAPATN|CXTY|.*TYPE.*|CVF.*)\|[^\|]*\|(?:expanded_form|tty_class|snomedct_rela_mapping|snomedct_rel_mapping|rela_inverse|rel_inverse|content_view)\|[^\|]+\|/;' $mrdoc | fgrep -v "rela_inverse" | fgrep -v "rel_inverse" >! MRDOC.badfields.$$
    set cnt = `cat MRDOC.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRDOC.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRDOC.badfields.$$

    #
    #   Verify TTYs (tty_class in expanded_form)
    #
    echo "    Verify TTYs (tty_class in expanded_form)"
    awk -F\| '$3=="tty_class"{print $2}' $mrdoc | sort -u >! MRDOC.tty_class.$$
    set ct=(`awk -F\| '$3=="expanded_form"{print $2}' $mrdoc | sort -u | comm -13 - MRDOC.tty_class.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: tty_class not in expanded_form"
	awk -F\| '$3=="tty_class"{print $2}' $mrdoc | comm -13 - MRDOC.tty_class.$$ | sed 's/^/  /'
    endif
    rm -f MRDOC.tty_class.$$

    #
    #   Verify RELs (rel_inverse same as expanded_form)
    #
    echo "    Verify RELs (rel_inverse same as expanded_form)"
    awk -F\| '$1=="REL"&&$3=="rel_inverse"{print $2}' $mrdoc | sort -u  >! MRDOC.rel_inverse.$$
    awk -F\| '$1=="REL"&&$3=="expanded_form"&&$2!=""{print $2}' $mrdoc | sort -u |egrep -v '(DEL|SUBX)' >! MRDOC.expanded_form.$$
    set ct=(`diff MRDOC.rel_inverse.$$ MRDOC.expanded_form.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: rel not in expanded_form"
	diff MRDOC.rel_inverse.$$ MRDOC.expanded_form.$$
    endif
    rm -f MRDOC.rel_inverse.$$ MRDOC.expanded_form.$$

    #
    #   Verify RELAs (rela_inverse same as expanded_form)
    #
    echo "    Verify RELAs (rela_inverse same as expanded_form)"
    awk -F\| '$1=="RELA"&&$3=="rela_inverse"{print $2}' $mrdoc | sort -u >! MRDOC.rela_inverse.$$
    awk -F\| '$1=="RELA"&&$3=="expanded_form"{print $2}' $mrdoc | sort -u >! MRDOC.expanded_form.$$
    set ct=(`diff MRDOC.rela_inverse.$$ MRDOC.expanded_form.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: rela not in expanded_form"
	diff MRDOC.rela_inverse.$$ MRDOC.expanded_form.$$
    endif
    rm -f MRDOC.rela_inverse.$$ MRDOC.expanded_form.$$

else if ($target == "MRX") then

    #
    # Handle environment
    #
    echo "    Verify required files"
    if (! -e $mrxns) then
	echo "ERROR: required file $mrxns cannot be found"
	exit 1
    endif

    if (! -e $mrxnw) then
	echo "ERROR: required file $mrxnw cannot be found"
	exit 1
    endif

    if (! -e $mrdoc) then
	echo "ERROR: required file $mrdoc cannot be found"
	exit 1
    endif

    #
    # Make mrx.lats
    #
    ls $mrxw* | sed 's/MRXW_//' | sed 's/\.RRF//' | sed 's/.*\///' | sort -u -o mrx.lats.$$

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $mrxns"
    $PATH_TO_PERL -ne 'print unless /^[^\|]*\|[^\|]+\|C.\d{6}\|L\d{7}\|S\d{7}\|/;' $mrxns >! MRX.badfields.$$
    set cnt = `cat MRX.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRX.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRX.badfields.$$

    #
    #   Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT
    #
    echo "    Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
    awk -F\| '$3=="expanded_form"&&$1=="LAT"{print $2}' $mrdoc | sort -u >! MRDOC.LAT.$$
    cut -d\| -f1 $mrxns | sort -u >! MRXNS.LAT.$$
    set cnt = `comm -13 MRDOC.LAT.$$ MRXNS.LAT.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  LAT not in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
	comm -13 MRDOC.LAT.$$ MRXNS.LAT.$$ | sed 's/^/  /'
    endif
    rm -f MRXNS.LAT.$$

    #
    #   Verify field formats
    #
    echo "    Verify field formats: $mrxnw"
    $PATH_TO_PERL -ne 'print unless /^[^\|]*\|[^\|]+\|C.\d{6}\|L\d{7}\|S\d{7}\|/;' $mrxnw >! MRX.badfields.$$
    set cnt = `cat MRX.badfields.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR: The following rows have bad field formats"
	cat MRX.badfields.$$ | sed 's/^/  /'
    endif
    rm -f MRX.badfields.$$

    #
    #   Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT
    #
    echo "    Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
    cut -d\| -f1 $mrxnw | sort -u >! MRXNW.LAT.$$
    set cnt = `comm -13 MRDOC.LAT.$$ MRXNW.LAT.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  LAT not in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
	comm -13 MRDOC.LAT.$$ MRXNW.LAT.$$ | sed 's/^/  /'
    endif
    rm -f MRXNW.LAT.$$

    #
    #   Verify field formats
    #
    foreach f (`cat mrx.lats.$$`)
	echo "    Verify field formats: ${mrxw}_$f"
	$PATH_TO_PERL -ne 'print unless /^[^\|]*\|[^\|]+\|C.\d{6}\|L\d{7}\|S\d{7}\|/;' ${mrxw}_$f.RRF >! MRX.badfields.$$
	set cnt = `cat MRX.badfields.$$ | wc -l`
	if ($cnt != 0) then
	    echo "ERROR: The following rows have bad field formats"
	    cat MRX.badfields.$$ | sed 's/^/  /'
	endif
	rm -f MRX.badfields.$$

	echo "    Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
	cut -d\| -f1 ${mrxw}_$f.RRF | sort -u >! MRXW.$f.LAT.$$
	set cnt = `comm -13 MRDOC.LAT.$$ MRXW.$f.LAT.$$ | wc -l`
	if ($cnt != 0) then
	    echo "ERROR:  LAT not in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
	    comm -13 MRDOC.LAT.$$ MRXW.$f.LAT.$$ | sed 's/^/  /'
	endif
	rm -f MRXW.$f.LAT.$$
    end
    rm -f MRDOC.LAT.$$

    #
    #  Verify MRXNS count equals MRXNW count
    #
    echo "    Verify MRXNS count equals MRXNW count"
    set ns_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxns | sort -u | wc -l`
    set nw_cnt=`$PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxnw | sort -u | wc -l`
    if ($ns_cnt != $nw_cnt) then
        echo "ERROR: the MRXNS_ENG and MRXNW_ENG line counts do not match"
	echo "       MRXNS_ENG ($ns_cnt)    MRXNW_ENG ($nw_cnt)"
    endif

    #
    #  Verify CUI|LUI|SUI matches (both directions) with matching language
    #
    echo "    Verify MRCONSO CUI|LUI|SUI in MRXNS_ENG CUI|LUI|SUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]|$_[5]\n" if $_[1] eq "ENG";' $mrconso | sort -u >! mrx.tmp2.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxns | sort -u >! mrx.tmp1.$$
    set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999 | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXNS_ENG"
	comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999 | sed 's/^/  /'
    endif
    echo "    Verify MRXNS_ENG CUI|LUI|SUI in MRCONSO CUI|LUI|SUI"
    set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXNS_ENG"
	comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999
    endif

    echo "    Verify MRCONSO CUI|LUI|SUI in MRXNW_ENG CUI|LUI|SUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxnw | sort -u >! mrx.tmp1.$$
    set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999 | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXNW_ENG"
	comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999 | sed 's/^/  /'
    endif
    echo "    Verify MRXNW_ENG CUI|LUI|SUI in MRCONSO CUI|LUI|SUI"
    set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXNW_ENG"
	comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999 | sed 's/^/  /'
    endif
    rm -f mrx.tmp[12].$$

    foreach f (`cat mrx.lats.$$`)
        echo "    Verify MRCONSO CUI|LUI|SUI in MRXW_$f CUI|LUI|SUI"
	setenv LAT=$f
	$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]|$_[5]\n" if $_[1] eq "'$f'" \
	    && $_[14] !~ /^(=|<=|>=|\+|\+\+|\+\+\+|\+\+\+\+|<|>)$/;' $mrconso |\
	    sort -u >! mrx.tmp2.$$
	$PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' ${mrxw}_$f.RRF | sort -u >! mrx.tmp1.$$

	set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999 | wc -l`
	if ($ct != 0) then
	    echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXW_$f"
	    comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999 | sed 's/^/  /'
	endif
	echo "    Verify MRXW_$f CUI|LUI|SUI in MRCONSO CUI|LUI|SUI"
	set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
        if ($ct != 0) then
	    echo "ERROR: CUI|LUI|SUI in MRXW_$f not in MRCONSO"
	    comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v L0028429 | grep -v L0000999 | sed 's/^/  /'
	endif
	rm -f mrx.tmp[12].$$
    end

    #
    #   Verify sort order (for all files)
    #
    echo "    Verify sort order for MRXNS_ENG"
    sort -c -u $mrxns >> /dev/null
    if ($status != 0) then
	echo "ERROR: $mrxns has incorrect sort order"
    endif

    echo "    Verify sort order for MRXNW_ENG"
    sort -c -u $mrxnw >> /dev/null
    if ($status != 0) then
	echo "ERROR: $mrxnw has incorrect sort order"
    endif

    foreach f (`cat mrx.lats.$$`)
	echo "    Verify sort order for ${mrxw}_$f"
	sort -c -u ${mrxw}_$f.RRF >> /dev/null
	if ($status != 0) then
	    echo "ERROR: ${mrxw}_$f has incorrect sort order"
	endif
    end
    rm -f mrx.lats.$$

else if ($target == "MetaMorphoSys") then

    #
    # Compare AMBIG files
    #
    echo "    Verify AMBIG files match"
    set ct=`diff $dir/AMBIGSUI.RRF $dir/../METASUBSET/AMBIGSUI.RRF | wc -l`
    if ($ct != 0) then
        echo "ERROR: AMBIGSUI.RRF does not match"
	diff $dir/AMBIGSUI.RRF $dir/../METASUBSET/AMBIGSUI.RRF | sed 's/^/  /'
    endif
    set ct=`diff $dir/AMBIGLUI.RRF $dir/../METASUBSET/AMBIGLUI.RRF | wc -l`
    if ($ct != 0) then
        echo "ERROR: AMBIGLUI.RRF does not match"
	diff $dir/AMBIGLUI.RRF $dir/../METASUBSET/AMBIGLUI.RRF | sed 's/^/  /'
    endif

    #
    # Compare AMBIG files
    #
    echo "    Verify CHANGE files match"
    set ct=`diff $dir/CHANGE/DELETEDCUI.RRF $dir/../METASUBSET/CHANGE/DELETEDCUI.RRF | wc -l `
    if ($ct != 0) then
        echo "ERROR: CHANGE/DELETEDCUI.RRF does not match"
	diff $dir/CHANGE/DELETEDCUI.RRF $dir/../METASUBSET/CHANGE/DELETEDCUI.RRF | sed 's/^/  /'
    endif
    set ct=`diff $dir/CHANGE/DELETEDLUI.RRF $dir/../METASUBSET/CHANGE/DELETEDLUI.RRF | wc -l `
    if ($ct != 0) then
        echo "ERROR: CHANGE/DELETEDLUI.RRF does not match"
	diff $dir/CHANGE/DELETEDLUI.RRF $dir/../METASUBSET/CHANGE/DELETEDLUI.RRF | sed 's/^/  /'
    endif
    set ct=`diff $dir/CHANGE/DELETEDSUI.RRF $dir/../METASUBSET/CHANGE/DELETEDSUI.RRF | wc -l `
    if ($ct != 0) then
        echo "ERROR: CHANGE/DELETEDSUI.RRF does not match"
	diff $dir/CHANGE/DELETEDSUI.RRF $dir/../METASUBSET/CHANGE/DELETEDSUI.RRF | sed 's/^/  /'
    endif
    set ct=`diff $dir/CHANGE/MERGEDCUI.RRF $dir/../METASUBSET/CHANGE/MERGEDCUI.RRF | wc -l `
    if ($ct != 0) then
        echo "ERROR: CHANGE/MERGEDCUI.RRF does not match"
	diff $dir/CHANGE/MERGEDCUI.RRF $dir/../METASUBSET/CHANGE/MERGEDCUI.RRF | sed 's/^/  /'
    endif
    set ct=`diff $dir/CHANGE/MERGEDLUI.RRF $dir/../METASUBSET/CHANGE/MERGEDLUI.RRF | wc -l `
    if ($ct != 0) then
        echo "ERROR: CHANGE/MERGEDLUI.RRF does not match"
	diff $dir/CHANGE/MERGEDLUI.RRF $dir/../METASUBSET/CHANGE/MERGEDLUI.RRF | sed 's/^/  /'
    endif

    #
    # Compare MRCONSO
    #
    echo "    Verify MRCONSO matches"
    set ct1=(`wc -l $dir/MRCONSO.RRF`)
    set ct2=(`wc -l $dir/../METASUBSET/MRCONSO.RRF`)
    if ($ct1[1] != $ct2[1]) then
        echo "ERROR: MRCONSO.RRF line count does not match ($ct1, $ct2)"
    endif

    # qa_checks on MRCONSO
    echo "      qa_checks on MRCONSO"
    $MRD_HOME/bin/qa_checks.csh $dir/../METASUBSET MRD MRCONSO full $prev_released_dir

    #
    # Compare MRCOLS
    #
    echo "    Verify MRCOLS matches"
    set ct=`diff $dir/MRCOLS.RRF $dir/../METASUBSET/MRCOLS.RRF | wc -l`
    if ($ct != 0) then
        echo "WARNING: MRCOLS.RRF does not match"
	diff $dir/MRCOLS.RRF $dir/../METASUBSET/MRCOLS.RRF | sed 's/^/  /'
    endif

    #
    # Compare MRFILES
    #
    echo "    Verify MRFILES matches"
    set ct=`diff $dir/MRFILES.RRF $dir/../METASUBSET/MRFILES.RRF | wc -l`
    if ($ct != 0) then
        echo "WARNING: MRFILES.RRF does not match"
	diff $dir/MRFILES.RRF $dir/../METASUBSET/MRFILES.RRF | sed 's/^/  /'
    endif

    #
    # Compare MRDOC
    #
    echo "    Verify MRDOC matches"
    set ct=`egrep -v '^RELEASE\|' $dir/../METASUBSET/MRDOC.RRF | diff $dir/MRDOC.RRF - | wc -l`
    if ($ct != 0) then
        echo "ERROR: MRDOC.RRF does not match"
	egrep -v '^RELEASE\|' $dir/../METASUBSET/MRDOC.RRF | diff $dir/MRDOC.RRF - | sed 's/^/  /'
    endif

    #
    # Compare remaining files (must be exact)
    #
    foreach f (`ls $dir/*RRF | $PATH_TO_PERL -pe '$dir="'$dir/'"; s/$dir//' | grep -v DOC | grep -v COLS | grep -v FILES | grep -v AMBIG | grep -v CHANGE | grep -v CONSO`)
	set md1=(`cat $dir/$f | $PATH_TO_MD5`)
	set md2=(`cat $dir/../METASUBSET/$f | $PATH_TO_MD5`)
	echo "    Verify $f matches"
	if ($md1 != $md2) then
	    echo "ERROR: $f does not match"
	    diff $dir/$f $dir/../METASUBSET/$f | sed 's/^/  /'
	endif
    # Next line MUST start at the beginning to end the foreach
end

    #
    #   Verify TS in MRDOC.VALUE where MRDOC.DOCKEY=TS
    #
    echo "    Verify TS in MRDOC.VALUE where MRDOC.DOCKEY=TS"
    cut -d\| -f3 $dir/../METASUBSET/MRCONSO.RRF | sort -u >! MRCONSO.TS.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="TS"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.TS.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  TS not in MRDOC.VALUE where MRDOC.DOCKEY=TS"
	awk -F\| '$3=="expanded_form"&&$1=="TS"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.TS.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.TS.$$

    #
    #   Verify STT in MRDOC.VALUE where MRDOC.DOCKEY=STT
    #
    echo "    Verify STT in MRDOC.VALUE where MRDOC.DOCKEY=STT"
    cut -d\| -f5 $dir/../METASUBSET/MRCONSO.RRF | sort -u >! MRCONSO.STT.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="STT"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.STT.$$ | wc -l`
    if ($cnt != 0) then
	echo "ERROR:  STT not in MRDOC.VALUE where MRDOC.DOCKEY=STT"
	awk -F\| '$3=="expanded_form"&&$1=="STT"{print $2}' $mrdoc | sort -u | comm -13 - MRCONSO.STT.$$ | sed 's/^/  /'
    endif
    rm -f MRCONSO.STT.$$

    #
    #   Verify that there is one P|PF per CUI,LAT
    #
    echo "    Verify that there is one [P]|PF per CUI,LAT"
    set ppf_ct=`$PATH_TO_PERL -ne 'split /\|/; print if $_[6] eq "Y" && $_[2] eq "P" && $_[4] eq "PF"' $dir/../METASUBSET/MRCONSO.RRF | wc -l`
    set cuilat_ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]\n"' $dir/../METASUBSET/MRCONSO.RRF | sort -u | wc -l`
    if ($ppf_ct != $cuilat_ct) then
	echo "ERROR: The P|PF count ($ppf_ct) does not equal the CUI|LAT ($cuilat_ct) count"
    endif

    #
    #   Distinct CUI/LAT count should match distinct CUI/LAT/LUI
    #   count where TS = P
    #
    echo "    Verify distinct CUI/LAT count should match distinct CUI/LAT/LUI where TS = P"
     set cl_cnt = `cut -d\| -f1,2 $dir/../METASUBSET/MRCONSO.RRF | sort -u | wc -l`
     set cll_cnt = `awk -F\| '$3=="P"{print $1"|"$2"|"$4}' $dir/../METASUBSET/MRCONSO.RRF | sort -u | wc -l`
     if ($cl_cnt != $cll_cnt) then
	echo "ERROR: The distinct CUI/LAT count ($cl_cnt) does not equal the CUI/LAT/LUI count ($cll_cnt)"
     endif

    #
    #   Distinct CUI/LAT/LUI count should match distinct CUI/LAT/SUI
    #   count where STT = PF
    #
    echo "    Verify distinct CUI/LAT/LUI count should match distinct CUI/LAT/SUI where STT = PF"
     set cll_cnt = `cut -d\| -f1,2,4 $dir/../METASUBSET/MRCONSO.RRF | sort -u | wc -l`
     set cls_cnt = `awk -F\| '$5=="PF"{print $1"|"$2"|"$6}' $dir/../METASUBSET/MRCONSO.RRF | sort -u | wc -l`
     if ($cll_cnt != $cls_cnt) then
	echo "ERROR: The distinct CUI/LAT/LUI count ($cll_cnt) does not equal the CUI/LAT/SUI count ($cls_cnt)"
     endif

    #
    #   Distinct CUI/LAT/SUI count should match distinct CUI/LAT/AUI
    #   count where ISPREF = Y
    #
    echo "    Verify distinct CUI/LAT/SUI count should match distinct CUI/LAT/AUI where ISPREF = Y"
     set cls_cnt = `cut -d\| -f1,2,6 $dir/../METASUBSET/MRCONSO.RRF | sort -u | wc -l`
     set cla_cnt = `awk -F\| '$7=="Y"{print $1"|"$2"|"$8}' $dir/../METASUBSET/MRCONSO.RRF | sort -u | wc -l`
     if ($cls_cnt != $cla_cnt) then
	echo "ERROR: The distinct CUI/LAT/SUI count ($cls_cnt) does not equal the CUI/LAT/AUI count ($cla_cnt)"
     endif

    #
    #   Verify that there is one PF SUI per CUI,LUI
    #
    echo "    Verify that there is one PF SUI per CUI,LUI"
    set pf_ct=`$PATH_TO_PERL -ne 'split /\|/; print "x\n" if $_[6] eq "Y" && $_[4] eq "PF"' $dir/../METASUBSET/MRCONSO.RRF | wc -l`
    set cuilui_ct=`$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]\n"' $dir/../METASUBSET/MRCONSO.RRF | sort -u | wc -l`
    if ($pf_ct != $cuilui_ct) then
	echo "ERROR: The S|PF count does not equal the CUI|LUI count"
    endif


else if ($target == "ORF") then

    source $MRD_HOME/bin/orf_checks.csh
    if ($status != 0) then
        echo "ERROR: Running orf_checks.csh FAILED: $!, $?"
        exit 1
    endif

else if ($target == "DOC") then

    # DO NOTHING
    echo "x" >> /dev/null

else

    echo "    Verify valid target"
    echo "ERROR: Invalid target $target"
    exit 1

endif

#
# Finished
#
