# Concepts with STY "Clinical Drug" and another STY != "Medical Device"
# powell@nlm.nih.gov 6/99 (proposed)
# suresh@nlm.nih.gov 6/99 (implemented)
# Oracle port - suresh 8/00
# suresh@nlm.nih.gov - EMS-3

SELECT DISTINCT a1.concept_id FROM attributes a1, attributes a2
WHERE  a1.concept_id=a2.concept_id
AND    a1.attribute_name ='SEMANTIC_TYPE'
AND    a1.attribute_value='Clinical Drug'
AND    a2.attribute_name ='SEMANTIC_TYPE'
AND    a2.attribute_value!='Medical Device'
AND    a1.attribute_value!=a2.attribute_value
