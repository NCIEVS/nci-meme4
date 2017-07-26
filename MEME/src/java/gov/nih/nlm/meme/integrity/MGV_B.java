/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_B
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merges between two {@link Concept}s that
 * both contain releasable {@link Atom}s from the same
 * {@link Source} and that {@link Source} is
 * listed in <code>ic_single</code>.
 *
 * @author MEME Group
 */
public class MGV_B extends AbstractUnaryDataMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_B} check.
   */
  public MGV_B() {
    super();
    setName("MGV_B");
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
    // Get unary check data
    //
    String[] sources = getCheckDataValues();

    if (sources == null) {
      return false;
    }

    //
    // Get source and target atoms from specified list of sources
    //
    Atom[] source_atoms = source.getRestrictedAtoms(new BySourceRestrictor(
        sources));
    Atom[] target_atoms = target.getRestrictedAtoms(new BySourceRestrictor(
        sources));

    //
    // Look for cases of within-source merges involving releasable atoms.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getSource().equals(source_atoms[i].getSource())) {
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
    MEMEToolkit.trace("Starting test of MGV_B ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_B- validate():";
    String test_result = null;

    // Create an MGV_B object to work with
    MGV_B mgv_b = new MGV_B();

    UnaryCheckData ucd1 = new UnaryCheckData("Name1", "Type1", "MTH", false);

    UnaryCheckData[] ucds = new UnaryCheckData[1];
    ucds[0] = ucd1;

    mgv_b.setCheckData(ucds);

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);

    Source src = new Source.Default();
    src.setSourceAbbreviation("MTH");
    atom1.setSource(src);

    atom1.setTobereleased('Y');

    atom2.setSource(src);
    atom2.setTobereleased('Y');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_b.validate(source, target)) {
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
    if (mgv_b.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts with both contain releasable atoms from the same " +
        "source and that source is listed in ic_single. ");

    atom1.setTobereleased('n');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_b.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "One of the merging two concepts does not contain releasable atoms.");

    atom1.setTobereleased('Y');
    src.setSourceAbbreviation("MSH");
    atom1.setSource(src);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_b.validate(source, target)) {
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
    if (mgv_b.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts with both contain releasable atoms from the same " +
        "source and that source is listed in ic_single. ");

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
    MEMEToolkit.trace("Finished test of MGV_B ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
