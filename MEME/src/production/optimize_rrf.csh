#!/bin/csh -f
#
# Script:    optimize_rrf.csh
# Author:    BAC
#
# Takes RRF as input and produces output that is optimized
# according to 20100624-035950.
# MRCOC – Blank CUI1,AUI1 fields where same as previous line
# MRHIER – Blank CUI,AUI,PTR, SAB,RELA and first 2 parts of PTR where same as previous line; PAUI blank
# MRREL - Blank CUI1,AUI1,SAB if same as prev line, blank SL always
# MRSAT - Blank CUI, AUI, STYPE, SAB if same as previous row
# MRSTY - Keep only CUI,TUI, CVF. Blank other fields
# (other optimizations may be added later)
#
# CHANGES
# 08/2010 BAC (1-RDJQ1): first version
#
# Version Information

set release=1
set version=0.1
set authority=BAC
set version_date="08/02/2010"

source $ENV_HOME/bin/env.csh

#
# Set Environment, Aliases
#
set usage="Usage: $0 <in dir> <out dir>"

#
# Parse arguments
#
if ($#argv != 2) then
	echo "ERROR: Wrong number of arguments"
	echo "$usage"
	exit 1
endif

set in = $1
set out = $2

echo "-----------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "-----------------------------------------------------"
echo ""

#release.dat
ln -s $in/release.dat $out/

#AMBIGLUI.RRF, AMBIGSUI.RRF
ln -s $in/AMBIGLUI.RRF $out/
ln -s $in/AMBIGSUI.RRF $out/

#CHANGE
ln -s $in/CHANGE $out/

#MRAUI.RRF
ln -s $in/MRAUI.RRF $out/

#MRCOC.RRF
echo "    Preparing MRCOC.RRF"
$PATH_TO_PERL -ne 'split /\|/; \
   if ($pk eq "$_[0]$_[1]") \
      { $_[0]=""; $_[1]=""; } \
   else {$pk = "$_[0]$_[1]"}; \
   print join "|", @_;' $in/MRCOC.RRF >! $out/MRCOC.RRF

#MRCOLS.RRF
ln -s $in/MRCOLS.RRF $out/

#MRCONSO.RRF
ln -s $in/MRCONSO.RRF $out/

#MRCUI.RRF
ln -s $in/MRCUI.RRF $out/

#MRDEF.RRF
ln -s $in/MRDEF.RRF $out/

#MRDOC.RRF
ln -s $in/MRDOC.RRF $out/

#MRFILES.RRF
ln -s $in/MRFILES.RRF $out/

#MRHIER.RRF
# Make sure to do the prev thing with PTRs only if the PTR has at least 2 parts!
echo "    Preparing MRHIER.RRF"
$PATH_TO_PERL -ne 'split /\|/; \
  $_[3] = ""; @ptr = split /\./, $_[6]; \
  if ($pk eq "$_[0]$_[1]$_[4]$_[5]$ptr[0]$ptr[1]" && scalar(@ptr)>2) \
    { $_[0]=""; $_[1]=""; $_[4]=""; $_[5]=""; $ptr[0]="";$ptr[1]= ""} \
    else {$pk = "$_[0]$_[1]$_[4]$_[5]$ptr[0]$ptr[1]"}; \
    $_[6] = join ".", @ptr; print join "|", @_;' \
    $in/MRHIER.RRF >! $out/MRHIER.RRF

#MRHIST.RRF
ln -s $in/MRHIST.RRF $out/

#MRMAP.RRF
ln -s $in/MRMAP.RRF $out/

#MRRANK.RRF
ln -s $in/MRRANK.RRF $out/

#MRREL.RRF
echo "    Preparing MRREL.RRF"
$PATH_TO_PERL -ne 'split /\|/;$_[11] = ""; \
  if ($pk eq "$_[0]$_[1]$_[2]$_[6]$_[10]") { \
    $_[0]=""; $_[1]=""; $_[2]=""; $_[6]=""; $_[10]=""; } \
  else {$pk = "$_[0]$_[1]$_[2]$_[6]$_[10]"}; \
  print join "|", @_;' $in/MRREL.RRF >! $out/MRREL.RRF

#MRSAB.RRF
ln -s $in/MRSAB.RRF $out/

#MRSAT.RRF
echo "    Preparing MRSAT.RRF"
$PATH_TO_PERL -ne 'split /\|/; $_[1] = ""; \
  $_[2] = ""; $_[5] = ""; \
  if ($pk eq "$_[0]$_[3]$_[4]$_[9]") { \
    $_[0]=""; $_[3]=""; $_[4] ="";$_[9]=""; } \
  else {$pk = "$_[0]$_[3]$_[4]$_[9]"}; \
  print join "|", @_;' $in/MRSAT.RRF >! $out/MRSAT.RRF

#MRSMAP.RRF
ln -s $in/MRSMAP.RRF $out/

#MRSTY.RRF
echo "    Preparing MRSTY.RRF"
$PATH_TO_PERL -ne 'split /\|/; $_[2] = ""; \
  $_[3] = ""; $_[4]= ""; $_[5]=""; \
  print join "|", @_;' $in/MRSTY.RRF >! $out/MRSTY.RRF

#MRXNS_ENG.RRF
ln -s $in/MRXNS_ENG.RRF $out/

#MRXNW_ENG.RRF
ln -s $in/MRXNW_ENG.RRF $out/

#MRXW*
ln -s $in/MRXW* $out/

#
# Finished
#
echo "   Linking other files"
echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
