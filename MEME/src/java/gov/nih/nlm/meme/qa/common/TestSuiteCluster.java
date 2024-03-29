/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteCluster
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Cluster;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Cluster
 */
public class TestSuiteCluster extends TestSuite {

  public TestSuiteCluster() {
    setName("TestSuiteCluster");
    setDescription("Test Suite for Cluster");
  }

  /**
   * Perform Test Suite Cluster
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Cluster cluster = new Cluster();


    addToLog(
        "    1. Test Cluster: setIdentifier(Identifier), getIdentifier() ... "
        + date_format.format(timestamp));

    Identifier identifier = new Identifier.Default("123456");
    cluster.setIdentifier(identifier);
    if (cluster.getIdentifier().equals(identifier))
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
    addToLog("Finished TestSuiteCluster at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}