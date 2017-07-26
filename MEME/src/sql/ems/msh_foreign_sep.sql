# Here is a query that identifies cases of foreign MSH sources no longer
# in concepts with their ENG MSH MUI counterparts.  The query produces a
# concept_id of the non-ENG MSH thing and its MUI.

# suresh@nlm.nih.gov 5/2005
# bcarlsen@msdinc.com 5/2005


select distinct concept_id, rownum as cluster_id from (
 select concept_id, source_cui from classes
 where source like 'MSH%'
 and language != 'ENG'
 and source_cui like 'M%'
 minus
 select concept_id, source_cui from classes a, source_version b
 where a.source=current_name
 and b.source='MSH'
)
