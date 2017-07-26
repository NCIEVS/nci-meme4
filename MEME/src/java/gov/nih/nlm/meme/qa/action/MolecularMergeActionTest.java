/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.action
 * Object:  MolecularMergeActionTest
 * 
 * 12/05/2005 RBE (1-72UTX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.action;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMergeAction;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MolecularMergeActionTest
    extends TestSuite{

  /**
   * Instantiates an empty {@link MolecularMergeActionTest}.
   */	
  public MolecularMergeActionTest() {
    setName("MolecularMergeActionTest");
    setDescription("Test suite for molecular merge actions");
  }
  
  /**
   * Perform molecular merge action test.
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

      // Initialize source and target
      Concept source = client.getConcept(101);
      Concept target = client.getConcept(102);
      
      Atom[] target_atoms = null;
      Relationship[] target_rels = null;
      
      Relationship rel = null;

      //
      // 1a. Approve a concept
      //
      addToLog("    1a. Approve a concept ... " + date_format.format(timestamp));

      MolecularAction ma1a = new MolecularApproveConceptAction(target);
      client.processAction(ma1a);
      
      // re-read concept
      target = client.getConcept(target);    
    
      // Save Transaction ID
      int tid1a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a);

      //
      // 2a. Insert a concept level relationship
      //
      addToLog("    2a. Insert a concept level relationship ... " + date_format.format(timestamp));

      rel = new Relationship.Default();
      rel.setConcept(source);
      rel.setRelatedConcept(client.getConcept(103));
      rel.setName("RT?");
      rel.setAttribute("translation_of");
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
      
      source.addRelationship(rel);    
      target.addRelationship(rel);    

      MolecularAction ma2a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma2a);
      
      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);      
      
      //
      // 3a. Insert an attribute
      //
      addToLog("    3a. Insert an attribute ... " + date_format.format(timestamp));
      
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

      MolecularAction ma3a = new MolecularInsertAttributeAction(attr);
      client.processAction(ma3a);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      //
      // 4a. Insert an attribute
      //
      addToLog("    4a. Insert an attribute ... " + date_format.format(timestamp));

      target.addAttribute(attr);
      MolecularAction ma4a = new MolecularInsertAttributeAction(attr);
      client.processAction(ma4a);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      
      Atom[] source_atoms = source.getAtoms();
      Atom source_atom = null;
      for (int i=0; i<source_atoms.length; i++) {
        source_atom = client.getAtom(source_atoms[i]);
        break;
      }

      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);

      //
      // 5a. Merge (1st Merge) source to target concept
      //
      addToLog("    5a. merge (1st Merge) source to target concept, set status to true ... " + date_format.format(timestamp));

      MolecularMergeAction ma5a = new MolecularMergeAction(source, target);
      ma5a.setDeleteDuplicateSemanticTypes(true);
      client.setChangeStatus(true);
      client.processAction(ma5a);
      
      // re-read concept
      target = client.getConcept(target);
      
      target_atoms = target.getAtoms();
      
      boolean found = false;
      for (int i=0; i<target_atoms.length; i++) {
        if (target_atoms[i].equals(source_atom)) {
        	source_atom = target_atoms[i];
        	found = true;
        }
      }

      addToLog("        5a1. Source atom must be move to target ...");
      if (found)
        addToLog("    5a1. Test Passed");
      else {
        addToLog("    5a1. Test Failed");
        thisTestFailed();
      }      

      addToLog("        5a2. Merged atom's status must be equal to 'N' ...");
      if (source_atom.getStatus() == 'N')
        addToLog("    5a2. Test Passed");
      else {
        addToLog("    5a2. Test Failed");
        thisTestFailed();
      }      

      target_rels = client.getRelationships(target);
      addToLog("        5a3. Source relationship must be move to target concept ...");
      if (target_rels[0].equals(rel))
        addToLog("    5a3. Test Passed");
      else {
	      addToLog("    5a3. Test Failed");
	      thisTestFailed();
	    }

      addToLog("        5a4. Relationship status must be equal to 'N' ...");
      if (target_rels[0].getStatus() == 'N')
        addToLog("    5a4. Test Passed");
      else {
        addToLog("    5a4. Test Failed");
        thisTestFailed();
      }      
      
      Attribute[] target_attrs = target.getAttributes();   
      addToLog("        5a6. Source attribute must be move to target...");
      if (target_attrs.length > 0)
        addToLog("    5a6. Test Passed");
      else {
        addToLog("    5a6. Test Failed");
        thisTestFailed();
      }      
      
      addToLog("        5a7. Source attribute status must be 'N' ...");
      if (target_attrs[0].getStatus() == 'N')
        addToLog("    5a7. Test Passed");
      else {
        addToLog("    5a7. Test Failed");
        thisTestFailed();
      }      

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
      // 7a. Insert an attribute
      //
      addToLog("    7a. Insert an attribute ... " + date_format.format(timestamp));

      attr.setConcept(target);
      target.addAttribute(attr);

      MolecularAction ma7a = new MolecularInsertAttributeAction(attr);
      client.processAction(ma7a);

      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);

      // Save Transaction ID
      int tid7a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7a);

      //
      // 8a. Merge (2nd Merge) source to target concept
      //
      addToLog("    8a. merge (2nd Merge) source to target concept, set status to true ... " + date_format.format(timestamp));

      MolecularMergeAction ma8a = new MolecularMergeAction(source, target);
      ma8a.setDeleteDuplicateSemanticTypes(true);
      client.setChangeStatus(true);
      client.processAction(ma8a);
      
      // re-read concept
      target = client.getConcept(target);
      
      target_atoms = target.getAtoms();
      addToLog("        8a1. Source atom must be move to target ...");
      if (target_atoms.length > 1)
        addToLog("    8a1. Test Passed");
      else {
        addToLog("    8a1. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid8a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid8a);

      //
      // 8b. Undo 8a
      //
      addToLog("    8b. Undo 8a ... "
               + date_format.format(timestamp));

      MolecularAction ma8b = ma8a;
      ma8b.setTransactionIdentifier(tid8a);
      client.processUndo(ma8a);

      //
      // 9a. Undo 7a
      //
      addToLog("    9a. Undo 7a ... "
               + date_format.format(timestamp));

      MolecularAction ma9a = ma7a;
      ma9a.setTransactionIdentifier(tid7a);
      client.processUndo(ma9a);

      //
      // 10a. Undo 3a
      //
      addToLog("    10a. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma10a = ma3a;
      ma10a.setTransactionIdentifier(tid3a);
      client.processUndo(ma10a);

      //
      // 11a. Undo 2a
      //
      addToLog("    11a. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma11a = ma2a;
      ma11a.setTransactionIdentifier(tid2a);
      client.processUndo(ma11a);

      //
      // 12a. Insert a concept level relationship
      //
      addToLog("    12a. Insert a concept level relationship ... " + date_format.format(timestamp));

      // Initialize source and target
      source = client.getConcept(101);
      target = client.getConcept(102);     

      rel = new Relationship.Default();
      rel.setConcept(source);
      rel.setRelatedConcept(client.getConcept(102));
      rel.setName("RT?");
      rel.setAttribute("translation_of");
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
      
      source.addRelationship(rel);    
      target.addRelationship(rel);    

      MolecularAction ma12a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma12a);
      
      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);

      // Save Transaction ID
      int tid12a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid12a);      
      
      //
      // 13a. Change status of source
      //
      addToLog("    13a. Change status of source to E ... " + date_format.format(timestamp));

      source.setStatus('E');
      MolecularAction ma13a = new MolecularChangeConceptAction(source);
      client.processAction(ma13a);

      // re-read concept
      source = client.getConcept(source);

      source_atoms = source.getAtoms();
      source_atom = null;
      for (int i=0; i<source_atoms.length; i++) {
        source_atom = client.getAtom(source_atoms[i]);
        break;
      }

      addToLog("        13a. Source status must be 'E' ...");
      if (source.getStatus() == 'E')
        addToLog("    13a. Test Passed");
      else {
        addToLog("    13a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid13a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid13a);

      //
      // 14a. Merge source (3rd Merge) to target concept
      //
      addToLog("    14a. Merge (3rd Merge) source to target concept, set status to true ... " + date_format.format(timestamp));

      MolecularMergeAction ma14a = new MolecularMergeAction(source, target);
      ma14a.setDeleteDuplicateSemanticTypes(true);
      client.setChangeStatus(true);
      client.processAction(ma14a);
      
      // re-read concept
      target = client.getConcept(target);
      
      target_atoms = target.getAtoms();

      found = false;
      for (int i=0; i<target_atoms.length; i++) {
        if (target_atoms[i].equals(source_atom)) {
        	source_atom = target_atoms[i];
        	found = true;
        }
      }

      addToLog("        14a1. Source atom must be move to target ...");
      if (found)
        addToLog("    14a1. Test Passed");
      else {
        addToLog("    14a1. Test Failed");
        thisTestFailed();
      }      

      addToLog("        14a2. Merged atom's status must be equal to 'N' ...");
      if (source_atom.getStatus() == 'N')
        addToLog("    14a2. Test Passed");
      else {
        addToLog("    14a2. Test Failed");
        thisTestFailed();
      }      

      target_rels = client.getRelationships(target);
      addToLog("        14a3. Source relationship must be deleted ...");
      if (target_rels.length == 0)
        addToLog("    14a3. Test Passed");
      else {
        addToLog("    14a3. Test Failed");
        thisTestFailed();
      }

      addToLog("        14a4. Target status must be 'E' ...");
      if (target.getStatus() == 'E')
        addToLog("    14a4. Test Passed");
      else {
        addToLog("    14a4. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid14a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid14a);
             	
      //
      // 14b. Undo 14a
      //
      addToLog("    14b. Undo 14a ... "
               + date_format.format(timestamp));

      MolecularAction ma14b = ma14a;
      ma14b.setTransactionIdentifier(tid14a);
      client.processUndo(ma14b);

      //
      // 15a. Undo 13a
      //
      addToLog("    15a. Undo 13a ... "
               + date_format.format(timestamp));

      MolecularAction ma15a = ma13a;
      ma15a.setTransactionIdentifier(tid13a);
      client.processUndo(ma15a);

      //
      // 16a. Undo 12a
      //
      addToLog("    16a. Undo 12a ... "
               + date_format.format(timestamp));

      MolecularAction ma16a = ma12a;
      ma16a.setTransactionIdentifier(tid12a);
      client.processUndo(ma16a);

      //
      // 17a. Insert a concept level relationship
      //
      addToLog("    17a. Insert a concept level relationship ... " + date_format.format(timestamp));

      // Initialize source and target
      source = client.getConcept(101);
      target = client.getConcept(102);
      
      rel = new Relationship.Default();
      rel.setConcept(source);
      rel.setRelatedConcept(client.getConcept(103));
      rel.setName("RT?");
      rel.setAttribute("translation_of");
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
      
      source.addRelationship(rel);    
      target.addRelationship(rel);    

      MolecularAction ma17a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma17a);
      
      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);
      Relationship orig_rel = client.getRelationship(rel);

      // Save Transaction ID
      int tid17a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid17a);      

      //
      // 18a. Insert another concept level relationship
      //
      addToLog("    18a. Insert another concept level relationship ... " + date_format.format(timestamp));

      rel = new Relationship.Default();
      rel.setConcept(client.getConcept(102));
      rel.setRelatedConcept(client.getConcept(103));
      rel.setName("RT?");
      rel.setAttribute("translation_of");
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
      
      source.addRelationship(rel);    
      target.addRelationship(rel);    

      MolecularAction ma18a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma18a);
      
      // re-read concept      
      source = client.getConcept(source);
      target = client.getConcept(target);

      // Save Transaction ID
      int tid18a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid18a);      

      //
      // 19a Change a relationship, set level to 'P' 
      //
      addToLog("    19a. Change a relationship, set level to 'P' ... " + date_format.format(timestamp));
      
      MolecularAction ma19a = new MolecularChangeRelationshipAction(rel);
      rel.setLevel('P');
      rel.setStatus('D');
      client.processAction(ma19a);

      // re-read relationship      
      rel = client.getRelationship(rel);

      addToLog("        19a1. Relationship status must be equal to 'D' ...");
      if (rel.getStatus() == 'D')
        addToLog("    19a3. Test Passed");
      else {
        addToLog("    19a3. Test Failed");
        thisTestFailed();
      }

      addToLog("        19a2. Relationship level must be equal to 'P' ...");
      if (rel.getLevel() == 'P')
        addToLog("    19a2. Test Passed");
      else {
        addToLog("    19a2. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid19a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid19a);
      
      //
      // 20a. Merge (4th Merge) source to target concept
      //
      addToLog("    20a. Merge (4th Merge) source to target concept ... " + date_format.format(timestamp));

      MolecularAction ma20a = new MolecularMergeAction(source, target);
      client.setChangeStatus(false);
      client.processAction(ma20a);
      
      // re-read concept
      target = client.getConcept(target);
      rel = client.getRelationship(rel);

      target_atoms = target.getAtoms();
      addToLog("        20a1. Source atom must be move to target ...");
      if (target_atoms.length > 0)
        addToLog("    20a1. Test Passed");
      else {
        addToLog("    20a1. Test Failed");
        thisTestFailed();
      }      
      
      target_rels = client.getRelationships(target);
      addToLog("        20a2. Source relationship must move to target ...");
      if (target_rels[0].equals(orig_rel))
        addToLog("    20a2. Test Passed");
      else {
        addToLog("    20a2. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid20a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid20a);

      //
      // 20b. Undo 20a
      //
      addToLog("    20b. Undo 20a ... "
               + date_format.format(timestamp));

      MolecularAction ma20b = ma20a;
      ma20b.setTransactionIdentifier(tid20a);
      client.processUndo(ma20b);

      //
      // 21a. Undo 19a
      //
      addToLog("    21a. Undo 19a ... "
               + date_format.format(timestamp));

      MolecularAction ma21a = ma19a;
      ma21a.setTransactionIdentifier(tid19a);
      client.processUndo(ma21a);

      //
      // 22a Change a relationship, set status to 'R' 
      //
      addToLog("    22a. Change a relationship, set status to 'R' ... " + date_format.format(timestamp));
      
      MolecularAction ma22a = new MolecularChangeRelationshipAction(rel);
      rel.setStatus('R');
      client.processAction(ma22a);

      // re-read relationship      
      rel = client.getRelationship(rel);

      addToLog("        22a1. Relationship status must be equal to 'R' ...");
      if (rel.getStatus() == 'R')
        addToLog("    22a1. Test Passed");
      else {
        addToLog("    22a1. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid22a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid22a);      

      //
      // 23a. Merge (5th Merge) source to target concept
      //
      addToLog("    23a. Merge (5th Merge) source to target concept ... " + date_format.format(timestamp));

      target.setEditingAuthority(null);
      target.setAuthority(null);
      target.setEditingTimestamp(null);

      MolecularAction ma23a = new MolecularMergeAction(source, target);
      client.setChangeStatus(false);
      client.processAction(ma23a);
      
      // re-read concept
      target = client.getConcept(target);
      
      target_rels = target.getRelationships();
      addToLog("        23a. Concept relationship must be deleted ...");
      if (target_rels.length == 0)
        addToLog("    23a. Test Passed");
      else {
        addToLog("    23a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid23a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid23a);

      //
      // 23b. Undo 23a
      //
      addToLog("    23b. Undo 23a ... "
               + date_format.format(timestamp));

      MolecularAction ma23b = ma23a;
      ma23b.setTransactionIdentifier(tid23a);
      client.processUndo(ma23b);
      
      //
      // 24a. Undo 22a
      //
      addToLog("    24a. Undo 22a ... "
               + date_format.format(timestamp));

      MolecularAction ma24a = ma22a;
      ma24a.setTransactionIdentifier(tid22a);
      client.processUndo(ma22a);

      //
      // 25a. Merge (6th Merge) source to target concept
      //
      addToLog("    25a. Merge (6th Merge) source to target concept ... " + date_format.format(timestamp));

      target.setEditingAuthority(null);
      target.setAuthority(null);
      target.setEditingTimestamp(null);

      MolecularAction ma25a = new MolecularMergeAction(source, target);
      client.setChangeStatus(false);
      client.processAction(ma25a);
      
      // re-read concept
      target = client.getConcept(target);
      
      target_rels = target.getRelationships();
      addToLog("        25a. Concept relationship must be deleted ...");
      if (target_rels.length == 0)
        addToLog("    25a. Test Passed");
      else {
        addToLog("    25a. Test Failed");
        thisTestFailed();
      }      

      // Save Transaction ID
      int tid25a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid25a);

      //
      // 25b. Undo 25a
      //
      addToLog("    25b. Undo 25a ... "
               + date_format.format(timestamp));

      MolecularAction ma25b = ma25a;
      ma25b.setTransactionIdentifier(tid25a);
      client.processUndo(ma25b);

      //
      // 26a. Undo 18a
      //
      addToLog("    26a. Undo 18a ... "
               + date_format.format(timestamp));

      MolecularAction ma26a = ma18a;
      ma26a.setTransactionIdentifier(tid18a);
      client.processUndo(ma26a);

      //
      // 27a. Undo 17a
      //
      addToLog("    27a. Undo 17a ... "
               + date_format.format(timestamp));

      MolecularAction ma27a = ma17a;
      ma27a.setTransactionIdentifier(tid17a);
      client.processUndo(ma27a);

      //
      // 28a. Undo 1a
      //
      addToLog("    28a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma28a = ma1a;
      ma28a.setTransactionIdentifier(tid1a);
      client.processUndo(ma28a);

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
      addToLog("Finished MolecularMergeActionTest at " +
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
