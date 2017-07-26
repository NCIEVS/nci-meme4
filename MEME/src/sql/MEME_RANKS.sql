CREATE OR REPLACE PACKAGE MEME_RANKS AS

/*******************************************************************************
 *
 * PL/SQL File: MEME_RANKS.sql
 *
 * This file contains the MEME_RANKS package
 *
 * Version Information
 *
 * 03/23/2006 BAC (1-AR2HF): changed set_ranks params to have classes_flag
 *      be the only default "yes" case.
 * 12/30/2004  3.16.0: Released
 * 12/29/2004  3.15.2: set_ranks and set_preference now take an optional
 *                     work_id.
 * 12/21/2004  3.15.1: All code for preferred_atom_id now uses editing rank 
 * 12/13/2004  3.15.0: Released
 * 12/08/2004  3.14.2: "set_preference" uses editing ranking
 * 11/23/2004  3.14.1: All termgroup_rank references use release_rank
 * 11/19/2004  3.14.0: Released
 * 11/03/2004  3.13.1: Fixes for set_ranks
 * 06/14/2004  3.13.0: Improved performance for set_ranks , Released
 * 04/19/2004  3.12.0: Released
 * 09/30/2003  3.11.1: set_preference, small change
 * 09/30/2003  3.11.0: Atom ranking algorithm for set_preference upgraded
 * 02/18/2003  3.10.0: Released
 * 01/28/2003  3.9.1:  Small change to set_rank, get_rank to 
 *                     allow either short or long names for core tables.
 * 12/14/2001  3.9.0:  Released
 * 12/13/2001  3.8.2:  pflag_[cls] should only consider releasable atoms
 *			This is true for foreign_classes also!
 * 11/20/2001  3.8.1:   rank_atom_for_release was ranking <2> MM atoms
 *                      above <1> MM atoms.  This was fixed.
 * 09/05/2001  3.8.0:   Released 7.* changes
 * 07/19/2001  3.7.2:   pflag_[cls] work for foreign_classes as well as
 *                      classes. Many atom ranking procedures were similarly
 *                      upgraded.  Anywhere that a foreign_flag parameter
 *                      exists, it either has a value of MEME_CONSTANTS.NO
 * 		 	or it is the foreign language itself
 * 06/06/2001  3.7.1:   Problem with set_atom_rank, it was using 
 *                      rank_to_number which shouldn't be used on atom ranks
 *                      because both SUI and atom_id must be stripped off.
 *                      use rank_atom_as_number.
 * 06/04/2001  3.7.0:   6.* changes Released
 * 05/29/2001  3.6.4:   Changes to: rank_atom, get_preferred_atom, 
 *                      rank_atom_as_number, affects_atom_rank, to introduce
 *			SUI into the calculation.
 *
 *			set_ranks updated to account for S level attributes
 *			(LEXICAL_TAGs) where source is not in source_rank.
 *			Also for relationships (SFO/LFO)
 * 05/25/2001  3.6.3:   Do NOT compare PNs to MMs in rank_atom_for_release
 * 04/26/2001  3.6.2:   remove GROUP BY termgroup from pflag_[cls]
 * 03/14/2001  3.6.1:	changes in pflag_[cls] to use rank_atom_for_release.
 * 11/10/2000  3.6.0:	Released
 * 9/21/2000  3.5.1:	changes in set_ranks.  A problem with the ct,ct2
 *			counters was causing a situation where it would not
 *			commit.
 * 9/11/2000  3.5.0:	Released to nlm.
 * 9/01/2000  3.4.2:	pflag_c, pflag_l, pflag_s
 * 8/24/2000  3.4.1:	rank_atom_for_release
 * 8/01/2000  3.4.0:	Package handover version
 * 6/12/2000  3.3.5:	set-preference uses dynamic sql
 *	3/6/2000:	suppressible can be '' and is equivalent to 'N'
 *	2/7/2000:	VERSION 3 released
 *	7/1/1999:	First version created and compiled
 * 
 * Status:
 *	Functionality:	DONE
 *	Testing:	DONE
 *	Enhancements:
 *	 	rank fields in CRACS should support row_ids,
 *		 (this affects all the rank_* functions and set_ranks)
 *		 this would cause problems for openroad objects
 *  		set_ranks should work on source_* tables.
 *
 ******************************************************************************/

    package_name	VARCHAR2(25) := 'MEME_RANKS';
    release_number	VARCHAR2(1)  := '4';
    version_number	VARCHAR2(5)  := '16.0';
    version_date	DATE	     := '30-Dec-2004';
    version_authority	VARCHAR2(3)  := 'BAC';

    meme_ranks_debug	BOOLEAN := FALSE;
    meme_ranks_trace	BOOLEAN := FALSE;

    location		VARCHAR2(10);
    err_msg		VARCHAR2(256);
    method		VARCHAR2(256);

    PROCEDURE initialize_trace ( method  IN VARCHAR2 );
    PRAGMA restrict_references (initialize_trace,WNDS,RNDS);

    meme_ranks_exception EXCEPTION;

    FUNCTION release
    RETURN INTEGER;

    FUNCTION version_info
    RETURN VARCHAR2;

    PRAGMA restrict_references (version_info,WNDS,RNDS,WNPS);

    FUNCTION version
    RETURN FLOAT;

    PROCEDURE version;

    PROCEDURE set_trace_on;
    PROCEDURE set_trace_off;
    PROCEDURE set_debug_on;
    PROCEDURE set_debug_off;
    PROCEDURE trace ( message IN VARCHAR2 );

    PROCEDURE local_exec (query in varchar2);
    FUNCTION local_exec (query IN VARCHAR2) RETURN INTEGER;

    PROCEDURE help;

    PROCEDURE help (
	topic IN VARCHAR2
    );

    PROCEDURE register_package;

    PROCEDURE self_test;

    PROCEDURE meme_ranks_error (
    	method		    IN VARCHAR2,
    	location	    IN VARCHAR2,
    	error_code	    IN INTEGER,
    	detail		    IN VARCHAR2
    );

    FUNCTION get_rank (
	row_id 		IN INTEGER,
    	table_name	IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_rank,WNDS);

    FUNCTION set_rank (
	row_id 		IN INTEGER,
    	table_name	IN VARCHAR2,
	field_name	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
    ) RETURN INTEGER;

    FUNCTION rank_to_number (
	rank	VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (rank_to_number,WNDS,RNDS);

    FUNCTION set_preferred_id (
	concept_id 	IN INTEGER,
	changed_field	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
    ) RETURN INTEGER;

    FUNCTION affects_atom_rank(
	field_name IN VARCHAR2
    ) RETURN BOOLEAN;

    FUNCTION rank_atom_as_number (
    	atom_id IN INTEGER,
	source_flag IN VARCHAR2 := MEME_CONSTANTS.NO,
	foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN INTEGER;

    PRAGMA restrict_references (rank_atom_as_number,WNDS);

    FUNCTION rank_atom (
    	atom_id IN INTEGER,
	source_flag IN VARCHAR2 := MEME_CONSTANTS.NO,
	foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (rank_atom,WNDS);

    FUNCTION rank_atom_for_release (
    	atom_id IN INTEGER,
	termgroup IN VARCHAR2,
	foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (rank_atom_for_release,WNDS);

    FUNCTION calculate_preferred_atom (
	concept_id IN INTEGER
    ) RETURN INTEGER;

    PRAGMA restrict_references (calculate_preferred_atom,WNDS);

    FUNCTION get_preferred_atom (
	concept_id IN INTEGER
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_preferred_atom,WNDS);

    FUNCTION set_atom_rank (
	atom_id IN INTEGER,
	changed_field	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
    ) RETURN INTEGER;

    FUNCTION affects_relationship_rank(
	field_name IN VARCHAR2
    ) RETURN BOOLEAN;

    FUNCTION rank_relationship_as_number (
	relationship_id	 IN INTEGER,
	source_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN INTEGER;

    PRAGMA restrict_references (rank_relationship_as_number,WNDS);

    FUNCTION rank_relationship (
	relationship_id	 IN INTEGER,
	source_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (rank_relationship,WNDS);

    FUNCTION get_preferred_relationship (
	concept_id_1 IN INTEGER,
	concept_id_2 IN INTEGER
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_preferred_relationship,WNDS);

    FUNCTION set_relationship_rank (
	relationship_id IN INTEGER,
	changed_field	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
    ) RETURN INTEGER;

    FUNCTION affects_attribute_rank(
	field_name IN VARCHAR2
    ) RETURN BOOLEAN;

    FUNCTION rank_attribute_as_number (
	attribute_id	 IN INTEGER,
	source_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN INTEGER;

    PRAGMA restrict_references (rank_attribute_as_number,WNDS);

    FUNCTION rank_attribute (
    	attribute_id	IN INTEGER,
	source_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN VARCHAR2;

    PRAGMA restrict_references (rank_attribute,WNDS);

    FUNCTION set_attribute_rank (
	attribute_id IN INTEGER,
	changed_field	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
    ) RETURN INTEGER;

    FUNCTION get_level_rank (
	table_name IN VARCHAR2,
	level IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_level_rank,WNDS);

    FUNCTION get_status_rank (
	table_name IN VARCHAR2,
	status IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_status_rank,WNDS);

    FUNCTION get_level_status_rank (
	table_name IN VARCHAR2,
	level IN VARCHAR2,
	status IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_level_status_rank,WNDS);

    FUNCTION get_released_rank (
	released IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_released_rank,WNDS);

    FUNCTION get_tobereleased_rank (
	tobereleased IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_tobereleased_rank,WNDS);

    FUNCTION get_generated_rank (
	generated IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_generated_rank,WNDS);

    FUNCTION get_suppressible_rank (
	suppressible IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_suppressible_rank,WNDS);

    FUNCTION get_authority_rank (
	authority IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_authority_rank,WNDS);

    FUNCTION get_source_rank (
	source IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_source_rank,WNDS);

    FUNCTION get_source_authority_rank (
	source IN VARCHAR2,
	authority IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_source_authority_rank,WNDS);

    FUNCTION get_termgroup_rank (
	termgroup IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_termgroup_rank,WNDS);

    FUNCTION get_relationship_name_rank (
	relationship_name IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_relationship_name_rank,WNDS);

    FUNCTION get_rel_attribute_rank (
	relationship_attribute IN VARCHAR2
    ) RETURN INTEGER;

    PRAGMA restrict_references (get_rel_attribute_rank,WNDS);

    PROCEDURE set_preference (
	work_id INTEGER := 0
    );

    PROCEDURE set_ranks (
	classes_flag 		VARCHAR2 := MEME_CONSTANTS.YES,
	relationships_flag 	VARCHAR2 := MEME_CONSTANTS.NO,
	attributes_flag 	VARCHAR2 := MEME_CONSTANTS.NO,
	source_processing_flag 	VARCHAR2 := MEME_CONSTANTS.NO,
	work_id	 		INTEGER := 0
    );

    FUNCTION pflag_c (
	atom_id 		INTEGER,
	concept_id		INTEGER,
        foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN VARCHAR2;

    FUNCTION pflag_l (
       atom_id			INTEGER,
       concept_id		INTEGER,
       foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN VARCHAR2;

    FUNCTION pflag_s (
       atom_id			INTEGER,
       concept_id		INTEGER,
       foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
    ) RETURN VARCHAR2;

END meme_ranks;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY MEME_RANKS AS

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

/* FUNCTION VERSION_INFO *******************************************************
 */
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
    return package_name || ' Release ' || release_number || ': ' ||
	   'version ' || version_number || ' (' || version_date || ')';
END version_info;

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

    meme_ranks_trace := TRUE;

END set_trace_on;

/* PROCEDURE SET_TRACE_OFF *****************************************************
 */
PROCEDURE set_trace_off
IS
BEGIN

    meme_ranks_trace := FALSE;

END set_trace_off;

/* PROCEDURE SET_DEBUG_ON ******************************************************
 */
PROCEDURE set_debug_on
IS
BEGIN

    meme_ranks_debug := TRUE;

END set_debug_on;

/* PROCEDURE SET_DEBUG_OFF *****************************************************
 */
PROCEDURE set_debug_off
IS
BEGIN

    meme_ranks_debug := FALSE;

END set_debug_off;

/* PROCEDURE TRACE *************************************************************
 */
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF meme_ranks_trace = TRUE THEN

   	MEME_UTILITY.PUT_MESSAGE(message);

    END IF;

END trace;


/* PROCEDURE LOCAL_EXEC ********************************************************
 */
PROCEDURE local_exec ( query IN VARCHAR2 )
IS
BEGIN

    IF meme_ranks_trace = TRUE THEN
	MEME_UTILITY.PUT_MESSAGE(query);
    END IF;

    IF meme_ranks_debug = FALSE THEN
	MEME_UTILITY.exec(query);
    END IF;

END local_exec;

/* FUNCTION LOCAL_EXEC *********************************************************
 */
FUNCTION local_exec ( query IN VARCHAR2 )
RETURN INTEGER
IS
BEGIN

    IF meme_ranks_trace = TRUE THEN
	MEME_UTILITY.PUT_MESSAGE(query);
    END IF;

    IF meme_ranks_debug = FALSE THEN
	return MEME_UTILITY.exec(query);
    END IF;

    RETURN 0;

END local_exec;

/* PROCEDURE MEME_RANKS_ERROR **************************************************
 */
PROCEDURE meme_ranks_error (
    	method		    IN VARCHAR2,
    	location	    IN VARCHAR2,
    	error_code	    IN INTEGER,
    	detail		    IN VARCHAR2
)
IS
    error_msg	    VARCHAR2(100);
BEGIN
    IF error_code = 1 THEN
	error_msg := 'MR0001: Unspecified error';
    ELSIF error_code = 10 THEN
	error_msg := 'MR0010: No Data Found';
    ELSIF error_code = 20 THEN
	error_msg := 'MR0020: Problem setting relationships rank';
    ELSIF error_code = 30 THEN
	error_msg := 'MR0030: Problem setting attributes rank';
    ELSIF error_code = 40 THEN
	error_msg := 'MR0040: Problem setting classes rank';
    ELSIF error_code = 50 THEN
	error_msg := 'MR0050: Illegal parameter value';
    ELSE
	error_msg := 'MR0000: Unknown Error';
    END IF;

    MEME_UTILITY.PUT_ERROR('Error in MEME_RANKS::'||method||' at '||
	location||' ('||error_msg||','||detail||')');

END meme_ranks_error;

/* PROCEDURE INITIALIZE_TRACE **************************************************
 * This method clears location, err_msg, method
 */
PROCEDURE initialize_trace ( method	IN VARCHAR2 )
IS
BEGIN
    location := '0';
    err_msg := '';
    meme_ranks.method := initialize_trace.method;
END initialize_trace;

/* FUNCTION GET_RANK ***********************************************************
 */
FUNCTION get_rank (
	row_id 		IN INTEGER,
  	table_name	IN VARCHAR2
) RETURN INTEGER
IS
BEGIN
    -- Allowable table_names are CLASSES, ATTRIBUTES, RELATIONSHIPS
    IF table_name = MEME_CONSTANTS.TN_CLASSES OR
       lower(table_name) = lower(MEME_CONSTANTS.LTN_CLASSES) THEN
	return rank_atom_as_number(row_id);
    ELSIF table_name = MEME_CONSTANTS.TN_FOREIGN_CLASSES OR
   	  lower(table_name) = 'foreign_classes' THEN
	return rank_atom_as_number(row_id,MEME_CONSTANTS.NO,MEME_CONSTANTS.YES);
    ELSIF table_name = MEME_CONSTANTS.TN_ATTRIBUTES OR
          lower(table_name) = lower(MEME_CONSTANTS.LTN_ATTRIBUTES) THEN
	return rank_attribute_as_number(row_id);
    ELSIF table_name = MEME_CONSTANTS.TN_RELATIONSHIPS OR
          lower(table_name) = lower(MEME_CONSTANTS.LTN_RELATIONSHIPS) THEN
	return rank_relationship_as_number(row_id);
    ELSIF table_name = MEME_CONSTANTS.TN_CONCEPT_STATUS OR
          lower(table_name) = lower(MEME_CONSTANTS.LTN_CONCEPT_STATUS) THEN
	return 0;
    ELSIF table_name = MEME_CONSTANTS.TN_SOURCE_CLASSES OR
          lower(table_name) = 'source_classes_atoms' THEN
	return rank_atom_as_number(row_id,MEME_CONSTANTS.YES);
    ELSIF table_name = MEME_CONSTANTS.TN_SOURCE_ATTRIBUTES OR
          lower(table_name) = 'source_attributes' THEN
	return rank_attribute_as_number(row_id,MEME_CONSTANTS.YES);
    ELSIF table_name = MEME_CONSTANTS.TN_SOURCE_RELATIONSHIPS OR
          lower(table_name) = 'source_relationships' THEN
	return rank_relationship_as_number(row_id,MEME_CONSTANTS.YES);
    ELSIF table_name = MEME_CONSTANTS.TN_SOURCE_CONCEPT_STATUS OR
          lower(table_name) = 'source_concept_status' THEN
	return 0;
    END IF;

END get_rank;

/* FUNCTION SET_RANK ***********************************************************
 */
FUNCTION set_rank (
	row_id 		IN INTEGER,
  	table_name	IN VARCHAR2,
	field_name	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
) RETURN INTEGER
IS
BEGIN
    -- Allowable table_names are CLASSES, ATTRIBUTES, RELATIONSHIPS
    IF table_name = MEME_CONSTANTS.TN_CLASSES OR
	upper(table_name) = MEME_CONSTANTS.LTN_CLASSES THEN
	return set_atom_rank(row_id,field_name);
    ELSIF table_name = MEME_CONSTANTS.TN_ATTRIBUTES OR
	upper(table_name) = MEME_CONSTANTS.LTN_ATTRIBUTES THEN
	return set_attribute_rank(row_id,field_name);
    ELSIF table_name = MEME_CONSTANTS.TN_RELATIONSHIPS OR
	upper(table_name) = MEME_CONSTANTS.LTN_RELATIONSHIPS THEN
	return set_relationship_rank(row_id,field_name);
    END IF;

    RETURN 0;

EXCEPTION

    WHEN OTHERS THEN
	RETURN -1;

END set_rank;

/* FUNCTION RANK_TO_NUMBER *****************************************************
 */
FUNCTION rank_to_number (
	rank	VARCHAR2
)
RETURN INTEGER
IS

BEGIN
    return to_number(substr(rank,0,length(rank)-MEME_CONSTANTS.ID_LENGTH));
END;

/* FUNCTION SET_PREFERRED_ID ***************************************************
 */
FUNCTION set_preferred_id (
	concept_id  IN INTEGER,
	changed_field IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
)
RETURN INTEGER
IS
    pref_atom_id	INTEGER;
BEGIN

    initialize_trace('set_preferred_id');

    IF affects_atom_rank(changed_field) = FALSE THEN
	RETURN 0;
    END IF;

    location := '10';
    pref_atom_id := calculate_preferred_atom (concept_id);

    location := '20';
    UPDATE concept_status
    SET preferred_atom_id = pref_atom_id
    WHERE concept_id = set_preferred_id.concept_id
      AND preferred_atom_id != pref_atom_id;

    RETURN 0;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	meme_ranks_error(method, location, 10, 'CS'||','||concept_id);
	RETURN -1;

    WHEN OTHERS THEN
	meme_ranks_error(method, location, 1,
		'CS'||','||concept_id||': '||SQLERRM);
	RETURN -1;

END set_preferred_id;

/* FUNCTION AFFECTS_ATOM_RANK **************************************************
 * This is not data-driven for performance reasons
 */
FUNCTION affects_atom_rank(
    field_name IN VARCHAR2
)
RETURN BOOLEAN
IS
    fn		VARCHAR2(50);
BEGIN
    fn := LOWER(field_name);
    -- As the rank algorithm changes, this should change
    IF	fn = 'tobereleased' OR
	fn = 'termgroup' OR
	fn = 'last_release_rank' OR
	fn = 'atom_id' OR
	fn = 'sui' OR
	fn = LOWER(MEME_CONSTANTS.NO_FIELDS_CHANGED)
    THEN
	RETURN TRUE;
    END IF;

    RETURN FALSE;

END affects_atom_rank;

/* FUNCTION RANK_ATOM_AS_NUMBER ************************************************
 */
FUNCTION rank_atom_as_number (
    atom_id		IN INTEGER,
    source_flag	  	IN VARCHAR2 := MEME_CONSTANTS.NO,
    foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
)
RETURN INTEGER
IS
    rank		VARCHAR2(256);
BEGIN
    -- strip off SUI and atom_id parts
    rank := rank_atom(atom_id,source_flag,foreign_flag);
    return to_number(substr(rank,0,length(rank)-(MEME_CONSTANTS.ID_LENGTH*2)));

END rank_atom_as_number;

/* FUNCTION RANK_ATOM **********************************************************
 * Function for ranking atoms
 * The rank is:  tobereleased termgroup_rank last_release_rank atom_id
 * Make sure this jives with optimized ranking for set_preference.
 */
FUNCTION rank_atom (
    atom_id		IN INTEGER,
    source_flag	  	IN VARCHAR2 := MEME_CONSTANTS.NO,
    foreign_flag  	IN VARCHAR2 := MEME_CONSTANTS.NO
)
RETURN VARCHAR2
IS
    atom_row	classes%ROWTYPE;
    arank	VARCHAR2(256);
    trank	INTEGER;
BEGIN

    IF source_flag = MEME_CONSTANTS.YES THEN
	-- Also look up SUI
    	SELECT tobereleased, termgroup, 0, atom_id, sui
	    into atom_row.tobereleased, atom_row.termgroup,
	 	 atom_row.last_release_rank, atom_row.atom_id, atom_row.sui
    	FROM source_classes_atoms WHERE atom_id = rank_atom.atom_id;

    ELSIF foreign_flag != MEME_CONSTANTS.NO THEN
	-- Also look up SUI
    	SELECT tobereleased, termgroup, 0, atom_id, sui
	    into atom_row.tobereleased, atom_row.termgroup,
	 	 atom_row.last_release_rank, atom_row.atom_id, atom_row.sui
    	FROM foreign_classes WHERE atom_id = rank_atom.atom_id;
    ELSE
	SELECT * into atom_row
    	FROM classes WHERE atom_id = rank_atom.atom_id;
    END IF;

    trank := get_tobereleased_rank(atom_row.tobereleased);

    IF trank = -1 THEN
	return MEME_CONSTANTS.EMPTY_RANK;
    ELSE
	arank := to_char(trank);
    END IF;

    SELECT nvl(max(rank),-1) INTO trank
    FROM termgroup_rank WHERE termgroup = atom_row.termgroup;
    IF trank = -1 THEN
	return MEME_CONSTANTS.EMPTY_RANK;
    ELSE
	arank := arank || LPAD(trank,4,'0');
    END IF;

    arank := arank || NVL(atom_row.last_release_rank,0);

    -- Use SUI
    arank := arank || LPAD(REPLACE(atom_row.sui,'S'),
			   MEME_CONSTANTS.ID_LENGTH,'0');

    arank := arank || LPAD(atom_row.atom_id,MEME_CONSTANTS.ID_LENGTH,'0');

    return arank;

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::RANK_ATOM: Error => ' || SQLERRM);
	RETURN MEME_CONSTANTS.EMPTY_RANK;

END rank_atom;

/* FUNCTION RANK_ATOM_FOR_RELEASE **********************************************
 * Function for ranking atoms
 * The rank is:  tobereleased termgroup_rank last_release_rank atom_id mm#
 * For MTH/MM atoms, slightly different
 */
FUNCTION rank_atom_for_release (
    atom_id		IN INTEGER,
    termgroup		IN VARCHAR2,
    foreign_flag	IN VARCHAR2 := MEME_CONSTANTS.NO
)
RETURN VARCHAR2
IS
    concept_id		INTEGER;
    bracket_number	INTEGER;
    stripped_string  	VARCHAR2(4000);
    arank		VARCHAR2(256);
    trank		INTEGER;
    mlrr		INTEGER;
BEGIN

    IF termgroup != 'MTH/MM' THEN
	return rank_atom(atom_id,'N',foreign_flag)||'00';
    END IF;

    SELECT concept_id,
	  MEME_UTILITY.strip_string(atom_name) as ss,
	  MEME_UTILITY.get_bracket_number(atom_name) as bn
	INTO rank_atom_for_release.concept_id, stripped_string, bracket_number
    FROM classes a, atoms b
    WHERE a.atom_id = rank_atom_for_release.atom_id
      AND b.atom_id = rank_atom_for_release.atom_id;

    SELECT NVL(max(rank_atom(a.atom_id)),MEME_CONSTANTS.EMPTY_RANK) into arank
	   --NVL(max(last_release_rank),0) into mlrr
    FROM classes a, atoms b
    WHERE a.atom_id = b.atom_id
      AND a.atom_id != rank_atom_for_release.atom_id
      AND LOWER(b.atom_name) =
	  LOWER(stripped_string)
      AND a.concept_id = rank_atom_for_release.concept_id
      -- Do NOT compare MMs to PNs
      AND termgroup != 'MTH/PN';

    IF arank != MEME_CONSTANTS.EMPTY_RANK THEN
	RETURN arank || LPAD(100-bracket_number,2,0);
    END IF;

    return arank;

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::RANK_ATOM: Error => ' || SQLERRM);
	RETURN MEME_CONSTANTS.EMPTY_RANK;

END rank_atom_for_release;

/* FUNCTION CALCULATE_PREFERRED_ATOM *******************************************
 * Function to get the preferred atom for a concept
 */
FUNCTION calculate_preferred_atom (
	concept_id IN INTEGER
)
RETURN INTEGER

IS

    TYPE curvar_type IS REF CURSOR;
    classes_cursor	curvar_type;
    preferred_atom_id	INTEGER;
    atom_id		INTEGER;
    max_rank		VARCHAR2(256);
    current_rank	VARCHAR2(256);

BEGIN

    max_rank := MEME_CONSTANTS.EMPTY_RANK;
    OPEN classes_cursor FOR select atom_id from classes where concept_id =
				calculate_preferred_atom.concept_id;
    LOOP
	FETCH classes_cursor INTO atom_id;
    	EXIT WHEN classes_cursor%NOTFOUND;

	current_rank := rank_atom(atom_id);
	IF current_rank > max_rank THEN
	    max_rank := current_rank;
	END IF;
    END LOOP; -- classes_cursor
    CLOSE classes_cursor;

    preferred_atom_id := to_number(substr(max_rank,length(max_rank)-(MEME_CONSTANTS.ID_LENGTH-1)));

    return NVL(preferred_atom_id,0);

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::CALCULATE_PREFERRED_ATOM: Error => ' || SQLERRM);
	RETURN -1;

END calculate_preferred_atom;

/* FUNCTION GET_PREFERRED_ATOM **********************************************
 * Function to get the preferred atom for a concept 
 * using the editing rank.
 */
FUNCTION get_preferred_atom (
	concept_id IN INTEGER
)
RETURN INTEGER

IS

    TYPE curvar_type IS REF CURSOR;
    classes_cursor	curvar_type;
    preferred_atom_id	INTEGER;
    atom_id		INTEGER;
    max_rank		VARCHAR2(256);
    current_rank	VARCHAR2(256);

BEGIN
    -- Add SUI to calculation
    SELECT 
      NVL(max(tbr.rank||lpad(tr.rank,5,0)||last_release_rank || 
	      sui || LPAD(atom_id,MEME_CONSTANTS.ID_LENGTH,'0')),
	  MEME_CONSTANTS.EMPTY_RANK) INTO max_rank
    FROM classes a, tobereleased_rank tbr, termgroup_rank tr
    WHERE concept_id = get_preferred_atom.concept_id
      AND a.tobereleased = tbr.tobereleased 
      AND a.termgroup = tr.termgroup;

    preferred_atom_id := to_number(substr(max_rank,length(max_rank)-(MEME_CONSTANTS.ID_LENGTH-1)));

    return NVL(preferred_atom_id,0);

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::GET_PREFERRED_ATOM: Error => ' || SQLERRM);
	RETURN -1;

END get_preferred_atom;

/* FUNCTION SET_ATOM_RANK ******************************************************
 */
FUNCTION set_atom_rank (
    atom_id		IN INTEGER,
    changed_field	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
)
RETURN INTEGER
IS
    arank	integer;
BEGIN

   initialize_trace ('set_atom_rank');

   IF affects_atom_rank(changed_field) = FALSE THEN
	RETURN 0;
   END IF;

   location := '10';
   arank := rank_atom_as_number(atom_id);

   location := '20';
   UPDATE classes SET rank = arank
   WHERE atom_id = set_atom_rank.atom_id;

   RETURN 0;
EXCEPTION

   WHEN NO_DATA_FOUND THEN
	meme_ranks_error(method, location, 10, 'C'||','||atom_id);
	RETURN -1;

   WHEN OTHERS THEN
	meme_ranks_error(method, location, 1,
		'C'||','||atom_id||': '||SQLERRM);
	RETURN -1;

END set_atom_rank;

/* FUNCTION AFFECTS_RELATIONSHIP_RANK ******************************************
 */
FUNCTION affects_relationship_rank(
    field_name IN VARCHAR2
)
RETURN BOOLEAN
IS
    fn		VARCHAR2(50);
BEGIN
    fn := LOWER(field_name);
    -- As the rank algorithm changes, this should change
    IF	fn = 'tobereleased' OR
	fn = 'status' OR
	fn = 'level' OR
	fn = 'source_of_label' OR
	fn = 'relationship_name' OR
	fn = 'relationship_id' OR
	fn = LOWER(MEME_CONSTANTS.NO_FIELDS_CHANGED)
    THEN
	RETURN TRUE;
    END IF;

    RETURN FALSE;

END affects_relationship_rank;

/* FUNCTION RANK_RELATIONSHIP_AS_NUMBER ****************************************
 */
FUNCTION rank_relationship_as_number (
    relationship_id		IN INTEGER,
    source_flag	  	IN VARCHAR2 := MEME_CONSTANTS.NO
)
RETURN INTEGER
IS
BEGIN
    return rank_to_number(rank_relationship(relationship_id,source_flag));
END rank_relationship_as_number;

/* FUNCTION RANK_RELATIONSHIPS *************************************************
 */
FUNCTION rank_relationship (
    relationship_id		IN INTEGER,
    source_flag	  	IN VARCHAR2 := MEME_CONSTANTS.NO

)

RETURN VARCHAR2

IS
     relationship_row	relationships%ROWTYPE;
     rrank  varchar2(30);
     trank  INTEGER;
BEGIN

-- ranking:  tobereleased (level status) relationship_name source_rank
--
-- Return relationship id if any invalid values occur.
--

--    MEME_UTILITY.PUT_MESSAGE('RANK_RELATIONSHIP::relationship_id ' || relationship_id);

    IF source_flag = MEME_CONSTANTS.TN_SOURCE_CXT_RELATIONSHIPS THEN
      	SELECT relationship_level, status, relationship_name,
	    source_of_label, relationship_id, tobereleased
	  INTO
	 	relationship_row.relationship_level,
	 	relationship_row.status,
	 	relationship_row.relationship_name,
	 	relationship_row.source_of_label,
	 	relationship_row.relationship_id,
	 	relationship_row.tobereleased
    	FROM source_context_relationships
	WHERE relationship_id = rank_relationship.relationship_id;
    ELSIF source_flag = MEME_CONSTANTS.YES THEN
      	SELECT relationship_level, status, relationship_name,
	    source_of_label, relationship_id, tobereleased
	  INTO
	 	relationship_row.relationship_level,
	 	relationship_row.status,
	 	relationship_row.relationship_name,
	 	relationship_row.source_of_label,
	 	relationship_row.relationship_id,
	 	relationship_row.tobereleased
    	FROM source_relationships
	WHERE relationship_id = rank_relationship.relationship_id;
    ELSE
      	SELECT * into relationship_row
    	FROM relationships
	WHERE relationship_id = rank_relationship.relationship_id;
    END IF;

    trank := get_tobereleased_rank(relationship_row.tobereleased);

    IF trank = -1 THEN
	return MEME_CONSTANTS.EMPTY_RANK;
    ELSE
	rrank := to_char(trank);
    END IF;

    trank := get_level_status_rank('R',
		   	relationship_row.relationship_level,
			relationship_row.status );
    IF trank = -1 THEN
	return MEME_CONSTANTS.EMPTY_RANK;
    ELSE
	rrank := rrank || LPAD(trank,2,'0');
    END IF;

    trank := get_relationship_name_rank(
			relationship_row.relationship_name);
    IF trank = -1 THEN
	return MEME_CONSTANTS.EMPTY_RANK;
    ELSE
	rrank := rrank || LPAD(trank,2,'0');
    END IF;

    -- Since only one concept level relationship can exist
    -- between a pair of rels and concept level rels do not
    -- necessarily have valid SABs, ignore source_authority rank.

    IF relationship_row.relationship_level != MEME_CONSTANTS.CONCEPT_LEVEL THEN
	trank := get_source_authority_rank(
		relationship_row.source_of_label,
		relationship_row.source_of_label);
	IF trank = -1 THEN
	    -- If source doesn't exist return 0
--	    rrank := rrank || '0000';
	    return MEME_CONSTANTS.EMPTY_RANK;
	ELSE
	    rrank := rrank || LPAD(trank,4,'0');
	END IF;

    ELSE

	rrank := rrank || MEME_CONSTANTS.MTH_RANK;

    END IF;

    rrank := rrank || LPAD(relationship_row.relationship_id,MEME_CONSTANTS.ID_LENGTH,'0');

    return rrank;

EXCEPTION
   WHEN OTHERS THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::RANK_RELATIONSHIP: Error => ' || SQLERRM);
	RETURN -1;

END rank_relationship;


/* FUNCTION GET_PREFERRED_RELATIONSHIPS ****************************************
 * Function to get the preferred atom for a concept
 */
FUNCTION get_preferred_relationship (
	concept_id_1 IN INTEGER,
	concept_id_2 IN INTEGER
)
RETURN integer

IS

    preferred_rel_id	integer;
    CURSOR relationships_cursor IS
	SELECT relationship_id FROM relationships
	WHERE concept_id_1 = get_preferred_relationship.concept_id_1
	  AND concept_id_2 = get_preferred_relationship.concept_id_2
	UNION SELECT relationship_id FROM relationships
	WHERE concept_id_1 = get_preferred_relationship.concept_id_2
	  AND concept_id_2 = get_preferred_relationship.concept_id_1;

    relationship_id	INTEGER;
    max_rank		VARCHAR2(256);
    current_rank	VARCHAR2(256);

BEGIN

    initialize_trace('get_preferred_relationship');

    location := '0';
    OPEN relationships_cursor;
    max_rank := MEME_CONSTANTS.EMPTY_RANK;
    LOOP
	location := '10';
	FETCH relationships_cursor INTO relationship_id;
	EXIT WHEN relationships_cursor%NOTFOUND;

	location := '20';
	err_msg := 'Error ranking relationship (' || relationship_id || ')';
	current_rank := rank_relationship(relationship_id);
	err_msg := '';
	IF current_rank > max_rank THEN
	    max_rank := current_rank;
	END IF;
    END LOOP; -- relationships_cursor
    location := '30';
    CLOSE relationships_cursor;

    preferred_rel_id := to_number(substr(max_rank,length(max_rank)-(MEME_CONSTANTS.ID_LENGTH-1)));

    return  NVL(preferred_rel_id,0);

END get_preferred_relationship;

/* FUNCTION SET_RELATIONSHIP_RANK **********************************************
 */
FUNCTION set_relationship_rank (
    relationship_id	IN INTEGER,
    changed_field	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
)
RETURN INTEGER
IS
    rrank		varchar2(50);
BEGIN

   initialize_trace('set_relationship_rank');

   location := '5';
   IF affects_relationship_rank(changed_field) = FALSE THEN
 	RETURN 0;
   END IF;

   location := '10';
   rrank := rank_relationship(relationship_id);

   location := '20';
   UPDATE relationships SET rank = rank_to_number(rrank)
   WHERE relationship_id = set_relationship_rank.relationship_id;

   RETURN 0;

EXCEPTION

   WHEN NO_DATA_FOUND THEN
	meme_ranks_error('set_relationship_rank',location,10,relationship_id);
	RETURN -1;

   WHEN OTHERS THEN
	meme_ranks_error('set_relationship_rank',location,1,
			 relationship_id || ', ' || SQLERRM);
	RETURN -1;

END set_relationship_rank;

/* FUNCTION AFFECTS_ATTRIBUTE_RANK *********************************************
 */
FUNCTION affects_attribute_rank(
    field_name IN VARCHAR2
)
RETURN BOOLEAN
IS
    fn		VARCHAR2(50);
BEGIN
    fn := LOWER(field_name);
    -- As the rank algorithm changes, this should change
    IF	fn = 'tobereleased' OR
	fn = 'source' OR
	fn = 'attribute_id' OR
	fn = LOWER(MEME_CONSTANTS.NO_FIELDS_CHANGED)
    THEN
	RETURN TRUE;
    END IF;

    RETURN FALSE;

END affects_attribute_rank;

/* FUNCTION RANK_ATTRIBUTE_AS_NUMBER *******************************************
 */
FUNCTION rank_attribute_as_number (
    attribute_id		IN INTEGER,
    source_flag	  	IN VARCHAR2 := MEME_CONSTANTS.NO
)
RETURN INTEGER
IS
BEGIN

    return rank_to_number(rank_attribute(attribute_id,source_flag));

END rank_attribute_as_number;

/* FUNCTION RANK_ATTRIBUTE *****************************************************
 */
FUNCTION rank_attribute (
	attribute_id	IN INTEGER,
    source_flag	  	IN VARCHAR2 := MEME_CONSTANTS.NO
)

RETURN VARCHAR2

IS
    attribute_row	attributes%ROWTYPE;
    trank  		integer;
    arank  		varchar2(30);
BEGIN

    IF source_flag = MEME_CONSTANTS.YES THEN
    	SELECT tobereleased, source, attribute_id
		into attribute_row.tobereleased,
		     attribute_row.source,
		     attribute_row.attribute_id
    	FROM source_attributes WHERE attribute_id = rank_attribute.attribute_id;
    ELSE
    	SELECT * into attribute_row
    	FROM attributes WHERE attribute_id = rank_attribute.attribute_id;
    END IF;

    trank := get_tobereleased_rank(attribute_row.tobereleased);

    IF trank = -1 THEN
	return MEME_CONSTANTS.EMPTY_RANK;
    ELSE
	arank := arank || to_char(trank);
    END IF;

    trank := get_source_authority_rank(
		attribute_row.source,
		attribute_row.source);
    IF trank = -1 THEN
	return MEME_CONSTANTS.EMPTY_RANK;
    ELSE
	arank := arank || LPAD(trank,4,'0');
    END IF;

    arank := arank || LPAD(attribute_row.attribute_id,MEME_CONSTANTS.ID_LENGTH,'0');

    RETURN arank;

EXCEPTION
   WHEN OTHERS THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::RANK_ATTRIBUTE: Error => ' || SQLERRM);
	RETURN -1;

END rank_attribute;

/* FUNCTION SET_ATTRIBUTE_RANK *************************************************
 */
FUNCTION set_attribute_rank (
    attribute_id	IN INTEGER,
    changed_field	IN VARCHAR2 := MEME_CONSTANTS.NO_FIELDS_CHANGED
)
RETURN INTEGER
IS
    arank		varchar2(50);
BEGIN

   initialize_trace('set_attribute_rank');

   IF affects_attribute_rank(changed_field) = FALSE THEN
	RETURN 0;
   END IF;

   location := '10';
   arank := rank_attribute(attribute_id);

   location := '10';
   UPDATE attributes SET rank =  rank_to_number(arank)
   WHERE attribute_id = set_attribute_rank.attribute_id;

   RETURN 0;

EXCEPTION
   WHEN NO_DATA_FOUND THEN
	meme_ranks_error('set_attribute_rank',location,10,attribute_id);
	RETURN -1;

   WHEN OTHERS THEN
	meme_ranks_error('set_attribute_rank',location,1,
			 attribute_id || ',' || SQLERRM);
	RETURN -1;

END set_attribute_rank;

/* FUNCTION GET_LEVEL_RANK *****************************************************
 */
FUNCTION get_level_rank (
	table_name IN VARCHAR2,
	level IN VARCHAR2
)
RETURN INTEGER
IS
	t_rank		INTEGER;
BEGIN
    SELECT max(rank) INTO t_rank
    FROM level_status_rank
    WHERE level_value = get_level_rank.level
    AND table_name = get_level_rank.table_name;

    IF t_rank is null THEN
	return -1;
    END IF;

    RETURN t_rank;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_level_rank: level not found.');
	RETURN -1;

END get_level_rank;

/* FUNCTION GET_STATUS_RANK ****************************************************
 */
FUNCTION get_status_rank (
	table_name IN VARCHAR2,
	status IN VARCHAR2
)
RETURN INTEGER
IS
	t_rank		INTEGER;
BEGIN
    SELECT max(rank) INTO t_rank
    FROM level_status_rank
    WHERE status = get_status_rank.status
    AND table_name = get_status_rank.table_name;

    IF t_rank is null THEN
	return -1;
    END IF;

    RETURN t_rank;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_status_rank: status not found.');
	RETURN -1;

END get_status_rank;

/* FUNCTION GET_LEVEL_STATUS_RANK **********************************************
 */
FUNCTION get_level_status_rank (
	table_name IN VARCHAR2,
	level IN VARCHAR2,
	status IN VARCHAR2
)
RETURN INTEGER
IS
	t_rank		INTEGER;
BEGIN
    SELECT rank INTO t_rank
    FROM level_status_rank
    WHERE status =  get_level_status_rank.status
    AND table_name = get_level_status_rank.table_name
    AND level_value = get_level_status_rank.level;

    RETURN t_rank;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_level_status_rank: level,status not found.');
	RETURN -1;

END get_level_status_rank;


/* FUNCTION GET_RELEASED_RANK **************************************************
 */
FUNCTION get_released_rank (
	released IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank		INTEGER;
BEGIN
	SELECT rank INTO t_rank
	FROM released_rank
	WHERE released = get_released_rank.released;

	RETURN NVL(t_rank,-1);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_released_rank: released not found.');
	RETURN -1;

END get_released_rank;

/* FUNCTION GET_TOBERELEASED_RANK **********************************************
 */
FUNCTION get_tobereleased_rank (
	tobereleased IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank		INTEGER;
BEGIN
	SELECT rank INTO t_rank
	FROM tobereleased_rank
	WHERE tobereleased = get_tobereleased_rank.tobereleased;

	RETURN NVL(t_rank,-1);

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_tobereleased_rank: tobereleased not found.');
	RETURN -1;

END get_tobereleased_rank;

/* FUNCTION GET_GENERATED_RANK *************************************************
 */
FUNCTION get_generated_rank (
	generated IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank		INTEGER;
BEGIN
	IF generated = MEME_CONSTANTS.YES THEN
	    RETURN 1;
   	ELSIF generated = MEME_CONSTANTS.NO THEN
	    RETURN 1;
	ELSE
	    RETURN -1;
	END IF;
EXCEPTION
    WHEN OTHERS THEN
	RAISE MEME_RANKS_EXCEPTION;

END get_generated_rank;

/* FUNCTION GET_SUPPRESSIBLE_RANK **********************************************
 */
FUNCTION get_suppressible_rank (
	suppressible IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank		INTEGER;
BEGIN
	SELECT rank INTO t_rank
	FROM suppressible_rank
	WHERE NVL(suppressible,'N') = NVL(get_suppressible_rank.suppressible,'N');

	RETURN NVL(t_rank,-1);

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_suppressible_rank: suppressible not found.');
	RETURN -1;

END get_suppressible_rank;

/* FUNCTION GET_AUTHORITY_RANK *************************************************
 */
FUNCTION get_authority_rank (
	authority IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank		INTEGER;
BEGIN
	RETURN 999;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_authority_rank: authority not found.');
	RETURN -1;

END get_authority_rank;

/* FUNCTION GET_SOURCE_RANK ****************************************************
 */
FUNCTION get_source_rank (
	source IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank		INTEGER;
BEGIN
	IF source like MEME_CONSTANTS.EDITOR_PREFIX OR
	   source like MEME_CONSTANTS.STAMPING_PREFIX OR
	   source like MEME_CONSTANTS.LEXICAL_PREFIX OR
	   source like MEME_CONSTANTS.ENG_PREFIX OR
	   source like MEME_CONSTANTS.PIR_PREFIX
	THEN
	    return MEME_CONSTANTS.MTH_RANK;
	END IF;

	SELECT rank INTO t_rank
	FROM source_rank
	WHERE source = get_source_rank.source;

	RETURN t_rank;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_source_rank: source not found.');
	RETURN -1;

END get_source_rank;

/* FUNCTION GET_SOURCE_AUTHORITY_RANK ******************************************
 */
FUNCTION get_source_authority_rank (
	source IN VARCHAR2,
	authority IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank		INTEGER;
BEGIN
    IF	source like MEME_CONSTANTS.EDITOR_PREFIX OR
	source like MEME_CONSTANTS.STAMPING_PREFIX OR
	source like MEME_CONSTANTS.LEXICAL_PREFIX OR
	source like MEME_CONSTANTS.ENG_PREFIX OR
	source like MEME_CONSTANTS.PIR_PREFIX THEN

 	IF authority = source THEN
	-- MEME_UTILITY.PUT_MESSAGE('GET_SOURCE_AUTHORITY_RANK::Returning MEME_CONSTANTS.MTH_RANK: ' ||MEME_CONSTANTS.MTH_RANK);
	    return MEME_CONSTANTS.MTH_RANK;
	END IF;
    END IF;

    SELECT rank INTO t_rank FROM source_rank
    WHERE source = get_source_authority_rank.source;

    RETURN t_rank;

EXCEPTION
    WHEN OTHERS THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_source_authority_rank: source_authority not found.');
	RETURN -1;

END get_source_authority_rank;

/* FUNCTION GET_TERMGROUP_RANK *************************************************
 * RETURNS release termgroup rank
 */
FUNCTION get_termgroup_rank (
	termgroup IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank	INTEGER;
BEGIN
    SELECT release_rank INTO t_rank
    FROM termgroup_rank
    WHERE termgroup = get_termgroup_rank.termgroup;

    RETURN NVL(t_rank,-1);

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_termgroup_rank: termgroup not found.');
	RETURN -1;

END get_termgroup_rank;

/* FUNCTION GET_RELATIONSHIP_NAME_RANK *****************************************
 */
FUNCTION get_relationship_name_rank (
	relationship_name IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank	INTEGER;
BEGIN
    SELECT rank INTO t_rank
    FROM inverse_relationships
    WHERE relationship_name = get_relationship_name_rank.relationship_name;

    RETURN NVL(t_rank,-1);

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_relationship_name_rank: relationship_name not found.');
	RETURN -1;

END get_relationship_name_rank;


/* FUNCTION GET_REL_ATTRIBUTE_RANK *********************************************
 */
FUNCTION get_rel_attribute_rank (
	relationship_attribute IN VARCHAR2
)
RETURN INTEGER
IS
    t_rank	INTEGER;
BEGIN
    IF relationship_attribute is null OR relationship_attribute = '' THEN

    	SELECT rank INTO t_rank
    	FROM inverse_rel_attributes
    	WHERE relationship_attribute IS NULL;

    ELSE

    	SELECT rank INTO t_rank
    	FROM inverse_rel_attributes
    	WHERE relationship_attribute = get_rel_attribute_rank.relationship_attribute;

    END IF;
    RETURN NVL(t_rank,-1);

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	MEME_UTILITY.PUT_MESSAGE('MEME_RANKS::get_rel_attribute_rank: relationship_attribute not found.');
	RETURN -1;

END get_rel_attribute_rank;


/* PROCEDURE SET_RANKS *******************************************************
 * Sets atom ranks based on release ranks.
 */
PROCEDURE set_ranks (
	classes_flag 		VARCHAR2 := MEME_CONSTANTS.YES,
	relationships_flag 	VARCHAR2 := MEME_CONSTANTS.NO,
	attributes_flag 	VARCHAR2 := MEME_CONSTANTS.NO,
	source_processing_flag 	VARCHAR2 := MEME_CONSTANTS.NO,
	work_id	 		INTEGER := 0
)
IS
    ct	       		INTEGER;
    start_time 		INTEGER;
    end_time   		INTEGER;
    local_work_id	INTEGER;
BEGIN

     initialize_trace('set_ranks');
     MEME_UTILITY.sub_timing_start;
     location := '0';

     --
     -- Get work id
     --
     err_msg := 'Error getting new work_id'; 
     IF work_id = 0 THEN
         local_work_id := MEME_UTILITY.new_work (
		authority => MEME_CONSTANTS.SYSTEM_AUTHORITY,
		type => 'INITIALIZE', 
	        description => 'MEME_RANKS::set ranks (' ||
		source_processing_flag || ' ' ||
		classes_flag || ' ' ||
		relationships_flag || ' ' ||
		attributes_flag || ')'
	);
    ELSE
	local_work_id := work_id;
    END IF;

    IF classes_flag = MEME_CONSTANTS.YES THEN

	-- Clear count variables
    	ct := 0;

	-- Start timing
	MEME_UTILITY.timing_start;


    	-- Remember: Any time this ranking code is changed,
    	-- 		 The code for rank_atom must be changed;

    	location := '10';
	err_msg := 'Error opening classes cursor';

	UPDATE classes c
	SET (termgroup_rank, rank) =
	  (SELECT tr.release_rank,
	       to_number(tbr.rank || LPAD(tr.release_rank,4,'0') ||
			NVL(last_release_rank,0))
	   FROM termgroup_rank tr, tobereleased_rank tbr
	   WHERE c.termgroup = tr.termgroup
	     AND c.tobereleased = tbr.tobereleased)
  	WHERE (atom_id,rank) in
	 (SELECT /*+ parallel(c) */ atom_id, c.rank
	   FROM classes c, termgroup_rank tr, tobereleased_rank tbr
	   WHERE c.termgroup = tr.termgroup
	     AND c.tobereleased = tbr.tobereleased
	     AND c.rank != to_number(tbr.rank || LPAD(tr.release_rank,4,'0') ||
				NVL(last_release_rank,0))
         );

	ct := SQL%ROWCOUNT;

	COMMIT;

    	MEME_UTILITY.PUT_MESSAGE('Rows updated: ' || ct);
    	MEME_UTILITY.PUT_MESSAGE('Elapsed time: '||MEME_UTILITY.elapsed_time);

	location := '50';
	err_msg := 'Error logging operation';
	MEME_UTILITY.sub_timing_stop;
    	MEME_UTILITY.log_operation(
	   authority => MEME_CONSTANTS.SYSTEM_AUTHORITY,
	   activity => 'Set core table ranks',
	   detail => 'Set classes rank (' || ct || ' rows).',
	   transaction_id => 0,
	   work_id => local_work_id,
	   elapsed_time => MEME_UTILITY.sub_elapsed_time);
	MEME_UTILITY.sub_timing_start;

	location := '60';
	err_msg := 'Error clearing meme_progress';
	MEME_UTILITY.reset_progress (local_work_id);

    END IF;

    IF relationships_flag = MEME_CONSTANTS.YES THEN

    	-- Remember: Any time this ranking code is changed,
    	-- 		 The code for rank_relationship must be changed;
    	-- Only recompute for those where source_rank changed.

	-- Reset counts
    	ct := 0;

	-- Start timing
    	MEME_UTILITY.timing_start;

	-- First get SOURCE_LEVEL and PROCESSED_LEVEL rels
    	location := '70';
	err_msg := 'Error opening relationships (1) cursor';
	UPDATE /*+ parallel(r) */ relationships r
	SET (source_rank, rank) =
	  (SELECT sr.rank,
	       to_number(tbr.rank || LPAD(lsr.rank,2,'0') ||
			LPAD(ir.rank,2,'0') || LPAD(sr.rank,2,'0'))
 	   FROM tobereleased_rank tbr, level_status_rank lsr,
	 	inverse_relationships ir, source_rank sr
	   WHERE r.source = sr.source
	     AND r.tobereleased = tbr.tobereleased
	     AND r.relationship_level = lsr.level_value
	     AND r.status = lsr.status
	     AND lsr.table_name = 'R'
	     AND r.relationship_name = ir.relationship_name)
	WHERE r.relationship_level = 'S'
          AND (source, source_rank) IN
	 (SELECT /*+ parallel(a) */ a.source, source_rank 
	  FROM relationships a, source_rank b WHERE a.source = b.source
	  MINUS SELECT source, rank FROM source_rank);


	ct := SQL%ROWCOUNT;

	COMMIT;

    	MEME_UTILITY.timing_stop;
    	MEME_UTILITY.PUT_MESSAGE('Rows updated: ' || ct || '.');
    	MEME_UTILITY.PUT_MESSAGE('Elapsed time: ' ||
				 MEME_UTILITY.elapsed_time );

   	location := '110';
	err_msg := 'Error logging operation';
	MEME_UTILITY.sub_timing_stop;
    	MEME_UTILITY.log_operation(
	   authority => MEME_CONSTANTS.SYSTEM_AUTHORITY,
	   activity => 'Set core table ranks',
	   detail => 'Set relationships rank (' || ct || ' rows).',
	   transaction_id => 0,
	   work_id => local_work_id,
	   elapsed_time => MEME_UTILITY.sub_elapsed_time);
	MEME_UTILITY.sub_timing_start;

   	location := '120';
	err_msg := 'Error clearing meme_progress';
	MEME_UTILITY.reset_progress (local_work_id);

	-- Reset counters
    	ct := 0;

	-- Start timing
    	MEME_UTILITY.timing_start;

	-- Get CONCEPT_LEVEL rels
    	location := '130';
	err_msg := 'Error opening relationships (2) cursor';

	UPDATE /*+ parallel(r) */ relationships r
	SET (source_rank, rank) =
	  (SELECT MEME_CONSTANTS.MTH_RANK,
	       to_number(tbr.rank || LPAD(lsr.rank,2,'0') ||
			LPAD(ir.rank,2,'0') || MEME_CONSTANTS.MTH_RANK)
 	   FROM tobereleased_rank tbr, level_status_rank lsr,
	 	inverse_relationships ir
	   WHERE r.tobereleased = tbr.tobereleased
	      and r.relationship_level = lsr.level_value
	      and r.status = lsr.status
	      and lsr.table_name = 'R'
	      and r.relationship_name = ir.relationship_name)
	WHERE (r.relationship_level = 'C' OR
	       r.source like MEME_CONSTANTS.EDITOR_PREFIX)
	  AND source_rank != MEME_CONSTANTS.MTH_RANK;

	ct := SQL%ROWCOUNT;

	COMMIT;

	MEME_UTILITY.timing_stop;
    	MEME_UTILITY.PUT_MESSAGE('Rows updated: ' || ct || '.');
    	MEME_UTILITY.PUT_MESSAGE('Elapsed time: ' ||
				 MEME_UTILITY.elapsed_time );

   	location := '170';
	err_msg := 'Error logging operation';
	MEME_UTILITY.sub_timing_stop;
    	MEME_UTILITY.log_operation(
	   authority => MEME_CONSTANTS.SYSTEM_AUTHORITY,
	   activity => 'Set core table ranks',
	   detail => 'Set C level relationships rank (' || ct || ' rows).',
	   transaction_id => 0,
	   work_id => local_work_id,
	   elapsed_time => MEME_UTILITY.sub_elapsed_time);
	MEME_UTILITY.sub_timing_start;

   	location := '180';
	err_msg := 'Error clearing meme_progress';
	MEME_UTILITY.reset_progress (local_work_id);

    END IF;

    IF attributes_flag = MEME_CONSTANTS.YES THEN

    	-- Remember: Any time this ranking code is changed,
    	-- 		 The code for rank_attribute must be changed;
    	-- Only recompute for those where source_rank changed.

	-- Reset counters
    	ct := 0;

	-- Start timing
	MEME_UTILITY.timing_start;

	-- All SOURCE_LEVEL attributes should have sources in source_rank
  	-- CONCEPT_LEVEL attributes dont need a source rank, use MEME_CONSTANTS.MTH_RANK.
    	location := '190';
	err_msg := 'Error opening attributes cursor';

	UPDATE /*+ parallel(a) */ attributes a
	SET (source_rank, rank) =
	  (SELECT sr.rank,
	       to_number(tbr.rank || LPAD(sr.rank,4,'0'))
	   FROM source_rank sr,
		 tobereleased_rank tbr
	   WHERE a.source=sr.source
   	     AND a.tobereleased = tbr.tobereleased)
	WHERE attribute_level = 'S'
          AND (source, source_rank) IN
	 (SELECT /*+ parallel(att) */ att.source, source_rank 
	  FROM attributes att, source_rank b WHERE a.source = b.source
	  MINUS SELECT source, rank FROM source_rank);

	ct := SQL%ROWCOUNT;

	COMMIT;

	UPDATE /*+ parallel(a) */ attributes a
	SET (source_rank, rank) =
	  (SELECT MEME_CONSTANTS.MTH_RANK,
	       to_number(tbr.rank || MEME_CONSTANTS.MTH_RANK)
	   FROM source_rank sr,
		 tobereleased_rank tbr
	   WHERE a.source=sr.source
   	     AND a.tobereleased = tbr.tobereleased)
	WHERE attribute_level = 'C'
	  AND source_rank != MEME_CONSTANTS.MTH_RANK;

	ct := ct +  SQL%ROWCOUNT;

	COMMIT;

	MEME_UTILITY.timing_stop;
    	MEME_UTILITY.PUT_MESSAGE('Rows updated: ' || ct || '.' );
    	MEME_UTILITY.PUT_MESSAGE('Elapsed time: ' ||
				 MEME_UTILITY.elapsed_time );

	location := '230';
	err_msg := 'Error logging operation';
	MEME_UTILITY.sub_timing_stop;
    	MEME_UTILITY.log_operation(
	   authority => MEME_CONSTANTS.SYSTEM_AUTHORITY,
	   activity => 'Set core table ranks',
	   detail => 'Set attributes rank (' || ct || ' rows).',
	   transaction_id => 0,
	   work_id => local_work_id,
	   elapsed_time => MEME_UTILITY.sub_elapsed_time);

	location := '240';
	err_msg := 'Error clearing meme_progress';
	MEME_UTILITY.reset_progress (local_work_id);

    END IF;

EXCEPTION

    WHEN OTHERS THEN
	meme_ranks_error('set_ranks',location,1,err_msg || ': ' || SQLERRM);
	RAISE meme_ranks_exception;

END set_ranks;

/* PROCEDURE SET_PREFERENCE **************************************************
 * Sets preferred_atom_id fields based on editing ranks
 */
PROCEDURE set_preference (
    work_id		INTEGER := 0
)
IS
    TYPE curvar_type IS REF CURSOR;

    curvar		curvar_type;
    cs_row		concept_status%ROWTYPE;
    new_cs_row		concept_status%ROWTYPE;
    concept_status_row	concept_status%ROWTYPE;
    arank		VARCHAR2(50);
    ct	       		INTEGER;
    ct2        		INTEGER;
    start_time 		INTEGER;
    end_time   		INTEGER;
    retval   		INTEGER;
    local_work_id	INTEGER;
    atom_id   		INTEGER;
    concept_id		INTEGER;
    t1			VARCHAR2(256);
    t2			VARCHAR2(256);
    query		VARCHAR2(1024);

BEGIN

    initialize_trace('set_preference');
    MEME_UTILITY.sub_timing_start;

    --
    -- Set work id
    --
    location := '0';
    err_msg := 'Error getting new work_id';
    IF work_id = 0 THEN
        local_work_id := MEME_UTILITY.new_work (
		authority => MEME_CONSTANTS.SYSTEM_AUTHORITY,
		type => 'INITIALIZE', 
	 	description => 'MEME_RANKS::set preference'
	);
    ELSE
	local_work_id := work_id;
    END IF;

    -- Start timing
    MEME_UTILITY.timing_start;

    -- Get tablenames
    location := '10';
    err_msg := 'Error getting unique tablename';
    t1 := MEME_UTILITY.get_unique_tablename;

    location := '20';
    t2 := MEME_UTILITY.get_unique_tablename;

    location := '30.1';
    MEME_UTILITY.drop_it('table',t1);
    location := '30.2';
    err_msg := 'SQL error';
    query :=
	   'CREATE TABLE ' || t1 || ' AS 
    	    SELECT /*+ PARALLEL(c) */
		   max(tbr.rank || LPAD(tr.rank,4,0) || c.last_release_rank ||
                       sui || LPAD(atom_id,10,0))
                   AS max_rank,
		   concept_id 
  	    FROM classes c, termgroup_rank tr, tobereleased_rank tbr
            WHERE c.termgroup = tr.termgroup
                AND c.tobereleased=tbr.tobereleased 
            GROUP BY concept_id';

    local_exec ( query );

    location := '40.1';
    MEME_UTILITY.drop_it('table',t2);
    location := '40.2';
    query :=
	    'CREATE TABLE ' || t2 || ' AS
    	     SELECT
		 to_number(substr(max_rank,length(max_rank)-
	    	                     (' || MEME_CONSTANTS.ID_LENGTH || '-1)))
	    	 as preferred_atom_id, a.concept_id 
	     FROM concept_status a, ' || t1 || ' b
  	     WHERE a.concept_id=b.concept_id
	       AND a.preferred_atom_id !=
	             to_number(substr(max_rank,length(max_rank)-
			(' || MEME_CONSTANTS.ID_LENGTH || '-1)))';

    local_exec ( query );

    location := '50';
    ct := 0;
    OPEN curvar FOR 'SELECT preferred_atom_id, concept_id FROM ' || t2;
    LOOP
    	location := '60';
	FETCH curvar INTO atom_id,concept_id;
	EXIT WHEN curvar%NOTFOUND;

	location := '70';
	UPDATE concept_status
	SET preferred_atom_id = set_preference.atom_id
	WHERE concept_id = set_preference.concept_id;

 	ct := ct + 1;

	location := '80';
	IF MOD(ct,10000) = 0 THEN
	    COMMIT;
	END IF;
    END LOOP;

    -- Cleanup temporary tables
    location := '90';
    err_msg := 'Error cleaning up temporary tables';
    MEME_UTILITY.drop_it ( 'table', t1 );

    location := '100';
    MEME_UTILITY.drop_it ( 'table', t2 );

    -- Stop timing
    MEME_UTILITY.timing_stop;
    MEME_UTILITY.PUT_MESSAGE('Rows Updated: ' || ct || '.');
    MEME_UTILITY.PUT_MESSAGE('Elapsed time: ' ||
			     MEME_UTILITY.elapsed_time);

    location := '80';
    err_msg := 'Error logging operation';
    MEME_UTILITY.sub_timing_stop;
    MEME_UTILITY.log_operation(
	authority =>  MEME_CONSTANTS.SYSTEM_AUTHORITY,
	activity => 'Set concept preferred names',
	detail => 'Done setting concept preferred names (' || ct || ' rows)',
	transaction_id => 0,
	work_id => local_work_id, 
	elapsed_time => MEME_UTILITY.sub_elapsed_time );

    COMMIT;

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.drop_it('table',t1);
	MEME_UTILITY.drop_it('table',t2);
	meme_ranks_error('set_preference',location,1,err_msg || ': ' || SQLERRM);
 	err_msg := '';
	RAISE meme_ranks_exception;

END set_preference;

/* FUNCTION PFLAG_C *********************************************************
 * This function will return 'C' if atom_id is the preferred concept, 
 * otherwise it will return 'X'.
 */
FUNCTION pflag_c(
   atom_id    IN INTEGER,
   concept_id IN INTEGER,
   foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
)
RETURN VARCHAR2
IS
   t_rank     		VARCHAR2(50);
   t_bn			INTEGER;
   t_termgroup     	VARCHAR2(40);
BEGIN

   -- foreign is a bit different
   IF foreign_flag != MEME_CONSTANTS.NO THEN

       SELECT MAX(rank_atom_for_release(a.atom_id, 'DUMMY', 
			a.language)) INTO t_rank
       FROM foreign_classes a, classes b
       WHERE a.eng_atom_id=b.atom_id
       AND b.concept_id = pflag_c.concept_id
       AND a.tobereleased in ('Y','y')
       AND b.tobereleased in ('Y','y')
       AND a.language = foreign_flag;

       IF t_rank = rank_atom_for_release(
			pflag_c.atom_id, 'DUMMY', foreign_flag ) THEN
          RETURN 'C';
       ELSE
	  RETURN 'X';
       END IF;
   END IF;

   /* Only releasable atoms should be considered */
   SELECT MAX(rank_atom_for_release(atom_id, termgroup)) INTO t_rank
   FROM classes WHERE concept_id = pflag_c.concept_id
    AND tobereleased in ('Y','y');

   SELECT termgroup INTO t_termgroup
   FROM classes WHERE atom_id = pflag_c.atom_id;

   IF t_rank = rank_atom_for_release(pflag_c.atom_id, t_termgroup) THEN
      RETURN 'C';
   END IF;

   RETURN 'X';

END pflag_c;

/* FUNCTION PFLAG_L ************************************************************
 * This function will return 'L' if atom_id is the highest rank term,
 * otherwise it will return 'X'.
 */
FUNCTION pflag_l(
   atom_id    IN INTEGER,
   concept_id IN INTEGER,
   foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
)
RETURN VARCHAR2
IS
   t_lui      		VARCHAR2(10);
   t_rank     		VARCHAR2(50);
   t_bn			INTEGER;
   t_termgroup     	VARCHAR2(40);
BEGIN

   -- foreign is a bit different
   IF foreign_flag != MEME_CONSTANTS.NO THEN

       SELECT MAX(rank_atom_for_release(a.atom_id, 'DUMMY', 
			a.language)) INTO t_rank
       FROM foreign_classes a, classes b, foreign_classes c
       WHERE a.eng_atom_id=b.atom_id
       AND b.concept_id = pflag_l.concept_id
       AND a.tobereleased in ('Y','y')
       AND b.tobereleased in ('Y','y')
       AND a.language = c.language
       AND a.lui = c.lui
       AND c.atom_id = pflag_l.atom_id
       AND b.atom_id != c.atom_id;

       IF t_rank = rank_atom_for_release(
			pflag_l.atom_id, 'DUMMY', foreign_flag ) THEN
          RETURN 'L';
       ELSE
	  RETURN 'X';
       END IF;
   END IF;

   SELECT lui, termgroup INTO t_lui, t_termgroup FROM classes
   WHERE atom_id = pflag_l.atom_id;

   /* Only releasable atoms should be considered */
   SELECT MAX(rank_atom_for_release(atom_id, termgroup)) INTO t_rank
   FROM classes WHERE lui = t_lui AND concept_id = pflag_l.concept_id
    AND tobereleased in ('Y','y');

   IF t_rank = rank_atom_for_release(pflag_l.atom_id, t_termgroup) THEN
      RETURN 'L';
   END IF;

   RETURN 'X';

END pflag_l;

/* FUNCTION PFLAG_S ************************************************************
 * This function will return 'S' if atom_id is the highest rank string,
 * otherwise it will return 'X'.
 */
FUNCTION pflag_s(
   atom_id    IN INTEGER,
   concept_id IN INTEGER,
   foreign_flag IN VARCHAR2 := MEME_CONSTANTS.NO
)
RETURN VARCHAR2
IS
   t_sui      		VARCHAR2(10);
   t_rank     		VARCHAR2(50);
   t_bn			INTEGER;
   t_termgroup     	VARCHAR2(40);
BEGIN

   -- foreign is a bit different
   IF foreign_flag != MEME_CONSTANTS.NO THEN

       SELECT MAX(rank_atom_for_release(a.atom_id, 'DUMMY', 
			a.language)) INTO t_rank
       FROM foreign_classes a, classes b, foreign_classes c
       WHERE a.eng_atom_id=b.atom_id
       AND b.concept_id = pflag_s.concept_id
       AND a.tobereleased in ('Y','y')
       AND b.tobereleased in ('Y','y')
       AND a.language = c.language
       AND a.lui = c.lui
       AND a.sui = c.sui
       AND c.atom_id = pflag_s.atom_id
       AND b.atom_id != c.atom_id;

       IF t_rank = rank_atom_for_release(
			pflag_s.atom_id, 'DUMMY', foreign_flag ) THEN
          RETURN 'S';
       ELSE
	  RETURN 'X';
       END IF;
   END IF;

   SELECT sui, termgroup INTO t_sui, t_termgroup FROM classes
   WHERE atom_id = pflag_s.atom_id;

   /* Only releasable atoms should be considered */
   SELECT MAX(rank_atom_for_release(atom_id, termgroup)) INTO t_rank
   FROM classes WHERE sui = t_sui AND concept_id = pflag_s.concept_id
    AND tobereleased in ('Y','y');

   IF t_rank = rank_atom_for_release(pflag_s.atom_id, t_termgroup) THEN
      RETURN 'S';
   END IF;

   RETURN 'X';

END pflag_s;

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
    -- Print version
    MEME_UTILITY.PUT_MESSAGE('.');
    version;
END help;

/* PROCEDURE REGISTER_PACKAGE **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_RANKS.release_number,
      MEME_RANKS.version_number,
      SYSDATE,
      MEME_RANKS.version_authority,
      MEME_RANKS.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

/* PROCEDURE SELF_TEST *********************************************************
 */
PROCEDURE self_test
IS
    TYPE curvar_type IS REF CURSOR;
    test_cursor   	curvar_type;
    atom_row		classes%ROWTYPE;
    attribute_row	attributes%ROWTYPE;
    relationship_row	relationships%ROWTYPE;
    retval  		INTEGER;
    i			INTEGER;
    id			INTEGER;
    rank		INTEGER;
BEGIN
    -- This procedure requires SET SERVEROUTPUT ON

    MEME_UTILITY.PUT_MESSAGE('This test is designed to demonstrate the functionality');
    MEME_UTILITY.PUT_MESSAGE('of each of the contained components.');
    MEME_UTILITY.PUT_MESSAGE('.');
    MEME_UTILITY.PUT_MESSAGE('It is currently not implemented.');


END self_test;

END; -- package body
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_RANKS.help;
execute MEME_RANKS.register_package;
