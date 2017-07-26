/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_MM
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merges between {@link Concept}s where both contain
 * releasable <code>MTH/MM</code> {@link Atom}s.
 *
 * @author MEME Group
 */
public class MGV_MM extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_MM} check.
   */
  public MGV_MM() {
    super();
    setName("MGV_MM");
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
    // Get MTH atoms from source/target
    //
    Atom[] source_atoms = source.getRestrictedAtoms(new BySourceRestrictor(
        "MTH"));
    Atom[] target_atoms = target.getRestrictedAtoms(new BySourceRestrictor(
        "MTH"));

    //
    // Find releasable MTH/MM atoms in both source and target
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].getTermgroup().getTermType().equals("MM") &&
          source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[i].getTermgroup().getTermType().equals("MM") &&
              target_atoms[i].isReleasable()) {
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
    MEMEToolkit.trace("Starting test of MGV_MM ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_MM- validate():";
    String test_result = null;

    // Create a MGV_MM object to work with
    MGV_MM mgv_mm = new MGV_MM();

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_mm.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concept has no atom.");

    // Create source, termgroup and atom object
    Source src = new Source.Default();
    Termgroup termgroup = new Termgroup.Default();
    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);

    src.setSourceAbbreviation("MTH");
    termgroup.setSource(src);
    termgroup.setTermType("MM");
    atom1.setSource(src);
    atom1.setTermgroup(termgroup);
    atom1.setTobereleased('Y');

    // Add an atom into concept
    source.addAtom(atom1);

    atom2.setSource(src);
    atom2.setTermgroup(termgroup);
    atom2.setTobereleased('Y');

    // Add an atom into concept
    target.addAtom(atom2);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_mm.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging concepts where both contain releasable MTH/MM atoms.");

    atom2.setTobereleased('n');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_mm.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "One of the merging concepts does not contain releasable MTH/MM atoms.");

    atom2.setTobereleased('Y');
    src.setSourceAbbreviation("MSH");
    atom1.setSource(src);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_mm.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "One of the merging concepts does not contain MTH source.");

    src.setSourceAbbreviation("MTH");
    atom1.setSource(src);
    termgroup.setTermType("PP");
    atom1.setTermgroup(termgroup);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_mm.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "One of the merging concepts does not contain MM termtype.");

    termgroup.setTermType("MM");
    atom1.setTermgroup(termgroup);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_mm.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging concepts where both contain releasable MTH/MM atoms.");

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
    MEMEToolkit.trace("Finished test of MGV_MM ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
