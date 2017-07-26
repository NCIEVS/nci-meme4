/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_SDUITest
 * 
 * 06/07/2006 RBE (1-BEYL1): File created
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
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.MGV_SDUI;
import gov.nih.nlm.meme.integrity.UnaryCheckData;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MGV_SDUITest extends TestSuite{

  /**
   * Instantiates an empty {@link MGV_SDUITest}.
   */	
  public MGV_SDUITest() {
    setName("MGV_SDUITest");
    setDescription("Test suite for MGV_SDUI integrity");
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
      MGV_SDUI ic = (MGV_SDUI) adc.getIntegrityCheck("MGV_SDUI");

      //
      // create data set-up to test all logic
      //
      
      Concept source = client.getConcept(101);
      Concept target = client.getConcept(102);

      UnaryCheckData[] ucds = new UnaryCheckData [] { 
        	new UnaryCheckData("MGV_SDUI","SOURCE","MTH",false),
        	new UnaryCheckData("MGV_SDUI","SOURCE","MTH",false)
        };
      
      ic.setCheckData(ucds);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No cases of merges where the sources are the same but source descriptor are different found ...");
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
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup("MTH/PT"));
      atom.setSource(client.getSource("MTH"));
      atom.setCode(Code.newCode("D000959"));
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(source);
      atom.setSourceDescriptorIdentifier(new Identifier.Default(12345));
      source.addAtom(atom);

      MolecularAction ma2a = new MolecularInsertConceptAction(source);
      client.processAction(ma2a);
      
      // re-read concept
      source = client.getConcept(source);    
    
      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      //
      // 2b. Insert target concept
      //
      addToLog("    2b. Insert target concept ... " + date_format.format(timestamp));

      // Set up target concept
      target = new Concept.Default();
      atom.setCode(Code.newCode("D000960"));
      atom.setConcept(target);
      atom.setSourceDescriptorIdentifier(new Identifier.Default(12346));
      target.addAtom(atom);
      
      MolecularAction ma2b = new MolecularInsertConceptAction(target);
      client.processAction(ma2b);

      // re-read concept
      target = client.getConcept(target);    
      
      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      addToLog("        Should be a violation. Found cases of merges where the sources are the same but source descriptor are different. ...");
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
      // 4a. Undo 2a
      //
      addToLog("    4a. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction mBa = ma2a;
      mBa.setTransactionIdentifier(tid2a);
      client.processUndo(mBa);

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
      addToLog("Finished MGV_SDUITest at " +
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