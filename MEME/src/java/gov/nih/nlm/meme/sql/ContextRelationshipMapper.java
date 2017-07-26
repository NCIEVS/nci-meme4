/*****************************************************************************
 * Package: gov.nih.nlm.meme.sql
 * Object:  ContextRelationshipMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a mechanism for creating a {@link ContextRelationship}
 * from a {@link ResultSet}.
 *
 * @author MEME Group
 */
public class ContextRelationshipMapper extends RelationshipMapper.Default {

  //
  // Constructor
  //

  /**
   * Instantiates an empty {@link ContextRelationshipMapper}.
   */
  public ContextRelationshipMapper() {
    super();
  }

  /**
   * Instantiates a {@link ContextRelationshipMapper}with the option
   * of whether or not to process CONCEPT_ID and ATOM_ID fields in
   * the {@link ResultSet}.
   * @param handle_id <code>true</code> if CONCEPT_ID and ATOM_ID should
   *  be handled, <code>false</code> otherwise.
   */
  public ContextRelationshipMapper(boolean handle_id) {
    super(handle_id);
  }

  //
  // Implementation of RelationshipMapper interface
  //

  /**
   * Maps the result set to a {@link ContextRelationship}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Relationship}.
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public Relationship map(ResultSet rs, MEMEDataSource mds) throws SQLException,
      DataSourceException, BadValueException {
    //
    // Creates a context relationship
    //
    ContextRelationship rel = new ContextRelationship.Default();

    //
    // Populates and returns it.
    //
    populate(rs, mds, rel);
    return rel;
  }

  /**
   * Overrides RelationshipMapper#addAtomRelationship(Atom, Relationship)
   * @param atom the {@link Atom}
   * @param rel the {@link Relationship}
   */
  protected void addAtomRelationship(Atom atom, Relationship rel) {
    atom.addContextRelationship( (ContextRelationship) rel);
  }

  /**
   * Overrides RelationshipMapper#addConceptRelationship(Concept, Relationship)
   * @param concept the {@link Concept}
   * @param rel the {@link Relationship}
   */
  protected void addConceptRelationship(Concept concept, Relationship rel) {
    concept.addContextRelationship( (ContextRelationship) rel);
  }

}
