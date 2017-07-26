/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteWarning
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Warning;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Warning
 */
public class TestSuiteWarning extends TestSuite {

  public TestSuiteWarning() {
    setName("TestSuiteWarning");
    setDescription("Test Suite for Warning");
  }

  /**
   * Perform Test Suite Warning
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Warning warning = new Warning.Default("CODE1", "LOCATION1", "METHOD1", "MESSAGE1");

    addToLog("    1. Test Warning: getCode() ... "
             + date_format.format(timestamp));

    if (warning.getCode().equals("CODE1"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test Warning: setCode(), getCode() ... "
             + date_format.format(timestamp));

    warning.setCode("CODE2");
    if (!(warning.getCode().equals("CODE1")))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test Warning: getLocation() ... "
             + date_format.format(timestamp));

    if (warning.getLocation().equals("LOCATION1"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4. Test Warning: setLocation(), getLocation() ... "
             + date_format.format(timestamp));

    warning.setLocation("CODE2");
    if (!(warning.getLocation().equals("LOCATION1")))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog("    5. Test Warning: getMethod() ... "
             + date_format.format(timestamp));

    if (warning.getMethod().equals("METHOD1"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog("    6. Test Warning: setMethod(), getMethod() ... "
             + date_format.format(timestamp));

    warning.setMethod("CODE2");
    if (!(warning.getMethod().equals("METHOD1")))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog("    7. Test Warning: getMessage() ... "
             + date_format.format(timestamp));

    if (warning.getMessage().equals("MESSAGE1"))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog("    8. Test Warning: setMessage(), getMessage() ... "
             + date_format.format(timestamp));

    warning.setMessage("CODE2");
    if (!(warning.getMessage().toString().equals("MESSAGE1")))
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
    addToLog("Finished TestSuiteWarning at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}