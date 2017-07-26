/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  ActionHarvesterBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Container for the "Action Harvester" parameters.
 *
 * @author MEME Group
 */

public class ActionHarvesterBean extends ClientBean {

  //
  // Fields
  //

  protected int days_ago = 0;
  protected Date start_date = null;
  protected Date end_date = null;
  protected int concept_id = 0;
  protected String recursive = null;
  protected String row_count = null;
  protected int transaction_id = 0;
  protected String worklist = null;
  protected String authority = null;
  protected String molecular_action = null;
  protected String core_table = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link ActionHarvesterBean}.
   */
  public ActionHarvesterBean() {
    super();
  }

  //
  // Methods
  //

  /**
   * Returns the number of days ago used as a search parameter to identify
   * actions within a certain date range (from "days ago" to "now") (<B>SEARCH PARAM</b>).
   * @return the "days ago" value
   */
  public int getDaysAgo() {
    return days_ago;
  }

  /**
   * Sets a search parameter usedto identify actions within a certain
   * date range (from "days ago" to "now") (<B>SEARCH PARAM</b>).
   * @param days_ago the "days ago" value
   */
  public void setDaysAgo(int days_ago) {
    this.days_ago = days_ago;
  }

  /**
   * Returns the start date (expressed in the standard date format) (<B>SEARCH PARAM</b>).
   * @return the start date (expressed in the standard date format)
   */
  public String getStartDate() {
    return start_date == null ? "" : getDateFormat().format(start_date);
  }

  /**
   * Sets the start date (expressed in the standard date format) (<B>SEARCH PARAM</b>).  If only
   * the date (and not the time) is specified, a time of "00:00:00" is appended
   * to the date representation.
   * @param start_date the start date (expressed in the standard date format)
   * @throws RuntimeException if the date format is invalid
   */
  public void setStartDate(String start_date) {
    try {
      if (start_date.length() > 10)
        this.start_date = getDateFormat().parse(start_date);
      else
        this.start_date = getDateFormat().parse(start_date + " 00:00:00");
    } catch (ParseException pe) {
      RuntimeException re = new RuntimeException("Invalid start date format");
      re.initCause(pe);
      throw re;
    }
  }

  /**
   * Returns the date today (expressed in the standard date format).
   * @return the date today (expressed in the standard date format)
   */
  public String getToday() {
    return getDateFormat().format(new Date()).substring(0,10) +
                                                " 00:00:00";
  }

  /**
   * Returns a date in the standard format representing "today" plus
   * the specified number of days.  Poorly named method.
   * @param days the number of days after today.
   * @return a date in the standard format representing "today" plus
   * the specified number of days
   */
  public String getToday(int days) {
    Date d = new Date();
    d.setTime(d.getTime() - ((24 * 3600000) * days));
    return getDateFormat().format(d).substring(0,10) +
                                                " 00:00:00";
  }

  /**
   * Returns the end date (expressed in the standard format) (<B>SEARCH PARAM</b>).
   * @return the end date (expressed in the standard format)
   */
  public String getEndDate() {
    return end_date == null ? "" : getDateFormat().format(end_date);
  }

  /**
   * Sets the end date (expressed in the standard date format) (<B>SEARCH PARAM</b>). If only
   * the date (and not the time) is specified, a time of "00:00:00" is appended
   * to the date representation.
   * @param end_date end date (expressed in the standard date format)
   */
  public void setEndDate(String end_date) {
    try {
      if (end_date.length() > 10)
        this.end_date = getDateFormat().parse(end_date);
      else
        this.end_date = getDateFormat().parse(end_date + " 00:00:00");
    } catch (ParseException pe) {
      RuntimeException re = new RuntimeException("Invalid start date format");
      re.initCause(pe);
      throw re;
    }
  }

  /**
   * Returns the concept id (<B>SEARCH PARAM</b>).
   * @return the concept id
   */
  public int getConceptId() {
    return concept_id;
  }

  /**
   * Sets the concept id (<B>SEARCH PARAM</b>).
   * @param concept_id the concept id
   */
  public void setConceptId(int concept_id) {
    if (concept_id > 0)
      this.concept_id = concept_id;
  }

  /**
   * Returns the HTML code <code>CHECKED</code> attribute if "recursive" flag.
   * @return the HTML code <code>CHECKED</code> attribute if "recursive" flag
   */
  public String isRecursiveChecked() {
    return recursive == null ? "" : "CHECKED";
  }

  /**
   * Returns the value of the "recursive" flag (<B>SEARCH PARAM</b>).
   * @return the value of the "recursive" flag
   */
  public String getRecursive() {
    return recursive == null ? "" : recursive;
  }

  /**
   * Sets the value of the "recursive" flag (<B>SEARCH PARAM</b>).
   * @param recursive the value of the "recursive" flag
   */
  public void setRecursive(String recursive) {
    if (recursive != null)
      this.recursive = recursive;
  }

  /**
   * Returns the row count (<B>SEARCH PARAM</b>).
   * @return the row count
   */
  public String getRowCount() {
    return row_count == null ? "" : row_count;
  }

  /**
   * Sets the row count (<B>SEARCH PARAM</b>).
   * @param row_count the row count
   */
  public void setRowCount(String row_count) {
    if (row_count != null)
      this.row_count = row_count;
  }

  /**
   * Returns the transaction id (<B>SEARCH PARAM</b>).
   * @return the transaction id
   */
  public int getTransactionId() {
    return transaction_id;
  }

  /**
   * Set the transaction id (<B>SEARCH PARAM</b>).
   * @param transaction_id the transaction id
   */
  public void setTransactionId(int transaction_id) {
    if (transaction_id > 0)
      this.transaction_id = transaction_id;
  }

  /**
   * Returns the worklist (<B>SEARCH PARAM</b>).
   * @return the worklist
   */
  public String getWorklist() {
    return worklist == null ? "" : worklist;
  }

  /**
   * Sets the worklist (<B>SEARCH PARAM</b>).
   * @param worklist the worklist
   */
  public void setWorklist(String worklist) {
    if (worklist != null)
      this.worklist = worklist;
  }

  /**
   * Returns the authority (<B>SEARCH PARAM</b>).
   * @return the authority
   */
  public String getAuthority() {
    return authority == null ? "" : authority;
  }

  /**
   * Sets the authority (<B>SEARCH PARAM</b>).
   * @param authority the authority
   */
  public void setAuthority(String authority) {
    if (authority != null)
      this.authority = authority;
  }

  /**
   * Returns the molecular action (<B>SEARCH PARAM</b>).
   * @return the molecular action
   */
  public String getMolecularAction() {
    return molecular_action == null ? "" : molecular_action;
  }

  /**
   * Sets the molecular action (<B>SEARCH PARAM</b>).
   * @param molecular_action the molecular action
   */
  public void setMolecularAction(String molecular_action) {
    if (molecular_action != null)
      this.molecular_action = molecular_action;
  }

  /**
   * Returns the core table (<B>SEARCH PARAM</b>).
   * @return the core table
   */
  public String getCoreTable() {
    return core_table == null ? "" : core_table;
  }

  /**
   * Sets the core table (<B>SEARCH PARAM</b>).
   * @param core_table the core table
   */
  public void setCoreTable(String core_table) {
    if (core_table != null)
      this.core_table = core_table;
  }

  /**
   * Returns the standard {@link DateFormat}.
   * @return the standard {@link DateFormat}
   */
  public DateFormat getDateFormat() {
    return new SimpleDateFormat("MM/dd/yyyy");
  }


}