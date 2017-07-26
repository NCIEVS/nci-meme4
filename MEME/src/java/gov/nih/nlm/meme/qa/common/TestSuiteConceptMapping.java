/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteConceptMapping
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.ConceptMapping;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for ConceptMapping
 */
public class TestSuiteConceptMapping extends TestSuite {

  public TestSuiteConceptMapping() {
    setName("TestSuiteConceptMapping");
    setDescription("Test Suite for ConceptMapping");
  }

  /**
   * Perform Test Suite ConceptMapping
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ConceptMapping cm = new ConceptMapping.Default();

    addToLog(
        "    1. Test ConceptMapping: setBirthVersion(String), " +
        " getBirthVersion() ... " +
        date_format.format(timestamp));

    cm.setBirthVersion("1.0");

    if (cm.getBirthVersion().equals("1.0"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test ConceptMapping: setDeathVersion(String), " +
        " getDeathVersion() ... " +
        date_format.format(timestamp));

    cm.setDeathVersion("1.0");

    if (cm.getDeathVersion().equals("1.0"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test ConceptMapping: setMappingReason(String), " +
        " getMappingReason() ... " +
        date_format.format(timestamp));

    cm.setMappingReason("MAPPING_REASON");

    if (cm.getMappingReason().equals("MAPPING_REASON"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test ConceptMapping: setRelationshipName(String), " +
        "getRelationshipName() ... " +
        date_format.format(timestamp));

    cm.setRelationshipName("REL_NAME");

    if (cm.getRelationshipName().equals("REL_NAME"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test ConceptMapping: setRelationshipName(String), " +
        "isBequeathalMapping() ... " +
        date_format.format(timestamp));

    cm.setRelationshipName("DEL");

    if (cm.isBequeathalMapping())
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test ConceptMapping: isDeletedMapping() ... "
        + date_format.format(timestamp));

    if (cm.isDeletedMapping())
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7.1 Test ConceptMapping: setRelationshipName(String), " +
        "setMappedToCUI(CUI), isSynonymousMapping() ... " +
        date_format.format(timestamp));

    cm.setRelationshipName("SY");
    cm.setMappedToCui(new CUI("C0000001"));

    if (cm.isSynonymousMapping())
      addToLog("    7.1 Test Passed");
    else {
      addToLog("    7.1 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7.2 Test ConceptMapping: getMappedToCUI() ... " +
        date_format.format(timestamp));

    if (cm.getMappedToCui().equals(new CUI("C0000001")))
      addToLog("    7.2 Test Passed");
    else {
      addToLog("    7.2 Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test ConceptMapping: setRelationshipAttribute(String), " +
        "getRelationshipAttribute() ... " +
        date_format.format(timestamp));

    cm.setRelationshipAttribute("REL_ATTR");

    if (cm.getRelationshipAttribute().equals("REL_ATTR"))
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    9. Test ConceptMapping: setCUI(CUI), getCUI() ... " +
        date_format.format(timestamp));

    cm.setCUI(new CUI("C0000002"));

    if (cm.getCUI().equals(new CUI("C0000002")))
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    10. Test ConceptMapping: setAlmostSY(), isAlmostSY() ... " +
        date_format.format(timestamp));

    cm.setAlmostSY(true);

    if (cm.isAlmostSY())
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
    addToLog("Finished TestSuiteConceptMapping at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}