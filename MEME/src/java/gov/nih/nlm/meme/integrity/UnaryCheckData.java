/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  UnaryCheckData
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Used to track data values used by a {@link UnaryDataIntegrityCheck}.
 * It roughly corresponds to table <code>ic_single</code>.
 *
 * @author MEME Group
 */

public class UnaryCheckData {

  //
  // Fields
  //

  private String name, type, value = null;
  private boolean negation = false;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link BinaryCheckData} from the specified values.
   * @param name the check name
   * @param type the type of the value
   * @param value the value
   * @param negation indicates whether or not the data should matc
       * the values (<code>true</code>), or not match the values (<code>false</code>)
   */
  public UnaryCheckData(String name, String type, String value,
                        boolean negation) {
    this.name = name;
    this.type = type;
    this.value = value;
    this.negation = negation;
  }

  //
  // Methods
  //

  /**
   * Returns the check name.
   * @return the check name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the check type.
   * @return the check type
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the check value.
   * @return the check value
   */
  public String getValue() {
    return value;
  }

  /**
   * Indicates whether the data should be matched or not.
   * @return <code>true</code> if the data should be matched,
   * <code>false</code> otherwise
   */
  public boolean isNegated() {
    return negation;
  }
}
