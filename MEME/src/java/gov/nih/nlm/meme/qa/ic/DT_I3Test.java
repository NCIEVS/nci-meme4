/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I3Test
 *
 * 01/05/2006 RBE (1-72TW3): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.DT_I3;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DT_I3Test extends TestSuite{

  /**
   * Instantiates an empty {@link DT_I3Test}.
   */	
  public DT_I3Test() {
    setName("DT_I3Test");
    setDescription("Test suite for DT_I3 integrity");
  }
  
  /**
   * Perform integrity test.
   */
  public void run() {
    TestSuiteUtils.printHeader(this);
    try {
      //
      // Initial Setup
      //
      SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
      Date timestamp = new Date(System.currentTimeMillis());

      EditingClient client = getClient();
      AuxiliaryDataClient adc = new AuxiliaryDataClient(client.getMidService());
      DT_I3 ic = (DT_I3) adc.getIntegrityCheck("DT_I3");

      //
      // create data set-up to test all logic
      //
      
      Concept concept = client.getConcept(101);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No demotion concept found ...");
      if (!ic.validate(concept))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert a demotion relationship
      //
      addToLog("    2a. Insert a demotion relationship ... " + date_format.format(timestamp));

      Relationship rel = new Relationship.Default();
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(104));
      rel.setAtom(client.getAtom(101));
      rel.setRelatedAtom(client.getAtom(103));
      rel.setName("LK");
      rel.setAttribute("translation_of");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('D');
      rel.setGenerated(false);
      rel.setLevel('P');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma2a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma2a);

      // re-read concept
      concept = client.getConcept(concept);
      client.populateRelationships(concept);
      
      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);
      
      addToLog("        Should be a violation. Found a demotion concept ...");
      if (ic.validate(concept))
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      //
      // 2b. Undo 2a
      //
      addToLog("    2b. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma2b = ma2a;
      ma2b.setTransactionIdentifier(tid2a);
      client.processUndo(ma2b);      

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
      addToLog("Finished DT_I3Test at " +
               date_format.format(new Date(System.currentTimeMillis())));
      addToLog("-------------------------------------------------------");
  	
    }
    catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
    }
  }
}