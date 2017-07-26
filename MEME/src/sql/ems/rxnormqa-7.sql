# 7. SCD Concepts where at least one ingredient has a
#    TRD lexical tag.  These cases should be SBDs
#    (eventually).

SELECT DISTINCT a.concept_id
FROM classes a, relationships b,
  (select concept_id_1 as scdc_id
   from relationships a, classes b, attributes c
   where relationship_attribute='ingredient_of'
     and atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
     and b.termgroup like 'RXNORM%/SCDC'
     and b.tobereleased in ('Y','y')
     and a.concept_id_2 = c.concept_id
     and c.attribute_name='LEXICAL_TAG'
     and c.attribute_value='TRD') c
WHERE a.atom_id = b.atom_id_1
  AND relationship_attribute = 'constitutes'
  AND a.source  like 'RXNORM%'
  AND a.termgroup  like 'RXNORM%/SCD' -- not SBD!
  AND a.tobereleased in ('Y','y')
  AND concept_id_2 = c.scdc_id;


