/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MVS_RX3Test
 * 
 * 12/07/2006 BAC (1-D0BIJ): Replace "RXNORM_2005AC" with current version (via lookup)
y * 04/07/2006 RBE (1-AV8WP): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.ic;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.MVS_RX3;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class MVS_RX3Test extends TestSuite {

	/**
	 * Instantiates an empty {@link MVS_RX3Test}.
	 */
	public MVS_RX3Test() {
		setName("MVS_RX3Test");
		setDescription("Test suite for MVS_RX3 integrity");
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
			AuxiliaryDataClient adc = new AuxiliaryDataClient(client
					.getMidService());
			MVS_RX3 ic = (MVS_RX3) adc.getIntegrityCheck("MVS_RX3");

			//
			// create data set-up to test all logic
			//

			Concept source = client.getConcept(101);
			Concept target = client.getConcept(102);
			Atom[] atoms = source.getAtoms();

			//
			// 1a. Validate concept
			//
			addToLog("    1a. Validate a concept ... "
					+ date_format.format(timestamp));

			addToLog("        Should not be a violation. No rxcui atoms from source/target found ...");
			if (!ic.validate(source, target, atoms))
				addToLog("    1a. Test Passed");
			else {
				addToLog("    1a. Test Failed");
				thisTestFailed();
			}

			//
			// 2a. Insert source concept
			//
			addToLog("    2a. Insert source concept ... "
					+ date_format.format(timestamp));

			// Set up source concept
			source = new Concept.Default();

      Source[] sources = client.getSources();
      Source src = null;
      for (int i=0; i<sources.length; i++) {
      	if (sources[i].getStrippedSourceAbbreviation().equals("RXNORM") &&
      			sources[i].isCurrent()) {
      		src = sources[i];
      	  break;
      	}
      }
      
      // Create an atom
			Atom atom = new Atom.Default();
			atom.setString("RXNORM");
      atom.setTermgroup(client.getTermgroup(src.getSourceAbbreviation() + "/BN"));
      atom.setSource(src);
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
			// source = client.getConcept(source);
			Concept concept = client.getConcept(source);
			atoms = concept.getAtoms();

			// Save Transaction ID
			int tid2a = client.getTransaction().getIdentifier().intValue();
			addToLog("        Transaction ID: " + tid2a);

			addToLog("        Should be a violation. Found a moving atom containing a primary atom ...");
			if (ic.validate(source, target, atoms))
				addToLog("    2a. Test Passed");
			else {
				addToLog("    2a. Test Failed");
				thisTestFailed();
			}

			//
			// 3a. Undo 2a
			//
			addToLog("    3a. Undo 2a ... " + date_format.format(timestamp));

			MolecularAction m3a = ma2a;
			m3a.setTransactionIdentifier(tid2a);
			client.processUndo(m3a);

			//
			// 4a. Insert source concept
			//
			addToLog("    4a. Insert source concept ... "
					+ date_format.format(timestamp));

			// Set up source concept
			source = new Concept.Default();

			// Create an atom
			atom = new Atom.Default();
			atom.setString("RXCUI");
			atom.setTermgroup(client.getTermgroup("MTH/PT"));
			atom.setSource(client.getSource("MTH"));
			atom.setStatus('R');
			atom.setGenerated(true);
			atom.setReleased('N');
			atom.setTobereleased('Y');
			atom.setSuppressible("N");
			atom.setConcept(source);

			// Create an attribute
			Attribute attr = new Attribute.Default();
			attr.setAtom(atom);
			attr.setLevel('S');
			attr.setName("RXCUI");
			attr.setValue("RX000001");
			attr.setSource(client.getSource("MTH"));
			attr.setStatus('R');
			attr.setGenerated(false);
			attr.setReleased('A');
			attr.setTobereleased('Y');
			attr.setSuppressible("N");
			attr.setConcept(source);
			atom.addAttribute(attr);

			source.addAtom(atom);
			source.addAttribute(attr);

			MolecularAction ma4a = new MolecularInsertConceptAction(source);
			client.processAction(ma4a);

			// re-read concept
			concept = client.getConcept(source);
			atoms = concept.getAtoms();

			// Save Transaction ID
			int tid4a = client.getTransaction().getIdentifier().intValue();
			addToLog("        Transaction ID: " + tid4a);

			addToLog("        Should be a violation. Found an RXCUI merge ...");
			if (ic.validate(source, target, atoms))
				addToLog("    4a. Test Passed");
			else {
				addToLog("    4a. Test Failed");
				thisTestFailed();
			}

			addToLog("        Should not be a violation. RXCUI merge not found ...");
			if (!ic.validate(concept, target, atoms))
				addToLog("    4b. Test Passed");
			else {
				addToLog("    4b. Test Failed");
				thisTestFailed();
			}

			//
			// 5a. Undo 4a
			//
			addToLog("    5a. Undo 4a ... " + date_format.format(timestamp));

			MolecularAction m5a = ma4a;
			m5a.setTransactionIdentifier(tid4a);
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
			addToLog("Finished MVS_RX3Test at "
					+ date_format.format(new Date(System.currentTimeMillis())));
			addToLog("-------------------------------------------------------");

		} catch (MEMEException e) {
			thisTestFailed();
			addToLog(e);
			e.setPrintStackTrace(true);
			e.printStackTrace();
		}
	}
}