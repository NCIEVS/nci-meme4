/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_BTest
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
import gov.nih.nlm.meme.integrity.UnaryCheckData;
import junit.framework.TestCase;

public class MGV_BTest extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private Atom[] atoms = null;
	private MGV_B ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		ic = new MGV_B();
		UnaryCheckData[] ucds = new UnaryCheckData[] {
				new UnaryCheckData("MGV_B", "SOURCE", "MTH", false) };
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
		createSourceConcept();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target));
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target, atoms));
	}

	public void testEmptySourceConcept() {
		source = new Concept.Default();
		atoms = new Atom[] {};
		createTargetConcept();
		assertTrue("No violation expected because source concept is empty", 
				!ic.validate(source, target));
		assertTrue("No violation expected because source concept is empty", 
				!ic.validate(source, target, atoms));
	}

	public void testViolationFound() {
		createSourceConcept();
		createTargetConcept();
		assertTrue("Violation expected.", ic.validate(source, target));
		assertTrue("Violation expected.", ic.validate(source, target, atoms));
	}
	
	public void testViolationNotFoundSetSource() {
		createSourceConceptSetSource();
		createTargetConcept();
		assertTrue("No violation expected because source value do not match", 
				!ic.validate(source, target));
		assertTrue("No violation expected because source value do not match", 
				!ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundSetTBR() {
		createSourceConceptSetTBR();
		createTargetConcept();
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
		atoms = new Atom[] {atom};
	}

	private void createTargetConcept() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetSource() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setSource(new Source.Default("MSH"));
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] {atom};
	}

	private void createSourceConceptSetTBR() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setTobereleased('N');
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] {atom};
	}

	//
	// Helper method(s)
	//

	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("TEST MGV_B ATOM");
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