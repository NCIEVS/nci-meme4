# Can you make a query for us that shows cases where we merged more
# then 1 Snomed concept w/ (organism) together? Once we finish editing
# the organisms I would like to QA these before we send them on - TPW

# bcarlsen@msdinc.com
# suresh@nlm.nih.gov 11/2003

select concept_id
from classes a, atoms b
where a.atom_id = b.atom_id
  and atom_name like '% (organism)'
  and source = (select current_name from source_version
                where source='SNOMEDCT_US')
  and tobereleased in ('Y','y')
  and tty='FN'
group by concept_id having count(*)>1
