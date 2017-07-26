/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  ConceptMapper
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MRDAtom;
import gov.nih.nlm.meme.common.MRDConcept;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.ConceptMapper;
import gov.nih.nlm.meme.sql.MEMEDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Maps the {@link ResultSet} to a {@link Concept}.
 *
 * @author MEME Group
 */
public interface MRDConceptMapper {

  /**
   * Maps the specified {@link ResultSet} to a {@link Concept}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Concept}, or <code>null</code> if the criteria for this mapper are not met
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public MRDConcept map(ResultSet rs, MRDDataSource mds) throws SQLException,
      DataSourceException, BadValueException;

  /**
   * Populates the top-level {@link Concept} information.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param concept the {@link Concept} to populate
   * @return <code>true</code>
   * @throws SQLException if sql process failed
   * @throws DataSourceException if populate failed
   * @throws BadValueException if failed due to invalid values
   */
  public boolean populate(ResultSet rs, MRDDataSource mds, MRDConcept concept) throws
      SQLException, DataSourceException, BadValueException;

  //
  // Inner Classes
  //

  /**
   * Default implementation of the {@link ConceptMapper} interface.
   */
  public class Default implements MRDConceptMapper {

    //
    // Constructors
    //

    /**
     * Instantiates an default {@link ConceptMapper}.
     */
    public Default() {
      super();
    }

    //
    // Implementation of ConceptMapper interface
    //

    /**
     * Maps the specified {@link ResultSet} to a {@link Concept}.
     * @param rs the {@link ResultSet}
     * @param mds the {@link MEMEDataSource}
     * @return the {@link Concept}, or <code>null</code> if the criteria for this mapper are not met
     * @throws SQLException if sql process failed
     * @throws DataSourceException if mapping failed
     * @throws BadValueException if failed due to invalid values
     */
    public MRDConcept map(ResultSet rs, MRDDataSource mds) throws SQLException,
        DataSourceException, BadValueException {
    	MRDConcept concept = new MRDConcept();
      populate(rs, mds, concept);
      return concept;
    }

    /**
     * Populates the specified {@link Concept}.
     * @param rs the {@link ResultSet}
     * @param mds the {@link MEMEDataSource}
     * @param concept the {@link Concept} to populate
     * @return <code>true</code>
     * @throws SQLException if sql process failed
     * @throws DataSourceException if mapping failed
     * @throws BadValueException if failed due to invalid values
     */
    public boolean populate(ResultSet rs, MRDDataSource mds, MRDConcept concept) throws
        SQLException, DataSourceException, BadValueException {

      // Populate core data fields
      if (rs.getString("CUI") != null) {
        concept.setCUI(rs.getString("CUI"));
      }
      concept.setAuthority(mds.getAuthority(rs.getString("AUTHORITY")));
      concept.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
      concept.setInsertion_date(getDate(rs.getTimestamp("INSERTION_DATE")));
      concept.setPreferredAtom(new MRDAtom(rs.getInt("PREFERRED_ATOM_ID")));
      concept.setTobereleased(rs.getString("TOBERELEASED").charAt(0));
      concept.setRank(new Rank.Default(rs.getInt("RANK")));
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

