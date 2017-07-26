/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  AdminClientTest
 * 
 * 03/22/2007 BAC (1-D0BIJ): removed calls to authenticate based on hardcoded passwords
 * 12/07/2006 BAC (1-D0BIJ): Fix to refreshMidService call to use real value
 * 06/19/2006 RBE (1-BIC23): Bug fixes
 * 01/30/2006 RBE (1-763IU): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Admin Client
 */
public class AdminClientTest extends TestSuite {

  public AdminClientTest() {
    setName("AdminClientTest");
    setDescription("Test Suite for Admin Client");
  }

  /**
   * Perform Test Suite Admin Client
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    AdminClient client = null;

    try {
      client = new AdminClient("");

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
      // 1.2. Test getServerVersion()
      //
      addToLog("    1.2. Test getServerVersion() ... "
          + date_format.format(timestamp));

      String server_ver = client.getServerVersion();
      if (server_ver != null) {
        addToLog("    1.2. Test Passed");
        addToLog("    1.2. Server Version: " + server_ver);
      } else {
        addToLog("    1.2. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.3. Test getLog()
      //
      addToLog("    1.3. Test getLog() ... "
          + date_format.format(timestamp));

      String log = client.getLog();
      if (log != null) {
        addToLog("    1.3. Test Passed");
        addToLog("    1.3. Server Log = " + log);
      } else {
        addToLog("    1.3. Test Failed");
        thisTestFailed();
      }

      //
      // 1.4. Test setHead(int), setTail(int), initiateSession()
      //
      addToLog("    1.4. Test setHead(int), setTail(int), ititiateSession() ... "
          + date_format.format(timestamp));

      client.setHead(10);
      client.setTail(15);
      client.initiateSession();

      //
      // 1.5. Test getSessionLog(String)
      //
      addToLog("    1.5. Test getSessionLog(String) ... "
          + date_format.format(timestamp));

      String session_log = client.getSessionLog(client.getSessionId());
      if (session_log != null) {
        addToLog("    1.5. Test Passed");
        addToLog("    1.5. Session Log: " + session_log);
      } else {
        addToLog("    1.5. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.6. Test getSessionProgress(String)
      //
      addToLog("    1.6. Test getSessionProgress(String) ... "
          + date_format.format(timestamp));

      int progress = client.getSessionProgress(client.getSessionId());
      if (session_log != null) {
        addToLog("    1.6. Test Passed");
        addToLog("    1.6. Session Progress: " + progress);
      } else {
        addToLog("    1.6. Test Failed");
        thisTestFailed();
      }

      //
      // 1.7. Test getSessionLogNotSeen()
      //
      addToLog("    1.7. Test getSessionLogNotSeen() ... "
          + date_format.format(timestamp));

      for (int i=0; i<1; i++) {
        session_log = client.getSessionLogNotSeen(client.getSessionId());
      }

      int t_id = 0;
      if (session_log.length() > 206)
        t_id = Integer.parseInt(session_log.substring(200, 207));

      if (session_log != null) {
        addToLog("    1.7. Test Passed");
        addToLog("    1.7. Session Log Not Seen: " + session_log);
      }
      else {
        addToLog("    1.7. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.8. Test getTransactionLog(int)
      //
      addToLog("    1.8. Test getTransactionLog(int) ... "
          + date_format.format(timestamp));

      if (t_id > 0) {
        log = client.getTransactionLog(t_id);
        if (log != null) {
          addToLog("    1.8. Test Passed");
          addToLog("    1.8. Transaction Log: " + log);
        }
        else {
          addToLog("    1.8. Test Failed");
          thisTestFailed();
        }
      } else
        addToLog("    1.8. Transaction Log: " + log);

      client.terminateSession();

      //
      // Test Statistics API
      //
      
      //
      // 1.9. Test getStatisticsReport()
      //
      addToLog("    1.9. Test getStatisticsReport() ... "
          + date_format.format(timestamp));

      String stat_report = client.getStatisticsReport();
      if (stat_report != null) {
        addToLog("    1.9. Test Passed");
        //addToLog("    1.9. Statistics Report: " + stat_report);
      } else {
        addToLog("    1.9. Test Failed");
        thisTestFailed();
      }
      
      //
      // Dummy API
      //      

      //
      // 1.10. Test ping()
      //
      addToLog("    1.10. Test ping() ... "
          + date_format.format(timestamp));

      String ping = client.ping();
      if (ping != null) {
        addToLog("    1.10. Test Passed");
        addToLog("    1.10. Ping: " + ping);
      } else {
        addToLog("    1.10. Test Failed");
        thisTestFailed();
      }

      //
      // 1.11. Test runActionSequence()
      //
      
      //
      // Refresh API
      //

      //
      // 1.12. Test refreshCaches()
      //
      addToLog("    1.12. Test refreshCaches() ... "
          + date_format.format(timestamp));

      client.refreshCaches();

      //
      // 1.13. Test refreshMidServices()
      //
      addToLog("    1.13. Test refreshMidServices() ... "
          + date_format.format(timestamp));
      addToLog("      avoid this test for now..."
          + date_format.format(timestamp));

      //String db = MIDServices.getService("testsrc-db");
      //client.refreshMidService(db);

      //
      // Shutdown API
      //

      //
      // 1.14. Test shutdownServer()
      //
      //addToLog("    1.14. Test shutdownServer() ... "
      //    + date_format.format(timestamp));

      //client.shutdownServer();

      //
      // 1.15. Test killServer()
      //
      //addToLog("    1.15. Test killServer() ... "
      //    + date_format.format(timestamp));

      //client.killServer();
      
      //
      // Integrity API
      //
      
      //
      // 1.16. Test enableIntegritySystem(), isIntegritySystemEnabled()
      //
      addToLog("    1.16. Test enableIntegritySystem(), isIntegritySystemEnabled() ... "
          + date_format.format(timestamp));

      client.enableIntegritySystem();
      if (client.isIntegritySystemEnabled())
        addToLog("    1.16. Test Passed");
      else {
        addToLog("    1.16. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.17. Test disableIntegritySystem(), isIntegritySystemEnabled()
      //
      addToLog("    1.17. Test disableIntegritySystem(), isIntegritySystemEnabled() ... "
          + date_format.format(timestamp));

      client.disableIntegritySystem();
      if (!client.isIntegritySystemEnabled())
        addToLog("    1.17. Test Passed");
      else {
        addToLog("    1.17. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.18. Test enableEditing(), isEditingEnabled()
      //
      addToLog("    1.18. Test enableEditing(), isEditingEnabled() ... "
          + date_format.format(timestamp));

      client.enableEditing();
      if (client.isEditingEnabled())
        addToLog("    1.18. Test Passed");
      else {
        addToLog("    1.18. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.19. Test disableEditing(), isEditingEnabled()
      //
      addToLog("    1.19. Test disableEditing(), isEditingEnabled() ... "
          + date_format.format(timestamp));

      client.disableEditing();
      if (!client.isEditingEnabled())
        addToLog("    1.19. Test Passed");
      else {
        addToLog("    1.19. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.20. Test enableAtomicActionValidation(), isAtomicActionValidationEnabled()
      //
      addToLog("    1.20. Test enableAtomicActionValidation(), "
          + "isAtomicActionValidationEnabled() ... "
          + date_format.format(timestamp));

      client.enableAtomicActionValidation();
      if (client.isAtomicActionValidationEnabled())
        addToLog("    1.20. Test Passed");
      else {
        addToLog("    1.20. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.21. Test disableAtomicActionValidation(), isAtomicActionValidationEnabled()
      //
      addToLog("    1.21. Test disableAtomicActionValidation(), "
          + "isAtomicActionValidationEnabled() ... "
          + date_format.format(timestamp));

      client.disableAtomicActionValidation();
      if (!client.isAtomicActionValidationEnabled())
        addToLog("    1.21. Test Passed");
      else {
        addToLog("    1.21. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.22. Test enableMolecularActionValidation(), isMolecularActionValidationEnabled()
      //
      addToLog("    1.22. Test enableMolecularActionValidation(), "
          + "isMolecularActionValidationEnabled() ... "
          + date_format.format(timestamp));

      client.enableMolecularActionValidation();
      if (client.isMolecularActionValidationEnabled())
        addToLog("    1.22. Test Passed");
      else {
        addToLog("    1.22. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.23. Test disableMolecularActionValidation(), isMolecularActionValidationEnabled()
      //
      addToLog("    1.23. Test disableMolecularActionValidation(), "
          + "isMolecularActionValidationEnabled() ... "
          + date_format.format(timestamp));

      client.disableMolecularActionValidation();
      if (!client.isMolecularActionValidationEnabled())
        addToLog("    1.23. Test Passed");
      else {
        addToLog("    1.23. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1.24. Test authenticate(String, String)
      //
      addToLog("    1.24. Test authenticate(String, String) ... "
          + date_format.format(timestamp));
      addToLog("        cannot test due to changing passwords"
      + date_format.format(timestamp));


      //
      // 1.25. Test setSystemStatus(String, String), getSystemStatus()
      //
      addToLog("    1.25. Test setSystemStatus(String, String), getSystemStatus() ... "
          + date_format.format(timestamp));

      client.setSystemStatus("log_actions", "OFF");
      if (client.getSystemStatus("log_actions").equals("OFF"))
        addToLog("    1.25. Test Passed");
      else {
        addToLog("    1.25. Test Failed");
        thisTestFailed();
      }

      //
      // 1.26. getPasswordExpirationDate(String, String)
      //
      addToLog("    1.26. Test getPasswordExpirationDate(String, String) ... "
          + date_format.format(timestamp));

      Date expiry = client.getPasswordExpirationDate(null,null);
      if (expiry == null)
        addToLog("    1.26. Test Passed, expiry = null");
      else {
        addToLog("    1.26. Test Passed, expiry = " + expiry);
        thisTestFailed();
      }
      
      //
      // changePassword, changePasswordForUser
      //

    } catch(MEMEException e) {
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
    addToLog("Finished AdminClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}