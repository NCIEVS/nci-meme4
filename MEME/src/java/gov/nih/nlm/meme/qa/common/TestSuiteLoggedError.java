/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteLoggedError
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.common.LoggedError;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Authority;

/**
 * Test suite for LoggedError
 */
public class TestSuiteLoggedError extends TestSuite {

  public TestSuiteLoggedError() {
    setName("TestSuiteLoggedError");
    setDescription("Test Suite for LoggedError");
  }

  /**
   * Perform Test Suite LoggedError
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    LoggedError le = new LoggedError.Default();

    addToLog(
        "    1. Test LoggedError: setTransaction(MolecularTransaction), " +
        " getTransaction() ... " +
        date_format.format(timestamp));

    MolecularTransaction transaction = new MolecularTransaction(123456);
    le.setTransaction(transaction);
    if (le.getTransaction().equals(transaction))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test LoggedError: setWorkLog(WorkLog), getWorkLog() ... " +
        date_format.format(timestamp));

    WorkLog worklog = new WorkLog(123456);
    le.setWorkLog(worklog);
    if (le.getWorkLog().equals(worklog))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test LoggedError: setTimestamp(Date), getTimestamp() ... " +
        date_format.format(timestamp));

    java.util.Date date = new java.util.Date();
    le.setTimestamp(date);
    if (le.getTimestamp().equals(date))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test LoggedError: setElapsedTime(long), getElapsedTime() ... " +
        date_format.format(timestamp));

    long elapsed_time = 10;
    le.setElapsedTime(elapsed_time);
    if (le.getElapsedTime() == elapsed_time)
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test LoggedError: setAuthority(Authority), getAuthority() ... " +
        date_format.format(timestamp));

    Authority authority = new Authority.Default("MTH");
    le.setAuthority(authority);
    if (le.getAuthority().equals(authority))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test LoggedError: setActivity(String), getActivity() ... " +
        date_format.format(timestamp));

    le.setActivity("ACTIVITY");
    if (le.getActivity().equals("ACTIVITY"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test LoggedError: setDetail(String), getDetail() ... " +
        date_format.format(timestamp));

    le.setDetail("DETAIL");
    if (le.getDetail().equals("DETAIL"))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    //
    // Main Footer
    //

    addToLog("");

    addToLog("-------------------------------------------------------");
    addToLog("Finished TestSuiteLoggedError at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}