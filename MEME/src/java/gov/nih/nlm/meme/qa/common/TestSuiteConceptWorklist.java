/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteConceptWorklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptCluster;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for ConceptWorklist
 */
public class TestSuiteConceptWorklist extends TestSuite {

  public TestSuiteConceptWorklist() {
    setName("TestSuiteConceptWorklist");
    setDescription("Test Suite for ConceptWorklist");
  }

  /**
   * Perform Test Suite ConceptWorklist
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ConceptWorklist concept_worklist = new ConceptWorklist();

    addToLog(
        "    1. Test ConceptWorklist: add(Atom[]), getAtoms() ... "
        + date_format.format(timestamp));

    Concept[] concepts = new Concept[] { new Concept.Default(), new Concept.Default() };
    concepts[0] = new Concept.Default();
    concepts[0].setCUI(new CUI("C0000001"));
    concepts[0].setPreferredAtom(new Atom.Default(1070));
    concepts[0].setEditingAuthority(new Authority.Default("KEVRIC"));
    concepts[0].setEditingTimestamp(new java.util.Date());
    concepts[0].setReadTimestamp(new java.util.Date());

    concepts[1] = new Concept.Default();
    concepts[1].setCUI(new CUI("C0000002"));
    concepts[1].setPreferredAtom(new Atom.Default(1071));
    concepts[1].setEditingAuthority(new Authority.Default("KEVRIC"));
    concepts[1].setEditingTimestamp(new java.util.Date());
    concepts[1].setReadTimestamp(new java.util.Date());

    concept_worklist.add(concepts);
    concepts = concept_worklist.getConcepts();
    if (concepts.length > 0)
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }


    addToLog(
        "    2. Test ConceptWorklist: add(Object), getConcepts() ... "
        + date_format.format(timestamp));

    Concept concept = new Concept.Default();
    concept = new Concept.Default();
    concept.setCUI(new CUI("C0000001"));
    concept.setPreferredAtom(new Atom.Default(1070));
    concept.setEditingAuthority(new Authority.Default("KEVRIC"));
    concept.setEditingTimestamp(new java.util.Date());
    concept.setReadTimestamp(new java.util.Date());

    concept_worklist.add(concept);
    concepts = concept_worklist.getConcepts();
    if (concepts.length > 0)
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test ConceptWorklist: addCluster(ConceptCluster), getConcepts() ... "
        + date_format.format(timestamp));

    ConceptCluster concept_cluster = new ConceptCluster();
    concept_cluster.add(concept);
    concept_worklist.addCluster(concept_cluster);
    concepts = concept_worklist.getConcepts();
    if (concepts.length > 0)
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
    addToLog("Finished TestSuiteConceptWorklist at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}