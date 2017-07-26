/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_DTest
 * 
 * 01/05/2006 RBE (1-72TW3): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.MGV_D;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MGV_DTest extends TestSuite{

  /**
   * Instantiates an empty {@link MGV_DTest}.
   */	
  public MGV_DTest() {
    setName("MGV_DTest");
    setDescription("Test suite for MGV_D integrity");
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
      MGV_D ic = (MGV_D) adc.getIntegrityCheck("MGV_D");

      //
      // create data set-up to test all logic
      //
      
      Concept source = client.getConcept(101);
      Concept target = client.getConcept(102);

      //
      // 1a. Validate source and target concept
      //
      addToLog("    1a. Validate a concept ... " + date_format.format(timestamp));
      
      addToLog("        Should not be a violation. No current MSH entry terms found ...");
      if (!ic.validate(source, target))
        addToLog("    1a. Test Passed");
      else {
        addToLog("    1a. Test Failed");
        thisTestFailed();
      }

      //
      // Get a current mesh sources
      //
      Source[] sources = client.getCurrentSources();
      Source src = null;
      for (int i=0; i<sources.length; i++) {
      	if (sources[i].getStrippedSourceAbbreviation().equals("MSH")) {
      		src = sources[i];
      	  break;
      	}
      }

      //
      // 2a. Insert a source concept
      //
      addToLog("    2a. Insert a source concept ... " + date_format.format(timestamp));

      source = client.getConcept(101);

      // Create an atom
      MeshEntryTerm met1 = new MeshEntryTerm();

      met1.setString("TEST ATOM 2");
      met1.setTermgroup(client.getTermgroup(src.getSourceAbbreviation()+"/EN"));
      met1.setSource(src);
      met1.setCode(Code.newCode("D000959"));
      met1.setStatus('R');
      met1.setGenerated(true);
      met1.setReleased('N');
      met1.setTobereleased('Y');
      met1.setSuppressible("N");
      met1.setConcept(source);
      met1.setMainHeading(client.getAtom(101));
      
      source.addAtom(met1);

      MolecularAction ma2a = new MolecularInsertAtomAction(met1);
      client.processAction(ma2a);
      
      // re-read concept
      source = client.getConcept(source);
      met1 = (MeshEntryTerm) client.getAtom(met1);
    
      // Save Transaction ID
      int tid2a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2a);

      //
      // 2b. Insert a target concept
      //
      addToLog("    2b. Insert a target concept ... " + date_format.format(timestamp));

      target = client.getConcept(102);

      // Create an atom
      MeshEntryTerm met2 = new MeshEntryTerm();

      met2.setString("TEST ATOM 2");
      met2.setTermgroup(client.getTermgroup(src.getSourceAbbreviation()+"/EN"));
      met2.setSource(src);
      met2.setCode(Code.newCode("D000959"));
      met2.setStatus('R');
      met2.setGenerated(true);
      met2.setReleased('N');
      met2.setTobereleased('Y');
      met2.setSuppressible("N");
      met2.setConcept(target);
      met2.setMainHeading(client.getAtom(102));
      
      target.addAtom(met2);

      MolecularAction ma2b = new MolecularInsertAtomAction(met2);
      client.processAction(ma2b);
      
      // re-read concept
      target = client.getConcept(target);
      met2 = (MeshEntryTerm) client.getAtom(met2);
    
      // Save Transaction ID
      int tid2b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid2b);

      //
      // 3a. Insert a source relationship
      //
      addToLog("    3a. Insert a source relationship ... " + date_format.format(timestamp));

      Atom mh1 = ( (MeshEntryTerm) met1).getMainHeading();
      
      Relationship rel = new Relationship.Default();
      rel.setAtom(client.getAtom(101));
      rel.setRelatedAtom(mh1);
      rel.setConcept(client.getConcept(101));
      rel.setRelatedConcept(client.getConcept(102));
      rel.setName("RT");
      rel.setAttribute("mapped_to");
      rel.setSource(src);
      rel.setSourceOfLabel(src);
      rel.setStatus('N');
      rel.setGenerated(false);
      rel.setLevel('S');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      source.addRelationship(rel);    

      MolecularAction ma3a = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma3a);
      
      // re-read concept
      source = client.getConcept(source);
      client.populateRelationships(source);

      // Save Transaction ID
      int tid3a = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3a);

      //
      // 3b. Insert a target relationship
      //
      addToLog("    3b. Insert a target relationship ... " + date_format.format(timestamp));

      Atom mh2 = ( (MeshEntryTerm) met2).getMainHeading();
      
      rel = new Relationship.Default();
      rel.setAtom(client.getAtom(101));
      rel.setRelatedAtom(mh2);
      rel.setConcept(client.getConcept(101));
      rel.setRelatedConcept(client.getConcept(102));
      rel.setName("BT");
      rel.setAttribute("mapped_to");
      rel.setSource(src);
      rel.setSourceOfLabel(src);
      rel.setStatus('N');
      rel.setGenerated(false);
      rel.setLevel('S');
      rel.setReleased('A');
      rel.setTobereleased('Y');
      rel.setSuppressible("N");
      rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
      
      target.addRelationship(rel);    

      MolecularAction ma3b = new MolecularInsertRelationshipAction(rel);
      client.processAction(ma3b);
      
      addToLog("        Should be a violation. Found cases where the source concept has a relationship ");
      addToLog("        to the same main heading as the target concept where the relationship names are different ...");
      if (ic.validate(source, target))
        addToLog("    3b. Test Passed");
      else {
        addToLog("    3b. Test Failed");
        thisTestFailed();
      }

      // re-read concept
      target = client.getConcept(target);
      client.populateRelationships(target);

      // Save Transaction ID
      int tid3b = client.getTransaction().getIdentifier().intValue();
      addToLog("        Transaction ID: " + tid3b);

      //
      // 3c. Undo 3b
      //
      addToLog("    3c. Undo 3b ... "
               + date_format.format(timestamp));

      MolecularAction ma3c = ma3b;
      ma3c.setTransactionIdentifier(tid3b);
      client.processUndo(ma3c);

      //
      // 3d. Undo 3a
      //
      addToLog("    3d. Undo 3a ... "
               + date_format.format(timestamp));

      MolecularAction ma3d = ma3a;
      ma3d.setTransactionIdentifier(tid3a);
      client.processUndo(ma3d);

      //
      // 4a. Undo 2b
      //
      addToLog("    4a. Undo 2b ... "
               + date_format.format(timestamp));

      MolecularAction ma4a = ma2b;
      ma4a.setTransactionIdentifier(tid2b);
      client.processUndo(ma4a);

      //
      // 4b. Undo 2a
      //
      addToLog("    4b. Undo 2a ... "
               + date_format.format(timestamp));

      MolecularAction ma4b = ma2a;
      ma4b.setTransactionIdentifier(tid2a);
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
      addToLog("Finished MGV_DTest at " +
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