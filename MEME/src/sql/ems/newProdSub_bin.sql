# SNOMED product and substance with same name not merged
# bcarlsen@msdinc.com

select distinct c1.concept_id as concept_id_1, c2.concept_id as concept_id_2
from classes c1, classes c2, atoms a1, atoms a2 where
c1.atom_id = a1.atom_id and
c2.atom_id = a2.atom_id and
c1.source= (select current_name from source_version where source='SNOMEDCT_US') and
c1.tty='FN' and
c1.tobereleased in ('Y', 'y') and
c2.source= (select current_name from source_version where source='SNOMEDCT_US') and
c2.tty='FN' and
c2.tobereleased in ('Y', 'y') and
c1.CONCEPT_ID <> c2.CONCEPT_ID and
lower(substr(a1.atom_name, 1, instr(a1.atom_name, '(', -1)-1)) = lower(substr(a2.atom_name, 1, instr(a2.atom_name, '(', -1)-1)) and
substr(a1.atom_name, instr(a1.atom_name, '(', -1)+1, length(substr(a1.atom_name, instr(a1.atom_name, '(', -1)))-2) = 'product' and
substr(a2.atom_name, instr(a2.atom_name, '(', -1)+1, length(substr(a2.atom_name, instr(a2.atom_name, '(', -1)))-2) = 'substance'
