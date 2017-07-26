/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularInsertAtomActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularInsertAtomActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularInsertAtomActionTest}.
   */	
  public MolecularInsertAtomActionTest() {
    setName("MolecularInsertAtomActionTest");
    setDescription("Test suite for molecular insert atom actions");
  }
  
  /**
   * Perform molecular insert atom action test.
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
      
      Concept concept = client.getConcept(101);

      //
      // 1a. Approve a concept
      //
      addToLog("    1a. Approve a concept ... " + date_format.format(timestamp));

      MolecularAction ma1a = new MolecularApproveConceptAction(concept);
      client.processAction(ma1a);
      
      // re-read concept
      concept = client.getConcept(concept);    
      char prev_status = concept.getStatus();
    
      // Save Transaction ID
      int tid1a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a);
   
      addToLog("        Current concept status and previous status must be equal to 'R' ...");
      if (concept.getStatus() == prev_status && concept.getStatus() == 'R')
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }      

      //
      // 2a. Insert an atom, set change status to true
      //
      addToLog("    2a. Insert an atom, set change status to true ... " + date_format.format(timestamp));

      // Create an atom
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
      atom.setConcept(concept);
      
      concept.addAtom(atom);

      MolecularAction ma2a = new MolecularInsertAtomAction(atom);
      client.setChangeStatus(true);
      client.processAction(ma2a);

      // re-read concept      
      concept = client.getConcept(concept);
      atom = client.getAtom(atom);     

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);
     
      addToLog("        Current concept status must not be equal to previous status ...");
      if (concept.getStatus() != prev_status)
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }      
            
      addToLog("        Current concept must be equal to previous concept ...");
      if (atom.getConcept().equals(concept))
        addToLog("    2a2. Test Passed");
      else {
        addToLog("    2a2. Test Failed");
        thisTestFailed();
      }      

      //
      // 2b. Undo 2a
      //
      addToLog("    2b. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma2b = ma2a;
      ma2b.setTransactionIdentifier(tid2a);
      client.processUndo(ma2b);

      //
      // 3a Insert an atom, set change status to false 
      //      
      addToLog("    3a. Insert an atom, set change status to false ... " + date_format.format(timestamp));
      
      MolecularAction ma3a = new MolecularInsertAtomAction(atom);
      client.setChangeStatus(false);
      client.processAction(ma3a);
      
      // re-read concept
      concept = client.getConcept(concept);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);
    
      addToLog("        Current concept status must not be equal to 'N' and previous status must be equal to 'R' ...");
      if (concept.getStatus() != 'N' && prev_status == 'R')
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }
      
      //
      // 3b. Undo 3a
      //
      addToLog("    3b. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma3b = ma3a;
      ma3b.setTransactionIdentifier(tid3a);
      client.processUndo(ma3b);      
     
      //
      // 4a. Undo 1a
      //
      addToLog("    4a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma4a = ma1a;
      ma4a.setTransactionIdentifier(tid1a);
      client.processUndo(ma4a);      

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
      addToLog("Finished MolecularInsertAtomActionTest at " +
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