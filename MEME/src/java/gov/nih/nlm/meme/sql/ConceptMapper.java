/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  ConceptMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Maps the {@link ResultSet} to a {@link Concept}.
 *
 * @author MEME Group
 */
public interface ConceptMapper {

  /**
   * Maps the specified {@link ResultSet} to a {@link Concept}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Concept}, or <code>null</code> if the criteria for this mapper are not met
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public Concept map(ResultSet rs, MEMEDataSource mds) throws SQLException,
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
  public boolean populate(ResultSet rs, MEMEDataSource mds, Concept concept) throws
      SQLException, DataSourceException, BadValueException;

  //
  // Inner Classes
  //

  /**
   * Default implementation of the {@link ConceptMapper} interface.
   */
  public class Default implements ConceptMapper {

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
    public Concept map(ResultSet rs, MEMEDataSource mds) throws SQLException,
        DataSourceException, BadValueException {
      Concept concept = new Concept.Default();
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
    public boolean populate(ResultSet rs, MEMEDataSource mds, Concept concept) throws
        SQLException, DataSourceException, BadValueException {

      // Populate core data fields
      if (rs.getString("CUI") != null) {
        concept.setCUI(new CUI(rs.getString("CUI")));
      }
      concept.setStatus(rs.getString("STATUS").charAt(0));
      concept.setDead(rs.getString("DEAD").equals("Y"));
      concept.setAuthority(mds.getAuthority(rs.getString("AUTHORITY")));
      concept.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
      concept.setInsertionDate(getDate(rs.getTimestamp("INSERTION_DATE")));
      concept.setPreferredAtom(new Atom.Default(rs.getInt("PREFERRED_ATOM_ID")));
      concept.setReleased(rs.getString("RELEASED").charAt(0));
      concept.setTobereleased(rs.getString("TOBERELEASED").charAt(0));
      concept.setRank(new Rank.Default(rs.getInt("RANK")));
      concept.setEditingAuthority(mds.getAuthority(rs.getString(
          "EDITING_AUTHORITY")));
      concept.setEditingTimestamp(getDate(rs.getTimestamp("EDITING_TIMESTAMP")));
      concept.setReadTimestamp(new Date());

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
