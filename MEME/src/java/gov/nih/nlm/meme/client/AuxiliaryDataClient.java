/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  AuxiliaryDataClient
 * 
 * 02/08/2006 RBE (1-763IU): method name changed from removeCheckToOverrideVector()
 * 													 to removeCheckFromOverrideVector() 
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.MetaCode;
import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This client API is used to access auxialiary data from the MID.
 * See {@link ClientAPI} for information
 * on configuring properties required by this class.
 *
 * With the properties properly configured, accessing auxiliary data
 * services is as simple as instantiating class and
 * calling its methods.  For example,
 *
 * <pre>
 *   // Instantiate client
 *   // connected to default data source ("editing-db")
 *   AuxiliaryDataClient client = new AuxiliaryDataClient();
 *
 *   // Get sources
 *   Source[] sources = client.getSources();
 *
 *   // Get termgroups
 *   Termgroup[] termgroups = client.getTermgroups();
 *
 *   // Get relationship names
 *   String[] rel_names = client.getValideRelationshipNames();
 *
 *   ... etc ...
 * </pre>
 *
 * This class caches most of the data that it obtains so that round trips
 * to the server are minimized.  If a client application knows that
 * data on the server is more recent than data in the client cache,
 * the {@link #refreshCache()} method can be called.  Additinally, if
 * the data source is changed by {@link #setMidService(String)} the
 * cache is emptied and all data is re-cached according to the new
 * data source.
 *
 * @author MEME Group
 */
public class AuxiliaryDataClient extends ClientAPI {

  //
  // Fields
  //
  private String mid_service = null;
  private String session_id = null;
  private Thread refresh_thread = null;
  private Authentication auth = null;

  private Source[] sources = null;
  private Termgroup[] termgroups = null;
  private Language[] languages = null;
  private SemanticType[] valid_stys = null;
  private EditorPreferences[] editor_preferences = null;
  private char[] atoms_status = null;
  private char[] attributes_status = null;
  private char[] concepts_status = null;
  private char[] relationships_status = null;
  private char[] attributes_level = null;
  private char[] relationships_level = null;
  private char[] released = null;
  private char[] tobereleased = null;
  private Map inverse_rels = null;
  private Map inverse_relas = null;
  private HashMap vectors = new HashMap();

  //
  // Constructors
  //

  /**
   * Instantiates an {@link AuxiliaryDataClient} connected to the
   * default MID service.
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated
   */
  public AuxiliaryDataClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiates an {@link AuxiliaryDataClient} connected to the specified
   * MID service.
   * @param service A service name.
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated.
   */
  public AuxiliaryDataClient(String service) throws MEMEException {
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
    request.setService("AuxiliaryDataService");
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
   * @param mid_service a valid MID service name
   */
  public void setMidService(String mid_service) {
    if (!mid_service.equals(this.mid_service)) {
      this.mid_service = mid_service;
      clearCache();
    }
  }

  /**
   * Returns the mid service.
   * @return a valid MID service name
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

  /**
   * Clears the cache.
   */
  public void clearCache() {
    sources = null;
    termgroups = null;
    languages = null;
    valid_stys = null;
    editor_preferences = null;
    atoms_status = null;
    attributes_status = null;
    concepts_status = null;
    relationships_status = null;
    attributes_level = null;
    relationships_level = null;
    released = null;
    tobereleased = null;
    inverse_rels = null;
    inverse_relas = null;
    vectors = new HashMap();
  }

  /**
   * Clears the cache and reloads all data (<B>SERVER CALLs</b>).
   */
  public void refreshCache() {
    if (refresh_thread != null) {
      refresh_thread.interrupt();

    }
    refresh_thread = new Thread(
        new Runnable() {
      public void run() {
        try {
          getSources();
          getTermgroups();
          getLanguages();
          getValidStatusValuesForAtoms();
          getValidStatusValuesForAttributes();
          getValidStatusValuesForConcepts();
          getValidStatusValuesForRelationships();
          getValidLevelValuesForAttributes();
          getValidLevelValuesForRelationships();
          getValidReleasedValues();
          getValidTobereleasedValues();
          getEditorPreferences();
          getInverseRelationshipNameMap();
          getInverseRelationshipAttributeMap();
          getValidSemanticTypes();
        } catch (Exception e) {
          // Do nothing
        }
        refresh_thread = null;
      }
    }
    );
    refresh_thread.start();
  }

  //
  // Accessing the data
  //

  /**
   * Returns all {@link Source}s (<B>SERVER CALL</b>).
   * Uses data from cache if available.
   * @return all {@link Source}s
   * @throws MEMEException if anything goes wrong
   */
  public Source[] getSources() throws MEMEException {
    if (sources != null) {
      return sources;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_sources"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    sources = (Source[]) request.getReturnValue("sources").getValue();
    return sources;
  }

  /**
   * Returns all source abbreviations (<B>SERVER CALL</b>).
   * Uses data from cache if available.
   * @return all source abbreviations
   * @throws MEMEException if anything goes wrong
   */
  public String[] getSourceAbbreviations() throws MEMEException {
    if (sources == null) {
      getSources();

    }
    String[] sabs = new String[sources.length];
    for (int i = 0; i < sources.length; i++) {
      sabs[i] = sources[i].getSourceAbbreviation();
    }
    return sabs;
  }

  /**
   * Returns the {@link Source} for the specified abbreviation (<B>SERVER CALL</b>).
   * Uses data from cache if available.
   * @param sab the source abbreviation
   * @return the {@link Source} corresponding to the specified abbreviation
   * @throws MEMEException if anything goes wrong
   */
  public Source getSource(String sab) throws MEMEException {
    if (sources == null) {
      getSources();

    }
    for (int i = 0; i < sources.length; i++) {
      if (sources[i].getSourceAbbreviation().equals(sab)) {
        return sources[i];
      }
    }
    return null;
  }

  /**
   * Returns all source abbreviations for current sources (<B>SERVER CALL</b>).
   * Uses data from cache if available.
   * @return all source abbreviations for current sources
   * @throws MEMEException if anything goes wrong
   */
  public Source[] getCurrentSources() throws MEMEException {
    if (sources == null) {
      getSources();

    }
    ArrayList current_sources = new ArrayList();
    for (int i = 0; i < sources.length; i++) {
      if (sources[i].isCurrent()) {
        current_sources.add(sources[i]);
      }
    }
    return (Source[]) current_sources.toArray(new Source[] {});
  }

  /**
   * Returns the current {@link Source} associated with
   * the specified root source abbreviation (<B>SERVER CALL</b>).
   * Uses data from cache if available.
   * @param root_sab the  root source abbreviation
   * @return the current version {@link Source}
   *         associated with the specified root source
   *         abbreviation
   * @throws MEMEException if anything goes wrong
   */
  public Source getCurrentSource(String root_sab) throws MEMEException {
    if (sources == null) {
      getSources();

    }
    for (int i = 0; i < sources.length; i++) {
      if (sources[i].getStrippedSourceAbbreviation() != null &&
          sources[i].getStrippedSourceAbbreviation().equals(root_sab) &&
          sources[i].isCurrent()) {
        return sources[i];
      }
    }
    return null;
  }

  /**
   * Returns the previous version {@link Source} associated with
   * the root source abbreviation (<B>SERVER CALL</b>).
   * Uses data from cache if available.
   * @param root_sab the root source abbreviation
   * @return the previous version {@link Source}
   *         associated with the specified root source
   *         abbreviation
   * @throws MEMEException if anything goes wrong
   */
  public Source getPreviousSource(String root_sab) throws MEMEException {
    if (sources == null) {
      getSources();

    }
    for (int i = 0; i < sources.length; i++) {
      if (sources[i].getStrippedSourceAbbreviation() != null &&
          sources[i].getStrippedSourceAbbreviation().equals(root_sab) &&
          sources[i].isPrevious()) {
        return sources[i];
      }
    }
    return null;
  }

  /**
   * Returns the current English language {@link Source}s (<B>SERVER CALL</b>).
   * Uses data from cache if available.
   * @return the current English language {@link Source}s
   * @throws MEMEException if anything goes wrong
   */
  public Source[] getCurrentEnglishSources() throws MEMEException {

    if (sources == null) {
      getSources();

    }
    ArrayList current_sources = new ArrayList();
    for (int i = 0; i < sources.length; i++) {
      if (sources[i].isCurrent() &&
          sources[i].getLanguage() != null &&
          sources[i].getLanguage().getAbbreviation().equals("ENG")) {
        current_sources.add(sources[i]);
      }
    }
    return (Source[]) current_sources.toArray(new Source[] {});
  }

  /**
   * Adds the specified {@link Source} (<B>SERVER CALL</b>).
   * Recaches data when finished
   * @param source the {@link Source}
   * @throws MEMEException if anything goes wrong
   */
  public void addSource(Source source) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_source"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", source));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    sources = null;
    getSources();
  }

  /**
   * Adds specified {@link Source}s (<B>SERVER CALL</b>).
   * Recaches data when finished.
   * @param sources the {@link Source}s to add
   * @throws MEMEException if anything goes wrong
   */
  public void addSources(Source[] sources) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_source"));
    request.addParameter(new Parameter.Default("command", "ADDS"));
    request.addParameter(new Parameter.Default("param", sources));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    sources = null;
    getSources();

  }

  /**
   * Removes specified {@link Source} (<B>SERVER CALL</b>).
   * Recaches data when finshed.
   * @param source the {@link Source} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeSource(Source source) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_source"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", source));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    sources = null;
    getSources();
  }

  /**
   * Applies changes to the specified {@link Source} (<B>SERVER CALL</b>).
   * Recaches data when finished. Note: source abbreviation cannot be changed.
   * @param source the updated {@link Source} object to change
   * @throws MEMEException if anything goes wrong
   */
  public void setSource(Source source) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_source"));
    request.addParameter(new Parameter.Default("command", "SET"));
    request.addParameter(new Parameter.Default("param", source));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Refresh cache
    sources = null;
    getSources();
  }

  /**
   * Adds specified {@link Termgroup} (<B>SERVER CALL</b>).
   * Recaches data when finished
   * @param termgroup the {@link Termgroup} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addTermgroup(Termgroup termgroup) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_termgroup"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", termgroup));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    termgroups = null;
    getTermgroups();

  }

  /**
   * Adds specified {@link Termgroup}s (<B>SERVER CALL</b>).
   * Recaches data when finished.
   * @param termgroups the {@link Termgroup}s to add
   * @throws MEMEException if anything goes wrong
   */
  public void addTermgroups(Termgroup[] termgroups) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_termgroup"));
    request.addParameter(new Parameter.Default("command", "ADDS"));
    request.addParameter(new Parameter.Default("param", termgroups));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    termgroups = null;
    getTermgroups();

  }

  /**
   * Removes specified {@link Termgroup} (<B>SERVER CALL</b>).
   * Recaches data when finished.
   * @param termgroup the {@link Termgroup} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeTermgroup(Termgroup termgroup) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_termgroup"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", termgroup));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    termgroups = null;
    getTermgroups();

  }

  /**
   * Applies changes to the specified {@link Termgroup} (<B>SERVER CALL</b>).
   * Recaches data when finished.
   * @param termgroup the updated {@link Termgroup} to change
   * @throws MEMEException if anything goes wrong
   */
  public void setTermgroup(Termgroup termgroup) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_termgroup"));
    request.addParameter(new Parameter.Default("command", "SET"));
    request.addParameter(new Parameter.Default("param", termgroup));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Refresh cache
    termgroups = null;
    getTermgroups();
  }

  /**
   * Returns all {@link Termgroup}s (<B>SERVER CALL</b>).
   * Uses cached data if available.
   * @return all {@link Termgroup}s
   * @throws MEMEException if anything goes wrong
   */
  public Termgroup[] getTermgroups() throws MEMEException {
    if (termgroups != null) {
      return termgroups;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_termgroups"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    termgroups = (Termgroup[]) request.getReturnValue("termgroups").getValue();
    return termgroups;
  }

  /**
   * Returns all termgroup names (<B>SERVER CALL</b>).
   * Uses cached data if available.
   * @return all termgroup names
   * @throws MEMEException if failed to get termgroups
   */
  public String[] getTermgroupsAsStrings() throws MEMEException {
    if (termgroups == null) {
      getTermgroups();

    }
    String[] tgs = new String[termgroups.length];
    for (int i = 0; i < termgroups.length; i++) {
      tgs[i] = termgroups[i].toString();
    }
    return tgs;
  }

  /**
   * Returns the {@link Termgroup} for the specied termgroup name (<B>SERVER CALL</b>).
   * @param sab_tty the termgroup name
   * @return the {@link Termgroup} for the specied termgroup name
   * @throws MEMEException if anything goes wrong
   */
  public Termgroup getTermgroup(String sab_tty) throws MEMEException {
    if (termgroups == null) {
      getTermgroups();

    }
    Termgroup termgroup = null;
    for (int i = 0; i < termgroups.length; i++) {
      if (termgroups[i].toString().equals(sab_tty)) {
        termgroup = termgroups[i];
        break;
      }
    }
    return termgroup;
  }

  /**
   * Returns the {@link Termgroup} for the specified source
   * abbreviation and term type (<B>SERVER CALL</b>).
   * @param sab the source abbreviation
   * @param tty the term type
   * @return the {@link Termgroup} for the specified
   *         abbreviation and term type
   * @throws MEMEException if anything goes wrong
   */
  public Termgroup getTermgroup(String sab, String tty) throws MEMEException {
    if (termgroups == null) {
      getTermgroups();

    }
    for (int i = 0; i < termgroups.length; i++) {
      if (termgroups[i].getSource().getSourceAbbreviation().equals(sab) &&
          termgroups[i].getTermType().equals(tty)) {
        return termgroups[i];
      }
    }
    return null;
  }

  /**
   * Adds the specified {@link Language} (<B>SERVER CALL</b>).
   * @param language the {@link Language} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addLanguage(Language language) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_language"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", language));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Removes the specified {@link Language} (<B>SERVER CALL</b>).
   * Refreshes cache when finished.
   * @param language the {@link Language} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeLanguage(Language language) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_language"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", language));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    languages = null;
  }

  /**
   * Sets changes to updated {@link Language} (<B>SERVER CALL</b>).
   * Recaches data when finished.
   * @param language the updated {@link Language} to change
   * @throws MEMEException if anything goes wrong
   */
  public void setLanguage(Language language) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_language"));
    request.addParameter(new Parameter.Default("command", "SET"));
    request.addParameter(new Parameter.Default("param", language));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Returns the {@link Language} for the specified abbreviation (<B>SERVER CALL</b>).
   * @param lat the language abbreviation
   * @return the {@link Language} for the specified abbreviation
   * @throws MEMEException if anything goes wrong
   */
  public Language getLanguage(String lat) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_language"));
    request.addParameter(new Parameter.Default("lat", lat));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (Language) request.getReturnValue("language").getValue();
  }

  /**
   * Returns all {@link Language}s (<B>SERVER CALL</b>).
   * Uses the cache if available.
   * @return all {@link Language}s
   * @throws MEMEException if anything goes wrong
   */
  public Language[] getLanguages() throws MEMEException {
    if (languages != null) {
      return languages;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_languages"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    languages = (Language[]) request.getReturnValue("languages").getValue();
    return languages;
  }

  /**
   * Returns the maximum {@link Identifier} for the specified type (<B>SERVER CALL</b>).
   * @param c a class representing a type of identifier
   * @return the max {@link Identifier} for the specified type
   * @throws MEMEException if anything goes wrong
   */
  public Identifier getMaxIdentifierForType(Class c) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_max_id"));
    request.addParameter(new Parameter.Default("param", c.getName()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (Identifier) request.getReturnValue("max_id").getValue();
  }

  /**
   * Returns the next {@link Identifier} for the specified type (<B>SERVER CALL</b>).
   * @param c a class representing a type of identifier
   * @return the next {@link Identifier} for the specified type
   * @throws MEMEException if anything goes wrong
   */
  public Identifier getNextIdentifierForType(Class c) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_next_id"));
    request.addParameter(new Parameter.Default("param", c.getName()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (Identifier) request.getReturnValue("next_id").getValue();
  }

  /**
       * Returns the {@link WorkLog} for the specified work id (<B>SERVER CALL</b>).
   * @param work_id a work id
   * @return the {@link WorkLog} for the specified work id
   * @throws MEMEException if anything goes wrong
   */
  public WorkLog getWorkLog(int work_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_work_log"));
    request.addParameter(new Parameter.Default("param", work_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (WorkLog) request.getReturnValue("get_work_log").getValue();
  }

  /**
   * Returns all {@link WorkLog}s (<B>SERVER CALL</b>).
   * @return all {@link WorkLog}s
   * @throws MEMEException if anything goes wrong
   */
  public WorkLog[] getWorkLogs() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_work_logs"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (WorkLog[]) request.getReturnValue("get_work_logs").getValue();
  }

  /**
       * Returns the {@link Activity} for the specified {@link MolecularTransaction}
   * (<B>SERVER CALL</B>).
   * @param transaction the {@link MolecularTransaction}
       * @return the {@link Activity} for the specified {@link MolecularTransaction}
   * @throws MEMEException if anything goes wrong
   */
  public Activity getActivityLog(MolecularTransaction transaction) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_activity_log"));
    request.addParameter(new Parameter.Default("param",
                                               transaction.getIdentifier().
                                               intValue()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (Activity) request.getReturnValue("get_activity_log").getValue();
  }

  /**
       * Returns the list of {@link Activity} entries for the specified {@link WorkLog}
   * (<B>SERVER CALL</b>).
   * @param work the {@link WorkLog}
       * @return the list of {@link Activity} entries for the specified {@link WorkLog}
   * @throws MEMEException if anything goes wrong
   */
  public Activity[] getActivityLogs(WorkLog work) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_activity_logs"));
    request.addParameter(new Parameter.Default("param",
                                               work.getIdentifier().intValue()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (Activity[]) request.getReturnValue("get_activity_logs").getValue();
  }

  /**
   * Adds the specified {@link MetaCode} (<B>SERVER CALL</b>).
   * @param mcode the {@link MetaCode} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addMetaCode(MetaCode mcode) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_metacode"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", mcode));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    Identifier id = (Identifier) request.getReturnValue("id").getValue();
    mcode.setIdentifier(id);

  }

  /**
   * Removes the specified {@link MetaCode} (<B>SERVER CALL</b>).
   * @param mcode the {@link MetaCode} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeMetaCode(MetaCode mcode) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_metacode"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", mcode));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Returns the {@link MetaCode} for the specified code and type
   * (<B>SERVER CALL</b>).
   * @param code the code
   * @param type the type
   * @return the {@link MetaCode} for the specified code and type
   * @throws MEMEException if anything goes wrong
   */
  public MetaCode getMetaCode(String code, String type) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_metacode"));
    request.addParameter(new Parameter.Default("param1", code));
    request.addParameter(new Parameter.Default("param2", type));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (MetaCode) request.getReturnValue("get_metacode").getValue();

  }

  /**
   * Returns all {@link MetaCode}s in natural sort order (<B>SERVER CALL</b>).
   * @return all {@link MetaCode}s in natural sort order
   * @throws MEMEException if anything goes wrong
   */
  public MetaCode[] getMetaCodes() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_metacodes"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    MetaCode[] mc = (MetaCode[]) request.getReturnValue("get_metacodes").
        getValue();
    Arrays.sort(mc);
    return mc;
  }

  /**
   * Returns all {@link MetaCode} types (<B>SERVER CALL</b>).
   * @return all {@link MetaCode} types
   * @throws MEMEException if anything goes wrong
   */
  public String[] getMetaCodeTypes() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_metacode_types"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String[]) request.getReturnValue("get_metacode_types").getValue();

  }

  /**
       * Returns all {@link MetaCode}s having the specified type (<B>SERVER CALL</b>).
   * @param type the specified type
   * @return all {@link MetaCode}s having the specified type
   * @throws MEMEException if anything goes wrong
   */
  public MetaCode[] getMetaCodesByType(String type) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_metacodes_by_type"));
    request.addParameter(new Parameter.Default("param", type));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    MetaCode[] mc = (MetaCode[]) request.getReturnValue("get_metacodes_by_type").
        getValue();
    Arrays.sort(mc);
    return mc;
  }

  /**
   * Adds specified {@link MetaProperty} (<B>SERVER CALL</b>).
   * @param meta_prop the {@link MetaProperty} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addMetaProperty(MetaProperty meta_prop) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "manage_meta_property"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", meta_prop));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    Identifier id = (Identifier) request.getReturnValue("id").getValue();
    meta_prop.setIdentifier(id);

  }

  /**
   * Removes specified {@link MetaProperty} (<B>SERVER CALL</b>).
   * @param meta_prop the {@link MetaProperty} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeMetaProperty(MetaProperty meta_prop) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "manage_meta_property"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", meta_prop));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Returns the {@link MetaProperty} for the specified key,
   * key qualifier, and value (<B>SERVER CALL</b>).
   * @param key the key
   * @param key_qualifier the key qualifier
   * @param value the value
   * @return the {@link MetaProperty} for the specified key,
   * key qualifier, and value
   * @throws MEMEException if anything goes wrong
   */
  public MetaProperty getMetaProperty(String key, String key_qualifier,
                                      String value) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_meta_property"));
    request.addParameter(new Parameter.Default("param1", key));
    request.addParameter(new Parameter.Default("param2", key_qualifier));
    request.addParameter(new Parameter.Default("param3", value));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (MetaProperty) request.getReturnValue("get_meta_property").getValue();

  }

  /**
   * Returns all {@link MetaProperty} entries in natural sort order (<B>SERVER CALL</b>).
   * @return all {@link MetaProperty} entries
   * @throws MEMEException if anything goes wrong
   */
  public MetaProperty[] getMetaProperties() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_meta_properties"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    MetaProperty[] mp = (MetaProperty[]) request.getReturnValue(
        "get_meta_properties").getValue();
    Arrays.sort(mp);
    return mp;
  }

  /**
       * Returns all {@link MetaProperty} key qualifier values (<B>SERVER CALL</b>).
   * @return all {@link MetaProperty} key qualifier values
   * @throws MEMEException if anything goes wrong
   */
  public String[] getMetaPropertyKeyQualifiers() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_meta_prop_key_qualifiers"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String[]) request.getReturnValue("get_meta_prop_key_qualifiers").
        getValue();

  }

  /**
   * Returns all {@link MetaProperty} entries for the specified key qualifier
   * (<B>SERVER CALL</b>).
   * @param key_qualifier the key qualifier
   * @return all {@link MetaProperty} entries for the specified key qualifier
   * @throws MEMEException if anything goes wrong
   */
  public MetaProperty[] getMetaPropertiesByKeyQualifier(String key_qualifier) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
        "get_meta_props_by_key_qualifier"));
    request.addParameter(new Parameter.Default("param", key_qualifier));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    MetaProperty[] mp = (MetaProperty[]) request.getReturnValue(
        "get_meta_props_by_key_qualifier").getValue();
    Arrays.sort(mp);
    return mp;

  }

  /**
   * Returns the code for the specified type and value (<B>SERVER CALL</b>).
   * Uses data from <code>code_map</code>.
   * @param type the type
   * @param value the value
   * @return the code for the specified type and value
   * @throws MEMEException if anything goes wrong
   */
  public String getCodeByValue(String type, String value) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_code_by_value"));
    request.addParameter(new Parameter.Default("param1", type));
    request.addParameter(new Parameter.Default("param2", value));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("code_by_value").getValue();
  }

  /**
   * Returns the value for the specified type and code (<B>SERVER CALL</b>).
   * Uses data from <code>code_map</code>.
   * @param type the type
   * @param code the code
   * @return the value for the specified type and code
   * @throws MEMEException if anything goes wrong
   */
  public String getValueByCode(String type, String code) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_value_by_code"));
    request.addParameter(new Parameter.Default("param1", type));
    request.addParameter(new Parameter.Default("param2", code));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("value_by_code").getValue();
  }

  /**
       * Returns all valid "status" values for the specified type (<B>SERVER CALL</b>).
   * Types include {@link Atom},
   * {@link Attribute},
   * {@link Concept}, and
   * {@link Relationship}.
   * @param c a {@link Class} representing the specified type
   * @return all valid status values for the specified type
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidStatusValuesForType(Class c) throws MEMEException {
    if (c == Atom.class) {
      return getValidStatusValuesForAtoms();
    } else if (c == Attribute.class) {
      return getValidStatusValuesForAttributes();
    } else if (c == Concept.class) {
      return getValidStatusValuesForConcepts();
    } else if (c == Relationship.class) {
      return getValidStatusValuesForRelationships();
    } else {
      return new char[0];
    }
  }

  /**
       * Returns all valid "status" values for an {@link Atom} (<B>SERVER CALL</b>}.
   * Uses data in cache if available.
   * @return all valid status values for an {@link Atom}
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidStatusValuesForAtoms() throws MEMEException {
    if (atoms_status != null) {
      return atoms_status;
    }
    atoms_status = getValidStatusValuesForCRACS("atoms");
    return atoms_status;
  }

  /**
   * Returns all valid "status" values for an {@link Attribute} (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @return all vlaid status values for an {@link Attribute}
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidStatusValuesForAttributes() throws MEMEException {
    if (attributes_status != null) {
      return attributes_status;
    }
    attributes_status = getValidStatusValuesForCRACS("attributes");
    return attributes_status;
  }

  /**
       * Returns all valid "status" values for a {@link Concept} (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @return all valid status values for a {@link Concept}
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidStatusValuesForConcepts() throws MEMEException {
    if (concepts_status != null) {
      return concepts_status;
    }
    concepts_status = getValidStatusValuesForCRACS("concepts");
    return concepts_status;
  }

  /**
   * Returns all valid "status" values for a {@link Relationship} (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   *         an {@link Relationship}
   * @return all valid status values for a {@link Relationship}
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidStatusValuesForRelationships() throws MEMEException {
    if (relationships_status != null) {
      return relationships_status;
    }
    relationships_status = getValidStatusValuesForCRACS("relationships");
    return relationships_status;
  }

  /**
   * Helper function for obtaining status values, returns valid status
   * values for specified type.
   * @param cracs type specified as table name
   * @return all valid status values for specified type
   * @throws MEMEException if anything goes wrong
   */
  private char[] getValidStatusValuesForCRACS(String cracs) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "status_for_" + cracs));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (char[]) request.getReturnValue("status_for_" + cracs).getValue();

  }

  /**
       * Returns all valid "level" values for the specified type (<B>SERVER CALL</b>).
   * Valid types include {@link Attribute} and {@link Relationship}.
   * @param c a {@link Class} representing the specified type
   * @return all valid level values for the specified type
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidLevelValuesForType(Class c) throws MEMEException {
    if (c == Attribute.class) {
      return getValidLevelValuesForAttributes();
    } else if (c == Relationship.class) {
      return getValidLevelValuesForRelationships();
    } else {
      return new char[0];
    }
  }

  /**
   * Returns all valid "level" values for an {@link Attribute} (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @return all valid level values for an {@link Attribute}
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidLevelValuesForAttributes() throws MEMEException {
    if (attributes_level != null) {
      return attributes_level;
    }
    attributes_level = getValidLevelValuesForCRACS("attributes");
    return attributes_level;
  }

  /**
   * Returns all valid "level" values for an {@link Relationship} (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @return all valid level values for an {@link Relationship}
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidLevelValuesForRelationships() throws MEMEException {
    if (relationships_level != null) {
      return relationships_level;
    }
    relationships_level = getValidLevelValuesForCRACS("relationships");
    return relationships_level;
  }

  /**
   * Helper function for obtaining level values.
   * @param table the type as a table name
   * @return all valid level values for the specified type
   * @throws MEMEException if anything goes wrong
   */
  private char[] getValidLevelValuesForCRACS(String table) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "level_for_" + table));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (char[]) request.getReturnValue("level_for_" + table).getValue();

  }

  /**
   * Returns all valid "released" values (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @return all valid "released" values
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidReleasedValues() throws MEMEException {
    if (released != null) {
      return released;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "valid_released"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    released = (char[]) request.getReturnValue("valid_released").getValue();
    return released;
  }

  /**
   * Returns all valid "tobereleased" values (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @return all valid "tobereleased" values
   * @throws MEMEException if anything goes wrong
   */
  public char[] getValidTobereleasedValues() throws MEMEException {
    if (tobereleased != null) {
      return tobereleased;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "valid_tobereleased"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    tobereleased = (char[]) request.getReturnValue("valid_tobereleased").
        getValue();
    return tobereleased;
  }

  /**
   * Adds specified {@link EditorPreferences} (<B>SERVER CALL</b>).
   * Invalidates cache when finished.
   * @param ep the {@link EditorPreferences} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addEditorPreferences(EditorPreferences ep) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_ep"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", ep));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    editor_preferences = null;
  }

  /**
   * Removes the specified {@link EditorPreferences} (<B>SERVER CALL</b>).
   * Invalidates cache when finished.
   * @param ep the {@link EditorPreferences} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeEditorPreferences(EditorPreferences ep) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_ep"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", ep));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    editor_preferences = null;
  }

  /**
   * Applies changes to {@link EditorPreferences} (<B>SERVER CALL</b>).
   * Invalidates cache when finished.
   * @param ep the {@link EditorPreferences} to change
   * @throws MEMEException if anything goes wrong
   */
  public void setEditorPreferences(EditorPreferences ep) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_ep"));
    request.addParameter(new Parameter.Default("command", "SET"));
    request.addParameter(new Parameter.Default("param", ep));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    editor_preferences = null;
  }

  /**
   * Returns all {@link EditorPreferences} (<B>SERVER CALL</b>).
   * Caches data.
   * @return all {@link EditorPreferences}
   * @throws MEMEException if anything goes wrong
   */
  public EditorPreferences[] getEditorPreferences() throws MEMEException {
    if (editor_preferences != null) {
      return editor_preferences;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_editor_preferences"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    editor_preferences =
        (EditorPreferences[]) request.getReturnValue("editor_preferences").
        getValue();
    return editor_preferences;
  }

  /**
   * Returns the {@link EditorPreferences} for a specified username (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @param username the username
   * @return the {@link EditorPreferences} for a specified username.
   * @throws MEMEException if failed to get editor preferences
   */
  public EditorPreferences getEditorPreferencesByUsername(String username) throws
      MEMEException {

    if (editor_preferences == null) {
      getEditorPreferences();

    }
    EditorPreferences ep = null;
    for (int i = 0; i < editor_preferences.length; i++) {
      if (editor_preferences[i].getUserName().equals(username)) {
        ep = editor_preferences[i];
        break;
      }
    }

    return ep;
  }

  /**
   * Returns the {@link EditorPreferences} for specified initials (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @param initials the editor initials
   * @return the {@link EditorPreferences} for specified initials
   * @throws MEMEException if anything goes wrong
   */
  public EditorPreferences getEditorPreferencesByInitials(String initials) throws
      MEMEException {
    if (editor_preferences == null) {
      getEditorPreferences();

    }
    EditorPreferences ep = null;
    for (int i = 0; i < editor_preferences.length; i++) {
      if (editor_preferences[i].getInitials().equals(initials)) {
        ep = editor_preferences[i];
        break;
      }
    }
    return ep;
  }

  /**
   * Returns a {@link Map} of relationship names to their inverses (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @return a {@link Map} of relationship names to their inverses
   * @throws MEMEException if anything goes wrong
   */
  private Map getInverseRelationshipNameMap() throws MEMEException {
    if (inverse_rels != null) {
      return inverse_rels;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "inverse_rel_name"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    inverse_rels = (Map) request.getReturnValue("inverse_rel_name").getValue();
    return inverse_rels;
  }

  /**
   * Returns a {@link Map} of relationship attributes to their inverses (<B>SERVER CALL</b>).
   * Uses data in cache if available.
   * @return a {@link Map} of relationship attributes to their inverses
   * @throws MEMEException if anything goes wrong
   */
  private Map getInverseRelationshipAttributeMap() throws MEMEException {
    if (inverse_relas != null) {
      return inverse_relas;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "inverse_rel_attr"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    inverse_relas = (Map) request.getReturnValue("inverse_rel_attr").getValue();
    return inverse_relas;
  }

  /**
   * Returns the {@link Authority} for the specified authority name (<B>SERVER CALL</b>).
   * @param auth the authority name
   * @return The {@link Authority} for the specified authority name
   * @throws MEMEException if anything goes wrong
   */
  public Authority getAuthority(String auth) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_authority"));
    request.addParameter(new Parameter.Default("param", auth));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (Authority) request.getReturnValue("authority").getValue();
  }

  /**
   * Returns the {@link MolecularAction} for the specified molecule id
   * (<B>SERVER CALL</b>).
   * @param molecule_id the <code>int</code> molecule id
   * @return the {@link MolecularAction} for the specified molecule id
   * @throws MEMEException if anything goes wrong
   */
  public MolecularAction getMolecularAction(int molecule_id) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_molecular_action"));
    request.addParameter(new Parameter.Default("param", molecule_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (MolecularAction) request.getReturnValue("molecular_action").
        getValue();
  }

  /**
   * Returns the full {@link MolecularAction} for the specified molecule id
   * (<B>SERVER CALL</b>).  A full action includes al {@link AtomicAction}s.
   * @param molecule_id the <code>int</code> molecule id
   * @return the full {@link MolecularAction} for the specified molecule id
   * @throws MEMEException if anything goes wrong
   */
  public MolecularAction getFullMolecularAction(int molecule_id) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_full_molecular_action"));
    request.addParameter(new Parameter.Default("param", molecule_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (MolecularAction) request.getReturnValue("full_molecular_action").
        getValue();
  }

  /**
   * Returns the {@link AtomicAction} for the specified atomic action id
   * (<B>SERVER CALL</b>).
   * @param atomic_action_id the <code>int</code> atomic action id
   * @return the {@link AtomicAction} for the specified atomic action id
   * @throws MEMEException if anything goes wrong
   */
  public AtomicAction getAtomicAction(int atomic_action_id) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_atomic_action"));
    request.addParameter(new Parameter.Default("param", atomic_action_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (AtomicAction) request.getReturnValue("atomic_action").getValue();
  }

  /**
   * Adds specified application {@link IntegrityVector} (<B>SERVER CALL</b>).
   * Adds application vector to cache.
   * @param application the application name
   * @param iv the {@link IntegrityVector}.
   * @throws MEMEException if anything goes wrong
   */
  public void addApplicationVector(String application, IntegrityVector iv) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_application_vector"));
    request.addParameter(new Parameter.Default("param1", application));
    request.addParameter(new Parameter.Default("param2", iv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.put(application, iv);
  }

  /**
   * Adds the {@link IntegrityCheck} to the specified application vector with
   * the specified code (<B>SERVER CALL</b>).  For example, <MGV_A4:E> could be
   * added to the "DEFAULT" application vector.  Clears vector from cache.
   * @param application the application name whose vector is to be modified
   * @param ic the {@link IntegrityCheck} to add
   * @param code the enforcement level code
   * @throws MEMEException if anything goes wrong
   */
  public void addCheckToApplicationVector(String application, IntegrityCheck ic,
                                          String code) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
        "add_check_to_application_vector"));
    request.addParameter(new Parameter.Default("param1", application));
    request.addParameter(new Parameter.Default("param2", ic));
    request.addParameter(new Parameter.Default("param3", code));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(application);

  }

  /**
   * Applies changes to the specified application {@link IntegrityVector}
   * (<B>SERVER CALL</B>).  Clears vector from cache when finished.
   * @param application the application name
   * @param iv the {@link IntegrityVector} to change
   * @throws MEMEException if anything goes wrong
   */
  public void setApplicationVector(String application, IntegrityVector iv) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "set_application_vector"));
    request.addParameter(new Parameter.Default("param1", application));
    request.addParameter(new Parameter.Default("param2", iv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(application);
  }

  /**
   * Removes specified application vector (<B>SERVER CALL</B>).
   * Clears vector from cache when finished.
   * @param application the application name
   * @throws MEMEException if anything goes wrong
   */
  public void removeApplicationVector(String application) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "remove_application_vector"));
    request.addParameter(new Parameter.Default("param", application));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(application);
  }

  /**
   * Removes the {@link IntegrityCheck} from the specified application vector
   * (<B>SERVER CALL</>B).  Clears vector from cache when finished.
   * @param application the application name
   * @param ic the {@link IntegrityCheck} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeCheckFromApplicationVector(String application,
                                               IntegrityCheck ic) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
        "remove_check_from_application_vector"));
    request.addParameter(new Parameter.Default("param1", application));
    request.addParameter(new Parameter.Default("param2", ic));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(application);
  }

  /**
   * Returns the {@link EnforcableIntegrityVector} for the specified application name
   * (<B>SERVER CALL</B>).  Uses data in cache if available.
   * @param app the application name
   * @return the {@link EnforcableIntegrityVector} for the specified application name
   * @throws MEMEException if anything goes wrong
   */
  public EnforcableIntegrityVector getApplicationVector(String app) throws
      MEMEException {
    if (vectors.containsKey(app)) {
      return (EnforcableIntegrityVector) vectors.get(app);
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_application_vector"));
    request.addParameter(new Parameter.Default("param", app));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    EnforcableIntegrityVector vector =
        (EnforcableIntegrityVector) request.getReturnValue("application_vector").
        getValue();
    vectors.put(app, vector);
    return vector;
  }

  /**
   * Returns all application vector names (<B>SERVER CALL</b>).
   * @return all application vector names
   * @throws MEMEException if anything goes wrong
   */
  public String[] getApplicationsWithVectors() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_applications_with_vectors"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    String[] apps =
        (String[]) request.getReturnValue("applications_with_vectors").getValue();
    return apps;
  }

  /**
   * Adds {@link IntegrityVector} to specified editor override level (<B>SERVER CALL</B>).
   * Clears vector from cache when finished.
   * @param ic_level the editor override level
   * @param iv the {@link IntegrityVector} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addOverrideVector(int ic_level, IntegrityVector iv) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_override_vector"));
    request.addParameter(new Parameter.Default("param1", ic_level));
    request.addParameter(new Parameter.Default("param2", iv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(Integer.toString(ic_level));
  }

  /**
       * Adds the {@link IntegrityCheck} to the specified editor override level with
   * the specified enforcement code (<B>SERVER CALL</b>).
   * Clears vector from cache when finished.
   * @param ic_level the editor override level
   * @param ic the {@link IntegrityCheck} to add
   * @param code the enforcement code
   * @throws MEMEException if anything goes wrong
   */
  public void addCheckToOverrideVector(int ic_level, IntegrityCheck ic,
                                       String code) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_check_to_override_vector"));
    request.addParameter(new Parameter.Default("param1", ic_level));
    request.addParameter(new Parameter.Default("param2", ic));
    request.addParameter(new Parameter.Default("param3", code));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(Integer.toString(ic_level));
  }

  /**
   * Applies changes to the specified override {@link IntegrityVector}
   * (<B>SERVER CALL</B>). Clears vector from cache when finished.
   * @param ic_level the editor override level
   * @param iv the {@link IntegrityVector} to change
   * @throws MEMEException if anything goes wrong
   */
  public void setOverrideVector(int ic_level, IntegrityVector iv) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "set_override_vector"));
    request.addParameter(new Parameter.Default("param1", ic_level));
    request.addParameter(new Parameter.Default("param2", iv));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(Integer.toString(ic_level));
  }

  /**
   * Removes the specified override vector (<B>SERVER CALL</B>).
   * Clears vector from cache when finished.
   * @param ic_level the editor override level
   * @throws MEMEException if anything goes wrong
   */
  public void removeOverrideVector(int ic_level) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "remove_override_vector"));
    request.addParameter(new Parameter.Default("param", ic_level));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(Integer.toString(ic_level));
  }

  /**
   * Removes the {@link IntegrityCheck} from the specified override vector
   * (<B>SERVER CALL</B>).  Clears vector from cache when finished.
   * @param ic_level the editor override level
   * @param ic the check {@link IntegrityCheck} to add
   * @throws MEMEException if anything goes wrong
   */
  public void removeCheckFromOverrideVector(int ic_level, IntegrityCheck ic) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
        "remove_check_from_override_vector"));
    request.addParameter(new Parameter.Default("param1", ic_level));
    request.addParameter(new Parameter.Default("param2", ic));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(Integer.toString(ic_level));
  }

  /**
   * Returns the {@link IntegrityVector} for the specified editor
   * override level (<B>SERVER CALL</b>).  Uses data in cache if available.
   * @param ic_level the editor override level
   * @return the {@link IntegrityVector} for the specified editor
   * override level
   * @throws MEMEException if anything goes wrong
   */
  public IntegrityVector getOverrideVector(int ic_level) throws MEMEException {
    if (vectors.containsKey(Integer.toString(ic_level))) {
      return (IntegrityVector) vectors.get(Integer.toString(ic_level));
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_override_vector"));
    request.addParameter(new Parameter.Default("param", ic_level));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    IntegrityVector vector =
        (IntegrityVector) request.getReturnValue("override_vector").getValue();
    vectors.put(Integer.toString(ic_level), vector);
    return vector;
  }

  /**
       * Returns all editor override levels with override vectors (<B>SERVER CALL</B>).
   * @return all editor override levels with override vectors
   * @throws MEMEException if anything goes wrong
   */
  public int[] getLevelsWithOverrideVectors() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
        "get_levels_with_override_vectors"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    int[] levels =
        (int[]) request.getReturnValue("levels_with_override_vectors").getValue();
    return levels;
  }

  /**
       * Adds a new {@link IntegrityCheck} (<B>SERVER CALL</B>).  This must be called
   * before adding the check to either application or override vectors.
   * @param ic the {@link IntegrityCheck} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addIntegrityCheck(IntegrityCheck ic) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_ic"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", ic));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
       * Applies changes to the specified {@link IntegrityCheck} (<B>SERVER CALL</b>).
   * Removes the check from the cache when finished.
   * @param ic the {@link IntegrityCheck} to change
   * @throws MEMEException if anything goes wrong.
   */
  public void setIntegrityCheck(IntegrityCheck ic) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_ic"));
    request.addParameter(new Parameter.Default("command", "SET"));
    request.addParameter(new Parameter.Default("param", ic));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(ic.getName());
  }

  /**
   * Removes the specified {@link IntegrityCheck} (<B>SERVER CALL</b>).
   * Removes the check from the cache when finished.
   * @param ic the {@link IntegrityCheck} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeIntegrityCheck(IntegrityCheck ic) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_ic"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", ic));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    vectors.remove(ic.getName());
  }

  /**
   * Returns the {@link IntegrityCheck} for the specified name (<B>SERVER CALL</B>).
   * Adds the check to the cache.
   * @param ic_name the integrity check name
   * @return the {@link IntegrityCheck} for the specified name
   * @throws MEMEException if anything goes wrong
   */
  public IntegrityCheck getIntegrityCheck(String ic_name) throws MEMEException {
    if (vectors.containsKey(ic_name)) {
      return (IntegrityCheck) vectors.get(ic_name);
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_integrity_check"));
    request.addParameter(new Parameter.Default("param", ic_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    IntegrityCheck ic =
        (IntegrityCheck) request.getReturnValue("integrity_check").getValue();
    vectors.put(ic_name, ic);
    return ic;
  }

  /**
   * Returns all {@link IntegrityCheck}s (<B>SERVER CALL</B>).
   * @return all {@link IntegrityCheck}s
   * @throws MEMEException if anything goes wrong
   */
  public IntegrityCheck[] getIntegrityChecks() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_integrity_checks"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    IntegrityCheck[] checks =
        (IntegrityCheck[]) request.getReturnValue("integrity_checks").getValue();
    return checks;
  }

  /**
   * Activates the specified {@link IntegrityCheck} (<B>SERVER CALL</b>).
   * @param ic the {@link IntegrityCheck} to activate
   * @throws MEMEException if anything goes wrong
   */
  public void activateIntegrityCheck(IntegrityCheck ic) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_ic"));
    request.addParameter(new Parameter.Default("command", "ACTIVATE"));
    request.addParameter(new Parameter.Default("param", ic));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Deactivates the specified {@link IntegrityCheck} (<B>SERVER CALL</b>).
   * @param ic the {@link IntegrityCheck} to deactivate
   * @throws MEMEException if anything goes wrong
   */
  public void deactivateIntegrityCheck(IntegrityCheck ic) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_ic"));
    request.addParameter(new Parameter.Default("command", "DEACTIVATE"));
    request.addParameter(new Parameter.Default("param", ic));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Returns all valid relationship names (<B>SERVER CALL</B>).
   * Uses data in cache if available.
   * @return all valid relationship names
   * @throws MEMEException if anything goes wrong
   */
  public String[] getValidRelationshipNames() throws MEMEException {
    if (inverse_rels == null) {
      getInverseRelationshipNameMap();

      // Process response
    }
    return (String[]) inverse_rels.keySet().toArray(new String[0]);
  }

  /**
   * Returns all valid relationship attributes (<B>SERVER CALL</B>).
   * Uses data in cache if available.
   * @return all valid relationship attributes
   * @throws MEMEException if anything goes wrong
   */
  public String[] getValidRelationshipAttributes() throws MEMEException {
    if (inverse_relas == null) {
      getInverseRelationshipAttributeMap();

      // Process response
    }
    return (String[]) inverse_relas.keySet().toArray(new String[0]);
  }

  /**
   * Adds the specified {@link SemanticType} (<B>SERVER CALL</b>).
   * Recaches data when finished.
   * @param sty the {@link SemanticType} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addValidSemanticType(SemanticType sty) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_sty"));
    request.addParameter(new Parameter.Default("command", "ADD"));
    request.addParameter(new Parameter.Default("param", sty));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    Identifier id = (Identifier) request.getReturnValue("id").getValue();
    sty.setTypeIdentifier(id);

    valid_stys = null;
    getValidSemanticTypes();
  }

  /**
   * Removes the specified {@link SemanticType} (<B>SERVER CALL</b>).
   * Recaches data when finished
   * @param sty the {@link SemanticType} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeValidSemanticType(SemanticType sty) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "manage_sty"));
    request.addParameter(new Parameter.Default("command", "REMOVE"));
    request.addParameter(new Parameter.Default("param", sty));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    valid_stys = null;
    getValidSemanticTypes();
  }

  /**
   * Returns all valid {@link SemanticType}s (<B>SERVER CALL</B>).
   * Caches data.
   * @return all valid {@link SemanticType}s
   * @throws MEMEException if anything goes wrong
   */
  public SemanticType[] getValidSemanticTypes() throws MEMEException {
    if (valid_stys != null) {
      return valid_stys;
    }

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_semantic_types"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    valid_stys = (SemanticType[]) request.getReturnValue("semantic_types").
        getValue();
    return valid_stys;
  }

  /**
   * Returns all valid semantic type names (<B>SERVER CALL</b>).
   * Uses data in cache if available
   * @return all valid semantic type names
   * @throws MEMEException if anything goes wrong
   */
  public String[] getValidSemanticTypeValues() throws MEMEException {
    if (valid_stys == null) {
      getValidSemanticTypes();

    }
    String[] stys = new String[valid_stys.length];
    for (int i = 0; i < valid_stys.length; i++) {
      stys[i] = valid_stys[i].getValue();
    }
    return stys;
  }

}
