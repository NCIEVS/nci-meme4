/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteIdentifier
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Identifier
 */
public class TestSuiteIdentifier extends TestSuite {

  public TestSuiteIdentifier() {
    setName("TestSuiteIdentifier");
    setDescription("Test Suite for Identifier");
  }

  /**
   * Perform Test Suite Identifier
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Identifier identifier = new Identifier.Default("111");

    addToLog("    1. Test Identifier: toString() ... "
             + date_format.format(timestamp));

    if (identifier.toString().equals("111"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test Identifier: equals() ... "
             + date_format.format(timestamp));

    Identifier id = new Identifier.Default("111");
    if (identifier.equals(id))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test Identifier: intValue() ... "
             + date_format.format(timestamp));

    if (identifier.intValue() == 111)
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
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
    addToLog("Finished TestSuiteIdentifier at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}