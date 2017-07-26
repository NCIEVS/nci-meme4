#!/bin/csh -f
# 
# Convert MRCUI.RRF and MRAUI.RRF into NCIMEME_${release}_history.txt 
# Represent merges, splits, and retires 
#
# conceptcode|conceptname|editaction|editdate|referencecode|referencename
#
# Usage:
#     make_history.csh <editdate> <previous> <current>
#
# Options:
#     <editdate>: Required (dd-mon-yy)
#     <previous>: Previous Release Name
#     <current>     : Current Release Name

if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

if ($#argv != 3) then
    echo "Usage: $0 <editdate> <previous> <current>" 
    echo "	editdate : dd-mon-yy"
    exit 1
endif

if ($?MRD_HOME == 0) then
    echo '$MRD_HOME must be set'
    exit 1
endif

if (! -e MRCUI.RRF) then
	echo "ERROR: required file MRCUI.RRF cannot be found"
	exit 1
endif

if (! -e MRAUI.RRF) then
	echo "ERROR: required file MRAUI.RRF cannot be found"
	exit 1
endif

set editdate=$1
set previous_release=$2
set release=$3
 
#
# MRCUI SY
#	C0008429|$previous_release|SY|||C0728810|Y|
#
# history file equivalent (without names)
#	C0008429|C0008429|merge|$editdate|C0728810|C0728810|
#	C0728810|C0728810|merge|$editdate|C0728810|C0728810|
#
/bin/grep $previous_release'|SY|' MRCUI.RRF |\
	$PATH_TO_PERL -ne 'chop; split /\|/; \
		  print "$_[0]|$_[0]|merge|'$editdate'|$_[5]|$_[5]\n"; \
		  print "$_[5]|$_[5]|merge|'$editdate'|$_[5]|$_[5]\n";' \
	>! NCIMEME_${release}_history.txt
 
#
# MRAUI - MRCONSO
#	A0027564|C0327123|$release|||move|A0027564|CL336379|Y|
#       C0327123|ENG|X|L1695191|X|S0024526|X|A0024710||||NCBI|SCN|38771|Gopherus|0|N||
#
# history file equivalent
#	C0327123|C0327123|split|$editdate|C0327123|C0327123|
#	C0327123|C0327123|split|$editdate|CL336379|CL336379|
#

/bin/sort -u -o NCIMEME_${release}_history.txt{,}

/bin/grep '|'$release'|' MRAUI.RRF | cut -d\| -f 2,8 | /bin/sort -u |\
	 /usr/bin/join -t\| -j 1 -a 1 -o 1.1 1.2 2.1 - MRCONSO.RRF |\
	 /bin/sort -u |\
	 /usr/bin/join -t\| -j 1 -v 1 -o 1.1 1.2 1.3 - NCIMEME_${release}_history.txt |\
	$PATH_TO_PERL -ne 'chop; split /\|/; \
		  print "$_[0]|$_[0]|split|'$editdate'|$_[0]|$_[0]\n" if scalar(@_) == 3; \
		  print "$_[0]|$_[0]|split|'$editdate'|$_[1]|$_[1]\n";' \
	>> NCIMEME_${release}_history.txt
  
#
# MRCUI 
#	C0170529|$previous_release|||||N|
#
# history file equivalent (without names)
#	C0170529|C0170529|retire|$editdate|||
#
/bin/grep $previous_release'|' MRCUI.RRF | sort -u |\
	$PATH_TO_PERL -ne 'chop; split /\|/; \
		  print "$_[0]|$_[0]|retire|'$editdate'||\n";' \
	>> NCIMEME_${release}_history.txt
 
/bin/sort -u -o NCIMEME_${release}_history.txt{,}

#
# Get Concept Names
#
#
/bin/awk -F\| '$2 == "ENG" && $3=="P" && $5=="PF" && $7=="Y" {print $1"|"$15}' MRCONSO.RRF \
        >! mrconso.cuis

cat $MRD_HOME/etc/deletedcui.txt >>  mrconso.cuis

/bin/sort -o  mrconso.cuis{,}

/usr/bin/join -t\| -a 1 -1 2 -2 1 -o 1.1 2.2 1.3 1.4 1.5 1.6 \
              NCIMEME_${release}_history.txt mrconso.cuis \
	>! NCIMEME_${release}_history.tmp

/bin/sort -t\| -k6,6 -o NCIMEME_${release}_history.tmp{,}

/usr/bin/join -t\| -a 1 -1 6 -2 1 -o 1.1 1.2 1.3 1.4 1.5 2.2 \
	NCIMEME_${release}_history.tmp mrconso.cuis >!  NCIMEME_${release}_history.txt

/bin/rm -f NCIMEME_${release}_history.tmp mrconso.cuis

#
# Sort and prep final file
#
/bin/sort -u -o NCIMEME_${release}_history.txt{,}
