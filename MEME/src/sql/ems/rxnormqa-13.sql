# 13. Ingredient concepts without 'Pharmacologic Substance'
#     semantic types.

SELECT DISTINCT concept_id_2 as concept_id
FROM relationships a, classes b
WHERE relationship_attribute like '%ingredient_of'
  AND atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
  AND b.termgroup like 'RXNORM%/SCDC'
  AND b.tobereleased in ('Y','y')
MINUS
SELECT concept_id FROM attributes
WHERE attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value in ('Pharmacologic Substance','Antibiotic')
  AND tobereleased in ('Y','y');


