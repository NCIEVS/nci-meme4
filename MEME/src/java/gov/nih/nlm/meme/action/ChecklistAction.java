/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  ChecklistAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.AtomChecklist;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MIDDataSource;

/**
 * Represents a checklist action.
 *
 * @author MEME Group
 */

public class ChecklistAction
    extends LoggedAction.Default
    implements MIDDataSourceAction {

  //
  // Fields
  //

  private String checklist_name = null;
  private AtomChecklist ac = null;
  private ConceptChecklist cc = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link ChecklistAction} with
   * the specified checklist name.
   * @param checklist_name the checklist name
   */
  private ChecklistAction(String checklist_name) {
    this.checklist_name = checklist_name;
  }

  /**
   * Instantiates an {@link ChecklistAction} with
   * the specified atom checklist.
   * @param ac an object {@link AtomChecklist}
   */
  private ChecklistAction(AtomChecklist ac) {
    this.ac = ac;
  }

  /**
   * Instantiates an {@link ChecklistAction} with
   * the specified concept checklist.
   * @param cc an object {@link ConceptChecklist}
   */
  private ChecklistAction(ConceptChecklist cc) {
    this.cc = cc;
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
   * Performs a new add atom checklist action
   * @param ac an object {@link AtomChecklist}
   * @return an object {@link ChecklistAction}
   */
  public static ChecklistAction newAddAtomChecklistAction(AtomChecklist ac) {
    ChecklistAction ca = new ChecklistAction(ac);
    ca.setMode("ADD_ATOM");
    return ca;
  }

  /**
   * Performs a new add concept checklist action
   * @param cc an object {@link ConceptChecklist}
   * @return an object {@link ChecklistAction}
   */
  public static ChecklistAction newAddConceptChecklistAction(ConceptChecklist
      cc) {
    ChecklistAction ca = new ChecklistAction(cc);
    ca.setMode("ADD_CONCEPT");
    return ca;
  }

  /**
   * Performs a new remove checklist action
   * @param checklist_name the checklist name
   * @return an object {@link ChecklistAction}
   */
  public static ChecklistAction newRemoveChecklistAction(String checklist_name) {
    ChecklistAction ca = new ChecklistAction(checklist_name);
    ca.setMode("REMOVE");
    return ca;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("ADD_ATOM"))
      mds.addAtomChecklist(ac);
    else if (mode.equals("ADD_CONCEPT"))
      mds.addConceptChecklist(cc);
    else if (mode.equals("REMOVE"))
      mds.removeChecklist(checklist_name);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    ChecklistAction ca = null;
    if (mode.equals("ADD_ATOM"))
      ca = newRemoveChecklistAction(ac.getName());
    else if (mode.equals("ADD_CONCEPT"))
      ca = newRemoveChecklistAction(cc.getName());
    else if (mode.equals("REMOVE"))
      ca = newAddAtomChecklistAction(ac);

    ca.setUndoActionOf(this);
    return ca;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("REMOVE"))
        ac = mds.getAtomChecklist(checklist_name);
    } catch (BadValueException bve) {
      DataSourceException dse = new DataSourceException(
          "Failed to get checklist.");
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