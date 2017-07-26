/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  DummyClientTest
 *
 * 01/30/2006 RBE (1-763IU): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.client.DummyClient;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Dummy Client
 */
public class DummyClientTest extends TestSuite {

  public DummyClientTest() {
    setName("DummyClientTest");
    setDescription("Test Suite for Dummy Client");
  }

  /**
   * Perform Test Suite Core Data
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    //EditingClient client = getClient();

    DummyClient client = null;

    try {
      client = new DummyClient("apelon");

      addToLog(
          "    1.0. Test DummyClientTest: echo() ... "
          + date_format.format(timestamp));
      addToLog("");

      client.echo();

    } catch (MEMEException me) {
      thisTestFailed();
      addToLog(me);
      me.setPrintStackTrace(true);
      me.printStackTrace();
    }

    addToLog("");

    //
    // Main Footer
    //

    addToLog("");

    addToLog("-------------------------------------------------------");
    addToLog("Finished DummyClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}