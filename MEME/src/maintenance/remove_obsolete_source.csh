#!/bin/csh -f
#
# File:   remove_obsolete_source.csh
# Author: Tim Kao
#
# Remarks: This script changes the releasibility of the input source
# 			to n.
# 
# CHANGES: 07-17-2007 TK (1-EP3M9): Creation date
#		   11/26/2007 TK (1-FUGMM): Change qa_diff_adjustment count to negative. 

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

#
# Check required variables
#
if ($?MEME_HOME == 0) then
    echo '$MEME_HOME must be set.'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set.'
    exit 1
endif

#
# Parse arguments
#
if ($#argv == 3) then
   set vsab=$1
   set db=$2
   set authority=$3
   set rsab=""
else if ($#argv == 4) then
   set vsab=$1
   set db=$2
   set authority=$3
   set rsab=$4
else
    cat <<EOF
 This script has the following usage:
   Usage: remove_obsolete_source.csh <vsab> <database> <authority> [<bequeath to rsab>]

   This script "removes" an obsolete source by:
   1. Marking all data as tobereleased=n
   2. Marking SRC concepts as tobereleased=N
   3. Marking VSAB as "previous" with a blank "current version"
   4. Bequeathing the SRC concepts.
EOF
    exit 0
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

echo "-----------------------------------------------------------"
echo "Starting $0 ...`/bin/date`"
echo "-----------------------------------------------------------"
echo "versioned source:         $vsab"
echo "db:                       $db"
echo "authority:                $authority"
echo "rsab:                     $rsab"
echo ""

#
# This will mark the source content and versioned SRC concept as tobereleased=n
#
echo "    Run update releasability... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
   WHENEVER SQLERROR EXIT -1;
   set serveroutput on size 100000
   alter session set sort_area_size=200000000;   
   alter session set hash_area_size=200000000;

   exec MEME_SOURCE_PROCESSING.update_releasability( -
      old_source => '$vsab', -
      new_source => '$vsab', - 
      authority => '$authority', -
      new_value => 'N', -
      work_id => 0);
EOF
if ($status != 0) then
    cat /tmp/t.$$.log
    echo "Error creating insertion indexes"
    exit 1
endif

#
# Identify RSAB SRC concept atoms (via CODE=V-<RSAB>) and mark them as unreleasable.
# Make sure MTH/(PN,TM,MM) are unreleasable as well.
#
echo "    Identify RSAB concept atoms...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
   WHENEVER SQLERROR EXIT -1;
   set serveroutput on size 100000
   alter session set sort_area_size=200000000;   
   alter session set hash_area_size=200000000;

   exec MEME_UTILITY.drop_it('table','obsolete_rsab_concept_atoms');
   CREATE TABLE obsolete_rsab_concept_atoms AS
   SELECT concept_id,atom_id as row_id FROM classes
   WHERE code = 'V-' || (SELECT source FROM source_version WHERE current_name = '$vsab');

   ALTER TABLE obsolete_rsab_concept_atoms MODIFY concept_id number(12) null;
	
   INSERT INTO obsolete_rsab_concept_atoms (row_id)
   SELECT DISTINCT atom_id as row_id FROM classes 
   WHERE source='MTH' 
     AND tty in ('PN','MM','TM') 
     AND concept_id IN
      (SELECT concept_id FROM obsolete_rsab_concept_atoms)
     AND concept_id NOT IN 
      (SELECT concept_id FROM classes
       WHERE tobereleased in ('Y','y')
         AND source NOT IN ('SRC', 'MTH'));
EOF
if ($status != 0) then
    echo "Error Identify RSAB concept atoms"
    cat /tmp/t.$$.log
    exit 1
endif

echo "    Batch action to change releasability...`/bin/date`"
$MEME_HOME/bin/batch.pl -a T -t C -s t obsolete_rsab_concept_atoms -n N $db $authority >&! /tmp/t.$$.log
if ($status != 0) then
    echo "Error batch change tobereleased operation"
    cat /tmp/t.$$.log
    exit 1
endif

#
# If rsab is set, create a bequeathal rel from the SRC/RPT atom of 
# $vsab's root source to the SRC/RPT atom of the passed in source 
# (use CODE_ROOT_TERMGROUP for the sg types).  
# In this case, ALSO create a bequeathal rel from the SRC/VPT atom 
# of $vsab's SRC concept to the SRC/VPT atom of the current version of the 
# passed in source.  
#
if ($rsab !="") then
   echo "    Create bequeathal rels to $rsab atoms...`/bin/date`" 
   $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
      WHENEVER SQLERROR EXIT -1;
      set serveroutput on size 100000
      alter session set sort_area_size=200000000;   
      alter session set hash_area_size=200000000;
      
      exec MEME_UTILITY.drop_it('table','obsolete_rsab_concept_atoms');
      exec MEME_UTILITY.drop_it('table','rsab_bequeathal_rels');
	
      CREATE TABLE rsab_bequeathal_rels AS
      SELECT concept_id_1,concept_id_2,atom_id_1,atom_id_2,
         relationship_name,relationship_attribute,
         source, source_of_label,status,generated_status,
         relationship_level,released,tobereleased,
         relationship_id, suppressible,
         sg_id_1, sg_type_1, sg_qualifier_1,
         sg_id_2, sg_type_2, sg_qualifier_2,
         source_rui, relationship_group
      FROM relationships WHERE 1=0;
      
      INSERT INTO rsab_bequeathal_rels
      SELECT a.concept_id,b.concept_id,a.atom_id,b.atom_id,
        'BRT','',
        'SRC','SRC','R','Y',
        'C','N','Y',
        0,'N',
        a.code,'CODE_ROOT_TERMGROUP','SRC/RPT',
        b.code,'CODE_ROOT_TERMGROUP','SRC/RPT',
        '',''
      FROM classes a, classes b
      WHERE a.termgroup = 'SRC/RPT'
        AND a.tobereleased in ('n','N')
        AND a.code = 'V-'|| (SELECT source FROM source_version 
                             WHERE current_name = '$vsab')
        AND b.termgroup = 'SRC/RPT'
        AND b.code = 'V-' || '$rsab'
        AND b.tobereleased in ('y','Y');
      
      INSERT INTO rsab_bequeathal_rels
      SELECT a.concept_id,b.concept_id,a.atom_id,b.atom_id,
        'BRT','',
        'SRC','SRC','R','Y',
        'C','N','Y',
        0,'N',
        a.code,'CODE_ROOT_TERMGROUP','SRC/VPT',
        b.code,'CODE_ROOT_TERMGROUP','SRC/RPT',
        '',''
      FROM classes a, classes b
      WHERE a.termgroup = 'SRC/VPT'
        AND a.code = 'V-'|| '$vsab'
        AND a.tobereleased in ('n','N')
        AND b.termgroup = 'SRC/RPT'
        AND b.code = 'V-$rsab'
        AND b.tobereleased in ('y','Y'); 
EOF
   if ($status != 0) then
      echo "Error creating bequeathal rels to $rsab."
      cat /tmp/t.$$.log
      exit 1
   endif

#
# Otherwise, create a bequeathal relationship to the SRC/RPT atom with 
# CODE=V-MTH
#
else
   echo "    Create bequeathal rels to SRC/RPT atom...`/bin/date`" 
   $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
      WHENEVER SQLERROR EXIT -1;
      set serveroutput on size 100000
      alter session set sort_area_size=200000000;   
      alter session set hash_area_size=200000000;
    
      exec MEME_UTILITY.drop_it('table','obsolete_rsab_concept_atoms');
      exec MEME_UTILITY.drop_it('table','rsab_bequeathal_rels');
	
      CREATE TABLE rsab_bequeathal_rels AS
      SELECT concept_id_1,concept_id_2,atom_id_1,atom_id_2,
         relationship_name,relationship_attribute,
         source, source_of_label,status,generated_status,
         relationship_level,released,tobereleased,
         relationship_id, suppressible,
         sg_id_1, sg_type_1, sg_qualifier_1,
         sg_id_2, sg_type_2, sg_qualifier_2,
         source_rui, relationship_group
      FROM relationships WHERE 1=0;
    
      INSERT INTO rsab_bequeathal_rels
	  SELECT a.concept_id,b.concept_id,a.atom_id,b.atom_id,
        'BRT','',
        'SRC','SRC','R','Y',
        'C','N','Y',
        0,'N',
        a.code,'CODE_ROOT_TERMGROUP','SRC/RPT',
        b.code,'CODE_ROOT_TERMGROUP','SRC/RPT',
        '',''
      FROM classes a, classes b
      WHERE a.code = 'V-' || (SELECT source FROM source_version 
                              WHERE current_name = '$vsab')
        AND a.tobereleased in ('n','N')
        AND a.termgroup = 'SRC/RPT'
        AND b.termgroup = 'SRC/RPT'
        AND b.tobereleased in ('Y','y')
        AND b.code = 'V-MTH';
EOF
   if ($status != 0) then
      echo "Error creating bequeathal_rels to SRC/RPT"
      cat /tmp/t.$$.log
      exit 1
   endif

endif

echo "    Insert bequeathal rels...`/bin/date`"
$MEME_HOME/bin/insert.pl -rels rsab_bequeathal_rels $db $authority >&! /tmp/t.$$.log
if ($status != 0) then
    echo "Error inserting bequeathal rels."
    cat /tmp/t.$$.log
    exit 1
endif

#
# Resolve SRC QA data
#
echo "    Resolve SRC QA data...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
   WHENEVER SQLERROR EXIT -1;
   set serveroutput on size 100000
   alter session set sort_area_size=200000000;   
   alter session set hash_area_size=200000000;
    
   exec MEME_UTILITY.drop_it('table','rsab_bequeathal_rels');
    
   UPDATE source_version 
   SET previous_name=current_name, current_name=null 
   WHERE current_name='$vsab';
    
   INSERT INTO src_obsolete_qa_results (qa_id, name, value ,qa_count, timestamp)
   SELECT qa_id, name, value, qa_count, timestamp
   FROM src_qa_results
   WHERE (value like '$vsab' || ',%' or value = '$vsab');
    
   UPDATE src_obsolete_qa_results a
   SET qa_count =
      (SELECT a.qa_count + sum(b.qa_count)
      FROM qa_adjustment b
      WHERE a.name = b.name and a.qa_id=b.qa_id
         AND a.value = b.value
      GROUP BY b.name, b.qa_id, b.value)
   WHERE (qa_id, name, value) IN
      (SELECT qa_id, name, value FROM qa_adjustment)
     AND (value like '$vsab' || ',%' or value = '$vsab');
    
   DELETE FROM src_qa_results
   WHERE (value like '$vsab' || ',%' or value = '$vsab');
   
   DELETE FROM qa_adjustment
   WHERE (value like '$vsab' || ',%' or value = '$vsab');
   
   INSERT INTO qa_diff_adjustment (qa_id_1,qa_id_2,name,value,qa_count,timestamp,description)
   SELECT qa_id as qa_id_1, qa_id as qa_id_2, name, value, 
     0 - qa_count, sysdate as timestamp,
    'Source Removed' as description 
   FROM src_obsolete_qa_results 
   WHERE (value like '$vsab' || ',%' or value = '$vsab');
EOF
if ($status != 0) then
    echo "Error resolving SRC QA data."
    cat /tmp/t.$$.log
    exit 1
endif

#
# Cleanup
#
/bin/rm -f /tmp/t.$$.log

echo "-----------------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "-----------------------------------------------------------"
