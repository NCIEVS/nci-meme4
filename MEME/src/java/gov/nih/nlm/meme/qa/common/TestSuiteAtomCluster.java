/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteAtomCluster
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.AtomCluster;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for AtomCluster
 */
public class TestSuiteAtomCluster extends TestSuite {

  public TestSuiteAtomCluster() {
    setName("TestSuiteAtomCluster");
    setDescription("Test Suite for AtomCluster");
  }

  /**
   * Perform Test Suite AtomCluster
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    AtomCluster atom_cluster = new AtomCluster();

    addToLog(
        "    1. Test AtomCluster: add(Atom), getAtoms() ... "
        + date_format.format(timestamp));

    Atom atom = new Atom.Default();
    atom.setTermgroup(new Termgroup.Default("MSH2000/PM"));
    atom.setCode(new Code("CODE1"));
    atom.setLastReleaseCUI(new CUI("C0000001"));
    atom.setLastAssignedCUI(new CUI("C0000002"));
    atom.setConcept(new Concept.Default(1070));
    atom.setLanguage(new Language.Default("English","ENG"));
    atom.setAUI(new AUI("A0000001"));

    atom_cluster.add(atom);
    Atom[] atoms = atom_cluster.getAtoms();
    if (atoms.length > 0)
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
    addToLog("Finished TestSuiteAtomCluster at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}