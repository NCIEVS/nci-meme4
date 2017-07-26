# 39.  Concepts containing normal form atoms with different case-sensitive
#      strings (SUI).

SELECT concept_id FROM classes
WHERE source like 'RXNORM%'
  AND termgroup like 'RXNORM%/SCD'
  AND tobereleased IN ('Y','y')
GROUP BY concept_id HAVING count(distinct sui)>1;


