/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ConceptMappingClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptMapping;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * This {@link ClientAPI} is used to access and edit {@link ConceptMapping}s.
 */
public class ConceptMappingClient extends ClientAPI {

  //
  // Fields
  //
  private String mid_service = null;
  private Authentication auth = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ConceptMappingClient} connected to the default mid service.
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated.
   */
  public ConceptMappingClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiates a {@link ConceptMappingClient} connected to the
   * specified mid service.
   * @param mid_service a valid MID service
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public ConceptMappingClient(String mid_service) throws MEMEException {
    super();
    this.mid_service = mid_service;
  }

  //
  // Methods
  //

  /**
   * Returns the {@link MEMEServiceRequest}.
   * @return the {@link MEMEServiceRequest}
   */
  protected MEMEServiceRequest getServiceRequest() {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ConceptMappingService");
    request.setMidService(mid_service);
    request.setAuthentication(auth);

    if (getSessionId() == null) {
      request.setNoSession(true);
    } else {
      request.setSessionId(getSessionId());

    }
    return request;
  }

  /**
   * Sets the mid service.
   * @param mid_service the MID service name
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
  }

  /**
   * Returns the mid service.
   * @return the MID service name
   */
  public String getMidService() {
    return mid_service;
  }

  /**
   * Sets the {@link Authentication}.
   * @param auth the {@link Authentication}
   */
  public void setAuthentication(Authentication auth) {
    this.auth = auth;
  }

  //
  // Concept Mapping API
  //

  /**
   * Adds the specified {@link ConceptMapping} (<B>SERVER CALL</B>).
   * @param cm the {@link ConceptMapping} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addConceptMapping(ConceptMapping cm) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "manage_concept_mapping"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", cm));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    Identifier id = (Identifier) request.getReturnValue("id").getValue();
    cm.setIdentifier(id);

  }

  /**
   * Removes the specified {@link ConceptMapping} (<B>SERVER CALL</B>).
   * @param cm the {@link ConceptMapping} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeConceptMapping(ConceptMapping cm) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "manage_concept_mapping"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", cm));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Returns all {@link ConceptMapping}s (<B>SERVER CALL</B>).
   * @return all {@link ConceptMapping}s
   * @throws MEMEException if anything goes wrong
   */
  public ConceptMapping[] getConceptMappings() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_concept_mappings"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return request
    return (ConceptMapping[])
        request.getReturnValue("get_concept_mappings").getValue();

  }

  /**
   * Return all {@link ConceptMapping}s for the specified {@link Concept}
   * (<B>SERVER CALL</b>).
   * @param concept the {@link Concept}
   * @return all {@link ConceptMapping}s for the specified {@link Concept}
   * @throws MEMEException if anything goes wrong
   */
  public ConceptMapping[] getConceptMappings(Concept concept) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
        "get_concept_mappings_by_concept"));
    request.addParameter(new Parameter.Default("concept", concept));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return request
    return (ConceptMapping[])
        request.getReturnValue("get_concept_mappings_by_concept").getValue();

  }

}
