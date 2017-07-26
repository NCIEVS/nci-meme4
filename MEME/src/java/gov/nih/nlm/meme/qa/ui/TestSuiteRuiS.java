/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ui
 * Object:  TestSuiteRuiS
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
 * Test suite for Rui S
 */
public class TestSuiteRuiS extends TestSuite {

  private int concept_id2 = 101;
  private int concept_id3 = 102;

  public TestSuiteRuiS() {
    setName("TestSuiteRuiS");
    setDescription("Test Suite for Rui S");
    setConceptId(100);
  }

  /**
   * Perform Test Suite Rui S
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
      Concept concept3 = client.getConcept(concept_id3);

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

      // Re-read atom
      atom = client.getAtom(atom.getIdentifier());

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

      // Re-read atom
      atom2 = client.getAtom(atom2.getIdentifier());

      //
      // 1a3. Insert an atom3
      //
      addToLog("    1a3. Insert an atom 3... " + date_format.format(timestamp));

      Atom atom3 = new Atom.Default();
      atom3.setString("TEST ATOM 3");
      atom3.setTermgroup(client.getTermgroup("MTH/PT"));
      atom3.setSource(client.getSource("MTH"));
      atom3.setCode(Code.newCode("NOCODE"));
      atom3.setStatus('R');
      atom3.setGenerated(true);
      atom3.setReleased('N');
      atom3.setTobereleased('Y');
      atom3.setSuppressible("N");
      atom3.setConcept(concept3);

      MolecularAction ma1a3 = new MolecularInsertAtomAction(atom3);
      client.processAction(ma1a3);

      // Save Transaction ID
      int tid1a3 = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a3);

      // Re-read atom
      atom3 = client.getAtom(atom3.getIdentifier());

      //
      // 1b. Insert a relationship
      //
      addToLog("    1b. Insert a relationship... " + date_format.format(timestamp));

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
      rel.setLevel('S');
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
      for (int i=0; i<rels.length; i++) {
        if (rels[i].isReleasable()) {
          rel_id = rels[i].getIdentifier().intValue();
        }
      }

      Identifier id = client.getMaxIdentifierForType(Relationship.class);
      rel_id = id.intValue();

      rel = client.getRelationship(rel_id);
      RUI rui1 = rel.getRUI();

      Relationship inverse_rel = client.getInverseRelationship(rel_id);
      RUI irui1 = inverse_rel.getRUI();

      // how to get the inverse rui
      addToLog("        relationship_id: " + rel.getIdentifier().intValue());
      addToLog("        rui: " + rui1.toString());
      addToLog("        irui: " + irui1.toString());

      //
      // 2a. Test changing the source
      //
      addToLog("    2a. Test change the source of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should change");

      rel.setSource(client.getSource("SRC"));
      MolecularAction ma2a = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma2a);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      RUI rui2 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      RUI irui2 = inverse_rel.getRUI();

      addToLog("        rui: " + rui2.toString());
      addToLog("        irui: " + irui2.toString());

      // Compare Rui and inverse Rui
      if (!rui1.equals(rui2) && !irui1.equals(irui2))
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
      addToLog("        RUI and inverse RUI should return to initial value");
      rel.setSource(client.getSource("MTH"));
      MolecularAction ma2b = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma2b);

      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      RUI rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      RUI irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && !irui2.equals(irui3))
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
      addToLog("        RUI and inverse RUI should not match initial value");

      MolecularAction ma2c = ma2b;
      ma2c.setTransactionIdentifier(tid2b);
      client.processUndo(ma2c);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (!rui1.equals(rui3) && rui2.equals(rui3) && !irui1.equals(irui3) && irui2.equals(irui3))
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
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma2d = ma2a;
      ma2d.setTransactionIdentifier(tid2a);
      client.processUndo(ma2d);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && !rui2.equals(rui3) && irui1.equals(irui3) && !irui2.equals(irui3))
        addToLog("    2d. Test Passed");
      else {
        addToLog("    2d. Test Failed");
        thisTestFailed();
      }

      //
      // 3a. Test changing the relationship name
      //
      addToLog("    3a. Test change the relationship name of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverser RUI should not change");
      rel.setName("BRT");
      MolecularAction ma3a = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma3a);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui2 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui2 = inverse_rel.getRUI();

      addToLog("        rui: " + rui2.toString());
      addToLog("        irui: " + irui2.toString());

      // Compare Rui
      if (!rui1.equals(rui2) && !irui1.equals(irui2))
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }

      //
      // 3b. Test changing the relationship name back
      //
      addToLog("    3b. Test change relationship name of the relationships back ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should return to initial value");
      rel.setName("RT?");
      MolecularAction ma3b = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma3b);

      // Save Transaction ID
      int tid3b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3b);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && irui1.equals(irui3))
        addToLog("    3b. Test Passed");
      else {
        addToLog("    3b. Test Failed");
        thisTestFailed();
      }

      //
      // 3c. Undo 3b
      //
      addToLog("    3c. Test undo step 3b ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should not match initial value");

      MolecularAction ma3c = ma3b;
      ma3c.setTransactionIdentifier(tid3b);
      client.processUndo(ma3c);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (!rui1.equals(rui3) && rui2.equals(rui3) && !irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    3c. Test Passed");
      else {
        addToLog("    3c. Test Failed");
        thisTestFailed();
      }

      //
      // 3d. Undo 3a
      //
      addToLog("    3d. Test undo step 3a ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma3d = ma3a;
      ma3d.setTransactionIdentifier(tid3a);
      client.processUndo(ma3d);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && !rui2.equals(rui3) && irui1.equals(irui3) && !irui2.equals(irui3))
        addToLog("    3d. Test Passed");
      else {
        addToLog("    3d. Test Failed");
        thisTestFailed();
      }

      //
      // 4a. Test changing the relationship attribute
      //
      addToLog("    4a. Test change the relationship attribute of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should not change");
      rel.setAttribute("location_of");
      MolecularAction ma4a = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma4a);

      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui2 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui2 = inverse_rel.getRUI();

      addToLog("        rui: " + rui2.toString());
      addToLog("        irui: " + irui2.toString());

      // Compare Rui
      if (!rui1.equals(rui2) && !irui1.equals(irui2))
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }

      //
      // 4b. Test changing the relationship attribute back
      //
      addToLog("    4b. Test change relationship attribute of the relationships back ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should return to initial value");
      rel.setAttribute("mapped_to");
      MolecularAction ma4b = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma4b);

      // Save Transaction ID
      int tid4b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4b);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && irui1.equals(irui3))
        addToLog("    4b. Test Passed");
      else {
        addToLog("    4b. Test Failed");
        thisTestFailed();
      }

      //
      // 4c. Undo 4b
      //
      addToLog("    4c. Test undo step 4b ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma4c = ma4b;
      ma4c.setTransactionIdentifier(tid4b);
      client.processUndo(ma4c);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (!rui1.equals(rui3) && rui2.equals(rui3) && !irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    4c. Test Passed");
      else {
        addToLog("    4c. Test Failed");
        thisTestFailed();
      }

      //
      // 4d. Undo 4a
      //
      addToLog("    4d. Test undo step 4a ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma4d = ma4a;
      ma4d.setTransactionIdentifier(tid4a);
      client.processUndo(ma4d);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && !rui2.equals(rui3) && irui1.equals(irui3) && !irui2.equals(irui3))
        addToLog("    4d. Test Passed");
      else {
        addToLog("    4d. Test Failed");
        thisTestFailed();
      }

      //
      // 5a. Test changing the atom id 1
      //
      addToLog("    5a. Test change the atom id 1 of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should change");
      rel.setAtom(atom3);
      MolecularAction ma5a = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma5a);

      // Save Transaction ID
      int tid5a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5a);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui2 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui2 = inverse_rel.getRUI();

      addToLog("        rui: " + rui2.toString());
      addToLog("        irui: " + irui2.toString());

      // Compare Rui
      if (!rui1.equals(rui2) && !irui1.equals(irui2))
        addToLog("    5a. Test Passed");
      else {
        addToLog("    5a. Test Failed");
        thisTestFailed();
      }

      //
      // 5b. Test changing the atom id 1 back
      //
      addToLog("    5b. Test change atom id 1 of the relationships back ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should return to initial value");
      rel.setAtom(atom);
      MolecularAction ma5b = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma5b);

      // Save Transaction ID
      int tid5b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5b);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && irui1.equals(irui3))
        addToLog("    5b. Test Passed");
      else {
        addToLog("    5b. Test Failed");
        thisTestFailed();
      }

      //
      // 5c. Undo 5b
      //
      addToLog("    5c. Test undo step 5b ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma5c = ma5b;
      ma5c.setTransactionIdentifier(tid5b);
      client.processUndo(ma5c);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (!rui1.equals(rui3) && rui2.equals(rui3) && !irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    5c. Test Passed");
      else {
        addToLog("    5c. Test Failed");
        thisTestFailed();
      }

      //
      // 5d. Undo 5a
      //
      addToLog("    5d. Test undo step 5a ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma5d = ma5a;
      ma5d.setTransactionIdentifier(tid5a);
      client.processUndo(ma5d);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && !rui2.equals(rui3) && irui1.equals(irui3) && !irui2.equals(irui3))
        addToLog("    5d. Test Passed");
      else {
        addToLog("    5d. Test Failed");
        thisTestFailed();
      }

      //
      // 6a. Test changing the atom id 2
      //
      addToLog("    6a. Test change the atom id 2 of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should change");
      inverse_rel.setAtom(atom3);
      MolecularAction ma6a = new MolecularChangeRelationshipAction(inverse_rel);
      client.processAction(ma6a);

      // Save Transaction ID
      int tid6a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6a);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui2 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui2 = inverse_rel.getRUI();

      addToLog("        rui: " + rui2.toString());
      addToLog("        irui: " + irui2.toString());

      // Compare Rui
      if (!rui1.equals(rui2) && !irui1.equals(irui2))
        addToLog("    6a. Test Passed");
      else {
        addToLog("    6a. Test Failed");
        thisTestFailed();
      }

      //
      // 6b. Test changing the atom id 2 back
      //
      addToLog("    6b. Test change atom id 2 of the relationships back ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should return to initial value");
      inverse_rel.setAtom(atom2);
      MolecularAction ma6b = new MolecularChangeRelationshipAction(inverse_rel);
      client.processAction(ma6b);

      // Save Transaction ID
      int tid6b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6b);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && irui1.equals(irui3))
        addToLog("    6b. Test Passed");
      else {
        addToLog("    6b. Test Failed");
        thisTestFailed();
      }

      //
      // 6c. Undo 6b
      //
      addToLog("    6c. Test undo step 6b ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma6c = ma6b;
      ma6c.setTransactionIdentifier(tid6b);
      client.processUndo(ma6c);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (!rui1.equals(rui3) && rui2.equals(rui3) && !irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    6c. Test Passed");
      else {
        addToLog("    6c. Test Failed");
        thisTestFailed();
      }

      //
      // 6d. Undo 6a
      //
      addToLog("    6d. Test undo step 6a ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma6d = ma6a;
      ma6d.setTransactionIdentifier(tid6a);
      client.processUndo(ma6d);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && !rui2.equals(rui3) && irui1.equals(irui3) && !irui2.equals(irui3))
        addToLog("    6d. Test Passed");
      else {
        addToLog("    6d. Test Failed");
        thisTestFailed();
      }

      //
      // 7a. Test changing the concept id 1
      //
      addToLog("    7a. Test change the concept id 1 of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inversre RUI should not change");
      rel.setConcept(concept3);
      MolecularAction ma7a = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma7a);

      // Save Transaction ID
      int tid7a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7a);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui2 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui2 = inverse_rel.getRUI();

      addToLog("        rui: " + rui2.toString());
      addToLog("        irui: " + irui2.toString());

      // Compare Rui
      if (rui1.equals(rui2) && irui1.equals(irui2))
        addToLog("    7a. Test Passed");
      else {
        addToLog("    7a. Test Failed");
        thisTestFailed();
      }

      //
      // 7b. Test changing the concept id 1 back
      //
      addToLog("    7b. Test change concept id 1 of the relationships back ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should return to initial value");
      rel.setConcept(concept1);
      MolecularAction ma7b = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma7b);

      // Save Transaction ID
      int tid7b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7b);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && irui1.equals(irui3))
        addToLog("    7b. Test Passed");
      else {
        addToLog("    7b. Test Failed");
        thisTestFailed();
      }

      //
      // 7c. Undo 7b
      //
      addToLog("    7c. Test undo step 7b ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma7c = ma7b;
      ma7c.setTransactionIdentifier(tid7b);
      client.processUndo(ma7c);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && rui2.equals(rui3) && irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    7c. Test Passed");
      else {
        addToLog("    7c. Test Failed");
        thisTestFailed();
      }

      //
      // 7d. Undo 7a
      //
      addToLog("    7d. Test undo step 7a ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma7d = ma7a;
      ma7d.setTransactionIdentifier(tid7a);
      client.processUndo(ma7d);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && rui2.equals(rui3) && irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    7d. Test Passed");
      else {
        addToLog("    7d. Test Failed");
        thisTestFailed();
      }

      //
      // 8a. Test changing the concept id 2
      //
      addToLog("    8a. Test change the concept id 2 of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should not change");
      inverse_rel.setConcept(concept3);
      MolecularAction ma8a = new MolecularChangeRelationshipAction(inverse_rel);
      client.processAction(ma8a);

      // Save Transaction ID
      int tid8a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid8a);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui2 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui2 = inverse_rel.getRUI();

      addToLog("        rui: " + rui2.toString());
      addToLog("        irui: " + irui2.toString());

      // Compare Rui
      if (rui1.equals(rui2) && irui1.equals(irui2))
        addToLog("    8a. Test Passed");
      else {
        addToLog("    8a. Test Failed");
        thisTestFailed();
      }

      //
      // 8b. Test changing the concept id 2 back
      //
      addToLog("    8b. Test change concept id 1 of the relationships back ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should return to initial value");
      inverse_rel.setConcept(concept2);
      MolecularAction ma8b = new MolecularChangeRelationshipAction(inverse_rel);
      client.processAction(ma8b);

      // Save Transaction ID
      int tid8b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid8b);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && irui1.equals(irui3))
        addToLog("    8b. Test Passed");
      else {
        addToLog("    8b. Test Failed");
        thisTestFailed();
      }

      //
      // 8c. Undo 8b
      //
      addToLog("    8c. Test undo step 8b ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma8c = ma8b;
      ma8c.setTransactionIdentifier(tid8b);
      client.processUndo(ma8c);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && rui2.equals(rui3) && irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    8c. Test Passed");
      else {
        addToLog("    8c. Test Failed");
        thisTestFailed();
      }

      //
      // 8d. Undo 8a
      //
      addToLog("    8d. Test undo step 8a ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma8d = ma8a;
      ma8d.setTransactionIdentifier(tid8a);
      client.processUndo(ma8d);

      // Look Up Rui C
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && rui2.equals(rui3) && irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    8d. Test Passed");
      else {
        addToLog("    8d. Test Failed");
        thisTestFailed();
      }

      //
      // 9a. Test changing the relationship level
      //
      addToLog("    9a. Test change the relationship level of the relationships ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should change");
      rel.setLevel('C');
      MolecularAction ma9a = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma9a);

      // Save Transaction ID
      int tid9a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid9a);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui2 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui2 = inverse_rel.getRUI();

      addToLog("        rui: " + rui2.toString());
      addToLog("        irui: " + irui2.toString());

      // Compare Rui
      if (!rui1.equals(rui2) && !irui1.equals(irui2))
        addToLog("    9a. Test Passed");
      else {
        addToLog("    9a. Test Failed");
        thisTestFailed();
      }

      //
      // 9b. Test changing the relationship level back
      //
      addToLog("    9b. Test change relationship level of the relationships back ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should return to initial value");
      rel.setLevel('S');
      MolecularAction ma9b = new MolecularChangeRelationshipAction(rel);
      client.processAction(ma9b);

      // Save Transaction ID
      int tid9b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid9b);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && irui1.equals(irui3))
        addToLog("    9b. Test Passed");
      else {
        addToLog("    9b. Test Failed");
        thisTestFailed();
      }

      //
      // 9c. Undo 9b
      //
      addToLog("    9c. Test undo step 9c ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma9c = ma9b;
      ma9c.setTransactionIdentifier(tid9b);
      client.processUndo(ma9c);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (!rui1.equals(rui3) && rui2.equals(rui3) && !irui1.equals(irui3) && irui2.equals(irui3))
        addToLog("    9c. Test Passed");
      else {
        addToLog("    9c. Test Failed");
        thisTestFailed();
      }

      //
      // 9d. Undo 9a
      //
      addToLog("    9d. Test undo step 9a ... "
               + date_format.format(timestamp));
      addToLog("        RUI and inverse RUI should match initial value");

      MolecularAction ma9d = ma9a;
      ma9d.setTransactionIdentifier(tid9a);
      client.processUndo(ma9d);

      // Look Up Rui S
      rel = client.getRelationship(rel_id);
      rui3 = rel.getRUI();

      inverse_rel = client.getInverseRelationship(rel_id);
      irui3 = inverse_rel.getRUI();

      addToLog("        rui: " + rui3.toString());
      addToLog("        irui: " + irui3.toString());

      // Compare Rui
      if (rui1.equals(rui3) && !rui2.equals(rui3) && irui1.equals(irui3) && !irui2.equals(irui3))
        addToLog("    9d. Test Passed");
      else {
        addToLog("    9d. Test Failed");
        thisTestFailed();
      }

      //
      // 10a. Undo 1b
      //
      addToLog("    10a. Test undo the 1b ... "
               + date_format.format(timestamp));

      MolecularAction ma10a = ma1b;
      ma10a.setTransactionIdentifier(tid1b);
      client.processUndo(ma10a);

      //
      // 10b. Undo 1a3
      //
      addToLog("    10b. Test undo the 1a3 ... "
               + date_format.format(timestamp));

      MolecularAction ma10b = ma1a3;
      ma10b.setTransactionIdentifier(tid1a3);
      client.processUndo(ma10b);

      //
      // 10c. Undo 1a2
      //
      addToLog("    10c. Test undo the 1a2 ... "
               + date_format.format(timestamp));

      MolecularAction ma10c = ma1a2;
      ma10c.setTransactionIdentifier(tid1a2);
      client.processUndo(ma10c);

      //
      // 10d. Undo 1a1
      //
      addToLog("    10d. Test undo the 1a1 ... "
               + date_format.format(timestamp));

      MolecularAction ma10d = ma1a1;
      ma10d.setTransactionIdentifier(tid1a1);
      client.processUndo(ma10d);

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
      addToLog("Finished TestSuiteRuiS at " + date_format.format(new Date(System.currentTimeMillis())));
      addToLog("-------------------------------------------------------");

    } catch(MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }

}