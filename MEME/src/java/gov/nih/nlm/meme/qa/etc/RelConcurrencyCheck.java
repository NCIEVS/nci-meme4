/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  RelConcurrencyCheck.java
 *
 * Author:  tkao
 *
 * History:
 *   Dec 10, 2003: 1st Version.
 *
 *****************************************************************************/


package gov.nih.nlm.meme.qa.etc;

import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * This check will get a concept, spurn off 4 threads to populate/retrieve relationships
     * to test the server's ability to handle calls from the same client and session
 */

public class RelConcurrencyCheck
    extends TestSuite {

  private int number_of_threads = 4;

  public RelConcurrencyCheck() {
    setName("RelConcurrencyCheck");
    setDescription("This test the server's ability to handle populate rel calls from the same "
        + "client and session");
    setConceptId(101);
  }

  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Concept test_concept = client.getConcept(this.getConceptId());

      Date timestamp = new Date(System.currentTimeMillis());
      addToLog("    0 Begin Test... " +
               date_format.format(timestamp));
      Thread t = null;
      Vector list_of_threads = new Vector();
      try {
        for (int i = 0; i < number_of_threads; i++) {
          t = new TestRel(client, test_concept, new Boolean(this.isPassed()));
          t.start();
          list_of_threads.add(t);
        }
        Thread.sleep(500);
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new MEMEException(
            "Server can not handle multiple threads from the same session");
      }

      timestamp.setTime(System.currentTimeMillis());
      addToLog("      0 success... " + date_format.format(timestamp));
      addToLog(this.getName() + " passed");
    }
    catch (MEMEException e) {
      thisTestFailed();
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }

  class TestRel
      extends Thread {
    EditingClient client = null;
    Concept test_concept = null;

    /**
     * Constructor for the inner class
     * @param client
     * @param test_concept
     */
    TestRel(EditingClient client, Concept test_concept, Boolean passed) {
      this.client = client;
      this.test_concept = test_concept;
    }

    /**
     * Calls populateRelationships and getRelationshipCount
     */
    public void run() {
      try {
        client.populateRelationships(test_concept);
        client.getRelationshipCount(test_concept);
      }
      catch (MEMEException e) {
        thisTestFailed();
        addToLog(e);
        e.setPrintStackTrace(true);
        e.printStackTrace();
      }
    }
  }

}