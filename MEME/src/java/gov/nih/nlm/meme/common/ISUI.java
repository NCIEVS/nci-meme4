/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ISUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a case-insensitive {@link MEMEString} identifier.
 *
 * @author MEME Group
 */

public class ISUI extends StringIdentifier {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link ISUI}.
   */
  public ISUI() {
    super();
  }

  /**
   * Instantiates an {@link ISUI} with the
   * specified isui value.
   * @param identifier the isui value
   */
  public ISUI(String identifier) {
    super(identifier);
  }

  //
  // Implementation of Identifier interface
  //

  /**
   * Returns the <code>int</code> part of the isui value.
   * @return the <code>int</code> part of the isui value
   */
  public int intValue() {
    return super.intValue();
  }

  /**
   * Instantiates and returns an {@link ISUI} from the specified value.
   * DO NOT CHANGE, this is used by the XML serlizer to fake a primitive type
   * @param isui the ISUI value
   * @return an {@link ISUI}
   */
  public static ISUI newISUI(String isui) {
    return new ISUI(isui);
  }

}
