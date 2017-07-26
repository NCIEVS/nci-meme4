/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_M3Test
 *
 * 01/05/2006 RBE (1-72TW3): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.DT_M3;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DT_M3Test extends TestSuite{

  /**
   * Instantiates an empty {@link DT_M3Test}.
   */	
  public DT_M3Test() {
    setName("DT_M3Test");
    setDescription("Test suite for DT_M3 integrity");
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
      DT_M3 ic = (DT_M3) adc.getIntegrityCheck("DT_M3");

      //
      // create data set-up to test all logic
      //
      
      Concept concept = client.getConcept(101);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should be a violation. No releasable atom found ...");
      if (ic.validate(concept))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert a concept with releasable atom
      //

      concept = new Concept.Default();

      // Create an atom
      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup("MTH/PT"));
      atom.setSource(client.getSource("MTH"));
      atom.setCode(Code.newCode("NOCODE"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept);
      
      concept.addAtom(atom);

      addToLog("    2a. Insert a concept with releasable atom ... " + date_format.format(timestamp));

      MolecularAction ma2a = new MolecularInsertConceptAction(concept);
      client.processAction(ma2a);
      
      // re-read concept
      concept = client.getConcept(concept);    
    
      addToLog("        Should not be a violation. Releasable atom found ...");
      if (!ic.validate(concept))
        addToLog("    2a. Test Passed");
      else {
        addToLog("    2a. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

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
      addToLog("Finished DT_M3Test at " +
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