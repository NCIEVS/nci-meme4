# 48. Concepts with split RXCUI values

select b.concept_id, row_num from (
  select /*+ INDEX(a,x_attr_an) */
    distinct a.concept_id as c1, b.concept_id as c2, rownum as row_num
  from attributes a, attributes b, classes c
  where a.concept_id != b.concept_id
    and a.attribute_name='RXCUI'
    and b.attribute_name='RXCUI'
    and a.tobereleased not in ('N','n')
    and b.tobereleased not in ('N','n')
    and a.attribute_value=b.attribute_value
    and a.concept_id = c.concept_id
    and c.source = (select current_name from source_version where source='RXNORM')  and c.tobereleased not in ('N','n')
) a, concept_status b where b.concept_id in (c1, c2) order by row_num