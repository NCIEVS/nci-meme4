/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I11Test
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

public class DT_I11Test extends TestCase {

	private Concept source = null;
	private DT_I11 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_I11();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_I11");
		ic.setDescription("Description of DT_I11.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNoRelationships() {
		System.out.println("DT_I11Test#testViolationNotFoundNoRelationships()");
		createConcept();
		assertTrue("No violation expected. No relationships found.", !ic.validate(source));
	}

	public void testViolationFound() {
		System.out.println("DT_I11Test#testViolationFound()");
		createSourceConcept();
		assertTrue("Violation expected.", ic.validate(source));
	}

	public void testViolationNotFoundSetName() {
		System.out.println("DT_I11Test#testViolationNotFoundSetName()");
		createSourceConceptSetName();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationNotFoundSetStatus() {
		System.out.println("DT_I11Test#testViolationNotFoundSetStatus()");
		createSourceConceptSetStatus();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationNotFoundSetIdentifier() {
		System.out.println("DT_I11Test#testViolationNotFoundSetIdentifier()");
		createSourceConceptSetIdentifier();
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
		Relationship rel = createRelationship();
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	private void createSourceConceptSetName() {
		source = new Concept.Default();
		Relationship rel = createRelationship();
		rel.setName("SFR");
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	private void createSourceConceptSetStatus() {
		source = new Concept.Default();
		Relationship rel = createRelationship();
		rel.setStatus('N');
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	private void createSourceConceptSetIdentifier() {
		source = new Concept.Default();
		Relationship rel = createRelationship();
	    source.setIdentifier(new Identifier.Default(12345));
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	//
	// Helper method(s)
	//

	private Relationship createRelationship() {
	    Relationship rel = new Relationship.Default();
	    source.setIdentifier(new Identifier.Default(54321));
	    rel.setConcept(source);
	    Concept related_concept = new Concept.Default();
	    related_concept.setIdentifier(new Identifier.Default(12345));
	    rel.setRelatedConcept(related_concept);
	    rel.setName("SFO/LFO");
	    rel.setAttribute("mapped_to");
	    rel.setStatus('R');
	    rel.setGenerated(false);
	    rel.setLevel('S');
	    rel.setReleased('A');
	    rel.setTobereleased('Y');
	    rel.setSuppressible("N");
	    rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
	    rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));	      
	    return rel;
	}
	
}