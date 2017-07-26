/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MIDConnection
 *
 * Changes
 *
 * 04/10/2006 TTN (1-AV6X1) : do not need to set keys map when using MultiMap
 * 02/07/2006 RBE (1-763IU): Changes to addApplicationVector and
 * 													 addOverrideVector() to allow new entry.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.AtomChecklist;
import gov.nih.nlm.meme.common.AtomCluster;
import gov.nih.nlm.meme.common.AtomWorklist;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Checklist;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptCluster;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MergeFact;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.integrity.BinaryCheckData;
import gov.nih.nlm.meme.integrity.BinaryDataIntegrityCheck;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.meme.integrity.UnaryCheckData;
import gov.nih.nlm.meme.integrity.UnaryDataIntegrityCheck;
import gov.nih.nlm.meme.integrity.ViolationsVector;
import gov.nih.nlm.util.FieldedStringTokenizer;
import gov.nih.nlm.util.MultiMap;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represents a connection to a MID database.
 *
 * @author MEME Group
 */
public class MIDConnection extends MEMEConnection implements MIDDataSource {

  //
  // Fields
  //

  private static MultiMap editor_pref_i_cache = null;
  private static MultiMap editor_pref_u_cache = null;
  private static MultiMap valid_char_values_cache = null;
  private static MultiMap integrity_cache = null;
  private static MultiMap av_cache = null;
  private static MultiMap ov_cache = null;

  // Initialize HashMap
  static {
    valid_char_values_cache = new MultiMap();
    integrity_cache = new MultiMap();
    av_cache = new MultiMap();
    ov_cache = new MultiMap();
    editor_pref_i_cache = new MultiMap();
    editor_pref_u_cache = new MultiMap();
  }

  //
  // Constructors
  //

  /**
   * This constructor calls its superclass
   * ({@link gov.nih.nlm.meme.sql.EnhancedConnection}) constructor.
   * @param conn the {@link Connection} representation of connection.
   * @throws DataSourceException if worklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   * @throws ReflectionException if failed to load or instantiate class.
   */
  public MIDConnection(Connection conn) throws DataSourceException,
      BadValueException, ReflectionException {
    super(conn);
  }

  //
  // Private Methods
  //

  /**
   * Puts the valid char values into cache.
   * @throws DataSourceException if failed to cache valid char values.
   */
  private void cacheValidCharValues() throws DataSourceException {
    MEMEToolkit.logComment("  Cache valid char values", true);

    // Map fields
    String[] field_map = new String[8];
    {
      field_map[0] = "tobereleased";
      field_map[1] = "released";
      field_map[2] = "status-C";
      field_map[3] = "status-R";
      field_map[4] = "status-A";
      field_map[5] = "status-CS";
      field_map[6] = "level-R";
      field_map[7] = "level-A";
    }

    // Initialize parameters
    String table = "";
    String field = "";
    String value = "";

    for (int i = 0; i < field_map.length; i++) {
      // Map parameters
      field = field_map[i];
      table = field_map[i] + "_rank";
      if (field_map[i].startsWith("status") || field_map[i].startsWith("level")) {
        table = "level_status_rank";

        if (field_map[i].startsWith("status")) {
          field = "status";
        } else if (field_map[i].startsWith("level")) {
          field = "level_value";

        }
        if (field_map[i].equals("status-C")) {
          value = "C";
        } else if (field_map[i].endsWith("-R")) {
          value = "R";
        } else if (field_map[i].endsWith("-A")) {
          value = "A";
        } else if (field_map[i].equals("status-CS")) {
          value = "CS";
        }
      }
      // Perform cache
      cacheValidCharValues(table, field, field_map[i], value);
    }
  }

  /**
   * Puts the valid char values into cache.
   * @param table A table name in query.
   * @param field A field name in query.
   * @param field_map A key in the hash map.
   * @param value A value assigned in query's where clause.
   * @throws DataSourceException if failed to cache valid char values.
   */
  private void cacheValidCharValues(String table, String field,
                                    String field_map, String value) throws
      DataSourceException {

    // Map query string
    String query = "SELECT DISTINCT " + field + " FROM " + table;
    if (value != "" || !value.equals("")) {
      query = query + " WHERE table_name = '" + value + "'";
    }

    ArrayList values = new ArrayList();
    try {
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);

      // Read
      int index = 0;
      while (rs.next()) {
        values.add(index, rs.getString(field));
        index++;
      }
      char[] chars = new char[values.size()];

      for (int i = 0; i < values.size(); i++) {
        chars[i] = values.get(i).toString().charAt(0);
      }
      valid_char_values_cache.put(getDataSourceName(), field_map, chars);

      // Close statement
      stmt.close();

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up " + table + ".", query, se);
      throw dse;
    }
  }

  /**
   * Puts all integrity checks into cache.
   * @throws DataSourceException if failed to cache integrity checks.
   */
  private void cacheIntegrities() throws DataSourceException {

    MEMEToolkit.logComment("  Cache integrities", true);

    // Cache integrity check
    IntegrityCheck[] ics = getIntegrityChecks();
    for (int i = 0; i < ics.length; i++) {
      integrity_cache.put(getDataSourceName(), ics[i].getName(), ics[i]);
    }

    // Cache application vector
    String[] aps = getApplicationsWithVectors();
    for (int i = 0; i < aps.length; i++) {
      EnforcableIntegrityVector eiv = getApplicationVector(aps[i]);
      av_cache.put(getDataSourceName(), aps[i], eiv);
    }

    // Cache override vector
    int[] ovs = getLevelsWithOverrideVectors();
    for (int i = 0; i < ovs.length; i++) {
      IntegrityVector iv = getOverrideVector(ovs[i]);
      ov_cache.put(getDataSourceName(), new Integer(ovs[i]), iv);
    }

  }

  /**
   * Puts the editor preferences into cache.
   * @throws DataSourceException if failed to cache editor preferences.
   */
  private void cacheEditorPreferences() throws DataSourceException {
    MEMEToolkit.logComment("  Cache editor preferences", true);

    final String query = "SELECT a.name, a.initials, editor_level, grp, cur, " +
        "NVL(show_concept,0) AS show_concept, NVL(show_classes,0) AS show_classes, " +
        "NVL(show_relationships,0) AS show_relationships, " +
        "NVL(show_attributes,0) AS show_attributes " +
        "FROM editors a, editor_preferences b WHERE a.name = b.name (+) ";

    try {
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);

      // Read
      while (rs.next()) {
        EditorPreferences ef = new EditorPreferences.Default();
        ef.setUserName(rs.getString("NAME"));
        ef.setInitials(rs.getString("INITIALS"));
        ef.setAuthority(new Authority.Default(rs.getString("INITIALS")));
        ef.setEditorLevel(rs.getInt("EDITOR_LEVEL"));
        ef.setEditorGroup(rs.getString("GRP"));
        ef.setIsCurrent(rs.getString("CUR").toLowerCase().equals("y"));
        ef.setShowConcept(rs.getInt("SHOW_CONCEPT") == 1);
        ef.setShowAtoms(rs.getInt("SHOW_CLASSES") == 1);
        ef.setShowRelationships(rs.getInt("SHOW_RELATIONSHIPS") == 1);
        ef.setShowAttributes(rs.getInt("SHOW_ATTRIBUTES") == 1);

        // add to cache
        editor_pref_i_cache.put(getDataSourceName(), ef.getInitials(), ef);
        editor_pref_u_cache.put(getDataSourceName(), ef.getUserName(), ef);
      }

      // Close statement
      stmt.close();

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up editors preferences.", query, se);
      throw dse;
    }
  }

  //
  // Public Methods
  //

  /**
   * Implements {@link MEMEDataSource#refreshCaches()}.
   * @throws DataSourceException if failed to refresh caches.
   * @throws BadValueException if failed due to invalid data value.
   * @throws ReflectionException if failed to load or instantiate class.
   */
  public void refreshCaches() throws DataSourceException, BadValueException,
      ReflectionException {

    super.refreshCaches();

    cacheEditorPreferences();
    cacheIntegrities();
    cacheValidCharValues();

  }

  /**
   * Populates worklist containing the given specified work.
   * @param work the {@link Worklist}.
   * @throws DataSourceException if worklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateWorklist(Worklist work) throws DataSourceException,
      BadValueException {

    MEMEToolkit.trace("MIDConnection::populateWorklist");

    String query =
        "SELECT * FROM meow.wms_worklist_info WHERE worklist_name = ?";

    try {

      //
      // Get the worklist information
      //
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setString(1, work.getName());
      ResultSet rs = pstmt.executeQuery();
      int ct = 0;
      while (rs.next()) {
        ct++;
        work.setName(rs.getString("WORKLIST_NAME"));
        work.setDescription(rs.getString("WORKLIST_DESCRIPTION"));
        String initials = rs.getString("EDITOR");
        work.setEditorPreferences(initials == null ? null :
                                  getEditorPreferencesByInitials(initials));
        work.setCreatedBy(rs.getString("CREATED_BY"));
        work.setCreatedBy(rs.getString("STAMPED_BY"));
        work.setCreationDate(getDate(rs.getTimestamp("CREATE_DATE")));
        work.setAssignDate(getDate(rs.getTimestamp("ASSIGN_DATE")));
        work.setReturnDate(getDate(rs.getTimestamp("RETURN_DATE")));
        work.setStampingDate(getDate(rs.getTimestamp("STAMP_DATE")));
      }

      if (ct < 1) {
        MissingDataException dse = new MissingDataException(
            "Missing reference to worklist name in wms_worklist_info.");
        dse.setDetail("schema", "meow");
        dse.setDetail("worklist_name", work.getName());
        throw dse;
      } else if (ct > 1) {
        BadValueException bve = new BadValueException(
            "Too many references to worklist name in wms_worklist_info.");
        bve.setDetail("schema", "meow");
        bve.setDetail("worklist_name", work.getName());
        throw bve;
      }

      pstmt.close();

    } catch (DataSourceException dse) {
      throw dse;
    } catch (BadValueException bve) {
      throw bve;
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to read worklist info", query, se);
      dse.setDetail("schema", "meow");
      dse.setDetail("worklist_name", work.getName());
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Populates checklist containing the given specified work.
   * @param check the {@link Checklist}.
   * @throws DataSourceException if checklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateChecklist(Checklist check) throws DataSourceException,
      BadValueException {

    MEMEToolkit.trace("MIDConnection::populateChecklist");

    String query =
        "SELECT * FROM meow.ems_checklist_info WHERE checklist_name = ?";
    try {

      //
      // Get the checklist information
      //
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setString(1, check.getName());
      ResultSet rs = pstmt.executeQuery();
      int ct = 0;
      while (rs.next()) {
        ct++;
        check.setName(rs.getString("CHECKLIST_NAME"));
        check.setOwner(rs.getString("OWNER"));
        check.setBinName(rs.getString("BIN_NAME"));
        check.setBinType(rs.getString("BIN_TYPE"));
        check.setCreationDate(getDate(rs.getTimestamp("CREATE_DATE")));
      }

      if (ct < 1) {
        MissingDataException dse = new MissingDataException(
            "Missing reference to checklist name in ems_checklist_info.");
        dse.setDetail("schema", "meow");
        dse.setDetail("checklist_name", check.getName());
        throw dse;
      } else if (ct > 1) {
        BadValueException bve = new BadValueException(
            "Too many references to checklist name in ems_checklist_info.");
        bve.setDetail("schema", "meow");
        bve.setDetail("checklist_name", check.getName());
        throw bve;
      }

      pstmt.close();

    } catch (DataSourceException dse) {
      throw dse;
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to read checklist info", query, se);
      dse.setDetail("schema", "meow");
      dse.setDetail("checklist_name", check.getName());
      dse.setDetail("query", query.toString());
      throw dse;
    }

  }

  /**
   * Returns a worklist containing given the specified name.
   * @param worklist_name the {@link String} representation
   *   of worklist name.
   * @return a worklist containing given the specified name.
   * @throws DataSourceException if worklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public AtomWorklist getAtomWorklist(String worklist_name) throws
      DataSourceException, BadValueException {

    MEMEToolkit.trace("MIDConnection::getAtomWorklist");

    AtomWorklist worklist = new AtomWorklist();
    worklist.setName(worklist_name);
    populateWorklist(worklist);

    String query2 =
        "SELECT DISTINCT a.atom_id, b.row_id, b.cluster_id " +
        " FROM classes a, meow." + worklist_name + " b, classes c" +
        " WHERE a.concept_id = c.concept_id and b.atom_id = c.atom_id" +
        " ORDER BY cluster_id, row_id";

    try {

      //
      // Add atoms to worklist
      //
      Ticket ticket = Ticket.getEmptyTicket();
      ticket.setReadAtoms(true);
      ticket.setReadAtomNames(true);

      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query2);

      int previous = -1;
      AtomCluster cluster = null;
      while (rs.next()) {
        // Read atom
        Atom atom = new Atom.Default(rs.getInt("ATOM_ID"));
        populateAtom(atom, ticket);

        // Get cluster_id
        int current = rs.getInt("CLUSTER_ID");
        // If it changed, deal with previous cluster
        if (current != previous) {
          // For first row don't add cluster
          if (cluster != null) {
            worklist.addCluster(cluster);
          }
          cluster = new AtomCluster(new Identifier.Default(current));
        }
        cluster.add(atom);
        previous = current;
      }
      if (cluster != null) {
        worklist.addCluster(cluster);
      }
      stmt.close();

    } catch (DataSourceException dse) {
      throw dse;
    } catch (BadValueException bve) {
      throw bve;
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to add atoms to worklist", query2, se);
      dse.setDetail("schema", "meow");
      dse.setDetail("worklist_name", worklist_name);
      dse.setDetail("query", query2.toString());
      throw dse;
    }

    return worklist;
  }

  /**
   * Returns a checklist containing given the specified name.
   * @param checklist_name the {@link String}
   *   representation of checklist name.
   * @return a checklist containing given the specified name.
   * @throws DataSourceException if checklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public AtomChecklist getAtomChecklist(String checklist_name) throws
      DataSourceException, BadValueException {

    MEMEToolkit.trace("MIDConnection::getAtomChecklist");

    AtomChecklist checklist = new AtomChecklist();
    checklist.setName(checklist_name);
    populateChecklist(checklist);

    String query2 =
        "SELECT DISTINCT a.atom_id, b.row_id, b.cluster_id " +
        " FROM classes a, meow." + checklist_name + " b, classes c" +
        " WHERE a.concept_id = c.concept_id and b.atom_id = c.atom_id" +
        " ORDER BY cluster_id, row_id";

    try {

      //
      // Add atoms to checklist
      //
      Ticket ticket = Ticket.getEmptyTicket();
      ticket.setReadAtoms(true);
      ticket.setReadAtomNames(true);

      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query2);

      int previous = -1;
      AtomCluster cluster = null;
      while (rs.next()) {
        // Read atom
        Atom atom = new Atom.Default(rs.getInt("ATOM_ID"));
        populateAtom(atom, ticket);

        // Get cluster_id
        int current = rs.getInt("CLUSTER_ID");
        // If it changed, deal with previous cluster
        if (current != previous) {
          // For first row don't add cluster
          if (cluster != null) {
            checklist.addCluster(cluster);
          }
          cluster = new AtomCluster(new Identifier.Default(current));
        }
        cluster.add(atom);
        previous = current;
      }
      if (cluster != null) {
        checklist.addCluster(cluster);
      }
      stmt.close();
    } catch (DataSourceException dse) {
      throw dse;
    } catch (BadValueException bve) {
      throw bve;
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to add atoms to checklist", query2, se);
      dse.setDetail("schema", "meow");
      dse.setDetail("checklist_name", checklist_name);
      dse.setDetail("query", query2.toString());
      throw dse;
    }

    return checklist;
  }

  /**
   * Returns a worklist containing given the specified name.
   * @param worklist_name the {@link String} representation
   *   of worklist name.
   * @return a worklist containing given the specified name.
   * @throws DataSourceException if worklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ConceptWorklist getConceptWorklist(String worklist_name) throws
      DataSourceException, BadValueException {

    MEMEToolkit.trace("MIDConnection::getConceptWorklist");

    ConceptWorklist worklist = new ConceptWorklist();
    worklist.setName(worklist_name);
    populateWorklist(worklist);

    String query2 =
        "SELECT rownum as row_id, cluster_id, concept_id FROM " +
        "(SELECT DISTINCT cluster_id, b.concept_id " +
        " FROM meow." + worklist_name + " a, classes b " +
        " WHERE a.atom_id = b.atom_id) " +
        "ORDER BY cluster_id";

    try {

      //
      // Add Concept to worklist
      //
      Ticket ticket = Ticket.getEmptyTicket();
      //ticket.setReadAtoms(true);
      //ticket.setReadAtomNames(true);
      ticket.setReadConcept(true);
      //ticket.setCalculatePreferredAtom(true);

      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query2);

      int previous = -1;
      ConceptCluster cluster = null;
      while (rs.next()) {
        // Read the concept
        Concept concept = new Concept.Default(rs.getInt("CONCEPT_ID"));
        populateConcept(concept, ticket);
        concept.setPreferredAtom(getPreferredAtom(concept));

        // Get current cluster id
        int current = rs.getInt("CLUSTER_ID");

        // If doesn't match prev cluster id, then we
        // need to add the old cluster to the worklist
        if (current != previous) {
          // First time through, don't add the empty cluster
          if (cluster != null) {
            worklist.addCluster(cluster);
            // Start a new cluster with the current id.
          }
          cluster = new ConceptCluster(new Identifier.Default(current));
        }
        // Add the current concept to the current cluster
        cluster.add(concept);

        // Set previous identifier
        previous = current;
      }
      if (cluster != null) {
        worklist.addCluster(cluster);
      }
      stmt.close();

    } catch (DataSourceException dse) {
      throw dse;
    } catch (BadValueException bve) {
      throw bve;
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to add concepts to worklist", query2, se);
      dse.setDetail("schema", "meow");
      dse.setDetail("worklist_name", worklist_name);
      dse.setDetail("query", query2.toString());
      throw dse;
    }

    return worklist;
  }

  /**
   * Returns a checklist containing given the specified name.
   * @param checklist_name the {@link String} representation
   *   of checklist name.
   * @return a checklist containing given the specified name.
   * @throws DataSourceException if checklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ConceptChecklist getConceptChecklist(String checklist_name) throws
      DataSourceException, BadValueException {

    MEMEToolkit.trace("MIDConnection::getConceptChecklist");

    ConceptChecklist checklist = new ConceptChecklist();
    checklist.setName(checklist_name);
    populateChecklist(checklist);

    String query2 =
        "SELECT rownum as row_id, cluster_id, concept_id FROM " +
        "(SELECT DISTINCT cluster_id, b.concept_id " +
        " FROM meow." + checklist_name + " a, classes b " +
        " WHERE a.atom_id = b.atom_id) " +
        "ORDER BY cluster_id";

    try {

      //
      // Add concept to checklist
      //
      Ticket ticket = Ticket.getEmptyTicket();
      //ticket.setReadAtoms(true);
      //ticket.setReadAtomNames(true);
      ticket.setReadConcept(true);
      //ticket.setCalculatePreferredAtom(true);

      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query2);

      int previous = -1;
      ConceptCluster cluster = null;
      while (rs.next()) {
        // Read the concept
        Concept concept = new Concept.Default(rs.getInt("CONCEPT_ID"));
        populateConcept(concept, ticket);
        concept.setPreferredAtom(getPreferredAtom(concept));

        // Get current cluster id
        int current = rs.getInt("CLUSTER_ID");

        // If doesn't match prev cluster id, then we
        // need to add the old cluster to the checklist
        if (current != previous) {
          // First time through, don't add the empty cluster
          if (cluster != null) {
            checklist.addCluster(cluster);
            // Start a new cluster with the current id.
          }
          cluster = new ConceptCluster(new Identifier.Default(current));
        }
        // Add the current concept to the current cluster
        cluster.add(concept);

        // Set previous identifier
        previous = current;
      }
      if (cluster != null) {
        checklist.addCluster(cluster);
      }
      stmt.close();

    } catch (DataSourceException dse) {
      throw dse;
    } catch (BadValueException bve) {
      throw bve;
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to add concepts to checklist", query2, se);
      dse.setDetail("schema", "meow");
      dse.setDetail("checklist_name", checklist_name);
      dse.setDetail("query", query2.toString());
      throw dse;
    }

    return checklist;
  }

  /**
   * Adds an atom worklist.
   * @param worklist the {@link AtomWorklist}.
   * @throws DataSourceException if failed to create atom worklist.
   */
  public void addAtomWorklist(AtomWorklist worklist) throws DataSourceException {

    MEMEToolkit.trace("MIDConnection::addAtomWorklist");

    String create_str =
        "CREATE TABLE meow." + worklist.getName() +
        " (orig_concept_id number(12) not null," +
        " atom_id number(12) not null," +
        " row_id number(12), " +
        " cluster_id number(12))";

    try {
      PreparedStatement pstmt = prepareStatement(create_str);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to create atom worklist table.", create_str, se);
      dse.setDetail("create_str", create_str);
      throw dse;
    }

    String insert_str =
        "INSERT INTO meow." + worklist.getName() +
        " (orig_concept_id, atom_id, row_id, cluster_id)" +
        " VALUES (?,?,?,?)";

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      Iterator iterator = worklist.iterator();
      int ct = 0;
      while (iterator.hasNext()) {
        Atom atom = (Atom) iterator.next();
        pstmt.setInt(1,
                     atom.getConcept().getIdentifier() == null ? 0 :
                     atom.getConcept().getIdentifier().intValue());
        pstmt.setInt(2, atom.getIdentifier().intValue());
        pstmt.setInt(3, ++ct);
        pstmt.setInt(4,
                     atom.getClusterIdentifier() == null ? ct :
                     atom.getClusterIdentifier().intValue());
        pstmt.executeUpdate();
      }
      pstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert into atom worklist.", insert_str, se);
      dse.setDetail("insert_str", insert_str);
      throw dse;
    }

    insert_str =
        "INSERT INTO meow.wms_worklist_info " +
        "(worklist_name, worklist_description, worklist_status, n_concepts," +
        " n_clusters, grp, editor, create_date, created_by, assign_date," +
        " return_date, edit_time, stamp_date, stamped_by, stamp_time," +
        " exclude_flag, n_actions, n_approved, n_approved_by_editor," +
        " n_stamped, n_not_stamped, n_rels_inserted, n_stys_inserted," +
        " n_splits, n_merges) " +
        "SELECT ?, ?, ?, COUNT(DISTINCT orig_concept_id), COUNT(DISTINCT cluster_id)," +
        " ?, ?, sysdate, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
        " FROM meow." + worklist.getName();

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      pstmt.setString(1, worklist.getName());
      pstmt.setString(2, worklist.getDescription());
      pstmt.setString(3, worklist.getStatus());
      pstmt.setString(4,
                      worklist.getEditorPreferences() == null ? null :
                      worklist.getEditorPreferences().getEditorGroup());
      pstmt.setString(5,
                      worklist.getEditorPreferences() == null ? null :
                      worklist.getEditorPreferences().getInitials());
      pstmt.setString(6, worklist.getCreatedBy());
      pstmt.setString(7,
                      worklist.getAssignDate() == null ? null :
                      MEMEToolkit.getDateFormat().format(worklist.getAssignDate()));
      pstmt.setString(8,
                      worklist.getReturnDate() == null ? null :
                      MEMEToolkit.getDateFormat().format(worklist.getReturnDate()));
      pstmt.setInt(9, -1);
      pstmt.setString(10,
                      worklist.getStampingDate() == null ? null :
                      MEMEToolkit.
                      getDateFormat().format(worklist.getStampingDate()));
      pstmt.setString(11, worklist.getStampedBy());
      pstmt.setInt(12, -1);
      pstmt.setString(13, "Y");
      pstmt.setInt(14, -1);
      pstmt.setInt(15, -1);
      pstmt.setInt(16, -1);
      pstmt.setInt(17, -1);
      pstmt.setInt(18, -1);
      pstmt.setInt(19, -1);
      pstmt.setInt(20, -1);
      pstmt.setInt(21, -1);
      pstmt.setInt(22, -1);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert into meow.wms_worklist_info.", insert_str, se);
      dse.setDetail("insert_str", insert_str);
      throw dse;
    }

    try {
      commit();
    } catch (Exception e) {}
  }

  /**
   * Adds a concept worklist.
   * @param worklist the {@link ConceptWorklist}.
   * @throws DataSourceException if failed to create concept worklist.
   */
  public void addConceptWorklist(ConceptWorklist worklist) throws
      DataSourceException {

    MEMEToolkit.trace("MIDConnection::addConceptWorklist");

    String create_str =
        "CREATE TABLE meow." + worklist.getName() +
        " (orig_concept_id number(12) not null," +
        " atom_id number(12) not null," +
        " row_id number(12), " +
        " cluster_id number(12))";

    try {
      PreparedStatement pstmt = prepareStatement(create_str);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to create concept worklist table.", create_str, se);
      dse.setDetail("create_str", create_str);
      throw dse;
    }

    String insert_str =
        "INSERT INTO meow." + worklist.getName() +
        " (orig_concept_id, atom_id, row_id, cluster_id) " +
        " SELECT concept_id, atom_id, rownum+?, ?" +
        " FROM classes WHERE concept_id = ?";

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      Iterator iterator = worklist.iterator();
      int ct = 0;
      int rct = 0;
      while (iterator.hasNext()) {
        Concept concept = (Concept) iterator.next();
        pstmt.setInt(1, rct);
        pstmt.setInt(2,
                     concept.getClusterIdentifier() == null ? ++ct :
                     concept.getClusterIdentifier().intValue());
        pstmt.setInt(3, concept.getIdentifier().intValue());
        int rowcount = pstmt.executeUpdate();
        rct += rowcount;
      }
      pstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert into atom worklist.", insert_str, se);
      dse.setDetail("insert_str", insert_str);
      throw dse;
    }

    insert_str =
        "INSERT INTO meow.wms_worklist_info " +
        "(worklist_name, worklist_description, worklist_status, n_concepts," +
        " n_clusters, grp, editor, create_date, created_by, assign_date," +
        " return_date, edit_time, stamp_date, stamped_by, stamp_time," +
        " exclude_flag, n_actions, n_approved, n_approved_by_editor," +
        " n_stamped, n_not_stamped, n_rels_inserted, n_stys_inserted," +
        " n_splits, n_merges) " +
        "SELECT ?, ?, ?, COUNT(DISTINCT orig_concept_id), COUNT(DISTINCT cluster_id)," +
        " ?, ?, sysdate, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
        " FROM meow." + worklist.getName();

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      pstmt.setString(1, worklist.getName());
      pstmt.setString(2, worklist.getDescription());
      pstmt.setString(3, worklist.getStatus());
      pstmt.setString(4,
                      worklist.getEditorPreferences() == null ? null :
                      worklist.getEditorPreferences().getEditorGroup());
      pstmt.setString(5,
                      worklist.getEditorPreferences() == null ? null :
                      worklist.getEditorPreferences().getInitials());
      pstmt.setString(6, worklist.getCreatedBy());
      pstmt.setString(7,
                      worklist.getAssignDate() == null ? null :
                      MEMEToolkit.getDateFormat().format(worklist.getAssignDate()));
      pstmt.setString(8,
                      worklist.getReturnDate() == null ? null :
                      MEMEToolkit.getDateFormat().format(worklist.getReturnDate()));
      pstmt.setInt(9, -1);
      pstmt.setString(10,
                      worklist.getStampingDate() == null ? null :
                      MEMEToolkit.
                      getDateFormat().format(worklist.getStampingDate()));
      pstmt.setString(11, worklist.getStampedBy());
      pstmt.setInt(12, -1);
      pstmt.setString(13, "Y");
      pstmt.setInt(14, -1);
      pstmt.setInt(15, -1);
      pstmt.setInt(16, -1);
      pstmt.setInt(17, -1);
      pstmt.setInt(18, -1);
      pstmt.setInt(19, -1);
      pstmt.setInt(20, -1);
      pstmt.setInt(21, -1);
      pstmt.setInt(22, -1);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert into meow.wms_worklist_info.", insert_str, se);
      dse.setDetail("insert_str", insert_str);
      throw dse;
    }

    try {
      commit();
    } catch (Exception e) {}
  }

  /**
   * Adds an atom checklist.
   * @param checklist the {@link AtomChecklist}.
   * @throws DataSourceException if failed to create atom checklist.
   */
  public void addAtomChecklist(AtomChecklist checklist) throws
      DataSourceException {

    MEMEToolkit.trace("MIDConnection::addAtomChecklist");

    String create_str =
        "CREATE TABLE meow." + checklist.getName() +
        " (orig_concept_id number(12) not null," +
        " atom_id number(12) not null," +
        " row_id number(12), " +
        " cluster_id number(12))";

    try {
      PreparedStatement pstmt = prepareStatement(create_str);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to create atom checklist table.", create_str, se);
      dse.setDetail("create_str", create_str);
      throw dse;
    }

    String insert_str =
        "INSERT INTO meow." + checklist.getName() +
        " (orig_concept_id, atom_id, row_id, cluster_id)" +
        " VALUES (?,?,?,?)";

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      Iterator iterator = checklist.iterator();
      int ct = 0;
      while (iterator.hasNext()) {
        Atom atom = (Atom) iterator.next();
        pstmt.setInt(1,
                     atom.getConcept().getIdentifier() == null ? 0 :
                     atom.getConcept().getIdentifier().intValue());
        pstmt.setInt(2, atom.getIdentifier().intValue());
        pstmt.setInt(3, ++ct);
        pstmt.setInt(4,
                     atom.getClusterIdentifier() == null ? ct :
                     atom.getClusterIdentifier().intValue());
        pstmt.executeUpdate();
      }
      pstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert into atom checklist.", insert_str, se);
      dse.setDetail("insert_str", insert_str);
      throw dse;
    }

    insert_str =
        "INSERT INTO meow.ems_checklist_info " +
        "(checklist_name, owner, bin_name, bin_type," +
        " create_date, concepts, clusters) " +
        "SELECT ?, ?, ?, ?, sysdate, COUNT(DISTINCT orig_concept_id)," +
        " COUNT(DISTINCT cluster_id) FROM meow." + checklist.getName();

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      pstmt.setString(1, checklist.getName());
      pstmt.setString(2, checklist.getOwner());
      pstmt.setString(3, checklist.getBinName());
      pstmt.setString(4, checklist.getBinType());
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert into meow.ems_checklist_info.", insert_str, se);
      dse.setDetail("insert_str", insert_str);
      throw dse;
    }

    try {
      commit();
    } catch (Exception e) {}
  }

  /**
   * Adds a concept checklist.
   * @param checklist the {@link ConceptChecklist}.
   * @throws DataSourceException if failed to create concept checklist.
   */
  public void addConceptChecklist(ConceptChecklist checklist) throws
      DataSourceException {

    MEMEToolkit.trace("MIDConnection::addConceptChecklist");

    String create_str =
        "CREATE TABLE meow." + checklist.getName() +
        " (orig_concept_id number(12) not null," +
        " atom_id number(12) not null," +
        " row_id number(12) not null, " +
        " cluster_id number(12) not null)";

    try {
      PreparedStatement pstmt = prepareStatement(create_str);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to create concept checklist table.", create_str, se);
      dse.setDetail("create_str", create_str);
      throw dse;
    }

    String insert_str =
        "INSERT INTO meow." + checklist.getName() +
        " (orig_concept_id, atom_id, row_id, cluster_id) " +
        " SELECT concept_id, atom_id, rownum+?, ?" +
        " FROM classes WHERE concept_id = ?";

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      Iterator iterator = checklist.iterator();
      int ct = 0;
      int rct = 0;
      while (iterator.hasNext()) {
        Concept concept = (Concept) iterator.next();
        pstmt.setInt(1, rct);
        pstmt.setInt(2,
                     concept.getClusterIdentifier() == null ? ++ct :
                     concept.getClusterIdentifier().intValue());
        pstmt.setInt(3, concept.getIdentifier().intValue());
        int rowcount = pstmt.executeUpdate();
        rct += rowcount;
      }
      pstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert into atom checklist.", insert_str, se);
      dse.setDetail("insert_str", insert_str);
      throw dse;
    }

    insert_str =
        "INSERT INTO meow.ems_checklist_info " +
        "(checklist_name, owner, bin_name, bin_type," +
        " create_date, concepts, clusters) " +
        "SELECT ?, ?, ?, ?, sysdate, COUNT(DISTINCT orig_concept_id)," +
        " COUNT(DISTINCT cluster_id) FROM meow." + checklist.getName();

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      pstmt.setString(1, checklist.getName());
      pstmt.setString(2, checklist.getOwner());
      pstmt.setString(3, checklist.getBinName());
      pstmt.setString(4, checklist.getBinType());
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to insert into meow.ems_checklist_info.", insert_str, se);
      dse.setDetail("insert_str", insert_str);
      throw dse;
    }

    try {
      commit();
    } catch (Exception e) {}
  }

  /**
   * Checks whether worklist exist.
   * @param worklist_name the {@link String} representation of worklist.
   * @return <code>true</code> if worklist exist; <code>false</code> otherwise.
   * @throws DataSourceException if failed to check if worklist exist.
   */
  public boolean worklistExists(String worklist_name) throws
      DataSourceException {

    MEMEToolkit.trace("MIDConnection::worklistExist");

    String query =
        "SELECT COUNT(*) FROM meow.wms_worklist_info WHERE worklist_name = ?";

    try {

      //
      // Get the worklist information
      //
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setString(1, worklist_name);
      ResultSet rs = pstmt.executeQuery();
      int ct = 0;
      while (rs.next()) {
        ct = rs.getInt(1);
      }
      pstmt.close();

      if (ct != 0) {
        return true;
      }

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to read worklist info", query, se);
      dse.setDetail("schema", "meow");
      dse.setDetail("worklist_name", worklist_name);
      dse.setDetail("query", query.toString());
      throw dse;
    }

    return false;

  }

  /**
   * Checks whether checklist exist.
   * @param checklist_name the {@link String} representation of checklist.
       * @return <code>true</code> if checklist exist; <code>false</code> otherwise.
   * @throws DataSourceException if failed to check if checklist exist.
   */
  public boolean checklistExists(String checklist_name) throws
      DataSourceException {

    MEMEToolkit.trace("MIDConnection::checklistExist");

    String query =
        "SELECT COUNT(*) FROM meow.ems_checklist_info WHERE checklist_name = ?";

    try {

      //
      // Get the checklist information
      //
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setString(1, checklist_name);
      ResultSet rs = pstmt.executeQuery();
      int ct = 0;
      while (rs.next()) {
        ct = rs.getInt(1);
      }
      pstmt.close();

      if (ct != 0) {
        return true;
      }

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to read checklist info", query, se);
      dse.setDetail("schema", "meow");
      dse.setDetail("checklist_name", checklist_name);
      dse.setDetail("query", query.toString());
      throw dse;
    }

    return false;
  }

  /**
   * Removes a worklist.
   * @param worklist_name the {@link String} representation of worklist.
   * @throws DataSourceException if worklist could not be removed.
   */
  public void removeWorklist(String worklist_name) throws DataSourceException {

    MEMEToolkit.trace("MIDConnection::removeWorklist");

    String delete_str =
        "DELETE FROM meow.wms_worklist_info" +
        " WHERE worklist_name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(delete_str);
      pstmt.setString(1, worklist_name);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to delete from worklist table.", delete_str, se);
      dse.setDetail("delete_str", delete_str);
      throw dse;
    }

    String drop_str = "DROP TABLE meow." + worklist_name;

    try {
      CallableStatement cstmt = prepareCall(drop_str);
      cstmt.execute();
      cstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to drop table.", drop_str, se);
      throw dse;
    }

    try {
      commit();
    } catch (Exception e) {}
  }

  /**
   * Removes a checklist.
   * @param checklist_name the {@link String} representation of checklist.
   * @throws DataSourceException if checklist could not be removed.
   */
  public void removeChecklist(String checklist_name) throws DataSourceException {

    MEMEToolkit.trace("MIDConnection::removeChecklist");

    String delete_str =
        "DELETE FROM meow.ems_checklist_info" +
        " WHERE checklist_name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(delete_str);
      pstmt.setString(1, checklist_name);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to delete from checklist table.", delete_str, se);
      dse.setDetail("delete_str", delete_str);
      throw dse;
    }

    String drop_str = "DROP TABLE meow." + checklist_name;

    try {
      CallableStatement cstmt = prepareCall(drop_str);
      cstmt.execute();
      cstmt.close();
    } catch (SQLException se) {
      try {
        rollback();
      } catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to drop table.", drop_str, se);
      throw dse;
    }

    try {
      commit();
    } catch (Exception e) {}
  }

  /**
   * Search for worklist.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find worklist.
   */
  public Iterator findWorklists(SearchParameter param) throws
      DataSourceException {
    return findWorklists(new SearchParameter[] {param});
  }

  /**
   * Search for worklist
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find worklists.
   */
  public Iterator findWorklists(SearchParameter[] param) throws
      DataSourceException {

    // Build up a query that searches for
    // molecule_ids, then use getFullMolecularAction to return
    // the objects.
    StringBuffer query = new StringBuffer(100);
    List list = new ArrayList();
    query.append("SELECT worklist_name from MEOW.wms_worklist_info " +
                 "WHERE 1=1 ");

    QueryBuilder builder = new QueryBuilder();
    builder.build(query, param, list);

    // Execute the query
    try {

      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("\n" + list.get(i).toString());
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();

      // Return an iterator that returns Worklist objects
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          Worklist worklist = new Worklist();
          worklist.setName(rs.getString(1));
          populateWorklist(worklist);
          return worklist;
        }
      }
      );
    } catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find worklist.", query.toString(), e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Search for worklist names.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find worklist names.
   */
  public Iterator findWorklistNames(SearchParameter param) throws
      DataSourceException {
    return findWorklistNames(new SearchParameter[] {param});
  }

  /**
   * Search for worklist names.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find worklist names.
   */
  public Iterator findWorklistNames(SearchParameter[] param) throws
      DataSourceException {

    // Build up a query that searches for
    // molecule_ids, then use getFullMolecularAction to return
    // the objects.
    StringBuffer query = new StringBuffer(100);
    List list = new ArrayList();
    query.append("SELECT worklist_name from MEOW.wms_worklist_info " +
                 "WHERE upper(worklist_name) IN (SELECT upper(table_name) " +
                 "FROM all_tables where owner='MEOW')");

    QueryBuilder builder = new QueryBuilder();
    builder.build(query, param, list);

    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("\n" + list.get(i).toString());
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();

      // Return an iterator that returns MolecularAction objects
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException {
          return rs.getString(1);
        }
      }
      );
    } catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find worklist names.", query.toString(), e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Search for checklist.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find checklist.
   */
  public Iterator findChecklists(SearchParameter param) throws
      DataSourceException {
    return findChecklists(new SearchParameter[] {param});
  }

  /**
   * Search for checklist.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find checklists.
   */
  public Iterator findChecklists(SearchParameter[] param) throws
      DataSourceException {

    // Build up a query that searches for
    // molecule_ids, then use getFullMolecularAction to return
    // the objects.
    StringBuffer query = new StringBuffer(100);
    List list = new ArrayList();
    query.append("SELECT checklist_name from MEOW.ems_checklist_info " +
                 "WHERE 1=1 ");

    QueryBuilder builder = new QueryBuilder();
    builder.build(query, param, list);

    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("\n" + list.get(i).toString());
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();

      // Return an iterator that returns Checklist objects
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          Checklist checklist = new Checklist();
          checklist.setName(rs.getString(1));
          populateChecklist(checklist);
          return checklist;
        }
      }
      );
    } catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find checklists.", query.toString(), e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Search for checklist names.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find checklist names.
   */
  public Iterator findChecklistNames(SearchParameter param) throws
      DataSourceException {
    return findChecklistNames(new SearchParameter[] {param});
  }

  /**
   * Search for checklist names.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find checklist names.
   */
  public Iterator findChecklistNames(SearchParameter[] param) throws
      DataSourceException {

    // Build up a query that searches for
    // molecule_ids, then use getFullMolecularAction to return
    // the objects.
    StringBuffer query = new StringBuffer(100);
    List list = new ArrayList();
    query.append("SELECT checklist_name from MEOW.ems_checklist_info " +
                 "WHERE upper(checklist_name) IN " +
        "(SELECT upper(table_name) FROM all_tables where owner='MEOW')");

    QueryBuilder builder = new QueryBuilder();
    builder.build(query, param, list);

    // Execute the query
    try {
      PreparedStatement pstmt = prepareStatement(query.toString());
      for (int i = 0; i < list.size(); i++) {
        MEMEToolkit.trace("\n" + list.get(i).toString());
        pstmt.setString(i + 1, list.get(i).toString());
      }
      final ResultSet rs = pstmt.executeQuery();

      // Return an iterator that returns MolecularAction objects
      return new ResultSetIterator(rs, this,
                                   new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException {
          return rs.getString(1);
        }
      }
      );
    } catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find checklist names.", query.toString(), e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#findMergeFacts(SearchParameter)}.
   * @param param the {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching merge fact.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Iterator findMergeFacts(SearchParameter param) throws
      DataSourceException, BadValueException {
    return findMergeFacts(new SearchParameter[] {param});
  }

  /**
   * Implements {@link MIDDataSource#findMergeFacts(SearchParameter[])}.
   * @param params An array of object {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching merge fact.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Iterator findMergeFacts(SearchParameter[] params) throws
      DataSourceException, BadValueException {

    MEMEToolkit.trace(
        "MIDConnection.findMergeFacts(SearchParameter[]) ...");

    // Build up a query that searches for mom_merge_fact rows
    StringBuffer query = new StringBuffer(500);
    query.append("SELECT merge_fact_id, merge_set ")
        .append("FROM mom_merge_facts WHERE 1=1");

    List list = new ArrayList();

    QueryBuilder builder = new QueryBuilder();
    builder.build(query, params, list);

    query.append(" ORDER BY DECODE(merge_level,'SY',0,'MAT',1,'NRM',2,3) ASC");

    MEMEToolkit.trace("MIDConnection.query=" + query);

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
          return getMergeFact(rs.getString("MERGE_SET"),
                              rs.getInt("MERGE_FACT_ID"));
        }
      }
      );
    } catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to find matching merge facts.", query.toString(), e);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#getDeadAtom(int)}.
   * @param atom_id the atom id.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getDeadAtom(int atom_id) throws DataSourceException,
      BadValueException {

    Ticket ticket = Ticket.getEmptyTicket();
    ticket.setReadDeleted(true);
    ticket.setReadAtoms(true);
    ticket.setReadAtomNames(true);

    Atom atom = new Atom.Default(atom_id);
    populateAtom(atom, ticket);
    return atom;
  }

  /**
   * Implements {@link MIDDataSource#getDeadConcept(int)}.
   * @param concept_id the concept_id.
   * @return the {@link Concept}.
   * @throws DataSourceException if failed to look up dead concept.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Concept getDeadConcept(int concept_id) throws DataSourceException,
      BadValueException {

    Ticket ticket = Ticket.getEmptyTicket();
    ticket.setReadDeleted(true);
    ticket.setReadConcept(true);

    Concept concept = new Concept.Default(concept_id);
    populateConcept(concept, ticket);

    Atom p_atom = null;
    try {
      p_atom = getAtomWithName(concept.getPreferredAtom().getIdentifier().
                               intValue(), null);
    } catch (BadValueException bve) {
      p_atom = getDeadAtom(concept.getPreferredAtom().getIdentifier().intValue());
    }

    concept.setPreferredAtom(p_atom);
    return concept;
  }

  /**
   * Implements {@link MIDDataSource#getDeadAttribute(int)}.
   * @param attr_id the attribute id.
   * @return the {@link Attribute}.
   * @throws DataSourceException if failed to load attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Attribute getDeadAttribute(int attr_id) throws DataSourceException,
      BadValueException {

    Ticket ticket = Ticket.getEmptyTicket();
    ticket.setReadDeleted(true);

    Attribute attr = new Attribute.Default(attr_id);
    populateAttribute(attr, ticket);

    return attr;
  }

  /**
   * Implements {@link MIDDataSource#getDeadRelationship(int)}.
   * @param rel_id the relationship id.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Relationship getDeadRelationship(int rel_id) throws
      DataSourceException, BadValueException {

    Ticket ticket = Ticket.getEmptyTicket();
    ticket.setReadDeleted(true);

    Relationship rel = new Relationship.Default(rel_id);
    populateRelationship(rel, ticket);

    return rel;
  }

  /**
   * Implements {@link MIDDataSource#getDeadContextRelationship(int)}.
   * @param rel_id the relationship id.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load context relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextRelationship getDeadContextRelationship(int rel_id) throws
      DataSourceException, BadValueException {

    Ticket ticket = Ticket.getEmptyTicket();
    ticket.setReadDeleted(true);

    ContextRelationship cxt_rel = new ContextRelationship.Default(rel_id);
    populateContextRelationship(cxt_rel, ticket);

    return cxt_rel;
  }

  /**
   * Implements {@link MIDDataSource#newEnforcableIntegrityVector(String)}.
   * @param iv the {@link String} representation of vector.
   * @return the {@link EnforcableIntegrityVector}.
   * @throws DataSourceException if failed to create new enforcable integrity
   * vector.
   * @throws BadValueException if failed due to invalid data value.
   */
  public EnforcableIntegrityVector newEnforcableIntegrityVector(String iv) throws
      DataSourceException, BadValueException {

    if (iv == null || !isIntegritySystemEnabled()) {
      return null;
    }

    EnforcableIntegrityVector eiv = new EnforcableIntegrityVector();
    String current_token = null;

    StringTokenizer tokens = new StringTokenizer(iv, "<>");
    while (tokens.hasMoreTokens()) {
      current_token = tokens.nextToken();
      StringTokenizer st = new StringTokenizer(current_token, ":");
      while (st.hasMoreTokens()) {
        eiv.addIntegrityCheck(getIntegrityCheck(st.nextToken()), st.nextToken());
      }
    }

    return eiv;
  }

  /**
   * Implements {@link MIDDataSource#newViolationsVector(String)}.
   * @param iv the {@link String} representation of vector.
   * @return the {@link ViolationsVector}.
   * @throws DataSourceException if failed to create new violations vector.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ViolationsVector newViolationsVector(String iv) throws
      DataSourceException, BadValueException {

    if (iv == null || !isIntegritySystemEnabled()) {
      return null;
    }

    ViolationsVector vv = new ViolationsVector();
    String current_token = null;

    StringTokenizer tokens = new StringTokenizer(iv, "<>");
    while (tokens.hasMoreTokens()) {
      current_token = tokens.nextToken();
      StringTokenizer st = new StringTokenizer(current_token, ":");
      while (st.hasMoreTokens()) {
        vv.addIntegrityCheck(
            getIntegrityCheck(st.nextToken()), st.nextToken());
      }
    }

    return vv;
  }

  /**
   * Implements {@link MIDDataSource#getMergeFact(String, int)}.
   * @param merge_set the {@link String} representation of merge set.
   * @param merge_fact_id the merge fact id.
   * @return the {@link MergeFact}.
   * @throws DataSourceException if failed to get merge set.
   * @throws BadValueException if failed due to invalid data value.
   */
  public MergeFact getMergeFact(String merge_set, int merge_fact_id) throws
      DataSourceException, BadValueException {

    MEMEToolkit.trace(
        "MIDConnection.getMergeFact(String, int)...");

    MergeFact fact = null;
    String query = "SELECT * FROM mom_merge_facts"
        + " WHERE merge_set = ? AND merge_fact_id = ?";
    try {
      // Eventually this looks in mom_facts_processed too!
      PreparedStatement pstmt = prepareStatement(query);
      pstmt.setString(1, merge_set);
      pstmt.setInt(2, merge_fact_id);
      ResultSet rs = pstmt.executeQuery();

      // Read and exactly one row should be returned
      int ctr = 0;
      while (rs.next()) {
        fact = new MergeFact();
        fact.setIdentifier(new Identifier.Default(rs.getInt("MERGE_FACT_ID")));
        fact.setAtom(getAtomWithName(rs.getInt("ATOM_ID_1"), null));
        String merge_level = rs.getString("MERGE_LEVEL");
        if (merge_level.equals("SY")) {
          fact.setRank(new Rank.Default(0));
        } else if (merge_level.equals("MAT")) {
          fact.setRank(new Rank.Default(1));
        } else if (merge_level.equals("NRM")) {
          fact.setRank(new Rank.Default(2));
        } else {
          fact.setRank(new Rank.Default(9));
        }
        fact.setConnectedAtom(getAtomWithName(rs.getInt("ATOM_ID_2"), null));
        fact.setSource(getSource(rs.getString("SOURCE")));
        fact.setIntegrityVector(newEnforcableIntegrityVector(rs.getString(
            "INTEGRITY_VECTOR")));
        if (rs.getString("MAKE_DEMOTION").equals("Y")) {
          fact.setDemoteIfMergeFails(true);
        } else {
          fact.setDemoteIfMergeFails(false);
        }
        if (rs.getString("CHANGE_STATUS").equals("Y")) {
          fact.setChangeStatus(true);
        } else {
          fact.setChangeStatus(false);
        }
        fact.setAuthority(getAuthority(rs.getString("AUTHORITY")));
        fact.setName(merge_set);
        fact.setViolationsVector(newViolationsVector(rs.getString(
            "VIOLATIONS_VECTOR")));
        fact.setStatus(rs.getString("STATUS"));
        int molecule_id = rs.getInt("MOLECULE_ID");
        if (molecule_id != 0) {
          fact.setAction(getFullMolecularAction(molecule_id));

        }
        ctr++;
      }

      // Close statement
      pstmt.close();

      if (ctr == 0) {
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("merge_set", merge_set);
        dse.setDetail("merge_fact_id", String.valueOf(merge_fact_id));
        throw dse;
      }

      if (ctr > 1) {
        DataSourceException dse = new DataSourceException("Too much Data.");
        dse.setDetail("merge_set", merge_set);
        dse.setDetail("merge_fact_id", String.valueOf(merge_fact_id));
        throw dse;
      }

      MEMEToolkit.trace(
          "MIDConnection.getMergeFact(String, int)... completed.");

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to load merge fact.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }

    return fact;
  }

  //
  // Action
  //

  /**
   * Implements {@link MEMEDataSource#getActionEngine()}.
   * @return the {@link ActionEngine}.
   */
  public ActionEngine getActionEngine() {
    if (Boolean.valueOf(MEMEToolkit.getProperty(MEMEConstants.
                                                VALIDATE_MOLECULAR_ACTIONS,
                                                "false")).booleanValue()) {
      return new ValidatingActionEngine(this);
    } else {
      return new MIDActionEngine(this);
    }
  }

  //
  // Action Validation Management
  //

  /**
   * Enables atomic action validation.
   * @throws DataSourceException if failed to set atomic action validation.
   */
  public void setAtomicActionValidationEnabled() throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setAtomicActionValidationEnabled()...");

    String call_str = "{call MEME_APROCS.set_validate_on}";

    try {
      CallableStatement cstmt = prepareCall(call_str);
      cstmt.execute();
      cstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Set atomic action validation enabled failed.", call_str, se);
      dse.setDetail("call_str", call_str);
      throw dse;
    }
  }

  /**
   * Disables atomic action validation.
   * @throws DataSourceException if failed to set atomic action validation.
   */
  public void setAtomicActionValidationDisabled() throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setAtomicActionValidationDisabled()...");

    String call_str = "{call MEME_APROCS.set_validate_off}";

    try {
      CallableStatement cstmt = prepareCall(call_str);
      cstmt.execute();
      cstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Set atomic action validation disabled failed.", call_str, se);
      dse.setDetail("call_str", call_str);
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#isAtomicActionValidationEnabled()}.
   * @return <code>true</code> if atomic action validation is enabled;
   * <code>false</code> otherwise.
   * @throws DataSourceException if failed to determine atomic action
   * validation status.
   */
  public boolean isAtomicActionValidationEnabled() throws DataSourceException {
    return Boolean.valueOf(MEMEToolkit.getProperty(
        MEMEConstants.VALIDATE_ATOMIC_ACTIONS, "false")).booleanValue();
  }

  /**
   * Enables molecular action validation.
   * @throws DataSourceException if failed to set molecular action validation.
   */
  public void setMolecularActionValidationEnabled() throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setMolecularActionValidationEnabled()...");
  }

  /**
   * Disables molecular action validation.
   * @throws DataSourceException if failed to set molecular action validation.
   */
  public void setMolecularActionValidationDisabled() throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setMolecularActionValidationDisabled()...");
  }

  /**
   * Implements {@link MIDDataSource#isMolecularActionValidationEnabled()}.
   * @return <code>true</code> if molecular action validation is enabled;
   * <code>false</code> otherwise.
   * @throws DataSourceException if failed to determine molecular action
   * validation status.
   */
  public boolean isMolecularActionValidationEnabled() throws
      DataSourceException {
    return Boolean.valueOf(MEMEToolkit.getProperty(
        MEMEConstants.VALIDATE_MOLECULAR_ACTIONS, "false")).booleanValue();
  }

  //
  // Valid Values Management
  //

  /**
   * Implements {@link MIDDataSource#getValidCharValues(String)}.
   * @param field_map A key in the hash map.
   * @return An array of <code>char</code>.
   */
  public char[] getValidCharValues(String field_map) {
    char[] chars = (char[]) valid_char_values_cache.get(getDataSourceName(),
        field_map);
    return chars;

  }

  /**
   * Implements {@link MIDDataSource#getValidStatusValuesForAtoms()}.
   * @return An array of <code>char</code> representation of status.
   * @throws DataSourceException if failed to load valid status.
   */
  public char[] getValidStatusValuesForAtoms() throws DataSourceException {
    return getValidCharValues("status-C");
  }

  /**
   * Implements {@link MIDDataSource#getValidStatusValuesForAttributes()}.
   * @return An array of <code>char</code> representation of status.
   * @throws DataSourceException if failed to load valid status.
   */
  public char[] getValidStatusValuesForAttributes() throws DataSourceException {
    return getValidCharValues("status-A");
  }

  /**
   * Implements {@link MIDDataSource#getValidStatusValuesForConcepts()}.
   * @return An array of <code>char</code> representation of status.
   * @throws DataSourceException if failed to load valid status.
   */
  public char[] getValidStatusValuesForConcepts() throws DataSourceException {
    return getValidCharValues("status-CS");
  }

  /**
   * Implements {@link MIDDataSource#getValidStatusValuesForRelationships()}.
   * @return An array of <code>char</code> representation of status.
   * @throws DataSourceException if failed to load valid status.
   */
  public char[] getValidStatusValuesForRelationships() throws
      DataSourceException {
    return getValidCharValues("status-R");
  }

  /**
   * Implements {@link MIDDataSource#getValidLevelValuesForRelationships()}.
   * @return An array of <code>char</code> representation of level.
   * @throws DataSourceException if failed to load valid level.
   */
  public char[] getValidLevelValuesForRelationships() throws
      DataSourceException {
    return getValidCharValues("level-R");
  }

  /**
   * Implements {@link MIDDataSource#getValidLevelValuesForAttributes()}.
   * @return An array of <code>char</code> representation of level.
   * @throws DataSourceException if failed to load valid level.
   */
  public char[] getValidLevelValuesForAttributes() throws DataSourceException {
    return getValidCharValues("level-A");
  }

  /**
   * Implements {@link MIDDataSource#getValidReleasedValues()}.
   * @return An array of <code>char</code> representation of valid released
   * values.
   * @throws DataSourceException if failed to load valid released.
   */
  public char[] getValidReleasedValues() throws DataSourceException {
    return getValidCharValues("released");
  }

  /**
   * Implements {@link MIDDataSource#getValidTobereleasedValues()}.
   * @return An array of <code>char</code> representation of valid tobereleased
   * values.
   * @throws DataSourceException if failed to load valid tobereleased.
   */
  public char[] getValidTobereleasedValues() throws DataSourceException {
    return getValidCharValues("tobereleased");
  }

  //
  // Editing System Management
  //

  /**
   * Implements {@link MIDDataSource#addEditorPreferences(EditorPreferences)}.
   * @param ep An of object {@link EditorPreferences}
   * @throws DataSourceException if failed to add editor preferences
   * @throws BadValueException of failed due to bad value
   */
  public void addEditorPreferences(EditorPreferences ep) throws
      DataSourceException, BadValueException {

    //
    // If editor already exists, bail
    //
    if (editor_pref_u_cache.get(getDataSourceName(), ep.getUserName()) != null) {
      throw new BadValueException(
          "Editor preferences with this name already exists", ep);
    }

    MEMEToolkit.trace(
        "MIDConnection.addEditorPreferences(EditorPreferences)...");

    final String insert_editor = "INSERT INTO editors" +
        " (name, editor_level, initials, grp, cur)" +
        " VALUES (?,?,?,?,?)";

    try {
      PreparedStatement pstmt = prepareStatement(insert_editor);
      pstmt.setString(1, ep.getUserName());
      pstmt.setInt(2, ep.getEditorLevel());
      pstmt.setString(3, ep.getInitials());
      pstmt.setString(4, ep.getEditorGroup());
      pstmt.setString(5, ep.isCurrent() ? "Y" : "N");
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();
    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to insert row into editors.", insert_editor, se);
      dse.setDetail("insert", insert_editor);
      dse.setDetail("ep_name", ep.getUserName());
      throw dse;
    }

    final String insert_ep = "INSERT INTO editor_preferences" +
        " (name, initials, show_concept, show_classes, show_relationships, " +
        "  show_attributes) VALUES (?,?,?,?,?,?)";

    try {
      PreparedStatement pstmt = prepareStatement(insert_ep);
      pstmt.setString(1, ep.getUserName());
      pstmt.setString(2, ep.getInitials());
      pstmt.setInt(3, ep.showConcept() ? 1 : 0);
      pstmt.setInt(4, ep.showAtoms() ? 1 : 0);
      pstmt.setInt(5, ep.showRelationships() ? 1 : 0);
      pstmt.setInt(6, ep.showAttributes() ? 1 : 0);
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();
    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to insert row into editors.", insert_ep, se);
      dse.setDetail("insert", insert_ep);
      dse.setDetail("ep_name", ep.getUserName());
      throw dse;
    }

    // Update the cache
    cacheEditorPreferences();

  }

  /**
       * Implements {@link MIDDataSource#removeEditorPreferences(EditorPreferences)}.
   * @param ep An of object {@link EditorPreferences}
   * @throws DataSourceException if failed to remove editor preferences
   */
  public void removeEditorPreferences(EditorPreferences ep) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.removeEditorPreferences(EditorPreferences)...");

    final String remove_editor = "DELETE FROM editors WHERE name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(remove_editor);
      pstmt.setString(1, ep.getUserName());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();
    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from editors.", remove_editor, se);
      dse.setDetail("remove", remove_editor);
      dse.setDetail("ep_name", ep.getUserName());
      throw dse;
    }

    final String remove_ep = "DELETE FROM editor_preferences WHERE name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(remove_ep);
      pstmt.setString(1, ep.getUserName());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();
    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to remove row from editor_preferences.", remove_ep, se);
      dse.setDetail("remove", remove_ep);
      dse.setDetail("ep_name", ep.getUserName());
      throw dse;
    }

    // Update the cache
    cacheEditorPreferences();

  }

  /**
   * Implements {@link MIDDataSource#setEditorPreferences(EditorPreferences)}.
   * @param ep An of object {@link EditorPreferences}
   * @throws DataSourceException if failed to set editor preferences
   */
  public void setEditorPreferences(EditorPreferences ep) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setEditorPreferences(EditorPreferences)...");

    final String update_editor = "UPDATE editors SET name = ?, " +
        "editor_level = ?, initials = ?, grp = ?, cur = ? WHERE name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_editor);
      pstmt.setString(1, ep.getUserName());
      pstmt.setInt(2, ep.getEditorLevel());
      pstmt.setString(3, ep.getInitials());
      pstmt.setString(4, ep.getEditorGroup());
      pstmt.setString(5, ep.isCurrent() ? "Y" : "N");
      pstmt.setString(6, ep.getUserName());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();
    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to update row from editors.", update_editor, se);
      dse.setDetail("update", update_editor);
      dse.setDetail("ep_name", ep.getUserName());
      throw dse;
    }

    final String update_ep = "UPDATE editor_preferences SET name = ?, " +
        "initials = ?, show_concept = ?, show_classes = ?, show_relationships = ?, " +
        "show_attributes = ? WHERE name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_ep);
      pstmt.setString(1, ep.getUserName());
      pstmt.setString(2, ep.getInitials());
      pstmt.setInt(3, ep.showConcept() ? 1 : 0);
      pstmt.setInt(4, ep.showAtoms() ? 1 : 0);
      pstmt.setInt(5, ep.showRelationships() ? 1 : 0);
      pstmt.setInt(6, ep.showAttributes() ? 1 : 0);
      pstmt.setString(7, ep.getUserName());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();
    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to update row from editor_preferences.", update_ep, se);
      dse.setDetail("update", update_ep);
      dse.setDetail("ep_name", ep.getUserName());
      throw dse;
    }

    // Update the cache
    cacheEditorPreferences();

  }

  /**
   * Implements {@link MIDDataSource#getEditorPreferences()}.
   * @return An array of object {@link EditorPreferences}
   */
  public EditorPreferences[] getEditorPreferences() {
    EditorPreferences[] eps = (EditorPreferences[]) editor_pref_i_cache.values(
        getDataSourceName()).
        toArray(new EditorPreferences[0]);
    return eps;
  }

  /**
   * Implements {@link MIDDataSource#getEditorPreferencesByInitials(String)}.
   * @param initials A {@link String} representing of editor's initials
   * @return The {@link EditorPreferences} representing the editor
   * @throws BadValueException if failed due to invalid data value.
   */
  public EditorPreferences getEditorPreferencesByInitials(String initials) throws
      BadValueException {
    EditorPreferences ef = (EditorPreferences) editor_pref_i_cache.get(
        getDataSourceName(), initials);
    if (ef == null) {
      BadValueException bve = new BadValueException("Bad initials.");
      bve.setDetail("initials", initials);
      throw bve;
    }
    return ef;
  }

  /**
   * Implements {@link MIDDataSource#getEditorPreferencesByUsername(String)}.
   * @param username A {@link String} representing of editor's username
   * @return The {@link EditorPreferences} representing the editor
   * @throws BadValueException if failed due to invalid data value.
   */
  public EditorPreferences getEditorPreferencesByUsername(String username) throws
      BadValueException {
    EditorPreferences ef = (EditorPreferences) editor_pref_u_cache.get(
        getDataSourceName(), username);
    if (ef == null) {
      BadValueException bve = new BadValueException("Bad username.");
      bve.setDetail("username", username);
      throw bve;
    }
    return ef;
  }

  /**
   * Implements {@link MIDDataSource#setEditingEnabled(boolean)}.
   * @param status A <code>boolean</code> represents whether editing systems
   * must be enabled or disabled.
   * @throws DataSourceException if failed to set editing system.
   */
  public void setEditingEnabled(boolean status) throws DataSourceException {
    setSystemStatus("dba_cutoff", status ? "y" : "n");
  }

  /**
   * Implements {@link MIDDataSource#isEditingEnabled()}.
   * @return <code>true</code> if editing system is enabled; <code>false</code>
   * otherwise.
   * @throws DataSourceException if failed to determine editing system status.
   */
  public boolean isEditingEnabled() throws DataSourceException {
    return getSystemStatus("dba_cutoff").equals("y");
  }

  //
  // Integrity System Management
  //

  /**
   * Implements {@link MIDDataSource#setIntegritySystemEnabled(boolean)}.
   * @param ic_status A <code>boolean</code> represents whether integrity
   * systems must be enabled or disabled.
   * @throws DataSourceException if failed to set integrity system.
   */
  public void setIntegritySystemEnabled(boolean ic_status) throws
      DataSourceException {
    setSystemStatus("ic_system", ic_status ? "ON" : "OFF");
  }

  /**
   * Implements {@link MIDDataSource#isIntegritySystemEnabled()}.
   * @return <code>true</code> if integrity system is enabled;
   * <code>false</code> otherwise.
       * @throws DataSourceException if failed to determine integrity system status.
   */
  public boolean isIntegritySystemEnabled() throws DataSourceException {
    return getSystemStatus("ic_system").equals("ON");
  }

  //
  // Integrity Check Management
  //

  /**
   * Implements {@link MIDDataSource#addIntegrityCheck(IntegrityCheck)}.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to add integrity check.
   * @throws BadValueException if failed due to bad value
   */
  public void addIntegrityCheck(IntegrityCheck ic) throws DataSourceException,
      BadValueException {

    //
    // If IC already exists, just bail
    //
    if (getIntegrityCheck(ic.getName()) != null) {
      throw new BadValueException(
          "Integrity check with this name already exists",
          ic);
    }

    MEMEToolkit.trace(
        "MIDConnection.addIntegrityCheck(IntegrityCheck)...");

    String insert_str = "INSERT INTO integrity_constraints "
        +
        "(ic_name, v_actions, c_actions, ic_status, ic_type, activation_date, "
        + "deactivation_date, ic_short_dsc, ic_long_dsc) "
        +
        "VALUES (?, 'CS,CA,CF,MV,D,MG,I,SP,CT', null, ?, ?, sysdate, sysdate, ?, ?)";

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      pstmt.setString(1, ic.getName());
      pstmt.setString(2, ic.isActive() ? "A" : "I");
      pstmt.setString(3, ic.isFatal() ? "I" : "R");
      pstmt.setString(4, ic.getShortDescription());
      pstmt.setString(5, ic.getDescription());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // Add to the cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to integrity_constraints.", insert_str, se);
      dse.setDetail("insert", insert_str);
      dse.setDetail("ic_name", ic.getName());
      dse.setDetail("ic_status", ic.isActive() ? "A" : "I");
      dse.setDetail("ic_type", ic.isFatal() ? "I" : "R");
      dse.setDetail("ic_short_dsc", ic.getShortDescription());
      dse.setDetail("ic_long_dsc", ic.getDescription());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#setIntegrityCheck(IntegrityCheck)}.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to set integrity check.
   */
  public void setIntegrityCheck(IntegrityCheck ic) throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setIntegrityCheck(IntegrityCheck)...");

    String update_str = "UPDATE integrity_constraints "
        + "SET ic_name = ?, v_actions = ?, c_actions = ?, ic_status = ?,"
        + "    ic_type = ?, activation_date = ?, deactivation_date = ?,"
        + "    ic_short_dsc = ?, ic_long_dsc = ? "
        + "WHERE ic_name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_str);
      pstmt.setString(1, ic.getName());
      pstmt.setString(2, "CS,CA,CF,MV,D,MG,I,SP,CT");
      pstmt.setString(3, "");
      pstmt.setString(4, ic.isActive() ? "A" : "I");
      pstmt.setString(5, ic.isFatal() ? "I" : "R");
      pstmt.setString(6, "");
      pstmt.setString(7, "");
      pstmt.setString(8, ic.getShortDescription());
      pstmt.setString(9, ic.getDescription());
      pstmt.setString(10, ic.getName());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // Update the cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to update row from integrity_constraints.", update_str, se);
      dse.setDetail("update", update_str);
      dse.setDetail("ic_name", ic.getName());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#removeIntegrityCheck(IntegrityCheck)}.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to remove integrity check.
   */
  public void removeIntegrityCheck(IntegrityCheck ic) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.removeIntegrityCheck(IntegrityCheck)...");

    String delete_str = "DELETE FROM integrity_constraints "
        + "WHERE ic_name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(delete_str);
      pstmt.setString(1, ic.getName());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // remove from cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to delete row from integrity_constraints.", delete_str, se);
      dse.setDetail("delete", delete_str);
      dse.setDetail("ic_name", ic.getName());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#activateIntegrityCheck(IntegrityCheck)}.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to activate integrity check.
   */
  public void activateIntegrityCheck(IntegrityCheck ic) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.activateIntegrityCheck(IntegrityCheck)...");

    String update_str = "UPDATE integrity_constraints"
        + " SET ic_status = ?, activation_date = sysdate"
        + " WHERE ic_name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_str);
      pstmt.setString(1, "A");
      pstmt.setString(2, ic.getName());
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to activate integrity check.", update_str, se);
      dse.setDetail("update", update_str);
      throw dse;
    }

    // Re-cache this check
    cacheIntegrities();
  }

  /**
   * Implements {@link MIDDataSource#deactivateIntegrityCheck(IntegrityCheck)}.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to deactivate integrity check.
   */
  public void deactivateIntegrityCheck(IntegrityCheck ic) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.deactivateIntegrityCheck(IntegrityCheck)...");

    String update_str = "UPDATE integrity_constraints"
        + " SET ic_status = ?, deactivation_date = sysdate"
        + " WHERE ic_name = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_str);
      pstmt.setString(1, "I");
      pstmt.setString(2, ic.getName());
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to deactivate integrity check.", update_str, se);
      dse.setDetail("update", update_str);
      throw dse;
    }

    // Re-cache this check
    cacheIntegrities();
  }

  /**
   * Implements {@link MIDDataSource#getIntegrityCheck(String)}.
   * @param check_name the {@link String} representation of ic name.
   * @return the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to load integrity check.
   */
  public IntegrityCheck getIntegrityCheck(String check_name) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.getIntegrityCheck(String)...");

    IntegrityCheck check = (IntegrityCheck) integrity_cache.get(
        getDataSourceName(), check_name);
    if (check != null) {
      return check;
    }

    // Query to look up data in integrity constraints
    final String ic_query =
        "SELECT ic_name, ic_status, ic_type, activation_date, " +
        "deactivation_date, ic_short_dsc, ic_long_dsc " +
        "FROM integrity_constraints WHERE ic_name =?";

    // Query to look up data in ic single matching the current ic name
    final String ic_single_query =
        "SELECT ic_name, negation, type, value " +
        "FROM ic_single WHERE ic_name = ?";

    // Query to look up data in ic pair matching the current ic name
    final String ic_pair_query =
        "SELECT ic_name, negation, type_1, value_1, type_2, value_2 " +
        "FROM ic_pair WHERE ic_name = ?";

    String class_name = null;

    try {
      PreparedStatement ic_single_pstmt = null;
      PreparedStatement ic_pair_pstmt = null;
      try {
        ic_single_pstmt = prepareStatement(ic_single_query);
        ic_pair_pstmt = prepareStatement(ic_pair_query);
      } catch (SQLException se) {
        DataSourceException dse = new DataSourceException(
            "Failed to prepare statement.", check_name, se);
        dse.setDetail("query1", ic_single_query);
        dse.setDetail("query2", ic_pair_query);
        throw dse;
      }

      // Prepare and execute READ_IC_APPLICATIONS query
      PreparedStatement ic_pstmt = prepareStatement(ic_query);
      ic_pstmt.setString(1, check_name);
      ResultSet rs = ic_pstmt.executeQuery();

      // Create IntegrityCheck object
      IntegrityCheck ic = null;

      // Get results
      while (rs.next()) {

        String ic_name = rs.getString("IC_NAME");
        class_name = "gov.nih.nlm.meme.integrity." + ic_name;
        ic = (IntegrityCheck) Class.forName(class_name).newInstance();

        String ic_status = rs.getString("IC_STATUS");
        if (ic_status.equals("A")) {
          ic.setIsActive(true);
          ic.setTimestamp(getDate(rs.getTimestamp("ACTIVATION_DATE")));
        } else { // "I"
          ic.setIsActive(false);
          ic.setTimestamp(getDate(rs.getTimestamp("DEACTIVATION_DATE")));
        }
        ic.setIsFatal(rs.getString("IC_TYPE").equals("I"));
        ic.setShortDescription(rs.getString("IC_SHORT_DSC"));
        ic.setDescription(rs.getString("IC_LONG_DSC"));

        ArrayList data = new ArrayList();

        if (ic instanceof UnaryDataIntegrityCheck) {
          try {
            // UnaryDataIntegrityCheck, look up data in ic_single.
            ic_single_pstmt.setString(1, ic_name);
            ResultSet ic_single_rs = ic_single_pstmt.executeQuery();
            while (ic_single_rs.next()) {
              String name = ic_single_rs.getString("IC_NAME");
              String type = ic_single_rs.getString("TYPE");
              String value = ic_single_rs.getString("VALUE");
              data.add(new UnaryCheckData(name, type, value,
                                          !ic_single_rs.getString("NEGATION").
                                          equals("N")));
            }
            ( (UnaryDataIntegrityCheck) ic).
                setCheckData( (UnaryCheckData[]) data.toArray(new
                UnaryCheckData[0]));

          } catch (SQLException se) {
            DataSourceException dse = new DataSourceException(
                "Failed to get unary check data.", ic_single_query, se);
            dse.setDetail("query", ic_single_query);
            dse.setDetail("name", ic_name);
            throw dse;
          }
        }

        if (ic instanceof BinaryDataIntegrityCheck) {
          try {
            // BinaryDataIntegrityCheck, look up data in ic_pair.
            ic_pair_pstmt.setString(1, ic_name);
            ResultSet ic_pair_rs = ic_pair_pstmt.executeQuery();
            while (ic_pair_rs.next()) {
              String name = ic_pair_rs.getString("IC_NAME");
              String type_1 = ic_pair_rs.getString("TYPE_1");
              String value_1 = ic_pair_rs.getString("VALUE_1");
              String type_2 = ic_pair_rs.getString("TYPE_2");
              String value_2 = ic_pair_rs.getString("VALUE_2");
              data.add(new BinaryCheckData(name, type_1, type_2, value_1,
                                           value_2,
                                           !ic_pair_rs.getString("NEGATION").
                                           equals("N")));
            }
            ( (BinaryDataIntegrityCheck) ic).
                setCheckData( (BinaryCheckData[]) data.toArray(new
                BinaryCheckData[0]));

          } catch (SQLException se) {
            DataSourceException dse = new DataSourceException(
                "Failed to get binary check data.", ic_pair_query, se);
            dse.setDetail("query", ic_pair_query);
            dse.setDetail("name", ic_name);
            throw dse;
          }
        }
      }

      // add to hashmap if not null
      if (ic != null) {
        integrity_cache.put(getDataSourceName(), ic.getName(), ic);

        // Close statements
      }
      ic_pstmt.close();
      ic_single_pstmt.close();
      ic_pair_pstmt.close();

      return ic;

    } catch (DataSourceException dse) {
      throw dse;
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to get integrity checks.", ic_query, se);
      dse.setDetail("query", ic_query);
      throw dse;
    } catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Class could not be loaded.", class_name, e);
      throw dse;
    }

  }

  /**
   * Implements {@link MIDDataSource#getIntegrityChecks()}.
   * @return An array of object {@link IntegrityCheck}.
   * @throws DataSourceException if failed to load integrity check.
   */
  public IntegrityCheck[] getIntegrityChecks() throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.getIntegrityChecks()...");

    if (!isIntegritySystemEnabled()) {
      return new IntegrityCheck.Default[0];
    }

    String query = "SELECT ic_name " +
        "FROM integrity_constraints";

    try {
      // Prepare and execute query

      PreparedStatement ic_pstmt = prepareStatement(query);
      ResultSet rs = ic_pstmt.executeQuery();

      List checks = new ArrayList();

      // Read
      while (rs.next()) {
        String ic_name = rs.getString("IC_NAME");
        checks.add(getIntegrityCheck(ic_name));
      }

      // Close statement
      ic_pstmt.close();

      return (IntegrityCheck[]) checks.toArray(new IntegrityCheck[0]);

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to load integrity checks.", query, se);
      throw dse;
    }
  }

  //
  // Application Vector Management
  //

  /**
   * Implements {@link MIDDataSource#addApplicationVector(String, IntegrityVector)}.
   * @param application the {@link String} representation of ic application.
   * @param iv the {@link IntegrityVector}.
   * @throws DataSourceException if failed to add integrity vector.
   * @throws BadValueException if failed due to bad value
   */
  public void addApplicationVector(String application, IntegrityVector iv) throws
      DataSourceException, BadValueException {

    MEMEToolkit.trace(
        "MIDConnection.addIntegrityVector(IntegrityVector)...");

    //
    // If IC already exists, just bail
    //
  	try {
      getApplicationVector(application);
      throw new BadValueException(
          "Application vector with this name already exists",
          application);
    } catch (MissingDataException mde) {
    	// No application vector with this name, ok to proceed
    }

    String insert_str = "INSERT INTO ic_applications "
        + "(application, integrity_vector) "
        + "VALUES (?, ?)";

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      pstmt.setString(1, application);
      pstmt.setString(2, iv.toString());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // add to the cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to insert row to ic_applications.", insert_str, se);
      dse.setDetail("insert", insert_str);
      dse.setDetail("application", application);
      dse.setDetail("vector", iv.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#setApplicationVector(String, IntegrityVector)}.
   * @param application the {@link String} representation of ic application.
   * @param iv the {@link IntegrityVector}.
   * @throws DataSourceException if failed to set application vector.
   */
  public void setApplicationVector(String application, IntegrityVector iv) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setApplicationVector(String, IntegrityVector)...");

    String update_str = "UPDATE ic_applications"
        + " SET integrity_vector = ? WHERE application = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_str);
      pstmt.setString(1, iv.toString());
      pstmt.setString(2, application);
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // update the cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to set the application vector.", update_str, se);
      dse.setDetail("update", update_str);
      dse.setDetail("application", application);
      dse.setDetail("vector", iv.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#removeApplicationVector(String)}.
   * @param application the {@link String}.
   * @throws DataSourceException if failed to remove application vector.
   */
  public void removeApplicationVector(String application) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.removeApplicationVector(String)...");

    String delete_str = "DELETE FROM ic_applications "
        + "WHERE application = ?";

    try {
      PreparedStatement pstmt = prepareStatement(delete_str);
      pstmt.setString(1, application);
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // remove from cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to delete row from ic_applications.", delete_str, se);
      dse.setDetail("delete", delete_str);
      dse.setDetail("application", application);
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#getApplicationVector(String)}.
   * @param application the {@link String} representation of ic application.
   * @return the {@link EnforcableIntegrityVector}.
   * @throws DataSourceException if failed to load application vector.
   */
  public EnforcableIntegrityVector getApplicationVector(String application) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.getApplicationVector(String)...");

    // If integrity system off, return empty vector
    if (!isIntegritySystemEnabled()) {
      return new EnforcableIntegrityVector();
    }

    // lookup in cache first
    EnforcableIntegrityVector vector =
        (EnforcableIntegrityVector) av_cache.get(getDataSourceName(),
                                                 application);
    if (vector != null) {
      return vector;
    }

    try {
      // Prepare and execute READ_IC_APPLICATIONS query
      PreparedStatement ia_pstmt = prepareStatement(READ_IC_APPLICATIONS);
      ia_pstmt.setString(1, application);
      ResultSet rs = ia_pstmt.executeQuery();

      // Create and populate EnforcableIntegrityVector object
      EnforcableIntegrityVector eiv = new EnforcableIntegrityVector();

      String iv_string = null;

      // Read
      int ctr = 0;
      while (rs.next()) {
        iv_string = rs.getString("INTEGRITY_VECTOR");
        ctr++;
      }

      // Close statement
      ia_pstmt.close();

      if (ctr == 0) {
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("integrity_application", application);
        throw dse;
      }

      if (ctr != 1) {
        DataSourceException dse = new DataSourceException("Too much data.");
        dse.setDetail("integrity_application", application);
        throw dse;
      }

      // Extract the integrity check
      FieldedStringTokenizer st = new FieldedStringTokenizer(iv_string, ">");
      String[] parts = new String[st.countTokens()];
      ctr = 0;
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (token.length() == 0) {
          break;
        }
        parts[ctr] = token.substring(1);
        //MEMEToolkit.trace("MIDConnection.parts["+ctr+"]= " + parts[ctr]);

        // Extract the code
        FieldedStringTokenizer sub_st =
            new FieldedStringTokenizer(parts[ctr], ":");
        String[] sub_parts = new String[sub_st.countTokens()];
        int sub_ctr = 0;
        while (sub_st.hasMoreTokens()) {
          sub_parts[sub_ctr] = sub_st.nextToken().toString();
          //MEMEToolkit.trace("MIDConnection.sub_parts["+sub_ctr+"]= "
          //+ sub_parts[sub_ctr]);
          sub_ctr++;
        }

        // Get valid integrity check from the hash map
        IntegrityCheck ic = getIntegrityCheck(sub_parts[0]);
        String code = sub_parts[1];
        // Get the ic code for this check
        eiv.addIntegrityCheck(ic, code);
        ctr++;
      }

      // update the cache
      av_cache.put(getDataSourceName(), application, eiv);

      return eiv;

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to load integrity vector.", READ_IC_APPLICATIONS, se);
      dse.setDetail("query", READ_IC_APPLICATIONS);
      dse.setDetail("application", application);
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#getApplicationsWithVectors()}.
   * @return An array of object {@link String}.
   * @throws DataSourceException if failed to load application vector.
   */
  public String[] getApplicationsWithVectors() throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.getApplicationWithVectors()...");

    if (!isIntegritySystemEnabled()) {
      return new String[0];
    }

    String query = "SELECT application " +
        "FROM ic_applications";

    try {
      // Prepare and execute READ_IC_APPLICATIONS query

      PreparedStatement ia_pstmt = prepareStatement(query);
      ResultSet rs = ia_pstmt.executeQuery();

      List vectors = new ArrayList();

      // Read
      while (rs.next()) {
        String application = rs.getString("APPLICATION");
        vectors.add(application);
      }

      // Close statement
      ia_pstmt.close();

      return (String[]) vectors.toArray(new String[0]);

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to load integrity vectors.", query, se);
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#addCheckToApplicationVector(String, IntegrityCheck, String)}.
   * @param application the {@link String} representation of ic application.
   * @param check the {@link IntegrityCheck}.
   * @param code the integrity check code.
   * @throws DataSourceException if failed to add check to application vector.
   */
  public void addCheckToApplicationVector(String application,
                                          IntegrityCheck check,
                                          String code) throws
      DataSourceException {

    IntegrityVector iv = getApplicationVector(application);
    iv.addIntegrityCheck(check, code);
    setApplicationVector(application, iv);
  }

  /**
   * Implements {@link MIDDataSource#removeCheckFromApplicationVector(String, IntegrityCheck)}.
   * @param application the {@link String} representation of ic application.
   * @param check the {@link IntegrityCheck}.
       * @throws DataSourceException if failed to remove check to application vector.
   */
  public void removeCheckFromApplicationVector(String application,
                                               IntegrityCheck check) throws
      DataSourceException {

    IntegrityVector iv = getApplicationVector(application);
    iv.removeIntegrityCheck(check);
    setApplicationVector(application, iv);
  }

  //
  // Override Vector Management
  //

  /**
   * Implements {@link MIDDataSource#addOverrideVector(int, IntegrityVector)}.
   * @param ic_level the ic level.
   * @param iv the {@link IntegrityVector}.
   * @throws DataSourceException if failed to insert override vector.
   * @throws BadValueException if failed due to bad value
   */
  public void addOverrideVector(int ic_level, IntegrityVector iv) throws
      DataSourceException, BadValueException {

    //
    // If IC already exists, just bail
    //
  	try {
      getOverrideVector(ic_level);
      throw new BadValueException(
          "Override vector with this level already exists",
          new Integer(ic_level));
    } catch (MissingDataException mde) {
    	// No override vector at this level, ok to proceed
    }

    MEMEToolkit.trace(
        "MIDConnection.addOverrideVector(int, IntegrityVector)...");

    String insert_str = "INSERT INTO ic_override "
        + "(ic_level, override_vector) "
        + "VALUES (?, ?)";

    try {
      PreparedStatement pstmt = prepareStatement(insert_str);
      pstmt.setInt(1, ic_level);
      pstmt.setString(2, iv.toString());
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // add to the cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to insert the override vector.", insert_str, se);
      dse.setDetail("insert", insert_str);
      dse.setDetail("ic_level", new Integer(ic_level));
      dse.setDetail("vector", iv.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#setOverrideVector(int, IntegrityVector)}.
   * @param ic_level the ic level.
   * @param iv the {@link IntegrityVector}.
   * @throws DataSourceException if failed to set override vector.
   */
  public void setOverrideVector(int ic_level, IntegrityVector iv) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setOverrideVector(int, IntegrityVector)...");

    String update_str = "UPDATE ic_override SET override_vector = ?"
        + " WHERE ic_level = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_str);
      pstmt.setString(1, iv.toString());
      pstmt.setInt(2, ic_level);
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // update the cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to set the override vector.", update_str, se);
      dse.setDetail("update", update_str);
      dse.setDetail("ic_level", new Integer(ic_level));
      dse.setDetail("vector", iv.toString());
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#removeOverrideVector(int)}.
   * @param ic_level the ic level.
   * @throws DataSourceException if failed to remove override vector.
   */
  public void removeOverrideVector(int ic_level) throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.removeOverrideVector(int)...");

    String delete_str = "DELETE FROM ic_override "
        + "WHERE ic_level = ?";

    try {
      PreparedStatement pstmt = prepareStatement(delete_str);
      pstmt.setInt(1, ic_level);
      int rowcount = pstmt.executeUpdate();
      pstmt.close();
      if (rowcount != 1) {
        throw new Exception();
      }
      commit();

      // remove from cache
      cacheIntegrities();

    } catch (Exception se) {
      DataSourceException dse = new DataSourceException(
          "Failed to delete row from ic_override.", delete_str, se);
      dse.setDetail("delete", delete_str);
      dse.setDetail("ic_level", new Integer(ic_level));
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#getOverrideVector(int)}.
   * @param ic_level the ic level.
   * @return the {@link IntegrityVector}.
   * @throws DataSourceException if failed to load override vector.
   */
  public IntegrityVector getOverrideVector(int ic_level) throws
      DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.getOverrideVector(String)...");

    // If integrity system is off return empty vector
    if (!isIntegritySystemEnabled()) {
      return new IntegrityVector.Default();
    }

    // Check the cache first
    IntegrityVector vector = (IntegrityVector) ov_cache.get(getDataSourceName(),
        new Integer(ic_level));
    if (vector != null) {
      return vector;
    }

    try {
      // Prepare and execute READ_IC_OVERRIDE query
      PreparedStatement io_pstmt = prepareStatement(READ_IC_OVERRIDE);
      io_pstmt.setInt(1, ic_level);
      ResultSet rs = io_pstmt.executeQuery();

      // Create and populate IntegrityVector object
      IntegrityVector iv = new IntegrityVector.Default();

      String iv_string = null;

      // Read
      int ctr = 0;
      while (rs.next()) {
        iv_string = rs.getString("OVERRIDE_VECTOR");
        ctr++;
      }
      io_pstmt.close();

      if (ctr == 0) {
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("integrity_level", String.valueOf(ic_level));
        throw dse;
      }

      if (ctr != 1) {
        DataSourceException dse = new DataSourceException("Too much data.");
        dse.setDetail("integrity_level", String.valueOf(ic_level));
        throw dse;
      }

      // Extract the integrity check
      FieldedStringTokenizer st = new FieldedStringTokenizer(iv_string, ">");
      String[] parts = new String[st.countTokens()];
      ctr = 0;
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (token.length() == 0) {
          break;
        }
        parts[ctr] = token.substring(1);
        //MEMEToolkit.trace("MIDConnection.parts["+ctr+"]= " + parts[ctr]);

        // Extract the code
        FieldedStringTokenizer sub_st =
            new FieldedStringTokenizer(parts[ctr], ":");
        String[] sub_parts = new String[sub_st.countTokens()];
        int sub_ctr = 0;
        while (sub_st.hasMoreTokens()) {
          sub_parts[sub_ctr] = sub_st.nextToken().toString();
          //MEMEToolkit.trace("MIDConnection.sub_parts["+sub_ctr+"]= "
          //+ sub_parts[sub_ctr]);
          sub_ctr++;
        }

        // Get valid integrity check from the hash map
        IntegrityCheck ic = getIntegrityCheck(sub_parts[0]);
        String code = sub_parts[1];
        // Get the ic code for this check
        iv.addIntegrityCheck(ic, code);
        ctr++;
      }

      // update the cache
      ov_cache.put(getDataSourceName(), new Integer(ic_level), iv);

      return iv;

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to load integrity vector.", READ_IC_OVERRIDE, se);
      dse.setDetail("query", READ_IC_OVERRIDE);
      dse.setDetail("integrity_level", String.valueOf(ic_level));
      throw dse;
    }
  }

  /**
   * Implements {@link MIDDataSource#getLevelsWithOverrideVectors()}.
   * @return A list of ic level.
   * @throws DataSourceException if failed to load override vector.
   */
  public int[] getLevelsWithOverrideVectors() throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.getLevelsWithOverrideVectors()...");

    int[] ic_levels = null;

    if (!isIntegritySystemEnabled()) {
      return new int[0];
    }

    String query = "SELECT ic_level " +
        "FROM ic_override";

    try {
      PreparedStatement io_pstmt = prepareStatement(query);
      ResultSet rs = io_pstmt.executeQuery();

      List vectors = new ArrayList();

      // Read
      while (rs.next()) {
        int override = rs.getInt("IC_LEVEL");
        vectors.add(new Integer(override));
      }

      // Close statement
      io_pstmt.close();

      Integer[] levels = (Integer[]) vectors.toArray(new Integer[0]);
      ic_levels = new int[levels.length];
      for (int i = 0; i < levels.length; i++) {
        ic_levels[i] = levels[i].intValue();
      }
      return ic_levels;

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to load override vectors.", query, se);
      throw dse;
    }

  }

  /**
   * Implements {@link MIDDataSource#addCheckToOverrideVector(int,
   *   IntegrityCheck, String)}.
   * @param ic_level the ic level.
   * @param check the {@link IntegrityCheck}.
   * @param code the integrity check code.
   * @throws DataSourceException if failed to add check to application vector.
   */
  public void addCheckToOverrideVector(int ic_level,
                                       IntegrityCheck check,
                                       String code) throws DataSourceException {

    IntegrityVector iv = getOverrideVector(ic_level);
    iv.addIntegrityCheck(check, code);
    setOverrideVector(ic_level, iv);
  }

  /**
   * Implements {@link MIDDataSource#removeCheckFromOverrideVector(int,
   *   IntegrityCheck)}.
   * @param ic_level the ic level.
   * @param check the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to remove check to override vector.
   */
  public void removeCheckFromOverrideVector(int ic_level,
                                            IntegrityCheck check) throws
      DataSourceException {

    IntegrityVector iv = getOverrideVector(ic_level);
    iv.removeIntegrityCheck(check);
    setOverrideVector(ic_level, iv);
  }

  /**
   * Implements {@link MIDDataSource#setMergeFact(MergeFact)}.
   * @param fact the {@link MergeFact}.
   * @throws DataSourceException if failed to load mom_merge_facts.
   */
  public void setMergeFact(MergeFact fact) throws DataSourceException {

    MEMEToolkit.trace(
        "MIDConnection.setMergeFact(MergeFact)...");

    String update_str = "UPDATE mom_merge_facts SET "
        + "status = ?, violations_vector = ?, molecule_id = ?, work_id = ? "
        + "WHERE merge_fact_id = ?";

    try {
      PreparedStatement pstmt = prepareStatement(update_str);
      pstmt.setString(1, fact.getStatus());
      pstmt.setString(2, (fact.getViolationsVector() == null) ? null :
                      fact.getViolationsVector().toString());
      pstmt.setInt(3, (fact.getAction() == null) ? 0 :
                   fact.getAction().getTransactionIdentifier().intValue());
      pstmt.setInt(4, (fact.getAction() == null) ? 0 :
                   fact.getAction().getWorkIdentifier().intValue());
      pstmt.setInt(5, fact.getIdentifier().intValue());
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to update mom_merge_facts.", update_str, se);
      dse.setDetail("update", update_str);
      throw dse;
    }
  }

}
