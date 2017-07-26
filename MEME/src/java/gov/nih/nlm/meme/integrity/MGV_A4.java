/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_A4
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merges between two {@link Concept}s that
 * were released previously with different CUIs.
 *
 * @author MEME Group
 */
public class MGV_A4 extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_A4} check.
   */
  public MGV_A4() {
    super();
    setName("MGV_A4");
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
    // Get all source and target atoms
    //
    Atom[] source_atoms = source.getAtoms();
    Atom[] target_atoms = target.getAtoms();

    //
    // Find releasable atom from source concept
    // having different last release cui from releasable
    // target concept atom.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable() &&
          source_atoms[i].getLastReleaseCUI() != null) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getLastReleaseCUI() != null &&
              !target_atoms[j].getLastReleaseCUI().equals(source_atoms[i].
              getLastReleaseCUI())) {
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
    MEMEToolkit.trace("Starting test of MGV_A4 ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_A4- validate():";
    String test_result = null;

    // Create a MGV_A4 object to work with
    MGV_A4 mgv_a4 = new MGV_A4();
    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);
    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_a4.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concept has no atoms.");

    atom1.setTobereleased('Y'); // CoreData.FV_RELEASABLE
    atom1.setLastReleaseCUI(new CUI("C0000001"));
    source.addAtom(atom1);

    atom2.setTobereleased('Y'); // CoreData.FV_RELEASABLE
    atom2.setLastReleaseCUI(new CUI("C0000002"));
    target.addAtom(atom2);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_a4.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept has a merging two concepts that were released previously in different CUIs.");

    atom2.setLastReleaseCUI(new CUI("C0000001"));

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_a4.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Concept that were released previously have the same CUIS.");

    atom2.setLastReleaseCUI(new CUI("C0000002"));
    atom1.setTobereleased('N');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_a4.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concept is not releasable.");

    atom1.setTobereleased('Y');
    atom1.setLastReleaseCUI(null);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_a4.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Last release CUI is null.");

    atom1.setLastReleaseCUI(new CUI("C0000002"));

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_a4.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept has a merging two concepts that were released previously has the same CUIs.");

    atom1.setLastReleaseCUI(new CUI("C0000001"));

    test_result = " VIOLATION:    "; // should return true
    if (mgv_a4.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept has a merging two concepts that were released previously in different CUIs.");

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
    MEMEToolkit.trace("Finished test of MGV_A4 ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
