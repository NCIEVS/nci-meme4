# Identify cases where NDFRT atom has an SCDC attribute and it matches the SCDC string of an atom in a different concept.

select c.concept_id as concept_id_1,b.concept_id as concept_id_2 from classes b, attributes c, atoms d
where b.atom_id = d.atom_id
and b.concept_id != c.concept_id
and b.tobereleased in ('Y','y') and c.tobereleased in ('Y','y')
and c.source = (select current_name from source_version where source='NDFRT')
and b.source like 'RXNORM%'
and c.attribute_name='RXNORM_SCDC'
and c.attribute_value = d.atom_name;
