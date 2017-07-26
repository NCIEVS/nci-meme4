/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MEMEConnection
 * Version Information
 * 10/30/2007 TTN (1-FN4GD): add paginated feature to getContentViewMembers method 
 * 07/31/2007 BAC (1-EUZVV): rel directionality flag is Y or null.
 * 04/17/2007 BAC (1-E0JWB): New getNullLui() method.
 * 03/06/2007 3.12.4 SL (1-DNO15) : Implementing the new interface findNDCConceptsFromCode to retrieve the NDC Code.
 * 03/02/2007 TTN (1-D3BQF): add SRC_REL_ID sg_type
 * 11/15/2006 BAC (1-CTLDV):  Change to atom rank to ensure SUI,AUI parts are always 9 digits
 * 08/31/2006 TTN(1-C261E): use the same ranking algorithm as in MEME_RANKS
 * 08/16/2006 BAC(1-BU75X): fix to cache clearing (sources,termgroups,rela)
 * 08/03/2006 BAC(1-BU75X): clear caches properly before re-cacheing.
 * 05/24/2006 RBE (1-BA55P) : Preventing certain errors from sending mail
 * 04/10/2006 TTN (1-AV6X1) : do not need to set keys map when using MultiMap
 * 02/27/2006 TTN (1-AHNAL) : add code and cascade field to content_view_members
 *                            fix add and remove cv_member to use the new columns
 * 02/03/2006 RBE (1-76X3H): support the additional parameter (set_ranks) in
 *  	MEME_SOURCE_PROCESSING.insert_ranks()
 * 02/03/2006 TTN (1-754X9) : Extend AUI to 8 chars. Pad AUI to fixed length
 *
 *****************************************************************************/

package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.AtomicChangeAtomAction;
import gov.nih.nlm.meme.action.AtomicChangeReleasabilityAction;
import gov.nih.nlm.meme.action.AtomicInsertAction;
import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.ATUI;
import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptMapping;
import gov.nih.nlm.meme.common.ContentView;
import gov.nih.nlm.meme.common.ContentViewMember;
import gov.nih.nlm.meme.common.ContextPath;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.ISUI;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.LUI;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.LoggedError;
import gov.nih.nlm.meme.common.MEMEString;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.common.MetaCode;
import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.RUI;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.common.Rankable;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SUI;
import gov.nih.nlm.meme.common.SafeReplacementFact;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.xml.ObjectXMLSerializer;
import gov.nih.nlm.util.MultiMap;
import oracle.sql.CLOB;

import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Generically represents a connection to a "MEME" database (MID or MRD).
 * @author MEME Group
 */

public abstract class MEMEConnection
    extends EnhancedConnection implements
    MEMEDataSource, MEMEConnectionQueries {

  //
  // Fields
  //

  private String db_service = null;
  private String data_source_name = null;
  private Object concept_lock = new Object();
  private static MultiMap authority_cache = null;
  private static MultiMap source_rank_cache = null;
  private static MultiMap sty_cache = null;
  private static MultiMap termgroup_rank_cache = null;
  private static MultiMap lat_cache = null;
  private static MultiMap code_map_cache = null;
  private static MultiMap meta_properties_cache = null;
  private static MultiMap inverse_rel_cache = null;
  private static MultiMap inverse_rela_cache = null;

  //
  // Static initializers for caches
  //
  static {
    authority_cache = new MultiMap();
    lat_cache = new MultiMap();
    source_rank_cache = new MultiMap();
    termgroup_rank_cache = new MultiMap();
    sty_cache = new MultiMap();
    inverse_rel_cache = new MultiMap();
    inverse_rela_cache = new MultiMap();
    code_map_cache = new MultiMap();
    meta_properties_cache = new MultiMap();
  }

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MEMEConnection} from the specified {@link Connection}.
   * @param conn the {@link Connection}
   * @throws DataSourceException if failed to load data source
   * @throws BadValueException if failed due to invalid data value
   * @throws ReflectionException if failed to load or instantiate class
   */
  public MEMEConnection(Connection conn) throws DataSourceException,
      BadValueException, ReflectionException {
    super(conn);
  }

  /**
   * Implements {@link MEMEDataSource#isCacheLoaded()}.
   * @return <code>true</code> if cache is loaded; <code>false</code> otherwise
   */
  public boolean isCacheLoaded() {
    return source_rank_cache.getMap(getDataSourceName()) != null;
  }

  /**
   * Implements {@link MEMEDataSource#refreshCaches()}.
   * @throws DataSourceException if failed to refresh caches.
   * @throws BadValueException if failed due to invalid data value.
   * @throws ReflectionException if failed to load or instantiate class.
   */
  public void refreshCaches() throws DataSourceException, BadValueException,
      ReflectionException {

    // Only cache data if it is null so that the same data will be
    // used for *all* meme connections

    cacheCodeMap();
    cacheMetaProperties();
    cacheLanguages();
    cacheRelaInverses(null);
    cacheRelInverses();
    cacheSemanticTypes();
    cacheSourceRank(null);
    cacheTermgroupRank(null);

  }

  /**
   * Implements {@link MEMEDataSource#getAuthority(String)}.
   * @param authority the {@link String}.
   * @return the {@link Authority}.
   */
  public Authority getAuthority(String authority) {
    if (authority == null) {
      return null;
    }
    Authority auth = null;
    // check cache first;
    if (authority_cache.containsKey(getDataSourceName(), authority)) {
      auth = (Authority) authority_cache.get(getDataSourceName(), authority);
    }
    else {
      auth = new Authority.Default(authority);
      authority_cache.put(getDataSourceName(), authority, auth);
    }
    return auth;
  }

  /**
   * Implements {@link MEMEDataSource#getAttribute(int,Ticket)}.
   * @param attr_id attribute id
   * @param ticket the {@link Ticket}.
   * @return the {@link Attribute}.
   * @throws DataSourceException if failed to load attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Attribute getAttribute(int attr_id, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    Attribute attr = null;
    boolean flag = false;

    if (ticket.mapDataType(Attribute.class)) {

      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(getAttributesQuery(ticket, Attribute.class));
        pstmt.setInt(1, attr_id);
        ResultSet rs = pstmt.executeQuery();

        // Create attribute object and assign it an id
        final AttributeMapper[] mappers = (AttributeMapper[])
            ticket.getDataMapper(Attribute.class).toArray(new AttributeMapper[0]);
        final AttributeMapper default_mapper = new AttributeMapper.Default();

        while (rs.next()) {
          for (int i = 0; i < mappers.length; i++) {
            attr = mappers[i].map(rs, this);
            if (attr != null) {
              break;
            }
          }
          // If attribute is still null, use default mapper
          if (attr == null) {
            attr = default_mapper.map(rs, this);

            // concept must be set
          }
          attr.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));
        }

        pstmt.close();
        flag = false;

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load attribute.", attr, se);
        dse.setDetail("query", getAttributesQuery(ticket, Attribute.class));
        dse.setDetail("attr_id", Integer.toString(attr_id));
        throw dse;
      }

    }
    else {
      attr = new Attribute.Default(attr_id);
      flag = true;
    }

    // Populate the attribute, ensure that
    // readAttributes toggle is off, but reset
    // it after the populate call.
    boolean read_attrs = ticket.readAttributes();
    ticket.setReadAttributes(flag);
    populateAttribute(attr, ticket);
    ticket.setReadAttributes(read_attrs);

    return attr;
  }

  /**
   * Implements {@link MEMEDataSource#getAttribute(ATUI, Ticket)}.
   * @param atui the {@link ATUI}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Attribute getAttribute(ATUI atui, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Query to get attribute by atui
    final String query = "SELECT attribute_id FROM attributes " +
        "WHERE atui = ? AND tobereleased IN ('Y','y')";

    int attr_id = 0;

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, atui.toString());
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        attr_id = rs.getInt(1);
      }

      // Close statement
      pstmt.close();

      if (attr_id == 0) {
        // No match found
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("atui", atui.toString());
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to look up attributes.", atui, se);
      dse.setDetail("query", query);
      dse.setDetail("atui", atui.toString());
      throw dse;
    }

    // Attribute id found
    return getAttribute(attr_id, ticket);

  }

  /**
   * Implements {@link MEMEDataSource#getRelationship(int,Ticket)}.
   * @param rel_id the relationship id
   * @param ticket the {@link Ticket}
   * @return the {@link Relationship}
   * @throws DataSourceException if failed to load relationship
   * @throws BadValueException if failed due to invalid data value
   */
  public Relationship getRelationship(int rel_id, Ticket ticket) throws
      DataSourceException, BadValueException {
    return getRelationship(rel_id, ticket, false);
  }

  /**
   * Implements {@link MEMEDataSource#getInverseRelationship(int,Ticket)}.
   * @param rel_id the relationship id.
   * @param ticket the {@link Ticket}
   * @return the {@link Relationship}
   * @throws DataSourceException if failed to load inverse relationship
   * @throws BadValueException if failed due to invalid data value
   */
  public Relationship getInverseRelationship(int rel_id, Ticket ticket) throws
      DataSourceException, BadValueException {
    return getRelationship(rel_id, ticket, true);
  }

  /**
   * Return the relationship or the inverse relationship.
   * @param rel_id the relationship id.
   * @param ticket the {@link Ticket}
   * @param inverse A <code>boolean</code> representation of inverse relationship.
   * @return the {@link Relationship}
   * @throws DataSourceException if failed to load relationship
   * @throws BadValueException if failed due to invalid data value
   */
  private Relationship getRelationship(int rel_id, Ticket ticket,
                                       boolean inverse) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    Relationship rel = null;
    boolean flag = false;
    if (ticket.mapDataType(Relationship.class)) {
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(getRelationshipsQuery(ticket, Relationship.class,
            inverse));
        pstmt.setInt(1, rel_id);
        final ResultSet rs = pstmt.executeQuery();

        // Create relationship object and assign it an id
        final RelationshipMapper[] mappers = (RelationshipMapper[])
            ticket.getDataMapper(Relationship.class).toArray(new
            RelationshipMapper[0]);
        final RelationshipMapper default_mapper = new RelationshipMapper.
            Default();

        while (rs.next()) {
          for (int i = 0; i < mappers.length; i++) {
            rel = mappers[i].map(rs, this);
            if (rel != null) {
              break;
            }
          }
          // If relationship is still null, use default mapper
          if (rel == null) {
            rel = default_mapper.map(rs, this);

            // concept must be set
          }
          rel.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));
        }

        pstmt.close();
        flag = false;

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load relationship.", rel, se);
        dse.setDetail("query", getRelationshipsQuery(ticket, Relationship.class, false));
        dse.setDetail("rel_id", Integer.toString(rel_id));
        throw dse;
      }

    }
    else {
      rel = new Relationship.Default(rel_id);
      flag = true;
    }

    // Populate the relationship, ensure that
    // readRelationships toggle is off, but reset
    // it after the populate call.
    boolean read_rels = ticket.readRelationships();
    ticket.setReadRelationships(flag);
    populateRelationship(rel, ticket, inverse);
    ticket.setReadRelationships(read_rels);

    return rel;
  }

  /**
   * Implements {@link MEMEDataSource#getRelationship(RUI)}.
   * @param rui the {@link RUI}.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Relationship getRelationship(RUI rui) throws DataSourceException,
      BadValueException {

    // Query to get context path by rui
    final String query = "SELECT relationship_id FROM relationships " +
        "WHERE rui = ? AND tobereleased IN ('Y','y')";

    int relationship_id = 0;

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, rui.toString());
      final ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        relationship_id = rs.getInt(1);

        // Close statement
      }
      pstmt.close();

      if (relationship_id == 0) {
        // No CUI match found
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("rui", rui.toString());
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to look up RUI.", rui, se);
      dse.setDetail("query", query);
      dse.setDetail("rui", rui.toString());
      throw dse;
    }

    // Relationship id found
    return getRelationship(relationship_id, null);

  }

  /**
   * Implements {@link MEMEDataSource#getRelationshipNames()}.
   * @return An array of object {@link String} representation of valid
   * relationship name.
   */
  public String[] getRelationshipNames() {
    return (String[]) inverse_rel_cache.values(getDataSourceName()).toArray(new
        String[0]);
  }

  /**
   * Adds the specified relationship attribute (and inverse).
   * @param rela the relationship attribute
   * @param inverse the inverse relationship attribute
   * @param rank the rank
   * @throws DataSourceException if failed to add relationship attribute
   */
  public void addRelationshipAttribute(String rela, String inverse, int rank) throws
      DataSourceException {

    final String[] relas = getRelationshipAttributes();

    // First, verify that it does not already exist.
    boolean found = false;
    for (int i = 0; i < relas.length; i++) {
      if (rela.equals(relas[i])) {
        found = true;
        break;
      }
    }

    // If it doesn't exist add it.
    if (!found) {
      final String insert_str = "INSERT INTO inverse_rel_attributes "
          + "VALUES (?, ?, ?)";

      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(insert_str);
        pstmt.setString(1, rela);
        pstmt.setString(2, inverse);
        pstmt.setInt(3, rank);
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert relationship attribute.", rela, se);
        dse.setDetail("rela", rela);
        dse.setDetail("inverse", inverse);
        dse.setDetail("rank", String.valueOf(rank));
        dse.setDetail("insert", insert_str);
        throw dse;
      }
    }

    // Handle inverse rela

    // First, verify that it does not already exist.
    found = false;
    for (int i = 0; i < relas.length; i++) {
      if (inverse.equals(relas[i])) {
        found = true;
        break;
      }
    }

    // If it doesn't exist add it.
    if (!found) {
      String insert_str = "INSERT INTO inverse_rel_attributes "
          + "VALUES (?, ?, ?)";

      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(insert_str);
        pstmt.setString(1, inverse);
        pstmt.setString(2, rela);
        pstmt.setInt(3, rank);
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert inverse relationship attribute.", inverse, se);
        dse.setDetail("rela", rela);
        dse.setDetail("inverse", inverse);
        dse.setDetail("rank", String.valueOf(rank));
        dse.setDetail("insert", insert_str);
        throw dse;
      }
    }

    // Recache inverses
    cacheRelaInverses(rela);
  }

  /**
   * Removes specified relationship attribute (and its inverse).
   * @param rela the relationship attribute
   * @throws DataSourceException if failed to remove relationship attribute
   */
  public void removeRelationshipAttribute(String rela) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.removeRelationshipAttribute(String, String)...");

    // Delete relationship attribute
    String delete_str =
        "DELETE FROM inverse_rel_attributes " +
        "WHERE relationship_attribute = ? ";
    String mode = "rela-inverse";

    PreparedStatement pstmt = null;
    for (int i = 0; i < 2; i++) {
      try {
        pstmt = prepareStatement(delete_str);
        pstmt.setString(1, rela);
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to delete relationship attribute.", rela, se);
        dse.setDetail("rela", rela);
        dse.setDetail("mode", mode);
        dse.setDetail("delete", delete_str);
        throw dse;
      }
      // Delete inverse relationship attribute
      delete_str =
          "DELETE FROM inverse_rel_attributes " +
          "WHERE inverse_rel_attribute = ? ";
      mode = "inverse-rela";
    }

    // Recache inverses
    cacheRelaInverses(rela);
  }

  /**
   * Adds the specified relationship name (and its inverse) with associated
   * additional information.
   * @param name the relationship name
   * @param inverse_name the relationship inverse name
   * @param weak the <code>boolean</code> represents weak flag
   * @param long_name the relationship long name
   * @param inverse_long_name the relationship inverse long name
   * @param release_name the relationship release name
   * @param inverse_release_name the relationship inverse release name
   * @throws DataSourceException if failed to add relationship name
   */
  public void addRelationshipName(String name, String inverse_name,
                                  boolean weak,
                                  String long_name, String inverse_long_name,
                                  String release_name,
                                  String inverse_release_name) throws
      DataSourceException {

    final String[] relas = getRelationshipNames();
    boolean found = false;

    // Handle rela

    // First, verify that it does not already exist.
    for (int i = 0; i < relas.length; i++) {
      if (name.equals(relas[i])) {
        found = true;
        break;
      }
    }

    // If it doesn't exist add it.
    if (!found) {
      String insert_str = "INSERT INTO inverse_relationships "
          + "VALUES (?, ?, ?, ?, ?, ?)";

      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(insert_str);
        pstmt.setString(1, name);
        pstmt.setString(2, inverse_name);
        pstmt.setString(3, weak ? "Y" : "N");
        pstmt.setString(4, long_name);
        pstmt.setString(5, release_name);
        pstmt.setInt(6, 1);
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert relationship name.", name, se);
        dse.setDetail("name", name);
        dse.setDetail("inverse_name", inverse_name);
        dse.setDetail("weak", weak ? "Y" : "N");
        dse.setDetail("long_name", long_name);
        dse.setDetail("release_name", release_name);
        dse.setDetail("insert", insert_str);
        throw dse;
      }
    }

    // Handle inverse rela

    // First, verify that it does not already exist.
    found = false;
    for (int i = 0; i < relas.length; i++) {
      if (inverse_name.equals(relas[i])) {
        found = true;
        break;
      }
    }

    // If it doesn't exist add it.
    if (!found) {
      String insert_str = "INSERT INTO inverse_relationships "
          + "VALUES (?, ?, ?, ?, ?, ?)";

      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(insert_str);
        pstmt.setString(1, inverse_name);
        pstmt.setString(2, name);
        pstmt.setString(3, weak ? "Y" : "N");
        pstmt.setString(4, inverse_long_name);
        pstmt.setString(5, inverse_release_name);
        pstmt.setInt(6, 1);
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert inverse relationship name.", inverse_name, se);
        dse.setDetail("name", name);
        dse.setDetail("inverse_name", inverse_name);
        dse.setDetail("weak", weak ? "Y" : "N");
        dse.setDetail("long_name", inverse_long_name);
        dse.setDetail("release_name", inverse_release_name);
        dse.setDetail("insert", insert_str);
        throw dse;
      }
    }

    // Recache inverses
    cacheRelInverses();

  }

  /**
   * Removes the specified relationship and its inverse.
   * @param name the relationship name
   * @throws DataSourceException if failed to remove relationship name.
   */
  public void removeRelationshipName(String name) throws DataSourceException {
    // Delete relationship relationship
    String delete_str =
        "DELETE FROM inverse_relationships " +
        "WHERE relationship_name = ? ";
    String mode = "rela-inverse";

    PreparedStatement pstmt = null;
    for (int i = 0; i < 2; i++) {
      try {
        pstmt = prepareStatement(delete_str);
        pstmt.setString(1, name);
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to delete relationship name.", name, se);
        dse.setDetail("rela", name);
        dse.setDetail("mode", mode);
        dse.setDetail("delete", delete_str);
        throw dse;
      }
      // Delete inverse relationship attribute
      delete_str =
          "DELETE FROM inverse_relationships " +
          "WHERE inverse_name = ? ";
      mode = "inverse-rela";
    }

    // Recache inverses
    cacheRelInverses();
  }

  /**
   * Implements {@link MEMEDataSource#getRelationshipAttributes()}.
   * @return An array of object {@link String} representation of valid
   * relationship attribute.
   */
  public String[] getRelationshipAttributes() {
    String[] relas = (String[]) inverse_rela_cache.values(getDataSourceName()).
        toArray(new String[0]);
    return relas;
  }

  /**
   * Implements {@link MEMEDataSource#getInverseRelationshipName(String)}.
   * @param rel the {@link String} representation of relationship name.
   * @return the {@link String} representation of the inverse of the
   * relationship name.
   * @throws BadValueException if failed due to invalid data value.
   */
  public String getInverseRelationshipName(String rel) throws BadValueException {
    if (rel == null || rel.equals("")) {
      return rel;
    }
    final String inv_rel = (String) inverse_rel_cache.get(getDataSourceName(),
        rel);
    if (inv_rel == null) {
      BadValueException bve = new BadValueException(
          "Illegal relationship name value.");
      bve.setDetail("relationship_name", rel);
      throw bve;
    }
    return inv_rel;
  }

  /**
   * Implements {@link MEMEDataSource#getInverseRelationshipAttribute(String)}.
   * @param rela the {@link String} representation of relationship attribute.
   * @return the {@link String} representation of the inverse of the
   * relationship attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public String getInverseRelationshipAttribute(String rela) throws
      BadValueException {

    if (rela == null || rela.equals("")) {
      return rela;
    }

    final String inv_rela = (String) inverse_rela_cache.get(getDataSourceName(),
        rela);

    if (inv_rela == null) {
      BadValueException bve = new BadValueException(
          "Illegal relationship attribute value.");
      bve.setDetail("relationship_attribute", rela);
      throw bve;
    }
    return inv_rela;
  }

  /**
   * Implements {@link MEMEDataSource#getRelationshipCount(Concept)}.
   * @param concept the {@link Concept}.
   * @return the relationships count.
   * @throws DataSourceException if failed to count relationship.
   */
  public int getRelationshipCount(Concept concept) throws DataSourceException {

    PreparedStatement pstmt = null;
    String query = READ_RELATIONSHIP_COUNT;
    int concept_id = concept.getIdentifier().intValue();

    try {

      // Prepare and execute query
      pstmt = prepareStatement(query);
      pstmt.setInt(1, concept_id);
      pstmt.setInt(2, concept_id);
      ResultSet rs = pstmt.executeQuery();

      // Read
      int count = 0;
      while (rs.next()) {
        count = rs.getInt("COUNT");
      }

      // Close statement
      pstmt.close();
      return count;

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to count relationship.", concept, se);
      dse.setDetail("query", query);
      dse.setDetail("concept_id", Integer.toString(concept_id));
      throw dse;
    }
  }

  /**
   * Determines the query used to read context relationship.
   * @param ticket the {@link Ticket}.
   * @param type the {@link Class}.
   * @param inverse returns <code>true</code> if inverse relationships;
   *        <code>false</code> otherwise.
   * @return the query.
   */
  private String getContextRelationshipsQuery(
      Ticket ticket, Class type, boolean inverse) {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    String data_type_query = ticket.getDataTypeQuery(ContextRelationship.class);
    if (type == Concept.class) {
      if (!inverse) {
        return READ_CONTEXT_RELATIONSHIPS_1 +
            ticket.getDataTypeRestriction(ContextRelationship.class) +
            ticket.getDataTypeOrderBy(ContextRelationship.class);
      }
      else {
        return READ_CONTEXT_RELATIONSHIPS_2 +
            ticket.getDataTypeRestriction(ContextRelationship.class) +
            ticket.getDataTypeOrderBy(ContextRelationship.class);
      }
    }
    else if (type == Atom.class) {
      if (!inverse) {
        return READ_ATOM_CONTEXT_RELATIONSHIPS_1 +
            ticket.getDataTypeRestriction(ContextRelationship.class) +
            ticket.getDataTypeOrderBy(ContextRelationship.class);
      }
      else {
        return READ_ATOM_CONTEXT_RELATIONSHIPS_2 +
            ticket.getDataTypeRestriction(ContextRelationship.class) +
            ticket.getDataTypeOrderBy(ContextRelationship.class);
      }
    }
    else if (data_type_query != null) {
      return data_type_query;
    }
    else {
      return READ_CONTEXT_RELATIONSHIP_COLUMNS +
          (ticket.readDeleted() ? "dead_" : "") +
          ticket.getDataTable(ContextRelationship.class) +
          READ_CONTEXT_RELATIONSHIP_CONDITIONS +
          ticket.getDataTypeRestriction(ContextRelationship.class) +
          ticket.getDataTypeOrderBy(ContextRelationship.class);
    }
  }

  /**
   * Implements {@link MEMEDataSource#getContextRelationship(int,Ticket)}.
   * @param rel_id the relationship id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load context relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextRelationship getContextRelationship(int rel_id, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    ContextRelationship cxt_rel = null;
    boolean flag = false;

    if (ticket.mapDataType(ContextRelationship.class)) {
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(getContextRelationshipsQuery(ticket,
            ContextRelationship.class, false));
        pstmt.setInt(1, rel_id);
        ResultSet rs = pstmt.executeQuery();
        // Create context relationship object and assign it an id
        RelationshipMapper[] mappers = (RelationshipMapper[])
            (ticket.getDataMapper(ContextRelationship.class)).toArray(new
            RelationshipMapper[0]);
        RelationshipMapper default_mapper = new ContextRelationshipMapper(true);

        while (rs.next()) {
          for (int i = 0; i < mappers.length; i++) {
            cxt_rel = (ContextRelationship) mappers[i].map(rs, this);
            if (cxt_rel != null) {
              break;
            }
          }
          // If context relationship is still null, use default mapper
          if (cxt_rel == null) {
            cxt_rel = (ContextRelationship) default_mapper.map(rs, this);

            // concept must be set
          }
          cxt_rel.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));
        }

        pstmt.close();
        flag = false;

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load context relationship.", cxt_rel, se);
        dse.setDetail("query",
                      getContextRelationshipsQuery(ticket, ContextRelationship.class, false));
        dse.setDetail("rel_id", Integer.toString(rel_id));
        throw dse;
      }

    }
    else {
      cxt_rel = new ContextRelationship.Default(rel_id);
      flag = true;
    }

    // Populate the context relationship, ensure that
    // readRelationships toggle is off, but reset
    // it after the populate call.
    boolean read_cxt_rels = ticket.readContextRelationships();
    ticket.setReadContextRelationships(flag);
    populateContextRelationship(cxt_rel, ticket);
    ticket.setReadContextRelationships(read_cxt_rels);

    return cxt_rel;
  }

  /**
   * Implements {@link MEMEDataSource#getContextRelationship(RUI)}.
   * @param rui the {@link RUI}.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load context relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextRelationship getContextRelationship(RUI rui) throws
      DataSourceException, BadValueException {

    // Query to get context relationship by rui
    final String query = "SELECT relationship_id FROM context_relationships " +
        "WHERE rui = ? AND tobereleased IN ('Y','y')";

    int relationship_id = 0;

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, rui.toString());
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        relationship_id = rs.getInt(1);
      }

      // Close statement
      pstmt.close();

      if (relationship_id == 0) {
        // No match found
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("rui", rui.toString());
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to look up relationship.", rui, se);
      dse.setDetail("query", query);
      dse.setDetail("rui", rui.toString());
      throw dse;
    }

    // Relationship id found
    return getContextRelationship(relationship_id, null);

  }

  /**
   * Implements {@link MEMEDataSource#getContextRelationshipCount(Concept)}.
   * @param concept the {@link Concept}.
   * @return the relationships count.
   * @throws DataSourceException if failed to count context relationship.
   */
  public int getContextRelationshipCount(Concept concept) throws
      DataSourceException {

    PreparedStatement pstmt = null;
    String query = READ_CONTEXT_RELATIONSHIP_COUNT;
    int concept_id = concept.getIdentifier().intValue();

    try {

      // Prepare and execute query
      pstmt = prepareStatement(query);
      pstmt.setInt(1, concept_id);
      pstmt.setInt(2, concept_id);
      ResultSet rs = pstmt.executeQuery();

      // Read
      int count = 0;
      while (rs.next()) {
        count = rs.getInt("COUNT");
      }

      // Close statement
      pstmt.close();
      return count;

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to count context relationship.", concept, se);
      dse.setDetail("query", query);
      dse.setDetail("concept_id", Integer.toString(concept_id));
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getContextPath(int)}.
   * @param relationship_id the relationship id.
   * @return the {@link ContextPath}.
   * @throws DataSourceException if failed to get context path.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextPath getContextPath(int relationship_id) throws
      DataSourceException, BadValueException {

    PreparedStatement pstmt = null;
    String query = READ_CONTEXT_PATH;

    ContextPath context_path = null;
    ContextPathMapper mapper = null;
    try {

      // Prepare and execute query
      pstmt = prepareStatement(query);
      pstmt.setInt(1, relationship_id);
      ResultSet rs = pstmt.executeQuery();

      // Create object and assign it an id
      context_path = new ContextPath.Default();
      mapper = new ContextPathMapper();
      while (rs.next()) {
        Atom atom = mapper.map(rs, this);
        context_path.addAtom(atom);
      }

      // Close statement
      pstmt.close();
      return context_path;

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to read context path.", context_path, se);
      dse.setDetail("query", query);
      dse.setDetail("relationship_id", Integer.toString(relationship_id));
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getContextPath(RUI)}.
   * @param rui the {@link RUI}.
   * @return the {@link ContextPath}.
   * @throws DataSourceException if failed to look up context path.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextPath getContextPath(RUI rui) throws DataSourceException,
      BadValueException {

    // Query to get context path by rui
    final String query = "SELECT relationship_id FROM context_relationships " +
        "WHERE rui = ? AND tobereleased IN ('Y','y')";

    int relationship_id = 0;

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, rui.toString());
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        relationship_id = rs.getInt(1);
      }

      // Close statement
      pstmt.close();

      if (relationship_id == 0) {
        // No CUI match found
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("rui", rui.toString());
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to look up RUI.", rui, se);
      dse.setDetail("query", query);
      dse.setDetail("rui", rui.toString());
      throw dse;
    }

    // Relationship id found
    return getContextPath(relationship_id);
  }

  /**
   * Implements {@link MEMEDataSource#addSources(Source[])}
   * @param sources An array of object {@link Source}
   * @throws DataSourceException if failed to add sources
   */
  public void addSources(Source[] sources) throws DataSourceException {

    MEMEToolkit.trace("MEMEConnection.addSources(Source[])...");

    // Truncate source_source_rank

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareCall("{call MEME_SYSTEM.truncate('source_source_rank')}");
      pstmt.execute();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (SQLException se2) {}
      DataSourceException dse = new DataSourceException(
          "Failed to truncate source_source_rank.", sources, se);
      throw dse;
    }

    try {
      pstmt = prepareCall(
          "{call MEME_SYSTEM.truncate('source_termgroup_rank')}");
      pstmt.execute();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (SQLException se2) {}
      DataSourceException dse = new DataSourceException(
          "Failed to truncate source_termgroup_rank.", sources, se);
      throw dse;
    }

    // Insert into source_source_rank
    String insert_str =
        "INSERT INTO source_source_rank (high_source, low_source," +
        " restriction_level, normalized_source, stripped_source," +
        " source_official_name, source_family, version, valid_start_date," +
        " valid_end_date, insert_meta_version, remove_meta_version, nlm_contact," +
        " inverter_contact, acquisition_contact, content_contact, license_contact," +
        " release_url_list, context_type, language, citation, license_info," +
        " character_set, rel_directionality_flag, rank)" +
        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    for (int i = 0; i < sources.length; i++) {
      pstmt = null;
      try {
        pstmt = prepareStatement(insert_str);
        pstmt.setString(1, sources[i].getSourceAbbreviation());
        pstmt.setString(2,
                        sources[i].getSourceToOutrank().getSourceAbbreviation());
        pstmt.setString(3, sources[i].getRestrictionLevel());
        pstmt.setString(4, sources[i].getNormalizedSourceAbbreviation());
        pstmt.setString(5, sources[i].getStrippedSourceAbbreviation());
        pstmt.setString(6, sources[i].getOfficialName());
        pstmt.setString(7, sources[i].getSourceFamilyAbbreviation());
        pstmt.setString(8, sources[i].getSourceVersion());
        pstmt.setDate(9,
                      sources[i].getInsertionDate() == null ? null :
                      new java.sql.Date(sources[i].getInsertionDate().getTime()));
        pstmt.setDate(10,
                      sources[i].getExpirationDate() == null ? null :
                      new java.sql.Date(sources[i].getExpirationDate().getTime()));
        pstmt.setString(11, sources[i].getInsertMetaVersion());
        pstmt.setString(12, sources[i].getRemoveMetaVersion());
        pstmt.setString(13, sources[i].getNLMContact());
        pstmt.setString(14, sources[i].getInverter());
        pstmt.setString(15, sources[i].getAcquisitionContact());
        pstmt.setString(16, sources[i].getContentContact());
        pstmt.setString(17, sources[i].getLicenseContact());
        pstmt.setString(18, null);
        pstmt.setString(19, sources[i].getContextType());
        pstmt.setString(20, sources[i].getLanguage().getAbbreviation());
        pstmt.setString(21, sources[i].getCitation());
        pstmt.setString(22, null);
        pstmt.setString(23, sources[i].getCharacterEncoding());
        pstmt.setString(24, null);
        pstmt.setString(25, sources[i].getRank().toString());
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert row to source_source_rank.", sources, se);
        dse.setDetail("insert", insert_str);
        throw dse;
      }
    }

    // Insert ranks
    pstmt = null;
    try {
      pstmt = prepareCall("{call MEME_SOURCE_PROCESSING.insert_ranks('" +
              sources[0].getSourceAbbreviation() + "', 0, set_ranks => 'N')}");
      pstmt.execute();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (SQLException se2) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert ranks", sources, se);
      throw dse;
    }

    // Update the cache
    cacheSourceRank(sources[0].getSourceAbbreviation());

  }

  /**
   * Implements {@link MEMEDataSource#addSource(Source)}
   * @param source the {@link Source}
   * @throws DataSourceException if failed to add source
   */
  public void addSource(Source source) throws DataSourceException {
    MEMEToolkit.trace("MEMEConnection.addSource(Source)...");
    addSources(new Source[] {source});
    setSource(source);
  }

  /**
   * Implements {@link MEMEDataSource#removeSource(Source)}
   * @param source the {@link Source}
   * @throws DataSourceException if failed to remove source
   */
  public void removeSource(Source source) throws DataSourceException {

    MEMEToolkit.trace("MEMEConnection.removeSource(Source)...");

    // Remove from source_rank
    String remove_str = "DELETE FROM source_rank WHERE source = ?";
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.setString(1, source.getSourceAbbreviation());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from source.", source, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

    // Remove from sims_info
    remove_str = "DELETE FROM sims_info WHERE source = ?";
    pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.setString(1, source.getSourceAbbreviation());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from sims_info.", source, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

    // Update source_version
    String update_str =
        "UPDATE source_version SET current_name = ? WHERE source = ?";
    pstmt = null;
    try {
      pstmt = prepareStatement(update_str);
      pstmt.setString(1, source.getSourceAbbreviation());
      pstmt.setString(2, source.getSourceVersion());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to update source_version.", source, se);
      dse.setDetail("update", update_str);
      throw dse;
    }

    // Update the cache
    cacheSourceRank(source.getSourceAbbreviation());

  }

  /**
   * Implements {@link MIDDataSource#setSource(Source)}.
   * @param source the {@link Source}
   * @throws DataSourceException if failed to set source
   */
  public void setSource(Source source) throws DataSourceException {

    MEMEToolkit.trace("MIDConnection.setSource(Source)...");

    //
    // Update source rank
    //

    String update_source = "UPDATE source_rank"
        + " SET source_family = ?, version = ?, restriction_level = ?"
        + " WHERE source = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_source);
      pstmt.setString(1, source.getSourceFamilyAbbreviation());
      pstmt.setString(2, source.getSourceVersion());
      pstmt.setInt(3, Integer.valueOf(source.getRestrictionLevel()).intValue());
      pstmt.setString(4, source.getSourceAbbreviation());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
    }
    catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to set the source.", source, se);
      dse.setDetail("update", update_source);
      dse.setDetail("source", source.getSourceAbbreviation());
      throw dse;
    }

    //
    // Update sims info
    //

    String update_sims_info = "UPDATE sims_info"
        + " SET date_created = ?, meta_year = ?, init_rcpt_date = ?,"
        + " clean_rcpt_date = ?, test_insert_date = ?, real_insert_date = ?,"
        + " source_contact = ?, inverter_contact = ?, nlm_path = ?,"
        + " apelon_path = ?, inversion_script = ?, inverter_notes_file = ?,"
        + " conserve_file = ?, sab_list = ?, meow_display_name = ?,"
        + " source_desc = ?, status = ?, worklist_sortkey_loc = ?,"
        + " termgroup_list = ?, attribute_list = ?, inversion_notes = ?,"
        + " notes = ?, inv_recipe_loc = ?, suppress_edit_rec = ?,"
        + " versioned_cui = ?, root_cui = ?, source_official_name = ?,"
        +
        " source_short_name = ?, attribute_name_list = ?, term_type_list = ?,"
        + " term_frequency = ?, cui_frequency = ?, citation = ?,"
        + " last_contacted = ?, license_info = ?, character_set = ?,"
        + " valid_start_date = ?, valid_end_date = ?, insert_meta_version = ?,"
        + " remove_meta_version = ?, nlm_contact = ?, acquisition_contact = ?,"
        + " content_contact = ?, license_contact = ?, context_type = ?,"
        + " language = ?, test_insertion_start = ?, test_insertion_end = ?,"
        +
        " real_insertion_start = ?, real_insertion_end = ?, editing_start = ?,"
        + " editing_end = ?, latest_available = ?, release_url_list = ?,"
        + " internal_url_list = ?, rel_directionality_flag = ?"
        + " WHERE source = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_sims_info);
      pstmt.setDate(1,
                    source.getDateCreated() == null ? null :
                    new java.sql.Date(source.getDateCreated().getTime()));
      pstmt.setInt(2, source.getMetaYear());
      pstmt.setDate(3,
                    source.getInitialReceiptDate() == null ? null :
                    new java.sql.Date(source.getInitialReceiptDate().getTime()));
      pstmt.setDate(4,
                    source.getCleanReceiptDate() == null ? null :
                    new java.sql.Date(source.getCleanReceiptDate().getTime()));
      pstmt.setDate(5,
                    source.getTestInsertionDate() == null ? null :
                    new java.sql.Date(source.getTestInsertionDate().getTime()));
      pstmt.setDate(6,
                    source.getRealInsertionDate() == null ? null :
                    new java.sql.Date(source.getRealInsertionDate().getTime()));
      pstmt.setString(7, source.getSourceContact());
      pstmt.setString(8, source.getInverterContact());
      pstmt.setString(9, source.getNLMPath());
      pstmt.setString(10, source.getApelonPath());
      pstmt.setString(11, source.getInversionScript());
      pstmt.setString(12, source.getInverterNotesFile());
      pstmt.setString(13, source.getConserveFile());
      pstmt.setString(14, source.getSABList());
      pstmt.setString(15, source.getMeowDisplayName());
      pstmt.setString(16, source.getSourceDescription());
      pstmt.setString(17, source.getStatus());
      pstmt.setString(18, source.getWorklistSortkeyLocation());
      pstmt.setString(19, source.getTermGroupList());
      pstmt.setString(20, source.getAttributeList());
      pstmt.setString(21, source.getInversionNotes());
      pstmt.setString(22, source.getNotes());
      pstmt.setString(23, source.getInverseRecipeLocation());
      pstmt.setString(24, source.getSuppressibleEditableRecord() ? "Y" : "N");
      pstmt.setString(25, String.valueOf(source.getVersionedCui()));
      pstmt.setString(26, String.valueOf(source.getRootCui()));
      pstmt.setString(27, source.getOfficialName());
      pstmt.setString(28, source.getShortName());
      pstmt.setString(29, source.getAttributeNameList());
      pstmt.setString(30, source.getTermTypeList());
      pstmt.setInt(31, source.getTermFrequency());
      pstmt.setInt(32, source.getCuiFrequency());
      pstmt.setString(33, source.getCitation());
      pstmt.setDate(34,
                    source.getLastContactedDate() == null ? null :
                    new java.sql.Date(source.getLastContactedDate().getTime()));
      pstmt.setString(35, source.getLicenseInformation());
      pstmt.setString(36, source.getCharacterEncoding());
      pstmt.setDate(37,
                    source.getInsertionDate() == null ? null :
                    new java.sql.Date(source.getInsertionDate().getTime()));
      pstmt.setDate(38,
                    source.getExpirationDate() == null ? null :
                    new java.sql.Date(source.getExpirationDate().getTime()));
      pstmt.setString(39, source.getInsertMetaVersion());
      pstmt.setString(40, source.getRemoveMetaVersion());
      pstmt.setString(41, source.getNLMContact());
      pstmt.setString(42, source.getAcquisitionContact());
      pstmt.setString(43, source.getContentContact());
      pstmt.setString(44, source.getLicenseContact());
      pstmt.setString(45, source.getContextType());
      pstmt.setString(46, String.valueOf(source.getLanguage().getAbbreviation()));
      pstmt.setDate(47,
                    source.getTestInsertionStartDate() == null ? null :
                    new java.
                    sql.Date(source.getTestInsertionStartDate().getTime()));
      pstmt.setDate(48,
                    source.getTestInsertionEndDate() == null ? null :
                    new java.sql.Date(source.getTestInsertionEndDate().getTime()));
      pstmt.setDate(49,
                    source.getRealInsertionStartDate() == null ? null :
                    new java.
                    sql.Date(source.getRealInsertionStartDate().getTime()));
      pstmt.setDate(50,
                    source.getRealInsertionEndDate() == null ? null :
                    new java.sql.Date(source.getRealInsertionEndDate().getTime()));
      pstmt.setDate(51,
                    source.getEditingStartDate() == null ? null :
                    new java.sql.Date(source.getEditingStartDate().getTime()));
      pstmt.setDate(52,
                    source.getEditingEndDate() == null ? null :
                    new java.sql.Date(source.getEditingEndDate().getTime()));
      pstmt.setString(53, source.getLatestAvailable());
      pstmt.setString(54, source.getReleaseUrlList());
      pstmt.setString(55, source.getInternalUrlList());
      pstmt.setString(56,
                      source.getRelationshipDirectionalityFlag() ? "Y" : null);
      pstmt.setString(57, source.getSourceAbbreviation());

      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
    }
    catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to set the sims info.", source, se);
      dse.setDetail("update", update_sims_info);
      dse.setDetail("source", source.getSourceAbbreviation());
      throw dse;
    }

    //
    // Fix official name (SRC/VPT)
    //

    Iterator iterator = findAtoms(
        new SearchParameter[] {
        new SearchParameter.Single("code", "V-" + source.getSourceAbbreviation()),
        new SearchParameter.Single("termgroup", "SRC/VPT"),
        new SearchParameter.Single("tobereleased", "Y")}
        , Ticket.getActionsTicket());

    Atom official_name = null;
    while (iterator.hasNext()) {
      official_name = (Atom) iterator.next();
      break;
    }

    if (official_name != null &&
        !official_name.toString().equals(source.getOfficialName())) {
      // Save reference to the old atom and initializes new atom
      Atom old_atom = new Atom.Default(official_name.getIdentifier().intValue());
      try {
        populateAtom(old_atom, Ticket.getActionsTicket());
      }
      catch (MEMEException me) {
        throw new DataSourceException("Failed to populate atom.", old_atom, me);
      }
      Atom new_atom = official_name;
      new_atom.setIdentifier(null);
      new_atom.setRank(null);
      new_atom.setString(source.getOfficialName());

      MolecularAction ma = new MolecularAction();
      ma.setActionName("MOLECULAR_CHANGE_OFFICIAL_NAME");

      ma.setTransactionIdentifier(getNextIdentifierForType(MolecularTransaction.class));
      ma.setWorkIdentifier(new Identifier.Default(0));
      ma.setIsImplied(true);
      ma.setSource(new_atom.getConcept());
      ma.setAuthority(new Authority.Default("MTH"));

      // 1. Add new VPT atom
      AtomicInsertAction aiaa = new AtomicInsertAction(new_atom);
      ma.addSubAction(aiaa);

      // 2. Move attributes from old to new atom
      Attribute[] attrs = old_atom.getAttributes();
      for (int i = 0; i < attrs.length; i++) {
        AtomicChangeAtomAction acaa = new AtomicChangeAtomAction(attrs[i]);
        acaa.setOldAtom(old_atom);
        acaa.setNewAtom(new_atom);
        ma.addSubAction(acaa);
      }

      // 3. Move relationships from old to new atom
      Relationship[] rels = old_atom.getRelationships();
      for (int i = 0; i < rels.length; i++) {
        AtomicChangeAtomAction acaa = new AtomicChangeAtomAction(rels[i]);
        acaa.setOldAtom(old_atom);
        acaa.setNewAtom(new_atom);
        ma.addSubAction(acaa);
      }

      // 4. Make old VPT atom unreleasable
      AtomicChangeReleasabilityAction acra = new
          AtomicChangeReleasabilityAction(old_atom);
      acra.setNewValue("N");
      ma.addSubAction(acra);

      try {
        getActionEngine().processAction(ma);
      }
      catch (MEMEException me) {
        throw new DataSourceException(
            "Failed to process molecular action.", ma, me);
      }

    }

    //
    // Fix source short name (SRC/SSN)
    //

    iterator = findAtoms(
        new SearchParameter[] {
        new SearchParameter.Single("code",
                                   "V-" + source.getRootSourceAbbreviation()),
        new SearchParameter.Single("termgroup", "SRC/SSN"),
        new SearchParameter.Single("tobereleased", "Y")}
        , Ticket.getActionsTicket());

    Atom short_name = null;
    while (iterator.hasNext()) {
      short_name = (Atom) iterator.next();
      break;
    }

    if (short_name != null &&
        !short_name.toString().equals(source.getShortName())) {
      // Save reference to the old atom and initializes new atom
      Atom old_atom = new Atom.Default(short_name.getIdentifier().intValue());
      try {
        populateAtom(old_atom, Ticket.getActionsTicket());
      }
      catch (MEMEException me) {
        throw new DataSourceException("Failed to populate atom.", old_atom, me);
      }
      Atom new_atom = short_name;
      new_atom.setIdentifier(null);
      new_atom.setRank(null);
      new_atom.setString(source.getShortName());

      MolecularAction ma = new MolecularAction();
      ma.setActionName("MOLECULAR_CHANGE_SHORT_NAME");

      ma.setWorkIdentifier(getNextIdentifierForType(MolecularTransaction.class));
      ma.setIsImplied(true);
      ma.setSource(new_atom.getConcept());
      ma.setAuthority(new Authority.Default("MTH"));

      // 1. Add new SSN atom
      AtomicInsertAction aiaa = new AtomicInsertAction(new_atom);
      ma.addSubAction(aiaa);

      // 2. Move attributes from old to new atom
      Attribute[] attrs = old_atom.getAttributes();
      for (int i = 0; i < attrs.length; i++) {
        AtomicChangeAtomAction acaa = new AtomicChangeAtomAction(attrs[i]);
        acaa.setOldAtom(old_atom);
        acaa.setNewAtom(new_atom);
        ma.addSubAction(acaa);
      }

      // 3. Move relationships from old to new atom
      Relationship[] rels = old_atom.getRelationships();
      for (int i = 0; i < rels.length; i++) {
        AtomicChangeAtomAction acaa = new AtomicChangeAtomAction(rels[i]);
        acaa.setOldAtom(old_atom);
        acaa.setNewAtom(new_atom);
        ma.addSubAction(acaa);
      }

      // 4. Make old SSN atom unreleasable
      AtomicChangeReleasabilityAction acra = new
          AtomicChangeReleasabilityAction(old_atom);
      acra.setNewValue("N");
      ma.addSubAction(acra);

      try {
        getActionEngine().processAction(ma);
      }
      catch (MEMEException me) {
        throw new DataSourceException(
            "Failed to process molecular action.", ma, me);
      }

    }

    //
    // Update cache
    //
    cacheSourceRank(source.getSourceAbbreviation());

  }

  /**
   * Implements {@link MEMEDataSource#getSource(String)}.
   * @param source_name the {@link String} representation of source name.
   * @return the {@link Source}.
   * @throws DataSourceException if failed to get source.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Source getSource(String source_name) throws DataSourceException,
      BadValueException {

    // Convert ENG- sources to the base source name
    String l_source = null;
    if (source_name.startsWith("ENG-")) {
      l_source = source_name.substring(4);
    }
    else {
      l_source = source_name;

      // Look up the source
    }
    Source source = (Source) source_rank_cache.get(getDataSourceName(),
        l_source);

    // If its not in the cache, look it up again
    if (source == null) {
      cacheSourceRank(source_name);
      source = (Source) source_rank_cache.get(getDataSourceName(), l_source);
    }

    // If its still not there, it is illegal
    if (source == null) {
      BadValueException bve = new BadValueException("Illegal source value.");
      bve.setDetail("source", source_name);
      throw bve;
    }

    return source;
  }

  /**
   * Implements {@link MEMEDataSource#getSources()}.
   * @return An array of object {@link Source}.
   */
  public Source[] getSources() {
    Source[] sources = (Source[]) source_rank_cache.values(getDataSourceName()).
        toArray(new Source[0]);
    return sources;
  }

  /**
   * Implements {@link MEMEDataSource#addTermgroups(Termgroup[])}
   * @param termgroups An array of object {@link Termgroup}
   * @throws DataSourceException if failed to add termgroups
   * @throws BadValueException if failed due to invalid data value.
   */
  public void addTermgroups(Termgroup[] termgroups) throws DataSourceException,
      BadValueException {

    MEMEToolkit.trace("MEMEConnection.addTermgroup(Termgroup)...");

    // Throws exception to_outrank ! exist
    if (termgroups[0].getTermgroupToOutrank() == null) {
      BadValueException bve = new BadValueException(
          "Termgroup to outrank does not exist.");
      bve.setDetail("termgroup", termgroups[0]);
      throw bve;
    }

    // Truncate source_termgroup_rank
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareCall(
          "{call MEME_SYSTEM.truncate('source_termgroup_rank')}");
      pstmt.execute();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (SQLException se2) {}
      DataSourceException dse = new DataSourceException(
          "Failed to truncate source_termgroup_rank.", termgroups, se);
      throw dse;
    }

    try {
      pstmt = prepareCall("{call MEME_SYSTEM.truncate('source_source_rank')}");
      pstmt.execute();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (SQLException se2) {}
      DataSourceException dse = new DataSourceException(
          "Failed to truncate source_source_rank.", termgroups, se);
      throw dse;
    }

    // Insert into source_termgroup_rank
    String insert_str =
        "INSERT INTO source_termgroup_rank (high_termgroup, low_termgroup," +
        " suppressible, exclude, norm_exclude, tty, rank)" +
        " VALUES (?,?,?,?,?,?,?)";

    for (int i = 0; i < termgroups.length; i++) {
      pstmt = null;
      try {
        pstmt = prepareStatement(insert_str);
        pstmt.setString(1, termgroups[i].toString());
        pstmt.setString(2, termgroups[i].getTermgroupToOutrank().toString());
        pstmt.setString(3, termgroups[i].getSuppressible());
        pstmt.setString(4, termgroups[i].exclude() ? "Y" : "N");
        pstmt.setString(5, termgroups[i].normExclude() ? "Y" : "N");
        pstmt.setString(6, termgroups[i].getTermType());
        pstmt.setString(7, termgroups[i].getRank().toString());
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert row to source_termgroup_rank.", termgroups, se);
        dse.setDetail("insert", insert_str);
        throw dse;
      }
    }

    // Insert ranks
    pstmt = null;
    try {
      pstmt = prepareCall("{call MEME_SOURCE_PROCESSING.insert_ranks('" +
                          termgroups[0].getSource().getSourceAbbreviation() +
                          "', 0, set_ranks => 'N')}");
      pstmt.execute();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (SQLException se2) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert ranks", termgroups, se);
      throw dse;
    }

    // Update the cache
    cacheTermgroupRank(termgroups[0].toString());

  }

  /**
   * Implements {@link MEMEDataSource#addTermgroup(Termgroup)}
   * @param termgroup the {@link Termgroup}
   * @throws DataSourceException if failed to add termgroup
   * @throws BadValueException if failed due to invalid data value.
   */
  public void addTermgroup(Termgroup termgroup) throws DataSourceException,
      BadValueException {
    addTermgroups(new Termgroup[] {termgroup});
  }

  /**
   * Implements {@link MEMEDataSource#removeTermgroup(Termgroup)}
   * @param termgroup the {@link Termgroup}
   * @throws DataSourceException if failed to remove termgroup
   * @throws BadValueException if failed due to invalid data value.
   */
  public void removeTermgroup(Termgroup termgroup) throws DataSourceException,
      BadValueException {

    MEMEToolkit.trace("MEMEConnection.removeTermgroup(Termgroup)...");

    // Remove from termgroup_rank
    String remove_str = "DELETE FROM termgroup_rank WHERE termgroup = ?";
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.setString(1, termgroup.toString());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from termgroup rank.", termgroup, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

    // Update the cache
    cacheTermgroupRank(termgroup.toString());

  }

  /**
   * Implements {@link MIDDataSource#setTermgroup(Termgroup)}.
   * @param termgroup the {@link Termgroup}
   * @throws DataSourceException if failed to set termgroup
   * @throws BadValueException if failed due to invalid data value.
   */
  public void setTermgroup(Termgroup termgroup) throws DataSourceException,
      BadValueException {

    MEMEToolkit.trace("MIDConnection.setTermgroup(Termgroup)...");

    //
    // Update termgroup rank
    //

    String update_termgroup = "UPDATE termgroup_rank"
        +
        " SET rank = ?, notes = ?, suppressible = ?, tty = ?, release_rank = ? "
        + " WHERE termgroup = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_termgroup);
      pstmt.setString(1, termgroup.getRank().toString());
      pstmt.setString(2, termgroup.getNotes());
      pstmt.setString(3, termgroup.getSuppressible());
      pstmt.setString(4, termgroup.getTermType());
      pstmt.setString(5, termgroup.getReleaseRank().toString());
      pstmt.setString(6, termgroup.toString());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
    }
    catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to set termgroup.", termgroup, se);
      dse.setDetail("update", update_termgroup);
      dse.setDetail("termgroup", termgroup.toString());
      throw dse;
    }

    //
    // Update cache
    //
    cacheTermgroupRank(termgroup.toString());

  }

  /**
   * Implements {@link MEMEDataSource#getTermgroup(String)}.
   * @param termgroup_name the {@link String} representation of termgroup
   * rank.
   * @return the {@link Termgroup}.
   * @throws DataSourceException if failed get termgroup.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Termgroup getTermgroup(String termgroup_name) throws
      DataSourceException, BadValueException {

    // Look it up in the cache
    Termgroup termgroup = (Termgroup) termgroup_rank_cache.get(
        getDataSourceName(), termgroup_name);

    // If its not in the cache, look it up again
    if (termgroup == null) {
      cacheTermgroupRank(termgroup_name);
      termgroup = (Termgroup) termgroup_rank_cache.get(getDataSourceName(),
          termgroup_name);
    }

    // If its still not there, it is illegal
    if (termgroup == null) {
      BadValueException bve = new BadValueException("Illegal termgroup value.");
      bve.setDetail("termgroup", termgroup_name);
      throw bve;
    }
    return termgroup;
  }

  /**
   * Implements {@link MEMEDataSource#getTermgroups()}.
   * @return An array of object {@link Termgroup}.
   */
  public Termgroup[] getTermgroups() {
    Termgroup[] termgroups = (Termgroup[]) termgroup_rank_cache.values(
        getDataSourceName()).toArray(new Termgroup[0]);
    return termgroups;
  }

  /**
   * Adds the specified {@link SemanticType}.
   * @param sty the specified {@link SemanticType}
   * @throws DataSourceException if failed to add semantic type
   */
  public void addValidSemanticType(SemanticType sty) throws DataSourceException {

    MEMEToolkit.trace("MEMEConnection.addValidSemanticType(SemanticType)...");

    // Insert into semantic_types
    String insert_str =
        "INSERT INTO semantic_types (semantic_type, is_chem," +
        " chem_type, editing_chem) VALUES (?,?,?,?)";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(insert_str);
      pstmt.setString(1, sty.getValue());
      pstmt.setString(2, sty.isChemical() ? "Y" : "N");
      pstmt.setString(3, sty.getChemicalType());
      pstmt.setString(4, sty.isEditingChemical() ? "Y" : "N");
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to semantic_types.", sty, se);
      dse.setDetail("insert", insert_str);
      throw dse;
    }

    // Insert into srdef
    insert_str =
        "INSERT INTO srdef (rt, ui, sty_rl, stn_rtn, def," +
        " ex, un, nh, abr, rin) VALUES (?,?,?,?,?,?,?,?,?,?)";

    pstmt = null;
    try {
      pstmt = prepareStatement(insert_str);
      pstmt.setString(1, "STY");
      pstmt.setString(2, sty.getTypeIdentifier().toString());
      pstmt.setString(3, sty.getValue());
      pstmt.setString(4, sty.getTreePosition());
      pstmt.setString(5, sty.getDefinition());
      pstmt.setString(6, null);
      pstmt.setString(7, null);
      pstmt.setString(8, null);
      pstmt.setString(9, null);
      pstmt.setString(10, null);
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to srdef.", sty, se);
      dse.setDetail("insert", insert_str);
      throw dse;
    }

    // Update the cache
    cacheSemanticTypes();

  }

  /**
   * Implements {@link MEMEDataSource#removeValidSemanticType(SemanticType)}
   * @param sty the {@link SemanticType}
   * @throws DataSourceException if failed to remove semantic type
   */
  public void removeValidSemanticType(SemanticType sty) throws
      DataSourceException {

    MEMEToolkit.trace("MEMEConnection.removeSemanticType(SemanticType)...");

    // Remove from semantic_type
    String remove_str = "DELETE FROM semantic_types WHERE semantic_type = ?";
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.setString(1, sty.getValue());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from semantic type.", sty, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

    // Remove from srdef
    remove_str = "DELETE FROM srdef WHERE ui = ?";
    pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.setString(1, sty.getTypeIdentifier().toString());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from srdef.", sty, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

    // Update the cache
    cacheSemanticTypes();

  }

  /**
   * Implements {@link MEMEDataSource#getSemanticType(String)}.
   * @param sty the {@link String} representation of semantic type.
   * @return the {@link SemanticType}.
   * @throws BadValueException if failed due to invalid data value.
   */
  public SemanticType getSemanticType(String sty) throws BadValueException {
    // Look it up
    SemanticType semantic_type = (SemanticType) sty_cache.get(getDataSourceName(),
        sty);
    // If it is not there, it is illegal
    if (semantic_type == null) {
      BadValueException bve = new BadValueException(
          "Illegal semantic type value.");
      bve.setDetail("semantic_type", semantic_type);
      throw bve;
    }
    return semantic_type;
  }

  /**
   * Implements {@link MEMEDataSource#getValidSemanticTypes()}.
   * @return An array of object {@link SemanticType}.
   */
  public SemanticType[] getValidSemanticTypes() {
    SemanticType[] stys = (SemanticType[]) sty_cache.values(getDataSourceName()).
        toArray(new SemanticType[0]);
    return stys;
  }

  /**
   * Implements {@link MEMEDataSource#addLanguage(Language)}
   * @param language the {@link Language}
   * @throws DataSourceException if failed to add language
   */
  public void addLanguage(Language language) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.addLanguage(Language)...");

    String insert_str =
        "INSERT INTO language (language, lat, iso_lat) VALUES (?, ?, ?)";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(insert_str);
      pstmt.setString(1, language.toString());
      pstmt.setString(2, language.getAbbreviation());
      pstmt.setString(3, language.getISOAbbreviation());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to language.", language, se);
      dse.setDetail("insert", insert_str);
      throw dse;
    }

    // Update the cache
    cacheLanguages();

  }

  /**
   * Implements {@link MEMEDataSource#removeLanguage(Language)}
   * @param language the {@link Language}
   * @throws DataSourceException if failed to remove language
   */
  public void removeLanguage(Language language) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.removeLanguage(Language)...");

    String remove_str =
        "DELETE FROM language WHERE lat = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.setString(1, language.getAbbreviation());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from language.", language, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

    // Update the cache
    cacheLanguages();

  }

  /**
   * Implements {@link MEMEDataSource#setLanguage(Language)}
   * @param language the {@link Language}
   * @throws DataSourceException if failed to set language
   */
  public void setLanguage(Language language) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.setLanguage(Language)...");

    String update_str =
        "UPDATE language SET language = ?, iso_lat = ? " +
        "WHERE lat = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(update_str);
      pstmt.setString(1, language.toString());
      pstmt.setString(2,
                      language.getISOAbbreviation() == null ? "" :
                      language.getISOAbbreviation());
      pstmt.setString(3, language.getAbbreviation());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to update row to language.", language, null);
      dse.setDetail("update", update_str);
      throw dse;
    }

    // Update the cache
    lat_cache.put(getDataSourceName(), language.getAbbreviation(), language);
    lat_cache.put(getDataSourceName(), language.getISOAbbreviation(), language);

  }

  /**
   * Implements {@link MEMEDataSource#getLanguage(String)}.
   * @param lat the {@link String} representation of language.
   * @return the {@link Language}.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Language getLanguage(String lat) throws BadValueException {
    Language language = (Language) lat_cache.get(getDataSourceName(), lat);
    if (language == null) {
      BadValueException bve = new BadValueException("Illegal language value.");
      bve.setDetail("language", lat);
      throw bve;
    }
    return language;
  }

  /**
   * Implements {@link MEMEDataSource#getLanguages()}.
   * @return An array of object {@link Language}.
   */
  public Language[] getLanguages() {
    Language[] lats = (Language[]) lat_cache.values(getDataSourceName()).
        toArray(new Language[0]);
    return lats;
  }

  /**
   * Implements {@link MEMEDataSource#getCodeByValue(String, String)}.
   * @param type the {@link String} representation of code type.
   * @param value the {@link String} representation of code value.
   * @return the {@link String} representation of code map.
   * @throws BadValueException if failed due to invalid data value.
   */
  public String getCodeByValue(String type, String value) throws
      BadValueException {

    MetaCode[] mcs = (MetaCode[]) code_map_cache.values(getDataSourceName()).
        toArray(new MetaCode[0]);
    for (int i = 0; i < mcs.length; i++) {
      if (mcs[i].getValue().equals(value) && mcs[i].getType().equals(type)) {
        return mcs[i].getCode();
      }
    }
    BadValueException bve = new BadValueException("Illegal code value.");
    throw bve;
  }

  /**
   * Implements {@link MEMEDataSource#getValueByCode(String, String)}.
   * @param type the {@link String} representation of type.
   * @param code the {@link String} representation of code.
   * @return the {@link String} representation of value.
   * @throws BadValueException if failed due to invalid data value.
   */
  public String getValueByCode(String type, String code) throws
      BadValueException {
    String value = ( (MetaCode) code_map_cache.get(getDataSourceName(),
        type + code)).getValue();
    if (value == null) {
      BadValueException bve = new BadValueException("Illegal code value.");
      bve.setDetail("value", value);
      throw bve;
    }
    return value;
  }

  /**
   * This maps classes to max_tab table_names.  It is used
   * by {@link #getNextIdentifierForType(Class)} and
   * {@link #getMaxIdentifierForType(Class)}
   * @param c and object {@link Class}
   * @return a valid table name.
   * @throws DataSourceException if failed to map class.
   */
  protected String mapClassToTableName(Class c) throws DataSourceException {
    if (c == Ticket.class) {
      return "ACTION_TICKETS";
    }
    else if (c == AtomicAction.class) {
      return "ATOMIC_ACTIONS";
    }
    else if (c == Atom.class) {
      return "ATOMS";
    }
    else if (c == Attribute.class) {
      return "ATTRIBUTES";
    }
    else if (c == ATUI.class) {
      return "ATUI";
    }
    else if (c == AUI.class) {
      return "AUI";
    }
    else if (c == Concept.class) {
      return "CONCEPT_STATUS";
    }
    else if (c == CUI.class) {
      return "CUI";
    }
    else if (c == ISUI.class) {
      return "ISUI";
    }
    else if (c == LUI.class) {
      return "LUI";
    }
    else if (c == MolecularAction.class) {
      return "MOLECULAR_ACTIONS";
    }
    else if (c == Relationship.class) {
      return "RELATIONSHIPS";
    }
    else if (c == RUI.class) {
      return "RUI";
    }
    else if (c == SUI.class) {
      return "SUI";
    }
    else if (c == MolecularTransaction.class) {
      return "TRANSACTIONS";
    }
    else if (c == WorkLog.class) {
      return "WORK";
    }
    else if (c == MapSet.class) {
      return "MAP_SET";
    }
    else if (c == ContentView.class) {
      return "CONTENT_VIEWS";
    }
    else if (c == ConceptMapping.class) {
      return "CONCEPT_MAPPINGS";
    }
    else if (c == LoggedAction.class) {
      return "ACTIONS";
    }
    else {
      DataSourceException dse = new DataSourceException(
          "Invalid type for max identifier.");
      dse.setDetail("table", c.getName());
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getNextIdentifierForType(Class)}.
   * @param c the {@link Class}.
   * @return the {@link Identifier}.
   * @throws DataSourceException if failed to load max tab.
   */
  public Identifier getNextIdentifierForType(Class c) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.getNextIdentifierForType(Class)...");

    String update_str = null;

    if (c == String.class) {
      update_str = "UPDATE stringtab SET row_sequence = row_sequence+1" +
          " WHERE string_id = -1";
    }
    else {
      update_str = "UPDATE max_tab SET max_id = max_id+1" +
          " WHERE table_name = ?";
    }

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(update_str);
      if (c != String.class) {
        pstmt.setString(1, mapClassToTableName(c));
      }
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to update table.", c.getName(), se);
      dse.setDetail("update", update_str);
      throw dse;
    }

    return getMaxIdentifierForType(c);
  }

  /**
   * Implements {@link MEMEDataSource#getMaxIdentifierForType(Class)}.
   * @param c the {@link Class}.
   * @return the {@link Identifier}.
   * @throws DataSourceException if failed to load max tab.
   */
  public Identifier getMaxIdentifierForType(Class c) throws DataSourceException {

    String query = null;

    if (c == String.class) {
      query = "SELECT row_sequence AS max_id FROM stringtab " +
          "WHERE string_id = -1";
    }
    else {
      query = "SELECT max_id FROM max_tab " +
          "WHERE table_name = ?";
    }

    int max_id = 0;
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      if (c != String.class) {
        pstmt.setString(1, mapClassToTableName(c));
      }
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        max_id = rs.getInt("MAX_ID");
      }

      // Close statement
      pstmt.close();

      if (max_id == 0) {
        // No match found
        DataSourceException dse = new DataSourceException(
            "No max id found.");
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to look up table.", c, se);
      dse.setDetail("query", query);
      throw dse;
    }

    // id found
    Identifier id = new Identifier.Default(max_id);
    return id;

  }

  /**
   * Implements {@link MEMEDataSource#lockConcept(Concept, boolean)}.
   * @param c the concept to lock
   * @param lock_related flag indicating whether or not to lock related concepts
   * @throws DataSourceException if failed to lock concept
   */
  public void lockConcept(Concept c, boolean lock_related) throws
      DataSourceException {
    Statement stmt = null;
    try {
      if (lock_related) {
        stmt = createStatement();
        stmt.execute(
            "SELECT * FROM concept_status WHERE concept_id IN (" +
            "SELECT " + c.getIdentifier() + " FROM dual " +
            "UNION " +
            "SELECT concept_id_1 FROM relationships " +
            "WHERE concept_id_2 = " + c.getIdentifier() + " " +
            "UNION " +
            "SELECT concept_id_2 FROM relationships " +
            "WHERE concept_id_1 = " + c.getIdentifier() + ") " +
            "FOR UPDATE NOWAIT");
        stmt.close();
      }
      else {
        stmt = createStatement();
        stmt.execute(
            "SELECT * FROM concept_status WHERE concept_id = " +
            c.getIdentifier() + " FOR UPDATE NOWAIT");
        stmt.close();
      }
    }
    catch (SQLException se) {
      try {
        stmt.close();
      }
      catch (SQLException e) {}
      //
      // If we encounter a :
      // ORA-00054: resource busy and acquire with NOWAIT specified
      // exception, it is OK.  We should wait for some period of time
      // and try again.
      //
      if (se.getMessage().indexOf("NOWAIT") != -1) {
        // Write entry to the log
        MEMEException me = new MEMEException(
            "Failed to acquire lock due to another process, trying again.");
        me.setFatal(false);
        me.setPrintStackTrace(true);
        me.setDetail("concept_id", c.getIdentifier());
        MEMEToolkit.handleError(me);
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException ie) {}
        lockConcept(c, lock_related);
        return;
      }

      throw new DataSourceException("Failed to lock concept.", null, se);
    }
    return ;
  }

  /**
   * Locks the pair of concepts, and optionally the related concepts as well.
   * @param s the source {@link Concept}
   * @param t the target {@link Concept}
   * @param lock_related <code>boolean</code> indicating whether or not to lock
   * related concepts
   * @throws DataSourceException if failed to lock concept
   */
  public void lockConcepts(Concept s, Concept t, boolean lock_related) throws
      DataSourceException {
    Statement stmt = null;
    try {
      if (lock_related) {
        stmt = createStatement();
        stmt.execute(
            "SELECT * FROM concept_status WHERE concept_id IN (" +
            "SELECT " + s.getIdentifier() + " FROM dual " +
            "UNION " +
            "SELECT concept_id_1 FROM relationships " +
            "WHERE concept_id_2 in (" + s.getIdentifier() + "," +
            t.getIdentifier() + ") " +
            "UNION " +
            "SELECT concept_id_2 FROM relationships " +
            "WHERE concept_id_1 in (" + s.getIdentifier() + "," +
            t.getIdentifier() + ") " +
            "UNION " +
            "SELECT " + t.getIdentifier() + " FROM dual " + ") " +
            "FOR UPDATE NOWAIT");
        stmt.close();
      }
      else {
        stmt = createStatement();
        stmt.execute(
            "SELECT * FROM concept_status WHERE concept_id in (" +
            s.getIdentifier() + "," + t.getIdentifier() + ") FOR UPDATE NOWAIT");
        stmt.close();
      }
    }
    catch (SQLException se) {
      try {
        stmt.close();
      }
      catch (SQLException e) {}
      //
      // If we encounter a :
      // ORA-00054: resource busy and acquire with NOWAIT specified
      // exception, it is OK.  We should wait for some period of time
      // and try again.
      //
      if (se.getMessage().indexOf("NOWAIT") != -1) {
        // Write entry to the log
        MEMEException me = new MEMEException(
            "Failed to acquire lock due to another process, trying again.");
        me.setFatal(false);
        me.setPrintStackTrace(true);
        me.setDetail("source_id", s.getIdentifier());
        me.setDetail("target_id", t.getIdentifier());
        MEMEToolkit.handleError(me);
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException ie) {}
        lockConcepts(s, t, lock_related);
        return;
      }

      throw new DataSourceException("Failed to lock concept.", null, se);
    }
    return ;
  }

  /**
   * Implements {@link MEMEDataSource#unlockConcepts()}.
   * @throws DataSourceException if failed to unlock concept
   */
  public void unlockConcepts() throws DataSourceException {
    synchronized (concept_lock) {
      concept_lock.notifyAll();
    }
  }

  /**
   * Determines the query used to read concept.
   * @param ticket the {@link Ticket}.
   * @return the query.
   */
  private String getConceptsQuery(Ticket ticket) {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    if (ticket.readDeleted()) {
      return READ_DEAD_CONCEPT;
    }
    else {
      return READ_CONCEPT;
    }

  }

  /**
   * Implements {@link MEMEDataSource#getConcept(CUI, Ticket)}.
   * @param cui the {@link CUI}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Concept}.
   * @throws DataSourceException if failed to look up concept.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Concept getConcept(CUI cui, Ticket ticket) throws DataSourceException,
      BadValueException {

    // Query to map CUI to a concept_id
    final String query = "SELECT concept_id FROM concept_status " +
        "WHERE cui = ?";

    int concept_id = 0;
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, cui.toString());
      ResultSet rs = pstmt.executeQuery();

      int row_count = 0;

      // Read
      while (rs.next()) {
        row_count++;
        concept_id = rs.getInt(1);
      }

      // Close statement
      pstmt.close();

      if (concept_id == 0) {
        // No CUI match found
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("cui", cui.toString());
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to look up CUI.", cui, se);
      dse.setDetail("query", query);
      dse.setDetail("cui", cui.toString());
      throw dse;
    }

    // Concept found
    return getConcept(concept_id, ticket);
  }

  /**
   * Implements {@link MEMEDataSource#getConcept(int, Ticket)}.
   * @param concept_id the concept_id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Concept}.
   * @throws DataSourceException if failed to look up concept.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Concept getConcept(int concept_id, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    Concept concept = null;
    boolean flag = false;

    if (ticket.mapDataType(Concept.class)) {
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(getConceptsQuery(ticket));
        pstmt.setInt(1, concept_id);
        ResultSet rs = pstmt.executeQuery();

        // Create concept object and assign it an id
        ConceptMapper[] mappers = (ConceptMapper[])
            ticket.getDataMapper(Concept.class).toArray(new ConceptMapper[0]);
        ConceptMapper default_mapper = new ConceptMapper.Default();

        while (rs.next()) {
          for (int i = 0; i < mappers.length; i++) {
            concept = mappers[i].map(rs, this);
            if (concept != null) {
              break;
            }
          }
          // If concept is still null, use default mapper
          if (concept == null) {
            concept = default_mapper.map(rs, this);
          }
        }

        pstmt.close();
        flag = false;

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load concept.", concept, se);
        dse.setDetail("query", getConceptsQuery(ticket));
        dse.setDetail("concept_id", Integer.toString(concept_id));
        throw dse;
      }

    }
    else {
      concept = new Concept.Default(concept_id);
      flag = true;
    }

    // Populate the concept, ensure that
    // readConcepts toggle is off, but reset
    // it after the populate call.
    boolean read_concepts = ticket.readConcept();
    ticket.setReadConcept(flag);
    populateConcept(concept, ticket);
    ticket.setReadConcept(read_concepts);

    return concept;
  }

  /**
   * Implements {@link MEMEDataSource#addConceptMapping(ConceptMapping)}
   * @param cm the {@link ConceptMapping}
   * @throws DataSourceException if failed to add concept mapping
   */
  public void addConceptMapping(ConceptMapping cm) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.addConceptMapping(ConceptMapping)...");

    cm.setIdentifier(getNextIdentifierForType(ConceptMapping.class));

    String insert_str = "INSERT INTO cui_map ( "
        + "map_id, cui, birth_version, death_version, "
        + "mapped_to_cui, relationship_name, relationship_attribute, "
        + "map_reason, almost_sy, generated_status, source, dead, "
        + "status, suppressible, authority, timestamp, insertion_date, "
        +
        "released, tobereleased, rank, last_molecule_id, last_atomic_action_id) "
        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    DateFormat dateformat = MEMEToolkit.getDateFormat();
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(insert_str);
      pstmt.setInt(1, cm.getIdentifier().intValue());
      pstmt.setString(2, cm.getCUI().toString());
      pstmt.setString(3, cm.getBirthVersion());
      pstmt.setString(4, cm.getDeathVersion());
      pstmt.setString(5, cm.getMappedToCui().toString());
      pstmt.setString(6, cm.getRelationshipName());
      pstmt.setString(7, cm.getRelationshipAttribute());
      pstmt.setString(8, cm.getMappingReason());
      pstmt.setString(9, cm.isAlmostSY() ? "Y" : "N");
      pstmt.setString(10, cm.isGenerated() ? "Y" : "N");
      pstmt.setString(11, cm.getSource().getSourceAbbreviation());
      pstmt.setString(12, cm.isDead() ? "Y" : "N");
      pstmt.setString(13, String.valueOf(cm.getStatus()));
      pstmt.setString(14, cm.getSuppressible());
      pstmt.setString(15, cm.getAuthority().toString());
      pstmt.setString(16, dateformat.format(cm.getTimestamp()));
      pstmt.setString(17, dateformat.format(cm.getInsertionDate()));
      pstmt.setString(18, String.valueOf(cm.getReleased()));
      pstmt.setString(19, String.valueOf(cm.getTobereleased()));
      pstmt.setString(20, cm.getRank().toString());
      pstmt.setInt(21, cm.getIdentifier().intValue());
      pstmt.setInt(22,
                   cm.getLastAction() == null ? 0 :
                   cm.getLastAction().getIdentifier().intValue());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to cui_map.", cm, se);
      dse.setDetail("insert", insert_str);
      throw dse;
    }

  }

  /**
   * Implements {@link MEMEDataSource#removeConceptMapping(ConceptMapping)}
   * @param cm the {@link ConceptMapping}
   * @throws DataSourceException if failed to remove concept mapping
   */
  public void removeConceptMapping(ConceptMapping cm) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.removeConceptMapping(ConceptMapping)...");

    String remove_str = "DELETE FROM cui_map WHERE map_id = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.setInt(1, cm.getIdentifier().intValue());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from cui_map.", cm, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

  }

  /**
   * Implements {@link MEMEDataSource#getConceptMappings(Ticket)}.
   * @param id the map id
   * @return an array of {@link ConceptMapping}
   * @throws BadValueException if failed due to invalid data value
   * @throws DataSourceException if failed to get concept mapping
   */
  public ConceptMapping getConceptMapping(int id) throws DataSourceException,
      BadValueException {

    ConceptMapping cm = null;

    // Query to get concept mappings
    final String query = "SELECT * FROM cui_map WHERE map_id = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setInt(1, id);
      ResultSet rs = pstmt.executeQuery();

      // Create concept object and assign it an id
      ConceptMappingMapper default_mapper = new ConceptMappingMapper();

      while (rs.next()) {
        cm = (ConceptMapping) default_mapper.map(rs, this);
      }

      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load concept mapping.", cm, se);
      dse.setDetail("query", query);
      throw dse;
    }

    // return
    return cm;
  }

  /**
   * Implements {@link MEMEDataSource#getConceptMappings(Ticket)}.
   * @param ticket the {@link Ticket}
   * @return an array of {@link ConceptMapping}
   * @throws BadValueException if failed due to invalid data value
   * @throws DataSourceException if failed to get concept mapping
   */
  public ConceptMapping[] getConceptMappings(Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    ConceptMapping cm = null;
    List maps = new ArrayList();

    // Query to get concept mappings
    final String query = "SELECT * FROM cui_map";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      ResultSet rs = pstmt.executeQuery();

      // Create concept object and assign it an id
      ConceptMappingMapper[] mappers = (ConceptMappingMapper[])
          ticket.getDataMapper(ConceptMapping.class).toArray(new
          ConceptMappingMapper[0]);
      ConceptMappingMapper default_mapper = new ConceptMappingMapper();

      while (rs.next()) {
        for (int i = 0; i < mappers.length; i++) {
          cm = mappers[i].map(rs, this);
          if (cm != null) {
            break;
          }
        }
        // If concept is still null, use default mapper
        if (cm == null) {
          cm = (ConceptMapping) default_mapper.map(rs, this);

        }
        maps.add(cm);
      }

      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load concept mapping.", cm, se);
      dse.setDetail("query", query);
      throw dse;
    }

    // return
    return (ConceptMapping[]) maps.toArray(new ConceptMapping[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getConceptMappings(Concept, Ticket)}.
   * @param concept the {@link Concept}
   * @param ticket the {@link Ticket}
   * @return an array of {@link ConceptMapping}
   * @throws BadValueException if failed due to invalid data value
   * @throws DataSourceException if failed to get concept mapping
   */
  public ConceptMapping[] getConceptMappings(Concept concept, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    ConceptMapping cm = null;
    List maps = new ArrayList();

    // Query to get concept mappings
    final String query = "SELECT * FROM cui_map WHERE cui = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, concept.getCUI().toString());
      ResultSet rs = pstmt.executeQuery();

      // Create concept object and assign it an id
      ConceptMappingMapper[] mappers = (ConceptMappingMapper[])
          ticket.getDataMapper(ConceptMapping.class).toArray(new
          ConceptMappingMapper[0]);
      ConceptMappingMapper default_mapper = new ConceptMappingMapper();

      while (rs.next()) {
        for (int i = 0; i < mappers.length; i++) {
          cm = mappers[i].map(rs, this);
          if (cm != null) {
            break;
          }
        }
        // If concept is still null, use default mapper
        if (cm == null) {
          cm = (ConceptMapping) default_mapper.map(rs, this);

        }
        maps.add(cm);
      }
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load concept mapping.", concept, se);
      dse.setDetail("query", query);
      throw dse;
    }

    // return
    return (ConceptMapping[]) maps.toArray(new ConceptMapping[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getMapSets()}.
   * @return An array of object {@link MapSet}
   * @throws DataSourceException if failed to look up map set
   * @throws BadValueException if failed due to invalid data value
   */
  public MapSet[] getMapSets() throws DataSourceException, BadValueException {
    Ticket ticket = Ticket.getMappingTicket();
    ticket.setDataTypeRestriction(Attribute.class,
                                  "AND attribute_name NOT IN ('XMAP', 'XMAPTO', 'XMAPFROM')");
    SearchParameter[] sps = {
        new SearchParameter.Single("tty", "XM"),
        new SearchParameter.Multiple("tobereleased", new String[] {"Y", "y"}),
        new SearchParameter.Single(
            "source IN (SELECT SUBSTR(termgroup,1,INSTR(termgroup,'/')-1) " +
            "FROM termgroup_rank WHERE tty='XM') AND 'a'", "a")
    };
    Iterator iterator = findConcepts(sps, null);
    List mapsets = new ArrayList();
    while (iterator.hasNext()) {
      Concept concept = (Concept) iterator.next();
      mapsets.add(getMapSet(concept.getIdentifier().intValue(), ticket));
    }
    return (MapSet[]) mapsets.toArray(new MapSet[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getMapSet(int, Ticket)}.
   * @param concept_id the concept_id.
   * @param ticket the {@link Ticket}.
   * @return An array of object {@link MapSet}.
   * @throws DataSourceException if failed to look up map set.
   * @throws BadValueException if failed due to invalid data value.
   */
  public MapSet getMapSet(int concept_id, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    MapSet concept = new MapSet.Default(concept_id);

    // Check ticket cache
    if (ticket.get(concept) != null) {
      return (MapSet) ticket.get(concept);
    }

    populateConcept(concept, ticket);

    // Set FROM, TO and mapset sources
    Attribute[] attrs =
        concept.getAttributesByNames(new String[] {"FROMVSAB", "TOVSAB",
                                     "MAPSETVSAB"});
    for (int i = 0; i < attrs.length; i++) {
      if (attrs[i].getName().equals("FROMVSAB") && attrs[i].isReleasable()) {
        concept.setFromSource(getSource(attrs[i].getValue()));
      }
      else if (attrs[i].getName().equals("TOVSAB") && attrs[i].isReleasable()) {
        concept.setToSource(getSource(attrs[i].getValue()));
      }
      else if (attrs[i].getName().equals("MAPSETVSAB") &&
               attrs[i].isReleasable()) {
        concept.setMapSetSource(getSource(attrs[i].getValue()));
      }
    }

    return concept;

  }

  /**
   * Implements {@link MEMEDataSource#populateConcept(Concept, Ticket)}.
   * @param concept the {@link Concept}.
   * @param ticket the {@link Ticket}.
   * @throws DataSourceException if failed to populate concept.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateConcept(Concept concept, Ticket ticket) throws
      DataSourceException, BadValueException {
	MEMEToolkit.trace("MEMEConnection.populateConcept(Concept, Ticket)...");

	Set excluded_atoms = new HashSet();
    String language = null;

    //
    // Use empty ticket if not ticket is specified
    //
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

      //
      // If the concept has no identifier, report an exception
      //
    }
    if (concept.getIdentifier() == null) {
      throw new BadValueException("Concept must have an Identifier.");
    }

    //
    // If we are building graphs add this concept to the cache
    //
    if (ticket.expandGraph()) {
      ticket.put(concept, concept);

      //
      // Clear the concept object
      //
    }
    concept.clear();

    //
    // Read Concept Information
    //
    PreparedStatement pstmt = null;
    int concept_id = concept.getIdentifier().intValue();
    Map cd_map = new HashMap();
    int row_count = 0;
    if (ticket.readConcept()) {
      String query = getConceptsQuery(ticket);
      try {
        pstmt = prepareStatement(query);
        pstmt.setInt(1, concept_id);
        ResultSet rs = pstmt.executeQuery();

        row_count = 0;
        ConceptMapper[] mappers = (ConceptMapper[])
            (ticket.getDataMapper(Concept.class)).toArray(new ConceptMapper[0]);
        ConceptMapper default_mapper = new ConceptMapper.Default();

        // Read
        while (rs.next()) {

          // Give each mapper a chance to populate the concept
          boolean found = false;
          if (ticket.mapDataType(Concept.class)) {
            for (int i = 0; i < mappers.length; i++) {
              if (mappers[i].populate(rs, this, concept)) {
                found = true;
                break;
              }
            }
          }

          // If relationship was not populated, use default mapper
          if (!found) {
            default_mapper.populate(rs, this, concept);
          }

          //
          // If reading actions, get the approval action
          //
          if (ticket.readActions()) {
            int molecule_id = rs.getInt("APPROVAL_MOLECULE_ID");
            MolecularAction action = getMolecularAction(molecule_id);
            if (action == null) {
              action = new MolecularAction(molecule_id);
            }
            concept.setApprovalAction(action);
          }

          row_count++;
        }

        // Close statement
        pstmt.close();

        //
        // There should only have exactly one concept row
        //
        if (row_count == 0) {
          MissingDataException dse = new MissingDataException("Missing data.");
          dse.setDetail("concept_id", Integer.toString(concept_id));
          throw dse;
        }
        if (row_count > 1) {
          DataSourceException dse = new DataSourceException(
              "Too much data.");
          dse.setDetail("concept_id", Integer.toString(concept_id));
          throw dse;
        }

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load concept.", concept, se);
        dse.setDetail("query", query);
        dse.setDetail("concept_id", Integer.toString(concept_id));
        throw dse;
      }
    }

    //
    // Read Atoms
    //
    if (ticket.readAtoms()) {
      Atom atom = null;
      try {
        //
        // Query to check if there are any ambiguous atoms.
        //
        String tmp_ambig_query = "SELECT COUNT(*) AS CT " +
            "FROM classes WHERE isui = ?" +
            " AND concept_id != ?" +
            " AND tobereleased IN ('Y','y')";
        final String ambig_query = tmp_ambig_query +
            " AND (source != 'MTH' OR termgroup NOT LIKE '%PN')";
        final String pn_ambig_query = tmp_ambig_query +
            " AND source = 'MTH' AND termgroup LIKE '%PN'";
        PreparedStatement pn_ambig_pstmt = null;
        PreparedStatement no_pn_ambig_pstmt = null;
        PreparedStatement ambig_pstmt = null;

        //
        // Check ambiguity (if requested)
        //
        if (ticket.checkAtomAmbiguity()) {
          try {
            pn_ambig_pstmt = prepareStatement(pn_ambig_query);
            no_pn_ambig_pstmt = prepareStatement(ambig_query);
          }
          catch (SQLException se) {
            DataSourceException dse = new DataSourceException(
                "Failed to prepare statement.", atom, se);
            dse.setDetail("query1", pn_ambig_query);
            dse.setDetail("query2", ambig_query);
            throw dse;
          }
        }

        //
        // Prepare and execute READ_ATOMS or READ_ATOMS_WITH_NAME query
        //
        pstmt = prepareStatement(getAtomsQuery(ticket, Concept.class));
        pstmt.setInt(1, concept_id);
        ResultSet rs = pstmt.executeQuery();

        //
        // Read atoms
        //
        row_count = 0;
        AtomMapper[] mappers = (AtomMapper[])
            ticket.getDataMapper(Atom.class).toArray(new AtomMapper[0]);
        AtomMapper default_mapper = new AtomMapper.Default();
        while (rs.next()) {

          language = rs.getString("LANGUAGE");

          //
          // Hash excluded atom id
          //

          if (!ticket.readLanguage(language)) {
            excluded_atoms.add(new Integer(rs.getInt("ATOM_ID")));
            continue;
          }

          //
          // Create and populate atom
          //
          atom = null;
          if (ticket.mapDataType(Atom.class)) {
            for (int i = 0; i < mappers.length; i++) {
              atom = mappers[i].map(rs, this);
              if (atom != null) {
                break;
              }
            }
          }

          //
          // If atom is still null, use default mapper
          //
          if (atom == null) {
            atom = default_mapper.map(rs, this);

            //
            // Read atom name
            //
          }
          if (ticket.readAtomNames()) {
            atom.setString(rs.getString("STRING"));
            // atom.setNormalizedString(rs.getString("NORM_STRING"));
          }

          //
          // Determine if the atom is ambiguous
          //

          if (ticket.checkAtomAmbiguity()) {
            if (atom.getTermgroup().toString().equals("MTH/PN")) {
              ambig_pstmt = pn_ambig_pstmt;
            }
            else {
              ambig_pstmt = no_pn_ambig_pstmt;

            }
            try {
              ambig_pstmt.setString(1, rs.getString("ISUI"));
              ambig_pstmt.setInt(2, concept_id);
              ResultSet ambig_rs = ambig_pstmt.executeQuery();
              while (ambig_rs.next()) {
                if (ambig_rs.getInt("CT") > 0) {
                  atom.setIsAmbiguous(true);
                }
                else {
                  atom.setIsAmbiguous(false);
                }
              }
            }
            catch (SQLException se) {
              DataSourceException dse = new DataSourceException(
                  "Failed to determine if atom is ambiguous.", atom, se);
              if (atom.getTermgroup().toString().equals("MTH/PN")) {
                dse.setDetail("query", pn_ambig_query);
              }
              else {
                dse.setDetail("query", ambig_query);
              }
              dse.setDetail("concept_id", concept.getIdentifier().toString());
              throw dse;
            }
          }

          //
          // Connect atom and concept
          //
          atom.setConcept(concept);
          concept.addAtom(atom);

          //
          // Hash atom for later use
          //
          cd_map.put("C" + atom.getIdentifier(), atom);

          row_count++;
        }

        //
        // Close statement
        //
        pstmt.close();

        //
        // Only if we want to check ambiguity of atoms
        //
        if (ticket.checkAtomAmbiguity()) {
          pn_ambig_pstmt.close();
          no_pn_ambig_pstmt.close();
        }

        //
        // There should be more than 0 rows
        //
        if (row_count < 1) {
          MissingDataException dse = new MissingDataException("Missing data.");
          dse.setDetail("concept_id", concept.getIdentifier().toString());
          throw dse;
        }

        //
        // Compute preferred atom of concept
        //
        if (ticket.readConcept() && ticket.calculatePreferredAtom()) {
          Atom[] atoms = concept.getAtoms();
          Arrays.sort(atoms);
          concept.setPreferredAtom(atoms[atoms.length - 1]);
        }

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load atom.", atom, se);
        dse.setDetail("query", getAtomsQuery(ticket, Concept.class));
        dse.setDetail("concept_id", concept.getIdentifier().toString());
        throw dse;
      }
    }

    //
    // Read relationships
    //
    AtomMapper pref_atom_mapper = new PreferredAtomMapper();
    if (ticket.readRelationships()) {

      //
      // Determine queries to use (must use 2 queries)
      //
      boolean inverse = false;
      int count = 0;
      int start = ticket.getDataTypeStart(Relationship.class);
      int end = ticket.getDataTypeEnd(Relationship.class);
      boolean using_count = start != -1;

      //
      // Iterate through queries
      //
      for (int i = 0; i < 2; i++) {

        try {
          //
          // Prepare and execute queries
          //
          pstmt = prepareStatement(
              getRelationshipsQuery(ticket, Concept.class, inverse));
          pstmt.setFetchSize(1000);
          pstmt.setInt(1, concept_id);
          ResultSet rs = pstmt.executeQuery();

          //
          // Read data
          //
          RelationshipMapper[] rel_mappers =
              (RelationshipMapper[]) (ticket.getDataMapper(Relationship.class)).
              toArray(new RelationshipMapper[0]);
          for (int j = 0; j < rel_mappers.length; j++) {
            rel_mappers[j].setConcept(concept);
            rel_mappers[j].setCoreDataMap(cd_map);
          }
          RelationshipMapper rel_mapper = new RelationshipMapper.Default(true);
          rel_mapper.setConcept(concept);
          rel_mapper.setCoreDataMap(cd_map);

          while (rs.next()) {
            count++;

            //
            // Skip excluded language relationship
            //
            if (!rs.getString("RELATIONSHIP_LEVEL").equals("C") &&
                excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_1")))) {
              continue;
            }

            if (!rs.getString("RELATIONSHIP_LEVEL").equals("C") &&
                excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_2")))) {
              continue;
            }

            //
            // If reading only a range, continue if index is in range
            //

            if (using_count && count <= start) {
              continue;
            }
            if (using_count && count > end) {
              break;
            }

            Relationship rel = null;

            //
            //  Construct and populate relationship
            //
            boolean found = false;
            if (ticket.mapDataType(Relationship.class)) {
              for (int j = 0; j < rel_mappers.length; j++) {
                if ( (rel = rel_mappers[j].map(rs, this)) != null) {
                  found = true;
                  break;
                }
              }
            }
            //
            // If relationship was not populated, use default mapper
            //
            if (!found) {
              rel = rel_mapper.map(rs, this);

              //
              // If reading inverse rels, inverse sense of rel and rela
              //
            }
            if (inverse) {
              rel.setName(
                  getInverseRelationshipName(rel.getName()));
              rel.setAttribute(
                  getInverseRelationshipAttribute(rel.getAttribute()));
            }

            rel.setIsInverse(inverse);

            //
            // If we are expanding an entire graph just recursively call getConcept
            //
            if (ticket.expandGraph()) {
              int concept_id_2 = rs.getInt("CONCEPT_ID_2");
              ticket.decrementRecursiveLevel();
              getConcept(concept_id_2, ticket);
              ticket.incrementRecursiveLevel();
            }

            //
            // If we are just expanding the name, look up the
            // preferred atom on the other side.
            //
            else if (ticket.readRelationshipNames()) {
              int concept_id_2 = rs.getInt("CONCEPT_ID_2");
              Concept rc = new Concept.Default(concept_id_2);
              if (rs.getString("STRING") != null) {
                rc.setPreferredAtom(pref_atom_mapper.map(rs, this));
              }
              else {
                try {
                  rc.setPreferredAtom(getPreferredAtom(rc));
                }
                catch (MissingDataException mde) {
                  Atom epa = new Atom.Default( -1);
                  epa.setLUI(new LUI("L0000000"));
                  epa.setSUI(new SUI("S0000000"));
                  epa.setISUI(new ISUI("I0000000"));
                  epa.setConcept(rc);
                  epa.setCode(new Code("NOCODE"));
                  epa.setSource(getSource("MTH"));
                  epa.setTermgroup(getTermgroup("MTH/PT"));
                  epa.setString("This concept has no preferred atom.");
                  rc.setPreferredAtom(epa);
                }
              }
              rel.setRelatedConcept(rc);
            }

            //
            // If we are expanding names then get them for all SFO/LFO
            // (at this point, we know that the sfo/lfo is not self-referential)
            //
            if ( (ticket.readRelationshipNames() &&
                  rel.getName().equals("SFO/LFO"))) {
              int atom_id_2 = rs.getInt("ATOM_ID_2");
              Ticket l_ticket = Ticket.getEmptyTicket();
              Atom atom = getAtomWithName(atom_id_2, l_ticket);
              rel.setRelatedAtom(atom);
            }

            //
            // Hash rel for later use
            //
            cd_map.put("R" + rel.getIdentifier(), rel);

            //
            // Connect atom and rel
            //
            if (rel.isAtomLevel() && rel.getAtom() != null) {
              rel.getAtom().addRelationship(rel);

            }
          } // End while

          // Close statement
          pstmt.close();

          // Set the inverse flag, this is
          // because we will reverse the
          // rel and rela for the next query
          inverse = true;

        }
        catch (SQLException se) {
          try {
            pstmt.close();
          }
          catch (Exception e) {}
          DataSourceException dse = new DataSourceException(
              "Failed to load relationship.", concept, se);
          dse.setDetail("query",
                        getRelationshipsQuery(ticket, Concept.class, inverse));
          dse.setDetail("concept_id", Integer.toString(concept_id));
          throw dse;
        }
      } // End for
    }

    //
    // Read context relationships
    //
    if (ticket.readContextRelationships()) {

      //
      // Determine context rel queries
      //
      boolean inverse = false;
      int count = 0;
      int start = ticket.getDataTypeStart(Relationship.class);
      int end = ticket.getDataTypeEnd(Relationship.class);
      boolean using_count = start != -1;

      //
      // Iterate over queries
      //
      for (int i = 0; i < 2; i++) {

        try {
          //
          // Prepare and execute queries
          //
          pstmt = prepareStatement(
              getContextRelationshipsQuery(ticket, Concept.class, inverse));
          pstmt.setInt(1, concept_id);
          ResultSet rs = pstmt.executeQuery();

          //
          // Read data
          //
          RelationshipMapper[] rel_mappers =
              (RelationshipMapper[]) (ticket.getDataMapper(ContextRelationship.class)).
              toArray(new RelationshipMapper[0]);
          for (int j = 0; j < rel_mappers.length; j++) {
            rel_mappers[j].setConcept(concept);
            rel_mappers[j].setCoreDataMap(cd_map);
          }
          RelationshipMapper rel_mapper = new ContextRelationshipMapper(true);
          rel_mapper.setConcept(concept);
          rel_mapper.setCoreDataMap(cd_map);
          while (rs.next()) {
            count++;

            //
            // Skip excluded language relationship
            //
            if (!rs.getString("RELATIONSHIP_LEVEL").equals("C") &&
                excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_1")))) {
              continue;
            }

            if (!rs.getString("RELATIONSHIP_LEVEL").equals("C") &&
                excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_2")))) {
              continue;
            }

            //
            // If reading only a range, continue if index is in range
            //

            if (using_count && count <= start) {
              continue;
            }
            if (using_count && count > end) {
              break;
            }

            Relationship cxt_rel = null;

            //
            //  Construct and map context rels
            //
            boolean found = false;
            if (ticket.mapDataType(Relationship.class)) {
              for (int j = 0; j < rel_mappers.length; j++) {
                if ( (cxt_rel = rel_mappers[j].map(rs, this)) != null) {
                  found = true;
                  break;
                }
              }
            }
            //
            // If relationship was not populated, use default mapper
            //
            if (!found) {
              cxt_rel = rel_mapper.map(rs, this);

              // inverse sense of rel and rela
            }
            if (inverse) {
              cxt_rel.setName(
                  getInverseRelationshipName(cxt_rel.getName()));
              cxt_rel.setAttribute(
                  getInverseRelationshipAttribute(cxt_rel.getAttribute()));
            }

            //
            // If we are reading relationship names
            // then just get fields from the query.
            //
            if (ticket.readRelationshipNames()) {
              Atom atom = pref_atom_mapper.map(rs, this);
              cxt_rel.setRelatedAtom(atom);
              cxt_rel.setRelatedConcept(new Concept.Default(rs.getInt(
                  "CONCEPT_ID_2")));
              cxt_rel.getRelatedConcept().setPreferredAtom(atom);
            }

            //
            // Hash context rel for later use
            //
            cd_map.put("R" + cxt_rel.getIdentifier(), cxt_rel);

            //
            // Connect atom and rel
            //
            if (cxt_rel.isAtomLevel() && cxt_rel.getAtom() != null) {
              cxt_rel.getAtom().addContextRelationship( (ContextRelationship)
                  cxt_rel);

            }
          } // End while

          //
          // Close statement
          //
          pstmt.close();

          //
          // Set the inverse flag
          //
          inverse = true;

        }
        catch (SQLException se) {
          try {
            pstmt.close();
          }
          catch (Exception e) {}
          DataSourceException dse = new DataSourceException(
              "Failed to load context relationship.", concept, se);
          dse.setDetail("query",
                        getContextRelationshipsQuery(ticket, Concept.class,
              inverse));
          dse.setDetail("concept_id", Integer.toString(concept_id));
          throw dse;
        }

      } // End for
    }

    //
    // Read attributes
    //
    if (ticket.readAttributes()) {

      int count = 0;
      int start = ticket.getDataTypeStart(Attribute.class);
      int end = ticket.getDataTypeEnd(Attribute.class);
      boolean using_count = start != -1;

      try {
        //
        // Prepare and execute READ_ATTRIBUTES query
        //
        pstmt = prepareStatement(getAttributesQuery(ticket, Concept.class));
        pstmt.setInt(1, concept_id);
        ResultSet rs = pstmt.executeQuery();

        //
        // Read data
        //
        AttributeMapper[] mappers = (AttributeMapper[]) (ticket.getDataMapper(
            Attribute.class)).toArray(new AttributeMapper[0]);
        for (int i = 0; i < mappers.length; i++) {
          mappers[i].setConcept(concept);
          mappers[i].setCoreDataMap(cd_map);
        }
        AttributeMapper default_mapper = new AttributeMapper.Default(true);
        default_mapper.setConcept(concept);
        default_mapper.setCoreDataMap(cd_map);
        LongAttributeMapper long_attribute_mapper = new LongAttributeMapper(this);
        while (rs.next()) {
          count++;

          //
          // Skip excluded language attribute
          //
          if (rs.getString("ATTRIBUTE_LEVEL").equals("S") &&
              excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID")))) {
            continue;
          }

          //
          // If reading only a range, continue if index is in range
          //

          if (using_count && count <= start) {
            continue;
          }
          if (using_count && count > end) {
            break;
          }

          //
          // Create and populate attribute
          //
          Attribute attr = null;
          boolean found = false;
          if (ticket.mapDataType(Attribute.class)) {
            for (int i = 0; i < mappers.length; i++) {
              if ( (attr = mappers[i].map(rs, this)) != null) {
                found = true;
                break;
              }
            }
          }
          //
          // If attribute was not populated, use default mapper
          //
          if (!found) {
            attr = default_mapper.map(rs, this);

            //
            // If the attribute is a long attribute, get the full value
            //
          }
          if (attr.getValue() != null &&
                  attr.getValue().startsWith("<>Long_Attribute<>:") &&
                  ticket.expandLongAttributes()) {
                try {
                  long_attribute_mapper.populate(rs, this, attr);
                }
                catch (SQLException se) {
                  long_attribute_mapper.close();
                  DataSourceException dse = new DataSourceException(
                      "Failed to load long attribute.", attr, se);
                  throw dse;
                }
              }
          
          //
          // Connect atom and attribute
          //
          if (attr.isAtomLevel() && attr.getAtom() != null) {
            attr.getAtom().addAttribute(attr);

          }
        }

        //
        // Close statements
        //
        long_attribute_mapper.close();
        pstmt.close();

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load the attribute.", concept, se);
        dse.setDetail("query", getAttributesQuery(ticket, Concept.class));
        dse.setDetail("concept_id_id", Integer.toString(concept_id));
        throw dse;
      }
    }

    //
    // If we are expanding a graph, clear the cache
    // if this is a top-level call, otherwise
    // add the current concept to the cache.
    //
    if (ticket.expandGraph() && ticket.isTopRecursiveLevel()) {
      ticket.clearCache();
    }
  }

  /**
   * Determines the query used to read atoms.
   * @param ticket the {@link Ticket}.
   * @param type the {@link Class}.
   * @return the query string.
   */
  private String getAtomsQuery(Ticket ticket, Class type) {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    String data_type_query = ticket.getDataTypeQuery(Atom.class);
    if (type == Concept.class) {
      if (ticket.readAtomNames()) {
        return READ_ATOMS_WITH_NAME +
            ticket.getDataTypeRestriction(Atom.class) +
            ticket.getDataTypeOrderBy(Atom.class);
      }
      else {
        return READ_ATOMS +
            ticket.getDataTypeRestriction(Atom.class) +
            ticket.getDataTypeOrderBy(Atom.class);
      }
    }
    else if (data_type_query != null) {
      return data_type_query;
    }
    else if (ticket.readAtomNames()) {
      return READ_ATOM_WITH_NAME_COLUMNS +
          (ticket.readDeleted() ? "dead_" : "") +
          ticket.getDataTable(Atom.class) +
          READ_ATOM_WITH_NAME_CONDITIONS +
          ticket.getDataTypeRestriction(Atom.class) +
          ticket.getDataTypeOrderBy(Atom.class);
    }
    else {
      return READ_ATOM_COLUMNS +
          (ticket.readDeleted() ? "dead_" : "") +
          ticket.getDataTable(Atom.class) +
          READ_ATOM_CONDITIONS +
          ticket.getDataTypeRestriction(Atom.class) +
          ticket.getDataTypeOrderBy(Atom.class);
    }

  }

  /**
   * Implements {@link MEMEDataSource#getAtom(int,Ticket)}.
   * @param atom_id the atom id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtom(int atom_id, Ticket ticket) throws DataSourceException,
      BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    Atom atom = null;
    boolean flag = false;

    if (ticket.mapDataType(Atom.class)) {
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(getAtomsQuery(ticket, Atom.class));
        pstmt.setInt(1, atom_id);
        ResultSet rs = pstmt.executeQuery();

        // Create atom object and assign it an id
        AtomMapper[] mappers = (AtomMapper[])
            ticket.getDataMapper(Atom.class).toArray(new AtomMapper[0]);
        AtomMapper default_mapper = new AtomMapper.Default();

        while (rs.next()) {
          for (int i = 0; i < mappers.length; i++) {
            atom = mappers[i].map(rs, this);
            if (atom != null) {
              break;
            }
          }
          // If atom is still null, use default mapper
          if (atom == null) {
            atom = default_mapper.map(rs, this);

          }
          if (ticket.readAtomNames() && atom.toString() == null) {
            atom.setString(rs.getString("STRING"));
          }

          // concept must be set
          atom.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));
        }

        pstmt.close();
        flag = false;

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load atom.", atom, se);
        dse.setDetail("query", getAtomsQuery(ticket, Atom.class));
        dse.setDetail("atom_id", Integer.toString(atom_id));
        throw dse;
      }

    }
    else {
      atom = new Atom.Default(atom_id);
      flag = true;
    }

    // Populate the atom, ensure that
    // readAtoms toggle is off, but reset
    // it after the populate call.
    boolean read_atoms = ticket.readAtoms();
    ticket.setReadAtoms(flag);
    populateAtom(atom, ticket);
    ticket.setReadAtoms(read_atoms);

    return atom;
  }

  /**
   * Implements {@link MEMEDataSource#getAtom(AUI, Ticket)}.
   * @param aui the {@link AUI}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtom(AUI aui, Ticket ticket) throws DataSourceException,
      BadValueException {

    // Query to get atom by aui
    final String query = "SELECT atom_id FROM classes " +
        "WHERE aui = ? AND tobereleased IN ('Y','y')";

    int atom_id = 0;

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, aui.toString());
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        atom_id = rs.getInt(1);
      }

      // Close statement
      pstmt.close();

      if (atom_id == 0) {
        // No match found
        MissingDataException dse = new MissingDataException("Missing Data.");
        dse.setDetail("aui", aui.toString());
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to look up atom.", aui, se);
      dse.setDetail("query", query);
      dse.setDetail("aui", aui.toString());
      throw dse;
    }

    // Atom id found
    return getAtom(atom_id, ticket);

  }

  /**
   * Implements {@link MEMEDataSource#getAtomWithName(int,Ticket)}.
   * @param atom_id the atom id.
   * @return the {@link Atom}.
   * @param ticket the {@link Ticket}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtomWithName(int atom_id, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

      // Populate and return the atom, making sure that atom names are read
    }
    boolean read_atom_names = ticket.readAtomNames();
    ticket.setReadAtomNames(true);
    Atom atom = getAtom(atom_id, ticket);
    ticket.setReadAtomNames(read_atom_names);
    return atom;
  }

  /**
   * Implements {@link MEMEDataSource#getAtomWithName(Code, Termgroup, Ticket)}.
   * @param code the {@link Code}.
   * @param termgroup the {@link Termgroup}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtomWithName(Code code, Termgroup termgroup, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Get the atom to populate by looking up atom_id
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    Atom atom = null;
    String query = READ_ATOM_WITH_NAME_BY_CODE_TERMGROUP;
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, code.toString());
      pstmt.setString(2, termgroup.toString());
      ResultSet rs = pstmt.executeQuery();

      // Create atom object and assign it an id
      int row_count = 0;
      AtomMapper[] mappers = (AtomMapper[])
          ticket.getDataMapper(Atom.class).toArray(new AtomMapper[0]);
      AtomMapper default_mapper = new AtomMapper.Default();

      while (rs.next()) {
        if (ticket.mapDataType(Atom.class)) {
          for (int i = 0; i < mappers.length; i++) {
            atom = mappers[i].map(rs, this);
            if (atom != null) {
              break;
            }
          }
        }
        // If atom is still null, use default mapper
        if (atom == null) {
          atom = default_mapper.map(rs, this);

        }
        atom.setString(rs.getString("STRING"));
        atom.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));
        row_count++;
        break;
      }
      pstmt.close();

      // Populate the atom, making sure to not
      // re-read atom information.  Reset
      // ticket settings after populate call
      boolean read_atoms = ticket.readAtoms();
      ticket.setReadAtoms(false);
      populateAtom(atom, ticket);
      ticket.setReadAtoms(read_atoms);
      return atom;
    }
    catch (NullPointerException npe) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load atom (null atom).", null, null);
      dse.setDetail("query", query.toString());
      dse.setDetail("code", code.toString());
      dse.setDetail("termgroup", termgroup.toString());
      throw dse;
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load atom.", atom, se);
      dse.setDetail("query", query.toString());
      dse.setDetail("code", code.toString());
      dse.setDetail("termgroup", termgroup.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getAtomWithName(AUI, Ticket)}.
   * @param aui the {@link AUI}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtomWithName(AUI aui, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Query to get atom by aui
    final String query = "SELECT atom_id FROM classes " +
        "WHERE aui = ? AND tobereleased IN ('Y','y')";

    int atom_id = 0;
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, aui.toString());
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        atom_id = rs.getInt(1);
      }

      // Close statement
      pstmt.close();

      if (atom_id == 0) {
        // No match found
        MissingDataException dse = new MissingDataException("Missing Data.");
        dse.setDetail("aui", aui.toString());
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to look up atom.", aui, se);
      dse.setDetail("query", query);
      dse.setDetail("aui", aui.toString());
      throw dse;
    }

    // Atom id found
    return getAtomWithName(atom_id, ticket);

  }

  /**
   * Implements {@link MEMEDataSource#getPreferredAtom(Concept)}.
   * @param concept a {@link Concept}.
   * @return the preferred {@link Atom} of the specified {@link Concept}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getPreferredAtom(Concept concept) throws DataSourceException,
      BadValueException {

    MEMEToolkit.trace(
        "MEMEConnection.getPreferredAtom(Concept)...");
    int concept_id = concept.getIdentifier().intValue();

    String query = READ_PREFERRED_ATOM;
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setInt(1, concept_id);
      ResultSet rs = pstmt.executeQuery();

      AtomMapper mapper = new PreferredAtomMapper();
      Atom atom = null;

      int row_count = 0;

      // Read
      while (rs.next()) {
        atom = mapper.map(rs, this);
        row_count++;
      }

      // Close statement
      pstmt.close();

      // There should have more than 0 rows
      if (row_count == 0) {
        MissingDataException dse = new MissingDataException("Missing Data.");
        dse.setDetail("concept_id", Integer.toString(concept_id));
        throw dse;
      }

      // Return
      return atom;

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load atom.", concept, se);
      dse.setDetail("query", query);
      dse.setDetail("concept_id", Integer.toString(concept_id));
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#populateAtom(Atom, Ticket)}.
   * @param atom An {@link Atom}.
   * @param ticket A {@link Ticket}.
   * @throws DataSourceException if failed to populate atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateAtom(Atom atom, Ticket ticket) throws DataSourceException,
      BadValueException {

    String language = null;

    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    if (atom.getIdentifier() == null) {
      throw new BadValueException("Atom must have an Identifier.");
    }

    MEMEToolkit.trace(
        "MEMEConnection.populateAtom(Atom, Ticket)..." + atom.getIdentifier());

    Set excluded_atoms = new HashSet();

    if (!ticket.readAtoms() &&
        (atom.getConcept() == null || atom.getConcept().getIdentifier() == null)) {
      throw new BadValueException("Atom must have a concept identifier.");
    }

    // Prepare variables
    PreparedStatement pstmt = null;
    int atom_id = atom.getIdentifier().intValue();
    Map cd_map = new HashMap();
    cd_map.put("C" + atom_id, atom);
    int concept_id = 0;
    int row_count = 0;

    if (ticket.readAtoms()) {
      // Empty the atom
      atom.clear();

      String query = null;
      try {
        query = getAtomsQuery(ticket, Atom.class);
        pstmt = prepareStatement(query);
        pstmt.setInt(1, atom_id);
        ResultSet rs = pstmt.executeQuery();

        row_count = 0;
        AtomMapper[] mappers = (AtomMapper[]) (ticket.getDataMapper(Atom.class)).
            toArray(new AtomMapper[0]);
        AtomMapper default_mapper = new AtomMapper.Default();

        // Read
        while (rs.next()) {

          language = rs.getString("LANGUAGE");

          //
          // Hash non-english atom id
          //

          if (!"ENG".equals(language)) {
            excluded_atoms.add(new Integer(rs.getInt("ATOM_ID")));

            //
            // Skip non-english if we are not reading it
            //
            //if (!ticket.readLanguage(language) && !"ENG".equals(language))

            //
            // Skip excluded language
            //
          }
          if (!ticket.readLanguage(language)) {
            continue;
          }

          // Give each mapper a chance to populate the atom
          boolean found = false;
          if (ticket.mapDataType(Atom.class)) {
            for (int i = 0; i < mappers.length; i++) {
              if (mappers[i].populate(rs, this, atom)) {
                found = true;
                break;
              }
            }
          }

          // If relationship was not populated, use default mapper
          if (!found) {
            default_mapper.populate(rs, this, atom);

          }
          row_count++;

          concept_id = rs.getInt("CONCEPT_ID");

          if (ticket.readAtomNames() && atom.toString() == null) {
            atom.setString(rs.getString("STRING"));
            // eventually we will need to read this
            // atom.setNormalizedString(rs.getString("NORM_STRING"));
          }
        }

        // Close statements
        pstmt.close();

        // There should have more than 0 rows
        if (row_count < 1) {
          MissingDataException dse = new MissingDataException("Missing Data.");
          dse.setDetail("atom_id", Integer.toString(atom_id));
          throw dse;
        }

        if (row_count > 1) {
          DataSourceException dse = new DataSourceException(
              "Too much data.");
          dse.setDetail("atom_id", Integer.toString(atom_id));
          throw dse;
        }

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load atom.", atom, se);
        if (ticket.readAtomNames() && !ticket.readDeleted()) {
          dse.setDetail("query", query);
        }
        else if (!ticket.readAtomNames() && !ticket.readDeleted()) {
          dse.setDetail("query", query);
        }
        else if (ticket.readDeleted()) {
          dse.setDetail("query", query);
        }
        dse.setDetail("atom_id", Integer.toString(atom_id));
        throw dse;
      }
    }
    else {
      // If not reading atoms,
      // assume that concept identifier has already been set
      concept_id = atom.getConcept().getIdentifier().intValue();
    }

    // Determine if the atom is ambiguous
    if (ticket.checkAtomAmbiguity()) {
      // Query to check if there are any ambiguous atoms.
      String tmp_ambig_query = "SELECT COUNT(*) AS CT " +
          "FROM classes WHERE isui = ?" +
          " AND concept_id != ?" +
          " AND tobereleased IN ('Y','y')";

      final String ambig_query = tmp_ambig_query +
          " AND (source != 'MTH' OR termgroup NOT LIKE '%PN')";

      final String pn_ambig_query = tmp_ambig_query +
          " AND source = 'MTH' AND termgroup LIKE '%PN'";

      try {

        PreparedStatement pn_ambig_pstmt = prepareStatement(pn_ambig_query);
        PreparedStatement no_pn_ambig_pstmt = prepareStatement(ambig_query);
        PreparedStatement ambig_pstmt = null;

        if (atom.getTermgroup().equals("MTH/PN")) {
          ambig_pstmt = pn_ambig_pstmt;
        }
        else {
          ambig_pstmt = no_pn_ambig_pstmt;

        }
        ambig_pstmt.setString(1, atom.getISUI().toString());
        ambig_pstmt.setInt(2, concept_id);
        ResultSet ambig_rs = ambig_pstmt.executeQuery();
        while (ambig_rs.next()) {
          atom.setIsAmbiguous(ambig_rs.getInt("CT") > 0);
        }

        // Close statements
        pn_ambig_pstmt.close();
        no_pn_ambig_pstmt.close();

      }
      catch (SQLException se) {
        DataSourceException dse = new DataSourceException(
            "Failed to determine if atom is ambiguous.", atom, se);
        if (atom.getTermgroup().toString().equals("MTH/PN")) {
          dse.setDetail("query1", pn_ambig_query);
        }
        else {
          dse.setDetail("query2", ambig_query);
        }
        dse.setDetail("atom_id", Integer.toString(atom_id));
        throw dse;
      }
    }

    // Here we will either read the atom's concept info
    // or just use a dummy placeholder.  The concept_id
    // was saved from the above section.
    //
    Concept concept = new Concept.Default(concept_id);

    if (ticket.readConcept()) {

      String query = getConceptsQuery(ticket);

      try {
        // Prepare and execute READ_CONCEPT query
        pstmt = prepareStatement(query);
        pstmt.setInt(1, concept_id);
        ResultSet rs = pstmt.executeQuery();

        row_count = 0;

        // Read
        while (rs.next()) {
          if (rs.getString("CUI") != null) {
            concept.setCUI(new CUI(rs.getString("CUI")));
          }
          concept.setStatus(rs.getString("STATUS").charAt(0));
          concept.setDead(rs.getString("DEAD").equals("Y"));
          concept.setAuthority(getAuthority(rs.getString("AUTHORITY")));
          concept.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
          concept.setInsertionDate(getDate(rs.getTimestamp("INSERTION_DATE")));
          concept.setPreferredAtom(new Atom.Default(rs.getInt(
              "PREFERRED_ATOM_ID")));
          concept.setReleased(rs.getString("RELEASED").charAt(0));
          concept.setTobereleased(rs.getString("TOBERELEASED").charAt(0));
          concept.setRank(new Rank.Default(rs.getInt("RANK")));
          concept.setEditingAuthority(getAuthority(rs.getString(
              "EDITING_AUTHORITY")));
          concept.setEditingTimestamp(getDate(rs.getTimestamp(
              "EDITING_TIMESTAMP")));

          // last_molecule_id column is currently being ignored
          // last_atomic_action_id column is currently being ignored

          if (ticket.readActions()) {
            int molecule_id = rs.getInt("APPROVAL_MOLECULE_ID");
            MolecularAction action = getMolecularAction(molecule_id);
            if (action == null) {
              action = new MolecularAction(molecule_id);
            }
            concept.setApprovalAction(action);
          }
          row_count++;
        }

        // Close statement
        pstmt.close();

        // There should only have exactly one concept row
        if (row_count == 0) {
          MissingDataException dse = new MissingDataException("Missing Data.");
          dse.setDetail("concept_id", Integer.toString(concept_id));
          throw dse;
        }

        if (row_count > 1) {
          DataSourceException dse = new DataSourceException(
              "Too much data.");
          dse.setDetail("concept_id", Integer.toString(concept_id));
          throw dse;
        }

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load concept.", concept, se);
        dse.setDetail("query", query);
        dse.setDetail("concept_id", Integer.toString(concept_id));
        throw dse;
      }
    }

    // Set the atom's concept now
    atom.setConcept(concept);

    // Read safe replacement
    if (ticket.readSafeReplacement()) {
      Iterator iterator = findSafeReplacementFacts(
          new SearchParameter.Single("new_atom_id", atom.getIdentifier()));
      while (iterator.hasNext()) {
        atom.addSafeReplacementFact( (SafeReplacementFact) iterator.next());
      }
      iterator = findSafeReplacementFacts(
          new SearchParameter.Single("old_atom_id", atom.getIdentifier()));
      while (iterator.hasNext()) {
        atom.addSafeReplacementFact( (SafeReplacementFact) iterator.next());
      }
    }

    PreferredAtomMapper pref_atom_mapper = new PreferredAtomMapper();

    // Read atom relationships
    if (ticket.readRelationships()) {

      boolean inverse = false;

      for (int i = 0; i < 2; i++) {

        try {
          // Prepare and execute queries
          pstmt = prepareStatement(getRelationshipsQuery(ticket, Atom.class,
              inverse));
          pstmt.setInt(1, atom_id);
          ResultSet rs = pstmt.executeQuery();

          RelationshipMapper[] mappers =
              (RelationshipMapper[]) (ticket.getDataMapper(Relationship.class)).
              toArray(new RelationshipMapper[0]);
          for (int j = 0; j < mappers.length; j++) {
            mappers[j].setConcept(concept);
            mappers[j].setCoreDataMap(cd_map);
          }
          RelationshipMapper default_mapper = new RelationshipMapper.Default(true);
          default_mapper.setConcept(concept);
          default_mapper.setCoreDataMap(cd_map);

          // Read
          while (rs.next()) {

            //
            // Skip excluded language relationship
            //
            if (!ticket.readLanguage(language) &&
                excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_1")))) {
              continue;
            }

            if (!ticket.readLanguage(language) &&
                excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_2")))) {
              continue;
            }

            Relationship rel = null;

            //  Give each mapper a chance to populate the relationship
            boolean found = false;
            if (ticket.mapDataType(Relationship.class)) {
              for (int j = 0; j < mappers.length; j++) {
                if ( (rel = mappers[j].map(rs, this)) != null) {
                  found = true;
                  break;
                }
              }
            }

            // If relationship was not populated, use default mapper
            if (!found) {
              rel = default_mapper.map(rs, this);

              // If we are in inverse query, then reverse the sense
              // of all of the relationships
            }
            if (inverse) {
              rel.setName(getInverseRelationshipName(rel.getName()));
              rel.setAttribute(
                  getInverseRelationshipAttribute(rel.getAttribute()));
            }

            rel.setIsInverse(inverse);

            cd_map.put("R" + rel.getIdentifier(), rel);

            //
            // If we are making a graph, then just
            // get the whole atom on the other side.
            //
            // If we are expanding relationship atoms
            // Look up the atom on the other side
            if (ticket.expandGraph()) {
              int atom_id_2 = rs.getInt("ATOM_ID_2");
              ticket.decrementRecursiveLevel();
              Atom atom2 = getAtomWithName(atom_id_2, ticket);
              ticket.incrementRecursiveLevel();
              rel.setRelatedAtom(atom2);
              rel.setRelatedConcept(atom2.getConcept());
            }

            //
            // If we are expanding relationships
            // Look up the preferred atom on the
            // other side of the relationship
            //
            else if (ticket.readRelationshipNames()) {
              int atom_id_2 = rs.getInt("ATOM_ID_2");
              int concept_id_2 = rs.getInt("CONCEPT_ID_2");
              Concept rc = new Concept.Default(concept_id_2);
              if (rs.getString("STRING") != null) {
                rc.setPreferredAtom(pref_atom_mapper.map(rs, this));
              }
              else {
                try {
                  rc.setPreferredAtom(getPreferredAtom(rc));
                }
                catch (MissingDataException mde) {
                  Atom epa = new Atom.Default( -1);
                  epa.setLUI(new LUI("L0000000"));
                  epa.setSUI(new SUI("S0000000"));
                  epa.setISUI(new ISUI("I0000000"));
                  epa.setConcept(rc);
                  epa.setCode(new Code("NOCODE"));
                  epa.setSource(getSource("MTH"));
                  epa.setTermgroup(getTermgroup("MTH/PT"));
                  epa.setString("This concept has no preferred atom.");
                  rc.setPreferredAtom(epa);
                }
              }
              rel.setRelatedConcept(rc);
              rel.setRelatedAtom(new Atom.Default(atom_id_2));
            }

            //
            // Add rel to atom
            //
            if (rel.isAtomLevel() && rel.getAtom() != null) {
              rel.getAtom().addRelationship(rel);

            }
          } // End while

          // Close statement
          pstmt.close();

          // Set the inverse flag, this is
          // because we will reverse the
          // rel and rela for the next query
          inverse = true;

        }
        catch (SQLException se) {
          try {
            pstmt.close();
          }
          catch (Exception e) {}
          DataSourceException dse = new DataSourceException(
              "Failed to load relationship.", atom, se);
          dse.setDetail("query",
                        getRelationshipsQuery(ticket, Atom.class, inverse));
          dse.setDetail("atom_id", Integer.toString(atom_id));
          throw dse;
        }
      } // End for
    }

    // Read context relationships
    if (ticket.readContextRelationships()) {

      boolean inverse = false;

      for (int i = 0; i < 2; i++) {

        try {
          // Prepare and execute queries
          pstmt = prepareStatement(
              getContextRelationshipsQuery(ticket, Atom.class, inverse));
          pstmt.setInt(1, atom_id);
          ResultSet rs = pstmt.executeQuery();

          RelationshipMapper[] mappers =
              (RelationshipMapper[]) (ticket.getDataMapper(ContextRelationship.class)).
              toArray(new RelationshipMapper[0]);
          for (int j = 0; j < mappers.length; j++) {
            mappers[j].setConcept(concept);
            mappers[j].setCoreDataMap(cd_map);
          }
          RelationshipMapper default_mapper = new ContextRelationshipMapper(true);
          default_mapper.setConcept(concept);
          default_mapper.setCoreDataMap(cd_map);

          // Read
          while (rs.next()) {

            //
            // Skip excluded language relationship
            //
            if (!ticket.readLanguage(language) &&
                excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_1")))) {
              continue;
            }

            if (!ticket.readLanguage(language) &&
                excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_2")))) {
              continue;
            }

            Relationship cxt_rel = null;

            //  Give each mapper a chance to populate the relationship
            boolean found = false;
            if (ticket.mapDataType(ContextRelationship.class)) {
              for (int j = 0; j < mappers.length; j++) {
                if ( (cxt_rel = mappers[j].map(rs, this)) != null) {
                  found = true;
                  break;
                }
              }
            }

            // If relationship was not populated, use default mapper
            if (!found) {
              cxt_rel = default_mapper.map(rs, this);

              // inverse sense of rel and rela
            }
            if (inverse) {
              cxt_rel.setName(
                  getInverseRelationshipName(cxt_rel.getName()));
              cxt_rel.setAttribute(
                  getInverseRelationshipAttribute(cxt_rel.getAttribute()));
            }

            cd_map.put("CR" + cxt_rel.getIdentifier(), cxt_rel);

            if (ticket.readRelationshipNames()) {
              Atom atom2 = pref_atom_mapper.map(rs, this);
              cxt_rel.setRelatedAtom(atom2);
            }
            else {
              int atom_id_2 = rs.getInt("ATOM_ID_2");
              cxt_rel.setRelatedAtom(new Atom.Default(atom_id_2));
            }

            //
            // Add cxt rel to atom
            //
            if (cxt_rel.isAtomLevel() && cxt_rel.getAtom() != null) {
              cxt_rel.getAtom().addContextRelationship( (ContextRelationship)
                  cxt_rel);

            }
          } // End while

          // Close statement
          pstmt.close();

          // Set the inverse flag
          inverse = true;

        }
        catch (SQLException se) {
          try {
            pstmt.close();
          }
          catch (Exception e) {}
          DataSourceException dse = new DataSourceException(
              "Failed to load context relationship.", atom, se);
          dse.setDetail("query",
                        getContextRelationshipsQuery(ticket, Atom.class,
              inverse));
          dse.setDetail("atom_id", Integer.toString(atom_id));
          throw dse;
        }

      } // End for
    }

    // Read attributes connected to the atom.
    if (ticket.readAttributes()) {

      String query = getAttributesQuery(ticket, Atom.class);

      try {

        // Prepare and execute READ_ATTRIBUTES query
        pstmt = prepareStatement(query);
        pstmt.setInt(1, atom_id);
        ResultSet rs = pstmt.executeQuery();

        AttributeMapper[] mappers = (AttributeMapper[]) (ticket.getDataMapper(
            Attribute.class)).toArray(new AttributeMapper[0]);
        for (int i = 0; i < mappers.length; i++) {
          mappers[i].setConcept(concept);
          mappers[i].setCoreDataMap(cd_map);
        }
        AttributeMapper default_mapper = new AttributeMapper.Default(true);
        default_mapper.setConcept(concept);
        default_mapper.setCoreDataMap(cd_map);
        LongAttributeMapper long_attribute_mapper = new LongAttributeMapper(this);

        // Read
        while (rs.next()) {

          //
          // Skip excluded language attribute
          //
          if (!ticket.readLanguage(language) &&
              excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID")))) {
            continue;
          }

          Attribute attr = null;

          // Give each mapper a chance to populate the attribute
          boolean found = false;
          if (ticket.mapDataType(Attribute.class)) {
            for (int i = 0; i < mappers.length; i++) {
              if ( (attr = mappers[i].map(rs, this)) != null) {
                found = true;
                break;
              }
            }
          }

          // If attribute was not populated, use default mapper
          if (!found) {
            attr = default_mapper.map(rs, this);

            // If the attribute is a long attribute, get the full value
          }
          if (attr.getValue() != null &&
              attr.getValue().startsWith("<>Long_Attribute<>:") &&
              ticket.expandLongAttributes()) {
            try {
              long_attribute_mapper.populate(rs, this, attr);
            }
            catch (SQLException se) {
              long_attribute_mapper.close();
              DataSourceException dse = new DataSourceException(
                  "Failed to load long attribute.", attr, se);
              throw dse;
            }
          }

          //
          // Add attribute
          //
          if (attr.isAtomLevel() && attr.getAtom() != null) {
            attr.getAtom().addAttribute(attr);

          }
        }

        // Close statement
        long_attribute_mapper.close();
        pstmt.close();

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load the attribute.", atom, se);
        dse.setDetail("query", query);
        dse.setDetail("atom_id", Integer.toString(atom_id));
        throw dse;
      }
    }

  } // End populateAtom

  /**
   * Determines the query used to read attributes.
   * @param ticket the {@link Ticket}.
   * @param type the {@link Class}.
   * @return the query string.
   */
  private String getAttributesQuery(Ticket ticket, Class type) {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    String data_type_query = ticket.getDataTypeQuery(Attribute.class);
    if (type == Concept.class) {
      return READ_ATTRIBUTES +
          ticket.getDataTypeRestriction(Attribute.class) +
          (ticket.readRelationships() ? "" :
           " AND NVL(sg_type,'null') NOT IN ('SOURCE_RUI', 'RUI', 'ROOT_SOURCE_RUI', 'SRUI', 'ROOT_SRUI')") +
          ticket.getDataTypeOrderBy(Attribute.class);
    }
    else if (type == Atom.class) {
      return READ_ATOM_ATTRIBUTES +
          ticket.getDataTypeRestriction(Attribute.class) +
          (ticket.readRelationships() ? "" :
           " AND NVL(sg_type,'null') NOT IN ('SOURCE_RUI', 'RUI', 'ROOT_SOURCE_RUI', 'SRUI', 'ROOT_SRUI')") +
          ticket.getDataTypeOrderBy(Attribute.class);
    }
    else if (type == Relationship.class) {
      return READ_RELATIONSHIP_ATTRIBUTES +
          ticket.getDataTypeRestriction(Attribute.class) +
          ticket.getDataTypeOrderBy(Attribute.class);
    }
    else if (data_type_query != null) {
      return data_type_query;
    }
    else {
      return READ_ATTRIBUTE_COLUMNS +
          (ticket.readDeleted() ? "dead_" : "") +
          ticket.getDataTable(Attribute.class) +
          READ_ATTRIBUTE_CONDITIONS +
          ticket.getDataTypeRestriction(Attribute.class) +
          ticket.getDataTypeOrderBy(Attribute.class);
    }
  }

  /**
   * Implements {@link MEMEDataSource#populateAttribute(Attribute, Ticket)}.
   * @param attr An {@link Attribute}.
   * @param ticket A {@link Ticket}.
   * @throws DataSourceException if failed to populate attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateAttribute(Attribute attr, Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    if (attr.getIdentifier() == null) {
      throw new BadValueException("Attribute must have an Identifier.");
    }

    MEMEToolkit.trace(
        "MEMEConnection.populateAttribute(Attribute, Ticket)..."
        + attr.getIdentifier());

    int attr_id = attr.getIdentifier().intValue();
    String query = null;
    int row_count = 0;

    PreparedStatement pstmt = null;
    try {

      query = getAttributesQuery(ticket, Attribute.class);
      pstmt = prepareStatement(query);
      pstmt.setInt(1, attr_id);
      ResultSet rs = pstmt.executeQuery();

      AttributeMapper[] mappers = (AttributeMapper[]) (ticket.getDataMapper(
          Attribute.class)).toArray(new AttributeMapper[0]);
      AttributeMapper default_mapper = new AttributeMapper.Default(true);
      LongAttributeMapper long_attribute_mapper = new LongAttributeMapper(this);

      // Read
      while (rs.next()) {

        // Give each mapper a chance to populate the attribute
        boolean found = false;
        if (ticket.mapDataType(Attribute.class)) {
          for (int i = 0; i < mappers.length; i++) {
            if (mappers[i].populate(rs, this, attr)) {
              found = true;
              break;
            }
          }
        }

        // If attribute was not populated, use default mapper
        if (!found) {
          default_mapper.populate(rs, this, attr);

          // If the attribute is a long attribute, get the full value
        }
        if (attr.getValue() != null &&
            attr.getValue().startsWith("<>Long_Attribute<>:") &&
            ticket.expandLongAttributes()) {
          try {
            long_attribute_mapper.populate(rs, this, attr);
          }
          catch (SQLException se) {
            long_attribute_mapper.close();
            DataSourceException dse = new DataSourceException(
                "Failed to load long attribute.", attr, se);
            throw dse;
          }
        }

        row_count++;
      }

      // Close statement
      long_attribute_mapper.close();
      pstmt.close();

      // There should have more than 0 rows
      if (row_count < 1) {
        MissingDataException dse = new MissingDataException("Missing Data.");
        dse.setDetail("attribute_id", Integer.toString(attr_id));
        throw dse;
      }

      if (row_count > 1) {
        DataSourceException dse = new DataSourceException(
            "Too much data.");
        dse.setDetail("attribute_id", Integer.toString(attr_id));
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load the attribute.", attr, se);
      dse.setDetail("query", query);
      dse.setDetail("attribute_id", Integer.toString(attr_id));
      throw dse;
    }
  }

  /**
   * Determines the query used to read relationship.
   * @param ticket the {@link Ticket}
   * @param type the {@link Class}
   * @param inverse returns <code>true</code> if inverse relationships;
   *        <code>false</code> otherwise
   * @return the query
   */
  private String getRelationshipsQuery(
      Ticket ticket, Class type, boolean inverse) {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    String data_type_query = ticket.getDataTypeQuery(Relationship.class);
    if (type == Concept.class) {
      if (ticket.readRelationshipNames()) {
        if (!inverse) {
          return READ_RELATIONSHIPS_WITH_NAMES_1 +
              ticket.getDataTypeRestriction(Relationship.class) +
              ticket.getDataTypeOrderBy(Relationship.class);
        }
        else {
          return READ_RELATIONSHIPS_WITH_NAMES_2 +
              ticket.getDataTypeRestriction(Relationship.class) +
              ticket.getDataTypeOrderBy(Relationship.class);
        }
      }
      else {
        if (!inverse) {
          return READ_RELATIONSHIPS_1 +
              ticket.getDataTypeRestriction(Relationship.class) +
              ticket.getDataTypeOrderBy(Relationship.class);
        }
        else {
          return READ_RELATIONSHIPS_2 +
              ticket.getDataTypeRestriction(Relationship.class) +
              ticket.getDataTypeOrderBy(Relationship.class);
        }
      }
    }
    else if (type == Atom.class) {
      if (ticket.readRelationshipNames()) {
        if (!inverse) {
          return READ_ATOM_RELATIONSHIPS_WITH_NAMES_1 +
              ticket.getDataTypeRestriction(Relationship.class) +
              ticket.getDataTypeOrderBy(Relationship.class);
        }
        else {
          return READ_ATOM_RELATIONSHIPS_WITH_NAMES_2 +
              ticket.getDataTypeRestriction(Relationship.class) +
              ticket.getDataTypeOrderBy(Relationship.class);
        }
      }
      else
      if (!inverse) {
        return READ_ATOM_RELATIONSHIPS_1 +
            ticket.getDataTypeRestriction(Relationship.class) +
            ticket.getDataTypeOrderBy(Relationship.class);
      }
      else {
        return READ_ATOM_RELATIONSHIPS_2 +
            ticket.getDataTypeRestriction(Relationship.class) +
            ticket.getDataTypeOrderBy(Relationship.class);
      }
    }
    else if (data_type_query != null) {
      return data_type_query;
    }
    else {
      if (!inverse) {
        return READ_RELATIONSHIP_COLUMNS +
            (ticket.readDeleted() ? "dead_" : "") +
            ticket.getDataTable(Relationship.class) +
            READ_RELATIONSHIP_CONDITIONS +
            ticket.getDataTypeRestriction(Relationship.class) +
            ticket.getDataTypeOrderBy(Relationship.class);
      }
      else {
        return READ_INVERSE_REL_COLUMNS +
            (ticket.readDeleted() ? "dead_" : "") +
            ticket.getDataTable(Relationship.class) +
            READ_INVERSE_REL_CONDITIONS +
            ticket.getDataTypeRestriction(Relationship.class) +
            ticket.getDataTypeOrderBy(Relationship.class);
      }
    }
  }

  /**
   * Implements {@link MEMEDataSource#populateRelationship(Relationship, Ticket)}.
   * @param rel An {@link Relationship}.
   * @param ticket A {@link Ticket}.
   * @throws DataSourceException if failed to populate relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateRelationship(Relationship rel, Ticket ticket) throws
      DataSourceException, BadValueException {
    populateRelationship(rel, ticket, true);
  }

  /**
   * Implements {@link MEMEDataSource#populateRelationship(Relationship, Ticket)}.
   * @param rel An {@link Relationship}.
   * @param ticket A {@link Ticket}.
   * @param inverse <code>true</code> if read from inverse relationships;
   *   <code>false</code> otherwise.
   * @throws DataSourceException if failed to populate relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  private void populateRelationship(Relationship rel, Ticket ticket,
                                    boolean inverse) throws DataSourceException,
      BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    if (rel.getIdentifier() == null) {
      throw new BadValueException("Relationship must have an Identifier.");
    }

    MEMEToolkit.trace(
        "MEMEConnection.populateRelationship(Relationship, Ticket)..."
        + rel.getIdentifier());

    int rel_id = rel.getIdentifier().intValue();
    Map cd_map = new HashMap();
    cd_map.put("R" + rel_id, rel);

    PreparedStatement pstmt = null;
    String query = null;
    int row_count = 0;

    try {
      query = getRelationshipsQuery(ticket, Relationship.class, inverse);
      pstmt = prepareStatement(query);
      pstmt.setInt(1, rel_id);
      ResultSet rs = pstmt.executeQuery();

      row_count = 0;
      RelationshipMapper[] mappers = (RelationshipMapper[]) (ticket.
          getDataMapper(Relationship.class)).toArray(new RelationshipMapper[0]);
      RelationshipMapper default_mapper = new RelationshipMapper.Default(true);

      // Read
      while (rs.next()) {

        // Give each mapper a chance to populate the relationship
        boolean found = false;
        if (ticket.mapDataType(Relationship.class)) {
          for (int i = 0; i < mappers.length; i++) {
            if (mappers[i].populate(rs, this, rel)) {
              found = true;
              break;
            }
          }
        }

        // If relationship was not populated, use default mapper
        if (!found) {
          default_mapper.populate(rs, this, rel);

        }
        row_count++;
      }

      // Close statement
      pstmt.close();

      // There should have more than 0 rows
      if (row_count < 1) {
        MissingDataException dse = new MissingDataException("Missing Data.");
        dse.setDetail("relationship_id", Integer.toString(rel_id));
        throw dse;
      }

      if (row_count > 1) {
        DataSourceException dse = new DataSourceException(
            "Too much data.");
        dse.setDetail("relationship_id", Integer.toString(rel_id));
        throw dse;
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load relationship.", rel, se);
      dse.setDetail("query", query);
      dse.setDetail("relationship_id", Integer.toString(rel_id));
      throw dse;
    }

    // Read attributes connected to the relationship.
    if (ticket.readAttributes()) {

      query = getAttributesQuery(ticket, Relationship.class);

      try {

        // Prepare and execute READ_ATTRIBUTES query
        pstmt = prepareStatement(query);
        pstmt.setInt(1, rel_id);
        pstmt.setInt(2, rel.getConcept().getIdentifier().intValue());
        ResultSet rs = pstmt.executeQuery();

        AttributeMapper[] mappers = (AttributeMapper[])
            (ticket.getDataMapper(Attribute.class)).toArray(new AttributeMapper[
            0]);
        for (int i = 0; i < mappers.length; i++) {
          mappers[i].setConcept(rel.getConcept());
          mappers[i].setCoreDataMap(cd_map);
        }
        AttributeMapper default_mapper = new AttributeMapper.Default(true);
        default_mapper.setConcept(rel.getConcept());
        default_mapper.setCoreDataMap(cd_map);
        LongAttributeMapper long_attribute_mapper = new LongAttributeMapper(this);

        // Read
        while (rs.next()) {

          Attribute attr = null;

          // Give each mapper a chance to populate the attribute
          boolean found = false;
          if (ticket.mapDataType(Attribute.class)) {
            for (int i = 0; i < mappers.length; i++) {
              if ( (attr = mappers[i].map(rs, this)) != null) {
                found = true;
                break;
              }
            }
          }

          // If attribute was not populated, use default mapper
          if (!found) {
            attr = default_mapper.map(rs, this);

            // If the attribute is a long attribute, get the full value
          }
          if (attr.getValue() != null &&
              attr.getValue().startsWith("<>Long_Attribute<>:") &&
              ticket.expandLongAttributes()) {
            try {
              long_attribute_mapper.populate(rs, this, attr);
            }
            catch (SQLException se) {
              long_attribute_mapper.close();
              DataSourceException dse = new DataSourceException(
                  "Failed to load long attribute.", attr, se);
              throw dse;
            }
          }

        }

        // Close statement
        long_attribute_mapper.close();
        pstmt.close();

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load the attribute.", rel, se);
        dse.setDetail("query", query);
        dse.setDetail("relationship_id", Integer.toString(rel_id));
        throw dse;
      }
    }

  }

  /**
   * Implements {@link MEMEDataSource#populateContextRelationship(ContextRelationship, Ticket)}.
   * @param cxt_rel An {@link ContextRelationship}.
   * @param ticket A {@link Ticket}.
   * @throws DataSourceException if failed to populate context relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateContextRelationship(ContextRelationship cxt_rel,
                                          Ticket ticket) throws
      DataSourceException, BadValueException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    if (cxt_rel.getIdentifier() == null) {
      throw new BadValueException(
          "Context Relationship must have an Identifier.");
    }

    MEMEToolkit.trace(
        "MEMEConnection.populateContextRelationship(ContextRelationship, Ticket)..."
        + cxt_rel.getIdentifier());

    int rel_id = cxt_rel.getIdentifier().intValue();

    PreparedStatement pstmt = null;
    int row_count = 0;
    String query = null;

    try {
      query = getContextRelationshipsQuery(ticket, ContextRelationship.class, false);
      pstmt = prepareStatement(query);
      pstmt.setInt(1, rel_id);
      ResultSet rs = pstmt.executeQuery();

      row_count = 0;
      RelationshipMapper[] mappers = (RelationshipMapper[])
          (ticket.getDataMapper(ContextRelationship.class)).toArray(new
          RelationshipMapper[0]);

      RelationshipMapper default_mapper = new ContextRelationshipMapper(true);

      // Read
      while (rs.next()) {

        // Give each mapper a chance to populate the relationship
        boolean found = false;
        if (ticket.mapDataType(ContextRelationship.class)) {
          for (int i = 0; i < mappers.length; i++) {
            if (mappers[i].populate(rs, this, cxt_rel)) {
              found = true;
              break;
            }
          }
        }

        // If context relationship was not populated, use default mapper
        if (!found) {
          default_mapper.populate(rs, this, cxt_rel);

        }
        row_count++;
      }

      // Close statement
      pstmt.close();

      // There should have more than 0 rows
      if (row_count < 1) {
        MissingDataException dse = new MissingDataException("Missing Data.");
        dse.setDetail("relationship_id", Integer.toString(rel_id));
        throw dse;
      }

      if (row_count > 1) {
        DataSourceException dse = new DataSourceException(
            "Too much data.");
        dse.setDetail("relationship_id", Integer.toString(rel_id));
        throw dse;
      }
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load context relationship.", cxt_rel, se);
      dse.setDetail("query", query);
      dse.setDetail("relationship_id", Integer.toString(rel_id));
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getActionEngine()}.
   * @return the {@link ActionEngine}.
   */
  public ActionEngine getActionEngine() {
    return new ActionEngine.Default(this);
  }

  /**
   * Implements {@link MEMEDataSource#getMolecularAction(int)}.
   * @param molecule_id the molecular id.
   * @return the {@link MolecularAction}.
   * @throws DataSourceException if failed to load molecular action.
   */
  public MolecularAction getMolecularAction(int molecule_id) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.getMolecularAction(int)...");

    String query = READ_MOLECULAR_ACTION;
    PreparedStatement ma_pstmt = null;
    try {
      // Prepare and execute READ_MOLECULAR_ACTION query
      ma_pstmt = prepareStatement(query);
      ma_pstmt.setInt(1, molecule_id);
      ResultSet rs = ma_pstmt.executeQuery();

      // Create and populate MolecularAction object
      MolecularAction action = new MolecularAction(molecule_id);
      int row_count = 0;

      // Read
      while (rs.next()) {
        int transaction_id = rs.getInt("TRANSACTION_ID");
        action.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        // ignore transaction_id
        action.setActionName(rs.getString("MOLECULAR_ACTION"));
        action.setSourceIdentifier(rs.getInt("SOURCE_ID"));
        action.setTargetIdentifier(rs.getInt("TARGET_ID"));
        action.setTransactionIdentifier(transaction_id);
        action.setWorkIdentifier(rs.getInt("WORK_ID"));
        if (rs.getString("UNDONE").equals("Y")) {
          action.setIsUndone(true);
          action.setUndoneAuthority(getAuthority(rs.getString("UNDONE_BY")));
          action.setUndoneTimestamp(getDate(rs.getTimestamp("UNDONE_WHEN")));
        }
        else {
          action.setIsUndone(false);
        }
        action.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        action.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        action.setStatus(rs.getString("STATUS").charAt(0));
        action.setElapsedTime(rs.getLong("ELAPSED_TIME"));
        if (transaction_id != 0) {
          action.setErrors(getErrors(new MolecularTransaction(transaction_id)));

          // ignore work_id
        }
        row_count++;
      }

      // Close statement
      ma_pstmt.close();

      if (row_count == 0) {
        return null;
      }
      else {
        return action;
      }

    }
    catch (SQLException se) {
      try {
        ma_pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load molecular action.", query, se);
      dse.setDetail("query", query);
      dse.setDetail("molecule_id", Integer.toString(molecule_id));
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getFullMolecularAction(int)}.
   * @param molecule_id the molecular id.
   * @return the {@link MolecularAction}.
   * @throws DataSourceException if failed to load full molecular action.
   */
  public MolecularAction getFullMolecularAction(int molecule_id) throws
      DataSourceException {

    // Call getMolecularAction
    MolecularAction ma = getMolecularAction(molecule_id);

    String query = READ_ATOMIC_ACTIONS;
    PreparedStatement aa_pstmt = null;
    try {
      // Prepare and execute READ_ATOMIC_ACTIONS query
      aa_pstmt = prepareStatement(query);
      aa_pstmt.setInt(1, molecule_id);
      ResultSet rs = aa_pstmt.executeQuery();

      // Read
      while (rs.next()) {
        AtomicAction aa = null;
        if ( (rs.getString("ACTION")).equals("I")) {
          aa = new AtomicInsertAction(rs.getInt("ATOMIC_ACTION_ID"));
        }
        else {
          aa = new AtomicAction(rs.getInt("ATOMIC_ACTION_ID"));
        }
        aa.setParent(ma);
        aa.setActionName(rs.getString("ACTION"));
        aa.setAffectedTable(rs.getString("TABLE_NAME"));
        aa.setRowIdentifier(rs.getInt("ROW_ID"));
        aa.setOldValue(rs.getString("OLD_VALUE"));
        aa.setNewValue(rs.getString("NEW_VALUE"));
        aa.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        aa.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        aa.setStatus(rs.getString("STATUS").charAt(0));
        aa.setField(rs.getString("ACTION_FIELD"));
        // Add this action to the molecular_Action
        ma.addSubAction(aa);
      }

      // Close statement
      aa_pstmt.close();

      return ma;
    }
    catch (SQLException se) {
      try {
        aa_pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load atomic actions.", ma, se);
      dse.setDetail("query", query);
      dse.setDetail("molecule_id", Integer.toString(molecule_id));
      throw dse;
    }

  }

  /**
   * Implements {@link MEMEDataSource#getLastMolecularAction(Concept)}.
   * @param concept the {@link Concept}.
   * @return the {@link MolecularAction}.
   * @throws DataSourceException if failed to load last molecular action.
   */
  public MolecularAction getLastMolecularAction(Concept concept) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.getLastMolecularAction(Concept)...");

    String query = READ_LAST_ACTION;
    PreparedStatement la_pstmt = null;
    try {
      // Prepare and execute READ_LAST_ACTION query
      la_pstmt = prepareStatement(query);
      int concept_id = concept.getIdentifier().intValue();
      la_pstmt.setInt(1, concept_id);
      la_pstmt.setInt(2, concept_id);
      ResultSet rs = la_pstmt.executeQuery();

      int molecule_id = 0;
      while (rs.next()) {
        molecule_id = rs.getInt("MOLECULE_ID");
      }

      // Close statement
      la_pstmt.close();

      if (molecule_id == 0) {
        return null;
      }
      else {
        return getMolecularAction(molecule_id);
      }

    }
    catch (SQLException se) {
      try {
        la_pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load last molecular action.", concept, se);
      dse.setDetail("query", query);
      dse.setDetail("concept_id", concept.getIdentifier().toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getAtomicAction(int)}.
   * @param atomic_action_id the atomic
   * action id.
   * @return the {@link AtomicAction}.
   * @throws DataSourceException if failed to load atomic action.
   */
  public AtomicAction getAtomicAction(int atomic_action_id) throws
      DataSourceException {

    MEMEToolkit.trace("MEMEConnection.getAtomicAction(int)...");

    String query = READ_ATOMIC_ACTION;
    PreparedStatement aa_pstmt = null;
    AtomicAction action = null;
    try {
      // Prepare and execute READ_ATOMIC_ACTIONS query
      aa_pstmt = prepareStatement(query);
      aa_pstmt.setInt(1, atomic_action_id);
      ResultSet rs = aa_pstmt.executeQuery();

      // Create and populate AtomicAction object
      action = new AtomicAction(atomic_action_id);

      // Read
      while (rs.next()) {
        action.setActionName(rs.getString("ACTION"));
        action.setAffectedTable(rs.getString("TABLE_NAME"));
        action.setRowIdentifier(rs.getInt("ROW_ID"));
        action.setOldValue(rs.getString("OLD_VALUE"));
        action.setNewValue(rs.getString("NEW_VALUE"));
        action.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        action.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        action.setStatus(rs.getString("STATUS").charAt(0));
        action.setField(rs.getString("ACTION_FIELD"));
      }

      // Close statement
      aa_pstmt.close();
      return action;

    }
    catch (SQLException se) {
      try {
        aa_pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load atomic action.", action, se);
      dse.setDetail("query", query);
      dse.setDetail("atomic_action_id", Integer.toString(atomic_action_id));
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getAction(int)}.
   * @param action_id the action id
   * @return the {@link LoggedAction} representation of Logged action
   * @throws DataSourceException if failed to load action
   */
  public LoggedAction getAction(int action_id) throws DataSourceException {

    String query = "SELECT * FROM action_log WHERE action_id = ?";
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setInt(1, action_id);
      ResultSet rs = pstmt.executeQuery();

      LoggedAction action = null;
      int row_count = 0;

      // Read
      while (rs.next()) {

        String s1 = getClobAsString( (CLOB) rs.getObject("document"));
        ObjectXMLSerializer oxs = new ObjectXMLSerializer();

        // Create and populate LoggedAction object
        action = (LoggedAction) oxs.fromXML(new StringReader(s1));
        row_count++;
      }

      // Close statement
      pstmt.close();

      if (row_count == 0) {
        return null;
      }
      else {
        return action;
      }

    }
    catch (Exception se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load action data.", query, se);
      dse.setDetail("query", query);
      dse.setDetail("action_id", Integer.toString(action_id));
      throw dse;
    }
  }

  /**
   * Returns the count of logged actions.
   * @return the count of logged actions
   * @throws DataSourceException if failed to load action
   */
  public int getActionCount() throws DataSourceException {

    String query = "SELECT count(*) FROM action_log";
    Statement stmt = null;
    try {
      stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);
      int ct = 0;
      // Read
      while (rs.next()) {
        ct = rs.getInt(1);
      }

      // Close statement
      stmt.close();

      return ct;

    }
    catch (Exception se) {
      try {
        stmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load action data.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Removes the specified action from the action log.
   * @param action the {@link LoggedAction} to remove
   * @throws DataSourceException if failed to remove action
   */
  public void removeActionFromLog(LoggedAction action) throws
      DataSourceException {

    final String query = "DELETE FROM action_log WHERE action_id = " +
        action.getIdentifier();
    try {
      int row_count = createStatement().executeUpdate(query);
      if (row_count == 0) {
        throw new Exception("No rows deleted!");
      }
    }
    catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to remove action data.", query, se);
      dse.setDetail("query", query);
      dse.setDetail("action_id", action.getIdentifier());
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getInverseRelationshipNameMap()}.
   * @return the {@link Map}.
   * @throws DataSourceException if failed to load relationship name.
   */
  public Map getInverseRelationshipNameMap() throws DataSourceException {
    return (Map) inverse_rel_cache.getMap(getDataSourceName());
  }

  /**
   * Implements {@link MEMEDataSource#getInverseRelationshipAttributeMap()}.
   * @return the {@link Map}.
   * @throws DataSourceException if failed to load relationship attribute.
   */
  public Map getInverseRelationshipAttributeMap() throws DataSourceException {
    return (Map) inverse_rela_cache.getMap(getDataSourceName());
  }

  /**
   * Finds the null LUI.
   * @param param the {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching string.
   */
  public LUI getNullLUI() throws DataSourceException {
    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement("SELECT DISTINCT lui FROM string_ui WHERE norm_string IS NULL");
      final ResultSet rs = pstmt.executeQuery();
      String lui = null;
      while (rs.next()) {
      	lui = rs.getString("LUI");
      }
      if (lui != null)
        return new LUI(lui);
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find null LUI.", "", e);
      throw dse;
    }
    return null;
  }

  /**
   * Implements {@link MEMEDataSource#findStrings(SearchParameter)}.
   * @param param the {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching string.
   */
  public Iterator findStrings(SearchParameter param) throws DataSourceException {
    return findStrings(new SearchParameter[] {param});
  }

  /**
   * Implements {@link MEMEDataSource#findStrings(SearchParameter[])}.
   * @param params An array of object {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching string.
   */
  public Iterator findStrings(SearchParameter[] params) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.findStrings(SearchParameter[])...");

    // Build up a query that searches for string_ui rows
    StringBuffer query = new StringBuffer(500);
    query.append("SELECT * FROM string_ui WHERE 1=1");

    List list = new ArrayList();

    QueryBuilder builder = new StringQueryBuilder();
    builder.build(query, params, list);

    MEMEToolkit.trace(query.toString());

    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("i[" + i + "]=" + list.get(i));
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();

      // Return an iterator that returns MolecularActions object
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          MEMEString string = new MEMEString.Default();
          string.setLUI(new StringIdentifier(rs.getString("LUI")));
          string.setSUI(new StringIdentifier(rs.getString("SUI")));
          string.setLanguage(getLanguage(rs.getString("LANGUAGE")));
          string.setString(rs.getString("STRING"));
          string.setNormalizedString(rs.getString("NORM_STRING"));
          string.setISUI(new StringIdentifier(rs.getString("ISUI")));
          return string;
        }
      }
      );
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find matching strings.", query, e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Search for actions.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find actions.
   */
  public Iterator findActions(SearchParameter param) throws DataSourceException {
    return findActions(new SearchParameter[] {param});
  }

  /**
   * Search for actions.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find actions.
   */
  public Iterator findActions(SearchParameter[] param) throws
      DataSourceException {

    // Build up a query that searches for
    // molecule_ids, then use getFullMolecularAction to return
    // the objects.
    StringBuffer query = new StringBuffer(100);
    query.append("SELECT action_id FROM action_log ")
        .append("WHERE 1=1 ");

    for (int i = 0; i < param.length; i++) {

      String name = param[i].getName();
      Identifier start = param[i].getStartId();
      Identifier end = param[i].getEndId();
      Identifier value = param[i].getValue();

      // Range Searches
      if (param[i].isRangeSearch()) {
        if (start != null) {
          query.append(" AND ").append(name).append(" >='")
              .append(start).append("'");
        }
        if (end != null) {
          query.append(" AND ").append(name).append(" <='")
              .append(end).append("'"); ;
        }
      }
      else if (param[i].isSingleValueSearch()) {
        query.append(" AND ").append(name).append(" ='")
            .append(value).append("'");
      }
    }
    // Execute the query
    try {
      Statement stmt = createStatement();
      final ResultSet rs = stmt.executeQuery(query.toString());

      // Return an iterator that returns MolecularAction objects
      return new Iterator() {
        private int action_id = 0;
        public boolean hasNext() {
          try {
            if (action_id == 0) {
              if (rs.next()) {
                action_id = rs.getInt("ACTION_ID");
                return true;
              }
              else {
                rs.close();
                return false;
              }
            }
            else {
              return true;
            }
          }
          catch (SQLException e) {
            return false;
          }
        }

        public Object next() {
          try {
            LoggedAction la = getAction(action_id);
            action_id = 0;
            return la;
          }
          catch (DataSourceException e) {
            try {
              rs.close();
            }
            catch (SQLException se) {}
            ;
            throw new NoSuchElementException(e.getMessage());
          }
        }

        public void remove() {
          try {
            rs.getStatement().close();
          }
          catch (Exception e) {}
        }
      };
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to load molecular actions.", query.toString(), e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#findAtoms(SearchParameter,Ticket)}.
   * @param param the {@link SearchParameter}.
   * @param ticket thee {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching atoms.
   */
  public Iterator findAtoms(SearchParameter param, Ticket ticket) throws
      DataSourceException {
    return findAtoms(new SearchParameter[] {param}
                     , ticket);
  }

  /**
   * Implements {@link MEMEDataSource#findAtoms(SearchParameter[],Ticket)}.
   * @param params An array of object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching atoms.
   */
  public Iterator findAtoms(SearchParameter[] params, Ticket ticket) throws
      DataSourceException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    MEMEToolkit.trace(
        "MEMEConnection.findAtoms(SearchParameter[])...");

    // Build up a query that searches for atom_ids
    StringBuffer query = new StringBuffer(500);
    query.append("SELECT atom_id FROM ")
        .append( (ticket.readDeleted()) ? "dead_" : "")
        .append(ticket.getDataTable(Atom.class))
        .append(" a, string_ui b")
        .append(" WHERE a.sui = b.sui");

    List list = new ArrayList();

    QueryBuilder builder = new StringQueryBuilder();
    builder.build(query, params, list);

    MEMEToolkit.trace(query.toString());

    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("i[" + i + "]=" + list.get(i));
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();
      final Ticket l_ticket = ticket;
      // Return an iterator that returns MolecularActions object
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          return getAtomWithName(rs.getInt("ATOM_ID"), l_ticket);
        }
      }
      );
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find matching atoms.", query, e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#findConcepts(SearchParameter, Ticket)}.
   * @param param the {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConcepts(SearchParameter param, Ticket ticket) throws
      DataSourceException {
    return findConceptsFromString(param, ticket);
  }

  /**
   * Implements {@link MEMEDataSource#findConcepts(SearchParameter[], Ticket)}.
   * @param params An array of object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConcepts(SearchParameter[] params, Ticket ticket) throws
      DataSourceException {
    return findConceptsFromString(params, ticket);
  }

  /**
   * Implements {@link MEMEDataSource#findConceptsFromString(SearchParameter, Ticket)}.
   * @param param the {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConceptsFromString(SearchParameter param, Ticket ticket) throws
      DataSourceException {
    return findConceptsFromString(new SearchParameter[] {param}
                                  , ticket);
  }

  /**
   * Implements {@link MEMEDataSource#findConceptsFromString(SearchParameter[], Ticket)}.
   * @param params An array of object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConceptsFromString(SearchParameter[] params,
                                         Ticket ticket) throws
      DataSourceException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    MEMEToolkit.trace(
        "MEMEConnection.findConceptsFromString(SearchParameter[])...");

    // Build up a query that searches for concept_ids
    StringBuffer query = new StringBuffer(500);
    query.append("SELECT DISTINCT concept_id, preferred_atom_id, status")
        .append(" FROM concept_status")
        .append(" WHERE concept_id IN")
        .append(" (SELECT concept_id FROM classes a, string_ui b")
        .append(" WHERE a.sui = b.sui");

    List list = new ArrayList();

    SemanticTypeQueryBuilder sty_builder = new SemanticTypeQueryBuilder();
    sty_builder.build(query, params, list);

    QueryBuilder builder = new StringQueryBuilder();
    builder.build(query, params, list);

    query.append(")");

    MEMEToolkit.trace(query.toString());

    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("i[" + i + "]=" + list.get(i));
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();
      final Ticket l_ticket = ticket;
      // Return an iterator that returns MolecularActions object
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          Concept concept = new Concept.Default(rs.getInt("CONCEPT_ID"));
          if (rs.getInt("PREFERRED_ATOM_ID") != 0) {
            concept.setPreferredAtom(getAtomWithName(rs.getInt(
                "PREFERRED_ATOM_ID"), l_ticket));
          }
          concept.setStatus(rs.getString("STATUS").charAt(0));
          return concept;
        }
      }
      );
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find matching concepts.", query, e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }
  /**
   * Implements {@link MEMEDataSource#findConceptsFromString(SearchParameter[], Ticket)}.
   * @param params An array of object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findNDCConceptsFromCode(String code,
                                         Ticket ticket) throws
      DataSourceException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    MEMEToolkit.trace(
        "MEMEConnection.findConceptsFromString(SearchParameter[])...", true);

    // Build up a query that searches for concept_ids
    // First get the normalized string and then query
    // 
    if (code.indexOf('-') == -1  && code.length() == 12 && code.substring(0,1).equals("0")) {
    	code = code.substring(1);
    }
    if (code.indexOf('-') == -1  && code.length() == 10) {
    	code = '0'+ code;
    }
    String query_code = "{? = call MEME_OPERATIONS.Get_Norm_Ndc('" + code + "')}";
    MEMEToolkit.trace(
    "MEMEConnection.findConceptsFromString(SearchParameter[])..." + query_code, true);

    StringBuffer query = new StringBuffer(500);
    // Execute the query
    try {
    	
    	CallableStatement cstmt = prepareCall(query_code);
        cstmt.registerOutParameter(1, Types.VARCHAR);
        cstmt.execute();
        String norm_code = cstmt.getString(1);
        MEMEToolkit.trace(
        	    "Return data )...[" + norm_code + "]", true);
        cstmt.close();
	    query.append("SELECT DISTINCT concept_id, preferred_atom_id, status")
	        .append(" FROM concept_status")
	        .append(" WHERE concept_id IN")
	        .append(" (SELECT concept_id FROM attributes ")
	        .append(" WHERE attribute_name = 'NDC' AND ");
	    query.append("MEME_OPERATIONS.Get_Norm_Ndc(attribute_value) = MEME_OPERATIONS.Get_Norm_Ndc('" + code +"') AND (");
/*	    if (!code.substring(0,1).equals("0")) {
	    	query.append("attribute_value like '0" + code.substring(0,2) + "%' OR ");
	    	 query.append("attribute_value like '" + code.substring(0, 2) + "%' )");
	    } else { */
	      
	    	query.append("attribute_value like '" + norm_code.substring(0,3) + "%' OR ");
	    	 query.append("attribute_value like '" + norm_code.substring(1, 3) + "%' )");
//	    }
	    query.append(")");
	
	    MEMEToolkit.trace(query.toString(), true);
	
	
	      PreparedStatement pstmt = prepareStatement(query.toString());
	      final ResultSet rs = pstmt.executeQuery();
	      final Ticket l_ticket = ticket;
	      // Return an iterator that returns MolecularActions object
	      return new ResultSetIterator(rs, this,
	                                   new ResultSetMapper() {
	        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
	            MEMEException {
	          Concept concept = new Concept.Default(rs.getInt("CONCEPT_ID"));
	          if (rs.getInt("PREFERRED_ATOM_ID") != 0) {
	            concept.setPreferredAtom(getAtomWithName(rs.getInt(
	                "PREFERRED_ATOM_ID"), l_ticket));
	          }
	          concept.setStatus(rs.getString("STATUS").charAt(0));
	          return concept;
	        }
	      }
	      );
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find matching concepts.", query, e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }
  /**
   * Implements {@link MEMEDataSource#findConceptsFromWords(SearchParameter, Ticket)}.
   * @param param the {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConceptsFromWords(SearchParameter param, Ticket ticket) throws
      DataSourceException {
    return findConceptsFromWords(new SearchParameter[] {param}
                                 , ticket);
  }

  /**
   * Implements {@link MEMEDataSource#findConceptsFromWords(SearchParameter[], Ticket)}.
   * @param params An array of object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConceptsFromWords(SearchParameter[] params, Ticket ticket) throws
      DataSourceException {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    MEMEToolkit.trace(
        "MEMEConnection.findConceptsFromWords(SearchParameter[])...");

    // Build up a query that searches for concept_ids
    StringBuffer query = new StringBuffer(500);
    query.append("SELECT DISTINCT concept_id, preferred_atom_id, status")
        .append(" FROM concept_status")
        .append(" WHERE concept_id IN")
        .append(" (SELECT concept_id FROM classes c WHERE 1=1 ");

    List list = new ArrayList();

    SemanticTypeQueryBuilder sty_builder = new SemanticTypeQueryBuilder();
    sty_builder.build(query, params, list);

    QueryBuilder builder = new WordQueryBuilder();
    builder.build(query, params, list);

    query.append(")");

    MEMEToolkit.trace(query.toString());

    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("i[" + i + "]=" + list.get(i));
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();
      final Ticket l_ticket = ticket;
      // Return an iterator that returns MolecularActions object
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          Concept concept = new Concept.Default(rs.getInt("CONCEPT_ID"));
          concept.setPreferredAtom(getAtomWithName(rs.getInt(
              "PREFERRED_ATOM_ID"), l_ticket));
          concept.setStatus(rs.getString("STATUS").charAt(0));
          return concept;
        }
      }
      );
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find matching concepts.", query, e);
      dse.setDetail("query", query.toString());
      throw dse;

    }
  }

  /**
   * Implements {@link MEMEDataSource#findMolecularActions(SearchParameter)}.
   * @param param A single {@link SearchParameter}
   * @return the {@link Iterator}
   * @throws DataSourceException if failed to find molecular actions
   */
  public Iterator findMolecularActions(SearchParameter param) throws
      DataSourceException {
    return findMolecularActions(new SearchParameter[] {param});
  }

  /**
   * Implements {@link MEMEDataSource#findMolecularActions(SearchParameter[])}.
   * @param params An array of {@link SearchParameter}
   * @return the {@link Iterator}
   * @throws DataSourceException if failed to find molecular actions
   */
  public Iterator findMolecularActions(SearchParameter[] params) throws
      DataSourceException {

    StringBuffer query = new StringBuffer(500);
    List list = new ArrayList();

    QueryBuilder builder = new MolecularActionQueryBuilder();
    builder.build(query, params, list);

    MEMEToolkit.trace(query.toString());

    // Execute the query
    try {

      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("\n" + list.get(i).toString());
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();

      // Return an iterator that returns MolecularAction objects
      return new Iterator() {
        private int molecule_id = 0;
        public boolean hasNext() {
          try {
            if (molecule_id == 0) {
              if (rs.next()) {
                molecule_id = rs.getInt("MOLECULE_ID");
                return true;
              }
              else {
                rs.close();
                return false;
              }
            }
            else {
              return true;
            }
          }
          catch (SQLException e) {
            return false;
          }
        }

        public Object next() {
          try {
            MolecularAction ma = getFullMolecularAction(molecule_id);
            molecule_id = 0;
            return ma;
          }
          catch (DataSourceException e) {
            try {
              rs.close();
            }
            catch (SQLException se) {}
            ;
            e.setFatal(false);
            MEMEToolkit.handleError(e);
            throw new NoSuchElementException(e.getMessage());
          }
        }

        public void remove() {
          try {
            rs.getStatement().close();
          }
          catch (Exception e) {}
        }
      };
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to load molecular actions.", query, e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#findSafeReplacementFacts(SearchParameter)}.
   * @param param the {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching
   * safe replacement facts.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Iterator findSafeReplacementFacts(SearchParameter param) throws
      DataSourceException, BadValueException {
    return findSafeReplacementFacts(new SearchParameter[] {param});
  }

  /**
   * Implements {@link MEMEDataSource#findSafeReplacementFacts(SearchParameter[])}.
   * @param params An array of object {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching
   * safe replacement facts.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Iterator findSafeReplacementFacts(SearchParameter[] params) throws
      DataSourceException, BadValueException {

    MEMEToolkit.trace(
        "MEMEConnection.findSafeReplacementFacts(SearchParameter[]) ...");

    // Build up a query that searches for mom_merge_fact rows
    StringBuffer query = new StringBuffer(500);
    query.append("SELECT * FROM mom_safe_replacement WHERE 1=1");

    List list = new ArrayList();

    QueryBuilder builder = new QueryBuilder();
    builder.build(query, params, list);

    MEMEToolkit.trace("MEMEConnection.query=" + query);

    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("i[" + i + "]=" + list.get(i));
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();

      // Return an iterator that returns object
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          SafeReplacementFact srf = new SafeReplacementFact();
          srf.setOldAtom(new Atom.Default(rs.getInt("OLD_ATOM_ID")));
          srf.setNewAtom(new Atom.Default(rs.getInt("NEW_ATOM_ID")));
          srf.setRank(new Rank.Default(rs.getInt("RANK")));
          srf.setSource(getSource(rs.getString("SOURCE")));
          return srf;
        }
      }
      );
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find matching safe replacements.", query, e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getCpuMode(String)}.
   * It uses a select statement like this:
   * @param application the {@link String} representation of application name.
   * @return the {@link String} representation of the CPU mode, OR null if no matching
   * criteria could be found.
   * @throws DataSourceException if failed to load CPU mode.
   *
   * <pre>
   *    SELECT * FROM meme_schedule
   *    WHERE (cpu_mode IS NOT NULL)
   *    AND (lower(application) = .... OR application IS NULL)
   *    AND (specific_date = trunc(sysdate, 'ddd') OR specific_date IS NULL)
   *    AND (day_of_week = trunc(sysdate)-trunc(sysdate,'d') OR day_of_week IS NULL)
   *    AND ((start_time IS NULL AND end_time IS NULL)
   *            OR
   *            (start_time < end_time AND
   *            (to_char(sysdate, 'hh24:mi:ss')>= start_time AND
   *            to_char(sysdate, 'hh24:mi:ss')< end_time))
   *            OR
   *            (start_time > end_time AND
   *            (to_char(sysdate, 'hh24:mi:ss')>= start_time OR
   *            to_char(sysdate, 'hh24:mi:ss')< end_time)));
   * </pre>
   */
  public String getCpuMode(String application) throws DataSourceException {

    // Start with null CPU mode
    String cpu_mode = null;

    // Prepare query
    String query =
        "SELECT * FROM meme_schedule WHERE (cpu_mode IS NOT NULL)" +
        " AND (lower(application) = '" + application.toLowerCase() +
        "' OR application IS NULL)" +
        " AND (specific_date = trunc(sysdate, 'ddd') OR specific_date IS NULL)" +
        " AND (day_of_week = trunc(sysdate) - trunc(sysdate,'d') OR day_of_week IS NULL)" +
        " AND ((start_time IS NULL AND end_time IS NULL)" +
        "      OR (start_time < end_time AND" +
        "          (to_char(sysdate, 'hh24:mi:ss') >= start_time AND" +
        "           to_char(sysdate, 'hh24:mi:ss') < end_time))" +
        "      OR (start_time > end_time AND" +
        "          (to_char(sysdate, 'hh24:mi:ss') >= start_time OR" +
        "           to_char(sysdate, 'hh24:mi:ss') < end_time)))";

    //MEMEToolkit.trace(query);

    try {
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query);
      int max = 0;
      while (rset.next()) {

        // Rank the results to find the most specific row.
        int level = 0;
        if (rset.getString("application") != null) {
          level += 1000;
        }
        if (rset.getDate("specific_date") != null) {
          level += 100;
        }
        if (rset.getInt("day_of_week") > 0 || ! (rset.wasNull())) {
          level += 10;
        }
        if (rset.getString("start_time") != null) {
          level += 1;

          // select highest ranking one
        }
        if (level > max) {
          cpu_mode = rset.getString("cpu_mode");
          max = level;
        }
      }
      stmt.close();

      // If there were no results, return null
      if (max > 0) {
        return cpu_mode;
      }
      else {
        DataSourceException dse = new DataSourceException(
            "MEMESchedule mute about cpu_mode.");
        dse.setDetail("application", application);
        throw dse;
      }
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to load CPU mode.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getUserName()}.
   * @return the {@link String} representation of user name.
   * @throws DataSourceException if failed to get user name.
   */
  public String getUserName() throws DataSourceException {
    try {
      return getMetaData().getUserName();
    }
    catch (SQLException se) {
      DataSourceException dse =
          new DataSourceException("Failed to obtain data source user name.", null,
                                  se);
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getServiceName()}.
   * @return the {@link String} representation of service name.
   */
  public String getServiceName() {
    return db_service;
  }

  /**
   * Implements {@link MEMEDataSource#setServiceName(String)}.
   * @param db_service the {@link String} representation of db service.
   */
  public void setServiceName(String db_service) {
    this.db_service = db_service;
  }

  /**
   * Implements {@link MEMEDataSource#getDataSourceName()}.
   * @return the {@link String} representation of data source name.
   */
  public String getDataSourceName() {
    return data_source_name;
  }

  /**
   * Implements {@link MEMEDataSource#setDataSourceName(String)}.
   * @param data_source_name the {@link String} representation of
   * data source name.
   */
  public void setDataSourceName(String data_source_name) {
    this.data_source_name = data_source_name;
  }

  /**
   * Overrides {@link Object#toString()}.
   * @return the {@link String} represents a string.
   */
  public String toString() {
    try {
      return "MEMEConection - " + getUserName() + "@" + getDataSourceName();
    }
    catch (DataSourceException dse) {
      return "MEME Connection - user unknown@" + getDataSourceName();
    }
  }

  //
  // Private Helper Methods
  //

  /**
   * Puts the source rank into cache.
   * @param sab the {@link String} representation of source abbreviation.
   * @throws DataSourceException if failed to cache source rank.
   */
  private void cacheSourceRank(String sab) throws DataSourceException {
    MEMEToolkit.logComment("  Cache source_rank data - " + sab, true);

    // We need to do transformations for E-* and S-* sources
    String l_sab = sab;

    if (sab != null) {
      if (sab.startsWith("E-") || sab.startsWith("S-") ||
          sab.startsWith("RESCUE") || sab.startsWith("L-")) {
        l_sab = "MTH";
      }
    } else {
      source_rank_cache.clear(getDataSourceName());
    }
    final String is_current_query = "SELECT COUNT(*) AS CT " +
        "FROM source_version WHERE current_name = ?";

    // Query to check whether or not the current row source
    // being processed is previous
    final String is_previous_query = "SELECT COUNT(*) AS CT " +
        "FROM source_version WHERE previous_name = ?";

    // Query to retrieve all source row
    String sr_query = "SELECT a.source, a.rank, a.normalized_source, " +
        " a.stripped_source, a.source_family, a.version, a.restriction_level, " +
        " b.date_created, b.meta_year, b.init_rcpt_date, " +
        " b.clean_rcpt_date, b.test_insert_date, b.real_insert_date, " +
        " b.source_contact, b.inverter_contact, b.nlm_path, " +
        " b.apelon_path, b.inversion_script, b.inverter_notes_file, " +
        " b.conserve_file, b.sab_list, b.meow_display_name, " +
        " b.source_desc, b.status, b.worklist_sortkey_loc, " +
        " b.termgroup_list, b.attribute_list, b.inversion_notes, " +
        " b.notes, b.inv_recipe_loc, b.suppress_edit_rec, " +
        " b.versioned_cui, b.root_cui, b.source_official_name, " +
        " b.source_short_name, b.attribute_name_list, b.term_type_list, " +
        " b.term_frequency, b.cui_frequency, b.citation, " +
        " b.last_contacted, b.license_info, b.character_set, " +
        " b.valid_start_date, b.valid_end_date, b.insert_meta_version, " +
        " b.remove_meta_version, b.nlm_contact, b.acquisition_contact, " +
        " b.content_contact, b.license_contact, b.context_type, " +
        " b.language, b.test_insertion_start, b.test_insertion_end, " +
        " b.real_insertion_start, b.real_insertion_end, b.editing_start, " +
        " b.editing_end, b.latest_available, b.release_url_list, " +
        " b.internal_url_list, b.rel_directionality_flag " +
        "FROM source_rank a, sims_info b WHERE a.source = b.source (+)";

    if (sab != null) {
      sr_query = sr_query + " AND a.source = '" + l_sab + "'";

      // Query to look up the CUI for SRC concept
    }
    final String vcui_query = "SELECT cui " +
        "FROM concept_status a, classes b " +
        "WHERE a.concept_id = b.concept_id" +
        " AND b.code = ? " +
        " AND source='SRC' and tty = 'VAB'";

    final String rcui_query = "SELECT cui " +
        "FROM concept_status a, classes b " +
        "WHERE a.concept_id = b.concept_id" +
        " AND b.code = ? " +
        " AND source='SRC' and tty = 'RAB'";

    try {
      PreparedStatement current_pstmt = null;
      PreparedStatement previous_pstmt = null;
      PreparedStatement vcui_pstmt = null;
      PreparedStatement rcui_pstmt = null;

      try {
        current_pstmt = prepareStatement(is_current_query);
        previous_pstmt = prepareStatement(is_previous_query);
        vcui_pstmt = prepareStatement(vcui_query);
        rcui_pstmt = prepareStatement(rcui_query);
      }
      catch (SQLException se) {
        DataSourceException dse = new DataSourceException(
            "Failed to prepare statement.", sab, se);
        dse.setDetail("is_current_query", is_current_query);
        dse.setDetail("is_previous_query", is_previous_query);
        dse.setDetail("vcui_query", vcui_query);
        dse.setDetail("rcui_query", rcui_query);
        throw dse;
      }

      Statement sr_stmt = createStatement();
      ResultSet rs = sr_stmt.executeQuery(sr_query);

      // Read
      while (rs.next()) {
        // Create and populate Source object
        String rs_string = rs.getString("SOURCE");
        Source source = new Source.Default();

        // If we are using a single SAB value, use it here
        // Note, sab might be E-TPW, but l_sab is MTH
        // We want characteristics of MTH with an abbreviation of E-TPW
        if (sab != null) {
          source.setSourceAbbreviation(sab);
        }
        else {
          source.setSourceAbbreviation(rs_string);

        }
        source.setRank(new Rank.Default(rs.getInt("RANK")));
        source.setNormalizedSourceAbbreviation(rs.getString("NORMALIZED_SOURCE"));
        source.setStrippedSourceAbbreviation(rs.getString("STRIPPED_SOURCE"));
        source.setRootSourceAbbreviation(rs.getString("STRIPPED_SOURCE"));
        source.setSourceFamilyAbbreviation(rs.getString("SOURCE_FAMILY"));
        source.setSourceVersion(rs.getString("VERSION"));
        source.setRestrictionLevel(rs.getString("RESTRICTION_LEVEL"));
        source.setDateCreated(getDate(rs.getTimestamp("DATE_CREATED")));
        if (rs.getString("META_YEAR") != null) {
          source.setMetaYear(Integer.valueOf(rs.getString("META_YEAR")).
                             intValue());
        }
        source.setInitialReceiptDate(getDate(rs.getTimestamp("INIT_RCPT_DATE")));
        source.setCleanReceiptDate(getDate(rs.getTimestamp("CLEAN_RCPT_DATE")));
        source.setTestInsertionDate(getDate(rs.getTimestamp("TEST_INSERT_DATE")));
        source.setRealInsertionDate(getDate(rs.getTimestamp("REAL_INSERT_DATE")));
        source.setSourceContact(rs.getString("SOURCE_CONTACT"));
        source.setInverterContact(rs.getString("INVERTER_CONTACT"));
        source.setNLMPath(rs.getString("NLM_PATH"));
        source.setApelonPath(rs.getString("APELON_PATH"));
        source.setInversionScript(rs.getString("INVERSION_SCRIPT"));
        source.setInverterNotesFile(rs.getString("INVERTER_NOTES_FILE"));
        source.setConserveFile(rs.getString("CONSERVE_FILE"));
        source.setSABList(rs.getString("SAB_LIST"));
        source.setMeowDisplayName(rs.getString("MEOW_DISPLAY_NAME"));
        source.setSourceDescription(rs.getString("SOURCE_DESC"));
        source.setStatus(rs.getString("STATUS"));
        source.setWorklistSortkeyLocation(rs.getString("WORKLIST_SORTKEY_LOC"));
        source.setTermGroupList(rs.getString("TERMGROUP_LIST"));
        source.setAttributeList(rs.getString("ATTRIBUTE_LIST"));
        source.setInversionNotes(rs.getString("INVERSION_NOTES"));
        source.setNotes(rs.getString("NOTES"));
        source.setInverseRecipeLocation(rs.getString("INV_RECIPE_LOC"));
        if (rs.getString("SUPPRESS_EDIT_REC") != null) {
          source.setSuppressibleEditableRecord(rs.getString("SUPPRESS_EDIT_REC").
                                               equals("Y") ? true : false);
        }
        if (rs.getString("VERSIONED_CUI") != null) {
          source.setVersionedCui(new CUI(rs.getString("VERSIONED_CUI")));
        }
        if (rs.getString("ROOT_CUI") != null) {
          source.setRootCui(new CUI(rs.getString("ROOT_CUI")));
        }
        source.setOfficialName(rs.getString("SOURCE_OFFICIAL_NAME"));
        source.setShortName(rs.getString("SOURCE_SHORT_NAME"));
        source.setAttributeNameList(rs.getString("ATTRIBUTE_NAME_LIST"));
        source.setTermTypeList(rs.getString("TERM_TYPE_LIST"));
        source.setTermFrequency(rs.getInt("TERM_FREQUENCY"));
        source.setCuiFrequency(rs.getInt("CUI_FREQUENCY"));
        source.setCitation(rs.getString("CITATION"));
        source.setLastContactedDate(getDate(rs.getTimestamp("LAST_CONTACTED")));
        source.setLicenseInformation(rs.getString("LICENSE_INFO"));
        source.setCharacterEncoding(rs.getString("CHARACTER_SET"));
        source.setInsertionDate(getDate(rs.getTimestamp("VALID_START_DATE")));
        source.setExpirationDate(getDate(rs.getTimestamp("VALID_END_DATE")));
        source.setInsertMetaVersion(rs.getString("INSERT_META_VERSION"));
        source.setRemoveMetaVersion(rs.getString("REMOVE_META_VERSION"));
        source.setNLMContact(rs.getString("NLM_CONTACT"));
        source.setAcquisitionContact(rs.getString("ACQUISITION_CONTACT"));
        source.setContentContact(rs.getString("CONTENT_CONTACT"));
        source.setLicenseContact(rs.getString("LICENSE_CONTACT"));
        source.setContextType(rs.getString("CONTEXT_TYPE"));
        source.setTestInsertionStartDate(getDate(rs.getTimestamp(
            "TEST_INSERTION_START")));
        source.setTestInsertionEndDate(getDate(rs.getTimestamp(
            "TEST_INSERTION_END")));
        source.setRealInsertionStartDate(getDate(rs.getTimestamp(
            "REAL_INSERTION_START")));
        source.setRealInsertionEndDate(getDate(rs.getTimestamp(
            "REAL_INSERTION_END")));
        source.setEditingStartDate(getDate(rs.getTimestamp("EDITING_START")));
        source.setEditingEndDate(getDate(rs.getTimestamp("EDITING_END")));
        source.setLatestAvailable(rs.getString("LATEST_AVAILABLE"));
        source.setReleaseUrlList(rs.getString("RELEASE_URL_LIST"));
        source.setInternalUrlList(rs.getString("INTERNAL_URL_LIST"));
        if (rs.getString("REL_DIRECTIONALITY_FLAG") != null) {
          source.setRelationshipDirectionalityFlag(rs.getString(
              "REL_DIRECTIONALITY_FLAG").equals("Y") ? true : false);

        }
        String language = null;
        try {
          language = rs.getString("LANGUAGE");
          if (language != null) {
            source.setLanguage(getLanguage(language));
          }
        }
        catch (BadValueException bve) {
          DataSourceException dse = new DataSourceException(
              "Failed to set language.", language, bve);
          throw dse;
        }

        try {
          // Determine if the source is current
          current_pstmt.setString(1, rs_string);
          ResultSet is_current_rs = current_pstmt.executeQuery();
          while (is_current_rs.next()) {
            if (is_current_rs.getInt("CT") == 1) {
              source.setIsCurrent(true);
            }
            else {
              source.setIsCurrent(false);
            }
          }
        }
        catch (SQLException se) {
          DataSourceException dse = new DataSourceException(
              "Failed to determine if source is current.", rs_string, se);
          dse.setDetail("query", is_current_query);
          dse.setDetail("source", rs_string);
          throw dse;
        }

        try {
          // Determine if the source is previous
          previous_pstmt.setString(1, rs_string);
          ResultSet is_previous_rs = previous_pstmt.executeQuery();
          while (is_previous_rs.next()) {
            if (is_previous_rs.getInt("CT") == 1) {
              source.setIsPrevious(true);
            }
            else {
              source.setIsPrevious(false);
            }
          }
        }
        catch (SQLException se) {
          DataSourceException dse = new DataSourceException(
              "Failed to determine if source is obsolete.", rs_string, se);
          dse.setDetail("query", is_previous_query);
          dse.setDetail("source", rs_string);
          throw dse;
        }

        try {
          // Look up Versioned CUI for SRC concept
          vcui_pstmt.setString(1, "V-" + source.getSourceAbbreviation());

          ResultSet vcui_rs = vcui_pstmt.executeQuery();

          // Set to null so if result set has zero rows, Versioned CUI is null
          source.setVersionedCui(null);
          while (vcui_rs.next()) {
            String s_cui = vcui_rs.getString("CUI");
            CUI cui;
            if (s_cui != null) {
              cui = new CUI(vcui_rs.getString("CUI"));
            }
            else {
              cui = null;
            }
            source.setVersionedCui(cui);
          }
        }
        catch (SQLException se) {
          DataSourceException dse = new DataSourceException(
              "Failed to look up versioned cui for source.", source, se);
          dse.setDetail("query", vcui_query);
          dse.setDetail("source_abbreviation", source.getSourceAbbreviation());
          throw dse;
        }

        try {
          // Look up Root CUI for SRC concept
          rcui_pstmt.setString(1, "V-" + source.getRootSourceAbbreviation());

          ResultSet rcui_rs = rcui_pstmt.executeQuery();

          // Set to null so if result set has zero rows, Root CUI is null
          source.setRootCui(null);
          while (rcui_rs.next()) {
            String s_cui = rcui_rs.getString("CUI");
            CUI cui;
            if (s_cui != null) {
              cui = new CUI(rcui_rs.getString("CUI"));
            }
            else {
              cui = null;
            }
            source.setRootCui(cui);
          }
        }
        catch (SQLException se) {
          DataSourceException dse = new DataSourceException(
              "Failed to look up root cui for source.", source, se);
          dse.setDetail("query", rcui_query);
          dse.setDetail("root_source_abbreviation",
                        source.getRootSourceAbbreviation());
          throw dse;
        }

        // Add object to HashMap
        if (sab != null) {
          source_rank_cache.put(getDataSourceName(), sab, source);
        }
        else {
          source_rank_cache.put(getDataSourceName(), rs_string, source);
        }
      }

      // Close query set
      sr_stmt.close();
      current_pstmt.close();
      previous_pstmt.close();
      vcui_pstmt.close();
      rcui_pstmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to cache source rank.", sr_query, se);
      dse.setDetail("query", sr_query);
      throw dse;
    }

    // MEMEToolkit.trace(
    //    "MEMEConnection.cacheSourceRank(String) - finished: "
    //    + getSources());
  }

  /**
   * Puts the termgroup rank into cache.
   * @param tg the {@link String} representation of termgroup.
   * @throws DataSourceException if failed to cache termgroup rank.
   * @throws BadValueException if failed due to invalid data value.
   */
  private void cacheTermgroupRank(String tg) throws DataSourceException,
      BadValueException {
    MEMEToolkit.logComment("  Cache termgroup_rank data " +
                           ( (tg == null) ? "" : tg), true);

    // Query to retrieve termgroup ranks
    String tr_query = "SELECT termgroup, " +
        "SUBSTR(termgroup, 0, INSTR(termgroup,'/')-1) AS source, " +
        "SUBSTR(termgroup, INSTR(termgroup,'/')+1) AS tty, " +
        "rank, release_rank, suppressible FROM termgroup_rank";
    if (tg != null) {
      tr_query = tr_query + " WHERE termgroup = '" + tg + "'";
    } else {
    	termgroup_rank_cache.clear(getDataSourceName());
    }
    
  	try {
      Statement tr_stmt = createStatement();
      ResultSet rs = tr_stmt.executeQuery(tr_query);

      // Read
      while (rs.next()) {
        // Create and populate Termgroup object
        String rs_string = rs.getString("TERMGROUP");
        Termgroup termgroup = new Termgroup.Default();
        termgroup.setTermType(rs.getString("TTY"));
        termgroup.setSource(getSource(rs.getString("SOURCE")));
        termgroup.setRank(new Rank.Default(rs.getInt("RANK")));
        termgroup.setReleaseRank(new Rank.Default(rs.getInt("RELEASE_RANK")));
        termgroup.setSuppressible(rs.getString("SUPPRESSIBLE"));

        // Add object to HashMap
        termgroup_rank_cache.put(getDataSourceName(), rs_string, termgroup);
      }

      // Close query set
      tr_stmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to cache termgroup rank.", tr_query, se);
      dse.setDetail("query", tr_query);
      throw dse;
    }

    MEMEToolkit.trace(
        "MEMEConnection.cacheTermgroupRank(String) - finished.");
  }

  /**
   * Puts the semantic type into cache.
   * @throws DataSourceException if failed to cache semantic types.
   */
  protected void cacheSemanticTypes() throws DataSourceException {
    MEMEToolkit.logComment("  Cache semantic types", true);

    sty_cache.clear(getDataSourceName());
    // Query to look up STYs
    final String sty_query =
        "SELECT semantic_type, is_chem, chem_type, editing_chem, ui, stn, def " +
        "FROM semantic_types a, srsty b " +
        "WHERE semantic_type=sty";

    try {
      Statement sty_stmt = createStatement();
      ResultSet rs = sty_stmt.executeQuery(sty_query);

      // Get results
      while (rs.next()) {
        SemanticType sty = new SemanticType.Default();
        String is_chem = rs.getString("IS_CHEM");
        String is_editing_chem = rs.getString("EDITING_CHEM");
        sty.setChemicalType(rs.getString("CHEM_TYPE"));
        sty.setValue(rs.getString(Attribute.SEMANTIC_TYPE));
        sty.setIsChemical(is_chem != null && is_chem.equals("Y"));
        sty.setIsEditingChemical(is_editing_chem != null &&
                                 is_editing_chem.equals("Y"));
        sty.setTypeIdentifier(new Identifier.Default(rs.getString("UI")));
        sty.setTreePosition(rs.getString("STN"));
        sty.setDefinition(rs.getString("DEF"));

        // Add to hashmap
        sty_cache.put(getDataSourceName(), sty.getValue(), sty);
      }

      // Close statement
      sty_stmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to cache semantic types.", sty_query, se);
      dse.setDetail("query", sty_query);
      throw dse;
    }

    MEMEToolkit.trace(
        "MEMEConnection.cacheSemanticTypes() - finished.");
  }

  /**
   * Puts the inverse relationships into cache.
   * @throws DataSourceException if failed to cache inverse relationships.
   */
  protected void cacheRelInverses() throws DataSourceException {
    MEMEToolkit.logComment("  Cache relationship inverses", true);

    inverse_rel_cache.clear(getDataSourceName());
    
    // Look up inverse relationship names
    final String rel_query =
        "SELECT relationship_name, inverse_name " +
        "FROM inverse_relationships";

    try {
      Statement inv_stmt = createStatement();
      ResultSet rs = inv_stmt.executeQuery(rel_query);
      while (rs.next()) {
        String rel = rs.getString("RELATIONSHIP_NAME");
        String inv_rel = rs.getString("INVERSE_NAME");
        inverse_rel_cache.put(getDataSourceName(), rel, inv_rel);
      }

      // Close statement
      inv_stmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to cache inverse relationships.", rel_query, se);
      dse.setDetail("query", rel_query);
      throw dse;
    }

    MEMEToolkit.trace(
        "MEMEConnection.cacheRelInverses() - Finished: " +
        inverse_rel_cache);
  }

  /**
   * Puts the inverse relationships attribute into cache.
   * @param rela the relationship attribute
   * @throws DataSourceException if failed to cache inverse relationships attribute.
   */
  protected void cacheRelaInverses(String rela) throws DataSourceException {
    MEMEToolkit.logComment("  Cache relationship attribute inverses - " + rela, true);

    // Look up inverse relationship attributes
    String rela_query =
        "SELECT relationship_attribute, inverse_rel_attribute " +
        "FROM inverse_rel_attributes " +
        "WHERE relationship_attribute IS NOT NULL";

    if (rela != null) {
      rela_query = rela_query + " AND relationship_attribute = '" + rela + "'";
    } else {
      inverse_rela_cache.clear(getDataSourceName());
    }

    try {
      Statement inv_stmt = createStatement();
      ResultSet rs = inv_stmt.executeQuery(rela_query);
      while (rs.next()) {
        String r = rs.getString("RELATIONSHIP_ATTRIBUTE");
        String ir = rs.getString("INVERSE_REL_ATTRIBUTE");
        inverse_rela_cache.put(getDataSourceName(), r, ir);
      }

      // Close statement
      inv_stmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to cache inverse relas.", rela_query, se);
      dse.setDetail("query", rela_query);
      throw dse;
    }

    MEMEToolkit.trace(
        "MEMEConnection.cacheRelaInverses() - Finished: " +
        inverse_rela_cache);
  }

  /**
   * Puts the languages into cache.
   * @throws DataSourceException if failed to cache languages.
   */
  protected void cacheLanguages() throws DataSourceException {
    MEMEToolkit.logComment("  Cache languages", true);

    lat_cache.clear(getDataSourceName());
    
    final String query = "SELECT language, lat, iso_lat FROM language";

    try {
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);

      // Read
      while (rs.next()) {
        Language language = new Language.Default(
            rs.getString("LANGUAGE"), rs.getString("LAT"));
        lat_cache.put(getDataSourceName(), language.getAbbreviation(), language);
        lat_cache.put(getDataSourceName(), language.getISOAbbreviation(),
                      language);
      }

      // Close statement
      stmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up languages.", query, se);
      throw dse;
    }

  }

  /**
   * Puts the code map into cache.
   * @throws DataSourceException if failed to cache code map.
   */
  protected void cacheCodeMap() throws DataSourceException {
    MEMEToolkit.logComment("  Cache code map", true);

    code_map_cache.clear(getDataSourceName());
    
    final String query = "SELECT rowid, code, type, value FROM code_map";

    try {
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);

      // Read
      while (rs.next()) {
        Identifier id = new Identifier.Default(rs.getString("ROWID"));
        String code = rs.getString("CODE");
        String type = rs.getString("TYPE");
        String value = rs.getString("VALUE");
        MetaCode meta_code = new MetaCode(id, code, type, value);
        code_map_cache.put(getDataSourceName(), type + code, meta_code);
      }

      // Close statement
      stmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up code_map.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Puts the meta properties into cache.
   * @throws DataSourceException if failed to cache meta properties
   */
  protected void cacheMetaProperties() throws DataSourceException {
    MEMEToolkit.logComment("  Cache meta properties", true);

    meta_properties_cache.clear(getDataSourceName());
    final String query = "SELECT rowid, key, key_qualifier, value, " +
        "description, definition, example, reference FROM meme_properties";

    try {
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);

      // Read
      while (rs.next()) {
        Identifier id = new Identifier.Default(rs.getString("ROWID"));
        String key = rs.getString("KEY");
        String key_qualifier = rs.getString("KEY_QUALIFIER");
        String value = rs.getString("VALUE");
        String description = rs.getString("DESCRIPTION");
        String definition = rs.getString("DEFINITION");
        String example = rs.getString("EXAMPLE");
        String reference = rs.getString("REFERENCE");
        MetaProperty meta_prop =
            new MetaProperty(id, key, key_qualifier, value, description,
                             definition, example, reference);
        meta_properties_cache.put(getDataSourceName(),
                key + key_qualifier + value + description, meta_prop); //naveen UMLS-60: added description to the key 
      }

      // Close statement
      stmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up meme_properties.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#addContentView(ContentView)}
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to add content view
   */
  public void addContentView(ContentView cv) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.addContentView(ContentView)...");

    String insert_str = "INSERT INTO content_views ( "
        +
        "content_view_id, contributor, contributor_version, contributor_date, "
        + "maintainer, maintainer_version, maintainer_date, "
        + "content_view_name, content_view_description, "
        +
        "content_view_algorithm, content_view_category, content_view_subcategory, "
        + "content_view_class, content_view_code, "
        + "cascade, is_generated, content_view_previous_meta, "
        + "content_view_contributor_url, content_view_maintainer_url) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    DateFormat dateformat = MEMEToolkit.getDateFormat();
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(insert_str);
      pstmt.setInt(1, cv.getIdentifier().intValue());
      pstmt.setString(2, cv.getContributor());
      pstmt.setString(3, cv.getContributorVersion());
      pstmt.setString(4, dateformat.format(cv.getContributorDate()));
      pstmt.setString(5, cv.getMaintainer());
      pstmt.setString(6, cv.getMaintainerVersion());
      pstmt.setString(7, dateformat.format(cv.getMaintainerDate()));
      pstmt.setString(8, cv.getName());
      pstmt.setString(9, cv.getDescription());
      pstmt.setString(10, cv.getAlgorithm());
      pstmt.setString(11, cv.getCategory());
      pstmt.setString(12, cv.getSubCategory());
      pstmt.setString(13, cv.getContentViewClass());
      pstmt.setLong(14, cv.getCode());
      pstmt.setString(15, cv.getCascade() ? "Y" : "N");
      pstmt.setString(16, cv.isGeneratedByQuery() ? "Y" : "N");
      pstmt.setString(17, cv.getPreviousMeta());
      pstmt.setString(18, cv.getContributorURL());
      pstmt.setString(19, cv.getMaintainerURL());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to content_view.", insert_str, se);
      dse.setDetail("insert", insert_str);
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#removeContentView(ContentView)}
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to remove content view
   */
  public void removeContentView(ContentView cv) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.removeContentView(ContentView)...");

    String remove_str = "DELETE FROM content_views " +
        "WHERE content_view_id = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.setInt(1, cv.getIdentifier().intValue());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from content_views.", remove_str, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }
    removeContentViewMembers(cv);
  }

  /**
   * Implements {@link MEMEDataSource#setContentView(ContentView)}
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to set content view
   */
  public void setContentView(ContentView cv) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.setContentView(ContentView)...");

    String update_str = "UPDATE content_views "
        +
        "SET contributor = ?, contributor_version = ?, contributor_date = ?, "
        + "maintainer = ?, maintainer_version = ?, maintainer_date = ?, "
        + "content_view_name = ?, content_view_description = ?, "
        + "content_view_algorithm = ?, content_view_category = ?, "
        + "content_view_subcategory = ?, content_view_class = ?, "
        + "content_view_code = ?, cascade = ?, is_generated = ?, "
        + "content_view_previous_meta = ?, content_view_contributor_url = ?, "
        + "content_view_maintainer_url = ? "
        + "WHERE content_view_id = ?";

    DateFormat dateformat = MEMEToolkit.getDateFormat();
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(update_str);
      pstmt.setString(1, cv.getContributor());
      pstmt.setString(2, cv.getContributorVersion());
      pstmt.setString(3, dateformat.format(cv.getContributorDate()));
      pstmt.setString(4, cv.getMaintainer());
      pstmt.setString(5, cv.getMaintainerVersion());
      pstmt.setString(6, dateformat.format(cv.getMaintainerDate()));
      pstmt.setString(7, cv.getName());
      pstmt.setString(8, cv.getDescription());
      pstmt.setString(9, cv.getAlgorithm());
      pstmt.setString(10, cv.getCategory());
      pstmt.setString(11, cv.getSubCategory());
      pstmt.setString(12, cv.getContentViewClass());
      pstmt.setLong(13, cv.getCode());
      pstmt.setString(14, cv.getCascade() ? "Y" : "N");
      pstmt.setString(15, cv.isGeneratedByQuery() ? "Y" : "N");
      pstmt.setString(16, cv.getPreviousMeta());
      pstmt.setString(17, cv.getContributorURL());
      pstmt.setString(18, cv.getMaintainerURL());
      pstmt.setInt(19, cv.getIdentifier().intValue());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to update row from content_views.", update_str, se);
      dse.setDetail("update", update_str);
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getContentView(Identifier)}
   * @param id the {@link Identifier}
   * @return the of {@link ContentView}
   * @throws DataSourceException if failed to get content view
   * @throws BadValueException if failed due to invalid data value
   */
  public ContentView getContentView(Identifier id) throws DataSourceException {
    return getContentView(id.intValue());
  }

  /**
   * Implements {@link MEMEDataSource#getContentView(Identifier)}
   * @param id the content view id
   * @return the of {@link ContentView}
   * @throws DataSourceException if failed to get content view
   * @throws BadValueException if failed due to invalid data value
   */
  public ContentView getContentView(int id) throws DataSourceException {

    ContentView cv = new ContentView.Default();

    // Query to get content view
    final String query = "SELECT "
        +
        "content_view_id, contributor, contributor_version, contributor_date, "
        + "maintainer, maintainer_version, maintainer_date, "
        + "content_view_name, content_view_description, "
        +
        "content_view_algorithm, content_view_category, content_view_subcategory, "
        + "content_view_class, content_view_code, "
        + "cascade, is_generated, content_view_previous_meta, "
        + "content_view_contributor_url, content_view_maintainer_url "
        + "FROM content_views WHERE content_view_id = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setInt(1, id);
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        cv.setIdentifier(new Identifier.Default(rs.getInt("CONTENT_VIEW_ID")));
        cv.setContributor(rs.getString("CONTRIBUTOR"));
        cv.setContributorVersion(rs.getString("CONTRIBUTOR_VERSION"));
        cv.setContributorDate(new Date(rs.getDate("CONTRIBUTOR_DATE").getTime()));
        cv.setMaintainer(rs.getString("MAINTAINER"));
        cv.setMaintainerVersion(rs.getString("MAINTAINER_VERSION"));
        cv.setMaintainerDate(new Date(rs.getDate("MAINTAINER_DATE").getTime()));
        cv.setName(rs.getString("CONTENT_VIEW_NAME"));
        cv.setDescription(rs.getString("CONTENT_VIEW_DESCRIPTION"));
        cv.setAlgorithm(rs.getString("CONTENT_VIEW_ALGORITHM"));
        cv.setCategory(rs.getString("CONTENT_VIEW_CATEGORY"));
        cv.setSubCategory(rs.getString("CONTENT_VIEW_SUBCATEGORY"));
        cv.setContentViewClass(rs.getString("CONTENT_VIEW_CLASS"));
        cv.setCode(rs.getLong("CONTENT_VIEW_CODE"));
        cv.setCascade(rs.getString("CASCADE").equals("Y") ? true : false);
        cv.setIsGeneratedByQuery(rs.getString("IS_GENERATED").equals("Y") ? true : false);
        cv.setPreviousMeta(rs.getString("CONTENT_VIEW_PREVIOUS_META"));
        cv.setContributorURL(rs.getString("CONTENT_VIEW_CONTRIBUTOR_URL"));
        cv.setMaintainerURL(rs.getString("CONTENT_VIEW_MAINTAINER_URL"));
      }

      // Close statement
      pstmt.close();

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to get content view.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }

    // return
    return cv;

  }

  /**
   * Implements {@link MEMEDataSource#getContentViews()}
   * @return An array of {@link ContentView}
   * @throws DataSourceException if failed to get content views
   * @throws BadValueException if failed due to invalid data value
   */
  public ContentView[] getContentViews() throws DataSourceException {

    List views = new ArrayList();

    // Query to get content views
    final String query = "SELECT content_view_id "
        + "FROM content_views";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        ContentView cv = getContentView(rs.getInt("CONTENT_VIEW_ID"));
        views.add(cv);
      }

      // Close statement
      pstmt.close();

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to get content views.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }

    // return
    return (ContentView[]) views.toArray(new ContentView[0]);

  }

  /**
   * Implements {@link MEMEDataSource#removeContentViewMembers(ContentView)}
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to remove content views
   */
  public void removeContentViewMembers(ContentView cv) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.removeContentViewMembers(ContentView)...");

    String update_str = "UPDATE content_view_members " +
        "SET code = code - ? WHERE ? = BITAND(code, ?)";
    String remove_str = "DELETE FROM content_view_members " +
        "WHERE code = 0";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(update_str);
      pstmt.setLong(1, cv.getCode());
      pstmt.setLong(2, cv.getCode());
      pstmt.setLong(3, cv.getCode());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to update rows from content_view_members.", cv, se);
      dse.setDetail("update", update_str);
      throw dse;
    }
    Statement stmt = null;
    try {
      stmt = createStatement();
      stmt.execute(remove_str);
      stmt.close();
    }
      catch (SQLException se) {
        try {
          stmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to remove row from content_view_members.", cv, se);
        dse.setDetail("remove", remove_str);
        throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#addContentViewMember(ContentViewMember)}
   * @param member the {@link ContentViewMember}
   * @throws DataSourceException if failed to add content view members
   */
  public void addContentViewMember(ContentViewMember member) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.addContentViewMembers(ContentViewMember)...");

    String select_str = "SELECT * FROM content_view_members "
          + "WHERE meta_ui = ? AND cascade = ?";
    String insert_str = "INSERT INTO content_view_members ( "
        + "meta_ui, code, cascade) VALUES (?, ?, ?)";
    String upate_str = "UPDATE content_view_members "
        + "SET code = code +  ? "
        + "WHERE meta_ui = ? AND cascade = ?";

    PreparedStatement pstmt = null;
    ResultSet rset = null;
    try {
      pstmt = prepareStatement(select_str);
      pstmt.setString(1, member.getIdentifier().toString());
      pstmt.setString(2, member.getContentView().getCascade() ? "Y" : "N");
      rset = pstmt.executeQuery();
      if(rset.next())  {
      	pstmt.close();
        pstmt = prepareStatement(upate_str);
        pstmt.setLong(1,member.getContentView().getCode());
        pstmt.setString(2, member.getIdentifier().toString());
        pstmt.setString(3, member.getContentView().getCascade() ? "Y" : "N");
        pstmt.executeUpdate();
      }
      else {
      	pstmt.close();
        pstmt = prepareStatement(insert_str);
        pstmt.setString(1, member.getIdentifier().toString());
        pstmt.setLong(2,member.getContentView().getCode());
        pstmt.setString(3, member.getContentView().getCascade() ? "Y" : "N");
        pstmt.executeUpdate();
      }

      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to content_view members.", member, se);
      dse.setDetail("insert", insert_str);
      throw dse;
    }

    // Update generated status
    setIsGenerated("N", member.getContentView());
  }

  /**
   * Implements {@link MEMEDataSource#removeContentViewMember(ContentViewMember)}
   * @param member the {@link ContentViewMember}
   * @throws DataSourceException if failed to remove content view members
   */
  public void removeContentViewMember(ContentViewMember member) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.removeContentViewMember(ContentViewMember)...");

      String update_str = "UPDATE content_view_members " +
          "SET code = code - ? " +
          "WHERE meta_ui = ? AND cascade = ?";

      String remove_str = "DELETE FROM content_view_members " +
          "WHERE code = 0";
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(update_str);
        pstmt.setLong(1, member.getContentView().getCode());
        pstmt.setString(2,member.getIdentifier().toString());
        pstmt.setString(3, member.getContentView().getCascade() ? "Y" : "N");
        pstmt.executeUpdate();
        pstmt.close();
      }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to update rows from content_view_members.", member, se);
      dse.setDetail("update", update_str);
      throw dse;
    }
    Statement stmt = null;
    try {
      stmt = createStatement();
      stmt.execute(remove_str);
      stmt.close();
    }
      catch (SQLException se) {
        try {
          stmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to remove row from content_view_members.", member, se);
        dse.setDetail("remove", remove_str);
        throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#addContentViewMembers(ContentViewMember[])}
   * @param members An array of object {@link ContentViewMember}
   * @throws DataSourceException if failed to add content view members
   */
  public void addContentViewMembers(ContentViewMember[] members) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.addContentViewMembers(ContentViewMember[])...");

    for (int i = 0; i < members.length; i++) {
      addContentViewMember(members[i]);
    }
  }

  /**
   * Implements {@link MEMEDataSource#removeContentViewMembers(ContentViewMember[])}
   * @param members the {@link ContentViewMember}s to remove
   * @throws DataSourceException if failed to remove content view members
   */
  public void removeContentViewMembers(ContentViewMember[] members) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.removeContentViewMembers(ContentViewMember[])...");

      for (int i = 0; i < members.length; i++) {
        removeContentViewMember(members[i]);
      }
  }

  /**
   * Implements {@link MEMEDataSource#generateContentViewMembers(ContentView)}
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to generate content view members
   */
  public void generateContentViewMembers(ContentView cv) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.generateContentViewMember(ContentView)...");

    String insert_str = "INSERT INTO content_view_members " +
        "(meta_ui, code, cascade) " +
        "SELECT meta_ui, ?, ? FROM (" + cv.getAlgorithm() +
        ") MINUS " +
        "SELECT meta_ui, ?, ? FROM content_view_members "  +
        "WHERE cascade = ?" ;

    String update_str = "UPDATE content_view_members a " +
        "SET code = code + ? " +
        "WHERE meta_ui IN (" + cv.getAlgorithm() + ")" +
        "AND cascade = ? " ;

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(update_str);
      pstmt.setLong(1, cv.getCode());
      pstmt.setString(2, cv.getCascade() ? "Y" : "N");
      pstmt.executeUpdate();
      pstmt.close();
      pstmt = prepareStatement(insert_str);
      pstmt.setLong(1, cv.getCode());
      pstmt.setString(2, cv.getCascade() ? "Y" : "N");
      pstmt.setLong(3, cv.getCode());
      pstmt.setString(4, cv.getCascade() ? "Y" : "N");
      pstmt.setString(5, cv.getCascade() ? "Y" : "N");
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to generate row into content_view_members.", cv, se);
      dse.setDetail("insert", insert_str);
      dse.setDetail("update", update_str);
      throw dse;
    }

    // Update generated status
    setIsGenerated("Y", cv);
  }

  /**
   * Sets generated status
   * @param generated value to assign
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to set generated status
   */
  private void setIsGenerated(String generated, ContentView cv) throws
      DataSourceException {

    String generated_str = "UPDATE content_views " +
        "SET is_generated = '" + generated + "', maintainer_date = sysdate " +
        "WHERE content_view_id = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(generated_str);
      pstmt.setInt(1, cv.getIdentifier().intValue());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to set generated status of content views.", generated, se);
      dse.setDetail("generated", generated_str);
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#getContentViewMembers(ContentView)}
   * @param cv the {@link ContentView}
   * @param start the start index
   * @param end the end index
   * @return An array of {@link ContentViewMember}
   * @throws DataSourceException if failed to get content view members
   */
  public ContentViewMember[] getContentViewMembers(ContentView cv, int start, int end) throws
      DataSourceException {

    List members = new ArrayList();

    // Query to get content view
    final String query = "SELECT meta_ui, atom_id, concept_id " +
    "FROM content_view_members a, classes b " +
    "WHERE ? = BITAND(a.code,?) AND cascade = ? " + 
    "AND tobereleased in ('Y','y') AND meta_ui = b.aui " + 
    "UNION ALL " +
    "SELECT meta_ui, null, null from content_View_members a, code_map b " +
    "WHERE ? = BITAND(a.code,?) AND cascade = ? " + 
    "AND type='ui_prefix' AND b.code='AUI' " +
    "AND NOT regexp_like(meta_ui,b.value||'[[:digit:]]')" + 
    "ORDER BY concept_id ";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      pstmt.setLong(1, cv.getCode());
      pstmt.setLong(2, cv.getCode());
      pstmt.setString(3, cv.getCascade() ? "Y" : "N");
      pstmt.setLong(4, cv.getCode());
      pstmt.setLong(5, cv.getCode());
      pstmt.setString(6, cv.getCascade() ? "Y" : "N");
      ResultSet rs = pstmt.executeQuery();
      
      if(start > 0)
    	  rs.absolute(start);
      // Read
      while (rs.next()) {
        ContentViewMember member = new ContentViewMember.Default();
        member.setContentView(cv);
        member.setIdentifier(new Identifier.Default(rs.getString("META_UI")));
        if(rs.getInt("ATOM_ID") != 0) {
	      	Atom atom = getAtomWithName(rs.getInt("ATOM_ID"), null);
	      	member.setAtom(atom);
        }
        members.add(member);
        if(rs.getRow() == end) {
        	break;
        }
      }

      // Close statement
      pstmt.close();

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to get content view.", cv, se);
      dse.setDetail("query", query);
      throw dse;
    } catch(BadValueException bve) {
        DataSourceException dse = new DataSourceException(
                "Failed to get content view.", cv, bve);
            dse.setDetail("query", query);
            throw dse;
    	
    }

    // return
    return (ContentViewMember[]) members.toArray(new ContentViewMember[0]);

  }

  /**
   * Maps the specified old CUI through the CUI history information in the MID.
   * Returns the best matching current CUI, or <code>null</code>.
   * @param cui the {@link CUI} to map
   * @return the best matching current CUI, or <code>null</code>
   * @throws DataSourceException if anything goes wrong
   */
  public CUI mapCUIThroughHistory(CUI cui) throws DataSourceException {
    return mapCUIThroughHistory(cui, false);
  }

  /**
   * Maps the specified old CUI through the CUI history information in the MID.
   * Returns the best matching current CUI, or <code>null</code>.
   * @param cui the {@link CUI} to map
   * @param sy_only indicates whether or not to map only through "synonymy facts"
   * @return the best matching current CUI, or <code>null</code>
   * @throws DataSourceException if anything goes wrong
   */
  public CUI mapCUIThroughHistory(CUI cui, boolean sy_only) throws
      DataSourceException {
    final String query =
        "SELECT relationship_name, cui2 FROM cui_history WHERE cui1 = ?";
    PreparedStatement pstmt = null;
    try {
      // default is null, means we did not find it in cui_history
      CUI mapped_cui = null;
      pstmt = prepareStatement(query);
      pstmt.setString(1, cui.toString());
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        String cui2 = rs.getString("CUI2");
        String rel = rs.getString("RELATIONSHIP_NAME");
        if (rel.equals("DEL")) {
          // If del we are done
          mapped_cui = null;
          break;
        }
        else if (rel.equals("SY")) {
          // If sy we are done
          mapped_cui = new CUI(cui2);
          break;
        }
        else if (!sy_only && (rel.equals("RB") || rel.equals("RN"))) {
          // RB/RN are better than RO
          mapped_cui = new CUI(cui2);
        }
        else if (rel.equals("RO") && mapped_cui == null && !sy_only) {
          // ONLY use if better mapping not found
          mapped_cui = new CUI(cui2);
        }
      }
      pstmt.close();
      return mapped_cui;
    }
    catch (SQLException sqe) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to map to current CUI", cui, sqe);
      dse.setDetail("query", query);
      dse.setDetail("cui", cui);
      throw dse;
    }
  }

  /**
   * Maps the specifired {@link NativeIdentifier} to its corresponding {@link CoreData}
   * element.
   * @param id the {@link NativeIdentifier} to map
   * @param ticket the {@link Ticket}
   * @return the {@link CoreData} corresponding to the native identifier (or <code>null</code>)
   * @throws DataSourceException
   */
  public CoreData mapNativeIdentifier(NativeIdentifier id, Ticket ticket) throws
      DataSourceException {

    StringBuffer sb = new StringBuffer(500);
    CallableStatement cstmt = null;
    try {

      sb.append("{call MEME_UTILITY.map_sg_fields(sg_id => '")
          .append(id.toString())
          .append("', sg_type => '")
          .append(id.getType())
          .append("', sg_qualifier => '")
          .append(id.getQualifier())
          .append("', sg_meme_id => ? ")
          .append(", sg_meme_data_type => ? ")
          .append(", atom_id => ?'")
          .append("')}");

      cstmt = prepareCall(sb.toString());
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.registerOutParameter(2, Types.VARCHAR);
      cstmt.registerOutParameter(3, Types.INTEGER);
      cstmt.execute();
      int sg_meme_id = cstmt.getInt(1);
      String sg_meme_data_type = cstmt.getString(2);
      int atom_id = cstmt.getInt(3);
      cstmt.close();

      if (sg_meme_data_type.equals("R")) {
        return getRelationship(sg_meme_id, ticket);
      }

      return getAtom(atom_id, ticket);

    }
    catch (BadValueException bve) {
      return null;
    }
    catch (SQLException se) {
      try {
        cstmt.close();
      }
      catch (SQLException se2) {}
      DataSourceException dse = new DataSourceException(
          "Failed to call map_sg_id.", id, se);
      throw dse;
    }

  }

  /**
   * Implements {@link MEMEDataSource#addMetaCode(MetaCode)}
   * @param mcode the {@link MetaCode}
   * @throws DataSourceException if failed to add meta code
   */
  public void addMetaCode(MetaCode mcode) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.addMetaCode(MetaCode)...");

    String insert_str =
        "INSERT INTO code_map (code, type, value) VALUES (?, ?, ?)";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(insert_str);
      pstmt.setString(1, mcode.getCode());
      pstmt.setString(2, mcode.getType());
      pstmt.setString(3, mcode.getValue());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to code map.", mcode, se);
      dse.setDetail("insert", insert_str);
      throw dse;
    }

    // Update the cache
    cacheCodeMap();

    mcode.setIdentifier(getMetaCode(mcode.getCode(), mcode.getType()).
                        getIdentifier());

  }

  /**
   * Implements {@link MEMEDataSource#removeMetaCode(MetaCode)}
   * @param mcode the {@link MetaCode}
   * @throws DataSourceException if failed to remove meta code
   */
  public void removeMetaCode(MetaCode mcode) throws DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.removeMetaCode(MetaCode)...");

    String remove_str =
        "DELETE FROM code_map WHERE rowid = '" +
        mcode.getIdentifier().toString() + "'";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from meta code.", mcode, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

    // Update the cache
    cacheCodeMap();

  }

  /**
   * Implements {@link MEMEDataSource#getMetaCode(String, String)}.
   * @param code the meta code code
   * @param type the meta code type
   * @return the {@link MetaCode}
   */
  public MetaCode getMetaCode(String code, String type) {
    if (code == null || type == null) {
      return null;
    }

    return (MetaCode) code_map_cache.get(getDataSourceName(), type + code);
  }

  /**
   * Implements {@link MEMEDataSource#getMetaCodeTypes()}.
   * @return An array representing the meta code types
   */
  public String[] getMetaCodeTypes() {
    Set type_set = new HashSet();
    MetaCode[] meta_codes = (MetaCode[]) code_map_cache.values(
        getDataSourceName()).toArray(new MetaCode[0]);
    for (int i = 0; i < meta_codes.length; i++) {
      type_set.add(meta_codes[i].getType());
    }
    return (String[]) type_set.toArray(new String[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getMetaCodesByType(String)}.
   * @param type the meta code type
   * @return An array of object {@link MetaCode}
   */
  public MetaCode[] getMetaCodesByType(String type) {
    if (type == null) {
      return null;
    }

    List mc_list = new ArrayList();
    MetaCode[] meta_codes = (MetaCode[]) code_map_cache.values(
        getDataSourceName()).toArray(new MetaCode[0]);
    for (int i = 0; i < meta_codes.length; i++) {
      if (meta_codes[i].getType().equals(type)) {
        mc_list.add(meta_codes[i]);
      }
    }
    return (MetaCode[]) mc_list.toArray(new MetaCode[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getMetaCodes()}.
   * @return An array of object {@link MetaCode}
   */
  public MetaCode[] getMetaCodes() {
    return (MetaCode[]) code_map_cache.values(getDataSourceName()).toArray(new
        MetaCode[0]);
  }

  /**
   * Implements {@link MEMEDataSource#addMetaProperty(MetaProperty)}
   * @param meme_prop the {@link MetaProperty}
   * @throws DataSourceException if failed to add meta property
   */
  public void addMetaProperty(MetaProperty meme_prop) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.addMetaProperty(MetaProperty)...");

    String insert_str =
        "INSERT INTO meme_properties (key, key_qualifier, value, " +
        " description, definition, example, reference) VALUES (?, ?, ?, ?, ?, ?, ?)";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(insert_str);
      pstmt.setString(1, meme_prop.getKey());
      pstmt.setString(2, meme_prop.getKeyQualifier());
      pstmt.setString(3, meme_prop.getValue());
      pstmt.setString(4, meme_prop.getDescription());
      pstmt.setString(5, meme_prop.getDefinition());
      pstmt.setString(6, meme_prop.getExample());
      pstmt.setString(7, meme_prop.getReference());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to meme properties.", meme_prop, se);
      dse.setDetail("insert", insert_str);
      throw dse;
    }

    // Update the cache
    cacheMetaProperties();

    meme_prop.setIdentifier(
        getMetaProperty(meme_prop.getKey(),
                        meme_prop.getKeyQualifier(),
                        meme_prop.getValue(),
                        meme_prop.getDescription()).getIdentifier());//naveen UMLS-60: added description parameter to getMetaProperty method 

  }

  /**
   * Implements {@link MEMEDataSource#removeMetaProperty(MetaProperty)}
   * @param meme_prop the {@link MetaProperty}
   * @throws DataSourceException if failed to remove meta property
   */
  public void removeMetaProperty(MetaProperty meme_prop) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MEMEConnection.removeMetaProperty(MetaProperty)...");

    String remove_str =
        "DELETE FROM meme_properties WHERE rowid = '" +
        meme_prop.getIdentifier().toString() + "'";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(remove_str);
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from meme property.", meme_prop, se);
      dse.setDetail("remove", remove_str);
      throw dse;
    }

    // Update the cache
    cacheMetaProperties();

  }

  /**
   * Implements {@link MEMEDataSource#getMetaProperty(String, String, String)}
   * @param key the key
   * @param key_qualifier the key qualifier
   * @param value the value
   * @return the {@link MetaProperty}
   */
  public MetaProperty getMetaProperty(String key, String key_qualifier,
                                      String value, String description) {//naveen UMLS-60: added description parameter to getMetaProperty method
    if (key == null || key_qualifier == null || value == null) {
      return null;
    }
    return (MetaProperty) meta_properties_cache.get(getDataSourceName(),
            key + key_qualifier + value + description);
  }

  /**
   * Implements {@link MEMEDataSource#getMetaProperties()}
   * @return an array of object {@link MetaProperty}
   */
  public MetaProperty[] getMetaProperties() {
    return (MetaProperty[]) meta_properties_cache.values(getDataSourceName()).
        toArray(new MetaProperty[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getMetaPropertiesByKeyQualifier(String key_qualifier)}
   * @param key_qualifier the key qualifier
   * @return an array of object {@link MetaProperty}
   */
  public MetaProperty[] getMetaPropertiesByKeyQualifier(String key_qualifier) {

    if (key_qualifier == null) {
      return null;
    }

    MetaProperty[] meta_props =
        (MetaProperty[]) meta_properties_cache.values(getDataSourceName()).
        toArray(new MetaProperty[0]);

    List mp_list = new ArrayList();
    for (int i = 0; i < meta_props.length; i++) {
      if (meta_props[i].getKeyQualifier().equals(key_qualifier)) {
        mp_list.add(meta_props[i]);
      }
    }
    return (MetaProperty[]) mp_list.toArray(new MetaProperty[0]);

  }

  /**
   * Implements {@link MEMEDataSource#getMetaPropertyKeyQualifiers()}.
   * @return a list of meta property key qualifiers
   */
  public String[] getMetaPropertyKeyQualifiers() {
    Set key_qualifier_set = new HashSet();
    MetaProperty[] meta_props =
        (MetaProperty[]) meta_properties_cache.values(getDataSourceName()).
        toArray(new MetaProperty[0]);
    for (int i = 0; i < meta_props.length; i++) {
      key_qualifier_set.add(meta_props[i].getKeyQualifier());
    }
    return (String[]) key_qualifier_set.toArray(new String[0]);
  }

  /**
   * Implements {@link MEMEDataSource#assignCuis()}.
   * @return the most recent cui assignment log
   * @throws DataSourceException if failed to assign cuis
   */
  public String assignCuis() throws DataSourceException {
    return assignCuis(
        newWorkLog(getAuthority("MTH"),
                   "MAINTENANCE", "ASSIGN_CUIS_FULL"));
  }

  /**
   * Implements {@link MEMEDataSource#assignCuis()}.
   * @param work the {@link WorkLog}
   * @return the most recent cui assignment log
   * @throws DataSourceException if failed to assign cuis
   */
  public String assignCuis(WorkLog work) throws DataSourceException {

    MEMEToolkit.trace("MEMEConnection.assignCuis()...");

    String call_str = "{? = call MEME_OPERATIONS.assign_cuis(" +
        "authority => 'MTH', work_id => ?, table_name => 'EMPTY_TABLE', " +
        "new_cui_flag => 'Y', all_flag => 'Y', " +
        "qa_flag => 'Y')}";

    // Assign cuis
    try {
      CallableStatement cstmt = prepareCall(call_str);
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.setInt(2, work.getIdentifier().intValue());
      cstmt.execute();
      //int work_id = cstmt.getInt(2);
      cstmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to assign cuis.", call_str, se);
      throw dse;
    }

    return flushBuffer();

  }

  /**
   * Implements {@link MEMEDataSource#assignCuis(Concept)}.
   * @param source the {@link Concept}
   * @throws DataSourceException if failed to assign cuis
   */
  public void assignCuis(Concept source) throws DataSourceException {
    assignCuis(source, null);
  }

  /**
   * Implements {@link MEMEDataSource#assignCuis(Concept, Concept)}.
   * @param source the {@link Concept} represents a source concept
   * @param target the {@link Concept} represents a target concept
   * @throws DataSourceException if failed to assign cuis
   */
  public void assignCuis(Concept source, Concept target) throws
      DataSourceException {

    //
    // 0. Verify the system status
    //
    if (getSystemStatus("live_cui_assignment").equals("OFF")) {
      return;
    }

    //
    // 1. Map suspect concepts
    //
    List suspect_concepts = new ArrayList();
    suspect_concepts.add(source.getIdentifier());
    if (target != null) {
      suspect_concepts.add(target.getIdentifier());

      //
      // 2. Add all suspect concepts
      //
    }
    while (true) {

      //
      // Build list of CUI values
      //
      StringBuffer sb = new StringBuffer(500);
      Iterator iterator = suspect_concepts.iterator();
      while (iterator.hasNext()) {
        Identifier concept_id = (Identifier) iterator.next();
        sb.append(concept_id.toString());
        if (iterator.hasNext()) {
          sb.append(",");
        }
      }
      final String list = sb.toString();

      //
      // Build query
      //
      final String query = "SELECT DISTINCT concept_id FROM classes" +
          " WHERE last_release_cui IN" +
          " (SELECT last_release_cui FROM classes" +
          "  WHERE concept_id IN (" + list + "))" +
          " AND last_release_cui IS NOT NULL" +
          " AND concept_id NOT IN (" + list + ")";

      //
      // Select additional concepts
      //
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        boolean found = false;
        while (rs.next()) {
          found = true;
          int concept_id = rs.getInt("CONCEPT_ID");
          suspect_concepts.add(new Identifier.Default(concept_id));
        }
        pstmt.close();
        if (!found) {
          break;
        }

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to select from classes.", query, se);
        dse.setDetail("query", query);
        throw dse;
      }
    }

    //
    // 3. Populate all suspect concepts and add all atoms into the list
    //
    final List atom_list = new ArrayList();
    final Ticket ticket = new Ticket();
    final List removed_atoms = new ArrayList();
    ticket.setReadAtoms(true);
    ticket.setReadConcept(true);
    for (int i = 0; i < suspect_concepts.size(); i++) {

      //
      // Get concept
      //
      final Identifier id = (Identifier) suspect_concepts.get(i);
      final Concept concept = new Concept.Default(id.intValue());

      //
      // Populate concept
      //
      try {
        populateConcept(concept, ticket);
      }
      catch (MissingDataException mde) {
        // If any of the concepts no longer exist, skip
        continue;
      }
      catch (BadValueException bve) {
        DataSourceException dse = new DataSourceException(
            "Failed to populate concept.", concept, bve);
        throw dse;
      }

      //
      // Get atoms, compute rank, add to list
      //
      final Atom[] atoms = concept.getAtoms();
      for (int j = 0; j < atoms.length; j++) {
        if (atoms[j].getLastReleaseCUI() != null ||
            atoms[j].getLastAssignedCUI() != null) {
          atom_list.add(atoms[j]);
          final StringBuffer extended_rank = new StringBuffer(30);
          extended_rank.append(atoms[j].getRank().toString().substring(0,1))
          	  .append(10000 + atoms[j].getTermgroup().getReleaseRank().intValue())
              .append(atoms[j].getLastReleaseRank().intValue())
              .append(999999999 - atoms[j].getSUI().intValue())
              .append(999999999 - (atoms[j].getAUI() == null ? 1 : atoms[j].getAUI().intValue()))
              .append(10000000000L + atoms[j].getIdentifier().intValue());
          atoms[j].setRank(new Rank.Default(extended_rank.toString()));
          if (atoms[j].getLastReleaseCUI() == null) {
            atoms[j].setLastReleaseCUI(atoms[j].getLastAssignedCUI());
          }
        }

        else {
          if (atoms[j].isReleasable()) {
            removed_atoms.add(atoms[j]);
          }
        }
      }
    }

    //
    // 4. Sort on rank and map cui assignments
    //
    final Comparator comp = new Comparator() {
      public int compare(Object o1, Object o2) {
        Rankable r1 = (Rankable) o1;
        Rankable r2 = (Rankable) o2;
        return r2.getRank().compareTo(r1.getRank());
      }
    };
    Collections.sort(atom_list, comp);

    //
    // Make CUI assignments in rank order
    //
    final Map cui_assignments = new HashMap();
    while (atom_list.size() > 0) {
      final Atom atom = (Atom) atom_list.get(0);
      cui_assignments.put(atom.getConcept().getIdentifier(),
                          atom.getLastReleaseCUI());
      for (int i = atom_list.size() - 1; i >= 0; i--) {
        Atom atom2 = (Atom) atom_list.get(i);

        //
        // Remove candidates if either CUI or concept_id matches
        //
        if (atom2.getConcept().equals(atom.getConcept())) {
          atom_list.remove(atom2);
          removed_atoms.add(atom2);

        }
        else if (atom2.getLastReleaseCUI().equals(atom.getLastReleaseCUI())) {

          //
          // If last_release_cui has been assigned, but not last_assigned_cui
          // In other words, an atom may have two CUI assignemtns, and the
          // last_release_cui is higher ranked, we try to assign it first.
          //
          if (atom2.getLastAssignedCUI() == null ||
              atom2.getLastReleaseCUI().equals(atom2.getLastAssignedCUI()) ||
              cui_assignments.containsKey(atom2.getLastAssignedCUI())) {
            atom_list.remove(atom2);
            removed_atoms.add(atom2);
          }
          else {
            atom2.setLastReleaseCUI(atom2.getLastAssignedCUI());
          }
        }
      }
    }

    //
    // 5. New CUIs (only for concepts with releasable atoms)
    //
    final String cui_prefix = getMetaCode("CUI", "ui_prefix").getValue();
    final int cui_length = Integer.parseInt(getMetaCode("CUI", "ui_length").
                                            getValue());
    for (int i = 0; i < removed_atoms.size(); i++) {
      final Atom atom = (Atom) removed_atoms.get(i);
      if (atom.isReleasable() &&
          !cui_assignments.containsKey(atom.getConcept().getIdentifier())) {
        Identifier id = getNextIdentifierForType(CUI.class);
        final String str = "0000000000" + id.toString();
        id = new Identifier.Default(str.substring(str.length() - cui_length));
        cui_assignments.put(atom.getConcept().getIdentifier(),
                            new CUI(cui_prefix + id));
      }
    }

    // 5.a Erase exisiting CUI assignment if no releasable atom exist in the concept
    for (int i = 0; i < suspect_concepts.size(); i++) {
      if (!cui_assignments.containsKey(suspect_concepts.get(i))) {
        cui_assignments.put(suspect_concepts.get(i), new CUI(""));
      }
    }

    //
    // 6. Foreach assigned CUIs, update concept_status and classes
    //
    final String update_concept = "UPDATE concept_status" +
        " SET cui = ? WHERE concept_id = ?";

    final String update_atom = "UPDATE classes" +
        " SET last_assigned_cui = ? WHERE concept_id = ?";

    final Set set = cui_assignments.entrySet();
    final Iterator iterator = set.iterator();
    while (iterator.hasNext()) {
      final Map.Entry me = (Map.Entry) iterator.next();

      //
      // Update concept_status.cui
      //
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(update_concept);
        pstmt.setString(1, me.getValue().toString());
        pstmt.setInt(2, Integer.valueOf(me.getKey().toString()).intValue());
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to update concept_status.", update_concept, se);
        dse.setDetail("update", update_concept);
        throw dse;
      }

      //
      // Update classes.last_assigned_cui
      //
      pstmt = null;
      try {
        pstmt = prepareStatement(update_atom);
        pstmt.setString(1, me.getValue().toString());
        pstmt.setInt(2, Integer.valueOf(me.getKey().toString()).intValue());
        pstmt.executeUpdate();
        pstmt.close();
      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to update classes.", update_atom, se);
        dse.setDetail("update", update_atom);
        throw dse;
      }
    }
  }

  /**
   * Implements {@link MEMEDataSource#setSystemStatus(String, String)}
   * @param key the key
   * @param value the value
   * @throws DataSourceException if failed to set system status
   */
  public void setSystemStatus(String key, String value) throws
      DataSourceException {

    String update_str = "UPDATE system_status " +
        "SET status = ? WHERE system = ?";

    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(update_str);
      pstmt.setString(1, value);
      pstmt.setString(2, key);
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to set system status.", update_str, se);
      dse.setDetail("update_str", update_str);
      throw dse;
    }

  }

  /**
   * Implements {@link MEMEDataSource#getSystemStatus(String)}
   * @param key the key
   * @return the system status
   * @throws DataSourceException if failed to set system status
   */
  public String getSystemStatus(String key) throws DataSourceException {

    // Query to get system status
    final String query = "SELECT status "
        + "FROM system_status WHERE system = ?";

    String status = null;
    PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, key);
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        status = rs.getString("STATUS");
      }

      // Close statement
      pstmt.close();

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to get system status.", key, se);
      dse.setDetail("query", query);
      throw dse;
    }

    // return
    return status;

  }

  /**
   * Implements {@link MEMEDataSource#newWorkLog(Authority, String, String)}
   * @param authority the {@link String} representation of authority.
   * @param type object {@link String} representation of work type.
   * @param description the {@link String} representation of work description.
   * @return the {@link WorkLog}.
   * @throws DataSourceException if new worklog could not be created.
   */
  public WorkLog newWorkLog(Authority authority, String type,
                            String description) throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.newWorkLog(String,String,String)...");

    String query = "{? = call MEME_UTILITY.new_work(" +
        "authority => ?, type => ?, description => ? )}";
    MEMEToolkit.trace(query);

    int work_id = 0;

    try {
      CallableStatement cstmt = prepareCall(query);
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.setString(2, authority.toString());
      cstmt.setString(3, type);
      cstmt.setString(4, description);
      cstmt.execute();
      work_id = cstmt.getInt(1);
      cstmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to create worklog.", query, se);
      throw dse;
    }

    return getWorkLog(work_id);
  }

  /**
   * Implements {@link MEMEDataSource#getWorkLog(int)}.
   * @param work_id the work id
   * @return the {@link WorkLog}
   * @throws DataSourceException if failed to load meme work
   */
  public WorkLog getWorkLog(int work_id) throws DataSourceException {

    final String query = "SELECT * FROM meme_work " +
        "WHERE work_id = ?";

    WorkLog wl = new WorkLog();
    try {
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setInt(1, work_id);
      ResultSet rs = pstmt.executeQuery();

      // Read
      int ctr = 0;
      while (rs.next()) {
        wl.setIdentifier(new Identifier.Default(work_id));
        wl.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        wl.setType(rs.getString("TYPE"));
        wl.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        wl.setDescription(rs.getString("DESCRIPTION"));
        ctr++;
      }

      // Close statement
      pstmt.close();

      if (ctr == 0) {
        // No match found
        MissingDataException mde = new MissingDataException("No work log found.");
        mde.setDetail("work_id", Integer.toString(work_id));
        throw mde;
      }

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up meme_work.", query, se);
      dse.setDetail("query", query);
      dse.setDetail("work_id", Integer.toString(work_id));
      throw dse;
    }

    // work found
    return wl;

  }

  /**
   * Implements {@link MEMEDataSource#getWorkLogs()}.
   * @return An array of object {@link WorkLog}
   * @throws DataSourceException if failed to load meme work.
   */
  public WorkLog[] getWorkLogs() throws DataSourceException {

    List logs = new ArrayList();

    final String query = "SELECT * FROM meme_work";
    try {
      PreparedStatement pstmt = prepareStatement(query);
      ResultSet rs = pstmt.executeQuery();
      // Read
      while (rs.next()) {
        WorkLog work_log = new WorkLog();
        work_log.setIdentifier(new Identifier.Default(rs.getInt("WORK_ID")));
        work_log.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        work_log.setType(rs.getString("TYPE"));
        work_log.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        work_log.setDescription(rs.getString("DESCRIPTION"));
        logs.add(work_log);
      }
      // Close statement
      pstmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up meme_work.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }

    return (WorkLog[]) logs.toArray(new WorkLog[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getWorkLogsByType(String)}.
   * @param type yhe type
   * @return An array of object {@link WorkLog}
   * @throws DataSourceException if failed to load meme work
   */
  public WorkLog[] getWorkLogsByType(String type) throws DataSourceException {

    List logs = new ArrayList();

    final String query = "SELECT * FROM meme_work WHERE type = ?";
    try {
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setString(1, type);
      ResultSet rs = pstmt.executeQuery();
      // Read
      while (rs.next()) {
        WorkLog work_log = new WorkLog();
        work_log.setIdentifier(new Identifier.Default(rs.getInt("WORK_ID")));
        work_log.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        work_log.setType(rs.getString("TYPE"));
        work_log.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        work_log.setDescription(rs.getString("DESCRIPTION"));
        logs.add(work_log);
      }
      // Close statement
      pstmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up meme_work.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }

    return (WorkLog[]) logs.toArray(new WorkLog[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getActivityLog(MolecularTransaction)}.
   * @param transaction the {@link MolecularTransaction}
   * @return the {@link Activity} for the specified transaction
   * @throws DataSourceException if failed to load activity log
   */
  public Activity getActivityLog(MolecularTransaction transaction) throws
      DataSourceException {

    final String query = "SELECT * FROM activity_log WHERE transaction_id = ?";

    Activity activity = new Activity();
    try {
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setInt(1, transaction.getIdentifier().intValue());
      ResultSet rs = pstmt.executeQuery();

      // Read
      int ctr = 0;
      while (rs.next()) {
        activity.setIdentifier(transaction.getIdentifier());
        activity.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        activity.setElapsedTime(rs.getLong("ELAPSED_TIME"));
        activity.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        activity.setShortDescription(rs.getString("ACTIVITY"));
        activity.setDescription(rs.getString("DETAIL"));
        if (transaction.getIdentifier() != null &&
            transaction.getIdentifier().intValue() != 0) {
          activity.setErrors(getErrors(transaction));
        }
        ctr++;
      }

      // Close statement
      pstmt.close();

      if (ctr == 0) {
        // No match found
        DataSourceException dse = new DataSourceException(
            "No activity log found.");
        dse.setDetail("transaction_id", transaction.getIdentifier());
        throw dse;
      }

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up activity_log.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }

    return activity;

  }

  /**
   * Implements {@link MEMEDataSource#getActivityLogs(WorkLog)}.
   * @param work the {@link WorkLog}
   * @return {@link gov.nih.nlm.meme.action.Activity} entries
   * @throws DataSourceException if failed to load activity work
   */
  public Activity[] getActivityLogs(WorkLog work) throws DataSourceException {

    List activities = new ArrayList();

    final String query = "SELECT * FROM activity_log WHERE work_id = ?";
    try {
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setInt(1, work.getIdentifier().intValue());
      ResultSet rs = pstmt.executeQuery();
      // Read
      while (rs.next()) {
        int transaction_id = rs.getInt("TRANSACTION_ID");
        Activity activity = new Activity();
        activity.setIdentifier(transaction_id);
        activity.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        activity.setElapsedTime(rs.getLong("ELAPSED_TIME"));
        activity.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        activity.setShortDescription(rs.getString("ACTIVITY"));
        activity.setDescription(rs.getString("DETAIL"));
        if (transaction_id != 0) {
          activity.setErrors(getErrors(new MolecularTransaction(transaction_id)));
        }
        activities.add(activity);
      }
      // Close statement
      pstmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up activity_log.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }

    return (Activity[]) activities.toArray(new Activity[0]);
  }

  /**
   * Implements {@link MEMEDataSource#getErrors(MolecularTransaction)}
   * @param transaction the {@link MolecularTransaction}
   * @return An array of object {@link LoggedError}.
   * @throws DataSourceException if failed to load logged error.
   */
  public LoggedError[] getErrors(MolecularTransaction transaction) throws
      DataSourceException {

    final String query = "SELECT * FROM meme_error WHERE transaction_id = ?";

    List errors = new ArrayList();
    try {
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setInt(1, transaction.getIdentifier().intValue());
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        LoggedError error = new LoggedError.Default();
        error.setActivity(rs.getString("ACTIVITY"));
        error.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        error.setDetail(rs.getString("DETAIL"));
        error.setElapsedTime(rs.getLong("ELAPSED_TIME"));
        error.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
        error.setTransaction(new MolecularTransaction(rs.getInt(
            "TRANSACTION_ID")));
        error.setWorkLog(new WorkLog(rs.getInt("WORK_ID")));
        errors.add(error);
      }

      // Close statement
      pstmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up meme_error.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }

    return (LoggedError[]) errors.toArray(new LoggedError[0]);

  }

  /**
   * Implements {@link MEMEDataSource#logOperation(Authority, String, String, MolecularTransaction, WorkLog, int)}
   * @param authority the {@link Authority}
   * @param activity the activity
   * @param detail the detail
   * @param transaction the {@link MolecularTransaction}
   * @param work the {@link WorkLog}
   * @param elapsed_time the elapsed time
   * @throws DataSourceException if failed to log operation
   */
  public void logOperation(Authority authority, String activity,
                           String detail, MolecularTransaction transaction,
                           WorkLog work, int elapsed_time) throws
      DataSourceException {

    MEMEToolkit.trace("MEMEConnection.logOperation()...");

    String call_str = "{? = call MEME_UTILITY.log_operation(" +
        "authority => ?, activity => ?, detail => ?, transaction_id => ?, " +
        "work_id => ?, elapsed_time => ?)}";

    try {
      CallableStatement cstmt = prepareCall(call_str);
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.setString(2, authority.toString());
      cstmt.setString(3, activity);
      cstmt.setString(4, detail);
      cstmt.setInt(5, transaction.getIdentifier().intValue());
      cstmt.setInt(6, work.getIdentifier().intValue());
      cstmt.setInt(7, elapsed_time);
      cstmt.execute();
      cstmt.getInt(1);
      cstmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to log the operation.", call_str, se);
      throw dse;
    }

  }

  /**
   * Implements {@link MEMEDataSource#logProgress(Authority, String, String, MolecularTransaction, WorkLog, int)}
   * @param authority the {@link Authority}
   * @param activity the activity
   * @param detail the detail
   * @param transaction the {@link MolecularTransaction}
   * @param work the {@link WorkLog}
   * @param elapsed_time the elapsed time
   * @throws DataSourceException if failed to log progress
   */
  public void logProgress(Authority authority, String activity,
                          String detail, MolecularTransaction transaction,
                          WorkLog work, int elapsed_time) throws
      DataSourceException {

    MEMEToolkit.trace("MEMEConnection.logProgress()...");

    String call_str = "{call MEME_UTILITY.log_progress(" +
        "authority => ?, activity => ?, detail => ?, transaction_id => ?, " +
        "work_id => ?, elapsed_time => ?)}";

    try {
      CallableStatement cstmt = prepareCall(call_str);
      cstmt.setString(1, authority.toString());
      cstmt.setString(2, activity);
      cstmt.setString(3, detail);
      cstmt.setInt(4, transaction.getIdentifier().intValue());
      cstmt.setInt(5, work.getIdentifier().intValue());
      cstmt.setInt(6, elapsed_time);
      cstmt.execute();
      cstmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to log the progress.", call_str, se);
      throw dse;
    }

  }

  /**
   * Implements {@link MEMEDataSource#resetProgress(WorkLog)}
   * @param work the {@link WorkLog}
   * @throws DataSourceException if failed to reset progress
   */
  public void resetProgress(WorkLog work) throws DataSourceException {

    MEMEToolkit.trace("MEMEConnection.resetProgress()...");

    String call_str = "{call MEME_UTILITY.reset_progress(" +
        "work_id => ?)}";

    try {
      CallableStatement cstmt = prepareCall(call_str);
      cstmt.setInt(1, work.getIdentifier().intValue());
      cstmt.execute();
      cstmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to reset the progress.", call_str, se);
      throw dse;
    }
  }

  /**
   * Implements {@link MEMEDataSource#initializeMatrix(WorkLog)}
   * @param work the {@link WorkLog}
   * @return the most recent initialize matrix log
   * @throws DataSourceException if failed to initialize matrix
   */
  public String initializeMatrix(WorkLog work) throws DataSourceException {

    MEMEToolkit.trace("MEMEConnection.initializeMatrix()...");

    String call_str =
        "{? = call MEME_INTEGRITY.matrix_initializer(work_id => ?)}";

    try {
      CallableStatement cstmt = prepareCall(call_str);
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.setInt(2, work.getIdentifier().intValue());
      cstmt.execute();
      cstmt.getInt(1);
      cstmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to initialize matrix.", call_str, se);
      throw dse;
    }

    return flushBuffer();

  }

  /**
   * Validates and return the date
   * @param timestamp the timestamp
   * @return the date
   */
  protected Date getDate(Date timestamp) {
    Date date = null;
    if (timestamp != null) {
      date = new Date(timestamp.getTime());
    }
    return date;
  }

  /**
   * Change the password of user
   * @param user String
   * @param password String
   * @throws DataSourceException
   */
  public void changePassword(String user, String password) throws
      DataSourceException {
    PreparedStatement stmt = null;
    try {
      //stmt = createStatement();
      //stmt.execute("ALTER USER " + user + " IDENTIFIED BY " + password);
    	//MEMEToolkit.logComment("MEMEConnection.Changing password(): user [" + user + "] password [" + password + "]");
      stmt = prepareCall("{call passwd('" + user+ "','" + password + "')}");
      stmt.execute();
      stmt.close();
    }
    catch (SQLException se) {
      throw new DataSourceException(
          "Fail to change the password. Please try again.", null, se);
    }
  }

  /**
   * Get the expiration date of user's password
   * @return Date
   * @throws DataSourceException
   */
  public Date getPasswordExpirationDate() throws DataSourceException {
    Statement stmt = null;
    try {
      stmt = createStatement();
      ResultSet rs = stmt.executeQuery("SELECT expiry_date FROM user_users");
      if (rs.next()) {
        return getDate(rs.getTimestamp("expiry_date"));
      }
      stmt.close();
    }
    catch (SQLException se) {
      throw new DataSourceException(
          "Fail to get the password's expiration date.", null, se);
    }
    return null;
  }

}
