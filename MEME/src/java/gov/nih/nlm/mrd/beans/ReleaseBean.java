/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.beans
 * Object:  ReleaseBean
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.beans;

import gov.nih.nlm.meme.beans.ClientBean;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.client.FullMRFilesReleaseClient;
import gov.nih.nlm.mrd.client.ReleaseClient;

import java.text.SimpleDateFormat;

/**
 * {@link ReleaseClient} wrapper for handling "Release Manager" events.
 *
 * @author  MEME Group
 */
public class ReleaseBean extends ClientBean {

  //
  // Fields
  //
  private ReleaseClient rc = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ReleaseBean}.
   * @throws MEMEException if failed to construct this class
   */
  public ReleaseBean() throws MEMEException {
    super();
    rc = new FullMRFilesReleaseClient("apelon");
  }

  //
  // Methods
  //

  /**
   * Returns a fully configured {@link ReleaseClient}.
   * @return a fully configured {@link ReleaseClient}
   */
  public ReleaseClient getReleaseClient() {
    configureClient(rc);
    rc.setMidService(getMidService());
    return rc;
  }

  /**
   * Sets the generator class name.
   * @param class_name the generator class name
   */
  public void setReleaseGenerator(String class_name) throws MEMEException {
    try {
      rc = (ReleaseClient) Class.forName(class_name).newInstance();
    } catch (Exception e) {
      InitializationException ie = new InitializationException(
          "Class could not be loaded.", e);
      ie.setDetail("class_name", class_name);
      throw ie;
    }
  }

  /**
   * Returns the standard {@link SimpleDateFormat}.
   * @return the standard {@link SimpleDateFormat}
   */
  public SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat("MM/dd/yyyy");
  }

  /**
   * Returns the HTML code <code>SELECTED</code>
   * if the specified host matches the client bean host.
   * @param host the  host to check
   * @return the SELECTED code or ""
   */
  public String getHostSelectedFlag(String host) {
    if (host.equals(getHost())) {
      return "SELECTED";
    } else {
      return "";
    }
  }

}