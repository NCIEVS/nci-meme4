/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  ActionReport
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

import java.util.ArrayList;

/**
 * This class is being used by action report.
 *
 * @author MEME Group
 */

public class ActionReport {

  //
  // Fields
  //

  private Concept source = null;
  private Concept target = null;
  private MolecularAction ma = null;
  private ArrayList atoms = null;
  private ArrayList attrs = null;
  private ArrayList rels = null;
  private ArrayList actions = null;

  //
  // Methods
  //

  /**
   * Returns the source.
   * @return An array of {@link Concept}
   */
  public Concept[] getSource() {
    return new Concept[] {
        source};
  }

  /**
   * Sets the source.
   * @param source an object {@link Concept}
   */
  public void setSource(Concept source) {
    this.source = source;
  }

  /**
   * Returns the target.
   * @return An array of {@link Concept}
   */
  public Concept[] getTarget() {
    return new Concept[] {
        target};
  }

  /**
   * Sets the target.
   * @param target an object {@link Concept}
   */
  public void setTarget(Concept target) {
    this.target = target;
  }

  /**
   * Returns the atoms.
   * @return An array of {@link Atom}
   */
  public Atom[] getAtoms() {
    if (atoms == null)
      return new Atom[0];
    else
      return (Atom[])atoms.toArray(new Atom[] {});
  }

  /**
   * Sets the atom.
   * @param atom an object {@link Atom}
   */
  public void addAtom(Atom atom) {
    if (atoms == null)
      atoms = new ArrayList();
    atoms.add(atom);
  }

  /**
   * Removes the specified atom.
   * @param atom an object {@link Atom}
   */
  public void removeAtom(Atom atom) {
    if (atoms == null)
      return;
    atoms.remove(atom);
  }

  /**
   * Returns the attributes.
   * @return An array of {@link Attribute}
   */
  public Attribute[] getAttribute() {
    if (attrs == null)
      return new Attribute[0];
    else
      return (Attribute[])attrs.toArray(new Attribute[] {});
  }

  /**
   * Sets the attribute.
   * @param attr an object {@link Attribute}
   */
  public void addAttribute(Attribute attr) {
    if (attrs == null)
      attrs = new ArrayList();
    attrs.add(attr);
  }

  /**
   * Removes the specified attribure.
   * @param attr an object {@link Attribute}
   */
  public void removeAttribute(Attribute attr) {
    if (attrs == null)
      return;
    attrs.remove(attr);
  }

  /**
   * Returns the relationships.
   * @return An array of {@link Relationship}
   */
  public Relationship[] getRelationship() {
    if (rels == null)
      return new Relationship[0];
    else
      return (Relationship[])rels.toArray(new Relationship[] {});
  }

  /**
   * Sets the relationship.
   * @param rel an object {@link Relationship}
   */
  public void addRelationship(Relationship rel) {
    if (rels == null)
      rels = new ArrayList();
    rels.add(rel);
  }

  /**
   * Removes the specified relationship.
   * @param rel an object {@link Relationship}
   */
  public void removeRelationship(Relationship rel) {
    if (rels == null)
      return;
    rels.remove(rel);
  }

  /**
   * Returns the molecular action.
   * @return An object of {@link MolecularAction}
   */
  public MolecularAction getMolecularAction() {
    return this.ma;
  }

  /**
   * Sets the molecular action.
   * @param ma an object {@link MolecularAction}
   */
  public void setMolecularAction(MolecularAction ma) {
    this.ma = ma;
  }

  /**
   * Returns the atomic actions.
   * @return An array of {@link AtomicAction}
   */
  public AtomicAction[] getAtomicAction() {
    if (actions == null)
      return new AtomicAction[0];
    else
      return (AtomicAction[])actions.toArray(new AtomicAction[] {});
  }

  /**
   * Sets the atomic action.
   * @param action an object {@link AtomicAction}
   */
  public void addAtomicAction(AtomicAction action) {
    if (actions == null)
      actions = new ArrayList();
    actions.add(action);
  }

  /**
   * Removes the specified action.
   * @param action an object {@link AtomicAction}
   */
  public void removeAtomicAction(AtomicAction action) {
    if (actions == null)
      return;
    actions.remove(action);
  }

}