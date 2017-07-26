# Script to get the concepts where mim type is 0

SELECT DISTINCT concept_id
FROM attributes
   WHERE attribute_name = 'MIMTYPE'
     and attribute_value = '0'
     and source in (select current_name from source_version where source = 'OMIM')
