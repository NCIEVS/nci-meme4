# 15. SCD/SBD Concepts with 'Solid' dose forms expressed as "ML".

SELECT DISTINCT a.concept_id
FROM classes a, atoms b
WHERE a.source  like 'RXNORM%'
  AND a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND a.tobereleased in ('Y','y')
  AND a.atom_id = b.atom_id
  AND atom_name like '%ML %'
  AND a.atom_id in
    (select atom_id_1 from relationships where
     relationship_attribute = 'dose_form_of' and
     atom_id_2 in
        (select d.atom_id from classes a, context_relationships b,
                        atoms c, classes d
  where a.source like 'HL7%' and a.termgroup like 'HL7%/DF'
    and a.atom_id = c.atom_id
    and b.parent_treenum like '%.'||a.aui||'.%'
    and c.atom_name IN ('Solid','Patch','Pad')
    and atom_id_1 = d.atom_id
    and d.source like 'HL7%' and d.termgroup like 'HL7%/DF'));


