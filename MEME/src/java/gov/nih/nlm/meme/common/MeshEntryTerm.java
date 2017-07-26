/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MeshEntryTerm
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a <code>MSH</code> entry term {@link Atom}.
 *
 * @author MEME Group
 */

public class MeshEntryTerm extends Atom.Default {

  //
  // Fields
  //

  private Atom mesh_heading = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link MeshEntryTerm}.
   */
  public MeshEntryTerm() {
    super();
  }

  /**
   * Instantiates a {@link MeshEntryTerm} with
   * the specified atom id.
   * @param atom_id the atom id
   */
  public MeshEntryTerm(int atom_id) {
    super(atom_id);
  }

  //
  // Methods
  //

  /**
   * Sets the MeSH <i>main heading</i> {@link Atom}.
   * @param atom the MeSH <i>main heading</i> {@link Atom}
   */
  public void setMainHeading(Atom atom) {
    this.mesh_heading = atom;
  }

  /**
   * Returns the MeSH <i>main heading</i> {@link Atom}.
   * @return atom the MeSH <i>main heading</i> {@link Atom}
   */
  public Atom getMainHeading() {
    return mesh_heading;
  }

}
