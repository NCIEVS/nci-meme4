WHENEVER SQLERROR EXIT -2
alter session set sort_area_size=200000000;
alter session set hash_area_size=200000000;

-- Save termgroup_rank
exec MEME_UTILITY.drop_it('table', 'tbac_tr_bak');
create table tbac_tr_bak as
select * from termgroup_rank;

-- Set NCI,PDQ sources lower, compute ranks

-- Rank UMLS sources above NCI above others

-- Start by finding atoms with UMLSCUI attributes
exec MEME_UTILITY.drop_it('table', 'tbac_mth');
create table tbac_mth as select atom_id from attributes
where attribute_name||''='UMLSCUI'
and source = (select current_name from sourcE_version where source='MTH');

-- Acquire the source list rank it as 2
exec MEME_UTILITY.drop_it('table', 'tbac');
create table tbac as select distinct a.source, 2 flag
from classes a
where atom_id in (select atom_id from tbac_mth);

-- Pick up any other related SABs
insert into tbac select distinct a.source, 2
from source_rank a, tbac b, source_rank c
where a.stripped_source = c.stripped_source and b.source = c.source;

-- Remove NCI sources
delete from tbac where source in (select source from source_rank where source_family='NCI');

-- Add NCI back as 1
insert into tbac select source, 1
from source_rank where source_family='NCI';
insert into tbac select 'NCIMTH', 1 from dual;

-- Add all others as zero
insert into tbac select source, 0
from source_rank where source not in (select source from tbac);

exec MEME_UTILITY.drop_it('table', 'tbac_tr_new');
create table tbac_tr_new as select * from termgroup_rank where 1=0;
insert into tbac_tr_new
select  termgroup, rank, rownum release_rank, notes, suppressible, tty
from
(select termgroup, rank, release_rank, notes, suppressible, tty
from
(select distinct termgroup, rank, release_rank, notes, suppressible, tty, flag
 from termgroup_rank a, tbac b
 where substr(a.termgroup,1,instr(a.termgroup,'/')-1) = b.source)
order by flag, release_rank
);
truncate table termgroup_rank;
insert into termgroup_rank select * from tbac_tr_new;
commit;
--exec meme_ranks.set_ranks(classes_flag=>'Y', relationships_flag=>'N', attributes_flag=>'N');

-- Reset lrc, lac
update classes set last_assigned_cui = last_release_cui
where nvl(last_assigned_cui,'n') != nvl(last_releasE_cui,'n');
commit;

-- Fix max tab
update max_tab set max_id =
(select to_number(substr(max(last_release_cui),3)) from classes)
where table_name in ('CUI','TCUI');
commit;

-- go!
exec dbms_output.put_line( meme_operations.assign_cuis('NCIMTH',0,'EMPTY_TABLE','Y','Y','Y'));

-- Restore termgroup ranks and rerun set_ranks
truncate table termgroup_rank;
insert into termgroup_rank select * from tbac_tr_bak;
--exec meme_ranks.set_ranks(classes_flag=>'Y', relationships_flag=>'N', attributes_flag=>'N');

-- Remap MTH CUI-CUI rels based on the release ranking (Because this is how the next update will work)
--   Obtain rels
--   Remap
--   Identify changes
--   Replace changes
exec MEME_UTILITY.drop_it('table', 't1');
create table t1 as select * from relationships
where sg_type_1='CUI' and sg_type_2='CUI' and tobereleased in ('Y','y');
alter table t1 modify atom_id_1 null;
alter table t1 modify atom_id_2 null;
exec meme_source_processing.map_sg_fields_all('t1','Y','Y','Y');
delete from t1 where atom_id_1 is null or atom_id_2 is null;

exec MEME_UTILITY.drop_it('table', 't2');
create table t2 as select a.relationship_id
from relationships a, t1 b
where a.relationship_id = b.relationship_id
  and a.atom_id_1||a.atom_id_2 != b.atom_id_1||b.atom_id_2;
delete from relationships where relationship_id in (select relationship_id from t2);
insert into relationships select * from t1
where relationship_id in (select relationship_id from t2);


exec MEME_UTILITY.drop_it('table', 'tbac_tr_bak');












