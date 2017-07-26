/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteRank
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Rank
 */
public class TestSuiteRank extends TestSuite {

  public TestSuiteRank() {
    setName("TestSuiteRank");
    setDescription("Test Suite for Rank");
  }

  /**
   * Perform Test Suite Rank
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Rank rank = new Rank.Default("111");

    addToLog("    1. Test Rank: toString() ... "
             + date_format.format(timestamp));

    if (rank.toString().equals("111"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test Rank: intValue() ... "
             + date_format.format(timestamp));

    if (rank.intValue() == 111)
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test Rank: compareTo() ... "
             + date_format.format(timestamp));

    if (rank.compareTo(new Rank.Default("112")) == -1)
      addToLog("    3.1. Test Passed");
    else {
      addToLog("    3.1. Test Failed");
      thisTestFailed();
    }

    if ((new Rank.Default(111)).compareTo(new Rank.Default(112)) == -1)
      addToLog("    3.2. Test Passed");
    else {
      addToLog("    3.2. Test Failed");
      thisTestFailed();
    }

    if (rank.compareTo(new Rank.Default("0112")) == 1)
      addToLog("    3.3. Test Passed");
    else {
      addToLog("    3.3. Test Failed");
      thisTestFailed();
    }

    if (rank.compareTo(new Rank.Default("111")) == 0)
      addToLog("    3.4. Test Passed");
    else {
      addToLog("    3.4. Test Failed");
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
    addToLog("Finished TestSuiteRank at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}