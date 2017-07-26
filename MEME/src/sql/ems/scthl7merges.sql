# concepts with both HL7 and SNOMEDCT atoms
# suresh@nlm.nih.gov 11/2005

select concept_id from classes
where tobereleased not in ('N','n')
and   source in (select current_name from source_version where source in ('HL7V3.0','SNOMEDCT'))
group by concept_id having count(distinct source)>1
