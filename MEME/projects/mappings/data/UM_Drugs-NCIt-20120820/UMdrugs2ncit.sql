!cat druglist.txt | $LVG_HOME/bin/luiNorm -t:2 >x

!perl -pe 's/$/\|/' x > druglist.norm



drop table tjfw_in;
create table tjfw_in as
select atom_name as original_name, atom_name as norm_name from atoms where 1=2;

!dump_mid.pl -t tjfw_in memestg .

!/bin/mv druglist.norm tjfw_in.dat

!sqlldr mth/umls_tuttle@memestg -control=tjfw_in.ctl


--matched normed versions of input strings to LUIs in Meta
drop table tjfw;
create table tjfw as 
select original_name,norm_name, lui from tjfw_in t
left outer join 
(select distinct lui, norm_string from string_ui) s
on t.norm_name = s.norm_string;

--match those found LUIs to NCI codes
drop table tjfw2;
create table tjfw2 as
select t.original_name,t.lui,cl.code,cl.atom_name,cl.atom_id,cl.tty,cl.concept_id,10 as rank from tjfw t
left outer join
(select distinct c.concept_id,c.atom_id,code,tty,lui,atom_name from classes c, atoms a
 where c.source=
    (select current_name from source_version where source='NCI')
 and tobereleased in ('Y','y')
 and c.atom_id = a.atom_id) cl
on t.lui = cl.lui;

--grab other synonyms in the same concept_id
insert into tjfw2
select distinct t.original_name,t.lui,c.code,a.atom_name,a.atom_id,c.tty,t.concept_id,10 as rank 
from tjfw2 t, classes c, atoms a
where c.concept_id = t.concept_id
and c.code = t.code
and c.source=
    (select current_name from source_version where source='NCI')
and tobereleased in ('Y','y')
and c.atom_id = a.atom_id
and nvl(c.atom_id,0) not in
   (select nvl(atom_id,0) from tjfw2);

update tjfw2 set rank=2 where tty is not null;
update tjfw2 set rank=1 where tty='PT';

-- grab unmatched LUIs, connect to Meta concept_ids
drop table tjfw3;
create table tjfw3 as
select t.original_name,t.lui,t.code,t.atom_name,t.tty,cl.concept_id,t.rank from 
(select * from tjfw2 where rank = 10) t
left outer join
(select distinct concept_id,lui from classes where tobereleased in ('Y','y')) cl
on t.lui = cl.lui;

--find NCI atoms in those concept_ids
drop table tjfw3_nci;
create table tjfw3_nci as
select a.atom_name, c.atom_id, c.concept_id, c.code, c.tty from classes c, atoms a
where a.atom_id = c.atom_id
and source=(select current_name from source_version where source='NCI')
and tobereleased in ('Y','y')
and c.atom_id = a.atom_id
and concept_id in (select concept_id from tjfw3);

--pair unmatched LUIs with NCI atoms via concept_id
drop table tjfw4;
create table tjfw4 as
select t3.original_name,t3.lui,t3n.code,t3n.atom_name,t3n.atom_id,t3n.tty,t3.concept_id, t3.rank 
from tjfw3 t3
left outer join
tjfw3_nci t3n
on t3.concept_id = t3n.concept_id;
update tjfw4 set rank=4 where tty is not null;
update tjfw4 set rank=3 where tty='PT';

--go back to the original match table and add the new entries
delete from tjfw2 where original_name in
(select original_name from tjfw4 where rank != 10);
insert into tjfw2 select * from tjfw4 where original_name in
(select original_name from tjfw4 where rank != 10);

--get rid of unmatched dupes
delete from tjfw2 where original_name in
(select original_name from tjfw2 where rank!=10)
and rank=10;

drop table tjfw_out;
create table tjfw_out as
select original_name,code,atom_name,tty,rank from tjfw2 
where rank != 10 order by original_name,rank;
insert into tjfw_out 
select original_name,code,atom_name,tty,rank from tjfw2 
where rank = 10 order by original_name;

!dump_mid.pl -t tjfw_out memestg .

