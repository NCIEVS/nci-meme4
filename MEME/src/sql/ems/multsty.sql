# concepts with more than 1 STYs where at least one is non-chemical
# suresh@nlm.nih.gov

select concept_id, rownum as cluster_id from (
 select concept_id from attributes
  where attribute_name = 'SEMANTIC_TYPE' and
  attribute_value in (select semantic_type from semantic_types)
  group by concept_id having count(distinct attribute_value) > 2
 intersect
select distinct concept_id from attributes 
where attribute_name = 'SEMANTIC_TYPE' and attribute_value in (select semantic_type from semantic_types where is_chem='N')
)

#select a.concept_id, rownum as cluster_id from
# (select concept_id from attributes
#  where attribute_name ='SEMANTIC_TYPE'
#  group by concept_id having count(distinct attribute_value)>2) a,
# (select distinct concept_id from attributes at, semantic_types sty
#  where attribute_name ='SEMANTIC_TYPE'
#    and attribute_value = semantic_type
#    and is_chem = 'N') b
#where a.concept_id = b.concept_id
