/**************************************************
*
* File:  $INIT_HOME/etc/sql/meme_views.sql
* Author:  EMW, BAC, others
*
* Remarks 
*     This file contains views for looking at the preferred relationships 
*     of a concept and the preferred atom_id of a concept
*     And multiple meaning cases
*
* 06/09/2004 3.11.0: Released
* 06/08/2004 3.10.2: separated_strings and amibg views use ENG only
* 05/26/2004 3.10.1: +mdba_cur
* 03/17/2004 3.10.0: Released
* 03/11/2004 (3.9.1): Parallelize ambig_sui
* 06/05/2003 (3.9.0): Released for MEME4
* 03/12/2003 (3.8.0): Released
* 03/04/2003 (3.7.1): added dba_cutoff and ic_system_status, rel_directionality
* 11/27/2002 (3.7.0): Released
* 5/9/2000:  Released to URL /MEME/Data/meme_views.sql
* 9/18/2000: SRSTY view fixed to only read STYs from SRDEF.
*            separated_strings_full
* 11/03/2000: Changes released to NLM
* 11/27/2001: DBA views added
* 04/16/2002 (3.5.0): +mdba views
* 06/10/2002 (3.5.1): +chemical_concepts;
* 09/06/2002 (3.6.0): Released
* 09/12/2002 (3.6.1): mdba_mom_m added.
*
* Version Info:
* Release: 4
* Version: 11.0
* Authority: BAC 
* Date: 06/09/2004
*
*****************************************************************************/
-- View of preferred atom ids
-- this query can be used elsewhere to get 

--CREATE OR REPLACE VIEW preferred_atoms;

--CREATE OR REPLACE view preferred_relationships as


----------------------------------------------------------------------------
-- SEMANTIC CONTENT VIEWS
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW chemical_concepts AS
SELECT DISTINCT /*+ PARALLEL(a) FULL(a) */ concept_id 
FROM attributes a, semantic_types b
WHERE a.attribute_name='SEMANTIC_TYPE'
  AND a.attribute_value = b.semantic_type
  AND tobereleased in ('Y','y')
  AND editing_chem = 'Y';


----------------------------------------------------------------------------
-- SEPARATED STRING VIEWS
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW ambig_isui AS
SELECT /*+ PARALLEL(c) */ isui from classes c
WHERE language='ENG'
GROUP BY isui HAVING count(distinct concept_id)>1;

CREATE OR REPLACE VIEW separated_strings AS
SELECT /*+ RULE */ distinct
       c1.concept_id as concept_id_1,
       c2.concept_id as concept_id_2,
       c1.isui
FROM   classes c1, classes c2, ambig_isui b
WHERE  c1.isui = b.isui and c2.isui=b.isui
  AND c1.language='ENG' and c2.language='ENG'
  AND  c1.concept_id < c2.concept_id
  and (c1.source != 'MTH' OR c1.termgroup not like '%PN')
  and c1.tobereleased in  ('Y','y','?')
  and (c2.source != 'MTH' OR c2.termgroup not like '%PN')
  and c2.tobereleased in  ('Y','y','?');

CREATE OR REPLACE VIEW separated_strings_full AS
SELECT /*+ RULE */ distinct
       c1.concept_id as concept_id_1, c1.atom_id as atom_id_1,
       c2.concept_id as concept_id_2, c2.atom_id as atom_id_2,
       a1.atom_name as atom_name_1, a2.atom_name as atom_name_2,
       c1.isui
FROM   classes c1, atoms a1, classes c2, atoms a2, ambig_isui b
WHERE  c1.isui = b.isui and c2.isui=b.isui
  AND c1.language='ENG' and c2.language='ENG'
  AND  c1.concept_id < c2.concept_id
/*  and (c1.source != 'MTH' OR c1.termgroup not like '%PN') */
  and c1.tobereleased in  ('Y','y','?')
/*  and (c2.source != 'MTH' OR c2.termgroup not like '%PN') */
  and c2.tobereleased in  ('Y','y','?')
  AND a1.atom_id = c1.atom_id and a2.atom_id=c2.atom_id;


----------------------------------------------------------------------------
-- The following views replaced MEME2 tables
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW ic_definitions AS
SELECT ic_name, ic_short_dsc, ic_long_dsc
FROM integrity_constraints;

CREATE OR REPLACE VIEW srsty AS
SELECT ui, sty_rl "STY", stn_rtn "STN", def
FROM srdef WHERE rt='STY';

----------------------------------------------------------------------------
-- The following views replaced MEME tables
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW dba_cutoff AS
SELECT status as edit
FROM system_status WHERE system='dba_cutoff';

CREATE OR REPLACE VIEW ic_system_status AS
SELECT status, 1 as version
FROM system_status WHERE system='ic_system';

CREATE OR REPLACE VIEW rel_directionality AS
SELECT relationship_name as short_form, long_name as long_form
FROM inverse_relationships WHERE long_name is not null;


----------------------------------------------------------------------------
-- The following views are for dba purposes
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW mdba_sql AS
SELECT sid,serial#,sql_text
FROM v$sql, v$session
WHERE sql_address = address
  AND users_executing>0;

CREATE OR REPLACE VIEW mdba_cur AS
SELECT count(*) ct, sql_text
FROM v$open_cursor group by sql_text order by 1;

CREATE OR REPLACE VIEW mdba_mom AS
SELECT count(*) as ct,merge_set,status FROM mom_merge_facts
GROUP BY merge_set,status;

CREATE OR REPLACE VIEW mdba_mom_m AS
SELECT count(distinct a.concept_id||'|'||b.concept_id) ct 
FROM mom_merge_facts, classes a, classes b
WHERE atom_id_1=a.atom_id AND atom_id_2=b.atom_id
  AND a.concept_id != b.concept_id;

CREATE OR REPLACE VIEW mdba_mom_sm AS
SELECT count(distinct a.concept_id||'|'||b.concept_id) ct 
FROM mom_merge_facts, source_classes_atoms a,
	source_classes_atoms b
WHERE atom_id_1=a.atom_id AND atom_id_2=b.atom_id
  AND a.concept_id != b.concept_id;

CREATE OR REPLACE VIEW mdba_kill AS
SELECT username,program,
  'ALTER SYSTEM KILL SESSION '''||sid||','||serial#||'''' as stmt
FROM v$session;

