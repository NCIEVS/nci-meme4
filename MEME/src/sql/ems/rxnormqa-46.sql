# 46. Concepts with releasable SCD/SBD and RxNorm status H.

select distinct a.concept_id from classes a, attributes b
 where a.concept_id = b.concept_id
  and a.source  like 'RXNORM%'
  and tty in ('SCD','SBD')
  and attribute_name = 'RX_NORM_STATUS'
  and attribute_value = 'U'
  and a.tobereleased in ('Y','y');
