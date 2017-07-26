/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteSafeReplacementFact
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.SafeReplacementFact;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for SafeReplacementFact
 */
public class TestSuiteSafeReplacementFact extends TestSuite {

  public TestSuiteSafeReplacementFact() {
    setName("TestSuiteSafeReplacementFact");
    setDescription("Test Suite for SafeReplacementFact");
  }

  /**
   * Perform Test Suite SafeReplacementFact
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    SafeReplacementFact sr_fact = new SafeReplacementFact();

    addToLog(
        "    1. Test SafeReplacementFact: setOldAtom(Atom), getOldAtom() ... " +
        date_format.format(timestamp));

    Atom atom = new Atom.Default(1234);
    atom.setTermgroup(new Termgroup.Default("MSH2000/PM"));
    atom.setCode(new Code("CODE1"));
    atom.setLastReleaseCUI(new CUI("C0000001"));
    atom.setLastAssignedCUI(new CUI("C0000002"));
    atom.setConcept(new Concept.Default(1070));
    atom.setLanguage(new Language.Default("English","ENG"));
    atom.setAUI(new AUI("A0000001"));

    sr_fact.setOldAtom(atom);
    if (sr_fact.getOldAtom().equals(atom))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test SafeReplacementFact: setNewAtom(Atom), getNewAtom() ... " +
        date_format.format(timestamp));

    sr_fact.setNewAtom(atom);
    if (sr_fact.getNewAtom().equals(atom))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
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
    addToLog("Finished TestSuiteSafeReplacementFact at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}