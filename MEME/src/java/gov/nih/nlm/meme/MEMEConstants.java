/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme
 * Object:  MEMEConstants
 *
 *****************************************************************************/
package gov.nih.nlm.meme;


/**
 * This interface holds constants commonly used by MEME applications. Many of
 * them represent names used in a properties file (like {@link #DEBUG}).
 * 
 * @author MEME Group
 */
public interface MEMEConstants {

	//
	// Used when running an external process
	//

	/**
	 * Indicates that STDOUT should be read when calling
	 * {@link MEMEToolkit#exec}.
	 */
	public final static int USE_INPUT_STREAM = 1;

	/**
	 * Indicates that STDERR should be read when calling
	 * {@link MEMEToolkit#exec}.
	 */
	public final static int USE_ERROR_STREAM = 2;

	/**
	 * Indicates that the process output should be ignored when calling
	 * {@link MEMEToolkit#exec}
	 */
	public final static int USE_NO_STREAM = -1;

	//
	// Mail configuration Properties
	//

	/**
	 * The name of a property whose value should be the email address of the
	 * administrator responsible for the midsvcs socket server.
	 */
	public final static String MIDSVCS_ADMIN = "meme.admin.midsvcs";

	/**
	 * The name of a property whose value should be the email address of the
	 * database administrator.
	 */
	public final static String DATABASE_ADMIN = "meme.admin.database";

	/**
	 * The name of a property whose value should be the email address of the
	 * system administrator.
	 */
	public final static String SYSTEM_ADMIN = "meme.admin.system";

	/**
	 * The name of a property whose value should be the email address of the
	 * administrator responsible for the meme application server.
	 */
	public final static String MEME_ADMIN = "meme.admin.meme";

	/**
	 * The property used by {@link MEMEMail} that specifies a mail host.
	 */
	public final static String MAIL_HOST = "meme.smtp.host";

	//
	// LVG configuration Properties
	//

	/**
	 * The name of a property whose value should be lvg configuration file.
	 */
	public final static String LVG_CONFIG_FILE = "meme.lvg.config.file";

	//
	// ThreadPool Configuration Properties
	//

	/**
	 * Thread pool optimal size.
	 */
	public final static String TP_OPTIMAL_SIZE = "meme.threadpool.size.optimal";

	/**
	 * Thread pool maximum size.
	 */
	public final static String TP_MAX_SIZE = "meme.threadpool.size.max";

	/**
	 * Thread pool auto extend.
	 */
	public final static String TP_AUTOEXTEND = "meme.threadpool.autoextend";

	//
	// Common MEME Application Properties
	//

	/**
	 * Name of the property indicating whether or not user interaction will take
	 * place in GUI windows or on the command line.
	 * 
	 * @see MEMEToolkit#usingView()
	 */
	public final static String VIEW = "meme.view";

	/**
	 * Name of the property indicating whether or not "debugging" mode is
	 * enabled.
	 * 
	 * @see MEMEToolkit#trace(String)
	 */
	public final static String DEBUG = "meme.debug";

	/**
	 * Name of a property which specifies the path to the log file. The path is
	 * specified relative to $MEME_HOME
	 */
	public final static String LOG_FILE = "meme.log.path";

	/**
	 * The name of the property which specifies the directory containing DTDs
	 * for the XML docs. It is specified relative to $MEME_HOME.
	 */
	public final static String DTD_DIRECTORY = "meme.dtd.directory";

	/**
	 * The name of the property which specifies the properties file name.
	 */
	public final static String PROPERTIES_FILE = "meme.properties.file";

	/**
	 * The name of a property which specifies the tmp directory.
	 */
	public final static String TMP_DIRECTORY = "meme.tmp.directory";

	//
	// Action Validation Switch Properties
	//

	/**
	 * The name of a property which specifies if atomic actions needs to be
	 * validated.
	 */
	public final static String VALIDATE_ATOMIC_ACTIONS = "meme.validate.actions.atomic";

	/**
	 * The name of a property which specifies if molecular actions needs to be
	 * validated.
	 */
	public final static String VALIDATE_MOLECULAR_ACTIONS = "meme.validate.actions.molecular";

	//
	// NLM Server properties
	//

	/**
	 * The name of the property specifying the host that {@link MIDServices}
	 * connects to for mid services.
	 */
	public final static String MIDSVCS_HOST = "env.MIDSVCS_HOST";

	/**
	 * The name of the property specifying the port that {@link MIDServices}
	 * connects to for mid services.
	 */
	public final static String MIDSVCS_PORT = "env.MIDSVCS_PORT";

	//
	// Environment variable properties
	//

	/**
	 * Property representing the $LVG_DIR directory.
	 */
	public final static String LVG_DIR = "env.LVG_DIR";

	/**
	 * Property representing the $MEME_HOME directory.
	 */
	public final static String MEME_HOME = "env.MEME_HOME";

	/**
	 * Property representing the $ORACLE_HOME directory.
	 */
	public final static String ORACLE_HOME = "env.ORACLE_HOME";

	/**
	 * Property representing the $ENV_HOME directory.
	 */
	public final static String ENV_HOME = "env.ENV_HOME";

	/**
	 * Property representing the $ENV_FILE directory.
	 */
	public final static String ENV_FILE = "env.ENV_FILE";

	//
	// Aggregations of Properties
	//

	/**
	 * When the {@link MEMEToolkit} is initialized, these properties will be
	 * read in from the properties file.
	 */
	public final static String[] ALLOWABLE_PROPERTIES = { VIEW, DEBUG,
			LOG_FILE, PROPERTIES_FILE, TMP_DIRECTORY, VALIDATE_ATOMIC_ACTIONS,
			VALIDATE_MOLECULAR_ACTIONS, DTD_DIRECTORY, LVG_DIR, MEME_HOME,
			ORACLE_HOME, MIDSVCS_ADMIN, MEME_ADMIN, SYSTEM_ADMIN,
			DATABASE_ADMIN, MAIL_HOST, LVG_CONFIG_FILE, TP_OPTIMAL_SIZE,
			TP_MAX_SIZE, TP_AUTOEXTEND, MIDSVCS_HOST, MIDSVCS_PORT };

	/**
	 * List of properties which have to be set.
	 */
	public final static String[] REQUIRED_PROPERTIES = {};

	//
	// Flags
	//

	/**
	 * Flag indicating a fatal error. Typically this is used in a call to
	 * {@link MEMEToolkit#handleError(Exception)}.
	 */
	public final static boolean FATAL = true;

}
