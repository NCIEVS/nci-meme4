/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ChangeAttributeActionTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Dec 31, 2003: 1st Version.
 *
 *****************************************************************************/


package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * This test the MolecularChangeRelationshipAction class
 */

public class ChangeRelationshipActionTest
    extends TestSuite{

  private int concept_id_2 = 102;

  public ChangeRelationshipActionTest() {
    setName("ChangeRelationshipActionTest");
    setDescription("This test the MolecularChangeRelationshipAction");
    setConceptId(101);
  }

  /**
   * runs the test for MolecularChangeRelationshipAction
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Concept test_concept = client.getConcept(this.getConceptId());
      Concept test_concept_2 = client.getConcept(concept_id_2);
      //insert a concept leve relationship
      Relationship test_rel = TestSuiteUtils.createConceptLevelRelationship(
          client, test_concept, test_concept_2);
      MolecularInsertRelationshipAction insert_rel_action = new
          MolecularInsertRelationshipAction(test_rel);
      client.processAction(insert_rel_action);
      //save test_rel attributes
      char previous_tbr = test_rel.getTobereleased();
      String previous_suppressible = test_rel.getSuppressible();
      char previous_status = test_rel.getStatus();

      Date timestamp = new Date(System.currentTimeMillis());
      addToLog(
          "    0 base case, test name, released, suppressible, status attributes... " +
          date_format.format(timestamp));

      test_rel.setTobereleased('Y');
      test_rel.setSuppressible("E");
      test_rel.setStatus('R');

      MolecularChangeRelationshipAction action = new
          MolecularChangeRelationshipAction(test_rel);
      client.processAction(action);

      test_rel = client.getRelationship(test_rel.getIdentifier().intValue());
      if (test_rel.getTobereleased() != 'Y')
        throw new MEMEException("toBeReleased did not change");
      if (test_rel.getStatus() != 'R')
        throw new MEMEException("Status did not change");
      if (!test_rel.isSuppressible())
        throw new MEMEException("Suppressible did not change");

      timestamp.setTime(System.currentTimeMillis());
      addToLog("      0 success... " +
                         date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    1 undo step 0... " +
                         date_format.format(timestamp));

      client.processUndo(action);

      test_rel = client.getRelationship(test_rel.getIdentifier().intValue());
      if (test_rel.getTobereleased() != previous_tbr)
        throw new MEMEException("toBeReleased did not undo");
      if (test_rel.getStatus() != previous_status)
        throw new MEMEException("Status did not undo");
      if (!test_rel.getSuppressible().equals(previous_suppressible))
        throw new MEMEException("Suppressible did not undo");

      timestamp.setTime(System.currentTimeMillis());
      addToLog("      1 success... " +
                         date_format.format(timestamp));

      client.processUndo(insert_rel_action);

      addToLog(this.getName() + " passed");
    }
    catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }
}