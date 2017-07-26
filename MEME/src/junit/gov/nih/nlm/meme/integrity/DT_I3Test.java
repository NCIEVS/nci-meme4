/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I3Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import junit.framework.TestCase;

public class DT_I3Test extends TestCase {

	private Concept source = null;
	private DT_I3 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_I3();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_I3");
		ic.setDescription("Description of DT_I3.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNoRelationships() {
		System.out.println("DT_I3Test#testViolationNotFoundNoRelationships()");
		createConcept();
		assertTrue("No violation expected. No relationships attached to concept.", !ic.validate(source));
	}

	public void testViolationFound() {
		System.out.println("DT_I3Test#testViolationFound()");
		createSourceConcept();
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
		Relationship rel = createRelationship();
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	//
	// Helper method(s)
	//

	private Relationship createRelationship() {
	    Relationship rel = new Relationship.Default();
	    rel.setConcept(source);
	    rel.setName("RT");
	    rel.setAttribute("mapped_to");
	    rel.setStatus('R');
	    rel.setGenerated(false);
	    rel.setLevel('S');
	    rel.setStatus('D');
	    rel.setReleased('A');
	    rel.setTobereleased('Y');
	    rel.setSuppressible("N");
	    rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
	    rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));	      
	    return rel;
	}
	
}