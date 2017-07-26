/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  AtomicChangeFieldAction
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
 * Represents an action to change some {@link CoreData} field.
 *
 * @author MEME Group
 */
public class AtomicChangeFieldAction
    extends AtomicAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeFieldAction}.
   */
  public AtomicChangeFieldAction() {
    super();
    setActionName("CF");
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the specified
   * atomic action identifier.
   * @param atomic_action_id the unique identifier for this action
   */
  public AtomicChangeFieldAction(int atomic_action_id) {
    this();
    setIdentifier(new Identifier.Default(atomic_action_id));
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the
   * specified atom.
   * @param atom the {@link Atom} to change
   */
  public AtomicChangeFieldAction(Atom atom) {
    this();
    setAffectedTable("C");
    setElementToChange(atom);
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeFieldAction(Relationship relationship) {
    this();
    setAffectedTable("R");
    setElementToChange(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeFieldAction(Attribute attribute) {
    this();
    setAffectedTable("A");
    setElementToChange(attribute);
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the
   * specified concept.
   * @param concept the {@link Concept} to change
   */
  public AtomicChangeFieldAction(Concept concept) {
    this();
    setAffectedTable("CS");
    setElementToChange(concept);
  }

  /**
   * Sets the core data element changed by this action.
   * @param element the {@link CoreData} element to change
   */
  public void setElementToChange(CoreData element) {
    setRowIdentifier(element.getIdentifier());
  }

  //
  // Inner Classes
  //

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
    MEMEToolkit.trace("Starting test of AtomicChangeFieldAction ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing AtomicChangeFieldAction-";
    String test_result = null;

    // Create an Atom object to work with
    AtomicChangeFieldAction acfa = new AtomicChangeFieldAction();

    //
    // set/getAffectedTable
    //

    acfa.setAffectedTable("C");
    if (acfa.getAffectedTable().equals("C"))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getAffectedTable() = " + acfa.getAffectedTable());

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
    MEMEToolkit.trace("Finished test of AtomicChangeFieldAction ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
