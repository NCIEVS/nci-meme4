# 9. SCD concepts with ingredients that are 'form_of' to other
#    ingredients.  These cases are either precise ingredients
#    masquerading as active ingredients (i.e. change the active
#    ingredient of the SCD) or they are cases where the ingredients
#    at the bottom level have backwards form_of relationships (i.e.
#    the direction of the form_of rel should be switched).

SELECT DISTINCT a.concept_id
FROM classes a, relationships b,
  (select distinct concept_id_1 as scdc_id
   from relationships a, classes b
   where relationship_attribute='ingredient_of'
     and atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
     and b.termgroup like 'RXNORM%/SCDC'
     and b.tobereleased in ('Y','y')
     and atom_id_2 in
	(select atom_id_2 from relationships where relationship_attribute = 'form_of'
	 union
	 select atom_id_1 from relationships where relationship_attribute = 'has_form')) c
WHERE a.atom_id = b.atom_id_1
  AND relationship_attribute = 'constitutes'
  AND a.source  like 'RXNORM%'
  AND a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND a.tobereleased in ('Y','y')
  AND concept_id_2 = c.scdc_id;


