/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  StringIdentifier
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents any kind of string identifier.
 * This includes SUI, LUI, and ISUI.
 *
 * @author MEME Group
 */

public class StringIdentifier extends Identifier.Default {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link StringIdentifier}.
   */
  public StringIdentifier() {
    super();
  }

  /**
   * Instantiates a {@link StringIdentifier} from the specified value.
   * @param identifier the value
   */
  public StringIdentifier(String identifier) {
    super(identifier);
  }

  //
  // Implementation of Identifier interface
  //

  /**
   * Returns the <codE>int</codE> value.
   * @return the <codE>int</codE> value
   */
  public int intValue() {
    return Integer.valueOf(toString().substring(1)).intValue();
  }

  /**
       * Returns a {@link StringIdentifier} holding the value of the specified String.
   * DO NOT REMOVE, used by serializtion to fake primitive value.
   * @param id the value
   * @return a new {@link StringIdentifier}
   */
  public static StringIdentifier newStringIdentifier(String id) {
    return new StringIdentifier(id);
  }
}
