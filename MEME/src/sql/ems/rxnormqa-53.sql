# 53. Concepts with merged SCUI, SBD/SBD, SBDF/SBDF, or SBDC/SBDC

select distinct a.concept_id
from classes a, classes b
where a.source = (select current_name
from source_version where source='RXNORM')
  and a.source=b.source
  and a.tty in ('SBD','SBDF','SBDC')
  and b.tty = a.tty
  and a.tobereleased in ('Y','y')
  and b.tobereleased in ('Y','y')
  and a.concept_id = b.concept_id
  and a.source_cui < b.source_cui;
