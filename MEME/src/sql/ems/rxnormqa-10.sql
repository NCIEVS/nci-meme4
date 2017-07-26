# 10. SCD, SBD, or SCD concept without a 'Clinical Drug' semantic type.
#     Most look like they have 'Drug Delivery Device' and as we
#     understand it, concepts with that STY should not have SCD atoms.

SELECT distinct concept_id FROM classes
WHERE source  like 'RXNORM%' 
  AND termgroup IN 
    (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD','SCDC'))
  AND tobereleased in ('Y','y')
MINUS
SELECT distinct concept_id FROM attributes
WHERE attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
  AND tobereleased in ('Y','y');


