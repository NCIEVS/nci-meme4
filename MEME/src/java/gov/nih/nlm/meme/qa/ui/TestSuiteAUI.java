/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ui
 * Object:  TestSuiteAUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ui;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for AUI
 */
public class TestSuiteAUI
    extends TestSuite {

  public TestSuiteAUI() {
    setName("TestSuiteAUI");
    setDescription("Test Suite for AUI");
    setConceptId(100);
  }

  /**
   * Perform Test Suite AUI
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
      Concept concept = client.getConcept(this.getConceptId());

      Termgroup termgroup = client.getTermgroup("MTH/PT");

      //
      // 1. Insert an atom
      //
      addToLog("    1. Insert an atom... " + date_format.format(timestamp));

      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM");
      atom.setTermgroup(termgroup);
      atom.setSource(client.getSource("MTH"));
      atom.setCode(Code.newCode("NOCODE"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept);

      MolecularAction ma1 = new MolecularInsertAtomAction(atom);
      client.processAction(ma1);

      // Save Transaction ID
      int tid1 = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1);

      // Re-read concept
      concept = client.getConcept(concept);

      // Re-read atom
      int atom_id = 0;
      Atom[] atoms = concept.getAtoms();
      for (int i = 0; i < atoms.length; i++) {
        if (atoms[i].isReleasable()) {
          atom_id = atoms[i].getIdentifier().intValue();
        }
      }
      atom = client.getAtom(atom_id);

      // Look Up AUI
      AUI aui1 = atom.getAUI();
      addToLog("        aui: " + aui1.toString());
      addToLog("        atom_id: " + atom.getIdentifier().intValue());

      //
      // 2a. Test changing the source
      //
      addToLog("    2a. Test change source of the atom ... "
               + date_format.format(timestamp));
      addToLog("        AUI should change");
      atom.setSource(client.getSource("SRC"));
      MolecularAction ma2a = new MolecularChangeAtomAction(atom);
      client.processAction(ma2a);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      AUI aui2 = atom.getAUI();
      addToLog("        aui: " + aui2.toString());

      // Compare AUI
      if (!aui1.equals(aui2))
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      //
      // 2b. Test changing the source back
      //
      addToLog("    2b. Test change source of the atom back ... "
               + date_format.format(timestamp));
      addToLog("        AUI should return to initial value");
      atom.setSource(client.getSource("MTH"));
      MolecularAction ma2b = new MolecularChangeAtomAction(atom);
      client.processAction(ma2b);

      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      AUI aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (aui1.equals(aui3) && !aui2.equals(aui3))
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
      addToLog("        AUI should not match initial value");

      MolecularAction ma2c = ma2b;
      ma2c.setTransactionIdentifier(tid2b);
      client.processUndo(ma2c);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (!aui1.equals(aui3) && aui2.equals(aui3))
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
      addToLog("        AUI should match initial value");

      MolecularAction ma2d = ma2a;
      ma2d.setTransactionIdentifier(tid2a);
      client.processUndo(ma2d);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (aui1.equals(aui3) && !aui2.equals(aui3))
        addToLog("    2d. Test Passed");
      else {
        addToLog("    2d. Test Failed");
        thisTestFailed();
      }

      //
      // 3a. Test changing the tty
      //
      addToLog("    3a. Test change tty of the atom ... "
               + date_format.format(timestamp));
      addToLog("        AUI should change");
      termgroup = client.getTermgroup("MTH/PN");
      atom.setTermgroup(termgroup);
      MolecularAction ma3a = new MolecularChangeAtomAction(atom);
      client.processAction(ma3a);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui2 = atom.getAUI();
      addToLog("        aui: " + aui2.toString());

      // Compare AUI
      if (!aui1.equals(aui2))
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }

      //
      // 3b. Test changing the tty back
      //
      addToLog("    3b. Test change tty of the atom back ... "
               + date_format.format(timestamp));
      addToLog("        AUI should return to initial value");
      termgroup = client.getTermgroup("MTH/PT");
      atom.setTermgroup(termgroup);
      MolecularAction ma3b = new MolecularChangeAtomAction(atom);
      client.processAction(ma3b);

      // Save Transaction ID
      int tid3b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3b);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (aui1.equals(aui3) && !aui2.equals(aui3))
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
      addToLog("        AUI should not match initial value");

      MolecularAction ma3c = ma3b;
      ma3c.setTransactionIdentifier(tid3b);
      client.processUndo(ma3c);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (!aui1.equals(aui3) && aui2.equals(aui3))
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
      addToLog("        AUI should match initial value");

      MolecularAction ma3d = ma3a;
      ma3d.setTransactionIdentifier(tid3a);
      client.processUndo(ma3d);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (aui1.equals(aui3) && !aui2.equals(aui3))
        addToLog("    3d. Test Passed");
      else {
        addToLog("    3d. Test Failed");
        thisTestFailed();
      }

      //
      // 4a. Test changing the code
      //
      addToLog("    4a. Test change code of the atom ... "
               + date_format.format(timestamp));
      addToLog("        AUI should change");
      atom.setCode(new Code("ABCDE"));
      MolecularAction ma4a = new MolecularChangeAtomAction(atom);
      client.processAction(ma4a);

      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui2 = atom.getAUI();
      addToLog("        aui: " + aui2.toString());

      // Compare AUI
      if (!aui1.equals(aui2))
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }

      //
      // 4b. Test changing the code back
      //
      addToLog("    4b. Test change the code of the atom back ... "
               + date_format.format(timestamp));
      addToLog("        AUI should return to initial value");
      atom.setCode(new Code("NOCODE"));
      MolecularAction ma4b = new MolecularChangeAtomAction(atom);
      client.processAction(ma4b);

      // Save Transaction ID
      int tid4b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4b);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (aui1.equals(aui3) && !aui2.equals(aui3))
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
      addToLog("        AUI should not match initial value");

      MolecularAction ma4c = ma4b;
      ma4c.setTransactionIdentifier(tid4b);
      client.processUndo(ma4c);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (!aui1.equals(aui3) && aui2.equals(aui3))
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
      addToLog("        AUI should match initial value");

      MolecularAction ma4d = ma4a;
      ma4d.setTransactionIdentifier(tid4a);
      client.processUndo(ma4d);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (aui1.equals(aui3) && !aui2.equals(aui3))
        addToLog("    4d. Test Passed");
      else {
        addToLog("    4d. Test Failed");
        thisTestFailed();
      }

      //
      // 5a. Test changing the sui
      //
      addToLog("    5a. Test change sui of the atom ... "
               + date_format.format(timestamp));
      addToLog("        AUI should change");
      StringIdentifier old_sui = atom.getSUI();
      atom.setSUI(new StringIdentifier("S0000474"));
      MolecularAction ma5a = new MolecularChangeAtomAction(atom);
      client.processAction(ma5a);

      // Save Transaction ID
      int tid5a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5a);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui2 = atom.getAUI();
      addToLog("        aui: " + aui2.toString());

      // Compare AUI
      if (!aui1.equals(aui2))
        addToLog("    5a. Test Passed");
      else {
        addToLog("    5a. Test Failed");
        thisTestFailed();
      }

      //
      // 5b. Test changing the sui back
      //
      addToLog("    5b. Test change the sui of the atom back ... "
               + date_format.format(timestamp));
      addToLog("        AUI should return to initial value");
      atom.setSUI(old_sui);
      MolecularAction ma5b = new MolecularChangeAtomAction(atom);
      client.processAction(ma5b);

      // Save Transaction ID
      int tid5b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5b);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (aui1.equals(aui3) && !aui2.equals(aui3))
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
      addToLog("        AUI should not match initial value");

      MolecularAction ma5c = ma5b;
      ma5c.setTransactionIdentifier(tid5b);
      client.processUndo(ma5c);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (!aui1.equals(aui3) && aui2.equals(aui3))
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
      addToLog("        AUI should match initial value");

      MolecularAction ma5d = ma5a;
      ma5d.setTransactionIdentifier(tid5a);
      client.processUndo(ma5d);

      // Look Up AUI
      atom = client.getAtom(atom_id);
      aui3 = atom.getAUI();
      addToLog("        aui: " + aui3.toString());

      // Compare AUI
      if (aui1.equals(aui3) && !aui2.equals(aui3))
        addToLog("    5d. Test Passed");
      else {
        addToLog("    5d. Test Failed");
      }

      //
      // 6. Undo 1
      //
      addToLog("    6. Test undo 1 ... "
               + date_format.format(timestamp));

      MolecularAction ma6 = ma1;
      ma6.setTransactionIdentifier(tid1);
      client.processUndo(ma6);

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
      addToLog("Finished TestSuiteAUI at " +
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