# 20.  Concepts with illegal dose forms.

SELECT DISTINCT concept_id_1 as concept_id
FROM relationships
WHERE relationship_attribute='dose_form_of'
  AND atom_id_2 in
    (SELECT a.atom_id from classes a, attributes b
     WHERE a.source like 'HL7%'
       AND a.termgroup like 'HL7%/DF'
       AND a.atom_id = b.atom_id
       AND attribute_name = 'SOS'
       AND attribute_value = 'Not to be used');


