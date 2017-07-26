# KWF - Split SNOMEDCT FNs and PTs
# suresh@nlm.nih.gov - pivot & clusterize

select distinct c1.concept_id as concept_id_1, c2.concept_id as concept_id_2 from classes c1, classes c2
where c1.concept_id <> c2.concept_id
and   c1.code = c2.code
and   c1.source = (select current_name from source_version where source='SNOMEDCT')
and   c2.source = c1.source
and   c1.tty = 'FN'
and   c2.tty = 'PT'
and   c1.tobereleased in ('Y', 'y')
and   c2.tobereleased in ('Y', 'y')
