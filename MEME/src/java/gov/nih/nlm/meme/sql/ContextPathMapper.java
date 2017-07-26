/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  ContextPathMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The <code>ContextPathMapper</code> creates context path data object.
 *
 * @author MEME Group
 */

public class ContextPathMapper extends AtomMapper.Default {

  //
  // Overrides AtomMapper.Default
  //

  /**
   * Implements {@link AtomMapper#populate(ResultSet, MEMEDataSource, Atom)}.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param atom the {@link Atom}
   * @return A <code>boolean</code> representation of populate status value.
   * @throws SQLException if sql process failed.
   * @throws DataSourceException if populate failed.
   * @throws BadValueException if failed due to invalid values.
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, Atom atom) throws
      SQLException, DataSourceException, BadValueException {

    // Populate atom object
    // We don't care about all of the data here
    // at some point in the future we may
    atom.setIdentifier(new Identifier.Default(rs.getInt("ATOM_ID")));
    atom.setString(rs.getString("STRING"));
    atom.setCode(new Code(rs.getString("CODE")));
    atom.setLUI(new StringIdentifier(rs.getString("LUI")));
    atom.setSUI(new StringIdentifier(rs.getString("SUI")));
    atom.setISUI(new StringIdentifier(rs.getString("ISUI")));
    atom.setSource(mds.getSource(rs.getString("ATOM_SOURCE")));
    atom.setTermgroup(mds.getTermgroup(rs.getString("TERMGROUP")));
    atom.setConcept(new Concept.Default(rs.getInt("CONCEPT_ID")));
    atom.setAUI(new AUI(rs.getString("AUI")));
    return true;
  }
}
