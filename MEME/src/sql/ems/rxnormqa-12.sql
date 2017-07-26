# 12. SCD/SBD concepts with the same components but different
#     dose forms where the dose forms are siblings in the HL7
#     hierarchy.  This returns only multiple-component cases.

select distinct concept_id_1,concept_id_2 from
(SELECT a.concept_id as concept_id_1, b.concept_id as concept_id_2
FROM
(select /*+ RULE */ distinct a.concept_id, b.atom_id+0 as df_id, a.termgroup,
   lower(substr(c.atom_name,0,instr(lower(c.atom_name),lower(d.atom_name))-2)) as str
 from classes a, classes b, atoms c, atoms d, relationships e
 where a.tobereleased in ('Y','y')
   and a.source  like 'RXNORM%'
   and a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   and b.source like 'HL7%'
   and b.termgroup like 'HL7%/DF'
   and a.atom_id = c.atom_id
   and b.atom_id = d.atom_id
   and a.atom_id = atom_id_1
   and b.atom_id = atom_id_2
   and relationship_attribute = 'dose_form_of') a,
(select /*+ RULE */ distinct a.concept_id, b.atom_id+0 as df_id, a.termgroup,
   lower(substr(c.atom_name,0,instr(lower(c.atom_name),lower(d.atom_name))-2)) as str
 from classes a, classes b, atoms c, atoms d, relationships e
 where a.tobereleased in ('Y','y')
   and a.source  like 'RXNORM%'
   and a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   and b.source like 'HL7%'
   and b.termgroup like 'HL7%/DF'
   and a.atom_id = c.atom_id
   and b.atom_id = d.atom_id
   and a.atom_id = atom_id_1
   and b.atom_id = atom_id_2
   and c.atom_name like '% / %'
   and relationship_attribute = 'dose_form_of') b,
   context_relationships c, context_relationships d
WHERE a.concept_id < b.concept_id
  AND a.str = b.str
  and a.df_id != b.df_id
  and a.termgroup = b.termgroup
  and a.df_id = c.atom_id_1
  and b.df_id = d.atom_id_1
  and c.parent_treenum = d.parent_treenum
union all
select 1,2 from dual where 1=0);


