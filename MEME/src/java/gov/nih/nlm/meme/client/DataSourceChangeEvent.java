/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  DataSourceChangeEvent
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

/**
 * Used to indicate that the data source used by an application has changed.
 *
 * @author BAC, RBE
 *
 */

public class DataSourceChangeEvent {

  //
  // Fields
  //

  private String service = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DataSourceChangeEvent} from the specified
   * data source service name.
   * @param service the selected data source service name
   */
  public DataSourceChangeEvent(String service) {
    this.service = service;
  }

  //
  // Methods
  //

  /**
   * Returns the service name.
   * @return the service name
   */
  public String getService() {
    return service;
  }

}