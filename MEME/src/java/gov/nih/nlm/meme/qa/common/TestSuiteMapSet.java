/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteMapSet
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for MapSet
 */
public class TestSuiteMapSet extends TestSuite {

  public TestSuiteMapSet() {
    setName("TestSuiteMapSet");
    setDescription("Test Suite for MapSet");
  }

  /**
   * Perform Test Suite MapSet
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MapSet ms = new MapSet.Default();

    addToLog(
        "    1. Test MapSet: setFromSource(Source), getFromSource() ... " +
        date_format.format(timestamp));

    Source source = new Source.Default("MTH");

    ms.setFromSource(source);
    if (ms.getFromSource().equals(source))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test MapSet: setToSource(Source), getToSource() ... " +
        date_format.format(timestamp));

    ms.setToSource(source);
    if (ms.getToSource().equals(source))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test MapSet: setMapSetSource(Source), getMapSetSource() ... " +
        date_format.format(timestamp));

    ms.setMapSetSource(source);
    if (ms.getMapSetSource().equals(source))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4. getType() = " + ms.getType());
    addToLog("    5. getMapSetIdentifier() = " + ms.getMapSetIdentifier());
    addToLog("    6. getName() = " + ms.getName());
    addToLog("    7. isFromExhaustive() = " + ms.isFromExhaustive());
    addToLog("    8. isToExhaustive() = " + ms.isToExhaustive());
    addToLog("    9. getFromComplexity() = " + ms.getFromComplexity());
    addToLog("    10. getToComplexity() = " + ms.getToComplexity());
    addToLog("    11. getMapSetComplexity() = " + ms.getMapSetComplexity());
    addToLog("    12. getDescription() = " + ms.getDescription());

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
    addToLog("Finished TestSuiteMapSet at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}