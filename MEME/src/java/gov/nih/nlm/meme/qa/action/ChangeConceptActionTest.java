/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ChangeConceptActionTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Dec 30, 2003: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularChangeConceptAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * This test MolecularChangeConceptAction
 */

public class ChangeConceptActionTest
    extends TestSuite {

  public ChangeConceptActionTest() {
    setName("ChangeConceptActionTest");
    setDescription("This test the MolecularChangeConceptAction");
    setConceptId(130);
  }

  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Concept test_concept = client.getConcept(this.getConceptId());

      unitTest(date_format, client, test_concept, 'R');
      unitTest(date_format, client, test_concept, 'N');

      addToLog(this.getName() + " passed");
    }
    catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }

  /**
   * This method test the status field
   * @param date_format
   * @param client
   * @param test_concept
   * @param status
   * @throws MEMEException
   */
  private void unitTest(SimpleDateFormat date_format, EditingClient client,
                        Concept test_concept, char status) throws
      MEMEException {
    Date timestamp = new Date(System.currentTimeMillis());
    addToLog(
        "    Status = " + status + "; " +
        date_format.format(timestamp));

    test_concept = client.getConcept(test_concept);

    char pre_status = test_concept.getStatus();

    test_concept.setStatus(status);

    MolecularChangeConceptAction action = new
        MolecularChangeConceptAction(test_concept);

    client.processAction(action);
    test_concept = client.getConcept(test_concept);

    if (test_concept.getStatus() != status)
      throw new MEMEException("Status did not change to " + status);

    addToLog("     undo...");
    client.processUndo(action);
    test_concept = client.getConcept(test_concept);

    if (test_concept.getStatus() != pre_status)
      throw new MEMEException("Status did not undo to " + pre_status);

    timestamp.setTime(System.currentTimeMillis());
    addToLog("      success... " + date_format.format(timestamp));
  }
}