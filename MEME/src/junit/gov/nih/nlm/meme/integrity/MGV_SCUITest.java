/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_SCUITest
 * 
 * 07/12/2006 RBE (1-BKQVT): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.integrity.MGV_SCUI;
import gov.nih.nlm.meme.integrity.UnaryCheckData;
import junit.framework.TestCase;

public class MGV_SCUITest extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private Atom[] atoms = null;
	private MGV_SCUI ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		ic = new MGV_SCUI();
		UnaryCheckData[] ucds = new UnaryCheckData[] {
				new UnaryCheckData("MGV_SCUI", "SOURCE", "MTH", false) };
		ic.setCheckData(ucds);

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success  
	//

	public void testEmptyTargetConcept() {
		createSourceConceptSetSAB();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target));
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target, atoms));
	}

	public void testEmptySourceConcept() {
		source = new Concept.Default();
		atoms = new Atom[] {};
		createTargetConceptSetSAB();
		assertTrue("No violation expected because source concept is empty", 
				!ic.validate(source, target));
		assertTrue("No violation expected because source concept is empty", 
				!ic.validate(source, target, atoms));
	}

	public void testDifferentSources() {
		createSourceConceptSetSAB();
		createTargetConceptSetSAB();
		assertTrue("No violation expected because SAB values do not match", 
				!ic.validate(source, target));
		assertTrue("No violation expected because SAB values do not match", 
				!ic.validate(source, target, atoms));
	}

	public void testExpectedViolation() {
		createSourceConceptSetSCUI();
		createTargetConceptSetSCUI();
		assertTrue("Violation expected because SABs match but SCUIs are different",
				ic.validate(source, target));
		assertTrue("Violation expected because SABs match but SCUIs are different",
				ic.validate(source, target, atoms));
	}

	public void testSourceSCUINull() {
		createSourceConceptSetSCUINull();
		createTargetConceptSetSCUI();
		assertTrue("No error expected due to null SCUI",
				ic.validate(source, target));
		assertTrue("No error expected due to null SCUI",
				ic.validate(source, target, atoms));
	}

	public void testTargetSCUINull() {
		createTargetConceptSetSCUINull();
		createSourceConceptSetSCUINull();
		assertTrue("No violation expected because target atom descriptor is null",
				!ic.validate(source, target));
		assertTrue("No violation expected because target atom descriptor is null",
				!ic.validate(source, target, atoms));
	}

	//
	// Test Cases
	//

	private void createSourceConceptSetSAB() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setSource(new Source.Default("MSH"));
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] {atom};
	}

	private void createTargetConceptSetSAB() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setSource(new Source.Default("MTH"));
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetSCUI() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setSourceConceptIdentifier(new Identifier.Default(12345));
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] {atom};
	}

	private void createTargetConceptSetSCUI() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setSourceConceptIdentifier(new Identifier.Default(12346));
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetSCUINull() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setSourceConceptIdentifier(null);
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] {atom};
	}

	private void createTargetConceptSetSCUINull() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setSourceConceptIdentifier(null);
		atom.setConcept(target);
		target.addAtom(atom);
	}

	//
	// Helper method(s)
	//

	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("TEST MGV_SCUI ATOM");
		atom.setTermgroup(new Termgroup.Default("MTH/PT"));
		atom.setSource(new Source.Default("MTH"));
		atom.setCode(Code.newCode("NOCODE"));
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setTobereleased('Y');
		atom.setSuppressible("N");
		atom.setSourceConceptIdentifier(new Identifier.Default(12345));
		return atom;
	}

}