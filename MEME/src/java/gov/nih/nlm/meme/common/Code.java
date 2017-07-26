/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Code
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents the code of an {@link Atom}.
 * When used as native identifiers, codes are often
 * associated with qualifying information.  The various
 * subclasses supply mechanisms to specify the
 * qualifying information that can be associated with a code.
 *
 * @author MEME Group
 */

public class Code extends Identifier.Default {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link Code} with the specified value.
   * @param code the code value
   */
  public Code(String code) {
    super(code);
  }

  /**
   * Returns a  {@link Code} holding the value of the specified String.
   * @param code an object {@link Code}
   * @return the new code
   */
  public static Code newCode(String code) {
    return new Code(code);
  }
}
