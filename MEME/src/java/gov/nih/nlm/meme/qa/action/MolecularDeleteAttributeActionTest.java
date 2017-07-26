/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularDeleteAttributeActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularDeleteAttributeActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularDeleteAttributeActionTest}.
   */	
  public MolecularDeleteAttributeActionTest() {
    setName("MolecularDeleteAttributeActionTest");
    setDescription("Test suite for molecular delete attribute actions");
  }
  
  /**
   * Perform molecular delete attribute action test.
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
      Atom atom = client.getAtom(101);

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
      // 2a. Insert an attribute, set change status to true
      //
      addToLog("    2a. Insert an attribute, set change status to true ... " + date_format.format(timestamp));

      Attribute attr = new Attribute.Default();
      attr.setAtom(atom);
      attr.setLevel('C');
      attr.setName("CONCEPT_NOTE");
      attr.setValue("test concept_note.");
      attr.setSource(client.getSource("MTH"));
      attr.setStatus('R');
      attr.setGenerated(false);
      attr.setReleased('A');
      attr.setTobereleased('Y');
      attr.setSuppressible("N");
      attr.setConcept(concept);

      MolecularAction ma2a = new MolecularInsertAttributeAction(attr);
      client.setChangeStatus(true);
      client.processAction(ma2a);

      // re-read concept      
      concept = client.getConcept(concept);
      attr = client.getAttribute(attr);     

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
      if (attr.getConcept().equals(concept))
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
      // 3a Insert an attribute, set change status to false 
      //      
      addToLog("    3a. Insert an attribute, set change status to false ... " + date_format.format(timestamp));
      
      MolecularAction ma3a = new MolecularInsertAttributeAction(attr);
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
      // 4a. Delete attribute
      //
      addToLog("    4a. Delete attribute, set status to true ... " + date_format.format(timestamp));

      MolecularAction ma4a = new MolecularDeleteAttributeAction(attr);
      client.setChangeStatus(true);
      client.processAction(ma4a);
      
      // re-read concept
      concept = client.getConcept(concept);    
    
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);

      addToLog("        Current concept status must not be equal to previous status ...");
      if (concept.getStatus() != prev_status)
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }      

      //
      // 4b. Undo 4a
      //
      addToLog("    4b. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma4b = ma4a;
      ma4b.setTransactionIdentifier(tid4a);
      client.processUndo(ma4b);      

      // re-read concept      
      concept = client.getConcept(concept);
    
      addToLog("        Current concept status must not be equal to 'N' and previous status must be equal to 'R' ...");
      if (concept.getStatus() != 'N' && prev_status == 'R')
        addToLog("    4b. Test Passed");
      else {
        addToLog("    4b. Test Failed");
        thisTestFailed();
      }
            
      //
      // 5a. Undo 3a
      //
      addToLog("    5a. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma5a = ma3a;
      ma5a.setTransactionIdentifier(tid3a);
      client.processUndo(ma5a);      
     
      addToLog("        Current concept status and previous status must be equal to 'R' ...");
      if (concept.getStatus() == prev_status && concept.getStatus() == 'R')
        addToLog("    5a. Test Passed");
      else {
        addToLog("    5a. Test Failed");
        thisTestFailed();
      }      
      
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
      addToLog("Finished MolecularDeleteAttributeActionTest at " +
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
