#!/bin/csh -f
#
# Script:    ref_integrity.csh
# Author:    BK, BAC, TK
#
# Validates RRF referential integrity.  Borrowed from MRD qa_counts.csh script.
#
source $ENV_HOME/bin/env.csh

#
# Set Environment, Aliases
#
set usage="Usage: $0 <dir>"
setenv LC_COLLATE C
setenv PATH "/bin:/usr/bin:/usr/local/bin"
alias sort sort -T .

#
# Parse arguments
#
if ($#argv != 1) then
    echo "ERROR: Wrong number of arguments"
    echo "$usage"
    exit 1
endif

set dir=$1

set ambig_sui=$dir/AMBIGSUI.RRF
set ambig_lui=$dir/AMBIGLUI.RRF
set mrmap=$dir/MRMAP.RRF
set mrsmap=$dir/MRSMAP.RRF
set mrhist=$dir/MRHIST.RRF
set mrcoc=$dir/MRCOC.RRF
set mrconso=$dir/MRCONSO.RRF
set mrcui=$dir/MRCUI.RRF
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

echo "Make MRCONSO.uis files"
    cut -d\| -f1 $mrconso | sort -u >! MRCONSO.uis.c.$$
    cut -d\| -f1,6 $mrconso | sort -u >! MRCONSO.uis.cs.$$
    cut -d\| -f 1,4 $mrconso | sort -u -T . >! MRCONSO.uis.cl.$$
    cut -d\| -f1,8 $mrconso | sort -u -T . >! MRCONSO.uis.ca.$$
    cut -d\| -f 1,4,6 $mrconso | sort -u  >! MRCONSO.uis.cls.$$
    cut -d\| -f 4,6,8,14 $mrconso | sort -u  >! MRCONSO.uis.alsc.$$

echo "General |null| check"
   grep '|null|' *RRF CHANGE/*RRF | sed 's/^/    /'

echo "Validate MRAUI"

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

    echo "    Verify sort order"
    sort -c -u $mraui >> /dev/null
    if ($status != 0) then
    echo "ERROR: MRAUI has incorrect sort order"
    endif

echo "Verify AMBIG"

    #
    #   Verify CUI|SUI in MRCONSO.CUI|SUI
    #
    echo "    Verify CUI|SUI in MRCONSO.CUI|SUI "
    $PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; \
      foreach $c (@c) { print "$c|$_[0]\n";}' $ambig_sui |\
    sort -u >! AMBIG.cuisui.$$
    set ct=(`comm -23 AMBIG.cuisui.$$ MRCONSO.uis.cs.$$ | wc -l`)
    if ($ct[1] != 0) then
    echo "ERROR: CUI,SUI combinations in AMBIG.SUI not in MRCONSO"
    comm -23 AMBIG.cuisui.$$ MRCONSO.uis.cs.$$ | sed 's/^/  /'
    endif
    rm -f AMBIG.cuisui.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $ambig_sui >> /dev/null
    if ($status != 0) then
    echo "ERROR: $ambig_sui has incorrect sort order"
    endif

    #
    #   Verify CUI|LUI in MRCONSO.CUI|LUI
    #
    echo "    Verify CUI|LUI in MRCONSO.CUI|LUI"
    $PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; \
        foreach $c (@c) { print "$c|$_[0]\n";}' $ambig_lui |\
    sort -u >! AMBIG.cuilui.$$
    set ct=`comm -23 AMBIG.cuilui.$$ MRCONSO.uis.cl.$$ | wc -l`
    if ($ct != 0) then
    echo "ERROR: CUI,SUI combinations in AMBIG.SUI not in MRCONSO"
    comm -23 AMBIG.cuilui.$$ MRCONSO.uis.cl.$$ | sed 's/^/  /'
    endif
    rm -f AMBIG.cuilui.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $ambig_lui >> /dev/null
    if ($status != 0) then
    echo "ERROR: $ambig_lui has incorrect sort order"
    endif

echo "Validate MRHIST"

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
    echo "    Verify CUI,SAB in in MRCONSO.CUI,SAB"
    cut -d\| -f1,12 $mrconso | sort -u -o MRCONSO.sabs.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[2]\n";' $mrhist | sort -u -o MRHIST.sabs.$$
    set ct=(`comm -23 MRHIST.sabs.$$ MRCONSO.sabs.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUI,SAB in MRHIST not in MRCONSO"
        comm -23 MRHIST.sabs.$$ MRCONSO.sabs.$$ | sed 's/^/  /'
    endif
    rm -f MRHIST.sabs.$$ MRCONSO.sabs.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrhist >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRHIST has incorrect sort order"
    endif

echo "Validate MRMAP"

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

echo "Validate MRSMAP"

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
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrmap >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRMAP has incorrect sort order"
    endif

echo "Validate MRCOC"

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
    #  Verify CUI1,AUI1 in MRCONSO.CUI,AUI
    #
    echo "    Verify CUI1,AUI1 in MRCONSO.CUI,AUI"
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
    rm -f mrcoc.tmp1.$$

    echo "    Verify COA are valid in MRSAT.ATV (where ATN=QA) (except for '<>')"
    (cat $mrcoc; echo "END") |\
    $PATH_TO_PERL -ne 'split /\|/; \
    if ($_[0] eq "END\n" && %qa) { print join("\n",keys %qa),"\n"; } \
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
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrcoc >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRCOC has incorrect sort order"
    endif

echo "Validate MRCONSO"

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
    echo "    Verify SAB|TTY|SUPPRESS in MRRANK.SAB|TTY|SUPPRESS"
    cut -d\| -f 2,3,4 $mrrank | sort -u -o MRRANK.sts.$$
    awk -F\| '$17!="O"&&$17!="E" {print $12"|"$13"|"$17}' $mrconso | sort -u -o MRCONSO.sts.$$
    set cnt=(`comm -13 MRRANK.sts.$$ MRCONSO.sts.$$ | wc -l `)
    if ($cnt[1] != 0) then
        echo "ERROR: SAB,TTY,SUPPRESS from MRRANK not in MRCONSO"
        comm -13 MRRANK.sts.$$ MRCONSO.sts.$$ | sed 's/^/  /'
    endif
    rm -f MRRANK.sts.$$ MRCONSO.sts.$$

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
    # Verify SAB|LAT in MRSAB.RSAB,LAT & v.v.
    #
    echo "    Verify MRCONSO.SAB,LAT IN MRSAB.RSAB,LAT"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[11]|$_[1]\n"' $mrconso |\
       sort -u >! sab.lat.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[3]|$_[19]\n" if $_[19] ne "";' $mrsab |\
    sort -u >! rsab.lat.$$
    set ct=`comm -23 sab.lat.$$ rsab.lat.$$ | wc -l`
    if ($ct > 0) then
        echo "ERROR: MRCONSO.SAB,LAT does not match MRSAB.RSAB,LAT"
        comm -23 sab.lat.$$ rsab.lat.$$ | sed 's/^/  /'
    endif
    rm -f rsab.lat.$$ sab.lat.$$


    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrconso >> /dev/null
    if ($status != 0) then
    echo "ERROR: MRCONSO has incorrect sort order"
    endif

echo "Validate MRCUI"

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
    #  Verify CUI1 not in MRCONSO.CUI
    #
    cut -d\| -f 1 $mrcui | sort -u >! mrcui.tmp1.$$
    cut -d\| -f 6 $mrcui | grep '^C' | sort -u >! mrcui.tmp2.$$
    echo "    Verify CUI1 not in MRCONSO.CUI"
    set ct=`comm -12 mrcui.tmp1.$$ MRCONSO.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 in MRCONSO.CUI"
    comm -12 mrcui.tmp1.$$ MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI2 in MRCONSO.CUI (where REL!=DEL)
    #
    echo "    Verify CUI2 in MRCONSO.CUI or MRCUI.CUI"
    set ct=`comm -23 mrcui.tmp2.$$ MRCONSO.uis.c.$$ | comm -23 - mrcui.tmp1.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI2 not in MRCONSO.CUI or MRCUI.CUI"
    comm -23 mrcui.tmp2.$$ MRCONSO.uis.c.$$ | comm -23 - mrcui.tmp1.$$ | sed 's/^/  /'
    endif
    rm -f mrcui.tmp[12].$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrcui >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRCUI has incorrect sort order"
    endif

echo "Validate MRHIER"

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
    rm -f mrhier.tmp1.$$ 

    #  Verify sort order
    echo "    Verify sort order"
    sort -c -u $mrhier >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRHIER has incorrect sort order"
    endif

echo "Validate MRDEF"

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
    #   Verify CUI in MRCONSO.CUI
    #
    echo "    Verify CUI in MRCONSO.CUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n"' $mrdef | sort -u >! MRDEF.uis.c.$$
    set ct=(`comm -23 MRDEF.uis.c.$$ MRCONSO.uis.c.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUIs in MRDEF not in MRCONSO"
    comm -23 MRDEF.uis.c.$$ MRCONSO.uis.c.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.uis.c.$$ 

    #
    #   Verify CUI,SAB in MRCONSO.CUI,SAB
    #
    echo "    Verify CUI,SAB in in MRCONSO.CUI,SAB"
    cut -d\| -f1,12 $mrconso | sort -u -o MRCONSO.sabs.$$
    cut -d\| -f1,5 $mrdef | grep -v "MTH" | sort -u -o MRDEF.sabs.$$
    set ct=(`comm -23 MRDEF.sabs.$$ MRCONSO.sabs.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "WARNING: CUI,SAB in MRDEF not in MRCONSO"
    comm -23 MRDEF.sabs.$$ MRCONSO.sabs.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.sabs.$$ MRCONSO.sabs.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrdef >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRDEF has incorrect sort order"
    endif

echo "Validate MRFILESCOLS"

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
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrfiles >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRFILES has incorrect sort order"
    endif

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
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrcols >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRCOLS has incorrect sort order"
    endif

echo "Validate MRRANK"
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
    #   Verify SAB|TTY in MRCONSO.SAB|TTY
    #
    echo "    Verify SAB|TTY in MRRANK.SAB|TTY"
    cut -d\| -f 2,3 $mrrank | sort -u -o MRRANK.sabtty.$$
    cut -d\| -f 12,13 $mrconso | sort -u -o MRCONSO.sabtty.$$
    set cnt=(`comm -23 MRRANK.sabtty.$$ MRCONSO.sabtty.$$ | wc -l`)
    if ($cnt[1] != 0) then
    echo "ERROR: SAB,TTY from MRRANK not in MRCONSO"
    comm -23 MRRANK.sabtty.$$ MRCONSO.sabtty.$$ | sed 's/^/  /'
    endif
    rm -f MRRANK.sabtty.$$ MRCONSO.sabtty.$$


    #
    #   Verify sort order (its in reverse)
    #
    echo "    Verify sort order (its in reverse)"
    sort -c -u -r $mrrank >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRRANK has incorrect sort order"
    endif

echo "Validate MRREL"

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
    rm -f mrrel.tmp1.$$ 

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

    #
    #  Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrrel >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRREL has incorrect sort order"
    endif

echo "Validate MRSAB"

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
    #   Verify SRL in MRDOC
    #
    echo "    Verify SRL in MRDOC"
    cut -d\| -f14 $mrsab | sort -u >! mrsab.tmp1.$$
    awk -F\| '$3=="expanded_form"&&$1=="SRL"{print $2}' $mrdoc | sort -u >! mrdoc.tmp1.$$
    set ct=`diff mrsab.tmp1.$$ mrdoc.tmp1.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR:  SRL not in MRDOC"
            diff mrsab.tmp1.$$ mrdoc.tmp1.$$
    endif
    rm -f mrsab.tmp1.$$ mrdoc.tmp1.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrsab >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRSAB has incorrect sort order"
    endif

echo "Validate MRSAT"

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
    #  Verify CUI|LUI|SUI in MRCONSO.CUI|LUI|SUI where sui!='' and UI =~ /A*/
    #
    echo "    Verify CUI|LUI|SUI in MRCON.CUI|LUI|SUI where sui!=''"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]|$_[2]\n" if $_[2] && $_[3] =~ /A*/ ' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    set ct=`comm -23 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUI|LUI|SUIs in MRSAT not in MRCONSO"
    comm -23 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$

    #
    #  Verify LUI|SUI|AUI|CODE in MRCONSO.LUI|SUI|AUI|CODE where sui!='' and uitype ='AUI'
    #
    echo "    Verify LUI|SUI|AUI|CODE in MRCONSO.LUI|SUI|AUI|CODE where sui!=''"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[1]|$_[2]|$_[3]|$_[5]\n" if $_[2];' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    set ct=`comm -23 mrsat.tmp1.$$ MRCONSO.uis.alsc.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are LUI|SUI|AUI|CODEs in MRSAT not in MRCONSO"
    comm -23 mrsat.tmp1.$$ MRCONSO.uis.alsc.$$ | sed 's/^/  /'
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
        echo "WARNING: There are CUIs in MRCONSO not in MRSAT"
    join -t\| -j 1 -v 2 -o 2.1 mrsat.tmp1.$$ MRCONSO.uis.cls.$$ |\
        sort -u | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$


    #
    #  Verify CUI|METAUI in MRCONSO.CUI|AUI where METAUI =~ /^A/
    #
    echo "    Verify CUI|METAUI in MRCONSO.CUI|AUI where METAUI =~ /^A/"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]\n" if $_[3] =~ /^A/' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    set ct=`comm -23  mrsat.tmp1.$$ MRCONSO.uis.ca.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUI|AUI in MRSAT not in MRCONSO.CUI|AUI"
    comm -23 mrsat.tmp1.$$ MRCONSO.uis.ca.$$ | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$ 

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
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrsat >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRSAT has incorrect sort order"
    endif

echo "Validate MRSTY"

    #
    #   Verify CUI in MRCONSO.CUI
    #
    echo "    Verify CUI in MRCONSO.CUI"
    cut -d\| -f1 $mrsty | sort -u >! MRSTY.uis.c.$$
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
    rm -f MRSTY.uis.c.$$ 

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrsty >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRSTY has incorrect sort order"
    endif

echo "Validate MRDOC"

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

echo "Validate MRX"

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
    rm -f MRDOC.LAT.$$

    #
    #  Verify CUI|LUI|SUI matches (both directions) with matching language
    #
    echo "    Verify MRCONSO CUI|LUI|SUI in MRXNS_ENG CUI|LUI|SUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]|$_[5]\n" if $_[1] eq "ENG";' $mrconso | sort -u >! mrx.tmp2.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxns | sort -u >! mrx.tmp1.$$
    set null_lui=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | cut -d\| -f 2 | sort -u | head -1`
    if ($null_lui == "") then
    set null_lui = NULL_LUI
    endif
    set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXNS_ENG"
        comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
    endif
    echo "    Verify MRXNS_ENG CUI|LUI|SUI in MRCONSO CUI|LUI|SUI"
    set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXNS_ENG"
        comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | sed 's/^/  /'
    endif

    echo "    Verify MRCONSO CUI|LUI|SUI in MRXNW_ENG CUI|LUI|SUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxnw | sort -u >! mrx.tmp1.$$
    set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXNW_ENG"
    comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
    endif
    echo "    Verify MRXNW_ENG CUI|LUI|SUI in MRCONSO CUI|LUI|SUI"
    set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXNW_ENG"
    comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
    endif
    rm -f mrx.tmp[12].$$
    ls $dir/MRXW* | sed 's/.*\/MRXW.//' | sed 's/.RRF//' >! mrx.lats.$$
    foreach f (`cat mrx.lats.$$`)
        echo "    Verify MRCONSO CUI|LUI|SUI in MRXW_$f CUI|LUI|SUI"
    setenv LAT $f
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]|$_[5]\n" if $_[1] eq "'$f'" \
        && $_[14] !~ /^(=|<=|>=|\+|\+\+|\+\+\+|\+\+\+\+|<|>)$/;' $mrconso |\
        sort -u >! mrx.tmp2.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' ${mrxw}_$f.RRF | sort -u >! mrx.tmp1.$$

    set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCONSO not in MRXW_$f"
        comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
    endif
    echo "    Verify MRXW_$f CUI|LUI|SUI in MRCONSO CUI|LUI|SUI"
    set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
        if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRXW_$f not in MRCONSO"
        comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
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

echo "Remove MRCONSO.uis files"
/bin/rm -f MRCONSO.uis.*.$$

