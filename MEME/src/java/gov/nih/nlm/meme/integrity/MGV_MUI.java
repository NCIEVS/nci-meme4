/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_MUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validate merges between two {@link Concept}s where
 * both contain current version <code>MSH</code> {@link Atom}s with
 * different MUIs.
 *
 * @author MEME Group
 */
public class MGV_MUI extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_MUI} check.
   */
  public MGV_MUI() {
    super();
    setName("MGV_MUI");
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
    // Obtain MSH atoms from source/target
    //
    ByStrippedSourceRestrictor restrictor = new ByStrippedSourceRestrictor(
        "MSH");
    Atom[] source_atoms = source.getRestrictedAtoms(restrictor);
    Atom[] target_atoms = target.getRestrictedAtoms(restrictor);

    //
    // Find cross-MUI merges among current-version MSH atoms
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable() &&
          source_atoms[i].getSource().isCurrent()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getSource().isCurrent() &&
              source_atoms[i].getSourceConceptIdentifier() !=
              target_atoms[j].getSourceConceptIdentifier()) {
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
    MEMEToolkit.trace("Starting test of MGV_MUI ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_MUI- validate():";
    String test_result = null;

    // Create a MGV_MUI object to work with
    MGV_MUI mgv_mui = new MGV_MUI();

    // Create concept
    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    // Create 2 different atom object
    Atom atom1 = new Atom.Default(12345);
    Atom atom2 = new Atom.Default(12346);

    // Create Attribute
    Attribute[] x_attributes = new Attribute[10];
    x_attributes[0] = new Attribute.Default(1041);
    x_attributes[0].setName("MUI");
    x_attributes[0].setValue("capsule");
    atom1.addAttribute(x_attributes[0]);

    Attribute[] y_attributes = new Attribute[10];
    y_attributes[0] = new Attribute.Default(1042);
    y_attributes[0].setName("MUI");
    y_attributes[0].setValue("tablet");
    atom2.addAttribute(y_attributes[0]);

    // Set both atom to be releasable
    atom1.setTobereleased('Y');
    atom2.setTobereleased('Y');

    // Set current mesh
    Source src = new Source.Default();
    src.setStrippedSourceAbbreviation("MSH");
    src.setSourceAbbreviation("MSH2002");
    src.setIsCurrent(true);

    // Set source atom
    atom1.setSource(src);
    atom2.setSource(src);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_mui.validate(source, target)) {
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
    if (mgv_mui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
        "both contain releasable current version MSH atoms with different MUIs.");

    // Set target atom to be non-releasable
    atom2.setTobereleased('N');

    test_result = " NO VIOLATION: "; // should return true
    if (!mgv_mui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
                      "one contain non-releasable atoms.");

    atom2.setTobereleased('Y');
    src.setIsCurrent(false);
    atom2.setSource(src);

    test_result = " NO VIOLATION: "; // should return true
    if (!mgv_mui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
                      "one contain non-current version MSH atoms.");

    src.setIsCurrent(true);
    atom2.setSource(src);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_mui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Merging two concepts where " +
        "both contain releasable current version MSH atoms with different MUIs.");

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
    MEMEToolkit.trace("Finished test of MGV_MUI ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
