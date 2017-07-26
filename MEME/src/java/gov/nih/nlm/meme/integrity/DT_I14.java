/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I14
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates {@link Concept}s that contain a current
 * version <code>MSH/N1</code> without a current version
 * same-code <code>MSH/N1</code> or <code>NM</code>.
 *
 * @author MEME Group
 */

public class DT_I14 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I14} check.
   */
  public DT_I14() {
    super();
    setName("DT_I14");
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
    MEMEToolkit.trace("DT_I14: No implementation");
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
    MEMEToolkit.trace("Starting test of DT_I14 ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing DT_I14- validate():";
    String test_result = null;

    // Create a DT_I14 object to work with
    DT_I14 dt_i14 = new DT_I14();
    Concept concept = new Concept.Default(1000);

    test_result = " NO VIOLATION: "; // should return false
    if (!dt_i14.validate(concept)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "No implementation.");

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
    MEMEToolkit.trace("Finished test of DT_I14 ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }

}
