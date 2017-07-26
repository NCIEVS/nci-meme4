/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  AtomicInsertAction
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
 * Represents an action to insert some {@link CoreData} element.
 *
 * @author MEME Group
 */

public class AtomicInsertAction
    extends AtomicAction {

  //
  // Fields
  //
  private CoreData insert_element = null;
  private String document = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicInsertAction}.
   */
  public AtomicInsertAction() {
    super();
    setActionName("I");
    setNewValue("N");
    setOldValue("Y");
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified atomic action identifier.
   * @param atomic_action_id the unique identifier for this action
   */
  public AtomicInsertAction(int atomic_action_id) {
    this();
    setIdentifier(new Identifier.Default(atomic_action_id));
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified atom.
   * @param atom the {@link Atom} to insert
   */
  public AtomicInsertAction(Atom atom) {
    this();
    setAffectedTable("C");
    setElementToInsert(atom);
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to insert
   */
  public AtomicInsertAction(Relationship relationship) {
    this();
    setAffectedTable("R");
    setElementToInsert(relationship);
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to insert
   */
  public AtomicInsertAction(Attribute attribute) {
    this();
    setAffectedTable("A");
    setElementToInsert(attribute);
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified concept.
   * @param concept the {@link Concept} to insert
   */
  public AtomicInsertAction(Concept concept) {
    this();
    setAffectedTable("CS");
    setElementToInsert(concept);
  }

  //
  // Additional AtomicInsertAction Methods
  //

  /**
   * Gets the core data element inserted by this action.
   * @return the {@link CoreData} inserted
   */
  public CoreData getElementToInsert() {
    return insert_element;
  }

  /**
   * Sets the core data element inserted by this action.
   * @param element the {@link CoreData} to insert
   */
  public void setElementToInsert(CoreData element) {
    this.insert_element = element;
    setRowIdentifier(element.getIdentifier());
  }

  /**
   * Returns the document representing the core data element
   * being inserted.
   * @return An XML document fragment
   */
  public String getDocument() {
    return document;
  }

  /**
   * Sets the document representing the core data element
   * being inserted.  This document is an XML document fragment.
   * @param doc An XML document fragment
   */
  public void setDocument(String doc) {
    this.document = doc;
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
    MEMEToolkit.trace("Starting test of AtomicInsertAction ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing AtomicInsertAction-";
    String test_result = null;

    // Create an Atom object to work with
    AtomicInsertAction aia = new AtomicInsertAction();

    //
    // set/getElementToInsert
    //

    CoreData cd = new CoreData.Default();
    Identifier identifier = new Identifier.Default("AtomicInsertAction");
    cd.setIdentifier(identifier);
    aia.setElementToInsert(cd);
    if (aia.getElementToInsert().getIdentifier().equals(identifier))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getElementToInsert() = " +
                      aia.getElementToInsert().getIdentifier());

    //
    // set/getAffectedTable
    //

    aia.setAffectedTable("C");
    if (aia.getAffectedTable().equals("C"))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getAffectedTable() = " + aia.getAffectedTable());

    //
    // set/getDocument
    //

    aia.setDocument("DOCUMENT!");
    if (aia.getDocument().equals("DOCUMENT!"))
      test_result = " PASSED: ";
    else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/getDocument() = " + aia.getDocument());

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
    MEMEToolkit.trace("Finished test of AtomicInsertAction ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
