/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  ConceptMappingMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.ConceptMapping;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Maps a {@link ResultSet} to a {@link ConceptMapping}.
 *
 * @author MEME Group
 */
public class ConceptMappingMapper {

  //
  // Fields
  //

  /**
   * Maps the specified {@link ResultSet} to a {@link ConceptMapping}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link ConceptMapping}, or <code>null</code> if the criteria for this mapper are not met
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public ConceptMapping map(ResultSet rs, MEMEDataSource mds) throws
      SQLException,
      DataSourceException, BadValueException {
    ConceptMapping cm = new ConceptMapping.Default();
    populate(rs, mds, cm);
    return cm;
  }

  /**
   * Populates the relase version, mapping reason, and CUI values of the {@link ConceptMapping}.
   * Allows superclass to do rest of the work.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param cm the {@link ConceptMapping} to populate
   * @return <code>true</code>
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, ConceptMapping cm) throws
      SQLException, DataSourceException, BadValueException {

    // Populate concept mapping fields

    cm.setIdentifier(new Identifier.Default(rs.getInt("MAP_ID")));
    cm.setRank(new Rank.Default(rs.getInt("RANK")));
    cm.setGenerated(rs.getString("GENERATED_STATUS").equals("Y"));
    cm.setSource(mds.getSource(rs.getString("SOURCE")));
    cm.setDead(rs.getString("DEAD").equals("Y"));
    cm.setStatus(rs.getString("STATUS").charAt(0));
    cm.setSuppressible(rs.getString("SUPPRESSIBLE"));
    cm.setAuthority(mds.getAuthority(rs.getString("AUTHORITY")));
    cm.setTimestamp(getDate(rs.getTimestamp("TIMESTAMP")));
    cm.setInsertionDate(getDate(rs.getTimestamp("INSERTION_DATE")));
    cm.setReleased(rs.getString("RELEASED").charAt(0));
    cm.setTobereleased(rs.getString("TOBERELEASED").charAt(0));

    cm.setCUI(new CUI(rs.getString("CUI")));
    cm.setBirthVersion(rs.getString("BIRTH_VERSION"));
    cm.setDeathVersion(rs.getString("DEATH_VERSION"));
    cm.setMappedToCui(new CUI(rs.getString("CUI")));
    cm.setRelationshipName(rs.getString("RELATIONSHIP_NAME"));
    cm.setRelationshipAttribute(rs.getString("RELATIONSHIP_ATTRIBUTE"));
    cm.setMappingReason(rs.getString("MAP_REASON"));
    cm.setAlmostSY(rs.getString("ALMOST_SY").equals("Y"));

    return true;
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
