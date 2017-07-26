/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteAtomWorklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.AtomCluster;
import gov.nih.nlm.meme.common.AtomWorklist;
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
 * Test suite for AtomWorklist
 */
public class TestSuiteAtomWorklist extends TestSuite {

  public TestSuiteAtomWorklist() {
    setName("TestSuiteAtomWorklist");
    setDescription("Test Suite for AtomWorklist");
  }

  /**
   * Perform Test Suite AtomWorklist
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    AtomWorklist atom_worklist = new AtomWorklist();

    addToLog(
        "    1. Test AtomWorklist: add(Atom[]), getAtoms() ... "
        + date_format.format(timestamp));

    Atom[] atoms = new Atom[] { new Atom.Default(), new Atom.Default() };
    atoms[0] = new Atom.Default();
    atoms[0].setTermgroup(new Termgroup.Default("MSH2000/PM"));
    atoms[0].setCode(new Code("CODE1"));
    atoms[0].setLastReleaseCUI(new CUI("C0000001"));
    atoms[0].setLastAssignedCUI(new CUI("C0000002"));
    atoms[0].setConcept(new Concept.Default(1070));
    atoms[0].setLanguage(new Language.Default("English","ENG"));
    atoms[0].setAUI(new AUI("A0000001"));

    atoms[1] = new Atom.Default();
    atoms[1].setTermgroup(new Termgroup.Default("MSH2000/PN"));
    atoms[1].setCode(new Code("CODE2"));
    atoms[1].setLastReleaseCUI(new CUI("C0000003"));
    atoms[1].setLastAssignedCUI(new CUI("C0000004"));
    atoms[1].setConcept(new Concept.Default(1071));
    atoms[1].setLanguage(new Language.Default("English","ENG"));
    atoms[1].setAUI(new AUI("A0000002"));

    atom_worklist.add(atoms);
    atoms = atom_worklist.getAtoms();
    if (atoms.length > 0)
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }


    addToLog(
        "    2. Test AtomWorklist: add(Object), getAtoms() ... "
        + date_format.format(timestamp));

    Atom atom = new Atom.Default();
    atom.setTermgroup(new Termgroup.Default("MSH2000/PM"));
    atom.setCode(new Code("CODE1"));
    atom.setLastReleaseCUI(new CUI("C0000001"));
    atom.setLastAssignedCUI(new CUI("C0000002"));
    atom.setConcept(new Concept.Default(1070));
    atom.setLanguage(new Language.Default("English","ENG"));
    atom.setAUI(new AUI("A0000001"));

    atom_worklist.add(atom);
    atoms = atom_worklist.getAtoms();
    if (atoms.length > 0)
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test AtomWorklist: addCluster(AtomCluster), getAtoms() ... "
        + date_format.format(timestamp));

    AtomCluster atom_cluster = new AtomCluster();
    atom_cluster.add(atom);
    atom_worklist.addCluster(atom_cluster);
    atoms = atom_worklist.getAtoms();
    if (atoms.length > 0)
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
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
    addToLog("Finished TestSuiteAtomWorklist at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}