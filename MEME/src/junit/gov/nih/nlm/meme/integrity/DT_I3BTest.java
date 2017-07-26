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
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import junit.framework.TestCase;

public class DT_I3BTest extends TestCase {

	private Concept source = null;
	private DT_I3B ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_I3B();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_I3B");
		ic.setDescription("Description of DT_I3B.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNoRelationships() {
		System.out.println("DT_I3BTest#testViolationNotFoundNoRelationships()");
		createConcept();
		assertTrue("No violation expected. No relationships attached to concept.", !ic.validate(source));
	}

	public void testViolationNotFound() {
		System.out.println("DT_I3BTest#testViolationNotFound()");
		createSourceConcept();
		assertTrue("No violation expected. Found matching C level rel", !ic.validate(source));
	}

	public void testViolationFoundSetLevel() {
		System.out.println("DT_I3BTest#testViolationFoundSetLevel()");
		createSourceConceptSetLevel();
		assertTrue("Violation expected. Matching C level rel not found", ic.validate(source));
	}

	public void testViolationFoundSetTBR() {
		System.out.println("DT_I3BTest#testViolationFoundSetTBR()");
		createSourceConceptSetTBR();
		assertTrue("Violation expected. Matching C level rel not found", ic.validate(source));
	}

	//
	// Test Cases
	//

	private void createConcept() {
		source = new Concept.Default();
	}

	private void createSourceConcept() {
		source = new Concept.Default();
		Relationship dem = createDemotions();
		dem.setConcept(source);
		source.addRelationship(dem);
		Relationship rel = createRelationships();
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	private void createSourceConceptSetLevel() {
		source = new Concept.Default();
		Relationship dem = createDemotions();
		dem.setLevel('S');
		dem.setConcept(source);
		source.addRelationship(dem);
		Relationship rel = createRelationships();
		rel.setLevel('S');
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	private void createSourceConceptSetTBR() {
		source = new Concept.Default();
		Relationship dem = createDemotions();
		dem.setTobereleased('N');
		dem.setConcept(source);
		source.addRelationship(dem);
		Relationship rel = createRelationships();
		rel.setTobereleased('n');
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	//
	// Helper method(s)
	//

	private Relationship createDemotions() {
	    Relationship rel = new Relationship.Default();
	    rel.setConcept(source);
	    Concept concept = new Concept.Default();
	    concept.setIdentifier(new Identifier.Default(12345));
	    rel.setRelatedConcept(concept);
	    rel.setName("RT");
	    rel.setAttribute("mapped_to");
	    rel.setStatus('R');
	    rel.setGenerated(false);
	    rel.setLevel('C');
	    rel.setStatus('N');
	    rel.setReleased('A');
	    rel.setTobereleased('Y');
	    rel.setSuppressible("N");
	    rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
	    rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));	      
	    return rel;
	}
	
	private Relationship createRelationships() {
	    Relationship rel = new Relationship.Default();
	    rel.setConcept(source);
	    Concept concept = new Concept.Default();
	    concept.setIdentifier(new Identifier.Default(12345));
	    rel.setRelatedConcept(concept);
	    rel.setName("RT");
	    rel.setAttribute("mapped_to");
	    rel.setStatus('R');
	    rel.setGenerated(false);
	    rel.setLevel('C');
	    rel.setStatus('D');
	    rel.setReleased('A');
	    rel.setTobereleased('Y');
	    rel.setSuppressible("N");
	    rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
	    rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));	      
	    return rel;
	}

}