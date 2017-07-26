package gov.nih.nlm.mrd.sql;


	import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.MRDContextRelationship;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.AtomMapper;
import gov.nih.nlm.meme.sql.MEMEDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

	public interface MRDContextRelationshipMapper {
	

	
	 /**
	  * Maps the specified {@link ResultSet} to an {@link Atom}.
	  * @param rs the {@link ResultSet}
	  * @param mds the {@link MEMEDataSource}
	  * @return the {@link Atom} for the current entry in the result set
	  * @throws SQLException if sql process failed.
	  * @throws DataSourceException if mapping failed.
	  * @throws BadValueException if failed due to invalid values.
	  */
	 public MRDContextRelationship map(ResultSet rs, MRDDataSource mds,Map atomMap) throws SQLException,
	     DataSourceException, BadValueException;

	 
	 //
	 // Inner Classes
	 //

	 /**
	  * This inner class serves as a default implementation of the
	  * {@link AtomMapper} interface.
	  */
	 public class Default implements MRDContextRelationshipMapper {

	   //
	   // Constructors
	   //

	   /**
	    * Instantiates a default {@link AtomMapper}.
	    */
	   public Default() {
	     super();
	   }

	   //
	   // Implementation of AtomMapper interface
	   //

	   /**
	    * Implements {@link AtomMapper#map(ResultSet, MEMEDataSource)}.
	    * @param rs the{@link ResultSet}
	    * @param mds the {@link MEMEDataSource}
	    * @return the {@link Atom}
	    * @throws SQLException if sql process failed
	    * @throws DataSourceException if mapping failed
	    * @throws BadValueException if failed due to invalid values
	    */
	   public MRDContextRelationship map(ResultSet rs, MRDDataSource mds,Map atomMap) throws SQLException,
	       DataSourceException, BadValueException {
		   MRDContextRelationship cxt = new MRDContextRelationship();
	        populate(rs, mds, atomMap,cxt);
	        return cxt;
	   }

	   /**
	    * Implements {@link AtomMapper#populate(ResultSet, MEMEDataSource, Atom)}.
	    * @param rs the {@link ResultSet}
	    * @param mds the {@link MEMEDataSource}
	    * @param atom the {@link Atom}
	    * @return <code>true</code>
	    * @throws SQLException if sql process failed
	    * @throws DataSourceException if populate failed
	    * @throws BadValueException if failed due to invalid values
	    */

	   public boolean populate(ResultSet rs, MRDDataSource mds, Map atomNameMap,MRDContextRelationship cxt) throws
	       SQLException, DataSourceException, BadValueException {

	     		   
	     cxt.setSource(mds.getSource_MRDConceptReport(rs.getString("ROOT_SOURCE")));
	     cxt.setAui(rs.getString("AUI"));
	     
	     String parTree = rs.getString("PARENT_TREENUM");
	     String[] parentAUIs = parTree.split("\\.");
//	     MEMEToolkit.logComment("Par Tree for AUI is [" + cxt.getAui() + "] " +  parTree + " Length is [" + parentAUIs.length + "]");
	     StringBuffer sb = new StringBuffer();
	     int length = parentAUIs.length;
	     int indent_ctr = 0;
	     for (int i=0; i < length; i++) {
	    	 if(!atomNameMap.containsKey(parentAUIs[i])) {
	    		 String aui1_name = mds.getAUIName(parentAUIs[i]);
				 atomNameMap.put(parentAUIs[i], aui1_name);
	    	 }
	    	 sb.append("   ");
    		 for (int j = 0; j < indent_ctr; j++) {
    			 sb.append("  ");
    		 }
    		 indent_ctr++;
    		      // Write ancestor
    		 sb.append((String)atomNameMap.get(parentAUIs[i]));
    		 sb.append("\n");
	     }
	     // Now Write the self
//	     sb.append("<");
	     if (!atomNameMap.containsKey(cxt.getAui())){
	    	 String aui1_name = mds.getAUIName(cxt.getAui());
			 atomNameMap.put(cxt.getAui(), aui1_name);
    	 }
	     sb.append("   ");
		 for (int j = 0; j < indent_ctr; j++) {
			 sb.append("  ");
		 }
		 indent_ctr++;
		      // Write ancestor
	     sb.append("&lt;");
		 sb.append((String)atomNameMap.get(cxt.getAui()));
		 sb.append("&gt;\n");
		 cxt.setHierarchy_String(sb.toString());
		 cxt.setHier_code(rs.getString("HIERARCHICAL_CODE"));
		 cxt.setRelationship_attribute(rs.getString("RELATIONSHIP_ATTRIBUTE"));
		 cxt.setRui(rs.getString("RUI"));
		 cxt.setSource_rui(rs.getString("SOURCE_RUI"));
		 cxt.setRel_group(rs.getString("RELATIONSHIP_GROUP"));
		 return true;
	   }

	}
}
