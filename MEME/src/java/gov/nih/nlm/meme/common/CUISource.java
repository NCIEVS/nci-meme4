/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  CUISource
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This class represents a CUI qualified by a {@link Source}
 * that is used as a core data native identifier.
 * This class represents an <code>sg_id</code>
 * that uses a type of <code>CUI_SOURCE</code> or
 * <code>CODE_STRIPPED_SOURCE</code>.
 *
 * @see CoreData
 *
 * @author MEME Group
 */

public class CUISource extends NativeIdentifier {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link CUISource} from the specified value and qualifier.
   * @param cui a CUI value
   * @param source a {@link Source} qualifier
   */
  public CUISource(String cui, String source) {
    super(cui, "CUI_STRIPPED_SOURCE", source, null, null);
  }

}
