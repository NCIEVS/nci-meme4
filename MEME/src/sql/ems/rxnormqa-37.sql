# 37.  Concepts expressed in terms of their precise ingredients (components
#      whose name does not match its base ingredient).

SELECT distinct scdc.concept_id
FROM classes scdc, atoms scdca, atoms inga, relationships scdcin
WHERE scdc.source  like 'RXNORM%'
  AND scdc.termgroup like 'RXNORM%/SCDC'
  AND scdc.tobereleased='Y'
  AND scdc.atom_id=scdca.atom_id
  AND scdcin.atom_id_1=scdc.atom_id
  AND scdcin.atom_id_2=inga.atom_id
  AND scdcin.source like 'RXNORM%'
  AND scdcin.relationship_attribute='ingredient_of'
  AND scdcin.relationship_level='S'
  AND lower(scdca.atom_name) NOT LIKE lower(inga.atom_name)||'%';


