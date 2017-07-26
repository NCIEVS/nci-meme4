/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  MergeEngineClientTest
 * 
 * 02/14/2006 RBE (1-79GGX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.client.ClientProgressEvent;
import gov.nih.nlm.meme.client.ClientProgressListener;
import gov.nih.nlm.meme.client.MergeEngineClient;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for MergeEngine
 */
public class MergeEngineClientTest extends TestSuite {

  public MergeEngineClientTest() {
    setName("MergeEngineClientTest");
    setDescription("Test Suite for MergeEngine");
  }

  /**
   * Perform Test Suite MergeEngine
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MergeEngineClient client = null;

    try {
      client = new MergeEngineClient("apelon");

	    //
	    // 1.1. Test setMidService(String), getMidService()
	    //
      addToLog("    1.1. Test setMidService(String), "
        + "getMidService() ... "
        + date_format.format(timestamp));

      client.setMidService("apelon");
      if (client.getMidService().equals("apelon"))
        addToLog("    1.1. Test Passed");
      else {
        addToLog("    1.1. Test Failed");
        thisTestFailed();
      }

	    //
	    // 2.1. Test processMergeSet(int, Authority, String)
	    //
      addToLog(
          "    2.1. Test processMergeSet(int, Authority, String) ... "
          + date_format.format(timestamp));

      // begin session
      client.initiateSession();
      
      addToLog("Session ID:    " + client.getSessionId());
      client.addClientProgressListener(new ClientProgressListener() {
          public void progressUpdated(ClientProgressEvent cpe) {
            addToLog(cpe.getMessage() + " " + new java.util.Date());
          }
        }
      );
      String log = client.processMergeSet(12345,
                     new Authority.Default("ALT2003"), "ALT2003-MID");
      addToLog(log);

	    //
	    // 3.1. Test getLog()
	    //
      addToLog(
          "    3.1. Test getLog() ... "
          + date_format.format(timestamp));
      
      log = client.getLog();
      if (log != null)
        addToLog("    3.1. Log: \n" + log);

	    //
	    // 3.2. Test getLogNotSeen()
	    //
      addToLog(
          "    3.2. Test getLogNotSeen() ... "
          + date_format.format(timestamp));
      
      log = client.getLogNotSeen();
      if (log != null)
        addToLog("    3.2. Log: \n" + log);
      
	    //
	    // 4.1. Test getSessionProgress()
	    //
      addToLog(
          "    4.1. Test getSessionProgress() ... "
          + date_format.format(timestamp));
      
      int session_progress = client.getSessionProgress();
      if (session_progress > 0)
        addToLog("    4.1. Session Progress: " + session_progress);
      
      // end session
      client.terminateSession();

    } catch (MEMEException me) {
      thisTestFailed();
      addToLog(me);
      me.setPrintStackTrace(true);
      me.printStackTrace();
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
    addToLog("Finished MergeEngineClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}