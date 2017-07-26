/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  AtomicDeleteAction
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
 * Represents an action to delete some {@link CoreData} element.
 *
 * @author MEME Group
 */

public class AtomicDeleteAction
    extends AtomicAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicDeleteAction}.
   */
  public AtomicDeleteAction() {
    super();
    setActionName("D");
    setNewValue("Y");
    setOldValue("N");
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed atomic action identifier.
   *@param atomic_action_id the unique identifier for this action
   */
  public AtomicDeleteAction(int atomic_action_id) {
    this();
    setIdentifier(new Identifier.Default(atomic_action_id));
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed atom.
   * @param atom the {@link Atom} to delete
   */
  public AtomicDeleteAction(Atom atom) {
    this();
    setAffectedTable("C");
    setElementToDelete(atom);
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed relationship.
   * @param relationship the {@link Relationship} to delete
   */
  public AtomicDeleteAction(Relationship relationship) {
    this();
    setAffectedTable("R");
    setElementToDelete(relationship);
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed attribute.
   * @param attribute the {@link Attribute} to delete
   */
  public AtomicDeleteAction(Attribute attribute) {
    this();
    setAffectedTable("A");
    setElementToDelete(attribute);
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed concept.
   * @param concept the {@link Concept} to delete
   */
  public AtomicDeleteAction(Concept concept) {
    this();
    setAffectedTable("CS");
    setElementToDelete(concept);
  }

  //
  // Additional AtomicDeleteAction Methods
  //

  /**
   * Sets the core data element deleted by this action.
   * @param element the {@link CoreData} to change
   */
  public void setElementToDelete(CoreData element) {
    setRowIdentifier(element.getIdentifier());
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
    MEMEToolkit.trace("Starting test of AtomicDeleteAction ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing AtomicDeleteAction-";
    String test_result = null;

    // Create an Atom object to work with
    AtomicDeleteAction ada = new AtomicDeleteAction();

    //
    // set/getAffectedTable
    //

    //CoreData cd = new CoreData.Default();
    ada.setAffectedTable("C");
    if (ada.getAffectedTable().equals("C"))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getAffectedTable() = " + ada.getAffectedTable());

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
    MEMEToolkit.trace("Finished test of AtomicDeleteAction ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
