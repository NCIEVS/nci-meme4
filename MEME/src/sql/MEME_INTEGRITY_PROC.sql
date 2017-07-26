CREATE OR REPLACE PACKAGE MEME_INTEGRITY_PROC AS

/********************************************************************************
 *
 * PL/SQL File: MEME_INTEGRITY_PROC.sql
 *
 * This package contains procedures
 * to perform MEME integrity checkings
 *
 * Changes
 * 02/24/2009 BAC (1-GCLNT): Improve performance of matrix init procedures.
 * 12/21/2006 BAC (1-D3YLZ): Bug fix to separated_strings
 * 05/10/2006 BAC (1-B6CFE): Changed msh_mui_merge, msh_mui_split to
 *     use classes.source_cui instead of attributes where atn='MUI'
 * 01/03/2006 BAC (1-72FLV): Bug fix to styisa query (extra ' character)
 * 10/28/2005 4.19.0: Bug fixes to styisa
 * 04/18/2005 4.18.0: Released
 * 03/10/2005 4.17.1: NLM02 -> RXNORM
 * 10/06/2004 4.17.0: Released
 * 09/29/2004 4.16.1: Changes to deleted_cui bin
 * 05/03/2004 4.16.0: Fixed to work with oracle 9.2
 * 10/16/2003 4.15.0: Fix to rcd_sep, +snomedct_mrg,sep
 * 09/09/2003 4.14.0: mxsuppr uses "tobereleased"
 * 04/16/2002 3.13.0: small fix to sfo_lfo, Released
 * 04/09/2002 3.12.0: sfo_lfo, scd_difflui only operate on releasable atoms
 * 04/05/2002 3.11.0: scd_difflui added. Released
 * 02/28/2002 3.10.0: Released.
 * 02/26/2001 3.9.1:  current_msh was VARCHAR2(10) changed to VARCHAR2(20).
 * 02/25/2001 3.9.0: msh_mui_merge, msh_mui_split
 * 		      fixed up for use.  These should become
 * 		      integrity bins.  Release to NLM
 * 10/30/2001 3.8.0: Released with new function
 * 07/19/2001 3.7.1: missing_sty_matrixinit: this query produces a violation
 *                     if a status R OR status N concept is missing an STY
 * 07/18/2001 3.7.0: deleted_cui ignores bequeathed rels,
 *			+deleted_cui_uwda (deleted_cui - UWDA)
 * 04/25/2001 3.6.2: Optimization to msh_c_orphan so it runs faster.
 * 04/16/2001 3.6.1: 'ICD' changed to 'ICD9' for icdproc 
 * 03/28/2001 3.6.0: Released version
 * 03/26/2001 3.5.1: deleted_cui ignores merges.
 * 02/14/2001 3.5.0: Released
 * 02/12/2001 3.4.2: obsolete_nec_pns
 * 01/10/2001 3.4.1: deleted_cui, deleted_cui_split, merged_cui,
 *                    msh_mui_split, msh_mui_merge
 * 11/10/2000 3.4.0: Released
 * 11/02/2000 3.3.9: changes in msh_?_orphan
 * 10/30/2000 3.3.8: changes in missing_sty
 * 10/12/2000 3.3.7: changes in mth_clone_rels
 * 09/26/2000 3.3.6: changes in msh_d_orphan, msh_q_orphan
 * 09/25/2000 3.3.5: changes in msh_d_orphan, msh_q_orphan, msh_c_orphan
 *		      xr_msh_d_orphan, xr_msh_q_orphan, xr_msh_c_orphan
 * 09/22/2000	      msh_d_orphan, msh_q_orphan
 * 09/20/2000 3.3.4: mth_clone_rels, changes in deleted_cui,
 *		      rescue_rels_help (procedure to function),
 * 09/13/2000 3.3.3: changes in deleted_cui, sr_split, rescue_lt
 * 09/05/2000 3.3.2: changes in mth_only, rescue_orphans, rescue_pair.
 *		      deleted_cui, sr_split
 * 09/01/2000 3.3.1: ambig_pn was looking at unreleasable PNs
 * 		      pn_pn_ambig looked at sui not isui
 * 08/24/2000 3.3.0: Release
 * 08/23/2000 3.2.1: missing_sty ignores unapproved concepts
 * 08/01/2000 3.2.0: Package handover version
 * 07/24/2000 3.1.7: sfo_lfo
 * 07/14/2000 3.1.6: mth_only, rescue_lt, rescue_orphan, rescue_pair
 * 07/14/2000 3.1.5: cui_splits
 * 06/27/2000 3.1.4: Fixed ambig_pn, mxsuppr
 * 4/6/2000 3.1.3: Added work_id parameter to the procedures
 * 3/12/2000 3.1.2: overloaded procedures were removed
 *		    only one method signature per procedure
 *		    Too big again, snapshot code offloaded to 
 *                  MEME_SNAPSHOT_PROC
 * 3/9/2000 3.1.1: Code added for qa_bins. (3.1.1)
 * 9/9/1999 3.1.0: First version created and compiled
 *
 * Status:
 *	Functionality:
 *	Testing:
 * 	Enhancements:
 *
 ***************************************************************************/

 --
 -- Standard Package API
 --
    package_name	VARCHAR2(25) := 'MEME_INTEGRITY_PROC';
    release_number	VARCHAR2(1)  := '4';
    version_number	VARCHAR2(5)  := '9.0';
    version_date	DATE	     := '28-Oct-2005';
    version_authority	VARCHAR2(3)  := 'BAC';

    meme_integrity_proc_debug BOOLEAN := FALSE;
    meme_integrity_proc_trace BOOLEAN := FALSE;

    -- Useful generic package variables
    location		VARCHAR2(5);
    err_msg		VARCHAR2(256);
    method		VARCHAR2(256);
    error_code		INTEGER;
    retval		INTEGER;

    meme_integrity_proc_exc  EXCEPTION;

    FUNCTION release RETURN INTEGER;
    FUNCTION version RETURN FLOAT;

    PROCEDURE version;
    PROCEDURE help;
    PROCEDURE help (
	topic IN VARCHAR2
    );

    PROCEDURE register_package;
    PROCEDURE set_trace_on;
    PROCEDURE set_trace_off;
    PROCEDURE set_debug_on;
    PROCEDURE set_debug_off;
    PROCEDURE trace ( message IN VARCHAR2 );

    PROCEDURE local_exec (query in varchar2 );
    FUNCTION local_exec (query IN VARCHAR2) RETURN INTEGER;

    FUNCTION version_info RETURN VARCHAR2;
    PRAGMA restrict_references (version_info,WNDS,RNDS,WNPS);

    PROCEDURE initialize_trace ( l_method IN VARCHAR2 );

    PROCEDURE meme_integrity_proc_error(
	method		IN VARCHAR2,
	location	IN VARCHAR2,
	error_code	IN INTEGER,
	detail		IN VARCHAR2
    );

 --
 -- Procedure to maintain the MEME_CLUSTER_HISTORY table
 --
    PROCEDURE recompute_cluster_history (
	name IN VARCHAR2,
	to_delete IN VARCHAR2 DEFAULT MEME_CONSTANTS.EMPTY_TABLE,
	to_insert IN VARCHAR2 DEFAULT MEME_CONSTANTS.EMPTY_TABLE);

 --
 -- Matrix Initiailizer/Updater Procedures
 -- These procedures are also used by the QA bins system
 --
    FUNCTION missing_sty (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION missing_sty_matrixinit (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION empty_concepts (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION auto_merged (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION demotions (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION pir (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION non_human (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION true_orphan (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION msh_d_orphan (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
 	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION xr_msh_d_orphan (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION msh_q_orphan (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION xr_msh_q_orphan (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION msh_c_orphan (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION xr_msh_c_orphan (
  	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION msh_et_synonym (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION msh_et_synonym_help1 (
	table_name IN VARCHAR2) RETURN VARCHAR2;
    FUNCTION msh_et_synonym_help2 RETURN VARCHAR2;

    FUNCTION msh_mh_diff (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION sfo_lfo (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION nh_sty (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION separated_pm (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    FUNCTION obsolete_nec_pns (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION scd_difflui (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

    --
    --	Multiple Meaning Procedures
    --

  -- DT_PN1
    FUNCTION ambig_no_pn (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  -- DT_PN2
    FUNCTION multiple_pn (
 	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  -- DT_PN3 :
    FUNCTION pn_no_ambig (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  -- DT_PN4
    FUNCTION pn_pn_ambig (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  -- DT_MM1
    FUNCTION ambig_no_rel (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  -- DT_MM2 :
    FUNCTION multiple_mm (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0) RETURN VARCHAR2;

  -- DT_MM3
    FUNCTION mm_no_ambig (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  -- DT_MM4
    FUNCTION mm_misalign (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  -- Find ambiguous strings
    FUNCTION separated_strings (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  --
  -- Multiple meaning (extra) queries
  --
    FUNCTION ambig_pn (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION pure_u_ambig_no_pn (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION approved_tm (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION merged_tm (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

 --
 -- QA Bins Procedures
 --   There is only one form because the results should ALWAYS
 --   be clustered, and always operate on the whole MID,
 --
 -- These should always be called with MEME_CONSTANTS.CLUSTER_YES
 --
    FUNCTION mxsuppr (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION checksrc (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION mthu (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION msh_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION msh_sep (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION msh_n1 (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION rcd_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION rcd_sep (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION snomedct_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION snomedct_sep (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION snm_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION pdq_orph (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION umd_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION umd_orph (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION hcpcs_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION cpt_split (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION cpt_orph (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION lnc_sep (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION styisa (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION icdproc (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION stydrug (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION mthdt_nomm (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION cui_splits (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION mth_only (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION rescue_lt (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION rescue_orphan (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION rescue_pair (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION rescue_rels_help (
	t_tbrn_rels   IN VARCHAR2,
	t_new_orphans IN VARCHAR2)
    RETURN INTEGER;
    FUNCTION deleted_cui (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION deleted_cui_uwda (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION deleted_cui_split (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION merged_cui (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION msh_mui_merge (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION msh_mui_split (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION suspect_cui (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION sr_split (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;
    FUNCTION mth_clone_rels (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

END MEME_INTEGRITY_PROC;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY MEME_INTEGRITY_PROC AS

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

/* FUNCTION VERSION_INFO *******************************************************
 */
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
    return package_name || ' Release ' || release_number || ': ' ||
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
PROCEDURE help ( topic IN VARCHAR2 )
IS
BEGIN
    -- This procedure requires SET SERVEROUTPUT ON
	--    DBMS_OUTPUT.ENABLE(100000);

    -- Print version
    MEME_UTILITY.put_message('. This package contains procedures used by MEME_INTEGRITY.');
    MEME_UTILITY.put_message('.');
    version;
END help;

/* PROCEDURE REGISTER_PACKAGE **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_INTEGRITY_PROC.release_number,
      MEME_INTEGRITY_PROC.version_number,
      SYSDATE,
      MEME_INTEGRITY_PROC.version_authority,
      MEME_INTEGRITY_PROC.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

/* PROCEDURE SET_DEBUG_ON ******************************************************
 */
PROCEDURE set_trace_on
IS
BEGIN

    meme_integrity_proc_trace := TRUE;

END set_trace_on;

/* PROCEDURE SET_DEBUG_OFF *****************************************************
 */
PROCEDURE set_trace_off
IS
BEGIN

    meme_integrity_proc_trace := FALSE;

END set_trace_off;

/* PROCEDURE SET_DEBUG_ON ******************************************************
 */
PROCEDURE set_debug_on
IS
BEGIN

    meme_integrity_proc_debug := TRUE;

END set_debug_on;

/* PROCEDURE SET_DEBUG_OFF *****************************************************
 */
PROCEDURE set_debug_off
IS
BEGIN

    meme_integrity_proc_debug := FALSE;

END set_debug_off;

/* PROCEDURE TRACE *************************************************************
 */
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF meme_integrity_proc_trace = TRUE THEN

	MEME_UTILITY.PUT_MESSAGE(message);

    END IF;

END trace;

/* PROCEDURE LOCAL_EXEC ********************************************************
 */
PROCEDURE local_exec ( query IN VARCHAR2 )
IS
BEGIN

    IF meme_integrity_proc_trace = TRUE THEN
	MEME_UTILITY.put_message(query);
    END IF;

    IF meme_integrity_proc_debug = FALSE THEN
	MEME_UTILITY.exec(query);
    END IF;

END local_exec;

/* FUNCTION LOCAL_EXEC *********************************************************
 */
FUNCTION local_exec ( query IN VARCHAR2 )
RETURN INTEGER
IS
BEGIN

    IF meme_integrity_proc_trace = TRUE THEN
	MEME_UTILITY.put_message(query);
    END IF;

    IF meme_integrity_proc_debug = FALSE THEN
	return MEME_UTILITY.exec(query);
    END IF;

    RETURN 0;

END local_exec;

/* PROCEDURE INITIALIZE_TRACE **************************************************
 */
PROCEDURE initialize_trace ( l_method IN VARCHAR2 )
IS
BEGIN
    location := '0';
    err_msg := '';
    method := l_method;
END initialize_trace;

/* PROCEDURE MEME_INTEGRITY_PROC_ERROR *****************************************
 */
PROCEDURE meme_integrity_proc_error(
    method		IN VARCHAR2,
    location		IN VARCHAR2,
    error_code		IN INTEGER,
    detail		IN VARCHAR2
)
IS
    error_msg	    VARCHAR2(1000);

BEGIN

    IF error_code = 1 THEN
	error_msg := 'MI0001: Unspecified error';
    ELSIF error_code = 2 THEN
	error_msg := 'MI0002: Call to MEME_UTILITY Failed';
    ELSIF error_code = 30 THEN
	error_msg := 'MI0030: Illegal Parameter Value';
    ELSIF error_code = 40 THEN
	error_msg := 'MI0040: Call to MEME_BATCH_ACTIONS Failed.';
    ELSE
	error_msg := 'MI0000: Unknown Error';
    END IF;

    MEME_UTILITY.PUT_ERROR('Error in MEME_INTEGRITY_PROC.' ||
			   ' Method: ' || method ||
			   ' Location: ' || location  ||
			   ' Error Code: '|| error_msg ||
			   ' Detail: '|| detail);

END meme_integrity_proc_error;

/* PROCEDURE RECOMPUTE_CLUSTER_HISTORY *****************************************
 * This procedure takes a check name and a
 * table with a subset of concept ids and clears
 * the cluster history information from meme_cluster_history
 */
PROCEDURE recompute_cluster_history (
	name IN VARCHAR2,
	to_delete IN VARCHAR2 DEFAULT MEME_CONSTANTS.EMPTY_TABLE,
	to_insert IN VARCHAR2 DEFAULT MEME_CONSTANTS.EMPTY_TABLE

)
IS

    location		VARCHAR(5);

BEGIN

    location := '0';
    -- If empty table, recompute entire history
    IF to_delete = MEME_CONSTANTS.EMPTY_TABLE THEN
	local_exec (
	    'DELETE FROM meme_cluster_history ' ||
	    'WHERE name = ''' || name || ''' ');

    ELSE

	location := '10';

    	/* check that to_delete has a concept_id field */
    	IF meme_integrity_proc_debug = FALSE AND
	    MEME_UTILITY.get_field_type(upper(to_delete),'CONCEPT_ID') != 'NUMBER'
    	THEN

	    meme_integrity_proc_error('recompute_cluster_history','1',0,
	     'Table ' || to_delete || ' does not have an integer concept_id field');
	    RAISE meme_integrity_proc_exc;
    	END IF;

    	location := '20';

    	local_exec (
	    'DELETE FROM meme_cluster_history WHERE concept_id_1 IN ' ||
	    '(SELECT concept_id FROM ' || to_delete || ') ' ||
	    'AND name = ''' || name || ''' '
	);

    	location := '30';

    	local_exec (
	    'DELETE FROM meme_cluster_history WHERE concept_id_2 IN ' ||
	    '(SELECT concept_id FROM ' || to_delete || ') ' ||
	    'AND name = ''' || name || ''' '
	);

    END IF;


    IF to_insert != MEME_CONSTANTS.EMPTY_TABLE THEN

	location := '40';

    	/* check that to_insert has concept_id_{1,2} fields */
    	IF meme_integrity_proc_debug = FALSE AND
	    MEME_UTILITY.get_field_type(upper(to_insert),'CONCEPT_ID_1') != 'NUMBER' AND
	    MEME_UTILITY.get_field_type(upper(to_insert),'CONCEPT_ID_2') != 'NUMBER'
    	THEN

	    meme_integrity_proc_error('recompute_cluster_history','1',0,
	     'Table ' || to_insert || ' does not have an integer concept_id field');
	    RAISE meme_integrity_proc_exc;
    	END IF;

    	location := '50';

    	local_exec (
	    'INSERT INTO meme_cluster_history ' ||
	    '		(name,concept_id_1,concept_id_2) ' ||
	    'SELECT ''' || name || ''', concept_id_1, ' ||
	    '	    concept_id_2 ' ||
	    'FROM ' || to_insert
	);

    END IF;

    return;

EXCEPTION
    WHEN OTHERS THEN
	meme_integrity_proc_error(
		'recompute_cluster_history',location,1,SQLERRM);
	RAISE meme_integrity_proc_exc;

END recompute_cluster_history;

/* FUNCTION MISSING_STY ********************************************************
 */
FUNCTION missing_sty (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    no_sty_check	VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='0';

    no_sty_check := missing_sty_matrixinit (
			table_name => table_name,
			cluster_flag => MEME_CONSTANTS.CLUSTER_NO,
			work_id => 0);

    location :='10';

    -- Remove unapproved concepts from the list
    local_exec(
	'DELETE FROM ' || no_sty_check || ' ' ||
	'WHERE concept_id IN ' ||
	'(SELECT concept_id FROM concept_status ' ||
	' WHERE status != ''R'') ');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location :='20';

	result_table := MEME_UTILITY.cluster_single(no_sty_check);

    ELSE

	result_table := no_sty_check;
	no_sty_check := '';

    END IF;

    MEME_UTILITY.drop_it('table',no_sty_check);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',no_sty_check);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('missing_sty',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END missing_sty;

/* FUNCTION MISSING_STY_MATRIXINIT **************************************/
-- This function was created to perform the "missing sty" check
-- in a slightly different way for the matrix initializer.  If a
-- concept lacks an approved semantic type, it will register
-- as a violation of this check.  In the "missing_sty" check
-- it is only a violation if the concept is unapproved.  
FUNCTION missing_sty_matrixinit (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    no_sty_check	VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='0';

    no_sty_check := MEME_UTILITY.get_unique_tablename('qat_');

    location :='10';

    local_exec(
	'CREATE TABLE ' || no_sty_check || ' AS 
	 SELECT DISTINCT concept_id FROM concept_status
	 WHERE tobereleased IN (''y'',''Y'',''?'')' ||
	restriction_clause ||
	'MINUS
	 SELECT /*+ PARALLEL(a)*/ concept_id FROM attributes a
	 WHERE attribute_name = ''SEMANTIC_TYPE''
	   AND tobereleased IN (''Y'',''y'',''?'')' ||
	 restriction_clause );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location :='20';

	result_table := MEME_UTILITY.cluster_single(no_sty_check);

    ELSE

	result_table := no_sty_check;
	no_sty_check := '';

    END IF;

    MEME_UTILITY.drop_it('table',no_sty_check);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',no_sty_check);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('missing_sty_matrixinit',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END missing_sty_matrixinit;

/* FUNCTION EMPTY_CONCEPTS *****************************************************
 */
FUNCTION empty_concepts (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause1  VARCHAR2(128);
    restriction_clause2 VARCHAR2(128);
    no_atoms_check	VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		 VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause1 :=
	    ' WHERE concept_id IN (select concept_id from ' || table_name || ') ';
	restriction_clause2 :=
	    'AND concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause1 := ' ';
	restriction_clause2 := ' ';
    END IF;

    location :='0';

    no_atoms_check := MEME_UTILITY.get_unique_tablename('qat_');

    location :='10';

    local_exec(
	'CREATE TABLE ' || no_atoms_check || ' AS ' ||
	'SELECT concept_id FROM concept_status c ' ||
	restriction_clause1 ||
	' MINUS ' ||
	'SELECT concept_id FROM classes ' ||
	'WHERE tobereleased IN (''Y'',''y'',''?'') ' ||
	restriction_clause2);

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location :='20';
	result_table := MEME_UTILITY.cluster_single(no_atoms_check);

    ELSE
	result_table := no_atoms_check;
	no_atoms_check := '';

    END IF;

    location :='30';
    MEME_UTILITY.drop_it('table',no_atoms_check);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',no_atoms_check);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('empty_concepts',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END empty_concepts;

/* FUNCTION AUTO_MERGED ********************************************************
 */
FUNCTION auto_merged (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    t_auto_merged	 VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		 VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='0';
    t_auto_merged := MEME_UTILITY.get_unique_tablename('qat_');

    -- Get concept_ids of classes with ENG-% authorities
    location := '10';
    local_exec(
	'CREATE TABLE ' || t_auto_merged || ' AS ' ||
	'SELECT /*+ PARALLEL(c) */ DISTINCT concept_id FROM classes c ' ||
	'WHERE authority LIKE ''ENG-%'' AND ' ||
	'tobereleased IN (''y'',''Y'',''?'') ' ||
	restriction_clause);

    location := '20';
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
    	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_auto_merged);
    ELSE
  	result_table := t_auto_merged;
	t_auto_merged := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it('table',t_auto_merged);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_auto_merged);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('auto_merged',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END auto_merged;

/* FUNCTION DEMOTIONS **********************************************************
 */
FUNCTION demotions (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause1	VARCHAR2(128);
    restriction_clause2 VARCHAR2(128);
    t_demotions 	VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    location := '0';

    t_demotions := MEME_UTILITY.get_unique_tablename('qat_');

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN

	location := '10';

    	local_exec(
	    'CREATE TABLE ' || t_demotions || ' AS ' ||
	    'SELECT DISTINCT concept_id_1, concept_id_2 ' ||
	    'FROM relationships ' ||
	    'WHERE status = ''D'' ' ||
	    'AND concept_id_1 IN (select concept_id from ' || table_name || ') ' ||
	    'UNION ALL ' ||
	    'SELECT DISTINCT concept_id_1, concept_id_2 ' ||
	    'FROM relationships ' ||
	    'WHERE status = ''D'' ' ||
	    'AND concept_id_2 IN (select concept_id from ' || table_name || ') '
	);

    ELSE

	location := '20';

    	local_exec(
	    'CREATE TABLE ' || t_demotions || ' AS ' ||
	    'SELECT /*+ PARALLEL(a) */ DISTINCT concept_id_1, concept_id_2 ' ||
	    'FROM relationships a ' ||
	    'WHERE status = ''D'' '
	);

    END IF;

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

    	location := '20';
	result_table := MEME_UTILITY.cluster_pair(t_demotions);

    ELSE

    	location := '30';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');

    	location := '40';
	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT concept_id_1 as concept_id FROM ' || t_demotions || ' ' ||
	    'UNION ' ||
	    'SELECT concept_id_2 FROM ' || t_demotions
	);

    END IF;

    -- Resolve meme_cluster_history
    location := '50';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('DEMOTIONS'),
	table_name, t_demotions);

    location := '60';
    MEME_UTILITY.drop_it('table',t_demotions);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_demotions);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('demotions',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END demotions;

/* FUNCTION PIR ****************************************************************
 */
FUNCTION pir (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    t_pir		VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		 VARCHAR2(256);

BEGIN

    location := '0';

    t_pir := MEME_UTILITY.get_unique_tablename('qat_');

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN

	location := '10';

    	local_exec(
	    'CREATE TABLE ' || t_pir || ' AS ' ||
	    'SELECT DISTINCT concept_id_1, concept_id_2 ' ||
	    'FROM relationships ' ||
	    'WHERE authority like ''PIR%'' ' ||
	    'AND concept_id_1 IN (select concept_id from ' || table_name || ') ' ||
	    'UNION ALL ' ||
	    'SELECT DISTINCT concept_id_1, concept_id_2 ' ||
	    'FROM relationships ' ||
	    'WHERE authority like ''PIR%'' ' ||
	    'AND concept_id_2 IN (select concept_id from ' || table_name || ') '
	);

    ELSE

	location := '20';

    	local_exec(
	    'CREATE TABLE ' || t_pir || ' AS ' ||
	    'SELECT DISTINCT concept_id_1, concept_id_2 ' ||
	    'FROM relationships ' ||
	    'WHERE authority like ''PIR%'' '
	);

    END IF;

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

    	location := '20';
	result_table := MEME_UTILITY.cluster_pair(t_pir);

    ELSE

    	location := '30';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');

    	location := '40';
	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT concept_id_1 as concept_id FROM ' || t_pir || ' ' ||
	    'UNION ' ||
	    'SELECT concept_id_2 FROM ' || t_pir
	);

    END IF;

    -- Resolve meme_cluster_history
    location := '50';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('PIR'),
	table_name, t_pir);

    location := '60';
    MEME_UTILITY.drop_it('table',t_pir);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_pir);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('pir',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END pir;

/* FUNCTION NON_HUMAN **********************************************************
 */
FUNCTION non_human (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    t_non_human 	VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		 VARCHAR2(256);
BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	    ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location := '0';

    t_non_human := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';

    local_exec(
	'CREATE TABLE ' || t_non_human || ' AS ' ||
	'SELECT DISTINCT concept_id FROM attributes ' ||
	'WHERE attribute_name = ''NON_HUMAN'' AND ' ||
	'tobereleased IN (''y'',''Y'',''?'') ' ||
	restriction_clause);

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

  	location :='20';
	result_table := MEME_UTILITY.cluster_single(t_non_human);

    ELSE

	result_table := t_non_human;
	t_non_human := '';

    END IF;

    location := '30';
    MEME_UTILITY.drop_it('table',t_non_human);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_non_human);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('non_human',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END non_human;

/* FUNCTION TRUE_ORPHAN ********************************************************
 */
FUNCTION true_orphan (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    t_true_orphans	VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);
BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	    ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location := '0';

    t_true_orphans := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';

    -- Get releasable concepts
    local_exec(
	'CREATE TABLE ' || t_true_orphans || '_pre1 AS ' ||
	'SELECT DISTINCT concept_id FROM classes ' ||
	'WHERE tobereleased IN (''y'',''Y'',''?'') ' ||
	restriction_clause);

    location := '20';

    -- Get concepts without a concept_id_1 relationship
    local_exec(
	'CREATE TABLE ' || t_true_orphans || '_pre2 AS ' ||
	'SELECT * FROM ' || t_true_orphans || '_pre1 a ' ||
	'WHERE NOT EXISTS (SELECT * FROM relationships b ' ||
	'WHERE a.concept_id = b.concept_id_1 AND ' ||
	'relationship_name != ''XR'' and tobereleased IN (''Y'',''y''))');

    location := '30';

    -- Get concepts without a concept_id_2 relationship
    local_exec(
	'CREATE TABLE ' || t_true_orphans || '_pre3 AS ' ||
	'SELECT * FROM ' || t_true_orphans || '_pre2 a ' ||
	'WHERE NOT EXISTS (SELECT * FROM relationships b ' ||
	'WHERE a.concept_id = b.concept_id_2 AND ' ||
	'relationship_name != ''XR'' AND tobereleased IN (''Y'',''y''))');

    location := '40';

    -- Get concepts without a concept_id_1 context relationship
    local_exec(
	'CREATE TABLE ' || t_true_orphans || '_pre4 AS ' ||
	'SELECT concept_id FROM ' || t_true_orphans || '_pre3 a ' ||
	'WHERE NOT EXISTS (SELECT * FROM classes b, context_relationships c ' ||
	'WHERE atom_id = atom_id_1 AND a.concept_id = b.concept_id)');

    location := '50';

    -- Get concepts without a concept_id_2 context relationship
    local_exec(
	'CREATE TABLE ' || t_true_orphans || ' AS ' ||
	'SELECT concept_id FROM ' || t_true_orphans || '_pre4 a ' ||
	'WHERE NOT EXISTS (SELECT * FROM classes b, context_relationships c ' ||
	'WHERE atom_id = atom_id_2 AND a.concept_id = b.concept_id)');

    location := '60';

    MEME_UTILITY.drop_it('table', t_true_orphans||'_pre1');
    MEME_UTILITY.drop_it('table', t_true_orphans||'_pre2');
    MEME_UTILITY.drop_it('table', t_true_orphans||'_pre3');
    MEME_UTILITY.drop_it('table', t_true_orphans||'_pre4');

    -- Cluster
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location := '50';
	result_table := MEME_UTILITY.cluster_single(t_true_orphans);

    ELSE

	result_table := t_true_orphans;
	t_true_orphans := '';

    END IF;

    location := '60';
    MEME_UTILITY.drop_it('table',t_true_orphans);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', t_true_orphans||'_pre1');
	MEME_UTILITY.drop_it('table', t_true_orphans||'_pre2');
	MEME_UTILITY.drop_it('table', t_true_orphans||'_pre3');
	MEME_UTILITY.drop_it('table', t_true_orphans||'_pre4');
	MEME_UTILITY.drop_it('table', t_true_orphans);
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('true_orphan',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END true_orphan;

/* FUNCTION MSH_D_ORPHAN *******************************************************
 */
FUNCTION msh_d_orphan (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    dc			VARCHAR2(100);
    dcode		VARCHAR2(100);
    ddef		VARCHAR2(100);
    cluster_table	 VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		 VARCHAR2(256);
    current_msh		VARCHAR2(40);
BEGIN

    current_msh := MEME_UTILITY.get_current_name('MSH');

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	    ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location := '0';
    dcode := MEME_UTILITY.get_unique_tablename('qat_');
    ddef := MEME_UTILITY.get_unique_tablename('qat_');
    dc := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';
    local_exec(
	'CREATE TABLE ' || ddef || ' AS ' ||
	'SELECT DISTINCT code, concept_id ' ||
	'FROM classes a ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND (termgroup LIKE ''%MH'' OR termgroup LIKE ''%HT'') ' ||
	'  AND code like ''D%'' ' ||
	restriction_clause);

    location := '20';
    local_exec(
	'CREATE TABLE ' || dcode || ' AS ' ||
	'SELECT DISTINCT a.code, a.concept_id ' ||
	'FROM classes a ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND code LIKE ''D%'' ' ||
	restriction_clause ||
	' MINUS ' ||
	'SELECT code ,concept_id ' ||
	'FROM ' || ddef);

    location := '30';
    local_exec(
	'CREATE TABLE ' || dc || '_pre AS ' ||
	'SELECT x.concept_id, x.code FROM ' || dcode || ' x, relationships, ' ||
	ddef || ' y WHERE x.concept_id = concept_id_2 AND ' ||
	'concept_id_1 = y.concept_id AND ' ||
	'x.code = y.code AND status = ''R'' AND ' ||
	'tobereleased IN (''y'',''Y'',''?'') AND ' ||
	'((relationship_level = ''C'' AND relationship_name IN ' ||
	'(''RT'',''BT'',''NT'') ' ||
	') OR (relationship_level = ''S'' AND ' ||
	'source = '||''''||current_msh||''''||' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'',''LK'')))');

    location := '40';
    local_exec(
	'INSERT INTO ' || dc || '_pre SELECT ' ||
	'x.concept_id, x.code FROM ' || dcode || ' x ,relationships, ' ||
	ddef || ' y WHERE concept_id_2 = y.concept_id ' ||
	'AND concept_id_1 = x.concept_id AND ' ||
	'x.code = y.code AND status = ''R'' AND ' ||
	'tobereleased IN (''y'',''Y'',''?'') AND ' ||
	'((relationship_level = ''C'' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'')) ' ||
	'OR (relationship_level = ''S'' AND ' ||
	'source = '||''''||current_msh||''''||' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'',''LK'')))');

    location := '50';
    local_exec(
	'CREATE TABLE ' || dc || ' AS SELECT concept_id, code FROM ' ||
	dcode || ' MINUS ' ||
	'SELECT concept_id, code FROM ' || dc || '_pre');

    --location := '60';
    --MEME_UTILITY.drop_it('table', dcode);
    --MEME_UTILITY.drop_it('table', dc||'_pre');

    location := '70';
    cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

    location := '80';
    local_exec (
	'CREATE TABLE ' || cluster_table || ' AS ' ||
	'SELECT a.concept_id as concept_id_1, b.concept_id as concept_id_2 ' ||
	'FROM ' || dcode || ' a, ' || ddef || ' b ' ||
	'WHERE a.code = b.code AND a.concept_id != b.concept_id');

    -- Resolve meme_cluster_history
    location := '82';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('MSH_D_ORPHAN'),
	table_name, cluster_table);

    MEME_UTILITY.drop_it('table',dc || '_pre');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '85';
	MEME_UTILITY.drop_it('table',cluster_table);

	location := '87';
	local_exec (
	   'CREATE TABLE ' || cluster_table || ' AS ' ||
	   'SELECT a.concept_id as concept_id_1, b.concept_id as concept_id_2 ' ||
	   'FROM ' || dc || ' a, ' || ddef || ' b ' ||
	   'WHERE a.code = b.code AND a.concept_id != b.concept_id');

	location := '90';
	result_table := MEME_UTILITY.cluster_pair(cluster_table);
    ELSE
	location := '92';
	result_table := dc;
	dc := '';
    END IF;

    location := '100';
    MEME_UTILITY.drop_it('table',dc);
    MEME_UTILITY.drop_it('table',ddef);
    MEME_UTILITY.drop_it('table',cluster_table);
    MEME_UTILITY.drop_it('table',dcode);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', dcode);
	MEME_UTILITY.drop_it('table', ddef);
	MEME_UTILITY.drop_it('table', dc || '_pre');
	MEME_UTILITY.drop_it('table', dc);
	MEME_UTILITY.drop_it('table', cluster_table);
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('msh_d_orphan',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_d_orphan;

/* FUNCTION XR_MSH_D_ORPHAN ****************************************************
 */
FUNCTION xr_msh_d_orphan (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    xr			VARCHAR2(100);
    dcode		VARCHAR2(100);
    ddef		VARCHAR2(100);
    location		VARCHAR2(256);
    result_table	VARCHAR2(50);
    current_msh		VARCHAR2(40);
BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='0';
    current_msh := MEME_UTILITY.get_current_name('MSH');

    location :='10';
    dcode := MEME_UTILITY.get_unique_tablename('qat_');
    ddef := MEME_UTILITY.get_unique_tablename('qat_');
    xr := MEME_UTILITY.get_unique_tablename('qat_');

    location :='20';
    local_exec(
	'CREATE TABLE ' || ddef || ' AS ' ||
	'SELECT DISTINCT code, concept_id ' ||
	'FROM classes a ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND (termgroup LIKE ''%MH'' OR termgroup LIKE ''%HT'') ' ||
	'  AND code like ''D%'' ' ||
	restriction_clause);

    location :='30';
    local_exec(
	'CREATE TABLE ' || dcode || ' AS ' ||
	'SELECT DISTINCT a.code, a.concept_id ' ||
	'FROM classes a ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND code LIKE ''D%'' ' ||
	restriction_clause ||
	' MINUS ' ||
	'SELECT code ,concept_id ' ||
	'FROM ' || ddef);

    location :='40';
    local_exec( 'CREATE TABLE ' || xr || ' AS ' ||
		'SELECT x.concept_id as concept_id_1, y.concept_id as concept_id_2 ' ||
		'FROM ' || dcode || ' x, relationships, ' || ddef || ' y ' ||
		'WHERE x.concept_id = concept_id_2 ' ||
		'AND concept_id_1 = y.concept_id ' ||
		'AND x.code = y.code ' ||
		'AND tobereleased IN (''y'',''Y'',''?'') ' ||
		'AND status||relationship_level||relationship_name = ''RCXR'' ' ||
		'UNION ' ||
		'SELECT x.concept_id, y.concept_id ' ||
		'FROM ' || dcode || ' x, relationships, ' || ddef || ' y ' ||
		'WHERE x.concept_id = concept_id_1 ' ||
		'AND concept_id_2 = y.concept_id ' ||
		'AND x.code = y.code ' ||
		'AND tobereleased IN (''y'',''Y'',''?'') ' ||
		'AND status||relationship_level||relationship_name = ''RCXR'' '
		);

    location := '50';
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	result_table := MEME_UTILITY.cluster_pair(xr);
    ELSE
	location := '70';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');
	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT distinct concept_id_1 as concept_id ' ||
	    'FROM ' || xr);
    END IF;

    -- Resolve meme_cluster_history
    -- 092500 cluster info is identical to MSH_D_ORPHAN
    -- location := '75';
    -- recompute_cluster_history(
    --	  MEME_UTILITY.get_ic_by_procedure_name('XR_MSH_D_ORPHAN'),
    -- table_name, xr);

    location := '80';
    MEME_UTILITY.drop_it('table', dcode);
    MEME_UTILITY.drop_it('table', ddef);
    MEME_UTILITY.drop_it('table',xr);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it('table', dcode);
    	MEME_UTILITY.drop_it('table', ddef);
    	MEME_UTILITY.drop_it('table', xr);
    	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('xr_msh_d_orphan',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END xr_msh_d_orphan;

/* FUNCTION MSH_Q_ORPHAN *******************************************************
 */
FUNCTION msh_q_orphan (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    qcode		VARCHAR2(100);
    qdef		VARCHAR2(100);
    qc			VARCHAR2(100);
    cluster_table	VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);
    current_msh		VARCHAR2(40);
BEGIN

    current_msh := MEME_UTILITY.get_current_name('MSH');
    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	  ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='0';
    qdef := MEME_UTILITY.get_unique_tablename('qat_');
    qcode := MEME_UTILITY.get_unique_tablename('qat_');
    qc := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';
    -- Get QMeSH Main headings
    local_exec(
	'CREATE TABLE ' || qdef || ' AS ' ||
	'SELECT DISTINCT code, concept_id FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND (termgroup like ''%GQ'' ' ||
	'    OR termgroup like ''%LQ'' ' ||
	'    OR termgroup like ''%TQ'') ' ||
	'AND code like ''Q%'' ' ||
	restriction_clause);

    location := '20';
    -- Get QMeSH non-Main headings with same codes
    local_exec(
	'CREATE TABLE ' || qcode || ' AS ' ||
	'SELECT DISTINCT code, concept_id FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'AND code like ''Q%'' ' ||
	restriction_clause ||
	' MINUS ' ||
	'SELECT code, concept_id FROM ' || qdef);

    location := '30';
    -- Get rels between Q MH's and non-MH's
    local_exec(
	'CREATE TABLE ' || qc || '_pre AS ' ||
	'SELECT x.concept_id, x.code FROM ' || qcode || ' x, relationships, ' ||
	qdef || ' y WHERE x.concept_id = concept_id_2 AND ' ||
	'concept_id_1 = y.concept_id AND ' ||
	'x.code = y.code AND status = ''R'' AND ' ||
	'tobereleased IN (''y'',''Y'',''?'') AND ' ||
	'((relationship_level = ''C'' AND relationship_name IN ' ||
	'(''RT'',''BT'',''NT'') ' ||
	') OR (relationship_level = ''S'' AND ' ||
	'source = '||''''||current_msh||''''||' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'',''LK'')))');

    location := '40';
    -- Get inverse rels between Q MH's and non-MH's
    local_exec(
	'INSERT INTO ' || qc || '_pre SELECT ' ||
	'x.concept_id, x.code FROM ' || qcode || ' x ,relationships, ' ||
	qdef || ' y WHERE concept_id_2 = y.concept_id ' ||
	'AND concept_id_1 = x.concept_id AND ' ||
	'x.code = y.code AND status = ''R'' AND ' ||
	'tobereleased IN (''y'',''Y'',''?'') AND ' ||
	'((relationship_level = ''C'' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'')) ' ||
	'OR (relationship_level = ''S'' AND ' ||
	'source = '||''''||current_msh||''''||' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'',''LK'')))');

    location := '50';
    -- Get pairs with same code lacking a relationship
    local_exec(
	'CREATE TABLE ' || qc || ' AS SELECT concept_id,code FROM ' ||
	qcode || ' MINUS ' ||
	'SELECT concept_id,code FROM ' || qc || '_pre');

    --location := '60';
    --MEME_UTILITY.drop_it('table',  qcode);
    --MEME_UTILITY.drop_it('table',  qc||'_pre');

   location := '70';
   cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

   location := '80';
   local_exec (
	'CREATE TABLE ' || cluster_table || ' AS ' ||
	'SELECT a.concept_id as concept_id_1, ' ||
	'	b.concept_id as concept_id_2 ' ||
	'FROM ' || qcode || ' a, ' || qdef || ' b ' ||
	'WHERE a.code = b.code AND a.concept_id != b.concept_id');

    -- Resolve meme_cluster_history
    location := '82';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('MSH_Q_ORPHAN'),
	table_name, cluster_table);

    MEME_UTILITY.drop_it('table',qc || '_pre');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '85';
	MEME_UTILITY.drop_it('table',cluster_table);

	location := '87';
	local_exec (
	   'CREATE TABLE ' || cluster_table || ' AS ' ||
	   'SELECT a.concept_id as concept_id_1, ' ||
	   '	b.concept_id as concept_id_2 ' ||
	   'FROM ' || qc || ' a, ' || qdef || ' b ' ||
	   'WHERE a.code = b.code AND a.concept_id != b.concept_id');

	location := '90';
	result_table := MEME_UTILITY.cluster_pair(cluster_table);
    ELSE
	location := '92';
	result_table := qc;
	qc := '';
    END IF;

    location := '100';
    MEME_UTILITY.drop_it('table',qc);
    MEME_UTILITY.drop_it('table',qdef);
    MEME_UTILITY.drop_it('table',cluster_table);
    MEME_UTILITY.drop_it('table',qcode);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', qcode);
	MEME_UTILITY.drop_it('table', qdef);
	MEME_UTILITY.drop_it('table', qc||'pre');
	MEME_UTILITY.drop_it('table', qc);
	MEME_UTILITY.drop_it('table', cluster_table);
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('msh_q_orphan',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_q_orphan;

/* FUNCTION XR_MSH_Q_ORPHAN ****************************************************
 */
FUNCTION xr_msh_q_orphan (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    xr			VARCHAR2(100);
    qcode		VARCHAR2(100);
    qdef		VARCHAR2(100);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);
    current_msh		VARCHAR2(40);
BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='0';
    current_msh := MEME_UTILITY.get_current_name('MSH');

    location :='10';
    qcode := MEME_UTILITY.get_unique_tablename('qat_');
    qdef := MEME_UTILITY.get_unique_tablename('qat_');
    xr := MEME_UTILITY.get_unique_tablename('qat_');

    location :='20';
    -- Get Q% MH's
    local_exec(
	'CREATE TABLE ' || qdef || ' AS ' ||
	'SELECT DISTINCT code, concept_id FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND (termgroup like ''%GQ'' ' ||
	'    OR termgroup like ''%LQ'' ' ||
	'    OR termgroup like ''%TQ'') ' ||
	'  AND code like ''Q%'' ' ||
	restriction_clause);

    location :='30';
    -- Get Q% non-MH's
    local_exec(
	'CREATE TABLE ' || qcode || ' AS ' ||
	'SELECT DISTINCT code, concept_id FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'AND code like ''Q%'' ' ||
	restriction_clause ||
	' MINUS ' ||
	'SELECT code, concept_id FROM ' || qdef);

    location :='40';
    -- Get XR rels between MH's and non-MH's
    local_exec(
	'CREATE TABLE ' || xr || ' AS ' ||
	'SELECT x.concept_id as concept_id_1, ' ||
	'	y.concept_id as concept_id_2 ' ||
	'FROM ' || qcode || ' x, relationships, ' || qdef || ' y ' ||
	'WHERE x.concept_id = concept_id_2 ' ||
	'AND concept_id_1 = y.concept_id ' ||
	'AND x.code = y.code ' ||
	'AND tobereleased IN (''y'',''Y'',''?'') ' ||
	'AND status||relationship_level||relationship_name = ''RCXR'' ' ||
	'UNION ' ||
	'SELECT x.concept_id, y.concept_id ' ||
	'FROM ' || qcode || ' x, relationships, ' || qdef || ' y ' ||
	'WHERE x.concept_id = concept_id_1 ' ||
	'AND concept_id_2 = y.concept_id ' ||
	'AND x.code = y.code ' ||
	'AND tobereleased IN (''y'',''Y'',''?'') ' ||
	'AND status||relationship_level||relationship_name = ''RCXR'' '
	);

    -- Cluster
    location := '50';
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	result_table := MEME_UTILITY.cluster_pair(xr);
    ELSE
    	location := '60';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');
    	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT distinct concept_id_1 as concept_id ' ||
	    'FROM ' || xr);
    END IF;

    -- Resolve meme_cluster_history
    -- 092500 cluster info is identical to MSH_Q_ORPHAN
    -- location := '65';
    -- recompute_cluster_history(
    --	 MEME_UTILITY.get_ic_by_procedure_name('XR_MSH_Q_ORPHAN'),
    --	 table_name, xr);

    location := '70';
    MEME_UTILITY.drop_it('table', qcode);
    MEME_UTILITY.drop_it('table', qdef);
    MEME_UTILITY.drop_it('table',xr);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it('table', qcode);
    	MEME_UTILITY.drop_it('table', qdef);
    	MEME_UTILITY.drop_it('table', xr);
    	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('xr_msh_q_orphan',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END xr_msh_q_orphan;

/* FUNCTION MSH_C_ORPHAN *******************************************************
 */
FUNCTION msh_c_orphan (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    cdef		VARCHAR2(100);
    ccode		VARCHAR2(100);
    cc			VARCHAR2(100);
    cluster_table	VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);
    current_msh		VARCHAR2(40);
BEGIN

    current_msh := MEME_UTILITY.get_current_name('MSH');

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	  ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location := '0';
    cdef := MEME_UTILITY.get_unique_tablename('qat_');
    ccode := MEME_UTILITY.get_unique_tablename('qat_');
    cc := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';
    -- Get C% MH's
    local_exec(
	'CREATE TABLE ' || cdef || ' AS ' ||
	'SELECT code, concept_id ' ||
	'FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND termgroup like ''%NM'' ' ||
	'  AND code like ''C%'' ' ||
	restriction_clause);

    location := '10.1';
    MEME_SYSTEM.analyze(cdef);

    location := '20';
    -- Get C% non-MH's
    local_exec(
	'CREATE TABLE ' || ccode || ' AS ' ||
	'SELECT DISTINCT code, concept_id ' ||
	'FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND code like ''C%'' ' ||
	restriction_clause ||
	' MINUS ' ||
	'SELECT code, concept_id FROM ' || cdef);

    location := '10.2';
    MEME_SYSTEM.analyze(ccode);

    location := '30';
    -- Get rels betweeh MH's and non-MH's
    local_exec(
	'CREATE TABLE ' || cc || '_pre AS ' ||
	'SELECT /*+ RULE */ x.concept_id,x.code FROM ' || ccode || ' x, relationships, ' ||
	cdef || ' y WHERE x.concept_id = concept_id_2 AND ' ||
	'concept_id_1 = y.concept_id AND ' ||
	'x.code = y.code AND status IN (''R'',''U'') AND ' ||
	'tobereleased IN (''y'',''Y'',''?'') AND ' ||
	'((relationship_level = ''C'' AND relationship_name IN ' ||
	'(''RT'',''BT'',''NT'') ' ||
	') OR (relationship_level = ''S'' AND ' ||
	'source = '||''''||current_msh||''''||' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'',''LK'')))');

    location := '40';
    -- Get inverse rels betweeh MH's and non-MH's
    local_exec(
	'INSERT INTO ' || cc || '_pre SELECT /*+RULE */ ' ||
	'x.concept_id, x.code FROM ' || ccode || ' x ,relationships, ' ||
	cdef || ' y WHERE concept_id_2 = y.concept_id ' ||
	'AND concept_id_1 = x.concept_id AND ' ||
	'x.code = y.code AND status IN (''R'',''U'') AND ' ||
	'tobereleased IN (''y'',''Y'',''?'') AND ' ||
	'((relationship_level = ''C'' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'')) ' ||
	'OR (relationship_level = ''S'' AND ' ||
	'source = '||''''||current_msh||''''||' AND ' ||
	'relationship_name IN (''RT'',''BT'',''NT'',''LK'')))');

    location := '50';
    -- Get MH and non-MH pairs without a rel between
    local_exec(
	'CREATE TABLE ' || cc || ' AS SELECT concept_id,code FROM ' ||
	ccode || ' MINUS ' ||
	'SELECT concept_id,code FROM ' || cc || '_pre');

    location :='60';
    cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

    location :='80';
    local_exec (
	'CREATE TABLE ' || cluster_table || ' AS ' ||
	'SELECT a.concept_id as concept_id_1, ' ||
	'	b.concept_id as concept_id_2 ' ||
	'FROM ' || ccode || ' a, ' || cdef || ' b ' ||
	'WHERE a.code = b.code AND A.concept_id != b.concept_id');

    -- Resolve meme_cluster_history
    location := '82';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('MSH_C_ORPHAN'),
	table_name, cluster_table);

    MEME_UTILITY.drop_it('table',cc||'_pre');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '85';
	MEME_UTILITY.drop_it('table',cluster_table);

	location :='87';
	local_exec (
	   'CREATE TABLE ' || cluster_table || ' AS ' ||
	   'SELECT a.concept_id as concept_id_1, ' ||
	   '	b.concept_id as concept_id_2 ' ||
	   'FROM ' || cc || ' a, ' || cdef || ' b ' ||
	   'WHERE a.code = b.code AND A.concept_id != b.concept_id');

	location :='90';
	result_table := MEME_UTILITY.cluster_pair(cluster_table);
    ELSE
	location :='92';
  	result_table := cc;
	cc := '';
    END IF;

    location := '100';
    MEME_UTILITY.drop_it('table',ccode);
    MEME_UTILITY.drop_it('table',cdef);
    MEME_UTILITY.drop_it('table',cc);
    MEME_UTILITY.drop_it('table',cluster_table);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', ccode);
	MEME_UTILITY.drop_it('table', cdef);
	MEME_UTILITY.drop_it('table', cc||'_pre');
	MEME_UTILITY.drop_it('table', cc);
	MEME_UTILITY.drop_it('table', cluster_table);
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('msh_c_orphan',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_c_orphan;

/* FUNCTION XR_MSH_C_ORPHAN ****************************************************
 */
FUNCTION xr_msh_c_orphan (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    xr			VARCHAR2(100);
    ccode		VARCHAR2(100);
    cdef		VARCHAR2(100);
    result_table	VARCHAR2(50);
    location		VARCHAR(256);
    current_msh		VARCHAR2(40);
BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='0';
    current_msh := MEME_UTILITY.get_current_name('MSH');

    location :='10';
    ccode := MEME_UTILITY.get_unique_tablename('qat_');
    cdef := MEME_UTILITY.get_unique_tablename('qat_');
    xr := MEME_UTILITY.get_unique_tablename('qat_');

    location :='20';
    -- Get C% MH's
     local_exec(
	'CREATE TABLE ' || cdef || ' AS ' ||
	'SELECT code, concept_id ' ||
	'FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND termgroup like ''%NM'' ' ||
	'  AND code like ''C%'' ' ||
	restriction_clause);

    location :='30';
    -- Get C% non-MH's
    local_exec(
	'CREATE TABLE ' || ccode || ' AS ' ||
	'SELECT DISTINCT code, concept_id ' ||
	'FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND code like ''C%'' ' ||
	restriction_clause ||
	' MINUS ' ||
	'SELECT code, concept_id FROM ' || cdef);

    location :='40';
    -- Get XR rels between MH's and non-MH's
    local_exec( 'CREATE TABLE ' || xr || ' AS ' ||
		'SELECT x.concept_id as concept_id_1, y.concept_id as concept_id_2 ' ||
		'FROM ' || ccode || ' x, relationships, ' || cdef || ' y ' ||
		'WHERE x.concept_id = concept_id_2 ' ||
		'AND concept_id_1 = y.concept_id ' ||
		'AND x.code = y.code ' ||
		'AND tobereleased IN (''y'',''Y'',''?'') ' ||
		'AND status||relationship_level||relationship_name = ''RCXR'' ' ||
		'UNION ' ||
		'SELECT x.concept_id, y.concept_id ' ||
		'FROM ' || ccode || ' x, relationships, ' || cdef || ' y ' ||
		'WHERE x.concept_id = concept_id_1 ' ||
		'AND concept_id_2 = y.concept_id ' ||
		'AND x.code = y.code ' ||
		'AND tobereleased IN (''y'',''Y'',''?'') ' ||
		'AND status||relationship_level||relationship_name = ''RCXR'' '
		);

    -- Cluster
    location := '50';
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	result_table := MEME_UTILITY.cluster_pair(xr);
    ELSE
	location := '60';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');
   	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT distinct concept_id_1 as concept_id ' ||
	    'FROM ' || xr);
    END IF;

    -- Resolve meme_cluster_history
    -- 092500 cluster info is identical to MSH_C_ORPHAN
    -- location := '65';
    -- recompute_cluster_history(
    --	 MEME_UTILITY.get_ic_by_procedure_name('XR_MSH_C_ORPHAN'),
    --	 table_name, xr);

    location := '70';
    MEME_UTILITY.drop_it('table', ccode);
    MEME_UTILITY.drop_it('table', cdef);
    MEME_UTILITY.drop_it('table',xr);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it('table', ccode);
    	MEME_UTILITY.drop_it('table', cdef);
    	MEME_UTILITY.drop_it('table', xr);
    	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('xr_msh_c_orphan',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END xr_msh_c_orphan;

/* FUNCTION MSH_ET_SYNONYM *****************************************************
 */
FUNCTION msh_et_synonym (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    help_result_table 	 VARCHAR2(50);
    result_table 	 VARCHAR2(50);
    cluster_table 	 VARCHAR2(50);
    restriction_clause	 VARCHAR2(256);
    location		 VARCHAR2(5);
BEGIN

    location := '0';

    -- If a table name is passed, call _help1
    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN

	help_result_table :=  msh_et_synonym_help1(table_name);

    -- If no table name is passed, call regular code
    ELSE

	help_result_table := msh_et_synonym_help2;

    END IF;

    location := '20';

    -- Prepare return table
    result_table := MEME_UTILITY.get_unique_tablename('qat_');

	location := '21';
    -- To cluster group mh_id, and 2 et_ids under same cluster_id;
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location := '30';
	cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

	location := '35';
	local_exec (
	    'CREATE TABLE ' || cluster_table || ' AS ' ||
	    'SELECT mh_concept_id as concept_id, ' ||
	    '	to_char(mh_concept_id) as cluster_id ' ||
	    'FROM ' || help_result_table || ' ' ||
	    'UNION ' ||
	    'SELECT et_concept_1 as concept_id, ' ||
	    '	to_char(mh_concept_id) as cluster_id ' ||
	    'FROM ' || help_result_table || ' ' ||
	    'UNION ' ||
	    'SELECT et_concept_2 as concept_id, ' ||
	    '	to_char(mh_concept_id) as cluster_id ' ||
	    'FROM ' || help_result_table );

	location := '40';
	result_table := MEME_UTILITY.recluster(cluster_table);

    	MEME_UTILITY.drop_it('table', cluster_table);

    -- Otherwise just return mh_ids in the restricted set
    ELSE
	location := '41';
	IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	    restriction_clause :=
		' WHERE mh_concept_id in (SELECT concept_id FROM ' ||
		table_name || ')';

	ELSE
	    restriction_clause := '';
	END IF;

	location := '50';

	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT distinct mh_concept_id as concept_id ' ||
	    'FROM ' || help_result_table ||
	    restriction_clause );

    END IF;

    -- Resolve meme_cluster_history
    location := '60';
    cluster_table := MEME_UTILITY.get_unique_tablename('qat_');
    location := '65';
    local_exec (
	'CREATE TABLE ' || cluster_table || ' AS ' ||
	'SELECT mh_concept_id as concept_id_1, et_concept_1 as concept_id_2 ' ||
	'FROM ' || help_result_table || ' ' ||
	'UNION ' ||
	'SELECT mh_concept_id as concept_id_1, et_concept_2 as concept_id_2 ' ||
	'FROM ' || help_result_table || ' ' ||
	'UNION ' ||
	'SELECT et_concept_1 as concept_id_1, et_concept_2 as concept_id_2 ' ||
	'FROM ' || help_result_table
	);
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('MSH_ET_SYNONYM'),
	table_name, cluster_table);

    location := '70';
    MEME_UTILITY.drop_it('table', help_result_table);
    MEME_UTILITY.drop_it('table', cluster_table);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it('table', cluster_table);
    	MEME_UTILITY.drop_it('table', help_result_table);
    	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('msh_et_synonym',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_et_synonym;

/* FUNCTION MSH_ET_SYNONYM_HELP1 ***********************************************
 */
FUNCTION msh_et_synonym_help1 (
	table_name	IN VARCHAR2
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    mesh_other		VARCHAR2(50);
    mh_et_rels		VARCHAR2(50);
    same_rels		VARCHAR2(50);
    safe_reps		VARCHAR2(50);
    mh_et_rels_pre	VARCHAR2(50);
    location		VARCHAR2(256);
    current_msh		VARCHAR2(40);
    previous_msh	VARCHAR2(40);
    result_table	VARCHAR2(50);
BEGIN

    location :='0';
    current_msh := MEME_UTILITY.get_current_name('MSH');
    previous_msh := MEME_UTILITY.get_previous_name('MSH');

    location :='5';
    IF table_name = MEME_CONSTANTS.EMPTY_TABLE THEN
	meme_integrity_proc_error('msh_et_synonym_help1','300',1,
	    'This procedure must be called with a table_name');
    END IF;

    restriction_clause :=
	' and concept_id IN (select concept_id from ' || table_name || ') ';

    location :='10';

    mesh_other := MEME_UTILITY.get_unique_tablename('qat_');
    mh_et_rels_pre := MEME_UTILITY.get_unique_tablename('qat_');
    mh_et_rels := MEME_UTILITY.get_unique_tablename('qat_');
    safe_reps := MEME_UTILITY.get_unique_tablename('qat_');
    same_rels := MEME_UTILITY.get_unique_tablename('qat_');

    location :='20 ';

    -- Get non-MH mesh concepts in the restriction set
    local_exec(
	'CREATE TABLE ' || mesh_other || ' AS ' ||
	'SELECT atom_id, concept_id, code ' ||
	'FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' '||
	restriction_clause ||
	'AND (code like ''D%'' OR code like ''Q%'') ' ||
	'AND (termgroup not like ''%MH'' AND termgroup not like ''%HT'' AND ' ||
	'     termgroup not like ''%GQ'' AND termgroup not like ''%LQ'' AND ' ||
	'     termgroup not like ''%TQ'') '
	);

    location :='30';

    -- Get all things related to non-MH mesh in restriction set
    local_exec(
	'CREATE TABLE ' || mh_et_rels_pre ||
	'	(et_concept_id, rel, code, mh_concept_id) as ' ||
	'SELECT concept_id_1, relationship_name, b.code, concept_id_2 ' ||
	'FROM relationships a, ' ||  mesh_other || ' b ' ||
	'WHERE concept_id_1 = b.concept_id ' ||
	'  AND a.tobereleased in (''y'',''Y'',''?'') ' ||
	'  AND relationship_name != ''XR'' ' ||
	'UNION ' ||
	'SELECT concept_id_2, inverse_name, b.code, concept_id_1 ' ||
	'FROM relationships a, ' ||  mesh_other || ' b, inverse_relationships c ' ||
	'WHERE concept_id_2 = b.concept_id ' ||
	'  AND a.tobereleased in (''y'',''Y'',''?'') ' ||
	'  AND a.relationship_name != ''XR'' ' ||
	'  AND a.relationship_name=c.relationship_name');

    location := '40 ';

    -- Keep only relationships between non-MH's and MH's
    local_exec(
	'CREATE TABLE ' || mh_et_rels ||
	'	(mh_concept_id,rel,code,et_concept_id) AS ' ||
	'SELECT mh_concept_id, rel, a.code, et_concept_id ' ||
	'FROM ' || mh_et_rels_pre || ' a ' ||
	'WHERE mh_concept_id IN ' ||
	'(SELECT concept_id FROM classes b ' ||
	' WHERE b.source = ''' || current_msh || ''' ' ||
	' AND a.code = b.code ' ||
	' AND (termgroup like ''%MH'' OR termgroup like ''%HT'' OR ' ||
	'      termgroup like ''%GQ'' OR termgroup like ''%LQ'' OR ' ||
	'      termgroup like ''%TQ''))' );

    location := '50';

    MEME_UTILITY.drop_it('table',mh_et_rels_pre);

    location := '60';

    -- Get everything that the MH's are related to (including outside restriction set)
    local_exec(
	'CREATE TABLE ' || mh_et_rels_pre ||
	'	(et_concept_id, rel, code, mh_concept_id) as ' ||
	'SELECT concept_id_1, relationship_name, b.code, concept_id_2 ' ||
	'FROM relationships a, ' ||  mh_et_rels || ' b ' ||
	'WHERE concept_id_2 = b.mh_concept_id ' ||
	'  AND a.tobereleased in (''y'',''Y'',''?'') ' ||
	'  AND relationship_name != ''XR'' ' ||
	'UNION ' ||
	'SELECT concept_id_2, inverse_name, b.code, concept_id_1 ' ||
	'FROM relationships a, ' ||  mh_et_rels || ' b, inverse_relationships c ' ||
	'WHERE concept_id_1 = b.mh_concept_id ' ||
	'  AND a.tobereleased in (''y'',''Y'',''?'') ' ||
	'  AND a.relationship_name != ''XR'' ' ||
	'  AND a.relationship_name=c.relationship_name');

    location := '70';

    -- Delete duplicates
    local_exec (
	'DELETE FROM ' || mh_et_rels_pre || ' WHERE et_concept_id IN ' ||
	'(SELECT et_concept_id FROM ' || mh_et_rels || ')');

    location :='80';

    -- Add these rels to earlier non-MH <==> MH ones.
    local_exec (
	'INSERT INTO ' || mh_et_rels || ' ' ||
	' 	(mh_concept_id, rel, code, et_concept_id) ' ||
	'SELECT DISTINCT mh_concept_id, rel, a.code, et_concept_id ' ||
	'FROM ' || mh_et_rels_pre || ' a, classes b ' ||
	'WHERE b.source = ''' || current_msh || ''' ' ||
	'AND a.code = b.code ' ||
	'and (termgroup not like ''%MH'' AND ' ||
	'     termgroup not like ''%HT'' AND ' ||
	'     termgroup not like ''%GQ'' AND ' ||
	'     termgroup not like ''%LQ'' AND ' ||
	'     termgroup not like ''%TQ'') ');

    location := '90';

    -- Delete self-referential rels
    local_exec(
	'DELETE FROM ' || mh_et_rels || ' WHERE mh_concept_id=et_concept_id');

    location :='100';

    -- Find where relationship name is the same,
    -- 'RT?' is the "same" as anything else
    local_exec(
	'CREATE TABLE ' || same_rels || ' AS ' ||
	'SELECT a.mh_concept_id, a.rel, b.code, ' ||
	' a.et_concept_id AS et_concept_1, b.et_concept_id AS et_concept_2 ' ||
	'FROM ' || mh_et_rels || ' a, ' || mh_et_rels || ' b ' ||
	'WHERE a.mh_concept_id = b.mh_concept_id ' ||
	'AND (a.rel = b.rel OR a.rel=''RT?'' OR b.rel=''RT?'') ' ||
	'AND a.code = b.code AND a.et_concept_id < b.et_concept_id');

    location :='110';

    -- Look up safe replacements
    local_exec(
	'CREATE TABLE ' || safe_reps || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.source = ''' || previous_msh || ''' ' ||
	'AND b.source = ''' || current_msh || ''' ' ||
	'AND a.concept_id = b.concept_id AND a.lui = b.lui ' ||
	'AND a.concept_id IN (SELECT et_concept_id FROM ' ||  mh_et_rels || ')' );

    location :='120';

    -- Remove safe replacements from candidate list
    local_exec(
	'DELETE FROM ' || same_rels ||
	' WHERE et_concept_1 IN (SELECT concept_id FROM ' ||
	safe_reps || ') AND et_concept_2 IN ' ||
	'(SELECT concept_id FROM ' || safe_reps || ')');

    location :='140';

    MEME_UTILITY.drop_it('table', mesh_other);
    MEME_UTILITY.drop_it('table', mh_et_rels);
    MEME_UTILITY.drop_it('table', mh_et_rels_pre);
    MEME_UTILITY.drop_it('table', safe_reps);


    return same_rels;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it('table', mesh_other);
    	MEME_UTILITY.drop_it('table', mh_et_rels);
    	MEME_UTILITY.drop_it('table', mh_et_rels_pre);
    	MEME_UTILITY.drop_it('table', same_rels);
    	MEME_UTILITY.drop_it('table', safe_reps);
	meme_integrity_proc_error('msh_et_synonym_help1',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_et_synonym_help1;

/* FUNCTION MSH_ET_SYNONYM_HELP2 ***********************************************
 */
FUNCTION msh_et_synonym_help2
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    current_mh_atoms	VARCHAR2(50);
    other_mesh_atoms	VARCHAR2(50);
    mesh_mh_concepts	VARCHAR2(50);
    mesh_other_concepts	VARCHAR2(50);
    mh_et_rels		VARCHAR2(50);
    same_rels		VARCHAR2(50);
    safe_reps		VARCHAR2(50);
    mh_concepts 	VARCHAR2(50);
    cluster_table	 VARCHAR2(50);
    location		VARCHAR2(256);
    current_msh		VARCHAR2(40);
    previous_msh	VARCHAR2(40);

BEGIN

    current_msh := MEME_UTILITY.get_current_name('MSH');
    previous_msh := MEME_UTILITY.get_previous_name('MSH');

    location :='10';

    mh_concepts := MEME_UTILITY.get_unique_tablename('qat_');
    current_mh_atoms := MEME_UTILITY.get_unique_tablename('qat_');
    other_mesh_atoms := MEME_UTILITY.get_unique_tablename('qat_');
    mesh_mh_concepts := MEME_UTILITY.get_unique_tablename('qat_');
    mesh_other_concepts := MEME_UTILITY.get_unique_tablename('qat_');
    mh_et_rels := MEME_UTILITY.get_unique_tablename('qat_');
    same_rels := MEME_UTILITY.get_unique_tablename('qat_');
    safe_reps := MEME_UTILITY.get_unique_tablename('qat_');

    location :='20';

    -- Get MH atoms
    local_exec(
	'CREATE TABLE ' || current_mh_atoms ||
	' AS SELECT atom_id, concept_id, code FROM classes ' ||
	'WHERE source = '||''''||current_msh||''''||' AND ' ||
	'(termgroup like ''%MH'' OR termgroup like ''%HT'' OR ' ||
	'(termgroup like ''%GQ'' OR termgroup like ''%LQ'' OR ' ||
	'termgroup like ''%TQ'')) '
	);

    location :='30';

    -- Get non-MH atoms
    local_exec(
	'CREATE TABLE ' || other_mesh_atoms || ' AS ' ||
	'SELECT atom_id, concept_id, code ' ||
	'FROM classes ' ||
	'WHERE source IN ' ||
	'  (''' || current_msh || ''', ''' || previous_msh || ''') ' ||
	'AND (code like ''D%'' OR code like ''Q%'' )' ||
	'MINUS ' ||
	'SELECT atom_id, concept_id, code ' ||
	'FROM ' || current_mh_atoms
	);

    location :='40';

    -- Get MH concepts
    local_exec(
	'CREATE TABLE ' || mesh_mh_concepts || ' AS ' ||
	'(SELECT DISTINCT concept_id, code FROM ' ||
	current_mh_atoms || ')');

    location :='50';

    -- Get non-MH concepts
    local_exec(
	'CREATE TABLE ' || mesh_other_concepts || ' AS ' ||
	'SELECT DISTINCT concept_id, code ' ||
	'FROM ' || other_mesh_atoms || ' ' ||
	'MINUS ' ||
	'SELECT concept_id, code ' ||
	'FROM ' || mesh_mh_concepts
	);

    location :='60';

    -- Get relationships between MH and non-MH concepts (non XR rels)
    local_exec(
	'CREATE TABLE ' || mh_et_rels || ' ' ||
	'	 (mh_concept_id, rel, code, et_concept_id) AS ' ||
	'SELECT DISTINCT concept_id_1, relationship_name, ' ||
	'	b.code,  concept_id_2 ' ||
	'FROM relationships a, ' || mesh_mh_concepts || ' b, ' ||
	mesh_other_concepts || ' c ' ||
	'WHERE concept_id_1 = b.concept_id ' ||
	'AND concept_id_2 = c.concept_id ' ||
	'AND b.code = c.code ' ||
	'AND tobereleased IN (''y'',''Y'',''?'') ' ||
	'AND relationship_name != ''XR'''
	);

    location :='70';

    -- Get inverse relationships between MH and non-MH concepts (non XR rels)
    local_exec(
	'INSERT INTO ' || mh_et_rels || ' ' ||
	'	 (mh_concept_id, rel, code, et_concept_id) ' ||
	'SELECT DISTINCT concept_id_1, inverse_name, ' ||
	'	b.code,  concept_id_2 ' ||
	'FROM relationships a, ' || mesh_mh_concepts || ' b, ' ||
	mesh_other_concepts || ' c, inverse_relationships ir ' ||
	'WHERE concept_id_2 = b.concept_id ' ||
	'AND concept_id_1 = c.concept_id ' ||
	'AND b.code = c.code ' ||
	'AND tobereleased IN (''y'',''Y'',''?'') ' ||
	'AND a.relationship_name != ''XR'' ' ||
	'AND a.relationship_name = ir.relationship_name'
	);

    location :='80';

    -- Find matching relationships (mh_id ,et_id1, et_id2)
    local_exec(
	'CREATE TABLE ' || same_rels || ' AS ' ||
	'SELECT a.mh_concept_id, a.rel, b.code, ' ||
	'	a.et_concept_id AS et_concept_1, ' ||
	'	b.et_concept_id AS et_concept_2 ' ||
	'FROM ' || mh_et_rels || ' a, ' || mh_et_rels || ' b ' ||
	'WHERE a.mh_concept_id = b.mh_concept_id ' ||
	'AND (a.rel = b.rel OR a.rel = ''RT?'' OR b.rel = ''RT?'') ' ||
	'AND a.code = b.code AND a.et_concept_id<b.et_concept_id');

    location :='90';

    -- Get safe replacement atoms
    local_exec(
	'CREATE TABLE ' || safe_reps || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.source = ''' || previous_msh || ''' ' ||
	'AND b.source = ''' || current_msh || ''' ' ||
	'AND a.concept_id = b.concept_id AND a.lui = b.lui'
	);

    location :='100';

    -- Remove safe replacement atoms from consideration
    local_exec(
	'DELETE FROM ' || same_rels || ' ' ||
	'WHERE et_concept_1 IN ' ||
	'  (SELECT concept_id FROM ' || safe_reps || ') ' ||
	'AND et_concept_2 IN ' ||
	'  (SELECT concept_id FROM ' || safe_reps || ')'
	);

    MEME_UTILITY.drop_it('table', current_mh_atoms);
    MEME_UTILITY.drop_it('table', other_mesh_atoms);
    MEME_UTILITY.drop_it('table', mesh_mh_concepts);
    MEME_UTILITY.drop_it('table', mesh_other_concepts);
    MEME_UTILITY.drop_it('table', mh_et_rels);
    MEME_UTILITY.drop_it('table', safe_reps);

    return same_rels;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', current_mh_atoms);
	MEME_UTILITY.drop_it('table', other_mesh_atoms);
	MEME_UTILITY.drop_it('table', mesh_mh_concepts);
	MEME_UTILITY.drop_it('table', mesh_other_concepts);
	MEME_UTILITY.drop_it('table', mh_et_rels);
	MEME_UTILITY.drop_it('table', safe_reps);
	MEME_UTILITY.drop_it('table', same_rels);
	meme_integrity_proc_error('msh_et_synonym_help2',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_et_synonym_help2;

/* FUNCTION MSH_MH_DIFF ********************************************************
 */
FUNCTION msh_mh_diff (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    pnw 		VARCHAR2(10);
    cnw 		VARCHAR2(10);
    bcd			VARCHAR2(100);
    result_table	VARCHAR2(50);
    t_mh_diff		VARCHAR2(50);
    location		VARCHAR2(256);
    current_msh		VARCHAR2(40);
    previous_msh	VARCHAR2(40);

BEGIN

    location :='0';

    current_msh := MEME_UTILITY.get_current_name('MSH');
    previous_msh := MEME_UTILITY.get_previous_name('MSH');

    location :='10';

    bcd := MEME_UTILITY.get_unique_tablename('qat_');
    t_mh_diff := MEME_UTILITY.get_unique_tablename('qat_');

    pnw := previous_msh || '%';
    cnw := current_msh || '%';

    -- Get D% and Q% main headings for previous and current years
    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN

	restriction_clause := ' AND concept_id IN (SELECT ' ||
			      'concept_id FROM ' || table_name || ') ';

	location :='30';

	local_exec(
	    'CREATE TABLE ' || bcd || '_pre AS ' ||
	    'SELECT code ' ||
	    'FROM classes ' ||
	    'WHERE source IN ' ||
	    '	  (''' || current_msh || ''' , ''' || previous_msh || ''') ' ||
	    'AND (termgroup LIKE ''%MH'' OR ' ||
	    '	  termgroup LIKE ''%HT'' OR ' ||
	    '	  termgroup LIKE ''%GQ'' OR ' ||
	    '	  termgroup LIKE ''%LQ'' OR ' ||
	    '	  termgroup LIKE ''%TQ'' OR ' ||
	    '	  termgroup LIKE ''%NM'') ' ||
	    restriction_clause
	);

	location :='40';

	local_exec (
	    'CREATE TABLE ' || bcd || ' AS ' ||
	    'SELECT concept_id, lui, atom_id, source, termgroup, code ' ||
	    'FROM classes ' ||
	    'WHERE source IN ' ||
	    '	  (''' || current_msh || ''' , ''' || previous_msh || ''') ' ||
	    'AND (termgroup LIKE ''%MH'' OR ' ||
	    '	  termgroup LIKE ''%HT'' OR ' ||
	    '	  termgroup LIKE ''%GQ'' OR ' ||
	    '	  termgroup LIKE ''%LQ'' OR ' ||
	    '	  termgroup LIKE ''%TQ'' OR ' ||
	    '	   termgroup LIKE ''%NM'') ' ||
	    'AND code IN (SELECT code FROM ' || bcd || '_pre)'
	);

    ELSE

	location :='50';

	local_exec(
	    'CREATE TABLE ' || bcd || ' AS ' ||
	    'SELECT concept_id, lui, atom_id, source, termgroup, code ' ||
	    'FROM classes ' ||
	    'WHERE source IN ' ||
	    '	  (''' || current_msh || ''' , ''' || previous_msh || ''') ' ||
	    'AND (termgroup LIKE ''%MH'' OR ' ||
	    '	  termgroup LIKE ''%HT'' OR ' ||
	    '	  termgroup LIKE ''%GQ'' OR ' ||
	    '	  termgroup LIKE ''%LQ'' OR ' ||
	    '	  termgroup LIKE ''%TQ'' OR ' ||
	    '	  termgroup LIKE ''%NM'') '
	);

	restriction_clause := '';

    END IF;

    location :='30';

    -- Get cases where codes are the same but concepts are different
    local_exec(
	'CREATE TABLE ' || t_mh_diff || ' AS ' ||
	'SELECT a.concept_id as concept_id_1, b.concept_id as concept_id_2 ' ||
	'FROM ' || bcd || ' a, ' || bcd || ' b ' ||
	'WHERE a.code = b.code ' ||
	'AND a.source = ''' || previous_msh || ''' ' ||
	'AND b.source = ''' || current_msh || ''' ' ||
	'AND a.concept_id != b.concept_id'
	);

    -- If cluster_flag is MEME_CONSTANTS.CLUSTER_YES, cluster it
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

    	location :='40';
	result_table := MEME_UTILITY.cluster_pair(t_mh_diff);

    -- Otherwise just put concept_ids together
    -- Don't restrict this set because the updater
    -- Needs to know all of the concept ids because some may
    -- not have been directly touched by actions
    ELSE

    	location :='50';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');

    	location :='60';

	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT concept_id_1 as concept_id ' ||
	    'FROM ' || t_mh_diff || ' ' ||
	    'UNION ' ||
	    'SELECT concept_id_2 ' ||
	    'FROM ' || t_mh_diff
	);

    END IF;

    location :='70';

    MEME_UTILITY.drop_it('table',bcd);
    MEME_UTILITY.drop_it('table',bcd || '_pre');
    MEME_UTILITY.drop_it('table',t_mh_diff);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',bcd);
	MEME_UTILITY.drop_it('table',bcd || '_pre');
	MEME_UTILITY.drop_it('table',t_mh_diff);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('msh_mh_diff',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_mh_diff;

/* FUNCTION SFO_LFO ************************************************************
 */
FUNCTION sfo_lfo (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause_1  VARCHAR2(128);
    restriction_clause_2  VARCHAR2(128);
    t_sfo_lfo		VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause_1 :=
	 ' and concept_id_1 IN (select concept_id from ' || table_name || ') ';
	restriction_clause_2 :=
	 ' and concept_id_2 IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause_1 := ' ';
	restriction_clause_2 := ' ';
    END IF;

    location := '0';

    t_sfo_lfo := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';

    -- Get all relationships in restriction set
    local_exec(
	'CREATE TABLE ' || t_sfo_lfo || ' AS 
	 SELECT atom_id_1, atom_id_2, concept_id_1, 
		concept_id_2 FROM relationships
	WHERE concept_id_1 != concept_id_2
	AND relationship_name IN (''SFO'',''LFO'',''SFO/LFO'') ' ||
	restriction_clause_1 || '
	UNION
	SELECT atom_id_1, atom_id_2,
	       concept_id_1, concept_id_2 FROM relationships
	WHERE concept_id_1 != concept_id_2 AND
	relationship_name IN (''SFO'',''LFO'',''SFO/LFO'') ' ||
	restriction_clause_2);

    location := '15';
    local_exec (
	'DELETE FROM ' || t_sfo_lfo || ' 
	 WHERE atom_id_1 in (SELECT Atom_id FROM classes
			     WHERE tobereleased in (''N'',''n''))');

    location := '16';
    local_exec (
	'DELETE FROM ' || t_sfo_lfo || ' 
	 WHERE atom_id_2 in (SELECT atom_id FROM classes
			     WHERE tobereleased in (''N'',''n''))');

    -- If cluster_flag is MEME_CONSTANTS.CLUSTER_YES, cluster it
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

    	location := '20';
	result_table := MEME_UTILITY.cluster_pair(t_sfo_lfo);

    -- Otherwise just put concept_ids together
    -- Don't restrict this set because the updater
    -- Needs to know all of the concept ids because some may
    -- not have been directly touched by actions
    ELSE
    	location := '30';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');

    	location := '40';
	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT concept_id_1 as concept_id ' ||
	    'FROM ' || t_sfo_lfo || ' ' ||
	    'UNION ' ||
	    'SELECT concept_id_2 ' ||
	    'FROM ' || t_sfo_lfo
	);

    END IF;

    MEME_UTILITY.drop_it('table',t_sfo_lfo);

    return result_table;

EXCEPTIoN
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_sfo_lfo);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('sfo_lfo',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END sfo_lfo;

/* FUNCTION NH_STY *************************************************************
 */
FUNCTION nh_sty (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    t_nhsty	    	VARCHAR2(50);
    t_non_human		VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);
BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	 ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='10';

    t_non_human := meme_integrity_proc.non_human(table_name);

    location :='20';

    t_nhsty := MEME_UTILITY.get_unique_tablename('qat_');

    location :='30';

    -- Remove those with legal stys, no need to restrict non_human
    -- table is already restricted.
    local_exec(
	'CREATE TABLE ' || t_nhsty || ' AS ' ||
	'SELECT concept_id FROM ' || t_non_human || ' ' ||
	'MINUS ' ||
	'SELECT concept_id FROM attributes ' ||
    	'WHERE attribute_name = ''SEMANTIC_TYPE'' ' ||
    	'AND attribute_value IN ' ||
	' (SELECT sty FROM nhsty )');

    result_table := t_nhsty;

    -- If cluster_flag then cluster
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location :='40';
	result_table := MEME_UTILITY.cluster_single(t_nhsty);

    ELSE

	result_table := t_nhsty;
	t_nhsty := '';

    END IF;

    location :='50';
    MEME_UTILITY.drop_it('table',t_non_human);
    MEME_UTILITY.drop_it('table',t_nhsty);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_non_human);
	MEME_UTILITY.drop_it('table',t_nhsty);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('non_human',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END nh_sty;

/* FUNCTION SEPARATED_PM *******************************************************
 */
FUNCTION separated_pm (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    lone_pm		VARCHAR2(50);
    location		VARCHAR2(256);
    sep_pm		VARCHAR2(50);
    cluster_table	VARCHAR2(50);
    result_table	VARCHAR2(50);
    current_msh		VARCHAR2(40);

BEGIN

    location := '0';
    current_msh := MEME_UTILITY.get_current_name('MSH');

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	 ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location :='10';
    lone_pm := MEME_UTILITY.get_unique_tablename('qat_');
    sep_pm := MEME_UTILITY.get_unique_tablename('qat_');

    location :='20';

    -- Get PMs
    local_exec(
	'CREATE TABLE ' || lone_pm || ' AS ' ||
	'SELECT DISTINCT concept_id, code ' ||
	'FROM classes WHERE source = ''' || current_msh || ''' ' ||
	'AND termgroup LIKE ''%PM'' ' ||
	restriction_clause ||
	'AND code like ''D%'' '
	);

    location :='30';

    -- get PM's with other MeSH
    local_exec(
	'CREATE TABLE ' || lone_pm || '_pre AS ' ||
	'SELECT a.concept_id, a.code ' ||
	'FROM ' || lone_pm || ' a, classes b ' ||
	'WHERE a.concept_id = b.concept_id ' ||
	'AND a.code = b.code ' ||
	'AND b.source = ''' || current_msh || ''' ' ||
	'AND (b.termgroup like ''%EP'' OR ' ||
	'     b.termgroup like ''%EN'' OR ' ||
	'     b.termgroup like ''%MH'' )'
	);

    location := '40';

    -- Get PM without other MeSH
    local_exec(
	'CREATE TABLE ' || sep_pm || ' AS ' ||
	'SELECT DISTINCT concept_id, code ' ||
	'FROM ' || lone_pm || ' ' ||
	'MINUS ' ||
	'SELECT concept_id, code FROM ' || lone_pm || '_pre');

    location := '60';
    cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

    location := '65';
    local_exec (
	'CREATE TABLE ' || cluster_table || '_pre AS ' ||
	'SELECT DISTINCT a.concept_id as concept_id_1, ' ||
	'	b.concept_id as concept_id_2, ' ||
	'	to_char(a.concept_id) as cluster_id ' ||
	'FROM ' || sep_pm || ' a, classes b ' ||
	'WHERE a.concept_id != b.concept_id ' ||
	'AND a.code = b.code ' ||
	'AND b.source = ''' || current_msh || ''' ' ||
	'AND (b.termgroup like ''%EP'' OR ' ||
	'     b.termgroup like ''%EN'' OR ' ||
	'     b.termgroup like ''%MH'' )'
	);

    -- If clustering, pick up things lone_pm might be attached to.
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

    	location := '70';
	local_exec (
	    'CREATE TABLE ' || cluster_table || ' AS ' ||
	    'SELECT concept_id_1 as concept_id, cluster_id ' ||
	    'FROM ' || cluster_table || '_pre ' ||
	    'UNION ' ||
	    'SELECT concept_id_2, cluster_id ' ||
	    'FROM ' || cluster_table || '_pre '
	);

	result_table := MEME_UTILITY.recluster(cluster_table);

    ELSE

    	location := '80';
    	result_table := MEME_UTILITY.get_unique_tablename('qat_');

	location := '85';
	local_exec (
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT distinct concept_id FROM ' || sep_pm
	);

    END IF;

    -- Resolve meme_cluster_history
    location := '85';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('SEPARATED_PM'),
	table_name, cluster_table || '_pre');

    location := '90';

    MEME_UTILITY.drop_it('table',  cluster_table || '_pre');
    MEME_UTILITY.drop_it('table',  cluster_table);
    MEME_UTILITY.drop_it('table',  lone_pm || '_pre');
    MEME_UTILITY.drop_it('table',lone_pm);
    MEME_UTILITY.drop_it('table',  sep_pm);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',  lone_pm || '_pre');
	MEME_UTILITY.drop_it('table',  lone_pm);
	MEME_UTILITY.drop_it('table',  sep_pm);
	MEME_UTILITY.drop_it('table',  cluster_table);
	MEME_UTILITY.drop_it('table',  cluster_table || '_pre');
	MEME_UTILITY.drop_it('table',  result_table);
	meme_integrity_proc_error('separated_pm',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END separated_pm;

/* FUNCTION AMBIG_NO_PN ********************************************************
 */
FUNCTION ambig_no_pn (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause1 VARCHAR2(128);
    restriction_clause2 VARCHAR2(128);
    t_dup		VARCHAR2(50);
    t_ambig_cids	VARCHAR2(50);
    t_ambig_isui	VARCHAR2(50);
    result_table	VARCHAR2(50);
    cluster_table	VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause1 :=
	 ' and concept_id_1 IN (select concept_id from ' || table_name || ') ';
	restriction_clause2 :=
	 ' and concept_id_2 IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause1 := ' ';
	restriction_clause2 := ' ';
    END IF;

    location := '10';

    t_ambig_cids := MEME_UTILITY.get_unique_tablename('qat_');
    t_ambig_isui := MEME_UTILITY.get_unique_tablename('qat_');

    location := '20';

    t_dup := separated_strings( table_name => table_name);

    -- Get concepts with ambiguous strings
    -- Remove those concepts containing PNs
    location := '30';
    local_exec(
	'CREATE TABLE ' || t_ambig_cids || ' AS ' ||
	'(SELECT concept_id_1 AS concept_id ' ||
	'FROM ' || t_dup || ') ' ||
	'UNION SELECT concept_id_2 FROM ' || t_dup || ' ' ||
	'MINUS SELECT concept_id FROM classes ' ||
	'WHERE source=''MTH'' and termgroup like ''%PN'' ' ||
	'AND tobereleased in (''Y'',''y'') '
	);

    location := '40';

    -- Get ambiguous ISUI in concepts w/o PNs
    local_exec(
	'CREATE TABLE ' || t_ambig_isui || ' AS ' ||
	'SELECT isui ' ||
	'FROM ' || t_dup || ' a, ' || t_ambig_cids || ' b ' ||
	'WHERE concept_id_1 = b.concept_id ' ||
	'UNION ' ||
	'SELECT isui ' ||
	'FROM ' || t_dup || ' a, ' || t_ambig_cids || ' b ' ||
	'WHERE concept_id_2 = b.concept_id'
	);

    -- If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location := '40';

	cluster_table := MEME_UTILITY.get_unique_tablename('qat_');
	local_exec(
	    'CREATE TABLE ' || cluster_table || ' AS ' ||
	    'SELECT concept_id_1 AS concept_id, isui as cluster_id ' ||
	    'FROM ' || t_dup || ' ' ||
	    'WHERE isui IN ' ||
	    ' (SELECT isui FROM ' || t_ambig_isui || ') ' ||
	    'UNION ' ||
	    'SELECT concept_id_2, isui FROM ' || t_dup ||
	    ' WHERE isui IN ' ||
	    ' (SELECT isui FROM ' || t_ambig_isui || ') '
    	);

	location := '50';

	result_table := MEME_UTILITY.recluster(cluster_table);
	MEME_UTILITY.drop_it ('table',cluster_table);
    ELSE

	location := '60';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');

	location := '70';
	local_exec(
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT concept_id_1 AS concept_id ' ||
	    'FROM ' || t_dup || ' ' ||
	    'WHERE isui IN ' ||
	    ' (SELECT isui FROM ' || t_ambig_isui || ') ' ||
	    restriction_clause1 ||
	    ' UNION ' ||
	    'SELECT concept_id_2 FROM ' || t_dup ||
	    ' WHERE isui IN ' ||
	    ' (SELECT isui FROM ' || t_ambig_isui || ') ' ||
	    restriction_clause2
	);

    END IF;

    -- Resolve meme_cluster_history
    location := '80';
    cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

    location := '90';
    local_exec (
	'CREATE TABLE ' || cluster_table || ' AS ' ||
	'SELECT distinct concept_id_1, concept_id_2 from ' || t_dup || ' ' ||
	'WHERE isui IN (SELECT isui FROM ' || t_ambig_isui || ')'
	);

    location := '100';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('AMBIG_NO_PN'),
	table_name, cluster_table);

    location := '110';

    MEME_UTILITY.drop_it('table', t_dup);
    MEME_UTILITY.drop_it('table', t_ambig_cids);
    MEME_UTILITY.drop_it('table', t_ambig_isui);
    MEME_UTILITY.drop_it('table', cluster_table);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', t_dup);
	MEME_UTILITY.drop_it('table', t_ambig_cids);
	MEME_UTILITY.drop_it('table', t_ambig_isui);
	MEME_UTILITY.drop_it('table', result_table);
	MEME_UTILITY.drop_it('table', cluster_table);
	meme_integrity_proc_error('ambig_no_pn',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END ambig_no_pn;

/* FUNCTION MULTIPLE_PN ********************************************************
 */
FUNCTION multiple_pn (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    result_table	VARCHAR2(50);
    t_mult_pn		VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	 ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location := '0';

    t_mult_pn := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';

    local_exec(
	'CREATE TABLE ' || t_mult_pn || ' AS ' ||
	'SELECT /*+ PARALLEL(a) */ concept_id FROM classes a ' ||
	'WHERE termgroup like ''%PN'' ' ||
	restriction_clause ||
	'AND source = ''MTH'' ' ||
	'AND tobereleased in (''Y'',''y'') ' ||
	'GROUP BY concept_id HAVING count(*) > 1'
	);

    -- If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

    	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_mult_pn);

    ELSE

	result_table := t_mult_pn;
	t_mult_pn := '';

    END IF;

    location := '30';

    MEME_UTILITY.drop_it('table', t_mult_pn);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', t_mult_pn);
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('multiple_pn',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END multiple_pn;

/* FUNCTION PN_NO_AMBIG ********************************************************
 */
FUNCTION pn_no_ambig (
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_pn_no_ambig	VARCHAR2(50);
    t_dup		VARCHAR2(50);
BEGIN

    -- Get MTH/PN atoms
    location := '0';
    t_pn_no_ambig := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_pn_no_ambig || ' AS ' ||
	'SELECT concept_id ' ||
	'FROM classes ' ||
	'WHERE source=''MTH'' ' ||
	'  AND termgroup=''MTH/PN'' ' ||
	'  AND tobereleased = ''Y'' '
    );

    -- Get concepts with PNs and ambiguous strings
    location := '20';
    t_dup := separated_strings(t_pn_no_ambig);

    -- Remove ambiguous concepts from pn_no_ambig list
    location := '30';
    local_exec (
	'DELETE FROM ' || t_pn_no_ambig || ' ' ||
	'WHERE concept_id IN ' ||
	'(SELECT concept_id_1 FROM ' || t_dup || ' ' ||
	' UNION ' ||
	' SELECT concept_id_2 FROM ' || t_dup || ')'
    );

    --	If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '40';
	result_table := MEME_UTILITY.cluster_single(t_pn_no_ambig);
    ELSE
    	result_table := t_pn_no_ambig; t_pn_no_ambig := '';
    END IF;

    location := '50';
    MEME_UTILITY.drop_it('table', t_dup);
    MEME_UTILITY.drop_it('table', t_pn_no_ambig);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', t_pn_no_ambig);
	MEME_UTILITY.drop_it('table', t_dup);
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('pn_no_ambig',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END pn_no_ambig;

/* FUNCTION PN_PN_AMBIG ********************************************************
 */
FUNCTION pn_pn_ambig (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause 	VARCHAR2(128);
    result_table	VARCHAR2(50);
    t_pn_pn_ambig	VARCHAR2(50);
    t_pn_pn_ambig_isui	VARCHAR2(50);
    t_pn_isui		VARCHAR2(50);
    cluster_table	 VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause := ' and concept_id IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause := ' ';
    END IF;

    location := '0';

    t_pn_isui := MEME_UTILITY.get_unique_tablename('qat_');
    t_pn_pn_ambig_isui := MEME_UTILITY.get_unique_tablename('qat_');
    t_pn_pn_ambig := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';

    -- Get isuis of PNs
    local_exec(
	'CREATE TABLE ' || t_pn_isui || ' AS ' ||
	'SELECT concept_id, isui FROM classes ' ||
	'WHERE tobereleased in (''Y'',''Y'') ' ||
	restriction_clause ||
	'AND source = ''MTH'' and termgroup like ''%PN'' ');

    location := '20';

    -- Get ambiguous concept_ids
    local_exec (
	'CREATE TABLE ' || t_pn_pn_ambig_isui || ' AS ' ||
	'SELECT isui FROM classes ' ||
	'WHERE isui IN (SELECT isui FROM ' || t_pn_isui || ') ' ||
	'AND tobereleased in (''Y'',''y'') ' ||
	'AND source = ''MTH'' and termgroup like ''%PN'' ' ||
	'GROUP BY isui having count(distinct concept_id)>1');

    location := '30';

    -- Get set of concept_ids with dups
    local_exec (
	'CREATE TABLE ' || t_pn_pn_ambig || ' AS ' ||
	'SELECT concept_id, isui FROM classes ' ||
	'WHERE tobereleased in (''Y'',''y'')
           AND isui in (SELECT isui FROM ' || t_pn_pn_ambig_isui || ') ' ||
	restriction_clause );

    -- If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES then

    	location := '40';
	cluster_table := MEME_UTILITY.get_unique_tablename('qat_');
	local_exec (
	    'CREATE TABLE ' || cluster_table || ' AS ' ||
	    'SELECT concept_id, isui as cluster_id ' ||
	    'FROM ' || t_pn_pn_ambig
	);

	result_table := MEME_UTILITY.recluster(cluster_table);
	MEME_UTILITY.drop_it ('table',cluster_table);
    ELSE
    	location := '50';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');
	local_exec (
	    'CREATE TABLE  ' || result_table || ' AS ' ||
	    'SELECT distinct concept_id ' ||
	    'FROM ' || t_pn_pn_ambig );

    END IF;

    -- Resolve meme_cluster_history
    location := '60';
    cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

    location := '70';
    cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

    location := '80';
    local_exec (
	'CREATE TABLE ' || cluster_table || ' AS ' ||
	'SELECT a.concept_id as concept_id_1, '||
	'	b.concept_id as concept_id_2 ' ||
	'FROM ' || t_pn_pn_ambig || ' a, ' || t_pn_pn_ambig || ' b ' ||
	'WHERE a.isui = b.isui'
	);

    location := '90';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('AMBIG_NO_REL'),
	table_name, cluster_table);

    location := '100';

    MEME_UTILITY.drop_it('table', t_pn_isui);
    MEME_UTILITY.drop_it('table', t_pn_pn_ambig_isui);
    MEME_UTILITY.drop_it('table', t_pn_pn_ambig);
    MEME_UTILITY.drop_it('table', cluster_table);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it('table', t_pn_isui);
    	MEME_UTILITY.drop_it('table', t_pn_pn_ambig_isui);
    	MEME_UTILITY.drop_it('table', t_pn_pn_ambig);
    	MEME_UTILITY.drop_it('table', result_table);
    	MEME_UTILITY.drop_it('table', cluster_table);
	meme_integrity_proc_error('pn_pn_ambig',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END pn_pn_ambig;

/* FUNCTION AMBIG_NO_REL *******************************************************
 */
FUNCTION ambig_no_rel (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause1 VARCHAR2(128);
    restriction_clause2 VARCHAR2(128);
    t_dup		VARCHAR2(50);
    t_has_rel		VARCHAR2(50);
    t_dup2		VARCHAR2(50);
    result_table	VARCHAR2(50);
    cluster_table	VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause1 :=
	 ' and concept_id_1 IN (select concept_id from ' || table_name || ') ';
	restriction_clause2 :=
	 ' and concept_id_2 IN (select concept_id from ' || table_name || ') ';
    ELSE
	restriction_clause1 := ' ';
	restriction_clause2 := ' ';
    END IF;

    location := '10';

    t_has_rel := MEME_UTILITY.get_unique_tablename('qat_');
    t_dup2 := MEME_UTILITY.get_unique_tablename('qat_');

    location := '20';

    t_dup := separated_strings(table_name => table_name);

    location := '30';

    -- Get Ambig pairs with relationships
    local_exec(
	'CREATE TABLE ' || t_has_rel || ' AS ' ||
	'SELECT a.concept_id_1, a.concept_id_2, isui ' ||
	'FROM ' || t_dup || ' a, relationships b ' ||
	'WHERE a.concept_id_1 = b.concept_id_1 ' ||
	'AND a.concept_id_2 = b.concept_id_2 ' ||
	'AND tobereleased in (''y'',''Y'',''?'') ' ||
	'AND status in (''U'',''R'') ' ||
	'UNION ' ||
	'SELECT a.concept_id_1, a.concept_id_2,isui ' ||
	'FROM ' || t_dup || ' a, relationships b ' ||
	'WHERE a.concept_id_1 = b.concept_id_2 ' ||
	'AND a.concept_id_2 = b.concept_id_1 ' ||
	'AND status in (''U'',''R'') ' ||
	'AND tobereleased in (''y'',''Y'',''?'')'
	);

    location := '40';

    -- Get Ambig pairs without relationships
    local_exec(
	'CREATE TABLE ' || t_dup2 || ' AS ' ||
	'SELECT concept_id_1, concept_id_2, isui ' ||
	'FROM ' || t_dup || ' ' ||
	'MINUS ' ||
	' (SELECT concept_id_1, concept_id_2, isui ' ||
	'  FROM ' || t_has_rel || ') ' ||
	'MINUS ' ||
	' (SELECT concept_id_2, concept_id_1, isui ' ||
	'  FROM ' || t_has_rel || ') '
	);

    -- If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location := '50';

	cluster_table := MEME_UTILITY.get_unique_tablename('qat_');
	local_exec(
	    'CREATE TABLE ' || cluster_table || ' AS ' ||
	    'SELECT concept_id_1 AS concept_id, isui as cluster_id ' ||
	    'FROM ' || t_dup || ' ' ||
	    'WHERE isui IN ' ||
	    ' (SELECT isui FROM ' || t_dup2 || ') ' ||
	    'UNION ' ||
	    'SELECT concept_id_2, isui FROM ' || t_dup ||
	    ' WHERE isui IN ' ||
	    ' (SELECT isui FROM ' || t_dup2 || ') '
    	);

	location := '60';
	result_table := MEME_UTILITY.recluster(cluster_table);

    ELSE

	location := '70';
	result_table := MEME_UTILITY.get_unique_tablename('qat_');

	location := '80';
	local_exec(
	    'CREATE TABLE ' || result_table || ' AS ' ||
	    'SELECT concept_id_1 AS concept_id ' ||
	    'FROM ' || t_dup || ' ' ||
	    'WHERE isui IN ' ||
	    ' (SELECT isui FROM ' || t_dup2 || ') ' ||
	    restriction_clause1 ||
	    ' UNION ' ||
	    'SELECT concept_id_2 FROM ' || t_dup ||
	    ' WHERE isui IN ' ||
	    ' (SELECT isui FROM ' || t_dup2 || ') ' ||
	    restriction_clause2
	);

    END IF;

    -- Resolve meme_cluster_history
    location := '90';
    cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

    location := '100';
    local_exec (
	'CREATE TABLE ' || cluster_table || ' AS ' ||
	'SELECT distinct concept_id_1, concept_id_2 from ' || t_dup || ' ' ||
	'WHERE isui IN (SELECT isui FROM ' || t_dup2 || ')'
	);

    location := '110';
    recompute_cluster_history(
	MEME_UTILITY.get_ic_by_procedure_name('AMBIG_NO_REL'),
	table_name, cluster_table);

    location := '120';

    MEME_UTILITY.drop_it('table', t_dup);
    MEME_UTILITY.drop_it('table', t_has_rel);
    MEME_UTILITY.drop_it('table', t_dup2);
    MEME_UTILITY.drop_it('table', cluster_table);

    return result_table;


EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', t_dup);
	MEME_UTILITY.drop_it('table', t_has_rel);
	MEME_UTILITY.drop_it('table', t_dup2);
	MEME_UTILITY.drop_it('table', result_table);
	MEME_UTILITY.drop_it('table', cluster_table);
	meme_integrity_proc_error('ambig_no_rel',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END ambig_no_rel;

/* FUNCTION MULTIPLE_MM ********************************************************
 */
FUNCTION multiple_mm (
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    t_multiple_mm	VARCHAR2(50);
    t_mms		VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    -- Get MTH/MM atoms with stripped strings
    location := '0';
    t_mms := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_mms || ' (concept_id,ambig) AS ' ||
	'SELECT concept_id, ' ||
 	'	UPPER(atom_name) ' ||
 	'FROM classes c, atoms a ' ||
	'WHERE a.atom_id = c.atom_id ' ||
 	'  AND c.source = ''MTH'' ' ||
	'  AND c.termgroup=''MTH/MM'' ' ||
	'  AND c.tobereleased NOT IN (''n'',''N'') '
    );

    location := '20';
    local_exec (
	'UPDATE ' || t_mms || ' ' ||
	'SET ambig = substr(ambig,0, length(ambig)-4) ' ||
   	' WHERE ambig like ''% <_>'' ');

    location := '30';
    local_exec (
	'UPDATE ' || t_mms || ' ' ||
	'SET ambig = substr(ambig,0, length(ambig)-5) ' ||
   	' WHERE ambig like ''% <__>'' ');

   -- Get concepts with multiple MMs with the same ambig string
    location := '40';
    t_multiple_mm := MEME_UTILITY.get_unique_tablename('qat_');
    location := '50';
    local_exec (
	'CREATE TABLE ' || t_multiple_mm || ' AS ' ||
	'SELECT distinct concept_id ' ||
	'FROM ' || t_mms || ' ' ||
	'GROUP BY concept_id, ambig ' ||
	'HAVING count(*) > 1'
    );

    --	If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '90';
	result_table := MEME_UTILITY.cluster_single(t_multiple_mm);
    ELSE
    	result_table := t_multiple_mm;
	t_multiple_mm := '';
    END IF;

    location := '100';
    MEME_UTILITY.drop_it('table', t_mms);
    MEME_UTILITY.drop_it('table', t_multiple_mm);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', t_mms);
	MEME_UTILITY.drop_it('table', t_multiple_mm);
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('multiple_mm',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END multiple_mm;

/* FUNCTION MM_NO_AMBIG ********************************************************
 */
FUNCTION mm_no_ambig (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    t_dup		VARCHAR2(50);
    t_pn_mm		VARCHAR2(50);
    result_table		VARCHAR2(50);
    location		VARCHAR2(256);

BEGIN

    location := '0';

    -- Get ambiguous ISUIs
    t_dup := separated_strings(table_name => table_name);

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause :=
	  ' and concept_id IN (select concept_id from ' || table_name || ') ';

    ELSE
	restriction_clause := ' ';
    END IF;

    location := '20';

    t_pn_mm := MEME_UTILITY.get_unique_tablename('qat_');

    location := '30';

    -- Get concepts with releasable MM atoms, and subtract ambiguous concepts

    location := '40';

    local_exec(
	'CREATE TABLE ' || t_pn_mm || ' AS ' ||
	'SELECT DISTINCT concept_id ' ||
	'FROM classes ' ||
	'WHERE source = ''MTH'' ' ||
	'AND tobereleased in (''y'',''Y'') ' ||
	restriction_clause ||
	'AND termgroup like ''%MM'' ' ||
	'MINUS ' ||
	' (SELECT concept_id_1 FROM ' || t_dup || ') ' ||
	'MINUS ' ||
	' (SELECT concept_id_2 FROM ' || t_dup || ') '
	);

    --	If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location := '50';
	result_table := MEME_UTILITY.cluster_single(t_pn_mm);

    ELSE

    	result_table := t_pn_mm;
   	t_pn_mm := '';

    END IF;

    location := '60';

    MEME_UTILITY.drop_it('table', t_dup);
    MEME_UTILITY.drop_it('table', t_pn_mm);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', result_table);
	MEME_UTILITY.drop_it('table', t_pn_mm);
	MEME_UTILITY.drop_it('table', t_dup);
	meme_integrity_proc_error('mm_no_ambig',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END mm_no_ambig;

/* FUNCTION MM_MISALIGN ********************************************************
 */
FUNCTION mm_misalign (
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_mm_misalign 	VARCHAR2(50);
    t_mms	 	VARCHAR2(50);
BEGIN

    -- Get MTH/MM atoms with stripped strings
    location := '0';
    t_mms := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_mms || ' (concept_id,lui,ambig) AS ' ||
	'SELECT concept_id, lui, ' ||
 	'	UPPER(atom_name) ' ||
 	'FROM classes c, atoms a ' ||
	'WHERE a.atom_id = c.atom_id ' ||
	'  AND c.termgroup=''MTH/MM'' ' ||
	'  AND c.tobereleased NOT IN (''n'',''N'') '
    );

    location := '20';
    local_exec (
	'UPDATE ' || t_mms || ' ' ||
	'SET ambig = substr(ambig,0, length(ambig)-4) ' ||
   	' WHERE ambig like ''% <_>'' ');

    location := '30';
    local_exec (
	'UPDATE ' || t_mms || ' ' ||
	'SET ambig = substr(ambig,0, length(ambig)-5) ' ||
   	' WHERE ambig like ''% <__>'' ');


    -- Get concepts with multiple MMs with the same ambig string
    location := '40';
    local_exec (
	'CREATE TABLE ' || t_mms || '_dup AS ' ||
	'SELECT concept_id, ambig, lui ' ||
	'FROM ' || t_mms || ' ' ||
	'GROUP BY concept_id, ambig, lui ' ||
	'HAVING count(*)>1 '
    );

    -- We now have the table of concepts containing duplicate MTH/MM atoms
    -- Join this on classes/atoms and get cases where atoms exist which
    -- match the ambig string but no the concept_id
    location := '50';
    t_mm_misalign := MEME_UTILITY.get_unique_tablename('qat_');
    location := '60';
    local_exec (
	'CREATE TABLE ' || t_mm_misalign || '_pre AS ' ||
	'SELECT /*+ RULE */ a.concept_id as concept_id_1, ' ||
	'	b.concept_id as concept_id_2, ambig ' ||
	'FROM ' || t_mms || '_dup a, classes b, atoms c ' ||
	'WHERE a.concept_id != b.concept_id ' ||
	'AND b.atom_id=c.atom_id ' ||
	'AND a.lui = b.lui ' ||
	'AND b.tobereleased not in (''n'',''N'') ' ||
	'AND ambig = UPPER(atom_name) ' );

    -- If the concept_id_2 actually contains an atom with
    -- a matching MTH/MM atom, it should be removed
    location := '70';
    local_exec (
	'DELETE FROM ' || t_mm_misalign || '_pre a ' ||
	'WHERE concept_id_2 IN ' ||
	'(SELECT concept_id FROM ' || t_mms || ' b ' ||
	' WHERE a.ambig = b.ambig)'
    );

    -- Uniq the results
    location := '80';
    local_exec (
	'CREATE TABLE ' || t_mm_misalign || ' AS ' ||
	'SELECT concept_id_1 as concept_id, ambig as cluster_id ' ||
	'FROM ' || t_mm_misalign || '_pre ' ||
	'UNION ' ||
	'SELECT concept_id_2, ambig ' ||
	'FROM ' || t_mm_misalign || '_pre' );

    --	If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '90';
	result_table := MEME_UTILITY.recluster(t_mm_misalign);
    ELSE
    	result_table := t_mm_misalign;
    END IF;

    location := '100';
    MEME_UTILITY.drop_it('table', t_mms);
    MEME_UTILITY.drop_it('table', t_mms || '_dup');
    IF result_table != t_mm_misalign THEN
    	MEME_UTILITY.drop_it('table', t_mm_misalign);
    END IF;
    MEME_UTILITY.drop_it('table', t_mm_misalign || '_pre');

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', result_table);
	MEME_UTILITY.drop_it('table', t_mms);
	MEME_UTILITY.drop_it('table', t_mms || '_dup');
	MEME_UTILITY.drop_it('table', t_mm_misalign);
	MEME_UTILITY.drop_it('table', t_mm_misalign || '_pre');
	meme_integrity_proc_error('mm_misalign',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END mm_misalign;

/* FUNCTION SEPARATED_STRING ***************************************************
 */
FUNCTION separated_strings (
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    classes		VARCHAR2(50);
    t_ss		 VARCHAR2(50);
    result_table	 VARCHAR2(50);
    location		 VARCHAR2(256);

BEGIN

    location := '0';
    t_ss := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';
    local_exec(
	'CREATE TABLE ' || t_ss || 
	' AS SELECT * FROM separated_strings_include_pn');

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
        location := '20';
        local_exec(
           'DELETE FROM ' || t_ss || 
           ' WHERE concept_id_1 NOT IN (SELECT concept_id FROM ' || table_name ||
           ' )');

        location := '30';
        local_exec(
           'DELETE FROM ' || t_ss || 
           ' WHERE concept_id_2 NOT IN (SELECT concept_id FROM ' || table_name ||
           ' )');
    END IF;	
    
    -- If cluster_flag = MEME_CONSTANTS.CLUSTER_YES, cluster results
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

   	location := '60';
	local_exec(
	    'CREATE TABLE ' || t_ss || '_cluster AS ' ||
	    'SELECT concept_id_1 AS concept_id, isui as cluster_id ' ||
	    'FROM ' || t_ss || ' ' ||
	    'UNION ' ||
	    'SELECT concept_id_2, isui FROM ' || t_ss
    	);

	location := '70';
	result_table := MEME_UTILITY.recluster(t_ss || '_cluster');

    ELSE
  	result_table := t_ss;
    END IF;

    location := '80';
    MEME_UTILITY.drop_it('table',  t_ss || '_ambig_isui');
    MEME_UTILITY.drop_it('table',  t_ss || '_cluster');
    IF result_table != t_ss THEN
	MEME_UTILITY.drop_it('table',  t_ss);
    END IF;
    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	MEME_UTILITY.drop_it('table', classes);
	MEME_UTILITY.drop_it('table', t_ss || '_classes');
    END IF;

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	    MEME_UTILITY.drop_it('table', classes);
	END IF;
	MEME_UTILITY.drop_it('table',t_ss );
	MEME_UTILITY.drop_it('table',t_ss || '_classes');
	MEME_UTILITY.drop_it('table',t_ss || '_ambig_isui');
	MEME_UTILITY.drop_it('table',t_ss || '_cluster');
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('separated_strings',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END separated_strings;


/*******************************************************************************
 *
 *  Extra MM Queries
 *
 * Following is code to implement QA bin code for suresh
 *
 ******************************************************************************/

/* FUNCTION AMBIG_PN ***********************************************************
 * This procedure returns concepts which have MTH/PN
 * atoms which are themselves ambiguous but do not have any
 * matching underlying ambiguous strings.
 */
FUNCTION ambig_pn (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    t_ambig_pn		VARCHAR2(50);
    t_dup		VARCHAR2(50);
    result_table	VARCHAR2(50);
BEGIN
    initialize_trace('ambig_pn');

    -- Get ambiguous PNs (concept_id,isui)
    location := '0';
    t_ambig_pn := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';
    local_exec (
	'CREATE TABLE ' || t_ambig_pn || '_isui AS
	 SELECT isui FROM classes
     	 WHERE source = ''MTH'' AND termgroup = ''MTH/PN''
	   AND tobereleased = ''Y''
	 GROUP BY isui HAVING count(distinct concept_id)>1'
    );

    location := '20';
    local_exec (
	'CREATE TABLE ' || t_ambig_pn || ' AS ' ||
	'SELECT concept_id, isui FROM classes ' ||
     	'WHERE source = ''MTH'' AND termgroup = ''MTH/PN'' ' ||
	'  AND tobereleased = ''Y'' ' ||
	'AND isui IN (SELECT isui FROM ' || t_ambig_pn || '_isui)'
    );

    location := '30';
    MEME_UTILITY.drop_it ('table', t_ambig_pn || '_isui');

    -- Get ambiguous strings (concept_id,isui)
    location := '40';
    t_dup := separated_strings( cluster_flag => MEME_CONSTANTS.CLUSTER_NO );

    -- Delete where concept_id,isui same
    location := '50';
    local_exec (
	'DELETE FROM ' || t_ambig_pn || ' a ' ||
	'WHERE exists  ' ||
	' (SELECT 1 FROM ' || t_dup || ' b ' ||
	'  WHERE a.concept_id = concept_id_1 ' ||
 	'    AND a.isui = b.isui) '
    );
    location := '60';
    local_exec (
	'DELETE FROM ' || t_ambig_pn || ' a ' ||
	'WHERE exists ' ||
	' (SELECT 1 FROM ' || t_dup || ' b ' ||
	'  WHERE a.concept_id = concept_id_2 ' ||
 	'    AND a.isui = b.isui) '
    );

    -- Cluster single
    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
    	location := '70';
	result_table := MEME_UTILITY.cluster_single(t_ambig_pn);
    ELSE
	result_table := t_ambig_pn;
    END IF;


    -- Cleanup
    location := '80';
    MEME_UTILITY.drop_it ('table', t_dup);
    MEME_UTILITY.drop_it ('table', t_ambig_pn || '_sui');
    IF result_table != t_ambig_pn THEN
	MEME_UTILITY.drop_it ('table',t_ambig_pn);
    END IF;

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it ('table',t_ambig_pn || '_isui');
    	MEME_UTILITY.drop_it ('table',t_ambig_pn);
    	MEME_UTILITY.drop_it ('table',t_dup);
    	MEME_UTILITY.drop_it ('table',result_table);
	meme_integrity_proc_error(method,location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END ambig_pn;

/* FUNCTION PURE_U_AMBIG_NO_PN *************************************************
 */
FUNCTION pure_u_ambig_no_pn (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_pure_u		VARCHAR2(50);
     t_dup		VARCHAR2(50);
BEGIN

    --	Get ambig pairs
    location := '0';
    t_dup := separated_strings;

    -- Get pure-u concepts without PNs
    location := '10';
    t_pure_u := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_pure_u || ' AS ' ||
	'SELECT DISTINCT concept_id FROM classes ' ||
	'WHERE status = ''U'' AND tobereleased not in (''n'',''N'') ' ||
	'MINUS ' ||
	'SELECT concept_id FROM classes ' ||
	'WHERE status = ''R'' AND tobereleased not in (''n'',''N'') ' ||
	'MINUS ' ||
	'SELECT concept_id FROM classes ' ||
	'WHERE source =''MTH'' AND termgroup = ''MTH/PN'' ' ||
	'  AND tobereleased = ''Y'' ');


    -- Get a cluster table
    location := '30';
    local_exec(
	'CREATE TABLE ' || t_pure_u || '_cluster AS ' ||
	'SELECT concept_id_1 as concept_id, isui as cluster_id ' ||
	'FROM ' || t_dup || ' a, ' || t_pure_u || ' b ' ||
	'WHERE concept_id_2 = b.concept_id ' ||
	'UNION ' ||
	'SELECT concept_id_2, isui ' ||
	'FROM ' || t_dup || ' a, ' || t_pure_u || ' b ' ||
	'WHERE concept_id_1 = b.concept_id ');


    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
    	location := '30';
	result_table := MEME_UTILITY.recluster(t_pure_u || '_cluster');
    ELSE
	result_table := t_pure_u || '_cluster';
    END IF;

    -- Cleanup
    location := '40';
    MEME_UTILITY.drop_it('table',t_dup);
    MEME_UTILITY.drop_it('table',t_pure_u || '_cluster');
    IF result_table != t_pure_u || '_cluster' THEN
	MEME_UTILITY.drop_it('table',t_pure_u);
    END IF;

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_dup);
	MEME_UTILITY.drop_it('table',t_pure_u);
	MEME_UTILITY.drop_it('table',t_pure_u || '_cluster');
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('pure_u_ambig_no_pn',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END  pure_u_ambig_no_pn;

/* FUNCTION APPROVED_TM ********************************************************
 * Find approved concepts containing MTH/TM atoms.
 */
FUNCTION approved_tm (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_approved_tm	VARCHAR2(50);
BEGIN

    -- Get approved concepts with TM atoms
    location := '0';
    t_approved_tm := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_approved_tm || ' AS ' ||
	'SELECT concept_id FROM classes ' ||
	'WHERE source = ''MTH'' AND termgroup = ''MTH/TM'' ' ||
	'INTERSECT ' ||
	'SELECT concept_id FROM concept_status ' ||
	'WHERE status = ''R'' ' );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
    	location := '20';
	result_table := MEME_UTILITY.cluster_single(t_approved_tm);
    ELSE
	result_table := t_approved_tm;
	t_approved_tm := '';
    END IF;

    -- Cleanup
    location := '30';
    MEME_UTILITY.drop_it('table',t_approved_tm);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_approved_tm);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('approved_tm',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END approved_tm;

/* FUNCTION MERGED_TM **********************************************************
 */
FUNCTION merged_tm (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    t_tmms		VARCHAR2(50);
    result_table	VARCHAR2(50);
BEGIN

    -- Get MTH/TM,MTH/MM atoms
    location := '0';
    t_tmms := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_tmms || ' AS ' ||
	'SELECT concept_id, termgroup, c.atom_id, lui, ' ||
 	'	UPPER(atom_name) as atom_name ' ||
 	'FROM classes c, atoms a ' ||
	'WHERE a.atom_id = c.atom_id ' ||
 	'  AND c.source = ''MTH'' ' ||
	'  AND c.termgroup IN (''MTH/MM'',''MTH/TM'') ' ||
	'  AND c.tobereleased NOT IN (''n'',''N'') '
    );

    location := '30';
    local_exec (
	'UPDATE ' || t_tmms || ' ' ||
	'SET atom_name = substr(atom_name,0, length(atom_name)-4) ' ||
   	' WHERE atom_name like ''% <_>'' ');

    location := '40';
    local_exec (
	'UPDATE ' || t_tmms || ' ' ||
	'SET atom_name = substr(atom_name,0, length(atom_name)-5) ' ||
   	' WHERE atom_name like ''% <__>'' ');

    -- get merged tms
    location := '50';
    local_exec (
	'CREATE TABLE ' || t_tmms || '_merged AS ' ||
	'SELECT DISTINCT a.* ' ||
	'FROM ' || t_tmms || ' a, ' || t_tmms || ' b ' ||
	'WHERE a.atom_id != b.atom_id ' ||
	'  AND a.concept_id = b.concept_id ' ||
	'  AND a.atom_name = b.atom_name ' ||
	'  AND a.termgroup=''MTH/TM'' ');

    location := '110';
    local_exec (
	'CREATE TABLE ' || t_tmms || '_distinct AS ' ||
	'SELECT DISTINCT concept_id FROM ' || t_tmms || '_merged' );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	result_table := MEME_UTILITY.cluster_single(t_tmms || '_distinct' );
    ELSE
	result_table := t_tmms || '_merged';
    END IF;

    -- Cleanup
    location := '30';
    MEME_UTILITY.drop_it('table', t_tmms);
    MEME_UTILITY.drop_it('table', t_tmms || '_distinct');
    IF result_table != t_tmms || '_merged' THEN
    	MEME_UTILITY.drop_it('table', t_tmms || '_merged');
    END IF;

    return result_table;

EXCEPTION
    WHEN OTHERS THEN

    	MEME_UTILITY.drop_it('table', t_tmms);
    	MEME_UTILITY.drop_it('table', t_tmms || '_merged');
    	MEME_UTILITY.drop_it('table', t_tmms || '_distinct');
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('merged_tm',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END merged_tm;


/********************************************************************************
 *
 *  QA Bins
 *
 * Following is code to implement QA bin code for suresh
 *
 *******************************************************************************/

/* FUNCTION MXSUPPR ************************************************************
 * This function finds cases of mixed suppressibility
 * among atoms in the same concept_id,lui
 */
FUNCTION mxsuppr (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    t_suppr_cid_lui	VARCHAR2(50);
    t_mxsuppr		VARCHAR2(50);
    result_table	VARCHAR2(50);
BEGIN

    location := '0';
    t_suppr_cid_lui := MEME_UTILITY.get_unique_tablename('qat_');

    location := '10';
    t_mxsuppr := MEME_UTILITY.get_unique_tablename('qat_');

    -- Get cases where count(distinct suppressible) >1
    -- within the concept_id,luis
    location := '30';
    local_exec (
	'CREATE TABLE ' || t_mxsuppr || ' AS ' ||
	'SELECT DISTINCT concept_id ' ||
	'FROM classes ' ||
	'WHERE termgroup IN ' ||
	' (SELECT termgroup FROM termgroup_rank ' ||
	'  WHERE suppressible != ''Y'') ' ||
	'AND termgroup NOT IN (''MTH/MM'',''MTH/TM'') ' ||
	'AND tobereleased IN (''Y'',''Y'') ' ||
	'GROUP BY concept_id, lui ' ||
	'HAVING count(distinct suppressible)>1');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
    	location := '40';
	result_table := MEME_UTILITY.cluster_single(t_mxsuppr);
    ELSE
	result_table := t_mxsuppr;
	t_mxsuppr := '';
    END IF;

    -- Cleanup
    location := '50';
    MEME_UTILITY.drop_it('table',t_mxsuppr);

    location := '';
    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_mxsuppr);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('mxsuppr',location,1,SQLERRM);
    	location := '';
	RAISE MEME_INTEGRITY_PROC_EXC;

END mxsuppr;

/* FUNCTION CHECKSRC ***********************************************************
 * This function finds cases of status 'N' SRC concepts
 */
FUNCTION checksrc (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    t_checksrc		VARCHAR2(50);
    result_table	VARCHAR2(50);
BEGIN

    -- Get all SRC concepts
    location := '0';
    t_checksrc := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_checksrc || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM classes a, concept_status c ' ||
	'WHERE a.source=''SRC'' ' ||
	'  AND a.tobereleased IN (''Y'',''y'') ' ||
	'  AND c.status = ''N'' ' ||
	'  AND a.concept_id=c.concept_id'
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '20';
	result_table := MEME_UTILITY.cluster_single(t_checksrc);
    ELSE
	result_table := t_checksrc;
	t_checksrc := '';
    END IF;

    location := '30';
    MEME_UTILITY.drop_it('table',t_checksrc);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_checksrc);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('checksrc',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END checksrc;

/* FUNCTION MTHU ***************************************************************
 * This function finds cases of concepts
 * containing status 'U' MTH atoms.
 */
FUNCTION mthu (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    t_mthu		VARCHAR2(50);
    result_table	VARCHAR2(50);
BEGIN

    -- Get concepts containing status 'U' MTH atoms
    location := '0';
    t_mthu := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_mthu || ' AS ' ||
	'SELECT DISTINCT concept_id ' ||
	'FROM classes ' ||
	'WHERE source= ''MTH'' AND status = ''U'' ' ||
	'  AND tobereleased in (''Y'',''y'') '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '20';
	result_table := MEME_UTILITY.cluster_single(t_mthu);
    ELSE
	result_table := t_mthu;
	t_mthu := '';
    END IF;

    location := '30';
    MEME_UTILITY.drop_it ('table',t_mthu);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_mthu);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('mthu',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END mthu;

/* FUNCTION MSH_MRG ************************************************************
 * This function finds cases of different MSH
 * codes merged into the same concept
 */
FUNCTION msh_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_msh_mrg		VARCHAR2(50);
    t_msh		VARCHAR2(50);
    current_msh		VARCHAR2(40);
BEGIN


    -- Get current version of MSH
    current_msh := MEME_UTILITY.get_current_name('MSH');

    -- Get MSH atoms
    location := '0';
    t_msh := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_msh || ' AS ' ||
	'SELECT concept_id, termgroup, code FROM classes ' ||
	'WHERE source = ''' || current_msh || ''' ' ||
	'  AND tobereleased in (''Y'',''y'') '
    );

    -- Get diff code merges
    location := '20';
    t_msh_mrg := MEME_UTILITY.get_unique_tablename('qat_');
    location := '30';
    local_exec (
	'CREATE TABLE ' || t_msh_mrg || ' AS ' ||
	'SELECT a.concept_id, a.termgroup as t1, a.code as c1, ' ||
	'	b.termgroup as t2, b.code as c2 ' ||
	'FROM ' || t_msh || ' a, ' || t_msh || ' b ' ||
	'WHERE a.concept_id = b.concept_id ' ||
	'  AND a.code < b.code'
    );

    -- Remove exceptions
    location := '40';
    local_exec (
	'DELETE FROM ' || t_msh_mrg || ' ' ||
	'WHERE c1 like ''D%'' ' ||
	'  AND (t2 like ''MSH%GQ'' OR t2 like ''MSH%XQ'') '
    );

    location := '50';
    local_exec (
	'DELETE FROM ' || t_msh_mrg || ' ' ||
	'WHERE c2 like ''D%'' ' ||
	'  AND (t1 like ''MSH%GQ'' OR t1 like ''MSH%XQ'') '
    );

    -- Remove bad merges due to publication type atoms
    location := '60';
    local_exec (
	'DELETE FROM ' || t_msh_mrg || ' ' ||
	'WHERE c1 in ' ||
	'(SELECT code FROM classes a, attributes b ' ||
	' WHERE a.atom_id=b.atom_id ' ||
	'   AND attribute_name=''DC'' ' ||
	'   AND attribute_value=''2'')'
    );

    location := '70';
    local_exec (
	'DELETE FROM ' || t_msh_mrg || ' ' ||
	'WHERE c2 in ' ||
	'(SELECT code FROM classes a, attributes b ' ||
	' WHERE a.atom_id=b.atom_id ' ||
	'   AND attribute_name=''DC'' ' ||
	'   AND attribute_value=''2'')'
    );

    -- Distinct the remaining concept_ids
    location := '80';
    local_exec (
	'CREATE TABLE ' || t_msh_mrg || '_2 AS ' ||
	'SELECT DISTINCT concept_id FROM ' || t_msh_mrg );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '90';
	result_table := MEME_UTILITY.cluster_single(t_msh_mrg || '_2');
    ELSE
	result_table := t_msh_mrg || '_2';
	t_msh_mrg := '';
    END IF;

    location := '100';
    MEME_UTILITY.drop_it ('table',t_msh);
    MEME_UTILITY.drop_it ('table',t_msh_mrg);
    MEME_UTILITY.drop_it ('table',t_msh_mrg || '_2');

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_msh);
	MEME_UTILITY.drop_it('table',t_msh_mrg);
	MEME_UTILITY.drop_it('table',t_msh_mrg || '_2');
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('msh_mrg',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_mrg;

/* FUNCTION MSH_SEP ************************************************************
 */
FUNCTION msh_sep (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_msh_sep		VARCHAR2(50);
    current_msh		VARCHAR2(40);
    previous_msh	VARCHAR2(40);

BEGIN

    -- Get previous/current names for MSH
    location := '0';
    current_msh := MEME_UTILITY.get_current_name('MSH');
    location := '10';
    previous_msh := MEME_UTILITY.get_previous_name('MSH');

    -- Get bad MSH splits
    location := '30';
    t_msh_sep := MEME_UTILITY.get_unique_tablename('qat_');
    location := '40';
    local_exec (
	'CREATE TABLE ' || t_msh_sep || ' AS ' ||
	'SELECT DISTINCT a.concept_id as concept_id_1, ' ||
	'	b.concept_id as concept_id_2 ' ||
	'FROM classes a, classes b '  ||
	'WHERE a.concept_id != b.concept_id ' ||
	'  AND a.code = b.code ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.termgroup IN ( ' ||
	'''' || previous_msh || '/MH'', ' ||
	'''' || previous_msh || '/HT'', ' ||
	'''' || previous_msh || '/GQ'', ' ||
	'''' || previous_msh || '/LQ'', ' ||
	'''' || previous_msh || '/TQ'', ' ||
	'''' || previous_msh || '/NM'') ' ||
 	'  AND b.termgroup IN (' ||
	'''' || current_msh || '/MH'', ' ||
	'''' || current_msh || '/HT'', ' ||
	'''' || current_msh || '/GQ'', ' ||
	'''' || current_msh || '/LQ'', ' ||
	'''' || current_msh || '/TQ'', ' ||
	'''' || current_msh || '/NM'') ' );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '50';
	result_table := MEME_UTILITY.cluster_pair(t_msh_sep);
    ELSE
	result_table := t_msh_sep;
	t_msh_sep := '';
    END IF;

    location := '60';
    MEME_UTILITY.drop_it ('table',t_msh_sep);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_msh_sep);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('msh_sep',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;
END msh_sep;

/* FUNCTION MSH_N1 *************************************************************
 */
FUNCTION msh_n1 (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_msh_n1		VARCHAR2(50);
    current_msh		VARCHAR2(40);

BEGIN

    -- Get previous/current names for MSH
    location := '0';
    current_msh := MEME_UTILITY.get_current_name('MSH');

    -- Get bad MSH splits
    location := '30';
    t_msh_n1 := MEME_UTILITY.get_unique_tablename('qat_');
    location := '40';
    local_exec (
	'CREATE TABLE ' || t_msh_n1 || ' AS ' ||
	'SELECT DISTINCT a.concept_id as concept_id_1, ' ||
	'	b.concept_id as concept_id_2 ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.termgroup IN ( ''' || current_msh ||
			'/MH'', ''' || current_msh || '/NM'' ) ' ||
 	'  AND b.termgroup = ''' || current_msh || '/N1'' ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.code = b.code ' ||
	'  AND a.concept_id != b.concept_id'
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '50';
	result_table := MEME_UTILITY.cluster_pair(t_msh_n1);
    ELSE
	result_table := t_msh_n1;
	t_msh_n1 := '';
    END IF;

    location := '60';
    MEME_UTILITY.drop_it ('table',t_msh_n1);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_msh_n1);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('msh_n1',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END msh_n1;

/* FUNCTION RCD_MRG ************************************************************
 */
FUNCTION rcd_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_rcd_mrg		VARCHAR2(50);
    current_rcd		VARCHAR2(40);
BEGIN


    -- Get current version of RCD
    location := '0';
    current_rcd := MEME_UTILITY.get_current_name('RCD');

    -- Get MSH atoms
    location := '10';
    t_rcd_mrg := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_rcd_mrg || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.source IN (''' || current_rcd || ''', ' ||
	'  ''RCDAE'',''RCDSY'',''RCDSA'') ' ||
 	'  AND b.source IN (''' || current_rcd || ''', ' ||
	'  ''RCDAE'',''RCDSY'',''RCDSA'') ' ||
	'  AND a.termgroup not like ''RCD%OP'' ' ||
	'  AND a.termgroup not like ''RCD%OA'' ' ||
	'  AND a.termgroup not like ''RCD%IS'' ' ||
	'  AND b.termgroup not like ''RCD%OP'' ' ||
	'  AND b.termgroup not like ''RCD%OA'' ' ||
	'  AND b.termgroup not like ''RCD%IS'' ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.code < b.code ' ||
	'  AND a.concept_id = b.concept_id '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_rcd_mrg );
    ELSE
	result_table := t_rcd_mrg;
	t_rcd_mrg := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_rcd_mrg);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_rcd_mrg);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('rcd_mrg',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END rcd_mrg;

/* FUNCTION RCD_SEP ************************************************************
 */
FUNCTION rcd_sep (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_rcd_sep		VARCHAR2(50);
    current_rcd		VARCHAR2(40);

BEGIN

    -- Get previous/current names for RCD
    location := '0';
    current_rcd := MEME_UTILITY.get_current_name('RCD');

    -- Get bad RCD splits
    location := '10';
    t_rcd_sep := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_rcd_sep || ' AS ' ||
	'SELECT DISTINCT a.concept_id as concept_id_1, ' ||
	'	b.concept_id as concept_id_2 ' ||
	'FROM classes a, classes b '  ||
	'WHERE a.concept_id != b.concept_id ' ||
	'  AND a.code = b.code ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.termgroup = ''' || current_rcd || '/PT'' ' ||
	'  AND b.termgroup = ''' || current_rcd || '/SY'' ' );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_pair(t_rcd_sep);
    ELSE
	result_table := t_rcd_sep;
	t_rcd_sep := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_rcd_sep);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_rcd_sep);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('rcd_sep',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END rcd_sep;
 
/* FUNCTION SNOMEDCT_MRG ************************************************************
 */
FUNCTION snomedct_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_snomedct_mrg		VARCHAR2(50);
BEGIN

    -- Get SNOMEDCT_US Merges
    location := '10';
    t_snomedct_mrg := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_snomedct_mrg || ' AS
	 SELECT concept_id
	 FROM classes a, source_version b
	 WHERE a.source = b.current_name
	   AND b.source = ''SNOMEDCT_US''
	   AND a.tobereleased in (''Y'',''y'')
	 GROUP BY concept_id HAVING count(distinct code)>1');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_snomedct_mrg );
    ELSE
	result_table := t_snomedct_mrg;
	t_snomedct_mrg := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_snomedct_mrg);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_snomedct_mrg);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('snomedct_mrg',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END snomedct_mrg;

/* FUNCTION SNOMEDCT_US_SEP ************************************************************
 */
FUNCTION snomedct_sep (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_snomedct_sep		VARCHAR2(50);
BEGIN
    -- Get SNOMEDCT_US splits
    location := '10';
    t_snomedct_sep := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_snomedct_sep || ' AS 
	 SELECT DISTINCT a.concept_id as concept_id_1,
	 	b.concept_id as concept_id_2 
	 FROM classes a, classes b, source_version c
	 WHERE a.concept_id < b.concept_id
	   AND a.code = b.code
	   AND a.tobereleased in (''Y'',''y'')
	   AND b.tobereleased in (''Y'',''y'')
	   AND a.source = c.current_name 
	   AND b.source = c.current_name 
	   AND c.source = ''SNOMEDCT_US'' ');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_pair(t_snomedct_sep);
    ELSE
	result_table := t_snomedct_sep;
	t_snomedct_sep := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_snomedct_sep);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_snomedct_sep);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('snomedct_sep',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END snomedct_sep;

/* FUNCTION SNM_MRG ************************************************************
 */
FUNCTION snm_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_snm_mrg		VARCHAR2(50);
    current_snm		VARCHAR2(40);
BEGIN


    -- Get current version of SNMI
    location := '0';
    current_snm := MEME_UTILITY.get_current_name('SNMI');

    -- Get MSH atoms
    location := '10';
    t_snm_mrg := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_snm_mrg || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.source = ''' || current_snm || ''' ' ||
	'  AND b.source = ''' || current_snm || ''' ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.code < b.code ' ||
	'  AND a.concept_id = b.concept_id '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_snm_mrg );
    ELSE
	result_table := t_snm_mrg;
	t_snm_mrg := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_snm_mrg);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_snm_mrg);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('snm_mrg',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END snm_mrg;

/* FUNCTION PDQ_ORPH ***********************************************************
 */
FUNCTION pdq_orph (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    t_pdq_src		VARCHAR2(50);
    t_pdq_pref		VARCHAR2(50);
    t_pdq_non_orph	VARCHAR2(50);
    t_pdq_orph		VARCHAR2(50);
    result_table	VARCHAR2(50);
    current_pdq		VARCHAR2(10);
BEGIN

    -- Get current version of PDQ
    location := '0';
    current_pdq := MEME_UTILITY.get_current_name('PDQ');

    -- Get current version PDQ preferred terms
    location := '10';
    t_pdq_pref := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec(
	'CREATE TABLE ' || t_pdq_pref || ' AS ' ||
	'SELECT DISTINCT code, concept_id ' ||
	'FROM classes a ' ||
	'WHERE source = ''' || current_pdq || ''' ' ||
	'  AND tobereleased in (''Y'',''y'') ' ||
	'  AND termgroup like ''%/PT'' ');

    -- Get current version PDQ atoms minus PTs
    location := '30';
    t_pdq_src := MEME_UTILITY.get_unique_tablename('qat_');
    location := '40';
    local_exec(
	'CREATE TABLE ' || t_pdq_src || ' AS ' ||
	'SELECT DISTINCT code, concept_id ' ||
	'FROM classes a ' ||
	'WHERE source = ''' || current_pdq || ''' ' ||
	'  AND tobereleased in (''Y'',''y'') ' ||
	'MINUS ' ||
	'SELECT code, concept_id FROM ' || t_pdq_pref
    );


    -- Find cases of non-orphans
    -- ON 3/30/2000 MSE,BAC decided that for these
    -- queries the requirement is that a rel owned by the source
    -- must exist between the pair of concepts.
    location := '50';
    t_pdq_non_orph := MEME_UTILITY.get_unique_tablename('qat_');
    location := '60';
    local_exec(
	'CREATE TABLE ' || t_pdq_non_orph || ' AS ' ||
	'SELECT x.concept_id, x.code ' ||
	'FROM ' || t_pdq_src || ' x, relationships, ' || t_pdq_pref || ' y ' ||
	'WHERE x.concept_id = concept_id_2 ' ||
	'  AND concept_id_1 = y.concept_id ' ||
	'  AND x.code = y.code AND status = ''R'' ' ||
	'  AND tobereleased IN (''y'',''Y'') ' ||
	'  AND relationship_level IN (''S'') ' ||
	'  AND source like ''PDQ%'' '
--	'  AND relationship_name not in (''XR'',''XS'',''RT?'', ' ||
--	'				 ''NT?'',''BT?'',''SY'') '
  	);


    location := '60';
    local_exec(
	'INSERT INTO ' || t_pdq_non_orph || ' ' ||
	'SELECT x.concept_id, x.code ' ||
	'FROM ' || t_pdq_src || ' x, relationships, ' || t_pdq_pref || ' y ' ||
	'WHERE x.concept_id = concept_id_1 ' ||
	'  AND concept_id_2 = y.concept_id ' ||
	'  AND x.code = y.code AND status = ''R'' ' ||
	'  AND tobereleased IN (''y'',''Y'') ' ||
	'  AND relationship_level IN (''S'') ' ||
	'  AND source like ''PDQ%'' '
--	'  AND relationship_name not in (''XR'',''XS'',''RT?'', ' ||
--	'				 ''NT?'',''BT?'',''SY'') '
  	);

    -- Get concepts without relationships
    location := '70';
    local_exec(
	'CREATE TABLE ' || t_pdq_src || '_2 AS ' ||
	'SELECT concept_id, code FROM ' || t_pdq_src || ' ' ||
	'MINUS ' ||
	'SELECT concept_id, code FROM ' || t_pdq_non_orph );

    -- Get orphan pairs
    location := '80';
    t_pdq_orph := MEME_UTILITY.get_unique_tablename('qat_');
    location := '90';
    local_exec (
	'CREATE TABLE ' || t_pdq_orph || ' AS ' ||
	'SELECT a.concept_id as concept_id_1, b.concept_id as concept_id_2 ' ||
	'FROM ' || t_pdq_src || '_2 a, ' || t_pdq_pref || ' b ' ||
	'WHERE a.code = b.code AND a.concept_id != b.concept_id');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location := '100';
	result_table := MEME_UTILITY.cluster_pair(t_pdq_orph);

    ELSE

	result_table := t_pdq_src || '_2';
    	MEME_UTILITY.drop_it('table',t_pdq_src);
	t_pdq_src := '';

    END IF;

    location := '110';
    MEME_UTILITY.drop_it('table',t_pdq_src);
    MEME_UTILITY.drop_it('table',t_pdq_src || '_2');
    MEME_UTILITY.drop_it('table',t_pdq_pref);
    MEME_UTILITY.drop_it('table',t_pdq_orph);
    MEME_UTILITY.drop_it('table',t_pdq_non_orph);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it('table',t_pdq_src);
    	MEME_UTILITY.drop_it('table',t_pdq_src || '_2');
    	MEME_UTILITY.drop_it('table',t_pdq_pref);
    	MEME_UTILITY.drop_it('table',t_pdq_orph);
    	MEME_UTILITY.drop_it('table',t_pdq_non_orph);
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('pdq_orph',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END pdq_orph;

/* FUNCTION UMD_MRG ************************************************************
 */
FUNCTION umd_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_umd_mrg		VARCHAR2(50);
    current_umd		VARCHAR2(40);
BEGIN


    -- Get current version of umd
    location := '0';
    current_umd := MEME_UTILITY.get_current_name('UMD');

    -- Get MSH atoms
    location := '10';
    t_umd_mrg := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_umd_mrg || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.source = ''' || current_umd || ''' ' ||
	'  AND b.source = ''' || current_umd || ''' ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.code < b.code ' ||
	'  AND a.concept_id = b.concept_id '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_umd_mrg );
    ELSE
	result_table := t_umd_mrg;
	t_umd_mrg := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_umd_mrg);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_umd_mrg);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('umd_mrg',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END umd_mrg;

/* FUNCTION UMD_ORPH ***********************************************************
 */
FUNCTION umd_orph (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    ct			INTEGER;
    restriction_clause	VARCHAR2(128);
    t_umd		VARCHAR2(50);
    result_table	VARCHAR2(50);
    current_umd		VARCHAR2(10);
BEGIN

    -- Get current version of UMD
    location := '0';
    current_umd := MEME_UTILITY.get_current_name('UMD');

    -- Get current version UMD preferred terms
    location := '10';
    t_umd := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec(
	'CREATE TABLE ' || t_umd || '_pref AS ' ||
	'SELECT DISTINCT code, concept_id ' ||
	'FROM classes a ' ||
	'WHERE source = ''' || current_umd || ''' ' ||
	'  AND tobereleased in (''Y'',''y'') ' ||
	'  AND (termgroup like ''%/HT'' OR ' ||
	'	termgroup like ''%/RT'') '
    );

    -- Get current version UMD atoms minus PTs
    location := '30';
    local_exec(
	'CREATE TABLE ' || t_umd || '_src AS ' ||
	'SELECT DISTINCT code, concept_id ' ||
	'FROM classes a ' ||
	'WHERE source = ''' || current_umd || ''' ' ||
	'  AND tobereleased in (''Y'',''y'') ' ||
	'MINUS ' ||
	'SELECT code, concept_id FROM ' || t_umd || '_pref'
    );


    -- Find cases of non-orphans
    location := '40';
    local_exec(
	'CREATE TABLE ' || t_umd || '_non_orph AS ' ||
	'SELECT x.concept_id, x.code ' ||
	'FROM ' || t_umd || '_src x, relationships, ' || t_umd || '_pref y ' ||
	'WHERE x.concept_id = concept_id_2 ' ||
	'  AND concept_id_1 = y.concept_id ' ||
	'  AND x.code = y.code AND status = ''R'' ' ||
	'  AND tobereleased IN (''y'',''Y'') ' ||
	'  AND relationship_level IN (''S'') ' ||
	'  AND source like ''UMD%'' '
--	'  AND relationship_name not in (''XR'',''XS'',''RT?'', ' ||
--	'				 ''NT?'',''BT?'',''SY'') '
  	);

    location := '50';
    local_exec(
	'INSERT INTO ' || t_umd || '_non_orph ' ||
	'SELECT x.concept_id, x.code ' ||
	'FROM ' || t_umd || '_src x, relationships, ' || t_umd || '_pref y ' ||
	'WHERE x.concept_id = concept_id_1 ' ||
	'  AND concept_id_2 = y.concept_id ' ||
	'  AND x.code = y.code AND status = ''R'' ' ||
	'  AND tobereleased IN (''y'',''Y'') ' ||
	'  AND relationship_level IN (''S'') ' ||
	'  AND source like ''UMD%'' '
--	'  AND relationship_name not in (''XR'',''XS'',''RT?'', ' ||
--	'				 ''NT?'',''BT?'',''SY'') '
  	);

    -- Get concepts without relationships
    location := '60';
    local_exec(
	'CREATE TABLE ' || t_umd || '_src_2 AS ' ||
	'SELECT concept_id, code FROM ' || t_umd || '_src ' ||
	'MINUS ' ||
	'SELECT concept_id, code FROM ' || t_umd || '_non_orph' );

    -- Get orphan pairs
    location := '70';
    local_exec (
	'CREATE TABLE ' || t_umd || '_orph AS ' ||
	'SELECT a.concept_id as concept_id_1, b.concept_id as concept_id_2 ' ||
	'FROM ' || t_umd || '_src_2 a, ' || t_umd || '_pref b ' ||
	'WHERE a.code = b.code AND a.concept_id != b.concept_id');

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN

	location := '100';
	result_table := MEME_UTILITY.cluster_pair(t_umd || '_orph');

    ELSE

	result_table := t_umd || '_src_2';

    END IF;

    location := '110';
    MEME_UTILITY.drop_it('table',t_umd || '_src');
    IF result_table != t_umd || '_src_2' THEN
	MEME_UTILITY.drop_it('table',t_umd || '_src_2');
    END IF;
    MEME_UTILITY.drop_it('table',t_umd || '_pref');
    MEME_UTILITY.drop_it('table',t_umd || '_orph');
    MEME_UTILITY.drop_it('table',t_umd || '_non_orph');

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it('table',t_umd || '_src');
    	MEME_UTILITY.drop_it('table',t_umd || '_src_2');
    	MEME_UTILITY.drop_it('table',t_umd || '_pref');
    	MEME_UTILITY.drop_it('table',t_umd || '_orph');
    	MEME_UTILITY.drop_it('table',t_umd || '_non_orph');
	MEME_UTILITY.drop_it('table', result_table);
	meme_integrity_proc_error('umd_orph',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END umd_orph;

/* FUNCTION HCPCS_MRG **********************************************************
 */
FUNCTION hcpcs_mrg (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_hcpcs_mrg		VARCHAR2(50);
    current_hcpcs	VARCHAR2(40);
    current_cpt		VARCHAR2(40);
BEGIN


    -- Get current version of HCPCS, CPT
    location := '0';
    current_hcpcs := MEME_UTILITY.get_current_name('HCPCS');
    location := '10';
    current_cpt := MEME_UTILITY.get_current_name('CPT');

    -- Get MSH atoms
    location := '20';
    t_hcpcs_mrg := MEME_UTILITY.get_unique_tablename('qat_');
    location := '30';
    local_exec (
	'CREATE TABLE ' || t_hcpcs_mrg || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.termgroup IN (''' || current_hcpcs || '/PT'', ''' ||
				   current_hcpcs || '/MP'', ''' ||
				   current_cpt || '/PT'') ' ||
	'  AND b.termgroup IN (''' || current_hcpcs || '/PT'', ''' ||
				   current_hcpcs || '/MP'', ''' ||
				   current_cpt || '/PT'') ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.code < b.code ' ||
	'  AND a.concept_id = b.concept_id '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '40';
	result_table := MEME_UTILITY.cluster_single(t_hcpcs_mrg );
    ELSE
	result_table := t_hcpcs_mrg;
	t_hcpcs_mrg := '';
    END IF;

    location := '50';
    MEME_UTILITY.drop_it ('table',t_hcpcs_mrg);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_hcpcs_mrg);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('hcpcs_mrg',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END hcpcs_mrg;

/* FUNCTION CPT_SPLIT **********************************************************
 */
FUNCTION cpt_split (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_cpt_split		VARCHAR2(50);
    current_hcpcs	VARCHAR2(40);
    current_cpt		VARCHAR2(40);
BEGIN


    -- Get current version of HCPCS, CPT
    location := '0';
    current_hcpcs := MEME_UTILITY.get_current_name('HCPCS');
    location := '10';
    current_cpt := MEME_UTILITY.get_current_name('CPT');

    -- Get same-code split apart
    location := '20';
    t_cpt_split := MEME_UTILITY.get_unique_tablename('qat_');
    location := '30';
    local_exec (
	'CREATE TABLE ' || t_cpt_split || ' AS ' ||
	'SELECT DISTINCT a.concept_id as concept_id_1, ' ||
	'		 b.concept_id as concept_id_2 ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.termgroup like ''' || current_cpt || '%'' ' ||
	'  AND b.termgroup like ''' || current_hcpcs || '%'' ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.code = b.code ' ||
	'  AND a.concept_id != b.concept_id '
   );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '40';
	result_table := MEME_UTILITY.cluster_pair(t_cpt_split );
    ELSE
	result_table := t_cpt_split;
	t_cpt_split := '';
    END IF;

    MEME_UTILITY.drop_it('table',t_cpt_split);

    return result_table;
EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_cpt_split);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('cpt_split',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END cpt_split;

/* FUNCTION CPT_ORPH ***********************************************************
 */
FUNCTION cpt_orph (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_cpt_orph		VARCHAR2(50);
    current_cpt		VARCHAR2(40);
BEGIN


    -- Get current version of HCPCS, CPT
    location := '0';
    current_cpt := MEME_UTILITY.get_current_name('CPT');

    -- Get CPT%/AB separated from CPT%/PT
    location := '10';
    t_cpt_orph := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_cpt_orph || ' AS ' ||
	'SELECT DISTINCT a.concept_id as concept_id_1, ' ||
	'		 b.concept_id as concept_id_2 ' ||
	'FROM classes a, classes b ' ||
	'WHERE a.termgroup = ''' || current_cpt || '/PT'' ' ||
	'  AND b.termgroup = ''' || current_cpt || '/AB'' ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.code = b.code ' ||
	'  AND a.concept_id != b.concept_id '
   );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '40';
	result_table := MEME_UTILITY.cluster_pair(t_cpt_orph );
    ELSE
	result_table := t_cpt_orph;
	t_cpt_orph := '';
    END IF;

    MEME_UTILITY.drop_it('table',t_cpt_orph);

    return result_table;
EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_cpt_orph);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('cpt_orph',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END cpt_orph;

/* FUNCTION LNC_SEP ************************************************************
 */
FUNCTION lnc_sep (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_lnc_sep		VARCHAR2(50);
    current_lnc		VARCHAR2(40);

BEGIN

    -- Get previous/current names for LNC
    location := '0';
    current_lnc := MEME_UTILITY.get_current_name('LNC');

    -- Get bad LNC splits
    location := '10';
    t_lnc_sep := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_lnc_sep || ' AS ' ||
	'SELECT DISTINCT a.concept_id as concept_id_1, ' ||
	'	b.concept_id as concept_id_2 ' ||
	'FROM classes a, classes b '  ||
	'WHERE a.concept_id < b.concept_id ' ||
	'  AND a.code = b.code ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND (a.termgroup = ''' || current_lnc || '/LN'' OR ' ||
	'	a.termgroup = ''' || current_lnc || '/LO'') ' ||
	'  AND (b.termgroup = ''' || current_lnc || '/LN'' OR ' ||
	'	b.termgroup = ''' || current_lnc || '/LO'') '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_pair(t_lnc_sep);
    ELSE
	result_table := t_lnc_sep;
	t_lnc_sep := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_lnc_sep);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_lnc_sep);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('lnc_sep',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END lnc_sep;

/* FUNCTION STYISA *************************************************************
 * Get concepts with ancestor-decendant STY pairs
 * get relationships from srstre2
 */
FUNCTION styisa (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_styisa		VARCHAR2(50);

BEGIN

    location := '0';
    t_styisa := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	   'CREATE TABLE ' || t_styisa || '_pre AS
	    SELECT /*+ INDEX(a1, x_attr_an) INDEX(a2, x_attr_an) */  
             DISTINCT a1.concept_id,
			 a1.attribute_value as ancestor_sty,
			 a2.attribute_value as decendant_sty
	    FROM attributes a1, attributes a2, srstre2 s
	    WHERE a1.concept_id = a2.concept_id
	      AND a2.attribute_value = s.sty1
	      AND a1.attribute_value = s.sty2
	      AND rel = ''isa''
	      AND a1.tobereleased in (''Y'',''y'')
	      AND a2.tobereleased in (''Y'',''y'')
	      AND a1.attribute_name = ''SEMANTIC_TYPE''
	      AND a2.attribute_name = ''SEMANTIC_TYPE'' ');

    location := '15';
    local_exec (
	   'CREATE TABLE ' || t_styisa || ' AS
	    SELECT * FROM ' || t_styisa || '_pre a
	    WHERE NOT EXISTS
	      (SELECT 1 FROM concept_status b
	       WHERE status = ''U''
	         AND a.concept_id=b.concept_id)'
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_styisa);
    ELSE
	result_table := t_styisa;
    END IF;

    location := '40';

    IF result_table != t_styisa THEN
    	MEME_UTILITY.drop_it ('table',t_styisa);
    END IF;
    MEME_UTILITY.drop_it ('table',t_styisa);
    MEME_UTILITY.drop_it ('table',t_styisa || '_pre');

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it ('table',t_styisa);

    	MEME_UTILITY.drop_it ('table',t_styisa || '_pre');
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('styisa',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END styisa;

/* FUNCTION ICDPROC ************************************************************
 * Get ICD atoms with 2 digit codes that
 * do not have procedure STYs
 */
FUNCTION icdproc (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_icdproc		VARCHAR2(50);
    current_icd		VARCHAR2(40);

BEGIN

    -- Get previous/current names for ICD
    location := '0';
    current_icd := MEME_UTILITY.get_current_name('ICD9');

    location := '10';
    t_icdproc := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_icdproc || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM classes a, attributes b ' ||
	'WHERE a.source = ''' || current_icd || ''' ' ||
	'  AND translate(code,''0123456789X'',''XXXXXXXXXY'') = ''XX'' ' ||
	'  AND a.concept_id = b.concept_id ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND attribute_name = ''SEMANTIC_TYPE'' ' ||
	'  AND attribute_value NOT IN ' ||
	' ( SELECT sty1 FROM srstre2 WHERE rel = ''isa'' ' ||
	'      AND sty2 = ''Health Care Activity'' ) ' ||
	'  AND attribute_value != ''Health Care Activity'' '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_icdproc);
    ELSE
	result_table := t_icdproc;
	t_icdproc := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_icdproc);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it ('table',t_icdproc);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('icdproc',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END icdproc;

/* FUNCTION STYDRUG ************************************************************
 * Find where 'Clinical Drug' co-occurs with
 * something other than 'Medical Device'
 */
FUNCTION stydrug (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_stydrug		VARCHAR2(50);

BEGIN

    location := '10';
    t_stydrug := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_stydrug || ' AS ' ||
	'SELECT DISTINCT a.concept_id ' ||
	'FROM attributes a, attributes b ' ||
	'WHERE a.concept_id = b.concept_id ' ||
	'  AND a.tobereleased in (''Y'',''y'') ' ||
	'  AND b.tobereleased in (''Y'',''y'') ' ||
	'  AND a.attribute_name = ''SEMANTIC_TYPE'' ' ||
	'  AND b.attribute_name = ''SEMANTIC_TYPE'' ' ||
	'  AND a.attribute_value = ''Clinical Drug'' ' ||
	'  AND b.attribute_value != ''Medical Device'' ' ||
	'  AND a.attribute_value != b.attribute_value '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_stydrug);
    ELSE
	result_table := t_stydrug;
	t_stydrug := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_stydrug);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it ('table',t_stydrug);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('stydrug',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END stydrug;

/* FUNCTION MTHDT_NOMM *********************************************************
 * Find MTH/DT atoms without MTH/MM atoms present
 */
FUNCTION mthdt_nomm (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0
)
RETURN VARCHAR2
IS
    result_table	VARCHAR2(50);
    t_mthdt_nomm		VARCHAR2(50);

BEGIN

    location := '10';
    t_mthdt_nomm := MEME_UTILITY.get_unique_tablename('qat_');
    location := '20';
    local_exec (
	'CREATE TABLE ' || t_mthdt_nomm || ' AS ' ||
	'SELECT DISTINCT concept_id ' ||
	'FROM classes ' ||
	'WHERE source = ''MTH'' AND termgroup = ''MTH/DT'' ' ||
	'  AND tobereleased in (''Y'',''y'') ' ||
	'MINUS ' ||
	'SELECT concept_id ' ||
	'FROM classes ' ||
	'WHERE source = ''MTH'' AND termgroup = ''MTH/MM'' ' ||
	'  AND tobereleased in (''Y'',''y'') '
    );

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location := '30';
	result_table := MEME_UTILITY.cluster_single(t_mthdt_nomm);
    ELSE
	result_table := t_mthdt_nomm;
	t_mthdt_nomm := '';
    END IF;

    location := '40';
    MEME_UTILITY.drop_it ('table',t_mthdt_nomm);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
    	MEME_UTILITY.drop_it ('table',t_mthdt_nomm);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('mthdt_nomm',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END mthdt_nomm;

/* FUNCTION CUI_SPLITS *********************************************************
 * This function returns set of splitted CUI.
 */
FUNCTION cui_splits (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   t_split_cui	   VARCHAR2(50);
   cluster_table   VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   t_split_cui := MEME_UTILITY.get_unique_tablename('qat_');

   location := '20';
   /* Retrieves split concepts */
   local_exec('CREATE TABLE '||t_split_cui||' AS
      SELECT a.concept_id AS concept_id_1,
	     b.concept_id AS concept_id_2,
	     a.last_release_cui AS cluster_id
      FROM classes a, classes b
      WHERE a.concept_id != b.concept_id
	AND a.last_release_cui = b.last_release_cui
	AND a.last_release_cui IS NOT NULL');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

      local_exec('CREATE TABLE '||cluster_table||' AS'||
	 ' SELECT concept_id_1 AS concept_id, cluster_id'||
	 ' FROM '||t_split_cui||
	 ' UNION SELECT concept_id_2, cluster_id'||
	 ' FROM '||t_split_cui);

      result_table := MEME_UTILITY.recluster(cluster_table);
   ELSE
      location := '40';
      result_table := cluster_table;
   END IF;

   location := '50';
   MEME_UTILITY.drop_it('table',t_split_cui);
   MEME_UTILITY.drop_it('table',cluster_table);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_split_cui);
      MEME_UTILITY.drop_it('table',cluster_table);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('cui_splits',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END cui_splits;

/* FUNCTION MTH_ONLY ***********************************************************
 * This function returns set of concepts whose only
 * releasable atoms have source=MTH
 */
FUNCTION mth_only (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   t_mth_only	   VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   t_mth_only := MEME_UTILITY.get_unique_tablename('qat_');

   location := '20';
   /* Get concepts containing only MTH atoms */
   local_exec('CREATE TABLE '||t_mth_only||' AS
      SELECT concept_id FROM classes
      WHERE source = ''MTH'' AND tobereleased IN (''Y'',''y'')
      MINUS
      SELECT concept_id FROM classes
      WHERE source != ''MTH'' AND tobereleased IN (''Y'',''y'')');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      result_table := MEME_UTILITY.cluster_single(t_mth_only);
   ELSE
      location := '40';
      result_table := t_mth_only;
      t_mth_only := '';
   END IF;

   location := '50';
   MEME_UTILITY.drop_it ('table',t_mth_only);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_mth_only);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('mth_only',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END mth_only;

/* FUNCTION RESCUE_LT **********************************************************
 * This function returns set of rescued lexically tagged (LT) atoms.
 */
FUNCTION rescue_lt (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   t_lts	   VARCHAR2(50);
   t_lts_rescue    VARCHAR2(50);
   result_table      VARCHAR2(50);

BEGIN

   t_lts := MEME_UTILITY.get_unique_tablename('qat_');
   t_lts_rescue := MEME_UTILITY.get_unique_tablename('qat_');

   location := '10';
   /* Get unreleasable LT's. */
   local_exec('CREATE TABLE '||t_lts||' AS '||
      'SELECT b.concept_id, b.atom_id, lui '||
      'FROM attributes a, classes b '||
      'WHERE a.atom_id = b.atom_id '||
      'AND attribute_name = ''LEXICAL_TAG'' '||
      'AND b.tobereleased IN (''N'',''n'')');

   location := '15';
   MEME_SYSTEM.ANALYZE(t_lts);

   location := '20';
   /* Determine and retrieve LT's to be rescue. */
   local_exec('CREATE TABLE '||t_lts_rescue||' AS '||
      'SELECT a.concept_id, a.atom_id AS old_atom_id, a.lui, '||
      'MAX(b.atom_id) AS new_atom_id '||
      'FROM '||t_lts||' a, classes b '||
      'WHERE a.lui = b.lui '||
      'AND a.atom_id != b.atom_id '||
      'AND a.concept_id = b.concept_id '||
      'AND tobereleased IN (''Y'',''y'') '||
      'GROUP BY a.concept_id, a.atom_id, a.lui');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      result_table := MEME_UTILITY.cluster_single(t_lts_rescue);
   ELSE
      result_table := t_lts_rescue;
      t_lts_rescue := '';
   END IF;

   MEME_UTILITY.drop_it('table',t_lts);
   MEME_UTILITY.drop_it('table',t_lts_rescue);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_lts);
      MEME_UTILITY.drop_it('table',t_lts_rescue);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('rescue_lt',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END rescue_lt;

/* FUNCTION RESCUE_RELS_HELP ***************************************************
 * This function will served as a common process for function rescue_orphan
 * and rescue_pair.  It will mapped all the necessary information needed by the
 * two rescue function.
 */
FUNCTION rescue_rels_help (
   t_tbrn_rels	     IN VARCHAR2,
   t_new_orphans     IN VARCHAR2
)
RETURN INTEGER
IS
   t_tbrn_atoms      VARCHAR2(50);
   t_other_rels      VARCHAR2(50);
   t_unreleasable    VARCHAR2(50);

BEGIN

   location := '10';
   /* Retrieve unreleasable atoms. */
   t_tbrn_atoms := MEME_UTILITY.get_unique_tablename('qat_');
   local_exec('CREATE TABLE '||t_tbrn_atoms||' AS '||
      'SELECT atom_id FROM classes '||
      'WHERE tobereleased IN (''n'',''N'')');

   location := '20';
   /* Retrieve unreleasable source level relationship atoms. */
   local_exec('CREATE TABLE '||t_tbrn_rels||'_pre AS '||
      'SELECT'||
      ' atom_id_1, atom_id_2, concept_id_1, concept_id_2, relationship_id '||
      'FROM relationships '||
      'WHERE atom_id_1 IN (SELECT * FROM '||t_tbrn_atoms||') '||
      'AND concept_id_1 != concept_id_2 '||
      'AND relationship_level = ''S'' '||
      'UNION SELECT'||
      ' atom_id_1, atom_id_2, concept_id_1, concept_id_2, relationship_id '||
      'FROM relationships '||
      'WHERE atom_id_2 IN (SELECT * FROM '||t_tbrn_atoms||') '||
      'AND concept_id_1 != concept_id_2 '||
      'AND relationship_level = ''S''');

   location := '30';
   /* Retrieve releasable relationships ("other rels"). */
   t_other_rels := MEME_UTILITY.get_unique_tablename('qat_');
   local_exec('CREATE TABLE '||t_other_rels||' AS '||
      'SELECT DISTINCT concept_id_1, concept_id_2 '||
      'FROM relationships WHERE relationship_id IN '||
      '(SELECT relationship_id FROM relationships'||
      ' MINUS SELECT relationship_id FROM '||t_tbrn_rels||'_pre)');

   location := '40';
   /* Do not rescue tbrn relationships if there are releasable
      Rels still around between the same pair of concepts. */

   local_exec('CREATE table '||t_tbrn_rels||' AS '||
      'SELECT concept_id_1, concept_id_2 '||
      'FROM '||t_tbrn_rels||'_pre '||
      'MINUS SELECT concept_id_1, concept_id_2 FROM '||t_other_rels||' '||
      'MINUS SELECT concept_id_2, concept_id_1 FROM '||t_other_rels);

   location := '50';
   /* Find unreleasable concepts */
   t_unreleasable := MEME_UTILITY.get_unique_tablename('qat_');
   local_exec('CREATE TABLE '||t_unreleasable||' AS '||
      'SELECT concept_id FROM concept_status a '||
      'MINUS '||
      'SELECT concept_id FROM classes b'||
      ' WHERE tobereleased IN (''Y'',''y'')');

   location := '60';
   /* Weed out unreleasable concepts from candidate rels */
   local_exec('DELETE FROM '||t_tbrn_rels||
      ' WHERE concept_id_1 IN'||
      ' (SELECT concept_id FROM '||t_unreleasable||')');
   local_exec('DELETE FROM '||t_tbrn_rels||
      ' WHERE concept_id_2 IN'||
      ' (SELECT concept_id FROM '||t_unreleasable||')');

   location := '70';
   /* Get concept_ids touching tbr='n' rels */
   local_exec('CREATE TABLE '||t_new_orphans||' AS'||
      ' SELECT concept_id_1 AS concept_id FROM '||t_tbrn_rels||
      ' UNION'||
      ' SELECT concept_id_2 AS concept_id FROM '||t_tbrn_rels);

   location := '80';
   /* Remove concept_ids touching other rels */
   local_exec('DELETE FROM '||t_new_orphans||
      ' WHERE concept_id IN (SELECT concept_id_1 FROM '||t_other_rels||')');
   local_exec('DELETE FROM '||t_new_orphans||
      ' WHERE concept_id IN (SELECT concept_id_2 FROM '||t_other_rels||')');

   location := '90';
   /* Weed out unreleasable concepts from orphans */
   local_exec('DELETE FROM '||t_new_orphans||
      ' WHERE concept_id IN (SELECT concept_id FROM '||t_unreleasable||')');

   MEME_UTILITY.drop_it('table',t_tbrn_atoms);
   MEME_UTILITY.drop_it('table',t_tbrn_rels||'_pre');
   MEME_UTILITY.drop_it('table',t_other_rels);
   MEME_UTILITY.drop_it('table',t_unreleasable);

   RETURN 0;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_tbrn_atoms);
      MEME_UTILITY.drop_it('table',t_tbrn_rels||'_pre');
      MEME_UTILITY.drop_it('table',t_other_rels);
      MEME_UTILITY.drop_it('table',t_unreleasable);
      meme_integrity_proc_error('rescue_rels_help',location,1,SQLERRM);
      RETURN -1;
END rescue_rels_help;

/* FUNCTION RESCUE_ORPHAN ******************************************************
 * This function returns set of rescued orphans.
 * Orphans are those obsolete relationships whose absence creates orphans.
 */
FUNCTION rescue_orphan (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   t_tbrn_rels	     VARCHAR2(50);
   t_new_orphans     VARCHAR2(50);
   t_rescue_orphans  VARCHAR2(50);
   result_table      VARCHAR2(50);
   retval	     INTEGER;
   rescue_orphan_exc EXCEPTION;

BEGIN

   location := '10';
   t_tbrn_rels	 := MEME_UTILITY.get_unique_tablename('qat_');
   t_new_orphans := MEME_UTILITY.get_unique_tablename('qat_');

   location := '20';
   retval := rescue_rels_help(t_tbrn_rels, t_new_orphans);
   IF retval < 0 THEN
      RAISE rescue_orphan_exc;
   END IF;

   location := '30';
   /* Rescue orphans */
   t_rescue_orphans := MEME_UTILITY.get_unique_tablename('qat_');
   local_exec('CREATE TABLE '||t_rescue_orphans||' AS'||
      ' SELECT * FROM '||t_tbrn_rels||
      ' WHERE concept_id_1 IN (SELECT concept_id FROM '||t_new_orphans||')'||
      ' UNION'||
      ' SELECT * FROM '||t_tbrn_rels||
      ' WHERE concept_id_2 IN (SELECT concept_id FROM '||t_new_orphans||')');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      result_table := MEME_UTILITY.cluster_pair(t_rescue_orphans);
   ELSE
      result_table := t_rescue_orphans;
      t_rescue_orphans := '';
   END IF;

   MEME_UTILITY.drop_it('table',t_tbrn_rels);
   MEME_UTILITY.drop_it('table',t_new_orphans);
   MEME_UTILITY.drop_it('table',t_rescue_orphans);

   RETURN result_table;

EXCEPTION
   WHEN rescue_orphan_exc THEN
      MEME_UTILITY.drop_it('table',t_tbrn_rels);
      MEME_UTILITY.drop_it('table',t_new_orphans);
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_tbrn_rels);
      MEME_UTILITY.drop_it('table',t_new_orphans);
      MEME_UTILITY.drop_it('table',t_rescue_orphans);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('rescue_orphan',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END rescue_orphan;

/* FUNCTION RESCUE_PAIR ********************************************************
 * This function returns set of rescued pairs of concept.
 * Rescued pair of concept are those obsolete relationships that are the only
 * ones between a pair of concepts.
 */
FUNCTION rescue_pair (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   t_tbrn_rels	     VARCHAR2(50);
   t_new_orphans     VARCHAR2(50);
   t_rescue_pairs    VARCHAR2(50);
   result_table      VARCHAR2(50);
   retval	     INTEGER;
   rescue_pair_exc   EXCEPTION;

BEGIN

   location := '10';
   t_tbrn_rels	 := MEME_UTILITY.get_unique_tablename('qat_');
   t_new_orphans := MEME_UTILITY.get_unique_tablename('qat_');

   location := '20';
   retval := rescue_rels_help(t_tbrn_rels, t_new_orphans);
   IF retval < 0 THEN
      RAISE rescue_pair_exc;
   END IF;

   location := '30';
   /* Rescue pairs */
   t_rescue_pairs := MEME_UTILITY.get_unique_tablename('qat_');
   local_exec('CREATE TABLE '||t_rescue_pairs||' AS '||
      'SELECT * FROM '||t_tbrn_rels);
   local_exec('DELETE FROM '||t_rescue_pairs||
      ' WHERE concept_id_1 IN (SELECT concept_id FROM '||t_new_orphans||')');
   local_exec('DELETE FROM '||t_rescue_pairs||
      ' WHERE concept_id_2 IN (SELECT concept_id FROM '||t_new_orphans||')');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      result_table := MEME_UTILITY.cluster_pair(t_rescue_pairs);
   ELSE
      result_table := t_rescue_pairs;
      t_rescue_pairs := '';
   END IF;

   location := '40';
   MEME_UTILITY.drop_it('table',t_tbrn_rels);
   MEME_UTILITY.drop_it('table',t_new_orphans);
   MEME_UTILITY.drop_it('table',t_rescue_pairs);

   RETURN result_table;

EXCEPTION
   WHEN rescue_pair_exc THEN
      MEME_UTILITY.drop_it('table',t_tbrn_rels);
      MEME_UTILITY.drop_it('table',t_new_orphans);
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_tbrn_rels);
      MEME_UTILITY.drop_it('table',t_new_orphans);
      MEME_UTILITY.drop_it('table',t_rescue_pairs);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('rescue_pair',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END rescue_pair;

/* FUNCTION DELETED_CUI ********************************************************
 * This functions produces a list of concept ids which contain CUIs that will
 * appear in the next DELETED.CUI file.  These CUIs are considered deleted
 * because all of their atoms are obsolete.
 */
FUNCTION deleted_cui (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   deleted_cui     VARCHAR2(50);
   cluster_table   VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   /* Get cuis excluding assigned ones */

   deleted_cui := MEME_UTILITY.get_unique_tablename('qat_');
   MEME_UTILITY.drop_it ('table',deleted_cui);

   -- Get assigned CUIs minus cuis in concept status and cui_history
   local_exec(
     'CREATE TABLE ' || deleted_cui || ' AS
      SELECT last_release_cui AS cui, concept_id
      FROM classes WHERE last_release_cui IS NOT NULL
	AND tobereleased in (''N'',''n'')
      MINUS 
      SELECT cui, concept_id FROM concept_status
      WHERE tobereleased IN (''Y'',''y'') '); 

   location := '20';
   -- Remove from consideration any cuis that
   -- are assigned to other concepts or are tracked
   -- already in cui_history
   local_exec(
      'DELETE FROM ' || deleted_cui || ' WHERE cui IN
         (SELECT cui FROM concept_status 
          WHERE tobereleased IN (''Y'',''y'') ) ');

   local_exec(
      'DELETE FROM ' || deleted_cui || ' WHERE concept_id IN
         (SELECT concept_id FROM concept_status 
          WHERE tobereleased IN (''Y'',''y'') ) ');

   location := '30';
   -- Remove any splits from consideration
   local_exec('DELETE FROM ' || deleted_cui || ' WHERE cui IN
      (SELECT a.last_release_cui FROM classes a, classes b
        WHERE a.concept_id != b.concept_id
          AND a.last_release_cui IS NOT NULL
          AND b.last_release_cui IS NOT NULL
          AND a.last_release_cui = b.last_release_cui)');

   location := '30';
   -- Remove any merges from consideration 
   local_exec('DELETE FROM ' || deleted_cui || ' WHERE cui IN
      (SELECT a.last_release_cui FROM classes a, classes b
        WHERE a.concept_id = b.concept_id
          AND a.last_release_cui IS NOT NULL
          AND b.last_release_cui IS NOT NULL
          AND a.last_release_cui != b.last_release_cui)');

   location := '35'; 
   -- Remove any bequeathed concepts from consideration
   local_exec('DELETE FROM ' || deleted_cui || ' WHERE concept_id IN
      (SELECT concept_id_1 FROM relationships 
       WHERE relationship_name in (''BBT'',''BNT'',''BRT'')
       UNION
       SELECT concept_id_2 FROM relationships 
       WHERE relationship_name in (''BBT'',''BNT'',''BRT'')) ');

   location := '40';
   -- Get distinct concept_ids 
   cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

   local_exec(
     'CREATE TABLE ' || cluster_table || ' AS
      SELECT DISTINCT concept_id FROM ' || deleted_cui);

   location := '50';
   -- Cluster the results 

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '50.1';
      result_table := MEME_UTILITY.cluster_single(cluster_table);
   ELSE
      location := '50.2';
      result_table := cluster_table;
      cluster_table := '';
   END IF;

   location := '60';
   MEME_UTILITY.drop_it ('table',deleted_cui);
   MEME_UTILITY.drop_it ('table',cluster_table);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',deleted_cui);
      MEME_UTILITY.drop_it('table',cluster_table);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('deleted_cui',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END deleted_cui;

/* FUNCTION DELETED_CUI_UWDA ************************************************
 * This functions produces a list of concept ids which contain CUIs that will
 * appear in the next DELETED.CUI file.  These CUIs are considered deleted
 * because all of their atoms are obsolete.
 */
FUNCTION deleted_cui_uwda (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   deleted_cui_uwda     VARCHAR2(50);
   result_table    VARCHAR2(50);
BEGIN

   location := '10';
   /* Get cuis excluding assigned ones */

   deleted_cui_uwda := deleted_cui(MEME_CONSTANTS.CLUSTER_NO,work_id);

   location := '20';
   /* Remove UWDA concepts */
   local_exec('DELETE FROM '||deleted_cui_uwda||' WHERE concept_id IN
      (SELECT concept_id FROM classes 
       WHERE source like ''SNOMEDCT_US%'') ');

   location := '30';
   /* Cluster the results */

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '50.1';
      result_table := MEME_UTILITY.cluster_single(deleted_cui_uwda);
   ELSE
      location := '50.2';
      result_table := deleted_cui_uwda;
   END IF;

   location := '60';
   MEME_UTILITY.drop_it ('table',deleted_cui_uwda);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',deleted_cui_uwda);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('deleted_cui',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END deleted_cui_uwda;

/* FUNCTION DELETED_CUI_SPLIT **************************************************
 * This function produces a list of concept ids which contain CUIs that will
 * appear in the next DELETED.CUI file.  These CUIs are considered deleted
 * because all atoms assigned these CUI in the previous release have been split
 * out of the original concept and merged into concepts containing higher
 * ranking atoms.  The results are clustered by CUI.
 */
FUNCTION deleted_cui_split (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   split_cuis      VARCHAR2(50);
   cluster_table   VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   /* Get all CUI splits */

   split_cuis := MEME_UTILITY.get_unique_tablename('qat_');
   MEME_UTILITY.drop_it ('table',split_cuis);

   local_exec('CREATE TABLE '||split_cuis||' AS
      SELECT DISTINCT a.concept_id AS concept_id_1,
                      b.concept_id AS concept_id_2,
                      a.last_release_cui AS cluster_id
      FROM classes a, classes b
      WHERE a.concept_id != b.concept_id
        AND a.last_release_cui IS NOT NULL
        AND b.last_release_cui IS NOT NULL
        AND a.last_release_cui = b.last_release_cui');

   location := '20';
   /* Remove splits that are assigned */

   local_exec('DELETE FROM '||split_cuis||' WHERE cluster_id IN
      (SELECT cui FROM concept_status)');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      /* Get clusters and cluster the results */

      cluster_table := MEME_UTILITY.get_unique_tablename('qat_');
      MEME_UTILITY.drop_it ('table',cluster_table);

      local_exec('CREATE TABLE '||cluster_table||' AS
         SELECT concept_id_1 AS concept_id, cluster_id FROM '||split_cuis||'
         UNION
         SELECT concept_id_2, cluster_id FROM '||split_cuis);

      location := '40';
      result_table := MEME_UTILITY.recluster(cluster_table);
   ELSE
      location := '50';
      result_table := cluster_table;
   END IF;

   location := '60';
   MEME_UTILITY.drop_it ('table',split_cuis);
   MEME_UTILITY.drop_it ('table',cluster_table);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',split_cuis);
      MEME_UTILITY.drop_it('table',cluster_table);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('deleted_cui_split',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END deleted_cui_split;

/* FUNCTION MERGED_CUI *********************************************************
 * This functions produces a list of concept ids which contain CUIs that will
 * appear in the next MERGED.CUI file.
 */
FUNCTION merged_cui (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   merged_cuis     VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   /* Get all merged CUI */

   merged_cuis := MEME_UTILITY.get_unique_tablename('qat_');
   MEME_UTILITY.drop_it ('table',merged_cuis);

   local_exec('CREATE TABLE '||merged_cuis||' AS
      SELECT DISTINCT a.last_release_cui AS old_cui,
                      b.last_release_cui AS new_cui,
                      a.concept_id AS concept_id
      FROM classes a, classes b, concept_status c
      WHERE a.concept_id = b.concept_id
        AND a.last_release_cui IS NOT NULL
        AND b.last_release_cui IS NOT NULL
        AND a.last_release_cui != b.last_release_cui
        AND b.concept_id = c.concept_id
        AND b.last_release_cui = c.cui');

   location := '20';
   MEME_SYSTEM.analyze(merged_cuis);

   location := '30';
   /* Live cuis cannot be merged cuis */

   local_exec('DELETE FROM '||merged_cuis||' WHERE old_cui IN
      (SELECT cui FROM concept_status)');

   location := '40';
   /* Split cuis cannot be merged cuis */

   local_exec('DELETE FROM '||merged_cuis||' WHERE old_cui IN
      (SELECT old_cui FROM '||merged_cuis||' GROUP BY old_cui
       HAVING COUNT(DISTINCT new_cui) > 1)');

   location := '50';
   /* Cluster the results */

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '50.1';
      result_table := MEME_UTILITY.cluster_single(merged_cuis);
   ELSE
      location := '50.2';
      result_table := merged_cuis;
      merged_cuis := '';
   END IF;

   location := '60';
   MEME_UTILITY.drop_it('table',merged_cuis);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',merged_cuis);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('merged_cui',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END merged_cui;

/* FUNCTION MSH_MUI_MERGE ******************************************************
 * This functions produces a list of concepts containing atoms with different
 * MSH MUI values.
 */
FUNCTION msh_mui_merge (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   current_msh     VARCHAR2(50);
   mui_merges      VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   /* Get source */
   current_msh := MEME_UTILITY.get_current_name('MSH');

   location := '20';
   /* Get mui merges */

   mui_merges := MEME_UTILITY.get_unique_tablename('qat_');
   MEME_UTILITY.drop_it ('table',mui_merges);

   location := '21';

   local_exec('CREATE TABLE '||mui_merges||' AS'||
      ' SELECT DISTINCT a.concept_id'||
      ' FROM classes a,  classes b'||
      ' WHERE a.concept_id = b.concept_id'||
      ' AND a.source_cui != b.source_cui'||
      ' AND a.source = '||''''||current_msh||''''||
      ' AND a.source = b.source');

   location := '30';
   /* Cluster the results */

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30.1';
      result_table := MEME_UTILITY.cluster_single(mui_merges);
   ELSE
      location := '30.2';
      result_table := mui_merges;
      mui_merges := '';
   END IF;

   MEME_UTILITY.drop_it('table',mui_merges);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',mui_merges);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('msh_mui_merge',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END msh_mui_merge;

/* FUNCTION MSH_MUI_SPLIT ******************************************************
 * This functions produces a list of concepts across which a MSH MUI is split.
 * The results are clustered by MUI values.
 */
FUNCTION msh_mui_split (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   current_msh     VARCHAR2(50);
   mui_splits      VARCHAR2(50);
   cluster_table   VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   current_msh := MEME_UTILITY.get_current_name('MSH');

   location := '20';
   /* Get mui splits */

   mui_splits := MEME_UTILITY.get_unique_tablename('qat_');
   MEME_UTILITY.drop_it ('table',mui_splits);

   location := '30';

   local_exec(
      'CREATE TABLE '||mui_splits||' AS 
	SELECT DISTINCT a.concept_id AS concept_id_1,
	       b.concept_id AS concept_id_2,
      	       a.source_cui AS cluster_id
	FROM classes a, classes b 
        WHERE a.concept_id != b.concept_id
          AND a.source_cui = b.source_cui
          AND a.source = ''' || current_msh || '''
          AND a.source = b.source');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      /* Get clusters and cluster the results */

      cluster_table := MEME_UTILITY.get_unique_tablename('qat_');
      MEME_UTILITY.drop_it ('table',cluster_table);

      local_exec('CREATE TABLE '||cluster_table||' AS
         SELECT concept_id_1 AS concept_id, cluster_id FROM '||mui_splits||'
         UNION
         SELECT concept_id_2, cluster_id FROM '||mui_splits);

      location := '40';
      result_table := MEME_UTILITY.recluster(cluster_table);
   ELSE
      location := '50';
      result_table := cluster_table;
   END IF;

   location := '60';
   MEME_UTILITY.drop_it ('table',mui_splits);
   MEME_UTILITY.drop_it ('table',cluster_table);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',mui_splits);
      MEME_UTILITY.drop_it('table',cluster_table);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('msh_mui_split',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END msh_mui_split;

/* FUNCTION SUSPECT_CUI ********************************************************
 */
FUNCTION suspect_cui (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   t_sus_cui	   VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   t_sus_cui := MEME_UTILITY.get_unique_tablename('qat_');

   location := '20';
   local_exec('CREATE TABLE '||t_sus_cui||' AS
      SELECT concept_id FROM classes a
      WHERE last_release_cui IS NOT NULL AND tobereleased IN (''y'',''Y'')
      GROUP BY concept_id HAVING COUNT(DISTINCT last_release_cui) > 1
      UNION
      SELECT a.concept_id FROM classes a, classes b
      WHERE a.last_release_cui = b.last_release_cui
      AND a.last_release_cui IS NOT NULL
      AND a.tobereleased IN (''y'',''Y'')
      AND b.tobereleased IN (''y'',''Y'')
      AND a.concept_id != b.concept_id');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      result_table := MEME_UTILITY.cluster_single(t_sus_cui);
   ELSE
      location := '40';
      result_table := t_sus_cui;
      t_sus_cui := '';
   END IF;

   location := '50';
   MEME_UTILITY.drop_it ('table',t_sus_cui);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_sus_cui);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('suspect_cui',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END suspect_cui;

/* FUNCTION SR_SPLIT ***********************************************************
 * This function returns set of concepts that has been splitted in
 * safe replacement.
 */
FUNCTION sr_split (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   t_sr_split	   VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   t_sr_split := MEME_UTILITY.get_unique_tablename('qat_');

   location := '20';
   local_exec('CREATE TABLE '||t_sr_split||' AS
      SELECT a.concept_id AS concept_id_1, b.concept_id AS concept_id_2
      FROM classes a, classes b, mom_safe_replacement c
      WHERE a.atom_id = old_atom_id
      AND b.atom_id = new_atom_id
      AND a.concept_id != b.concept_id');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      result_table := MEME_UTILITY.cluster_pair(t_sr_split);
   ELSE
      location := '40';
      result_table := t_sr_split;
      t_sr_split := '';
   END IF;

   location := '50';
   MEME_UTILITY.drop_it ('table',t_sr_split);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_sr_split);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('sr_split',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END sr_split;

/* FUNCTION MTH_CLONE_RELS *****************************************************
 */
FUNCTION mth_clone_rels (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id	IN INTEGER := 0
)
RETURN VARCHAR2
IS
   t_clone_prime   VARCHAR2(50);
   t_clone_rels    VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   t_clone_prime := MEME_UTILITY.get_unique_tablename('q_');

   MEME_UTILITY.drop_it ('table',t_clone_prime);

   local_exec('CREATE TABLE '||t_clone_prime||' AS
      (SELECT DISTINCT a.concept_id_1, a.concept_id_2
      FROM relationships a, relationships b, source_rank c, source_rank d
      WHERE a.relationship_name = b.relationship_name
      AND NVL(a.relationship_attribute,''null'') = NVL(b.relationship_attribute,''null'')
      AND a.source != b.source
      AND a.source = c.source
      AND b.source = d.source
      AND c.normalized_source != d.normalized_source
      AND a.concept_id_1 = b.concept_id_1
      AND a.concept_id_2 = b.concept_id_2
      AND a.relationship_level = ''S''
      AND b.relationship_level = ''S''
      AND a.tobereleased IN (''y'',''Y'')
      AND b.tobereleased IN (''y'',''Y'')
      AND a.concept_id_1 != a.concept_id_2
      UNION
      SELECT DISTINCT a.concept_id_1, a.concept_id_2
      FROM relationships a, relationships b, inverse_relationships c,
	   inverse_rel_attributes d, source_rank e, source_rank f
      WHERE a.relationship_name = c.inverse_name
      AND b.relationship_name = c.relationship_name
      AND NVL(a.relationship_attribute,''null'') = NVL(d.inverse_rel_attribute,''null'')
      AND NVL(b.relationship_attribute,''null'') = NVL(d.relationship_attribute,''null'')
      AND a.source != b.source
      AND a.source = e.source
      AND b.source = f.source
      AND e.normalized_source != f.normalized_source
      AND a.concept_id_2 = b.concept_id_1
      AND a.concept_id_1 = b.concept_id_2
      AND a.relationship_level = ''S''
      AND b.relationship_level = ''S''
      AND a.tobereleased IN (''y'',''Y'')
      AND b.tobereleased IN (''y'',''Y'')
      AND a.concept_id_1 != a.concept_id_2)');

   local_exec('DELETE FROM '||t_clone_prime||' g WHERE EXISTS
      (SELECT * FROM relationships h WHERE relationship_level = ''C''
      AND g.concept_id_1 = h.concept_id_1
      AND g.concept_id_2 = h.concept_id_2)');

   local_exec('DELETE from '||t_clone_prime||' g where EXISTS
      (select * from relationships h WHERE relationship_level = ''C''
      AND g.concept_id_2 = h.concept_id_1
      AND g.concept_id_1 = h.concept_id_2)');

   location := '20';
   t_clone_rels := MEME_UTILITY.get_unique_tablename('q_');

   MEME_UTILITY.drop_it ('table',t_clone_rels);

   local_exec('CREATE TABLE '||t_clone_rels||' AS
      SELECT concept_id_1,concept_id_2 FROM '||t_clone_prime||' WHERE
      concept_id_1 < concept_id_2
      UNION
      SELECT concept_id_2,concept_id_1 FROM '||t_clone_prime||' WHERE
      concept_id_2 < concept_id_1');

   IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
      location := '30';
      result_table := MEME_UTILITY.cluster_pair(t_clone_rels);
   ELSE
      location := '40';
      result_table := t_clone_rels;
      t_clone_rels := '';
   END IF;

   location := '50';
   MEME_UTILITY.drop_it ('table',t_clone_prime);
   MEME_UTILITY.drop_it ('table',t_clone_rels);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_clone_prime);
      MEME_UTILITY.drop_it('table',t_clone_rels);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('mth_clone_rels',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END mth_clone_rels;

/* FUNCTION OBSOLETE_NEC_PNS ***************************************************
 */
FUNCTION obsolete_nec_pns (
	cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
	work_id 		IN INTEGER := 0
)
RETURN VARCHAR2
IS
    obsolete_nec_pns	VARCHAR2(50);
    result_table	    VARCHAR2(50);
    location		    VARCHAR2(256);
    l_query             VARCHAR2(500);

BEGIN

    location :='0';

    obsolete_nec_pns := MEME_UTILITY.get_unique_tablename('qat_');

    location :='10';

	l_query := 
        'CREATE TABLE ' || obsolete_nec_pns || ' AS ' ||
	    'SELECT concept_id FROM classes a, atoms b ' ||
		'WHERE a.atom_id = b.atom_id '||
		'AND source = ''MTH'' '||
		'AND termgroup = ''MTH/PN'' '||
		'AND tobereleased IN (''Y'',''y'') '||
		'AND atom_name LIKE ''% NEC in %'' '||
		'AND a.atom_id NOT IN '||
			'(SELECT atom_id FROM atoms, source_version '||
			'WHERE atom_name LIKE ''%''||current_name||''%'')';

    --MEME_UTILITY.put_message(l_query);

	local_exec(l_query);

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
		location :='20';
		result_table := MEME_UTILITY.cluster_single(obsolete_nec_pns);
    ELSE
		result_table := obsolete_nec_pns;
		obsolete_nec_pns := '';
    END IF;

    MEME_UTILITY.drop_it('table',obsolete_nec_pns);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',obsolete_nec_pns);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('obsolete_nec_pns',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END obsolete_nec_pns;


/* FUNCTION SCD_DIFFLUI ***************************************************
 */
FUNCTION scd_difflui (
    cluster_flag	IN INTEGER := MEME_CONSTANTS.CLUSTER_NO,
    work_id 		IN INTEGER := 0
)
RETURN VARCHAR2
IS
    scd_difflui		VARCHAR2(50);
    result_table	VARCHAR2(50);
    location		VARCHAR2(256);
    l_query             VARCHAR2(500);

BEGIN

    location :='0';
    scd_difflui := MEME_UTILITY.get_unique_tablename('qat_');

    location :='10';
    l_query := 
        'CREATE TABLE ' || scd_difflui || ' AS 
	 SELECT concept_id FROM classes WHERE source like ''RXNORM%''
            AND termgroup like ''RXNORM%/SCD'' 
	    AND tobereleased in (''Y'',''y'')
	 GROUP BY concept_id HAVING count(distinct lui)>1';

    location := '15';
    local_exec(l_query);

    IF cluster_flag = MEME_CONSTANTS.CLUSTER_YES THEN
	location :='20';
	result_table := MEME_UTILITY.cluster_single(scd_difflui);
    ELSE
	result_table := scd_difflui;
	scd_difflui := '';
    END IF;

    MEME_UTILITY.drop_it('table',scd_difflui);

    return result_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',scd_difflui);
	MEME_UTILITY.drop_it('table',result_table);
	meme_integrity_proc_error('scd_difflui',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END scd_difflui;

END MEME_INTEGRITY_PROC;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_INTEGRITY_PROC.help;
execute MEME_INTEGRITY_PROC.register_package;

