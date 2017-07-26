/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteParameter
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Parameter
 */
public class TestSuiteParameter extends TestSuite {

  public TestSuiteParameter() {
    setName("TestSuiteParameter");
    setDescription("Test Suite for Parameter");
  }

  /**
   * Perform Test Suite Parameter
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Parameter parameter = new Parameter.Default("PARAM1", "OBJECT1", true);

    addToLog("    1.1. Test Parameter: getName() ... "
             + date_format.format(timestamp));

    if (parameter.getName().equals("PARAM1"))
      addToLog("    1.1. Test Passed");
    else {
      addToLog("    1.1. Test Failed");
      thisTestFailed();
    }

    addToLog("    1.2. Test Parameter: setName(), getName() ... "
             + date_format.format(timestamp));

    parameter.setName("PARAM2");
    if (!parameter.getName().equals("PARAM1"))
      addToLog("    1.2. Test Passed");
    else {
      addToLog("    1.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.1. Test Parameter: getValue() ... "
             + date_format.format(timestamp));

    if (parameter.getValue().toString().equals("OBJECT1"))
      addToLog("    2.1. Test Passed");
    else {
      addToLog("    2.1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.2. Test Parameter: setValue(), getValue() ... "
             + date_format.format(timestamp));

    parameter.setValue("OBJECT2");
    if (!parameter.getValue().toString().equals("OBJECT1"))
      addToLog("    2.2. Test Passed");
    else {
      addToLog("    2.2. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.3. Test Parameter: setValue(), getBoolean() ... "
             + date_format.format(timestamp));

    parameter.setValue(true);
    if (parameter.getBoolean())
      addToLog("    2.3. Test Passed");
    else {
      addToLog("    2.3. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.4. Test Parameter: setValue(), getByte() ... "
             + date_format.format(timestamp));

    byte b = 'B';
    parameter.setValue(b);
    if (parameter.getByte() == b)
      addToLog("    2.4. Test Passed");
    else {
      addToLog("    2.4. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.5. Test Parameter: setValue(), getChar() ... "
             + date_format.format(timestamp));

    parameter.setValue('C');
    if (parameter.getChar() == 'C')
      addToLog("    2.5. Test Passed");
    else {
      addToLog("    2.5. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.6. Test Parameter: setValue(), getDouble() ... "
             + date_format.format(timestamp));

    double d = 2.0;
    parameter.setValue(d);
    if (parameter.getDouble() == d)
      addToLog("    2.6. Test Passed");
    else {
      addToLog("    2.6. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.7. Test Parameter: setValue(), getFloat() ... "
             + date_format.format(timestamp));

    float f = 1;
    parameter.setValue(f);
    if (parameter.getFloat() == f)
      addToLog("    2.7. Test Passed");
    else {
      addToLog("    2.7. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.8. Test Parameter: setValue(), getInt() ... "
             + date_format.format(timestamp));

    int i = 100;
    parameter.setValue(i);
    if (parameter.getInt() == i)
      addToLog("    2.8. Test Passed");
    else {
      addToLog("    2.8. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.9. Test Parameter: setValue(), getLong() ... "
             + date_format.format(timestamp));

    long l = 100;
    parameter.setValue(l);
    if (parameter.getLong() == l)
      addToLog("    2.9. Test Passed");
    else {
      addToLog("    2.9. Test Failed");
      thisTestFailed();
    }

    addToLog("    2.10. Test Parameter: setValue(), getShort() ... "
             + date_format.format(timestamp));

    short s = 1;
    parameter.setValue(s);
    if (parameter.getShort() == s)
      addToLog("    2.10. Test Passed");
    else {
      addToLog("    2.10. Test Failed");
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
    addToLog("Finished TestSuiteParameter at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}