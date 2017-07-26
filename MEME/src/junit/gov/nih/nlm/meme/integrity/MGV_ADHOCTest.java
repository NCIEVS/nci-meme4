/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_ADHOCTest
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import junit.framework.TestCase;

public class MGV_ADHOCTest extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private MGV_ADHOC ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		ic = new MGV_ADHOC();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of MGV_ADHOC");
		ic.setDescription("Description of MGV_ADHOC.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	
	public void testViolationNotFoundNonChemSTY() {
		System.out.println("MGV_ADHOCTest#testViolationNotFoundNonChemSTY()");
		createSourceConcept();
		createTargetConcept();
		assertTrue("No violation expected.", !ic.validate(source, target));
	}

	public void testViolationFound() {
		System.out.println("MGV_ADHOCTest#testViolationFound()");
		createSourceConceptSetChemicalSTY();
		createTargetConceptSetChemicalSTY();
		assertTrue("Violation expected.", ic.validate(source, target));
	}
	public void testViolationNotFoundNonReleasable() {
		System.out.println("MGV_ADHOCTest#testViolationNotFoundNonReleasable()");
		createSourceConceptSetTBR();
		createTargetConceptSetTBR();
		assertTrue("No violation expected.", !ic.validate(source, target));
	}

	//
	// Test Cases
	//

	private void createSourceConcept() {
		source = new Concept.Default();
		Atom atom = createAtom1();
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createTargetConcept() {
		target = new Concept.Default();
		Atom atom = createAtom1();
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetChemicalSTY() {
		source = new Concept.Default();
		Atom atom = createAtom2();
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createTargetConceptSetChemicalSTY() {
		target = new Concept.Default();
		Atom atom = createAtom2();
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetTBR() {
		source = new Concept.Default();
		Atom atom = createAtom2();
		atom.setTobereleased('N');
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createTargetConceptSetTBR() {
		target = new Concept.Default();
		Atom atom = createAtom2();
		atom.setTobereleased('N');
		atom.setConcept(target);
		target.addAtom(atom);
	}

	//
	// Helper method(s)
	//
	private Atom createAtom1() {
		Atom atom = new Atom.Default();
		atom.setString("TEST ATOM");
		atom.setTermgroup(new Termgroup.Default("MDR/PN"));		
		Source src = new Source.Default("MDR");
		src.setStrippedSourceAbbreviation("MDR");
		atom.setSource(src);
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
		atom.setConcept(source);
		return atom;
	}

	private Atom createAtom2() {
		Atom atom = new Atom.Default();
		atom.setString("TEST ATOM");
		atom.setTermgroup(new Termgroup.Default("MDR/PN"));		
		Source src = new Source.Default("MDR");
		src.setStrippedSourceAbbreviation("MDR");
		atom.setSource(src);
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
	    ConceptSemanticType attribute = new ConceptSemanticType();
	    attribute.setIsChemical(true);
	    attribute.setTobereleased('Y');
	    source.addAttribute(attribute);		
		atom.setConcept(source);
		return atom;
	}
}