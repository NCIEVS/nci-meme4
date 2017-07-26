/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ui
 * Object:  TestSuiteAtuiSG
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ui;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularMoveAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.ATUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Performs a series of actions on a source-level attribute and
 * validates the ATUI semantics at each stage.
 */
public class TestSuiteAtuiSG
    extends TestSuite {

  private int concept_id2 = 101;

  /**
   * Instantiates an empty {@link TestSuiteAtuiSG}.
   */
  public TestSuiteAtuiSG() {
    setName("TestSuiteAtuiSG");
    setDescription("Test Suite for Atui SG");
    setConceptId(100);

    /**
     * Perform Test Suite Atui SG.
     */
  }

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
      // 1b. Insert an attribute
      //
      addToLog("    1b. Insert an attribute... " + date_format.format(timestamp));

      Attribute attr = new Attribute.Default();
      attr.setAtom(atom);
      attr.setLevel('S');
      attr.setName("CONCEPT_NOTE");
      attr.setValue("test concept_note.");
      attr.setSource(client.getSource("MTH"));
      attr.setStatus('R');
      attr.setGenerated(false);
      attr.setReleased('A');
      attr.setTobereleased('Y');
      attr.setSuppressible("N");
      attr.setConcept(concept1);

      MolecularAction ma1b = new MolecularInsertAttributeAction(attr);
      client.processAction(ma1b);

      // Save Transaction ID
      int tid1b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1b);

      // Re-read concept
      concept1 = client.getConcept(concept1);

      // Re-read attribute
      int attr_id = 0;
      Attribute[] attrs = concept1.getAttributes();
      for (int i = 0; i < attrs.length; i++) {
        if (attrs[i].isReleasable()) {
          attr_id = attrs[i].getIdentifier().intValue();
        }
      }

      Identifier id = client.getMaxIdentifierForType(Attribute.class);
      attr_id = id.intValue();

      attr = client.getAttribute(attr_id);

      // Look Up Atui SG
      ATUI atui1 = attr.getATUI();
      addToLog("        atui: " + atui1.toString());
      addToLog("        attribute_id: " + attr.getIdentifier().intValue());

      //
      // 2a. Test changing the atom id
      //
      addToLog("    2a. Test change the atom id of the attributes ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should not change");

      attr.setAtom(atom);
      MolecularAction ma2a = new MolecularChangeAttributeAction(attr);
      client.processAction(ma2a);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      // Look Up Atui SG
      attr = client.getAttribute(attr_id);
      ATUI atui2 = attr.getATUI();
      addToLog("        atui: " + atui2.toString());

      // Compare ATUI
      if (atui1.equals(atui2))
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      //
      // 2b. Test changing the atom id back
      //
      addToLog("    2b. Test change atom id of the attributes back ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should return to initial value");

      attr.setAtom(atom);
      MolecularAction ma2b = new MolecularChangeAttributeAction(attr);
      client.processAction(ma2b);

      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      // Look Up Atui SG
      attr = client.getAttribute(attr_id);
      ATUI atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3))
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
      addToLog("        ATUI should match initial value");

      MolecularAction ma2c = ma2b;
      ma2c.setTransactionIdentifier(tid2b);
      client.processUndo(ma2c);

      // Look Up Atui SG
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && atui2.equals(atui3))
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
      addToLog("        ATUI should match initial value");

      MolecularAction ma2d = ma2a;
      ma2d.setTransactionIdentifier(tid2a);
      client.processUndo(ma2d);

      // Look Up Atui SG
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && atui2.equals(atui3))
        addToLog("    2d. Test Passed");
      else {
        addToLog("    2d. Test Failed");
        thisTestFailed();
      }

      //
      // 3a. Test changing the concept id
      //
      addToLog("    3a. Test change the concept id of the attributes ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should not change");

      atom = client.getAtom(atom);
      attr.setAtom(atom);
      MolecularMoveAction ma3a = new MolecularMoveAction(concept1, concept2);
      ma3a.addAtomToMove(attr.getAtom());
      client.processAction(ma3a);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      // Look Up Atui SG
      attr = client.getAttribute(attr_id);
      atui2 = attr.getATUI();
      addToLog("        atui: " + atui2.toString());

      // Compare ATUI
      if (atui1.equals(atui2))
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }

      //
      // 3b. Test changing the concept id back
      //
      addToLog("    3b. Test change concept id of the attributes back ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should return to initial value");

      atom2 = client.getAtom(atom2);
      attr.setAtom(atom2);
      MolecularMoveAction ma3b = new MolecularMoveAction(concept2, concept1);
      ma3b.addAtomToMove(attr.getAtom());
      client.processAction(ma3b);

      // Save Transaction ID
      int tid3b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3b);

      // Look Up Atui SG
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3))
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
      addToLog("        ATUI should match initial value");

      MolecularAction ma3c = ma3b;
      ma3c.setTransactionIdentifier(tid3b);
      client.processUndo(ma3c);

      // Look Up Atui SG
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && atui2.equals(atui3))
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
      addToLog("        ATUI should match initial value");

      MolecularAction ma3d = ma3a;
      ma3d.setTransactionIdentifier(tid3a);
      client.processUndo(ma3d);

      // Look Up Atui SG
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && atui2.equals(atui3))
        addToLog("    3d. Test Passed");
      else {
        addToLog("    3d. Test Failed");
        thisTestFailed();
      }

      //
      // 4a. Undo 1b
      //
      addToLog("    4a. Test undo the 1b ... "
               + date_format.format(timestamp));

      MolecularAction ma4a = ma1b;
      ma4a.setTransactionIdentifier(tid1b);
      client.processUndo(ma4a);

      //
      // 4b. Undo 1a2
      //
      addToLog("    4b. Test undo the 1a2 ... "
               + date_format.format(timestamp));

      MolecularAction ma4b = ma1a2;
      ma4b.setTransactionIdentifier(tid1a2);
      client.processUndo(ma4b);

      //
      // 4c. Undo 1a1
      //
      addToLog("    4c. Test undo the 1a1 ... "
               + date_format.format(timestamp));

      MolecularAction ma4c = ma1a1;
      ma4c.setTransactionIdentifier(tid1a1);
      client.processUndo(ma4c);

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
      addToLog("Finished TestSuiteAtuiSG at " +
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