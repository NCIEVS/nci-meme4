/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MapSetMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a mechanism for creating a {@link MapSet} objects
 * when concepts are being created.
 *
 * @author MEME Group
 */
public class MapSetMapper extends ConceptMapper.Default {

  /**
   * Maps the parameters to a {@link MapSet}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return a {@link MapSet}
   * @throws SQLException if sql process failed
   * @throws DataSourceException if mapping failed
   * @throws BadValueException if failed due to invalid values
   */
  public Concept map(ResultSet rs, MEMEDataSource mds) throws SQLException,
      DataSourceException, BadValueException {
    return new MapSet.Default();
  };

}
