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
echo "pid: $$"
echo ""

set u=`echo $user | cut -d\/ -f 1`


echo "    Create mappings ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1

    -- Get the Neoplasm NCI/PT (aui=A2662660) and its children.
    exec MEME_UTILITY.drop_it('table','t_$$_nci_neoplasms');
    create table t_$$_nci_neoplasms as
    select concept_id, code from classes where aui='A2662660'
    and tobereleased in ('y','Y')
    union
    select concept_id, code from classes where atom_id in
        (select atom_id_1 from context_relationships where parent_treenum like '%A2662660%')
    and tobereleased in ('y','Y');


    -- Get all SNOMEDCT and NCI atoms in the same CUI/concept as Neoplasm NCI/PTs.
    exec MEME_UTILITY.drop_it('table','t_$$_nci_sct');
    create table t_$$_nci_sct as
    select c1.atom_id as nci_id,c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty, c1.rank||lpad(c1.atom_id,10,0) as nci_rank,
             c2.atom_id  as snomedct_id, c2.concept_id as snomedct_cid, c2.code as snomedct_code, a2.atom_name as snomedct_name, c2.tty as snomedct_tty, c2.rank||lpad(c2.atom_id,10,0) as snomedct_rank
    from classes c1, classes c2, atoms a1, atoms a2
    where (c1.concept_id, c1.code) in
        (select concept_id,code from t_$$_nci_neoplasms)
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id
    and c1.concept_id = c2.concept_id
    and c2.source=(select current_name from source_version where source='SNOMEDCT')
    and c2.tobereleased in ('Y','y')
    and c2.atom_id = a2.atom_id;


    -- Create mapping table between SNOMEDCT and NCI CUIs and codes.

    exec MEME_UTILITY.drop_it('table','t_$$_nci_sct_map');
    create table t_$$_nci_sct_map as
    select a.nci_code, a.nci_cid, nci_name,
            'SY' as relationship_name,
             a.snomedct_code, a.snomedct_cid, snomedct_name,
             0 as is_nci_merge, 0 as is_nci_split, 0 as is_sct_merge, 0 as is_sct_split, 0 as has_both_pts,
             0 as map_rank
    from t_$$_nci_sct a;


    -- Create staging table for context rels.
    exec MEME_UTILITY.drop_it('table','t_$$_nci_sct_cxt');
    create table t_$$_nci_sct_cxt as
    select distinct a.concept_id as concept_id_1, r.source, 'RN' as relationship_name, b.concept_id as concept_id_2
    from classes a, classes b, context_relationships r
    where r.relationship_name !='SIB'
    and r.source in (select current_name from source_version where source in ('NCI','SNOMEDCT'))
    and atom_id_1 = a.atom_id
    and atom_id_2 = b.atom_id
    and a.concept_id in (select concept_id from t_$$_nci_neoplasms)
    and a.concept_id not in (select concept_id from classes where source=(select current_name from source_version where source='SNOMEDCT'))
    and b.concept_id in (select concept_id from classes where source=(select current_name from source_version where source='SNOMEDCT'))
    UNION
    select distinct b.concept_id as concept_id_1, r.source, 'RB' as relationship_name, a.concept_id as concept_id_2
    from classes a, classes b, context_relationships r
    where r.relationship_name !='SIB'
    and r.source in (select current_name from source_version where source in ('NCI','SNOMEDCT')) and atom_id_1 = a.atom_id and atom_id_2 = b.atom_id
    and b.concept_id in (select concept_id from t_$$_nci_neoplasms)
    and b.concept_id not in (select concept_id from classes where source=(select current_name from source_version where source='SNOMEDCT'))
    and a.concept_id in (select concept_id from classes where source=(select current_name from source_version where source='SNOMEDCT'));

    -- Create table for NCI-SNOMEDCT rels, seed with cxts
    exec MEME_UTILITY.drop_it('table','t_$$_nci_sct_rels');
    create table t_$$_nci_sct_rels as
    select c1.atom_id as nci_id,c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty, c1.rank||lpad(c1.atom_id,10,0) as nci_rank,
             tnsc.relationship_name,
             c2.atom_id  as snomedct_id, c2.concept_id as snomedct_cid, c2.code as snomedct_code, a2.atom_name as snomedct_name, c2.tty as snomedct_tty, c2.rank||lpad(c2.atom_id,10,0) as snomedct_rank
    from classes c1, classes c2, atoms a1, atoms a2, t_$$_nci_sct_cxt tnsc
    where (c1.concept_id, c1.code) in
        (select concept_id,code from t_$$_nci_neoplasms)
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id
    and c1.concept_id = tnsc.concept_id_1
    and c2.concept_id = tnsc.concept_id_2
    and c2.source=(select current_name from source_version where source='SNOMEDCT')
    and c2.tobereleased in ('Y','y')
    and c2.atom_id = a2.atom_id;

    delete from t_$$_nci_sct_rels where nci_code in (select nci_code from t_$$_nci_sct_map);

    -- Insert non-context rels
    insert into t_$$_nci_sct_rels
    select c1.atom_id as nci_id,c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty, c1.rank||lpad(c1.atom_id,10,0) as nci_rank,
             r.relationship_name,
             c2.atom_id  as snomedct_id, c2.concept_id as snomedct_cid, c2.code as snomedct_code, a2.atom_name as snomedct_name, c2.tty as snomedct_tty, c2.rank||lpad(c2.atom_id,10,0) as snomedct_rank
    from classes c1, classes c2, atoms a1, atoms a2, relationships r
    where c1.atom_id = a1.atom_id and c2.atom_id = a2.atom_id
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and (c1.concept_id,c1.code) in (select concept_id,code from t_$$_nci_neoplasms)
    and c2.source=(select current_name from source_version where source='SNOMEDCT')
    and c2.tobereleased in ('Y','y')
    and c1.concept_id != c2.concept_id
    and r.concept_id_1 = c1.concept_id and r.concept_id_2 = c2.concept_id
    and r.relationship_name in ('BT','NT','RT')
    and r.tobereleased in ('Y','y')
    and r.source in (select current_name from source_version where source in ('NCI','SNOMEDCT'))
    and c1.code not in (select nci_code from t_$$_nci_sct_rels);

    delete from t_$$_nci_sct_rels where nci_cid in (select nci_cid from t_$$_nci_sct_map);

    -- Insert rels into mapping table
    insert into t_$$_nci_sct_map
    select a.nci_code, a.nci_cid, nci_name,
             a.relationship_name,
             a.snomedct_code, a.snomedct_cid, snomedct_name,
             0 as is_nci_merge, 0 as is_nci_split, 0 as is_sct_merge, 0 as is_sct_split, 0 as has_both_pts,
             0 as map_rank
    from t_$$_nci_sct_rels a;

    -- Make all names SNOMEDCT/FN or NCI/PT only
    update t_$$_nci_sct_map tnsm
    set nci_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='NCI')
         and c.code = tnsm.nci_code
         and c.tty='PT'),
    snomedct_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='SNOMEDCT')
         and c.code = tnsm.snomedct_code
         and c.tty='FN');

    -- Set ranking conditions for all cases.
    update t_$$_nci_sct_map set is_nci_merge=1 where nci_cid in
        (select nci_cid from t_$$_nci_sct group by nci_cid having count(distinct nci_code)>1
         union
         select nci_cid from t_$$_nci_sct_rels group by nci_cid having count(distinct nci_code)>1);
    update t_$$_nci_sct_map set is_sct_merge=1 where snomedct_cid in
        (select snomedct_cid from t_$$_nci_sct group by snomedct_cid having count(distinct snomedct_code)>1
         union
         select snomedct_cid from t_$$_nci_sct_rels group by snomedct_cid having count(distinct snomedct_code)>1);
    update t_$$_nci_sct_map set is_nci_split=1 where nci_code in
        (select nci_code from t_$$_nci_sct group by nci_code having count(distinct nci_cid)>1
         union
         select nci_code from t_$$_nci_sct_rels group by nci_code having count(distinct nci_cid)>1);
    update t_$$_nci_sct_map set is_sct_split=1 where snomedct_code in
        (select snomedct_code from t_$$_nci_sct group by snomedct_code having count(distinct snomedct_cid)>1
         union
         select snomedct_code from t_$$_nci_sct_rels group by snomedct_code having count(distinct snomedct_cid)>1);
    update t_$$_nci_sct_map set has_both_pts=1
    where nci_cid in
        (select nci_cid from t_$$_nci_sct where nci_tty='PT' union select nci_cid from t_$$_nci_sct_rels where nci_tty='PT')
    and snomedct_cid in
        (select snomedct_cid from t_$$_nci_sct where snomedct_tty='FN' union select snomedct_cid from t_$$_nci_sct_rels where snomedct_tty='FN');

    update t_$$_nci_sct_map set map_rank = 2 - has_both_pts + is_nci_merge + is_nci_split + is_sct_merge + is_sct_split;


    -- Keep track of things that were not mapped.

    alter table t_$$_nci_sct_map modify nci_cid NULL;
    alter table t_$$_nci_sct_map modify nci_name NULL;
    alter table t_$$_nci_sct_map modify snomedct_cid NULL;
    alter table t_$$_nci_sct_map modify snomedct_name NULL;

    exec MEME_UTILITY.drop_it('table','t_$$_unmapped');
    create table t_$$_unmapped as
    select c1.atom_id as nci_id, c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty
    from classes c1, atoms a1
    where (c1.code) in
        (select code from t_$$_nci_neoplasms minus select nci_code from t_$$_nci_sct_map)
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id;

    update t_$$_unmapped tnsu
    set nci_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='NCI')
         and c.code = tnsu.nci_code
         and c.tty='PT');

    -- Make a table of unmapped NCI codes and their parents.
    exec MEME_UTILITY.drop_it('table','t_$$_unmapped_parents');
    create table t_$$_unmapped_parents as
    select distinct a.nci_id as atom_id_1, a.nci_cid as concept_id_1,a.nci_code as code,
    cr.relationship_name,
    b.atom_id as atom_id_2, b.concept_id as concept_id_2, b.code as parent_code
    from t_$$_unmapped a, classes b, context_relationships cr
    where cr.relationship_name = 'PAR'
    and cr.source in (select current_name from source_version where source ='NCI')
    and cr.atom_id_1 in (select atom_id from classes a1 where a1.concept_id = a.nci_cid)
    and cr.atom_id_2 = b.atom_id;

    -- Make a table of unmapped NCI codes and their grandparents.
    exec MEME_UTILITY.drop_it('table','t_$$_grandparent_rels');
    create table t_$$_grandparent_rels as
    select distinct a.concept_id_1 as concept_id_1, r.source, 'RN' as relationship_name, b.concept_id as concept_id_2
    from t_$$_unmapped_parents a, classes b, context_relationships r
    where r.relationship_name !='SIB'
    and r.source in (select current_name from source_version where source in ('NCI','SNOMEDCT'))
    and r.atom_id_1 = a.atom_id_2
    and r.atom_id_2 = b.atom_id
    and a.concept_id_2 not in (select concept_id from classes where source=(select current_name from source_version where source='SNOMEDCT'))
    and b.concept_id in (select concept_id from classes where source=(select current_name from source_version where source='SNOMEDCT'))
    UNION
    select distinct b.concept_id as concept_id_1, r.source, 'RB' as relationship_name, a.concept_id_1 as concept_id_2
    from t_$$_unmapped_parents a, classes b, context_relationships r
    where r.relationship_name !='SIB'
    and r.source in (select current_name from source_version where source in ('NCI','SNOMEDCT'))
    and r.atom_id_1 = a.atom_id_2
    and r.atom_id_2 = b.atom_id
    and b.concept_id in (select concept_id from t_$$_nci_neoplasms)
    and b.concept_id not in (select concept_id from classes where source=(select current_name from source_version where source='SNOMEDCT'))
    and a.concept_id_2 in (select concept_id from classes where source=(select current_name from source_version where source='SNOMEDCT'));

    -- Create a mapping table based upon grandparent concept's context relationships.
    exec MEME_UTILITY.drop_it('table','t_$$_gp_map');
    create table t_$$_gp_map as
    select c1.atom_id as nci_id,c1.concept_id as nci_cid, c1.code as nci_code, a1.atom_name as nci_name, c1.tty as nci_tty, c1.rank||lpad(c1.atom_id,10,0) as nci_rank,
             tnsgr.relationship_name,
             c2.atom_id  as snomedct_id, c2.concept_id as snomedct_cid, c2.code as snomedct_code, a2.atom_name as snomedct_name, c2.tty as snomedct_tty, c2.rank||lpad(c2.atom_id,10,0) as snomedct_rank,
             0 as is_nci_merge, 0 as is_nci_split, 0 as is_sct_merge, 0 as is_sct_split, 0 as has_both_pts,
             0 as map_rank
    from classes c1, classes c2, atoms a1, atoms a2, t_$$_grandparent_rels tnsgr
    where (c1.concept_id, c1.code) in
        (select concept_id,code from t_$$_nci_neoplasms)
    and c1.source=(select current_name from source_version where source='NCI')
    and c1.tobereleased in ('Y','y')
    and c1.atom_id = a1.atom_id
    and c1.concept_id = tnsgr.concept_id_1
    and c2.concept_id = tnsgr.concept_id_2
    and c2.source=(select current_name from source_version where source='SNOMEDCT')
    and c2.tobereleased in ('Y','y')
    and c2.atom_id = a2.atom_id;

    -- Make all names SNOMEDCT/FN or NCI/PT only
    update t_$$_gp_map tgp
    set nci_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='NCI')
         and c.code = tgp.nci_code
         and c.tty='PT'),
    snomedct_name =
        (select atom_name from atoms a, classes c
         where a.atom_id = c.atom_id
         and c.source=(select current_name from source_version where source='SNOMEDCT')
         and c.code = tgp.snomedct_code
         and c.tty='FN');

    -- Set map ranking criteria
    update t_$$_gp_map set is_nci_merge=1 where nci_cid in
        (select nci_cid from t_$$_gp_map group by nci_cid having count(distinct nci_code)>1);
    update t_$$_gp_map set is_sct_merge=1 where snomedct_cid in
        (select snomedct_cid from t_$$_gp_map group by snomedct_cid having count(distinct snomedct_code)>1);
    update t_$$_gp_map set is_nci_split=1 where nci_code in
        (select nci_code from t_$$_gp_map group by nci_code having count(distinct nci_cid)>1);
    update t_$$_gp_map set is_sct_split=1 where snomedct_code in
        (select snomedct_code from t_$$_gp_map group by snomedct_code having count(distinct snomedct_cid)>1);
    update t_$$_gp_map set has_both_pts=1
    where nci_cid in
        (select nci_cid from t_$$_gp_map where nci_tty='PT')
    and snomedct_cid in
        (select snomedct_cid from t_$$_gp_map where snomedct_tty='FN');

    update t_$$_gp_map set map_rank = 2 - has_both_pts + is_nci_merge + is_nci_split + is_sct_merge + is_sct_split;

    delete from t_$$_unmapped where nci_code in
        (select nci_code from t_$$_gp_map);

    insert into t_$$_nci_sct_map
    select a.nci_code, a.nci_cid, nci_name,
            '' as relationship_name,
             '' as snomedct_code, '' as snomedct_cid, '' as snomedct_name,
             0 as is_nci_merge, 0 as is_nci_split, 0 as is_sct_merge, 0 as is_sct_split, 0 as has_both_pts,
             0 as map_rank
    from t_$$_unmapped a;

    exec MEME_UTILITY.drop_it('table','t_$$_out');
    create table t_$$_out as select distinct nci_code,nci_name, relationship_name, map_rank, snomedct_code, snomedct_name from t_$$_nci_sct_map
    union
    select distinct nci_code,nci_name, relationship_name, map_rank, snomedct_code, snomedct_name from t_$$_gp_map
    order by map_rank asc;


    exec MEME_UTILITY.drop_it('table','t_$$_best_out');
    create table t_$$_best_out as
    select * from t_$$_out where map_rank||nci_code in
         (select min(map_rank)||nci_code from t_$$_out group by nci_code);


    exec MEME_UTILITY.drop_it('table','t_$$_out_mrmap');
    create table t_$$_out_mrmap as
    select 'NCI2SCT' as MAPSETCUI, 'NCIMTH' as MAPSAB, 1 as MAPSUBSETID, map_rank as MAPRANK, rownum as MAPID, ' ' as MAPSID,
    nci_code as FROMID, ' ' as FROMSID, nci_code as FROMEXPR, 'CODE' as FROMTYPE, ' ' as FROMRULE, ' ' as FROMRES,
    relationship_name as REL, ' ' as RELA,
    snomedct_code as TOID, ' ' as TOSID, snomedct_code as TOEXPR, 'CODE' as TOTYPE, ' ' as TORULE, ' ' as TORES,
    ' ' as MAPRULE, ' ' as MAPRES, ' ' as MAPTYPE, ' ' as ATN, ' ' as ATV
    from t_$$_out;

    exec MEME_UTILITY.drop_it('table','t_$$_out_best_mrmap');
    create table t_$$_out_best_mrmap as
    select 'NCI2SCT' as MAPSETCUI, 'NCIMTH' as MAPSAB, 1 as MAPSUBSETID, map_rank as MAPRANK, rownum as MAPID, ' ' as MAPSID,
    nci_code as FROMID, ' ' as FROMSID, nci_code as FROMEXPR, 'CODE' as FROMTYPE, ' ' as FROMRULE, ' ' as FROMRES,
    relationship_name as REL, ' ' as RELA,
    snomedct_code as TOID, ' ' as TOSID, snomedct_code as TOEXPR, 'CODE' as TOTYPE, ' ' as TORULE, ' ' as TORES,
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

