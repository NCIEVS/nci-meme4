/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  SourceAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a source action.
 *
 * @author MEME Group
 */

public class SourceAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private Source source = null;
  private Source old_source = null;
  private Source[] sources = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link SourceAction} with
   * the specified source action.
   * @param source an object {@link Source}
   */
  private SourceAction(Source source) {
    this.source = source;
  }

  /**
   * Instantiates a {@link SourceAction} with
   * the specified source action.
   * @param sources an array of object {@link Source}
   */
  private SourceAction(Source[] sources) {
    this.sources = sources;
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
   * Performs a new add source action
   * @param source an object {@link Source}
   * @return an object {@link SourceAction}
   */
  public static SourceAction newAddSourceAction(Source source) {
    SourceAction ca = new SourceAction(source);
    ca.setMode("ADD");
    return ca;
  }

  /**
   * Performs a new add sources action
   * @param sources an array of object {@link Source}
   * @return an object {@link SourceAction}
   */
  public static SourceAction newAddSourcesAction(Source[] sources) {
    SourceAction ca = new SourceAction(sources);
    ca.setMode("ADDS");
    return ca;
  }

  /**
   * Performs a new set source action
   * @param source an object {@link Source}
   * @return an object {@link SourceAction}
   */
  public static SourceAction newSetSourceAction(Source source) {
    SourceAction ca = new SourceAction(source);
    ca.setMode("SET");
    return ca;
  }

  /**
   * Performs a new remove source action
   * @param source an object {@link Source}
   * @return an object {@link SourceAction}
   */
  public static SourceAction newRemoveSourceAction(Source source) {
    SourceAction ca = new SourceAction(source);
    ca.setMode("REMOVE");
    return ca;
  }

  /**
   * Performs a new remove sources action
   * @param sources an array of object {@link Source}
   * @return an object {@link SourceAction}
   */
  public static SourceAction newRemoveSourcesAction(Source[] sources) {
    SourceAction ca = new SourceAction(sources);
    ca.setMode("REMOVES");
    return ca;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      mds.addSource(source);
    else if (mode.equals("ADDS"))
      mds.addSources(sources);
    else if (mode.equals("SET"))
      mds.setSource(source);
    else if (mode.equals("REMOVE"))
      mds.removeSource(source);
    else if (mode.equals("REMOVES")) {
      for (int i = 0; i < sources.length; i++) {
        mds.removeSource(sources[i]);
      }
    }

  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    SourceAction sa = null;
    if (mode.equals("ADD"))
      sa = newRemoveSourceAction(source);
    else if (mode.equals("ADDS"))
      sa = newRemoveSourcesAction(sources);
    else if (mode.equals("SET"))
      sa = newSetSourceAction(old_source);
    else if (mode.equals("REMOVE"))
      sa = newAddSourceAction(source);
    else if (mode.equals("REMOVES"))
      sa = newAddSourcesAction(sources);

    sa.setUndoActionOf(this);
    return sa;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("SET"))
        old_source = mds.getSource(source.getSourceAbbreviation());
      else if (mode.equals("REMOVE"))
        source = mds.getSource(source.getSourceAbbreviation());
      else if (mode.equals("REMOVES")) {
        for (int i = 0; i < sources.length; i++) {
          sources[i] = mds.getSource(sources[i].getSourceAbbreviation());
        }
      }

    } catch (BadValueException bve) {
      DataSourceException dse = new DataSourceException(
          "Failed to initialize state.", mds, bve);
      throw dse;
    }
  }

}