CREATE OR REPLACE PACKAGE MEME_UTILITY AS

/*******************************************************************************
 *
 * PL/SQL File: MEME_UTILITY.sql
 *
 * This package contains all the utilities
 * that used by other MEME software.
 *
 * Version Information
 *
 * 02/22/2013 PM: Modified log_operation() to substring the err_msg and detail
 *                to specified length.
 * 08/29/2006 SL (1-C17ND)  Adding Oracle10g performance like analyze staments
 * 08/30/2005 3.21.2: log_progress takes an optional progress parameter.
 * 08/09/2005 3.21.1: map_sg_fields puts s_c_a query first and checks switch
 *                    This should allow mapping to always choose highest ranked
 *                    atom for any class
 * 06/01/2005 3.21.0: Released
 * 05/26/2005 3.20.1: cluster_pair_recursive fix
 * 05/16/2005 3.20.0: Released
 * 04/26/2005 3.19.1: Fixes for cluster_pair_recursive
 * 03/25/2005 3.19.0: Support AUI map type
 * 02/15/2005 3.18.1: Support CUI_SOURCE
 * 12/13/2004 3.18.0: Released
 * 11/29/2004 3.17.1: -report_table_changed, map_sg_fields
 * 08/31/2004 3.17.0: Released
 *                    new_work looks up work by detail and returns existing
 *                    work_id if it matches.
 * 08/09/2004 3.16.0: Released
 * 08/02/2004 3.15.1: map_sg_fields:  maps a single sg_id,type,qualifier
 *                    to its meme id and type and an atom_id proxy.
 * 04/28/2004 3.15.0: Released
 * 04/26/2004 3.14.1: Better MD5 calculation
 * 03/25/2004 3.14.0: Released
 * 03/17/2004 3.13.2: cluster_pair_recursive optimized
 * 03/04/2004 3.13.1: elapsed_time calculated in milliseconds
 *	              md5-enabled
 * 01/15/2003 3.13.0: Fix MEME_SYSTEM dependency
 * 10/24/2002 3.12.0: +cluster_pair_recursive
 * 04/16/2002 3.11.0: oq_mode=Y, Released.
 * 02/28/2002 3.10.0: Released
 * 01/22/2002 3.9.1: report_table_changed updated so that if multiple
 *                   sections of a truncate event are recorded, the subsequent
 *                   sections have append as the command.
 * 09/05/2001 3.9.0: Released 8.* changes
 * 05/15/2001 3.8.2: report_table_change was still not properly closing/opening
 *			CDATA sections.  
 * 05/11/2001 3.8.1: report_table_change was not properly closing <![[CDATA 
 *			section, fixed.
 * 02/14/2001 3.8.0: Released
 * 02/06/2001 3.7.1: changes in report_table_change
 * 11/10/2000 3.7.0: Released
 * 11/03/2000 3.6.7: report_table_change
 * 10/24/2000 3.6.6: Released, set_oq_mode_on, set_oq_mode_off
 * 10/17/2000 3.6.5: changes in exec
 * 10/10/2000 3.6.4: ddl_exec, PRAGMA AUTONOMOUS_TRANSACTION
 * 10/05/2000 3.6.3: changes in get_ic_state, object_exist
 * 9/28/2000  3.6.2: changes in get_field_type
 * 9/19/2000  3.6.1: set_mode_mrd, set_mode_mid
 * 9/11/2000  3.6.0: Package version
 * 8/24/2000  3.5.1: strip_string added to remove <1> or <11> from MTH/MM 
 *                   strings
 * 8/01/2000  3.5.0: Package handover version
 *		     get_bracket_number added to get the bracket number 
 *                   from mm strings
 * 6/07/2000  3.4.2: initialize_trace. Prepared for 8i enhancements
 * 5/9/2000   3.4.0: Released to NLM
 * 5/25/2000  3.4.1: get_unique_tablename can take a prefix
 * 04/13/2000 3.3.2: log_progress, log_operation use row_sequence
 * 3/6/2000:	This package now uses MEME_CONSTANTS
 * 9/9/1999:	First version created and compiled
 *
 * Status:
 *	Functionality:	DONE
 *	Testing:	DONE
 * 	Enhancements:
 *		8i enhancements (autonomous transactions and dynamic SQL)
 *
 ******************************************************************************/

    package_name	VARCHAR2(25) := 'MEME_UTILITY';
    release_number	VARCHAR2(1)  := '4';
    version_number	VARCHAR2(5)  := '21.0';
    version_date	DATE	     := '01-Jun-2005';
    version_authority	VARCHAR2(10) := 'BAC';

    location		VARCHAR2(10);
    err_msg		VARCHAR2(3000);
    method		VARCHAR2(256);

    meme_utility_debug	BOOLEAN := FALSE;
    meme_utility_trace	BOOLEAN := FALSE;
    meme_mode		VARCHAR2(10) := MEME_CONSTANTS.MID_MODE;
    ddl_commit_mode	VARCHAR2(10) := MEME_CONSTANTS.YES;
    oq_mode		VARCHAR2(10) := MEME_CONSTANTS.NO;

    meme_utility_exception	EXCEPTION;

    -- timing
    start_time			INTEGER;
    stop_time			INTEGER;
    sub_start_time		INTEGER;
    sub_stop_time		INTEGER;

    FUNCTION release RETURN INTEGER;
    FUNCTION version RETURN FLOAT;
    FUNCTION version_info RETURN VARCHAR2;
    PRAGMA restrict_references (version_info,WNDS,RNDS,WNPS);

    PROCEDURE version;
    PROCEDURE set_mode_mrd;
    PROCEDURE set_mode_mid;
    PROCEDURE set_ddl_commit_on;
    PROCEDURE set_ddl_commit_off;
    PROCEDURE set_oq_mode_on;
    PROCEDURE set_oq_mode_off;
    PROCEDURE help;
    PROCEDURE help (topic IN VARCHAR2);
    PROCEDURE register_package;
    PROCEDURE self_test;
    PROCEDURE set_trace_on;
    PROCEDURE set_trace_off;
    PROCEDURE set_debug_on;
    PROCEDURE set_debug_off;
    PROCEDURE trace(message IN VARCHAR2 );

    PROCEDURE meme_utility_error (
    	method		    IN VARCHAR2,
    	location	    IN VARCHAR2,
    	error_code	    IN INTEGER,
    	detail		    IN VARCHAR2
    );

    PROCEDURE initialize_trace ( method IN VARCHAR2 );

    FUNCTION count_row_id (
    	table_name  IN VARCHAR2,
    	row_id IN integer
    ) RETURN INTEGER;

    FUNCTION drop_it(
    	type	IN VARCHAR2,
    	name	IN VARCHAR2
    ) RETURN INTEGER;

    PROCEDURE drop_it(
    	type	IN VARCHAR2,
    	name	IN VARCHAR2
    );

    PROCEDURE exec(
    	string	    IN VARCHAR2
    );

    FUNCTION exec(
    	string	IN VARCHAR2
    ) RETURN INTEGER;

    PROCEDURE ddl_exec(
    	string	IN VARCHAR2
    );

    FUNCTION exec_select(
	query	IN VARCHAR2
    ) RETURN INTEGER;

    FUNCTION exec_select_varchar(
	query	IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION exec_count(
	t_name IN VARCHAR2
    ) RETURN INTEGER;

    PROCEDURE exec_plsql(
	query IN VARCHAR2
    );

    FUNCTION table_to_string (
	table_name	IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_next_id (
	table_name	IN VARCHAR2
    ) RETURN INTEGER;

    FUNCTION get_unique_tablename
    RETURN VARCHAR2;
    FUNCTION get_unique_tablename ( prefix IN VARCHAR2 )
    RETURN VARCHAR2;

    FUNCTION strip_string (string IN VARCHAR2 )
    RETURN VARCHAR2;

    PRAGMA restrict_references (strip_string,WNDS,WNPS);

    FUNCTION get_bracket_number (string IN VARCHAR2 )
    RETURN NUMBER;

    PRAGMA restrict_references (get_bracket_number,WNDS,WNPS);

    PROCEDURE validate_code (
	code 		IN VARCHAR2,
	type		IN VARCHAR2
    );

    FUNCTION get_value_by_code(
    	code 		    IN VARCHAR2,
    	type 		    IN VARCHAR2
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (get_value_by_code,WNDS,WNPS);

    FUNCTION get_code_by_value (
    	value 		    IN VARCHAR2,
    	type 		    IN VARCHAR2
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (get_code_by_value,WNDS,WNPS);

    FUNCTION get_procedure_name_by_ic(
    	ic_name 		    IN VARCHAR2
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (get_procedure_name_by_ic,WNDS,WNPS);

    FUNCTION get_ic_by_procedure_name (
    	procedure_name 		    IN VARCHAR2
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (get_ic_by_procedure_name,WNDS,WNPS);

    FUNCTION get_table_name_by_code(
    	code		    IN VARCHAR2
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (get_table_name_by_code,WNDS,WNPS);

    FUNCTION get_molecular_action_by_code(
    	code 		    IN VARCHAR2
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (get_molecular_action_by_code,WNDS,WNPS);

    FUNCTION get_code_by_molecular_action (
    	action		    IN VARCHAR2
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (get_codE_by_molecular_action,WNDS,WNPS);

    FUNCTION get_ic_by_num (
	iv		IN VARCHAR2,
	ic_num		IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_ic_code (
	iv		IN VARCHAR2,
	ic_name		IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_ic_code_by_num (
	iv		IN VARCHAR2,
	ic_num		IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_ic_name_by_num (
	iv		IN VARCHAR2,
	ic_num		IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_ic_state (
	ic_code		IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_field_type(
	tab_name	IN VARCHAR2,
	col_name	IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_current_name(
	source		IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_previous_name(
	source		IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION get_integrity_vector (
	application	IN VARCHAR2
    ) RETURN VARCHAR2;

    FUNCTION object_exists(
	type		IN VARCHAR2,
	name		IN VARCHAR2
    ) RETURN INTEGER;

    FUNCTION log_operation(
    	authority	IN VARCHAR2,
    	activity	IN VARCHAR2,
    	detail		IN VARCHAR2,
	transaction_id	IN INTEGER,
	work_id		IN INTEGER,
	elapsed_time	IN INTEGER DEFAULT 0
    ) RETURN INTEGER;

    PROCEDURE log_operation(
    	authority	IN VARCHAR2,
    	activity	IN VARCHAR2,
    	detail		IN VARCHAR2,
	transaction_id	IN INTEGER,
	work_id		IN INTEGER,
	elapsed_time	IN INTEGER DEFAULT 0
    );

    PROCEDURE log_progress(
    	authority	IN VARCHAR2,
    	activity	IN VARCHAR2,
    	detail		IN VARCHAR2,
		transaction_id	IN INTEGER,
		work_id		IN INTEGER,
		elapsed_time	IN INTEGER DEFAULT 0,
		progress	IN INTEGER DEFAULT 0
    );

    -- returns work_id
    FUNCTION new_work (
		authority	IN VARCHAR2,
		type		IN VARCHAR2,
		description	IN VARCHAR2
    ) RETURN INTEGER ;

    -- returns work_id but does not log in meme_work
    FUNCTION new_work_id  RETURN INTEGER ;

    PROCEDURE reset_progress(work_id	IN INTEGER );

    PROCEDURE put_error(
	err_msg		IN VARCHAR2,
	authority	IN VARCHAR2 := NULL,
	transaction_id	IN INTEGER := 0,
	work_id		IN INTEGER := 0,
	elapsed_time	IN INTEGER DEFAULT 0
    );

    PROCEDURE put_application_error(
	err_msg		IN VARCHAR2
    );

    PROCEDURE put_message(
	message		IN VARCHAR2
    );

    PRAGMA restrict_references (put_message,WNDS);

    -- timing
    PROCEDURE timing_start;
    PROCEDURE timing_stop;
    FUNCTION elapsed_time RETURN INTEGER;

    PROCEDURE sub_timing_start;
    PROCEDURE sub_timing_stop;
    FUNCTION sub_elapsed_time RETURN INTEGER;

    -- Clustering
    -- These were moved from MEME_INTEGRITY because
    -- There was a module dependency where MEME_INTEGRITY relied
    -- on MEME_INTEGRITY_PROC and vice versa
    FUNCTION cluster_single ( table_name IN VARCHAR2 ) RETURN VARCHAR2;
    FUNCTION cluster_pair ( table_name IN VARCHAR2 ) RETURN VARCHAR2;
    FUNCTION cluster_pair_recursive ( table_name IN VARCHAR2 ) RETURN VARCHAR2;
    FUNCTION recluster ( table_name IN VARCHAR2 ) RETURN VARCHAR2;

   FUNCTION md5 (
      str IN VARCHAR2) RETURN VARCHAR2;

   PRAGMA RESTRICT_REFERENCES(md5, TRUST);

END meme_utility;
/
SHOW ERRORS

CREATE OR REPLACE PACKAGE BODY meme_utility AS

/* FUNCTION RELEASE ************************************************************
 */
FUNCTION release
RETURN INTEGER
IS
BEGIN

    version;
    return to_number(release_number);

END release;

/* FUNCTION VERSION_INFO *******************************************************
 */
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
    return package_name || ' Release ' || release_number || ': ' ||
	   'version ' || version_number || ' (' || version_date || ')';
END version_info;

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

/* PROCEDURE SET_MODE_MID ******************************************************
 */
PROCEDURE set_mode_mid
IS
BEGIN
   meme_mode := 'MID';
END set_mode_mid;

/* PROCEDURE SET_MODE_MRD ******************************************************
 */
PROCEDURE set_mode_mrd
IS
BEGIN
   meme_mode := 'MRD';
END set_mode_mrd;

/* PROCEDURE SET_DDL_COMMIT_MODE_ON ********************************************
 */
PROCEDURE set_ddl_commit_on
IS
BEGIN
   ddl_commit_mode := MEME_CONSTANTS.YES;
END set_ddl_commit_on;

/* PROCEDURE SET_DDL_COMMIT_MODE_OFF *******************************************
 */
PROCEDURE set_ddl_commit_off
IS
BEGIN
   ddl_commit_mode := MEME_CONSTANTS.NO;
END set_ddl_commit_off;

/* PROCEDURE SET_OQ_MODE_ON ****************************************************
 */
PROCEDURE set_oq_mode_on
IS
BEGIN
   oq_mode := MEME_CONSTANTS.YES;
END set_oq_mode_on;

/* PROCEDURE SET_OQ_MODE_OFF ***************************************************
 */
PROCEDURE set_oq_mode_off
IS
BEGIN
   oq_mode := MEME_CONSTANTS.NO;
END set_oq_mode_off;


/* PROCEDURE REGISTER_PACKAGE **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_UTILITY.release_number,
      MEME_UTILITY.version_number,
      SYSDATE,
      MEME_UTILITY.version_authority,
      MEME_UTILITY.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

/* PROCEDURE SELF_TEST *********************************************************
 */
PROCEDURE self_test
IS
BEGIN

    DBMS_OUTPUT.ENABLE(100000);
    -- This procedure requires SET SERVEROUTPUT ON

END self_test;

/* PROCEDURE SET_TRACE_ON ******************************************************
 */
PROCEDURE set_trace_on
IS
BEGIN

    meme_utility_trace := TRUE;

END set_trace_on;

/* PROCEDURE SET_TRACE_OFF******************************************************
 */
PROCEDURE set_trace_off
IS
BEGIN

    meme_utility_trace := FALSE;

END set_trace_off;

/* PROCEDURE SET_DEBUG_ON ******************************************************
 */
PROCEDURE set_debug_on
IS
BEGIN

    meme_utility_debug := TRUE;

END set_debug_on;

/* PROCEDURE SET_DEBUG_OFF *****************************************************
 */
PROCEDURE set_debug_off
IS
BEGIN

    meme_utility_debug := FALSE;

END set_debug_off;

/* PROCEDURE TRACE *************************************************************
 */
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF meme_utility_trace = TRUE THEN

	PUT_MESSAGE(message);

    END IF;

END trace;

/* PROCEDURE MEME_UTILITY_ERROR ************************************************
 */
PROCEDURE meme_utility_error (
    	method		    IN VARCHAR2,
    	location	    IN VARCHAR2,
    	error_code	    IN INTEGER,
    	detail		    IN VARCHAR2
)
IS
    error_msg	    VARCHAR2(100);
BEGIN
    IF error_code = 1 THEN
	error_msg := 'MU0001: Unspecified error';
    ELSIF error_code = 10 THEN
	error_msg := 'MU0010: No Data Found';
    ELSIF error_code = 20 THEN
	error_msg := 'MU0020: Error executing dynamic PL/SQL block';
    ELSIF error_code = 30 THEN
	error_msg := 'MU0030: Invalid code';
    ELSE
	error_msg := 'MU0000: Unknown Error';
    END IF;

    MEME_UTILITY.PUT_ERROR('Error in MEME_UTILITY::'||method||' at '||
	location||' ('||error_msg||','||detail||')');

END meme_utility_error;

/* PROCEDURE INITIALIZE_TRACE **************************************************
 * This method clears location, err_msg, method
 */
PROCEDURE initialize_trace ( method	IN VARCHAR2 )
IS
BEGIN
    location := '0';
    err_msg := '';
    meme_utility.method := initialize_trace.method;
END initialize_trace;

/* FUNCTION COUNT_ROW_ID *******************************************************
 */
FUNCTION count_row_id (
	table_name IN VARCHAR2,
	row_id	   IN INTEGER
) RETURN INTEGER

IS
	i		INTEGER;

BEGIN

    i := 0;

    IF table_name = 'CS' THEN

	SELECT count(*) into i
	FROM concept_status
	WHERE concept_id = row_id;

    ELSIF table_name = 'C' THEN

	SELECT count(*) into i
	FROM classes
	WHERE atom_id = row_id;

    ELSIF table_name = 'A' THEN

	SELECT count(*) into i
	FROM attributes
	WHERE attribute_id = row_id;

    ELSIF table_name = 'R' THEN

	SELECT count(*) into i
	FROM relationships
	WHERE relationship_id = row_id;

    END IF;

    RETURN i;

EXCEPTION
    WHEN OTHERS THEN
	meme_utility_error('count_row_id','10',1,table_name||','||SQLERRM);
	RETURN -1;

END count_row_id;

/* FUNCTION DROP_IT ************************************************************
 */
FUNCTION drop_it(
    type	    IN VARCHAR2,
    name	    IN VARCHAR2
) RETURN INTEGER
IS
BEGIN
    drop_it(type,name);
    RETURN 0;
EXCEPTION
    WHEN OTHERS THEN
	RETURN -1;
END drop_it;

/* PROCEDURE DROP_IT ***********************************************************
 */
PROCEDURE drop_it(
    type	    IN VARCHAR2,
    name	    IN VARCHAR2
)

IS
	obj_name	VARCHAR2(50);
	NO_TYPE_FOUND	EXCEPTION;

BEGIN

    initialize_trace('drop_it');

    if LOWER(type) NOT IN ('table','index','view','sequence','cluster') THEN
	RAISE NO_TYPE_FOUND;
    END IF;

    IF name = '' THEN
	RETURN;
    END IF;

    location := '10';
    err_msg := 'Error selecting from user_objects';
    SELECT object_name INTO obj_name FROM user_objects
    WHERE object_name = UPPER(name);

    -- goes to NO_DATA_FOUND exception if object_name not found

    location := '20';
    err_msg := 'Error dropping ' || type;

    IF ddl_commit_mode = MEME_CONSTANTS.YES THEN
       location := '20.1';
       MEME_UTILITY.exec('DROP '||type||' '||name);
    ELSIF ddl_commit_mode = MEME_CONSTANTS.NO THEN
       location := '20.2';
       MEME_UTILITY.ddl_exec('DROP '||type||' '||name);
    END IF;

EXCEPTION

    WHEN NO_TYPE_FOUND THEN
	trace('Object Type ' || UPPER(type) || ' does not exist.');

    WHEN NO_DATA_FOUND THEN
	trace(INITCAP(type) || ' ' || UPPER(name) || ' does not exist.');

    WHEN OTHERS THEN
	meme_utility_error(method,location,1,
		type || ',' || name || ',' || SQLERRM);
	RAISE meme_utility_exception;

END drop_it;

/* PROCEDURE EXEC **************************************************************
 * In order to use this procedure to dynamically create
 * a table, the user must be explicitly granted the
 * create table privelege
 */
PROCEDURE exec(string IN varchar2)
AS
	cursor_name INTEGER;
	ret INTEGER;
BEGIN
	ret := exec(string);
	IF ret < -1 THEN
	    RAISE meme_utility_exception;
	END IF;
END exec;

/* FUNCTION EXEC ***************************************************************
 */
FUNCTION exec(string IN varchar2)
RETURN INTEGER
AS
	cursor_name INTEGER;
	ret INTEGER := 0;
BEGIN

	IF LOWER(string) LIKE 'create%' AND
	   ddl_commit_mode = MEME_CONSTANTS.NO THEN
	   ddl_exec(string);
	   RETURN 0;
	END IF;
  	IF meme_utility_trace = TRUE THEN
	    put_message(string);
	END IF;

	IF meme_utility_debug = FALSE THEN
-- execute immediate does not return row count
	    execute immediate string;
--	    cursor_name:= DBMS_SQL.OPEN_CURSOR;
--	    DBMS_SQL.PARSE(cursor_name, string, DBMS_SQL.NATIVE);
--	    ret := DBMS_SQL.EXECUTE(cursor_name);
--	    DBMS_SQL.CLOSE_CURSOR(cursor_name);
	    ret := SQL%ROWCOUNT;
	END IF;
	RETURN ret;

--  This needs to pass exceptions along

END exec;

/* PROCEDURE DDL_EXEC ***********************************************************
 */
PROCEDURE ddl_exec(string IN varchar2)
IS
	PRAGMA AUTONOMOUS_TRANSACTION;
	cursor_name INTEGER;
	ret INTEGER := 0;
BEGIN
  	IF meme_utility_trace = TRUE THEN
	    put_message(string);
	END IF;

	IF meme_utility_debug = FALSE THEN
	    execute immediate string;
--	    cursor_name:= DBMS_SQL.OPEN_CURSOR;
--	    DBMS_SQL.PARSE(cursor_name, string, DBMS_SQL.NATIVE);
--	    ret := DBMS_SQL.EXECUTE(cursor_name);
--	    DBMS_SQL.CLOSE_CURSOR(cursor_name);
	END IF;

--  This needs to pass exceptions along

END ddl_exec;


/* FUNCTION EXEC_SELECT ********************************************************
 */
FUNCTION exec_select(query IN varchar2)
RETURN INTEGER
AS
    c		INTEGER;	-- cursor
    ct		INTEGER;
    ret		INTEGER;

BEGIN

    IF meme_utility_trace = TRUE THEN
	put_message (query);
    END IF;

    IF meme_utility_debug = FALSE THEN
    	c:= DBMS_SQL.OPEN_CURSOR;
    	DBMS_SQL.PARSE(c, query, DBMS_SQL.NATIVE);
    	DBMS_SQL.DEFINE_COLUMN(c, 1, ct);
    	ret := DBMS_SQL.EXECUTE(c);
    	ret := DBMS_SQL.FETCH_ROWS(c);
    	DBMS_SQL.COLUMN_VALUE(c,1,ct);
    	DBMS_SQL.CLOSE_CURSOR(c);
	RETURN ct;
    ELSE
	RETURN -1;
    END IF;

-- Pass exceptions along

END exec_select;

/* FUNCTION EXEC_SELECT_VARCHAR ************************************************
 * This function takes a sql query which returns a single
 * row, single column varchar2 value and it executes it and
 * returns the result
 */
FUNCTION exec_select_varchar(query IN varchar2)
RETURN VARCHAR2
AS
    c		INTEGER;	-- cursor
    vchar	VARCHAR2(2000);
    ret		INTEGER;
BEGIN

    trace(query);

    IF meme_utility_debug = FALSE THEN
     	c:= DBMS_SQL.OPEN_CURSOR;

    	DBMS_SQL.PARSE(c, query, DBMS_SQL.NATIVE);

    	DBMS_SQL.DEFINE_COLUMN(c, 1, vchar,2000);

    	ret := DBMS_SQL.EXECUTE(c);

    	ret := DBMS_SQL.FETCH_ROWS(c);

    	DBMS_SQL.COLUMN_VALUE(c,1,vchar);

    	DBMS_SQL.CLOSE_CURSOR(c);

    	RETURN vchar;

    ELSE

	RETURN '';

    END IF;

-- Pass exceptions along

END exec_select_varchar;

/* FUNCTION EXEC_COUNT *********************************************************
 * Count the rows in a table
 */
FUNCTION exec_count(t_name IN varchar2)
RETURN INTEGER
AS
    ct		INTEGER;
    query	VARCHAR2(256);

BEGIN

    query := 'SELECT count(*) FROM ' || t_name;
    ct := MEME_UTILITY.exec_select(query);
    RETURN ct;

-- Pass exceptions along

END exec_count;

/* FUNCTION EXEC_PLSQL *********************************************************
 * Execute PL/SQL by wrapping query with 'BEGIN query END;'
 */
PROCEDURE exec_plsql(query IN varchar2)
IS
    ct		INTEGER;
    cur		INTEGER := DBMS_SQL.OPEN_CURSOR;
    plsql_query VARCHAR2(1000);
BEGIN

    initialize_trace('exec_plsql');

    trace(query);

    IF meme_utility_debug = FALSE THEN
    	-- Open Cursor and Parse Query
    	plsql_query := 'BEGIN ' || RTRIM(query,';') || '; END;';

    	DBMS_SQL.PARSE(cur, plsql_query, DBMS_SQL.NATIVE);

    	ct := DBMS_SQL.EXECUTE(cur);

    	DBMS_SQL.CLOSE_CURSOR(cur);

    END IF;

-- Pass exceptions along

END exec_plsql;

/* FUNCTION GET_UNIQUE_TABLENAME ***********************************************
 */
FUNCTION get_unique_tablename
RETURN VARCHAR2
IS
BEGIN
	RETURN get_unique_tablename(MEME_CONSTANTS.TMP_TABLE_PREFIX);
END get_unique_tablename;

/* FUNCTION GET_UNIQUE_TABLENAME ***********************************************
 * Get unique tablename.
 * Autocommit must be off for this to work.
 */
FUNCTION get_unique_tablename (
    	prefix		IN VARCHAR2
)
RETURN VARCHAR2

IS
	t_id		INTEGER;
	username	VARCHAR2(50);
	PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
    initialize_trace('get_unique_tablename');

-- Useful for multiple schemas
--    location := '0';
--    SELECT user into username from dual;

    location := '10';
    err_msg := 'Error increasing counter';
    UPDATE max_tab SET max_id = max_id+1
    WHERE table_name = 'TRANSACTIONS';

    location := '20';
    err_msg := 'Error selecting counter';
    SELECT max_id into t_id
    FROM max_tab WHERE table_name = 'TRANSACTIONS';

    COMMIT;

--    RETURN username || '.' || prefix || to_char(t_id);
    RETURN prefix || to_char(t_id);

EXCEPTION
    WHEN OTHERS THEN
	meme_utility_error(method,location,1,err_msg || ': ' || SQLERRM);
	RAISE meme_utility_exception;
END get_unique_tablename;

/* FUNCTION STRIP_STRING *******************************************************
 */
FUNCTION strip_string (
	string 	IN VARCHAR2
)
RETURN VARCHAR2
IS
    len		INTEGER;
    t_string	VARCHAR2(4000);
BEGIN
    len := length(string);
    IF string like '% <_>' THEN
  	t_string := SUBSTR(string,0,len-4);
    ELSIF string like '% <__>' THEN
  	t_string := SUBSTR(string,0,len-5);
    ELSE
	RETURN string;
    END IF;
    RETURN t_string;
END strip_string;


/* FUNCTION GET_BRACKET_NUMBER *************************************************
 */
FUNCTION get_bracket_number (
	string 	IN VARCHAR2
)
RETURN NUMBER
IS
    len		INTEGER;
    bracket_number INTEGER;
BEGIN
    len := length(string);
    bracket_number := 0;
    IF string like '% <_>' THEN
  	bracket_number := to_number(SUBSTR(string,len-1,1));
    ELSIF string like '% <__>' THEN
  	bracket_number := to_number(SUBSTR(string,len-2,2));
    END IF;
    RETURN bracket_number;

END get_bracket_number;

/* FUNCTION GET_CODE_BY_VALUE **************************************************
 */
FUNCTION get_code_by_value (
	value 	IN VARCHAR2,
	type 	IN VARCHAR2
) RETURN VARCHAR2
IS
    code	VARCHAR2(50);
BEGIN

	SELECT code INTO get_code_by_value.code FROM code_map
 	WHERE type = get_code_by_value.type
	  AND value=get_code_by_value.value;

	return code;

END get_code_by_value ;

/* PROCEDURE VALIDATE_CODE *****************************************************
 */
PROCEDURE validate_code (
	code 	IN VARCHAR2,
	type 	IN VARCHAR2
)
IS
    value	VARCHAR2(100);
BEGIN

    SELECT value into validate_code.value
    FROM code_map
    WHERE code=validate_code.code and type=validate_code.type;

END validate_code;

/* FUNCTION GET_VALUE_BY_CODE **************************************************
 */
FUNCTION get_value_by_code (
	code 	IN VARCHAR2,
	type 	IN VARCHAR2
) RETURN VARCHAR2
IS
    value	VARCHAR2(50);
BEGIN

	SELECT value INTO get_value_by_code.value FROM code_map
 	WHERE type = get_value_by_code.type
	  AND code=get_value_by_code.code;

	return value;

END get_value_by_code ;

/* FUNCTION GET_PROCEDURE_NAME_BY_IC *******************************************
 */
FUNCTION get_procedure_name_by_ic (
	ic_name 	   IN VARCHAR2
) RETURN VARCHAR2
IS
    name	VARCHAR2(50);
BEGIN

	SELECT value INTO name FROM code_map
 	WHERE lower(type) in ('integrity_check')
	  AND code=get_procedure_name_by_ic.ic_name;

	return name;

END get_procedure_name_by_ic;

/* FUNCTION GET_IC_BY_PROCEDURE_NAME *******************************************
 */
FUNCTION get_ic_by_procedure_name (
	procedure_name		  IN VARCHAR2
) RETURN VARCHAR2
IS
    name	VARCHAR2(50);
BEGIN

	SELECT code INTO name FROM code_map
 	WHERE lower(type) in ('integrity_check')
	  AND lower(value)=lower(get_ic_by_procedure_name.procedure_name);

	return name;

END get_ic_by_procedure_name;

/* FUNCTION GET_TABLE_NAME_BY_CODE *********************************************
 */
FUNCTION get_table_name_by_code (
	code		IN VARCHAR2
) RETURN VARCHAR2

IS
    name	VARCHAR2(50);
BEGIN

	SELECT value INTO name FROM code_map
 	WHERE lower(type)='table_name'
	  AND lower(code)=lower(get_table_name_by_code.code);

	return name;

END get_table_name_by_code;

/* FUNCTION GET_MOLECULAR_ACTION_BY_CODE ***************************************
 */
FUNCTION get_molecular_action_by_code (
	code		IN VARCHAR2
) RETURN VARCHAR2

IS
    action	VARCHAR2(50);
BEGIN

	SELECT value INTO action FROM code_map
 	WHERE lower(type)='molecular_action'
	AND code=get_molecular_action_by_code.code;

	return action;

END get_molecular_action_by_code;

/* FUNCTION GET_CODE_BY_MOLECULAR_ACTION ***************************************
 */
FUNCTION get_code_by_molecular_action (
	action		  IN VARCHAR2
) RETURN VARCHAR2

IS
    code	VARCHAR2(50);
BEGIN

	SELECT code INTO get_code_by_molecular_action.code FROM code_map
 	WHERE lower(type)='molecular_action'
	AND value=action;

	return code;

END get_code_by_molecular_action;

/* FUNCTION GET_IC_BY_NUM ******************************************************
 */
FUNCTION get_ic_by_num(
	iv		VARCHAR2,
	ic_num		VARCHAR2
)
RETURN VARCHAR2
IS
	ic_loc 		INTEGER;
BEGIN
	IF ic_num=0 OR iv='' THEN
	    RETURN '';
	END IF;

	ic_loc := INSTR(iv,'<',1,ic_num);

	-- returns null if ic_num exceeds max number of ic(ic_loc=0)
	if ic_loc = 0 then
	    RETURN '';
	end if;

	RETURN SUBSTR(iv,ic_loc+1,INSTR(iv,'>',ic_loc,1)-ic_loc-1);

-- Pass exceptions along

END get_ic_by_num;

/* FUNCTION GET_IC_CODE ********************************************************
 */
FUNCTION get_ic_code(
	iv		VARCHAR2,
	ic_name		VARCHAR2
)
RETURN VARCHAR2

IS
	ic_loc 		INTEGER;
BEGIN
	ic_loc := INSTR(iv,ic_name,1,1);
	ic_loc := INSTR(iv,':',ic_loc,1)+1;
	RETURN SUBSTR(iv,ic_loc,(INSTR(iv,'>',ic_loc,1))-ic_loc);

END get_ic_code;

/* FUNCTION GET_IC_CODE_BY_NUM *************************************************
 */
FUNCTION get_ic_code_by_num(
	iv		VARCHAR2,
	ic_num		VARCHAR2
)
RETURN VARCHAR2
IS
	ic_loc 		INTEGER;
BEGIN

	IF ic_num=0 OR iv='' THEN
	    RETURN '';
	END IF;

	ic_loc := INSTR(iv,':',1,ic_num);

	-- returns null if ic_num exceeds max number of ic(ic_loc=0)
	if ic_loc = 0 then
	    RETURN '';
	end if;

	RETURN SUBSTR(iv,ic_loc+1,INSTR(iv,'>',ic_loc,1)-ic_loc-1);

END get_ic_code_by_num;

/* FUNCTION GET_IC_NAME_BY_NUM *************************************************
 */
FUNCTION get_ic_name_by_num(
	iv		VARCHAR2,
	ic_num		VARCHAR2
)
RETURN VARCHAR2
IS
	ic_loc 		INTEGER;
BEGIN
	IF ic_num=0 OR iv='' THEN
	    RETURN '';
	END IF;

	ic_loc := INSTR(iv,'<',1,ic_num);

	-- returns null if ic_num exceeds max number of ic(ic_loc=0)
	if ic_loc = 0 then
	    RETURN '';
	end if;

	RETURN SUBSTR(iv,ic_loc+1,INSTR(iv,':',ic_loc,1)-ic_loc-1);

END get_ic_name_by_num;

/* FUNCTION GET_IC_STATE *******************************************************
 */
FUNCTION get_ic_state(
	ic_code		VARCHAR2
)
RETURN VARCHAR2
IS
	ic_state	VARCHAR2(10);
BEGIN
	IF ic_code='' THEN
	    RETURN '';
	END IF;

	ic_state := get_value_by_code(ic_code,'ic_state');

	RETURN ic_state;

EXCEPTION
   WHEN OTHERS THEN
      RETURN ic_code;

END get_ic_state;

/* FUNCTION GET_FIELD_TYPE *****************************************************
 */
FUNCTION get_field_type(
	tab_name	IN VARCHAR2,
	col_name	IN VARCHAR2
)
RETURN VARCHAR2
IS
	field_type 	VARCHAR2(20);
BEGIN

    SELECT data_type INTO field_type FROM user_tab_columns
    WHERE table_name = UPPER(tab_name)
    AND column_name = UPPER(col_name);

    RETURN field_type;

EXCEPTION

    WHEN OTHERS THEN
	meme_utility_error('get_field_type','0',10,
		tab_name || ',' || col_name );
	RETURN MEME_CONSTANTS.FIELD_NOT_FOUND;

END get_field_type;

/* FUNCTION GET_CURRENT_NAME ***************************************************
 */
FUNCTION get_current_name(
	source	IN VARCHAR2
)
RETURN VARCHAR2
IS
       c_name 	VARCHAR2(40);
BEGIN

    SELECT current_name INTO c_name FROM source_version
    WHERE source = UPPER(get_current_name.source);

    RETURN c_name;

EXCEPTION
    WHEN OTHERS THEN
	RETURN '';

END get_current_name;

/* FUNCTION GET_PREVIOUS_NAME **************************************************
 */
FUNCTION get_previous_name(
	source	IN VARCHAR2
)
RETURN VARCHAR2
IS
       p_name 	VARCHAR2(40);
BEGIN

    SELECT previous_name INTO p_name FROM source_version
    WHERE source = UPPER(get_previous_name.source);

    RETURN p_name;

EXCEPTION
    WHEN OTHERS THEN
	RETURN '';

END get_previous_name;

/* FUNCTION GET_INTEGRITY_VECTOR ***********************************************
 */
FUNCTION get_integrity_vector(
	application	IN VARCHAR2
)
RETURN VARCHAR2
IS
       iv 	VARCHAR2(2000);
BEGIN

    SELECT integrity_vector INTO iv FROM ic_applications
    WHERE application = get_integrity_vector.application;

    RETURN iv;

EXCEPTION
    WHEN OTHERS THEN
	RETURN '';

END get_integrity_vector;

/* FUNCTION OBJECT_EXISTS ******************************************************
 */
FUNCTION object_exists(
	type		IN VARCHAR2,
	name		IN VARCHAR2
)
RETURN INTEGER
IS

	obj_name	VARCHAR2(50);
	NO_TYPE_FOUND	EXCEPTION;
BEGIN

    IF LOWER(type) NOT IN ('table','index','view','sequence','cluster','package') THEN
	RAISE NO_TYPE_FOUND;
    END IF;

    SELECT object_name INTO obj_name FROM USER_OBJECTS
    WHERE object_name = UPPER(name);

    -- goes to NO_DATA_FOUND exception if object_name not found
    RETURN 1;

EXCEPTION

    WHEN NO_TYPE_FOUND THEN
	RETURN 0;

    WHEN NO_DATA_FOUND THEN
	RETURN 0;

END object_exists;

/* FUNCTION TABLE_TO_STRING ****************************************************
 * This procedure takes a table name which has a single
 * varchar2 colum, and it concatenates the values into a
 * comma separated list
 */
FUNCTION table_to_string (
	table_name	IN VARCHAR2
)
RETURN VARCHAR2
IS
    c			INTEGER;	-- cursor
    ret			INTEGER;
    result_string 	VARCHAR2(2000);
    col			VARCHAR2(256);
    query		VARCHAR2(512);
BEGIN

    initialize_trace('table_to_string');

    query := 'SELECT * FROM ' || table_name;
    result_string := '';

    IF meme_utility_trace = TRUE THEN
	put_message (query);
    END IF;

    location := '0';
    c:= DBMS_SQL.OPEN_CURSOR;

    location := '10';
    DBMS_SQL.PARSE(c, query, DBMS_SQL.NATIVE);

    location := '20';
    DBMS_SQL.DEFINE_COLUMN(c, 1, col,2000);

    location := '30';
    ret := DBMS_SQL.EXECUTE(c);

    LOOP
    	location := '40';
    	ret := DBMS_SQL.FETCH_ROWS(c);
	EXIT WHEN ret = 0;

    	location := '50';
    	DBMS_SQL.COLUMN_VALUE(c,1,col);

	result_string := result_string || ',' || col;

    END LOOP;

    location := '60';
    DBMS_SQL.CLOSE_CURSOR(c);

    RETURN LTRIM(result_string,',');

EXCEPTION

    WHEN OTHERS THEN
	meme_utility_error (method, location, 20, SQLERRM);
	RAISE meme_utility_exception;

END table_to_string;

/* FUNCTION GET_NEXT_ID ********************************************************
 * This function takes a table_name. It updates the
 * corresonding row of max_tab and returns the next
 * available id.  this fn assumes AUTOCOMMIT is off.
 */
FUNCTION get_next_id (
	table_name	IN VARCHAR2
)
RETURN INTEGER
IS
BEGIN

    exec(
	'UPDATE max_tab SET max_id=max_id+1 ' ||
	'WHERE table_name = ''' || table_name || '''');

    return exec_select(
	'SELECT max_id FROM max_tab ' ||
	'WHERE table_name = ''' || table_name || '''');

-- Pass exceptions along

END get_next_id;

/* FUNCTION LOG_OPERATION ******************************************************
 * This function logs an operation in activity_log
 */
FUNCTION log_operation (
	authority	IN VARCHAR2,
	activity	IN VARCHAR2,
	detail		IN VARCHAR2,
	transaction_id	IN INTEGER,
	work_id		IN INTEGER,
	elapsed_time	IN INTEGER DEFAULT 0
)
RETURN INTEGER
IS
BEGIN
    log_operation(
	authority,activity,detail,
	transaction_id,work_id,log_operation.elapsed_time);
    RETURN 0;
EXCEPTION
    WHEN OTHERS THEN
	RETURN -1;
END;

/* PROCEDURE LOG_OPERATION *****************************************************
 */
PROCEDURE log_operation (
	authority	IN VARCHAR2,
	activity	IN VARCHAR2,
	detail		IN VARCHAR2,
	transaction_id	IN INTEGER,
	work_id		IN INTEGER,
	elapsed_time	IN INTEGER DEFAULT 0
)
IS
    local_t_id		INTEGER;
    row_sequence	INTEGER;
    msg			VARCHAR2(3000);
BEGIN

    initialize_trace('log_operation (2)');
    err_msg := SUBSTR('(' || transaction_id || ',' || work_id || ',' ||
	       authority || ',' || activity || ',' || detail || ')',1,3000);

    local_t_id := transaction_id;
    IF transaction_id = 0 THEN
	location := '0';
	local_t_id := get_next_id('TRANSACTIONS');
    END IF;

    IF work_id != 0 THEN
	location := '10';
	INSERT INTO activity_log
	    (row_sequence,transaction_id, work_id, authority,
	     elapsed_time, timestamp, activity, detail)
	SELECT
	    NVL(max(row_sequence+1),1),
	    log_operation.transaction_id, log_operation.work_id,
	    log_operation.authority, log_operation.elapsed_time,
	    SYSDATE, log_operation.activity, SUBSTR(log_operation.detail,1,4000)
	FROM activity_log
	WHERE work_id = log_operation.work_id;
    ELSE
	location := '20';
	INSERT INTO activity_log
	    (transaction_id, work_id, authority, elapsed_time,
	     timestamp, activity, detail)
    	VALUES
    	    (local_t_id, work_id, authority, log_operation.elapsed_time,
	    SYSDATE, activity, SUBSTR(detail,1,4000));
    END IF;

EXCEPTION
    WHEN OTHERS THEN
	ROLLBACK;
	meme_utility_error(method,location,1, err_msg || ': ' || SQLERRM);
	RAISE meme_utility_exception;


END log_operation;

/* PROCEDURE LOG_PROGRESS ******************************************************
 * This procedure should have an autonymous transaction scope
 * in oracle 8i to commit while running.
 */
PROCEDURE log_progress (
	authority	IN VARCHAR2,
	activity	IN VARCHAR2,
	detail		IN VARCHAR2,
	transaction_id	IN INTEGER,
	work_id		IN INTEGER,
	elapsed_time	IN INTEGER DEFAULT 0,
	progress	IN INTEGER DEFAULT 0
)
IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    row_sequence	INTEGER;

BEGIN

    -- this block needs to have an autonomous transaction scope

    -- If work_id != 0 then use incremental sequence numbers
    IF work_id != 0 THEN

	INSERT INTO meme_progress
	    (row_sequence,transaction_id, work_id, authority,
	     elapsed_time, timestamp, activity, detail, progress)
	SELECT
	    NVL(max(row_sequence+1),1),
	    log_progress.transaction_id, log_progress.work_id,
	    log_progress.authority, log_progress.elapsed_time,
	    SYSDATE, log_progress.activity, log_progress.detail,
	    log_progress.progress
	FROM meme_progress
	WHERE work_id = log_progress.work_id;
    ELSE
	INSERT INTO meme_progress
	    (transaction_id, work_id, authority, elapsed_time,
	     timestamp, activity, detail,progress)
	VALUES
	    (transaction_id, work_id, authority, log_progress.elapsed_time,
	    SYSDATE, activity, detail, progress);
    END IF;

    COMMIT;

END log_progress;

/* PROCEDURE RESET_PROGRESS ****************************************************
 */
PROCEDURE reset_progress (
	work_id		IN INTEGER
)

IS
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN

    DELETE FROM meme_progress WHERE work_id = reset_progress.work_id;

    COMMIT;

END reset_progress;

/* FUNCTION NEW_WORK ***********************************************************
 */
FUNCTION new_work (
	authority 	IN VARCHAR2,
	type		IN VARCHAR2,
	description	IN VARCHAR2
) RETURN INTEGER
IS
    work_id	INTEGER;
    ct          INTEGER;
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN

    initialize_trace ('new_work');
    err_msg := 'type = ' || type;

    validate_code(type,'work_type');


    location := '25';
    SELECT count(*) into ct FROM meme_work
    WHERE authority=new_work.authority AND type=new_work.type
    AND description=new_work.description;

    if ct > 0 then
        SELECT max(work_id) into work_id FROM meme_work
        WHERE authority=new_work.authority AND type=new_work.type
        AND description=new_work.description;    
        return work_id;
    end if;

    location := '25';
    work_id := get_next_id('WORK');

    location := '30';
    INSERT into meme_work (work_id, timestamp,type,authority,description)
    values (new_work.work_id, SYSDATE, type, authority, description);

    COMMIT;

    return work_id;

EXCEPTION
    WHEN OTHERS THEN
	IF location = '0' THEN
	    meme_utility_error(method, location, 30, err_msg);
	ELSE
	    meme_utility_error(method, location, 1,
		err_msg || ': ' || SQLERRM);
	END IF;
	RAISE meme_utility_exception;

END new_work;

/* FUNCTION NEW_WORK_ID ********************************************************
 */
FUNCTION new_work_id
RETURN INTEGER
IS
    work_id	INTEGER;
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN

    initialize_trace('new_work_id');

    location := '10';
    work_id := get_next_id('WORK');

    COMMIT;

    return work_id;

EXCEPTION
    WHEN OTHERS THEN
	meme_utility_error(method, location, 1, SQLERRM);
	RAISE meme_utility_exception;

END new_work_id;

/* PROCEDURE PUT_ERROR *********************************************************
 */
PROCEDURE put_error (
	err_msg		IN VARCHAR2,
	authority	IN VARCHAR2 := NULL,
	transaction_id	IN INTEGER := 0,
	work_id		IN INTEGER := 0,
	elapsed_time	IN INTEGER DEFAULT 0
)
IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    msg 	VARCHAR2(4000);
    i 		INTEGER;
BEGIN

    msg := 'MEME_ERROR => ' ||
			 '[' || SYSDATE || '] ' ||
			 err_msg;

    i := 0;
    LOOP
	EXIT WHEN i > length(msg);
    	DBMS_OUTPUT.PUT_LINE(substr(msg,i,MEME_CONSTANTS.LINE_BREAK));
	i := i + MEME_CONSTANTS.LINE_BREAK+1;
    END LOOP;

    INSERT INTO meme_error
	(timestamp, detail, authority, elapsed_time,
	 transaction_id, work_id)
    VALUES
	(SYSDATE, err_msg, put_error.authority,
	 put_error.elapsed_time,
	 put_error.transaction_id,
	 put_error.work_id);
    COMMIT;

END put_error;

/* PROCEDURE PUT_APPLICATION_ERROR *********************************************
 */
PROCEDURE put_application_error (
	err_msg		IN VARCHAR2
)

IS
    local_err_msg	VARCHAR2(1000);
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN

    local_err_msg := 'MEME_ERROR => ' ||
			 '[' || SYSDATE || '] ' ||
			 err_msg;

    MEME_UTILITY.PUT_ERROR(err_msg);

    RAISE_APPLICATION_ERROR(MEME_CONSTANTS.APPLICATION_ERROR_NUMBER,
			    local_err_msg);

    COMMIT;

END put_application_error;

/* PROCEDURE PUT_MESSAGE *******************************************************
 */
PROCEDURE put_message (
	message		IN VARCHAR2
)

IS
    msg 	VARCHAR2(4000);
    i 		INTEGER;

BEGIN

    msg := ('[' || SYSDATE || '] ' || message);

    i := 0;
    LOOP
	EXIT WHEN i > length(msg);
    	DBMS_OUTPUT.PUT_LINE(substr(msg,i,MEME_CONSTANTS.LINE_BREAK));
	i := i + MEME_CONSTANTS.LINE_BREAK+1;
    END LOOP;

END put_message;


/* PROCEDURE TIMING_START ******************************************************
 */
PROCEDURE timing_start
IS
BEGIN
    start_time := DBMS_UTILITY.GET_TIME;
END timing_start;

/* PROCEDURE TIMING_STOP *******************************************************
 */
PROCEDURE timing_stop
IS
BEGIN
    stop_time := DBMS_UTILITY.GET_TIME;
END timing_stop;


/* FUNCTION ELAPSED_TIME *******************************************************
 */
FUNCTION elapsed_time RETURN INTEGER
IS
BEGIN
    return ((stop_time - start_time) * 10);
END elapsed_time;

/* PROCEDURE SUB_TIMING_START **************************************************
 */
PROCEDURE sub_timing_start
IS
BEGIN
    sub_start_time := DBMS_UTILITY.GET_TIME;
END sub_timing_start;

/* PROCEDURE SUB_TIMING_STOP ***************************************************
 */
PROCEDURE sub_timing_stop
IS
BEGIN
    sub_stop_time := DBMS_UTILITY.GET_TIME;
END sub_timing_stop;

/* FUNCTION SUB_ELAPSED_TIME ***************************************************
 */
FUNCTION sub_elapsed_time RETURN INTEGER
IS
BEGIN
    return ((sub_stop_time - sub_start_time) * 10);
END sub_elapsed_time;

/* FUNCTION CLUSTER_SINGLE *****************************************************
 */
FUNCTION cluster_single( table_name IN VARCHAR2 )
RETURN VARCHAR2
IS
    cluster_table	VARCHAR2(50);
    field_type		VARCHAR2(50);
BEGIN

    initialize_trace('cluster_single');
    location := '0';

    /* check that table_name has a concept_id field */
    IF meme_utility_debug = FALSE AND
	get_field_type(upper(table_name),'CONCEPT_ID') != 'NUMBER'
    THEN

	meme_utility_error(method,'1',0, 'Table ' || table_name ||
		' does not have an integer concept_id field');
	RAISE MEME_UTILITY_EXCEPTION;
    END IF;

    location := '10';
    cluster_table := get_unique_tablename;
    exec
	('CREATE TABLE ' || cluster_table || ' AS ' ||
	 'SELECT distinct concept_id, rownum as cluster_id ' ||
	 'FROM ' || table_name);

    return cluster_table;
EXCEPTION

    WHEN OTHERS THEN
	meme_utility_error(method, location, 1, SQLERRM);
	RAISE meme_utility_exception;

END cluster_single;

/* FUNCTION CLUSTER_PAIR *******************************************************
 */
FUNCTION cluster_pair( table_name IN VARCHAR2 )
RETURN VARCHAR2
IS
    cluster_table	VARCHAR2(50);
BEGIN

    initialize_trace('cluster_pair');

    /* check that table_name has a concept_id field */
    location := '0';
    IF meme_utility_debug = FALSE AND
	(get_field_type(upper(table_name),'CONCEPT_ID_1') != 'NUMBER' OR
	 get_field_type(upper(table_name),'CONCEPT_ID_2') != 'NUMBER')
    THEN
	meme_utility_error(method,'1',0,
	'Table ' || table_name || ' does not have ' ||
	'NUMBER concept_id_1 and concept_id_2 fields.');
	RAISE MEME_UTILITY_EXCEPTION;
    END IF;

    location := '10';

    cluster_table := get_unique_tablename;
    exec
	('CREATE TABLE ' || cluster_table || ' AS ' ||
	 'SELECT concept_id_1 as concept_id, rownum as cluster_id ' ||
	 'FROM ' || table_name || ' ' ||
	 'UNION ' ||
	 'SELECT concept_id_2, rownum ' ||
	 'FROM ' || table_name
	);

    return cluster_table;

EXCEPTION

    WHEN OTHERS THEN
	drop_it('table',cluster_table);
	meme_utility_error(method, location, 1, SQLERRM);
	RAISE meme_utility_exception;

END cluster_pair;

/* FUNCTION CLUSTER_PAIR_RECURSIVE*****************************************
 *
 * This method takes concept_id_1, concept_id_2 and produces
 * concept_id, cluster_id for each connected set of related
 * concepts.  Cycles are allowed in the initial set and the
 * order of concept ids does not matter.
 */
FUNCTION cluster_pair_recursive( table_name IN VARCHAR2 )
RETURN VARCHAR2
IS
    cluster_table	VARCHAR2(50);
    rowct		NUMBER;
BEGIN

    initialize_trace('cluster_pair_recursive');

    /* check that table_name has a concept_id field */
    location := '0';
    IF meme_utility_debug = FALSE AND
	(get_field_type(upper(table_name),'CONCEPT_ID_1') != 'NUMBER' OR
	 get_field_type(upper(table_name),'CONCEPT_ID_2') != 'NUMBER')
    THEN
	meme_utility_error(method,'1',0,
	'Table ' || table_name || ' does not have ' ||
	'NUMBER concept_id_1 and concept_id_2 fields.');
	RAISE MEME_UTILITY_EXCEPTION;
    END IF;

    location := '10';

    cluster_table := get_unique_tablename;

    location := '20'; 
    EXECUTE IMMEDIATE
	'CREATE TABLE ' || cluster_table || ' AS
	 SELECT concept_id, row_num as cluster_id FROM
	 (SELECT concept_id_1 as concept_id, rownum as row_num
	  FROM ' || table_name || ')
	 UNION 
	 (SELECT concept_id_2, rownum
	 FROM ' || table_name || ')    ';
  
    LOOP
	-- set cluster id to the min of the cluster ids of those other
	-- concepts whose cluster ids match this one.
        -- set cluster id to the min of the cluster ids of those other
        -- concepts whose cluster ids match this one.
        location := '40';
        drop_it('table',cluster_table || '_1');
        location := '40.1';
        EXECUTE IMMEDIATE
            'CREATE TABLE ' || cluster_table || '_1 AS
             SELECT a.concept_id, min(g_par.cluster_id) cluster_id
             FROM ' || cluster_table || ' a, ' || cluster_table || ' par,
                  ' || cluster_table || ' g_par
             WHERE par.cluster_id = a.cluster_id
               AND par.concept_id = g_par.concept_id
             GROUP BY a.concept_id';

        -- Exit when this table matches cluster table
        location := '40.11';
        EXIT when exec_select(
              'SELECT count(*) ct FROM (SELECT * FROM '||
                 cluster_table||'_1 MINUS SELECT * FROM '||
                 cluster_table||' )') = 0;

        location := '40.2';
        drop_it('table',cluster_table||'_2');
        location := '40.3';
        EXECUTE IMMEDIATE
            'CREATE TABLE ' || cluster_table || '_2 AS
             SELECT DISTINCT a.concept_id, b.cluster_id
             FROM ' || cluster_table || ' a, ' || cluster_table || '_1 b
             WHERE a.concept_id = b.concept_id';

        location := '40.4';
        drop_it('table',cluster_table);
        location := '40.5';
        EXECUTE IMMEDIATE
            'CREATE TABLE ' || cluster_table || ' AS
             SELECT * FROM ' || cluster_table || '_2';

        location := '40.6';
        drop_it('table',cluster_table||'_2');

    END LOOP;	

    location := '50';
    drop_it('table',cluster_table||'_1');
    location := '60';
    EXECUTE IMMEDIATE
	'CREATE TABLE ' || cluster_table|| '_1 AS
	 SELECT DISTINCT concept_id, cluster_id 
	 FROM ' || cluster_table; 	

    location := '70';
    drop_it('table',cluster_table);

    return cluster_table || '_1';

EXCEPTION

    WHEN OTHERS THEN
	drop_it('table',cluster_table);
	drop_it('table',cluster_table||'_1');
	meme_utility_error(method, location, 1, SQLERRM);
	RAISE meme_utility_exception;

END cluster_pair_recursive;

/* FUNCTION RECLUSTER **********************************************************
 */
FUNCTION recluster( table_name IN VARCHAR2 )
RETURN VARCHAR2
IS
    cluster_table1	VARCHAR2(50);
    cluster_table2	VARCHAR2(50);
BEGIN

    initialize_trace('recluster');
    /* check that table_name has a concept_id=INTEGER field */
    /* and a cluster_id=varchar2 field */
    location := '0';
    IF meme_utility_debug = FALSE AND
	(get_field_type(table_name,'concept_id') != 'NUMBER' OR
	 get_field_type(table_name,'cluster_id') != 'VARCHAR2')
    THEN
	meme_utility_error(method, location, 0,
	'Table ' || table_name || ' does not have an ' ||
	'integer concept_id and a varchar2 cluster_id field.');
	RAISE meme_utility_exception;
    END IF;

    location := '10';
    cluster_table1 := get_unique_tablename;
    exec
	('CREATE TABLE ' || cluster_table1 || ' AS ' ||
	 'SELECT cluster_id as old_cluster_id, rownum as cluster_id ' ||
	 'FROM (SELECT distinct cluster_id FROM ' || table_name || ')'
	);

    location := '20';
    cluster_table2 := get_unique_tablename;
    exec
	('CREATE TABLE ' || cluster_table2 || ' AS ' ||
	 'SELECT concept_id, b.cluster_id ' ||
	 'FROM ' || table_name || ' a, ' || cluster_table1 || ' b ' ||
	 'WHERE a.cluster_id = old_cluster_id'
	);

    location := '40';
    drop_it('table',cluster_table1);

    return cluster_table2;

EXCEPTION

    WHEN OTHERS THEN
	drop_it('table',cluster_table1);
	drop_it('table',cluster_table2);
	meme_utility_error(method, location, 1, SQLERRM);
	RAISE meme_utility_exception;

END recluster;


/* FUNCTION MD5 **************************************************************
 */
FUNCTION md5 (str IN VARCHAR2) 
RETURN VARCHAR2
IS
BEGIN
    return lower(rawtohex(utl_raw.cast_to_raw(dbms_obfuscation_toolkit.md5(
	input_string => str))));
END;



/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help
IS
BEGIN
    help('');
END help;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help ( topic IN VARCHAR2 )
IS
BEGIN
    -- This procedure requires SET SERVEROUTPUT ON
   DBMS_OUTPUT.PUT_LINE('.');

   IF topic IS NULL OR topic = '' THEN
      DBMS_OUTPUT.PUT_LINE('.This package provides utility functions used by the ');
      DBMS_OUTPUT.PUT_LINE('.other MEME packages.');
      DBMS_OUTPUT.PUT_LINE('.');
      DBMS_OUTPUT.PUT_LINE('. count_row_id:		Check if row_id in CRACS table.');
      DBMS_OUTPUT.PUT_LINE('. drop_it:			Drop database object.');
      DBMS_OUTPUT.PUT_LINE('. exec:			Execute dynamic SQL.');
      DBMS_OUTPUT.PUT_LINE('. ddl_exec:			Execute dynamic DDL w/o commit.');
      DBMS_OUTPUT.PUT_LINE('. exec_select:		Returns integer from 1 row 1 col query.');
      DBMS_OUTPUT.PUT_LINE('. exec_select_varchar:	Returns varchar from 1 row 1 col query.');
      DBMS_OUTPUT.PUT_LINE('. exec_count:		Returns count(*) of a table.');
      DBMS_OUTPUT.PUT_LINE('. exec_plsql:		Execute dynamic PL/SQL.');
      DBMS_OUTPUT.PUT_LINE('. table_to_string:		Convert single-column table to');
      DBMS_OUTPUT.PUT_LINE('. 				a comma separated value list.');
      DBMS_OUTPUT.PUT_LINE('. get_next_id:		Get next id from max_tab.');
      DBMS_OUTPUT.PUT_LINE('. get_unique_tablename:	Get a unique table name.');
      DBMS_OUTPUT.PUT_LINE('. strip_string:		Get base string of a bracket term.');
      DBMS_OUTPUT.PUT_LINE('. get_bracket_number:	Get bracket number from a bracket term.');
      DBMS_OUTPUT.PUT_LINE('. validate_code:		Verify that a (code,type) pair is valid.');
      DBMS_OUTPUT.PUT_LINE('. get_value_by_code:	Map a code to a value in code_map.');
      DBMS_OUTPUT.PUT_LINE('. get_code_by_value:	Map a value to a code in code_map.');
      DBMS_OUTPUT.PUT_LINE('. get_procedure_name_by_ic:	Get the procedure name corresponding with');
      DBMS_OUTPUT.PUT_LINE('. 				an integrity check.');
      DBMS_OUTPUT.PUT_LINE('. get_ic_by_procedure_name:	Inverse of get_procedure_name_by_ic.');
      DBMS_OUTPUT.PUT_LINE('. get_table_name_by_code:	Map table name code to a table name.');
      DBMS_OUTPUT.PUT_LINE('. get_molecular_action_by_code:	Map code to a molecular action.');
      DBMS_OUTPUT.PUT_LINE('. get_code_by_molecular_action:	Inverse of above.');
      DBMS_OUTPUT.PUT_LINE('. get_ic_by_num:		Get n-th integrity check in a vector.');
      DBMS_OUTPUT.PUT_LINE('. get_ic_code:		Get the code part of an integrity check.');
      DBMS_OUTPUT.PUT_LINE('. get_ic_code_by_num:	Get the code for the n-th check in a vector.');
      DBMS_OUTPUT.PUT_LINE('. get_ic_name_by_num:	Get the name for the n-th check in a vector.');
      DBMS_OUTPUT.PUT_LINE('. get_ic_state:		Get the ic_state corresponding to a code..');
      DBMS_OUTPUT.PUT_LINE('. get_field_type:		Look up field type in data dictionary.');
      DBMS_OUTPUT.PUT_LINE('. get_current_name:		Get current version of a stripped source .');
      DBMS_OUTPUT.PUT_LINE('. get_previous_name:	Get previous version of a stipped source.');
      DBMS_OUTPUT.PUT_LINE('. get_integrity_vector:	Get integrity vector for an application.');
      DBMS_OUTPUT.PUT_LINE('. object_exists:		Check if an object exists.');
      DBMS_OUTPUT.PUT_LINE('. log_operation:		Log operation in activity_log.');
      DBMS_OUTPUT.PUT_LINE('. log_progress:		Log progress of long-running application.');
      DBMS_OUTPUT.PUT_LINE('. reset_progress:		Clear out meme_progress table.');
      DBMS_OUTPUT.PUT_LINE('. new_work:			Register work in meme_work, get a work_id.');
      DBMS_OUTPUT.PUT_LINE('. new_work_id:		Get next work_id.');
      DBMS_OUTPUT.PUT_LINE('. put_error:		Report an error, log it in meme_error.');
      DBMS_OUTPUT.PUT_LINE('. put_application_error:	Report an application error.');
      DBMS_OUTPUT.PUT_LINE('. put_message:		Output to the DBMS_OUTPUT buffer.');
      DBMS_OUTPUT.PUT_LINE('. timing_start:		Start timing an operation.');
      DBMS_OUTPUT.PUT_LINE('. timing_stop:		Stop timing an operation..');
      DBMS_OUTPUT.PUT_LINE('. elapsed_time:		Return stop time - start time.');
      DBMS_OUTPUT.PUT_LINE('. sub_timing_start:		Start timing a sub-operation.');
      DBMS_OUTPUT.PUT_LINE('. sub_timing_stop:		Stop timing a sub-operation.');
      DBMS_OUTPUT.PUT_LINE('. sub_elapsed_time:		Return sub-stop time - sub-start time.');
      DBMS_OUTPUT.PUT_LINE('. cluster_single:		Convert concept_id => concept_id,cluster_id.');
      DBMS_OUTPUT.PUT_LINE('. cluster_pair:		Convert concept_id_1,concept_id_2 =>');
      DBMS_OUTPUT.PUT_LINE('. 				   concept_id, cluster_id.');
      DBMS_OUTPUT.PUT_LINE('. cluster_pair_recursive:	Convert concept_id_1,concept_id_2 =>');
      DBMS_OUTPUT.PUT_LINE('. 				   concept_id, cluster_id.');
      DBMS_OUTPUT.PUT_LINE('. recluster:		Convert concept_id, cluster_id (vchar) =>');
      DBMS_OUTPUT.PUT_LINE('. 				   concept_id, cluster_id (int).');
   ELSE
      DBMS_OUTPUT.PUT_LINE('.There is no help for the topic: "' || topic || '".');
   END IF;

    -- Print version
    MEME_UTILITY.PUT_MESSAGE('.');
    version;
END help;

END meme_utility;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_UTILITY.help;
execute MEME_UTILITY.register_package;

