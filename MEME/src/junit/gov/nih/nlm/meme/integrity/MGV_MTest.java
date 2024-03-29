/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_MTest
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import junit.framework.TestCase;

public class MGV_MTest extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private Atom[] atoms = null;
	private MGV_M ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		atoms = null;
		ic = new MGV_M();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of MGV_M");
		ic.setDescription("Description of MGV_M.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testEmptyTargetConcept() {
		System.out.println("MGV_MTest#testEmptyTargetConcept()");
		createSourceConcept();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target));
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target, atoms));
	}

	public void testEmptySourceConcept() {
		System.out.println("MGV_MTest#testEmptySourceConcept()");
		source = new Concept.Default();
		createTargetConcept();
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target, atoms));
	}

	public void testViolationFound() {
		System.out.println("MGV_MTest#testViolationFound()");
		createSourceConcept();
		createTargetConceptSetSAB();
		assertTrue("Violation expected.", ic.validate(source, target));
		assertTrue("Violation expected.", ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundSameSAB() {
		System.out.println("MGV_MTest#testViolationNotFoundSameSAB()");
		createSourceConceptSetSAB();
		createTargetConceptSetSAB();
		assertTrue("Violation expected.", !ic.validate(source, target));
		assertTrue("Violation expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundNonSetLanguage() {
		System.out.println("MGV_MTest#testViolationNotFoundSetLanguage()");
		createSourceConceptSetLanguage();
		createTargetConceptSetLanguage();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundNonReleasable() {
		System.out.println("MGV_MTest#testViolationNotFoundNonReleasable()");
		createSourceConceptSetTBR();
		createTargetConceptSetTBR();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundNonNEC() {
		System.out.println("MGV_MTest#testViolationNotFoundNonNEC()");
		createSourceConceptSetString();
		createTargetConceptSetString();
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

	private void createSourceConceptSetLanguage() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(source);
		atom.setConcept(target);
		atom.setLanguage(new Language.Default("FRANCE", "FRA"));
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetLanguage() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(target);
		atom.setLanguage(new Language.Default("SPANISH", "SPA"));
		target.addAtom(atom);
	}

	private void createSourceConceptSetString() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(source);
		atom.setString("NON NEC");
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetString() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(target);
		atom.setString("NON NEC");
		target.addAtom(atom);
	}

	private void createSourceConceptSetSAB() {
		source = new Concept.Default();
		Atom atom = createAtom();
		Source src = new Source.Default("MSH2005");
		src.setStrippedSourceAbbreviation("MMM");
		atom.setSource(src);
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptSetSAB() {
		target = new Concept.Default();
		Atom atom = createAtom();
		Source src = new Source.Default("MSH2005");
		src.setStrippedSourceAbbreviation("MMM");
		atom.setSource(src);
		atom.setConcept(target);
		target.addAtom(atom);
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

	//
	// Helper method(s)
	//
	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("NEC");
		atom.setTermgroup(new Termgroup.Default("MSH2005/MH"));		
		Source src = new Source.Default("MSH2005");
		src.setStrippedSourceAbbreviation("MSH");
		src.setIsCurrent(true);
		src.setIsPrevious(true);
		atom.setSource(src);
		atom.setStatus('R');
		atom.setLanguage(new Language.Default("ENGLISH", "ENG"));
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
		atom.setCode(new Code("C12345"));
		atom.setConcept(source);
		return atom;
	}

}