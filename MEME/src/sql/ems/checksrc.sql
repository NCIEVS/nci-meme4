# Ensure that SRC concepts and all releasable atoms are approved
# and the concepts themselves are approved
# suresh@nlm.nih.gov 3/2003
# suresh@nlm.nih.gov - EMS-3

select distinct concept_id, rownum as cluster_id from
(
  select concept_id from classes
  where  concept_id in (select concept_id from classes where source='SRC')
  and    tobereleased in ('y', 'Y')
  and    status='N'
  union
  select concept_id from concept_status
  where  concept_id in (select concept_id from classes where source='SRC')
  and    status='N'
)
