/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I10Test
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

public class DT_I10Test extends TestCase {

	private Concept source = null;
	private DT_I10 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_I10();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_I10");
		ic.setDescription("Description of DT_I10.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNoAtoms() {
		System.out.println("DT_I10Test#testViolationNotFoundNoAtoms()");
		createConcept();
		assertTrue("No violation expected. No atoms attached to concept.", !ic.validate(source));
	}

	public void testViolationNotFound() {
		System.out.println("DT_I10Test#testViolationNotFound()");
		createSourceConcept();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationNotFoundSetCurrent() {
		System.out.println("DT_I10Test#testViolationNotFoundSetCurrent()");
		createSourceConceptSetCurrent();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationFoundSetPrevious() {
		System.out.println("DT_I10Test#testViolationFoundSetPrevious()");
		createSourceConceptSetPrevious();
		assertTrue("Violation expected.", ic.validate(source));
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

	private void createSourceConceptSetCurrent() {
		source = new Concept.Default();
		Atom atom = createAtom();
		Source src = new Source.Default("MSH2005");
		src.setStrippedSourceAbbreviation("MSH");
		src.setIsCurrent(false);
		atom.setSource(src);
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createSourceConceptSetPrevious() {
		source = new Concept.Default();
		Atom atom = createAtom();
		Source src = new Source.Default("MSH2005");
		src.setStrippedSourceAbbreviation("MSH");
		src.setIsCurrent(true);
		src.setIsPrevious(false);
		atom.setSource(src);
		atom.setConcept(source);
		source.addAtom(atom);
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
        atom.setSUI(new StringIdentifier("S0000474"));
		atom.setCode(new Code("C12345"));
		atom.setSourceConceptIdentifier(new Identifier.Default(12345));
		atom.setConcept(source);
		return atom;
	}

}