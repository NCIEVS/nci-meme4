/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteConceptCluster
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptCluster;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for ConceptCluster
 */
public class TestSuiteConceptCluster extends TestSuite {

  public TestSuiteConceptCluster() {
    setName("TestSuiteConceptCluster");
    setDescription("Test Suite for ConceptCluster");
  }

  /**
   * Perform Test Suite ConceptCluster
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ConceptCluster concept_cluster = new ConceptCluster();

    addToLog(
        "    1. Test ConceptCluster: add(Concept), getConcepts() ... "
        + date_format.format(timestamp));

    Concept concept = new Concept.Default();
    concept.setCUI(new CUI("C0000001"));
    concept.setPreferredAtom(new Atom.Default(1070));
    concept.setEditingAuthority(new Authority.Default("KEVRIC"));
    concept.setEditingTimestamp(new java.util.Date());
    concept.setReadTimestamp(new java.util.Date());

    concept_cluster.add(concept);
    Concept[] concepts = concept_cluster.getConcepts();
    if (concepts.length > 0)
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
    addToLog("Finished TestSuiteConceptCluster at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}