/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I3BTest
 *
 * 01/05/2006 RBE (1-72TW3): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.DT_I3B;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DT_I3BTest extends TestSuite{

  /**
   * Instantiates an empty {@link DT_I3BTest}.
   */	
  public DT_I3BTest() {
    setName("DT_I3BTest");
    setDescription("Test suite for DT_I3B integrity");
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
      DT_I3B ic = (DT_I3B) adc.getIntegrityCheck("DT_I3B");

      //
      // create data set-up to test all logic
      //
      
      Concept concept = client.getConcept(101);

      //
      // 1a. Validate concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));

      addToLog("        Should not be a violation. No demotion concept found ...");
      if (!ic.validate(concept))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // 2a. Insert a C level relationship
      //
      addToLog("    2a. Insert a C level relationship ... " + date_format.format(timestamp));

      Relationship rel = new Relationship.Default();
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(103));
      rel.setAtom(client.getAtom(101));
      rel.setRelatedAtom(client.getAtom(103));
      rel.setName("LK");
      rel.setAttribute("translation_of");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('R');
      rel.setGenerated(false);
      rel.setLevel('C');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma2a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma2a);

      // re-read concept
      concept = client.getConcept(concept);
      client.populateRelationships(concept);
      
      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);
      
      //
      // 3a. Insert a demotion relationship
      //
      addToLog("    3a. Insert a demotion relationship ... " + date_format.format(timestamp));

      rel = new Relationship.Default();
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(104));
      rel.setAtom(client.getAtom(101));
      rel.setRelatedAtom(client.getAtom(103));
      rel.setName("LK");
      rel.setAttribute("translation_of");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('D');
      rel.setGenerated(false);
      rel.setLevel('P');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma3a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma3a);

      // re-read concept
      concept = client.getConcept(concept);
      client.populateRelationships(concept);
      
      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);
      
      addToLog("        Should not be a violation. Found a matching C level demotion relationship ...");
      if (!ic.validate(concept))
        addToLog("    3a. Test Passed");
      else {
        addToLog("    3a. Test Failed");
        thisTestFailed();
      }

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
      // 4a. Insert a demotion relationship
      //
      addToLog("    4a. Insert a demotion relationship ... " + date_format.format(timestamp));

      rel = new Relationship.Default();
      rel.setConcept(concept);
      rel.setRelatedConcept(client.getConcept(103));
      rel.setAtom(client.getAtom(101));
      rel.setRelatedAtom(client.getAtom(103));
      rel.setName("LK");
      rel.setAttribute("translation_of");
      rel.setSource(client.getSource("MTH"));
      rel.setSourceOfLabel(client.getSource("MTH"));
      rel.setStatus('D');
      rel.setGenerated(false);
      rel.setLevel('P');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      concept.addRelationship(rel);    

      MolecularAction ma4a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma4a);

      // re-read concept
      concept = client.getConcept(concept);
      client.populateRelationships(concept);
      
      // Save Transaction ID
      int tid4a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid4a);
      
      addToLog("        Should be a violation. Demotion found but a matching C level relationship was not found ...");
      if (ic.validate(concept))
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
      addToLog("Finished DT_I3BTest at " +
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