# 14. Ingredient concepts participating in a split where the
#     'ingredient_of' relationships were inserted before the
#     split took place.  Editors should make sure that the
#     splits were correct and then edit the concepts in #4.

(SELECT concept_id_2 as concept_id_1, target_id as concept_id_2
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
 SELECT concept_id_2, source_id
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
	greatest(a.timestamp, NVL(a.undone_when,'01-jan-1980')) );


