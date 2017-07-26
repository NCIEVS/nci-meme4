/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteCoocurrence
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Coocurrence;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Coocurrence
 */
public class TestSuiteCoocurrence extends TestSuite {

  public TestSuiteCoocurrence() {
    setName("TestSuiteCoocurrence");
    setDescription("Test Suite for Coocurrence");
  }

  /**
   * Perform Test Suite Coocurrence
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Coocurrence coocurrence = new Coocurrence();

    coocurrence.setName("SFO/LFO");
    addToLog("    1. Test Coocurrence: getType() ... "
        + date_format.format(timestamp));

    addToLog("       Type = " + coocurrence.getType());

    addToLog("    2. Test Coocurrence: setFrequency(), getFrequency() ... "
        + date_format.format(timestamp));

    int freq = 10;
    coocurrence.setFrequency(freq);
    if (coocurrence.getFrequency() == freq)
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
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
    addToLog("Finished TestSuiteCoocurrence at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}