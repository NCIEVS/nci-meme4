/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteLocator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Locator;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.SUI;
import gov.nih.nlm.meme.common.Identifier;

/**
 * Test suite for Locator
 */
public class TestSuiteLocator extends TestSuite {

  public TestSuiteLocator() {
    setName("TestSuiteLocator");
    setDescription("Test Suite for Locator");
  }

  /**
   * Perform Test Suite Locator
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Locator locator = new Locator();

    addToLog(
        "    1. Test Locator: setFrequency(String), getFrequency() ... " +
        date_format.format(timestamp));

    locator.setFrequency("FRQ");
    if (locator.getFrequency().equals("FRQ"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test Locator: setSUI(String), getSUI() ... " +
        date_format.format(timestamp));

    locator.setSUI(new SUI("S0000001"));
    if (locator.getSUI().equals(new SUI("S0000001")))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test Locator: setString(String), getString() ... " +
        date_format.format(timestamp));

    locator.setString("LOCATOR");
    if (locator.getString().equals("LOCATOR"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test Locator: setRecordIdentifier(Identifier), getRecordIdentifier() ... " +
        date_format.format(timestamp));

    locator.setRecordIdentifier(new Identifier.Default("12345"));
    if (locator.getRecordIdentifier().equals(new Identifier.Default("12345")))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
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
    addToLog("Finished TestSuiteLocator at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}