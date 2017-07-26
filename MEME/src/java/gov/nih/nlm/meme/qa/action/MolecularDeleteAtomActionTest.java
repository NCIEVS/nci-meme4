/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularDeleteAtomActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
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

public class MolecularDeleteAtomActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularDeleteAtomActionTest}.
   */	
  public MolecularDeleteAtomActionTest() {
    setName("MolecularDeleteAtomActionTest");
    setDescription("Test suite for molecular delete atom actions");
  }
  
  /**
   * Perform molecular delete atom action test.
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
      atom1.setConcept(concept);
      
      concept.addAtom(atom1);

      MolecularAction ma2a = new MolecularInsertAtomAction(atom1);
      client.setChangeStatus(true);
      client.processAction(ma2a);

      // re-read concept      
      concept = client.getConcept(concept);
      atom1 = client.getAtom(atom1);     
      
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
      if (atom1.getConcept().equals(concept))
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
      
      MolecularAction ma3a = new MolecularInsertAtomAction(atom1);
      client.setChangeStatus(false);
      client.processAction(ma3a);
      
      // re-read concept
      concept = client.getConcept(concept);
      atom1 = client.getAtom(atom1);

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

      addToLog("        Atom 1 must have an identifier ...");
      if (atom1.getIdentifier() != null)
        addToLog("    3a2. Test Passed");
      else {
        addToLog("    3a2. Test Failed");
        thisTestFailed();
      }      

      //
      // 4a Insert an atom2 
      //      
      addToLog("    4a. Insert an atom2 ... " + date_format.format(timestamp));

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
      atom2.setConcept(concept);
      
      concept.addAtom(atom2);

      MolecularAction ma4a = new MolecularInsertAtomAction(atom2);
      client.setChangeStatus(false);
      client.processAction(ma4a);

      // re-read concept      
      concept = client.getConcept(concept);
      atom2 = client.getAtom(atom2);     
      
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);

      addToLog("        Current concept status must not be equal to 'N' and previous status must be equal to 'R' ...");
      if (concept.getStatus() != 'N' && prev_status == 'R')
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }      

      addToLog("        Atom 2 must have an identifier ...");
      if (atom1.getIdentifier() != null)
        addToLog("    4a2. Test Passed");
      else {
        addToLog("    4a2. Test Failed");
        thisTestFailed();
      }      

      //
      // 5a Insert an attribute, add to atom 
      //      
      addToLog("    5a. Insert an attribute ... " + date_format.format(timestamp));

      Attribute attr = new Attribute.Default();
      attr.setAtom(atom1);
      attr.setLevel('S');
      attr.setName("ATOM_NOTE");
      attr.setValue("test concept_note.");
      attr.setSource(client.getSource("MTH"));
      attr.setStatus('R');
      attr.setGenerated(false);
      attr.setReleased('A');
      attr.setTobereleased('Y');
      attr.setSuppressible("N");
      attr.setConcept(concept);

      atom1.addAttribute(attr);

      MolecularAction ma5a = new MolecularInsertAttributeAction(attr);
      client.processAction(ma5a);

      // re-read concept
      concept = client.getConcept(concept);
      attr = client.getAttribute(attr);
      atom1 = client.getAtom(atom1);
           
      int tid5a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5a);

      boolean found = false;
      Attribute[] attrs = atom1.getAttributes();
      for (int i=0; i<attrs.length; i++) {
      	if (attr.getIdentifier().equals(attrs[i].getIdentifier()))
      		found = true;
      }
      
      addToLog("        Attribute must be connected to atom ...");
      if (found)
        addToLog("    5a. Test Passed");
      else {
        addToLog("    5a. Test Failed");
        thisTestFailed();
      }      

      //
      // 6a Insert a relationship, add to atom 
      //            
      addToLog("    6a. Insert a relationship ... " + date_format.format(timestamp));

      Relationship rel = new Relationship.Default();
      rel.setAtom(atom1);
      rel.setRelatedAtom(atom2);
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(102));
      rel.setName("RT?");
      rel.setAttribute("translation_of");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('R');
      rel.setGenerated(false);
      rel.setLevel('S');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      atom1.addRelationship(rel);    

      MolecularAction ma6a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma6a);

      // re-read concept
      concept = client.getConcept(concept);
      rel = client.getRelationship(rel);
      atom1 = client.getAtom(atom1);

      int tid6a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6a);

      found = false;
      Relationship[] rels = atom1.getRelationships();
      for (int i=0; i<rels.length; i++) {
      	if (rel.getIdentifier().equals(rels[i].getIdentifier()))
      		found = true;
      }
      
      addToLog("        Relationship must be connected to atom ...");
      if (found)
        addToLog("    6a. Test Passed");
      else {
        addToLog("    6a. Test Failed");
        thisTestFailed();
      }      

      //
      // 7a Insert a foreign atom, add to atom 
      //            
      addToLog("    7a. Insert a foreign atom ... " + date_format.format(timestamp));

      Atom foreign_atom = new Atom.Default(12345);
      foreign_atom.setString("TEST ATOM 1");
      foreign_atom.setTermgroup(client.getTermgroup("MTH/PT"));
      foreign_atom.setSource(client.getSource("MTH"));
      foreign_atom.setCode(Code.newCode("NOCODE"));
      foreign_atom.setStatus('R');
      foreign_atom.setGenerated(true);
      foreign_atom.setReleased('N');
      foreign_atom.setTobereleased('Y');
      foreign_atom.setSuppressible("N");
      foreign_atom.setConcept(concept);
      
      atom1.addTranslationAtom(foreign_atom);

      MolecularAction ma7a = new MolecularInsertAtomAction(foreign_atom);
      client.processAction(ma7a);

      // re-read concept
      concept = client.getConcept(concept);
      foreign_atom = client.getAtom(foreign_atom);
      atom1 = client.getAtom(atom1);

      int tid7a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7a);
      
      Atom[] foreign_atoms = atom1.getTranslationAtoms();
      
      addToLog("        Foreign atom must be connected to atom ...");
      if (foreign_atoms.length > 0)
        addToLog("    7a. Test Passed");
      else {
        addToLog("    7a. Test Failed");
        thisTestFailed();
      }      

      //
      // 8a. Delete an atom
      //
      addToLog("    8a. Delete an atom, set status to true ... " + date_format.format(timestamp));

      MolecularAction ma8a = new MolecularDeleteAtomAction(atom1);
      client.setChangeStatus(true);
      client.processAction(ma8a);
      
      // re-read concept
      concept = client.getConcept(concept);    

      // Save Transaction ID
      int tid8a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid8a);

      addToLog("        Current concept status must not be equal to previous status ...");
      if (concept.getStatus() != prev_status)
        addToLog("    8a. Test Passed");
      else {
        addToLog("    8a. Test Failed");
        thisTestFailed();
      }      
      
      //
      // 8b. Undo 8a
      //
      addToLog("    8b. Undo 8a ... "
               + date_format.format(timestamp));

      MolecularAction ma8b = ma8a;
      ma8b.setTransactionIdentifier(tid8a);
      client.processUndo(ma8b);      

      //
      // 9a. Undo 7a
      //
      addToLog("    9a Undo 7a ... "
               + date_format.format(timestamp));

      MolecularAction ma9a = ma7a;
      ma9a.setTransactionIdentifier(tid7a);
      client.processUndo(ma9a);
      
      //
      // 10a. Undo 6a
      //
      addToLog("    10a. Undo 6a ... "
               + date_format.format(timestamp));

      MolecularAction ma10a = ma6a;
      ma10a.setTransactionIdentifier(tid6a);
      client.processUndo(ma10a);

      //
      // 11a. Undo 5a
      //
      addToLog("    11a. Undo 5a ... "
               + date_format.format(timestamp));

      MolecularAction ma11a = ma5a;
      ma11a.setTransactionIdentifier(tid5a);
      client.processUndo(ma11a);

      //
      // 12a. Undo 4a
      //
      addToLog("    12a. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma12a = ma4a;
      ma12a.setTransactionIdentifier(tid4a);
      client.processUndo(ma12a);

      //
      // 13a. Undo 3a
      //
      addToLog("    13a. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma13a = ma3a;
      ma13a.setTransactionIdentifier(tid3a);
      client.processUndo(ma13a);

      //
      // 14a. Undo 1a
      //
      addToLog("    14a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma14a = ma1a;
      ma14a.setTransactionIdentifier(tid1a);
      client.processUndo(ma14a);      
            
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
      addToLog("Finished MolecularDeleteAtomActionTest at " +
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
