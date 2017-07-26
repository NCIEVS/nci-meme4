# 34.  Component Merges: Concepts containing component atoms with 
#      different case-insensitve strings. 

SELECT concept_id FROM classes
WHERE source like 'RXNORM%' AND termgroup like 'RXNORM%/SCDC'
  AND tobereleased IN ('Y','y')
GROUP BY concept_id HAVING count(distinct isui)>1;


