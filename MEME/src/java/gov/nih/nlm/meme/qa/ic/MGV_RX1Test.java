/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_RX1Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
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
import gov.nih.nlm.meme.integrity.MGV_RX1;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MGV_RX1Test extends TestSuite{

  /**
   * Instantiates an empty {@link MGV_RX1Test}.
   */	
  public MGV_RX1Test() {
    setName("MGV_RX1Test");
    setDescription("Test suite for MGV_RX1 integrity");
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
      MGV_RX1 ic = (MGV_RX1) adc.getIntegrityCheck("MGV_RX1");

      //
      // create data set-up to test all logic
      //
      
      Concept source = client.getConcept(101);
      Concept target = client.getConcept(102);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No rx1 atoms from source/target found ...");
      if (!ic.validate(source, target))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert source concept
      //
      addToLog("    2a. Insert source concept ... " + date_format.format(timestamp));

      // Set up source concept
      source = new Concept.Default();

      // Create an atom
      Atom atom = new Atom.Default();
      atom.setString("RXNORM");
      atom.setTermgroup(client.getTermgroup("RXNORM_2005AC/BN"));
      atom.setSource(client.getSource("RXNORM_2005AC"));
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
      // 2b. Insert target concept
      //
      addToLog("    2b. Insert target concept ... " + date_format.format(timestamp));

      // Set up target concept
      target = new Concept.Default();

      // Create an atom
      atom = new Atom.Default();
      atom.setString("RXNORM");
      atom.setTermgroup(client.getTermgroup("RXNORM_2005AC/DF"));
      atom.setSource(client.getSource("RXNORM_2005AC"));
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

      addToLog("        Should be a violation. Found an RX1 merge ...");
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
      // 4a. Insert target concept
      //
      addToLog("    4a. Insert target concept ... " + date_format.format(timestamp));

      // Set up target concept
      target = new Concept.Default();

      // Create an atom
      atom = new Atom.Default();
      atom.setString("RXNORM");
      atom.setTermgroup(client.getTermgroup("RXNORM_2005AC/BN"));
      atom.setSource(client.getSource("RXNORM_2005AC"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(target);
      target.addAtom(atom);

      MolecularAction ma4a = new MolecularInsertConceptAction(target);
      client.processAction(ma4a);

      // re-read concept
      //target = client.getConcept(target);    
      
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);

      addToLog("        Should not be a violation. RX1 merge not found ...");
      if (!ic.validate(source, target))
        addToLog("    4a. Test Passed");
      else {
        addToLog("    4a. Test Failed");
        thisTestFailed();
      }

      //
      // 4b. Undo 4a
      //
      addToLog("    4b. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma4b = ma4a;
      ma4b.setTransactionIdentifier(tid4a);
      client.processUndo(ma4b);
      
      //
      // 5a. Undo 2a
      //
      addToLog("    5a. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction m5a = ma2a;
      m5a.setTransactionIdentifier(tid2a);
      client.processUndo(m5a);

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
      addToLog("Finished MGV_RX1Test at " +
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