/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  CodeTermgroup
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This class represents a code qualified by a {@link Termgroup}
 * that is used as a core data native identifier.
 * This class represents an <code>sg_id</code>
 * that uses a type of <code>CODE_TERMGROUP</code> or
 * <code>CODE_STRIPPED_TERMGROUP</code>.
 *
 * @see CoreData
 *
 * @author MEME Group
 */

public class CodeTermgroup extends NativeIdentifier {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link CodeTermgroup} from the
   * specified code and termgroup values.
   * @param code the code value
   * @param termgroup the termgroup qualifier
   */
  public CodeTermgroup(String code, String termgroup) {
    super(code, "CODE_TERMGROUP", termgroup, null, null);
  }

}
