-- Clean up data
delete dead_concept_status where concept_id > 99 and concept_id<202;
delete concept_status where concept_id > 99 and concept_id < 202;
delete atoms where atom_id > 99 and atom_id < 202;
delete classes where concept_id > 99 and concept_id < 202;
delete classes where atom_id > 99 and atom_id < 202;
delete attributes where concept_id > 99 and concept_id < 202;
delete relationships where concept_id_1 > 99 and concept_id_2 < 202;
-- generate data
@gen_qa_data.sql
-- commit
commit;
