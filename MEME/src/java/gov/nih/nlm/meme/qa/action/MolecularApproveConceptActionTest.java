/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularApproveConceptActionTest
 *
 * 12/05/2005 RBE (1-72UTX): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularApproveConceptActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularApproveConceptActionTest}.
   */	
  public MolecularApproveConceptActionTest() {
    setName("MolecularApproveConceptActionTest");
    setDescription("Test suite for molecular approve concept actions");
  }
  
  /**
   * Perform molecular approve concept action test.
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

      client.setAuthority(new Authority.Default("L-MEME4"));      
      Concept concept = client.getConcept(101);
      
      //
      // 1a. Insert an atom
      //
      addToLog("    1a. Insert an atom ... " + date_format.format(timestamp));

      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup("MTH/PT"));
      atom.setSource(client.getSource("MTH"));
      atom.setCode(Code.newCode("NOCODE"));
      atom.setStatus('N');
      atom.setGenerated(true);
      atom.setReleased('A');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept);
      
      concept.addAtom(atom);

      MolecularAction ma1a = new MolecularInsertAtomAction(atom);
      client.processAction(ma1a);

      // re-read concept      
      concept = client.getConcept(concept);

      // Save Transaction ID
      int tid1a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a);

      //
      // 2a. Insert an attribute
      //
      addToLog("    2a. Insert an attribute ... " + date_format.format(timestamp));
      
      Attribute attr = new Attribute.Default();
      attr.setAtom(atom);
      attr.setLevel('C');
      attr.setName("CONCEPT_NOTE");
      attr.setValue("test concept_note.");
      attr.setSource(client.getSource("MTH"));
      attr.setStatus('N');
      attr.setGenerated(false);
      attr.setReleased('A');
      attr.setTobereleased('Y');
      attr.setSuppressible("N");
      attr.setConcept(concept);

      concept.addAttribute(attr);

      MolecularAction ma2a = new MolecularInsertAttributeAction(attr);
      client.processAction(ma2a);

      // re-read concept      
      concept = client.getConcept(concept);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);
      
      //
      // 3a. Insert a concept level relationship
      //
      addToLog("    3a. Insert a concept level relationship ... " + date_format.format(timestamp));

      Relationship rel = new Relationship.Default();
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(102));
      rel.setName("RT?");
      rel.setAttribute("translation_of");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('N');
      rel.setGenerated(false);
      rel.setLevel('C');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      addToLog("        rel.getStatus() = " + rel.getStatus());
      addToLog("        rel.getName() = " + rel.getName());

      concept.addRelationship(rel);    

      MolecularAction ma3a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma3a);
      
      // re-read concept      
      concept = client.getConcept(concept);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      //
      // 4a. Approve concept
      //
      addToLog("    4a. Approve a concept ... " + date_format.format(timestamp));

      MolecularAction ma4a = new MolecularApproveConceptAction(concept);
      client.processAction(ma4a);
      
      // re-read concept
      concept = client.getConcept(concept);
      rel = client.getRelationship(rel);
    
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
   
      addToLog("        Relationship status must be equal to 'R' ...");
      if (rel.getStatus() == 'R')
        addToLog("        Test Passed");
      else {
        addToLog("        Test Failed");
        thisTestFailed();
      }
      
      addToLog("        Relationship name must be equal to 'RT' ...");
      if (rel.getName().equals("RT"))
        addToLog("        Test Passed");
      else {
        addToLog("        Test Failed");
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
      // 6a. Insert a P level relationship
      //
      addToLog("    6a. Insert a P level relationship ... " + date_format.format(timestamp));

      rel = new Relationship.Default();
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(104));
      rel.setAtom(atom);
      rel.setRelatedAtom(client.getAtom(103));
      rel.setName("LK");
      rel.setAttribute("translation_of");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('D');
      rel.setGenerated(false);
      rel.setLevel('P');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma6a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma6a);
      
      // re-read concept      
      concept = client.getConcept(concept);

      // Save Transaction ID
      int tid6a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6a);

      //
      // 7a. Approve concept
      //
      addToLog("    7a. Approve a concept ... " + date_format.format(timestamp));

      MolecularAction ma7a = new MolecularApproveConceptAction(concept);
      client.processAction(ma7a);
      
      // re-read concept
      concept = client.getConcept(concept);
    
      // Save Transaction ID
      int tid7a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7a);

      addToLog("        Matching P level relationship is deleted.");

      //
      // 8a. Undo 7a
      //
      addToLog("    8a. Undo 7a ... "
               + date_format.format(timestamp));

      MolecularAction ma8a = ma7a;
      ma8a.setTransactionIdentifier(tid7a);
      client.processUndo(ma8a);

      //
      // 9a. Undo 6a
      //
      addToLog("    9a. Undo 6a ... "
               + date_format.format(timestamp));

      MolecularAction ma9a = ma6a;
      ma9a.setTransactionIdentifier(tid6a);
      client.processUndo(ma9a);

      //
      // 10a. Insert a P level relationship
      //
      addToLog("    10a. Insert a P level relationship ... " + date_format.format(timestamp));
      
      rel = new Relationship.Default();
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(104));
      rel.setAtom(atom);
      rel.setRelatedAtom(client.getAtom(102));
      rel.setName("LK");
      rel.setAttribute("translation_of");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('N');
      rel.setGenerated(false);
      rel.setLevel('P');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma10a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma10a);
      
      // re-read concept      
      concept = client.getConcept(concept);
      
      // Save Transaction ID
      int tid10a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid10a);

      //
      // 11a. Approve concept
      //
      addToLog("    11a. Approve a concept ... " + date_format.format(timestamp));

      MolecularAction ma11a = new MolecularApproveConceptAction(concept);
      client.processAction(ma11a);
      
      // re-read concept
      concept = client.getConcept(concept);
      
      addToLog("        A Concept level relationship is inserted.");
      
      // Save Transaction ID
      int tid11a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid11a);

      //
      // 12a. Undo 11a
      //
      addToLog("    12a. Undo 11a ... "
               + date_format.format(timestamp));

      MolecularAction ma12a = ma11a;
      ma12a.setTransactionIdentifier(tid11a);
      client.processUndo(ma12a);

      //
      // 13a. Undo 10a
      //
      addToLog("    13a. Undo 10a ... "
               + date_format.format(timestamp));

      MolecularAction ma13a = ma10a;
      ma13a.setTransactionIdentifier(tid10a);
      client.processUndo(ma13a);
      
      concept.setEditingAuthority(new Authority.Default("KEVRIC"));
      
      //
      // 14a. Undo 2a
      //
      addToLog("    14a. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma14a = ma2a;
      ma14a.setTransactionIdentifier(tid2a);
      client.processUndo(ma14a);
      
     	//
      // 15a. Undo 1a
      //
      addToLog("    15a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma15a = ma1a;
      ma15a.setTransactionIdentifier(tid1a);
      client.processUndo(ma15a);
         
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
      addToLog("Finished MolecularApproveConceptActionTest at " +
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
