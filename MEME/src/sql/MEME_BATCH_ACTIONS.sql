CREATE OR REPLACE PACKAGE MEME_BATCH_ACTIONS AS

/******************************************************************************
 *
 * PL/SQL File: MEME_BATCH_ACTIONS.sql
 *
 * This package contains functions to perform batch and macro action operations
 *
 * Version Information
 *
 * 03/11/2005 4.30.0: last_release_cui is loaded into classes from s_c_a 
 * 12/30/2004 4.29.0: Released. Includes better log_operation call 
 * 12/23/2004 4.28.2: action_log bug fix for query to delete attributes 
 *                    connected to relationships that are being removed.
 * 12/22/2004 4.28.1: action_redo and action_undo support force undo/redo
 * 12/13/2004 4.28.0: Released
 * 11/29/2004 4.27.1: Support for connected actions 
 *                          (D:C -> D:A,R,CR, D:R,CR -> A)
 *                    No longer using operations_queue
 *                    -report_macro_change
 *                    -report_table_change
 * 06/09/2004 4.27.0: Released
 * 05/14/2004 4.26.1: Batch insert removed (remove "! remove this")
 * 05/05/2004 4.26.0: Fixed to work with oracle 9.2
 * 05/03/2004 4.25.0: Fixed to work with oracle 9.2
 * 04/28/2004 3.24.0: Released
 * 04/26/2004 3.23.1: Changing classes releasability may affect AUI
 * 03/17/2004 3.23.0: Fix in insert into context_relationships query 
 * 03/08/2004 3.22.0: Released
 * 02/26/2004 3.21.1: insert into CR uses sg fields
 * 12/15/2003 3.21.0: Released
 * 11/26/2003 3.20.1: cleaned up action_log queries
 * 11/07/2003 3.20.0: insert into context_relationships was not loading rui
 * 09/30/2003 3.19.0: Released
 * 09/10/2003 3.18.4: insert into rels,atts has faster ranking algo
 *                    improvements to ATUI,RUI,AUI maintenance
 * 07/10/2003 3.18.3: insert into classes has faster ranking algo
 * 06/20/2003 3.18.2: insert into classes supports language
 * 06/02/2003 3.18.1: ATUI,RUI,ATUI maintenance
 * 05/12/2003 3.18.0: Released
 * 05/06/2003 3.17.1: insert into sg_attribute/sg_relationships only happens
 * 11/27/2002 3.17.0: insert into sg_attribute/sg_relationships only happens
 *                    for CUI or %STRIPPED% types.
 * 09/09/2002 3.16.0: aproc_change_field handels null values
 * 09/06/2002 3.15.1: insert into classes now uses aui
 * 07/17/2002 3.15.0: Released
 * 06/10/2002 3.14.3: When creating t_rtc tables to report
 *                    inserts to the MRD, we make the table
 *                    STORAGE (INITIAL 100M NEXT 100M) so that
 *                    data is contiguous, making the reporting
 *                    operation faster.
 * 05/28/2002 3.14.2: set_preferred_flag for macro_action and batch_action
 *                    now has default of YES instead of NO.
 * 04/17/2002 3.14.1: INSERT INTO classes now adds tty field
 * 02/28/2002 3.14.0: action_log was reporting an error when calling performing
 *                    a batch CF if the field being changed was VARCHAR2 
 *                    because the procedure was attempting to wrap 
 *                    to_char around it. Now, we check the field type first. 
 * 12/14/2001 3.13.0: Released
 * 10/23/2001 3.12.1: Performance enhancment: sometimes you  know that
 * 		      precedence will be recomputed later so this package
 *		      does not need to do it.  A set_preferred_flag
 *		      was added to batch_action and macro_action
 * 		      that can disable or enable the setting of the preferred
 *		      concept_id.
 * 09/05/2001 3.12.0: Released 11.* changes
 *    07/11/2001 3.11.1: Location 310 action_help had an error
 *		 	 where the query was selecting from
 *			 molecular_Actions using transaction_id=transaction_id
 *			 instead of transaction_id=action_help.transaction_id;
 *
 * 			 Fixed up report_macro_change to correctly report
 * 			 changes
 *
 *			 Removed package variables: cracs_field, macro_field,
 *                       action_method, cracs_source  by using local variables.
 *    06/04/2001 3.11.0: Changed so action_change_field requires a table
 *                        with a varchar2 new_value field
 *                       Released.
 *    05/16/2001 3.10.2: append events sent to the MRD should
 *                       use key_field="".  There is no good reason
 *                       why the key field should be set.
 *    05/11/2001 3.10.1: Problem in macro_insert if oq_mode was on.
 *                   	dropping the tmp tables (t_rtc_...) was not
 *			working because of transaction scope issues.
 *			The drop tables were moved to after the COMMIT
 *			in action_help to just avoid the issue
 *    04/20/2001 3.10.0: Released to NLM
 *    04/10/2001 3.9.2: Changes to macro_insert to support the new
 *                      [*_]context_relationships schema
 *    04/09/2001 3.9.1: Fix to action_change_atom_id to allow 
 *                      atom_ids of C level
 *			stuff to be maintained also.
 *    03/28/2001 3.9.0: Released version
 *    03/21/2001 3.8.1: Changes in action_help, macro_redo (action_redo) and
 *                    macro_undo (action_undo) to correct reporting of table
 *                    changes in MRD.
 *    12/14/2000 3.8.0: action_delete works with context_relationships
 *    11/16/2000:     If table is empty,  action will return 0
 *                    without performing any action log
 *    11/10/2000:     Released
 *    11/03/2000:     Call to report_table_change
 *    10/31/2000:     Deal with ddl_commit_mode
 *    10/26/2000:     Changes in macro_insert
 *    10/18/2000:     batch_redo, batch_undo, macro_redo, macro_undo
 *    10/16/2000:     Major changes to package
 *		      batch_change_atom_id, macro_action
 *    8/02/2000:      Macro-insert calculates preferred atom ids
 *		      This is actually done now in MEME_SOURCE_PROCESSING
 *    8/01/2000:      Package handover version
 *    9/09/1999:      First version created and compiled
 *    7/26/2000:      Better error messages, switch='R' for macro insert
 *		      Better elapsed time calculations
 *    7/21/2000:      Core table insert CR: don't rank these rows
 *		      classes.last_release_cui should be '' not 0 (macro ins)
 *    6/30/2000:      INSERT /*+ APPEND * /
 *    3/23/2000:      Package re-creation
 *
 * Status:
 *    Functionality:  DONE?
 *    Testing:	      DONE?
 *    Enhancements:
 *
 *    Description of the function being called from outside:
 *	 The batch_action and macro_action expect necessary input parameters to
 *	 perform a batch or macro action operation.  It calls internal function
 *	 to produced the desired results.
 *
 *	 The differences between these two actions are batch action logged each
 *	 row  for atomic and  molecular action, while macro action logged  each
 *	 atomic action	into a single  molecular action.  
 *
 *	 The   batch_undo,   macro_undo,    batch_redo and   macro_redo  expect
 *       transaction_id and authority as input parameters to perform a batch or
 *       macro undo and redo operation.
 *
 *       Reporting to MRD now happens in MEME directly.
 *
 ******************************************************************************/

   /* package info */
   package_name 	VARCHAR2(25) := 'MEME_BATCH_ACTIONS';
   release_number	VARCHAR2(1)  := '4';
   version_number	VARCHAR2(5)  := '30.0';
   version_date 	DATE	     := '11-Mar-2005';
   version_authority	VARCHAR2(3)  := 'BAC';

   /* public variables */
   mba_debug		BOOLEAN := FALSE;
   mba_trace		BOOLEAN := FALSE;

   method		VARCHAR2(50);
   location		VARCHAR2(10);
   error_code		INTEGER;
   error_detail 	VARCHAR2(4000);

   cracs_table		VARCHAR2(50) := NULL;
   primary_key		VARCHAR2(50) := NULL;

   msp_set_preferred_flag      VARCHAR2(1) := MEME_CONSTANTS.YES;

   /* functions and procedures */

   FUNCTION release RETURN INTEGER;
   FUNCTION version RETURN FLOAT;

   PROCEDURE version;

   FUNCTION version_info RETURN VARCHAR2;
   PRAGMA RESTRICT_REFERENCES(version_info,WNDS,RNDS,WNPS);

   PROCEDURE help;
   PROCEDURE help(topic IN VARCHAR2);

   PROCEDURE register_package;
   PROCEDURE set_trace_on;
   PROCEDURE set_trace_off;
   PROCEDURE set_debug_on;
   PROCEDURE set_debug_off;

   PROCEDURE trace(message IN VARCHAR2);

   FUNCTION  local_exec(query IN VARCHAR2) RETURN INTEGER;

   PROCEDURE initialize_trace(l_method IN VARCHAR2);

   PROCEDURE error_log(
      method		IN VARCHAR2,
      location		IN VARCHAR2,
      error_code	IN INTEGER,
      detail		IN VARCHAR2);

   FUNCTION action_check(
      action		IN VARCHAR2,
      id_type		IN VARCHAR2,
      table_name	IN VARCHAR2,
      new_value		IN VARCHAR2,
      action_field	IN VARCHAR2)
   RETURN INTEGER;

   FUNCTION action_log(
      action		IN VARCHAR2,
      id_type		IN VARCHAR2,
      authority 	IN VARCHAR2,
      table_name	IN VARCHAR2,
      action_name	IN VARCHAR2,
      action_short	IN VARCHAR2,
      work_id		IN NUMBER,
      status		IN VARCHAR2,
      new_value 	IN VARCHAR2,
      action_field	IN VARCHAR2)
   RETURN INTEGER;

   FUNCTION macro_insert(
      id_type		IN VARCHAR2,
      authority 	IN VARCHAR2,
      transaction_id	IN INTEGER)
   RETURN INTEGER;

   FUNCTION action_change_field(
      id_type		IN VARCHAR2,
      transaction_id	IN INTEGER,
      action_field	IN VARCHAR2)
   RETURN INTEGER;

   FUNCTION action_move(
      id_type		IN VARCHAR2,
      transaction_id	IN INTEGER)
   RETURN INTEGER;

   FUNCTION action_change_atom_id(
      id_type		IN VARCHAR2,
      transaction_id	IN INTEGER)
   RETURN INTEGER;

   FUNCTION action_delete(
      id_type		IN VARCHAR2,
      transaction_id	IN INTEGER)
   RETURN INTEGER;

   FUNCTION action_change_status(
      id_type		IN VARCHAR2,
      transaction_id	IN INTEGER)
   RETURN INTEGER;

   FUNCTION action_change_tobereleased(
      id_type		IN VARCHAR2,
      transaction_id	IN INTEGER)
   RETURN INTEGER;

   FUNCTION action_redo(
      transaction_id	IN INTEGER,
      authority 	IN VARCHAR2,
      batch_or_macro	IN VARCHAR2,
      force    	        IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION macro_redo(
      transaction_id	IN INTEGER,
      authority 	IN VARCHAR2,
      force    	        IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION batch_redo(
      transaction_id	IN INTEGER,
      authority 	IN VARCHAR2)
   RETURN INTEGER;

   FUNCTION action_undo(
      transaction_id	IN INTEGER,
      authority 	IN VARCHAR2,
      batch_or_macro	IN VARCHAR2,
      force    	        IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION macro_undo(
      transaction_id	IN INTEGER,
      authority 	IN VARCHAR2,
      force    	        IN VARCHAR2 := 'N'
   ) RETURN INTEGER;

   FUNCTION batch_undo(
      transaction_id	IN INTEGER,
      authority 	IN VARCHAR2)
   RETURN INTEGER;

   FUNCTION action_help(
      action		IN VARCHAR2,
      id_type		IN VARCHAR2,
      authority 	IN VARCHAR2,
      table_name	IN VARCHAR2,
      work_id		IN NUMBER,
      status		IN VARCHAR2,
      new_value 	IN VARCHAR2 DEFAULT NULL,
      action_field	IN VARCHAR2 DEFAULT 'NONE',
      batch_or_macro	IN VARCHAR2)
   RETURN INTEGER;

   FUNCTION macro_action(
      action		IN VARCHAR2,
      id_type		IN VARCHAR2,
      authority 	IN VARCHAR2,
      table_name	IN VARCHAR2,
      work_id		IN NUMBER,
      status		IN VARCHAR2 := 'R',
      new_value 	IN VARCHAR2 DEFAULT NULL,
      action_field	IN VARCHAR2 DEFAULT 'NONE',
      set_preferred_flag IN VARCHAR2 DEFAULT MEME_CONSTANTS.YES)
   RETURN INTEGER;

   FUNCTION batch_action(
      action		IN VARCHAR2,
      id_type		IN VARCHAR2,
      authority 	IN VARCHAR2,
      table_name	IN VARCHAR2,
      work_id		IN NUMBER,
      status		IN VARCHAR2 := 'R',
      new_value 	IN VARCHAR2 DEFAULT NULL,
      action_field	IN VARCHAR2 DEFAULT 'NONE',
      set_preferred_flag IN VARCHAR2 DEFAULT MEME_CONSTANTS.YES)
   RETURN INTEGER;

END meme_batch_actions;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY meme_batch_actions AS

/* FUNCTION RELEASE ************************************************************
 */
FUNCTION release
RETURN INTEGER
IS
BEGIN
   version;
   RETURN TO_NUMBER(release_number);
END release;

/* FUNCTION VERSION ************************************************************
 */
FUNCTION version
RETURN FLOAT
IS
BEGIN
   version;
   RETURN TO_NUMBER(version_number);
END version;

/* PROCEDURE VERSION ***********************************************************
 */
PROCEDURE version
IS
BEGIN
   MEME_UTILITY.put_message('Package: ' || package_name);
   MEME_UTILITY.put_message('Release ' || release_number || ': ' ||
			    'version ' || version_number || ', ' ||
			    version_date || ' (' ||
			    version_authority || ')');
END version;

/* FUNCTION VERSION INFO *******************************************************
 */
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
   RETURN package_name || ' Release ' || release_number || ': ' ||
      'version ' || version_number || ' (' || version_date || ')';
END version_info;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help
IS
BEGIN
   help('');
END;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help(topic IN VARCHAR2)
IS
BEGIN
   /* This procedure requires SET SERVEROUTPUT ON */
-- DBMS_OUTPUT.ENABLE(1000000);

   /*  Print version */
   version;
END help;

/* PROCEDURE REGISTER_PACKAGE **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_BATCH_ACTIONS.release_number,
      MEME_BATCH_ACTIONS.version_number,
      SYSDATE,
      MEME_BATCH_ACTIONS.version_authority,
      MEME_BATCH_ACTIONS.package_name,
      '',
      MEME_CONSTANTS.YES,
      MEME_CONSTANTS.YES);
END register_package;

/* PROCEDURE SET TRACE ON ******************************************************
 */
PROCEDURE set_trace_on
IS
BEGIN
   mba_trace := TRUE;
END set_trace_on;

/* PROCEDURE SET TRACE OFF *****************************************************
 */
PROCEDURE set_trace_off
IS
BEGIN
   mba_trace := FALSE;
END set_trace_off;

/* PROCEDURE SET DEBUG ON ******************************************************
 */
PROCEDURE set_debug_on
IS
BEGIN
   mba_debug := TRUE;
END set_debug_on;

/* PROCEDURE SET DEBUG OFF *****************************************************
 */
PROCEDURE set_debug_off
IS
BEGIN
   mba_debug := FALSE;
END set_debug_off;

/* PROCEDURE TRACE *************************************************************
 */
PROCEDURE trace(message IN VARCHAR2)
IS
BEGIN
   IF mba_trace = TRUE THEN
      MEME_UTILITY.put_message(message);
   END IF;
END trace;

/* FUNCTION LOCAL_EXEC *********************************************************
 */
FUNCTION local_exec(query IN VARCHAR2)
RETURN INTEGER
IS
   retval   INTEGER;
BEGIN
   IF mba_trace = TRUE THEN
      MEME_UTILITY.put_message(query);
   END IF;
   IF mba_debug = FALSE THEN
      retval := MEME_UTILITY.exec(query);
      RETURN retval;
   END IF;

   RETURN 0;

END local_exec;

/* PROCEDURE INITIALIZE_TRACE **************************************************
 * This procedure initializes error parameters and set auto commit off.
 * It must be called at the begginning of every function.
 */
PROCEDURE initialize_trace(
   l_method 		IN VARCHAR2
)
IS
BEGIN

   method	:= UPPER(l_method);
   location	:= '00';
   error_code	:= 0;
   error_detail := '';
END initialize_trace;

/* PROCEDURE ERROR_LOG *********************************************************
 * This procedure set user's define errors and log every error
 * encountered in this package.
 */
PROCEDURE error_log(
   method     		IN VARCHAR2,
   location   		IN VARCHAR2,
   error_code 		IN INTEGER,
   detail     		IN VARCHAR2
)
IS
   error_msg  		VARCHAR2(100);
BEGIN

   ROLLBACK;

   IF error_code = 1 THEN
      error_msg := 'MBA0001: Unspecified error.';
   -- Table and Columns validation
   ELSIF error_code = 10 THEN
      error_msg := 'MBA0010: Table does not exist.';
   ELSIF error_code = 11 THEN
      error_msg := 'MBA0011: Table does not have an integer '||
	 SUBSTR(detail,INSTR(detail,',')+12)||' field.';
   ELSIF error_code = 13 THEN
      error_msg := 'MBA0011: Table does not have an varchar2 '||
	 SUBSTR(detail,INSTR(detail,',')+12)||' field.';
   ELSIF error_code = 12 THEN
      error_msg := 'MBA0012: Row ID is not unique.';
   -- Invalid
   ELSIF error_code = 40 THEN
      error_msg := 'MBA0040: Invalid id_type for this process.';
   ELSIF error_code = 41 THEN
      error_msg := 'MBA0041: Invalid action for this process.';
   ELSIF error_code = 42 THEN
      error_msg := 'MBA0042: Invalid field name for this process.';
   -- SQL execution
   ELSIF error_code = 50 THEN
      error_msg := 'MBA0050: DDL execution failed.';
   ELSIF error_code = 51 THEN
      error_msg := 'MBA0051: DML execution failed.';
   ELSIF error_code = 52 THEN
      error_msg := 'MBA0052: No data found.';
   ELSIF error_code = 53 THEN
      error_msg := 'MBA0053: No rows updated.';
   ELSIF error_code = 54 THEN
      error_msg := 'MBA0054: Bad count.';
   ELSIF error_code = 55 THEN
      error_msg := 'MBA0055: Count mismatch.';
   ELSIF error_code = 56 THEN
      error_msg := 'MBA0056: Bad return.';
   -- Call to MEME_UTILITY
   ELSIF error_code = 61 THEN
      error_msg := 'MBA0061: Call to MEME_UTILITY.get_value_by_code failed.';
   ELSIF error_code = 62 THEN
      error_msg := 'MBA0062: Call to MEME_UTILITY.report_table_change failed.';
   -- Call to MEME_RANKS
   ELSIF error_code = 71 THEN
      error_msg := 'MBA0071: Call to MEME_RANKS.get_status_rank failed.';
   ELSIF error_code = 72 THEN
      error_msg := 'MBA0072: Call to MEME_RANKS.get_tobereleased_rank failed.';
   ELSIF error_code = 73 THEN
      error_msg := 'MBA0073: Call to MEME_RANKS.set_preferred_id failed.';
   ELSIF error_code = 74 THEN
      error_msg := 'MBA0074: Call to MEME_RANKS.set_rank failed.';
   -- Call to MEME_APROCS
   ELSIF error_code = 81 THEN
      error_msg := 'MBA0081: Call to MEME_APROCS.bury_classes.';
   ELSIF error_code = 82 THEN
      error_msg := 'MBA0082: Call to MEME_APROCS.bury_relationships.';
   ELSIF error_code = 83 THEN
      error_msg := 'MBA0083: Call to MEME_APROCS.bury_attributes.';
   ELSIF error_code = 84 THEN
      error_msg := 'MBA0084: Call to MEME_APROCS.bury_concept_status.';
   ELSIF error_code = 85 THEN
      error_msg := 'MBA0085: Call to MEME_APROCS.bury_index.';
   ELSIF error_code = 86 THEN
      error_msg := 'MBA0086: Call to MEME_APROCS.digup_index.';
   ELSIF error_code = 88 THEN
      error_msg := 'MBA0088: Call to MEME_APROCS.redo failed.';
   ELSIF error_code = 89 THEN
      error_msg := 'MBA0089: Call to MEME_APROCS.undo failed.';
   ELSIF error_code = 90 THEN
      error_msg := 'MBA0090: Call to MEME_APROCS.bury_cxt_relationships failed.';
   ELSIF error_code = 91 THEN
      error_msg := 'MBA0091: ID type not recognized.';

   -- Call to MEME_SYSTEM
   -- Call to unimplemented process
   ELSIF error_code = 99 THEN
      error_msg := 'MBA0099: Call to unimplemented process.';
   ELSE
      error_msg := 'MBA0000: System error.';
   END IF;

   MEME_UTILITY.put_error
    	('Error in MEME_BATCH_ACTIONS.'||
	 ' Method: '||method||
    	 ' Location: '||location||
	 ' Error Code: '||error_msg||
	 ' Detail: '||detail||'('||SQLERRM||')');

END error_log;

/* FUNCTION ACTION_CHECK *******************************************************
 * This function checks for the integrity, validity and other requirements
 * of the current action.
 */
FUNCTION action_check(
   action	 	IN VARCHAR2,
   id_type	 	IN VARCHAR2,
   table_name	 	IN VARCHAR2,
   new_value	 	IN VARCHAR2,
   action_field  	IN VARCHAR2
)
RETURN INTEGER
IS
   row_count	      	INTEGER := 0;
   unique_ct	      	INTEGER := 0;
   retval	      	INTEGER;
   row_id	      	VARCHAR2(20);
   action_check_exc   	EXCEPTION;
BEGIN

   initialize_trace('ACTION_CHECK');

   error_code := 40; -- 'Invalid id_type for this process.';
   error_detail := 'action='||action||',id_type='||id_type||
      ',table_name='||table_name||',new_value='||new_value||
      ',action_field='||action_field;

   location := '10.1';
   /* Disallow action if id_type is not listed core tables */
   IF UPPER(id_type) NOT IN
      (MEME_CONSTANTS.TN_CLASSES,
       MEME_CONSTANTS.TN_RELATIONSHIPS,
       MEME_CONSTANTS.TN_ATTRIBUTES,
       MEME_CONSTANTS.TN_CONCEPT_STATUS,
       MEME_CONSTANTS.TN_SOURCE_RELATIONSHIPS,
       MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS) THEN
      RAISE action_check_exc;
   END IF;

   location := '10.2';
   IF UPPER(action) = MEME_CONSTANTS.MA_MOVE THEN
      /* Disallow action molecular move if id_type is relationships */
      IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	 RAISE action_check_exc;
      END IF;
   ELSIF UPPER(action) = MEME_CONSTANTS.AA_MOVE THEN
      /* Disallow action move if id_type is concept_status */
      IF UPPER(id_type) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
	 RAISE action_check_exc;
      END IF;
   ELSIF UPPER(action) = MEME_CONSTANTS.AA_CHANGE_ATOM_ID THEN
      /* Disallow action change_atom_id
	 if id_type is not attributes or relationships */
      IF UPPER(id_type) NOT IN (MEME_CONSTANTS.TN_ATTRIBUTES,
	 MEME_CONSTANTS.TN_RELATIONSHIPS) THEN
	 RAISE action_check_exc;
      END IF;
   END IF;

   location := '10.3';
   /* Disallow any action if action_field is null */
   IF action_field IS NULL THEN
      -- Dependency: Dynamic query in action_log
      error_code := 42;
      error_detail := 'action='||action||', id_type='||id_type;
      RAISE action_check_exc;
   END IF;


   location := '10.4';
   /* Additional requirements for current action */
   IF UPPER(action) = MEME_CONSTANTS.AA_CHANGE_STATUS THEN
      retval := MEME_RANKS.get_status_rank(id_type,new_value);
      IF retval = -1 THEN -- bad return, status 0 or positive values expected
	 location := '10.5'; error_code := 71;
	 error_detail := 'action='||action||', new_value='||new_value;
	 RAISE action_check_exc;
      END IF;
   ELSIF UPPER(action) = MEME_CONSTANTS.AA_CHANGE_TOBERELEASED THEN
      IF UPPER(id_type) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
	 location := '10.6'; error_code := 99;
	 error_detail := 'action='||action||', id_type='||id_type;
	 RAISE action_check_exc;
      END IF;
      retval := MEME_RANKS.get_tobereleased_rank(new_value);
      IF retval = -1 THEN -- bad return, status 0 or positive values expected
	 location := '10.6'; error_code := 72;
	 error_detail := 'action='||action||', new_value='||new_value;
	 RAISE action_check_exc;
      END IF;
   END IF;

   location := '20';
   /* Abort process if table does not exist */
   IF MEME_UTILITY.object_exists('table',table_name) != 1 THEN
      error_code := 10; error_detail := 'table_name='||table_name;
      RAISE action_check_exc;
   END IF;

   location := '30';
   /* Abort process if table does not have an integer row_id field */
   IF mba_debug = FALSE AND
      MEME_UTILITY.get_field_type(table_name, 'ROW_ID') != 'NUMBER' THEN
      error_code := 11;
      error_detail := 'table_name='||table_name||',field_name=row_id';
      RAISE action_check_exc;
   END IF;

   location := '40';
   IF UPPER(action) = MEME_CONSTANTS.AA_MOVE OR
      UPPER(action) = MEME_CONSTANTS.AA_CHANGE_ATOM_ID THEN
      /* Disallow action move or change_atom_id if table does not have an
	 integer new_value field */
      IF mba_debug = FALSE THEN
	 location := '40.1';
	 IF MEME_UTILITY.get_field_type(table_name, 'NEW_VALUE') != 'NUMBER' THEN
	    error_code := 11;
	    error_detail := 'table_name='||table_name||',field_name=new_value';
	    RAISE action_check_exc;
	 END IF;
	 location := '40.2';
	 IF MEME_UTILITY.get_field_type(table_name, 'OLD_VALUE') != 'NUMBER' THEN
	    error_code := 11;
	    error_detail := 'table_name='||table_name||',field_name=old_value';
	    RAISE action_check_exc;
	 END IF;
      END IF;
   END IF;

   -- For change_field actions, there must be a varchar NEW_VALUE field in the
   -- driving table
   location := '45';
   IF UPPER(action) = MEME_CONSTANTS.AA_CHANGE_FIELD THEN
      /* Change Field action must have varchar2 old_value and new_value fields */
      IF mba_debug = FALSE THEN
	 location := '45.1';
	 IF MEME_UTILITY.get_field_type(table_name, 'NEW_VALUE') != 'VARCHAR2' THEN
	    error_code := 13;
	    error_detail := 'table_name='||table_name||',field_name=new_value';
	    RAISE action_check_exc;
	 END IF;
      END IF;
   END IF;

   location := '50';
   /* Abort process if row_id in table is not unique */
   row_count := MEME_UTILITY.exec_select('SELECT COUNT(*) FROM '||table_name);
   unique_ct := MEME_UTILITY.exec_select
      ('SELECT COUNT(DISTINCT(row_id)) FROM '||table_name);
   IF row_count != unique_ct THEN
      error_code := 12; error_detail := 'table_name='||table_name;
      RAISE action_check_exc;
   END IF;

   RETURN 0;

EXCEPTION
   WHEN action_check_exc THEN
      error_log ('ACTION_CHECK', location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_CHECK', location, error_code, '');
      RETURN -1;

END action_check;

/* FUNCTION ACTION_LOG ********************************************************
 *
 * This function log every action except for action redo and undo.
 *
 */
FUNCTION action_log (
   action		IN VARCHAR2,
   id_type		IN VARCHAR2,
   authority		IN VARCHAR2,
   table_name		IN VARCHAR2,
   action_name		IN VARCHAR2,
   action_short 	IN VARCHAR2,
   work_id		IN NUMBER,
   status		IN VARCHAR2,
   new_value		IN VARCHAR2,
   action_field 	IN VARCHAR2
)
RETURN INTEGER
IS
   retval		INTEGER := 0;
   aa_row_count		INTEGER := 0;
   ma_row_count		INTEGER := 0;
   atomic_id		INTEGER := 0;
   molecule_id		INTEGER := 0;
   transaction_id	INTEGER := 0;
   timestamp		DATE := SYSDATE;
   l_join_field		VARCHAR2(30);
   l_join_table		VARCHAR2(30);
   l_temp_table		VARCHAR2(30);
   l_source_id		VARCHAR2(30);
   l_target_id		VARCHAR2(30);
   l_new_value		VARCHAR2(50);
   l_old_value		VARCHAR2(50);
   l_status		VARCHAR2(30);
   l_query		VARCHAR2(2000);

  
   l_dep_field          VARCHAR2(50);
   l_dep_table          VARCHAR2(50);

   action_log_exc	EXCEPTION;
BEGIN

   initialize_trace('BATCH_ACTION_LOG');

   MEME_SYSTEM.ANALYZE(table_name);

   --
   --  Set required ID's
   --
   location := '10';
   aa_row_count := 
	MEME_UTILITY.exec_select('SELECT COUNT(*) FROM ' || table_name);
   location := '30';
   molecule_id := 
	MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_MOLECULAR_ACTIONS);
   location := '40';
   transaction_id := 
	MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_TRANSACTIONS);

   --
   -- Lock max tab
   --
   location := '50';
   UPDATE max_tab SET max_id = max_id+1
      WHERE table_name = MEME_CONSTANTS.LTN_ATOMIC_ACTIONS;
   IF SQL%ROWCOUNT != 1 THEN
      error_code := 53; error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT;
      RAISE action_log_exc;
   END IF;

   location := '70';
   SELECT max_id INTO atomic_id
   FROM max_tab WHERE table_name = MEME_CONSTANTS.LTN_ATOMIC_ACTIONS;
   IF SQL%ROWCOUNT != 1 THEN
      error_code := 54; error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT;
      RAISE action_log_exc;
   END IF;

   --
   -- Create temporary table for the purpose of joining 
   -- to CORE tables to get the old value for action_field
   --
   -- temp_table parameter's initial value,
   -- it will vary according to actions and id_type
   --
   location := '100';
   l_join_table := cracs_table;
   l_join_field := primary_key;
   l_target_id := '0';
   l_source_id := 'ct.concept_id';
   l_old_value := 'ct.' || action_field;
   l_new_value := '''' || new_value || '''';
   l_status := ''''|| status ||'''';

   --
   -- Mapped variables according to action
   --
   location := '110';
   IF UPPER(action) = MEME_CONSTANTS.AA_INSERT THEN
      -- use the source table
      l_join_table := 
	MEME_UTILITY.get_value_by_code('S'||UPPER(id_type),'table_name');
      l_new_value := '''N''';
      l_old_value := '''Y''';
   ELSIF UPPER(action) = MEME_CONSTANTS.AA_DELETE THEN
      l_new_value := '''Y''';
      l_old_value := '''N''';
   ELSIF UPPER(action) = MEME_CONSTANTS.AA_CHANGE_FIELD THEN
      location := '100.1';
      l_new_value := ' tn.new_value ';
      -- If the action field is date or number
      l_old_value := ' to_char(ct.' || action_field || ') ';
      -- if the action field is varchar2 already
      IF MEME_UTILITY.get_field_type(
		tab_name=>cracs_table, 
		col_name=>action_field ) = 'VARCHAR2' THEN
	l_old_value := ' ct.' || action_field;
      END IF;

   ELSIF UPPER(action) = MEME_CONSTANTS.AA_MOVE THEN
      l_target_id := 'new_value';
      l_new_value := ' tn.new_value ';
      l_old_value := ' tn.old_value ';
   ELSIF UPPER(action) = MEME_CONSTANTS.AA_CHANGE_ATOM_ID THEN
      l_new_value := ' tn.new_value ';
      l_old_value := ' tn.old_value ';
   END IF;

   --
   -- Mapped variables according to id_type
   --
   location := '120';
   IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS OR
      UPPER(id_type) = MEME_CONSTANTS.TN_SOURCE_RELATIONSHIPS OR
      UPPER(id_type) = MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS THEN
      l_source_id := 'ct.concept_id_1';
   END IF;

   location := '125';
   l_temp_table := 't_log_'||transaction_id;
   MEME_UTILITY.drop_it('table',l_temp_table);

   --
   -- Create driving table
   --
   location := '130';
   EXECUTE IMMEDIATE
	'CREATE TABLE ' || l_temp_table || '
          (row_id, source_id, target_id, new_value, old_value,
           status, authority, timestamp, atomic_action_id, molecule_id, 
	   table_name) AS
         SELECT c.row_id, c.row_id, c.row_id, 
      		' || l_new_value || ',' || l_old_value || ', 
	   c.status, c.authority, c.timestamp, c.atomic_action_id, 
	   c.molecule_id, c.table_name 
  	 FROM ' || l_join_table || ' ct, ' || table_name || ' tn,
              atomic_actions c
         WHERE 1=0
   	 UNION ALL
	 SELECT tn.row_id, 
      		' || l_source_id || ',' || l_target_id || ',
      		' || l_new_value || ',' || l_old_value || ', 
      		NVL(' || l_status || ',''R''),
	        ''' || authority || ''', to_date(''' || timestamp || '''),
      		rownum + ' || atomic_id || ',
      		rownum + ' || molecule_id || ', ''' || id_type || '''
  	 FROM ' || l_join_table || ' ct, ' || table_name || ' tn
         WHERE tn.row_id = ct.' || l_join_field;

   --
   -- Compute dependencies here and load atomic_actions rows.
   -- 1. If deleting atoms, remove connected A,R,CR
   -- 2. If deleting rels, remove connected A
   -- 3. If deleting cxt rels, remove connected A
   -- 4. If changing atom concept_id, change connected A, R, CR
   --
   IF id_type = 'C' AND action = 'D' THEN
      --
      -- attach corresponding attributes
      --
      location := '130.1';
      EXECUTE IMMEDIATE
	'INSERT INTO ' || l_temp_table || '
           (row_id, source_id, target_id, new_value, old_value,
            status, authority, timestamp, atomic_action_id, molecule_id,table_name)
         SELECT b.attribute_id, a.source_id, a.target_id,
      		' || l_new_value || ',' || l_old_value || ', 
      		NVL(' || l_status || ',''R''),
	        ''' || authority || ''', ''' || timestamp || ''',
      		rownum + (select max(atomic_action_id) from ' || l_temp_table || '),
      		a.molecule_id, ''A''
  	 FROM ' || l_temp_table || ' a, attributes b
         WHERE row_id = atom_id AND table_name = ''C''
	   AND attribute_level = ''S''';

      aa_row_count := aa_row_count + SQL%ROWCOUNT;

      --
      -- attach corresponding relationships
      --
      location := '130.2';
      EXECUTE IMMEDIATE
	'INSERT INTO ' || l_temp_table || '
           (row_id, source_id, target_id, new_value, old_value,
            status, authority, timestamp, atomic_action_id, molecule_id,table_name)
         SELECT b.relationship_id, a.source_id, a.target_id,
      		' || l_new_value || ',' || l_old_value || ', 
      		NVL(' || l_status || ',''R''),
	        ''' || authority || ''', ''' || timestamp || ''',
      		rownum + (select max(atomic_action_id) from ' || l_temp_table || '),
      		a.molecule_id,''R''
  	 FROM ' || l_temp_table || ' a, relationships b
         WHERE row_id in (atom_id_1,atom_id_2) AND table_name = ''C''
	   AND relationship_level = ''S''';

      aa_row_count := aa_row_count + SQL%ROWCOUNT;

      --
      -- attach corresponding context relationships
      --
      location := '130.3';
      EXECUTE IMMEDIATE
	'INSERT INTO ' || l_temp_table || '
           (row_id, source_id, target_id, new_value, old_value,
            status, authority, timestamp, atomic_action_id, molecule_id,table_name)
         SELECT b.relationship_id, a.source_id, a.target_id,
      		' || l_new_value || ',' || l_old_value || ', 
      		NVL(' || l_status || ',''R''),
	        ''' || authority || ''', ''' || timestamp || ''',
      		rownum + (select max(atomic_action_id) from ' || l_temp_table || '),
      		a.molecule_id,''CR''
  	 FROM ' || l_temp_table || ' a, context_relationships b
         WHERE row_id in (atom_id_1,atom_id_2) AND table_name = ''C''
	   AND relationship_level = ''S''';

      aa_row_count := aa_row_count + SQL%ROWCOUNT;

   ELSIF id_type = 'R' AND action = 'D' THEN
      --
      -- attach corresponding attributes
      --
      location := '130.4';
      EXECUTE IMMEDIATE
	'INSERT INTO ' || l_temp_table || '
           (row_id, source_id, target_id, new_value, old_value,
            status, authority, timestamp, atomic_action_id, molecule_id,table_name)
         SELECT attribute_id, source_id, target_id, 
      		' || l_new_value || ',' || l_old_value || ', 
      		NVL(' || l_status || ',''R''),
	        ''' || authority || ''', ''' || timestamp || ''',
      		rownum + (select max(atomic_action_id) from ' || l_temp_table || '),
      		molecule_id,''A''  FROM
	(SELECT DISTINCT a.molecule_id, b.attribute_id, a.source_id, a.target_id
  	 FROM ' || l_temp_table || ' a, attributes b, relationships c
         WHERE row_id = c.relationship_id AND table_name = ''R''
	   AND attribute_level = ''S''
	   AND relationship_level = ''S''
  	   AND b.sg_meme_id = relationship_id
           AND b.sg_meme_data_type = ''R''
	   AND b.atom_id in atom_id_1) ';

      aa_row_count := aa_row_count + SQL%ROWCOUNT;

   ELSIF id_type = 'CR' AND action = 'D' THEN
      --
      -- attach corresponding attributes
      -- 
      location := '130.5';
      EXECUTE IMMEDIATE
	'INSERT INTO ' || l_temp_table || '
           (row_id, source_id, target_id, new_value, old_value,
            status, authority, timestamp, atomic_action_id, molecule_id,table_name)
         SELECT attribute_id, source_id, target_id, 
      		' || l_new_value || ',' || l_old_value || ', 
      		NVL(' || l_status || ',''R''),
	        ''' || authority || ''', ''' || timestamp || ''',
      		rownum + (select max(atomic_action_id) from ' || l_temp_table || '),
      		molecule_id,''A'' FROM
	(SELECT DISTINCT a.molecule_id, b.attribute_id, a.source_id, a.target_id
  	 FROM ' || l_temp_table || ' a, attributes b, context_relationships c
         WHERE row_id = c.relationship_id AND table_name = ''R''
	   AND attribute_level = ''S''
	   AND relationship_level = ''S''
  	   AND b.sg_meme_id = relationship_id
           AND b.sg_meme_data_type = ''R''
	   AND b.atom_id in atom_id_1) ';

      aa_row_count := aa_row_count + SQL%ROWCOUNT;

   END IF;

   location := '140';
   retval := MEME_UTILITY.exec_count(l_temp_table);
   IF retval != aa_row_count THEN -- count mismatch, equal to row_count expected
      error_code := 55;
      error_detail := 'retval='||retval||',aa_row_count='||aa_row_count;
      RAISE action_log_exc;
   END IF;

   --
   -- Must update max_tab to reserve id ranges; check 
   -- that ids have not been updated
   -- during sequence creation
   --
   location := '50';
   UPDATE max_tab SET max_id = max_id + aa_row_count
      WHERE table_name = MEME_CONSTANTS.LTN_ATOMIC_ACTIONS;
   IF SQL%ROWCOUNT != 1 THEN
      error_code := 53; error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT;
      RAISE action_log_exc;
   END IF;

   IF action_name LIKE 'MOLECULAR%' THEN
      ma_row_count := MEME_UTILITY.exec_select('SELECT count(distinct molecule_id) FROM ' || l_temp_table);
      location := '60';
      UPDATE max_tab SET max_id = max_id + ma_row_count
      WHERE table_name = MEME_CONSTANTS.LTN_MOLECULAR_ACTIONS;

      IF SQL%ROWCOUNT != 1 THEN
	 error_code := 53; error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT;
	 RAISE action_log_exc;
      END IF;
   END IF;


   --
   -- Mapped log batch atomic_action query
   --
   location := '150';
   l_query := 
      	'INSERT INTO atomic_actions 
           (molecule_id, atomic_action_id, action, table_name, row_id,
            new_version_id, old_version_id, old_value, new_value, authority,
            timestamp, status, action_field)';

   location := '160';
   IF action_name LIKE 'MOLECULAR%' THEN
      l_query := l_query || ' SELECT molecule_id, atomic_action_id, ';
   ELSIF action_name LIKE 'MACRO%' THEN
      l_query := l_query || ' SELECT '||molecule_id||', atomic_action_id, ';
   END IF;

   location := '170';
   l_query := l_query ||
      '''' || action_short || ''',' || 
      ' table_name, row_id, 0, 0, old_value, new_value, ' ||
      '''' || authority || ''', ' ||
      '''' || timestamp || ''', status, ' ||
      '''' || action_field || '''' ||
      ' FROM ' || l_temp_table;

   --
   -- Logged batch atomic_action
   --
   location := '180';
   retval := local_exec(l_query);
   IF retval != aa_row_count THEN -- count mismatch, equal to aa_row_count expected
      error_code := 55;
      error_detail := 'retval='||retval||',aa_row_count='||aa_row_count;
      RAISE action_log_exc;
   END IF;

   location := '190';
   l_query := 'INSERT INTO molecular_actions 
       (transaction_id, molecule_id, authority, timestamp,
        molecular_action, source_id, target_id, undone, undone_by,
        undone_when, status, elapsed_time, work_id ) ';

   --
   -- Logged batch molecular_action
   --
   IF action_name LIKE 'MOLECULAR%' THEN
      location := '200';
      l_query := l_query||
	 'SELECT DISTINCT ' || transaction_id || ', molecule_id, 
	         ''' || authority || ''', 
	         ''' || timestamp || ''',
	         ''' || action_name || ''',
	         source_id, target_id, ''N'', '''', '''',
	         status, 0, ' || work_id || ' 
          FROM ' || l_temp_table;

      retval := local_exec(l_query);
      IF retval != ma_row_count THEN 
         -- count mismatch, equal to ma_row_count expected
	 error_code := 55;
	 error_detail := 'retval='||retval||',ma_row_count='||ma_row_count;
	 RAISE action_log_exc;
      END IF;

   ELSIF action_name LIKE 'MACRO%' THEN
  
      location := '210';
      l_query := l_query ||
	 ' VALUES ( ' || transaction_id || ',' || molecule_id || ',
	            ''' || authority || ''',
	            ''' || timestamp || ''',
	            ''' || action_name || ''',
	            0, 0, ''N'', '''', '''',
	            ''' || status || ''',
	            0, ' || work_id || ')';

      retval := local_exec(l_query);
      IF retval != 1 THEN -- bad return, single row insert expected
	 error_code := 51; error_detail := 'retval='||retval;
	 RAISE action_log_exc;
      END IF;
   END IF;

   location := '220';
   COMMIT;
   MEME_UTILITY.drop_it('table',l_temp_table);

   RETURN transaction_id;

EXCEPTION
   WHEN action_log_exc THEN
      error_log ('ACTION_LOG', location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',l_temp_table);
      error_log ('ACTION_LOG', location, error_code, '');
      RETURN -1;

END action_log;

/* FUNCTION MACRO_INSERT *******************************************************
 * Preconditions:
 *	Target source_* table and support tables preloaded with correct data
 *	Max_tab.<row_id> ranger reserved
 *	Macro actions rows inserted into action tables
 *
 * Postconditions:
 *	Ids loaded into source-id_map if non-zero
 *	Data inserted into target core table
 *	Rank set in target core table
 *	Preferred_atom_id set in CS if inserting into C
 *
 *	Word indexes will be handled correctly elsewhere if inserting into C
 *	(string_ui normwrd word_index normstr)
 *
 *	Report table changes
 *
 */
FUNCTION macro_insert(
   id_type	    	IN VARCHAR2,
   authority	    	IN VARCHAR2,
   transaction_id   	INTEGER
)
RETURN INTEGER
IS
   retval	    	INTEGER := 0;
   row_count	    	INTEGER := 0;
   row_count2	    	INTEGER := 0;
   timestamp	    	DATE := SYSDATE;

   loop_ctr	    	INTEGER;
   loop_table	    	DBMS_SQL.VARCHAR2_TABLE;

   l_temp_table     	VARCHAR2(50);
   l_source_table   	VARCHAR2(50);
	
   l_query	    	VARCHAR2(2000);
   l_state_flag     	VARCHAR2(2) DEFAULT MEME_CONSTANTS.NO;
   macro_insert_exc 	EXCEPTION;

   TYPE ct IS REF CURSOR;
   cv		    	ct;
   col		    	VARCHAR2(30);
   col_list	    	VARCHAR2(2000);

   CURSOR sca_cur IS SELECT DISTINCT concept_id FROM source_classes_atoms;
   sca_rec 		sca_cur%ROWTYPE;

BEGIN

   initialize_trace('MACRO_INSERT');

   l_source_table :=
	 MEME_UTILITY.get_value_by_code('S'||UPPER(id_type),'table_name');

   IF UPPER(id_type) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN

      location := '10';
      SELECT COUNT(*) INTO row_count FROM source_concept_status
      WHERE switch = 'R';

      location := '10.1';
      INSERT /*+ APPEND */ INTO concept_status
	    (concept_id, version_id, status, dead, authority,
	    timestamp, insertion_date, preferred_atom_id,
	    released, tobereleased, last_molecule_id,
	    last_atomic_action_id, rank, editing_authority,
	    editing_timestamp, approval_molecule_id)
	 SELECT concept_id, 0, a.status, 'N', macro_insert.authority,
	    timestamp, timestamp, 0, released, tobereleased, 0, 0,
	    MEME_RANKS.get_rank(concept_id,
	    MEME_CONSTANTS.TN_SOURCE_CONCEPT_STATUS),
	    macro_insert.authority, timestamp, 0
	 FROM source_concept_status a, molecular_actions m
	 WHERE m.transaction_id = macro_insert.transaction_id
	 AND switch = 'R';

      IF SQL%ROWCOUNT != row_count THEN
	 error_code := 55;
	 error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT||'; row_count='||row_count;
	 RAISE macro_insert_exc;
      END IF;

   ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_CLASSES THEN

      location := '20';
      SELECT COUNT(*) INTO row_count FROM source_classes_atoms
      WHERE switch = 'R';

      location := '20.1';
      INSERT /*+ APPEND */ INTO classes
	    (atom_id, version_id, source, termgroup,
	    termgroup_rank, code, sui, lui, generated_status,
	    last_release_cui, dead, status, authority, timestamp,
	    insertion_date, concept_id, released, tobereleased,
	    last_molecule_id, last_atomic_action_id, sort_key,
	    rank, last_release_rank, suppressible,
	    last_assigned_cui, isui, tty, aui, language,
	    source_aui, source_cui, source_dui)
	 SELECT atom_id, 0, source, a.termgroup, termgroup_rank, code, sui,
	    lui, generated_status, last_release_cui, 'N', a.status,
	    macro_insert.authority, timestamp, timestamp, concept_id,
	    released, tobereleased, 0, 0, a.sort_key,
	    to_number(
	        DECODE(a.tobereleased,'Y','9','y','7','n','3','1')
	         || lpad(a.termgroup_rank,4,0)||'0'),
	    a.last_release_rank, a.suppressible, a.last_assigned_cui,
	    a.isui, tty, aui, nvl(language,'ENG'),
	    source_aui, source_cui, source_dui
	 FROM source_classes_atoms a, molecular_actions c
	 WHERE transaction_id = macro_insert.transaction_id
	 AND switch = 'R';

      IF SQL%ROWCOUNT != row_count THEN
	 error_code := 55;
	 error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT||'; row_count='||row_count;
	 RAISE macro_insert_exc;
      END IF;

      /* set preferred atom_id */

      location := '20.2';
      INSERT /*+ APPEND */ INTO atoms (atom_id, version_id, atom_name)
	 SELECT atom_id, 0, atom_name
	 FROM source_classes_atoms
	 WHERE switch = 'R';

      IF SQL%ROWCOUNT != row_count THEN
	 error_code := 55;
	 error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT||'; row_count='||row_count;
	 RAISE macro_insert_exc;
      END IF;

   ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_ATTRIBUTES THEN

      location := '30';
      SELECT COUNT(*) INTO row_count FROM source_attributes
      WHERE switch = 'R';
COMMIT;

      location := '30.1';
      INSERT INTO attributes
	    (atom_id, version_id, attribute_id, attribute_level,
	    attribute_name, attribute_value, generated_status, source,
	    dead, status, authority, timestamp, insertion_date,
	    concept_id, released,tobereleased, source_rank,
	    preferred_level, last_molecule_id, last_atomic_action_id,
	    rank, suppressible, sg_id, sg_type, sg_qualifier,
	    atui, source_atui, hashcode)
	 SELECT atom_id, 0, attribute_id, attribute_level, attribute_name,
	    attribute_value, generated_status, source, 'N', a.status,
	    macro_insert.authority, timestamp, timestamp, concept_id,
	    released, tobereleased, source_rank, '', 0, 0,
	    to_number(DECODE(a.tobereleased,'Y','9','n','3','1')||lpad(a.source_rank,4,0)||'0'), 
	    a.suppressible, sg_id, sg_type, sg_qualifier,
	    atui, source_atui, hashcode
	 FROM source_attributes a, molecular_actions c
	 WHERE transaction_id = macro_insert.transaction_id
	   AND switch = 'R';

      IF SQL%ROWCOUNT != row_count THEN
	 error_code := 55;
	 error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT||'; row_count='||row_count;
	 RAISE macro_insert_exc;
      END IF;

      location := '30.3';
      SELECT COUNT(*) INTO row_count FROM source_attributes
      WHERE attribute_value LIKE '<>Long_Attribute<>:%';

      location := '30.4';
      SELECT COUNT(DISTINCT(string_id)) INTO row_count2 FROM source_stringtab;

      IF row_count2 != row_count THEN
	 error_code := 55;
	 error_detail := 'row_count2='||row_count2|| '; row_count='||row_count;
	 RAISE macro_insert_exc;
      END IF;

      location := '30.5';
      INSERT /*+ APPEND */ INTO stringtab SELECT * FROM source_stringtab;

   ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN

      location := '40';
      SELECT COUNT(*) INTO row_count FROM source_relationships
      WHERE switch = 'R';

      location := '40.1';
      INSERT /*+ APPEND */ INTO relationships
	    (relationship_id, version_id, relationship_level, atom_id_1,
	    relationship_name, relationship_attribute, atom_id_2,
	    source, generated_status, dead, status,
	    authority, timestamp, insertion_date, concept_id_1,
	    concept_id_2, released, tobereleased, source_rank,
	    preferred_level, last_molecule_id, last_atomic_action_id,
	    rank, source_of_label, suppressible,
	    sg_id_1, sg_type_1, sg_qualifier_1, 
	    sg_id_2, sg_type_2, sg_qualifier_2,
	    rui, source_rui, relationship_group)
	 SELECT relationship_id, 0, relationship_level, atom_id_1,
	    relationship_name, relationship_attribute, atom_id_2,
	    source, generated_status, 'N', a.status,
	    macro_insert.authority, timestamp, timestamp, concept_id_1,
	    concept_id_2, released, tobereleased, source_rank, '', 0, 0,
	    to_number(DECODE(a.tobereleased,'Y','9','n','3','1')||lpad(a.source_rank,4,0)||'0'), 
	    a.source_of_label, a.suppressible, 
	    sg_id_1, sg_type_1, sg_qualifier_1, 
	    sg_id_2, sg_type_2, sg_qualifier_2,
	    rui, source_rui, relationship_group
	 FROM source_relationships a, molecular_actions c
	 WHERE transaction_id = macro_insert.transaction_id
 	   AND switch = 'R';

      IF SQL%ROWCOUNT != row_count THEN
	 error_code := 55;
	 error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT||'; row_count='||row_count;
	 RAISE macro_insert_exc;
      END IF;

   ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS THEN

      location := '50';
      SELECT COUNT(*) INTO row_count FROM source_context_relationships
      WHERE switch = 'R';

      location := '50.1';
      -- Rank field is not maintained in context_relationships.
      INSERT /*+ APPEND */ INTO context_relationships
	    (relationship_id, version_id, relationship_level, atom_id_1,
	    relationship_name, relationship_attribute, atom_id_2, source,
	    generated_status, dead, status, authority, timestamp,
	    insertion_date, concept_id_1, concept_id_2, released,
	    tobereleased, source_rank, preferred_level, last_molecule_id,
	    last_atomic_action_id, rank, source_of_label, suppressible,
            hierarchical_code, parent_treenum, release_mode, rui,
	    sg_id_1, sg_type_1, sg_qualifier_1, 
	    sg_id_2, sg_type_2, sg_qualifier_2,
	    source_rui, relationship_group)
	 SELECT relationship_id, 0, relationship_level, atom_id_1,
	    relationship_name, relationship_attribute, atom_id_2,
	    source, generated_status, 'N', a.status,
	    macro_insert.authority, c.timestamp, c.timestamp,
	    concept_id_1, concept_id_2, released, tobereleased,
	    source_rank, '', 0, 0, 0,
	    a.source_of_label, a.suppressible,
            a.hierarchical_code, a.parent_treenum, a.release_mode,
            a.rui,
	    sg_id_1, sg_type_1, sg_qualifier_1, 
	    sg_id_2, sg_type_2, sg_qualifier_2,
	    source_rui, a.relationship_group
	 FROM source_context_relationships a, molecular_actions c
	 WHERE transaction_id = macro_insert.transaction_id
	   AND switch = 'R';

      IF SQL%ROWCOUNT != row_count THEN
	 error_code := 55;
	 error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT||'; row_count='||row_count;
	 RAISE macro_insert_exc;
      END IF;

   ELSE
      location := '60';
      error_code := 40; error_detail := 'id_type='||id_type;
      RAISE macro_insert_exc;
   END IF;

   location := '70';


   RETURN 0;

EXCEPTION
   WHEN macro_insert_exc THEN
      error_log ('MACRO_INSERT', location, error_code, SQLERRM);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('MACRO_INSERT', location, error_code, '');
      RETURN -1;

END macro_insert;


/* FUNCTION ACTION_CHANGE_FIELD ************************************************
 * This function change the value of field
 */
FUNCTION action_change_field(
   id_type		  IN VARCHAR2,
   transaction_id	  IN INTEGER,
   action_field 	  IN VARCHAR2
)
RETURN INTEGER
IS
   retval		  INTEGER := 0;
   row_count		  INTEGER := 0;
   cfstring		  VARCHAR2(20) :='';
   l_update		  VARCHAR2(2000);
   l_ui			  VARCHAR2(20);
   action_cf_exc	  EXCEPTION;

   CURSOR aa_cur(id IN INTEGER) IS

   SELECT m.source_id, m.target_id, m.molecule_id,
	 a.row_id, a.old_value, a.new_value, a.timestamp, a.authority,
	 a.atomic_action_id
   FROM atomic_actions a, molecular_actions m
   WHERE a.molecule_id = m.molecule_id
     AND m.transaction_id = id
     AND a.action = 'CF' 
     AND a.table_name = id_type;

   aa_rec aa_cur%ROWTYPE;

BEGIN

   initialize_trace('ACTION_CHANGE_FIELD');

    location := '5';
    -- Certain fields are not allowed to change
    IF lower(action_field) like 'sg_%' OR
	lower(action_field) in ('tobereleased','status') THEN
	error_detail := 'Changing tobereleaesd, status, sg_type, sg_id, or sg_qualifier fields is not allowed.';
	RAISE action_cf_exc;
    END IF;

   /* calculate conversion function */
   IF MEME_UTILITY.get_field_type(cracs_table,action_field) = 'NUMBER' THEN
      cfstring := 'TO_NUMBER';
   END IF;

   location := '20';
   OPEN aa_cur(transaction_id);
   LOOP
      FETCH aa_cur INTO aa_rec;
      EXIT WHEN aa_cur%NOTFOUND;

      location := '20.1';
      l_update := 
 	'UPDATE ' || cracs_table || '
	    SET ' || action_field || ' = ' || cfstring || '(''' || aa_rec.new_value || '''),
		timestamp = ''' || aa_rec.timestamp || ''',
		authority = ''' || aa_rec.authority || ''',
	    	last_atomic_action_id = ' || aa_rec.atomic_action_id || ',
		last_molecule_id = ' || aa_rec.molecule_id || '
	 WHERE ' || primary_key || ' = ' || aa_rec.row_id || '
	   AND (' || action_field || ' = ' || cfstring || '(''' || aa_rec.old_value || ''') OR
	        NVL(' || action_field || ',''null'') = ''null'' ) ';
 
      retval := local_exec(l_update);

      IF retval != 1 THEN -- bad return, single row update expected
	 error_code := 53; error_detail := 'SQL%ROWCOUNT='||SQL%ROWCOUNT||',query='||l_update;
	 RAISE action_cf_exc;
      END IF;

      /* call set rank */
      location := '20.2';
      retval := MEME_RANKS.set_rank(aa_rec.row_id,cracs_table,action_field);
      IF retval != 0 THEN -- bad return, status 0 expected
	 error_code := 74; error_detail := 'retval='||retval;
	 RAISE action_cf_exc;
      END IF;

      location := '20.3';
      /* check that preferred atom_id has changed */
      IF UPPER(id_type) = MEME_CONSTANTS.TN_CLASSES AND
	 msp_set_preferred_flag = MEME_CONSTANTS.YES THEN
	 location := '20.31';
	 retval := MEME_RANKS.set_preferred_id(aa_rec.source_id,action_field);
	 IF retval != 0 THEN -- bad return, status 0 expected
	    error_code := 73; error_detail := 'retval='||retval;
	    RAISE action_cf_exc;
	 END IF;

	 IF aa_rec.target_id != 0 THEN
	    location := '20.32';
	    retval := MEME_RANKS.set_preferred_id(aa_rec.old_value,action_field);
	    IF retval != 0 THEN -- bad return, status 0 expected
	       error_code := 73; error_detail := 'retval='||retval;
	       RAISE action_cf_exc;
	    END IF;
	 END IF;
      END IF;

      IF UPPER(id_type) = MEME_CONSTANTS.TN_CLASSES AND
         LOWER(action_field) IN ('source','termgroup','tty','code','sui',
				'source_aui','source_cui','source_dui') THEN
      	  location := '20.32';
	  l_ui := MEME_APROCS.assign_aui(atom_id => aa_rec.row_id);
      	  location := '20.33';
	  UPDATE classes
	  SET aui = l_ui
    	  WHERE atom_id = aa_rec.row_id
	    AND nvl(aui,'null') != l_ui; 
      END IF;
      location := '20.4';
      IF UPPER(id_type) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
	IF LOWER(action_field) IN ('atom_id','concept_id','attribute_level',
			       'source','attribute_name','source_atui') THEN
      	    location := '20.41';
	    l_ui := MEME_APROCS.assign_atui(attribute_id => aa_rec.row_id);
      	    location := '20.42';
	    UPDATE attributes
	    SET atui = l_ui
    	    WHERE attribute_id = aa_rec.row_id
	      AND nvl(atui,'null') != l_ui; 
 	END IF;
      END IF;

      location := '20.5';
      IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	IF LOWER(action_field) IN ('atom_id','concept_id',
				'relationship_name','relationship_attribute',
			       'source','source_rui') THEN
      	    location := '20.51';
	    l_ui := MEME_APROCS.assign_rui(relationship_id => aa_rec.row_id);
      	    location := '20.52';
	    UPDATE relationships
	    SET rui = l_ui
    	    WHERE relationship_id = aa_rec.row_id
	      AND nvl(rui,'null') != nvl(l_ui,'null'); 
 	END IF;

	IF LOWER(action_field) IN ('relationship_level') THEN
      	    location := '20.51';
	    l_ui := MEME_APROCS.assign_rui(relationship_id => aa_rec.row_id);
      	    location := '20.52';
      	      UPDATE relationships 
              SET (rui, sg_id_1, sg_type_1, sg_qualifier_1, 
	 	        sg_id_2, sg_type_2, sg_qualifier_2 ) =
	        (SELECT rui, sg_id_1, sg_type_1, sg_qualifier_1, 
	 	             sg_id_2, sg_type_2, sg_qualifier_2
	         FROM relationships_ui WHERE rui = l_ui)
      	      WHERE relationship_id = aa_rec.row_id 	
	        AND nvl(rui,'null') != nvl(l_ui,'null');
 	END IF;
      END IF;
      row_count := row_count + 1;
   END LOOP;
   CLOSE aa_cur;

   RETURN row_count;

EXCEPTION
   WHEN action_cf_exc THEN
      error_log ('ACTION_CHANGE_FIELD', location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_CHANGE_FIELD', location, error_code, '');
      RETURN -1;

END action_change_field;

/* FUNCTION ACTION_MOVE ********************************************************
 * This function move concept_ids
 */
FUNCTION action_move(
   id_type	  	IN VARCHAR2,
   transaction_id 	IN INTEGER
)
RETURN INTEGER
IS
   retval	  	INTEGER := 0;
   row_count	  	INTEGER :=0;
   loop_count	  	INTEGER :=0;
   loop_ctr	  	INTEGER :=0;

   l_field_name   	VARCHAR2(50);
   l_field_concept_id  	VARCHAR2(30);
   l_field_sg_id   	VARCHAR2(30);
   l_field_sg_type   	VARCHAR2(30);

   l_ui		  	VARCHAR2(20);

   action_c_exc   	EXCEPTION;

   CURSOR aa_cur(id IN INTEGER) IS
   SELECT * FROM atomic_actions a
   WHERE molecule_id  IN (SELECT molecule_id FROM molecular_actions WHERE transaction_id = id)
     AND a.action = 'C' 
     AND a.table_name = id_type;
   aa_rec 		aa_cur%ROWTYPE;

BEGIN

   initialize_trace('ACTION_MOVE');

   location := '10'; /* Re-validate id_type for this action */

   OPEN aa_cur(transaction_id);
   LOOP
      FETCH aa_cur INTO aa_rec;
      EXIT WHEN aa_cur%NOTFOUND;

      loop_count := 0;
      location := '10.1';

      location := '10.3';
      FOR loop_ctr IN 1..2 LOOP
	 IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS OR
	    UPPER(id_type) = MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS THEN
	    l_field_concept_id := MEME_CONSTANTS.FN_CONCEPT_ID||'_'||loop_ctr;
	    l_field_sg_type := 'sg_type_'||loop_ctr;
	    l_field_sg_id := 'sg_id_'||loop_ctr;
	 ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
	    l_field_concept_id := MEME_CONSTANTS.FN_CONCEPT_ID;
	    l_field_sg_type := 'sg_type';
	    l_field_sg_id := 'sg_id';
	    IF loop_ctr > 1 THEN
	       EXIT;
	    END IF;
	 ELSE
	    l_field_concept_id := MEME_CONSTANTS.FN_CONCEPT_ID;
	    l_field_sg_type := '''CONCEPT_ID''';
	    l_field_sg_id := 'concept_id';
	    IF loop_ctr > 1 THEN
	       EXIT;
	    END IF;
	 END IF;

	IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS OR
	    UPPER(id_type) = MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS OR
	    UPPER(id_type) = MEME_CONSTANTS.TN_ATTRIBUTES THEN

           location := '10.35';
	   EXECUTE IMMEDIATE
	     'UPDATE ' || cracs_table || '
	      SET ' || l_field_concept_id || ' = :x,
	         ' || l_field_sg_id || ' = 
		DECODE(' || l_field_sg_type || ', 
			''CONCEPT_ID'', :x, ' || l_field_sg_id || '),
	         timestamp = :x,
	         authority = :x,
	         last_atomic_action_id = :x,
	         last_molecule_id = :x
	       WHERE ' || primary_key || ' = :x
	         AND ' || l_field_concept_id || ' = :x '
	    USING to_number(aa_rec.new_value),
	      aa_rec.new_value,
	      aa_rec.timestamp,
	      aa_rec.authority,
	      aa_rec.atomic_action_id,
	      aa_rec.molecule_id,
	      aa_rec.row_id,
	      to_number(aa_rec.old_value);

	ELSE

           location := '10.36';
	   EXECUTE IMMEDIATE
	     'UPDATE ' || cracs_table || '
	      SET ' || l_field_concept_id || ' = :x,
	         timestamp = :x,
	         authority = :x,
	         last_atomic_action_id = :x,
	         last_molecule_id = :x
	       WHERE ' || primary_key || ' = :x
	         AND ' || l_field_concept_id || ' = :x '
	    USING to_number(aa_rec.new_value),
	      aa_rec.timestamp,
	      aa_rec.authority,
	      aa_rec.atomic_action_id,
	      aa_rec.molecule_id,
	      aa_rec.row_id,
	      to_number(aa_rec.old_value);

	END IF;

	loop_count := loop_count + SQL%ROWCOUNT;
      END LOOP;

      -- @ this point, loop_count could not be 0,
      -- 1 or 2 for relationships, otherwise it should be 1
      location := '10.4';
      IF loop_count = 0 THEN
	 error_code := 53; error_detail := 'loop_count='||loop_count;
	 RAISE action_c_exc;
      END IF;

      row_count := row_count + 1;

      location := '10.31';
      IF UPPER(id_type) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
	    location := '10.32';
	    l_ui := MEME_APROCS.assign_atui(attribute_id => aa_rec.row_id);
	    location := '10.33';
	    UPDATE attributes
	    SET atui = l_ui
    	    WHERE attribute_id = aa_rec.row_id
	      AND nvl(atui,'null') != l_ui; 
      END IF;

      location := '15.1';
      IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	    location := '15.2';
	    l_ui := MEME_APROCS.assign_rui(relationship_id => aa_rec.row_id);
	    location := '15.3';
	    UPDATE relationships
	    SET rui = l_ui
    	    WHERE relationship_id = aa_rec.row_id
	      AND nvl(rui,'null') != nvl(l_ui,'null'); 
      END IF;

      IF UPPER(id_type) = MEME_CONSTANTS.TN_CLASSES THEN

	 /* move source level atts and rels */
	 /* cannot predict count -> cannot check count or
	    if concept_id changed. */

	 location := '20';
	 UPDATE attributes
	    SET concept_id = TO_NUMBER(aa_rec.new_value)
	    WHERE atom_id = aa_rec.row_id
	    AND attribute_level != 'C';

	 location := '20.2';
	 UPDATE relationships
	    SET concept_id_1 = TO_NUMBER(aa_rec.new_value)
	    WHERE atom_id_1 = aa_rec.row_id
	    AND relationship_level IN  (MEME_CONSTANTS.SOURCE_LEVEL,
		 			MEME_CONSTANTS.PROCESSED_LEVEL);

	 location := '20.3';
	 UPDATE relationships
	    SET concept_id_2 = TO_NUMBER(aa_rec.new_value)
	    WHERE atom_id_2 = aa_rec.row_id
	    AND relationship_level IN  (MEME_CONSTANTS.SOURCE_LEVEL,
		 			MEME_CONSTANTS.PROCESSED_LEVEL);

	 /* need to reset preferred atom for each concept;
	    could optimize by sorting */

	 IF msp_set_preferred_flag = MEME_CONSTANTS.YES THEN
	    location := '20.5';
	    retval := MEME_RANKS.set_preferred_id(TO_NUMBER(aa_rec.new_value));
	    IF retval != 0 THEN -- bad return, status 0 expected
	       error_code := 73; error_detail := 'retval='||retval;
	       RAISE action_c_exc;
	    END IF;

	    location := '20.6';
	    retval := MEME_RANKS.set_preferred_id(TO_NUMBER(aa_rec.old_value));
	    IF retval != 0 THEN -- bad return, status 0 expected
	       error_code := 73; error_detail := 'retval='||retval;
	       RAISE action_c_exc;
	    END IF;

	 END IF;

      END IF;

   END LOOP;
   CLOSE aa_cur;

   RETURN row_count;

EXCEPTION
   WHEN action_c_exc THEN
      error_log ('ACTION_MOVE', location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_MOVE', location, error_code, '');
      RETURN -1;

END action_move;

/* FUNCTION ACTION_CHANGE_ATOM_ID **********************************************
 * This function change atom_ids
 */
FUNCTION action_change_atom_id(
   id_type	  	IN VARCHAR2,
   transaction_id 	IN INTEGER
)
RETURN INTEGER
IS
   retval	  	INTEGER := 0;
   row_count	  	INTEGER := 0;
   loop_count	  	INTEGER := 0;
   loop_ctr	  	INTEGER;
/* Dynamic  */
   l_update	 	VARCHAR2(2000);
   l_field_name   	VARCHAR2(50);
   l_field_concept_id  	VARCHAR2(30);
   l_field_sg_id   	VARCHAR2(30);
   l_field_sg_type   	VARCHAR2(30);
   l_level	  	VARCHAR2(30);
   l_ui		  	VARCHAR2(20);

   action_a_exc   	EXCEPTION;

   CURSOR aa_cur(id IN INTEGER) IS
   SELECT * 
   FROM atomic_actions a
   WHERE molecule_id IN
     (SELECT molecule_id FROM molecular_actions WHERE transaction_id = id)
     AND a.action = 'A' 
     AND a.table_name = id_type;
   aa_rec 		aa_cur%ROWTYPE;

BEGIN

   initialize_trace('ACTION_CHANGE_ATOM_ID');

   location := '10';

   OPEN aa_cur(transaction_id);
   LOOP
      FETCH aa_cur INTO aa_rec;
      EXIT WHEN aa_cur%NOTFOUND;
	
      loop_count := 0;
      location := '10.2';
      FOR loop_ctr IN 1..2 LOOP
	 IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	    l_field_name := MEME_CONSTANTS.FN_ATOM_ID||'_'||loop_ctr;
	    l_field_concept_id := MEME_CONSTANTS.FN_CONCEPT_ID||'_'||loop_ctr;
	    l_field_sg_id := 'sg_id_'||loop_ctr;
	    l_field_sg_type := 'sg_type_'||loop_ctr;
	    l_level := 'relationship_level';
	 ELSE
	    l_field_name := MEME_CONSTANTS.FN_ATOM_ID;
	    l_field_concept_id := MEME_CONSTANTS.FN_CONCEPT_ID;
	    l_field_sg_id := 'sg_id';
	    l_field_sg_type := 'sg_type';
	    l_level := 'attribute_level';
	    IF loop_ctr > 1 THEN
	       EXIT;
	    END IF;
	 END IF;
	 location := '10.2.1';
	 EXECUTE IMMEDIATE
	    'UPDATE ' || cracs_table || '
	     SET  (' || l_field_name || ', ' 
	  	     || l_field_concept_id || ', ' 
		     || l_field_sg_id || ',
		  authority, timestamp, last_molecule_id,
		  last_atomic_action_id) =
	  	(SELECT :x, concept_id, 
		   DECODE(' || l_field_sg_type || ',''AUI'',aui,' || l_field_sg_id || '),
		   :x, :x, :x, :x
 	         FROM classes WHERE atom_id = :x)
	     WHERE ' || primary_key || ' = :x
	       AND ' || l_field_name || ' = :x '
 	 USING to_number(aa_rec.new_value),
	       aa_rec.authority,
	       aa_rec.timestamp,
	       aa_rec.molecule_id,
	       aa_rec.atomic_action_id,
	       to_number(aa_rec.new_value),
	       aa_rec.row_id,
	       to_number(aa_rec.old_value);

	 loop_count := loop_count + SQL%ROWCOUNT;

      END LOOP;

      -- @ this point, loop_count could not be 0.  It should be 1 or 2.
      location := '10.4';
      IF loop_count = 0 THEN
	 error_code := 53; error_detail := 'loop_count='||loop_count;
	 RAISE action_a_exc;
      END IF;

      -- increment row counter
      row_count := row_count + 1;
      location := '10.51';
      IF UPPER(id_type) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
          location := '10.52';
	  l_ui := MEME_APROCS.assign_atui(attribute_id => aa_rec.row_id);
          location := '10.53';
	  UPDATE attributes
	  SET atui = l_ui
    	  WHERE attribute_id = aa_rec.row_id
	    AND nvl(atui,'null') != l_ui; 
      END IF;

      location := '15.51';
      IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
          location := '15.52';
	  l_ui := MEME_APROCS.assign_rui(relationship_id => aa_rec.row_id);
          location := '15.53';
	  UPDATE relationships
	  SET rui = l_ui
    	  WHERE relationship_id = aa_rec.row_id
	    AND nvl(rui,'null') != nvl(l_ui,'null'); 
      END IF;
   END LOOP;

   MEME_UTILITY.put_message('action_change_atom_id: ' || aa_cur%ROWCOUNT ||
			' rows processed.');

   CLOSE aa_cur;

   RETURN row_count;

EXCEPTION
   WHEN action_a_exc THEN
      error_log ('ACTION_CHANGE_ATOM_ID', location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_CHANGE_ATOM_ID', location, error_code, error_detail);
      RETURN -1;

END action_change_atom_id;

/* FUNCTION ACTION_DELETE ******************************************************
 * This function move concepts to dead
 */
FUNCTION action_delete(
   id_type	    	IN VARCHAR2,
   transaction_id   	IN INTEGER
)
RETURN INTEGER
IS
   retval		INTEGER := 0;
   row_count		INTEGER := 0;
   l_update		VARCHAR2(2000);

   action_d_exc 	EXCEPTION;

   CURSOR aa_cur(id IN INTEGER) IS
   SELECT m.source_id, m.target_id, m.molecule_id,
	 a.row_id, a.old_value, a.new_value, a.timestamp, a.authority,
	 a.atomic_action_id
   FROM atomic_actions a, molecular_actions m
   WHERE a.molecule_id = m.molecule_id AND transaction_id = id
     AND a.action = 'D'
     AND a.table_name = id_type;
   aa_rec 		aa_cur%ROWTYPE;

BEGIN

   initialize_trace('ACTION_DELETE');

   location := '0.1';
   OPEN aa_cur(transaction_id);
   LOOP
      location := '0.2';
      FETCH aa_cur INTO aa_rec;
      EXIT WHEN aa_cur%NOTFOUND;

      location := '0.3';
      l_update := 
     	'UPDATE ' || cracs_table || ' SET 
	  dead = ''Y'',
	  timestamp = ''' || aa_rec.timestamp || ''',
	  authority = ''' || aa_rec.authority || ''',
	  last_atomic_action_id = ' || aa_rec.atomic_action_id || ',
	  last_molecule_id = ' || aa_rec.molecule_id || '
	 WHERE ' || primary_key || ' = ' || aa_rec.row_id;

      EXECUTE IMMEDIATE l_update;
      retval := SQL%ROWCOUNT;
      IF retval != 1 THEN -- bad return, single row update expected
	 location := '10.0';
	 error_code := 53; 
	 error_detail := 'retval='||retval||
		',id_type='||id_type||',row_id='||aa_rec.row_id;
	 RAISE action_d_exc;
      END IF;

      row_count := row_count + 1;

      IF UPPER(id_type) = MEME_CONSTANTS.TN_CLASSES THEN
	 -- Reset preferred atom id
	 IF  msp_set_preferred_flag = MEME_CONSTANTS.YES THEN
	     location := '10.1'; error_code := 73;
	     retval := MEME_RANKS.set_preferred_id(aa_rec.source_id,'dead');
  	     IF retval != 0 THEN -- bad return, status 0 expected
	   	 error_detail := 'retval='||retval;
	         RAISE action_d_exc;
	     END IF;
 	 END IF;

	 location := '10.2'; error_code := 81;
	 retval := MEME_APROCS.bury_classes(aa_rec.row_id);
      ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS THEN
	 location := '15.1'; error_code := 90;
	 retval := MEME_APROCS.bury_cxt_relationships(aa_rec.row_id);
      ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
	 location := '20.1'; error_code := 82;
	 retval := MEME_APROCS.bury_relationships(aa_rec.row_id);
      ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
	 location := '30.1'; error_code := 83;
	 retval := MEME_APROCS.bury_attributes(aa_rec.row_id);
      ELSIF UPPER(id_type) = MEME_CONSTANTS.TN_CONCEPT_STATUS THEN
	 location := '40.1'; error_code := 84;
	 retval := MEME_APROCS.bury_concept_status(aa_rec.row_id);
      ELSE
	 location := '50'; error_code := 91;
 	 error_detail := 'id_type='||id_type;
      END IF;

      location := '60';
      IF retval != 0 THEN -- bad return, status 0 expected
	 error_detail := 'retval='||retval;
	 RAISE action_d_exc;
      END IF;

   END LOOP;

   RETURN row_count;

EXCEPTION
   WHEN action_d_exc THEN
      error_log ('ACTION_DELETE', location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_DELETE', location, error_code, '');
      RETURN -1;

END action_delete;

/* FUNCTION ACTION_CHANGE_STATUS ***********************************************
 * This function change the values of field status
 */
FUNCTION action_change_status(
   id_type		   IN VARCHAR2,
   transaction_id	   IN INTEGER
)
RETURN INTEGER
IS
   retval		   INTEGER := 0;
   row_count		   INTEGER := 0;

   l_update		   VARCHAR2(2000);

   action_s_exc 	   EXCEPTION;

   CURSOR aa_cur(id IN INTEGER) IS
   SELECT * 
   FROM atomic_actions a
   WHERE molecule_id IN 
	 (SELECT molecule_id FROM molecular_actions WHERE transaction_id = id)
     AND a.action = 'S' 
     AND a.table_name = id_type;
   aa_rec 			aa_cur%ROWTYPE;

BEGIN

   initialize_trace('ACTION_CHANGE_STATUS');

   OPEN aa_cur(transaction_id);
   LOOP
      FETCH aa_cur INTO aa_rec;
      EXIT WHEN aa_cur%NOTFOUND;

      l_update := 'UPDATE '||cracs_table||' SET '||
	 'status = '||''''||aa_rec.new_value||''''||','||
	 'timestamp = '||''''||aa_rec.timestamp||''''||','||
	 'authority = '||''''||aa_rec.authority||''''||','||
	 'last_atomic_action_id = '||aa_rec.atomic_action_id||','||
	 'last_molecule_id = '||aa_rec.molecule_id||
	 ' WHERE '||primary_key||' = '||aa_rec.row_id||
	 ' AND status = '||''''||aa_rec.old_value||'''';

      --MEME_UTILITY.put_message(method||','||location||'='||l_update);

      retval := local_exec(l_update);
      IF retval != 1 THEN -- bad return, single row update expected
	 location := '10.1';
	 error_code := 53; error_detail := 'retval='||retval;
	 RAISE action_s_exc;
      END IF;

      row_count := row_count + 1;
   END LOOP;

   RETURN row_count;

EXCEPTION
   WHEN action_s_exc THEN
      error_log ('ACTION_CHANGE_STATUS', location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_CHANGE_STATUS', location, error_code, '');
      RETURN -1;

END action_change_status;

/* FUNCTION ACTION_CHANGE_TOBERELEASED *****************************************
 * This function change the values of field tobereleased
 */
FUNCTION action_change_tobereleased(
   id_type		IN VARCHAR2,
   transaction_id	IN INTEGER
)
RETURN INTEGER
IS
   retval		INTEGER := 0;
   row_count		INTEGER := 0;

   l_update		VARCHAR2(2000);
   l_ui		  	VARCHAR2(20);

   action_t_exc 	EXCEPTION;

   CURSOR aa_cur(id IN INTEGER) IS
   SELECT m.source_id, m.target_id, m.molecule_id,
	 a.row_id, a.old_value, a.new_value, a.timestamp, a.authority,
	 a.atomic_action_id
   FROM atomic_actions a, molecular_actions m
   WHERE a.molecule_id = m.molecule_id AND transaction_id = id
     AND a.action = 'T'
     AND a.table_name = id_type;
   aa_rec 		aa_cur%ROWTYPE;

BEGIN

   initialize_trace('ACTION_CHANGE_TOBERELEASED');

   OPEN aa_cur(transaction_id);
   LOOP
      FETCH aa_cur INTO aa_rec;
      EXIT WHEN aa_cur%NOTFOUND;

      l_update := 
	'UPDATE ' || cracs_table || ' 
	 SET tobereleased = ''' || aa_rec.new_value || ''',
	     timestamp = ''' || aa_rec.timestamp || ''',
	     authority = ''' || aa_rec.authority || ''',
	     last_atomic_action_id = ' || aa_rec.atomic_action_id || ',
	     last_molecule_id = ' || aa_rec.molecule_id || '
	 WHERE ' || primary_key || ' = ' || aa_rec.row_id || '
	   AND tobereleased = ''' || aa_rec.old_value || '''';

      --MEME_UTILITY.put_message(method||','||location||'='||l_update);

      retval := local_exec(l_update);
      IF retval != 1 THEN -- bad return, single row update expected
	 location := '10.1';
	 error_code := 53; error_detail := 'retval='||retval;
	 RAISE action_t_exc;
      END IF;

      IF UPPER(id_type) = MEME_CONSTANTS.TN_CLASSES AND
	 msp_set_preferred_flag = MEME_CONSTANTS.YES THEN
	 retval := MEME_RANKS.set_preferred_id(aa_rec.source_id,'tobereleased');
	 IF retval != 0 THEN -- bad return, status 0 expected
	    location := '10.2';
	    error_code := 73; error_detail := 'retval='||retval;
	    RAISE action_t_exc;
	 END IF;
      END IF;

      retval := MEME_RANKS.set_rank(aa_rec.row_id,cracs_table,'tobereleased');
      IF retval != 0 THEN -- bad return, status 0 expected
	 location := '10.3';
	 error_code := 74; error_detail := 'retval='||retval;
	 RAISE action_t_exc;
      END IF;

      IF UPPER(id_type) = MEME_CONSTANTS.TN_CLASSES THEN
	 IF aa_rec.new_value = 'N' AND aa_rec.old_value != 'N' THEN
	    retval := MEME_APROCS.bury_index(aa_rec.row_id);
	    IF retval != 0 THEN -- bad return, status 0 expected
	       location := '10.4';
	       error_code := 85; error_detail := 'retval='||retval;
	       RAISE action_t_exc;
	    END IF;
	 END IF;
	 IF aa_rec.new_value != 'N' AND aa_rec.old_value = 'N' THEN
	    retval := MEME_APROCS.digup_index(aa_rec.row_id);
	    IF retval != 0 THEN -- bad return, status 0 expected
	       location := '10.5';
	       error_code := 86; error_detail := 'retval='||retval;
	       RAISE action_t_exc;
	    END IF;
	 END IF;
      END IF;

      -- Compute ATUI,RTUI if the tbr went from N,n to Y,y
      IF aa_rec.old_value in ('N','n') AND aa_rec.new_value in ('Y','y') THEN

          location := '10.50';
          IF UPPER(id_type) = MEME_CONSTANTS.TN_CLASSES THEN
              location := '10.505';
	      l_ui := MEME_APROCS.assign_aui(atom_id => aa_rec.row_id);
              location := '10.506';
      	      UPDATE classes SET aui = l_ui
      	      WHERE atom_id = aa_rec.row_id 	
	        AND nvl(aui,'null') != nvl(l_ui,'null');
          END IF;

          location := '10.51';
          IF UPPER(id_type) = MEME_CONSTANTS.TN_ATTRIBUTES THEN
              location := '10.52';
	      l_ui := MEME_APROCS.assign_atui(attribute_id => aa_rec.row_id);
              location := '10.53';
      	      UPDATE attributes SET (atui,sg_id,sg_type,sg_qualifier) =
	        (SELECT atui, sg_id, sg_type, sg_qualifier
	         FROM attributes_ui WHERE atui = l_ui)
      	      WHERE attribute_id = aa_rec.row_id 	
	        AND nvl(atui,'null') != l_ui;
          END IF;

          location := '10.54';
          IF UPPER(id_type) = MEME_CONSTANTS.TN_RELATIONSHIPS THEN
              location := '10.55';
	      l_ui := MEME_APROCS.assign_rui(relationship_id => aa_rec.row_id);
              location := '10.56';
      	      UPDATE relationships 
              SET (rui, sg_id_1, sg_type_1, sg_qualifier_1, 
	 	        sg_id_2, sg_type_2, sg_qualifier_2 ) =
	        (SELECT rui, sg_id_1, sg_type_1, sg_qualifier_1, 
	 	             sg_id_2, sg_type_2, sg_qualifier_2
	         FROM relationships_ui WHERE rui = l_ui)
      	      WHERE relationship_id = aa_rec.row_id 	
	        AND nvl(rui,'null') != nvl(l_ui,'null');
          END IF;

      END IF;

      row_count := row_count + 1;

   END LOOP;
   CLOSE aa_cur;

   RETURN row_count;

EXCEPTION
   WHEN action_t_exc THEN
      error_log ('ACTION_CHANGE_TOBERELEASED',location,error_code,error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_CHANGE_TOBERELEASED',location,error_code,'');
      RETURN -1;

END action_change_tobereleased;


/* FUNCTION ACTION_REDO ********************************************************
 * This is a common function of macro_redo and batch_redo. The process redo
 * previously undone transaction.
 */
FUNCTION action_redo(
   transaction_id	IN INTEGER,
   authority		IN VARCHAR2,
   batch_or_macro	IN VARCHAR2,
   force    		IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval		INTEGER := 0;
   row_count		INTEGER := 0;
   loop_ctr		INTEGER;
   l_molecule_id	INTEGER;
   l_mid_event_id	INTEGER;

   l_action_field	VARCHAR2(50);
   l_table_name 	VARCHAR2(50);
   l_temp_table 	VARCHAR2(50);
 
   action_redo_exc	EXCEPTION;

   CURSOR ma_cur(t_id IN INTEGER) IS
   SELECT molecule_id FROM molecular_actions
     WHERE transaction_id = t_id ORDER BY 1 ASC;

   CURSOR aa_cur(m_id IN INTEGER) IS
   SELECT * FROM atomic_actions
     WHERE molecule_id = m_id ORDER BY atomic_action_id ASC;
   aa_rec 		aa_cur%ROWTYPE;

BEGIN

   initialize_trace('ACTION_REDO');

   OPEN ma_cur(transaction_id);
   LOOP
      FETCH ma_cur INTO l_molecule_id;
      EXIT WHEN ma_cur%NOTFOUND;

      l_mid_event_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_MOLECULAR_ACTIONS);

      location := '10';
      OPEN aa_cur(l_molecule_id);
      LOOP
	 FETCH aa_cur INTO aa_rec;
	 EXIT WHEN aa_cur%NOTFOUND;
	 retval := MEME_APROCS.aproc_redo(
	    atomic_action_id => aa_rec.atomic_action_id,
	    authority => authority,
	    force => force);
	 location := '10.1';
	 IF retval != 0 THEN -- bad return, status 0 expected
	    error_code := 89;
	    error_detail := 'atomic_action_id= '||aa_rec.atomic_action_id;
	    RAISE action_redo_exc;
	 END IF;
	 l_action_field := aa_rec.action_field;
	 l_table_name := aa_rec.table_name;
      END LOOP;
      CLOSE aa_cur;

      -- The value of cracs_table and primary_key mapped in this function
      cracs_table := MEME_UTILITY.get_value_by_code(l_table_name,'table_name');
      primary_key := MEME_UTILITY.get_value_by_code(cracs_table,'primary_key');

      location := '20';
      UPDATE molecular_actions
      SET undone = 'N', undone_by = '', undone_when = ''
      WHERE molecule_id = l_molecule_id;

      row_count := row_count + 1;

   END LOOP;
   CLOSE ma_cur;

   RETURN row_count;

EXCEPTION
   WHEN action_redo_exc THEN
      error_log ('ACTION_REDO:'||batch_or_macro, location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_REDO:'||batch_or_macro, location, error_code, '');
      RETURN -1;

END action_redo;

/* FUNCTION MACRO_REDO *********************************************************
 * This function perform macro redo of transaction.
 */
FUNCTION macro_redo(
   transaction_id	IN INTEGER,
   authority		IN VARCHAR2,
   force    		IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval		INTEGER := 0;

   restore_flag 	VARCHAR2(1);

BEGIN

   IF MEME_UTILITY.ddl_commit_mode = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_off;
      restore_flag := MEME_CONSTANTS.YES;
   END IF;

   retval := action_redo(
		transaction_id => transaction_id, 
		authority => authority, 
		batch_or_macro => MEME_CONSTANTS.MACRO_ACTION,
		force => force);

   IF restore_flag = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_on;
   END IF;

   RETURN retval;

END macro_redo;

/* FUNCTION BATCH_REDO *********************************************************
 * This function perform batch redo of transaction.
 */
FUNCTION batch_redo(
   transaction_id	IN INTEGER,
   authority		IN VARCHAR2
)
RETURN INTEGER
IS
   retval		INTEGER := 0;

   restore_flag 	VARCHAR2(1);

BEGIN

   IF MEME_UTILITY.ddl_commit_mode = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_off;
      restore_flag := MEME_CONSTANTS.YES;
   END IF;

   retval := action_redo(
		transaction_id => transaction_id, 
		authority => authority, 
		batch_or_macro => MEME_CONSTANTS.BATCH_ACTION);

   IF restore_flag = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_on;
   END IF;

   RETURN retval;

END batch_redo;

/* FUNCTION ACTION_UNDO ********************************************************
 * This is a common function of macro_undo and batch_undo. The process undo
 * requested transaction.
 */
FUNCTION action_undo(
   transaction_id	IN INTEGER,
   authority		IN VARCHAR2,
   batch_or_macro	IN VARCHAR2,
   force    	        IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval		INTEGER := 0;
   row_count		INTEGER := 0;
   l_molecule_id	INTEGER;
   l_mid_event_id	INTEGER;

   l_action_field 	VARCHAR2(50);
   l_table_name 	VARCHAR2(50);
   l_temp_table 	VARCHAR2(50);

   action_undo_exc	EXCEPTION;

   CURSOR ma_cur(t_id IN INTEGER) IS
   SELECT molecule_id FROM molecular_actions
     WHERE transaction_id = t_id ORDER BY 1 DESC;

   CURSOR aa_cur(m_id IN INTEGER) IS
   SELECT * FROM atomic_actions
     WHERE molecule_id = m_id ORDER BY atomic_action_id DESC;
   aa_rec 		aa_cur%ROWTYPE;

BEGIN

   initialize_trace('ACTION_UNDO');

   OPEN ma_cur(transaction_id);
   LOOP
      FETCH ma_cur INTO l_molecule_id;
      EXIT WHEN ma_cur%NOTFOUND;

      l_mid_event_id := MEME_UTILITY.get_next_id(MEME_CONSTANTS.LTN_MOLECULAR_ACTIONS);

      location := '10';
      OPEN aa_cur(l_molecule_id);
      LOOP
	 FETCH aa_cur INTO aa_rec;
	 EXIT WHEN aa_cur%NOTFOUND;

	 location := '10.1';
	 retval := MEME_APROCS.aproc_undo(
	    atomic_action_id => aa_rec.atomic_action_id,
	    authority => authority,
	    force => force);
	 IF retval != 0 THEN -- bad return, status 0 expected
	    error_code := 89;
	    error_detail := 'atomic_action_id= '||aa_rec.atomic_action_id;
	    RAISE action_undo_exc;
	 END IF;
	 l_action_field := aa_rec.action_field;
	 l_table_name := aa_rec.table_name;
      END LOOP;
      CLOSE aa_cur;

      -- The value of cracs_table and primary_key mapped in this function
      cracs_table := MEME_UTILITY.get_value_by_code(l_table_name,'table_name');
      primary_key := MEME_UTILITY.get_value_by_code(cracs_table,'primary_key');

      location := '20';
      UPDATE molecular_actions
      SET undone = 'Y',
	  undone_by = action_undo.authority,
	  undone_when = SYSDATE
      WHERE molecule_id = l_molecule_id;

      row_count := row_count + 1;

   END LOOP;
   CLOSE ma_cur;

   RETURN row_count;

EXCEPTION
   WHEN action_undo_exc THEN
      error_log ('ACTION_UNDO:'||batch_or_macro, location, error_code, error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_UNDO:'||batch_or_macro, location, error_code, '');
      RETURN -1;

END action_undo;

/* FUNCTION MACRO_UNDO *********************************************************
 * This function perform macro undo of transaction.
 */
FUNCTION macro_undo(
   transaction_id	IN INTEGER,
   authority		IN VARCHAR2,
   force    		IN VARCHAR2 := 'N'
)
RETURN INTEGER
IS
   retval	    	INTEGER := 0;

   restore_flag     	VARCHAR2(1);

BEGIN

   IF MEME_UTILITY.ddl_commit_mode = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_off;
      restore_flag := MEME_CONSTANTS.YES;
   END IF;

   initialize_trace('MACRO_UNDO');

   location := '10';
   retval := action_undo(
		transaction_id => transaction_id, 
		authority => authority, 
		batch_or_macro => MEME_CONSTANTS.MACRO_ACTION,
		force => force);

   IF restore_flag = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_on;
   END IF;

   RETURN retval;

END macro_undo;

/* FUNCTION BATCH_UNDO *********************************************************
 * This function perform batch undo of transaction.
 */
FUNCTION batch_undo(
   transaction_id	IN INTEGER,
   authority		IN VARCHAR2
)
RETURN INTEGER
IS
   retval	    	INTEGER := 0;

   restore_flag     	VARCHAR2(1);

BEGIN

   IF MEME_UTILITY.ddl_commit_mode = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_off;
      restore_flag := MEME_CONSTANTS.YES;
   END IF;

   initialize_trace('BATCH_UNDO');

   location := '10';
   retval := action_undo(
		transaction_id => transaction_id, 
		authority => authority, 
		batch_or_macro => MEME_CONSTANTS.BATCH_ACTION);

   IF restore_flag = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_on;
   END IF;

   RETURN retval;

END batch_undo;

/* FUNCTION ACTION_HELP ********************************************************
 * This is a common function of macro_action and batch_action, perform as the
 * brain of this package.
 *
 * 1. The "process" started by dynamically mapped all the action parameters
 *    according to action and id_type.
 * 2. If action is insert, the process use temporary table which record selected
 *    from the source table.  Otherwise, the process use the parameter table.
 *    (abort process if table is empty).
 * 3. Call action_check for process integrity, validation and process requirements
 * 4. Log the action to record its molecular and atomic action.
 * 5. Call function that perform the actual action.
 * 6. If action is macro (not for insert), it call report table change.
 * 7. Finally, log completed action.
 *
 */
FUNCTION action_help(
   action	    	IN VARCHAR2,		 -- [molecular, atomic, macro]
   id_type	    	IN VARCHAR2,		 -- [C,R,A,CS]
   authority	    	IN VARCHAR2,
   table_name	    	IN VARCHAR2,
   work_id	    	IN NUMBER,
   status	    	IN VARCHAR2,
   new_value	    	IN VARCHAR2 DEFAULT NULL,
   action_field     	IN VARCHAR2 DEFAULT 'NONE',
   batch_or_macro	IN VARCHAR2
)
RETURN INTEGER
IS

   l_dummy	    	INTEGER := 0;
   retval	    	INTEGER := 0;
   row_count	    	INTEGER := 0;
   transaction_id   	INTEGER;
   l_molecule_id    	INTEGER;
   l_error_code     	INTEGER	 := 0;

   l_query	    	VARCHAR2(1024);
   l_temp_table     	VARCHAR2(50) := NULL;
   l_source_table   	VARCHAR2(50) := NULL;
   l_new_table	    	VARCHAR2(50) := NULL;
   l_table_name     	VARCHAR2(50) := NULL;
   l_action_code    	VARCHAR2(50) := NULL;
   l_action_name    	VARCHAR2(50) := NULL;
   l_action_field   	VARCHAR2(50) := NULL;
   l_report_change  	VARCHAR2(1)  := MEME_CONSTANTS.YES;
   l_location	    	VARCHAR2(10) := '00';
   l_error_detail   	VARCHAR2(250):= '';

   action_help_exc  	EXCEPTION;

BEGIN

   /* No need to call initialize trace,
      this function use local error log parameters */

   MEME_UTILITY.timing_start;

   -- Set error_code to error in mapping
   l_location := '10'; l_error_code := 61; 
   l_error_detail := 'action='||action||',id_type='||id_type||',table_name='||
      table_name||',new_value='||new_value||',action_field='||action_field;

   /* Map action parameters */

   -- Map action to a batch action code
   l_location := '10.1';
   l_action_code := MEME_UTILITY.get_value_by_code(action,'batch_action_code');
   --MEME_UTILITY.put_message('l_action_code='||l_action_code);

   -- Get long action name (e.g. MOLECULAR_MERGE)
   l_location := '10.2';
   l_action_name := MEME_UTILITY.get_value_by_code(l_action_code,LOWER(batch_or_macro));
   --MEME_UTILITY.put_message('l_action_name='||l_action_name);

   -- Get action_field (it should be set for change field actions)
   l_location := '10.3';
   IF l_action_code = MEME_CONSTANTS.AA_CHANGE_FIELD THEN
      l_action_field := action_field;
   ELSE
      l_action_field := MEME_UTILITY.get_value_by_code(l_action_code,'action_field');
   END IF;
   --MEME_UTILITY.put_message('l_action_field='||l_action_field);

   -- Get the name of the core table being updated
   l_location := '10.4';
   cracs_table := MEME_UTILITY.get_value_by_code(UPPER(id_type),'table_name');

   -- Get the primary key of the table being updated
   l_location := '10.5';
   IF UPPER(id_type) = MEME_CONSTANTS.TN_CONTEXT_RELATIONSHIPS THEN
      primary_key := MEME_CONSTANTS.FN_RELATIONSHIP_ID;
   ELSE
      primary_key := MEME_UTILITY.get_value_by_code(cracs_table,'primary_key');
   END IF;

   l_table_name := table_name;

   l_location := '100'; 
   l_error_code := 0; -- Set error_code to system error

   IF l_action_code = MEME_CONSTANTS.AA_INSERT THEN
      l_location := '100.10';
      l_new_table := MEME_UTILITY.get_unique_tablename;
      MEME_UTILITY.drop_it('table',l_new_table);

      l_source_table :=
	 MEME_UTILITY.get_value_by_code('S'||UPPER(id_type),'table_name');

      l_query := 'CREATE TABLE '||l_new_table||' (row_id) AS'||
	 ' SELECT '||primary_key||' FROM '||l_source_table||
	 ' WHERE switch = '||'''R''';

      --MEME_UTILITY.put_message(method||','||l_location||'='||l_query);

      retval := local_exec(l_query);

      -- overwrite current values
      l_table_name := l_new_table;
   END IF;

   /* Abort process if table is empty */
   l_location := '120'; 
   row_count := MEME_UTILITY.exec_count(l_table_name);
   IF row_count < 1 THEN
      MEME_UTILITY.put_message('Table '||l_table_name||' is empty.');
      RETURN 0;
   END IF;

   /* Restrictions */
   l_location := '130'; 
   retval := action_check
      (action, id_type, l_table_name, new_value, l_action_field);

   IF retval != 0 THEN -- bad return, status 0 expected
      l_location := '200'; l_error_code := 56;
      RAISE action_help_exc;
   END IF;

   transaction_id := -1;

   /* Logged action */
   l_location := '220'; 
   transaction_id := action_log(
	      	action => l_action_code,
		id_type => action_help.id_type,
		authority => action_help.authority,
		table_name => l_table_name,
		action_name => l_action_name,	
		action_short => l_action_code,
		work_id => action_help.work_id,
		status => action_help.status,
		new_value => action_help.new_value,
		action_field => l_action_field );

   IF transaction_id = -1 THEN -- bad return, status 0 or positive values expected
      l_location := '250'; l_error_code := 56;
      RAISE action_help_exc;
   END IF;

   MEME_UTILITY.put_message('transaction_id='||transaction_id);
 
   --
   -- Perform action
   --
   l_location := '310'; 
   IF l_action_code = MEME_CONSTANTS.AA_DELETE THEN
      retval := action_delete(id_type, transaction_id);

      --
      -- Connected actions if id_type is C,R,CR
      --
      IF id_type = 'C' THEN
	IF retval != -1 THEN
	    cracs_table := 'attributes';
	    primary_key := 'attribute_id';
	    l_dummy := action_delete('A',transaction_id);
	    cracs_table := 'relationships';
	    primary_key := 'relationship_id';
	    l_dummy := action_delete('R',transaction_id);
	    cracs_table := 'context_relationships';
	    primary_key := 'relationship_id';
	    l_dummy := action_delete('CR',transaction_id);
	    cracs_table := 'classes';
	    primary_key := 'atom_id';
	END IF;
      ELSIF id_type IN ('R') THEN
	IF retval != -1 THEN
	    cracs_table := 'attributes';
	    primary_key := 'attribute_id';
	    l_dummy := action_delete('A',transaction_id);
	    cracs_table := 'relationships';
	    primary_key := 'relationship_id';
	END IF;
      ELSIF id_type IN ('CR') THEN
	IF retval != -1 THEN
	    cracs_table := 'attributes';
	    primary_key := 'attribute_id';
	    l_dummy := action_delete('A',transaction_id);
	    cracs_table := 'context_relationships';
	    primary_key := 'relationship_id';
	END IF;
      END IF;
   ELSIF l_action_code = MEME_CONSTANTS.AA_CHANGE_STATUS THEN
      retval := action_change_status(id_type, transaction_id);
   ELSIF l_action_code = MEME_CONSTANTS.AA_CHANGE_TOBERELEASED THEN
      retval := action_change_tobereleased(id_type, transaction_id);
   ELSIF l_action_code = MEME_CONSTANTS.AA_CHANGE_FIELD THEN
      retval := action_change_field(id_type, transaction_id, l_action_field);
   ELSIF l_action_code = MEME_CONSTANTS.AA_MOVE THEN
      retval := action_move(id_type, transaction_id);
   ELSIF l_action_code = MEME_CONSTANTS.AA_CHANGE_ATOM_ID THEN
      retval := action_change_atom_id(id_type, transaction_id);
   ELSIF l_action_code = MEME_CONSTANTS.AA_INSERT THEN
      l_report_change := MEME_CONSTANTS.NO;
      IF batch_or_macro = MEME_CONSTANTS.BATCH_ACTION THEN
	 retval := macro_insert(id_type, authority, transaction_id);
      ELSIF batch_or_macro = MEME_CONSTANTS.MACRO_ACTION THEN
	 retval := macro_insert(id_type, authority, transaction_id);
      END IF;
   END IF;

   IF retval = -1 THEN -- bad return, status 0 or positive values expected
      l_location := '275'; l_error_code := 56;
      RAISE action_help_exc;
   END IF;

   l_location := '300';

   MEME_UTILITY.timing_stop;

   l_location := '400';
   retval := MEME_UTILITY.exec_select('SELECT COUNT(*) FROM molecular_actions '||
      'WHERE transaction_id = ' || transaction_id);

   l_location := '500';
   retval := local_exec('UPDATE molecular_actions SET elapsed_time = '||
      (MEME_UTILITY.elapsed_time / retval) || ' '||
      'WHERE transaction_id = ' || transaction_id );

   COMMIT; -- The only commit in this package

   -- macro_insert location 90.6 was creating errors
   -- when meme_utility.oq_mode was on when it tried to drop
   -- its helper tables.  Instead, we just try to drop them here
   -- after the commit.
   MEME_UTILITY.drop_it('table','t_rtc_'||transaction_id||'1');
   MEME_UTILITY.drop_it('table','t_rtc_'||transaction_id||'2');
   MEME_UTILITY.drop_it('table','t_rtc_'||transaction_id||'3');
   MEME_UTILITY.drop_it('table','t_rtc_'||transaction_id||'4');
   MEME_UTILITY.drop_it('table','t_rmc_'||l_molecule_id);

   MEME_UTILITY.log_operation (
	authority => authority,
	activity => method || ' action',
	detail => action || ': ' || 'id_type=' || id_type || ', action_field=' || action_field,
	transaction_id => transaction_id,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.elapsed_time);

   MEME_UTILITY.put_message
      ('Action '||l_action_name||' successfully completed ('||retval||').');

   COMMIT; 

   RETURN transaction_id;

EXCEPTION
   WHEN action_help_exc THEN
      error_log
      ('ACTION_HELP:'||batch_or_macro, l_location, l_error_code, l_error_detail);
      RETURN -1;
   WHEN OTHERS THEN
      error_log ('ACTION_HELP:'||batch_or_macro, l_location, l_error_code, l_error_detail);
      RETURN -1;
END action_help;

/* FUNCTION MACRO_ACTION *******************************************************
 * This function perform macro action operation.
 *
 * Macro actions
 * [MACRO_MOVE, MACRO_INSERT, MACRO_DELETE, MACRO_CHANGE_ATOM_ID,
 *  MACRO_CHANGE_STATUS, MACRO_CHANGE_TOBERELEASED, MACRO_CHANGE_FIELD]
 *
 */
FUNCTION macro_action(
   action	    IN VARCHAR2,		 -- [macro]
   id_type	    IN VARCHAR2,		 -- [C,R,A,CS]
   authority	    IN VARCHAR2,
   table_name	    IN VARCHAR2,
   work_id	    IN NUMBER,
   status	    IN VARCHAR2 := 'R',
   new_value	    IN VARCHAR2 DEFAULT NULL,
   action_field     IN VARCHAR2 DEFAULT 'NONE',
   set_preferred_flag IN VARCHAR2 DEFAULT MEME_CONSTANTS.YES
)
RETURN INTEGER
IS
   transaction_id   INTEGER;

   restore_flag     VARCHAR2(1);

BEGIN

   msp_set_preferred_flag := set_preferred_flag;

   IF MEME_UTILITY.ddl_commit_mode = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_off;
      restore_flag := MEME_CONSTANTS.YES;
   END IF;

   transaction_id := action_help(
			action => action, 
			id_type => id_type, 
			authority => authority, 
			table_name => table_name,
			work_id => work_id,
			status => status,
			new_value => new_value,
			action_field => action_field,
			batch_or_macro => MEME_CONSTANTS.MACRO_ACTION );

   IF restore_flag = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_on;
   END IF;

   RETURN transaction_id;

END macro_action;

/* FUNCTION BATCH_ACTION *******************************************************
 * This function perform batch action operation.
 *
 * Atomic actions
 * [MOVE, INSERT, DELETE, CHANGE_ATOM_ID, CHANGE_STATUS, CHANGE_TOBERELEASED,
 *  CHANGE_FIELD, C, I, D, A, S, T, CF]
 *
 * Molecular actions
 * [MOLECULAR_CHANGE_STATUS, MOLECULAR_CHANGE_TOBERELEASED,
 *  MOLECULAR_CHANGE_FIELD, MOLECULAR_INSERT, MOLECULAR_DELETE, MOLECULAR_MOVE,
 *  MOLECULAR_UNDO, MOLECULAR_REDO]
 *
 */
FUNCTION batch_action(
   action	    IN VARCHAR2,		 -- [molecular, atomic]
   id_type	    IN VARCHAR2,		 -- [C,R,A,CS]
   authority	    IN VARCHAR2,
   table_name	    IN VARCHAR2,
   work_id	    IN NUMBER,
   status	    IN VARCHAR2 := 'R',
   new_value	    IN VARCHAR2 DEFAULT NULL,
   action_field     IN VARCHAR2 DEFAULT 'NONE',
   set_preferred_flag IN VARCHAR2 DEFAULT MEME_CONSTANTS.YES
)
RETURN INTEGER
IS
   transaction_id   INTEGER;

   restore_flag     VARCHAR2(1);

BEGIN

   msp_set_preferred_flag := set_preferred_flag;

   IF MEME_UTILITY.ddl_commit_mode = MEME_CONSTANTS.YES THEN

      MEME_UTILITY.set_ddl_commit_off;
      restore_flag := MEME_CONSTANTS.YES;
   END IF;

   transaction_id := action_help(
			action => action, 
			id_type => id_type, 
			authority => authority, 
			table_name => table_name,
			work_id => work_id,
			status => status,
			new_value => new_value,
			action_field => action_field,
			batch_or_macro => MEME_CONSTANTS.BATCH_ACTION );

   IF restore_flag = MEME_CONSTANTS.YES THEN
      MEME_UTILITY.set_ddl_commit_on;
   END IF;

   RETURN transaction_id;

END batch_action;

END meme_batch_actions; -- end package
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_BATCH_ACTIONS.help;
execute MEME_BATCH_ACTIONS.register_package;
