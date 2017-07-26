# Concepts multiple semantic types where atleast one of the semantic types is 'GENE OR GENOME'
# Created for OMIM

SELECT DISTINCT a1.concept_id FROM attributes a1, attributes a2
WHERE  a1.concept_id=a2.concept_id
AND    a1.attribute_name ='SEMANTIC_TYPE'
AND    a1.attribute_value='Gene or Genome'
AND    a2.attribute_name ='SEMANTIC_TYPE'
AND    a2.attribute_value !='Gene or Genome'
AND    a1.attribute_value!=a2.attribute_value
