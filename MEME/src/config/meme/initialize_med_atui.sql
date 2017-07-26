--
-- Brian Carlsen 06/13/2006
-- This script was used to initialize the new MED<year> semantics
--

--
-- Load ATUI data
-- 
TRUNCATE TABLE source_Attributes;
INSERT INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
           sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
           attribute_value, generated_status, source, status, released,
           tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, atom_id, concept_id,
       aui, 'AUI', '', 'S', 'MED'||atn, '*'||ct, 'Y', 'NLM-MED',
           'R', 'N','Y',0,'N','','','*'||ct
    FROM
     (SELECT /*+ PARALLEL(coc) */  count(*) ct,
            TO_CHAR(publication_date,'YYYY') as atn, heading_id
      FROM coc_headings coc
      WHERE source = 'NLM-MED'
        AND major_topic = 'Y'
      GROUP BY heading_id, TO_CHAR(publication_date,'YYYY')) a, classes b
    WHERE heading_id = atom_id
      AND b.tobereleased in ('Y','y');

INSERT INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
           sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
           attribute_value, generated_status, source, status, released,
           tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, atom_id, concept_id,
       aui, 'AUI', '', 'S', 'MED'||atn, ''||ct, 'Y', 'NLM-MED',
           'R', 'N','Y',0,'N','','',''||ct
    FROM
     (SELECT /*+ PARALLEL(coc) */  count(*) ct,
            TO_CHAR(publication_date,'YYYY') as atn, heading_id
      FROM coc_headings coc
      WHERE source = 'NLM-MED'
      GROUP BY heading_id, TO_CHAR(publication_date,'YYYY')) a, classes b
    WHERE heading_id = atom_id
      AND b.tobereleased in ('Y','y');

INSERT INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
           sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
           attribute_value, generated_status, source, status, released,
           tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, c.atom_id, c.concept_id,
       c.aui, 'AUI', '', 'S', 'MED'||year, ''||sum(ct), 'Y', 'NLM-MED',
           'R', 'N','Y',0,'N','','',''||sum(ct)
    FROM attributes a,
        (SELECT year, subheading_qa, count(*) as ct
         FROM (SELECT /*+ PARALLEL(coc) */ DISTINCT citation_set_id,
                  TO_CHAR(publication_date,'YYYY') as year
               FROM coc_headings coc
                       WHERE source='NLM-MED') a, coc_subheadings b
         WHERE a.citation_set_id = b.citation_set_id
         GROUP BY year, subheading_qa) b, classes c
    WHERE a.attribute_name = 'QA'
      AND a.atom_id = c.atom_id
      AND a.source = (SELECT current_name FROM source_version WHERE source='MSH')
      AND a.tobereleased in ('Y','y')
      AND c.tobereleased in ('Y','y')
      AND a.attribute_value = b.subheading_qa
    GROUP BY c.concept_id, c.atom_id, c.aui, b.year;

--
-- Create new attributes_ui view of MED<year> entries (where hashcode is populated)
--
drop table tbac;
create table tbac as select * from attributes_ui where attribute_name like 'MED%' and root_source='NLM-MED';

update tbac a set hashcode = '*', attribute_name = substr(a.attribute_name,1,7)
where substr(a.attribute_name,8,1)= '*';

drop table tbac2;
create table tbac2 as
select a.atui, a.root_source, a.attribute_level,
 b.attribute_name, b.attribute_value, a.sg_id, a.sg_type, a.sg_qualifier,
 a.source_atui
from tbac a, source_attributes b
where a.attribute_name = b.attribute_name
  and a.hashcode is not null
  and a.hashcode = substr(b.attribute_value,1,1)
  and a.sg_id = b.sg_id
union
select a.atui, a.root_source, a.attribute_level,
 b.attribute_name, b.attribute_value, a.sg_id, a.sg_type, a.sg_qualifier,
 a.source_atui
from tbac a, source_attributes b
where a.attribute_name = b.attribute_name
  and a.hashcode is null
  and b.attribute_value not like '*%'
  and a.sg_id = b.sg_id;

--
-- Run as QA check
--
select count(*),attribute_name,sg_id from tbac2
group by attribute_name,sg_id
having count(*)>2;

--
-- Fix up actual attributes_ui table
--
drop table t_attributes_ui;
create table t_attributes_ui as select * from attributes_ui where root_source !='NLM-MED'
union select * from tbac2;

alter session set sort_area_size=400000000;
alter session set hash_area_size=400000000;
drop table attributes_ui;
rename t_attributes_ui to attributes_ui;
grant all on attributes_ui to public;
exec meme_system.reindex('attributes_ui','N',' ');

