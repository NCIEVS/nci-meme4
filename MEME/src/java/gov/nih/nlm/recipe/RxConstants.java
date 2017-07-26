/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     RxConstants.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:    Constants used by Recipe components.
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.awt.Color;
import java.awt.Event;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;


/**
 * This object holds all of the constants commonly used by the meme.orwel.* package. 
 * @author Brian Carlsen, Owen J. Carlsen, Yun-Jung Kim
 * @version 1.5
 */
public class RxConstants {
  
  //
  // RxToolkit constants
  //
  public final static String MERGE_SET = "MERGE_SET";
  public final static String SOURCE = "SOURCE";
  public final static String TERMGROUP = "TERMGROUP";
  
  //
  // Recipe editing "Functions"
  //
  public final static String FN_NEXT_SECTION = "Next Section";
  public final static String FN_PREVIOUS_SECTION = "Previous Section";
  public final static String FN_NEXT_STEP = "Next Step";
  public final static String FN_PREVIOUS_STEP = "Previous Step";
  public final static String FN_INSERT_SECTION = "Insert Section";
  public final static String FN_INSERT_STEP = "Insert Step";
  public final static String FN_DELETE_SECTION = "Delete Section";
  public final static String FN_DELETE_STEP = "Delete Step";
  public final static String FN_SET_STEP = "Set Step";
  public final static String FN_SKIP_STEP = "Skip Step";
  public final static String FN_OPEN_RECIPE = "Open";
  public final static String FN_SAVE_RECIPE = "Save";
  public final static String FN_SAVE_RECIPE_AS = "Save As";
  public final static String FN_VIEW_RECIPE_HTML = "View HTML";
  public final static String FN_WRITE_SHELL_SCRIPT = "Write Shell Script";
  public final static String FN_NEW_RECIPE = "New";
  public final static String FN_EXIT = "Exit";
  public final static String FN_SAVE_PROPERTIES = "Save Options";
  public final static String FN_HELP_WRITER = "Recipe Writer";
  public final static String FN_HELP_STEP = "Current Step";
  public final static String FN_FILE_MENU = "File";
  public final static String FN_OPTIONS_MENU = "Options";
  public final static String FN_ACTIONS_MENU = "Actions";
  public final static String FN_HELP_MENU = "Help";
  public final static String FN_CHANGE_DB = "Set Database Connection";

  //
  // Recipe running "Functions"
  //
  public final static String FN_RUN_RX = "Go!";
  public final static String FN_PAUSE_RX = "Pause recipe";
  public final static String FN_RESUME_RX = "Resume recipe";
  public final static String FN_STOP_RX = "Force recipe to stop";
  public final static String FN_STOP_RX_AT_NEXT_STEP = "Stop at next step";
  public final static String FN_CHANGE_SI = "Change step instruction";
  public final static String FN_CHANGE_EM = "Change execution mode";
  // Also editing "functions"
  //public final static String FN_FILE_MENU = "File";
  //public final static String FN_OPTIONS_MENU = "Options";
  //public final static String FN_ACTIONS_MENU = "Actions";
  //public final static String FN_OPEN_RECIPE = "Open";
  //public final static String FN_SAVE_RECIPE = "Save";
  //public final static String FN_SAVE_RECIPE_AS = "Save As";
  //public final static String FN_VIEW_RECIPE_HTML = "View HTML";
  //public final static String FN_WRITE_SHELL_SCRIPT = "Write Shell Script";
  //public final static String FN_EXIT = "Exit";

  // Recipe editing "functions" mnemonic map
  // This is to standardize across applications
  public final static HashMap MENU_FN_MNEMONIC_MAP = new HashMap();
  static {
    MENU_FN_MNEMONIC_MAP.put(FN_FILE_MENU,new Character('F'));
    MENU_FN_MNEMONIC_MAP.put(FN_OPTIONS_MENU,new Character('O'));
    MENU_FN_MNEMONIC_MAP.put(FN_ACTIONS_MENU,new Character('A'));
    MENU_FN_MNEMONIC_MAP.put(FN_HELP_MENU,new Character('H'));
    MENU_FN_MNEMONIC_MAP.put(FN_OPEN_RECIPE,new Character('O'));
    MENU_FN_MNEMONIC_MAP.put(FN_SAVE_RECIPE,new Character('S'));
    MENU_FN_MNEMONIC_MAP.put(FN_SAVE_RECIPE_AS,new Character('A'));
    MENU_FN_MNEMONIC_MAP.put(FN_VIEW_RECIPE_HTML,new Character('V'));
    MENU_FN_MNEMONIC_MAP.put(FN_WRITE_SHELL_SCRIPT,new Character('T'));
    MENU_FN_MNEMONIC_MAP.put(FN_NEW_RECIPE,new Character('N'));
    MENU_FN_MNEMONIC_MAP.put(FN_EXIT,new Character('x'));
    MENU_FN_MNEMONIC_MAP.put(FN_SAVE_PROPERTIES,new Character('p'));
    MENU_FN_MNEMONIC_MAP.put(FN_CHANGE_DB,new Character('C'));
    MENU_FN_MNEMONIC_MAP.put(FN_RUN_RX,new Character('G'));
    MENU_FN_MNEMONIC_MAP.put(FN_PAUSE_RX,new Character('P'));
    MENU_FN_MNEMONIC_MAP.put(FN_RESUME_RX,new Character('R'));
    MENU_FN_MNEMONIC_MAP.put(FN_STOP_RX,new Character('F'));
    MENU_FN_MNEMONIC_MAP.put(FN_CHANGE_SI,new Character('S'));
    MENU_FN_MNEMONIC_MAP.put(FN_CHANGE_EM,new Character('E'));
  }

  //
  // Recipe editing menu functions accelerators map
  //
  public final static HashMap MENU_FN_ACCELERATOR_MAP = new HashMap();
  static {
    MENU_FN_ACCELERATOR_MAP.put(
     FN_OPEN_RECIPE,KeyStroke.getKeyStroke('O',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_SAVE_RECIPE,KeyStroke.getKeyStroke('S',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_VIEW_RECIPE_HTML,KeyStroke.getKeyStroke('V',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_WRITE_SHELL_SCRIPT,KeyStroke.getKeyStroke('T',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_NEW_RECIPE,KeyStroke.getKeyStroke('N',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_EXIT,KeyStroke.getKeyStroke('Q',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_HELP_STEP,KeyStroke.getKeyStroke('H',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_RUN_RX,KeyStroke.getKeyStroke('G',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_PAUSE_RX,KeyStroke.getKeyStroke('Z',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_RESUME_RX,KeyStroke.getKeyStroke('R',Event.CTRL_MASK,false));
    MENU_FN_ACCELERATOR_MAP.put(
     FN_STOP_RX,KeyStroke.getKeyStroke('C',Event.CTRL_MASK,false));
  }

  //
  // RxStep status options
  //
  public final static int UNTOUCHED = 0;
  public final static int SAVED = 1;
  public final static int SKIPPED =2;
  public final static int EXECUTED =3;
  public final static int EXECUTED_FAILED =4;
  private static String [] step_status_map =  
       {"Untouched","Saved","Skipped","Successful Execution",
	"Failed Execution"};
  private static java.awt.Color [] step_status_color_map = new java.awt.Color[5];
  static {
    step_status_color_map[0] = java.awt.Color.red;
    step_status_color_map[1] = java.awt.Color.blue;
    step_status_color_map[2] = java.awt.Color.yellow;
    step_status_color_map[3] = java.awt.Color.black;
    step_status_color_map[4] = java.awt.Color.black;
  };    

  public static String stepStatusToString( int ss ) {
    return step_status_map[ss]; 
  };
  public static java.awt.Color stepStatusToColor( int ss ) {
    return step_status_color_map[ss]; 
  };



  // Load Step Insert options
  public final static int CAT = 0;
  public final static int SAT = 1;
  private static String [] insertOptionMap = {"CAT","SAT"};

  public static String insertOptionToString( int io ) {
	return insertOptionMap[io];
  };
  public static boolean validInsertOption(int io) {
	try {
		insertOptionMap[io] = insertOptionMap[io];
	} catch (ArrayIndexOutOfBoundsException e) {
	  return false;
	}
	return true;
  }

  // LoadStep mapping options
  public final static int SOURCE_ID = 0;
  public final static int MEME_ID = 1;
  public final static int CUI = 2;
  public final static int NONE = 4;

  // Maximum string length in stringtab
  public final static int MAX_STRING_LENGTH = 1786;

  // length of source/authority/termgroup fields

  public final static int SOURCE_FIELD_LENGTH = 20;

  // RunnableRx execution mode options
  public final static int EM_RUN = 0;
  public final static int EM_UNDO = 1;
  private static String [] executionModeOptions = {"RUN","UNDO"};

  public static String executionModeToString(int em) {
	return executionModeOptions[em];
  };

  public static boolean validExecutionMode(int em ) {
	try {
		executionModeOptions[em] = executionModeOptions[em];
	} catch (ArrayIndexOutOfBoundsException e) {
	  return false;
	}
	return true;
  }

  // RunnableStep step instruction options
  public final static int SI_RUN = 0;
  public final static int SI_SKIP = 1;
  public final static int SI_UNDO = 2;
  public final static int SI_REDO = 3;
  public final static int SI_STOP = 4;
  public final static int SI_NONE = 5;
  private static String [] stepInstructionOptions = 
  {"RUN","SKIP","UNDO","REDO","STOP"," "};

  public static String stepInstructionToString(int si) {
	return stepInstructionOptions[si];
  };

  public static boolean validStepInstruction(int si ) {
	// Check that its a valid value
	try {
		stepInstructionOptions[si] = stepInstructionOptions[si];
	} catch (ArrayIndexOutOfBoundsException e) {
	  return false;
	}
	return true;
  }


  // RunnableStep mask : Must be powers of 2
  public final static int SIM_NONE = 1;
  public final static int SIM_IGNORE_ERRORS = 2;
  public final static int SIM_RERUN_SUCCESSFUL = 4;
  public final static int SIM_UNDO_FAILED = 8;
  public final static String SIM_INVALID = "Invalid Mask";
  public static String stepInstructionMaskToString(int sim) {
	switch (sim) {
	case SIM_NONE: return "";
	case SIM_IGNORE_ERRORS: return "Ignore errors";
	case SIM_RERUN_SUCCESSFUL: return "Run successful sub-steps again";
	case SIM_UNDO_FAILED: return "Undo failed sub-steps";
	}
	return SIM_INVALID;
  };

  public static boolean validStepInstructionMask(int sim) {
	if (stepInstructionMaskToString(sim).equals(SIM_INVALID)) {
	  return false;
	};
	return true;
  };


  // Table of valid step instructions in execution_mode context
  // Y axis is SI_RUN, SI_SKIP, SI_UNDO, SI_REDO, SI_STOP, SI_NONE
  // X axis is EM_RUN, EM_UNDO
  public static boolean [] [] stepInstructionsInContext =
  {
	{true, false},
	{true, true},
	{false, true},
	{true, false},
	{true, true},
	{true, true}
  };

  public static boolean validStepInstructionInContext( int si, int em ) {
    // Check that the step instruction makes sense with respect to the execution_mode
    RxToolkit.trace("RxConstants::validStepInstructionInContext("+
			  si + "," + em + ").");
    RxToolkit.trace("stepInstructionsInContext: " + stepInstructionsInContext);
    return stepInstructionsInContext[si][em];
  };


  // RunnableStep result status optinos
  public final static int RS_SUCCESS = 0;
  public final static int RS_FAILURE = 1;
  public final static int RS_SKIPPED = 2;
  public final static int RS_NOT_RUN_YET = 3;
  public final static int RS_CURRENTLY_RUNNING = 4;
  public final static int RS_UNDONE = 5;
  public final static int RS_REDONE = 6;
  public final static int RS_PARTIALLY_COMPLETED = 7;
  public final static int RS_PAUSED = 8;
  public final static int RS_STOPPED = 9;
  private static String [] resultStatusOptions =
  {"Completed Successfully","Failed to Complete",
   "Skipped","Untouched","Currently Running",
   "Undone","Redone","Partially Completed","Paused",
   "Execution Stopped"};

  public static String resultStatusToString(int rs) {
	return resultStatusOptions[rs];
  };

  public static boolean validResultStatus(int rs ) {
	try {
		resultStatusOptions[rs] = resultStatusOptions[rs];
	} catch (ArrayIndexOutOfBoundsException e) {
	  return false;
	}
	return true;
  }

  //
  // This section deals with property names
  // Valid values for those properties
  // Required properties and properties allowed on the command line
  //

  // Parameter file property names
  public final static String RECIPE_FILE = "RECIPE_FILE";
  public final static String SRC_DIRECTORY = "SRC_DIRECTORY";

  // Required writer properties:
  public final static String [] WRITER_REQUIRED_PROPERTIES = 
  {SRC_DIRECTORY};
  
  // Allowable writer properties
  public final static String [] WRITER_ALLOWABLE_PROPERTIES =
  { RECIPE_FILE, SRC_DIRECTORY };

  // Required runner properties:
  public final static String [] RUNNER_REQUIRED_PROPERTIES = 
  {SRC_DIRECTORY, RxConstants.NLS, RxConstants.II_SYSTEM};
  
  // Allowable runner properties
  public final static String [] RUNNER_ALLOWABLE_PROPERTIES =
  { RECIPE_FILE, SRC_DIRECTORY };

  // Default property file
  public final static String DEFAULT_PROPERTY_FILE = "recipe.ini";

  // Source file names
  public final static String CLASSES_SRC = "classes_atoms.src";
  public final static String MERGEFACTS_SRC = "mergefacts.src";
  public final static String ATTRIBUTES_SRC = "attributes.src";
  public final static String RELATIONSHIPS_SRC = "relationships.src";
  public final static String TERMGROUPS_SRC = "termgroups.src";
  public final static int TERMGROUPS_SRC_TOKEN_COUNT = 5;
  public final static String SOURCES_SRC = "sources.src";
  public final static int SOURCES_SRC_TOKEN_COUNT = 2;
  public final static String CONTEXTS_RAW = ".raw3";
  public final static String CONTEXTS_SRC = "contexts.src";
  public final static String STRINGS_SRC = "strings.src";
  public final static String STRINGTAB_SRC = "stringtab.src";


  // Source tables
  public final static String [] SOURCE_TABLES =
  {"source_classes_atoms","source_concept_status","source_relationships",
   "source_attributes", "source_stringtab", "source_string_ui",
   "source_source_rank","source_termgroup_rank"};

  // Debugging property name/values, used by Toolkit 
  public final static String DEBUG_OFF = "false";
  public final static String DEBUG_ON = "true";

  // View property:  Whether a view is being used
  public final static String VIEW_OFF = "false";
  public final static String VIEW_ON = "true";  

  // Constant representing work types
  public final static String INSERTION_WORK = "INSERTION";
  public final static String MAINTENANCE_WORK = "MAINTENANCE";

  // Number of queries to add to a batch before committing
  public final static int BATCH_SIZE = 10000;

  //
  // These are package names used by dynamic class loaders
  //
  public final static String RECIPE_PACKAGE_NAME = "gov.nih.nlm.recipe";
  public final static String RX_STEP_PACKAGE_NAME = "gov.nih.nlm.recipe.steps";
  public final static String RX_SECTION_PACKAGE_NAME = "gov.nih.nlm.recipe.sections";

  //
  // Mid services host and port
  // (maybe these should be properties)
  //
  public final static String MIDSVCS_HOST = "midns.nlm.nih.gov";
  public final static int MIDSVCS_PORT = 5125;

  //
  // The following sections have constants corresponding with steps
  //

  public final static String ENG_PREFIX = "ENG-";

  // Merge steps (Precomputed and Generated)
  public final static String PI_NONE = "None";
  public final static String PI_ONE_TO_ONE = "One to One";
  public final static String PI_N_TO_N = "N to N";

  // GeneratedRelationshipMatchStep
  public final static String RT_DEMOTION = "Demotions";
  public final static String RT_PIR = "PIR Relationships";
  public final static String [] RT_ARRAY =
  {RT_DEMOTION, RT_PIR};
 
  ////
  // 
  // Border sytles
  //
  public final static Border HAS_POPUP_BORDER = 
    new BevelBorder(BevelBorder.LOWERED, new Color(0,135,0), new Color(0,95,0));
  public final static Border IS_REQUIRED_BORDER = 
    new BevelBorder(BevelBorder.LOWERED, new Color(135,0,0), new Color(95,0,0));
  public final static Border EMPTY_BORDER = 
    BorderFactory.createEmptyBorder(15,15,15,15);
  public final static Border EMPTY_BORDER_NO_TOP = 
    BorderFactory.createEmptyBorder(0,15,15,15);

  //
  // Field styles
  //
  public final static Insets GRID_INSETS =
    new Insets(2,2,2,2);
  public final static Insets EMPTY_INSETS =
    new Insets(0,0,0,0);

  // 
  // Used when running an external process
  //
  public final static int USE_INPUT_STREAM = 1;
  public final static int USE_ERROR_STREAM = 2;

  //
  // This section deals with property names
  // Valid values for those properties
  // Required properties and properties allowed on the command line
  //

  //
  // MEME Application properties
  //
  // public final static String DB_HOST = "DB_HOST";
  // public final static String DB_PORT = "DB_PORT";
  // public final static String DB_NAME = "DB_NAME";
  public final static String DB_USER = "DB_USER";
  public final static String DB_PASSWORD = "DB_PASSWORD";
  public final static String DB_DRIVER_CLASS = "DB_DRIVER_CLASS";
  public final static String DB_SERVICE = "DB_SERVICE";
  // oracle tnsnames connect string
  public final static String DEBUG_PROPERTY = "DEBUG";
  public final static String LOG_FILE = "LOG_FILE";
  public final static String VIEW_PROPERTY = "VIEW";
  public final static String PROPERTY_FILE = "PROPERTY_FILE";
  public final static String PASSWORD_FILE = "PASSWORD_FILE";
  public final static String MEME_HOME = "MEME_HOME";
  public final static String ORACLE_HOME = "ORACLE_HOME";
  public final static String BROWSER_PROPERTY = "BROWSER";
  public final static String SAVE_DIRECTORY = "SAVE_DIRECTORY";
  public final static String TMP_DIRECTORY = "TMP_DIRECTORY";
  public final static String NLS = "NLS";
  public final static String II_SYSTEM = "II_SYSTEM";
  public final static String SMTP_HOST = "SMTP_HOST";

  //
  // Required properties:
  //
  public final static String [] REQUIRED_PROPERTIES = 
  {MEME_HOME,DB_DRIVER_CLASS};
  
  //
  // These properties are the ones the MEMEToolkit will
  // pay attention to, others will not be recorded
  //
  public final static String [] ALLOWABLE_PROPERTIES =
  {DEBUG_PROPERTY, DB_USER, DB_PASSWORD, PASSWORD_FILE,
   DB_DRIVER_CLASS, DB_SERVICE, LOG_FILE, VIEW_PROPERTY,
   PROPERTY_FILE, BROWSER_PROPERTY, MEME_HOME, ORACLE_HOME, SAVE_DIRECTORY,
   TMP_DIRECTORY, II_SYSTEM, NLS};

  // Default files
  public final static String DEFAULT_TMP_DIRECTORY = "/tmp";
  public final static String DEFAULT_PASSWORD_FILE = "/etc/umls/oracle.passwd";

  //
  // Useful vars
  //
  public final static String Y = "Y";
  public final static String N = "N";

  public static final MouseListener NULL_MOUSE_LISTENER = new MouseAdapter() {};
 

}
