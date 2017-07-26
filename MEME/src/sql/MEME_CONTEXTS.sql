CREATE OR REPLACE PACKAGE MEME_CONTEXTS AS

/*****************************************************************************
 *
 * PL/SQL File: MEME_CONTEXTS.sql
 *
 * This package contains code for regular MID maintenance work
 *
 * Version Information
 * 08/27/2004 3.1.8: Updated code to be cleaner, next: parallelize
 * 04/29/2003 3.1.7: debugging...
 * 07/21/2003 3.1.6: Handle source_rui and relationship_group
 *                   attributes of treepos, written to HCD field
 *                   of contexts_raw (ancestors only)
 * 05/13/2003 3.1.5: Handle sources that have rela rules plus
 *                   sources that do not have rela rules.
 * 08/29/2002 3.1.4: Generate_siblings query further optimized for
 *                   MSH2003_2002_08_14
 * 08/08/2002 3.1.3: 'NO_SIB' section changed from NOT IN to NOT EXISTS
 * 2/28/2001  3.1.2: Optimization in bt_nt_to_treepos with the leaf_id field
 * 11/29/2000 3.1.1: /*+RULE seems to be causing problems add space
 * 5/16/2000  3.1.0: First Version
 *
 * Status:
 *	Functionality:
 *	Testing:
 * 	Enhancements:
 *	      CUI assignment algorithm.
 *
 *****************************************************************************/

    package_name	VARCHAR2(25) := 'MEME_CONTEXTS';
    release_number	VARCHAR2(1) := '4';
    version_number	VARCHAR2(3) := '1.8';
    version_date	DATE := '27-Aug-2004';
    version_authority	VARCHAR2(3) := 'BAC';

    meme_contexts_debug	BOOLEAN := FALSE;
    meme_contexts_trace	BOOLEAN := FALSE;

    location		VARCHAR2(5);
    err_msg		VARCHAR2(256);

    meme_contexts_exception EXCEPTION;

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

    PROCEDURE meme_contexts_error (
	method              IN VARCHAR2,
	location            IN VARCHAR2,
	error_code          IN INTEGER,
	detail              IN VARCHAR2
    );


    --
    -- MEME_CONTEXTS Procedure declarations
    --
    PROCEDURE analyze_tables; 

    FUNCTION qa_treepos
     RETURN INTEGER;

    FUNCTION init_context_numbers
     RETURN INTEGER;

    FUNCTION generate_ancestors(
	source_of_context   IN VARCHAR2,
	use_rela            IN VARCHAR2 := 'Y'
    )RETURN INTEGER;

    FUNCTION generate_children(
	source_of_context   IN VARCHAR2,
	use_rela            IN VARCHAR2 := 'Y'
    )RETURN INTEGER;

    FUNCTION generate_siblings(
	source_of_context   IN VARCHAR2,
	insert_sibs         IN INTEGER,
	use_rela            IN VARCHAR2 := 'Y'
    )RETURN INTEGER;

    FUNCTION qa_contexts_raw
     RETURN INTEGER;

    FUNCTION qa_code_ranges
     RETURN INTEGER;

    FUNCTION bt_nt_to_treepos
      RETURN INTEGER;

    FUNCTION ranges_to_bt_nt 
      RETURN INTEGER;

    FUNCTION prefix_ranges_to_bt_nt 
      RETURN INTEGER;

    FUNCTION generate_parent_treenums 
      RETURN INTEGER;

END meme_contexts;
/
SHOW ERRORS

CREATE OR REPLACE PACKAGE BODY meme_contexts AS

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

    meme_contexts_trace := TRUE;

END set_trace_on;

--************************* SET trace off **********************************
PROCEDURE set_trace_off
IS
BEGIN

    meme_contexts_trace := FALSE;

END set_trace_off;

--************************* SET debug on **********************************
PROCEDURE set_debug_on
IS
BEGIN

    meme_contexts_debug := TRUE;

END set_debug_on;

--************************* SET debug off **********************************
PROCEDURE set_debug_off
IS
BEGIN

    meme_contexts_debug := FALSE;

END set_debug_off;

--************************* TRACE **********************************
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF meme_contexts_trace = TRUE THEN

   	MEME_UTILITY.PUT_MESSAGE(message);

    END IF;

END trace;

--************************* local_exec **********************************
PROCEDURE local_exec ( query IN VARCHAR2 )
IS
BEGIN

    IF meme_contexts_trace = TRUE THEN
	MEME_UTILITY.put_message(query);
    END IF;

    IF meme_contexts_debug = FALSE THEN
	MEME_UTILITY.exec(query);
    END IF;

END local_exec;

--************************* local_exec **********************************
FUNCTION local_exec ( query IN VARCHAR2 )
RETURN INTEGER
IS
BEGIN

    IF meme_contexts_trace = TRUE THEN
	MEME_UTILITY.put_message(query);
    END IF;

    IF meme_contexts_debug = FALSE THEN
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
    DBMS_OUTPUT.ENABLE(100000);

    -- Print version
    MEME_UTILITY.PUT_MESSAGE('.');
    version;
END help;

--************************* SELF_TEST ***************************
PROCEDURE self_test
IS
BEGIN

    DBMS_OUTPUT.ENABLE(100000);
    -- This procedure requires SET SERVEROUTPUT ON

END self_test;

--************************* MEME_CONTEXTS_ERROR ***************************
PROCEDURE meme_contexts_error (
    	method		    IN VARCHAR2,
    	location	    IN VARCHAR2,
    	error_code	    IN INTEGER,
    	detail		    IN VARCHAR2
)
IS
    error_msg	    VARCHAR2(100);
BEGIN
    IF error_code = 1 THEN
	error_msg := 'MO0001: Unspecified error';
    ELSIF error_code = 30 THEN
	error_msg := 'MO0030: Bad count';
    ELSE
	error_msg := 'MO0000: Unknown Error';
    END IF;

    MEME_UTILITY.PUT_ERROR('Error in MEME_CONTEXTS::'||method||' at '||
	location||' ('||error_msg||','||detail||')');

END meme_contexts_error;

--
-- MEME_CONTEXTS Procedures
--

--************************* ANALYZE TABLES **********************************
PROCEDURE analyze_tables 
IS
BEGIN

    meme_system.analyze('treepos'); 
    meme_system.analyze('code_ranges'); 
    meme_system.analyze('bt_nt_rels'); 
    meme_system.analyze('source_atoms'); 
    meme_system.analyze('exclude_list'); 

END analyze_tables;

--************************* QA TREEPOS ***************************
FUNCTION qa_treepos 
 RETURN INTEGER
IS

    query                       VARCHAR2(1024);
    TYPE curvar_type IS REF CURSOR;
    cvar      		curvar_type;
    my_source_atom_id 		treepos.source_atom_id%TYPE;
    my_treenum			treepos.treenum%TYPE;
    path			treepos.treenum%TYPE;
    first_delim			NUMBER;
    term_id2			treepos.treenum%TYPE;
    ct				INTEGER;
    root			treepos.treenum%TYPE;
	
BEGIN

    MEME_UTILITY.timing_start;

    --
    -- Ensure that all termgroups have a / in them after the source name
    --
    location := '10';
    err_msg := 'Error in termgroups not having a / after the source name';
    SELECT count(*) into ct 
    FROM source_atoms 
    WHERE instr(termgroup, '/') = 0;
    IF ct != 0 THEN
	RAISE meme_contexts_exception;
    END IF;

    --
    -- Ensure that first atom_id in each treenum is the root
    --
    location := '15';
    err_msg := 'Error in getting root from treepos';
    SELECT min(substr(treenum, 1, instr(treenum, '.')-1)) INTO root 
    FROM treepos;

    location := '20';
    err_msg := 'Error in cvar loop for qa_treepos';
    OPEN cvar FOR 
        SELECT treenum, source_atom_id 
        FROM treepos;

    LOOP

        FETCH cvar INTO my_treenum, my_source_atom_id;
        EXIT WHEN cvar%NOTFOUND; 

        --
	-- Set path to treenum
       	--
 	path := my_treenum;

       	location := '25';
       	err_msg := 'Error in that first atom_id in treenum is not the root ';
       	IF root != substr(path, 1, instr(path, '.') -1) THEN
           RAISE meme_contexts_exception;
       	END IF;

       	-- make sure that last atom_id in treenum is the source_atom_id
       	location := '30';
       	err_msg := 'Error in inner qa_treepos loop';
       	LOOP 
	    location := '40';
            err_msg := 'Error in getting atom_ids from treenum';
	    first_delim := instr(path, '.');
	    IF first_delim != 0 THEN
               term_id2 := substr(path, 1, (first_delim-1));
	    ELSE
	       term_id2 := path;
	       IF term_id2 != my_source_atom_id THEN
	           location := '45';
	           err_msg := 'Last atom_id in treenum not the source-atom_id';
	           RAISE meme_contexts_exception;
               END IF;
            END IF;
	    path := substr(path, (first_delim+1));


	    location := '50';
            err_msg := 'Error in checking atom_ids are in source_atoms ';
	    SELECT count(*) into ct
	    FROM source_atoms
	    WHERE source_atom_id = term_id2;

            IF ct = 0 THEN
                RAISE meme_contexts_exception;
            END IF;

	    IF first_delim = 0 THEN
	        EXIT;
            END IF;
       	END LOOP;
    END LOOP;
    CLOSE cvar;

    dbms_output.put_line('qa_treepos ' || sysdate); 
    location := '60';
    MEME_UTILITY.timing_stop;
    MEME_UTILITY.log_operation(
	authority => '---', 
	activity => 'MEME_CONEXTS::qa_treepos',
	detail => 'MEME_CONTEXTS.qa_treepos done at ' || SYSDATE,
	transaction_id => 0, 
	work_id => 0,
	elapsed_time => MEME_UTILITY.elapsed_time);
    RETURN 0; 

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('qa_treepos',location,1,err_msg||' '||term_id2||' '||
	SQLERRM);

    RAISE meme_contexts_exception;
END qa_treepos;

--************************* INIT_CONTEXT_NUMBERS ***************************
FUNCTION init_context_numbers
  RETURN INTEGER
IS

BEGIN
    MEME_UTILITY.timing_start;

    -- Set context numbers in treepos    
    UPDATE treepos a
    SET context_number =
      (SELECT count(*)+1 FROM treepos b
       WHERE a.source_atom_id = b.source_atom_id
         AND b.treenum < a.treenum);

    RETURN 0;

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('init_context_numbers',location,1,err_msg||' '||
	SQLERRM);

    RAISE meme_contexts_exception;
END init_context_numbers;


--************************* GENERATE ANCESTORS ***************************
FUNCTION generate_ancestors (
	source_of_context   IN VARCHAR2,
	use_rela	    IN VARCHAR2 := 'Y'
) RETURN INTEGER
IS

    TYPE curvar_type IS REF CURSOR;
    cvar	      		curvar_type;

    context_level		INTEGER;  -- <50=anc, 50=self
    release_mode		NUMBER;
    first_delim_pos		NUMBER;
    commit_ct			INTEGER := 0;
    source			contexts_raw.source%TYPE;
    anc_source_atom_id		VARCHAR2(20);

    tp				treepos%ROWTYPE;
    anc_path			treepos.treenum%TYPE;
    anc_treenum			treepos.treenum%TYPE;
    sa				source_atoms%ROWTYPE;
    has_child			contexts_raw.xc%TYPE;
    sa1				source_atoms%ROWTYPE;
    sa2				source_atoms%ROWTYPE;
    l_rela			VARCHAR2(100);	
BEGIN

    MEME_UTILITY.timing_start;

    location := '120';
    err_msg := 'Error in cvar loop for generate ancestors';
    OPEN cvar FOR 
         SELECT treenum, b.source_atom_id, a.code, b.rela,
		substr(termgroup, 1, instr(termgroup, '/')-1),
		b.context_number, sg_id, sg_type, sg_qualifier
 	 FROM source_atoms a, treepos b
	 WHERE a.source_atom_id = b.source_atom_id;
    LOOP
       FETCH cvar INTO tp.treenum, tp.source_atom_id,
		sa.code, tp.rela, source, tp.context_number,
		sa1.sg_id, sa1.sg_type, sa1.sg_qualifier;
       EXIT WHEN cvar%NOTFOUND; 

       -- Start with ancestor 0
       context_level := 0; 
       -- Initial path is complete path to root
       anc_path := tp.treenum;

       location := '150';
       err_msg := 'Error in ancestor loop';
       LOOP 

	   -- Find the position of the first delimiter
	   first_delim_pos := instr(anc_path, '.');

	   -- Are there more ANC,
	   -- If so get the ancestor term id
	   IF first_delim_pos != 0 THEN
               anc_source_atom_id := substr(anc_path, 1, (first_delim_pos-1));

	   -- Or are we on a CCP row
	   -- "self" term id is the full path
	   ELSE
	       anc_source_atom_id := anc_path;
	       context_level := 50;
           END IF;

	   -- Strip top ancestor off path
	   --   tp.treenum = 1.2.3.4
	   --   anc_path is something like 2.3.4
	   --
	   anc_path := substr(anc_path, (first_delim_pos+1));

	   --  Rela assignment rules  e.g. for 1.2.3.4  
	   --  if context_level = 0 then
	   --    tp.treenum = 1
	   --    the root atom never has a rela so no need to look it up.
	   --  else if context_level = 1 then
	   --    tp.treenum = 1.2 
 	   --    look up the rela for treenum = '1.2' in treepos
	   --  else if context_level = 2 then
	   --    tp.treenum = 1.2.3
	   --    look up the rela for treenum = '1.2.3' in treepos
           --  etc
	   --  else context_level = 50 then  
	   --    tp.treenum = 1.2.3.4
	   --    This is the current treenum row we are looking at 
           --    so rela should be known.
	   --
	   --  if 1.2.3.5 is also a treenum in treepos, when inserting 
           --		 siblings for this treenum we should also add rela
	   --
           --  if 1.2.3.4.6 is also a treenum in treepos, when inserting 
	   --            children we should include the rela.
	   --
	   -- Assign anc_treenum

	   location := '158';
	   err_msg := 'Error getting the anc_treenum';

	   -- If CCP row, the ancestor treenum is the current treenum
	   IF context_level = 50 THEN
	       anc_treenum := tp.treenum;

	   -- Otherwise the ancestor treenum is the top part
	   -- of tp.treenum down to the context level
           ELSIF context_level != 0 THEN
	       anc_treenum := substr(tp.treenum, 1, 
			     instr(tp.treenum, '.', 1, context_level + 1) -1);
           END IF;

	   -- Look up the ancestor name
           location := '155';
           err_msg := 'Error getting atom_name for the ancestor';
           SELECT atom_name, sg_id, sg_type, sg_qualifier
           INTO sa.atom_name, sa2.sg_id, sa2.sg_type, sa2.sg_qualifier
           FROM source_atoms  
           WHERE source_atom_id = anc_source_atom_id;

	   --
	   -- Look up the ancestor HCD, source_rui, and relationship_group
	   -- 
	   location := '157';
	   err_msg := 'Error getting the hcd for this ancestor - ' ||
		      'Make sure that treetop is in treepos.dat file';
	   SELECT hcd,source_rui,relationship_group 
	          INTO tp.hcd,tp.source_rui,tp.relationship_group
	   FROM treepos a
           WHERE anc_source_atom_id = source_atom_id
	     AND tp.treenum like treenum||'%'
             AND (context_level = 0 OR use_rela = 'N' OR 
		  nvl(a.rela,'null') = nvl(tp.rela,'null'));

	   -- If we are on an ancestor row, 
	   -- there are obviously children
           location := '165';
           err_msg := 'Error determining xc field';
	   IF context_level < 50 THEN
	       has_child := 1;

	   -- Otherwise, check to see if there are children
           ELSE
               SELECT /*+ USE_INDEX(a,x_pt_parnum) */ 
	         DECODE(count(parnum),0,0,1) into has_child 
               FROM parent_treenums a
               WHERE parnum = tp.treenum
	         AND (context_level = 0 OR use_rela = 'N' OR 
		      nvl(a.rela,'null') = nvl(tp.rela,'null'));
	   END IF;

	   -- Look up the rela if we are not processing the root ancestor
	   location := '160';
	   err_msg := 'Error getting the rela for this ancestor';
	   l_rela := tp.rela;
	   IF context_level != 0 THEN
	     -- If paying attention to rela, we are looking
	     -- for just the matching rela, if use_rela='Y', then
	     -- the existing value of tp.rela is already correct
	     -- don't waste time with another lookup
	     IF use_rela = 'N' THEN
	       SELECT /*+ RULE */ rela
	       INTO l_rela
	       FROM treepos
               WHERE treenum = anc_treenum;
	     END IF;
 	   ELSE
	       -- Use null rela for tree-top
	       l_rela := '';
           END IF;

	   -- Check if the parent of this ancestor 
	   -- is on the sibling exclude list
           location := '165.1';
           err_msg := 'Error getting my_mrcxt flag';
	   SELECT DECODE(count(parnum),0,1,0) into release_mode
	   FROM exclude_list
	   WHERE token in ('NO_SIB_MRCXT','NO_SIB','NO_SIB_MRREL')
	   and parnum = substr(anc_treenum,1,instr(anc_treenum,'.'||anc_source_atom_id)-1);

           location := '170';
           err_msg := 'Error inserting ancestors into contexts_raw';
           INSERT into contexts_raw
	       (source_atom_id_1, context_number, context_level, sort_field, 
	        atom_name, source_atom_id_2, scd, hcd, rela, 
	        xc, source, source_of_context, mrcxt_flag, mrrel_flag)
	   VALUES
	       (tp.source_atom_id, tp.context_number, context_level, 0,
	        sa.atom_name, anc_source_atom_id, sa.code, 
		tp.hcd || ':' || tp.source_rui || ':' || tp.relationship_group || '~' ||
		sa1.sg_id || '~' || sa1.sg_type || '~' || sa1.sg_qualifier || '~' ||
		sa2.sg_id || '~' || sa2.sg_type || '~' || sa2.sg_qualifier,
		l_rela, has_child, source, source_of_context, 
		release_mode, release_mode );

	   context_level := context_level + 1;

	   IF first_delim_pos = 0 THEN
  	       EXIT;
           END IF;

	   commit_ct := commit_ct + 1;
	   IF MOD(commit_ct, MEME_CONSTANTS.COMMIT_INTERVAL) = 0 THEN
	       COMMIT;
           END IF;

       END LOOP;
    END LOOP;
    CLOSE cvar;

   dbms_output.put_line('gen_ancestors ' || sysdate); 
    MEME_UTILITY.timing_stop;
    MEME_UTILITY.log_operation( '---','generate_ancestors',
	'MEME_CONTEXTS.generate_ancestors done at ' || SYSDATE || 
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN 0; 

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('generate_ancestors',location,1,
	err_msg || 
	', treenum=' || tp.treenum || 
 	', source_atom_id=' || anc_source_atom_id || 
	', rela=' || tp.rela ||
	', sqlerrm=' ||SQLERRM);

    RAISE meme_contexts_exception;
END generate_ancestors;

--************************* GENERATE CHILDREN ***************************
FUNCTION generate_children(
    source_of_context   	IN VARCHAR2,
    use_rela	    		IN VARCHAR2 := 'Y'
)  RETURN INTEGER
IS
    TYPE curvar_type IS REF CURSOR;
    cvar      		curvar_type;
    tp				treepos%ROWTYPE;

    has_child			contexts_raw.xc%TYPE;
    chd_source_atom_id		VARCHAR2(20);
    childnum			treepos.treenum%TYPE;
    atom_name		source_atoms.atom_name%TYPE;
    code			source_atoms.code%TYPE;

    commit_ct			INTEGER:=0;
    source			VARCHAR2(40);
    my_mrcxt			INTEGER := 1;
    my_mrrel			INTEGER := 1;
	
BEGIN

    MEME_UTILITY.timing_start;
    location := '200';
    err_msg := 'Error getting the source' ;
    SELECT substr(max(termgroup), 1, instr(max(termgroup), '/')-1)
    INTO source
    FROM source_atoms;

    -- Account for the rela flag plus
    -- the tree-top atom
    location := '203';
    err_msg := 'Error in cvar loop for generate children';
    OPEN cvar FOR 
	 SELECT a.treenum, a.context_number, source_atom_id, b.treenum, a.rela
         FROM treepos a, parent_treenums b
         WHERE a.treenum = parnum
           AND a.treenum NOT IN 
	     (SELECT parnum FROM exclude_list
	      WHERE token = 'NO_CHD') 
	   AND (use_rela = 'N' OR 
	        nvl(a.rela,'null') = nvl(b.rela,'null') OR
	        a.treenum = to_char(a.source_atom_id) );
    LOOP
       FETCH cvar INTO tp.treenum, tp.context_number, 
	     tp.source_atom_id, childnum, tp.rela;
       EXIT WHEN cvar%NOTFOUND; 

           chd_source_atom_id := substr(childnum, (instr(childnum, '.', -1) + 1));
	
	/****** CHD rows are not loaded into context_relationships
		so their release_mode is not important
           location := '205';
           err_msg := 'Error getting my_mrcxt flag';
	   SELECT DECODE(count(parnum),0,1,0) into my_mrcxt
	   FROM exclude_list 
	   WHERE token = 'NO_CHD_MRCXT' 
	   and parnum = tp.treenum;

           location := '215';
           err_msg := 'Error getting my_mrrel flag';
	   SELECT DECODE(count(parnum),0,1,0) into my_mrrel
	   FROM exclude_list 
	   WHERE token = 'NO_CHD_MRREL'
	   and parnum = tp.treenum;
	********/

           location := '240';
           err_msg := 'Error getting the atom_name for this child';
           SELECT atom_name
           INTO atom_name
           FROM source_atoms 
           WHERE chd_source_atom_id = source_atom_id;

           location := '245';
           err_msg := 'Error getting the code for this parent';
           SELECT code 
           INTO code
           FROM source_atoms 
           WHERE tp.source_atom_id = source_atom_id;

           location := '250';
           err_msg := 'Error getting the hcd for this child';
	   IF tp.treenum != to_char(tp.source_atom_id) THEN
               SELECT hcd
               INTO tp.hcd
               FROM treepos a
               WHERE chd_source_atom_id = source_atom_id
	         AND childnum like treenum || '%'
	         AND (use_rela = 'N' OR 
		      nvl(a.rela,'null') = nvl(tp.rela,'null'));
	   ELSE
		-- Ignore HCD for children of treetop
	       tp.hcd := '';
 	   END IF;

           location := '253';
           err_msg := 'Error in determining parent status for xc field';
	   IF tp.treenum != to_char(tp.source_atom_id) THEN
               SELECT /*+ RULE */ DECODE(count(parnum),0,0,1) into has_child
               FROM parent_treenums a
               WHERE parnum = childnum
	         AND (use_rela = 'N' OR 
		      nvl(a.rela,'null') = nvl(tp.rela,'null'));
	   ELSE
	  	-- Treetop's children always have children
	       has_child := 1;
 	   END IF;

           location := '260';
           err_msg := 'Error inserting children into contexts_raw';
           INSERT into contexts_raw
	   (source_atom_id_1, context_number, context_level, sort_field, 
	    atom_name, source_atom_id_2, scd, hcd, rela, xc, source, 
	    source_of_context, mrcxt_flag, mrrel_flag)
	   VALUES(
	       tp.source_atom_id, tp.context_number, 99, 0,
	       atom_name, chd_source_atom_id, code, tp.hcd, tp.rela, has_child, source,
	       source_of_context, my_mrcxt, my_mrrel );
	   commit_ct := commit_ct + 1;
	   IF MOD(commit_ct, MEME_CONSTANTS.COMMIT_INTERVAL) = 0 THEN
	       COMMIT;
           END IF;
    END LOOP;
    CLOSE cvar;
    
    MEME_UTILITY.timing_stop;
    dbms_output.put_line('gen_children ' || sysdate); 
    MEME_UTILITY.log_operation( '---','generate_children',
	'MEME_CONTEXTS.generate_children done at ' || SYSDATE ||
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN 0;

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('generate_children',location,1,
	err_msg || 
	', child treenum=' || childnum || 
	', source_atom_id=' || tp.source_atom_id || 
	', rela=' || tp.rela || 
	', sqlerrm '|| SQLERRM);

    RAISE meme_contexts_exception;
END generate_children;


--************************* GENERATE SIBLINGS ***************************
FUNCTION generate_siblings (
	source_of_context   IN VARCHAR2,   
        insert_sibs         IN INTEGER,
        use_rela            IN VARCHAR2 := 'Y'
) RETURN INTEGER
IS

    TYPE curvar_type IS REF CURSOR;
    cvar	      		curvar_type;
    sib_a	 		treepos.source_atom_id%TYPE;
    sib_b	 		treepos.source_atom_id%TYPE;
    treenum_a			treepos.treenum%TYPE;
    treenum_b			treepos.treenum%TYPE;
    parnum_a_b			treepos.treenum%TYPE;
    hcd				treepos.hcd%TYPE;
    my_rela			treepos.rela%TYPE;
    cnt				INTEGER := 0;
    atom_name		source_atoms.atom_name%TYPE;
    code			source_atoms.code%TYPE;
    sa1   			source_atoms%ROWTYPE;
    sa2   			source_atoms%ROWTYPE;
    my_context_number		treepos.context_number%TYPE;
    has_child			contexts_raw.xc%TYPE;
    commit_ct			INTEGER:=0;
    source			VARCHAR2(40);
    my_mrcxt			INTEGER := 1;
    my_mrrel			INTEGER := 1;
	
BEGIN

    MEME_UTILITY.timing_start;

    location := '300';
    err_msg := 'Error getting the source' ;
    SELECT substr(max(termgroup), 1, instr(max(termgroup), '/')-1)
    INTO source
    FROM source_atoms;

    location := '302';
    err_msg := 'Error in cvar loop for generate siblings';
    OPEN cvar FOR
	 SELECT /*+ RULE */ 
	     to_number(substr(a.treenum, (instr(a.treenum, '.', -1 ) + 1))),
             to_number(substr(b.treenum, (instr(b.treenum, '.', -1 ) + 1))),
	     a.parnum, b.rela, a.context_number, d.atom_name, c.code,
	     c.sg_id, c.sg_type, c.sg_qualifier, d.sg_id, d.sg_type, d.sg_qualifier
	 FROM parent_treenums a, 
	      (SELECT * FROM parent_treenums a  
	       WHERE parnum in (
		SELECT parnum FROM parent_treenums
		MINUS SELECT parnum FROM exclude_list
		WHERE token='NO_SIB')) b,
	     source_atoms c, source_atoms d
	 WHERE a.parnum = b.parnum 
	   AND a.treenum != b.treenum
	   AND (use_rela = 'N' OR nvl(a.rela,'null') = nvl(b.rela,'null'))
	   AND c.source_atom_id = to_number(substr(a.treenum, (instr(a.treenum, '.', -1 ) + 1)))
	   AND d.source_atom_id = to_number(substr(b.treenum, (instr(b.treenum, '.', -1 ) + 1)));
    LOOP
       FETCH cvar INTO sib_a, sib_b, parnum_a_b, my_rela, my_context_number, atom_name, code,
	sa1.sg_id, sa1.sg_type, sa1.sg_qualifier, sa2.sg_id, sa2.sg_type, sa2.sg_qualifier;
       EXIT WHEN cvar%NOTFOUND;

           --get the source_atom_id for the row
           --sib_a := substr(treenum_a, (instr(treenum_a, '.', -1 ) + 1));
           --sib_b := substr(treenum_b, (instr(treenum_b, '.', -1 ) + 1));
	   treenum_a := parnum_a_b || '.' || sib_a;        
	   treenum_b := parnum_a_b || '.' || sib_b;        

           cnt := cnt + 1;
       
           IF insert_sibs = 1 THEN

	   /*** The release_mode only applies to ANC and CCP rows
		So these lookups are not necessary
               location := '305';
               err_msg := 'Error getting my_mrcxt flag';
	       SELECT DECODE(count(parnum),0,1,0) into my_mrcxt
	       FROM exclude_list 
	       WHERE token = 'NO_SIB_MRCXT'
	       and parnum = parnum_a_b; 

               location := '307';
               err_msg := 'Error getting my_mrrel flag';
	       SELECT DECODE(count(parnum),0,1,0) into my_mrrelt 
	       FROM exclude_list 
	       WHERE token = 'NO_SIB_MRREL'
	       and parnum = parnum_a_b;
	   *****/

	/*
               location := '310';
               err_msg := 'Error getting context_number';
               SELECT /*+ RULE * / context_number 
               INTO my_context_number
               FROM treepos a
               WHERE treenum_a = treenum
	         AND (use_rela = 'N' OR nvl(a.rela,'null') = nvl(my_rela,'null'));

               location := '320';
               err_msg := 'Error getting atom_name';
               SELECT atom_name 
               INTO atom_name
               FROM source_atoms 
               WHERE sib_b = source_atom_id;

               location := '330';
               err_msg := 'Error getting code';
               SELECT code 
               INTO code
               FROM source_atoms 
               WHERE sib_a = source_atom_id;
	*/

               location := '340';
               err_msg := 'Error getting hcd and xc field ';
               SELECT /*+ RULE */ hcd INTO hcd
               FROM treepos a
               WHERE treenum = treenum_b
	         AND (use_rela = 'N' OR nvl(a.rela,'null') = nvl(my_rela,'null'));

               location := '345';
               err_msg := 'Error getting hcd and xc field ';
               SELECT /*+ RULE */ DECODE(count(parnum),0,0,1)
               INTO has_child
               FROM  parent_treenums 
               WHERE parnum = treenum_b
	         AND (use_rela = 'N' OR nvl(rela,'null') = nvl(my_rela,'null'));

               location := '350';
               err_msg := 'Error inserting siblings into contexts_raw';
               INSERT into contexts_raw
                (source_atom_id_1, context_number, context_level, sort_field,
	         atom_name, source_atom_id_2, scd, hcd, rela, xc, source,
	         source_of_context, mrcxt_flag, mrrel_flag)
               VALUES(
                   sib_a,
                   my_context_number,
                   60,
		   0,
                   atom_name,
                   sib_b,
                   code,
                   hcd || '::~' || 
 		   sa1.sg_id || '~' || sa1.sg_type || '~' || sa1.sg_qualifier || '~' ||
	 	   sa2.sg_id || '~' || sa2.sg_type || '~' || sa2.sg_qualifier,
                   my_rela,
		   has_child, 
                   source,
                   source_of_context,
                   my_mrcxt,
                   my_mrrel 
               );
	       commit_ct := commit_ct + 1;
	       IF MOD(commit_ct, MEME_CONSTANTS.COMMIT_INTERVAL) = 0 THEN
	         COMMIT;
               END IF;
           END IF;
    END LOOP;
    CLOSE cvar;
    
    location := '360';
   dbms_output.put_line('gen_siblings ' || sysdate); 
    MEME_UTILITY.timing_stop;
    MEME_UTILITY.log_operation( '---','generate_siblings',
	'MEME_CONTEXTS.generate_siblings done at ' || SYSDATE ||
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN cnt; 

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('generate_siblings',location,1,err_msg||' '||parnum_a_b||' '||treenum_a||' '||treenum_b||' '||SQLERRM);

    RAISE meme_contexts_exception;
END generate_siblings;

--************************* QA CONTEXTS RAW  ***************************
FUNCTION qa_contexts_raw 
 RETURN INTEGER
IS
    rowcount1				INTEGER;
    rowcount2				INTEGER;
    rowcount3				INTEGER;
	
BEGIN

    --ensure that the number of unique source_atom_ids in treepos is the
    --same as that in contexts_raw and that the number contexts per source_
    --atom_id is also the same

    MEME_UTILITY.timing_start;
    location := '400';
    err_msg := 'Error in creating treepos_cxts';
    MEME_UTILITY.drop_it('table', 'treepos_cxts');
    local_exec
     ('CREATE table treepos_cxts as
       SELECT source_atom_id, 
	   count(*) as cnt
       FROM treepos
       GROUP BY source_atom_id');

    location := '410';
    err_msg := 'Error in getting rowcount of treepos_cxts';
    rowcount1 := MEME_UTILITY.exec_select(
     'SELECT count(*) 
      FROM treepos_cxts');

    location := '420';
    err_msg := 'Error in creating contexts_raw_cxts';
    MEME_UTILITY.drop_it('table', 'contexts_raw_cxts');
    local_exec
     ('CREATE table contexts_raw_cxts as
       SELECT source_atom_id_1 as source_atom_id, 
           count(distinct context_number) as cnt
       FROM contexts_raw
       GROUP BY source_atom_id_1');

    location := '430';
    err_msg := 'Error in getting rowcount of contexts_raw_cxts';
    rowcount2:= MEME_UTILITY.exec_select(
     'SELECT count(*) 
      FROM contexts_raw_cxts');

    location := '440';
    err_msg := 'Error that rowcounts of context comparison tables unequal';
    IF rowcount1 != rowcount2 THEN
	RAISE meme_contexts_exception;
    END IF;

    location := '450';
    err_msg := 'Error getting count of intersection of cxt comparison tables';
    rowcount3:= MEME_UTILITY.exec_select(
     'SELECT count(*)
     FROM treepos_cxts a, contexts_raw_cxts b
     WHERE a.source_atom_id = b.source_atom_id
      and a.cnt = b.cnt'); 

    location := '460';
    err_msg := 'Error in context number processing';
    IF (rowcount3 != rowcount2) THEN
       RAISE meme_contexts_exception;
    END IF;

    MEME_UTILITY.drop_it('table', 'treepos_cxts');
    MEME_UTILITY.drop_it('table', 'contexts_raw_cxts');
    
    location := '470';
    MEME_UTILITY.timing_stop;
    MEME_UTILITY.log_operation( '---','qa_contexts_raw',
	'MEME_CONTEXTS.qa_contexts_raw done at ' || SYSDATE ||
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN 0; 

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('qa_contexts_raw',location,1,err_msg||' '||
	SQLERRM);

    RAISE meme_contexts_exception;
END qa_contexts_raw;

--************************* BT_NT_TO_TREEPOS ***************************
FUNCTION bt_nt_to_treepos
   RETURN INTEGER
IS
    query			VARCHAR2(1024);
    ct				INTEGER;
    rowcount1			INTEGER;
	
BEGIN
    MEME_UTILITY.timing_start;

    MEME_SYSTEM.drop_indexes('treepos');

    location := '500';
    err_msg := 'Error creating table btnt1';
    MEME_UTILITY.drop_it('table', 'btnt1');
    EXECUTE IMMEDIATE
     'CREATE table btnt1 (
       unique_id            NUMBER(12),
       source_atom_id	    NUMBER(12),
       treenum	    	    VARCHAR2(1000),
       leaf_id		    NUMBER(12)
      )';

    location := '510';
    err_msg := 'Error inserting initial terms into btnt1';
    EXECUTE IMMEDIATE
     'INSERT into btnt1 (source_atom_id, treenum)
      SELECT source_atom_id_1, to_char(source_atom_id_1)
      FROM bt_nt_rels
      UNION
      SELECT source_atom_id_2, to_char(source_atom_id_2)
      FROM bt_nt_rels';

    location := '515';
    err_msg := 'Error inserting initial terms into btnt1';
    EXECUTE IMMEDIATE
     'UPDATE btnt1 set unique_id = rownum';

    LOOP 
        location := '530';
        err_msg := 'Error creating table btnt2';
        COMMIT;
        MEME_UTILITY.drop_it('table', 'btnt2');
	EXECUTE IMMEDIATE
          'CREATE table btnt2 as 
	   SELECT unique_id,
	      source_atom_id_1 as source_atom_id,
	      source_atom_id_1|| ''.'' ||treenum as treenum,
	      to_number(substr(treenum, instr(treenum, ''.'', -1) + 1)) as leaf_id
	  FROM btnt1 a, bt_nt_rels b
	  WHERE a.source_atom_id = b.source_atom_id_2 ';
       	ct := SQL%ROWCOUNT;

       	location := '535';
       	err_msg := 'Error counting btnt2.';
       	rowcount1 := 
	  MEME_UTILITY.exec_select('SELECT count(*) FROM btnt2 a');

       	location := '540';
       	err_msg := 'Error inserting into treepos from btnt1 and bt_nt_rels';
	EXECUTE IMMEDIATE
       	    'INSERT INTO treepos 
		(source_atom_id, treenum, rela, source_rui, relationship_group)
       	     SELECT b.source_atom_id, a.treenum, 
		c.rela, c.source_rui, c.relationship_group
             FROM btnt1 a, source_atoms b, bt_nt_rels c
             WHERE a.source_atom_id = b.source_atom_id
	       AND leaf_id = c.source_atom_id_2
               AND a.treenum like ''%''||c.source_atom_id_1||''.''||c.source_atom_id_2
	       AND a.unique_id IN
	   	(SELECT unique_id FROM btnt1
	     	 MINUS
	     	 SELECT unique_id FROM btnt2) ';

	-- DONT NEED TO DO EVERY TIME!	
       	-- insert single atom treenums into treepos separately
       	location := '550';
       	err_msg := 'Error inserting into treepos from btnt1 and bt_nt_rels';
       	EXECUTE IMMEDIATE
       	    'INSERT into treepos (source_atom_id, treenum, 
		rela, source_rui, relationship_group)
       	     SELECT b.source_atom_id, a.treenum, null, null, null
       	     FROM btnt1 a, source_atoms b
             WHERE a.source_atom_id = b.source_atom_id
	       AND a.treenum = to_char(a.source_atom_id)
	       AND a.unique_id IN
	    	(SELECT unique_id FROM btnt1
	     	 MINUS
	     	 SELECT unique_id FROM btnt2) ';

	COMMIT;

       	EXIT WHEN rowcount1=0; 
       
       	location := '560';
       	err_msg := 'Error creating new btnt1';
       	MEME_UTILITY.drop_it('table', 'btnt1');
       	query :=
         'CREATE TABLE btnt1 AS
	  SELECT rownum as unique_id,
	      source_atom_id, 
	      treenum, 
	      leaf_id
	  FROM btnt2 ';
       ct := local_exec(query);

    END LOOP; 

    location := '570';
    err_msg := 'Error updating source_atom_id field of treepos';
    EXECUTE IMMEDIATE
        'UPDATE treepos
     	 SET source_atom_id =
     	    substr(treenum, instr(treenum, ''.'', -1)+1) ';

    location := '580';
    err_msg := 'Error updating source_atom_id field of treepos';
    EXECUTE IMMEDIATE
     	'UPDATE treepos
      	 SET treepos.hcd =
           (SELECT source_atoms.hcd
            FROM source_atoms
            WHERE source_atoms.source_atom_id = treepos.source_atom_id) ';

    MEME_UTILITY.drop_it('table', 'btnt1');
    MEME_UTILITY.drop_it('table', 'btnt2');

    location := '600.1';
    EXECUTE IMMEDIATE
	'ALTER TABLE treepos MOVE PARALLEL';
    location := '600.2';
    EXECUTE IMMEDIATE
	'CREATE INDEX X_TP_SAID ON treepos(source_atom_id)
	 PCTFREE 10 STORAGE (INITIAL 40M) TABLESPACE MIDI PARALLEL';
    location := '600.3';
    EXECUTE IMMEDIATE
	'CREATE INDEX X_TP_TN ON treepos(treenum)
	 PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MIDI PARALLEL';

    location := '590';
    MEME_UTILITY.timing_stop;
    location := '595';
    MEME_UTILITY.log_operation( '---','bt_nt_to_treepos',
	'MEME_CONTEXTS.bt_nt_to_treepos done at ' || SYSDATE || 
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN 0;

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('bt_nt_to_treepos',location,1,
        err_msg ||
	', sqlerrm=' ||SQLERRM);

    RAISE meme_contexts_exception;
END bt_nt_to_treepos;

--************************* RANGES_TO_BT_NT ***************************
FUNCTION ranges_to_bt_nt 
   RETURN INTEGER
IS
    TYPE curvar_type IS REF CURSOR;
    cvar      		curvar_type;
    my_bt_id			NUMBER(12);
    my_source_atom_id		NUMBER(12);
    my_context_level		NUMBER(12);
    my_high_range		VARCHAR2(30);
    my_low_range		VARCHAR2(30);
    code			VARCHAR2(30);
    query			VARCHAR2(1024);
    ct				INTEGER;
	
BEGIN

    MEME_UTILITY.timing_start;

    location := '600';
    err_msg := 'Error creating bt_options table';
    MEME_UTILITY.drop_it('table', 'bt_options');
    local_exec(
     'CREATE table bt_options as 
      SELECT source_atom_id, 
	     context_level,
	     high_range, 
	     low_range
      FROM code_ranges
      WHERE 0=1');

    location := '610';
    err_msg := 'Error in cursor loop on code_ranges in ranges_to_bt_nt';
    OPEN cvar FOR select source_atom_id, context_level, high_range, low_range 
	 from code_ranges;
    LOOP
       FETCH cvar INTO my_source_atom_id, my_context_level, my_high_range, my_low_range;
       EXIT WHEN cvar%NOTFOUND; 

       location := '620';
       err_msg := 'Error while  inserting into bt_options';
       EXECUTE IMMEDIATE
          'INSERT into bt_options 
           SELECT source_atom_id, context_level, high_range, low_range
           FROM code_ranges
           WHERE :low_range >= low_range
             AND :high_range <= high_range
             AND :source_atom_id != source_atom_id'
       USING my_low_range, my_high_range, my_source_atom_id;

       location := '622';
       err_msg := 'Error while deleting from bt_options ranges not on the correct level';
       EXECUTE IMMEDIATE
           'DELETE from bt_options
	    WHERE  context_level != (:context_level - 1)'
       USING my_context_level;

       location := '623';
       err_msg := 'Error while deleting from bt_options leaving the lowest range';
       EXECUTE IMMEDIATE
          'DELETE from bt_options
	   WHERE  low_range not in
	    (SELECT max(low_range)
	     FROM bt_options)';

       location := '626';
       err_msg := 'Error while deleting from bt_option leaving the highest range';
       EXECUTE IMMEDIATE
           'DELETE from bt_options
	    WHERE  high_range not in
	       (SELECT min(high_range)
	        FROM bt_options)';

       location := '630';
       err_msg := 'Error while inserting into bt_nt_rels from code_ranges';
	EXECUTE IMMEDIATE
	   'INSERT into bt_nt_rels
       		(source_atom_id_1, source_atom_id_2)
            SELECT source_atom_id, :source_atom_id
            FROM bt_options'
	USING my_source_atom_id;
       ct := SQL%ROWCOUNT;

       location := '640';
       err_msg := 'Error while truncating bt_options';
       MEME_SYSTEM.truncate ('bt_options');

    END LOOP;

    location := '645';
    err_msg := 'Error while deleting ranges from source atoms';
    DELETE from source_atoms
    WHERE source_atom_id in 
      (SELECT source_atom_id 
       FROM code_ranges);

    location := '650';
    err_msg := 'Error while inserting into bt_nt_rels from source_atoms';
    INSERT into bt_nt_rels
      (source_atom_id_1, source_atom_id_2)
    SELECT a.source_atom_id as par_id, 
	    b.source_atom_id as chd_id
    FROM code_ranges a,
          (SELECT a.source_atom_id, a.code,
	     max(low_range) as low_range,
	     min(high_range) as high_range,
	     max(context_level) as context_level 
           FROM source_atoms a, code_ranges b
           WHERE code >= low_range
	   and code <= high_range
	   GROUP by a.source_atom_id, a.code) b
    WHERE a.low_range = b.low_range
      AND a.high_range = b.high_range
      AND a.context_level = b.context_level;
 
    location := '690';
    err_msg := 'Error dropping bt_options table';
    MEME_UTILITY.drop_it('table', 'bt_options');

    MEME_UTILITY.timing_stop;
    MEME_UTILITY.log_operation( '---','ranges_to_bt_nt',
	'MEME_CONTEXTS.ranges_to_bt_nt done at ' || SYSDATE ||
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN 0;

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('ranges_to_bt_nt',location,1,err_msg||' '||
	SQLERRM);

    RAISE meme_contexts_exception;
END ranges_to_bt_nt;

--************************* PREFIX_RANGES_TO_BT_NT ***************************
FUNCTION prefix_ranges_to_bt_nt 
   RETURN INTEGER
IS
    TYPE curvar_type IS REF CURSOR;
    cvar      		curvar_type;
    my_bt_id			NUMBER(12);
    my_source_atom_id		NUMBER(12);
    my_context_level		NUMBER(12);
    my_high_range		VARCHAR2(30);
    my_low_range		VARCHAR2(30);
    code			VARCHAR2(30);
    my_par_code 		VARCHAR2(30);
    query			VARCHAR2(1024);
    ct				INTEGER;
	
BEGIN

    MEME_UTILITY.timing_start;

    location := '700';
    err_msg := 'Error creating bt_options table';
    MEME_UTILITY.drop_it('table', 'bt_options');
    local_exec(
     'CREATE table bt_options as 
      SELECT source_atom_id, 
	     context_level,
	     high_range, 
	     low_range
      FROM code_ranges
      WHERE 0=1');

    -- loop inserts from code_ranges into bt_nt_rels
    -- after loop, codes from source_atoms are inserted into bt_nt_rels
    location := '710';
    err_msg := 'Error while cursor loop in ranges_to_bt_nt';
    OPEN cvar FOR select source_atom_id, context_level, high_range, low_range from code_ranges;
    LOOP
       FETCH cvar INTO my_source_atom_id, my_context_level, my_high_range, my_low_range;
       EXIT WHEN cvar%NOTFOUND; 

       location := '720';
       err_msg := 'Error while inserting from code_ranges into bt_options';
       local_exec
       (' INSERT into bt_options 
         SELECT source_atom_id, context_level, high_range, low_range 
         FROM code_ranges  '  ||
         'WHERE '''||my_low_range||''' >= low_range ' ||
         'and '''||my_high_range||''' <= high_range ' ||
         'and '''||my_source_atom_id||''' != source_atom_id'); 

       location := '722';
       err_msg := 'Error while deleting from bt_options ranges not on the correct level';
       local_exec
       (' DELETE from bt_options
	 WHERE  context_level != '''||my_context_level||''' - 1');

       location := '723';
       err_msg := 'Error while inserting from code_ranges into bt_options';
       local_exec
       (' DELETE from bt_options
	 WHERE  low_range not in
	   (SELECT max(low_range)
	   FROM bt_options)');

       location := '726';
       err_msg := 'Error while inserting from code_ranges into bt_options';
       local_exec
       (' DELETE from bt_options
	 WHERE  high_range not in
	   (SELECT min(high_range)
	   FROM bt_options)');

       location := '730';
       err_msg := 'Error while inserting from code_ranges into bt_nt_rels';
       query :=
       'INSERT into bt_nt_rels '||
       '  (source_atom_id_1, source_atom_id_2) '||
       ' (SELECT source_atom_id, '''||my_source_atom_id||''''|| 
       ' FROM bt_options ' || 
       ' )'; 
       local_exec(query);

       location := '740';
       err_msg := 'Error while truncating bt_options';
       MEME_SYSTEM.truncate ('bt_options'); 

    END LOOP;

    location := '750';
    err_msg := 'Error while deleting ranges from source atoms';
    DELETE from source_atoms
    WHERE source_atom_id in (
     SELECT source_atom_id 
     FROM code_ranges);

    location := '760';
    err_msg := 'Error while inserting prefixed codes with decimal places into bt_nt_rels';
    INSERT into bt_nt_rels
    (source_atom_id_1, source_atom_id_2)
     SELECT a.source_atom_id, b.source_atom_id
     FROM source_atoms a,
       (SELECT b.source_atom_id , max(a.code) as code
	FROM source_atoms a, source_atoms b
	WHERE a.source_atom_id != b.source_atom_id
	 and b.code like '%.%'
	 and b.code like a.code||'_%'
        GROUP by b.source_atom_id) b
     WHERE a.code = b.code
      and a.source_atom_id != b.source_atom_id; 

    location := '770';
    err_msg := 'Error while inserting integer prefixed codes into bt_nt_rels';
    INSERT into bt_nt_rels
    (source_atom_id_1, source_atom_id_2)
     SELECT a.source_atom_id as par_id, 
	    b.source_atom_id as chd_id
     FROM code_ranges a,
          (SELECT a.source_atom_id, a.code,
	     max(low_range) as low_range,
	     min(high_range) as high_range,
	     max(context_level) as context_level
           FROM source_atoms a, code_ranges b
           WHERE code not like '%.%'
	   and code >= low_range
	   and code <= high_range
	   GROUP by a.source_atom_id, a.code) b
     WHERE a.low_range = b.low_range
       and a.high_range = b.high_range
       and a.context_level = b.context_level;

    MEME_UTILITY.drop_it('table', 'bt_options');

    MEME_UTILITY.timing_stop;
    MEME_UTILITY.log_operation( '---','prefix_ranges_to_bt_nt',
	'MEME_CONTEXTS.prefix_ranges_to_bt_nt done at ' || SYSDATE ||
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN 0;

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('prefix_ranges_to_bt_nt',location,1,err_msg||' '||
	SQLERRM);

    RAISE meme_contexts_exception;
END prefix_ranges_to_bt_nt;

--*************************  GENERATE PARENT_TREENUMS **************************
FUNCTION generate_parent_treenums 
   RETURN INTEGER
IS
BEGIN

    location := '0';
    err_msg := 'Error generating parent treenums';
    MEME_UTILITY.timing_start;

    location := '10';
    err_msg := 'Error loading parent_treenums';
    INSERT 
	 INTO parent_treenums (treenum, parnum, rela, context_number)
    SELECT distinct treenum, 
	   substr(treenum, 1, (instr(treenum, '.', -1) -1)), 
	   rela, context_number
    FROM treepos
    WHERE instr(treenum, '.', -1) !=0;

    MEME_UTILITY.timing_stop;
    MEME_UTILITY.log_operation( '---','generate_parent_treenums',
	'MEME_CONTEXTS.generate_parent_treenums done at ' || SYSDATE ||
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN 0;

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('generate_parent_treenums',location,1,err_msg||' '||
	SQLERRM);

    RAISE meme_contexts_exception;
END generate_parent_treenums;

--************************* QA CODE RANGES  ***************************
FUNCTION qa_code_ranges
 RETURN INTEGER
IS
    TYPE curvar_type IS REF CURSOR;
    qa_cursor  		    		curvar_type;
    rowcount1				INTEGER;
    rowcount2				INTEGER;
    rowcount3				INTEGER;
    my_source_atom_id			NUMBER(12);
    code				VARCHAR2(30);
	
BEGIN

    MEME_UTILITY.timing_start;
    location := '900';
    err_msg := 'Error checking if broader range has higher context level';
    MEME_UTILITY.drop_it('table', 'treepos_cxts');
    rowcount1:= MEME_UTILITY.exec_select(
    'SELECT count(*)
     FROM code_ranges a, code_ranges b
     WHERE a.high_range > b.high_range
      and a.low_range < b.low_range
      and a.context_level > b.context_level');

    location := '910';
    err_msg := 'Error in that broader range has higher context level';
    IF rowcount1 != 0 THEN
       RAISE meme_contexts_exception;
    END IF;

    location := '920';
    err_msg := 'Error in loop ensuring that all source_atom_ids are in a range';
    OPEN qa_cursor FOR select source_atom_id, code 
     from source_atoms 
     where source_atom_id not in (select source_atom_id from code_ranges);
    LOOP
	FETCH qa_cursor INTO my_source_atom_id, code;
	EXIT WHEN qa_cursor%NOTFOUND;

        location := '930';
        err_msg := 'Error ensuring that all source_atom_ids are in a range';
        rowcount2:= MEME_UTILITY.exec_select(
        'SELECT count(*) '||
        'FROM code_ranges a '||
        'WHERE '''||code||''' <= a.high_range '|| 
        '   and '''||code||''' >= a.low_range ' );
	
        IF rowcount2 = 0 THEN
	   dbms_output.put_line(code||' '||my_source_atom_id);
           RAISE meme_contexts_exception;
        END IF;  

    END LOOP;

    location := '470';
    MEME_UTILITY.timing_stop;
    MEME_UTILITY.log_operation( '---','qa_code_ranges',
	'MEME_CONTEXTS.qa_code_ranges done at ' || SYSDATE || 
	'. Elapsed time was ' || MEME_UTILITY.elapsed_time ||
	' seconds.', 0, 0);
    RETURN 0; 

EXCEPTION
  WHEN OTHERS THEN
    meme_contexts_error('qa_code_ranges',location,1,err_msg||' '||
	SQLERRM);
    RAISE meme_contexts_exception;
END qa_code_ranges;

END MEME_CONTEXTS;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_CONTEXTS.help
