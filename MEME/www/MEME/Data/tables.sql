*****************************************************************************
*
* File:  $MEME_HOME/sql/tables.sql
* Author:  EMW, BAC, others
*
* Remarks:  This script is used to create the MEME3 oracle tables.
*
*     If you add another table to the MEME system you must do the following
*     1. Add any indexes to $MEME_HOME/indexes.sql
*     2. Add the table to $MEME_HOME/synonyms.sql
*     3. Call MEME_SYSTEM.register_table(table_name)
*     4. $MRD_HOME/sql/tables.sql
*     5. Add descriptions to $MEME_HOME/doc/tables/table_documentation.pl
*
* To test for tables that need to have PCTFREE and PCTUSED updated
* do this in sqlplus:
*
* SQL> @$ORACLE_HOME/rdbms/admin/utlchain.sql
* SQL> analyze table <table> list chained rows into chained_rows;
* SQL> select table_name, count(*) from chained_rows group by table_name
*
* To determine where STORAGE initial clause must be larger do this
* in sqlplus:
*
* SQL> select count(*),segment_name from user_extents 
*      group by segment_name order by 1;
* 
* Version Info:
* Release: 4
* Version: 38.0
* Authority: BAC
* Date: 12/13/2004
*
* 01/14/2004 3.38.1: sims_info has longer fields and whats_new field
* 12/13/2004 3.38.0: Released
* 12/07/2004 3.37.3: +mid_validation_results
* 11/29/2004 3.37.2: +action_log, aui_history,
*                    -operations_queue, -authority_rank,groups, -ic_violations
* 11/16/2004 3.37.1: stringtab.text_value needs to be bigger (3k)
*                    attributes.attribute_value (150)
* 10/06/2004 3.37.0: RELEASED
* 09/29/2004 3.36.3: Primary keys for editors, editor_preferences, language,
*                    code_map
* 09/20/2004 3.36.2: cui_map changes
* 08/23/2004 3.36.1: meme_properties gets 3 new fields: definition,example,reference
* 08/09/2004 3.36.0: Released
* 07/27/2004 3.35.2: content_veiws changes.
* 07/15/2004 3.35.1: +termgroup_rank.release_rank, +content_views.is_generated
* 07/06/2004 3.35.0: Revised content_views, content_view_members
* 06/09/2004 3.34.0: attribute_name => 50, Released
* 05/25/2004 3.33.2: +content_views, +content_view_members
* 04/29/2004 3.33.1: sr_predicate (fields are 2000 instead of 200).
*                    detail fields now 4000 not 1000.
* 04/28/2004 3.33.0: Released
* 04/26/2004 3.32.2: AUI fields can be null
* 04/01/2004 3.32.1: atoms_ui, attributes_ui, relationships_ui
*                    updated to reflect source*ui fields
* 03/17/2004 3.32.0: Released
* 03/15/2004 3.31.1: coc_subheadings not an IOT
* 03/08/2004 3.31.0: Used for load of oa_mid2004 and 2004AA mrd
* 02/20/2004 3.30.0: -foreign_attributes, -dead_foreign_attributes
*                    +sg_meme_id, +sg_meme_data_type
* 02/10/2004 3.29.1: source_replacement revised
* 02/06/2004 3.29.0: Released
* 02/04/2004 3.28.2: change to source_replacement
* 12/02/2003 3.28.1: +test_suite_statistics
* 12/01/2003 3.28.0: Released
* 11/20/2003 3.27.1: Added inverse_relationships.release_name
* 10/09/2003 3.27.0: Released
* 10/08/2003 3.26.1: Added auto_fix field to mid_validation_queries
* 09/30/2003 3.26.0: - max_action_tab
* 09/08/2003 3.25.0: Released
* 09/03/2003 3.24.1: +sr_predicate.source_cui_match
*                    +cui_history.rela,map_reason
* 08/29/2003 3.24.0: +inverse_relationships_ui
* 07/18/2003 3.23.0: Released
* 07/09/2003 3.22.1: +foreign_attributes, +dead_foreign_attributes
* 06/16/2003 3.22.0: Final Schema used for 2003AB Release
* 06/05/2003 3.21.0: Minor fixes.
* 05/12/2003 3.20.0: Removed sg_attributes, sg_rels (& dead ones)
* 05/07/2003 3.19.3: Removed sg_attributes, sg_rels (& dead ones)
* 05/02/2003 3.19.2: relationships_ui, attributes_ui, rel_directionality_flag
* 04/11/2003 3.19.1: source_coc_{sub,}headings and source_coc_headings_todelete
*                    tables added
* 04/09/2003 3.19.0: Released
* 04/04/2003 3.18.2: New fields for MEME4 src files & SNOMED
* 03/27/2003 3.18.1: Final sims_info/dead_sims_info fixes.
* 03/25/2003 3.18.0: small fixes to sims_info/dead_sims_info.  
*                    Released to support schema changes.
* 03/17/2003 3.17.1: +relationships_ui
* 03/12/2003 3.17.0: Released
* 03/04/2003 3.16.1: {source_,}source_rank/sims_info changes
*                    authority => 50
*                    language => +iso_lat varchar2(2)
*                    new system_status table
*                    made dba_cutoff and ic_system_status into views
*		     new deleted_cuis table
*                    removed sg_status
*                    new atom_ordering table, foreign_classes +eng_aui
* 02/18/2003 3.16.0: Released
* 01/21/2003 3.15.5: +allocated_ui_ranges
* 01/17/2003 3.15.4: +source_replacement
* 12/18/2002 3.15.3: MONITORING clause added.
* 12/16/2002 3.15.2: source_rank, sims_info changes.  Also source_source_rank
*                    and dead_sims_info.
* 12/05/2002 3.15.1: We need an allocated_ui_ranges 
*                    (type,low,high,root_source,dsc) table
* 11/27/2002 3.15.0: +reindex_tables, released
* 09/06/2002 3.14.0: Released.
* 05/29/2002 3.13.1: +aui fields ( {,dead_}{,foreign_}classes, 
*                                  source_classes_atoms);
* 05/28/2002 3.13.0: {source_,}source_rank has language field
*                    Released.
* 04/16/2002 3.12.0: Released.
* 03/22/2002 3.11.4: +atoms_ui: like string_ui but for AUIs
*                    -mom_facts_to_process, -mom_new_atoms
* 02/25/2002 3.11.3: dead_relationships.source was changed from 10 chars to 20
* 01/17/2002 3.11.2: added dead_sims_info
* 01/15/2002 3.11.1: sims_info changed slightly
* 12/09/2001 3.11.0: atom_name -> 3000 chars (4000 was causing problems
*                    for the indexes).
* 12/05/2001 3.10.0: Final changes to tables sizes, released to NLM
*  		     for use in oa_mid2003
* 11/19/2001 3.9.3:  Added sims_info table
* 10/31/2001 3.9.2:  parent_treenum, hierarchical_code extended to 1000 chars
* 10/22/2001 3.9.1:  atom_name -> 4000 chars
* 09/05/2001 3.8.2:  coc_headings changed, context_type added to source_rank
* 		     Released to NLM
* 07/11/2001 3.8.1:  operations_queue is CACHE not NOCACHE
* 06/04/2001 3.8.0:  changes to coc_headings, coc_subheadings
*                    Released.
* 05/04/2001 3.7.1:  +foreign_classes, +dead_foreign_classes
* 04/20/2001 3.7.0:  Released to NLM
* 04/10/2001 3.6.7:  +source_rank.lots, sg_type -> varchar2(50)
* 04/03/2001 3.6.6:  -cui_history, +coc_{sub,}headings, +cui_map,
*		     +deleted_cuis, +source_rank.source_family
*			+source_source_rank.source_family
* 03/16/2001 3.6.5:  {dead,source,}context_relationships
* 		     +meme_properties, +lui_assignment
* 01/09/2001 3.6.4:  +mid_validation_queries
* 01/05/2001 3.6.3:  termgroup -> 40 chars, atom_name -> 1200 chars
*                    merge_set -> 30 chars
* 12/15/2000 3.6.2:  cui_history  
* 12/05/2000 3.5.1:  +dead_context_relationships. +context_relationships_pk
*		     pctfree 15 pctused 75 For: atomic_actions,concept_status, 
*		     classes, relationships, attributes, molecular_actions
*                    CACHE for small tables.
* 11/03/2000 3.5.0:  Release changes to NLM
* 10/24/2000 3.4.5:  INITIAL extents adjusted to reflect growing segment counts
* 10/11/2000 3.4.4:  pctfree 20 pctused 60 for atomic_actions,concept_status
*		     and source_classes_atoms
* 9/21/2000  3.4.31: _DATE changed to _date (this was a problem for The
* 		     MRDEvent.dtd and parser).
* 9/19/2000  3.4.3:  operations_queue: primary key (mid_event_id,row_id)
* 8/24/2000  3.4.2:  classes primary key is atom_id.  secondary: concept_id
* 		      sr_predicate has tty_match field
* 8/17/2000  3.4.1:  Increased storage parameters for some tables.
* 8/09/2000  3.4.0:  Released for MEME3 deployment 
* 8/01/2000  3.3.93: meme_indexes changed
* 7/26/2000  3.3.92: qa tables added by RBE
* 7/21/2000  3.3.91: application_help, application_versions version
*                    changed to varchar2 field, object changed to object_name
* 6/29/2000  3.3.9: primary keys for:atomic_actions, dead_stringtab
*                   stringtab.  Added meme_schedule table
* 6/20/2000  3.3.8: inverse_rel_attributes: no primary key, CACHE
* 6/14/2000  3.3.7: molecular_actions: primary key (molecule_id)
* 6/08/2000  3.3.6: Increase INITIAL extent size to minmize fragmentation
*                   Use tablespace storage parameters (1M extents)
*	 	    NO index organized tables.
* 6/01/2000  3.3.5: Comments describing tables were added.  
*                   operations_queue.queue_id -> mid_event_id
*
* 5/26/2000  3.3.4: hetero_relationships->sg_relationships
*                   sg_attributes, source_termgroup_rank, source_source_rank
* 5/17/2000  3.3.3: added operations_queue table
* 5/16/2000  3.3.2: mom_safe_replacement table structure changed slightly
* 5/10/2000  3.3.1: atom_name, string, normstr fields extended to 1200 chars  
* 5/9/2000   3.3.0: atomic_actions:  {old,new}_value can be null
*            Released to http://meow.nlm.nih.gov/MEME3/Data/tables.sql
*          
* 4/6/2000   3.2.4: touch up to sr_predicate, sg_id fields made 50 chars
*		 atoms field expanded, etc...
*
*            3.2.3: Old WTH schema tables for EMS/WMS management removed
*		Enhancement: the MEOW schema ones should be added
*
* 03/28/2000 3.2.2: Create heterogeneous relationships tables, and fix
* 		    schema for CRACS & others to account for sg_ids.d
* 
* 
*****************************************************************************/
set autocommit on;
ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS';

------------------------------------------------------------------
-- MEME3 Tables
------------------------------------------------------------------

--
-- This table tracks ALL Actions
--
DROP TABLE action_log;
CREATE TABLE action_log(
	action_id		NUMBER(12) NOT NULL,
	transaction_id		NUMBER(12),
	work_id			NUMBER(12),
	undo_action_id		NUMBER(12),
	elapsed_time		NUMBER(12) NOT NULL,
	action			VARCHAR2(500) NOT NULL,
	synchronize		VARCHAR2(1) NOT NULL, 
	authority		VARCHAR2(50) NOT NULL,
	timestamp		DATE NOT NULL,
	document		CLOB,
	CONSTRAINT ael_pk PRIMARY KEY (action_id)
)
PCTFREE 10 PCTUSED 80 MONITORING;

--
--  This table is used to track different activities.  
--  Rows are inserted into this table by MEME_UTILITY.log_operation   
--
DROP TABLE activity_log ;
CREATE TABLE activity_log (
	transaction_id	NUMBER(12) DEFAULT 0,
	work_id		NUMBER(12) DEFAULT 0,
	row_sequence	NUMBER(12) DEFAULT 0,
	timestamp	DATE,
	elapsed_time	NUMBER(12) DEFAULT 0,
	authority	VARCHAR2(50),
	activity	VARCHAR2(100),
	detail		VARCHAR2(4000)
)
PCTFREE 10 PCTUSED 80 MONITORING;

--
-- This table trackes UI ranges that have been specifically
-- allocated to external organizations. The table is not
-- really used for anything except to remember.
--
DROP TABLE allocated_ui_ranges;
CREATE TABLE allocated_ui_ranges (
	type		VARCHAR2(50),
	low_ui		VARCHAR2(10),
	high_ui		VARCHAR2(10),
	root_source 	VARCHAR2(20),
	description	VARCHAR2(2000) 
)
PCTFREE 10 PCTUSED 80 MONITORING;

--
-- This table is used to track help information for
-- different packages.  Eventually tools in the Common application
-- were going to allow editing & searching of this help database
--
DROP TABLE application_help;
CREATE TABLE application_help(
	application	VARCHAR2(25),
	topic		VARCHAR2(100),
	timestamp	DATE,
	authority	VARCHAR2(50),
	release		NUMBER(12),
	version		VARCHAR2(20),
	text		VARCHAR2(2000) 
)
PCTFREE 10 PCTUSED 80 MONITORING;
	
--
-- This table will eventually track the current version of a
-- component.  Scripts that load components into the database
-- should insert rows into this table
-- Enforcement will eventually be done by MEME_SYSTEM
--
DROP TABLE application_versions;
CREATE TABLE application_versions(
	release		NUMBER(12),
	version		VARCHAR2(20),
	timestamp	DATE,
	authority	VARCHAR2(50),
	object_name	VARCHAR2(50),
	comments	VARCHAR2(100),
	enforce_flag	CHAR(1),
	current_version	CHAR(1)
)
PCTFREE 10 PCTUSED 80 MONITORING;

--
-- Tracks atomic actions
-- (secondary indexes:  molecule_id, row_id)
-- (FK: molecule_id must be in molecular_actions)
--
DROP TABLE atomic_actions;
CREATE TABLE atomic_actions(
	molecule_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	atomic_action_id 	NUMBER(12) DEFAULT 0 NOT NULL
			CONSTRAINT atomic_actions_pk PRIMARY KEY,
	action 			VARCHAR2(2) NOT NULL,
	table_name 		VARCHAR2(50) NOT NULL,
	row_id 			NUMBER(12) DEFAULT 0 NOT NULL,
	new_version_id 		NUMBER(12) DEFAULT 0,
	old_version_id 		NUMBER(12) DEFAULT 0,
	old_value 		VARCHAR2(500),
	new_value 		VARCHAR2(500),
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	status			VARCHAR2(1), 
	action_field		VARCHAR2(25)
)
PCTFREE 15 PCTUSED 75 MONITORING
STORAGE (INITIAL 1000M);

--
-- Applies an ordering of atoms within a source
-- for the purpose of sorting worklist content
--
DROP TABLE atom_ordering;
CREATE TABLE atom_ordering(
	atom_id 		NUMBER(12) NOT NULL,
	root_source 		VARCHAR2(40) NOT NULL,
	order_id 		VARCHAR2(100) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 200M);

--
-- Atom names (join with classes)
-- (secondary indexes: atom_name)
-- (FK: atom_id in classes, atom_name in string_ui)
-- DEPRECATE THIS TABLE
--
DROP TABLE atoms;
CREATE TABLE atoms(
	atom_id 		NUMBER(12) NOT NULL
				  CONSTRAINT atoms_pk PRIMARY KEY,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	atom_name 		VARCHAR2(3000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 200M);

--
-- Tracks atom unique identifiers over time
-- secondary index on sui
--
DROP TABLE atoms_ui;
CREATE TABLE atoms_ui (
	aui			VARCHAR2(10) NOT NULL
				  CONSTRAINT atoms_ui_pk PRIMARY KEY,
	sui			VARCHAR2(10) NOT NULL,
	stripped_source		VARCHAR2(20) NOT NULL,
	tty			VARCHAR2(20) NOT NULL,
	code			VARCHAR2(50),
	source_aui		VARCHAR2(50), 
	source_cui		VARCHAR2(50),
	source_dui		VARCHAR2(50)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 200M);

--
-- attributes table 
-- (secondary indexes atom_id, concept_id, (attribute_name,attribute_value) )
--
DROP TABLE attributes;
CREATE TABLE attributes(
	atom_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	attribute_id 		NUMBER(12) 
		CONSTRAINT attributes_pk PRIMARY KEY,
	attribute_level 	VARCHAR2(1) NOT NULL,
	attribute_name 		VARCHAR2(100) NOT NULL,
	attribute_value 	VARCHAR2(200),
	generated_status	VARCHAR2(1) NOT NULL,
	source		 	VARCHAR2(20) NOT NULL,
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50),
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	concept_id 		NUMBER(12) NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	source_rank 		NUMBER(12)  DEFAULT 0 NOT NULL,
	preferred_level 	VARCHAR2(1),
	last_molecule_id 	NUMBER(12) DEFAULT 0 NOT NULL,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0 NOT NULL,
	rank 			NUMBER DEFAULT 0 NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	atui			VARCHAR2(12),
	source_atui		VARCHAR2(50),
	hashcode		VARCHAR2(100),
	sg_id			VARCHAR2(50),
	sg_type			VARCHAR2(50),
	sg_qualifier		VARCHAR2(50),
	sg_meme_data_type	VARCHAR2(10),
	sg_meme_id		NUMBER(12) DEFAULT 0
)
PCTFREE 15 PCTUSED 75 MONITORING
STORAGE (INITIAL 1000M);

-- Tracks attribute unique identifiers over time
-- Secondary index on sg_id
DROP TABLE attributes_ui;
CREATE TABLE attributes_ui (
	atui			VARCHAR2(12) NOT NULL,
	root_source		VARCHAR2(20) NOT NULL,
	attribute_level		VARCHAR2(1) NOT NULL,
	attribute_name		VARCHAR2(100) NOT NULL,
	hashcode		VARCHAR2(100),
	sg_id			VARCHAR2(50) NOT NULL,
	sg_type			VARCHAR2(50) NOT NULL,
	sg_qualifier		VARCHAR2(50),
	source_atui		VARCHAR2(50) 
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 200M);

-- 
-- This table contains info about AUI history
-- 
DROP TABLE aui_history;
CREATE TABLE aui_history (
	map_id			NUMBER(12) NOT NULL,
    	aui1			VARCHAR2(10) NOT NULL,
    	cui1			VARCHAR2(10) NOT NULL,
    	ver			VARCHAR2(50), 
    	relationship_name	VARCHAR2(10) NOT NULL,
    	relationship_attribute	VARCHAR2(100),
    	map_reason		VARCHAR2(4000),
    	aui2			VARCHAR2(10),
    	cui2			VARCHAR2(10),
	authority		VARCHAR2(50) NOT NULL,
	timestamp		DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M);

-- classes table (primary key is concept_id,atom_id)
-- (secondary indexes: atom_id, lui, isui, sui, source, code )
DROP TABLE classes;
CREATE TABLE classes(
	atom_id 		NUMBER(12) NOT NULL,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	source 			VARCHAR2(20) NOT NULL,
	termgroup 		VARCHAR2(40) NOT NULL,
	tty	 		VARCHAR2(20),
	termgroup_rank 		NUMBER(12) DEFAULT 0 NOT NULL,
	code 			VARCHAR2(30),
	sui 			VARCHAR2(10) NOT NULL,
	lui 			VARCHAR2(10) NOT NULL,
	generated_status	VARCHAR2(1) NOT NULL,
	last_release_cui 	VARCHAR2(10),
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	concept_id 		NUMBER(12) NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	sort_key 		NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0 NOT NULL,
	last_release_rank	NUMBER(12) DEFAULT 0,
	suppressible		VARCHAR2(10) NOT NULL,
	last_assigned_cui	VARCHAR2(10),
	isui			VARCHAR2(10) NOT NULL,
	aui			VARCHAR2(10),
	source_aui		VARCHAR2(50), 
	source_cui		VARCHAR2(50),
	source_dui		VARCHAR2(50),
	language		VARCHAR2(10),
	CONSTRAINT classes_pk PRIMARY KEY (atom_id)
)
PCTFREE 15 PCTUSED 75 MONITORING
STORAGE (INITIAL 500M);


-- This table tracks the co-occurence data used to
-- generate MRCOC, MRSAT, and MRLO data
--
-- It joins with the coc_subheadings table to produce
-- the COA field for the Medline MRCOC rows.
--
-- heading_id is an atom_id
-- heading_code is the code of the heading_id in classes
-- subheading_set_id should be equal to
--   to_number(citation_set_id || LPAD(heading_id,12,0))
-- source should be in source_rank
-- coc_type must be a valid coc_type (see code_map)
-- citation set id must come 
DROP TABLE coc_headings;
CREATE TABLE coc_headings (
        citation_set_id         NUMBER(12) NOT NULL,
        publication_date        DATE NOT NULL,
        heading_id              NUMBER(12) NOT NULL,
        major_topic             VARCHAR2(1) NOT NULL ,
                CHECK (major_topic in ('Y','N')),
        subheading_set_id       NUMBER(12),
        source                  VARCHAR2(20) NOT NULL,
        coc_type                VARCHAR2(10)
)
PARTITION BY RANGE (publication_date)
  (
   PARTITION coc_headings_1965 VALUES LESS THAN ('01-jan-1965 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_1970 VALUES LESS THAN ('01-jan-1970 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_1975 VALUES LESS THAN ('01-jan-1975 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_1980 VALUES LESS THAN ('01-jan-1980 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_1985 VALUES LESS THAN ('01-jan-1985 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_1990 VALUES LESS THAN ('01-jan-1990 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_1995 VALUES LESS THAN ('01-jan-1995 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_2000 VALUES LESS THAN ('01-jan-2000 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_2005 VALUES LESS THAN ('01-jan-2005 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M ),
   PARTITION coc_headings_2010 VALUES LESS THAN ('01-jan-2010 00:00:00')
        PCTFREE 5 PCTUSED 95 STORAGE (INITIAL 100M NEXT 100M )
  );

-- This table tracks subheadings of the heading_ids in the
-- coc_headings table above.  
--
-- subheading_id is an atom_id
-- subheading_qa is the QA attribute of that atom_id in attributes.
-- 
-- With the full medline data, this table is about 1GB.  Since it is
-- always accessed on citation_set_id, subheading_id we wanted to
-- avoid ever having to sort this table and so made it an IOT.
-- There is considerable overlap of citation_Set_id, so we chose
-- COMPRESS 1 to eliminate duplicate instances of citation_set_id
-- to keep the table smaller.
--
DROP TABLE coc_subheadings;
CREATE TABLE coc_subheadings (
-- This field is not really necessary
--        subheading_id           NUMBER(12) NOT NULL,
        citation_set_id         NUMBER(12) NOT NULL,
-- this will never be > 99
        subheading_set_id       NUMBER(2) NOT NULL,
        subheading_qa           CHAR(2) NOT NULL,
        subheading_major_topic  VARCHAR2(1) NOT NULL,
	CONSTRAINT coc_subheadings_pk PRIMARY KEY 
	   (citation_set_id, subheading_set_id, subheading_qa,
	    subheading_major_topic),
        CHECK (subheading_major_topic in ('Y','N'))
)
--ORGANIZATION INDEX COMPRESS 1
STORAGE (INITIAL 1200M);


-- This table tracks commonly used codes and what they map to.
-- It groups codes together into categories
DROP TABLE code_map;
CREATE TABLE code_map(
	code			VARCHAR2(50) NOT NULL,
 	type			VARCHAR2(50) NOT NULL, 
 	value			VARCHAR2(4000) NOT NULL,
 	CONSTRAINT code_map_pk PRIMARY KEY (code,type)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- One row per concept
-- (secondary index: cui )
DROP TABLE concept_status;
CREATE TABLE concept_status(
	concept_id 		NUMBER(12) 
				  CONSTRAINT concept_status_pk PRIMARY KEY,
	cui			VARCHAR2(10),
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	dead 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	preferred_atom_id 	NUMBER(12) DEFAULT 0 NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0,
	editing_authority	VARCHAR2(50),
	editing_timestamp	DATE,
	approval_molecule_id	NUMBER(12) DEFAULT 0 NOT NULL
)
PCTFREE 15 PCTUSED 75 MONITORING
STORAGE (INITIAL 100M);

-- Context relationships as loaded from *raw[23] files
-- concept_id fields are NOT maintained
-- (secondary indexes: atom_id_1, atom_id_2)
--
-- parent_treenum is expressed as a sequence of '.' separated AUIs
-- detailing the path to/from the root atom
--
DROP TABLE context_relationships;
CREATE TABLE context_relationships(
        relationship_id         NUMBER(12) NOT NULL
		CONSTRAINT context_relationships_pk PRIMARY KEY,
        version_id              NUMBER(12) DEFAULT 0 NOT NULL,
        relationship_level	VARCHAR2(1) NOT NULL,
        atom_id_1               NUMBER(12) DEFAULT 0 NOT NULL,
        relationship_name       VARCHAR2(10) NOT NULL,
        relationship_attribute  VARCHAR2(100),
        atom_id_2               NUMBER(12) DEFAULT 0 NOT NULL,
        source		        VARCHAR2(20) NOT NULL,
        generated_status        VARCHAR2(1) NOT NULL,
        dead                    VARCHAR2(1) NOT NULL,
        status                  VARCHAR2(1) NOT NULL,
        authority               VARCHAR2(50) NOT NULL,
        timestamp               DATE NOT NULL,
        insertion_date          DATE,
        concept_id_1            NUMBER(12) NOT NULL,
        concept_id_2            NUMBER(12) NOT NULL,
        released                VARCHAR2(1),
        tobereleased            VARCHAR2(1),
        source_rank             NUMBER(12) DEFAULT 0 NOT NULL,
        preferred_level         VARCHAR2(1),
        last_molecule_id        NUMBER(12) DEFAULT 0,
        last_atomic_action_id   NUMBER(12) DEFAULT 0,
        rank                    NUMBER DEFAULT 0,
	source_of_label		VARCHAR2(20) NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	hierarchical_code	VARCHAR2(1000),
	parent_treenum		VARCHAR2(1000),
	release_mode		VARCHAR2(10),
	rui			VARCHAR2(12),
	source_rui		VARCHAR2(50),
	relationship_group	VARCHAR2(10),
	sg_id_1			VARCHAR2(50),
	sg_type_1		VARCHAR2(50),
	sg_qualifier_1		VARCHAR2(50),
	sg_meme_data_type_1	VARCHAR2(10),
	sg_meme_id_1		NUMBER(12) DEFAULT 0,
	sg_id_2			VARCHAR2(50),
	sg_type_2		VARCHAR2(50),
	sg_qualifier_2		VARCHAR2(50),
	sg_meme_data_type_2	VARCHAR2(10),
	sg_meme_id_2		NUMBER(12) DEFAULT 0
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 900M);

-- This table tracks mrd states for content view members
--
DROP TABLE content_view_members;
CREATE TABLE content_view_members(
 	meta_ui			VARCHAR2(20) NOT NULL,
	content_view_id		NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) TABLESPACE MID;

-- This table tracks mrd states for content views
--
DROP TABLE content_views;
CREATE TABLE content_views (
	content_view_id		NUMBER(12) NOT NULL,
        contributor	        VARCHAR2(100) NOT NULL,
        contributor_version	VARCHAR2(100) NOT NULL,
	content_view_contributor_url VARCHAR2(1000),
        contributor_date	DATE NOT NULL,
        maintainer	        VARCHAR2(100) NOT NULL,
        maintainer_version	VARCHAR2(100) NOT NULL,
	content_view_maintainer_url VARCHAR2(1000),
        maintainer_date		DATE NOT NULL,
 	content_view_name	VARCHAR2(1000) NOT NULL,
 	content_view_description VARCHAR2(4000) NOT NULL,
 	content_view_algorithm  VARCHAR2(4000) NOT NULL,
 	content_view_category  	VARCHAR2(100) NOT NULL,
 	content_view_subcategory VARCHAR2(100),
	content_view_class	VARCHAR2(100) NOT NULL,
 	content_view_code  	NUMBER NOT NULL, -- Powers of 2
	content_view_previous_meta VARCHAR2(10),
 	cascade	  		VARCHAR2(1) NOT NULL,
	is_generated		VARCHAR2(1),
                        	CHECK (cascade IN ('Y','N')),
                        	CHECK (is_generated IN ('Y','N'))
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) TABLESPACE MID;

-- This table must stay around until cui_map is populated
-- because it is used by MEME_SOURCE_PROCESSING
DROP TABLE cui_history;
CREATE TABLE cui_history (
    	cui1			VARCHAR2(10) NOT NULL,
    	ver			VARCHAR2(50), 
    	relationship_name	VARCHAR2(10) NOT NULL,
    	relationship_attribute	VARCHAR2(100),
    	map_reason		VARCHAR2(4000),
    	cui2			VARCHAR2(10)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M);

-- Tracks the history of activity relating to CUIs
-- This will eventually be considered a "core table"
DROP TABLE cui_map;
CREATE TABLE cui_map( 
	map_id			NUMBER(12) NOT NULL
				CONSTRAINT cui_map_pk PRIMARY KEY,
        cui             	VARCHAR2(10) NOT NULL,
        birth_version   	VARCHAR2(10) NOT NULL,
        death_version   	VARCHAR2(10) NOT NULL,
        mapped_to_cui   	VARCHAR2(10) NOT NULL,
        relationship_name	VARCHAR2(10) NOT NULL,
        relationship_attribute  VARCHAR2(100) NOT NULL,
        map_reason      	VARCHAR2(100),
        almost_sy       	VARCHAR2(1) NOT NULL,
	generated_status	VARCHAR2(1) NOT NULL,
	source		 	VARCHAR2(40) NOT NULL,
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
        authority       	VARCHAR2(50) NOT NULL,
        timestamp       	DATE NOT NULL,
	insertion_date 		DATE,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
        rank            	NUMBER,
	last_molecule_id 	NUMBER(12) DEFAULT 0 NOT NULL,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0 NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- Used by editing interface to lock editors out
--DROP TABLE dba_cutoff;
--CREATE TABLE dba_cutoff(
--        edit                    VARCHAR2(1)
--)
--PCTFREE 10 PCTUSED 80 MONITORING;


-- Not maintained by any code.  When brain surgery needs to be done
-- and undoability needs to be preserved, atomic actions are at times
-- removed from the live table and stored here in case they need
-- to be recovered.  This work is almost ALWAYS done by hand.
DROP TABLE dead_atomic_actions;
CREATE TABLE dead_atomic_actions(
	molecule_id		NUMBER(12) NOT NULL,
	atomic_action_id	NUMBER(12) NOT NULL,
	action			VARCHAR2(1) NOT NULL,
	table_name		VARCHAR2(50) NOT NULL,
	row_id			NUMBER(12) NOT NULL,
	new_version_id		NUMBER(12) DEFAULT 0,
	old_version_id		NUMBER(12) DEFAULT 0,
	old_value		VARCHAR2(500) NOT NULL,
	new_value		VARCHAR2(500) NOT NULL,
	authority		VARCHAR2(50) NOT NULL,
	timestamp		DATE NOT NULL,
	status			VARCHAR2(1),
	action_field		VARCHAR2(25)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- Stores atoms that have been deleted.
DROP TABLE dead_atoms;
CREATE TABLE dead_atoms(
	atom_id 		NUMBER(12) NOT NULL 
				CONSTRAINT dead_atoms_pk PRIMARY KEY,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	atom_name 		VARCHAR2(3000)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- Dead attributes
DROP TABLE dead_attributes;
CREATE TABLE dead_attributes(
	atom_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	attribute_id 		NUMBER(12) 
				   CONSTRAINT dead_attributes_pk PRIMARY KEY,
	attribute_level 	VARCHAR2(1) NOT NULL,
	attribute_name 		VARCHAR2(100) NOT NULL,
	attribute_value 	VARCHAR2(200),
	generated_status	VARCHAR2(1) NOT NULL,
	source		 	VARCHAR2(20) NOT NULL,
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	concept_id 		NUMBER(12) NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	source_rank 		NUMBER(12) DEFAULT 0 NOT NULL,
	preferred_level 	VARCHAR2(1),
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0 NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	atui			VARCHAR2(12),
	source_atui		VARCHAR2(50),
	hashcode		VARCHAR2(100),
	sg_id			VARCHAR2(50),
	sg_type			VARCHAR2(50),
	sg_qualifier		VARCHAR2(50),
	sg_meme_data_type	VARCHAR2(10),
	sg_meme_id		NUMBER(12) DEFAULT 0
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 350M);

-- Dead classes
DROP TABLE dead_classes;
CREATE TABLE dead_classes(
	atom_id 		NUMBER(12) 
		CONSTRAINT dead_classes_pk PRIMARY KEY,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	source 			VARCHAR2(20) NOT NULL,
	termgroup 		VARCHAR2(40) NOT NULL,
	tty	 		VARCHAR2(20),
	termgroup_rank 		NUMBER(12) DEFAULT 0 NOT NULL,
	code 			VARCHAR2(30),
	sui 			VARCHAR2(10) NOT NULL,
	lui 			VARCHAR2(10) NOT NULL,
	generated_status	VARCHAR2(1) NOT NULL,
	last_release_cui 	VARCHAR2(10),
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	concept_id 		NUMBER(12) NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	sort_key 		NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0 NOT NULL,
	last_release_rank	NUMBER(12) DEFAULT 0,
	suppressible		VARCHAR2(10) NOT NULL,
	last_assigned_cui	VARCHAR2(10),
	isui			VARCHAR2(10) NOT NULL,
	aui			VARCHAR2(10),
	source_aui		VARCHAR2(50),
	source_cui		VARCHAR2(50),
	source_dui		VARCHAR2(50),
	language		VARCHAR2(10)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M);

-- Dead concepts
DROP TABLE dead_concept_status;
CREATE TABLE dead_concept_status(
	concept_id 		NUMBER(12) 
			  	 CONSTRAINT dead_concept_status_pk PRIMARY KEY,
	cui			VARCHAR2(10),
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	dead 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	preferred_atom_id 	NUMBER(12) DEFAULT 0 NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0,
	editing_authority	VARCHAR2(50),
	editing_timestamp	DATE,
	approval_molecule_id	NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 45M);

-- Dead context_relationships
DROP TABLE dead_context_relationships;
CREATE TABLE dead_context_relationships(
        relationship_id         NUMBER(12) NOT NULL
		CONSTRAINT dead_context_relationships_pk PRIMARY KEY,
        version_id              NUMBER(12) DEFAULT 0 NOT NULL,
        relationship_level	VARCHAR2(1) NOT NULL,
        atom_id_1               NUMBER(12) DEFAULT 0 NOT NULL,
        relationship_name       VARCHAR2(10) NOT NULL,
        relationship_attribute  VARCHAR2(100),
        atom_id_2               NUMBER(12) DEFAULT 0 NOT NULL,
        source		        VARCHAR2(20) NOT NULL,
        generated_status        VARCHAR2(1) NOT NULL,
        dead                    VARCHAR2(1) NOT NULL,
        status                  VARCHAR2(1) NOT NULL,
        authority               VARCHAR2(50) NOT NULL,
        timestamp               DATE NOT NULL,
        insertion_date          DATE,
        concept_id_1            NUMBER(12) NOT NULL,
        concept_id_2            NUMBER(12) NOT NULL,
        released                VARCHAR2(1),
        tobereleased            VARCHAR2(1),
        source_rank             NUMBER(12) DEFAULT 0 NOT NULL,
        preferred_level         VARCHAR2(1),
        last_molecule_id        NUMBER(12) DEFAULT 0,
        last_atomic_action_id   NUMBER(12) DEFAULT 0,
        rank                    NUMBER DEFAULT 0,
	source_of_label		VARCHAR2(20) NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	hierarchical_code	VARCHAR2(1000),
	parent_treenum		VARCHAR2(1000),
	release_mode		VARCHAR2(10),
	rui			VARCHAR2(12),
	source_rui		VARCHAR2(50),
	relationship_group	VARCHAR2(10),
	sg_id_1			VARCHAR2(50),
	sg_type_1		VARCHAR2(50),
	sg_qualifier_1		VARCHAR2(50),
	sg_meme_data_type_1	VARCHAR2(10),
	sg_meme_id_1		NUMBER(12) DEFAULT 0,
	sg_id_2			VARCHAR2(50),
	sg_type_2		VARCHAR2(50),
	sg_qualifier_2		VARCHAR2(50),
	sg_meme_data_type_2	VARCHAR2(10),
	sg_meme_id_2		NUMBER(12) DEFAULT 0
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M);


-- This table exists to track the translations
-- Foreign strings need to be associated with english
-- strings so there is a additional atom id field
-- To avoid redundancy, the strings are not stored in
-- atoms (they are already in string_ui)
-- Also, foreign strings will not show up on the reports
-- because this table is not viewed by Cproc_Read
DROP TABLE dead_foreign_classes;
CREATE TABLE dead_foreign_classes(
	atom_id 		NUMBER(12) NOT NULL,
	eng_atom_id 		NUMBER(12) NOT NULL,
	eng_aui 		VARCHAR2(10),
	language		VARCHAR2(10) NOT NULL,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	source 			VARCHAR2(20) NOT NULL,
	termgroup 		VARCHAR2(40) NOT NULL,
	tty	 		VARCHAR2(20),
	termgroup_rank 		NUMBER(12) DEFAULT 0 NOT NULL,
	code 			VARCHAR2(30),
	sui 			VARCHAR2(10) NOT NULL,
	lui 			VARCHAR2(10) NOT NULL,
	generated_status	VARCHAR2(1) NOT NULL,
	last_release_cui 	VARCHAR2(10),
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	concept_id 		NUMBER(12) NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	sort_key 		NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0 NOT NULL,
	last_release_rank	NUMBER(12) DEFAULT 0,
	suppressible		VARCHAR2(10) NOT NULL,
	last_assigned_cui	VARCHAR2(10),
	isui			VARCHAR2(10) NOT NULL,
	aui			VARCHAR2(10),
	source_aui		VARCHAR2(50),
	source_cui		VARCHAR2(50),
	source_dui		VARCHAR2(50),
	CONSTRAINT d_f_classes_pk PRIMARY KEY (atom_id)
)
PCTFREE 15 PCTUSED 75 MONITORING
STORAGE (INITIAL 10M);

-- Dead norm strings.  Rows are inserted into this table
-- when classes are deleted or when their tbr value is set to N
DROP TABLE dead_normstr;
CREATE TABLE dead_normstr(
	normstr_id		NUMBER(12)
/* CONSTRAINT dead_normstr_pk PRIMARY KEY */,
	normstr			VARCHAR2(3000)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table contains data used by the SIMS
-- http://meow.nlm.nih.gov/cgi-lti-oracle/SIMS.cgi
DROP TABLE dead_sims_info;
CREATE TABLE dead_sims_info (
	source      		VARCHAR2(20) NOT NULL,
	date_created		DATE,
	meta_year               VARCHAR2(20),
	init_rcpt_date          DATE,
	clean_rcpt_date         DATE,
	test_insert_date        DATE,
	real_insert_date        DATE,
	source_contact          VARCHAR2(4000),
	inverter_contact        VARCHAR2(100),
	nlm_path                VARCHAR2(200),
	apelon_path             VARCHAR2(200),
	inversion_script        VARCHAR2(100),
	inverter_notes_file     VARCHAR2(100),
	conserve_file           VARCHAR2(100),
	sab_list                VARCHAR2(100),
	meow_display_name       VARCHAR2(4000),
	source_desc             VARCHAR2(4000),
	status                  VARCHAR2(10),
	worklist_sortkey_loc    VARCHAR2(100),
	termgroup_list          VARCHAR2(4000),
	attribute_list          VARCHAR2(4000),
	inversion_notes         VARCHAR2(4000),
	release_url_list        VARCHAR2(4000),
	internal_url_list       VARCHAR2(4000),
	notes                   VARCHAR2(4000),
	inv_recipe_loc          VARCHAR2(100),
	suppress_edit_rec	VARCHAR2(1),
	source_official_name	VARCHAR2(4000), /* SRC/VPT */
	source_short_name	VARCHAR2(4000), /* SRC/SSN */
	valid_start_date	DATE,
	valid_end_date		DATE,
	insert_meta_version	VARCHAR2(20), 
	remove_meta_version	VARCHAR2(20),
	nlm_contact		VARCHAR2(100),
	acquisition_contact	VARCHAR2(4000),
	content_contact		VARCHAR2(4000),
	license_contact		VARCHAR2(4000),
 	context_type		VARCHAR2(100),
 	language		VARCHAR2(10),
	character_set		VARCHAR2(20),
	citation		VARCHAR2(4000),
	latest_available	VARCHAR2(100),
	last_contacted		DATE,
	license_info		VARCHAR2(4000),
	versioned_cui		VARCHAR2(10),
	root_cui		VARCHAR2(10),
	attribute_name_list	VARCHAR2(4000),
	term_type_list		VARCHAR2(1000),
	cui_frequency		NUMBER(12),
	term_frequency		NUMBER(12),
	test_insertion_start	DATE,
	test_insertion_end	DATE,
	real_insertion_start	DATE,
	real_insertion_end	DATE,
	editing_start		DATE,
	editing_end		DATE,
	rel_directionality_flag	VARCHAR2(1),
	whats_new		VARCHAR2(4000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- Dead norm words.  Rows are inserted into this table
-- when classes are deleted or when their tbr value is set to N
DROP TABLE dead_normwrd;
CREATE TABLE dead_normwrd(
	normwrd_id		NUMBER(12) NOT NULL,
	normwrd			VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- Dead rels
DROP TABLE dead_relationships;
CREATE TABLE dead_relationships(
	relationship_id 	NUMBER(12) 
			  CONSTRAINT dead_relationships_pk PRIMARY KEY,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	relationship_level 	VARCHAR2(1) NOT NULL,
	atom_id_1 		NUMBER(12) DEFAULT 0 NOT NULL,
	relationship_name 	VARCHAR2(10),
	relationship_attribute 	VARCHAR2(100),
	atom_id_2 		NUMBER(12) DEFAULT 0 NOT NULL,
	source	 		VARCHAR2(20) NOT NULL,
	generated_status	VARCHAR2(1) NOT NULL,
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	concept_id_1 		NUMBER(12) DEFAULT 0 NOT NULL,
	concept_id_2 		NUMBER(12) DEFAULT 0 NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	source_rank 		NUMBER(12) DEFAULT 0 NOT NULL,
	preferred_level 	VARCHAR2(1),
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0 NOT NULL,
	source_of_label		VARCHAR2(20) NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	rui			VARCHAR2(12),
	source_rui		VARCHAR2(50),
	relationship_group	VARCHAR2(10),
	sg_id_1			VARCHAR2(50),
	sg_type_1		VARCHAR2(50),
	sg_qualifier_1		VARCHAR2(50),
	sg_meme_data_type_1	VARCHAR2(10),
	sg_meme_id_1		NUMBER(12) DEFAULT 0,
	sg_id_2			VARCHAR2(50),
	sg_type_2		VARCHAR2(50),
	sg_qualifier_2		VARCHAR2(50),
	sg_meme_data_type_2	VARCHAR2(10),
	sg_meme_id_2		NUMBER(12) DEFAULT 0
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 50M);

-- Dead long attributes
DROP TABLE dead_stringtab;
CREATE TABLE dead_stringtab(
	string_id		NUMBER(12) NOT NULL,
	row_sequence		NUMBER(12) NOT NULL,
	text_total		NUMBER(12) NOT NULL,
	text_value		VARCHAR2(3000) NOT NULL,
	CONSTRAINT dead_stringtab_pk PRIMARY KEY (string_id,row_sequence)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 60M);

-- Dead words.  Rows are inserted into this table
-- when classes are deleted or when their tbr value is set to N
DROP TABLE dead_word_index;
CREATE TABLE dead_word_index(
	atom_id			NUMBER(12) NOT NULL,
	word			VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- Dead cuis.  Rows are inserted into this table during
-- CUI assignment with the <new> flag.
DROP TABLE deleted_cuis;
CREATE TABLE deleted_cuis (
	cui			VARCHAR2(10) NOT NULL,
	lui			VARCHAR2(10) NOT NULL,
	isui			VARCHAR2(10) NOT NULL,
	aui			VARCHAR2(10) NOT NULL,
	root_source		VARCHAR2(20) NOT NULL,
	code  			VARCHAR2(50) NOT NULL,
	timestamp		DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;


-- This table tracks the editing/integrity state of a concept.  
-- The matrix initializer uses it to calculate the concept status
DROP TABLE editing_matrix;
CREATE TABLE editing_matrix(
	concept_id		NUMBER(12) 
				   CONSTRAINT editing_matrix_pk PRIMARY KEY,
	classes_status		VARCHAR2(1) NOT NULL,
	attributes_status	VARCHAR2(1) NOT NULL,
	relationships_status	VARCHAR2(1) NOT NULL,
	integrity_status	VARCHAR2(1) NOT NULL,
	integrity_vector	VARCHAR2(2000),
	clean_molecule_id	NUMBER(12) DEFAULT 0,
	last_app_change		DATE,	
	last_app_change_by	VARCHAR2(50),
	last_unapp_change	DATE, 
	last_unapp_change_by	VARCHAR2(50),
	concept_status		VARCHAR2(1)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 50M);

-- This table tracks valid editors
DROP TABLE editors;
CREATE TABLE editors(
	name			VARCHAR2(10),
	editor_level		NUMBER(12) DEFAULT 0,
	initials		VARCHAR2(10),
	grp			VARCHAR2(10),
	cur 			VARCHAR2(10),
	CONSTRAINT editors_pk PRIMARY KEY (name)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks preferences for valid editors
DROP TABLE editor_preferences;
CREATE TABLE editor_preferences(
	name			VARCHAR2(9),
	initials		VARCHAR2(3),
	show_concept		NUMBER(12) DEFAULT 1,
	show_classes		NUMBER(12) DEFAULT 1, 
	show_relationships	NUMBER(12) DEFAULT 1, 
	show_attributes 	NUMBER(12) DEFAULT 1,
	CONSTRAINT editor_preferences_pk PRIMARY KEY (name)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;


-- This table exists to track the translations
-- Foreign strings need to be associated with english
-- strings so there is a additional atom id field
-- To avoid redundancy, the strings are not stored in
-- atoms (they are already in string_ui)
-- Also, foreign strings will not show up on the reports
-- because this table is not viewed by Cproc_Read
DROP TABLE foreign_classes;
CREATE TABLE foreign_classes(
	atom_id 		NUMBER(12) NOT NULL,
	eng_atom_id 		NUMBER(12) NOT NULL,
	eng_aui 		VARCHAR2(10),
	language		VARCHAR2(10) NOT NULL,	
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	source 			VARCHAR2(20) NOT NULL,
	termgroup 		VARCHAR2(40) NOT NULL,
	tty	 		VARCHAR2(20),
	termgroup_rank 		NUMBER(12) DEFAULT 0 NOT NULL,
	code 			VARCHAR2(30),
	sui 			VARCHAR2(10) NOT NULL,
	lui 			VARCHAR2(10) NOT NULL,
	generated_status	VARCHAR2(1) NOT NULL,
	last_release_cui 	VARCHAR2(10),
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	concept_id 		NUMBER(12) NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	sort_key 		NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0 NOT NULL,
	last_release_rank	NUMBER(12) DEFAULT 0,
	suppressible		VARCHAR2(10) NOT NULL,
	last_assigned_cui	VARCHAR2(10),
	isui			VARCHAR2(10) NOT NULL,
	aui			VARCHAR2(10),
	source_aui		VARCHAR2(50),
	source_cui		VARCHAR2(50),
	source_dui		VARCHAR2(50),
	CONSTRAINT f_classes_pk PRIMARY KEY (atom_id)
)
PCTFREE 15 PCTUSED 75 MONITORING
STORAGE (INITIAL 100M);

-- This table tracks integrity vectors for applications like
-- the matrix initializer, and intetgrity snapshot
DROP TABLE ic_applications;
CREATE TABLE ic_applications(
	application 		VARCHAR2(30) NOT NULL,
	integrity_vector 	VARCHAR2(2000)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks override vectors for particular user levels.
DROP TABLE ic_override;
CREATE TABLE ic_override(
	ic_level		NUMBER(12) DEFAULT 0 NOT NULL,
	override_vector		VARCHAR2(2000)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks data-driven integrity checks that involve a
-- single piece of data (such as "Dont merge a source with itself")
-- Valid type values are described in code_map.
DROP TABLE ic_single ;
CREATE TABLE ic_single (
	ic_name		VARCHAR(10) NOT NULL,
	negation	VARCHAR(1) NOT NULL CHECK (negation IN ('N','Y')),
	type		VARCHAR(20) NOT NULL,
	value		VARCHAR(100) NOT NULL)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks data-driven integrity checks that involve
-- two pieces of data (such as "Dont merge source 1 with source 2")
-- Valid type values are described in code_map.
DROP TABLE ic_pair ;
CREATE TABLE ic_pair (
	ic_name		VARCHAR(10) NOT NULL,
	negation	VARCHAR(1) NOT NULL CHECK (negation IN ('N','Y')),
	type_1		VARCHAR(20) NOT NULL,
	value_1		VARCHAR(100) NOT NULL,
	type_2		VARCHAR(20) NOT NULL,
	value_2		VARCHAR(100) NOT NULL)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This is a "switch" for the integrity system.
-- If status is "OFF" integrities do not run.
--DROP TABLE ic_system_status;
--CREATE TABLE ic_system_status(
--	status			VARCHAR2(3) NOT NULL,
--	version			NUMBER(12) NOT NULL
--)
--PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks valid integrity constraints and descriptions
DROP TABLE integrity_constraints;
CREATE TABLE integrity_constraints(
	ic_name			VARCHAR2(10) NOT NULL,
	v_actions		VARCHAR2(100) NOT NULL,
	c_actions		VARCHAR2(100),
	ic_status		VARCHAR2(1) NOT NULL,
	ic_type			VARCHAR2(1) NOT NULL,
	activation_date		DATE,
	deactivation_date	DATE,
	ic_short_dsc		VARCHAR2(100) NOT NULL,
	ic_long_dsc		VARCHAR2(1000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks valid relationship_attributes and their inverses
-- (including the null rela)
DROP TABLE inverse_rel_attributes;
CREATE TABLE inverse_rel_attributes(
	relationship_attribute	VARCHAR2(100),
	inverse_rel_attribute 	VARCHAR2(100),
	rank			NUMBER(12)  DEFAULT 0 NOT NULL
-- This table is so small, not really needed
--	CONSTRAINT inverse_rel_attributes_pk PRIMARY KEY (relationship_attribute)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks valid relationship names (and their ranks)
DROP TABLE inverse_relationships;
CREATE TABLE inverse_relationships(
	relationship_name 	VARCHAR(20) NOT NULL,
	inverse_name 		VARCHAR2(20) NOT NULL,
	weak_flag		VARCHAR2(20),
	long_name		VARCHAR2(100),
	release_name		VARCHAR2(20),
	rank			NUMBER(12) DEFAULT 0 NOT NULL
-- This table is so small, not really needed
--	CONSTRAINT inverse_relationships_pk PRIMARY KEY (relationship_name)
)	
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks RUIs and their inverses
DROP TABLE inverse_relationships_ui;
CREATE TABLE inverse_relationships_ui (
	rui			VARCHAR2(12) NOT NULL
	    CONSTRAINT ir_ui_pk PRIMARY KEY,
	inverse_rui		VARCHAR2(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M);

-- This table tracks the valid languages (and their abbreviations)
DROP TABLE language;
CREATE TABLE language(
	language		VARCHAR2(20) NOT NULL,
	lat			VARCHAR2(20) NOT NULL,
	iso_lat			VARCHAR2(2),
	CONSTRAINT language_pk PRIMARY KEY (lat)
) PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks valid level and status values for the 4 core tables.
-- For attributes/rels it also tracks valid level/status combinations.
-- Furthermore, it ranks the level,status and level-status values.
DROP TABLE level_status_rank;
CREATE TABLE level_status_rank(
	level_value		VARCHAR2(1),
	status			VARCHAR2(2),
	table_name		VARCHAR2(25) NOT NULL,
	rank			NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table is dropped and created by the
-- $MEME_HOME/bin/assign_luis.csh procedure, but it is
-- an official MEME table and so it should exist in
-- this script.  It is used as the basis for the lui_map
-- table in production which makes MERGED.LUI.
DROP TABLE lui_assignment;
CREATE TABLE lui_assignment(
	sui			VARCHAR2(10) NOT NULL,
	new_lui			VARCHAR2(10) NOT NULL,
	old_lui			VARCHAR2(10) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE
STORAGE (INITIAL 60M);


-- This table tracks maximum id values for things like meme ids.
-- Generally the max_id is the last used (NOT next available)
-- To get the next id  UPDATE max_tab SET max_id=max_id+1...
-- Then select the max_id.  If autocommit is OFF this should be
-- sufficient to ensure that no other processes get your id.
DROP TABLE max_tab;
CREATE TABLE max_tab(
	TABLE_name 		VARCHAR2(50) NOT NULL,
	max_id 			NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table is used by the meme_integrity package to track concepts
-- that are related through integrity checks.  It enables the matrix_updater
-- to correctly assemble the set of concepts it needs to operate on 
-- to "catch up".
-- (secondary indexes: concept_id_1, concept_id_2 )
DROP TABLE meme_cluster_history ;
CREATE TABLE meme_cluster_history (
	name		VARCHAR2(20) NOT NULL,
	concept_id_1    NUMBER(12) NOT NULL, 
	concept_id_2    NUMBER(12) NOT NULL 
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 80M);

-- Each MEME_* package has a meme_*_error method which is called
-- when errors occur.  This method inserts a row into this table
-- which can be looked up later to understand what happened.
DROP TABLE meme_error ;
CREATE TABLE meme_error (
	transaction_id	NUMBER(12) DEFAULT 0,
	work_id		NUMBER(12) DEFAULT 0,
	timestamp	DATE NOT NULL,
	elapsed_time	NUMBER(12) DEFAULT 0,
	authority	VARCHAR2(50),
	activity	VARCHAR2(100),
	detail		VARCHAR2(4000))
PCTFREE 10 PCTUSED 80 MONITORING;

-- This is the official catalog of indexes on meme tables
-- It is used by MEME_SYSTEM.reindex.
DROP TABLE meme_indexes;
CREATE TABLE meme_indexes AS 
SELECT index_name, table_name,
 pct_free, pct_increase, initial_extent, next_extent,
 min_extents, max_extents, tablespace_name, index_type
FROM user_indexes WHERE 1=0;
ALTER TABLE meme_indexes CACHE;

-- This table tracks the columns used by the indexes.
DROP TABLE meme_ind_columns;
CREATE TABLE meme_ind_columns AS
SELECT * FROM user_ind_columns WHERE 1=0;
ALTER TABLE meme_ind_columns CACHE;

-- PL/SQL procedures cannot print to the screen until they have
-- finished running.  To enable tracking of long-running processes
-- this table can be populated as something is running.  polling it
-- based on work_id can tell you where it is along the way. 
-- The MEME_UTILITY.log_progress procedure which populates this table
-- should be enhanced when using Oracle 8i to have an autonomous 
-- transaction scope (so it can commit its inserts for exernal processes)
DROP TABLE meme_progress ;
CREATE TABLE meme_progress (
	transaction_id	NUMBER(12) DEFAULT 0,
	work_id		NUMBER(12) DEFAULT 0,
	row_sequence	NUMBER(12) DEFAULT 0,
	timestamp	DATE,
	elapsed_time	NUMBER(12) DEFAULT 0,
	authority	VARCHAR2(50),
	activity	VARCHAR2(100),
	detail		VARCHAR2(4000))
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table was designed to hold general configuration data.
-- Its original motivation was to store MRFILES, MRCOLS config
-- data so a release could be produced entirely from MID data
-- but eventually tables like ic_system_status and dba_cutoff
-- should be come obsolete as this table can serve their respective
-- purposes in a more general way.
DROP TABLE meme_properties;
CREATE TABLE meme_properties (
 	key		VARCHAR2(100) NOT NULL,
 	key_qualifier	VARCHAR2(100),
 	value		VARCHAR2(4000),
 	description	VARCHAR2(1000),
	definition	VARCHAR2(4000),
	example		VARCHAR2(4000),
	reference	VARCHAR2(1000)
)PCTFREE 10 PCTUSED 80 MONITORING;

-- This table is used to keep a schedule of what level of automated
-- database work can be done.
-- An application finds the most specific entry that corresponds
-- to its name and the current SYSDATE.  The precedence of the fields is:
-- 1. application
-- 2. specific_date
-- 3. day of week - trunc(sysdate)-trunc(sysdate,'d');  (0 (sun) - 6 (sat))
-- 4. start_time/end_time (time range)
--   For time calculations compare to_char(sysdate,'hh24:mi:ss')
--   to start_time (inclusive) and end_time (exclusive).  e.g
-- ... WHERE to_char(sysdate,'hh24:mi:ss') >= start_time
--       AND to_char(sysdate,'hh24:mi:ss') < end_time
--   If start_time is null, end_time must be null.
--
-- The "most specific" row is the one to apply, where tie breaks are
-- performed based on the rank described above.  Having a matching
-- application name is better than having a matching day of the week
-- and a null application name.
-- 
-- cpu_mode is usually one of:  SHUTDOWN, STANDBY, DELAY, NODELAY
-- lock_mode is unspecified
DROP TABLE meme_schedule ;
CREATE TABLE meme_schedule (
	application		VARCHAR2(100),
	specific_date		DATE,
	day_of_week		NUMBER(12),	
	start_time		VARCHAR2(10),
	end_time		VARCHAR2(10),
	cpu_mode		VARCHAR2(20),
	lock_mode		VARCHAR2(20)
) PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This is the official list of tables tracked by the MEME system
-- MEME_SYSTEM.register_table and MEME_SYSTEM.remove_table 
-- will alter this list.
DROP TABLE meme_tables ;
CREATE TABLE meme_tables (
	table_name	VARCHAR(50) NOT NULL)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table normalizes work_ids.  Each kind of "work" done inserts
-- a row into this table and that work_id is used in various other places
-- to group all of the work of a process together.
DROP TABLE meme_work ;
CREATE TABLE meme_work (
	work_id		NUMBER(12) NOT NULL,
	timestamp	DATE NOT NULL,
	type		VARCHAR2(20) NOT NULL,
	authority	VARCHAR2(50) NOT NULL,
	description	VARCHAR2(1000) NOT NULL)
PCTFREE 10 PCTUSED 80 MONITORING;

--
-- This table holds results from MID validation runs
-- 
DROP TABLE mid_validation_results;
CREATE TABLE mid_validation_results (
        result_set_name		VARCHAR2(100) NOT NULL,
        check_type		VARCHAR2(100) NOT NULL,
	check_name		VARCHAR2(54) NOT NULL,
	-- Includes adjustment
	result_count		NUMBER NOT NULL,
	authority		VARCHAR2(50) NOT NULL,
	timestamp		DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table holds queries used by the "MID Validation"
-- to perform semantic validation of the MID database
-- The decision was used to make the application data-driven
-- so that as problems were discovered, checks could be
-- dynamically added
--
-- For rows with make_checklist='Y', the query must return
-- a concept_id field (other fields are OK).
DROP TABLE mid_validation_queries;
CREATE TABLE mid_validation_queries (
        check_type		VARCHAR2(100) NOT NULL,
	query			VARCHAR2(2000) NOT NULL,
	-- For formatting reasons cannot be > 54
	check_name		VARCHAR2(54) NOT NULL,
	make_checklist		VARCHAR2(1) NOT NULL,
                CHECK (make_checklist in ('Y','N')),
        description		VARCHAR2(4000) NOT NULL,
	adjustment		NUMBER NOT NULL,
	adjustment_dsc		VARCHAR2(1000),
        auto_fix		VARCHAR2(4000)
)
PCTFREE 10 PCTUSED 80 MONITORING;


-- Molecular action rows.  One row per molecular_action
-- (secondary indexes: source_id, target_id )
DROP TABLE molecular_actions;
CREATE TABLE molecular_actions(
	transaction_id 		NUMBER(12) NOT NULL,
	molecule_id 		NUMBER(12) NOT NULL
		CONSTRAINT molecular_actions_pk PRIMARY KEY,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	molecular_action 	VARCHAR2(30) NOT NULL,
	source_id 		NUMBER(12) NOT NULL,
	target_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	undone 			VARCHAR2(1),
	undone_by 		VARCHAR2(20),
	undone_when 		DATE,
	status			VARCHAR2(1),
	elapsed_time		NUMBER(12) DEFAULT 0,
	work_id			NUMBER(12) DEFAULT 0
)
PCTFREE 15 PCTUSED 75 MONITORING
STORAGE (INITIAL 600M);



-- The rank field in this table has the following semantics
--   rank[0] : 1 if same code, 0 otherwise (& code is part of predicate)
--   rank[1] : 1 if same lui, 2 if same isui, 
--			      3 if same sui (& string is part of predicate).
--   rank[2] : 1 if same termgroup, 0 otherwise (& termgroup part of predicate)
--   rank[3-9] : 10000000 - to_number(replace(cui,'C'))
--   rank[10-19] : lpad(to_char(atom_id),10,'0');
--
-- This table tracks safe-replacement facts.  MEME_SOURCE_PROCESSING
-- populates it and MEME_OPERATIONS.assign_cuis utilizes it.
-- (secondary indexes: new_atom_id, old_atom_id, source )
DROP TABLE mom_safe_replacement ;
CREATE TABLE mom_safe_replacement (
	old_atom_id		NUMBER(12) NOT NULL,
	new_atom_id		NUMBER(12) NOT NULL,
	last_release_cui	VARCHAR2(10),
 	rank			VARCHAR2(50) NOT NULL,
	source			VARCHAR2(20) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 150M);

-- This table tracks termgroups that should be excluded from matching.
-- For the most part they are abbreviations/suppressible termgroups
DROP TABLE mom_exclude_list;
CREATE TABLE mom_exclude_list(
	termgroup		VARCHAR2(40) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- mergefacts.src are loaded into this table and mapped before being
-- loaded into mom_merge_facts and run by the merge engine.
DROP TABLE mom_precomputed_facts ;
CREATE TABLE mom_precomputed_facts (
	status			VARCHAR2(1),
	atom_id_1		NUMBER(12) NOT NULL,
	atom_id_2		NUMBER(12) NOT NULL,
	sg_id_1			VARCHAR2(50),
	sg_type_1		VARCHAR2(50),
	sg_qualifier_1		VARCHAR2(50),
	sg_id_2			VARCHAR2(50),
	sg_type_2		VARCHAR2(50),
	sg_qualifier_2		VARCHAR2(50),
	merge_level		VARCHAR2(3),
	source			VARCHAR2(20),
	integrity_vector	VARCHAR2(1500),
	make_demotion		VARCHAR2(1),
	change_status		VARCHAR2(1),
	merge_set		VARCHAR2(30) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 50M);

-- This is where candidate facts are generated.  From here they
-- are loaded into mom_merge_facts
DROP TABLE mom_candidate_facts;
CREATE TABLE mom_candidate_facts(
	ATOM_ID_1		NUMBER(12) NOT NULL,
 	ATOM_ID_2               NUMBER(12) NOT NULL,
 	CODE1                   VARCHAR2(50),
 	CODE2                   VARCHAR2(50),
 	TERMGROUP1              VARCHAR2(40),
 	TERMGROUP2              VARCHAR2(40),
 	SOURCE1                 VARCHAR2(20),
 	SOURCE2                 VARCHAR2(20),
 	MERGE_LEVEL             VARCHAR2(3),
 	STATUS                  VARCHAR2(1),
 	LUI_1             	VARCHAR2(10),
 	LUI_2			VARCHAR2(10),
 	ISUI_1             	VARCHAR2(10),
 	ISUI_2			VARCHAR2(10),
 	SUI_1             	VARCHAR2(10),
 	SUI_2			VARCHAR2(10),
	MERGE_SET		VARCHAR2(30) NOT NULL,
	SOURCE			VARCHAR2(20) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 30M);

-- This table tracks all merge facts that have been processed
-- The fields indicate what the result of processing was
-- and what (if any) integrity violations occurred during processing.
-- (secondary index: (source,merge_set) )
DROP TABLE mom_facts_processed;
CREATE TABLE mom_facts_processed(
	merge_fact_id		NUMBER(12) NOT NULL,
	atom_id_1		NUMBER(12) NOT NULL,
	merge_level		VARCHAR2(3),
	atom_id_2		NUMBER(12) NOT NULL,
	source			VARCHAR2(20),
	integrity_vector	VARCHAR2(1500),
	make_demotion		VARCHAR2(1),
	change_status		VARCHAR2(1),
	authority		VARCHAR2(50),
	merge_set		VARCHAR2(30) NOT NULL,
	violations_vector	VARCHAR2(1500),
	status			VARCHAR2(1),
	merge_order		NUMBER(12),
	molecule_id		NUMBER(12),
	work_id			NUMBER(12)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 250M);

-- This is where merge_facts are held during processing
-- The merge-engine loops through this table processing facts.
-- (secondary indexes: merge_fact_id, atom_id_1, atom_id_2 )
DROP TABLE mom_merge_facts;
CREATE TABLE mom_merge_facts(
	merge_fact_id		NUMBER(12) NOT NULL,
	atom_id_1		NUMBER(12) NOT NULL,
	merge_level		VARCHAR2(3),
	atom_id_2		NUMBER(12) NOT NULL,
	source			VARCHAR2(20),
	integrity_vector	VARCHAR2(1500),
	make_demotion		VARCHAR2(1),
	change_status		VARCHAR2(1),
	authority		VARCHAR2(50),
	merge_set		VARCHAR2(30) NOT NULL,
	violations_vector	VARCHAR2(1500),
	status			VARCHAR2(1),
	merge_order		NUMBER(12),
	molecule_id		NUMBER(12),
	work_id			NUMBER(12)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M);

-- This table tracks termgroups that should be excluded from norm
-- comparisons.  Typically they are chemical termgroups.
DROP TABLE mom_norm_exclude_list;
CREATE TABLE mom_norm_exclude_list(
	termgroup		VARCHAR2(40) NOT NULL,
	code_prefix		VARCHAR2(1)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table contains STYs not allowed with NH flags.
DROP TABLE nhsty;
CREATE TABLE nhsty(
	ui			VARCHAR2(4) NOT NULL,
	sty			VARCHAR2(50) NOT NULL,
	stn			VARCHAR2(15) NOT NULL,
	def			VARCHAR2(1900) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- Index of norm strings - atom_ids
-- (secondary indexes: normstr_id, normstr)
DROP TABLE normstr;
CREATE TABLE normstr(
	normstr_id 		NUMBER(12)
/* CONSTRAINT normstr_pk PRIMARY KEY */,
	normstr 		VARCHAR2(3000)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 200M);

-- Index of norm words - atom_ids
-- (secondary indexes: normwrd_id, normwrd)
DROP  TABLE normwrd;
CREATE  TABLE normwrd(
	normwrd_id 		NUMBER(12) NOT NULL,
	normwrd 		VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 350M);

-- List of core tables to reindex daily
-- Should be a subset of meme_tables
--DROP TABLE reindex_tables;
--CREATE TABLE reindex_tables (
--	table_name	VARCHAR(50) NOT NULL)
--PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- Relationships
-- (secondary indexes: atom_id_1, atom_id_2, concept_id_1, concept_id_2 )
DROP TABLE relationships;
CREATE TABLE relationships (
	relationship_id 	NUMBER(12) 
		  CONSTRAINT relationships_pk PRIMARY KEY,
	version_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	relationship_level 	VARCHAR2(1) NOT NULL,
	atom_id_1 		NUMBER(12) DEFAULT 0 NOT NULL,
	relationship_name 	VARCHAR2(10) NOT NULL,
	relationship_attribute 	VARCHAR2(100),
	atom_id_2 		NUMBER(12) DEFAULT 0 NOT NULL,
	source		 	VARCHAR2(20) NOT NULL,
	generated_status	VARCHAR2(1) NOT NULL,
	dead 			VARCHAR2(1) NOT NULL,
	status 			VARCHAR2(1) NOT NULL,
	authority 		VARCHAR2(50) NOT NULL,
	timestamp 		DATE NOT NULL,
	insertion_date 		DATE,
	concept_id_1 		NUMBER(12) DEFAULT 0 NOT NULL,
	concept_id_2 		NUMBER(12) DEFAULT 0 NOT NULL,
	released 		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	source_rank 		NUMBER(12) DEFAULT 0 NOT NULL,
	preferred_level 	VARCHAR2(1),
	last_molecule_id 	NUMBER(12) DEFAULT 0,
	last_atomic_action_id 	NUMBER(12) DEFAULT 0,
	rank 			NUMBER DEFAULT 0 NOT NULL,
	source_of_label		VARCHAR2(20) NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	rui			VARCHAR2(12),
	source_rui		VARCHAR2(50),
	relationship_group	VARCHAR2(10),
	sg_id_1			VARCHAR2(50),
	sg_type_1		VARCHAR2(50),
	sg_qualifier_1		VARCHAR2(50),
	sg_meme_data_type_1	VARCHAR2(10),
	sg_meme_id_1		NUMBER(12) DEFAULT 0,
	sg_id_2			VARCHAR2(50),
	sg_type_2		VARCHAR2(50),
	sg_qualifier_2		VARCHAR2(50),
	sg_meme_data_type_2	VARCHAR2(10),
	sg_meme_id_2		NUMBER(12) DEFAULT 0
)
PCTFREE 15 PCTUSED 75 MONITORING
STORAGE (INITIAL 500M);

-- Tracks relationship unique identifiers over time
-- Secondary indexes on sg_id_1, sg_id_2
DROP TABLE relationships_ui;
CREATE TABLE relationships_ui (
	rui			VARCHAR2(10) NOT NULL,
	root_source		VARCHAR2(20) NOT NULL,
	relationship_level	VARCHAR2(1) NOT NULL,
	relationship_name	VARCHAR2(10) NOT NULL,
	relationship_attribute	VARCHAR2(100),
	sg_id_1			VARCHAR2(50) NOT NULL,
	sg_type_1		VARCHAR2(50) NOT NULL,
	sg_qualifier_1		VARCHAR2(50),
	sg_id_2			VARCHAR2(50) NOT NULL,
	sg_type_2		VARCHAR2(50) NOT NULL,
	sg_qualifier_2		VARCHAR2(50),
	source_rui		VARCHAR2(50) 
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 200M);


-- This table tracks (and ranks) valid released values.
DROP TABLE released_rank;
CREATE TABLE released_rank(
	released		VARCHAR2(1) NOT NULL,
	rank			NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks valid semantic types and includes information
-- about whether/how they should be treated as chemical types
DROP TABLE semantic_types;
CREATE TABLE semantic_types(
	semantic_type		VARCHAR2(100) NOT NULL,
	is_chem			VARCHAR2(1), 
			/* NOT NULL - because of ALL CHEMICAL STYs row */
	chem_type		VARCHAR2(1),
	editing_chem            VARCHAR2(1)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;



-- This table contains data used by the SIMS
-- http://meow.nlm.nih.gov/cgi-lti-oracle/SIMS.cgi
DROP TABLE sims_info;
CREATE TABLE sims_info (
	source      		VARCHAR2(20) NOT NULL
	    CONSTRAINT sims_info_pk PRIMARY KEY,
	date_created		DATE,
	meta_year               VARCHAR2(20),
	init_rcpt_date           DATE,
	clean_rcpt_date         DATE,
	test_insert_date        DATE,
	real_insert_date        DATE,
	source_contact          VARCHAR2(4000),
	inverter_contact        VARCHAR2(100),
	nlm_path                VARCHAR2(200),
	apelon_path             VARCHAR2(200),
	inversion_script        VARCHAR2(100),
	inverter_notes_file     VARCHAR2(100),
	conserve_file           VARCHAR2(100),
	sab_list                VARCHAR2(100),
	meow_display_name       VARCHAR2(4000),
	source_desc             VARCHAR2(4000),
	status                  VARCHAR2(10),
	worklist_sortkey_loc    VARCHAR2(100),
	termgroup_list          VARCHAR2(4000),
	attribute_list          VARCHAR2(4000),
	inversion_notes         VARCHAR2(4000),
	release_url_list        VARCHAR2(4000),
	internal_url_list       VARCHAR2(4000),
	notes                   VARCHAR2(4000),
	inv_recipe_loc          VARCHAR2(100),
	suppress_edit_rec	VARCHAR2(1),
	source_official_name	VARCHAR2(4000),
	source_short_name	VARCHAR2(4000),
	valid_start_date	DATE,
	valid_end_date		DATE,
	insert_meta_version	VARCHAR2(20),
	remove_meta_version	VARCHAR2(20),
	nlm_contact		VARCHAR2(100), 
	acquisition_contact	VARCHAR2(4000),
	content_contact		VARCHAR2(4000),
	license_contact		VARCHAR2(4000),
 	context_type		VARCHAR2(100),
 	language		VARCHAR2(10),
	character_set		VARCHAR2(20),
	citation		VARCHAR2(4000),
	latest_available	VARCHAR2(100),
	last_contacted		DATE,
	license_info		VARCHAR2(4000),
	versioned_cui		VARCHAR2(10),
	root_cui		VARCHAR2(10),
	attribute_name_list	VARCHAR2(4000),
	term_type_list		VARCHAR2(1000),
	cui_frequency		NUMBER(12),
	term_frequency		NUMBER(12),
	test_insertion_start	DATE,
	test_insertion_end	DATE,
	real_insertion_start	DATE,
	real_insertion_end	DATE,
	editing_start		DATE,
	editing_end		DATE,
	rel_directionality_flag	VARCHAR2(1),
	whats_new		VARCHAR2(4000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table keeps the results of the integrity_snapshot
-- It attempts to provide molecule_ids for offending merges
DROP TABLE snapshot_results ;
CREATE TABLE snapshot_results (
	concept_id		NUMBER(12) DEFAULT 0 NOT NULL,
	ic_name			VARCHAR2(10),
	molecule_id		NUMBER(12) DEFAULT 0 NOT NULL,
	authority		VARCHAR2(50)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- Attributes from attributes.src are loaded into this table and 
-- mapped.  MEME_SOURCE_PROCESSING, MEME_BATCH_ACTIONS, use it.
DROP TABLE source_attributes;
CREATE TABLE source_attributes(
	switch			VARCHAR2(1),
	source_attribute_id	NUMBER(12) DEFAULT 0 NOT NULL,
	attribute_id		NUMBER(12) DEFAULT 0 NOT NULL,
	atom_id			NUMBER(12) DEFAULT 0 NOT NULL,
	concept_id		NUMBER(12) DEFAULT 0 NOT NULL,
	sg_id			VARCHAR2(50),
	sg_type			VARCHAR2(50),
	sg_qualifier		VARCHAR2(50),
	sg_meme_data_type	VARCHAR2(10),
	sg_meme_id		NUMBER(12) DEFAULT 0,
	attribute_level		VARCHAR2(1) NOT NULL,
	attribute_name		VARCHAR2(100) NOT NULL,
	attribute_value		VARCHAR2(200),
	generated_status	VARCHAR2(1) NOT NULL,	 
	source			VARCHAR2(20) NOT NULL,
	status			VARCHAR2(1) NOT NULL,
	released		VARCHAR2(1) NOT NULL,
	tobereleased		VARCHAR2(1) NOT NULL,
	source_rank		NUMBER(12) DEFAULT 0,
	suppressible		VARCHAR2(10) NOT NULL,
	atui			VARCHAR2(12),
	source_atui		VARCHAR2(50),
	hashcode		VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 200M);

-- Atoms from classes_atoms.src are loaded into this table.
-- MEME_SOURCE_PROCESSING, MEME_BATCH_ACTIONS use it.
DROP TABLE source_classes_atoms;
CREATE TABLE source_classes_atoms(
	switch			VARCHAR2(1),
	source_atom_id		NUMBER(12) DEFAULT 0 NOT NULL,
	source			VARCHAR2(20) NOT NULL,
	termgroup		VARCHAR2(40) NOT NULL,
	tty			VARCHAR2(20),
	termgroup_rank		NUMBER(12) DEFAULT 0,
	code			VARCHAR2(30),
	sui			VARCHAR2(10),
	lui			VARCHAR2(10),
	isui                    VARCHAR2(10),
	aui                     VARCHAR2(10),
	generated_status	VARCHAR2(1) NOT NULL,
	status			VARCHAR2(1) NOT NULL,
	released		VARCHAR2(1) NOT NULL,
	tobereleased		VARCHAR2(1) NOT NULL,
	atom_name		VARCHAR2(3000),
	atom_id			NUMBER(12) DEFAULT 0 NOT NULL,
	concept_id 		NUMBER(12) DEFAULT 0 NOT NULL,
	sort_key		NUMBER(12) DEFAULT 0,
	pref_rank		NUMBER DEFAULT 0,
	last_release_cui	VARCHAR2(10),
	last_release_rank       NUMBER(12) DEFAULT 0,
	suppressible            VARCHAR2(10) NOT NULL,
	last_assigned_cui       VARCHAR2(10),
	source_aui		VARCHAR2(50),
	source_cui		VARCHAR2(50),
	source_dui		VARCHAR2(50),
	language		VARCHAR2(10),
	order_id		VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 80M);

-- This table is a staging area for the processing
-- of the coc_headings portion of the medline update files.
--
-- Index on citation_set_id
DROP TABLE source_coc_headings;
CREATE TABLE source_coc_headings (
 	citation_set_id	        NUMBER(12) NOT NULL,
 	publication_date	DATE NOT NULL,
 	heading_id		NUMBER(12) NOT NULL,
 	major_topic		VARCHAR2(1) NOT NULL,
 	subheading_set_id       NUMBER(10),
 	source			VARCHAR2(20) NOT NULL,
 	coc_type                VARCHAR2(10)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M);

-- This table is a staging area for the processing
-- of the coc_headings portion of the medline update files.
--
-- Index on citation_set_id
DROP TABLE source_coc_headings_todelete;
CREATE TABLE source_coc_headings_todelete (
 	citation_set_id	        NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table is a staging area for the processing
-- of the coc_headings portion of the medline update files.
--
-- Index on citation_set_id
DROP TABLE source_coc_subheadings;
CREATE TABLE source_coc_subheadings (
	citation_set_id		NUMBER(12) NOT NULL,
	subheading_set_id	NUMBER(10) NOT NULL,
	subheading_qa		CHAR(2) NOT NULL,
	subheading_major_topic	VARCHAR(1) NOT NULL,
	CHECK (subheading_major_topic in ('Y','N'))
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M);

-- Concepts are loaded into this table (from source_classes_atoms).
-- MEME_SOURCE_PROCESSING, MEME_BATCH_ACTIONS use it.
-- Currently there is no concept_status.src file and so the
-- source_concept_id field is NOT being used.  We keep it around "in case".
DROP TABLE source_concept_status;
CREATE TABLE source_concept_status(
	switch			VARCHAR2(1),
	source_concept_id	NUMBER(12) DEFAULT 0,
	cui			VARCHAR2(10),
	source			VARCHAR2(20) NOT NULL,
	status			VARCHAR2(1) NOT NULL,
	released		VARCHAR2(1) NOT NULL,
	tobereleased		VARCHAR2(1) NOT NULL,
	concept_id		NUMBER(12) DEFAULT 0 NOT NULL,
	preferred_atom_id	NUMBER(12) DEFAULT 0 NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 30M);

-- This is where context_relationships are loaded into from the *.raw[23]
-- files.  MEME_SOURCE_PROCESSING, MEME_BATCH_ACTIONS use this table.
DROP TABLE source_context_relationships;
CREATE TABLE source_context_relationships(
	switch			VARCHAR2(2),
	source_rel_id		NUMBER(12) DEFAULT 0 NOT NULL,
	relationship_id		NUMBER(12) DEFAULT 0 NOT NULL,
	atom_id_1		NUMBER(12) DEFAULT 0 NOT NULL,
	atom_id_2		NUMBER(12) DEFAULT 0 NOT NULL,
	concept_id_1		NUMBER(12) DEFAULT 0 NOT NULL,
	concept_id_2		NUMBER(12) DEFAULT 0 NOT NULL,
	sg_id_1			VARCHAR2(50),
	sg_type_1		VARCHAR2(50),
	sg_qualifier_1		VARCHAR2(50),
	sg_meme_data_type_1	VARCHAR2(10),
	sg_meme_id_1		NUMBER(12) DEFAULT 0,
	sg_id_2			VARCHAR2(50),
	sg_type_2		VARCHAR2(50),
	sg_qualifier_2		VARCHAR2(50),
	sg_meme_data_type_2	VARCHAR2(10),
	sg_meme_id_2		NUMBER(12) DEFAULT 0,
	source			VARCHAR2(20) NOT NULL,
	source_of_label		VARCHAR2(20) NOT NULL,
	relationship_level	VARCHAR2(1) NOT NULL,
	relationship_name	VARCHAR2(10) NOT NULL,
	relationship_attribute	VARCHAR2(100),
	generated_status	VARCHAR2(1) NOT NULL,
	status			VARCHAR2(1) NOT NULL,
	released		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	source_rank		NUMBER(12) DEFAULT 0,
	suppressible		VARCHAR2(10) NOT NULL,
	hierarchical_code	VARCHAR2(1000),
	parent_treenum		VARCHAR2(1000),
	release_mode		VARCHAR2(10),
	rui			VARCHAR2(12),
	source_rui		VARCHAR2(50),
	relationship_group	VARCHAR2(10)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 50M);

-- This table keeps a map of source ids to MEME ids.
-- It is required that source atom ids are unique.
-- (secondary indexes: source_row_id, loca_row_id )
DROP TABLE source_id_map;
CREATE TABLE source_id_map(
	local_row_id		NUMBER(12) NOT NULL,
	table_name		VARCHAR2(2) NOT NULL,
	origin			VARCHAR2(10) NOT NULL,
	source			VARCHAR2(20) NOT NULL,
	source_row_id		NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 400M);


-- This table tracks (and ranks) valid source values.
-- The normalized source field is to eliminate the need for
-- the meme2 table source_release_map (which maps things like
-- SNMI98XREF -> SNMI98).  The stripped_source filed provides
-- a key into the source_version table.  (SNMI98 -> SNMI)
-- Since stripped_source cannot be algorithmically computed with
-- perfect accuracy, it should be included in the sources.src file.
-- stripped source does not need to be not null
--
-- The needs of this table have been extended by the
-- addition of MRSAB as a release file and the need to
-- track additional info associated with a source.
--
-- Note, MRSAB will have additional information not derived 
-- from this file but computed at release time (such as counts for
-- each source, and CUI).  Also, some of these fields may not apper
-- in MRSAB
--
DROP TABLE source_rank;
CREATE TABLE source_rank(
/*
	source			VARCHAR2(20) 
				  CONSTRAINT source_rank_pk PRIMARY KEY,
	rank			NUMBER(12) DEFAULT 0 NOT NULL,
	restriction_level	NUMBER(12) DEFAULT 0 NOT NULL,
	normalized_source	VARCHAR2(20) NOT NULL,
	stripped_source		VARCHAR2(20),
 	official_name           VARCHAR2(3000),
	source_family		VARCHAR2(20),
	version			VARCHAR2(20),
 	valid_start_date        DATE,
 	valid_end_date          DATE,
 	insert_meta_version     VARCHAR2(20),
 	remove_meta_version     VARCHAR2(20),
 	nlm_contact             VARCHAR2(100),
 	inverter_contact        VARCHAR2(100),
 	acquisition_contact     VARCHAR2(1000),
 	content_contact         VARCHAR2(1000),
 	license_contact         VARCHAR2(1000),
 	release_url_list         VARCHAR2(1000),
 	context_type            VARCHAR2(100),
 	language                VARCHAR2(10),
	notes			VARCHAR2(1000)
*/
	source			VARCHAR2(20) 
				  CONSTRAINT source_rank_pk PRIMARY KEY,
	rank			NUMBER(12) DEFAULT 0 NOT NULL,
	restriction_level	NUMBER(12) DEFAULT 0 NOT NULL,
	normalized_source	VARCHAR2(20) NOT NULL,
	stripped_source		VARCHAR2(20),
	source_family		VARCHAR2(20),
	version			VARCHAR2(20),
	notes			VARCHAR2(1000)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- Relationships from the relationships.src file are loaded into
-- this table and mapped.  MEME_SOURCE_PROCESSING, MEME_BATCH_ACTIONS
-- use this table.
DROP TABLE source_relationships;
CREATE TABLE source_relationships(
	switch			VARCHAR2(2),
	source_rel_id	NUMBER(12) DEFAULT 0 NOT NULL,
	relationship_id		NUMBER(12) DEFAULT 0 NOT NULL,
	atom_id_1		NUMBER(12) DEFAULT 0 NOT NULL,
	atom_id_2		NUMBER(12) DEFAULT 0 NOT NULL,
	concept_id_1		NUMBER(12) DEFAULT 0 NOT NULL,
	concept_id_2		NUMBER(12) DEFAULT 0 NOT NULL,
	sg_id_1			VARCHAR2(50),
	sg_type_1		VARCHAR2(50),
	sg_qualifier_1		VARCHAR2(50),
	sg_meme_data_type_1	VARCHAR2(10),
	sg_meme_id_1		NUMBER(12) DEFAULT 0,
	sg_id_2			VARCHAR2(50),
	sg_type_2		VARCHAR2(50),
	sg_qualifier_2		VARCHAR2(50),
	sg_meme_data_type_2	VARCHAR2(10),
	sg_meme_id_2		NUMBER(12) DEFAULT 0,
	source			VARCHAR2(20) NOT NULL,
	source_of_label		VARCHAR2(20) NOT NULL,
	relationship_level	VARCHAR2(1) NOT NULL,
	relationship_name	VARCHAR2(10) NOT NULL,
	relationship_attribute	VARCHAR2(100),
	generated_status	VARCHAR2(1) NOT NULL,
	status			VARCHAR2(1) NOT NULL,
	released		VARCHAR2(1) NOT NULL,
	tobereleased 		VARCHAR2(1) NOT NULL,
	source_rank		NUMBER(12) DEFAULT 0,
	suppressible		VARCHAR2(10) NOT NULL,
	rui			VARCHAR2(12),
	source_rui		VARCHAR2(50),
	relationship_group	VARCHAR2(10)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M);

--
-- This table tracks identifiers from old versions of a
-- source (during the update insertion) that represent
-- changed or deleted content.  The table is used
-- during a "partial update insertion" to identify
-- data that should be unreleasable.
--
-- Data specified with an sg_type of SOURCE_RUI, ROOT_SOURCE_RUI 
--   are treated as relationships
-- Data specified with any other sg_type but without a ATN values
--   are treated as atoms
-- Data specified with ATN values are treated as attributes
--
DROP TABLE source_replacement;
CREATE TABLE source_replacement (
	atom_id			NUMBER(12) DEFAULT 0 NOT NULL,
	sg_id			VARCHAR2(50),
	sg_type			VARCHAR2(50),
	sg_qualifier		VARCHAR2(50),
	sg_meme_data_type	VARCHAR2(10),
	sg_meme_id		NUMBER(12) DEFAULT 0,
	attribute_name		VARCHAR2(100),
	hashcode		VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 50M);

-- This table tracks long attribute values for new attribuets.
-- There is code to generate a stringtab.src file from attributes.src
-- and that is used to load this table.
DROP TABLE source_stringtab;
CREATE TABLE source_stringtab(
	string_id		NUMBER(12) NOT NULL,
	row_sequence		NUMBER(12) NOT NULL,
	text_total		NUMBER(12) NOT NULL,
	text_value		VARCHAR2(3000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 50M);

-- This table tracks the string UIs for new atoms.
-- It is initially loaded from strings.src which is derived from 
-- classes_atoms.src.  MEME_SOURCE_PROCESSING.assign_string_uis 
-- manages this table.
-- (secondary indexes:  string_pre, lowercase_string_pre, norm_string_pre )
DROP TABLE source_string_ui;
CREATE TABLE source_string_ui(
	atom_id			NUMBER(12) DEFAULT 0,
	lui                     VARCHAR2(10),
	sui                     VARCHAR2(10),
	string_pre              VARCHAR2(30),
	norm_string_pre         VARCHAR2(30),
	language                VARCHAR2(20),
	base_string             VARCHAR2(1),
	string                  VARCHAR2(3000),
	norm_string             VARCHAR2(3000),
	isui                    VARCHAR2(10),
	lowercase_string_pre    VARCHAR2(30)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 80M);

-- This table is loaded by the RxRunner, either from sources.src or it is
-- inferred from termgroups.src
DROP TABLE source_source_rank;
CREATE TABLE source_source_rank(
	high_source		VARCHAR2(20) NOT NULL,
	low_source		VARCHAR2(20) NOT NULL,
	restriction_level	NUMBER(12) NOT NULL,
	normalized_source	VARCHAR2(20),
	stripped_source		VARCHAR2(20),
	source_official_name    VARCHAR2(3000),
	source_family		VARCHAR2(20),
	version			VARCHAR2(20),
 	valid_start_date        DATE,
 	valid_end_date          DATE,
 	insert_meta_version     VARCHAR2(20),
 	remove_meta_version     VARCHAR2(20),
 	nlm_contact             VARCHAR2(100),
 	inverter_contact        VARCHAR2(100),
 	acquisition_contact     VARCHAR2(1000),
 	content_contact         VARCHAR2(1000),
 	license_contact         VARCHAR2(1000),
 	release_url_list        VARCHAR2(1000),
 	context_type            VARCHAR2(100),
 	language                VARCHAR2(10),
     	citation 		VARCHAR2(4000),
     	license_info 		VARCHAR2(4000),
     	character_set 		VARCHAR2(50),
	rel_directionality_flag	VARCHAR2(1),
	rank			NUMBER(12)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table is loaded by the RxRunner each time a source insertion is done
-- It is pretty much a copy of the termgroups.src file
DROP TABLE source_termgroup_rank;
CREATE TABLE source_termgroup_rank(
	high_termgroup		VARCHAR2(40) NOT NULL,
	low_termgroup		VARCHAR2(40) NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	exclude			VARCHAR2(1) NOT NULL,
	norm_exclude		VARCHAR2(1) NOT NULL,
	tty			VARCHAR2(20),
	rank			NUMBER(12)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table tracks current/previous versions of update sources.
--
-- There are a number of integrity rules for this table
-- 1. Every source value should have a corresponding stripped_source
--     value in source_rank (where the source_rank.source=normalized_source)
-- 2. Every stripped source in source_rank 
--    (where the source_rank.source=normalized_source) should appear
--    in source_version.source
-- 3. Every current_name should appear as a source_rank.normalized_source 
--    value.
-- 4. Every previous_name (not null) should appear as a 
--    source_rank.normalized_source value
--
DROP TABLE source_version;
CREATE TABLE source_version(
	source			VARCHAR2(20),
	current_name		VARCHAR2(20),
	previous_name		VARCHAR2(20)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table stores safe_replacement predicates
-- Complicated predicates with multiple steps are ordered by
-- the row_sequence field.  Reconstructing the predicate would
-- involve looking through rows for a source in row_sequence order
DROP TABLE sr_predicate;
CREATE TABLE sr_predicate(
	source			VARCHAR2(20) NOT NULL,
	row_sequence		NUMBER(12) DEFAULT 0,
	current_name		VARCHAR2(20) NOT NULL,
	previous_name		VARCHAR2(20) NOT NULL,
	string_match		VARCHAR2(10) NOT NULL,
	code_match		VARCHAR2(10) NOT NULL,
	tty_match		VARCHAR2(10) NOT NULL,
	source_cui_match	VARCHAR2(10) NOT NULL,
	replaced_termgroups	VARCHAR2(2000),
	replacement_termgroups  VARCHAR2(2000),
	replaced_sources	VARCHAR2(2000),
	replacement_sources     VARCHAR2(2000)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table is used by MEME_INTEGRITY.styisa
DROP TABLE srstre2;
CREATE TABLE srstre2(
	sty1			VARCHAR2(50),
	rel			VARCHAR2(32),
	sty2			VARCHAR2(50),
	empty			VARCHAR2(1)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks valid semantic types and relas
-- It is a copy of the SRDEF release file.
DROP TABLE srdef;
CREATE TABLE srdef(
	rt			VARCHAR2(3) NOT NULL,
	ui			VARCHAR2(4) NOT NULL,
 	sty_rl			VARCHAR2(50) NOT NULL,
	stn_rtn			VARCHAR2(15),
	def			VARCHAR2(1900),
	ex 			VARCHAR2(700),
	un			VARCHAR2(700),
	nh			VARCHAR2(1),
	abr			VARCHAR2(10),
 	rin			VARCHAR2(50)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table tracks every string that has been in the Metathesaurus
-- (secondary indexes: lui, isui, norm_string_pre, string_pre, 
--  lowercase_string_pre )
DROP TABLE string_ui;
CREATE TABLE string_ui(
	lui 			VARCHAR2(10) NOT NULL,
	sui 			VARCHAR2(10) 
				   CONSTRAINT string_ui_pk PRIMARY KEY,
	string_pre 		VARCHAR2(30),
	norm_string_pre 	VARCHAR2(30),
	language 		VARCHAR2(20) NOT NULL,
	base_string 		VARCHAR2(1),
	string 			VARCHAR2(3000),
	norm_string 		VARCHAR2(3000),
	isui			VARCHAR2(10) NOT NULL,
	lowercase_string_pre	VARCHAR2(30)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 350M);

-- Long attribute values are broken up and stored in this table
-- The row_sequence provides an ordering to the parts
-- The text_total is the length of all of the whole attribute, not just
-- that one row
DROP TABLE stringtab;
CREATE TABLE stringtab(
	string_id 		NUMBER(12) NOT NULL,
	row_sequence 		NUMBER(12) NOT NULL, 
	text_total 		NUMBER(12) NOT NULL,
	text_value 		VARCHAR2(3000),
	CONSTRAINT stringtab_pk PRIMARY KEY (string_id,row_sequence)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 900M);

-- This table tracks (and ranks) valid suppressible values
DROP TABLE suppressible_rank;
CREATE TABLE suppressible_rank(
	suppressible		VARCHAR2(10) NOT NULL,
	rank			NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks (and ranks) valid termgroups
DROP TABLE termgroup_rank;
CREATE TABLE termgroup_rank(
	termgroup		VARCHAR2(40)
				   CONSTRAINT termgroup_rank_pk PRIMARY KEY,
	rank			NUMBER(12)  DEFAULT 0 NOT NULL,
	release_rank		NUMBER(12)  DEFAULT 0 NOT NULL,
	notes			VARCHAR2(100),
	suppressible		VARCHAR2(10) NOT NULL,
	tty			VARCHAR2(20)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks the results of running test suites.
--
DROP TABLE test_suite_statistics;
CREATE TABLE test_suite_statistics (
	set_name		VARCHAR2(100) NOT NULL,
	suite_name		VARCHAR2(100) NOT NULL,
	test_name		VARCHAR2(100) NOT NULL,
	value			VARCHAR2(4000) NOT NULL,
	timestamp		DATE NOT NULL,
	elapsed_time 		NUMBER(12),
	authority 		VARCHAR2(50) NOT NULL,
	error			VARCHAR2(4000)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;


-- This table acts like a switch
-- for the various status things, like dba_cutoff and ic_system_status
-- 'dba_cutoff','y'
-- 'ic_system','ON'
DROP TABLE system_status;
CREATE TABLE system_status(
	system			VARCHAR2(1000),
	status			VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table tracks (and ranks) valid tbr values
DROP TABLE tobereleased_rank;
CREATE TABLE tobereleased_rank(
	tobereleased 		VARCHAR2(1) NOT NULL,
	rank 			NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING CACHE;

-- This table is a word - atom_id index
-- (secondary indexes: atom_id, word )
DROP TABLE word_index;
CREATE TABLE word_index(
	atom_id 		NUMBER(12) NOT NULL,
	word 			VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 350M);


------------------------------------------------------------------
--QA System Tables
------------------------------------------------------------------
-- The following tables were added to support
-- an automated QA system

-- This table holds qa count results from the
-- source tables.  A single QA_id represents a complete
-- set of data to compare against, and so can span
-- multiple sources.
DROP TABLE src_qa_results;
CREATE TABLE src_qa_results(
        qa_id                   NUMBER(12) NOT NULL,
        name                    VARCHAR2(100) NOT NULL,
        value                   VARCHAR2(100),
        qa_count                NUMBER(12) NOT NULL,
        timestamp               DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table holds qa count results from the most
-- recent set of queries run against the core tables
DROP TABLE mid_qa_results;
CREATE TABLE mid_qa_results(
        qa_id                   NUMBER(12) NOT NULL,
        name                    VARCHAR2(100) NOT NULL,
        value                   VARCHAR2(100),
        qa_count                NUMBER(12) NOT NULL,
        timestamp               DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table holds historical results dumped from 
-- mid_qa_results
DROP TABLE mid_qa_history;
CREATE TABLE mid_qa_history(
        qa_id                   NUMBER(12) NOT NULL,
        name                    VARCHAR2(100) NOT NULL,
        value                   VARCHAR2(100),
        qa_count                NUMBER(12) NOT NULL,
        timestamp               DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 40M);

-- This table holds the queries used to generate counts
-- for mid_qa_results
DROP TABLE mid_qa_queries;
CREATE TABLE mid_qa_queries(
        name                    VARCHAR2(100) NOT NULL,
        query                   VARCHAR2(1000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table holds the queries used to generate counts
-- for src_qa_results
DROP TABLE src_qa_queries;
CREATE TABLE src_qa_queries(
        name                    VARCHAR2(100) NOT NULL,
        query                   VARCHAR2(1000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- This table tracks adjustments to src or mid counts
-- used to make the numbers add up.  Typically
-- these are applied to SRC counts with a description
-- of what merited the change
DROP TABLE qa_adjustment;
CREATE TABLE qa_adjustment(
        qa_id                   NUMBER(12) NOT NULL,
        name                    VARCHAR2(100) NOT NULL,
        value                   VARCHAR2(100),
        qa_count                NUMBER(12) NOT NULL,
        timestamp               DATE NOT NULL,
        description             VARCHAR2(1000)
)
PCTFREE 10 PCTUSED 80 MONITORING;

-- this table can be used to track qa differences between
-- any two qa sets with different ids.  It is used to adjust
-- when comparing the most recent MID counts with the past one
DROP TABLE qa_diff_adjustment;
CREATE TABLE qa_diff_adjustment(
        qa_id_1                 NUMBER(12) NOT NULL,
        qa_id_2                 NUMBER(12) NOT NULL,
        name                    VARCHAR2(100) NOT NULL,
        value                   VARCHAR2(100),
        qa_count                NUMBER(12) NOT NULL,
        timestamp               DATE NOT NULL,
        description             VARCHAR2(1000)
)
PCTFREE 10 PCTUSED 80 MONITORING;
 
-- This table keeps track of the results of the most recent
-- QA run.
DROP TABLE qa_diff_results;
CREATE TABLE qa_diff_results(
        name                    VARCHAR2(100) NOT NULL,
        value                   VARCHAR2(100),
        qa_id_1                 NUMBER(12) NOT NULL,
        count_1                 NUMBER(12) NOT NULL,
        qa_id_2                 NUMBER(12) NOT NULL,
        count_2                 NUMBER(12) NOT NULL,
        timestamp               DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING;
