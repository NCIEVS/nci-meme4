/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularChangeAtomActionTest
 *
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularChangeAtomActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularChangeAtomActionTest}.
   */	
  public MolecularChangeAtomActionTest() {
    setName("MolecularChangeAtomActionTest");
    setDescription("Test suite for molecular change atom actions");
  }
  
  /**
   * Perform molecular change atom action test.
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

      // re-read concept
      concept = client.getConcept(concept);

      //
      // 3a Insert an atom, set change status to false 
      //      
      addToLog("    3a. Insert an atom, set change status to false ... " + date_format.format(timestamp));
      
      MolecularAction ma3a = new MolecularInsertAtomAction(atom);
      client.setChangeStatus(false);
      client.processAction(ma3a);
      
      // re-read concept
      concept = client.getConcept(concept);

      atom = client.getAtom(atom);
      char prev_atom_status = atom.getStatus();
      char prev_atom_tobereleased = atom.getTobereleased();
      String prev_atom_suppressible = atom.getSuppressible();
      Source prev_atom_source = atom.getSource();
      Termgroup prev_atom_termgroup = atom.getTermgroup();
      Code prev_atom_code = atom.getCode();
      StringIdentifier prev_atom_sui = atom.getSUI();

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
      // 4a Change an atom 
      //      
      addToLog("    4a. Change an atom, set status to 'N' ... " + date_format.format(timestamp));
      
      MolecularAction ma4a = new MolecularChangeAtomAction(atom);
      atom.setStatus('N');
      atom.setTobereleased('N');
      atom.setSuppressible("Y");
      atom.setSource(client.getSource("SRC"));
      atom.setTermgroup(client.getTermgroup("MTH/PN"));
      atom.setCode(Code.newCode("MYCODE"));
      atom.setSUI(new StringIdentifier("S0001137"));
      client.setChangeStatus(true);
      client.processAction(ma4a);

      // re-read atom      
      atom = client.getAtom(atom);

      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
     
      addToLog("        Current atom status must not be equal to previous atom status ...");
      if (atom.getStatus() != prev_atom_status)
        addToLog("    4a1. Test Passed");
      else {
        addToLog("    4a1. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current atom tobereleased value must not be equal to previous atom tobereleased value ...");
      if (atom.getTobereleased() != prev_atom_tobereleased)
        addToLog("    4a2. Test Passed");
      else {
        addToLog("    4a2. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current atom suppressible value must not be equal to previous atom suppressible value ...");
      if (!atom.getSuppressible().equals(prev_atom_suppressible))
        addToLog("    4a3. Test Passed");
      else {
        addToLog("    4a3. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current atom source value must not be equal to previous atom source value ...");
      if (!atom.getSource().equals(prev_atom_source))
        addToLog("    4a4. Test Passed");
      else {
        addToLog("    4a4. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current atom termgroup value must not be equal to previous atom termgroup value ...");
      if (!atom.getTermgroup().equals(prev_atom_termgroup))
        addToLog("    4a5. Test Passed");
      else {
        addToLog("    4a5. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current atom code value must not be equal to previous atom code value ...");
      if (!atom.getCode().equals(prev_atom_code))
        addToLog("    4a6. Test Passed");
      else {
        addToLog("    4a6. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current atom sui value must not be equal to previous atom sui value ...");
      if (!atom.getSUI().equals(prev_atom_sui))
        addToLog("    4a7. Test Passed");
      else {
        addToLog("    4a7. Test Failed");
        thisTestFailed();
      }

      //
      // 4b. Undo 4a
      //
      addToLog("    4b. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma4b = ma4a;
      ma4b.setTransactionIdentifier(tid4a);
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
      // 6a. Undo 1a
      //
      addToLog("    6a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma6a = ma1a;
      ma6a.setTransactionIdentifier(tid1a);
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
      addToLog("Finished MolecularChangeAtomActionTest at " +
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
