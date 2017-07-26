/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  SUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a case-insensitive {@link MEMEString} identifier.
 *
 * @author MEME Group
 */

public class SUI extends StringIdentifier {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link SUI}.
   */
  public SUI() {
    super();
  }

  /**
   * Instantiates a {@link SUI} from the specified value.
   * @param identifier the SUI value
   */
  public SUI(String identifier) {
    super(identifier);
  }

  //
  // Implementation of Identifier interface
  //

  /**
   * Returns the <code>int</code> representation of the SUI.
   * @return the <code>int</code> representation of the SUI
   */
  public int intValue() {
    return super.intValue();
  }

  /**
   * Instantiates and returns an {@link SUI} from the specified value.
   * DO NOT CHANGE, this is used by the XML serlizer to fake a primitive type
   * @param sui the SUI value
   * @return an {@link SUI}
   */
  public static SUI newSUI(String sui) {
    return new SUI(sui);
  }
}
