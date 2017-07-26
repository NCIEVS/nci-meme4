# 48. Concepts with split RXCUI values

select /*+ INDEX(a,x_attr_an) */ 
  distinct a.concept_id as concept_id_1, b.concept_id as concept_id_2
from attributes a, attributes b, classes c
where a.concept_id != b.concept_id
  and a.attribute_name='RXCUI'
  and b.attribute_name='RXCUI'
  and a.tobereleased not in ('N','n')
  and b.tobereleased not in ('N','n')
  and a.attribute_value=b.attribute_value
  and a.concept_id = c.concept_id
  and c.source = (select current_name from source_version where source='RXNORM')
  and c.tobereleased not in ('N','n');
