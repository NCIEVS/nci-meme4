/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_G
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merges between two {@link Concept}s
 * where one contains a previous version <code>MSH/MH</code>
 * {@link Atom} and the other contains a releasable, latest
 * version <code>MSH</code> {@link Atom} and their {@link Code}s are different.
 *
 * @author MEME Group
 */

public class MGV_G extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_G} check.
   */
  public MGV_G() {
    super();
    setName("MGV_G");
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
    // Find case where source and target both have current or previous version MSH
    // MH atoms with different codes.  One should have current and the other previous.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].getSource().getStrippedSourceAbbreviation().equals(
          "MSH") &&
          source_atoms[i].getTermgroup().getTermType().equals("MH")) {
        //source_atoms[i].getSource().isPrevious() ) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].getSource().getStrippedSourceAbbreviation().
              equals("MSH") &&
              target_atoms[j].getTermgroup().getTermType().equals("MH") &&
              ( (target_atoms[j].getSource().isCurrent() &&
                 source_atoms[i].getSource().isPrevious() &&
                 target_atoms[j].isReleasable()) ||
               (source_atoms[i].getSource().isCurrent() &&
                target_atoms[j].getSource().isPrevious() &&
                source_atoms[i].isReleasable())) &&
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
    MEMEToolkit.trace("Starting test of MGV_G ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_G- validate():";
    String test_result = null;

    // Create an MGV_G object to work with
    MGV_G mgv_g = new MGV_G();

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);
    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);
    Code code1 = new Code("Code1");
    Code code2 = new Code("Code2");

    Termgroup termgroup = new Termgroup.Default();
    termgroup.setTermType("MH");

    Source src = new Source.Default();
    Source src2 = new Source.Default();
    src.setStrippedSourceAbbreviation("MSH");
    src.setSourceAbbreviation("MSH");
    src.setIsPrevious(true);
    src2.setStrippedSourceAbbreviation("MSH");
    src2.setSourceAbbreviation("MSH");
    src2.setIsCurrent(true);

    atom1.setSource(src);
    atom1.setTobereleased('Y');
    atom1.setTermgroup(termgroup);
    atom1.setCode(code1);

    atom2.setSource(src2);
    atom2.setTobereleased('Y');
    atom2.setTermgroup(termgroup);
    atom2.setCode(code2);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_g.validate(source, target)) {
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
    if (mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
        "where source contains a previous version MSH MH and the target contains a " +
        "latest version MSH atom and their codes are different.");

    source.removeAtom(atom1);
    target.removeAtom(atom2);
    source.addAtom(atom2);
    target.addAtom(atom1);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
        "where target contains a previous version MSH MH and the source contains a " +
        "latest version MSH atom and their codes are different.");

    atom2.setTobereleased('N');
    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
        "where one contains a previous version MSH MH and the other does not contain a " +
        "latest version MSH atom and their codes are different.");

    atom2.setTobereleased('Y');
    src.setStrippedSourceAbbreviation("MTH");
    src.setSourceAbbreviation("MTH");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains non MSH atom.");

    src.setStrippedSourceAbbreviation("MSH");
    src.setSourceAbbreviation("MSH");
    termgroup.setTermType("MM");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains non MH termtype.");

    termgroup.setTermType("MH");
    src.setIsPrevious(false);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains non previous version MSH MH atom.");

    src.setIsPrevious(true);
    atom1.setTobereleased('N');
    atom2.setTobereleased('N');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains non releasable atom.");

    atom1.setTobereleased('Y');
    atom2.setTobereleased('Y');
    atom2.setCode(code1);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
        "where one contains a previous version MSH MH and the other contains a " +
        "latest version MSH atom with the same codes.");

    atom2.setCode(code2);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_g.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
        "where one contains a previous version MSH MH and the other contains a " +
        "latest version MSH atom and their codes are different.");

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
    MEMEToolkit.trace("Finished test of MGV_G ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
