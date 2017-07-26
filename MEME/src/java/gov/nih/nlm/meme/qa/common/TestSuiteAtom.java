/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteAtom
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.SafeReplacementFact;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Comparator;

/**
 * Test suite for Atom
 */
public class TestSuiteAtom extends TestSuite {

  public TestSuiteAtom() {
    setName("TestSuiteAtom");
    setDescription("Test Suite for Atom");
  }

  /**
   * Perform Test Suite Atom
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Atom atom = new Atom.Default();

    addToLog(
        "    0. Test Atom: clear() ... "
        + date_format.format(timestamp));

    atom.clear();

    addToLog(
        "    1. Test Atom: setTermgroup(), getTermgroup() ... "
        + date_format.format(timestamp));

    Termgroup termgroup = new Termgroup.Default("MSH2000/PM");
    atom.setTermgroup(termgroup);
    if (atom.getTermgroup().equals(termgroup))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test Atom: setCode(), getCode() ... "
        + date_format.format(timestamp));

    Code code = new Code("CODE1");
    atom.setCode(code);
    if (atom.getCode().equals(code))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test Atom: setLastReleaseCUI(), getLastReleaseCUI() ... "
        + date_format.format(timestamp));

    CUI cui = new CUI("C0000001");
    atom.setLastReleaseCUI(cui);
    if (atom.getLastReleaseCUI().equals(cui))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test Atom: setLastAssignedCUI(), getLastAssignedCUI() ... "
        + date_format.format(timestamp));

    cui = new CUI("C0000002");
    atom.setLastAssignedCUI(cui);
    if (atom.getLastAssignedCUI().equals(cui))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test Atom: setConcept(), getConcept() ... "
        + date_format.format(timestamp));

    Concept concept = new Concept.Default(1070);
    atom.setConcept(concept);
    if (atom.getConcept().equals(concept))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test Atom: setLanguage(), getLanguage() ... "
        + date_format.format(timestamp));

    atom.setLanguage(new Language.Default("English","ENG"));
    if (atom.getLanguage().getAbbreviation().equals("ENG"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test Atom: addIndexWord(), setIndexWords(), getIndexWords ... "
        + date_format.format(timestamp));

    String[] words = {"word1", "word2", "word3"};
    atom.setIndexWords(words);
    atom.addIndexWord("word4");
    String[] word_index = atom.getIndexWords();
    boolean same_array = true;
    for (int i = 0; i < words.length; i++) {
      if (!(word_index[i].equals(words[i]))) {
        same_array = false;
      }
    }
    if (same_array)
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test Atom: addNormalizedIndexWord(), setNormalizedIndexWords(), getNormalizedIndexWords() ... "
        + date_format.format(timestamp));

    String[] norm_words = {"String1", "String2", "String3"};
    atom.setNormalizedIndexWords(norm_words);
    atom.addNormalizedIndexWord("String4");
    word_index = atom.getNormalizedIndexWords();
    same_array = true;
    for (int i = 0; i < norm_words.length; i++) {
      if (!(word_index[i].equals(norm_words[i]))) {
        same_array = false;
      }
    }
    if (same_array)
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    9. Test Atom: addNormalizedIndexString(), setNormalizedIndexStrings(), getNormalizedIndexStrings() ... "
        + date_format.format(timestamp));

    String[] norm_strings = {"String1", "String2", "String3"};
    atom.setNormalizedIndexStrings(norm_strings);
    atom.addNormalizedIndexString("String4");
    String[] string_index = atom.getNormalizedIndexStrings();
    same_array = true;
    for (int i = 0; i < norm_strings.length; i++) {
      if (!(string_index[i].equals(norm_strings[i]))) {
        same_array = false;
      }
    }
    if (same_array)
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    10. Test Atom: setisAmbiguous(), isAmbiguous() ... "
        + date_format.format(timestamp));

    atom.setIsAmbiguous(true);
    if (atom.isAmbiguous())
      addToLog("    10. Test Passed");
    else {
      addToLog("    10. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    11. Test Atom: setAUI(), getAUI() ... "
        + date_format.format(timestamp));

    AUI aui = new AUI("A0000001");
    atom.setAUI(aui);
    if (atom.getAUI().equals(aui))
      addToLog("    11. Test Passed");
    else {
      addToLog("    11. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    12.1 Test Atom: addTranslationAtom(), getTranslationAtoms() ... "
        + date_format.format(timestamp));

    Atom foreign_atom = new Atom.Default(12345);
    atom.addTranslationAtom(foreign_atom);
    Atom[] atoms = atom.getTranslationAtoms();
    boolean found = false;
    for (int i=0; i<atoms.length; i++) {
      if (atoms[i].equals(foreign_atom))
        found = true;
      else
        found = false;
    }
    if (found)
      addToLog("    12.1 Test Passed");
    else {
      addToLog("    12.1 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    12.2 Test Atom: setTranslationAtoms(), getTranslationAtoms() ... "
        + date_format.format(timestamp));

    atom.setTranslationAtoms(atoms);
    atoms = atom.getTranslationAtoms();
    found = false;
    for (int i=0; i<atoms.length; i++) {
      if (atoms[i].equals(foreign_atom))
        found = true;
      else
        found = false;
    }
    if (found)
      addToLog("    12.2 Test Passed");
    else {
      addToLog("    12.2 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    12.3 Test Atom: removeTranslationAtom(), getTranslationAtoms() ... "
        + date_format.format(timestamp));

    atom.removeTranslationAtom(foreign_atom);
    atoms = atom.getTranslationAtoms();
    found = false;
    for (int i=0; i<atoms.length; i++) {
      if (atoms[i].equals(foreign_atom))
        found = true;
      else
        found = false;
    }
    if (found)
      addToLog("    12.3 Test Passed");
    else {
      addToLog("    12.3 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    12.4 Test Atom: clearTranslationAtoms() ... "
        + date_format.format(timestamp));

    atom.clearTranslationAtoms();
    atoms = atom.getTranslationAtoms();
    if (atoms.length == 0)
      addToLog("    12.4 Test Passed");
    else {
      addToLog("    12.4 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    13.1 Test Atom: addContextRelationship(), removeContextRelationship(), " +
        " getContextRelationships(), getSortedContextRelationships() ... "
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
      addToLog("    13.1 Test Passed");
    else {
      addToLog("    13.1 Test Failed");
      thisTestFailed();
    }

    concept.removeContextRelationship(x_context_relationships[1]);
    y_context_relationships = concept.getContextRelationships();
    same_array = true;
    for (int i = 0; i < y_context_relationships.length; i++) {
      if (!(x_context_relationships[i].equals(y_context_relationships[i]))) {
        same_array = false;
      }
    }
    if (!(same_array))
      addToLog("    13.2 Test Passed");
    else {
      addToLog("    13.2 Test Failed");
      thisTestFailed();
    }

    Comparator oc = new Comparator() {
      public int compare(Object object1, Object object2) {
        return ((Comparable)object1).compareTo(object2);
      }
    };

    if (!(concept.getContextRelationships().equals(concept.getSortedContextRelationships(oc))))
      addToLog("    13.3 Test Passed");
    else {
      addToLog("    13.3 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    14.1 Test Atom: addSafeReplacementFact(), getSafeReplacementFacts() ... "
        + date_format.format(timestamp));

    SafeReplacementFact srf = new SafeReplacementFact();
    Atom new_atom = new Atom.Default(2222);
    atom.setIdentifier(new Identifier.Default(2223));
    // srf equals method requires old/new atoms and a source
    srf.setNewAtom(new_atom);
    srf.setOldAtom(atom);
    srf.setSource(new Source.Default("MTH"));
    new_atom.addSafeReplacementFact(srf);
    atom.addSafeReplacementFact(srf);
    SafeReplacementFact[] srfs = atom.getSafeReplacementFacts();
    found = false;
    for (int i=0; i<srfs.length; i++) {
      if (srfs[i].equals(srf))
        found = true;
      else
        found = false;
    }
    if (found)
      addToLog("    14.1 Test Passed");
    else {
      addToLog("    14.1 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    14.2 Test Atom: getBestSafeReplacementFact() ... "
        + date_format.format(timestamp));

    srf = atom.getBestSafeReplacementFact();

    addToLog(
        "    14.3 Test Atom: isSafeReplacementFor() ... "
        + date_format.format(timestamp));

    if (new_atom.isSafeReplacementFor(atom))
      addToLog("    14.3 Test Passed");
    else {
      addToLog("    14.3 Test Failed");
      thisTestFailed();
    }

    atom.removeSafeReplacementFact(srf);
    srfs = atom.getSafeReplacementFacts();
    found = false;
    for (int i=0; i<srfs.length; i++) {
      if (srfs[i].equals(srf))
        found = true;
      else
        found = false;
    }
    if (!found)
      addToLog("    14.4 Test Passed");
    else {
      addToLog("    14.4 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    15. Test Atom: setSourceConceptIdentifier(), getSourceConceptIdentifier() ... "
        + date_format.format(timestamp));

    Identifier id = new Identifier.Default(12345);
    atom.setSourceConceptIdentifier(id);
    if (atom.getSourceConceptIdentifier().equals(id))
      addToLog("    15. Test Passed");
    else {
      addToLog("    15. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    16. Test Atom: setSourceDescriptorIdentifier(), getSourceDescriptorIdentifier() ... "
        + date_format.format(timestamp));

    id = new Identifier.Default(12346);
    atom.setSourceDescriptorIdentifier(id);
    if (atom.getSourceDescriptorIdentifier().equals(id))
      addToLog("    16. Test Passed");
    else {
      addToLog("    16. Test Failed");
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
    addToLog("Finished TestSuiteAtom at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}