/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  SourceRelationshipMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.NativeIdentifier;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Maps a {@link ResultSet} to  a {@link Relationship}. Used by $MEME_HOME/bin/insert.pl.
 *
 * @author MEME Group
 */
public class SourceRelationshipMapper extends RelationshipMapper.Default {

  //
  // Overrides RelationshipMapper.Default
  //

  /**
   * Maps the specified {@link ResultSet} to an {@link Relationship}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param rel the {@link Relationship}
   * @return <code>true</code> if the {@link ResultSet} was populated
   * @throws SQLException if sql process failed
   * @throws DataSourceException if populate failed
   * @throws BadValueException if failed due to invalid values
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, Relationship rel) throws
      SQLException, DataSourceException, BadValueException {

    // Populate source relationship data fields
    rel.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID_1")));
    rel.setRelatedConcept(new Concept.Default(rs.getInt("CONCEPT_ID_2")));
    rel.setAtom(new Atom.Default(rs.getInt("ATOM_ID_1")));
    rel.setRelatedAtom(new Atom.Default(rs.getInt("ATOM_ID_2")));
    rel.setName(rs.getString("RELATIONSHIP_NAME"));
    rel.setAttribute(rs.getString("RELATIONSHIP_ATTRIBUTE"));
    rel.setSource(mds.getSource(rs.getString("SOURCE")));
    rel.setSourceOfLabel(mds.getSource(rs.getString("SOURCE_OF_LABEL")));
    rel.setStatus(rs.getString("STATUS").charAt(0));
    rel.setGenerated(rs.getString("GENERATED_STATUS").equals("Y"));
    rel.setLevel(rs.getString("RELATIONSHIP_LEVEL").charAt(0));
    rel.setReleased(rs.getString("RELEASED").charAt(0));
    rel.setTobereleased(rs.getString("TOBERELEASED").charAt(0));
    rel.setIdentifier(new Identifier.Default(rs.getInt("RELATIONSHIP_ID")));
    rel.setSuppressible(rs.getString("SUPPRESSIBLE"));

    ResultSetMetaData rsmd = rs.getMetaData();
    if (rsmd.getColumnCount() > 15) {
      String sg_type_1 = rs.getString("SG_TYPE_1");
      if (sg_type_1 != null) {
        rel.setNativeIdentifier(
            new NativeIdentifier(rs.getString("SG_ID_1"),
                                 rs.getString("SG_TYPE_1"),
                                 rs.getString("SG_QUALIFIER_1"),
                                 null, null));
      }
      String sg_type_2 = rs.getString("SG_TYPE_2");
      if (sg_type_2 != null) {
        rel.setRelatedNativeIdentifier(
            new NativeIdentifier(rs.getString("SG_ID_2"),
                                 rs.getString("SG_TYPE_2"),
                                 rs.getString("SG_QUALIFIER_2"),
                                 null, null));
      }
    }
    if (rsmd.getColumnCount() > 21) {
      String source_rui = rs.getString("SOURCE_RUI");
      if (source_rui != null) {
        rel.setSourceIdentifier(new Identifier.Default(source_rui));
      }
      String relationship_group = rs.getString("RELATIONSHIP_GROUP");
      if (relationship_group != null) {
        rel.setGroupIdentifier(new Identifier.Default(relationship_group));
      }
    }
    return true;
  }
}
