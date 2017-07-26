/*****************************************************************************
*
* File:  $MEME_HOME/Contexts/tables.sql
* Author: DSS 
*
* Remarks:  This script is used to create the context tables.
*
*
* 06/05/2000 3.2.2: Create context tables
*
*
* Version Info:
*   Release 3
*   Version 3.5
*   Deborah Shapiro (6/05/2000)
* 
*****************************************************************************/
set autocommit on;


--  This table is one of the input tables needed for context processing. 
--  It contains the treenum field which is a . deliminated list of
--  concepts in the path to the source concept. The sort_field is not    
--  currently being used for anything.  The context_number is determined
--  from within the db and is not input in treepos.dat
DROP TABLE treepos;
CREATE TABLE treepos (
	source_atom_id	NUMBER(12) NOT NULL, 
	context_number	NUMBER(12),
	hcd		VARCHAR2(1000),
	treenum 	VARCHAR2(4000),
	rela		VARCHAR2(100),
	sort_field      NUMBER(4),
	source_rui	VARCHAR2(100),
	relationship_group VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80
STORAGE (INITIAL 20M PCTINCREASE 0);

-- This table is an optional table which is a list of the parents of 
-- children which should not be included in the raw3 file, MRREL, or MRCXT.  
-- The options for the token are as follows:
-- NO_SIB
-- NO_CHD
-- NO_SIB_MRREL
-- NO_CHD_MRCXT
-- NO_SIB_MRCXT
-- NO_CHD_MRREL
DROP TABLE exclude_list;
CREATE TABLE exclude_list (
	parnum 	VARCHAR2(1000),
	token   VARCHAR2(15)
)
PCTFREE 10 PCTUSED 80
STORAGE (INITIAL 20M PCTINCREASE 0);


-- This table is used to store the broader than/narrower than relationships 
-- that are used to make a treepos file.  This table can be the output of 
-- the code_ranges data and processing.
DROP TABLE bt_nt_rels;
CREATE TABLE bt_nt_rels(
	source_atom_id_1	NUMBER(12) NOT NULL,
	rel			VARCHAR2(30),
	rela			VARCHAR2(100),
	source_atom_id_2	NUMBER(12) NOT NULL,
	source_rui	VARCHAR2(100),
	relationship_group VARCHAR2(100)
)
PCTFREE 10 PCTUSED 80
STORAGE (INITIAL 20M PCTINCREASE 0);

	
-- This table is used to create broader than/narrower than relationships.  
DROP TABLE code_ranges;
CREATE TABLE code_ranges(
	source_atom_id	NUMBER(12) NOT NULL,
	context_level	NUMBER(12),
	low_range	VARCHAR2(30),
	high_range	VARCHAR2(30)
)
PCTFREE 10 PCTUSED 80
STORAGE (INITIAL 20M PCTINCREASE 0);

-- This is one of the temporary tables needed for context processing.  
-- It contains a list of all of the distinct treenums from treepos and their 
-- parents.  The parents path is the subset of the child's path, leaving off the-- last source_atom_id. 
DROP TABLE parent_treenums;
CREATE TABLE parent_treenums(
	treenum			VARCHAR2(1000),
	parnum			VARCHAR2(1000),
	rela			VARCHAR2(100),
	context_number		NUMBER(12)
)
PCTFREE 10 PCTUSED 80
STORAGE (INITIAL 20M PCTINCREASE 0);


-- This is one of the temporary tables needed for contexts processing.  
-- It  maintains a count of the number of contexts existing for a given 
-- source_atom_id at any given time in the processing.
DROP TABLE context_numbers;
CREATE TABLE context_numbers(
	source_atom_id			NUMBER(12) NOT NULL,
	max_context_number		NUMBER(12)
)
PCTFREE 10 PCTUSED 80
STORAGE (INITIAL 20M PCTINCREASE 0);


-- This is one of the input tables needed for context processing.  It has 
-- otherwise been known as ATOMS in the past.
-- The hcd field is only to be used when there is no original treepos file
-- and treepos needs to be created from source_atoms.dat and either bt_nt_rels
-- or code_ranges.dat  Otherwise, in the cases where there is an original 
-- treepos.dat, the hcd will only be read from the hcd field in treepos.dat
DROP TABLE source_atoms;
CREATE TABLE source_atoms(
	source_atom_id 		NUMBER(12) NOT NULL,
	termgroup		VARCHAR2(60) NOT NULL,
        code			VARCHAR2(30),
	atom_name 		VARCHAR2(3000) NOT NULL,
        hcd			VARCHAR2(1000),
	sg_id			VARCHAR2(100),
	sg_type			VARCHAR2(50),
	sg_qualifier		VARCHAR2(50)
)
PCTFREE 10 PCTUSED 80
STORAGE (INITIAL 20M PCTINCREASE 0);


-- The contexts_raw table is the output table from contexts processing that is 
-- written into the raw3 and all files.  
-- The context_level field indicates ancestor (1-49), self (50), child (99), or
-- sibling (60) relationship from the source_atom_id_2 to the source_atom_id_1.
-- The sort_field is not currently being used.
-- The atom_name is that of the source_atom_id_2.
-- The scd is the code from source_atoms.
-- The hcd is the code from treepos.
-- xc is 1 if the source_atom_id_2 is a parent.
-- The source is the first part of the termgroup from source_atoms.
-- mrrel and mrcxt flags are 1 if the row should be included in mrrel or mrcxt.
DROP TABLE contexts_raw;
CREATE TABLE contexts_raw(
	source_atom_id_1	NUMBER(12) NOT NULL,
	context_number 		NUMBER(12) NOT NULL,
	context_level 		NUMBER(12) NOT NULL, 
	sort_field		NUMBER(4),
        atom_name		VARCHAR2(3000) NOT NULL,
	source_atom_id_2	NUMBER(12),
	scd			VARCHAR2(30),
	hcd			VARCHAR2(1000),
	rela			VARCHAR2(100),
	xc			NUMBER(1),
	source			VARCHAR2(40) NOT NULL,
	source_of_context	VARCHAR2(40) NOT NULL,
	mrcxt_flag		NUMBER(1),
	mrrel_flag		NUMBER(1)
)
PCTFREE 10 PCTUSED 80
STORAGE (INITIAL 20M PCTINCREASE 0);


