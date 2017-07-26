/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  AtomicChangeAtomAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.AtomElement;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Represents an action to change the atom that an {@link AtomElement}
 * is connected to.  Typically this moves an {@link Attribute} or
 * {@link Relationship} from one {@link Atom} to another.
 *
 * @author MEME Group
 */

public class AtomicChangeAtomAction
    extends AtomicAction {

  //
  // Fields
  //

  Atom old_atom = null;
  Atom new_atom = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeAtomAction}.
   */
  public AtomicChangeAtomAction() {
    super();
    setActionName("A");
  }

  /**
   * Instantiates an {@link AtomicChangeAtomAction} with the specified
   * atomic action identifier.
   * @param atomic_action_id the unique id of this action
   */
  public AtomicChangeAtomAction(int atomic_action_id) {
    this();
    setIdentifier(new Identifier.Default(atomic_action_id));
  }

  /**
   * Instantiates an {@link AtomicChangeAtomAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeAtomAction(Relationship relationship) {
    this();
    setAffectedTable("R");
    setElementToChange(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeAtomAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeAtomAction(Attribute attribute) {
    this();
    setAffectedTable("A");
    setElementToChange(attribute);
  }

  //
  // Methods
  //

  /**
   * Sets the atom element inserted by this action.
   * @param element the {@link AtomElement} changed by this action
   */
  public void setElementToChange(AtomElement element) {
    setRowIdentifier(element.getIdentifier());
    setOldAtom(element.getAtom());
  }

  /**
   * Sets the old {@link Atom}.
   * @param old_atom the old {@link Atom}.
   */
  public void setOldAtom(Atom old_atom) {
    this.old_atom = old_atom;
  }

  /**
   * Sets the new {@link Atom}.
   * @param new_atom the new {@link Atom}.
   */
  public void setNewAtom(Atom new_atom) {
    this.new_atom = new_atom;
  }

  //
  // Overriden AtomicAction method
  //

  /**
   * Overrides {@link AtomicAction#getOldValue()}.
   */
  public String getOldValue() {
    return old_atom.getIdentifier().toString();
  }

  /**
   * Overrides {@link AtomicAction#getOldValueAsInt()}.
   */
  public int getOldValueAsInt() {
    return old_atom.getIdentifier().intValue();
  }

  /**
   * Overrides {@link AtomicAction#getNewValue()}.
   */
  public String getNewValue() {
    return new_atom.getIdentifier().toString();
  }

  /**
   * Overrides {@link AtomicAction#getNewValueAsInt()}.
   */
  public int getNewValueAsInt() {
    return new_atom.getIdentifier().intValue();
  }

  /**
   * The main method performs a self-QA test.
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
    MEMEToolkit.trace("Starting test of AtomicChangeAtomAction ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing AtomicChangeAtomAction-";
    String test_result = null;

    // Create an Atom object to work with
    AtomicChangeAtomAction acaa = new AtomicChangeAtomAction();

    //
    // set/getAffectedTable
    //

    //CoreData cd = new CoreData.Default();
    acaa.setAffectedTable("C");
    if (acaa.getAffectedTable().equals("C"))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getAffectedTable() = " + acaa.getAffectedTable());

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
    MEMEToolkit.trace("Finished test of AtomicChangeAtomAction ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
