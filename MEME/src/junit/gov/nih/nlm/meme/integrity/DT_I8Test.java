/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  DT_I8Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import junit.framework.TestCase;

public class DT_I8Test extends TestCase {

	private Concept source = null;
	private DT_I8 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		ic = new DT_I8();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of DT_I8");
		ic.setDescription("Description of DT_I8.");
		ic.setTermType("MH");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	public void testViolationNotFoundNoRelationships() {
		System.out.println("DT_I8Test#testViolationNotFoundNoRelationships()");
		createConcept();
		assertTrue("No violation expected. No relationships attached to concept.", !ic.validate(source));
	}

	public void testViolationFound() {
		System.out.println("DT_I8Test#testViolationFound()");
		createSourceConcept();
		assertTrue("Violation expected.", ic.validate(source));
	}
	
	// XR is true

	public void testViolationNotFoundSetSAB() {
		System.out.println("DT_I8Test#testViolationNotFoundSetSAB()");
		ic.setXR(true);
		createSourceConceptSetSAB();
		assertTrue("No violation expected.", !ic.validate(source));
	}

	public void testViolationFoundWithRelationship() {
		System.out.println("DT_I8Test#testViolationFoundWithRelationship()");
		createSourceConceptWithRelationship();
		ic.setXR(true);
		assertTrue("Violation expected.", ic.validate(source));
	}

	public void testViolationNotFoundSetName() {
		System.out.println("DT_I8Test#testViolationNotFoundSetName()");
		createSourceConceptSetSAB();
		ic.setXR(true);
		assertTrue("Violation expected.", !ic.validate(source));
	}

	//
	// Test Cases
	//

	private void createConcept() {
		source = new Concept.Default();
	}

	private void createSourceConcept() {
		source = new Concept.Default();
		MeshEntryTerm msh = createAtom();
		msh.setConcept(source);
		source.addAtom(msh);
	}

	private void createSourceConceptWithRelationship() {
		source = new Concept.Default();
		MeshEntryTerm msh = createAtom();
		source.addAtom(msh);
		Relationship rel = createRelationship();
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	private void createSourceConceptSetSAB() {
		source = new Concept.Default();
		MeshEntryTerm msh = createAtom();
		source.addAtom(msh);
		Relationship rel = createRelationship();
		Source src = new Source.Default("MMM");
		src.setStrippedSourceAbbreviation("MMM");
		rel.setSource(src);
		rel.setName("EX");
		rel.setConcept(source);
		source.addRelationship(rel);
	}

	//
	// Helper method(s)
	//

	private MeshEntryTerm createAtom() {
		MeshEntryTerm msh = new MeshEntryTerm();
		msh.setString("TEST ATOM");
		msh.setTermgroup(new Termgroup.Default("MSH/PN"));		
		msh.setSource(createSource());
		msh.setStatus('R');
		msh.setGenerated(true);
		msh.setReleased('N');
		msh.setSuppressible("N");
		msh.setTobereleased('Y');
		msh.setCode(new Code("D12345"));
		Concept concept = new Concept.Default();
		concept.setIdentifier(new Identifier.Default(12345));
		msh.setConcept(concept);
		msh.setMainHeading(msh);
		return msh;
	}
	
	private Source createSource() {
		Source src = new Source.Default("MSH");
		src.setStrippedSourceAbbreviation("MSH");
		src.setIsCurrent(true);
		return src;
	}

	private Relationship createRelationship() {
	    Relationship rel = new Relationship.Default();
	    Concept concept = new Concept.Default();
	    concept.setIdentifier(new Identifier.Default(12345));
	    rel.setRelatedConcept(concept);	    
	    rel.setAtom(createAtom());
	    rel.setConcept(source);
	    rel.setName("XR");
	    rel.setAttribute("mapped_to");
	    rel.setSource(createSource());
	    rel.setSourceOfLabel(createSource());
	    rel.setStatus('R');
	    rel.setGenerated(false);
	    rel.setLevel('C');
	    rel.setReleased('A');
	    rel.setTobereleased('Y');
	    rel.setSuppressible("N");
	    rel.setNativeIdentifier(new NativeIdentifier("", "", "", "", ""));
	    rel.setRelatedNativeIdentifier(new NativeIdentifier("", "", "", "", ""));	      
	    return rel;
	}
	
}