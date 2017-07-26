/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularChangeAttributeActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularChangeAttributeActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularChangeAttributeActionTest}.
   */	
  public MolecularChangeAttributeActionTest() {
    setName("MolecularChangeAttributeActionTest");
    setDescription("Test suite for molecular change attribute actions");
  }
  
  /**
   * Perform molecular change attribute action test.
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
      
      Atom atom1 = client.getAtom(101);
      Atom atom2 = client.getAtom(102);

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
      attr.setAtom(atom1);
      attr.setLevel('S');
      attr.setName("CONCEPT_NOTE");
      attr.setValue("test concept_note.");
      attr.setSource(client.getSource("MTH"));
      attr.setStatus('N');
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

      // re-read concept
      concept = client.getConcept(concept);

      //
      // 3a Insert an attribute, set change status to false 
      //      
      addToLog("    3a. Insert an attribute, set change status to false ... " + date_format.format(timestamp));
      
      MolecularAction ma3a = new MolecularInsertAttributeAction(attr);
      client.setChangeStatus(false);
      client.processAction(ma3a);
      
      // re-read concept
      concept = client.getConcept(concept);
      attr = client.getAttribute(attr);
      char prev_attr_status = attr.getStatus();
      char prev_attr_tobereleased = attr.getTobereleased();
      String prev_attr_suppressible = attr.getSuppressible();
      Source prev_attr_source = attr.getSource();
      String prev_attr_name = attr.getName();
      int prev_attr_level = attr.getLevel();
      Atom prev_attr_atom = attr.getAtom();

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
      // 4a Change an attribute 
      //      
      addToLog("    4a. Change an attribute ... " + date_format.format(timestamp));
      
      MolecularAction ma4a = new MolecularChangeAttributeAction(attr);
      attr.setStatus('R');
      attr.setTobereleased('N');
      attr.setSuppressible("Y");
      attr.setSource(client.getSource("SRC"));
      attr.setLevel('P');
      attr.setName("LANGUAGECODE");
      attr.setAtom(atom2);
      client.setChangeStatus(true);
      client.processAction(ma4a);

      // re-read attribute     
      attr = client.getAttribute(attr);

      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
     
      addToLog("        Current attribute status must not be equal to previous attribute status ...");
      if (attr.getStatus() != prev_attr_status)
        addToLog("    4a1. Test Passed");
      else {
        addToLog("    4a1. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current attribute tobereleased value must not be equal to previous attribute tobereleased value ...");
      if (attr.getTobereleased() != prev_attr_tobereleased)
        addToLog("    4a2. Test Passed");
      else {
        addToLog("    4a2. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current attribute suppressible value must not be equal to previous attribute suppressible value ...");
      if (!attr.getSuppressible().equals(prev_attr_suppressible))
        addToLog("    4a3. Test Passed");
      else {
        addToLog("    4a3. Test Failed");
        thisTestFailed();
      }
      
      addToLog("        Current attribute source value must not be equal to previous attribute source value ...");
      if (!attr.getSource().equals(prev_attr_source))
        addToLog("    4a4. Test Passed");
      else {
        addToLog("    4a4. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current attribute name value must not be equal to previous attribute name value ...");
      if (!attr.getName().equals(prev_attr_name))
        addToLog("    4a5. Test Passed");
      else {
        addToLog("    4a5. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current attribute level value must not be equal to previous attribute level value ...");
      if (attr.getLevel() != prev_attr_level)
        addToLog("    4a6. Test Passed");
      else {
        addToLog("    4a6. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current attribute atom value must not be equal to previous attribute atom value ...");
      if (!attr.getAtom().equals(prev_attr_atom))
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
      client.processUndo(ma4b);      
      
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
      addToLog("Finished MolecularChangeAttributeActionTest at " +
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
