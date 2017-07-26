/**************************************************
*
* File:  $INIT_HOME/etc/sql/meme_views.sql
* Author:  EMW, BAC, others
*
* Remarks 
*     This file contains views for looking at the preferred relationships 
*     of a concept and the preferred atom_id of a concept
*     And multiple meaning cases
* 02/24/2009 BAC (1-GCLNT): Improvements to ambig views
# 01/14/2009 BAC (1-J2CSD): Final improvements to ensure MID-MID results are communicated to MRD.
* 12/15/2008 BAC (1-J2CSD): More views to support Automated QA improvements.
* 10/15/2008 BAC (1-J2CSD): Additional views to support Automated QA improvements.
* 02/28/2008 TK (1-GMAC4) : Added 3 views: qa_diff_results, src_mid_diff_results, mid_mid_diff_results
* 06/11/2007 BAC (1-EH3Y3): Fix released_source_version to handle RXNORM sources properly.
* 04/26/2007 TK (1-E730H) : Added released_source_version view
* 12/21/2006 BAC (1-D3YLZ): Added separated_strings_include_pn
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
-- RELEASED_SOURCE_VERSION VIEW
-- (1) previous name has been released (IMETA not null)
-- (2) previous name is null (new sources, never released)
-- (3) non-normalized cases (never released)
-- union
-- (4) previous name has not been released but an earlier version than that has
--    (for cases like RXNORM, updated more than once per release cycle)
-- union
-- (5) previous name has not been released and no earlier versions have been
--    (for cases that were put in and updated but have not yet been released)
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW released_source_version AS
SELECT * FROM source_version  a
WHERE previous_name in 
  (SELECT source FROM sims_info WHERE insert_meta_version IS NOT NULL) 
   OR previous_name IS NULL
   OR current_name IN (SELECT source FROM source_rank WHERE source!=normalized_source)
UNION
(SELECT a.source,current_name, b.source
 FROM source_version a, 
   (SELECT stripped_source, b.source, insert_meta_version
    FROM sims_info b, source_rank c 
    WHERE b.source = c.source
      AND insert_meta_version IN
        (SELECT max(insert_meta_version) FROM sims_info d, source_rank e
         WHERE d.source = e.source
           AND c.stripped_source = e.stripped_source
           AND insert_meta_version is not null
           AND d.source NOT IN (SELECT NVL(current_name,'null') FROM source_version))
    ) b
 WHERE a.source = b.stripped_source 
   AND insert_meta_version IS NOT NULL
   AND previous_name IN (SELECT source FROM sims_info WHERE insert_meta_version IS NULL))
UNION
SELECT source, current_name, NULL FROM source_version a
WHERE NOT EXISTS (SELECT 1 FROM sims_info b, source_rank c
                  WHERE insert_meta_version IS NOT NULL
                    AND b.source=c.source
                    AND a.current_name != b.source 
                    AND a.source=c.stripped_source) 
   AND current_name IN (SELECT normalized_source FROM source_rank)
   AND previous_name IS NOT NULL;
   
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

CREATE OR REPLACE VIEW separated_strings_include_pn AS
SELECT /*+ RULE */ distinct
       c1.concept_id as concept_id_1,
       c2.concept_id as concept_id_2,
       c1.isui
FROM   classes c1, classes c2, ambig_isui b
WHERE  c1.isui = b.isui and c2.isui=b.isui
  AND c1.language='ENG' and c2.language='ENG'
  AND  c1.concept_id < c2.concept_id
  and c1.tobereleased in  ('Y','y','?')
  and c2.tobereleased in  ('Y','y','?');


CREATE OR REPLACE VIEW separated_strings_full AS
SELECT /*+ PARALLEL c1 */ DISTINCT
       c1.concept_id as concept_id_1, c1.atom_id as atom_id_1,
       c2.concept_id as concept_id_2, c2.atom_id as atom_id_2,
       a1.atom_name as atom_name_1, a2.atom_name as atom_name_2,
       c1.isui
FROM   classes c1, atoms a1, classes c2, atoms a2
WHERE  c1.isui = c2.isui
  AND c1.language='ENG' and c2.language='ENG'
  AND  c1.concept_id < c2.concept_id
/*  and (c1.source != 'MTH' OR c1.termgroup not like '%PN') */
  and c1.tobereleased in  ('Y','y','?')
/*  and (c2.source != 'MTH' OR c2.termgroup not like '%PN') */
  and c2.tobereleased in  ('Y','y','?')
  AND a1.atom_id = c1.atom_id and a2.atom_id=c2.atom_id;

----------------------------------------------------------------------------
-- MID_SRC_DIFF_RESULTS
-- Current: MID QA RESULTS
-- Previous: SRC QA RESULTS
--
-- Formula:  Previous + adjustment = Current
-- No current/previous name manipulations needed for comparison, all are current.
--
-- Compute:
-- (1) Current,Previous share keys
-- (2) Keys in Previous, not Current
--
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW mid_src_diff_results AS
WITH
 prev AS
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM src_qa_results a
       GROUP BY qa_id, name, value),
 adj AS
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM qa_adjustment
       GROUP BY qa_id, name, value),
 cur AS
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM mid_qa_results a
       WHERE name not like 'mid_%'
       GROUP BY qa_id, name, value)
SELECT DISTINCT
    prev.name, prev.value, 
    prev.qa_id as qa_id_1, (prev.qa_count+nvl(adj.qa_count,0)) count_1, 
    cur.qa_id qa_id_2, cur.qa_count count_2, 
    sysdate as timestamp, 'CHANGED' as type
FROM prev, adj, cur
WHERE prev.qa_id = adj.qa_id (+)
  AND prev.name = adj.name (+)
  AND prev.value = adj.value (+)
  AND prev.name = cur.name
  AND prev.value = cur.value
  AND (prev.qa_count + nvl(adj.qa_count,0)) != cur.qa_count
UNION ALL
SELECT DISTINCT 
    prev.name, prev.value,
    prev.qa_id as qa_id_1, prev.qa_count + NVL(adj.qa_count,0) count_1, 
    (select distinct qa_id from cur) as qa_id_2, 0 count_2, 
    sysdate as timestamp, 'MISSING'
FROM prev, adj
WHERE prev.qa_id = adj.qa_id (+)
  AND prev.name = adj.name (+)
  AND prev.value = adj.value (+)
  AND (prev.qa_count + NVL(adj.qa_count,0)) != 0
  AND (prev.name, prev.value) IN
  (SELECT name, value
   FROM prev
   MINUS
   SELECT name, value
   FROM cur);
      
-- Report Form
CREATE OR REPLACE VIEW mid_src_adj_report AS
WITH
 prev AS
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM src_qa_results a
       GROUP BY qa_id, name, value),
 adj AS
      (SELECT qa_id, name, value, sum(qa_count) qa_count, description, timestamp
       FROM qa_adjustment
       WHERE timestamp > NVL((SELECT min(timestamp) FROM atomic_actions),sysdate)
       GROUP BY qa_id, name, value, description, timestamp),
 cur AS
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM mid_qa_results a
       WHERE name not like 'mid_%'
       GROUP BY qa_id, name, value)
SELECT DISTINCT
   cur.name, cur.value, 
   cur.qa_count cur_count, prev.qa_count prev_count, adj.qa_count adj_count, 
   adj.qa_id, adj.timestamp, adj.description, 'CHANGED' type
FROM prev, adj, cur
WHERE prev.qa_id = adj.qa_id
  AND prev.name = adj.name
  AND prev.value = adj.value
  AND prev.name = cur.name
  AND prev.value = cur.value
UNION ALL
SELECT DISTINCT 
   prev.name, prev.value, 
   0, prev.qa_count, adj.qa_count, 
   adj.qa_id, adj.timestamp, adj.description, 'MISSING' type
FROM prev, adj
WHERE prev.qa_id = adj.qa_id
  AND prev.name = adj.name
  AND prev.value = adj.value
  AND (prev.name, prev.value) IN
  (SELECT name, value
   FROM prev
   MINUS
   SELECT name, value
   FROM cur);
   
----------------------------------------------------------------------------
-- SRC_INV_DIFF_RESULTS
-- Current: SRC QA RESULTS
-- Previous: INV QA RESULTS
--
-- Formula:  Previous = Current
-- No current/previous name manipulations needed for comparison, all are current.
-- No adjustments - should be exact
--
-- Compute:
-- (1) Current,Previous share keys
-- (2) Keys in Previous, not Current
-- (3) Keys in Current, not Previous
--
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW src_inv_diff_results AS
WITH
 prev AS
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM inv_qa_results a
       GROUP BY qa_id, name, value),
 cur AS
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM src_qa_results a
       GROUP BY qa_id, name, value)
SELECT DISTINCT
    prev.name, prev.value, 
    prev.qa_id as qa_id_1, prev.qa_count count_1, 
    cur.qa_id qa_id_2, cur.qa_count count_2, 
    sysdate as timestamp, 'CHANGED' as type
FROM prev, cur
WHERE prev.name = cur.name
  AND prev.value = cur.value
  AND prev.qa_count != cur.qa_count
UNION ALL
SELECT DISTINCT 
    prev.name, prev.value,
    prev.qa_id as qa_id_1, prev.qa_count count_1, 
    (select distinct qa_id from cur) as qa_id_2, 0 count_2, 
    sysdate as timestamp, 'MISSING'
FROM prev
WHERE (prev.name, prev.value) IN
  (SELECT name, value
   FROM prev
   MINUS
   SELECT name, value
   FROM cur)
UNION ALL
SELECT DISTINCT 
    cur.name, cur.value,
    (select distinct qa_id from prev)as qa_id_1, 0 count_1, 
    cur.qa_id as qa_id_2, cur.qa_count count_2, 
    sysdate as timestamp, 'NEW'
FROM cur
WHERE (cur.name, cur.value) IN
  (SELECT name, value
   FROM cur
   MINUS
   SELECT name, value
   FROM prev);

----------------------------------------------------------------------------
-- SRC_OBSOLETE_DIFF_RESULT
-- Current: SRC QA RESULTS
-- Previous: SRC OBSOLETE QA RESULTS
--
-- Formula: Previous + adjustment = Current (or adjustment is null)
-- Convert keys to "previous name" for comparison.  Except for sources
-- that do not have a previous version (e.g. only inserted once so far)
-- In that case, use "-NEW" tag on current_name to support adjustment mechanism.
-- Adjustments are tracked with "previous name" values
--
-- Compute:
-- (1) Current,Previous share keys
-- (2) Previous not in current (unless previous is OBSOLETE)
-- (3) Current not in previous (including current is NEW)
--
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW src_obsolete_diff_results AS
WITH 
 cur AS 
   (SELECT qa_id, name, REPLACE(value,current_name,NVL(previous_name,current_name||'-NEW')) value,
		sum(qa_count) qa_count
    FROM src_qa_results, released_source_version
    WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = current_name
    GROUP BY qa_id, name, REPLACE(value,current_name,NVL(previous_name,current_name||'-NEW'))),
 adj AS
   (SELECT qa_id_1, name, value, sum(qa_count) qa_count
    FROM qa_diff_adjustment
    GROUP BY qa_id_1, name, value), 
 prev AS
       (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM src_obsolete_qa_results, released_source_version
       WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = previous_name
         AND current_name IS NOT NULL
       GROUP BY qa_id, name, value)
SELECT DISTINCT
 	prev.name, prev.value, 
 	prev.qa_id as qa_id_1, (prev.qa_count+nvl(adj.qa_count,0)) count_1, 
	cur.qa_id as qa_id_2, cur.qa_count as count_2, 
	sysdate as timestamp, 'CHANGED' as type
FROM cur, adj, prev
WHERE prev.qa_id = adj.qa_id_1 (+)
  AND prev.name = adj.name (+)
  AND prev.value = adj.value (+)
  AND cur.qa_id = prev.qa_id
  AND cur.name = prev.name
  AND cur.value = prev.value
  AND (prev.qa_count + nvl(adj.qa_count,0) != cur.qa_count OR
        adj.qa_count IS NULL)
UNION ALL
SELECT DISTINCT 
	prev.name, prev.value, 
	prev.qa_id as qa_id_1, prev.qa_count + NVL(adj.qa_count,0) count_1,
	(select distinct qa_id from cur) as qa_id_2, 0 count_2, 
	sysdate as timestamp, 'MISSING'
FROM prev, adj
WHERE prev.qa_id = qa_id_1 (+)
  AND prev.name = adj.name (+)
  AND prev.value = adj.value (+)
  AND prev.qa_count + NVL(adj.qa_count,0) != 0
  AND (prev.name, prev.value) IN
    (SELECT name, value FROM prev
     MINUS
     SELECT name, value FROM cur)
UNION ALL
SELECT DISTINCT 
	cur.name, cur.value,
	(select distinct qa_id from prev) as qa_id_1, NVL(adj.qa_count,0) count_1, 
	cur.qa_id as qa_id_2, cur.qa_count count_2, 
	sysdate as timestamp, 'NEW'
FROM cur, adj
WHERE cur.qa_id = qa_id_1 (+)
  AND cur.name = adj.name (+)
  AND cur.value = adj.value (+)
  AND 0 + NVL(adj.qa_count,0) != cur.qa_count
  AND (cur.name, cur.value) IN
    (SELECT name, value FROM cur
     MINUS
     SELECT name, value FROM prev);
  
-- Report form
CREATE OR REPLACE VIEW src_obsolete_adj_report AS
WITH 
 cur AS 
   (SELECT qa_id, name, REPLACE(value,current_name,NVL(previous_name,current_name||'-NEW')) value,
		sum(qa_count) qa_count
    FROM src_qa_results, released_source_version
    WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = current_name
    GROUP BY qa_id, name, REPLACE(value,current_name,NVL(previous_name,current_name||'-NEW'))),
 adj AS
   (SELECT qa_id_1, qa_id_2, 
       name, value, sum(qa_count) qa_count, description, timestamp
    FROM qa_diff_adjustment
    GROUP BY qa_id_1, qa_id_2, name, value, description, timestamp), 
 prev AS
       (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM src_obsolete_qa_results, released_source_version
       WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = previous_name
         AND current_name IS NOT NULL
       GROUP BY qa_id, name, value)
SELECT DISTINCT
   cur.name, cur.value, 
   cur.qa_count cur_count, prev.qa_count prev_count, adj.qa_count adj_count, 
   adj.qa_id_1, adj.qa_id_2, adj.timestamp, adj.description, 'CHANGED' type
FROM cur, adj, prev
WHERE prev.qa_id = adj.qa_id_1
  AND cur.qa_id = adj.qa_id_2
  AND prev.name = adj.name
  AND prev.value = adj.value
  AND cur.name = prev.name
  AND cur.value = prev.value
UNION ALL
SELECT DISTINCT 
   prev.name, prev.value, 
   0 cur_count, prev.qa_count prev_count, adj.qa_count adj_count, 
   adj.qa_id_1, adj.qa_id_2, adj.timestamp, adj.description, 'MISSING' type
FROM prev, adj
WHERE prev.qa_id = qa_id_1
  AND (SELECT DISTINCT qa_id FROM cur) = qa_id_2
  AND prev.name = adj.name
  AND prev.value = adj.value
  AND (prev.name, prev.value) IN
    (SELECT name, value FROM prev
     MINUS
     SELECT name, value FROM cur)
UNION ALL
SELECT DISTINCT 
   cur.name, cur.value, 
   cur.qa_count cur_count, 0 prev_count, adj.qa_count adj_count, 
   adj.qa_id_1, adj.qa_id_2, adj.timestamp, adj.description, 'NEW' type
FROM cur, adj
WHERE cur.qa_id = qa_id_1
  AND (SELECT DISTINCT qa_id FROM prev) = qa_id_2
  AND cur.name = adj.name
  AND cur.value = adj.value
  AND (cur.name, cur.value) IN
    (SELECT name, value FROM cur
     MINUS
     SELECT name, value FROM prev);
     
---------------------------------------------------------------------------
-- INV_OBSOLETE_DIFF_RESULTS
-- Current: INV QA RESULTS
-- Previous: INV QA RESULTS
--
-- Convert keys to "previous name" for comparison.
-- No adjustments at this level.
--
-- Compute:
-- (1) Current,Previous share keys
-- (2) Current not in previous
-- (3) Previous not in current
--
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW INV_OBSOLETE_DIFF_RESULTS as
WITH
  cur AS 
   (SELECT qa_id, name, REPLACE(value,current_name,NVL(previous_name,current_name||'-NEW')) as value, 
           sum(qa_count) qa_count
    FROM inv_qa_results a, released_source_version b
    WHERE SUBSTR(a.value,1,INSTR(a.value||',',',')-1) = current_name
    GROUP BY qa_id, name, REPLACE(value,current_name,NVL(previous_name,current_name||'-NEW'))),
  prev AS 
   (SELECT qa_id, name, value, sum(qa_count) qa_count
    FROM inv_qa_results a, released_source_version b
    WHERE SUBSTR(a.value,1,INSTR(a.value||',',',')-1) = previous_name
    GROUP BY qa_id, name, value)
SELECT cur.name, cur.value,
  prev.qa_id qa_id_1, prev.qa_count count_1, 
  cur.qa_id qa_id_2, cur.qa_count count_2,
  sysdate as timestamp, 'CHANGED' as type
FROM cur, prev
WHERE cur.name = prev.name 
  AND cur.value = prev.value
UNION
SELECT cur.name, cur.value,
  (select distinct qa_id from prev), 0,
  cur.qa_id, cur.qa_count, 
  sysdate, 'NEW'
FROM cur
WHERE (name,value) IN
  (SELECT name,value FROM cur
   MINUS
   SELECT name,value FROM prev)
UNION
SELECT prev.name, prev.value,
  prev.qa_id, prev.qa_count,
  (select distinct qa_id from cur), 0, 
  sysdate, 'MISSING'
FROM prev
WHERE (name,value) IN
  (SELECT name,value FROM prev
   MINUS
   SELECT name,value FROM cur);
      
----------------------------------------------------------------------------
-- MID_MID_DIFF_RESULT
-- Current: MID QA RESULTS
-- Previous: earliest MID QA HISTORY
--
-- Formula: Previous + adjustment = Current (or adjustment is null)
-- Convert keys to "previous name" for comparison.  
-- Adjustments are tracked with "previous name" values.
--
-- Compute:
-- (1) Current,Previous share keys
-- (2) Previous not in current
-- (3) Current not in previous
--
----------------------------------------------------------------------------
CREATE OR REPLACE VIEW mid_mid_diff_results AS
WITH
 cur AS
   (SELECT qa_id, name, 
           REPLACE(value, current_name, NVL(previous_name,current_name||'-NEW')) value,
           sum(qa_count) qa_count
    FROM mid_qa_results a, released_source_version b
    WHERE name like 'mid_%'
      AND SUBSTR(value,1,INSTR(value||',',',')-1) = current_name
      AND current_name IN 
        (SELECT source FROM sims_info WHERE insert_meta_version IS NULL)
    GROUP BY qa_id, name, REPLACE(value, current_name, NVL(previous_name,current_name||'-NEW'))
    UNION ALL
    SELECT qa_id, name, value, sum(qa_count) qa_count
    FROM mid_qa_results a, released_source_version b
    WHERE name like 'mid_%'
      AND SUBSTR(value,1,INSTR(value||',',',')-1) = current_name
      AND current_name IN (SELECT source FROM sims_info WHERE insert_meta_version IS NOT NULL)
      GROUP BY qa_id, name, value
    UNION
    SELECT qa_id, name, value, sum(qa_count) qa_count
    FROM mid_qa_results a
    WHERE name like 'mid_%'
      AND SUBSTR(value,1,INSTR(value||',',',')-1) IN
        (SELECT SUBSTR(value,1,INSTR(value||',',',')-1) FROM mid_qa_results
         MINUS
         SELECT current_name FROM released_source_version)
    GROUP BY qa_id, name, value),
 adj AS
   (SELECT 'mid_'||name name, value, sum(qa_count) qa_count
    FROM qa_adjustment
    WHERE qa_id = (select min(qa_id) from mid_qa_history)
    GROUP BY qa_id, name, value), 
 prev AS 
   (SELECT qa_id, name, value, sum(qa_count) qa_count
    FROM mid_qa_history
    WHERE qa_id = (select min(qa_id) from mid_qa_history)
      AND name like 'mid_%'
    GROUP BY qa_id, name, value)
SELECT DISTINCT
 	prev.name, prev.value, 
 	prev.qa_id as qa_id_1, (prev.qa_count+nvl(adj.qa_count,0)) count_1, 
	cur.qa_id as qa_id_2, cur.qa_count count_2, 
	sysdate as timestamp, 'CHANGED' as type
FROM cur, adj, prev
WHERE prev.name = adj.name (+)
   AND prev.value = adj.value (+)
   AND cur.name = prev.name
   AND cur.value = prev.value
   AND prev.qa_count + nvl(adj.qa_count,0) != cur.qa_count
UNION ALL
SELECT DISTINCT 
	prev.name, prev.value,
	prev.qa_id as qa_id_1, prev.qa_count + NVL(adj.qa_count,0) count_1, 
	(select distinct qa_id from cur) as qa_id_2, 0 count_2, 
	sysdate as timestamp, 'MISSING'
FROM prev, adj
WHERE prev.name = adj.name (+)
  AND prev.value = adj.value (+)
  AND prev.qa_count + NVL(adj.qa_count,0) != 0
  AND (prev.name, prev.value) IN
    (SELECT name, value FROM prev 
     MINUS
	 SELECT name,value FROM cur)
UNION ALL
SELECT DISTINCT 
	cur.name, cur.value, 
	(select distinct qa_id from prev) as qa_id_1, NVL(adj.qa_count,0) count_1,
	cur.qa_id as qa_id_2, cur.qa_count count_2, sysdate as timestamp, 'NEW'
FROM cur, adj
WHERE cur.name = adj.name (+)
  AND cur.value = adj.value (+)
  AND 0 + NVL(adj.qa_count,0) != cur.qa_count
  AND (cur.name, cur.value) IN
    (SELECT name,value FROM cur
     MINUS
     SELECT name,value FROM prev);
     
-- Report form of comparisons
CREATE OR REPLACE VIEW mid_mid_adj_report AS
WITH
 cur AS
   (SELECT qa_id, name, 
           REPLACE(value, current_name, NVL(previous_name,current_name||'-NEW')) value,
           sum(qa_count) qa_count
    FROM mid_qa_results a, released_source_version b
    WHERE name like 'mid_%'
      AND SUBSTR(value,1,INSTR(value||',',',')-1) = current_name
      AND current_name IN 
        (SELECT source FROM sims_info WHERE insert_meta_version IS NULL)
      GROUP BY qa_id, name, REPLACE(value, current_name, NVL(previous_name,current_name||'-NEW'))
    UNION ALL
    SELECT qa_id, name, value, sum(qa_count) qa_count
    FROM mid_qa_results a, released_source_version b
    WHERE name like 'mid_%'
      AND SUBSTR(value,1,INSTR(value||',',',')-1) = current_name
      AND current_name IN (SELECT source FROM sims_info WHERE insert_meta_version IS NOT NULL)
      GROUP BY qa_id, name, value
    UNION
    SELECT qa_id, name, value, sum(qa_count) qa_count
    FROM mid_qa_results a
    WHERE name like 'mid_%'
      AND SUBSTR(value,1,INSTR(value||',',',')-1) IN
        (SELECT SUBSTR(value,1,INSTR(value||',',',')-1) FROM mid_qa_results
         MINUS
         SELECT current_name FROM released_source_version)
    GROUP BY qa_id, name, value),
 adj AS
   (SELECT qa_id, 'mid_'||name name, value, sum(qa_count) qa_count, description, timestamp
    FROM qa_adjustment
    WHERE qa_id = (select min(qa_id) from mid_qa_history)
    GROUP BY qa_id, name, value, description, timestamp), 
 prev AS 
   (SELECT qa_id, name, value, sum(qa_count) qa_count
    FROM mid_qa_history
    WHERE qa_id = (select min(qa_id) from mid_qa_history)
      AND name like 'mid_%'
    GROUP BY qa_id, name, value)
SELECT 
   cur.name, cur.value, 
   cur.qa_count cur_count, prev.qa_count prev_count, adj.qa_count adj_count, 
   adj.qa_id, adj.timestamp, adj.description, 'CHANGED' type
FROM cur, adj, prev
WHERE prev.name = adj.name
   AND prev.value = adj.value
   AND cur.name = prev.name
   AND cur.value = prev.value
   AND prev.qa_id = adj.qa_id
UNION ALL
SELECT DISTINCT 
   prev.name, prev.value, 
   0 cur_count, prev.qa_count prev_count, adj.qa_count adj_count, 
   adj.qa_id, adj.timestamp, adj.description, 'MISSING' type
FROM prev, adj
WHERE prev.name = adj.name
  AND prev.value = adj.value
  AND prev.qa_id = adj.qa_id
  AND (prev.name, prev.value) IN
    (SELECT name, value FROM prev 
     MINUS
	 SELECT name,value FROM cur)
UNION ALL
SELECT DISTINCT 
   cur.name, cur.value, 
   cur.qa_count cur_count, 0 prev_count, adj.qa_count adj_count, 
   adj.qa_id, adj.timestamp, adj.description, 'NEW' type
FROM cur, adj
WHERE cur.name = adj.name
  AND cur.value = adj.value
  AND (SELECT distinct qa_id FROM prev) = adj.qa_id
  AND (cur.name, cur.value) IN
    (SELECT name,value FROM cur
     MINUS
     SELECT name,value FROM prev);  

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