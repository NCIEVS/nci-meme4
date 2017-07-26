/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuitePasswordAuthenticator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.PasswordAuthenticator;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for PasswordAuthenticator
 */
public class TestSuitePasswordAuthenticator extends TestSuite {

  public TestSuitePasswordAuthenticator() {
    setName("TestSuitePasswordAuthenticator");
    setDescription("Test Suite for PasswordAuthenticator");
  }

  /**
   * Perform Test Suite PasswordAuthenticator
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    PasswordAuthenticator pa = new PasswordAuthenticator();

    addToLog("    1. Test PasswordAuthenticator: setUsernameAndPassword(), getUsername() ... "
             + date_format.format(timestamp));

    char[] passwd = {'u','s','e','r','p','a','s','s'};
    pa.setUsernameAndPassword("user1", passwd);
    if (pa.getUsername().equals("user1"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test PasswordAuthenticator: getPassword() ... "
             + date_format.format(timestamp));

    if (pa.getPassword().equals(passwd))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test PasswordAuthenticator: clearPassword() ... "
             + date_format.format(timestamp));

    pa.clearPassword();
    if (pa.getPassword() == null)
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
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
    addToLog("Finished TestSuitePasswordAuthenticator at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}