/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  AtomicChangeConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptElement;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;

/**
 * Represents an action to change the atom that an {@link ConceptElement}
     * is connected to.  Typically this moves an {@link Atom}, {@link Attribute}, or
 * {@link Relationship} from one {@link Concept} to another.
 *
 * @author MEME Group
 */

public class AtomicChangeConceptAction
    extends AtomicAction {

  //
  // Fields
  //

  Concept old_concept = null;
  Concept new_concept = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeConceptAction}.
   */
  public AtomicChangeConceptAction() {
    super();
    setActionName("C");
  }

  /**
   * Instantiates an {@link AtomicChangeConceptAction} with the
   * specified atomic action identifier.
   * @param atomic_action_id the unique identifier for this action
   */
  public AtomicChangeConceptAction(int atomic_action_id) {
    this();
    setIdentifier(new Identifier.Default(atomic_action_id));
  }

  /**
   * Instantiates an {@link AtomicChangeConceptAction} with the
   * specified atom.
   * @param atom the {@link Atom} to change
   */
  public AtomicChangeConceptAction(Atom atom) {
    this();
    setAffectedTable("C");
    setElementToChange(atom);
  }

  /**
   * Instantiates an {@link AtomicChangeConceptAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeConceptAction(Relationship relationship) {
    this();
    setAffectedTable("R");
    setElementToChange(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeConceptAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeConceptAction(Attribute attribute) {
    this();
    setAffectedTable("A");
    setElementToChange(attribute);
  }

  //
  // Methods
  //

  /**
   * Sets the concept element changed by this action.
   * @param element the {@link ConceptElement} to change
   */
  public void setElementToChange(ConceptElement element) {
    setRowIdentifier(element.getIdentifier());
    setOldConcept(element.getConcept());
  }

  /**
   * Sets the old {@link Concept}.
   * @param old_concept the old {@link Concept}.
   */
  public void setOldConcept(Concept old_concept) {
    this.old_concept = old_concept;
  }

  /**
   * Sets the new {@link Concept}.
   * @param new_concept the new {@link Concept}.
   */
  public void setNewConcept(Concept new_concept) {
    this.new_concept = new_concept;
  }

  //
  // Overriden AtomicAction method
  //

  /**
   * Overrides {@link AtomicAction#getOldValue()}.
   */
  public String getOldValue() {
    return old_concept.getIdentifier().toString();
  }

  /**
   * Overrides {@link AtomicAction#getOldValueAsInt()}.
   */
  public int getOldValueAsInt() {
    return old_concept.getIdentifier().intValue();
  }

  /**
   * Overrides {@link AtomicAction#getNewValue()}.
   */
  public String getNewValue() {
    return new_concept.getIdentifier().toString();
  }

  /**
   * Overrides {@link AtomicAction#getNewValueAsInt()}.
   */
  public int getNewValueAsInt() {
    return new_concept.getIdentifier().intValue();
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
    MEMEToolkit.trace("Starting test of AtomicChangeConceptAction ..." +
                      new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing AtomicChangeConceptAction-";
    String test_result = null;

    // Create an Atom object to work with
    AtomicChangeConceptAction acca = new AtomicChangeConceptAction();

    //
    // set/getAffectedTable
    //

    acca.setAffectedTable("C");
    if (acca.getAffectedTable().equals("C"))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getAffectedTable() = " + acca.getAffectedTable());

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
    MEMEToolkit.trace("Finished test of AtomicChangeConceptAction ..." +
                      new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
