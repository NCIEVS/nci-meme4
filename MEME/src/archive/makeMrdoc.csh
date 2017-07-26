#!/bin/csh -f
#
# makes an ORF MRDOC file from the various other files
#
# input: <ORF dir>
# output: MRDOC file
#
if ($#argv != 1) then
    echo "Usage: $0 <dir>"
    exit 1
endif

set dir=$1
if (! (-e $dir/MRCON)) then
    echo "$dir/MRCON must exist! "
    exit 1
endif
if (! (-e $dir/MRSO)) then
    echo "$dir/MRSO must exist! "
    exit 1
endif

echo "------------------------------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------------------------------"
echo "dir: $dir"
echo ""

/bin/rm -rf $dir/MRDOC
touch $dir/MRDOC

#
# ATN|expanded_form
# CUI,LUI,SUI,CODE,ATN,SAB,ATV
#
echo "    Acquire ATN|expanded from MRSAT ...`/bin/date`"
cut -d\| -f 5 $dir/MRSAT | sort -u |\
   perl -ne 'chop; print "ATN|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# COA|expanded_form
# CUI1,CUI2,SOC,COT,COF,COA
#   QA=CT
echo "    Acquire COA|expanded from MRCOC ...`/bin/date`"
cut -d\| -f 6 $dir/MRCOC | \
   perl -e 'while (<>) { chop; @f = split /,/; foreach $f (@f) {\
    ($coa) = split /=/, $f; $coas{$coa}=1; } } \
    foreach $coa (sort keys %coas) { \
      print "COA|$coa|expanded_form|$coa|\n";}' |\
   grep -v '<>'  >> $dir/MRDOC

#
# COT|expanded_form
# CUI1,CUI2,SOC,COT,COF,COA
#
echo "    Acquire COT|expanded from MRCOC ...`/bin/date`"
cut -d\| -f 4 $dir/MRCOC | sort -u |\
   perl -ne 'chop; print "COT|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# CXTY|expanded_form
# VCUI,RCUI,VSAB,RSAB,SON,SF,SVER,MSTART,MEND,IMETA,RMETA,SLC,SCC,SRL,TFR,CFR,CXTY,TTYL,ATNL,LAT,CENC,CURVER,SABIN
#
echo "    Acquire CXTY|expanded from MRSAB ...`/bin/date`"
cut -d\| -f 17 $dir/MRSAB | sort -u |\
   perl -ne 'chop; print "CXTY|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# No FROMTYPE in ORF
#

#
# LAT|expanded_form
# VCUI,RCUI,VSAB,RSAB,SON,SF,SVER,MSTART,MEND,IMETA,RMETA,SLC,SCC,SRL,TFR,CFR,CXTY,TTYL,ATNL,LAT,CENC,CURVER,SABIN
#
echo "    Acquire LAT|expanded from MRSAB ...`/bin/date`"
cut -d\| -f 20 $dir/MRSAB | sort -u |\
   perl -ne 'chop; print "LAT|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# No MAPATN in ORF
#

#
# RELA|expanded_form
# CUI1,REL,CUI2,RELA,SAB,SL,MG
#
echo "    Acquire RELA|expanded from MRREL ...`/bin/date`"
cut -d\| -f 4 $dir/MRREL | sort -u |\
   perl -ne 'chop; print "RELA|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# RELA|rela_inverse
#
echo "    Acquire RELA|rela_inverse from MRDOC ...`/bin/date`"
egrep '^RELA\|' $dir/MRDOC | cut -d\| -f 2 |\
   perl -ne 'chop; print "RELA|$_|rela_inverse|Not Available|\n";' >> $dir/MRDOC


#
# No RELA|snomedct_rela_mapping in ORF
#

#
# REL|expanded_form
# CUI1,REL,CUI2,RELA,SAB,SL,MG
#
echo "    Acquire REL|expanded from MRREL ...`/bin/date`"
cut -d\| -f 2 $dir/MRREL | sort -u |\
   perl -ne 'chop; print "REL|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# REL|rel_inverse
#
echo "    Acquire REL|rela_inverse from MRDOC ...`/bin/date`"
egrep '^REL\|' $dir/MRDOC | cut -d\| -f 2 |\
   perl -ne 'chop; print "REL|$_|rel_inverse|Not Available|\n";' >> $dir/MRDOC


#
# No REL|snomedct_rel_mapping in ORF
#

#
# STT|expanded_form
# CUI,LAT,TS,LUI,STT,SUI,STR,LRL
#
echo "    Acquire STT|expanded from MRCON ...`/bin/date`"
cut -d\| -f 5 $dir/MRCON | sort -u |\
   perl -ne 'chop; print "STT|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# No STYPE1|expanded_form in ORF
# No STYPE2|expanded_form in ORF
# No STYPE|expanded_form in ORF
# No SUPPRESS|expanded_form in ORF
# No TOTYPE|expanded_form in ORF
#

#
# TS|expanded_form
# CUI,LAT,TS,LUI,STT,SUI,STR,LRL
#
echo "    Acquire TS|expanded from MRCON ...`/bin/date`"
cut -d\| -f 3 $dir/MRCON | sort -u |\
   perl -ne 'chop; print "TS|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# TTY|expanded_form
# RANK,SAB,TTY,SUPRES
#
echo "    Acquire TTY|expanded from MRRANK ...`/bin/date`"
cut -d\| -f 3 $dir/MRRANK | sort -u |\
   perl -ne 'chop; print "TTY|$_|expanded_form|$_|\n";' >> $dir/MRDOC

#
# TTY|tty_class
#
echo "    Acquire TTY|tty_class from MRDOC  ...`/bin/date`"
grep '|TTY|' $dir/MRDOC | cut -d\| -f 2 |\
   perl -ne 'chop; print "REL|$_|tty_class|Not Available|\n";' >> $dir/MRDOC

echo "    Sorting final MRDOC ...`/bin/date`"
/bin/sort -u -o $dir/MRDOC{,}

echo "------------------------------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------------------------------"


