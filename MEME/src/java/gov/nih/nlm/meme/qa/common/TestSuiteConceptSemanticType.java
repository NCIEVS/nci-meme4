/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteConceptSemanticType
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for ConceptSemanticType
 */
public class TestSuiteConceptSemanticType extends TestSuite {

  public TestSuiteConceptSemanticType() {
    setName("TestSuiteConceptSemanticType");
    setDescription("Test Suite for ConceptSemanticType");
  }

  /**
   * Perform Test Suite ConceptSemanticType
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ConceptSemanticType csty = new ConceptSemanticType(123456);

    addToLog(
        "    1. Test ConceptSemanticType: setIsEditingChemical(boolean), " +
        " isEditingChemical() ... " +
        date_format.format(timestamp));

    csty.setIsEditingChemical(true);

    if (csty.isEditingChemical())
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test ConceptSemanticType: setIsChemical(boolean), " +
        " isChemical() ... " +
        date_format.format(timestamp));

    csty.setIsChemical(true);

    if (csty.isChemical())
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test ConceptSemanticType: setChemicalType(String), " +
        " getChemicalType() ... " +
        date_format.format(timestamp));

    csty.setChemicalType("TYPE");

    if (csty.getChemicalType().equals("TYPE"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test ConceptSemanticType: setTreePosition(String), " +
        " getTreePosition() ... " +
        date_format.format(timestamp));

    csty.setTreePosition("TREE_POSITION");

    if (csty.getTreePosition().equals("TREE_POSITION"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test ConceptSemanticType: setDefinition(String), " +
        " getDefinition() ... " +
        date_format.format(timestamp));

    csty.setDefinition("DEFINITION");

    if (csty.getDefinition().equals("DEFINITION"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test ConceptSemanticType: setValue(String), " +
        " getValue() ... " +
        date_format.format(timestamp));

    csty.setValue("VALUE");

    if (csty.getValue().equals("VALUE"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test ConceptSemanticType: isFunctionalChemical() ... " +
        date_format.format(timestamp));

    if (!csty.isFunctionalChemical())
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test ConceptSemanticType: isStructuralChemical() ... " +
        date_format.format(timestamp));

    if (!csty.isStructuralChemical())
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
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
    addToLog("Finished TestSuiteConceptSemanticType at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}