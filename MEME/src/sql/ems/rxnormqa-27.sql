# 27. Identify concepts with both rxnorm status of U and H

SELECT concept_id
FROM attributes 
WHERE attribute_name='RX_NORM_STATUS'
  AND attribute_value in ('H','U')
GROUP BY concept_id HAVING count(distinct attribute_value) >1;


