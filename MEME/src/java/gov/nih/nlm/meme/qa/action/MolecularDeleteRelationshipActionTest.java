/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularDeleteRelationshipActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularDeleteRelationshipActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularDeleteRelationshipActionTest}.
   */	
  public MolecularDeleteRelationshipActionTest() {
    setName("MolecularDeleteRelationshipActionTest");
    setDescription("Test suite for molecular delete relationship actions");
  }
  
  /**
   * Perform molecular delete relationship action test.
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

      Concept concept1 = client.getConcept(101);
      Concept concept2 = client.getConcept(102);      
      
      //
      // 1a. Approve a concept
      //
      addToLog("    1a. Approve a concept ... " + date_format.format(timestamp));

      MolecularAction ma1a = new MolecularApproveConceptAction(concept1);
      client.processAction(ma1a);
      
      // re-read concept
      concept1 = client.getConcept(concept1);    
      char prev_status = concept1.getStatus();
    
      // Save Transaction ID
      int tid1a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a);
   
      addToLog("        Current concept status and previous status must be equal to 'R' ...");
      if (concept1.getStatus() == prev_status && concept1.getStatus() == 'R')
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }      

      //
      // 2a. Insert a concept level relationship, set change status to false
      //
      addToLog("    2a. Insert a concept level relationship, set change status to false ... " + date_format.format(timestamp));

      Relationship rel = new Relationship.Default();
      rel.setConcept(concept1);
      rel.setRelatedConcept(concept2);
      rel.setAtom(client.getAtom(101));
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
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      MolecularAction ma2a = new MolecularInsertRelationshipAction(rel);
      client.setChangeStatus(false);
      client.processAction(ma2a);

      // re-read concept      
      concept1 = client.getConcept(concept1);
      rel = client.getRelationship(rel);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      addToLog("        Current concept status must be equal to previous status ...");
      if (concept1.getStatus() == prev_status)
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }      

      addToLog("        Current concept must be equal to previous concept ...");
      if (rel.getConcept().equals(concept1))
        addToLog("    2a2. Test Passed");
      else {
        addToLog("    2a2. Test Failed");
        thisTestFailed();
      }      
    
      //
      // 3a. Delete a relationship
      //
      
      /* The delete a relationship with an attribute connected to it is not tested
       * because no molecular action supported the insert of that case yet.
       */      
      
      addToLog("    3a. Delete a relationship, set change status to true ... " + date_format.format(timestamp));
      
      MolecularAction ma3a = new MolecularDeleteRelationshipAction(rel);
      client.setChangeStatus(true);
      client.processAction(ma3a);
     
      // re-read concept
      concept1 = client.getConcept(concept1);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);
          
      addToLog("        Current concept status must not be equal to previous status ...");
      if (concept1.getStatus() != prev_status)
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
      // 4a. Undo 2a
      //
      addToLog("    4a. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma4a = ma2a;
      ma4a.setTransactionIdentifier(tid2a);
      client.processUndo(ma4a);      

      //
      // 5a. Undo 1a
      //
      addToLog("    5a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma5a = ma1a;
      ma5a.setTransactionIdentifier(tid1a);
      client.processUndo(ma5a);      

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
      addToLog("Finished MolecularDeleteRelationshipActionTest at " +
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
