/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteMergedFact
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MergeFact;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for MergedFact
 */
public class TestSuiteMergedFact extends TestSuite {

  public TestSuiteMergedFact() {
    setName("TestSuiteMergedFact");
    setDescription("Test Suite for MergedFact");
  }

  /**
   * Perform Test Suite MergedFact
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MergeFact mf = new MergeFact();

    addToLog(
        "    1. Test MergedFact: setIdentifier(Identifier), " +
        " getIdentifier() ... " +
        date_format.format(timestamp));

    Identifier id = new Identifier.Default(123456);
    mf.setIdentifier(id);
    if (mf.getIdentifier().equals(id))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test MergedFact: setIsSynonym(boolean), getSynonym() ... " +
        date_format.format(timestamp));

    mf.setIsSynonym(true);
    if (mf.isSynonym())
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test MergedFact: setIsExactMatch(boolean), isExactMatch() ... " +
        date_format.format(timestamp));

    mf.setIsExactMatch(true);
    if (mf.isExactMatch())
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test MergedFact: setIsNormMatch(boolean), isNormMatch() ... " +
        date_format.format(timestamp));

    mf.setIsNormMatch(true);
    if (mf.isNormMatch())
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test MergedFact: setIntegrityVector(EnforcableIntegrityVector), " +
        " getIntegrityVector() ... " +
        date_format.format(timestamp));

    mf.setIntegrityVector(new EnforcableIntegrityVector());
    if (mf.getIntegrityVector().equals(new EnforcableIntegrityVector()))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test MergedFact: setViolationsVector(ViolationsVector), " +
        " getViolationsVector() ... " +
        date_format.format(timestamp));

    mf.setViolationsVector(new ViolationsVector());
    if (mf.getViolationsVector().equals(new ViolationsVector()))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test MergedFact: setDemoteIfMergeFails(boolean), demoteIfMergeFails() ... " +
        date_format.format(timestamp));

    mf.setDemoteIfMergeFails(true);
    if (mf.demoteIfMergeFails())
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test MergedFact: setChangeStatus(boolean), getChangeStatus() ... " +
        date_format.format(timestamp));

    mf.setChangeStatus(true);
    if (mf.getChangeStatus())
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    9. Test MergedFact: setName(String), getName() ... " +
        date_format.format(timestamp));

    mf.setName("MERGE_SET");
    if (mf.getName().equals("MERGE_SET"))
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    10. Test MergedFact: setStatus(String), getStatus(), " +
        " mergeSucceeded ... " +
        date_format.format(timestamp));

    mf.setStatus("M");
    if (mf.getStatus().equals("M"))
      addToLog("    10. Test Passed");
    else {
      addToLog("    10. Test Failed");
      thisTestFailed();
    }

    if (mf.mergeSucceeded())
      addToLog("    10.1 Test Passed");
    else {
      addToLog("    10.1 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    11. Test MergedFact: setStatus(String), mergeFailed ... " +
        date_format.format(timestamp));

    mf.setStatus("D");
    if (mf.mergeFailed())
      addToLog("    11. Test Passed");
    else {
      addToLog("    11. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    12. Test MergedFact: setAction(MolecularAction), " +
        " getAction() ... " +
        date_format.format(timestamp));

    MolecularAction ma = new MolecularAction(12345);
    mf.setAction(ma);
    if (mf.getAction().equals(ma))
      addToLog("    12. Test Passed");
    else {
      addToLog("    12. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    13. Test MergedFact: setAuthority(Authority), " +
        " getAuthority() ... " +
        date_format.format(timestamp));

    mf.setAuthority(new Authority.Default("MTH"));
    if (mf.getAuthority().equals(new Authority.Default("MTH")))
      addToLog("    14. Test Passed");
    else {
      addToLog("    14. Test Failed");
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
    addToLog("Finished TestSuiteMergedFact at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}