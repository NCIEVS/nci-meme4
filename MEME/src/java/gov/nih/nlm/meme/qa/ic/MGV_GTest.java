/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_GTest
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
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.MGV_G;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MGV_GTest extends TestSuite{

  /**
   * Instantiates an empty {@link MGV_GTest}.
   */	
  public MGV_GTest() {
    setName("MGV_GTest");
    setDescription("Test suite for MGV_G integrity");
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
      MGV_G ic = (MGV_G) adc.getIntegrityCheck("MGV_G");

      //
      // create data set-up to test all logic
      //
      
      Concept source = client.getConcept(101);
      Concept target = client.getConcept(102);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No case where source and target both have current or previous version MSH ");
      addToLog("        MH atoms with different codes found. ...");
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

      Source[] sources = client.getCurrentSources();
      Source src = null;
      for (int i=0; i<sources.length; i++) {
      	if (sources[i].getStrippedSourceAbbreviation().equals("MSH")) {
      		src = sources[i];
      	  break;
      	}
      }

      // Create an atom
      Atom atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup(src.getSourceAbbreviation()+"/MH"));
      atom.setSource(src);
      atom.setCode(Code.newCode("D000959"));      
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setLevel('C');
      atom.setConcept(source);
      atom.setLastReleaseCUI(new CUI("C0000002"));
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
      src = client.getPreviousSource("MSH");

      target = new Concept.Default();
      atom.setTermgroup(client.getTermgroup(src.getSourceAbbreviation()+"/MH"));
      atom.setSource(src);
      atom.setCode(Code.newCode("D000960"));
      atom.setLastReleaseCUI(new CUI("C0000003"));
      atom.setConcept(target);     
      target.addAtom(atom);
      
      MolecularAction ma2b = new MolecularInsertConceptAction(target);
      client.processAction(ma2b);

      // re-read concept
      target = client.getConcept(target);    
      
      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      addToLog("        Should be a violation. Found case where source and target both have current or previous version MSH ");
      addToLog("        MH atoms with different codes ...");
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

      MolecularAction ma4a = ma2a;
      ma4a.setTransactionIdentifier(tid2a);
      client.processUndo(ma4a);

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
      addToLog("Finished MGV_GTest at " +
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