package gov.nih.nlm.mrd.sql;


	import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MRDAttribute;
import gov.nih.nlm.meme.common.MRDRelationship;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.AttributeMapper;
import gov.nih.nlm.meme.sql.MEMEDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;



public interface MRDRelationshipMapper {
	

	/**
	 * Maps a {@link ResultSet} to {@link Attribute}s.
	 *
	 * @author MEME Group
	 */


	  /**
	   * Maps the specified {@link ResultSet} to an {@link Attribute}.
	   * @param rs the {@link ResultSet}
	   * @param mds the {@link MEMEDataSource}
	   * @return the {@link Attribute}
	   * @throws SQLException if sql process failed
	   * @throws DataSourceException if mapping failed
	   * @throws BadValueException if failed due to invalid values
	   */
	  public MRDRelationship map(ResultSet rs, MRDDataSource mds, Map atomNameMap) throws SQLException,
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
	  public boolean populate(ResultSet rs, MRDDataSource mds, Map atomNameMap,MRDRelationship rel) throws
	      SQLException, DataSourceException, BadValueException;

  
	  //
	  // Inner Classes
	  //

	  /**
	   * Default implementation of
	   * {@link AttributeMapper} interface.
	   */
	  public class Default implements MRDRelationshipMapper  {

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
	    public MRDRelationship map(ResultSet rs, MRDDataSource mds,Map atomNameMap) throws SQLException,
	        DataSourceException, BadValueException {

	    	MRDRelationship rel = new MRDRelationship();
	    	populate(rs, mds, atomNameMap,rel);
	    	return rel;
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
	    public boolean populate(ResultSet rs,  MRDDataSource mds, Map atomNameMap, MRDRelationship rel) throws
	        SQLException, DataSourceException, BadValueException {

	    	rel.setLevel(rs.getString("RELATIONSHIP_LEVEL"));
			 rel.setAui_1(rs.getString("AUI_1"));
			 rel.setAui_2(rs.getString("AUI_2"));
			 rel.setCui_1(rs.getString("CUI_1"));
			 rel.setCui_2(rs.getString("CUI_2"));
			 rel.setSType_1(rs.getString("SG_TYPE_1"));
			 rel.setSType_2(rs.getString("SG_TYPE_2"));
			 rel.setRelationship_name(rs.getString("RELATIONSHIP_NAME"));
			 rel.setRelationship_attribute(rs.getString("RELATIONSHIP_ATTRIBUTE"));
			 rel.setSuppressible(rs.getString("SUPPRESSIBLE"));
			 rel.setSource_label(rs.getString("ROOT_SOURCE_OF_LABEL"));
			 rel.setSource(mds.getSource_MRDConceptReport(rs.getString("ROOT_SOURCE")));
			 rel.setRui(rs.getString("RUI"));
			 rel.setSource_rui(rs.getString("SOURCE_RUI"));
			 rel.setRel_group(rs.getString("RELATIONSHIP_GROUP"));
			 rel.setRel_direction_flag(rs.getString("REL_DIRECTIONALITY_FLAG"));
			 if(atomNameMap != null && atomNameMap.size() > 0) {
			 String aui1_name = "";
			 String aui2_name = "";
			 String cui1_name = "";
			 String cui2_name = "";
			 if (rel.getLevel().equals("S")) {
				 // populate the atom strings
				 if (atomNameMap.containsKey(rel.getAui_1())) {
					 aui1_name = (String)atomNameMap.get(rel.getAui_1());
				 } else {
					 // Make a query and get the atomName
					 aui1_name = mds.getAUIName(rel.getAui_1());
					 atomNameMap.put(rel.getAui_1(), aui1_name);
				 }
				 if (atomNameMap.containsKey(rel.getAui_2())) {
					 aui2_name = (String)atomNameMap.get(rel.getAui_2());
				 } else {
					 // Make a query to get the atomName
					 aui2_name = mds.getAUIName(rel.getAui_2());
					 atomNameMap.put(rel.getAui_2(), aui2_name);
				 }
			 } else {
		
				 if (atomNameMap.containsKey(rel.getCui_1())) {
					 cui1_name = (String)atomNameMap.get(rel.getCui_1());
				 } else {
					 // make a query to get the cuiName ( Highest atom name)
					 cui1_name = mds.getCUIName(rel.getCui_1());
					 atomNameMap.put(rel.getCui_1(), cui1_name);
				 }
				 if (atomNameMap.containsKey(rel.getCui_2())) {
					 cui2_name = (String) atomNameMap.get(rel.getCui_2());
				 } else {
					 // Make a query to get the cuiName ( highest atomName for this cui)
					 cui2_name = mds.getCUIName(rel.getCui_2());
					 atomNameMap.put(rel.getCui_2(), cui2_name);
				 }
			 }
			 rel.setAui1_name(aui1_name);
			 rel.setAui2_name(aui2_name);
			 rel.setCui1_name(cui1_name);
			 rel.setCui2_name(cui2_name);
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
