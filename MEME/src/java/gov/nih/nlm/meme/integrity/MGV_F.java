/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_F
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Validates merging between two {@link Concept}s.
     * It is connected by an approved, releasable, non RT?, SY, LK, SFO/LFO, BT?, or
 * NT? current version <code>MSH</code> {@link Relationship}.
 *
 * @author MEME Group
 */
public class MGV_F extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_F} check.
   */
  public MGV_F() {
    super();
    setName("MGV_F");
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
    // Obtain MSH rels
    //
    Relationship[] rels =
        source.getRestrictedRelationships(new ByStrippedSourceRestrictor("MSH"));

    //
    // Find current version MSH rel connecting source and target
    //
    for (int i = 0; i < rels.length; i++) {
      if (rels[i].isReleasable() &&
          !rels[i].getName().equals("SFO/LFO") &&
          !rels[i].getName().equals("RT?") &&
          !rels[i].getName().equals("BT?") &&
          !rels[i].getName().equals("NT?") &&
          !rels[i].getName().equals("SY") &&
          !rels[i].getName().equals("LK") &&
          rels[i].isApproved() &&
          rels[i].getSource().isCurrent() &&
          rels[i].getRelatedConcept().equals(target)) {
        return true;
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
    MEMEToolkit.trace("Starting test of MGV_F ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_F- validate():";
    String test_result = null;

    // Create a MGV_F object to work with
    MGV_F mgv_f = new MGV_F();

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);

    Relationship rel = new Relationship.Default();
    rel.setTobereleased('Y'); // CoreData.FV_RELEASABLE
    rel.setLevel('C'); // CoreData.FV_MTH_ASSERTED
    rel.setStatus('R'); // CoreData.FV_STATUS_REVIEWED
    rel.setRelatedConcept(target);
    rel.setName("RT");

    Source src = new Source.Default();
    src.setStrippedSourceAbbreviation("MSH");
    src.setSourceAbbreviation("MSH");
    src.setIsCurrent(true);

    rel.setSource(src);

    Atom atom = new Atom.Default(12345);
    atom.setSource(src);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_f.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept has no relationship.");

    source.addAtom(atom);
    source.addRelationship(rel);
    target.addAtom(atom);
    target.addRelationship(rel);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_f.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts connected by an approved, releasable, " +
        "non RT?, SY, LK, SFO/LFO, BT?, or NT? relationship whose source " +
        "is MSH.");

    rel.setTobereleased('N');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_f.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept relationship is non releasable.");

    rel.setTobereleased('Y');
    rel.setName("LK");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_f.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Concept relationship is LK.");

    rel.setName("RT");
    rel.setStatus('U');

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_f.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Status is not approved.");

    rel.setStatus('R');
    src.setIsCurrent(false);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_f.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Source is current.");

    src.setIsCurrent(true);
    rel.setRelatedConcept(new Concept.Default(12345));

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_f.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "Related concept is different from target.");

    rel.setRelatedConcept(target);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_f.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "Merging two concepts connected by an approved, releasable, " +
        "non RT?, SY, LK, SFO/LFO, BT?, or NT? relationship whose source " +
        "is not MSH and is not listed in ic_single.");

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
    MEMEToolkit.trace("Finished test of MGV_F ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
