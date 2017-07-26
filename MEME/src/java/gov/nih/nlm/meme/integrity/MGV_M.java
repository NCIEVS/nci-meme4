/*****************************************************************************
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_M
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.Date;

/**
 * Validates merges between two {@link Concept}s that
 * contain releasable "NEC" {@link Atom}s with different {@link Source}s.
 *
 * @author MEME Group
 */
public class MGV_M extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_M} check.
   */
  public MGV_M() {
    super();
    setName("MGV_M");
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
    // Get source and target atoms
    //
    Atom[] source_atoms = source.getAtoms();
    Atom[] target_atoms = target.getAtoms();

    //
    // Set up vars
    //
    FieldedStringTokenizer st = null;
    String source_atom_name = null;
    String target_atom_name = null;

    //
    // Find NEC atoms from different sources being merged
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable()) {
            if (!source_atoms[i].getSource().getStrippedSourceAbbreviation().
                equals(target_atoms[j].getSource().
                       getStrippedSourceAbbreviation())) {

              // SNOMEDCT allowed to merge with SNMI98 and RCD99
              if (source_atoms[i].getSource().getStrippedSourceAbbreviation().
                  equals("SNOMEDCT") &&
                  (target_atoms[j].getSource().getStrippedSourceAbbreviation().
                   equals("SNMI") ||
                   target_atoms[j].getSource().getStrippedSourceAbbreviation().
                   equals("RCD"))) {
                continue;
              }

              if (target_atoms[j].getSource().getStrippedSourceAbbreviation().
                  equals("SNOMEDCT") &&
                  (source_atoms[i].getSource().getStrippedSourceAbbreviation().
                   equals("SNMI") ||
                   source_atoms[i].getSource().getStrippedSourceAbbreviation().
                   equals("RCD"))) {
                continue;
              }

              if (!target_atoms[j].getLanguage().getAbbreviation().equals(
                  source_atoms[i].getLanguage().getAbbreviation())) {
                continue;
              }

              source_atom_name = source_atoms[i].getString();
              target_atom_name = target_atoms[j].getString();
              st = new FieldedStringTokenizer(source_atom_name, " ");
              while (st.hasMoreTokens()) {
                if (st.nextToken().toString().toUpperCase().equals("NEC")) {
                  return true;
                }
              }
              st = new FieldedStringTokenizer(target_atom_name, " ");
              while (st.hasMoreTokens()) {
                if (st.nextToken().toString().toUpperCase().equals("NEC")) {
                  return true;
                }
              }

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
    MEMEToolkit.trace("Starting test of MGV_M ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_M- validate():";
    String test_result = null;

    // Create a MGV_M object to work with
    MGV_M mgv_m = new MGV_M();

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    Atom atom1 = new Atom.Default(12345);
    atom1.setString("Soft tissue neoplasms malignant NEC (excluding sarcomas) in MDR33 exclude malignant mdr33 nec neoplasm sarcoma soft tissue");
    Atom atom2 = new Atom.Default(12346);
    atom2.setString("Soft tissue NEC (excluding sarcomas) in MDR33 exclude malignant mdr33 nec neoplasm sarcoma soft tissue");

    Source src1 = new Source.Default();
    src1.setSourceAbbreviation("MTH");
    Source src2 = new Source.Default();
    src2.setSourceAbbreviation("MDR");

    atom1.setSource(src1);
    atom2.setSource(src2);

    atom1.setTobereleased('Y');
    atom2.setTobereleased('Y');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_m.validate(source, target)) {
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
    if (mgv_m.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts that contain releasable NEC atoms with different sources.");

    atom1.setTobereleased('n');
    atom2.setTobereleased('n');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_m.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging concept has no releasable atom.");

    atom1.setTobereleased('Y');
    atom2.setTobereleased('Y');
    src2.setSourceAbbreviation("MTH");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_m.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts that contain releasable NEC atoms with the same sources.");

    src2.setSourceAbbreviation("MDR");
    atom1.setString("Soft tissue neoplasms malignant (excluding sarcomas) in MDR33 exclude malignant mdr33 neoplasm sarcoma soft tissue");
    atom2.setString("Soft tissue neoplasms malignant (excluding sarcomas) in MDR33 exclude malignant mdr33 neoplasm sarcoma soft tissue");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_m.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts does not contain any NEC atoms.");

    atom1.setString("Soft tissue neoplasms malignant NEC (excluding sarcomas) in MDR33 exclude malignant mdr33 nec neoplasm sarcoma soft tissue");
    atom2.setString("Soft tissue neoplasms malignant NEC (excluding sarcomas) in MDR33 exclude malignant mdr33 nec neoplasm sarcoma soft tissue");

    test_result = " VIOLATION:    "; // should return true
    if (mgv_m.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts that contain releasable NEC atoms with different sources.");

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
    MEMEToolkit.trace("Finished test of MGV_M ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
