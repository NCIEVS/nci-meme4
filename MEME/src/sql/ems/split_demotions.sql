WITH molecular_splits AS
  (SELECT source_id,target_id FROM molecular_actions WHERE molecular_action='MOLECULAR_SPLIT'),
split_concepts AS
  (SELECT concept_id, atom_id FROM classes WHERE concept_id IN
     (SELECT source_id concept_id FROM molecular_splits UNION SELECT target_id FROM molecular_splits)), 
has_demotions AS
  (SELECT atom_id_1 AS atom_id, concept_id_2 FROM dead_relationships WHERE status='D'
   UNION ALL
   SELECT atom_id_2, concept_id_1 FROM dead_relationships WHERE status='D') 
SELECT a.concept_id FROM split_concepts a, has_demotions b WHERE a.atom_id = b.atom_id GROUP BY concept_id HAVING COUNT(*)>= 5
