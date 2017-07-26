/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteEditorPreferences
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.Authority;

/**
 * Test suite for EditorPreferences
 */
public class TestSuiteEditorPreferences extends TestSuite {

  public TestSuiteEditorPreferences() {
    setName("TestSuiteEditorPreferences");
    setDescription("Test Suite for EditorPreferences");
  }

  /**
   * Perform Test Suite EditorPreferences
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    EditorPreferences ef = new EditorPreferences.Default();

    addToLog("    1. Test EditorPreferences: setInitials(), getInitials() ... "
             + date_format.format(timestamp));

    ef.setInitials("FYI");
    if (ef.getInitials().equals("FYI"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog("    2. Test EditorPreferences: setUserName(), getUserName() ... "
             + date_format.format(timestamp));

    ef.setUserName("User1");
    if (ef.getUserName().equals("User1"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog("    3. Test EditorPreferences: setEditorLevel(), getEditorLevel() ... "
             + date_format.format(timestamp));

    ef.setEditorLevel(1);
    if (ef.getEditorLevel() == 1)
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog("    4. Test EditorPreferences: setIsCurrent(), isCurrent() ... "
             + date_format.format(timestamp));

    ef.setIsCurrent(true);
    if (ef.isCurrent())
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog("    5. Test EditorPreferences: setEditorGroup(), getEditorGroup() ... "
             + date_format.format(timestamp));

    ef.setEditorGroup("E-GROUP");
    if (ef.getEditorGroup().equals("E-GROUP"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog("    6. Test EditorPreferences: setAuthority(), getAuthority() ... "
             + date_format.format(timestamp));

    Authority author = new Authority.Default("KEVRIC");
    ef.setAuthority(author);
    if (ef.getAuthority().equals(author))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog("    7. Test EditorPreferences: setShowConcept(), getShowConcept() ... "
             + date_format.format(timestamp));

    ef.setShowConcept(true);
    if (ef.showConcept())
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog("    8. Test EditorPreferences: setShowAtoms(), getShowAtoms() ... "
             + date_format.format(timestamp));

    ef.setShowAtoms(true);
    if (ef.showAtoms())
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog("    9. Test EditorPreferences: setShowAttributes(), getShowAttributes() ... "
             + date_format.format(timestamp));

    ef.setShowAttributes(true);
    if (ef.showAttributes())
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
      thisTestFailed();
    }

    addToLog("    10. Test EditorPreferences: setShowRelationships(), getShowRelationships() ... "
             + date_format.format(timestamp));

    ef.setShowRelationships(true);
    if (ef.showRelationships())
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
    addToLog("Finished TestSuiteEditorPreferences at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}