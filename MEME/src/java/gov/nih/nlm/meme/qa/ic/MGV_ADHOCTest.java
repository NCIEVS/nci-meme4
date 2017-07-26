/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_ADHOCTest
 * 
 * 12/07/2006 BAC (1-D0BIJ): Replace "MDR80" with current version (via lookup)
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
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.MGV_ADHOC;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MGV_ADHOCTest extends TestSuite{

  /**
   * Instantiates an empty {@link MGV_ADHOCTest}.
   */	
  public MGV_ADHOCTest() {
    setName("MGV_ADHOCTest");
    setDescription("Test suite for MGV_ADHOC integrity");
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
      MGV_ADHOC ic = (MGV_ADHOC) adc.getIntegrityCheck("MGV_ADHOC");

      //
      // create data set-up to test all logic
      //
      
      Concept source = client.getConcept(101);
      Concept target = client.getConcept(102);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No chemical semantic type found ...");
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
      
      Source[] sources = client.getSources();
      Source src = null;
      for (int i=0; i<sources.length; i++) {
      	if (sources[i].getStrippedSourceAbbreviation().equals("MDR") &&
      			sources[i].isCurrent()) {
      		src = sources[i];
      	  break;
      	}
      }
      
      // Create an atom
      Atom atom = client.getAtom(101);
      atom.setSource(src);
      atom.setTobereleased('Y');
      
      atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup(src.getSourceAbbreviation()+"/PT"));
      atom.setSource(src);
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(source);

      // Create a concept semantic type
      ConceptSemanticType cst = new ConceptSemanticType();
      cst.setIsChemical(true);
      cst.setName("SEMANTIC_TYPE");
      cst.setValue("Carbohydrate");
      cst.setChemicalType("S");
      cst.setIsEditingChemical(true);
      cst.setLevel('C');
      cst.setStatus('R');
      cst.setReleased('A');
      cst.setTobereleased('Y');
      cst.setConcept(source);
      cst.setSource(src);
      
      source.addAtom(atom);
      source.addAttribute(cst);
      source.setTobereleased('Y');
      source.setLevel('N');
      source.setStatus('S');
      source.setReleased('A');
      source.setSource(src);
      
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
      
      atom.setConcept(target);
      cst.setConcept(target);
      
      target.addAtom(atom);
      target.addAttribute(cst);
      target.setTobereleased('Y');
      target.setLevel('N');
      target.setStatus('S');
      target.setReleased('A');
      target.setSource(src);

      
      MolecularAction ma2b = new MolecularInsertConceptAction(target);
      client.processAction(ma2b);

      // re-read concept
      target = client.getConcept(target);    
      
      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      addToLog("        Should be a violation. Found a chemical semantic type in the source concept with a releasable MDR atoms in target ...");
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

      //
      // 5a. Insert target concept
      //
      addToLog("    5a. Insert target concept ... " + date_format.format(timestamp));

      // Set up target concept
      target = new Concept.Default();
      
      sources = client.getSources();
      src = null;
      for (int i=0; i<sources.length; i++) {
      	if (sources[i].getStrippedSourceAbbreviation().equals("MDR")) {
      		src = sources[i];
      	  break;
      	}
      }
      
      // Create an atom
      atom = client.getAtom(101);
      atom.setSource(src);
      atom.setTobereleased('Y');
      
      atom = new Atom.Default();
      atom.setString("TEST ATOM 1");
      atom.setTermgroup(client.getTermgroup(src.getSourceAbbreviation()+"/PT"));
      atom.setSource(src);
      atom.setStatus('R');
      atom.setGenerated(true);
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setSuppressible("N");
      atom.setConcept(target);

      // Create a concept semantic type
      cst = new ConceptSemanticType();
      cst.setIsChemical(true);
      cst.setName("SEMANTIC_TYPE");
      cst.setValue("Carbohydrate");
      cst.setChemicalType("S");
      cst.setIsEditingChemical(true);
      cst.setLevel('C');
      cst.setStatus('R');
      cst.setReleased('A');
      cst.setTobereleased('Y');
      cst.setConcept(target);
      cst.setSource(src);
      
      target.addAtom(atom);
      target.addAttribute(cst);
      target.setTobereleased('Y');
      target.setLevel('N');
      target.setStatus('S');
      target.setReleased('A');
      target.setSource(src);
      
      MolecularAction ma5a = new MolecularInsertConceptAction(target);
      client.processAction(ma5a);
      
      // re-read concept
      //target = client.getConcept(target);    
    
      // Save Transaction ID
      int tid5a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5a);

      //
      // 5b. Insert source concept
      //
      addToLog("    5b. Insert source concept ... " + date_format.format(timestamp));

      // Set up source concept
      source = new Concept.Default();
      
      atom.setConcept(source);
      cst.setConcept(source);
      
      source.addAtom(atom);
      source.setTobereleased('Y');
      source.setLevel('N');
      source.setStatus('S');
      source.setReleased('A');
      source.setSource(src);

      
      MolecularAction ma5b = new MolecularInsertConceptAction(source);
      client.processAction(ma5b);

      // re-read concept
      source = client.getConcept(source);    
      
      // Save Transaction ID
      int tid5b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5b);

      addToLog("        Should be a violation. Found a chemical semantic type in the target concept with a releasable MDR atoms in source ...");
      if (ic.validate(source, target))
        addToLog("    5b. Test Passed");
      else {
        addToLog("    5b. Test Failed");
        thisTestFailed();
      }

      //
      // 6a. Undo 5b
      //
      addToLog("    6a. Undo 5b ... "
               + date_format.format(timestamp));

      MolecularAction ma6a = ma5b;
      ma6a.setTransactionIdentifier(tid5b);
      client.processUndo(ma6a);
      
      //
      // 7a. Undo 5a
      //
      addToLog("    7a. Undo 5a ... "
               + date_format.format(timestamp));

      MolecularAction ma7a = ma5a;
      ma7a.setTransactionIdentifier(tid5a);
      client.processUndo(ma7a);

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
      addToLog("Finished MGV_ADHOCTest at " +
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