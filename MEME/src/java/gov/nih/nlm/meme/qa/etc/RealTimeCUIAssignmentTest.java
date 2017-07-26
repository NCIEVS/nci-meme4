/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.etc
 * Object:  RealTimeCUIAssignmentTest.java
 *
 * Author:  tkao
 *
 * History:
 *   Jan 19, 2005: 1st Version.
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.etc;

import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMergeAction;
import gov.nih.nlm.meme.action.MolecularMoveAction;
import gov.nih.nlm.meme.action.MolecularSplitAction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class RealTimeCUIAssignmentTest
    extends TestSuite {

  private int concept_id_2 = 102;

  public RealTimeCUIAssignmentTest() {
    setName("RealTimeCUIAssignmentTest");
    setDescription("Careful as this test use up CUI assignments. This test the real time CUI assignment algorithm.");
    setConceptId(101);
  }

  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //setup
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      EditingClient client = getClient();
      Concept concept1 = client.getConcept(this.getConceptId());
      Concept concept2 = client.getConcept(concept_id_2);

      Date timestamp = new Date(System.currentTimeMillis());
      addToLog(
          "    Case 1, Create CUI assigments..." +
          date_format.format(timestamp));

      concept1.getAtoms()[0].setTobereleased('Y');
      concept2.getAtoms()[0].setTobereleased('y');

      MolecularChangeAtomAction change_atom_action1 = new
          MolecularChangeAtomAction(concept1.getAtoms()[0]);
      client.processAction(change_atom_action1);
      MolecularChangeAtomAction change_atom_action2 = new
          MolecularChangeAtomAction(concept2.getAtoms()[0]);
      client.processAction(change_atom_action2);

      concept1 = client.getConcept(this.getConceptId());
      concept2 = client.getConcept(concept_id_2);

      CUI cui1 = concept1.getCUI();
      CUI cui2 = concept2.getCUI();

      addToLog("    Concept1 = " + cui1);
      addToLog("    Concept2 = " + cui2);
      
      if (cui1 == null)
        throw new MEMEException("CUI was not assigned for Concept 1.");
      if (cui2 == null)
        throw new MEMEException("CUI was not assigned for Concept 2.");

      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 1 is complete..." + date_format.format(timestamp));

      addToLog("    Case 2 merge concept 1 and concept 2..." + date_format.format(timestamp));

      MolecularMergeAction merge_action = new MolecularMergeAction(concept1, concept2);
      client.processAction(merge_action);
     
      concept2 = client.getConcept(concept2);
      CUI current_cui = concept2.getCUI();
      addToLog("    Concept 2 = " + current_cui);
      
      if (!current_cui.toString().equals(cui1.toString()))
        throw new MEMEException("The merged concept has the incorrect CUI assignment.");
     
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 2 is complete..." + date_format.format(timestamp));

      addToLog("    Case 3 split concept2..." + date_format.format(timestamp));

      MolecularSplitAction split_action = new MolecularSplitAction(concept2);
      split_action.addAtomToSplit(concept2.getPreferredAtom());
      client.processAction(split_action);

      concept2 = client.getConcept(concept2);
      Concept new_concept = client.getAtom(101).getConcept();

      addToLog("    Concept 2 = " + concept2.getCUI());
      addToLog("    Concept 3 = " + new_concept.getCUI());
      
      if (concept2.getCUI().toString().equals(cui1.toString()))
        throw new MEMEException("Concept2 did not get a new CUI");
      if (!new_concept.getCUI().toString().equals(cui1.toString()))
        throw new MEMEException("The new concept should inherit the CUI from the splited concept");

      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 3 is complete..." + date_format.format(timestamp));

      addToLog("    Case 4 Add an atom to concept 2 and move it to a new concept..." + date_format.format(timestamp));

      CUI previous_cui = concept2.getCUI();
      Atom new_atom = TestSuiteUtils.createAtom("CUI Assignment Atom",client);
      new_atom.setTobereleased('Y');
      concept2.addAtom(new_atom);
      new_atom.setConcept(concept2);

      MolecularInsertAtomAction insert_atom_action = new MolecularInsertAtomAction(new_atom);
      client.processAction(insert_atom_action);


      concept2 = client.getConcept(concept2);
      new_concept = client.getConcept(104);
      MolecularMoveAction move_action = new MolecularMoveAction(concept2,new_concept);
      move_action.addAtomToMove(new_atom);
      client.processAction(move_action);

      concept2 = client.getConcept(concept2);
      new_concept = client.getConcept(new_concept);
      
      addToLog("    Concept 2 = " + concept2.getCUI());
      Atom[] atoms = concept2.getAtoms();
      for (int i=0; i<atoms.length; i++) {
      	addToLog("      atoms["+i+"] = " + atoms[i].getTermgroup() + " " + 
      			atoms[i].getTobereleased() + " " + atoms[i].getRank() + " " +
      			atoms[i].getLastAssignedCUI());
      }
      addToLog("    Concept 4 = " + new_concept.getCUI());
      atoms = new_concept.getAtoms();
      for (int i=0; i<atoms.length; i++) {
      	addToLog("      atoms["+i+"] = " + atoms[i].getTermgroup() + " " + 
      			atoms[i].getTobereleased() + " " + atoms[i].getRank() + " " +
      			atoms[i].getLastAssignedCUI());
      }
      
      if (!new_concept.getCUI().toString().equals(previous_cui.toString()))
        throw new MEMEException("New concept did not get new CUI after higher preference atom is received.");

      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 4 is complete..." + date_format.format(timestamp));

      addToLog("    Case 5 move the atom back..." + date_format.format(timestamp));

      new_atom = client.getAtom(new_atom);
      move_action = new MolecularMoveAction(new_concept,concept2);
      move_action.addAtomToMove(new_atom);
      client.processAction(move_action);

      concept2 = client.getConcept(concept2);

      previous_cui = new_concept.getCUI();
      new_concept = client.getConcept(new_concept);
           
      addToLog("    Concept 2 = " + concept2.getCUI());
      
      atoms = concept2.getAtoms();
      for (int i=0; i<atoms.length; i++) {
      	addToLog("      atoms["+i+"] = " + atoms[i].getTermgroup() + " " + 
      			atoms[i].getTobereleased() + " " + atoms[i].getRank() + " " +
      			atoms[i].getLastAssignedCUI());
      }
      addToLog("    Concept 4 (before move) = " + previous_cui);
      addToLog("    Concept 4 (after move) = " + new_concept.getCUI());
      atoms = new_concept.getAtoms();
      for (int i=0; i<atoms.length; i++) {
      	addToLog("      atoms["+i+"] = " + atoms[i].getTermgroup() + " " + 
      			atoms[i].getTobereleased() + " " + atoms[i].getRank() + " " +
      			atoms[i].getLastAssignedCUI());
      }
     
      if (!previous_cui.toString().equals(concept2.getCUI().toString()))
        throw new MEMEException("CUI related to the prefered atom did not move back with the atom.");

      if (new_concept.getCUI() != null)
        throw new MEMEException("CUI from the moved concept should be cleaned or null.");

      new_concept.getAtoms()[0].setTobereleased('y');
      MolecularChangeAtomAction change_atom_action = new
          MolecularChangeAtomAction(new_concept.getAtoms()[0]);
      client.processAction(change_atom_action);
      new_concept = client.getConcept(new_concept);

      addToLog("    Concept 4 (make atom releasable) = " + new_concept.getCUI());
      atoms = new_concept.getAtoms();
      for (int i=0; i<atoms.length; i++) {
      	addToLog("      atoms["+i+"] = " + atoms[i].getTermgroup() + " " + 
      			atoms[i].getTobereleased() + " " + atoms[i].getRank() + " " +
      			atoms[i].getLastAssignedCUI());
      }
      
      if (previous_cui.toString().equals(new_concept.getCUI().toString()))
        throw new MEMEException("CUI for this concept should be new.");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 5 is complete..." + date_format.format(timestamp));

      addToLog("    Case 6 insert STY then delete it..." + date_format.format(timestamp));

      new_concept = client.getConcept(105);
      Attribute sty = new Attribute.Default();
      sty.setName("SEMANTIC_TYPE");
      sty.setValue("Enzyme");
      sty.setSource(client.getSource("MTH"));
      sty.setConcept(new_concept);
      sty.setReleased('N');
      sty.setTobereleased('Y');
      sty.setLevel('C');
      sty.setStatus('R');
      new_concept.addAttribute(sty);
      MolecularInsertAttributeAction insert_sty_action = new MolecularInsertAttributeAction(sty);
      client.processAction(insert_sty_action);
      new_concept = client.getConcept(new_concept);

      addToLog("    Concept 5 = " + new_concept.getCUI());
      
      if (new_concept.getCUI() != null)
        throw new MEMEException("STY insertion should not affect the CUI assignment.");

      client.processUndo(insert_sty_action);
      new_concept = client.getConcept(new_concept);

      if (new_concept.getCUI() != null)
        throw new MEMEException("STY insertion should not affect the CUI assignment.");

      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 6 is complete..." + date_format.format(timestamp));

      addToLog("    Case 7 insert relationship then delete it..." + date_format.format(timestamp));
      new_concept = client.getConcept(new_concept);
      concept2 = client.getConcept(106);
      Relationship rel = TestSuiteUtils.createConceptLevelRelationship(
            client, new_concept, concept2);
      MolecularInsertRelationshipAction insert_rel_action = new MolecularInsertRelationshipAction(rel);
      client.processAction(insert_rel_action);

      addToLog("    Concept 6 = " + new_concept.getCUI());
      
      new_concept = client.getConcept(new_concept);
      if (new_concept.getCUI() != null)
        throw new MEMEException("Relationship insertion should not affect the CUI assignment.");

      client.processUndo(insert_rel_action);

      if (new_concept.getCUI() != null)
        throw new MEMEException("Relationship deletion should not affect the CUI assignment.");
      timestamp.setTime(System.currentTimeMillis());
      addToLog("      Case 7 is complete..." + date_format.format(timestamp));

      addToLog(this.getName() + " passed");

    }
    catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }
}
