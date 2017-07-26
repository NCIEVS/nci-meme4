/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  ReportsClientTest
 * 
 * 02/14/2006 RBE (1-79GGX): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.action.ActionReport;
import gov.nih.nlm.meme.action.AtomicAction;
//import gov.nih.nlm.meme.client.AdminClient;
//import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.ReportsClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.ReportStyle;
//import gov.nih.nlm.meme.common.SourceDifference;
//import gov.nih.nlm.meme.common.SourceMetadataReport;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Reports Client
 */
public class ReportsClientTest extends TestSuite {

  public ReportsClientTest() {
    setName("ReportsClientTest");
    setDescription("Test Suite for Reports Client");
  }

  /**
   * Perform Test Suite Reports Client
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ReportsClient client = null;

    try {
      client = new ReportsClient("apelon");

      //
      // 1.1. Test getReport(int)
      //
      addToLog("    1.1. Test getReport(int) ... "
        + date_format.format(timestamp));
      
      String report = client.getReport(101);
      addToLog("            Report: " + report);

      //
      // 2.1. Test getReports(int[])
      //
      addToLog("    2.1. Test getReports(int[]) ... "
        + date_format.format(timestamp));
      
      report = client.getReports(new int[] {101, 102, 103});
      addToLog("            Report: " + report);
      
      //
      // 3.1. Test getReport(String)
      //
      addToLog("    3.1. Test getReport(String) ... "
        + date_format.format(timestamp));
      
      report = client.getReport("C0734767");
      addToLog("            Report: " + report);
      
      //
      // 4.1. Test getReports(String[])
      //
      addToLog("    4.1. Test getReports(String[]) ... "
        + date_format.format(timestamp));
      
      report = client.getReports(new String[] {"C0734767", "C0734768"});
      addToLog("            Report: " + report);
      
      //
      // 5.1. Test getReportForAtom(int)
      //
      addToLog("    5.1. Test getReportForAtom(int) ... "
        + date_format.format(timestamp));
      
      report = client.getReportForAtom(102);
      addToLog("            Report: " + report);
      
      //
      // 6.1. Test getReportsForAtoms(int[])
      //
      addToLog("    6.1. Test getReportsForAtoms(int[]) ... "
        + date_format.format(timestamp));
      
      report = client.getReportsForAtoms(new int[] {101, 102});
      addToLog("            Report: " + report);
/*
      //
      // 7.1. Test getSourceMetadataReport()
      //
      addToLog("    7.1. Test getSourceMetadataReport ... "
        + date_format.format(timestamp));
      
      SourceMetadataReport smr = client.getSourceMetadataReport();
      addToLog("            SourceMetadataReport: " + smr.toString());
      
      SourceDifference[] sd = smr.getAttributeNameDifferences();
      for (int i=0; i<sd.length; i++) {
        addToLog("            Source Difference["+i+"] = " + sd.toString());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
      
      sd = smr.getRelationshipAttributeDifferences();
      for (int i=0; i<sd.length; i++) {
        addToLog("            Source Difference["+i+"] = " + sd.toString());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }

      sd = smr.getSourceDifferences();
      for (int i=0; i<sd.length; i++) {
        addToLog("            Source Difference["+i+"] = " + sd.toString());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
      
      sd = smr.getTermgroupDifferences();
      for (int i=0; i<sd.length; i++) {
        addToLog("            Source Difference["+i+"] = " + sd.toString());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
 */
      
      //
      // 8.1. Test setMidService(String)
      //
      addToLog("    8.1. Test setMidService(String) ... "
        + date_format.format(timestamp));

      client.setMidService("apelon");
      
      //
      // 9.1. Test includeOrExcludeLanguages()
      //
      addToLog("    9.1. Test includeOrExcludeLanguages() ... "
        + date_format.format(timestamp));

      addToLog("    9.1. Include Or Exclude Languages: " + client.includeOrExcludeLanguages());
      
      //
      // 10.1. Test getReadLanguages()
      //
      addToLog("    10.1. Test getReadLanguages() ... "
        + date_format.format(timestamp));

      String[] languages = client.getReadLanguages();
      for (int i=0; i<languages.length; i++) {
        addToLog("            Languages["+i+"] = " + languages[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }

      //
      // 11.1. Test setReadLanguagesToExclude(String[])
      //
      addToLog("    11.1. Test setReadLanguagesToExclude(String[]) ... "
        + date_format.format(timestamp));

      client.setReadLanguagesToExclude(new String[] {"DAN", "DUT", "FIN" });

      //
      // 12.1. Test setReadLanguagesToInclude(String[])
      //
      addToLog("    12.1. Test setReadLanguagesToInclude(String[]) ... "
        + date_format.format(timestamp));

      client.setReadLanguagesToInclude(new String[] {"DAN", "DUT", "FIN" });

      //
      // 13.1. Test setRelationshipViewMode(int)
      //
      addToLog("    13.1. Test setRelationshipViewMode(int) ... "
        + date_format.format(timestamp));

      client.setRelationshipViewMode(1);

      //
      // 14.1. Test setContextRelationshipViewMode(int)
      //
      addToLog("    14.1. Test setContextRelationshipViewMode(int) ... "
        + date_format.format(timestamp));

      client.setContextRelationshipViewMode(1);

      //
      // 15.1. Test setMaxReviewedRelationshipCount(int)
      //
      addToLog("    15.1. Test setMaxReviewedRelationshipCount(int) ... "
        + date_format.format(timestamp));

      client.setMaxReviewedRelationshipCount(10);
      
      //
      // 16.1. Test setContentType(String), getContentType()
      //
      addToLog("    16.1. Test setContentType(String), getContentType() ... "
        + date_format.format(timestamp));
      
      client.setContentType("text/plain");
      if (client.getContentType().equals("text/plain"))
        addToLog("    16.1. Test Passed");
      else {
        addToLog("    16.1. Test Failed");
        thisTestFailed();
      }

      //
      // 17.1. Test addStyle(ReportStyle), removeStyle(ReportStyle), clearStyles()
      //
      addToLog("    17.1. Test addStyle(ReportStyle), removeStyle(ReportStyle),"
    		+ " clearStyles() ... "
        + date_format.format(timestamp));
      
      ReportStyle rs = new ReportStyle.Default();
      client.addStyle(rs);
      client.removeStyle(rs);
      client.clearStyles();

      //
      // 18.1. Test getActionReport(int)
      //
      addToLog("    18.1. Test getActionReport(int) ... "
        + date_format.format(timestamp));

      int molecule_id = 45173814;

      ActionReport ar = client.getActionReport(molecule_id);
      addToLog("            Molecular Action Name: " + ar.getMolecularAction().getActionName());
      AtomicAction[] actions = ar.getAtomicAction();
      for (int i=0; i<actions.length; i++) {
        addToLog("            Atomic Actions["+i+"] = " + actions[i].getActionName());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
      Atom[] atoms = ar.getAtoms();
      for (int i=0; i<atoms.length; i++) {
        addToLog("            Atoms["+i+"] = " + atoms[i].getString());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
      Attribute[] attrs = ar.getAttribute();
      for (int i=0; i<attrs.length; i++) {
        addToLog("            Attributes["+i+"] = " + attrs[i].getName());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
      Relationship[] rels = ar.getRelationship();
      for (int i=0; i<rels.length; i++) {
        addToLog("            Relationships["+i+"] = " + rels[i].getName());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
      Concept[] source_concepts = ar.getSource();
      for (int i=0; i<source_concepts.length; i++) {
      	if (source_concepts[i] != null)
      		addToLog("            Source Concepts["+i+"] = " + source_concepts[i].getIdentifier().toString());
      	else
      		addToLog("            Source Concepts["+i+"] = " + source_concepts[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
      Concept[] target_concepts = ar.getTarget();
      for (int i=0; i<target_concepts.length; i++) {
      	if (target_concepts[i] != null)
      		addToLog("            Target Concepts["+i+"] = " + target_concepts[i].getIdentifier().toString());
      	else
      		addToLog("            Target Concepts["+i+"] = " + target_concepts[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }      	
      }
      
      //
      // 19.1. Test getActionReportDocument(int)
      //
      addToLog("    19.1. Test getActionReportDocument(int) ... "
        + date_format.format(timestamp));

      String report_doc = client.getActionReportDocument(molecule_id);
      addToLog("            Action Report Document: " + report_doc);

      // For 20.1 & 21.1
      // Test this through the Action Harvester web application
      // Click on “Report of Editor Actions for Today”
      
      //
      // 22.1. Test getEditingReportData(int)
      //
      addToLog("    22.1. Test getEditingReportData(int) ... "
        + date_format.format(timestamp));

      //String[][] erd = client.getEditingReportData(30);
      //for (int i=0; i<erd.length; i++) {
      //  for (int j=0; j<erd[j].length; j++) {
      //    addToLog("            Editing Report Data[i][j]: " + erd[i][j]);
      //  }
      //}
      
      //
      // 23.1. Test getEditingReportData(String, Date, Date)
      //
      addToLog("    23.1. Test getEditingReportData(String, Date, Date) ... "
        + date_format.format(timestamp));

      //erd = client.getEditingReportData("wrk05b_icpc2icd10_nc_113", new Date(), new Date());
      //for (int i=0; i<erd.length; i++) {
      //  for (int j=0; j<erd[j].length; j++) {
      //    addToLog("            Editing Report Data[i][j]: " + erd[i][j]);
      //  }
      //}

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
    addToLog("Finished ReportsClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}