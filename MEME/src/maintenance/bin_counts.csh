
#!/bin/csh -f
#
# File:    bin_counts.csh
# Author:  Priya Mathur
#
#          This script runs matrix Init and partitions to regenerate
#          ME bins and some selected AH/QA bins. This script should
#          be called before insertion of a source.

###################################################################
#
# Configuration
#
###################################################################


set db=$1

#
#Set email flags
#
set subject_flag='-subject="Bin counts before Insertion - '$db'"'
set to=nlmutsdev@mail.nih.gov
set from=mpriya@mail.nih.gov
if ($?to == 1) then
   set to_from_flags="-to $to -from $from"
else
   set to_from_flags=
endif


if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

###################################################################
# Get username/password
###################################################################
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`


#################################################################
#
#  Start RECIPE
#
#################################################################

echo "--------------------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
"/umls_s/umls_apps_linux_prod/meme/bin/bin_counts.csh" [readonly] line 1 of 102 --0%-- col 1
endif
echo ""

#
# Run matrix initializer
#
echo "    Run matrix initializer ...`/bin/date`"
$MEME_HOME/bin/matrixinit.pl -w 0 -I $db >&! matrixinit.log
if ($status != 0) then
    echo "Error initializing matrix"
    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message="Error initializing matrix"'
    exit 1
endif


#
# Repartition MID
#
echo "    Repartition MID ...`/bin/date`"
$EMS_HOME/bin/chemconcepts.pl -d $db
$EMS_HOME/bin/batchpartition.pl -d $db >&! batch.partition.log



#
# Generate the needsrel bin
#
echo "    Generating needsrel AH bin ...`/bin/date`"
$EMS_HOME/bin/batchgenerate.pl -t AH -b needsrel -d $db >&! generateNeedsrelBin.log

# wait for a while
foreach num (9 8 7 6 5 4 3 2 1)
    echo "Starting in $num seconds..."
    sleep 1
end


#
# Generate the ambig_no_pn bin
#
echo "    Generating ambig_no_pn AH bin ...`/bin/date`"
$EMS_HOME/bin/batchgenerate.pl -t QA -b ambig_no_pn -d $db >&! generateAmbigNoPn.log


if ($?to == 1)  eval $EMS_HOME/bin/mail-bin-counts.pl -d $db

echo "--------------------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "--------------------------------------------------------------"
