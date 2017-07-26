/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_PN2Test
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
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.DT_PN2;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DT_PN2Test extends TestSuite{

  /**
   * Instantiates an empty {@link DT_PN2Test}.
   */	
  public DT_PN2Test() {
    setName("DT_PN2Test");
    setDescription("Test suite for DT_PN2 integrity");
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
      DT_PN2 ic = (DT_PN2) adc.getIntegrityCheck("DT_PN2");

      //
      // create data set-up to test all logic
      //
      
      Concept concept = new Concept.Default();

      // Create an atom
      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup("MTH/PN"));
      atom.setSource(client.getSource("MTH"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept);
      
      concept.addAtom(atom);

      //
      // 1a. Insert a concept
      //
      addToLog("    1a. Insert a concept with releasable MTH/PN Atom ... " + date_format.format(timestamp));

      MolecularAction ma1a = new MolecularInsertConceptAction(concept);
      client.processAction(ma1a);
      
      // re-read concept
      concept = client.getConcept(concept);    
    
      addToLog("        Should not be a violation. No multiple releasable MTH/PN Atom found ...");
      if (!ic.validate(concept))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid1a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid1a);

      //
      // 2a. Insert another concept with releasable MTH/PN atom
      //

      // No need to initialize concept
      
      // Create an atom
      atom = new Atom.Default();
      atom.setString("TEST ATOM 2");
      atom.setTermgroup(client.getTermgroup("MTH/PN"));
      atom.setSource(client.getSource("MTH"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept);
      
      concept.addAtom(atom);

      addToLog("    2a. Insert a concept with releasable MTH/PN Atom ... " + date_format.format(timestamp));

      MolecularAction ma2a = new MolecularInsertConceptAction(concept);
      client.processAction(ma2a);
      
      // re-read concept
      concept = client.getConcept(concept);    
    
      addToLog("        Should be a violation. Found a multiple releasable MTH/PN atom ...");
      if (ic.validate(concept))
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

      //
      // 3a. Undo 1a
      //
      addToLog("    3a. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma3a = ma1a;
      ma3a.setTransactionIdentifier(tid1a);
      client.processUndo(ma3a);

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
      addToLog("Finished DT_PN2Test at " +
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