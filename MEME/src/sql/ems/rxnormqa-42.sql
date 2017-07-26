# 42. Snomed CT Clinical drugs w/out RXNORM Forms that are not
#     veterinary drugs.

SELECT a.concept_id from classes a, attributes b
WHERE a.source like 'SNOMEDCT%'
  AND a.tobereleased IN ('Y','y')
  AND a.concept_id = b.concept_id
  AND attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
  AND b.tobereleased in ('Y','y')
MINUS
(SELECT concept_id FROM classes
 WHERE source  like 'RXNORM%'
   AND termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   AND tobereleased IN ('Y','y')
 UNION
 SELECT concept_id FROM context_relationships a, classes b
 WHERE a.relationship_name = 'PAR' AND a.source like 'SNOMEDCT%'
   AND atom_id_1 = atom_id
   AND parent_treenum like 
     (SELECT '%.'||b.aui||'%' FROM classes b, atoms c
      WHERE c.atom_name = 'Veterinary proprietary drug AND/OR biological'
        AND b.atom_id = c.atom_id
        AND b.source like 'SNOMEDCT%'
        AND b.tobereleased in ('Y','y')));


