CREATE OR REPLACE PACKAGE MEME_APROCS AS

/****************************************************************************
 *
 * PL/SQL File: MEME_APROCS.sql
 *
 * To call each atomic_action in the package:
 * execute MEME_APROCS.atomic_action(...)
 *
 * Version Information
 *
 * 03/11/2005 3.29.0: force works for change field actions
 * 01/14/2005 3.28.0: digup_attributes not properly digging up 
 * 01/04/2005 3.27.1: digup_attributes not properly digging up 
 *                    dead_stringtab data!
 * 12/13/2004 3.27.0: Released
 * 12/10/2004 3.26.4: Inserting source level E-% attributes should use
 *                    MTH source
 * 11/29/2004 3.26.3: - report_insert_data
 * 11/23/2004 3.26.2: ATUI,RUI,AUI now use data-driven prefix and length
 * 11/22/2004 3.26.1: Minor fixes based on test suites
 * 11/19/2004 3.26.0: Released
 * 11/04/2004 3.25.1: Better handling of ATUI,RUI if level changes.
 *                    AUI logic no longer nulls code for NLM02/RXNORM data
 *                    RUI logic handles MTH,NLM03 and MTHRELA data properly
 * 10/06/2004 3.25.0: Released
 * 09/23/2004 3.24.1: sg_meme_id, sg_meme_data_type support for rels/attributes
 * 09/20/2004 3.24.0: released.
 * 09/14/2004 3.23.1: inverse RUI not inserted if self-referential 
 *                    (logic improved).  Always fix sg_ids for AUI
 *                    and C level relationships and attributes.
 * 08/09/2004 3.23.0: Released
 * 07/30/2004 3.22.3: when inserting into classes set language=ENG
 * 07/16/2004 3.22.2: Infrastructure for "force" undo/redo
 *                    Released
 * 06/14/2004 3.22.1: Minor change to assign_atui (for SEMANTIC_TYPE).
 * 06/09/2004 3.22.0: Minor change to assign_rui. Released
 * 05/19/2004 3.21.0: Released
 * 05/03/2004 3.20.1: change field now recomptues aui on change in SCUI,
 *                    SAUI, SDUI and recomputes rui on change in SRUI,
 *                    and recomptues atui on change in SATUI.
 * 04/28/2004 3.20.0: Released
 * 04/26/2004 3.19.1: Unreleasable atom gets null AUI, and AUI is recomputed
 *                    if atom goes from tbr=Nn to tbr=Yy
 * 04/19/2004 3.19.0: Released
 * 04/01/2004 3.18.1: Upgraded assign_atui,aui,rui to include 
 *                    source*ui identifiers
 * 11/07/2003 3.15.1: Validates semantic type values when inserted
 *                    we should also validate attribute names!
 * 02/03/2004 3.17.1: Fixed error code for bad STY
 * 01/06/2004 3.17.0: Released
 * 01/06/2004 3.16.1: relationships_ui uses normalized sources so we have
 *                    to check RXNORM instead of NLM03 when deciding whether or
 *                    not to use a null RELA.
 * 12/01/2003 3.16.0: Released
 * 11/07/2003 3.15.1: Validates semantic type values when inserted
 *                    we should also validate attribute names!
 * 10/09/2003 3.15.0: Released
 * 10/08/2003 3.14.2: fix to code that updates sg_id when a concept_id
 *                    changes for C level data because the "new concept id"
 *                    was a different data type than the existing sg_id for
 *                    cases like ATX_REL which have sg_id of CUI.
 * 10/06/2003 3.14.1: assign_* procedures now use root source
 *                    of the normalized source.  
 *                    inverse_relationships_ui must be populated
 *		      in both directions.
 * 09/30/2003 3.14.0: Implemented ATUI and RUI assignment.
 *                    Updated the actions which affect these
 *		      assignments.
 * 05/12/2003 3.13.0: Released
 * 05/06/2003 3.12.1: No longer deal with sg_* fields
 *                    aproc_* insert fields handle sg_* fields
 * 11/27/2002 3.12.0: aproc_insert_classes assigns tty
 * 09/26/2002 3.11.0: aproc_insert_classes accepts an aui
 * 07/17/2002 3.10.0: validate_atomic_action was failing because the
 *                   'primary_key' lookup at location '35' was using
 *                   the table code (like 'C') instead of the table
 *                   name (like 'classes').  The data in code_map
 *                   most likely changed since the original implementation
 *                   of this procedure.
 * 07/26/2001 3.9.0: ll_relationship_attribute field in insert_rel was
 *                   only 20 chars, expanded to 100.
 *		     l_old_value and l_new_value fields in aproc_undo
 *		     and aproc_redo were similarly updated.
 * 04/12/2001 3.8.0: Released version
 * 04/09/2001 3.7.1: Change to aproc_change_atom_id to allow atom_ids ofg
 *		"MTH Asserted" information to be maintained also 
 * 12/11/2000 3.7.0:  Released to NLM
 * 12/04/2000: aproc_change_dead works with context_relationships.
 *             +bury_cxt_relationships, +digup_cxt_relationships.
 * 11/27/2000: fix to aproc_change_dead: updating dead_sg_relationships
 *             does not necessarily update 1 row.  Change
 *             SQL%ROWCOUNT != 1 to SQL%ROWCOUNT > 1
 * 11/22/2000: Changes in aproc_change_atom_id exception
 * 11/16/2000: bury_attributes, bury_relationships, digup_attributes and
 *             digup_relationships needs to deal with sg_attributes and
 *             sg_relationships
 * 11/13/2000: Recall old exception handling
 * 11/10/2000: report_insert_data needed to look up LOWER(table_name) when
 *		finding 'primary_key'
 *	       use of oq_mode before dealing with operations_queue
 * 10/16/2000: Changes in aproc_change_atom_id, aproc_change_id,
 *	       aproc_change_status, aproc_change_tbr, aproc_redo, aproc_undo,
 *	       report_insert_data, validate_atomic_action
 * 9/27/2000:  Changes in aproc_insert_*
 * 9/26/2000:  clean up all error report mechanism,
 *	       replaced all hardcoded constants which are defined in
 *	       MEME_CONSTANTS
 * 9/25/2000:  small changes in aproc_change_id, aproc_change_concept_id
 *	       and aproc_change_status to report error
 * 9/21/2000:  changes to report_insert_data, aproc_insert_(*)
 * 9/19/2000:  meme_aprocs_mode to MEME_UTILITY.meme_mode
 * 9/18/2000:  report_insert_data to be called in atomic insert
 * 9/13/2000:  aproc_change_atom_id
 * 9/12/2000:  report_insert_data, fix to aproc_change_id
 * 8/29/2000:  connect_concepts bug fixed.
 * 8/24/2000:  Aprocs do not raise application errors
 * 8/16/2000:  in MRD mode, when a concept_id changes or an atom_id changes
 *		which moves the element to a new concept, the old_value and
 *		new_value must be added to connected_concepts
 * 8/15/2000:  don't RAISE_APPLICATION_ERROR because molecular_actions
 *  		will now just open a window and the error is logged
 *		already to meme_Error
 * 8/01/2000:  Package handover version
 * 7/27/2000:  MRD and MID modes for atomic actions.  In MRD mode
 *	       just make the data changes, do not insert action rows.
 * 7/24/2000:  When validating a change field action where the old value
 *	       is null, need to compare '.'||field  not just field.
 * 6/21/2000:  Small change: when validating AA_MOVE on TN_CLASSES
 *	       added the atom_id=row_id to the where clause.
 *	       when validating AA_DELETE changed logic to check if we
 *	       are deleting a row or undeleting a row
 * 6/12/2000:  validate_atomic_action
 * 5/9/2000:   atomic_actions was changed to support null values in
 *	       {old,new}_value fields.	This alleviates earlier problem.
 * 5/1/2000:   Null problem in aproc_change_field
 * 9/9/1999:   First version created and compiled
 *
 * Status:
 *   Functionality: DONE
 *	   Testing: DONE
 *    Enhancements:
 *	Implement an aproc_replace procedure.
 *	aproc_change_field does no checking, only changing
 *	Future:  MEME_RANKS.get_field_rank(...);
 *
 ******************************************************************************/

   package_name 	       	VARCHAR2(25) := 'MEME_APROCS';
   release_number	       	VARCHAR2(1)  := '4';
   version_number	       	VARCHAR2(5)  := '29.0';
   version_date 	       	DATE	     := '11-Mar-2005';
   version_authority	       	VARCHAR2(3)  := 'BAC';

   meme_aprocs_debug	       	BOOLEAN := FALSE;
   meme_aprocs_trace	       	BOOLEAN := FALSE;
   meme_aprocs_validate        	BOOLEAN := FALSE;

   -- trace variables
   method		       	VARCHAR2(50);
   location		       	VARCHAR2(10);
   error_code		       	NUMBER(12);
   error_detail 	    	VARCHAR2(256);

   -- Exception
   meme_aprocs_exception       	EXCEPTION;
   aproc_exception	       	EXCEPTION;
   aproc_bad_count_err	       	EXCEPTION;
   aproc_bad_retval_err        	EXCEPTION;
   aproc_invalid_field_err      EXCEPTION;
   aproc_validate_action_err    EXCEPTION;

   FUNCTION release RETURN INTEGER;
   FUNCTION version RETURN FLOAT;
   FUNCTION version_info RETURN VARCHAR2;

   PRAGMA restrict_references(version_info,WNDS,RNDS,WNPS);

   PROCEDURE version;
   PROCEDURE set_validate_on;
   PROCEDURE set_validate_off;
   PROCEDURE set_mode_mrd;
   PROCEDURE set_mode_mid;
   PROCEDURE help;
   PROCEDURE help(method_name IN VARCHAR2);
   PROCEDURE register_package;
   PROCEDURE self_test;

   PROCEDURE initialize_trace(l_method IN VARCHAR2);

   PROCEDURE meme_aprocs_error(
      method		       IN VARCHAR2,
      location		       IN VARCHAR2,
      error_code	       IN INTEGER,
      detail		       IN VARCHAR2,
      raise_error_flag	       IN VARCHAR2 := MEME_CONSTANTS.YES
   );

   FUNCTION aproc_change_field(
      row_id		       IN INTEGER,
      table_name	       IN VARCHAR2,
      field_name	       IN VARCHAR2,
      old_value 	       IN VARCHAR2,
      new_value 	       IN VARCHAR2,
      action_status	       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      atomic_action_id 	       IN INTEGER := 0,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION aproc_change_concept_id(
      atom_id		       IN INTEGER,
      old_concept_id	       IN INTEGER,
      new_concept_id	       IN INTEGER,
      action_status	       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      atomic_action_id 	       IN INTEGER := 0,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION aproc_change_atom_id(
      row_id		       IN INTEGER,
      table_name	       IN VARCHAR2,
      old_atom_id	       IN INTEGER,
      new_atom_id	       IN INTEGER,
      action_status	       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      atomic_action_id 	       IN INTEGER := 0,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION aproc_change_dead(
      table_name	       IN VARCHAR2,
      row_id		       IN INTEGER,
      old_dead		       IN VARCHAR2,
      new_dead		       IN VARCHAR2,
      action_status	       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      atomic_action_id 	       IN INTEGER := 0,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION aproc_change_id(
      table_name	       IN VARCHAR2,
      row_id		       IN INTEGER,
      old_concept_id	       IN INTEGER,
      new_concept_id	       IN INTEGER,
      action_status	       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      atomic_action_id 	       IN INTEGER := 0,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION aproc_change_status(
      table_name	       IN VARCHAR2,
      row_id		       IN INTEGER,
      old_status	       IN VARCHAR2,
      new_status	       IN VARCHAR2,
      action_status	       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      atomic_action_id 	       IN INTEGER := 0,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION aproc_change_tbr(
      table_name	       IN VARCHAR2,
      row_id		       IN INTEGER,
      old_tobereleased	       IN VARCHAR2,
      new_tobereleased	       IN VARCHAR2,
      action_status	       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      atomic_action_id 	       IN INTEGER := 0,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION assign_atui (
      attribute_id		IN INTEGER
   ) RETURN VARCHAR2;

   FUNCTION assign_atui (
      source			IN VARCHAR2,
      attribute_level		IN VARCHAR2,
      attribute_name		IN VARCHAR2,
      tobereleased		IN VARCHAR2,
      hashcode			IN VARCHAR2,
      sg_id			IN OUT VARCHAR2,
      sg_type			IN OUT VARCHAR2,
      sg_qualifier		IN OUT VARCHAR2,
      source_atui		IN VARCHAR2,
      concept_id		IN INTEGER,
      atom_id			IN INTEGER,
      atui			IN VARCHAR2 := NULL
   ) RETURN VARCHAR2;

   FUNCTION aproc_insert_attribute(
      source_attribute_id      IN INTEGER,
      origin		       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      l_level		       IN VARCHAR2,
      l_concept_id	       IN INTEGER,
      l_atom_id 	       IN INTEGER,
      l_attribute_name	       IN VARCHAR2,
      l_source		       IN VARCHAR2,
      l_status		       IN VARCHAR2,
      l_attribute_value        IN VARCHAR2,
      l_generated	       IN VARCHAR2,
      l_released	       IN VARCHAR2,
      l_tobereleased	       IN VARCHAR2,
      l_suppressible	       IN VARCHAR2,
      atomic_action_id 	       IN INTEGER := 0,
      attribute_id 	       IN INTEGER := 0,
      l_sg_id 	               IN VARCHAR2 := '',
      l_sg_type	               IN VARCHAR2 := '',
      l_sg_qualifier           IN VARCHAR2 := '',
      l_source_atui            IN VARCHAR2 := '',
      l_hashcode               IN VARCHAR2 := null
   ) RETURN INTEGER;

   -- Used to maintain AUI values
   -- as source, code or TTY are changed
   FUNCTION assign_aui (
      atom_id			IN INTEGER
   ) RETURN VARCHAR2;

   FUNCTION assign_aui (
      source			IN VARCHAR2,
      tty			IN VARCHAR2,
      code			IN VARCHAR2,
      sui			IN VARCHAR2,
      source_aui		IN VARCHAR2,
      source_cui		IN VARCHAR2,
      source_dui		IN VARCHAR2,
      tobereleased		IN VARCHAR2
   ) RETURN VARCHAR2;

   FUNCTION aproc_insert_classes(
      source_atom_id	       IN INTEGER,
      origin		       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      l_atom_name	       IN VARCHAR2,
      l_source		       IN VARCHAR2,
      l_termgroup	       IN VARCHAR2,
      l_code		       IN VARCHAR2,
      l_aui		       IN VARCHAR2 := NULL,
      l_sui		       IN VARCHAR2,
      l_isui		       IN VARCHAR2,
      l_lui		       IN VARCHAR2,
      l_generated	       IN VARCHAR2,
      l_last_release_cui       IN VARCHAR2,
      l_last_assigned_cui      IN VARCHAR2,
      l_status		       IN VARCHAR2,
      l_concept_id	       IN INTEGER,
      l_tobereleased	       IN VARCHAR2,
      l_released	       IN VARCHAR2,
      l_last_release_rank      IN INTEGER,
      l_suppressible	       IN VARCHAR2,
      l_source_aui	       IN VARCHAR2 := NULL,
      l_source_cui	       IN VARCHAR2 := NULL,
      l_source_dui	       IN VARCHAR2 := NULL,
      atomic_action_id 	       IN INTEGER := 0,
      atom_id 		       IN INTEGER := 0
   ) RETURN INTEGER;

   FUNCTION aproc_insert_cs(
      source_concept_id        IN INTEGER,
      origin		       IN VARCHAR2,
      source		       IN VARCHAR2,
      status		       IN VARCHAR2,
      tobereleased	       IN VARCHAR2,
      released		       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      atomic_action_id 	       IN INTEGER := 0,
      concept_id 	       IN INTEGER := 0
   ) RETURN INTEGER;

   FUNCTION assign_rui (
      relationship_id		IN INTEGER
   ) RETURN VARCHAR2;

   FUNCTION assign_rui (
      source			IN VARCHAR2,
      relationship_level	IN VARCHAR2,
      relationship_name		IN VARCHAR2,
      relationship_attribute	IN VARCHAR2,
      tobereleased		IN VARCHAR2,
      sg_id_1			IN OUT VARCHAR2,
      sg_type_1			IN OUT VARCHAR2,
      sg_qualifier_1		IN OUT VARCHAR2,
      sg_id_2			IN OUT VARCHAR2,
      sg_type_2			IN OUT VARCHAR2,
      sg_qualifier_2		IN OUT VARCHAR2,
      concept_id_1		IN INTEGER,
      concept_id_2		IN INTEGER,
      atom_id_1			IN INTEGER,
      atom_id_2			IN INTEGER,
      source_rui		IN VARCHAR2,
      rui			IN VARCHAR2 := NULL
   ) RETURN VARCHAR2;

   FUNCTION aproc_insert_rel(
      source_rel_id	       IN INTEGER,
      origin		       IN VARCHAR2,
      molecule_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      timestamp 	       IN DATE,
      l_concept_id_1	       IN INTEGER,
      l_concept_id_2	       IN INTEGER,
      l_atom_id_1	       IN INTEGER,
      l_relationship_name      IN VARCHAR2,
      l_relationship_attribute IN VARCHAR2,
      l_atom_id_2	       IN INTEGER,
      l_source_of_label        IN VARCHAR2,
      l_source		       IN VARCHAR2,
      l_status		       IN VARCHAR2,
      l_generated	       IN VARCHAR2,
      l_level		       IN VARCHAR2,
      l_released	       IN VARCHAR2,
      l_tobereleased	       IN VARCHAR2,
      l_suppressible	       IN VARCHAR2,
      atomic_action_id 	       IN INTEGER := 0,
      relationship_id 	       IN INTEGER := 0,
      l_sg_id_1 	       IN VARCHAR2 := '',
      l_sg_type_1	       IN VARCHAR2 := '',
      l_sg_qualifier_1         IN VARCHAR2 := '',
      l_sg_id_2 	       IN VARCHAR2 := '',
      l_sg_type_2	       IN VARCHAR2 := '',
      l_sg_qualifier_2         IN VARCHAR2 := '',
      l_source_rui 		IN VARCHAR2 := '',
      l_relationship_group      IN VARCHAR2 := ''
   ) RETURN INTEGER;

   FUNCTION aproc_redo(
      atomic_action_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION aproc_undo(
      atomic_action_id	       IN INTEGER,
      authority 	       IN VARCHAR2,
      force	 	       IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION bury_index(
      atom_id		       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION bury_attributes(
      attribute_id	       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION bury_classes(
      atom_id		       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION bury_concept_status(
      concept_id	       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION bury_relationships(
      relationship_id	       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION bury_cxt_relationships(
      relationship_id	       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION digup_index(
      atom_id		       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION digup_attributes(
      attribute_id	       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION digup_classes(
      atom_id		       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION digup_concept_status(
      concept_id	       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION digup_relationships(
      relationship_id	       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION digup_cxt_relationships(
      relationship_id	       IN INTEGER
   ) RETURN INTEGER;

   FUNCTION validate_atomic_action(
      atomic_action_id	       IN INTEGER
   ) RETURN BOOLEAN;

   PROCEDURE connect_concepts(
      new_value			IN INTEGER,
      old_value			IN INTEGER,
      field_name		IN VARCHAR2
   );

END MEME_APROCS;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY MEME_APROCS AS

/* FUNCTION RELEASE ************************************************************
 */
FUNCTION release
RETURN INTEGER
IS
BEGIN
   version;
   return to_number(release_number);
END release;

/* FUNCTION VERSION ************************************************************
 */
FUNCTION version
RETURN FLOAT
IS
BEGIN
   version;
   return to_number(version_number);
END version;

/* FUNCTION VERSION_INFO *******************************************************
 */
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
   return package_name || ' Release ' || release_number || ': ' ||
     'version ' || version_number || ' (' || version_date || ')';
END version_info;

/* PROCEDURE VERSION ***********************************************************
 */
PROCEDURE version
IS
BEGIN

   DBMS_OUTPUT.PUT_LINE('Package: ' || package_name);
   DBMS_OUTPUT.PUT_LINE('Release ' || release_number || ': ' ||
	      'version ' || version_number || ', ' ||
	 version_date || ' (' ||
	 version_authority || ')');

END version;

/* PROCEDURE SET_VALIDATE_ON ***************************************************
 */
PROCEDURE set_validate_on
IS
BEGIN
   meme_aprocs_validate := TRUE;
END set_validate_on;

/* PROCEDURE SET_VALIDATE_OFF **************************************************
 */
PROCEDURE set_validate_off
IS
BEGIN
   meme_aprocs_validate := FALSE;
END set_validate_off;

/* PROCEDURE SET_MODE_MID ******************************************************
 */
PROCEDURE set_mode_mid
IS
BEGIN
   MEME_UTILITY.meme_mode := MEME_CONSTANTS.MID_MODE;
END set_mode_mid;

/* PROCEDURE SET_MODE_MRD ******************************************************
 */
PROCEDURE set_mode_mrd
IS
BEGIN
   MEME_UTILITY.meme_mode := MEME_CONSTANTS.MRD_MODE;
END set_mode_mrd;

/* PROCEDURE INITIALIZE_TRACE **************************************************
 */
PROCEDURE initialize_trace(
   l_method IN VARCHAR2
)
IS
BEGIN
   /* must be called at the begginning of every procedure or function. */
   method	:= UPPER(l_method);
   location	:= '00';
   error_code	:= 0;
   error_detail := '';
END initialize_trace;

/* PROCEDURE MEME_APROCS_ERROR *************************************************
 */
PROCEDURE meme_aprocs_error(
   method		       IN VARCHAR2,
   location		       IN VARCHAR2,
   error_code		       IN INTEGER,
   detail		       IN VARCHAR2,
   raise_error_flag	       IN VARCHAR2 := MEME_CONSTANTS.YES
)
IS
   error_msg		       VARCHAR2(100);
BEGIN
   IF error_code = 1 THEN
      error_msg := 'MA0001: Unspecified error';
   ELSIF error_code = 10 THEN
      error_msg := 'MA0010: No Data Found';
   ELSIF error_code = 20 THEN
      error_msg := 'MA0020: Invalid Field';
   ELSIF error_code = 25 THEN
      error_msg := 'MA0025: Invalid Table Name';
   ELSIF error_code = 30 THEN
      error_msg := 'MA0030: Bad Count';
   ELSIF error_code = 40 THEN
      error_msg := 'MA0040: Bad Return Value';
   ELSIF error_code = 60 THEN
      error_msg := 'MA0060: Bad Level Value';
   ELSIF error_code = 61 THEN
      error_msg := 'MA0061 Bad Status Value';
   ELSIF error_code = 62 THEN
      error_msg := 'MA0062: Bad Level,Status Tuple';
   ELSIF error_code = 63 THEN
      error_msg := 'MA0063: Bad Generated Value';
   ELSIF error_code = 64 THEN
      error_msg := 'MA0064: Bad released Value';
   ELSIF error_code = 65 THEN
      error_msg := 'MA0065: Bad Tobereleased Value';
   ELSIF error_code = 66 THEN
      error_msg := 'MA0066: Bad Suppressible Value';
   ELSIF error_code = 67 THEN
      error_msg := 'MA0067: Bad Source,Authority Tuple';
   ELSIF error_code = 68 THEN
      error_msg := 'MA0068: Bad Source Value';
   ELSIF error_code = 69 THEN
      error_msg := 'MA0069: Bad Termgroup_rank Value';
   ELSIF error_code = 70 THEN
      error_msg := 'MA0070: Bad relationship_name Value';
   ELSIF error_code = 71 THEN
      error_msg := 'MA0071: Bad relationship_attribute Value';
   ELSIF error_code = 72 THEN
      error_msg := 'MA0072: Bad semantic type';
   ELSIF error_code = 80 THEN
      error_msg := 'MA0080: Missing concept_id';
   ELSIF error_code = 90 THEN
      error_msg := 'MA0090: Aproc validation failed';
   ELSE
      error_msg := 'MA0000: Unknown Error';
   END IF;

   ROLLBACK;
--   IF raise_error_flag = MEME_CONSTANTS.YES THEN
--	MEME_UTILITY.PUT_APPLICATION_ERROR( -- Not working yet.
--	'Error in MEME_APROCS::'||method||' at '||
--	   location||' ('||error_msg||','||detail||')');
--   ELSE
      MEME_UTILITY.PUT_ERROR(
      'Error in MEME_APROCS::'||method||' at '||
	   location||' ('||error_msg||','||detail||')');
--   END IF;
   COMMIT;
END meme_aprocs_error;

/* FUNCTION APROC_CHANGE_FIELD *************************************************
 */
FUNCTION aproc_change_field(
   row_id		       IN INTEGER,
   table_name		       IN VARCHAR2,
   field_name		       IN VARCHAR2,
   old_value		       IN VARCHAR2,
   new_value		       IN VARCHAR2,
   action_status	       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   atomic_action_id	       IN INTEGER := 0,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   query		       VARCHAR2(512);
   old_value_clause	       VARCHAR2(256);
   new_value_clause	       VARCHAR2(256);
   atomic_id		       INTEGER;
   rowcount		       INTEGER;
   aa_timestamp 	       DATE;
   value_type		       VARCHAR(25);
   full_tablename	       VARCHAR(50);
   retval		       INTEGER;
   l_ui				VARCHAR2(20);
   l_method		       VARCHAR2(50) := 'APROC_CHANGE_FIELD';
BEGIN

    initialize_trace(l_method);

    SELECT SYSDATE INTO aa_timestamp FROM dual;

    location := '5';
    -- Certain fields are not allowed to change
    IF lower(field_name) like 'sg_%' OR 
       lower(field_name) in ('tobereleased','status') THEN
	error_detail := 'Changing tobereleased, status, sg_type, ' ||
	  	        'sg_id, or sg_qualifier fields is not allowed.';
	RAISE aproc_exception;
    END IF;

    IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
	location := '1';
	atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

    	location := '10';

   -- OSM - 5/1/2000: Null values being passed into old/new_value field.
   --		      Temporary fix to check for them, but should find source.

   	INSERT INTO atomic_actions
   	 (atomic_action_id, molecule_id, action, table_name, row_id,
   	  old_value, new_value, authority, timestamp, status, action_field)
   	VALUES
   	 (atomic_id, molecule_id, MEME_CONSTANTS.AA_CHANGE_FIELD, table_name,
   	  aproc_change_field.row_id,old_value, new_value, authority,
	  aa_timestamp, action_status, field_name);

      	location := '20';
	IF SQL%ROWCOUNT != 1 THEN
	   rowcount := SQL%ROWCOUNT;
	   RAISE aproc_bad_count_err;
   	END IF;

    ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN

	location := '1.1';
	atomic_id := atomic_action_id;

    END IF;

   --  value_type can be {'varchar','number','date',null};

   location := '25';

   full_tablename := UPPER(MEME_UTILITY.get_table_name_by_code(table_name));

   SELECT data_type INTO value_type
   FROM user_tab_columns
   WHERE table_name = full_tablename
   AND column_name = UPPER(field_name);

   IF value_type = 'NUMBER' THEN
      old_value_clause := '= to_number(''' || old_value || ''')';
      new_value_clause := 'to_number(''' || new_value || ''')';
   ELSIF value_type = 'DATE' THEN
      old_value_clause := '= to_date(''' || old_value || ''')';
      new_value_clause := 'to_date(''' || new_value || ''')';
   ELSIF value_type = 'VARCHAR2' OR value_type IS NULL THEN
      old_value_clause := '= ''' || old_value || '''';
      new_value_clause := '''' || new_value || '''';
   END IF;

   IF old_value = '' OR old_value is null THEN
      old_value_clause := ' is null';
   END IF;

   -- If forcing action, ignore old value
   IF force = 'Y' THEN
      old_value_clause := ' = ' || field_name;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_CLASSES THEN
      location := '30';
      query := 'UPDATE classes SET ' ||
	 field_name || '= ' || new_value_clause || ', ' ||
	 'authority = '''||aproc_change_field.authority||''', ' ||
	 'timestamp = '''||aproc_change_field.aa_timestamp||''', ' ||
	 'last_molecule_id = ' || aproc_change_field.molecule_id || ', ' ||
	 'last_atomic_action_id = '|| aproc_change_field.atomic_id || ' ' ||
	 'WHERE atom_id = ' || aproc_change_field.row_id || ' ' ||
	 'AND ' || field_name || ' ' || old_value_clause;

      location := '40';
      rowcount := MEME_UTILITY.exec(query);

      IF rowcount !=1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '45';
      retval := MEME_RANKS.set_atom_rank(row_id, field_name);
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '45.2';
      IF lower(field_name) IN ('source','tty',
			       'code','sui',
				'source_aui','source_dui','source_cui') THEN
      	  location := '45.3';
	  l_ui := assign_aui(atom_id => row_id);
      	  location := '45.4';
	  UPDATE classes
	  SET aui = l_ui
	  WHERE atom_id = row_id
	    AND nvl(aui,'null') != l_ui;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
      location := '50';
      query := 'UPDATE attributes SET ' ||
	 field_name || '= ' || new_value_clause || ', ' ||
	 'authority = '''||aproc_change_field.authority||''', ' ||
	 'timestamp = '''||aproc_change_field.aa_timestamp||''', ' ||
	 'last_molecule_id = ' || aproc_change_field.molecule_id || ', ' ||
	 'last_atomic_action_id = '|| aproc_change_field.atomic_id || ' ' ||
	 'WHERE attribute_id = ' || aproc_change_field.row_id || ' ' ||
	 'AND ' || field_name || ' ' || old_value_clause;

      rowcount := MEME_UTILITY.exec(query);

      location := '60';
      IF rowcount !=1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '65';
      retval := MEME_RANKS.set_attribute_rank(row_id, field_name);
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '85.2';
      IF lower(field_name) IN ('atom_id','concept_id','sg_id',
			       'sg_type','sg_qualifier','attribute_level',
			       'source', 'attribute_name','source_atui') THEN
      	  location := '65.3';
	  l_ui := assign_atui(attribute_id => row_id);
      	  location := '65.4';
	  UPDATE attributes
	  SET atui = l_ui
	  WHERE attribute_id = row_id
	    AND nvl(atui,'null') != l_ui;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
      location := '70';
      query := 'UPDATE relationships SET ' ||
	 field_name || '= ' || new_value_clause || ', ' ||
	 'authority = '''||aproc_change_field.authority||''', ' ||
	 'timestamp = '''||aproc_change_field.aa_timestamp||''', ' ||
	 'last_molecule_id = ' || aproc_change_field.molecule_id || ', ' ||
	 'last_atomic_action_id = '|| aproc_change_field.atomic_id || ' ' ||
	 'WHERE relationship_id = ' || aproc_change_field.row_id || ' ' ||
	 'AND ' || field_name || ' ' || old_value_clause;

      location := '80';
      rowcount := MEME_UTILITY.exec(query);
      IF rowcount !=1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '85';
      retval := MEME_RANKS.set_relationship_rank(row_id, field_name);
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '65.2';
      IF lower(field_name) IN ('atom_id','concept_id','relationship_name',
				'relationship_attribute','source','source_rui') THEN
      	  location := '85.3';
	  l_ui := assign_rui(relationship_id => row_id);
      	  location := '85.4';
	  UPDATE relationships
	  SET rui = l_ui
	  WHERE relationship_id = row_id
	    AND nvl(rui,'null') != nvl(l_ui,'null');
      END IF;
 
      -- Here we may be going from P to C, so we need to set SG identifiers as well
      location := '65.3';
      IF lower(field_name) IN ('relationship_level') THEN 
      	  location := '85.3';
	  l_ui := assign_rui(relationship_id => row_id);
      	  location := '85.4';
      	  UPDATE relationships 
          SET (rui, sg_id_1, sg_type_1, sg_qualifier_1, 
	 	    sg_id_2, sg_type_2, sg_qualifier_2 ) =
	    (SELECT rui, sg_id_1, sg_type_1, sg_qualifier_1, 
	 	         sg_id_2, sg_type_2, sg_qualifier_2
	     FROM relationships_ui WHERE rui = l_ui)
      	  WHERE relationship_id = row_id 	
	    AND nvl(rui,'null') != nvl(l_ui,'null');
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
      location := '90';
      query := 'UPDATE concept_status SET ' ||
	 field_name || '= ' || new_value_clause || ', ' ||
	 'authority = '''||aproc_change_field.authority||''', ' ||
	 'timestamp = '''||aproc_change_field.aa_timestamp||''', ' ||
	 'last_molecule_id = ' || aproc_change_field.molecule_id || ', ' ||
	 'last_atomic_action_id = '|| aproc_change_field.atomic_id || ' ' ||
	 'WHERE concept_id = ' || aproc_change_field.row_id || ' ' ||
	 'AND ' || field_name || ' ' || old_value_clause;

      location := '100';
      rowcount := MEME_UTILITY.exec(query);
      IF rowcount !=1 THEN
	 RAISE aproc_bad_count_err;
      END IF;
   END IF;

   location := '110';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   -- If changing concept_id or atom_id MRD expects these things
   -- to be tied together in connected_concepts
   location := '120';
   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      IF UPPER(field_name) like 'CONCEPT_ID%' OR
	 UPPER(field_name) like 'ATOM_ID%' THEN
	    connect_concepts(to_number(new_value),to_number(old_value),field_name);
      END IF;
   END IF;

   RETURN 0;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_change_field',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_change_field',location,30,
         row_id || ',' || table_name || ',' || field_name || ',' ||
         old_value || ',' || new_value || ',' || action_status ||
         ',' || retval);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_change_field',location,40,
         row_id || ',' || table_name || ',' || field_name || ',' ||
         old_value || ',' || new_value || ',' || action_status ||
         ',' || retval);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_change_field',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_change_field',location,1,
         row_id || ',' || table_name || ',' || field_name || ',' ||
         old_value || ',' || new_value || ',' || action_status ||
         ',' || SQLERRM);
      RETURN -1;

END aproc_change_field;

/* FUNCTION APROC_CHANGE_CONCEPT_ID ********************************************
 */
FUNCTION aproc_change_concept_id(
   atom_id		       IN INTEGER,
   old_concept_id	       IN INTEGER,
   new_concept_id	       IN INTEGER,
   action_status	       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   atomic_action_id 	       IN INTEGER := 0,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   atomic_id		       INTEGER;
   aa_timestamp 	       DATE;
   l_method		       VARCHAR2(50) := 'APROC_CHANGE_CONCEPT_ID';
BEGIN

   initialize_trace(l_method);

   SELECT SYSDATE INTO aa_timestamp FROM dual;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      location := '1';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      -- Ensure that the new concept_id actually exists
      location := '10';
      IF MEME_UTILITY.count_row_id
	 (MEME_CONSTANTS.TN_CONCEPT_STATUS,new_concept_id) != 1 THEN
	 error_code := 80; error_detail := new_concept_id;
	 RAISE aproc_exception;
      END IF;

      location := '15';
      INSERT INTO atomic_actions
       (atomic_action_id, molecule_id, action, table_name, row_id,
	old_value, new_value, authority, timestamp, status, action_field)
      VALUES
       (atomic_id, molecule_id, MEME_CONSTANTS.AA_MOVE,
	MEME_CONSTANTS.TN_CLASSES, atom_id, old_concept_id, new_concept_id,
	authority, aa_timestamp, action_status,
	LOWER(MEME_CONSTANTS.FN_CONCEPT_ID));

      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '1.1';
      atomic_id := atomic_action_id;

   END IF;

   location := '30';
   UPDATE classes
   SET concept_id = new_concept_id,
       authority = aproc_change_concept_id.authority,
       timestamp = aproc_change_concept_id.aa_timestamp,
       last_molecule_id = aproc_change_concept_id.molecule_id,
       last_atomic_action_id = aproc_change_concept_id.atomic_id
   WHERE atom_id = aproc_change_concept_id.atom_id
   AND concept_id = old_concept_id;

   IF SQL%ROWCOUNT !=1 THEN
      RAISE aproc_bad_count_err;
   END IF;

   location := '50';
   UPDATE attributes
   SET concept_id = new_concept_id
   WHERE atom_id = aproc_change_concept_id.atom_id
   AND concept_id = old_concept_id
   AND attribute_level != MEME_CONSTANTS.CONCEPT_LEVEL;

   location := '60';
   UPDATE relationships
   SET concept_id_1 = new_concept_id
   WHERE atom_id_1 = aproc_change_concept_id.atom_id
   AND concept_id_1 = old_concept_id
   AND relationship_level IN (MEME_CONSTANTS.SOURCE_LEVEL,MEME_CONSTANTS.PROCESSED_LEVEL);

   location := '70';
   UPDATE relationships
   SET concept_id_2 = new_concept_id
   WHERE atom_id_2 = aproc_change_concept_id.atom_id
   AND concept_id_2 = old_concept_id
   AND relationship_level IN (MEME_CONSTANTS.SOURCE_LEVEL,MEME_CONSTANTS.PROCESSED_LEVEL);

   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   -- Connect new_value and old_value in MRD mode
   location := '120';
   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      connect_concepts(old_concept_id,new_concept_id,MEME_CONSTANTS.FN_CONCEPT_ID);
   END IF;

   RETURN 0;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_change_concept_id',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_change_concept_id',location,30,
         atom_id || ',' ||  old_concept_id || ',' ||
         new_concept_id || ',' || action_status ||
         ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_change_concept_id',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_change_concept_id',location,1,
         atom_id || ',' ||  old_concept_id || ',' ||
         new_concept_id || ',' || action_status ||
         ',' || SQLERRM);
      RETURN -1;

END aproc_change_concept_id;

/* FUNCTION APROC_CHANGE_ATOM_ID ***********************************************
 */
FUNCTION aproc_change_atom_id(
   row_id		       IN INTEGER,
   table_name		       IN VARCHAR2,
   old_atom_id		       IN INTEGER,
   new_atom_id		       IN INTEGER,
   action_status	       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority 		       IN VARCHAR2,
   timestamp 		       IN DATE,
   atomic_action_id 	       IN INTEGER := 0,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   l_ui				VARCHAR2(50);
   atomic_id		       INTEGER;
   ct			       INTEGER;
   aa_timestamp 	       DATE;
   l_method		       VARCHAR2(50) := 'APROC_CHANGE_ATOM_ID';
BEGIN

   initialize_trace(l_method);

   SELECT SYSDATE INTO aa_timestamp FROM dual;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      location := '10';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '12';
      INSERT INTO atomic_actions
       (atomic_action_id, molecule_id, action, table_name, row_id,
	old_value, new_value, authority, timestamp, status, action_field)
      VALUES
       (atomic_id, molecule_id, MEME_CONSTANTS.AA_CHANGE_ATOM_ID, table_name,
	aproc_change_atom_id.row_id, old_atom_id, new_atom_id, authority,
	aa_timestamp, action_status, LOWER(MEME_CONSTANTS.FN_ATOM_ID));

      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '20';
      atomic_id := atomic_action_id;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_CLASSES OR
      UPPER(table_name) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
      location := '30';
      error_code := 25; error_detail := 'table_name='||table_name;
      RAISE aproc_exception;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
      location := '40';
      UPDATE relationships a
      SET (atom_id_1, concept_id_1, sg_id_1,
	   authority, timestamp, last_molecule_id,
	   last_atomic_action_id) =
 	(SELECT new_atom_id, concept_id,
	   DECODE(sg_type_1,'AUI',aui,sg_id_1),
	   aproc_change_atom_id.authority,
	   aproc_change_atom_id.timestamp,
	   aproc_change_atom_id.molecule_id,
	   aproc_change_atom_id.atomic_id
	 FROM classes WHERE atom_id = new_atom_id)
      WHERE relationship_id = aproc_change_atom_id.row_id
      AND (force = 'Y' OR atom_id_1 = old_atom_id);

      -- This part was removed for version 3.7.1
      -- AND (relationship_level = MEME_CONSTANTS.SOURCE_LEVEL OR
      --      relationship_level = MEME_CONSTANTS.PROCESSED_LEVEL);

      -- more than one row is an error
      location := '45';
      IF SQL%ROWCOUNT > 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      -- save rowcount
      ct := SQL%ROWCOUNT;

      location := '50';
      UPDATE relationships a
      SET (atom_id_2, concept_id_2, sg_id_2,
	   authority, timestamp, last_molecule_id,
	   last_atomic_action_id) =
 	(SELECT new_atom_id, concept_id,
	   DECODE(sg_type_2,'AUI',aui,sg_id_2),
	   aproc_change_atom_id.authority,
	   aproc_change_atom_id.timestamp,
	   aproc_change_atom_id.molecule_id,
	   aproc_change_atom_id.atomic_id
	 FROM classes WHERE atom_id = new_atom_id)
      WHERE relationship_id = aproc_change_atom_id.row_id
      AND (force = 'Y' OR atom_id_2 = old_atom_id);

      -- This part was removed for version 3.7.1
      -- AND (relationship_level = MEME_CONSTANTS.SOURCE_LEVEL OR
      --      relationship_level = MEME_CONSTANTS.PROCESSED_LEVEL);

      -- more than one row is an error
      location := '55';
      IF SQL%ROWCOUNT > 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      -- save rowcount
      ct := ct + SQL%ROWCOUNT;

      -- if neither update updated, error
      location := '59';
      IF ct = 0 THEN
	 error_code := 30;
	 error_detail :=
	    'atomic_action_id='||atomic_action_id||','||'ct='||ct;
	 RAISE aproc_exception;
      END IF;

      location := '59.2';
      l_ui := assign_rui(relationship_id => row_id);
      location := '59.3';
      UPDATE relationships
      SET rui = l_ui
      WHERE relationship_id = row_id
	AND nvl(rui,'null') != nvl(l_ui,'null');

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
      location := '60.1';
      UPDATE attributes
      SET (atom_id, concept_id, sg_id,
	   authority, timestamp, last_molecule_id,
	   last_atomic_action_id) =
 	(SELECT new_atom_id, concept_id,
	   DECODE(sg_type,'AUI',aui,sg_id),
	   aproc_change_atom_id.authority,
	   aproc_change_atom_id.timestamp,
	   aproc_change_atom_id.molecule_id,
	   aproc_change_atom_id.atomic_id
	 FROM classes WHERE atom_id = new_atom_id)
      WHERE attribute_id = aproc_change_atom_id.row_id
      AND (force = 'Y' OR atom_id = old_atom_id);

      -- This part was removed for version 3.7.1
      -- AND (attribute_level = MEME_CONSTANTS.SOURCE_LEVEL OR
      --      attribute_level = MEME_CONSTANTS.PROCESSED_LEVEL);

      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '60.1';
      l_ui := assign_atui (attribute_id => row_id);
      location := '60.2';
      UPDATE attributes SET atui = l_ui
      WHERE attribute_id = row_id 	
	AND nvl(atui,'null') != l_ui;

   END IF;

   location := '80';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   location := '90';
   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      connect_concepts(old_atom_id,new_atom_id,MEME_CONSTANTS.FN_ATOM_ID);
   END IF;

   RETURN 0;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_change_atom_id',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_change_atom_id',location,30,
         row_id || ',' ||  old_atom_id || ',' ||
         new_atom_id || ',' || action_status ||
         ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_change_atom_id',location,1,
         row_id || ',' ||  old_atom_id || ',' ||
         new_atom_id || ',' || action_status ||
         ',' || SQLERRM);
      RETURN -1;

END aproc_change_atom_id;

/* FUNCTION APROC_CHANGE_DEAD **************************************************
 */
FUNCTION aproc_change_dead(
   table_name		       IN VARCHAR2,
   row_id		       IN INTEGER,
   old_dead		       IN VARCHAR2,
   new_dead		       IN VARCHAR2,
   action_status	       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   atomic_action_id 	       IN INTEGER := 0,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval		       INTEGER;
   atomic_id		       INTEGER;
   aa_timestamp 	       DATE;
   l_method		       VARCHAR2(50) := 'APROC_CHANGE_DEAD';
BEGIN

   initialize_trace(l_method);

   SELECT SYSDATE INTO aa_timestamp FROM dual;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      location := '1';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '10';
      INSERT INTO atomic_actions
	(atomic_action_id, molecule_id, action, table_name, row_id,
	 old_value, new_value, authority, timestamp, status, action_field)
      VALUES
	(atomic_id, molecule_id, MEME_CONSTANTS.AA_DELETE, table_name, row_id,
	 old_dead, new_dead, authority, aa_timestamp, action_status,
	 LOWER(MEME_CONSTANTS.FN_DEAD));

      location := '20';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '1.1';
      atomic_id := atomic_action_id;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_CLASSES AND
      new_dead = MEME_CONSTANTS.YES THEN

      location := '31';
      retval := bury_classes ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '33';
      UPDATE dead_classes
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE atom_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '40';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS
      AND new_dead = MEME_CONSTANTS.YES THEN

      location := '51';
      retval := bury_relationships ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '52';
      UPDATE dead_relationships
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE relationship_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '53';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS
      AND new_dead = MEME_CONSTANTS.YES THEN

      location := '51.1';
      retval := bury_cxt_relationships ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '52.2';
      UPDATE dead_context_relationships
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE relationship_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '53.3';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_ATTRIBUTES
      AND new_dead = MEME_CONSTANTS.YES THEN

      location := '71';
      retval := bury_attributes ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '72';
      UPDATE dead_attributes
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE attribute_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '73';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_CONCEPT_STATUS
      AND new_dead = MEME_CONSTANTS.YES THEN

      location := '91';
      retval := bury_concept_status ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '92';
      UPDATE dead_concept_status
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE concept_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '100';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_CLASSES
      AND new_dead = MEME_CONSTANTS.NO THEN

      location := '111';
      retval := digup_classes ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '113';
      UPDATE classes
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE atom_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '120';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS
      AND new_dead = MEME_CONSTANTS.NO THEN

      location := '131';
      retval := digup_relationships ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '132';
      UPDATE relationships
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE relationship_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '133';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS
      AND new_dead = MEME_CONSTANTS.NO THEN

      location := '131';
      retval := digup_cxt_relationships ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '132';
      UPDATE context_relationships
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE relationship_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '133';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_ATTRIBUTES
      AND new_dead = MEME_CONSTANTS.NO THEN

      location := '151';
      retval := digup_attributes ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '152';
      UPDATE attributes
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE attribute_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '153';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_CONCEPT_STATUS
      AND new_dead = MEME_CONSTANTS.NO THEN

      location := '171';
      retval := digup_concept_status ( row_id );
      IF retval != 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      location := '172';
      UPDATE concept_status
      SET dead = aproc_change_dead.new_dead,
	  authority = aproc_change_dead.authority,
	  timestamp = aproc_change_dead.aa_timestamp,
	  last_molecule_id = aproc_change_dead.molecule_id,
	  last_atomic_action_id = aproc_change_dead.atomic_id
      WHERE concept_id = aproc_change_dead.row_id
      AND (force = 'Y' OR dead = aproc_change_dead.old_dead);

      location := '180';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;
   END IF;

   location := '190';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   RETURN 0;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_change_dead',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_change_dead',location,30,
         row_id || ',' || table_name || ',' ||
         old_dead || ',' || new_dead || ',' || action_status ||
         ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_change_dead',location,40,
         row_id || ',' || table_name || ',' ||
         old_dead || ',' || new_dead || ',' || action_status ||
         ',' || retval);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_change_dead',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_change_dead',location,1,
         row_id || ',' || table_name || ',' ||
         old_dead || ',' || new_dead || ',' || action_status ||
         ',' || SQLERRM);
      RETURN -1;

END aproc_change_dead;

/* FUNCTION APROC_CHANGE_ID ****************************************************
 */
FUNCTION aproc_change_id(
   table_name		       IN VARCHAR2,
   row_id		       IN INTEGER,
   old_concept_id	       IN INTEGER,
   new_concept_id	       IN INTEGER,
   action_status	       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   atomic_action_id 	       IN INTEGER := 0,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   atomic_id		       	INTEGER;
   ct			       	INTEGER;
   aa_timestamp 	 	DATE;
   l_ui			 	VARCHAR2(20);
   l_method		       	VARCHAR2(50) := 'APROC_CHANGE_ID';
BEGIN

   initialize_trace(l_method);

   SELECT SYSDATE INTO aa_timestamp FROM dual;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      location := '1';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '2';
      IF MEME_UTILITY.count_row_id
	 (MEME_CONSTANTS.TN_CONCEPT_STATUS,new_concept_id) != 1 THEN
	 error_code := 80; error_detail := new_concept_id;
	 RAISE aproc_exception;
      END IF;

      location := '15';
      INSERT INTO atomic_actions
	(atomic_action_id, molecule_id, action, table_name, row_id,
	 old_value, new_value, authority, timestamp, status, action_field)
      VALUES
	(atomic_id, molecule_id,MEME_CONSTANTS.AA_MOVE, table_name,
	 row_id, old_concept_id, new_concept_id, authority, aa_timestamp,
	 action_status, LOWER(MEME_CONSTANTS.FN_CONCEPT_ID));

      location := '20';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '1.1';
      atomic_id := atomic_action_id;
   END IF;

   IF UPPER(table_name) NOT IN
      (MEME_CONSTANTS.TN_ATTRIBUTES, MEME_CONSTANTS.TN_RELATIONSHIPS) THEN
      location := '25'; error_code := 25; error_code := table_name;
      RAISE aproc_exception;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
      location := '30.2';
      UPDATE attributes
      SET concept_id = aproc_change_id.new_concept_id,
	  sg_id = DECODE(sg_type,'CONCEPT_ID',to_char(new_concept_id), sg_id),
	  authority = aproc_change_id.authority,
	  timestamp = aproc_change_id.aa_timestamp,
	  last_atomic_action_id = aproc_change_id.atomic_id,
	  last_molecule_id = aproc_change_id.molecule_id
      WHERE attribute_id = aproc_change_id.row_id
      AND (force = 'Y' OR concept_id = aproc_change_id.old_concept_id)
      AND attribute_level = MEME_CONSTANTS.CONCEPT_LEVEL;

      location := '40.1';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '40.2';
      l_ui := assign_atui (attribute_id => row_id);
      location := '40.3';
      UPDATE attributes SET atui = l_ui
      WHERE attribute_id = row_id 	
	AND nvl(atui,'null') != l_ui;

   ELSIF UPPER(table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
      location := '50';
      UPDATE relationships
      SET concept_id_1 = aproc_change_id.new_concept_id,
	  sg_id_1 = DECODE(sg_type_1,'CONCEPT_ID',to_char(new_concept_id), sg_id_1),
	  authority = aproc_change_id.authority,
	  timestamp = aproc_change_id.aa_timestamp,
	  last_atomic_action_id = aproc_change_id.atomic_id,
	  last_molecule_id = aproc_change_id.molecule_id
      WHERE relationship_id = aproc_change_id.row_id
      AND concept_id_1 = aproc_change_id.old_concept_id
      AND relationship_level = MEME_CONSTANTS.CONCEPT_LEVEL;

      -- more than one row is an error
      location := '60';
      IF SQL%ROWCOUNT > 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      -- save rowcount
      ct := SQL%ROWCOUNT;

      location := '70';
      UPDATE relationships
      SET concept_id_2 = aproc_change_id.new_concept_id ,
	  sg_id_2 = DECODE(sg_type_2,'CONCEPT_ID',to_char(new_concept_id), sg_id_2),
	  authority = aproc_change_id.authority,
	  timestamp = aproc_change_id.aa_timestamp,
	  last_atomic_action_id = aproc_change_id.atomic_id,
	  last_molecule_id = aproc_change_id.molecule_id
      WHERE relationship_id = aproc_change_id.row_id
	AND concept_id_2 = aproc_change_id.old_concept_id
	AND relationship_level= MEME_CONSTANTS.CONCEPT_LEVEL;

      -- more than one row is an error
      location := '90';
      IF SQL%ROWCOUNT > 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      -- save rowcount
      ct := ct + SQL%ROWCOUNT;

      -- if neither update updated, error
      location := '100';
      IF ct = 0 THEN
	 error_code := 30;
	 error_detail :=
	    'atomic_action_id='||atomic_action_id||','||'ct='||ct;
	 RAISE aproc_exception;
      END IF;

      location := '90.2';
      l_ui := assign_rui(relationship_id => row_id);
      location := '90.3';
      UPDATE relationships
      SET rui = l_ui
      WHERE relationship_id = row_id
	AND nvl(rui,'null') != nvl(l_ui,'null');

   END IF;

   location := '110';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   -- Connect new_value and old_value in MRD mode
   location := '120';
   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      connect_concepts(old_concept_id,new_concept_id,MEME_CONSTANTS.FN_CONCEPT_ID);
   END IF;

   RETURN 0;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_change_id',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_change_id',location,30,
         row_id || ',' || table_name || ',' ||
         old_concept_id || ',' || new_concept_id || ',' ||
         action_status || ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_change_id',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_change_id',location,1,
         row_id || ',' || table_name || ',' ||
         old_concept_id || ',' || new_concept_id || ',' ||
         action_status || ',' || SQLERRM);
      RETURN -1;

END aproc_change_id;

/* FUNCTION APROC_CHANGE_STATUS ************************************************
 */
FUNCTION aproc_change_status(
   table_name		       IN VARCHAR2,
   row_id		       IN INTEGER,
   old_status		       IN VARCHAR2,
   new_status		       IN VARCHAR2,
   action_status	       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   atomic_action_id 	       IN INTEGER := 0,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval		       INTEGER;
   atomic_id		       INTEGER;
   ct			       INTEGER;
   aa_timestamp 	       DATE;
   l_method		       VARCHAR2(50) := 'APROC_CHANGE_STATUS';
BEGIN

   initialize_trace(l_method);

   SELECT SYSDATE INTO aa_timestamp FROM dual;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      -- Check that new status value is valid
      location := '1';
      IF MEME_RANKS.get_status_rank(table_name,new_status) = -1 THEN
	 error_code := 61; error_detail := new_status;
	 RAISE aproc_exception;
      END IF;

      location := '5';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '10';
      INSERT INTO atomic_actions
	(atomic_action_id, molecule_id, action, table_name, row_id, old_value,
	 new_value, authority, timestamp, status, action_field)
      VALUES
	(atomic_id, molecule_id, MEME_CONSTANTS.AA_CHANGE_STATUS, table_name,
	 row_id, old_status, new_status, authority, aa_timestamp, action_status,
	 LOWER(MEME_CONSTANTS.FN_STATUS));

      location := '20';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;
   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '1.1';
      atomic_id := atomic_action_id;
   END IF;

   location := '25';
   IF MEME_RANKS.get_status_rank (table_name,new_status) = -1 THEN
      error_code := 61; error_detail := new_status;
      RAISE aproc_exception;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_CLASSES THEN
      location := '30';
      UPDATE classes
      SET status = aproc_change_status.new_status,
	  timestamp = aproc_change_status.aa_timestamp,
	  authority = aproc_change_status.authority,
	  last_molecule_id = aproc_change_status.molecule_id,
	  last_atomic_action_id = aproc_change_status.atomic_id
      WHERE atom_id = aproc_change_status.row_id
      AND (force = 'Y' OR status = aproc_change_status.old_status);

      location := '40';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '50';
      retval := MEME_RANKS.set_atom_rank(row_id, LOWER(MEME_CONSTANTS.FN_STATUS));
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
      location := '70';
      UPDATE concept_status
      SET status = aproc_change_status.new_status,
	  timestamp = aproc_change_status.aa_timestamp,
	  authority = aproc_change_status.authority,
	  last_molecule_id = aproc_change_status.molecule_id,
	  last_atomic_action_id = aproc_change_status.atomic_id
      WHERE concept_id = aproc_change_status.row_id
      AND (force = 'Y' OR status = aproc_change_status.old_status);

      location := '80';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;
   END IF;


   IF UPPER(table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
      location := '100';
      UPDATE relationships
      SET status = aproc_change_status.new_status,
	  timestamp = aproc_change_status.aa_timestamp,
	  authority = aproc_change_status.authority,
	  last_molecule_id = aproc_change_status.molecule_id,
	  last_atomic_action_id = aproc_change_status.atomic_id
      WHERE relationship_id = aproc_change_status.row_id
      AND (force = 'Y' OR status = aproc_change_status.old_status);

      location := '110';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '120';
      retval := MEME_RANKS.set_relationship_rank(row_id,LOWER(MEME_CONSTANTS.FN_STATUS));
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
      location := '140';
      UPDATE attributes
      SET status = aproc_change_status.new_status,
	  timestamp = aproc_change_status.aa_timestamp,
	  authority = aproc_change_status.authority,
	  last_molecule_id = aproc_change_status.molecule_id,
	  last_atomic_action_id = aproc_change_status.atomic_id
      WHERE Attribute_ID = aproc_change_status.row_id
      AND (force = 'Y' OR status = aproc_change_status.old_status);

      location := '150';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '160';
      retval := MEME_RANKS.set_attribute_rank(row_id, LOWER(MEME_CONSTANTS.FN_STATUS));
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;
   END IF;

   location := '170';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id'||atomic_id;
      RAISE aproc_exception;
   END IF;

   RETURN 0;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_change_status',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_change_status',location,30,
         row_id || ',' || table_name || ',' ||
         old_status || ',' || new_status || ',' || action_status ||
         ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_change_status',location,40,
         row_id || ',' || table_name || ',' ||
         old_status || ',' || new_status || ',' || action_status ||
         ',' || retval);
      RETURN -1;
   WHEN aproc_invalid_field_err THEN
      meme_aprocs_error('aproc_change_status',location,61,
         new_status);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_change_status',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_change_status',location,1,
         row_id || ',' || table_name || ',' ||
         old_status || ',' || new_status || ',' || action_status ||
         ',' || SQLERRM);
      RETURN -1;
END aproc_change_status;

/* FUNCTION APROC_CHANGE_TBR ***************************************************
 */
FUNCTION aproc_change_tbr(
   table_name		       IN VARCHAR2,
   row_id		       IN INTEGER,
   old_tobereleased	       IN VARCHAR2,
   new_tobereleased	       IN VARCHAR2,
   action_status	       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   atomic_action_id 	       IN INTEGER := 0,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval		       INTEGER;
   atomic_id		       INTEGER;
   aa_timestamp 	       DATE;
   l_ui				VARCHAR2(20);
   l_method		       VARCHAR2(50) := 'APROC_CHANGE_TBR';
BEGIN

   initialize_trace(l_method);

   SELECT SYSDATE INTO aa_timestamp FROM dual;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      -- Check that new status value is valid
      location := '5';
      IF MEME_RANKS.get_tobereleased_rank(new_tobereleased) = -1 THEN
	 location := '0';
	 error_code := 65; error_detail := new_tobereleased;
	 RAISE aproc_exception;
      END IF;

      location := '1';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '10';
      INSERT INTO atomic_actions
	(atomic_action_id, molecule_id, action, table_name, row_id, old_value,
	 new_value, authority, timestamp, status, action_field)
      VALUES
	(atomic_id, molecule_id, MEME_CONSTANTS.AA_CHANGE_TOBERELEASED,
	 table_name, row_id, old_tobereleased, new_tobereleased, authority,
	 aa_timestamp, action_status, LOWER(MEME_CONSTANTS.FN_TBR));

      location := '20';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '1.1';
      atomic_id := atomic_action_id;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_CLASSES THEN
      IF new_tobereleased = MEME_CONSTANTS.NO AND
	 old_tobereleased != MEME_CONSTANTS.NO THEN

	 location := '30';
	 retval := bury_index ( row_id );
	 IF retval != 0 THEN
	    RAISE aproc_bad_retval_err;
	 END IF;
      ELSIF new_tobereleased != MEME_CONSTANTS.NO AND
	 old_tobereleased = MEME_CONSTANTS.NO THEN

	 location := '31';
	 retval := digup_index ( row_id );
	 IF retval != 0 THEN
	    RAISE aproc_bad_retval_err;
	 END IF;
      END IF;

      location := '32';
      UPDATE classes c
      SET tobereleased = aproc_change_tbr.new_tobereleased,
	  authority = aproc_change_tbr.authority,
	  timestamp = aproc_change_tbr.aa_timestamp,
	  last_molecule_id = aproc_change_tbr.molecule_id,
	  last_atomic_action_id = aproc_change_tbr.atomic_id
      WHERE c.atom_id = aproc_change_tbr.row_id
      AND (force = 'Y' OR tobereleased = aproc_change_tbr.old_tobereleased);

      location := '40';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      -- If an atom becomes releasable, then compute its AUI
      -- If it goes the other way, let the AUI stay
      IF old_tobereleased in ('N','n') AND new_tobereleased in ('Y','y') THEN
          location := '40.1';
          l_ui := assign_aui (atom_id => row_id);
      	  location := '40.2';
      	  UPDATE classes SET aui = l_ui
      	  WHERE atom_id = row_id 	
	    AND nvl(aui,'null') != nvl(l_ui,'null');
      END IF;

      location := '45';
      retval := MEME_RANKS.set_atom_rank(row_id, LOWER(MEME_CONSTANTS.FN_TBR));
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
      location := '50'; error_code := 1;
      RAISE aproc_exception;
   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
      location := '70';
      UPDATE relationships c
      SET tobereleased = aproc_change_tbr.new_tobereleased,
	  authority = aproc_change_tbr.authority,
	  timestamp = aproc_change_tbr.aa_timestamp,
	  last_molecule_id = aproc_change_tbr.molecule_id,
	  last_atomic_action_id = aproc_change_tbr.atomic_id
      WHERE c.relationship_id = aproc_change_tbr.row_id
      AND (force = 'Y' OR tobereleased = aproc_change_tbr.old_tobereleased);

      location := '80';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '90';
      retval := MEME_RANKS.set_relationship_rank(row_id,LOWER(MEME_CONSTANTS.FN_TBR));
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      -- If a relationship becomes releasable, then compute its RUI
      -- If it goes the other way, let the RUI stay
      IF old_tobereleased in ('N','n') AND new_tobereleased in ('Y','y') THEN
          location := '90.1';
          l_ui := assign_rui (relationship_id => row_id);
      	  location := '90.2';
      	  UPDATE relationships 
          SET (rui, sg_id_1, sg_type_1, sg_qualifier_1, 
	 	    sg_id_2, sg_type_2, sg_qualifier_2 ) =
	    (SELECT rui, sg_id_1, sg_type_1, sg_qualifier_1, 
	 	         sg_id_2, sg_type_2, sg_qualifier_2
	     FROM relationships_ui WHERE rui = l_ui)
      	  WHERE relationship_id = row_id 	
	    AND nvl(rui,'null') != nvl(l_ui,'null');
      END IF;

   END IF;

   IF UPPER(table_name) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
      location := '100';
      UPDATE attributes c
      SET tobereleased = aproc_change_tbr.new_tobereleased,
	  authority = aproc_change_tbr.authority,
	  timestamp = aproc_change_tbr.aa_timestamp,
	  last_molecule_id = aproc_change_tbr.molecule_id,
	  last_atomic_action_id = aproc_change_tbr.atomic_id
      WHERE c.attribute_id = aproc_change_tbr.row_id
      AND (force = 'Y' OR tobereleased = aproc_change_tbr.old_tobereleased);

      location := '110';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '120';
      retval := MEME_RANKS.set_attribute_rank(row_id, LOWER(MEME_CONSTANTS.FN_TBR));
      IF retval < 0 THEN
	 RAISE aproc_bad_retval_err;
      END IF;

      -- If an attribute becomes releasable, then compute its ATUI
      -- If it goes the other way, let the ATUI stay
      IF old_tobereleased in ('N','n') AND new_tobereleased in ('Y','y') THEN
          location := '120.1';
          l_ui := assign_atui (attribute_id => row_id);
      	  location := '120.2';
      	  UPDATE attributes SET (atui,sg_id,sg_type,sg_qualifier) =
	    (SELECT atui, sg_id, sg_type, sg_qualifier
	     FROM attributes_ui WHERE atui = l_ui)
      	  WHERE attribute_id = row_id 	
	    AND nvl(atui,'null') != l_ui;
      END IF;

   END IF;

   location := '130';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   RETURN 0;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_change_tbr',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_change_tbr',location,30,
         row_id || ',' || table_name || ',' ||
         old_tobereleased || ',' || new_tobereleased || ',' ||
         action_status ||  ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_change_tbr',location,40,
         row_id || ',' || table_name || ',' ||
         old_tobereleased || ',' || new_tobereleased || ',' ||
         action_status ||  ',' || retval);
      RETURN -1;
   WHEN aproc_invalid_field_err THEN
      meme_aprocs_error('aproc_change_tbr',location,65,
         new_tobereleased);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_change_tbr',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_change_tbr',location,1,
         row_id || ',' || table_name || ',' ||
         old_tobereleased || ',' || new_tobereleased || ',' ||
         action_status ||  ',' || SQLERRM);
      RETURN -1;

END aproc_change_tbr;


/* FUNCTION ASSIGN_ATUI *********************************************
 */
FUNCTION assign_atui (
      attribute_id		IN INTEGER
)
 RETURN VARCHAR2
IS
   l_att			attributes%ROWTYPE;
BEGIN

    -- Look up relevant attribute fields
    location := '0';
    SELECT
 	source,attribute_level,attribute_name,
	hashcode, sg_id, sg_type, sg_qualifier,
	concept_id, atom_id, atui, tobereleased, source_atui INTO 
 	l_att.source, l_att.attribute_level, l_att.attribute_name,
	l_att.hashcode, l_att.sg_id, l_att.sg_type, l_att.sg_qualifier,
	l_att.concept_id, l_att.atom_id, l_att.atui, l_att.tobereleased,
	l_att.source_atui
    FROM attributes WHERE attribute_id = assign_atui.attribute_id;

    -- Do not re-assign the same ATUI, 
    -- get a new one if we can't find one
    -- with matching fields
    location := '5'; 
    RETURN assign_atui(
	source => l_att.source,
	attribute_level => l_att.attribute_level,
	attribute_name => l_att.attribute_name,
	tobereleased => l_att.tobereleased,
	hashcode => l_att.hashcode,
	sg_id => l_att.sg_id,
	sg_type => l_att.sg_type,
	sg_qualifier => l_att.sg_qualifier,
	source_atui => l_att.source_atui,
	concept_id => l_att.concept_id,
	atom_id => l_att.atom_id,
	atui => null);

EXCEPTION

   WHEN others THEN
       meme_aprocs_error('assign_atui',location,30,
         'attribute_id='||attribute_id || ', ' ||
	SQLERRM);
      RAISE aproc_exception;

END assign_atui;

/* FUNCTION ASSIGN_ATUI *********************************************
 */
FUNCTION assign_atui (
      source  			IN VARCHAR2,
      attribute_level		IN VARCHAR2,
      attribute_name		IN VARCHAR2,
      tobereleased		IN VARCHAR2,
      hashcode			IN VARCHAR2,
      sg_id			IN OUT VARCHAR2,
      sg_type			IN OUT VARCHAR2,
      sg_qualifier		IN OUT VARCHAR2,
      source_atui		IN VARCHAR2,
      concept_id		IN INTEGER,
      atom_id			IN INTEGER,
      atui			IN VARCHAR2 := NULL
)
 RETURN VARCHAR2
IS
   l_source		VARCHAR2(20);
   l_root_source	VARCHAR2(20);
   l_atui		VARCHAR2(20);
   l_atui_ct		INTEGER;

   l_atui_prefix	VARCHAR2(10);
   l_atui_length	INTEGER;
BEGIN

    location := '0';
    -- Unreleasable attributes do not get ATUIs
    IF tobereleased in ('N','n') THEN
	RETURN null;
    END IF;

    location := '2';
    -- Concept level attributes get MTH source always
    IF attribute_level = 'C' OR source like 'E-%'THEN
	l_source := 'MTH';
    ELSE
	l_source := source;
    END IF;

    location := '5';
    error_Detail := 'Missing source.';
    SELECT min(b.stripped_source) into l_root_source
    FROM source_rank a, source_rank b
    WHERE a.source = assign_atui.l_source
      AND a.normalized_source = b.source;

    -- not needed because of location 2
    /****
    IF attribute_name = 'SEMANTIC_TYPE' AND
	attribute_level = 'C' THEN
	l_root_source := 'MTH';
    ELSIF l_root_source IS NULL THEN
	location := '.5';
	RAISE aproc_exception;
    END IF;
    ****/

    -- IF sg_id is null, it has never been
    -- assigned and we know we are inserting 
    -- a new attribute
    IF NVL(sg_id,'AUI') = 'AUI' OR attribute_level = 'C' OR
	(attributE_level = 'S' AND sg_id = 'CONCEPT_ID') THEN
	-- When inserting a new attribute
	-- which is the case if both l_atui and atui 
	-- are null, we need to clean up
	-- the sg_id, sg_type, and sg_qualifier values
	location := '10';
   	IF attribute_level = 'C' THEN
            location := '10.2';
	    sg_id := to_char(concept_id);
	    sg_type := 'CONCEPT_ID';
	    sg_qualifier := null;
   	ELSIF attribute_level = 'S' THEN
            location := '10.31';
	    SELECT aui INTO sg_id
	    FROM classes WHERE atom_id = assign_atui.atom_id;
            location := '10.32';
	    sg_type := 'AUI';
	    sg_qualifier := null;
   	END IF;
    END IF;

    location := '5'; 
    SELECT min(atui) INTO l_atui
    FROM attributes_ui
    WHERE root_source = l_root_source
     AND attribute_name = assign_atui.attribute_name
     AND attribute_level = assign_atui.attribute_level
     AND hashcode = assign_atui.hashcode
     AND sg_id = assign_atui.sg_id
     AND sg_type = assign_atui.sg_type
     AND NVL(sg_qualifier,'null') = NVL(assign_atui.sg_qualifier,'null')
     AND NVL(source_atui,'null') = NVL(assign_atui.source_atui,'null');

    -- not found, insert new ui row
    IF l_atui IS NULL THEN

	-- If atui was passed in, use that one
	-- instead of making a new one
	-- that way same ATUI value will
	-- exist for multiple sets of things
	-- this makes ATUI not-unique in attributes_ui
	IF atui IS NOT NULL THEN
	    l_atui := atui;
 	ELSE

  	    -- Get next ATUI
            location := '20'; 
	    UPDATE max_tab SET max_id = max_id + 1
	    WHERE table_name = 'ATUI';
            location := '20.1';
	    SELECT value INTO l_atui_prefix FROM code_map
	    WHERE type = 'ui_prefix' AND code = 'ATUI';
 
            location := '20.2'; 
	    SELECT to_number(value) INTO l_atui_length FROM code_map
	    WHERE type = 'ui_length' AND code = 'ATUI';

            location := '20.3'; 
	    SELECT l_atui_prefix|| LPAD(max_id,l_atui_length,0) INTO l_atui FROM max_Tab
	    WHERE table_name = 'ATUI';
 	END IF;

	-- Add new attributes_ui row
        location := '30'; 
	INSERT INTO attributes_ui
	 (atui, root_source, attribute_level, attribute_name,
	  hashcode, sg_id, sg_type, sg_qualifier, source_atui)
      	VALUES (l_atui, l_root_source, attribute_level, attribute_name,
		hashcode, sg_id, sg_type, sg_qualifier, source_atui);

    END IF;

    -- Return the ATUI
    RETURN l_atui;

EXCEPTION

   WHEN others THEN
       meme_aprocs_error('assign_atui',location,30,
         'source='||source || ', ' ||
         'attribute_name='||attribute_name || ', ' ||
         'attribute_level='||attribute_level || ', ' ||
         'sg_id='||sg_id || ', ' ||
         'sg_type='||sg_type || ', ' ||
         'sg_qualifier='||sg_qualifier || ', ' ||
         'source_atui='||source_atui || ', ' ||
	SQLERRM);
      RAISE aproc_exception;

END assign_atui;

/* FUNCTION APROC_INSERT_ATTRIBUTE *********************************************
 */
FUNCTION aproc_insert_attribute(
   source_attribute_id	       	IN INTEGER,
   origin		       	IN VARCHAR2,
   molecule_id		       	IN INTEGER,
   authority		       	IN VARCHAR2,
   timestamp		       	IN DATE,
   l_level		       	IN VARCHAR2,
   l_concept_id 	       	IN INTEGER,
   l_atom_id		       	IN INTEGER,
   l_attribute_name	       	IN VARCHAR2,
   l_source		       	IN VARCHAR2,
   l_status		       	IN VARCHAR2,
   l_attribute_value	       	IN VARCHAR2,
   l_generated		       	IN VARCHAR2,
   l_released		       	IN VARCHAR2,
   l_tobereleased	       	IN VARCHAR2,
   l_suppressible	       	IN VARCHAR2,
   atomic_action_id 	       	IN INTEGER := 0,
   attribute_id		       	IN INTEGER,
   l_sg_id 	               	IN VARCHAR2 := '',
   l_sg_type 	               	IN VARCHAR2 := '',
   l_sg_qualifier		IN VARCHAR2 := '',
   l_source_atui		IN VARCHAR2 := '',
   l_hashcode			IN VARCHAR2 := null
)
RETURN INTEGER
IS
   new_id		       	INTEGER;
   retval		       	INTEGER;
   atomic_id		       	INTEGER;
   l_att			attributes%ROWTYPE;
   aa_timestamp 	    	DATE;
   l_method		 	VARCHAR2(50) := 'APROC_INSERT_ATTRIBUTE';

   l_sg_meme_data_type		VARCHAR2(10);
   l_sg_meme_id			NUMBER;

BEGIN

   initialize_trace(l_method);

   aa_timestamp := timestamp;
   l_att.concept_id := l_concept_id;

   error_code := 0;
   IF MEME_RANKS.get_level_rank
      (MEME_CONSTANTS.TN_ATTRIBUTES,l_level) = -1 THEN
      error_code := 60; error_detail := l_level;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_status_rank
      (MEME_CONSTANTS.TN_ATTRIBUTES,l_status) = -1 THEN
      error_code := 61; error_detail := l_status;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_level_status_rank
      (MEME_CONSTANTS.TN_ATTRIBUTES,l_level,l_status) = -1 THEN
      error_code := 62; error_detail := l_level||','||l_status;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_generated_rank(l_generated) = -1 THEN
      error_code := 63; error_detail := l_generated;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_released_rank(l_released) = -1 THEN
      error_code := 64; error_detail := l_released;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_tobereleased_rank(l_tobereleased) = -1 THEN
      error_code := 65; error_detail := l_tobereleased;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_suppressible_rank(l_suppressible) = -1 THEN
      error_code := 66; error_detail := l_suppressible;
      RAISE aproc_exception;
   END IF;

   -- Make sure concept exists if CONCEPT level
   IF l_level = MEME_CONSTANTS.CONCEPT_LEVEL THEN
      IF MEME_UTILITY.count_row_id
	 (MEME_CONSTANTS.TN_CONCEPT_STATUS, l_concept_id) != 1 THEN
	 error_code := 80; error_detail := l_concept_id;
	 RAISE aproc_exception;
      END IF;
   END IF;

   -- Verify source
   l_att.source_rank := MEME_RANKS.get_source_authority_rank(l_source, authority);
   IF l_att.source_rank = -1 THEN
      error_code := 67; error_detail := l_source || ', ' || authority;
      RAISE aproc_exception;
   END IF;

   -- If SEMANTIC_TYPE verify that the l_attribute_value is valid.
   IF l_attribute_name = 'SEMANTIC_TYPE' THEN

      SELECT count(*) INTO retval
      FROM semantic_types
      WHERE semantic_type = l_attribute_value;

      IF retval != 1 THEN
	error_code := 72; error_detail := l_attribute_value;
        RAISE aproc_exception;
      END IF;

   END IF;


   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      location := '60.1';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '60.2';
      new_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATTRIBUTES);

      location := '60.3';

      INSERT INTO atomic_actions
	(atomic_action_id, molecule_id, action, table_name, row_id, old_value,
	 new_value, authority, timestamp, action_field, status)
      VALUES
	(atomic_id, molecule_id, MEME_CONSTANTS.AA_INSERT,
	 MEME_CONSTANTS.TN_ATTRIBUTES, new_id, MEME_CONSTANTS.YES,
	 MEME_CONSTANTS.NO, authority, aa_timestamp,
	 LOWER(MEME_CONSTANTS.FN_DEAD), 'R');

      location := '60';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '70';
      atomic_id := atomic_action_id;
      new_id := attribute_id;
   END IF;

   IF l_level = MEME_CONSTANTS.SOURCE_LEVEL OR
      l_level = MEME_CONSTANTS.PROCESSED_LEVEL THEN

      location := '100';
      SELECT concept_id INTO l_att.concept_id FROM classes
      WHERE atom_id = l_atom_id;

      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      l_sg_meme_data_type := 'C';
      l_sg_meme_id := l_atom_id;

   ELSE

      l_sg_meme_data_type := 'CS';
      l_sg_meme_id := l_concept_id;


   END IF;

   -- assign hashcode
   location := '110.1';
   IF l_hashcode IS NULL THEN
        l_att.hashcode := MEME_UTILITY.md5(l_attribute_value);
   ELSE
  	l_att.hashcode := l_hashcode;
   END IF;

   -- assign atui.  The procedure cleans up the
   -- sg_id, sg_type, and sg_qualifier values, so
   -- we have to make writable copies of the varaibles.
   location := '110.2';
   l_att.sg_id := l_sg_id;
   l_att.sg_type := l_sg_type;
   l_att.sg_qualifier := l_sg_qualifier;
   location := '110.3';
   l_att.atui := assign_atui (
	source => l_source,
	attribute_level => l_level,
	attribute_name => l_attribute_name,
	tobereleased => l_tobereleased,
	hashcode => l_att.hashcode,
	sg_id => l_att.sg_id,
	sg_type => l_att.sg_type,
	sg_qualifier => l_att.sg_qualifier,
	source_atui => l_att.source_atui,
	concept_id => l_concept_id,
	atom_id => l_atom_id);
   location := '110.4';

   location := '120';
   INSERT INTO attributes
    (attribute_level, atom_id, concept_id, attribute_id, attribute_name,
     attribute_value, generated_status, source, suppressible,
     dead, status, authority, timestamp, version_id, released,
     tobereleased, source_rank, rank, insertion_date, atui, hashcode,
     last_molecule_id, last_atomic_action_id, sg_id, sg_type, sg_qualifier,
     sg_meme_data_type, sg_meme_id,
     source_atui)
   VALUES
    (l_level, l_atom_id, l_att.concept_id, new_id, l_attribute_name,
     l_attribute_value, l_generated, l_source, l_suppressible,
     MEME_CONSTANTS.NO, l_status, authority, aa_timestamp, 0, l_released,
     l_tobereleased, l_att.source_rank, 0, aa_timestamp, l_att.atui, l_att.hashcode,
     molecule_id, atomic_id, l_att.sg_id, l_att.sg_type, l_att.sg_qualifier,
     l_sg_meme_data_type, l_sg_meme_id,
     l_source_atui);

   location := '130';
   IF SQL%ROWCOUNT != 1 THEN
      RAISE aproc_bad_count_err;
   END IF;

   location := '140';
   retval := MEME_RANKS.set_attribute_rank(new_id);
   IF retval < 0 THEN
      RAISE aproc_bad_retval_err;
   END IF;

   IF source_attribute_id > 0 THEN
      location := '150';
      INSERT INTO source_id_map
       (local_row_id, table_name, origin, source,source_row_id)
      VALUES
       (new_id, MEME_CONSTANTS.TN_ATTRIBUTES, origin, l_source,
	source_attribute_id);

      location := '160';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;
   END IF;

   location := '170';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   RETURN new_id;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_insert_attribute',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_insert_attribute',location,30,
         source_attribute_id || ',' || l_concept_id || ',' ||
         l_atom_id || ',' || new_id || ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_insert_attribute',location,40,
         source_attribute_id || ',' || l_concept_id || ',' ||
         l_atom_id || ',' || new_id || ',' || retval);
      RETURN -1;
   WHEN aproc_invalid_field_err THEN
      meme_aprocs_error('aproc_insert_attribute',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_insert_attribute',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;

END aproc_insert_attribute;

/* FUNCTION ASSIGN_AUI *********************************************
 */
FUNCTION assign_aui (
      atom_id			IN INTEGER
)
RETURN VARCHAR2
IS
   l_source		VARCHAR2(40);
   l_tty		VARCHAR2(20);
   l_code		VARCHAR2(50);
   l_sui		VARCHAR2(20);
   l_source_aui		VARCHAR2(50);
   l_source_cui		VARCHAR2(50);
   l_source_dui		VARCHAR2(50);
   l_aui		VARCHAR2(20);
   l_tbr		VARCHAR2(20);
   l_aui_ct		INTEGER;

BEGIN

   location := '0';
   SELECT source, tty, code, sui, source_aui, source_cui, source_dui,tobereleased INTO
	l_source, l_tty, l_code, l_sui, l_source_aui, l_source_cui, l_source_dui, l_tbr
   FROM classes a
   WHERE atom_id = assign_aui.atom_id;

   location := '5';
   RETURN assign_aui (
	source => l_source,
	tty => l_tty,
	code => l_code,
	sui => l_sui,
	source_aui => l_source_aui,
	source_cui => l_source_cui,
	source_dui => l_source_dui,
	tobereleased => l_tbr);

EXCEPTION

   WHEN others THEN
       meme_aprocs_error('assign_aui',location,30,
         'source='||l_source || ', ' ||
         'tty='||l_tty || ', ' ||
         'code='||l_code || ', ' ||
         'sui='||l_sui || ', ' ||
         'source_aui='||l_source_aui || ', ' ||
         'source_cui='||l_source_cui || ', ' ||
         'source_dui='||l_source_dui || ', ' ||
         'tobereleased='||l_tbr || ', ' ||
	SQLERRM);
      RAISE aproc_exception;

END assign_aui;

/* FUNCTION ASSIGN_AUI *********************************************
 */
FUNCTION assign_aui (
      source  			IN VARCHAR2,
      tty			IN VARCHAR2,
      code			IN VARCHAR2,
      sui			IN VARCHAR2,
      source_aui		IN VARCHAR2,
      source_cui		IN VARCHAR2,
      source_dui		IN VARCHAR2,
      tobereleased		IN VARCHAR2
)
RETURN VARCHAR2
IS
   l_root_source	VARCHAR2(20);
   l_aui		VARCHAR2(20);
   l_aui_ct		INTEGER;
   l_code		VARCHAR2(50);

   l_aui_prefix	VARCHAR2(10);
   l_aui_length	INTEGER;

BEGIN

    location := '0';

    -- Unreleasable atoms get null AUIs
    IF tobereleased in ('N','n') THEN
	RETURN null;
    END IF;

    error_Detail := 'Missing source.';
    SELECT min(b.stripped_source) into l_root_source
    FROM source_rank a, source_rank b
    WHERE a.source = assign_aui.source
      AND a.normalized_source = b.source;

    l_code := code;
    -- No longer used, 
    -- RXNORM codes should be viewed as null
    --IF source = 'NLM02' THEN
    --	l_code := '';
    --ELSE
    --	l_code := code;
    --END IF;

    location := '5'; 
    SELECT min(aui) INTO l_aui
    FROM atoms_ui
    WHERE stripped_source = l_root_source
     AND tty = assign_aui.tty
     AND nvl(code,'null') = nvl(l_code,'null')
     AND sui = assign_aui.sui
     AND nvl(source_aui,'null') = nvl(assign_aui.source_aui,'null')
     AND nvl(source_cui,'null') = nvl(assign_aui.source_cui,'null')
     AND nvl(source_dui,'null') = nvl(assign_aui.source_dui,'null');

    -- NOT FOUND, get new one
    IF l_aui IS NULL THEN

	-- Get next AUI
        location := '10'; 
	UPDATE max_tab SET max_id = max_id + 1
	WHERE table_name = 'AUI';
        location := '10.1';
	SELECT value INTO l_aui_prefix FROM code_map
	WHERE type = 'ui_prefix' AND code = 'AUI';

        location := '10.2'; 
	SELECT to_number(value) INTO l_aui_length FROM code_map
        WHERE type = 'ui_length' AND code = 'AUI';
 
        location := '10.3'; 
	SELECT l_aui_prefix || LPAD(max_id,l_aui_length,0) INTO l_aui FROM max_Tab
	WHERE table_name = 'AUI';

	-- Add new atoms_ui row
        location := '20'; 
	INSERT INTO atoms_ui
	 (aui, sui, stripped_source, tty, code, source_aui, source_cui, source_dui)
      	VALUES (l_aui, sui, l_root_source, tty, l_code, source_aui, source_cui, source_dui);

    END IF;

    -- Return the AUI
    RETURN l_aui;

EXCEPTION

   WHEN others THEN
       meme_aprocs_error('assign_aui',location,30,
         'source='||source || ', ' ||
         'tty='||tty || ', ' ||
         'code='||code || ', ' ||
         'sui='||sui || ', ' ||
         'source_aui='||source_aui || ', ' ||
         'source_cui='||source_cui || ', ' ||
         'source_dui='||source_dui || ', ' ||
         'tobereleased='||tobereleased || ', ' ||
	SQLERRM);
      RAISE aproc_exception;

END assign_aui;

/* FUNCTION APROC_INSERT_CLASSES ***********************************************
 */
FUNCTION aproc_insert_classes(
   source_atom_id	       IN INTEGER,
   origin		       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   l_atom_name		       IN VARCHAR2,
   l_source		       IN VARCHAR2,
   l_termgroup		       IN VARCHAR2,
   l_code		       IN VARCHAR2,
   l_aui		       IN VARCHAR2 := NULL,
   l_sui		       IN VARCHAR2,
   l_isui		       IN VARCHAR2,
   l_lui		       IN VARCHAR2,
   l_generated		       IN VARCHAR2,
   l_last_release_cui	       IN VARCHAR2,
   l_last_assigned_cui	       IN VARCHAR2,
   l_status		       IN VARCHAR2,
   l_concept_id 	       IN INTEGER,
   l_tobereleased	       IN VARCHAR2,
   l_released		       IN VARCHAR2,
   l_last_release_rank	       IN INTEGER,
   l_suppressible	       IN VARCHAR2,
   l_source_aui		       IN VARCHAR2 := NULL,
   l_source_cui		       IN VARCHAR2 := NULL,
   l_source_dui		       IN VARCHAR2 := NULL,
   atomic_action_id 	       IN INTEGER := 0,
   atom_id 		       IN INTEGER
) RETURN INTEGER
IS
   retval		       	INTEGER;
   new_id		       	INTEGER;
   atomic_id		       	INTEGER;
   l_atom			classes%ROWTYPE;
   aa_timestamp			DATE;
   l_method			VARCHAR2(50) := 'APROC_INSERT_CLASSES';
BEGIN

   initialize_trace(l_method);

   aa_timestamp := timestamp;

   location := '0';
   IF MEME_RANKS.get_status_rank(MEME_CONSTANTS.TN_CLASSES,l_status) = -1 THEN
      error_code := 61; error_detail := l_status;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_generated_rank(l_generated) = -1 THEN
      error_code := 63; error_detail := l_generated;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_released_rank(l_released) = -1 THEN
      error_code := 64; error_detail := l_released;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_tobereleased_rank(l_tobereleased) = -1 THEN
      error_code := 65; error_detail := l_tobereleased;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_suppressible_rank(l_suppressible) = -1 THEN
      error_code := 66; error_detail := l_suppressible;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_source_rank(l_source) = -1 THEN
      error_code := 68; error_detail := l_source;
      RAISE aproc_exception;
   END IF;

   l_atom.termgroup_rank := MEME_RANKS.get_termgroup_rank(l_termgroup);
   IF l_atom.termgroup_rank = -1 THEN
      error_code := 69; error_detail := l_termgroup;
      RAISE aproc_exception;
   END IF;

   -- Verify concept id exists
   IF MEME_UTILITY.count_row_id
      (MEME_CONSTANTS.TN_CONCEPT_STATUS,l_concept_id) != 1 THEN
      error_detail := to_char(l_concept_id); error_code := 80;
      RAISE aproc_exception;
   END IF;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      location := '60.1';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '60.2';
      new_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMS);

      location := '60.3';
      INSERT INTO atomic_actions
	(atomic_action_id, molecule_id, action, table_name, row_id,
	 old_value, new_value, authority, timestamp, action_field, status)
      VALUES
     	(atomic_id, molecule_id, MEME_CONSTANTS.AA_INSERT,
     	 MEME_CONSTANTS.TN_CLASSES, new_id, MEME_CONSTANTS.YES,
	 MEME_CONSTANTS.NO, authority, aa_timestamp,
	 LOWER(MEME_CONSTANTS.FN_DEAD), 'R');

      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '1.1';
      atomic_id := atomic_action_id;
      new_id := atom_id;
   END IF;

   location := '70.1';
   l_atom.tty := substr(l_termgroup,instr(l_termgroup,'/')+1);
   location := '70.2';
   l_atom.aui := assign_aui(
	source => l_source,
	tty => l_atom.tty,
	code => l_code,
	sui => l_sui,
	source_aui => l_source_aui,
	source_cui => l_source_cui,
	source_dui => l_source_dui,
	tobereleased => l_tobereleased);

   location := '80';
   INSERT INTO atoms (atom_id, atom_name, version_id)
   VALUES (new_id, l_atom_name, 0);

   IF SQL%ROWCOUNT != 1 THEN
      RAISE aproc_bad_count_err;
   END IF;

   location := '90';
   INSERT INTO classes
    (atom_id, source, termgroup, code, aui, sui, isui, lui,
     generated_status, dead, concept_id, status, authority, timestamp,
     last_release_cui, last_assigned_cui,version_id, tobereleased,
     released, termgroup_rank, rank, insertion_date, suppressible,
     last_release_rank, last_molecule_id, last_atomic_action_id, tty,
     source_cui, source_aui, source_dui, language)
   VALUES
    (new_id, l_source, l_termgroup, l_code, l_atom.aui, l_sui, l_isui, l_lui,
     l_generated, MEME_CONSTANTS.NO, l_concept_id, l_status, authority, aa_timestamp,
     l_last_release_cui, l_last_assigned_cui, 0, l_tobereleased,
     l_released, l_atom.termgroup_rank, 0, aa_timestamp,
     l_suppressible, l_last_release_rank, molecule_id, atomic_id, l_atom.tty,
     l_source_cui, l_source_aui, l_source_dui, 'ENG');

   IF SQL%ROWCOUNT != 1 THEN
      RAISE aproc_bad_count_err;
   END IF;

   location := '110';
   retval := MEME_RANKS.set_atom_rank(new_id);
   IF retval < 0 THEN
      RAISE aproc_bad_retval_err;
   END IF;

   IF source_atom_id > 0 THEN
      location := '140';
      INSERT INTO source_id_map
       (local_row_id, table_name, origin, source, source_row_id)
      VALUES
       (new_id, MEME_CONSTANTS.TN_CLASSES, origin, l_source, source_atom_id);

      location := '150';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;
   END IF;

   location := '160';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   RETURN new_id;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_insert_class',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_insert_class',location,30,
         source_atom_id || ',' || l_concept_id || ',' ||
         new_id || ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_insert_class',location,40,
         source_atom_id || ',' || l_concept_id || ',' ||
         new_id || ',' || retval);
      RETURN -1;
   WHEN aproc_invalid_field_err THEN
      meme_aprocs_error('aproc_insert_class',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_insert_classes',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_insert_class',location,1,
         source_atom_id || ',' || l_concept_id || ',' ||
         new_id || ',' || SQLERRM);
      RETURN -1;

END aproc_insert_classes;

/* FUNCTION APROC_INSERT_CS ****************************************************
 */
FUNCTION aproc_insert_cs(
   source_concept_id	       IN INTEGER,
   origin		       IN VARCHAR2,
   source		       IN VARCHAR2,
   status		       IN VARCHAR2,
   tobereleased 	       IN VARCHAR2,
   released		       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   atomic_action_id 	       IN INTEGER := 0,
   concept_id 		       IN INTEGER
)
RETURN INTEGER
IS
   retval		       INTEGER;
   new_id		       INTEGER;
   atomic_id		       INTEGER;
   l_rank		       INTEGER := 0;
   aa_timestamp 	       DATE;
   l_method		       VARCHAR2(50) := 'APROC_INSERT_CS';
BEGIN

   initialize_trace(l_method);

   aa_timestamp := timestamp;

   location := '0';
   IF MEME_RANKS.get_status_rank
      (MEME_CONSTANTS.TN_CONCEPT_STATUS, status) = -1 THEN
      error_code := 61; error_detail := status;
      RAISE aproc_exception;
   ELSIF source is not null THEN
      IF MEME_RANKS.get_source_authority_rank(source,authority) = -1 THEN
	 error_code := 67; error_detail := authority || ',' || source;
	 RAISE aproc_exception;
      END IF;
   END IF;

   l_rank := MEME_RANKS.get_tobereleased_rank(tobereleased);
   IF l_rank = -1 THEN
      error_code := 65; error_detail := tobereleased;
      RAISE aproc_exception;
   END IF;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      location := '60.1';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '60.2';
      new_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_CONCEPT_STATUS);

      location := '60.3';
      INSERT INTO atomic_actions
	(atomic_action_id, molecule_id, action, table_name, row_id,
	 old_value, new_value, authority, timestamp, action_field, status)
      VALUES
     	(atomic_id, molecule_id, MEME_CONSTANTS.AA_INSERT,
     	 MEME_CONSTANTS.TN_CONCEPT_STATUS, new_id, MEME_CONSTANTS.YES,
	 MEME_CONSTANTS.NO, authority, aa_timestamp,
	 LOWER(MEME_CONSTANTS.FN_DEAD), 'R');

      location := '60';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '1.1';
      atomic_id := atomic_action_id;
      new_id := concept_id;
   END IF;

   location := '80';
   INSERT INTO concept_status
    (concept_id, status, dead, authority, timestamp, version_id,
     tobereleased, released, rank, preferred_atom_id,
     editing_authority, editing_timestamp, approval_molecule_id,
     insertion_date, last_molecule_id, last_atomic_action_id)
   VALUES
    (new_id, status,MEME_CONSTANTS.NO, authority, aa_timestamp, 0, tobereleased,
     released, l_rank, 0, authority, aa_timestamp,0,aa_timestamp,
     molecule_id, atomic_id);

   location := '90';
   IF SQL%ROWCOUNT != 1 THEN
      RAISE aproc_bad_count_err;
   END IF;

   IF source_concept_id > 0 THEN
      location := '100';
      INSERT INTO source_id_map
       (local_row_ID, table_name, origin, source, source_row_ID)
      VALUES
       (new_id, MEME_CONSTANTS.TN_CONCEPT_STATUS, origin, source,
	source_concept_ID);

      location := '110';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;
   END IF;

   location := '120';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   RETURN new_id;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_insert_cs',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_insert_cs',location,30,
         source_concept_id || ',' || new_id || ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_insert_cs',location,40,
         source_concept_id || ',' || new_id || ',' || retval);
      RETURN -1;
   WHEN aproc_invalid_field_err THEN
      meme_aprocs_error('aproc_insert_cs',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_insert_cs',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_insert_cs',location,1,
      source_concept_id || ',' || new_id || ',' || SQLERRM);
      RETURN -1;

END aproc_insert_cs;

/* FUNCTION ASSIGN_RUI *********************************************
 */
FUNCTION assign_rui (
      relationship_id		IN INTEGER
)
 RETURN VARCHAR2
IS
   l_rui			VARCHAR2(20);
   l_rel			relationships%ROWTYPE;
BEGIN

    -- Look up relevant relationship fields and pass to assign_rui
    location := '0';
    SELECT
 	source, relationship_level, relationship_name, 
	relationship_attribute, sg_id_1, sg_type_1, sg_qualifier_1,
	sg_id_2, sg_type_2, sg_qualifier_2, atom_id_1, concept_id_1, 
	atom_id_2, concept_id_2, rui, tobereleased, source_rui INTO 
 	l_rel.source, l_rel.relationship_level, l_rel.relationship_name, 
	l_rel.relationship_attribute, 
	l_rel.sg_id_1, l_rel.sg_type_1, l_rel.sg_qualifier_1,
	l_rel.sg_id_2, l_rel.sg_type_2, l_rel.sg_qualifier_2,
	l_rel.atom_id_1, l_rel.concept_id_1, 
	l_rel.atom_id_2, l_rel.concept_id_2, l_rui, 
	l_rel.tobereleased, l_rel.source_rui
    FROM relationships WHERE relationship_id = assign_rui.relationship_id;

    -- Look up RUI for new set of fields
    -- Do NOT re-use RUI, instead assign new one
    -- If there is not a matching one
    location := '20'; 
    RETURN assign_rui(
	source => l_rel.source,
	relationship_level => l_rel.relationship_level,
	relationship_name => l_rel.relationship_name,
	relationship_attribute => l_rel.relationship_attribute,
	tobereleased => l_rel.tobereleased,
	sg_id_1 => l_rel.sg_id_1,
	sg_type_1 => l_rel.sg_type_1,
	sg_qualifier_1 => l_rel.sg_qualifier_1,
	sg_id_2 => l_rel.sg_id_2,
	sg_type_2 => l_rel.sg_type_2,
	sg_qualifier_2 => l_rel.sg_qualifier_2,
	concept_id_1 => l_rel.concept_id_1,
	concept_id_2 => l_rel.concept_id_2,
	atom_id_1 => l_rel.atom_id_1,
	atom_id_2 => l_rel.atom_id_2,
	source_rui => l_rel.source_rui,
	rui => null);
EXCEPTION

   WHEN others THEN
       meme_aprocs_error('assign_rui',location,30,
         'relationship_id='||relationship_id || ', ' ||
	SQLERRM);
      RAISE aproc_exception;

END assign_rui;

/* FUNCTION ASSIGN_RUI *********************************************
 * 
 * We implemented the logic that says C level relationships
 * have a null RELA unless the source is NLM03,MTHRELA. 
 * Also P level relationships are not assigned a RUI.
 *
 */
FUNCTION assign_rui (
      source  			IN VARCHAR2,
      relationship_level	IN VARCHAR2,
      relationship_name		IN VARCHAR2,
      relationship_attribute	IN VARCHAR2,
      tobereleased		IN VARCHAR2,
      sg_id_1			IN OUT VARCHAR2,
      sg_type_1			IN OUT VARCHAR2,
      sg_qualifier_1		IN OUT VARCHAR2,
      sg_id_2			IN OUT VARCHAR2,
      sg_type_2			IN OUT VARCHAR2,
      sg_qualifier_2		IN OUT VARCHAR2,
      concept_id_1		IN INTEGER,
      concept_id_2		IN INTEGER,
      atom_id_1			IN INTEGER,
      atom_id_2			IN INTEGER,
      source_rui		IN VARCHAR2,
      rui			IN VARCHAR2 := NULL
)
 RETURN VARCHAR2
IS
   l_root_source	VARCHAR2(20);
   l_source		VARCHAR2(20);
   l_rui		VARCHAR2(20);
   l_rui2		VARCHAR2(20);
   l_rui_ct		INTEGER;
   l_rela			VARCHAR2(100);

   l_rui_prefix	VARCHAR2(10);
   l_rui_length	INTEGER;
BEGIN

    location := '0';

    -- Unreleasable rels get null RUIs
    location := '1';
    IF relationship_level = 'P' OR tobereleased in ('N','n') THEN
	RETURN null;
    END IF;

    location := '2';
    l_source := source;
    -- All C level rels have source MTH unless they are NLM03 or MTHRELA
    IF relationship_level = 'C' THEN
	IF source not in ('NLM03','MTHRELA') THEN
	    l_source := 'MTH';
	END IF;
    END IF;

    error_Detail := 'Missing source.';
    location := '5';
    SELECT min(b.stripped_source) into l_root_source
    FROM source_rank a, source_rank b
    WHERE a.source = assign_rui.l_source
      AND a.normalized_source = b.source;

    location := '5.3';
    l_rela := relationship_attribute;

    -- Null RELA for C level MTH non MTHRELA rels
    location := '5.4';
    IF relationship_level = 'C' AND 
       l_root_source = 'MTH' AND
       l_source != 'MTHRELA' THEN
	l_rela := null;
    END IF;

    -- If sg_id_1 is null, it was never assigned
    -- and it means we are inserting a new
    -- relationship (without a mappable type)
    -- If relationship level is 'P' or 'S' and sg_type_1 = 'CONCEPT_ID', reassign
    IF NVL(sg_id_1,'AUI') = 'AUI' OR relationship_level = 'C' OR
	(relationship_level in ('P','S') AND sg_type_1 = 'CONCEPT_ID') THEN
	-- When inserting a new relationship
	-- which is the case if both l_rui and rui 
	-- are null, we need to clean up
	-- the sg_id_[12], sg_type_[12], and sg_qualifier_[12] values
	location := '10';
   	IF relationship_level = 'C' THEN
            location := '10.2';
	    sg_id_1 := to_char(concept_id_1);
	    sg_type_1 := 'CONCEPT_ID';
	    sg_qualifier_1 := null;
   	ELSIF relationship_level = 'S' THEN
            location := '10.31';
	    SELECT aui INTO sg_id_1
	    FROM classes WHERE atom_id = assign_rui.atom_id_1;
            location := '10.32';
	    sg_type_1 := 'AUI';
	    sg_qualifier_1 := null;
   	END IF;
    END IF;

    IF NVL(sg_id_2,'AUI') = 'AUI' OR relationship_level = 'C' OR
	(relationship_level in ('P','S') AND sg_type_2 = 'CONCEPT_ID') THEN
	location := '11';
   	IF relationship_level = 'C' THEN
            location := '11.2';
	    sg_id_2 := to_char(concept_id_2);
	    sg_type_2 := 'CONCEPT_ID';
	    sg_qualifier_2 := null;
   	ELSIF relationship_level = 'S' THEN
            location := '11.31';
	    SELECT aui INTO sg_id_2
	    FROM classes WHERE atom_id = assign_rui.atom_id_2;
            location := '11.32';
	    sg_type_2 := 'AUI';
	    sg_qualifier_2 := null;
   	END IF;
    END IF;


    location := '5'; 
    SELECT min(rui) INTO l_rui
    FROM relationships_ui
    WHERE root_source = l_root_source
     AND relationship_name = assign_rui.relationship_name
     AND NVL(relationship_attribute,'null') = NVL(l_rela,'null')
     AND relationship_level = assign_rui.relationship_level
     AND sg_id_1 = assign_rui.sg_id_1
     AND sg_type_1 = assign_rui.sg_type_1
     AND NVL(sg_qualifier_1,'null') = NVL(assign_rui.sg_qualifier_1,'null')
     AND sg_id_2 = assign_rui.sg_id_2
     AND sg_type_2 = assign_rui.sg_type_2
     AND NVL(source_rui,'null') = NVL(assign_rui.source_rui,'null')
     AND NVL(sg_qualifier_2,'null') = NVL(assign_rui.sg_qualifier_2,'null');

    -- not found, insert new ui row
    IF l_rui IS NULL THEN

	-- If rui was passed in, use that one
	-- instead of making a new one
	-- that way same RUI value will
	-- exist for multiple sets of things
	-- this makes RUI not-unique in relationships_ui
	IF rui IS NOT NULL THEN
	    l_rui := rui;
 	ELSE
  	    -- Get next RUI
            location := '10'; 
	    UPDATE max_tab SET max_id = max_id + 1
	    WHERE table_name = 'RUI';
            location := '10.11';
 	    SELECT value INTO l_rui_prefix FROM code_map
	    WHERE type = 'ui_prefix' AND code = 'RUI';

            location := '10.12'; 
	    SELECT to_number(value) INTO l_rui_length FROM code_map
	    WHERE type = 'ui_length' AND code = 'RUI';

            location := '10.2'; 
	    SELECT l_rui_prefix || LPAD(max_id,l_rui_length,0) INTO l_rui FROM max_tab
	    WHERE table_name = 'RUI';
            location := '10'; 
	    UPDATE max_tab SET max_id = max_id + 1
	    WHERE table_name = 'RUI';
            location := '10.2'; 
	    SELECT l_rui_prefix || LPAD(max_id,l_rui_length,0) INTO l_rui2 FROM max_tab
	    WHERE table_name = 'RUI';
 	END IF;

	-- Add new relationships_ui row
        location := '20'; 
	INSERT INTO relationships_ui
	       (rui, root_source, relationship_level, 
	        relationship_name, relationship_attribute,
		sg_id_1, sg_type_1, sg_qualifier_1,
		sg_id_2, sg_type_2, sg_qualifier_2, source_rui)
      	VALUES (l_rui, l_root_source, relationship_level, 
	        relationship_name, l_rela,
		sg_id_1, sg_type_1, sg_qualifier_1,
		sg_id_2, sg_type_2, sg_qualifier_2, source_rui);

	location := '40';
	INSERT INTO inverse_relationships_ui (rui, inverse_rui)
	VALUES (l_rui, l_rui2);

        location := '30'; 
	IF sg_id_1 != sg_id_2 OR sg_type_1 != sg_type_2 THEN
	  INSERT INTO relationships_ui
	       (rui, root_source, relationship_level, 
	        relationship_name, relationship_attribute,
		sg_id_1, sg_type_1, sg_qualifier_1,
		sg_id_2, sg_type_2, sg_qualifier_2, source_rui)
      	  SELECT l_rui2, l_root_source, relationship_level, 
	        inverse_name, inverse_rel_attribute,
		sg_id_2, sg_type_2, sg_qualifier_2,
		sg_id_1, sg_type_1, sg_qualifier_1, source_rui
 	  FROM inverse_relationships a, inverse_rel_attributes b
	  WHERE a.relationship_name = assign_rui.relationship_name
	    AND NVL(b.relationship_attribute,'null') =
	        NVL(l_rela,'null');

  	  location := '41';
   	  INSERT INTO inverse_relationships_ui (rui, inverse_rui)
	  VALUES (l_rui2, l_rui);
        END IF;

    END IF;

    -- Return the RUI
    RETURN l_rui;

EXCEPTION

   WHEN others THEN
       meme_aprocs_error('assign_rui',location,30,
         'source='||source || ', ' ||
         'relationship_level='||relationship_level || ', ' ||
         'relationship_name='||relationship_name || ', ' ||
         'relationship_attribute='||l_rela || ', ' ||
         'sg_id_1='||sg_id_1 || ', ' ||
         'sg_type_1='||sg_type_1 || ', ' ||
         'sg_qualifier_1='||sg_qualifier_1 || ', ' ||
         'sg_id_2='||sg_id_2 || ', ' ||
         'sg_type_2='||sg_type_2 || ', ' ||
         'sg_qualifier_2='||sg_qualifier_2 || ', ' ||
         'source_rui='||source_rui || ', ' ||
	SQLERRM);
      RAISE aproc_exception;

END assign_rui;

/* FUNCTION APROC_INSERT_REL ***************************************************
 */
FUNCTION aproc_insert_rel(
   source_rel_id	       IN INTEGER,
   origin		       IN VARCHAR2,
   molecule_id		       IN INTEGER,
   authority		       IN VARCHAR2,
   timestamp		       IN DATE,
   l_concept_id_1	       IN INTEGER,
   l_concept_id_2	       IN INTEGER,
   l_atom_id_1		       IN INTEGER,
   l_relationship_name	       IN VARCHAR2,
   l_relationship_attribute    IN VARCHAR2,
   l_atom_id_2		       IN INTEGER,
   l_source_of_label	       IN VARCHAR2,
   l_source		       IN VARCHAR2,
   l_status		       IN VARCHAR2,
   l_generated		       IN VARCHAR2,
   l_level		       IN VARCHAR2,
   l_released		       IN VARCHAR2,
   l_tobereleased	       IN VARCHAR2,
   l_suppressible	       IN VARCHAR2,
   atomic_action_id 	       IN INTEGER := 0,
   relationship_id 	       IN INTEGER,
   l_sg_id_1 	               IN VARCHAR2 := '',
   l_sg_type_1	               IN VARCHAR2 := '',
   l_sg_qualifier_1            IN VARCHAR2 := '',
   l_sg_id_2 	               IN VARCHAR2 := '',
   l_sg_type_2	               IN VARCHAR2 := '',
   l_sg_qualifier_2            IN VARCHAR2 := '',
   l_source_rui                IN VARCHAR2 := '',
   l_relationship_group        IN VARCHAR2 := ''
) 
RETURN INTEGER
IS
   retval		       	INTEGER;
   new_id		       	INTEGER;
   atomic_id		       	INTEGER;
   l_rel			relationships%ROWTYPE;
   aa_timestamp			DATE;
   l_method			VARCHAR2(50) := 'APROC_INSERT_REL';
   l_sg_meme_data_type_1        VARCHAR2(10);
   l_sg_meme_id_1		NUMBER;
   l_sg_meme_data_type_2        VARCHAR2(10);
   l_sg_meme_id_2		NUMBER;

BEGIN

   initialize_trace (l_method);

   aa_timestamp := timestamp;
   l_rel.concept_id_1 := l_concept_id_1;
   l_rel.concept_id_2 := l_concept_id_2;

   location := '0';
   IF MEME_RANKS.get_level_status_rank
      (MEME_CONSTANTS.TN_RELATIONSHIPS,l_level,l_status) = -1 THEN
      error_code := 61;  error_detail := l_level || ',' || l_status;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_generated_rank(l_generated) = -1 THEN
      error_code := 63; error_detail := l_generated;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_released_rank(l_released) = -1 THEN
      error_code := 64; error_detail := l_released;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_tobereleased_rank( l_tobereleased) = -1 THEN
      error_code := 65; error_detail := l_released;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_source_authority_rank(l_source, authority) = -1 THEN
      error_code := 67; error_detail := l_source || ',' || authority;
   ELSIF MEME_RANKS.get_relationship_name_rank(l_relationship_name) = -1 THEN
      error_code := 70; error_detail := l_relationship_name;
      RAISE aproc_exception;
   ELSIF MEME_RANKS.get_rel_attribute_rank(l_relationship_attribute) = -1 THEN
      error_code := 71; error_detail := l_relationship_attribute;
      RAISE aproc_exception;
   END IF;

   l_rel.source_rank := MEME_RANKS.get_source_authority_rank (
	l_source_of_label,authority );
   IF l_rel.source_rank = -1 THEN 
      error_code := 67; error_detail := l_source_of_label || ',' || authority;
      RAISE aproc_exception;
   END IF;

   IF l_level = MEME_CONSTANTS.CONCEPT_LEVEL THEN
      IF MEME_UTILITY.count_row_id
	 (MEME_CONSTANTS.TN_CONCEPT_STATUS, l_concept_id_1 ) != 1 THEN
	 error_code := 80; error_detail := l_concept_id_1;
	 RAISE aproc_exception;
      ELSIF MEME_UTILITY.count_row_id
	 (MEME_CONSTANTS.TN_CONCEPT_STATUS, l_concept_id_2 ) != 1 THEN
	 error_code := 80; error_detail := l_concept_id_2;
	 RAISE aproc_exception;
      END IF;
   END IF;

   IF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MID_MODE THEN
      location := '60.1';
      atomic_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_ATOMIC_ACTIONS);

      location := '60.2';
      new_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_RELATIONSHIPS);

      location := '60.3';

      INSERT INTO atomic_actions
	(atomic_action_id, molecule_id, action, table_name, row_id,
	 old_value, new_value, authority, timestamp, action_field, status)
      VALUES
     	(atomic_id, molecule_id, MEME_CONSTANTS.AA_INSERT,
     	 MEME_CONSTANTS.TN_RELATIONSHIPS, new_id, MEME_CONSTANTS.YES,
	 MEME_CONSTANTS.NO, authority, aa_timestamp,
	 LOWER(MEME_CONSTANTS.FN_DEAD), 'R');

      location := '60';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

   ELSIF MEME_UTILITY.meme_mode = MEME_CONSTANTS.MRD_MODE THEN
      location := '1.1';
      atomic_id := atomic_action_id;
      new_id := relationship_id;
   END IF;

   IF l_level = MEME_CONSTANTS.SOURCE_LEVEL OR
      l_level = MEME_CONSTANTS.PROCESSED_LEVEL THEN

      location := '110';
      SELECT concept_id INTO l_rel.concept_id_1 FROM classes
      WHERE atom_id = l_atom_id_1;

      location := '120';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      location := '130';
      SELECT concept_id INTO l_rel.concept_id_2 FROM classes
      WHERE atom_id = l_atom_id_2;

      location := '140';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;

      l_sg_meme_data_type_1 := 'C';
      l_sg_meme_id_2 := l_atom_id_1;
      l_sg_meme_data_type_2 := 'C';
      l_sg_meme_id_2 := l_atom_id_2;

   ELSE
      l_sg_meme_data_type_1 := 'CS';
      l_sg_meme_id_2 := l_concept_id_1;
      l_sg_meme_data_type_2 := 'CS';
      l_sg_meme_id_2 := l_concept_id_2;
   END IF;

   location := '160.2';
   l_rel.sg_id_1 := l_sg_id_1;
   l_rel.sg_type_1 := l_sg_type_1;
   l_rel.sg_qualifier_1 := l_sg_qualifier_1;
   l_rel.sg_id_2 := l_sg_id_2;
   l_rel.sg_type_2 := l_sg_type_2;
   l_rel.sg_qualifier_2 := l_sg_qualifier_2;
   location := '160.3';
   l_rel.rui := assign_rui (
	source => l_source,
	relationship_level => l_level,
	relationship_name => l_relationship_name,
	relationship_attribute => l_relationship_attribute,
	tobereleased => l_tobereleased,
	sg_id_1 => l_rel.sg_id_1,
	sg_type_1 => l_rel.sg_type_1,
	sg_qualifier_1 => l_rel.sg_qualifier_1,
	sg_id_2 => l_rel.sg_id_2,
	sg_type_2 => l_rel.sg_type_2,
	sg_qualifier_2 => l_rel.sg_qualifier_2,
	concept_id_1 => l_concept_id_1,
	concept_id_2 => l_concept_id_2,
	atom_id_1 => l_atom_id_1,
	atom_id_2 => l_atom_id_2,
	source_rui => l_source_rui);

   location := '170';
   INSERT INTO relationships
    (relationship_level, relationship_id, atom_id_1,
     relationship_name, relationship_attribute, generated_status,
     authority, timestamp, atom_id_2, source_of_label, source,
     status, dead, concept_id_1, concept_id_2, version_id,
     tobereleased, released, source_rank, rank, rui,
     suppressible,insertion_date,last_molecule_id, last_atomic_action_id,
     sg_id_1, sg_type_1, sg_qualifier_1, sg_meme_data_type_1, sg_meme_id_1,
     sg_id_2, sg_type_2, sg_qualifier_2, sg_meme_data_type_2, sg_meme_id_2,
     source_rui, relationship_group)
   VALUES
    (l_level, new_id, l_atom_id_1,
     l_relationship_Name, l_relationship_attribute, l_generated,
     authority, aa_timestamp, l_atom_id_2, l_source_of_label, l_source,
     l_status, MEME_CONSTANTS.NO, l_rel.concept_id_1, l_rel.concept_id_2, 0,
     l_tobereleased, l_released, l_rel.source_rank, 0, l_rel.rui,
     l_suppressible,   aa_timestamp, molecule_id, atomic_id,
     l_rel.sg_id_1, l_rel.sg_type_1, l_rel.sg_qualifier_1, 
     l_sg_meme_data_type_1, l_sg_meme_id_1,
     l_rel.sg_id_2, l_rel.sg_type_2, l_rel.sg_qualifier_2,
     l_sg_meme_data_type_1, l_sg_meme_id_1,
     l_source_rui, l_relationship_group);

   location := '180';
   IF SQL%ROWCOUNT != 1 THEN
      RAISE aproc_bad_count_err;
   END IF;

   location := '185';
   retval := MEME_RANKS.set_relationship_rank(new_id);
   IF retval < 0 THEN
      RAISE aproc_bad_retval_err;
   END IF;

   IF source_rel_id > 0 THEN
      location := '190';
      INSERT INTO source_id_map
       (local_row_id, table_name, origin, source, source_row_ID)
      VALUES
       (new_id, MEME_CONSTANTS.TN_RELATIONSHIPS, origin,
	l_source_of_label,source_rel_id);

      location := '200';
      IF SQL%ROWCOUNT != 1 THEN
	 RAISE aproc_bad_count_err;
      END IF;
   END IF;

   location := '210';
   IF validate_atomic_action(atomic_id) = FALSE THEN
      error_code := 90; error_detail := 'atomic_id='||atomic_id;
      RAISE aproc_exception;
   END IF;

   RETURN new_id;

EXCEPTION
   WHEN aproc_exception THEN
      meme_aprocs_error('aproc_insert_rel',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_insert_rel',location,30,
         source_rel_id || ',' || l_concept_id_1 || ',' ||
         l_concept_id_2 || ',' || l_atom_id_1 || ',' ||
         l_atom_id_2 || ',' || new_id || ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_insert_rel',location,40,
         source_rel_id || ',' || l_concept_id_1 || ',' ||
         l_concept_id_2 || ',' || l_atom_id_1 || ',' ||
         l_atom_id_2 || ',' || new_id || ',' || retval);
      RETURN -1;
   WHEN aproc_invalid_field_err THEN
      meme_aprocs_error('aproc_insert_rel',location,error_code,error_detail);
      RETURN -1;
   WHEN aproc_validate_action_err THEN
      meme_aprocs_error('aproc_insert_rel',location,90,
         'Invalid atomic_action_id.');
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_insert_rel',location,1,
         source_rel_id || ',' || l_concept_id_1 || ',' ||
         l_concept_id_2 || ',' || l_atom_id_1 || ',' ||
         l_atom_id_2 || ',' || new_id || ',' || SQLERRM);
      RETURN -1;

END aproc_insert_rel;

/* FUNCTION APROC_REDO *********************************************************
 */
FUNCTION aproc_redo(
   atomic_action_id	       IN INTEGER,
   authority		       IN VARCHAR2,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval		       INTEGER;
   l_action		       VARCHAR2(2);
   l_table_name 	       VARCHAR2(2);
   l_row_id		       INTEGER;
   l_new_version_id	       INTEGER;
   l_old_version_id	       INTEGER;
   l_old_value		       VARCHAR2(100);
   l_new_value		       VARCHAR2(100);
   l_status		       VARCHAR2(2);
   l_action_field	       VARCHAR2(25);
   aa_timestamp 	       DATE;
   l_method		       VARCHAR2(50) := 'APROC_REDO';
BEGIN

   initialize_trace (l_method);

   SELECT SYSDATE INTO aa_timestamp FROM dual;

   location := '0';
   SELECT action, table_name, row_id, new_version_id,
	  old_version_id, old_value, new_value, status, action_field
   INTO l_action, l_table_name, l_row_id, l_new_version_id,
	l_old_version_id, l_old_value, l_new_value, l_status, l_action_field
   FROM atomic_actions
   WHERE atomic_action_id = aproc_redo.atomic_action_id;

   IF SQL%ROWCOUNT != 1 THEN
      location := '10';
      RAISE aproc_bad_count_err;
   END IF;

   IF UPPER(l_action) = MEME_CONSTANTS.AA_MOVE THEN
      IF UPPER(l_table_name) = MEME_CONSTANTS.TN_CLASSES THEN
	 retval := aproc_change_concept_id
	    (l_row_id, l_old_value, l_new_value,
	     l_status,0, authority, aa_timestamp, 0, force);
	 IF retval < 0 THEN
	    location := '20';
	    RAISE aproc_bad_retval_err;
	 END IF;
      ELSIF UPPER(l_table_name) = MEME_CONSTANTS.TN_ATTRIBUTES OR
	 UPPER(l_table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	 retval := aproc_change_id
	    (l_table_name, l_row_id, l_old_value, l_new_value,
	     l_status,0, authority, aa_timestamp, 0, force);
	 IF retval < 0 THEN
	    location := '30';
	    RAISE aproc_bad_retval_err;
	 END IF;
      END IF;
   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_CHANGE_ATOM_ID THEN
      retval := aproc_change_atom_id
	 (l_row_id, l_table_name, l_old_value, l_new_value,
	 l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '35';
	 RAISE aproc_bad_retval_err;
      END IF;
   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_CHANGE_FIELD THEN
      retval := aproc_change_field
	 (l_row_id, l_table_name, l_action_field,
	  l_old_value, l_new_value, l_status,
	  0, authority, aa_timestamp, 0, force );
      IF retval < 0 THEN
	 location := '40';
	 RAISE aproc_bad_retval_err;
      END IF;
   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_CHANGE_STATUS THEN
      retval := aproc_change_status
	 (l_table_name, l_row_id, l_old_value,
	  l_new_value,l_status,0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '50';
	 RAISE aproc_bad_retval_err;
      END IF;
   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_CHANGE_TOBERELEASED THEN
      retval := aproc_change_tbr
	 (l_table_name, l_row_id, l_old_value,
	  l_new_value, l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '60';
	 RAISE aproc_bad_retval_err;
      END IF;
   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_DELETE THEN
      retval := aproc_change_dead
	 (l_table_name, l_row_id, l_old_value,
	  l_new_value, l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '70';
	 RAISE aproc_bad_retval_err;
      END IF;
   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_INSERT THEN
      retval := aproc_change_dead
	 (l_table_name, l_row_id, l_old_value,
	  l_new_value, l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '90';
	 RAISE aproc_bad_retval_err;
      END IF;
   END IF;

RETURN 0;

EXCEPTION
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_redo',location,30,
         atomic_action_id || ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_redo',location,40,
         atomic_action_id || ',' || retval);
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_redo',location,1,
         atomic_action_id || ',' || SQLERRM);
      RETURN -1;

END aproc_redo;

/* FUNCTION APROC_UNDO *********************************************************
 */
FUNCTION aproc_undo(
   atomic_action_id	       INTEGER,
   authority		       VARCHAR2,
   force	 	       IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval		       INTEGER;
   l_action		       VARCHAR2(2);
   l_status		       VARCHAR2(2);
   l_table_name 	       VARCHAR2(2);
   l_row_id		       INTEGER;
   l_new_version_id	       INTEGER;
   l_old_version_id	       INTEGER;
   l_old_value		       VARCHAR2(100);
   l_new_value		       VARCHAR2(100);
   l_action_field	       VARCHAR2(25);
   aa_timestamp 	       DATE;
   l_method		       VARCHAR2(50) := 'APROC_UNDO';
BEGIN

   initialize_trace('aproc_redo');

   SELECT SYSDATE INTO aa_timestamp FROM dual;

   location := '0';
   SELECT action, table_name, row_id, new_version_id,
	  old_version_id, old_value, new_value, status, action_field
   INTO l_action, l_table_name, l_row_id, l_new_version_id,
	l_old_version_id, l_old_value, l_new_value, l_status, l_action_field
   FROM atomic_actions
   WHERE atomic_action_id = aproc_undo.atomic_action_id;

   IF SQL%ROWCOUNT != 1 THEN
      location := '10';
      RAISE aproc_bad_count_err;
   END IF;

   IF UPPER(l_action) = MEME_CONSTANTS.AA_MOVE THEN
      IF UPPER(l_table_name) = MEME_CONSTANTS.TN_CLASSES THEN
	 retval := aproc_change_concept_id
	    (l_row_id, l_new_value, l_old_value,
	     l_status, 0, authority, aa_timestamp, 0, force);
	 IF retval < 0 THEN
	    location := '20';
	    RAISE aproc_bad_retval_err;
	 END IF;
      ELSIF UPPER(l_table_name) = MEME_CONSTANTS.TN_ATTRIBUTES OR
	    UPPER(l_table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	 retval := aproc_change_id
	    (l_table_name, l_row_id, l_new_value, l_old_value,
	     l_status, 0, authority, aa_timestamp, 0, force);
	 IF retval < 0 THEN
	    location := '30';
	    RAISE aproc_bad_retval_err;
	 END IF;
      END IF;

   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_CHANGE_ATOM_ID THEN
      retval := aproc_change_atom_id
	 (l_row_id, l_table_name, l_new_value, l_old_value,
	 l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '35';
	 RAISE aproc_bad_retval_err;
      END IF;

   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_CHANGE_FIELD THEN
      retval := aproc_change_field
	 (l_row_id, l_table_name, l_action_field,
	  l_new_value, l_old_value, l_status,
	  0, authority, aa_timestamp, 0, force );
      IF retval < 0 THEN
	 location := '40';
	 RAISE aproc_bad_retval_err;
      END IF;

   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_CHANGE_STATUS THEN
      retval := aproc_change_status
	 (l_table_name, l_row_id, l_new_value,
	  l_old_value, l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '50';
	 RAISE aproc_bad_retval_err;
      END IF;

   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_CHANGE_TOBERELEASED THEN
      retval := aproc_change_tbr
	 (l_table_name, l_row_id, l_new_value,
	  l_old_value, l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '60';
	 RAISE aproc_bad_retval_err;
      END IF;

   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_INSERT THEN
      retval := aproc_change_dead
	 (l_table_name, l_row_id, l_new_value,
	  l_old_value, l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '70';
	 RAISE aproc_bad_retval_err;
      END IF;
   ELSIF UPPER(l_action) = MEME_CONSTANTS.AA_DELETE THEN
      retval := aproc_change_dead
	 (l_table_name, l_row_id, l_new_value,
	  l_old_value, l_status, 0, authority, aa_timestamp, 0, force);
      IF retval < 0 THEN
	 location := '80';
	 RAISE aproc_bad_retval_err;
      END IF;
   END IF;

   RETURN 0;

EXCEPTION
   WHEN aproc_bad_count_err OR NO_DATA_FOUND THEN
      meme_aprocs_error('aproc_undo',location,30,
         atomic_action_id || ',' || SQL%ROWCOUNT);
      RETURN -1;
   WHEN aproc_bad_retval_err THEN
      meme_aprocs_error('aproc_undo',location,40,
         atomic_action_id || ',' || retval);
      RETURN -1;
   WHEN OTHERS THEN
      meme_aprocs_error('aproc_undo',location,1,
         atomic_action_id || ',' || SQLERRM);
      RETURN -1;

END aproc_undo;

/* FUNCTION BURY_INDEX *********************************************************
 */
FUNCTION bury_index(
   atom_id		       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'BURY_INDEX';
BEGIN
   initialize_trace(l_method);

   location := '10';
   INSERT INTO dead_normwrd
   SELECT * FROM normwrd
   WHERE normwrd_id = atom_id;

   location := '20';
   INSERT INTO dead_normstr
   SELECT * FROM normstr
   WHERE normstr_id = atom_id;

   location := '30';
   INSERT INTO dead_word_INDEX
   SELECT * FROM word_INDEX
   WHERE atom_id = bury_index.atom_id;

   location := '40';
   DELETE FROM normwrd
   WHERE normwrd_id = atom_id;

   location := '50';
   DELETE FROM normstr
   WHERE normstr_id = atom_id;

   location := '60';
   DELETE FROM word_INDEX
   WHERE atom_id = bury_index.atom_id;

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('bury_index',location,1,atom_id||','||SQLERRM);
       RETURN -1;

END bury_index;

/* FUNCTION BURY_ATTRIBUTES ****************************************************
 */
FUNCTION bury_attributes(
   attribute_id 	       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'BURY_ATTRIBUTES';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO dead_stringtab
   SELECT * FROM stringtab
   WHERE string_id IN
    (SELECT to_NUMBER(substr(attribute_value,20))
     FROM attributes
     WHERE attribute_id= bury_attributes.attribute_id
     AND attribute_value like '<>Long_Attribute<>:%');

   location := '20';
   DELETE FROM stringtab
   WHERE string_id IN
    (SELECT to_NUMBER(substr(attribute_value,20))
     FROM attributes
     WHERE attribute_id= bury_attributes.attribute_id
     AND attribute_value like '<>Long_Attribute<>:%');

   location := '30';
   INSERT INTO dead_attributes
   SELECT * FROM attributes
   WHERE attribute_id = bury_attributes.attribute_id;

   location := '40';
   DELETE FROM attributes
   WHERE attribute_id = bury_attributes.attribute_id;

   /** This table is no longer used
   location := '50';
   INSERT INTO dead_sg_attributes
   SELECT * FROM sg_attributes
   WHERE attribute_id = bury_attributes.attribute_id;

   location := '60';
   DELETE FROM sg_attributes
   WHERE attribute_id = bury_attributes.attribute_id;
   */
   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('bury_attributes',location,1,
        attribute_id||','||SQLERRM);
      RETURN -1;
END bury_attributes;

/* FUNCTION BURY_CLASSES *******************************************************
 */
FUNCTION bury_classes(
   atom_id		       IN INTEGER
)
RETURN INTEGER
IS
   retval		       INTEGER;
   l_method		       VARCHAR2(50) := 'BURY_CLASSES';
BEGIN

   initialize_trace(l_method);

   location := '0';
   retval := bury_index(atom_id);
   IF retval != 0 THEN
      RAISE meme_aprocs_exception;
   END IF;

   location := '10';
   INSERT INTO dead_classes
   SELECT * FROM classes
   WHERE atom_id= bury_classes.atom_id;

   location := '20';
   INSERT INTO dead_atoms
   SELECT * FROM atoms
   WHERE atom_id= bury_classes.atom_id;

   location := '30';
   DELETE FROM classes
   WHERE atom_id= bury_classes.atom_id;

   location := '40';
   DELETE FROM atoms
   WHERE atom_id = bury_classes.atom_id;

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('bury_classes',location,1,atom_id||','||SQLERRM);
      RETURN -1;
END bury_classes;

/* FUNCTION BURY_CONCEPT_STATUS ************************************************
 */
FUNCTION bury_concept_status(
   concept_id		       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'BURY_CONCEPT_STATUS';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO dead_concept_status
   SELECT * FROM concept_status
   WHERE concept_id = bury_concept_status.concept_id;

   location := '20';
   DELETE FROM concept_status
   WHERE concept_id =  bury_concept_status.concept_id;

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('bury_concept_status',location,1,
         concept_id||','||SQLERRM);
      RETURN -1;

END bury_concept_status;

/* FUNCTION BURY_RELATIONSHIPS *************************************************
 */
FUNCTION bury_relationships(
   relationship_id	       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'BURY_RELATIONSHIPS';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO dead_relationships
   SELECT * FROM relationships
   WHERE relationship_id = bury_relationships.relationship_id;

   location := '20';
   DELETE FROM relationships
   WHERE relationship_id = bury_relationships.relationship_id;

   /** this table is no longer used
   location := '30';
   INSERT INTO dead_sg_relationships
   SELECT * FROM sg_relationships
   WHERE relationship_id = bury_relationships.relationship_id;

   location := '40';
   DELETE FROM sg_relationships
   WHERE relationship_id = bury_relationships.relationship_id;
   */

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('bury_relationships',location,1,
         relationship_id||','||SQLERRM);
      RETURN -1;

END bury_relationships;


/* FUNCTION BURY_CXT_RELATIONSHIPS *************************************************
 */
FUNCTION bury_cxt_relationships(
   relationship_id	       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'BURY_CXT_RELATIONSHIPS';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO dead_context_relationships
   SELECT * FROM context_relationships
   WHERE relationship_id = bury_cxt_relationships.relationship_id;

   location := '20';
   DELETE FROM context_relationships
   WHERE relationship_id = bury_cxt_relationships.relationship_id;

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('bury_cxt_relationships',location,1,
         relationship_id||','||SQLERRM);
      RETURN -1;

END bury_cxt_relationships;

/* FUNCTION DIGUP_INDEX ********************************************************
 */
FUNCTION digup_index(
   atom_id		       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'DIGUP_INDEX';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO normwrd
   SELECT * FROM dead_normwrd
   WHERE normwrd_id = atom_id;

   location := '20';
   INSERT INTO normstr
   SELECT * FROM dead_normstr
   WHERE normstr_id = atom_id;

   location := '30';
   INSERT INTO word_index
   SELECT * FROM dead_word_index
   WHERE atom_id = digup_index.atom_id;

   location := '40';
   DELETE FROM dead_normwrd
   WHERE normwrd_id = atom_id;

   location := '50';
   DELETE FROM dead_normstr
   WHERE normstr_id = atom_id;

   location := '60';
   DELETE FROM dead_word_index
   WHERE atom_id = digup_index.atom_id;

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('digup_index',location,1,atom_id||','||SQLERRM);
      RETURN -1;

END digup_index;

/* FUNCTION DIGUP_ATTRIBUTES ***************************************************
 */
FUNCTION digup_attributes(
   attribute_id 	       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'DIGUP_ATTRIBUTES';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO attributes
   SELECT * FROM dead_attributes
   WHERE attribute_id = digup_attributes.attribute_id;

   location := '20';
   INSERT INTO stringtab
   SELECT * FROM dead_stringtab
   WHERE string_id IN
    (SELECT to_NUMBER(substr(attribute_value,20))
     FROM dead_attributes
     WHERE attribute_id = digup_attributes.attribute_id
     AND attribute_value like MEME_CONSTANTS.LONG_ATTRIBUTE || '%');

   location := '30';
   DELETE FROM dead_stringtab
   WHERE string_id IN
    (SELECT to_NUMBER(substr(attribute_value,20))
     FROM dead_attributes
     WHERE attribute_id= digup_attributes.attribute_id
     AND attribute_value like MEME_CONSTANTS.LONG_ATTRIBUTE || '%');

   location := '40';
   DELETE FROM dead_attributes
   WHERE attribute_id = digup_attributes.attribute_id;

   /** this table is no longer used
   location := '50';
   DELETE FROM dead_sg_attributes
   WHERE attribute_id = digup_attributes.attribute_id;
   */
   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('digup_index',location,1,
         attribute_id||','||SQLERRM);
      RETURN -1;

END digup_attributes;

/* FUNCTION DIGUP_CLASSES ******************************************************
 */
FUNCTION digup_classes(
   atom_id		       IN INTEGER
)
RETURN INTEGER
IS
   retval		       INTEGER;
   l_method		       VARCHAR2(50) := 'DIGUP_CLASSES';
BEGIN

   initialize_trace(l_method);

   location := '0';
   retval := digup_index(atom_id);
   IF retval != 0 THEN
      RAISE meme_aprocs_exception;
   END IF;

   location := '10';
   INSERT INTO classes
   SELECT * FROM dead_classes
   WHERE atom_id= digup_classes.atom_id;

   location := '20';
   INSERT INTO atoms
   SELECT * FROM dead_atoms
   WHERE atom_id= digup_classes.atom_id;

   location := '30';
   DELETE FROM dead_classes
   WHERE atom_id= digup_classes.atom_id;

   location := '40';
   DELETE FROM dead_atoms
   WHERE atom_id = digup_classes.atom_id;

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('digup_classes',location,1,atom_id || ',' ||SQLERRM);
      RETURN -1;

END digup_classes;

/* FUNCTION DIGUP_CONCEPT_STATUS ***********************************************
 */
FUNCTION digup_concept_status(
   concept_id		       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'DIGUP_CONCEPT_STATUS';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO concept_status
   SELECT * FROM dead_concept_status
   WHERE concept_id = digup_concept_status.concept_id;

   location := '20';
   DELETE FROM dead_concept_status
   WHERE concept_id =  digup_concept_status.concept_id;

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('digup_concept_status',location,1,
      concept_id||','||SQLERRM);
      RETURN -1;

END digup_concept_status;

/* FUNCTION DIGUP_RELATIONSHIPS ************************************************
 */
FUNCTION digup_relationships(
   relationship_id	       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'DIGUP_RELATIONSHIPS';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO relationships
   SELECT * FROM dead_relationships
   WHERE relationship_id = digup_relationships.relationship_id;

   location := '20';
   DELETE FROM dead_relationships
   WHERE relationship_id = digup_relationships.relationship_id;

   /** this table is no longer used
   location := '30';
   INSERT INTO sg_relationships
   SELECT * FROM dead_sg_relationships
   WHERE relationship_id = digup_relationships.relationship_id;

   location := '20';
   DELETE FROM dead_sg_relationships
   WHERE relationship_id = digup_relationships.relationship_id;
   */
   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('digup_relationships',location,1,
      relationship_id||','||SQLERRM);
      RETURN -1;

END digup_relationships;


/* FUNCTION DIGUP_RELATIONSHIPS ************************************************
 */
FUNCTION digup_cxt_relationships(
   relationship_id	       IN INTEGER
)
RETURN INTEGER
IS
   l_method		       VARCHAR2(50) := 'DIGUP_CXT_RELATIONSHIPS';
BEGIN

   initialize_trace(l_method);

   location := '10';
   INSERT INTO context_relationships
   SELECT * FROM dead_context_relationships
   WHERE relationship_id = digup_cxt_relationships.relationship_id;

   location := '20';
   DELETE FROM dead_context_relationships
   WHERE relationship_id = digup_cxt_relationships.relationship_id;

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('digup_cxt_relationships',location,1,
      relationship_id||','||SQLERRM);
      RETURN -1;

END digup_cxt_relationships;

/* VALIDATE_ATOMIC_ACTION ******************************************************
 */
FUNCTION validate_atomic_action(
   atomic_action_id	       IN INTEGER
)
RETURN BOOLEAN
IS
   aa_rec		       atomic_actions%ROWTYPE;
   row_count		       INTEGER := 0;
   where_clause 	       VARCHAR2(200) := '';
   l_cracs_id		       VARCHAR2(20);
   l_cracs_table	       VARCHAR2(50);
   l_method		       VARCHAR2(50) := 'VALIDATE_ATOMIC_ACTION';
BEGIN

   initialize_trace(l_method);

   IF meme_aprocs_validate = FALSE THEN
      RETURN TRUE;
   END IF;

   location := '10';
   SELECT * INTO aa_rec FROM atomic_actions
   WHERE atomic_action_id = validate_atomic_action.atomic_action_id;

   IF SQL%ROWCOUNT != 1 THEN
      location := '20';
      meme_aprocs_error(method,location,error_code,SQLERRM);
      RETURN FALSE;
   END IF;

   /* Mapped CRACS id and table name */
   location := '30';
   l_cracs_table := MEME_UTILITY.get_value_by_code(aa_rec.table_name,'table_name');
   location := '35';
   l_cracs_id := MEME_UTILITY.get_value_by_code(l_cracs_table,'primary_key');

   /* Mapped query conditions */
   where_clause := l_cracs_id||' = '||aa_rec.row_id;
   /* The current value of where_clause, is enough to handle insert */

   IF UPPER(aa_rec.action) = MEME_CONSTANTS.AA_CHANGE_TOBERELEASED THEN
      where_clause := where_clause||
	 ' AND tobereleased = '||''''||aa_rec.new_value||'''';
   ELSIF UPPER(aa_rec.action) = MEME_CONSTANTS.AA_CHANGE_STATUS THEN
      where_clause := where_clause||
	 ' AND status = '||''''||aa_rec.new_value||'''';
   ELSIF UPPER(aa_rec.action) = MEME_CONSTANTS.AA_MOVE THEN
      IF UPPER(aa_rec.table_name) = MEME_CONSTANTS.TN_CLASSES THEN
	 where_clause := where_clause ||
	 ' AND concept_id = TO_NUMBER('||aa_rec.new_value||')';
      ELSIF UPPER(aa_rec.table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	 where_clause := where_clause||
	    ' AND (concept_id_1 = TO_NUMBER('||aa_rec.new_value||')'||
	    ' OR concept_id_2 = TO_NUMBER('||aa_rec.new_value||'))';
      ELSIF UPPER(aa_rec.table_name) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
	 where_clause := where_clause||
	    ' AND concept_id = TO_NUMBER('||aa_rec.new_value||')';
      ELSIF UPPER(aa_rec.table_name) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
	 location := '12';
	 error_detail := 'action='||''''||aa_rec.action||''''||
		     ' table='||''''||aa_rec.table_name||'''';
	 meme_aprocs_error(method,location,error_code,error_detail);
	 RETURN FALSE;
      END IF;
   ELSIF UPPER(aa_rec.action) = MEME_CONSTANTS.AA_CHANGE_ATOM_ID THEN
      IF UPPER(aa_rec.table_name) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	 where_clause := where_clause||
	    ' AND (atom_id_1 = TO_NUMBER('||aa_rec.new_value||')'||
	    ' OR atom_id_2 = TO_NUMBER('||aa_rec.new_value||'))';
      ELSIF UPPER(aa_rec.table_name) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
	 where_clause := where_clause||
	    ' AND atom_id = TO_NUMBER('||aa_rec.new_value||')';
      ELSIF UPPER(aa_rec.table_name) = MEME_CONSTANTS.TN_CLASSES OR
	 UPPER(aa_rec.table_name) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
	 location := '14';
	 error_detail := 'action='||''''||aa_rec.action||''''||
		     ' table='||''''||aa_rec.table_name||'''';
	 meme_aprocs_error(method,location,error_code,error_detail);
	 RETURN FALSE;
      END IF;
   ELSIF UPPER(aa_rec.action) = MEME_CONSTANTS.AA_CHANGE_FIELD THEN
      -- prepend a . to compare null values
      where_clause := where_clause||
	' AND ''.''||'||aa_rec.action_field||' = '||
	'''.'||aa_rec.new_value||'''';
   END IF;

   location := '40';

   /* Query execution */

   row_count := MEME_UTILITY.EXEC_SELECT
      ('SELECT COUNT(*) FROM '||l_cracs_table||
	 ' WHERE '||where_clause);

   /* CRACS ID validation */

   IF UPPER(aa_rec.action) = MEME_CONSTANTS.AA_DELETE THEN
      -- AA_DELETE is actually "change dead".  i.e. the same
      -- action is used to delete AND undo a delete, so we need
      -- to check new_value to see where the row actually should be
      IF (row_count > 0 AND aa_rec.new_value = MEME_CONSTANTS.YES) OR
	 (row_count != 1 AND aa_rec.new_value = MEME_CONSTANTS.NO) THEN
	 location := '22'; error_detail := 'Found in '||l_cracs_table;
	 meme_aprocs_error(method,location,error_code,error_detail);
	 RETURN FALSE;
      END IF;

      /* Validate DEAD_CRACS */

      location := '54';

      row_count := MEME_UTILITY.EXEC_SELECT
	 ('SELECT COUNT(*) FROM dead_'||l_cracs_table||
	     ' WHERE '||l_cracs_id||' = '||aa_rec.row_id);

      IF (row_count != 1 AND aa_rec.new_value = MEME_CONSTANTS.YES) OR
	 (row_count > 0 AND aa_rec.new_value = MEME_CONSTANTS.NO) THEN
	 location := '60';
	 error_detail := 'Not found in dead_'||l_cracs_table;
	 meme_aprocs_error(method,location,error_code,error_detail);
	 RETURN FALSE;
      END IF;
   ELSE
      IF row_count != 1 THEN
	 location := '70';
	 error_detail := 'Not found in ' || l_cracs_table || '(' ||
		where_clause || ')';
	 meme_aprocs_error(method,location,error_code,error_detail);
	 RETURN FALSE;
      END IF;
   END IF;

   RETURN TRUE;

EXCEPTION
   WHEN OTHERS THEN
      meme_aprocs_error('validate_atomic_action',location,90,
      atomic_action_id||','||SQLERRM);
      RETURN FALSE;
END validate_atomic_action;

/* PROCEDURE CONNECT_CONCEPTS **************************************************
 */
PROCEDURE connect_concepts(
      new_value			IN INTEGER,
      old_value			IN INTEGER,
      field_name		IN VARCHAR2
   )
IS
   old_concept_id	INTEGER;
   new_concept_id	INTEGER;
BEGIN

   -- If values are atom ids, pick up the concept_ids
   IF field_name like 'ATOM%' THEN
      SELECT concept_id INTO new_concept_id
      FROM classes WHERE atom_id = new_value;

      SELECT concept_id INTO old_concept_id
      FROM classes WHERE atom_id = old_value;
   ELSE
	old_concept_id := old_value;
	new_concept_id := new_value;
   END IF;

   IF new_concept_id != old_concept_id THEN
      MEME_UTILITY.exec(
  	'INSERT INTO connected_concepts (concept_id_1,concept_id_2) ' ||
	'VALUES (' || new_concept_id || ', ' || old_concept_id || ')' );
   END IF;

END connect_concepts;

/* PROCEDURE REGISTER_PACKAGE **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_APROCS.release_number,
      MEME_APROCS.version_number,
      SYSDATE,
      MEME_APROCS.version_authority,
      MEME_APROCS.package_name,
      '',
      MEME_CONSTANTS.YES,
      MEME_CONSTANTS.YES
   );
END register_package;

/* PROCEDURE SELF_TEST *********************************************************
 */
PROCEDURE self_test
IS
BEGIN
   -- SERVEROUTPUT must be on to see this.

   DBMS_OUTPUT.PUT_LINE('Self_test');

END self_test;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help
IS
BEGIN
   help('');
END;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help( method_name IN VARCHAR2)
IS
BEGIN

   -- This procedure requires SET SERVEROUTPUT ON

   DBMS_OUTPUT.PUT_LINE('.');

   IF method_name IS NULL OR method_name = '' THEN
      DBMS_OUTPUT.PUT_LINE('.This package provides the core functionality for the ');
      DBMS_OUTPUT.PUT_LINE('.MEME model, the atomic actions. Following is a list of');
      DBMS_OUTPUT.PUT_LINE('.these methods.  ');
      DBMS_OUTPUT.PUT_LINE('.');
      DBMS_OUTPUT.PUT_LINE('. aproc_change_field:      Change arbitrary field in core table.');
      DBMS_OUTPUT.PUT_LINE('. aproc_change_concept_id: Change concept_id field in classes and');
      DBMS_OUTPUT.PUT_LINE('.			       move corresponding attributes and rels.');
      DBMS_OUTPUT.PUT_LINE('. aproc_change_atom_id:    Move atom_id from attributes and rels.');
      DBMS_OUTPUT.PUT_LINE('. aproc_change_dead:       Move core table row to/from dead_* table.');
      DBMS_OUTPUT.PUT_LINE('. aproc_change_id:	       Change concept_id field in relationships');
      DBMS_OUTPUT.PUT_LINE('.			       or attributes tables.');
      DBMS_OUTPUT.PUT_LINE('. aproc_change_status:     Change status field in core table.');
      DBMS_OUTPUT.PUT_LINE('. aproc_change_tbr:        Change tobereleased field in core table.');
      DBMS_OUTPUT.PUT_LINE('. aproc_insert_attribute:  Insert an attributes row.');
      DBMS_OUTPUT.PUT_LINE('. aproc_insert_classes:    Insert a classes row.');
      DBMS_OUTPUT.PUT_LINE('. aproc_insert_cs:	       Insert a concept_status row.');
      DBMS_OUTPUT.PUT_LINE('. aproc_insert_rel:        Insert a relationship row.');
      DBMS_OUTPUT.PUT_LINE('. aproc_redo:	       Redo an atomic action.');
      DBMS_OUTPUT.PUT_LINE('. aproc_undo:	       Undo an atomic action.');
      DBMS_OUTPUT.PUT_LINE('. assign_aui:	       Assign an AUI to a set of fields.');
      DBMS_OUTPUT.PUT_LINE('. assign_atui:	       Assign an ATUI to a set of fields.');
      DBMS_OUTPUT.PUT_LINE('. assign_rui:	       Assign an RUI to a set of fields.');
      DBMS_OUTPUT.PUT_LINE('. bury_attributes:	       Move an attribute row to dead table');
      DBMS_OUTPUT.PUT_LINE('. bury_classes:	       Move a classes row to dead table.');
      DBMS_OUTPUT.PUT_LINE('. bury_concept_status:     Move a concept row to dead table.');
      DBMS_OUTPUT.PUT_LINE('. bury_index:	       Move index table rows to dead tables.');
      DBMS_OUTPUT.PUT_LINE('. bury_relationships:      Move relationships row to dead table.');
      DBMS_OUTPUT.PUT_LINE('. bury_cxt_relationships:      Move context relationships row to dead table.');
      DBMS_OUTPUT.PUT_LINE('. digup_attributes:        Move dead attribute to attributes.');
      DBMS_OUTPUT.PUT_LINE('. digup_classes:	       Move dead atom to classes.');
      DBMS_OUTPUT.PUT_LINE('. digup_concept_status:    Move dead concept to concept_status.');
      DBMS_OUTPUT.PUT_LINE('. digup_index:	       Move dead index rows to live tables.');
      DBMS_OUTPUT.PUT_LINE('. digup_relationships:     Move dead relationship to relationships.');
      DBMS_OUTPUT.PUT_LINE('. digup_cxt_relationships: Move dead context relationship to context_relationships.');
      DBMS_OUTPUT.PUT_LINE('. validate_atomic_actions: Validate atomic actions.');
      DBMS_OUTPUT.PUT_LINE('. connect_concepts:        Mark concept as "connected".');
   ELSE
      DBMS_OUTPUT.PUT_LINE('.There is no help for the topic: "' || method_name || '".');
   END IF;

   -- Print version
   DBMS_OUTPUT.PUT_LINE('.');
   version;

END help;


END MEME_APROCS;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_APROCS.help;
execute MEME_APROCS.register_package;

