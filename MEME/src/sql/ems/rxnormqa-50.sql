# 06/15/2006 BAC (1-BHMK1): changes requested by TPW
# 50. RXNORM primary atom with non-matching MTH/PN

select distinct a.concept_id
from classes a, classes b
where a.source = (select current_name
from source_version where source='RXNORM')
and b.source='MTH' and b.tty='PN'
and a.tty in ('SCD','SBD','SCDC','SBDC','SCDF','SBDF')
and a.tobereleased in ('Y','y')
and b.tobereleased in ('Y','y')
and a.isui != b.isui
and a.concept_id=b.concept_id;