/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I2Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Termgroup;
import junit.framework.TestCase;

public class DT_I2Test extends TestCase {

	private Concept source = null;
	private DT_I2 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_I2();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_I2");
		ic.setDescription("Description of DT_I2.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNoAtoms() {
		System.out.println("DT_I2Test#testViolationNotFoundNoAtoms()");
		createConcept();
		assertTrue("No violation expected. No atoms attached to concept.", !ic.validate(source));
	}

	public void testViolationFound() {
		System.out.println("DT_I2Test#testViolationFound()");
		createSourceConcept();
		assertTrue("Violation expected.", ic.validate(source));
	}

	public void testViolationNotFoundSetTBR() {
		System.out.println("DT_I2Test#testViolationNotFoundSetTBR()");
		createSourceConceptSetTBR();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationNotFoundSetAuthority() {
		System.out.println("DT_I2Test#testViolationNotFoundSetAuthority()");
		createSourceConceptSetAuthority();
		assertTrue("No violation expected.", !ic.validate(source));
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

	private void createSourceConceptSetAuthority() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setAuthority(new Authority.Default("NON-NLM"));
		atom.setConcept(source);
		source.addAtom(atom);
	}

	//
	// Helper method(s)
	//

	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("TEST ATOM");
		atom.setTermgroup(new Termgroup.Default("MSH/PM"));
		atom.setAuthority(new Authority.Default("ENG-NLM"));
		atom.setStatus('R');
		atom.setCode(Code.newCode("NOCODE"));		
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
		atom.setConcept(source);
		return atom;
	}
	
}