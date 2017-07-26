CREATE OR REPLACE PACKAGE MEME_OPERATIONS AS

/*****************************************************************************
 *
 * PL/SQL File: MEME_OPERATIONS.sql
 *
 * This package contains code for regular MID maintenance work
 *
 * Version Information
 *
 * 02/28/2006 3.12.3 BAC (1-754X9) : Bug fix for AUI fix
 * 02/03/2006 3.12.2 TTN (1-754X9) : Extend AUI to 8 chars. Pad AUI to fixed length
 * 10/12/2005 3.12.1: fix to bad_assignments logic to use only last_release_cui
 * 01/25/2005 3.12.0: assign_cuis now uses same algorithm as MEME server
 * 12/30/2004 3.11.0: Minor change to how assign_cuis uses work_id.
 * 12/13/2004 3.10.0: Released
 * 11/22/2004 3.9.2: Use data-driven prefix/length values
 *                   use release_rank and last_assigned_cui data
 * 11/18/2004 3.9.1: "local" CUI assignment allows for any prefix
 *                   to be passed in via new_cui_flag
 * 09/20/2004 3.9.0: Ranking algoritm put MTH/MM above everything else,
 *                   Released.
 * 03/08/2004 3.8.0: Better ranking algorithm
 * 12/14/2001 3.7.0: Released
 * 12/06/2001 3.6.1: When assigning "new" cuis, don't count the new cuis
 *                   as bad assignments because there are no matching
 *                   last_release_cui values.  Queries were cleaned up a bit
 *                   and also the length of the atom rank was extended to 28
 *                   from 18 to account for SUI.  Finally, the join to map
 *                   through mom_safe_replacement now uses only the "best"
 *                   fact for each new_atom_id.
 * 04/12/2001 3.6.0: Released version
 * 04/02/2001 3.5.1: In assigning split-merge cases the code was not
 *                   unassigning CUIs for concept_ids that lost their
 *                   assignments.  This was fixed.  QA procedures were
 *                   added to look for duplicate CUI or duplicate concept_id
 *                   assignments in cui_assignment.  This QA would've caught
 *                   the problem we fixed.  Also, the code to assign null
 *                   CUIs in concept_status,classes was not working correctly
 *                   It was comparing NVL(b.cui,'null') to a.cui instead of
 *                   to NVL(a.cui,'null').
 * 12/11/2000 3.5.0: Fixed assign_cuis 1-1 logic, released
 * 12/05/2000 3.4.1: Slightly faster updates for concept_status,classes
 *                   Fixed logic for pure merges, pure split
 * 11/10/2000 3.4.0: Released
 * 10/5/2000 3.3.3: new_cuis bug fix
 * 9/29/2000 3.3.2: new_cuis
 * 9/28/2000 3.3.1: update classes.last_ASSIGNED_cui!!!
 * 8/31/2000 3.3.0: Released
 * 8/30/2000 3.2.1: Optimized algorithm, allowed it to take
 *                  table_name parameter.
 * 8/01/2000 3.2.0: Package handover version
 * 6/12/2000 3.1.2: Added initialize_trace and assign_cuis logs elapsed time
 * 5/16/2000 3.1.0: First Version

 *
 * Status:
 *        Functionality:
 *        Testing:
 *         Enhancements:
 *              Temporary cui assignments should not be reassigned each time
 *              Keep the CT% last_assigned_cui assignments, don't always
 *            reassign.
 *
 *****************************************************************************/

    package_name                 VARCHAR2(25) := 'MEME_OPERATIONS';
    release_number               VARCHAR2(1) := '4';
    version_number               VARCHAR2(5) := '12.1';
    version_date                 DATE := '12-oct-2005';
    version_authority            VARCHAR2(3) := 'BAC';

    meme_operations_debug        BOOLEAN := FALSE;
    meme_operations_trace        BOOLEAN := FALSE;

    location                     VARCHAR2(10);
    err_msg                      VARCHAR2(256);
    method                       VARCHAR2(256);

    PROCEDURE initialize_trace (method IN VARCHAR2);

    meme_operations_exception EXCEPTION;

    --
    -- MEME PACKAGE API
    --
    FUNCTION release
    RETURN INTEGER;

    FUNCTION version
    RETURN FLOAT;

    FUNCTION version_info
    RETURN VARCHAR2;
    PRAGMA restrict_references (version_info,WNDS,RNDS,WNPS);

    PROCEDURE version;
    PROCEDURE register_package;

    PROCEDURE set_trace_on;
    PROCEDURE set_trace_off;
    PROCEDURE set_debug_on;
    PROCEDURE set_debug_off;
    PROCEDURE trace ( message IN VARCHAR2 );

    PROCEDURE local_exec (query in varchar2);
    FUNCTION local_exec (query IN VARCHAR2) RETURN INTEGER;

    PROCEDURE help;

    PROCEDURE help ( topic IN VARCHAR2 );

    PROCEDURE self_test;

    PROCEDURE meme_operations_error (
            method              IN VARCHAR2,
            location            IN VARCHAR2,
            error_code          IN INTEGER,
            detail              IN VARCHAR2
    );

    --
    -- MEME_OPERATIONS Procedure declarations
    --
    FUNCTION assign_cuis(
        authority           IN VARCHAR2,
        work_id             IN INTEGER := 0,
        table_name          IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
        new_cui_flag        IN VARCHAR2 := MEME_CONSTANTS.NO,
        all_flag            IN VARCHAR2 := MEME_CONSTANTS.NO,
        qa_flag             IN VARCHAR2 := MEME_CONSTANTS.YES
    ) RETURN INTEGER;

END meme_operations;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY meme_operations AS

--
--  MEME Package API
--

--************************* RELEASE **********************************
FUNCTION release
RETURN INTEGER
IS
BEGIN

    version;
    return to_number(release_number);

END release;

--************************* VERSION INFO **********************************
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
    return package_name || ' Release ' || release_number || ': ' ||
           'version ' || version_number || ' (' || version_date || ')';
END version_info;

--************************* VERSION **********************************
FUNCTION version
RETURN FLOAT
IS
BEGIN

    version;
    return to_number(version_number);
END version;

--************************* VERSION **********************************
PROCEDURE version
IS
BEGIN

    DBMS_OUTPUT.PUT_LINE('Package: ' || package_name);
    DBMS_OUTPUT.PUT_LINE('Release ' || release_number || ': ' ||
                         'version ' || version_number || ', ' ||
                         version_date || ' (' ||
                         version_authority || ')');

END version;

--************************* SET trace on **********************************
PROCEDURE set_trace_on
IS
BEGIN

    meme_operations_trace := TRUE;

END set_trace_on;

--************************* SET trace off **********************************
PROCEDURE set_trace_off
IS
BEGIN

    meme_operations_trace := FALSE;

END set_trace_off;

--************************* SET debug on **********************************
PROCEDURE set_debug_on
IS
BEGIN

    meme_operations_debug := TRUE;

END set_debug_on;

--************************* SET debug off **********************************
PROCEDURE set_debug_off
IS
BEGIN

    meme_operations_debug := FALSE;

END set_debug_off;

--************************* TRACE **********************************
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF meme_operations_trace = TRUE THEN

           MEME_UTILITY.PUT_MESSAGE(message);

    END IF;

END trace;

--************************* local_exec **********************************
PROCEDURE local_exec ( query IN VARCHAR2 )
IS
BEGIN

    IF meme_operations_trace = TRUE THEN
        MEME_UTILITY.put_message(query);
    END IF;

    IF meme_operations_debug = FALSE THEN
        MEME_UTILITY.exec(query);
    END IF;

END local_exec;

--************************* local_exec **********************************
FUNCTION local_exec ( query IN VARCHAR2 )
RETURN INTEGER
IS
BEGIN

    IF meme_operations_trace = TRUE THEN
        MEME_UTILITY.put_message(query);
    END IF;

    IF meme_operations_debug = FALSE THEN
        return MEME_UTILITY.exec(query);
    END IF;

    RETURN 0;

END local_exec;

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
   DBMS_OUTPUT.PUT_LINE('.');

   IF topic IS NULL OR topic = '' THEN
      DBMS_OUTPUT.PUT_LINE('.This package provides standard maintenance utilities.');
      DBMS_OUTPUT.PUT_LINE('.');
      DBMS_OUTPUT.PUT_LINE('. assign_cuis:                Assign concept_status.cui');
   ELSE
      DBMS_OUTPUT.PUT_LINE('.There is no help for the topic: "' || topic || '".');
   END IF;

    -- Print version
    MEME_UTILITY.PUT_MESSAGE('.');
    version;
END help;

--*********************** register_package ********************************
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_OPERATIONS.release_number,
      MEME_OPERATIONS.version_number,
      SYSDATE,
      MEME_OPERATIONS.version_authority,
      MEME_OPERATIONS.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

--************************* SELF_TEST ***************************
PROCEDURE self_test
IS
BEGIN

    DBMS_OUTPUT.ENABLE(100000);
    -- This procedure requires SET SERVEROUTPUT ON

END self_test;

--************************* MEME_OPERATIONS_ERROR ***************************
PROCEDURE meme_operations_error (
            method                    IN VARCHAR2,
            location            IN VARCHAR2,
            error_code            IN INTEGER,
            detail                    IN VARCHAR2
)
IS
    error_msg            VARCHAR2(100);
BEGIN
    IF error_code = 1 THEN
        error_msg := 'MO0001: Unspecified error';
    ELSIF error_code = 30 THEN
        error_msg := 'MO0030: Bad count';
    ELSE
        error_msg := 'MO0000: Unknown Error';
    END IF;

    MEME_UTILITY.PUT_ERROR('Error in MEME_OPERATIONS::'||method||' at '||
        location||' ('||error_msg||','||detail||')');

END meme_operations_error;

--************************* INITIALIZE_TRACE ***************************
PROCEDURE initialize_trace (method IN VARCHAR2)
IS
BEGIN

    location := '0';
    err_msg := '';
    meme_operations.method := method;

END initialize_trace;

--
-- MEME_OPERATIONS Procedures
--

--************************* ASSIGN_CUIS ***************************
--
-- Assumption:
--  mom safe replacement facts only count if atoms are in same concept
--
-- For the whole database
-- 1. calculate suspect_concepts (cui splits/merges)
--    assume that safe-replacement facts split across concepts
--    do not constitute cui -splits
-- 2. calculate cuis in suspect_concepts (get safe_replacement cuis too)
-- 3. get atoms with those cuis (last_release_cui or mom_safe_replacement)
--
-- For a table_name subset
-- 1. Pass in table of suspect concepts.
--    This set has the property that it is "connected".
--    i.e. there are no concepts outside the set that would normally
--    affect the cui assignments of concepts in the set.
--
-- For both
-- 3. get suspect atoms (with atom_id,lrc,concept_id,termgroup)
-- 4. calculate cui ranks of specified atoms.
-- 5. assign merges
-- 6. assign splits
-- 7. assign split-merge
-- 8. update concept_status, classes
-- 9. QA
--
--
-- new_cui_flag: "prefix",YES,NO
--        used to assign temporary,real cuis to concepts that would otherwise
--        have none.  If real cuis are assigned the CUI row in max tab is
--      updated and so this must be rewound if the operation needs to be
--      undone.
--
-- all_flag: YES,NO
--        assign all cuis including single last_release_cui/last_assigned_cui
--        and null cases (only if new_cui_flag is NO)
--
-- qa_flag:  YES, NO
--      Used to flag whether or not assignments should be QA'd
--
-- If all_flag = YES and new_cui_flag != NO, new cuis are assigned for all
-- concepts where a null cui would be assigned.
--
FUNCTION assign_cuis (
        authority           IN VARCHAR2,
        work_id             IN INTEGER := 0,
        table_name          IN VARCHAR2 := MEME_CONSTANTS.EMPTY_TABLE,
        new_cui_flag        IN VARCHAR2 := MEME_CONSTANTS.NO,
        all_flag            IN VARCHAR2 := MEME_CONSTANTS.NO,
        qa_flag             IN VARCHAR2 := MEME_CONSTANTS.YES
) RETURN INTEGER
IS
    TYPE curvar_type IS REF CURSOR;
    curvar                      curvar_type;

    -- table names
    uniq                        VARCHAR2(50);
    suspect_atoms               VARCHAR2(50);
    suspect_concepts            VARCHAR2(50);
    suspect_cuis                VARCHAR2(50);
    cui_rank                    VARCHAR2(50);
    work_table                  VARCHAR2(50);
    cui_assignment              VARCHAR2(50);
    new_cui                     VARCHAR2(50);
    curr_concept                VARCHAR2(256);
    curr_cui                    VARCHAR2(256);

    -- qa table names
    bad_assignments             VARCHAR2(50);
    correct_assignments         VARCHAR2(50);
    naked_concepts              VARCHAR2(50);
    unassigned_cuis             VARCHAR2(50);
    possible_cuis               VARCHAR2(50);
    missed_assignments          VARCHAR2(50);

    query                       VARCHAR2(1024);
    ct                          INTEGER;
    new_cui_ct                  INTEGER := 0;
    l_cid                       INTEGER;
    l_cui                       VARCHAR2(20);
    rowcount                    INTEGER;
    concept_assigned            INTEGER;
    cui_assigned                VARCHAR2(10);

    cui_prefix                  VARCHAR2(10);
    cui_length                  INTEGER;

BEGIN

    MEME_UTILITY.timing_start;

    --
    -- Get unique value for table name
    --
    IF table_name = MEME_CONSTANTS.EMPTY_TABLE THEN
        uniq := MEME_UTILITY.get_unique_tablename;
    ELSE
        uniq := table_name;
    END IF;

    --
    -- Assign temp table names
    --
    suspect_concepts := uniq || '_sc';
    suspect_atoms := uniq || '_sa';
    suspect_cuis := uniq || '_scui';
    cui_rank := uniq || '_cr';
    work_table := uniq || '_work';
    cui_assignment := uniq || '_cassign';
    new_cui := uniq || '_new';

    --
    -- Assign temp QA table names
    --
    bad_assignments := uniq || '_ba';
    correct_assignments := uniq || '_ca';
    naked_concepts := uniq || '_nc';
    unassigned_cuis := uniq || '_unassign';
    possible_cuis := uniq || '_pcui';
    missed_assignments := uniq || '_miss';

    --
    -- Get concept list to compute CUIs for
    --
    -- If no table of ids supplied or list has > 5K concepts, perform
    -- database-wide search
    --
    IF table_name = MEME_CONSTANTS.EMPTY_TABLE OR
            MEME_UTILITY.exec_count(table_name) > 5000 THEN

            err_msg := 'Error creating suspect_concepts';
                location := '130';
            MEME_UTILITY.put_message(LPAD('CREATE suspect_concepts table',45,'.'));
                location := '130b';
            MEME_UTILITY.drop_it('table', suspect_concepts);
            local_exec(
                'CREATE table ' || suspect_concepts || ' as
                 -- Get MERGED cuis
                 SELECT concept_id
                 FROM
                 (SELECT last_release_cui cui, concept_id
                  FROM classes a
                  WHERE last_release_cui IS NOT NULL
                  UNION
                  SELECT last_assigned_cui cui, concept_id
                  FROM classes a
                  WHERE last_assigned_cui IS NOT NULL
                    AND last_assigned_cui != last_release_cui)
                 GROUP BY concept_id HAVING count(distinct cui)>1
                 -- Get SPLIT cuis
                 UNION
                 SELECT DISTINCT a.concept_id
                 FROM
                   (SELECT last_release_cui cui, concept_id
                    FROM classes a
                    WHERE last_release_cui IS NOT NULL
                    UNION
                    SELECT last_assigned_cui cui, concept_id
                    FROM classes a
                    WHERE last_assigned_cui IS NOT NULL
                      AND last_assigned_cui != last_release_cui) a,
                        (SELECT last_release_cui cui, concept_id
                         FROM classes a
                         WHERE last_release_cui IS NOT NULL
                         UNION
                         SELECT last_assigned_cui cui, concept_id
                         FROM classes a
                         WHERE last_assigned_cui IS NOT NULL
                           AND last_assigned_cui != last_release_cui) b
                  WHERE a.cui = b.cui
                    AND a.concept_id != b.concept_id
        ');

    ELSE

        suspect_concepts := table_name;

    END IF;

    --
    -- analyze the table for performance reasons
    --
    err_msg := 'error analyzing suspect_cuis';
    location := '135';
    MEME_UTILITY.put_message(LPAD('ANALYZE suspect_concepts table',45,'.'));
    MEME_SYSTEM.analyze(suspect_concepts);

    --
    -- Get suspect atoms from classes
    --
    MEME_UTILITY.put_message(LPAD('Create suspect_atoms table',45,'.'));
    err_msg := 'Error creating suspect_atoms';
    location := '140';
    MEME_UTILITY.drop_it('table', suspect_atoms);
    location := '140b';
    local_exec (
        'CREATE TABLE ' || suspect_atoms || ' AS
         SELECT a.atom_id, last_release_cui, last_assigned_cui,
                a.concept_id, a.termgroup, last_release_rank,
                a.sui, a.aui, a.tobereleased
         FROM classes a, ' || suspect_concepts || ' b
         WHERE a.concept_id = b.concept_id
           AND (a.last_release_cui IS NOT NULL OR a.last_assigned_cui IS NOT NULL)
        ');

    --
    -- Rank all atoms
    --
    --         '0' +                         /* tobereleased_rank */
    --         '0000' +                /* Termgroup release rank */
    --         '0' +                        /* Released last year */
    --         '0000000000' +                /* sui */
    --         '0000000000' +                /* aui */
    --         '0' +                         /* "last release" or "last assigned" */
    --
    -- Some atoms may have one CUI as an LRC and one CUI as a LAC
    -- We try to assign the LRC first, and the LAC second, hence the
    -- last portion of the rank
    MEME_UTILITY.put_message(LPAD('Create cui_rank table',45,'.'));
    err_msg := 'Error creating cui_rank';
    location := '160';
    MEME_UTILITY.drop_it('table', cui_rank);
    location := '160b';
    local_exec(
        'CREATE table ' || cui_rank || ' as
             SELECT concept_id, cui, max(rank) as rank
         FROM
         (SELECT /*+ parallel(a) */ a.concept_id, a.last_release_cui cui,
                b.rank || LPAD(c.release_rank,4,0) ||
                 a.last_release_rank || a.sui ||
                 LPAD(
                  SUBSTR(a.aui,
                         INSTR(a.aui,
                               (SELECT value FROM code_map 
                                WHERE code = ''AUI'' AND type = ''ui_prefix''))+1),                  
                  (SELECT value FROM code_map 
                   WHERE code = ''AUI'' AND type = ''ui_length''),''0'') 
                 || ''1'' as rank
             FROM ' || suspect_atoms || ' a, tobereleased_rank b,
                termgroup_rank c
         WHERE a.tobereleased = b.tobereleased
           AND a.termgroup = c.termgroup
           AND a.last_release_cui IS NOT NULL
          UNION ALL
           SELECT /*+ parallel(a) */ a.concept_id, a.last_assigned_cui,
                b.rank || LPAD(c.release_rank,4,0) ||
                 a.last_release_rank || a.sui ||
                 LPAD(
                  SUBSTR(a.aui,
                   INSTR(a.aui,
                         (SELECT value FROM code_map 
                          WHERE code = ''AUI'' AND type = ''ui_prefix''))+1),
                  (SELECT value FROM code_map 
                   WHERE code = ''AUI'' AND type = ''ui_length''),''0'')
                 || ''0'' as rank
              FROM ' || suspect_atoms || ' a, tobereleased_rank b,
                termgroup_rank c
          WHERE a.tobereleased = b.tobereleased
            AND a.termgroup = c.termgroup
           AND a.last_assigned_cui IS NOT NULL
           AND a.last_assigned_cui != a.last_release_cui)
         GROUP BY concept_id, cui
        ');

    --
    -- Save initial data if we are performing QA
    --
    IF qa_flag = MEME_CONSTANTS.YES THEN
        MEME_UTILITY.put_message(LPAD('Create work_table',45,'.'));
        err_msg := 'Error creating work_table';
        location := '300';
        MEME_UTILITY.drop_it('table', work_table);
        location := '300b';
        local_exec(
            'CREATE table ' ||work_table||' as
             SELECT *
             FROM ' || cui_rank
        );
    ELSE
        work_table := cui_rank;
    END IF;

    --
    -- Create table to track current assignments
    --
    err_msg := 'Error creating cui_assignment';
    location := '310';
    MEME_UTILITY.drop_it('table', cui_assignment);
    location := '310b';
    local_exec(
        'CREATE table ' || cui_assignment || ' as
             SELECT cui, concept_id
         FROM ' || work_table || '
          WHERE 0 = 1
        ');

    --
    -- Load dummy entry into work table (to finish process)
    --
    location := '315';
    local_exec(
        'INSERT INTO ' || work_table || ' (concept_id, cui, rank) VALUES
         (0,null,''00000'')');

    --
    -- Index work table
    --
    err_msg := 'Error indexing work table';
    location := '326';
    EXECUTE IMMEDIATE
      'CREATE INDEX x_work_table1 on ' || work_table || ' (concept_id)
        COMPUTE STATISTICS PARALLEL';

    location := '327';
    EXECUTE IMMEDIATE
      'CREATE INDEX x_work_table2 on ' || work_table || ' (cui)
        COMPUTE STATISTICS PARALLEL';

    --
    -- Assign CUIs in atom-rank order
    --
    location := '328';
    MEME_UTILITY.put_message(LPAD('Start cui assignment loop',45,'.'));
    ct  := 0;
    LOOP

       location := '330';
       err_msg := 'Error looking up concept_id,cui';
       EXECUTE IMMEDIATE
        'SELECT concept_id, cui FROM
          (SELECT concept_id,cui FROM ' || work_table || ' ORDER BY rank DESC)
         WHERE rownum < 2'
       INTO concept_assigned, cui_assigned;

       EXIT WHEN concept_assigned = 0;

       location := '340';
       err_msg := 'Error deleting concept_ids from work_table';
       EXECUTE IMMEDIATE
        'DELETE FROM ' || work_table || ' WHERE concept_id = :x'
       USING concept_assigned;

       location := '350';
       err_msg := 'Error deleting cuis from work_table';
       EXECUTE IMMEDIATE
        'DELETE FROM ' || work_table || ' WHERE cui = :cui'
       USING cui_assigned;

       location := '360';
       err_msg := 'Error inserting cui and concept into cui_assignment';
       EXECUTE IMMEDIATE
        'INSERT INTO ' || cui_assignment || ' (concept_id, cui) VALUES (:concept, :cui)'
       USING concept_assigned, cui_assigned;

       ct := ct + 1;
       IF MOD(ct,1000) = 0 THEN
             COMMIT;
           MEME_UTILITY.put_message(LPAD(ct || ' rows processed',45,'.'));
       END IF;

    END LOOP;

    IF MOD(ct,1000) != 0 THEN
       MEME_UTILITY.put_message(LPAD(ct || ' rows processed',45,'.'));
    END IF;

    --
    -- Assign all 1-1 cases (if we are assigning "all" CUIs)
    --
    IF all_flag = MEME_CONSTANTS.YES THEN
        location := '378.2';
        MEME_UTILITY.put_message(LPAD('Assign single last_release_cui cases',45,'.'));
        ct := local_exec (
            'INSERT INTO ' || cui_assignment || '
             SELECT a.cui, concept_id
             FROM
             (SELECT min(cui) cui, concept_id FROM
               (SELECT last_release_cui cui, concept_id FROM classes
                 WHERE last_release_cui IS NOT NULL
                UNION
                SELECT last_assigned_cui cui, concept_id FROM classes
                 WHERE last_assigned_cui IS NOT NULL
                  AND last_assigned_cui != last_release_cui)
              GROUP BY concept_id HAVING count(DISTINCT cui) = 1) a,
             (SELECT cui FROM
               (SELECT last_release_cui cui, concept_id FROM classes
                 WHERE last_release_cui IS NOT NULL
                UNION
                SELECT last_assigned_cui cui, concept_id FROM classes
                 WHERE last_assigned_cui IS NOT NULL
                  AND last_assigned_cui != last_release_cui)
              GROUP BY cui HAVING count(DISTINCT concept_id) = 1) b
             WHERE a.cui = b.cui'
        );
        MEME_UTILITY.put_message(LPAD(ct || ' rows processed',45,'.'));

	    --
	    -- Find cases where concept is going to get a "NEW" cui
	    -- but has a last_assigned_cui that has not yet been used.
	    -- In these cases, reuse the last_assigned_cui value
	    -- instead of choosing a new CUI.  This should allow
	    -- an immediate re-run of the CUI assignment algorithm to
	    -- effectively make no changes.
	    --
        ct := local_exec (
            'INSERT INTO ' || cui_assignment || '
             SELECT DISTINCT a.cui, concept_id
             FROM
             (SELECT min(concept_id) concept_id, last_assigned_cui cui
              FROM classes
              WHERE last_assigned_cui != last_release_cui
              	AND last_assigned_cui IS NOT NULL
              	AND last_release_cui IS NOT NULL
              GROUP BY last_assigned_cui HAVING count(distinct concept_id) = 1) a
             WHERE concept_id NOT IN
             	(SELECT concept_id FROM ' || cui_assignment || ')
               AND cui NOT IN
               	(SELECT cui FROM ' || cui_assignment || ')'
        );
        MEME_UTILITY.put_message(LPAD(ct || ' (assigned cui) rows processed',45,'.'));
    END IF;

    --
    -- If we are not assigning new CUIs, then load
    -- null assignments for concepts not assigned to CUIs
    --
    IF new_cui_flag = MEME_CONSTANTS.NO THEN
            MEME_UTILITY.put_message(LPAD('Assign null CUIs (1)',45,'.'));
            ct := local_exec (
            'INSERT INTO ' || cui_assignment || '
             SELECT null, concept_id FROM
              (SELECT concept_id FROM ' || cui_rank || '
               MINUS SELECT concept_id FROM ' || cui_assignment || ')
          ');
        MEME_UTILITY.put_message(LPAD(ct || ' rows processed',45,'.'));
    END IF;

    --
    -- If we are not assigning new CUIs, and we are assigning CUIs for all concepts
    -- load null assignments for all concepts not given a CUI assignment
    --
    IF all_flag = MEME_CONSTANTS.YES AND
       new_cui_flag = MEME_CONSTANTS.NO  THEN

            location := '378.1';
        MEME_UTILITY.put_message(LPAD('Assign null CUIs',45,'.'));
        ct := local_exec (
            'INSERT INTO ' || cui_assignment || '
             SELECT null, concept_id FROM
              (SELECT concept_id FROM concept_status
               MINUS
               SELECT concept_id FROM ' || cui_assignment || ')');
        MEME_UTILITY.put_message(LPAD(ct || ' rows processed',45,'.'));

    END IF;

    --
    -- Assign new CUIs
    --
    IF new_cui_flag != MEME_CONSTANTS.NO THEN

        MEME_UTILITY.put_message(LPAD('Assign new cuis (' ||
                new_cui_flag || ')',45,'.'));

            location := '378.3';
        MEME_UTILITY.drop_it('table', new_cui);

         --
        -- Get candidate concepts
        --
        IF all_flag = MEME_CONSTANTS.YES THEN
            location := '378.3b';
            local_exec(
              'CREATE TABLE ' || new_cui || ' AS
               SELECT concept_id FROM concept_status
               MINUS SELECT concept_id FROM ' || cui_assignment
           );
         ELSE
                   location := '378.3c';
            local_exec(
              'CREATE TABLE ' || new_cui || ' AS
               SELECT concept_id FROM ' || suspect_concepts || '
               MINUS SELECT concept_id FROM ' || cui_assignment
            );
          END IF;

        --
        -- count how many there are
        --
            location := '378.4';
        ct := MEME_UTILITY.exec_select(
            'SELECT count(*) FROM ' || new_cui || '
             WHERE concept_id IN
              (SELECT concept_id FROM classes
               WHERE tobereleased in (''Y'',''y''))
        ');
        MEME_UTILITY.put_message(LPAD(ct|| ' new cuis.',45,'.'));

        --
          -- This is the count of new CUIs.
        --
          new_cui_ct := ct;

        --
        -- Get max_tab.table_name lookup key
        --
        IF new_cui_flag = MEME_CONSTANTS.YES THEN
            l_cui := 'CUI';
           ELSIF new_cui_flag != MEME_CONSTANTS.NO THEN
            l_cui := 'TCUI';
        END IF;

        --
        -- update max_tab
        --
            location := '378.5';
        UPDATE max_tab SET max_id = max_id + ct
        WHERE table_name = l_cui;

        --
        -- If TCUI row does not exist, add it
        --
        IF SQL%ROWCOUNT = 0 THEN
            location := '378.6';
            INSERT INTO max_tab (table_name, max_id)
            VALUES (l_cui, ct+1);
        END IF;

        --
        -- Get ID to start with
        --
            location := '378.7';
        SELECT max_id - ct into ct FROM max_Tab
        WHERE table_name = l_cui;

        --
        -- Add to cui_assignment table
        --
        IF new_cui_flag != MEME_CONSTANTS.YES AND
           new_cui_flag != MEME_CONSTANTS.NO THEN
              location := '378.81';
          SELECT to_number(value) INTO cui_length
          FROM code_map
           WHERE code = 'CUI' and type = 'ui_length';

              location := '378.82';
          local_exec(
            'INSERT INTO ' || cui_assignment || '
             SELECT ''' || new_cui_flag || ''' ||
                LPAD(rownum+' || ct || ',' || cui_length || ',0), concept_id
             FROM ' || new_cui || '
             WHERE concept_id IN
              (SELECT concept_id FROM classes
               WHERE tobereleased in (''Y'',''y''))
          ');
        ELSIF new_cui_flag = MEME_CONSTANTS.YES THEN
              location := '378.91';
          SELECT value INTO cui_prefix
          FROM code_map
           WHERE code = 'CUI' and type = 'ui_prefix';

              location := '378.92';
          SELECT value INTO cui_length
          FROM code_map
           WHERE code = 'CUI' and type = 'ui_length';

              location := '378.93';
          local_exec(
            'INSERT INTO ' || cui_assignment || '
             SELECT ''' || cui_prefix || ''' ||
                LPAD(rownum+' || ct || ',' || cui_length || ',0), concept_id
             FROM ' || new_cui || '
             WHERE concept_id IN
              (SELECT concept_id FROM classes
               WHERE tobereleased in (''Y'',''y''))
          ');
        END IF;

    END IF;

    --
    -- Analyze table
    --
    location := '379';
    err_msg := 'Error analyzing cui_assignment';
    MEME_SYSTEM.analyze(cui_assignment);

    --
    -- Assign concept_status.cui
    --
    location := '380';
    MEME_UTILITY.put_message(LPAD('Update concept_status.cui',45,'.'));
    err_msg := 'Error updating concept_status';
    ct := 0;
    location := '380.2';
    OPEN curvar FOR
        'SELECT a.concept_id, a.cui
         FROM ' || cui_assignment || ' a, concept_status b
         WHERE a.concept_id = b.concept_id
           AND nvl(b.cui,''null'') != nvl(a.cui,''null'') ';
    LOOP
        location := '380.2';
        FETCH curvar INTO l_cid, l_cui;
        EXIT WHEN curvar%NOTFOUND;

        location := '380.3';
        UPDATE concept_status SET cui = l_cui
        WHERE concept_id = l_cid
        AND NVL(cui,'null') != NVL(l_cui,'null');

        ct := ct + SQL%ROWCOUNT;

    END LOOP;
    MEME_UTILITY.put_message(LPAD(ct || ' rows assigned',45,'.'));

    --
    -- Assign classes.last_assigned_cui
    --
    location := '390.1';
    MEME_UTILITY.put_message(LPAD('Update classes.last_assigned_cui',45,'.'));
    err_msg := 'Error updating classes';
    ct := 0;
    location := '390.2';
    OPEN curvar FOR
        'SELECT a.concept_id, a.cui
         FROM ' || cui_assignment || ' a, classes b
         WHERE a.concept_id = b.concept_id
           AND nvl(last_assigned_cui,''null'') != nvl(a.cui,''null'') ';
    LOOP
        location := '390.2';
        FETCH curvar INTO l_cid, l_cui;
        EXIT WHEN curvar%NOTFOUND;

        location := '390.3';
        UPDATE classes SET last_assigned_cui = l_cui
        WHERE concept_id = l_cid
           AND nvl(last_assigned_cui,'null') != NVL(l_cui,'null');

        ct := ct + SQL%ROWCOUNT;

    END LOOP;
    MEME_UTILITY.put_message(LPAD(ct || ' rows assigned',45,'.'));


    --
    -- ONLY perform QA if flag is set
    --
    IF qa_flag = MEME_CONSTANTS.YES THEN

            --
            --  QA section
            --
            -- These steps used to be executed by $META_PROD/QA/
            -- QA.cui_assignment.sql
            MEME_UTILITY.put_message(LPAD('Finished, perform QA',45,'.'));

        -- Check that there is at least one atom in each concept with
            -- a last_release_cui equal to the CUI assigned by the algorithm
            -- since we only use safe replacement facts still in the same
            -- concept, all we have to do is look at the last release cuis
            -- in the concepts because the safe-replacement inherited ones
            -- came from the same concept.
            location := '400';
            err_msg := 'Error creating bad_assignments';
            MEME_UTILITY.drop_it('table', bad_assignments);
        local_exec(
                'CREATE table ' || bad_assignments || ' as
                 SELECT DISTINCT a.concept_id,
                            b.last_release_cui last_cui
                 FROM ' || cui_assignment || ' a, classes b
                 WHERE a.concept_id = b.concept_id
                   AND b.last_release_cui IS NOT NULL
                   AND a.cui IS NOT NULL
            ');

            location := '410';
            err_msg := 'Error creating correct_assignments';
            MEME_UTILITY.drop_it('table', correct_assignments);
        local_exec(
            'CREATE table ' || correct_assignments || ' as
                 SELECT distinct a.concept_id
                 FROM ' || bad_assignments || ' a, ' || cui_assignment || ' b
                 WHERE a.concept_id = b.concept_id
                   AND a.last_cui = b.cui
        ');

            COMMIT;

            /***************** This should be zero. ****************/
        /***************** Or equal to new cui count ***********/
            location := '430';
            err_msg := 'Error getting rowcount of bad_assignments';
            rowcount := MEME_UTILITY.exec_select(
           'SELECT count(*) FROM
                (SELECT concept_id FROM ' || bad_assignments || '
                 MINUS
                 (SELECT concept_id FROM ' || correct_assignments || '
                  UNION
                  SELECT concept_id FROM ' || cui_assignment || '
                  WHERE cui > (SELECT max(last_release_cui)
                               FROM classes) ) )
        ');
            IF rowcount != 0 THEN
              err_msg := 'This rowcount should be 0 or ' || new_cui_ct ||
                     ' (' || rowcount || ')';
              RAISE meme_operations_exception;
            END IF;

            -- Check to see that no concept without a CUI contains an
            -- atom with an unassigned last_release_cui

            location := '440';
            err_msg := 'Error creating naked_concepts';
            MEME_UTILITY.drop_it('table', naked_concepts);
          location := '440.1';
        local_exec(
                'CREATE table ' || naked_concepts || ' as
                 SELECT distinct concept_id
                 FROM ' || cui_rank || '
                 MINUS
             SELECT concept_id FROM ' || cui_assignment
        );

            location := '460';
            err_msg := 'Error creating unassigned_cuis';
            MEME_UTILITY.drop_it('table', unassigned_cuis);
            location := '460.1';
        local_exec(
            'CREATE table ' || unassigned_cuis || ' as
                 SELECT distinct cui
                 FROM ' || cui_rank || '
             MINUS
             SELECT cui FROM ' || cui_assignment
        );

            location := '480';
            err_msg := 'Error creating possible_cuis';
            MEME_UTILITY.drop_it('table', possible_cuis);
            location := '480.1';
        local_exec(
                'CREATE table ' ||possible_cuis|| ' as
                 SELECT a.concept_id,
                        nvl(b.last_release_cui,b.last_assigned_cui) last_cui
                 FROM ' || naked_concepts || ' a, classes b
                 WHERE a.concept_id = b.concept_id
            ');

            location := '490';
            err_msg := 'Error creating missed_assignments';
            MEME_UTILITY.drop_it('table', missed_assignments);
            location := '490.1';
        local_exec(
                'CREATE table ' || missed_assignments || ' as
                 SELECT concept_id, last_cui
                 FROM ' || possible_cuis || ' , ' || unassigned_cuis || '
                WHERE last_cui = cui
        ');

             /***************** This should be zero. ****************/

            location := '495';
            err_msg := 'Error getting rowcount of missed_assignments';
            rowcount := MEME_UTILITY.exec_count(missed_assignments);
            IF rowcount != 0 THEN
               err_msg := 'This rowcount should be 0 (' || rowcount || ') (cids: ' ||
           MEME_UTILITY.table_to_string(missed_assignments) || ').';
                  meme_operations_error('assign_cuis', location, 20,
                  rowcount||', '|| err_msg || ': ' ||SQLERRM);
                  RAISE meme_operations_exception;
            END IF;

             /***************** This should be zero. ****************/

            location := '497';
            err_msg := 'Error finding duplicate CUI assignments.';
            rowcount := MEME_UTILITY.exec_select(
            'SELECT count(*) FROM
              (SELECT cui FROM ' || cui_assignment || '
               WHERE cui IS NOT NULL
               GROUP BY cui
               HAVING count(*)>1)
            ');
            IF rowcount != 0 THEN
               err_msg := 'This rowcount should be 0 (' || rowcount || ')';
                  meme_operations_error('assign_cuis', location, 20,
                  rowcount||', '|| err_msg || ': ' ||SQLERRM);
                  RAISE meme_operations_exception;
            END IF;

             /***************** This should be zero. ****************/

            location := '498';
            err_msg := 'Error finding duplicate conceptid assignments.';
            rowcount := MEME_UTILITY.exec_select(
            'SELECT count(*) FROM
              (SELECT concept_id FROM ' || cui_assignment || '
               WHERE cui IS NOT NULL
               GROUP BY concept_id
               HAVING count(*)>1)
            ');
            IF rowcount != 0 THEN
               err_msg := 'This rowcount should be 0 (' || rowcount || ')';
                  meme_operations_error('assign_cuis', location, 20,
                  rowcount||', '|| err_msg || ': ' ||SQLERRM);
                  RAISE meme_operations_exception;
            END IF;


    END IF; -- only do qa if qa_flag is MEME_CONSTANTS.YES

    location := '500';
    err_msg := 'Dropping temporary tables.';

    --
    -- only drop suspect_concepts if table_name is EMPTY_TABLE
    --
    IF table_name = MEME_CONSTANTS.EMPTY_TABLE THEN
        MEME_UTILITY.drop_it('table', suspect_concepts );
    END IF;

    MEME_UTILITY.drop_it('table', suspect_atoms );
    MEME_UTILITY.drop_it('table', suspect_cuis);
    MEME_UTILITY.drop_it('table', cui_rank);
    MEME_UTILITY.drop_it('table', work_table );
    MEME_UTILITY.drop_it('table', work_table || '_backup' );
    MEME_UTILITY.drop_it('table', cui_assignment );
    MEME_UTILITY.drop_it('table', new_cui);
    MEME_UTILITY.drop_it('table', bad_assignments);
    MEME_UTILITY.drop_it('table', correct_assignments);
    MEME_UTILITY.drop_it('table', naked_concepts);
    MEME_UTILITY.drop_it('table', unassigned_cuis);
    MEME_UTILITY.drop_it('table', possible_cuis);
    MEME_UTILITY.drop_it('table', missed_assignments );

    MEME_UTILITY.timing_stop;
    MEME_UTILITY.put_message(LPAD('Finished, log results',45,'.'));
    MEME_UTILITY.log_operation(
        authority => authority,
        activity => 'Assign concept identifiers (CUI)',
        detail => 'Done assigning CUIs (new=' ||
          new_cui_flag || ', all=' || all_flag || ')',
        transaction_id => 0,
        work_id => work_id,
        elapsed_time => MEME_UTILITY.elapsed_time);
    RETURN work_id;

EXCEPTION
  WHEN OTHERS THEN
    meme_operations_error('assign_cuis',location,1,err_msg||' '||SQLERRM);

    IF table_name = MEME_CONSTANTS.EMPTY_TABLE THEN
        MEME_UTILITY.drop_it('table', suspect_concepts );
    END IF;
    MEME_UTILITY.drop_it('table', suspect_atoms );
    MEME_UTILITY.drop_it('table', suspect_cuis);
    MEME_UTILITY.drop_it('table', cui_rank);
    MEME_UTILITY.drop_it('table', work_table );
    MEME_UTILITY.drop_it('table', work_table || '_backup');
    MEME_UTILITY.drop_it('table', cui_assignment );
    MEME_UTILITY.drop_it('table', new_cui);
    MEME_UTILITY.drop_it('table', bad_assignments);
    MEME_UTILITY.drop_it('table', correct_assignments);
    MEME_UTILITY.drop_it('table', naked_concepts);
    MEME_UTILITY.drop_it('table', unassigned_cuis);
    MEME_UTILITY.drop_it('table', possible_cuis);
    MEME_UTILITY.drop_it('table', missed_assignments );

    RAISE meme_operations_exception;

END assign_cuis;


END MEME_OPERATIONS;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_OPERATIONS.help;
execute MEME_OPERATIONS.register_package
