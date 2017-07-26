CREATE OR REPLACE PACKAGE MRD_OPERATIONS AS

/*******************************************************************
*
* PL/SQL File: MRD_OPERATIONS.sql
*
* 03/28/2006 BAC (1-7B9EP): added attribute_level to DA,MR,ST lookups
* 03/27/2006 BAC (1-7B9EP): Fix to MR lookups
* 03/01/2006 BAC (1-7B9EP): DA, MR, ST ATUI lookups match new maintenance algorithm.
* 02/27/2006 2.0.5 TTN (1-AHNAL) : add code and cascade field to content_view_members
* 02/02/2006 2.0.4 (TTN) 1-76SUZ : souce_coc_headings and souce_coc_subheadings
*            do not insert the data if the citation already exists
* 07/18/2005 2.0.3 (BAC): MEDLINEPLUS patch removed.
* 05/04/2005 2.0.2 (BAC): generate_aux states for 'aui_history' assumed
*            relationship_name could not be null, so the update to expire
*            bad states was not working properly. NVL clauses added.
*
* 04/22/2005 Patch for MEDLINPLUS is back, for now.  Update tmp_attributes
*            to properly set hashcode
* 11/16/2004 1.2.1 (BAC): MTHRELA logic added,
*                         logic for NLM03 attributes removed
* 07/07/2004 1.2.1 (BAC): Compute DA attributes for tmp_attributes
* 07/01/2004 1.2.0 (BAC): Final version used for 2004AB
* 05/13/2004 1.1.9 (BAC): generate_core_data_states cleaned up and now
*             supports a table_name parameter.
* 01/23/2003 1.1.8 (TTN,BAC): Code reworked for root_source,aui instead
*             of source,atom_id
* 07/29/2002 1.1.7 (BAC): The generate_core_data_states for
*             relationships converts the precise_ingredient_of
*             and has_precise_ingredient relas.
* 07/23/2002 1.1.6 (BAC): Fixed context_relationships section of
*             generate_auxiliary_data_states to only look at
*             contexts that are releasable.
*
* 05/28/2002 1.1.5 (BAC): Removed generate_auxiliary_data_states sections
*             for normwrd, normstr, word_index.  We no longer care about
*             tracking states because we will just normalize the
*             strings at release-time.  This was necessary because the
*             normwrd table contains lui norm strings intead of MRXNS norm
*             strings.  We either had to recompute at release time
*             or add additional data to the MID.
*
* 06/04/2001: Added rank coulmn in mom_safe_replacement statements and changed
*		subheading_id to citation_set_id in mrd_coc_subheadings
* 05/30/2001: Updated initialize_connected_sets so that it runs better
*             with the ddl_commit_mode=false
* 05/04/2001: Updated code in generate_auxiliary_data_states procedure
*		to handle coc_headings,coc_subheadings,mom_safe_replacement and
*		meme_properties
* 04/20/2001: Cleaned up the package.  Compiled in ob_mrd
* 4/17/2001(ssh): Changed all remaining external_.. to mrd_..., especially
*               external_contexts got replaced by mrd_contexts and the
*               generate_auxiliary_data_states procedure (before called
*               update_external_table_from) got updated accordingly.
*               The initialize_MRD procedure should now also work for
*               an "unclean" MID.
* 3/6/2001(ssh): Changed external_termgroup_rank/source_rank/word_index/
*               normwrd/normstr to mrd_....
* 9/6/2000(ssh): calculate_connected_set should work now, but is very slow
*		Added function is_concept_clean, sift_out_clean_concepts
* 9/1/2000(ssh): Added update_external_table procedure, found bug in
*		calculatec_onnected_set, not yet fixed
* 8/22/2000:	First version
*
* Status:
*   Functionality: ?
*         Testing: ?
*    Enhancements:
*
*******************************************************************/

    package_name		VARCHAR2(25)	:= 'MRD_OPERATIONS';
    release_number		VARCHAR2(5)	:= '2';
    version_number		VARCHAR2(10)	:= '2.0.2';
    version_date		DATE		:= '04-May-2005';
    version_authority 		VARCHAR2(5) 	:= 'BAC';

    location			VARCHAR2(10);
    method			VARCHAR2(256);
    err_msg			VARCHAR2(256);
    err_code			INTEGER;

    mrd_operations_debug	BOOLEAN := FALSE;
    mrd_operations_trace	BOOLEAN := FALSE;

    mrd_operations_exception	EXCEPTION;

    FUNCTION release RETURN INTEGER;
    FUNCTION version RETURN FLOAT;
    FUNCTION version_info RETURN VARCHAR2;

    PRAGMA restrict_references(version_info, WNDS, RNDS, WNPS);

    PROCEDURE version;
    PROCEDURE set_debug_on;
    PROCEDURE set_debug_off;
    PROCEDURE set_trace_on;
    PROCEDURE set_trace_off;
    PROCEDURE trace( message IN VARCHAR2 );
    PROCEDURE initialize_trace ( method IN VARCHAR2 );

    PROCEDURE mrd_operations_error (
      method		       IN VARCHAR2,
      location		       IN VARCHAR2,
      error_code	       IN INTEGER,
      detail		       IN VARCHAR2
    );

    PROCEDURE help;
    PROCEDURE help(method_name IN VARCHAR2);
    PROCEDURE self_test;

    -- NO LONGER USED
    --PROCEDURE handle_norm_change_mid;
    --PROCEDURE handle_norm_change_mrd;

    FUNCTION is_concept_clean(
	concept_id IN NUMBER
    ) RETURN INTEGER;

    PROCEDURE update_clean_concepts (
	table_name IN VARCHAR2
    );

    FUNCTION initialize_connected_sets(
	table_name IN VARCHAR2
    ) RETURN INTEGER;

    FUNCTION calculate_connected_set(
	batch_flag IN INTEGER
    )  RETURN INTEGER;

    PROCEDURE generate_core_data_states(
        table_name	IN VARCHAR2 := 'ALL',
    	use_connected_set IN VARCHAR2 DEFAULT MEME_CONSTANTS.YES
    );

    PROCEDURE generate_auxiliary_data_states(
	table_name in VARCHAR2
    );

END MRD_OPERATIONS;
/
SHOW ERRORS

CREATE OR REPLACE PACKAGE BODY MRD_OPERATIONS AS

-- FUNCTION RELEASE *****************************************************/
FUNCTION release
RETURN INTEGER
IS
BEGIN
    version;
    return to_number(release_number);
END release;

-- FUNCTION VERSION *****************************************************/
FUNCTION version
RETURN FLOAT
IS
BEGIN
    version;
    return to_number(version_number);
END version;

-- FUNCTION VERSION_INFO ************************************************/
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
    return package_name || ' Release ' || release_number || ': ' ||
     'version ' || version_number || ' (' || version_date || ')';
END version_info;

-- PROCEDURE VERSION ****************************************************/
PROCEDURE version
IS
BEGIN
    DBMS_OUTPUT.PUT_LINE('Package: ' || package_name);
    DBMS_OUTPUT.PUT_LINE('Release ' || release_number || ': ' ||
	      'version ' || version_number || ', ' ||
	 version_date || ' (' ||
	 version_authority || ')');

END version;

-- PROCEDURE SET_DEBUG_ON ************************************************/
PROCEDURE set_debug_on
IS
BEGIN
    mrd_operations_debug := TRUE;
END set_debug_on;

-- PROCEDURE SET_DEBUG_OFF ***********************************************/
PROCEDURE set_debug_off
IS
BEGIN
    mrd_operations_debug := FALSE;
END set_debug_off;

-- PROCEDURE SET_TRACE_ON ************************************************/
PROCEDURE set_trace_on
IS
BEGIN
    mrd_operations_trace := TRUE;
END set_trace_on;

-- PROCEDURE SET_TRACE_OFF ***********************************************/
PROCEDURE set_trace_off
IS
BEGIN
    mrd_operations_trace:= FALSE;
END set_trace_off;

-- PROCEDURE TRACE ***********************************************************/
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF mrd_operations_trace = TRUE THEN

	MEME_UTILITY.put_message(LPAD(message,45,' .'));

    END IF;

END trace;

-- PROCEDURE INITIALIZE_TRACE *********************************************/
-- This method clears location, err_msg, method, err_code
PROCEDURE initialize_trace ( method	IN VARCHAR2 )
IS
BEGIN
    location := '0';
    err_msg := '';
    err_code := 1;
    mrd_operations.method := initialize_trace.method;
END initialize_trace;

--PROCEDURE MRD_OPERATIONS_ERROR ********************************************/
-- This procedure logs and reports an error
PROCEDURE mrd_operations_error (
	method		IN VARCHAR2,
    	location	IN VARCHAR2,
    	error_code	IN INTEGER,
	detail		IN VARCHAR2
)
IS
    error_msg	    VARCHAR2(100);
BEGIN
    IF error_code = 1 THEN
	error_msg := 'MRO0001: Unspecified error';
    --ELSIF error_code = 10 THEN
	--error_msg := 'MRO0010: No Data Found';
   -- ELSIF error_code = 20 THEN
	--error_msg := 'MRO0020: Error executing dynamic PL/SQL block';
  --  ELSIF error_code = 30 THEN
	--error_msg := 'MRO0030: Invalid code';* /
    ELSE
	error_msg := 'MRO0000: Unknown Error';
    END IF;

    ROLLBACK;

    MEME_UTILITY.PUT_ERROR(
      'Error in MRD_OPERATIONS::'||method||' at '||
	   location||' ('||error_msg||','||detail||')');

    COMMIT;

END mrd_operations_error;


/****************** NO LONGER USED ****************************************
  handle_norm_change_m{i,r}d

--PROCEDURE HANDLE_NORM_CHANGE_MID **************************************** /
-- Called when a "Norm Change Event" occurs
-- It updates the lui fields in classes/dead_classes
-- and the norm_string field in string_ui
PROCEDURE handle_norm_change_mid
IS
    CURSOR c1 IS
    SELECT atom_id,b.lui
    FROM classes a, string_ui b
		    WHERE a.sui=b.sui AND a.lui!=b.lui;

    CURSOR c2 IS
    SELECT atom_id,b.lui
    FROM dead_classes  a, string_ui b
    WHERE a.sui=b.sui AND a.lui!=b.lui;

    cr   c2%ROWTYPE;
BEGIN

    initialize_trace('HANDLE_HORM_CHANGE_MID');

    -- Open cursor for updating classes
    location := '10';
    OPEN c1;
    LOOP
	location := '10.1';
        FETCH c1 INTO cr;
        EXIT WHEN c1%NOTFOUND;

	location := '10.2';
	UPDATE classes SET lui=cr.lui
	WHERE atom_id=cr.atom_id;

    END LOOP;
    CLOSE c1;

    -- Open cursor for updating dead_classes
    location := '20';
    OPEN c2;
    LOOP
	location := '20.1';
        FETCH c2 INTO cr;
        EXIT WHEN c2%NOTFOUND;

	location := '20.2';
        UPDATE dead_classes SET lui=cr.lui
	WHERE atom_id=cr.atom_id;

    END LOOP;
    CLOSE c2;

    -- We need code to update string_ui!

EXCEPTION

    WHEN OTHERS THEN
	mrd_operations_error(method,location,1,SQLERRM);
	RAISE mrd_operations_exception;

END handle_norm_change_mid;


--PROCEDURE HANDLE_NORM_CHANGE_MRD **************************************** /
-- Called when a "Norm Change Event' occurs
-- It generates new mrd_classes states for the udpated luis
PROCEDURE handle_norm_change_mrd
IS
    CURSOR cur IS
    SELECT DISTINCT aui, b.lui
    FROM mrd_classes a, string_ui b
    WHERE a.sui = b.sui
      AND a.lui != b.lui
      AND expiration_date IS NULL;

    cr 				cur%ROWTYPE;
    st_timestamp		DATE;
BEGIN

    initialize_trace('HANDLE_NORM_CHANGE_MRD');

    -- Get current time to use as insertion date for
    -- new mrd_classes states
    location := '10';
    SELECT SYSDATE INTO st_timestamp FROM dual;

    location := '20';
    OPEN cur;
    LOOP
	location := '20.1';
        FETCH cur INTO cr;
        EXIT WHEN cur%NOTFOUND;

	-- Insert rows into mrd_classes where luis changed
	location := '20.2';
        INSERT INTO mrd_classes
	    (aui, cui, lui, isui, sui, suppressible, language, root_source,
             tty, code, insertion_date, expiration_date)
        SELECT DISTINCT aui, cui, cr.lui, isui, sui,
		suppressible, language, root_source, tty, code,
               st_timestamp, null
        FROM mrd_classes
        WHERE aui = cr.aui
          AND expiration_date IS NULL;

	-- Expire rows with "old" luis
	location := '20.3';
        UPDATE mrd_classes
        SET expiration_date = st_timestamp
        WHERE aui = cr.aui
          AND lui != cr.lui
          AND expiration_date IS NULL;

      END LOOP;

EXCEPTION

    WHEN OTHERS THEN
	mrd_operations_error(method,location,1,SQLERRM);
	RAISE mrd_operations_exception;

END handle_norm_change_mrd;
**************************************************************************/

-- FUNCTION IS_CONCEPT_CLEAN *********************************************/
-- A concept is "clean" if all of its elements have a status of R or U.
-- This returns 1 if the concept is clean and 0 if not
FUNCTION is_concept_clean(
    concept_id in NUMBER
)  RETURN INTEGER
IS
    rowcount		INTEGER;
BEGIN

    initialize_trace('IS_CONCEPT_CLEAN');

    -- Check status in concept_Status
    location := '10';
    SELECT count(*) INTO rowcount
    FROM concept_status
    WHERE concept_id = is_concept_clean.concept_id
    AND status NOT IN ('R', 'U');

    IF rowcount > 0 THEN
	RETURN 0;
    END IF;

    -- Check status in classes
    location := '20';
    SELECT count(*) INTO rowcount
    FROM classes
    WHERE concept_id = is_concept_clean.concept_id
    AND status NOT IN ('R', 'U');

    IF rowcount > 0 THEN
	RETURN 0;
    END IF;

    -- Check status in attributes
    location := '30';
    SELECT count(*) INTO rowcount
    FROM attributes
    WHERE concept_id = is_concept_clean.concept_id
    AND status NOT IN ('R', 'U');

    IF rowcount > 0 THEN
	RETURN 0;
    END IF;

    -- Check status in relationships
    location := '40';
    SELECT count(*) INTO rowcount FROM (
    	SELECT *
	FROM relationships
	WHERE concept_id_1 = is_concept_clean.concept_id
	AND status NOT IN ('R', 'U', 'S')
	UNION
	SELECT *
	FROM relationships
	WHERE concept_id_2 = is_concept_clean.concept_id
	AND status NOT IN ('R', 'U', 'S'));

    IF rowcount > 0 THEN
	RETURN 0;
    END IF;

    -- Concept is "clean" return 1
    RETURN 1;

EXCEPTION

    WHEN OTHERS THEN
	mrd_operations_error(method,location,1,SQLERRM);
	RAISE mrd_operations_exception;

END is_concept_clean;

-- PROCEDURE UPDATE_CLEAN_CONCEPTS *****************************************/
-- Add or delete rows from clean_concepts based on the cleanliness
-- of the concept_ids in the table passed in
PROCEDURE update_clean_concepts (
    table_name IN VARCHAR2
)
IS
    TYPE curvar_type IS REF CURSOR;

    curvar		curvar_type;
    c_id		INTEGER;
    rowcount		INTEGER;
BEGIN

    initialize_trace('UPDATE_CLEAN_CONCEPTS');

    -- Open cursor to loop through table_name
    location:='10';
    OPEN curvar FOR 'SELECT DISTINCT concept_id FROM '|| table_name ;
    LOOP
	location := '10.1';
	FETCH curvar INTO c_id;
	EXIT WHEN curvar%NOTFOUND;

  	-- If the concept is clean and not already in
 	-- clean_concepts, add it.
	IF MRD_OPERATIONS.is_concept_clean(c_id) = 1 THEN
	    location := '10.2';
  	    SELECT count(concept_id) INTO rowcount
  	    FROM clean_concepts
	    WHERE concept_id = c_id;

	    IF rowcount = 0 THEN
	 	location := '10.3';
		INSERT INTO clean_concepts VALUES ( c_id );
  	    END IF;
	ELSE
	    location := '10.4';
	    DELETE FROM clean_concepts
	    WHERE concept_id =  c_id;
	END IF;

    END LOOP;

EXCEPTION

    WHEN OTHERS THEN
	mrd_operations_error(method,location,1,SQLERRM);
	RAISE mrd_operations_exception;

END update_clean_concepts;

-- FUNCTION initialize_connected_sets ***********************/
-- This function takes a table name and loads the
-- connected_sets table where it overlaps with clean_concepts
-- and then returns the rowcount.
-- It is used by the machinery that processes batch actions
FUNCTION initialize_connected_sets(
    table_name IN VARCHAR2
) RETURN INTEGER
IS
    rowcount	INTEGER;
BEGIN

    initialize_trace('INITIALIZE_CONNECTED_SETS');

    -- Truncate connected sets table
    location := '10';
    MEME_SYSTEM.truncate('connected_sets');

    -- Load connected sets from table_name passed in
    -- Where it joins with clean_concepts
    location:='20';
    rowcount:= MEME_UTILITY.exec(
     	'INSERT INTO connected_sets (concept_id, set_id)
         SELECT concept_id, rownum
	 FROM (SELECT DISTINCT concept_id
	       FROM '|| table_name || '
	       INTERSECT
	       SELECT concept_id FROM clean_concepts) ' );

    RETURN rowcount;

EXCEPTION

    WHEN OTHERS THEN
	mrd_operations_error(method,location,1,SQLERRM);
	RAISE mrd_operations_exception;

END initialize_connected_sets;

-- FUNCTION CALCULATE_CONNECTED_SET *****************************************/
-- This function calculates connected sets of concepts.  It starts with
-- a certain set and adds to it those things that are "connected".
-- Connected is determined by one of these things
-- 1. Shared last_release_cui values
-- 2. New relationships
-- 3. Connections expressed in connected_concepts
--
-- If the batch flag is off, it assumes that all set_ids are 0
-- Otherwise it assumes that the table already has different set ids.
FUNCTION calculate_connected_set(
    batch_flag  		IN INTEGER
)
RETURN INTEGER
IS
    TYPE curvar_type IS REF CURSOR;

    curvar		curvar_type;
    rowcount1		INTEGER :=1;
    rowcount2		INTEGER :=1;
    counter		INTEGER;
    n_id		INTEGER;
    o_id		INTEGER;
    row_id		ROWID;
BEGIN

    initialize_trace('CALCULATE_CONNECTED_SET');

    -- The status field prevents the algorithm  to look for connected concepts
    -- of a specific concept again and again.
    -- If first inserted the status is 0. If the status of a concept c is
    -- set to 1, the algorithm below looks up the concepts directly connected
    -- to c (which get status 0 until after one run of the algorithm)
    -- Afterwards, the status is set to 2 to indicate that the neighbours of
    -- this concept are already found.

    location := '20';
    UPDATE connected_sets
    SET status = 1;

    -- delete concepts which belong to a cluster with unclean concepts
    IF batch_flag = 1 THEN
	location := '30';
	DELETE FROM connected_sets
	WHERE concept_id NOT IN
	    (SELECT concept_id FROM clean_concepts);
    END IF;

    -- Main loop, the "connectedness" predicate is implemented here
    LOOP
        -- Analyze data for starting set
        location := '10';
        MEME_SYSTEM.analyze('connected_sets');

	location:='40';

	INSERT INTO connected_sets
	   (concept_id,set_id,status)
	((SELECT a.concept_id, b.set_id, 0
	FROM classes a, classes c, connected_sets b
	WHERE a.last_release_cui = c.last_release_cui
          AND b.status = 1 and b.concept_id=c.concept_id
	  AND a.concept_id != c.concept_id
	  AND a.last_release_cui IS NOT NULL
          AND c.last_release_cui IS NOT NULL)
	UNION
	(SELECT  a.concept_id_2 AS concept_id, b.set_id, 0
	FROM relationships a, connected_sets b
	WHERE b.status = 1
	  AND a.concept_id_1 = b.concept_id
	  AND a.rui IN (SELECT rui FROM relationships)
          AND a.rui NOT IN
	  (SELECT rui FROM mrd_relationships
           WHERE expiration_date IS NULL))
	UNION
	(SELECT a.concept_id_1 AS concept_id, b.set_id, 0
	FROM relationships a, connected_sets b
	WHERE b.status = 1
	  AND a.concept_id_2 = b.concept_id
	  AND a.rui IN (SELECT rui FROM relationships)
          AND a.rui NOT IN
	  (SELECT rui FROM mrd_relationships
           WHERE expiration_date IS NULL))
	UNION
	(SELECT a.concept_id_2 AS concept_id, b.set_id, 0
	FROM connected_concepts a, connected_sets b
	WHERE b.status = 1
	  AND a.concept_id_1 = b.concept_id)
	UNION
	(SELECT a.concept_id_1 AS concept_id, b.set_id, 0
	FROM connected_concepts a, connected_sets b
	WHERE b.status = 1
	  AND a.concept_id_2 = b.concept_id)
	)
        -- only concept_id's that are not yet in the table under this set_id.
	MINUS
	SELECT concept_id, set_id, 0 FROM connected_sets;

	EXIT WHEN SQL%ROWCOUNT = 0;

	IF batch_flag = 1 THEN

	    location := '50';
	    LOOP
	 	location := '50.0';
	        OPEN curvar FOR
		    'SELECT a.rowid, d.new_id
		     FROM connected_sets a,
	  	          (SELECT min(b.set_id) AS new_id, c.set_id AS old_id
	   	           FROM connected_sets b, connected_sets c
	   	           WHERE b.concept_id = c.concept_id
	   	 	     AND b.set_id < c.set_id
	    	           GROUP BY c.set_id) d
 		     WHERE a.set_id = old_id';
	        LOOP
		    location := '50.4';
		    FETCH curvar INTO row_id, n_id;
		    EXIT WHEN curvar%NOTFOUND;

		    location := '50.5';
		    UPDATE connected_sets
		    SET set_id = n_id
		    WHERE rowid = row_id;
	    	END LOOP;

		EXIT WHEN curvar%ROWCOUNT = 0;


	    END LOOP;

	END IF;

	-- delete concepts which belong to a cluster with unclean concepts --
	-- This applies to all calls, not just batch mode
  	location := '60';
	DELETE FROM connected_sets
	WHERE set_id NOT IN
        (SELECT DISTINCT set_id FROM connected_sets WHERE concept_id IN
	  (SELECT concept_id FROM clean_concepts));

	location := '70';
	-- sets the status of the concepts whose neighbours are now found to 2
	-- indicating that it is no longer necessary to look at them.
   	--
	-- sets the status of the newly inserted concepts to 1 indicating that
	-- the algorithm should find their direct neighbours in the next run
	-- through the loop.
	UPDATE connected_sets
	SET status = DECODE(status,0,1,1,2)
        WHERE status !=2;

    END LOOP;

    -- if connected_sets is not empty, there are cluster(s) of clean connected
    -- concepts. These will be stored in the connected_set table. THe
    -- assign_cuis procedure gets them from there.
    location := '90';
    MEME_SYSTEM.truncate('connected_set');

    -- Load connected_set with concepts from all sets
    location:= '100';
    INSERT INTO connected_set(concept_id)
	(SELECT DISTINCT concept_id
	FROM connected_sets);

    -- Analyze the connected set
    location := '110';
    MEME_SYSTEM.analyze('connected_set');

    -- The connected_sets table gets not truncated at the moment because it is
    -- easier to debug the program if this table is still visible. But as soon
    -- as the MRD is working, it should get truncated.
    -- MEME_SYSTEM.truncate('connected_sets');

    -- Return the concept count
    return MEME_UTILITY.exec_count('connected_set');

EXCEPTION

    WHEN OTHERS THEN
	mrd_operations_error(method,location,1,SQLERRM);
	RAISE mrd_operations_exception;

END calculate_connected_set;


-- PROCEDURE GENERATE_CORE_DATA_STATES *************************************/
--
-- This procedure is responsible for enacting the
-- core table to MRD core table transformation
-- A projection of each core table is computed and compared
-- against the corresponding MRD core table
--
-- The set difference between the mrd core table and the
-- projection results in expired states.
--
-- The set difference between the projection and the mrd
-- core table results in new mrd states.
--
PROCEDURE generate_core_data_states (
    table_name	IN VARCHAR2 := 'ALL',
    use_connected_set IN VARCHAR2 DEFAULT MEME_CONSTANTS.YES
)
IS
    st_timestamp		DATE;
    ct				NUMBER;
    -- Cursor for assigning DA attribute
    CURSOR mp_cur IS
      SELECT * FROM meme_properties
      WHERE key_qualifier='MRSAT'
      ORDER BY value;

    mp_var		mp_cur%ROWTYPE;
    prev_cui    	VARCHAR2(10) := '';
BEGIN

    initialize_trace('GENERATE_CORE_DATA_STATES');

    --
    -- Get a timestamp to use for insertion_dates
    --
    location := '10';
    SELECT SYSDATE INTO st_timestamp FROM dual;

    --
    -- Set CUIs in connected_set
    --
    location := '15';
    UPDATE connected_set a SET cui =
     (SELECT cui FROM concept_status b
      WHERE a.concept_id = b.concept_id);

    --
    -- Handle Concepts
    --
    IF table_name = 'ALL' OR table_name = 'CS' THEN
        MEME_UTILITY.put_message(LPAD('Process concept states.',45,' .'));
        location := '40.1';
        MEME_SYSTEM.truncate('tmp_concepts');
        location := '40.2';
        INSERT INTO tmp_concepts (concept_id, cui, status, major_revision_date)
        SELECT /*+ PARALLEL(cst) */
	    DISTINCT cst.concept_id, cst.cui, cst.status, cst.timestamp
        FROM concept_status cst, classes cl
        WHERE cst.concept_id = cl.concept_id
          AND cl.tobereleased NOT IN ('n','N')
	  AND cl.termgroup NOT IN ('MTH/MM','MTH/TM');

        -- Expire mrd concepts states
        location := '40.3';
        UPDATE mrd_concepts
        SET expiration_date = st_timestamp
        WHERE (concept_id, cui, status, major_revision_date) IN
	    (SELECT a.concept_id, a.cui, a.status, a.major_revision_date
  	     FROM mrd_concepts a
    	     WHERE expiration_date IS NULL
	     MINUS
    	     SELECT concept_id, cui, status, major_revision_date
    	     FROM tmp_concepts)
          AND expiration_date IS NULL;

        -- Insert new mrd concepts states
        location := '40.4';
        INSERT /*+ append */ INTO mrd_concepts
             (concept_id, cui, status, major_revision_date,
    	      insertion_date, expiration_date)
        SELECT concept_id, cui, status, major_revision_date,
	       st_timestamp, null
        FROM
    	(SELECT concept_id, cui, status, major_revision_date
	 FROM tmp_concepts
    	 MINUS
         SELECT a.concept_id, a.cui, a.status, major_revision_date
         FROM mrd_concepts a
         WHERE expiration_date IS NULL);

    END IF;


    --
    -- Handle Atoms
    --
    IF table_name = 'ALL' OR table_name = 'C' THEN

    -- Project classes into tmp_classes for concept_ids
    -- in the connected set.
    --
    -- Note: tmp_classes has both atom_id and AUI fields.  This is
    --       to make it possible to compute the relative ranking of
    --	     the foreign atoms connected to these atoms.
    location := '70.1';
    MEME_UTILITY.put_message(LPAD('Process classes states.',45,' .'));
    location := '70.2';
    MEME_SYSTEM.truncate('tmp_classes');
    location := '70.2b';
    MEME_SYSTEM.drop_indexes('tmp_classes');

    location := '70.3';
    INSERT INTO tmp_classes
	(atom_id,aui,cui,lui,isui,sui,
	 suppressible, language, root_source, tty, code,
	 source_aui, source_cui, source_dui)
    SELECT DISTINCT cl.atom_id, cl.aui, cl.last_assigned_cui, cl.lui,
	   cl.isui, cl.sui,
	   cl.suppressible, cl.language, b.root_source,
	   cl.tty, cl.code,
	   cl.source_aui, cl.source_cui, cl.source_dui
    FROM classes cl,
	 mrd_source_rank ms, mrd_source_rank b
    WHERE cl.tobereleased NOT IN ('n','N')
      AND cl.source = ms.source
      AND cl.termgroup not in ('MTH/MM','MTH/TM')
      AND ms.normalized_source = b.source
      AND ms.expiration_date IS NULL
      AND b.expiration_date IS NULL;

    --
    -- Fix the NLM02 codes, strip off anything before the RX
    --
    location := '70.3';
    UPDATE /*+ PARALLEL(a) */ tmp_classes a
    SET code = SUBSTR(code,INSTR(code,':')+1)
    WHERE root_source='RXNORM';

    COMMIT;

    --
    -- Add code for foreign classes
    -- Here we join back on tmp_classes to match up
    -- the foreign_classes eng_atom_id.  This is why
    -- tmp_classes still needs an atom_id field.
    --
    -- WHAT ABOUT SCTSPA?!
    --
    location := '70.4';
    INSERT INTO tmp_classes
        (atom_id,aui,cui,lui,isui,sui,
         suppressible, language, root_source, tty, code,
	 source_aui, source_cui, source_dui)
    SELECT DISTINCT f.atom_id, f.aui, tc.cui, f.lui, f.isui, f.sui,
           f.suppressible, f.language, b.root_source,
           f.tty, f.code,
 	   f.source_aui, f.source_cui, f.source_dui
    FROM foreign_classes f, tmp_classes tc,
	 mrd_source_rank ms, mrd_source_rank b
    WHERE f.tobereleased NOT IN ('n','N')
      AND eng_aui = tc.aui
      AND f.source = ms.source
      AND ms.normalized_source = b.source
      AND ms.expiration_date IS NULL
      AND b.expiration_date IS NULL;

    COMMIT;

    --
    -- Fix disambiguating codes (e.g. WHOFRE)
    --
    location := '70.b';
    UPDATE /*+ PARALLEL(a) */ tmp_classes a
    SET code = SUBSTR(code,1,INSTR(code,'~DA:')-1)
    WHERE code like '%~DA:%';

    COMMIT;


    location := '80';
    SELECT count(*) INTO ct
    FROM
	(SELECT aui FROM tmp_classes
	 GROUP BY aui HAVING count(distinct cui)>1);
    location := '80.1';
    IF ct > 0 THEN
	RAISE mrd_operations_exception;
    END IF;

    --
    -- Reindex tmp_classes
    --
    location := '100';
    MEME_SYSTEM.reindex('tmp_classes','N',' ');

    --
    -- AUI is now a primary key (where expiration date is null)
    --
    location := '110.1';
    UPDATE mrd_classes
    SET expiration_date = st_timestamp
    WHERE aui IN
	(SELECT aui
	 FROM
	    (SELECT aui, cui, lui, isui, sui,
		    suppressible, language,
		    root_source, tty, code, source_aui, source_cui, source_dui
	     FROM mrd_classes a
	     WHERE expiration_date IS NULL
	     MINUS
	     SELECT aui, cui, lui, isui, sui,
		    suppressible, language,
	  	    root_source, tty, code, source_aui, source_cui, source_dui
	     FROM tmp_classes)
 	)
       AND expiration_date IS NULL;

    --
    -- Insert new MRD classes states
    --
    location := '110.2';
    INSERT /*+ append */ INTO mrd_classes
	(aui,cui,lui,isui,sui,
	 suppressible,language,root_source,tty,code,
	 source_aui, source_cui, source_dui,
	 insertion_date,expiration_date)
    SELECT aui, cui, lui, isui, sui,
	   suppressible, language, root_source,  tty, code,
	   source_aui, source_cui, source_dui,
	   st_timestamp, null
    FROM
	(SELECT DISTINCT aui, cui, lui, isui, sui,
		suppressible, language, root_source, tty, code,
	        source_aui, source_cui, source_dui
	 FROM tmp_classes
    	 MINUS
    	 SELECT aui, cui, lui, isui, sui,
		suppressible, language, root_source, tty, code,
	        source_aui, source_cui, source_dui
    	 FROM mrd_classes
    	 WHERE expiration_date IS NULL);

    END IF;

    --
    -- Rebuild mrd_classes
    --
    location := '120';
    MEME_SYSTEM.rebuild_table('mrd_classes','N',' ');

    --
    -- Handle Relationships
    --
    IF table_name = 'ALL' OR table_name = 'R' THEN

    MEME_UTILITY.put_message(LPAD('Process relationships states.',45,' .'));
    location := '120.1';
    MEME_SYSTEM.truncate('tmp_relationships');
    MEME_SYSTEM.drop_indexes('tmp_relationships');

    --
    -- Source level relationships
    --
    location := '120.2';
    INSERT /*+ append */ INTO tmp_relationships
	(relationship_level, aui_1, aui_2,
	 cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
	 relationship_attribute, suppressible,
	 root_source, root_source_of_label, rui, source_rui,
	 relationship_group, rel_directionality_flag)
    SELECT /*+ parallel(r) */
	DISTINCT r.relationship_level, tc1.aui, tc2.aui,
        tc1.cui, tc2.cui,  r.sg_type_1, r.sg_type_2, r.relationship_name,
        r.relationship_attribute, r.suppressible,
	a.root_source, b.root_source, r.rui, r.source_rui,
	r.relationship_group, a.rel_directionality_flag
    FROM  relationships r, tmp_classes tc1, tmp_classes tc2,
	  mrd_source_rank ms, mrd_source_rank ms2,
	  mrd_source_rank a, mrd_source_rank b
    WHERE r.relationship_level = 'S'
      AND r.atom_id_1 = tc1.atom_id
      AND r.atom_id_2 = tc2.atom_id
      AND r.tobereleased NOT IN ('n','N')
      AND r.source = ms.source
      AND ms.normalized_source = a.source
      AND ms.expiration_date IS NULL
      AND a.expiration_date IS NULL
      AND r.source_of_label = ms2.source
      AND ms2.normalized_source = b.source
      AND ms2.expiration_date IS NULL
      AND b.expiration_date IS NULL;

    COMMIT;

    --
    -- Concept level relationships
    --
    location := '120.3';
    INSERT /*+ append */ INTO tmp_relationships
	(relationship_level, aui_1, aui_2,
	 cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
	 relationship_attribute, suppressible,
	 root_source, root_source_of_label, rui, source_rui,
	 relationship_group, rel_directionality_flag)
    SELECT /*+ parallel(r) */ DISTINCT 'C', null, null,
        tc1.cui, tc2.cui, 'CUI','CUI', r.relationship_name,
        DECODE(r.source,'NLM03',r.relationship_attribute,
		        'MTHRELA',r.relationship_attribute,''),
	       r.suppressible,
	DECODE(r.source,'NLM03','RXNORM','MTH'),
	DECODE(r.source_of_label,'NLM03','RXNORM','MTH'), r.rui, r.source_rui,
	r.relationship_group, 'N'
    FROM relationships r, tmp_concepts tc1, tmp_concepts tc2
    WHERE r.relationship_level = 'C'
      AND r.concept_id_1 = tc1.concept_id
      AND r.concept_id_2 = tc2.concept_id
      AND relationship_name not in ('XR','XS','BRT','BBT','BNT')
      AND r.tobereleased NOT IN ('n','N');

    COMMIT;

    --
    -- Parent and Sibling relationships
    -- backwards rel directionality flag
    --  'y' means stay Y on inverse, become Y for itself
    --  'n' means become Y on inverse, become N for iteself
    --
    location := '120.4';
    INSERT /*+ append */ INTO tmp_relationships
	(relationship_level, aui_1, aui_2,
	 cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
	 relationship_attribute, suppressible,
	 root_source, root_source_of_label, rui, source_rui,
	 relationship_group, rel_directionality_flag)
    SELECT /*+ PARALLEL (r) */ DISTINCT 'S', tc1.aui, tc2.aui,
        tc1.cui, tc2.cui, r.sg_type_1, r.sg_type_2, r.relationship_name,
        r.relationship_attribute, r.suppressible,
	a.root_source, b.root_source, r.rui, r.source_rui,
	r.relationship_group,
	    decode(r.relationship_name||a.rel_directionality_flag,
		'PAR', '', 'PARY', 'n', 'PARN', 'N',
	        'SIB', '', 'SIBY', 'y', 'SIBN', 'N', '')
    FROM context_relationships r, tmp_classes tc1, tmp_classes tc2,
	  mrd_source_rank ms, mrd_source_rank ms2,
	  mrd_source_rank a, mrd_source_rank b
    WHERE r.relationship_level = 'S'
      AND r.atom_id_1 = tc1.atom_id
      AND r.atom_id_2 = tc2.atom_id
      AND r.tobereleased NOT IN ('n','N')
      AND r.source = ms.source
      AND ms.normalized_source = a.source
      AND ms.expiration_date IS NULL
      AND a.expiration_date IS NULL
      AND r.source_of_label = ms2.source
      AND ms2.normalized_source = b.source
      AND ms2.expiration_date IS NULL
      AND b.expiration_date IS NULL;

    COMMIT;

    --
    -- Inverse of all relationships
    -- Here, we assume tmp_relationship contains only data from above
    --
    location := '120.5';
    INSERT /*+ append */ INTO tmp_relationships
	(relationship_level, aui_1, aui_2,
	 cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
	 relationship_attribute, suppressible,
	 root_source, root_source_of_label, rui, source_rui,
	 relationship_group, rel_directionality_flag)
    SELECT /*+ PARALLEL(tr) */ DISTINCT tr.relationship_level, aui_2, aui_1,
	 cui_2, cui_1, sg_type_2, sg_type_1, inverse_name,
	 inverse_rel_attribute, suppressible,
   	 root_source, root_source_of_label, ru.inverse_rui, source_rui,
	 relationship_group,
   decode(nvl(rel_directionality_flag,'X'),'y','Y','n','Y','Y','N','N','N','')
    FROM tmp_relationships tr, inverse_relationships_ui ru,
 	 inverse_relationships ir, inverse_rel_attributes ira
    WHERE tr.relationship_name = ir.relationship_name
      AND NVL(tr.relationship_attribute,'null') =
          NVL(ira.relationship_attribute,'null')
      AND tr.rui = ru.rui;

    COMMIT;

    --
    -- bequeathal relationships
    --
    -- This query can cause duplicate RUIs in some instances.
    --
    /******** NO LONGER NEED THESE
    location := '120.41';
    INSERT INTO tmp_relationships
	(relationship_level, aui_1, aui_2,
	 cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
	 relationship_attribute, suppressible,
	 root_source, root_source_of_label, rui, source_rui,
	 relationship_group, rel_directionality_flag)
     SELECT /*+ USE_NL(r,a)* / 'C', null, null, last_release_cui as cui1,
           b.cui as cui2, 'CUI', 'CUI', relationship_name,
         relationship_attribute, 'N',
         'MTH', 'MTH', rui, null,
         null, null
     FROM relationships r, classes a, tmp_concepts b
     WHERE r.concept_id_1=a.concept_id
       AND r.concept_id_2=b.concept_id
       AND r.relationship_name in ('BBT','BNT','BRT')
       AND r.tobereleased in ('Y','y')
       AND a.tobereleased in ('N','n')
       AND last_release_cui IS NOT NULL
       AND last_release_cui NOT IN (SELECT cui FROM tmp_concepts)
     UNION
     SELECT /*+ USE_NL(r,a)* /  'C', null, null, last_release_cui as cui1,
           b.cui as cui2, 'CUI', 'CUI', inverse_name as relationship_name,
         relationship_attribute, 'N',
         'MTH', 'MTH', rui, null,
         null, null
     FROM relationships r, classes a, tmp_concepts b,
          inverse_relationships c
     WHERE r.concept_id_2=a.concept_id
       AND r.concept_id_1=b.concept_id
       AND r.relationship_name in ('BBT','BNT','BRT')
       AND r.relationship_name=c.relationship_name
       AND r.tobereleased in ('Y','y')
       AND a.tobereleased in ('N','n')
       AND last_release_cui IS NOT NULL
       AND last_release_cui NOT IN (SELECT cui FROM tmp_concepts);

    COMMIT;
    ***********************************************************************/
    --
    -- Set rel directionality flag
    --
    location := '120.6';
    UPDATE /*+ PARALLEL(tr) */
	tmp_relationships tr
    SET rel_directionality_flag = 'N'
    WHERE rel_directionality_flag = 'n';

    COMMIT;

    --
    -- Set rel directionality flag
    --
    location := '120.7';
    UPDATE /*+ PARALLEL(tr) */
	tmp_relationships tr
    SET rel_directionality_flag = 'Y'
    WHERE rel_directionality_flag = 'y';

    COMMIT;

    --
    -- Don't do this anymore
    --
    --UPDATE /*+ PARALLEL(tr) */
    --	tmp_relationships tr
    --SET relationship_attribute =
    --	DECODE(relationship_attribute,
    --	    'precise_ingredient_of', 'ingredient_of',
    --	    'has_precise_ingredient','has_ingredient')
    --WHERE relationship_attribute IN
    --	 ('precise_ingredient_of','has_precise_ingredient');

    --
    -- UWDA siblings get RELA values of the form 'sib_in_*'
    -- However, the values should not be inversed, so for
    -- the 'part_of' tree, we should use 'sib_in_part_of'
    -- for the SIBs in both directions and never use
    -- 'sib_in_has_part'.  The following code will correct
    -- any that were inversed above...
    --
    location := '120.8';
    UPDATE /*+ PARALLEL(tr) */tmp_relationships tr
    SET relationship_attribute =
       DECODE(relationship_attribute,
	'part_of', 'sib_in_part_of', 'has_part','sib_in_part_of',
	'branch_of','sib_in_branch_of', 'has_branch', 'sib_in_branch_of',
	'tributary_of','sib_in_tributary_of',
	'has_tributary','sib_in_tributary_of',
	'isa', 'sib_in_isa', 'inverse_isa', 'sib_in_isa')
    WHERE relationship_name = 'SIB'
      AND root_source = 'UWDA'
      AND relationship_attribute IS NOT NULL;

    COMMIT;

    UPDATE /*+ PARALLEL(tr) */tmp_relationships tr
    SET relationship_attribute = null
    WHERE relationship_name = 'SIB'
      AND root_source != 'UWDA'
      AND relationship_attribute IS NOT NULL;

    COMMIT;

    --
    -- Compute release names
    --
    location := '120.9';
    UPDATE /*+ PARALLEL(tr) */tmp_relationships tr
    SET relationship_name =
	(SELECT release_name FROM inverse_relationships
	 WHERE relationship_name = tr.relationship_name)
    WHERE relationship_name in
      (SELECT relationship_name FROM inverse_relationships
	 WHERE relationship_name != release_name)
      AND relationship_name NOT IN ('BBT','BNT','BRT');

    COMMIT;

    --
    -- STYPE1
    --
    location := '130.1';
    UPDATE /*+ PARALLEL(tr) */tmp_relationships tr
       SET sg_type_1 =
       DECODE(NVL(sg_type_1,'null'),
	   'null','AUI',
	   'CONCEPT_ID', 'CUI',
	   'CUI_SOURCE','CUI',
	   'CUI_ROOT_SOURCE','CUI',
	   'CODE_SOURCE','CODE',
	   'CODE_STRIPPED_SOURCE','CODE',
	   'CODE_ROOT_SOURCE','CODE',
	   'CODE_TERMGROUP','CODE',
	   'CODE_ROOT_TERMGROUP','CODE',
	   'CUI_STRIPPED_SOURCE','CUI',
	   'CUI_ROOT_SOURCE','CUI',
	   'SOURCE_CUI','SCUI',
	   'SOURCE_AUI','SAUI',
	   'SOURCE_DUI','SDUI',
 	   'SOURCE_RUI','SRUI',
	   'ROOT_SOURCE_AUI','SAUI',
	   'ROOT_SOURCE_CUI','SCUI',
	   'ROOT_SOURCE_DUI','SDUI',
	   'ROOT_SOURCE_RUI','SRUI')
    WHERE relationship_level = 'S'
      AND NVL(sg_type_1,'null') not in ('AUI','CUI');

    COMMIT;

    --
    -- STYPE2
    --
    location := '130.2';
    UPDATE /*+ PARALLEL(tr) */tmp_relationships tr
       SET sg_type_2 =
       DECODE(NVL(sg_type_2,'null'),
	   'null','AUI',
	   'CONCEPT_ID', 'CUI',
	   'CUI_SOURCE','CUI',
	   'CUI_ROOT_SOURCE','CUI',
	   'CODE_SOURCE','CODE',
	   'CODE_STRIPPED_SOURCE','CODE',
	   'CODE_ROOT_SOURCE','CODE',
	   'CODE_TERMGROUP','CODE',
	   'CODE_STRIPPED_TERMGROUP','CODE',
	   'CODE_ROOT_TERMGROUP','CODE',
	   'CUI_STRIPPED_SOURCE','CUI',
	   'CUI_ROOT_SOURCE','CUI',
	   'SOURCE_CUI','SCUI',
	   'SOURCE_AUI','SAUI',
	   'SOURCE_DUI','SDUI',
 	   'SOURCE_RUI','SRUI',
	   'ROOT_SOURCE_AUI','SAUI',
	   'ROOT_SOURCE_CUI','SCUI',
	   'ROOT_SOURCE_DUI','SDUI',
	   'ROOT_SOURCE_RUI','SRUI')
    WHERE relationship_level = 'S'
      AND NVL(sg_type_2,'null') NOT IN ('CUI','AUI');

    COMMIT;


    location := '130.31';
    SELECT count(*) INTO ct FROM
	(SELECT rui FROM tmp_relationships
	 WHERE relationship_name NOT IN ('BRT','BBT','BNT') GROUP BY rui
	 HAVING count(distinct cui_1||aui_1||cui_2||aui_2||relationship_group)>1);
    location := '130.32';
    IF ct > 0 THEN
	RAISE mrd_operations_exception;
    END IF;


    --
    -- Reindex
    --
    MEME_SYSTEM.reindex('tmp_relationships','N',' ');

    --
    -- Expire old MRD relationship states
    --
    location := '130.4';
    UPDATE mrd_relationships
    SET expiration_date = st_timestamp
    WHERE rui IN
	(SELECT rui
   	 FROM
	    (SELECT relationship_level, aui_1, aui_2,
		cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
		relationship_attribute, suppressible,
		root_source, root_source_of_label,
		rui, source_rui, relationship_group,
		rel_directionality_flag
	     FROM mrd_relationships mrel
	     WHERE expiration_date IS NULL
	    MINUS
	    SELECT relationship_level, aui_1, aui_2,
	    	cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
		relationship_attribute, suppressible,
		root_source, root_source_of_label,
		rui, source_rui, relationship_group,
		rel_directionality_flag
	    FROM tmp_relationships)
	)
      AND expiration_date IS NULL;

    --
    -- Insert new MRD relationship states
    --
    location := '130.5';
    INSERT /*+ append */ INTO mrd_relationships
	(relationship_level, aui_1, aui_2,
	 cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
	 relationship_attribute, suppressible,
	 root_source, root_source_of_label, rui,
	 source_rui, relationship_group,
	 rel_directionality_flag, insertion_date, expiration_date)
    SELECT /*+ parallel(r) */ relationship_level, aui_1, aui_2,
	   cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
	   relationship_attribute, suppressible,
	   root_source, root_source_of_label, rui,
	   source_rui, relationship_group,
	   rel_directionality_flag, st_timestamp, null
    FROM tmp_relationships
    MINUS
    SELECT /*+ parallel(m) */ relationship_level, aui_1, aui_2,
	   cui_1, cui_2, sg_type_1, sg_type_2, relationship_name,
	   relationship_attribute, suppressible,
	   root_source, root_source_of_label, rui,
	   source_rui, relationship_group,
	   rel_directionality_flag, st_timestamp, null
    FROM mrd_relationships m
    WHERE expiration_date IS NULL;

    --
    -- Rebuild mrd_relationships
    --
    location := '120';
    MEME_SYSTEM.rebuild_table('mrd_relationships','N',' ');

    END IF;



    --
    -- Handle Attributes
    --
    IF table_name = 'ALL' OR table_name = 'A' THEN

    location := '140.1';
    MEME_UTILITY.put_message(LPAD('Process attributes states.',45,' .'));

    --
    -- Truncate and drop indexes
    --
    location := '140.2';
    MEME_SYSTEM.truncate('tmp_attributes');
    MEME_SYSTEM.drop_indexes('tmp_attributes');

    --
    -- Source level attributes
    --
    location := '140.3';
    INSERT /*+ APPEND */ INTO tmp_attributes
	(attribute_level, ui, cui, lui, sui, sg_type,
	 suppressible, attribute_name, attribute_value,
	 code, root_source, atui, source_atui, hashcode)
    SELECT /*+ PARALLEL(a) USE_HASH(a,tc) */ DISTINCT
	   a.attribute_level, tc.aui,
	   tc.cui, tc.lui, tc.sui, a.sg_type, a.suppressible,
	   a.attribute_name, a.attribute_value,
	   tc.code, ms2.root_source,
 	   a.atui, a.source_atui, a.hashcode
    FROM attributes a, tmp_classes tc,
	 mrd_source_rank ms, mrd_source_rank ms2
    WHERE a.attribute_level = 'S'
      AND sg_type not in ('SOURCE_RUI','ROOT_SOURCE_RUI')
      AND a.atom_id = tc.atom_id
      AND a.tobereleased NOT IN ('n','N')
      AND a.source = ms.source
      AND ms.normalized_source = ms2.source
      AND ms.expiration_date IS NULL
      AND ms2.expiration_date IS NULL;

    COMMIT;

    --
    -- RUI attributes
    --
    location := '140.3';
    INSERT /*+ APPEND */ INTO tmp_attributes
	(attribute_level, ui, cui, lui, sui, sg_type,
	 suppressible, attribute_name, attribute_value,
	 code, root_source, atui, source_atui, hashcode)
    SELECT /*+ PARALLEL(a) USE_HASH(a,mr) */ DISTINCT
           a.attribute_level, rui,
	   mr.cui_1, null, null, a.sg_type, a.suppressible, a.attribute_name,
	   a.attribute_value, null, ms.root_source,
	   a.atui, a.source_atui, a.hashcode
    FROM attributes a, mrd_relationships mr,
         mrd_source_rank ms, mrd_source_rank ms2
    WHERE a.attribute_level = 'S'
      AND sg_type in ('SOURCE_RUI','ROOT_SOURCE_RUI')
      AND nvl(mr.rel_directionality_flag,'Y') = 'Y'
      AND a.sg_id = mr.source_rui
      AND mr.root_source like a.sg_qualifier || '%'
      AND a.tobereleased NOT IN ('n','N')
      AND a.source = ms.source
      AND mr.expiration_date IS NULL
      AND ms.normalized_source = ms2.source
      AND ms.expiration_date IS NULL
      AND ms2.expiration_date IS NULL;

    COMMIT;

    --
    -- C level attributes
    --
    location := '140.4';
    INSERT /*+ APPEND */ INTO tmp_attributes
	(attribute_level, ui, cui, lui, sui, sg_type,
	 suppressible, attribute_name, attribute_value,
	 code, root_source, atui, source_atui, hashcode)
    SELECT /*+ PARALLEL(a) USE_HASH(a,tc) */ DISTINCT
           a.attribute_level, null, tc.cui,null, null, 'CUI',
	   a.suppressible, a.attribute_name, a.attribute_value, null,
	   a.source, a.atui, a.source_atui, a.hashcode
    FROM attributes a, tmp_concepts tc
    WHERE a.attribute_level = 'C'
      AND a.concept_id = tc.concept_id
      AND a.tobereleased NOT IN ('n','N')
      AND attribute_name != 'SEMANTIC_TYPE';

    COMMIT;

    --
    -- C level attributes (SEMANTIC_TYPES)
    --
    location := '140.4b';
    INSERT /*+ APPEND */ INTO tmp_attributes
	(attribute_level, ui, cui, lui, sui, sg_type,
	 suppressible, attribute_name, attribute_value,
	 code, root_source, atui, source_atui, hashcode)
    SELECT /*+ PARALLEL(a) USE_HASH(a,tc) */ DISTINCT
           a.attribute_level, null, tc.cui,null, null, 'CUI',
	   a.suppressible, a.attribute_name, a.attribute_value, null,
	   'MTH', a.atui, a.source_atui, a.hashcode
    FROM attributes a, tmp_concepts tc
    WHERE a.attribute_level = 'C'
      AND a.concept_id = tc.concept_id
      AND a.tobereleased NOT IN ('n','N')
      AND attribute_name = 'SEMANTIC_TYPE';

    COMMIT;

    --
    -- The 'ST' attribute is the mrd_concepts.status value for this cui.
    --
    location := '140.5';
    INSERT /*+ APPEND */ INTO tmp_attributes
	(attribute_level, ui, cui, lui, sui, sg_type,
	 suppressible, attribute_name, attribute_value,
	 code, root_source, atui, source_atui, hashcode)
    SELECT /*+ PARALLEL(a) USE_HASH(a,b) */ DISTINCT
    	    attribute_level, null, cui, null, null, sg_type,
	    'N', attribute_name, status,
	    null, root_source, atui, null, hashcode
    FROM tmp_concepts a, attributes_ui b
    WHERE b.attribute_name = 'ST'
   	  AND b.attribute_level = 'C'
   	  AND b.root_source = 'MTH'
	  AND b.sg_id = a.cui
	  AND sg_type = 'CUI'
	  AND b.hashcode = a.status
	  AND sg_qualifier IS NULL;

    COMMIT;

    -- The 'DA' attribute is the date this cui was added to the metathesaurus.
    -- Ideally, this would be calculated from mrd_concepts by looking at
    -- insertion_date, but this has 2 problems
    -- 1. there is a need to back-load the data for past Meta releases
    -- 2. insertion_Date in mrd_concepts is the date that the state was
    --    added which is not necessarily the date that the CUI was added
    --    to the Meta.
    --
    -- As an alternative, we use the data in meme_properties
    -- Where key_qualifier='MRSAT' we look at the key field (a CUI)
    -- and any CUI <= that CUI gets the value (a DA value).  We must
    -- use a cursor and track the previous value, for two rows like this:
    -- C0078863|MRSAT|19900930
    -- C0085070|MRSAT|19910815
    -- We set DA for all CUIs <= C0078863 to 19900930
    -- and we set DA for all CUIs <= C0085070 and > C0078863 to 19910815
    --
    -- This strategy relies upon the assign_cuis procedure adding a new
    -- row each time it is called with new_cui_flag='Y';
    --
    MEME_UTILITY.sub_timing_start;
    location := '140.6';
    prev_cui := 'C0000000';
    OPEN mp_cur;
    LOOP
	location := '140.6.1';
   	FETCH mp_cur INTO mp_var;

    	-- Create DA rows for this CUI range
    	location := '140.6.2';
    	EXECUTE IMMEDIATE
  	  'INSERT /*+ APPEND */ INTO tmp_attributes
		(attribute_level, ui, cui, lui, sui, sg_type,
	 	suppressible, attribute_name, attribute_value,
	 	code, root_source, atui, source_atui, hashcode)
	   SELECT /*+ USE_HASH(a,b) */  DISTINCT
    	    	''C'', null, a.cui, null, null, b.sg_type,
	    	''N'', ''DA'' , :x,
	    	null, b.root_source, b.atui, null, b.hashcode
	   FROM tmp_concepts a, attributes_ui b
	   WHERE a.cui > :x and a.cui <= :x
	     AND b.attribute_name = ''DA''
	     AND b.attribute_level = ''C''
	     AND b.root_source = ''MTH''
	     AND b.sg_id = a.cui
	     AND sg_type = ''CUI''
	     AND sg_qualifier IS NULL'
    	USING mp_var.value, prev_cui, mp_var.key;

	COMMIT;

        location := '140.6.3';
    	-- Set prev_cui
    	prev_cui := mp_var.key;

	location := '140.6.4';
    	EXIT WHEN mp_cur%NOTFOUND;

    END LOOP;

    CLOSE mp_cur;

    -- The MR attribute is the major revision date for the CUI.
    -- This is calculated into the major_revision_date field in mrd_concepts.
    --
    
    -- Look up prev max CUI for MR=00000000 cases
    location := '140.7a';
    SELECT max(key) into prev_cui 
    FROM meme_properties 
    WHERE key_qualifier='MRSAT' 
      AND key != 
      	(SELECT max(key) FROM meme_properties WHERE key_qualifier='MRSAT');
    
    location := '140.7b';
    INSERT /*+ APPEND */ INTO tmp_attributes
	(attribute_level, ui, cui, lui, sui, sg_type,
	 suppressible, attribute_name, attribute_value,
	 code, root_source, atui, source_atui, hashcode)
    SELECT /*+ PARALLEL(a) USE_HASH(a,b) */  DISTINCT
    	    attribute_level, null, cui, null, null, sg_type,
	    'N', attribute_name, TO_CHAR(major_revision_date, 'YYYYMMDD'),
	    null, root_source, atui, null, hashcode
    FROM tmp_concepts a, attributes_ui b
    WHERE b.attribute_name = 'MR'
	  AND b.attribute_level = 'C'
	  AND b.root_source = 'MTH'
	  AND b.sg_id = a.cui
	  AND sg_type = 'CUI'
	  AND b.hashcode = to_char(a.major_revision_date,'YYYYMMDD')
	  AND a.cui <= prev_cui
	  AND sg_qualifier IS NULL;

    COMMIT;

    -- If MR value is < DA value, set it to '00000000'
    MEME_UTILITY.sub_timing_start;
    location := '140.8';
    INSERT /*+ APPEND */ INTO tmp_attributes
	(attribute_level, ui, cui, lui, sui, sg_type,
	 suppressible, attribute_name, attribute_value,
	 code, root_source, atui, source_atui, hashcode)
    SELECT /*+ PARALLEL(a) USE_HASH(a,b) */  DISTINCT
    	    attribute_level, null, cui, null, null, sg_type,
	    'N', attribute_name, '00000000',
	    null, root_source, atui, null, hashcode
    FROM tmp_concepts a, attributes_ui b
    WHERE b.attribute_name = 'MR'
      AND b.attribute_level = 'C'
	  AND b.root_source = 'MTH'
	  AND b.sg_id = a.cui
	  AND sg_type = 'CUI'
	  AND b.hashcode = '00000000'
	  AND a.cui > prev_cui
	  AND sg_qualifier IS NULL;

    COMMIT;

    --
    -- Fix long attribute pointers to use hashcode
    --
    location := '140.2b';
    UPDATE /*+ PARALLEL(a) */ tmp_attributes a
    SET attribute_value = '<>Long_Attribute<>:'||hashcode
    WHERE attribute_value like '<>Long_Attribute<>:%';

    COMMIT;

    --
    -- STYPE
    --
    location := '140.9';
    UPDATE /*+ PARALLEL(a) */ tmp_attributes a
    SET sg_type =
       DECODE(NVL(sg_type,'null'),
	   'null','AUI',
	   'CONCEPT_ID', 'CUI',
	   'CUI_SOURCE','CUI',
	   'CUI_ROOT_SOURCE','CUI',
	   'CODE_SOURCE','CODE',
	   'CODE_STRIPPED_SOURCE','CODE',
	   'CODE_ROOT_SOURCE','CODE',
	   'CODE_TERMGROUP','CODE',
	   'CODE_ROOT_TERMGROUP','CODE',
	   'CUI_STRIPPED_SOURCE','CUI',
	   'CUI_ROOT_SOURCE','CUI',
	   'SOURCE_CUI','SCUI',
	   'SOURCE_AUI','SAUI',
	   'SOURCE_DUI','SDUI',
 	   'SOURCE_RUI','SRUI',
	   'ROOT_SOURCE_AUI','SAUI',
	   'ROOT_SOURCE_CUI','SCUI',
	   'ROOT_SOURCE_DUI','SDUI',
	   'ROOT_SOURCE_RUI','SRUI')
    WHERE attribute_level = 'S'
      AND nvl(sg_type,'null') not in ('AUI','CUI');

    COMMIT;

    UPDATE /*+ PARALLEL(a) */ tmp_attributes a
    SET sg_type = 'CUI'
    WHERE attribute_level = 'C'
      AND nvl(sg_type,'null') != 'CUI';

    COMMIT;

    location := '150';
    SELECT count(*) INTO ct FROM
	(SELECT atui FROM tmp_attributes
	 GROUP BY atui HAVING count(distinct cui||ui)>1);
    location := '130.32';
    IF ct > 0 THEN
	RAISE mrd_operations_exception;
    END IF;

    --
    -- Reindex tmp_attributes
    --
    location := '160.a';
    MEME_SYSTEM.reindex('tmp_attributes','N',' ');

    --
    -- Expire old MRD attributes states
    --
    location := '160';
    UPDATE mrd_attributes
    SET expiration_date = sysdate
    WHERE atui IN
	(SELECT atui
	 FROM
	    (SELECT atui, attribute_level, ui, cui,
		lui, sui, sg_type, suppressible,
		attribute_name, attribute_value, code,
		root_source, source_atui, hashcode
	     FROM mrd_attributes a
	     WHERE expiration_date IS NULL
    	     MINUS
	     SELECT atui, attribute_level, ui, cui, lui, sui, sg_type,
		suppressible, attribute_name, attribute_value, code,
		root_source, source_atui, hashcode
	     FROM tmp_attributes)
	)
      AND expiration_date IS NULL;

    --
    -- Insert new MRD attributes states
    --
    location := '170';
    INSERT /*+ APPEND */  INTO mrd_attributes
	(attribute_level, ui, cui, lui, sui, sg_type,
	 suppressible, attribute_name, attribute_value, code, root_source,
	 atui, source_atui, hashcode,
	 insertion_date, expiration_date)
    SELECT DISTINCT attribute_level, ui,cui, lui, sui, sg_type,
 	suppressible, attribute_name, attribute_value, code, root_source,
	atui, source_atui, hashcode,	st_timestamp, null
    FROM tmp_attributes a
    MINUS
    SELECT attribute_level, ui, cui, lui, sui, sg_type,
	suppressible, attribute_name, attribute_value, code, root_source,
	atui, source_atui, hashcode, st_timestamp, null
    FROM mrd_attributes m
    WHERE expiration_date IS NULL;


    --
    -- Rebuild mrd_relationships
    --
    location := '120';
    MEME_SYSTEM.rebuild_table('mrd_attributes','N',' ');

    END IF;

    location := '220';
    MEME_UTILITY.drop_it('table', 'updated_cuis');

    --
    -- Clean out connected concepts
    --
    location := '210';
    DELETE FROM connected_concepts
    WHERE concept_id_1 IN (SELECT concept_id FROM connected_set);

    location := '220';
    DELETE FROM connected_concepts
    WHERE concept_id_2 IN (SELECT concept_id FROM connected_set);

    --
    -- Clean deleted concepts out in clean_concepts
    --
    location := '230';
    DELETE FROM clean_concepts
    WHERE concept_id IN
	(SELECT concept_id FROM connected_set
	 MINUS
	 SELECT concept_id FROM concept_status);

    MEME_UTILITY.put_message(
	LPAD('Finished generating core data states.',45,' .'));

EXCEPTION

    WHEN OTHERS THEN
	mrd_operations_error(method, location, 1,SQLERRM);
	RAISE mrd_operations_exception;

END generate_core_data_states;

--*************** PROCEDURE GENERATE_AUXILIARY_DATA_STATES ******************/
-- This procedure is responsible for enacting MID-MRD
-- data transformations for non-core tables.
--
-- Typically the transformation involves comparing the MID
-- table to the MRD table in its entirety and expiring any
-- MRD table differences and inserting any MID table differences.
--
PROCEDURE generate_auxiliary_data_states(
    table_name 	IN VARCHAR2
)
IS
    TYPE ct IS REF CURSOR;
    cv				ct;
    row_id1			ROWID;
    row_id2			ROWID;
    st_timestamp		DATE;
    flag			VARCHAR2(1):= MEME_CONSTANTS.YES;
    start_year 			INTEGER := 1800;
    end_year 			INTEGER := 1960;
BEGIN

    initialize_trace('GENERATE_AUXILIARY_DATA_STATES');

    -- Get timestamp for insertion dates
    location := '10';
    SELECT SYSDATE INTO st_timestamp FROM dual;

    location := '10.1';
    EXECUTE IMMEDIATE 'ALTER SESSION SET sort_area_size=33554432';
    location := '10.2';
    EXECUTE IMMEDIATE 'ALTER SESSION SET hash_area_size=33554432';

    -- Deal with source_rank changes (source_version too)
    IF LOWER(table_name) = 'source_rank' OR
	LOWER(table_name) = 'source_version' OR
	LOWER(table_name) = 'sims_info' THEN

	-- Expire old mrd source_rank states
	location := '20.1';
 	EXECUTE IMMEDIATE '
	UPDATE mrd_source_rank
	SET expiration_date = :x
	WHERE (source, rank, restriction_level, normalized_source,
	       NVL(root_source, ''null''), NVL(source_official_name, ''null''),
	       NVL(source_short_name, ''null''), NVL(citation, ''null''),
	       NVL(source_family, ''null''), NVL(version, ''null''),
	       NVL(to_char(valid_start_date), ''null''),
	       NVL(character_set, ''null''),
	       NVL(to_char(valid_end_date), ''null''),
	       NVL(insert_meta_version, ''null''),
	       NVL(remove_meta_version, ''null''),
	       NVL(nlm_contact, ''null''), NVL(content_contact, ''null''),
	       NVL(license_contact, ''null''), NVL(release_url_list, ''null''),
	       NVL(context_type, ''null''), NVL(language,''null''),
	       NVL(rel_directionality_flag,''null''),
		is_current) IN
	     (SELECT source, rank, restriction_level, normalized_source,
	         NVL(root_source, ''null''),
	         NVL(source_official_name, ''null''),
		 NVL(source_short_name, ''null''), NVL(citation, ''null''),
	         NVL(source_family, ''null''), NVL(version, ''null''),
	         NVL(to_char(valid_start_date), ''null''),
		 NVL(character_set, ''null''),
	         NVL(to_char(valid_end_date), ''null''),
	         NVL(insert_meta_version, ''null''),
		 NVL(remove_meta_version, ''null''),
	         NVL(nlm_contact, ''null''), NVL(content_contact, ''null''),
	         NVL(license_contact, ''null''),
		 NVL(release_url_list, ''null''),
	         NVL(context_type, ''null''), NVL(language,''null''),
		 NVL(rel_directionality_flag,''null''),
	         is_current
	     FROM mrd_source_rank
	     WHERE expiration_date IS NULL
	     MINUS
	     SELECT a.source, rank, restriction_level, normalized_source,
	         NVL(stripped_source, ''null''),
		 NVL(source_official_name, ''null''),
	 	 NVL(source_short_name, ''null''), NVL(citation, ''null''),
	         NVL(source_family, ''null''), NVL(version, ''null''),
	         NVL(to_char(valid_start_date), ''null''),
		 NVL(character_set, ''null''),
	         NVL(to_char(valid_end_date), ''null''),
	         NVL(insert_meta_version, ''null''),
		 NVL(remove_meta_version, ''null''),
	         NVL(nlm_contact, ''null''), NVL(content_contact, ''null''),
	         NVL(license_contact, ''null''),
		 NVL(release_url_list, ''null''),
	         NVL(context_type, ''null''), NVL(language, ''null''),
	         NVL(rel_directionality_flag,''null''),
		 DECODE((SELECT count(*) FROM source_version b
	 	  WHERE a.source=current_name
		    AND stripped_source=b.source),1,''Y'',0,''N'') as is_current
	     FROM source_rank a, sims_info b WHERE a.normalized_source=b.source )
	  AND expiration_date IS NULL'
	USING st_timestamp;

	-- Insert new mrd_source_rank states
	location := '20.2';
	EXECUTE IMMEDIATE '
 	INSERT INTO mrd_source_rank
	    (source,rank,restriction_level,normalized_source,root_source,
	     source_official_name, source_short_name, citation,
	     source_family, version, valid_start_date, character_set,
	     valid_end_date, insert_meta_version, remove_meta_version,
	     nlm_contact, content_contact,
	     license_contact, release_url_list, context_type, language,
	     is_current, rel_directionality_flag,
	     insertion_date, expiration_date )
	SELECT source,rank,restriction_level,normalized_source,
	     stripped_source,
	     source_official_name, source_short_name, citation,
	     source_family, version, valid_start_date, character_set,
	     valid_end_date, insert_meta_version, remove_meta_version,
	     nlm_contact, content_contact,
	     license_contact, release_url_list, context_type, language,
	     is_current, rel_directionality_flag, :x, null
 	FROM
	(SELECT a.source,rank,restriction_level,normalized_source,
	     stripped_source,
	     source_official_name, source_short_name, citation,
	     source_family, version, valid_start_date, character_set,
	     valid_end_date, insert_meta_version, remove_meta_version,
	     nlm_contact, content_contact,
	     license_contact, release_url_list, context_type, language,
		 DECODE((SELECT count(*) FROM source_version b
	 	  WHERE a.normalized_source=current_name
		    AND stripped_source = b.source),1,''Y'',0,''N'') as is_current,
		rel_directionality_flag
 	 FROM source_rank a, sims_info b WHERE a.normalized_source=b.source
	 MINUS
	 SELECT source,rank,restriction_level,normalized_source,root_source,
	     source_official_name, source_short_name, citation,
	     source_family, version, valid_start_date, character_set,
	     valid_end_date, insert_meta_version, remove_meta_version,
	     nlm_contact, content_contact,
	     license_contact, release_url_list, context_type, language,
	     is_current, rel_directionality_flag
	 FROM mrd_source_rank
	 WHERE expiration_date IS NULL ) '
	USING st_timestamp;

    -- Process termgroup_rank changes
    ELSIF LOWER(table_name) = 'termgroup_rank' THEN

	-- Expire old MRD termgroup states
	location := '30.1';
	UPDATE mrd_termgroup_rank
	SET expiration_date = st_timestamp
	WHERE (rank, termgroup, normalized_termgroup,
	       NVL(tty,'null'), suppressible) IN
	    (SELECT rank, termgroup, normalized_termgroup,
		    NVL(tty,'null'), suppressible
	     FROM mrd_termgroup_rank
             WHERE expiration_date IS NULL
	     MINUS
	     SELECT a.release_rank, termgroup, b.normalized_source ||
			  SUBSTR(termgroup,INSTR(termgroup,'/')),
		NVL(tty,'null'), suppressible
	     FROM termgroup_rank a, source_rank b
	     WHERE substr(termgroup,0,INSTR(termgroup,'/')-1) = source)
 	  AND expiration_date IS NULL;

	-- Insert new MRD termgroup states
	location := '30.2';
	INSERT INTO mrd_termgroup_rank
	    (rank,termgroup,normalized_termgroup,
	     tty,suppressible,insertion_date,expiration_date)
	SELECT a.release_rank, termgroup, b.normalized_source ||
			  SUBSTR(termgroup,INSTR(termgroup,'/')),
		tty, suppressible, st_timestamp, null
	     FROM termgroup_rank a, source_rank b
	     WHERE substr(termgroup,0,INSTR(termgroup,'/')-1) = source
	MINUS
	SELECT rank, termgroup, normalized_termgroup,
		tty, suppressible, st_timestamp, null
	FROM mrd_termgroup_rank
	WHERE expiration_date IS NULL;

    -- Process stringtab changes
    -- We have an mrd_stringtab table because stringtab is
    -- not a historical table and we need to save the data
    ELSIF LOWER(table_name) = 'stringtab' THEN

	-- First, expire active MRD states where the
	-- ow is no longer in stringtab
	location := '70.1';
	UPDATE mrd_stringtab
	SET expiration_date = st_timestamp
	WHERE (hashcode, row_sequence, text_total, text_value) in
	    (SELECT hashcode, row_sequence,text_total, text_value
	     FROM mrd_stringtab
	     WHERE expiration_date IS NULL
	     MINUS
	     SELECT b.hashcode, row_sequence, text_total, text_value
	     FROM stringtab a,
	        (SELECT /*+ PARALLEL(a) */ hashcode,
	 	   to_number(substr(attribute_value,20)) as string_id
	         FROM attributes a WHERE tobereleased in ('Y','y')
	 	 AND attribute_value like '<>Long_Attribute<>:%') b
	     WHERE a.string_id = b.string_id )
	  AND expiration_date IS NULL;

	location := '70.2';
	-- Next, make MRD states for stringtab rows
	-- that are not in mrd_stringtab
	INSERT INTO mrd_stringtab
	    (hashcode, row_sequence, text_total, text_value,
	     insertion_date, expiration_date)
	SELECT hashcode, row_sequence, text_total, text_value,
		 st_timestamp, null
  	FROM
	 (SELECT b.hashcode, row_sequence, text_total, text_value
	     FROM stringtab a,
	        (SELECT /*+ parallel(a) */hashcode,
		    to_number(substr(attribute_value,20)) as string_id
	         FROM attributes a WHERE tobereleased in ('Y','y')
	 	 AND attribute_value like '<>Long_Attribute<>:%') b
	     WHERE a.string_id = b.string_id
	  MINUS
	  SELECT hashcode, row_sequence, text_total, text_value
	  FROM mrd_stringtab WHERE expiration_date IS NULL );

    -- Process contexts changes
    ELSIF LOWER(table_name) = 'context_relationships' THEN

	-- Expire old MRD contexts states
	-- Elapsed: 00:29:09.17
	location := '80.1';
	UPDATE mrd_contexts
	SET expiration_date = st_timestamp
	WHERE (aui, NVL(parent_treenum, 'null'),root_source,
	       NVL(hierarchical_code, 'null'),
	       NVL(relationship_attribute, 'null'), release_mode,
	       NVL(rui,'null'), NVL(source_rui,'null'),
	       NVL(relationship_group,'null')) IN
	    (SELECT aui, NVL(parent_treenum, 'null'), root_source,
		    NVL(hierarchical_code, 'null'),
  		    NVL(relationship_attribute, 'null'), release_mode,
		    NVL(rui,'null'), NVL(source_rui,'null'),
	            NVL(relationship_group,'null')
	     FROM
	     (SELECT aui, parent_treenum, root_source, hierarchical_code,
		     relationship_attribute, release_mode,
		     rui, source_rui, relationship_group
	      FROM mrd_contexts
              WHERE expiration_date IS NULL
  	      MINUS
  	      (SELECT /*+ parallel(a) */
		     aui, parent_treenum, r.root_source, hierarchical_code,
  		     b.relationship_attribute, release_mode,
		     rui, source_rui, relationship_group
  	       FROM context_relationships a, inverse_rel_attributes b, classes,
				mrd_source_rank ms, mrd_source_rank r
	       WHERE relationship_name='PAR'
	 	 AND a.tobereleased in ('Y','y')
		 AND a.source = ms.source
		 AND ms.normalized_source = r.source
		 AND ms.expiration_date IS NULL
		 AND r.expiration_date IS NULL
	 	 AND NVL(a.relationship_attribute,'null') = NVL(b.inverse_rel_attribute,'null')
		 AND atom_id = atom_id_1
    	       UNION ALL
	        -- highest level parents (not in context_relationships
	        --by themselves)
  	       SELECT /*+ parallel(a) */
		      b.aui, null, r.root_source, null, null, '11',
		      null, null, null
  	       FROM context_relationships a, classes b,
		    mrd_source_rank ms, mrd_source_rank r
  	       WHERE aui = parent_treenum
		 AND relationship_name='PAR'
		 AND a.tobereleased in ('Y','y')
		 AND a.source = ms.source
		 AND ms.normalized_source = r.source
		 AND ms.expiration_date IS NULL
		 AND r.expiration_date IS NULL
		 AND atom_id_2 = atom_id)
	     ) )
	  AND expiration_date IS NULL;

	-- Insert new MRD contexts states
	location := '80.2';
	INSERT INTO mrd_contexts
	    (aui, parent_treenum, root_source, hierarchical_code,
	     relationship_attribute, release_mode,
	     rui, source_rui, relationship_group, insertion_date,
	     expiration_date)
        SELECT DISTINCT aui, parent_treenum, root_source, hierarchical_code,
             relationship_attribute, release_mode,
	     rui, source_rui, relationship_group, st_timestamp, null
        FROM
        ((SELECT /*+ PARALLEL(a) */
		aui, parent_treenum, r.root_source, hierarchical_code,
		b.relationship_attribute, release_mode,
		rui, source_rui, relationship_group
        FROM context_relationships a, inverse_rel_attributes b, classes c,
			 mrd_source_rank ms, mrd_source_rank r
	WHERE relationship_name='PAR'
	  AND a.tobereleased in ('Y','y')
	  AND c.tobereleased in ('Y','y')
  	  AND NVL(a.relationship_attribute,'null') =
		NVL(b.inverse_rel_attribute,'null')
	  AND a.source = ms.source
	  AND ms.normalized_source = r.source
	  AND ms.expiration_date IS NULL
	  AND r.expiration_date IS NULL
	  AND c.atom_id = atom_id_1
    	UNION ALL
  	SELECT /*+ parallel(a) */
		aui, null, r.root_source, null, null, '11',
		null, null, null
  	FROM context_relationships a, classes b,
		 mrd_source_rank ms, mrd_source_rank r
  	WHERE aui = parent_treenum
	  AND a.tobereleased in ('Y','y')
	  AND b.tobereleased in ('Y','y')
	  AND relationship_name='PAR'
	  AND a.source = ms.source
	  AND ms.normalized_source = r.source
	  AND ms.expiration_date IS NULL
	  AND r.expiration_date IS NULL
	  AND atom_id_2 = atom_id)
        MINUS
  	SELECT aui, parent_treenum, root_source, hierarchical_code,
		relationship_attribute,	release_mode,
		rui, source_rui, relationship_group
  	FROM mrd_contexts
  	WHERE expiration_date IS NULL);

    -- Process meme_properties changes
    ELSIF LOWER(table_name) = 'meme_properties' THEN

  	location := '90.a';
	EXECUTE IMMEDIATE 'TRUNCATE TABLE tmp_properties';

	-- populate tmp_properties
  	location := '90.0';
	INSERT INTO tmp_properties SELECT * FROM meme_properties;

	--
        -- Remove unused MRDOC TTY rows
	--
        location := '90.21';
	DELETE FROM tmp_properties
	WHERE key_qualifier = 'TTY'
	  AND value IN
	  (SELECT value FROM tmp_properties
	   WHERE key_qualifier='TTY'
	   MINUS
	   SELECT DISTINCT tty FROM mrd_classes WHERE expiration_date IS NULL);

	--
	-- Remove unused MRDOC ATN rows
	--
	location := '90.22';
	DELETE FROM tmp_properties
	 WHERE key_qualifier='ATN'
	   AND value NOT LIKE 'MED____'
	   AND value IS NOT NULL
	   AND value NOT IN ('DA','MR','ST','LT','NH')
	   AND value IN
	   (SELECT value FROM tmp_properties WHERE key_qualifier='ATN'
	    MINUS
	    SELECT /*+ parallel(a) */ DISTINCT attribute_name
	     FROM mrd_attributes a
	     WHERE expiration_date IS NULL);

	--
	-- Remove unused MRDOC RELA rows
	--
	location := '90.23';
    	DELETE FROM tmp_properties
	 WHERE key_qualifier='RELA'
	   AND value IS NOT NULL
	   AND value IN
	   (SELECT value FROM tmp_properties WHERE key_qualifier='RELA'
	    MINUS
	    (SELECT DISTINCT relationship_attribute FROM mrd_relationships r
	     WHERE expiration_date IS NULL
	     UNION
	     SELECT DISTINCT relationship_attribute FROM mrd_contexts r
	     WHERE expiration_date IS NULL
	     UNION
	     SELECT DISTINCT
	      SUBSTR(attribute_value, instr(attribute_value, '~', 1, 4) + 1,
                     (INSTR(attribute_value, '~', 1, 5) -
                     INSTR(attribute_value, '~', 1, 4)) - 1)
	     FROM mrd_attributes
  	     WHERE attribute_name='XMAP' AND expiration_date IS NULL));

	--
        -- Expire old MRD properties states
	--
        location := '90.1';
        UPDATE mrd_properties
        SET expiration_date = st_timestamp
        WHERE (key, NVL(key_qualifier,'null'),
               NVL(value,'null'), NVL(description,'null'),
                    NVL(definition,'null'), NVL(example,'null'),
                    NVL(reference,'null')) IN
            (SELECT key, NVL(key_qualifier,'null'),
                    NVL(value,'null'), NVL(description,'null'),
                    NVL(definition,'null'), NVL(example,'null'),
                    NVL(reference,'null')
             FROM mrd_properties
             WHERE expiration_date IS NULL
             MINUS
             SELECT key, NVL(key_qualifier,'null'),
                    NVL(value,'null'), NVL(description,'null'),
                    NVL(definition,'null'), NVL(example,'null'),
                    NVL(reference,'null')
             FROM tmp_properties)
          AND expiration_date IS NULL;

        -- Insert new MRD properties states
        location := '90.2';
        INSERT INTO mrd_properties
            (key, key_qualifier, value, description,
	     definition, example, reference,
             insertion_date, expiration_date)
	SELECT key, key_qualifier, value, description,
	     definition, example, reference, st_timestamp, null
	FROM
  	(SELECT key, key_qualifier, value, description,
	     definition, example, reference
        FROM tmp_properties
        MINUS
        SELECT key, key_qualifier, value, description,
	     definition, example, reference
        FROM mrd_properties
        WHERE expiration_date IS NULL);


        location := '90.3';
	UPDATE mrd_file_statistics
	SET expiration_date = st_timestamp
	WHERE (file_name, column_list, description) IN
		(SELECT file_name, column_list, description
		FROM mrd_file_statistics WHERE expiration_date IS NULL
  		MINUS
  		SELECT key,value,description FROM meme_properties
  		WHERE key_qualifier='MRFILES')
  	AND expiration_date IS NULL;

        location := '90.4';
	INSERT INTO mrd_file_statistics
		(file_name, column_list, description,
		insertion_date, expiration_date)
	SELECT  file_name, column_list, description, st_timestamp, null
	FROM
	(SELECT  key as file_name,value as column_list,description
	FROM meme_properties WHERE key_qualifier='MRFILES'
	MINUS
	SELECT file_name, column_list, description
	FROM mrd_file_statistics WHERE expiration_date IS NULL);

        location := '90.5';
	UPDATE mrd_column_statistics
	SET expiration_date = st_timestamp
	WHERE (file_name, column_name, data_type, description) IN
		(SELECT file_name, column_name, data_type, description
  		FROM mrd_column_statistics WHERE expiration_date IS NULL
		MINUS
  		SELECT mrfiles.key, mrcols.key, mrcols.value, mrcols.description
  		FROM meme_properties mrfiles, meme_properties mrcols
  		WHERE mrfiles.key_qualifier = 'MRFILES'
    		AND mrcols.key_qualifier = 'MRCOLS'
    		AND (mrfiles.value like '%,'||mrcols.key||',%' OR
		     mrfiles.value like mrcols.key||',%' OR
		     mrfiles.value like '%,'||mrcols.key));

        location := '90.6';
	INSERT INTO mrd_column_statistics
		(file_name, column_name, data_type, description, min_length,
		insertion_date, expiration_date)
	SELECT file_name, column_name, data_type, description, 999999999,
		st_timestamp, null
	FROM
	(SELECT mrfiles.key as file_name, mrcols.key as column_name,
		mrcols.value as data_type, mrcols.description
  	FROM meme_properties mrfiles, meme_properties mrcols
  	WHERE mrfiles.key_qualifier = 'MRFILES'
    		AND mrcols.key_qualifier = 'MRCOLS'
    		AND (mrfiles.value like '%,'||mrcols.key||',%' OR
		     mrfiles.value like mrcols.key||',%' OR
		     mrfiles.value like '%,'||mrcols.key)
	MINUS
	SELECT file_name, column_name, data_type, description
  		FROM mrd_column_statistics WHERE expiration_date IS NULL);

    ELSIF LOWER(table_name) = 'source_coc_headings' THEN

        -- Expire old coc_headings states
        location := '100.1';
        UPDATE mrd_coc_headings
        SET expiration_date = st_timestamp
        WHERE citation_set_id IN
	    (SELECT citation_set_id FROM source_coc_headings_todelete)
          AND expiration_date IS NULL;

        -- Insert new MRD properties states
        location := '100.2';
        INSERT INTO mrd_coc_headings
            (citation_set_id,publication_date,heading_aui,
	     major_topic,subheading_set_id,
	     root_source,coc_type,
             insertion_date, expiration_date)
        SELECT citation_set_id,publication_date,aui,
	       major_topic,subheading_set_id,
	       root_source,coc_type,
               st_timestamp, null
        FROM
        (SELECT citation_set_id,publication_date,aui,
	       major_topic,subheading_set_id,
	       root_source,coc_type
        FROM source_coc_headings a, mrd_source_rank b, classes c
	WHERE a.source=b.source
	  AND a.heading_id = c.atom_id
	  AND expiration_date IS NULL
        MINUS
        SELECT citation_set_id,publication_date,heading_aui,
	       major_topic,subheading_set_id,
	       root_source,coc_type
        FROM mrd_coc_headings
        WHERE citation_set_id IN
	    (SELECT citation_set_id FROM source_coc_headings)
          AND expiration_date IS NULL);

    ELSIF LOWER(table_name) = 'source_coc_subheadings' THEN

        -- Expire old coc_subheadings states
        location := '101.1';
        UPDATE mrd_coc_subheadings
        SET expiration_date = st_timestamp
        WHERE citation_set_id IN
	    (SELECT citation_set_id FROM source_coc_headings_todelete)
          AND expiration_date IS NULL;

        -- Insert new MRD properties states
        location := '101.2';
        INSERT INTO mrd_coc_subheadings
            (subheading_set_id,citation_set_id,
  	     subheading_qa,subheading_major_topic,
             insertion_date, expiration_date)
	SELECT subheading_set_id, citation_set_id, subheading_qa,
	       subheading_major_topic, st_timestamp, null
        FROM
        (SELECT subheading_set_id, citation_set_id, subheading_qa,
	       subheading_major_topic
        FROM source_coc_subheadings
        MINUS
        SELECT subheading_set_id, citation_set_id, subheading_qa,
	       subheading_major_topic
        FROM mrd_coc_subheadings
        WHERE citation_set_id IN
	    (SELECT citation_set_id FROM source_coc_subheadings)
          AND expiration_date IS NULL);



    ELSIF LOWER(table_name) = 'foreign_classes' THEN

        -- Use tmp_classes to figure out what to keep
        location := '120.1';
        INSERT INTO tmp_classes
            (atom_id,aui,cui,lui,isui,sui,
             suppressible, language, root_source, tty, code,
	     source_aui, source_cui, source_dui)
        SELECT DISTINCT f.atom_id, f.aui, tc.cui, f.lui, f.isui, f.sui,
           f.suppressible, f.language, b.root_source,
           f.tty, NVL(f.tty,SUBSTR(f.termgroup,INSTR(f.termgroup,'/')+1)),
 	   f.source_aui, f.source_cui, f.source_dui
        FROM foreign_classes f, mrd_classes tc,
	     mrd_source_rank ms, mrd_source_rank b
        WHERE f.tobereleased NOT IN ('n','N')
          AND eng_aui = tc.aui
          AND f.source = ms.source
          AND ms.normalized_source = b.source
          AND ms.expiration_date IS NULL
          AND b.expiration_date IS NULL;

        -- Expire old mrd_classes states
        location := '120.2';
        UPDATE mrd_classes
        SET expiration_date = st_timestamp
        WHERE aui IN
            (SELECT aui
             FROM
              (SELECT aui, cui, lui, isui, sui,
                    suppressible, language,
                    root_source, tty, code,
	     	    source_aui, source_cui, source_dui
               FROM mrd_classes mcl
               WHERE expiration_date IS NULL
               MINUS
               SELECT aui, cui, lui, isui, sui,
                    suppressible, language,
                    root_source, tty, code,
	     	    source_aui, source_cui, source_dui
               FROM tmp_classes)
            )
          AND expiration_date IS NULL;

        -- Insert new MRD classes states
        location := '120.3';
        INSERT INTO mrd_classes
            (aui,cui,lui,isui,sui,
             suppressible,language,root_source,tty,code,
	     source_aui, source_cui, source_dui,
             insertion_date,expiration_date)
        SELECT aui, cui, lui, isui, sui,
	   suppressible, language, root_source, tty, code,
	   source_aui, source_cui, source_dui,
           st_timestamp, null
        FROM tmp_classes
        MINUS
        SELECT aui, cui, lui, isui, sui,
	   suppressible, language, root_source, tty, code,
	   source_aui, source_cui, source_dui,
           st_timestamp, null
        FROM mrd_classes
        WHERE expiration_date IS NULL
          AND aui in (SELECT aui FROM tmp_classes);

    ELSIF LOWER(table_name) = 'coc_headings' THEN

    	start_year := 1800;
    	end_year := 1960;

	LOOP
	    EXIT WHEN ('01-jan-'||start_year) > sysdate;
    	    location := '130.1';
    	    MEME_UTILITY.drop_it('table','t_mrd_coc_headings');
    	    MEME_UTILITY.exec(
    		'CREATE TABLE t_mrd_coc_headings as
	         SELECT citation_set_id, publication_date, heading_aui,
             	        subheading_set_id, major_topic, root_source, coc_type
      		 FROM mrd_coc_headings
      		 WHERE expiration_date IS NULL
        	   AND publication_date BETWEEN
    		       to_date(''01-jan-'||start_year||''',''DD-mon-YYYY'')
    		   AND to_date(''01-jan-'||end_year||''',''DD-mon-YYYY'')');

    	    location := '130.2';
    	    MEME_UTILITY.drop_it('table','t_coc_headings');
    	    MEME_UTILITY.exec(
    		'CREATE TABLE t_coc_headings
		   (citation_set_id, publication_date, heading_aui,
           	    subheading_set_id, major_topic, root_source, coc_type) AS
      		 SELECT citation_set_id, publication_date, aui,
             	        subheading_set_id, major_topic,
    	 	       (SELECT stripped_source FROM source_rank b
    	  		WHERE a.source = b.source) , coc_type
      		 FROM coc_headings a, classes b
      		 WHERE publication_date BETWEEN
    		        to_date(''01-jan-'||start_year||''',''DD-mon-YYYY'') AND
    		 	to_date(''01-jan-'||end_year||''',''DD-mon-YYYY'')
          	   AND heading_id = atom_id');

            -- Expire old coc_headings states
    	    location := '130.3a';
    	    EXECUTE IMMEDIATE
    		'UPDATE mrd_coc_headings
       		 SET expiration_date = sysdate
    		 WHERE (citation_set_id, publication_date, heading_aui,
           		subheading_set_id, major_topic, root_source, coc_type) IN
    		   (SELECT citation_set_id, publication_date, heading_aui,
           		subheading_set_id, major_topic, root_source, coc_type
	            FROM t_mrd_coc_headings WHERE subheading_set_id IS NOT NULL
     		    MINUS
     		    SELECT citation_set_id, publication_date, heading_aui,
           		subheading_set_id, major_topic, root_source, coc_type
		     FROM t_coc_headings WHERE subheading_set_id IS NOT NULL)
      		   AND expiration_date IS NULL
		   AND subheading_set_id IS NOT NULL';

    	    location := '130.3b';
    	    EXECUTE IMMEDIATE
    		'UPDATE mrd_coc_headings
       		 SET expiration_date = sysdate
    		 WHERE (citation_set_id, publication_date, heading_aui,
           		major_topic, root_source, coc_type) IN
    		   (SELECT citation_set_id, publication_date, heading_aui,
           		major_topic, root_source, coc_type
	            FROM t_mrd_coc_headings WHERE subheading_set_id IS NULL
     		    MINUS
     		    SELECT citation_set_id, publication_date, heading_aui,
           		   major_topic, root_source, coc_type
		     FROM t_coc_headings WHERE subheading_set_id IS NULL)
      		   AND expiration_date IS NULL
		   AND subheading_set_id IS NULL';

            -- Insert new coc_headings states
    	    location := '130.4';
    	    EXECUTE IMMEDIATE
    		'INSERT INTO mrd_coc_headings
       			(citation_set_id, publication_date, heading_aui,
        		subheading_set_id, major_topic, root_source, coc_type,
        		insertion_date, expiration_date)
    		 SELECT citation_set_id, publication_date, heading_aui,
           		subheading_set_id, major_topic, root_source, coc_type,
           		sysdate, null
    		 FROM t_coc_headings
    		 MINUS
    		 SELECT citation_set_id, publication_date, heading_aui,
           		subheading_set_id, major_topic, root_source, coc_type,
           		sysdate, null
    		 FROM t_mrd_coc_headings';

    	    location := '130.5';
    	    MEME_UTILITY.drop_it('table','t_mrd_coc_headings');
    	    MEME_UTILITY.drop_it('table','t_coc_headings');

    	    start_year := end_year;
    	    end_year := end_year + 5;

    	END LOOP;

    ELSIF LOWER(table_name) = 'coc_subheadings' THEN

        -- Expire old coc_subheadings states

        -- Expire where mrd does not match mid view
        location := '140.1';
        UPDATE mrd_coc_subheadings
        SET expiration_date = st_timestamp
        WHERE (subheading_set_id,citation_set_id,
               subheading_qa,subheading_major_topic) IN
            (SELECT subheading_set_id,citation_set_id,
                    subheading_qa,subheading_major_topic
             FROM mrd_coc_subheadings
             WHERE expiration_date IS NULL
             MINUS
             SELECT subheading_set_id,citation_set_id,
                    subheading_qa,subheading_major_topic
             FROM coc_subheadings)
          AND expiration_date IS NULL;

        -- Insert new MRD properties states
        location := '140.2';
        INSERT INTO mrd_coc_subheadings
            (subheading_set_id,citation_set_id,
             subheading_qa,subheading_major_topic,
             insertion_date, expiration_date)
        SELECT subheading_set_id, citation_set_id, subheading_qa,
               subheading_major_topic, st_timestamp, null
        FROM
        (SELECT subheading_set_id,citation_set_id,
                subheading_qa,subheading_major_topic
         FROM coc_subheadings
         MINUS
         SELECT subheading_set_id,citation_set_id,
                subheading_qa,subheading_major_topic
         FROM mrd_coc_subheadings
         WHERE expiration_date IS NULL);

    ELSIF LOWER(table_name) = 'aui_history' THEN
        -- Expire old aui_history states
        location := '150.1a';
        -- Expire where mrd does not match mid view
        UPDATE mrd_aui_history
        SET expiration_date = st_timestamp
        WHERE (aui1, cui1, ver, nvl(relationship_name,'null'),
	       NVL(relationship_attribute,'null'),
	       map_reason, NVL(aui2,'null'), NVL(cui2,'null')) IN
	    (SELECT aui1, cui1, ver, nvl(relationship_name,'null'),
	        NVL(relationship_attribute,'null'),
		map_reason, NVL(aui2,'null'), NVL(cui2,'null')
	     FROM mrd_aui_history
             WHERE expiration_date IS NULL
             MINUS
             SELECT aui1, cui1, ver, nvl(relationship_name,'null'),
		NVL(relationship_attribute,'null'),
		map_reason, NVL(aui2,'null'), NVL(cui2,'null')
	     FROM aui_history)
          AND expiration_date IS NULL;

        -- Insert new MRD aui_history states
        location := '150.2a';
        INSERT INTO mrd_aui_history
	    (aui1, cui1, ver, relationship_name,
	     relationship_attribute, map_reason, aui2, cui2,
	     insertion_date, expiration_date)
	SELECT aui1, cui1, ver, relationship_name,
	       relationship_attribute, map_reason, aui2, cui2,
	       st_timestamp, null
	FROM
	 (SELECT aui1, cui1, ver, relationship_name,
	         relationship_attribute, map_reason, aui2, cui2
	  FROM aui_history
          MINUS
          SELECT aui1, cui1, ver, relationship_name,
	 	 relationship_attribute, map_reason, aui2, cui2
	  FROM mrd_aui_history
	  WHERE expiration_date IS NULL);

    ELSIF LOWER(table_name) = 'cui_history' THEN
        -- Expire old cui_history states
        location := '150.1';
        -- Expire where mrd does not match mid view
        UPDATE mrd_cui_history
        SET expiration_date = st_timestamp
        WHERE (cui1, NVL(ver,'null'),
	       relationship_name,  NVL(relationship_attribute,'null'),
	       NVL(map_reason,'null'),NVL(cui2,'null')) IN
	    (SELECT cui1, NVL(ver,'null'),
		relationship_name,  NVL(relationship_attribute,'null'),
		NVL(map_reason,'null'),NVL(cui2,'null')
	     FROM mrd_cui_history
             WHERE expiration_date IS NULL
             MINUS
             SELECT cui1, NVL(ver,'null'),
		relationship_name,  NVL(relationship_attribute,'null'),
		NVL(map_reason,'null'),NVL(cui2,'null')
	     FROM cui_history)
          AND expiration_date IS NULL;

        -- Insert new MRD cui_history states
        location := '150.2';
        INSERT INTO mrd_cui_history
		(cui1, ver, relationship_name, relationship_attribute,
		  	map_reason, cui2, insertion_date, expiration_date)
	SELECT cui1, ver, relationship_name, relationship_attribute,
		  	map_reason, cui2, st_timestamp, null FROM
	(SELECT cui1, ver, relationship_name, relationship_attribute,
		  	map_reason, cui2
	 FROM cui_history
         MINUS
         SELECT cui1, ver, relationship_name, relationship_attribute,
		  	map_reason, cui2
	 FROM mrd_cui_history
	 WHERE expiration_date IS NULL);

    ELSIF LOWER(table_name) = 'content_views' THEN
        -- Expire old content_views states
        location := '160.1';
        -- Expire where mrd does not match mid view
        UPDATE mrd_content_views
        SET expiration_date = st_timestamp
        WHERE (content_view_id,
	       contributor, contributor_version, contributor_date,
	       maintainer, maintainer_version, maintainer_date,
	       content_view_name, content_view_description,
	       content_view_algorithm, content_view_category,
	       NVL(content_view_subcategory,'null'), content_view_class,
	       NVL(content_view_previous_meta,'null'),
	       NVL(content_view_contributor_url,'null'),
	       NVL(content_view_maintainer_url,'null'),
	       content_view_code, cascade, NVL(is_generated,'null')) IN
	    (SELECT content_view_id,
	       contributor, contributor_version, contributor_date,
	       maintainer, maintainer_version, maintainer_date,
	       content_view_name, content_view_description,
	       content_view_algorithm, content_view_category,
	       NVL(content_view_subcategory,'null'), content_view_class,
	       NVL(content_view_previous_meta,'null'),
	       NVL(content_view_contributor_url,'null'),
	       NVL(content_view_maintainer_url,'null'),
	       content_view_code, cascade, NVL(is_generated,'null')
	     FROM mrd_content_views
             WHERE expiration_date IS NULL
             MINUS
             SELECT content_view_id,
	       contributor, contributor_version, contributor_date,
	       maintainer, maintainer_version, maintainer_date,
	       content_view_name, content_view_description,
	       content_view_algorithm, content_view_category,
	       NVL(content_view_subcategory,'null'), content_view_class,
	       NVL(content_view_previous_meta,'null'),
	       NVL(content_view_contributor_url,'null'),
	       NVL(content_view_maintainer_url,'null'),
	       content_view_code, cascade, NVL(is_generated,'null')
	     FROM content_views)
         AND expiration_date IS NULL;

        -- Insert new MRD content_views states
        location := '160.2';
        INSERT INTO mrd_content_views
		(content_view_id,
	       contributor, contributor_version, contributor_date,
	       maintainer, maintainer_version, maintainer_date,
	       content_view_name, content_view_description,
	       content_view_algorithm, content_view_category,
	       content_view_subcategory, content_view_class,
	       content_view_previous_meta,content_view_contributor_url,
	       content_view_maintainer_url,
	       content_view_code, cascade, is_generated,
		insertion_date, expiration_date)
	SELECT content_view_id,
	       contributor, contributor_version, contributor_date,
	       maintainer, maintainer_version, maintainer_date,
	       content_view_name, content_view_description,
	       content_view_algorithm, content_view_category,
	       content_view_subcategory, content_view_class,
	       content_view_previous_meta,content_view_contributor_url,
	       content_view_maintainer_url,
	       content_view_code, cascade, is_generated,
		st_timestamp, null
        FROM
	(SELECT content_view_id,
	       contributor, contributor_version, contributor_date,
	       maintainer, maintainer_version, maintainer_date,
	       content_view_name, content_view_description,
	       content_view_algorithm, content_view_category,
	       content_view_subcategory, content_view_class,
	       content_view_previous_meta,content_view_contributor_url,
	       content_view_maintainer_url,
	       content_view_code, cascade, is_generated
	 FROM content_views
         MINUS
         SELECT content_view_id,
	       contributor, contributor_version, contributor_date,
	       maintainer, maintainer_version, maintainer_date,
	       content_view_name, content_view_description,
	       content_view_algorithm, content_view_category,
	       content_view_subcategory, content_view_class,
	       content_view_previous_meta,content_view_contributor_url,
	       content_view_maintainer_url,
	       content_view_code, cascade, is_generated
	 FROM mrd_content_views
	 WHERE expiration_date IS NULL);

    ELSIF LOWER(table_name) = 'content_view_members' THEN
        -- Expire old content_view_members states
        location := '170.1';
        -- Expire where mrd does not match mid view
        UPDATE mrd_content_view_members
        SET expiration_date = st_timestamp
        WHERE (meta_ui, code, cascade) IN
	    (SELECT meta_ui, code, cascade
	     FROM mrd_content_view_members
             WHERE expiration_date IS NULL
	             MINUS
             SELECT meta_ui, code, cascade
	     FROM content_view_members)
          AND expiration_date IS NULL;

        -- Insert new MRD content_view_members states
        location := '160.2';
        INSERT INTO mrd_content_view_members
		(meta_ui, code, cascade,
	         insertion_date, expiration_date)
	SELECT meta_ui, code, cascade, st_timestamp, null FROM
	(SELECT meta_ui, code, cascade
	 FROM content_view_members
         MINUS
         SELECT meta_ui, code, cascade
	 FROM mrd_content_view_members
	 WHERE expiration_date IS NULL);

    -- Otherwise some other table was updated, do nothing
    ELSE
	location := '1000';
    END IF;

EXCEPTION

    WHEN OTHERS THEN
	mrd_operations_error(method, location, 1, SQLERRM);
	RAISE mrd_operations_exception;

END generate_auxiliary_data_states;

-- PROCEDURE SELF_TEST ******************************************************/
PROCEDURE self_test
IS
BEGIN
   -- SERVEROUTPUT must be on to see this.

   DBMS_OUTPUT.PUT_LINE('Self_test');

END self_test;

-- PROCEDURE HELP ***********************************************************/
PROCEDURE help
IS
BEGIN
   help('');
END;

-- PROCEDURE HELP ***********************************************************/
PROCEDURE help( method_name IN VARCHAR2)
IS
BEGIN

   -- This procedure requires SET SERVEROUTPUT ON

   DBMS_OUTPUT.PUT_LINE('.');

   IF method_name IS NULL OR method_name = '' THEN
      DBMS_OUTPUT.PUT_LINE('. This package provides some operations for the MRD ');
      DBMS_OUTPUT.PUT_LINE('. Following is a list of these methods.  ');
      DBMS_OUTPUT.PUT_LINE('.');
      DBMS_OUTPUT.PUT_LINE('. is_concept_clean(concept_id): ');
      DBMS_OUTPUT.PUT_LINE('.    This method should be moved to MRDConnection');
      DBMS_OUTPUT.PUT_LINE('. update_clean_concepts(table_name):');
      DBMS_OUTPUT.PUT_LINE('.    The table should contain a concept_id field.');
      DBMS_OUTPUT.PUT_LINE('.    The method tests every concept_id in the table');
      DBMS_OUTPUT.PUT_LINE('.    and inserts it into or deletes it from the ');
      DBMS_OUTPUT.PUT_LINE('.    clean_concepts table.');
      DBMS_OUTPUT.PUT_LINE('. initialize_connected_sets(table_name):');
      DBMS_OUTPUT.PUT_LINE('.    Takes the concept_ids stored in the table and');
      DBMS_OUTPUT.PUT_LINE('.    puts them into the connected_sets table if they');
      DBMS_OUTPUT.PUT_LINE('.    are clean. Used for batch actions.');
      DBMS_OUTPUT.PUT_LINE('. calculate_connected_set:');
      DBMS_OUTPUT.PUT_LINE('.    Calculate the set of clean connected concepts');
      DBMS_OUTPUT.PUT_LINE('.    depending on the content of the connected_sets');
      DBMS_OUTPUT.PUT_LINE('.    table.');
      DBMS_OUTPUT.PUT_LINE('.    The set is stored in the connected_set table.');
      DBMS_OUTPUT.PUT_LINE('.    Returns the number of rows in connected_set.');
      DBMS_OUTPUT.PUT_LINE('. generate_core_data_states:       ');
      DBMS_OUTPUT.PUT_LINE('.    Updates the mrd states for the concepts of which');
      DBMS_OUTPUT.PUT_LINE('.    the concept_id is in the connected set table.');
      DBMS_OUTPUT.PUT_LINE('. generate_auxiliary_data_statesy(table_name):');
      DBMS_OUTPUT.PUT_LINE('.    Updates the corresponding MRD table for MID tables.');
      DBMS_OUTPUT.PUT_LINE('.    Returns MEME_CONSTANTS.YES if the table_name is ');
      DBMS_OUTPUT.PUT_LINE('.    processed above and MEME_CONSTANTS.NO otherwise.');
      DBMS_OUTPUT.PUT_LINE('. handle_norm_change_mid:');
      DBMS_OUTPUT.PUT_LINE('.    Processes changes to MID data structures that occur');
      DBMS_OUTPUT.PUT_LINE('.    when the LVG version changes.');
      DBMS_OUTPUT.PUT_LINE('. handle_norm_change_mrd:');
      DBMS_OUTPUT.PUT_LINE('.    Processes changes to MRD data structures that occur');
      DBMS_OUTPUT.PUT_LINE('.    when the LVG version changes.');
      DBMS_OUTPUT.PUT_LINE('.                          ');
   ELSE
      DBMS_OUTPUT.PUT_LINE('.There is no help for the topic: "' || method_name || '".');
   END IF;

   -- Print version
   DBMS_OUTPUT.PUT_LINE('.');
   version;

END help;

END MRD_OPERATIONS;
/
SHOW ERRORS

set serveroutput on size 100000
execute MRD_OPERATIONS.help;


