#!/bin/csh -f
#
# File:   preproduction.csh
# Usage:  preproduction.csh <db> <startop 1,2,3>
# Author: Joanne Wong (wongjf@mail.nih.gov)
#
# Remarks: Perform pre_production tasks.
#
# Changes
# 04/15/2016 JFW: Initial version.
#                 Section #s have not been implemented yet,
#                 so feel free to use 0 in calling the script.
# 05/20/2016 JFW: Tested version with bug fixes. Still no section #s.
# 06/01/2016 JFW: Added missing steps (cui history, check for null ATUIs)
#

# required ENV variables for this script to work
set required_vars = ("release" "old_release" "MEME_HOME" "MRD_HOME" "ORACLE_HOME" "ENV_HOME")
foreach rv ($required_vars)
    if (`eval 'echo $?'$rv` == 0) then
        echo '$'$rv' must be set.'
    endif
end

# Precondition and default arguments
set MR_HOME=/meme_work/mr
set mth_update=0
set deleted_cui_bins=''
set db=memestg
set section=1
set date=`/bin/date +%Y%m%d`
# Get user ID
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`


# Parse arguments
if ($#argv == 2) then
        set db=$1
        set section=$2
else
    echo "   Usage: $0 <db> <startop 1,2,3>"
    exit 1
endif


#
# Check preconditions
#
echo "   Check preconditions ... `/bin/date`"

    echo "   ... Check for status N concepts"
    set to_edit=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select count(*) ct from concept_status where status='N'"`
    if ($status != 0) then
       echo "error checking for status N concepts"
       exit 1
    endif
    if ($to_edit != 0) then
       echo "ERROR: Status N concepts exist"
       echo "Please fix this before proceeding."
       exit 1
    endif
    
    echo  "   ... Check for MTH insertion during this editing cycle"
    set mth_ver=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select NVL(meta_ver,'$release') as meta_ver from sims_info where source=(select current_name from source_version where source='MTH')"`
    if ($status != 0 || $mth_ver == "") then
        echo "Error getting MTH release info"
        exit 1
    endif
    if ($mth_ver == $release) then
        set mth_update = 1;
    endif

    echo "   ... Check for T_DELETED_CUI_% bins"

    set deleted_cui_bins=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select table_name from user_tables where table_name like 'T_DELETEDCUI_%'"`
    if ($status != 0) then
        echo "Error getting deleted_cui_bins"
        exit 1
    endif
# end check preconditions


echo "-----------------------------------------------------------"
echo "Starting pre-production...`/bin/date`"
echo "-----------------------------------------------------------"
echo "DB:          $db"
echo "release:     $release"
echo "old_release: $old_release"
echo "section:     $section"
echo


#
# Check max CL CUI
#

echo "   ... Check for max CL CUI vs. value in max_tab"
set max_cl_query=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select query from mid_validation_queries where check_name='Max CL CUI should match value in max_tab'"`
set max_cl_cui_mismatch=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select count(*) ct from ($max_cl_query)"`

if ($max_cl_cui_mismatch != 0) then
   echo "ERROR: Max CL CUI in classes does not match max_tab."
   echo "This may cause problems when assigning CUIs."
   exit 1
endif



#
# Check for T_DELETED_CUI_% bins and restore LRCs if needed
#
    if ("$deleted_cui_bins" != "") then

        echo "    Restore LRCs from T_DELETED_CUI_% bins ... `/bin/date`"
        foreach dcb ($deleted_cui_bins)
            echo "    ... $dcb"
            $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
                WHENEVER SQLERROR EXIT -2
                ALTER SESSION SET sort_area_size=200000000;
                ALTER SESSION SET hash_area_size=200000000;

                UPDATE classes c
                SET c.released='A', c.last_release_cui =
                  (SELECT a.last_release_cui from $dcb a
                   WHERE a.atom_id = c.atom_id)
                WHERE c.atom_id in
                   (select atom_id from $dcb);
EOF
            if ($status != 0) then
                echo "Error restoring last_release_cuis from $dcb"
                cat /tmp/t.$$.log
                exit 1
            endif
       end
    endif


#
# Copy NET folder to release directory so that MRD can build MMSYS
echo "   Copy NET folder ... `/bin/date`"
if (! -e $MR_HOME/NET.zip) then
    echo "$MR_HOME/NET.zip does not exist"
    exit 1
endif

unzip $MR_HOME/NET.zip -d $MR_HOME/$release
if ($status != 0) then
    echo "Error copying $MR_HOME/NET.zip to $MR_HOME/$release"    
    exit 1
endif


#
# Load previous MRCUI.RRF into cui_history
#
echo "   Load previous MRCUI.RRF into cui_history ... `/bin/date`"

cd $MR_HOME/$release/log
$MEME_HOME/bin/dump_mid.pl -t cui_history $db .
/bin/cp -f $MR_HOME/$old_release/META/MRCUI.RRF cui_history.dat
$ORACLE_HOME/bin/sqlldr $user@$db -control=cui_history.ctl
if ($status != 0) then
    echo "Error loading previous MRCUI.RRF into cui_history"    
    exit 1
endif

#
# Verify default preferred_atoms 
#
echo "   Verify default preferred atoms ... `/bin/date`"
$MEME_HOME/bin/verifyDefaultPreferredAtoms.csh $db >&! $MR_HOME/$release/log/verifyDefaultPreferredAtoms.log
if ($status != 0) then
    echo "Error verifying default atoms"
    cat $MR_HOME/$release/log/verifyDefaultPreferredAtoms.log     
    exit 1
endif


#
# Insert NCI_THESAURUS_CODE attributes to PDQ PTs
#
echo "   Insert NCI_THESAURUS_CODE attributes to PDQ PTs ... `/bin/date`"
$MEME_HOME/bin/assign_pdq_nci_code.csh $db >&! $MR_HOME/$release/log/assign_pdq_nci_code.log
if ($status != 0) then
    echo "Error inserting NCI_THESAURUS_CODE attributes"
    cat $MR_HOME/$release/log/assign_pdq_nci_code.log
    exit 1
endif

#
# Insert PDQ to NCI mappings
#
echo "   Insert PDQ to NCI mappings ... `/bin/date`"
set from_source=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select current_name from source_version where source='PDQ'"`
set to_source=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select current_name from source_version where source='NCI'"`
$MEME_HOME/bin/create_pdq_mappings.csh $db $from_source $to_source >&! $MR_HOME/$release/log/create_pdq_mappings.log
if ($status != 0) then
    echo "Error inserting PDQ to NCI mappings"
    cat $MR_HOME/$release/log/create_pdq_mappings.log 
    exit 1
endif

#
# XMAP queries
#
echo "   Run XMAP MID validation checks ... `/bin/date`"
foreach q ("Dangling XMAP (no XMAPFROM)" "Dangling XMAPTO" "Dangling XMAPFROM" "Dangling XMAP (no XMAPTO)")
    set xmap_query=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select query from mid_validation_queries where check_name= '$q'"`
    set xmap_result=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select count(*) ct from ($xmap_query)"`
    if ($xmap_result != 0) then
       echo "ERROR: XMAP query $q returns non-zero value."
       echo "Please fix before proceeding."
       exit 1
    endif
end

#
# Set ranks
#
echo "   Set ranks ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    ALTER SESSION SET sort_area_size=200000000;
    ALTER SESSION SET hash_area_size=200000000;
    
    exec meme_ranks.set_ranks(classes_flag=>'Y', relationships_flag=>'N', attributes_flag=>'N');
EOF
if ($status != 0) then
    echo "Error setting ranks"
    cat /tmp/t.$$.log
    exit 1
endif


#
# Set context types, official and short names
#

echo "   Set context types ... `/bin/date`"
cd $MEME_HOME/cgi-bin/
/usr/bin/perl -e 'print "state=RUNNING&db='$db'&meme_home=/local/content/MEME/MEME4&command=set_context_type\n\n"' | \
    $MEME_HOME/cgi-bin/release_maintenance.cgi >>& /dev/null
set running=`/usr/bin/pgrep release_maint`
while ($running != '')
    echo "       ... pid $running running ... `/bin/date`"
    sleep 300
    set running=`/usr/bin/pgrep release_maint`
end
if ($status != 0 || `/bin/egrep -c 'FAILED|Error' $MEME_HOME/cgi-bin/MIDLogs/$date.set_context_type.$db.log` > 0) then
   echo "Error setting context types"
   exit 1
endif

echo "   Set official and short names ... `/bin/date`"
cd $MEME_HOME/cgi-bin/
/usr/bin/perl -e 'print "state=RUNNING&db='$db'&meme_home=/local/content/MEME/MEME4&command=set_official_name\n\n"' | \
    $MEME_HOME/cgi-bin/release_maintenance.cgi >>& /dev/null
set running=`/usr/bin/pgrep release_maint`
while ($running != '')
    echo "       ... pid $running running ... `/bin/date`"
    sleep 300
    set running=`/usr/bin/pgrep release_maint`
end
if ($status != 0 || `/bin/fgrep -c 'ERROR' MIDLogs/$date.set_official_name.$db.log` > 0) then
   echo "Error setting official and short names"
   exit 1
endif
cd -


#
# Set IGNORE-RELA
#
echo "   Set IGNORE-RELA ... (NCI,RADLEX,OMIM) `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    ALTER SESSION SET sort_area_size=200000000;
    ALTER SESSION SET hash_area_size=200000000;
    
    UPDATE sims_info set context_type = 'FULL-NOSIB-MULTIPLE-IGNORE-RELA'
    where source in
     (select current_name from source_version where source in('NCI'));
    UPDATE sims_info set context_type = 'FULL-MULTIPLE-IGNORE-RELA'
    where source in
     (select current_name from source_version where source = 'RADLEX');
    UPDATE sims_info set context_type = 'FULL-NOSIB-MULTIPLE'
    where source in
     (select current_name from source_version where source = 'OMIM');
    
EOF
if ($status != 0) then
    echo "Error setting IGNORE-RELA"
    cat /tmp/t.$$.log
    exit 1
endif


#
# Set IMETA, RMETA, and sims_info meta_ver values
#

echo "   Set IMETA, RMETA, and sims_info meta_ver values ... `/bin/date`"
cd $MEME_HOME/cgi-bin/
/usr/bin/perl -e 'print "state=RUNNING&db='$db'&meme_home=/local/content/MEME/MEME4&command=set_imeta_rmeta&new_release='$release'&old_release='$old_release'\n\n"' | \
    $MEME_HOME/cgi-bin/release_maintenance.cgi >>& /dev/null
set running=`/usr/bin/pgrep release_maint`
while ($running != '')
    echo "       ... pid $running running ... `/bin/date`"
    sleep 300
    set running=`/usr/bin/pgrep release_maint`
end
if ($status != 0 || `/bin/egrep -c 'FAILED|Error' $MEME_HOME/cgi-bin/MIDLogs/$date.set_imeta_rmeta.$db.log` > 0) then
   echo "Error setting IMETA, RMETA, and sims_info meta_ver values"
   exit 1
endif
cd -

#
# Assign CUIs
#
echo "   Assign CUIs ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! $MR_HOME/$release/log/assign_cuis.log
    WHENEVER SQLERROR EXIT -2
    ALTER SESSION SET sort_area_size=200000000;
    ALTER SESSION SET hash_area_size=200000000;
    @$MRD_HOME/etc/sql/assign_cuis.sql
EOF
if ($status != 0) then
    echo "Error assigning CUIs"
    cat $MR_HOME/$release/log/assign_cuis.log
    exit 1
endif

#
# Assign SEMANTIC_TYPE ATUI placeholders
#
echo "   Assign SEMANTIC_TYPE ATUI placeholders ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! $MR_HOME/$release/log/assign_atuis.log
    WHENEVER SQLERROR EXIT -2
    ALTER SESSION SET sort_area_size=200000000;
    ALTER SESSION SET hash_area_size=200000000;
    @$MRD_HOME/etc/sql/assign_atuis.sql
EOF
if ($status != 0) then
    echo "Error assigning SEMANTIC_TYPE ATUI placeholders"
    cat $MR_HOME/$release/log/assign_atuis.log
    exit 1
endif




#
# Map CUI history
#
echo "   Map CUI history ... `/bin/date`"
echo "    ... Run Map CUI history ...`/bin/date`"
cd $MEME_HOME/cgi-bin/
/usr/bin/perl -e 'print "state=RUNNING&db='$db'&meme_home=/local/content/MEME/MEME4&command=cui_history&old_release='$old_release'\n\n"' | \
    $MEME_HOME/cgi-bin/release_maintenance.cgi >>& /dev/null
set running=`/usr/bin/pgrep release_maint`
while ($running != '')
    echo "       ... pid $running running ... `/bin/date`"
    sleep 300
    set running=`/usr/bin/pgrep release_maint`
end
if ($status != 0 || `/bin/egrep -c 'FAILED|Error' $MEME_HOME/cgi-bin/MIDLogs/$date.cui_history.$db.log` > 0) then
   echo "Error running CUI history"
   exit 1
endif
cd -

echo "   ... Fix CUI history to make sure no bogus entries wind up in MRCUI.RRF ... `/bin/date`"
$MRD_HOME/bin/fix_history.csh $MR_HOME/$old_release/META $MR_HOME/$release/META $db >&! $MR_HOME/$release/log/fix_history.log
if ($status != 0 || `/bin/fgrep -c 'FAILED' $MR_HOME/$release/log/fix_history.log` > 0) then
    echo "Error running fix_history"
    cat $MR_HOME/$release/log/fix_history.log
    exit 1
endif

echo "   ... Check for null ATUI assignments where CUIs merged... `/bin/date`"
set null_atuis=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select count(*) ct from (select * from attributes where atui is null and tobereleased in ('Y','y') and attribute_name != 'SEMANTIC_TYPE');"`

if ($null_atuis != 0) then
   echo "ERROR: Null ATUI assignments after running fix_history.csh."
   echo "Please assign ATUIs to these cases before proceeding."
   exit 1
endif  
  

#
# Map AUI history
#
echo "   Map AUI history ... `/bin/date`"

if ($mth_update == 1) then
    echo "    ... Save current atom_id,last_release_cui, and restore values from last_release_cuis ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -2
        ALTER SESSION SET sort_area_size=200000000;
        ALTER SESSION SET hash_area_size=200000000;
        exec MEME_UTILITY.drop_it('table','t_lrc_bak');
        create table t_lrc_bak as
        select atom_id, last_release_cui from classes where atom_id in (select atom_id from last_release_cuis);
        alter table t_lrc_bak add primary key (atom_id);
        update (select a.last_release_cui lrc1, b.last_release_cui lrc2 from classes a, last_release_cuis b where a.atom_id=b.atom_id)
        set lrc1 = lrc2 where lrc1 !=lrc2;
EOF
    if ($status != 0) then
        echo "Error using LRCs from last_release_cuis"
        cat /tmp/t.$$.log
        exit 1
    endif
endif


echo "    ... Run Map AUI history ...`/bin/date`"
cd $MEME_HOME/cgi-bin/
/usr/bin/perl -e 'print "state=RUNNING&db='$db'&meme_home=/local/content/MEME/MEME4&command=aui_history&new_release='$release'\n\n"' | \
    $MEME_HOME/cgi-bin/release_maintenance.cgi >>& /dev/null
set running=`/usr/bin/pgrep release_maint`
while ($running != '')
    echo "       ... pid $running running ... `/bin/date`"
    sleep 300
    set running=`/usr/bin/pgrep release_maint`
end
if ($status != 0 || `/bin/egrep -c 'FAILED|Error' $MEME_HOME/cgi-bin/MIDLogs/$date.aui_history.$db.log` > 0) then
   echo "Error running AUI history"
   exit 1
endif
cd -


if ($mth_update == 1) then
    echo "   ... Restore LRCs ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -2
        ALTER SESSION SET sort_area_size=200000000;
        ALTER SESSION SET hash_area_size=200000000;
        UPDATE 
            (select a.last_release_cui lrc1, b.last_release_cui lrc2 from classes a, t_lrc_bak b where a.atom_id=b.atom_id)
        SET lrc1 = lrc2 where lrc1 != lrc2;
        COMMIT;
        DROP table t_lrc_bak;
EOF
    if ($status != 0) then
        echo "Error restoring LRCs"
        cat /tmp/t.$$.log
        exit 1
    endif
endif


#
# Assign LRR
#

echo "    Assign last_release_rank ...`/bin/date`"
cd $MEME_HOME/cgi-bin/
/usr/bin/perl -e 'print "state=RUNNING&db='$db'&meme_home=/local/content/MEME/MEME4&command=lrr\n\n"' | \
    $MEME_HOME/cgi-bin/release_maintenance.cgi >>& /dev/null
set running=`/usr/bin/pgrep release_maint`
while ($running != '')
    echo "       ... pid $running running ... `/bin/date`"
    sleep 300
    set running=`/usr/bin/pgrep release_maint`
end
if ($status != 0 || `/bin/egrep -c 'FAILED|Error' $MEME_HOME/cgi-bin/MIDLogs/$date.lrr.$db.log` > 0) then
   echo "Error assigning LRR"
   exit 1
endif
cd -

#
# Run matrixinit
#
echo "    Run matrix initializer ...`/bin/date`"
$MEME_HOME/bin/matrixinit.pl -I $db >&! $MR_HOME/$release/log/matrixinit.log
if ($status != 0) then
    echo "Error initializing matrix"
    cat $MR_HOME/$release/log/matrixinit.log
    exit 1
endif

#
# Check for duplicate RUIs
#
echo "   Run Duplicate RUI MID validation checks ... `/bin/date`"

foreach q ("Non-unique RUI (R) - selfref" "Non-unique RUI (R)" "Non-unique RUI (CR)" "Non-unique RUI (R union inverse R)" "Non-unique RUI (R with relationship_group)")
    set rui_query=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select query from mid_validation_queries where check_name='$q'"`
    set rui_result=`$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select count(*) ct from ($rui_query)"`
    if ($rui_result != 0) then
       echo "ERROR: RUI query $q returns non-zero value."
       echo "Please fix before proceeding."
       exit 1
    endif
end

#
# Prep mrd
#
echo "    Prep for copyout to MRD (drop mrd_contexts indexes, create timestamp) ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -2
    ALTER SESSION SET sort_area_size=200000000;
    ALTER SESSION SET hash_area_size=200000000;
    EXEC MEME_SYSTEM.drop_indexes('mrd_contexts');
    EXEC MEME_UTILITY.drop_it('table','preprod');
    CREATE TABLE preprod AS SELECT sysdate AS timestamp FROM dual;
EOF
if ($status != 0) then
    echo "Error prepping for copyout to MRD"
    cat /tmp/t.$$.log
    exit 1
endif


#
# done
#
echo "-----------------------------------------------------------"
echo "Finished pre-production...`/bin/date`"
echo "-----------------------------------------------------------"
