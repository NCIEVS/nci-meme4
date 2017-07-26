#!/bin/csh -f
#
# Script:    orf_integrity.csh
# Author:    BK, BAC, TK
#
# Validates ORF referential integrity. 
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

set ambig_sui=$dir/AMBIG.SUI
set ambig_lui=$dir/AMBIG.LUI
set mrcoc=$dir/MRCOC
set mrcon=$dir/MRCON
set mrso=$dir/MRSO
set mrcui=$dir/MRCUI
set deleted_cui=$dir/CHANGE/DELETED.CUI
set deleted_lui=$dir/CHANGE/DELETED.LUI
set deleted_sui=$dir/CHANGE/DELETED.SUI
set merged_cui=$dir/CHANGE/MERGED.CUI
set merged_lui=$dir/CHANGE/MERGED.LUI
set mrcxt=$dir/MRCXT
set mrdef=$dir/MRDEF
set mrdoc=$dir/MRDOC
set mrfiles=$dir/MRFILES
set mrcols=$dir/MRCOLS
set mrrank=$dir/MRRANK
set mrrel=$dir/MRREL
set mrsab=$dir/MRSAB
set mrsat=$dir/MRSAT
set mrsty=$dir/MRSTY
set mrxw=$dir/MRXW
set mrxnw=$dir/MRXNW.ENG
set mrxns=$dir/MRXNS.ENG

# CUI,LAT,TS,LUI,STT,SUI,STR,LRL
echo "Make MRCON.uis files"
    cut -d\| -f1 $mrcon | sort -u >! MRCON.uis.c.$$
    cut -d\| -f1,6 $mrcon | sort -u >! MRCON.uis.cs.$$
    cut -d\| -f 1,4 $mrcon | sort -u -T . >! MRCON.uis.cl.$$
    cut -d\| -f 1,4,6 $mrcon | sort -u  >! MRCON.uis.cls.$$

# CUI,LUI,SUI,SAB,TTY,CODE,SRL
echo "Make MRSO.uis files"
    cut -d\| -f1 $mrso | sort -u >! MRSO.uis.c.$$
    cut -d\| -f1,3 $mrso | sort -u >! MRSO.uis.cs.$$
    cut -d\| -f 1,2 $mrso | sort -u -T . >! MRSO.uis.cl.$$
    cut -d\| -f 1,2,3 $mrso | sort -u  >! MRSO.uis.cls.$$

echo "General |null| check"
   grep '|null|' MR* CHANGE/* | sed 's/^/    /'

# LUI,CUI
# SUI,CUI
echo "Verify AMBIG"

    #
    #   Verify CUI|SUI in MRCON.CUI|SUI
    #
    echo "    Verify CUI|SUI in MRCON.CUI|SUI "
    $PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; \
      foreach $c (@c) { print "$c|$_[0]\n";}' $ambig_sui |\
    sort -u >! AMBIG.cuisui.$$
    set ct=(`comm -23 AMBIG.cuisui.$$ MRCON.uis.cs.$$ | wc -l`)
    if ($ct[1] != 0) then
    echo "ERROR: CUI,SUI combinations in AMBIG.SUI not in MRCON"
    comm -23 AMBIG.cuisui.$$ MRCON.uis.cs.$$ | sed 's/^/  /'
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
    #   Verify CUI|LUI in MRCON.CUI|LUI
    #
    echo "    Verify CUI|LUI in MRCON.CUI|LUI"
    $PATH_TO_PERL -ne 'chop; split /\|/; @c = split/,/, $_[1]; \
        foreach $c (@c) { print "$c|$_[0]\n";}' $ambig_lui |\
    sort -u >! AMBIG.cuilui.$$
    set ct=`comm -23 AMBIG.cuilui.$$ MRCON.uis.cl.$$ | wc -l`
    if ($ct != 0) then
    echo "ERROR: CUI,SUI combinations in AMBIG.SUI not in MRCON"
    comm -23 AMBIG.cuilui.$$ MRCON.uis.cl.$$ | sed 's/^/  /'
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

echo "Validate MRCOC"

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    #
    # CUI1,CUI2,SOC,COT,COF,COA
    # VCUI,RCUI,VSAB,RSAB,SON,SF,SVER,MSTART,MEND,IMETA,RMETA,SLC,SCC,SRL,TFR,CFR,CXTY,TTYL,ATNL,LAT,CENC,CURVER,SABIN
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f3 $mrcoc | sort -u >! MRCOC.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCOC.SAB.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
    awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCOC.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRCOC.SAB.$$

    #
    #   Verify COT in MRDOC.VALUE where MRDOC.DOCKEY=COT"
    # CUI1,CUI2,SOC,COT,COF,COA
    # DOCKEY,VALUE,TYPE,EXPL
    #
    echo "    Verify COT in MRDOC.VALUE where MRDOC.DOCKEY=COT"
    cut -d\| -f4 $mrcoc | sort -u >! MRCOC.COT.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="COT"{print $2}' $mrdoc | sort -u | comm -13 - MRCOC.COT.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  COT not in MRDOC.VALUE where MRDOC.DOCKEY=COT"
    awk -F\| '$3=="expanded_form"&&$1=="COT"{print $2}' $mrdoc | sort -u | comm -13 - MRCOC.COT.$$ | sed 's/^/  /'
    endif
    rm -f MRCOC.COT.$$

    #
    #  Verify CUI1 in MRCON.CUI
    # CUI1,CUI2,SOC,COT,COF,COA
    # CUI,LAT,TS,LUI,STT,SUI,STR,LRL
    #
    echo "    Verify CUI1 in MRCON.CUI"
    cut -d\| -f 1 $mrcoc | sort -u >! mrcoc.tmp1.$$
    set ct=`comm -23 mrcoc.tmp1.$$ MRCON.uis.c.$$ | wc -l`
     if ($ct != 0) then
        echo "ERROR: CUI1 not in MRCON.CUI"
    comm -23 mrcoc.tmp1.$$ MRCON.uis.c.$$  | sed 's/^/  /'
    endif

    #
    #  Verify CUI2 in MRCON.CUI
    # CUI1,CUI2,SOC,COT,COF,COA
    # CUI,LAT,TS,LUI,STT,SUI,STR,LRL
    #
    echo "    Verify CUI2 in MRCON.CUI"
    awk -F\| '$2!="" {print $2}' $mrcoc | sort -u >! mrcoc.tmp1.$$
    set ct=`comm -23 mrcoc.tmp1.$$ MRCON.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI2 not in MRCON.CUI"
    comm -23 mrcoc.tmp1.$$ MRCON.uis.c.$$  | sed 's/^/  /'
    endif
    rm -f mrcoc.tmp1.$$

    # 
    # CUI1,CUI2,SOC,COT,COF,COA
    # CUI,LUI,SUI,CODE,ATN,SAB,ATV|
    #
    echo "    Verify COA are valid in MRSAT.ATV (where ATN=QA) (except for '<>')"
    (cat $mrcoc; echo "END") |\
    $PATH_TO_PERL -ne 'split /\|/; \
    if ($_[0] eq "END\n" && %qa) { print join("\n",keys %qa),"\n"; } \
    foreach $p (split /,/,$_[7]) { \
    ($q,$f) = split /=/, $p; $qa{$q} = 1 if $q ne "<>"; } ' | sort -u >! mrcoc.qa.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[6]\n" if $_[4] eq "QA" && $_[5] =~ /MSH/;' $mrsat | \
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
    # CUI1,CUI2,SOC,COT,COF,COA
    # CUI,LUI,SUI,CODE,ATN,SAB,ATV
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
    if ($_[4] =~ /^MED(\d\d\d\d)$/) { $year=$1; } \
    print "$_[0]\n" \
      if ($year && $_[5] eq "NLM-MED" && $year >= $y && $_[6] =~ /^\*/)' $mrsat |\
    sort -u >! mrsat.tmp1.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]\n" \
        if ($_[3] eq "L" || $_[3] eq "LQ" || $_[3] eq "LQB")' $mrcoc |\
    sort -u >! mrsat.tmp2.$$
    set ct=`join -t\| -v 1 -j 1 mrsat.tmp1.$$ mrsat.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI in MRSAT with MED attribute not in MRCOC"
    join -v 1 -t\| -j 1 mrsat.tmp1.$$ mrsat.tmp2.$$ | sed 's/^/  /'
    endif

    #
    #  Verify MRSAT.CUI in MRCOC.CUI2 (where ATN like MED% and COT=L,LQ,LQB)
    # CUI1,CUI2,SOC,COT,COF,COA
    # CUI,LUI,SUI,CODE,ATN,SAB,ATV
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

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrcoc >> /dev/null
    if ($status != 0) then
        echo "ERROR: MRCOC has incorrect sort order"
    endif

echo "Validate MRCON and MRSO?"

    #
    # Verify uis files match
    #
    echo "    Verify CLS in MRCON and MRSO"
    set cnt=(`diff MRSO.uis.cls.$$ MRCON.uis.cls.$$ | wc -l`)
    if ($cnt[1] != 0) then
        echo "ERROR: MRCON uis do not match MRSO uis"
        diff MRSO.uis.cls.$$ MRCON.uis.cls.$$ | sed 's/^/  /'
    endif
    #
    #   Verify SAB|TTY in MRRANK.SAB|TTY
    # CUI,LUI,SUI,SAB,TTY,CODE,SRL
    # RANK,SAB,TTY,SUPRES
    #
    echo "    Verify SAB|TTY in MRRANK.SAB|TTY"
    cut -d\| -f 2,3 $mrrank | sort -u -o MRRANK.sabtty.$$
    cut -d\| -f 4,5 $mrso | sort -u -o MRCON.sabtty.$$
    set cnt=(`comm -13 MRRANK.sabtty.$$ MRCON.sabtty.$$ | wc -l`)
    if ($cnt[1] != 0) then
        echo "ERROR: SAB,TTY from MRSO not in MRRANK"
        comm -13 MRRANK.sabtty.$$ MRCON.sabtty.$$ | sed 's/^/  /'
    endif
    rm -f MRRANK.sabtty.$$ MRCON.sabtty.$$


    #
    #   Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT
    # CUI,LAT,TS,LUI,STT,SUI,STR,LRL
    # DOCKEY,VALUE,TYPE,EXPL
    echo "    Verify LAT in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
    cut -d\| -f2 $mrcon | sort -u >! MRCON.LAT.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="LAT"{print $2}' $mrdoc | sort -u | comm -13 - MRCON.LAT.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  LAT not in MRDOC.VALUE where MRDOC.DOCKEY=LAT"
    awk -F\| '$3=="expanded_form"&&$1=="LAT"{print $2}' $mrdoc | sort -u | comm -13 - MRCON.LAT.$$ | sed 's/^/  /'
    endif
    rm -f MRCON.LAT.$$

    #
    #   Verify TS in MRDOC.VALUE where MRDOC.DOCKEY=TS
    # CUI,LAT,TS,LUI,STT,SUI,STR,LRL
    # DOCKEY,VALUE,TYPE,EXPL
    #
    echo "    Verify TS in MRDOC.VALUE where MRDOC.DOCKEY=TS"
    cut -d\| -f3 $mrcon | sort -u >! MRCON.TS.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="TS"{print $2}' $mrdoc | sort -u | comm -13 - MRCON.TS.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  TS not in MRDOC.VALUE where MRDOC.DOCKEY=TS"
    awk -F\| '$3=="expanded_form"&&$1=="TS"{print $2}' $mrdoc | sort -u | comm -13 - MRCON.TS.$$ | sed 's/^/  /'
    endif
    rm -f MRCON.TS.$$

    #
    #   Verify STT in MRDOC.VALUE where MRDOC.DOCKEY=STT
    # CUI,LAT,TS,LUI,STT,SUI,STR,LRL
    # DOCKEY,VALUE,TYPE,EXPL
    #
    echo "    Verify STT in MRDOC.VALUE where MRDOC.DOCKEY=STT"
    cut -d\| -f5 $mrcon | sort -u >! MRCON.STT.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="STT"{print $2}' $mrdoc | sort -u | comm -13 - MRCON.STT.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  STT not in MRDOC.VALUE where MRDOC.DOCKEY=STT"
    awk -F\| '$3=="expanded_form"&&$1=="STT"{print $2}' $mrdoc | sort -u | comm -13 - MRCON.STT.$$ | sed 's/^/  /'
    endif
    rm -f MRCON.STT.$$

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    # CUI,LUI,SUI,SAB,TTY,CODE,SRL
    # DOCKEY,VALUE,TYPE,EXPL
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f4 $mrso | sort -u >! MRCON.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCON.SAB.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
    awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRCON.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRCON.SAB.$$

    #
    #   Verify TTY in MRDOC.VALUE where MRDOC.DOCKEY=TTY
    # CUI,LUI,SUI,SAB,TTY,CODE,SRL
    # DOCKEY,VALUE,TYPE,EXPL
    #
    echo "    Verify TTY in MRDOC.VALUE where MRDOC.DOCKEY=TTY"
    cut -d\| -f5 $mrso | sort -u >! MRCON.TTY.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="TTY"{print $2}' $mrdoc | sort -u | comm -13 - MRCON.TTY.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  TTY not in MRDOC.VALUE where MRDOC.DOCKEY=TTY"
    awk -F\| '$3=="expanded_form"&&$1=="TTY"{print $2}' $mrdoc | sort -u | comm -13 - MRCON.TTY.$$ | sed 's/^/  /'
    endif
    rm -f MRCON.TTY.$$

    #
    #   Verify sort order
    #
    echo "    Verify sort order"
    sort -c -u $mrcon >> /dev/null
    if ($status != 0) then
    echo "ERROR: MRCON has incorrect sort order"
    endif

    sort -c -u $mrso >> /dev/null
    if ($status != 0) then
    echo "ERROR: MRSO has incorrect sort order"
    endif

if (-e $mrcui) then
    echo "Validate MRCUI"

    #
    #   Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    # CUI1,VER,CREL,CUI2,MAPIN
    # DOCKEY,VALUE,TYPE,EXPL
    echo "    Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    cut -d\| -f3 $mrcui | sort -u >! MRCUI.REL.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRCUI.REL.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  REL not in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRCUI.REL.$$ | sed 's/^/  /'
    endif
    rm -f MRCUI.REL.$$

    #
    #  Verify CUI1 not in MRCON.CUI
    # CUI1,VER,CREL,CUI2,MAPIN
    #
    cut -d\| -f 1 $mrcui | sort -u >! mrcui.tmp1.$$
    cut -d\| -f 4 $mrcui | grep '^C' | sort -u >! mrcui.tmp2.$$
    echo "    Verify CUI1 not in MRCON.CUI"
    set ct=`comm -12 mrcui.tmp1.$$ MRCON.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 in MRCON.CUI"
    comm -12 mrcui.tmp1.$$ MRCON.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI2 in MRCON.CUI (where REL!=DEL)
    #
    echo "    Verify CUI2 in MRCON.CUI or MRCUI.CUI"
    set ct=`comm -23 mrcui.tmp2.$$ MRCON.uis.c.$$ | comm -23 - mrcui.tmp1.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI2 not in MRCON.CUI or MRCUI.CUI"
    comm -23 mrcui.tmp2.$$ MRCON.uis.c.$$ | comm -23 - mrcui.tmp1.$$ | sed 's/^/  /'
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

endif


echo "Validate MRDEF"

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    # CUI,SAB,DEF
    # VCUI,RCUI,VSAB,RSAB,SON,SF,SVER,MSTART,MEND,IMETA,RMETA,SLC,SCC,SRL,TFR,CFR,CXTY,TTYL,ATNL,LAT,CENC,CURVER,SABIN
    # 
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f2 $mrdef | sort -u >! MRDEF.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRDEF.SAB.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
    awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRDEF.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.SAB.$$

    #
    #   Verify CUI in MRCON.CUI
    #
    echo "    Verify CUI in MRCON.CUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n"' $mrdef | sort -u >! MRDEF.uis.c.$$
    set ct=(`comm -23 MRDEF.uis.c.$$ MRCON.uis.c.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUIs in MRDEF not in MRCON"
    comm -23 MRDEF.uis.c.$$ MRCON.uis.c.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.uis.c.$$ 

    #
    #   Verify CUI,SAB in MRCON.CUI,SAB
    # CUI,SAB,DEF
    # CUI,LUI,SUI,SAB,TTY,CODE,SRL
    echo "    Verify CUI,SAB in in MRCON.CUI,SAB"
    cut -d\| -f1,4 $mrso | sort -u -o MRCON.sabs.$$
    cut -d\| -f1,2 $mrdef | grep -v "MTH" | sort -u -o MRDEF.sabs.$$
    set ct=(`comm -23 MRDEF.sabs.$$ MRCON.sabs.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "WARNING: CUI,SAB in MRDEF not in MRCON"
    comm -23 MRDEF.sabs.$$ MRCON.sabs.$$ | sed 's/^/  /'
    endif
    rm -f MRDEF.sabs.$$ MRCON.sabs.$$

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
    #   Verify SAB|TTY in MRRANK.SAB|TTY
    # CUI,LUI,SUI,SAB,TTY,CODE,SRL
    # RANK,SAB,TTY,SUPRES
    #
    echo "    Verify SAB|TTY in MRSO.SAB|TTY"
    cut -d\| -f 2,3 $mrrank | sort -u -o MRRANK.sabtty.$$
    cut -d\| -f 4,5 $mrso | sort -u -o MRSO.sabtty.$$
    set cnt=(`comm -23 MRRANK.sabtty.$$ MRSO.sabtty.$$ | wc -l`)
    if ($cnt[1] != 0) then
        echo "ERROR: SAB,TTY from MRRANK not in MRSO"
        comm -23 MRRANK.sabtty.$$ MRSO.sabtty.$$ | sed 's/^/  /'
    endif
    rm -f MRRANK.sabtty.$$ MRSO.sabtty.$$

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
    # CUI1,REL,CUI2,RELA,SAB,SL,MG
    # DOCKEY,VALUE,TYPE,EXPL 
    echo "    Verify REL in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    cut -d\| -f2 $mrrel | sort -u >! MRREL.REL.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.REL.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  REL not in MRDOC.VALUE where MRDOC.DOCKEY=REL"
    awk -F\| '$3=="expanded_form"&&$1=="REL"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.REL.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.REL.$$

    #
    #   Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    # CUI1,REL,CUI2,RELA,SAB,SL,MG
    # DOCKEY,VALUE,TYPE,EXPL 
    #
    echo "    Verify RELA in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    cut -d\| -f4 $mrrel | sort -u >! MRREL.RELA.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.RELA.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  RELA not in MRDOC.VALUE where MRDOC.DOCKEY=RELA"
    awk -F\| '$3=="expanded_form"&&$1=="RELA"{print $2}' $mrdoc | sort -u | comm -13 - MRREL.RELA.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.RELA.$$

    #
    # Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y
    # CUI1,REL,CUI2,RELA,SAB,SL,MG
    # DOCKEY,VALUE,TYPE,EXPL 
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f5 $mrrel | sort -u >! MRREL.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRREL.SAB.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
    awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRREL.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.SAB.$$

    #
    # Verify SL in MRSAB.RSAB where MRSAB.SABIN=Y
    # CUI1,REL,CUI2,RELA,SAB,SL,MG
    # VCUI,RCUI,VSAB,RSAB,SON,SF,SVER,MSTART,MEND,IMETA,RMETA,SLC,SCC,SRL,TFR,CFR,CXTY,TTYL,ATNL,LAT,CENC,CURVER,SABIN
    #
    echo "    Verify SL in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f6 $mrrel | sort -u >! MRREL.SL.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRREL.SL.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  SL not in MRSAB.RSAB where MRSAB.SABIN=Y"
    awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRREL.SL.$$ | sed 's/^/  /'
    endif
    rm -f MRREL.SL.$$

    #
    #  Verify CUI1 in MRCON.CUI
    #
    echo "    Verify CUI1 in MRCON.CUI"
    set ct=`join -v 1 -t\| -j1 1 -j2 1 $mrrel MRCON.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1 in MRREL not in MRCON.CUI"
    join -v 1 -t\| -j1 1 -j2 1 -o 1.1 $mrrel MRCON.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI2 in MRCON.CUI
    # CUI1,REL,CUI2,RELA,SAB,SL,MG
    #
    echo "    Verify CUI2 in MRCON.CUI"
    set ct=`cut -d\| -f3 $mrrel | sort -u | join -v 1 -t\| -j1 1 -j2 1 - MRCON.uis.c.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI2 in MRREL not in MRCON.CUI"
    cut -d\| -f5 $mrrel | sort -u |\
        join -v 1 -t\| -j1 1 -j2 1 - MRCON.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #  Verify CUI1|CUI2 list is the same as CUI2|CUI1
    #
    echo "    Verify CUI1|AUI1|CUI2|AUI2 list is the same as CUI2|AUI2|CUI1|AUI1"
    cut -d\| -f 1,3 $mrrel | sort -u >! MRREL.cui12.$$
    awk -F\| '{print $3"|"$1}' $mrrel | sort -u >! MRREL.cui21.$$
    set ct=`diff MRREL.cui12.$$ MRREL.cui21.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI1|CUI2 does not match CUI2|CUI1"
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
    # VCUI,RCUI,VSAB,RSAB,SON,SF,SVER,MSTART,MEND,IMETA,RMETA,SLC,SCC,SRL,TFR,CFR,CXTY,TTYL,ATNL,LAT,CENC,CURVER,SABIN
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
    # CUI,LUI,SUI,CODE,ATN,SAB,ATV
    # VCUI,RCUI,VSAB,RSAB,SON,SF,SVER,MSTART,MEND,IMETA,RMETA,SLC,SCC,SRL,TFR,CFR,CXTY,TTYL,ATNL,LAT,CENC,CURVER,SABIN
    #
    echo "    Verify SAB in MRSAB.RSAB where MRSAB.SABIN=Y"
    cut -d\| -f6 $mrsat | sort -u >! MRSAT.SAB.$$
    set cnt = `awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRSAT.SAB.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  SAB not in MRSAB.RSAB where MRSAB.SABIN=Y"
    awk -F\| '$23=="Y"{print $4}' $mrsab | sort -u | comm -13 - MRSAT.SAB.$$ | sed 's/^/  /'
    endif
    rm -f MRSAT.SAB.$$

    #
    #   Verify ATN in MRDOC.VALUE where MRDOC.DOCKEY=ATN"
    # CUI,LUI,SUI,CODE,ATN,SAB,ATV
    # DOCKEY,VALUE,TYPE,EXPL
    #
    echo "    Verify ATN in MRDOC.VALUE where MRDOC.DOCKEY=ATN"
    cut -d\| -f5 $mrsat | sort -u >! MRSAT.ATN.$$
    set cnt = `awk -F\| '$3=="expanded_form"&&$1=="ATN"{print $2}' $mrdoc | sort -u | comm -13 - MRSAT.ATN.$$ | wc -l`
    if ($cnt != 0) then
    echo "ERROR:  ATN not in MRDOC.VALUE where MRDOC.DOCKEY=ATN"
    awk -F\| '$3=="expanded_form"&&$1=="ATN"{print $2}' $mrdoc | sort -u | comm -13 - MRSAT.ATN.$$ | sed 's/^/  /'
    endif
    rm -f MRSAT.ATN.$$

    #
    #  Verify CUI|LUI|SUI in MRCON.CUI|LUI|SUI where sui!='' and UI =~ /A*/
    # CUI,LUI,SUI,CODE,ATN,SAB,ATV
    #
    echo "    Verify CUI|LUI|SUI in MRCON.CUI|LUI|SUI where sui!=''"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[1]|$_[2]\n" if $_[2] && $_[3] =~ /A*/ ' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    set ct=`comm -23 mrsat.tmp1.$$ MRCON.uis.cls.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUI|LUI|SUIs in MRSAT not in MRCON"
    comm -23 mrsat.tmp1.$$ MRCON.uis.cls.$$ | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$


    #
    #  Verify CUI in MRCON.CUI where SUI=''
    #
    echo "    Verify CUI in MRCON.CUI where SUI=''"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]\n" unless $_[2]' $mrsat |\
       sort -u >! mrsat.tmp1.$$
    set ct=`join -t\| -j 1 -v 1 mrsat.tmp1.$$ MRCON.uis.cls.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: There are CUIs in MRSAT not in MRCON"
    join -t\| -j 1 -v 1 mrsat.tmp1.$$ MRCON.uis.cls.$$ | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$

    #
    #  Verify MRCON.CUI in MRSAT.CUI
    #
    echo "    Verify MRCON.CUI in MRSAT.CUI"
    cut -d\| -f 1 $mrsat | sort -u >! mrsat.tmp1.$$
    set ct=`join -t\| -j 1 -v 2 -o 2.1 mrsat.tmp1.$$ MRCON.uis.cls.$$ | wc -l`
    if ($ct != 0) then
        echo "WARNING: There are CUIs in MRCON not in MRSAT"
    join -t\| -j 1 -v 2 -o 2.1 mrsat.tmp1.$$ MRCON.uis.cls.$$ |\
        sort -u | sed 's/^/  /'
    endif
    rm -f mrsat.tmp1.$$

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
    #   Verify CUI in MRCON.CUI
    # CUI,TUI,STY
    echo "    Verify CUI in MRCON.CUI"
    cut -d\| -f1 $mrsty | sort -u >! MRSTY.uis.c.$$
    set ct=(`comm -23 MRSTY.uis.c.$$ MRCON.uis.c.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUIs in MRSTY not in MRCON"
    comm -23 MRSTY.uis.c.$$ MRCON.uis.c.$$ | sed 's/^/  /'
    endif

    #
    #   Verify MRCON.CUI in CUI
    #
    echo "    Verify MRCON.CUI in CUI"
    set ct=(`comm -13 MRSTY.uis.c.$$ MRCON.uis.c.$$ | wc -l`)
    if ($ct[1] != 0) then
        echo "ERROR: CUIs in MRCON not in MRSTY"
    comm -13 MRSTY.uis.c.$$ MRCON.uis.c.$$ | sed 's/^/  /'
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
    # CUI,LAT,TS,LUI,STT,SUI,STR,LRL
    echo "    Verify MRCON CUI|LUI|SUI in MRXNS_ENG CUI|LUI|SUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]|$_[5]\n" if $_[1] eq "ENG";' $mrcon | sort -u >! mrx.tmp2.$$
    $PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxns | sort -u >! mrx.tmp1.$$
    set null_lui=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | cut -d\| -f 2 | sort -u | head -1`
    if ($null_lui == "") then
    set null_lui = NULL_LUI
    endif
    set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCON not in MRXNS_ENG"
        comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
    endif
    echo "    Verify MRXNS_ENG CUI|LUI|SUI in MRCON CUI|LUI|SUI"
    set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCON not in MRXNS_ENG"
        comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | sed 's/^/  /'
    endif

    echo "    Verify MRCON CUI|LUI|SUI in MRXNW_ENG CUI|LUI|SUI"
    $PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' $mrxnw | sort -u >! mrx.tmp1.$$
    set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCON not in MRXNW_ENG"
    comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
    endif
    echo "    Verify MRXNW_ENG CUI|LUI|SUI in MRCON CUI|LUI|SUI"
    set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
    if ($ct != 0) then
        echo "ERROR: CUI|LUI|SUI in MRCON not in MRXNW_ENG"
    comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
    endif
    rm -f mrx.tmp[12].$$
    ls $dir/MRXW* | sed 's/.*\/MRXW.//'  >! mrx.lats.$$
    foreach f (`cat mrx.lats.$$`)
        echo "    Verify MRCON CUI|LUI|SUI in MRXW.$f CUI|LUI|SUI"
        setenv LAT $f
        $PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[3]|$_[5]\n" if $_[1] eq "'$f'" \
            && $_[6] !~ /^(=|<=|>=|\+|\+\+|\+\+\+|\+\+\+\+|<|>)$/;' $mrcon |\
            sort -u >! mrx.tmp2.$$
        $PATH_TO_PERL -ne 'split /\|/; print "$_[2]|$_[3]|$_[4]\n";' ${mrxw}.$f |\
            sort -u >! mrx.tmp1.$$
    
        set ct=`comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | wc -l`
        if ($ct != 0) then
            echo "ERROR: CUI|LUI|SUI in MRCON not in MRXW.$f"
            comm -13 mrx.tmp1.$$ mrx.tmp2.$$ | grep -v $null_lui | sed 's/^/  /'
        endif
        echo "    Verify MRXW.$f CUI|LUI|SUI in MRCON CUI|LUI|SUI"
        set ct=`comm -23 mrx.tmp1.$$ mrx.tmp2.$$ | wc -l`
            if ($ct != 0) then
            echo "ERROR: CUI|LUI|SUI in MRXW.$f not in MRCON"
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
    echo "    Verify sort order for ${mrxw}.$f"
    sort -c -u ${mrxw}.$f >> /dev/null
    if ($status != 0) then
        echo "ERROR: ${mrxw}.$f has incorrect sort order"
    endif
    end
    rm -f mrx.lats.$$

echo "Remove MR{CON,SO}.uis files"
/bin/rm -f MRCON.uis.*.$$
/bin/rm -f MRSO.uis.*.$$

