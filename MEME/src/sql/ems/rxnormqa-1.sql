# 1. SCD/SBD Concepts with infrequently appearing units.

SELECT DISTINCT concept_id FROM
(SELECT a.concept_id
FROM classes a, relationships b,
(SELECT concept_id
 FROM
  (select distinct substr(atom_name,instr(atom_name,' ',-1)+1) as units
   from classes a, atoms b
   where a.atom_id=b.atom_id
     and a.tobereleased in ('Y','y')
     and a.source like 'RXNORM%'
     and a.termgroup like 'RXNORM%/SCDC'
     and atom_name NOT LIKE '%MG/ML'
     and atom_name NOT LIKE '%MG/MG'
     and atom_name NOT LIKE '%MG'
     and length(atom_name) >
 	length(replace(translate(atom_name,'123456789','~~~~~~~~~'),'~',''))
   group by substr(atom_name,instr(atom_name,' ',-1)+1) having count(*)<26
  ) a,
  (select distinct a.concept_id,
 	 substr(atom_name,instr(atom_name,' ',-1)+1) as units
   from classes a, atoms b
   where a.atom_id=b.atom_id
     and a.tobereleased in ('Y','y')
     and a.source like 'RXNORM%'
     and a.termgroup like 'RXNORM%/SCDC'
     and atom_name NOT LIKE '%MG/ML'
     and atom_name NOT LIKE '%MG/MG'
     and atom_name NOT LIKE '%MG'
     and length(atom_name) >
 	length(replace(translate(atom_name,'123456789','~~~~~~~~~'),'~',''))
  ) b
 WHERE a.units = b.units) c
WHERE a.concept_id = concept_id_1
  AND c.concept_id = concept_id_2
  AND relationship_attribute='constitutes'
  AND a.tobereleased in ('Y','y')
  AND a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
UNION ALL
SELECT 1 FROM dual WHERE 1=0);
