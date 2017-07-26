select a.concept_id, r from (
  SELECT DISTINCT C1.CONCEPT_ID as c1, C2.CONCEPT_ID as c2, rownum as r
  FROM CLASSES C1, CLASSES C2
  WHERE
  C1.CONCEPT_ID <> C2.CONCEPT_ID
  AND C1.CODE = C2.CODE
  AND C1.SOURCE = (select current_name from source_version where source='SNOMEDCT_US')
  AND C2.SOURCE = C1.SOURCE
  AND C1.TTY = 'FN'
  AND C2.TTY = 'PT'
  and c1.tobereleased in ('Y', 'y')
  and c2.tobereleased in ('Y', 'y')
) b, concept_status a where a.concept_id in (c1, c2) order by r

