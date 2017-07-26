/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteContextPath
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.ContextPath;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Test suite for ContextPath
 */
public class TestSuiteContextPath extends TestSuite {

  public TestSuiteContextPath() {
    setName("TestSuiteContextPath");
    setDescription("Test Suite for ContextPath");
  }

  /**
   * Perform Test Suite ContextPath
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ContextPath cp = new ContextPath.Default();

    addToLog(
        "    1. Test ContextPath: addAtom(Atom), getPathRoot() ... " +
        date_format.format(timestamp));

    Atom atom = new Atom.Default();
    atom.setIdentifier(new Identifier.Default(2223));
    cp.addAtom(atom);

    List atoms = cp.getPathToRoot();
    if (atoms.size() > 0)
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test ContextPath: removeAtom(Atom), getPathRoot() ... " +
        date_format.format(timestamp));

    cp.removeAtom(atom);
    if (atoms.size() == 0)
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    cp.clearAtoms();

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
    addToLog("Finished TestSuiteContextPath at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}