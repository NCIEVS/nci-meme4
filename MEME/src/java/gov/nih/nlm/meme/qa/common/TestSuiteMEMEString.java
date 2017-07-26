/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteMEMEString
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.MEMEString;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.common.Language;

/**
 * Test suite for MEMEString
 */
public class TestSuiteMEMEString extends TestSuite {

  public TestSuiteMEMEString() {
    setName("TestSuiteMEMEString");
    setDescription("Test Suite for MEMEString");
  }

  /**
   * Perform Test Suite MEMEString
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MEMEString meme_string = new MEMEString.Default();

    addToLog("    1. Test MEMEString: setSUI(), getSUI() ... "
             + date_format.format(timestamp));

    StringIdentifier sui = new StringIdentifier("S0001136");
    meme_string.setSUI(sui);
    if (meme_string.getSUI().equals(sui))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test MEMEString: setLUI(), getLUI() ... "
             + date_format.format(timestamp));

    StringIdentifier lui = new StringIdentifier("L0001046");
    meme_string.setLUI(lui);
    if (meme_string.getLUI().equals(lui))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test MEMEString: setISUI(), getISUI() ... "
             + date_format.format(timestamp));

    StringIdentifier isui = new StringIdentifier("S0001136");
    meme_string.setISUI(isui);
    if (meme_string.getISUI().equals(isui))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4. Test MEMEString: setString(), getString() ... "
             + date_format.format(timestamp));

    meme_string.setString("Test String");
    if (meme_string.getString().equals("Test String"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog("    5. Test MEMEString: setNormalizedString(), getNormalizedString() ... "
             + date_format.format(timestamp));

    meme_string.setNormalizedString("Test Norm String");
    if (meme_string.getNormalizedString().equals("Test Norm String"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog("    6. Test MEMEString: setString(), getString() ... "
             + date_format.format(timestamp));

    meme_string.setString("This is the base_string <32872383>");
    if (meme_string.getBaseString().equals("This is the base_string"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog("    7. Test MEMEString: getBracketNumber() ... "
             + date_format.format(timestamp));

    if (meme_string.getBracketNumber() == 32872383)
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog("    8.1. Test MEMEString: isBaseString() ... "
             + date_format.format(timestamp));

    if (!(meme_string.isBaseString()))
      addToLog("    8.1. Test Passed");
    else {
      addToLog("    8.1. Test Failed");
      thisTestFailed();
    }

    meme_string.setString("This is the base_string");
    if (meme_string.isBaseString())
      addToLog("    8.2. Test Passed");
    else {
      addToLog("    8.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    9.1. Test MEMEString: isBracketString() ... "
             + date_format.format(timestamp));

    if (!(meme_string.isBracketString()))
      addToLog("    9.1. Test Passed");
    else {
      addToLog("    9.1. Test Failed");
      thisTestFailed();
    }

    meme_string.setString(" <32872383>");
    if (meme_string.isBracketString())
      addToLog("    9.2. Test Passed");
    else {
      addToLog("    9.2. Test Failed");
      thisTestFailed();
    }

    Language language = new Language.Default("English", "ENG");
    meme_string.setLanguage(language);
    if (meme_string.getLanguage().getAbbreviation().equals("ENG"))
      addToLog("    10. Test Passed");
    else {
      addToLog("    10. Test Failed");
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
    addToLog("Finished TestSuiteMEMEString at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}