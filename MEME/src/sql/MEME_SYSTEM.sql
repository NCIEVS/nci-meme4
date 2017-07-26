CREATE OR REPLACE PACKAGE MEME_SYSTEM AS
/*******************************************************************************
 *
 * PL/SQL File: MEME_SYSTEM.sql
 *
 * This package contains all the utilities
 * that used by other MEME software.
 *
 * Version Information
 * 06/22/2009 BAC (1-MD04R): meme_indexes has UNIQUENESS
 *  08/29/2006 SL (1-C17ND)  Adding Oracle10g performance like analyze staments
 * 03/25/2005 4.20.0: Released
 * 03/23/2005 4.19.1: No longer COALESCE tablespaces
 * 06/14/2004 4.19.0: Gather stats using DBMS_STATS instead of analyze table.
 * 05/03/2004 4.18.0: Fixed to work with oracle 9.2
 * 04/19/2004 4.17.0: BITMAP indexes(requires additional meme_indexes field)
 * 04/06/2004 4.16.2: BITMAP indexes(requires additional meme_indexes field)
 * 04/02/2004 3.16.1: reindex and rebuild support compute_stats param
 * 01/20/2004 3.16.0: Released
 * 01/15/2004 3.15.3: rebuild_table supports partitioned tables
 * 01/08/2004 3.15.2: when rebuilding table max initial extent is 1G
 * 12/30/2003 3.15.1: analyzing is done in parallel fashion
 * 06/19/2003 3.15.0: reindex no longer uses nologging
 * 06/05/2003 3.14.0: dump_ctl_file dumps correctly for varchar fields>255 
 * 12/11/2002 3.13.0: rebuild_table defragments better now. 
 * 12/17/2001 3.12.0: reindex was fixed to ignore PCTINCREASE
 * 10/14/2001 3.11.0: RELEASED TO NLM
 * 10/14/2001 3.10.3: Many upgrades:
 *                    1. A parallel flag was added to reindex, reindex_mid, and
 *                       rebuild_table allowing these operations to exploit
 *		 	 parallelism
 *                    2. A rebuild_mid procedure was added to rebuild and 
 *			 reindex all tables
 *                    3. reindex was fixed up to support reindex_flag.  If 
 *			 the index exists and the flag is 'Y', the index is 
 *			 rebuilt instead of recreated.  If the flag is 'Y' 
 *			 and the index does not existd it is created instead 
 *			 of rebuilt.
 * 09/06/2001 3.10.2: Fixes to refresh_meme_indexes and reindex to
 *                    ignore IOT tables.
 * 07/19/2001 3.10.1: Added a rebuild_table procedure that rebuilds a table
 *                    in its designated tablespace (ALTER ... MOVE) and then
 *                    reindexes it 
 * 04/12/2001 3.10.0: Released version
 * 03/29/2001 3.9.1: Changes to dump_file to support any position of field with
 *                   CLOB data type in a table. To support table with multiple
 *                   fields with CLOB data types.
 * 03/28/2001 3.9.0: Released version
 * 03/22/2001 3.8.3: changes dump procedures to support MRD.
 *                   analyze_mid uses meme_tables not meme_indexes
 * 03/20/2001 3.8.2: register_table loads meme_indexes/ind_columsn for
 *                   new table.
 * 03/14/2001 3.8.1: changes to dump_file, dump_schema_script to 
 *                   ignore tables with LOB fields that cannot be handled.
 * 12/14/2000 3.8.0: reindex fix: dont' analyze primary keys first.be explicit
 * 		     about tablespace name
 * 11/29/2000 3.7.1: Fixes to dump_table,dump_schema_Script and dump_ctl_file
 *                   so they would run from the command line.  This involved
 *    BAC            changing UTL_FILE.PUT_LINE into UTL_FILE.PUT and
 *                   UTL_FILE.NEW_LINE calls.  I don't know why this worked.
 * 11/10/2000 3.7.0: Released
 * 11/07/2000 3.6.2: changes in reindex
 * 11/03/2000 3.6.1: Transfer report_table_change to MEME_UTILITY package
 * 10/24/2000 3.6.0: Use of oq_mode before dealing with operations_queue
 *		     report_table_change does not commit.
 * 10/13/2000 3.5.41: reindex: drop index before creating
 * 10/10/2000 3.5.4: reindex uses rebuild_flag='N' by default
 *		     indexes are recreated even if they exist when
 *		     rebuild_flag='N'
 * 9/25/2000 3.5.32: small changes in dump_mid
 * 9/21/2000 3.5.31: changes to report_table_change, sql_str made 4000 chars.
 * 9/19/2000 3.5.3: changes in dump_mid
 * 9/11/2000 3.5.2: changes in dump_table, dump_file, dump_schema_script
 * 9/01/2000 3.5.1: changes in report_table_change
 * 8/01/2000 3.5.0: Package handover version
 * 7/14/2000 3.4.9: dump_schema_script
 * 7/14/2000 3.4.8: dump_file
 * 7/13/2000 3.4.7: dump_file, dump_mid, dump_schema_script,report_table_change
 * 6/30/2000 3.4.6: dump_file, dump_mid, dump_table, cleanup_temporary_tables
 * 6/28/2000 3.4.5: dump_mid, report_table_change (some more changes)
 * 6/28/2000 3.4.4: dump_mid call dump_schema_script
 * 6/28/2000 3.4.4: dump_schema_script created
 * 6/21/2000 3.4.2: dump_file support ctl file and LOB data
 * 6/20/2000 3.4.1: dump_mid support control file and generation of
 *		    load_mid.sch script
 * 6/19/2000 3.4.0: dump_table calls dump_file
 * 6/19/2000 3.4.0: dump_file formerly dump_table support diff file types
 * 6/12/2000 3.3.9: reindex uses COMPUTE STATISTICS PARALLEL NOLOGGING
 * 		    for this to work, must be enough space in data files
 * 6/07/2000 3.3.8: dump_mid, cache, nocache
 * 6/06/2000 3.3.5: dump_mid created
 * 6/06/2000 3.3.5: dump_table created
 * 6/06/2000 3.3.4: report_table_change created
 * 5/30/2000 3.3.3: register,remove_table
 * 5/25/2000 3.3.2: cleanup_temporary_tables can take a prefix
 * 5/15/2000 3.3.1: refresh_meme_indexes
 * 5/9/2000 3.3.0: Released to NLM
 * 4/7/2000 3.2.1: added delete_flag parameter to xplan_id
 * 9/9/1999:   First version created and compiled
 *
 * Status:
 *   Functionality:
 *	Version management not done
 *	Integrity config management
 *
 *   Testing:  DONE
 *   Enhancements:
 *	Track schema changes (e.g. register_table, etc..)
 *	Track system (MEME_SYSTEM_ACTIONS)
 *	Track system (MEME_QA)
 *
 */

   package_name 	       VARCHAR2(25) := 'MEME_SYSTEM';
   release_number	       VARCHAR2(1)  := '4';
   version_number	       VARCHAR2(5)  := '20.0';
   version_date 	       DATE	    := '25-Mar-2005';
   version_authority	       VARCHAR2(3)  := 'BAC';

   meme_system_debug	       BOOLEAN := FALSE;
   meme_system_trace	       BOOLEAN := FALSE;

   location		       VARCHAR2(5);
   method		       VARCHAR2(256);
   err_msg		       VARCHAR2(256);

   -- constants
   TMP_PREFIX_PATTERN	       CONSTANT VARCHAR2(5) := 'T\_%';
   ESCAPE		       CONSTANT VARCHAR2(5) := '\';

   meme_system_exception       EXCEPTION;

   FUNCTION release RETURN INTEGER;
   FUNCTION version RETURN FLOAT;
   FUNCTION version_info RETURN VARCHAR2;

   PRAGMA restrict_references(version_info,WNDS,RNDS,WNPS);

   PROCEDURE version;
   PROCEDURE set_trace_on;
   PROCEDURE set_trace_off;
   PROCEDURE set_debug_on;
   PROCEDURE set_debug_off;

   PROCEDURE trace(
      message		       IN VARCHAR2);

   PROCEDURE local_exec(
      query		       IN VARCHAR2);

   FUNCTION local_exec(
      query		       IN VARCHAR2
   ) RETURN INTEGER;

   PROCEDURE help;

   PROCEDURE help(
      topic		       IN VARCHAR2);
   PROCEDURE self_test;

   PROCEDURE meme_system_error(
      method		       IN VARCHAR2,
      location		       IN VARCHAR2,
      error_code	       IN INTEGER,
      detail		       IN VARCHAR2);

   PROCEDURE initialize_trace(
      method		       IN VARCHAR2);

   -- Version control procedures
   PROCEDURE register_package;

--   FUNCTION enforce_version (release,version,object) RETURN INTEGER;
--

   -- Table management
   PROCEDURE register_table(
     table_name 	       IN VARCHAR2);

   PROCEDURE remove_table(
     table_name 	       IN VARCHAR2);

-- MEME SYSTEM ACTIONS:  used to chagne tables
-- type, table_name, query...

   PROCEDURE xplan(
      query		       IN VARCHAR2);

   PROCEDURE xplan_id(
      statement_id	       IN VARCHAR2,
      delete_flag	       IN VARCHAR2 := MEME_CONSTANTS.NO);

   PROCEDURE drop_indexes(
      table_name	       IN VARCHAR2);

   PROCEDURE reindex(
      table_name    IN VARCHAR2,
      rebuild_flag  IN VARCHAR2 := MEME_CONSTANTS.NO,    
      parallel_flag IN VARCHAR2 := MEME_CONSTANTS.NO,
      compute_stats_flag IN VARCHAR2 := MEME_CONSTANTS.YES);     

   PROCEDURE rebuild_table(
      table_name 	       IN VARCHAR2,
      rebuild_flag  IN VARCHAR2 := MEME_CONSTANTS.NO,    
      parallel_flag IN VARCHAR2 := MEME_CONSTANTS.NO,
      compute_stats_flag IN VARCHAR2 := MEME_CONSTANTS.YES);     

   PROCEDURE reindex_mid(
      rebuild_flag  IN VARCHAR2 := MEME_CONSTANTS.NO,    
      parallel_flag IN VARCHAR2 := MEME_CONSTANTS.NO,
      compute_stats_flag IN VARCHAR2 := MEME_CONSTANTS.YES);     

   PROCEDURE rebuild_mid(
      rebuild_flag  IN VARCHAR2 := MEME_CONSTANTS.NO,    
      parallel_flag IN VARCHAR2 := MEME_CONSTANTS.NO,
      compute_stats_flag IN VARCHAR2 := MEME_CONSTANTS.YES);     

   PROCEDURE refresh_meme_indexes;

   PROCEDURE analyze(
      table_name	       IN VARCHAR2);

   PROCEDURE analyze_mid;

   PROCEDURE truncate(
      table_name	       IN VARCHAR2);

   PROCEDURE nologging(
      table_name	       IN VARCHAR2);

   PROCEDURE logging(
      table_name	       IN VARCHAR2);

   PROCEDURE nocache(
      table_name	       IN VARCHAR2);

   PROCEDURE cache(
      table_name	       IN VARCHAR2);

   PROCEDURE verify_mid;

   PROCEDURE cleanup_temporary_tables(
      prefix		       IN VARCHAR2);

   PROCEDURE cleanup_temporary_tables;

   FUNCTION dump_file(
      schema		       IN VARCHAR2,
      table_name	       IN VARCHAR2,
      dir		       IN VARCHAR2,
      file_type 	       IN VARCHAR2,
      wlob_filename	       IN VARCHAR2)
   RETURN INTEGER;

   PROCEDURE dump_table(
      schema		       IN VARCHAR2,
      table_name	       IN VARCHAR2,
      dir		       IN VARCHAR2);

   PROCEDURE dump_ctl_file(
      schema		       IN VARCHAR2,
      table_name	       IN VARCHAR2,
      dir		       IN VARCHAR2);

   PROCEDURE dump_schema_script(
      dbase_user	       IN VARCHAR2,
      dir		       IN VARCHAR2);

   PROCEDURE dump_mid(
      dir		       IN VARCHAR2);

END MEME_SYSTEM;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY meme_system AS

/* FUNCTION RELEASE ************************************************************
 */
FUNCTION release
RETURN INTEGER
IS
BEGIN
   version;
   RETURN TO_NUMBER(release_number);
END release;

/* FUNCTION VERSION_INFO *******************************************************
 */
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
   RETURN package_name || ' Release ' || release_number || ': ' ||
     'version ' || version_number || ' (' || version_date || ')';
END version_info;

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

   DBMS_OUTPUT.PUT_LINE('Package: ' || package_name);
   DBMS_OUTPUT.PUT_LINE('Release ' || release_number || ': ' ||
	'version ' || version_number || ', ' ||
	version_date || ' (' ||
	version_authority || ')');

END version;

/* PROCEDURE SET_TRACE_ON ******************************************************
 */
PROCEDURE set_trace_on
IS
BEGIN
   meme_system_trace := TRUE;
END set_trace_on;

/* PROCEDURE SET_TRACE_OFF *****************************************************
 */
PROCEDURE set_trace_off
IS
BEGIN
   meme_system_trace := FALSE;
END set_trace_off;

/* PROCEDURE SET_DEBUG_ON ******************************************************
 */
PROCEDURE set_debug_on
IS
BEGIN
   meme_system_debug := TRUE;
END set_debug_on;

/* PROCEDURE SET_DEBUG_OFF *****************************************************
 */
PROCEDURE set_debug_off
IS
BEGIN
   meme_system_debug := FALSE;
END set_debug_off;

/* PROCEDURE TRACE *************************************************************
 */
PROCEDURE trace(message IN VARCHAR2)
IS
BEGIN
   IF meme_system_trace = TRUE THEN
      MEME_UTILITY.PUT_MESSAGE(message);
   END IF;
END trace;

/* PROCEDURE LOCAL_EXEC ********************************************************
 */
PROCEDURE local_exec(query IN VARCHAR2)
IS
BEGIN

   IF meme_system_trace = TRUE THEN
      MEME_UTILITY.put_message(query);
   END IF;

   IF meme_system_debug = FALSE THEN
      MEME_UTILITY.exec(query);
   END IF;

END local_exec;

/* FUNCTION LOCAL_EXEC *********************************************************
 */
FUNCTION local_exec(query IN VARCHAR2)
RETURN INTEGER
IS
BEGIN

   IF meme_system_trace = TRUE THEN
      MEME_UTILITY.put_message(query);
   END IF;

   IF meme_system_debug = FALSE THEN
      RETURN MEME_UTILITY.exec(query);
   END IF;

   RETURN 0;

END local_exec;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help
IS
BEGIN
   help('');
END;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help( topic IN VARCHAR2 )
IS
BEGIN
   -- This procedure requires SET SERVEROUTPUT ON
   --DBMS_OUTPUT.ENABLE(2048);

   -- Print version
   MEME_UTILITY.PUT_MESSAGE('.');
   version;
END help;

/* PROCEDURE SELF_TEST *********************************************************
 */
PROCEDURE self_test
IS
BEGIN

   DBMS_OUTPUT.ENABLE(100000);
   -- This procedure requires SET SERVEROUTPUT ON

END self_test;

/* PROCEDURE MEME_SYSTEM_ERROR *************************************************
 */
PROCEDURE meme_system_error(
   method		       IN VARCHAR2,
   location		       IN VARCHAR2,
   error_code		       IN INTEGER,
   detail		       IN VARCHAR2
)
IS
   error_msg		       VARCHAR2(100);
BEGIN
   IF error_code = 1 THEN
      error_msg := 'MS0001: Unspecified error';
   ELSIF error_code = 2 THEN
      error_msg := 'MS0002: UTF_FILE error';
   ELSIF error_code = 30 THEN
      error_msg := 'MS0030: Bad count';
   ELSE
      error_msg := 'MS0000: Unknown Error';
   END IF;

   MEME_UTILITY.PUT_ERROR('Error in MEME_SYSTEM::'||method||' at '||
   location||' ('||error_msg||','||detail||')');

END meme_system_error;

/* PROCEDURE INITIALIZE_TRACE **************************************************
 */
-- This method clears location, err_msg, method
PROCEDURE initialize_trace(
   method		       IN VARCHAR2
)
IS
BEGIN
   location := '0';
   err_msg := '';
   meme_system.method := initialize_trace.method;
END initialize_trace;

/* PROCEDURE REGISTER_PACKAGE **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_SYSTEM.release_number,
      MEME_SYSTEM.version_number,
      SYSDATE,
      MEME_SYSTEM.version_authority,
      MEME_SYSTEM.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

/* PROCEDURE REGISTER_TABLE ****************************************************
 */
PROCEDURE register_table(
   table_name		       IN VARCHAR2
)
IS
BEGIN
   initialize_trace('register_table');

   IF MEME_UTILITY.object_exists('table',table_name) = -1 THEN
      err_msg := 'Table ' || table_name || ' does not exist.';
      RAISE meme_system_exception;
   END IF;

   location := '10';
   err_msg := 'Error inserting into meme_tables';
   local_exec('INSERT INTO meme_tables VALUES (''' || table_name || ''')');

   location := '20';
   MEME_UTILITY.put_message('Delete from meme_indexes.');
   DELETE FROM meme_indexes where table_name = register_table.table_name;

   location := '30';
   MEME_UTILITY.put_message('Delete from meme_ind_columns.');
   DELETE FROM meme_ind_columns where table_name = register_table.table_name;

   location := '40';
   MEME_UTILITY.put_message('Insert into meme_indexes.');
   INSERT INTO meme_indexes (index_name, table_name, pct_free, pct_increase,
      initial_extent, next_extent, min_extents, max_extents, tablespace_name,
      index_type, uniqueness)
   SELECT index_name, table_name, pct_free, pct_increase,
      initial_extent, next_extent, min_extents, max_extents, tablespace_name,
      index_type, uniqueness
   FROM user_indexes
   WHERE table_name = register_table.table_name
   AND index_type in ('BITMAP','NORMAL');

   location := '50';
   MEME_UTILITY.put_message('Insert into meme_ind_columns.');
   INSERT INTO meme_ind_columns 
   	(index_name, table_name, column_name,
	 column_position, column_length, descend)
   SELECT index_name, table_name, column_name,
	column_position, column_length, descend
   FROM user_ind_columns
   WHERE table_name = register_table.table_name;

EXCEPTION
   WHEN OTHERS THEN
      meme_system_error (method,location,1,err_msg || ': ' || SQLERRM);
      RAISE meme_system_exception;
END register_table;

/* PROCEDURE REMOVE_TABLE ******************************************************
 */
PROCEDURE remove_table(
   table_name		       IN VARCHAR2
)
IS
BEGIN
   initialize_trace('remove_table');
   location := '0';
   err_msg := 'Error deleting from meme_tables';
   local_exec('DELETE FROM meme_tables ' ||
      'WHERE table_name = ''' || table_name || '''');

EXCEPTION
   WHEN OTHERS THEN
      meme_system_error (method,location,1,err_msg || ': ' || SQLERRM);
      RAISE meme_system_exception;
END remove_table;

/* PROCEDURE XPLAN *************************************************************
 */
PROCEDURE xplan(
   query		       IN VARCHAR2
)
IS
   TYPE curvar_type	       IS REF CURSOR;
   curvar		       curvar_type;
   line 		       VARCHAR2(128);
   statement_id 	       VARCHAR2(30);

BEGIN

   location := '0';
   err_msg := 'Error getting statement_id';
   statement_id := MEME_UTILITY.get_unique_tablename;

   location := '10';
   err_msg := 'Error explaining plan';
   local_exec( 'EXPLAIN PLAN set statement_id=''' || statement_id ||
      ''' for ' || query );

   COMMIT;

   DBMS_OUTPUT.PUT_LINE('   ');
   DBMS_OUTPUT.PUT_LINE(
      'EXPLAIN PLAN report of statement_id '||statement_id||
      ' in nested format: ' );
   DBMS_OUTPUT.PUT_LINE('   ');

   location := '20';
   err_msg := 'Error opening cursor for nested format';
   OPEN curvar FOR
      SELECT LPAD(' ',2*(LEVEL-1)) || operation || ' ' ||
	 options || ' ' || object_name || ' ' ||
	 DECODE(id, 0, 'Cost = ' || position)
      FROM plan_table
      START WITH id = 0 AND statement_id = xplan.statement_id
      CONNECT BY PRIOR id = parent_id AND statement_id = xplan.statement_id;

      LOOP
	 location := '30';
	 err_msg := 'Error fetching from cursor';
	 FETCH curvar INTO line;
	 EXIT WHEN curvar%NOTFOUND;
	    DBMS_OUTPUT.PUT_LINE('.   ' || line);
      END LOOP;
   CLOSE curvar;

   DBMS_OUTPUT.PUT_LINE('   ');
   DBMS_OUTPUT.PUT_LINE(
      'EXPLAIN PLAN report of statement_id ' || xplan.statement_id ||
      ' in TABLE format: ' );
   DBMS_OUTPUT.PUT_LINE('   ');
   DBMS_OUTPUT.PUT_LINE('.   OPERATION	 OPTIONS   OBJECT_NAME	 ID   PARENT_ID POSITION');
   DBMS_OUTPUT.PUT_LINE('.   ---------	 -------   -----------	 --   --------- --------');

   location := '40';
   err_msg := 'Error opening cursor for table format';
   OPEN curvar FOR
      SELECT RPAD(NVL(operation,' '),18,' ') ||
	 RPAD(NVL(options,' '),15,' ')	||
	 RPAD(NVL(object_name,' '),17,' ')  ||
	 NVL(RPAD(id,5,' '),'	 ')   ||
	 NVL(RPAD(parent_id,10,' '),'	 ')  ||
	 NVL(RPAD(position,10,' '),'	 ')
      FROM plan_table
      WHERE statement_id = xplan.statement_id
      ORDER BY id;

      LOOP
	 location := '50';
	 err_msg := 'Error fetching from cursor';
	 FETCH curvar INTO line;
	 EXIT WHEN curvar%NOTFOUND;
	 DBMS_OUTPUT.PUT_LINE('.   ' || line);
      END LOOP;
   CLOSE curvar;

   DBMS_OUTPUT.PUT_LINE ('   ');
   local_exec('DELETE FROM plan_table WHERE statement_id = ''' || statement_id || '''');

   COMMIT;

EXCEPTION
   WHEN OTHERS THEN
      meme_system_error ('xplan',location,1,err_msg || ': ' || SQLERRM);
      RAISE meme_system_exception;
END xplan;

/* PROCEDURE XPLAN_ID **********************************************************
 */
PROCEDURE xplan_id(
   statement_id 	       IN VARCHAR2,
   delete_flag		       IN VARCHAR2 := MEME_CONSTANTS.NO
)
IS
   TYPE curvar_type	       IS REF CURSOR;
   curvar		       curvar_type;
   line 		       VARCHAR2(128);
BEGIN

   DBMS_OUTPUT.PUT_LINE('   ');
   DBMS_OUTPUT.PUT_LINE(
      'EXPLAIN PLAN report of statement_id ' || statement_id ||
      ' in nested format: ' );
   DBMS_OUTPUT.PUT_LINE('   ');

   location := '0';
   err_msg := 'Error opening cursor for nested format';
   OPEN curvar FOR
      SELECT LPAD(' ',2*(LEVEL-1)) || operation || ' ' ||
	 options || ' ' || object_name || ' ' ||
	 DECODE(id, 0, 'Cost = ' || position)
      FROM plan_table
      START WITH id = 0 AND statement_id = xplan_id.statement_id
      CONNECT BY PRIOR id = parent_id AND statement_id = xplan_id.statement_id;

      LOOP
	 location := '30';
	 err_msg := 'Error fetching from cursor';
	 FETCH curvar INTO line;
	 EXIT WHEN curvar%NOTFOUND;
	 DBMS_OUTPUT.PUT_LINE('.   ' || line);
      END LOOP;
   CLOSE curvar;

   DBMS_OUTPUT.PUT_LINE('   ');
   DBMS_OUTPUT.PUT_LINE(
   'EXPLAIN PLAN report of statement_id ' || xplan_id.statement_id ||
   ' in TABLE format: ' );
   DBMS_OUTPUT.PUT_LINE('   ');
   DBMS_OUTPUT.PUT_LINE('.   OPERATION	 OPTIONS   OBJECT_NAME	 ID   PARENT_ID POSITION');
   DBMS_OUTPUT.PUT_LINE('.   ---------	 -------   -----------	 --   --------- --------');

   location := '40';
   err_msg := 'Error opening cursor for table format';
   OPEN curvar FOR
      SELECT RPAD(NVL(operation,' '),18,' ') ||
	 RPAD(NVL(options,' '),15,' ')	||
	 RPAD(NVL(object_name,' '),17,' ')  ||
	 NVL(RPAD(id,5,' '),'	 ')   ||
	 NVL(RPAD(parent_id,10,' '),'	 ')  ||
	 NVL(RPAD(position,10,' '),'	 ')
      FROM plan_table
      WHERE statement_id = xplan_id.statement_id
      ORDER BY id;

      LOOP
	 location := '50';
	 err_msg := 'Error fetching from cursor';
	 FETCH curvar INTO line;
	 EXIT WHEN curvar%NOTFOUND;
	 DBMS_OUTPUT.PUT_LINE('.   ' || line);
      END LOOP;
   CLOSE curvar;

   IF delete_flag = MEME_CONSTANTS.YES THEN
      DBMS_OUTPUT.PUT_LINE('   ');
      local_exec('DELETE FROM plan_table WHERE statement_id = ''' ||
	 statement_id || '''');
   END IF;

   COMMIT;

EXCEPTION
   WHEN OTHERS THEN
      meme_system_error ('xplan_id',location,1,err_msg || ': ' || SQLERRM);
      RAISE meme_system_exception;
END xplan_id;

/* PROCEDURE DROP_INDEXES ******************************************************
 */
PROCEDURE drop_indexes(
   table_name		       IN VARCHAR2
)

IS
   location		       VARCHAR(3);
   each_index		       VARCHAR2(50);
   constraint_type	       VARCHAR2(1);

   CURSOR index_cur IS
   SELECT index_name FROM user_indexes
	WHERE table_name = UPPER(drop_indexes.table_name)
   	AND index_type in ('BITMAP','NORMAL');

BEGIN

   location := '10';
   OPEN index_cur;
   LOOP
      location := '20';
      FETCH index_cur INTO each_index;
      EXIT WHEN index_cur%NOTFOUND;

      SELECT NVL(max(constraint_type),'N') into constraint_type
      FROM user_constraints
      WHERE constraint_name = each_index;

      IF constraint_type != 'P' THEN
	 location := '30';
	 local_exec('DROP INDEX '||each_index);
      ELSE
	 location := '40';
	 local_exec('ALTER TABLE ' || table_name ||
	 ' DISABLE PRIMARY KEY');
      END IF;
   END LOOP;
   CLOSE index_cur;

EXCEPTION
   WHEN OTHERS THEN
      meme_system_error('drop_indexes',location,1, table_name || ',' || SQLERRM);
      RAISE meme_system_exception;
END drop_indexes;

/* PROCEDURE REINDEX ***********************************************************
 */
PROCEDURE reindex(
   table_name		       IN VARCHAR2,
   rebuild_flag 	       IN VARCHAR2 := MEME_CONSTANTS.NO,
   parallel_flag 	       IN VARCHAR2 := MEME_CONSTANTS.NO,
   compute_stats_flag 	       IN VARCHAR2 := MEME_CONSTANTS.YES
)
IS
   CURSOR index_cursor (name IN varchar2) IS
   SELECT * FROM meme_indexes a
   WHERE a.table_name = name;

   CURSOR column_cursor (name IN varchar2) IS
   SELECT * FROM meme_ind_columns
   WHERE index_name = name
   ORDER BY column_position;

   index_row		       index_cursor%ROWTYPE;
   column_row		       column_cursor%ROWTYPE;
   max_extents		       VARCHAR2(30);
   cols 		       VARCHAR2(256);
   query		       VARCHAR2(4000);
   i			       INTEGER;
   pk_flag		       INTEGER;
   exists_flag		       INTEGER;
   reindex_exception	       EXCEPTION;

   bitmap_clause           VARCHAR2(50);
   unique_clause           VARCHAR2(50);
   end_clause	  	       VARCHAR2(50);
   parallel_clause	       VARCHAR2(50);

BEGIN

   initialize_trace('reindex');

   IF compute_stats_flag = MEME_CONSTANTS.YES THEN
        end_clause := ' COMPUTE STATISTICS ';
   ELSE
 	end_clause := '';
   END IF;

   -- Deal with the parallel_flag
   -- Either it is 'N' or a parallel number like '4'u
   IF parallel_flag != MEME_CONSTANTS.NO THEN
    end_clause := end_clause || ' PARALLEL ' || parallel_flag;
    parallel_clause := ' PARALLEL ' || parallel_flag;
   END IF;

   -- Do not rebuild IOTs
   location := '5.1';
   SELECT count(*) into i FROM user_tables
    WHERE table_name = reindex.table_name
      AND iot_type='IOT';

   IF i > 0 THEN
    	MEME_UTILITY.put_message(table_name || 
	   ' is IOT, not indexing.');
 	RETURN;
   END IF;

   location := '10';
   OPEN index_cursor (UPPER(table_name));
   LOOP

      location := '15';
      --EXECUTE IMMEDIATE 'ALTER TABLESPACE MIDI COALESCE';

      location := '20';
      FETCH index_cursor INTO index_row;
      EXIT WHEN index_cursor%NOTFOUND;

      -- Is it a primary key?
      SELECT count(*) into pk_flag
      FROM user_constraints WHERE constraint_name=index_row.index_name
      AND constraint_type = 'P';

      -- Does the index exist currently?
      SELECT count(*) into exists_flag
      FROM user_indexes WHERE index_name = index_row.index_name;

      -- Is it a bitmap index
      IF upper(index_row.index_type) = 'BITMAP' THEN
    bitmap_clause := ' BITMAP ';
      ELSE
    bitmap_clause := '';
      END IF;
      
      -- Is it a bitmap index
      IF upper(index_row.uniqueness) = 'UNIQUE' THEN
    unique_clause := ' UNIQUE ';
      ELSE
    unique_clause := '';
      END IF;

      -- Index is not a primary key
      IF pk_flag = 0 THEN

	 -- If rebuild_flag is off OR if the index does not exist
  	 -- We must create instead of altering it
	 IF rebuild_flag = MEME_CONSTANTS.NO OR
	    exists_flag = 0 THEN
	    cols := '';
	    location := '40';
	    OPEN column_cursor(index_row.index_name);
	       LOOP
		  location := '50';
		  FETCH column_cursor INTO column_row;
		  EXIT WHEN column_cursor%NOTFOUND;
		  cols := cols || column_row.column_name || ', ';
	       END LOOP; -- column_cursor
	    CLOSE column_cursor;

	    location := '55';
	    MEME_UTILITY.drop_it('index',index_row.index_name);

	    cols := RTRIM(cols,', ');
	    query := 'CREATE ' || unique_clause || bitmap_clause || 
	       ' INDEX ' || index_row.index_name || ' ON ' ||
	       UPPER(table_name) || ' ( ' || cols || ' ) ' || 
	       ' PCTFREE ' || index_row.pct_free || 
	       ' STORAGE ( INITIAL ' || index_row.initial_extent || 
	       ' MINEXTENTS ' || index_row.min_extents || 
	       ' MAXEXTENTS ' || index_row.max_extents || 
 	       ') TABLESPACE ' || index_row.tablespace_name ||
               -- ' PCTINCREASE ' || index_row.pct_increase || 
	       end_clause;
	    location := '60';

	    -- DROP the index first if it exists
	    MEME_UTILITY.drop_it('index',index_row.index_name);

	    -- CREATE the index
	    local_exec(query);

	 -- If the user wants to rebuild and the index exists
	 -- then we want to alter/rebuild the index
	 ELSIF rebuild_flag = MEME_CONSTANTS.YES AND
		exists_flag = 1 THEN
	    query := 'ALTER INDEX ' || index_row.index_name ||
	       ' REBUILD' || end_clause;
	    location := '70';
	    local_exec(query);

	 -- Some error happened, report it
	 ELSE
	    location := '80';
	    meme_system_error('reindex','80',30,
	    index_row.index_name||','||i);
	    RAISE reindex_exception;
	 END IF;

      -- Here we are working with a primary key
      ELSIF pk_flag = 1 THEN

	 -- If the rebuild_flag is NO, recreate
  	 -- the primary key by disabling and enabling it 
	 -- and then computing statistics
	 --IF rebuild_flag = MEME_CONSTANTS.NO THEN

	   -- location := '90';
	   -- query := 'ALTER TABLE ' || table_name ||
       --       ' DISABLE PRIMARY KEY';
	   -- local_exec(query);

	    -- Enable the primary key
	   -- location := '100';
	   -- query := 'ALTER TABLE ' || table_name ||
	   --    ' ENABLE PRIMARY KEY';
	   -- local_exec(query);

	 --END IF;

	 -- Rebuild the index
	 location := '110';
	 query := 'ALTER INDEX '||index_row.index_name||
	    ' REBUILD STORAGE (INITIAL '||index_row.initial_extent||')'||
	    ' TABLESPACE ' || index_row.tablespace_name ||
	    end_clause;

	 local_exec(query);

      ELSE -- pk_flag > 1
	 location := '120';
	 meme_system_error('reindex',location,30,
	    pk_flag||','||table_name||','||rebuild_flag);
	 RAISE meme_system_exception;
      END IF; -- index_name is primary key

      COMMIT;

   END LOOP; -- index_cursor
   CLOSE index_cursor;

   COMMIT;

EXCEPTION
   WHEN reindex_exception THEN
      RAISE meme_system_exception;
   WHEN OTHERS THEN
      COMMIT;
      meme_system_error(method,location,1,
	 table_name || ',' || rebuild_flag || ',' || SQLERRM);
      RAISE meme_system_exception;
END reindex;

/* PROCEDURE REBUILD_TABLE ******************************************************
 -- This procedure rebuilds a table by
 -- removing indexes, calling ALTER...MOVE, restoring indexes,
 -- and then reanalyzing the table.
 */
PROCEDURE rebuild_table(
   table_name		       IN VARCHAR2,
   rebuild_flag		       IN VARCHAR2 := MEME_CONSTANTS.NO,
   parallel_flag 	       IN VARCHAR2 := MEME_CONSTANTS.NO,
   compute_stats_flag 	       IN VARCHAR2 := MEME_CONSTANTS.YES
)
IS
   parallel_clause		VARCHAR2(50) := '';
   initial_extent		INTEGER;

   CURSOR partition_cur (tn IN VARCHAR2) IS
	SELECT partition_name FROM user_tab_partitions
	WHERE upper(table_name) = upper(tn);
   partition_var	partition_cur%ROWTYPE;
BEGIN
   initialize_trace('rebuild_table');

   location := '5';
   IF parallel_flag != MEME_CONSTANTS.NO THEN
	parallel_clause := ' PARALLEL ' || parallel_flag;
   END IF;


   --
   -- Determine if there are partitions
   --
   IF MEME_UTILITY.exec_select(
	'SELECT count(*) FROM user_tab_partitions
	 WHERE upper(table_name) = upper('''||table_name||''') ') > 0 THEN

	location := '10.1';
	OPEN partition_cur(table_name);
	LOOP
	    FETCH partition_cur INTO partition_var;
	    EXIT WHEN partition_cur%NOTFOUND;
	
   	    location := '10.2';
	    MEME_UTILITY.put_message(
		'ALTER TABLE ' || table_name || 
	       ' MOVE PARTITION ' || partition_var.partition_name ||
	       ' STORAGE (INITIAL 1M) ' ||
	         parallel_clause);
   	    EXECUTE IMMEDIATE 
		'ALTER TABLE ' || table_name || 
	       ' MOVE PARTITION ' || partition_var.partition_name || 
	       ' STORAGE (INITIAL 1M) ' ||
	         parallel_clause;

   	    location := '10.3';
   	    initial_extent := MEME_UTILITY.exec_select(
	        'SELECT sum(bytes) FROM user_extents 
	         WHERE upper(segment_name)=''' || upper(table_name) || ''' 
	 	   AND upper(partition_name) = ''' || 
			upper(partition_var.partition_name) || '''');

	    location := '10.4';
   	    initial_extent := (ceil(initial_extent/65536) * 65536);
   	    IF (initial_extent > 1073741824) THEN
	        initial_extent := 1073741824;
   	    END IF;

	    location := '10.5';
	    IF initial_extent > 1048576 THEN
	      location := '10.6';
   	      MEME_UTILITY.put_message(
		'ALTER TABLE ' || table_name || 
	       ' MOVE PARTITION ' || partition_var.partition_name || 
	       ' STORAGE (INITIAL ' || initial_extent || ') ' ||
	         parallel_clause);
              EXECUTE IMMEDIATE 
		'ALTER TABLE ' || table_name || 
	       ' MOVE PARTITION ' || partition_var.partition_name || 
	       ' STORAGE (INITIAL ' || initial_extent || ') ' ||
	         parallel_clause;
  	    END IF;

 	END LOOP;

   ELSE

   	location := '20';
   	MEME_UTILITY.put_message(
	    'ALTER TABLE ' || table_name || ' MOVE STORAGE (INITIAL 1M) ' ||
	     parallel_clause);
   	EXECUTE IMMEDIATE 
	    'ALTER TABLE ' || table_name || ' MOVE STORAGE (INITIAL 1M) ' ||
	     parallel_clause;

   	location := '25';
   	initial_extent := MEME_UTILITY.exec_select(
	    'SELECT sum(bytes) FROM user_extents 
	     WHERE upper(segment_name)=''' || upper(table_name) || ''' ');

   	location := '28';
   	initial_extent := (ceil(initial_extent/65536) * 65536);
   	IF (initial_extent > 1073741824) THEN
	    initial_extent := 1073741824;
   	END IF;

   	location := '30';
	IF initial_extent > 1048576 THEN
   	  MEME_UTILITY.put_message(
	    'ALTER TABLE ' || table_name || ' 
	     MOVE STORAGE (INITIAL ' || initial_extent || ') ' ||
	     parallel_clause);
   	  EXECUTE IMMEDIATE 
	    'ALTER TABLE ' || table_name || ' 
	     MOVE STORAGE (INITIAL ' || initial_extent || ') ' ||
	     parallel_clause;
   	END IF;

   END IF;

   COMMIT;

   location := '100';
   err_msg := 'Error rebuilding indexes.';
   reindex(
	table_name => table_name,
	rebuild_flag => rebuild_flag,
	parallel_flag => parallel_flag,
	compute_stats_flag => compute_stats_flag);

   MEME_UTILITY.put_message('Rebuild of ' || table_name || ' was successful.');

EXCEPTION
   WHEN OTHERS THEN
      meme_system_error (method,location,1,err_msg || ': ' || SQLERRM);
      RAISE meme_system_exception;
END rebuild_table;

/* PROCEDURE REINDEX_MID ********************************************************
 */
PROCEDURE reindex_mid(
   rebuild_flag 	       VARCHAR2 := MEME_CONSTANTS.NO,
   parallel_flag 	       VARCHAR2 := MEME_CONSTANTS.NO,
   compute_stats_flag 	       VARCHAR2 := MEME_CONSTANTS.YES
)
IS
   TYPE curvar_type	       IS REF CURSOR;
   curvar		       curvar_type;
   table_name		       VARCHAR2(50);
BEGIN


   OPEN curvar FOR SELECT table_name from meme_tables;
      LOOP
	 FETCH curvar INTO table_name;
	 EXIT WHEN curvar%NOTFOUND;

   	 --execute immediate 'ALTER TABLESPACE MIDI COALESCE';

	 reindex(
	    table_name => table_name,
	    rebuild_flag => rebuild_flag,
	    parallel_flag => parallel_flag,
	    compute_stats_flag => compute_stats_flag);
	 COMMIT;

      END LOOP;

END reindex_mid;

/* PROCEDURE REBUILD_MID ********************************************************
 */
PROCEDURE rebuild_mid(
   rebuild_flag 	       VARCHAR2 := MEME_CONSTANTS.NO,
   parallel_flag 	       VARCHAR2 := MEME_CONSTANTS.NO,
   compute_stats_flag 	       VARCHAR2 := MEME_CONSTANTS.YES
)
IS
   TYPE curvar_type	       IS REF CURSOR;
   curvar		       curvar_type;
   table_name		       VARCHAR2(50);
BEGIN

   OPEN curvar FOR SELECT table_name from meme_tables;
      LOOP
	 FETCH curvar INTO table_name;
	 EXIT WHEN curvar%NOTFOUND;

   	 --execute immediate 'ALTER TABLESPACE MID COALESCE';
         --execute immediate 'ALTER TABLESPACE MIDI COALESCE';

	 rebuild_table(
	    table_name => table_name,
	    rebuild_flag => rebuild_flag,
	    parallel_flag => parallel_flag,
	    compute_stats_flag => compute_stats_flag);
	 COMMIT;

      END LOOP;

END rebuild_mid;

/* PROCEDURE REFRESH_MEME_INDEXES ***********************************************
 */
PROCEDURE refresh_meme_indexes
IS
BEGIN

   location := '10';
   MEME_UTILITY.put_message('Delete from meme_indexes.');
   DELETE FROM meme_indexes;

   location := '20';
   MEME_UTILITY.put_message('Delete from meme_ind_columns.');
   DELETE FROM meme_ind_columns;

   location := '30';
   MEME_UTILITY.put_message('Insert into meme_indexes.');
   INSERT INTO meme_indexes (index_name, table_name, pct_free, pct_increase,
      initial_extent, next_extent, min_extents, max_extents, tablespace_name,
	index_type, uniqueness)
   SELECT index_name, table_name, pct_free, pct_increase,
      initial_extent, next_extent, min_extents, max_extents, tablespace_name,
	index_type, uniqueness
   FROM user_indexes
   WHERE table_name IN (SELECT table_name FROM meme_tables
			MINUS 
			SELECT table_name FROM user_tables
			WHERE iot_type = 'IOT'
			)
   AND index_type IN ('BITMAP','NORMAL');

   location := '40';
   MEME_UTILITY.put_message('Insert into meme_ind_columns.');
   INSERT INTO meme_ind_columns 
   	(index_name, table_name, column_name,
	 column_position, column_length, descend)
   SELECT index_name, table_name, column_name,
	column_position, column_length, descend
   FROM user_ind_columns
   WHERE table_name IN (SELECT table_name FROM meme_indexes);

EXCEPTION
   WHEN OTHERS THEN
      meme_system_error('refresh_meme_indexes',location,1,
	 SQLERRM);
      RAISE meme_system_exception;
END refresh_meme_indexes;

/* PROCEDURE ANALYZE ************************************************************
 *  Soma 1g changing analyze statement
 *  BAC: cannot estimate here, need to actually analyze entire table
 */
PROCEDURE analyze(
   table_name		       IN VARCHAR2
)
IS
BEGIN
    DBMS_STATS.gather_table_stats (
	ownname => 'MTH', tabname => table_name, estimate_percent=>100,
	method_opt=> 'for all indexed columns size auto',
 	degree => 8, cascade => TRUE);
END analyze;

/* PROCEDURE ANALYZE_MID ********************************************************
 */
PROCEDURE analyze_mid
IS
   TYPE curvar_type	       IS REF CURSOR;
   curvar		       curvar_type;
   table_name		       VARCHAR2(50);
   authority		       VARCHAR2(50);
   work_id		       INTEGER;
BEGIN

   work_id := MEME_UTILITY.new_work_id;

   SELECT user INTO authority FROM dual;

   OPEN curvar FOR SELECT distinct table_name FROM meme_tables;
      LOOP
	 FETCH curvar INTO table_name;
	 EXIT WHEN curvar%NOTFOUND;

	 MEME_UTILITY.timing_start;

	 analyze(table_name);

	 MEME_UTILITY.timing_stop;
	 MEME_UTILITY.log_progress (
	    authority, 'MEME_SYSTEM::analyze_mid',
	    'ANALYZE TABLE ' || table_name || ' has completed.',
	    0, work_id, MEME_UTILITY.elapsed_time );
      END LOOP;

   MEME_UTILITY.reset_progress(work_id);

END analyze_mid;

/* PROCEDURE TRUNCATE ***********************************************************
 */
PROCEDURE truncate(
   table_name		       IN VARCHAR2
)
IS
BEGIN
   local_exec('TRUNCATE TABLE ' || table_name);
   IF meme_system_trace = TRUE THEN
   	MEME_UTILITY.put_message('Truncated table ' || table_name);
   END IF;
END truncate;

/* PROCEDURE NOLOGGING **********************************************************
 */
PROCEDURE nologging(
   table_name		       IN VARCHAR2
)
IS
BEGIN
   local_exec('ALTER TABLE ' || table_name || ' NOLOGGING');
END nologging;

/* PROCEDURE LOGGING ************************************************************
 */
PROCEDURE logging(
   table_name		       IN VARCHAR2
)
IS
BEGIN
   local_exec('ALTER TABLE ' || table_name || ' LOGGING');
END logging;

/* PROCEDURE NOCACHE ************************************************************
 */
PROCEDURE nocache(
   table_name		       IN VARCHAR2
)
IS
BEGIN
   local_exec('ALTER TABLE ' || table_name || ' NOCACHE');
END nocache;

/* PROCEDURE CACHE **************************************************************
 */
PROCEDURE cache(
   table_name		       IN VARCHAR2
)
IS
BEGIN
   local_exec('ALTER TABLE ' || table_name || ' CACHE');
END cache;

/* PROCEDURE VERIFY_MID *********************************************************
 */
-- This procedure is used to make sure all of the
-- neccesary tables exist, and it reports their current
-- counts
PROCEDURE verify_mid
IS
   TYPE curvar_type	       IS REF CURSOR;
   curvar		       curvar_type;
   table_name		       VARCHAR2(50);
   ct			       INTEGER;
   location		       VARCHAR2(5);
   authority		       VARCHAR2(20);
   work_id		       INTEGER;
BEGIN

   work_id := MEME_UTILITY.new_work_id;

   SELECT user INTO authority FROM dual;

   location := '0';
   OPEN curvar FOR SELECT distinct table_name FROM meme_tables;
      LOOP
	 FETCH curvar INTO table_name;
	 EXIT WHEN curvar%NOTFOUND;

	 MEME_UTILITY.timing_start;

	 location := '10';
	 ct := MEME_UTILITY.exec_select(
	   'SELECT count(*) FROM ' || table_name);

	 MEME_UTILITY.timing_stop;

	 MEME_UTILITY.put_message('Table name ' || table_name ||
	   ' exists, its count is: ' || ct);

	 MEME_UTILITY.log_progress (
	    authority, 'MEME_SYSTEM::verify_mid',
	    table_name || ' exists and has count ' || ct,
	    0, work_id, MEME_UTILITY.elapsed_time );

      END LOOP;
   CLOSE curvar;

   MEME_UTILITY.put_message
      ('Verification complete, all necessary meme tables exist.');

   MEME_UTILITY.reset_progress(work_id);

EXCEPTION
   WHEN OTHERS THEN
      CLOSE curvar;
      meme_system_error('verify_mid',location,1,
      'table_name = ' || table_name);
      RAISE meme_system_exception;
END verify_mid;

/* PROCEDURE CLEANUP_TEMPORARY_TABLES *******************************************
 */
PROCEDURE cleanup_temporary_tables
IS
BEGIN
   cleanup_temporary_tables(MEME_CONSTANTS.TMP_PREFIX_PATTERN);
END cleanup_temporary_tables;

/* PROCEDURE CLEANUP_TEMPORARY_TABLES *******************************************
 */
PROCEDURE cleanup_temporary_tables (
   prefix		       IN VARCHAR2
)
IS
   TYPE curvar_type	       IS REF CURSOR;
   curvar		       curvar_type;
   table_name		       VARCHAR2(50);
   pattern		       VARCHAR2(50);
BEGIN

   pattern := UPPER(prefix || '%');
   OPEN curvar FOR SELECT distinct table_name FROM user_tables
	WHERE table_name like pattern escape MEME_CONSTANTS.ESCAPE
	AND table_name NOT IN (SELECT table_name FROM meme_tables);
   LOOP
      FETCH curvar INTO table_name;
      EXIT WHEN curvar%NOTFOUND;

      IF meme_system_debug = FALSE THEN
	 MEME_UTILITY.drop_it('table',table_name);
      END IF;
      trace('DROP TABLE ' || table_name || ';');
   END LOOP;

END cleanup_temporary_tables;

/* FUNCTION DUMP_FILE ***********************************************************
 */
FUNCTION dump_file(
   schema		       IN VARCHAR2,
   table_name		       IN VARCHAR2,
   dir			       IN VARCHAR2,
   file_type		       IN VARCHAR2,
   wlob_filename	       IN VARCHAR2
)
RETURN INTEGER
IS
   f_handle		       UTL_FILE.FILE_TYPE;
   record_buffer	       VARCHAR2(32000);

   /* Local variables */
   user_schema		       VARCHAR2(50);	 /* username	 */
   col_list		       VARCHAR2(2000);	 /* column list  */
   col_clob		       VARCHAR2(2000);	 /* column clob  */
   sql_str		       VARCHAR2(3000);	 /* SQL string	 */
   col_cnt		       INTEGER := 0;	 /* column count */
   l_ctr		       INTEGER := 0;	 /* loop counter */

   lobtype_count	       INTEGER; 	 /* CLOB count	 */
   lob_locator		       CLOB;		 /* CLOB locator */
   clob_limit		       INTEGER := 3900;  /* CLOB limit	 */
   loop_flag		       BOOLEAN; 	 /* while flag	 */
   long_length		       INTEGER; 	 /* amount var	 */
   amount_var		       INTEGER; 	 /* amount var	 */
   offset_var		       INTEGER; 	 /* offset var	 */

   clob_flag	               BOOLEAN := TRUE;  /* clob flag    */
   clob_field		       VARCHAR2(2000);	 /* clob field   */
   clob_field_ctr              INTEGER := 0;     /* clob field counter */
   l_occurence		       INTEGER := 1; 	 /* occurence of search string in INSTR() */
   l_position		       INTEGER := 0; 	 /* position of the start string in SUBSTR() */

   /* Local cursor */
   CURSOR cur_column(tableName IN VARCHAR2, schemaName IN VARCHAR2) IS
      SELECT column_name, data_type, data_length FROM all_tab_columns
      WHERE LOWER(table_name) = LOWER(tableName)
      AND LOWER(owner) = LOWER(schemaName);

   column_rec cur_column%ROWTYPE;

   TYPE clob_rows IS REF CURSOR;
   cur_clob clob_rows;

   TYPE nonclob_rec IS REF CURSOR;
   cur_nonclob nonclob_rec;

BEGIN

   initialize_trace('dump_file');

   location := '05';

   /* Map user_schema */
   IF dump_file.schema IS NULL THEN
      SELECT username INTO user_schema FROM user_users;
   ELSE
      user_schema := dump_file.schema;
   END IF;

   location := '06';

   /* For field with LOB types, allow only CLOB and not any other LOB types */
   SELECT COUNT(*) INTO lobtype_count FROM all_tab_columns
   WHERE LOWER(table_name) = LOWER(dump_file.table_name)
   AND LOWER(owner) = LOWER(user_schema)
   AND data_type LIKE '%LOB' AND data_type != 'CLOB';

   IF lobtype_count > 0 THEN
      MEME_UTILITY.PUT_MESSAGE
      ('The '||dump_file.table_name||' table has a non-CLOB LOB field, no action taken.');
      RETURN 0;
   END IF;

   location := '10';

   IF wlob_filename = MEME_CONSTANTS.NO THEN
      -- Create .dat file for table without CLOB data type
      f_handle := UTL_FILE.FOPEN(dump_file.dir,
      dump_file.table_name||'.'||LOWER(dump_file.file_type),'W',4000);
   END IF;

   location := '15';

   /* Map control file header */
   IF dump_file.file_type = MEME_CONSTANTS.CTL_FILE THEN

      -- Count the number of CLOB field of the current table
      SELECT COUNT(*) INTO lobtype_count FROM all_tab_columns
      WHERE LOWER(table_name) = LOWER(dump_file.table_name)
      AND LOWER(owner) = LOWER(user_schema)
      AND data_type = 'CLOB';

      -- Count the number of column_names of the current table
      SELECT COUNT(column_name) INTO col_cnt FROM all_tab_columns
      WHERE LOWER(table_name) = LOWER(dump_file.table_name)
      AND LOWER(owner) = LOWER(user_schema);

      /* Mapped control file header */
      IF lobtype_count > 0 THEN
         -- Control file header for table with CLOB field(s)
	 record_buffer :=
	    'load data'||CHR(10)||
	    'infile '||''''||dump_file.table_name||''''||'"str X''7c0a''"'||CHR(10)||
	    'into table '||dump_file.table_name||CHR(10)||
	    'truncate'||CHR(10)||
	    'fields terminated by '''||'|'''||CHR(10)||
	    '(';
      ELSE
         -- Control file header for table without CLOB field(s)
	 record_buffer :=
	    'options (direct=true)'||CHR(10)||
	    'unrecoverable'||CHR(10)||
	    'load data'||CHR(10)||
	    'infile '||''''||dump_file.table_name||''''||'"str X''7c0a''"'||CHR(10)||
	    'badfile '''||dump_file.table_name||'.bad'''||CHR(10)||
	    'discardfile '''||dump_file.table_name||'.dsc'''||CHR(10)||
	    'truncate'||CHR(10)||
	    'into table '||dump_file.table_name||CHR(10)||
	    'fields terminated by '''||'|'''||CHR(10)||
	    'trailing nullcols'||CHR(10)||
	    '(';
      END IF;
      location := '15.8';
      UTL_FILE.PUT(f_handle, record_buffer);
      location := '15.9';
      UTL_FILE.NEW_LINE(f_handle);
   END IF;

   /* Map table schema.  This process all .dat and .ctl files */

   location := '20';
   OPEN cur_column(dump_file.table_name, user_schema);
   LOOP
      FETCH cur_column INTO column_rec;

      IF cur_column%NOTFOUND THEN
         -- This block only used for mapping control file
         IF dump_file.file_type = MEME_CONSTANTS.CTL_FILE THEN
            record_buffer := ')';
            location := '20.1';
            UTL_FILE.PUT(f_handle, record_buffer);
            location := '20.2';
            UTL_FILE.NEW_LINE(f_handle);
         END IF;
         EXIT;
      END IF;

      location := '22';

      /* The ff blocks are used for dumping all .dat files. */
      /* It maps the col_list that will hold the list of the field names of the table. */

      IF l_ctr = 0 THEN
         /* Initial loop, this determine the data type of the 1st field */
         IF column_rec.data_type = 'CLOB' THEN
            IF wlob_filename = MEME_CONSTANTS.YES THEN
               -- clob field in 1st column.
               col_list := column_rec.column_name;
               col_clob := column_rec.column_name;
            END IF;
         ELSE
            IF col_list IS NULL THEN
               -- non-clob field in 1st column.
               col_list := column_rec.column_name;
            END IF;
         END IF;
      END IF;

      location := '24';

      IF l_ctr > 0 THEN
         /* Loop continuation, this maps the remaining field names separated by '|' */
         IF column_rec.data_type = 'CLOB' THEN
            IF wlob_filename = MEME_CONSTANTS.YES THEN
               -- clob field not in 1st column.
               IF col_clob IS NOT NULL THEN
                  -- append ',' to support multiple clob fields
                  col_clob := col_clob||',';
               END IF;
               col_list := col_clob||column_rec.column_name;
               col_clob := column_rec.column_name;
            END IF;
         ELSE
            IF col_clob IS NULL THEN
               IF col_list IS NOT NULL THEN
                  -- appends non-clob field names.
                  col_list := col_list||'||'||'''|'''||'||'||column_rec.column_name;
               ELSE
                  -- 1st field is a clob, should omit the clob field in the field name list.
                  -- skipped clob field area and mapped 1st non clob field names.
                  col_list := column_rec.column_name;
               END IF;
            END IF;
         END IF;
      END IF;

      l_ctr := l_ctr+1;

      location := '26';

      /* Map control file */

      IF dump_file.file_type = MEME_CONSTANTS.CTL_FILE THEN
         /* Map data types */
         IF column_rec.data_type = 'NUMBER' THEN
            column_rec.data_type := 'DECIMAL EXTERNAL';
         ELSIF column_rec.data_type = 'VARCHAR2' THEN
	    IF column_rec.data_length > 255 THEN
                column_rec.data_type := 'CHAR('||column_rec.data_length||')';
	    ELSE
                column_rec.data_type := 'CHAR';
	    END IF;
         ELSIF
            column_rec.data_type = 'DATE' THEN
            column_rec.data_type := 'DATE "DD-mon-YYYY HH24:MI:SS"';
         ELSIF
            column_rec.data_type = 'CLOB' THEN
            clob_field_ctr := clob_field_ctr+1;
            column_rec.data_type := 'LOBFILE(CONSTANT '||''''||
            'lob_'||dump_file.table_name||'_'||clob_field_ctr||'.dat'||''''||')'||CHR(10)||
            CHR(9)||CHR(9)||CHR(9)||'terminated by '||''''||'|\n'||''''||CHR(10)||
            CHR(9)||CHR(9)||CHR(9)||'enclosed by "<lob>" and "</lob>"';
         END IF;

         /* Format the value of record_buffer */
         record_buffer := LOWER(column_rec.column_name)||CHR(9);
         IF LENGTH(column_rec.column_name) < 8 THEN
            record_buffer := record_buffer||CHR(9)||CHR(9);
         ELSIF LENGTH(column_rec.column_name) BETWEEN 8 AND 15 THEN
            record_buffer := record_buffer||CHR(9);
         END IF;
         record_buffer := record_buffer||column_rec.data_type;

         /* Append comma if it is not the last record */
         IF l_ctr < col_cnt THEN
            record_buffer := record_buffer||',';
         END IF;
         UTL_FILE.PUT(f_handle, record_buffer);
         UTL_FILE.NEW_LINE(f_handle);
      END IF;

   END LOOP;
   CLOSE cur_column;

   IF dump_file.file_type = MEME_CONSTANTS.CTL_FILE THEN
      UTL_FILE.FCLOSE(f_handle);
      COMMIT;
      RETURN 0;
   END IF;

   location := '40';

   /* Statement below only used for all .dat file(s) */

   /* Map query */
   IF wlob_filename = MEME_CONSTANTS.NO THEN
      col_list := col_list||'||'||'''|''';
   END IF;

   sql_str := 'SELECT '||col_list||
   ' FROM '||user_schema||'.'||dump_file.table_name;

   --MEME_UTILITY.put_message(sql_str);

   location := '50';

   clob_field_ctr := 0; -- recycled variable
   IF wlob_filename = MEME_CONSTANTS.YES THEN
      /* Process table which contain(s) CLOB data types */

      WHILE clob_flag
         LOOP

         location := '52';

         /* Extract col_list and map to clob_field to support multiple */
         /* CLOB data types in a table */

         -- l_occurence holds the value of the current occurence of the search string in INSTR().
         -- l_position holds the value of the current start position of SUBSTR().

         clob_field := NULL;

         -- Get the current CLOB field
         clob_field := SUBSTR(col_list,1,INSTR(col_list,',',1,l_occurence)-1);

         IF clob_field IS NOT NULL THEN
            l_position := INSTR(col_list,',',1,l_occurence);
         ELSE 
            -- Map the last CLOB field in the table
            clob_field := SUBSTR(col_list,l_position+1);
            clob_flag := FALSE; -- this should be the last loop
         END IF;

         l_occurence := l_occurence + 1;

         sql_str := 'SELECT '||clob_field||
         ' FROM '||user_schema||'.'||dump_file.table_name;

         --MEME_UTILITY.put_message(sql_str);

         clob_field_ctr := clob_field_ctr+1;
         f_handle := UTL_FILE.FOPEN(dump_file.dir,
	    'lob_'||dump_file.table_name||'_'||clob_field_ctr||'.'||LOWER(dump_file.file_type),'W',4000);

         OPEN cur_clob FOR sql_str;
         LOOP
	    location := '51';
	    FETCH cur_clob INTO lob_locator;
	    EXIT WHEN cur_clob%NOTFOUND;

	    amount_var := 0;
	    IF lob_locator IS NOT NULL THEN
	       amount_var := DBMS_LOB.GETLENGTH(lob_locator);
	    END IF;

	    location := '52';

	    record_buffer := '<lob>';
	    UTL_FILE.PUT(f_handle, record_buffer);

	    offset_var := 1;

	    loop_flag := TRUE;
	    WHILE loop_flag
	    LOOP
	       long_length := amount_var;
	       IF amount_var > clob_limit THEN
	          amount_var := clob_limit;
	       END IF;
	       IF amount_var = long_length THEN
	          loop_flag := FALSE;
	       END IF;

	       IF amount_var > 0 THEN
	          DBMS_LOB.READ(lob_locator, amount_var, offset_var, record_buffer);
	       ELSE
	          record_buffer := '';
	       END IF;

	       UTL_FILE.PUT(f_handle, record_buffer);

	       offset_var := amount_var + 1;
	       amount_var := long_length - amount_var;
	    END LOOP;

	    record_buffer := '</lob>|';
	    UTL_FILE.PUT(f_handle, record_buffer);

	    UTL_FILE.NEW_LINE(f_handle);
         END LOOP;
         CLOSE cur_clob;
         UTL_FILE.FCLOSE(f_handle);
      END LOOP; -- clob_flag
   END IF;

   location := '60';

   IF wlob_filename = MEME_CONSTANTS.NO THEN
      /* Process table which contains all primitive data types */
      location := '60.1';
      OPEN cur_nonclob FOR sql_str;
      LOOP
         location := '60.2';
         FETCH cur_nonclob INTO record_buffer;
         EXIT WHEN cur_nonclob%NOTFOUND;

         --meme_utility.put_message(record_buffer);
         location := '60.3';
         UTL_FILE.PUT(f_handle, record_buffer);
         location := '60.4';
         UTL_FILE.NEW_LINE(f_handle);
      END LOOP;
      CLOSE cur_nonclob;
      UTL_FILE.FCLOSE(f_handle);
   END IF;

   COMMIT;
   RETURN 0;

EXCEPTION
   WHEN UTL_FILE.INTERNAL_ERROR THEN
      meme_system_error (method,location,2,'Internal Error '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
   WHEN UTL_FILE.INVALID_FILEHANDLE THEN
      meme_system_error (method,location,2,'Invalid Filehandle '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
   WHEN UTL_FILE.INVALID_MODE THEN
      meme_system_error (method,location,2,'Invalid mode in UTL_FILE package '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
   WHEN UTL_FILE.INVALID_OPERATION THEN
      meme_system_error (method,location,2,'User does not have write privileges '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
   WHEN UTL_FILE.INVALID_PATH THEN
      meme_system_error (method,location,2,'Invalid Path '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
   WHEN UTL_FILE.READ_ERROR THEN
      meme_system_error (method,location,2,'Read Error '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
   WHEN UTL_FILE.WRITE_ERROR THEN
      meme_system_error (method,location,2,'Write Error '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
   WHEN VALUE_ERROR THEN
      meme_system_error (method,location,2,'Text is too long '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
   WHEN OTHERS THEN
      meme_system_error (method,location,1,'Oracle Internal Error '||
	 '(SQLERRM:'||SQLERRM||'. Table:'||dump_file.table_name||').');
      RETURN -1;
END dump_file;

/* PROCEDURE DUMP_TABLE *********************************************************
 */
PROCEDURE dump_table(
   schema		       IN VARCHAR2,
   table_name		       IN VARCHAR2,
   dir			       IN VARCHAR2
)
IS
   user_schema		       VARCHAR2(50);
   ret_val		       INTEGER;
   lobtype_count	       INTEGER;
   dump_table_exc	       EXCEPTION;
BEGIN
   IF dump_table.schema IS NULL THEN
      SELECT username INTO user_schema FROM user_users;
   ELSE
      user_schema := dump_table.schema;
   END IF;

   SELECT COUNT(*) INTO lobtype_count FROM all_tab_columns
   WHERE LOWER(table_name) = LOWER(dump_table.table_name)
   AND LOWER(owner) = LOWER(user_schema)
   AND data_type = 'CLOB';

   IF lobtype_count > 0 THEN
      ret_val := dump_file(schema, LOWER(table_name), dir,
	 MEME_CONSTANTS.DAT_FILE, MEME_CONSTANTS.YES);
   END IF;

   IF ret_val != 0 THEN
      RAISE dump_table_exc;
   END IF;

   ret_val := dump_file(schema, LOWER(table_name), dir,
      MEME_CONSTANTS.DAT_FILE, MEME_CONSTANTS.NO);

   IF ret_val != 0 THEN
      RAISE dump_table_exc;
   END IF;

EXCEPTION
   WHEN dump_table_exc THEN
      RAISE meme_system_exception;
   WHEN OTHERS THEN
      meme_system_error (method,location,1,'Oracle Internal Error:'||SQLERRM);
      RAISE meme_system_exception;

END dump_table;

/* PROCEDURE DUMP_CTL_FILE ******************************************************
 */
PROCEDURE dump_ctl_file(
   schema		       IN VARCHAR2,
   table_name		       IN VARCHAR2,
   dir			       IN VARCHAR2
)
IS
   ret_val		       INTEGER;
   dump_ctl_file_exc	       EXCEPTION;
BEGIN

   ret_val := dump_file(schema, LOWER(table_name), dir,
      MEME_CONSTANTS.CTL_FILE, MEME_CONSTANTS.NO);

   IF ret_val != 0 THEN
      RAISE dump_ctl_file_exc;
   END IF;

EXCEPTION
   WHEN dump_ctl_file_exc THEN
      RAISE meme_system_exception;
   WHEN OTHERS THEN
      meme_system_error (method,location,1,'Oracle Internal Error:'||SQLERRM);
      RAISE meme_system_exception;

END dump_ctl_file;

/* PROCEDURE DUMP_SCHEMA_SCRIPT *************************************************
 */
PROCEDURE dump_schema_script(
   dbase_user		       IN VARCHAR2,
   dir			       IN VARCHAR2
)
IS
   f_handle		       UTL_FILE.FILE_TYPE; /* file handle    */
   schema_buffer	       VARCHAR2(500);	   /* schema buffer  */
   db_user		       VARCHAR2(50);	   /* dbase user     */
   col_ctr		       INTEGER; 	   /* column counter */
   col_cnt		       INTEGER; 	   /* column count   */

   /* Local cursor */

   CURSOR cur_schema(dbase_user IN VARCHAR2) IS
      SELECT * FROM all_tab_columns
      WHERE LOWER(owner) = LOWER(dbase_user)
      AND table_name NOT IN (SELECT view_name FROM all_views);

   schema_rec cur_schema%ROWTYPE;

BEGIN

   initialize_trace('dump_schema_script');

   location := '05';
   IF dump_schema_script.dbase_user IS NULL THEN
      SELECT username INTO db_user FROM user_users;
   ELSE
      db_user := dump_schema_script.dbase_user;
   END IF;

   location := '10';
   f_handle := UTL_FILE.FOPEN(dir, LOWER(db_user)||'_tables.sql', 'W');

   col_ctr := 0;
   col_cnt  := 0;

   location := '20';
   OPEN cur_schema(db_user);
      LOOP
	 location := '30';
	 FETCH cur_schema INTO schema_rec;
	 EXIT WHEN cur_schema%NOTFOUND;

	    col_ctr := col_ctr + 1;

	    /* Map begining of statement */
	    IF col_ctr = 1 THEN
	       /* Count the number of column_names of the current table */
	       location := '50';
	       SELECT COUNT(column_name) INTO col_cnt FROM all_tab_columns
               WHERE LOWER(owner) = LOWER(schema_rec.owner)
               AND LOWER(table_name) = LOWER(schema_rec.table_name);

	       schema_buffer :=
		  'DROP TABLE '||LOWER(schema_rec.table_name)||';'||CHR(10)||
		  'CREATE TABLE '||LOWER(schema_rec.table_name)||' ('||CHR(10);
	    ELSE
	       schema_buffer := '';
	    END IF;

	    /* Map column name */
	    schema_buffer := schema_buffer||
		  CHR(9)||LOWER(schema_rec.column_name);

	    /* Map data type */
	    location := '60';
	    IF LENGTH(schema_rec.column_name) < 8 THEN
	       schema_buffer := schema_buffer||CHR(9)||CHR(9)||CHR(9);
	    ELSIF LENGTH(schema_rec.column_name) BETWEEN 8 AND 15 THEN
	       schema_buffer := schema_buffer||CHR(9)||CHR(9);
	    ELSIF LENGTH(schema_rec.column_name) BETWEEN 16 AND 23 THEN
	       schema_buffer := schema_buffer||CHR(9);
	    ELSE
	       schema_buffer := schema_buffer||' ';
	    END IF;

	    schema_buffer := schema_buffer||schema_rec.data_type;

	    /* Map data length, data precision */
	    location := '70';
	    IF schema_rec.data_type = 'VARCHAR2' OR
	       schema_rec.data_type = 'CHAR' THEN
	       schema_buffer := schema_buffer||'('||
		  schema_rec.data_length||')';
	    ELSIF schema_rec.data_type = 'NUMBER' THEN
	       IF schema_rec.data_precision > 1 THEN
		  schema_buffer := schema_buffer||'('||
		     schema_rec.data_precision||')';
	       END IF;
	    END IF;

	    /* Map default value */
	    location := '80';
	    IF schema_rec.data_default IS NOT NULL THEN
	       schema_buffer := schema_buffer||
		  ' DEFAULT '||schema_rec.data_default;
	    END IF;

	    /* Map string NOT NULL */
	    location := '90';
	    IF schema_rec.nullable = 'N' THEN
	       schema_buffer := schema_buffer||' NOT NULL';
	    END IF;

	    /* Map EndOfLine, EndOfStatement */
	    location := '100';
	    IF col_ctr < col_cnt THEN
	       schema_buffer := schema_buffer||',';
	    ELSE
	       schema_buffer := schema_buffer||')'||CHR(10)||
		  'PCTFREE 10 PCTUSED 80;'||CHR(10);
	       col_ctr := 0;
	    END IF;

	    location := '110';
	    UTL_FILE.PUT(f_handle, schema_buffer);
	    UTL_FILE.NEW_LINE(f_handle);

      END LOOP;
   CLOSE cur_schema;

   UTL_FILE.FCLOSE(f_handle);

EXCEPTION
   WHEN OTHERS THEN
     meme_system_error (method,location,1,'Oracle Internal Error: '||SQLERRM);
     RAISE meme_system_exception;

END dump_schema_script;

/* PROCEDURE DUMP_MID ***********************************************************
  This procedure dumps schema and load scripts for the MID.
  It is called by $MEME_HOME/bin/dump_mid.pl when dumping
  the entire database.

  It no longer dumps the table data.

 */
PROCEDURE dump_mid (
   dir			       IN VARCHAR2
)
IS
   /* Local variables */
   test_ctr		       INTEGER; 	   /* test counter    */
   f_handle		       UTL_FILE.FILE_TYPE; /* file handle     */
   script_buffer	       VARCHAR2(4000);	   /* script buffer   */

   /* Local cursor */
   CURSOR cur_table(wms_user IN VARCHAR2, mid_user IN VARCHAR2) IS
      SELECT owner, table_name FROM all_tables
      WHERE (owner = wms_user OR owner = mid_user)
	AND LOWER(table_name) != 'plan_table';
	
   table_rec cur_table%ROWTYPE;

BEGIN

   initialize_trace('dump_mid');

   location := '10';
   f_handle := UTL_FILE.FOPEN(dir, 'load_mid.csh', 'W');

   location := '20';
   script_buffer :=
     '#!/bin/csh -f'||CHR(10)||
     '#'||CHR(10)||
     '# This file was generated by MEME_SYSTEM.dump_mid on: '||sysdate||CHR(10)||
     '#'||CHR(10)||
     '# This script will not index the database, will not analyze the database,'||CHR(10)||
     '# and will leave the tablespace with a potentially large number of extents.'||CHR(10)||
     '#'||CHR(10)||
     'if ($?ORACLE_HOME == 0) then'||CHR(10)||
     '	 echo "ORACLE_HOME must be set."'||CHR(10)||
     '	 exit 1'||CHR(10)||
     'endif'||CHR(10)||CHR(10)||
     'if ($?MEME_HOME == 0) then'||CHR(10)||
     '	 echo "MEME_HOME must be set."'||CHR(10)||
     '	 exit 1'||CHR(10)||
     'endif'||CHR(10)||CHR(10)||
     'if ($#argv != 1) then'||CHR(10)||
     '	 echo "Usage: $0 <database>"'||CHR(10)||
     '	 exit 1'||CHR(10)||
     'endif'||CHR(10)||CHR(10)||
     'set db = $1'||CHR(10)||
     'set wms_user = '||''''||LOWER(MEME_CONSTANTS.WMS_USER)||''''||CHR(10)||
     'set mid_user = '||''''||LOWER(MEME_CONSTANTS.MID_USER)||''''||CHR(10)||
     'set wms_userpw = `$MIDSVCS_HOME/bin/get-oracle-pwd.pl -u $wms_user -d $db`'||CHR(10)||
     'set mid_userpw = `$MIDSVCS_HOME/bin/get-oracle-pwd.pl -u $mid_user -d $db`'||CHR(10)||CHR(10)||
     'echo "-------------------------------------------------------------"'||CHR(10)||
     'echo "Starting ...`/bin/date`"'||CHR(10)||
     'echo "-------------------------------------------------------------"'||CHR(10)||CHR(10)||
     '#'||CHR(10)||
     '# Load files'||CHR(10)||
     '#';

   location := '30';
   UTL_FILE.PUT_LINE(f_handle, script_buffer);

   location := '40';
   dump_schema_script(MEME_CONSTANTS.MID_USER, dir);
   location := '50';
   dump_schema_script(MEME_CONSTANTS.WMS_USER, dir);

   location := '60';
   script_buffer :=
      'sqlplus $mid_userpw@$db < ${mid_user}_tables.sql'||CHR(10)||
      'sqlplus $wms_userpw@$db < ${wms_user}_tables.sql'||CHR(10)||
      'sqlplus $mid_userpw@$db < $MEME_HOME/etc/sql/meme_tables.sql'||CHR(10)||
      'sqlplus $mid_userpw@$db < $MEME_HOME/etc/sql/meme_indexes.sql'||CHR(10)||
      'sqlplus $mid_userpw@$db < $MEME_HOME/etc/sql/meme_synonyms.sql'||CHR(10)||
      'sqlplus $mid_userpw@$db < $MEME_HOME/etc/sql/meme_views.sql';

   location := '70';
   UTL_FILE.PUT_LINE(f_handle, script_buffer);

   /* Retrieve control table names. */
   /* The use of all_tables instead of dba_tables answered the user privilege issue. */

   location := '80';
   test_ctr := 0;
   OPEN cur_table(MEME_CONSTANTS.WMS_USER, MEME_CONSTANTS.MID_USER);
      LOOP
   	 location := '90';
	 FETCH cur_table INTO table_rec;
	 EXIT WHEN cur_table%NOTFOUND;

	 --dump_table(table_rec.owner, LOWER(table_rec.table_name), dir);
	 --dump_ctl_file(table_rec.owner, LOWER(table_rec.table_name), dir);

	 /* Determine which user to pass when calling SQL Loader. */
   	 location := '100';
	 IF table_rec.owner = MEME_CONSTANTS.WMS_USER THEN
	    script_buffer := 'sqlldr $wms_userpw@$db';
	 ELSE
	    script_buffer := 'sqlldr $mid_userpw@$db';
	 END IF;

   	 location := '110';
	 script_buffer := script_buffer||' control ="'||LOWER(table_rec.table_name)||'.ctl"';

   	 location := '120';
	 UTL_FILE.PUT_LINE(f_handle, script_buffer);

	 test_ctr := test_ctr+1;

      END LOOP;
   location := '130';
   CLOSE cur_table;

   location := '140';
   script_buffer :=
     CHR(10)||'#'||CHR(10)||
     '# Load PL/SQL Packages, analyze tables and remove temporary tables'||CHR(10)||
     '#'||CHR(10)||
     '$ORACLE_HOME/bin/sqlplus $mid_userpw@$db << EOF'||CHR(10)||
     CHR(9)||'-- Load PL/SQL packages'||CHR(10)||
     CHR(9)||'@$MEME_HOME/etc/sql/meme_packages.sql'||CHR(10)||
     CHR(9)||'-- Analyze tables'||CHR(10)||
     CHR(9)||'exec MEME_SYSTEM.analyze_mid;'||CHR(10)||
     CHR(9)||'-- Cleanup temporary tables'||CHR(10)||
     CHR(9)||'exec MEME_SYSTEM.cleanup_temporary_tables;'||CHR(10)||
     CHR(9)||'exec MEME_SYSTEM.cleanup_temporary_tables(''qat\_'');'||CHR(10)||
     'EOF';

   location := '150';
   UTL_FILE.PUT_LINE(f_handle, script_buffer);

   location := '160';
   script_buffer :=
     CHR(10)||'#'||CHR(10)||
     '# Check for errors'||CHR(10)||
     '#'||CHR(10)||
     'echo ""'||CHR(10)||
     'echo ""'||CHR(10)||
     'echo "The following log files had errors:"'||CHR(10)||
     'echo "-------------------------------------------------------------"'||CHR(10)||
     'fgrep -1 "ORA-" *log'||CHR(10)||
     'echo "-------------------------------------------------------------"'||CHR(10)||
     CHR(10)||'#'||CHR(10)||
     '# Done'||CHR(10)||
     '#'||CHR(10)||
     'echo ""'||CHR(10)||
     'echo "-------------------------------------------------------------"'||CHR(10)||
     'echo "Finished ... `/bin/date`"'||CHR(10)||
     'echo "-------------------------------------------------------------"';

   location := '170';
   UTL_FILE.PUT_LINE(f_handle, script_buffer);
   location := '180';
   UTL_FILE.FCLOSE(f_handle);

   COMMIT;

EXCEPTION
   WHEN OTHERS THEN
     meme_system_error (method,location,1,'Oracle Internal Error: '||SQLERRM);
     RAISE meme_system_exception;
END dump_mid;

END meme_system;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_SYSTEM.help;
execute MEME_SYSTEM.register_package;

