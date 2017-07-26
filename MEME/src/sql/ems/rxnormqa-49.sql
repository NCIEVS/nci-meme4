# 49. Concepts with merged RXNORM primary or ingredient atom TTYs.

select concept_id
from classes where source = (select current_name
from sourcE_version where source='RXNORM')
and tty in ('SCD','SBD','SCDC','SBDC','SCDF','SBDF','BN','IN')
and tobereleased in ('Y','y')
group by concept_id having count(distinct tty)>1;
