/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  SourceAttributeMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Maps a {@link ResultSet} to an {@link Attribute}.  Used by $MEME_HOME/bin/insert.pl.
 *
 * @author MEME Group
 */
public class SourceAttributeMapper extends AttributeMapper.Default {

  //
  // Overrides AttributeMapper.Default
  //

  /**
   * Maps the specified {@link ResultSet} to an {@link Attribute}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param attr the {@link Attribute}
   * @return <code>true</code> if the {@link ResultSet} was populated
   * @throws SQLException if sql process failed
   * @throws DataSourceException if populate failed
   * @throws BadValueException if failed due to invalid values
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, Attribute attr) throws
      SQLException, DataSourceException, BadValueException {

    // Populate source attribute data fields
    attr.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));
    attr.setAtom(new Atom.Default(rs.getInt("ATOM_ID")));
    attr.setIdentifier(new Identifier.Default(rs.getInt("ATTRIBUTE_ID")));
    attr.setLevel(rs.getString("ATTRIBUTE_LEVEL").charAt(0));
    attr.setName(rs.getString("ATTRIBUTE_NAME"));
    attr.setValue(rs.getString("ATTRIBUTE_VALUE"));
    attr.setSource(mds.getSource(rs.getString("SOURCE")));
    attr.setStatus(rs.getString("STATUS").charAt(0));
    attr.setGenerated(rs.getString("GENERATED_STATUS").equals("Y"));
    attr.setReleased(rs.getString("RELEASED").charAt(0));
    attr.setTobereleased(rs.getString("TOBERELEASED").charAt(0));
    attr.setSuppressible(rs.getString("SUPPRESSIBLE"));

    ResultSetMetaData rsmd = rs.getMetaData();
    if (rsmd.getColumnCount() > 12) {
      String sg_type = rs.getString("SG_TYPE");
      if (sg_type != null) {
        attr.setNativeIdentifier(
            new NativeIdentifier(rs.getString("SG_ID"),
                                 rs.getString("SG_TYPE"),
                                 rs.getString("SG_QUALIFIER"),
                                 null, null));
      }
    }
    if (rsmd.getColumnCount() > 15) {
      String source_atui = rs.getString("SOURCE_ATUI");
      if (source_atui != null) {
        attr.setSourceIdentifier(new Identifier.Default(source_atui));
      }
    }
    return true;
  }
}
