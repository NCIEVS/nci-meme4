/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_MMTest
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import junit.framework.TestCase;

public class MGV_MMTest extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private Atom[] atoms = null;
	private MGV_MM ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		atoms = null;
		ic = new MGV_MM();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of MGV_MM");
		ic.setDescription("Description of MGV_MM.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testEmptyTargetConcept() {
		System.out.println("MGV_MMTest#testEmptyTargetConcept()");
		createSourceConcept();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target));
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target, atoms));
	}

	public void testEmptySourceConcept() {
		System.out.println("MGV_MMTest#testEmptySourceConcept()");
		source = new Concept.Default();
		createTargetConceptNoAtoms();
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target, atoms));
	}

	public void testViolationFound() {
		System.out.println("MGV_MMTest#testViolationFound()");
		createSourceConcept();
		createTargetConcept();
		assertTrue("Violation expected.", ic.validate(source, target));
		assertTrue("Violation expected.", ic.validate(source, target, atoms));
	}
	public void testViolationNotFoundNonReleasable() {
		System.out.println("MGV_MMTest#testViolationNotFoundNonReleasable()");
		createSourceConceptSetTBR();
		createTargetConceptSetTBR();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundSetTTY() {
		System.out.println("MGV_MMTest#testViolationNotFoundSetTTY()");
		createSourceConceptSetTTY();
		createTargetConceptSetTTY();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	//
	// Test Cases
	//

	private void createSourceConcept() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConcept() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(target);
		target.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptNoAtoms() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(target);
		target.addAtom(atom);
		atoms = new Atom[] { };
	}

	private void createSourceConceptSetTBR() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setTobereleased('N');
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetTBR() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setTobereleased('N');
		atom.setConcept(target);
		target.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createSourceConceptSetTTY() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setTermgroup(new Termgroup.Default("MTH/PP"));		
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetTTY() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setTermgroup(new Termgroup.Default("MTH/PP"));		
		atom.setConcept(target);
		target.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	//
	// Helper method(s)
	//
	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("NEC");
		atom.setTermgroup(new Termgroup.Default("MTH/MM"));		
		Source src = new Source.Default("MTH");
		src.setStrippedSourceAbbreviation("MTH");
		atom.setSource(src);
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
		atom.setConcept(source);
		return atom;
	}

}