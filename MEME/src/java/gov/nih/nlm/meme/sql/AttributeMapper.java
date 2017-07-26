/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  AttributeMapper
 *
 * 02/24/2009 BAC (1-GCLNT): Sets attribute rank to 0.
 * 
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.ATUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.ContextFormatter;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * Maps a {@link ResultSet} to {@link Attribute}s.
 *
 * @author MEME Group
 */
public interface AttributeMapper {

  /**
   * Maps the specified {@link ResultSet} to an {@link Attribute}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Attribute}
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public Attribute map(ResultSet rs, MEMEDataSource mds) throws SQLException,
      DataSourceException, BadValueException;

  /**
   * Sets the {@link Concept}.
   * @param concept the {@link Concept}
   */
  public void setConcept(Concept concept);

  /**
   * Sets the core data {@link Map}.
   * @param cd_map the core data {@link Map}
   */
  public void setCoreDataMap(Map cd_map);

  /**
   * Populates the {@link Attribute}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param attr the {@link Attribute}
   * @return <code>true</code> if the attribute was populated, <code>false</code> otherwise
   * @throws SQLException if sql process failed
   * @throws DataSourceException if populate failed
   * @throws BadValueException if failed due to invalid values
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, Attribute attr) throws
      SQLException, DataSourceException, BadValueException;

  //
  // Inner Classes
  //

  /**
   * Default implementation of
   * {@link AttributeMapper} interface.
   */
  public class Default implements AttributeMapper {

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
     * Instantiates a default {@link AttributeMapper}.
     */
    public Default() {
      this(false);
    }

    /**
     * Instantiates a default {@link AttributeMapper} with the option
     * of whether or not to process CONCEPT_ID and ATOM_ID fields in
     * the {@link ResultSet}.
     * @param handle_id <code>true</code> if CONCEPT_ID and ATOM_ID should
     *  be handled, <code>false</code> otherwise
     */
    public Default(boolean handle_id) {
      super();
      this.handle_id = handle_id;
    }

    //
    // Implementation of AttributeMapper interface
    //

    /**
     * Maps the result set to a particular {@link Attribute} implementation.
     * @param rs the {@link ResultSet}
     * @param mds the {@link MEMEDataSource}
     * @return the {@link Attribute}
     * @throws SQLException if sql process failed
     * @throws DataSourceException if mapping failed
     * @throws BadValueException if failed due to invalid values
     */
    public Attribute map(ResultSet rs, MEMEDataSource mds) throws SQLException,
        DataSourceException, BadValueException {

      Attribute attr = null;

      //
      // Either create a semantic type
      //
      if (rs.getString("ATTRIBUTE_NAME").equals(Attribute.SEMANTIC_TYPE)) {
        SemanticType sty = mds.getSemanticType(rs.getString("ATTRIBUTE_VALUE"));
        attr = new ConceptSemanticType(sty);

        //
        // Or a default attribute
        //
      } else {
        attr = new Attribute.Default();
      }

      //
      // Populate the attribute and return it
      //
      populate(rs, mds, attr);
      return attr;
    }

    /**
     * Sets the {@link Concept}.
     * @param concept the {@link Concept}
     */
    public void setConcept(Concept concept) {
      this.concept = concept;
    }

    /**
     * Sets the core data {@link Map}.
     * @param cd_map the core data {@link Map}
     */
    public void setCoreDataMap(Map cd_map) {
      this.cd_map = cd_map;
    }

    /**
     * Populates the attribute.
     * @param rs the {@link ResultSet}
     * @param mds the {@link MEMEDataSource}
     * @param attr the {@link Attribute}
     * @return <code>true</code> if the attribute was populated, <code>false</code> otherwise
     * @throws SQLException if sql process failed
     * @throws DataSourceException if populate failed
     * @throws BadValueException if failed due to invalid values
     */
    public boolean populate(ResultSet rs, MEMEDataSource mds, Attribute attr) throws
        SQLException, DataSourceException, BadValueException {

      //
      // Populate core data fields
      //
      attr.setIdentifier(new Identifier.Default(rs.getInt("ATTRIBUTE_ID")));
      attr.setSource(mds.getSource(rs.getString("SOURCE")));
      attr.setGenerated(rs.getString("GENERATED_STATUS").equals("Y"));
      attr.setDead(rs.getString("DEAD").equals("Y"));
      attr.setSuppressible(rs.getString("SUPPRESSIBLE"));
      attr.setStatus(rs.getString("STATUS").charAt(0));
      attr.setAuthority(mds.getAuthority(rs.getString("AUTHORITY")));
      attr.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
      attr.setInsertionDate(getDate(rs.getTimestamp("INSERTION_DATE")));
      attr.setReleased(rs.getString("RELEASED").charAt(0));
      attr.setTobereleased(rs.getString("TOBERELEASED").charAt(0));
      // last_molecule_id column is currently being ignored
      // last_atomic_action column is currently being ignored

      //
      // Populate attribute-specific fields
      //
      attr.setLevel(rs.getString("ATTRIBUTE_LEVEL").charAt(0));
      attr.setName(rs.getString("ATTRIBUTE_NAME"));
      attr.setValue(rs.getString("ATTRIBUTE_VALUE"));
      attr.setRank(new Rank.Default(0));

      //
      // Populate atom and concept
      //
      if (handle_id) {
        if (concept != null) {
          attr.setConcept(concept);
          concept.addAttribute(attr);
        } else {
          attr.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));

        }
        if (attr.isAtomLevel()) {
          String atom_id = rs.getString("ATOM_ID");
          if (cd_map != null && cd_map.containsKey("C" + atom_id)) {
            Atom atom = (Atom) cd_map.get("C" + atom_id);
            attr.setAtom(atom);
          } else {
            attr.setAtom(new Atom.Default(rs.getInt("ATOM_ID")));
          }
        }
      }

      // Handle formatting context
      if (attr.getName().equals(Attribute.CONTEXT) &&
          attr.getValue().indexOf('\t') != -1 &&
          attr.getAtom().getTermgroup() != null && attr.getAtom().getCode() != null) {
        attr.setValue
            (ContextFormatter.formatContext(
            attr.getValue(),
            attr.getSource().getSourceAbbreviation() + "/" +
            attr.getAtom().getTermgroup().toString(),
            attr.getAtom().getCode().toString()));
      }

      //
      // conditionally populate native identifier
      //
      String sg_type = rs.getString("SG_TYPE");
      if (sg_type != null) {
        String sg_meme_data_type = rs.getString("SG_MEME_DATA_TYPE");
        String sg_meme_id = rs.getString("SG_MEME_ID");
        attr.setNativeIdentifier(
            new NativeIdentifier(rs.getString("SG_ID"),
                                 sg_type,
                                 rs.getString("SG_QUALIFIER"),
                                 sg_meme_id,
                                 sg_meme_data_type));

        // C and CS cases handled in the handle_id section above
        if (sg_meme_data_type != null &&
            !sg_meme_data_type.equals("C") && !sg_meme_data_type.equals("CS") &&
            cd_map != null && cd_map.containsKey(sg_meme_data_type + sg_meme_id)) {
          CoreData cd = (CoreData) cd_map.get(sg_meme_data_type + sg_meme_id);
          attr.getNativeIdentifier().setCoreDataElement(cd);
          cd.addAttribute(attr);
        }
      }

      //
      // Conditionally populate other fields
      //
      if (rs.getString("SOURCE_ATUI") != null) {
        attr.setSourceIdentifier(new Identifier.Default(
            rs.getString("SOURCE_ATUI")));
      }
      if (rs.getString("ATUI") != null) {
        attr.setATUI(new ATUI(rs.getString("ATUI")));

      }
      return true;
    }

    /**
     * Validates and return the date.
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
