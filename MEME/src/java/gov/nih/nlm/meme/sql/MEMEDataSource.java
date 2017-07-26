/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MEMEDataSource
 * Version Information
 * 10/30/2007 TTN (1-FN4GD): add paginated feature to getContentViewMembers method 
 * 04/17/2007 BAC (1-E0JWB): New getNullLui() method.
 * 03/06/2007 3.12.4 SL (1-DNO15) : Adding a new interface findNDCConceptsFromCode to retrieve the NDC Code.
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.AtomicAction;
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
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.LUI;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.LoggedError;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.common.MetaCode;
import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.RUI;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.ReflectionException;
import oracle.sql.CLOB;

import java.io.BufferedReader;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;

/**
 * Represents a connection to a source of MEME data.  Typically a MID or MRD.
 *
 * @author MEME Group
 */
public interface MEMEDataSource extends Connection {

  //
  // Constants
  //

  public static final int STRINGTAB_MAX_ROW_LENGTH = 1786;

  //
  // EnhancedConnection interface
  //

  /**
   * Restores the sort data area size for this connection.
   * @throws DataSourceException if failed to restore sort area size.
   */
  public void restoreSortAreaSize() throws DataSourceException;

  /**
   * Sets the sort data area size for this connection.
   * @param sort_area_size the area size to be sorted.
   * @throws DataSourceException if failed to set sort area size.
   */
  public void setSortAreaSize(int sort_area_size) throws DataSourceException;

  /**
   * Restores the hash data area size for this connection.
   * @throws DataSourceException if failed to restore hash area size.
   */
  public void restoreHashAreaSize() throws DataSourceException;

  /**
   * Sets the hash data area size for this connection.
   * @param hash_area_size the area size to be hashed.
   * @throws DataSourceException if failed to set hash area size.
   */
  public void setHashAreaSize(int hash_area_size) throws DataSourceException;

  /**
   * Restores the auto commit mode fom the stack.
   * Should not be used more times than the setAutoCommitMode method.
   * @throws DataSourceException if failed to restore auto commit.
   */
  public void restoreAutoCommit() throws DataSourceException;

  /**
   * Resets the autocommit mode to the mode at the start of the program
   * and clears the commit mode stack.
   * @throws DataSourceException if failed to reset auto commit.
   */
  public void resetAutoCommit() throws DataSourceException;

  //
  // CLOB functions
  //

  /**
   * Given a CLOB object, this method returns a String representation
   * of it.
   * @param clob the {@link CLOB}.
   * @return the {@link String} containing data from the CLOB.
   * @throws DataSourceException if failed to convert clob to string.
   */
  public String getClobAsString(CLOB clob) throws DataSourceException;

  /**
   * Given a CLOB object, this method returns a BufferedReader allowing
   * access to it.
   * @param clob the {@link CLOB}.
   * @return the {@link BufferedReader} containing CLOB data.
   * @throws DataSourceException if failed to convert clob to buffer reader.
   */
  public BufferedReader getClobAsReader(CLOB clob) throws DataSourceException;

  /**
   * Given a CLOB object, this method writes the String data to the
   * clob.
   * @param clob the {@link CLOB} field (typically EMPTY_CLOB()).
   * @param data the {@link String} to write to the clob.
   * @throws DataSourceException if failed to write data into clob.
   */
  public void setClob(CLOB clob, String data) throws DataSourceException;

  //
  // Buffer functions
  //

  /**
   * Enables the buffer to the default size.
   * @throws DataSourceException if failed to enable buffer.
   */
  public void enableBuffer() throws DataSourceException;

  /**
   * Enables the buffer to the specified size.
   * @param size buffer size
   * @throws DataSourceException if failed to enable buffer.
   */
  public void enableBuffer(int size) throws DataSourceException;

  /**
   * Flushes the DataSource buffer to MEMEToolkit.trace().
   * @return the {@link String} representation of buffer string.
   * @throws DataSourceException if failed to flush the buffer.
   */
  public String flushBuffer() throws DataSourceException;

  /**
   * Determines if session is still active.
   * @return <code>true</code> if session is still active; <code>false</code>
   * otherwise.
   */
  public boolean isConnected();

  //
  // Data Source Methods
  //

  /**
   * Determines if cache is loaded.
   * @return <code>true</code> if cache is loaded; <code>false</code> otherwise
   */
  public boolean isCacheLoaded();

  /**
   * Clears all of the caches.
   * @throws DataSourceException if failed to refresh caches.
   * @throws BadValueException if failed due to invalid data value.
   * @throws ReflectionException if failed to load or instantiate a class.
   */
  public void refreshCaches() throws DataSourceException, BadValueException,
      ReflectionException;

  /**
   * Returns an atom object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param atom_id the atom id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtom(int atom_id, Ticket ticket) throws DataSourceException,
      BadValueException;

  /**
   * Returns an atom object.  The contents of the object
   * are subject to the {@link AUI} and {@link Ticket} parameters.
   * @param aui the {@link AUI}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtom(AUI aui, Ticket ticket) throws DataSourceException,
      BadValueException;

  /**
   * Returns an atom object with the atom name set.
   * @param atom_id the atom id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtomWithName(int atom_id, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns an atom object with the atom name set.
   * @param code the {@link Code}.
   * @param termgroup the {@link Termgroup}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtomWithName(Code code, Termgroup termgroup, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns an atom object with the atom name set.
   * @param aui the {@link AUI}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getAtomWithName(AUI aui, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns the preferred atom of the specified concept.
   * @param concept A {@link Concept}.
   * @return the preferred {@link Atom} of the {@link Concept}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getPreferredAtom(Concept concept) throws DataSourceException,
      BadValueException;

  /**
   * Returns an authority object.
   * @param authority the {@link String} representation of authority.
   * @return the {@link Authority}.
   */
  public Authority getAuthority(String authority);

  /**
   * Returns an attribute object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param rel_id the attribute id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Attribute}.
   * @throws DataSourceException if failed to load attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Attribute getAttribute(int rel_id, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns an attribute object.  The contents of the object
   * are subject to the {@link ATUI} and {@link Ticket} parameters.
   * @param atui the {@link ATUI}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Attribute}.
   * @throws DataSourceException if failed to load attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Attribute getAttribute(ATUI atui, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns a relationship object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param rel_id the relationship id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Relationship getRelationship(int rel_id, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns the inverse relationship object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param rel_id the relationship id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Relationship getInverseRelationship(int rel_id, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns a relationship object.  The contents of the object
   * are subject to the {@link RUI} parameters.
   * @param rui the {@link RUI}.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Relationship getRelationship(RUI rui) throws DataSourceException,
      BadValueException;

  /**
   * Returns a list of relationship names.
   * @return An array of object {@link String} representation of
   * relationship names.
   */
  public String[] getRelationshipNames();

  /**
   * Adds a relationship attribute to the list of relationship attributes.
   * @param rela the {@link String} representation of relationship attribute.
       * @param inverse the {@link String} representation of the inverse relationship
   * attribute of <code>rela</code>.
   * @param rank the rank
   * @throws DataSourceException if failed to add relationship attribute.
   */
  public void addRelationshipAttribute(String rela, String inverse, int rank) throws
      DataSourceException;

  /**
   * Removes a relationship attribute from the list of relationship attributes.
   * @param rela the {@link String} representation of relationship attribute.
   * @throws DataSourceException if failed to remove relationship attribute.
   */
  public void removeRelationshipAttribute(String rela) throws
      DataSourceException;

  /**
   * Adds a relationship attribute to the list of relationship attributes.
   * @param name The relationship name
   * @param inverse_name The relationship inverse name
   * @param weak The <code>boolean</code> represents weak flag
   * @param long_name The relationship long name
   * @param release_name The relationship release name
   * @param inverse_long_name The inverse relationship long name
   * @param inverse_release_name The inverse relationship release name
   * @throws DataSourceException if failed to add relationship name
   */
  public void addRelationshipName(String name, String inverse_name,
                                  boolean weak,
                                  String long_name, String inverse_long_name,
                                  String release_name,
                                  String inverse_release_name) throws
      DataSourceException;

  /**
   * Removes a relationship name.
   * @param name The relationship name
   * @throws DataSourceException if failed to remove relationship name.
   */
  public void removeRelationshipName(String name) throws DataSourceException;

  /**
   * Returns a list of relationship attributes.
   * @return An array of object {@link String} representation of
   * relationship attributes.
   */
  public String[] getRelationshipAttributes();

  /**
   * Looks up the inverse of a relationship name.
   * @param rel the {@link String} representation of relationship name.
   * @return the {@link String} representation of the inverse of the
   * relationship name.
   * @throws BadValueException if failed due to invalid data value.
   */
  public String getInverseRelationshipName(String rel) throws BadValueException;

  /**
   * Looks up the inverse of a relationship attribute.
   * @param rela the {@link String} representation of relationship attribute.
   * @return the {@link String} representation of the inverse of the
   * relationship attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public String getInverseRelationshipAttribute(String rela) throws
      BadValueException;

  /**
   * Counts the relationship for a particular concept.
   * @param concept the {@link Concept}.
   * @return the relationships count.
   * @throws DataSourceException if failed to count relationships.
   */
  public int getRelationshipCount(Concept concept) throws DataSourceException;

  /**
   * Returns a context relationship object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param rel_id the relationship id.
   * @param ticket the {@link Ticket}.
   * @return the {@link ContextRelationship}.
   * @throws DataSourceException if failed to load context relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextRelationship getContextRelationship(int rel_id, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns a context relationship object.  The contents of the object
   * are subject to the {@link RUI} parameters.
   * @param rui the {@link RUI}.
   * @return the {@link ContextRelationship}.
   * @throws DataSourceException if failed to load context relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextRelationship getContextRelationship(RUI rui) throws
      DataSourceException, BadValueException;

  /**
   * Counts the context relationship for a particular concept.
   * @param concept the {@link Concept}.
   * @return the context relationships count.
   * @throws DataSourceException if failed to count context relationship.
   */
  public int getContextRelationshipCount(Concept concept) throws
      DataSourceException;

  /**
   * Returns the context path.
   * @param relationship_id the
   * relationship id.
   * @return the {@link ContextPath}.
   * @throws DataSourceException if failed to get context path.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextPath getContextPath(int relationship_id) throws
      DataSourceException, BadValueException;

  /**
   * Returns the context path.
   * @param rui the {@link RUI}.
   * @return the {@link ContextPath}.
   * @throws DataSourceException if failed to get context path.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextPath getContextPath(RUI rui) throws DataSourceException,
      BadValueException;

  /**
   * Adds a source object.
   * @param source the {@link Source} to add
   * @throws DataSourceException if failed to add source
   */
  public void addSource(Source source) throws DataSourceException;

  /**
   * Adds multiple source object.
   * @param sources An array of object {@link Source} to add
   * @throws DataSourceException if failed to add sources
   */
  public void addSources(Source[] sources) throws DataSourceException;

  /**
   * Removes a source object.
   * @param source the {@link Source} to remove
   * @throws DataSourceException if failed to remove source
   */
  public void removeSource(Source source) throws DataSourceException;

  /**
   * Sets the source.
   * @param source the {@link Source}
   * @throws DataSourceException if failed to set the source
   */
  public void setSource(Source source) throws DataSourceException;

  /**
   * Returns a source object.
   * @param source_rank the {@link String} representation of source rank.
   * @return the {@link Source}.
   * @throws DataSourceException if failed to load source.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Source getSource(String source_rank) throws DataSourceException,
      BadValueException;

  /**
   * Returns a list of source object.
   * @return An array of object {@link  Source}.
   */
  public Source[] getSources();

  /**
   * Adds a termgroup object.
   * @param termgroup the {@link Termgroup} to add
   * @throws DataSourceException if failed to add termgroup
   * @throws BadValueException if failed due to invalid data value.
   */
  public void addTermgroup(Termgroup termgroup) throws DataSourceException,
      BadValueException;

  /**
   * Adds a multiple termgroup object.
   * @param termgroups An array of {@link Termgroup} to add
   * @throws DataSourceException if failed to add termgroups
   * @throws BadValueException if failed due to invalid data value.
   */
  public void addTermgroups(Termgroup[] termgroups) throws DataSourceException,
      BadValueException;

  /**
   * Removes a termgroup object.
   * @param termgroup the {@link Termgroup} to remove
   * @throws DataSourceException if failed to remove termgroup
   * @throws BadValueException if failed due to invalid data value.
   */
  public void removeTermgroup(Termgroup termgroup) throws DataSourceException,
      BadValueException;

  /**
   * Sets the Termgroup.
   * @param termgroup the {@link Termgroup}
   * @throws DataSourceException if failed to set the termgroup
   * @throws BadValueException if failed due to invalid data value.
   */
  public void setTermgroup(Termgroup termgroup) throws DataSourceException,
      BadValueException;

  /**
   * Returns a termgroup object.
   * @param termgroup_rank the {@link String} representation of termgroup rank.
   * @return the {@link Termgroup}.
   * @throws DataSourceException if failed to get termgroup.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Termgroup getTermgroup(String termgroup_rank) throws
      DataSourceException, BadValueException;

  /**
   * Returns a list of termgroup object.
   * @return An array of object {@link Termgroup}.
   */
  public Termgroup[] getTermgroups();

  /**
   * Adds a semantic type object.
   * @param sty the {@link SemanticType} to add
   * @throws DataSourceException if failed to add semantic type
   */
  public void addValidSemanticType(SemanticType sty) throws DataSourceException;

  /**
   * Removes a semantic type object.
   * @param sty the {@link SemanticType} to remove
   * @throws DataSourceException if failed to remove semantic type
   */
  public void removeValidSemanticType(SemanticType sty) throws
      DataSourceException;

  /**
   * Returns a semantic type object.
   * @param sty the {@link String} representation of semantic type.
   * @return the {@link SemanticType}.
   * @throws BadValueException if failed due to invalid data value.
   */
  public SemanticType getSemanticType(String sty) throws BadValueException;

  /**
   * Returns a list of semantic type object.
   * @return An array of object {@link SemanticType}.
   */
  public SemanticType[] getValidSemanticTypes();

  /**
   * Adds a language object.
   * @param language the {@link String} to add
   * @throws DataSourceException if failed to add language
   */
  public void addLanguage(Language language) throws DataSourceException;

  /**
   * Removes a language object.
   * @param language the {@link String} to remove
   * @throws DataSourceException if failed to remove language
   */
  public void removeLanguage(Language language) throws DataSourceException;

  /**
   * Sets the language object.
   * @param language the {@link String} to set
   * @throws DataSourceException if failed to set language
   */
  public void setLanguage(Language language) throws DataSourceException;

  /**
   * Returns a language object.
   * @param lat the {@link String} representation of language.
   * @return the {@link String} representation of language.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Language getLanguage(String lat) throws BadValueException;

  /**
   * Returns a list of language object.
   * @return An array of object {@link Language}.
   */
  public Language[] getLanguages();

  /**
   * Returns a code from code map according to its type and value.
   * @param type the {@link String} representation of type.
   * @param value the {@link String} representation of value.
   * @return the {@link String} representation of code from code map.
   * @throws BadValueException if failed due to invalid data value.
   */
  public String getCodeByValue(String type, String value) throws
      BadValueException;

  /**
   * Returns a value from code map according to its type and code.
   * @param type the {@link String} representation of type.
   * @param code the {@link String} representation of code.
   * @return the {@link String} representation of value from code map.
   * @throws BadValueException if failed due to invalid data value.
   */
  public String getValueByCode(String type, String code) throws
      BadValueException;

  /**
   * Lock concept and potentially related concepts as well.
   * @param c the concept to lock
   * @param lock_related flag indicating whether or not to lock
   * related concepts
   * @throws DataSourceException if failed to lock concept
   */
  public void lockConcept(Concept c, boolean lock_related) throws
      DataSourceException;

  /**
   * Lock concepts and potentially related concepts as well.
   * @param s a source object {@link Concept}
   * @param t a target object {@link Concept}
   * @param lock_related flag indicating whether or not to lock
   * related concepts
   * @throws DataSourceException if failed to lock concept
   */
  public void lockConcepts(Concept s, Concept t, boolean lock_related) throws
      DataSourceException;

  /**
   * Unlock concepts and potentially related concepts as well.
   * @throws DataSourceException if failed to unlock concept
   */
  public void unlockConcepts() throws DataSourceException;

  /**
   * Returns a concept object.
   * @param concept_id the concept id.
   * @param ticket the {@link Ticket}.
   * @return the {@link Concept}.
   * @throws DataSourceException if failed to look up concept.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Concept getConcept(int concept_id, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns a concept object.
   * @param cui the {@link CUI}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Concept}.
   * @throws DataSourceException if failed to look up concept.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Concept getConcept(CUI cui, Ticket ticket) throws DataSourceException,
      BadValueException;

  /**
   * Inserts a new concept mapping.
   * @param cm the {@link ConceptMapping}.
   * @throws DataSourceException if failed to add concept mapping
   */
  public void addConceptMapping(ConceptMapping cm) throws DataSourceException;

  /**
   * Removes a concept mapping.
   * @param cm the {@link ConceptMapping}
   * @throws DataSourceException if failed to remove concept mapping
   */
  public void removeConceptMapping(ConceptMapping cm) throws
      DataSourceException;

  /**
   * Returns a concept mapping object.
   * @param id the map id
   * @return an array of {@link ConceptMapping}
   * @throws BadValueException if failed due to invalid data value
   * @throws DataSourceException if failed to look up concept
   */
  public ConceptMapping getConceptMapping(int id) throws
      DataSourceException, BadValueException;

  /**
   * Returns a concept mapping object.
   * @param ticket the {@link Ticket}
   * @return an array of {@link ConceptMapping}
   * @throws BadValueException if failed due to invalid data value
   * @throws DataSourceException if failed to look up concept
   */
  public ConceptMapping[] getConceptMappings(Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns a concept mapping object.
   * @param concept the {@link Concept}
   * @param ticket the {@link Ticket}
   * @return an array of {@link ConceptMapping}
   * @throws BadValueException if failed due to invalid data value
   * @throws DataSourceException if failed to look up concept
   */
  public ConceptMapping[] getConceptMappings(Concept concept, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns an array of map set object.
   * @return An array of object {@link MapSet}.
   * @throws DataSourceException if failed to look up map set.
   * @throws BadValueException if failed due to invalid data value.
   */
  public MapSet[] getMapSets() throws DataSourceException, BadValueException;

  /**
   * Returns a map set object.
   * @param concept_id the concept id.
   * @param ticket the {@link Ticket}.
   * @return the {@link MapSet}.
   * @throws DataSourceException if failed to look up map set.
   * @throws BadValueException if failed due to invalid data value.
   */
  public MapSet getMapSet(int concept_id, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Populates a concept.
   * @param concept the {@link Concept}.
   * @param ticket the {@link Ticket}.
   * @throws DataSourceException if failed to populate concept.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateConcept(Concept concept, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Populates an atom.
   * @param atom the {@link Atom}.
   * @param ticket the {@link Ticket}.
   * @throws DataSourceException if failed to populate atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateAtom(Atom atom, Ticket ticket) throws DataSourceException,
      BadValueException;

  /**
   * Populates an attribute.
   * @param rel the {@link Attribute}.
   * @param ticket the {@link Ticket}.
   * @throws DataSourceException if failed to populate attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateAttribute(Attribute rel, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Populates a relationship.
   * @param rel the {@link Relationship}.
   * @param ticket the {@link Ticket}.
   * @throws DataSourceException if failed to populate relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateRelationship(Relationship rel, Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Populates a context relationship.
   * @param rel the {@link ContextRelationship}.
   * @param ticket the {@link Ticket}.
   * @throws DataSourceException if failed to populate context relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateContextRelationship(ContextRelationship rel,
                                          Ticket ticket) throws
      DataSourceException, BadValueException;

  /**
   * Returns an identifier object.
   * @param c the {@link Class}.
   * @return the {@link Identifier}.
   * @throws DataSourceException if failed to load max tab.
   */
  public Identifier getNextIdentifierForType(Class c) throws
      DataSourceException;

  /**
   * Returns an identifier object.
   * @param c the {@link Class}.
   * @return the {@link Identifier}.
   * @throws DataSourceException if failed to load max tab.
   */
  public Identifier getMaxIdentifierForType(Class c) throws DataSourceException;

  /**
   * Returns an action engine object.
   * @return the {@link ActionEngine}.
   */
  public ActionEngine getActionEngine();

  /**
   * Returns a molecular action object.
   * @param molecule_id the molecule id.
   * @return the {@link MolecularAction}.
   * @throws DataSourceException if failed to load molecular action.
   */
  public MolecularAction getMolecularAction(int molecule_id) throws
      DataSourceException;

  /**
   * Returns a molecular action object with all its atomic actions.
   * @param molecule_id the molecular id.
   * @return the {@link MolecularAction}.
   * @throws DataSourceException if failed to load full molecular action.
   */
  public MolecularAction getFullMolecularAction(int molecule_id) throws
      DataSourceException;

  /**
   * Returns a molecular action with all its last molecular actions.
   * @param concept the {@link Concept}.
   * @return the {@link MolecularAction}.
   * @throws DataSourceException if failed to load last molecular action.
   */
  public MolecularAction getLastMolecularAction(Concept concept) throws
      DataSourceException;

  /**
   * Returns an atomic action object.
   * @param atomic_action_id the atomic action id.
   * @return the {@link AtomicAction}.
   * @throws DataSourceException if failed to load atomic action.
   */
  public AtomicAction getAtomicAction(int atomic_action_id) throws
      DataSourceException;

  /**
   * Returns an logged action.
   * @param action_id the action id.
   * @return the {@link LoggedAction} representation of logged action.
   * @throws DataSourceException if failed to get action.
   */
  public LoggedAction getAction(int action_id) throws DataSourceException;

  /**
   * Returns the count of logged actions.
   * @return the count of logged actions
   * @throws DataSourceException if failed to get count of actions
   */
  public int getActionCount() throws DataSourceException;

  /**
   * Removes an entry from the action log.
   * @param action the {@link LoggedAction} to remove
   * @throws DataSourceException if failed to remove action
   */
  public void removeActionFromLog(LoggedAction action) throws DataSourceException;

  /**
   * Returns a map of inverse relationship name.
   * @return the {@link Map}.
   * @throws DataSourceException if failed to load relationship name.
   */
  public Map getInverseRelationshipNameMap() throws DataSourceException;

  /**
   * Returns a map of inverse relationship attribute.
   * @return the {@link Map}.
   * @throws DataSourceException if failed to load relationship attribute.
   */
  public Map getInverseRelationshipAttributeMap() throws DataSourceException;

  /**
   * Search for actions.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find actions.
   */
  public Iterator findActions(SearchParameter param) throws DataSourceException;

  /**
   * Search for actions.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find actions.
   */
  public Iterator findActions(SearchParameter[] param) throws
      DataSourceException;

  /**
   * Finds a matching string.
   * @param param the {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching string.
   */
  public Iterator findStrings(SearchParameter param) throws DataSourceException;

  
  /**
   * Finds the null LUI.
   * @param param the {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching string.
   */
  public LUI getNullLUI() throws DataSourceException;

  
  /**
   * Finds a matching string.
   * @param params An array of object {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching string.
   */
  public Iterator findStrings(SearchParameter[] params) throws
      DataSourceException;

  /**
   * Finds a matching atoms.
   * @param param the {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching atoms.
   */
  public Iterator findAtoms(SearchParameter param, Ticket ticket) throws
      DataSourceException;

  /**
   * Finds a matching atoms.
   * @param params An array of object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching atoms.
   */
  public Iterator findAtoms(SearchParameter[] params, Ticket ticket) throws
      DataSourceException;

  /**
   * Finds a matching concepts from string.
   * @param param the {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConcepts(SearchParameter param, Ticket ticket) throws
      DataSourceException;

  /**
   * Finds a matching concepts from string.
   * @param params An array object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConcepts(SearchParameter[] params, Ticket ticket) throws
      DataSourceException;

  /**
   * Finds a matching concepts from string.
   * @param param the {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConceptsFromString(SearchParameter param, Ticket ticket) throws
      DataSourceException;

  /**
   * Finds a matching concepts from NDC COde.
   * @param param the {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findNDCConceptsFromCode(String code,Ticket ticket) throws
      DataSourceException;
  /**
   * Finds a matching concepts from string.
   * @param params An array of object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConceptsFromString(SearchParameter[] params,
                                         Ticket ticket) throws
      DataSourceException;

  /**
   * Finds a matching concepts from words.
   * @param param the {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConceptsFromWords(SearchParameter param, Ticket ticket) throws
      DataSourceException;

  /**
   * Finds a matching concepts from words.
   * @param params An array of object {@link SearchParameter}.
   * @param ticket the {@link Ticket}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching concepts.
   */
  public Iterator findConceptsFromWords(SearchParameter[] params, Ticket ticket) throws
      DataSourceException;

  /**
   * Search for molecular actions.
   * @param param A single {@link SearchParameter}
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find molecular actions.
   */
  public Iterator findMolecularActions(SearchParameter param) throws
      DataSourceException;

  /**
   * Search for molecular actions.
   * @param params An array of {@link SearchParameter}s
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find molecular actions.
   */
  public Iterator findMolecularActions(SearchParameter[] params) throws
      DataSourceException;

  /**
   * Finds a matching safe replacement facts.
   * @param param An array of object {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching
   * safe replacement facts.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Iterator findSafeReplacementFacts(SearchParameter param) throws
      DataSourceException, BadValueException;

  /**
   * Finds a matching safe replacement facts.
   * @param params An array of object {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching
   * safe replacement facts.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Iterator findSafeReplacementFacts(SearchParameter[] params) throws
      DataSourceException, BadValueException;

  /**
   * Looks up the CPU mode for an application
       * @param application the {@link String} representation of an application name,
   * can be "".
   * @return the {@link String} representation of CPU mode.
   * @throws DataSourceException if failed to load CPU mode.
   */
  public String getCpuMode(String application) throws DataSourceException;

  /**
   * Returns the user name.
   * @return the {@link String} representation of user name.
   * @throws DataSourceException if failed to get user name.
   */
  public String getUserName() throws DataSourceException;

  /**
       * Returns the {@link gov.nih.nlm.meme.MIDServices} database service name that
   * was used to create this MEMEDataSource.
   * @return the {@link String} representation of DB Service.
   */
  public String getServiceName();

  /**
       * Set the {@link gov.nih.nlm.meme.MIDServices} database service name that will
   * be use to create this MEMEDataSource.
   * @param service the {@link String} representation of service name.
   */
  public void setServiceName(String service);

  /**
   * Returns the {@link MEMEDataSource} name.
   * @return the {@link String} representation of data source name.
   */
  public String getDataSourceName();

  /**
   * Set the {@link MEMEDataSource} name.
   * @param data_source_name the {@link String} representation of data
   * source name.
   */
  public void setDataSourceName(String data_source_name);

  /**
   * Inserts a new content view.
   * @param cv the {@link ContentViewMember}.
   * @throws DataSourceException if failed to add content view
   */
  public void addContentView(ContentView cv) throws DataSourceException;

  /**
   * Removes content view.
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to remove content view
   */
  public void removeContentView(ContentView cv) throws DataSourceException;

  /**
   * Sets content view.
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to set content view
   */
  public void setContentView(ContentView cv) throws DataSourceException;

  /**
   * Gets content view.
   * @param id the content view id
   * @return the {@link ContentView}
   * @throws DataSourceException if failed to get content view
   */
  public ContentView getContentView(int id) throws DataSourceException;

  /**
   * Gets content view.
   * @param id the {@link Identifier}
   * @return the {@link ContentView}
   * @throws DataSourceException if failed to get content view
   */
  public ContentView getContentView(Identifier id) throws DataSourceException;

  /**
   * Gets content views.
   * @return An array of object {@link ContentView}
   * @throws DataSourceException if failed to get content views
   */
  public ContentView[] getContentViews() throws DataSourceException;

  /**
   * Removes a content view members.
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to remove content view members
   */
  public void removeContentViewMembers(ContentView cv) throws
      DataSourceException;

  /**
   * Inserts a content view member.
   * @param member the {@link ContentViewMember}
   * @throws DataSourceException if failed to add content view members
   */
  public void addContentViewMember(ContentViewMember member) throws
      DataSourceException;

  /**
   * Removes a content view member.
   * @param member the {@link ContentViewMember}
   * @throws DataSourceException if failed to remove content view member
   */
  public void removeContentViewMember(ContentViewMember member) throws
      DataSourceException;

  /**
   * Inserts an arrays of content view member.
   * @param members An array of object {@link ContentViewMember}
   * @throws DataSourceException if failed to add content view members
   */
  public void addContentViewMembers(ContentViewMember[] members) throws
      DataSourceException;

  /**
   * Removes an array of content view member.
   * @param members An array of object {@link ContentViewMember}
   * @throws DataSourceException if failed to remove content view members
   */
  public void removeContentViewMembers(ContentViewMember[] members) throws
      DataSourceException;

  /**
   * Generates a content view members.
   * @param cv the {@link ContentView}
   * @throws DataSourceException if failed to generate content view members
   */
  public void generateContentViewMembers(ContentView cv) throws
      DataSourceException;

  /**
   * Gets content view member.
   * @param member the {@link ContentViewMember}
   * @param start the start index
   * @param end the end index
   * @return An array of object {@link ContentViewMember}
   * @throws DataSourceException if failed to get content view members
   */
  public ContentViewMember[] getContentViewMembers(ContentView member, int start, int end) throws
      DataSourceException;

  /**
   * Maps the specified old CUI through the CUI history information in the MID.
   * Returns the best matching current CUI, or <code>null</code>.
   * @param cui the {@link CUI} to map
   * @return the best matching current CUI, or <code>null</code>
   * @throws DataSourceException if anything goes wrong
   */
  public CUI mapCUIThroughHistory(CUI cui) throws DataSourceException;

  /**
   * Maps the specified old CUI through the CUI history information in the MID.
   * Returns the best matching current CUI, or <code>null</code>.
   * @param cui the {@link CUI} to map
       * @param sy_only indicates whether or not to map only through "synonymy facts"
   * @return the best matching current CUI, or <code>null</code>
   * @throws DataSourceException if anything goes wrong
   */
  public CUI mapCUIThroughHistory(CUI cui, boolean sy_only) throws
      DataSourceException;

  /**
   * Maps the specifired {@link NativeIdentifier} to its corresponding {@link CoreData}
   * element.
   * @param id the {@link NativeIdentifier} to map
   * @param ticket a {@link Ticket} for reading the core data, or <code>null</code> if
   *        only dummy objects should be returned.
   * @return the {@link CoreData} corresponding to the native identifier (or <code>null</code>)
   * @throws DataSourceException
   */
  public CoreData mapNativeIdentifier(NativeIdentifier id, Ticket ticket) throws
      DataSourceException;

  /**
   * Adds a a meta code object.
   * @param mcode the metacode
   * @throws DataSourceException if failed to add meta code
   */
  public void addMetaCode(MetaCode mcode) throws DataSourceException;

  /**
   * Removes a a meta code object.
   * @param mcode the metacode
   * @throws DataSourceException if failed to remove meta code
   */
  public void removeMetaCode(MetaCode mcode) throws DataSourceException;

  /**
   * Returns a meta code object.
   * @param code the code
   * @param type the type
   * @return the {@link MetaCode}
   */
  public MetaCode getMetaCode(String code, String type);

  /**
   * Returns a list of meta code types
   * @return An array of representing meta code types
   */
  public String[] getMetaCodeTypes();

  /**
   * Returns a list of meta code object.
   * @param type the type
   * @return An array of object {@link MetaCode}
   */
  public MetaCode[] getMetaCodesByType(String type);

  /**
   * Returns a list of meta codes
   * @return An array of {@link MetaCode}
   */
  public MetaCode[] getMetaCodes();

  /**
   * Adds a meta property object.
   * @param meta_prop the meta property
   * @throws DataSourceException if failed to add meta property
   */
  public void addMetaProperty(MetaProperty meta_prop) throws
      DataSourceException;

  /**
   * Removes a meta property object.
   * @param meta_prop the meta property
   * @throws DataSourceException if failed to remove meta property
   */
  public void removeMetaProperty(MetaProperty meta_prop) throws
      DataSourceException;

  /**
   * Returns a meta property object.
   * @param key the key
   * @param key_qualifier the key qualifier
   * @param value the value
   * @return the {@link MetaProperty}
   */
  public MetaProperty getMetaProperty(String key, String key_qualifier,
                                      String value, String description);//naveen UMLS-60 added description parameter to getMetaProperty method

  /**
   * Returns a list of meta property object.
   * @return an array of object {@link MetaProperty}
   */
  public MetaProperty[] getMetaProperties();

  /**
   * Returns a list of meta property object according to its key qualifier.
   * @param key_qualifier the key qualifier
   * @return an array of object {@link MetaProperty}
   */
  public MetaProperty[] getMetaPropertiesByKeyQualifier(String key_qualifier);

  /**
   * Returns a list of meta property key qualifier.
   * @return a list of meta property key qualifier
   */
  public String[] getMetaPropertyKeyQualifiers();

  /**
   * Assign CUIs.
   * @return the most recent cui assignment log
   * @throws DataSourceException if failed to assign cuis
   */
  public String assignCuis() throws DataSourceException;

  /**
   * Assign CUIs.
   * @param work the {@link WorkLog}
   * @return the most recent cui assignment log
   * @throws DataSourceException if failed to assign cuis
   */
  public String assignCuis(WorkLog work) throws DataSourceException;

  /**
   * Assign CUI's.
   * @param source the {@link Concept}
   * @throws DataSourceException if failed to assign cuis
   */
  public void assignCuis(Concept source) throws DataSourceException;

  /**
   * Assign CUI's.
   * @param source the {@link Concept} represent the source concept
   * @param target the {@link Concept} represent the target concept
   * @throws DataSourceException if failed to assign cuis
   */
  public void assignCuis(Concept source, Concept target) throws
      DataSourceException;

  /**
   * Sets the system status.
   * @param key the key
   * @param value the value
   * @throws DataSourceException if failed to set system status
   */
  public void setSystemStatus(String key, String value) throws
      DataSourceException;

  /**
   * Gets the system status.
   * @param key the key
   * @return the system status
   * @throws DataSourceException if failed to get system status
   */
  public String getSystemStatus(String key) throws DataSourceException;

  /**
   * Creates new worklog.
   * @param authority the {@link String} representation of authority.
   * @param type object {@link String} representation of work type.
   * @param description the {@link String} representation of work description.
   * @return the {@link WorkLog}.
   * @throws DataSourceException if new worklog could not be created.
   */
  public WorkLog newWorkLog(Authority authority, String type,
                            String description) throws DataSourceException;

  /**
   * Returns a work log object.
   * @param work_id the work id.
   * @return the {@link WorkLog}.
   * @throws DataSourceException if failed to load meme work.
   */
  public WorkLog getWorkLog(int work_id) throws DataSourceException;

  /**
   * Returns a list of work log object.
   * @return An array of {@link WorkLog}.
   * @throws DataSourceException if failed to load meme work.
   */
  public WorkLog[] getWorkLogs() throws DataSourceException;

  /**
   * Returns a list of work log object.
   * @param type the type
   * @return An array of {@link WorkLog}
   * @throws DataSourceException if failed to load meme work
   */
  public WorkLog[] getWorkLogsByType(String type) throws DataSourceException;

  /**
   * Returns an activity log object.
   * @param transaction the {@link MolecularTransaction}
   * @return the {@link Activity}
   * @throws DataSourceException if failed to load activity log
   */
  public Activity getActivityLog(MolecularTransaction transaction) throws
      DataSourceException;

  /**
   * Returns a list of activity log objects.
   * @param work the {@link WorkLog}
   * @return {@link gov.nih.nlm.meme.action.Activity} entries
   * @throws DataSourceException if failed to load activity work
   */
  public Activity[] getActivityLogs(WorkLog work) throws DataSourceException;

  /**
   * Returns the logged error.
   * @param transaction the {@link MolecularTransaction}
   * @return An array of object {@link LoggedError}.
   * @throws DataSourceException if failed to load logged error.
   */
  public LoggedError[] getErrors(MolecularTransaction transaction) throws
      DataSourceException;

  /**
   * Logs the operation.
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
      DataSourceException;

  /**
   * Logs the progress.
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
      DataSourceException;

  /**
   * Resets the progress.
   * @param work the {@link WorkLog}
   * @throws DataSourceException if failed to reset progress
   */
  public void resetProgress(WorkLog work) throws DataSourceException;

  /**
   * Initialize Matrix.
   * @param work the {@link WorkLog}
   * @return the most recent initialize matrix log
   * @throws DataSourceException if failed to initialize matrix
   */
  public String initializeMatrix(WorkLog work) throws DataSourceException;

  /**
   * Change the password of user
   * @param user String
   * @param password String
   * @throws DataSourceException
   */
  public void changePassword(String user, String password)  throws DataSourceException;


  /**
   * Get the expiration date of user's password
   * @param user String
   * @param password String
   * @return Date
   * @throws MEMEException
   */
  public Date getPasswordExpirationDate() throws DataSourceException;

}
