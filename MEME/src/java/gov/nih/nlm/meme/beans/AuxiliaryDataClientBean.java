/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  AuxiliaryDataClientBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.exception.MEMEException;

import java.text.SimpleDateFormat;

/**
 * {@link AuxiliaryDataClient} wrapper for accessing metadata.
 *
 * @author MEME Group
 */

 public class AuxiliaryDataClientBean extends ClientBean {

  //
  // Fields
  //

  private AuxiliaryDataClient ac = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link AuxiliaryDataClientBean}.
   * @throws MEMEException if failed to construct this class.
   */
  public AuxiliaryDataClientBean() throws MEMEException {
    super();
    ac = new AuxiliaryDataClient("");
  }

  //
  // Methods
  //

  /**
   * Returns a fully configured {@link AuxiliaryDataClient}.
   * @return a fully configured {@link AuxiliaryDataClient}
   */
  public AuxiliaryDataClient getAuxiliaryDataClient() {
    configureClient(ac);
    ac.setMidService(getMidService());
    return ac;
  }

  /**
   * Returns the standard {@link SimpleDateFormat}.
   * @return the standard {@link SimpleDateFormat}
   */
  public SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat("MM/dd/yyyy");
  }

  /**
   * Sets the mid service.
   * @param mid_service the mid service
   */
  public void setMidService(String mid_service) {
    super.setMidService(mid_service);
    if(ac != null) {
      try {
        ac = new AuxiliaryDataClient(mid_service);
      } catch(MEMEException me) {
        throw new RuntimeException("Unexcepted Error",me);
      }
    }
  }

  /**
   * Returns the HTML code <code>SELECTED</code> if the specified host name
   * matches the one used by the {@link AuxiliaryDataClient}.
   * @param host the host to check
   * @return the SELECTED code or ""
   */
  public String getHostSelectedFlag(String host) {
    if (host.equals(getHost()))
      return "SELECTED";
    else return "";
  }

}