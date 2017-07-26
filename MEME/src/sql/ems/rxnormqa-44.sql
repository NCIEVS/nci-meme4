# 44. Non-chemical ingredient concepts.

SELECT concept_id_2 as concept_id FROM relationships    
WHERE relationship_attribute IN ('ingredient_of','precise_ingredient_of')
  AND source  like 'RXNORM%'
  AND relationship_level = 'S'
MINUS
SELECT concept_id FROM attributes
WHERE attribute_name ='SEMANTIC_TYPE'
  AND attribute_value IN
   (SELECT semantic_type
    FROM semantic_types WHERE is_chem='Y');

