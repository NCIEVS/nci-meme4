/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme
 * Object:  MEMEToolkit
 * 
 * Changes:
 *   03/24/2006 RBE (1-AQRCB): use MID Services mailing list
 *  
 *  Modified: Soma Lanka: Added a method logXmlComment to log the XML request/response
 *****************************************************************************/

package gov.nih.nlm.meme;

import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MidsvcsException;
import gov.nih.nlm.meme.server.Statistics;
import gov.nih.nlm.swing.SwingToolkit;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;

import java.awt.Component;

/**
 * Utility class that has generic methods that the server/client can
 * use.  All of the methods of the toolkit are static which means that the
 * toolkit should never be directly instantiated.  Instead, it has initialize
 * methods that initialize a set of internal properties and a log.
 * Applications using the toolkit should pass in a set of application specific
 * allowed and required properties.
 * 
 * @author MEME Group
 */
public abstract class MEMEToolkit extends SwingToolkit implements MEMEConstants {

  //
  // Fields
  //

  private static PrintWriter log = null;
  private static PrintWriter xmlLog = null;
  private static HashMap timetable = new HashMap();
  private static String dateformat = "dd-MMM-yyyy HH:mm:ss";

  /**
   * Initialize the toolkit properties and log.  The toolkit is not directly
   * constructed, instead all of its methods are accessed in a static way, so
   * it must be initialized.
   *
   * This calls {@link #initializeProperties(String,String[],String[])} and
   * passes it the property file name specified in
   * {@link MEMEConstants#PROPERTIES_FILE}, then it initializes the log from
   * the {@link MEMEConstants#LOG_FILE} property.  This initialize method
   * loads properties, especially a whole property file, over the command line
   * and sets the properties of the application if their keys are included in
   * <code>app_specific_properties</code> or {@link MEMEConstants#ALLOWABLE_PROPERTIES}. It
   * sets the {@link PrintWriter} to STDOUT unless the log file property is set.
   * @param app_specific_properties A {@link String}<code>[]</code>
   * containing application specific property names to initialize
   * @param app_specific_req_props A {@link String}<code>[]</code>
   * containing application specific require properties
   * @throws InitializationException if initialization failed.
   */
  public static void initialize(String[] app_specific_properties,
                                String[] app_specific_req_props) throws
      InitializationException {
    // Initialize the MEMEToolkit.properties
    initializeProperties(System.getProperty(PROPERTIES_FILE),
                         app_specific_properties, app_specific_req_props);
    trace("Using View? " + usingView());
    // Open the PrintWriter for MEMEToolkit.log
    initializeLog();
  }

  /**
   * Initialize the toolkit properties and log.  The toolkit is not directly
   * constructed, instead all of its methods are accessed in a static way, so
   * it must be initialized.
   *
   * This calls {@link #initializeProperties(Properties,String[],String[])}.
   * This sets the properties of the application if their keys are included in
   * <code>app_specific_properties</code> or {@link MEMEConstants#ALLOWABLE_PROPERTIES}. It
       * sets the {@link PrintWriter} to STDOUT unless the log file property is set.
   * @param props An object {@link Properties}
   * @param app_specific_properties A {@link String}<code>[]</code>
   * containing application specific property names to initialize
   * @param app_specific_req_props A {@link String}<code>[]</code>
   * containing application specific require properties
   * @throws InitializationException if initialization failed.
   */
  public static void initialize(Properties props,
                                String[] app_specific_properties,
                                String[] app_specific_req_props) throws
      InitializationException {

    // Initialize the MEMEToolkit.properties
    initializeProperties(props,
                         app_specific_properties, app_specific_req_props);
    trace("Using View? " + usingView());
    // Open the PrintWriter for MEMEToolkit.log
    initializeLog();
  }

  /**
   * Initialize the toolkit with <code>null</code>
   * application specific properties.
   * @throws InitializationException if initialization failed.
   */
  public static void initialize() throws InitializationException {
    initialize(null, null);
  }

  /**
   * Initializes properties mentioned in
   * {@link MEMEConstants#ALLOWABLE_PROPERTIES}, in the array
   * <code>app_specific_prop</code>, and any system properties
   * starting with <code>meme.app</code>.
   * Checks afterwards if the required properties
   * in {@link MEMEConstants#REQUIRED_PROPERTIES} and in the array
   * app_specific_req_props are set, Exits otherwise.
   * @param properties_file the name of a valid java properties file
   * @param app_specific_properties a {@link String}<code>[]</code>
   * containing application specific property names to initialize
   * @param app_specific_req_props a {@link String}<code>[]</code>
   * containing application specific require properties
   * @throws InitializationException if failed to initialize properties
   */
  public static void initializeProperties(String properties_file,
                                          String[] app_specific_properties,
                                          String[] app_specific_req_props) throws
      InitializationException {

    // If the properties file is null or ""
    // then just create an empty properties object
    // and read properties only from the System properties
    Properties load_props = null;
    if (properties_file == null || properties_file.equals("")) {
      load_props = new Properties();

      // If there is a properties file, open it and load properties
    } else {
      load_props = new Properties();
      try {
        load_props.load(new FileInputStream(properties_file));
      } catch (Exception e) {
        InitializationException ie = new InitializationException(
            "Problem loading properties file.", e);
        ie.setDetail("file_name", properties_file);
        throw ie;
      }
    }
    ;

    initializeProperties(load_props, app_specific_properties,
                         app_specific_req_props);

  }

  /**
   * Initializes properties mentioned in
   * {@link MEMEConstants#ALLOWABLE_PROPERTIES}, in the array
   * <code>app_specific_prop</code>, and any system properties
   * starting with <code>meme.app</code>
   * Checks afterwards if the required properties
   * in {@link MEMEConstants#REQUIRED_PROPERTIES} and in the array
   * app_specific_req_props are set, Exits otherwise.
   * @param load_props an object {@link Properties}
   * @param app_specific_properties a {@link String}<code>[]</code>
   * containing application specific property names to initialize
   * @param app_specific_req_props a {@link String}<code>[]</code>
   * containing application specific require properties
   * @throws InitializationException if failed to initialize properties
   */
  public static void initializeProperties(Properties load_props,
                                          String[] app_specific_properties,
                                          String[] app_specific_req_props) throws
      InitializationException {

    //
    // Initialize properties from env.ENV_FILE if set
    //
    if (load_props.getProperty(ENV_FILE) != null ||
        System.getProperty(ENV_FILE) != null) {
      try {
        final Properties env_props = new Properties();
        // let system prop override file prop
        String prop_file = System.getProperty(ENV_FILE);
        if (prop_file == null) prop_file = load_props.getProperty(ENV_FILE);
        logComment("Using Environment File = " + prop_file,true);
        env_props.load(new FileInputStream(new File(prop_file)));
        final Iterator iter = env_props.keySet().iterator();
        while (iter.hasNext()) {
          final String key = (String)iter.next();
          final String value = env_props.getProperty(key);
          // Set env.* property
          load_props.setProperty("env."+key,value);
          logComment("Environment Setting " + key + " = " + value,true);
        }
      } catch (Exception ioe) {
        throw new InitializationException(
            "Error initializing from environment file", ioe);
      }
    }

    // Override the property file properties with system ones
    // where they match the allowable properties list.
    String name, value;
    for (int i = 0; i < ALLOWABLE_PROPERTIES.length; i++) {
      name = ALLOWABLE_PROPERTIES[i];
      value = System.getProperty(name);
      if (value != null) {
        load_props.setProperty(name, value);
      } else if (load_props.getProperty(name) == null) {
        // If property is not used use empty instead of null value
        load_props.setProperty(name, "");
      }
    }

    // Override properties file with system properties
    // where they match app specific allowable properties
    if (app_specific_properties != null) {
      trace("MEMEToolkit::initializeProperties - app specific properties size:" +
            app_specific_properties.length);
      for (int i = 0; i < app_specific_properties.length; i++) {
        name = app_specific_properties[i];
        value = System.getProperty(name);
        if (value != null) {
          load_props.put(name, value);
        } else if (load_props.getProperty(name) == null) {
          // If property is not used use empty instead of null value
          load_props.put(name, "");
        }
      }

    } else {
      trace(
          "MEMEToolkit::initializeProperties - app specific properties are null");
    }

    // Sort the properties.
    TreeSet sortednames = new TreeSet(load_props.keySet());
    Iterator iprop = sortednames.iterator();
    while (iprop.hasNext()) {
      name = (String) iprop.next();
      SwingToolkit.setProperty(name, load_props.getProperty(name));
    }

    // Check required properties
    // First against the local required properties list
    trace("MEMEToolkit::initializeProperties - check required properties");
    for (int i = 0; i < REQUIRED_PROPERTIES.length; i++) {
      String req_prop =
          getProperty(REQUIRED_PROPERTIES[i]);
      if (req_prop == null || req_prop.equals("")) {
        InitializationException ie = new InitializationException(
            "Required property is missing.");
        ie.setDetail("property", REQUIRED_PROPERTIES[i]);
        throw ie;
      } else {
        trace("\tRequired property " +
              REQUIRED_PROPERTIES[i] + " = " +
              getProperty(REQUIRED_PROPERTIES[i]));
        // why is this here?: Exit(0);
      }
    }

    // Check application specific required properties
    trace("MEMEToolkit::initializeProperties - " +
          "check application specific required properties");
    if (app_specific_req_props != null) {
      for (int i = 0; i < app_specific_req_props.length; i++) {
        String req_prop = getProperty(app_specific_req_props[i]);
        if (req_prop == null || req_prop.equals("")) {
          InitializationException ie = new InitializationException(
              "Application specific required property is missing.");
          ie.setDetail("property", app_specific_req_props[i]);
          throw ie;
        } else {
          trace("\tRequired property " + app_specific_req_props[i] + " = " +
                getProperty(app_specific_req_props[i]));
        }
      }
    }
  }

  /**
   * Initializes the internal {@link PrintWriter} representing the
   * log. If the log file already exists, the user is asked if (s)he wishes to
   * overwrite it.  If the specified logfile is null or "" then log to
   * STDOUT.
   * To specify an actual log file set the {@link MEMEConstants#MEME_HOME}
   * property to the <code>$MEME_HOME</code> directory, and then set the
   * {@link MEMEConstants#LOG_FILE} property to a relative path to the desired
   * log file, where the path is relative to <code>$MEME_HOME</code>.
   */
  public static void initializeLog() {
    String meme_home = getProperty(MEME_HOME);
    String log_file = getProperty(LOG_FILE);
    String logfile = meme_home + "/" + log_file;

    trace("MEMEToolkit::initializeLog(" + logfile + ")");
    PrintWriter tmp_log = null;

    // Proceed if the logfile is set
    if (log_file != null && !log_file.equals("") &&
        meme_home != null && !meme_home.equals("")) {
      try {
        boolean write_file = true;
        File f = new File(logfile);
        if (f.exists()) {
          write_file = confirmRequest(
              "You are about to overwrite the logfile:\n'" +
              logfile + "'. \n" +
              "Do you really want to do this.");
        }
        if (write_file) {
          tmp_log = new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(logfile), "UTF-8")));
        } else {
          notifyUser("Logfile already exists, logging to standard out.");
          tmp_log = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
              System.out, "UTF-8")));
        }
      } catch (Exception e) {
        // Report error, do not fail
        InitializationException ie = new InitializationException(
            "Problem trying to create log file.", e);
        ie.setDetail("log_file", log_file);
        handleError(ie);
      }
    } else {
      try {
        tmp_log = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
            System.out, "UTF-8")));
      } catch (Exception uee) {
        handleError(uee);
      }
    }
    log = tmp_log;
  }

  //
  // Accessor Methods
  //

  /**
   * Sets the log to the specified {@link PrintWriter}.
   * @param newlog a {@link PrintWriter} for the new log
   */
  public static void setLog(PrintWriter newlog) {
    log = newlog;
  }
/*
 * Soma Changed:  Adding a new log file just to log the XMLS for debugging.
 * 
 */
  public static void setXMLLog(PrintWriter newlog) {
	  xmlLog = newlog;
  }
  /**
   * Sets a property name.  If the property being set is the
   * log file name, the log is re-initialized.
   * @param name the property name
   * @param value the property value
   */
  public static void setProperty(String name, String value) {
    SwingToolkit.setProperty(name, value);
    // If logfile is set, close existing logfile open another one
    if (name.equals(LOG_FILE)) {
      initializeLog();
    }
  }

  /**
   * Returns the {@link PrintWriter} log.
   * @return the {@link PrintWriter} log
   */
  public static PrintWriter getLog() {
    return log;
  }

  /**
   * Returns a standard {@link SimpleDateFormat}.
   * @return a standard {@link SimpleDateFormat}
   */
  public static SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat(dateformat);
  }

  /**
   * Returns a URI for the directory containing the DTD. Used by XML parsers
   * to locate the relevant DTDs.
   * @return a URI for the directory containing the DTD
   */
  public static String getSystemId() {
    MEMEToolkit.trace("MEMEToolkit::getSystemId() - file://"
                      + getProperty(MEME_HOME) + "/"
                      + getProperty(DTD_DIRECTORY));
    return "file://" + getProperty(MEME_HOME) + "/"
        + getProperty(DTD_DIRECTORY);
  }

  /**
   * Returns a flag indiciating whether "debugging" mode is enabled.
   * @return <code>true</code> if debugging mode is on,
   *         <code>false</code> otherwise
   */
  public static boolean debugging() {
    String debug = getProperty(DEBUG);
    if (debug == null) {
      return false;
    } else {
      return Boolean.valueOf(debug).booleanValue();
    }
  }

  /**
   * Returns a flag indiciating whether the current application is
   * using GUI components.
   * @return <code>true</code> if the current application is using GUI
   *         components, <code>false</code> otherwise
   */
  public static boolean usingView() {
    return Boolean.valueOf(getProperty(SwingToolkit.VIEW)).booleanValue();
  }

  /**
   * Reports an error with the specified message.
   * @param error the error message
   * @see #reportError(Component,String,boolean)
   */
  public static void reportError(String error) {
    reportError(null, error, false);
  }

  /**
   * Reports an error with the specified message and a flag indicating
   * whether or not the error is fatal.
   * @param error the error message
   * @param fatal a flag indicating whether or not this is a fatal error
   * @see #reportError(Component,String,boolean)
   */
  public static void reportError(String error, boolean fatal) {
    reportError(null, error, fatal);
  }

  /**
   * Reports an error to the user.
   * @param parent the {@link Component} that the error message dialog
   *        should be opened relative to
   * @param error the error message
   * @param fatal a flag indicating whether or not this is a fatal error
   * @see SwingToolkit#reportError(Component, String, boolean)
   */
  public static void reportError(Component parent, String error, boolean fatal) {
    // if view is open, open a dialog box
    logComment("ERROR - " + error, true);
    if (fatal) {
      logComment("   Error is FATAL.");
      error = error + " Exiting . . .";
    }
    ;
    SwingToolkit.reportError(parent, error, fatal);
  }

  /**
   * Asks the user a "yes/no" question.
   * @param request the question
   * @return <code>true</code> if the user answered in the affirmative,
   *         <code>false</code> otherwise
   * @see #confirmRequest(Component,String)
   */
  public static boolean confirmRequest(String request) {
    return confirmRequest(null, request);
  }

  /**
   * Asks the user a "yes/no" question.
   * @param request the question
   * @param parent the {@link Component} that the error message dialog
   *        should be opened relative to
   * @return <code>true</code> if the user answered in the affirmative,
   *         <code>false</code> otherwise
   * @see SwingToolkit#confirmRequest(Component,String)
   */
  public static boolean confirmRequest(Component parent, String request) {
    logComment("CONFIRM REQUEST - " + request + "?", true);
    boolean result = SwingToolkit.confirmRequest(parent, request);
    logComment("   Response is " + result);
    return result;
  }

  /**
   * Informs the user.
   * @param message a message
   * @see #notifyUser(Component,String)
   */
  public static void notifyUser(String message) {
    notifyUser(null, message);
  }

  /**
   * Informs the user.
   * @param parent the {@link Component} that the error message dialog
   *        should be opened relative to
   * @param message a message
   * @see SwingToolkit#notifyUser(Component,String)
   */
  public static void notifyUser(Component parent, String message) {
    logComment("NOTIFY USER - " + message, true);
    SwingToolkit.notifyUser(parent, message);
  }

  /**
   * Requests user input.
   * @param request the request for input
   * @return An input from user.
   * @see #getUserInput(Component,String)
   */
  public static String getUserInput(String request) {
    return getUserInput(null, request);
  }

  /**
   * Requests user input.
   * @param parent the {@link Component} that the error message dialog
   *        should be opened relative to
   * @param message the request for input
   * @return An input from user.
   * @see SwingToolkit#getUserInput(Component,String)
   */
  public static String getUserInput(Component parent, String message) {
    logComment("REQUEST USER INPUT - " + message, true);
    return SwingToolkit.getUserInput(parent, message);
  }

  /**
   * Requests multi-line input from the user.
   * @param request the request for input
   * @return A multi-line input from user.
   * @see #getMultiLineUserInput(Component,String)
   */
  public static String getMultiLineUserInput(String request) {
    return getMultiLineUserInput(null, request);
  }

  /**
   * Requests multi-line input from the user.
   * @param parent the {@link Component} that the error message dialog
   *        should be opened relative to
   * @param message the request for input
   * @return A multi-line input from user.
   * @see SwingToolkit#getMultiLineUserInput(Component,String)
   */
  public static String getMultiLineUserInput(Component parent, String message) {
    logComment("REQUEST USER INPUT (type 'done' when finished) - " +
               message, true);
    return SwingToolkit.getMultiLineUserInput(parent, message);
  }

  /**
   * Writes a message to STDERR if {@link #debugging()} returns <code>true</code>.
   * @param comment the message to write
   */
  public static void trace(String comment) {
    trace(comment, false);
  }

  /**
   * Writes a message to STDERR if {@link #debugging()} returns <code>true</code>
   * and (optionally) print out the time the comment was written.
   * @param comment the message to write
   * @param print_date a flag indicating whether or not the date should be
   *       prepended to the comment
   */
  public static void trace(String comment, boolean print_date) {
    if (debugging()) {
      if (print_date) {
        Date now = new Date();
        System.err.println(comment + "(" + now + ")");
      } else {
        System.err.println(comment);
      }
      System.err.flush();
    }
  }

  /**
   * Writes specified comment to the log.
   * @param comment the comment
   */
  public static void logComment(String comment) {
    logComment(comment, false);
  }

  /**
   * Writes specified comment to the log and (optionally) prepends the current date.
   * If the comment is multi-line, the date is prepended at the beginning of
   * each of the lines.
   * @param comment the comment
   * @param print_date a flag indicating whether or not to prepend the date
   */
  public static void logComment(String comment, boolean print_date) {
    StringTokenizer st = new StringTokenizer(comment, "\n\r");
    while (st.hasMoreTokens()) {
      String str = "\t" + st.nextToken();
      if (print_date) {
        Date now = new Date();
        str = "[" + getDateFormat().format(now) + "] " + str;
      }
      if (debugging()) {
        trace(str);
      }
      if (log == null) {
        System.out.println(str);
      } else {
        log.println(str);
        log.flush();
      }
    }
  }
  /* 
   * Soma Lanka: Adding a new method to log the XML strings.
   */
public static void logXmlComment(String pre, String comment){
	if (xmlLog != null && getProperty("meme.xml.log.enabled") != null && getProperty("meme.xml.log.enabled").equals("true") ) {
		Date now = new Date();
        pre = "[" + getDateFormat().format(now) + "] " + pre;
        xmlLog.println(pre);
        xmlLog.flush();
      	StringTokenizer st = new StringTokenizer(comment, "\n\r");
	    while (st.hasMoreTokens()) {
	      String str = "\t" + st.nextToken();
	          xmlLog.println(str);
	          xmlLog.flush();
	    }
	}
}
  /**
   * Writes specified comment to the specified log and (optionally) prepends
   * the current date to the beginning of each comment line.
   * @param comment the comment
   * @param print_date a flag indiciating whether or not to prepend the date
   * @param log a {@link StringBuffer} log
   */
  public static void logCommentToBuffer(
      String comment, boolean print_date, StringBuffer log) {
    StringTokenizer st = new StringTokenizer(comment, "\n\r");
    while (st.hasMoreTokens()) {
      if (print_date) {
        Date now = new Date();
        log.append("[").append(getDateFormat().format(now)).append("] ");
      }
      log.append("\t").append(st.nextToken()).append("\n");
    }
  }

  /**
   * Hashes the current time using the specified key, and writes a comment
   * to the log.  Generally, this method is used in conjunction with
   * {@link #logElapsedTime(String)}.
   * @param key the name of an operation whose elapsed time is to be measured
   */
  public static void logStartTime(String key) {
    if (log == null) {
      return;
    }
    Date now = new Date();
    timetable.put(key, now);
    String str = "\n" + key + ": " + now + "\n";
    log.println(str);
    log.flush();
    trace(str);
  }

  /**
   * Writes the elapsed time of an operation to the log.  This should be used
   * in conjunction with {@link #logStartTime(String)} to write both the
   * start and elapsed times of an operation to the log.  Do NOT call this
   * method without first establishing a start time.
   * @param key the name of an operation whose elapsed time is to be measured
   */
  public static void logElapsedTime(String key) {
    if (log == null) {
      return;
    }
    Date now = new Date();
    Date then = (Date) (timetable.get(key));
    if (then == null) {
      BadValueException bve = new BadValueException("No start time logged.");
      bve.setDetail("key", key);
      handleError(bve);
    }
    String str = "\n" + "Elapsed time for '" + key + "': " +
        timeToString(timeDifference(now, then));
    log.println(str);
    timetable.remove(key);
    log.flush();
    trace(str);
  }

  /**
   * Returns the elapsed time for an operation as a {@link String}.  This method
   * is much like {@link #logElapsedTime(String)} except that the elapsed time
   * is returned instead of written to the log.
   * @param key the name of an operation whose elapsed time is to be measured
   * @return a {@link String} representation of the elapsed time
   */
  public static String getElapsedTime(String key) {
    return timeToString(timeDifference(new Date(), (Date) timetable.get(key)));
  }

  /**
   * Converts a time (in milliseconds) to a string representation with
   * the format <code>HH:MM:SS</code>.
   * @param time a <code>long</code> number of milliseconds
   * @return a {@link String} representation of that time
   */
  public static String timeToString(long time) {
    if (time < 1000) {
      return time + " ms";
    } else {
      int hours = (int) (time / 3600000);
      time = time % 3600000;
      int minutes = (int) (time / 60000);
      time = time % 60000;
      int seconds = (int) (time / 1000);

      String h = "0" + hours;
      String m = "0" + minutes;
      String s = "0" + seconds;
      return h.substring(h.length() - 2) + ":" +
          m.substring(m.length() - 2) + ":" +
          s.substring(s.length() - 2);
    }
  }

  /**
   * Returns the time difference between the specified start and end dates
   * in milliseconds.
   * @param now the end date
   * @param then the start date
   * @return a <code>long</code> number of milliseconds
   */
  public static long timeDifference(Date now, Date then) {
    return now.getTime() - then.getTime();
  }

  /**
   * Executes the specified operating system command.
   * @param command the command
   * @throws ExecException if command failed to execute
   * @return a {@link String} containing the process log
   */
  public static String exec(String command) throws ExecException {
    return exec(new String[] {command}
                , new String[] {}
                , true, USE_INPUT_STREAM, false, null);
  }

  /**
   * Executes the specified operating system command.
   * @param command a {@link String}<code>[]</code> where the first
   *        element is the command and remaining elements are arguments
   * @throws ExecException if command failed to execute
   * @return a {@link String} containing the process log
   */
  public static String exec(String[] command) throws ExecException {
    return exec(command, new String[] {}
                , true, USE_INPUT_STREAM, false, null);
  }

  /**
   * Executes the specified operating system command with the specified
   * environment.
   * @param command the command
   * @param env a {@link String}<code>[]</code> containing "NAME=VALUE" pairs
   *        of environment variable definitions
   * @throws ExecException if command failed to execute
   * @return a {@link String} containing the process log
   */
  public static String exec(String command, String[] env) throws ExecException {
    return exec(new String[] {command}
                , env, true, USE_INPUT_STREAM, false, null);
  }

  /**
   * Executes the specified operating system command with the specified
   * environment.
   * @param command the command
   * @param env a {@link String}<code>[]</code> containing "NAME=VALUE" pairs
   *        of environment variable definitions
   * @param dir the working directory of the subprocess
   * @throws ExecException if command failed to execute
   * @return a {@link String} containing the process log
   */
  public static String exec(String command, String[] env, File dir) throws
      ExecException {
    return exec(new String[] {command}
                , env, true, USE_INPUT_STREAM, false, dir);
  }

  /**
   * Executes the specified operating system command with the specified
   * environment.
   * @param command a {@link String}<code>[]</code> where the first
   *        element is the command and remaining elements are arguments
   * @param env a {@link String}<code>[]</code> containing "NAME=VALUE" pairs
   *        of environment variable definitions
   * @throws ExecException if command failed to execute
   * @return a {@link String} containing the process log
   */
  public static String exec(String command[], String[] env) throws
      ExecException {
    return exec(command, env, true, USE_INPUT_STREAM, false, null);
  }

  /**
   * Executes the specified operating system command with the specified
   * environment.
   * @param command a {@link String}<code>[]</code> where the first
   *        element is the command and remaining elements are arguments
   * @param env a {@link String}<code>[]</code> containing "NAME=VALUE" pairs
   *        of environment variable definitions
   * @param dir the working directory of the subprocess
   * @throws ExecException if command failed to execute
   * @return a {@link String} containing the process log
   */
  public static String exec(String command[], String[] env, File dir) throws
      ExecException {
    return exec(command, env, true, USE_INPUT_STREAM, false, dir);
  }

  /**
   * Executes the specified operating system command with the specified
   * environment.
   * @param command a {@link String}<code>[]</code> where the first
   *        element is the command and remaining elements are arguments
   * @param env a {@link String}<code>[]</code> containing "NAME=VALUE" pairs
   *        of environment variable definitions
   * @param dir the working directory of the subprocess
   * @param s <code>PrintWriter</code> to use for output
   * @throws ExecException if command failed to execute
   * @return a {@link String} containing the process log
   */
  public static String exec(String command[], String[] env, File dir,
                            PrintWriter s) throws ExecException {
    return exec(command, env, true, USE_INPUT_STREAM, false, dir, s);
  }

  /**
   * Executes an operating system command with more options.
       * This is the most flexible (and confusing) of the <code>exec</code> methods.
   * <p> It allows you to specify a command with parameters and a set of
   * environment variable definitions.  You may determine whether or not
   * the process should write to the application log, and if it does whether
   * it reads the processes STDOUT or STDERR.  Finally, you can choose to
   * run the process in the background.
   *
   * @param cmdarray a {@link String}<code>[]</code> where the first
   *        element is the command and remaining elements are arguments
   * @param env a {@link String}<code>[]</code> containing "NAME=VALUE" pairs
   *        of environment variable definitions
   * @param write_to_log a flag indicating whether or not this process should
   *        write to the server log
   * @param write_mode an <code>int</code> representing whether to connect
   *        to the process STDOUT or STDERR.  <code>-1</code> can be
   *        passed to connect to neither
   * @param background a flag indicating whether or not to run the process
   *        in the background.  It is recommended that if you set this
       *        to <code>true</code> then you should also set <code>write_mode</code>
   *        to {@link MEMEConstants#USE_NO_STREAM}
   * @throws ExecException if failed to execute command
   * @return a {@link String} containing the process log
   */
  public static String exec(String[] cmdarray, String[] env,
                            boolean write_to_log,
                            int write_mode, boolean background) throws
      ExecException {
    return exec(cmdarray, env, write_to_log, write_mode, background, null);
  }

  /**
   * Executes an operating system command with more options.
   * This is the most flexible (and confusing) of the <code>exec</code> methods.
   * <p> It allows you to specify a command with parameters and a set of
   * environment variable definitions.  You may determine whether or not
   * the process should write to the application log, and if it does whether
   * it reads the processes STDOUT or STDERR.  Finally, you can choose to
   * run the process in the background.
   *
   * @param cmdarray a {@link String}<code>[]</code> where the first
   *        element is the command and remaining elements are arguments
   * @param env a {@link String}<code>[]</code> containing "NAME=VALUE" pairs
   *        of environment variable definitions
   * @param write_to_log a flag indicating whether or not this process should
   *        write to the server log
   * @param write_mode an <code>int</code> representing whether to connect
   *        to the process STDOUT or STDERR.  <code>-1</code> can be
   *        passed to connect to neither
   * @param background a flag indicating whether or not to run the process
   *        in the background.  It is recommended that if you set this
   *        to <code>true</code> then you should also set <code>write_mode</code>
   *        to {@link MEMEConstants#USE_NO_STREAM}
   * @param dir the working directory of the subprocess
   * @throws ExecException if failed to execute command
   * @return a {@link String} containing the process log
   */
  public static String exec(String[] cmdarray, String[] env,
                            boolean write_to_log,
                            int write_mode, boolean background, File dir) throws
      ExecException {
    return exec(cmdarray, env, write_to_log, write_mode, background, dir, null);
  }

  /**
   * Executes an operating system command with more options.
   * This is the most flexible (and confusing) of the <code>exec</code> methods.
   * <p> It allows you to specify a command with parameters and a set of
   * environment variable definitions.  You may determine whether or not
   * the process should write to the application log, and if it does whether
   * it reads the processes STDOUT or STDERR.  Finally, you can choose to
   * run the process in the background.
   *
   * @param cmdarray a {@link String}<code>[]</code> where the first
   *        element is the command and remaining elements are arguments
   * @param env a {@link String}<code>[]</code> containing "NAME=VALUE" pairs
   *        of environment variable definitions
   * @param write_to_log a flag indicating whether or not this process should
   *        write to the server log
   * @param write_mode an <code>int</code> representing whether to connect
   *        to the process STDOUT or STDERR.  <code>-1</code> can be
   *        passed to connect to neither
   * @param background a flag indicating whether or not to run the process
   *        in the background.  It is recommended that if you set this
   *        to <code>true</code> then you should also set <code>write_mode</code>
   *        to {@link MEMEConstants#USE_NO_STREAM}
   * @param dir the working directory of the subprocess
   * @param s <code>PrintWriter</code> to use for output
   * @throws ExecException if failed to execute command
   * @return a {@link String} containing the process log
   */
  public static String exec(String[] cmdarray, String[] env,
                            boolean write_to_log,
                            int write_mode, boolean background, File dir,
                            PrintWriter s) throws ExecException {
    Runtime run = null;
    Process proc = null;
    StringBuffer output = new StringBuffer(1000);
    try {
      trace("MEMEToolkit::exec - " + cmdarray);
      String line;
      run = Runtime.getRuntime();
      String[] new_env = new String[2 + (env == null ? 0 : env.length)];
      new_env[0] = "ENV_FILE="+getProperty(ENV_FILE);
      new_env[1] = "ENV_HOME="+getProperty(ENV_HOME);
      if (env != null) {
      	int i = 2;
      	for (String e : env)
      		new_env[i++] = e;
      }
      proc = run.exec(cmdarray, new_env, dir);
      BufferedReader in = null;

      // If USE_NO_STREAM then do not connect a reader to the process
      if (write_mode != USE_NO_STREAM) {
        if (write_mode == USE_INPUT_STREAM) {
          InputStreamReader converter =
              new InputStreamReader(proc.getInputStream(), "UTF-8");
          in = new BufferedReader(converter);
        } else if (write_mode == USE_ERROR_STREAM) {
          InputStreamReader converter =
              new InputStreamReader(proc.getErrorStream());
          in = new BufferedReader(converter);
        } else {
          throw new ExecException("Illegal write mode to exec.");
        }
        while ( (line = in.readLine()) != null) {
          if (write_to_log) {
            log.println("\t" + line);
            log.flush();
          }
          if (s != null) {
            s.println(line);
            s.flush();
          }
          output.append(line).append(System.getProperty("line.separator"));
        }
      }

      // If we are not running in the background
      // then wait for the process to finish and track its exit value
      if (!background) {
        proc.waitFor();
        if (proc.exitValue() != 0) {
          // If there was an error, read from the error stream
          InputStreamReader converter =
              new InputStreamReader(proc.getErrorStream(), "UTF-8");
          in = new BufferedReader(converter);
          StringBuffer sb = new StringBuffer(1000);
          sb.append("\n--------------------------------------------\n");
          sb.append("Error:");
          while ( (line = in.readLine()) != null) {
            sb.append("\t" + line);
            sb.append("\n");
          }
          sb.append("--------------------------------------------\n");
          ExecException ee = new ExecException("Command failed.");
          StringBuffer cmdBuffer = new StringBuffer();
          for (String cmdarg : cmdarray)
          	cmdBuffer.append(cmdarg).append(" ");
          ee.setDetail("cmdarray", cmdBuffer.toString());
          ee.setDetail("exit_value", Integer.toString(proc.exitValue()));
          StringBuffer envBuffer = new StringBuffer();
          for (String envarg : new_env)
          	envBuffer.append(envarg).append(" ");
          ee.setDetail("env", envBuffer.toString());
          ee.setDetail("stderr", sb.toString());
          throw ee;
        }
      }

    } catch (ExecException ee) {
      throw ee;
    } catch (Exception e) {
      ExecException ee = new ExecException(
          "Attempt to execute command failed.", e);
      throw ee;
    }

    return output.toString();
  }

  /**
   * Returns {@link Object}'s unqualified class name.
   * For example: If the object is an instance of
   * <code>gov.nih.nlm.meme.MEMEToolkit</code>
   * getUnqualifiedClassName(object) gives the unqualified name
   * <code>MEMEToolkit</code> back.
   * @param object an {@link Object}.
   * @return a {@link String} representation of unqualified class name
   */
  public static String getUnqualifiedClassName(Object object) {
    String name = object.getClass().getName();
    int period = name.lastIndexOf('.');
    return name.substring(period + 1);
  }

  /**
   * Clean up resources and exit the application
   * @param return_value the exit value
   * @see SwingToolkit#Exit(int)
   */
  public static void Exit(int return_value) {
    if (return_value != 0) {
      trace("Exiting abnormally....");
    } else {
      trace("Exiting properly....");
    }
    SwingToolkit.Exit(return_value);
  }

  /**
   * Handle an exception. Here we wrap the generic exception into
   * a {@link MEMEException} wrapper and send it to be processed again.
   * @param e an {@link Exception}
   */
  public static void handleError(Exception e) {
    MEMEException me = new MEMEException("Non MEME exception.");
    me.setEnclosedException(e);
    handleError(me);
  }

  /**
   * Handle a {@link MEMEException}.  This method does a series of things
   * based on the configuration of the exception.  It does
   * the following things:
   * <ol>
   *   <li>Write the stack trace to the application log</li>
   *   <li>Inform the user (optionally)</li>
   *   <li>Inform the administrator via email (optionally)</li>
   *   <li>Add the exception to the statistics tracking</li>
   *   <li>Exit the application if fatal (optionally)</li>
   * </ol>
   * Whether or not these things are done depends upon the configuration
   * of the exception object itself.
   * @param me a {@link MEMEException}
   */
  public static void handleError(MEMEException me) {

    // Collect the stack trace(s) into a string.
    StringWriter stack_trace = new StringWriter();
    me.printStackTrace(new PrintWriter(stack_trace));

    // Inform the user
    if (me.informUser()) {
      notifyUser(me.getMessage());

      // Inform the administrator
    }
    if (me.informAdministrator()) {
      MEMEMail meme_mail = new MEMEMail();

      try {
        meme_mail.smtp_host = 
        	MIDServices.getService(getServiceForProperty(MAIL_HOST));
        if (meme_mail.smtp_host.equals("")) {
          meme_mail.smtp_host = getProperty(MAIL_HOST);        	
        }      	
      	meme_mail.from =
      	  MIDServices.getService("meme-from");
        if (meme_mail.from.equals("")) {
          meme_mail.from = "meme@mail.nlm.nih.gov";        	
        }

        meme_mail.to = FieldedStringTokenizer.split(
        	MIDServices.getService(getServiceForProperty(me.getAdministrator())),
      		",");

        if (meme_mail.to.equals("") || meme_mail.to.length == 0) {
        	meme_mail.to = FieldedStringTokenizer.split(
            	getProperty(me.getAdministrator()),
          		",");
        }

      } catch (MidsvcsException me2) {
        me.setEnclosedException(me2);
        me.setInformAdministrator(false);
        handleError(me);
      }

      String subject = null;
      try {
        subject = "MEME4 Application Server Error - " +
            InetAddress.getLocalHost().getHostName();
      } catch (Exception e) {
        MEMEException m_e = new MEMEException("Unknown hostname.");
        m_e.setEnclosedException(e);
        m_e.setInformAdministrator(false);
        handleError(m_e);
      }

      String message = me.toString() + "\n\n" +
          stack_trace.toString();
      try {
        meme_mail.send(subject, message);
      } catch (MEMEException e) {
        e.setInformAdministrator(false);
        handleError(e);
      }
    }

    // Write to log
    logComment(me.toString(), true);
    logComment(stack_trace.toString(), true);

    // Exit if fatal
    if (me.isFatal()) {
      System.exit(1);

      // add to statistics
    }
    Statistics.addException(me);

  }

  /**
   * Splits a string on word boundaries so that each element of the string
   * array returned represents a "line" of the original string that is no
   * longer than the specified max line length. This is very useful for
   * breaking up long strings into 80 character chunks for displaying in a "
   * terminal.
   * @param to_split the {@link String} to split up
   * @param line_length the maximum line length, or <code>-1</code> to
   *        accept the default setting.
   * @return a {@link String}<code>[]</code> of the lines
   */
  public static String[] splitString(String to_split, int line_length) {
    // default max
    int max_length = 80;
    if (line_length > 0) {
      max_length = line_length;
    }
    String word = null;
    StringTokenizer st = new StringTokenizer(to_split, " ");
    String[] result = new String[1000];
    int first_word = 1;
    int char_count = 0;
    int i = 0;
    while (st.hasMoreTokens()) {
      word = st.nextToken();
      if (char_count + ( (first_word > 0) ? 0 : 1) + word.length() < max_length) {
        if (result[i] == null) {
          result[i] = "";
        }
        result[i] = result[i] + ( (first_word > 0) ? "" : " ") + word;
        char_count += ( (first_word > 0) ? 0 : 1) + word.length();
      } else {
        if (first_word == 0) {
          i++;
        }
        result[i] = word;
        char_count = word.length();
      }
      if (first_word > 0) {
        first_word = 0;
      }
    }
    return result;
  }

  /**
   * Sort the specified file using a UNIX sort command. This should
   * really be moved elsewhere.
   * @param filename the file to sort
   * @throws MEMEException if failed to sort.
   */
  public static void sort(String filename) throws MEMEException {
    exec(new String[] {"/bin/sort", "-u", "-T", ".", "-o", filename, filename});
  }

  /**
   * Returns mid service property
   * @param dot_prop the property to be converted into mid service property
   */  
  public static String getServiceForProperty(String dot_prop) {
  	String dash_prop = dot_prop.replaceAll("\\.", "-");
  	if (dash_prop.indexOf("admin") != -1) {
  		return dash_prop + "-list";
  	}  	
  	if (dash_prop.equals("meme-smtp-host")) {
  		return "smtp-host";
  	}  	
  	return dash_prop;
  }

}
