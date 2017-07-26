/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_I
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merges between two {@link Concept}s that
 * both contain releasable {@link Atom}s from the same {@link Source} but different
 * {@link Code}s and that {@link Source} is listed in <code>ic_single</code>.
 * Typically the  {@link MGV_B} and {@link MGV_I} lists should be disjoint.
 *
 * @author MEME Group
 */
public class MGV_I extends AbstractUnaryDataMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_I} check.
   */
  public MGV_I() {
    super();
    setName("MGV_I");
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
    // Get sources list
    //
    String[] sources = getCheckDataValues();
    if (sources == null) {
      return false;
    }

    //
    // Get source and target atoms with source values in the list
    //
    Atom[] source_atoms = source.getRestrictedAtoms(new BySourceRestrictor(
        sources));
    Atom[] target_atoms = target.getRestrictedAtoms(new BySourceRestrictor(
        sources));

    //
    // Find cases of merges where the sources are the same but codes are
    // different.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getSource().equals(source_atoms[i].getSource()) &&
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
    MEMEToolkit.trace("Starting test of MGV_I ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_I- validate():";
    String test_result = null;

    // Create an MGV_I object to work with
    MGV_I mgv_i = new MGV_I();

    UnaryCheckData ucd1 = new UnaryCheckData("Name1", "Type1", "MTH", false);

    UnaryCheckData[] ucds = new UnaryCheckData[1];
    ucds[0] = ucd1;

    mgv_i.setCheckData(ucds);

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);

    Source src = new Source.Default();
    src.setSourceAbbreviation("MTH");
    atom1.setSource(src);

    Code code1 = new Code("Code1");
    atom1.setCode(code1);
    atom1.setTobereleased('Y');

    atom2.setSource(src);
    Code code2 = new Code("Code2");
    atom2.setCode(code2);
    atom2.setTobereleased('Y');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_i.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concept has no atom.");

    // Add atom1 to source
    source.addAtom(atom1);

    // Add atom2 to target
    target.addAtom(atom2);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_i.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts with both contain releasable atoms from the same " +
        "source but different codes and that source is listed in ic_single. " +
        "Typically the MGV_B and MGV_I lists should be disjoint.");

    atom1.setTobereleased('n');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_i.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "One of the merging two concepts does not contain releasable atoms.");

    atom1.setTobereleased('Y');
    atom2.setCode(code1);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_i.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts have the same codes.");

    atom2.setCode(code2);
    src.setSourceAbbreviation("MSH");
    atom1.setSource(src);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_i.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts have different source.");

    src.setSourceAbbreviation("MTH");
    atom1.setSource(src);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_i.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts with both contain releasable atoms from the same " +
        "source but different codes and that source is listed in ic_single. " +
        "Typically the MGV_B and MGV_I lists should be disjoint.");

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
    MEMEToolkit.trace("Finished test of MGV_I ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
