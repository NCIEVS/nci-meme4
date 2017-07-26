# 31. Concepts with normal forms but no Clinical Drug atoms.
#     These are concepts that should either be removed
#     or merged with other concepts.

SELECT concept_id FROM classes 
WHERE source like 'RXNORM%' AND termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND tobereleased IN ('Y','y') 
MINUS
SELECT concept_id FROM classes 
WHERE (source not like 'RXNORM%' OR tty in ('OBD','OCD'))
  AND tobereleased in ('Y','y')


