
delete from string_ui where sui = 'S0000000';
insert into string_ui 
  (lui, sui, string_pre, norm_string_pre,language, base_string,
	string, norm_string, isui, lowercase_string_pre)
values
  ('L0000000','S0000000','Test atom','atom test','ENG','N','Test atom',
	'atom test','I0000000','test atom');

insert into classes
 (atom_id, version_id, source, termgroup, tty, termgroup_rank, code,
  sui, lui, generated_status, last_release_cui, dead, status,
  authority, timestamp, insertion_date, concept_id, released,
  tobereleased, last_molecule_id, last_atomic_action_id,
  sort_key, rank, last_release_rank, suppressible, 
  last_assigned_cui, isui, aui, source_aui, source_cui, source_dui,
  language)
select rownum+99, 0, 'MTH','MTH/PN','PN',b.rank,'NOCODE',
  'S0000000','L0000000','Y','','N','R',
  'L-QA',sysdate,sysdate,rownum+99,'N',
  'N',0,0,
  0,to_number('9'||lpad(b.release_rank,4,0)||'0'),0,'N',
  '','I0000000','A0000000','','','','ENG' 
from mid_validation_queries a, termgroup_rank b 
where rownum<101 and b.termgroup = 'MTH/PN';

delete from atoms_ui where aui = 'A0000000';
insert into atoms_ui (aui, sui, stripped_source ,tty, code,
  source_aui, source_cui, source_dui)
select distinct aui, sui, source, tty, code, 
  source_aui, source_cui, source_dui
from classes where atom_id < 200;

insert into atoms (atom_id, version_id, atom_name)
select rownum+99,0,'Test atom' 
from mid_validation_queries
where rownum<101;

insert into concept_status
select rownum+99,'',0,'N','N','L-MEME4',sysdate,sysdate,rownum+99,'A','N',0,0,9,'L-QA',sysdate,0 
from mid_validation_queries
where rownum<101;
