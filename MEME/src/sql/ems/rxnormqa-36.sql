# 36.  Concepts with ingredients in the same concept as other 
#      non-ingredient RXNORM atoms.

SELECT distinct a.concept_id FROM classes a, classes b
WHERE a.tobereleased IN ('Y','y')
  AND a.source like 'RXNORM%'
  AND a.tty='IN'
  AND a.concept_id = b.concept_id
  AND b.tobereleased IN ('Y','y')
  AND b.source like 'RXNORM%'
  AND b.tty !='IN';


