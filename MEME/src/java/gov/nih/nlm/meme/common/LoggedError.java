/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  LoggedError
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;

import java.util.Date;

/**
 * Generically represents a logged error.
 *
 * @author MEME Group
 */

public interface LoggedError {

  /**
   * Returns the {@link MolecularTransaction}.
   * @return the {@link MolecularTransaction}
   */
  public MolecularTransaction getTransaction();

  /**
   * Sets the {@link MolecularTransaction}.
   * @param transaction the {@link MolecularTransaction}
   */
  public void setTransaction(MolecularTransaction transaction);

  /**
   * Returns the {@link WorkLog}.
   * @return the {@link WorkLog}
   */
  public WorkLog getWorkLog();

  /**
   * Sets the {@link WorkLog}.
   * @param worklog the {@link WorkLog}
   */
  public void setWorkLog(WorkLog worklog);

  /**
   * Returns the timestamp.
   * @return the timestamp
   */
  public Date getTimestamp();

  /**
   * Sets the timestamp.
   * @param timestamp the timestamp
   */
  public void setTimestamp(Date timestamp);

  /**
   * Returns the elapsed time in milliseconds.
       * @return a <code>long</code> value containing the elapsed time in milliseconds
   */
  public long getElapsedTime();

  /**
   * Sets the elapsed time in milliseconds.
   * @param elapsed_time a <code>long</code> value containing the elapsed time in milliseconds
   */
  public void setElapsedTime(long elapsed_time);

  /**
   * Returns the {@link Authority} responsible for this actcion.
   * @return the {@link Authority} responsible for this actcion
   */
  public Authority getAuthority();

  /**
   * Sets the {@link Authority} responsible for this action.
   * @param authority the {@link Authority} responsible for this actcion
   */
  public void setAuthority(Authority authority);

  /**
   * Returns the activity.
   * @return the activity
   */
  public String getActivity();

  /**
   * Sets the activity.
   * @param activity the activity
   */
  public void setActivity(String activity);

  /**
   * Returns the detail.
   * @return the detail
   */
  public String getDetail();

  /**
   * Sets the detail.
   * @param detail the detail
   */
  public void setDetail(String detail);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link LoggedError} interface.
   */
  public class Default implements LoggedError {

    //
    // Fields
    //

    private MolecularTransaction transaction = null;
    private WorkLog worklog = null;
    private Date timestamp = null;
    private long elapsed_time = 0;
    private Authority authority = null;
    private String activity = null;
    private String detail = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link LoggedError}.
     */
    public Default() {
      super();
    }

    //
    // Implementation of LoggedError interface
    //

    /**
     * Implements {@link LoggedError#getTransaction()}.
     * @return an object {@link MolecularTransaction}
     */
    public MolecularTransaction getTransaction() {
      return transaction;
    }

    /**
     * Implements {@link LoggedError#setTransaction(MolecularTransaction)}.
     * @param transaction an object {@link MolecularTransaction}
     */
    public void setTransaction(MolecularTransaction transaction) {
      this.transaction = transaction;
    }

    /**
     * Implements {@link LoggedError#getWorkLog()}.
     * @return an object {@link WorkLog}
     */
    public WorkLog getWorkLog() {
      return worklog;
    }

    /**
     * Implements {@link LoggedError#setWorkLog(WorkLog)}.
     * @param worklog an object {@link WorkLog}
     */
    public void setWorkLog(WorkLog worklog) {
      this.worklog = worklog;
    }

    /**
     * Implements {@link LoggedError#getTimestamp()}.
     * @return an object {@link Date}
     */
    public Date getTimestamp() {
      return timestamp;
    }

    /**
     * Implements {@link LoggedError#setTimestamp(Date)}.
     * @param timestamp an object {@link Date}
     */
    public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
    }

    /**
     * Implements {@link LoggedError#setElapsedTime(long)}.
     * @param elapsed_time a <code>long</code> value containing the elapsed time in milliseconds
     */
    public void setElapsedTime(long elapsed_time) {
      this.elapsed_time = elapsed_time;
    }

    /**
     * Implements {@link LoggedError#getElapsedTime()}.
         * @return A <code>long</code> value containing the elapsed time in milliseconds
     */
    public long getElapsedTime() {
      return elapsed_time;
    }

    /**
     * Implements {@link LoggedError#getAuthority()}.
     * @return the {@link Authority}
     */
    public Authority getAuthority() {
      return authority;
    }

    /**
     * Implements {@link LoggedError#setAuthority(Authority)}.
     * @param authority the {@link Authority}
     */
    public void setAuthority(Authority authority) {
      this.authority = authority;
    }

    /**
     * Implements {@link LoggedError#getActivity()}.
     * @return the activity
     */
    public String getActivity() {
      return activity;
    }

    /**
     * Implements {@link LoggedError#setActivity(String)}.
     * @param activity the activity
     */
    public void setActivity(String activity) {
      this.activity = activity;
    }

    /**
     * Implements {@link LoggedError#getDetail()}.
     * @return the detail
     */
    public String getDetail() {
      return detail;
    }

    /**
     * Implements {@link LoggedError#setDetail(String)}.
     * @param detail the detail
     */
    public void setDetail(String detail) {
      this.detail = detail;
    }

  }
}