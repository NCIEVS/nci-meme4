/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  Ticket
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameter container class used to handle some
 * configuration information for an editing session.
 *
 * @see MEMEConnection
 *
 * @author MEME Group
 */

public class Ticket {

  //
  // Fields
  //

  // Indicates whether or not preferred atoms of concepts should be calculated
  private boolean calculate_preferred_atom = false;

  // Indicates whether or not atom ambiguity should be checked
  private boolean check_atom_ambiguity = false;

  // Indicates whether or not long attributes should be expanded
  private boolean expand_long_attributes = false;

  // Indicates whether or not concept actions should be looked up
  private boolean read_actions = false;

  // Indicates whether or not atoms should be read
  private boolean read_atoms = false;

  // Indicates whether or not atom names should be read
  private boolean read_atom_names = false;

  // Indicates whether or not attributes should be read
  private boolean read_attributes = false;

  // Indicates whether or not coc data should be read
  private boolean read_coc_data = false;

  // Indicates whether or not concept data should be read
  private boolean read_concept = false;

  // Indicates whether or not context path data should be read
  private boolean read_context_path = false;

  // Indicates whether or not context relationships should be read
  private boolean read_context_relationships = false;

  // Indicates whether or not deleted data should be read
  private boolean read_deleted = false;

  // Indicates whether or not index entries should be read
  private boolean read_index_entries = false;

  // Indicates whether or not relationships should be read
  private boolean read_relationships = false;

  // Indicates whether or not "related atoms" and "related concepts" of relationships should be read
  private boolean read_relationship_names = false;

  // Indicates whether or not safe replacement facts should be read
  private boolean read_safe_replacement = false;

  // Indicates whether or not to include or exclude the selected languages list
  private boolean include_or_exclude = false;

  // The list of languages to read or not read
  private String[] selected_languages = new String[0];

  // Indicates whether or not to recursively expand a graph (by following rels)
  private boolean expand_graph = false;

  // Indicates the level of recursion
  private int recursive_level = 1;

  // Indicates the top recursive level
  private int top_recursive_level = 1;

  // Caches data during recursion to prevent needless lookups
  private Map graph_cache = null;

  // Transaction identifier
  private Identifier transaction_id = null;

  // Work identifier
  private Identifier work_id = null;

  // Authority
  private Authority authority = null;

  // All mapers
  private Map data_mappers_map = new HashMap();

  // Tracks whether or not various data types are to be mapped
  private Map map_data_type_map = new HashMap();

  // data type restrictions
  private Map data_type_restrictions = new HashMap();

  // data type order by
  private Map data_type_order_by = new HashMap();

  // data type queries
  private Map data_type_queries = new HashMap();

  // Tables to read from for each data type
  private Map data_table_map = new HashMap();

  // Start index for reading data types, e.g. read rels 0-1000
  private Map data_type_start = new HashMap();

  // End index for reading data types, e.g. read rels 0-1000
  private Map data_type_end = new HashMap();

  /* These parameters are used to configure the integrity system */
  // The integrity vector to use
  private EnforcableIntegrityVector vector = null;

  // The override vector to use
  private IntegrityVector override_vector = null;

  // The violations vector to use
  private ViolationsVector violations_vector = null;

  // The application name to use for retrieving the integrity vector
  private String application = "DEFAULT";

  // The current editor level
  private int editor_level = 0;

  //
  // Constructors
  //

  /**
   * Instantiates a default {@link Ticket}.
   */
  public Ticket() {
    super();
    data_table_map.put(Atom.class, "classes");
    data_table_map.put(Relationship.class, "relationships");
    data_table_map.put(ContextRelationship.class, "context_relationships");
    data_table_map.put(Attribute.class, "attributes");
  }

  //
  // Accessor Methods for flags
  //

  /**
   * Indicates whether or not the approval action should
   * be looked up when a concept is being read
   * @return A <code>true</code> if it shoud, <code>false</code> otherwise
   */
  public boolean readActions() {
    return read_actions;
  }

  /**
   * Sets the flag indicating whether or not actions should be read.
   * @param read_actions A <code>boolean</code>
   */
  public void setReadActions(boolean read_actions) {
    this.read_actions = read_actions;
  }

  /**
   * Indicates whether or not atom names should be read
   * when reading a concept.
   * @return <code>true</code> if they should, <code>false</code> otherwise
   */
  public boolean readAtomNames() {
    return read_atom_names;
  }

  /**
   * Sets the flag indicating whether or not atom names should be read.
   * @param read_atom_names A <code>boolean</code>
   */
  public void setReadAtomNames(boolean read_atom_names) {
    this.read_atom_names = read_atom_names;
  }

  /**
   * Indicates whether or not ambiguity of atoms should be checked when
   * reading a concept.
   * @return <code>true</code> if it should, <code>false</code> otherwise
   */
  public boolean checkAtomAmbiguity() {
    return check_atom_ambiguity;
  }

  /**
   * Sets the flag indicating whether or not atom ambiguity should be checked.
   * @param caa A <code>boolean</code>
   */
  public void setCheckAtomAmbiguity(boolean caa) {
    check_atom_ambiguity = caa;
  }

  /**
   * Indicates whether or not attributes should be read when reading
   * a concept.
   * @return <code>true</code> if they should, <code>false</codE> otherwise
   */
  public boolean readAttributes() {
    return read_attributes;
  }

  /**
   * Sets the flag indicating whether or not attributes should be read.
   * @param read_attributes A <code>boolean</code>
   */
  public void setReadAttributes(boolean read_attributes) {
    this.read_attributes = read_attributes;
  }

  /**
   * Indicates whether or not safe replacement should be read when reading
   * a concept.
   * @return <code>true</code> if they should, <code>false</codE> otherwise
   */
  public boolean readSafeReplacement() {
    return read_safe_replacement;
  }

  /**
   * Sets the flag indicating whether or not safe replacement should be read.
   * @param read_safe_replacement A <code>boolean</code>
   */
  public void setReadSafeReplacement(boolean read_safe_replacement) {
    this.read_safe_replacement = read_safe_replacement;
  }

  /**
   * Indicates whether long attributes should be expanded when
   * reading a concept or an attribute.
   * @return <code>true</code> if they should, <code>false</code> otherwise
   */
  public boolean expandLongAttributes() {
    return expand_long_attributes;
  }

  /**
   * Sets the flag indicating whether or not long attributes
   * (i.e. '<>LongAttribute<>:') should be expanded.
   * @param ela A <code>boolean</code>
   */
  public void setExpandLongAttributes(boolean ela) {
    this.expand_long_attributes = ela;
  }

  /**
   * Indicates whether or not atoms should be read when reading
   * a concept.
   * @return <code>true</code> if they should, <code>false</code> otherwise
   */
  public boolean readAtoms() {
    return read_atoms;
  }

  /**
   * Sets the flag indicating whether or not atom should be read.
   * @param read_atoms A <code>boolean</code>.
   */
  public void setReadAtoms(boolean read_atoms) {
    this.read_atoms = read_atoms;
  }

  /**
   * Returns the table from which to read attribute information.
   * @param c the {@link Class}.
   * @return the table from which to read attribute information.
   */
  public String getDataTable(Class c) {
    return (String) data_table_map.get(c);
  }

  /**
   * Sets the table from which to read attribute information.
   * @param c the {@link Class}.
   * @param table the table name.
   */
  public void setDataTable(Class c, String table) {
    data_table_map.put(c, table);
  }

  /**
   * Returns the query used to read information.
   * If this method returns null, then the default query should be used.
   * @param c the {@link Class} represents hashmap key.
   * @return the query from which to read information.
   */
  public String getDataTypeQuery(Class c) {
    return (String) data_type_queries.get(c);
  }

  /**
   * Sets the query used to read relationship information.
   * Used to override the default query from {@link MEMEConnectionQueries}.
   * @param c the {@link Class} represents hashmap key.
   * @param query the query represents hashmap value.
   */
  public void setDataTypeQuery(Class c, String query) {
    data_type_queries.put(c, query);
  }

  /**
   * Returns the start index for reading a section of data.
   * @param c the {@link Class} represents hashmap key
   * @return the start index for reading a section of data
   */
  public int getDataTypeStart(Class c) {
    Integer i = (Integer) data_type_start.get(c);
    if (i != null) {
      return i.intValue();
    } else {
      return -1;
    }
  }

  /**
   * Sets the start index for reading a section of data.
   * @param c the {@link Class} represents hashmap key
   * @param start the start index for reading a section of data
   */
  public void setDataTypeStart(Class c, int start) {
    data_type_start.put(c, new Integer(start));
  }

  /**
   * Returns the end index for reading a section of data.
   * @param c the {@link Class} represents hashmap key
   * @return the end index for reading a section of data
   */
  public int getDataTypeEnd(Class c) {
    Integer i = (Integer) data_type_end.get(c);
    if (i != null) {
      return i.intValue();
    } else {
      return -1;
    }
  }

  /**
   * Sets the end index for reading a section of data.
   * @param c the {@link Class} represents hashmap key
   * @param end the end index for reading a section of data
   */
  public void setDataTypeEnd(Class c, int end) {
    data_type_end.put(c, new Integer(end));
  }

  /**
   * Returns additional WHERE clauses (starting with AND) to be used when
   * populating a concept or populating an atom. The additional restriction
   * are appended to the end of the queries used to read attributes or
   * relationships.
   * If this method returns null, then the default query should be used.
   * @param c the {@link Class} represents hashmap key
   * @return the additional restriction
   */
  public String getDataTypeRestriction(Class c) {
    String res = (String) data_type_restrictions.get(c);
    return res == null ? "" : res;
  }

  /**
   * Sets the additional WHERE clause restriction used when reading
   * attributes and relationships while populating atoms and concepts.
   * Used to add additional restrictions to the default query from {@link MEMEConnectionQueries}.
   * @param c the {@link Class} represents hashmap key
   * @param restrict the additional restriction
   */
  public void setDataTypeRestriction(Class c, String restrict) {
    data_type_restrictions.put(c, " " + restrict);
  }

  /**
   * Returns additional ORDER BY clause used when populating a concept or
   * populating an atom. The additional order by clause
   * are appended to the end of the queries used to read attributes or
   * relationships.
   * If this method returns null, then the default query should be used.
   * @param c the {@link Class} represents hashmap key
   * @return the additional order by
   */
  public String getDataTypeOrderBy(Class c) {
    String order_by = (String) data_type_order_by.get(c);
    return order_by == null ? "" : order_by;
  }

  /**
   * Sets the additional ORDER BY clause when reading attributes and
   * relationships while populating atoms and concepts.
   * Used to add order by clause to the default query from {@link MEMEConnectionQueries}.
   * @param c the {@link Class} represents hashmap key
   * @param order_by the additional order by
   */
  public void setDataTypeOrderBy(Class c, String order_by) {
    data_type_order_by.put(c, " " + order_by);
  }

  /**
   * Indicates whether or not concept info should be read when
   * reading a concept.  This includes things like the CUI.
   * @return <code>true</code> if it should, <code>false</code> otherwise
   */
  public boolean readConcept() {
    return read_concept;
  }

  /**
   * Sets the flag indicating whether or not concept information
   * should be read.
   * @param read_concept A <code>boolean</code>.
   */
  public void setReadConcept(boolean read_concept) {
    this.read_concept = read_concept;
  }

  /**
   * Indicates whether or not the preferred atom of a concept
   * should be calculated when reading a concept.  The alternative
   * is to use the <code>preferred_atom_id</code> found in
   * <code>concept_status</code>
   * @return <code>true</code> if it should be, <code>false</code> otherwise
   */
  public boolean calculatePreferredAtom() {
    return calculate_preferred_atom;
  }

  /**
   * Sets the flag indicating whether or not the
   * preferred atom should be calculated.
   * @param calculate_preferred_atom A <code>boolean</code>.
   * preferred atom id.
   */
  public void setCalculatePreferredAtom(boolean calculate_preferred_atom) {
    this.calculate_preferred_atom = calculate_preferred_atom;
  }

  /**
   * Indicates whether or not context path should
   * be read when reading a concept.
   * @return <code>true</code> if they should, <code>false</code> otherwise
   */
  public boolean readContextPath() {
    return read_context_path;
  }

  /**
   * Sets the flag indicating whether or not context path
   * should be read.
   * @param read_context_path A <code>boolean</code>.
   */
  public void setReadContextPath(boolean read_context_path) {
    this.read_context_path = read_context_path;
  }

  /**
   * Indicates whether or not context relationships should
   * be read when reading a concept. First, the flag must be
   * set, and second if we are expanding the graph, the
   * recursive level must be greater than zero.
   * @return <code>true</code> if they should, <code>false</code> otherwise
   */
  public boolean readContextRelationships() {
    return read_context_relationships &&
        (!expand_graph || recursive_level > 0);
  }

  /**
   * Sets the flag indicating whether or not context relationships
   * should be read.
   * @param read_context_relationships A <code>boolean</code>.
   */
  public void setReadContextRelationships(boolean read_context_relationships) {
    this.read_context_relationships = read_context_relationships;
  }

  /**
       * Determines whether or not to read the given language when reading a concept.
   * @param lat A language to read.
   * @return <code>true</code> if they should, <code>false</code> otherwise
   */
  public boolean readLanguage(String lat) {
    // Check and return false if it is excluded mode
    if (!include_or_exclude) {
      for (int i = 0; i < selected_languages.length; i++) {
        if (selected_languages[i].equals(lat)) {
          return false;
        }
      }
      return true;
    }
    // Check and return true if it is included mode
    if (include_or_exclude) {
      for (int i = 0; i < selected_languages.length; i++) {
        if (selected_languages[i].equals(lat)) {
          return true;
        }
      }
      return false;
    }
    return true;
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
    include_or_exclude = false;
  }

  /**
   * Sets any languages to include when reading a concept.
   * @param lats languages to include.
   */
  public void setReadLanguagesToInclude(String[] lats) {
    this.selected_languages = lats;
    include_or_exclude = true;
  }

  /**
   * Indicates whether or not index entries for atoms should be
   * read when reading a concept.
   * @return <code>true</code> if they should be, <code>false</code>
   *         otherwise
   */
  public boolean readIndexEntries() {
    return read_index_entries;
  }

  /**
   * Sets the flag indicating whether or not index
   * entries for atoms should be read.
   * @param read_index_entries A <code>boolean</code>.
   */
  public void setReadIndexEntries(boolean read_index_entries) {
    this.read_index_entries = read_index_entries;
  }

  /**
   * Indicates whether or not relationships should be read
   * when reading a concept.  First, the flag must be on
   * and second, if we are expanding the graph, the recursive
   * level must be  greater than zero.
   * @return <code>true</code> if they should be, <code>false</code>
   */
  public boolean readRelationships() {
    return read_relationships &&
        (!expand_graph || recursive_level > 0);
  }

  /**
   * Sets the flag indicating whether or not relationships
   * should be read
   * @param read_relationships A <code>boolean</code>.
   */
  public void setReadRelationships(boolean read_relationships) {
    this.read_relationships = read_relationships;
  }

  /**
   * Indicates that related atom names for relationships
   * should be read when reading a concept.
   * Three effects:
   * <ol><li>
   *       Preferred atoms of related concepts are
   *       read for regular relationshipss</li>
   *     <li>Related atoms are read for all context relationships<li>
   *     <li>Related atoms are read for all lexical relationships<li>
   * </ol>
   * @return <code>true</code> if they should be expanded,
   *         <code>false</code> otherwise
   * relationships.
   */
  public boolean readRelationshipNames() {
    return read_relationship_names;
  }

  /**
   * Sets the flag indicating whether or not atom names for
   * relationships should be read.
   *
   * @param read_relationship_names A <code>boolean</code>.
   * of expand relationships.
   */
  public void setReadRelationshipNames(boolean read_relationship_names) {
    this.read_relationship_names = read_relationship_names;
  }

  /**
   * Indicates whether or not dead table should be read.
   * @return <code>true</code> if they should be, <code>false</code>
   */
  public boolean readDeleted() {
    return read_deleted;
  }

  /**
   * Sets the flag indicating whether or not dead table
   * should be read.
   *
   * @param read_deleted A <code>boolean</code>.
   * of dead table.
   */
  public void setReadDeleted(boolean read_deleted) {
    this.read_deleted = read_deleted;
  }

  /**
   * Indicates whether or not co-ocurrence data should be read
   * when reading a concept.
   * @return <code>true</code> if it should, <code>false</code> otherwise
   */
  public boolean readCoOcurrenceData() {
    return read_coc_data;
  }

  /**
   * Sets the flag indicating wheter or not co-ocurrence data should be read.
   * @param read_coc_data A <code>boolean</code>.
   */
  public void setReadCoOcurrenceData(boolean read_coc_data) {
    this.read_coc_data = read_coc_data;
  }

  /**
   * Indicates whether or not {@link AtomMapper}s should be
   * used when creating new atom objects.  This allows
   * source-specific {@link Atom} subclasses to be used
   * instead of the generic {@link Atom} class.
   * @return <code>true</code> if atom mappers should be used,
   *         <code>false</code> otherwise
   */
  public boolean expandSourceAtoms() {
    return mapDataType(Atom.class);
  }

  /**
   * Sets the flag indicating whether or not {@link AtomMapper}s
   * should be used for building atoms.
   * @param expand_source_atoms A <code>boolean</code>.
   */
  public void setExpandSourceAtoms(boolean expand_source_atoms) {
    setMapDataType(Atom.class, expand_source_atoms);
  }

  /**
   * Indicates whether or not reading an atom or concept
   * should result in the recursive reading of an atom
   * or concept graph.
   *
   * The depth of recursion would be controlled by
   * the recursive level.  At the lowest recursive level
   * any data that would extend the graph (e.g. relationships
   * or context relationships) is not read in.  This
   * is controlled by methods like {@link #readRelationships()}.
   *
   * @return <code>true</code> if atoms/concepts should be recursively read,
   *         <code>false</code> otherwise
   */
  public boolean expandGraph() {
    return expand_graph && recursive_level > 0;
  }

  /**
   * Sets the flag indicating whether or not reading an
   * atom or concept should recursively read a graph.
   * @param expand_graph A <code>boolean</code>.
   */
  public void setExpandGraph(boolean expand_graph) {
    this.expand_graph = expand_graph;
    if (expand_graph) {
      this.graph_cache = new HashMap();
    } else {
      this.graph_cache = null;
    }
  }

  /**
   * Returns how many levels of recursion are left
   * when reading a graph.
   * @return the recursive levels
   */
  public int getRecursiveLevel() {
    return recursive_level;
  }

  /**
   * Sets the number of levels of recursion are left
   * when reading a graph.
   * @param recursive_level the recursive level
   */
  public void setRecursiveLevel(int recursive_level) {
    this.recursive_level = recursive_level;
    this.top_recursive_level = recursive_level;
  }

  /**
   * Increments the recursive level.
   */
  protected void incrementRecursiveLevel() {
    this.recursive_level++;
  }

  /**
   * Decrements the recursive level.
   */
  protected void decrementRecursiveLevel() {
    this.recursive_level--;
  }

  /**
   * Indicates whether or not this ticket
   * is currently operating in a top-level
   * call to read a graph.  It is important
   * because this determines whether or not the
   * cache should be cleared at the end
   * of the call or not.
   * @return <code>true</code> if this is a top-level ticket,
   *         <code>false</code> otherwise
   */
  protected boolean isTopRecursiveLevel() {
    return recursive_level == top_recursive_level;
  }

  /**
   * Returns contents of the graph cache for a key.
   * @param key An {@link Object} key.
   * @return contents of the graph cache for a key.
   */
  protected Object get(Object key) {
    return (graph_cache == null) ? null : graph_cache.get(key);
  }

  /**
   * Adds an element to the graph cache.
   * @param key An {@link Object} key.
   * @param value An {@link Object} value.
   */
  protected void put(Object key, Object value) {
    if (graph_cache != null) {
      graph_cache.put(key, value);
    }
  }

  /**
   * Empties the graph cache.
   */
  protected void clearCache() {
    graph_cache.clear();
  }

  /**
   * Maps data type.
   * @param c the {@link Class} represents hashmap key.
   * @return <code>true</code> if data type exist,
   *         <code>false</code> otherwise
   */
  public boolean mapDataType(Class c) {
    if (map_data_type_map.containsKey(c)) {
      return ( (Boolean) map_data_type_map.get(c)).booleanValue();
    } else {
      return false;
    }
  }

  /**
   * Initializes map data type.
   * @param c the {@link Class} represents hashmap key.
   * @param b represents hashmap value.
   */
  public void setMapDataType(Class c, boolean b) {
    map_data_type_map.put(c, new Boolean(b));
  }

  /**
   * Returns data mappers.
   * @param c the {@link Class} represents hashmap key.
   * @return the {@link List}.
   */
  public List getDataMapper(Class c) {
    if (data_mappers_map.containsKey(c)) {
      return new ArrayList( (List) data_mappers_map.get(c));
    } else {
      return new ArrayList();
    }
  }

  /**
   * Adds data mapper.
   * @param c the {@link Class} represents hashmap key.
   * @param mapper the {@link Object} represents hashmap value.
   */
  public void addDataMapper(Class c, Object mapper) {
    if (data_mappers_map.containsKey(c)) {
      ( (List) data_mappers_map.get(c)).add(mapper);
    } else {
      List mappers = new ArrayList();
      mappers.add(mapper);
      data_mappers_map.put(c, mappers);
    }
  }

  /**
   * Clears all data mappers.
   */
  public void clearDataMappers() {
    if (data_mappers_map != null) {
      data_mappers_map.clear();
    }
  }

  /**
   * Clears data mappers for specified type
   * @param c the {@link Class} to remove mappers for
   */
  public void clearDataMappers(Class c) {
    if (data_mappers_map.containsKey(c)) {
      data_mappers_map.remove(c);
    }
  }

  /**
   * Removes the specified for the specified type data mapper.
   * @param c the {@link Class} type
   * @param mapper the mapper to remove
   */
  public void removeDataMapper(Class c, Object mapper) {
    if (data_mappers_map.containsKey(c)) {
      ( (List) data_mappers_map.get(c)).remove(mapper);
    }
  }

  //
  // Accessor methods for other info
  //

  /**
   * Returns the work identifier.
   * @return An {@link Identifier}
   */
  public Identifier getWorkIdentifier() {
    return work_id;
  }

  /**
   * Sets the work identifier.
   * @param work_id An  {@link Identifier}
   */
  public void setWorkIdentifier(Identifier work_id) {
    this.work_id = work_id;
  }

  /**
   * Returns the authority.
   * @return the {@link Authority}.
   */
  public Authority getAuthority() {
    return authority;
  }

  /**
   * Sets the authority.
   * @param authority the {@link Authority}.
   */
  public void setAuthority(Authority authority) {
    this.authority = authority;
  }

  /**
   * Returns the transaction Identifier.
   * @return An {@link Identifier}.
   */
  public Identifier getTransactionIdentifier() {
    return transaction_id;
  }

  /**
   * Sets the transaction identifier.
   * @param transaction_id An {@link Identifier}
   */
  public void setTransactionIdentifier(Identifier transaction_id) {
    this.transaction_id = transaction_id;
  }

  /**
   * Sets the {@link EnforcableIntegrityVector}
   * @param vector the {@link EnforcableIntegrityVector}
   */
  public void setIntegrityVector(EnforcableIntegrityVector vector) {
    this.vector = vector;
  }

  /**
   * Returns the vector used with this ticket.
   * @return the <code>IntegrityVector</code> to used by this ticket
   */
  public EnforcableIntegrityVector getIntegrityVector() {
    return vector;
  }

  /**
   * Sets the override vector used by the application.
   * @param vector the override {@link IntegrityVector}
   */
  public void setOverrideVector(IntegrityVector vector) {
    this.override_vector = vector;
  }

  /**
   * Returns the vector used with this ticket.
   * @return the <code>IntegrityVector</code> to used by this ticket
   */
  public IntegrityVector getOverrideVector() {
    return override_vector;
  }

  /**
   * Sets the violations vector used by the application.
   * @param vector the {@link ViolationsVector}
   */
  public void setViolationsVector(ViolationsVector vector) {
    this.violations_vector = vector;
  }

  /**
   * Returns the vector used with this ticket.
   * @return the <code>ViolationsVector</code> to used by this ticket
   */
  public ViolationsVector getViolationsVector() {
    return violations_vector;
  }

  /**
   * Sets the name of the application using this ticket.
   * This is eventually used to obtain an integrity vector.
   * @param application The name of the application using this ticket
   */
  public void setApplication(String application) {
    this.application = application;
  }

  /**
   * Returns the name of the application using this ticket.
   * @return the name of the application using this ticket
   */
  public String getApplication() {
    return application;
  }

  /**
   * Sets the level of the editor using this ticket.
   * This is eventually used to obtain an override vector.
   * @param level The level of the editor using this ticket
   */
  public void setEditorLevel(int level) {
    this.editor_level = level;
  }

  /**
   * Returns the level of the editor using this ticket.
   * @return the level of the editor using this ticket
   */
  public int getEditorLevel() {
    return editor_level;
  }

  /**
   * Returns a ticket for the actions.
   * @return a ticket for the actions.
   */
  public static Ticket getActionsTicket() {
    Ticket ticket = new Ticket();

    // Do we need these for integrity checks?
    //ticket.setExpandLongAttributes(true);
    //ticket.setReadContextRelationships(true);

    // We need to read core data
    ticket.setReadAttributes(true);

    ticket.setReadAtoms(true);
    ticket.setReadConcept(true);
    ticket.setReadRelationships(true);

    // We need to read actions to
    // get concept's last action
    ticket.setReadActions(true);

    // We need to expand source atoms for MSH integrity constraints
    ticket.setMapDataType(Atom.class, true);
    ticket.addDataMapper(Atom.class, new MeshEntryTermMapper());

    // We need this for MGV_M (at least)
    ticket.setReadAtomNames(true);

    // Actions need all atoms to be present
    ticket.setReadLanguagesToExclude(new String[0]);

    // Do we need to calculate preferred atoms?
    ticket.setCalculatePreferredAtom(true);

    return ticket;
  }

  /**
   * Returns an empty ticket.
   * @return an empty ticket
   */
  public static Ticket getEmptyTicket() {
    Ticket ticket = new Ticket();
    return ticket;
  }

  /**
   * Returns a ticket for the mappings.
   * @return a ticket for the mappings.
   */
  public static Ticket getMappingTicket() {
    Ticket ticket = new Ticket();

    // Do we need these for integrity checks?
    ticket.setExpandLongAttributes(true);
    //ticket.setReadContextRelationships(true);

    // We need to read core data
    ticket.setReadAttributes(true);
    ticket.setReadAtoms(true);
    ticket.setReadConcept(true);
    ticket.setReadRelationships(false);

    // We need to read actions to
    // get concept's last action
    ticket.setReadActions(false);

    // We need this for MGV_M (at least)
    ticket.setReadAtomNames(true);

    // Do we need to calculate preferred atoms?
    ticket.setCalculatePreferredAtom(true);

    // Use mapping mapper
    ticket.addDataMapper(Attribute.class, new MappingMapper());
    ticket.setMapDataType(Attribute.class, true);

    return ticket;
  }

  /**
   * Returns a ticket used to generate concept reports.
   * @return a ticket used to generate concept reports.
   */
  public static Ticket getReportsTicket() {
    Ticket ticket = new Ticket();

    // Set ticket for concepts
    ticket.setReadConcept(true);
    ticket.setCalculatePreferredAtom(true);
    ticket.setReadAtoms(true);
    ticket.setReadAtomNames(true);
    ticket.setReadLanguagesToExclude(new String[0]);
    ticket.setCheckAtomAmbiguity(true);
    ticket.setMapDataType(Atom.class, true);
    ticket.addDataMapper(Atom.class, new MeshEntryTermMapper());

    // Set ticket for attributes
    ticket.setReadAttributes(true);
    ticket.setExpandLongAttributes(true);
    // IGNORE XMAP attributes
    ticket.setDataTypeRestriction(Attribute.class,
        "AND attribute_name NOT IN ('XMAP', 'XMAPTO', 'XMAPFROM')");

    ticket.setReadRelationships(true);
    ticket.setReadRelationshipNames(true);

    ticket.setReadContextRelationships(true);

    // Set ticket for read actions
    ticket.setReadActions(true);

    return ticket;
  }

  /**
   * The main method performs a self-QA test
   * @param args An array of strings.
   */
  public static void main(String[] args) {

    try {
      MEMEToolkit.initialize(null, null);
    } catch (InitializationException ie) {
      MEMEToolkit.handleError(ie);
    }
    MEMEToolkit.setProperty(MEMEConstants.DEBUG, "true");

    //
    // Main Header
    //

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Starting test of Ticket ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing Ticket-";
    String test_result = null;

    // Create a Ticket object to work with
    Ticket ticket = new Ticket();

    //
    // setReadAtomNames, readAtomNames
    //

    ticket.setReadAtomNames(true);
    if (ticket.readAtomNames()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/readAtomNames() = " + ticket.readAtomNames());

    //
    // setReadAttributes, readAttributes
    //

    ticket.setReadAttributes(true);
    if (ticket.readAttributes()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/readAttributes() = " + ticket.readAttributes());

    //
    // setExpandLongAttributes, expandLongAttributes
    //

    ticket.setExpandLongAttributes(true);
    if (ticket.expandLongAttributes()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/expandLongAttributes() = " +
                      ticket.expandLongAttributes());

    //
    // setReadAtoms, readAtoms
    //

    ticket.setReadAtoms(true);
    if (ticket.readAtoms()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/readAtoms() = " + ticket.readAtoms());

    //
    // setConcept, readConcept
    //

    ticket.setReadConcept(true);
    if (ticket.readConcept()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/readConcept() = " + ticket.readConcept());

    //
    // setSetPreferredAtomID, setPreferredAtomId
    //

    ticket.setCalculatePreferredAtom(true);
    if (ticket.calculatePreferredAtom()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "calculatePreferredAtom() = " +
                      ticket.calculatePreferredAtom());

    //
    // setContextRelationships, readContextRelationships
    //

    ticket.setReadContextRelationships(true);
    if (ticket.readContextRelationships()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/readContextRelationships() = " +
                      ticket.readContextRelationships());

    //
    // setForeignAtoms, readForeignAtoms
    //

    ticket.setReadLanguagesToExclude(new String[0]);
    if (ticket.readLanguage("ENG")) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/readNonEnglishAtoms() = " +
                      ticket.readLanguage("ENG"));

    //
    // setIndexEntries, readIndexEntries
    //

    ticket.setReadIndexEntries(true);
    if (ticket.readIndexEntries()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/readIndexEntries() = " + ticket.readIndexEntries());

    //
    // setRelationships, readRelationships
    //

    ticket.setReadRelationships(true);
    if (ticket.readRelationships()) {
      test_result = " PASSED: ";
    } else {
      test_result = " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
                      "set/readRelationships() = " + ticket.readRelationships());

    //
    // Main Footer
    //

    MEMEToolkit.trace("");

    if (failed) {
      MEMEToolkit.trace("AT LEAST ONE TEST DID NOT COMPLETE SUCCESSFULLY");
    } else {
      MEMEToolkit.trace("ALL TESTS PASSED");

    }
    MEMEToolkit.trace("");

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Finished test of Ticket ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
