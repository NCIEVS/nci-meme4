/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  DummyClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * This was an initial test class for connecting to the
 * server.  It accesses the dummy service running on the server.
 * <i>DO NOT USE THIS CLASS.</i>
 *
 * @see gov.nih.nlm.meme.server.HTTPRequestListener
 * @author MEME Group
 */
public class DummyClient extends ClientAPI {

  //
  // Fields
  //
  private String mid_service = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DummyClient}.
   * @throws MEMEException if failed to instantiate this class.
   */
  public DummyClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Creates a dummy client connected to a specified mid service.
   * @param mid_service service name.
   * @throws MEMEException if failed to instantiate this class.
   */
  public DummyClient(String mid_service) throws MEMEException {
    super();
    this.mid_service = mid_service;
  }

  //
  // Dummy API
  //

  /**
   * Sends a request to the server.  Used exclusively for testing
   * and debugging.
   * @throws MEMEException if anything goes wrong
   */
  public void echo() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("DummyService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "echo"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    System.err.println(request.getReturnValue("function").getValue());

  }

}
