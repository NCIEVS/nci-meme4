/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  AtomicChangeReleasabilityAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Represents an action to change the releasability of a {@link CoreData}
 * element.
 *
 * @author MEME Group
 */

public class AtomicChangeReleasabilityAction
    extends AtomicAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeReleasabilityAction}.
   */
  public AtomicChangeReleasabilityAction() {
    super();
    setActionName("T");
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified atomic action identifier.
   * @param atomic_action_id the atomic action id.
   */
  public AtomicChangeReleasabilityAction(int atomic_action_id) {
    this();
    setIdentifier(new Identifier.Default(atomic_action_id));
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified atom.
   * @param atom the {@link Atom} to change
   */
  public AtomicChangeReleasabilityAction(Atom atom) {
    this();
    setAffectedTable("C");
    setElementToChange(atom);
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeReleasabilityAction(Relationship relationship) {
    this();
    setAffectedTable("R");
    setElementToChange(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeReleasabilityAction(Attribute attribute) {
    this();
    setAffectedTable("A");
    setElementToChange(attribute);
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified concept.
   * @param concept the {@link Concept} to change
   */
  public AtomicChangeReleasabilityAction(Concept concept) {
    this();
    setAffectedTable("CS");
    setElementToChange(concept);
  }

  /**
   * Sets the core data element changed by this action.
   * @param element the {@link CoreData} to change
   */
  public void setElementToChange(CoreData element) {
    setRowIdentifier(element.getIdentifier());
    setOldValue(String.valueOf(element.getTobereleased()));
  }

  /**
   * The main method performs a self-QA test
   * @param args An array of argument.
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
    MEMEToolkit.trace("Starting test of AtomicChangeReleasabilityAction ..." +
                      new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing AtomicChangeReleasabilityAction-";
    String test_result = null;

    // Create an Atom object to work with
    AtomicChangeReleasabilityAction acra = new AtomicChangeReleasabilityAction();

    //
    // set/getAffectedTable
    //

    acra.setAffectedTable("C");
    if (acra.getAffectedTable().equals("C"))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getAffectedTable() = " + acra.getAffectedTable());

    //
    // Main Footer
    //

    MEMEToolkit.trace("");

    if (failed)
      MEMEToolkit.trace("AT LEAST ONE TEST DID NOT COMPLETE SUCCESSFULLY");
    else
      MEMEToolkit.trace("ALL TESTS PASSED");

    MEMEToolkit.trace("");

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Finished test of AtomicChangeReleasabilityAction ..." +
                      new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
