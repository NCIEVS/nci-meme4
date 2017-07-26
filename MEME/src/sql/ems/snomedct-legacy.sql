# Can we have a query that gives us clusters of 
# concepts where the CT concept is not in the same concept
# as the SNMI or RCD concept with the legacy code? - TPW

# bcarlsen@msdinc.com
# suresh@nlm.nih.gov 11/2003

select concept_id, cluster_id from
(select b.concept_id, a.concept_id as cluster_id
from attributes a, classes b
where attribute_name='SNOMEDID'
  and a.source = (select current_name from source_version
                   where source='SNOMEDCT_US')
and b.tobereleased in ('Y','y') and attribute_value = b.code
and a.concept_id != b.concept_id and b.source in 
  (select current_name from source_version where source in ('RCD','SNMI'))
union
select b.concept_id, a.concept_id
from attributes a, classes b
where attribute_name='CTV3ID'
  and a.source = (select current_name from source_version
                   where source='SNOMEDCT_US')
and b.tobereleased in ('Y','y') and attribute_value = b.code
and a.concept_id != b.concept_id and b.source in 
  (select current_name from source_version where source in ('RCD','SNMI'))
union
select a.concept_id, a.concept_id
from attributes a, classes b
where attribute_name='SNOMEDID'
  and a.source = (select current_name from source_version
                   where source='SNOMEDCT_US')
and b.tobereleased in ('Y','y') and attribute_value = b.code
and a.concept_id != b.concept_id and b.source in 
  (select current_name from source_version where source in ('RCD','SNMI'))
union
select a.concept_id, a.concept_id
from attributes a, classes b
where attribute_name='CTV3ID'
  and a.source = (select current_name from source_version
                   where source='SNOMEDCT_US')
and b.tobereleased in ('Y','y') and attribute_value = b.code
and a.concept_id != b.concept_id and b.source in 
  (select current_name from source_version where source in ('RCD','SNMI'))
) order by cluster_id
