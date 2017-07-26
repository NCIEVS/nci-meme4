/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_DTest
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import junit.framework.TestCase;

public class MGV_DTest extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private MGV_D ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		ic = new MGV_D();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of MGV_D");
		ic.setDescription("Description of MGV_D.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//

	
	public void testViolationNotFoundEmptyRels() {
		System.out.println("MGV_DTest#testViolationNotFoundEmptyRels()");
		createSourceConcept();
		createTargetConcept();
		assertTrue("No violation expected.", !ic.validate(source, target));
	}

	public void testViolationFound() {
		System.out.println("MGV_DTest#testViolationFound()");
		createSourceConceptSetMeshEntryTerms();
		createTargetConceptSetMeshEntryTerms();
		assertTrue("Violation expected.", ic.validate(source, target));
	}

	public void testViolationNotFoundSetLevel() {
		System.out.println("MGV_DTest#testViolationNotFoundSetLevel()");
		createSourceConceptSetLevel();
		createTargetConceptSetLevel();
		assertTrue("No violation expected.", !ic.validate(source, target));
	}

	public void testViolationNotFoundSetTBR() {
		System.out.println("MGV_DTest#testViolationNotFoundSetTBR()");
		createSourceConceptSetTBR();
		createTargetConceptSetTBR();
		assertTrue("No violation expected.", !ic.validate(source, target));
	}

	public void testViolationNotFoundSetName() {
		System.out.println("MGV_DTest#testViolationNotFoundSetName()");
		createSourceConceptSetName();
		createTargetConceptSetName();
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
	}

	private void createTargetConcept() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetMeshEntryTerms() {
		source = new Concept.Default();
		Atom atom = createMeshEntryTerm();
        source.addRelationship(createRelationship());    
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createTargetConceptSetMeshEntryTerms() {
		target = new Concept.Default();
		Atom atom = createMeshEntryTerm();
		Relationship rel = createRelationship();
	    rel.setName("BT");
        target.addRelationship(rel);    
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetTBR() {
		source = new Concept.Default();
		Atom atom = createAtom();
	    Relationship rel = createRelationship();
	    rel.setTobereleased('N');
        source.addRelationship(rel);    
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createTargetConceptSetTBR() {
		target = new Concept.Default();
		Atom atom = createAtom();
	    Relationship rel = createRelationship();
	    rel.setTobereleased('N');
        target.addRelationship(rel);    
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetLevel() {
		source = new Concept.Default();
		Atom atom = createAtom();
	    Relationship rel = createRelationship();
	    rel.setLevel('C');
        source.addRelationship(rel);    
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createTargetConceptSetLevel() {
		target = new Concept.Default();
		Atom atom = createAtom();
	    Relationship rel = createRelationship();
	    rel.setLevel('C');
        target.addRelationship(rel);    
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetName() {
		source = new Concept.Default();
		Atom atom = createAtom();
	    Relationship rel = createRelationship();
	    rel.setName("RT?");
        source.addRelationship(rel);    
		atom.setConcept(source);
		source.addAtom(atom);
	}

	private void createTargetConceptSetName() {
		target = new Concept.Default();
		Atom atom = createAtom();
	    Relationship rel = createRelationship();
	    rel.setName("RT?");
        target.addRelationship(rel);    
		atom.setConcept(target);
		target.addAtom(atom);
	}

	//
	// Helper method(s)
	//
	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setIdentifier(new Identifier.Default(123457));
		atom.setString("TEST ATOM");
		atom.setTermgroup(new Termgroup.Default("MSH/PN"));		
		atom.setSource(createSource());
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setSuppressible("N");
		atom.setTobereleased('Y');
		atom.setConcept(source);
		return atom;
	}
	
	private Atom createMeshEntryTerm() {
		MeshEntryTerm met1 = new MeshEntryTerm();
		met1.setIdentifier(new Identifier.Default(11111));
		met1.setString("TEST ATOM 1");
		met1.setTermgroup(new Termgroup.Default("MSH/PN"));
		met1.setSource(createSource());
		met1.setCode(Code.newCode("D000959"));
		met1.setStatus('R');
		met1.setGenerated(true);
		met1.setReleased('N');
		met1.setTobereleased('Y');
		met1.setSuppressible("N");
		met1.setConcept(source);
		met1.setMainHeading(createAtom());
		return met1;
	}

	private Source createSource() {
		Source src = new Source.Default("MSH");
		src.setStrippedSourceAbbreviation("MSH");
		src.setIsCurrent(true);
		return src;
	}

	private Relationship createRelationship() {
	    Relationship rel = new Relationship.Default();
	    rel.setAtom(createAtom());
	    rel.setRelatedAtom(createAtom());
	    rel.setConcept(source);
	    target = new Concept.Default();
	    target.setIdentifier(new Identifier.Default(12345));
	    rel.setRelatedConcept(target);
	    rel.setName("RT");
	    rel.setAttribute("mapped_to");
	    rel.setSource(createSource());
	    rel.setSourceOfLabel(createSource());
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