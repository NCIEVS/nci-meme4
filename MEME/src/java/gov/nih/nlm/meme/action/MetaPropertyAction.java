/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MetaPropertyAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a meta property action.
 *
 * @author MEME Group
 */

public class MetaPropertyAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private MetaProperty meta_prop = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MetaPropertyAction} with
   * the specified meta property.
   * @param meta_prop an object {@link MetaProperty}
   */
  private MetaPropertyAction(MetaProperty meta_prop) {
    this.meta_prop = meta_prop;
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
   * Performs a new add meta property action
   * @param meta_prop an object {@link MetaProperty}
   * @return an object {@link MetaPropertyAction}
   */
  public static MetaPropertyAction newAddMetaPropertyAction(MetaProperty
      meta_prop) {
    MetaPropertyAction mpa = new MetaPropertyAction(meta_prop);
    mpa.setMode("ADD");
    return mpa;
  }

  /**
   * Performs a new remove meta property action
   * @param meta_prop an object {@link MetaProperty}
   * @return an object {@link MetaPropertyAction}
   */
  public static MetaPropertyAction newRemoveMetaPropertyAction(MetaProperty
      meta_prop) {
    MetaPropertyAction mpa = new MetaPropertyAction(meta_prop);
    mpa.setMode("REMOVE");
    return mpa;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      mds.addMetaProperty(meta_prop);
    else if (mode.equals("REMOVE"))
      mds.removeMetaProperty(meta_prop);
  }

  /**
   * Return the inverse action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    MetaPropertyAction mpa = null;
    if (mode.equals("ADD"))
      mpa = newRemoveMetaPropertyAction(meta_prop);
    else if (mode.equals("REMOVE"))
      mpa = newAddMetaPropertyAction(meta_prop);

    mpa.setUndoActionOf(this);
    return mpa;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("REMOVE"))
      meta_prop = mds.getMetaProperty(meta_prop.getKey(),
                                      meta_prop.getKeyQualifier(),
                                      meta_prop.getValue(),
                                      meta_prop.getDescription());//naveen UMLS-60 added description parameter to getMetaProperty method
  }

}