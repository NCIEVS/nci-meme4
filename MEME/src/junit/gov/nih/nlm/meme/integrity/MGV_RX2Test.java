/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_RX2Test
 * 05/24/2007 BAC (1-EB7O5)
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.integrity.MGV_RX2;
import junit.framework.TestCase;

public class MGV_RX2Test extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private Atom[] atoms = null;
	private MGV_RX2 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		atoms = null;
		ic = new MGV_RX2();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of MGV_RX2");
		ic.setDescription("Description of MGV_RX2.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success  
	//

	public void testEmptyTargetConcept() {
		System.out.println("MGV_RX2Test#testEmptyTargetConcept()");
		createSourceConceptSetSCD();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because target concept is empty", !ic
				.validate(source, target, atoms));
	}

	public void testEmptySourceConcept() {
		System.out.println("MGV_RX2Test#testEmptySourceConcept()");
		source = new Concept.Default();
		createTargetConceptSetSCD();
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because source concept is empty", !ic.validate(source, target, atoms));
	}

	public void testViolationFound() {
		System.out.println("MGV_RX2Test#testViolationFound()");
		createTargetConceptSetSCD();
		createSourceConceptSetSCD();
		assertTrue("Violation expected.", ic.validate(source, target));
		assertTrue("Violation expected.", ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundObsoleteSource() {
		System.out.println("MGV_RX2Test#testViolationFound()");
		createTargetConceptSetSCD();
		createSourceConceptSetSCD();
		source.getAtoms()[0].setSuppressible("O");
		assertTrue("Violation not expected.", !ic.validate(source, target));
		assertTrue("Violation not expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundObsoleteTarget() {
		System.out.println("MGV_RX2Test#testViolationFound()");
		createTargetConceptSetSCD();
		createSourceConceptSetSCD();
		target.getAtoms()[0].setSuppressible("O");
		assertTrue("Violation not expected.", !ic.validate(source, target));
		assertTrue("Violation not expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundOCD() {
		System.out.println("MGV_RX2Test#testViolationNotFoundOCD()");
		createSourceConceptSetOCD();
		createTargetConceptSetOCD();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundSCDC() {
		System.out.println("MGV_RX2Test#testViolationNotFoundSCDC()");
		createSourceConceptSetRXNSCDC();
		createTargetConceptSetSCD();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundRXNSCD() {
		System.out.println("MGV_RX2Test#testViolationNotFoundRXNSCD()");
		createSourceConceptSetSCD();
		createTargetConceptSetRXNSCD();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	//
	// Test Cases
	//

	private void createSourceConceptSetSCD() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/SCD"));
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetSCD() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/SCD"));
		atom.setConcept(target);
		target.addAtom(atom);
		atoms = new Atom[] { };
	}

	private void createSourceConceptSetOCD() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/OCD"));
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetOCD() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/OCD"));
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetRXNSCDC() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setString("RXN");
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/SCDC"));
		Source src = new Source.Default("RXN_2005AC");
		src.setRootSourceAbbreviation("RXN");
		atom.setSource(src);
		source.addAtom(atom);
		atom.setConcept(source);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetRXNSCD() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setString("RXN");
		atom.setTermgroup(new Termgroup.Default("RXN_2005AC/SCD"));
		Source src = new Source.Default("RXN_2005AC");
		src.setRootSourceAbbreviation("RXN");
		atom.setSource(src);
		target.addAtom(atom);
		atom.setConcept(target);
	}

	//
	// Helper method(s)
	//

	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("RXNORM");
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/ET"));
		Source src = new Source.Default("RXNORM_2005AC");
		src.setRootSourceAbbreviation("RXNORM");
		atom.setSource(src);
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setTobereleased('Y');
		atom.setSuppressible("N");
		atom.setConcept(source);
		return atom;
	}

}