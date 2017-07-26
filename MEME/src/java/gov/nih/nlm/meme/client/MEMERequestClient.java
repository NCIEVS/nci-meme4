/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  MEMERequestClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * This is a generic interface for handling requests on the
 * client side.
 *
 * Implementations of this interface will use a specific
 * network protocol (such as HTTP) to connect to the server.
 * Following is an example how to use this interface:
 * <pre>
 *   MEMEServiceRequest request = ...;
 *   MEMERequestClient client = ...;
 *   client.setHost("example.com");
 *   client.setPort(8080);
 *   request = client.processRequest(request);
 * </pre>
 *
 * Implementations of this class should rarely be used directly
 * but instead should be used by subclassing the {@link ClientAPI}.
 *
 * @author MEME Group
 */
public interface MEMERequestClient {

  /**
   * Sets the host.
   * @param host the name of a host running the MEME Application Server
   */
  public void setHost(String host);

  /**
   * Sets the port.
   * @param port the port listened to by the MEME Application Server
   */
  public void setPort(int port);

  /**
   * Add a listener.
   * @param l the {@link ClientProgressListener} to add
   */
  public void addClientProgressListener(ClientProgressListener l);

  /**
   * Remove a listener.
   * @param l the {@link ClientProgressListener} to remove
   */
  public void removeClientProgressListener(ClientProgressListener l);

  /**
   * This method is responsible for sending the request to the server
   * and processing the response.
   * @param request the {@link MEMEServiceRequest}
   * @return the {@link MEMEServiceRequest} response
   * @throws MEMEException if failed to process the client request
   */
  public MEMEServiceRequest processRequest(MEMEServiceRequest request) throws
      MEMEException;

}