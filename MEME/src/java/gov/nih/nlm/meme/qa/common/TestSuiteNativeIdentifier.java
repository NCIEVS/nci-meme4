/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteNativeIdentifier
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for NativeIdentifier
 */
public class TestSuiteNativeIdentifier extends TestSuite {

  public TestSuiteNativeIdentifier() {
    setName("TestSuiteNativeIdentifier");
    setDescription("Test Suite for NativeIdentifier");
  }

  /**
   * Perform Test Suite NativeIdentifier
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    NativeIdentifier ni = new NativeIdentifier();

    addToLog(
        "    1. Test NativeIdentifier: setQualifier(String), getQualifier() ... " +
        date_format.format(timestamp));

    ni.setQualifier("QUALIFIER");
    if (ni.getQualifier().equals("QUALIFIER"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test NativeIdentifier: setType(String), getType() ... " +
        date_format.format(timestamp));

    ni.setType("TYPE");
    if (ni.getType().equals("TYPE"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test NativeIdentifier: setCoreDataIdentifier(String), getCoreDataIdentifier() ... " +
        date_format.format(timestamp));

    Identifier id = new Identifier.Default(123456);
    ni.setCoreDataIdentifier(id);
    if (ni.getCoreDataIdentifier().equals(id))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test NativeIdentifier: setCoreDataTypeCode(String), getCoreDataTypeCode() ... " +
        date_format.format(timestamp));

    ni.setCoreDataTypeCode("DATA_TYPE_CODE");
    if (ni.getCoreDataTypeCode().equals("DATA_TYPE_CODE"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test NativeIdentifier: setCoreDataElement(String), getCoreDataElement() ... " +
        date_format.format(timestamp));

    CoreData core_data = new CoreData.Default();
    core_data.setIdentifier(id);
    ni.setCoreDataElement(core_data);
    if (ni.getCoreDataElement().equals(core_data))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
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
    addToLog("Finished TestSuiteNativeIdentifier at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}