/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularMoveActionTest
 * 
 * 08/22/2006 RBE (1-BYX49): Additional test case
 * 08/07/2006 RBE (1-BV19T): Changes to fix foreign relationship test
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularChangeConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMoveAction;
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

public class MolecularMoveActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularMoveActionTest}.
   */	
  public MolecularMoveActionTest() {
    setName("MolecularMoveActionTest");
    setDescription("Test suite for molecular move actions");
  }
  
  /**
   * Perform molecular move action test.
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
      Concept extra = client.getConcept(103);

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
      addToLog("    2a. Insert an atom2 ... " + date_format.format(timestamp));

      // Create an atom2
      Atom atom2 = new Atom.Default();
      atom2.setString("TEST ATOM 2");
      atom2.setTermgroup(client.getTermgroup("MTH/PT"));
      atom2.setSource(client.getSource("MTH"));
      atom2.setCode(Code.newCode("NOCODE"));
      atom2.setLanguage(new Language.Default("English","ENG"));
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
      
      addToLog("        Atom must be inserted ...");
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
      addToLog("    3a. Insert an atom3 (foreign atom) ... " + date_format.format(timestamp));

      // Create an atom3
      Atom atom3 = new Atom.Default();
      atom3.setString("TEST ATOM 3");
      atom3.setTermgroup(client.getTermgroup("MTH/PT"));
      atom3.setSource(client.getSource("MTH"));
      atom3.setCode(Code.newCode("NOCODE"));
      atom3.setLanguage(new Language.Default("Spanish","SPA"));      
      atom3.setStatus('R');
      atom3.setGenerated(true);
      atom3.setReleased('N');
      atom3.setTobereleased('Y');
      atom3.setSuppressible("N");
      atom3.setConcept(source);
      
      source.addAtom(atom3);

      MolecularAction ma3a = new MolecularInsertAtomAction(atom3);
      client.setChangeStatus(true);
      client.processAction(ma3a);

      // re-read concept      
      source = client.getConcept(source);
      atom3 = client.getAtom(atom3);     
      
      addToLog("        Foreign atom must be inserted ...");
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
      // 4a. Insert an attribute
      //
      addToLog("    4a. Insert an attribute ... " + date_format.format(timestamp));
      
      Attribute attr = new Attribute.Default();
      attr.setAtom(client.getAtom(101));
      attr.setLevel('C');
      attr.setName("SEMANTIC_TYPE");
      attr.setValue("Virus");
      attr.setSource(client.getSource("MTH"));
      attr.setStatus('R');
      attr.setGenerated(false);
      attr.setReleased('A');
      attr.setTobereleased('Y');
      attr.setSuppressible("N");
      attr.setConcept(source);

      source.addAttribute(attr);

      MolecularAction ma4a = new MolecularInsertAttributeAction(attr);
      client.processAction(ma4a);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      attr = client.getAttribute(attr);

      addToLog("        Attribute must be inserted ...");
      if (attr != null)
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
      
      //
      // 6a Insert a P level relationship 
      //            
      addToLog("    6a. Insert a P level relationship ... " + date_format.format(timestamp));

      Relationship rel1 = new Relationship.Default();
      rel1.setConcept(source);
      rel1.setRelatedConcept(extra);
      rel1.setAtom(atom1);
      rel1.setRelatedAtom(extra.getAtoms()[0]);
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
      
      MolecularAction ma6a = new MolecularInsertRelationshipAction(rel1);
      client.processAction(ma6a);

      // re-read concept
      source = client.getConcept(source);
      rel1 = client.getRelationship(rel1);

      int tid6a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6a);

      addToLog("        Source relationship must be inserted ...");
      if (rel1 != null)
        addToLog("    6a. Test Passed");
      else {
        addToLog("    6a. Test Failed");
        thisTestFailed();
      }      

      //
      // 7a Insert a S level relationship 
      //            
      addToLog("    7a. Insert an S level relationship ... " + date_format.format(timestamp));

      Relationship rel2 = new Relationship.Default();
      rel2.setAtom(atom1);
      rel2.setRelatedAtom(atom3);
      rel2.setConcept(source);
      rel2.setRelatedConcept(target);
      rel2.setName("RT?");
      rel2.setAttribute("translation_of");
      rel2.setSource(client.getSource("MTH"));
      rel2.setSourceOfLabel(client.getSource("MTH"));
      rel2.setStatus('N');
      rel2.setGenerated(false);
      rel2.setLevel('S');
      rel2.setReleased('A');
      rel2.setTobereleased('Y');
      rel2.setSuppressible("N");
      rel2.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel2.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      source.addRelationship(rel2);
      
      MolecularAction ma7a = new MolecularInsertRelationshipAction(rel2);
      client.processAction(ma7a);

      // re-read concept
      source = client.getConcept(source);
      rel2 = client.getRelationship(rel2);
      
      addToLog("        Source relationship must be inserted ...");
      if (rel2 != null)
        addToLog("    7a. Test Passed");
      else {
        addToLog("    7a. Test Failed");
        thisTestFailed();
      }      

      int tid7a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7a);
     
      //
      // 8a Insert a C level relationship 
      //            
      addToLog("    8a. Insert a C level relationship ... " + date_format.format(timestamp));

      Relationship rel3 = new Relationship.Default();
      rel3.setConcept(source);
      rel3.setRelatedConcept(extra);
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
      
      MolecularAction ma8a = new MolecularInsertRelationshipAction(rel3);
      client.processAction(ma8a);

      // re-read concept
      source = client.getConcept(source);
      rel3 = client.getRelationship(rel3);

      int tid8a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid8a);

      addToLog("        Concept level relationship must be inserted ...");
      if (rel3 != null)
        addToLog("    8a. Test Passed");
      else {
        addToLog("    8a. Test Failed");
        thisTestFailed();
      }      

      //
      // 9a. Change the status of the source
      //
      addToLog("    9a. Change the status of the source ... " + date_format.format(timestamp));

      source.setStatus('R');
      MolecularAction ma9a = new MolecularChangeConceptAction(source);
      client.processAction(ma9a);

      // re-read concept      
      source = client.getConcept(source);

      addToLog("        Source concept status value must be 'R' ...");
      if (source.getStatus() == 'R')
        addToLog("    9a. Test Passed");
      else {
        addToLog("    9a. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid9a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid9a);
    
      //
      // 10a. Change the status of the target
      //
      addToLog("    10a. Change the status of the target ... " + date_format.format(timestamp));

      target.setStatus('R');
      MolecularAction ma10a = new MolecularChangeConceptAction(target);
      client.processAction(ma10a);

      // re-read concept      
      target = client.getConcept(target);

      addToLog("        Target concept status value must be 'R' ...");
      if (target.getStatus() == 'R')
        addToLog("    10a. Test Passed");
      else {
        addToLog("    10a. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid10a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid10a);
      
      //
      // 11a. Move source to target concept
      //
      addToLog("    11a. Move (1st Move) source to target concept ... " + date_format.format(timestamp));

      MolecularMoveAction ma11a = new MolecularMoveAction(source, target);
      ma11a.addAtomToMove(atom1);
      ma11a.addAttributeToMove(attr);
      ma11a.addRelationshipToMove(rel3);
      client.processAction(ma11a);
      
      // re-read concept
      source = client.getConcept(source);
      target = client.getConcept(target);
      
      Atom[] target_atoms = target.getAtoms();
      Atom moved_atom1 = null;

      addToLog("        11a1. Source atom must be move to target ...");
      for (int i=0; i<target_atoms.length; i++) {
      	if (target_atoms[i].equals(atom1)) {
      		moved_atom1 = target_atoms[i];
      	}
      }
      if (moved_atom1 != null)
        addToLog("    11a1. Test Passed");
      else {
        addToLog("    11a1. Test Failed");
        thisTestFailed();
      }      
    
      addToLog("        11a2. Atom's status must changed to N ...");
      if (moved_atom1 != null && moved_atom1.getStatus() == 'N')
        addToLog("    11a2. Test Passed");
      else {
        addToLog("    11a2. Test Failed");
        thisTestFailed();
      }      

      Atom moved_atom3 = null;
      addToLog("        11a3. Foreign atom must be move to target ...");
      for (int i=0; i<target_atoms.length; i++) {
      	if (target_atoms[i].equals(atom3)) {
      		moved_atom3 = target_atoms[i];
      	}
      }
      if (moved_atom3 != null)
        addToLog("    11a3. Test Passed");
      else {
        addToLog("    11a3. Test Failed");
        thisTestFailed();
      }      
      
      addToLog("        11a4. Source relationship must be deleted ...");
      Relationship[] source_rels = client.getRelationships(source);
      boolean found = false;
      for (int i=0; i<source_rels.length; i++) {
    	  if (source_rels[i].equals(rel1) || source_rels[i].equals(rel2) || 
    		  source_rels[i].equals(rel3)) {
    		  found = true;
    		  break;
    	  } 		  
      }
      if (!found)
        addToLog("    11a4. Test Passed");
      else {
        addToLog("    11a4. Test Failed");
        thisTestFailed();
      }

      Attribute[] target_attrs = target.getAttributes();
      Attribute moved_attr = null;

      addToLog("        11a5. Source attribute must be move to target ...");
      for (int i=0; i<target_attrs.length; i++) {
      	if (target_attrs[i].equals(attr)) {
      		moved_attr = target_attrs[i];
      	}
      }
      if (moved_attr != null)
        addToLog("    11a5. Test Passed");
      else {
        addToLog("    11a5. Test Failed");
        thisTestFailed();
      }      
    
      addToLog("        11a6. Attribute's status must changed to N ...");
      if (moved_attr != null && moved_attr.getStatus() == 'N')
        addToLog("    11a6. Test Passed");
      else {
        addToLog("    11a6. Test Failed");
        thisTestFailed();
      }      

      Relationship[] target_rels = client.getRelationships(target);
      Relationship moved_rel = null;

      addToLog("        11a7. Source relationship must be move to target ...");
      for (int i=0; i<target_rels.length; i++) {
      	if (target_rels[i].equals(rel3)) {
      		moved_rel = target_rels[i];
      	}
      }
      if (moved_rel != null)
        addToLog("    11a7. Test Passed");
      else {
        addToLog("    11a7. Test Failed");
        thisTestFailed();
      }      
    
      addToLog("        11a8. Relationship's status must changed to N ...");
      if (moved_rel != null && moved_rel.getStatus() == 'N')
        addToLog("    11a8. Test Passed");
      else {
        addToLog("    11a8. Test Failed");
        thisTestFailed();
      }      

      addToLog("        11a9. Target status must changed to N ...");
      if (target.getStatus() == 'N')
        addToLog("    11a9. Test Passed");
      else {
        addToLog("    11a9. Test Failed");
        thisTestFailed();
      }      

      addToLog("        11a10. Source status must changed to N ...");
      if (source.getStatus() == 'N')
        addToLog("    11a10. Test Passed");
      else {
        addToLog("    11a10. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid11a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid11a);
             	
      //
      // 11b. Undo 11a
      //
      addToLog("    11b. Undo 11a ... "
               + date_format.format(timestamp));

      MolecularAction ma11b = ma11a;
      ma11b.setTransactionIdentifier(tid11a);
      client.processUndo(ma11b);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      
      //
      // 12a. Undo 9a
      //
      addToLog("    12a. Undo 9a ... "
               + date_format.format(timestamp));

      MolecularAction ma12a = ma9a;
      ma12a.setTransactionIdentifier(tid9a);
      client.processUndo(ma12a);

      //
      // 13a. Undo 10a
      //
      addToLog("    13a. Undo 10a ... "
               + date_format.format(timestamp));

      MolecularAction ma13a = ma10a;
      ma13a.setTransactionIdentifier(tid10a);
      client.processUndo(ma13a);

      //
      // 14a. Undo 8a
      //
      addToLog("    14a. Undo 8a ... "
               + date_format.format(timestamp));

      MolecularAction ma14a = ma8a;
      ma14a.setTransactionIdentifier(tid8a);
      client.processUndo(ma14a);

      //
      // 15a. Undo 7a
      //
      addToLog("    15a. Undo 7a ... "
               + date_format.format(timestamp));

      MolecularAction ma15a = ma7a;
      ma15a.setTransactionIdentifier(tid7a);
      client.processUndo(ma15a);

      //
      // 16a. Undo 6a
      //
      addToLog("    16a. Undo 6a ... "
               + date_format.format(timestamp));

      MolecularAction ma16a = ma6a;
      ma16a.setTransactionIdentifier(tid6a);
      client.processUndo(ma16a);

      // re-read concept
      source = client.getConcept(source);      
      target = client.getConcept(target);

      
      //
      // 17a. Undo 4a
      //
      addToLog("    17a. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma17a = ma4a;
      ma17a.setTransactionIdentifier(tid4a);
      client.processUndo(ma17a);

      //
      // 17b. Undo 3a
      //
      addToLog("    17b. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma17b = ma3a;
      ma17b.setTransactionIdentifier(tid3a);
      client.processUndo(ma17b);

      //
      // 17c. Undo 2a
      //
      addToLog("    17c. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma17c = ma2a;
      ma17c.setTransactionIdentifier(tid2a);
      client.processUndo(ma17c);
      
      //
      // 17d. Undo 1a
      //
      addToLog("    17d. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma17d = ma1a;
      ma17d.setTransactionIdentifier(tid1a);
      client.processUndo(ma17d);   

      // re-read concept
      source = client.getConcept(source);      
      target = client.getConcept(target);

      //
      // Additional move case
      //
      
      //
      // 21a. Insert an atom1
      //
      addToLog("    21a. Insert an atom1 ... " + date_format.format(timestamp));

      // Create an atom1
      atom1 = new Atom.Default();
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

      MolecularAction ma21a = new MolecularInsertAtomAction(atom1);
      client.setChangeStatus(true);
      client.processAction(ma21a);

      // re-read concept
      source = client.getConcept(source);
      atom1 = client.getAtom(atom1);     
      
      addToLog("        Atom must be inserted ...");
      if (atom1 != null)
        addToLog("    21a. Test Passed");
      else {
        addToLog("    21a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid21a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid21a);
      
      //
      // 22a Insert an atom2 
      //      
      addToLog("    22a. Insert an atom2 (foreign atom) ... " + date_format.format(timestamp));

      // Create an atom2
      atom2 = new Atom.Default();
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
      atom2.setConcept(source);
      
      source.addAtom(atom2);

      MolecularAction ma22a = new MolecularInsertAtomAction(atom2);
      client.setChangeStatus(true);
      client.processAction(ma22a);

      // re-read concept      
      source = client.getConcept(source);
      atom2 = client.getAtom(atom2);     
      
      addToLog("        foreign atom must be inserted ...");
      if (atom2 != null)
        addToLog("    22a. Test Passed");
      else {
        addToLog("    22a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid22a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid22a);

      //
      // 23a Insert an atom3 
      //      
      addToLog("    23a. Insert an atom3 ... " + date_format.format(timestamp));

      // Create an atom3
      atom3 = new Atom.Default();
      atom3.setString("TEST ATOM 3");
      atom3.setTermgroup(client.getTermgroup("MTH/PT"));
      atom3.setSource(client.getSource("MTH"));
      atom3.setCode(Code.newCode("NOCODE"));
      atom3.setLanguage(new Language.Default("English","ENG"));      
      atom3.setStatus('R');
      atom3.setGenerated(true);
      atom3.setReleased('N');
      atom3.setTobereleased('Y');
      atom3.setSuppressible("N");
      atom3.setConcept(target);
      
      source.addAtom(atom3);

      MolecularAction ma23a = new MolecularInsertAtomAction(atom3);
      client.setChangeStatus(true);
      client.processAction(ma23a);

      // re-read concept      
      source = client.getConcept(source);
      atom3 = client.getAtom(atom3);     
      
      addToLog("        Atom must be inserted ...");
      if (atom3 != null)
        addToLog("    23a. Test Passed");
      else {
        addToLog("    23a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid23a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid23a);

      //
      // 24a. Insert an attribute
      //
      addToLog("    24a. Insert an attribute ... " + date_format.format(timestamp));
      
      attr = new Attribute.Default();
      attr.setAtom(client.getAtom(101));
      attr.setLevel('C');
      attr.setName("SEMANTIC_TYPE");
      attr.setValue("Virus");
      attr.setSource(client.getSource("MTH"));
      attr.setStatus('R');
      attr.setGenerated(false);
      attr.setReleased('A');
      attr.setTobereleased('Y');
      attr.setSuppressible("N");
      attr.setConcept(source);

      source.addAttribute(attr);

      MolecularAction ma24a = new MolecularInsertAttributeAction(attr);
      client.processAction(ma24a);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      attr = client.getAttribute(attr);

      addToLog("        Attribute must be inserted ...");
      if (attr != null)
        addToLog("    24a. Test Passed");
      else {
        addToLog("    24a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid24a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid24a);
      
      //
      // 26a Insert a P level relationship 
      //            
      addToLog("    26a. Insert a P level relationship ... " + date_format.format(timestamp));

      rel1 = new Relationship.Default();
      rel1.setConcept(source);
      rel1.setRelatedConcept(extra);
      rel1.setAtom(atom1);
      rel1.setRelatedAtom(extra.getAtoms()[0]);
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
      
      MolecularAction ma26a = new MolecularInsertRelationshipAction(rel1);
      client.processAction(ma26a);

      // re-read concept
      source = client.getConcept(source);
      rel1 = client.getRelationship(rel1);

      int tid26a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid26a);

      addToLog("        Source relationship must be inserted ...");
      if (rel1 != null)
        addToLog("    26a. Test Passed");
      else {
        addToLog("    26a. Test Failed");
        thisTestFailed();
      }      

      //
      // 27a Insert a S level relationship 
      //            
      addToLog("    27a. Insert an S level relationship ... " + date_format.format(timestamp));

      rel2 = new Relationship.Default();
      rel2.setAtom(atom1);
      rel2.setRelatedAtom(atom2);
      rel2.setConcept(source);
      rel2.setRelatedConcept(target);
      rel2.setName("RT?");
      rel2.setAttribute("translation_of");
      rel2.setSource(client.getSource("MTH"));
      rel2.setSourceOfLabel(client.getSource("MTH"));
      rel2.setStatus('N');
      rel2.setGenerated(false);
      rel2.setLevel('S');
      rel2.setReleased('A');
      rel2.setTobereleased('Y');
      rel2.setSuppressible("N");
      rel2.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel2.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      source.addRelationship(rel2);
      
      MolecularAction ma27a = new MolecularInsertRelationshipAction(rel2);
      client.processAction(ma27a);

      // re-read concept
      source = client.getConcept(source);
      rel2 = client.getRelationship(rel2);
      
      addToLog("        Source relationship must be inserted ...");
      if (rel2 != null)
        addToLog("    27a. Test Passed");
      else {
        addToLog("    27a. Test Failed");
        thisTestFailed();
      }      

      int tid27a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid27a);
     
      //
      // 28a Insert a C level relationship 
      //            
      addToLog("    28a. Insert a C level relationship ... " + date_format.format(timestamp));

      rel3 = new Relationship.Default();
      rel3.setConcept(source);
      rel3.setRelatedConcept(extra);
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
      
      MolecularAction ma28a = new MolecularInsertRelationshipAction(rel3);
      client.processAction(ma28a);

      // re-read concept
      source = client.getConcept(source);
      rel3 = client.getRelationship(rel3);

      int tid28a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid28a);

      addToLog("        Concept level relationship must be inserted ...");
      if (rel3 != null)
        addToLog("    28a. Test Passed");
      else {
        addToLog("    28a. Test Failed");
        thisTestFailed();
      }      

      //
      // 29a. Change the status of the source
      //
      addToLog("    29a. Change the status of the source ... " + date_format.format(timestamp));

      source.setStatus('R');
      MolecularAction ma29a = new MolecularChangeConceptAction(source);
      client.processAction(ma29a);

      // re-read concept      
      source = client.getConcept(source);

      addToLog("        Source concept status value must be 'R' ...");
      if (source.getStatus() == 'R')
        addToLog("    29a. Test Passed");
      else {
        addToLog("    29a. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid29a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid29a);
    
      //
      // 30a. Change the status of the target
      //
      addToLog("    30a. Change the status of the target ... " + date_format.format(timestamp));

      target.setStatus('R');
      MolecularAction ma30a = new MolecularChangeConceptAction(target);
      client.processAction(ma30a);

      // re-read concept      
      target = client.getConcept(target);

      addToLog("        Target concept status value must be 'R' ...");
      if (target.getStatus() == 'R')
        addToLog("    30a. Test Passed");
      else {
        addToLog("    30a. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid30a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid30a);
      
      //
      // 31a. Move source to target concept
      //
      addToLog("    31a. Move (1st Move) source to target concept ... " + date_format.format(timestamp));

      MolecularMoveAction ma31a = new MolecularMoveAction(source, target);
      ma31a.addAtomToMove(atom1);
      ma31a.addAttributeToMove(attr);
      ma31a.addRelationshipToMove(rel3);
      client.processAction(ma31a);
      
      // re-read concept
      source = client.getConcept(source);
      target = client.getConcept(target);
      
      target_atoms = target.getAtoms();
      moved_atom1 = null;

      addToLog("        31a1. Source atom must be move to target ...");
      for (int i=0; i<target_atoms.length; i++) {
      	if (target_atoms[i].equals(atom1)) {
      		moved_atom1 = target_atoms[i];
      	}
      }
      if (moved_atom1 != null)
        addToLog("    31a1. Test Passed");
      else {
        addToLog("    31a1. Test Failed");
        thisTestFailed();
      }      
    
      addToLog("        31a2. Atom's status must changed to N ...");
      if (moved_atom1 != null && moved_atom1.getStatus() == 'N')
        addToLog("    31a2. Test Passed");
      else {
        addToLog("    31a2. Test Failed");
        thisTestFailed();
      }      

      Atom moved_atom2 = null;
      addToLog("        31a3. Foreign atom must be move to target ...");
      for (int i=0; i<target_atoms.length; i++) {
      	if (target_atoms[i].equals(atom2)) {
      		moved_atom2 = target_atoms[i];
      	}
      }
      if (moved_atom2 != null)
        addToLog("    31a3. Test Passed");
      else {
        addToLog("    31a3. Test Failed");
        thisTestFailed();
      }      
      
      addToLog("        31a4. Source relationship must be deleted ...");
      source_rels = client.getRelationships(source);
      found = false;
      for (int i=0; i<source_rels.length; i++) {
    	  if (source_rels[i].equals(rel1) || source_rels[i].equals(rel2) || 
    		  source_rels[i].equals(rel3)) {
    		  found = true;
    		  break;
    	  } 		  
      }
      if (!found)
        addToLog("    31a4. Test Passed");
      else {
        addToLog("    31a4. Test Failed");
        thisTestFailed();
      }

      target_attrs = target.getAttributes();
      moved_attr = null;

      addToLog("        31a5. Source attribute must be move to target ...");
      for (int i=0; i<target_attrs.length; i++) {
      	if (target_attrs[i].equals(attr)) {
      		moved_attr = target_attrs[i];
      	}
      }
      if (moved_attr != null)
        addToLog("    31a5. Test Passed");
      else {
        addToLog("    31a5. Test Failed");
        thisTestFailed();
      }      
    
      addToLog("        31a6. Attribute's status must changed to N ...");
      if (moved_attr != null && moved_attr.getStatus() == 'N')
        addToLog("    31a6. Test Passed");
      else {
        addToLog("    31a6. Test Failed");
        thisTestFailed();
      }      

      target_rels = client.getRelationships(target);
      moved_rel = null;

      addToLog("        31a7. Source relationship must be move to target ...");
      for (int i=0; i<target_rels.length; i++) {
      	if (target_rels[i].equals(rel3)) {
      		moved_rel = target_rels[i];
      	}
      }
      if (moved_rel != null)
        addToLog("    31a7. Test Passed");
      else {
        addToLog("    31a7. Test Failed");
        thisTestFailed();
      }      
    
      addToLog("        31a8. Relationship's status must changed to N ...");
      if (moved_rel != null && moved_rel.getStatus() == 'N')
        addToLog("    31a8. Test Passed");
      else {
        addToLog("    31a8. Test Failed");
        thisTestFailed();
      }      

      addToLog("        31a9. Target status must changed to N ...");
      if (target.getStatus() == 'N')
        addToLog("    31a9. Test Passed");
      else {
        addToLog("    31a9. Test Failed");
        thisTestFailed();
      }      

      addToLog("        31a10. Source status must changed to N ...");
      if (source.getStatus() == 'N')
        addToLog("    31a10. Test Passed");
      else {
        addToLog("    31a10. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid31a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid31a);
             	
      //
      // 31b. Undo 31a
      //
      addToLog("    31b. Undo 31a ... "
               + date_format.format(timestamp));

      MolecularAction ma31b = ma31a;
      ma31b.setTransactionIdentifier(tid31a);
      client.processUndo(ma31b);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      
      //
      // 32a. Undo 29a
      //
      addToLog("    32a. Undo 29a ... "
               + date_format.format(timestamp));

      MolecularAction ma32a = ma29a;
      ma32a.setTransactionIdentifier(tid29a);
      client.processUndo(ma32a);

      //
      // 33a. Undo 30a
      //
      addToLog("    33a. Undo 30a ... "
               + date_format.format(timestamp));

      MolecularAction ma33a = ma30a;
      ma33a.setTransactionIdentifier(tid30a);
      client.processUndo(ma33a);

      //
      // 34a. Undo 28a
      //
      addToLog("    34a. Undo 28a ... "
               + date_format.format(timestamp));

      MolecularAction ma34a = ma28a;
      ma34a.setTransactionIdentifier(tid28a);
      client.processUndo(ma34a);

      //
      // 35a. Undo 27a
      //
      addToLog("    35a. Undo 27a ... "
               + date_format.format(timestamp));

      MolecularAction ma35a = ma27a;
      ma35a.setTransactionIdentifier(tid27a);
      client.processUndo(ma35a);

      //
      // 36a. Undo 26a
      //
      addToLog("    36a. Undo 26a ... "
               + date_format.format(timestamp));

      MolecularAction ma36a = ma26a;
      ma36a.setTransactionIdentifier(tid26a);
      client.processUndo(ma36a);

      // re-read concept
      source = client.getConcept(source);      
      target = client.getConcept(target);

      
      //
      // 37a. Undo 24a
      //
      addToLog("    37a. Undo 24a ... "
               + date_format.format(timestamp));

      MolecularAction ma37a = ma24a;
      ma37a.setTransactionIdentifier(tid24a);
      client.processUndo(ma37a);

      // re-read concept
      source = client.getConcept(source);      
      target = client.getConcept(target);
      
      //
      // 38a Insert a P level relationship 
      //            
      addToLog("    38a. Insert a P level relationship ... " + date_format.format(timestamp));

      rel1 = new Relationship.Default();
      rel1.setAtom(atom1);
      rel1.setRelatedAtom(atom3);
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
      
      MolecularAction ma38a = new MolecularInsertRelationshipAction(rel1);
      client.processAction(ma38a);

      // re-read concept
      source = client.getConcept(source);
      rel1 = client.getRelationship(rel1);

      int tid38a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid38a);

      addToLog("        Source relationship must be inserted ...");
      if (rel1 != null)
        addToLog("    38a. Test Passed");
      else {
        addToLog("    38a. Test Failed");
        thisTestFailed();
      }      

      //
      // 39a Insert a P level relationship 
      //            
      addToLog("    39a. Insert a P level relationship ... " + date_format.format(timestamp));

      rel2 = new Relationship.Default();
      rel2.setAtom(atom1);
      rel2.setRelatedAtom(atom2);
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
      
      MolecularAction ma39a = new MolecularInsertRelationshipAction(rel2);
      client.processAction(ma39a);

      // re-read concept
      source = client.getConcept(source);
      rel2 = client.getRelationship(rel2);
      
      addToLog("        Source relationship must be inserted ...");
      if (rel2 != null)
        addToLog("    39a. Test Passed");
      else {
        addToLog("    39a. Test Failed");
        thisTestFailed();
      }      

      int tid39a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid39a);

      //
      // 40a Change a relationship, set level to C 
      //      
      addToLog("    40a. Change a relationship, set level to C ... " + date_format.format(timestamp));
      
      MolecularAction ma40a = new MolecularChangeRelationshipAction(rel1);
      rel1.setLevel('C');
      client.processAction(ma40a);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      rel1 = client.getRelationship(rel1);

      // Save Transaction ID
      int tid40a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid40a);
    
      //
      // 41a. Move source to target concept
      //
      addToLog("    41a. Move (2nd Move) source to target concept ... " + date_format.format(timestamp));

      MolecularMoveAction ma41a = new MolecularMoveAction(source, target);
      ma41a.addAtomToMove(atom1);
      client.processAction(ma41a);
      
      // re-read concept
      source = client.getConcept(source);
      target = client.getConcept(target);

      addToLog("        41a. Source relationship must be deleted ...");
      source_rels = client.getRelationships(source);
      Relationship source_rel = null;
      for (int i=0; i< source_rels.length; i++) {
      	if (source_rels[i].getIdentifier().equals(rel2.getIdentifier()))
      		source_rel = source_rels[i];
      }
      if (source_rel == null)
        addToLog("    41a. Test Passed");
      else {
        addToLog("    41a. Test Failed");
        thisTestFailed();
      }

      
      // Save Transaction ID
      int tid41a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid41a);

      //
      // 41b. Undo 41a
      //
      addToLog("    41b. Undo 41a ... "
               + date_format.format(timestamp));

      MolecularAction ma41b = ma41a;
      ma41b.setTransactionIdentifier(tid41a);
      client.processUndo(ma41b);

      //
      // 42a Change a relationship, set status to D 
      //      
      addToLog("    42a. Change a relationship, set status to D ... " + date_format.format(timestamp));
      
      MolecularAction ma42a = new MolecularChangeRelationshipAction(rel1);
      rel1.setStatus('D');
      client.processAction(ma42a);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      rel1 = client.getRelationship(rel1);
      rel2 = client.getRelationship(rel2);

      // Save Transaction ID
      int tid42a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid42a);
      
      //
      // 43a Change relationship 2, set status to D 
      //      
      addToLog("    43a. Change relationship 2, set status to D ... " + date_format.format(timestamp));
      
      MolecularAction ma43a = new MolecularChangeRelationshipAction(rel2);
      rel2.setStatus('D');
      client.processAction(ma43a);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      rel1 = client.getRelationship(rel1);
      rel2 = client.getRelationship(rel2);

      // Save Transaction ID
      int tid43a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid43a);
      
      //
      // 44a. Move source to target concept
      //
      addToLog("    44a. Move (3rd Move) source to target concept ... " + date_format.format(timestamp));

      MolecularMoveAction ma44a = new MolecularMoveAction(source, target);
      ma44a.addAtomToMove(atom1);
      client.processAction(ma44a);
      
      // re-read concept
      source = client.getConcept(source);
      target = client.getConcept(target);

      addToLog("        44a. Target relationship status must be N ...");
      target_rels = client.getRelationships(source);
      
      Relationship target_rel = null;
      for (int i=0; i<target_rels.length; i++) {
      	if (target_rels[i].getIdentifier().equals(rel1.getIdentifier()))
      		target_rel = target_rels[i];
      }
      if (target_rel != null && target_rel.getStatus() == 'N')
        addToLog("    44a. Test Passed");
      else {
        addToLog("    44a. Test Failed");
        thisTestFailed();
      }

      
      // Save Transaction ID
      int tid44a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid44a);

      //
      // 44b. Undo 44a
      //
      addToLog("    44b. Undo 44a ... "
               + date_format.format(timestamp));

      MolecularAction ma44b = ma44a;
      ma44b.setTransactionIdentifier(tid44a);
      client.processUndo(ma44b);
      
      //
      // 45a. Undo 43a
      //
      addToLog("    45a. Undo 43a ... "
               + date_format.format(timestamp));

      MolecularAction ma45a = ma43a;
      ma45a.setTransactionIdentifier(tid43a);
      client.processUndo(ma45a);
      
      //
      // 46a. Undo 42a
      //
      addToLog("    46a. Undo 42a ... "
               + date_format.format(timestamp));

      MolecularAction ma46a = ma42a;
      ma46a.setTransactionIdentifier(tid42a);
      client.processUndo(ma46a);

      //
      // 47a. Undo 40a
      //
      addToLog("    47a. Undo 40a ... "
               + date_format.format(timestamp));

      MolecularAction ma47a = ma40a;
      ma47a.setTransactionIdentifier(tid40a);
      client.processUndo(ma47a);
      
      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      rel1 = client.getRelationship(rel1);
      rel2 = client.getRelationship(rel2);


      //
      // 48a. Undo 39a
      //
      addToLog("    48a. Undo 39a ... "
               + date_format.format(timestamp));

      MolecularAction ma48a = ma39a;
      ma48a.setTransactionIdentifier(tid39a);
      client.processUndo(ma48a);

      //
      // 49a. Undo 38a
      //
      addToLog("    49a. Undo 38a ... "
               + date_format.format(timestamp));

      MolecularAction ma49a = ma38a;
      ma49a.setTransactionIdentifier(tid38a);
      client.processUndo(ma49a);
      
      //
      // 50a. Undo 23a
      //
      addToLog("    50a. Undo 23a ... "
               + date_format.format(timestamp));

      MolecularAction ma50a = ma23a;
      ma50a.setTransactionIdentifier(tid23a);
      client.processUndo(ma50a);

      //
      // 51a. Undo 22a
      //
      addToLog("    51a. Undo 22a ... "
               + date_format.format(timestamp));

      MolecularAction ma51a = ma22a;
      ma51a.setTransactionIdentifier(tid22a);
      client.processUndo(ma51a);
      
      //
      // 52a. Undo 21a
      //
      addToLog("    52a. Undo 21a ... "
               + date_format.format(timestamp));

      MolecularAction ma52a = ma21a;
      ma52a.setTransactionIdentifier(tid21a);
      client.processUndo(ma52a);   

      
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
      addToLog("Finished MolecularMoveActionTest at " +
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
