# 54. Identfy cases where a NDDF CDC, CDD, and CDA have the same code 
#    but are split across UMLS concepts.

select distinct a.concept_id as concept_id_1, b.concept_id as concept_id_2
from classes a, classes b
where a.concept_id < b.concept_id
  and a.code = b.code  
  and a.source = (select current_name from source_version where source='NDDF')
  and a.source = b.source
  and a.tty in ('CDC','CDD','CDA')
  and b.tty in ('CDC','CDD','CDA');
