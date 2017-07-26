# 32. Concepts with normal forms containing the word "obsolete".  This
#     is caused by ingredient concepts from MMSL containing the word
#     obsolete.

SELECT DISTINCT concept_id 
FROM classes a, atoms b 
WHERE a.source  like 'RXNORM%' 
  AND a.termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')) 
  AND a.atom_id = b.atom_id 
  AND lower(atom_name) like '%obsolete%';


