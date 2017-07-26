/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I8Test
 * 
 * 01/05/2006 RBE (1-72TW3): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.DT_I8;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DT_I8Test extends TestSuite{

  /**
   * Instantiates an empty {@link DT_I8Test}.
   */	
  public DT_I8Test() {
    setName("DT_I8Test");
    setDescription("Test suite for DT_I8 integrity");
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
      DT_I8 ic = (DT_I8) adc.getIntegrityCheck("DT_I8");

      //
      // create data set-up to test all logic
      //
      
      Concept concept = client.getConcept(101);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));
      
      ic.setTermType("MH");

      addToLog("        Should not be a violation. No current MSH entry terms found ...");
      if (!ic.validate(concept))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert a concept with a EN TTY
      //
      addToLog("    2a. Insert a concept with a EN TTY ... " + date_format.format(timestamp));

      concept = new Concept.Default();

      Source[] sources = client.getCurrentSources();
      Source source = null;
      for (int i=0; i<sources.length; i++) {
      	if (sources[i].getStrippedSourceAbbreviation().equals("MSH")) {
      		source = sources[i];
      	  break;
      	}
      }
      
      // Create an atom
      MeshEntryTerm met = new MeshEntryTerm();
      
      met.setString("TEST ATOM 2");
      met.setTermgroup(client.getTermgroup(source.getSourceAbbreviation()+"/EN"));
      met.setSource(source);
      met.setCode(Code.newCode("D000959"));
      met.setStatus('R');
      met.setGenerated(true);
      met.setReleased('N');
      met.setTobereleased('Y');
      met.setSuppressible("N");
      met.setConcept(concept);

      met.setMainHeading(client.getAtom(102));
      
      concept.addAtom(met);

      MolecularAction ma2a = new MolecularInsertConceptAction(concept);
      client.processAction(ma2a);
      
      // re-read concept
      concept = client.getConcept(concept);
    
      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      //
      // 3a. Insert a concept with MH with matching code
      //

      // No need to initialize concept
      
      // Create an atom
      met = new MeshEntryTerm();
      met.setString("TEST ATOM 3");
      met.setTermgroup(client.getTermgroup(source.getSourceAbbreviation()+"/MH"));
      met.setSource(source);
      met.setCode(Code.newCode("D000959"));
      met.setStatus('R');
      met.setGenerated(true);
      met.setReleased('N');
      met.setTobereleased('Y');
      met.setSuppressible("N");
      met.setConcept(concept);
      
      concept.addAtom(met);

      addToLog("    3a. Insert a concept with MH ... " + date_format.format(timestamp));

      MolecularAction ma3a = new MolecularInsertConceptAction(concept);
      client.processAction(ma3a);
      
      // re-read concept
      concept = client.getConcept(concept);    
    
      addToLog("        Should not be a violation. Found the same code main heading in the same concept ...");
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

      //
      // 3c. Undo 2a
      //
      addToLog("    3c. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma3c = ma2a;
      ma3c.setTransactionIdentifier(tid2a);
      client.processUndo(ma3c);

      //
      // 4a. Insert a concept with a EN TTY
      //
      addToLog("    4a. Insert a concept with a EN TTY ... " + date_format.format(timestamp));

      concept = client.getConcept(102);

      met.setString("TEST ATOM 2");
      met.setTermgroup(client.getTermgroup(source.getSourceAbbreviation()+"/EN"));
      met.setSource(source);
      met.setCode(Code.newCode("D000959"));
      met.setStatus('R');
      met.setGenerated(true);
      met.setReleased('N');
      met.setTobereleased('Y');
      met.setSuppressible("N");
      met.setConcept(concept);

      met.setMainHeading(client.getAtom(102));
      
      concept.addAtom(met);

      MolecularAction ma4a = new MolecularInsertAtomAction(met);
      client.processAction(ma4a);
      
      // re-read concept
      concept = client.getConcept(concept);
    
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);

      //
      // 5a. Insert a relationship
      //
      addToLog("    5a. Insert a relationship ... " + date_format.format(timestamp));

      Relationship rel = new Relationship.Default();
      rel.setConcept(client.getConcept(101));
      rel.setRelatedConcept(client.getConcept(1001544));
      rel.setName("XR");
      rel.setAttribute("mapped_to");
      rel.setSource(source);
      rel.setSourceOfLabel(source);
      rel.setStatus('R');
      rel.setGenerated(false);
      rel.setLevel('C');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma5a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma5a);
      
      ic.setXR(true);

      addToLog("        Should be a violation. Checked for XR flag ...");
      if (ic.validate(concept))
        addToLog("    5a. Test Passed");
      else {
        addToLog("    5a. Test Failed");
        thisTestFailed();
      }

      // re-read concept
      concept = client.getConcept(concept);
      client.populateRelationships(concept);

      // Save Transaction ID
      int tid5a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid5a);

      //
      // 5b. Undo 5a
      //
      addToLog("    5b. Undo 5a ... "
               + date_format.format(timestamp));

      MolecularAction ma5b = ma5a;
      ma5b.setTransactionIdentifier(tid5a);
      client.processUndo(ma5b);

      //
      // 5c. Undo 4a
      //
      addToLog("    5c. Undo 4a ... "
               + date_format.format(timestamp));

      MolecularAction ma5c = ma4a;
      ma5c.setTransactionIdentifier(tid4a);
      client.processUndo(ma5c);
      
      //
      // 6a. Insert a concept with a EN TTY
      //
      addToLog("    6a. Insert a concept with a EN TTY ... " + date_format.format(timestamp));

      concept = client.getConcept(102);
      met.setMainHeading(client.getAtom(102));
      concept.addAtom(met);

      MolecularAction ma6a = new MolecularInsertAtomAction(met);
      client.processAction(ma6a);
      
      // re-read concept
      concept = client.getConcept(concept);
    
      // Save Transaction ID
      int tid6a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid6a);

      //
      // 7a. Insert a relationship
      //
      addToLog("    7a. Insert a relationship ... " + date_format.format(timestamp));

      rel = new Relationship.Default();
      rel.setConcept(client.getConcept(101));
      rel.setRelatedConcept(client.getConcept(1001544));
      rel.setName("RT");
      rel.setAttribute("mapped_to");
      rel.setSource(source);
      rel.setSourceOfLabel(source);
      rel.setStatus('R');
      rel.setGenerated(false);
      rel.setLevel('C');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma7a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma7a);
      
      ic.setXR(false);

      addToLog("        Should not be a violation. Relationship is MTH asserted and has a valid name ...");
      if (!ic.validate(concept))
        addToLog("    7a. Test Passed");
      else {
        addToLog("    7a. Test Failed");
        thisTestFailed();
      }

      // re-read concept
      concept = client.getConcept(concept);
      client.populateRelationships(concept);

      // Save Transaction ID
      int tid7a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid7a);

      //
      // 7b. Undo 7a
      //
      addToLog("    7b. Undo 7a ... "
               + date_format.format(timestamp));

      MolecularAction ma7b = ma7a;
      ma7b.setTransactionIdentifier(tid7a);
      client.processUndo(ma7b);

      //
      // 7c. Undo 6a
      //
      addToLog("    7c. Undo 6a ... "
               + date_format.format(timestamp));

      MolecularAction ma7c = ma6a;
      ma7c.setTransactionIdentifier(tid6a);
      client.processUndo(ma7c);

      //
      // 8a. Insert a concept with a EN TTY
      //
      addToLog("    8a. Insert a concept with a EN TTY ... " + date_format.format(timestamp));

      concept = client.getConcept(102);
      met.setMainHeading(client.getAtom(102));
      concept.addAtom(met);

      MolecularAction ma8a = new MolecularInsertAtomAction(met);
      client.processAction(ma8a);
      
      // re-read concept
      concept = client.getConcept(concept);
    
      // Save Transaction ID
      int tid8a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid8a);

      //
      // 9a. Insert a relationship
      //
      addToLog("    9a. Insert a relationship ... " + date_format.format(timestamp));

      rel = new Relationship.Default();
      rel.setConcept(client.getConcept(101));
      rel.setRelatedConcept(client.getConcept(1001544));
      rel.setName("XR");
      rel.setAttribute("mapped_to");
      rel.setSource(source);
      rel.setSourceOfLabel(source);
      rel.setStatus('R');
      rel.setGenerated(false);
      rel.setLevel('C');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma9a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma9a);
      
      ic.setXR(false);

      addToLog("        Should be a violation. Same code main heading in related concept not found ...");
      if (ic.validate(concept))
        addToLog("    9a. Test Passed");
      else {
        addToLog("    9a. Test Failed");
        thisTestFailed();
      }

      // re-read concept
      concept = client.getConcept(concept);
      client.populateRelationships(concept);

      // Save Transaction ID
      int tid9a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid9a);

      //
      // 9b. Undo 9a
      //
      addToLog("    9b. Undo 9a ... "
               + date_format.format(timestamp));

      MolecularAction ma9b = ma9a;
      ma9b.setTransactionIdentifier(tid9a);
      client.processUndo(ma9b);

      //
      // 9c. Undo 8a
      //
      addToLog("    9c. Undo 8a ... "
               + date_format.format(timestamp));

      MolecularAction ma9c = ma8a;
      ma9c.setTransactionIdentifier(tid8a);
      client.processUndo(ma9c);

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
      addToLog("Finished DT_I8Test at " +
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