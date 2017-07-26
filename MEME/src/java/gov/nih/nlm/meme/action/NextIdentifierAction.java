/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  NextIdentifierAction
 * Changes
 *   01/19/2006 TTN (1-739BX): no changes
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents an integrity check action.
 *
 * @author MEME Group
 */

public class NextIdentifierAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private String c = null;
  private String mode = null;
  private Identifier id = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link NextIdentifierAction}.
   * Only use by ObjectXMLSerializer.
   */
  private NextIdentifierAction() {}

  /**
   * Instantiates an {@link NextIdentifierAction} with
   * the specified identifier.
   * @param c an object {@link Class}
   */
  private NextIdentifierAction(Class c) {
    this.c = c.getName();
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
   * Returns the next identifier.
   * @return an object {@link Identifier}
   */
  public Identifier getNextIdentifier() {
    return id;
  }

  /**
   * Performs a new set identifier action
   * @param c an object {@link Class}
   * @return an object {@link NextIdentifierAction}
   */
  public static NextIdentifierAction newSetNextIdentifierAction(Class c) {
    NextIdentifierAction ica = new NextIdentifierAction(c);
    ica.setMode("SET");
    return ica;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("SET")) {
        id = mds.getNextIdentifierForType(Class.forName(c));
      }
    } catch (ClassNotFoundException cnfe) {
      DataSourceException dse = new DataSourceException(
          "Class not found.", c, cnfe);
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSourceAction#getInitialState(MEMEDataSource)}.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {}

}
