/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_STYTest
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

public class MGV_STYTest extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private MGV_STY ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		ic = new MGV_STY();
		BinaryCheckData[] bcds = new BinaryCheckData[] {
	        	new BinaryCheckData("MGV_STY","VALUE","VALUE","Carbohydrate","Carbohydrate",false) };
		ic.setCheckData(bcds);
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of MGV_STY");
		ic.setDescription("Description of MGV_STY.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	
	public void testEmptyTargetConcept() {
		System.out.println("MGV_STYTest#testEmptyTarget()");
		createSourceConcept();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", 
				!ic.validate(source, target));
	}

	public void testEmptySourceConcept() {
		System.out.println("MGV_STYTest#testEmptySource()");
		source = new Concept.Default();
		createTargetConcept();
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target));
	}

	public void testViolationFound() {
		System.out.println("MGV_STYTest#testViolationFound()");
		createSourceConcept();
		createTargetConcept();
		assertTrue("Violation expected.", ic.validate(source, target));
	}

	public void testViolationNotFoundNoSTY() {
		System.out.println("MGV_STYTest#testViolationNotFoundNoSTY()");
		createSourceConceptNoSTY();
		createTargetConceptNoSTY();
		assertTrue("No violation expected.", !ic.validate(source, target));
	}

	//
	// Test Cases
	//

	private void createSourceConcept() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(source);
		source.addAtom(atom);
		source.addAttribute(createSTY());
	}

	private void createTargetConcept() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(target);
		target.addAtom(atom);
		target.addAttribute(createSTY());
	}

	private void createSourceConceptNoSTY() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createTargetConceptNoSTY() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(target);
		target.addAtom(atom);
	}	

	//
	// Helper method(s)
	//
	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("TEST ATOM");
		atom.setTermgroup(new Termgroup.Default("MTH/PN"));		
		atom.setSource(createSource());
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
		atom.setConcept(source);
		return atom;
	}
	
	private Source createSource() {
		Source src = new Source.Default("MTH");
		src.setStrippedSourceAbbreviation("MTH");
		src.setIsCurrent(true);
		return src;
	}

	private ConceptSemanticType createSTY() {
	    // Create a concept semantic type
	    ConceptSemanticType cst = new ConceptSemanticType();
	    cst.setIsChemical(true);
	    cst.setName("SEMANTIC_TYPE");
	    cst.setValue("Carbohydrate");
	    cst.setChemicalType("S");
	    cst.setIsEditingChemical(true);
	    cst.setLevel('C');
	    cst.setStatus('R');
	    cst.setReleased('A');
	    cst.setTobereleased('Y');
	    cst.setConcept(source);
	    cst.setSource(createSource());
	    return cst;
	}
	
}