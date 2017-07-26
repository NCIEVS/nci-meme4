# Are there multiple, releaseable "NEC" atoms from different source families
# in the same concept?

# suresh EMS 3.0 5/2005

select concept_id, rownum as cluster_id from
(
  select distinct concept_id from
  (
    select a.concept_id, c.source_family as fam1, d.source_family as fam2 from classes a, classes b, source_rank c, source_rank d
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
    and    a.atom_id <> b.atom_id
    and    a.source <> b.source
    and    a.source <> 'MTH'
    and    b.source <> 'MTH'
    and    a.source=c.source
    and    b.source=d.source
  )
  where fam1<>fam2
)
