/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  ActionClientTest
 *
 * 01/30/2006 RBE (1-763IU): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.client.ActionClient;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.LoggedError;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Admin Client
 */
public class ActionClientTest extends TestSuite {

  public ActionClientTest() {
    setName("ActionClientTest");
    setDescription("Test Suite for Admin Client");
  }

  /**
   * Perform Test Suite Admin Client
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ActionClient client = null;

    try {
      client = new ActionClient("apelon");
    
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
	    // 1.2. Test setAuthority(Authority), getAuthority()
	    //
	    addToLog("    1.2. Test setAuthority(Authority), "
        + "getAuthority() ... "
        + date_format.format(timestamp));
	
	    Authority authority = new Authority.Default("MTH");
	    client.setAuthority(authority);
	    if (client.getAuthority().equals(authority))
	      addToLog("    1.2. Test Passed");
	    else {
	      addToLog("    1.2. Test Failed");
	      thisTestFailed();
	    }
	    
	    //
	    // 1.3. Test setForce(boolean), getForce()
	    //
	    addToLog("    1.3. Test setForce(boolean), "
	      + "isForce() ... "
	      + date_format.format(timestamp));
	
	    client.setForce(true);
	    if (client.isForce())
	      addToLog("    1.3. Test Passed");
	    else {
	      addToLog("    1.3. Test Failed");
	      thisTestFailed();
	    }
	
	    //
	    // 1.4. Test setIntegrityVector(EnforcableIntegrityVector), getIntegrityVector()
	    //
	    addToLog("    1.4. Test setIntegrityVector(EnforcableIntegrityVector), "
	      + " getIntegrityVector() ... "
	      + date_format.format(timestamp));
	
	    EnforcableIntegrityVector eiv = new EnforcableIntegrityVector(); 
	    client.setIntegrityVector(eiv);
	    if (client.getIntegrityVector().equals(eiv))
	      addToLog("    1.4. Test Passed");
	    else {
	      addToLog("    1.4. Test Failed");
	      thisTestFailed();
	    }
	    
	    //
	    // 1.5. Test setChangeStatus(true), getChangeStatus()
	    //
	    addToLog("    1.5. Test setChangeStatus(true), "
	      + "getChangeStatus() ... "
	      + date_format.format(timestamp));
	
	    client.setChangeStatus(true);
	    if (client.getChangeStatus())
	      addToLog("    1.5. Test Passed");
	    else {
	      addToLog("    1.5. Test Failed");
	      thisTestFailed();
	    }
	
	    //
	    // 1.6. Test setTransactionIdentifier(Identifier), getTransactionIdentifier()
	    //
	    addToLog("    1.6. Test setTransactionIdentifier(Identifier), "
	      + "getTransactionIdentifier() ... "
	      + date_format.format(timestamp));
	
	    Identifier transaction_id = new Identifier.Default(12345);
	    client.setTransactionIdentifier(transaction_id);
	    if (client.getTransaction().getIdentifier().equals(transaction_id))
	      addToLog("    1.6.1. Test Passed. Transaction.getIdentifier() is null when transaction id was set.");
	    else {
	      addToLog("    1.6.1. Test Failed");
	      thisTestFailed();
	    }
	    
	    client.setTransactionIdentifier(transaction_id);
	    if (client.getTransaction().getIdentifier().equals(transaction_id))
	      addToLog("    1.6.2. Test Passed. Transaction.getIdentifier() is not null when transaction id was set.");
	    else {
	      addToLog("    1.6.2. Test Failed");
	      thisTestFailed();
	    }
	
	    //
	    // 1.7. Test setWorkIdentifier(Identifier), getWorkLog()
	    //
	    addToLog("    1.7. Test setWorkIdentifier(Identifier), "
	      + "getWorkLog() ... "
	      + date_format.format(timestamp));
	
	    Identifier work_id = new Identifier.Default(12345);
	    client.setWorkIdentifier(transaction_id);
	    if (client.getWorkLog().getIdentifier().equals(work_id))
	      addToLog("    1.7.1. Test Passed. Worklog.getIdentifier() is null when work id was set.");
	    else {
	      addToLog("    1.7.1. Test Failed");
	      thisTestFailed();
	    }
	
	    client.setWorkIdentifier(transaction_id);
	    if (client.getWorkLog().getIdentifier().equals(work_id))
	      addToLog("    1.7.2. Test Passed. Worklog.getIdentifier() is not null when work id was set.");
	    else {
	      addToLog("    1.7.2. Test Failed");
	      thisTestFailed();
	    }
	
	    //
	    // 1.8. Test getWorkLog(int)
	    //
	    addToLog("    1.8. Test getWorkLog(int) ... "
	      + date_format.format(timestamp));
	
	    // Get all worklogs
	    WorkLog[] worklogs = client.getWorkLogs();

	    WorkLog worklog = client.getWorkLog(worklogs[0].getIdentifier().intValue());
	    if (worklog != null) {
	      addToLog("    1.8. Test Passed");
	      addToLog("    1.8. Worklog ID: " + worklog.getIdentifier());
	    } else {
	      addToLog("    1.8. Test Failed");
	      thisTestFailed();
	    }
    
	    //
	    // 1.9. Test getWorkLogs()
	    //
	    addToLog("    1.9. Test getWorkLogs() ... "
	      + date_format.format(timestamp));
	
	    worklogs = client.getWorkLogs();
      for (int i=0; i<worklogs.length; i++) {
        addToLog("            Worklogs["+i+"] = " + worklogs[i].getIdentifier());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

	    //
	    // 1.10. Test getWorkLogsByType(String)
	    //
	    addToLog("    1.10. Test getWorkLogsByType(String) ... "
	      + date_format.format(timestamp));
	
	    worklogs = client.getWorkLogsByType("INSERTION");
      for (int i=0; i<worklogs.length; i++) {
        addToLog("            Worklogs["+i+"] = " + worklogs[i].getIdentifier());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

	    //
	    // 1.11. Test getActivityLog(MolecularTransaction)
	    //
	    addToLog("    1.11. Test getActivityLog(MolecularTransaction) ... "
	      + date_format.format(timestamp));
	    
	    Activity activity = client.getActivityLog(new MolecularTransaction(0));
	    if (activity != null) {
	      addToLog("    1.11. Test Passed");
        addToLog("         Activity: " + activity.getShortDescription());
	    } else {
	      addToLog("    1.11. Test Failed");
	      thisTestFailed();
	    }

	    //
	    // 1.12. Test getActivityLogs(WorkLog)
	    //
	    addToLog("    1.12. Test getActivityLogs(WorkLog) ... "
	      + date_format.format(timestamp));
	    
	    Activity[] activities = client.getActivityLogs(worklog);
      for (int i=0; i<activities.length; i++) {
        addToLog("            Activities["+i+"] = " + activities[i].getIdentifier());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

	    //
	    // 1.13. Test getErrors(MolecularTransaction)
	    //
	    addToLog("    1.13. Test getErrors(MolecularTransaction) ... "
	      + date_format.format(timestamp));
	    
	    LoggedError[] errors = client.getErrors(new MolecularTransaction(0));	    
      for (int i=0; i<errors.length; i++) {
        addToLog("            Errors["+i+"] = " + errors[i].getDetail());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

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
    addToLog("Finished ActionClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}