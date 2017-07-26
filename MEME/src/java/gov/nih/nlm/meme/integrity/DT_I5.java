/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I5
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates those {@link Concept}s that contain
 * a releasable <code>NON_HUMAN</code> attribute.
 *
 * @author MEME Group
 */

public class DT_I5 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I5} check.
   */
  public DT_I5() {
    super();
    setName("DT_I5");
  }

  //
  // Methods
  //

  /**
   * Validates the specified concept.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code> otherwise
   */
  public boolean validate(Concept source) {
    return source.isNonHuman();
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
    MEMEToolkit.trace("Starting test of DT_I5 ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing DT_I5- validate():";
    String test_result = null;

    // Create a DT_I5 object to work with
    DT_I5 dt_i5 = new DT_I5();
    Concept concept = new Concept.Default(1000);

    test_result = " NO VIOLATION: "; // should return false
    if (!dt_i5.validate(concept)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concept has no attribute.");

    Attribute attribute = new Attribute.Default();
    attribute.setName("NON_HUMAN");
    attribute.setTobereleased('Y');
    concept.addAttribute(attribute);

    test_result = " VIOLATION:    "; // should return true
    if (dt_i5.validate(concept)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains releasable non human.");

    attribute.setTobereleased('n');

    test_result = " NO VIOLATION: "; // should return false
    if (!dt_i5.validate(concept)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains non releasable non human.");

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
    MEMEToolkit.trace("Finished test of DT_I5 ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
