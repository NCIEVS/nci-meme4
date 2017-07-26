# 4. SCD/SBD concepts whose ingredients participated in splits
#    where the ingredient_of relationship was inserted before
#    the split.  Thus, after the split, the SCD is potentially
#    connected to the wrong ingredient (like the Protein Measurement
#    thing from before).

SELECT DISTINCT concept_id FROM
(
SELECT a.concept_id
FROM classes a, relationships b,
(SELECT concept_id_1, concept_id_2
 FROM molecular_actions a,
 (select concept_id_1, concept_id_2, a.insertion_date
  from relationships a, classes b
  where relationship_attribute='ingredient_of'
    and atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
    and b.termgroup like 'RXNORM%/SCDC'
    and b.tobereleased in ('Y','y')) b
 WHERE concept_id_2 = source_id
   AND ( (molecular_action in ('MOLECULAR_SPLIT','MOLECULAR_MOVE')
	  AND undone = 'N') OR
         (molecular_action = 'MOLECULAR_MERGE'
	  AND undone = 'Y'))
   AND insertion_date <
	greatest(a.timestamp, NVL(a.undone_when,'01-jan-1980'))
 UNION
 SELECT concept_id_1, concept_id_2
 FROM molecular_actions a,
 (select concept_id_1, concept_id_2, a.insertion_date
  from relationships a, classes b
  where relationship_attribute='ingredient_of'
    and atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
    and b.termgroup like 'RXNORM%/SCDC'
    and b.tobereleased in ('Y','y')) b
 WHERE concept_id_2 = target_id
   AND ( (molecular_action in ('MOLECULAR_SPLIT','MOLECULAR_MOVE')
	  AND undone = 'N') OR
         (molecular_action = 'MOLECULAR_MERGE'
	  AND undone = 'Y') )
   AND insertion_date <
	greatest(a.timestamp, NVL(a.undone_when,'01-jan-1980')) ) c
WHERE a.atom_id = atom_id_1
  AND b.concept_id_2 = c.concept_id_1
  AND a.source  like 'RXNORM%'
  AND a.termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND a.tobereleased in ('Y','y')
  AND b.relationship_attribute = 'constitutes'
UNION
SELECT 1 FROM dual WHERE 1=0);


