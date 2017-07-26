/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_PN3Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.common.Termgroup;
import junit.framework.TestCase;

public class DT_PN3Test extends TestCase {

	private Concept source = null;
	private DT_PN3 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_PN3();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_PN3");
		ic.setDescription("Description of DT_PN3.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNoAtoms() {
		System.out.println("DT_PN3Test#testViolationNotFoundNoAtoms()");
		createConcept();
		assertTrue("No violation expected. No atoms attached to concept.", !ic.validate(source));
	}

	public void testViolationFound() {
		System.out.println("DT_PN3Test#testViolationFound()");
		createSourceConcept();
		assertTrue("Violation expected.", ic.validate(source));
	}

	public void testViolationNotFoundSetSAB() {
		System.out.println("DT_PN3Test#testViolationNotFoundSetSAB()");
		createSourceConceptSetSAB();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationNotFoundSetTTY() {
		System.out.println("DT_PN3Test#testViolationNotFoundSetTTY()");
		createSourceConceptSetTTY();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationNotFoundSetTBR() {
		System.out.println("DT_PN3Test#testViolationNotFoundSetTBR()");
		createSourceConceptSetTBR();
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

	private void createSourceConceptSetSAB() {
		source = new Concept.Default();
		Atom atom = createAtom();
		Source src = new Source.Default("MTH2005");
		src.setSourceAbbreviation("MSH");
		atom.setSource(src);
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createSourceConceptSetTTY() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setTermgroup(new Termgroup.Default("MTH2005/EN"));		
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
		atom.setTermgroup(new Termgroup.Default("MTH2005/PN"));		
		Source src = new Source.Default("MTH2005");
		src.setSourceAbbreviation("MTH");
		src.setIsCurrent(true);
		src.setIsPrevious(true);
		atom.setSource(src);
		atom.setIsAmbiguous(true);
		atom.setStatus('R');
		atom.setLanguage(new Language.Default("ENGLISH", "ENG"));
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
        atom.setSUI(new StringIdentifier("S0000474"));
		atom.setCode(new Code("C12345"));
		atom.setSourceConceptIdentifier(new Identifier.Default(12345));
		atom.setConcept(source);
		return atom;
	}

}