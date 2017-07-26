/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  AtomMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
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
public interface AtomMapper {

  /**
   * Maps the specified {@link ResultSet} to an {@link Atom}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Atom} for the current entry in the result set
   * @throws SQLException if sql process failed.
   * @throws DataSourceException if mapping failed.
   * @throws BadValueException if failed due to invalid values.
   */
  public Atom map(ResultSet rs, MEMEDataSource mds) throws SQLException,
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
  public boolean populate(ResultSet rs, MEMEDataSource mds, Atom atom) throws
      SQLException, DataSourceException, BadValueException;

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link AtomMapper} interface.
   */
  public class Default implements AtomMapper {

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
    public Atom map(ResultSet rs, MEMEDataSource mds) throws SQLException,
        DataSourceException, BadValueException {
      Atom atom = new Atom.Default();
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
    public boolean populate(ResultSet rs, MEMEDataSource mds, Atom atom) throws
        SQLException, DataSourceException, BadValueException {

      // Populate core data fields
      atom.setSource(mds.getSource(rs.getString("SOURCE")));
      atom.setGenerated(rs.getString("GENERATED_STATUS").equals("Y"));
      atom.setDead(rs.getString("DEAD").equals("Y"));
      atom.setSuppressible(rs.getString("SUPPRESSIBLE"));
      atom.setStatus(rs.getString("STATUS").charAt(0));
      atom.setAuthority(mds.getAuthority(rs.getString("AUTHORITY")));
      atom.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
      atom.setInsertionDate(getDate(rs.getTimestamp("INSERTION_DATE")));
      atom.setReleased(rs.getString("RELEASED").charAt(0));
      atom.setTobereleased(rs.getString("TOBERELEASED").charAt(0));

      // Populate other atom fields
      atom.setIdentifier(new Identifier.Default(rs.getInt("ATOM_ID")));
      atom.setLanguage(mds.getLanguage(rs.getString("LANGUAGE")));
      atom.setTermgroup(mds.getTermgroup(rs.getString("TERMGROUP")));
      atom.setCode(new Code(rs.getString("CODE")));
      atom.setSUI(new StringIdentifier(rs.getString("SUI")));
      atom.setISUI(new StringIdentifier(rs.getString("ISUI")));
      atom.setLUI(new StringIdentifier(rs.getString("LUI")));
      if (rs.getString("LAST_RELEASE_CUI") != null) {
        atom.setLastReleaseCUI(new CUI(rs.getString("LAST_RELEASE_CUI")));
        // last_release_rank column is currently being ignored

        // Expand the rank
      }
      StringBuffer extended_rank = new StringBuffer(30);
      extended_rank.append(rs.getString("RANK").substring(0, 1))
          .append(10000 + atom.getTermgroup().getRank().intValue())
          .append(rs.getInt("LAST_RELEASE_RANK"))
          .append(atom.getSUI().toString())
          .append(10000000000L + atom.getIdentifier().intValue());
      atom.setRank(new Rank.Default(extended_rank.toString()));
      if (rs.getString("LAST_ASSIGNED_CUI") != null) {
        atom.setLastAssignedCUI(new CUI(rs.getString("LAST_ASSIGNED_CUI")));
      }
      if (rs.getString("SOURCE_CUI") != null) {
        atom.setSourceConceptIdentifier(new Identifier.Default(
            rs.getString("SOURCE_CUI")));
      }
      if (rs.getString("SOURCE_AUI") != null) {
        atom.setSourceIdentifier(new Identifier.Default(
            rs.getString("SOURCE_AUI")));
      }
      if (rs.getString("SOURCE_DUI") != null) {
        atom.setSourceDescriptorIdentifier(new Identifier.Default(
            rs.getString("SOURCE_DUI")));
      }
      if (rs.getString("AUI") != null) {
        atom.setAUI(new AUI(rs.getString("AUI")));
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
