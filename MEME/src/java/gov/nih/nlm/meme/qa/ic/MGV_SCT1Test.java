/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_SCT1Test
 * 
 * 06/29/2006 RBE (1-BKQVT): File created
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
import gov.nih.nlm.meme.integrity.MGV_SCT1;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MGV_SCT1Test extends TestSuite{

  /**
   * Instantiates an empty {@link MGV_SCT1Test}.
   */	
  public MGV_SCT1Test() {
    setName("MGV_SCT1Test");
    setDescription("Test suite for MGV_SCT1 integrity");
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
      MGV_SCT1 ic = (MGV_SCT1) adc.getIntegrityCheck("MGV_SCT1");

      //
      // create data set-up to test all logic
      //
      
      Concept source = client.getConcept(101);
      Concept target = client.getConcept(102);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No SCT1 atoms from source/target found ...");
      if (!ic.validate(source, target))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert source contain entire concept
      //
      addToLog("    2a. Insert source contain entire concept ... " + date_format.format(timestamp));

      // Set up source concept
      source = new Concept.Default();

      // Create SNOMEDCT_US
      //Source src = client.getSource("MTH");
	  //src.setSourceToOutrank(src);
      //src.setInverter("MEME4 INVERTER");
      //src.setSourceAbbreviation("SNOMEDCT_US");
      //src.setNormalizedSourceAbbreviation("SNOMEDCT_US");
      //src.setRootSourceAbbreviation("SNOMEDCT_US");
      //src.setVersionedCui(new CUI("C000000"));
      //src.setRootCui(new CUI("C000000"));
  	  //adc.addSource(src);
      
      // Create an atom
      Atom atom = new Atom.Default();
      atom.setString("Entire fetal lower extremities (body structure)");
      atom.setTermgroup(client.getTermgroup("SNOMEDCT_US_2013_09_01/FN"));
      atom.setSource(client.getSource("SNOMEDCT_US"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(source);
      source.addAtom(atom);

      MolecularAction ma2a = new MolecularInsertConceptAction(source);
      client.processAction(ma2a);
      
      // re-read concept
      //source = client.getConcept(source);    
    
      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      //
      // 2b. Insert target contains the non SNOMEDCT_US/SCTSPA concept
      //
      addToLog("    2b. Insert target contains the non SNOMEDCT_US/SCTSPA concept ... " + date_format.format(timestamp));

      // Set up target concept
      target = new Concept.Default();

  	  // Create an atom
      atom = new Atom.Default();
      atom.setString("SNMI98");
      atom.setTermgroup(client.getTermgroup("SNMI98/PT"));
      atom.setSource(client.getSource("SNMI98"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(target);
      target.addAtom(atom);

      MolecularAction ma2b = new MolecularInsertConceptAction(target);
      client.processAction(ma2b);

      // re-read concept
      //target = client.getConcept(target);    
      
      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      addToLog("        Should be a violation. Found an SCT1 merge ...");
      if (ic.validate(source, target))
        addToLog("    2b. Test Passed");
      else {
        addToLog("    2b. Test Failed");
        thisTestFailed();
      }

      //
      // 3a. Undo 2b
      //
      addToLog("    3a. Undo 2b ... "
               + date_format.format(timestamp));

      MolecularAction ma3a = ma2b;
      ma3a.setTransactionIdentifier(tid2b);
      client.processUndo(ma3a);

      //
      // 3b. Undo 2a
      //
      addToLog("    3b. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction m3b = ma2a;
      m3b.setTransactionIdentifier(tid2a);
      client.processUndo(m3b);     
      
      //
      // 4a. Insert source contains the non SNOMEDCT_US/SCTSPA concept
      //
      addToLog("    4a. Insert source contains the non SNOMEDCT_US/SCTSPA concept ... " + date_format.format(timestamp));

      // Set up source concept
      source = new Concept.Default();

  	  // Create an atom
      atom = new Atom.Default();
      atom.setString("SNMI98");
      atom.setTermgroup(client.getTermgroup("SNMI98/PT"));
      atom.setSource(client.getSource("SNMI98"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(source);
      source.addAtom(atom);

      MolecularAction ma4a = new MolecularInsertConceptAction(source);
      client.processAction(ma4a);

      // re-read concept
      //source = client.getConcept(source);    
      
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
      
      //
      // 4b. Insert target contain entire concept
      //
      addToLog("    4b. Insert target contain entire concept ... " + date_format.format(timestamp));

      // Set up target concept
      target = new Concept.Default();

      // Create an atom
      atom = new Atom.Default();
      atom.setString("Entire fetal lower extremities (body structure)");
      atom.setTermgroup(client.getTermgroup("SNOMEDCT_US_2005_07_31/FN"));
      atom.setSource(client.getSource("SNOMEDCT_US"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(target);
      target.addAtom(atom);

      MolecularAction ma4b = new MolecularInsertConceptAction(target);
      client.processAction(ma4b);

      // re-read concept
      //target = client.getConcept(target);    
      
      // Save Transaction ID
      int tid4b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4b);

      addToLog("        Should be a violation. SCT1 merge not found ...");
      if (ic.validate(source, target))
        addToLog("    4b. Test Passed");
      else {
        addToLog("    4b. Test Failed");
        thisTestFailed();
      }

      //
      // 5a. Undo 4b
      //
      addToLog("    5a. Undo 4b ... "
               + date_format.format(timestamp));

      MolecularAction ma5a = ma4b;
      ma5a.setTransactionIdentifier(tid4b);
      client.processUndo(ma5a);

      //
      // 5b. Undo 4a
      //
      addToLog("    5b. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma5b = ma4a;
      ma5b.setTransactionIdentifier(tid4a);
      client.processUndo(ma5b);
      
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
      addToLog("Finished MGV_SCT1Test at " +
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