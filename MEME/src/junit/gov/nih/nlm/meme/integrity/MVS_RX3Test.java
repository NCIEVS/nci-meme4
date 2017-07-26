/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MVS_RX3Test
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.integrity.MVS_RX3;
import junit.framework.TestCase;

public class MVS_RX3Test extends TestCase {

    private Concept source = new Concept.Default();
    private Concept target = new Concept.Default();
	private Atom[] atoms = null;
    private MVS_RX3 ic = null;
    
    /* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		
		ic = new MVS_RX3();
	    ic.setIsActive(true);
	    ic.setIsFatal(true);
	    ic.setShortDescription("Short description of MVS_RX3");
	    ic.setDescription("Description of MVS_RX3.");
				
	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	//
	// Test and assert success  
	//
	
	public void testEmptyTargetConcept() {
		createSourceConcept();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", !ic.
				validate(source, target, atoms));
	}
	
	public void testEmptySourceConcept() {
		createTargetConcept();
		source = new Concept.Default();
		assertTrue("No violation expected because source concept is empty", !ic.
				validate(source, target, atoms));
	}

	public void testViolationFound() {
		createSourceRX();
		createTargetRXWithID();
		assertTrue("Violation expected because a moving atom containing a primary atom was found", ic.
				validate(source, target, atoms));
	}

	public void testViolationNotFound() {
		createSourceRX();
		assertTrue("No violation expected because RXCUI Merge not found", !ic.
				validate(source, target, atoms));
	}

	public void testViolationNotFoundSourceNonRXNORM() {
		createSourceNonRXNORM();
		assertTrue("No violation expected.", !ic.
				validate(source, target, atoms));
	}

	public void testViolationNotFoundSourceNonReleasable() {
		createSourceNonReleasable();
		assertTrue("No violation expected.", !ic.
				validate(source, target, atoms));
	}

	public void testViolationNotFoundSetRXNSCD() {
		createSourceConceptSetRXNSCDC();
		assertTrue("No violation expected.", !ic.
				validate(source, target, atoms));
	}
	
	//
	// Test Cases
	//
	
	private void createSourceConcept() {
	    source = new Concept.Default();
	    Atom atom = createAtom();
		atom.setConcept(source);		
	    source.addAtom(atom);	    
		atoms = new Atom[] { atom };
	}

	private void createTargetConcept() {
	    target = new Concept.Default();
	    Atom atom = createAtom();
		atom.setConcept(target);
	    target.addAtom(atom);
		atoms = new Atom[] { };
	}

	private void createSourceRX() {
	    source = new Concept.Default();
	    Atom atom = createAtom();
		atom.setConcept(source);
	    Attribute attr = createAttribute(atom);
	    atom.addAttribute(attr);		
	    source.addAtom(atom);
	    source.addAttribute(attr);
		atoms = new Atom[] { atom };
	}

	private void createSourceNonReleasable() {
	    source = new Concept.Default();
	    Atom atom = createAtom();
		atom.setConcept(source);
		atom.setTobereleased('N');
	    Attribute attr = createAttribute(atom);
	    atom.addAttribute(attr);		
	    source.addAtom(atom);
	    source.addAttribute(attr);
		atoms = new Atom[] { atom };
	}

	private void createSourceNonRXNORM() {
	    source = new Concept.Default();
	    Atom atom = createAtom();
		atom.setConcept(source);
		atom.setString("RXN");
		atom.setTermgroup(new Termgroup.Default("RXN_2005AC/SCD"));
		Source src = new Source.Default("RXN_2005AC");
		src.setRootSourceAbbreviation("RXN");				
	    Attribute attr = createAttribute(atom);
	    atom.addAttribute(attr);		
	    source.addAtom(atom);
	    source.addAttribute(attr);
		atoms = new Atom[] { atom };
	}

	private void createSourceConceptSetRXNSCDC() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setString("RXN");
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/SCDC"));
		Source src = new Source.Default("RXN_2005AC");
		src.setRootSourceAbbreviation("RXN");
		atom.setSource(src);
		source.addAtom(atom);
		atom.setConcept(source);
		atoms = new Atom[] { atom };
	}

	private void createTargetRXWithID() {
	    target = new Concept.Default();
	    Atom atom = createAtom2();
		atom.setConcept(source);
	    Attribute attr = createAttribute(atom);
	    atom.addAttribute(attr);		
	    target.addAtom(atom);
	    target.addAttribute(attr);
		atoms = new Atom[] { atom };
	}
	
	//
	// Helper method(s)
	//

	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("RXNORM");
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/ET"));
		Source src = new Source.Default("RXNORM_2005AC");
		src.setRootSourceAbbreviation("RXNORM");
		src.setIsCurrent(true);
		atom.setSource(src);
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setTobereleased('Y');
		atom.setSuppressible("N");
		return atom;
	}
	
	private Atom createAtom2() {
		Atom atom = new Atom.Default();
		atom.setIdentifier(new Identifier.Default(12324));
		atom.setString("RXNORM");
		atom.setTermgroup(new Termgroup.Default("RXNORM_2005AC/ET"));
		Source src = new Source.Default("RXNORM_2005AC");
		src.setRootSourceAbbreviation("RXNORM");
		src.setIsCurrent(true);
		atom.setSource(src);
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setTobereleased('Y');
		atom.setSuppressible("N");
		return atom;
	}

	private Attribute createAttribute(Atom atom) {
		Attribute attr = new Attribute.Default();
		attr.setAtom(atom);
		attr.setLevel('S');
		attr.setName("RXCUI");
		attr.setValue("RX000001");		
		Source src = new Source.Default("MTH");
		src.setRootSourceAbbreviation("MTH");
		src.setIsCurrent(true);
		attr.setSource(src);
		attr.setStatus('R');
		attr.setGenerated(false);
		attr.setReleased('A');
		attr.setTobereleased('Y');
		attr.setSuppressible("N");
		atom.addAttribute(attr);
		return attr;
	}
	
}