#!/bin/csh -f
#
# This script creates MRMAP.RRF-style mappings between SNOMEDCT and ICD10CM.
#
#

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($?MIDSVCS_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($#argv != 1) then
    echo "Usage: $0 <database>"
    exit 1
else if ($#argv == 1) then
    set db=$1
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "db: $db"
echo ""

set u=`echo $user | cut -d\/ -f 1`


echo "    Create mappings ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1

    -- Get SNOMEDCT concept_ids/codes
    drop table t_ASDF_snomedct;
    create table t_ASDF_snomedct as
    select distinct concept_id, code from classes
    where source = (select current_name from source_version where source='SNOMEDCT')
    and tobereleased in ('y','Y');
   
    create index x_ASDF_snomedct on t_ASDF_snomedct (concept_id,code) compute statistics;

    -- Get all ICD10CM and SNOMEDCT atoms in the same CUI/concept
    drop table t_ASDF_snomedct_icd10;
    create table t_ASDF_snomedct_icd10 as
    select c1.atom_id as snomedct_id,c1.concept_id as snomedct_cid, c1.code as snomedct_code, a1.atom_name as snomedct_name, c1.tty as snomedct_tty, c1.rank||lpad(c1.atom_id,10,0) as snomedct_rank,
             c2.atom_id  as icd10_id, c2.concept_id as icd10_cid, c2.code as icd10_code, a2.atom_name as icd10_name, c2.tty as icd10_tty, c2.rank||lpad(c2.atom_id,10,0) as icd10_rank
    from classes c1, classes c2, atoms a1, atoms a2
    where (c1.concept_id, c1.code) in
        (select concept_id,code from t_ASDF_snomedct)
    and c1.source=(select current_name from source_version where source='SNOMEDCT')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id
    and c1.concept_id = c2.concept_id
    and c2.source=(select current_name from source_version where source='ICD10CM')
    and c2.tobereleased in ('Y','y')
    and c2.atom_id = a2.atom_id;

    -- Create mapping table between ICD10CM and SNOMEDCT CUIs and codes.

    drop table t_ASDF_snomedct_icd10_map;
    create table t_ASDF_snomedct_icd10_map as
    select a.snomedct_code, a.snomedct_cid, snomedct_name,
            'SY' as relationship_name,
             a.icd10_code, a.icd10_cid, icd10_name,
             0 as has_both_pts,
             0 as map_rank
    from t_ASDF_snomedct_icd10 a;


    -- Make all names ICD10CM/PT or SNOMEDCT/PT only
    update t_ASDF_snomedct_icd10_map tnsm
    set snomedct_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='SNOMEDCT')
         and c.code = tnsm.snomedct_code
         and c.tty='FN'),
    icd10_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='ICD10CM')
         and c.code = tnsm.icd10_code
         and c.tty in ('PT','HT'));


    -- Set ranking conditions for all cases.
    update t_ASDF_snomedct_icd10_map set has_both_pts=1
    where snomedct_cid in
        (select snomedct_cid from t_ASDF_snomedct_icd10 where snomedct_tty='FN' union select snomedct_cid from t_ASDF_snomedct_icd10_rels where snomedct_tty='FN')
    and icd10_cid in
        (select icd10_cid from t_ASDF_snomedct_icd10 where icd10_tty in ('PT','HT') union select icd10_cid from t_ASDF_snomedct_icd10_rels where icd10_tty in ('PT','HT'));

    update t_ASDF_snomedct_icd10_map set map_rank = 2 - has_both_pts;


    drop table t_ASDF_out;
    create table t_ASDF_out as select distinct snomedct_code,snomedct_name, relationship_name, map_rank, icd10_code, icd10_name from t_ASDF_snomedct_icd10_map
    order by 1 asc;

    --drop table t_ASDF_best_out;
    --create table t_ASDF_best_out as
    --select * from t_ASDF_out where map_rank||snomedct_code in
    --     (select min(map_rank)||snomedct_code from t_ASDF_out group by snomedct_code);

    --drop table t_ASDF_out_mrmap;
    --create table t_ASDF_out_mrmap as
    --select 'SNOMEDCT2ICD10CM' as MAPSETCUI, 'NCIMTH' as MAPSAB, 1 as MAPSUBSETID, map_rank as MAPRANK, rownum as MAPID, ' ' as MAPSID,
    --snomedct_code as FROMID, ' ' as FROMSID, snomedct_code as FROMEXPR, 'CODE' as FROMTYPE, ' ' as FROMRULE, ' ' as FROMRES,
    --relationship_name as REL, ' ' as RELA,
    --icd10_code as TOID, ' ' as TOSID, icd10_code as TOEXPR, 'CODE' as TOTYPE, ' ' as TORULE, ' ' as TORES,
    --' ' as MAPRULE, ' ' as MAPRES, ' ' as MAPTYPE, ' ' as ATN, ' ' as ATV
    --from t_ASDF_out;

    --drop table t_ASDF_out_best_mrmap;
    --create table t_ASDF_out_best_mrmap as
    --select 'SNOMEDCT2ICD10CM' as MAPSETCUI, 'NCIMTH' as MAPSAB, 1 as MAPSUBSETID, map_rank as MAPRANK, rownum as MAPID, ' ' as MAPSID,
    --snomedct_code as FROMID, ' ' as FROMSID, snomedct_code as FROMEXPR, 'CODE' as FROMTYPE, ' ' as FROMRULE, ' ' as FROMRES,
    --relationship_name as REL, ' ' as RELA,
    --icd10_code as TOID, ' ' as TOSID, icd10_code as TOEXPR, 'CODE' as TOTYPE, ' ' as TORULE, ' ' as TORES,
    --' ' as MAPRULE, ' ' as MAPRES, ' ' as MAPTYPE, ' ' as ATN, ' ' as ATV
    --from t_ASDF_best_out;

EOF
if ($status != 0) then
    echo "ERROR creating tables"
    exit 1
endif


echo "    Dump mappings to file... `/bin/date`"
$MEME_HOME/bin/dump_mid.pl -t t_ASDF_out $db .
if ($status != 0) then
    echo "ERROR dumping t_ASDF_out"
    exit 1
endif



echo "--------------------------------------------------------------"
echo "Done... `/bin/date`"
echo "--------------------------------------------------------------"
