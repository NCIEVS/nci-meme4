CREATE OR REPLACE PACKAGE MEME_INTEGRITY_PROC_NCI AS

/********************************************************************************
 *
 * PL/SQL File: MEME_INTEGRITY_PROC_NCI.sql
 *
 * This package contains procedures
 * to perform MEME integrity checkings
 *
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
    package_name	VARCHAR2(25) := 'MEME_INTEGRITY_PROC_NCI';
    release_number	VARCHAR2(1)  := '4';
    version_number	VARCHAR2(5)  := '18.0';
    version_date	DATE	     := '18-Apr-2005';
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

   -- Find ambiguous strings
    FUNCTION separated_strings (
	table_name IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
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

  -- DT_PN1 b
    FUNCTION ambig_no_mth_pn (
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

  --
  -- Multiple meaning (extra) queries
  --
    FUNCTION ambig_pn (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;

  --
  -- Deleted CUI (minus MTH-only) query
  --
    FUNCTION deleted_cui_nomth (
	cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
	work_id IN INTEGER := 0)
    RETURN VARCHAR2;


END MEME_INTEGRITY_PROC_NCI;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY MEME_INTEGRITY_PROC_NCI AS

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
      MEME_INTEGRITY_PROC_NCI.release_number,
      MEME_INTEGRITY_PROC_NCI.version_number,
      SYSDATE,
      MEME_INTEGRITY_PROC_NCI.version_authority,
      MEME_INTEGRITY_PROC_NCI.package_name,
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

    MEME_UTILITY.PUT_ERROR('Error in MEME_INTEGRITY_PROC_NCI.' ||
			   ' Method: ' || method ||
			   ' Location: ' || location  ||
			   ' Error Code: '|| error_msg ||
			   ' Detail: '|| detail);

END meme_integrity_proc_error;

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

    IF table_name != MEME_CONSTANTS.EMPTY_TABLE THEN
	restriction_clause := ' and concept_id IN (select concept_id from
' || table_name || ') ';

	location := '10';
	local_exec('CREATE TABLE ' || t_ss || '_classes AS ' ||
		  'SELECT isui FROM CLASSES a, ' || table_name || ' b ' ||
		  'WHERE a.concept_id = b.concept_id ' ||
		  '  AND tobereleased not in (''n'',''N'') ' );
    	location := '20';
	classes := MEME_UTILITY.get_unique_tablename('qat_');
	location := '30';
	local_exec('CREATE TABLE ' || classes || ' AS ' ||
		   'SELECT * FROM classes ' ||
		   'WHERE tobereleased not in (''N'',''n'') ' ||
		   '  AND isui IN (SELECT isui FROM ' || t_ss || '_classes)' );
   ELSE
	restriction_clause := ' ';
	classes := 'classes';
   END IF;

    -- Get ambiguous isuis
    location := '40';
    local_exec (
	'CREATE TABLE ' || t_ss || '_ambig_isui AS ' ||
	'SELECT isui FROM ' || classes || ' ' ||
	'WHERE tobereleased not in (''N'',''n'') ' ||
	'GROUP BY isui HAVING count(*) > 1'  );

    location := '50';
    local_exec(
	'CREATE TABLE ' || t_ss || ' AS SELECT /*+ RULE */ ' ||
	'c1.concept_id AS concept_id_1, ' ||
	'c2.concept_id AS concept_id_2, ' ||
	'c1.isui ' ||
	'FROM ' || classes || ' c1, ' || classes || ' c2, ' ||
		   t_ss || '_ambig_isui b ' ||
	'WHERE c1.isui = b.isui and c2.isui = b.isui ' ||
	'AND c1.concept_id < c2.concept_id ' ||
--	'AND (c1.source != ''NCIMTH'' OR c1.termgroup NOT LIKE ''%PN'') ' ||
	'AND c1.tobereleased IN (''Y'',''y'',''?'') ' ||
--	'AND (c2.source != ''NCIMTH'' OR c2.termgroup NOT LIKE ''%PN'') ' ||
	'AND c2.tobereleased IN (''Y'',''y'',''?'')');

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
	'WHERE source=''NCIMTH'' and termgroup like ''%PN'' ' ||
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

/* FUNCTION AMBIG_NO_PN ********************************************************
 */
FUNCTION ambig_no_mth_pn (
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
	'WHERE source like ''MTH_%'' and termgroup like ''%PN'' ' ||
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
	meme_integrity_proc_error('ambig_no_mth_pn',location,1,SQLERRM);
	RAISE MEME_INTEGRITY_PROC_EXC;

END ambig_no_mth_pn;

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
	'SELECT concept_id FROM classes ' ||
	'WHERE termgroup like ''%PN'' ' ||
	restriction_clause ||
	'AND source = ''NCIMTH'' ' ||
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

    -- Get NCIMTH/PN atoms
    location := '0';
    t_pn_no_ambig := MEME_UTILITY.get_unique_tablename('qat_');
    location := '10';
    local_exec (
	'CREATE TABLE ' || t_pn_no_ambig || ' AS ' ||
	'SELECT concept_id ' ||
	'FROM classes ' ||
	'WHERE source=''NCIMTH'' ' ||
	'  AND termgroup=''NCIMTH/PN'' ' ||
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
	'AND source = ''NCIMTH'' and termgroup like ''%PN'' ');

    location := '20';

    -- Get ambiguous concept_ids
    local_exec (
	'CREATE TABLE ' || t_pn_pn_ambig_isui || ' AS ' ||
	'SELECT isui FROM classes ' ||
	'WHERE isui IN (SELECT isui FROM ' || t_pn_isui || ') ' ||
	'AND tobereleased in (''Y'',''y'') ' ||
	'AND source = ''NCIMTH'' and termgroup like ''%PN'' ' ||
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

/*******************************************************************************
 *
 *  Extra MM Queries
 *
 * Following is code to implement QA bin code for suresh
 *
 ******************************************************************************/

/* FUNCTION AMBIG_PN ***********************************************************
 * This procedure returns concepts which have NCIMTH/PN
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
     	 WHERE source = ''NCIMTH'' AND termgroup = ''NCIMTH/PN''
	   AND tobereleased = ''Y''
	 GROUP BY isui HAVING count(distinct concept_id)>1'
    );

    location := '20';
    local_exec (
	'CREATE TABLE ' || t_ambig_pn || ' AS ' ||
	'SELECT concept_id, isui FROM classes ' ||
     	'WHERE source = ''NCIMTH'' AND termgroup = ''NCIMTH/PN'' ' ||
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

/* FUNCTION deleted_cui_nomth ********************************************************
 * This functions produces a list of concept ids which contain CUIs that will
 * appear in the next DELETED.CUI file.  These CUIs are considered deleted
 * because all of their atoms are obsolete.
 * This particular version of the function removes concepts from consideration
 * that are (UMLS) MTH-only.
 */
FUNCTION deleted_cui_nomth (
   cluster_flag IN INTEGER := MEME_CONSTANTS.CLUSTER_YES,
   work_id      IN INTEGER := 0
)
RETURN VARCHAR2
IS
   deleted_cui_nomth     VARCHAR2(50);
   cluster_table   VARCHAR2(50);
   result_table    VARCHAR2(50);

BEGIN

   location := '10';
   /* Get cuis excluding assigned ones */

   deleted_cui_nomth := MEME_UTILITY.get_unique_tablename('qat_');
   MEME_UTILITY.drop_it ('table',deleted_cui_nomth);

   -- Get assigned CUIs minus cuis in concept status and cui_history
   local_exec(
     'CREATE TABLE ' || deleted_cui_nomth || ' AS
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
      'DELETE FROM ' || deleted_cui_nomth || ' WHERE cui IN
         (SELECT cui FROM concept_status
          WHERE tobereleased IN (''Y'',''y'') ) ');

   local_exec(
      'DELETE FROM ' || deleted_cui_nomth || ' WHERE concept_id IN
         (SELECT concept_id FROM concept_status
          WHERE tobereleased IN (''Y'',''y'') ) ');

   location := '30';
   -- Remove any splits from consideration
   local_exec('DELETE FROM ' || deleted_cui_nomth || ' WHERE cui IN
      (SELECT a.last_release_cui FROM classes a, classes b
        WHERE a.concept_id != b.concept_id
          AND a.last_release_cui IS NOT NULL
          AND b.last_release_cui IS NOT NULL
          AND a.last_release_cui = b.last_release_cui)');

   location := '30';
   -- Remove any merges from consideration
   local_exec('DELETE FROM ' || deleted_cui_nomth || ' WHERE cui IN
      (SELECT a.last_release_cui FROM classes a, classes b
        WHERE a.concept_id = b.concept_id
          AND a.last_release_cui IS NOT NULL
          AND b.last_release_cui IS NOT NULL
          AND a.last_release_cui != b.last_release_cui)');

   location := '35';
   -- Remove any bequeathed concepts from consideration
   local_exec('DELETE FROM ' || deleted_cui_nomth || ' WHERE concept_id IN
      (SELECT concept_id_1 FROM relationships
       WHERE relationship_name in (''BBT'',''BNT'',''BRT'')
       UNION
       SELECT concept_id_2 FROM relationships
       WHERE relationship_name in (''BBT'',''BNT'',''BRT'')) ');

   location := '37';
   -- Remove any (UMLS) MTH-only concepts from consideration
   local_exec('DELETE FROM ' || deleted_cui_nomth || ' WHERE concept_id IN
      (SELECT concept_id FROM classes
       WHERE source =
          (SELECT current_name FROM source_version WHERE source=''MTH'')
       MINUS
       SELECT concept_id FROM classes
       WHERE source !=
          (SELECT current_name FROM source_version WHERE source=''MTH'')
       )
   ');

   location := '40';
   -- Get distinct concept_ids
   cluster_table := MEME_UTILITY.get_unique_tablename('qat_');

   local_exec(
     'CREATE TABLE ' || cluster_table || ' AS
      SELECT DISTINCT concept_id FROM ' || deleted_cui_nomth);

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
   MEME_UTILITY.drop_it ('table',deleted_cui_nomth);
   MEME_UTILITY.drop_it ('table',cluster_table);

   RETURN result_table;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',deleted_cui_nomth);
      MEME_UTILITY.drop_it('table',cluster_table);
      MEME_UTILITY.drop_it('table',result_table);
      meme_integrity_proc_error('deleted_cui_nomth',location,1,SQLERRM);
      RAISE MEME_INTEGRITY_PROC_EXC;
END deleted_cui_nomth;


END MEME_INTEGRITY_PROC_NCI;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_INTEGRITY_PROC_NCI.help;
execute MEME_INTEGRITY_PROC_NCI.register_package;

