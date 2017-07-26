/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteSemanticType
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for SemanticType
 */
public class TestSuiteSemanticType extends TestSuite {

  public TestSuiteSemanticType() {
    setName("TestSuiteSemanticType");
    setDescription("Test Suite for SemanticType");
  }

  /**
   * Perform Test Suite SemanticType
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    SemanticType sty = new SemanticType.Default();

    addToLog("    1. Test SemanticType: setIsChemical(), isChemical() ... "
             + date_format.format(timestamp));

    sty.setIsChemical(true);
    if (sty.isChemical())
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test SemanticType: setChemicalType(), getChemicalType() ... "
             + date_format.format(timestamp));

    sty.setChemicalType("N");
    if (sty.getChemicalType().equals("N"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test SemanticType: isFunctionalChemical() ... "
             + date_format.format(timestamp));

    sty.setChemicalType("F");
    if (sty.isFunctionalChemical())
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4. Test SemanticType: isStructuralChemical() ... "
             + date_format.format(timestamp));

    sty.setChemicalType("S");
    if (sty.isStructuralChemical())
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog("    5. Test SemanticType: setTreePosition(), getTreePosition() ... "
             + date_format.format(timestamp));

    sty.setTreePosition("CHILD");
    if (sty.getTreePosition().equals("CHILD"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog("    6. Test SemanticType: setDefinition(), getDefinition() ... "
             + date_format.format(timestamp));

    sty.setDefinition("FACT");
    if (sty.getDefinition().equals("FACT"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog("    7. Test SemanticType: setValue(), getValue() ... "
             + date_format.format(timestamp));

    sty.setValue("TEST");
    if (sty.getValue().equals("TEST"))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog("    8. Test SemanticType: setTypeIdentifier(), getTypeIdentifier() ... "
             + date_format.format(timestamp));

    Identifier identifier = new Identifier.Default("123");
    sty.setTypeIdentifier(identifier);
    if (sty.getTypeIdentifier().equals(identifier))
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog("    9. Test SemanticType: setIsEditingChemical(), isEditingChemical() ... "
             + date_format.format(timestamp));

    sty.setIsEditingChemical(true);
    if (sty.isEditingChemical())
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
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
    addToLog("Finished TestSuiteSemanticType at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}