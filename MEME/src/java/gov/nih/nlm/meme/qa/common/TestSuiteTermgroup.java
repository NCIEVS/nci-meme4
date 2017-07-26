/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteTermgroup
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.Rank;

/**
 * Test suite for Source
 */
public class TestSuiteTermgroup extends TestSuite {

  public TestSuiteTermgroup() {
    setName("TestSuiteTermgroup");
    setDescription("Test Suite for Atom");
  }

  /**
   * Perform Test Suite Atom
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Termgroup termgroup = new Termgroup.Default();

    addToLog(
        "    1. Test Termgroup: setSource(), getSource() ... "
        + date_format.format(timestamp));

    Source source = new Source.Default("MTH");
    termgroup.setSource(source);
    if (termgroup.getSource().equals(source))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test Termgroup: setTermType(), getTermType() ... "
        + date_format.format(timestamp));

    termgroup.setTermType("TERMTYPE");
    if (termgroup.getTermType().equals("TERMTYPE"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test Termgroup: setSuppressible(), isSuppressible() ... "
        + date_format.format(timestamp));

    termgroup.setSuppressible("E");
    if (termgroup.isSuppressible())
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test Termgroup: setExclude(), exclude() ... "
        + date_format.format(timestamp));

    termgroup.setExclude(true);
    if (termgroup.exclude())
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test Termgroup: setNormExclude(), normExclude() ... "
        + date_format.format(timestamp));

    termgroup.setNormExclude(true);
    if (termgroup.normExclude())
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test Termgroup: setTermgroupToOutrank(), getTermgroupToOutrank() ... "
        + date_format.format(timestamp));

    termgroup.setTermgroupToOutrank(termgroup);
    if (termgroup.getTermgroupToOutrank().equals(termgroup))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test Termgroup: setReleaseRank(), getReleaseRank() ... "
        + date_format.format(timestamp));

    Rank rank = new Rank.Default("1234");
    termgroup.setReleaseRank(rank);
    if (termgroup.getReleaseRank().equals(rank))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test Termgroup: setNotes(), getNotes() ... "
        + date_format.format(timestamp));

    termgroup.setNotes("NOTES");
    if (termgroup.getNotes().equals("NOTES"))
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
    addToLog("Finished TestSuiteTermgroup at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}