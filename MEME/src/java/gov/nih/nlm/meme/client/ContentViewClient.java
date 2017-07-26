/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ContentViewClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.ContentView;
import gov.nih.nlm.meme.common.ContentViewMember;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * This client API is used to access content view.
 */
public class ContentViewClient extends ClientAPI {

  //
  // Fields
  //
  private String mid_service = null;
  private String session_id = null;
  private Authentication auth = null;

  //
  // Constructors
  //

  /**
       * Instantiates a {@link ContentViewClient} connected to the default mid service.
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated.
   */
  public ContentViewClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiates a {@link ContentViewClient} connected to the
   * specified mid service.
   * @param mid_service a valid MID service
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public ContentViewClient(String mid_service) throws MEMEException {
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
    request.setService("ContentViewService");
    request.setMidService(mid_service);
    request.setAuthentication(auth);

    if (session_id == null) {
      request.setNoSession(true);
    } else {
      request.setSessionId(session_id);

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

  /**
   * Sets the session id.
   * @param session_id the session id
   */
  public void setSessionId(String session_id) {
    this.session_id = session_id;
  }

  /**
   * Returns the session id.
   * @return the session id
   */
  public String getSessionId() {
    return session_id;
  }

  //
  // Content View API
  //

  /**
   * Adds the specified {@link ContentView} (<B>SERVER CALL</b>).
   * @param cv the {@link ContentView} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addContentView(ContentView cv) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "add_content_view"));
    request.addParameter(new Parameter.Default("content_view", cv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    Identifier id = (Identifier) request.getReturnValue("id").getValue();
    cv.setIdentifier(id);

  }

  /**
   * Removes the specified {@link ContentView} (<B>SERVER CALL</>B).
   * @param cv the {@link ContentView} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeContentView(ContentView cv) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "remove_content_view"));
    request.addParameter(new Parameter.Default("content_view", cv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Applies changes to the specified {@link ContentView} (<B>SERVER CALL</B>).
   * @param cv the {@link ContentView} to change
   * @throws MEMEException if anything goes wrong
   */
  public void setContentView(ContentView cv) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "set_content_view"));
    request.addParameter(new Parameter.Default("content_view", cv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Returns the fully populated {@link ContentView}
   * for the specified id (<B>SERVER CALL</b>).
   * @param id the content view id
   * @return the {@link ContentView} for the specified id
   * @throws MEMEException if anything goes wrong
   */
  public ContentView getContentView(int id) throws MEMEException {
    return getContentView(new Identifier.Default(id));
  }

  /**
   * Returns the fully populated {@link ContentView} for the specified
   * {@link Identifier} (<B>SERVER CALL</b>).
   * @param id the content view {@link Identifier}
   * @return the {@link ContentView} for the specified id
   * @throws MEMEException if anything goes wrong
   */
  public ContentView getContentView(Identifier id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_content_view"));
    request.addParameter(new Parameter.Default("content_view_id", id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return request
    return (ContentView) request.getReturnValue("get_content_view").getValue();

  }

  /**
   * Returns all {@link ContentView}s (<B>SERVER CALL</B>).
   * @return all {@link ContentView}s
   * @throws MEMEException if anything goes wrong
   */
  public ContentView[] getContentViews() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_content_views"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return request
    return (ContentView[])
        request.getReturnValue("get_content_views").getValue();

  }

  /**
   * Removes all members from the specified {@link ContentView} (<B>SERVER CALL</B>).
   * @param cv the {@link ContentView}
   * @throws MEMEException if anything goes wrong
   */
  public void removeContentViewMembers(ContentView cv) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "remove_content_view_members"));
    request.addParameter(new Parameter.Default("content_view", cv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Adds the {@link ContentViewMember} to its {@link ContentView} (<B>SERVER CALL</B>).
   * Requires that the member's content view is set properly.
   * @param member the {@link ContentViewMember}
   * @throws MEMEException if anything goes wrong
   */
  public void addContentViewMember(ContentViewMember member) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_content_view_member"));
    request.addParameter(new Parameter.Default("content_view_member", member));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    Identifier id = (Identifier) request.getReturnValue("id").getValue();
    member.setIdentifier(id);

  }

  /**
   * Removes the {@link ContentViewMember} from its {@link ContentView} (<B>SERVER CALL</B>).
   * Requires that the member's content view is set properly.
   * @param member the {@link ContentViewMember}
   * @throws MEMEException if anything goes wrong
   */
  public void removeContentViewMember(ContentViewMember member) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "remove_content_view_member"));
    request.addParameter(new Parameter.Default("content_view_member", member));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Adds the {@link ContentViewMember}s to their {@link ContentView} (<B>SERVER CALL</B>).
   * Requires that the members' content views are set properly.
   * @param members the {@link ContentViewMember}s
   * @throws MEMEException if anything goes wrong
   */
  public void addContentViewMembers(ContentViewMember[] members) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_content_view_members"));
    request.addParameter(new Parameter.Default("content_view_members", members));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Removes the {@link ContentViewMember}s from their {@link ContentView} (<B>SERVER CALL</B>).
   * Requires that the members' content views are set properly.
   * @param members the {@link ContentViewMember}s
   * @throws MEMEException if anything goes wrong
   */
  public void removeContentViewMembers(ContentViewMember[] members) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "remove_content_view_members"));
    request.addParameter(new Parameter.Default("content_view_members", members));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Generates {@link ContentViewMember}s for the specified {@link ContentView}
   * (<B>SERVER CALL</B>).  Requires that the content view have a query
   * specifying how to generate its members.
   * @param cv the {@link ContentView}
   * @throws MEMEException if anything goes wrong
   */
  public void generateContentViewMembers(ContentView cv) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "generate_content_view_members"));
    request.addParameter(new Parameter.Default("content_view", cv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
       * Returns all {@link ContentViewMember}s for the specified {@link ContentView}
   * (<B>SERVER CALL</B>)
   * @param cv the {@link ContentView}
       * @return all {@link ContentViewMember}s for the specified {@link ContentView}
   * @throws MEMEException if anything goes wrong
   */
  public ContentViewMember[] getContentViewMembers(ContentView cv) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_content_view_members"));
    request.addParameter(new Parameter.Default("content_view", cv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return request
    return (ContentViewMember[])
        request.getReturnValue("get_content_view_members").getValue();

  }

}
