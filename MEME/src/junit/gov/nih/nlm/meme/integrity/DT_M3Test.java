/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_M3Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import junit.framework.TestCase;

public class DT_M3Test extends TestCase {

	private Concept source = null;
	private DT_M3 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_M3();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_M3");
		ic.setDescription("Description of DT_M3.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationFoundNoAtoms() {
		System.out.println("DT_M3Test#testViolationFoundNoAtoms()");
		createConcept();
		assertTrue("Violation expected. No atoms attached to concept.", ic.validate(source));
	}

	public void testViolationNotFound() {
		System.out.println("DT_M3Test#testViolationNotFound()");
		createSourceConcept();
		assertTrue("No violation expected. Found releasable atom", !ic.validate(source));
	}

	public void testViolationFoundSetTBR() {
		System.out.println("DT_M3Test#testViolationFoundSetTBR()");
		createSourceConceptSetTBR();
		assertTrue("Violation expected. No releasable atom found", ic.validate(source));
	}

	//
	// Test Cases
	//

	private void createConcept() {
		source = new Concept.Default();
	}

	private void createSourceConcept() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createSourceConceptSetTBR() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setTobereleased('N');
		atom.setConcept(source);
		source.addAtom(atom);
	}

	//
	// Helper method(s)
	//
	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("NEC");
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
		return atom;
	}

}