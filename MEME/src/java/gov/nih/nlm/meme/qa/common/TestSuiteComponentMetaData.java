/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteComponentMetaData
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.ComponentMetaData;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for ComponentMetaData
 */
public class TestSuiteComponentMetaData extends TestSuite {

  public TestSuiteComponentMetaData() {
    setName("TestSuiteComponentMetaData");
    setDescription("Test Suite for ComponentMetaData");
  }

  /**
   * Perform Test Suite ComponentMetaData
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ComponentMetaData cmd = new ComponentMetaData("",1,1.0);

    addToLog(
        "    1. Test ComponentMetaData: setName(), getName() ... "
        + date_format.format(timestamp));

    cmd.setName("MEMERelaEditor");
    if (cmd.getName().equals("MEMERelaEditor"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test ComponentMetaData: setRelease(), getRelease() ... "
        + date_format.format(timestamp));

    cmd.setRelease(2);
    if (cmd.getRelease() == 2)
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test ComponentMetaData: setVersion(), getVersion() ... "
        + date_format.format(timestamp));

    cmd.setVersion(3.0);
    if (cmd.getVersion() == 3.0)
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test ComponentMetaData: setServer(), getServer() ... "
        + date_format.format(timestamp));

    cmd.setServer("MEME4");
    if (cmd.getServer().equals("MEME4"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test ComponentMetaData: setUsername(), getUsername() ... "
        + date_format.format(timestamp));

    cmd.setUsername("user1");
    if (cmd.getUsername().equals("user1"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test ComponentMetaData: setDirectory(), getDirectory() ... "
        + date_format.format(timestamp));

    cmd.setDirectory("$MEME_HOME/Release");
    if (cmd.getDirectory().equals("$MEME_HOME/Release"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test ComponentMetaData: setFile(), getFile() ... "
        + date_format.format(timestamp));

    cmd.setFile("xml.jar");
    if (cmd.getFile().equals("xml.jar"))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test ComponentMetaData: setPassword(), getPassword() ... "
        + date_format.format(timestamp));

    char[] password = {'m', 'y', 'p', 'a', 's', 's'};
    cmd.setPassword(password);
    char[] passwd = cmd.getPassword();

    if (passwd.equals(password))
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    9. Test ComponentMetaData: clearPassword() ... "
        + date_format.format(timestamp));

    cmd.clearPassword();
    if (passwd != null)
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
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
    addToLog("Finished TestSuiteComponentMetaData at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}