#!/bin/csh -f

#set dir=/usr/d5/MRD/200509
set dir=$1

# cut MRCOC down to ORF
# CUI1,AUI1,CUI2,AUI2,SAB,COT,COF,COA,CVF to
# CUI1,CUI2,SAB,COT,COF,COA
/bin/cut -d\| -f -1,3,5-8,10 $dir/MRCOC.RRF >&! $dir/METAO/MRCOC

/bin/cut -d\| -f 1 $dir/METAO/MRSTY | /bin/sort -T . -u -o $dir/METAO/cuis
/usr/bin/join -t\| -j 1 -o 1.1 1.2 1.3 1.4 1.5 1.6 1.7 \
  $dir/METAO/MRCOC $dir/METAO/cuis |\
  /bin/sort -T . -t\| -k 2,2 -o $dir/METAO/mrcoc
/usr/bin/join -t\| -j1 2 -j2 1 -o 1.1 1.2 1.3 1.4 1.5 1.6 1.7 \
  $dir/METAO/mrcoc $dir/METAO/cuis |\
  /bin/sort -T . -u -o $dir/METAO/mrcoc.2
/bin/mv -f $dir/METAO/mrcoc.2 $dir/METAO/MRCOC
/bin/rm -f $dir/METAO/mrcoc $dir/METAO/cuis
