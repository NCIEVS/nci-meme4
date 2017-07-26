-- Changes:
-- 06/15/2006 BAC (1-BHMK1): changes requested by TPW

----------------------------------------------------------

1. SCD/SBD Concepts with infrequently appearing units.

SELECT DISTINCT concept_id FROM
(SELECT a.concept_id
FROM classes a, relationships b,
(SELECT concept_id
 FROM
  (select distinct substr(atom_name,instr(atom_name,' ',-1)+1) as units
   from classes a, atoms b
   where a.atom_id=b.atom_id
     and a.tobereleased in ('Y','y')
     and a.source like 'RXNORM%'
     and a.termgroup like 'RXNORM%/SCDC'
     and atom_name NOT LIKE '%MG/ML'
     and atom_name NOT LIKE '%MG/MG'
     and atom_name NOT LIKE '%MG'
     and length(atom_name) >
 	length(replace(translate(atom_name,'123456789','~~~~~~~~~'),'~',''))
   group by substr(atom_name,instr(atom_name,' ',-1)+1) having count(*)<26
  ) a,
  (select distinct a.concept_id,
 	 substr(atom_name,instr(atom_name,' ',-1)+1) as units
   from classes a, atoms b
   where a.atom_id=b.atom_id
     and a.tobereleased in ('Y','y')
     and a.source like 'RXNORM%'
     and a.termgroup like 'RXNORM%/SCDC'
     and atom_name NOT LIKE '%MG/ML'
     and atom_name NOT LIKE '%MG/MG'
     and atom_name NOT LIKE '%MG'
     and length(atom_name) >
 	length(replace(translate(atom_name,'123456789','~~~~~~~~~'),'~',''))
  ) b
 WHERE a.units = b.units) c
WHERE a.concept_id = concept_id_1
  AND c.concept_id = concept_id_2
  AND relationship_attribute='constitutes'
  AND a.tobereleased in ('Y','y')
  AND a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
UNION ALL
SELECT 1 FROM dual WHERE 1=0);


2. SCD/SBD Concepts with 'Solution' dose forms expressed as "MG".

SELECT DISTINCT a.concept_id
FROM classes a, atoms b
WHERE a.source in (SELECT current_name FROM source_version WHERE source='RXNORM')
  AND a.tty in ('SCD','SBD')
  AND a.tobereleased in ('Y','y')
  AND a.atom_id = b.atom_id
  AND atom_name like '%MG % Solution%';


7. SCD Concepts where at least one ingredient has a
   TRD lexical tag.  These cases should be SBDs
   (eventually).

SELECT DISTINCT a.concept_id
FROM classes a, relationships b,
  (select concept_id_1 as scdc_id
   from relationships a, classes b, attributes c
   where relationship_attribute='ingredient_of'
     and atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
     and b.termgroup like 'RXNORM%/SCDC'
     and b.tobereleased in ('Y','y')
     and a.concept_id_2 = c.concept_id
     and c.attribute_name='LEXICAL_TAG'
     and c.attribute_value='TRD') c
WHERE a.atom_id = b.atom_id_1
  AND relationship_attribute = 'constitutes'
  AND a.source  like 'RXNORM%'
  AND a.termgroup  like 'RXNORM%/SCD' -- not SBD!
  AND a.tobereleased in ('Y','y')
  AND concept_id_2 = c.scdc_id;


8. Ingredient concepts with TRD tags connected to
   SCD concepts.  Review this list to make sure that they
   should all actually be trade names.  Then edit
   list #7 and assign TRD lexical tags to those
   concepts.

SELECT DISTINCT c.in_id as concept_id
FROM classes a, relationships b,
  (select concept_id_2 as in_id, concept_id_1 as scdc_id
   from relationships a, classes b, attributes c
   where relationship_attribute='ingredient_of'
     and atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
     and b.termgroup like 'RXNORM%/SCDC'
     and b.tobereleased in ('Y','y')
     and a.concept_id_2 = c.concept_id
     and c.attribute_name='LEXICAL_TAG'
     and c.attribute_value='TRD') c
WHERE a.atom_id = b.atom_id_1
  AND relationship_attribute = 'constitutes'
  AND a.source  like 'RXNORM%'
  AND a.termgroup  like 'RXNORM%/SCD' -- not SBD!
  AND a.tobereleased in ('Y','y')
  AND concept_id_2 = c.scdc_id;


10. SCD, SBD, or SCDC, SCDF, SBDF, SBDC concept without a 'Clinical Drug' semantic type.
    Most look like they have 'Drug Delivery Device' and as we
    understand it, concepts with that STY should not have SCD atoms.

SELECT distinct concept_id FROM classes
WHERE source = (SELECT current_name FROM source_version WHERE source='RXNORM%') 
  AND termgroup IN 
    (SELECT termgroup FROM termgroup_rank 
     WHERE termgroup like 'RXNORM%' 
       AND tty in ('SCD','SBD','SCDC','SCDF','SBDC','SBDF'))
  AND tobereleased in ('Y','y')
MINUS
SELECT /*+ FULL(a) */ distinct concept_id FROM attributes a
WHERE attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
  AND tobereleased in ('Y','y');


13. Ingredient concepts without 'Pharmacologic Substance'
    or 'Antibiotic' semantic types.

SELECT DISTINCT concept_id_2 as concept_id
FROM relationships a, classes b
WHERE relationship_attribute like '%ingredient_of'
  AND atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
  AND b.termgroup like 'RXNORM%/SCDC'
  AND b.tobereleased in ('Y','y')
MINUS
SELECT /*+ FULL(a) */ concept_id FROM attributes a
WHERE attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value in ('Pharmacologic Substance','Antibiotic')
  AND tobereleased in ('Y','y');


15. SCD/SBD Concepts with 'Solid' dose forms expressed as "ML".

SELECT DISTINCT a.concept_id
FROM classes a, atoms b
WHERE a.source in (SELECT current_name FROM source_version WHERE source='RXNORM')
  AND a.tty in ('SCD','SBD')
  AND a.tobereleased in ('Y','y')
  AND a.atom_id = b.atom_id
  AND atom_name like '%ML % Solid%';


32. Concepts with normal forms containing the word "obsolete".  This
    is caused by ingredient concepts from MMSL containing the word
    obsolete.

SELECT DISTINCT concept_id 
FROM classes a, atoms b 
WHERE a.source  like 'RXNORM%' 
  AND a.termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')) 
  AND a.atom_id = b.atom_id 
  AND lower(atom_name) like '%obsolete%';


37.  Concepts expressed in terms of their precise ingredients (components
     whose name does not match its base ingredient).

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


38.  Normal form merges: Concepts containing normal form atoms with 
     different case-insensitive strings (ISUI).

SELECT concept_id FROM classes
WHERE source = (SELECT current_name FROM source_version WHERE source='RXNORM')
  AND tty in ('SCD','SBD','SCDC','SBDC','SCDF','SBDF')
  AND tobereleased in ('Y','y')
GROUP BY concept_id HAVING count(distinct tty||isui)>1;


44. Non-chemical ingredient concepts.

SELECT concept_id_2 as concept_id FROM relationships    
WHERE relationship_attribute IN ('ingredient_of','precise_ingredient_of')
  AND source = (SELECT current_name FROM source_version WHERE source='RXNORM')
  AND relationship_level = 'S'
MINUS
SELECT /*+ FULL(a) */ concept_id FROM attributes
WHERE attribute_name ='SEMANTIC_TYPE'
  AND attribute_value IN
   (SELECT semantic_type
    FROM semantic_types WHERE is_chem='Y');


47. Concepts with merged RXCUI values

select /*+ INDEX(a,x_attr_an) */ distinct a.concept_id
from attributes a, attributes b
where a.concept_id=b.concept_id
  and a.attribute_name='RXCUI'
  and b.attribute_name='RXCUI'
  and a.tobereleased not in ('N','n')
  and b.tobereleased not in ('N','n')
  and a.attribute_value != b.attribute_value;


48. Concepts with split RXCUI values

select /*+ INDEX(a,x_attr_an) */ 
  distinct a.concept_id as concept_id_1, b.concept_id as concept_id_2
from attributes a, attributes b, classes c
where a.concept_id != b.concept_id
  and a.attribute_name='RXCUI'
  and b.attribute_name='RXCUI'
  and a.tobereleased not in ('N','n')
  and b.tobereleased not in ('N','n')
  and a.attribute_value=b.attribute_value
  and a.concept_id = c.concept_id
  and c.source = (select current_name from source_version where source='RXNORM')
  and c.tobereleased not in ('N','n');


49. Concepts with merged RXNORM primary or ingredient atom TTYs.

select concept_id
from classes where source = (select current_name
from sourcE_version where source='RXNORM')
and tty in ('SCD','SBD','SCDC','SBDC','SCDF','SBDF','BN','IN')
and tobereleased in ('Y','y')
group by concept_id having count(distinct tty)>1;


50. RXNORM primary atom with non-matching MTH/PN

select distinct a.concept_id
from classes a, classes b
where a.source = (select current_name
from source_version where source='RXNORM')
and b.source='MTH' and b.tty='PN'
and a.tty in ('SCD','SBD','SCDC','SBDC','SCDF','SBDF')
and a.tobereleased in ('Y','y')
and b.tobereleased in ('Y','y')
and a.isui != b.isui
and a.concept_id=b.concept_id;


51. Concepts with merged SCUI, IN/IN or BN/BN atoms

select distinct a.concept_id
from classes a, classes b
where a.source = (select current_name
from source_version where source='RXNORM')
  and a.source=b.source
  and a.tty in ('IN','BN')
  and b.tty = a.tty
  and a.tobereleased in ('Y','y')
  and b.tobereleased in ('Y','y')
  and a.concept_id = b.concept_id
  and a.source_cui < b.source_cui;


52. Concepts with merged SCUI, SCD/SCD, SCDF/SCDF, or SCDC/SCDC

select distinct a.concept_id
from classes a, classes b
where a.source = (select current_name
from source_version where source='RXNORM')
  and a.source=b.source
  and a.tty in ('SCD','SCDF','SCDC')
  and b.tty = a.tty
  and a.tobereleased in ('Y','y')
  and b.tobereleased in ('Y','y')
  and a.concept_id = b.concept_id
  and a.source_cui < b.source_cui;


53. Concepts with merged SCUI, SBD/SBD, SBDF/SBDF, or SBDC/SBDC

select distinct a.concept_id
from classes a, classes b
where a.source = (select current_name
from source_version where source='RXNORM')
  and a.source=b.source
  and a.tty in ('SBD','SBDF','SBDC')
  and b.tty = a.tty
  and a.tobereleased in ('Y','y')
  and b.tobereleased in ('Y','y')
  and a.concept_id = b.concept_id
  and a.source_cui < b.source_cui;

  
54. Identfy cases where a NDDF CDC, CDD, and CDA have the same code 
    but are split across UMLS concepts.

select distinct a.concept_id as concept_id_1, b.concept_id as concept_id_2
from classes a, classes b
where a.concept_id < b.concept_id
  and a.code = b.code  
  and a.source = (select current_name from source_version where source='NDDF')
  and a.source = b.source
  and a.tty in ('CDC','CDD','CDA')
  and b.tty in ('CDC','CDD','CDA');
  
