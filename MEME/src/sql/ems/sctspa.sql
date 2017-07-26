# All Spanish SNOMED concepts clustered by the semantic neighborhood,
# i.e., English concepts with same SCUI.

# bcarlsen@msdinc.com 4/2004
# suresh@nlm.nih.gov 5/2004
# suresh@nlm.nih.gov 5/2005 * EMS-3

select concept_id,cluster_id
from
  (select source_cui, rownum as cluster_id
   from (select source_cui from classes
	  where tobereleased in ('Y','y') and source =
	     (select current_name
	           from source_version
	           where source = 'SNOMEDCT_US')
	  group by source_cui having count(distinct concept_id)>1) ) a,
  (select distinct concept_id, source_cui from classes
   where tobereleased in ('Y','y')
     and source =
   (select current_name from source_version where source='SNOMEDCT_US')
     and source_cui in
      (select source_cui from classes
       where source =
        (select current_name from source_version where source='SCTSPA')
          and tobereleased in ('Y','y'))) b
where b.source_cui = a.source_cui
order by 2
