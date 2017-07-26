/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteAttribute
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.ATUI;

/**
 * Test suite for Attribute
 */
public class TestSuiteAttribute extends TestSuite {

  public TestSuiteAttribute() {
    setName("TestSuiteAttribute");
    setDescription("Test Suite for Attribute");
  }

  /**
   * Perform Test Suite Attribute
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Attribute attribute = new Attribute.Default();

    addToLog(
        "    1. Test Attribute: setAtom(), getAtom() ... "
        + date_format.format(timestamp));

    Atom atom = new Atom.Default(1046);
    attribute.setAtom(atom);
    if (attribute.getAtom().equals(atom))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test Attribute: setConcept(), getConcept() ... "
        + date_format.format(timestamp));

    Concept concept = new Concept.Default(1070);
    attribute.setConcept(concept);
    if (attribute.getConcept().equals(concept))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test Attribute: setName(), getName() ... "
        + date_format.format(timestamp));

    attribute.setName("DST");
    if (attribute.getName().equals("DST"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test Attribute: setValue(), getValue() ... "
        + date_format.format(timestamp));

    attribute.setValue("capsule");
    if (attribute.getValue().equals("capsule"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test Attribute: setATUI(), getATUI() ... "
        + date_format.format(timestamp));

    ATUI atui = new ATUI("AT001");
    attribute.setATUI(atui);
    if (attribute.getATUI().equals(atui))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
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
    addToLog("Finished TestSuiteAttribute at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}