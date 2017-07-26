drop table tjfw;
drop table tjfw2;
drop table tjfw3;
drop table tjfw4;
drop table tjfw5;
drop table tjfw_out_match;
drop table tjfw_out_nomatch;

create table tjfw as select * from attributes where attribute_name='Accepted_Therapeutic_Use_For';

create table tjfw2 as 
select a.atom_name as drug_name, c.code as drug_code, t.attribute_value as atv
from tjfw t, classes c, atoms a
where t.atom_id = c.atom_id and c.atom_id = a.atom_id;

alter table tjfw2 modify (atv VARCHAR2(3000));

update tjfw2 set atv =
  (select text_value from stringtab where string_id = substr(atv,20))
where atv like '<>Long_Attribute<>:%';

create table tjfw3 as
select drug_name,drug_code,atv,s.lui from tjfw2 t
left outer join (select * from string_ui where language='ENG') s
on atv=string;


--(117 matches)

create table tjfw4 as
select drug_name,drug_code,atv,t.lui,c.code as icd10_code, c.atom_name as icd10_name from tjfw3 t
left outer join
(select code,atom_name,lui from classes c, atoms a
 where c.atom_id = a.atom_id and c.source=(select current_name from source_version where source='ICD10')) c
on t.lui = c.lui;


create table tjfw5 as 
select a.drug_name,a.drug_code,a.atv,a.lui, b.code as icd10_code, b.atom_name as icd10_name from
(select * from tjfw4 where lui is not null and icd10_code is null) a
left outer join
(select distinct c2.code,a.atom_name,c1.lui from classes c1, classes c2, atoms a
 where c2.atom_id = a.atom_Id and c1.concept_id = c2.concept_id
  and c2.source=(select current_name from source_version where source='ICD10')) b
on a.lui = b.lui;


!dump_mid.pl -t tjfw_out_match memestg .
!dump_mid.pl -t tjfw_out_nomatch memestg .

delete from tjfw4 where icd10_code is null and lui is not null;
insert into tjfw4 select * from tjfw5;

create table tjfw_out_match as select distinct drug_name,drug_code,icd10_code,icd10_name, atv
from tjfw4 where icd10_code is not null order by drug_name;

create table tjfw_out_nomatch as select distinct drug_name,drug_code,icd10_code,icd10_name, atv
from tjfw4 where icd10_code is null order by drug_name;
