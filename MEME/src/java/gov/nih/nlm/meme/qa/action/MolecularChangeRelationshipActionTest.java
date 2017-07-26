/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularChangeRelationshipActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularChangeRelationshipActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularChangeRelationshipActionTest}.
   */	
  public MolecularChangeRelationshipActionTest() {
    setName("MolecularChangeRelationshipActionTest");
    setDescription("Test suite for molecular change relationship actions");
  }
  
  /**
   * Perform molecular change relationship action test.
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
      
      Atom atom1 = client.getAtom(101);
      Atom atom2 = client.getAtom(102);
      
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
      // 2a. Insert a source level relationship, set change status to true
      //
      addToLog("    2a. Insert a source level relationship, set change status to true ... " + date_format.format(timestamp));
    
      Relationship relS = new Relationship.Default();
      relS.setAtom(atom1);
      relS.setRelatedAtom(atom2);
      relS.setConcept(concept1);
      relS.setRelatedConcept(concept2);
      relS.setName("RT?");
      relS.setAttribute("mapped_to");
      relS.setSource(client.getSource("MTH"));
      relS.setSourceOfLabel(client.getSource("MTH"));
      relS.setStatus('N');
      relS.setGenerated(false);
      relS.setLevel('S');
      relS.setReleased('A');
      relS.setTobereleased('Y');
      relS.setSuppressible("N");
      relS.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      relS.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));

      MolecularAction ma2a = new MolecularInsertRelationshipAction(relS);
      client.setChangeStatus(true);
      client.processAction(ma2a);

      // re-read concept      
      concept1 = client.getConcept(concept1);
      relS = client.getRelationship(relS);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      addToLog("        Current concept status must not be equal to previous status ...");
      if (concept1.getStatus() != prev_status)
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }      

      addToLog("        Current concept must be equal to previous concept ...");
      if (relS.getConcept().equals(concept1))
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
      concept1 = client.getConcept(concept1);
      
      //
      // 3a. Insert a source level relationship, set change status to false
      //
      addToLog("    3a. Insert a source level relationship, set change status to false ... " + date_format.format(timestamp));
      
      MolecularAction ma3a = new MolecularInsertRelationshipAction(relS);
      client.setChangeStatus(false);
      client.processAction(ma3a);

      // re-read concept      
      concept1 = client.getConcept(concept1);
      relS = client.getRelationship(relS);
      char prev_rel_status = relS.getStatus();
      Source prev_rel_source = relS.getSource();
      char prev_rel_tobereleased = relS.getTobereleased();
      char prev_rel_level = relS.getLevel();
      String prev_rel_suppressible = relS.getSuppressible();
      Atom prev_rel_atom = relS.getAtom();
      String prev_rel_name = relS.getName();
      String prev_rel_attr = relS.getAttribute();

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);
    
      addToLog("        Current concept status must not be equal to 'N' and previous status must be equal to 'R' ...");
      if (concept1.getStatus() != 'N' && prev_status == 'R')
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }
      
      //
      // 4a Change (1st Change) a relationship 
      //      
      addToLog("    4a. Change (1st Change) a relationship ... " + date_format.format(timestamp));
      
      Relationship rel = relS;
      MolecularAction ma4a = new MolecularChangeRelationshipAction(rel);
      rel.setStatus('R');
      rel.setSource(client.getSource("SRC"));
      rel.setTobereleased('N');
      rel.setLevel('P');
      rel.setSuppressible("Y");
      rel.setAtom(atom2);
      rel.setName("BT");
      rel.setAttribute("ingredient_of");
      client.setChangeStatus(true);
      client.processAction(ma4a);

      // re-read relationship     
      rel = client.getRelationship(rel);
     
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
     
      addToLog("        Current relationship status must not be equal to previous relationship status ...");
      if (rel.getStatus() != prev_rel_status)
        addToLog("    4a1. Test Passed");
      else {
        addToLog("    4a1. Test Failed");
        thisTestFailed();
      }
      
      addToLog("        Current relationship source value must not be equal to previous relationship source value ...");
      if (!rel.getSource().equals(prev_rel_source))
        addToLog("    4a2. Test Passed");
      else {
        addToLog("    4a2. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current relationship tobereleased value must not be equal to previous relationship tobereleased value ...");
      if (rel.getTobereleased() != prev_rel_tobereleased)
        addToLog("    4a3. Test Passed");
      else {
        addToLog("    4a3. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current relationship level value must not be equal to previous relationship level value ...");
      if (rel.getLevel() != prev_rel_level)
        addToLog("    4a4. Test Passed");
      else {
        addToLog("    4a4. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current relationship suppressible value must not be equal to previous relationship suppressible value ...");
      if (!rel.getSuppressible().equals(prev_rel_suppressible))
        addToLog("    4a5. Test Passed");
      else {
        addToLog("    4a5. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current relationship atom value must not be equal to previous relationship atom value ...");
      if (!rel.getAtom().equals(prev_rel_atom))
        addToLog("    4a6. Test Passed");
      else {
        addToLog("    4a6. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current relationship name value must not be equal to previous relationship name value ...");
      if (!rel.getName().equals(prev_rel_name))
        addToLog("    4a7. Test Passed");
      else {
        addToLog("    4a7. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current relationship attribute value must not be equal to previous relationship attribute value ...");
      if (!rel.getAttribute().equals(prev_rel_attr))
        addToLog("    4a8. Test Passed");
      else {
        addToLog("    4a8. Test Failed");
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
      // 6a. Insert a demotion relationship
      //
      addToLog("    6a. Insert a relationship ... " + date_format.format(timestamp));
      concept2.removeAtom(atom2);
      concept2.addAtom(atom1);
      Relationship relC = new Relationship.Default();
      relC.setAtom(atom1);
      relC.setRelatedAtom(atom2);
      relC.setConcept(concept1);
      relC.setRelatedConcept(concept2);
      relC.setName("RT?");
      relC.setAttribute("mapped_to");
      relC.setSource(client.getSource("MTH"));
      relC.setSourceOfLabel(client.getSource("MTH"));
      relC.setStatus('R');
      relC.setGenerated(false);
      relC.setLevel('C');
      relC.setReleased('A');
      relC.setTobereleased('Y');
      relC.setSuppressible("N");
      relC.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      relC.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));

      MolecularAction ma6a = new MolecularInsertRelationshipAction(relC);
      client.setChangeStatus(true);
      client.processAction(ma6a);

      // re-read relationship
      relC = client.getRelationship(relC);

      // Save Transaction ID
      int tid6a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6a);
      
      //
      // 7a. Insert a concept-level relationship that is status N
      //
      addToLog("    7a. Insert a relationship ... " + date_format.format(timestamp));
      relC = client.getRelationship(relC);
      relC.setLevel('C');
      relC.setStatus('N');

      MolecularAction ma7a = new MolecularInsertRelationshipAction(relC);
      client.setChangeStatus(true);
      client.processAction(ma7a);

      // re-read relationship
      relC = client.getRelationship(relC);

      // Save Transaction ID
      int tid7a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7a);

      //
      // 8a. Insert a P-level relationship that is status D
      //
      addToLog("    8a. Insert a relationship ... " + date_format.format(timestamp));
      Relationship relP = new Relationship.Default();
      relP.setAtom(atom1);
      relP.setRelatedAtom(atom2);
      relP.setConcept(concept1);
      relP.setRelatedConcept(concept2);
      relP.setName("RT?");
      relP.setAttribute("mapped_to");
      relP.setSource(client.getSource("MTH"));
      relP.setSourceOfLabel(client.getSource("MTH"));
      relP.setStatus('D');
      relP.setGenerated(false);
      relP.setLevel('P');
      relP.setReleased('A');
      relP.setTobereleased('Y');
      relP.setSuppressible("N");
      relP.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      relP.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));

      MolecularAction ma8a = new MolecularInsertRelationshipAction(relP);
      client.setChangeStatus(true);
      client.processAction(ma8a);

      // re-read relationship
      relP = client.getRelationship(relP);

      // Save Transaction ID
      int tid8a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid8a);

      //
      // 9a Change (2nd Change) a relationship, set status to 'R' 
      //      
      addToLog("    9a. Change (2nd Change) a relationship, set status to 'R' ... " + date_format.format(timestamp));
      
      prev_rel_status = relC.getStatus();
      prev_rel_name = relC.getName();
      
      MolecularAction ma9a = new MolecularChangeRelationshipAction(relC);
      relC.setStatus('R');
      client.processAction(ma9a);

      // re-read relationship
      relC = client.getRelationship(relC);

      addToLog("        Current relationship status must not be equal to previous relationship status ...");
      if (relC.getStatus() != prev_rel_status)
        addToLog("    9a1. Test Passed");
      else {
        addToLog("    9a1. Test Failed");
        thisTestFailed();
      }

      addToLog("        Relationship name must change to RT ...");
      if (relC.getName().equals("RT"))
        addToLog("    9a2. Test Passed");
      else {
        addToLog("    9a2. Test Failed");
        thisTestFailed();
      }

      addToLog("        Current relationship name value must not be equal to previous relationship name value ...");
      if (!relC.getName().equals(prev_rel_name))
        addToLog("    9a3. Test Passed");
      else {
        addToLog("    9a3. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid9a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid9a);     

      //
      // 9b. Undo 9a
      //
      addToLog("    9b. Undo 9a ... "
               + date_format.format(timestamp));

      MolecularAction ma9b = ma9a;
      ma9b.setTransactionIdentifier(tid9a);
      client.processUndo(ma9b);

      //
      // 10b. Undo 8a
      //
      addToLog("    10b. Undo 8a ... "
               + date_format.format(timestamp));

      MolecularAction ma10a = ma8a;
      ma10a.setTransactionIdentifier(tid8a);
      client.processUndo(ma10a);

      //
      // 11a. Undo 7a
      //
      addToLog("    11a. Undo 7a ... "
               + date_format.format(timestamp));

      MolecularAction ma11a = ma7a;
      ma11a.setTransactionIdentifier(tid7a);
      client.processUndo(ma11a);

      //
      // 12a. Undo 6a
      //
      addToLog("    12a. Undo 6a ... "
               + date_format.format(timestamp));

      MolecularAction ma12a = ma6a;
      ma12a.setTransactionIdentifier(tid6a);
      client.processUndo(ma12a);

      //
      // 13a. Undo 1a
      //
      addToLog("    13a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma13a = ma1a;
      ma13a.setTransactionIdentifier(tid1a);
      client.processUndo(ma13a);      
      
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
      addToLog("Finished MolecularChangeRelationshipActionTest at " +
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
