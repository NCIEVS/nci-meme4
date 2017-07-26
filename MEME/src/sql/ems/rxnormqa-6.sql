# 6. Ingredient concepts without a level 0 source.

SELECT concept_id_2 as concept_id
FROM relationships a, classes b
WHERE relationship_attribute like '%ingredient_of'
  AND atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
  AND b.termgroup like 'RXNORM%/SCDC'
  AND b.tobereleased in ('Y','y')
MINUS
SELECT concept_id FROM classes a, source_rank b
WHERE restriction_level = 0
  AND a.source = b.source;


