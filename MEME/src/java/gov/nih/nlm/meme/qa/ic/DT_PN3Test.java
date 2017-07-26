/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_PN3Test
 *
 * 03/13/2006 RBE (1-A07F5): Changes to trigger ambiguous MTH/PN
 * 01/05/2006 RBE (1-72TW3): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.DT_PN3;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DT_PN3Test extends TestSuite{

  /**
   * Instantiates an empty {@link DT_PN3Test}.
   */	
  public DT_PN3Test() {
    setName("DT_PN3Test");
    setDescription("Test suite for DT_PN3 integrity");
  }
  
  /**
   * Perform integrity test.
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
      AuxiliaryDataClient adc = new AuxiliaryDataClient(client.getMidService());
      DT_PN3 ic = (DT_PN3) adc.getIntegrityCheck("DT_PN3");

      //
      // create data set-up to test all logic
      //
      
      Concept concept1 = client.getConcept(100);
      Concept concept2 = client.getConcept(101);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No MTH/PN Atom found ...");
      if (!ic.validate(concept1))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert MTH/PN atom connected to concept1
      //

      addToLog("    2a. Insert an MTH/PN atom connected to concept1 ... " + date_format.format(timestamp));

      // Create an atom
      Atom atom2 = new Atom.Default();
      atom2.setString("TEST ATOM 2");
      atom2.setTermgroup(client.getTermgroup("MTH/PN"));
      atom2.setSource(client.getSource("MTH"));
      atom2.setStatus('R');
      atom2.setGenerated(true);
      atom2.setReleased('N');
      atom2.setTobereleased('Y');
      atom2.setSuppressible("N");
      atom2.setConcept(concept1);
      
      concept1.addAtom(atom2);

      MolecularAction ma2a = new MolecularInsertAtomAction(atom2);
      client.processAction(ma2a);
      
      // re-read concepts
      concept1 = client.getConcept(concept1);    
    
      addToLog("        Should be a violation. No ambiguous MTH/PN and releasable atom found ...");
      if (ic.validate(concept1))
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      
      //
      // 3a. Insert MTH/PT atom connected to concept2
      //

      addToLog("    3a. Insert an MTH/PT atom connected to concept2 ... " + date_format.format(timestamp));
     
      Atom atom3 = new Atom.Default();
      atom3.setString("TEST ATOM 3");
      atom3.setTermgroup(client.getTermgroup("MTH/PT"));
      atom3.setSource(client.getSource("MTH"));
      atom3.setStatus('R');
      atom3.setGenerated(true);
      atom3.setReleased('N');
      atom3.setTobereleased('Y');
      atom3.setSuppressible("N");     
      atom3.setConcept(concept2);
      
      concept2.addAtom(atom3);

      MolecularAction ma3a = new MolecularInsertAtomAction(atom3);
      client.processAction(ma3a);
      
      // re-read concept
      concept2 = client.getConcept(concept2);
      
      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);


      //
      // 3b. Insert MTH/PT atom connected to concept1
      //

      addToLog("    3b. Insert an MTH/PT atom connected to concept1 ... " + date_format.format(timestamp));

      // Create an atom
      atom3.setConcept(concept1);
      concept1.addAtom(atom3);

      MolecularAction ma3b = new MolecularInsertAtomAction(atom3);
      client.processAction(ma3b);
      
      // re-read concepts
      concept1 = client.getConcept(concept1);    
    
      // Save Transaction ID
      int tid3b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3b);
      
      addToLog("        Should not be a violation. Found an ambiguous releasable MTH/PN atom ...");
      if (!ic.validate(concept1))
        addToLog("    3b. Test Passed");
      else {
        addToLog("    3b. Test Failed");
        thisTestFailed();
      }
      
      //
      // 4a. Undo 3b
      //
      addToLog("    4a. Undo 3b ... "
               + date_format.format(timestamp));

      MolecularAction ma4a = ma3b;
      ma4a.setTransactionIdentifier(tid3b);
      client.processUndo(ma4a);
                 
      //
      // 5a. Undo 3a
      //
      addToLog("    5a. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma5a = ma3a;
      ma5a.setTransactionIdentifier(tid3a);
      client.processUndo(ma5a);

      //
      // 6a. Undo 2a
      //
      addToLog("    6a. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma6a = ma2a;
      ma6a.setTransactionIdentifier(tid2a);
      client.processUndo(ma6a);

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
      addToLog("Finished DT_PN3Test at " +
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