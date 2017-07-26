package gov.nih.nlm.mrd.sql;


import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MRDAttribute;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.AttributeMapper;
import gov.nih.nlm.meme.sql.MEMEDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * Maps a {@link ResultSet} to {@link Attribute}s.
 *
 * @author MEME Group
 */
public interface MRDAttributeMapper {

  /**
   * Maps the specified {@link ResultSet} to an {@link Attribute}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Attribute}
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public MRDAttribute map(ResultSet rs, MRDDataSource mds) throws SQLException,
      DataSourceException, BadValueException;

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
  public boolean populate(ResultSet rs, MRDDataSource mds, MRDAttribute attr) throws
      SQLException, DataSourceException, BadValueException;

  //
  // Inner Classes
  //

  /**
   * Default implementation of
   * {@link AttributeMapper} interface.
   */
  public class Default implements MRDAttributeMapper {

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
    public MRDAttribute map(ResultSet rs, MRDDataSource mds) throws SQLException,
        DataSourceException, BadValueException {

      MRDAttribute attr = new MRDAttribute();

      //
      // Either create a semantic type
      //
     
      attr.setAName(rs.getString("ATTRIBUTE_NAME"));
      attr.setAValue(rs.getString("ATTRIBUTE_VALUE"));
      
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
    public boolean populate(ResultSet rs,  MRDDataSource mds, MRDAttribute attr) throws
        SQLException, DataSourceException, BadValueException {

      //
      // Populate core data fields
      //
      attr.setAtui(rs.getString("ATUI"));
      attr.setSource(mds.getSource_MRDConceptReport(rs.getString("ROOT_SOURCE")));
      
      attr.setSuppressible(rs.getString("SUPPRESSIBLE"));
      
      // last_molecule_id column is currently being ignored
      // last_atomic_action column is currently being ignored

      //
      // Populate attribute-specific fields
      //
      attr.setLevel(rs.getString("ATTRIBUTE_LEVEL"));
      
      //
      // Populate atom and concept
      //
      
        if (attr.isAtomLevel()) {
          attr.setAui(rs.getString("UI"));   
        }
        attr.setCui(rs.getString("CUI"));
        attr.setCuiName(mds.getCUIName(attr.getCui()));

        attr.setSgType(rs.getString("SG_TYPE"));
        attr.setCode(rs.getString("CODE"));
        attr.setSource_atui(rs.getString("SOURCE_ATUI"));
        attr.setHashcode(rs.getString("HASHCODE"));
        if (attr.getAValue() != null && attr.getAValue().startsWith("<>Long_Attribute<>:")) {
        	attr.setAValue(mds.getLongAttributeValue(attr.getHashcode()));
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
