/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteLanguage
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Language
 */
public class TestSuiteLanguage extends TestSuite {

  public TestSuiteLanguage() {
    setName("TestSuiteLanguage");
    setDescription("Test Suite for Language");
  }

  /**
   * Perform Test Suite Language
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Language language = new Language.Default("ENGLISH", "ENG");

    addToLog("    1. Test Language: getAbbreviation() ... "
             + date_format.format(timestamp));
    if (language.getAbbreviation().equals("ENG"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test Language: setISOAbbreviation(), getISOAbbreviation() ... "
             + date_format.format(timestamp));
    language.setISOAbbreviation("en");
    if (language.getISOAbbreviation().equals("en"))
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
    addToLog("Finished TestSuiteLanguage at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}