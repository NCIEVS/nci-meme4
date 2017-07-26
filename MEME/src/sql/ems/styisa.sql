# concepts with more than 1 STY where one is an ancestor of the other
# suresh@nlm.nih.gov

SELECT DISTINCT a1.concept_id, rownum as cluster_id FROM attributes a1, attributes a2, SRSTRE2 s
WHERE  a1.concept_id=a2.concept_id
AND    a1.attribute_name = 'SEMANTIC_TYPE'
AND    a2.attribute_name = 'SEMANTIC_TYPE'
AND    a1.attribute_value != a2.attribute_value
AND    a1.attribute_value = s.sty1
AND    a2.attribute_value = s.sty2
AND    s.rel='isa'

