# 17. Drug Delivery Devices not related to a SCD or SBD.

SELECT DISTINCT concept_id from attributes
WHERE attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value = 'Drug Delivery Device'
MINUS
(SELECT concept_id_1 from relationships a, classes b
 WHERE concept_id_2 = concept_id
   AND b.source like 'RXNORM%'
   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   AND b.tobereleased in ('Y','y')
 UNION
 SELECT concept_id_2 from relationships a, classes b
 WHERE concept_id_1 = concept_id
   AND b.source like 'RXNORM%'
   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   AND b.tobereleased in ('Y','y'));


