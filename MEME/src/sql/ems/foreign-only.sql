# concepts with releasable non-English atoms only
# lad@msdinc.com, suresh@nlm.nih.gov

# note that foreign_classes has foreign atoms too, but these are not edited

select concept_id, rownum as cluster_id from (
  select distinct concept_id from classes
  where language!='ENG'
  and   tobereleased in ('y', 'Y')
  minus
  select concept_id from classes
  where language='ENG'
  and   tobereleased in ('y','Y')
)

