/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  AtomicChangeStatusAction
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
 * Represents an action to change the status of a {@link CoreData}
 * element.
 *
 * @author MEME Group
 */

public class AtomicChangeStatusAction
    extends AtomicAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeStatusAction}.
   */
  public AtomicChangeStatusAction() {
    super();
    setActionName("S");
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified atomic action identifier.
   * @param atomic_action_id the unique identifier for this acction
   */
  public AtomicChangeStatusAction(int atomic_action_id) {
    this();
    setIdentifier(new Identifier.Default(atomic_action_id));
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified atom.
   * @param atom the {@link Atom} to change
   */
  public AtomicChangeStatusAction(Atom atom) {
    this();
    setAffectedTable("C");
    setElementToChange(atom);
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeStatusAction(Relationship relationship) {
    this();
    setAffectedTable("R");
    setElementToChange(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeStatusAction(Attribute attribute) {
    this();
    setAffectedTable("A");
    setElementToChange(attribute);
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified concept.
   * @param concept the {@link Concept} to change
   */
  public AtomicChangeStatusAction(Concept concept) {
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
    setOldValue(String.valueOf(element.getStatus()));
  }

  /**
   * The main method performs a self-QA test
   * @param args An array of arguments.
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
    MEMEToolkit.trace("Starting test of AtomicChangeStatusAction ..." +
                      new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing AtomicChangeStatusAction-";
    String test_result = null;

    // Create an Atom object to work with
    AtomicChangeStatusAction acsa = new AtomicChangeStatusAction();

    //
    // set/getAffectedTable
    //

    acsa.setAffectedTable("C");
    if (acsa.getAffectedTable().equals("C"))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getAffectedTable() = " + acsa.getAffectedTable());

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
    MEMEToolkit.trace("Finished test of AtomicChangeStatusAction ..." +
                      new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
