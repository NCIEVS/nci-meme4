/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteConceptChecklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptCluster;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Test suite for ConceptChecklist
 */
public class TestSuiteConceptChecklist extends TestSuite {

  public TestSuiteConceptChecklist() {
    setName("TestSuiteConceptChecklist");
    setDescription("Test Suite for ConceptChecklist");
  }

  /**
   * Perform Test Suite ConceptChecklist
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ConceptChecklist concept_checklist = new ConceptChecklist();

    addToLog(
        "    1. Test ConceptChecklist: add(Concept[]), getConcepts() ... "
        + date_format.format(timestamp));

    ArrayList list = new ArrayList();
    Concept concept = new Concept.Default();
    concept.setCUI(new CUI("C0000001"));
    concept.setPreferredAtom(new Atom.Default(1070));
    concept.setEditingAuthority(new Authority.Default("KEVRIC"));
    concept.setEditingTimestamp(new java.util.Date());
    concept.setReadTimestamp(new java.util.Date());
    list.add(concept);

    concept_checklist.add((Concept[]) list.toArray(new Concept[] {}));
    Concept[] concepts = concept_checklist.getConcepts();
    if (concepts.length > 0)
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test ConceptChecklist: add(Object), getConcepts() ... "
        + date_format.format(timestamp));

    concept_checklist.add(concept);
    concepts = concept_checklist.getConcepts();
    if (concepts.length > 0)
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test ConceptChecklist: addCluster(Cluster), getConcepts() ... "
        + date_format.format(timestamp));

    ConceptCluster cluster = new ConceptCluster(new Identifier.Default(12345));
    concept_checklist.addCluster(cluster);
    concepts = concept_checklist.getConcepts();
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
    addToLog("Finished TestSuiteConceptChecklist at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}