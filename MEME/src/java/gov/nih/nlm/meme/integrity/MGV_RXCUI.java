/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_RXCUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Validate merges between two {@link Concept}s with different RXCUIs.
 * 
 * @author MEME Group
 */
public class MGV_RXCUI extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_RXCUI} check.
   */
  public MGV_RXCUI() {
    super();
    setName("MGV_RXCUI");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s.
   * 
   * @param source
   *          the source {@link Concept}
   * @param target
   *          the target {@link Concept}
   * @return <code>true</code> if constraint violated, <code>false</code>
   *         otherwise
   */
  public boolean validate(Concept source, Concept target) {

    //
    // Obtain rxcui atoms from source/target
    //
    Attribute[] source_rxcuis = source.getAttributesByName("RXCUI");
    Attribute[] target_rxcuis = target.getAttributesByName("RXCUI");

    Set sset = new HashSet();
    Set tset = new HashSet();
    
    //
    // Keep releaseable source/target RXCUIs
    //
    for (int i = 0; i < source_rxcuis.length; i++)
      if (source_rxcuis[i].isReleasable() &&
          source_rxcuis[i].getSource().isCurrent() &&
          source_rxcuis[i].getAtom().isReleasable()) 
        sset.add(source_rxcuis[i].getValue());
    
    if (sset.isEmpty()) return false;
    
    for (int i = 0; i < target_rxcuis.length; i++)
      if (target_rxcuis[i].isReleasable() &&
          target_rxcuis[i].getSource().isCurrent() &&
          target_rxcuis[i].getAtom().isReleasable()) 
        tset.add(target_rxcuis[i].getValue());

    if (tset.isEmpty()) return false;
    
    //
    // Remove all matching target set
    //
    Set sset_copy = new HashSet(sset);
    sset.removeAll(tset);
    tset.removeAll(sset_copy);

    return !sset.isEmpty() && !tset.isEmpty();
  }

  /**
   * Self-qa test.
   * 
   * @param args
   *          command line arguments
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

    MEMEToolkit
        .trace("-------------------------------------------------------");
    MEMEToolkit.trace("Starting test of MGV_RXCUI ..." + new Date());
    MEMEToolkit
        .trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_RXCUI- validate():";
    String test_result = null;

    // Create a MGV_RXCUI object to work with
    MGV_RXCUI mgv_rxcui = new MGV_RXCUI();

    // Create concept
    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    // Create Source
    Source src = new Source.Default();
    src.setIsCurrent(true);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_rxcui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concepts have no atoms.");

    // 
    // Create releasable atom and add releasable RXCUI value
    //
    Atom atom1 = new Atom.Default(12345);
    atom1.setTobereleased('Y');
    atom1.setConcept(source);
    atom1.setSource(src);
    source.addAtom(atom1);
    Attribute[] x_attributes = new Attribute[10];
    x_attributes[0] = new Attribute.Default(1041);
    x_attributes[0].setName("RXCUI");
    x_attributes[0].setValue("RX1");
    x_attributes[0].setTobereleased('Y');
    x_attributes[0].setSource(src);
    x_attributes[0].setAtom(atom1);
    x_attributes[0].setConcept(source);
    atom1.addAttribute(x_attributes[0]);
    source.addAttribute(x_attributes[0]);
    source.setTobereleased('Y');

    Atom atom2 = new Atom.Default(12346);

    atom2.setTobereleased('Y');
    atom2.setConcept(target);
    atom2.setSource(src);
    Attribute[] y_attributes = new Attribute[10];
    y_attributes[0] = new Attribute.Default(1042);
    y_attributes[0].setName("RXCUI");
    y_attributes[0].setValue("RX2");
    y_attributes[0].setTobereleased('Y');
    y_attributes[0].setSource(src);
    y_attributes[0].setAtom(atom2);
    y_attributes[0].setConcept(target);
    atom2.addAttribute(y_attributes[0]);
    target.addAtom(atom2);
    target.addAttribute(y_attributes[0]);
    target.setTobereleased('Y');

    test_result = " VIOLATION:    "; // should return true
    if (mgv_rxcui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result
        + "Merging two concepts where "
        + "both contain releasable current atoms with different RXCUIs.");

    // Set target atom to be non-releasable
    atom2.setTobereleased('N');

    test_result = " NO VIOLATION: "; // should return true
    if (!mgv_rxcui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result
        + "Merging two concepts where one contain non-releasable atoms.");

    src.setIsCurrent(false);
    atom2.setSource(src);
    atom2.setTobereleased('Y');

    test_result = " NO VIOLATION: "; // should return true
    if (!mgv_rxcui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result
        + "Merging two concepts where "
        + "one contain non-current version atoms.");

    y_attributes[0].setTobereleased('Y');
    src.setIsCurrent(true);
    atom2.setSource(src);
    x_attributes[1] = new Attribute.Default(1041);
    x_attributes[1].setName("RXCUI");
    x_attributes[1].setValue("RX1");
    x_attributes[1].setTobereleased('Y');
    x_attributes[1].setSource(src);
    x_attributes[1].setAtom(atom1);
    x_attributes[1].setConcept(source);
    x_attributes[1].setAtom(atom2);
    x_attributes[1].setConcept(target);
    atom2.addAttribute(x_attributes[1]);
    target.addAttribute(x_attributes[1]);

    test_result = " NO VIOLATION: "; // should return true
    if (!mgv_rxcui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result
        + "Merging two concepts where "
        + "source contains RX1 and target has RX1,RX2.");

    x_attributes[2] = new Attribute.Default(1041);
    x_attributes[2].setName("RXCUI");
    x_attributes[2].setValue("RX3");
    x_attributes[2].setTobereleased('Y');
    x_attributes[2].setSource(src);
    x_attributes[2].setAtom(atom1);
    x_attributes[2].setConcept(source);
    atom1.addAttribute(x_attributes[2]);
    source.addAttribute(x_attributes[2]);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_rxcui.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result
        + "Merging two concepts where "
        + "source contains RX1,RX3 and target has RX1,RX2.");

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

    MEMEToolkit
        .trace("-------------------------------------------------------");
    MEMEToolkit.trace("Finished test of MGV_RXCUI ..." + new Date());
    MEMEToolkit
        .trace("-------------------------------------------------------");

  }
}
