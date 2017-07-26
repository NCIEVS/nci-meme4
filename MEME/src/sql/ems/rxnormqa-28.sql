# 28. Identify concepts containing more than one SCD or SBD
#     from a case-sensitive string perspective.

SELECT concept_id
FROM classes
WHERE source  like 'RXNORM%' AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND tobereleased in ('Y','y')
GROUP BY concept_id HAVING count(distinct sui)>1;


