/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ui
 * Object:  TestSuiteAtuiS
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
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Performs a series of actions on a source-level attribute and
 * validates the ATUI semantics at each stage.  Each of the following actions
 * is performed.  The middle actions are performed, then performed in reverse,
 * and then undone to test all data conditions.  Running this test should leave
 * the database in a clean state if it finishes successfully.
 * <ol>
 *   <li>Insert initial data, including source level attribute.
 *   <li>Change source</li>
 *   <li>Change attribute name</li>
 *   <li>Change atom id</li>
 *   <li>Change concept id</li>
 *   <li>Change attribute level</li>
 *   <li>Undo initial insert</li>
 * </ol>
 */
public class TestSuiteAtuiS extends TestSuite {

  private int concept_id2 = 101;

  /**
   * Instantiates an empty {@link TestSuiteAtuiS}.
   */
  public TestSuiteAtuiS() {
    setName("TestSuiteAtuiS");
    setDescription("Test Suite for Atui S");
    setConceptId(100);
  }

  /**
   * Perform Test Suite Atui S.
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

      // Re-read atom
      int atom_id1 = client.getMaxIdentifierForType(Atom.class).intValue();
      atom = client.getAtom(atom_id1);

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
      int atom_id2 = client.getMaxIdentifierForType(Atom.class).intValue();
      atom2 = client.getAtom(atom_id2);

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
      int attr_id = client.getMaxIdentifierForType(Attribute.class).intValue();
      attr = client.getAttribute(attr_id);

      // Look Up Atui S
      ATUI atui1 = attr.getATUI();
      addToLog("        atui: " + atui1.toString());
      addToLog("        attribute_id: " + attr.getIdentifier().intValue());

      //
      // 2a. Test changing the source
      //
      addToLog("    2a. Test change source of the attributes ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should change");
      attr.setSource(client.getSource("SRC"));
      MolecularAction ma2a = new MolecularChangeAttributeAction(attr);
      client.processAction(ma2a);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      ATUI atui2 = attr.getATUI();
      addToLog("        atui: " + atui2.toString());

      // Compare ATUI
      if (!atui1.equals(atui2))
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      //
      // 2b. Test changing the source back
      //
      addToLog("    2b. Test change source of the attributes back ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should return to initial value");
      attr.setSource(client.getSource("MTH"));
      MolecularAction ma2b = new MolecularChangeAttributeAction(attr);
      client.processAction(ma2b);

      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      // Look Up ATUI
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
      addToLog("        ATUI should not match initial value");

      MolecularAction ma2c = ma2b;
      ma2c.setTransactionIdentifier(tid2b);
      client.processUndo(ma2c);

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (!atui1.equals(atui3) && atui2.equals(atui3))
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

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && !atui2.equals(atui3))
        addToLog("    2d. Test Passed");
      else {
        addToLog("    2d. Test Failed");
        thisTestFailed();
      }

      //
      // 3a. Test changing the attribute name
      //
      addToLog("    3a. Test change attribute name of the attributes ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should change");
      attr.setName("NEW_CONCEPT_NOTE");
      MolecularAction ma3a = new MolecularChangeAttributeAction(attr);
      client.processAction(ma3a);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui2 = attr.getATUI();
      addToLog("        atui: " + atui2.toString());

      // Compare ATUI
      if (!atui1.equals(atui2))
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }

      //
      // 3b. Test changing the attribute name back
      //
      addToLog("    3b. Test change attribute name of the attributes back ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should return to initial value");
      attr.setName("CONCEPT_NOTE");
      MolecularAction ma3b = new MolecularChangeAttributeAction(attr);
      client.processAction(ma3b);

      // Save Transaction ID
      int tid3b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3b);

      // Look Up ATUI
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
      addToLog("        ATUI should not match initial value");

      MolecularAction ma3c = ma3b;
      ma3c.setTransactionIdentifier(tid3b);
      client.processUndo(ma3c);

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (!atui1.equals(atui3) && atui2.equals(atui3))
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

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && !atui2.equals(atui3))
        addToLog("    3d. Test Passed");
      else {
        addToLog("    3d. Test Failed");
        thisTestFailed();
      }

      //
      // 4a. Test changing the atom id
      //
      addToLog("    4a. Test change the atom id of the attributes ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should change");

      attr.setAtom(atom2);
      MolecularAction ma4a = new MolecularChangeAttributeAction(attr);
      client.processAction(ma4a);

      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);

      // Look Up Atui S
      attr = client.getAttribute(attr_id);
      atui2 = attr.getATUI();
      addToLog("        atui: " + atui2.toString());

      // Compare ATUI
      if (!atui1.equals(atui2))
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }

      //
      // 4b. Test changing the atom id back
      //
      addToLog("    4b. Test change atom id of the attributes back ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should return to initial value");

      attr.setAtom(atom);
      MolecularAction ma4b = new MolecularChangeAttributeAction(attr);
      client.processAction(ma4b);

      // Save Transaction ID
      int tid4b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4b);

      // Look Up Atui S
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3))
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
      addToLog("        ATUI should not match initial value");

      MolecularAction ma4c = ma4b;
      ma4c.setTransactionIdentifier(tid4b);
      client.processUndo(ma4c);

      // Look Up Atui S
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (!atui1.equals(atui3) && atui2.equals(atui3))
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
      addToLog("        ATUI should match initial value");

      MolecularAction ma4d = ma4a;
      ma4d.setTransactionIdentifier(tid4a);
      client.processUndo(ma4d);

      // Look Up Atui S
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && !atui2.equals(atui3))
        addToLog("    4d. Test Passed");
      else {
        addToLog("    4d. Test Failed");
        thisTestFailed();
      }

      //
      // 5a. Test changing the concept id
      //
      addToLog("    5a. Test change the concept id of the attributes ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should not change");

      atom = client.getAtom(atom_id1);
      attr.setAtom(atom);
      MolecularMoveAction ma5a = new MolecularMoveAction(concept1, concept2);
      ma5a.addAtomToMove(attr.getAtom());
      client.processAction(ma5a);

      // Save Transaction ID
      int tid5a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5a);

      // Look Up Atui S
      attr = client.getAttribute(attr_id);
      atui2 = attr.getATUI();
      addToLog("        atui: " + atui2.toString());

      // Compare ATUI
      if (atui1.equals(atui2))
        addToLog("    5a. Test Passed");
      else {
        addToLog("    5a. Test Failed");
        thisTestFailed();
      }

      //
      // 5b. Test changing the concept id back
      //
      addToLog("    5b. Test change concept id of the attributes back ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should return to initial value");

      atom2 = client.getAtom(atom_id2);
      attr.setAtom(atom2);
      MolecularMoveAction ma5b = new MolecularMoveAction(concept2, concept1);
      ma5b.addAtomToMove(attr.getAtom());
      client.processAction(ma5b);

      // Save Transaction ID
      int tid5b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5b);

      // Look Up Atui S
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3))
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
      addToLog("        ATUI should match initial value");

      MolecularAction ma5c = ma5b;
      ma5c.setTransactionIdentifier(tid5b);
      client.processUndo(ma5c);

      // Look Up Atui S
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && atui2.equals(atui3))
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
      addToLog("        ATUI should match initial value");

      MolecularAction ma5d = ma5a;
      ma2d.setTransactionIdentifier(tid5a);
      client.processUndo(ma5d);

      // Look Up Atui S
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && atui2.equals(atui3))
        addToLog("    5d. Test Passed");
      else {
        addToLog("    5d. Test Failed");
        thisTestFailed();
      }

      //
      // 6a. Test changing the attribute level
      //
      addToLog("    6a. Test change attribute level of the attributes ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should change");
      attr.setLevel('C');
      MolecularAction ma6a = new MolecularChangeAttributeAction(attr);
      client.processAction(ma6a);

      // Save Transaction ID
      int tid6a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6a);

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui2 = attr.getATUI();
      addToLog("        atui: " + atui2.toString());

      // Compare ATUI
      if (!atui1.equals(atui2))
        addToLog("    6a. Test Passed");
      else {
        addToLog("    6a. Test Failed");
        thisTestFailed();
      }

      //
      // 6b. Test changing the attribute level back
      //
      addToLog("    6b. Test change attribute level of the attributes back ... "
               + date_format.format(timestamp));
      addToLog("        ATUI should return to initial value");

      attr.setLevel('S');
      MolecularAction ma6b = new MolecularChangeAttributeAction(attr);
      client.processAction(ma6b);

      // Save Transaction ID
      int tid6b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6b);

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3))
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
      addToLog("        ATUI should not match initial value");

      MolecularAction ma6c = ma6b;
      ma6c.setTransactionIdentifier(tid6b);
      client.processUndo(ma6c);

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (!atui1.equals(atui3) && atui2.equals(atui3))
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
      addToLog("        ATUI should match initial value");

      MolecularAction ma6d = ma6a;
      ma6d.setTransactionIdentifier(tid6a);
      client.processUndo(ma6d);

      // Look Up ATUI
      attr = client.getAttribute(attr_id);
      atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui1.equals(atui3) && !atui2.equals(atui3))
        addToLog("    6d. Test Passed");
      else {
        addToLog("    6d. Test Failed");
        thisTestFailed();
      }

      //
      // 7a. Undo 1b
      //
      addToLog("    7a. Test undo the 1b ... "
               + date_format.format(timestamp));

      MolecularAction ma7a = ma1b;
      ma7a.setTransactionIdentifier(tid1b);
      client.processUndo(ma7a);

      //
      // 7b. Undo 1a2
      //
      addToLog("    7b. Test undo the 1a2 ... "
               + date_format.format(timestamp));

      MolecularAction ma7b = ma1a2;
      ma7b.setTransactionIdentifier(tid1a2);
      client.processUndo(ma7b);

      //
      // 7c. Undo 1a1
      //
      addToLog("    7c. Test undo the 1a1 ... "
               + date_format.format(timestamp));

      MolecularAction ma7c = ma1a1;
      ma7c.setTransactionIdentifier(tid1a1);
      client.processUndo(ma7c);

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
      addToLog("Finished TestSuiteAtuiS at " + date_format.format(new Date(System.currentTimeMillis())));
      addToLog("-------------------------------------------------------");

    } catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }

}