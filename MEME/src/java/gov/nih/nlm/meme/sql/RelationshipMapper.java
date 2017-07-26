/*****************************************************************************
 * Package: gov.nih.nlm.meme.sql
 * Object:  RelationshipMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.RUI;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * Maps a {@link Relationship} from a {@link ResultSet}.
 *
 * @author MEME Group
 */
public interface RelationshipMapper {

  /**
   * Maps the result set to a particular {@link Relationship} implementation.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Relationship}
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public Relationship map(ResultSet rs, MEMEDataSource mds) throws SQLException,
      DataSourceException, BadValueException;

  /**
   * Sets the concept.
   * @param concept the {@link Concept}
   */
  public void setConcept(Concept concept);

  /**
   * Sets the core data map.
   * @param cd_map the {@link Map}
   */
  public void setCoreDataMap(Map cd_map);

  /**
   * Populates the {@link Relationship}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param rel the {@link Relationship}
   * @return <code>true</code> if the relationship was populated, <code>false</code> otherwise
   * @throws SQLException if sql process failed
   * @throws DataSourceException if populate failed
   * @throws BadValueException if failed due to invalid values
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, Relationship rel) throws
      SQLException, DataSourceException, BadValueException;

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link RelationshipMapper} interface.
   */
  public class Default implements RelationshipMapper {

    //
    // Fields
    //

    protected boolean handle_id = false;
    protected Concept concept = null;
    protected Map cd_map = null;

    //
    // Constructors
    //

    /**
     * Instantiates a default {@link RelationshipMapper}.
     */
    public Default() {
      this(false);
    }

    /**
     * Instantiates a default {@link RelationshipMapper} with the option
     * of whether or not to process CONCEPT_ID and ATOM_ID fields in
     * the {@link ResultSet}.
     * @param handle_id <code>true</code> if CONCEPT_ID and ATOM_ID should
     *  be handled, <code>false</code> otherwise.
     */
    public Default(boolean handle_id) {
      super();
      this.handle_id = handle_id;
    }

    //
    // Implementation of RelationshipMapper interface
    //

    /**
     * Maps the result set to a particular {@link Relationship} implementation.
     * @param rs the {@link ResultSet}
     * @param mds the {@link MEMEDataSource}
     * @return the {@link Relationship}
     * @throws SQLException if sql process failed
     * @throws DataSourceException if mapping failed
     * @throws BadValueException if failed due to invalid values
     */
    public Relationship map(ResultSet rs, MEMEDataSource mds) throws
        SQLException, DataSourceException, BadValueException {

      //
      // Create a default relationship
      //
      Relationship rel = new Relationship.Default();

      //
      // Populate it and return it
      //
      populate(rs, mds, rel);
      return rel;
    }

    /**
     * Sets the concept.
     * @param concept the {@link Concept}
     */
    public void setConcept(Concept concept) {
      this.concept = concept;
    }

    /**
     * Sets the core data map.
     * @param cd_map the {@link Map}
     */
    public void setCoreDataMap(Map cd_map) {
      this.cd_map = cd_map;
    }

    /**
     * Populates the {@link Relationship}.
     * @param rs the {@link ResultSet}
     * @param mds the {@link MEMEDataSource}
     * @param rel the {@link Relationship}
     * @return <code>true</code> if the relationship was populated, <code>false</code> otherwise
     * @throws SQLException if sql process failed
     * @throws DataSourceException if populate failed
     * @throws BadValueException if failed due to invalid values
     */
    public boolean populate(ResultSet rs, MEMEDataSource mds, Relationship rel) throws
        SQLException, DataSourceException, BadValueException {

      //
      // Populate core data fields
      //
      rel.setIdentifier(new Identifier.Default(rs.getInt("RELATIONSHIP_ID")));
      rel.setSource(mds.getSource(rs.getString("SOURCE")));
      rel.setGenerated(rs.getString("GENERATED_STATUS").equals("Y"));
      rel.setDead(rs.getString("DEAD").equals("Y"));
      rel.setSuppressible(rs.getString("SUPPRESSIBLE"));
      rel.setStatus(rs.getString("STATUS").charAt(0));
      rel.setAuthority(mds.getAuthority(rs.getString("AUTHORITY")));
      rel.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
      rel.setInsertionDate(getDate(rs.getTimestamp("INSERTION_DATE")));
      rel.setReleased(rs.getString("RELEASED").charAt(0));
      rel.setTobereleased(rs.getString("TOBERELEASED").charAt(0));
      // last_molecule_id column is currently being ignored
      // last_atomic_action column is currently being ignored

      //
      // Populate relationship-specific fields
      //
      rel.setLevel(rs.getString("RELATIONSHIP_LEVEL").charAt(0));
      rel.setName(rs.getString("RELATIONSHIP_NAME"));
      rel.setAttribute(rs.getString("RELATIONSHIP_ATTRIBUTE"));
      rel.setSourceOfLabel(mds.getSource(rs.getString("SOURCE_OF_LABEL")));
      rel.setRank(new Rank.Default(rs.getInt("RANK")));

      int concept_id_1 = rs.getInt("CONCEPT_ID_1");
      int concept_id_2 = rs.getInt("CONCEPT_ID_2");
      int atom_id_1 = rs.getInt("ATOM_ID_1");
      int atom_id_2 = rs.getInt("ATOM_ID_2");

      //
      // Populate concept/atom fields
      //
      if (handle_id) {
        if (concept != null) {
          rel.setConcept(concept);
          //
          // Add relationship to concept If it is self-referential, add it only once
          //
          if (concept_id_1 != concept_id_2 ||
              (concept_id_1 == concept_id_2 &&
               atom_id_1 < atom_id_2)) {
            addConceptRelationship(concept, rel);

          }
        } else {
          rel.setConcept(new Concept.Default(concept_id_1));
        }

        if (concept != null && concept_id_1 == concept_id_2) {
          rel.setRelatedConcept(concept);
        } else {
          rel.setRelatedConcept(new Concept.Default(concept_id_2));
        }

        if (rel.isAtomLevel()) {
          int atom_id = atom_id_2;
          if (cd_map != null && cd_map.containsKey("C" + atom_id)) {
            Atom atom = (Atom) cd_map.get("C" + atom_id);
            rel.setRelatedAtom(atom);
          } else {
            rel.setRelatedAtom(new Atom.Default(atom_id_2));

          }
          atom_id = atom_id_1;
          if (cd_map != null && cd_map.containsKey("C" + atom_id)) {
            Atom atom = (Atom) cd_map.get("C" + atom_id);
            rel.setAtom(atom);
          } else {
            rel.setAtom(new Atom.Default(atom_id_1));
          }
        }

      }

      //
      // Populate translation atoms
      //
      if (rel.getAtom() != null && rel.getRelatedAtom() != null &&
          rel.getAttribute() != null &&
          rel.getAttribute().equals("translation_of") &&
          rel.getAtom().getLanguage() != null &&
          rel.getAtom().getLanguage().getAbbreviation().equals("ENG") &&
          rel.isAtomLevel()) {
        rel.getAtom().addTranslationAtom(rel.getRelatedAtom());
      }

      //
      // populate native identifiers
      //
      String sg_type_1 = rs.getString("SG_TYPE_1");
      if (sg_type_1 != null) {
        String sg_meme_data_type_1 = rs.getString("SG_MEME_DATA_TYPE_1");
        String sg_meme_id_1 = rs.getString("SG_MEME_ID_1");
        rel.setNativeIdentifier(
            new NativeIdentifier(rs.getString("SG_ID_1"),
                                 sg_type_1,
                                 rs.getString("SG_QUALIFIER_1"),
                                 sg_meme_id_1,
                                 sg_meme_data_type_1));
        // C and CS cases handled in the handle_id section above
        if (sg_meme_data_type_1 != null && !sg_meme_data_type_1.equals("C") &&
            !sg_meme_data_type_1.equals("CS") &&
            cd_map != null &&
            cd_map.containsKey(sg_meme_data_type_1 + sg_meme_id_1)) {
          CoreData cd = (CoreData) cd_map.get(sg_meme_data_type_1 +
                                              sg_meme_id_1);
          rel.getNativeIdentifier().setCoreDataElement(cd);
          cd.addRelationship(rel);
        }
      }

      String sg_type_2 = rs.getString("SG_TYPE_2");
      if (sg_type_2 != null) {
        String sg_meme_data_type_2 = rs.getString("SG_MEME_DATA_TYPE_2");
        String sg_meme_id_2 = rs.getString("SG_MEME_ID_2");
        rel.setNativeIdentifier(
            new NativeIdentifier(rs.getString("SG_ID_2"),
                                 sg_type_2,
                                 rs.getString("SG_QUALIFIER_2"),
                                 sg_meme_id_2,
                                 sg_meme_data_type_2));
        // C and CS cases handled in the handle_id section above
        if (sg_meme_data_type_2 != null && !sg_meme_data_type_2.equals("C") &&
            !sg_meme_data_type_2.equals("CS") &&
            cd_map != null &&
            cd_map.containsKey(sg_meme_data_type_2 + sg_meme_id_2)) {
          CoreData cd = (CoreData) cd_map.get(sg_meme_data_type_2 +
                                              sg_meme_id_2);
          rel.getRelatedNativeIdentifier().setCoreDataElement(cd);
          cd.addRelationship(rel);
        }
      }

      //
      // Populate fields added later
      //
      if (rs.getString("SOURCE_RUI") != null) {
        rel.setSourceIdentifier(new Identifier.Default(
            rs.getString("SOURCE_RUI")));
      }
      if (rs.getString("RUI") != null) {
        rel.setRUI(new RUI(rs.getString("RUI")));
      }
      if (rs.getString("RELATIONSHIP_GROUP") != null) {
        rel.setGroupIdentifier(new Identifier.Default(
            rs.getString("RELATIONSHIP_GROUP")));

      }
      return true;
    }

    /**
     * Adds relationship to the atom.
     * @param atom the {@link Atom}
     * @param rel the {@link Relationship}
     */
    protected void addAtomRelationship(Atom atom, Relationship rel) {
      atom.addRelationship(rel);
    }

    /**
     * Adds relationship to the concept
     * @param concept the {@link Concept}
     * @param rel the {@link Relationship}
     */
    protected void addConceptRelationship(Concept concept, Relationship rel) {
      concept.addRelationship(rel);
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
  }
}