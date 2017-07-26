/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  InsertDeleteActionTests.java
 *
 * Author:  TK
 *
 * History:
 *   1/05/2004: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
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

public class InsertDeleteActionTests
    extends TestSuite{

  private int concept_id_2 = 102;

  public InsertDeleteActionTests() {
    setName("InsertDeleteActionTests");
    setDescription("This test atom, attribute, concept, and relationship insert and delete actions");
    setConceptId(101);
  }

  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Atom test_atom = TestSuiteUtils.createAtom("Test Atom", client);
      Concept test_concept = client.getConcept(this.getConceptId());
      Concept test_concept2 = client.getConcept(concept_id_2);
      Relationship test_rel = TestSuiteUtils.createConceptLevelRelationship(
          client, test_concept, test_concept2);
      Attribute test_attr = TestSuiteUtils.createAttribute("QA Attribute",
          test_atom, client);
      MolecularInsertAtomAction insert_atom_action = new
          MolecularInsertAtomAction(test_atom);
      MolecularInsertRelationshipAction insert_rel_action = new
          MolecularInsertRelationshipAction(test_rel);
      MolecularInsertAttributeAction insert_attr_action = new
          MolecularInsertAttributeAction(test_attr);
      client.processAction(insert_atom_action);
      client.processAction(insert_rel_action);
      client.processAction(insert_attr_action);

      Date timestamp = new Date(System.currentTimeMillis());
      addToLog("    Insert a concept.. " +
                         date_format.format(timestamp));
      //create a new concept
      Concept new_concept = new Concept.Default();
      Atom new_atom = TestSuiteUtils.createAtom("QA Atom", client);
      new_atom.setConcept(new_concept);
      new_concept.addAtom(new_atom);

      MolecularInsertConceptAction insert_concept_action = new
          MolecularInsertConceptAction(new_concept);
      client.processAction(insert_concept_action);


      timestamp.setTime(System.currentTimeMillis());
      addToLog("    Delete a concept.. " +
                         date_format.format(timestamp));

      MolecularDeleteConceptAction delete_concept_action = new
          MolecularDeleteConceptAction(test_concept);
      client.processAction(delete_concept_action);
      try {
        client.getDeadConcept(test_concept.getIdentifier().intValue());
      }
      catch (NullPointerException e) {
        throw new MEMEException("Delete concept failed");
      }

      addToLog("      undo...");
      client.processUndo(delete_concept_action);
      try {
        client.getConcept(test_concept.getIdentifier().intValue());
      }
      catch (NullPointerException e) {
        throw new MEMEException("Undo delete concept failed");
      }
      timestamp.setTime(System.currentTimeMillis());
      addToLog("        success... " +
                         date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    Delete an attribute.. " +
                         date_format.format(timestamp));
      MolecularDeleteAttributeAction delete_attr_action = new
          MolecularDeleteAttributeAction(test_attr);
      client.processAction(delete_attr_action);

      try {
        client.getDeadAttribute(test_attr.getIdentifier().intValue());
      }
      catch (NullPointerException e) {
        throw new MEMEException("Delete attribute failed");
      }

      addToLog("      undo...");
      client.processUndo(delete_attr_action);
      try {
        client.getAttribute(test_attr.getIdentifier().intValue());
      }
      catch (NullPointerException e) {
        throw new MEMEException("Undo delete attribute failed");
      }
      client.processAction(delete_attr_action);
      timestamp.setTime(System.currentTimeMillis());
      addToLog("        success... " +
                         date_format.format(timestamp));

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