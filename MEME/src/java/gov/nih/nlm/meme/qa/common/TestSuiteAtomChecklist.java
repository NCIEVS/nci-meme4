/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteAtomChecklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.AtomChecklist;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import gov.nih.nlm.meme.common.AUI;
import java.util.ArrayList;
import gov.nih.nlm.meme.common.AtomCluster;
import gov.nih.nlm.meme.common.Identifier;

/**
 * Test suite for AtomChecklist
 */
public class TestSuiteAtomChecklist extends TestSuite {

  public TestSuiteAtomChecklist() {
    setName("TestSuiteAtomChecklist");
    setDescription("Test Suite for AtomChecklist");
  }

  /**
   * Perform Test Suite AtomChecklist
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    AtomChecklist atom_checklist = new AtomChecklist();

    addToLog(
        "    1. Test AtomChecklist: add(Atom[]), getAtoms() ... "
        + date_format.format(timestamp));

    ArrayList list = new ArrayList();
    Atom atom = new Atom.Default();
    atom.setTermgroup(new Termgroup.Default("MSH2000/PM"));
    atom.setCode(new Code("CODE1"));
    atom.setLastReleaseCUI(new CUI("C0000001"));
    atom.setLastAssignedCUI(new CUI("C0000002"));
    atom.setConcept(new Concept.Default(1070));
    atom.setLanguage(new Language.Default("English","ENG"));
    atom.setAUI(new AUI("A0000001"));
    list.add(atom);

    atom_checklist.add((Atom[]) list.toArray(new Atom[] {}));
    Atom[] atoms = atom_checklist.getAtoms();
    if (atoms.length > 0)
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test AtomChecklist: add(Object), getAtoms() ... "
        + date_format.format(timestamp));

    atom_checklist.add(atom);
    atoms = atom_checklist.getAtoms();
    if (atoms.length > 0)
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test AtomChecklist: addCluster(Cluster), getAtoms() ... "
        + date_format.format(timestamp));

    AtomCluster cluster = new AtomCluster(new Identifier.Default(12345));
    atom_checklist.addCluster(cluster);
    atoms = atom_checklist.getAtoms();
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
    addToLog("Finished TestSuiteAtomChecklist at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}