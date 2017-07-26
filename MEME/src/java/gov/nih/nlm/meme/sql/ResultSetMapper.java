/*****************************************************************************
 * Package: gov.nih.nlm.meme.sql
 * Object:  ResultSetMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.exception.MEMEException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a {@link ResultSet} to an object.
 *
 * @author MEME Group
 */
public interface ResultSetMapper {

  //
  // Methods
  //

  /**
   * Maps the specified {@link ResultSet} to the.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the object represented by the top of the {@link ResultSet}
   * @throws SQLException because we are handling a result set
   * @throws MEMEException if mapping failed
   */
  public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
      MEMEException;

}
