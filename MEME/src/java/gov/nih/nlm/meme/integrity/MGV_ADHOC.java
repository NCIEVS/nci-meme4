/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_ADHOC
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merges between two {@link Concept}s
     * where one contains a releasable, current version <code>MDR</code> {@link Atom}
 * and the other has a chemical {@link ConceptSemanticType}.
 *
 * @author MEME Group
 */
public class MGV_ADHOC extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_ADHOC} check.
   */
  public MGV_ADHOC() {
    super();
    setName("MGV_ADHOC");
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
    // If source has a chemical semantic type, look
    // for releasable MDR atoms in target
    //
    if (source.hasChemicalSemanticType()) {
      Atom[] atoms = target.getRestrictedAtoms(new ByStrippedSourceRestrictor(
          "MDR"));
      for (int i = 0; i < atoms.length; i++) {
        if (atoms[i].isReleasable()) {
          return true;
        }
      }
    }

    //
    // If target has a chemical semantic type, look
    // for releasable MDR atoms in source
    //
    if (target.hasChemicalSemanticType()) {
      Atom[] atoms = source.getRestrictedAtoms(new ByStrippedSourceRestrictor(
          "MDR"));
      for (int i = 0; i < atoms.length; i++) {
        if (atoms[i].isReleasable()) {
          return true;
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
    MEMEToolkit.trace("Starting test of MGV_ADHOC ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_ADHOC- validate():";
    String test_result = null;

    // Create a MGV_ADHOC object to work with
    MGV_ADHOC mgv_adhoc = new MGV_ADHOC();

    ConceptSemanticType sty = (ConceptSemanticType)new ConceptSemanticType();
    sty.setIsChemical(true);
    sty.setTobereleased('Y');

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    source.addAttribute(sty);
    target.addAttribute(sty);

    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);

    Source src = new Source.Default();
    src.setStrippedSourceAbbreviation("MDR");

    atom1.setSource(src);
    atom2.setSource(src);

    atom1.setTobereleased('Y');
    atom2.setTobereleased('Y');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_adhoc.validate(source, target)) {
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
    if (mgv_adhoc.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
        "where both contain a releasable, current version MDR atom and both " +
        "have chemical semantic types.");

    src.setStrippedSourceAbbreviation("MTH");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_adhoc.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
                      "where one contains a non MDR atom.");

    src.setStrippedSourceAbbreviation("MDR");
    atom1.setTobereleased('N');
    atom2.setTobereleased('N');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_adhoc.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
                      "where both contains a non releasable atom.");

    atom1.setTobereleased('N');
    atom2.setTobereleased('Y');

    test_result = " VIOLATION:    "; // should return true
    if (mgv_adhoc.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
        "where target contains a releasable, current version MDR atom and the source " +
        "has a chemical semantic type.");

    atom1.setTobereleased('Y');
    atom2.setTobereleased('N');

    test_result = " VIOLATION:    "; // should return true
    if (mgv_adhoc.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts " +
        "where source contains a releasable, current version MDR atom and the target " +
        "has a chemical semantic type.");

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
    MEMEToolkit.trace("Finished test of MGV_ADHOC ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
