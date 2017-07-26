/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteMetaCode
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MetaCode;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for MetaCode
 */
public class TestSuiteMetaCode extends TestSuite {

  public TestSuiteMetaCode() {
    setName("TestSuiteMetaCode");
    setDescription("Test Suite for MetaCode");
  }

  /**
   * Perform Test Suite MetaCode
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MetaCode mc = new MetaCode();

    addToLog(
        "    1. Test MetaCode: setIdentifier(Identifier), getIdentifier() ... " +
        date_format.format(timestamp));

    Identifier id = new Identifier.Default(123456);
    mc.setIdentifier(id);
    if (mc.getIdentifier().equals(id))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test MetaCode: setCode(String), getCode() ... " +
        date_format.format(timestamp));

    mc.setCode("CODE");
    if (mc.getCode().equals("CODE"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test MetaCode: setType(String), getType() ... " +
        date_format.format(timestamp));

    mc.setType("TYPE");
    if (mc.getType().equals("TYPE"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test MetaCode: setValue(String), getValue() ... " +
        date_format.format(timestamp));

    mc.setValue("VALUE");
    if (mc.getValue().equals("VALUE"))
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
    addToLog("Finished TestSuiteMetaCode at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}