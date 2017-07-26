/*****************************************************************************
*
* File:  $MRD_HOME/etc/sql/mrd_tables.sql
* Author:  Brian Carlsen
*
* Remarks:  This script is used to create the MRD tables
*
* Version Information
*
* Release: 2
* Version: 0.4
* Authority: BAC
*  Date: 01/11/2006
*   02/27/2006 2.0.6 TTN (1-AHNAL) : add code and cascade field to mrd_content_view_members schema
*   02/03/2006 2.0.5 TTN (1-76Y8V) : change code to varchar2(50)
*   01/24/2006 BAC (1-7558C): remove classes_feedback references
*   01/11/2006 TK 1-73IUN: attribute_value increase from 100 to 200 in length
*   12/21/2005 2.0.4 BAC (1-718MU): set nls_date_format before creating mrd_coc_headings.
*                           if date format is wrong, table isn't created properly.
*   06/16/2005 2.0.3 (BAC): mrd_coc_headings should be built in MRD
*                           tablespace (not MID) with partitions also
*                           in mrd.
*   05/23/2005 2.0.2 (BAC): release_history tracks previous release and
*                           previous_major_release
*   04/22/2005 2.0.1 (BAC): Release 2
*   04/14/2005 1.3.97 (BAC): mrd_attributes, tmp_Attributes have attribute
*                          value field 200 bytes, mrd_stringtab indexed
*                          by hashcode not string_id, mrd_Attributes does
*  			   not track it (or tmp_attributes)
*   12/07/2004 3.96 (BAC): +{mid,mrd}_validation_results
*   11/29/2004 3.95 (BAC): +action_log,+aui_history, +mrd_aui_history
*                          -authority_rank,groups, -operations_queue
*   11/15/2004 3.94 (BAC): release_history.documentation_{host,uri}
*   07/26/2004 3.93 (BAC): mrd_content_view changes
*   05/25/2004 3.92 (BAC): +mrd_content_view*, +mrd_cui_history
*   04/29/2004 3.91 (BAC): Use tablespace MRD, MRDI
*   02/17/2004 3.9 (BAC): Added qa_result_reasons, qa_comparison_reasons
*   11/26/2003 3.8 (BAC): Added mrd_validation_queries
*   03/21/2003 3.72 (TTN): MONITORING clause added.
*   03/14/2003 3.71 (BAC): mrd_source_rank upgraded to match source_rank/
*                        sims_info
*   08/20/2002 3.7 (BAC): release_history added.
*   08/02/2002 3.6 (BAC): Changed mrd_column_statistics.average_length
*                         to NUMBER(12,4)
*   07/20/2002 3.5 (BAC): Removed mrd_word_index, mrd_normwrd, mrd_normstr
*   05/28/2002 3.4 (BAC): Added language field to mrd_source_rank
*   03/21/2002 3.3 (BAC): Added ic_applications, ic_override, integrity_constr
*                         semantic_types, ic_single,and ic_pair to the list of
*                         tables to load into MRD (used by initialize_mrd.csh).
*   02/25/2002 3.2 (BAC): mrd_file_statistics and mrd_column_statistics
*                         were modified to have nullable description fields
*   02/15/2002 3.1 (BAC): Added mrd_file_statistics, mrd_column_statistics
*   12/07/2001 3.0 (BAC): This version of the script was
*      			  used to load the MRD for 2002AA production
*   10/30/2001 2.6 (BAC): parent_treenum, hierarchical_code => 1000 chars
*   10/22/2001 2.5 (BAC): string fields -> 4000 chars
*   09/05/2001 2.4 (BAC): mrd_coc_headings restructured,
*			  +mrd_source_rank.context_type, is_current
*   7/11/2001 2.3 (BaC): lob tables are CACHE not NOCACHE
*   6/4/2001  2.2 (BAC): mrd_termgroup_rank, mrd_source_rank have regular
*                        indexes instead of primary keys.
*   5/31/2001 2.1 (BAC): Fixes to mrd_coc_headings, mrd_coc_subheadings
*   5/4/2001  2.0 (BAC):
* 	Cleaned up the script, added storage clauses, etc.
*
*   4/16/2001 1.995 (BAC):
*       MID tables were replaced with @@$MEME_HOME/etc/sql/meme_tables.sql
*
*   See old/ tables.sql for older comments.
*
*****************************************************************************/
set autocommit on;

-- Create plan table.
@$ORACLE_HOME/rdbms/admin/utlxplan

/******************** MID tables ********************/

-- We use the MEME_HOME script to build all
-- of the MID tables so that we don't have to
-- maintain them in two places.
--
-- Actually These tables should now be tracked in
-- a separate tablespace and so there should never
-- be a need to create them to initialize an MRD database.
--
-- Click <a href="/MEME/Data/mrd_tables.sql">here</a>
-- to see this schema script.
/**
--@@$MEME_HOME/etc/sql/meme_tables.sql;

    Following is a list of tables that belong in meme_tables.
    The $MRD_HOME/bin/initialize.csh script greps for CREATE
    TABLE statements to determine which of the MEME tables
    should be kept.  This list is for that purpose.
CREATE TABLE ACTION_LOG
CREATE TABLE ACTIVITY_LOG
CREATE TABLE ALLOCATED_UI_RANGES
CREATE TABLE APPLICATION_HELP
CREATE TABLE APPLICATION_VERSIONS
CREATE TABLE ATOMIC_ACTIONS
CREATE TABLE ATOMS_UI
CREATE TABLE ATOM_ORDERING
CREATE TABLE ATOMS
CREATE TABLE ATTRIBUTES
CREATE TABLE ATTRIBUTES_UI
CREATE TABLE AUI_HISTORY
CREATE TABLE CLASSES
CREATE TABLE COC_HEADINGS
CREATE TABLE COC_SUBHEADINGS
CREATE TABLE CODE_MAP
CREATE TABLE CONCEPT_STATUS
CREATE TABLE CONTEXT_RELATIONSHIPS
CREATE TABLE CONTENT_VIEWS
CREATE TABLE CONTENT_VIEW_MEMBERS
CREATE TABLE CUI_HISTORY
CREATE TABLE CUI_MAP
CREATE TABLE DEAD_ATOMS
CREATE TABLE DEAD_ATTRIBUTES
CREATE TABLE DEAD_CLASSES
CREATE TABLE DEAD_CONCEPT_STATUS
CREATE TABLE DEAD_CONTEXT_RELATIONSHIPS
CREATE TABLE DEAD_FOREIGN_ATTRIBUTES
CREATE TABLE DEAD_FOREIGN_CLASSES
CREATE TABLE DEAD_NORMSTR
CREATE TABLE DEAD_NORMWRD
CREATE TABLE DEAD_RELATIONSHIPS
CREATE TABLE DEAD_SIMS_INFO
CREATE TABLE DEAD_STRINGTAB
CREATE TABLE DEAD_WORD_INDEX
CREATE TABLE EDITING_MATRIX
CREATE TABLE EDITORS
CREATE TABLE EDITOR_PREFERENCES
CREATE TABLE FOREIGN_ATTRIBUTES
CREATE TABLE FOREIGN_CLASSES
CREATE TABLE IC_APPLICATIONS
CREATE TABLE IC_OVERRIDE
CREATE TABLE IC_PAIR
CREATE TABLE IC_SINGLE
CREATE TABLE INTEGRITY_CONSTRAINTS
CREATE TABLE INVERSE_REL_ATTRIBUTES
CREATE TABLE INVERSE_RELATIONSHIPS
CREATE TABLE LANGUAGE
CREATE TABLE LEVEL_STATUS_RANK
CREATE TABLE LUI_ASSIGNMENT
CREATE TABLE MAX_TAB
CREATE TABLE MID_VALIDATION_RESULTS
CREATE TABLE MID_VALIDATION_QUERIES
CREATE TABLE MID_QA_HISTORY
CREATE TABLE MID_QA_RESULTS
CREATE TABLE MID_QA_QUERIES
CREATE TABLE MOLECULAR_ACTIONS
CREATE TABLE MOM_SAFE_REPLACEMENT
CREATE TABLE MOM_PRECOMPUTED_FACTS
CREATE TABLE MOM_CANDIDATE_FACTS
CREATE TABLE MOM_FACTS_PROCESSED
CREATE TABLE MOM_MERGE_FACTS
CREATE TABLE NHSTY
CREATE TABLE NORMSTR
CREATE TABLE NORMWRD
CREATE TABLE QA_ADJUSTMENT
CREATE TABLE QA_DIFF_ADJUSTMENT
CREATE TABLE QA_DIFF_RESULTS
CREATE TABLE RELATIONSHIPS
CREATE TABLE INVERSE_RELATIONSHIPS_UI
CREATE TABLE RELATIONSHIPS_UI
CREATE TABLE RELEASED_RANK
CREATE TABLE SEMANTIC_TYPES
CREATE TABLE SRC_QA_RESULTS
CREATE TABLE SRC_OBSOLETE_QA_RESULTS
CREATE TABLE SRC_QA_QUERIES
CREATE TABLE SIMS_INFO
CREATE TABLE SOURCE_ID_MAP
CREATE TABLE SOURCE_RANK
CREATE TABLE SR_PREDICATE
CREATE TABLE SRDEF
CREATE TABLE STRING_UI
CREATE TABLE STRINGTAB
CREATE TABLE SUPPRESSIBLE_RANK
CREATE TABLE TERMGROUP_RANK
CREATE TABLE TOBERELEASED_RANK
CREATE TABLE WORD_INDEX
CREATE TABLE IC_APPLICATIONS
CREATE TABLE SOURCE_ATTRIBUTES
CREATE TABLE SOURCE_CLASSES_ATOMS
CREATE TABLE SOURCE_COC_HEADINGS
CREATE TABLE SOURCE_COC_HEADINGS_TODELETE
CREATE TABLE SOURCE_COC_SUBHEADINGS
CREATE TABLE SOURCE_COC_HEADINGS_TODELETE
CREATE TABLE SOURCE_CONCEPT_STATUS
CREATE TABLE SOURCE_CONTEXT_RELATIONSHIPS
CREATE TABLE SOURCE_RELATIONSHIPS
CREATE TABLE SOURCE_REPLACEMENT
CREATE TABLE SOURCE_STRINGTAB
CREATE TABLE SOURCE_STRING_UI
CREATE TABLE SOURCE_TERMGROUP_RANK
CREATE TABLE SOURCE_SOURCE_RANK
CREATE TABLE SYSTEM_STATUS
CREATE TABLE MEME_ERROR
CREATE TABLE MEME_INDEXES
CREATE TABLE MEME_IND_COLUMNS
CREATE TABLE MEME_PROGRESS
CREATE TABLE MEME_PROPERTIES
CREATE TABLE MEME_SCHEDULE
CREATE TABLE MEME_TABLES
CREATE TABLE MEME_WORK
CREATE TABLE DELETED_CUIS
CREATE TABLE SOURCE_VERSION
**/


/******************** MRD Tables ********************/

-- This table tracks elements that are ready to receive new MRD states
-- Table not used at the moment (4/4/2001).
DROP TABLE available_elements;
CREATE TABLE available_elements (
	row_id		NUMBER(12) NOT NULL,
	table_name	VARCHAR2(10) NOT NULL,
	CONSTRAINT ae_pk PRIMARY KEY (row_id, table_name)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 30M)
TABLESPACE MRD;


-- This table tracks concepts that have elements ready to
-- receive new MRD states.  A concept gets added, if there
-- was an action after which the concept is in a clean state
-- and is removed when an action happens which leaves this
-- concept no longer clean. In particular, concepts deleted
-- in the MID are clean. The get deleted from this table if
-- they get expired in the MRD.
DROP TABLE clean_concepts;
CREATE TABLE clean_concepts (
	concept_id		NUMBER(12) NOT NULL,
        CONSTRAINT clc_pk PRIMARY KEY (concept_id)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 30M)
TABLESPACE MRD;


-- This table keeps track of connections between concepts
-- arising from atomic_actions which change aui, aui_1,
-- aui_2, concept_id, concept_id_1 or concept_id_2
-- See MEME_APROCS.connect_concepts
DROP TABLE connected_concepts;
CREATE TABLE connected_concepts (
	concept_id_1    NUMBER(12) NOT NULL,
	concept_id_2    NUMBER(12) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M)
TABLESPACE MRD;


-- This table is used during the computation of a connected
-- set of concepts by the MRDStateManager
DROP TABLE connected_set;
CREATE TABLE connected_set (
	concept_id      NUMBER(12) NOT NULL,
	cui		VARCHAR2(10),
        CONSTRAINT conc_pk PRIMARY KEY (concept_id)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 20M)
TABLESPACE MRD;

-- This table is used to track multiple sets of connected
-- concepts.  It is used by the MRDStateManager when
-- batch actions are used to change the core tables.
DROP TABLE connected_sets;
CREATE TABLE connected_sets (
	concept_id      NUMBER(12)  	NOT NULL,
	set_id		NUMBER(12)  	DEFAULT 0 NOT NULL,
	status		NUMBER(12) 	DEFAULT 0 NOT NULL,
	CONSTRAINT cs_check CHECK (status in (0,1,2))
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 20M)
TABLESPACE MRD;


--  This table tracks the document type definitions used by the MRD
DROP TABLE dtd_versions;
CREATE TABLE dtd_versions(
       dtd_name                VARCHAR2(100),
       dtd_version                     VARCHAR2(20),
       dtd                     CLOB,
       authority               VARCHAR2(20),
       timestamp               DATE,
       CONSTRAINT dtd_pk PRIMARY KEY (dtd_name, dtd_version)
)
PCTFREE 10 PCTUSED 80 MONITORING
TABLESPACE MRD
lob (dtd) store as
( STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0)
  CHUNK 8K PCTVERSION 10 CACHE TABLESPACE MRD);


-- Once the MRDStateManager processes events from the
-- event_queue, they are moved into this table for
-- permanent storage.  They are removed at that point
-- from event_queue.
DROP TABLE events_processed;
CREATE TABLE events_processed(
        action_id               NUMBER(12) NOT NULL,
        elapsed_time            NUMBER(12) NOT NULL,
        action                  VARCHAR2(500) NOT NULL,
        authority               VARCHAR2(50) NOT NULL,
        timestamp               DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M) TABLESPACE MRD;


-- This table is used by the MRDSyncManager to remember what
-- data changes have been extracted from the MID.  When the
-- application starts, it looks up the data in this table
-- to determine where to start extracting new data changes.
DROP TABLE extraction_history;
CREATE TABLE extraction_history (
 	work_id			NUMBER(12) DEFAULT 0 NOT NULL,
	authority		VARCHAR2(20) NOT NULL,
	timestamp		DATE,
	first_mid_event_id	NUMBER(12) NOT NULL,
	last_mid_event_id	NUMBER(12) NOT NULL,
	row_count		NUMBER(12),
	valid_extraction	VARCHAR2(1) NOT NULL
	  CONSTRAINT eh_ve_check CHECK (valid_extraction in ('Y','N'))
)
PCTFREE 10 PCTUSED 80 MONITORING
TABLESPACE MRD;


-- The MIDSyncManager reads events from this table and
-- applies the data changes to the MID.  The documents
-- in this table will be MRDEvents
DROP TABLE feedback_queue;
CREATE TABLE feedback_queue(
	mrd_event_id		NUMBER(12) NOT NULL
				  CONSTRAINT fq_pk PRIMARY KEY,
	mid_event_id		NUMBER(12) NOT NULL,
	authority		VARCHAR2(20) NOT NULL,
	timestamp		DATE NOT NULL,
	dtd_name		VARCHAR2(20),
	dtd_version		VARCHAR2(20),
	document		CLOB
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) TABLESPACE MRD
lob (document) store as
( STORAGE (INITIAL 50M NEXT 1M PCTINCREASE 0)
  CHUNK 8K PCTVERSION 10 CACHE TABLESPACE MRD);


-- This table contains class names of components which handle
-- various stages in the processing of MRD data and release states
-- Any number of handlers can be assigned to a particular process
-- and type.  Use row_sequence to order the processing of multiple handlers.
-- Set activated to 'N' to deactivate a handler.
--
DROP TABLE registered_handlers;
CREATE TABLE registered_handlers (
	handler_name		VARCHAR2(256) NOT NULL,
	process			VARCHAR2(50) NOT NULL,
	type			VARCHAR2(20),
	row_sequence		NUMBER(12) NOT NULL,
	activated		VARCHAR2(1) NOT NULL,
	dependencies		VARCHAR2(1000),
	authority		VARCHAR2(20) NOT NULL,
	timestamp		DATE,
	CONSTRAINT rh_pk PRIMARY KEY (process, row_sequence)
)
PCTFREE 10 PCTUSED 80 MONITORING
TABLESPACE MRD;

--
-- Used by the ReleaseManager application to mark up
-- a QA report with known reasons for differences that
-- appear in the comparison section of a QA report
--
-- The test_value can be a regular_expression
--
DROP TABLE qa_comparison_reasons;
CREATE TABLE qa_comparison_reasons (
	release_name		VARCHAR2(100) NOT NULL,
	comparison_name		VARCHAR2(100) NOT NULL,
	target_name		VARCHAR2(100) NOT NULL,
  	test_name		VARCHAR2(100) NOT NULL,
  	test_name_operator	VARCHAR2(100),
	test_value		VARCHAR2(4000),
  	test_value_operator	VARCHAR2(100),
	test_count_1		NUMBER(12),
  	test_count_1_operator	VARCHAR2(100),
	test_count_2		NUMBER(12),
  	test_count_2_operator	VARCHAR2(100),
	count_diff		NUMBER(12),
  	test_diff_operator	VARCHAR2(100),
	reason			VARCHAR2(4000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
TABLESPACE MRD;

--
-- Used by the ReleaseManager application to mark up
-- a QA report with known reasons for entries that appear
-- in one QA report (e.g. Gold script) but not in another
-- (e.g. current META).
--
-- The test_value can be a regular_expression
--
DROP TABLE qa_result_reasons;
CREATE TABLE qa_result_reasons (
	release_name		VARCHAR2(100) NOT NULL,
	comparison_name		VARCHAR2(100) NOT NULL,
	target_name		VARCHAR2(100) NOT NULL,
  	test_name		VARCHAR2(100) NOT NULL,
  	test_name_operator	VARCHAR2(100),
	test_value		VARCHAR2(4000),
  	test_value_operator	VARCHAR2(100),
	test_count		NUMBER(12),
  	test_count_operator	VARCHAR2(100),
	reason			VARCHAR2(4000) NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
TABLESPACE MRD;


-- This table contains a history of releases made by the MRD
-- It tracks data akin to the 'release.dat' file created by Suresh
--
DROP TABLE release_history;
CREATE TABLE release_history (
  	release	   		VARCHAR2(10) NOT NULL,
  	previous_release	VARCHAR2(10) NOT NULL,
  	previous_major_release	VARCHAR2(10) NOT NULL,
  	release_date    	DATE NOT NULL,
   	description     	VARCHAR2(4000),
	generator_class		VARCHAR2(256),
	administrator		VARCHAR2(256),
	authority		VARCHAR2(256),
   	build_host            	VARCHAR2(256) NOT NULL,
   	build_uri       	VARCHAR2(256) NOT NULL,
   	release_host           	VARCHAR2(256) NOT NULL,
   	release_uri	        VARCHAR2(256) NOT NULL,
   	documentation_host     	VARCHAR2(256) NOT NULL,
   	documentation_uri      	VARCHAR2(256) NOT NULL,
   	start_date  		DATE,
   	end_date  		DATE,
   	med_start_date  	DATE,
   	mbd_start_date  	DATE,
   	built		   	CHAR(1) DEFAULT 'N' NOT NULL,
   	published	   	CHAR(1) DEFAULT 'N' NOT NULL
) PCTFREE 10 PCTUSED 80 MONITORING
TABLESPACE MRD;

--
-- This table holds results from MRD validation runs
--
DROP TABLE mrd_validation_results;
CREATE TABLE mrd_validation_results AS
SELECT * FROM mid_validation_results WHERE 1=0;

-- This table holds queries used by the
-- $MEME_HOME/bin/validate_mrd.pl script
-- to validate various aspects of the MRD database
-- The decision was used to make the application data-driven
-- so that as problems were discovered, checks could be
-- dynamically added
--
-- For rows with make_checklist='Y', the query must return
-- a concept_id field (other fields are OK).
DROP TABLE mrd_validation_queries;
CREATE TABLE mrd_validation_queries (
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
PCTFREE 10 PCTUSED 80 MONITORING
TABLESPACE MRD;


/******************** MRD Core Tables ********************
*
* The tables in this section track "MRD States".  Each
* table has insertion/expiration dates which are used
* to specify which rows are "alive" at particular moments in
* time.  When a state is expired, it represents a row that
* was either removed from the MID or changed in a way whic
* alters the release.
*
**********************************************************/

-- This table tracks mrd states arising from the attributes table
DROP TABLE mrd_attributes;
CREATE TABLE mrd_attributes(
	attribute_level		VARCHAR2(1) NOT NULL,
	ui			VARCHAR2(10),
	cui			VARCHAR2(10) NOT NULL,
	lui			VARCHAR2(10),
	sui			VARCHAR2(10),
	sg_type 		VARCHAR2(50) NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	attribute_name 		VARCHAR2(50) NOT NULL,
	attribute_value 	VARCHAR2(200),
	code			VARCHAR2(50),
	root_source		VARCHAR2(20) NOT NULL,
	atui 			VARCHAR2(12) NOT NULL,
	source_atui 		VARCHAR2(50),
	hashcode 		VARCHAR2(100),
	insertion_date 		DATE NOT NULL,
	expiration_date 	DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 900M) TABLESPACE MRD;
--LOB (attribute_value) STORE AS
--( STORAGE (INITIAL 1M NEXT 1M PCTINCREASE 0)
--  CHUNK 2K PCTVERSION 10 CACHE );

-- This table AUI history
DROP TABLE mrd_aui_history;
CREATE TABLE mrd_aui_history (
 	aui1			VARCHAR2(10) NOT NULL,
 	cui1			VARCHAR2(10) NOT NULL,
	ver			VARCHAR2(50),
 	relationship_name	VARCHAR2(10),
 	relationship_attribute	VARCHAR2(100),
 	map_reason		VARCHAR2(4000) NOT NULL,
 	aui2			VARCHAR2(10),
 	cui2			VARCHAR2(10),
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) CACHE TABLESPACE MRD;


-- This table tracks mrd states arising from the classes table
DROP TABLE mrd_classes;
CREATE TABLE mrd_classes(
	aui			VARCHAR2(10) NOT NULL,
	cui	 		VARCHAR2(10) NOT NULL,
	lui 			VARCHAR2(10) NOT NULL,
	isui			VARCHAR2(10) NOT NULL,
	sui 			VARCHAR2(10) NOT NULL,
	suppressible		VARCHAR2(10),
	language		VARCHAR2(10),
	root_source 		VARCHAR2(20) NOT NULL,
	tty	 		VARCHAR2(20),
	code 			VARCHAR2(50),
	source_aui		VARCHAR2(50),
	source_cui		VARCHAR2(50),
	source_dui		VARCHAR2(50),
	insertion_date 		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 900M)
TABLESPACE MRD;


-- This table is part of a two-table representation of
-- co-occurrence data.  States are added/expired
-- whenever the COC data changes.  This tables should track
-- All COC data (MEDLINE, CCPSS, AIR, etc).
ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS';
DROP TABLE mrd_coc_headings;
CREATE TABLE mrd_coc_headings (
 	citation_set_id	        NUMBER(12) NOT NULL,
 	publication_date	DATE NOT NULL,
 	heading_aui	        VARCHAR2(10) NOT NULL,
 	major_topic		VARCHAR2(1) NOT NULL,
 	subheading_set_id       NUMBER(10),
 	root_source		VARCHAR2(20) NOT NULL,
 	coc_type                VARCHAR2(10) NOT NULL,
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
) MONITORING
TABLESPACE MRD
PARTITION BY RANGE (publication_date)
  (
   PARTITION mrd_coc_headings_1965 VALUES LESS THAN ('01-jan-1965 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_1970 VALUES LESS THAN ('01-jan-1970 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_1975 VALUES LESS THAN ('01-jan-1975 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_1980 VALUES LESS THAN ('01-jan-1980 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_1985 VALUES LESS THAN ('01-jan-1985 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_1990 VALUES LESS THAN ('01-jan-1990 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_1995 VALUES LESS THAN ('01-jan-1995 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_2000 VALUES LESS THAN ('01-jan-2000 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_2005 VALUES LESS THAN ('01-jan-2005 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD,
   PARTITION mrd_coc_headings_2010 VALUES LESS THAN ('01-jan-2010 00:00:00')
	PCTFREE 5 PCTUSED 90 STORAGE (INITIAL 100M NEXT 100M ) TABLESPACE MRD
  );


-- This table is the second part of the representation of
-- co-occurrence data.  Headings can co-occur in a citation
-- and have certain subheadings connected to them.  Those
-- subheading relationships are reprsented here.
DROP TABLE mrd_coc_subheadings;
CREATE TABLE mrd_coc_subheadings (
	citation_set_id		NUMBER(12) NOT NULL,
	subheading_set_id	NUMBER(10) NOT NULL,
	subheading_qa		CHAR(2) NOT NULL,
	subheading_major_topic	VARCHAR(1) NOT NULL,
	-- The check is unnecessary since data comes from coc_subheadings
	-- which is already validated.
	--	CHECK (subheading_major_topic in ('Y','N')),
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
	--, CONSTRAINT mrd_coc_subheadings_pk PRIMARY KEY (citation_set_id,
	-- subheading_set_id, subheading_qa, insertion_date)
)
PCTFREE 10 PCTUSED 80 MONITORING TABLESPACE MRD;
--ORGANIZATION INDEX COMPRESS 1;
--STORAGE (INITIAL 1200M);

-- This table is used to track statistics about
-- columns for various release files. Eventually
-- it is used in the building of MRCOLS
--
-- The table needs to be persistent so that
-- each release handler can generate data for its
-- own file.
DROP TABLE mrd_column_statistics;
CREATE TABLE mrd_column_statistics (
	file_name	VARCHAR2(20)	NOT NULL,
	column_name	VARCHAR2(20)	NOT NULL,
	min_length	NUMBER(12)  	DEFAULT 0 NOT NULL,
	max_length	NUMBER(12)  	DEFAULT 0 NOT NULL,
	average_length	NUMBER(12,4)  	DEFAULT 0 NOT NULL,
	data_type	VARCHAR2(100)	NOT NULL,
	description	VARCHAR2(1000),
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING TABLESPACE MRD;

-- This table tracks mrd states that come from concept_status
-- It can be used to look up a list of CUIs that were actvie
-- during a particular time.
DROP TABLE mrd_concepts;
CREATE TABLE mrd_concepts (
	concept_id 		NUMBER(12) NOT NULL,
	cui			VARCHAR2(10) NOT NULL,
	status			VARCHAR2(10) NOT NULL, /* MRSAT field */
	major_revision_date	DATE NOT NULL,
	insertion_date 		DATE NOT NULL,
	expiration_date 	DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M) TABLESPACE MRD;


-- This table tracks mrd states for contexts.  The data is
-- derived from the PAR rows in the context_relationships table.
-- Note, sine the data comes from PAR rows, it is not obvious
-- that the context informaiton for treetops will be in
-- this table, but they are.
--
-- RUI can be null in cases where the
-- parent_treenum is null (for treetops)
--
DROP TABLE mrd_contexts;
CREATE TABLE mrd_contexts(
	aui			VARCHAR2(10) NOT NULL,
	parent_treenum	       	VARCHAR2(1000),
	root_source		VARCHAR2(20),
	hierarchical_code	VARCHAR2(1000),
        relationship_attribute  VARCHAR2(100),
	release_mode		VARCHAR2(10) NOT NULL,
	rui 			VARCHAR2(12),
	source_rui 		VARCHAR2(50),
	relationship_group 	VARCHAR2(10),
	insertion_date 		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 500M) TABLESPACE MRD;

-- This table tracks mrd states for content view members
-- the data for this table comes from content_view_members
DROP TABLE mrd_content_view_members;
CREATE TABLE mrd_content_view_members(
 	meta_ui			VARCHAR2(20) NOT NULL,
	code		        NUMBER NOT NULL, -- Powers of 2
	cascade		        VARCHAR2(1) NOT NULL,
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) CACHE TABLESPACE MRD;

-- This table tracks mrd states for content views
-- the data for this table comes from content_views
DROP TABLE mrd_content_views;
CREATE TABLE mrd_content_views (
	content_view_id			NUMBER(12) NOT NULL,
        contributor	        	VARCHAR2(100) NOT NULL,
        contributor_version		VARCHAR2(100) NOT NULL,
        contributor_date		DATE NOT NULL,
        maintainer	        	VARCHAR2(100) NOT NULL,
        maintainer_version		VARCHAR2(100) NOT NULL,
        maintainer_date			DATE NOT NULL,
 	content_view_name		VARCHAR2(1000) NOT NULL,
 	content_view_description 	VARCHAR2(4000) NOT NULL,
 	content_view_algorithm  	VARCHAR2(4000) NOT NULL,
 	content_view_category  		VARCHAR2(100) NOT NULL,
 	content_view_subcategory 	VARCHAR2(100),
	content_view_class		VARCHAR2(100) NOT NULL,
 	content_view_code  		NUMBER NOT NULL, -- Powers of 2
 	content_view_previous_meta 	VARCHAR2(10),
	content_view_contributor_url 	VARCHAR2(1000),
	content_view_maintainer_url 	VARCHAR2(1000),
	cascade	  			VARCHAR2(1) NOT NULL,
	is_generated			VARCHAR2(1),
	insertion_date			DATE NOT NULL,
	expiration_date			DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) CACHE TABLESPACE MRD;


-- This table tracks mrd states for cui history
-- the data for this table comes from cui_history and relationships
DROP TABLE mrd_cui_history;
CREATE TABLE mrd_cui_history (
 	cui1			VARCHAR2(10) NOT NULL,
	ver			VARCHAR2(50),
 	relationship_name	VARCHAR2(10) NOT NULL,
 	relationship_attribute	VARCHAR2(100),
 	map_reason		VARCHAR2(4000),
 	cui2			VARCHAR2(10),
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) CACHE TABLESPACE MRD;

-- This table is used to track statistics about
-- the various release files. Eventually
-- it is used in the building of MRFILES
--
-- The table needs to be persistent so that
-- each release handler can generate data for its
-- own file.
DROP TABLE mrd_file_statistics;
CREATE TABLE mrd_file_statistics  (
	file_name	VARCHAR2(20)	NOT NULL,
	column_list	VARCHAR2(200)	NOT NULL,
	byte_count	NUMBER(12)  	DEFAULT 0 NOT NULL,
	line_count	NUMBER(12)  	DEFAULT 0 NOT NULL,
	description	VARCHAR2(1000),
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING TABLESPACE MRD;


-- This table maintains the meme_properties over time.
-- This table tracks configuration data for MRCOLS/MRFILES
-- and poteintially other system config data.  To accurately
-- reconstruct an old MRCOLS, we need the old data so this
-- tracks the changes to the properties over time.
DROP TABLE mrd_properties;
CREATE TABLE mrd_properties (
 	key			VARCHAR2(100) NOT NULL,
 	key_qualifier		VARCHAR2(100),
 	value			VARCHAR2(4000),
 	description		VARCHAR2(1000),
	definition		VARCHAR2(4000),
	example			VARCHAR2(4000),
	reference		VARCHAR2(1000),
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING TABLESPACE MRD;


-- Ths table tracks mrd states arising from the relationships table
DROP TABLE mrd_relationships;
CREATE TABLE mrd_relationships(
	relationship_level      VARCHAR2(1) NOT NULL,
	aui_1 			VARCHAR2(10),
	aui_2 			VARCHAR2(10),
	cui_1 			VARCHAR2(10) NOT NULL,
	cui_2	 		VARCHAR2(10) NOT NULL,
	sg_type_1 		VARCHAR2(50) NOT NULL,
	sg_type_2 		VARCHAR2(50) NOT NULL,
	relationship_name 	VARCHAR2(10),
	relationship_attribute 	VARCHAR2(100),
	suppressible		VARCHAR2(10) NOT NULL,
	root_source	 	VARCHAR2(20) NOT NULL,
	root_source_of_label	VARCHAR2(20) NOT NULL,
	rui 			VARCHAR2(12) NOT NULL,
	source_rui 		VARCHAR2(50),
	relationship_group 	VARCHAR2(10),
	rel_directionality_flag VARCHAR2(1),
	insertion_date 		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 200M) TABLESPACE MRD;

-- This table tracks mrd states for sources.  The data
-- comes from source_rank.
DROP TABLE mrd_source_rank;
CREATE TABLE mrd_source_rank(
	source			VARCHAR2(20) NOT NULL,
	rank			NUMBER(12) DEFAULT 0 NOT NULL,
	restriction_level	NUMBER(12) DEFAULT 0 NOT NULL,
	normalized_source	VARCHAR2(20) NOT NULL,
	root_source		VARCHAR2(20),
	source_official_name	VARCHAR2(4000),
	source_short_name	VARCHAR2(4000),
	citation		VARCHAR2(4000),
	character_Set		VARCHAR2(50),
	source_family		VARCHAR2(20),
	version			VARCHAR2(20),
	valid_start_date	DATE,
	valid_end_date		DATE,
	insert_meta_version	VARCHAR2(20),
	remove_meta_version	VARCHAR2(20),
	nlm_contact		VARCHAR2(100),
	inverter_contact	VARCHAR2(100),
	acquisition_contact	VARCHAR2(1000),
	content_contact		VARCHAR2(1000),
	license_contact		VARCHAR2(1000),
	release_url_list	VARCHAR2(1000),
	context_type		VARCHAR2(100),
	language		VARCHAR2(10),
	is_current		CHAR(1),
	rel_directionality_flag VARCHAR2(1),
	notes			VARCHAR2(1000),
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) CACHE TABLESPACE MRD;


-- This table represents mrd states for the long attributes
-- This is basically a historical version of stringtab.
-- States really only need to be expired if the text value
-- for a particular string_id,row_sequence changes.
-- Otherwise all data can remain "alive" because it joins
-- back on mrd_attributes (where attribute_value like '<>Long%')
DROP TABLE mrd_stringtab;
CREATE TABLE mrd_stringtab(
	hashcode 		VARCHAR2(100),
	row_sequence 		NUMBER(12) NOT NULL,
	text_total 		NUMBER(12) NOT NULL,
	text_value 		VARCHAR2(1786),
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 50M) TABLESPACE MRD;


-- This table tracks mrd states for termgroups
-- the data for this table comes from termgroup_rank;
DROP TABLE mrd_termgroup_rank;
CREATE TABLE mrd_termgroup_rank(
	rank			NUMBER(12) NOT NULL,
	termgroup		VARCHAR2(40) NOT NULL,
	normalized_termgroup	VARCHAR2(40) NOT NULL,
	tty			VARCHAR2(20),
	suppressible		VARCHAR2(20) NOT NULL,
	insertion_date		DATE NOT NULL,
	expiration_date		DATE
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 10M) CACHE TABLESPACE MRD;


/************************* Temporary tables *************************/

/** The following tables have the same structure as the mrd_... tables
    apart from not having fields for insertion_date and expiration_date
    They are used to create new MRD states before inserting them into
    the MRD **/

/** indexes on concept_id, aui **/
DROP TABLE tmp_classes;
CREATE TABLE tmp_classes(
	atom_id 		NUMBER(12) NOT NULL
		CONSTRAINT tmp_classes_pk PRIMARY KEY,
	aui 			VARCHAR2(10) NOT NULL,
	cui	 		VARCHAR2(10) NOT NULL,
	lui 			VARCHAR2(10) NOT NULL,
	isui			VARCHAR2(10) NOT NULL,
	sui 			VARCHAR2(10) NOT NULL,
	suppressible		VARCHAR2(10),
	language		VARCHAR2(10),
	root_source 		VARCHAR2(20) NOT NULL,
	tty	 		VARCHAR2(20),
	code 			VARCHAR2(50),
	source_aui		VARCHAR2(50),
	source_cui		VARCHAR2(50),
	source_dui		VARCHAR2(50)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M) TABLESPACE MRD;

/** indexes on cui_1, cui_2, aui_1, aui_2,
    relationship_id **/
DROP TABLE tmp_relationships;
CREATE TABLE tmp_relationships(
	relationship_level      VARCHAR2(1) NOT NULL,
	aui_1 			VARCHAR2(10),
	aui_2			VARCHAR2(10),
	cui_1 			VARCHAR2(10) NOT NULL,
	cui_2	 		VARCHAR2(10) NOT NULL,
	sg_type_1 		VARCHAR2(50) NOT NULL,
	sg_type_2 		VARCHAR2(50) NOT NULL,
	relationship_name 	VARCHAR2(10),
	relationship_attribute 	VARCHAR2(100),
	suppressible		VARCHAR2(10) NOT NULL,
	root_source	 	VARCHAR2(20) NOT NULL,
	root_source_of_label	VARCHAR2(20) NOT NULL,
	rui 			VARCHAR2(12),
			-- CONSTRAINT tmp_relationships_pk PRIMARY KEY,
	source_rui 		VARCHAR2(50),
	rel_directionality_flag VARCHAR2(1),
	relationship_group 	VARCHAR2(10)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M) TABLESPACE MRD;

/** indexes on attribute_id, aui, concept_id **/
DROP TABLE tmp_attributes;
CREATE TABLE tmp_attributes(
	attribute_level		VARCHAR2(1) NOT NULL,
	ui			VARCHAR2(10),
	cui			VARCHAR2(10) NOT NULL,
	lui			VARCHAR2(10),
	sui			VARCHAR2(10),
	sg_type 		VARCHAR2(50) NOT NULL,
	suppressible		VARCHAR2(10) NOT NULL,
	attribute_name 		VARCHAR2(50) NOT NULL,
	attribute_value 	VARCHAR2(200),
	code			VARCHAR2(50),
	root_source		VARCHAR2(20) NOT NULL,
	atui 			VARCHAR2(12),
			--CONSTRAINT tmp_attributes_pk PRIMARY KEY,
	source_atui 		VARCHAR2(50),
	hashcode 		VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 500M) TABLESPACE MRD;

/** index on concept_id **/
DROP TABLE tmp_concepts;
CREATE TABLE tmp_concepts (
	concept_id 		NUMBER(12) NOT NULL,
	cui			VARCHAR2(10) NOT NULL
			CONSTRAINT tmp_concepts_pk PRIMARY KEY,
	status			VARCHAR2(10) NOT NULL /* MRSAT field */,
	major_revision_date	DATE NOT NULL
)
PCTFREE 10 PCTUSED 80 MONITORING
STORAGE (INITIAL 100M) TABLESPACE MRD;


-- Staging area for mrd_properties
DROP TABLE tmp_properties;
CREATE TABLE tmp_properties (
 	key			VARCHAR2(100) NOT NULL,
 	key_qualifier		VARCHAR2(100),
 	value			VARCHAR2(4000),
 	description		VARCHAR2(1000),
	definition		VARCHAR2(4000),
	example			VARCHAR2(4000),
	reference		VARCHAR2(1000)
)
PCTFREE 10 PCTUSED 80 MONITORING TABLESPACE MRD;
