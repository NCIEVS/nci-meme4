/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  CoreDataClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This client API is used to access core data from the MID.
 * See {@link ClientAPI} for information
 * on configuring properties required by this class.
 *
 * With the properties properly configured, accessing core data
 * services is as simple as instantiating the client and
 * calling its methods.  For example,
 *
 * <pre>
 *   // Instantiate client
 *   // connected to default data source ("editing-db")
 *   CoreDataClient client = new CoreDataClient();
 *
 *   // Read concept# 123403
 *   Concept concept = client.getConcept(123403)
 *
 *   // Read concept for C0023493
 *   Concept concept2 = client.getConcept(new CUI("C0023493"));
 *
 *   // Re-read concept 2
 *   concept2 = client.getConcept(concept2);
 *
 *   // Re-read concept 2
 *   concept2 = client.getConcept(concept2.getIdentifier());
 *
 *   // Read an atom
 *   Atom atom = client.getAtom(13245);
 *
 *   // Populate a concept relationships
 *   client.populateRelationships(concept2);
 *
 *   // Find a relationship (whether dead or alive)
 *   Relationship r = null;
 *   int relationship_id = 12348432;
 *   try {
 *      // First try to find it if it is alive
 *      r = client.getRelationship(relationship_id);
 *   } catch (MEMEException me) {
 *      // Then, try to find if it is dead
 *      try {
 *        r = client.getDeadRelationship(relationship_id);
 *      } catch (MEMEException me) {
 *        // bogus relationship id
 *      }
 *   }
 * </pre>
 *
 * This class currently only provides direct access to
 * {@link Concept} and {@link Atom} objects
 *
 * @author MEME Group
 */
public class CoreDataClient extends ClientAPI {

  //
  // Fields
  //
  private String mid_service = null;
  private String session_id = null;
  private Authentication auth = null;
  private boolean include_or_exclude_lats = false;
  private String[] selected_languages = new String[0];

  //
  // Constructors
  //

  /**
       * Instantiates a {@link CoreDataClient} connected to the default mid service.
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated.
   */
  public CoreDataClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiates a {@link CoreDataClient} connected to the
   * specified mid service.
   * @param mid_service a valid MID service
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public CoreDataClient(String mid_service) throws MEMEException {
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
    request.setService("CoreDataService");
    request.setMidService(mid_service);
    request.setAuthentication(auth);
    request.addParameter(new Parameter.Default("selected_languages",
                                               getReadLanguages()));
    request.addParameter(new Parameter.Default("include_or_exclude",
                                               includeOrExcludeLanguages()));

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
  // Core Data API
  //

  /**
   * Returns the fully populated {@link Concept} (<B>SERVER CALL</B>).
   * @param concept the {@link Concept} to populate
   * @return a refreshed copy of the specified {@link Concept}
   * @throws MEMEException if anything goes wrong to process get concept
   * @throws MissingDataException if failed due to missing data
   */
  public Concept getConcept(Concept concept) throws MEMEException,
      MissingDataException {
    return getConcept(concept.getIdentifier().intValue());
  }

  /**
   * Returns the {@link Concept} for the specified {@link Identifier} (<B>SERVER CALL</b>).
   * @param identifier the concept {@link Identifier}
   * @return the {@link Concept} for the specified identifier
   * @throws MEMEException if failed to process get concept
   * @throws MissingDataException if failed due to missing data
   */
  public Concept getConcept(Identifier identifier) throws MEMEException,
      MissingDataException {
    return getConcept(identifier.intValue());
  }

  /**
       * Returns the {@link Concept} for the specified concept id (<B>SERVER CALL</B>).
   * @param concept_id the <code>int</code> concept id
   * @return the {@link Concept} for the specified concept id
   * @throws MEMEException if failed to process get concept
   * @throws MissingDataException if failed due to missing data
   */
  public Concept getConcept(int concept_id) throws MEMEException,
      MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_concept"));
    request.addParameter(new Parameter.Default("concept", concept_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Concept) request.getReturnValue("get_concept").getValue();
  }

  /**
   * Returns the {@link Concept} for the specified {@link CUI} (<B>SERVER CALL</B>).
   * @param cui the {@link CUI}
   * @return the {@link Concept} for the specified {@link CUI}
   * @throws MEMEException if failed to process get concept
   * @throws MissingDataException if failed due to missing data
   */
  public Concept getConcept(CUI cui) throws MEMEException, MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_concept"));
    request.addParameter(new Parameter.Default("cui", cui));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Concept) request.getReturnValue("get_concept").getValue();
  }

  /**
   * Returns the fully populated {@link Atom} (<B>SERVER CALL</B>).
   * @param atom the {@link Atom} to refresh
   * @return the refreshed {@link Atom} object
   * @throws MEMEException if failed to process get atom
   * @throws MissingDataException if failed due to missing data
   */
  public Atom getAtom(Atom atom) throws MEMEException, MissingDataException {
    return getAtom(atom.getIdentifier().intValue());
  }

  /**
   * Returns the {@link Atom} for the specified atom {@link Identifier} (<B>SERVER CALL</B>).
   * @param identifier the atom {@link Identifier}
   * @return the {@link Atom} for the specified atom identifier
   * @throws MEMEException if failed to process get atom
   * @throws MissingDataException if failed due to missing data
   */
  public Atom getAtom(Identifier identifier) throws MEMEException,
      MissingDataException {
    return getAtom(identifier.intValue());
  }

  /**
   * Returns the {@link Atom} for the specified atom id (<B>SERVER CALL</B>).
   * @param atom_id the <code>int</code> atom id
   * @return the {@link Atom} for the specified atom id
   * @throws MEMEException if failed to process get atom
   * @throws MissingDataException if failed due to missing data
   */
  public Atom getAtom(int atom_id) throws MEMEException, MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_atom"));
    request.addParameter(new Parameter.Default("atom", atom_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Atom) request.getReturnValue("get_atom").getValue();
  }

  /**
   * Returns all {@link Atom}s from the specified {@link Concept} (<B>SERVER CALL</B>)
   * @param c the {@link Concept}
   * @return all {@link Atom}s from the specified {@link Concept}
   * @throws MEMEException if failed to process get atoms
   * @throws MissingDataException if failed due to missing data
   */
  public Atom[] getAtoms(Concept c) throws MEMEException, MissingDataException {
    return getConcept(c).getAtoms();
  }

  /**
   * Returns a refreshed copy of the {@link Attribute} (<B>SERVER CALL</B>).
   * @param attr An object {@link Attribute}
   * @return An object {@link Attribute}
   * @throws MEMEException if failed to process get attribute
   * @throws MissingDataException if failed due to missing data
   */
  public Attribute getAttribute(Attribute attr) throws MEMEException,
      MissingDataException {
    return getAttribute(attr.getIdentifier().intValue());
  }

  /**
   * Returns the {@link Attribute} for the specified attribute id (<B>SERVER CALL</B>).
   * @param attr_id the <code>int</code> attribute id
   * @return the {@link Attribute} for the specified attribute id
   * @throws MEMEException if failed to process get attribute
   * @throws MissingDataException if failed due to missing data
   */
  public Attribute getAttribute(int attr_id) throws MEMEException,
      MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_attribute"));
    request.addParameter(new Parameter.Default("attribute", attr_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Attribute) request.getReturnValue("get_attribute").getValue();
  }

  /**
   * Returns all {@link Attribute}s for the specified {@link Concept} (<B>SERVER CALL</B>).
   * @param c the {@link Concept}
   * @return all {@link Attribute}s for the specified {@link Concept}
   * @throws MEMEException if failed to process get attributes
   * @throws MissingDataException if failed due to missing data
   */
  public Attribute[] getAttributes(Concept c) throws MEMEException,
      MissingDataException {
    return getConcept(c).getAttributes();
  }

  /**
       * Returns the dead {@link Atom} for the specified atom id (<B>SERVER CALL</B>).
   * @param atom_id the <code>int</code> atom id
   * @return the {@link Atom} for the specified atom id
   * @throws MEMEException if failed to process get dead atom
   * @throws MissingDataException if failed due to missing data
   */
  public Atom getDeadAtom(int atom_id) throws MEMEException,
      MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_dead_atom"));
    request.addParameter(new Parameter.Default("dead_atom", atom_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Atom) request.getReturnValue("get_dead_atom").getValue();
  }

  /**
   * Returs the dead {@link Attribute} for the specified attribute id (<B>SERVER CALL</B>).
   * @param attr_id the <code>int</code> attribute id
   * @return the {@link Attribute} for the specified attribute id
   * @throws MEMEException if failed to process get dead attribute
   * @throws MissingDataException if failed due to missing data
   */
  public Attribute getDeadAttribute(int attr_id) throws MEMEException,
      MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_dead_attribute"));
    request.addParameter(new Parameter.Default("dead_attribute", attr_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Attribute) request.getReturnValue("get_dead_attribute").getValue();
  }

  /**
   * Returns the dead {@link Concept} for the specified concept id (<B>SERVER CALL</B>).
   * @param concept_id the <code>int</code> concept id
   * @return the {@link Concept} for the specified concept id
   * @throws MEMEException if failed to process get dead concept
   * @throws MissingDataException if failed due to missing data
   */
  public Concept getDeadConcept(int concept_id) throws MEMEException,
      MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_dead_concept"));
    request.addParameter(new Parameter.Default("dead_concept", concept_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Concept) request.getReturnValue("get_dead_concept").getValue();
  }

  /**
   * Returns the dead {@link Relationship} for the specified relationship id (<B>SERVER CALL</B>).
   * @param rel_id the <code>int</code> relationship id
   * @return the {@link Relationship} for the specified relationship id
   * @throws MEMEException if failed to process get dead relationship
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship getDeadRelationship(int rel_id) throws MEMEException,
      MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "get_dead_relationship"));
    request.addParameter(new Parameter.Default("dead_relationship", rel_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Relationship) request.getReturnValue("get_dead_relationship").
        getValue();
  }

  /**
   * Returns the dead {@link ContextRelationship} for the specified relationship id (<B>SERVER CALL</B>).
   * @param rel_id the <code>int</code> relationship id
   * @return the {@link ContextRelationship} for the specified relationship id
   * @throws MEMEException if failed to process get dead context relationship
   * @throws MissingDataException if failed due to missing data
   */
  public ContextRelationship getDeadContextRelationship(int rel_id) throws
      MEMEException, MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_dead_cxt_rel"));
    request.addParameter(new Parameter.Default("dead_cxt_rel", rel_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (ContextRelationship) request.getReturnValue("get_dead_cxt_rel").
        getValue();
  }

  /**
   * Returns a refreshed copy of the{@link Relationship} (<B>SERVER CALL</B>).
   * @param rel the {@link Relationship}
   * @return a refreshed copy of the {@link Relationship}
   * @throws MEMEException if failed to process get relationship
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship getRelationship(Relationship rel) throws MEMEException,
      MissingDataException {
    return getRelationship(rel.getIdentifier().intValue());
  }

  /**
   * Returns the {@link Relationship} for the specified relationship id (<B>SERVER CALL</B>).
   * @param rel_id the <code>int</code> rel id
   * @return the {@link Relationship} for the specified relationship id
   * @throws MEMEException if failed to process get relationship
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship getRelationship(int rel_id) throws MEMEException,
      MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_relationship"));
    request.addParameter(new Parameter.Default("relationship", rel_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Relationship) request.getReturnValue("get_relationship").getValue();
  }

  /**
   * Returns the {@link Relationship} for the specified relationship id (<B>SERVER CALL</B>).
   * @param rel_id the <code>int</code> rel id
   * @return the {@link Relationship} for the specified relationship id
   * @throws MEMEException if failed to process get inverse relationship
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship getInverseRelationship(int rel_id) throws MEMEException,
      MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_inverse_rel"));
    request.addParameter(new Parameter.Default("relationship", rel_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (Relationship) request.getReturnValue("get_inverse_rel").getValue();
  }

  /**
   * Returns all {@link Relationship}s within the specified range for the
   * specified {@link Concept} (<B>SERVER CALL</B>).
   * @param concept the {@link Concept}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @return all {@link Relationship}s within the specified range for the
   * specified {@link Concept}
   * @throws MEMEException if failed to process get relationships
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship[] getRelationships(Concept concept, int start, int end) throws
      MEMEException, MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "pop_rels"));
    request.addParameter(new Parameter.Default("start", start));
    request.addParameter(new Parameter.Default("end", end));
    int concept_id = concept.getIdentifier().intValue();
    request.addParameter(new Parameter.Default("concept_id", concept_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process request
    Relationship[] rels =
        (Relationship[]) request.getReturnValue("pop_rels").getValue();

    List list_rels = new ArrayList();
    for (int i = 0; i < rels.length; i++) {
      // Exclude all null rels
      if (rels[i] == null) {
        continue;
      }

      list_rels.add(rels[i]);
    }

    // Return
    return (Relationship[]) list_rels.toArray(new Relationship[0]);
  }

  /**
   * Returns all {@link Relationship}s for the specified {@link Concept} (<B>SERVER CALL</B>).
   * @param c the {@link Concept}
   * @return all {@link Relationship}s for the specified {@link Concept}
   * @throws MEMEException if failed to process get relationships
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship[] getRelationships(Concept c) throws MEMEException,
      MissingDataException {
    Concept c2 = new Concept.Default(c.getIdentifier().intValue());
    populateRelationships(c2);
    return c2.getRelationships();
  }

  /**
   * Returns the relationship count for the specified {@link Concept} (<B>SERVER CALL</B>).
   * @param concept the {@link Concept}
   * @return the relationship count for the specified {@link Concept}
   * @throws MEMEException if failed to get relationship count
   */
  public int getRelationshipCount(Concept concept) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_rels_count"));
    request.addParameter(new Parameter.Default("concept", concept));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (int) request.getReturnValue("get_rels_count").getInt();
  }

  /**
   * Returns the {@link ContextRelationship} for the specified context rel id (<B>SERVER CALL</B>).
   * @param cxt_rel_id the <code>int</code> rel id
   * @return the {@link ContextRelationship} for the specified context rel id
   * @throws MEMEException if failed to process get context relationship
   * @throws MissingDataException if failed due to missing data
   */
  public ContextRelationship getContextRelationship(int cxt_rel_id) throws
      MEMEException, MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_cxt_rel"));
    request.addParameter(new Parameter.Default("cxt_rel", cxt_rel_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process response
    return (ContextRelationship) request.getReturnValue("get_cxt_rel").getValue();
  }

  /**
   * Returns all {@link ContextRelationship}s within the specified range for
   * the specified {@link Concept} {<B>SERVER CALL</B>).
   * @param concept the {@link Concept}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @return all {@link ContextRelationship}s within the specified range for
   * the specified {@link Concept}
   * @throws MEMEException if failed to process get context relationships
   * @throws MissingDataException if failed due to missing data
   */
  public ContextRelationship[] getContextRelationships(Concept concept,
      int start, int end) throws MEMEException, MissingDataException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "pop_cxt_rels"));
    request.addParameter(new Parameter.Default("start", start));
    request.addParameter(new Parameter.Default("end", end));
    int concept_id = concept.getIdentifier().intValue();
    request.addParameter(new Parameter.Default("concept_id", concept_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof MissingDataException) {
        throw (MissingDataException) exceptions[0];
      } else {
        throw (MEMEException) exceptions[0];
      }
    }

    // Process request
    ContextRelationship[] cxt_rels =
        (ContextRelationship[]) request.getReturnValue("pop_cxt_rels").getValue();

    List list_cxt_rels = new ArrayList();
    for (int i = 0; i < cxt_rels.length; i++) {
      // Exclude all null context rels
      if (cxt_rels[i] == null) {
        continue;
      }

      list_cxt_rels.add(cxt_rels[i]);
    }

    // Return
    return (ContextRelationship[]) list_cxt_rels.toArray(new
        ContextRelationship[0]);
  }

  /**
   * Returns all {@link ContextRelationship}s for the specified {@link Concept} {<B>SERVER CALL</B>).
   * @param c the {@link Concept}
   * @return all {@link ContextRelationship}s for the specified {@link Concept}
   * @throws MEMEException if failed to process get context relationships
   * @throws MissingDataException if failed due to missing data
   */
  public ContextRelationship[] getContextRelationships(Concept c) throws
      MEMEException, MissingDataException {
    Concept c2 = new Concept.Default(c.getIdentifier().intValue());
    populateContextRelationships(c2);
    return c2.getContextRelationships();
  }

  /**
   * Returns the context relationship count for the specified {@link Concept} {<B>SERVER CALL</B>).
   * @param concept the {@link Concept}
   * @return the context relationship count for the specified {@link Concept}
   * @throws MEMEException if failed to get context relationship count
   */
  public int getContextRelationshipCount(Concept concept) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_cxt_rels_count"));
    request.addParameter(new Parameter.Default("concept", concept));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (int) request.getReturnValue("get_cxt_rels_count").getInt();
  }

  /**
   * Populates the {@link Concept} with relationships within the specified range {<B>SERVER CALL</B>).
   * @param concept the {@link Concept}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @throws MEMEException if failed to process populate relationships
   * @throws MissingDataException if failed due to missing data
   */
  public void populateRelationships(Concept concept, int start, int end) throws
      MEMEException, MissingDataException {

    // Get relationships from the specified range of concept
    Relationship[] rels = getRelationships(concept, start, end);

    Atom[] atoms = concept.getAtoms();
    Map map = new HashMap();
    for (int i = 0; i < atoms.length; i++) {
      map.put(atoms[i].getIdentifier(), atoms[i]);
    }
    for (int i = 0; i < rels.length; i++) {

      rels[i].setConcept(concept);
      if (rels[i].getRelatedConcept().getIdentifier().equals(concept.
          getIdentifier())) {
        rels[i].setRelatedConcept(concept);
      }
      concept.addRelationship(rels[i]);
      if (rels[i].isAtomLevel()) {
        Atom atom = (Atom) map.get(rels[i].getAtom().getIdentifier());
        if (atom != null) {
          rels[i].setAtom(atom);
          atom.addRelationship(rels[i]);
        }
        if (rels[i].getConcept().getIdentifier().equals(rels[i].
            getRelatedConcept().getIdentifier())) {
          atom = (Atom) map.get(rels[i].getRelatedAtom().getIdentifier());
          if (atom != null) {
            rels[i].setRelatedAtom(atom);
          }
        }
      }
    }

  }

  /**
   * Indicates whether the read languages are included or excluded.
   * @return <code>true</code>if include; <code>false</code>otherwise.
   */
  public boolean includeOrExcludeLanguages() {
    return include_or_exclude_lats;
  }

  /**
   * Returns selected languages when reading a concept.
   * @return selected languages.
   */
  public String[] getReadLanguages() {
    return selected_languages;
  }

  /**
   * Sets any languages to exclude when reading a concept.
   * @param lats languages to exclude.
   */
  public void setReadLanguagesToExclude(String[] lats) {
    this.selected_languages = lats;
    include_or_exclude_lats = false;
  }

  /**
   * Sets any languages to include when reading a concept.
   * @param lats languages to include.
   */
  public void setReadLanguagesToInclude(String[] lats) {
    this.selected_languages = lats;
    include_or_exclude_lats = true;
  }

  /**
   * Populates the {@link Concept} with all of its relationship {<B>SERVER CALL</B>).
   * @param concept the {@link Concept}
   * @throws MEMEException if failed to populate relationships
   */
  public void populateRelationships(Concept concept) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    int concept_id = concept.getIdentifier().intValue();
    request.addParameter(new Parameter.Default("function", "pop_rels"));
    request.addParameter(new Parameter.Default("concept_id", concept_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    Relationship[] rels =
        (Relationship[]) request.getReturnValue("pop_rels").getValue();

    concept.clearRelationships();
    Atom[] atoms = concept.getAtoms();
    Map map = new HashMap();
    for (int i = 0; i < atoms.length; i++) {
      atoms[i].clearRelationships();
      map.put(atoms[i].getIdentifier(), atoms[i]);
    }
    for (int i = 0; i < rels.length; i++) {

      rels[i].setConcept(concept);
      if (rels[i].getRelatedConcept().getIdentifier().equals(concept.
          getIdentifier())) {
        rels[i].setRelatedConcept(concept);
      }
      concept.addRelationship(rels[i]);
      if (rels[i].isAtomLevel()) {
        Atom atom = (Atom) map.get(rels[i].getAtom().getIdentifier());
        if (atom != null) {
          rels[i].setAtom(atom);
          atom.addRelationship(rels[i]);
        }
        if (rels[i].getConcept().getIdentifier().equals(rels[i].
            getRelatedConcept().getIdentifier())) {
          atom = (Atom) map.get(rels[i].getRelatedAtom().getIdentifier());
          if (atom != null) {
            rels[i].setRelatedAtom(atom);
          }
        }
      }
    }
  }

  /**
   * Populates the {@link Concept} with its {@link ContextRelationship}s within
   * the specified range {<B>SERVER CALL</B>).
   * @param concept the {@link Concept}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @throws MEMEException if failed to process populate context relationships
   * @throws MissingDataException if failed due to missing data
   */
  public void populateContextRelationships(Concept concept, int start, int end) throws
      MEMEException, MissingDataException {

    // Get context relationships from the specified range of concept
    ContextRelationship[] cxt_rels = getContextRelationships(concept, start,
        end);

    Atom[] atoms = concept.getAtoms();
    Map map = new HashMap();
    for (int i = 0; i < atoms.length; i++) {
      map.put(atoms[i].getIdentifier(), atoms[i]);
    }
    for (int i = 0; i < cxt_rels.length; i++) {

      cxt_rels[i].setConcept(concept);
      if (cxt_rels[i].getRelatedConcept().getIdentifier().equals(concept.
          getIdentifier())) {
        cxt_rels[i].setRelatedConcept(concept);
      }
      concept.addRelationship(cxt_rels[i]);
      if (cxt_rels[i].isAtomLevel()) {
        Atom atom = (Atom) map.get(cxt_rels[i].getAtom().getIdentifier());
        if (atom != null) {
          cxt_rels[i].setAtom(atom);
          atom.addRelationship(cxt_rels[i]);
        }
        if (cxt_rels[i].getConcept().getIdentifier().equals(cxt_rels[i].
            getRelatedConcept().getIdentifier())) {
          atom = (Atom) map.get(cxt_rels[i].getRelatedAtom().getIdentifier());
          if (atom != null) {
            cxt_rels[i].setRelatedAtom(atom);
          }
        }
      }
    }

  }

  /**
   * Populates the {@link Concept} with all of its {@link ContextRelationship}s {<B>SERVER CALL</B>).
   * @param concept An object {@link Concept}
   * @throws MEMEException if anything goes wrong
   */
  public void populateContextRelationships(Concept concept) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    int concept_id = concept.getIdentifier().intValue();
    request.addParameter(new Parameter.Default("function", "pop_cxt_rels"));
    request.addParameter(new Parameter.Default("concept_id", concept_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    ContextRelationship[] cxt_rels =
        (ContextRelationship[]) request.getReturnValue("pop_cxt_rels").getValue();

    concept.clearContextRelationships();
    Atom[] atoms = concept.getAtoms();
    Map map = new HashMap();
    for (int i = 0; i < atoms.length; i++) {
      atoms[i].clearContextRelationships();
      map.put(atoms[i].getIdentifier(), atoms[i]);
    }
    for (int i = 0; i < cxt_rels.length; i++) {

      cxt_rels[i].setConcept(concept);
      if (cxt_rels[i].getRelatedConcept().getIdentifier().equals(concept.
          getIdentifier())) {
        cxt_rels[i].setRelatedConcept(concept);
      }
      concept.addContextRelationship(cxt_rels[i]);
      if (cxt_rels[i].isAtomLevel()) {
        Atom atom = (Atom) map.get(cxt_rels[i].getAtom().getIdentifier());
        if (atom != null) {
          cxt_rels[i].setAtom(atom);
          atom.addContextRelationship(cxt_rels[i]);
        }
        if (cxt_rels[i].getConcept().getIdentifier().equals(cxt_rels[i].
            getRelatedConcept().getIdentifier())) {
          atom = (Atom) map.get(cxt_rels[i].getRelatedAtom().getIdentifier());
          if (atom != null) {
            cxt_rels[i].setRelatedAtom(atom);
          }
        }
      }
    }
  }

}
