/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.commmon
 * Object:  TestSuiteConcept
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Comparator;

/**
 * Test suite for Concept
 */
public class TestSuiteConcept extends TestSuite {

  public TestSuiteConcept() {
    setName("TestSuiteConcept");
    setDescription("Test Suite for Concept");
  }

  /**
   * Perform Test Suite Concept
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Concept concept = new Concept.Default();

    addToLog(
        "    0. Test Concept: clear() ... "
        + date_format.format(timestamp));

    concept.clear();

    addToLog(
        "    1.1. Test Concept: setCUI(), getCUI() ... "
        + date_format.format(timestamp));

    CUI cui = new CUI("C0000001");
    concept.setCUI(cui);
    if (concept.getCUI().equals(cui))
      addToLog("    1.1. Test Passed");
    else {
      addToLog("    1.1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    1.2. Test Concept: getCUIs() ... "
        + date_format.format(timestamp));

    CUI[] cuis = concept.getCUIs();
    for (int i=0; i<cuis.length; i++) {
      addToLog("            CUI["+i+"] = " + cuis[i]);
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }

    addToLog(
        "    2. Test Concept: setPreferredAtom(), getPreferredAtom() ... "
        + date_format.format(timestamp));

    Atom atom = new Atom.Default(1070);
    concept.setPreferredAtom(atom);
    if (concept.getPreferredAtom().equals(atom))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test Concept: setApprovalAction(), getApprovalAction() ... "
        + date_format.format(timestamp));

    LoggedAction action = null;
    concept.setApprovalAction(action);
    if (concept.getApprovalAction() == null)
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test Concept: setEditingAuthority(), getEditingAuthority() ... "
        + date_format.format(timestamp));

    Authority authority = new Authority.Default("KEVRIC");
    concept.setEditingAuthority(authority);
    if (concept.getEditingAuthority().equals(authority))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5.1. Test Concept: setEditingTimestamp(), getEditingTimestamp() ... "
        + date_format.format(timestamp));

    java.util.Date today = new java.util.Date();
    concept.setEditingTimestamp(today);
    if (concept.getEditingTimestamp().equals(today))
      addToLog("    5.1. Test Passed");
    else {
      addToLog("    5.1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5.2. Test Concept: setReadTimestamp(), getReadTimestamp() ... "
        + date_format.format(timestamp));

    today = new java.util.Date();
    concept.setReadTimestamp(today);
    if (concept.getReadTimestamp().equals(today))
      addToLog("    5.2. Test Passed");
    else {
      addToLog("    5.2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6.0. Test Concept: clearAtoms() ... "
        + date_format.format(timestamp));

    concept.clearAtoms();

    addToLog(
        "    6.1. Test Concept: addAtom(), getAtoms() ... "
        + date_format.format(timestamp));

    Atom[] x_atoms = new Atom[10];
    x_atoms[0] = new Atom.Default(1070);
    concept.addAtom(x_atoms[0]);
    x_atoms[1] = new Atom.Default(1071);
    concept.addAtom(x_atoms[1]);
    x_atoms[2] = new Atom.Default(1072);
    concept.addAtom(x_atoms[2]);
    Atom[] y_atoms = concept.getAtoms();
    boolean same_array = true;
    for (int i = 0; i < y_atoms.length; i++) {
      if (!(x_atoms[i].equals(y_atoms[i]))) {
        same_array = false;
      }
    }
    if (same_array)
      addToLog("    6.1. Test Passed");
    else {
      addToLog("    6.1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6.2. Test Concept: removeAtom(), getAtoms() ... "
        + date_format.format(timestamp));

    concept.removeAtom(x_atoms[1]);
    y_atoms = concept.getAtoms();
    same_array = true;
    for (int i = 0; i < y_atoms.length; i++) {
      if (!(x_atoms[i].equals(y_atoms[i]))) {
        same_array = false;
      }
    }
    if (!(same_array))
      addToLog("    6.2. Test Passed");
    else {
      addToLog("    6.2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6.3. Test Concept: getSortedAtoms() ... "
        + date_format.format(timestamp));

    Comparator oc = new Comparator() {
      public int compare(Object object1, Object object2) {
        return ((Comparable)object1).compareTo(object2);
      }
    };

    if (!(concept.getAtoms().equals(concept.getSortedAtoms(oc))))
      addToLog("    6.3. Test Passed");
    else {
      addToLog("    6.3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7.0. Test Concept: clearContextRelationships() ... "
        + date_format.format(timestamp));

    concept.clearContextRelationships();

    addToLog(
        "    7.1. Test Concept: addContextRelationship(), getContextRelationship() ... "
        + date_format.format(timestamp));

    ContextRelationship[] x_context_relationships = new ContextRelationship[10];
    x_context_relationships[0] = new ContextRelationship.Default(1041);
    concept.addContextRelationship(x_context_relationships[0]);
    x_context_relationships[1] = new ContextRelationship.Default(1042);
    concept.addContextRelationship(x_context_relationships[1]);
    x_context_relationships[2] = new ContextRelationship.Default(1043);
    concept.addContextRelationship(x_context_relationships[2]);
    ContextRelationship[] y_context_relationships = concept.getContextRelationships();
    same_array = true;
    for (int i = 0; i < y_context_relationships.length; i++) {
      if (!(x_context_relationships[i].equals(y_context_relationships[i]))) {
        same_array = false;
      }
    }
    if (same_array)
      addToLog("    7.1. Test Passed");
    else {
      addToLog("    7.1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7.2. Test Concept: removeContextRelationship(), getContextRelationship() ... "
        + date_format.format(timestamp));

    concept.removeContextRelationship(x_context_relationships[1]);
    y_context_relationships = concept.getContextRelationships();
    same_array = true;
    for (int i = 0; i < y_context_relationships.length; i++) {
      if (!(x_context_relationships[i].equals(y_context_relationships[i]))) {
        same_array = false;
      }
    }
    if (!(same_array))
      addToLog("    7.2. Test Passed");
    else {
      addToLog("    7.2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7.3. Test Concept: getSortedContextRelationship() ... "
        + date_format.format(timestamp));

    if (!(concept.getContextRelationships().equals(concept.getSortedContextRelationships(oc))))
      addToLog("    7.3. Test Passed");
    else {
      addToLog("    7.3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test Concept: hasChemicalSemanticType() ... "
        + date_format.format(timestamp));

    ConceptSemanticType attribute = new ConceptSemanticType();
    concept = new Concept.Default();
    attribute.setIsChemical(true);
    attribute.setTobereleased('Y');
    concept.addAttribute(attribute);
    if (concept.hasChemicalSemanticType())
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    9. Test Concept: hasSemanticType() ... "
        + date_format.format(timestamp));

    attribute.setName("SEMANTIC_TYPE");
    concept.addAttribute(attribute);
    if (concept.hasSemanticType())
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    10. Test Concept: isCurrentMeSHMainHeading() ... "
        + date_format.format(timestamp));

    atom = new Atom.Default(1070);
    Termgroup termgroup = new Termgroup.Default();
    Source source = new Source.Default();
    source.setSourceAbbreviation("MSH2001");
    source.setRootSourceAbbreviation("MSH");
    source.setIsCurrent(true);
    termgroup.setSource(source);
    termgroup.setTermType("MH");
    atom.setSource(source);
    atom.setTermgroup(termgroup);
    atom.setCode(new Code("D0000"));
    concept.addAtom(atom);

    if (concept.isCurrentMeSHMainHeading())
      addToLog("    10. Test Passed");
    else {
      addToLog("    10. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    11. Test Concept: isSupplementaryConceptOnly() ... "
        + date_format.format(timestamp));

    source.setSourceAbbreviation("UMD2001");
    if (!(concept.isSupplementaryConceptOnly()))
      addToLog("    11. Test Passed");
    else {
      addToLog("    11. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    12. Test Concept: isNonHuman() ... "
        + date_format.format(timestamp));

    attribute.setName("NON_HUMAN");
    concept.addAttribute(attribute);
    if (concept.isNonHuman())
      addToLog("    12. Test Passed");
    else {
      addToLog("    12. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    13. Test Concept: getSemanticTypes() ... "
        + date_format.format(timestamp));

    SemanticType[] stys = concept.getSemanticTypes();
    for (int i=0; i<stys.length; i++) {
      addToLog("            Semantic Type["+i+"] = " + stys[i]);
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }

    addToLog(
        "    14. Test Concept: getDemotions() ... "
        + date_format.format(timestamp));

    Relationship[] demotions = concept.getDemotions();
    for (int i=0; i<demotions.length; i++) {
      addToLog("            Demotion["+i+"] = " + demotions[i]);
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }

    addToLog(
        "    15. Test Concept: getLexicalRelationships() ... "
        + date_format.format(timestamp));

    Relationship[] lexical_rels = concept.getLexicalRelationships();
    for (int i=0; i<lexical_rels.length; i++) {
      addToLog("            Lexical Relationship["+i+"] = " + lexical_rels[i]);
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }

    addToLog(
        "    16. Test Concept: getRestrictedAtoms() ... "
        + date_format.format(timestamp));

    addToLog("            Not Tested.");

    /*
    Atom[] atoms = concept.getRestrictedAtoms(new ByStrippedSourceRestrictor("MTH"));
    for (int i=0; i<atoms.length; i++) {
      addToLog("            Restricted Atom["+i+"] = " + atoms[i]);
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }
    */

    addToLog(
        "    17. Test Concept: getRestrictedContextRelationships() ... "
        + date_format.format(timestamp));

    addToLog("            Not Tested.");

    /*
    ContextRelationship[] cxt_rels = concept.getRestrictedContextRelationships(new ByStrippedSourceRestrictor("MTH"));
    for (int i=0; i<atoms.length; i++) {
      addToLog("            Restricted Relationship["+i+"] = " + cxt_rels[i]);
      if (i > 5) {
        addToLog("          >>> Loop terminated. Only few records displayed.");
        break;
      }
    }
    */

    addToLog(
        "    18. Test Concept: getAmbiguous() ... "
        + date_format.format(timestamp));

    atom.setIsAmbiguous(true);
    concept.addAtom(atom);
    if (concept.getAmbiguous())
      addToLog("    18. Test Passed");
    else {
      addToLog("    18. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    19. Test Concept: contains() ... "
        + date_format.format(timestamp));

    if (concept.contains(atom))
      addToLog("    19. Test Passed");
    else {
      addToLog("    19. Test Failed");
      thisTestFailed();
    }

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
    addToLog("Finished TestSuiteConcept at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}