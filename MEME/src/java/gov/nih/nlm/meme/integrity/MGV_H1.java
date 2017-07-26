/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_H1
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merges between two {@link Concept}s where
 * both contain releasable current version <code>MSH</code> {@link Atom}s
 * with different {@link Code}s,
 * specifically (D-D, Q-Q, D-Q, Q-D). However, D-Q may exist together if the
 * D has termgroup EN, EP, or MH and the Q has termgroup GQ.
 *
 * @author MEME Group
 */
public class MGV_H1 extends AbstractMergeInhibitor implements MoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_H1} check.
   */
  public MGV_H1() {
    super();
    setName("MGV_H1");
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
    ByStrippedSourceRestrictor restrictor = new ByStrippedSourceRestrictor(
        "MSH");
    Atom[] source_atoms = source.getRestrictedAtoms(restrictor);
    return validate(source, target, source_atoms);
  }

  /**
       * Validates the pair of {@link Concept}s where the specified {@link Atom}s are
   * moving from the source to the target concept.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @param source_atoms {@link Atom}s from the source concept to be moved to target concept
   * @return <code>true</code> if constraint violated, <code>false</code>
   * otherwise
   */
  public boolean validate(Concept source, Concept target, Atom[] source_atoms) {

    //
    // Get MSH atoms from target concept.
    //
    ByStrippedSourceRestrictor restrictor = new ByStrippedSourceRestrictor(
        "MSH");
    Atom[] target_atoms = target.getRestrictedAtoms(restrictor);

    //
    // Find cases where releasable current version MSH atoms with
    // different codes (in the specific combinations) are being merged.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable() &&
          source_atoms[i].getSource().getStrippedSourceAbbreviation().equals(
          "MSH") &&
          source_atoms[i].getSource().isCurrent() &&
          (source_atoms[i].getCode().toString().startsWith("D") ||
           source_atoms[i].getCode().toString().startsWith("Q"))) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getSource().isCurrent() &&
              (target_atoms[j].getCode().toString().startsWith("D") ||
               target_atoms[j].getCode().toString().startsWith("Q")) &&
              !target_atoms[j].getCode().equals(source_atoms[i].getCode())) {
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
    MEMEToolkit.trace("Starting test of MGV_H1 ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_H- validate():";
    String test_result = null;

    // Create an MGV_H1 object to work with
    MGV_H1 mgv_h1 = new MGV_H1();

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);

    Code code1 = new Code("D00001");
    Code code2 = new Code("Q10001");

    atom1.setTobereleased('Y');
    atom2.setTobereleased('Y');

    atom1.setCode(code1);
    atom2.setCode(code2);

    Source src = new Source.Default();
    src.setStrippedSourceAbbreviation("MSH");
    src.setSourceAbbreviation("MSH2002");
    src.setIsCurrent(true);

    atom1.setSource(src);
    atom2.setSource(src);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_h1.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concept has no atom.");

    // Add atom
    source.addAtom(atom1);
    target.addAtom(atom2);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_h1.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
        "both contain releasable current version MSH atoms with different codes (D-Q).");

    atom1.setTobereleased('N');
    atom2.setTobereleased('N');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_h1.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains non releasable atom.");

    atom1.setTobereleased('Y');
    atom2.setTobereleased('Y');
    src.setIsCurrent(false);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_h1.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Source is not current.");

    src.setIsCurrent(true);
    atom1.setCode(new Code("Z00001"));

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_h1.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains code that does not start with D or Q.");

    atom1.setCode(code2);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_h1.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
        "both contain releasable current version MSH atoms with the same codes.");

    atom1.setCode(code1);
    atom2.setCode(code2);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_h1.validate(target, source)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
        "both contain releasable current version MSH atoms with different codes (Q-D).");

    atom2.setCode(new Code("D5"));
    test_result = " VIOLATION:    "; // should return true
    if (mgv_h1.validate(target, source)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
        "both contain releasable current version MSH atoms with different codes (D-D).");

    atom2.setCode(new Code("Q5"));
    atom1.setCode(code2);
    test_result = " VIOLATION:    "; // should return true
    if (mgv_h1.validate(target, source)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
        "both contain releasable current version MSH atoms with different codes (Q-Q).");

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
    MEMEToolkit.trace("Finished test of MGV_H1 ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
