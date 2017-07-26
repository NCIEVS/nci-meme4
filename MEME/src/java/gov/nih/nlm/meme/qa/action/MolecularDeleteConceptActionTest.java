/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularDeleteConceptActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularDeleteConceptActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularDeleteConceptActionTest}.
   */	
  public MolecularDeleteConceptActionTest() {
    setName("MolecularDeleteConceptActionTest");
    setDescription("Test suite for molecular delete concept actions");
  }
  
  /**
   * Perform molecular delete concept action test.
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
      // 1a. Insert an atom
      //
      addToLog("    1a. Insert an atom ... " + date_format.format(timestamp));

      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup("MTH/PT"));
      atom.setSource(client.getSource("MTH"));
      atom.setCode(Code.newCode("NOCODE"));
      atom.setLevel('C');
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept);
      
      concept.addAtom(atom);

      MolecularAction ma1a = new MolecularInsertAtomAction(atom);
      client.processAction(ma1a);

      // re-read concept
      concept = client.getConcept(concept);
      atom = client.getAtom(atom);

      // Save Transaction ID
      int tid1a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a);

      addToLog("        Atom id must not be null ...");
      if (atom.getIdentifier() != null)
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

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
      attr.setStatus('R');
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
      attr = client.getAttribute(attr);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      addToLog("        Attribute id must not be null ...");
      if (attr.getIdentifier() != null)
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      //
      // 3a. Insert a relationship
      //
      addToLog("    3a. Insert a relationship ... " + date_format.format(timestamp));

      Relationship rel = new Relationship.Default();
      rel.setAtom(atom);
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(102));
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
      
      concept.addRelationship(rel);    

      MolecularAction ma3a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma3a);

      // re-read concept
      concept = client.getConcept(concept);
      rel = client.getRelationship(rel);
      
      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      addToLog("        Relationship id must not be null ...");
      if (rel.getIdentifier() != null)
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }

      //
      // 4a. Approve concept
      //
      addToLog("    4a. Approve a concept ... " + date_format.format(timestamp));

      MolecularAction ma4a = new MolecularApproveConceptAction(concept);
      client.processAction(ma4a);
      
      // re-read concept
      concept = client.getConcept(concept);    
    
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
   
      addToLog("        Current concept status must be equal to 'R' ...");
      if (concept.getStatus() == 'R')
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }
      
      //
      // 5a. Delete a concept
      //
      addToLog("    5a. Delete a concept ... " + date_format.format(timestamp));
      
      MolecularAction ma5a = new MolecularDeleteConceptAction(concept);
      client.processAction(ma5a);
     
      // Save Transaction ID
      int tid5a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5a);
          
      //
      // 5b. Undo 5a
      //
      addToLog("    5b. Undo 5a ... "
               + date_format.format(timestamp));

      MolecularAction ma5b = ma5a;
      ma5b.setTransactionIdentifier(tid5a);
      client.processUndo(ma5b);      

      //
      // 6a. Undo 4a
      //
      addToLog("    6a. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma6a = ma4a;
      ma6a.setTransactionIdentifier(tid4a);
      client.processUndo(ma6a);      

      //
      // 7a. Undo 3a
      //
      addToLog("    7a. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma7a = ma3a;
      ma7a.setTransactionIdentifier(tid3a);
      client.processUndo(ma7a);      

      //
      // 8a. Undo 2a
      //
      addToLog("    8a. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma8a = ma2a;
      ma8a.setTransactionIdentifier(tid2a);
      client.processUndo(ma8a);      

      //
      // 9a. Undo 1a
      //
      addToLog("    9a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma9a = ma1a;
      ma9a.setTransactionIdentifier(tid1a);
      client.processUndo(ma9a);      

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
      addToLog("Finished MolecularDeleteConceptActionTest at " +
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
