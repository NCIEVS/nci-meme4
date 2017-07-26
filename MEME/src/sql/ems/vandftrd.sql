# Here is a query to find the VANDF concepts with TRD lexical tags and
# 'Clinical Drug' semantic types that do not have NLM02/SBD atoms
# as discussed in last Wednesday's conference call.

# suresh@nlm.nih.gov 3/2003
# suresh@nlm.nih.gov - EMS-3

select concept_id, rownum as cluster_id from
(
  select distinct concept_id from classes
  where source=(select current_name from source_version where source='VANDF')
  and   tobereleased in ('Y','y')
  and   concept_id in
    (
      select concept_id from attributes
      where  attribute_name='LEXICAL_TAG'
      and    attribute_value='TRD'
    )
  and   concept_id in
    (
      select concept_id from attributes
      where  attribute_name||''='SEMANTIC_TYPE'
      and    attribute_value='Clinical Drug'
    )
  minus
  select concept_id from classes
  where source=(select current_name from source_version where source='RXNORM')
  and   termgroup like '%/SBD'
  and   tobereleased in ('Y','y')
)


