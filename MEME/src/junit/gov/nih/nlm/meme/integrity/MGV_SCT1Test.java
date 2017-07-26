/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_SCT1Test
 * 
 * 07/12/2006 RBE (1-BKQVT): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.integrity.MGV_SCT1;
import junit.framework.TestCase;

public class MGV_SCT1Test extends TestCase {

    private Concept source = new Concept.Default();
    private Concept target = new Concept.Default();
	private Atom[] atoms = null;
    private MGV_SCT1 ic = null;
    
    /* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		
		ic = new MGV_SCT1();
	    ic.setIsActive(true);
	    ic.setIsFatal(true);
	    ic.setShortDescription("Short description of MGV_SCT1");
	    ic.setDescription("Description of MGV_SCT1.");
	    
	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	//
	// Test and assert success  
	//
	
	public void testEmptyTargetConcept() {
		System.out.println("MGV_SCT1Test#testEmptyTargetConcept()");
		createSourceConceptContainEntire();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because target concept is empty", !ic
				.validate(source, target, atoms));
	}	
	
	public void testEmptySourceConcept() {
		System.out.println("MGV_SCT1Test#testEmptySourceConcept()");
		source = new Concept.Default();
		createTargetConceptContainEntire();
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target, atoms));
	}	

	public void testViolationFound() {
		System.out.println("MGV_SCT1Test#testViolationFound()");
		createTargetConceptContainSNOMEDCT_SCTSPA();
		createSourceConceptContainEntire();
		assertTrue("Violation expected.", ic.validate(source, target));
		assertTrue("Violation expected.", ic.validate(source, target, atoms));
	}	

	public void testViolationNotFound() {
		System.out.println("MGV_SCT1Test#testViolationNotFound()");
		createSourceConceptContainEntire();
		createTargetConceptContainEntire();
		assertTrue("Violation expected.", !ic.validate(source, target));
		assertTrue("Violation expected.", !ic.validate(source, target, atoms));
	}	
	
	public void testViolationNotFoundSCTSPA() {
		System.out.println("MGV_SCT1Test#testViolationNotFoundSCTSPA()");
		createSourceConceptContainSNOMEDCT_SCTSPA();
		createTargetConceptContainSNOMEDCT_SCTSPA();
		assertTrue("Violation expected.", !ic.validate(source, target));
		assertTrue("Violation expected.", !ic.validate(source, target, atoms));
	}	

	//
	// Test Cases
	//
	
	private void createSourceConceptContainEntire() {
	    source = new Concept.Default();
	    Atom atom = createAtom();
	    atom.setString("Entire fetal lower extremities (body structure)");
	    atom.setTermgroup(new Termgroup.Default("SNOMEDCT_US_2013_09_01/FN"));    
        Source src = new Source.Default("SNOMEDCT_US");
        src.setSourceAbbreviation("SNOMEDCT_US");        
        src.setRootSourceAbbreviation("SNOMEDCT_US");
        src.setIsCurrent(true);
        atom.setSource(src);
		atom.setConcept(source);
	    source.addAtom(atom);		
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptContainEntire() {
		target = new Concept.Default();
	    Atom atom = createAtom();
	    atom.setString("Entire fetal lower extremities (body structure)");
	    atom.setTermgroup(new Termgroup.Default("SNOMEDCT_US_2013_09_01/FN"));    
        Source src = new Source.Default("SNOMEDCT_US");
        src.setSourceAbbreviation("SNOMEDCT_US");        
        src.setRootSourceAbbreviation("SNOMEDCT_US");
        src.setIsCurrent(true);
        atom.setSource(src);
		atom.setConcept(target);
		target.addAtom(atom);
		atoms = new Atom[] { };
	}

	private void createSourceConceptContainSNOMEDCT_SCTSPA() {
	    source = new Concept.Default();
	    Atom atom = createAtom();
	    atom.setString("SNMI98");
	    atom.setTermgroup(new Termgroup.Default("SNMI98/PT"));
	    atom.setSource(new Source.Default("SNMI98"));
        Source src = new Source.Default("SNMI98");
        src.setSourceAbbreviation("SNMI98");        
        src.setRootSourceAbbreviation("SNMI98");
        src.setIsCurrent(true);
        atom.setSource(src);
	    atom.setConcept(source);
	    source.addAtom(atom);	
		atoms = new Atom[] { atom };
	}

	private void createTargetConceptContainSNOMEDCT_SCTSPA() {		
		target = new Concept.Default();
	    Atom atom = createAtom();
	    atom.setString("SNMI98");
	    atom.setTermgroup(new Termgroup.Default("SNMI98/PT"));
        Source src = new Source.Default("SNMI98");
        src.setSourceAbbreviation("SNMI98");        
        src.setRootSourceAbbreviation("SNMI98");
        src.setIsCurrent(true);
        atom.setSource(src);
	    atom.setConcept(target);		
		target.addAtom(atom);
		atoms = new Atom[] { };
	}

	//
	// Helper method(s)
	//

	private Atom createAtom() {
        Atom atom = new Atom.Default();
	    atom.setString("Entire fetal lower extremities (body structure)");
	    atom.setTermgroup(new Termgroup.Default("SNOMEDCT_US_2013_09_01/FN"));    
        Source src = new Source.Default("SNOMEDCT_US");
        src.setSourceAbbreviation("SNOMEDCT_US");        
        src.setRootSourceAbbreviation("SNOMEDCT_US");
        src.setIsCurrent(true);
        atom.setSource(src);
	    atom.setStatus('R');
	    atom.setGenerated(true);
	    atom.setReleased('N');
	    atom.setTobereleased('Y');
	    atom.setSuppressible("N");
		return atom;
	}
	
}