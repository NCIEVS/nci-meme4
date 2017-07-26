/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteRelationship
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.RUI;

/**
 * Test suite for Relationship
 */
public class TestSuiteRelationship extends TestSuite {

  public TestSuiteRelationship() {
    setName("TestSuiteRelationship");
    setDescription("Test Suite for Relationship");
  }

  /**
   * Perform Test Suite Relationship
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Relationship relationship = new Relationship.Default();

    addToLog("    1. Test Relationship: setAtom(), getAtom() ... "
             + date_format.format(timestamp));

    Atom atom = new Atom.Default(1046);
    relationship.setAtom(atom);
    if (relationship.getAtom().equals(atom))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test Relationship: setRelatedAtom(), getRelatedAtom() ... "
             + date_format.format(timestamp));

    atom = new Atom.Default(1047);
    relationship.setRelatedAtom(atom);
    if (relationship.getRelatedAtom().equals(atom))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test Relationship: setConcept(), getConcept() ... "
             + date_format.format(timestamp));

    Concept concept = new Concept.Default(1070);
    relationship.setConcept(concept);
    if (relationship.getConcept().equals(concept))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4. Test Relationship: setRelatedConcept(), getRelatedConcept() ... "
             + date_format.format(timestamp));

    concept = new Concept.Default(1071);
    relationship.setRelatedConcept(concept);
    if (relationship.getRelatedConcept().equals(concept))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog("    5. Test Relationship: setName(), getName() ... "
             + date_format.format(timestamp));

    relationship.setName("BT");
    if (relationship.getName().equals("BT"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog("    6. Test Relationship: setAttribute(), getAttribute() ... "
             + date_format.format(timestamp));

    relationship.setAttribute("INGREDIENT_OF");
    if (relationship.getAttribute().equals("INGREDIENT_OF"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog("    7. Test Relationship: setSourceOfLabel(), getSourceOfLabel() ... "
             + date_format.format(timestamp));

    Source source = new Source.Default("NLM01");
    relationship.setSourceOfLabel(source);
    if (relationship.getSourceOfLabel().equals(source))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog("    8. Test Relationship: setRelatedNativeIdentifier(), getRelatedNativeIdentifier() ... "
             + date_format.format(timestamp));

    NativeIdentifier ni = new NativeIdentifier("CODE", "SOURCE_AUI", "MTH", "", "");
    relationship.setRelatedNativeIdentifier(ni);
    if (relationship.getRelatedNativeIdentifier().equals(ni))
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog("    9. Test Relationship: setGroupIdentifier(), getGroupIdentifier() ... "
             + date_format.format(timestamp));

    Identifier id = new Identifier.Default(12345);
    relationship.setGroupIdentifier(id);
    if (relationship.getGroupIdentifier().equals(id))
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
      thisTestFailed();
    }

    addToLog("    10. Test Relationship: setRUI(), getRUI() ... "
             + date_format.format(timestamp));

    RUI rui = new RUI("R12345");
    relationship.setRUI(rui);
    if (relationship.getRUI().equals(rui))
      addToLog("    10. Test Passed");
    else {
      addToLog("    10. Test Failed");
      thisTestFailed();
    }

    addToLog("    11. Test Relationship: setIsInverse(), isInverse() ... "
             + date_format.format(timestamp));

    relationship.setIsInverse(true);
    if (relationship.isInverse())
      addToLog("    11. Test Passed");
    else {
      addToLog("    11. Test Failed");
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
    addToLog("Finished TestSuiteRelationship at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}