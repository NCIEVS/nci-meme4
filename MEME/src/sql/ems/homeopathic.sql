# All concepts with STY of Plant and with the word 'homeopathic' in them

# wth@nlm.nih.gov and
# suresh@nlm.nih.gov 5/6/98
# suresh@nlm.nih.gov - to Oracle 8/00
# suresh@nlm.nih.gov - EMS-3

select concept_id, rownum as (
  select distinct a.concept_id FROM attributes a, normwrd w, classes c
  where  a.attribute_name='SEMANTIC_TYPE'
  and    a.attribute_value='Plant'
  and    w.normwrd_id=c.atom_id
  and    c.concept_id=a.concept_id
  and    w.normwrd='homeopathic'
  and    c.tobereleased IN ('y', 'Y')
)
