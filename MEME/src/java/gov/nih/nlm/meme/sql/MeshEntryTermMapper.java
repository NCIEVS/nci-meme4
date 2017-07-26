/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MeshEntryTermMapper
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps atoms to {@link MeshEntryTerm}s.
 *
 * @author MEME Group
 */

public class MeshEntryTermMapper extends AtomMapper.Default {

  /**
   * Maps the atom.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @return the {@link Atom}
   * @throws SQLException if sql process failed.
   * @throws DataSourceException if mapping failed.
   * @throws BadValueException if failed due to invalid values.
   */
  public Atom map(ResultSet rs, MEMEDataSource mds) throws SQLException,
      DataSourceException, BadValueException {

    Termgroup termgroup = mds.getTermgroup(rs.getString("TERMGROUP"));
    Source source = mds.getSource(rs.getString("SOURCE"));
    Atom atom = null;
    // Create mesh entry term atom
    if (source.getRootSourceAbbreviation().equals("MSH") &&
        !termgroup.getTermType().equals("MH") &&
        !termgroup.getTermType().equals("NM") &&
        !termgroup.getTermType().equals("TQ")) {
      atom = new MeshEntryTerm();
    } else {
      return null;
    }

    populate(rs, mds, atom);
    return atom;
  }

  /**
   * Populates the atom.
   * @param rs the {@link ResultSet}
   * @param mds the {@link MEMEDataSource}
   * @param atom the {@link Atom}
   * @return A <code>boolean</code> representation of ppopulate status value.
   * @throws SQLException if sql process failed.
   * @throws DataSourceException if mapping failed.
   * @throws BadValueException if failed due to invalid values.
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, Atom atom) throws
      SQLException, DataSourceException, BadValueException {

    if (! (atom instanceof MeshEntryTerm)) {
      return false;
    }

    super.populate(rs, mds, atom);

    if (!atom.getSource().isCurrent()) {
      return true;
    }
    Ticket ticket2 = Ticket.getEmptyTicket();
    ticket2.setReadAtoms(true);
    ticket2.setReadAtomNames(true);
    ticket2.setReadAttributes(true);
    ticket2.setExpandLongAttributes(true);
    Atom mh = null;
    // Extract the first letter of the code
    char code = atom.getCode().toString().charAt(0);
    switch (code) {
      case 'C':
        mh = mds.getAtomWithName(atom.getCode(),
                                 mds.getTermgroup(atom.getSource().
                                                  getSourceAbbreviation() +
                                                  "/NM"), ticket2);
        break;
      case 'D':
        mh = mds.getAtomWithName(atom.getCode(),
                                 mds.getTermgroup(atom.getSource().
                                                  getSourceAbbreviation() +
                                                  "/MH"), ticket2);
        break;
      case 'Q':
        mh = mds.getAtomWithName(atom.getCode(),
                                 mds.getTermgroup(atom.getSource().
                                                  getSourceAbbreviation() +
                                                  "/TQ"), ticket2);
        break;
    }
    if (mh != null) {
      ( (MeshEntryTerm) atom).setMainHeading(mh);
    }

    return true;
  }
}
