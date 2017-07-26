
/*****************************************************************************
*
* Package: gov.nih.nlm.meme.sql
* Object:  AtomMapper
* CHANGES
*  11/15/2006 BAC (1-CTLDV): Change to atom rank to ensure SUI,AUI parts are always 9 digits
*  
*****************************************************************************/
package gov.nih.nlm.mrd.sql;


import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.MRDAtom;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
* Maps a {@link ResultSet} to {@link Atom}s.
*
* @author MEME Group
*/
public interface MRDAtomMapper {

 /**
  * Maps the specified {@link ResultSet} to an {@link Atom}.
  * @param rs the {@link ResultSet}
  * @param mds the {@link MEMEDataSource}
  * @return the {@link Atom} for the current entry in the result set
  * @throws SQLException if sql process failed.
  * @throws DataSourceException if mapping failed.
  * @throws BadValueException if failed due to invalid values.
  */
 public MRDAtom map(ResultSet rs, MRDDataSource mds) throws SQLException,
     DataSourceException, BadValueException;

 /**
  * Populates the {@link Atom}}.
  * @param rs the {@link ResultSet}
  * @param mds the {@link MEMEDataSource}
  * @param atom the {@link Atom} to populate
  * @return <code>true</code> if this mapper's populate method was used,
  * <code>false</code> otherwise
  * @throws SQLException if sql process failed.
  * @throws DataSourceException if populate failed.
  * @throws BadValueException if failed due to invalid values.
  */
 public boolean populate(ResultSet rs, MRDDataSource mds, MRDAtom atom) throws
     SQLException, DataSourceException, BadValueException;

 //
 // Inner Classes
 //

 /**
  * This inner class serves as a default implementation of the
  * {@link AtomMapper} interface.
  */
 public class Default implements MRDAtomMapper {

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
   public MRDAtom map(ResultSet rs, MRDDataSource mds) throws SQLException,
       DataSourceException, BadValueException {
     MRDAtom atom = new MRDAtom();
     populate(rs, mds, atom);
     return atom;
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

   public boolean populate(ResultSet rs, MRDDataSource mds, MRDAtom atom) throws
       SQLException, DataSourceException, BadValueException {

     // Populate core data fields
     atom.setSource(mds.getSource_MRDConceptReport(rs.getString("ROOT_SOURCE")));
     atom.setSuppressible(rs.getString("SUPPRESSIBLE"));
     atom.setAuthority(mds.getAuthority(rs.getString("AUTHORITY")));
     atom.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
     atom.setInsertionDate(getDate(rs.getTimestamp("INSERTION_DATE")));
     
     // Populate other atom fields
     atom.setAtomId(rs.getInt("ATOM_ID"));
     atom.setLanguage(mds.getLanguage(rs.getString("LANGUAGE")));
     atom.setTermgroup(mds.getMRDTermgroup(rs.getString("SOURCE") + "/" + rs.getString("TTY"), atom.getSource()));     
     atom.setCode(rs.getString("CODE"));
     atom.setSUI(new StringIdentifier(rs.getString("SUI")));
     atom.setISUI(new StringIdentifier(rs.getString("ISUI")));
     atom.setLUI(new StringIdentifier(rs.getString("LUI")));
     if (rs.getString("LAST_RELEASE_CUI") != null) {
       atom.setLastReleaseCUI(rs.getString("LAST_RELEASE_CUI"));
       // Expand the rank
     }
     if (rs.getString("AUI") != null) {
         atom.setAUI(rs.getString("AUI"));
       }
     atom.setLastReleaseRank(new Rank.Default(rs.getInt("LAST_RELEASE_RANK")));
     StringBuffer extended_rank = new StringBuffer(30);
     
     extended_rank.append("9")
     	  .append(10000 + atom.getTermgroup().getRank().intValue())
         .append(rs.getInt("LAST_RELEASE_RANK"))
         .append(999999999 - atom.getSUI().intValue())
         .append(999999999 - (atom.getAUI() == null ? 1 : new Integer(atom.getAUI().substring(1)).intValue()))
         .append(10000000000L + atom.getAtomId());
     atom.setRank(new Rank.Default(extended_rank.toString()));
     if (rs.getString("CUI") != null) {
       atom.setCUI(rs.getString("CUI"));
     }
     if (rs.getString("SOURCE_CUI") != null) {
       atom.setSourceConceptIdentifier(rs.getString("SOURCE_CUI"));
     }
     if (rs.getString("SOURCE_AUI") != null) {
       atom.setSourceAuiIdentifier(rs.getString("SOURCE_AUI"));
     }
     if (rs.getString("SOURCE_DUI") != null) {
       atom.setSourceDescriptorIdentifier(rs.getString("SOURCE_DUI"));
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