/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  LoggedAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.LoggedError;
import gov.nih.nlm.meme.exception.ActionException;

import java.util.ArrayList;
import java.util.Date;

/**
 * Generically represents an action that has been logged.
 *
 * @author MEME Group
 */

public interface LoggedAction {

  /**
   * Indicates whether or not this action is "approved".
   * @return <code>true</code> if the action is "approved",
   *         <code>false</code> otherwise
   */
  public boolean isApproved();

  /**
   * Returns the status of the action.
   * @return the <code>char</code> status value
   */
  public char getStatus();

  /**
   * Sets the status, used to determine the approval status
   * of the action itself.
   * @param status the <code>char</code> status value
   */
  public void setStatus(char status);

  /**
   * Returns the {@link Authority} responsible for this actcion.
   * @return the {@link Authority}
   */
  public Authority getAuthority();

  /**
   * Sets the {@link Authority} responsible for this action.
   * @param authority the {@link Authority}
   */
  public void setAuthority(Authority authority);

  /**
   * Return the timestamp.
   * @return the {@link Date} timestamp
   */
  public Date getTimestamp();

  /**
   * Sets the timestamp.
   * @param timestamp the {@link Date} timestamp
   */
  public void setTimestamp(Date timestamp);

  /**
   * Returns the action name.
   * @return the action name
   */
  public String getActionName();

  /**
   * Sets the action name.
   * @param action the action name
   */
  public void setActionName(String action);

  /**
   * Returns the elapsed time in milliseconds.
       * @return A <code>long</code> value containing the elapsed time in milliseconds
   */
  public long getElapsedTime();

  /**
   * Sets the elapsed time in milliseconds.
   * @param elapsed_time a <code>long</code> value containing the elapsed time in milliseconds
   */
  public void setElapsedTime(long elapsed_time);

  /**
   * Returns the action {@link Identifier}.
   * @return the action {@link Identifier}
   */
  public Identifier getIdentifier();

  /**
   * Sets the action {@link Identifier}.
   * @param id the action {@link Identifier}
   */
  public void setIdentifier(Identifier id);

  /**
   * Sets the action {@link Identifier} from an <code>int</code> value.
   * @param id An <code>int</code> representation of the id for this action.
   */
  public void setIdentifier(int id);

  /**
   * Returns the sub actions.
   * @return a {@link LoggedAction}<code>[]</code> of sub actions
   */
  public LoggedAction[] getSubActions();

  /**
   * Add a sub action.
   * @param sub_action the {@link LoggedAction} sub action to add
   */
  public void addSubAction(LoggedAction sub_action);

  /**
   * Remove a sub action.
   * @param sub_action the {@link LoggedAction} sub action to remove
   */
  public void removeSubAction(LoggedAction sub_action);

  /**
   * Clear the sub-actions.
   */
  public void clearSubActions();

  /**
   * Returns the inverse action.
   * @return the {@link LoggedAction}
   * @throws ActionException
   */
  public LoggedAction getInverseAction() throws ActionException;

  /**
   * Returns undo action.
   * @return the {@link LoggedAction}
   */
  public LoggedAction getUndoActionOf();

  /**
   * Sets undo action.
   * @param la the undo {@link LoggedAction}
   */
  public void setUndoActionOf(LoggedAction la);

  /**
   * Returns the enclosing {@link LoggedAction}.
   * @return the enclosing {@link LoggedAction}
   */
  public LoggedAction getParent();

  /**
   * Sets the enclosing {@link LoggedAction}.
   * @param parent the enclosing {@link LoggedAction}
   */
  public void setParent(LoggedAction parent);

  /**
   * Determines whether or not is implied.
   * @return <code>true</code> if it is implied; <code>false</code> otherwise
   */
  public boolean isImplied();

  /**
   * Determines whether or not is synchronized.
       * @return <code>true</code> if it is synchronized; <code>false</code> otherwise
   */
  public boolean isSynchronizable();

  /**
   * Sets the value of is implied.
   * @param is_implied <code>true</code> if it is implied; <code>false</code> otherwise
   */
  public void setIsImplied(boolean is_implied);

  /**
   * Returns all errors from the log.
   * @return a list of {@link LoggedError}
   */
  public LoggedError[] getErrors();

  /**
   * Adds an error to log.
   * @param error the {@link LoggedError} to add
   */
  public void addError(LoggedError error);

  /**
   * Sets the log errors.
   * @param error an array of {@link LoggedError}
   */
  public void setErrors(LoggedError[] error);

  /**
   * Removes an error from the log.
   * @param error an object {@link LoggedError} to remove
   */
  public void removeError(LoggedError error);

  /**
   * Clear the error logs.
   */
  public void clearErrors();

  //
  // Inner classes
  //

  /**
   * This class serves as a default abstract implementation of the
   * interface, implementations should extend it.
   */

  public abstract class Default
      implements LoggedAction {

    //
    // Fields
    //

    protected Authority authority = null;
    protected Date timestamp = null;
    protected String action_name = null;
    protected long elapsed_time = 0;
    protected ArrayList sub_actions = null;
    protected Identifier id = null;
    protected char status = 'R';
    protected LoggedAction parent = null;
    protected boolean is_implied = false;
    protected ArrayList errors = null;
    protected LoggedAction undo_la = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link LoggedAction}.
     */
    public Default() {}

    //
    // LoggedAction Implementation
    //

    /**
     * Implements {@link LoggedAction#isApproved()}
     * @return <code>true</code> if the action is "approved",
     *         <code>false</code> otherwise
     */
    public boolean isApproved() {
      return status == CoreData.FV_STATUS_REVIEWED;
    }

    /**
     * Implements {@link LoggedAction#getStatus()}
     * @return the <code>char</code> status value
     */
    public char getStatus() {
      return status;
    }

    /**
     * Implements {@link LoggedAction#getAuthority()}.
     * @param status the <code>char</code> status value
     */
    public void setStatus(char status) {
      this.status = status;
    }

    /**
     * Implements {@link LoggedAction#getAuthority()}.
     * @return the {@link Authority}
     */
    public Authority getAuthority() {
      return authority;
    }

    /**
     * Implements {@link LoggedAction#setAuthority(Authority)}.
     * @param authority the {@link Authority}
     */
    public void setAuthority(Authority authority) {
      this.authority = authority;
    }

    /**
     * Implements {@link LoggedAction#getTimestamp()}.
     * @return the {@link Date} timestamp
     */
    public Date getTimestamp() {
      return timestamp;
    }

    /**
     * Implements {@link LoggedAction#setTimestamp(Date)}.
     * @param timestamp the {@link Date} timestamp
     */
    public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
    }

    /**
     * Implements {@link LoggedAction#getActionName()}.
     * @return the action name
     */
    public String getActionName() {
      return (action_name) == null ? getClass().getName() : action_name;
    }

    /**
     * Implements {@link LoggedAction#setActionName(String)}.
     * @param action the action name
     */
    public void setActionName(String action) {
      this.action_name = action;
    }

    /**
     * Implements {@link LoggedAction#getElapsedTime()}.
         * @return A <code>long</code> value containing the elapsed time in milliseconds
     */
    public long getElapsedTime() {
      return elapsed_time;
    }

    /**
     * Implements {@link LoggedAction#setElapsedTime(long)}.
     * @param elapsed_time a <code>long</code> value containing the elapsed time in milliseconds
     */
    public void setElapsedTime(long elapsed_time) {
      this.elapsed_time = elapsed_time;
    }

    /**
     * Implements {@link LoggedAction#getIdentifier()}.
     * @return the action {@link Identifier}
     */
    public Identifier getIdentifier() {
      return id;
    }

    /**
     * Implements {@link LoggedAction#setIdentifier(Identifier)}.
     * @param id the action {@link Identifier}
     */
    public void setIdentifier(Identifier id) {
      this.id = id;
    }

    /**
     * Implements {@link LoggedAction#setIdentifier(int)}.
     * @param id An <code>int</code> representation of the id for this action.
     */
    public void setIdentifier(int id) {
      this.id = new Identifier.Default(id);
    }

    /**
     * Implements {@link LoggedAction#getSubActions()}.
     * @return a {@link LoggedAction}<code>[]</code> of sub actions
     */
    public LoggedAction[] getSubActions() {
      if (sub_actions == null)
        return new LoggedAction[0];
      return (LoggedAction[])sub_actions.toArray(new LoggedAction[0]);
    }

    /**
     * Implements {@link LoggedAction#addSubAction(LoggedAction)}.
     * @param sub_action the {@link LoggedAction} sub action to add
     */
    public void addSubAction(LoggedAction sub_action) {
      if (sub_actions == null)
        sub_actions = new ArrayList();
      sub_action.setParent(this);
      sub_actions.add(sub_action);
    }

    /**
     * Implements {@link LoggedAction#removeSubAction(LoggedAction)}.
     * @param sub_action the {@link LoggedAction} sub action to remove
     */
    public void removeSubAction(LoggedAction sub_action) {
      if (sub_actions != null)
        sub_actions.remove(sub_action);
    }

    /**
     * Implements {@link LoggedAction#clearSubActions()}.
     */
    public void clearSubActions() {
      if (sub_actions != null)
        sub_actions.clear();
    }

    /**
     * Implements {@link LoggedAction#getUndoActionOf()}.
     * @return the {@link LoggedAction}
     */
    public LoggedAction getUndoActionOf() {
      return undo_la;
    }

    /**
     * Implements {@link LoggedAction#setUndoActionOf(LoggedAction)}.
     * @param la the undo {@link LoggedAction}
     */
    public void setUndoActionOf(LoggedAction la) {
      this.undo_la = la;
    }

    /**
     * Implements {@link LoggedAction#getParent()}.
     * @return the enclosing {@link LoggedAction}
     */
    public LoggedAction getParent() {
      return parent;
    }

    /**
     * Implements {@link LoggedAction#setParent(LoggedAction)}.
     * @param parent the enclosing {@link LoggedAction}
     */
    public void setParent(LoggedAction parent) {
      this.parent = parent;
    }

    /**
     * Implements {@link LoggedAction#isImplied()}.
     * @return <code>true</code> if it is implied; <code>false</code> otherwise
     */
    public boolean isImplied() {
      return is_implied;
    }

    /**
     * Determines whether or not is synchronized.
         * @return <code>true</code> if it is synchronized; <code>false</code> otherwise
     */
    public boolean isSynchronizable() {
      return true;
    }

    /**
     * Implements {@link LoggedAction#setIsImplied(boolean)}.
     * @param is_implied <code>true</code> if it is implied; <code>false</code> otherwise
     */
    public void setIsImplied(boolean is_implied) {
      this.is_implied = is_implied;
    }

    /**
     * Implements {@link LoggedAction#getErrors()}.
     * @return a list of {@link LoggedError}
     */
    public LoggedError[] getErrors() {
      if (this.errors == null)
        return new LoggedError[0];
      return (LoggedError[])errors.toArray(new LoggedError[0]);
    }

    /**
     * Implements {@link LoggedAction#addError(LoggedError)}.
     * @param error the {@link LoggedError} to add
     */
    public void addError(LoggedError error) {
      if (errors == null)
        this.errors = new ArrayList();
      this.errors.add(error);
    }

    /**
     * Implements {@link LoggedAction#setErrors(LoggedError[])}.
     * @param errors an array of {@link LoggedError}
     */
    public void setErrors(LoggedError[] errors) {
      if (errors == null)
        this.errors = new ArrayList();
      for (int i = 0; i < errors.length; i++) {
        this.errors.add(errors[i]);
      }
    }

    /**
     * Implements {@link LoggedAction#removeError(LoggedError)}.
     * @param error an object {@link LoggedError} to remove
     */
    public void removeError(LoggedError error) {
      if (errors != null)
        this.errors.remove(error);
    }

    /**
     * Implements {@link LoggedAction#clearErrors()}.
     */
    public void clearErrors() {
      if (errors != null)
        this.errors.clear();
    }

    /**
     * Returns the inverse action.
     * @return the {@link LoggedAction}
     * @throws ActionException
     */
    public LoggedAction getInverseAction() throws ActionException {
      return new NonOperationAction();
    }

  } // End of inner class
}
