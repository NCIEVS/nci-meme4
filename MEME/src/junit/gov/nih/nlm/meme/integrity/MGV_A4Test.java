/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_A4Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.integrity.MGV_A4;
import junit.framework.TestCase;

public class MGV_A4Test extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private Atom[] atoms = null;
	private MGV_A4 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		atoms = null;
		ic = new MGV_A4();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of MGV_A4");
		ic.setDescription("Description of MGV_A4.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testEmptyTargetConcept() {
		System.out.println("MGV_A4Test#testEmptyTargetConcept()");
		createSourceConceptSetCUI();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target));
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target, atoms));
	}

	public void testEmptySourceConcept() {
		System.out.println("MGV_A4Test#testEmptySourceConcept()");
		source = new Concept.Default();
		createTargetConceptSetCUI();
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target, atoms));
	}

	public void testViolationFound() {
		System.out.println("MGV_A4Test#testViolationFound()");
		createSourceConceptSetCUI();
		createTargetConceptSetCUINoAtoms();
		assertTrue("Violation expected.", ic.validate(source, target));
		assertTrue("Violation expected.", ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundNonReleasableSource() {
		System.out.println("MGV_A4Test#testViolationNotFoundNonReleasableSource()");
		createNonReleasableSourceConcept();
		createNonReleasableTargetConcept();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundSameCUI2() {
		System.out.println("MGV_A4Test#testViolationNotFoundSameCUI2()");
		createSourceConceptSetCUI2();
		createTargetConceptSetCUI2();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundDiffTBRAndCUI() {
		System.out.println("MGV_A4Test#testViolationNotFoundDiffTBRAndCUI()");
		createSourceConceptSetDiffTBRAndCUI();
		createTargetConceptSetDiffTBRAndCUI();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	//
	// Test Cases
	//

	private void createSourceConceptSetCUI() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000001"));
		atom.setTobereleased('Y');
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetCUI() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000002"));
		atom.setTobereleased('Y');
		atom.setConcept(target);
		target.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetCUINoAtoms() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000002"));
		atom.setTobereleased('Y');
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createNonReleasableSourceConcept() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000001"));
		atom.setTobereleased('N');
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createNonReleasableTargetConcept() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000002"));
		atom.setTobereleased('Y');
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetCUI2() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000002"));
		atom.setTobereleased('Y');
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetCUI2() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000002"));
		atom.setTobereleased('Y');
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetDiffTBRAndCUI() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000001"));
		atom.setTobereleased('Y');
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetDiffTBRAndCUI() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setLastReleaseCUI(new CUI("C000002"));
		atom.setTobereleased('N');
		atom.setConcept(target);
		target.addAtom(atom);
	}

	//
	// Helper method(s)
	//
	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("TEST ATOM");
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setConcept(source);
		return atom;
	}

}