/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  ClientBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.client.ClientAPI;
import gov.nih.nlm.meme.client.ClientConstants;
import gov.nih.nlm.meme.client.ClientToolkit;

import java.util.Date;

/**
 * Parent of all other beans, tracks "host", "port", and "mid service" parameters.
 * Supports the "configureClient" method used by subclasses to configure
 * MEME Client API classes with these three parameters.
 *
 * @author MEME Group
 */
public abstract class ClientBean {

  //
  // Fields
  //

  private String mid_service = "";
  private String host = null;
  private String port = null;
  private Date start_date = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ClientBean}.
   */
  public ClientBean() {
    start_date = new Date();
    setHost(ClientToolkit.getProperty(ClientConstants.SERVER_HOST));
    setPort(ClientToolkit.getProperty(ClientConstants.SERVER_PORT));
  }

  //
  // Methods
  //

  /**
   * Sets the mid service.
   * @param mid_service the mid service
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
  }

  /**
   * Returns the mid service.
   * @return the mid service
   */
  public String getMidService() {
    return mid_service;
  }

  /**
   * Sets the host.
   * @param host the host
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Returns the host.
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * Sets the port.
   * @param port the port
   */
  public void setPort(String port) {
    this.port = port;
  }

  /**
   * Returns the port.
   * @return the port
   */
  public String getPort() {
    return port;
  }

  /**
   * Configures client by setting host and port parameters.  Not all clients
   * make use of a "mid service" so those that do must override this method to
   * also set that parameter.
   * @param client the MEME {@link ClientAPI} to configure
   */
  public void configureClient(ClientAPI client) {
    client.getRequestHandler().setHost(getHost());
    client.getRequestHandler().setPort(Integer.valueOf(getPort()).intValue());
  }

  /**
   * Returns the elapsed time.
   * @return the elapsed time
   */
  public double getElapsedTimeInSeconds() {
    return ((new Date()).getTime() - start_date.getTime()) / 1000.0;
  }

  /**
   * Return host, port, and midservice in a URL formatted string
   * @return host, port, and midservice in a URL formatted string
   */
  public String getURLParam() {
    return "&host=" + this.host + "&port=" + this.port + "&midService=" +
        this.mid_service;
  }

}