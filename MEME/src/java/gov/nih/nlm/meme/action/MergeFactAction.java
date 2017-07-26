/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MergeFactAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.MergeFact;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MIDDataSource;

/**
 * Represents a merge fact action.
 *
 * @author MEME Group
 */

public class MergeFactAction
    extends LoggedAction.Default
    implements MIDDataSourceAction {

  //
  // Fields
  //

  private MergeFact mf = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MergeFactAction} with
   * the specified merge fact.
   * @param mf an object {@link MergeFact}
   */
  private MergeFactAction(MergeFact mf) {
    this.mf = mf;
    setIsImplied(true);
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
   * Performs a new set merge fact action
   * @param mf an object {@link MergeFact}
   * @return an object {@link MergeFactAction}
   */
  public static MergeFactAction newSetMergeFactAction(MergeFact mf) {
    MergeFactAction mfa = new MergeFactAction(mf);
    mfa.setMode("SET");
    return mfa;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("SET"))
      mds.setMergeFact(mf);
  }

  /**
   * Implements {@link MIDDataSourceAction#getInitialState(MIDDataSource)}.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException {}

  /**
   * Determines whether or not is synchronized.
       * @return <code>true</code> if it is synchronized; <code>false</code> otherwise
   */
  public boolean isSynchronizable() {
    return false;
  }

}