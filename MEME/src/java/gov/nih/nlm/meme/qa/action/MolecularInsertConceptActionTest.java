/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularInsertConceptActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularInsertConceptActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularInsertConceptActionTest}.
   */	
  public MolecularInsertConceptActionTest() {
    setName("MolecularInsertConceptActionTest");
    setDescription("Test suite for molecular insert concept actions");
  }
  
  /**
   * Perform molecular insert concept action test.
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

      //
      // 1a. Insert a concept
      //
      addToLog("    1a. Insert a concept without an attribute ... " + date_format.format(timestamp));
      Concept concept = new Concept.Default();

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
      // Set editing_authority, editing_timestamp, and 
      // approval_molecule_id
      Authority authority = new Authority.Default("KEVRIC");
      concept.setEditingAuthority(authority);
      java.util.Date today = new java.util.Date();
      concept.setEditingTimestamp(today);
      LoggedAction action = null;
      concept.setApprovalAction(action);

      MolecularAction ma1a = new MolecularInsertConceptAction(concept);
      client.processAction(ma1a);
      concept = client.getConcept(concept);    
     
      // Save Transaction ID
      int tid1a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a);

      // Check if the concept contains an atom
      boolean atom_found = false;
      Atom[] atoms = concept.getAtoms(); 
      for (int i=0; i<atoms.length; i++) {
       	atom_found = true;
      }
      
      addToLog("        Concept contain an atom ...");
      if (atom_found)
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }
            
      // Check if the concept does not contains an attribute
      boolean attr_found = false;
      Attribute[] attrs = concept.getAttributes(); 
      for (int i=0; i<attrs.length; i++) {
       	attr_found = true;
      }
      
      addToLog("        Concept does not contain an attribute ...");
      if (!attr_found)
        addToLog("    1a2. Test Passed");
      else {
        addToLog("    1a2. Test Failed");
        thisTestFailed();
      }
      
      //
      // 1b. Undo 1a
      //
      addToLog("    1b. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma1b = ma1a;
      ma1b.setTransactionIdentifier(tid1a);
      client.processUndo(ma1b);      

      //
      // 2a. Insert a concept
      //
      addToLog("    2a. Insert a concept with an attribute ... " + date_format.format(timestamp));
     
      // Create an attribute
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

      MolecularAction ma2a = new MolecularInsertConceptAction(concept);
      client.processAction(ma2a);
      concept = client.getConcept(concept);    
     
      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);
     
      // Check if the concept contains an atom
      atom_found = false;
      atoms = concept.getAtoms(); 
      for (int i=0; i<atoms.length; i++) {
       	atom_found = true;
      }
      
      addToLog("        Concept contain an atom ...");
      if (atom_found)
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      // Check if the concept contains an attribute
      attr_found = false;
      for (int i=0; i<concept.getAttributes().length; i++) {
       	attr_found = true;
      }
      
      addToLog("        Concept contains an attribute ...");
      if (attr_found)
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
      ma1b.setTransactionIdentifier(tid2a);
      client.processUndo(ma2b);      
      
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
      addToLog("Finished MolecularInsertConceptActionTest at " +
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
