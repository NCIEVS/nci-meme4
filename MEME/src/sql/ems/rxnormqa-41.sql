# 41. SNOMEDCT ingredient concepts without MTH/PN.

SELECT concept_id_2 as concept_id
FROM relationships a, classes b
WHERE a.source like 'RXNORM%' and a.tobereleased in ('Y','y')
  AND a.relationship_attribute in ('ingredient_of','precise_ingredient_of')
  AND concept_id_2 = b.concept_id
  AND b.source like 'SNOMEDCT%' AND b.tobereleased in ('Y','y')
MINUS
SELECT concept_id FROM classes
WHERE source='MTH' AND tty='PN'
 AND tobereleased in ('Y','y');


