/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ui
 * Object:  TestSuiteRuiNlm03
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ui;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.RUI;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Rui NLM03
 */
public class TestSuiteRuiNlm03
    extends TestSuite {

  private int concept_id2 = 101;

  public TestSuiteRuiNlm03() {
    setName("TestSuiteRuiNlm03");
    setDescription("Test Suite for Rui NLM03");
    setConceptId(100);
  }

  /**
   * Perform Test Suite Rui NLM03
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //
      // Initial Setup
      //
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      Date timestamp = new Date(System.currentTimeMillis());

      EditingClient client = getClient();
      AdminClient ac = new AdminClient(client.getMidService());
      ac.disableMolecularActionValidation();
      Concept concept1 = client.getConcept(this.getConceptId());
      Concept concept2 = client.getConcept(concept_id2);

      //
      // 1a. Insert an atom
      //
      addToLog("    1a1. Insert an atom 1... " + date_format.format(timestamp));

      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup("MTH/PT"));
      atom.setSource(client.getSource("MTH"));
      atom.setCode(Code.newCode("NOCODE"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept1);

      MolecularAction ma1a1 = new MolecularInsertAtomAction(atom);
      client.processAction(ma1a1);

      // Save Transaction ID
      int tid1a1 = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a1);

      atom = client.getAtom(atom);

      //
      // 1a2. Insert an atom2
      //
      addToLog("    1a2. Insert an atom 2... " + date_format.format(timestamp));

      Atom atom2 = new Atom.Default();
      atom2.setString("TEST ATOM 2");
      atom2.setTermgroup(client.getTermgroup("MTH/PT"));
      atom2.setSource(client.getSource("MTH"));
      atom2.setCode(Code.newCode("NOCODE"));
      atom2.setStatus('R');
      atom2.setGenerated(true);
      atom2.setReleased('N');
      atom2.setTobereleased('Y');
      atom2.setSuppressible("N");
      atom2.setConcept(concept2);

      MolecularAction ma1a2 = new MolecularInsertAtomAction(atom2);
      client.processAction(ma1a2);

      // Save Transaction ID
      int tid1a2 = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a2);

      atom2 = client.getAtom(atom2);

      //
      // 1b. Insert a relationship
      //
      addToLog("    1b. Insert a relationship... " +
               date_format.format(timestamp));
      addToLog("        RUI should not be null");

      Relationship rel = new Relationship.Default();
      rel.setConcept(concept1);
      rel.setRelatedConcept(concept2);
      rel.setAtom(atom);
      rel.setRelatedAtom(atom2);
      rel.setName("RT?");
      rel.setAttribute("mapped_to");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('R');
      rel.setGenerated(false);
      rel.setLevel('C');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(null);
      rel.setRelatedNativeIdentifier(null);

      MolecularAction ma1b = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma1b);

      // Save Transaction ID
      int tid1b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1b);

      // Re-read concept
      concept1 = client.getConcept(concept1);

      // Re-read relationship
      int rel_id = 0;
      Relationship[] rels = concept1.getRelationships();
      for (int i = 0; i < rels.length; i++) {
        if (rels[i].isReleasable()) {
          rel_id = rels[i].getIdentifier().intValue();
        }
      }

      Identifier id = client.getMaxIdentifierForType(Relationship.class);
      rel_id = id.intValue();

      rel = client.getRelationship(rel_id);

      // Look Up Rui S
      RUI rui1 = rel.getRUI();

      // how to get the inverse rui
      addToLog("        rui: " + rui1.toString());
      addToLog("        relationship_id: " + rel.getIdentifier().intValue());

      // Compare Rui
      if (rui1 != null)
        addToLog("    1b. Test Passed");
      else {
        addToLog("    1b. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Test changing the source to NLM03
      //
      addToLog("    2a. Test change the source of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI should change");

      rel.setSource(client.getSource("NLM03"));
      MolecularAction ma2a = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma2a);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      // Look Up Rui NLM03
      rel = client.getRelationship(rel_id);
      RUI rui2 = rel.getRUI();

      addToLog("        rui: " + rui2.toString());

      // Compare Rui
      if (!rui1.equals(rui2))
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      //
      // 2b. Test changing the source back
      //
      addToLog("    2b. Test change source of the relationships back ... "
               + date_format.format(timestamp));
      addToLog("        RUI should return to initial value");
      rel.setSource(client.getSource("MTH"));
      MolecularAction ma2b = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma2b);

      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      // Look Up Rui NLM03
      rel = client.getRelationship(rel_id);
      RUI rui3 = rel.getRUI();
      addToLog("        rui: " + rui3.toString());

      // Compare Rui
      if (rui1.equals(rui3))
        addToLog("    2b. Test Passed");
      else {
        addToLog("    2b. Test Failed");
        thisTestFailed();
      }

      //
      // 2c. Undo 2b
      //
      addToLog("    2c. Test undo step 2b ... "
               + date_format.format(timestamp));
      addToLog("        RUI should not match initial value");

      MolecularAction ma2c = ma2b;
      ma2c.setTransactionIdentifier(tid2b);
      client.processUndo(ma2c);

      // Look Up Rui NLM03
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();
      addToLog("        rui: " + rui3.toString());

      // Compare Rui
      if (!rui1.equals(rui3) && rui2.equals(rui3))
        addToLog("    2c. Test Passed");
      else {
        addToLog("    2c. Test Failed");
        thisTestFailed();
      }

      //
      // 2d. Undo 2a
      //
      addToLog("    2d. Test undo step 2a ... "
               + date_format.format(timestamp));
      addToLog("        RUI should match initial value");

      MolecularAction ma2d = ma2a;
      ma2d.setTransactionIdentifier(tid2a);
      client.processUndo(ma2d);

      // Look Up Rui NLM03
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();
      addToLog("        rui: " + rui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && !rui2.equals(rui3))
        addToLog("    2d. Test Passed");
      else {
        addToLog("    2d. Test Failed");
        thisTestFailed();
      }

      //
      // 3a. Undo 1b
      //
      addToLog("    3a. Test undo the 1b ... "
               + date_format.format(timestamp));

      MolecularAction ma3a = ma1b;
      ma3a.setTransactionIdentifier(tid1b);
      client.processUndo(ma3a);

      //
      // 3b. Undo 1a2
      //
      addToLog("    3b. Test undo the 1a2 ... "
               + date_format.format(timestamp));

      MolecularAction ma3b = ma1a2;
      ma3b.setTransactionIdentifier(tid1a2);
      client.processUndo(ma3b);

      //
      // 3c. Undo 1a1
      //
      addToLog("    3c. Test undo the 1a1 ... "
               + date_format.format(timestamp));

      MolecularAction ma3c = ma1a1;
      ma3c.setTransactionIdentifier(tid1a1);
      client.processUndo(ma3c);

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
      addToLog("Finished TestSuiteRuiNlm03 at " +
               date_format.format(new Date(System.currentTimeMillis())));
      addToLog("-------------------------------------------------------");

    }
    catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }

}