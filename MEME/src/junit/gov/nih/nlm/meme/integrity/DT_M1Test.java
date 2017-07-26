/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_M1Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import junit.framework.TestCase;

public class DT_M1Test extends TestCase {

	private Concept source = null;
	private DT_M1 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_M1();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_M1");
		ic.setDescription("Description of DT_M1.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationFoundNoSTY() {
		System.out.println("DT_M1Test#testViolationFoundNoSTY()");
		createConcept();
		assertTrue("Violation expected. No STY attached to concept.", ic.validate(source));
	}

	public void testViolationNotFound() {
		System.out.println("DT_M1Test#testViolationNotFound()");
		createSourceConcept();
		assertTrue("No violation expected. A releasable STY found", !ic.validate(source));
	}

	public void testViolationFoundSetTBR() {
		System.out.println("DT_M1Test#testViolationFoundSetTBR()");
		createSourceConceptSetTBR();
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
		ConceptSemanticType sty = createSTY();
		sty.setConcept(source);
		source.addAttribute(sty);
	}

	private void createSourceConceptSetTBR() {
		source = new Concept.Default();
		ConceptSemanticType sty = createSTY();
	    sty.setTobereleased('N');
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
	    cst.setName("SEMANTIC_TYPE");
	    cst.setValue("Carbohydrate");
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