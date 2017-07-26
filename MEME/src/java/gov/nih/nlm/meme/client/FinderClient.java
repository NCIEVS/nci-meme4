/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  FinderClient
 * Version Information
 * 03/06/2007 3.12.4 SL (1-DNO15) : Adding a new function find_concepts_by_ndc to retrieve the NDC Concepts
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.Date;

/**
 * This client API is used to find data in the MID.
 * See {@link ClientAPI} for information
 * on configuring properties required by this class.
 *
 * With the properties properly configured, using the finder
 * is as simple as instantiating the class and calling
 * its various methods.
 *
 * This class is used to perform various kinds of string
 * and word searches, all of which return arrays of
 * {@link Concept} objects as results.  The maximum
 * result count can be configured, and various filters
 * can be added to restrict the results to one or more
 * semantic types (including options for chem/non-chem)
 * and/or restrict the results to one or more sources.
 *
 * Following is an example of using the client.
 *
 * <pre>
 *   // Instantiate client against default
 *   // mid service ("editing-db")
 *   FinderClient client = new FinderClient();
 *
 *   // Set the maximum result count
 *   // Return at most 100 results
 *   client.setMaxResultCount(100);
 *
 *   // Perform case-insensitive string lookup
 *   Concept[] results = client.findExactStringMatches("....");
 *
 *   // Perform normalized string lookup
 *   Concept[] results = client.findNormalizedStringMatches(".....");
 *
 *   // Perform and AND-based multiple word search
 *   Concept[] results = client.findAllWordMatches(
 *                          new String[] {"...","..."});
 *
 *   // Perform an AND-based multiple normalized word search
 *   Concept[] results = client.findAllNormalizedWordMatches(
 *                          new String[] {"...","..."});
 *
 *   // Perform an OR-based multiple word search
 *   Concept[] results = client.findAnyWordMatches(
 *                          new String[] {"...","..."});
 *
 *   // Perform an OR-based multiple normalized word search
 *   Concept[] results = client.findAnyNormalizedWordMatches(
 *                          new String[] {"...","..."});
 *
 *   // Search again
 *   client.findExactStringMatches("other");
 *
 *   // Set restrictions - restrict by concept_id
 *   Concept concept = .... 2342 ...
 *   client.restrictByConcept(concept);
 *
 *   // Set restrictions - restrict by worklist
 *   Worklist worklist = ... meow. ...
 *   worklist.setName("worklist_name");
 *   client.restrictByWorklist(worklist);
 *
 *   // Set restrictions - restrict by core data type
 *   client.restrictByCoreDataType(Atom.class);
 *
 *   // Set restrictions - restrict by STY
 *   SemanticType sty = .... Mammal ...
 *   client.restrictBySemanticType(sty);
 *
 *   // Set restrictions - restrict by Source
 *   Source source = .... MTH ...
 *   client.restrictBySource(source);
 *
 *   // Set restrictions - restrict by Molecular Transaction
 *   MolecularTransaction mt = .... 12345 ...
 *   client.restrictByTransaction(mt);
 *
 *   // Set restrictions - restrict by action type
 *   MolecularAction ma = .... 12345 ...
 *   ma.setActionName("MOLECULAR_INSERT");
 *   client.restrictByActionType(ma);
 *
 *   // Set restrictions - restrict by date range
 *   Date start_range = .... date ...
 *   Date end_range = .... date ...
 *   client.restrictByDateRange(start_range, end_range);
 *
 *   // Clear restrictions
 *   client.clearRestrictions();
 *
 *   // Set recursive
 *   client.setRecursive(recursive);
 *
 *  // Set authority
 *   client.setAuthority(authority);
 *
 * </pre>
 *
 * @author MEME Group
 */
public class FinderClient extends ClientAPI {

  //
  // Fields
  //
  private String mid_service = null;
  private String session_id = null;
  private Authentication auth = null;
  private int max_result_count = 100;

  // Used by find strings
  private SemanticType[] stys = null;
  private Source[] sources = null;
  private boolean releasable = false;
  private boolean chemical = false;
  private boolean non_chemical = false;

  // Used by find molecular actions
  private int concept_id = 0;
  private String worklist = null;
  private String core_table = null;
  private int transaction_id = 0;
  private String molecular_action = null;
  private Date start_date = null;
  private Date end_date = null;
  private boolean recursive = false;
  private Authority authority = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link FinderClient} connected to the default mid service.
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public FinderClient() throws MEMEException {
    this("editing-db");
  }

  /**
       * Instantiates a {@link FinderClient} connected to the specified mid service.
   * @param service a valid MID service
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public FinderClient(String service) throws MEMEException {
    super();
    this.mid_service = service;
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
    request.setService("FinderService");
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
   * Sets the {@link Authentication}
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

  /**
   * Sets the maximum result count.
   * @param max_result_count the maximum number of results
   *                         that a search should return
   */
  public void setMaxResultCount(int max_result_count) {
    this.max_result_count = max_result_count;
  }

  /**
   * Returns the maximum result count.
   * @return the maximum number of results that a search should return
   */
  public int getMaxResultCount() {
    return max_result_count;
  }

  /**
   * Sets the "recursive" flag.
   * @param recursive a flag indicating whether or not molecular action
   *        searches should be recursive
   */
  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  /**
   * Returns the "recursive" flag.
   * @return <code>true</code> if molecular action searches should be recursive
   *         <code>false</code> otherwise.
   */
  public boolean getRecursive() {
    return recursive;
  }

  /**
   * Sets the {@link Authority}.
   * @param authority the {@link Authority}
   */
  public void setAuthority(Authority authority) {
    this.authority = authority;
  }

  /**
   * Returns the {@link Authority}.
   * @return the {@link Authority}
   */
  public Authority getAuthority() {
    return authority;
  }

  /**
   * Sets a semantic type to restrict by.
   * @param sty the {@link SemanticType} to restrict by
   */
  public void restrictBySemanticType(SemanticType sty) {
    stys = new SemanticType[] {
        sty};
  }

  /**
   * Sets a list of semantic types to restrict by.
   * @param stys the {@link SemanticType}<code>[]</code> to restrict by
   */
  public void restrictBySemanticTypes(SemanticType[] stys) {
    this.stys = stys;
  }

  /**
   * Sets a source to restrict by.
   * @param source the {@link Source} to restrict by
   */
  public void restrictBySource(Source source) {
    sources = new Source[] {
        source};
  }

  /**
   * Sets the sources to restrict by.
   * @param sources the {@link Source}<code>[]</code> to restrict by
   */
  public void restrictBySources(Source[] sources) {
    this.sources = sources;
  }

  /**
   * Indicates that concept searches should be all releasable.
   */
  public void restrictByReleasable() {
    this.releasable = true;
  }

  /**
   * Indicates that concept searches should be restricted
   * to concepts containing chemical semantic types.
   */
  public void restrictByChemicalSemanticType() {
    stys = null;
    chemical = true;
    non_chemical = false;
  }

  /**
   * Indicates that concept searches should be restricted
   * to concepts containing non chemical semantic types.
   */
  public void restrictByNonChemicalSemanticType() {
    stys = null;
    chemical = false;
    non_chemical = true;
  }

  /**
   * Indicates that searches should be restricted to
   * the specified {@link Concept}.
   * @param concept the {@link Concept} to restrict by
   */
  public void restrictByConcept(Concept concept) {
    concept_id = concept.getIdentifier().intValue();
  }

  /**
   * Indicates that concept searches should be restricted
   * to {@link Concept}s on the specified {@link Worklist}.
   * @param worklist An object {@link Worklist}.
   */
  public void restrictByWorklist(Worklist worklist) {
    this.worklist = worklist.getName();
  }

  /**
   * Indicates that molecular action searches should be restricted
   * to those affecting the specified data type.
   * @param c a {@link Class} representing a core data type
   */
  public void restrictByCoreDataType(Class c) {
    if (c == Atom.class) {
      core_table = "C";
    } else if (c == Relationship.class) {
      core_table = "R";
    } else if (c == Attribute.class) {
      core_table = "A";
    } else if (c == Concept.class) {
      core_table = "CS";
    } else if (c == ContextRelationship.class) {
      core_table = "CR";
    }
  }

  /**
   * Indicates that molecular action searches should be restricted
   * to those in the specified {@link MolecularTransaction}.  In other
   * words, those actions whose transaction id matches the identifier
   * in the specified transaction.
   * @param transaction the {@link MolecularTransaction}
   */
  public void restrictByTransaction(MolecularTransaction transaction) {
    transaction_id = transaction.getIdentifier().intValue();
  }

  /**
   * Indicates that molecular action searches should be restricted
   * to those with an action name matching that of the specified
   * {@link MolecularAction}
   * @param molecular_action the {@link MolecularAction}
   */
  public void restrictByActionType(MolecularAction molecular_action) {
    this.molecular_action = molecular_action.getActionName();
  }

  /**
   * Indicates that molecular action searches should be restricted
   * to those performed within the specified range of {@link Date}s.
   * @param start_date the start {@link Date}
   * @param end_date the end {@link Date}
   */
  public void restrictByDateRange(Date start_date, Date end_date) {
    this.start_date = start_date;
    this.end_date = end_date;
  }

  /**
   * Clear all restrictions.
   */
  public void clearRestrictions() {
    stys = null;
    sources = null;
    releasable = false;
    chemical = false;
    non_chemical = false;
    concept_id = 0;
    worklist = null;
    core_table = null;
    transaction_id = 0;
    molecular_action = null;
    start_date = null;
    end_date = null;
    max_result_count = -1;
  }

  /**
   * Finds {@link MolecularAction}s (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @return all {@link MolecularAction}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public MolecularAction[] findMolecularActions() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "find_molecular_actions"));
    request.addParameter(new Parameter.Default("concept_id", concept_id));
    request.addParameter(new Parameter.Default("worklist", worklist));
    request.addParameter(new Parameter.Default("core_table", core_table));
    request.addParameter(new Parameter.Default("transaction_id", transaction_id));
    request.addParameter(new Parameter.Default("molecular_action",
                                               molecular_action));
    request.addParameter(new Parameter.Default("start_date", start_date));
    request.addParameter(new Parameter.Default("end_date", end_date));
    request.addParameter(new Parameter.Default("recursive", recursive));
    request.addParameter(new Parameter.Default("authority", authority));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (MolecularAction[])
        request.getReturnValue("find_molecular_actions").getValue();
  }

  /**
   * Finds {@link Concept}s with at least one{@link Atom} having a matching
   * code (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @param code the {@link Code}
   * @return all {@link Concept}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public Concept[] findConceptsByCode(Code code) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "find_concepts_by_code"));
    request.addParameter(new Parameter.Default("code", code));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));
    request.addParameter(new Parameter.Default("semantic_types", stys));
    request.addParameter(new Parameter.Default("sources", sources));
    request.addParameter(new Parameter.Default("releasable",
                                               new Boolean(releasable)));
    request.addParameter(new Parameter.Default("chemical",
                                               new Boolean(chemical)));
    request.addParameter(new Parameter.Default("non_chemical",
                                               new Boolean(non_chemical)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (Concept[]) request.getReturnValue("concepts_by_code").getValue();
  }

  
  /**
   * Finds {@link Concept}s with at least one{@link Atom} having a matching
   * code (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @param code the {@link Code}
   * @return all {@link Concept}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public Concept[] findConceptsByNDC(Code code) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "find_concepts_by_ndc"));
    request.addParameter(new Parameter.Default("code", code));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));
    request.addParameter(new Parameter.Default("semantic_types", stys));
    request.addParameter(new Parameter.Default("sources", sources));
    request.addParameter(new Parameter.Default("releasable",
                                               new Boolean(releasable)));
    request.addParameter(new Parameter.Default("chemical",
                                               new Boolean(chemical)));
    request.addParameter(new Parameter.Default("non_chemical",
                                               new Boolean(non_chemical)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (Concept[]) request.getReturnValue("concepts_by_ndc").getValue();
  }

  
  
  
  /**
   * Finds {@link Concept}s with at least one{@link Atom} having a matching
   * string (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @param string the search string
   * @return all {@link Concept}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public Concept[] findExactStringMatches(String string) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "exact_string"));
    request.addParameter(new Parameter.Default("string", string));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));
    request.addParameter(new Parameter.Default("semantic_types", stys));
    request.addParameter(new Parameter.Default("sources", sources));
    request.addParameter(new Parameter.Default("releasable",
                                               new Boolean(releasable)));
    request.addParameter(new Parameter.Default("chemical",
                                               new Boolean(chemical)));
    request.addParameter(new Parameter.Default("non_chemical",
                                               new Boolean(non_chemical)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (Concept[]) request.getReturnValue("exact_string").getValue();

  }
  
  /**
   * Finds {@link Concept}s with at least one{@link Atom} having a matching
   * normalized string (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @param string the search string
   * @return all {@link Concept}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public Concept[] findNormalizedStringMatches(String string) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "norm_string"));
    request.addParameter(new Parameter.Default("string", string));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));
    request.addParameter(new Parameter.Default("semantic_types", stys));
    request.addParameter(new Parameter.Default("sources", sources));
    request.addParameter(new Parameter.Default("releasable",
                                               new Boolean(releasable)));
    request.addParameter(new Parameter.Default("chemical",
                                               new Boolean(chemical)));
    request.addParameter(new Parameter.Default("non_chemical",
                                               new Boolean(non_chemical)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (Concept[]) request.getReturnValue("norm_string").getValue();

  }

  /**
   * Finds {@link Concept}s with at least one{@link Atom} having a string
   * matching all of the specified words (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @param words the search words
   * @return all {@link Concept}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public Concept[] findAllWordMatches(String[] words) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "all_word"));
    request.addParameter(new Parameter.Default("words", words));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));
    request.addParameter(new Parameter.Default("semantic_types", stys));
    request.addParameter(new Parameter.Default("sources", sources));
    request.addParameter(new Parameter.Default("releasable",
                                               new Boolean(releasable)));
    request.addParameter(new Parameter.Default("chemical",
                                               new Boolean(chemical)));
    request.addParameter(new Parameter.Default("non_chemical",
                                               new Boolean(non_chemical)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (Concept[]) request.getReturnValue("all_word").getValue();

  }

  /**
   * Finds {@link Concept}s with at least one{@link Atom} having a string
   * matching all of the specified normalized words (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @param norm_words the search words
   * @return all {@link Concept}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public Concept[] findAllNormalizedWordMatches(String[] norm_words) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "all_norm_word"));
    request.addParameter(new Parameter.Default("norm_words", norm_words));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));
    request.addParameter(new Parameter.Default("semantic_types", stys));
    request.addParameter(new Parameter.Default("sources", sources));
    request.addParameter(new Parameter.Default("releasable",
                                               new Boolean(releasable)));
    request.addParameter(new Parameter.Default("chemical",
                                               new Boolean(chemical)));
    request.addParameter(new Parameter.Default("non_chemical",
                                               new Boolean(non_chemical)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (Concept[]) request.getReturnValue("all_norm_word").getValue();

  }

  /**
   * Finds {@link Concept}s with at least one{@link Atom} having a string
   * matching any of the specified words (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @param words the search words
   * @return all {@link Concept}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public Concept[] findAnyWordMatches(String[] words) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "any_word"));
    request.addParameter(new Parameter.Default("words", words));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));
    request.addParameter(new Parameter.Default("semantic_types", stys));
    request.addParameter(new Parameter.Default("sources", sources));
    request.addParameter(new Parameter.Default("releasable",
                                               new Boolean(releasable)));
    request.addParameter(new Parameter.Default("chemical",
                                               new Boolean(chemical)));
    request.addParameter(new Parameter.Default("non_chemical",
                                               new Boolean(non_chemical)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (Concept[]) request.getReturnValue("any_word").getValue();

  }

  /**
   * Finds {@link Concept}s with at least one{@link Atom} having a string
   * matching any of the specified normalized words (<B>SERVER CALL</B>).
   * Restricts search based on the way this instance has been configured.
   * @param norm_words the search words
   * @return all {@link Concept}s matching the criteria
   * @throws MEMEException if anything goes wrong
   */
  public Concept[] findAnyNormalizedWordMatches(String[] norm_words) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "any_norm_word"));
    request.addParameter(new Parameter.Default("norm_words", norm_words));
    request.addParameter(new Parameter.Default("max_result_count",
                                               max_result_count));
    request.addParameter(new Parameter.Default("semantic_types", stys));
    request.addParameter(new Parameter.Default("sources", sources));
    request.addParameter(new Parameter.Default("releasable",
                                               new Boolean(releasable)));
    request.addParameter(new Parameter.Default("chemical",
                                               new Boolean(chemical)));
    request.addParameter(new Parameter.Default("non_chemical",
                                               new Boolean(non_chemical)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (Concept[]) request.getReturnValue("any_norm_word").getValue();

  }

}
