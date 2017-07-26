/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ApproveConceptActionTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Nov 24, 2003: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 *
 *This test the MolecularApproveConceptAction class
 */
public class ApproveConceptActionTest
    extends TestSuite {

  private int concept_id2 = 120;

  public ApproveConceptActionTest() {
    setName("ApproveConceptActionTest");
    setDescription("This test the MolecularApproveConceptAction");
    setConceptId(112);
  }

  /**
   * Perform tests
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Concept test_concept = client.getConcept(this.getConceptId());

      Atom test_atom = test_concept.getAtoms()[0];
      Attribute test_attr = TestSuiteUtils.createAttribute("Test Attribute",
          test_atom, client);
      test_attr.setConcept(test_concept);

      MolecularInsertAttributeAction insert_attr_action = new
          MolecularInsertAttributeAction(test_attr);
      client.processAction(insert_attr_action);

      //refresh data
      test_concept = client.getConcept(test_concept);

      Date timestamp = new Date(System.currentTimeMillis());
      addToLog(
          "    0 Base case, 1 concept with 1 atom 1 attribute... " +
          date_format.format(timestamp));

      MolecularApproveConceptAction action = new MolecularApproveConceptAction(
          test_concept);
      client.processAction(action);

      test_concept = client.getConcept(this.getConceptId());
      test_atom = client.getAtom(test_atom);
      test_attr = client.getAttribute(test_attr.getIdentifier().intValue());
      if (test_concept.getStatus() != 'R')
        throw new MEMEException("Concept status did not change to R");
      if (test_atom.getStatus() != 'R')
        throw new MEMEException("Atom status did not change to R");
      if (test_attr.getStatus() != 'R')
        throw new MEMEException("Attribute status did not change to R");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      0 success... " + date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    1 Undo step 0... " +
               date_format.format(timestamp));
      client.processUndo(action);
      test_concept = client.getConcept(this.getConceptId());
      if (test_concept.getStatus() != 'N')
        throw new MEMEException("Concept status did not revert to N");
      if (test_atom.getStatus() != 'R')
        throw new MEMEException("Atom status did not revert to N");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      1 success... " + date_format.format(timestamp));
      client.processUndo(insert_attr_action);

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    2 Approve concept level relationship... " +
               date_format.format(timestamp));
      Relationship concept_level_relationship = TestSuiteUtils.
          createConceptLevelRelationship(client, test_concept,
                                         client.getConcept(concept_id2));
      MolecularInsertRelationshipAction insert_rel_action = new
          MolecularInsertRelationshipAction(concept_level_relationship);
      client.processAction(insert_rel_action);
      client.processAction(action);

      concept_level_relationship = client.getRelationship(
          concept_level_relationship.getIdentifier().intValue());
      if (concept_level_relationship.getStatus() != 'R')
        throw new MEMEException("Relationship status did not change to R");
      if (!concept_level_relationship.getName().equals("RT"))
        throw new MEMEException("Relationship name did not change to RT");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      2 success... " + date_format.format(timestamp));
      client.processUndo(action);
      client.processUndo(insert_rel_action);
      concept_level_relationship = null;
      insert_rel_action = null;

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