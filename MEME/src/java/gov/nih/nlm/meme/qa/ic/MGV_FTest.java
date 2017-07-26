/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_FTest
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
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.MGV_F;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MGV_FTest extends TestSuite{

  /**
   * Instantiates an empty {@link MGV_FTest}.
   */	
  public MGV_FTest() {
    setName("MGV_FTest");
    setDescription("Test suite for MGV_F integrity");
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
      MGV_F ic = (MGV_F) adc.getIntegrityCheck("MGV_F");

      //
      // create data set-up to test all logic
      //

      Concept source = client.getConcept(101);
      Concept target = client.getConcept(102);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No current version MSH rel connecting source and target found ...");
      if (!ic.validate(source, target))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert a source relationship
      //
      addToLog("    2a. Insert a source relationship ... " + date_format.format(timestamp));

      Source[] sources = client.getCurrentSources();
      Source src = null;
      for (int i=0; i<sources.length; i++) {
      	if (sources[i].getStrippedSourceAbbreviation().equals("MSH")) {
      		src = sources[i];
      	  break;
      	}
      }

      Relationship rel = new Relationship.Default();
      rel.setAtom(client.getAtom(101));
      rel.setRelatedAtom(client.getAtom(102));
      rel.setConcept(source);
      rel.setRelatedConcept(client.getConcept(102));
      rel.setName("RT");
      rel.setAttribute("mapped_to");
      rel.setSource(src);
      rel.setSourceOfLabel(src);
      rel.setStatus('R');
      rel.setGenerated(false);
      rel.setLevel('S');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      source.addRelationship(rel);    

      MolecularAction ma2a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma2a);

      // re-read concept
      source = client.getConcept(source);
      client.populateRelationships(source);
      
      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);
      
      addToLog("        Should be a violation. Found a current version MSH rel connecting source and target ...");
      if (ic.validate(source, target))
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
      addToLog("Finished MGV_FTest at " +
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