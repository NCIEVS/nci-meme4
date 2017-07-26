#!/bin/csh -f
#
# This script creates MRMAP.RRF-style mappings between NCIt and ICD9CM.
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

    -- Get the Neoplasm NCI/PT (aui=A2662660) and its children.
    drop table t_$$_nci_neoplasms;
    create table t_$$_nci_neoplasms as
    select concept_id, code from classes where aui='A2662660'
    and tobereleased in ('y','Y')
    union
    select concept_id, code from classes where atom_id in
        (select atom_id_1 from context_relationships where parent_treenum like '%A2662660%')
    and tobereleased in ('y','Y');
    
    -- Get all ICD9CM and NCI atoms in the same CUI/concept as Neoplasm NCI/PTs.
    drop table t_$$_nci_icd9;
    create table t_$$_nci_icd9 as
    select c1.atom_id as nci_id,c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty, c1.rank||lpad(c1.atom_id,10,0) as nci_rank,
             c2.atom_id  as icd9_id, c2.concept_id as icd9_cid, c2.code as icd9_code, a2.atom_name as icd9_name, c2.tty as icd9_tty, c2.rank||lpad(c2.atom_id,10,0) as icd9_rank
    from classes c1, classes c2, atoms a1, atoms a2
    where (c1.concept_id, c1.code) in
        (select concept_id,code from t_$$_nci_neoplasms)
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id
    and c1.concept_id = c2.concept_id
    and c2.source=(select current_name from source_version where source='ICD9CM')
    and c2.tobereleased in ('Y','y')
    and c2.atom_id = a2.atom_id;
    
    -- Create mapping table between ICD9CM and NCI CUIs and codes.
    
    drop table t_$$_nci_icd9_map;
    create table t_$$_nci_icd9_map as
    select a.nci_code, a.nci_cid, nci_name,
            'SY' as relationship_name,
             a.icd9_code, a.icd9_cid, icd9_name,
             0 as is_nci_merge, 0 as is_nci_split, 0 as is_icd9_merge, 0 as is_icd9_split, 0 as has_both_pts,
             0 as map_rank
    from t_$$_nci_icd9 a;

    -- Create staging table for context rels.
    drop table t_$$_nci_icd9_cxt;
    create table t_$$_nci_icd9_cxt as
    select distinct a.concept_id as concept_id_1, r.source, 'RN' as relationship_name, b.concept_id as concept_id_2
    from classes a, classes b, context_relationships r
    where r.relationship_name !='SIB'
    and r.source in (select current_name from source_version where source in ('NCI','ICD9CM'))
    and atom_id_1 = a.atom_id
    and atom_id_2 = b.atom_id
    and a.concept_id in (select concept_id from t_$$_nci_neoplasms)
    and a.concept_id not in (select concept_id from classes where source=(select current_name from source_version where source='ICD9CM'))
    and b.concept_id in (select concept_id from classes where source=(select current_name from source_version where source='ICD9CM'))
    UNION
    select distinct b.concept_id as concept_id_1, r.source, 'RB' as relationship_name, a.concept_id as concept_id_2
    from classes a, classes b, context_relationships r
    where r.relationship_name !='SIB'
    and r.source in (select current_name from source_version where source in ('NCI','ICD9CM')) and atom_id_1 = a.atom_id and atom_id_2 = b.atom_id
    and b.concept_id in (select concept_id from t_$$_nci_neoplasms)
    and b.concept_id not in (select concept_id from classes where source=(select current_name from source_version where source='ICD9CM'))
    and a.concept_id in (select concept_id from classes where source=(select current_name from source_version where source='ICD9CM'));
    
    -- Create table for NCI-ICD9CM rels, seed with cxts
    drop table t_$$_nci_icd9_rels;
    create table t_$$_nci_icd9_rels as
    select c1.atom_id as nci_id,c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty, c1.rank||lpad(c1.atom_id,10,0) as nci_rank,
             tnsc.relationship_name,
             c2.atom_id  as icd9_id, c2.concept_id as icd9_cid, c2.code as icd9_code, a2.atom_name as icd9_name, c2.tty as icd9_tty, c2.rank||lpad(c2.atom_id,10,0) as icd9_rank
    from classes c1, classes c2, atoms a1, atoms a2, t_$$_nci_icd9_cxt tnsc
    where (c1.concept_id, c1.code) in
        (select concept_id,code from t_$$_nci_neoplasms)
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id
    and c1.concept_id = tnsc.concept_id_1
    and c2.concept_id = tnsc.concept_id_2
    and c2.source=(select current_name from source_version where source='ICD9CM')
    and c2.tobereleased in ('Y','y')
    and c2.atom_id = a2.atom_id;
    
    delete from t_$$_nci_icd9_rels where nci_code in (select nci_code from t_$$_nci_icd9_map);
    
    
    -- Insert non-context rels
    insert into t_$$_nci_icd9_rels
    select c1.atom_id as nci_id,c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty, c1.rank||lpad(c1.atom_id,10,0) as nci_rank,
             r.relationship_name,
             c2.atom_id  as icd9_id, c2.concept_id as icd9_cid, c2.code as icd9_code, a2.atom_name as icd9_name, c2.tty as icd9_tty, c2.rank||lpad(c2.atom_id,10,0) as icd9_rank
    from classes c1, classes c2, atoms a1, atoms a2, relationships r
    where c1.atom_id = a1.atom_id and c2.atom_id = a2.atom_id
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and (c1.concept_id,c1.code) in (select concept_id,code from t_$$_nci_neoplasms)
    and c2.source=(select current_name from source_version where source='ICD9CM')
    and c2.tobereleased in ('Y','y')
    and c1.concept_id != c2.concept_id
    and r.concept_id_1 = c1.concept_id and r.concept_id_2 = c2.concept_id
    and r.relationship_name in ('BT','NT','RT')
    and r.tobereleased in ('Y','y')
    and r.source in (select current_name from source_version where source in ('NCI','ICD9CM'))
    and c1.code not in (select nci_code from t_$$_nci_icd9_rels);
    
    delete from t_$$_nci_icd9_rels where nci_cid in (select nci_cid from t_$$_nci_icd9_map);
    
    
    -- Insert rels into mapping table
    insert into t_$$_nci_icd9_map
    select a.nci_code, a.nci_cid, nci_name,
             a.relationship_name,
             a.icd9_code, a.icd9_cid, icd9_name,
             0 as is_nci_merge, 0 as is_nci_split, 0 as is_icd9_merge, 0 as is_icd9_split, 0 as has_both_pts,
             0 as map_rank
    from t_$$_nci_icd9_rels a;
    
    
    -- Make all names ICD9CM/PT or NCI/PT only
    update t_$$_nci_icd9_map tnsm
    set nci_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='NCI')
         and c.code = tnsm.nci_code
         and c.tty='PT'),
    icd9_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='ICD9CM')
         and c.code = tnsm.icd9_code
         and c.tty in ('PT','HT'));
    
    
    -- Set ranking conditions for all cases.
    update t_$$_nci_icd9_map set is_nci_merge=1 where nci_cid in
        (select nci_cid from t_$$_nci_icd9 group by nci_cid having count(distinct nci_code)>1
         union
         select nci_cid from t_$$_nci_icd9_rels group by nci_cid having count(distinct nci_code)>1);
    update t_$$_nci_icd9_map set is_icd9_merge=1 where icd9_cid in
        (select icd9_cid from t_$$_nci_icd9 group by icd9_cid having count(distinct icd9_code)>1
         union
         select icd9_cid from t_$$_nci_icd9_rels group by icd9_cid having count(distinct icd9_code)>1);
    update t_$$_nci_icd9_map set is_nci_split=1 where nci_code in
        (select nci_code from t_$$_nci_icd9 group by nci_code having count(distinct nci_cid)>1
         union
         select nci_code from t_$$_nci_icd9_rels group by nci_code having count(distinct nci_cid)>1);
    update t_$$_nci_icd9_map set is_icd9_split=1 where icd9_code in
        (select icd9_code from t_$$_nci_icd9 group by icd9_code having count(distinct icd9_cid)>1
         union
         select icd9_code from t_$$_nci_icd9_rels group by icd9_code having count(distinct icd9_cid)>1);
    update t_$$_nci_icd9_map set has_both_pts=1
    where nci_cid in
        (select nci_cid from t_$$_nci_icd9 where nci_tty='PT' union select nci_cid from t_$$_nci_icd9_rels where nci_tty='PT')
    and icd9_cid in
        (select icd9_cid from t_$$_nci_icd9 where icd9_tty in ('PT','HT') union select icd9_cid from t_$$_nci_icd9_rels where icd9_tty in ('PT','HT'));
    
    update t_$$_nci_icd9_map set map_rank = 2 - has_both_pts + is_nci_merge + is_nci_split + is_icd9_merge + is_icd9_split;
    
    
    -- Keep track of things that were not mapped.
    
    alter table t_$$_nci_icd9_map modify nci_cid NULL;
    alter table t_$$_nci_icd9_map modify nci_name NULL;
    alter table t_$$_nci_icd9_map modify icd9_cid NULL;
    alter table t_$$_nci_icd9_map modify icd9_name NULL;
    
    drop table t_$$_nci_icd9_unmapped;
    create table t_$$_nci_icd9_unmapped as
    select c1.atom_id as nci_id, c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty
    from classes c1, atoms a1
    where (c1.code) in
        (select code from t_$$_nci_neoplasms minus select nci_code from t_$$_nci_icd9_map)
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id;
    
    update t_$$_nci_icd9_unmapped tnsu
    set nci_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='NCI')
         and c.code = tnsu.nci_code
         and c.tty='PT');
    
    -- Make a table of unmapped NCI codes and their parents.
    drop table t_$$_nci_icd9_unmapped_parents;
    create table t_$$_nci_icd9_unmapped_parents as
    select distinct a.nci_id as atom_id_1, a.nci_cid as concept_id_1,a.nci_code as code,
    cr.relationship_name,
    b.atom_id as atom_id_2, b.concept_id as concept_id_2, b.code as parent_code
    from t_$$_nci_icd9_unmapped a, classes b, context_relationships cr
    where cr.relationship_name = 'PAR'
    and cr.source in (select current_name from source_version where source ='NCI')
    and cr.atom_id_1 in (select atom_id from classes a1 where a1.concept_id = a.nci_cid)
    and cr.atom_id_2 = b.atom_id;
    
    -- Make a table of unmapped NCI codes and their grandparents.
    drop table t_$$_nci_icd9_grandparent_rels;
    create table t_$$_nci_icd9_grandparent_rels as
    select distinct a.concept_id_1 as concept_id_1, r.source, 'RN' as relationship_name, b.concept_id as concept_id_2
    from t_$$_nci_icd9_unmapped_parents a, classes b, context_relationships r
    where r.relationship_name !='SIB'
    and r.source in (select current_name from source_version where source in ('NCI','ICD9CM'))
    and r.atom_id_1 = a.atom_id_2
    and r.atom_id_2 = b.atom_id
    and a.concept_id_2 not in (select concept_id from classes where source=(select current_name from source_version where source='ICD9CM'))
    and b.concept_id in (select concept_id from classes where source=(select current_name from source_version where source='ICD9CM'))
    UNION
    select distinct b.concept_id as concept_id_1, r.source, 'RB' as relationship_name, a.concept_id_1 as concept_id_2
    from t_$$_nci_icd9_unmapped_parents a, classes b, context_relationships r
    where r.relationship_name !='SIB'
    and r.source in (select current_name from source_version where source in ('NCI','ICD9CM'))
    and r.atom_id_1 = a.atom_id_2
    and r.atom_id_2 = b.atom_id
    and b.concept_id in (select concept_id from t_$$_nci_neoplasms)
    and b.concept_id not in (select concept_id from classes where source=(select current_name from source_version where source='ICD9CM'))
    and a.concept_id_2 in (select concept_id from classes where source=(select current_name from source_version where source='ICD9CM'));
    
    -- Create a mapping table based upon grandparent concept's context relationships.
    drop table t_$$_gp;
    create table t_$$_gp as
    select c1.atom_id as nci_id,c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty, c1.rank||lpad(c1.atom_id,10,0) as nci_rank,
             tnsgr.relationship_name,
             c2.atom_id  as icd9_id, c2.concept_id as icd9_cid, c2.code as icd9_code, a2.atom_name as icd9_name, c2.tty as icd9_tty, c2.rank||lpad(c2.atom_id,10,0) as icd9_rank,
             0 as is_nci_merge, 0 as is_nci_split, 0 as is_icd9_merge, 0 as is_icd9_split, 0 as has_both_pts,
             0 as map_rank
    from classes c1, classes c2, atoms a1, atoms a2, t_$$_nci_icd9_grandparent_rels tnsgr
    where (c1.concept_id, c1.code) in
        (select concept_id,code from t_$$_nci_neoplasms)
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id
    and c1.concept_id = tnsgr.concept_id_1
    and c2.concept_id = tnsgr.concept_id_2
    and c2.source=(select current_name from source_version where source='ICD9CM')
    and c2.tobereleased in ('Y','y')
    and c2.atom_id = a2.atom_id;
    
    -- Make all names ICD9CM/FN or NCI/PT only
    update t_$$_gp tgp
    set nci_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='NCI')
         and c.code = tgp.nci_code
         and c.tty='PT'),
    icd9_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='ICD9CM')
         and c.code = tgp.icd9_code
         and c.tty in ('PT','HT'));
    
    -- Set map ranking criteria
    update t_$$_gp set is_nci_merge=1 where nci_cid in
        (select nci_cid from t_$$_gp group by nci_cid having count(distinct nci_code)>1);
    update t_$$_gp set is_icd9_merge=1 where icd9_cid in
        (select icd9_cid from t_$$_gp group by icd9_cid having count(distinct icd9_code)>1);
    update t_$$_gp set is_nci_split=1 where nci_code in
        (select nci_code from t_$$_gp group by nci_code having count(distinct nci_cid)>1);
    update t_$$_gp set is_icd9_split=1 where icd9_code in
        (select icd9_code from t_$$_gp group by icd9_code having count(distinct icd9_cid)>1);
    update t_$$_gp set has_both_pts=1
    where nci_cid in
        (select nci_cid from t_$$_gp where nci_tty='PT')
    and icd9_cid in
        (select icd9_cid from t_$$_gp where icd9_tty in ('PT','HT'));
    
    update t_$$_gp set map_rank = 2 - has_both_pts + is_nci_merge + is_nci_split + is_icd9_merge + is_icd9_split;
    
    delete from t_$$_nci_icd9_unmapped where nci_code in
        (select nci_code from t_$$_gp);
    
    insert into t_$$_nci_icd9_map
    select a.nci_code, a.nci_cid, nci_name,
            '' as relationship_name,
             '' as icd9_code, '' as icd9_cid, '' as icd9_name,
             0 as is_nci_merge, 0 as is_nci_split, 0 as is_icd9_merge, 0 as is_icd9_split, 0 as has_both_pts,
             0 as map_rank
    from t_$$_nci_icd9_unmapped a;
    
    drop table t_$$_out;
    create table t_$$_out as select distinct nci_code,nci_name, relationship_name, map_rank, icd9_code, icd9_name from t_$$_nci_icd9_map
    union
    select distinct nci_code,nci_name, relationship_name, map_rank, icd9_code, icd9_name from t_$$_gp
    order by map_rank asc;
    
    --remove unmapped items
    delete from t_$$_out where map_rank=0;
    
    drop table t_$$_best_out;
    create table t_$$_best_out as
    select * from t_$$_out where map_rank||nci_code in
         (select min(map_rank)||nci_code from t_$$_out group by nci_code);
    
    drop table t_$$_out_mrmap;
    create table t_$$_out_mrmap as
    select 'NCI2ICD9' as MAPSETCUI, 'NCIMTH' as MAPSAB, 1 as MAPSUBSETID, map_rank as MAPRANK, rownum as MAPID, ' ' as MAPSID,
    nci_code as FROMID, ' ' as FROMSID, nci_code as FROMEXPR, 'CODE' as FROMTYPE, ' ' as FROMRULE, ' ' as FROMRES,
    relationship_name as REL, ' ' as RELA,
    icd9_code as TOID, ' ' as TOSID, icd9_code as TOEXPR, 'CODE' as TOTYPE, ' ' as TORULE, ' ' as TORES,
    ' ' as MAPRULE, ' ' as MAPRES, ' ' as MAPTYPE, ' ' as ATN, ' ' as ATV
    from t_$$_out;
    
    drop table t_$$_out_best_mrmap;
    create table t_$$_out_best_mrmap as
    select 'NCI2ICD9' as MAPSETCUI, 'NCIMTH' as MAPSAB, 1 as MAPSUBSETID, map_rank as MAPRANK, rownum as MAPID, ' ' as MAPSID,
    nci_code as FROMID, ' ' as FROMSID, nci_code as FROMEXPR, 'CODE' as FROMTYPE, ' ' as FROMRULE, ' ' as FROMRES,
    relationship_name as REL, ' ' as RELA,
    icd9_code as TOID, ' ' as TOSID, icd9_code as TOEXPR, 'CODE' as TOTYPE, ' ' as TORULE, ' ' as TORES,
    ' ' as MAPRULE, ' ' as MAPRES, ' ' as MAPTYPE, ' ' as ATN, ' ' as ATV
    from t_$$_best_out;

EOF
if ($status != 0) then
    echo "ERROR creating tables"      
    exit 1
endif


echo "    Dump mappings to file... `/bin/date`"
$MEME_HOME/bin/dump_mid.pl -t t_$$_out_mrmap $db .
if ($status != 0) then
    echo "ERROR dumping t_$$_out_mrmap"
    exit 1
endif

$MEME_HOME/bin/dump_mid.pl -t t_$$_out_best_mrmap $db .
if ($status != 0) then
    echo "ERROR dumping t_$$_out_best_mrmap"
    exit 1
endif



echo "--------------------------------------------------------------"
echo "Done... `/bin/date`"
echo "--------------------------------------------------------------"

