/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  MRDProcessHandler
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.server;

import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.XMLParseException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Generically represents a handler for MRD events.
 *
 * @author  MRD Group
 */
public interface MRDProcessHandler {

  /**
   * Handles MRD states.
   * @param action an {@link LoggedAction} that may affect MRD states
   * @throws DataSourceException if anything goes wrong
   */
  public void handleStates(LoggedAction action) throws DataSourceException;

  /**
   * Feeds back information to MID.
   * @param action the {@link LoggedAction} that may generate feedback data
   * @throws DataSourceException if anything goes wrong
   */
  public void feedback(LoggedAction action) throws DataSourceException;

  /**
   * Returns the {@link MEMEDataSource}.
   * @param data_source {@link MEMEDataSource}
   */
  public void setDataSource(MEMEDataSource data_source);

  /**
   * Default implementation.
   */
  public abstract class Default implements MRDProcessHandler {

    //
    // Data source
    //
    protected MEMEDataSource data_source = null;

    /**
     * Returns the {@link LoggedAction} represented by the document.
     * @param dtd_version the DTD version
     * @param doc the document
     * @return the {@link LoggedAction} represented by the document
     * @throws BadValueException if failed to handle the event
     * @throws XMLParseException if failed to handle the event
     */
    public LoggedAction parse(String dtd_version, String doc) throws
        XMLParseException, BadValueException {
      return null;
    }

    /**
     * Feeds back information to MID.
     * @param action the {@link LoggedAction} that may generate feedback data
     * @throws DataSourceException if anything goes wrong
     */
    public void feedback(LoggedAction action) throws DataSourceException {}

    /**
     * Sets the {@link MEMEDataSource}.
     * @param data_source the {@link MEMEDataSource}
     */
    public void setDataSource(MEMEDataSource data_source) {
      this.data_source = data_source;
    }

  }
}