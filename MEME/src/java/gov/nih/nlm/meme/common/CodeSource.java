/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  CodeSource
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This class represents a code qualified by a {@link Source}
 * that is used as a core data native identifier.
 * This class represents an <code>sg_id</code>
 * that uses a type of <code>CODE_SOURCE</code> or
 * <code>CODE_STRIPPED_SOURCE</code>.
 *
 * @see CoreData
 *
 * @author MEME Group
 */

public class CodeSource extends NativeIdentifier {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link CodeSource} from the specified
   * code and source values.
   * @param code a code value
   * @param source a source abbreviation qualifier
   */
  public CodeSource(String code, String source) {
    super(code, "CODE_STRIPPED_SOURCE", source, null, null);
  }

}
