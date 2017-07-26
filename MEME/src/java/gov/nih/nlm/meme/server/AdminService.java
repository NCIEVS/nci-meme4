/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  AdminService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.SystemStatusAction;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceConnectionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.MIDActionEngine;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.CGIStyleMEMEServiceRequest;
import gov.nih.nlm.meme.xml.MASRequestSerializer;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Handles requests for administrative tasks.
 *
 * @author MEME Group
 */
public class AdminService implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from {@link MEMEApplicationServer}.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    //
    // Get Service Request and function parameter
    //
    MEMEServiceRequest request = context.getServiceRequest();
    MEMEDataSource data_source = (MEMEDataSource) context.getDataSource();
    String function = (String) request.getParameter("function").getValue();

    //
    // Server log
    //
    int head = 0;
    int tail = 0;

    if (request.getParameter("head") != null) {
      head = request.getParameter("head").getInt();
    }
    if (request.getParameter("tail") != null) {
      tail = request.getParameter("tail").getInt();

    }
    if (function.equals("log") || function.equals("server_log")) {
      // If there is no log, report that message
      if (MEMEToolkit.getProperty( (ServerConstants.LOG_FILE)).toString().
          equals("")) {
        request.addReturnValue(new Parameter.Default("log",
            "The server is logging to STDOUT instead of to a file."));
        // If there is a log, show it (or head/tail sections of it)
      } else {
        // Return the log via the "log" return value
        request.addReturnValue(new Parameter.Default("log",
            getLog(getServerLog(), head, tail)));
      }
    } else

    //
    // Session Log
    //
    if (function.equals("session_log")) {
      if (request.getSessionId() == null || request.getSessionId().equals("")) {
        BadValueException bve = new BadValueException(
            "Request has no session id and tries to access " +
            "a service that requires a session id.");
        bve.setDetail("service", request.getService());
        bve.setFatal(false);
        throw bve;
      } else {
        request.addReturnValue(new Parameter.Default("log",
            getLog(getSessionLog(context), head, tail)));
      }
    } else

    //
    // Session Progress
    //
    if (function.equals("session_progress")) {
      if (request.getSessionId() == null || request.getSessionId().equals("")) {
        BadValueException bve = new BadValueException(
            "Request has no session id and tries to access " +
            "a service that requires a session id.");
        bve.setDetail("service", request.getService());
        bve.setFatal(false);
        throw bve;
      } else {
        request.addReturnValue(new Parameter.Default("progress",
            getProgress(context)));
      }
    } else

    //
    // Session Progress Redirect
    //
    if (function.equals("session_progress_redirect")) {
      OutputStream out =
          ( (CGIStyleMEMEServiceRequest) context.getServiceRequest()).
          getOutputStream();
      if (request.getSessionId() == null || request.getSessionId().equals("")) {
        BadValueException bve = new BadValueException(
            "Request has no session id and tries to access " +
            "a service that requires a session id.");
        bve.setDetail("service", request.getService());
        bve.setFatal(false);
        throw bve;
      }
      int progress = getProgress(context);
      try {
        Writer x = new OutputStreamWriter(out);
        x.write("HTTP/1.1 302 redirect\n");
        x.write("Location: /images/pm/" + progress +
                ".gif\n");
        x.write("\n");
        x.flush();
        out.flush();
        out.close();
      } catch (IOException ioe) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to write to output stream.", ioe);
        throw ere;
      }

    } else

    //
    // Session Log Not Seen
    //
    if (function.equals("session_log_not_seen")) {
      if (request.getSessionId() == null || request.getSessionId().equals("")) {
        BadValueException bve = new BadValueException(
            "Request has no session id and tries to access " +
            "a service that requires a session id.");
        bve.setDetail("service", request.getService());
        bve.setFatal(false);
        throw bve;
      } else {
        request.addReturnValue(new Parameter.Default("log",
            getLog(getSessionLogNotSeen(context), head, tail)));
      }
    } else

    //
    // Transaction Log
    //
    if (function.equals("transaction_log")) {
      request.addReturnValue(new Parameter.Default("log",
          getLog(getTransactionLog((MIDDataSource)data_source,
                                   request.getParameter("transaction_id").
                                   getInt()), head, tail)));
    } else

    //
    // Statistics
    //
    if (function.equals("stats")) {

      // Explicitly call garbage collector
      System.gc();

      // Handle request to see Statistics.

      StringBuffer stats = new StringBuffer(1000);

      // Write statistics
      stats.append("\nServer Statistics\n");
      stats.append("-----------------\n");

      // Time
      stats.append("Start time:    ");
      stats.append(Statistics.getServerStartTime());

      stats.append("\nTotal Runtime: ");
      stats.append(MEMEToolkit.timeToString(Statistics.getServerRunTime()));
      stats.append("\n");

      // Memory
      stats.append("\nFree memory:                 ");
      stats.append(Statistics.getFreeMemory());

      stats.append("\nTotal memory used by server: ");
      stats.append(Statistics.getTotalMemory());
      stats.append("\n");

      // ThreadPool Statistics
      PoolStatistics pool_statistics = Statistics.getThreadPoolStatistics();

      double avg = pool_statistics.getAverageUsage();
      avg = ( ( (int) (avg * 10000)) / 100.0);

      stats.append("\nThread pool statistics");
      stats.append("\n    Active Count:\t ");
      stats.append(pool_statistics.getActiveCount());
      stats.append("\n    Inactive Count:\t ");
      stats.append(pool_statistics.getInactiveCount());
      stats.append("\n    Last Sample:\t ");
      stats.append(pool_statistics.getLastSample());
      stats.append("\n    Average Usage:\t ");
      stats.append(String.valueOf(avg));
      stats.append("%");
      stats.append("\n    Sample Size:\t ");
      stats.append(pool_statistics.getSampleSize());
      stats.append("\n");

      // DataSourcePool Statistics
      pool_statistics = Statistics.getDataSourcePoolStatistics();

      avg = pool_statistics.getAverageUsage();
      avg = ( ( (int) (avg * 10000)) / 100.0);
      stats.append("\nData source pool statistics");
      stats.append("\n    Active Count:\t ");
      stats.append(pool_statistics.getActiveCount());
      stats.append("\n    Inactive Count:\t ");
      stats.append(pool_statistics.getInactiveCount());
      stats.append("\n    Last Sample:\t ");
      stats.append(pool_statistics.getLastSample());
      stats.append("\n    Average Usage:\t ");
      stats.append(String.valueOf(avg));
      stats.append("%");
      stats.append("\n    Sample Size:\t ");
      stats.append(pool_statistics.getSampleSize());
      stats.append("\n");

      // Count
      stats.append("\nRequest count:           ");
      stats.append(Statistics.getRequestCount());

      stats.append("\nActive request count:    ");
      stats.append(Statistics.getActiveRequestCount());

      stats.append("\nProcessed request count: ");
      stats.append(Statistics.getProcessedRequestCount());

      stats.append("\nActive session count:    ");
      stats.append(Statistics.getActiveSessionCount());
      stats.append("\n");

      // Session count by service
      Collection sessions = Statistics.getSessionCountsByService().keySet();
      stats.append("\nSession counts by service name\n");
      Iterator iterator = sessions.iterator();
      String space50 =
          "                                                           ";
      while (iterator.hasNext()) {
        String str = (String) iterator.next();
        String str_pad = str + space50;
        stats.append("   ");
        stats.append(str_pad.substring(0, 50));
        stats.append("\t");
        stats.append(Statistics.getSessionCountByService(str));
        stats.append("\n");
      }

      // Request count by service
      Collection requests = Statistics.getRequestCountsByService().keySet();
      stats.append("\nRequest counts by service name\n");
      iterator = requests.iterator();
      while (iterator.hasNext()) {
        String str = (String) iterator.next();
        String str_pad = str + space50;
        stats.append("   ");
        stats.append(str_pad.substring(0, 50));
        stats.append("\t");
        stats.append(Statistics.getRequestCountByService(str));
        stats.append("\n");
      }

      // Count by type
      requests = Statistics.getRequestCountsByType().keySet();
      stats.append("\nRequest counts by service type\n");
      iterator = requests.iterator();
      while (iterator.hasNext()) {
        Class cls = (Class) iterator.next();
        String str_pad = cls.getName() + space50;
        stats.append("   ");
        stats.append(str_pad.substring(0, 50));
        stats.append("\t");
        stats.append(Statistics.getRequestCountByType(cls));
        stats.append("\n");
      }

      // Count by mid service
      requests = Statistics.getRequestCountsByMidService().keySet();
      stats.append("\nRequest counts by mid service\n");
      iterator = requests.iterator();
      while (iterator.hasNext()) {
        String str = (String) iterator.next();
        String str_pad = str + space50;
        stats.append("   ");
        stats.append(str_pad.substring(0, 50));
        stats.append("\t");
        stats.append(Statistics.getRequestCountByMidService(str));
        stats.append("\n");
      }

      // Exception
      requests = Statistics.getExceptions().keySet();
      if (requests.size() > 0) {
        stats.append("\nExceptions\n");

      }
      iterator = requests.iterator();
      while (iterator.hasNext()) {
        String str1 = (String) iterator.next();
        String str2 = str1.substring(27);
        String str_pad = str2 + space50;
        stats.append("   ");
        stats.append(str_pad.substring(0, 50));
        stats.append("\t");
        stats.append(Statistics.getExceptionCountByType(str1));
        stats.append("\n");
      }

      stats.append("\n");

      // Logs the statistics
      MEMEToolkit.logComment(stats.toString(), true);

      // Sets the return value
      request.addReturnValue("stats", stats.toString(), false);

    } else

    //
    // Dummy
    //
    if (function.equals("dummy")) {
      // Handle request to test the connection.
      request.addReturnValue(new Parameter.Default("dummy",
          "This is a dummy request."));

    } else

    //
    // Version
    //
    if (function.equals("version")) {
      // Handle request to test the connection.
      request.addReturnValue(new Parameter.Default("version",
          gov.nih.nlm.meme.Version.getVersionInformation()));

    } else

    //
    // Shutdown
    //
    if (function.equals("shutdown")) {

      // TODO IN THIS SECTION:
      // Check if theres a sub-parameter "kill"
      // if so, call ServerToolkit.setKillWithWait(kill)
      Parameter kill = request.getParameter("kill");
      if (kill != null) {
        ServerToolkit.setKillWithWait(!Boolean.valueOf( (String) kill.getValue()).
                                      booleanValue());

        // Handle request to stop the server.
      }
      MEMEToolkit.trace(
          "AdminService.processRequest() - request to stop the server.");
      context.getServer().stop();

      MEMEToolkit.trace(
          "AdminService.processRequest() - get writer from the session context.");
      Writer out = context.getServiceRequest().getWriter();

      MASRequestSerializer serializer = new MASRequestSerializer(MEMEToolkit.
          getSystemId());

      //MEMEServiceRequest request = context.getServiceRequest();
      MEMEToolkit.trace("AdminService.processRequest() - " + request);

      request.addReturnValue("response", "The server is down.", false);
      String doc = serializer.toXML(request);

      try {

        // Send an HTTP response header to the client
        out.write("HTTP/1.1 200 OK\n");
        out.write("Expires: Fri, 20 Sep 1998 01:01:01 GMT\n");

        // Write headers
        MEMEToolkit.trace(
            "AdminService.processRequest() - Content-Type: text/xml");
        out.write("Content-Type: text/xml\n");
        MEMEToolkit.trace("AdminService.processRequest() - Content-Length: " +
                          doc.length());
        out.write("Content-Length: ");

        // Write the document
        out.write(String.valueOf(doc.length()));
        out.write("\n\n");

        MEMEToolkit.trace("AdminService.processRequest() - Write document: " +
                          doc);
        out.write(doc);
        out.write("\n");

        // Flush output buffer
        out.flush();

        out.close();

      } catch (Exception e) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to write document to output stream.", e);
        ere.setDetail("document", doc);
        throw ere;
      }

      MEMEToolkit.Exit(0);

    }

    //
    // Refresh caches
    //
    else if (function.equals("refresh_caches")) {
      MIDServices.refreshCache();
      
      if (data_source != null)
        data_source.refreshCaches();
      else
        // Handle request to clear data source caches.
        ServerToolkit.getDataSourcePool().refreshCaches();

    }

    //
    // Refresh db connections
    //
    else if (function.equals("refresh_db")) {
      ServerToolkit.getDataSourcePool().reallocateDataSources(request.
          getMidService());
      try {
        context.getDataSource().close();
      } catch (Exception e) {
        MEMEToolkit.handleError(e);
      }
      ;
      context.setDataSource(null);

    }

    //
    // System Status
    //
    else if (function.equals("set_system_status")) {
      if (request.getParameter("key") != null && request.getParameter("value") != null) {
        String key = (String) request.getParameter("key").getValue();
        String value = (String) request.getParameter("value").getValue();
        SystemStatusAction ssa = SystemStatusAction.newSetSystemStatusAction(
            key, value);
        ( (MIDActionEngine) data_source.getActionEngine()).processAction(ssa);
      }
    }

    else if (function.equals("get_system_status")) {
      if (request.getParameter("key") != null) {
        String key = (String) request.getParameter("key").getValue();
        String value = data_source.getSystemStatus(key);
        // Return the log via the "log" return value
        request.addReturnValue(new Parameter.Default("system_status", value));
      }

    }

    //
    // Enable Integrity System
    //
    else if (function.equals("enable_integrity")) {
      SystemStatusAction ssa = SystemStatusAction.newSetSystemStatusAction(
          "ic_system", "ON");
      ( (MIDActionEngine) data_source.getActionEngine()).processAction(ssa);
    }

    //
    // Disable Integrity System
    //
    else if (function.equals("disable_integrity")) {
      SystemStatusAction ssa = SystemStatusAction.newSetSystemStatusAction(
          "ic_system", "OFF");
      ( (MIDActionEngine) data_source.getActionEngine()).processAction(ssa);
    }

    //
    // Is Integrity System Enabled
    //
    else if (function.equals("is_integrity_enabled")) {
      // Handle request to determine if status of integrity system
      boolean integrity_system_flag = ((MIDDataSource)data_source).isIntegritySystemEnabled();
      request.addReturnValue(new Parameter.Default("is_integrity_enabled",
          integrity_system_flag));

    }

    //
    // Enable Editing System
    //
    else if (function.equals("enable_editing")) {
      SystemStatusAction ssa = SystemStatusAction.newSetSystemStatusAction(
          "dba_cutoff", "y");
      ( (MIDActionEngine) data_source.getActionEngine()).processAction(ssa);
    }

    //
    // Disable Editing System
    //
    else if (function.equals("disable_editing")) {
      SystemStatusAction ssa = SystemStatusAction.newSetSystemStatusAction(
          "dba_cutoff", "n");
      ( (MIDActionEngine) data_source.getActionEngine()).processAction(ssa);
    }

    //
    // Is Editing System Enabled
    //
    else if (function.equals("is_editing_enabled")) {
      // Handle request to determine if status of editing system
      boolean editing_flag = ((MIDDataSource)data_source).isEditingEnabled();
      request.addReturnValue(new Parameter.Default("is_editing_enabled",
          editing_flag));

    }

    //
    // Enable atomic action validation
    //
    if (function.equals("enable_validate_atomic_action")) {
      // Handle request to enable validate atomic action
      ServerToolkit.setProperty(MEMEConstants.VALIDATE_ATOMIC_ACTIONS, "true");
      request.addReturnValue(
          new Parameter.Default("enable_validate_atomic_action",
                                "Validate atomic action is enabled."));
    }

    //
    // Disable atomic action validation
    //
    else if (function.equals("disable_validate_atomic_action")) {
      // Handle request to enable validate atomic action
      ServerToolkit.setProperty(MEMEConstants.VALIDATE_ATOMIC_ACTIONS, "false");
      request.addReturnValue(
          new Parameter.Default("disable_validate_atomic_action",
                                "Validate atomic action is disabled."));

    }

    //
    // Is atomic action validation enabled
    //
    else if (function.equals("is_validate_atomic_action_enabled")) {
      // Handle request to determine if status of editing system
      boolean atomic_flag = ((MIDDataSource)data_source).isAtomicActionValidationEnabled();
      request.addReturnValue(new Parameter.Default(
          "is_validate_atomic_action_enabled",
          atomic_flag));

    }

    //
    // Enable molecular action validation
    //
    else if (function.equals("enable_validate_molecular_action")) {
      // Handle request to enable validate molecular action
      ServerToolkit.setProperty(MEMEConstants.VALIDATE_MOLECULAR_ACTIONS,
                                "true");
      request.addReturnValue(
          new Parameter.Default("enable_validate_molecular_action",
                                "Validate molecular action is enabled."));
    }

    //
    // Disable molecular action validation
    //
    else if (function.equals("disable_validate_molecular_action")) {
      // Handle request to enable validate molecular action
      ServerToolkit.setProperty(MEMEConstants.VALIDATE_MOLECULAR_ACTIONS,
                                "false");
      request.addReturnValue(
          new Parameter.Default("disable_validate_molecular_action",
                                "Validate molecular action is disabled."));

    }

    //
    // Is molecular action validation enabled?
    //
    else if (function.equals("is_validate_molecular_action_enabled")) {
      // Handle request to determine if status of editing system
      boolean molecular_flag = ((MIDDataSource)data_source).isMolecularActionValidationEnabled();
      request.addReturnValue(new Parameter.Default(
          "is_validate_molecular_action_enabled",
          molecular_flag));

    }

    //
    // Authentication
    //
    else if (function.equals("authenticate")) {
      String user = (String) request.getParameter("user").getValue();
      String password = (String) request.getParameter("password").getValue();
      if (password == null || password.equals("")) {
        password = "null";

      }
      MIDDataSource ds2 = null;
      boolean authenticated = true;
      try {
        ds2 = ServerToolkit.newMIDDataSource(request.getMidService(), user,
                                             password);
      } catch (DataSourceConnectionException e) {
        authenticated = false;
      }
      // If authenticated, return the EditorPreferences associated
      EditorPreferences ep = null;
      if (authenticated) {
        ep = ds2.getEditorPreferencesByUsername(user.toLowerCase());

      }
      try {
        ds2.close();
      } catch (Exception e) {}

      request.addReturnValue(new Parameter.Default("editor_preferences", ep));

    } else if(function.equals("change_password")) {
      String user = (String) request.getParameter("user").getValue();
      String password = (String) request.getParameter("password").getValue();
      data_source.changePassword(user,password);

    } else if(function.equals("get_password_expiration_date")) {
      Date expiration_date = data_source.getPasswordExpirationDate();
      request.addReturnValue(new Parameter.Default("expiration_date", expiration_date));

    }

  } // end processRequest

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean requiresSession() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isReEntrant() {
    return false;
  }

  //
  // Private methods
  //

  /**
   * Returns the log portion specified by "head" and "tail" parameters.
   * @param in the {@link BuffereDreader} log
   * @param head the "head" value
   * @param tail the "tail" value
   * @return the log portion specified by "head" and "tail" parameters
   * @throws ExternalResourceException if anything goes wrong
   */
  private String getLog(BufferedReader in, int head, int tail) throws
      ExternalResourceException {

    StringBuffer logs = new StringBuffer(5000);
    int ctr = 0;
    String line = null;

    try {

      if (head > 0) {
        // If its a head request, read either
        // until the end of the log or until
        // a number of lines equal to the head parameter
        // has been read
        while ( (line = in.readLine()) != null &&
               ctr < head) {
          logs.append(line + "\n");
          ctr++;
        }
      } else if (tail > 0) {
        // If it is a tail request, read all
        // lines, then return only the lines at
        // the end equal in number to the
        // tail parameter
        StringBuffer tmp = new StringBuffer(5000);
        while ( (line = in.readLine()) != null) {
          tmp.append(line + "\n");
        }

        int index = tmp.toString().lastIndexOf('\n');
        for (int i = 0; i < tail; i++) {
          index = tmp.toString().lastIndexOf('\n', index - 1);
          // handle case where tail parameter
          // is greater than # lines in the file
          if (index == -1) {
            line = tmp.toString();
            break;
          }
          line = tmp.toString().substring(index);
        }
        logs.append(line);
      } else {
        // Here, this is not a tail or head
        // request, read the entire log.
        while ( (line = in.readLine()) != null) {
          logs.append(line + "\n");
        }
      }
    } catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to read log file.", e);
      throw ere;
    }
    return logs.toString();
  }

  /**
   * Returns a {@link BufferedReader} wrapper around server log.
   * @return a {@link BufferedReader} wrapper around server log
   * @throws ExternalResourceException if the log file cannot be accessed
   */
  private BufferedReader getServerLog() throws ExternalResourceException {

    // Find path to log file & prep a buffer
    String log_file = MEMEToolkit.getProperty( (ServerConstants.LOG_FILE));
    BufferedReader in = null;
    String path = null;
    try {
      // Get the log file
      path = MEMEToolkit.getProperty(MEMEConstants.MEME_HOME);
      in = new BufferedReader(new InputStreamReader(new FileInputStream(path +
          "/" + log_file), "UTF-8"));

    } catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to read log file.", e);
      ere.setDetail("log_file", path + "/" + log_file);
      throw ere;
    }
    return in;
  }

  /**
   * Returns a {@link BufferedReader} wrapper on the session log.
   * @param context the {@link SessionContext}
   * @return a {@link BufferedReader} wrapper on the session log
   */
  private BufferedReader getSessionLog(SessionContext context) {
    StringBuffer sb = (StringBuffer) context.get("log");
    if (sb == null) {
      sb = (new StringBuffer()).append("This session has no log.");
    }
    return new BufferedReader(new StringReader(sb.toString()));
  }

  /**
   * Returns progress value.
   * @param context the {@link SessionContext}
   * @return progress value
   */
  private int getProgress(SessionContext context) {
    Integer progress = (Integer) context.get("progress");
    if (progress == null) {
      return -1;
    }
    return progress.intValue();
  }

  /**
   * Returns a {@link BufferedReader} for the portion of the session log not yet seen.
   * @param context the {@link SessionContext}
   * @return a {@link BufferedReader} for the portion of the session log not yet seen
   */
  private BufferedReader getSessionLogNotSeen(SessionContext context) {
    StringBuffer sb = (StringBuffer) context.get("log");
    Integer len = (Integer) context.get("length");
    if (sb == null) {
      sb = (new StringBuffer()).append("This session has no log.");
      len = new Integer(0);
    }
    if (len == null) {
      len = new Integer(0);
    }
    context.put("length", new Integer(sb.length()));
    return new BufferedReader(new StringReader(sb.toString().substring(len.
        intValue())));
  }

  /**
   * Returns a {@link BufferedReader} of the transaction log.
   * @param data_source the {@link MIDDataSource}
   * @param transaction_id the transaction id to get a log for
   * @return a {@link BufferedReader} of the transaction log
   * @throws DataSourceException if transaction log cannot be created
   */
  private BufferedReader getTransactionLog(MIDDataSource data_source,
                                           int transaction_id) throws
      DataSourceException {
    StringBuffer log = new StringBuffer(5000);
    Iterator iter = data_source.findMolecularActions(
        new SearchParameter.Single("transaction_id",
                                   String.valueOf(transaction_id)));

    List actions = new ArrayList();
    while (iter.hasNext()) {
      actions.add(iter.next());
    }

    if (actions.size() > 0) {
      MolecularAction first_action = (MolecularAction) actions.get(0);
      log.append(
          "-----------------------------------------------------------------\n");
      log.append("Starting ... (").append(MEMEToolkit.getDateFormat().format(
          first_action.getTimestamp())).append(")\n");
      log.append(
          "-----------------------------------------------------------------\n");
      log.append("  Transaction ID: ")
          .append(first_action.getTransactionIdentifier().toString()).append(
          "\n");
      log.append("  Work ID: ")
          .append(first_action.getWorkIdentifier().toString()).append("\n");
      log.append("  Database: ")
          .append(data_source.getDataSourceName()).append("\n");
      log.append("  Action Count: ")
          .append(actions.size()).append("\n\n");

      for (int i = 0; i < actions.size(); i++) {
        MolecularAction action = (MolecularAction) actions.get(i);
        AtomicAction[] aas = action.getAtomicActions();
        boolean c_flag = false;
        boolean r_flag = false;
        boolean a_flag = false;
        for (int j = 0; j < aas.length; j++) {
          if (aas[j].getAffectedTable().equals("C")) {
            c_flag = true;
          }
          if (aas[j].getAffectedTable().equals("R")) {
            r_flag = true;
          }
          if (aas[j].getAffectedTable().equals("A")) {
            a_flag = true;
          }
        }
        String source = "";
        String target = "";
        if (action.getSourceIdentifier() != null) {
          source = " " + action.getSourceIdentifier().toString();
        }
        if (action.getTargetIdentifier() != null) {
          target = " " + action.getTargetIdentifier().toString();
        }
        log.append("    ")
            .append(action.getActionName())
            .append(source)
            .append(target)
            .append(" (");
        if (c_flag) {
          log.append("C");
        }
        if (r_flag) {
          log.append("R");
        }
        if (a_flag) {
          log.append("A");
        }
        log.append(") performed by ").append(action.getAuthority())
            .append(" on ").append(MEMEToolkit.getDateFormat().format(action.
            getTimestamp()))
            .append("\n");
        if (action.isUndone()) {
          log.append("      ")
              .append("(undone by ")
              .append(action.getAuthority())
              .append(" on ").append(MEMEToolkit.getDateFormat().format(action.
              getTimestamp()))
              .append(")\n");
        }
      }
      MolecularAction last_action = (MolecularAction) actions.get(actions.size() -
          1);
      log.append("\n");
      log.append(
          "-----------------------------------------------------------------\n");
      log.append("Finished ... (").append(MEMEToolkit.getDateFormat().format(
          last_action.getTimestamp())).append(")\n");
      log.append(
          "-----------------------------------------------------------------\n");

    } else {
      throw new DataSourceException("No actions matching this transaction id.");
    }

    return new BufferedReader(new StringReader(log.toString()));
  }

 
}
