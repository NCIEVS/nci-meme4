# 47. Concepts with merged RXCUI values

select /*+ INDEX(a,x_attr_an) */ distinct a.concept_id
from attributes a, attributes b
where a.concept_id=b.concept_id
  and a.attribute_name='RXCUI'
  and b.attribute_name='RXCUI'
  and a.tobereleased not in ('N','n')
  and b.tobereleased not in ('N','n')
  and a.attribute_value != b.attribute_value;
