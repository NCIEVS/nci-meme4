/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  TermgroupAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a termgroup action.
 *
 * @author MEME Group
 */

public class TermgroupAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private Termgroup termgroup = null;
  private Termgroup old_termgroup = null;
  private Termgroup[] termgroups = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link TermgroupAction} with
   * the specified termgroup.
   * @param termgroup an object {@link Termgroup}
   */
  private TermgroupAction(Termgroup termgroup) {
    this.termgroup = termgroup;
  }

  /**
   * Instantiates a {@link TermgroupAction} with
   * the specified termgroups.
   * @param termgroups an array of object {@link Termgroup}
   */
  private TermgroupAction(Termgroup[] termgroups) {
    this.termgroups = termgroups;
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
   * Performs a new add Termgroup action
   * @param termgroup an object {@link Termgroup}
   * @return an object {@link TermgroupAction}
   */
  public static TermgroupAction newAddTermgroupAction(Termgroup termgroup) {
    TermgroupAction ca = new TermgroupAction(termgroup);
    ca.setMode("ADD");
    return ca;
  }

  /**
   * Performs a new add Termgroups action
   * @param termgroups an array of object {@link Termgroup}
   * @return an object {@link TermgroupAction}
   */
  public static TermgroupAction newAddTermgroupsAction(Termgroup[] termgroups) {
    TermgroupAction ca = new TermgroupAction(termgroups);
    ca.setMode("ADDS");
    return ca;
  }

  /**
   * Performs a new set Termgroup action
   * @param termgroup an object {@link Termgroup}
   * @return an object {@link TermgroupAction}
   */
  public static TermgroupAction newSetTermgroupAction(Termgroup termgroup) {
    TermgroupAction ca = new TermgroupAction(termgroup);
    ca.setMode("SET");
    return ca;
  }

  /**
   * Performs a new remove termgroup action
   * @param termgroup an object {@link Termgroup}
   * @return an object {@link TermgroupAction}
   */
  public static TermgroupAction newRemoveTermgroupAction(Termgroup termgroup) {
    TermgroupAction ca = new TermgroupAction(termgroup);
    ca.setMode("REMOVE");
    return ca;
  }

  /**
   * Performs a new remove Termgroups action
   * @param termgroups an array of object {@link Termgroup}
   * @return an object {@link TermgroupAction}
   */
  public static TermgroupAction newRemoveTermgroupsAction(Termgroup[]
      termgroups) {
    TermgroupAction ca = new TermgroupAction(termgroups);
    ca.setMode("REMOVES");
    return ca;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("ADD"))
        mds.addTermgroup(termgroup);
      else if (mode.equals("ADDS"))
        mds.addTermgroups(termgroups);
      else if (mode.equals("REMOVE"))
        mds.removeTermgroup(termgroup);
      else if (mode.equals("SET"))
        mds.setTermgroup(termgroup);
      else if (mode.equals("REMOVES")) {
        for (int i = 0; i < termgroups.length; i++) {
          mds.removeTermgroup(termgroups[i]);
        }
      }
    } catch (BadValueException bve) {
      DataSourceException dse = new DataSourceException(
          "Failed to perform action", this, bve);
      throw dse;
    }
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    TermgroupAction ta = null;
    if (mode.equals("ADD"))
      ta = newRemoveTermgroupAction(termgroup);
    else if (mode.equals("ADDS"))
      ta = newRemoveTermgroupsAction(termgroups);
    else if (mode.equals("REMOVE"))
      ta = newAddTermgroupAction(termgroup);
    else if (mode.equals("REMOVES"))
      ta = newAddTermgroupsAction(termgroups);
    else if (mode.equals("SET"))
      ta = newSetTermgroupAction(old_termgroup);

    ta.setUndoActionOf(this);
    return ta;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("SET"))
        old_termgroup = mds.getTermgroup(termgroup.toString());
      else if (mode.equals("REMOVE"))
        termgroup = mds.getTermgroup(termgroup.toString());
      else if (mode.equals("REMOVES")) {
        for (int i = 0; i < termgroups.length; i++) {
          termgroups[i] = mds.getTermgroup(termgroups[i].toString());
        }
      }
    } catch (BadValueException bve) {
      DataSourceException dse = new DataSourceException(
          "Failed to initialize state.", mds, bve);
      throw dse;
    }

  }

}