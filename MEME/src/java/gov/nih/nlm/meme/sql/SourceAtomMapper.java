/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  SourceAtomMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Maps a {@link ResultSet} to {@link Atom}. Used by $MEME_HOME/bin/insert.pl.
 *
 * @author MEME Group
 */
public class SourceAtomMapper extends AtomMapper.Default {

  //
  // Overrides AtomMapper.Default
  //

  /**
   * Maps the {@link ResultSet} to an {@link Atom}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param atom the {@link Atom}
   * @return <code>true</code> if the {@link ResultSet} was populated
   * @throws SQLException if sql process failed
   * @throws DataSourceException if populate failed
   * @throws BadValueException if failed due to invalid values
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, Atom atom) throws
      SQLException, DataSourceException, BadValueException {

    // Populate source atom data fields
    atom.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));
    atom.setIdentifier(new Identifier.Default(rs.getInt("ATOM_ID")));
    atom.setString(rs.getString("ATOM_NAME"));
    atom.setTermgroup(mds.getTermgroup(rs.getString("TERMGROUP")));
    atom.setSource(mds.getSource(rs.getString("SOURCE")));
    atom.setCode(new Code(rs.getString("CODE")));
    atom.setStatus(rs.getString("STATUS").charAt(0));
    atom.setGenerated(rs.getString("GENERATED_STATUS").equals("Y"));
    atom.setReleased(rs.getString("RELEASED").charAt(0));
    atom.setTobereleased(rs.getString("TOBERELEASED").charAt(0));
    atom.setSuppressible(rs.getString("SUPPRESSIBLE"));

    ResultSetMetaData rsmd = rs.getMetaData();
    if (rsmd.getColumnCount() > 11) {
      String source_aui = rs.getString("SOURCE_AUI");
      String source_cui = rs.getString("SOURCE_CUI");
      String source_dui = rs.getString("SOURCE_DUI");
      if (source_aui != null) {
        atom.setSourceIdentifier(new Identifier.Default(source_aui));
      }
      if (source_cui != null) {
        atom.setSourceConceptIdentifier(new Identifier.Default(source_cui));
      }
      if (source_dui != null) {
        atom.setSourceDescriptorIdentifier(new Identifier.Default(source_dui));
      }
    }

    return true;
  }
}
