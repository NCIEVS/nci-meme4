/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_SDUITest
 * 
 * 07/12/2006 RBE (1-BKQVT): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.integrity.MGV_SDUI;
import gov.nih.nlm.meme.integrity.UnaryCheckData;
import junit.framework.TestCase;

public class MGV_SDUITest extends TestCase {

  private Concept source = null;
  private Concept target = null;
  private Atom[] atoms = null;
    private MGV_SDUI ic = null;
    
    /* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
    source = null;
    target = null;
    atoms = null;
		ic = new MGV_SDUI();
		UnaryCheckData[] ucds = new UnaryCheckData [] { 
        	new UnaryCheckData("MGV_SDUI","SOURCE","MTH",false) };
		ic.setCheckData(ucds);
	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success
	//
	
	public void testEmptyTargetConcept() {
		createSourceConceptSetSource();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", !ic.validate(source, target));
		assertTrue("No violation expected because target concept is empty", !ic.validate(source, target, atoms));
	}	
	
	public void testEmptySourceConcept() {
		createTargetConceptSetSource();
		source = new Concept.Default();
		atoms = new Atom[] {};
		assertTrue("No violation expected because source concept is empty", !ic.validate(source, target));
		assertTrue("No violation expected because source concept is empty", !ic.validate(source, target, atoms));
	}	

	public void testDifferentSources() {
		createTargetConceptSetSource();
		createSourceConceptSetSource();
		assertTrue("No violation expected because SAB values do not match", !ic.validate(source, target));
		assertTrue("No violation expected because SAB values do not match", !ic.validate(source, target, atoms));
	}
	
	public void testExpectedViolation() {
		createTargetConceptSetDescriptor();
		createSourceConceptSetDescriptor();
		assertTrue("Violation expected because SABs match but SDUIs are different", ic.validate(source, target));
		assertTrue("Violation expected because SABs match but SDUIs are different", ic.validate(source, target, atoms));
	}

	public void testSourceSDUINull() {
		createTargetConceptSetDescriptor();
		createSourceConceptSetDescToNull();
		assertTrue("Violation expected because source SDUI is null and different from target", ic.validate(source, target));
		assertTrue("Violation expected because source SDUI is null and different from target", ic.validate(source, target, atoms));
	}

	public void testTargetSDUINull() {
		createTargetConceptSetDescToNull();
		createSourceConceptSetDescriptor();
		assertTrue("No violation expected because target atom descriptor is null", !ic.validate(source, target));
		assertTrue("No violation expected because target atom descriptor is null", !ic.validate(source, target, atoms));
	}

	//
	// Test Cases
	//
	
	private void createSourceConceptSetSource() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setSource(new Source.Default("MSH"));
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] {atom};
	}

	private void createTargetConceptSetSource() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setSource(new Source.Default("MTH"));
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetDescriptor() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setSourceDescriptorIdentifier(new Identifier.Default(12345));
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] {atom};
	}

	private void createTargetConceptSetDescriptor() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setSourceDescriptorIdentifier(new Identifier.Default(12346));
		atom.setConcept(target);
		target.addAtom(atom);
	}

	private void createSourceConceptSetDescToNull() {
		source = new Concept.Default();
		Atom atom = createAtom();
		atom.setSourceDescriptorIdentifier(null);
		atom.setConcept(source);
		source.addAtom(atom);
		atoms = new Atom[] {atom};
	}

	private void createTargetConceptSetDescToNull() {
		target = new Concept.Default();
		Atom atom = createAtom();
		atom.setSourceDescriptorIdentifier(null);
		atom.setConcept(target);
		target.addAtom(atom);
	}

	//
	// Helper method(s)
	//

	private Atom createAtom() {
		Atom atom = new Atom.Default();
		atom.setString("TEST MGV_SDUI ATOM");
		atom.setTermgroup(new Termgroup.Default("MTH/PT"));
		atom.setSource(new Source.Default("MTH"));
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setTobereleased('Y');
		atom.setSuppressible("N");
		atom.setSourceDescriptorIdentifier(new Identifier.Default(12345));
		return atom;
	}
	
}