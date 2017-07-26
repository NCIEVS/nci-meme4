# 40.  Normal form splits: concepts containing normal form atoms with the same
#      case-insensitive string as normal form atoms in other concepts.

SELECT a.concept_id FROM classes a, classes b
WHERE a.concept_id != b.concept_id
  AND a.isui = b.isui and a.source like 'RXNORM%'
  AND a.termgroup like 'RXNORM%/SCD'
  AND a.tobereleased IN ('Y','y')
  AND b.source like 'RXNORM%'
  AND b.termgroup like 'RXNORM%/SCD'
  AND b.tobereleased IN ('Y','y');


