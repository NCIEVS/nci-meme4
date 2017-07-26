/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_J
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;
import java.util.HashSet;

/**
 * Validates merges between two {@link Concept}s that
 * contain releasable {@link Atom}s across {@link Source}s (usually previous and update
 * versions of same source) but different {@link Code}s where these {@link Source}s are
 * listed in <code>ic_pair</code>.
 *
 * @author MEME Group
 */
public class MGV_J extends AbstractBinaryDataMergeInhibitor {

  //
  // Fields
  //
  private static HashSet sources_set = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_J} check.
   */
  public MGV_J() {
    super();
    setName("MGV_J");
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
    // Acquire check data
    //
    BinaryCheckData[] data = getCheckData();

    //
    // Get source and target atoms
    //
    Atom[] source_atoms = source.getAtoms();
    Atom[] target_atoms = target.getAtoms();

    //
    // Cache source pairs
    //
    if (sources_set == null) {
      sources_set = new HashSet(data.length);
      for (int i = 0; i < data.length; i++) {
        sources_set.add(data[i].getValue1() + data[i].getValue2());
        sources_set.add(data[i].getValue2() + data[i].getValue1());
      }
    }

    //
    // Find cases where one of the specified pairs of sources will be
    // merged together when they have different codes. Typically the
    // pairs of sources will be previous/current versions of the same source
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              (sources_set.contains(source_atoms[i].getSource().
                                    getSourceAbbreviation() +
                                    target_atoms[j].getSource().
                                    getSourceAbbreviation()) ||
               sources_set.contains(target_atoms[j].getSource().
                                    getSourceAbbreviation() +
                                    source_atoms[i].getSource().
                                    getSourceAbbreviation()) &&
               !target_atoms[j].getCode().equals(source_atoms[i].getCode()))) {
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
    MEMEToolkit.trace("Starting test of MGV_J ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_J- validate():";
    String test_result = null;

    // Create an MGV_J object to work with
    MGV_J mgv_j = new MGV_J();

    BinaryCheckData bcd1 = new BinaryCheckData("Name1", "Type11", "Type12",
                                               "MTH", "NLM", false);

    BinaryCheckData[] bcds = new BinaryCheckData[1];
    bcds[0] = bcd1;

    mgv_j.setCheckData(bcds);

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);
    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);

    Source src = new Source.Default();
    Source src2 = new Source.Default();
    src.setSourceAbbreviation("MTH");
    src2.setSourceAbbreviation("NLM");
    atom1.setSource(src);

    Code code1 = new Code("Code1");
    atom1.setCode(code1);
    atom1.setTobereleased('Y');

    atom2.setSource(src2);
    Code code2 = new Code("Code2");
    atom2.setCode(code2);
    atom2.setTobereleased('Y');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_j.validate(source, target)) {
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
    if (mgv_j.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts which contain contain releasable atoms across sources, " +
        "(usually previous and update versions of same source) but different codes " +
        "where these sources are listed in ic_pair.");

    test_result = " VIOLATION:    "; // should return true
    if (mgv_j.validate(target, source)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Same test, other direction.");

    atom2.setTobereleased('n');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_j.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "One of the merging two concepts does not contain releasable atoms.");

    atom2.setTobereleased('Y');

    test_result = " VIOLATION:    "; // should return true
    if (mgv_j.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts which contain contain releasable atoms across sources, " +
        "(usually previous and update versions of same source) but different codes " +
        "where these sources are listed in ic_pair.");

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
    MEMEToolkit.trace("Finished test of MGV_J ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
