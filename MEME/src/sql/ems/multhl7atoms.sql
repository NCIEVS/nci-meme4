# concepts with more than 1 HL7 atoms
# suresh@nlm.nih.gov 11/2005

select concept_id from classes
where tobereleased not in ('N','n')
and source in (select current_name from source_version where source='HL7V3.0')
group by concept_id having count(distinct aui)>1
