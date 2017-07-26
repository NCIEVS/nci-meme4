/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteReportStyle
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.ReportStyle;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for ReportStyle
 */
public class TestSuiteReportStyle extends TestSuite {

  public TestSuiteReportStyle() {
    setName("TestSuiteReportStyle");
    setDescription("Test Suite for ReportStyle");
  }

  /**
   * Perform Test Suite ReportStyle
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ReportStyle rs = new ReportStyle.Default();

    addToLog("    1. Test ReportStyle: setRegexp(), getRegexp() ... "
             + date_format.format(timestamp));

    rs.setRegexp("a*b");
    if (rs.getRegexp().equals("a*b"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test ReportStyle: setColor(), getColor() ... "
             + date_format.format(timestamp));

    rs.setColor("#00FB00");
    if (rs.getColor().equals("#00FB00"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test ReportStyle: setShade(), getShade() ... "
             + date_format.format(timestamp));

    double f = 1.0;
    rs.setShade(f);
    if (rs.getShade() == f)
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4. Test ReportStyle: setBold(), isBold() ... "
             + date_format.format(timestamp));

    rs.setBold(true);
    if (rs.isBold())
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog("    5. Test ReportStyle: setUnderline(), isUnderline() ... "
             + date_format.format(timestamp));

    rs.setUnderline(true);
    if (rs.isUnderline())
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog("    6. Test ReportStyle: setItalics(), isItalics() ... "
             + date_format.format(timestamp));

    rs.setItalics(true);
    if (rs.isItalics())
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog("    7. Test ReportStyle: setContentType(), getContentType() ... "
             + date_format.format(timestamp));

    rs.setContentType("text/enscript");
    if (rs.getContentType().equals("text/enscript"))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog("    8. Test ReportStyle: setSections(), getSections() ... "
             + date_format.format(timestamp));

    String[] sections = new String[] { "Section1", "Section2", "Section3" };
    rs.setSections(sections);
    if (rs.getSections().equals(sections))
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
    addToLog("Finished TestSuiteReportStyle at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}