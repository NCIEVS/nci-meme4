#!/bin/csh -f

#
# Makes deletedcui.txt from MRCUI.RRF, MRCONSO.RRF
#  Run in directory containing files
#

if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

set s1=0
set s2=0

if ($#argv != 2) then
    echo "Usage: $0 <old> <new>"
    exit 1
endif

if ($?MRD_HOME == 0) then
    echo '$MRD_HOME must be set'
    exit 1
endif

set new=$2
set old=$1

echo "-------------------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "-------------------------------------------------------------------------"
echo "new: $new"
echo "old: $old"

#
# Get deleted CUIs
#
echo "    Get deleted CUIs ... `/bin/date`"
/bin/cut -d\| -f 1 $new/MRCUI.RRF >&! mrcui.cuis
/bin/cut -d\| -f 1 $new/MRCONSO.RRF >&! mrconso.new.cuis
/usr/bin/comm -23 mrcui.cuis mrconso.new.cuis >&! oldcuis.txt

#
# Acquire CUI-STR map (no TS,STT,ISPREF) - only for older installations that do not compute TS,STT,ISPREF
#
echo "    Acquire CUI-STR map ... `/bin/date`"
#/bin/cut -d\| -f 1,15 $old/MRCONSO.RRF |\
#  perl -e 'while(<>) { @f = split /\|/; \   
#    if ($f[0] ne $pc || $pc eq "") {print "$f[0]|$f[1]"; } \
#    $pc = $f[0]; }; ' >&! mrconso.old.cuis

#
# Acquire CUI-STR map (no TS,STT,ISPREF)
#
/bin/awk -F\| '$2=="ENG" && $3=="P" && $5=="PF" && $7=="Y" {print $0}' $old/MRCONSO.RRF |\
  /bin/cut -d\| -f 1,15 >&! mrconso.old.cuis

#
# Acquire CUI-STR map for SPA, add section for each additional language
#
/bin/awk -F\| '$2=="SPA" && $3=="P" && $5=="PF" && $7=="Y" {print $0}' $old/MRCONSO.RRF |\
  /bin/cut -d\| -f 1,15 |\
    /usr/bin/join -t\| -j 1 -v 2 -o 2.1 2.2 mrconso.old.cuis -  >>& mrconso.old.cuis

#
# Add entries to $MRD_HOME/etc/deletedcui.txt
#
echo "    Add $MRD_HOME/etc/deletedcui.txt entries ... `/bin/date`"
/bin/cp -f $MRD_HOME/etc/deletedcui.txt $MRD_HOME/etc/deletedcui.txt.bak

# not needed - already sorted
#/bin/sort -u -o oldcuis.txt oldcuis.txt

/bin/sort -u -o mrconso.old.cuis mrconso.old.cuis

#
# Check if no STR for some CUIs
#
set ct=`/usr/bin/join -t\| -j 1 -v 2 -o 2.1 mrconso.old.cuis oldcuis.txt | /usr/bin/join -t\| -j 1 -v 2 -o 2.1 $MRD_HOME/etc/deletedcui.txt - | wc -l`
if ($ct != 0) then
    echo "ERROR: No STR found for these CUIs"
    /usr/bin/join -t\| -j 1 -v 2 -o 2.1 mrconso.old.cuis oldcuis.txt |\
      /usr/bin/join -t\| -j 1 -v 2 -o 2.1 $MRD_HOME/etc/deletedcui.txt -
    exit 1
    #
    # One way to address this problem is to just use the id for both
    #
    /usr/bin/join -t\| -j 1 -v 2 -o 2.1 mrconso.old.cuis oldcuis.txt |\
      /usr/bin/join -t\| -j 1 -v 2 -o 2.1 $MRD_HOME/etc/deletedcui.txt - |\
      awk '{print $0"|"$0}' >> $MRD_HOME/etc/deletedcui.txt 
    /bin/sort -u -o $MRD_HOME/etc/deletedcui.txt $MRD_HOME/etc/deletedcui.txt

endif

#
# Check if any new MRCONSO entries are already in deletedcui.txt
#
set ct=`/usr/bin/join -t\| -j 1 -o 1.1 $MRD_HOME/etc/deletedcui.txt mrconso.new.cuis | wc -l`
if ($ct != 0) then
    echo "WARNING: Resurrected CUIs"
    echo "  CUIs in $MRD_HOME/etc/deletedcui.txt  and $new/MRCONSO.RRF"
    echo "  deleted from $MRD_HOME/etc/deletedcui.txt"
    /usr/bin/join -t\| -j 1 -o 1.1 $MRD_HOME/etc/deletedcui.txt mrconso.new.cuis | sort -u
    /usr/bin/join -t\| -v 1 -j 1 $MRD_HOME/etc/deletedcui.txt mrconso.new.cuis > $MRD_HOME/etc/deletedcui.txt.tmp
    /bin/mv -f $MRD_HOME/etc/deletedcui.txt.tmp $MRD_HOME/etc/deletedcui.txt
endif

set ct=`/usr/bin/join -t\| -j 1 -o 1.1 1.2 mrconso.old.cuis oldcuis.txt | wc -l`
echo "      $ct new entries"

/usr/bin/join -t\| -j 1 -o 1.1 1.2 mrconso.old.cuis oldcuis.txt \
  >> $MRD_HOME/etc/deletedcui.txt 
/bin/sort -u -o $MRD_HOME/etc/deletedcui.txt $MRD_HOME/etc/deletedcui.txt

#
# Cleanup
#
echo "    Cleanup ...`/bin/date`"
/bin/rm -f mrcui.cuis mrconso.new.cuis mrconso.old.cuis oldcuis.txt

echo "-------------------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "-------------------------------------------------------------------------"

