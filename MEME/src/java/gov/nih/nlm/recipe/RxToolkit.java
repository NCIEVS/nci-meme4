/*****************************************************************************
 *
 * Package:    com.lexical.meme.core
 * Object:     MEMEToolkit.java
 * 
 * Author:     Brian Carlsen
 * CHANGES
 *  02/08/2007 BAC (1-DG6SD): Ensure getFilterData for SOURCE and TERMGROUP
 *   modes return data from files as well as DB.
 *  11/15/2006 BAC (1-CTN9X): Ensure picklists in recipe writer are sorted by
 *   sorting all data retrieval queries.
 *  
 *****************************************************************************/
package gov.nih.nlm.recipe;

import gov.nih.nlm.swing.ConnectionDialog;
import gov.nih.nlm.swing.HtmlViewer;
import gov.nih.nlm.swing.ListDialog;
import gov.nih.nlm.swing.MultiLineInputDialog;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarFile;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

/**
 * This class contains useful static methods.
 * 
 * @author Brian A. Carlsen
 * @version 1.0
 * 
 */
public class RxToolkit extends RxConstants {

	//
	// Constants
	//
	private static String PROPERTIES = "PROPERTIES";

	private static String DB_CONNECTION = "DB_CONNECTION";

	private static String LOG = "LOG";

	private static String PM = "PERSISTENCE_MANAGER";

	//
	// Fields
	//
	private static HashMap objects = new HashMap();

	private static HashMap timetable = new HashMap();

	private static HashMap object_file_map = new HashMap();

	//
	// Static initializers
	// 

	/**
	 * This is causing problems because it is calling exit! static {
	 * Runtime.getRuntime().addShutdownHook( new Thread () { public void run () {
	 * Exit(1); } } ); }
	 */

	/**
	 * This default initialize method creates the java.sql.Connection and it loads
	 * the default property file, and it sets the printwriter to System.out unless
	 * there is a logfile.
	 */
	public static void initialize(String[] app_specific_properties,
			String[] app_specific_req_prop) {
		trace("MEMEToolkit::initialize()");

		initializeProperties(System.getProperty(RxConstants.PROPERTY_FILE),
				app_specific_properties, app_specific_req_prop);

		logComment("Initializing Toolkit ... please wait", true);

		// trace the status of using a view.
		trace("\tUsing View? " + usingView());

		initializeLog(getProperty(RxConstants.LOG_FILE));

		initializeSQLConnection();

		// Cache data
		/**
		 * Thread cache_thread = new Thread () { public void run () { logComment
		 * ("Cacheing data.",true); DBToolkit.cacheData(); logComment ("Done
		 * cacheing data.",true); } }; cache_thread.start();
		 */
		logComment("Cacheing data.", true);
		// DBToolkit.cacheData();
		logComment("Done cacheing data.", true);

		logComment("Finished initializing toolkit.", true);

	}

	/**
	 * This method initializes the properties from a property file
	 * @param paramfile String
	 */
	public static void initializeProperties(String paramfile,
			String[] app_specific_properties, String[] app_specific_req_props) {
		trace("MEMEToolkit::initializeProperties(" + paramfile + ")");

		// Load from the property file
		Properties props = new Properties();

		if (paramfile != null) {
			try {
				props.load(new FileInputStream(paramfile));
			} catch (FileNotFoundException e) {
				// If file not found, continue only with system properties
				logComment("Property file not found: '" + paramfile + "'.\n"
						+ "Continuing with only system properties.", false);

			} catch (Exception e) {
				reportError("Exception while loading property file : " + e
						+ ". Exiting . . .");
				Exit(1);
			}
		} else {
			// Not a problem if there is no default property file
			logComment("No property file specified.\n"
					+ "Continuing with only system properties.");
		}

		// Override property file with system properties
		// Only allow those properties in RxConstants.ALLOWABLE_PROPERTIES
		String key, value;
		for (int i = 0; i < RxConstants.ALLOWABLE_PROPERTIES.length; i++) {
			key = RxConstants.ALLOWABLE_PROPERTIES[i];
			value = System.getProperties().getProperty(key);
			if (value != null)
				props.put(key, value);
			else if (props.getProperty(key) == null)
				props.put(key, "");
		}

		// add application specific properties
		for (int i = 0; i < app_specific_properties.length; i++) {
			key = app_specific_properties[i];
			value = System.getProperties().getProperty(key);
			if (value != null)
				props.put(key, value);
			else if (props.getProperty(key) == null)
				props.put(key, "");
		}

		// set current properties in sort order
		Properties properties = new Properties();
		TreeSet sortedkeys = new TreeSet(props.keySet());
		Iterator iprop = sortedkeys.iterator();
		while (iprop.hasNext()) {
			key = (String) iprop.next();
			properties.put(key, props.get(key));
		}
		;

		// Check required properties
		String req;
		for (int i = 0; i < RxConstants.REQUIRED_PROPERTIES.length; i++) {
			req = props.getProperty(RxConstants.REQUIRED_PROPERTIES[i]);
			if (req == null || req.equals("")) {
				reportError("Required property '" + RxConstants.REQUIRED_PROPERTIES[i]
						+ "' is missing.", true);
			} else {
				trace("\tRequired property " + RxConstants.REQUIRED_PROPERTIES[i]
						+ " = " + props.getProperty(RxConstants.REQUIRED_PROPERTIES[i]));
			}
		}

		// Check required properties
		for (int i = 0; i < app_specific_req_props.length; i++) {
			req = props.getProperty(app_specific_req_props[i]);
			if (req == null || req.equals("")) {
				reportError("Application specific required property '"
						+ app_specific_req_props[i] + "' is missing.", true);
			} else {
				trace("\tRequired property " + app_specific_req_props[i] + " = "
						+ props.getProperty(app_specific_req_props[i]));
			}
		}

		objects.put(PROPERTIES, properties);
	}

	/**
	 * This method initializes the log for a file
	 * @param logfile String
	 */
	public static void initializeLog(String logfile) {

		trace("MEMEToolkit::initializeLog(" + logfile + ")");

		if (getLog() != null) {
			getLog().close();
		}

		PrintWriter tmp_log = null;
		if (logfile != null && !logfile.equals("")) {
			try {
				boolean write_file = true;
				File f = new File(logfile);
				if (f.exists()) {
					write_file = confirmRequest("You are about to overwrite the logfile:\n'"
							+ logfile + "'. \n" + "Do you really want to do this.");
				}
				if (write_file) {
					tmp_log = new PrintWriter(new FileWriter(logfile));
				} else {
					notifyUser("Logfile already exists, logging to standard out.");
					tmp_log = new PrintWriter(System.out);
				}
			} catch (Exception e) {
				// Report error, do not fail
				reportError("An exception occurred while trying to create "
						+ "the logfile: " + e, true);
			}
		} else {
			tmp_log = new PrintWriter(System.out);
		}
		;

		objects.put(LOG, tmp_log);

	}

	/**
	 * This method initializes the java.sql.Connection connection
	 */
	public static void initializeSQLConnection() {

		boolean connection_exists = false;
		java.sql.Connection con = null;
		while (!connection_exists) {
			con = ConnectionDialog.getDBConnection();
			if (con != null) {
				connection_exists = true;
			}
		}
		objects.put(DB_CONNECTION, con);
	}

	//
	// Accessor methods
	//
	/**
	 * Method to set the current log
	 * @param log PrintWriter
	 */
	public static void setPersistenceManager(PersistenceManager pm) {
		objects.put(PM, pm);
	}

	/**
	 * Method to set the current log
	 * @param log PrintWriter
	 */
	public static void setLog(PrintWriter log) {
		objects.put(LOG, log);
	}

	/**
	 * Method to set the properties
	 * @param props Properties
	 */
	public static void setProperties(Properties props) {
		objects.put(PROPERTIES, props);
	}

	/**
	 * Method to set the sql connection
	 * @param session java.sql.Connection
	 */
	public static void setConnection(java.sql.Connection session) {
		objects.put(DB_CONNECTION, session);
	}

	/**
	 * Accessor method to provide access to log
	 * @return PrintWriter
	 */
	public static PrintWriter getLog() {
		return (PrintWriter) objects.get(LOG);
	}

	/**
	 * Accessor method to provide access to properties
	 * @return Properties
	 */
	public static Properties getProperties() {
		return (Properties) objects.get(PROPERTIES);
	}

	/**
	 * Accessor method to provide access to db_connection
	 * @return java.sql.Connection
	 */
	public static java.sql.Connection getSQLConnection() {
		return (java.sql.Connection) objects.get(DB_CONNECTION);
	}

	/**
	 * This method gets a property from the properties file
	 * @return String
	 */
	public static String getProperty(String name) {
		return getProperty(name, null);
	};

	/**
	 * This method gets a property from the properties file
	 * @param name String
	 * @param default String
	 * @return String
	 */
	public static String getProperty(String name, String def) {
		if (objects.get(PROPERTIES) == null)
			return def;

		Properties p = (Properties) objects.get(PROPERTIES);
		if (p.getProperty(name) == null || p.getProperty(name).equals(""))
			return def;

		return ((Properties) objects.get(PROPERTIES)).getProperty(name);
	};

	/**
	 * This method sets a property in properties
	 * @param name String
	 * @param value String
	 */
	public static void setProperty(String name, String value) {
		if (objects.get(PROPERTIES) == null) {
			objects.put(PROPERTIES, new Properties());
		}
		((Properties) objects.get(PROPERTIES)).put(name, value);

		// If logfile is set, close existing logfile open another one
		if (name.equals(RxConstants.LOG_FILE)) {
			initializeLog(value);
		}
		;

	};

	/**
	 * This writes the current properties to the current property file as
	 * indicated by MEMEToolkit.getProperty(RxConstants.PROPERTY_FILE)
	 * @return void
	 */
	public static void storeProperties() {
		String param_filename = getProperty(RxConstants.PROPERTY_FILE);

		// don't save PROPERTY_FILE property
		getProperties().remove(RxConstants.PROPERTY_FILE);

		File param_file;
		if (param_filename == null) {
			param_file = chooseFile("Choose PROPERTY File",
					"Select a file to save properties to:", JFileChooser.FILES_ONLY,
					new File(""));
		} else {
			param_file = new File(param_filename);
		}

		try {
			FileOutputStream fos = new FileOutputStream(param_file);
			((Properties) objects.get(PROPERTIES)).store(fos, "");
			fos.close();
		} catch (IOException e) {
			reportError("Error opening paramater file: " + e);
		}

		// Add PROPERTY_FILE parameter back
		if (param_filename != null)
			setProperty(RxConstants.PROPERTY_FILE, param_filename);
	}

	/**
	 * Boolean method to provide view state
	 * @return boolean
	 */
	public static boolean usingView() {
		return Boolean.valueOf(getProperty(RxConstants.VIEW_PROPERTY))
				.booleanValue();
	};

	/**
	 * Boolean method to provide debug state
	 * @return boolean
	 */
	public static boolean debugging() {
		String debug = getProperty(RxConstants.DEBUG_PROPERTY);
		if (debug == null)
			return false;

		return debug.equals(RxConstants.DEBUG_ON);

	}

	/**
	 * Gets a file for an object
	 * @param obj Object
	 * @return File
	 */
	public static File getFileForObject(Object obj) {
		return (File) object_file_map.get(obj);
	}

	/**
	 * Returns the default directory
	 * @return file
	 */
	public static File getDefaultSaveDirectory() {
		String directory = RxToolkit.getProperty(RxConstants.SAVE_DIRECTORY);
		if (directory == null)
			directory = "";
		trace("MEMEToolkit::getDefaultSaveDirectory - " + directory);
		return new File(directory);
	}

	/**
	 * Uses file chooser to get the save file
	 * @param file File
	 * @param title String
	 * @param button String
	 * @return file
	 */
	public static File getSaveFile(File file, String title, String button) {
		RxToolkit.trace("MEMEToolkit.getSaveFile(" + file + ")");

		// Choose file
		File cur_dir = null;
		File cur_file = null;
		if (file == null) {
			cur_dir = getDefaultSaveDirectory();
		} else if (file.isDirectory())
			cur_dir = file;
		else {
			return file;
		}
		;

		cur_file = RxToolkit.chooseFile(title, button, JFileChooser.FILES_ONLY,
				cur_dir);

		return cur_file;
	}

	//
	// Standard MEMEToolkit methods
	//

	/**
	 * This method either writes an error to stderr or opens a dialog box, depends
	 * upon usingView().
	 * @param error String
	 */
	public static void reportError(String error) {
		reportError(error, false);
	};

	/**
	 * This method reports an error via a JOptionPane If fatal is true, the
	 * application exits.
	 * @param error String
	 * @param fatal boolean
	 */
	public static void reportError(String error, boolean fatal) {
		// if view is open, open a dialog box
		logComment(error, true);
		if (fatal) {
			logComment("Error is fatal.");
			error = error + " Exiting . . .";
		}
		;
		if (usingView()) {
			trace("using view, opening window..");
			JOptionPane.showMessageDialog(null, error, "Error Report",
					JOptionPane.ERROR_MESSAGE);
		} else {
			System.err.println("Error: " + error);
		}
		if (fatal) {
			Exit(0);
		}
	};

	/**
	 * This if usingView() is true, this method opens a dialog box requesting an
	 * answer from the user. If usingView() is false, it returns false.
	 * @param request String
	 * @return boolean
	 */
	public static boolean confirmRequest(String request) {
		logComment("Confirm request: " + request + "?", true);
		if (usingView()) {
			// Open a dialog box, return results
			int response = JOptionPane.showConfirmDialog(null, request,
					"Confirm Request", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				logComment("Response is Yes.");
				return true;
			} else if (response == JOptionPane.NO_OPTION) {
				logComment("Response is No.");
				return false;
			}
		}
		logComment("No response, return false.");
		return false;
	};

	/**
	 * If usingView(), this method opens a dialog box informing the user of some
	 * state of affairs. it returns upon an OK click or close
	 * @param message String
	 */
	public static void notifyUser(String message) {
		logComment("Notify user: " + message, true);
		if (usingView()) {
			// Open a dialog box, return results
			JOptionPane.showMessageDialog(null, message, "Notify User",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * If usingView(), this method opens a dialog box asking the user a question
	 * and soliciting a response upon an OK click or close
	 * @param message String
	 * @return String
	 */
	public static String getUserInput(String message) {
		logComment("getUserInput: " + message, true);
		if (usingView()) {
			// Open a dialog box, return results
			return JOptionPane.showInputDialog(null, message, "Get User Input",
					JOptionPane.QUESTION_MESSAGE);

		}
		return null;
	}

	/**
	 * If usingView(), this method opens a dialog box asking the user a question
	 * and soliciting a response upon an OK click or close
	 * @param message String
	 * @return String
	 */
	public static String getMultiLineUserInput(String message) {
		logComment("getUserInput: " + message, true);
		if (usingView()) {
			return MultiLineInputDialog.showDialog(null, message, "Get User Input",
					"");
		}
		return null;
	}

	/**
	 * This method gives a file dialog. It is called by requestLoad.
	 * @param title String
	 * @param button_text String
	 * @param selection_mode int
	 * @param current_dir File
	 * @return File
	 */
	public static java.io.File chooseFile(String title, String button_text,
			int selection_mode, File current_dir) {
		JFileChooser file_chooser = new JFileChooser();
		file_chooser.setBounds(1, 0, 500, 300);
		file_chooser.setDialogTitle(title);
		file_chooser.setFileSelectionMode(selection_mode);
		file_chooser.setCurrentDirectory(current_dir);
		int result = file_chooser.showDialog(file_chooser, button_text);
		if (result == JFileChooser.APPROVE_OPTION) {
			java.io.File filename = file_chooser.getSelectedFile();
			return filename;
		} else
			return null;
	}

	/**
	 * This method writes the passed-in content to the specified file.
	 * @param content java.lang.String
	 * @param file java.io.File (This parameter gets changed)
	 */
	public static void writeToFile(String content, File file /* byref */)
			throws Exception {

		// Choose file
		File cur_file = getSaveFile(file, "Select Save File", "Save");

		// Pass file back to user
		file = cur_file;

		// if cancel, bail
		if (cur_file == null)
			throw new Exception("Load was cancelled.");

		FileWriter fileOutStream = null;
		PrintWriter dataOutStream;

		try {
			fileOutStream = new FileWriter(cur_file);
		} catch (IOException e) {
			throw new Exception("IO exception opening File " + cur_file);
		}
		dataOutStream = new PrintWriter(fileOutStream);

		try {
			dataOutStream.write(content);
		} catch (Exception e) {
			throw new Exception("Exception writing html File " + cur_file);
		}
		try {
			fileOutStream.close();
			dataOutStream.close();
		} catch (IOException e) {
			throw new Exception("IO exception closing File " + cur_file);
		}
	}

	/**
	 * This method writes a comment to standarderr if debugging is on
	 * @param comment String
	 */
	public static void trace(String comment) {
		trace(comment, false);
	}

	/**
	 * This method writes a comment to STDERR if debugging is on and optionally
	 * appends a date
	 * @param comment String
	 * @param print_date boolean
	 */
	public static void trace(String comment, boolean print_date) {
		if (debugging()) {
			if (print_date) {
				java.util.Date now = new java.util.Date();
				System.err.println(comment + " (" + now + ")");
			} else {
				System.err.println(comment);
			}
			System.err.flush();
		}
	}

	/**
	 * This method logs comments to the log stream
	 * @param comment String
	 */
	public static void logComment(String comment) {
		logComment(comment, false);
	}

	/**
	 * This method logs comments to the log stream and appends the current date to
	 * those comments.
	 * @param comment String
	 * @param print_date boolean
	 */
	public static void logComment(String comment, boolean print_date) {
		PrintWriter log = (PrintWriter) objects.get(LOG);
		if (log == null) {
			return;
		}
		String str = "\t" + comment;
		if (print_date) {
			java.util.Date now = new java.util.Date();
			str = str + " (" + now + ")";
		}
		if (debugging()) {
			trace(str);
		}
		log.println(str);
		log.flush();
	}

	/**
	 * This method is used to track how long something runs for. It prints a
	 * comment to log and records the time for later use.
	 * @param key String
	 */
	public static void logStartTime(String key) {
		PrintWriter log = (PrintWriter) objects.get(LOG);
		if (log == null) {
			return;
		}
		java.util.Date now = new java.util.Date();
		timetable.put(key, now);
		String str = "\n" + key + ": " + now + "\n";
		log.println(str);
		log.flush();
		trace(str);
	};

	/**
	 * This method is used with logStart time to track how long something runs
	 * for. It logs to log and appends the current date. It also calculates the
	 * time between the current date and the start date and prints that elapsed
	 * time out
	 * @param key String
	 */
	public static void logElapsedTime(String key) {
		PrintWriter log = (PrintWriter) objects.get(LOG);
		if (log == null) {
			return;
		}
		java.util.Date now = new java.util.Date();
		java.util.Date then = (java.util.Date) (timetable.get(key));
		String str = "\n" + "Elapsed time for '" + key + "': "
				+ timeToString(timeDifference(now, then));
		log.println(str);
		timetable.remove(key);
		log.flush();
		trace(str);
	};

	/**
	 * This method returns the elapsed time for an operation
	 * @return Date
	 */
	public static String getElapsedTime(String key) {
		return timeToString(timeDifference(new java.util.Date(),
				(java.util.Date) timetable.get(key)));
	}

	/**
	 * this method returns true if the string appears in the string array
	 * @param key String
	 * @param list String []
	 * @return boolean
	 */
	public static boolean member(String key, String[] list) {
		boolean found = false;
		for (int i = 0; i < list.length; i++) {
			if (key.equals(list[i])) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * This method counts the number of lines in a file (flags="-l") or the number
	 * of bytes in a file (flags="")
	 * @param filename String
	 * @param flags String
	 * @return int
	 */
	public static int wc(String filename, String flags) {
		int i = 0;
		try {
			if (flags.equals("-l")) {
				BufferedReader br = new BufferedReader(new FileReader(filename));
				while (br.readLine() != null) {
					i++;
				}
			} else {
				File f = new File(filename);
				return (int) (f.length());
			}
		} catch (Exception e) {
		}
		;
		return i;
	};

	/**
	 * This method executes an operating system command
	 * @param command String
	 */
	public static void exec(String command) throws Exception {
		exec(command, new String[] {}, true, USE_INPUT_STREAM, false);
	};

	/**
	 * This method executes an operating system command given the environment
	 * @param command String
	 * @param env String []
	 */
	public static void exec(String command, String[] env) throws Exception {
		exec(command, env, true, USE_INPUT_STREAM, false);
	};

	/**
	 * This method executes an operating system command. It also logs either the
	 * STDERR or STDOUT of that process to the log. Valid values for writeMode
	 * paramter are RxConstants.{USE_INPUT_STREAM,USE_ERROR_STREAM}.
	 * @param command String
	 * @param write_to_log boolean
	 * @param write_mode int
	 */
	public static void exec(String command, String[] env, boolean write_to_log,
			int write_mode, boolean background) throws Exception {
		trace("MEMEToolkit::exec - " + command);

		String line;
		Runtime run = Runtime.getRuntime();
		Process proc = run.exec(command, env);
		PrintWriter log = getLog();
		BufferedReader in;
		log.println("MEMEToolkit::exec.log - " + command);

		if (write_to_log) {
			if (write_mode == RxConstants.USE_INPUT_STREAM) {
				InputStreamReader converter = new InputStreamReader(proc
						.getInputStream());
				in = new BufferedReader(converter);
			} else if (write_mode == RxConstants.USE_ERROR_STREAM) {
				InputStreamReader converter = new InputStreamReader(proc
						.getErrorStream());
				in = new BufferedReader(converter);
			} else {
				throw new IllegalArgumentException("Illegal write mode to exec: "
						+ write_mode);
			}
			while ((line = in.readLine()) != null) {
				log.println("\t" + line);
				log.flush();
			}
		}
		;

		if (!background) {
			proc.waitFor();
			if (proc.exitValue() != 0) {
				// here read from error stream
				InputStreamReader converter = new InputStreamReader(proc
						.getErrorStream());
				in = new BufferedReader(converter);
				log.println("\n--------------------------------------------");
				log.println("Error:");
				while ((line = in.readLine()) != null) {
					log.println("\t" + line);
					log.flush();
				}
				log.println("--------------------------------------------\n");
				log.flush();
				throw new Exception("Command : '" + command + "' failed ("
						+ proc.exitValue() + ").");
			}
		}
	};

	/**
	 * This public method takes a time (in thousandths of a second) and converts
	 * it to a string representation
	 * @param time long
	 */
	public static String timeToString(long time) {

		if (time < 1000) {
			return time + " thousandths of a second.";
		} else {
			int hours = (int) (time / 3600000);
			time = time % 3600000;
			int minutes = (int) (time / 60000);
			time = time % 60000;
			int seconds = (int) (time / 1000);

			String h = "0" + hours;
			String m = "0" + minutes;
			String s = "0" + seconds;
			return h.substring(h.length() - 2) + ":" + m.substring(m.length() - 2)
					+ ":" + s.substring(s.length() - 2);
		}

	}

	/**
	 * This method takes two dates and returns the time in thousandths of a second
	 * between the two
	 * @param now java.util.Date
	 * @param then java.util.Date
	 * @return long
	 */
	public static long timeDifference(java.util.Date now, java.util.Date then) {

		return now.getTime() - then.getTime();
	};

	/**
	 * This method takes a String[] of class names, a dialog title, and dialog
	 * text and it returns an instance of a class of a type chosen by the user
	 * from the set. The class names passed in the string array must be fully
	 * qualified and have a static typeToString method and have a default
	 * constructor. An exception thrown indicates failure
	 * 
	 * If the user cancells the operation, return null;
	 * 
	 * @param class_names String[]
	 * @param title String
	 * @param text String
	 * @return Object
	 */
	public static Object classChooser(String[] class_names, String title,
			String text) throws Exception {

		trace("MEMEToolkit::classChooser (" + class_names + "," + title + ","
				+ text);
		String class_type = null;
		HashMap class_type_map = new HashMap();

		if (class_names.length == 0) {
			throw new Exception("No classes to choose from.");
		} else {
			// Build map of types to class names
			for (int i = 0; i < class_names.length; i++) {
				Class c = Class.forName(class_names[i]);
				Method m = c.getMethod("typeToString", new Class[] {});
				class_type = (String) m.invoke(null, new Object[] {});
				class_type_map.put(class_type, c);
			}

			if (class_names.length > 1) {
				// Get response from user (sort types)
				TreeSet sortedkeys = new TreeSet(class_type_map.keySet());
				class_type = (String) ListDialog.showListSingleMode(null, text, title,
						sortedkeys.toArray(), null);
			} else {
			}
		}
		;

		// Dynamically load new step,
		// restore working step upon failure.
		Object obj = null;
		RxToolkit.trace("\nMEMEToolkit.classChooser - creating " + class_type
				+ "\n");
		if (class_type != null) {
			Class step_class = (Class) (class_type_map.get(class_type));
			/*
			 * Constructor constr = step_class.getConstructor( new Class [] {}); obj =
			 * constr.newInstance(new Object [] {});
			 */
			obj = step_class.newInstance();

		}
		return obj;

	};

	/**
	 * This method creates a db output buffer of size "size".
	 */
	public static void enableBuffer(int size) throws SQLException {
		trace("MEMEToolkit::enableBuffer");
		StringBuffer call = new StringBuffer("{call DBMS_OUTPUT.enable(");
		call.append(size);
		call.append(")}");
		CallableStatement cstmt = getSQLConnection().prepareCall(call.toString());
		cstmt.execute();
	}

	/**
	 * Flushes the database buffer to MEMEToolkit.trace(). So the information
	 * stored in the database buffer can be seen from a Java program.
	 */
	public static void flushBuffer() {
		trace("MEMEToolkit::flushBuffer");
		try {
			String call = "{call DBMS_OUTPUT.get_line(?,?)}";
			CallableStatement cstmt = getSQLConnection().prepareCall(call);
			String line;
			int status;
			do {
				cstmt.registerOutParameter(1, Types.VARCHAR);
				cstmt.registerOutParameter(2, Types.INTEGER);
				cstmt.execute();
				status = cstmt.getInt(2);
				line = cstmt.getString(1);
				if (line != null) {
					RxToolkit.logComment(line);
				}
			} while (status != 1);
		} catch (Exception e) {
			RxToolkit.logComment("EnhancedConnection: exception in flushBuffer");
		}
	}

	/**
	 * This method takes a package name and a superclass (fully qualified) name.
	 * If any of the *.jar files in the current classpath contain classes in the
	 * named package with the specified superclass, their fully qualified class
	 * names are returned in the string array.
	 * 
	 * This is used to dynamically load Recipes and RxSteps.
	 * @param package_name String
	 * @param superclass_name String
	 * @return String []
	 */
	public static String[] getSubclasses(String package_name,
			String superclass_name) throws ClassNotFoundException {

		ArrayList subclasses = new ArrayList();

		String class_path = System.getProperty("java.class.path");
		String path_separator = System.getProperty("path.separator");
		StringTokenizer path_names = new StringTokenizer(class_path, path_separator);

		while (path_names.hasMoreElements()) {
			String s = path_names.nextToken();

			// Get only JAR files
			if (s.endsWith(".jar")) {
				System.out.println("Checking " + s);
				JarFile jf = null;
				try {
					File f = new File(s);
					if (f.exists()) {
						jf = new JarFile(f);
					} else {
						// JAR file in classpath, but not actually there; jump to next.
						continue;
					}
				} catch (Exception e) {
					System.err.println("Exception: " + e.toString() + " -- "
							+ e.getMessage());
					Exit(-1);
				}
				Enumeration enumeration = jf.entries();
				while (enumeration.hasMoreElements()) {

					// Replace path separators with '.'
					// char file_separator =
					// System.getProperty("file.separator").charAt(0);
					char file_separator = '/';
					String entry = (enumeration.nextElement()).toString().replace(
							file_separator, '.');

					// System.out.println("Entry length: "+entry.length());

					// Remove ".class" extension
					if (entry.endsWith(".class")) {
						entry = entry.substring(0, entry.length() - ".class".length());
					} else {
						continue;
					}

					// Get classes from specific package
					if (entry.startsWith(package_name)) {

						// Filter out subpackages, inner classes
						String stripped_entry = entry.substring((package_name + ".")
								.length(), entry.length());
						if ((stripped_entry.indexOf(".") == -1)
								&& (stripped_entry.indexOf("$") == -1)) {

							// Finally, get classes that have specific superclass
							// Be sure to use fully specified superclass name
							Class entry_class = Class.forName(entry);
							if ((entry_class.getModifiers() & Modifier.ABSTRACT) != 0) {
								continue;
							}
							Class superclass_to_match = Class.forName(superclass_name);
							Class entry_superclass = entry_class;

							do {
								entry_superclass = entry_superclass.getSuperclass();
								// System.out.println("Entry Superclass: "+entry_superclass);

								if (entry_superclass != null) {
									if (entry_superclass.equals(superclass_to_match)) {
										trace("MEMEToolkit::getSubclasses - adding " + entry);
										subclasses.add(entry);
										break;
									}
								}

							} while (entry_superclass != null);
						}
					}
				}
			}
		}
		String[] _subclasses = new String[subclasses.size()];
		for (int i = 0; i < subclasses.size(); i++) {
			_subclasses[i] = (String) subclasses.get(i);
		}
		return _subclasses;
	};

	/**
	 * This method is used to clean up some loose ends before calling
	 * system.exit();
	 * 
	 */
	public static void Exit(int return_value) {
		java.sql.Connection db_connection = (java.sql.Connection) objects
				.get(DB_CONNECTION);
		if (db_connection != null)
			try {
				db_connection.close();
			} catch (Exception e) {
			}
		;

		System.err.println("Exiting ....");
		System.exit(return_value);
	}

	/**
	 * This method reads a PROPERTY file and adds it to properties
	 * @param filename String
	 */
	public static void loadProperty(String filename) {
	};

	/**
	 * This method takes a string and makes it multi-line HTML by breaking at
	 * words every x characters and moving on.
	 * @param string String
	 * @param length int
	 * @return String
	 */
	public static String toMultiLineHTML(String string, int length) {

		// tokenize at whitespace
		StringTokenizer st = new StringTokenizer(string);

		// Begin stringbuffer
		StringBuffer multi_line = new StringBuffer("<html>");
		String word, line = "";
		boolean append_flag = false;
		while (st.hasMoreTokens()) {
			word = st.nextToken();
			line = line + " " + word;
			append_flag = false;
			if (line.length() > 40) {
				multi_line.append(line + "<p>");
				line = "";
				append_flag = true;
			}
		}
		if (!append_flag)
			multi_line.append(line);
		multi_line.append("</html>");
		return multi_line.toString();
	};

	/**
	 * This method converts String [] to Object []
	 * @param sa String []
	 * @return Object []
	 */
	public static Object[] toObjectArray(String[] sa) {
		Object[] oa = new Object[sa.length];
		for (int i = 0; i < sa.length; i++)
			oa[i] = (Object) sa[i];
		return oa;
	}

	/**
	 * This method converts Object [] to String []
	 * @param sa Object []
	 * @return String []
	 */
	public static String[] toStringArray(Object[] oa) {
		String[] sa = new String[oa.length];
		for (int i = 0; i < sa.length; i++)
			sa[i] = oa[i].toString();
		return sa;
	}

	/**
	 * This method serializes an object given an object It looks up the file in
	 * object_file_map and calls serializeAs if it cannot find it
	 * @param obj Object
	 */
	public static void serialize(Object obj) throws Exception {
		File f = getFileForObject(obj);
		if (f == null) {
			serializeAs(obj);
		} else {
			serialize(obj, f);
		}
	};

	/**
	 * This method serializes an object given a File and an Object
	 * @param obj Object
	 * @param file File
	 */
	public static void serialize(Object obj, File file) throws Exception {
		trace("MEMEToolkit::serialize(" + obj + "," + file + ")");

		//
		// Serialize the object
		try {
			if (file.exists() && !(file.equals(getFileForObject(obj)))) {
				boolean response = confirmRequest("The file \"" + file
						+ "\" already exists.\n" + "Do you want to overwrite it?");
				if (!response)
					throw new Exception("Save was cancelled.");
			}
			;
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutput oo = new ObjectOutputStream(fos);
			try {
				oo.writeObject(obj);
			} catch (IOException e) {
				throw new Exception("Couldn't write to file (" + file + ").\n"
						+ "Check permission, etc.\n" + e.getMessage());
			}
			oo.close();

			trace("Data has been serialized (" + obj + "," + file + ")");

		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

		object_file_map.put(obj, file);

	}

	/**
	 * This method save an object to a user selected file Here the directory is
	 * not known
	 * @param obj Object
	 */
	public static void serializeAs(Object obj) throws Exception {
		serializeAs(obj, null);
	}

	/**
	 * This method save an object to a user selected file The directory is the
	 * default for the file chooser
	 * @param obj Object
	 * @param directory String
	 */
	public static void serializeAs(Object obj, File file) throws Exception {
		trace("MEMEToolkit::serializeAs(" + obj + ")");

		// Choose file
		File cur_file = getSaveFile(file, "Save to file", "Save");

		// bail if operation cancelled
		if (cur_file == null)
			throw new Exception("Save was cancelled.");

		serialize(obj, cur_file);
	}

	/**
	 * This method deserializes an object from a file If file is null, look it up
	 * @return Object
	 */
	public static Object deserialize() throws Exception {
		return deserialize(null);
	}

	/**
	 * This method deserializes an object from a file If file is null, look it up
	 * @param filename File
	 * @return Object
	 */
	public static Object deserialize(File file) throws Exception {
		trace("MEMEToolkit::deserialize(" + file + ")");

		// Choose file
		File cur_file = getSaveFile(file, "Select File to Open", "Open");

		// if cancel, bail
		if (cur_file == null)
			throw new Exception("Load was cancelled.");

		// so that callers know what file was used
		file = cur_file;

		trace("MEMEToolkit::deserialize(" + file + ")");

		Object obj;
		try {
			// deserialize
			FileInputStream in = new FileInputStream(cur_file);
			ObjectInputStream s = new ObjectInputStream(in);
			try {

				obj = s.readObject();

			} catch (Exception e) {
				throw new Exception("Unable to open object.  The file may contain\n"
						+ "an older version that is no longer compatible.\n"
						+ e.getMessage());
			}

		} catch (Exception e) {
			throw new Exception("Deserialize execption : " + e.getMessage());
		}

		object_file_map.put(obj, cur_file);
		trace("MEMEToolkit::deserialize(" + obj + ")");
		return obj;
	}

	public static void save(Object obj) throws Exception {
		trace("MEMEToolkit::save()");
		File f = getFileForObject(obj);
		PersistenceManager pm = (PersistenceManager) objects.get(PM);
		if (pm == null)
			throw new Exception(
					"No persistence manager: Application must call setPersistenceManager()");

		if (f == null) {
			saveAs(obj, null, pm);
		} else {
			save(obj, f, pm);
		}
	};

	public static void save(Object obj, File file, PersistenceManager pm)
			throws Exception {
		trace("MEMEToolkit::save(" + file + ")");
		// Save the object
		// persistence manager deals with it
		pm.write(obj, file);
		trace("Data has been saved (" + obj + "," + file + ")");
		object_file_map.put(obj, file);
	};

	public static void saveAs(Object obj) throws Exception {
		trace("MEMEToolkit::saveAs()");
		PersistenceManager pm = (PersistenceManager) objects.get(PM);
		if (pm == null)
			throw new Exception(
					"No persistence manager: Application must call setPersistenceManager()");
		saveAs(obj, null, pm);
	};

	public static void saveAs(Object obj, File file, PersistenceManager pm)
			throws Exception {
		trace("MEMEToolkit::saveAs(" + file + ")");
		// Choose file
		File cur_file = getSaveFile(file, "Save to file", "Save");
		// bail if operation cancelled
		if (cur_file == null)
			throw new Exception("Save was cancelled.");
		save(obj, cur_file, pm);
	};

	public static Object open() throws Exception {
		trace("MEMEToolkit::open()");
		PersistenceManager pm = (PersistenceManager) objects.get(PM);
		if (pm == null)
			throw new Exception(
					"No persistence manager: Application must call setPersistenceManager()");
		return open(null, pm);
	};

	public static Object open(File file) throws Exception {
		trace("MEMEToolkit::open(" + file + ")");
		PersistenceManager pm = (PersistenceManager) objects.get(PM);
		if (pm == null)
			throw new Exception(
					"No persistence manager: Application must call setPersistenceManager()");
		return open(file, pm);
	};

	public static Object open(File file, PersistenceManager pm) throws Exception {
		trace("MEMEToolkit::open()");
		// Choose file
		File cur_file = getSaveFile(file, "Select File to Open", "Open");
		// if cancel, bail
		if (cur_file == null)
			throw new Exception("Load was cancelled.");
		// so that callers know what file was used
		file = cur_file;
		Object obj = pm.read(file);
		object_file_map.put(obj, cur_file);
		return obj;
	}

	/**
	 * This method creates an html file that represents the Object and invokes a
	 * browser. If the initialized browser does not work for some reason, this
	 * application's html viewer is invoked.
	 * @param file File
	 */
	public static void viewHTML(File file) {
		RxToolkit.trace("RxWriter::requestViewAsHTML()");

		URL url = null;
		try {
			url = file.toURL();
		} catch (MalformedURLException e) {
			reportError("Malformed URL: " + e.getMessage());
		}
		RxToolkit.trace("View HTML : " + url);

		JFrame frame = new JFrame();
		frame.setTitle(file.getAbsolutePath());
		frame.setName("HtmlViewer");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(750, 500);

		HtmlViewer html_viewer = new HtmlViewer();
		html_viewer.setPage(url);
		frame.setContentPane(new JScrollPane(html_viewer));
		frame.setVisible(true);

	};

	public void finalize() {
		try {
			getSQLConnection().close();
		} catch (Exception e) {
		}
		;
	}

	//
	// DBToolkit functionality
	// These functions grant access to database data
	// 
	// This is implemented as an inner class

	/**
	 * 
	 * This class is a general utility class to be used by the RecipeWriter to
	 * access the database for certain information like
	 * 
	 * binary integrity checks, unary integrity checks data checks, merge checks,
	 * fields from mergefacts.src all integrity checks, filter types, languages
	 * sources, termgroups, tobereleased values
	 * 
	 * 
	 * @author Brian Carlsen (3/1999) Edited By Owen J. Carlsen, Yun-Jung Kim
	 * 
	 * @version 1.5
	 * 
	 */

	public static class DBToolkit {

		// 
		// code_map types
		//
		public final static String STRING_MATCH = "string_match";

		public final static String CODE_MATCH = "code_match";

		// 
		// Types used externally
		//
		//
		public final static String STY = "SEMANTIC_TYPE";

		public final static String MERGE_SET = "MERGE_SET";

		public final static String CODE = "CODE";

		public final static String SOURCE = "SOURCE";

		public final static String TERMGROUP = "TERMGROUP";

		public final static String LANGUAGE = "LANGUAGE";

		public final static String FILTER_TYPES = "FILTER_TYPES";

		public final static String INTEGRITY_CHECKS = "INTEGRITY_CHECKS";

		public final static String UNARY_CHECKS = "UNARY_CHECKS";

		public final static String BINARY_CHECKS = "BINARY_CHECKS";

		public final static String ALL_CHECKS = "ALL_CHECKS";

		public final static String DATA_CHECKS = "DATA_CHECKS";

		public final static String MERGE_CHECKS = "MERGE_CHECKS";

		public final static String TOBERELEASED = "TOBERELEASED";

		public final static String STATUS = "STATUS";

		public static HashMap allowed_types = new HashMap();
		static {
			allowed_types.put(MERGE_SET, "");
			allowed_types.put(SOURCE, "");
			allowed_types.put(TERMGROUP, "");
			allowed_types.put(LANGUAGE, "");
			allowed_types.put(FILTER_TYPES, "");
			allowed_types.put(UNARY_CHECKS, "");
			allowed_types.put(BINARY_CHECKS, "");
			allowed_types.put(ALL_CHECKS, "");
			allowed_types.put(DATA_CHECKS, "");
			allowed_types.put(MERGE_CHECKS, "");
			allowed_types.put(TOBERELEASED, "");
			allowed_types.put(STATUS, "");
			allowed_types.put(STY, "");
		};

		// SQL Objects
		private static java.sql.Connection connection;

		private static java.sql.Statement statement;

		private static java.sql.ResultSet results;

		// Cache'd copies of the data
		private static HashMap cache = new HashMap();

		/**
		 * This method calls the various functions to cache all of the data When new
		 * functionality is added to this class, it should called here too.
		 */
		public static void cacheData() {
			// Get all codes
			String[] code_types = getCodes("valid_code_type");
			for (int i = 0; i < code_types.length; i++) {
				RxToolkit.trace("MEMEToolkit::DBToolkit.getCodes - "
						+ "valid code type: " + code_types[i]);
				getCodes(code_types[i]);
			}
			// Get unary/binary integrity checks
			getUnaryChecks(true);
			getUnaryChecks(false);
			getBinaryChecks(true);
			getBinaryChecks(false);
			getDataChecks();
			getMergeChecks();
			getIntegrityChecks();
			getLanguages();
			getSources();
			getTermgroups();
			getSemanticTypes();
			getTobereleased();
			getStatus();
		}

		/**
		 * This method clears the cache
		 */
		public static void clearCache() {
			cache = new HashMap();
		}

		/**
		 * This method sets up the connection and statement fields
		 */
		public static void getConnectionAndStatement() throws SQLException {
			if (connection == null) {
				connection = getSQLConnection();
			}
			statement = connection.createStatement();
		}

		/**
		 * This method calls MEME_UTILITY.new_work
		 * @param authority String
		 * @param type String
		 * @param dsc String
		 */
		public static int new_work(String authority, String type, String dsc)
				throws Exception {
			trace("MEMEToolkit.DBToolkit::new_work");

			CallableStatement stmt = getSQLConnection().prepareCall(
					"{ ? = call MEME_UTILITY.new_work(?,?,?)}");
			stmt.registerOutParameter(1, Types.INTEGER);
			stmt.setString(2, authority);
			stmt.setString(3, type);
			stmt.setString(4, dsc);
			stmt.execute();
			int i = stmt.getInt(1);
			stmt.close();
			trace("\t trace work_id = " + i);
			return i;
		}

		/**
		 * This method takes a type and determines which of the other methods to
		 * call. Since each returns a different thing, this returns an Object which
		 * must be cast back to the appropriate type.
		 * @param type String
		 * @return Object
		 */
		public static Object[] getFilterData(String type) {

			if (type.equals(SOURCE)) {
				return RxToolkit.toObjectArray(RxToolkit.getSources());
			} else if (type.equals(TERMGROUP)) {
				return RxToolkit.toObjectArray(RxToolkit.getTermgroups());
			} else if (type.equals(STY)) {
				return RxToolkit.toObjectArray(getSemanticTypes());
			} else if (type.equals(STATUS)) {
				return RxToolkit.toObjectArray(getStatus());
			} else if (type.equals(FILTER_TYPES)) {
				return RxToolkit.toObjectArray(getFilterTypes());
			}

			return null;
		}

		/**
		 * This method looks up codes in code map by type
		 * @param type String
		 * @return String []
		 */
		public static String[] getCodes(String type) {
			RxToolkit.trace("MEMEToolkit.DBToolkit::getCodes()");
			if (cache.get(CODE + type) != null) {
				return (String[]) cache.get(CODE + type);
			}

			ArrayList codes_from_db = new ArrayList();
			try {
				getConnectionAndStatement();
				results = statement
						.executeQuery("select code from code_map where type = '" + type
								+ "' order by code");
				while (results.next()) {
					codes_from_db.add(results.getString("CODE"));
				}
			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception: \n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new String[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Exception while reading codes from mid\n:" + e2);
				return new String[] {};
			}
			;

			// Convert to string array
			String[] all_codes = RxToolkit.toStringArray(codes_from_db.toArray());

			cache.put(CODE + type, all_codes);
			return all_codes;

		}

		/**
		 * This method gets only the checks found in ic_sources integrity checks If
		 * with_values is set, the actual rows and values are retrieved NOTE:
		 * ic_single should have a dummy row for each allowed one return
		 * IntegrityCheck []
		 */
		public static UnaryIntegrityCheck[] getUnaryChecks(boolean with_values) {
			RxToolkit.trace("MEMEToolkit.DBToolkit::getUnaryChecks(" + with_values
					+ ")");

			if (cache.get(UNARY_CHECKS + with_values) != null)
				return (UnaryIntegrityCheck[]) cache.get(UNARY_CHECKS + with_values);

			ArrayList ic_checks = new ArrayList();
			String query = "";
			if (with_values) {
				query = "select distinct a.ic_name,v_actions,c_actions, "
						+ "ic_status, ic_type, activation_date, deactivation_date, "
						+ "ic_short_dsc, ic_long_dsc, negation, type, value "
						+ "from integrity_constraints a, ic_single b "
						+ "where a.ic_name = b.ic_name order by a.ic_name, type, value";
			} else {
				query = "select distinct a.ic_name,v_actions,c_actions, "
						+ "ic_status, ic_type, activation_date, deactivation_date, "
						+ "ic_short_dsc, ic_long_dsc "
	  	  		+ "from integrity_constraints a, ic_single b "
 			    	+ "where a.ic_name = b.ic_name order by a.ic_name";
			}

			try {
				// Set up the database connection if not already active;
				getConnectionAndStatement();
				results = statement.executeQuery(query);
				RxToolkit.trace("MEMEToolkit.DBToolkit::getUnaryChecks query - "
						+ query);

				// Iterate through ResultSet
				while (results.next()) {
					UnaryIntegrityCheck ic = new UnaryIntegrityCheck();
					ic.ic_name = results.getString("IC_NAME");
					ic.violation_actions = results.getString("V_ACTIONS");
					ic.correction_actions = results.getString("C_ACTIONS");
					ic.ic_status = results.getString("IC_STATUS");
					ic.ic_type = results.getString("IC_TYPE");
					ic.activation_date = results.getDate("ACTIVATION_DATE");
					ic.deactivation_date = results.getDate("DEACTIVATION_DATE");
					ic.short_description = results.getString("IC_SHORT_DSC");
					ic.long_description = results.getString("IC_LONG_DSC");
					if (with_values) {
						ic.negation = (results.getString("NEGATION").equals("Y"));
						ic.type = results.getString("TYPE");
						ic.value = results.getString("VALUE");
					}
					ic_checks.add(ic);
				}

			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception\n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new UnaryIntegrityCheck[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Unknown exception " + e2);
				return new UnaryIntegrityCheck[] {};
			}
			;

			// Return as IntegrityCheck []
			UnaryIntegrityCheck[] ics = new UnaryIntegrityCheck[ic_checks.size()];
			int index = 0;
			Iterator iter = ic_checks.iterator();
			while (iter.hasNext()) {
				ics[index++] = (UnaryIntegrityCheck) iter.next();
			}
			;

			cache.put(UNARY_CHECKS + with_values, ics);
			return ics;
		}

		/**
		 * This method gets checks which have binary predicates. If with_values is
		 * set, it retrieves the contents if ic_pair instead of just the
		 * IntegrityCheck info NOTE: ic_pair should have a dummy row for each
		 * allowed one
		 * @return IntegrityCheck []
		 */
		public static BinaryIntegrityCheck[] getBinaryChecks(boolean with_values) {
			RxToolkit.trace("MEMEToolkit.DBToolkit::getBinaryChecks(" + with_values
					+ ")");

			if (cache.get(BINARY_CHECKS + with_values) != null)
				return (BinaryIntegrityCheck[]) cache.get(BINARY_CHECKS + with_values);

			ArrayList ic_checks = new ArrayList();
			String query = "";
			if (with_values) {
				query = "select distinct a.ic_name,v_actions,c_actions, "
						+ "ic_status, ic_type, activation_date, deactivation_date,  "
						+ "ic_short_dsc, ic_long_dsc, negation, type_1, value_1, "
						+ "type_2, value_2 from integrity_constraints a, ic_pair b "
						+ "where a.ic_name = b.ic_name order by a.ic_name, type_1, value_1";
			} else {
				query = "select distinct a.ic_name,v_actions,c_actions, "
						+ "ic_status, ic_type, activation_date, deactivation_date,  "
						+ "ic_short_dsc, ic_long_dsc "
						+ "from integrity_constraints a, ic_pair b "
						+ "where a.ic_name = b.ic_name order by a.ic_name";
			}
			try {
				// Set up the database connection if not already active;
				getConnectionAndStatement();
				results = statement.executeQuery(query);
				RxToolkit.trace("MEMEToolkit.DBToolkit::getBinaryChecks query - "
						+ query);

				// Iterate through ResultSet
				while (results.next()) {
					BinaryIntegrityCheck ic = new BinaryIntegrityCheck();
					ic.ic_name = results.getString("IC_NAME");
					ic.violation_actions = results.getString("V_ACTIONS");
					ic.correction_actions = results.getString("C_ACTIONS");
					ic.ic_status = results.getString("IC_STATUS");
					ic.ic_type = results.getString("IC_TYPE");
					ic.activation_date = results.getDate("ACTIVATION_DATE");
					ic.deactivation_date = results.getDate("DEACTIVATION_DATE");
					ic.short_description = results.getString("IC_SHORT_DSC");
					ic.long_description = results.getString("IC_LONG_DSC");
					if (with_values) {
						ic.negation = (results.getString("NEGATION").equals("Y"));
						ic.type_1 = results.getString("TYPE_1");
						ic.value_1 = results.getString("VALUE_1");
						ic.type_2 = results.getString("TYPE_2");
						ic.value_2 = results.getString("VALUE_2");
					}
					ic_checks.add(ic);
				}

			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception\n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new BinaryIntegrityCheck[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Unknown exception " + e2);
				return new BinaryIntegrityCheck[] {};
			}
			;

			// Return as IntegrityCheck []
			BinaryIntegrityCheck[] ics = new BinaryIntegrityCheck[ic_checks.size()];
			int index = 0;
			Iterator iter = ic_checks.iterator();
			while (iter.hasNext()) {
				ics[index++] = (BinaryIntegrityCheck) iter.next();
			}
			;

			cache.put(BINARY_CHECKS + with_values, ics);
			return ics;
		};

		/**
		 * This method gets only the data integrity checks
		 * @return IntegrityCheck []
		 */
		public static IntegrityCheck[] getDataChecks() {
			RxToolkit.trace("MEMEToolkit.DBToolkit::getDataChecks()");
			return getIntegrityChecks(DATA_CHECKS);
		}

		/**
		 * This method gets only the merge integrity checks
		 * @return IntegrityCheck []
		 */
		public static IntegrityCheck[] getMergeChecks() {
			return getIntegrityChecks(MERGE_CHECKS);
		}

		/**
		 * This method returns all integrity checks.
		 * @return String[]
		 */
		public static IntegrityCheck[] getIntegrityChecks() {
			RxToolkit.trace("MEMEToolkit.DBToolkit::getIntegrityChecks()");
			return getIntegrityChecks(ALL_CHECKS);
		}

		/**
		 * This method looks up integrity checks in the database It can screen by
		 * DATA, MERGE, or ALL types
		 * @param type String
		 * @return IntegrityCheck []
		 */
		public static IntegrityCheck[] getIntegrityChecks(String type) {

			RxToolkit
					.trace("MEMEToolkit.DBToolkit::getIntegrityChecks(" + type + ")");

			if (cache.get(INTEGRITY_CHECKS + type) != null) {
				return (IntegrityCheck[]) cache.get(INTEGRITY_CHECKS + type);
			}

			ArrayList ic_checks = new ArrayList();
			String query;

			if (type.equals(DATA_CHECKS)) {
				query = "select * from integrity_constraints where ic_status = 'A' and ic_type='R' order by ic_name";
			} else if (type.equals(MERGE_CHECKS)) {
				query = "select * from integrity_constraints where ic_status = 'A' and ic_type='I' order by ic_name";
			} else {
				query = "select * from integrity_constraints where ic_status = 'A' order by ic_name";
			}

			try {
				// Set up the database connection if not already active;
				getConnectionAndStatement();
				results = statement.executeQuery(query);

				// Iterate through ResultSet
				while (results.next()) {
					IntegrityCheck ic = new IntegrityCheck();
					ic.ic_name = results.getString("IC_NAME");
					ic.violation_actions = results.getString("V_ACTIONS");
					ic.correction_actions = results.getString("C_ACTIONS");
					ic.ic_status = results.getString("IC_STATUS");
					ic.ic_type = results.getString("IC_TYPE");
					ic.activation_date = results.getDate("ACTIVATION_DATE");
					ic.deactivation_date = results.getDate("DEACTIVATION_DATE");
					ic.short_description = results.getString("IC_SHORT_DSC");
					ic.long_description = results.getString("IC_LONG_DSC");
					ic_checks.add(ic);
				}

			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception\n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new IntegrityCheck[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Unknown exception " + e2);
				return new IntegrityCheck[] {};
			}
			;

			// Return as IntegrityCheck []
			IntegrityCheck[] ics = new IntegrityCheck[ic_checks.size()];
			int index = 0;
			Iterator iter = ic_checks.iterator();
			while (iter.hasNext()) {
				ics[index++] = (IntegrityCheck) iter.next();
			}
			;

			cache.put(INTEGRITY_CHECKS + type, ics);
			return ics;

		}

		/**
		 * This method looks up filter types types in code_map
		 * @return String []
		 */
		public static String[] getFilterTypes() {

			RxToolkit.trace("MEMEToolkit.DBToolkit::getFilterTypes()");

			if (cache.get(FILTER_TYPES) != null)
				return (String[]) cache.get(FILTER_TYPES);

			ArrayList types = new ArrayList();

			try {
				// Set up the database connection if not already active;
				getConnectionAndStatement();
				results = statement
						.executeQuery("select code from code_map where type='filter_type' order by code");

				// Iterate through ResultSet
				while (results.next()) {
					types.add(results.getString("CODE"));
				}

			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception\n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new String[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Unknown exception " + e2);
				return new String[] {};
			}
			;

			// Return as String []
			String[] types_array = new String[types.size()];
			int index = 0;
			Iterator iter = types.iterator();
			while (iter.hasNext()) {
				types_array[index++] = (String) iter.next();
			}
			;

			Arrays.sort(types_array);
			cache.put(FILTER_TYPES, types_array);
			return types_array;

		}

		/**
		 * This method gets the valid language values
		 * @return String []
		 */
		public static String[] getLanguages() {
			ArrayList languages = new ArrayList();
			try {
				getConnectionAndStatement();
				results = statement.executeQuery("select lat from language order by lat");
				while (results.next()) {
					languages.add(results.getString("LAT"));
				}
			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception\n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new String[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Unknown exception " + e2);
				return new String[] {};
			}
			;

			Iterator iter = languages.iterator();
			String[] lang = new String[languages.size()];
			int index = 0;
			while (iter.hasNext()) {
				lang[index++] = (String) iter.next();
			}
			;
			return lang;
		};

		/**
		 * This method looks up sources in the database or just returns a standard
		 * list. It also looks up sources in the ranks.src file.
		 * @return String []
		 */
		public static String[] getSources() {
			RxToolkit.trace("MEMEToolkit.DBToolkit::getSources()");
			if (cache.get(SOURCE) != null) {
				return (String[]) cache.get(SOURCE);
			}
			;

			String[] all_sources;
			ArrayList sources_from_db = new ArrayList();

			try {
				getConnectionAndStatement();
				results = statement
						.executeQuery("select source from source_rank order by 1");
				while (results.next()) {
					sources_from_db.add(results.getString(SOURCE));
				}
			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception: \n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new String[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Exception while reading sources from mid\n:"
						+ e2);
				return new String[] {};
			}
			;

			// Convert to string array
			Iterator iter = sources_from_db.iterator();
			all_sources = new String[sources_from_db.size()];
			int index = 0;
			while (iter.hasNext()) {
				all_sources[index++] = (String) iter.next();
			}

			cache.put(SOURCE, all_sources);
			return all_sources;

		}

		/**
		 * This method looks up termgroups in the database or just returns a
		 * standard list
		 * @return String []
		 */
		public static String[] getTermgroups() {
			RxToolkit.trace("MEMEToolkit.DBToolkit::getTermgroups()");

			if (cache.get(TERMGROUP) != null) {
				return (String[]) cache.get(TERMGROUP);
			}
			;

			String[] all_termgroups;
			ArrayList termgroups_from_db = new ArrayList();

			try {
				getConnectionAndStatement();
				results = statement
						.executeQuery("select termgroup from termgroup_rank order by 1");
				while (results.next()) {
					termgroups_from_db.add(results.getString(TERMGROUP));
				}
			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception: \n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new String[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Exception while reading termgroups from mid\n:"
						+ e2);
				return new String[] {};
			}
			;

			// Convert to string array
			Iterator iter = termgroups_from_db.iterator();
			all_termgroups = new String[termgroups_from_db.size()];

			int index = 0;

			while (iter.hasNext()) {
				all_termgroups[index++] = (String) iter.next();
			}

			cache.put(TERMGROUP, all_termgroups);
			return all_termgroups;
		}

		/**
		 * This method looks up semantic types in the database
		 * @return String []
		 */
		public static String[] getSemanticTypes() {
			RxToolkit.trace("MEMEToolkit.DBToolkit::getSemanticTypes()");
			if (cache.get(STY) != null) {
				return (String[]) cache.get(STY);
			}

			ArrayList sty_from_db = new ArrayList();
			try {
				getConnectionAndStatement();
				results = statement
						.executeQuery("select stn, sty from srsty order by 1");
				while (results.next()) {
					String sty = results.getString("STY");
					String stn = results.getString("STN");
					sty_from_db.add(stn + " " + sty);
				}
			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception: \n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new String[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Exception while reading stys from mid\n:" + e2);
				return new String[] {};
			}
			;

			// Convert to string array
			String[] all_sty = RxToolkit.toStringArray(sty_from_db.toArray());

			cache.put(STY, all_sty);
			return all_sty;

		}

		/**
		 * This method gets the valid tobereleased values
		 * @return String []
		 */
		public static String[] getTobereleased() {
			String[] tbr;
			ArrayList db_tbr = new ArrayList();
			try {
				getConnectionAndStatement();
				results = statement
						.executeQuery("select tobereleased from tobereleased_rank order by 1 desc");
				while (results.next()) {
					db_tbr.add(results.getString("TOBERELEASED"));
				}

			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception: \n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new String[] {};
			} catch (Exception e2) {
				RxToolkit.reportError("Exception while reading termgroups from mid\n:"
						+ e2);
				return new String[] {};
			}
			;

			// Return as String []
			Iterator iter = db_tbr.iterator();
			int index = 0;
			tbr = new String[db_tbr.size()];
			while (iter.hasNext()) {
				tbr[index++] = (String) iter.next();
			}

			return tbr;
		}

		/**
		 * This method gets the valid status values for classes
		 * @return String []
		 */
		public static String[] getStatus() {

			if (cache.get(STATUS) != null)
				return (String[]) cache.get(STATUS);

			String[] status;
			ArrayList db_status = new ArrayList();
			try {
				getConnectionAndStatement();
				results = statement
						.executeQuery("select distinct status from level_status_rank where table_name='C' order by 1");
				while (results.next()) {
					db_status.add(results.getString("STATUS"));
				}

			} catch (SQLException e1) {
				RxToolkit.reportError("SQL exception: \n" + e1.getMessage() + "("
						+ e1.getErrorCode() + ")");
				return new String[] {};
			} catch (Exception e2) {
				RxToolkit
						.reportError("Exception while reading status from mid\n:" + e2);
				return new String[] {};
			}
			;

			// Return as String []
			Iterator iter = db_status.iterator();
			int index = 0;
			status = new String[db_status.size()];
			while (iter.hasNext()) {
				status[index++] = (String) iter.next();
			}

			cache.put(STATUS, status);
			return status;
		}

	} // end class DBToolkit.java

	//
	// reference to writer
	//
	protected static RxWriter writer = null;

	//
	// reference to current recipe
	//
	protected static Recipe recipe = null;

	//
	// DBToolkit fields
	//
	private static Map cache = new HashMap();

	private static Map sab_to_rsab = new HashMap();

	private static String location;

	/**
	 * This default initialize method creates the java.sql.Connection and it loads
	 * the default property file, and it sets the printwriter to System.out unless
	 * there is a logfile.
	 */
	public static void initialize() {
		RxToolkit.trace("RxToolkit::initialize()");

		// Cache data
		RxToolkit.trace("RxToolkit::initialize() - cacheing data");
		getMergeSetsFromFile();
		getSources();
		getTermgroups();
		RxToolkit.trace("RxToolkit::initialize() - done cacheing data");
	}

	//
	// Accessor methods
	//

	/**
	 * Method to set the RxWriter
	 * @param writer RxWriter
	 */
	public static void setRxWriter(RxWriter writer) {
		RxToolkit.writer = writer;
	}

	/**
	 * Method to set the recipe
	 * @param recipe Recipe
	 */
	public static void setRecipe(Recipe recipe) {
		RxToolkit.recipe = recipe;
	}

	/**
	 * Accessor method to provide access to writer
	 * @return RxWriter
	 */
	public static RxWriter getRxWriter() {
		return writer;
	}

	/**
	 * Accessor method to provide access to recipe
	 * @return Recipe
	 */
	public static Recipe getRecipe() {
		return recipe;
	}

	//
	// Local DxToolkit methods
	//

	/**
	 * This method gets source/termgroup values from the ranks files param mode
	 * String
	 * @return String []
	 */
	public static String[] getFromRanksFile(String mode) throws Exception {
		RxToolkit.trace("RxToolkit::getFromRanksFile(" + mode + ")");

		setLocation();

		// if location is null, bail
		if (location == null)
			return new String[] {};

		BufferedReader fin = new BufferedReader(new FileReader(location
				+ "/termgroups.src"));

		String line;
		StringTokenizer st;
		java.util.ArrayList results = new java.util.ArrayList();

		// get sources from sources.src or termgroups.src
		if (mode.equals(RxConstants.SOURCE)) {

			// check if sources.src exists
			File file = new File(location + "/sources.src");
			if (file.exists()) {
				fin = new BufferedReader(new FileReader(file));
				while ((line = fin.readLine()) != null) {
					String[] tokens = FieldedStringTokenizer.split(line, "|");
					results.add(tokens[0]);
					sab_to_rsab.put(tokens[0], tokens[4]);
				}
				;

			} else {
				// otherwise read from termgroups.src
				fin = new BufferedReader(new FileReader(location + "/termgroups.src"));

				while ((line = fin.readLine()) != null) {
					st = new StringTokenizer(line, "/");
					results.add(st.nextToken());
				}
				;
			}
		} else if (mode.equals(RxConstants.TERMGROUP)) {
			while ((line = fin.readLine()) != null) {
				st = new StringTokenizer(line, "|");
				results.add(st.nextToken());
			}
			;

		} else {
			throw new Exception("Invalid mode: " + mode);
		}
		;

		fin.close();

		// Unique the results
		java.util.HashSet uniq_results = new java.util.HashSet(results);

		// Sort the results
		java.util.TreeSet sorted_results = new java.util.TreeSet(uniq_results);

		// Convert to String []
		Iterator iter = sorted_results.iterator();
		String[] final_results = new String[sorted_results.size()];
		int index = 0;
		while (iter.hasNext()) {
			final_results[index++] = (String) iter.next();
		}
		return final_results;
	}

	/**
	 * This method gets merge set values from the mergefacts.src file
	 * @param String mode
	 * @return String []
	 */
	public static String[] getMergeSetsFromFile() {
		RxToolkit.trace("RxToolkit::getMergeSetsFromFile()");

		if (cache.get(RxConstants.MERGE_SET) != null) {
			return (String[]) cache.get(RxConstants.MERGE_SET);
		}
		;

		setLocation();

		// if location is null, bail
		if (location == null)
			return new String[] {};

		// if mergefacts.src does not exist, bail
		if (!(new File(location + "/" + RxConstants.MERGEFACTS_SRC).exists())) {
			return new String[] {};
		}
		;

		BufferedReader fin;
		try {
			fin = new BufferedReader(new FileReader(location + "/"
					+ RxConstants.MERGEFACTS_SRC));
		} catch (IOException e) {
			RxToolkit.reportError("Error opening mergefacts file: " + e);
			RxToolkit.trace("RxToolkit::getMergeSetsFromFile (1)");
			return new String[] {};
		}
		;

		String line;
		int delim_place;
		java.util.ArrayList results = new java.util.ArrayList();

		String old_set = "";
		String current_set = "";

		try {
			while ((line = fin.readLine()) != null) {
				// Get 8th field for MEME3 format mergefacts.src
				for (int x = 0; x <= 6; x++) {
					delim_place = line.indexOf('|');
					current_set = line.substring(delim_place + 1);
					line = current_set;
				}
				delim_place = line.indexOf('|');
				current_set = line.substring(0, delim_place);
				line = current_set;
				// MEMEToolkit.trace("line" + line);

				if (!current_set.equals(old_set))
					results.add(current_set);
			}
			;
		} catch (IOException e) {
			RxToolkit.reportError("Error reading from mergefacts file: " + e);
			RxToolkit.trace("RxToolkit::getMergeSetsFromFile (2)");
			return new String[] {};
		}
		;

		try {
			fin.close();
		} catch (IOException e) {
			RxToolkit.reportError("Error closing mergefacts file: " + e);
			RxToolkit.trace("RxToolkit::getMergeSetsFromFile (3)");
			return new String[] {};
		}
		;

		// Unique the results
		java.util.HashSet uniq_results = new java.util.HashSet(results);

		// Sort the results
		java.util.TreeSet sorted_results = new java.util.TreeSet(uniq_results);

		// Convert to String []
		Iterator iter = sorted_results.iterator();
		String[] final_results = new String[sorted_results.size()];
		int index = 0;
		while (iter.hasNext()) {
			final_results[index++] = (String) iter.next();
		}

		// Cache results
		cache.put(RxConstants.MERGE_SET, final_results);

		return final_results;
	}

	/**
	 * This method looks up sources in the database or just returns a standard
	 * list. It also looks up sources in the ranks.src file.
	 * @return String []
	 */
	public static String[] getSources() {
		RxToolkit.trace("RxToolkit::getSources()");

		//
		// Look in cache first
		//
		if (cache.containsKey(RxConstants.SOURCE))
			return (String[]) cache.get(RxConstants.SOURCE);

		String[] sources_from_file;
		String[] sources_from_db;
		String[] all_sources = null;
		try {
			sources_from_file = getFromRanksFile(RxConstants.SOURCE);
		} catch (Exception e) {
			e.printStackTrace();
			RxToolkit.reportError("Exception while getting new sources:\n" + e);
			return new String[] {};
		}
		;

		// Convert to string array
		sources_from_db = RxToolkit.DBToolkit.getSources();

		all_sources = new String[sources_from_db.length + sources_from_file.length];

		// Add sources from file & db to all_sources
		System.arraycopy(sources_from_file, 0, all_sources, 0,
				sources_from_file.length);
		System.arraycopy(sources_from_db, 0, all_sources, sources_from_file.length,
				sources_from_db.length);

		cache.put(RxConstants.SOURCE, all_sources);
		return all_sources;

	}

	/**
	 * This method looks up termgroups in the database or just returns a standard
	 * list
	 * @return String []
	 */
	public static String[] getTermgroups() {
		RxToolkit.trace("RxToolkit::getTermgroups()");

		if (cache.get(RxConstants.TERMGROUP) != null) {
			return (String[]) cache.get(RxConstants.TERMGROUP);
		}
		;

		String[] termgroups_from_file;
		String[] all_termgroups;
		String[] termgroups_from_db;
		try {
			termgroups_from_file = getFromRanksFile(RxConstants.TERMGROUP);
		} catch (Exception e) {
			RxToolkit.reportError("Exception while getting new termgroups:\n" + e);
			return new String[] {};
		}
		;

		termgroups_from_db = RxToolkit.DBToolkit.getTermgroups();

		all_termgroups = new String[termgroups_from_db.length
				+ termgroups_from_file.length];

		// Add termgroups from file & db to all_termgroups
		System.arraycopy(termgroups_from_file, 0, all_termgroups, 0,
				termgroups_from_file.length);
		System.arraycopy(termgroups_from_db, 0, all_termgroups,
				termgroups_from_file.length, termgroups_from_db.length);

		cache.put(RxConstants.TERMGROUP, all_termgroups);
		return all_termgroups;
	}

	/**
	 * Accessor method to set the location value
	 * @param location String
	 */
	public static void setLocation(String loc) {
		RxToolkit.trace("RxToolkit::setLocation(" + loc + ")");
		location = loc;
	}

	/**
	 * Accessor method to set the location value from the environment or from a
	 * user choice
	 * @param location String
	 */
	public static void setLocation() {
		RxToolkit.trace("RxToolkit::setLocation()");

		// if not set get it from the toolkit
		if (location == null || location.equals("")) {
			location = RxToolkit.getProperty(RxConstants.SRC_DIRECTORY);
		}
		// if still not set, get it from the suer.
		if (location.equals("")) {
			File loc_file = RxToolkit.chooseFile(
					"Find the directory containing the .src files", "OK",
					JFileChooser.DIRECTORIES_ONLY, new File(""));
			if (loc_file == null) {
				location = null;
			}
			location = loc_file.getPath();
		}
	}

	/**
	 * Return the root source name for the specified source name.
	 * @param sab the source name
	 * @return the root source name for the specified source name
	 */
	public static String getRootSourceName(String sab) {
		return (String) sab_to_rsab.get(sab);
	}

} // end class MEMEToolkit.java
