/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I8XR
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Superclass for
 * the various code <code>DT_I8XR*</code> integrity checks.
 * Validates those {@link Concept}s that contain:
 * <pre>`
 * - MSH "entry term" with Q# code matching a MSH TQ in a different {@link Concept} with
 *   an approved, releasable XR {@link Relationship} to that {@link Concept} which overrides any
 *   other valid {@link Relationship}s.
 *
 * - MSH "entry term" with C# code matching a MSH NM in a different {@link Concept} with
     *   NO approved (or unreviewed), releasable, RT, NT, BT, or non MTH asserted LK
 *   {@link Relationship} to that {@link Concept}.
 *
 * - MSH "entry term" with C# code matching a MSH NM in a different {@link Concept} with
 *   an approved, releasableXR {@link Relationship} to that {@link Concept} which overrides any
 *   other valid {@link Relationship}s.
 * </pre>
 * @author MEME Group
 */

public class DT_I8XR extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates {@link DT_I8XR}.
   */
  private DT_I8XR() {}

  //
  // Methods
  //

  /**
   * Validates the specified concept.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code>, otherwise
   */
  public boolean validate(Concept source) {
    return false;
  }

  /**
   * Self-qa test.
   * @param args command line arguments
   */
  public static void main(String[] args) {

    if (args.length < 1) {
      return;
    }

    String vcode = null;
    if (args[1].equals("D")) {
      vcode = "A";
    } else if (args[1].equals("Q")) {
      vcode = "B";
    } else if (args[1].equals("C")) {
      vcode = "C";

    }
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
    MEMEToolkit.trace("Starting test of DT_I8" + vcode + "XR ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing DT_I8" + vcode + "XR- validate():";
    String test_result = null;

    // Create a DT_I8XR object to work with
    DT_I8 dt_i8 = new DT_I8();
    dt_i8.setTermType(args[0]);
    dt_i8.setXR(true);

    // Create concept and related concept
    Concept c1 = new Concept.Default(12345);

    // Create entry termgroup
    Termgroup et_tag = new Termgroup.Default("MSH2002/EN");

    // Create mesh termgroup
    Termgroup msh_tag = new Termgroup.Default("MSH2002/" + args[0]);

    // Create MSH
    Source msh = new Source.Default();
    Source msh2 = new Source.Default();

    msh.setIsCurrent(true);
    msh.setStrippedSourceAbbreviation("MSH");
    msh.setSourceAbbreviation("MSH2002");

    msh2.setIsCurrent(false);
    msh2.setStrippedSourceAbbreviation("MSH");
    msh2.setSourceAbbreviation("MSH2002");

    // Create entry term
    MeshEntryTerm et = new MeshEntryTerm(12345);
    et.setSource(msh);
    et.setTermgroup(et_tag);

    // Create main heading
    Atom mh = new Atom.Default(12346);
    mh.setSource(msh);
    mh.setTermgroup(msh_tag);

    // Create code
    Code code1 = new Code(args[1] + "12345");
    Code code2 = new Code(args[1] + "12346");

    et.setCode(code1);
    mh.setCode(code2);
    et.setMainHeading(mh);
    mh.setConcept(c1);

    Relationship rel = new Relationship.Default();
    rel.setLevel('C'); // CoreData.FV_MTH_ASSERTED
    rel.setStatus('R'); // CoreData.FV_STATUS_REVIEWED
    rel.setTobereleased('Y'); // CoreData.FV_RELEASABLE
    rel.setRelatedConcept(c1);
    rel.setSource(msh);
    rel.setName("XR");

    // Case# 1 no violation
    test_result = " NO VIOLATION: "; // should return false
    if (!dt_i8.validate(c1)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept has no relationship.");

    // Case# 2 violation
    c1.addRelationship(rel);
    c1.addAtom(et);
    c1.addAtom(mh);

    test_result = " VIOLATION:    "; // should return true
    if (dt_i8.validate(c1)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains MSH entry term with " +
                      "an approved, releasable XR relationship.");

    // Case# 3 not a violation
    rel.setLevel('S');

    test_result = " NO VIOLATION: "; // should return false
    if (!dt_i8.validate(c1)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Relationship is not MTH asserted.");

    // Case# 4 not a violation
    rel.setLevel('C');
    rel.setTobereleased('n');

    test_result = " NO VIOLATION: "; // should return false
    if (!dt_i8.validate(c1)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Relationship is not releasable.");

    // Case# 5 not a violation
    rel.setTobereleased('Y');
    rel.setName("RT");

    test_result = " NO VIOLATION: "; // should return false
    if (!dt_i8.validate(c1)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Relationship is not XR.");

    // Case# 6 not a violation
    rel.setName("XR");
    rel.setStatus('N');

    test_result = " NO VIOLATION: "; // should return false
    if (!dt_i8.validate(c1)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Relationship is not reviewed.");

    rel.setStatus('R');

    test_result = " VIOLATION:    "; // should return true
    if (dt_i8.validate(c1)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept contains MSH entry term with " +
                      "an approved, releasable XR relationship.");

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
    MEMEToolkit.trace("Finished test of DT_I8" + vcode + "XR ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
