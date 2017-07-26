/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme
 * Object:  MIDServices
 *
 *****************************************************************************/

package gov.nih.nlm.meme;

import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.LvgServerException;
import gov.nih.nlm.meme.exception.MidsvcsException;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Utility for accessing
 * NLM socket services. Following is an example of how to use
 * this class.
 * <pre>
 * try {
 *
 *   // Get a service where we know the name
 *   String current_db = MIDServices.getService("editing-db");
 *
 *   // Get a list of services provided
 *   String[] services_list = MIDServices.getServicesList();
 *
 *   // Get a list of database services.
 *   // These are service name prefixes to which "-db"
 *   // could be appended to get a real service
 *   String[] db_services = MIDServices.getDbServicesList();
 *
 * } catch (Exception e) {
 *   // Something went wrong, most likely the server is not running
 *   // or is not accessible (i.e. the network is down)
 * }
 * </pre>
 *
 * In addition to accessing the mid services socket server, this
 * class has utility methods for accessing the various LVG
 * servers, in particular the <code>norm</code> and <code>luiNorm</code>
 * servers.  For example,
 * <pre>
 * try {
 *
 *   // Get the unnormalized words
 *   String [] words = MIDServices.getWords(
 *                             "this is a test string");
 *
 *   // Get the normalized words for a string
 *   String [] norm_words = MIDServices.getNormalizedWords(
 *                             "this is a test string");
 *
 *   // Get the lui normalized words for a string
 *   String [] lui_norm_words = MIDServices.getLuiNormalizedWords(
 *                             "this is a test string");
 *
 *   // Normalize a string
 *   // Note that (potentially) multiple strings are returned
 *   // If the string normalizes to nothing (such as "and")
 *   // the output will be an empty string array.
 *   String [] norm_strings = MIDServices.getNormalizedStrings(
 *                             "this is a test string");
 *
 *   // LuiNormalize a string
 *   // Note that only one string is returned
 *   // If the string normalizes to nothing (such as "and")
 *   // the output will be "" (the empty string).
 *   String lui_norm_string = MIDServices.getLuiNormalizedString(
 *                             "this is a test string");
 *
 * } catch (Exception e) {
 *   // Something went wrong, most likely the server is not running
 * }
 * </pre>
 *
 * This class comes equiped with a self-testing {@link #main(String[])} method.
 * Previously, the MID services socket server was accessed using the
 * <code>$MIDSVCS_HOME/bin/midsvcs.pl</code> and the norm servers were
 * accessed using the various <code>$LVGIF_HOME</code> scripts.
 *
 * @author MEME Group
 */
public class MIDServices implements MEMEConstants {

  //
  // Private Fields
  //
  private static Properties services_cache = null;

  //
  // Public Methods
  //

  /**
   * Returns a list of all supported MID services.
   * @return a {@link String}<code>[]</code> containing
   *         valid MID service names
   * @throws MidsvcsException if the midsvcs server is not available
   */
  public static String[] getServicesList() throws MidsvcsException {
    if (services_cache == null) {
      refreshCache();
    }
    return (String[]) services_cache.keySet().toArray(new String[0]);
  }

  /**
   * Refreshes the cached MID service name/value pairs.
   * @throws MidsvcsException if the midsvcs server is not available
   */
  public static void refreshCache() throws MidsvcsException {
    try {
      services_cache = new Properties();
      String line;
      Socket sock = new Socket(
          MEMEToolkit.getProperty(MIDSVCS_HOST),
          Integer.parseInt(MEMEToolkit.getProperty(MIDSVCS_PORT)));
      BufferedReader bin = new BufferedReader(
          new InputStreamReader(sock.getInputStream(), "UTF-8"));
      while ( (line = bin.readLine()) != null) {
        services_cache.put(line.substring(0, line.indexOf("|")),
                           line.substring(line.indexOf("|") + 1));
      }
    } catch (Exception e) {
      MidsvcsException me = new MidsvcsException(
          "Mid services server is not available.", e);
      throw me;
    }
  }

  /**
   * Returns a list of all supported DB services.  The values
   * returned here can be then sent to {@link #getService(String)} to
   * obtain relevant Oracle TNS names.
   * @return a {@link String}<code>[]</code> containing
   *         valid DB service names
   * @throws MidsvcsException if the midsvcs server is not available
   */
  public static String[] getDbServicesList() throws MidsvcsException {

    if (services_cache == null) {
      refreshCache();

    }
    String all_services[] = new String[100];
    ArrayList db_services = new ArrayList();

    all_services = getServicesList();
    for (int i = 0; i < all_services.length; i++) {
      if (all_services[i].endsWith("db")) {
        db_services.add(all_services[i]);
      }
    }
    return (String[]) db_services.toArray(new String[0]);
  }

  /**
   * Returns a list of all supported meme server services.  The values
   * returned here can be then sent to {@link #getService(String)} to
   * obtain relevant meme application servers.
   * @return a {@link String}<code>[]</code> containing
   *         valid meme server services.
   * @throws MidsvcsException if the midsvcs server is not available
   */
  public static String[] getHostServicesList() throws MidsvcsException {

    if (services_cache == null) {
      refreshCache();

    }
    String all_services[] = new String[100];
    Set host_services = new HashSet();

    all_services = getServicesList();
    for (int i = 0; i < all_services.length; i++) {
      if (all_services[i].endsWith("meme-server-host")) {
        host_services.add(all_services[i]);
      }
    }
    String[] x = (String[]) host_services.toArray(new String[0]);
    Arrays.sort(x);
    return x;
  }

  /**
   * Returns a list of all supported meme server port services.  The values
   * returned here can be then sent to {@link #getService(String)} to
   * obtain relevant meme application server ports.
   * @return a {@link String}<code>[]</code> containing
   *         valid meme server ports services.
   * @throws MidsvcsException if the midsvcs server is not available
   */
  public static String[] getPortServicesList() throws MidsvcsException {

    if (services_cache == null) {
      refreshCache();

    }
    String all_services[] = new String[100];
    ArrayList port_services = new ArrayList();

    all_services = getServicesList();
    for (int i = 0; i < all_services.length; i++) {
      if (all_services[i].indexOf("meme-server-port") != -1) {
        port_services.add(all_services[i]);
      }
    }
    return (String[]) port_services.toArray(new String[0]);
  }

  /**
   * Returns the value matching a service name.
   * @param service A valid MID service name
   * @return service name.
   * @throws MidsvcsException if the midsvcs server is not available or
   *                         if an invalid service name is passed
   */
  public static String getService(String service) throws MidsvcsException {
  	// Following if statement is wrong as the lvg server can exist in different server. Commenting this out. 
	//if (service.equals("lvg-server-host")) return "localhost";
  	if (services_cache == null) {
      refreshCache();

    }
    try {

      String result = null;
      result = (String) services_cache.get(service);
      return (result == null) ? "" : result;

    } catch (Exception e) {
      MidsvcsException me = new MidsvcsException(
          "Mid services server is not available.", e);
      throw me;
    }
  }

  /**
   * Normalize a string using LVG <code>norm</code>
   * and break it into words.
   * @param string a {@link String} to normalize
   * @return a {@link String}<code>[]</code> of normalized words
   * @throws LvgServerException if failed to normalized words
   */
  public static String[] getNormalizedWords(String string) throws
      LvgServerException {

    // Get normalized strings
    String[] norm_strings = getNormalizedStrings(string);

    HashSet hashset = new HashSet();

    // Loop thru normalized strings
    for (int i = 0; i < norm_strings.length; i++) {

      // Feed each of normalized strings to getWords
      String[] words = getWords(norm_strings[i]);

      for (int j = 0; j < words.length; j++) {
        // Look for words without duplicates

        if (words[j] != null && !hashset.contains(words[j])) {

          // Put all the words from all strings into a set
          hashset.add(words[j]);
        }
      }
    }

    return (String[]) hashset.toArray(new String[0]);
  }

  /**
   * Normalize a string using LVG <code>luiNorm</code>
   * and break it into words.
   * @param string a {@link String} to normalize
   * @return a {@link String}<code>[]</code> of lui-normalized words
   * @throws LvgServerException if failed to normalized words
   */
  public static String[] getLuiNormalizedWords(String string) throws
      LvgServerException {

    // Get normalized strings
    String norm_string = getLuiNormalizedString(string);

    // Break the string
    String[] words = getWords(norm_string);

    return words;
  }

  /**
   * Break a string into words.
   * @return a {@link String}<code>[]</code> of words
   * @param string a {@link String} to break into words
   */
  public static String[] getWords(String string) {
    // This mimics the wordInd functionality
    final String delim = " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^";
    StringTokenizer st = new StringTokenizer(string, delim);
    String[] words = new String[st.countTokens()];
    for (int i = 0; st.hasMoreTokens(); i++) {
      words[i] = st.nextToken();
    }
    return words;
  }

  /**
   * Normalizes a string using LVG <code>norm</code>.
   * @param string a {@link String} to normalize
   * @return a {@link String}<code>[]</code> of normalized strings
   * @throws LvgServerException if failed to normalized the string
   */
  public static String[] getNormalizedStrings(String string) throws
      LvgServerException {

    // Open Socket
    Socket sock = null;
    try {
      sock = new Socket(
          getService("lvg-server-host"),
          Integer.valueOf(getService("lvg-server-port")).intValue());
    } catch (MidsvcsException mse) {
      LvgServerException me =
          new LvgServerException("Failed to get mid service.", mse);
      throw me;
    } catch (IOException ioe) {
      LvgServerException me =
          new LvgServerException("Failed to open socket.", ioe);
      throw me;
    }

    // Write to socket
    try {
      OutputStreamWriter out =
          new OutputStreamWriter(sock.getOutputStream(), "UTF-8");
      out.write("N|");
      out.write(string);
      out.write("\n");
      out.flush();
    } catch (IOException ioe) {
      LvgServerException me =
          new LvgServerException("Failed to write socket.", ioe);
      throw me;
    }

    // Read from socket
    // there may be multiple lines.
    String[] results;
    try {
      BufferedReader bin = new BufferedReader(
          new InputStreamReader(sock.getInputStream(), "UTF-8"));

      String line = bin.readLine();
      if (line.length() == 0) {
        throw new NullPointerException("No data read from socket");
      }
      String[] tmp_results = FieldedStringTokenizer.split(line, "|");
      results = new String[tmp_results.length - 2];
      for (int i = 2; i < tmp_results.length; i++) {
        results[i - 2] = tmp_results[i];
      }
    } catch (NullPointerException ne) {
      LvgServerException me =
          new LvgServerException("No data read from socket");
      throw me;
    } catch (IOException ie) {
      LvgServerException me =
          new LvgServerException("Failed to read from socket.", ie);
      throw me;
    }

    try {
      sock.close();
    } catch (IOException e) {
    // no big deal, do nothing
    }

    // Return as string array
    return results;

  }

  /**
   * Normalizes a string using LVG <code>luiNorm</code>.
   * @param string a {@link String} to lui-normalize
   * @return a lui-normalized {@link String}
   * @throws LvgServerException if failed to lui-normalize the string
   */
  public static String getLuiNormalizedString(String string) throws
      LvgServerException {

    // Open Socket
    Socket sock = null;
    try {
      sock = new Socket(
          getService("lvg-server-host"),
          Integer.valueOf(getService("lvg-server-port")).intValue());
    } catch (MidsvcsException mse) {
      LvgServerException me =
          new LvgServerException("Failed to get mid service.", mse);
      throw me;
    } catch (IOException ioe) {
      LvgServerException me =
          new LvgServerException("Failed to open socket.", ioe);
      throw me;
    }

    // Write to socket
    try {
      OutputStreamWriter out =
          new OutputStreamWriter(sock.getOutputStream(), "UTF-8");
      out.write("L|");
      out.write(string);
      out.write("\n");
      out.flush();
    } catch (IOException ioe) {
      LvgServerException me =
          new LvgServerException("Failed to write socket.", ioe);
      throw me;
    }

    // Read from socket
    // there may be multiple lines.
    String line = null;
    try {
      BufferedReader bin = new BufferedReader(
          new InputStreamReader(sock.getInputStream(), "UTF-8"));
      line = bin.readLine();
      if (line == null) {
        throw new NullPointerException("No data read from socket");
      }
    } catch (NullPointerException ne) {
      LvgServerException me =
          new LvgServerException("No data read from socket");
      throw me;
    } catch (IOException ioe) {
      LvgServerException me =
          new LvgServerException("Failed to read from socket.", ioe);
      throw me;
    }

    try {
      sock.close();
    } catch (IOException e) {
    // no big deal, do nothing
    }

    // Return as string
    return line.substring(line.lastIndexOf('|') + 1);

  }

  /**
   * Returns the data source password for the specified user.
   * @param user the data source user
   * @return the password
   * @throws ExecException if $MIDSVCS/bin/get-oracle-pwd.pl fails
   */
  public static String getDataSourcePassword(String user,String database)
      throws MidsvcsException {
    try {
    	String[] new_env = {
    	          "ENV_FILE="+MEMEToolkit.getProperty(ENV_FILE),
    	          "ENV_HOME="+MEMEToolkit.getProperty(ENV_HOME) };
    	 String pwd = MEMEToolkit.exec(new String[] {
          MEMEToolkit.getProperty("env.MIDSVCS_HOME")
              + "/bin/get-oracle-pwd.pl", "-u" + user,  "-d" + database }, new_env, false,
          MEMEToolkit.USE_INPUT_STREAM, false, null);
      
      return pwd.substring(pwd.indexOf('/') + 1).trim();
    } catch (ExecException e) {
      throw new MidsvcsException("Error calling get-oracle-pwd.pl", e);
    }
  }

  /**
   * Self test.
   *
   * @param s
   *          a {@link String}<code>[]</code>
   */
  public static void main(String[] s) {

    try {
      MEMEToolkit.initialize(null, null);
    } catch (InitializationException ie) {
      MEMEToolkit.handleError(ie);
    }
    MEMEToolkit.setProperty(DEBUG, "true");
    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Starting test of MIDServices ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    MEMEToolkit.trace("Look up all services (getServicesList)");
    try {
      String[] svcs = getServicesList();
      for (int i = 0; i < svcs.length; i++) {
        MEMEToolkit.trace(svcs[i] + " = " +
                          getService(svcs[i]));
      }
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    MEMEToolkit.trace("");
    MEMEToolkit.trace("Look up DB Services (getDbServicesList) - get TNS names");
    try {
      String[] dbsvcs = getDbServicesList();
      for (int i = 0; i < dbsvcs.length; i++) {
        MEMEToolkit.trace(dbsvcs[i] + " = " + getService(dbsvcs[i]));
      }
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    MEMEToolkit.trace("");
    MEMEToolkit.trace("Look up host Services (getHostServicesList)");
    try {
      String[] dbsvcs = getHostServicesList();
      for (int i = 0; i < dbsvcs.length; i++) {
        MEMEToolkit.trace(dbsvcs[i] + " = " + getService(dbsvcs[i]));
      }
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    MEMEToolkit.trace("");
    MEMEToolkit.trace("Look up port Services (getPortServicesList)");
    try {
      String[] dbsvcs = getPortServicesList();
      for (int i = 0; i < dbsvcs.length; i++) {
        MEMEToolkit.trace(dbsvcs[i] + " = " + getService(dbsvcs[i]));
      }
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    //
    // getWords
    //

    MEMEToolkit.trace("");
    MEMEToolkit.trace("Look up words (getWords)");
    try {
      String[] words = getWords("jogging");
      for (int i = 0; i < words.length; i++) {
        MEMEToolkit.trace("Words = " + words[i]);
      }
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    //
    // getNormalizedWords
    //

    MEMEToolkit.trace("");
    MEMEToolkit.trace("Look up normalized words (getNormalizedWords)");
    try {
      String[] normwords = getNormalizedWords("jogging");
      for (int i = 0; i < normwords.length; i++) {
        MEMEToolkit.trace("Normalized Words = " + normwords[i]);
      }
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    //
    // getLuiNormalizedWords
    //

    MEMEToolkit.trace("");
    MEMEToolkit.trace("Look up Lui normalized words (getLuiNormalizedWords)");
    try {
      String[] luinormwords = getLuiNormalizedWords("jogging");
      for (int i = 0; i < luinormwords.length; i++) {
        MEMEToolkit.trace("Lui Normalized Words = " + luinormwords[i]);
      }
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    //
    // getNormalizedStrings
    //

    MEMEToolkit.trace("");
    MEMEToolkit.trace("Look up Normalized strings (getNormalizedStrings)");
    try {
      String[] normstrs = getNormalizedStrings(
          "Accidental cut, puncture, perforation or hemorrhage during transfusion");
      for (int i = 0; i < normstrs.length; i++) {
        MEMEToolkit.trace("Normalized strings = " + normstrs[i]);
      }
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    //
    // getLuiNormalizedString
    //

    MEMEToolkit.trace("");
    MEMEToolkit.trace("Look up Lui Normalized strings (getLuiNormalizedString)");
    try {
      String normstr = getLuiNormalizedString(
          "Accidental cut, puncture, perforation or hemorrhage during transfusion");
      MEMEToolkit.trace("Normalized Lui string = " + normstr + ".");
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Finished test of MIDServices ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }; // end main

}
