/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  ConceptMappingClientTest
 * 
 * 01/30/2006 RBE (1-763IU): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.client.ConceptMappingClient;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptMapping;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Concept Mapping
 */
public class ConceptMappingClientTest extends TestSuite {

  public ConceptMappingClientTest() {
    setName("ConceptMappingClientTest");
    setDescription("Test Suite for Concept Mapping");
  }

  /**
   * Perform Test Suite Concept Mapping
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ConceptMappingClient client = null;

    try {
      client = new ConceptMappingClient("apelon-db");

	    //
	    // 1.1. Test setMidService(String), getMidService()
	    //
      addToLog("    1.1. Test setMidService(String), "
        + "getMidService() ... "
        + date_format.format(timestamp));

      client.setMidService("apelon");
      if (client.getMidService().equals("apelon"))
        addToLog("    1.1. Test Passed");
      else {
        addToLog("    1.1. Test Failed");
        thisTestFailed();
      }

      //
      // 2.1. Test addConceptMapping(ConceptMapping)
      //
      addToLog(
        "    2.1. Test addConceptMapping(ConceptMapping) ... "
        + date_format.format(timestamp));

      Identifier id = new Identifier.Default("123456");
      ConceptMapping cm = new ConceptMapping.Default();
      cm.setIdentifier(id);
      cm.setCUI(new CUI("C0012345"));
      cm.setBirthVersion("2003AA");
      cm.setDeathVersion("2004AC");
      cm.setMappedToCui(new CUI("C0012346"));
      cm.setRelationshipName("R0");
      cm.setRelationshipAttribute("mapped_to");
      cm.setMappingReason("NO REASON");
      cm.setAlmostSY(false);
      cm.setGenerated(false);
      cm.setSource(new Source.Default("AIR93"));
      cm.setDead(false);
      cm.setStatus('R');
      cm.setSuppressible("N");
      cm.setAuthority(new Authority.Default("MTH"));
      cm.setTimestamp(new java.util.Date());
      cm.setInsertionDate(new java.util.Date());
      cm.setReleased('N');
      cm.setTobereleased('Y');
      cm.setRank(new Rank.Default(123));
      client.addConceptMapping(cm);

      //
      // 2.2. Test getConceptMappings()
      //
      addToLog(
          "    2.2. Test getConceptMappings() ... "
          + date_format.format(timestamp));
      ConceptMapping[] cms = client.getConceptMappings();
      if (cms.length > 0)
      {
        for (int i=0; i<cms.length; i++) {
          addToLog("\tRelationship Name["+i+"]: " + cms[i].getRelationshipName());
          addToLog("\t\tMapping Reason: " + cms[i].getMappingReason());
          addToLog("\t\tBirth Version: " + cms[i].getBirthVersion());
          addToLog("\t\tIs Bequeathal Relationships?: " + cms[i].isBequeathalMapping());
          addToLog("\t\tIs Deleted Mapping?: " + cms[i].isDeletedMapping());
          addToLog("\t\tIs Synonymous Mapping?: " + cms[i].isSynonymousMapping());
          client.addConceptMapping(cms[i]);
          client.removeConceptMapping(cms[i]);
        }
        addToLog("    2.2. Test Passed");
      }
      else {
        addToLog("    2.2. Test Failed");
        thisTestFailed();
      }

      //
      // 2.3. Test getConceptMappings(Concept)
      //
      addToLog(
          "    2.3. Test getConceptMappings(Concept) ... "
          + date_format.format(timestamp));
      Concept concept = new Concept.Default(0);
      concept.setCUI(new CUI("C0012345"));
      cms = client.getConceptMappings(concept);
      if (cms.length > 0)
      {
        for (int i=0; i<cms.length; i++) {
          addToLog("\tName["+i+"]: " + cms[i].getRelationshipName());
          addToLog("\t\tMapping Reason: " + cms[i].getMappingReason());
          addToLog("\t\tBirth Version: " + cms[i].getBirthVersion());
          addToLog("\t\tIs Bequeathal Mapping?: " + cms[i].isBequeathalMapping());
          addToLog("\t\tIs Deleted Mapping?: " + cms[i].isDeletedMapping());
          addToLog("\t\tIs Synonymous Mapping?: " + cms[i].isSynonymousMapping());
          client.addConceptMapping(cms[i]);
          client.removeConceptMapping(cms[i]);
        }
        addToLog("    2.3. Test Passed");
      }
      else {
        addToLog("    2.3. No record found.");
      }

      //
      // 2.4. Test removeConceptMapping(ConceptMapping)
      //
      addToLog(
          "    2.4. Test removeConceptMapping(ConceptMapping) ... "
          + date_format.format(timestamp));

      client.removeConceptMapping(cm);

    } catch(MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
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
    addToLog("Finished ConceptMappingClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}