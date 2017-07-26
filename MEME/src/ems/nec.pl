select a.concept_id from classes a, classes b
where  a.concept_id=b.concept_id
and    a.tobereleased in ('y', 'Y')
and    b.tobereleased in ('y', 'Y')
and    a.atom_id in (
		     select normwrd_id as atom_id from normwrd where normwrd='nec'
		     union
		     select atom_id from atoms where lower(atom_name) like '% not elsewhere classified%'
		     )
and    b.atom_id in (
		     select normwrd_id as atom_id from normwrd where normwrd='nec'
		     union
		     select atom_id from atoms where lower(atom_name) like '% not elsewhere classified%'
		     )
and    a.atom_id<>b.atom_id
