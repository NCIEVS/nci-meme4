/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  WorklistAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.AtomWorklist;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MIDDataSource;

/**
 * Represents a worklist action.
 *
 * @author MEME Group
 */

public class WorklistAction
    extends LoggedAction.Default
    implements MIDDataSourceAction {

  //
  // Fields
  //

  private String worklist_name = null;
  private AtomWorklist aw = null;
  private ConceptWorklist cw = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link WorklistAction} with
   * the specified worklist name.
   * @param worklist_name the worklist name
   */
  private WorklistAction(String worklist_name) {
    this.worklist_name = worklist_name;
  }

  /**
   * Instantiates an {@link WorklistAction} with
   * the specified atom worklist.
   * @param aw an object {@link AtomWorklist}
   */
  private WorklistAction(AtomWorklist aw) {
    this.aw = aw;
  }

  /**
   * Instantiates an {@link WorklistAction} with
   * the specified concept Worklist.
   * @param cw an object {@link ConceptWorklist}
   */
  private WorklistAction(ConceptWorklist cw) {
    this.cw = cw;
  }

  //
  // Methods
  //

  /**
   * Set the action mode.
   * @param mode the action mode
   */
  private void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * Performs a new add atom worklist action
   * @param aw an object {@link AtomWorklist}
   * @return an object {@link WorklistAction}
   */
  public static WorklistAction newAddAtomWorklistAction(AtomWorklist aw) {
    WorklistAction wa = new WorklistAction(aw);
    wa.setMode("ADD_ATOM");
    return wa;
  }

  /**
   * Performs a new add concept worklist action
   * @param cw an object {@link ConceptWorklist}
   * @return an object {@link WorklistAction}
   */
  public static WorklistAction newAddConceptWorklistAction(ConceptWorklist cw) {
    WorklistAction wa = new WorklistAction(cw);
    wa.setMode("ADD_CONCEPT");
    return wa;
  }

  /**
   * Performs a new remove worklist action
   * @param worklist_name the worklist name
   * @return an object {@link WorklistAction}
   */
  public static WorklistAction newRemoveWorklistAction(String worklist_name) {
    WorklistAction wa = new WorklistAction(worklist_name);
    wa.setMode("REMOVE");
    return wa;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("ADD_ATOM"))
      mds.addAtomWorklist(aw);
    else if (mode.equals("ADD_CONCEPT"))
      mds.addConceptWorklist(cw);
    else if (mode.equals("REMOVE"))
      mds.removeWorklist(worklist_name);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    WorklistAction wa = null;
    if (mode.equals("ADD_ATOM"))
      wa = newRemoveWorklistAction(aw.getName());
    else if (mode.equals("ADD_CONCEPT"))
      wa = newRemoveWorklistAction(cw.getName());
    else if (mode.equals("REMOVE"))
      wa = newAddAtomWorklistAction(aw);

    wa.setUndoActionOf(this);
    return wa;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("REMOVE"))
        aw = mds.getAtomWorklist(worklist_name);
    } catch (BadValueException bve) {
      DataSourceException dse = new DataSourceException(
          "Failed to get worklist.");
      throw dse;
    }
  }

  /**
   * Determines whether or not is synchronized.
       * @return <code>true</code> if it is synchronized; <code>false</code> otherwise
   */
  public boolean isSynchronizable() {
    return false;
  }

}