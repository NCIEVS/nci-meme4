/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  TestSequence2.java
 *
 * Author:  tkao
 *
 * History:
 *   Nov 7, 2003: 1st Version.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.qa.sequence;

import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 *
 *This class test attribute and concept insertion, deletion, and undo
 */
public class TestSequence2
    extends TestSuite {

  private int concept_id_2 = 105;

  public TestSequence2() {
    setName("TestSuite2");
    setDescription("This test suite test the insert delete undo attribute and concept actions");
    setConceptId(104);
  }

  /**
   * perform the test
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      EditingClient client = getClient();
      Concept test_concept = client.getConcept(this.getConceptId());
      Atom test_atom = test_concept.getAtoms()[0];
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();

      Date timestamp = new Date(System.currentTimeMillis());
      addToLog("    0 Insert an atom level attribute... " +
               date_format.format(timestamp));
      //create a new attribute
      Attribute test_attr = TestSuiteUtils.createAttribute("Test Attribute",
          test_atom, client);
      test_attr.setConcept(test_concept);
      test_atom.addAttribute(test_attr);

      MolecularInsertAttributeAction insert_attr_action = new
          MolecularInsertAttributeAction(test_attr);
      client.processAction(insert_attr_action);

      //check for attr insert process
      if (test_attr.getIdentifier() == null)
        throw new MEMEException("Attribute insertion failed");

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    0.a Insert an attribute is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    1 Delete the attribute... " +
               date_format.format(timestamp));
      test_attr = client.getAttribute(test_attr.getIdentifier().intValue());
      MolecularDeleteAttributeAction delete_attr_action = new
          MolecularDeleteAttributeAction(test_attr);
      client.processAction(delete_attr_action);
      //check for attr deletion process
      if (client.getDeadAttribute(test_attr.getIdentifier().intValue()) == null)
        throw new MEMEException("Attribute deletion failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      1.a Attribute deletion is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    2 Undo step 1... " +
               date_format.format(timestamp));
      //check for attr delete undo process
      client.processUndo(delete_attr_action);
      if (client.getAttribute(test_attr.getIdentifier().intValue()) == null)
        throw new MEMEException("Undo attribute deletion failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      2.a Undo step 1 is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    3 Undo step 0... " +
               date_format.format(timestamp));
      //check for attr insert undo process
      client.processUndo(insert_attr_action);

      //will throw a nullpointer exception if attribute is not found
      try {
        client.getAttribute(test_attr.getIdentifier().intValue());
        throw new MEMEException("Undo attribute insertion failed");
      }
      catch (Exception e) {
      }
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      3.a Undo step 0 is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    4 Insert a new concept with 1 atom... " +
               date_format.format(timestamp));
      Concept concept2 = new Concept.Default();
      Atom test_atom2 = TestSuiteUtils.createAtom("Test Atom 2", client);
      test_atom2.setConcept(concept2);
      concept2.addAtom(test_atom2);
      MolecularInsertConceptAction insert_concept_action = new
          MolecularInsertConceptAction(concept2);
      client.processAction(insert_concept_action);
      //check for concept insertion
      if (concept2.getIdentifier() == null)
        throw new MEMEException("Concept insertion failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      4.a Concept insertion is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    5 Delete the new concept... " +
               date_format.format(timestamp));
      MolecularDeleteConceptAction delete_concept_action = new
          MolecularDeleteConceptAction(concept2);
      client.processAction(delete_concept_action);
      boolean concept_is_dead = false;
      try {
        client.getConcept(concept2.getIdentifier());
      }
      catch (MissingDataException e) {
        concept_is_dead = true;
      }
      if (!concept_is_dead)
        throw new MEMEException("Concept deletion failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      5.a Concept deletion is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    6 Undo step 5... " +
               date_format.format(timestamp));
      client.processUndo(delete_concept_action);
      concept_is_dead = false;
      try {
        client.getConcept(concept2.getIdentifier());
      }
      catch (MissingDataException e) {
        concept_is_dead = true;
      }
      if (concept_is_dead)
        throw new MEMEException("Undo concept deletion failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      6.a Undo step 5 is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    7 Undo step 4... " +
               date_format.format(timestamp));
      client.processUndo(insert_concept_action);
      concept_is_dead = false;
      try {
        client.getConcept(concept2.getIdentifier());
      }
      catch (MissingDataException e) {
        concept_is_dead = true;
      }
      if (!concept_is_dead)
        throw new MEMEException("Undo concept insertion failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      7.a Undo step 4 is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    8 Insert a concept level relationship... " +
               date_format.format(timestamp));
      //refresh concepts
      concept2 = client.getConcept(concept_id_2);
      test_concept = client.getConcept(test_concept);
      Relationship test_rel = TestSuiteUtils.createConceptLevelRelationship(
          client,
          test_concept, concept2);
      MolecularInsertRelationshipAction insert_rel_action = new
          MolecularInsertRelationshipAction(test_rel);
      client.processAction(insert_rel_action);

      //refresh data
      client.populateRelationships(test_concept);
      //retrieve the newly inserted relationship
      Relationship[] result_test_rel = test_concept.getRelationships();
      for (int i = 0; i < result_test_rel.length; i++) {
        if (result_test_rel[i].getIdentifier().intValue() ==
            test_rel.getIdentifier().intValue()) {
          test_rel = result_test_rel[i];
          break;
        }
      }

      if (test_rel.getRelatedConcept().getIdentifier().intValue() !=
          concept2.getIdentifier().intValue() ||
          test_rel.getConcept().getIdentifier().intValue() !=
          test_concept.getIdentifier().intValue())
        throw new MEMEException("Insert relationship failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      8.a Insert relationship is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    9 Undo step 8... " +
               date_format.format(timestamp));
      client.processUndo(insert_rel_action);
      boolean relationship_is_dead = true;
      //refresh data
      client.populateRelationships(test_concept);
      result_test_rel = test_concept.getRelationships();
      for (int i = 0; i < result_test_rel.length; i++) {
        if (result_test_rel[i].getIdentifier().intValue() ==
            test_rel.getIdentifier().intValue()) {
          test_rel = result_test_rel[i];
          relationship_is_dead = false;
          break;
        }
      }

      if (!relationship_is_dead)
        throw new MEMEException("Undo insert relationship failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      9.a Undo step 8 is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    10 Approve a concept... " +
               date_format.format(timestamp));
      //refresh concept
      test_concept = client.getConcept(test_concept);
      char previous_status = test_concept.getStatus();

      MolecularApproveConceptAction approve_concept_action = new
          MolecularApproveConceptAction(test_concept);
      client.processAction(approve_concept_action);
      test_concept = client.getConcept(test_concept);
      if (test_concept.getStatus() != 'R')
        throw new MEMEException("Approve concept action failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      10.a Approve concept is successful... " +
               date_format.format(timestamp));

      timestamp.setTime(System.currentTimeMillis());
      addToLog("    11 Undo step 10... " +
               date_format.format(timestamp));
      //revert back to the original state
      client.processUndo(approve_concept_action);
      test_concept = client.getConcept(test_concept.getIdentifier());
      if (test_concept.getStatus() != previous_status)
        throw new MEMEException("Undo change concept action failed");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      11.a Undo step 10 is successful... " +
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