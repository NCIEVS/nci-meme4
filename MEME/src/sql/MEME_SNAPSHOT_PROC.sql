CREATE OR REPLACE PACKAGE MEME_SNAPSHOT_PROC AS

/*****************************************************************************
 *
 * PL/SQL File: MEME_SNAPSHOT_PROC.sql
 *
 * This package contains procedures
 * to perform MEME SNAPSHOT checkings
 *
 * Version Information
 *
 *  3/12/2000 3.1.0: MEME_INTEGRITY_PROC too big
 *		     This package created to hold snapshot procedures
 *  4/7/2000  3.1.1: Added work_id parameters to the procedures
 *  8/1/2000  3.2.0: Package handover version
 *
 *
 * Status:
 *	Functionality:
 *		not ready
 *	Testing:
 *		not tested
 * 	Enhancements:
 *
 *****************************************************************************/

 --
 -- Standard Package API
 --
    package_name	VARCHAR2(25) := 'MEME_SNAPSHOT_PROC';
    release_number	VARCHAR2(1) := '3';
    version_number	VARCHAR2(5) := '2.0';
    version_date	DATE := '01-Aug-2000';
    version_authority	VARCHAR2(3) := 'BAC';

    meme_snapshot_proc_debug BOOLEAN := FALSE;
    meme_snapshot_proc_trace BOOLEAN := TRUE;

    -- Useful generic package variables
    location		VARCHAR2(5);
    err_msg		VARCHAR2(256);
    error_code		INTEGER;
    retval		INTEGER;

    meme_SNAPSHOT_proc_exc  EXCEPTION;

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

    PROCEDURE meme_snapshot_proc_error(
	method		IN VARCHAR2,
	location	IN VARCHAR2,
	error_code	IN INTEGER,
	detail		IN VARCHAR2
    );

 --
 -- SNAPSHOT Snapshot procedures
 --
    PROCEDURE snapshot_mgv_a4 ( pre_post_diff IN VARCHAR2,
				work_id IN INTEGER := 0 );
    PROCEDURE snapshot_mgv_b ( pre_post_diff IN VARCHAR2,
				work_id IN INTEGER := 0 );
    PROCEDURE snapshot_mgv_b2 ( pre_post_diff IN VARCHAR2,
				work_id IN INTEGER := 0 );
    PROCEDURE snapshot_mgv_e ( pre_post_diff IN VARCHAR2,
				work_id IN INTEGER := 0 );
    PROCEDURE snapshot_mgv_f ( pre_post_diff IN VARCHAR2,
				work_id IN INTEGER := 0 );
    PROCEDURE snapshot_mgv_i ( pre_post_diff IN VARCHAR2,
				work_id IN INTEGER := 0 );
    PROCEDURE snapshot_mgv_k ( pre_post_diff IN VARCHAR2,
				work_id IN INTEGER := 0 );

END MEME_SNAPSHOT_PROC;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY MEME_SNAPSHOT_PROC AS

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

--************************* HELP **********************************
PROCEDURE help
IS
BEGIN
    help('');
END;
--************************* HELP **********************************
PROCEDURE help ( topic IN VARCHAR2 )
IS
BEGIN
    -- This procedure requires SET SERVEROUTPUT ON
	--    DBMS_OUTPUT.ENABLE(100000);

    -- Print version
    MEME_UTILITY.PUT_MESSAGE('. This package contains procedures used by MEME_SNAPSHOT.');
    MEME_UTILITY.PUT_MESSAGE('.');
    version;
END help;

--************************* REGISTER_PACKAGE ******************************
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_SNAPSHOT_PROC.release_number,
      MEME_SNAPSHOT_PROC.version_number,
      SYSDATE,
      MEME_SNAPSHOT_PROC.version_authority,
      MEME_SNAPSHOT_PROC.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

--************************* SET trace on **********************************
PROCEDURE set_trace_on
IS
BEGIN

    meme_snapshot_proc_trace := TRUE;

END set_trace_on;

--************************* SET trace off **********************************
PROCEDURE set_trace_off
IS
BEGIN

    meme_snapshot_proc_trace := FALSE;

END set_trace_off;

--************************* SET debug on **********************************
PROCEDURE set_debug_on
IS
BEGIN

    meme_snapshot_proc_debug := TRUE;

END set_debug_on;

--************************* SET debug off **********************************
PROCEDURE set_debug_off
IS
BEGIN

    meme_snapshot_proc_debug := FALSE;

END set_debug_off;

--************************* TRACE **********************************
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF meme_snapshot_proc_trace = TRUE THEN

	MEME_UTILITY.PUT_MESSAGE(message);

    END IF;

END trace;

--************************* local_exec **********************************
PROCEDURE local_exec ( query IN VARCHAR2 )
IS
BEGIN

    IF meme_snapshot_proc_trace = TRUE THEN
	MEME_UTILITY.put_message(query);
    END IF;

    IF meme_snapshot_proc_debug = FALSE THEN
	MEME_UTILITY.exec(query);
    END IF;

END local_exec;

--************************* local_exec **********************************
FUNCTION local_exec ( query IN VARCHAR2 )
RETURN INTEGER
IS
BEGIN

    IF meme_snapshot_proc_trace = TRUE THEN
	MEME_UTILITY.put_message(query);
    END IF;

    IF meme_snapshot_proc_debug = FALSE THEN
	return MEME_UTILITY.exec(query);
    END IF;

    RETURN 0;

END local_exec;

/********** meme_snapshot_proc_error **********/
PROCEDURE meme_snapshot_proc_error(
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

    MEME_UTILITY.PUT_ERROR('Error in MEME_SNAPSHOT_PROC.' ||
			   ' Method: ' || method ||
			   ' Location: ' || location  ||
			   ' Error Code: '|| error_msg ||
			   ' Detail: '|| detail);

END meme_snapshot_proc_error;


/*****************************************************************************
*
*  SNAPSHOT_SNAPSHOT
*
* Following is code to implement SNAPSHOT_snapshot procedures
*
*****************************************************************************/

/***********  Snapshot API for MGV_A4 ************/
PROCEDURE snapshot_mgv_a4 (
	pre_post_diff	IN VARCHAR2,
	work_id		IN INTEGER DEFAULT 0
)
IS
BEGIN

    location := '0';
    IF pre_post_diff = MEME_CONSTANTS.SN_PRE OR pre_post_diff = MEME_CONSTANTS.SN_POST THEN

    	location := '10';
    	MEME_UTILITY.drop_it ('table',pre_post_diff || '_mgv_a4');

    	location := '20';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_a4 AS ' ||
	    'SELECT concept_id, count(distinct last_release_cui) as ct ' ||
	    'FROM classes ' ||
	    'WHERE tobereleased in (''Y'',''y'') ' ||
	    '  AND last_release_cui is not null ' ||
	    'GROUP BY concept_id having count(distinct last_release_cui)>1'
	);

    ELSIF pre_post_diff = MEME_CONSTANTS.SN_DIFF THEN

    	location := '30';
    	MEME_UTILITY.drop_it ('table', MEME_CONSTANTS.SN_DIFF || '_mgv_a4');

    	location := '40';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_a4 AS ' ||
	    'SELECT * FROM post_mgv_a4 a ' ||
	    'WHERE not exists ' ||
	    ' (SELECT 1 FROM pre_mgv_a4 b ' ||
	    '  WHERE a.concept_id=b.concept_id AND a.ct=b.ct) '
	);

	location := '50';
	local_exec (
	    'INSERT INTO snapshot_results ' ||
	    ' (concept_id, ic_name, molecule_id, authority) ' ||
	    'SELECT distinct a.concept_id, ''mgv_a4'' as ic_name, ' ||
	    '	    m.molecule_id, m.authority ' ||
	    'FROM molecular_actions m, diff_mgv_a4 a ' ||
	    'WHERE m.molecular_action = ''MOLECULAR_MERGE'' ' ||
	    '  AND m.undone=''N'' AND a.concept_id=m.target_id'
	);

    ELSE
	meme_SNAPSHOT_proc_error('snapshot_mgv_a4',location,30,pre_post_diff);
	RAISE meme_SNAPSHOT_proc_exc;
    END IF;

END snapshot_mgv_a4;

/***********  Snapshot API for MGV_B ************/
PROCEDURE snapshot_mgv_b (
	pre_post_diff	IN VARCHAR2,
	work_id		IN INTEGER DEFAULT 0
)
IS
BEGIN

    location := '0';
    IF pre_post_diff = MEME_CONSTANTS.SN_PRE OR pre_post_diff = MEME_CONSTANTS.SN_POST THEN

    	location := '10';
    	MEME_UTILITY.drop_it ('table',pre_post_diff || '_mgv_b');

    	location := '20';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_b AS ' ||
	    'SELECT concept_id, source, count(*) as ct ' ||
	    'FROM classes ' ||
	    'WHERE source IN (SELECT value FROM ic_single ' ||
	    '		      WHERE ic_name = ''MGV_B'' ' ||
	    '		      AND type=''SOURCE'' ' ||
	    '		      AND negation=''N'') ' ||
	    '  AND tobereleased in (''Y'',''y'') ' ||
	    'GROUP by concept_id, source ' ||
	    'HAVING count(*)>1'
	);

    ELSIF pre_post_diff = MEME_CONSTANTS.SN_DIFF THEN

    	location := '30';


    ELSE
	meme_SNAPSHOT_proc_error('snapshot_mgv_b',location,30,pre_post_diff);
	RAISE meme_SNAPSHOT_proc_exc;
    END IF;

END snapshot_mgv_b;

/***********  Snapshot API for MGV_B2 ************/
PROCEDURE snapshot_mgv_b2 (
	pre_post_diff	IN VARCHAR2,
	work_id		IN INTEGER DEFAULT 0
)
IS
BEGIN

    location := '0';
    IF pre_post_diff = MEME_CONSTANTS.SN_PRE OR pre_post_diff = MEME_CONSTANTS.SN_POST THEN

    	location := '10';
    	MEME_UTILITY.drop_it ('table',pre_post_diff || '_mgv_b2');

    	location := '20';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_b2 AS ' ||
	    'SELECT a.concept_id, a.source as source1, ' ||
	    '	    b.source as source2, count(*) as ct ' ||
	    'FROM classes a, classes b, ic_pair c ' ||
	    'WHERE a.concept_id=b.concept_id ' ||
	    '  AND ic_name=''MGV_B2'' ' ||
	    '  AND a.source = c.value_1 AND c.type=''SOURCE'' ' ||
	    '  AND b.source = c.value_2 AND c.type=''SOURCE'' ' ||
	    '  AND 1.tobereleased in (''Y'',''y'') ' ||
	    '  AND b.tobereleased in (''Y'',''y'') ' ||
	    '  AND negation = ''N'' ' ||
	    'GROUP by a.concept_id, a.source, b.source '
	);

    ELSIF pre_post_diff = MEME_CONSTANTS.SN_DIFF THEN

    	location := '30';

    ELSE
	meme_SNAPSHOT_proc_error('snapshot_mgv_b2',location,30,pre_post_diff);
	RAISE meme_SNAPSHOT_proc_exc;
    END IF;

END snapshot_mgv_b2;

/***********  Snapshot API for MGV_E ************/
PROCEDURE snapshot_mgv_e (
	pre_post_diff	IN VARCHAR2,
	work_id		IN INTEGER DEFAULT 0
)
IS
BEGIN

    location := '0';
    IF pre_post_diff = MEME_CONSTANTS.SN_PRE OR pre_post_diff = MEME_CONSTANTS.SN_POST THEN

    	location := '10';
    	MEME_UTILITY.drop_it ('table',pre_post_diff || '_mgv_e');

    	location := '20';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_e AS ' ||
	    'SELECT relationship_id, atom_id_1, atom_id_2, ' ||
	    ' 	     concept_id_1 as concept_id ' ||
	    'FROM relationships ' ||
	    'WHERE concept_id_1=concept_id_2 ' ||
	    '  AND relationship_name not in (' ||
	    '	  ''SFO/LFO'',''RT?'',''NT?'',''BT?'',''LK'',''SY'') ' ||
	    '  AND tobereleased in (''Y'',''y'') ' ||
	    '  AND source_of_relationship not like ''MSH%'' ' ||
	    '  AND relationship_level = ''S'' '
	);

    	location := '25';
    	local_exec (
	    'INSERT INTO ' || pre_post_diff || '_mgv_e	' ||
	    '	(relationship_id, atom_id_1, atom_id_2, concept_id) ' ||
	    'SELECT relationship_id, atom_id_1, atom_id_2, ' ||
	    '	       concept_id_1 as concept_id ' ||
	    'FROM dead_relationships ' ||
	    'WHERE concept_id_1 = concept_id_2 AND authority != ''E-%'' ' ||
	    '  AND tobereleased in (''Y'',''y'') ' ||
	    '  AND relationship_name not in (''RT?'',''BT?'',''NT?'',''LK'',''SY'') ' ||
	    '  AND relationship_level = ''C'' '
	);

    ELSIF pre_post_diff = MEME_CONSTANTS.SN_DIFF THEN

    	location := '30';

    ELSE
	meme_SNAPSHOT_proc_error('snapshot_mgv_e',location,30,pre_post_diff);
	RAISE meme_SNAPSHOT_proc_exc;
    END IF;

END snapshot_mgv_e;

/***********  Snapshot API for MGV_F ************/
PROCEDURE snapshot_mgv_f (
	pre_post_diff	IN VARCHAR2,
	work_id		IN INTEGER DEFAULT 0
)
IS
BEGIN

    location := '0';
    IF pre_post_diff = MEME_CONSTANTS.SN_PRE OR pre_post_diff = MEME_CONSTANTS.SN_POST THEN

    	location := '10';
    	MEME_UTILITY.drop_it ('table',pre_post_diff || '_mgv_f');

    	location := '20';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_f AS ' ||
	    'SELECT relationship_id, atom_id_1, atom_id_2, ' ||
	    '	    concept_id_1 as concept_id ' ||
	    'FROM relationships r, source_version sv ' ||
	    'WHERE sv.source = ''MSH'' ' ||
	    '  AND concept_id_1=concept_id_2 ' ||
	    '  AND relationship_name != ''SFO/LFO'' ' ||
	    '  AND relationship_level = ''S'' AND status = ''R'' ' ||
	    '  AND tobereleased in (''Y'',''y'') ' ||
	    '  AND source_of_relationship = current_name '
	);

    ELSIF pre_post_diff = MEME_CONSTANTS.SN_DIFF THEN

    	location := '30';

    ELSE
	meme_SNAPSHOT_proc_error('snapshot_mgv_f',location,30,pre_post_diff);
	RAISE meme_SNAPSHOT_proc_exc;
    END IF;

END snapshot_mgv_f;

/***********  Snapshot API for MGV_I ************/
PROCEDURE snapshot_mgv_i (
	pre_post_diff	IN VARCHAR2,
	work_id		IN INTEGER DEFAULT 0
)
IS
BEGIN

    location := '0';
    IF pre_post_diff = MEME_CONSTANTS.SN_PRE OR pre_post_diff = MEME_CONSTANTS.SN_POST THEN

    	location := '10';
    	MEME_UTILITY.drop_it ('table',pre_post_diff || '_mgv_i');

    	location := '20';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_i AS ' ||
	    'SELECT a.atom_id as atom_id_1, b.atom_id as atom_id_2, ' ||
	    '	a.code as code_1, b.code as code_2,a.concept_id ' ||
	    'FROM classes a, classes b ' ||
	    'WHERE a.concept_id=b.concept_id ' ||
	    '  AND a.code != b.code ' ||
	    '  AND a.tobereleased in (''Y'',''y'') ' ||
	    '  AND b.tobereleased in (''Y'',''y'') ' ||
	    '  AND a.source IN (SELECT value FROM ic_single ' ||
	    '		      WHERE ic_name = ''MGV_I'' ' ||
	    '		      AND type = ''SOURCE'' ' ||
	    '		      AND negation=''N'') ' ||
	    '  AND b.source = a.source '
	);

    ELSIF pre_post_diff = MEME_CONSTANTS.SN_DIFF THEN

    	location := '30';

    ELSE
	meme_SNAPSHOT_proc_error('snapshot_mgv_i',location,30,pre_post_diff);
	RAISE meme_SNAPSHOT_proc_exc;
    END IF;

END snapshot_mgv_i;

/***********  Snapshot API for MGV_k ************/
PROCEDURE snapshot_mgv_k (
	pre_post_diff	IN VARCHAR2,
	work_id		IN INTEGER DEFAULT 0
)
IS
BEGIN

    location := '0';
    IF pre_post_diff = MEME_CONSTANTS.SN_PRE OR pre_post_diff = MEME_CONSTANTS.SN_POST THEN

    	location := '10';
    	MEME_UTILITY.drop_it ('table',pre_post_diff || '_mgv_k');

    	location := '20';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_k AS ' ||
	    'SELECT a.atom_id as atom_id_1, b.atom_id as atom_id_2, ' ||
	    '	    a.source as source_1, b.source as source_2, ' ||
	    '	    a.code as code_1, b.code as code_2, a.concept_id ' ||
	    'FROM classes a, classes b, ic_pair ic ' ||
	    'WHERE a.concept_id=b.concept_id ' ||
	    '  AND a.code != b.code ' ||
	    '  AND a.tobereleased in (''Y'',''y'') ' ||
	    '  AND b.tobereleased in (''Y'',''y'') ' ||
	    '  AND a.source = ic.value_1 AND b.source = ic.value_2 ' ||
	    '  AND ic.type_1=''SOURCE'' AND ic.type_2 = ''SOURCE'' ' ||
	    '  AND ic.negation = ''N'' AND ic.ic_name = ''MGV_k'' '
	);

    	location := '30';
    	local_exec (
	    'CREATE TABLE ' || pre_post_diff || '_mgv_k_exceptions AS ' ||
	    'SELECT atom_id_1 AS atom_id ' ||
	    'FROM classes c, ' || pre_post_diff || '_mgv_k_exceptions m ' ||
	    'WHERE (c.source = m.source_1 OR c.source = m.source_2) ' ||
	    '  AND c.concept_id=m.concept_id ' ||
	    '  AND c.tobereleased in (''Y'',''y'') ' ||
	    '  AND m.atom_id_1 != c.atom_id ' ||
	    '  AND m.code_1 = c.code '
	);

    	location := '40';
    	local_exec (
	    'INSERT INTO ' || pre_post_diff || '_mgv_k_exceptions ' ||
	    'SELECT atom_id_1 AS atom_id ' ||
	    'FROM classes c, ' || pre_post_diff || '_mgv_k_exceptions m ' ||
	    'WHERE (c.source = m.source_1 OR c.source = m.source_2) ' ||
	    '  AND c.concept_id=m.concept_id ' ||
	    '  AND c.tobereleased in (''Y'',''y'') ' ||
	    '  AND m.atom_id_2 != c.atom_id ' ||
	    '  AND m.code_2 = c.code '
	);

	location := '50';
	local_exec (
	    'DELETE FROM ' || pre_post_diff || '_mgv_k ' ||
	    'WHERE atom_id_1 IN ' ||
	    '(SELECT atom_id FROM ' || pre_post_diff || '_mgv_k_exceptions )'
	);

	location := '60';
	local_exec (
	    'DELETE FROM ' || pre_post_diff || '_mgv_k ' ||
	    'WHERE atom_id_2 IN ' ||
	    '(SELECT atom_id FROM ' || pre_post_diff || '_mgv_k_exceptions )'
	);

    ELSIF pre_post_diff = MEME_CONSTANTS.SN_DIFF THEN

    	location := '30';

    ELSE
	meme_SNAPSHOT_proc_error('snapshot_mgv_k',location,30,pre_post_diff);
	RAISE meme_SNAPSHOT_proc_exc;
    END IF;

END snapshot_mgv_k;


END MEME_SNAPSHOT_PROC;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_SNAPSHOT_PROC.help;
execute MEME_SNAPSHOT_PROC.register_package;

