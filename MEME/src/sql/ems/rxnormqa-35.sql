# 35.  Component Splits: Concepts containing component atoms 
#      with the same case-insensitive string as a component atom
#      in a different concept.

SELECT a.concept_id FROM classes a, classes b
WHERE a.concept_id != b.concept_id
  AND a.isui = b.isui
  AND a.source like 'RXNORM%'
  AND a.termgroup like 'RXNORM%/SCDC'
  AND a.tobereleased IN ('Y','y')
  AND b.source like 'RXNORM%'
  AND b.termgroup like 'RXNORM%/SCDC'
  AND b.tobereleased IN ('Y','y');


