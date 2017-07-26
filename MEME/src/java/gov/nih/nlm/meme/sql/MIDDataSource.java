/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MIDDataSource
 * 
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.AtomChecklist;
import gov.nih.nlm.meme.common.AtomWorklist;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Checklist;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.MergeFact;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;

import java.util.Iterator;

/**
 * Generically represents a connection to a source of MID data.
 * @author MEME Group
 */
public interface MIDDataSource extends MEMEDataSource {

  /**
   * Populates worklist containing the given specified work.
   * @param work the {@link Worklist}.
   * @throws DataSourceException if worklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateWorklist(Worklist work) throws DataSourceException,
      BadValueException;

  /**
   * Populates checklist containing the given specified work.
   * @param check the {@link Checklist}.
   * @throws DataSourceException if checklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public void populateChecklist(Checklist check) throws DataSourceException,
      BadValueException;

  /**
   * Returns a worklist containing given specified name.
   * @param worklist_name the {@link String} representation
   *   of worklist name.
   * @return a worklist containing given specified name.
   * @throws DataSourceException if worklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public AtomWorklist getAtomWorklist(String worklist_name) throws
      DataSourceException, BadValueException;

  /**
   * Returns a checklist containing given the specified name.
   * @param checklist_name the {@link String} representation
   *   of checklist name.
   * @return a checklist containing given the specified name.
   * @throws DataSourceException if checklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public AtomChecklist getAtomChecklist(String checklist_name) throws
      DataSourceException, BadValueException;

  /**
   * Returns a worklist containing given the specified name.
   * @param worklist_name the {@link String} representation
   *   of worklist name.
   * @return a worklist containing given the specified name.
   * @throws DataSourceException if worklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ConceptWorklist getConceptWorklist(String worklist_name) throws
      DataSourceException, BadValueException;

  /**
   * Returns a checklist containing given the specified name.
   * @param checklist_name the {@link String} representation
   *   of checklist name.
   * @return a checklist containing given the specified name.
   * @throws DataSourceException if checklist could not be found or built.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ConceptChecklist getConceptChecklist(String checklist_name) throws
      DataSourceException, BadValueException;

  /**
   * Adds an atom worklist.
   * @param worklist the {@link AtomWorklist}.
   * @throws DataSourceException if atom worklist could not be built.
   */
  public void addAtomWorklist(AtomWorklist worklist) throws DataSourceException;

  /**
   * Adds a concept worklist.
   * @param worklist the {@link ConceptWorklist}.
   * @throws DataSourceException if concept worklist could not be built.
   */
  public void addConceptWorklist(ConceptWorklist worklist) throws
      DataSourceException;

  /**
   * Adds an atom checklist.
   * @param checklist the {@link AtomChecklist}.
   * @throws DataSourceException if atom checklist could not be built.
   */
  public void addAtomChecklist(AtomChecklist checklist) throws
      DataSourceException;

  /**
   * Adds a concept checklist.
   * @param checklist the {@link ConceptChecklist}.
   * @throws DataSourceException if concept checklist could not be built.
   */
  public void addConceptChecklist(ConceptChecklist checklist) throws
      DataSourceException;

  /**
   * Checks whether worklist exist.
   * @param worklist_name the {@link String} representation of worklist.
   * @return <code>true</code> if worklist exist; <code>false</code> otherwise.
   * @throws DataSourceException if failed to check if worklist exist.
   */
  public boolean worklistExists(String worklist_name) throws
      DataSourceException;

  /**
   * Checks whether checklist exist.
   * @param checklist_name the {@link String} representation of checklist.
       * @return <code>true</code> if checklist exist; <code>false</code> otherwise.
   * @throws DataSourceException if failed to check if checklist exist.
   */
  public boolean checklistExists(String checklist_name) throws
      DataSourceException;

  /**
   * Removes a worklist.
   * @param worklist_name the {@link String} representation of worklist.
   * @throws DataSourceException if worklist could not be removed.
   */
  public void removeWorklist(String worklist_name) throws DataSourceException;

  /**
   * Removes a checklist.
   * @param checklist_name the {@link String} representation of checklist.
   * @throws DataSourceException if checklist could not be removed.
   */
  public void removeChecklist(String checklist_name) throws DataSourceException;

  /**
   * Search for worklist.
   * @param param A single {@link SearchParameter}
   * @return the {@link Iterator}
   * @throws DataSourceException if failed to find worklist
   */
  public Iterator findWorklists(SearchParameter param) throws
      DataSourceException;

  /**
   * Search for worklists.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find worklist.
   */
  public Iterator findWorklists(SearchParameter[] param) throws
      DataSourceException;

  /**
   * Search for names of worklists.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find worklists.
   */
  public Iterator findWorklistNames(SearchParameter param) throws
      DataSourceException;

  /**
   * Search for names of worklists.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find worklists.
   */
  public Iterator findWorklistNames(SearchParameter[] param) throws
      DataSourceException;

  /**
   * Search for checklist.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find checklist.
   */
  public Iterator findChecklists(SearchParameter param) throws
      DataSourceException;

  /**
   * Search for checklists.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find checklist.
   */
  public Iterator findChecklists(SearchParameter[] param) throws
      DataSourceException;

  /**
   * Search for names of checklists.
   * @param param A single {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find checklists.
   */
  public Iterator findChecklistNames(SearchParameter param) throws
      DataSourceException;

  /**
   * Search for names of checklists.
   * @param param An array of {@link SearchParameter}s.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find checklists.
   */
  public Iterator findChecklistNames(SearchParameter[] param) throws
      DataSourceException;

  /**
   * Finds a matching merge facts.
   * @param param the {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching merge fact.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Iterator findMergeFacts(SearchParameter param) throws
      DataSourceException, BadValueException;

  /**
   * Finds a matching merge facts.
   * @param params An array of object {@link SearchParameter}.
   * @return the {@link Iterator}.
   * @throws DataSourceException if failed to find a matching merge fact.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Iterator findMergeFacts(SearchParameter[] params) throws
      DataSourceException, BadValueException;

  /**
   * Returns an atom object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param atom_id the atom id.
   * @return the {@link Atom}.
   * @throws DataSourceException if failed to load atom.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Atom getDeadAtom(int atom_id) throws DataSourceException,
      BadValueException;

  /**
   * Returns a concept object.
   * @param concept_id the concept id.
   * @return the {@link Concept}.
   * @throws DataSourceException if failed to look up dead concept.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Concept getDeadConcept(int concept_id) throws DataSourceException,
      BadValueException;

  /**
   * Returns an attribute object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param rel_id the attribute id.
   * @return the {@link Attribute}.
   * @throws DataSourceException if failed to load attribute.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Attribute getDeadAttribute(int rel_id) throws DataSourceException,
      BadValueException;

  /**
   * Returns a relationship object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param rel_id the relationship id.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Relationship getDeadRelationship(int rel_id) throws
      DataSourceException, BadValueException;

  /**
   * Returns a context relationship object.  The contents of the object
   * are subject to the {@link Ticket} parameters.
   * @param rel_id the relationship id.
   * @return the {@link Relationship}.
   * @throws DataSourceException if failed to load context relationship.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ContextRelationship getDeadContextRelationship(int rel_id) throws
      DataSourceException, BadValueException;

  /**
   * Returns a new enforcable integrity vector.
   * @param iv the {@link String} representation of vector.
   * @return the {@link EnforcableIntegrityVector}.
   * @throws DataSourceException if failed to create new enforcable integrity vector.
   * @throws BadValueException if failed due to invalid data value.
   */
  public EnforcableIntegrityVector newEnforcableIntegrityVector(String iv) throws
      DataSourceException, BadValueException;

  /**
   * Returns a new violations vector.
   * @param iv the {@link String} representation of vector.
   * @return the {@link ViolationsVector}.
   * @throws DataSourceException if failed to create new violations vector.
   * @throws BadValueException if failed due to invalid data value.
   */
  public ViolationsVector newViolationsVector(String iv) throws
      DataSourceException, BadValueException;

  /**
   * Returns a list of MergeFact.
   * @param merge_set the {@link String} representation of merge set.
   * @param merge_fact_id the merge fact id.
   * @return the {@link MergeFact}.
   * @throws DataSourceException if failed to load merge set.
   * @throws BadValueException if failed due to invalid data value.
   */
  public MergeFact getMergeFact(String merge_set, int merge_fact_id) throws
      DataSourceException, BadValueException;

  //
  // Action
  //

  /**
   * Returns an action engine object.
   * @return the {@link ActionEngine}.
   */
  public ActionEngine getActionEngine();

  //
  // Action Validation Management
  //

  /**
   * Enables atomic action validation.
   * @throws DataSourceException if failed to set atomic action validation.
   */
  public void setAtomicActionValidationEnabled() throws DataSourceException;

  /**
   * Disables atomic action validation.
   * @throws DataSourceException if failed to set atomic action validation.
   */
  public void setAtomicActionValidationDisabled() throws DataSourceException;

  /**
   * Determines whether atomic action validation is enabled or disabled.
   * @return <code>true</code> if atomic action validation is enabled;
   * <code>false</code> otherwise.
   * @throws DataSourceException if failed to determine atomic action
   * validation status.
   */
  public boolean isAtomicActionValidationEnabled() throws DataSourceException;

  /**
   * Enables molecular action validation.
   * @throws DataSourceException if failed to set molecular action validation.
   */
  public void setMolecularActionValidationEnabled() throws DataSourceException;

  /**
   * Disables molecular action validation.
   * @throws DataSourceException if failed to set molecular action validation.
   */
  public void setMolecularActionValidationDisabled() throws DataSourceException;

  /**
   * Determines whether molecular action validation is enabled or disabled.
   * @return <code>true</code> if molecular action validation is enabled;
   * <code>false</code> otherwise.
   * @throws DataSourceException if failed to determine molecular action
   * validation status.
   */
  public boolean isMolecularActionValidationEnabled() throws
      DataSourceException;

  //
  // Valid Values Management
  //

  /**
   * Returns a list of valid char values.
   * @param field_map A key in the hash map.
   * @return An array of <code>char</code>.
   */
  public char[] getValidCharValues(String field_map);

  /**
   * Returns a list of valid status values for atoms.
   * @return An array of <code>char</code> representation of status.
   * @throws DataSourceException if failed to load valid status.
   */
  public char[] getValidStatusValuesForAtoms() throws DataSourceException;

  /**
   * Returns a list of valid status values for attributes.
   * @return An array of <code>char</code> representation of status.
   * @throws DataSourceException if failed to load valid status.
   */
  public char[] getValidStatusValuesForAttributes() throws DataSourceException;

  /**
   * Returns a list of valid status values for concepts.
   * @return An array of <code>char</code> representation of status.
   * @throws DataSourceException if failed to load valid status.
   */
  public char[] getValidStatusValuesForConcepts() throws DataSourceException;

  /**
   * Returns a list of valid status values for relationships.
   * @return An array of <code>char</code> representation of status.
   * @throws DataSourceException if failed to load valid status.
   */
  public char[] getValidStatusValuesForRelationships() throws
      DataSourceException;

  /**
   * Returns a list of valid level values for relationships.
   * @return An array of <code>char</code> representation of level.
   * @throws DataSourceException if failed to load valid level.
   */
  public char[] getValidLevelValuesForRelationships() throws
      DataSourceException;

  /**
   * Returns a list of valid level values for attributes.
   * @return An array of <code>char</code> representation of level.
   * @throws DataSourceException if failed to load valid level.
   */
  public char[] getValidLevelValuesForAttributes() throws DataSourceException;

  /**
   * Returns a list of valid released values.
       * @return An array of <code>char</code> representation of valid released values.
   * @throws DataSourceException if failed to load valid released.
   */
  public char[] getValidReleasedValues() throws DataSourceException;

  /**
   * Returns a list of valid toreleased values.
   * @return An array of <code>char</code> representation of valid toreleased values.
   * @throws DataSourceException if failed to load valid toreleased.
   */
  public char[] getValidTobereleasedValues() throws DataSourceException;

  //
  // Editing System Management
  //

  /**
   * Adds the editor preferences object.
   * @param ep An of object {@link EditorPreferences}
   * @throws DataSourceException if failed to add editor preferences
   * @throws BadValueException if failed due to bad value
   */
  public void addEditorPreferences(EditorPreferences ep) throws
      DataSourceException, BadValueException;

  /**
   * Removes the editor preferences object.
   * @param ep An of object {@link EditorPreferences}
   * @throws DataSourceException if failed to remove editor preferences
   */
  public void removeEditorPreferences(EditorPreferences ep) throws
      DataSourceException;

  /**
   * Sets the editor preferences object.
   * @param ep An of object {@link EditorPreferences}
   * @throws DataSourceException if failed to set editor preferences
   */
  public void setEditorPreferences(EditorPreferences ep) throws
      DataSourceException;

  /**
   * Returns a list of editor preferences object.
   * @return An array of object {@link EditorPreferences}.
   */
  public EditorPreferences[] getEditorPreferences();

  /**
   * Returns an editor preferences object.
   * @param initials A {@link String} representing the editor's initials
   * @return The {@link EditorPreferences} representing the editor
   * @throws BadValueException if failed due to invalid data value.
   */
  public EditorPreferences getEditorPreferencesByInitials(String initials) throws
      BadValueException;

  /**
   * Returns an editor preferences object.
   * @param username A {@link String} representing the editor's username
   * @return The {@link EditorPreferences} representing the editor
   * @throws BadValueException if failed due to invalid data value.
   */
  public EditorPreferences getEditorPreferencesByUsername(String username) throws
      BadValueException;

  /**
   * Enables or disables editing system.
   * @param status A <code>boolean</code> represents whether editing systems
   * must be enabled or disabled.
   * @throws DataSourceException if failed to set integrity system.
   */
  public void setEditingEnabled(boolean status) throws DataSourceException;

  /**
   * Determines whether editing system is enabled or disabled.
   * @return <code>true</code> if editing system is enabled; <code>false</code>
   * otherwise.
   * @throws DataSourceException if failed to determine editing system status.
   */
  public boolean isEditingEnabled() throws DataSourceException;

  //
  // Integrity System Management
  //

  /**
   * Enables or disables integrity system.
       * @param ic_status A <code>boolean</code> represents whether integrity systems
   * must be enabled or disabled.
   * @throws DataSourceException if failed to set integrity system.
   */
  public void setIntegritySystemEnabled(boolean ic_status) throws
      DataSourceException;

  /**
   * Determines whether integrity system is enabled or disabled.
       * @return <code>true</code> if integrity system is enabled; <code>false</code>
   * otherwise.
       * @throws DataSourceException if failed to determine integrity system status.
   */
  public boolean isIntegritySystemEnabled() throws DataSourceException;

  //
  // Integrity Check
  //

  /**
   * Inserts a new integrity check.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to add integrity check
   * @throws BadValueException if failed due to bad values
   */
  public void addIntegrityCheck(IntegrityCheck ic) throws DataSourceException,
      BadValueException;

  /**
   * Updates the integrity check object.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to update integrity check.
   */
  public void setIntegrityCheck(IntegrityCheck ic) throws DataSourceException;

  /**
   * Removes the integrity check object.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to remove integrity check.
   */
  public void removeIntegrityCheck(IntegrityCheck ic) throws
      DataSourceException;

  /**
   * Activates integrity check.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to activate integrity check.
   */
  public void activateIntegrityCheck(IntegrityCheck ic) throws
      DataSourceException;

  /**
   * Deactivates integrity check.
   * @param ic the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to deactivate integrity check.
   */
  public void deactivateIntegrityCheck(IntegrityCheck ic) throws
      DataSourceException;

  /**
   * Returns an integrity check object.
   * @param check_name the {@link String} representation of ic name.
   * @return the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to load integrity check.
   */
  public IntegrityCheck getIntegrityCheck(String check_name) throws
      DataSourceException;

  /**
   * Returns an array of integrity check object.
   * @return An array of object {@link IntegrityCheck}.
   * @throws DataSourceException if failed to load integrity check.
   */
  public IntegrityCheck[] getIntegrityChecks() throws DataSourceException;

  //
  // Application Vector
  //

  /**
   * Inserts a new application vector.
   * @param application the {@link String} representation of ic application.
   * @param iv the {@link IntegrityVector}.
   * @throws DataSourceException if failed to add integrity vector.
   * @throws BadValueException if failed due to bad value
   */
  public void addApplicationVector(String application, IntegrityVector iv) throws
      DataSourceException, BadValueException;

  /**
   * Sets the application vector.
   * @param application the {@link String} representation of ic application.
   * @param iv the {@link IntegrityVector}.
   * @throws DataSourceException if failed to set application vector.
   */
  public void setApplicationVector(String application, IntegrityVector iv) throws
      DataSourceException;

  /**
   * Removes the application vector object.
   * @param application the {@link String} representation of application.
   * @throws DataSourceException if failed to remove application vector.
   */
  public void removeApplicationVector(String application) throws
      DataSourceException;

  /**
   * Returns the application vector.
   * @param application the {@link String} representation of ic application.
   * @return the {@link EnforcableIntegrityVector}.
   * @throws DataSourceException if failed to load application vector.
   */
  public EnforcableIntegrityVector getApplicationVector(String application) throws
      DataSourceException;

  /**
   * Returns an array of vector.
   * @return An array of object {@link String} representation of vectors.
   * @throws DataSourceException if failed to load application vector.
   */
  public String[] getApplicationsWithVectors() throws DataSourceException;

  /**
   * Adds check to application vector.
   * @param application the {@link String} representation of ic application.
   * @param check the {@link IntegrityCheck}.
   * @param code A <code>String</code> representation of integrity check code.
   * @throws DataSourceException if failed to add check to application vector.
   */
  public void addCheckToApplicationVector(String application,
                                          IntegrityCheck check, String code) throws
      DataSourceException;

  /**
   * Removes check from application vector.
   * @param application the {@link String} representation of ic application.
   * @param check the {@link IntegrityCheck}.
       * @throws DataSourceException if failed to remove check to application vector.
   */
  public void removeCheckFromApplicationVector(String application,
                                               IntegrityCheck check) throws
      DataSourceException;

  //
  // Override Vector Management
  //

  /**
   * Inserts a new override vector.
   * @param ic_level the ic level.
   * @param iv the {@link IntegrityVector}.
   * @throws DataSourceException if failed to set override vector
   * @throws BadValueException if failed due to bad value
   */
  public void addOverrideVector(int ic_level, IntegrityVector iv) throws
      DataSourceException, BadValueException;

  /**
   * Sets the override vector.
   * @param ic_level the ic level.
   * @param iv the {@link IntegrityVector}.
   * @throws DataSourceException if failed to set override vector.
   */
  public void setOverrideVector(int ic_level, IntegrityVector iv) throws
      DataSourceException;

  /**
   * Removes the override vector object.
   * @param ic_level the ic level.
   * @throws DataSourceException if failed to remove override vector.
   */
  public void removeOverrideVector(int ic_level) throws DataSourceException;

  /**
   * Returns the integrity vector.
   * @param ic_level the ic level.
   * @return the {@link IntegrityVector}.
   * @throws DataSourceException if failed to load override vector.
   */
  public IntegrityVector getOverrideVector(int ic_level) throws
      DataSourceException;

  /**
   * Returns an array of ic levels.
   * @return A list of ic levels.
   * @throws DataSourceException if failed to load override vector.
   */
  public int[] getLevelsWithOverrideVectors() throws DataSourceException;

  /**
   * Adds check to override vector.
   * @param ic_level the ic level.
   * @param check the {@link IntegrityCheck}.
   * @param code A <code>String</code> representation of integrity vector code.
   * @throws DataSourceException if failed to add check to override vector.
   */
  public void addCheckToOverrideVector(int ic_level, IntegrityCheck check,
                                       String code) throws DataSourceException;

  /**
   * Removes check from override vector.
   * @param ic_level the ic level.
   * @param check the {@link IntegrityCheck}.
   * @throws DataSourceException if failed to remove check to override vector.
   */
  public void removeCheckFromOverrideVector(int ic_level, IntegrityCheck check) throws
      DataSourceException;

  /**
   * Sets a merge fact.
   * @param fact the {@link MergeFact}.
   * @throws DataSourceException if failed to set merge fact.
   */
  public void setMergeFact(MergeFact fact) throws DataSourceException;

}
