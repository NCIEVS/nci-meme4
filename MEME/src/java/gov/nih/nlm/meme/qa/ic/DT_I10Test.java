/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I10Test
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
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.DT_I10;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DT_I10Test extends TestSuite{

  /**
   * Instantiates an empty {@link DT_I10Test}.
   */	
  public DT_I10Test() {
    setName("DT_I10Test");
    setDescription("Test suite for DT_I10 integrity");
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
      DT_I10 ic = (DT_I10) adc.getIntegrityCheck("DT_I10");

      //
      // create data set-up to test all logic
      //
      Concept concept = client.getConcept(101);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No MSH/MH Atom found ...");
      if (!ic.validate(concept))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert concept with current MSH/MH atom
      //
      addToLog("    2a. Insert a concept with current MSH/MH Atom ... " + date_format.format(timestamp));

      concept = new Concept.Default();

      Source source = client.getCurrentSource("MSH");     

      // Create an atom
      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM 2");
      atom.setTermgroup(client.getTermgroup(source.getSourceAbbreviation()+"/MH"));
      atom.setSource(source);
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
    
      addToLog("        Should  be a violation. Found a current MH atom ...");
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
      // 3a. Insert a concept that have previous MH atom
      //
      addToLog("    3a. Insert a concept that have previous MH atom ... " + date_format.format(timestamp));

      //concept = new Concept.Default();

      source = client.getPreviousSource("MSH");

      // Create an atom
      atom = new Atom.Default();
      atom.setString("TEST ATOM 2");
      atom.setTermgroup(client.getTermgroup(source.getSourceAbbreviation()+"/MH"));
      atom.setSource(source);
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(concept);
      
      concept.addAtom(atom);

      MolecularAction ma3a = new MolecularInsertConceptAction(concept);
      client.processAction(ma3a);
      
      // re-read concept
      concept = client.getConcept(concept);
   
      addToLog("        Should not be a violation. Found a previous MSH/MH atom ...");
      if (!ic.validate(concept))
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      //
      // 3b. Undo 3a
      //
      addToLog("    3b. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma3b = ma3a;
      ma3b.setTransactionIdentifier(tid3a);
      client.processUndo(ma3b);

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
      addToLog("Finished DT_I10Test at " +
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