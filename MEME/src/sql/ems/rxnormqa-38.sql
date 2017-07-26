# 38.  Normal form merges: Concepts containing normal form atoms with 
#      different case-insensitive strings (ISUI).

SELECT concept_id FROM classes
WHERE source like 'RXNORM%'
  AND termgroup like 'RXNORM%/SCD'
  AND tobereleased in ('Y','y')
GROUP BY concept_id HAVING count(distinct isui)>1;


