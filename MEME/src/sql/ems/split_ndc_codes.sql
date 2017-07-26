# Identify cases where NDC codes are split across UMLS concepts.

WITH duplicates AS
(select attribute_value, rownum as row_num from
    (select attribute_value from attributes where attribute_name='NDC'
     and tobereleased in ('Y','y')
     group by attribute_value having count(distinct concept_id)>1))
select distinct concept_id, row_num as cluster_id
from attributes a, duplicates b where attribute_name='NDC'
and tobereleased in ('Y','y')
and a.attribute_value = b.attribute_value
order by cluster_id;
