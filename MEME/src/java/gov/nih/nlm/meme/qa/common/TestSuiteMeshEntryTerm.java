/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteMeshEntryTerm
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for MeshEntryTerm
 */
public class TestSuiteMeshEntryTerm extends TestSuite {

  public TestSuiteMeshEntryTerm() {
    setName("TestSuiteMeshEntryTerm");
    setDescription("Test Suite for MeshEntryTerm");
  }

  /**
   * Perform Test Suite MeshEntryTerm
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MeshEntryTerm met = new MeshEntryTerm();

    addToLog("    1. Test MeshEntryTerm: setMainHeading(), getMainHeading() ... "
             + date_format.format(timestamp));

    Atom atom = new Atom.Default(12345);
    met.setMainHeading(atom);
    if (met.getMainHeading().equals(atom))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
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
    addToLog("Finished TestSuiteMeshEntryTerm at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}