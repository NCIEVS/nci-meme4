/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_C
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merges between two {@link Concept}s where
 * both contain releasable current version <code>MSH</code> {@link Atom}s.
 *
 * @author MEME Group
 */
public class MGV_C extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates the {@link MGV_C} check.
   */
  public MGV_C() {
    super();
    setName("MGV_C");
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
    // Obtain source and target atoms
    //
    Atom[] source_atoms = source.getAtoms();
    Atom[] target_atoms = target.getAtoms();

    //
    // Look for cases where both source and target contain
    // releasable current-version MSH atoms.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable()) {
            if (source_atoms[i].getSource().getStrippedSourceAbbreviation().
                equals("MSH") &&
                source_atoms[i].getSource().isCurrent() &&
                target_atoms[j].getSource().getStrippedSourceAbbreviation().
                equals("MSH") &&
                target_atoms[j].getSource().isCurrent()) {
              return true;
            }
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
    MEMEToolkit.trace("Starting test of MGV_C ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_C- validate():";
    String test_result = null;

    // Create an MGV_C object to work with
    MGV_C mgv_c = new MGV_C();

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);
    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);

    Source src = new Source.Default();
    src.setStrippedSourceAbbreviation("MSH");
    src.setSourceAbbreviation("MSH2002");
    src.setIsCurrent(true);

    atom1.setTobereleased('Y');
    atom1.setSource(src);

    atom2.setTobereleased('Y');
    atom2.setSource(src);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_c.validate(source, target)) {
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
    if (mgv_c.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts with both contain current version MSH atoms.");

    atom2.setTobereleased('n');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_c.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "One of the merging two concepts does not contain releasable atoms.");

    atom2.setTobereleased('Y');
    src.setIsCurrent(false);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_c.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "One of the merging concepts is not current.");

    src.setIsCurrent(true);
    src.setStrippedSourceAbbreviation("MTH");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_c.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "One of the merging concepts is not MSH atom.");

    src.setStrippedSourceAbbreviation("MSH");

    test_result = " VIOLATION:    "; // should return true
    if (mgv_c.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts with both contain current version MSH atoms.");

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
    MEMEToolkit.trace("Finished test of MGV_C ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
