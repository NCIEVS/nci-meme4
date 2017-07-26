set orf_dir = $dir
set rrf_dir = $dir/../META
set ef = 0

alias awk gawk

echo "----------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "----------------------------------------------"

echo "  Verify Composing ORF files from $rrf_dir... `/bin/date`"

cp $rrf_dir/AMBIGLUI.RRF AMBIG.LUI.$$
cp $rrf_dir/AMBIGSUI.RRF AMBIG.SUI.$$

awk -F\| '$23=="ATX" {print $1$7"|"$7"|"$2"|"$13"|"$17"|"}' $rrf_dir/MRMAP.RRF | sort -u >! MRATX.tmp.$$
awk -F\| '$23=="ATX" {print $1$7"|"$1"|"}' $rrf_dir/MRMAP.RRF | sort -u >! MRATX.translation.$$
cat $rrf_dir/MRSAT.RRF | grep TORSAB >! torsab.$$
join -t\| -1 1 -2 2 -o 2.1 1.11 torsab.$$ MRATX.translation.$$ | sort -u  >! MRATX.join.$$
join -a 2 -t\| -1 1 -2 1 -o 2.2 1.2 2.4 2.5 2.6 MRATX.join.$$ MRATX.tmp.$$ | sort -u >! MRATX.$$
rm -f MRATX.tmp.$$ MRATX.translation.$$ torsab.$$ MRATX.join.$$

cat $rrf_dir/MRCOC.RRF | cut -d\| -f1,3,5,6,7,8,9 | sort -u >! MRCOC.$$

awk -F\| '{print $1"|"$2"|"$3"|"$6"|"$7"|"}' $rrf_dir/MRCUI.RRF | sort -u >! MRCUI.$$

awk -F\| '{print $1"|"$5"|"$6"|"}' $rrf_dir/MRDEF.RRF | sort -u >! MRDEF.$$
cp $rrf_dir/MRRANK.RRF MRRANK.$$

cat $rrf_dir/MRREL.RRF | awk -F\| '{print $1"|"$4"|"$5"|"$8"|"$11"|"$12"|"$16"|"}' | sort -u >! MRREL.$$
awk -F\| '{print $1"|"$2"|"$3"|"$4"|"$5"|"$6"|"$7"|"$8"|"$9"|"$10"|"$11"|"$12"|"$13"|"$14"|"$17"|"$18"|"$20"|"$21"|"$22"|"$23"|"}' $rrf_dir/MRSAB.RRF | sort -u >! MRSAB.$$

cat $rrf_dir/MRSAT.RRF | perl -ne 'split /\|/; print "$_[0]|$_[1]|$_[2]|$_[5]|$_[8]|$_[9]|$_[10]|\n" if ($_[3] !~ /^R/ && $_[8] ne "CV_MEMBER");'| sort -u >! MRSAT.$$

awk -F\| '{print $1"|"$2"|"$4"|"}' $rrf_dir/MRSTY.RRF | sort -u >! MRSTY.$$

cp $rrf_dir/MRXNS_ENG.RRF MRXNS.ENG.$$
cp $rrf_dir/MRXNW_ENG.RRF MRXNW.ENG.$$
cp $rrf_dir/MRXW_BAQ.RRF MRXW.BAQ.$$
cp $rrf_dir/MRXW_CZE.RRF MRXW.CZE.$$
cp $rrf_dir/MRXW_DAN.RRF MRXW.DAN.$$
cp $rrf_dir/MRXW_DUT.RRF MRXW.DUT.$$
cp $rrf_dir/MRXW_ENG.RRF MRXW.ENG.$$
cp $rrf_dir/MRXW_FIN.RRF MRXW.FIN.$$
cp $rrf_dir/MRXW_FRE.RRF MRXW.FRE.$$
cp $rrf_dir/MRXW_GER.RRF MRXW.GER.$$
cp $rrf_dir/MRXW_HUN.RRF MRXW.HUN.$$
cp $rrf_dir/MRXW_ITA.RRF MRXW.ITA.$$
cp $rrf_dir/MRXW_JPN.RRF MRXW.JPN.$$
cp $rrf_dir/MRXW_NOR.RRF MRXW.NOR.$$
cp $rrf_dir/MRXW_POR.RRF MRXW.POR.$$
cp $rrf_dir/MRXW_RUS.RRF MRXW.RUS.$$
cp $rrf_dir/MRXW_SPA.RRF MRXW.SPA.$$
cp $rrf_dir/MRXW_SWE.RRF MRXW.SWE.$$

awk -F\| '{print $1"|"$2"|"$3"|"$4"|"substr($5,1,1)"|"$6"|"$15"|"}' $rrf_dir/MRCONSO.RRF | sort -u  >! MRCON.s.$$

$PATH_TO_PERL -ne 'chop; split /\|/; print "$_[0]|$_[1]|".(uc($_[2]))."|$_[3]|".(substr($_[4],0,1))."|$_[5]|$_[6]|\n"' $orf_dir/MRCON | sort -u >! $orf_dir/MRCON.s

awk -F\| '{print $1"|"$4"|"$6"|"$12"|"$13"|"$14"|"$16"|"}' $rrf_dir/MRCONSO.RRF | sort -u >! MRSO.$$

cat $rrf_dir/MRCXT.RRF | awk -F\| '{print $1"|"$2"|"$4"|"$5"|"$6"|"$7"|"$8"|"$9"|"$10"|"$12"|"$13"|"$14"|"}' | sort -u >! MRCXT.$$

#CHANGE directory
cp $rrf_dir/CHANGE/DELETEDCUI.RRF DELETED.CUI.CHANGE.$$
cp $rrf_dir/CHANGE/DELETEDLUI.RRF DELETED.LUI.CHANGE.$$
cp $rrf_dir/CHANGE/DELETEDSUI.RRF DELETED.SUI.CHANGE.$$
cp $rrf_dir/CHANGE/MERGEDCUI.RRF MERGED.CUI.CHANGE.$$
cp $rrf_dir/CHANGE/MERGEDLUI.RRF MERGED.LUI.CHANGE.$$

echo "  Verify Compare composed ORF files with $orf_dir... `/bin/date`"

foreach f1 (`ls *.$$ | grep -v CHANGE`)
  set base_file = $f1:r
  set f2 = $orf_dir/$base_file
   echo "  Verify Compare composed ORF files $f1 $f2"
  if ($base_file == "MRCXT") then
    join -v 1 -v 2 -t '\n' $f1 $f2 >! $base_file.diff.$$
  endif
   if ($base_file == "MRSAB") then
         awk -F\| '{print $1"|"$2"|"$3"|"$4"|"$5"|"$6"|"$7"|"$8"|"$9"|"$10"|"$11"|"$12"|"$13"|"$14"|"$17"|"$18"|"$20"|"$21"|"$22"|"$23"|"}' $f2 | sort -u >! MRSAB_ORF.$$
     diff $f1 MRSAB_ORF.$$ >! $base_file.diff.$$
  else
    diff $f1 $f2 >! $base_file.diff.$$
  endif

  set ct=(`wc -l $base_file.diff.$$`)
  if ($ct[1] != 0) then
    echo "ERROR: Differences detected between $f1 $f2"
    echo "Please check the file $base_file.diff.$$"
    set ef = 1
  endif
end

foreach f1 (`ls *.CHANGE.$$`)
  set base_file1 = $f1:r
  set base_file = $base_file1:r
  set f2 = $orf_dir/CHANGE/$base_file
  echo "  Verify Compare composed ORF CHANGE files $f1 $f2"
  diff $f1 $f2 >! $base_file.diff.$$
  set ct=(`wc -l $base_file.diff.$$`)
  if ($ct[1] != 0) then
    echo "ERROR: Differences detected between $f1 $f2"
    echo "Please check the file $base_file.diff.$$"
    set ef = 1
  endif
end

if ($ef == 0) then
#  echo "$0 passed."
  rm -f *.$$ $orf_dir/*.$$
else
#  echo "$0 failed."
endif

#echo "----------------------------------------------"
#echo "Finished $0 ... `/bin/date`"
#echo "----------------------------------------------"
  
