#!/bin/csh -f
# Script to compare two release classes_atoms.src files for all TTYs, e.g.
# compare-versions.s 10 /.../MSH2006_2005_11_15 /.../MSH2006_2006_02_06

if ($#argv != 3) then 
	echo "Usage:  compare-versions.s <sample#> <full-path to old classes_atoms.src> <full-path to new classes_atoms.src>"
	exit()  
endif

echo "`date` $0 start"
echo ""
set od="$2"
set nd="$3"
set of="$od"/classes_atoms.src
set nf="$nd"/classes_atoms.src
echo "Old File = " "$of"
nawk -F\| 'BEGIN {OFS="|"} { n=split($3,t,"/"); print t[2],$4,$8,$9,$10,$11,$12 }' "$of" > z$$of
tallyfield '$1' z$$of
echo ""
echo "New File = " "$nf"
nawk -F\| 'BEGIN {OFS="|"} { n=split($3,t,"/"); print t[2],$4,$8,$9,$10,$11,$12 }' "$nf" > z$$nf
tallyfield '$1' z$$nf
tallyfield '$1' z$$nf | nawk '{ if ($1 != "~TOTAL") print $1 }' > z$$nt
echo ""

foreach i (`cat z$$nt`)
	echo "		$i's"
	# Make old and new files for comparisons from meshrjoin files
	nawk -F\| 'BEGIN { OFS = "|" } { if ( $1 == "'$i'" ) { $1 = ""; print $0 }}' z$$of | /bin/sort -t\| > z$$srcold
	nawk -F\| 'BEGIN { OFS = "|" } { if ( $1 == "'$i'" ) { $1 = ""; print $0 }}' z$$nf | /bin/sort -t\| > z$$srcnew
	echo "Total Old $i's"
	wc -l z$$srcold
	echo "Total New $i's"
	wc -l z$$srcnew
	
	echo "Identical $i's"
	/bin/comm -12 z$$srcold z$$srcnew | wc -l
	
	# Compute how many new $i TTYs there are
	echo "$i's That Are New"
	/bin/comm -13 z$$srcold z$$srcnew > z$$newsrc
	set n = `cat z$$newsrc | wc -l`
	echo $n
	if ($n <= $1) then
		cat z$$newsrc
	else
		nawk 'BEGIN { r = '$n'/'$1'; c = 0 } { if (NR == int(c+r)) { print $0; c += r } }' z$$newsrc
	endif
	
	# Compute how many 'disappeared $i TTYs there are
	echo "$i's That Are Gone"
	/bin/comm -23 z$$srcold z$$srcnew > z$$oldsrc
	set n = `cat z$$oldsrc | wc -l`
	echo $n
	if ($n <= $1) then
		cat z$$oldsrc
	else
		nawk 'BEGIN { r = '$n'/'$1'; c = 0 } { if (NR == int(c+r)) { print $0; c += r } }' z$$oldsrc
	endif
	echo ""
end

echo "`date` $0 end"

rm -f z$$*
