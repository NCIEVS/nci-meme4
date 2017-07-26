/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteMetaProperty
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for MetaProperty
 */
public class TestSuiteMetaProperty extends TestSuite {

  public TestSuiteMetaProperty() {
    setName("TestSuiteMetaProperty");
    setDescription("Test Suite for MetaProperty");
  }

  /**
   * Perform Test Suite MetaProperty
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MetaProperty mp = new MetaProperty();

    addToLog(
        "    1. Test MetaProperty: setIdentifier(Identifier), getIdentifier() ... " +
        date_format.format(timestamp));

    Identifier id = new Identifier.Default(123456);
    mp.setIdentifier(id);
    if (mp.getIdentifier().equals(id))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test MetaProperty: setKey(String), getKey() ... " +
        date_format.format(timestamp));

    mp.setKey("KEY");
    if (mp.getKey().equals("KEY"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test MetaProperty: setKeyQualifier(String), getKeyQualifier() ... " +
        date_format.format(timestamp));

    mp.setKeyQualifier("KEY_QUALIFIER");
    if (mp.getKeyQualifier().equals("KEY_QUALIFIER"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test MetaProperty: setValue(String), getValue() ... " +
        date_format.format(timestamp));

    mp.setValue("VALUE");
    if (mp.getValue().equals("VALUE"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test MetaProperty: setDescription(String), getDescription() ... " +
        date_format.format(timestamp));

    mp.setDescription("DESCRIPTION");
    if (mp.getDescription().equals("DESCRIPTION"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test MetaProperty: setDefinition(String), getDefinition() ... " +
        date_format.format(timestamp));

    mp.setDefinition("DEFINITION");
    if (mp.getDefinition().equals("DEFINITION"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test MetaProperty: setExample(String), getExample() ... " +
        date_format.format(timestamp));

    mp.setExample("EXAMPLE");
    if (mp.getExample().equals("EXAMPLE"))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test MetaProperty: setReference(String), getReference() ... " +
        date_format.format(timestamp));

    mp.setReference("REFERENCE");
    if (mp.getReference().equals("REFERENCE"))
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
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
    addToLog("Finished TestSuiteMetaProperty at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}