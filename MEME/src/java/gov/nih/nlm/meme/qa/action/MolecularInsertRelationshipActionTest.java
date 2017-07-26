/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularInsertRelationshipActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularInsertRelationshipActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularInsertRelationshipActionTest}.
   */	
  public MolecularInsertRelationshipActionTest() {
    setName("MolecularInsertRelationshipActionTest");
    setDescription("Test suite for molecular insert relationship actions");
  }
  
  /**
   * Perform molecular insert relationship action test.
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
      
      Concept source = client.getConcept(101);     
      Concept target = client.getConcept(102);      

      //
      // 1a. Insert an atom1
      //
      addToLog("    1a. Insert an atom1 ... " + date_format.format(timestamp));

      // Create an atom1
      Atom atom1 = new Atom.Default();
      atom1.setString("TEST ATOM 1");
      atom1.setTermgroup(client.getTermgroup("MTH/PT"));
      atom1.setSource(client.getSource("MTH"));
      atom1.setCode(Code.newCode("NOCODE"));
      atom1.setLanguage(new Language.Default("English","ENG"));
      atom1.setStatus('R');
      atom1.setGenerated(true);
      atom1.setReleased('N');
      atom1.setTobereleased('Y');
      atom1.setSuppressible("N");
      atom1.setConcept(source);

      source.addAtom(atom1);

      MolecularAction ma1a = new MolecularInsertAtomAction(atom1);
      client.setChangeStatus(true);
      client.processAction(ma1a);

      // re-read concept
      source = client.getConcept(source);
      atom1 = client.getAtom(atom1);     
      
      addToLog("        Atom must be inserted ...");
      if (atom1 != null)
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid1a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a);
      
      //
      // 2a Insert an atom2 
      //      
      addToLog("    2a. Insert an atom2 (foreign atom) ... " + date_format.format(timestamp));

      // Create an atom2
      Atom atom2 = new Atom.Default();
      atom2.setString("TEST ATOM 2");
      atom2.setTermgroup(client.getTermgroup("MTH/PT"));
      atom2.setSource(client.getSource("MTH"));
      atom2.setCode(Code.newCode("NOCODE"));
      atom2.setLanguage(new Language.Default("Spanish","SPA"));      
      atom2.setStatus('R');
      atom2.setGenerated(true);
      atom2.setReleased('N');
      atom2.setTobereleased('Y');
      atom2.setSuppressible("N");
      atom2.setConcept(target);
      
      source.addAtom(atom2);

      MolecularAction ma2a = new MolecularInsertAtomAction(atom2);
      client.setChangeStatus(true);
      client.processAction(ma2a);

      // re-read concept      
      source = client.getConcept(source);
      atom2 = client.getAtom(atom2);     
      
      addToLog("        foreign atom must be inserted ...");
      if (atom2 != null)
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      //
      // 3a Insert an atom3 
      //      
      addToLog("    3a. Insert an atom3 ... " + date_format.format(timestamp));

      // Create an atom3
      Atom atom3 = new Atom.Default();
      atom3.setString("TEST ATOM 3");
      atom3.setTermgroup(client.getTermgroup("MTH/PT"));
      atom3.setSource(client.getSource("MTH"));
      atom3.setCode(Code.newCode("NOCODE"));
      atom3.setStatus('R');
      atom3.setGenerated(true);
      atom3.setReleased('N');
      atom3.setTobereleased('Y');
      atom3.setSuppressible("N");
      atom3.setConcept(source);
      
      atom1.addTranslationAtom(atom3);
      source.addAtom(atom3);

      MolecularAction ma3a = new MolecularInsertAtomAction(atom3);
      client.setChangeStatus(true);
      client.processAction(ma3a);

      // re-read concept      
      source = client.getConcept(source);
      atom3 = client.getAtom(atom3);     
      
      addToLog("        Atom must be inserted ...");
      if (atom3 != null)
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      //
      // 4a. Approve a concept
      //
      addToLog("    4a. Approve a concept ... " + date_format.format(timestamp));

      MolecularAction ma4a = new MolecularApproveConceptAction(source);
      client.processAction(ma4a);
      
      // re-read concept
      source = client.getConcept(source);    
    
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
   
      addToLog("        Concept status must be equal to 'R' ...");
      if (source.getStatus() == 'R')
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }      

      //
      // 5a Insert a P level relationship 
      //            
      addToLog("    5a. Insert a P level relationship ... " + date_format.format(timestamp));

      Relationship rel1 = new Relationship.Default();
      rel1.setAtom(atom1);
      rel1.setRelatedAtom(atom2);
      rel1.setConcept(source);
      rel1.setRelatedConcept(target);
      rel1.setName("RT?");
      rel1.setAttribute("mapped_to");
      rel1.setSource(client.getSource("MTH"));
      rel1.setSourceOfLabel(client.getSource("MTH"));
      rel1.setStatus('N');
      rel1.setGenerated(false);
      rel1.setLevel('P');
      rel1.setReleased('A');
      rel1.setTobereleased('Y');
      rel1.setSuppressible("N");
      rel1.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel1.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      source.addRelationship(rel1);
      
      MolecularAction ma5a = new MolecularInsertRelationshipAction(rel1);
      client.processAction(ma5a);

      // re-read concept
      source = client.getConcept(source);
      rel1 = client.getRelationship(rel1);

      int tid5a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5a);

      addToLog("        P level relationship must be inserted ...");
      if (rel1 != null)
        addToLog("    5a1. Test Passed");
      else {
        addToLog("    5a1. Test Failed");
        thisTestFailed();
      }      

      addToLog("        Source status must be N ...");
      if (source.getStatus() == 'N')
        addToLog("    5a2. Test Passed");
      else {
        addToLog("    5a2. Test Failed");
        thisTestFailed();
      }      

      //
      // 6a Insert another P level relationship 
      //            
      addToLog("    6a. Insert another P level relationship ... " + date_format.format(timestamp));

      Relationship rel2 = new Relationship.Default();
      rel2.setAtom(atom1);
      rel2.setRelatedAtom(atom3);
      rel2.setConcept(source);
      rel2.setRelatedConcept(target);
      rel2.setName("RT?");
      rel2.setAttribute("dose_form_of");
      rel2.setSource(client.getSource("MTH"));
      rel2.setSourceOfLabel(client.getSource("MTH"));
      rel2.setStatus('N');
      rel2.setGenerated(false);
      rel2.setLevel('P');
      rel2.setReleased('A');
      rel2.setTobereleased('Y');
      rel2.setSuppressible("N");
      rel2.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel2.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      source.addRelationship(rel2);
      
      MolecularAction ma6a = new MolecularInsertRelationshipAction(rel2);
      client.processAction(ma6a);

      // re-read concept
      source = client.getConcept(source);
      rel2 = client.getRelationship(rel2);
      
      addToLog("        P level relationship must be inserted ...");
      if (rel2 != null)
        addToLog("    6a. Test Passed");
      else {
        addToLog("    6a. Test Failed");
        thisTestFailed();
      }      

      int tid6a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6a);
     
      //
      // 7a Insert a C level relationship 
      //            
      addToLog("    7a. Insert a C level relationship ... " + date_format.format(timestamp));

      Relationship rel3 = new Relationship.Default();
      rel3.setAtom(atom1);
      rel3.setRelatedAtom(atom2);
      rel3.setConcept(source);
      rel3.setRelatedConcept(target);
      rel3.setName("RT?");
      rel3.setAttribute("mapped_to");
      rel3.setSource(client.getSource("MTH"));
      rel3.setSourceOfLabel(client.getSource("MTH"));
      rel3.setStatus('R');
      rel3.setGenerated(false);
      rel3.setLevel('C');
      rel3.setReleased('A');
      rel3.setTobereleased('Y');
      rel3.setSuppressible("N");
      rel3.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel3.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      source.addRelationship(rel3);
      
      MolecularAction ma7a = new MolecularInsertRelationshipAction(rel3);
      client.processAction(ma7a);

      // re-read concept
      source = client.getConcept(source);
      rel3 = client.getRelationship(rel3);

      int tid7a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7a);

      addToLog("        Concept level relationship must be inserted ...");
      if (rel3 != null)
        addToLog("    7a. Test Passed");
      else {
        addToLog("    7a. Test Failed");
        thisTestFailed();
      }      

      //
      // 8a. Insert a D status P level relationship
      //
      addToLog("    8a. Insert a D status P level relationship ... " + date_format.format(timestamp));      

      Relationship rel4 = new Relationship.Default();
      rel4.setAtom(atom2);
      rel4.setRelatedAtom(atom1);
      rel4.setConcept(source);
      rel4.setRelatedConcept(target);
      rel4.setName("RT?");
      rel4.setAttribute("mapped_to");
      rel4.setSource(client.getSource("MTH"));
      rel4.setSourceOfLabel(client.getSource("MTH"));
      rel4.setStatus('D');
      rel4.setGenerated(false);
      rel4.setLevel('P');
      rel4.setReleased('A');
      rel4.setTobereleased('Y');
      rel4.setSuppressible("N");
      rel4.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel4.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      MolecularAction ma8a = new MolecularInsertRelationshipAction(rel4);      
      client.setChangeStatus(true);
      client.processAction(ma8a);
      
      // re-read concept
      source = client.getConcept(source);
      rel4 = client.getRelationship(rel4);

      int tid8a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid8a);

      addToLog("        P level relationship must be inserted ...");
      if (rel4 != null)
        addToLog("    8a1. Test Passed");
      else {
        addToLog("    8a1. Test Failed");
        thisTestFailed();
      }      
      
      //
      // 9a. Undo 8a
      //
      addToLog("    9a. Undo 8a ... "
               + date_format.format(timestamp));

      MolecularAction ma9a = ma8a;
      ma9a.setTransactionIdentifier(tid8a);
      client.processUndo(ma9a);

      //
      // 10a. Undo 7a
      //
      addToLog("    10a. Undo 7a ... "
               + date_format.format(timestamp));

      MolecularAction ma10a = ma7a;
      ma10a.setTransactionIdentifier(tid7a);
      client.processUndo(ma10a);
      
      //
      // 11a. Undo 6a
      //
      addToLog("    11a. Undo 6a ... "
               + date_format.format(timestamp));

      MolecularAction ma11a = ma6a;
      ma11a.setTransactionIdentifier(tid6a);
      client.processUndo(ma11a);
      
      //
      // 12a. Undo 5a
      //
      addToLog("    12a. Undo 5a ... "
               + date_format.format(timestamp));

      MolecularAction ma12a = ma5a;
      ma12a.setTransactionIdentifier(tid5a);
      client.processUndo(ma12a);
      
      //
      // 13a. Undo 4a
      //
      addToLog("    13a. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma13a = ma4a;
      ma13a.setTransactionIdentifier(tid4a);
      client.processUndo(ma13a);

      //
      // 14a. Undo 3a
      //
      addToLog("    14a. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma14a = ma3a;
      ma14a.setTransactionIdentifier(tid3a);
      client.processUndo(ma14a);

      //
      // 15a. Undo 2a
      //
      addToLog("    15a. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma15a = ma2a;
      ma15a.setTransactionIdentifier(tid2a);
      client.processUndo(ma15a);
      
      //
      // 16a. Undo 1a
      //
      addToLog("    16a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma16a = ma1a;
      ma16a.setTransactionIdentifier(tid1a);
      client.processUndo(ma16a);
      
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
      addToLog("Finished MolecularInsertRelationshipActionTest at " +
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
