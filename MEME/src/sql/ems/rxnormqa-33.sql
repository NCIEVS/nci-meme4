# 33.  Concepts with branded ingredients in the same concept as as other
#      non-branded-ingredient RXNORM atoms.
     
SELECT DISTINCT a.concept_id FROM classes a, classes b
WHERE a.tobereleased in ('Y','y')
  AND a.source like 'RXNORM%'
  AND a.tty='BN'
  AND a.concept_id = b.concept_id
  AND b.tobereleased in ('Y','y')
  AND b.source like 'RXNORM%' AND b.tty !='BN';


