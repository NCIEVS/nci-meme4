/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.ic
 * Object:  MGV_RX4Test
 * 
 * 04/24/2007 BAC (1-E3I65): File created
 *  
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.integrity.MGV_RX4;
import junit.framework.TestCase;

public class MGV_RX4Test extends TestCase {

	private Concept source = null;
	private Concept target = null;
	private Atom[] atoms = null;
	private MGV_RX4 ic = null;

	/* Code to set up a fresh scaffold for each test */
	protected void setUp() throws Exception {
		super.setUp();
		source = null;
		target = null;
		atoms = null;
		ic = new MGV_RX4();
		ic.setIsActive(true);
		ic.setIsFatal(true);
		ic.setShortDescription("Short description of MGV_RX4");
		ic.setDescription("Description of MGV_RX4.");

	}

	/* Code to destroy the scaffold after each test */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//
	// Test and assert success  
	//
	public void testEmptyTargetConcept() {
		System.out.println("MGV_RX4Test#testEmptyTargetConcept()");
		source = createBaseFormConcept();
		target = new Concept.Default();
		atoms = source.getAtoms();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because target concept is empty", !ic
				.validate(source, target, atoms));

		source = createNormalFormConcept();
		atoms = source.getAtoms();
		target = new Concept.Default();
		assertTrue("No violation expected because target concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because target concept is empty", !ic
				.validate(source, target, atoms));
}

	public void testEmptySourceConcept() {
		System.out.println("MGV_RX4Test#testEmptySourceConcept()");
		source = new Concept.Default();
		target = createBaseFormConcept();
		atoms = new Atom[0];
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because source concept is empty", !ic.validate(source, target, atoms));

		source = new Concept.Default();
		target = createNormalFormConcept();
		atoms = new Atom[0];
		assertTrue("No violation expected because source concept is empty", !ic
				.validate(source, target));
		assertTrue("No violation expected because source concept is empty", !ic.validate(source, target, atoms));
	
	}

	public void testViolationFound() {
		System.out.println("MGV_RX4Test#testViolationFound()");
		source = createBaseFormConcept();
		target = createNormalFormConcept();
		atoms = source.getAtoms();
		assertTrue("Violation expected.", ic.validate(source, target));
		assertTrue("Violation expected.", ic.validate(source, target, atoms));

		// switch

		source = createNormalFormConcept();
		target = createBaseFormConcept();
		atoms = source.getAtoms();
		assertTrue("Violation expected.", ic.validate(source, target));
		assertTrue("Violation expected.", ic.validate(source, target, atoms));
	}

	public void testViolationNotFoundOCD() {
		System.out.println("MGV_RX4Test#testViolationNotFoundOCD()");
		source = createBaseFormConcept();
		target = createOCDConcept();
		atoms = source.getAtoms();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));

		// switch

		source = createOCDConcept();
		target = createBaseFormConcept();
		atoms = source.getAtoms();
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	
	}

	public void testViolationNotFoundObsoleteAtom() {
		System.out.println("MGV_RX4Test#testViolationNotFoundObsoleteAtom()");
		source = createBaseFormConcept();		
		target = createNormalFormConcept();
		atoms = source.getAtoms();
		atoms[0].getSource().setIsCurrent(false);
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));

		// switch

		source = createNormalFormConcept();
		target = createBaseFormConcept();		
		atoms = source.getAtoms();
		atoms[0].getSource().setIsCurrent(false);
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
	
	}

	public void testViolationNotFoundObsoleteAttribute() {
		System.out.println("MGV_RX4Test#testViolationNotFoundObsoleteAttrribute()");
		source = createBaseFormConcept();		
		target = createNormalFormConcept();
		atoms = source.getAtoms();
		atoms[0].getAttributes()[0].getSource().setIsCurrent(false);
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));

		// switch

		source = createNormalFormConcept();
		target = createBaseFormConcept();		
		atoms = source.getAtoms();
		target.getAtoms()[0].getAttributes()[0].getSource().setIsCurrent(false);
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
  }
	
	public void testViolationNotFoundWrongAttribute() {
		System.out.println("MGV_RX4Test#testViolationNotFoundObsoleteAttrribute()");
		source = createBaseFormConcept();		
		target = createNormalFormConcept();
		atoms = source.getAtoms();
		atoms[0].getAttributes()[0].setName("NOT_AMBIGUITY_FLAG");
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));

		atoms[0].getAttributes()[0].setName("AMBIGUITY_FLAG");
		atoms[0].getAttributes()[0].setValue("NOT_Base");
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
		atoms[0].getAttributes()[0].setName("AMBIGUITY_FLAG");
		atoms[0].getAttributes()[0].setValue("Base");
		atoms[0].getAttributes()[0].getSource().setRootSourceAbbreviation("NOT_RXNORM");
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));

		// switch
		
		target = createBaseFormConcept();		
		source = createNormalFormConcept();
		atoms = source.getAtoms();
		target.getAtoms()[0].getAttributes()[0].setName("NOT_AMBIGUITY_FLAG");
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));

		target.getAtoms()[0].getAttributes()[0].setName("AMBIGUITY_FLAG");
		target.getAtoms()[0].getAttributes()[0].setValue("NOT_Base");
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
		target.getAtoms()[0].getAttributes()[0].setName("AMBIGUITY_FLAG");
		target.getAtoms()[0].getAttributes()[0].setValue("Base");
		target.getAtoms()[0].getAttributes()[0].getSource().setRootSourceAbbreviation("NOT_RXNORM");
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));
		
	}	

	public void testViolationNotFoundDifferentSource() {
		System.out.println("MGV_RX4Test#testViolationNotFoundObsoleteAttrribute()");
		source = createBaseFormConcept();		
		target = createNormalFormConcept();
		atoms = source.getAtoms();
		atoms[0].getSource().setRootSourceAbbreviation("NOT_RXNORM");
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));

		// switch
		
		source = createNormalFormConcept();
		target = createBaseFormConcept();		
		atoms = source.getAtoms();
		target.getAtoms()[0].getSource().setRootSourceAbbreviation("NOT_RXNORM");
		
		assertTrue("No violation expected.", !ic.validate(source, target));
		assertTrue("No violation expected.", !ic.validate(source, target, atoms));

	}
	
	//
	// Helper Methods
	//

	private Concept createBaseFormConcept() {
		final Concept c = new Concept.Default();
		final Atom atom = createAtom("CD");
		atom.setConcept(c);
		c.addAtom(atom);
		final Attribute att = addAttribute(atom,"RXNORM","AMBIGUITY_FLAG","Base");
		return c;
	}
	
	private Concept createNormalFormConcept() {
		final Concept c = new Concept.Default();
		final Atom atom = createAtom("SCD");
		atom.setConcept(c);
		c.addAtom(atom);
		return c;
	}

	private Concept createOCDConcept() {
		final Concept c = new Concept.Default();
		final Atom atom = createAtom("OCD");
		atom.setConcept(c);
		c.addAtom(atom);
		return c;
	}

	private Atom createAtom(String tty) {
		final Atom atom = new Atom.Default();
		atom.setString("RXNORM");
		atom.setTermgroup(new Termgroup.Default("RXNORM/"+tty));
		atom.setSource(createSource("RXNORM"));
		atom.setStatus('R');
		atom.setGenerated(true);
		atom.setReleased('N');
		atom.setTobereleased('Y');
		atom.setSuppressible("N");
		atom.setConcept(source);
		return atom;
	}

	private Attribute addAttribute(Atom atom, String sab, String atn, String atv) {
		final Attribute attribute = new Attribute.Default();
		attribute.setName(atn);
		attribute.setValue(atv);
		attribute.setAtom(atom);
		atom.addAttribute(attribute);
		attribute.setConcept(atom.getConcept());
		attribute.setSource(createSource(sab));
		attribute.setStatus('R');
		attribute.setGenerated(true);
		attribute.setReleased('N');
		attribute.setTobereleased('Y');
		attribute.setSuppressible("N");
		return attribute;
	}

	public Source createSource(String sab) {
		final Source src = new Source.Default(sab);
		src.setRootSourceAbbreviation(sab);
		src.setIsCurrent(true);
		return src;
	}

	public Source createObsoleteSource(String sab) {
		final Source src = new Source.Default(sab);
		src.setRootSourceAbbreviation(sab);
		src.setIsCurrent(false);
		return src;
	}

}