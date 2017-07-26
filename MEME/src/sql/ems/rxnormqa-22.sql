# 22. Clinical Drugs without RxNorm Forms (MMSL).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'MMSL%'
  AND attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(
  SELECT concept_id from classes
    WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
  UNION
  SELECT concept_id_1 as concept_id FROM relationships
    WHERE relationship_level = 'S'
    AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
    AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
      AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
  UNION
  SELECT concept_id_2 as concept_id FROM relationships
    WHERE relationship_level = 'S'
    AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
    AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
      AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
)
