CREATE OR REPLACE PACKAGE MEME_INTEGRITY AS
/*****************************************************************************
 *
 * PL/SQL File: MEME_INTEGRITY.sql
 *
 * This package contains procedures
 * to perform MEME integrity checkings
 *
 * Version Information
 *
 * 06/01/2005 4.18.0: Released
 * 05/24/2005 4.17.1: Slighlty improved logic for managing obsolete SRC counts
 * 05/23/2005 4.17.0: Released
 * 05/17/2005 4.16.1: Performance enhancement to src_obsolete_qa_diff
 *                    and src_mid_qa_diff and mid_mid_qa_diff
 * 05/16/2005 4.16.0: Released
 * 05/12/2005 4.15.1: src_obsolete_qa_results, src_src_qa_diff
 * 04/18/2005 4.15.0: Released
 * 04/05/2005 4.14.1: src_monster_qa cleans up results from old sources
 *                    before adding new data.
 * 12/30/2004 4.14.0: Released
 * 12/29/2004 4.13.1: Cleaned up code/logging for matrix init/updater
 * 05/03/2004 4.13.0: Fixed to work with oracle 9.2
 * 04/19/2004 4.12.0: Released
 * 04/06/2004 4.11.1: parallelize matrixinit
 * 06/19/2003 4.11.0: Released
 * 06/10/2003 4.10.1: editing_matrix no longer built with nologging
 * 04/09/2002 3.10.0: Released                   
 * 04/01/2002 3.9.1: Changed set_releasability because it was taking
 *                   way too long on oa_mid2003. Added rule hint to queries.
 * 02/28/2002 3.9.0: Released.
 * 02/26/2002 3.8.1: current_msh, previous_msh fields were only 10 chars
 *                   this fix makes them 20 to support msh2002-02-10
 * 10/23/2001 3.8.0: released
 * 10/02/2001 3.7.1: set_cmesh_status disabled again.  MSH2002 has no
 *		     unreviewed concepts anymore so this code is deprecated.
 * 06/04/2001 3.7.0: Released
 * 06/01/2001 3.6.1: mid_mid_qa_diff was fixed.  It was not properly accounting
 *			for adjustments, and if there was a discrepancy it
 *                      would list it in both directions instead of just one.
 * 02/14/2001 3.6.0: Released
 * 02/13/2001 3.5.1: changes to set_cmesh_status
 * 12/11/2000 3.5.0: Released to NLM.
 * 11/29/2000 3.4.1: Changes in mid_mid_qa_diff
 * 10/24/2000 3.4.0: Released
 * 10/18/2000 3.3.6: set_cmesh_stys DISABLED
 * 09/22/2000 3.3.5: In TABLE mode, analyze the table!
 * 09/19/2000 3.3.4: More verbose matrix logging.
 * 09/14/2000 3.3.3: changes in monster_qa, src_monster_qa, src_mid_qa_diff,
 *		     mid_mid_qa_diff
 * 09/11/2000 3.3.2: better elapsed time in matrix init/updater
 * 08/30/2000 3.3.1: mid_mid_qa_diff
 * 08/24/2000 3.3.0: Released
 * 08/24/2000 3.2.2: log only matrix_init,updater or calculate_snapshot
 * 08/16/2000 3.2.1: src_mid_qa_diff, better comments in matrix init
 * 08/01/2000 3.2.0: Package handover version
 * 07/21/2000 3.1.3: monster_qa, src_monster_qa
 * 05/16/2000 3.1.2: set_releasability: concepts with ONLY MTH atoms are
 *		     considered unreleasable.
 * 04/13/2000 3.1.1: Analyze restrict table in make_subset,
 *		     enable set_cmesh_status
 * 9/9/1999:	First version created and compiled
 *
 * Status:
 *	Functionality:	DONE
 *	Testing:  	DONE
 * 	Enhancements:
 *
 *****************************************************************************/

    package_name	VARCHAR2(25) := 'MEME_INTEGRITY';
    release_number	VARCHAR2(1)  := '4';
    version_number	VARCHAR2(5)  := '18.0';
    version_date	DATE	     := '01-Jun-2005';
    version_authority	VARCHAR2(3)  := 'BAC';

    meme_integrity_debug BOOLEAN := FALSE;
    meme_integrity_trace BOOLEAN := FALSE;

    MI_TABLE		CONSTANT VARCHAR2(10) := 'TABLE';
    MI_TIMESTAMP	CONSTANT VARCHAR2(10) := 'TIMESTAMP';
    MI_CATCHUP		CONSTANT VARCHAR2(10) := 'CATCHUP';

    matrix_result_table VARCHAR2(50);
    location		VARCHAR2(10);
    err_msg		VARCHAR2(256);
    error_code		INTEGER;
    retval		INTEGER;
    meme_integrity_exc	EXCEPTION;

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

    PROCEDURE local_exec (query IN VARCHAR2);
    FUNCTION local_exec (query IN VARCHAR2) RETURN INTEGER;

    FUNCTION version_info RETURN VARCHAR2;
    PRAGMA restrict_references (version_info,WNDS,RNDS,WNPS);

    PROCEDURE meme_integrity_error(
	method		IN VARCHAR2,
	location	IN VARCHAR2,
	error_code	IN INTEGER,
	detail		IN VARCHAR2
    );

    -- Sets releasability in concept_status
    PROCEDURE set_releasability (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	work_id    IN INTEGER := 0 );

    -- Sets status fields in editing_matrix
    PROCEDURE set_em_status (
	table_name IN VARCHAR2 := 'editing_matrix',
	work_id    IN INTEGER := 0 );

    -- Sets pure cmesh to status 'U' in concept_status
    PROCEDURE set_cmesh_status (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	work_id    IN INTEGER := 0 );

    PROCEDURE finalize_initialization ( work_id IN INTEGER := 0 );

    -- Returns work_id
    FUNCTION matrix_initializer ( work_id  IN INTEGER := 0 )
	RETURN INTEGER;

    FUNCTION make_subset(
	run_mode   	IN VARCHAR2 := MI_CATCHUP,
	table_name 	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	timestamp  	IN DATE := NULL ) RETURN VARCHAR2;

    -- Returns work_id
    FUNCTION matrix_updater(
	run_mode 	IN VARCHAR2,
	table_name 	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	timestamp	IN DATE := NULL,
	work_id 	IN INTEGER := 0 )
    RETURN INTEGER;

    PROCEDURE actions_initializer (
	table_name 	IN VARCHAR2 := 'editing_matrix',
	work_id 	IN INTEGER := 0
    );

    FUNCTION violations_initializer (
	table_name 	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE
    ) RETURN INTEGER;

    -- In addition to Matrix, Integrities, and QA bin queries
    -- This package implements the integrity_snapshot
    PROCEDURE snapshot_pre;
    PROCEDURE snapshot_post;
    PROCEDURE snapshot_diff;
    PROCEDURE calculate_snapshot ( pre_post_diff IN VARCHAR2);

    PROCEDURE monster_qa_help(
	tbl_qa_queries IN VARCHAR2,
	qa_id	       IN INTEGER);
    PROCEDURE monster_qa;

    PROCEDURE src_monster_qa;

    PROCEDURE src_mid_qa_diff;

    PROCEDURE src_obsolete_qa_diff;

    PROCEDURE mid_mid_qa_diff(history_qa_id IN INTEGER);

END MEME_INTEGRITY;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY MEME_INTEGRITY AS

FUNCTION release
RETURN INTEGER
IS
BEGIN

    version;
    return to_number(release_number);
END release;

FUNCTION version
RETURN FLOAT
IS
BEGIN

    version;
    return to_number(version_number);
END version;


PROCEDURE version
IS
BEGIN

    DBMS_OUTPUT.PUT_LINE('Package: ' || package_name);
    DBMS_OUTPUT.PUT_LINE('Release ' || release_number || ': ' ||
			 'version ' || version_number || ', ' ||
			 version_date || ' (' ||
			 version_authority || ')');

END version;

FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN

    return package_name || ' Release ' || release_number || ': ' ||
	       'version ' || version_number || ' (' || version_date || ')';

END version_info;

/* PROCEDURE help **************************************************************
 */
PROCEDURE help
IS
BEGIN
    help('');
END;

/* PROCEDURE help **************************************************************
 */
PROCEDURE help ( topic IN VARCHAR2 )
IS
BEGIN
    -- This procedure requires SET SERVEROUTPUT ON
	--    DBMS_OUTPUT.ENABLE(100000);

    -- Print version
    MEME_UTILITY.PUT_MESSAGE('.');
    version;
END help;

/* PROCEDURE register_package **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_INTEGRITY.release_number,
      MEME_INTEGRITY.version_number,
      SYSDATE,
      MEME_INTEGRITY.version_authority,
      MEME_INTEGRITY.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

/* PROCEDURE set_trace_on ******************************************************
 */
PROCEDURE set_trace_on
IS
BEGIN

    meme_integrity_trace := TRUE;

END set_trace_on;

/* PROCEDURE set_trace_off *****************************************************
 */
PROCEDURE set_trace_off
IS
BEGIN

    meme_integrity_trace := FALSE;

END set_trace_off;

/* PROCEDURE set_debug_on ******************************************************
 */
PROCEDURE set_debug_on
IS
BEGIN

    meme_integrity_debug := TRUE;

END set_debug_on;

/* PROCEDURE set_debug_off *****************************************************
 */
PROCEDURE set_debug_off
IS
BEGIN

    meme_integrity_debug := FALSE;

END set_debug_off;

/* PROCEDURE trace *************************************************************
 */
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF meme_integrity_trace = TRUE THEN

	MEME_UTILITY.PUT_MESSAGE(message);

    END IF;

END trace;

/* PROCEDURE local_exec ********************************************************
 */
PROCEDURE local_exec ( query IN VARCHAR2 )
IS
BEGIN

    IF meme_integrity_trace = TRUE THEN
	MEME_UTILITY.put_message(query);
    END IF;

    IF meme_integrity_debug = FALSE THEN
	MEME_UTILITY.exec(query);
    END IF;

END local_exec;

/* FUNCTION local_exec *********************************************************
 */
FUNCTION local_exec ( query IN VARCHAR2 )
RETURN INTEGER
IS
BEGIN

    IF meme_integrity_trace = TRUE THEN
	MEME_UTILITY.put_message(query);
    END IF;

    IF meme_integrity_debug = FALSE THEN
	return MEME_UTILITY.exec(query);
    END IF;

    RETURN 0;

END local_exec;

/* PROCEDURE meme_integrity_error **********************************************
 */
PROCEDURE meme_integrity_error(
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

    MEME_UTILITY.PUT_ERROR('Error in MEME_INTEGRITY.' ||
			   ' Method: ' || method ||
			   ' Location: ' || location  ||
			   ' Error Code: '|| error_msg ||
			   ' Detail: '|| detail);

END meme_integrity_error;


/* PROCEDURE set_releasability *********************************************
 * Set concept_status.tobereleased
 */
PROCEDURE set_releasability (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	work_id	   IN INTEGER := 0
)
IS
    ct				INTEGER;
    releasable_concepts		VARCHAR2(50);
    unreleasable_concepts	VARCHAR2(50);
    and_restriction_clause	VARCHAR2(256);
    where_restriction_clause	VARCHAR2(256);
    location			VARCHAR2(5);
BEGIN

    --
    -- Starting
    --
    MEME_UTILITY.PUT_MESSAGE(LPAD('Setting tobereleased',45,'. '));
    MEME_UTILITY.sub_timing_start;

    --
    -- Set up restriction clause
    --
    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	and_restriction_clause :=
	  ' AND concept_id IN (SELECT concept_id FROM ' || table_name || ')';
	where_restriction_clause :=
	  ' WHERE concept_id IN (SELECT concept_id FROM ' || table_name || ')';
    END IF;


    --
    -- Get tables for releasable/unreleasble concepts
    --
    releasable_concepts := MEME_UTILITY.get_unique_tablename;
    unreleasable_concepts := MEME_UTILITY.get_unique_tablename;

    --
    -- Checking classes with no atoms
    -- concepts with ONLY releasable MTH atoms
    --	 are considered UNreleasable. (MAYBE)
    --
    location := '0';
    local_exec (
	    'CREATE TABLE ' || releasable_concepts || ' AS
	     SELECT /*+ PARALLEL(c) */ 
		DISTINCT concept_id,tobereleased 
	     FROM classes c
	     WHERE tobereleased in (''Y'',''y'',''?'')
	       AND termgroup not in (''MTH/MM'',''MTH/TM'') ' ||
	    and_restriction_clause );

    location := '30';
    local_exec (
	'CREATE TABLE ' || unreleasable_concepts || ' AS
	SELECT concept_id FROM concept_status cs ' ||
	where_restriction_clause || '
	MINUS 
	SELECT concept_id FROM ' || releasable_concepts);

    location := '50';
    EXECUTE IMMEDIATE 
	'CREATE INDEX x_rel_concepts ON ' || releasable_concepts || ' (concept_id) 
	 COMPUTE STATISTICS PARALLEL';
    location := '51';

    ct := local_exec(
	'UPDATE /*+ parallel(cs) */  concept_status cs
	 SET tobereleased = ''N''
	 WHERE concept_id IN 
	 (SELECT concept_id FROM ' || unreleasable_concepts || ') '
	);
    IF ct > 0 THEN
	MEME_UTILITY.PUT_MESSAGE(
		LPAD(ct || ' rows set to tobereleased=''N''',43,'. '));
    END IF;


    --
    -- Get releasable concepts (tbr=Y)
    --
    location := '60';
    ct := local_exec (
	'UPDATE /*+ PARALLEL(cs) */ concept_status cs
    	 SET tobereleased = ''Y''
    	 WHERE cs.concept_id IN
	  (SELECT concept_id FROM ' || releasable_concepts || '
	   WHERE tobereleased = ''Y'')
    	   AND cs.tobereleased NOT IN (''Y'') '
	);
    IF ct > 0 THEN
	MEME_UTILITY.PUT_MESSAGE(
		LPAD(ct || ' rows set to tobereleased=''Y''',43,'. '));
    END IF;

    --
    -- Get releasable concepts (tbr=y)
    --
    location := '65';
    ct := local_exec (
	'UPDATE /*+ PARALLEL(cs) */ concept_status cs
    	 SET tobereleased = ''y''
    	 WHERE cs.concept_id IN 
	  (SELECT concept_id FROM ' || releasable_concepts || ' 
	   WHERE tobereleased = ''y'') 
    	   AND cs.tobereleased NOT IN (''Y'',''y'') '
	);
    IF ct > 0 THEN
	MEME_UTILITY.PUT_MESSAGE(
		LPAD(ct || ' rows set to tobereleased=''y''',43,'. '));
    END IF;

    --
    -- Get releasable concepts (tbr=?)
    --
    location := '70';
    ct := local_exec (
	'UPDATE /*+ PARALLEL(cs) */ concept_status cs
    	 SET tobereleased = ''?''
    	 WHERE cs.concept_id IN 
	  (SELECT concept_id FROM ' || releasable_concepts || '
	   WHERE tobereleased = ''?'')
    	   AND cs.tobereleased NOT IN (''Y'',''y'',''?'') '
	);
    IF ct > 0 THEN
	MEME_UTILITY.PUT_MESSAGE(
		LPAD(ct || ' rows set to tobereleased=''?''',43,'. '));
    END IF;

    --
    -- Get unreleasable concepts (tbr=n)
    --
    location := '80';
    ct := local_exec (
	'UPDATE /*+ PARALLEL(cs) */ concept_status cs 
    	 SET tobereleased = ''n''
    	 WHERE cs.concept_id IN
	  (SELECT c.concept_id FROM classes c
	   WHERE c.tobereleased = ''n'')
    	   AND cs.tobereleased NOT IN (''Y'',''y'',''?'',''n'') ' ||
	and_restriction_clause
	);
    IF ct > 0 THEN
	MEME_UTILITY.PUT_MESSAGE(
		LPAD(ct || ' rows set to tobereleased=''n''',43,'. '));
    END IF;

    location := '90';
    MEME_UTILITY.drop_it ('table',unreleasable_concepts);
    MEME_UTILITY.drop_it ('table',releasable_concepts);

    MEME_UTILITY.sub_timing_stop;

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.drop_it ('table',unreleasable_concepts);
	MEME_UTILITY.drop_it ('table',releasable_concepts);
	meme_integrity_error('set_releasability',location,1,SQLERRM);
	RAISE meme_integrity_exc;

END set_releasability;

/* PROCEDURE set_em_status *****************************************************
 *  Compute editing matrix status
 */
PROCEDURE set_em_status (
	table_name IN VARCHAR2 := 'editing_matrix',
	work_id	   IN INTEGER := 0
)
IS
    location		VARCHAR2(5);
BEGIN

    MEME_UTILITY.PUT_MESSAGE(LPAD('Set classes_status in EM',45,'. '));
    MEME_UTILITY.sub_timing_start;

    --
    -- Set for needs review atoms
    --
    location := '110';
    local_exec (
	'UPDATE ' || table_name || ' em
	 SET classes_status = ''N''
    	 WHERE concept_id IN
     	  (SELECT /*+ parallel(a) */ concept_id FROM classes a
     	   WHERE tobereleased IN (''Y'',''y'')
	     AND status = ''N'') '
	);

    MEME_UTILITY.PUT_MESSAGE(LPAD('Set attribute_status in EM',45,'. '));

    --
    -- Set for needs review attributes
    --
    location := '140';
    local_exec (
	'UPDATE ' || table_name || ' em
	 SET attributes_status = ''N''
    	 WHERE concept_id IN
     	  (SELECT  /*+ parallel(a) */ concept_id FROM attributes a
     	   WHERE tobereleased IN (''Y'',''y'')
	     AND status = ''N'') '
	);

    MEME_UTILITY.PUT_MESSAGE(LPAD('Set relationship_status in EM',45,'. '));

    --
    -- These queries may require change to allow source_level,
    -- status 'N' rels to trigger editing
    --

    --
    -- Set for needs review relationships (id1)
    --
    location := '170';
    local_exec (
	'UPDATE ' || table_name || ' em
	 SET relationships_status = ''N''
    	 WHERE concept_id IN
     	  (SELECT /*+ parallel(r) */ concept_id_1 FROM relationships r
     	   WHERE tobereleased IN (''Y'',''y'')
	     AND status = ''N'' AND relationship_level != ''S'') '
	);

    --
    -- Set for needs review relationships (id2)
    --
    location := '180';
    local_exec (
	'UPDATE ' || table_name || ' em
	 SET relationships_status = ''N''
    	 WHERE concept_id IN
     	  (SELECT /*+ parallel(r) */ concept_id_2 FROM relationships r
     	   WHERE tobereleased IN (''Y'',''y'')
	     AND status = ''N'' AND relationship_level != ''S'') '
	);

    --
    -- Set for demotions (id1)
    --
    location := '190';
    local_exec (
	'UPDATE ' || table_name || ' em
	 SET relationships_status = ''D''
    	 WHERE concept_id IN
     	  (SELECT /*+ parallel(r) */ concept_id_1 FROM relationships r
     	   WHERE tobereleased IN (''Y'',''y'')
	   AND status = ''D'' AND relationship_level != ''S'') '
	);

    --
    -- Set for demotions (id2)
    --
    location := '200';
    local_exec (
	'UPDATE ' || table_name || ' em
	 SET relationships_status = ''D''
    	 WHERE concept_id IN
     	  (SELECT /*+ parallel(r) */ concept_id_2 FROM relationships r
     	   WHERE tobereleased IN (''Y'',''y'')
	     AND status = ''D'' AND relationship_level != ''S'') '
	);

    MEME_UTILITY.sub_timing_stop;

EXCEPTION

    WHEN OTHERS THEN
	meme_integrity_error('set_em_status',location,1,SQLERRM);
	RAISE meme_integrity_exc;

END set_em_status;

/* PROCEDURE set_cmesh_status **************************************************
 * NOT USED
 */
PROCEDURE set_cmesh_status (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	work_id	   IN INTEGER := 0
)
IS
    cmesh_only		VARCHAR2(50);
    restriction_clause	VARCHAR2(100);
    location 		VARCHAR2(5);
    row_count		INTEGER;
    transaction_id	INTEGER;

BEGIN

    MEME_UTILITY.PUT_MESSAGE(LPAD('Set CMesH status has been DISABLED',45,' .'));
    RETURN;

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table', cmesh_only);
	MEME_UTILITY.drop_it('table', cmesh_only || '_tofix');
	meme_integrity_error('set_cmesh_status',location,error_code,SQLERRM);
	RAISE meme_integrity_exc;

END set_cmesh_status;

/* PROCEDURE finalize_initialization *******************************************
 */
PROCEDURE finalize_initialization (
    work_id		IN INTEGER := 0
)
IS
    i 		INTEGER;
    table_name 	VARCHAR2(50);
BEGIN

    MEME_UTILITY.PUT_MESSAGE(LPAD('Finalizing Steps',45,'. '));

    -- "Catch up" Matrix initializer in max_Tab
    location := '0';
    SELECT max_id INTO i FROM max_tab
    WHERE table_name = 'MOLECULAR_ACTIONS';

    location := '10';
    UPDATE max_tab SET max_id = i
    WHERE table_name = MEME_CONSTANTS.IV_MATRIX;

EXCEPTION

    WHEN OTHERS THEN
	meme_integrity_error('finalize_initialization',location,1,SQLERRM);
	RAISE meme_integrity_exc;

END finalize_initialization;

/* FUNCTION matrix_initializer *************************************************
 * Initialize matrix
 */
FUNCTION matrix_initializer (
	work_id 	IN INTEGER := 0
)
RETURN INTEGER
IS
    start_time 		INTEGER;
    elapsed_time	INTEGER;
    ct	  		INTEGER;
    i	  		INTEGER;
    j			INTEGER;
    increment		INTEGER := 500000;
    iv			VARCHAR2(2000);
    err_msg		VARCHAR2(256);
    ic_name		VARCHAR2(10);
    ic_code		VARCHAR2(10);
    ic_state		VARCHAR2(10);
    ic_procedure	VARCHAR2(50);
    empty_concept	VARCHAR2(50);
    local_work_id	INTEGER;
    retval		INTEGER;
    mi_exception	EXCEPTION;
    current_msh		VARCHAR2(20);
    previous_msh	VARCHAR2(20);

BEGIN

    --
    -- Start
    --
    MEME_UTILITY.put_message(LPAD('Initializing Matrix Initializer',45,'. '));
    start_time := DBMS_UTILITY.get_time;

    --
    -- Get Integrity Vector
    --
    location := '0';
    err_msg := 'Error getting integrity_vector.';
    iv := MEME_UTILITY.get_integrity_vector(MEME_CONSTANTS.IV_MATRIX);

    --
    -- Get Work_ID, or use passed in work_id
    --
    location := '5';
    err_msg := 'Error getting new work_id.';
    IF work_id = 0 THEN
      local_work_id := MEME_UTILITY.new_work(
	  authority => MEME_CONSTANTS.MATRIX_AUTHORITY, 
	  type => MEME_CONSTANTS.MW_MATRIX,
	  description => 
	    'The matrix initializer is being run with integrity vector '''||
	    iv || '''. ' );
    ELSE
	local_work_id := work_id;
    END IF;

    --
    -- Log progress
    --
    location := '6';
    err_msg := 'Error logging progress.';
    MEME_UTILITY.log_progress (
	MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_initializer',
       	'Starting MEME_INTEGRITY.matrix_initializer',
	0, local_work_id, 0);

    --
    -- Get current and previous versions of MSH
    --
    location := '7';
    err_msg := 'Error getting current and previous msh.';
    current_msh := MEME_UTILITY.get_current_name('MSH');
    previous_msh := MEME_UTILITY.get_previous_name('MSH');


    --
    -- Set tobereleased field in concept_status
    --
    MEME_UTILITY.sub_timing_start;
    location := '10';
    err_msg := 'Error setting releasability.';
    set_releasability( work_id => local_work_id );

    --
    -- Log progress
    --
    location := '190';
    MEME_UTILITY.sub_timing_stop;
    err_msg := 'Error logging progress (' || ic_procedure || ').';
    MEME_UTILITY.log_progress (
	MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_initializer',
       	'MEME_INTEGRITY.matrix_initializer::set_releasability',
	0, local_work_id, MEME_UTILITY.sub_elapsed_time);

    --
    -- disable indexes on editing_matrix, meme_cluster_history
    --
    location := '20';
    err_msg := 'Error dropping editing_matrix, meme_cluster_history indexes.';
    MEME_SYSTEM.drop_indexes('editing_matrix');
    MEME_SYSTEM.drop_indexes('meme_cluster_history');

    --
    -- Build editing matrix
    --
    MEME_UTILITY.PUT_MESSAGE(LPAD('Bulding editing matrix',45,'. '));
    location := '100';
    err_msg := 'Error truncating editing_matrix.';
    MEME_SYSTEM.truncate('editing_matrix');
    location := '120';
    err_msg := 'Error building editing_matrix..';
    INSERT INTO editing_matrix(concept_id, classes_status,
		    attributes_status, relationships_status,
		    integrity_status, integrity_vector,
		    clean_molecule_id, last_app_change,
		    last_app_change_by, last_unapp_change,
		    last_unapp_change_by)
    SELECT concept_id, 'R', 'R', 'R', 'R', '', 0,
	   SYSDATE, authority, '', ''
    FROM concept_status;
    COMMIT;

    location := '140';
    err_msg := 'Error analyzing editing_matrix.';
    MEME_SYSTEM.analyze('editing_matrix');

    --
    -- Set {classes,relationships,attributes}_status
    --
    MEME_UTILITY.sub_timing_start;
    location := '160';
    err_msg := 'Error setting editing_matrix status.';
    set_em_status( work_id => local_work_id );

    --
    -- Log progress
    --
    MEME_UTILITY.sub_timing_stop;
    location := '150';
    err_msg := 'Error logging progress set_em_status.';
    MEME_UTILITY.log_progress (
	MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_initializer',
       	'MEME_INTEGRITY.matrix_initializer::set_em_status',
	0, local_work_id, MEME_UTILITY.sub_elapsed_time);

    --
    -- Loop through the integrity vector and call the
    -- corresponding functions then update editing_matrix
    --
    i := 1;
    LOOP

    	--
	-- Parse vector to get individual checks and codes
    	--
    	location := '170';
    	err_msg := 'Error parsing integrity vector.';
	ic_name := MEME_UTILITY.GET_IC_NAME_BY_NUM(iv,i);
	ic_code := MEME_UTILITY.GET_IC_CODE_BY_NUM(iv,i);
	ic_state := MEME_UTILITY.GET_IC_STATE(ic_code);
	EXIT WHEN ic_name IS NULL;

    	--
	-- Get procedure matching current ic_code;
    	--
	location := '180';
    	err_msg := 'Error looking up ic_procedure (' || ic_name || ').';
	ic_procedure := 'MEME_INTEGRITY_PROC.' ||
			MEME_UTILITY.get_procedure_name_by_ic(upper(ic_name));

    	--
	-- Print procedure name
    	--
	MEME_UTILITY.put_message(LPAD('.....' || ic_procedure,45,'. '));

    	--
	-- Dynamically execute procedure name, set result to
	-- MEME_INTEGRITY.matrix_result_table
	-- Note the := in dynamic SQL only works for globally declared
	-- variables, hence the package variable.  The downside is that
	-- it means only one instance of matrix init can run at once
	-- otherwise this variable may be overwritten
    	--
        IF upper(ic_procedure) != MEME_CONSTANTS.NO_PROCEDURE THEN

	    MEME_UTILITY.sub_timing_start;
	    location := '220';
  	    err_msg := 
	      'Error dynamically calling procedure (' || ic_procedure || ').';
	    local_exec (
	    	'BEGIN MEME_INTEGRITY.matrix_result_table := ' ||
	    	 ic_procedure || '(work_id => ' || work_id || '); END;'
	    );

    	    --
	    -- Log progress
    	    --
	    MEME_UTILITY.sub_timing_stop;
	    location := '190';
	    err_msg := 'Error logging progress (' || ic_procedure || ').';
	    MEME_UTILITY.log_progress (
	 	MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_initializer',
       		'MEME_INTEGRITY.matrix_initializer::' || ic_procedure,
		0, local_work_id, MEME_UTILITY.sub_elapsed_time);

    	    --
	    -- Verbose logging.  If the concept count is under 20 list the
	    -- concept ids, otherwise just print count.
    	    --
	    ct := MEME_UTILITY.exec_count(matrix_result_table);
	   IF ct <= 20 and ct > 0 THEN
	   	MEME_UTILITY.put_message(
		    LPAD('(' || ct || ' concepts affected: ' ||
		      MEME_UTILITY.table_to_string(matrix_result_table) || ')',
		     43,'. '));
	   ELSE
	     	MEME_UTILITY.put_message(
		  LPAD('(' || ct || ' concepts affected)',43,'. '));
	   END IF;

    	    --
	    -- Update editing matrix with results 
	    -- (for updater this is different)
    	    --
	    location := '230';
  	    err_msg := 'Error updating editing_matrix.';
	    local_exec(
    	    'UPDATE editing_matrix 
    	     SET integrity_vector = 
	         integrity_vector || ''<' || ic_name ||
				      ':' || ic_state || '>''
       	     WHERE concept_id IN 
    	        (SELECT concept_id FROM ' || matrix_result_table || ')'
	    );

	    location := '240';
  	    err_msg := 'Error dropping matrix_result_table.';
	    MEME_UTILITY.drop_it('table', matrix_result_table);

        END IF;

    	--
        -- Increment ic counter
    	--
	i := i + 1;

    END LOOP;

    --
    -- Set integrity status
    --
    location := '250';
    err_msg := 'Error setting editing_matrix.integrity_status.';
    local_exec(
	'UPDATE editing_matrix e
	 SET integrity_status = ''N''
	 WHERE integrity_vector LIKE ''%:V>%'' '
	);

    --
    -- Calls Actions_Initializer
    --
    MEME_UTILITY.PUT_MESSAGE(LPAD('Actions_Initializer',45,'. '));
    location := '260';
    err_msg := 'Error calling actions_initializer.';
    actions_initializer('editing_matrix',local_work_id);


    --
    -- Reindex editing_matrix and meme_cluster history
    --
    location := '310';
    MEME_UTILITY.PUT_MESSAGE(LPAD('Reindex editing_matrix',45,'. '));
    err_msg := 'Error reindexing editing_matrix.';
    MEME_SYSTEM.reindex('editing_matrix');
    location := '320';
    err_msg := 'Error reindexing meme_cluster_history.';
    MEME_SYSTEM.reindex('meme_cluster_history');

    -- 
    -- Update MATRIX_INITIALIZER row in max_tab,
    --
    location := '330';
    err_msg := 'Error finalizing initialization.';
    finalize_initialization( work_id => local_work_id );

    --
    -- Drop all tmp tables
    --
    location := '340';
    MEME_UTILITY.drop_it('table',empty_concept);

    location := '345';
    err_msg := 'Error resetting progress (' || local_work_id || ').';
    MEME_UTILITY.reset_progress(local_work_id);

    --
    -- Log operation
    --
    elapsed_time := ((DBMS_UTILITY.get_time - start_time)*10);
    location := '350';
    err_msg := 'Error logging operation.';
    MEME_UTILITY.log_operation(
	authority => MEME_CONSTANTS.MATRIX_AUTHORITY,
	activity => 'Matrix initializer',
	detail => 'Done intializing matrix (' || iv || ')', 
	transaction_id => 0, 
	work_id => local_work_id,
	elapsed_time => elapsed_time);

    MEME_UTILITY.PUT_MESSAGE(LPAD('Matrix Initializer Done',45,'. '));

    RETURN local_work_id;

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.DROP_IT('table',empty_concept);
	MEME_UTILITY.DROP_IT('table',matrix_result_table);
	MEME_UTILITY.reset_progress(local_work_id);
	meme_integrity_error('matrix_initializer',location,1,
		err_msg || '(' || SQLERRM || ')');
	RAISE MEME_INTEGRITY_EXC;

END matrix_initializer;

/* FUNCTION matrix_updater *****************************************************
 * Update matrix
 */
FUNCTION matrix_updater(
	run_mode 	IN VARCHAR2,
	table_name	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	timestamp 	IN DATE := NULL,
	work_id 	IN INTEGER := 0
)
RETURN INTEGER
IS
    start_time		INTEGER;
    elapsed_time	INTEGER;
    ct	  		INTEGER;
    i	  		INTEGER;
    j			INTEGER;
    max_concept_id	INTEGER;
    retval		INTEGER;
    local_work_id	INTEGER;
    iv			VARCHAR2(2000);
    ic_name		VARCHAR2(10);
    ic_code		VARCHAR2(10);
    ic_state		VARCHAR2(10);
    ic_procedure	VARCHAR2(50);
    empty_concept	VARCHAR2(50);
    t_pure_u_only	VARCHAR2(50);
    t_non_pure_u_only	VARCHAR2(50);
    current_msh		VARCHAR2(20);
    previous_msh	VARCHAR2(20);
    restrict_table	VARCHAR2(50);
    tmp_editing_matrix	VARCHAR2(50);

BEGIN

    --
    -- Starting
    --
    MEME_UTILITY.PUT_MESSAGE(LPAD('Starting Matrix Updater',45,'. '));
    start_time := DBMS_UTILITY.get_time;

    --
    -- Get Integrity Vector
    --
    location := '0';
    err_msg := 'Error getting integrity_vector for ' || 
		MEME_CONSTANTS.IV_MATRIX;
    iv := MEME_UTILITY.get_integrity_vector(MEME_CONSTANTS.IV_MATRIX);

    --
    -- Get Work_ID
    --
    location := '10';
    err_msg := 'Error getting new work_id.';
    IF work_id = 0 THEN
      local_work_id := MEME_UTILITY.new_work(
	  authority => MEME_CONSTANTS.MATRIX_AUTHORITY, 
	  type => MEME_CONSTANTS.MW_MATRIX,
	  description => 
   	   'The matrix updater is being run with integrity_vector ''' ||
	   iv || '''. ' );
    ELSE
	local_work_id := work_id;
    END IF;

    --
    -- Log progress
    --
    location := '15';
    err_msg := 'Error logging progress.';
    MEME_UTILITY.log_progress (
	MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_updater',
       	'Starting MEME_INTEGRITY.matrix_updater',
	0, local_work_id, 0);

    --
    -- Get current/previous MSH versions
    --
    location := '20';
    err_msg := 'Error getting current/previous MSH.';
    current_msh := MEME_UTILITY.get_current_name('MSH');
    previous_msh := MEME_UTILITY.get_previous_name('MSH');

    --
    -- The first step is to get our restrict set
    -- This includes things not touched but prevously clustered
    -- in meme_cluster_history with things that were touched.
    --
    location := '30';
    MEME_UTILITY.sub_timing_start;
    err_msg := 'Error generating concept_id subset (' ||
		run_mode || ',' || table_name || ',' || timestamp || ').';
    restrict_table := make_subset(run_mode,table_name,timestamp);

    --
    -- Log progress
    --
    location := '35';
    MEME_UTILITY.sub_timing_stop;
    err_msg := 'Error logging progress of make_subset.';
    MEME_UTILITY.log_progress (
	MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_updater',
       	'MEME_INTEGRITY.matrix_updater::make_subset',
	0, local_work_id, MEME_UTILITY.sub_elapsed_time);

    --
    -- Set concept_status.tobereleased
    --
    MEME_UTILITY.sub_timing_start;
    location := '40';
    err_msg := 'Error setting releasabilty.';
    set_releasability( table_name => restrict_table,
		       work_id => local_work_id);

    --
    -- Log progress
    --
    location := '50';
    MEME_UTILITY.sub_timing_stop;
    err_msg := 
	'Error logging progress set_releasability (' || restrict_table || ').';
    MEME_UTILITY.log_progress (
	MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_updater',
       	'MEME_INTEGRITY.matrix_updater::set_releasability(' || 
	 restrict_table || ')',
	0, local_work_id, MEME_UTILITY.sub_elapsed_time);

    --
    -- NO need to disable editing_matrix index
    --
    MEME_UTILITY.PUT_MESSAGE(LPAD('Bulding editing matrix',45,'. '));
    location := '90';
    err_msg := 'Error deleting from editing_matrix.';
    local_exec (
	'DELETE FROM editing_matrix where concept_id IN ' ||
	'(SELECT concept_id FROM ' || restrict_table || ')'
	);

    location := '100';
    err_msg := 'Error getting unique tablename.';
    tmp_editing_matrix := MEME_UTILITY.get_unique_tablename;

    location := '110';
    err_msg := 
	'Error creating tmp_editing_matrix (' || tmp_editing_matrix || ').';
    local_exec (
	'CREATE TABLE ' || tmp_editing_matrix || ' AS 
    	 SELECT * FROM editing_matrix WHERE 1=0' );

    location := '115';
    err_msg := 
	'Error creating tmp_editing_matrix (' || tmp_editing_matrix || ').';
    local_exec (
	'INSERT INTO ' || tmp_editing_matrix || ' 
	   	    (concept_id, classes_status, 
		    attributes_status, relationships_status,
		    integrity_status, integrity_vector, 
		    clean_molecule_id, last_app_change, 
		    last_app_change_by, last_unapp_change, 
		    last_unapp_change_by) 
   	SELECT concept_id, ''R'', ''R'', ''R'', ''R'', null, 0, 
	   SYSDATE, authority, null, null
	FROM concept_status 
	WHERE concept_id IN 
	 (SELECT concept_id FROM ' || restrict_table || ')'
	);

    COMMIT;

    --
    -- Set {classes,attributes,relationships}_status
    --
    MEME_UTILITY.sub_timing_start;
    location := '120';
    set_em_status( table_name => tmp_editing_matrix,
		   work_id => local_work_id );
    --
    -- Log progress
    --
    location := '130';
    MEME_UTILITY.sub_timing_stop;
    err_msg := 
	'Error logging progress set_em_status (' || tmp_editing_matrix || ').';
    MEME_UTILITY.log_progress (
	MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_updater',
       	'MEME_INTEGRITY.matrix_updater::set_em_status(' || 
	tmp_editing_matrix || ')',
	0, local_work_id, MEME_UTILITY.sub_elapsed_time);

    --
    -- Loop through the integrity vector and call the
    -- corresponding functions then update {tmp_,}editing_matrix
    --
    i := 1;
    LOOP

	--
	-- Start timing
	--
	MEME_UTILITY.sub_timing_start;

	--
	-- Parse vector to get individual checks and codes
	--
	location := '140';
	err_msg := 'Error parsing integrity_vector.';
	ic_name := MEME_UTILITY.GET_IC_NAME_BY_NUM(iv,i);
	ic_code := MEME_UTILITY.GET_IC_CODE_BY_NUM(iv,i);
	ic_state := MEME_UTILITY.GET_IC_STATE(ic_code);
	EXIT WHEN ic_name IS NULL;

	--
	--
	-- Get procedure matching current ic_code
	location := '150';
	err_msg := 'Error looking up procedure for ic_name (' || ic_name || ').';
	ic_procedure := 'MEME_INTEGRITY_PROC.' ||
			MEME_UTILITY.get_procedure_name_by_ic(upper(ic_name));

	--
	--
	-- Print procedure name
	MEME_UTILITY.put_message(LPAD(ic_procedure,45,'. '));


	--
	-- Dynamically execute procedure name, set result to
	-- MEME_INTEGRITY.matrix_result_table
	-- Note the := in dynamic SQL only works for globally declared
	-- variables, hence the package variable.  The downside is that
	-- it means only one instance of matrix init can run at once
	-- otherwise this variable may be overwritten
	--
        IF upper(ic_procedure) != MEME_CONSTANTS.NO_PROCEDURE THEN

	    location := '200';
	    err_msg := 'Error dynamically calling ' || ic_procedure || '(' ||
			restrict_table || ').';
	    local_exec (
	        'BEGIN MEME_INTEGRITY.matrix_result_table := ' ||
	        ic_procedure || '( table_name => ''' || restrict_table ||
	    	''', work_id => ' || work_id || '); END;'
	    );

	    --
	    -- Verbose logging.  If the concept count is under 20 list the
	    -- concept ids, otherwise just print count.
	    --
	    ct := MEME_UTILITY.exec_count(matrix_result_table);
	    IF ct <= 20 and ct > 0 THEN
	    	MEME_UTILITY.put_message(
		    LPAD('(' || ct || ' concepts affected: ' ||
		      MEME_UTILITY.table_to_string(matrix_result_table) || ')',
		       43,'. '));
    	    ELSE
	    	MEME_UTILITY.put_message(
		  LPAD('(' || ct || ' concepts affected)',43,'. '));
	    END IF;

	    --
	    -- Update editing_matrix, tmp_editing_matrix
	    -- matrix_result_table has all current violations
	    --
   	    location :='240';
	    err_msg := 'Error updating editing_matrix to add vector.';

	    --
  	    -- Add <ic_name:ic_state> to vector if missing
	    --
	    local_exec (
	     	'UPDATE editing_matrix e
	    	SET integrity_vector = integrity_vector ||
		      ''<' || ic_name || ':' || ic_state || '>''
	    	WHERE integrity_vector not like ''%' || ic_name || '%''
	    	AND e.concept_id IN 
	    	(SELECT concept_id FROM ' || matrix_result_table || ')'
	    );

  	    --
  	    -- Update tmp_editing_matrix
  	    --
    	    location :='250';
	    err_msg := 'Error updating tmp_editing_matrix to add vector.';
	    local_exec (
	       	'UPDATE ' || tmp_editing_matrix || ' e
	    	 SET integrity_vector=integrity_vector ||
		    ''<' || ic_name || ':' || ic_state || '>''
	    	 WHERE e.concept_id in
	    	   (SELECT concept_id FROM ' || matrix_result_table || ')'
 	    );

  	    --
	    -- There is no need to remove potential cases from editing_matrix
	    -- Because the make_subset procedure ensures that all OLD cases
	    -- which may have gone away are taken care of because they are 
  	    -- tracked.  In meme_cluster_history along with changed concepts
            -- and so are picked up.
  	    --
   	    location :='260';
	    err_msg := 'Error dropping ' || matrix_result_table;
	    MEME_UTILITY.drop_it('table', matrix_result_table);

  	    --
	    -- Log Progress
  	    --
   	    location := '270';
    	    MEME_UTILITY.sub_timing_stop;
    	    err_msg := 'Error logging progress ic_procedure (' || 
		restrict_table || ').';
	    MEME_UTILITY.log_progress (
  	        MEME_CONSTANTS.MATRIX_AUTHORITY, 'matrix_updater',
       	        'MEME_INTEGRITY.matrix_updater::' || ic_procedure ||
	        '' || restrict_table || ')',
	        0, local_work_id, MEME_UTILITY.sub_elapsed_time);

        END IF;

	--
	-- Increment ic counter
	--
	i := i + 1;

    END LOOP;

    --
    -- Set integrity status
    --
    location := '300';
    err_msg := 'Error updating tmp_editing_matrix.integrity_status.';
    local_exec(
	'UPDATE ' || tmp_editing_matrix || '
	 SET integrity_status = ''N''
	 WHERE integrity_vector LIKE ''%:V>%'' '
	);

    --
    -- Calls Actions_Initializer
    -- Currently the inverse sets (things which lost violations but were not
    -- directly in the set) do not have their status updated because the
    -- checks which produce these sets typically are run with a W ic_code
    --
    MEME_UTILITY.PUT_MESSAGE(LPAD('Actions_Initializer',45,'. '));
    location := '300';
    err_msg := 'Error calling actions_initializer ('|| tmp_editing_matrix || ')';
    actions_initializer(tmp_editing_matrix,local_work_id);
    MEME_UTILITY.PUT_MESSAGE(LPAD('Reindex editing_matrix',45,'. '));

    --
    -- Add tmp_editing_matrix rows to editing_matrix
    --
    location := '310';
    err_msg := 'Error moving tmp_editing_matrix rows to editing_matrix.';
    local_exec (
	'INSERT INTO editing_matrix SELECT * from ' || tmp_editing_matrix
	);

    --
    -- Update MATRIX_INITIALIZER row in max_tab,
    --
    location := '330';
    err_msg := 'Error calling finalize_initialization.';
    finalize_initialization (work_id => local_work_id );

    --
    -- Drop all tmp tables
    --
    location := '340';
    err_msg := 'Error dropping tmp tables.';
    MEME_UTILITY.DROP_IT('table',empty_concept);
    MEME_UTILITY.DROP_IT('table',restrict_table);
    MEME_UTILITY.DROP_IT('table',tmp_editing_matrix);

    location := '345';
    err_msg := 'Error resetting progress (' || local_work_id || ').';
    MEME_UTILITY.reset_progress(local_work_id);

    --
    -- Log Operation
    --
    elapsed_time := ((DBMS_UTILITY.get_time - start_time)*10);
    location := '350';
    MEME_UTILITY.log_operation(
	authority => MEME_CONSTANTS.MATRIX_AUTHORITY,
	activity => 'matrix_updater',
	detail => 'Done updating matrix (' || iv || ')',
	transaction_id => 0, 
	work_id => local_work_id, 
	elapsed_time => elapsed_time);

    MEME_UTILITY.PUT_MESSAGE(LPAD('Matrix Updater Done',45,'. '));
    RETURN local_work_id;

EXCEPTION
    WHEN OTHERS THEN

	MEME_UTILITY.drop_it('table',restrict_table);
	MEME_UTILITY.drop_it('table',empty_concept);
	MEME_UTILITY.reset_progress(local_work_id);
	meme_integrity_error ('matrix_updater',location,1,
		err_msg || '(' || SQLERRM || ')' );
	RAISE meme_integrity_exc;

END matrix_updater;


/* FUNCTION make_subset ********************************************************
 * updater utility
 */
FUNCTION make_subset(
	run_mode 	IN VARCHAR2 := MI_CATCHUP,
	table_name 	IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
	timestamp 	IN DATE := NULL
)
RETURN VARCHAR2
IS
    molecule_id		INTEGER;
    restrict_table	VARCHAR2(50);

BEGIN

    restrict_table := MEME_UTILITY.get_unique_tablename;
    IF run_mode = MI_TABLE THEN
	location := '10';
	local_exec(
	    'CREATE TABLE ' || restrict_table || ' AS
	     SELECT DISTINCT concept_id FROM ' || table_name
	);
 	RETURN restrict_table;

    ELSIF run_mode = MI_TIMESTAMP then
	MEME_UTILITY.put_message(
	    'Get list of concepts touched since ' ||
	    timestamp || '... ' || SYSDATE );
	location := '20';
	local_exec(
	    'CREATE TABLE ' || restrict_table || '_2 AS
	     SELECT table_name, row_id FROM atomic_actions
	     WHERE timestamp > ''' || timestamp || '''
	       AND authority != ''' || MEME_CONSTANTS.MATRIX_AUTHORITY || ''' '
	);

    ELSIF run_mode = MI_CATCHUP THEN
	location := '30';
	molecule_id := MEME_UTILITY.exec_select (
	    'SELECT max_id FROM max_tab WHERE table_name = ''' ||
	    MEME_CONSTANTS.IV_MATRIX || ''' '
	);
	location := '40';
	local_exec (
	    'CREATE TABLE ' || restrict_table || '_2 AS
	     SELECT table_name, row_id FROM atomic_actions
	     WHERE molecule_id > ' || molecule_id || '
	       AND authority != ''' || MEME_CONSTANTS.MATRIX_AUTHORITY || ''' '
	);

    END IF;

    --
    -- For MI_CATCHUP and MI_TIMESTAMP modes, get concepts.
    --
    location := '50';
    local_exec (
	'CREATE TABLE ' || restrict_table || '_3 AS
	 SELECT concept_id FROM concept_status 
	 WHERE concept_id IN 
	  (SELECT row_id FROM ' || restrict_table || '_2
	   WHERE table_name=''CS'')
	 UNION
	 SELECT concept_id FROM dead_concept_status
	 WHERE concept_id IN
	  (SELECT row_id FROM ' || restrict_table || '_2
	   WHERE table_name=''CS'') '
	);

    location := '60';
    local_exec (
	'INSERT INTO ' || restrict_table || '_3
	SELECT concept_id FROM classes
	 WHERE atom_id IN
	  (SELECT row_id FROM ' || restrict_table || '_2 
	   WHERE table_name=''C'')
	 UNION
	 SELECT concept_id FROM dead_classes
	 WHERE atom_id IN
	  (SELECT row_id FROM ' || restrict_table || '_2
	   WHERE table_name=''C'') '
	);

    location := '70';
    local_exec (
	'INSERT INTO ' || restrict_table || '_3
	 SELECT concept_id FROM attributes
	 WHERE attribute_id IN
	  (SELECT row_id FROM ' || restrict_table || '_2
	   WHERE table_name=''A'')
	 UNION
	 SELECT concept_id FROM dead_attributes
	 WHERE attribute_id IN
	  (SELECT row_id FROM ' || restrict_table || '_2
	   WHERE table_name=''A'') '
	);

    location := '80';
    local_exec (
	'INSERT INTO ' || restrict_table || '_3 
	 SELECT concept_id_1 FROM relationships 
	 WHERE relationship_id IN
	  (SELECT row_id FROM ' || restrict_table || '_2 
	   WHERE table_name=''R'') 
	 UNION 
	 SELECT concept_id_1 FROM dead_relationships 
	 WHERE relationship_id IN 
	  (SELECT row_id FROM ' || restrict_table || '_2 
	   WHERE table_name=''R'') 
	 UNION 
	 SELECT concept_id_2 FROM relationships 
	 WHERE relationship_id IN 
	  (SELECT row_id FROM ' || restrict_table || '_2 
	   WHERE table_name=''R'') 
	 UNION 
	 SELECT concept_id_2 FROM dead_relationships 
	 WHERE relationship_id IN 
	  (SELECT row_id FROM ' || restrict_table || '_2 
	   WHERE table_name=''R'') '
	);


    IF run_mode = MI_TIMESTAMP THEN

	location := '100';
	local_exec (
	    'INSERT INTO ' || restrict_table || '_3
	     SELECT source_id FROM molecular_actions
	     WHERE timestamp > ''' || timestamp || '''
 	       AND authority != ''' || MEME_CONSTANTS.MATRIX_AUTHORITY || ''' '
	);

	location := '110';
	local_exec (
	    'INSERT INTO ' || restrict_table || '_3
	     SELECT target_id FROM molecular_actions
	     WHERE timestamp > ''' || timestamp || ''' AND target_id != 0
 	       AND authority != ''' || MEME_CONSTANTS.MATRIX_AUTHORITY || ''' '
	);



    ELSIF run_mode = MI_CATCHUP THEN

	location := '120';
	local_exec (
	    'INSERT INTO ' || restrict_table || '_3
	     SELECT source_id FROM molecular_actions
	     WHERE molecule_id > ' || molecule_id || '
 	       AND authority != ''' || MEME_CONSTANTS.MATRIX_AUTHORITY || ''' '
	);

	location := '130';
	local_exec (
	    'INSERT INTO ' || restrict_table || '_3
	     SELECT target_id FROM molecular_actions
	     WHERE molecule_id > ' || molecule_id || '
 	       AND authority != ''' || MEME_CONSTANTS.MATRIX_AUTHORITY || ''' '
	);

    END IF;

    --
    -- To the restrict table, we have to add any concepts
    -- clustered with those in the restrict_table in meme_cluster_history
    -- which are not already in the restrict_table
    --
    location := '140';
    local_exec (
	'INSERT INTO ' || restrict_table || '_3
 	 SELECT concept_id_2 as concept_id FROM meme_cluster_history
	 WHERE concept_id_1 IN (SELECT concept_id FROM ' ||
	 restrict_table || '_3)
	 UNION
	 SELECT concept_id_1 as concept_id FROM meme_cluster_history
	 WHERE concept_id_2 IN (SELECT concept_id FROM ' ||
	 restrict_table || '_3)'
	);

    location := '150';
    local_exec (
	'CREATE TABLE ' || restrict_table || ' AS
	 SELECT DISTINCT concept_id FROM ' || restrict_table || '_3'
	);

    location := '160';
    MEME_UTILITY.drop_it('table',restrict_table || '_2');
    MEME_UTILITY.drop_it('table',restrict_table || '_3');

    MEME_SYSTEM.analyze(restrict_table);
    RETURN restrict_table;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',restrict_table);
 	MEME_UTILITY.drop_it('table',restrict_table || '_2');
	MEME_UTILITY.drop_it('table',restrict_table || '_3');
	meme_integrity_error('make_subset',location,1,SQLERRM);
	RAISE meme_integrity_exc;

END make_subset;

/* PROCEDURE actions_initializer ***********************************************
 * Set concept status.
 */
PROCEDURE actions_initializer (
	table_name 	IN VARCHAR2 := 'editing_matrix',
	work_id 	IN INTEGER := 0
)
IS

    t_concept_status	VARCHAR2(50);
    ct			INTEGER;
    transaction_id	INTEGER;

BEGIN

    t_concept_status := MEME_UTILITY.get_unique_tablename;
    location := '10';

    --
    -- Get status 'R' concepts
    --
    local_exec(
	'CREATE TABLE ' || t_concept_status || '_r as
	 SELECT cs.concept_id as row_id
	 FROM concept_status cs, ' || table_name || ' e
	 WHERE classes_status in (''R'')
	   AND integrity_status = ''R''
	   AND relationships_status not in (''D'',''N'')
	   AND attributes_status != ''N''
	   AND e.concept_id=cs.concept_id
	   AND cs.status != e.classes_status'
	);

    --
    -- deal with status 'N' concepts
    --
    location := '20';
    local_exec(
	'CREATE TABLE ' || t_concept_status || '_n as
    	 SELECT cs.concept_id as row_id
	 FROM concept_Status cs, ' || table_name || ' e 
	 WHERE (classes_status= ''N'' 
    	 	OR integrity_status = ''N''
	 	OR attributes_status = ''N''
	 	OR relationships_status in (''D'',''N'')) 
	   AND classes_status != ''U''
	   AND e.concept_id=cs.concept_id 
	   AND cs.status != ''N'''
	);

    MEME_UTILITY.put_message(LPAD('Calling BATCH_ACTIONS',45,'. '));

    location := '40';
    ct := MEME_UTILITY.exec_count(t_concept_status || '_r');
    IF ct > 0 THEN
	MEME_UTILITY.PUT_MESSAGE(LPAD('Change concept_status.status => R',43,'. '));
    	location := '50';
	transaction_id := MEME_BATCH_ACTIONS.batch_action (
		action => 'MOLECULAR_CHANGE_STATUS',
		id_type => 'CS',
		authority => MEME_CONSTANTS.MATRIX_AUTHORITY,
		table_name => t_concept_status||'_R',
		work_id => work_id,
		status => 'R',
		new_value => 'R',
		action_field => 'status' );
	IF transaction_id < 0 THEN
	    error_code := 40;
   	    RAISE meme_integrity_exc;
	END IF; 
    ELSE
	MEME_UTILITY.put_message(LPAD('0 Rows Processed (R)',43,'. '));
    END IF;

    location := '80';
    ct := MEME_UTILITY.exec_count(t_concept_status || '_n');
    IF ct > 0 THEN

	MEME_UTILITY.PUT_MESSAGE(LPAD('Change concept_status.status => N',43,'. '));
    	location := '90';
	transaction_id := MEME_BATCH_ACTIONS.batch_action (
		action => 'MOLECULAR_CHANGE_STATUS',
		id_type => 'CS',
		authority => MEME_CONSTANTS.MATRIX_AUTHORITY,
		table_name => t_concept_status||'_N',
		work_id => work_id,
		status => 'R',
		new_value => 'N',
		action_field => 'status' );
	IF transaction_id < 0 THEN
	    error_code := 40;
   	    RAISE meme_integrity_exc;
	END IF; 
    ELSE
	MEME_UTILITY.put_message(LPAD('0 Rows Processed (N)',43,'. '));
    END IF;

    location := '100';
    MEME_UTILITY.drop_it('table',t_concept_status||'_R');
    MEME_UTILITY.drop_it('table',t_concept_status||'_N');

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t_concept_status||'_R');
	MEME_UTILITY.drop_it('table',t_concept_status||'_N');
	meme_integrity_error('actions_initializer',location,
			     error_code,SQLERRM);
	RAISE meme_integrity_exc;

END actions_initializer;

/* FUNCTION violations_initializer *********************************************
 * NOT USED
 */
FUNCTION violations_initializer (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE
)
RETURN INTEGER
IS
BEGIN

    return MEME_CONSTANTS.ER_OK;

END violations_initializer;

/*******************************************************************************
*
*  INTEGRITY_SNAPSHOT
*
* Following is code to implement integrity_snapshot functionality.
* It includes the standard pre and post procedures and a diff. The API
* for this is as follows:
*
* 1. To add a new check
*    a. Write a procedure called 'snapshot_<ic_name>'
*    b. Add this check to the INTEGRITY_SNAPSHOT vector in ic_applications
*    c. snapshot_ic_name takes SN_PRE,SN_POST, or SN_DIFF.
*	i. For SN_PRE,SN_POST: calculate the set create <pre/post>_<ic_name>
*  	ii. For SN_DIFF, diff the two add results to snapshot_results
*
* 2. To call the integrity_snapshot use
*    MEME_INTEGRITY.snapshot_pre
*    MEME_INTEGRITY.snapshot_post
*    MEME_INTEGRITY.snapshot_diff
*
*    The ic_applications table has an INTEGRITY_SNAPSHOT row which
*    holds all of the MGV_* checks that will be run.
*
*******************************************************************************/

/* PROCEDURE snapshot_pre ******************************************************
 */
PROCEDURE snapshot_pre
IS
BEGIN
    calculate_snapshot(MEME_CONSTANTS.SN_PRE);
END snapshot_pre;

/* PROCEDURE snapshot_post *****************************************************
 */
PROCEDURE snapshot_post
IS
BEGIN
    calculate_snapshot(MEME_CONSTANTS.SN_POST);
END snapshot_post;

/* PROCEDURE snapshot_diff *****************************************************
 */
PROCEDURE snapshot_diff
IS
BEGIN

    location := '0';
    err_msg := 'Error truncating snapshot_results table.';
    MEME_SYSTEM.truncate('snapshot_results');

    location := '10';
    calculate_snapshot(MEME_CONSTANTS.SN_DIFF);

EXCEPTION

    WHEN OTHERS THEN
	meme_integrity_error ('snapshot_diff',location,0,err_msg);
  	RAISE meme_integrity_exc;

END snapshot_diff;

/* PROCEDURE calculate_snapshot ************************************************
 */
PROCEDURE calculate_snapshot (pre_post_diff IN VARCHAR2)
IS
    location 		VARCHAR2(5);
    err_msg		VARCHAR2(256);
    ic_name		VARCHAR2(10);
    ic_code		VARCHAR2(10);
    ic_state		VARCHAR2(10);
    ic_procedure	VARCHAR2(50);
    iv			VARCHAR2(2000);
    result_table	VARCHAR2(50);
    work_id		INTEGER;
    i			INTEGER;
BEGIN

    location := '0';
    MEME_UTILITY.timing_start;

    -- Like the Matrix Inializer, the snapshot uses
    -- a vector to determine which checks to explore.
    -- It uses the INTEGRITY_SNAPSHOT vector in ic_applications
    location := '10';
    err_msg := 'Error fetching integrity vector from ic_applications.';
    iv := MEME_UTILITY.get_integrity_vector(MEME_CONSTANTS.IV_SNAPSHOT);

    location := '20';
    err_msg := 'Error getting new work_id.';
    work_id := MEME_UTILITY.new_work(
	MEME_CONSTANTS.SNAPSHOT_AUTHORITY, MEME_CONSTANTS.MW_SNAPSHOT,
	'The snapshot is being run in ' || pre_post_diff ||
	' mode, with this vector: ''' || iv || '''. ');

    -- Log start
    location := '80';
    err_msg := 'Error logging progress.';
    MEME_UTILITY.log_progress (
	MEME_CONSTANTS.SNAPSHOT_AUTHORITY, 'calculate_snapshot_' || pre_post_diff,
	'Starting MEME_INTEGRITY.calculate_snapshot',
	0, work_id, 0);

    i := 1;
    LOOP
	-- Start timing procedure call
	MEME_UTILITY.sub_timing_start;

    	location := '30';
	err_msg := 'Error parsing integrity vector.';
	-- Parse vector to get individual checks and codes
	ic_name := MEME_UTILITY.GET_IC_NAME_BY_NUM(iv,i);
	ic_code := MEME_UTILITY.GET_IC_CODE_BY_NUM(iv,i);
	ic_state := MEME_UTILITY.GET_IC_STATE(ic_code);
	EXIT WHEN ic_name IS NULL;


	-- Get procedure matching current ic_code;
	location := '40';
	err_msg := 'Error getting snapshot procedure to match ic_name (' || ic_name || ')';
	ic_procedure := 'MEME_SNAPSHOT_PROC.' ||
			MEME_UTILITY.get_value_by_code(
				upper(ic_name),
				'snapshot_procedure');

	-- Print procedure name
	MEME_UTILITY.put_message(LPAD(ic_procedure,45,'. '));

	-- Dynamically execute procedure name,
	location := '60';
	result_table := MEME_UTILITY.get_unique_tablename;

	-- Dynamically call procedure.	No need to worry about return
	-- values here.
	location := '70';
	err_msg := 'Error dynamically calling ' || ic_procedure;
	local_exec (
	    'BEGIN ' || ic_procedure ||
	    '( pre_post_diff => ''' || pre_post_diff ||
	     ''', work_id => ' || work_id || '); END;'
	);

	-- Stop Timing and Log Progress
	MEME_UTILITY.sub_timing_stop;
	location := '80';
	err_msg := 'Error logging progress (' || ic_procedure || ').';
	MEME_UTILITY.log_progress (
	 	MEME_CONSTANTS.SNAPSHOT_AUTHORITY, 'calculate_snapshot_' || pre_post_diff,
		'MEME_INTEGRITY.calculate_snapshot::' || ic_procedure,
		0, work_id, MEME_UTILITY.sub_elapsed_time);

	-- Increment ic counter
	i := i + 1;

    END LOOP;

    -- Clear Progress log
    location := '90';
    err_msg := 'Error resetting progress (' || work_id || ').';
    MEME_UTILITY.reset_progress (work_id);

    MEME_UTILITY.timing_stop;

    -- Log operation
    location := '100';
    err_msg := 'Error logging operation.';
    MEME_UTILITY.log_operation(
	authority => MEME_CONSTANTS.SNAPSHOT_AUTHORITY,
	activity => 'Calculate snapshot',
 	detail => 'MEME_INTEGRITY.calculate_snapshot (' ||
		  pre_post_diff || ').  Integrity vector is ' || iv,
	transaction_id =>0, 
	work_id => work_id, 
	elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
	meme_integrity_error (
	    'calculate_snapshot',location,1,err_msg);
	RAISE meme_integrity_exc;

END calculate_snapshot;

/* PROCEDURE monster_qa_help ***************************************************
 * Used by "monster_qa", "src_monster_qa"
 */
PROCEDURE monster_qa_help(
   tbl_qa_queries	 IN VARCHAR2,
   qa_id		 IN INTEGER
)
IS
   qa_count		 INTEGER;
   qa_value		 VARCHAR2(100);
   qa_timestamp 	 DATE := SYSDATE;

   separator_count	 INTEGER;
   separator_block	 INTEGER;
   instr_occurrence	 INTEGER;
   next_position	 INTEGER;
   qry_mapped		 VARCHAR2(1000);
   tbl_qa_results	 VARCHAR2(50);

   TYPE mqa_rec_type IS RECORD(
      name		 VARCHAR2(100),
      query		 VARCHAR2(1000));
   mqa_record mqa_rec_type;

   TYPE mqa_type IS REF CURSOR;
   mqa_cursor mqa_type;

   TYPE qry_type IS REF CURSOR;
   qry_cursor qry_type;

BEGIN

   --
   -- Get queries to run
   --
   location := '10';
   OPEN mqa_cursor FOR 'SELECT * FROM '||tbl_qa_queries||' ORDER BY 1';
   LOOP

      FETCH mqa_cursor INTO mqa_record;
      EXIT WHEN mqa_cursor%NOTFOUND;

	--
	-- separator_count is an indicator that determine whether the
	--   current query will retrieve one or more than one column.
	--
	separator_count := INSTR(mqa_record.query,',');

	--
	-- separator_block is an indicator for blocking the search
	-- for a separator.
	--
	separator_block := INSTR(LOWER(mqa_record.query),'from');

        --
	-- Map qry_mapped
        --
	location := '20';
	IF separator_count > 0 THEN

	    --
	    -- Inside this block, the separator_count is the current
	    -- position of the separator.
	    --

	    --
	    -- Map up to the first separator.
	    --
	    location := '30';
	    qry_mapped := SUBSTR(mqa_record.query,1,separator_count);

	    instr_occurrence := 1;
	    LOOP
	       EXIT WHEN separator_count > separator_block;

	       location := '40';
	       instr_occurrence := instr_occurrence+1;
	       next_position := INSTR(mqa_record.query,',',1,instr_occurrence);

	       --
	       -- Map up to the next separator.
	       --
	       qry_mapped := qry_mapped||SUBSTR(mqa_record.query,
		  separator_count+1,next_position-separator_count-1);

	       separator_count := next_position;

	       --
	       -- Pad with separator.
	       --
	       IF separator_block > separator_count THEN
		  qry_mapped := qry_mapped||'||'',''||';
	       ELSE
		  qry_mapped := qry_mapped||',';
	       END IF;
	    END LOOP;

	    --
	    -- Map the rest of the query.
	    --
	    qry_mapped := qry_mapped||SUBSTR(mqa_record.query,separator_count+1);
	ELSE
	    location := '50';
	    qry_mapped :=
		SUBSTR(mqa_record.query,1,15)||','||''''''||SUBSTR(mqa_record.query,16);
	END IF;

	-- 
	-- Execute qry_mapped.
	-- 
	location := '70';
	OPEN qry_cursor FOR qry_mapped;
	LOOP
	    location := '80';
	    FETCH qry_cursor INTO qa_count, qa_value;
	    EXIT WHEN qry_cursor%NOTFOUND;

	    location := '90';
	    tbl_qa_results := SUBSTR(tbl_qa_queries,1,3)||'_qa_results';

	    location := '100';
	    EXECUTE IMMEDIATE
		'INSERT INTO ' || tbl_qa_results || '
	          (qa_id, name, value, qa_count, timestamp)
	 	 VALUES (:qa_id, :name, :value, :qa_count, :timestamp)'
  	    USING
	      qa_id, mqa_record.name, qa_value, qa_count, qa_timestamp;

	END LOOP;
	CLOSE qry_cursor;
    END LOOP;
    CLOSE mqa_cursor;

    COMMIT;

EXCEPTION
   WHEN OTHERS THEN
      meme_integrity_error('monster_qa',location,1,
	 ' Table name: '||tbl_qa_queries||
	 ' Query name: '||mqa_record.name||
	 ' SQLERRM: '||SQLERRM);
      RAISE meme_integrity_exc;

END monster_qa_help;

/* PROCEDURE monster_qa ********************************************************
 * This procedure moves prior mid_qa_results to mid_qa_history.  Performs new
 * execution of monster QA and generates new mid_qa_results.
 */
PROCEDURE monster_qa
IS
   qa_id		 INTEGER;
BEGIN

    --
    -- History records
    --
    INSERT INTO mid_qa_history SELECT * FROM mid_qa_results;

    --
    -- Clear mid_qa_results
    --
    MEME_SYSTEM.truncate('mid_qa_results');

    --
    -- Get next QA id
    --
    qa_id := MEME_UTILITY.get_next_id('QA_SETS');

    --
    -- Perform monster_qa
    --
    monster_qa_help('mid_qa_queries', qa_id);

END monster_qa;

/* PROCEDURE src_monster_qa ****************************************************
 * This procedure performs execution of source monster QA and insert results
 * to src_qa_results.
 */
PROCEDURE src_monster_qa
IS
   qa_id		 INTEGER;
   l_source		VARCHAR2(100);
   CURSOR old_source_cur IS
   SELECT previous_name FROM source_version 
   WHERE current_name IN 
	(SELECT high_source FROM source_source_rank)
     AND current_name not in ('SRC')
     aND previous_name IS NOT NULL;

BEGIN

    --
    -- Clean up entries with old SAB values
    --
    location := '05';
    OPEN old_source_cur;
    LOOP
    	location := '10';
	FETCH old_source_cur INTO l_source;
	EXIT WHEN old_source_cur%NOTFOUND;

    	location := '20a';
	INSERT INTO src_obsolete_qa_results
	(qa_id, name, value ,qa_count, timestamp)
	SELECT qa_id, name, value, qa_count, timestamp
	FROM src_qa_results
        WHERE value like l_source || ',%';

    	location := '20b';
	DELETE FROM src_qa_results
        WHERE value like l_source||',%';

    END LOOP;

    COMMIT;

    --
    -- Get the same qa_id
    --
    SELECT NVL(MIN(qa_id),0) INTO qa_id FROM src_qa_results;

    --
    -- Perform src_monster_qa
    --
    monster_qa_help('src_qa_queries', qa_id);

END src_monster_qa;

/* PROCEDURE src_mid_qa_diff ***************************************************
 * This procedure compare mid_qa_results to src_qa_results, and record results
 * into qa_diff_results.
 */
PROCEDURE src_mid_qa_diff
IS
BEGIN

    location := '05';
    MEME_SYSTEM.truncate('qa_diff_results');

    --
    -- Handle the 2 cases
    --
    -- 1. src != mid
    -- 2. src not mid
    --
    location := '10';
    INSERT INTO qa_diff_results
      (name, value, qa_id_1, count_1, qa_id_2, count_2, timestamp)
    SELECT DISTINCT
 	mid.name, mid.value, src.qa_id, 
        (src.qa_count+nvl(adj.qa_count,0)) count_1, 
	mid.qa_id, mid.qa_count count_2, sysdate
    FROM 
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM src_qa_results a
       GROUP BY qa_id, name, value) src,
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM qa_adjustment
       GROUP BY qa_id, name, value) adj,
      (SELECT qa_id, name, value,
		sum(qa_count) qa_count
       FROM mid_qa_results a
       GROUP BY qa_id, name, value) mid
    WHERE src.qa_id = adj.qa_id (+)
      AND src.name = adj.name (+)
      AND src.value = adj.value (+)
      AND src.name = mid.name
      AND src.value = mid.value
      AND (src.qa_count+nvl(adj.qa_count,0))-mid.qa_count != 0
    UNION ALL
    SELECT DISTINCT 
	src.name, src.value,
	src.qa_id, src.qa_count + NVL(adj.qa_count,0) count_1, 
	src.qa_id, 0 count_2, sysdate
    FROM 
      (SELECT qa_id, name, value,
		sum(qa_count) qa_count
       FROM src_qa_results a
       GROUP BY qa_id, name, value) src,
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM qa_adjustment
       GROUP BY qa_id, name, value) adj
    WHERE src.qa_id = adj.qa_id (+)
      AND src.name = adj.name (+)
      AND src.value = adj.value (+)
      AND src.qa_count+NVL(adj.qa_count,0) != 0
      AND (src.name, src.value) IN
    (SELECT name, value
     FROM src_qa_results
     MINUS
     SELECT name, value
     FROM mid_qa_results);

    COMMIT;

EXCEPTION
   WHEN OTHERS THEN
      meme_integrity_error('src_mid_qa_diff',location,1,SQLERRM);
      RAISE meme_integrity_exc;
END src_mid_qa_diff;

/* PROCEDURE src_obsolete_qa_diff ******************************************
 * 
 * Compares src_qa_results to src_obsolete_qa_results
 * 1. The src_qa_results will have current version data
 *     e.g. CPT2005,CPT2005,S,PAR,
 * 2. The src_obsolete_qa_results will have previous version data
 *     qa_id is negative
 *     e.g. CPT2004,CPT2004,S,PAR,
 * 3. The qa_adjustment table will have previous version data
 *     qa_id is negative
 *     e.g. CPT2004,CPT2004,S,PAR,
 *
 */
PROCEDURE src_obsolete_qa_diff
IS

    -- Count from the src_qa_results table
    src_count		 INTEGER := 0;

BEGIN
    --
    -- Truncate the reports table
    --
    location := '05';
    MEME_SYSTEM.truncate('qa_diff_results');

    --
    -- Handle the 3 cases
    --
    -- 1. obs != cur
    -- 2. obs not cur
    -- 3. cur not obs
    INSERT INTO qa_diff_results
      (name, value, qa_id_1, count_1, qa_id_2, count_2, timestamp)
    SELECT DISTINCT
 	obs.name, obs.value, obs.qa_id, 
        (obs.qa_count+nvl(adj.qa_count,0)) count_1, 
	src.qa_id, src.qa_count count_2, sysdate
    FROM 
      (SELECT qa_id, name, REPLACE(value,current_name,previous_name) value,
		sum(qa_count) qa_count
       FROM src_qa_results, source_version
       WHERE value like current_name || ',%'
         AND current_name IS NOT NULL
         AND previous_name IS NOT NULL
       GROUP BY qa_id, name, value, current_name, previous_name) src,
      (SELECT qa_id_1, name, value, sum(qa_count) qa_count
       FROM qa_diff_adjustment
       GROUP BY qa_id_1, name, value) adj, 
      (SELECT qa_id, name, value,
		sum(qa_count) qa_count
       FROM src_obsolete_qa_results, source_version
       WHERE value like previous_name || ',%'
         AND current_name IS NOT NULL
         AND previous_name IS NOT NULL
       GROUP BY qa_id, name, value) obs
    WHERE obs.qa_id = adj.qa_id_1 (+)
      AND obs.name = adj.name (+)
      AND obs.value = adj.value (+)
      AND src.qa_id = obs.qa_id
      AND src.name = obs.name
      AND src.value = obs.value
      AND (src.qa_count+nvl(adj.qa_count,0))-obs.qa_count != 0
    UNION ALL
    SELECT DISTINCT 
	obs.name, obs.value, obs.qa_id, 
 	obs.qa_count + NVL(adj.qa_count,0) count_1,
	obs.qa_id, 0 count_2, sysdate
    FROM 
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM src_obsolete_qa_results, source_version
       WHERE value like previous_name || ',%'
         AND current_name IS NOT NULL
         AND previous_name IS NOT NULL
       GROUP BY qa_id, name, value) obs, 
      (SELECT qa_id_1, name, value, sum(qa_count) qa_count
       FROM qa_diff_adjustment
       GROUP BY qa_id_1, name, value) adj
    WHERE obs.qa_id = qa_id_1 (+)
      AND obs.name = adj.name (+)
      AND obs.value = adj.value (+)
      AND obs.qa_count + NVL(adj.qa_count,0) != 0
      AND (obs.qa_id, obs.name, obs.value) IN
    (SELECT qa_id, name, value
     FROM src_obsolete_qa_results
     MINUS
     SELECT qa_id, name, REPLACE(value,current_name,previous_name)
     FROM src_qa_results, source_version
     WHERE value like current_name || ',%'
       AND current_name IS NOT NULL
       AND previous_name IS NOT NULL)
    UNION ALL
    SELECT DISTINCT 
	src.name, src.value,
	src.qa_id, 0 count_1, 
	src.qa_id, src.qa_count - NVL(adj.qa_count,0) count_2, sysdate
    FROM 
      (SELECT qa_id, name, REPLACE(value,current_name,previous_name) value,
		sum(qa_count) qa_count
       FROM src_qa_results, source_version
       WHERE value like current_name || ',%'
         AND current_name IS NOT NULL
         AND previous_name IS NOT NULL
       GROUP BY qa_id, name, value, current_name, previous_name) src,
      (SELECT qa_id_1, name, value, sum(qa_count) qa_count
       FROM qa_diff_adjustment
       GROUP BY qa_id_1, name, value) adj
    WHERE src.qa_id = qa_id_1 (+)
      AND src.name = adj.name (+)
      AND src.value = adj.value (+)
      AND src.qa_count - NVL(adj.qa_count,0) != 0
      AND (src.qa_id, src.name, src.value) IN
    (SELECT DISTINCT qa_id, name, REPLACE(value,current_name,previous_name)
     FROM src_qa_results, source_version
     WHERE value like current_name || ',%'
       AND current_name IS NOT NULL
       AND previous_name IS NOT NULL
     MINUS
     SELECT qa_id, name, value
     FROM src_obsolete_qa_results);

    COMMIT;

EXCEPTION
   WHEN OTHERS THEN
      meme_integrity_error('src_obsolete_qa_diff',location,1,SQLERRM);
      RAISE meme_integrity_exc;
END src_obsolete_qa_diff;

/* PROCEDURE mid_mid_qa_diff ***************************************************
 * This procedure will bi-directional compare mid_qa_results to mid_qa_history
 * and record results into qa_diff_results.
 */
PROCEDURE mid_mid_qa_diff(
    history_qa_id	 IN INTEGER)
IS
BEGIN
    --
    -- Truncate the reports table
    --
    location := '05';
    MEME_SYSTEM.truncate('qa_diff_results');

    --
    -- Handle the 3 cases
    --
    -- 1. cur != hist
    -- 2. cur not hist
    -- 3. cur not hist
    INSERT INTO qa_diff_results
      (name, value, qa_id_1, count_1, qa_id_2, count_2, timestamp)
    SELECT DISTINCT
 	hist.name, hist.value, mid.qa_id, 
        (mid.qa_count+nvl(adj.qa_count,0)) count_1, 
	hist.qa_id, hist.qa_count count_2, sysdate
    FROM 
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM mid_qa_results a
       GROUP BY qa_id, name, value) mid,
      (SELECT qa_id_1, name, value, sum(qa_count) qa_count
       FROM qa_diff_adjustment
       WHERE qa_id_2 = mid_mid_qa_diff.history_qa_id
       GROUP BY qa_id_1, name, value) adj, 
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM mid_qa_history
       WHERE qa_id = mid_mid_qa_diff.history_qa_id
       GROUP BY qa_id, name, value) hist
    WHERE mid.qa_id = adj.qa_id_1 (+)
      AND mid.name = adj.name (+)
      AND mid.value = adj.value (+)
      AND mid.name = hist.name
      AND mid.value = hist.value
      AND (mid.qa_count+nvl(adj.qa_count,0))-hist.qa_count != 0
    UNION ALL
    SELECT DISTINCT 
	mid.name, mid.value,
	mid.qa_id, mid.qa_count + NVL(adj.qa_count,0) count_1, 
	mid.qa_id, 0 count_2, sysdate
    FROM 
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM mid_qa_results
       GROUP BY qa_id, name, value) mid,
      (SELECT qa_id_1, name, value, sum(qa_count) qa_count
       FROM qa_diff_adjustment
       WHERE qa_id_2 = mid_mid_qa_diff.history_qa_id
       GROUP BY qa_id_1, name, value) adj
    WHERE mid.qa_id = qa_id_1 (+)
      AND mid.name = adj.name (+)
      AND mid.value = adj.value (+)
      AND mid.qa_count+NVL(adj.qa_count,0) != 0
      AND (mid.name, mid.value) IN
    (SELECT name, value
     FROM mid_qa_results a
     MINUS
     SELECT  name, value
     FROM mid_qa_history
     WHERE qa_id = mid_mid_qa_diff.history_qa_id)
    UNION ALL
    SELECT DISTINCT 
	hist.name, hist.value, hist.qa_id, 0 count_1,
	hist.qa_id, hist.qa_count - NVL(adj.qa_count,0) count_2, sysdate
    FROM 
      (SELECT qa_id, name, value, sum(qa_count) qa_count
       FROM mid_qa_history
       WHERE qa_id = mid_mid_qa_diff.history_qa_id
       GROUP BY qa_id, name, value) hist, 
      (SELECT qa_id_2, name, value, sum(qa_count) qa_count
       FROM qa_diff_adjustment
       WHERE qa_id_1 in (select qa_id from mid_qa_results)
       GROUP BY qa_id_2, name, value) adj
    WHERE hist.qa_id = qa_id_2 (+)
      AND hist.name = adj.name (+)
      AND hist.value = adj.value (+)
      AND hist.qa_count-NVL(adj.qa_count,0) != 0
      AND (hist.name, hist.value) IN
    (SELECT name, value
     FROM mid_qa_history
     WHERE qa_id = mid_mid_qa_diff.history_qa_id
     MINUS
     SELECT name, value
     FROM mid_qa_results);

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        meme_integrity_error('mid_mid_qa_diff',location,1,SQLERRM);
        RAISE meme_integrity_exc;
END mid_mid_qa_diff;

END MEME_INTEGRITY;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_INTEGRITY.help;
execute MEME_INTEGRITY.register_package;

