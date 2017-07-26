12. SCD/SBD concepts with the same components but different
    dose forms where the dose forms are siblings in the HL7
    hierarchy.  This returns only multiple-component cases.

select distinct concept_id_1,concept_id_2 from
(SELECT a.concept_id as concept_id_1, b.concept_id as concept_id_2
FROM
(select /*+ RULE */ distinct a.concept_id, b.atom_id+0 as df_id, a.termgroup,
   lower(substr(c.atom_name,0,instr(lower(c.atom_name),lower(d.atom_name))-2)) as str
 from classes a, classes b, atoms c, atoms d, relationships e
 where a.tobereleased in ('Y','y')
   and a.source  like 'RXNORM%'
   and a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   and b.source like 'HL7%'
   and b.termgroup like 'HL7%/DF'
   and a.atom_id = c.atom_id
   and b.atom_id = d.atom_id
   and a.atom_id = atom_id_1
   and b.atom_id = atom_id_2
   and relationship_attribute = 'dose_form_of') a,
(select /*+ RULE */ distinct a.concept_id, b.atom_id+0 as df_id, a.termgroup,
   lower(substr(c.atom_name,0,instr(lower(c.atom_name),lower(d.atom_name))-2)) as str
 from classes a, classes b, atoms c, atoms d, relationships e
 where a.tobereleased in ('Y','y')
   and a.source  like 'RXNORM%'
   and a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   and b.source like 'HL7%'
   and b.termgroup like 'HL7%/DF'
   and a.atom_id = c.atom_id
   and b.atom_id = d.atom_id
   and a.atom_id = atom_id_1
   and b.atom_id = atom_id_2
   and c.atom_name like '% / %'
   and relationship_attribute = 'dose_form_of') b,
   context_relationships c, context_relationships d
WHERE a.concept_id < b.concept_id
  AND a.str = b.str
  and a.df_id != b.df_id
  and a.termgroup = b.termgroup
  and a.df_id = c.atom_id_1
  and b.df_id = d.atom_id_1
  and c.parent_treenum = d.parent_treenum
union all
select 1,2 from dual where 1=0);


13. Ingredient concepts without 'Pharmacologic Substance'
    semantic types.

SELECT DISTINCT concept_id_2 as concept_id
FROM relationships a, classes b
WHERE relationship_attribute like '%ingredient_of'
  AND atom_id_1 = b.atom_id and b.source  like 'RXNORM%'
  AND b.termgroup like 'RXNORM%/SCDC'
  AND b.tobereleased in ('Y','y')
MINUS
SELECT concept_id FROM attributes
WHERE attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value in ('Pharmacologic Substance','Antibiotic')
  AND tobereleased in ('Y','y');


14. Ingredient concepts participating in a split where the
    'ingredient_of' relationships were inserted before the
    split took place.  Editors should make sure that the
    splits were correct and then edit the concepts in #4.

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


15. SCD/SBD Concepts with 'Solid' dose forms expressed as "ML".

SELECT DISTINCT a.concept_id
FROM classes a, atoms b
WHERE a.source  like 'RXNORM%'
  AND a.termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND a.tobereleased in ('Y','y')
  AND a.atom_id = b.atom_id
  AND atom_name like '%ML %'
  AND a.atom_id in
    (select atom_id_1 from relationships where
     relationship_attribute = 'dose_form_of' and
     atom_id_2 in
        (select d.atom_id from classes a, context_relationships b,
                        atoms c, classes d
  where a.source like 'HL7%' and a.termgroup like 'HL7%/DF'
    and a.atom_id = c.atom_id
    and b.parent_treenum like '%.'||a.aui||'.%'
    and c.atom_name IN ('Solid','Patch','Pad')
    and atom_id_1 = d.atom_id
    and d.source like 'HL7%' and d.termgroup like 'HL7%/DF'));


16. Clinical Drugs without RxNorm Forms (non big-5 drug sources).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source not like 'MMX%'
  AND b.source not like 'MMSL%'
  AND b.source not like 'NDDF%'
  AND b.source not like 'VANDF%'
  AND b.source not like 'MTHFDA%'
  AND b.source not like 'MDDB%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);



17. Drug Delivery Devices not related to a SCD or SBD.

SELECT DISTINCT concept_id from attributes
WHERE attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value = 'Drug Delivery Device'
MINUS
(SELECT concept_id_1 from relationships a, classes b
 WHERE concept_id_2 = concept_id
   AND b.source like 'RXNORM%'
   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   AND b.tobereleased in ('Y','y')
 UNION
 SELECT concept_id_2 from relationships a, classes b
 WHERE concept_id_1 = concept_id
   AND b.source like 'RXNORM%'
   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   AND b.tobereleased in ('Y','y'));


18.  Concepts with RxNorm status U.

SELECT DISTINCT concept_id from attributes
WHERE attribute_name = 'RX_NORM_STATUS'
  AND attribute_value = 'U';


19.  Concepts with RxNorm status H.

SELECT DISTINCT concept_id from attributes
WHERE attribute_name = 'RX_NORM_STATUS'
  AND attribute_value = 'H';


20.  Concepts with illegal dose forms.

SELECT DISTINCT concept_id_1 as concept_id
FROM relationships
WHERE relationship_attribute='dose_form_of'
  AND atom_id_2 in
    (SELECT a.atom_id from classes a, attributes b
     WHERE a.source like 'HL7%'
       AND a.termgroup like 'HL7%/DF'
       AND a.atom_id = b.atom_id
       AND attribute_name = 'SOS'
       AND attribute_value = 'Not to be used');


21. Clinical Drugs without RxNorm Forms (MMX).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'MMX%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);


22. Clinical Drugs without RxNorm Forms (MMSL).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'MMSL%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);


23. Clinical Drugs without RxNorm Forms (VANDF).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'VANDF%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);


24. Clinical Drugs without RxNorm Forms (MDDB).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'MDDB%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);


25. Clinical Drugs without RxNorm Forms (NDDF).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'NDDF%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);


26.  Clinical Drugs without RxNorm Forms (MTHFDA).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'MTHFDA%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);


27. Identify concepts with both rxnorm status of U and H

SELECT concept_id
FROM attributes 
WHERE attribute_name='RX_NORM_STATUS'
  AND attribute_value in ('H','U')
GROUP BY concept_id HAVING count(distinct attribute_value) >1;


28. Identify concepts containing more than one SCD or SBD
    from a case-sensitive string perspective.

SELECT concept_id
FROM classes
WHERE source  like 'RXNORM%' AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND tobereleased in ('Y','y')
GROUP BY concept_id HAVING count(distinct sui)>1;


29.  Clinical Drugs without RxNorm Forms (SNMI).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'SNMI%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);


30.  Clinical Drugs without RxNorm Forms (RCD).

SELECT DISTINCT a.concept_id
FROM attributes a, classes b
WHERE a.concept_id=b.concept_id
  AND b.source like 'RCD%'
  AND attribute_name||'' = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
MINUS
(SELECT concept_id from classes
 WHERE source like 'RXNORM%' and tobereleased in ('Y','y')
 UNION
 SELECT concept_id_1 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_2 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
 UNION
 SELECT concept_id_2 FROM relationships
 WHERE relationship_level = 'S'
   AND relationship_attribute in ('mapped_to','mapped_from','isa','inverse_isa')
   AND atom_id_1 in (SELECT atom_id FROM classes WHERE source like 'RXNORM%'
  			   AND termgroup in (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')))
);

31. Concepts with normal forms but no Clinical Drug atoms.
    These are concepts that should either be removed
    or merged with other concepts.

SELECT concept_id FROM classes 
WHERE source like 'RXNORM%' AND termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND tobereleased IN ('Y','y') 
MINUS
SELECT concept_id FROM classes 
WHERE (source ! like 'RXNORM%' OR tty in ('OBD','OCD'))
  AND tobereleased in ('Y','y')


32. Concepts with normal forms containing the word "obsolete".  This
    is caused by ingredient concepts from MMSL containing the word
    obsolete.

SELECT DISTINCT concept_id 
FROM classes a, atoms b 
WHERE a.source  like 'RXNORM%' 
  AND a.termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD')) 
  AND a.atom_id = b.atom_id 
  AND lower(atom_name) like '%obsolete%';


33.  Concepts with branded ingredients in the same concept as as other
     non-branded-ingredient RXNORM atoms.
     
SELECT DISTINCT a.concept_id FROM classes a, classes b
WHERE a.tobereleased in ('Y','y')
  AND a.source like 'RXNORM%'
  AND a.tty='BN'
  AND a.concept_id = b.concept_id
  AND b.tobereleased in ('Y','y')
  AND b.source like 'RXNORM%' AND b.tty !='BN';


34.  Component Merges: Concepts containing component atoms with 
     different case-insensitve strings. 

SELECT concept_id FROM classes
WHERE source like 'RXNORM%' AND termgroup like 'RXNORM%/SCDC'
  AND tobereleased IN ('Y','y')
GROUP BY concept_id HAVING count(distinct isui)>1;


35.  Component Splits: Concepts containing component atoms 
     with the same case-insensitive string as a component atom
     in a different concept.

SELECT a.concept_id FROM classes a, classes b
WHERE a.concept_id ! b.concept_id
  AND a.isui = b.isui
  AND a.source like 'RXNORM%'
  AND a.termgroup like 'RXNORM%/SCDC'
  AND a.tobereleased IN ('Y','y')
  AND b.source like 'RXNORM%'
  AND b.termgroup like 'RXNORM%/SCDC'
  AND b.tobereleased IN ('Y','y');


36.  Concepts with ingredients in the same concept as other 
     non-ingredient RXNORM atoms.

SELECT distinct a.concept_id FROM classes a, classes b
WHERE a.tobereleased IN ('Y','y')
  AND a.source like 'RXNORM%'
  AND a.tty='IN'
  AND a.concept_id = b.concept_id
  AND b.tobereleased IN ('Y','y')
  AND b.source like 'RXNORM%'
  AND b.tty !='IN';


37.  Concepts expressed in terms of their precise ingredients (components
     whose name does not match its base ingredient).

SELECT distinct scdc.atom_id, scdc.concept_id, inga.atom_id as ing_id
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
WHERE source like 'RXNORM%'
  AND termgroup like 'RXNORM%/SCD'
  AND tobereleased in ('Y','y')
GROUP BY concept_id HAVING count(distinct isui)>1;


39.  Concepts containing normal form atoms with different case-sensitive
     strings (SUI).

SELECT concept_id FROM classes
WHERE source like 'RXNORM%'
  AND termgroup like 'RXNORM%/SCD'
  AND tobereleased IN ('Y','y')
GROUP BY concept_id HAVING count(distinct sui)>1;


40.  Normal form splits: concepts containing normal form atoms with the same
     case-insensitive string as normal form atoms in other concepts.

SELECT a.concept_id FROM classes a, classes b
WHERE a.concept_id != b.concept_id
  AND a.isui = b.isui and a.source like 'RXNORM%'
  AND a.termgroup like 'RXNORM%/SCD'
  AND a.tobereleased IN ('Y','y')
  AND b.source like 'RXNORM%'
  AND b.termgroup like 'RXNORM%/SCD'
  AND b.tobereleased IN ('Y','y');


41. SNOMEDCT ingredient concepts without MTH/PN.

SELECT concept_id_2 as concept_id
FROM relationships a, classes b
WHERE a.source like 'RXNORM%' and a.tobereleased in ('Y','y')
  AND a.relationship_attribute in ('ingredient_of','precise_ingredient_of')
  AND concept_id_2 = b.concept_id
  AND b.source like 'SNOMEDCT%' AND b.tobereleased in ('Y','y')
MINUS
SELECT concept_id FROM classes
WHERE source='MTH' AND tty='PN'
 AND tobereleased in ('Y','y');


42. Snomed CT Clinical drugs w/out RXNORM Forms that are not
    veterinary drugs.

SELECT a.concept_id from classes a, attributes b
WHERE a.source like 'SNOMEDCT%'
  AND a.tobereleased IN ('Y','y')
  AND a.concept_id = b.concept_id
  AND attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
  AND b.tobereleased in ('Y','y')
MINUS
(SELECT concept_id FROM classes
 WHERE source  like 'RXNORM%'
   AND termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
   AND tobereleased IN ('Y','y')
 UNION
 SELECT concept_id FROM context_relationships a, classes b
 WHERE a.relationship_name = 'PAR' AND a.source like 'SNOMEDCT%'
   AND atom_id_1 = atom_id
   AND parent_treenum like 
     (SELECT '%.'||b.aui||'%' FROM classes b, atoms c
      WHERE c.atom_name = 'Veterinary proprietary drug AND/OR biological'
        AND b.atom_id = c.atom_id
        AND b.source like 'SNOMEDCT%'
        AND b.tobereleased in ('Y','y')));


43. Snomed CT Clinical drugs w/out RXNORM Forms that are
    veterinary drugs.

SELECT a.concept_id from classes a, attributes b
WHERE a.source like 'SNOMEDCT%'
  AND a.tobereleased IN ('Y','y')
  AND a.concept_id = b.concept_id
  AND attribute_name = 'SEMANTIC_TYPE'
  AND attribute_value = 'Clinical Drug'
  AND b.tobereleased in ('Y','y')
  AND a.atom_id IN
     (SELECT atom_id_1 FROM context_relationships a
      WHERE a.relationship_name = 'PAR' AND a.source like 'SNOMEDCT%'
        AND parent_treenum like 
          (SELECT '%.'||b.aui||'%' FROM classes b, atoms c
           WHERE c.atom_name = 'Veterinary proprietary drug AND/OR biological'
             AND b.atom_id = c.atom_id
             AND b.source like 'SNOMEDCT%'
             AND b.tobereleased in ('Y','y')))
MINUS
SELECT concept_id FROM classes
WHERE source  like 'RXNORM%'
  AND termgroup IN (SELECT termgroup FROM termgroup_rank WHERE termgroup like 'RXNORM%' and tty in ('SCD','SBD'))
  AND tobereleased IN ('Y','y');

44. Non-chemical ingredient concepts.

SELECT concept_id_2 as concept_id FROM relationships    
WHERE relationship_attribute IN ('ingredient_of','precise_ingredient_of')
  AND source  like 'RXNORM%'
  AND relationship_level = 'S'
MINUS
SELECT concept_id FROM attributes
WHERE attribute_name ='SEMANTIC_TYPE'
  AND attribute_value IN
   (SELECT semantic_type
    FROM semantic_types WHERE is_chem='Y');

45. Concepts with releasable SCD/SBD and RxNorm status U.

select distinct a.concept_id from classes a, attributes b
 where a.concept_id = b.concept_id
  and a.source  like 'RXNORM%'
  and tty in ('SCD','SBD')
  and attribute_name = 'RX_NORM_STATUS'
  and attribute_value = 'U'
  and a.tobereleased in ('Y','y');
  

46. Concepts with releasable SCD/SBD and RxNorm status H.

select distinct a.concept_id from classes a, attributes b
 where a.concept_id = b.concept_id
  and a.source  like 'RXNORM%'
  and tty in ('SCD','SBD')
  and attribute_name = 'RX_NORM_STATUS'
  and attribute_value = 'U'
  and a.tobereleased in ('Y','y');


---
You are currently subscribed to nlmreg as: suresh@lhc.nlm.nih.gov
To unsubscribe send a blank email to leave-nlmreg-342F@umlsinfo.nlm.nih.gov
