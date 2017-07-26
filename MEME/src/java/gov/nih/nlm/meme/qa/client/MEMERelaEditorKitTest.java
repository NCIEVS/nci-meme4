/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  MEMERelaEditorKitTest
 *
 * 02/14/2006 RBE (1-79GGX): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.MEMERelaEditorKit;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for MEME Rela Editor Kit
 */
public class MEMERelaEditorKitTest extends TestSuite {

  public MEMERelaEditorKitTest() {
    setName("MEMERelaEditorKitTest");
    setDescription("Test Suite for MEME Rela Editor Kit");
  }

  /**
   * Perform Test Suite MEME Rela Editor Kit
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    MEMERelaEditorKit client = null;
    AdminClient admin_client = null;

    try {
      client = new MEMERelaEditorKit("apelon");
      admin_client = new AdminClient("apelon");

      //
      // 1.1. Test getRelationshipNames()
      //
      addToLog(
        "    1.1. Test getRelationshipNames() ... "
        + date_format.format(timestamp));

      String[] rels = (String[]) client.getRelationshipNames();
      for (int i=0; i<rels.length; i++) {
        addToLog("            rels[i]: " + rels[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 1.2. Test insertRelationshipName(String, String, boolean, String, String, String, String)
      //
      addToLog(
        "    1.2. Test insertRelationshipName(String, String, boolean, String, String, String, String) ... "
        + date_format.format(timestamp));

      client.insertRelationshipName("MEME_QA", "MEME4_QA", true, "TEST QA", "INVERSE TEST QA", "TESTING QA", "INVERSE TESTING QA");
      rels = (String[]) client.getRelationshipNames();
      boolean found = false;
      for (int i=0; i<rels.length; i++) {
        if (rels[i].equals("MEME_QA")) {
          found = true;
          break;
        }
      }
      if (found)
        addToLog("    1.2. Test Passed");
      else {
        addToLog("    1.2. Test Failed");
        thisTestFailed();
      }

      //
      // 1.3. Test removeRelationshipName(String)
      //
      addToLog(
        "    1.3. Test removeRelationshipName(String) ... "
        + date_format.format(timestamp));

      client.removeRelationshipName("MEME_QA");
      rels = (String[]) client.getRelationshipNames();
      found = false;
      for (int i=0; i<rels.length; i++) {
        if (rels[i].equals("MEME_QA")) {
          found = true;
          break;
        }
      }
      if (!found)
        addToLog("    1.3. Test Passed");
      else {
        addToLog("    1.3. Test Failed");
        thisTestFailed();
      }

        
      //
      // 2.1. Test getRelationshipAttributes()
      //        
      addToLog(
        "    2.1. Test getRelationshipAttributes() ... "
        + date_format.format(timestamp));

      String[] relas = (String[]) client.getRelationshipAttributes();
      for (int i=0; i<relas.length; i++) {
        addToLog("            relas[i]: " + relas[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 2.2. Test insertRelationshipAttribute(String, String, int)
      //        
      addToLog(
        "    2.2. Test insertRelationshipAttribute(String, String, int) ... "
        + date_format.format(timestamp));

      client.insertRelationshipAttribute("programs", "programmed_by", 1);
      relas = (String[]) client.getRelationshipAttributes();
      found = false;
      for (int i=0; i<relas.length; i++) {
        if (relas[i].equals("programs")) {
          found = true;
          break;
        }
      }
      if (found)
        addToLog("    2.2. Test Passed");
      else {
        addToLog("    2.2. Test Failed");
        thisTestFailed();
      }
      
      //
      // 2.3. Test removeRelationshipAttribute(String)
      //        
      addToLog(
        "    2.3. Test removeRelationshipAttribute(String) ... "
        + date_format.format(timestamp));

      client.removeRelationshipAttribute("programs");
      admin_client.refreshCaches();
      relas = (String[]) client.getRelationshipAttributes();
      found = false;
      for (int i=0; i<relas.length; i++) {
        if (relas[i].equals("programs")) {
          found = true;
          break;
        }
      }
      if (!found)
        addToLog("    2.3. Test Passed");
      else {
        addToLog("    2.3. Test Failed");
        thisTestFailed();
      }

      //
      // 3.1. Test getSources()
      //        
      addToLog(
        "    3.1. Test getSources() ... "
        + date_format.format(timestamp));

      String[] sources = (String[]) client.getSources();
      for (int i=0; i<sources.length; i++) {
        addToLog("            sources[i]: " + sources[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 4.1. Test getTobereleased()
      //        
      addToLog(
        "    4.1. Test getTobereleased() ... "
        + date_format.format(timestamp));

      String[] tbrs = (String[]) client.getTobereleased();
      for (int i=0; i<tbrs.length; i++) {
        addToLog("            tobereleased[i]: " + tbrs[i]);
      }

      //
      // 5.1. Test processRelationshipBetweenCUIs(String, String, String, String, String, String, String, int)
      //        
      addToLog(
        "    5.1. Test processRelationshipBetweenCUIs(String, String, String, String, String, String, String, int) ... "
        + date_format.format(timestamp));

      int rel_id = client.processRelationshipBetweenCUIs("C0036024", "C0036025", "BT", "associated_with", "CSP2004", "CSP2004", "M4TEST", 72850314);
      if (rel_id > 0)
        addToLog("    5.1. Test Passed. rel_id = " + rel_id);
      else {
        addToLog("    5.1. Test Failed");
        thisTestFailed();
      }

      //
      // 5.2. Test processRelationshipBetweenAtoms(int, int, String, String, String, String, String, int)
      //        
      addToLog(
        "    5.2. Test processRelationshipBetweenAtoms(int, int, String, String, String, String, String, int) ... "
        + date_format.format(timestamp));

      rel_id = client.processRelationshipBetweenAtoms(13889596, 13891064, "BT", "associated_with", "CSP2004", "CSP2004", "M4TEST", 72850314);
      if (rel_id > 0)
        addToLog("    5.2. Test Passed. rel_id = " + rel_id);
      else {
        addToLog("    5.2. Test Failed");
        thisTestFailed();
      }

      //
      // 5.3. Test processRelationshipBetweenConcepts(int, int, String, String, String, String, String, int)
      //        
      addToLog(
        "    5.3. Test processRelationshipBetweenConcepts(int, int, String, String, String, String, String, int) ... "
        + date_format.format(timestamp));

      rel_id = client.processRelationshipBetweenConcepts(1070690, 820910, "BT", "associated_with", "CSP2004", "CSP2004", "M4TEST", 72850314);
      if (rel_id > 0)
        addToLog("    5.3. Test Passed. rel_id = " + rel_id);
      else {
        addToLog("    5.3. Test Failed");
        thisTestFailed();
      }

      //
      // 6.1. Test insertRelationshipBetweenCUIs(String, String, String, String, String, String, String)
      //        
      addToLog(
        "    6.1. Test insertRelationshipBetweenCUIs(String, String, String, String, String, String, String) ... "
        + date_format.format(timestamp));

      rel_id = client.insertRelationshipBetweenCUIs("C0036024", "C0036025", "BT", "associated_with", "CSP2004", "CSP2004", "M4TEST");
      if (rel_id > 0)
        addToLog("    6.1. Test Passed. rel_id = " + rel_id);
      else {
        addToLog("    6.1. Test Failed");
        thisTestFailed();
      }

      //
      // 6.2. Test insertRelationshipBetweenAtoms(int, int, String, String, String, String, String)
      //        
      addToLog(
        "    6.2. Test insertRelationshipBetweenAtoms(int, int, String, String, String, String, String) ... "
        + date_format.format(timestamp));

      rel_id = client.insertRelationshipBetweenAtoms(13889596, 13891064, "BT", "associated_with", "CSP2004", "CSP2004", "M4TEST");
      if (rel_id > 0)
        addToLog("    6.2. Test Passed. rel_id = " + rel_id);
      else {
        addToLog("    6.2. Test Failed");
        thisTestFailed();
      }

      //
      // 6.3. Test insertRelationshipBetweenConcepts(int, int, String, String, String, String, String)
      //        
      addToLog(
        "    6.3. Test insertRelationshipBetweenConcepts(int, int, String, String, String, String, String) ... "
        + date_format.format(timestamp));

      rel_id = client.insertRelationshipBetweenConcepts(1070690, 820910, "BT", "associated_with", "CSP2004", "CSP2004", "M4TEST");
      if (rel_id > 0)
        addToLog("    6.3. Test Passed. rel_id = " + rel_id);
      else {
        addToLog("    6.3. Test Failed");
        thisTestFailed();
      }

      //
      // 7.1. Test changeRelationshipTBR(int, String, String)
      //        
      addToLog(
        "    7.1. Test changeRelationshipTBR(int, String, String) ... "
        + date_format.format(timestamp));

      client.changeRelationshipTBR(72850314, "M4TEST", "Y");     

    } catch (MEMEException e) {
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
    addToLog("Finished MEMERelaEditorKitTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}