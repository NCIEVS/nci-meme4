/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  MaintenanceClientTest
 *
 * 03/22/2007 BAC (1-D0BIJ): Remove "exec" tests (not platform-specific), and
 * 01/30/2006 RBE (1-763IU): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.MaintenanceClient;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.action.MolecularTransaction;

/**
 * Test suite for Maintenance
 */
public class MaintenanceClientTest extends TestSuite {

  public MaintenanceClientTest() {
    setName("MaintenanceClientTest");
    setDescription("Test Suite for Maintenance");
  }

  /**
   * Perform Test Suite Maintenance
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MaintenanceClient client = null;
    AdminClient admin_client = null;

    try {
      client = new MaintenanceClient("");
      admin_client = new AdminClient("");

	    //
	    // 1.1. Test setMidService(String), getMidService()
	    //
      addToLog("    1.1. Test setMidService(String), "
        + "getMidService() ... "
        + date_format.format(timestamp));

      client.setMidService("");
      if (client.getMidService().equals(""))
        addToLog("    1.1. Test Passed");
      else {
        addToLog("    1.1. Test Failed");
        thisTestFailed();
      }

	    //
	    // 2.1. Test setSessionId(String), getSessionId()
	    //
      addToLog("    2.1. Test setSessionId(String), "
        + "getSessionId() ... "
        + date_format.format(timestamp));

      client.setSessionId(null);
      if (client.getSessionId() == null)
        addToLog("    2.1. Test Passed");
      else {
        addToLog("    2.1. Test Failed");
        thisTestFailed();
      }
      
	    //
	    // 3.1. Test assignCuis()
	    //
      addToLog(
        "    3.1. Test assignCuis() ... "
        + date_format.format(timestamp));

      addToLog("    >>>> Working but test had to skipped due to long time to process.");

      //String cui = client.assignCuis();
      //if (cui != null)
      //  addToLog("    3.1. Test Passed");
      //else {
      //  addToLog("    3.1. Test Failed");
      //  thisTestFailed();
      //}

	    //
	    // 3.2. Test assignCuis(WorkLog)
	    //
      addToLog(
        "    3.2. Test assignCuis(WorkLog) ... "
        + date_format.format(timestamp));
      
      addToLog("    >>>> Working but test had to skipped due to long time to process.");

      //cui = client.assignCuis(new WorkLog(0));
      //if (cui != null)
      //  addToLog("    3.2. Test Passed");
      //else {
      //  addToLog("    3.2. Test Failed");
      //  thisTestFailed();
      //}
      
	    //
	    // 3.3. Test assignCuis(Concept)
	    //
      addToLog(
        "    3.3. Test assignCuis(Concept) ... "
        + date_format.format(timestamp));

      Concept source = new Concept.Default();
      source.setIdentifier(new Identifier.Default(794586));
      CUI source_cui = new CUI("C0556985");
      source.setCUI(source_cui);
      client.assignCuis(source);

	    //
	    // 3.4. Test assignCuis(Concept, Concept)
	    //
      addToLog(
        "    3.4. Test assignCuis(Concept, Concept) ... "
        + date_format.format(timestamp));

      Concept target = new Concept.Default();
      target.setIdentifier(new Identifier.Default(794579));
      CUI target_cui = new CUI("C0556979");
      target.setCUI(target_cui);
      client.assignCuis(source, target);

	    //
	    // 4.1. Test executeQuery(String)
	    //
      addToLog(
        "    4.1. Test executeQuery(String) ... "
        + date_format.format(timestamp));

      String query = "SELECT * FROM concept_status WHERE concept_id = 2222";
      client.executeQuery(query);

	    //
	    // 4.2. Test executeQuery(String, String)
	    //
      addToLog(
        "    4.2. Test executeQuery(String, String) ... "
        + date_format.format(timestamp));

      query = "SELECT * FROM concept_status WHERE concept_id = 2222";
      String inverse_query = "SELECT * FROM concept_status WHERE concept_id = 2223";
      client.executeQuery(query, inverse_query);

	    //
	    // 5.1. Test loadTable(String)
	    //
      addToLog(
        "    5.1. Test loadTable(String) ... "
        + date_format.format(timestamp));

      String la_value = admin_client.getSystemStatus("log_actions");
      admin_client.setSystemStatus("log_actions", "ON");
      String table_name = "tmp_inverse_relationships";
      client.executeQuery("BEGIN MEME_UTILITY.drop_it('table', '" + table_name + "'); END;");
      client.executeQuery("CREATE table " + table_name + " AS SELECT * FROM inverse_relationships");
      client.loadTable(table_name);
      client.executeQuery("BEGIN MEME_UTILITY.drop_it('table', '" + table_name + "'); END;");
      admin_client.setSystemStatus("log_actions", la_value);
      //admin_client.setSystemStatus("log_actions", "OFF");

	    //
	    // 6.1. Test exec(String[], String[])
	    //
      addToLog(
          "    6.1. Test exec(String[], String[]) ... "
        + date_format.format(timestamp));
      addToLog(
          "        Cannot test in platform-independent way"
        + date_format.format(timestamp));
      //String[] command = new String[] { "C:\\Program Files\\Internet Explorer\\iexplore.exe" };
      //client.exec(command, new String[0]);

	    //
	    // 7.1. Test logOperation(String, String, String, int, int, int)
	    //
      addToLog(
        "    7.1. Test logOperation(String, String, String, int, int, int) ... "
        + date_format.format(timestamp));
      String authority = "MTH";
      String activity = "LOG_OPERATION";
      String detail = "This action logs the operation";
      int transaction_id = 0;
      int work_id = 0;
      int elapsed_time = 0;
      client.logOperation(new Authority.Default(authority), activity, detail,
                         new MolecularTransaction(transaction_id),
                         new WorkLog(work_id), elapsed_time);

	    //
	    // 8.1. Test logProgress(String, String, String, int, int, int)
	    //
      addToLog(
        "    8.1. Test logProgress(String, String, String, int, int, int) ... "
        + date_format.format(timestamp));
      activity = "LOG_PROGRESS";
      detail = "This action logs the progress";
      client.logProgress(new Authority.Default(authority), activity, detail,
                         new MolecularTransaction(transaction_id),
                         new WorkLog(work_id), elapsed_time);

	    //
	    // 9.1. Test resetProgress(int)
	    //
      addToLog(
        "    9.1. Test resetProgress(int) ... "
        + date_format.format(timestamp));
      activity = "RESET_PROGRESS";
      detail = "This action resets the progress";
      client.resetProgress(new WorkLog(work_id));

	    //
	    // 10.1. Test initializeMatrix(WorkLog)
	    //
      addToLog(
        "    10.1. Test initializeMatrix(WorkLog) ... "
        + date_format.format(timestamp));

      addToLog("    >>>> Working but test had to skipped due to long time to process.");
      //String log = client.initializeMatrix(new WorkLog(work_id));
      //addToLog("Matrix Initializer Log: " + log);

    } catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }

    addToLog("");

    if (this.isPassed())
      addToLog("    All tests passed");
    else
      addToLog("    At least one test did not complete successfully");

    //
    // Main Footer
    //

    addToLog("");

    addToLog("-------------------------------------------------------");
    addToLog("Finished MaintenanceClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}