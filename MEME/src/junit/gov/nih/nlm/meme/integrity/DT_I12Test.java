/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I12Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import junit.framework.TestCase;

public class DT_I12Test extends TestCase {

	private Concept source = null;
	private DT_I12 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_I12();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_I12");
		ic.setDescription("Description of DT_I12.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNonNonHuman() {
		System.out.println("DT_I12Test#testViolationNotFoundNonNonHuman()");
		createConcept();
		assertTrue("No violation expected. Non NON_HUMAN", !ic.validate(source));
	}

	public void testViolationNotFoundNoSTY() {
		System.out.println("DT_I12Test#testViolationNotFoundNoSTY()");
		createSourceConcept();
		assertTrue("No violation expected. No STY found", !ic.validate(source));
	}
	
	public void testViolationFoundSetValue() {
		System.out.println("DT_I12Test#testViolationFoundSetValue()");
		createSourceConceptSetValue();
		assertTrue("Violation expected. Invalid STY", ic.validate(source));
	}
	
	//
	// Test Cases
	//

	private void createConcept() {
		source = new Concept.Default();
	}

	private void createSourceConcept() {
		source = new Concept.Default();
		ConceptSemanticType sty = createSTY();
		sty.setConcept(source);
		source.addAttribute(sty);
	}

	private void createSourceConceptSetValue() {
		source = new Concept.Default();
		ConceptSemanticType sty = createSTY();
		sty.setValue("Carbohydrates");
		sty.setConcept(source);
		source.addAttribute(sty);
	}


	//
	// Helper method(s)
	//

	private ConceptSemanticType createSTY() {
	    // Create a concept semantic type
	    ConceptSemanticType cst = new ConceptSemanticType();
	    cst.setIsChemical(true);
	    cst.setName("NON_HUMAN");
	    cst.setValue("Biologic Function");
	    cst.setChemicalType("S");
	    cst.setIsEditingChemical(true);
	    cst.setLevel('C');
	    cst.setStatus('R');
	    cst.setReleased('A');
	    cst.setTobereleased('Y');
	    cst.setConcept(source);
	    return cst;
	}
	
}