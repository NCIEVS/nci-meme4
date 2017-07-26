/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;

import java.util.Date;

/**
 * Generically represents a molecular action and is
 * typically has a {@link MolecularTransaction} as a parent and
 * one or more {@link AtomicAction}s as children.
 *
 * There is a contract for using a molecular action.  It requires
 * that applications instantiating actions (for use in the <i>MID</i>)
 * must set the work identifier, transaction identifierd, authority,
 * integrity vector, the change status flag, and the source/target concepts.
 *
 * Subclasses may have specific additional requirements.
 *
 * @author MEME Group
 */

public class MolecularAction
    extends LoggedAction.Default {

  //
  // Constants
  //
  public final static String MOLECULAR_UNDO = "UNDO";
  public final static String MOLECULAR_REDO = "REDO";

  //
  // Fields
  //
  // the transaction_id is the parent_id
  // the work_id is the parent_id's parent_id

  // Should these be concept objects ?
  protected Identifier source_id = null, target_id = null;
  protected Identifier transaction_id = null, work_id = null;
  protected boolean undone = false;
  protected boolean interactive = false;
  protected Date undone_when = null;
  protected Authority undone_by = null;
  protected EnforcableIntegrityVector eiv = null;
  protected ViolationsVector vv = null;
  protected boolean change_status = true;
  protected Concept source = null;
  protected Concept target = null;
  protected boolean assign_cuis = true;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MolecularAction} with the specified
   * molecule id.
   * @param molecule_id the <code>int</code> molecule id
   */
  public MolecularAction(int molecule_id) {
    this();
    setIdentifier(new Identifier.Default(molecule_id));
  }

  /**
   * Instantiates an empty {@link MolecularAction}.
   */
  public MolecularAction() {
    super();
  }

  //
  // Overridden LoggedAction Methods
  //

  /**
   * Sets the parent (a {@link MolecularTransaction}) and also
   * sets the transaction {@link Identifier}.
   * @param parent the parent {@link MolecularTransaction}
   */
  public void setParent(LoggedAction parent) {
    super.setParent(parent);
    if (parent != null)
      setTransactionIdentifier(parent.getIdentifier());
  }

  //
  // Additional MolecularAction Methods
  //

  /**
   * Returns a list of concepts to refresh before performing the acction.
   * @return the {@link Concept}<code>[]</code> to refresh
   */
  public Concept[] getConceptsToRefresh() {
    return null;
  }

  /**
   * Indicates whether or not this action represents an undo operation.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isUndo() {
    return MOLECULAR_UNDO.equals(getActionName());
  }

  /**
   * Indicates whether or not this action represents an redo operation.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isRedo() {
    return MOLECULAR_REDO.equals(getActionName());
  }

  /**
   * Indicates whether or not this action has been undone.
   * @return <code>true</code> if so, and <code>false</code> otherwise
   */
  public boolean isUndone() {
    return undone;
  }

  /**
   * Sets the undone flag.
   * @param undone the <code>boolean</code> undone flag
   */
  public void setIsUndone(boolean undone) {
    this.undone = undone;
  }

  /**
   * Gets the {@link Date} this action was undone.
   * @return the {@link Date} this action was undone
   */
  public Date getUndoneTimestamp() {
    return undone_when;
  }

  /**
   * Sets the {@link Date} this action was undone.
   * @param undone_when the {@link Date} this action was undone
   */
  public void setUndoneTimestamp(Date undone_when) {
    this.undone_when = undone_when;
  }

  /**
   * If the action was undone, get the {@link Authority} responsible.
   * @return the {@link Authority}
   */
  public Authority getUndoneAuthority() {
    return undone_by;
  }

  /**
   * Sets the {@link Authority} responsible for undoing the action.
   * @param undone_by the {@link Authority}
   */
  public void setUndoneAuthority(Authority undone_by) {
    this.undone_by = undone_by;
  }

  /**
   * Indicates whether this action is being performed by a human
   * editor or a machine process.
   * @return <code>true</code> if human, and <code>false</code> otherwise
   */
  public boolean isInteractive() {
    return interactive;
  }

  /**
   * Sets the interactive flag.
   * @param interactive the <code>boolean</code> interactive flag
   */
  public void setIsInteractive(boolean interactive) {
    this.interactive = interactive;
  }

  /**
   * Returns the source {@link Identifier}, or the {@link Identifier} of the
   * primary {@link Concept} acted upon.
   * @return the {@link Identifier} of primary {@link Concept}
   */
  public Identifier getSourceIdentifier() {
    return source_id;
  }

  /**
   * Sets the source {@link Identifier}.
   * @param source_id the source {@link Identifier}
   */
  public void setSourceIdentifier(Identifier source_id) {
    this.source_id = source_id;
    if (source == null) {
      source = new Concept.Default();
      source.setIdentifier(source_id);
    }
  }

  /**
   * Sets the source {@link Identifier}.
   * @param source_id the <code>int</code> source identifier value
   */
  public void setSourceIdentifier(int source_id) {
    this.source_id = new Identifier.Default(source_id);
  }

  /**
   * Returns the target {@link Identifier}, or the {@link Identifier} of the
   * secondary {@link Concept} acted upon.
   * @return the {@link Identifier} of secondary {@link Concept}
   */
  public Identifier getTargetIdentifier() {
    return target_id;
  }

  /**
   * Sets the target {@link Identifier}.
   * @param target_id the target {@link Identifier}
   */
  public void setTargetIdentifier(Identifier target_id) {
    this.target_id = target_id;
  }

  /**
   * Sets the target {@link Identifier}.
   * @param target_id the <code>int</code> target identifier value
   */
  public void setTargetIdentifier(int target_id) {
    this.target_id = new Identifier.Default(target_id);
  }

  /**
   * Returns the transaction {@link Identifier}.  This is a
       * convenience method, equivalent to <code>getParent().getIdentifier()</code>.
   * @return the transaction {@link Identifier}
   */
  public Identifier getTransactionIdentifier() {
    return transaction_id;
  }

  /**
   * Sets the transaction {@link Identifier}. Sends it to the parent also.
   * @param transaction_id the transaction {@link Identifier}
   */
  public void setTransactionIdentifier(Identifier transaction_id) {
    this.transaction_id = transaction_id;
    if (getParent() != null) {
      getParent().setIdentifier(transaction_id);
      getParent().clearSubActions();
    }
  }

  /**
   * Sets the transaction {@link Identifier}.
   * @param transaction_id the <code>int</code> transaction identifier value
   */
  public void setTransactionIdentifier(int transaction_id) {
    setTransactionIdentifier(new Identifier.Default(transaction_id));
  }

  /**
   * Gets the work {@link Identifier}.  Although, in the model, the parent
   * of the parent of this action is a {@link WorkLog} which
   * is the object represented by the work id. This method exists
   * for convenience.
   * @return the work {@link Identifier}
   */
  public Identifier getWorkIdentifier() {
    return work_id;
  }

  /**
   * Sets the work {@link Identifier}.
   * @param work_id the work {@link Identifier}
   */
  public void setWorkIdentifier(Identifier work_id) {
    this.work_id = work_id;
    if (getParent() != null && getParent().getParent() != null) {
      getParent().getParent().setIdentifier(work_id);
      getParent().getParent().clearSubActions();
    }
  }

  /**
   * Sets the work {@link Identifier}
   * @param work_id the <code>int</code> work identifier value
   */
  public void setWorkIdentifier(int work_id) {
    setWorkIdentifier(new Identifier.Default(work_id));
  }

  /**
   * Gets the sub {@link AtomicAction} objects.
   * @return the {@link AtomicAction}<code>[]</code>
   */
  public AtomicAction[] getAtomicActions() {
    if (sub_actions == null)
      return new AtomicAction[0];
    else
      return (AtomicAction[])sub_actions.toArray(new AtomicAction[0]);
  }

  /**
   * Sets the {@link EnforcableIntegrityVector}.
   * @param eiv the {@link EnforcableIntegrityVector}
   */
  public void setIntegrityVector(EnforcableIntegrityVector eiv) {
    this.eiv = eiv;
  }

  /**
   * Returns the {@link EnforcableIntegrityVector}.
   * @return the {@link EnforcableIntegrityVector}
   */
  public EnforcableIntegrityVector getIntegrityVector() {
    return eiv;
  }

  /**
   * Returns a {@link ViolationsVector} resulting from any
   * fatal integrity violations.
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector checkFatalIntegrities() {
    return new ViolationsVector();
  }

  /**
   * Sets the {@link ViolationsVector}.
   * @param vv the {@link ViolationsVector}
   */
  public void setViolationsVector(ViolationsVector vv) {
    this.vv = vv;
  }

  /**
   * Returns the {@link ViolationsVector}.
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector getViolationsVector() {
    return vv;
  }

  /**
   * Sets the flag indiciating whether or not this action
   * should cause affected {@link CoreData} elements to
   * become unapproved.
   * @param change_status the "change status" flag
   */
  public void setChangeStatus(boolean change_status) {
    this.change_status = change_status;
  }

  /**
   * Indicates whether or not this action should cause
   * affected {@link CoreData} elements to become unapproved.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean getChangeStatus() {
    return change_status;
  }

  /**
   * Sets the source {@link Concept}.
   * @param source the source {@link Concept}
   */
  public void setSource(Concept source) {
    this.source = source;
    setSourceIdentifier(source.getIdentifier());
  }

  /**
   * Returns the source {@link Concept}.
   * @return the source {@link Concept}
   */
  public Concept getSource() {
    return source;
  }

  /**
   * Sets the target {@link Concept}.
   * @param target the target {@link Concept}
   */
  public void setTarget(Concept target) {
    this.target = target;
    setTargetIdentifier(target.getIdentifier());
  }

  /**
   * Returns the target {@link Concept}.
   * @return the target {@link Concept}
   */
  public Concept getTarget() {
    return target;
  }

  /**
   * Indicates whether or not the action should lock related concept rows.
   * @return true the default value
   */
  public boolean lockRelatedConcepts() {
    return true;
  }

  /**
   * Determine whether or not to assign cuis.
   * @return true the default value
   */
  public boolean getAssignCuis() {
    return assign_cuis;
  }

  /**
   * Sets whether or not to assign cuis.
   * @param value indicates whether or not to assign a cui
   */
  public void setAssignCuis(boolean value) {
    this.assign_cuis = value;
  }

  /**
   * Returns the information of this class
   * @return the information of this class
   */
  public String toString() {
    return getClass().getName() + ": id=" + getIdentifier() + ", sid=" +
        getSourceIdentifier() + ", tid=" + getTargetIdentifier();
  }

  /**
   * Performs action.
   * @throws ActionException if failed to perform action.
   */
  public void performAction() throws ActionException {};

  /**
   * Returns the inverse of this action.
   * @return the inverse of this action
   */
  public LoggedAction getInverseAction() {
    MolecularAction ma = new MolecularAction();
    String action_name = getActionName();
    ma.setActionName("UNDO_" +
                     action_name.substring(action_name.indexOf("_") + 1));
    ma.setAssignCuis(getAssignCuis());
    ma.setChangeStatus(false);
    //ma.setIntegrityVector(null);
    ma.setIsInteractive(false);
    ma.setSource(new Concept.Default(getSourceIdentifier().intValue()));
    if (getTargetIdentifier().intValue() != 0)
      ma.setTarget(new Concept.Default(getTargetIdentifier().intValue()));
      // Add inverse atomic actions in reverse order.
    AtomicAction[] aa = getAtomicActions();
    for (int i = aa.length - 1; i >= 0; i--)
      ma.addSubAction(aa[i].getInverseAction());

    ma.setUndoActionOf(this);

    //
    // Configure inverse action
    //
    ma.setAuthority(getAuthority());
    ma.setTransactionIdentifier(getTransactionIdentifier());
    ma.setWorkIdentifier(getWorkIdentifier());

    //
    // Connect transaction and work log ancestors for action engine logging
    //
    if (ma.getTransactionIdentifier() != null) {
      ma.setParent(new MolecularTransaction(
          ma.getTransactionIdentifier().intValue()));
      if (ma.getWorkIdentifier() != null)
        ma.getParent().setParent(
            new WorkLog(ma.getWorkIdentifier().intValue()));
    }

    return ma;
  }

}
