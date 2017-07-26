#!/bin/csh -f
#
# File          insert_atom_notes.csh
# Written by    Priya Mathur
#
# This script downloads the ATOM_NOTE data from the database and then loads them as attributes.
#
# Dependencies:
#   requires MEME_HOME to be set
# CHANGES
# 05/26/2015: Added
#

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

if ($?MEME_HOME != 1) then
    echo '$MEME_HOME must be set'
#!/bin/csh -f
#
# File          insert_atom_notes.csh
# Written by    Priya Mathur
#
# This script downloads the ATOM_NOTE data from the database and then loads them as attributes.
#
# Dependencies:
#   requires MEME_HOME to be set
# CHANGES
# 05/26/2015: Added
#

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

if ($?MEME_HOME != 1) then
    echo '$MEME_HOME must be set'
    exit 1
endif

if ($?ORACLE_HOME != 1) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

#
# Environment
#
set db = $1
set new_source = $2
set authority = $3
set dir = $4
set work_id=$5

#
# Parse arguments
#

if ($#argv == 0) then
    echo "Error: Bad argument"
    echo "Usage: $0 <database> <new_source> <authority> <dir> <work_id>"
    exit 1
endif


set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "MEME_HOME:      $MEME_HOME"
echo "ORACLE_HOME:    $ORACLE_HOME"
echo "database:       $db"
echo "work_id:        $work_id"
echo ""
set start_t=`$PATH_TO_PERL -e 'print time'`


#
# Create patch directory
#

/bin/mkdir $dir/patch_atom_notes

cd patch_atom_notes

#
# Write out atom_notes for attributes
#
set query="select attr_string from tpm_download_atom_notes"
$EMS_HOME/bin/dump_table.pl -u $user -d $db -q "$query" \
>! attributes.src
echo "    Finished dumping atom notes data as attributes ...`/bin/date`"



$MEME_HOME/bin/load_atom_notes.csh $db $new_source $authority >! atom_notes.log


echo "--------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "--------------------------------------------------------------"