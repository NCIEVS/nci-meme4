/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ui
 * Object:  TestSuiteAtuiTBR
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ui;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
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
 * Test suite for Atui TBR
 */
public class TestSuiteAtuiTBR
    extends TestSuite {

  private int concept_id2 = 101;

  public TestSuiteAtuiTBR() {
    setName("TestSuiteAtuiTBR");
    setDescription("Test Suite for Atui TBR");
    setConceptId(100);
  }

  /**
   * Perform Test Suite Atui TBR
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
      // 1b. Insert an attribute
      //
      addToLog("    1b. Insert an attribute... " + date_format.format(timestamp));
      addToLog("        ATUI should be null");

      Attribute attr = new Attribute.Default();
      attr.setAtom(atom);
      attr.setLevel('S');
      attr.setName("CONCEPT_NOTE");
      attr.setValue("test concept_note.");
      attr.setSource(client.getSource("MTH"));
      attr.setStatus('R');
      attr.setGenerated(false);
      attr.setReleased('A');
      attr.setTobereleased('n');
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

      // Look Up Atui TBR
      ATUI atui1 = attr.getATUI();
      addToLog("        attribute_id: " + attr.getIdentifier().intValue());

      // Compare ATUI
      if (atui1 == null)
        addToLog("    1b. Test Passed");
      else {
        addToLog("    1b. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Test changing the tobereleased of the attributes
      //
      addToLog("    2a. Test change the tobereleased of the attributes ... "
               + date_format.format(timestamp));
      addToLog(
          "        ATUI should not be null and ATUI should not be empty string");
      attr.setTobereleased('Y');
      MolecularAction ma2a = new MolecularChangeAttributeAction(attr);
      client.processAction(ma2a);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      // Look Up Atui TBR
      attr = client.getAttribute(attr_id);
      ATUI atui2 = attr.getATUI();
      addToLog("        atui: " + atui2.toString());

      // Compare ATUI
      if (atui2 != null && !atui2.equals(""))
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      //
      // 2b. Undo 2a
      //
      addToLog("    2b. Test undo step 2a ... "
               + date_format.format(timestamp));
      addToLog("        b.ATUI should be equal to c.ATUI");

      MolecularAction ma2b = ma2a;
      ma2b.setTransactionIdentifier(tid2a);
      client.processUndo(ma2b);

      // Look Up Atui TBR
      attr = client.getAttribute(attr_id);
      ATUI atui3 = attr.getATUI();
      addToLog("        atui: " + atui3.toString());

      // Compare ATUI
      if (atui2.equals(atui3))
        addToLog("    2b. Test Passed");
      else {
        addToLog("    2b. Test Failed");
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
      addToLog("Finished TestSuiteAtuiTBR at " +
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