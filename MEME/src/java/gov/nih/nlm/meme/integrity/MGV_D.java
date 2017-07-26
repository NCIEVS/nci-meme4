/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_D
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;
import java.util.HashSet;

/**
 * Validates merges between two {@link Concept}s
 * that contain different releasable {@link Relationship}s to the same latest version
 * <code>MSH/MH</code>.
 *
 * @author MEME Group
 */
public class MGV_D extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_D} check.
   */
  public MGV_D() {
    super();
    setName("MGV_D");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @return <code>true</code> if constraint violated, <code>false</code>
   * otherwise
   */
  public boolean validate(Concept source, Concept target) {

    //
    // Collect main headings for each entry term in source/target concepts
    //
    HashSet mh_atoms = new HashSet();
    Atom[] atoms = source.getAtoms();
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i] instanceof MeshEntryTerm) {
        Atom mh = ( (MeshEntryTerm) atoms[i]).getMainHeading();
        mh_atoms.add(mh);
      }
    }
    atoms = target.getAtoms();
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i] instanceof MeshEntryTerm) {
        Atom mh = ( (MeshEntryTerm) atoms[i]).getMainHeading();
        mh_atoms.add(mh);
      }
    }

    //
    // Get all relationships
    //
    Relationship[] source_rels = source.getRelationships();
    Relationship[] target_rels = target.getRelationships();

    //
    // Find cases where the source concept has a
    // relationship to the same main heading as the target
    // concept where the relationship names are different.
    //
    for (int i = 0; i < source_rels.length; i++) {
      if (source_rels[i].isSourceAsserted() &&
          source_rels[i].isReleasable() &&
          (!source_rels[i].getName().equals("RT?") &&
           !source_rels[i].getName().equals("BT?") &&
           !source_rels[i].getName().equals("NT?") &&
           !source_rels[i].getName().equals("SY")) &&
          mh_atoms.contains(source_rels[i].getRelatedAtom())) {
        for (int j = 0; j < target_rels.length; j++) {
          if (target_rels[j].isSourceAsserted() &&
              target_rels[j].isReleasable() &&
              (!target_rels[j].getName().equals("RT?") &&
               !target_rels[j].getName().equals("BT?") &&
               !target_rels[j].getName().equals("NT?") &&
               !target_rels[j].getName().equals("SY")) &&
              mh_atoms.contains(target_rels[j].getRelatedAtom()) &&
              source_rels[i].getRelatedAtom().equals(target_rels[j].
              getRelatedAtom()) &&
              !source_rels[i].getName().equals(target_rels[j].getName())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Self-qa test.
   * @param args command line arguments
   */
  public static void main(String[] args) {

    try {
      MEMEToolkit.initialize(null, null);
    } catch (InitializationException ie) {
      MEMEToolkit.handleError(ie);
    }
    MEMEToolkit.setProperty(MEMEConstants.DEBUG, "true");

    //
    // Main Header
    //

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Starting test of MGV_D ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_D- validate():";
    String test_result = null;

    // Create an MGV_D object to work with
    MGV_D mgv_d = new MGV_D();

    // Create concept and related concept
    Concept source = new Concept.Default(12345);
    Concept target = new Concept.Default(12346);
    Concept mh_concept = new Concept.Default(12347);

    // Create entry termgroup
    Termgroup et_tag = new Termgroup.Default("MSH2002/EN");

    // Create mesh termgroup
    Termgroup msh_tag = new Termgroup.Default("MSH2002/MH");

    // Create MSH
    Source msh = new Source.Default();

    msh.setIsCurrent(true);
    msh.setStrippedSourceAbbreviation("MSH");
    msh.setSourceAbbreviation("MSH2002");

    // Create entry term
    MeshEntryTerm et = new MeshEntryTerm(12345);
    et.setSource(msh);
    et.setTermgroup(et_tag);

    MeshEntryTerm et2 = new MeshEntryTerm(12346);
    et2.setSource(msh);
    et2.setTermgroup(et_tag);

    // Create main heading
    Atom mh = new Atom.Default(123456);
    mh.setSource(msh);
    mh.setTermgroup(msh_tag);

    // Create code
    Code code = new Code("D12345");

    et.setCode(code);
    et2.setCode(code);
    mh.setCode(code);
    et.setMainHeading(mh);
    et2.setMainHeading(mh);
    mh.setConcept(mh_concept);

    Relationship rel1 = new Relationship.Default();
    rel1.setLevel('S'); // CoreData.FV_SOURCE_ASSERTED
    rel1.setTobereleased('Y');
    rel1.setRelatedConcept(mh_concept);
    rel1.setName("XR");
    rel1.setAtom(et);
    rel1.setRelatedAtom(mh);

    Relationship rel2 = new Relationship.Default();
    rel2.setLevel('S'); // CoreData.FV_SOURCE_ASSERTED
    rel2.setTobereleased('Y');
    rel2.setRelatedConcept(mh_concept);
    rel2.setName("RR");
    rel2.setAtom(et2);
    rel2.setRelatedAtom(mh);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_d.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concept has no atom.");

    source.addAtom(et);
    source.addRelationship(rel1);

    target.addAtom(et2);
    target.addRelationship(rel2);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_d.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts that contain different releasable relationships " +
        "to the same latest version MSH MH.");

    rel1.setLevel('L');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_d.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Relationship is not asserted.");

    rel1.setLevel('S');
    rel1.setTobereleased('N');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_d.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Relationship is not releasable.");

    rel1.setTobereleased('Y');
    rel1.setName("RR");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_d.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts contain different relationship name.");

    rel1.setName("XR");
    rel1.setRelatedAtom(new Atom.Default(12333));

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_d.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts contain different atom.");

    rel1.setRelatedAtom(mh);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_d.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts that contain different releasable relationships " +
        "to the same latest version MSH MH.");

    //
    // Main Footer
    //

    MEMEToolkit.trace("");

    if (failed) {
      MEMEToolkit.trace("AT LEAST ONE TEST DID NOT COMPLETE SUCCESSFULLY");
    } else {
      MEMEToolkit.trace("ALL TESTS PASSED");

    }
    MEMEToolkit.trace("");

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Finished test of MGV_D ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
