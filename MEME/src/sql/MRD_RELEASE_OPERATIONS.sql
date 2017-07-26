CREATE OR REPLACE PACKAGE MRD_RELEASE_OPERATIONS AS

/******************************************************************************
 *
 * PL/SQL File: MRD_RELEASE_OPERATIONS.sql
 *
 * Changes
 * 09/29/2011 PM : Modified mrrel_prepare queries for AQ and QB rels, added filtering on source=MSH.
 * 04/25/2008 BAC (1-H83PD): Use YYYYMMDD instead of YYYY_MM_DD for VSTART and VEND when making MRSAB.
 *  01/30/2008 TTN (1-GC89L): implement the code to apply CVF field for STY based on the atoms CVF values in that concept.
 *   07/17/2007 2.2   SL (1-EG305) : Changed the toid/fromid in mrmap value column to 50 varchar2
 *   07/17/2007 2.2   SL (1-EG3G3) : Changed the attribute value column to 350 varchar2
 *  01/29/2007 BAC, Soma (1-D5427): fix ATNL computation.
 *  08/31/2006 TTN (1-C261E): use the ranking algorithm from MEME_RANKS
 *  06/12/2006 TTN (1-BGD15): compute ts,stt,ispref in mrconso_prepare
 *  06/09/2006 TTN (1-BFPCX): remove CVF entries from mrdoc
 *  06/01/2006 BAC (1-BCSO4): Implement correct MED<year> ATUI semantics in mrsat_prepare
 *  03/29/2006 TK (1-AHNAL) : Fixed code changes to apply_cvf
 *  02/27/2006 TTN (1-AHNAL) : add code and cascade field to content_view_members
 *                             apply_cvf can just use the code value of cv_memebers
 *  01/24/2006 BAC (1-7558C): remove classes_feedback references.
 *
 * 09/09/2005  2.0.5: mrcui_prepare builds t_merged_cuis,
		      t_deleted_cuis from mrd_cui_history
 * 07/18/2005  2.0.4: mrcui_prepare builds mrcui_pre straight from
 *                    mrd_cui_history
 * 07/12/2005  2.0.3: Replace GO, INC with MRSAB.CXTY
 * 06/21/2005  2.0.2: Remove _feedback procedures
 * 04/22/2005  2.0.1: +mraui_prepare for building MRAUI data
 *                  Use of expiration dates for mrd tables verified
 * 10/30/2003  1.1.7: Changes for 2003AC
 *                - mrrel_pre reads from mrd_relationships and builds AQs only
 *                - mrrel_pre joins AQ rels to pick up RUI
 *                - mrsat_pre joins to pick up ATUI for DA,MR,ST,AM,MED*
 * 10/30/2002  1.1.6: mrcui_pre handles merges that are now the opposite
 *                    of a historical MRCUI. i.e. C1|2002AB|SY|C2 becomes
 *                    C2|2002AD|SY|C1.  Also, MRCUI looks at dead_classes
 *                    for finding old CUIs.  Also, MRCUI reviews the previous
 *                    release mappings (classes_feedback)
 *                    for computing old cuis.
 *                    mrsab_pre better handles MED<YYYY> and LT attributes
 *	 	      mrrel_pre allows NLM02 and NLM03 to be RXNORM.
 * 		      mrcui_pre builds with MAPIN field.
 *                    mrcui_pre was not correctly inserting all DEL rows.
 *                    mrsab_pre mstart,mend date formats changed
 * 10/15/2002  1.1.5: mrsat_prepare deals with lexical tags.
 * 08/13/2002  1.1.4: mrcui_prepare has a much refined and cleaned
 *                    algorithm that should allow a gold script to be written
 * 07/30/2002  1.1.3: mrcui_prepare fixed to take bequeathal rel
 *                    mappings from cui_history. mrlo_prepare fixed
 *                    to correctly handle SNA/SUI transformations.
 * 07/25/2002  1.1.2: mrcui_prepare was updated to handle transitivity
 *                  of bequeathal relationships.
 * 05/02/2002  1.1: Updated for NLM02 algorithms:
 *                  - NLM02 atoms should have null codes
 *                  - Rels connected to NLM02/SCD, NLM02/SCDC atoms
 *                    should have NLM02 as a source.
 * 01/01/2002  1.0: This is the version of the code used to produce
 *		    the Meta2002AA release.  Any further changes should
 *		    be recorded/logged in this section.
 * 07/12/2006  1.0: SL: 1-BNL1W -- Adding new MTH ATN names for 2006AC release
 *
 *****************************************************************************/


    package_name	VARCHAR2(25) 	:= 'MRD_RELEASE_OPERATIONS';
    release_number	VARCHAR2(5)	:= '2';
    version_number	VARCHAR2(10)	:= '0.1';
    version_date	DATE		:= '22-Apr-2005';
    version_authority 	VARCHAR2(10)   	:= 'BAC';

    location	      	VARCHAR2(10);
    method	      	VARCHAR2(256);
    err_msg		VARCHAR2(256);
    err_code		INTEGER;

    mrd_release_debug	BOOLEAN := FALSE;
    mrd_release_trace	BOOLEAN := FALSE;

    mrd_release_exception	EXCEPTION;

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

    PROCEDURE log_progress(
    	authority	IN VARCHAR2,
    	activity	IN VARCHAR2,
    	detail		IN VARCHAR2,
	transaction_id	IN INTEGER,
	work_id		IN INTEGER,
	elapsed_time	IN INTEGER DEFAULT 0
    );

    PROCEDURE mrd_release_operations_error (
    	method		      IN VARCHAR2,
    	location	      IN VARCHAR2,
    	error_code	      IN INTEGER,
	detail		      IN VARCHAR2
    );

    PROCEDURE help;
    PROCEDURE help(method_name IN VARCHAR2);
    PROCEDURE self_test;

    PROCEDURE mrcoc_prepare(
      meta_mbd      IN DATE,
      meta_med      IN DATE
    );

    PROCEDURE mrconso_prepare;

    PROCEDURE mrcxt_prepare;

    PROCEDURE mrhier_prepare;

    PROCEDURE mrdef_prepare;

    PROCEDURE mrdoc_prepare;

    PROCEDURE mrhist_prepare;

    PROCEDURE mrmap_prepare;

    PROCEDURE mrrank_prepare;

    PROCEDURE mrrel_prepare;

    PROCEDURE mrsat_prepare(
      meta_med      IN DATE
    );

    PROCEDURE mrsab_prepare;

    PROCEDURE mrsty_prepare;

    PROCEDURE ambig_prepare;

    PROCEDURE mraui_prepare;

    PROCEDURE mrcui_prepare(
      meta_previous           IN DATE,
      meta_previous_major     IN DATE := NULL,
      prev_version	      IN VARCHAR2
    );

    PROCEDURE apply_cvf(
      table_name	IN VARCHAR2,
      key_field		IN VARCHAR2,
      cascade_field	IN VARCHAR2,
      cui_field		IN VARCHAR2
    );

    PROCEDURE apply_sty_cvf(
      table_name	IN VARCHAR2,
      cui_field		IN VARCHAR2
    );

    END MRD_RELEASE_OPERATIONS;
/
SHOW ERRORS

CREATE OR REPLACE PACKAGE BODY MRD_RELEASE_OPERATIONS AS
/* FUNCTION RELEASE *****************************************/
FUNCTION release
RETURN INTEGER
IS
BEGIN
   version;
   return to_number(release_number);
END release;


/* FUNCTION VERSION *****************************************/
FUNCTION version
RETURN FLOAT
IS
BEGIN
   version;
   return to_number(version_number);
END version;

/* FUNCTION VERSION_INFO ************************************/
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
   return package_name || ' Release ' || release_number || ': ' ||
     'version ' || version_number || ' (' || version_date || ')';
END version_info;

/* PROCEDURE VERSION ****************************************/
PROCEDURE version
IS
BEGIN

   DBMS_OUTPUT.PUT_LINE('Package: ' || package_name);
   DBMS_OUTPUT.PUT_LINE('Release ' || release_number || ': ' ||
	      'version ' || version_number || ', ' ||
	 version_date || ' (' ||
	 version_authority || ')');

END version;

/* PROCEDURE SET_DEBUG_ON ***********************************/
PROCEDURE set_debug_on
IS
BEGIN
   mrd_release_debug := TRUE;
END set_debug_on;

/* PROCEDURE SET_DEBUG_OFF **********************************/
PROCEDURE set_debug_off
IS
BEGIN
   mrd_release_debug := FALSE;
END set_debug_off;

/* PROCEDURE SET_TRACE_ON ***********************************/
PROCEDURE set_trace_on
IS
BEGIN
   mrd_release_trace := TRUE;
END set_trace_on;

/* PROCEDURE SET_TRACE_OFF **********************************/
PROCEDURE set_trace_off
IS
BEGIN
   mrd_release_trace:= FALSE;
END set_trace_off;

/* PROCEDURE TRACE ******************************************/
PROCEDURE trace ( message IN VARCHAR2 )
IS
BEGIN

    IF mrd_release_trace = TRUE THEN

	MEME_UTILITY.PUT_MESSAGE(message);

    END IF;

END trace;

/* PROCEDURE INITIALIZE_TRACE *******************************
 *
 * This method clears location, err_msg, method
 */
PROCEDURE initialize_trace ( method	IN VARCHAR2 )
IS
BEGIN
    location := '0';
    err_msg := '';
    err_code := 1;
    mrd_release_operations.method := initialize_trace.method;
END initialize_trace;

/* PROCEDURE MRD_RELEASE_OPERATIONS_ERROR ***************************/
PROCEDURE mrd_release_operations_error (
    method	    	IN VARCHAR2,
    location	    	IN VARCHAR2,
    error_code	  	IN INTEGER,
    detail	 	IN VARCHAR2
)
IS
    error_msg	    VARCHAR2(100);
BEGIN
    IF error_code = 1 THEN
  	error_msg := 'MRO0001: Unspecified error';
    ELSE
	  error_msg := 'MRO0000: Unknown Error';
    END IF;

    MEME_UTILITY.PUT_ERROR(
      'Error in MRD_RELEASE_OPERATIONS::'||method||' at '||
	   location||' ('||error_msg||','||detail||')');

END mrd_release_operations_error;

/* PROCEDURE MRMAP_PREPARE **********************************
 * Creates and loads mratx_pre which contains the
 * full release view of MRMAP
 */
PROCEDURE mrmap_prepare
IS
    row_count			INTEGER;
    row_multiplier		INTEGER;
    chunk_size			INTEGER := 2000;
BEGIN

	-- Initialize tracking parameters and start timing elements
   	initialize_trace('MRMAP_PREPARE');
   	MEME_UTILITY.timing_start;

    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrmap_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Starting mrmap_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrmap_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrmap_pre');

    location := '20';
    MEME_UTILITY.exec(
	'CREATE TABLE  mrmap_pre (
        mapsetcui		VARCHAR2(10) NOT NULL,
	    mapsetsab		VARCHAR2(40) NOT NULL,
        mapsubsetid		VARCHAR2(10) ,
        maprank		VARCHAR2(10) ,
        mapid		VARCHAR2(12) ,
        mapsid		VARCHAR2(50) ,
        fromid		VARCHAR2(50) ,
        fromsid		VARCHAR2(10) ,
	    fromexpr		VARCHAR2(1786),
        fromtype		VARCHAR2(100) ,
        fromrule		VARCHAR2(100) ,
	    fromres		VARCHAR2(100),
	    rel			VARCHAR2(1786),
	    rela		VARCHAR2(1786),
        toid		VARCHAR2(50) ,
        tosid		VARCHAR2(10) ,
	    toexpr		VARCHAR2(1786),
        totype		VARCHAR2(100) ,
        torule		VARCHAR2(100) ,
	    tores		VARCHAR2(100),
	    maprule		VARCHAR2(500),
	    mapres		VARCHAR2(500),
	    maptype		VARCHAR2(100),
	    mapatn		VARCHAR2(20),
	    mapatv		VARCHAR2(1786),
	    cvf			NUMBER(20) )
	');

    -- Recreate the mrmap_pre_xmap table
    MEME_UTILITY.sub_timing_start;
    location := '30';
    MEME_UTILITY.drop_it('table','mrmap_pre_xmap');

    location := '40';
    MEME_UTILITY.exec(
	'CREATE TABLE  mrmap_pre_xmap (
            mapsetcui		VARCHAR2(10) NOT NULL,
	    mapsetsab		VARCHAR2(40) NOT NULL,
            mapsubsetid		VARCHAR2(10) ,
            maprank		VARCHAR2(10) ,
            mapid		VARCHAR2(12) ,
            mapsid		VARCHAR2(50) ,
            fromid		VARCHAR2(50) ,
	    rel			VARCHAR2(1786),
	    rela		VARCHAR2(1786),
            toid		VARCHAR2(50) ,
	    maprule		VARCHAR2(500),
	    mapres		VARCHAR2(500),
	    maptype		VARCHAR2(100),
	    mapatn		VARCHAR2(20),
	    mapatv		VARCHAR2(1786))
	');

    -- Get XMAP attributes
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrmap_pre_xmap (mapsetcui,mapsetsab,mapid,mapsubsetid,
			maprank,fromid,rel,rela,toid,
			maprule,maptype,mapatn,mapatv,mapsid,mapres)
      	 SELECT /*+ PARALLEL(a) */ cui, root_source, atui,
		SUBSTR(attribute_value, 1, instr(attribute_value, ''~'') - 1),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'') + 1,
        	             (INSTR(attribute_value, ''~'', 1, 2) -
			      INSTR(attribute_value, ''~'', 1)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 2) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 3) -
			      INSTR(attribute_value, ''~'', 1, 2)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 3) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 4) -
			      INSTR(attribute_value, ''~'', 1, 3)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 4) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 5) -
			      INSTR(attribute_value, ''~'', 1, 4)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 5) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 6) -
			      INSTR(attribute_value, ''~'', 1, 5)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 6) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 7) -
			      INSTR(attribute_value, ''~'', 1, 6)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 7) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 8) -
			      INSTR(attribute_value, ''~'', 1, 7)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 8) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 9) -
			      INSTR(attribute_value, ''~'', 1, 8)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 9) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 10) -
			      INSTR(attribute_value, ''~'', 1, 9)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 10) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 11) -
			      INSTR(attribute_value, ''~'', 1, 10)) -1 ),
      		SUBSTR(attribute_value, instr(attribute_value, ''~'', -1) + 1)
      	 FROM mrd_attributes  a
      	 WHERE expiration_date IS NULL
	   AND attribute_value NOT LIKE ''<>Long_Attribute<>:%''
      	   AND attribute_name = ''XMAP'' ' );

   COMMIT;

   MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '45';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Table mrmap_pre_xmap created ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get Long XMAP attributes
    MEME_UTILITY.sub_timing_start;
    location := '50';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrmap_pre_xmap (mapsetcui,mapsetsab,mapid,mapsubsetid,
			maprank,fromid,rel,rela,toid,
			maprule,maptype,mapatn,mapatv,mapsid,mapres)
      	 SELECT /*+ PARALLEL(a) */ cui, root_source, atui,
		SUBSTR(text_value, 1, instr(text_value, ''~'') - 1),
       		SUBSTR(text_value, instr(text_value, ''~'') + 1,
        	             (INSTR(text_value, ''~'', 1, 2) -
			      INSTR(text_value, ''~'', 1)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 2) + 1,
        	             (INSTR(text_value, ''~'', 1, 3) -
			      INSTR(text_value, ''~'', 1, 2)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 3) + 1,
        	             (INSTR(text_value, ''~'', 1, 4) -
			      INSTR(text_value, ''~'', 1, 3)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 4) + 1,
        	             (INSTR(text_value, ''~'', 1, 5) -
			      INSTR(text_value, ''~'', 1, 4)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 5) + 1,
        	             (INSTR(text_value, ''~'', 1, 6) -
			      INSTR(text_value, ''~'', 1, 5)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 6) + 1,
        	             (INSTR(text_value, ''~'', 1, 7) -
			      INSTR(text_value, ''~'', 1, 6)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 7) + 1,
        	             (INSTR(text_value, ''~'', 1, 8) -
			      INSTR(text_value, ''~'', 1, 7)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 8) + 1,
        	             (INSTR(text_value, ''~'', 1, 9) -
			      INSTR(text_value, ''~'', 1, 8)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 9) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 10) -
			      INSTR(attribute_value, ''~'', 1, 9)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 10) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 11) -
			      INSTR(attribute_value, ''~'', 1, 10)) -1 ),
      		SUBSTR(text_value, instr(text_value, ''~'', -1) + 1)
      	 FROM mrd_attributes a, mrd_stringtab b
      	 WHERE a.expiration_date IS NULL
	   AND b.expiration_date IS NULL
	   AND attribute_value LIKE ''<>Long_Attribute<>:%''
      	   AND b.hashcode = a.hashcode
      	   AND attribute_name = ''XMAP'' ' );

    COMMIT;

    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '55';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Finished generating long XMAP attributes ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Recreate the mrmap_pre_from table
    MEME_UTILITY.sub_timing_start;
    location := '60';
    MEME_UTILITY.drop_it('table','mrmap_pre_from');

    location := '70';
    MEME_UTILITY.exec(
	'CREATE TABLE  mrmap_pre_from (
            mapsetcui		VARCHAR2(10) NOT NULL,
            fromid		VARCHAR2(50) ,
            fromsid		VARCHAR2(10) ,
	    fromexpr		VARCHAR2(1786),
            fromtype		VARCHAR2(100) ,
            fromrule		VARCHAR2(100) ,
	    fromres		VARCHAR2(100))
	');

    -- Get XMAPFROM attributes
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrmap_pre_from (mapsetcui,fromid,fromsid,fromexpr,fromtype,fromrule,fromres)
      	 SELECT /*+ PARALLEL(a) */ cui,
		SUBSTR(attribute_value, 1, instr(attribute_value, ''~'') - 1),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'') + 1,
        	             (INSTR(attribute_value, ''~'', 1, 2) -
			      INSTR(attribute_value, ''~'', 1)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 2) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 3) -
			      INSTR(attribute_value, ''~'', 1, 2)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 3) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 4) -
			      INSTR(attribute_value, ''~'', 1, 3)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 4) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 5) -
			      INSTR(attribute_value, ''~'', 1, 4)) -1 ),
      		SUBSTR(attribute_value, instr(attribute_value, ''~'', -1) + 1)
      	 FROM mrd_attributes a
      	 WHERE expiration_date IS NULL
	   AND attribute_value NOT LIKE ''<>Long_Attribute<>:%''
      	   AND attribute_name = ''XMAPFROM'' ' );

    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '75';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Table mrmap_pre_from created ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get Long XMAPFROM attributes
    MEME_UTILITY.sub_timing_start;
    location := '80';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrmap_pre_from (mapsetcui,fromid,fromsid,fromexpr,fromtype,fromrule,fromres)
      	 SELECT /*+ PARALLEL(a) */  cui,
		SUBSTR(text_value, 1, instr(text_value, ''~'') - 1),
       		SUBSTR(text_value, instr(text_value, ''~'') + 1,
        	             (INSTR(text_value, ''~'', 1, 2) -
			      INSTR(text_value, ''~'', 1)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 2) + 1,
        	             (INSTR(text_value, ''~'', 1, 3) -
			      INSTR(text_value, ''~'', 1, 2)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 3) + 1,
        	             (INSTR(text_value, ''~'', 1, 4) -
			      INSTR(text_value, ''~'', 1, 3)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 4) + 1,
        	             (INSTR(text_value, ''~'', 1, 5) -
			      INSTR(text_value, ''~'', 1, 4)) -1 ),
      		SUBSTR(text_value, instr(text_value, ''~'', -1) + 1)
      	 FROM mrd_attributes a, mrd_stringtab b
      	 WHERE a.expiration_date IS NULL
	   AND b.expiration_date IS NULL
	   AND attribute_value LIKE ''<>Long_Attribute<>:%''
      	   AND b.hashcode = a.hashcode
      	   AND attribute_name = ''XMAPFROM'' ' );

    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '80';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Finished generating long XMAPFROM attributes ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Recreate the mrmap_pre_to table
    MEME_UTILITY.sub_timing_start;
    location := '90';
    MEME_UTILITY.drop_it('table','mrmap_pre_to');

    location := '100';
    MEME_UTILITY.exec(
	'CREATE TABLE  mrmap_pre_to (
            mapsetcui		VARCHAR2(10) NOT NULL,
            toid		VARCHAR2(50) ,
            tosid		VARCHAR2(10) ,
	    toexpr		VARCHAR2(1786),
            totype		VARCHAR2(100) ,
            torule		VARCHAR2(100) ,
	    tores		VARCHAR2(100))
	');

    -- Get XMAPTO attributes
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrmap_pre_to (mapsetcui,toid,tosid,toexpr,totype,torule,tores)
      	 SELECT /*+ PARALLEL(a) */  cui,
		SUBSTR(attribute_value, 1, instr(attribute_value, ''~'') - 1),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'') + 1,
        	             (INSTR(attribute_value, ''~'', 1, 2) -
			      INSTR(attribute_value, ''~'', 1)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 2) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 3) -
			      INSTR(attribute_value, ''~'', 1, 2)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 3) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 4) -
			      INSTR(attribute_value, ''~'', 1, 3)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 4) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 5) -
			      INSTR(attribute_value, ''~'', 1, 4)) -1 ),
      		SUBSTR(attribute_value, instr(attribute_value, ''~'', -1) + 1)
      	 FROM mrd_attributes
      	 WHERE expiration_date IS NULL
	   AND attribute_value NOT LIKE ''<>Long_Attribute<>:%''
      	   AND attribute_name = ''XMAPTO'' ' );

    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '105';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Table mrmap_pre_to created ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get Long XMAPTO attributes
    MEME_UTILITY.sub_timing_start;
    location := '110';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrmap_pre_to (mapsetcui,toid,tosid,toexpr,totype,torule,tores)
      	 SELECT /*+ PARALLEL(a) */ cui,
		SUBSTR(text_value, 1, instr(text_value, ''~'') - 1),
       		SUBSTR(text_value, instr(text_value, ''~'') + 1,
        	             (INSTR(text_value, ''~'', 1, 2) -
			      INSTR(text_value, ''~'', 1)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 2) + 1,
        	             (INSTR(text_value, ''~'', 1, 3) -
			      INSTR(text_value, ''~'', 1, 2)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 3) + 1,
        	             (INSTR(text_value, ''~'', 1, 4) -
			      INSTR(text_value, ''~'', 1, 3)) -1 ),
       		SUBSTR(text_value, instr(text_value, ''~'', 1, 4) + 1,
        	             (INSTR(text_value, ''~'', 1, 5) -
			      INSTR(text_value, ''~'', 1, 4)) -1 ),
      		SUBSTR(text_value, instr(text_value, ''~'', -1) + 1)
      	 FROM mrd_attributes a, mrd_stringtab b
      	 WHERE a.expiration_date IS NULL
	   AND b.expiration_date IS NULL
	   AND attribute_value LIKE ''<>Long_Attribute<>:%''
      	   AND b.hashcode = a.hashcode
      	   AND attribute_name = ''XMAPTO'' ' );

    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '115';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Finished generating long XMAPTO attributes ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- We now have everything, we just need to join three tables.
    MEME_UTILITY.sub_timing_start;
    location := '120';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrmap_pre (mapsetcui,mapsetsab,mapsubsetid,maprank,
			fromid,fromsid,fromexpr,fromtype,fromrule,fromres,rel,rela,
			toid,tosid,toexpr,totype,torule,tores,
			maprule,maptype,mapatn,mapatv,mapid,mapsid,mapres)
	SELECT a.mapsetcui, a.mapsetsab, mapsubsetid, maprank,
		a.fromid, fromsid, fromexpr, fromtype, fromrule, fromres, rel, rela,
		a.toid, c.tosid, toexpr, totype, torule, tores,
		maprule, maptype, mapatn, mapatv, mapid, mapsid, mapres
	FROM mrmap_pre_xmap a, mrmap_pre_from b, mrmap_pre_to c
      	WHERE a.mapsetcui = b.mapsetcui
     	AND a.fromid = b.fromid
      	AND a.mapsetcui = c.mapsetcui
     	AND a.toid = c.toid
	');
    MEME_UTILITY.sub_timing_stop;
 -- To Handle the null values in TOID for ICD10PCS data
 
     MEME_UTILITY.sub_timing_start;
     location := '120.1';
     row_count := MEME_UTILITY.exec(
       'INSERT INTO mrmap_pre (mapsetcui,mapsetsab,mapsubsetid,maprank,
                       fromid,fromsid,fromexpr,fromtype,fromrule,fromres,rel,rela,
                       toid,tosid,toexpr,totype,torule,tores,
                       maprule,maptype,mapatn,mapatv,mapid,mapsid,mapres)
       SELECT a.mapsetcui, a.mapsetsab, mapsubsetid, maprank,
               a.fromid, fromsid, fromexpr, fromtype, fromrule, fromres, rel, rela,
               a.toid, c.tosid, toexpr, totype, torule, tores,
              maprule, maptype, mapatn, mapatv, mapid, mapsid, mapres
       FROM mrmap_pre_xmap a, mrmap_pre_from b, mrmap_pre_to c
               WHERE a.mapsetcui = b.mapsetcui
       AND a.fromid = b.fromid
       AND a.mapsetcui = c.mapsetcui
       AND a.toid is null
         AND c.toid is null
       ');
     MEME_UTILITY.sub_timing_stop;
    -- Log row count
    location := '130';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Finished generating mrmap_pre ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);


     -- Update relationship_name
    location := '131';
    MEME_UTILITY.sub_timing_start;
    EXECUTE IMMEDIATE
        'UPDATE mrmap_pre tr
    	SET rel =
	(SELECT release_name FROM inverse_relationships
	 WHERE relationship_name = tr.rel)
    	WHERE rel in
      	(SELECT relationship_name FROM inverse_relationships
	 WHERE relationship_name != release_name)';

    row_count := SQL%ROWCOUNT;

    -- Log completion of updating relationship_name
    MEME_UTILITY.sub_timing_stop;
    location := '132';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Finished updating relationship_name ' ||
	    row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);


    -- Update mapsubsetid, maprank
    location := '135';
    MEME_UTILITY.sub_timing_start;
    EXECUTE IMMEDIATE
        'UPDATE mrmap_pre SET  mapsubsetid = null, maprank = null
	 WHERE mapsubsetid = ''0''
	   AND maprank = ''0''
	   AND mapsetsab != ''SNOMEDCT_US''';

    row_count := SQL%ROWCOUNT;

    -- Log completion of updating mapsubsetid, maprank
    MEME_UTILITY.sub_timing_stop;
    location := '136';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmrap_prepare',
        detail => 'Finished updating mapsubsetid, maprank, ' ||
	    row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);


    -- Update XR rows
    location := '135';
    MEME_UTILITY.sub_timing_start;
    EXECUTE IMMEDIATE
        'UPDATE mrmap_pre mp
	 SET  toid=null, tosid=null, toexpr=null, totype=null,
	      torule=null, tores=null, rela=null
	 WHERE rel=''XR'' ';

    row_count := SQL%ROWCOUNT;

    -- Log completion of updating mapsubsetid, maprank
    MEME_UTILITY.sub_timing_stop;
    location := '136';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Finished updating mapsubsetid, maprank, ' ||
	    row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Set cvf
    apply_cvf(
	table_name => 'mrmap_pre',
	key_field => null,
	cascade_field => null,
	cui_field => 'mapsetcui');

    apply_cvf(
	table_name => 'mrmap_pre',
	key_field => null,
	cascade_field => 'mapsetsab',
	cui_field => null);

    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log row count
    location := '140';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrmap_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

	COMMIT;

END mrmap_prepare;

/* PROCEDURE MRCOC_PREPARE ********************************************
 *
 * This procedure joins coc_headings on itself and looks up all
 * of the co-ocurrences that have major topics.  It then finds cases
 * where a citation set id has only a single major topic producing
 * the null CUI2 MRCOC rows.  The procedure logs significant events
 * in meme_progress so you can see how far its gotten
 *
 * There are some special notes for this procedure
 * - For AIR93 COCs, the cof should be null
 * - For CCPSS, the cot=PP COCs should be bidirectional
 * - For CCPSS, the cot=MP COCs should be unidirectional (like LQ)
 *	where the cui1 is an MP (or there should be a MP)
 */
PROCEDURE mrcoc_prepare(
    meta_mbd      IN DATE,
    meta_med      IN DATE
)
IS

    CURSOR headings_cursor (mbd_date IN DATE) IS
	SELECT /*+ PARALLEL(a) USE_HASH(a,b) */
	      a.subheading_set_id as subheading_set_id_1,
	      a.heading_aui as aui_1, b.heading_aui as aui_2,
	      a.citation_set_id, a.publication_date, a.coc_type, a.root_source
        FROM mrd_coc_headings a, mrd_coc_headings b
        WHERE a.heading_aui != b.heading_aui
          AND a.citation_set_id = b.citation_set_id
	  AND a.root_source = b.root_source
          AND a.major_topic = 'Y'
          AND b.major_topic = 'Y'
          AND (a.publication_date >= mbd_date OR a.root_source != 'NLM-MED')
          AND a.expiration_date IS NULL
          AND b.expiration_date IS NULL
        ORDER BY a.heading_aui, b.heading_aui,
	         a.coc_type, a.root_source, a.publication_date;

    headings_var 		headings_cursor%ROWTYPE;

    CURSOR subheadings_cursor (ssid IN NUMBER, csid IN NUMBER) IS
	SELECT subheading_qa FROM mrd_coc_subheadings
 	WHERE subheading_set_id = ssid
	  AND citation_set_id = csid
          AND expiration_date IS NULL;

    -- types for handling coa_freq
    CURSOR coa_freq_cursor IS
    	SELECT string, 0
        FROM string_ui a, mrd_classes b
        WHERE b.expiration_date IS NULL
        AND a.sui=b.sui AND b.root_source = 'MSH'
        AND b.tty = 'QAB'
        UNION SELECT '<>', 0 FROM dual;

    qa                      	VARCHAR2(2);

    -- Data structure for tracking COA values
    TYPE coa_freq_rec IS RECORD ( coa VARCHAR2(2), freq NUMBER(12));
    TYPE coa_freq_tab IS TABLE OF coa_freq_rec;
    coa_freq  coa_freq_tab := coa_freq_tab();

    str                     	VARCHAR2(4000);

    -- mrcoc_pre values
    sab                     	VARCHAR2(10);
    cot                     	VARCHAR2(10);
    cof                     	NUMBER(12);
    coa                     	VARCHAR2(1000):= '';

    -- Tracking of previous entry
    prev_aui_1                	VARCHAR2(10):= '';
    prev_aui_2                	VARCHAR2(10):= '';
    prev_cot                	VARCHAR2(10):= '';
    prev_sab                	VARCHAR2(10):= '';

    -- SAB values, looked up in mrd_source_rank
    mbd			    	VARCHAR2(40);
    med			    	VARCHAR2(40);

    -- Counting/Tracking variables
    -- To make logging more frequent, make chunk_size smaller
    i				INTEGER;
    row_count			INTEGER;
    row_multiplier		INTEGER;
    chunk_size			INTEGER := 50000;

BEGIN

    initialize_trace('MRCOC_PREPARE');
    MEME_UTILITY.timing_start;

    EXECUTE IMMEDIATE 'ALTER SESSION SET sort_area_size=200000000';
    EXECUTE IMMEDIATE 'ALTER SESSION SET hash_area_size=200000000';

    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrcoc_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Starting mrcoc_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrcoc_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrcoc_pre');

    location := '20';
    MEME_UTILITY.exec(
      	'CREATE TABLE mrcoc_pre (
             cui_1		VARCHAR2(10),
             aui_1		VARCHAR2(10),
             cui_2		VARCHAR2(10),
             aui_2		VARCHAR2(10),
             sab        	VARCHAR2(10),
             cot        	VARCHAR2(10),
             cof        	NUMBER(12),
             coa        	VARCHAR2(1000),
             cvf        	NUMBER(20))
	 STORAGE (INITIAL 500M) ' );

    -- Initialize coa_freq from mrd_attributes
    location := '25.1';
    row_count := 1;
    OPEN coa_freq_cursor;
    LOOP
	location := '25.2';
	coa_freq.extend;
	location := '25.3';
	FETCH coa_freq_cursor INTO coa_freq(row_count);
	location := '25.4';
	EXIT WHEN coa_freq_cursor%NOTFOUND;

	row_count := row_count + 1;
    END LOOP;

    location := '25.5';
    CLOSE coa_freq_cursor;

    -- Get current values for MED and MBD sources
    location := '40';
    med := 'MED';
    mbd := 'MBD';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Using "' || med || '" for MED and "' || mbd || '" for MBD.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '55';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Opening main cursor loop.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Open main cursor
    -- We want all non-MSH rows and all MSH rows where
    -- the publication_date is >= the MBD start date
    MEME_UTILITY.sub_timing_start;
    row_count := 0;
    row_multiplier := 0;

    -- The frequency starts with zero, prev_aui_1 is null
    cof := 0;
    prev_aui_1 := '';

    location := '60';
    OPEN headings_cursor(meta_mbd);

    LOOP

     	location := '65';
    	FETCH headings_cursor INTO headings_var;

   	-- Catch the last citation_set_id by changing the AUI
    	IF headings_cursor%NOTFOUND THEN
    	    headings_var.aui_1 := 'A00000000';
    	END IF;

    	-- Set cot and sab for non Medline cooccurences
    	cot := headings_var.coc_type;
    	sab := headings_var.root_source;

    	-- Set cot and sab for Medline cooccurences
    	IF headings_var.root_source = 'NLM-MED' THEN

	    -- D-D coocurrences get cot of L
            cot := 'L';

     	    -- If publication date is before the starting med date
      	    -- it is MBD because we are only selecting rows with
      	    -- publication dates after mbd start date.
      	    IF headings_var.publication_date < meta_med THEN
       	    	sab := mbd;
      	    ELSE
            	sab := med;
      	    END IF;

    	END IF;


        -- If the previous parameters are not the same
	-- (unless they are null), then the current row
    	-- represents a new COC, insert the previous data.
    	IF (headings_var.aui_1 != prev_aui_1 OR
	    headings_var.aui_2 != prev_aui_2 OR
	    cot != prev_cot OR sab != prev_sab) AND
  	   prev_aui_1 IS NOT NULL
    	THEN

      	    -- Build the COA value for 'L' COT values
      	    IF prev_cot = 'L' THEN

		-- Reset the coa string
                str := '';

		-- Assemble the COA field from entries where the freq!=0
		-- Also, reset the freq =0 for next round
 	    	location := '155';
		FOR i IN coa_freq.first .. coa_freq.last LOOP
	 	    IF coa_freq(i).freq != 0 THEN
			str := str || coa_freq(i).coa || '=' ||
				coa_freq(i).freq || ',';

			-- Reset the coa frequency
			coa_freq(i).freq := 0;
		    END IF;
		END LOOP;

	    	-- Strip trailing comma
	    	location := '190';
            	coa := RTRIM(str, ',');

	    ELSE

		-- Not an 'L' cot, set coa to null
	    	location := '200';
            	coa := '';

	    END IF;


	    -- For AIR ones, we do not want a cof.
	    IF prev_sab like 'AIR%' THEN
		cof := '';
	    END IF;

      	    	-- insert row into mrcoc_pre
	    -- as long as we are not on the first row
  	    location := '210';
      	    EXECUTE IMMEDIATE
        	    'INSERT INTO mrcoc_pre (
        		aui_1, aui_2, sab, cot, cof, coa)
        	     VALUES(:x, :x, :x, :x, :x, :x) '
            USING prev_aui_1, prev_aui_2, prev_sab, prev_cot, cof, coa;

      	    -- Reset frequency
      	    cof := 0;

	    -- Manage logging of progress
   	    row_count := row_count + 1;

  	    IF row_count = chunk_size THEN

		row_multiplier := row_multiplier + 1;
		row_count := 0;

		-- Log the chunk's worth of progress
		MEME_UTILITY.sub_timing_stop;
		location := '225';
	        log_progress(
        	    authority => 'RELEASE',
        	    activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        	    detail => 'Processing COCs, ' ||
			(row_multiplier*chunk_size) ||
			' rows completed.',
        	    transaction_id => 0,
        	    work_id => 0,
        	    elapsed_time => MEME_UTILITY.sub_elapsed_time);

		-- Commit every chunk_size rows
		location := '225.2';
		COMMIT;

	    END IF;

	END IF;

	-- Once we have inserted the last row, if the cursor is
	-- empty (meaning headings_var.aui_1 IS NULL) then we are done.
      	EXIT WHEN headings_cursor%NOTFOUND;

	-- From here down, we are just dealing with the current row

      	-- increment frequency
      	cof := cof + 1;

        -- If the current COC we are dealing with is cot=L
  	-- Look up the subheading counts.
      	IF cot = 'L' THEN

	    -- If there is no subheading, use <> instead
	    -- Otherwise look up subheadings for this citation set id
            IF headings_var.subheading_set_id_1 IS NOT NULL THEN
	    	location := '90';
             	OPEN subheadings_cursor(headings_var.subheading_set_id_1,
		      			headings_var.citation_set_id);
	    	-- For each subheading set, increment QA values in coa_freq
            	LOOP

	  	    location := '100';
            	    FETCH subheadings_cursor INTO qa;

	    	    location := '110';
            	    EXIT WHEN subheadings_cursor%NOTFOUND;

		    -- Update a row in coa_freq matching the qa.
 		    FOR i IN coa_freq.first .. coa_freq.last LOOP
		    	IF coa_freq(i).coa = qa THEN
			    coa_freq(i).freq := coa_freq(i).freq + 1;
			    EXIT;
			END IF;
	            END LOOP;

	   	END LOOP;

	   	location := '150';
           	CLOSE subheadings_cursor;

	    ELSE

		-- There are no subheadings, increment the '<>' entry
		-- which is assumed to be the first row of coa_freq
		coa_freq(1).freq := coa_freq(1).freq + 1;

	    END IF;

        END IF;

      	-- Set prev values
      	location := '220';
      	prev_aui_1 := headings_var.aui_1;
      	prev_aui_2 := headings_var.aui_2;
      	prev_sab := sab;
      	prev_cot := cot;

    END LOOP;

    -- Done with first section
    location := '226';
    CLOSE headings_cursor;

    -- Log completion of first section
    MEME_UTILITY.sub_timing_stop;
    location := '228';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Main loop completed successfully, ' ||
	    (row_multiplier*chunk_size+row_count) ||
	    ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);


    -- Here, we need to compute the LQ and LQB Medline rows.
    -- This involves scanning through coc_headings and identifying
    -- those subheadings connected to the descriptors.
    MEME_UTILITY.sub_timing_start;

    -- Prepare temporary table
    location := '240';
    MEME_UTILITY.drop_it('table','t_lq_lqb_count');

    location := '250';
    MEME_UTILITY.exec(
        'CREATE TABLE t_lq_lqb_count (
	   heading_aui  	VARCHAR2(10),
	   qa         		VARCHAR2(2),
	   sab			VARCHAR2(40),
	   ct	      		NUMBER(12) ) ' );

    -- Get mbd count with subheadings
    location := '260';
    EXECUTE IMMEDIATE
	'INSERT INTO t_lq_lqb_count
	SELECT /*+ PARALLEL(a) */ a.heading_aui, b.subheading_qa, :x, count(*)
	FROM mrd_coc_headings a, mrd_coc_subheadings b
	WHERE root_source = ''NLM-MED''
	  AND publication_date >= :x
	  AND publication_date < :x
	  AND major_topic = ''Y''
	  AND a.citation_set_id = b.citation_set_id
	  AND a.subheading_set_id = b.subheading_set_id
          AND a.expiration_date IS NULL
          AND b.expiration_date IS NULL
	GROUP BY a.heading_aui, b.subheading_qa '
    USING mbd, meta_mbd, meta_med;

    COMMIT;

   -- Get mbd count without subheadings
    location := '270';
    EXECUTE IMMEDIATE
	'INSERT INTO t_lq_lqb_count
 	SELECT heading_aui, :x, :x, count(*)
	FROM mrd_coc_headings a
	WHERE root_source = ''NLM-MED''
	  AND publication_date >= :x
	  AND publication_date < :x
	  AND major_topic = ''Y''
	  AND subheading_set_id IS NULL
          AND expiration_date IS NULL
	GROUP BY heading_aui'
    USING '', mbd, meta_mbd, meta_med;

    COMMIT;

    -- Get med count with subheadings
    location := '280';
    EXECUTE IMMEDIATE
	'INSERT INTO t_lq_lqb_count
	SELECT /*+ PARALLEL(a) */ a.heading_aui, b.subheading_qa, :x, count(*)
	FROM mrd_coc_headings a, mrd_coc_subheadings b
	WHERE root_source = ''NLM-MED''
	  AND publication_date >= :x
	  AND publication_date < :x
	  AND major_topic = ''Y''
	  AND a.citation_set_id = b.citation_set_id
	  AND a.subheading_set_id = b.subheading_set_id
          AND a.expiration_date IS NULL
          AND b.expiration_date IS NULL
	GROUP BY a.heading_aui, b.subheading_qa '
    USING med, meta_med, sysdate;

    COMMIT;

    -- Get mbd count without subheadings
    location := '290';
    EXECUTE IMMEDIATE
	'INSERT INTO t_lq_lqb_count
 	SELECT heading_aui, :x, :x, count(*)
	FROM mrd_coc_headings a
	WHERE root_source = ''NLM-MED''
	  AND publication_date >= :x
	  AND publication_date < :x
	  AND major_topic = ''Y''
	  AND subheading_set_id IS NULL
          AND expiration_date IS NULL
	GROUP BY heading_aui'
    USING '', med, meta_med, sysdate;

    COMMIT;

    -- Aggregate the temp table counts by AUI,SAB
    -- Get null-cui2 LQ rows
    MEME_UTILITY.sub_timing_start;
    location := '300';
    EXECUTE IMMEDIATE
        'INSERT INTO mrcoc_pre (aui_1,aui_2,sab,cot,cof,coa)
         SELECT heading_aui, :x ,sab, :x, sum(ct), :x
         FROM t_lq_lqb_count
         WHERE qa IS NULL
         GROUP BY heading_aui, sab'
    USING '','LQ','';

    row_count := SQL%ROWCOUNT;

    -- Get LQ rows
    location := '310';
    EXECUTE IMMEDIATE
        'INSERT INTO mrcoc_pre (aui_1,aui_2,sab,cot,cof,coa)
         SELECT a.heading_aui, d.aui , sab, ''LQ'', sum(ct), ''''
         FROM t_lq_lqb_count a, string_ui c, mrd_classes b, mrd_classes d
         WHERE qa = c.string
           AND d.tty = ''TQ''
           AND d.code = b.code
           AND d.root_source = ''MSH''
           AND b.tty = ''QAB''
           AND b.root_source = ''MSH''
           AND b.sui = c.sui
           AND b.expiration_date IS NULL
         GROUP BY a.heading_aui, d.aui, sab';

    row_count := row_count + SQL%ROWCOUNT;

    -- Get LQB rows
    location := '320';
    EXECUTE IMMEDIATE
        'INSERT INTO mrcoc_pre (aui_1,aui_2,sab,cot,cof,coa)
         SELECT aui_2, aui_1, sab, ''LQB'', cof, coa
	 FROM mrcoc_pre WHERE cot=''LQ'' AND aui_2 IS NOT NULL';

    row_count := row_count + SQL%ROWCOUNT;

    -- Clean up temp table
    location := '330';
    MEME_UTILITY.drop_it('table','t_lq_lqb_count');

    -- Log completion of processing LQ,LQB rows
    MEME_UTILITY.sub_timing_stop;
    location := '340';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Finished processing LQ,LQB COCs, ' ||
	    row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Update CUI_1
    location := '350';
        
    EXECUTE IMMEDIATE
        'UPDATE /*+ parallel(c) */ mrcoc_pre c SET cui_1 =
         (SELECT cui
	 FROM mrd_classes
	 WHERE expiration_date IS NULL
	   AND aui = aui_1) ';

    row_count := SQL%ROWCOUNT;

    -- Log completion of updating CUI_1
    MEME_UTILITY.sub_timing_stop;
    location := '360';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Finished updating CUI_1, ' ||
	    row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Update CUI_2
    location := '370';
    EXECUTE IMMEDIATE
        'UPDATE /*+ parallel(c) */ mrcoc_pre c SET cui_2 =
         (SELECT cui
	 FROM mrd_classes
	 WHERE expiration_date IS NULL
	   AND aui = aui_2) ';

    row_count := SQL%ROWCOUNT;

    -- Log completion of updating CUI_2
    MEME_UTILITY.sub_timing_stop;
    location := '380';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Finished updating CUI_2, ' ||
	    row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Remove null CUI_1 and CUI_2 rows
    -- THIS probably shouldn't do anything
    location := '390';
    EXECUTE IMMEDIATE
        'DELETE mrcoc_pre
	     WHERE (cui_1 IS NULL OR cui_2 IS NULL)
	       AND cot != ''LQ'' ';

    row_count := SQL%ROWCOUNT;

    -- Log completion of removing null CUI_1 and CUI_2 rows
    MEME_UTILITY.sub_timing_stop;
    location := '400';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Finished removing null CUI_1 and CUI_2, ' ||
	    row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Set cvf
    apply_cvf(
	table_name => 'mrcoc_pre',
	key_field => null,
	cascade_field => 'aui_1',
	cui_field => 'cui_1');

    apply_cvf(
	table_name => 'mrcoc_pre',
	key_field => null,
	cascade_field => 'sab',
	cui_field => null);

    -- Log completion of procedure
    MEME_UTILITY.timing_stop;
    location := '410';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcoc_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

   WHEN OTHERS THEN
       mrd_release_operations_error(method,location,err_code,SQLERRM);
       RAISE mrd_release_exception;

END mrcoc_prepare;

/* PROCEDURE MRCONSO_PREPARE *******************************************
 * Creates and loads mrconso_pre which contains the
 * full release view of MRCONSO
 */
PROCEDURE mrconso_prepare
IS
    row_count           INTEGER;
BEGIN

    -- Initialize tracking parameters and start timing elements
    initialize_trace('MRCONSO_PREPARE');
    MEME_UTILITY.timing_start;

    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrconso_prepare';

    -- Log start
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrconso_prepare',
        detail => 'Starting mrconso_prepare.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Drop any existing mrconso_pre table
    location := '10';
    MEME_UTILITY.drop_it('table','mrconso_pre');

    -- Create mrconso_pre
    MEME_UTILITY.sub_timing_start;
    location := '20';
    MEME_UTILITY.exec (
        'CREATE TABLE mrconso_pre (
            cui         	VARCHAR2(10) NOT NULL,
            language    	VARCHAR2(10),
            ts          	VARCHAR2(1) NOT NULL,
            lui         	VARCHAR2(10) NOT NULL,
            stt         	VARCHAR2(5) NOT NULL,
            sui        		VARCHAR2(10) NOT NULL,
            ispref      	VARCHAR2(1) NOT NULL,
            aui         	VARCHAR2(10) NOT NULL,
            source_aui        	VARCHAR2(100),
            source_cui        	VARCHAR2(100),
            source_dui        	VARCHAR2(100),
            sab         	VARCHAR2(40) NOT NULL,
            tty         	VARCHAR2(20) NOT NULL,
            code        	VARCHAR2(100),
            string      	VARCHAR2(3000) NOT NULL,
  	    srl			NUMBER(12) NOT NULL,
  	    suppressible	VARCHAR2(20) NOT NULL,
  	    cvf			NUMBER(20)
         ) ');

    location := '25';
	-- Select from mrd_classes
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrconso_pre
            (cui,language,ts,lui,stt,sui,ispref,aui,
		source_aui,source_cui,source_dui,sab,tty,
		code,string,srl,suppressible)
        SELECT /*+ PARALLEL(a) */ cui, a.language,
               ''S'' AS ts,  a.lui, ''VO'' AS stt, a.sui, ''N'' AS ispref, aui,
		source_aui, source_cui, source_dui, a.root_source as sab, tty,
		code, string,
		restriction_level AS srl, suppressible
        FROM mrd_classes a, string_ui b, mrd_source_rank c
        WHERE a.sui = b.sui
          AND a.root_source = c.root_source
          AND a.expiration_date IS NULL
          AND c.expiration_date IS NULL
	  AND is_current=''Y''
	');

	COMMIT;
	
    -- Stop timing of first operation
    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '30';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrconso_prepare',
        detail => 'Table mrconso_pre created with ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    location := '32';
    MEME_UTILITY.drop_it('table','mrconso_pre_t1');

    -- Create mrconso_pre_t1
    MEME_UTILITY.sub_timing_start;
    location := '35';
    MEME_UTILITY.exec (
    'CREATE TABLE mrconso_pre_t1 (
       lat  VARCHAR2(100) NOT NULL,
       rank VARCHAR2(40),
       cui  VARCHAR2(10),
       aui  VARCHAR2(10),
       sui  VARCHAR2(10),
       lui  VARCHAR2(10))
     ');

    location := '40';
    row_count := MEME_UTILITY.exec (
    'INSERT INTO mrconso_pre_t1
     SELECT a.language,
     		MEME_RANKS.get_atom_release_rank(c.rank, 
	      		last_release_rank, sui , aui) as rank,
           cui, aui, sui, lui
      FROM mrd_classes a, mrd_source_rank b, mrd_termgroup_rank c
      WHERE a.root_source = b.root_source
        AND b.is_current = ''Y''
        AND b.source = substr(normalized_termgroup, 1, instr(normalized_termgroup,''/'')-1)
        AND a.tty = c.tty
        AND a.expiration_date IS NULL
        AND b.expiration_date IS NULL
        AND c.expiration_date IS NULL
        ');

	COMMIT;
	
    -- Stop timing of first operation
    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '45';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrconso_prepare',
        detail => 'Table mrconso_pre_t1 created with ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    MEME_UTILITY.drop_it('table','mrconso_pref_lui_4_cui_lat');

    MEME_UTILITY.sub_timing_start;
    location := '50';
    MEME_UTILITY.exec (
    'CREATE TABLE mrconso_pref_lui_4_cui_lat AS
     SELECT lui, cui, lat
     FROM mrconso_pre_t1
     WHERE (rank) IN
     (SELECT max(rank)
      FROM mrconso_pre_t1
      GROUP BY cui, lat)
      ');

    MEME_UTILITY.drop_it('table','mrconso_pref_sui_4_cui_lat_lui');
    location := '60';
    MEME_UTILITY.exec (
    'CREATE TABLE mrconso_pref_sui_4_cui_lat_lui AS
     SELECT sui, cui, lat
     FROM mrconso_pre_t1
     WHERE (rank) IN
     (SELECT max(rank)
      FROM mrconso_pre_t1
      GROUP BY cui, lat, lui)
      ');

    MEME_UTILITY.drop_it('table','mrconso_pref_aui_4_cui_lui_sui');
    location := '70';
    MEME_UTILITY.exec (
    'CREATE TABLE mrconso_pref_aui_4_cui_lui_sui AS
     SELECT aui, cui, lat
     FROM mrconso_pre_t1
     WHERE (rank) IN
     (SELECT max(rank)
      FROM mrconso_pre_t1
      GROUP BY cui, lat, lui, sui)
      ');

	COMMIT;
	
    location := '80';
    MEME_UTILITY.sub_timing_start;
    row_count := MEME_UTILITY.exec (
    'UPDATE mrconso_pre SET ts = ''P''
     WHERE (cui,language,lui) IN
            (SELECT cui,lat,lui FROM mrconso_pref_lui_4_cui_lat)
     ');

    -- Stop timing of first operation
    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '85';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrconso_prepare',
        detail => 'Set ts = P ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    location := '90';
    MEME_UTILITY.sub_timing_start;
    row_count := MEME_UTILITY.exec (
    'UPDATE mrconso_pre SET stt = ''PF''
     WHERE (cui,language,sui) IN
           (SELECT cui,lat,sui FROM mrconso_pref_sui_4_cui_lat_lui)
	');
	
    -- Stop timing of first operation
    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '95';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrconso_prepare',
        detail => 'Set stt = PF ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    location := '100';
    MEME_UTILITY.sub_timing_start;
    row_count := MEME_UTILITY.exec (
    'UPDATE mrconso_pre SET ispref = ''Y''
     WHERE (cui,language,aui) IN
           (SELECT cui,lat,aui FROM mrconso_pref_aui_4_cui_lui_sui)
    ');

    -- Stop timing of first operation
    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '105';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrconso_prepare',
        detail => 'Set ispref = Y ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

	COMMIT;
	
    MEME_UTILITY.drop_it('table','mrconso_pre_t1');
    MEME_UTILITY.drop_it('table','mrconso_pref_lui_4_cui_lat');
    MEME_UTILITY.drop_it('table','mrconso_pref_sui_4_cui_lat_lui');
    MEME_UTILITY.drop_it('table','mrconso_pref_aui_4_cui_lui_sui');
    

    MEME_SYSTEM.analyze('mrconso_pre');

    -- Set the cvf
    apply_cvf(
	table_name => 'mrconso_pre',
	key_field => 'aui',
	cascade_field => 'sui',
	cui_field => 'cui');

    apply_cvf(
	table_name => 'mrconso_pre',
	key_field => 'sui',
	cascade_field => 'sab',
	cui_field => 'cui');

    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log completion of activity
    location := '120';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrconso_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mrconso_prepare;

/* PROCEDURE MRCXT_PREPARE *****************************
 * Creates and loads mrcxt_pre which contains the
 * full release view of MRCXT
 */
PROCEDURE mrcxt_prepare
IS
    i					INTEGER;
    factor				INTEGER;
    factor2				INTEGER;
    row_count			INTEGER;
    row_multiplier		INTEGER;
    chunk_size			INTEGER := 50000;

    --
    -- Variables for computing CXN
    --
    CURSOR cxn_cur IS
        SELECT /*+ PARALLEL(a) */ a.aui, a.parent_treenum, a.root_source,
	      a.relationship_attribute
	FROM mrd_contexts a
	WHERE a.expiration_date IS NULL
	ORDER BY a.aui, a.parent_treenum,
	  a.root_source, a.relationship_attribute;
    cxn_var 		cxn_cur%ROWTYPE;
    cxn_key 		VARCHAR2(4000);
    cxn_prev_key 	VARCHAR2(4000);
    cxn			NUMBER := 0;

    PART_LENGTH		NUMBER := 9;
BEGIN
    -- Initialize tracking parameters and start timing elements
	initialize_trace('MRCXT_PREPARE');

    MEME_UTILITY.timing_start;

    EXECUTE IMMEDIATE 'ALTER SESSION SET sort_area_size=200000000';
    EXECUTE IMMEDIATE 'ALTER SESSION SET hash_area_size=200000000';
    

    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrcxt_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Starting mrcxt_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- use parallelized operations
    --EXECUTE IMMEDIATE
    --	'ALTER TABLE mrd_contexts PARALLEL 4';

    -- Recreate the mrcxt_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrcxt_pre_1');

    location := '20';
    MEME_UTILITY.exec(
    	'CREATE TABLE mrcxt_pre_1(
    	    aui		VARCHAR2(10),
    	    sab		VARCHAR2(40),
    	    aui_2	VARCHAR2(10),
    	    cxn    	NUMBER(12),
    	    cxl		CHAR(3), -- ANC, SIB, CCP, CHD
    	    rnk 	NUMBER(12),
	    treenum     VARCHAR2(1000),
    	    hcd		VARCHAR2(100),
    	    rela  	VARCHAR2(100),
	    xc		CHAR(1)
         ) STORAGE (INITIAL 1000M NEXT 100M)
	' );


    -- The first thing we need to do is figure
    -- out how to assign context numbers.
    --
    -- A context number defines a path to a root for a given RELA
    -- unless your source is MSH, GO, and NIC, then it defines a path
    -- to the root (irrespective of rela)
    location := '25';
    MEME_UTILITY.drop_it('table','mrcxt_pre_2');
    location := '30';
    MEME_UTILITY.exec(
    	'CREATE TABLE mrcxt_pre_2(
    	    aui		VARCHAR2(10),
       	    treenum     VARCHAR2(1000),
	    sab		VARCHAR2(40),
	    rela	VARCHAR2(100),
	    cxn		NUMBER(12)
         ) STORAGE (INITIAL 500M)
	' );

    --
    -- Assign CXN
    --
    location := '35.1';
    OPEN cxn_cur;
    cxn := 0;
    cxn_prev_key := 'start';
    LOOP
	FETCH cxn_cur INTO cxn_var;
	EXIT WHEN cxn_cur%NOTFOUND;
	cxn_key := cxn_var.aui || cxn_var.root_source;
	IF cxn_prev_key != cxn_key THEN
	    cxn := 1;
        ELSE
	    cxn := cxn + 1;
	END IF;
	cxn_prev_key := cxn_key;
	EXECUTE IMMEDIATE
	    'INSERT /*+ append */ INTO mrcxt_pre_2 (aui, treenum, sab, rela, cxn)
	     VALUES (:x,:x,:x,:x,:x)'
 	USING cxn_var.aui, cxn_var.parent_treenum, cxn_var.root_source,
		cxn_var.relationship_attribute, cxn;
    END LOOP;
    CLOSE cxn_cur;

    location := '35.2a';
    EXECUTE IMMEDIATE
	'ALTER TABLE mrcxt_pre_2 MOVE PARALLEL';

    location := '35.2a';
    EXECUTE IMMEDIATE
	'CREATE INDEX x_mrcxt_pre_2 ON mrcxt_pre_2 (aui) COMPUTE STATISTICS PARALLEL';

    location := '35.3';
    MEME_SYSTEM.analyze('mrcxt_pre_2');

    location := '35.35';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Assigned initial CXN values',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

 	--
    -- The code above assigns context numbers based on aui
    -- which means that each aui has its various contexts
    -- numbered starting with 1.  This can create a problem in
    -- MRCXT if multiple atoms have the same CUI,SUI,SAB,CODE
    -- because it will produce cases of overlapping contexts.
    --
    -- The code below fixes the problem by reassigning context
    -- numbers in cases where overlap is found
    --
    MEME_UTILITY.sub_timing_start;
    row_count := 0;
    LOOP
  	-- Prepare a temporary table
        location := '35.4';
    	MEME_UTILITY.drop_it('table','mrcxt_pre_3');
    	MEME_UTILITY.exec(
    	  'CREATE TABLE mrcxt_pre_3 (
		aui_1	VARCHAR2(10),
		aui_2	VARCHAR2(10))'
	  );

	-- Find cases of different auis sharing the same MRCXT key
	-- fields: cui, sui, code, source.
	--
	-- What is happening here is tricky.  We are first mapping
	-- any overlapping auis to the min atom id with the
	-- shared MRCXT key fields.
	--
	-- Then we take only case per aui_2 at at time.
	-- This is to handle situations where there are more than
	-- two auis with the same cui, sui, code, and source.
	--
        location := '35.5';
 	EXECUTE IMMEDIATE
	    'INSERT INTO mrcxt_pre_3 (aui_1,aui_2)
	     SELECT min(aui_1), aui_2 FROM
	       (SELECT a.aui as aui_1, min(b.aui) as aui_2
	        FROM mrd_classes a, mrd_classes b,
		    mrcxt_pre_2 c, mrcxt_pre_2 d
	     	WHERE a.aui = c.aui
		  AND b.aui = d.aui
		  AND a.aui > b.aui
		  AND c.cxn = d.cxn
		  AND a.code = b.code
		  AND a.sui = b.sui
		  AND a.cui = b.cui
		  AND a.root_source = b.root_source
		  AND a.expiration_date IS NULL
		  AND b.expiration_date IS NULL
		GROUP BY a.aui)
	      GROUP BY aui_2';

	-- If no cases are found, we are finished
        location := '35.6';
	EXIT WHEN SQL%ROWCOUNT = 0;

	-- At this point we have pairs of auis that have shared MRCXT
	-- key fields.  What we do is find the maximum context number
	-- for the lower of the two auis and we add that value to
	-- the context numbers for the higher aui calculated earlier.
	-- Suppose we have this (in mrcxt_pre_2):
	--
	--  aui	cxn
	--    1		 1
	--    1		 2
	--    1		 3
	--    2		 1
	--    2		 2
	--
	-- And in mrcxt_pre_3 we have this
	--
	--  aui_1   aui_2
	--    2		 1
	--
	-- The result of the next query will be to assign a new context
	-- number range for aui 2 based on the max context number
	-- for aui 1.  The result will be (in mrcxt_pre_2):
	--
	--  aui	cxn
	--    1		 1
	--    1		 2
	--    1		 3
	--    2		 4
	--    2		 5
	--
	location := '35.7';
	EXECUTE IMMEDIATE
  	    'UPDATE mrcxt_pre_2 a set cxn =
	       (SELECT a.cxn+max_cxn FROM
		 (SELECT aui_1,max(cxn) as max_cxn
		  FROM mrcxt_pre_2 b, mrcxt_pre_3
		  WHERE b.aui = aui_2
		  GROUP BY aui_1)
		WHERE aui_1 = a.aui)
	     WHERE aui IN (SELECT aui_1 FROM mrcxt_pre_3) ';

        row_count := row_count + SQL%ROWCOUNT;

	-- To drop the table
	COMMIT;

    END LOOP;

    location := '35.9';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Fixed cases of different auis sharing the same MRCXT key ('||row_count||')',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get Top-level ANC rows
    -- Since the tree-top has no rela, we do not need to worry
    -- about joining to it here (on mrcxt_pre_2)
    MEME_UTILITY.sub_timing_start;
    location := '40.1';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrcxt_pre_1
	    (aui, aui_2, sab, cxn, cxl, rnk, hcd, rela, treenum)
	 SELECT /*+ USE_HASH(par,cxn) */
		cxn.aui, par.aui, cxn.sab, cxn.cxn, ''ANC'', 1,
		null, null, null
	 FROM mrd_contexts par, mrcxt_pre_2 cxn
	 WHERE SUBSTR(cxn.treenum,1,8) = par.aui
	   AND par.parent_treenum is null
	   AND par.expiration_date IS NULL
	   AND par.root_source = cxn.sab
	   AND cxn.treenum is not null
	');

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '40.2';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Finished generating top-level ANC rows, ' ||
		row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Now, get all remaining ANC rows
    MEME_UTILITY.sub_timing_start;

    -- Get  ANC rows
    -- Make sure that you only get the exact ancestor, such that
    -- The ancestors parent_treenum appended to its atom is
    -- contained within the other atoms treenum
    --
    -- This starts with ANC 2 so we do not need to consider
    -- the case where the treenum has no '.' character.
    --
    -- If a source (like UWDA) has multiple instances of the same
    -- path to the root with different relas, we will need to
    -- only pick up the CXN from mrcxt_pre_2 for the stated RELA.
    -- i.e. the rela of the aui_2's context should match the
    -- rela of the CXN.  Sources that do not have structured rela
    -- trees ( GO, NIC) should ignore this rule.

    MEME_UTILITY.sub_timing_start;
    i := 1;
    row_count := 0;
    LOOP

	factor := (i * PART_LENGTH)-1;

        location := '40.25';
        EXECUTE IMMEDIATE
	--
	-- It is important for this query to join on mrcxt_pre_2
	-- using the entire path and not parent_treenum and aui separately
 	--
	'INSERT /*+ APPEND */ INTO mrcxt_pre_1
	    (aui, aui_2, sab, cxn, cxl, rnk, hcd, rela, treenum)
	  SELECT /*+ PARALLEL(ccp) */
	     ccp.aui, anc.aui, ccp.root_source, cxn.cxn, ''ANC'', ' || (i+1) || ',
	     anc.hierarchical_code,  anc.relationship_attribute,
	     ''''
	 FROM mrd_contexts ccp, mrcxt_pre_2 cxn, mrd_contexts anc
	 WHERE length(ccp.parent_treenum) > ' || factor || '
	   AND length(cxn.treenum) > ' || factor || '
	   AND length(anc.parent_treenum) = ' || factor || '
	   AND ccp.parent_treenum||''.''||ccp.aui = cxn.treenum||''.''||cxn.aui
	   AND ccp.root_source = cxn.sab
	   AND substr(ccp.parent_treenum,1,' || factor || '+9) =
		 anc.parent_treenum || ''.'' || anc.aui
	   AND anc.root_source = ccp.root_source
	   AND (ccp.root_source IN (SELECT root_source FROM mrd_source_rank
				    WHERE expiration_date IS NULL
				      AND normalized_source = source
  				      AND context_type like ''%IGNORE-RELA%'')
		OR
	        (NVL(ccp.relationship_attribute,''null'') =
	         NVL(cxn.rela,''null'') AND
	         NVL(ccp.relationship_attribute,''null'') =
	         NVL(anc.relationship_attribute,''null'')
	        ) )
	   AND ccp.expiration_date IS NULL
	   AND anc.expiration_date IS NULL';

        row_count :=  SQL%ROWCOUNT;

	i := i + 1;

    	MEME_UTILITY.sub_timing_stop;
    	location := '40.27';
    	log_progress(
            authority => 'RELEASE',
            activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
            detail => 'Finished generating ANC (' || i || ') rows, ' ||
		SQL%ROWCOUNT || ' rows processed.',
            transaction_id => 0,
            work_id => 0,
            elapsed_time => MEME_UTILITY.sub_elapsed_time);

    	COMMIT;

	EXIT WHEN row_count = 0;

    END LOOP;

    -- Get CCP rows
    -- The rela from mrd_contexts must match mrcxt_pre_2
    -- otherwise sources that have multiple instances
    -- of the same path to the root will wind up
    -- with two CCP rows for the same CUI|SUI|SAB|CODE|CXN
    --
    -- Only keep the CCP row where the rela matches the CXN's rela
    -- This is true REGARDLESS of source because it is the CCP row.
    MEME_UTILITY.sub_timing_start;
    location := '40.3';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrcxt_pre_1
	    (aui, aui_2, sab, cxn, cxl, hcd, rela, treenum)
	 SELECT /*+ PARALLEL(ccp) */ ccp.aui, ccp.aui,
		root_source, cxn.cxn, ''CCP'',
		hierarchical_code,  relationship_attribute,
		parent_treenum as treenum
	 FROM mrd_contexts ccp, mrcxt_pre_2 cxn
	 WHERE expiration_date IS NULL
	   AND ccp.parent_treenum || ''.'' || ccp.aui =
		cxn.treenum || ''.'' || cxn.aui
	   AND ccp.root_source = cxn.sab
	   AND NVL(ccp.relationship_attribute,''null'') =
		NVL(cxn.rela,''null'') ');

    COMMIT;

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '40.4';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Finished generating CCP rows, ' ||
		row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get SIB rows
    -- In general, there is a requirement
    -- that SIB rows must have the same RELA as the CCP atom.
    -- The exceptional cases are  GO, and NIC, where this does not hold.
    --
    -- Here we join the aui_2's rela to the CXN's rel
    -- which by the logic above will also be the aui's RELA
    -- (i.e. b.rela = c.rela)
    --
    -- NOTE: If this query fills up the log, check release_mode
    -- especially for C MSH
    -- select count(*), root_source, release_mode from mrd_contexts
    --  group by root_source, release_mode
    --
    MEME_UTILITY.sub_timing_start;
    location := '40.5';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrcxt_pre_1
	    (aui, aui_2, sab, cxn, cxl, hcd, rela,treenum)
	 SELECT
		ccp.aui, sib.aui, ccp.root_source, cxn.cxn, ''SIB'',
		sib.hierarchical_code,  sib.relationship_attribute,
		ccp.parent_treenum as treenum
	 FROM mrd_contexts ccp, mrd_contexts sib, mrcxt_pre_2 cxn
	 WHERE ccp.parent_treenum = sib.parent_treenum
	   AND ccp.aui != sib.aui
	   AND ccp.root_source = sib.root_source
	   AND ccp.parent_treenum || ''.'' || ccp.aui =
		cxn.treenum || ''.'' || cxn.aui
	   AND ccp.root_source = cxn.sab
	   AND ((NVL(ccp.relationship_attribute,''null'') =
	         NVL(cxn.rela,''null'') AND
	         NVL(ccp.relationship_attribute,''null'') =
	         NVL(sib.relationship_attribute,''null'') ) OR
		ccp.root_source in (SELECT root_source FROM mrd_source_rank
				    WHERE expiration_date IS NULL
				      AND normalized_source = source
  				      AND context_type like ''%IGNORE-RELA%''))
           AND substr(ccp.release_mode,2,1) = ''1''
           AND substr(sib.release_mode,2,1) = ''1''
	   AND ccp.expiration_date IS NULL
	   AND sib.expiration_date IS NULL
	');

    COMMIT;

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '40.6';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Finished generating SIB rows, ' ||
		row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- The algorithm says that MSH SIB rows should have null RELA fields
    MEME_UTILITY.sub_timing_start;
    location := '40.5.2';
    row_count := MEME_UTILITY.exec(
	'UPDATE mrcxt_pre_1
	 SET rela = null
	 WHERE sab like ''MSH%''
	   AND cxl = ''SIB'' AND rela IS NOT NULL
	');

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '40.6';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Finished setting SIB RELA values to null for MSH, ' ||
		row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get CHD rows (not children of treetops)
    --
    -- In the general case, like SIB rows, CHD rows should have
    -- the same RELA as the CCP row to which they belong.  There
    -- are two exceptions:  MSH and tree-top CCP atoms (which are
    -- identified by having null parent_treenums in mrd_contexts).
    -- (here b.aui is the CCP atom)
    MEME_UTILITY.sub_timing_start;
    location := '40.7';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrcxt_pre_1
	    (aui, aui_2, sab, cxn, cxl, hcd, rela,treenum)
	 SELECT ccp.aui, chd.aui, ccp.root_source, cxn.cxn, ''CHD'',
		  chd.hierarchical_code,  chd.relationship_attribute,
		  ccp.parent_treenum as treenum
	 FROM mrd_contexts chd, mrd_contexts ccp, mrcxt_pre_2 cxn
	 WHERE chd.parent_treenum = ccp.parent_treenum || ''.'' || ccp.aui
	   AND chd.root_source = ccp.root_source
	   AND ccp.parent_treenum || ''.'' || ccp.aui = cxn.treenum || ''.'' || cxn.aui
	   AND ccp.root_source = cxn.sab
	   AND ((NVL(chd.relationship_attribute,''null'') =
	         NVL(cxn.rela,''null'') AND
	         NVL(chd.relationship_attribute,''null'') =
	         NVL(ccp.relationship_attribute,''null'') ) OR
		chd.root_source in (SELECT root_source FROM mrd_source_rank
				    WHERE expiration_date IS NULL
				      AND normalized_source = source
  				      AND context_type like ''%IGNORE-RELA%''))
	   AND chd.expiration_date IS NULL
	   AND ccp.expiration_date IS NULL
	');

    -- Get CHD rows (children of treetops)
    --
    -- Tree-top CCP rows do not have the same relationship_attribute
    -- restriction as above becuae they do not have parents and therefore
    -- have a null relationship_attribute value.
    location := '40.8';
    row_count := row_count + MEME_UTILITY.exec(
	'INSERT INTO mrcxt_pre_1
	    (aui, aui_2, sab, cxn, cxl, hcd, rela,treenum)
	 SELECT ccp.aui, chd.aui, ccp.root_source, cxn.cxn, ''CHD'',
		  chd.hierarchical_code,  chd.relationship_attribute,
		  ccp.parent_treenum as treenum
		  --, ''2'' as treenum_2
	 FROM mrd_contexts chd, mrd_contexts ccp, mrcxt_pre_2 cxn
	 WHERE chd.parent_treenum = ccp.aui
	   AND chd.expiration_date IS NULL
	   AND ccp.expiration_date IS NULL
	   AND ccp.aui = cxn.aui AND ccp.root_source = cxn.sab
	   AND NVL(ccp.parent_treenum,''0'') = NVL(cxn.treenum,''0'')
	');

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '40.9';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Finished generating CHD rows, ' ||
		row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    COMMIT;

    -- Analyze mrcxt_pre_1
    location := '125';
    MEME_SYSTEM.analyze('mrcxt_pre_1');

    -- Set XC rows for CCP, SIB, CHD rows
    -- This operation should be performed for the aui_2
    -- not the aui_1.  the treenum is the tree number
    -- of the CCP row for this context.

    -- For CCP,SIB rows, look see if there any
    -- rows in mrd_contexts with a parent_treenum of
    -- of   "treenum || '.' || aui_2"
    --
    -- The union is to get cases where the row
    -- is the immediate child of the tree-top

    MEME_UTILITY.sub_timing_start;
    location := '130.1';
    
    row_count := MEME_UTILITY.exec (
	'UPDATE /*+ PARALLEL(a) */ mrcxt_pre_1 a
	 SET xc = ''+''
	 WHERE cxl in (''CCP'',''SIB'')
	   AND treenum || ''.'' || aui_2 IN
	   (SELECT parent_treenum
	    FROM mrd_contexts
	    WHERE expiration_date IS NULL
	    UNION SELECT ''.'' || parent_treenum
	    FROM mrd_contexts
	    WHERE expiration_date IS NULL
	      AND parent_treenum not like ''%.%'')
	');

    COMMIT;

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '130.2';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Finished processing xc (SIB,CCP), ' ||
			row_count || ' rows completed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    MEME_UTILITY.sub_timing_start;
    -- For CHD rows, look see if there any
    -- rows in mrd_contexts with a parent_treenum of
    -- of   "treenum || '.' || aui || '.' || aui_2"
    --
    -- The UNION clause finds cases where the parent treenumber
    -- has exactly 2 levels, as in 12345.6789.  We are looking
    -- for cases where the CCP is the tree-top and the CHD
    -- is below it, so if it has any children the parent_treenum
    -- will be the aui.aui_2 and the treenum will be null.
    location := '130.3';
    row_count := MEME_UTILITY.exec (
	'UPDATE /*+ PARALLEL(a) */ mrcxt_pre_1 a
	 SET xc = ''+''
	 WHERE cxl = ''CHD''
	   AND treenum || ''.'' || aui || ''.'' || aui_2 IN
	   (SELECT parent_treenum
	    FROM mrd_contexts
	    WHERE expiration_date IS NULL
	    UNION SELECT ''.'' || parent_treenum
	    FROM mrd_contexts
	    WHERE expiration_date IS NULL
	      AND INSTR(parent_treenum,''.'',1,2) = 0
	      AND INSTR(parent_treenum,''.'',1,1) != 0 )
	');

    COMMIT;

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '150';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Finished processing xc (CHD), ' ||
			row_count || ' rows completed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    COMMIT;

    -- We now have everything, we just need to join with
    -- mrd_classes to pick up CUIs, etc, and the strings.
    MEME_UTILITY.sub_timing_start;
    location := '160.1';
    MEME_UTILITY.drop_it('table','mrcxt_pre');
    location := '160.2';
    MEME_UTILITY.exec(
    	'CREATE TABLE mrcxt_pre(
            cui                VARCHAR2(10),
            sui                VARCHAR2(10),
            aui                VARCHAR2(10),
            sab                VARCHAR2(40),
            scd                VARCHAR2(100),
            cxn                NUMBER,
            cxl                CHAR(3), -- ANC,CCP,SIB,CHD
            rnk                NUMBER(3),
            cxs                VARCHAR2(3000), -- string
            cui2               VARCHAR2(10),
            aui2               VARCHAR2(10),
            hcd                VARCHAR2(100),
            rela               VARCHAR2(100),
            xc                 CHAR(1),
            cvf                NUMBER(20)
        ) STORAGE (INITIAL 1000M NEXT 500M)  ' );

    -- Join with mrd_classes
    -- The SAB field should come from mrcxt_pre_1.
    location := '170';
    row_count := MEME_UTILITY.exec(
	'INSERT /*+ append */ INTO mrcxt_pre
             (cui, sui, aui, sab, scd, cxn, cxl, rnk, cxs,
             cui2, aui2, hcd, rela, xc)
         SELECT /*+ PARALLEL(c) INDEX(a) INDEX(b) INDEX(d) */
           a.cui, a.sui, c.aui, c.sab, a.code,
           c.cxn, c.cxl, c.rnk, d.string,
           b.cui, c.aui_2, c.hcd, c.rela, c.xc
         FROM mrd_classes a, mrd_classes b, mrcxt_pre_1 c, string_ui d
         WHERE a.aui = c.aui and b.aui = c.aui_2
           AND b.sui = d.sui
           AND a.expiration_date IS NULL
           AND b.expiration_date IS NULL
	' );

    COMMIT;

    -- Log final step
    MEME_UTILITY.sub_timing_stop;
    location := '180';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Finished building mrcxt_pre, ' ||
			row_count || ' rows completed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Clean up table
    location := '190';
    MEME_UTILITY.drop_it('table','mrcxt_pre_1');
	location := '195';
    MEME_UTILITY.drop_it('table','mrcxt_pre_2');
    location := '200';
    MEME_UTILITY.drop_it('table','mrcxt_pre_3');

    -- reset parallel to default
    --EXECUTE IMMEDIATE
    --	'ALTER TABLE mrd_contexts NOPARALLEL';

    -- Set cvf
    apply_cvf(
	table_name => 'mrcxt_pre',
	key_field => null,
	cascade_field => 'aui',
	cui_field => 'cui');

    apply_cvf(
	table_name => 'mrcxt_pre',
	key_field => null,
	cascade_field => 'sab',
	cui_field => null);

    -- Log completion of activity
    MEME_UTILITY.timing_stop;
    location := '280';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcxt_prepare',
        detail => 'Procedure completed successfully, ' ||
  		  	row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

   WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
       RAISE mrd_release_exception;

END mrcxt_prepare;

/* PROCEDURE MRHIER_PREPARE *****************************
 * Creates and loads mrhier_pre which contains the
 * full release view of MRHIER
 */
PROCEDURE mrhier_prepare
IS
    i					INTEGER;
    row_count			INTEGER;
    row_multiplier		INTEGER;
    chunk_size			INTEGER := 50000;

    --
    -- Variables for computing CXN
    --
    CURSOR cxn_cur IS
        SELECT a.aui, a.parent_treenum, a.root_source,
	      a.relationship_attribute
	FROM mrd_contexts a
	WHERE a.expiration_date IS NULL
	ORDER BY a.aui, a.parent_treenum,
		 a.root_source, a.relationship_attribute;
    cxn_var 		cxn_cur%ROWTYPE;
    cxn_key 		VARCHAR2(4000);
    cxn_prev_key 	VARCHAR2(4000);
    cxn			NUMBER := 0;

BEGIN
    -- Initialize tracking parameters and start timing elements
	initialize_trace('MRHIER_PREPARE');
    MEME_UTILITY.timing_start;
    

    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrhier_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrhier_prepare',
        detail => 'Starting mrhier_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- use parallelized operations
    --EXECUTE IMMEDIATE
    --	'ALTER TABLE mrd_contexts PARALLEL 4';

    -- Recreate the mrhier_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrhier_pre');

    location := '20';
    MEME_UTILITY.exec(
    	'CREATE TABLE mrhier_pre(
    	    cui		VARCHAR2(10),
    	    aui		VARCHAR2(10),
    	    cxn		NUMBER(12),
    	    paui	VARCHAR2(10),
    	    sab		VARCHAR2(40),
    	    rela  	VARCHAR2(100),
	    ptr		VARCHAR2(1000),
    	    hcd		VARCHAR2(100),
	    cvf		NUMBER(20)
         ) STORAGE (INITIAL 1000M NEXT 100M)
	' );

    -- reset parallel to default
    --EXECUTE IMMEDIATE
    --	'ALTER TABLE mrd_contexts NOPARALLEL';

    -- The first thing we need to do is figure
    -- out how to assign context numbers.
    --
    -- A context number defines a path to a root for a given RELA
    -- unless your source is GO, and NIC, then it defines a path
    -- to the root (irrespective of rela)
    location := '25';
    MEME_UTILITY.drop_it('table','mrhier_pre_1');
    location := '30';
    MEME_UTILITY.exec(
    	'CREATE TABLE mrhier_pre_1(
    	    aui		VARCHAR2(10),
       	    treenum     VARCHAR2(1000),
	    sab		VARCHAR2(40),
	    rela	VARCHAR2(100),
	    cxn		NUMBER(12)
         ) STORAGE (INITIAL 100M)
	' );

    --
    -- Assign CXN
    --
    location := '35.1';
    OPEN cxn_cur;
    cxn := 0;
    cxn_prev_key := 'start';
    LOOP
	FETCH cxn_cur INTO cxn_var;
	EXIT WHEN cxn_cur%NOTFOUND;
	cxn_key := cxn_var.aui || cxn_var.root_source;
	IF cxn_prev_key != cxn_key THEN
	    cxn := 1;
        ELSE
	    cxn := cxn + 1;
	END IF;
	cxn_prev_key := cxn_key;
	EXECUTE IMMEDIATE
	    'INSERT INTO mrhier_pre_1 (aui, treenum, sab, rela, cxn)
	     VALUES (:x,:x,:x,:x,:x)'
 	USING cxn_var.aui, cxn_var.parent_treenum, cxn_var.root_source,
		cxn_var.relationship_attribute, cxn;
    END LOOP;
    CLOSE cxn_cur;

    location := '35.2';
    EXECUTE IMMEDIATE
	'CREATE INDEX x_mrhier_pre_1 ON mrhier_pre_1 (aui)';

    location := '35.3';
    MEME_SYSTEM.analyze('mrhier_pre_1');

 	--
    -- The code above assigns context numbers based on aui
    -- which means that each aui has its various contexts
    -- numbered starting with 1.  This can create a problem in
    -- MRHIER if multiple atoms have the same CUI,SUI,SAB,CODE
    -- because it will produce cases of overlapping contexts.
    --
    -- The code below fixes the problem by reassigning context
    -- numbers in cases where overlap is found
    --
    MEME_UTILITY.sub_timing_start;
    LOOP
  	-- Prepare a temporary table
        location := '35.4';
    	MEME_UTILITY.drop_it('table','mrhier_pre_2');
    	MEME_UTILITY.exec(
    	  'CREATE TABLE mrhier_pre_2 (
		aui_1	VARCHAR2(10),
		aui_2	VARCHAR2(10))'
	  );

	-- Find cases of different auis sharing the same MRHIER key
	-- fields: cui, sui, code, source.
	--
	-- What is happening here is tricky.  We are first mapping
	-- any overlapping auis to the min atom id with the
	-- shared MRHIER key fields.
	--
	-- Then we take only case per aui_2 at at time.
	-- This is to handle situations where there are more than
	-- two auis with the same cui, sui, code, and source.
	--
        location := '35.5';
 	EXECUTE IMMEDIATE
	    'INSERT INTO mrhier_pre_2 (aui_1,aui_2)
	     SELECT min(aui_1), aui_2 FROM
	       (SELECT a.aui as aui_1, min(b.aui) as aui_2
	        FROM mrd_classes a, mrd_classes b,
		    mrhier_pre_1 c, mrhier_pre_1 d
	     	WHERE a.aui = c.aui
		  AND b.aui = d.aui
		  AND a.aui > b.aui
		  AND c.cxn = d.cxn
		  AND a.code = b.code
		  AND a.sui = b.sui
		  AND a.cui = b.cui
		  AND a.root_source = b.root_source
		  AND a.expiration_date IS NULL
		  AND b.expiration_date IS NULL
		GROUP BY a.aui)
	      GROUP BY aui_2';

	-- If no cases are found, we are finished
        location := '35.6';
	EXIT WHEN SQL%ROWCOUNT = 0;

	-- At this point we have pairs of auis that have shared MRHIER
	-- key fields.  What we do is find the maximum context number
	-- for the lower of the two auis and we add that value to
	-- the context numbers for the higher aui calculated earlier.
	-- Suppose we have this (in mrhier_pre_1):
	--
	--  aui	cxn
	--    1		 1
	--    1		 2
	--    1		 3
	--    2		 1
	--    2		 2
	--
	-- And in mrhier_pre_2 we have this
	--
	--  aui_1   aui_2
	--    2		 1
	--
	-- The result of the next query will be to assign a new context
	-- number range for aui 2 based on the max context number
	-- for aui 1.  The result will be (in mrhier_pre_1):
	--
	--  aui	cxn
	--    1		 1
	--    1		 2
	--    1		 3
	--    2		 4
	--    2		 5
	--
	location := '35.7';
	EXECUTE IMMEDIATE
  	    'UPDATE mrhier_pre_1 a set cxn =
	       (SELECT a.cxn+max_cxn FROM
		 (SELECT aui_1,max(cxn) as max_cxn
		  FROM mrhier_pre_1 b, mrhier_pre_2
		  WHERE b.aui = aui_2
		  GROUP BY aui_1)
		WHERE aui_1 = a.aui)
	     WHERE aui IN (SELECT aui_1 FROM mrhier_pre_2) ';

	-- To drop the table
	COMMIT;

    END LOOP;

    location := '35.9';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrhier_prepare',
        detail => 'Fixed cases of different auis sharing the same MRHIER key',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- We now have everything, we just need to join with
    -- mrd_classes to pick up CUIs, etc, and the context number.
    MEME_UTILITY.sub_timing_start;
    location := '40';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrhier_pre
	    (cui, aui, cxn, paui, sab, rela, ptr, hcd)
	 SELECT /*+ PARALLEL(b) */ a.cui, b.aui, cxn,
		SUBSTR(parent_treenum,
		       INSTR(parent_treenum,''.'',-1)+1) as paui,
		b.root_source, b.relationship_attribute,
		parent_treenum, b.hierarchical_code
	 FROM mrd_classes a, mrd_contexts b, mrhier_pre_1 c
	 WHERE a.aui = b.aui and b.aui = c.aui
	   AND b.root_source = c.sab
	   AND NVL(b.parent_treenum,''0'') = NVL(treenum,''0'')
	   AND NVL(b.relationship_attribute,''null'') =
	       NVL(c.rela,''null'')
	   AND a.expiration_date IS NULL
	   AND b.expiration_date IS NULL
	' );

    COMMIT;

    -- Log final step
    MEME_UTILITY.sub_timing_stop;
    location := '50';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrhier_prepare',
        detail => 'Finished building mrhier_pre, ' ||
			row_count || ' rows completed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Set cvf
    apply_cvf(
	table_name => 'mrhier_pre',
	key_field => null,
	cascade_field => 'aui',
	cui_field => 'cui');

    apply_cvf(
	table_name => 'mrhier_pre',
	key_field => null,
	cascade_field => 'sab',
	cui_field => null);

   -- Log completion of activity
    MEME_UTILITY.timing_stop;
    location := '60';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrhier_prepare',
        detail => 'Procedure completed successfully, ' ||
  		  	row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

   WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
       RAISE mrd_release_exception;

END mrhier_prepare;

/* PROCEDURE MRDOC_PREPARE *****************************/

PROCEDURE mrdoc_prepare
IS
    row_count			INTEGER;
    row_multiplier		INTEGER;
    chunk_size			INTEGER := 2000;
BEGIN

	-- Initialize tracking parameters and start timing elements
   	initialize_trace('MRDOC_PREPARE');
   	MEME_UTILITY.timing_start;

    
    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrdoc_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdoc_prepare',
        detail => 'Starting mrdoc_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrdoc_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrdoc_pre');

    location := '20';
    MEME_UTILITY.exec(
	'CREATE TABLE  mrdoc_pre (
 		key_qualifier		VARCHAR2(100),
 		value			VARCHAR2(4000),
 		key			VARCHAR2(100),
 		description		VARCHAR2(1000))'
	);

    -- Get properties
    MEME_UTILITY.sub_timing_start;
    location := '30';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrdoc_pre(key_qualifier,value,key,description)
	 SELECT key_qualifier,value,key,description
	 FROM mrd_properties
	 WHERE key_qualifier not in (''MRSAT'',''MRFILES'',''MRCOLS'',''MEDLINE'')
	   AND expiration_date IS NULL'
	);

    -- Log row count
    location := '40';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdoc_prepare',
        detail => 'Finished generating mrdoc_pre ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Remove bogus TTY, RELA, ATN
    MEME_UTILITY.sub_timing_start;
    location := '35.1';
    row_count := MEME_UTILITY.exec(
    	'DELETE FROM mrdoc_pre
	 WHERE key_qualifier=''TTY'' and value IN
	   (SELECT value FROM mrdoc_pre WHERE key_qualifier=''TTY''
	    MINUS SELECT DISTINCT tty FROM mrd_classes a
	    WHERE expiration_date IS NULL)');

    --
    -- don't forget about MAPATN
    --
    location := '35.2';
    row_count := row_count + MEME_UTILITY.exec(
    	'DELETE FROM mrdoc_pre
	 WHERE key_qualifier=''ATN''
	   AND value NOT LIKE ''MED____''
	   AND value IS NOT NULL
	   AND value NOT IN (''DA'',''MR'',''ST'',''LT'',''NH'')
	   AND value IN
	   (SELECT value FROM mrdoc_pre WHERE key_qualifier=''ATN''
	    MINUS
	    (SELECT /*+ parallel(a) */ DISTINCT attribute_name
	     FROM mrd_attributes a
	     WHERE expiration_date IS NULL
         UNION
          SELECT ''MEMBERSTATUS'' from dual
           UNION
		SELECT DISTINCT substr(attribute_value, instr(attribute_value,''~'') +1,
                instr(attribute_value,''~'',1,2)-instr(attribute_value,''~'')-1)
     FROM attributes WHERE attribute_name=''CV_MEMBER''
     AND attribute_value not like ''<>Long_Attribute<>:%''
     UNION
     SELECT DISTINCT substr(text_value,instr(text_value,''~'')+1,
                instr(text_value,''~'',1,2)-instr(text_value,''~'')-1)
     FROM attributes a, stringtab b
     WHERE to_number(substr(attribute_value,20)) = string_id
       AND attribute_name=''CV_MEMBER'' 
       AND attribute_value like ''<>Long_Attribute<>:%''
    UNION
       SELECT DISTINCT SUBSTR(attribute_value, INSTR(attribute_value,''~'', 1, 1)+1,
       INSTR(attribute_value,''~'',1,2)-INSTR(attribute_value,''~'',1,1)-1) atn
       FROM attributes where attribute_name=''SUBSET_MEMBER'' and tobereleased in (''Y'',''y'')
     AND attribute_value not like ''<>Long_Attribute<>:%''
     UNION
     SELECT DISTINCT substr(text_value,instr(text_value,''~'')+1,
                instr(text_value,''~'',1,2)-instr(text_value,''~'')-1)
     FROM attributes a, stringtab b
     WHERE to_number(substr(attribute_value,20)) = string_id
       AND attribute_name=''SUBSET_MEMBER''
       AND attribute_value like ''<>Long_Attribute<>:%''
))');

    --
    -- RELA in MRHIER, MRREL, MRMAP
    --
    location := '35.3';
    row_count := row_count + MEME_UTILITY.exec(
    	'DELETE FROM mrdoc_pre
	 WHERE key_qualifier=''RELA''
	   AND value IS NOT NULL
	   AND value IN
	   (SELECT value FROM mrdoc_pre WHERE key_qualifier=''RELA''
	    MINUS
	    (SELECT DISTINCT relationship_attribute FROM mrd_relationships
	     WHERE expiration_date IS NULL
	     UNION
	     SELECT DISTINCT relationship_attribute FROM mrd_contexts
	     WHERE expiration_date IS NULL
	     UNION
	     SELECT DISTINCT
	      SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 4) + 1,
                     (INSTR(attribute_value, ''~'', 1, 5) -
                     INSTR(attribute_value, ''~'', 1, 4)) -1 )
	     FROM mrd_attributes
  	     WHERE attribute_name=''XMAP'' AND expiration_date IS NULL
			UNION
              SELECT DISTINCT inverse_rel_attribute from inverse_rel_attributes
                                        where relationship_attribute in ( select distinct
              SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 4) + 1,
                     (INSTR(attribute_value, ''~'', 1, 5) -
                     INSTR(attribute_value, ''~'', 1, 4)) -1 )
             FROM mrd_attributes
             WHERE attribute_name=''XMAP'' AND expiration_date IS NULL)))' );

    -- Assume REL are correct

    -- Log row count
    location := '40';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdoc_prepare',
        detail => 'Finished removing obsolete mrdoc_pre data: ' ||
			row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get RELA inverses
    MEME_UTILITY.sub_timing_start;
    location := '50';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrdoc_pre(key_qualifier,value,key,description)
	 SELECT ''RELA'', relationship_attribute,''rela_inverse'',
		inverse_rel_attribute
	 FROM inverse_rel_attributes a
	 WHERE nvl(relationship_attribute,''null'') IN
	  (SELECT NVL(value,''null'') FROM mrdoc_pre b
	   WHERE key = ''expanded_form''
	   AND key_qualifier = ''RELA'')'
	);

    -- Log row count
    location := '60';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdoc_prepare',
        detail => 'Finished generating RELA inverses ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get REL inverses
    MEME_UTILITY.sub_timing_start;
    location := '70';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrdoc_pre(key_qualifier,value,key,description)
	 SELECT ''REL'', a.release_name, ''rel_inverse'', b.release_name
	 FROM inverse_relationships a, inverse_relationships b
	 WHERE a.inverse_name = b.relationship_name
	   AND a.relationship_name NOT IN (''SFO'',''LFO'',''NT?'',
			''BT?'',''LEX'',''XS'')'
	);

    -- Log row count
    location := '80';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdoc_prepare',
        detail => 'Finished generating REL inverses ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get UMLS REL
    MEME_UTILITY.sub_timing_start;
    location := '90';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrdoc_pre(key_qualifier,value,key,description)
	 SELECT /*+ USE_NL(a,b) */
	 DISTINCT ''REL'', source_cui,
		''snomedct_rel_mapping'', attribute_value
	 FROM mrd_attributes a, mrd_classes b
	 WHERE a.attribute_name = ''UMLSREL''
	   AND a.ui = b.aui
	   AND a.expiration_date IS NULL
	   AND b.expiration_date IS NULL '
	);

    -- Log row count
    location := '100';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdoc_prepare',
        detail => 'Finished generating UMLS REL ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get UMLS RELA
    MEME_UTILITY.sub_timing_start;
    location := '110';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrdoc_pre(key_qualifier,value,key,description)
	 SELECT /*+ USE_NL(a,b) */
	 DISTINCT ''RELA'', source_cui,
		''snomedct_rela_mapping'', attribute_value
	 FROM mrd_attributes a, mrd_classes b
	 WHERE a.attribute_name = ''UMLSRELA''
	   AND a.ui = b.aui
	   AND a.expiration_date IS NULL
	   AND b.expiration_date IS NULL'
	);

    -- Log row count
    location := '120';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdoc_prepare',
        detail => 'Finished generating UMLS RELA ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Log completion of activity
    location := '150';
    MEME_UTILITY.timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdoc_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mrdoc_prepare;

/* PROCEDURE MRDEF_PREPARE *****************************/

PROCEDURE mrdef_prepare
IS
    row_count			INTEGER;
BEGIN
    -- Initialize tracking parameters and start timing elements
    initialize_trace('MRDEF_PREPARE');
    MEME_UTILITY.timing_start;

    
    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrdef_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdef_prepare',
        detail => 'Starting mrdef_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrdef_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrdef_pre');

    location := '20';
    MEME_UTILITY.exec(
        'CREATE TABLE mrdef_pre (
            cui         	VARCHAR2(10) NOT NULL,
            ui	         	VARCHAR2(10),
            atui	        VARCHAR2(12),
            satui	        VARCHAR2(50),
	    sab			VARCHAR2(40),
	    def 		VARCHAR2(350),
	    suppress	 	VARCHAR2(10),
            cvf	        	NUMBER(20) )
	  STORAGE (INITIAL 10M)
	' );
    location := '20';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrdef_pre
	   (cui, ui, atui, satui, sab, def, suppress)
	 SELECT cui, ui, atui, source_atui, root_source,
		attribute_value, suppressible
         FROM mrd_attributes
         WHERE attribute_name = ''DEFINITION''
           AND expiration_date IS NULL');

    -- Log row count
    location := '35';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdef_prepare',
        detail => 'Table mrdef_pre created.  with ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Set cvf
    apply_cvf(
        table_name => 'mrdef_pre',
        key_field => 'atui',
        cascade_field => 'ui',
        cui_field => 'cui');

    apply_cvf(
	table_name => 'mrdef_pre',
	key_field => null,
	cascade_field => 'sab',
	cui_field => null);

    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log completion of activity
    location := '210';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrdef_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mrdef_prepare;

/* PROCEDURE MRRANK_PREPARE *****************************/
PROCEDURE mrrank_prepare
IS
BEGIN
  MEME_UTILITY.put_message('mrrank_prepare is unimplemented.');
END mrrank_prepare;

/* PROCEDURE MRREL_PREPARE *****************************
 * Creates and loads mrrel_pre which contains the
 * full release view of MRREL
 */
PROCEDURE mrrel_prepare
IS
 	uwda		VARCHAR2(40);
	row_count	INTEGER;
BEGIN

    -- Initialize tracking parameters and start timing elements
    initialize_trace('MRREL_PREPARE');
    MEME_UTILITY.timing_start;

    
    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrrel_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrrel_prepare',
        detail => 'Starting mrrel_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrrel_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrrel_pre');

    location := '20';
    MEME_UTILITY.exec(
	'CREATE TABLE mrrel_pre (
	    cui_1			VARCHAR2(10) NOT NULL,
	    aui_1			VARCHAR2(10) ,
	    sg_type_1			VARCHAR2(50) NOT NULL,
	    relationship_name 		VARCHAR2(10),
	    cui_2	 		VARCHAR2(10) NOT NULL,
	    aui_2	 		VARCHAR2(10) ,
	    sg_type_2			VARCHAR2(50) NOT NULL,
  	    relationship_attribute 	VARCHAR2(100),
	    rui				VARCHAR2(12) NOT NULL,
	    srui			VARCHAR2(50) ,
	    source	 		VARCHAR2(40) NOT NULL,
	    source_of_label		VARCHAR2(40) NOT NULL,
	    relationship_group	 	VARCHAR2(10) ,
	    dir_flag		 	VARCHAR2(1) ,
	    suppressible	 	VARCHAR2(10) ,
	    cvf				NUMBER(20))
    ');

    -- Get source asserted relationships
    -- This only gets relationships with *valid* sources
    location := '30';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrrel_pre
		(cui_1,aui_1,sg_type_1,relationship_name,
		cui_2,aui_2,sg_type_2,
		relationship_attribute,rui, srui, source, source_of_label,
		relationship_group,dir_flag,suppressible)
 	 SELECT cui_1, aui_1, sg_type_1, relationship_name, cui_2, aui_2,
		sg_type_2, relationship_attribute, rui, source_rui,
	  	root_source, root_source_of_label, relationship_group,
		rel_directionality_flag, suppressible
         FROM mrd_relationships
         WHERE relationship_name NOT IN
		(''XS'', ''BBT'', ''BNT'', ''BRT'')
           AND relationship_level = ''S''
           AND expiration_date IS NULL
    ');

    location := '35';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrrel_pre
		(cui_1,sg_type_1,relationship_name,cui_2,sg_type_2,
		relationship_attribute,rui, srui, source, source_of_label,
		relationship_group,dir_flag,suppressible)
 	 SELECT cui_1, sg_type_1, relationship_name, cui_2, sg_type_2,
		relationship_attribute, rui, source_rui,
		root_source,root_source_of_label,
		relationship_group,rel_directionality_flag,suppressible
         FROM mrd_relationships
         WHERE relationship_name NOT IN
		(''XR'', ''XS'', ''BBT'', ''BNT'', ''BRT'')
           AND relationship_level = ''C''
           AND expiration_date IS NULL
    ');

    -- Get Parent context relationships ('PAR')
    -- mrd_contexts should have a source field, it should come from there.
    -- We need to figure out the sense of rel, rela
    --
    -- The relationship_attribute from mrd_contexts
    -- is the rela from the atom_id to its immediate parent.
    --
    -- Thus the cui_2 should be the child so that we do not
    -- have to reverse the sense of the RELA. This means that
    -- a.cui is the child and b.cui is the parent.
    --

    -- Get sibling context relationships ('SIB')
    -- We use c.atom_id != d.atom_id to get SIBs in both directions
    -- The source should come from mrd_contexts not mrd_classes
    -- Only include SIBs if the first char of release_mode='1'
    -- Set relationship_attribute for UWDA source
    --
    -- As in MRCXT, we only keep sibling relationships
    -- where the RELA values match.  In other words, the
    -- respective atoms must have the same rela to the parent
    -- and the same parent.  The exception to this is MSH.

    -- UWDA siblings get RELA values of the form 'sib_in_*'
    -- However, the values should not be inversed, so for
    -- the 'part_of' tree, we should use 'sib_in_part_of'
    -- for the SIBs in both directions and never use
    -- 'sib_in_has_part'.  The following code will correct
    -- any that were inversed above...

    -- It is important that mrd_attributes be analyzed
    -- otherwise Oracle uses a bad query plan (nested loops)
    -- ? MEME_SYSTEM.analyze('mrd_attributes');

   -- Recreate the mrrel_pre_aq table
    MEME_UTILITY.sub_timing_start;
    location := '40';
    MEME_UTILITY.drop_it('table','mrrel_pre_aq');

    location := '50';
    MEME_UTILITY.exec(
	'CREATE TABLE mrrel_pre_aq (
	    cui_1			VARCHAR2(10) NOT NULL,
	    aui_1			VARCHAR2(10) ,
	    relationship_name 		VARCHAR2(10),
	    cui_2	 		VARCHAR2(10) NOT NULL,
	    aui_2	 		VARCHAR2(10) ,
  	    relationship_attribute 	VARCHAR2(100),
	    source	 		VARCHAR2(40) NOT NULL,
	    source_of_label		VARCHAR2(40) NOT NULL,
	    SG_ID_1         VARCHAR(20) NOT NULL,
        SG_ID_2         VARCHAR(20)  NOT NULL,
        SG_QUALIFIER_1       VARCHAR(40)  NOT NULL,
        SG_QUALIFIER_2  VARCHAR(40) NOT NULL,
        SG_TYPE_1       VARCHAR(20) NOT NULL,
        SG_TYPE_2       VARCHAR(20) NOT NULL)
	STORAGE (initial 100M)
    ');

   
    -- Get 'AQ' relationships
    MEME_UTILITY.sub_timing_start;
    location := '80';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrrel_pre_aq
         SELECT a.cui, a.ui, ''AQ'', d.cui,d.aui, null,
                a.root_source, a.root_source, c.source_dui, d.source_dui, c.root_source,d.root_source,
        ''ROOT_SOURCE_DUI'', ''ROOT_SOURCE_DUI''
         FROM mrd_classes b, mrd_attributes a, mrd_classes c, mrd_classes d, string_ui e
         WHERE b.code like ''Q%''
           AND a.code like ''D%''
           AND b.tty = ''QAB''
           AND b.code = d.code
       AND d.tty = ''TQ''
       AND b.sui = e.sui
           AND a.attribute_name IN (''ATN'',''AQL'')
           AND INSTR(a.attribute_value,e.string) > 0
       AND a.ui = c.aui
           AND a.root_source = ''MSH''
           AND b.root_source = ''MSH''
           AND d.root_source = ''MSH''
           AND c.root_source = ''MSH''
           AND a.expiration_date IS NULL
           AND b.expiration_date IS NULL
    ');

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '85';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrrel_prepare',
        detail => 'Finished generating AQ relationship rows, ' ||
                row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get Long 'AQ' relationships
    MEME_UTILITY.sub_timing_start;
    location := '86';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrrel_pre_aq
         SELECT a.cui, a.ui, ''AQ'', e.cui, e.aui, null,
                a.root_source, a.root_source,d.source_dui, e.source_dui, d.root_source,e.root_source,
        ''ROOT_SOURCE_DUI'', ''ROOT_SOURCE_DUI''
         FROM mrd_classes b, mrd_attributes a, mrd_stringtab c,mrd_classes d, mrd_classes e, string_ui f
         WHERE b.code like ''Q%''
           AND a.code like ''D%''
           AND b.tty = ''QAB''
       AND b.code = e.code
       ANd e.tty = ''TQ''
       AND b.sui = f.sui
           AND a.attribute_name IN (''ATN'',''AQL'')
           AND a.attribute_value like ''<>Long_Attribute<>:%''
           AND c.hashcode||'''' = a.hashcode
           AND INSTR(c.text_value,f.string) > 0
       AND a.ui = d.aui
           AND a.root_source = ''MSH''
           AND b.root_source = ''MSH''
           AND d.root_source = ''MSH''
           AND e.root_source = ''MSH''
           AND a.expiration_date IS NULL
           AND b.expiration_date IS NULL
           AND c.expiration_date IS NULL
    ');

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '87';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrrel_prepare',
        detail => 'Finished generating Long AQ relationship rows, ' ||
                row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get 'QB' relationships
    MEME_UTILITY.sub_timing_start;
    location := '90';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrrel_pre_aq
         SELECT d.cui, d.aui, ''QB'', a.cui, a.ui, null,
                a.root_source, a.root_source, d.source_dui, c.source_dui, d.root_source,c.root_source,
        ''ROOT_SOURCE_DUI'', ''ROOT_SOURCE_DUI''
         FROM mrd_classes b, mrd_attributes a, mrd_classes c, mrd_classes d, string_ui e
         WHERE b.code like ''Q%''
           AND a.code like ''D%''
           AND b.tty = ''QAB''
           AND b.code = d.code
       AND d.tty = ''TQ''
       AND b.sui = e.sui
           AND a.attribute_name IN (''ATN'',''AQL'')
           AND INSTR(a.attribute_value,e.string) > 0
       AND a.ui = c.aui
           AND a.root_source = ''MSH''
           AND b.root_source = ''MSH''
           AND d.root_source = ''MSH''
           AND c.root_source = ''MSH''
           AND a.expiration_date IS NULL
           AND b.expiration_date IS NULL
    ');

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '95';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrrel_prepare',
        detail => 'Finished generating QB relationship rows, ' ||
                row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get Long 'QB' relationships
    MEME_UTILITY.sub_timing_start;
    location := '96';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrrel_pre_aq
         SELECT e.cui, e.aui, ''QB'', a.cui, a.ui, null,
                a.root_source, a.root_source,e.source_dui, d.source_dui, e.root_source,d.root_source,
        ''ROOT_SOURCE_DUI'', ''ROOT_SOURCE_DUI''
         FROM mrd_classes b, mrd_attributes a, mrd_stringtab c,mrd_classes d, mrd_classes e, string_ui f
         WHERE b.code like ''Q%''
           AND a.code like ''D%''
           AND b.tty = ''QAB''
       AND b.code = e.code
       ANd e.tty = ''TQ''
       AND b.sui = f.sui
           AND a.attribute_name IN (''ATN'',''AQL'')
           AND a.attribute_value like ''<>Long_Attribute<>:%''
           AND c.hashcode||'''' = a.hashcode
           AND INSTR(c.text_value,f.string) > 0
       AND a.ui = d.aui
           AND a.root_source = ''MSH''
           AND b.root_source = ''MSH''
           AND e.root_source = ''MSH''
           AND d.root_source = ''MSH''
           AND a.expiration_date IS NULL
           AND b.expiration_date IS NULL
           AND c.expiration_date IS NULL
    ');

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '97';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrrel_prepare',
        detail => 'Finished generating Long QB relationship rows, ' ||
                row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    COMMIT;

    -- Get RUI values for AQ rels
    MEME_UTILITY.sub_timing_start;
    location := '100';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrrel_pre
                (cui_1,aui_1,sg_type_1,relationship_name,
                cui_2,aui_2,sg_type_2,
                relationship_attribute,rui, source, source_of_label,
                relationship_group,dir_flag,suppressible)
         SELECT /*+ USE_MERGE(a,b) */
                cui_1, aui_1, ''SDUI'', a.relationship_name,
                cui_2, aui_2, ''SDUI'',
                a.relationship_attribute, rui, source, source_of_label,
                null, null, ''N''
         FROM mrrel_pre_aq a, relationships_ui b
         WHERE a.source = b.root_source
           AND b.relationship_level = ''S''
           AND a.relationship_name = b.relationship_name
           AND b.relationship_attribute IS NULL
           AND a.sg_id_1 = b.sg_id_1
           AND b.sg_qualifier_1 = a.sg_qualifier_1
           AND b.sg_type_1 = ''ROOT_SOURCE_DUI''
           AND a.sg_id_2 = b.sg_id_2
           AND b.sg_qualifier_2 = a.sg_qualifier_1
           AND b.sg_type_2 = ''ROOT_SOURCE_DUI''
    ');
    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '115';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrrel_prepare',
        detail => 'Get RUI values for AQ rels ' ||
                row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);
    -- drop temp table
    MEME_UTILITY.drop_it('table','mrrel_pre_aq');

    -- Set cvf
    apply_cvf(
	table_name => 'mrrel_pre',
	key_field => 'rui',
	cascade_field => 'aui_1',
	cui_field => 'cui_1');

    apply_cvf(
	table_name => 'mrrel_pre',
	key_field => null,
	cascade_field => 'source',
	cui_field => null);

    apply_cvf(
	table_name => 'mrrel_pre',
	key_field => null,
	cascade_field => 'source_of_label',
	cui_field => null);

    -- Log completion of activity
    MEME_UTILITY.timing_stop;
    location := '120';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrrel_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mrrel_prepare;

/* PROCEDURE MRSAT_PREPARE *****************************
 * Creates and loads mrsat_pre which contains the
 * full release view of MRSAT
 */
PROCEDURE mrsat_prepare(
    meta_med    IN DATE
)
IS
    row_count		INTEGER;
    med 		VARCHAR2(40);

    -- Cursor for assigning DA attribute
    CURSOR mp_cur IS
      SELECT * FROM meme_properties
      WHERE key_qualifier='MRSAT'
      ORDER BY value;

    mp_var		mp_cur%ROWTYPE;
    prev_cui    	VARCHAR2(10) := '';

BEGIN

    -- Initialize tracking parameters and start timing elements
    initialize_trace('MRSAT_PREPARE');
    MEME_UTILITY.timing_start;

    EXECUTE IMMEDIATE 'ALTER SESSION SET sort_area_size=200000000';
    EXECUTE IMMEDIATE 'ALTER SESSION SET hash_area_size=200000000';
    
    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrsat_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'Starting mrsat_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrsat_pre table
    location := '10';
    MEME_UTILITY.drop_it('table','mrsat_pre');

    location := '20';
    MEME_UTILITY.exec(
        'CREATE TABLE mrsat_pre (
            cui         	VARCHAR2(10) NOT NULL,
            lui         	VARCHAR2(10) ,
            sui        		VARCHAR2(10) ,
            ui	         	VARCHAR2(20) ,
	    sg_type 		VARCHAR2(50) NOT NULL,
            code        	VARCHAR2(100),
            atui        	VARCHAR2(12),
            source_atui        	VARCHAR2(50),
	    attribute_name 	VARCHAR2(100) NOT NULL,
            root_source        	VARCHAR2(40) NOT NULL,
	    attribute_value 	VARCHAR2(350),
  	    suppressible	VARCHAR2(20) NOT NULL,
  	    cvf			NUMBER(20))
	' );

    -- Get all S level attributes
    -- But exclude rows that go into MRDEF, MRMAP, and MRREL
    -- What about HISTORY?!
    location := '21';
    MEME_UTILITY.sub_timing_start;
    MEME_UTILITY.exec(
        'INSERT /*+ APPEND */ INTO mrsat_pre(cui, lui, sui, ui, sg_type,
		code, atui, source_atui,
	        attribute_name, root_source, attribute_value,
		suppressible)
         SELECT /*+ PARALLEL(a) */
		cui, lui, sui, ui, sg_type, code, atui, a.source_atui,
	        attribute_name, root_source, attribute_value,
		suppressible
         FROM mrd_attributes a
         WHERE expiration_date IS NULL
           AND attribute_level = ''S''
           AND attribute_name not IN
             (''DEFINITION'',''ATX_REL'',''MRLO'',''HDA'',
	      ''HPC'',''COC'',''LEXICAL_TAG'',
	      ''XMAP'',''XMAPFROM'',''XMAPTO'',''COMPONENTHISTORY'')'
    );

    COMMIT;

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '22';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'Table mrsat_pre created with all S level attributes ',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);


    -- Get all C level attributes
    -- But exclude rows that go into MRDEF, MRMAP, and MRLO, MRSTY, MRHIST
    location := '23';
    MEME_UTILITY.sub_timing_start;
    MEME_UTILITY.exec(
        'INSERT /*+ APPEND */ INTO mrsat_pre(cui, lui, sui, ui, sg_type,
		code, atui, source_atui,
	        attribute_name, root_source, attribute_value,
		suppressible)
         SELECT /*+ PARALLEL(a) */
		cui, lui, sui, ui, sg_type, code, atui, a.source_atui,
	        attribute_name, root_source, attribute_value,
		suppressible
         FROM mrd_attributes a
         WHERE expiration_date IS NULL
           AND attribute_level = ''C''
           AND attribute_name not IN
             (''DEFINITION'',''ATX_REL'',''MRLO'',''HDA'',
	      ''HPC'',''COC'',''LEXICAL_TAG'', ''SEMANTIC_TYPE'',
	      ''XMAP'',''XMAPFROM'',''XMAPTO'',''COMPONENTHISTORY'')'
    );

    COMMIT;

    -- Log progress
    MEME_UTILITY.sub_timing_stop;
    location := '30';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'Table mrsat_pre created with C level attributes ',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Recreate the mrsat_pre_3 table
    location := '37';
    MEME_UTILITY.drop_it('table','mrsat_pre_3');

    location := '38';
    MEME_UTILITY.exec(
        'CREATE TABLE mrsat_pre_3 (
            cui         	VARCHAR2(10) NOT NULL,
            lui         	VARCHAR2(10) ,
            sui        		VARCHAR2(10) ,
            ui	         	VARCHAR2(10) ,
            code        	VARCHAR2(30),
            source_atui        	VARCHAR2(10),
	    attribute_name 	VARCHAR2(100) NOT NULL,
            root_source        	VARCHAR2(40) NOT NULL,
	    attribute_value 	VARCHAR2(350),
  	    suppressible	VARCHAR2(20) NOT NULL)
	' );

    -- Get the lexical tag attributes
    MEME_UTILITY.sub_timing_start;
    location := '40';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrsat_pre
		(cui, lui, sui, ui, sg_type,
		code, atui, source_atui,
	        attribute_name, root_source, attribute_value,
		suppressible)
      	 SELECT  cui, lui, sui, ui, ''AUI'',
	        code, atui, a.source_atui,
		''LT'', root_source, ''TRD'', suppressible
      	 FROM mrd_attributes a
      	 WHERE expiration_date IS NULL
      	   AND attribute_level = ''S''
      	   AND attribute_name = ''LEXICAL_TAG''
	   AND attribute_value = ''TRD'' '
    );

    MEME_UTILITY.sub_timing_stop;
    -- Log row count
    location := '42';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => row_count || ' MSH LT:TRD attributes added.' ,
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Get the lexical tag attributes
    MEME_UTILITY.sub_timing_start;
    location := '45';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrsat_pre_3
	    (cui, lui, sui, ui, code, source_atui,
		attribute_name, root_source, attribute_value, suppressible)
      	 SELECT DISTINCT b.cui, b.lui, b.sui, b.aui, b.code, a.source_atui,
		''LT'', ''MTH'', ''TRD'', a.suppressible
      	 FROM mrd_attributes a, mrd_classes b
      	 WHERE a.expiration_date IS NULL
      	   AND b.expiration_date IS NULL
      	   AND attribute_level = ''S''
      	   AND a.sui = b.sui
      	   AND a.cui = b.cui
      	   AND attribute_name = ''LEXICAL_TAG''
	   AND attribute_value = ''TRD'' '
    );

    MEME_UTILITY.sub_timing_stop;
    -- Log row count
    location := '50';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => row_count || ' LT:TRD attributes added.' ,
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Insert into mrsat_pre
    MEME_UTILITY.sub_timing_start;
    location := '55';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrsat_pre
	    (cui, lui, sui, ui, sg_type, code, atui, source_atui,
		attribute_name,  root_source, attribute_value, suppressible)
         SELECT /*+ USE_MERGE (a,b) */
		cui, lui, sui, ui, b.sg_type, code, atui, '''',
		a.attribute_name,  a.root_source, a.attribute_value, a.suppressible
	 FROM mrsat_pre_3 a, attributes_ui b
	 WHERE b.attribute_name = a.attribute_name
	   AND b.attribute_name = ''LT''
	   AND a.root_source = b.root_source
	   AND b.sg_id = a.ui
	   AND sg_type = ''AUI''
	   AND sg_qualifier IS NULL'
    );

    MEME_UTILITY.sub_timing_stop;
    -- Log row count
    location := '56';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'Get atui for LT attributes (' ||
		row_count || ' rows).' ,
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    COMMIT;

    -- Recreate the mrsat_pre_2 table
    MEME_UTILITY.sub_timing_start;
    location := '147';
    MEME_UTILITY.drop_it('table','mrsat_pre_2');

    location := '148';
    MEME_UTILITY.exec(
        'CREATE TABLE mrsat_pre_2 (
            cui         	VARCHAR2(10) NOT NULL,
            lui         	VARCHAR2(10) ,
            sui        		VARCHAR2(10) ,
            ui	         	VARCHAR2(10) ,
            code        	VARCHAR2(30),
	    attribute_name 	VARCHAR2(100) NOT NULL,
            root_source        	VARCHAR2(40) NOT NULL,
	    attribute_value 	VARCHAR2(350))
	' );


    -- Insert major topic count
    MEME_UTILITY.sub_timing_start;
    location := '150';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrsat_pre_2
	    (cui, lui, sui, ui, code,
		attribute_name,  root_source, attribute_value)
         SELECT DISTINCT cui, lui, sui, aui, code,
		''MED''||atn, ''NLM-MED'',
                ''*'' || ct
	 FROM
            (SELECT /*+ PARALLEL(coc) */  count(*) ct,
		    TO_CHAR(publication_date,''YYYY'') as atn, heading_aui
             FROM mrd_coc_headings coc
             WHERE expiration_date IS NULL
	       AND root_source = ''NLM-MED''
               AND major_topic = ''Y''
	     GROUP BY heading_aui, TO_CHAR(publication_date,''YYYY'')
	    ) a, mrd_classes b
 	 WHERE a.heading_aui = b.aui
	   AND b.expiration_date IS NULL '
    );

    MEME_UTILITY.sub_timing_stop;
    -- Log row count
    location := '160';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'MED<year> * attributes processed (' ||
		row_count || ' rows).',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);


    -- Insert aggregate count
    MEME_UTILITY.sub_timing_start;
    location := '170';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrsat_pre_2
	    (cui, lui, sui, ui, code, attribute_name, root_source, attribute_value)
         SELECT DISTINCT cui, lui, sui, aui, code,
		''MED''||atn, ''NLM-MED'', ct
	 FROM
            (SELECT /*+ PARALLEL(coc) */ count(*) ct,
		    TO_CHAR(publication_date,''YYYY'') as atn, heading_aui
             FROM mrd_coc_headings coc
             WHERE expiration_date IS NULL
	       AND root_source = ''NLM-MED''
	     GROUP BY heading_aui, TO_CHAR(publication_date,''YYYY'')
	    ) a, mrd_classes b
 	 WHERE a.heading_aui = b.aui
	   AND b.expiration_date IS NULL '
    );

    MEME_UTILITY.sub_timing_stop;
    -- Log row count
    location := '180';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'MED<year> attributes processed (' ||
		row_count || ' rows).',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);
       MEME_UTILITY.drop_it('table','T_SL_MRD_COC_1');
location := '180.2';
        MEME_UTILITY.exec(
        'CREATE TABLE T_SL_MRD_COC_1 AS SELECT /*+ PARALLEL(coc) */ DISTINCT citation_set_id,
                            TO_CHAR(publication_date,''YYYY'') as year
                     FROM mrd_coc_headings coc
                     WHERE root_source=''NLM-MED''
                       AND expiration_date IS NULL ');


   location := '180.1';
    MEME_UTILITY.drop_it('table','T_SL_MRD_COC');
    MEME_UTILITY.exec(
   'CREATE TABLE T_SL_MRD_COC AS SELECT year, subheading_qa, count(*) as ct
                       FROM T_SL_MRD_COC_1 a,
                    mrd_coc_subheadings b
               WHERE a.citation_set_id = b.citation_set_id
                 AND b.expiration_date IS NULL
               GROUP BY year, subheading_qa ');
    --Get the Q# MED<year> attributes
    MEME_UTILITY.sub_timing_start;
    location := '190';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrsat_pre_2
           (cui,lui,sui,ui,code,attribute_name,root_source,attribute_value)
         SELECT d.cui, d.lui, d.sui, d.aui, d.code, ''MED''||year, ''NLM-MED'', sum(ct)
         FROM mrd_classes a,string_ui c, mrd_classes d, T_SL_MRD_COC b
         WHERE a.tty = ''QAB''
           AND d.tty = ''TQ''
           AND d.root_source = ''MSH''
           AND d.code = a.code
           AND a.sui = c.sui
           AND a.root_source = ''MSH''
           AND a.expiration_date IS NULL
           AND c.string=b.subheading_qa
         GROUP BY d.cui,d.lui,d.sui,d.aui,d.code,year  '
    );

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '200';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'MED<year> Q# attributes processed (' ||
		row_count || ' rows).',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);


    /******************* NO LONGER PRODUCE AM ATTRIBUTES ***************
    -- Handle AM attributes
    -- We use min(sui) in the inner join because concepts *may* have
    -- multiple PNs (although they should not).
    --
    -- This code was tested against the make.MRSAT.osm.s script
    -- during META2001AC and found to be exactly the same.
    MEME_UTILITY.sub_timing_start;
    location := '190';
    row_count := MEME_UTILITY.exec(
        'INSERT INTO mrsat_pre_2
           (cui,lui,sui,ui,code,
		attribute_name,root_source,attribute_value)
         SELECT DISTINCT cui, lui, sui, aui, code,
		''AM'', ''MTH'', ''A'' ||
             (SELECT '':''||min(sui) FROM mrd_classes b
              WHERE a.cui = b.cui AND root_source = ''MTH''
		AND tty = ''PN'' AND b.expiration_date IS NULL)
         FROM mrd_classes a
         WHERE isui IN (SELECT isui FROM mrd_classes
                        WHERE expiration_date IS NULL
                          AND language=''ENG''
                        GROUP BY isui HAVING count(distinct cui)>1)
           AND language=''ENG''
           AND expiration_date IS NULL'    );
    -- Anything marked as 'A:' should be changed to 'A';
    location := '195';
    EXECUTE IMMEDIATE
        'UPDATE mrsat_pre_2 SET attribute_value = ''A''
         WHERE attribute_name = ''AM''
           AND attribute_value= ''A:'' ';
    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '200';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'AM attributes processed (' ||
                row_count || ' rows).',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);
    *********************************************************************/

    -- Insert into mrsat_pre
    MEME_UTILITY.sub_timing_start;
    location := '210';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrsat_pre
	    (cui, lui, sui, ui, sg_type, code, atui, source_atui,
		attribute_name,  root_source, attribute_value, suppressible)
         SELECT /*+ USE_HASH (a,b) */
		cui, lui, sui, ui, b.sg_type, code, atui, '''',
		a.attribute_name,  a.root_source, a.attribute_value, ''N''
	 FROM mrsat_pre_2 a, attributes_ui b
	 WHERE a.attribute_name = b.attribute_name 
	   AND a.attribute_value = b.hashcode
	   AND a.root_source = b.root_source
	   AND b.attribute_level = ''S''
	   AND b.source_atui IS NULL
	   AND b.sg_id = a.ui
	   AND sg_type = ''AUI''
	   AND sg_qualifier IS NULL'
    );

    MEME_UTILITY.sub_timing_stop;
    -- Log row count
    location := '215';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'Get atui for MED,MED*,AM attributes (' ||
		row_count || ' rows).' ,
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    COMMIT;

    MEME_UTILITY.drop_it('table','mrsat_pre_1');
    MEME_UTILITY.drop_it('table','mrsat_pre_2');
    MEME_UTILITY.drop_it('table','mrsat_pre_3');

    -- Set cvf
    apply_cvf(
	table_name => 'mrsat_pre',
	key_field => 'atui',
	cascade_field => 'ui',
	cui_field => 'cui');

    apply_cvf(
	table_name => 'mrsat_pre',
	key_field => null,
	cascade_field => 'root_source',
	cui_field => null);

    -- Log completion
    MEME_UTILITY.timing_stop;
    location := '220';
        log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsat_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

   WHEN OTHERS THEN
       mrd_release_operations_error(method,location,err_code,SQLERRM);
       RAISE mrd_release_exception;

END mrsat_prepare;

/* PROCEDURE MRSAB_PREPARE *****************************
 * Creates and loads mrsab_pre which contains the
 * full release view of MRSAB
 */
PROCEDURE mrsab_prepare
IS
    CURSOR tty_cursor IS
    SELECT /*+ parallel(a) */
	DISTINCT normalized_source as source, tty
    FROM mrd_classes a, mrd_source_rank b
    WHERE a.expiration_date IS NULL
      AND b.expiration_date IS NULL
      AND a.root_source = b.root_source
      AND is_current = 'Y'
    ORDER BY normalized_source, tty;

    CURSOR atn_cursor IS
    SELECT distinct source, DECODE(attribute_name,
       'LEXICAL_TAG','LT',attribute_name)
        as attribute_name
    FROM (
      SELECT /*+ PARALLEL(a) */ distinct source, attribute_name
      FROM mrd_attributes a, mrd_source_rank b
      WHERE a.expiration_date IS NULL
        AND b.expiration_date IS NULL
        AND a.root_source = b.root_source
        AND is_current = 'Y'
        AND attribute_level = 'S'
        AND attribute_name not IN
        ('DEFINITION','COC',
         'XMAPTO','XMAP','XMAPFROM','COMPONENTHISTORY')
      UNION ALL
      SELECT distinct 'MTH', attribute_name
      FROM mrd_attributes a
      WHERE a.expiration_date IS NULL
        AND attribute_level = 'C'
        AND attribute_name not IN
        ('SEMANTIC_TYPE')
    UNION ALL
    SELECT DISTINCT source, substr(attribute_value,
    instr(attribute_value,'~') +1,
                instr(attribute_value,'~',1,2)-instr(attribute_value,'~')-1)
    FROM mrd_attributes a, mrd_source_rank b
    WHERE a.expiration_date IS NULL
        AND b.expiration_date IS NULL
        AND a.root_source = b.root_source
        AND is_current = 'Y'
        AND attribute_name = 'SUBSET_MEMBER'
    AND attribute_value not like '<>Long_Attribute<>:%'
    UNION
    SELECT DISTINCT source, substr(text_value,instr(text_value,'~')+1,
                instr(text_value,'~',1,2)-instr(text_value,'~')-1)
    FROM mrd_attributes a, mrd_stringtab b, mrd_source_rank c
    WHERE a.expiration_date IS NULL
        AND c.expiration_date IS NULL
        AND a.root_source = c.root_source
        AND is_current = 'Y'
        AND a.hashcode = b.hashcode
      AND attribute_name = 'SUBSET_MEMBER'
      AND attribute_value like '<>Long_Attribute<>:%'
    UNION ALL
    SELECT DISTINCT root_source, substr(attribute_value,
    instr(attribute_value,'~') +1,
                instr(attribute_value,'~',1,2)-instr(attribute_value,'~')-1)
    FROM mrd_attributes
    WHERE attribute_name ='CV_MEMBER'
    AND attribute_value not like '<>Long_Attribute<>:%'
    UNION
    SELECT DISTINCT root_source, substr(text_value,instr(text_value,'~')+1,
                instr(text_value,'~',1,2)-instr(text_value,'~')-1)
    FROM mrd_attributes a, mrd_stringtab b
    WHERE a.hashcode = b.hashcode
      AND attribute_name = 'CV_MEMBER'
      AND attribute_value like '<>Long_Attribute<>:%'
     )
    ORDER BY source, attribute_name;


    tty_row             tty_cursor%ROWTYPE;
    atn_row             atn_cursor%ROWTYPE;


    list                VARCHAR2(1000);
    prev_source         VARCHAR2(40);
    row_count			INTEGER;

BEGIN

    -- Initialize tracking parameters and start timing elements
	initialize_trace('MRSAB_PREPARE');
    MEME_UTILITY.timing_start;

    EXECUTE IMMEDIATE 'ALTER SESSION SET sort_area_size=50000000';
    EXECUTE IMMEDIATE 'ALTER SESSION SET hash_area_size=50000000';
    
    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrsab_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsab_prepare',
        detail => 'Starting mrsab_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrsab_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrsab_pre');

    location := '20';
    MEME_UTILITY.exec(
    	'CREATE TABLE mrsab_pre(
        vcui     VARCHAR2(10),
        rcui     VARCHAR2(10),
        vsab     VARCHAR2(40),
        rsab     VARCHAR2(40),
        son      VARCHAR2(1500),
        sf       VARCHAR2(40),
        ver      VARCHAR2(20),
        mstart   VARCHAR(10),
        mend     VARCHAR(10),
        imeta    VARCHAR2(20),
        rmeta    VARCHAR2(20),
        slc      VARCHAR2(1000),
        scc      VARCHAR2(1000),
        srl      NUMBER(1),
        tfr      NUMBER(12),
        cfr      NUMBER(12),
        cxty     VARCHAR2(100),
        ttyl     VARCHAR2(1000),
        atnl     VARCHAR2(1000),
        lat      CHAR(3),
        cenc     VARCHAR2(20),
        curver   VARCHAR2(1),
        sabin    VARCHAR2(1),
        ssn    	 VARCHAR2(4000),
        scit     VARCHAR2(4000)
    ) STORAGE (INITIAL 10M NEXT 10M)
	');

    -- Get data from mrd_source_rank
    MEME_UTILITY.sub_timing_start;
    location := '30';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrsab_pre
        	(vsab, rsab, srl, son, sf, ver, mstart, mend ,imeta, rmeta,
         	scc, slc, cxty, cenc, curver, sabin, ssn, scit, lat)
	 SELECT source, root_source, restriction_level, source_official_name,
        	source_family, version,
        	to_char(valid_start_date,''YYYYMMDD''),
        	to_char(valid_end_date,''YYYYMMDD''),
        	insert_meta_version, remove_meta_version, content_contact,
        	license_contact, context_type, character_set,
		is_current, ''Y'', source_short_name, citation, language
    	 FROM mrd_source_rank
	 WHERE expiration_date IS NULL
	   AND source=normalized_source
           AND is_current=''Y'' ');
    MEME_UTILITY.sub_timing_stop;

    --
    -- Get data from mrd_source_rank
    -- For TOVSAB and FROMVSAB sources
    --
    MEME_UTILITY.sub_timing_start;
    location := '30b';
    row_count := MEME_UTILITY.exec(
    	'INSERT INTO mrsab_pre
        	(vsab, rsab, srl, son, sf, ver, mstart, mend ,imeta, rmeta,
         	scc, slc, cxty, cenc, curver, sabin, ssn, scit, lat)
	 SELECT source, root_source, restriction_level, source_official_name,
        	source_family, version,
        	to_char(valid_start_date,''YYYYMMDD''),
        	to_char(valid_end_date,''YYYYMMDD''),
        	insert_meta_version, remove_meta_version, content_contact,
        	license_contact, context_type, character_set,
		is_current, ''N'', source_short_name, citation, language
    	 FROM mrd_source_rank
	 WHERE expiration_date IS NULL
	   AND source=normalized_source
           AND source not in (select vsab from mrsab_pre)
	   AND source in (SELECT attribute_value FROM mrd_attributes
			  WHERE expiration_date IS NULL
			  AND attribute_name IN (''TOVSAB'',''FROMVSAB'') )');
    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '40';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsab_prepare',
        detail => 'Table mrsab_pre created with ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Set vcui
    MEME_UTILITY.sub_timing_start;
    location := '50';
    row_count := MEME_UTILITY.exec(
    	'UPDATE mrsab_pre a SET vcui =
          (SELECT min(cui) FROM mrd_classes b, string_ui c
           WHERE expiration_date IS NULL
             AND b.sui = c.sui
             AND a.vsab = c.string
	     AND root_source = ''SRC''
	     AND tty = ''VAB''
             AND substr(a.vsab,0,10) = c.string_pre ) ');
    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '55';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsab_prepare',
        detail => 'Finished processing vcui field ' ||
			row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Set rcui
    MEME_UTILITY.sub_timing_start;
    location := '60';
    row_count := MEME_UTILITY.exec(
    	'UPDATE mrsab_pre a SET rcui =
           (SELECT min(cui) FROM mrd_classes b, string_ui c
            WHERE expiration_date IS NULL
              AND b.sui = c.sui
              AND a.rsab = c.string
	      AND root_source = ''SRC''
	      AND tty = ''RAB''
              AND substr(a.rsab,0,10) = c.string_pre ) ');
    MEME_UTILITY.sub_timing_stop;

    -- Log row count
    location := '65';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsab_prepare',
        detail => 'Finished processing rcui field ' ||
			row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Set tfr,cfr
    MEME_UTILITY.sub_timing_start;
    location := '70.1';
    MEME_UTILITY.drop_it('table','mrsab_pre_1');
    location := '70.2';
    EXECUTE IMMEDIATE
	'CREATE TABLE mrsab_pre_1 AS
	 SELECT /*+ PARALLEL(b) */
	        count(distinct AUI) as tfr,
                count(distinct cui) as cfr,
		normalized_source as source
         FROM mrd_classes b, mrd_source_rank c
         WHERE b.expiration_date IS NULL
	   AND c.expiration_date IS NULL
           AND b.root_source = c.root_source
	   AND is_current = ''Y''
         GROUP BY normalized_source';
    location := '70.3';
    row_count := MEME_UTILITY.exec(
 	'UPDATE mrsab_pre a SET (tfr,cfr) =
          (SELECT tfr, cfr FROM mrsab_pre_1 b
	   WHERE vsab = b.source) ');
    MEME_UTILITY.sub_timing_stop;
    location := '70.35';
    row_count := MEME_UTILITY.exec(
      'UPDATE mrsab_pre a SET tfr = 0,cfr = 0 
         WHERE tfr IS NULL');

    location := '70.4';
    MEME_UTILITY.drop_it('table','mrsab_pre_1');

    -- Log row count
    location := '80';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsab_prepare',
        detail => 'Finished processing tfr,cfr field ' ||
			row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Set ttyl
    location := '90';
    list := '';
    prev_source := '';
    OPEN tty_cursor;
    LOOP
	location := '100';
      	FETCH tty_cursor INTO tty_row;
	location := '105';
	IF (tty_cursor%NOTFOUND) THEN
	   tty_row.source := '';
   	END IF;
	location := '110';
	IF (tty_row.source = prev_source) THEN
	   list := list || tty_row.tty || ',';
	ELSE
	    list := RTRIM(list, ',');
	    location := '115';
	    EXECUTE IMMEDIATE
		'UPDATE mrsab_pre SET ttyl = :x
		 WHERE vsab = :x '
	    USING list,prev_source;
	    list := tty_row.tty || ',';
	END IF;

        -- Set prev source
	location := '120';
	prev_source := tty_row.source;

	location := '125';
      	EXIT WHEN tty_cursor%NOTFOUND;

    END LOOP;

    CLOSE tty_cursor;

    location := '130';
    list := '';
    prev_source := '';
    OPEN atn_cursor;
    LOOP
	location := '140';
      	FETCH atn_cursor INTO atn_row;
	location := '145';
	IF (atn_cursor%NOTFOUND) THEN
	    atn_row.source := '';
	END IF;

	location := '150';
	IF (atn_row.source = prev_source) THEN
	    list := list || atn_row.attribute_name || ',';
	ELSE
	    list := RTRIM(list, ',');
	    EXECUTE IMMEDIATE
	 	    'UPDATE mrsab_pre SET atnl = :x
		     WHERE vsab = :x '
	    USING list,prev_source;
	    list := atn_row.attribute_name || ',';
	END IF;

	-- Set prev source
	location := '160';
	prev_source := atn_row.source;

	location := '165';
      	EXIT WHEN atn_cursor%NOTFOUND;

    END LOOP;

    CLOSE atn_cursor;


    -- To drop the table
    COMMIT;

    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log completion of activity
    location := '210';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsab_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mrsab_prepare;

/* PROCEDURE MRHIST_PREPARE *****************************/

PROCEDURE mrhist_prepare
IS
    row_count			INTEGER;
BEGIN

    -- Initialize tracking parameters and start timing elements
    initialize_trace('MRHIST_PREPARE');
    MEME_UTILITY.timing_start;

    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrhist_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrhist_prepare',
        detail => 'Starting mrhist_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrhist_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrhist_pre');

    location := '20';
    MEME_UTILITY.exec(
        'CREATE TABLE mrhist_pre (
            cui         	VARCHAR2(10) NOT NULL,
            ui	         	VARCHAR2(100),
            sab	        	VARCHAR2(40) NOT NULL,
            sver        	VARCHAR2(40),
            changetype        	VARCHAR2(100),
            changekey        	VARCHAR2(1000),
            changeval	        VARCHAR2(1000),
            reason	        VARCHAR2(1000),
            cvf	        	NUMBER(20) )
	  STORAGE (INITIAL 100M)
	' );

    location := '30';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrhist_pre
	   (cui, ui, sab, sver, changetype, changekey, changeval, reason)
	 SELECT cui,
		SUBSTR(attribute_value, 1, instr(attribute_value, ''~'') - 1),
		root_source,
       		SUBSTR(attribute_value, instr(attribute_value, ''~'') + 1,
        	             (INSTR(attribute_value, ''~'', 1, 2) -
			      INSTR(attribute_value, ''~'', 1)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 2) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 3) -
			      INSTR(attribute_value, ''~'', 1, 2)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 3) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 4) -
			      INSTR(attribute_value, ''~'', 1, 3)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 4) + 1,
        	             (INSTR(attribute_value, ''~'', 1, 5) -
			      INSTR(attribute_value, ''~'', 1, 4)) -1 ),
       		SUBSTR(attribute_value, instr(attribute_value, ''~'', 1, 5) + 1)
        FROM mrd_attributes a
    	WHERE attribute_name = ''COMPONENTHISTORY''
		and attribute_value not like ''<>Long%''
	  	AND expiration_date IS NULL');

    -- Log row count
    location := '35';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrhist_prepare',
        detail => 'Table mrhist_pre created.  with ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);
     row_count := MEME_UTILITY.exec(
      'INSERT INTO mrhist_pre
          (cui, ui, sab, sver, changetype, changekey, changeval, reason)
        SELECT cui,
               SUBSTR(text_value, 1, instr(text_value, ''~'') - 1),
               root_source,
                       SUBSTR(text_value, instr(text_value, ''~'') + 1,
                            (INSTR(text_value, ''~'', 1, 2) -
                             INSTR(text_value, ''~'', 1)) -1 ),
                       SUBSTR(text_value, instr(text_value, ''~'', 1, 2) + 1,
                            (INSTR(text_value, ''~'', 1, 3) -
                             INSTR(text_value, ''~'', 1, 2)) -1 ),
                       SUBSTR(text_value, instr(text_value, ''~'', 1, 3) + 1,
                            (INSTR(text_value, ''~'', 1, 4) -
                             INSTR(text_value, ''~'', 1, 3)) -1 ),
                       SUBSTR(text_value, instr(text_value, ''~'', 1, 4) + 1,
                            (INSTR(text_value, ''~'', 1, 5) -
                             INSTR(text_value, ''~'', 1, 4)) -1 ),
                       SUBSTR(text_value, instr(text_value, ''~'', 1, 5) + 1)
      FROM mrd_attributes a, mrd_stringtab b
        WHERE attribute_name=''COMPONENTHISTORY''
        AND a.expiration_date IS NULL
        AND attribute_value like ''<>Long%''
        AND b.hashcode = substr(attribute_value,20)');
    -- Set cvf
    apply_cvf(
	table_name => 'mrhist_pre',
	key_field => null,
	cascade_field => 'ui',
	cui_field => 'cui');

    apply_cvf(
	table_name => 'mrhist_pre',
	key_field => null,
	cascade_field => 'sab',
	cui_field => null);

    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log completion of activity
    location := '210';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrhist_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mrhist_prepare;

/* PROCEDURE MRSTY_PREPARE *****************************/

PROCEDURE mrsty_prepare
IS
    row_count			INTEGER;
BEGIN
    -- Initialize tracking parameters and start timing elements
    initialize_trace('MRSTY_PREPARE');
    MEME_UTILITY.timing_start;

    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrsty_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsty_prepare',
        detail => 'Starting mrsty_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the mrsty_pre table
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mrsty_pre');

    location := '20';
    MEME_UTILITY.exec(
        'CREATE TABLE mrsty_pre (
            cui         	VARCHAR2(10) NOT NULL,
            ui	         	VARCHAR2(10),
	    stn			VARCHAR2(15),
	    sty 		VARCHAR2(100),
            atui	        VARCHAR2(12),
            cvf	        	NUMBER(20) )
	  STORAGE (INITIAL 10M)
	' );
    location := '20';
    row_count := MEME_UTILITY.exec(
	'INSERT INTO mrsty_pre
	   (cui, ui, stn, sty, atui)
	 SELECT cui, b.ui, stn_rtn, attribute_value, min(atui)
         FROM mrd_attributes a, srdef b
         WHERE attribute_value = sty_rl
           AND attribute_name = ''SEMANTIC_TYPE''
           AND expiration_date IS NULL
         GROUP BY cui, b.ui, stn_rtn, attribute_value');

    -- Log row count
    location := '35';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsty_prepare',
        detail => 'Table mrsty_pre created.  with ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

     -- Set cvf
     apply_cvf(
	table_name => 'mrsty_pre',
	key_field => 'atui',
	cascade_field => null,
	cui_field => 'cui');

     -- Set sty cvf
     apply_sty_cvf(
	table_name => 'mrsty_pre',
	cui_field => 'cui');

    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log completion of activity
    location := '210';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrsty_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mrsty_prepare;


/* PROCEDURE AMBIG_PREPARE *****************************
 * Creates and loads ambig_lui_pre and ambig_sui_pre which contain the
 * full release view of AMBIG files
 */
PROCEDURE ambig_prepare
IS
    TYPE curvar_type IS REF CURSOR;
    curvar    	curvar_type;
    sui		VARCHAR2(10);
    lui		VARCHAR2(10);
    cui		VARCHAR2(10);
    str       	VARCHAR2(3000);
    last_sui  	VARCHAR2(10) := NULL;
    last_lui  	VARCHAR2(10) := NULL;
    row_count	INTEGER;

BEGIN

    -- Initialize tracking parameters and start timing elements
    initialize_trace('AMBIG_PREPARE');
    MEME_UTILITY.timing_start;

    -- Clean any previous logging
    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::ambig_prepare';

     -- Log initial activity
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::ambig_prepare',
        detail => 'Starting ambig_prepare',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    -- Recreate the ambig_lui_pre table
    MEME_UTILITY.sub_timing_start;

    -- Find all cases where a lui
    -- exists in more than one cui
    location := '10';
    MEME_UTILITY.drop_it('table','tmp_lui');

    location := '20';
    MEME_UTILITY.exec(
    	'CREATE TABLE tmp_lui AS
      	 SELECT lui
      	 FROM mrd_classes
      	 WHERE expiration_date IS NULL
      	 GROUP BY lui
      	 HAVING count(DISTINCT cui) > 1
	');

    -- Create a table to hold the cuis
    -- containing ambiguous luis, one CUI per line
    location := '30';
  	MEME_UTILITY.drop_it('table','ambig_lui_pre');
    MEME_UTILITY.exec(
    	'CREATE TABLE ambig_lui_pre (
      		lui	VARCHAR2(10),
      		cui	VARCHAR2(10) )
	');

    MEME_UTILITY.sub_timing_start;

    last_lui := null;

    location := '40';
    EXECUTE IMMEDIATE
	'INSERT INTO ambig_lui_pre (lui,cui)
	 SELECT DISTINCT b.lui, cui
	 FROM tmp_lui a, mrd_classes b
	 WHERE b.expiration_date IS NULL
	   AND a.lui = b.lui';
    row_count := SQL%ROWCOUNT;

    -- Log row count
    location := '75';
    MEME_UTILITY.sub_timing_stop;
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::ambig_prepare',
        detail => 'Finished processing lui ' ||
			row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    location := '80';
    MEME_UTILITY.drop_it('table','tmp_sui');

    -- Get all sui, cui combinations where ther is more
    -- than one cui for this sui.
    location := '90';
    MEME_UTILITY.exec(
    	'CREATE TABLE tmp_sui AS
      	 SELECT DISTINCT a.sui, a.cui
      	 FROM mrd_classes a, mrd_classes b
      	 WHERE a.sui = b.sui
      	   AND a.cui != b.cui
      	   AND a.expiration_date IS NULL
      	   AND b.expiration_date IS NULL
    ');

    -- Create empty table which will contain the cuis belonging to a sui in one
    -- column separated with ",".
    MEME_UTILITY.sub_timing_start;
    location := '100';
    MEME_UTILITY.drop_it('table','ambig_sui_pre');


    location := '110';
    MEME_UTILITY.exec(
    	'CREATE TABLE ambig_sui_pre (
      	    sui		VARCHAR2(10),
      	    cui		VARCHAR2(10)
	 )
    ');

    -- Fill ambig_sui_pre as mentioned above.
    location := '120';
    EXECUTE IMMEDIATE
	'INSERT INTO ambig_sui_pre (sui,cui)
	 SELECT DISTINCT sui,cui FROM tmp_sui';
    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '160';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::ambig_prepare',
        detail => 'Finished processing sui ' ||
			row_count || ' rows processed.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    location := '170';
    MEME_UTILITY.drop_it('table','tmp_sui');
    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log completion of activity
    location := '180';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::ambig_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
	MEME_UTILITY.put_message('str='||str||', cui='||cui);
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END ambig_prepare;

/* PROCEDURE MRCUI_PREPARE *****************************/
--
-- This procedure is responsible for generating the
-- data used to build the following files:
--  MRCUI, DELETED.CUI, DELETED.LUI, DELETED.SUI
--  MERGED.CUI, MERGED.LUI
--
-- Parameter 'meta_previous' is the previous version.
-- If this is 2002AC, it is the release date of 2002AB.
--
-- Parameter 'meta_previous_major' is the last release
-- to have an AA extension.  If this is 2002AC, it is
-- the release date of 2002AA (01-jan-2002).
--
PROCEDURE mrcui_prepare (
      meta_previous	      	IN DATE,
      meta_previous_major   IN DATE := NULL,
      prev_version	      	IN VARCHAR2
)
IS
    TYPE ctype IS REF CURSOR;
    c_var			ctype;
    ct    			INTEGER:=1;
    row_count		INTEGER;
    l_cui			VARCHAR2(10);
    l_rowid			ROWID;
    l_rowid2		ROWID;
BEGIN

    -- Initialize tracking parameters and start timing elements
    initialize_trace('MRCUI_PREPARE');
    MEME_UTILITY.timing_start;

    location := '5';
    DELETE FROM meme_progress
    WHERE activity = 'MRD_RELEASE_OPERATIONS::mrcui_prepare';

    -- Log start
    location := '7';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Starting mrcui_prepare.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => 0);

    --
    -- Create the merged cuis table by finding cases where
    -- multiple CUIs from the last "major" release now
    -- appear in the same CUI.
    --
    MEME_UTILITY.sub_timing_start;
    location := '80';
    MEME_UTILITY.drop_it('table','t_merged_cuis');
    location := '81';
    MEME_UTILITY.exec(
	'CREATE TABLE t_merged_cuis (
		old_cui		VARCHAR2(10) NOT NULL,
		new_cui		VARCHAR2(10) NOT NULL)
	');

    location := '82';
    EXECUTE IMMEDIATE
	'INSERT INTO t_merged_cuis (old_cui,new_cui)
	 SELECT cui1, cui2
         FROM mrd_cui_history
         WHERE expiration_date IS NULL
           AND relationship_name=''SY''
	   AND ver = ''' || prev_version || '''';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '90';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Created and loaded merged CUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);


    --
    -- Split cuis cannot be merged cuis.
    -- Remove "merged cui" cases where the old_cui
    -- is involved in a "CUI split"
    --
    MEME_UTILITY.sub_timing_start;
    location := '100';
    EXECUTE IMMEDIATE
	'DELETE FROM t_merged_cuis WHERE old_cui IN
           (SELECT old_cui FROM t_merged_cuis
	    GROUP BY old_cui HAVING count(DISTINCT new_cui) > 1) ';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '110';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Remove splits from merged CUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Create t_deleted_cuis, These are old cuis that
    -- are not considered "cui merges"
    --
    MEME_UTILITY.sub_timing_start;
    location := '120';
    MEME_UTILITY.drop_it('table','t_deleted_cuis');
    location := '121';
    MEME_UTILITY.exec(
    	'CREATE TABLE t_deleted_cuis (cui VARCHAR2(10) NOT NULL) ');

    location := '122';
    EXECUTE IMMEDIATE
    	'INSERT INTO t_deleted_cuis (cui)
      	 SELECT cui1 FROM mrd_cui_history
	     WHERE expiration_date IS NULL
           AND ver = ''' || prev_version || '''
         MINUS
      	 SELECT old_cui as cui FROM t_merged_cuis';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '130';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Created and load deleted CUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Pick up the string for the deleted CUI
    -- Eventually this will use mrd_classes with
    -- insertion_Date < previous_meta < expiration_date.
    --
    MEME_UTILITY.sub_timing_start;
    location := '136';
    MEME_UTILITY.drop_it('table','t_deleted_cui');

    location := '137';
    MEME_UTILITY.exec(
    	'CREATE TABLE t_deleted_cui (
 	        cui VARCHAR2(10) NOT NULL,
	        string VARCHAR2(3000) NOT NULL )
	');

    location := '137.2';
    EXECUTE IMMEDIATE
    	'INSERT INTO t_deleted_cui (cui, string)
	 SELECT c.cui, string
	 FROM classes a, string_ui b, t_deleted_cuis c,
			termgroup_rank tr, tobereleased_rank tbr
	 WHERE a.last_release_cui = c.cui
	   AND a.released != ''N''
	   AND a.sui = b.sui
	   AND a.termgroup = tr.termgroup
       AND a.tobereleased = tbr.tobereleased
	   AND a.last_release_cui IS NOT NULL
	   AND ( MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      a.last_release_rank, a.sui , a.aui, a.atom_id) ) IN
	      (SELECT max(MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      d.last_release_rank, d.sui , d.aui, d.atom_id))
	       FROM classes d,
			termgroup_rank tr, tobereleased_rank tbr
	       WHERE d.released != ''N''
	  	 AND d.last_release_cui IS NOT NULL
			AND d.termgroup = tr.termgroup
        	AND d.tobereleased = tbr.tobereleased
	       GROUP BY d.last_release_cui) ';

    row_count := SQL%ROWCOUNT;

    location := '137.3';
    EXECUTE IMMEDIATE
    	'INSERT INTO t_deleted_cui (cui, string)
	 SELECT c.cui, string
	 FROM dead_classes a, string_ui b, t_deleted_cuis c,
			termgroup_rank tr, tobereleased_rank tbr
	 WHERE a.last_release_cui = c.cui
	   AND a.released != ''N''
	   AND a.sui = b.sui
	   AND a.termgroup = tr.termgroup
       AND a.tobereleased = tbr.tobereleased
	   AND a.last_release_cui IS NOT NULL
	   AND (MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      a.last_release_rank, a.sui , a.aui, a.atom_id),a.last_release_cui) IN
	      (SELECT MAX(MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      d.last_release_rank, d.sui , d.aui, d.atom_id)),last_release_cui
	       FROM dead_classes d,
			termgroup_rank tr, tobereleased_rank tbr
	       WHERE d.last_release_cui IS NOT NULL
	       AND d.released != ''N''
			AND d.termgroup = tr.termgroup
        	AND d.tobereleased = tbr.tobereleased
	       GROUP BY last_release_cui)
	  AND c.cui IN (SELECT cui FROM t_deleted_cuis
			MINUS SELECT cui FROM t_deleted_cui) ';

    row_count := row_count + SQL%ROWCOUNT;

/*******  THIS SECTION NO LONGER APPLIES AS classes_feedback IS DEPRECATED
*    location := '137.4';
*    EXECUTE IMMEDIATE
* 	 'INSERT INTO t_deleted_cui (cui, string)
*	 SELECT c.cui, string
*	 FROM dead_classes a, string_ui b, t_deleted_cuis c, classes_feedback d
*	 WHERE a.atom_id = d.atom_id
*	   AND a.released != ''N''
*	   AND d.lrc = cui
*	   AND a.sui = b.sui
*	   AND d.lrc IS NOT NULL
*	   AND (a.rank || a.sui || lpad(a.atom_id,10,0),d.lrc) IN
*	      (SELECT MAX(e.rank || e.sui || lpad(e.atom_id,10,0)),f.lrc
*	       FROM dead_classes e, classes_feedback f
*	       WHERE f.lrc IS NOT NULL
*		 AND e.released != ''N''
*		 AND e.atom_id = f.atom_id
*	       GROUP BY f.lrc)
*	  AND c.cui IN (SELECT cui FROM t_deleted_cuis
*			MINUS SELECT cui FROM t_deleted_cui) ';
*
*   row_count := row_count + SQL%ROWCOUNT;
*
*    location := '137.5';
*    EXECUTE IMMEDIATE
*   	'INSERT INTO t_deleted_cui (cui, string)
*	 SELECT c.cui, string
*	 FROM classes a, string_ui b, t_deleted_cuis c, classes_feedback d
*	 WHERE a.atom_id = d.atom_id
*	   AND a.released != ''N''
*	   AND d.lrc = cui
*	   AND a.sui = b.sui
*	   AND d.lrc IS NOT NULL
*	   AND (a.rank || a.sui || lpad(a.atom_id,10,0),d.lrc) IN
*	      (SELECT MAX(e.rank || e.sui || lpad(e.atom_id,10,0)),f.lrc
*	       FROM classes e, classes_feedback f
*	       WHERE f.lrc IS NOT NULL
*		 AND e.released != ''N''
*		 AND e.atom_id = f.atom_id
*	       GROUP BY f.lrc)
*	  AND c.cui IN (SELECT cui FROM t_deleted_cuis
*			MINUS SELECT cui FROM t_deleted_cui) ';
*
*    row_count := row_count + SQL%ROWCOUNT;
********************************************************************************/

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '138';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Loaded strings for deleted CUIs: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- "old luis" are either luis that appeared in the
    -- last major release that do not appear in this
    -- release, or they are luis that got "tweaked"
    -- in the lui re-assignment process when norm changed.
    -- What we need to do is to combine the luis from
    -- the last release that no longer exist with
    -- all luis that no longer exist from the lui
    -- assignment algorithm.
    --
    MEME_UTILITY.sub_timing_start;
    location := '140';
    MEME_UTILITY.drop_it('table', 't_old_luis');
    -- Create t_old_luis
    MEME_UTILITY.sub_timing_start;
    location := '141';
    MEME_UTILITY.exec (
        'CREATE TABLE t_old_luis ( lui VARCHAR2(10) NOT NULL ) ');
/*
  	MEME_UTILITY.exec(
    	'CREATE TABLE t_old_luis AS ( '||
      	'SELECT sui, lui '||
      	'FROM mrd_classes '||
      	'WHERE insertion_date < '''|| meta_previous_major ||
      	''' AND expiration_date >=  '''|| meta_previous_major ||
      	''')');
*/
    location := '142';
    EXECUTE IMMEDIATE
    	'INSERT INTO t_old_luis (lui)
      	 ((SELECT lui
      	   FROM classes where released != ''N''
	   UNION
	   SELECT lui
	   FROM foreign_classes where released !=''N''
	   UNION
	   SELECT lui
	   FROM dead_classes WHERE released != ''N'')
	  MINUS
	  SELECT lui FROM mrd_classes WHERE expiration_date IS NULL)
	 UNION
	 (SELECT old_lui FROM lui_assignment
	  MINUS
	  SELECT new_lui FROM lui_assignment) ';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '150';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Created and loaded old LUIs table: ' || row_count ||' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);
    --
    -- Delete the new LUI that are not part of the previous release Seibel Ticket 1-D62PI
    -- Changes needed to support mixed-length LUIs
    --
    MEME_UTILITY.sub_timing_start;
    location := '150.1';
    EXECUTE IMMEDIATE
        'DELETE FROM t_old_luis WHERE to_number(substr(lui,2)) >
           (SELECT max(to_number(substr(lui,2))) from
                (SELECT old_lui lui FROM lui_assignment WHERE sui in
                       (SELECT sui FROM classes WHERE released=''A''
                        UNION
                        SELECT sui FROM dead_classes WHERE released=''A''
                        UNION
                        SELECT sui FROM foreign_classes WHERE released=''A'')
                 UNION ALL
                 SELECT lui FROM classes WHERE released=''A''
                 AND (SELECT count(*) FROM lui_assignment) = 0
                 UNION ALL
                 SELECT lui FROM foreign_classes WHERE released=''A''
                 AND (SELECT count(*) FROM lui_assignment) = 0
                 UNION ALL
                 SELECT lui FROM dead_classes WHERE released=''A''
                 AND (SELECT COUNT(*) FROM lui_assignment) = 0
                       ) 
            WHERE lui IS NOT null)';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '150.2';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Remove any false positive LUI from old luis table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);
        
        
    --
    -- Now find the "merged luis" which are t_old_luis from
    -- lui assignment that have different luis assigned
    -- to them.
    --
    MEME_UTILITY.sub_timing_start;
    location := '180';
    MEME_UTILITY.drop_it('table', 't_merged_luis');
    location := '183';
    MEME_UTILITY.exec(
	'CREATE TABLE t_merged_luis (
	    old_lui	VARCHAR2(10) NOT NULL,
	    new_lui	VARCHAR2(10) NOT NULL )
	');

    location := '184';
    EXECUTE IMMEDIATE
	'INSERT INTO t_merged_luis (old_lui, new_lui)
	 SELECT DISTINCT old_lui, new_lui
	 FROM lui_assignment
	 WHERE old_lui != new_lui';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '190';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Created and loaded merged LUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- "live" LUIs cannot be merged luis so remove them from the candidate list
    --
    MEME_UTILITY.sub_timing_start;
    location := '200';
    EXECUTE IMMEDIATE
    	'DELETE FROM t_merged_luis WHERE old_lui IN
	   (SELECT lui FROM mrd_classes WHERE expiration_date IS NULL)';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '210';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Remove live LUI1 from merged LUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Only "live" luis can be "new" luis
    -- Any "new" luis that are not being released should be removed
    -- from the candidatelist
    --
    MEME_UTILITY.sub_timing_start;
    location := '214';
    EXECUTE IMMEDIATE
    	'DELETE FROM t_merged_luis WHERE new_lui IN
	   (SELECT new_lui FROM t_merged_luis
	    MINUS SELECT lui FROM mrd_classes WHERE expiration_date IS NULL)';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '215';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Remove dead LUI2 from merged LUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Split luis cannot be merged luis.
    -- Remove "merged lui" cases where the old_lui
    -- is involved in a "LUI split"
    --
    MEME_UTILITY.sub_timing_start;
    location := '200';
    EXECUTE IMMEDIATE
    	'DELETE FROM t_merged_luis WHERE old_lui IN
	   (SELECT old_lui FROM t_merged_luis
	    GROUP BY old_lui HAVING count(DISTINCT new_lui) > 1 )';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '210';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Remove split LUI1 from merged LUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Now the t_old_luis in t_merged_luis are exactly the luis lost through merging
    -- since the last release. The deleted luis are the luis in t_old_luis which
    -- got not merged.
    --

    -- Create t_deleted_luis
    MEME_UTILITY.sub_timing_start;
    location := '220';
    MEME_UTILITY.drop_it('table','t_deleted_luis');
    location := '221';
    MEME_UTILITY.exec(
	'CREATE TABLE t_deleted_luis ( lui VARCHAR2(10) NOT NULL ) ');

    location := '222';
    EXECUTE IMMEDIATE
	'INSERT INTO t_deleted_luis
         SELECT lui FROM t_old_luis
         MINUS
         SELECT old_lui as lui FROM t_merged_luis ';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '230';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Created and loaded deleted LUIs table: ' ||row_count||' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Now we have the list of candidate deleted luis
    -- the next step is to get the strings to represent them.
    -- Eventually, this should be replaced with a lookup
    -- in mrd_classes ala pflag_l='L' and insertion_date <
    -- previous_meta < expiration_date
    --
    MEME_UTILITY.sub_timing_start;
    location := '231';
    MEME_UTILITY.drop_it('table','t_deleted_lui');
    location := '232';
    MEME_UTILITY.exec(
	'CREATE table t_deleted_lui(
		lui         VARCHAR2(10) NOT NULL,
		string      VARCHAR2(3000) NOT NULL
         ) STORAGE (INITIAL 20M)   ');

    location := '232.1';
    EXECUTE IMMEDIATE
	'INSERT INTO t_deleted_lui (lui,string)
	 SELECT b.lui, string
	 FROM string_ui a,
	      (SELECT b.lui,
		MAX(MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      a.last_release_rank, a.sui , a.aui, a.atom_id) || ''/'' || a.sui)
		  as rank
	       FROM classes a, t_deleted_luis b,
		    lui_assignment c,
			termgroup_rank tr, tobereleased_rank tbr
        	       WHERE b.lui = c.old_lui
		 AND a.sui = c.sui
	         AND a.released != ''N''
			AND a.termgroup = tr.termgroup
        	AND a.tobereleased = tbr.tobereleased 
	       GROUP BY b.lui ) b
	 WHERE a.sui = SUBSTR(rank,instr(rank,''/'')+1)';

    row_count := SQL%ROWCOUNT;

    location := '232.2';
    EXECUTE IMMEDIATE
	'INSERT INTO t_deleted_lui (lui,string)
	 SELECT b.lui, string
	 FROM string_ui a,
	      (SELECT b.lui,
		MAX(MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      a.last_release_rank, a.sui , a.aui, a.atom_id) || ''/'' || a.sui)
		  as rank
	       FROM classes a, t_deleted_luis b,
			termgroup_rank tr, tobereleased_rank tbr
	       WHERE b.lui = a.lui
	         AND a.released != ''N''
			AND a.termgroup = tr.termgroup
        	AND a.tobereleased = tbr.tobereleased 
	       GROUP BY b.lui ) b
	 WHERE a.sui = SUBSTR(rank,instr(rank,''/'')+1) ';

    row_count := row_count + SQL%ROWCOUNT;

    location := '232.3';
    EXECUTE IMMEDIATE
	'INSERT INTO t_deleted_lui (lui,string)
	 SELECT b.lui, string
	 FROM string_ui a,
	      (SELECT a.lui,
		MAX(MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      a.last_release_rank, a.sui , a.aui, a.atom_id) || ''/'' || a.sui)
		  as rank
	       FROM foreign_classes a, t_deleted_luis b,
			termgroup_rank tr, tobereleased_rank tbr
	       WHERE a.lui = b.lui
	         AND a.released != ''N''
			AND a.termgroup = tr.termgroup
        	AND a.tobereleased = tbr.tobereleased 
	       GROUP BY a.lui ) b
	 WHERE a.lui = b.lui and a.sui = SUBSTR(rank,instr(rank,''/'')+1)';

    row_count := row_count + SQL%ROWCOUNT;

    commit;
    location := '232.4';
    EXECUTE IMMEDIATE
	'INSERT INTO t_deleted_lui (lui,string)
	 SELECT b.lui, string
	 FROM string_ui a,
	      (SELECT /*+ ORDERED USE_HASH(b,c) USE_HASH(a,c) */ b.lui,
		MAX(MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      a.last_release_rank, a.sui , a.aui, a.atom_id) || ''/'' || a.sui)
		  as rank
	       FROM t_deleted_luis b, lui_assignment c, dead_classes a,
			termgroup_rank tr, tobereleased_rank tbr
	       WHERE b.lui = c.old_lui
		 AND a.sui = c.sui
	         AND a.released != ''N''
			AND a.termgroup = tr.termgroup
        	AND a.tobereleased = tbr.tobereleased 
	       GROUP BY b.lui ) b
	 WHERE a.sui = SUBSTR(rank,instr(rank,''/'')+1)
	   AND a.lui in (SELECT lui FROM t_deleted_luis
			MINUS SELECT lui FROM t_deleted_lui)';

    row_count := row_count + SQL%ROWCOUNT;

    location := '232.5';
    EXECUTE IMMEDIATE
	'INSERT INTO t_deleted_lui (lui,string)
	 SELECT b.lui, string
	 FROM string_ui a,
	      (SELECT b.lui,
		MAX(MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
	      a.last_release_rank, a.sui , a.aui, a.atom_id) || ''/'' || a.sui)
		  as rank
	       FROM dead_classes a, t_deleted_luis b,
			termgroup_rank tr, tobereleased_rank tbr
	       WHERE b.lui = a.lui
	         AND a.released != ''N''
			AND a.termgroup = tr.termgroup
        	AND a.tobereleased = tbr.tobereleased 
	       GROUP BY b.lui ) b
	 WHERE a.sui = SUBSTR(rank,instr(rank,''/'')+1)
	   AND a.lui in (SELECT lui FROM t_deleted_luis
			MINUS SELECT lui FROM t_deleted_lui)';

   row_count := row_count + SQL%ROWCOUNT;

    --
    -- In the end t_deleted_lui may have less rows than
    -- t_deleted_luis.  This is because lui_assignment exists
    -- for ALL sui including ones released in past years.  So
    -- LUIs that became deleted in the past and never came back
    -- into classes will appear in t_deleted_luis but not in
    -- t_deleted_lui
    --
    -- As of 2003AB we believe that t_deleted_lui should have
    -- same number of rows because we do investigation in dead_classes
    --

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '233';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Loaded strings for deleted LUIs: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Get all suis that appeared in the last "major release"
    -- Currently we do this from classes because the
    -- relevant data is not in mrd_classes yet.
    --
    MEME_UTILITY.sub_timing_start;
    location := '240';
    MEME_UTILITY.drop_it('table','t_old_suis');
    location := '240.2';
    MEME_UTILITY.exec(
	'CREATE TABLE t_old_suis ( sui VARCHAR2(10) NOT NULL )');
/*
   	MEME_UTILITY.exec(
    'CREATE TABLE t_old_suis AS ('||
      'SELECT sui FROM mrd_classes '||
      'WHERE insertion_date < '''|| meta_previous_major ||
      ''' AND expiration_date >=  '''|| meta_previous_major ||
      ''')');
*/
    location := '241';
    EXECUTE IMMEDIATE
	'INSERT INTO t_old_suis (sui)
	 (SELECT sui FROM classes WHERE released != ''N''
	  UNION
	  SELECT sui FROM foreign_classes WHERE released != ''N''
	  UNION
	  SELECT sui FROM dead_classes WHERE released != ''N'' )';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '250';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Created and loaded old SUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Create t_deleted_suis
    -- Find cases of suis that are no longer active
    --
    MEME_UTILITY.sub_timing_start;
    location := '280';
    MEME_UTILITY.drop_it('table','t_deleted_suis');
    location := '281';
    MEME_UTILITY.exec(
    	'CREATE TABLE t_deleted_suis ( sui VARCHAR2(10) NOT NULL ) ');
    location := '282';
    EXECUTE IMMEDIATE
    	'INSERT INTO t_deleted_suis (sui)
         SELECT sui FROM t_old_suis
         MINUS
         SELECT sui FROM mrd_classes
      	 WHERE expiration_date IS NULL';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '290';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Created and loaded deleted SUIs table: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Now we have candidate suis, look up the strings
    -- Eventually this will be a lookup in mrd_classes
    -- where insertion_date < previous_meta < expiration_date
    --
    MEME_UTILITY.sub_timing_start;
    location := '291';
    MEME_UTILITY.drop_it('table','t_deleted_sui');
    location := '292';
    MEME_UTILITY.exec(
    	'CREATE TABLE t_deleted_sui (
	    sui VARCHAR2(10) NOT NULL,
	    language VARCHAR2(10) NOT NULL,
	    string VARCHAR2(3000) NOT NULL )
	');
    location := '293';
    EXECUTE IMMEDIATE
    	'INSERT INTO t_deleted_sui (sui,language,string)
	 SELECT b.sui, language, string
	 FROM t_deleted_suis a, string_ui b
	 WHERE a.sui = b.sui';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '290';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Created and loaded deleted SUIs table with strings: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    --
    -- Build MRCUI
    --


    --
    -- Create MRCUI table
    --
    MEME_UTILITY.sub_timing_start;
    location := '370';
    MEME_UTILITY.drop_it('table','mrcui_pre');
    location := '380';
    MEME_UTILITY.exec(
    	'CREATE TABLE mrcui_pre(
    		cui1		VARCHAR2(10) NOT NULL,
    		ver		VARCHAR2(50),
    		rel		VARCHAR2(10) NOT NULL,
    		rela		VARCHAR2(100) ,
    		map_reason	VARCHAR2(4000) ,
    		cui2		VARCHAR2(10),
    		mapin		VARCHAR2(1)) ');


      location := '390';
    EXECUTE IMMEDIATE
       'INSERT INTO mrcui_pre
        SELECT cui1, ver, relationship_name, relationship_attribute,
               map_reason, cui2,
               DECODE(relationship_name,''DEL'','''',''Y'')
        FROM mrd_cui_history WHERE expiration_Date IS NULL';

    row_count := SQL%ROWCOUNT;

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '610';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Loading mrcui_pre: ' || row_count || ' rows.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    COMMIT;

    -- CLEANUP
    MEME_UTILITY.drop_it('table', 't_old_luis');
    MEME_UTILITY.drop_it('table', 't_deleted_luis');
    MEME_UTILITY.drop_it('table', 't_old_suis');
    MEME_UTILITY.drop_it('table', 't_deleted_suis');
    MEME_UTILITY.drop_it('table', 't_bequeathal_cuis');
    -- MEME_UTILITY.drop_it('table', 't_merged_cuis');

    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log completion of activity
    location := '450';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mrcui_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mrcui_prepare;

/* PROCEDURE MRAUI_PREPARE *****************************/
--
-- This procedure is responsible for generating the
-- data used to build MRAUI.RRF
--
PROCEDURE mraui_prepare
IS
BEGIN

    -- Initialize tracking parameters and start timing elements
    initialize_trace('MRAUI_PREPARE');
    MEME_UTILITY.timing_start;
    MEME_UTILITY.sub_timing_start;

    --
    -- Create MRAUI table
    --
    MEME_UTILITY.sub_timing_start;
    location := '10';
    MEME_UTILITY.drop_it('table','mraui_pre');
    location := '20';
    MEME_UTILITY.exec(
    	'CREATE TABLE mraui_pre(
    		aui1		VARCHAR2(10) NOT NULL,
    		cui1		VARCHAR2(10) NOT NULL,
    		ver		VARCHAR2(50),
    		rel		VARCHAR2(10),
    		rela		VARCHAR2(100),
    		mapreason	VARCHAR2(4000) NOT NULL,
    		aui2		VARCHAR2(10),
    		cui2		VARCHAR2(10),
    		mapin		VARCHAR2(1)) ');

    --
    -- Insert AUI mappings
    --
    MEME_UTILITY.sub_timing_start;
    location := '30';
    EXECUTE IMMEDIATE
	'INSERT INTO mraui_pre
	   (aui1, cui1, ver, rel, rela, mapreason, aui2, cui2, mapin)
	 SELECT aui1, cui1, ver, relationship_name,
		relationship_attribute, map_reason, aui2, cui2, ''Y''
	 FROM mrd_aui_history
 	 WHERE expiration_date IS NULL';

    -- Log row count
    MEME_UTILITY.sub_timing_stop;
    location := '40';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mraui_prepare',
        detail => 'Load data: ' || SQL%ROWCOUNT || ' rows',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.sub_elapsed_time);

    -- Log for the whole procedure
    MEME_UTILITY.timing_stop;

    -- Log completion of activity
    location := '50';
    log_progress(
        authority => 'RELEASE',
        activity => 'MRD_RELEASE_OPERATIONS::mraui_prepare',
        detail => 'Procedure completed successfully.',
        transaction_id => 0,
        work_id => 0,
        elapsed_time => MEME_UTILITY.elapsed_time);

EXCEPTION

    WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
        RAISE mrd_release_exception;

END mraui_prepare;

/* PROCEDURE APPLY_CVF *****************************/
--
-- This procedure is responsible for generating the
--  value of cvf field.
--
-- Parameter 'table_name' is the name of the target file.
--
-- Parameter 'key_field' is the value of aui,rui,atui,sui or cui.
--
-- Parameter 'cascade_field' is the value of aui,metaui,sui or aui1.

PROCEDURE apply_cvf(
      table_name	IN VARCHAR2,
      key_field		IN VARCHAR2,
      cascade_field	IN VARCHAR2,
      cui_field		IN VARCHAR2
    )
IS
    row_count		INTEGER;
BEGIN

	location := '25';
	IF ( key_field IS NOT NULL) THEN
	    location := '30';
    	MEME_UTILITY.sub_timing_start;
    	
    
	EXECUTE IMMEDIATE
	    'UPDATE /*+ PARALLEL(a) */ ' || table_name || ' a
	     SET cvf =
	        (SELECT NVL(cvf,0) - BITAND(NVL(cvf,0), code) + code
	     	 FROM mrd_content_view_members
	     	 WHERE a.' || key_field || ' = meta_ui
                   AND cascade = :cascade
	       	   AND expiration_date IS NULL)
  	     WHERE a.' || key_field || ' IN
		(SELECT meta_ui FROM mrd_content_view_members
		 WHERE cascade = :cascade
		   AND expiration_date IS NULL)'
        USING 'Y','Y';
    	row_count := SQL%ROWCOUNT;
    	
    COMMIT;

	EXECUTE IMMEDIATE
	    'UPDATE /*+ PARALLEL(a) */ ' || table_name || ' a
	     SET cvf =
	        (SELECT NVL(cvf,0) - BITAND(NVL(cvf,0), code) + code
	     	 FROM mrd_content_view_members
	     	 WHERE a.' || key_field || ' = meta_ui
                   AND cascade = :cascade
	       	   AND expiration_date IS NULL)
  	     WHERE a.' || key_field || ' IN
		(SELECT meta_ui FROM mrd_content_view_members
		 WHERE cascade = :cascade
		   AND expiration_date IS NULL)'
        USING 'N','N';
    	row_count := row_count + SQL%ROWCOUNT;
    	
	COMMIT;

	-- Log row count
    	MEME_UTILITY.sub_timing_stop;
    	location := '30.5';
    	log_progress(
        	authority => 'RELEASE',
        	activity => 'MRD_RELEASE_OPERATIONS::apply_cvf',
        	detail => 'Set cvf values: ' || row_count || ' rows.',
        	transaction_id => 0,
        	work_id => 0,
        	elapsed_time => MEME_UTILITY.sub_elapsed_time);

	END IF;
	location := '40';
	IF (cascade_field IS NOT NULL) THEN
	    location := '50';
    	    MEME_UTILITY.sub_timing_start;
		-- RXNORM CVF 4096 does not cascade to all attributes.
		-- attribute_name list in this query needs to be refreshed every release.
		-- 2015AA onwards, RXNORM CVF 4096 cascades to all attributes.
    	    EXECUTE IMMEDIATE
		  	'UPDATE /*+ PARALLEL(a) */ ' || table_name || ' a
		   	 SET cvf =
		    	  (SELECT NVL(cvf,0) - BITAND(NVL(cvf,0), code) + code
		     	  FROM mrd_content_view_members
		     	  WHERE a.' || cascade_field || ' = meta_ui
		       	    AND cascade = :cascade
			    AND expiration_date IS NULL)
	      	         WHERE a.' || cascade_field || ' IN
			   (SELECT meta_ui FROM mrd_content_view_members
			    WHERE cascade = :cascade
			      AND expiration_date IS NULL)'
		    USING 'Y','Y';
    	    row_count := SQL%ROWCOUNT;

	    COMMIT;

	    -- Log row count
    	    MEME_UTILITY.sub_timing_stop;
    	    location := '50.5';
    	    log_progress(
        	authority => 'RELEASE',
        	activity => 'MRD_RELEASE_OPERATIONS::apply_cvf',
        	detail => 'Set cascade field cvf values: ' || row_count || ' rows.',
        	transaction_id => 0,
        	work_id => 0,
        	elapsed_time => MEME_UTILITY.sub_elapsed_time);

	END IF;
	location := '60';
	IF ( cui_field IS NOT NULL) THEN
	    location := '70';
    	    MEME_UTILITY.sub_timing_start;
		-- RXNORM CVF 4096 does not cascade to all attributes.
		-- attribute_name list in this query needs to be refreshed every release.
		-- 2015AA onwards, RXNORM CVF 4096 cascades to all attributes.
		    EXECUTE IMMEDIATE
		  	'UPDATE /*+ PARALLEL(a) */ ' || table_name || ' a
		   	 SET cvf =
		    	  (SELECT NVL(cvf,0) - BITAND(NVL(cvf,0), code) + code
		     	  FROM mrd_content_view_members
		     	  WHERE a.' || cui_field || ' = meta_ui
		       	    AND cascade = :cascade
			    AND expiration_date IS NULL)
	      	         WHERE a.' || cui_field || ' IN
			   (SELECT meta_ui FROM mrd_content_view_members
			    WHERE cascade = :cascade
			      AND expiration_date IS NULL)'
		    USING 'Y','Y';

	    COMMIT;

	    -- Log row count
	    MEME_UTILITY.sub_timing_stop;
    	location := '50.5';
    	log_progress(
        	authority => 'RELEASE',
        	activity => 'MRD_RELEASE_OPERATIONS::apply_cvf',
        	detail => 'Set cui field cvf values: ' || row_count || ' rows.',
        	transaction_id => 0,
        	work_id => 0,
        	elapsed_time => MEME_UTILITY.sub_elapsed_time);
	END IF;

EXCEPTION

   WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
       RAISE mrd_release_exception;

END apply_cvf;

/* PROCEDURE APPLY_STY_CVF *****************************/
--
-- This procedure is responsible for generating the
--  value of cvf field  for Semantic Types
-- Parameter 'table_name' is the name of the target file.
-- Parameter 'cui_field' is the name of the cui field.
--

PROCEDURE apply_sty_cvf(
      table_name	IN VARCHAR2,
      cui_field		IN VARCHAR2
    )
IS
    CURSOR code_cursor IS
    SELECT DISTINCT code
    FROM mrd_content_view_members
    WHERE expiration_date IS NULL;
    
    row_count		INTEGER;
    code_row		code_cursor%ROWTYPE;
BEGIN

    OPEN code_cursor;
    LOOP
        location := '70.2';
	FETCH code_cursor INTO code_row;
	EXIT WHEN code_cursor%NOTFOUND;
	    location := '70';
    	MEME_UTILITY.sub_timing_start;
    	
    	
	    EXECUTE IMMEDIATE
	  	'UPDATE /*+ PARALLEL(a) */ ' || table_name || ' a
	   	 SET cvf = 
	    	  (SELECT NVL(cvf,0) - BITAND(NVL(cvf,0), c.code) + c.code
	     	  	FROM mrd_content_view_members c, mrd_classes b
	     	  	WHERE a.' || cui_field || ' = b.cui
	     	  	  AND c.code = ' || code_row.code || '
	     	  	  AND meta_ui = b.aui
			    	  AND c.expiration_date IS NULL
		    	  AND b.expiration_date IS NULL
		    	GROUP BY c.code)
      	 WHERE a.' || cui_field || ' IN
		   (SELECT cui FROM mrd_content_view_members c, mrd_classes b
	     	  WHERE c.code = ' || code_row.code || '
	     	  	AND meta_ui = b.aui
			    	AND c.expiration_date IS NULL
		    	AND b.expiration_date IS NULL)';
    	row_count := SQL%ROWCOUNT + row_count;
    location := '70.4';
    END LOOP;
    CLOSE code_cursor;
	COMMIT;

	    -- Log row count
	    MEME_UTILITY.sub_timing_stop;
    	location := '50.5';
    	log_progress(
        	authority => 'RELEASE',
        	activity => 'MRD_RELEASE_OPERATIONS::apply_sty_cvf',
        	detail => 'Set sty cvf values: ' || row_count || ' rows.',
        	transaction_id => 0,
        	work_id => 0,
        	elapsed_time => MEME_UTILITY.sub_elapsed_time);

EXCEPTION

   WHEN OTHERS THEN
        mrd_release_operations_error(method,location,err_code,SQLERRM);
       RAISE mrd_release_exception;

END apply_sty_cvf;

/* PROCEDURE SELF_TEST *****************************/
PROCEDURE self_test
IS
BEGIN
   -- SERVEROUTPUT must be on to see this.

   DBMS_OUTPUT.PUT_LINE('Self_test');

END self_test;

/* PROCEDURE HELP *****************************/
PROCEDURE help
IS
BEGIN
   help('');
END;

/* PROCEDURE HELP *****************************/
PROCEDURE help( method_name IN VARCHAR2)
IS
BEGIN

   -- This procedure requires SET SERVEROUTPUT ON

  DBMS_OUTPUT.PUT_LINE('.');

  IF method_name IS NULL OR method_name = '' THEN
    DBMS_OUTPUT.PUT_LINE('. This package provides procedures used to create ');
    DBMS_OUTPUT.PUT_LINE('. the different MR release files. ');
  ELSE
    DBMS_OUTPUT.PUT_LINE('.There is no help for the topic: "' || method_name || '".');
  END IF;

   -- Print version
   DBMS_OUTPUT.PUT_LINE('.');
   version;

END help;

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
	elapsed_time	IN INTEGER DEFAULT 0
)
IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    row_sequence	INTEGER;
BEGIN

    -- this block needs to have an autonomous transaction scope


	MEME_UTILITY.PUT_MESSAGE(detail);

    -- If work_id != 0 then use incremental sequence numbers
    IF work_id != 0 THEN

	INSERT INTO meme_progress
	    (row_sequence,transaction_id, work_id, authority,
	     elapsed_time, timestamp, activity, detail)
	SELECT
	    NVL(max(row_sequence+1),1),
	    log_progress.transaction_id, log_progress.work_id,
	    log_progress.authority, log_progress.elapsed_time,
	    SYSDATE, log_progress.activity, log_progress.detail
	FROM meme_progress
	WHERE work_id = log_progress.work_id;
    ELSE
	INSERT INTO meme_progress
	    (transaction_id, work_id, authority, elapsed_time,
	     timestamp, activity, detail)
	VALUES
	    (transaction_id, work_id, authority, log_progress.elapsed_time,
	    SYSDATE, activity, detail);
    END IF;

    COMMIT;

END log_progress;

END MRD_RELEASE_OPERATIONS;
/
SHOW ERRORS



set serveroutput on size 100000
execute MRD_RELEASE_OPERATIONS.help;
