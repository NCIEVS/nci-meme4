/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteChecklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Checklist;
import gov.nih.nlm.meme.common.Cluster;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Test suite for Checklist
 */
public class TestSuiteChecklist extends TestSuite {

  public TestSuiteChecklist() {
    setName("TestSuiteChecklist");
    setDescription("Test Suite for Checklist");
  }

  /**
   * Perform Test Suite Checklist
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Checklist checklist = new Checklist();

    addToLog(
        "    1. Test Checklist: setName(String), getName() ... "
        + date_format.format(timestamp));

    checklist.setName("CHK001");

    String name = checklist.getName();
    if (name.equals("CHK001"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test Checklist: setOwner(String), getOwner() ... "
        + date_format.format(timestamp));

    checklist.setOwner("MTH");

    String owner = checklist.getOwner();
    if (owner.equals("MTH"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test Checklist: setBinName(String), getBinName() ... "
        + date_format.format(timestamp));

    checklist.setBinName("QA_BIN");

    String bin_name = checklist.getBinName();
    if (bin_name.equals("QA_BIN"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test Checklist: setBinType(String), getBinType() ... "
        + date_format.format(timestamp));

    checklist.setBinType("QA");

    String bin_type = checklist.getBinType();
    if (bin_type.equals("QA"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4.1 Test Checklist: isQABin() ... "
        + date_format.format(timestamp));

    if (checklist.isQABin())
      addToLog("    4.1 Test Passed");
    else {
      addToLog("    4.1 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4.2 Test Checklist: setBinType(String), isMEBin() ... "
        + date_format.format(timestamp));

    checklist.setBinType("ME");

    if (checklist.isMEBin())
      addToLog("    4.2 Test Passed");
    else {
      addToLog("    4.2 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4.3 Test Checklist: setBinType(String), isAHBin() ... "
        + date_format.format(timestamp));

    checklist.setBinType("AH");

    if (checklist.isAHBin())
      addToLog("    4.3 Test Passed");
    else {
      addToLog("    4.3 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test Checklist: setCreationDate() ... "
        + date_format.format(timestamp));

    java.util.Date today = new java.util.Date();
    checklist.setCreationDate(today);

    if (checklist.getCreationDate().equals(today))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test Checklist: addCluster(Cluster), getClusters() ... "
        + date_format.format(timestamp));

    Cluster cluster = new Cluster();
    cluster.setIdentifier(new Identifier.Default("123456"));
    checklist.addCluster(cluster);

    List clusters = checklist.getClusters();
    if (clusters.size() > 0)
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6.1 Test Checklist: getClusterIterator() ... "
        + date_format.format(timestamp));

    Iterator iter = checklist.getClusterIterator();
    if (iter.hasNext())
      addToLog("    6.1 Test Passed");
    else {
      addToLog("    6.1 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6.2 Test Checklist: removeCluster(), getClusters() ... "
        + date_format.format(timestamp));

    checklist.removeCluster(cluster);

    clusters = checklist.getClusters();
    if (clusters.size() == 0)
      addToLog("    6.2 Test Passed");
    else {
      addToLog("    6.2 Test Failed");
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
    addToLog("Finished TestSuiteChecklist at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}