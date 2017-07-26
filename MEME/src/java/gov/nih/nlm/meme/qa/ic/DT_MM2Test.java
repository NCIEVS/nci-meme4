/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_MM2Test
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
import gov.nih.nlm.meme.integrity.DT_MM2;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DT_MM2Test extends TestSuite{

  /**
   * Instantiates an empty {@link DT_MM2Test}.
   */	
  public DT_MM2Test() {
    setName("DT_MM2Test");
    setDescription("Test suite for DT_MM2 integrity");
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
      DT_MM2 ic = (DT_MM2) adc.getIntegrityCheck("DT_MM2");

      //
      // create data set-up to test all logic
      //
      
      Concept concept = new Concept.Default();

      // Create an atom
      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup("MTH/MM"));
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
      addToLog("    1a. Insert a concept with releasable MTH/MM ... " + date_format.format(timestamp));

      MolecularAction ma1a = new MolecularInsertConceptAction(concept);
      client.processAction(ma1a);
      
      // re-read concept
      concept = client.getConcept(concept);    
    
      addToLog("        Should not be a violation. MTH/MM atoms with different bracket numbers not found ...");
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
      // 2a. Insert a concept with releasable MTH/MM
      //

      addToLog("    2a. Insert a concept with releasable MTH/MM ... " + date_format.format(timestamp));

      // No need to initialize concept 
      
      // Create an atom with releasable MTH/MM
      atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup("MTH/MM"));
      atom.setSource(client.getSource("MTH"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept);
      
      concept.addAtom(atom);

      MolecularAction ma2a = new MolecularInsertConceptAction(concept);
      client.processAction(ma2a);
      
      // re-read concept
      concept = client.getConcept(concept);    
    
      addToLog("        Should be a violation.  Found an MTH/MM atoms with different bracket numbers but the same base string ...");
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
      // 2c. Undo 1a
      //
      addToLog("    2c. Undo 1a ... "
               + date_format.format(timestamp));

      MolecularAction ma2c = ma1a;
      ma2c.setTransactionIdentifier(tid1a);
      client.processUndo(ma2c);

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
      addToLog("Finished DT_MM2Test at " +
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