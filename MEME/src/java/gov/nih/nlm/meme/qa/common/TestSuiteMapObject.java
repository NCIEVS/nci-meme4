/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteMapObject
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MapObject;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.Source;

/**
 * Test suite for MapObject
 */
public class TestSuiteMapObject extends TestSuite {

  public TestSuiteMapObject() {
    setName("TestSuiteMapObject");
    setDescription("Test Suite for MapObject");
  }

  /**
   * Perform Test Suite MapObject
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MapObject mo = new MapObject.Default();

    addToLog(
        "    1. Test MapObject: setMapObjectIdentifier(Identifier), getMapObjectIdentifier() ... " +
        date_format.format(timestamp));

    Identifier id = new Identifier.Default("123456");

    mo.setMapObjectIdentifier(id);
    if (mo.getMapObjectIdentifier().equals(id))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test MapObject: setMapObjectSourceIdentifier(Identifier), getMapObjectSourceIdentifier() ... " +
        date_format.format(timestamp));

    id = new Identifier.Default("123457");

    mo.setMapObjectSourceIdentifier(id);
    if (mo.getMapObjectSourceIdentifier().equals(id))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test MapObject: setExpression(String), getExpression() ... " +
        date_format.format(timestamp));

    mo.setExpression("EXPRESSION");
    if (mo.getExpression().equals("EXPRESSION"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test MapObject: setType(String), getType() ... " +
        date_format.format(timestamp));

    mo.setType("TYPE");
    if (mo.getType().equals("TYPE"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test MapObject: setRule(String), getRule() ... " +
        date_format.format(timestamp));

    mo.setRule("RULE");
    if (mo.getRule().equals("RULE"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test MapObject: setRestriction(String), getRestriction() ... " +
        date_format.format(timestamp));

    mo.setRestriction("RESTRICTION");
    if (mo.getRestriction().equals("RESTRICTION"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test MapObject: setMapObjectSource(String), getMapObjectSource() ... " +
        date_format.format(timestamp));

    Source source = new Source.Default("MTH");
    mo.setMapObjectSource(source);
    if (mo.getMapObjectSource().equals(source))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test MapObject: setMapObjectSource(String), getMapObjectSource() ... " +
        date_format.format(timestamp));

    mo.setValue("<>Long_Attribute<>:");
    if (mo.getValue().equals("<>Long_Attribute<>:"))
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
    addToLog("Finished TestSuiteMapObject at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}