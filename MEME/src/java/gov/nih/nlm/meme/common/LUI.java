/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  LUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents the {@link MEMEString} lexical class identifier.
 *
 * @author MEME Group
 */

public class LUI extends StringIdentifier {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link LUI}.
   */
  public LUI() {
    super();
  }

  /**
   * Instantiates a {@link LUI}
   * with the specified LUI value
   * @param identifier A LUI {@link Identifier} value
   */
  public LUI(String identifier) {
    super(identifier);
  }

  //
  // Implementation of Identifier interface
  //

  /**
   * Returns the <code>int</code> part of the LUI.
   * @return the <code>int</code> part of the LUI.
   */
  public int intValue() {
    return super.intValue();
  }

  /**
   * Instantiates and returns an {@link LUI} from the specified value.
   * DO NOT CHANGE, this is used by the XML serlizer to fake a primitive type
   * @param lui the LUI value
   * @return an {@link LUI}
   */
  public static LUI newLUI(String lui) {
    return new LUI(lui);
  }
}
