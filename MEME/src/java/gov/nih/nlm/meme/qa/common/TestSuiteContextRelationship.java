/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteContextRelationship
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.ContextPath;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for ContextRelationship
 */
public class TestSuiteContextRelationship extends TestSuite {

  public TestSuiteContextRelationship() {
    setName("TestSuiteContextRelationship");
    setDescription("Test Suite for ContextRelationship");
  }

  /**
   * Perform Test Suite ContextRelationship
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ContextRelationship cr = new ContextRelationship.Default();

    addToLog(
        "    1. Test ContextRelationship: setHierarchicalCode(), getHierarchicalCode() ... "
        + date_format.format(timestamp));

    cr.setHierarchicalCode("TOP");
    if (cr.getHierarchicalCode().equals("TOP"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test ContextRelationship: setParentTreenum(), getParentTreenum() ... "
        + date_format.format(timestamp));

    ContextPath cp = new ContextPath.Default();
    cr.setParentTreenum(cp);
    if (cr.getParentTreenum().equals(cp))
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
    addToLog("Finished TestSuiteContextRelationship at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}