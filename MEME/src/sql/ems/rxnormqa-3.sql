# 3. SCD/SBD Concepts with components (SCDCs) lacking units/strength.

SELECT DISTINCT a.concept_id
FROM classes a, relationships b, classes c, atoms d
WHERE a.source  like 'RXNORM%'
  AND a.termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND a.tobereleased in ('Y','y')
  AND a.atom_id = atom_id_1
  AND relationship_attribute = 'constitutes'
  AND atom_id_2 = c.atom_id
  AND c.source  like 'RXNORM%'
  AND c.termgroup  like 'RXNORM%/SCDC'
  AND c.tobereleased in ('Y','y')
  AND c.atom_id = d.atom_id
  AND length(atom_name) =
 	length(replace(translate(atom_name,'123456789','~~~~~~~~~'),'~',''));


