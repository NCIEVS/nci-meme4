CREATE OR REPLACE PACKAGE MEME_CONSTANTS AS
/*****************************************************************************
 *
 * PL/SQL File: MEME_CONSTANTS.sql
 *
 * This file contains the MEME_CONSTANTS package
 *
 * Version Information
 *
 * 	9/10/2001:	TN_FOREIGN_CLASSES added.
 *	11/10/2000:	Released
 *	10/11/2000:	Additional constants variables
 *	9/12/2000:	AA_CHANGE_ATOM_ID
 *	8/1/2000:	Package handover version
 *	5/9/2000:	Released to NLM
 *	3/6/2000:	This package does not conform to the standard API
 * 			because it contains no functionality and needs to
 * 			precede MEME_UTILITY in the dependency tree
 *	9/9/1999:	First version created and compiled
 *     			Released
 * Status:
 *	Functionality:	DONE
 *	Testing:	DONE
 * 	Enhancements:
 *		Have all PLSQL packages use standard error codes/messages.
 *		Don't use MEME_UTILITY methods.
 *
 *****************************************************************************/

    package_name	VARCHAR2(25) := 'MEME_CONSTANTS';
    release_number	VARCHAR2(1)  := '4';
    version_number	VARCHAR2(5)  := '6.0';
    version_date	DATE	     := '10-Sep-2000';
    version_authority	VARCHAR2(3)  := 'BAC';

    -- Useful Generic Constants
    YES				CONSTANT VARCHAR2(1) := 'Y';
    NO				CONSTANT VARCHAR2(1) := 'N';
    CLUSTER_YES			CONSTANT INTEGER := 1;
    CLUSTER_NO			CONSTANT INTEGER := 0;
    DATE_MASK			CONSTANT VARCHAR2(50)
	:= 'DD-mon-YYYY HH24:MI:SS';
    NO_FIELDS_CHANGED		CONSTANT VARCHAR2(12) := 'NO_CHANGE';
    NO_TOPIC			CONSTANT VARCHAR2(1) := '';
    EMPTY_TABLE 		CONSTANT VARCHAR2(50) := 'EMPTY_TABLE';
    TMP_PREFIX_PATTERN		CONSTANT VARCHAR2(5) := 'T\_%';
    ESCAPE			CONSTANT VARCHAR2(5) := '\';
    LINE_BREAK			CONSTANT INTEGER := 250;
    COMMIT_INTERVAL		CONSTANT INTEGER := 10000;
    TMP_TABLE_PREFIX		CONSTANT VARCHAR2(5) := 'T_';
    SN_PRE			CONSTANT VARCHAR2(4) := 'PRE';
    SN_POST			CONSTANT VARCHAR2(4) := 'POST';
    SN_DIFF			CONSTANT VARCHAR2(4) := 'DIFF';
    IV_MATRIX			CONSTANT VARCHAR2(20) := 'MATRIX_INITIALIZER';
    IV_SNAPSHOT			CONSTANT VARCHAR2(20) := 'INTEGRITY_SNAPSHOT';
    FIELD_NOT_FOUND		CONSTANT VARCHAR2(20) := 'FIELD NOT FOUND';
    IV_DEFAULT			CONSTANT VARCHAR2(20) := 'DEFAULT';
    MW_SNAPSHOT			CONSTANT VARCHAR2(20) := 'SNAPSHOT';
    MW_INITIALIZE		CONSTANT VARCHAR2(20) := 'INITIALIZE';
    MW_MATRIX			CONSTANT VARCHAR2(20) := 'MATRIX';
    MW_MAINTENANCE		CONSTANT VARCHAR2(20) := 'MAINTENANCE';
    MATRIX_AUTHORITY		CONSTANT VARCHAR2(20) := 'MATRIXINIT';
    SNAPSHOT_AUTHORITY		CONSTANT VARCHAR2(20) := 'SNAPSHOT';
    SYSTEM_AUTHORITY		CONSTANT VARCHAR2(20) := 'SYSTEM';
    TESTER_AUTHORITY		CONSTANT VARCHAR2(20) := 'TESTER';
    WMS_USER			CONSTANT VARCHAR2(20) := 'MEOW';
    MID_USER			CONSTANT VARCHAR2(20) := 'MTH';
    MID_MODE			CONSTANT VARCHAR2(20) := 'MID';
    MRD_MODE			CONSTANT VARCHAR2(20) := 'MRD';
    CTL_FILE			CONSTANT VARCHAR2(20) := 'CTL';
    DAT_FILE			CONSTANT VARCHAR2(20) := 'DAT';
    BATCH_ACTION		CONSTANT VARCHAR2(20) := 'BATCH_ACTION';
    MACRO_ACTION		CONSTANT VARCHAR2(20) := 'MACRO_ACTION';

    -- Error Constants (code_map could map these to messages)
    ER_OK		 	CONSTANT INTEGER := 0;
    APPLICATION_ERROR_NUMBER 	CONSTANT INTEGER := -20005;
    APROC_ERROR_NUMBER		CONSTANT INTEGER := -20005;
    ER_BAD_ROW_COUNT		CONSTANT INTEGER := -100;

    ER_BAD_PARAMETER_VALUE	CONSTANT INTEGER := -200;
    ER_BAD_RETURN_VALUE 	CONSTANT INTEGER := -300;
    ER_BAD_LEVEL_VALUE		CONSTANT INTEGER := -400;
    ER_BAD_STATUS_VALUE		CONSTANT INTEGER := -500;
    ER_BAD_LEVEL_STATUS_VALUE	CONSTANT INTEGER := -600;
    ER_BAD_GENERATED_VALUE	CONSTANT INTEGER := -700;
    ER_BAD_RELEASED_VALUE	CONSTANT INTEGER := -800;
    ER_BAD_TOBERELEASED_VALUE	CONSTANT INTEGER := -900;
    ER_BAD_SUPPRESSIBLE_VALUE	CONSTANT INTEGER := -1000;
    ER_BAD_SOURCE_AUTHORITY_VALUE CONSTANT INTEGER := -1100;
    ER_BAD_SOURCE_VALUE		CONSTANT INTEGER := -1200;
    ER_BAD_TERMGROUP_VALUE	CONSTANT INTEGER := -1300;
    ER_BAD_REL_NAME_VALUE	CONSTANT INTEGER := -1400;
    ER_BAD_REL_ATTRIBUTE_VALUE	CONSTANT INTEGER := -1500;
    ER_INVALID_FIELD		CONSTANT INTEGER := -1600;
    ER_APROC_FAILED		CONSTANT INTEGER := -1700;
    ER_EXEC_FAILED		CONSTANT INTEGER := -1800;
    ER_UNIMPLEMENTED_CALL	CONSTANT INTEGER := -1900;
    ER_NONEXISTENT_OBJECT	CONSTANT INTEGER := -2000;
    ER_TRANSACTION_OVERLAP	CONSTANT INTEGER := -2100;
    ER_EMPTY_TABLE		CONSTANT INTEGER := -2200;
    ER_IDS_NOT_UNIQUE		CONSTANT INTEGER := -2300;
    ER_NO_DATA_FOUND		CONSTANT INTEGER := -2400;
    ER_BAD_RELATIONSHIP_RANK	CONSTANT INTEGER := -2500;
    ER_BAD_ATTRIBUTE_RANK	CONSTANT INTEGER := -2600;
    ER_BAD_ATOM_RANK		CONSTANT INTEGER := -2700;
    ER_MISSING_PREFERRED_ID	CONSTANT INTEGER := -2800;
    ER_MISSING_CONCEPT_ID	CONSTANT INTEGER := -2900;
    ER_UNSPECIFIED_ERROR	CONSTANT INTEGER := -3000;
    ER_UNKNOWN_ERROR		CONSTANT INTEGER := -3100;

    -- Atomic Action names
    AA_MOVE			CONSTANT VARCHAR2(2) := 'C';
    AA_INSERT			CONSTANT VARCHAR2(2) := 'I';
    AA_DELETE			CONSTANT VARCHAR2(2) := 'D';
    AA_CHANGE_ATOM_ID		CONSTANT VARCHAR2(2) := 'A';
    AA_CHANGE_STATUS		CONSTANT VARCHAR2(2) := 'S';
    AA_CHANGE_TOBERELEASED	CONSTANT VARCHAR2(2) := 'T';
    AA_CHANGE_FIELD		CONSTANT VARCHAR2(2) := 'CF';
    AA_MACRO_INSERT		CONSTANT VARCHAR2(2) := 'I';

    -- Molecular Action Names
    MA_CHANGE_STATUS		CONSTANT VARCHAR2(30) := 'MOLECULAR_CHANGE_STATUS';
    MA_CHANGE_TOBERELEASED	CONSTANT VARCHAR2(30) := 'MOLECULAR_CHANGE_TOBERELEASED';
    MA_CHANGE_FIELD		CONSTANT VARCHAR2(30) := 'MOLECULAR_CHANGE_FIELD';
    MA_CONCEPT_APPROVAL		CONSTANT VARCHAR2(30) := 'MOLECULAR_CONCEPT_APPROVAL';
    MA_INSERT			CONSTANT VARCHAR2(30) := 'MOLECULAR_INSERT';
    MA_DELETE			CONSTANT VARCHAR2(30) := 'MOLECULAR_DELETE';
    MA_MERGE			CONSTANT VARCHAR2(30) := 'MOLECULAR_MERGE';
    MA_MOVE			CONSTANT VARCHAR2(30) := 'MOLECULAR_MOVE';
    MA_SPLIT			CONSTANT VARCHAR2(30) := 'MOLECULAR_SPLIT';
    MA_UNDO			CONSTANT VARCHAR2(30) := 'MOLECULAR_UNDO';
    MA_REDO			CONSTANT VARCHAR2(30) := 'MOLECULAR_REDO';
    MA_MACRO_INSERT		CONSTANT VARCHAR2(30) := 'MACRO_INSERT';

    -- Attribute/Relationship Levels
    CONCEPT_LEVEL		CONSTANT VARCHAR2(2) := 'C';
    SOURCE_LEVEL		CONSTANT VARCHAR2(2) := 'S';
    PROCESSED_LEVEL		CONSTANT VARCHAR2(2) := 'P';

    -- Core Table Constants
    LONG_ATTRIBUTE		CONSTANT VARCHAR2(20) := '<>Long_Attribute<>:';
    TN_CONCEPT_STATUS		CONSTANT VARCHAR2(2) := 'CS';
    TN_CLASSES			CONSTANT VARCHAR2(2) := 'C';
    TN_FOREIGN_CLASSES		CONSTANT VARCHAR2(2) := 'FC';
    TN_ATTRIBUTES		CONSTANT VARCHAR2(2) := 'A';
    TN_RELATIONSHIPS		CONSTANT VARCHAR2(2) := 'R';
    TN_CONTEXT_RELATIONSHIPS	CONSTANT VARCHAR2(2) := 'CR';
    TN_SOURCE_CXT_RELATIONSHIPS CONSTANT VARCHAR2(3) := 'SCR';
    TN_SOURCE_CONCEPT_STATUS	CONSTANT VARCHAR2(3) := 'SCS';
    TN_SOURCE_CLASSES		CONSTANT VARCHAR2(2) := 'SC';
    TN_SOURCE_ATTRIBUTES	CONSTANT VARCHAR2(2) := 'SA';
    TN_SOURCE_RELATIONSHIPS	CONSTANT VARCHAR2(2) := 'SR';

    -- Core table long name constants
    LTN_ATOMIC_ACTIONS		CONSTANT VARCHAR2(30) := 'ATOMIC_ACTIONS';
    LTN_ATOMS			CONSTANT VARCHAR2(30) := 'ATOMS';
    LTN_ATTRIBUTES		CONSTANT VARCHAR2(30) := 'ATTRIBUTES';
    LTN_CLASSES 		CONSTANT VARCHAR2(30) := 'CLASSES';
    LTN_CONCEPT_STATUS		CONSTANT VARCHAR2(30) := 'CONCEPT_STATUS';
    LTN_MOLECULAR_ACTIONS	CONSTANT VARCHAR2(30) := 'MOLECULAR_ACTIONS';
    LTN_RELATIONSHIPS		CONSTANT VARCHAR2(30) := 'RELATIONSHIPS';
    LTN_TRANSACTIONS		CONSTANT VARCHAR2(30) := 'TRANSACTIONS';

    -- Field name constants
    FN_DEAD			CONSTANT VARCHAR2(20) := 'DEAD';
    FN_CONCEPT_ID		CONSTANT VARCHAR2(20) := 'CONCEPT_ID';
    FN_ATOM_ID			CONSTANT VARCHAR2(20) := 'ATOM_ID';
    FN_RELATIONSHIP_ID		CONSTANT VARCHAR2(20) := 'RELATIONSHIP_ID';
    FN_ATTRIBUTE_ID		CONSTANT VARCHAR2(20) := 'ATTRIBUTE_ID';
    FN_STATUS			CONSTANT VARCHAR2(20) := 'STATUS';
    FN_TBR			CONSTANT VARCHAR2(20) := 'TOBERELEASED';

     -- Important Authority/Source prefixes
    EDITOR_PREFIX		CONSTANT VARCHAR2(3) := 'E-%';
    STAMPING_PREFIX		CONSTANT VARCHAR2(3) := 'S-%';
    LEXICAL_PREFIX		CONSTANT VARCHAR2(3) := 'L-%';
    ENG_PREFIX			CONSTANT VARCHAR2(5) := 'ENG-%';
    PIR_PREFIX			CONSTANT VARCHAR2(5) := 'PIR-%';

    -- Constants for Calcualting Ranks
    MAX_RANK			CONSTANT INTEGER := 9999;
    MTH_RANK			CONSTANT INTEGER := 9999;
    ID_LENGTH			CONSTANT INTEGER := 10;
    EMPTY_RANK			CONSTANT VARCHAR2(11) := '00000000000';

    -- Constants for SOURCE_PROCESSING
    HIGHER_INTO_LOWER		CONSTANT INTEGER := 1;
    LOWER_INTO_HIGHER		CONSTANT INTEGER := 0;

    -- Constants for Integry System
    NO_PROCEDURE		CONSTANT VARCHAR2(30) := 'MEME_INTEGRITY.NOTHING';

    FUNCTION release
    RETURN INTEGER;

    FUNCTION version_info
    RETURN VARCHAR2;

    PRAGMA restrict_references (version_info,WNDS,RNDS,WNPS);

    FUNCTION version
    RETURN FLOAT;

    PROCEDURE version;

    PROCEDURE help;

    PROCEDURE help (
	method_name IN VARCHAR2
    );

    PROCEDURE register_package;

END meme_constants;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY MEME_CONSTANTS AS

/* FUNCTION release ************************************************************
 */
FUNCTION release
RETURN INTEGER
IS
BEGIN

    version;
    return to_number(release_number);
END release;

/* FUNCTION version ************************************************************
 */
FUNCTION version
RETURN FLOAT
IS
BEGIN

    version;
    return to_number(version_number);
END version;

/* FUNCTION version_info *******************************************************
 */
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
    return package_name || ' Release ' || release_number || ': ' ||
	   'version ' || version_number || ' (' || version_date || ')';
END version_info;

/* PROCEDURE version ***********************************************************
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

/* PROCEDURE help **************************************************************
 */
PROCEDURE help
IS
BEGIN
	help(NO_TOPIC);
END help;

/* PROCEDURE help **************************************************************
 */
PROCEDURE help (
	method_name IN VARCHAR2
)
IS
BEGIN
    -- Print version
    DBMS_OUTPUT.PUT_LINE('.');
    version;
END help;

/* PROCEDURE register_package **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_CONSTANTS.release_number,
      MEME_CONSTANTS.version_number,
      SYSDATE,
      MEME_CONSTANTS.version_authority,
      MEME_CONSTANTS.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

END; -- package body
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_CONSTANTS.help;
execute MEME_CONSTANTS.register_package;

