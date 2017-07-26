/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_MM3Test
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

public class DT_MM3Test extends TestCase {

	private Concept source = null;
	private DT_MM3 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_MM3();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_MM3");
		ic.setDescription("Description of DT_MM3.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNoAtoms() {
		System.out.println("DT_MM3Test#testViolationNotFoundNoAtoms()");
		createConcept();
		assertTrue("No violation expected. No atoms attached to concept.", !ic.validate(source));
	}

	public void testViolationFound() {
		System.out.println("DT_MM3Test#testViolationFound()");
		createSourceConcept();
		assertTrue("Violation expected.", ic.validate(source));
	}

	public void testViolationNotFoundSetTTY() {
		System.out.println("DT_MM3Test#testViolationNotFoundSetTTY()");
		createSourceConceptSetTTY();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationNotFoundSetTBR() {
		System.out.println("DT_MM3Test#testViolationNotFoundSetTBR()");
		createSourceConceptSetTBR();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationNotFoundSetAtom2() {
		System.out.println("DT_MM3Test#testViolationNotFoundSetAtom2()");
		createSourceConceptSetAtom2();
		assertTrue("No violation expected. No MTH/MM atom without matching ambiguous atom found", !ic.validate(source));
	}

	public void testViolationFoundSetString() {
		System.out.println("DT_MM3Test#testViolationFoundSetString()");
		createSourceConceptSetString();
		assertTrue("No violation expected. Found MTH/MM atom without matching ambiguous atom", ic.validate(source));
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

	private void createSourceConceptSetString() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(source);
		source.addAtom(atom);
		atom = createAtom2();
		atom.setString("NON-NEC");
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createSourceConceptSetAtom2() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(source);
		source.addAtom(atom);
		atom = createAtom2();
		atom.setConcept(source);
		source.addAtom(atom);
	}

	//
	// Helper method(s)
	//
	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("NEC");
		atom.setTermgroup(new Termgroup.Default("MTH2005/MM"));		
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

	private Atom createAtom2() {
		Atom atom = new Atom.Default();
		atom.setString("NEC");
		atom.setTermgroup(new Termgroup.Default("MTH2005/EN"));		
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