/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  BinaryCheckData
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Used to track data values used by a {@link BinaryDataIntegrityCheck}.
 * It roughly corresponds to table <code>ic_pair</code>.
 *
 * @author MEME Group
 */

public class BinaryCheckData {

  //
  // Fields
  //

  private String name, type_1, type_2, value_1, value_2 = null;
  private boolean negation = false;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link BinaryCheckData} from the specified values.
   * @param name the check name
   * @param type_1 the type of the first value
   * @param type_2 the type of the second value
   * @param value_1 the first value
   * @param value_2 the second value
   * @param negation indicates whether or not the data should matc
       * the values (<code>true</code>), or not match the values (<code>false</code>)
   */
  public BinaryCheckData(String name, String type_1, String type_2,
                         String value_1, String value_2, boolean negation) {
    this.name = name;
    this.type_1 = type_1;
    this.type_2 = type_2;
    this.value_1 = value_1;
    this.value_2 = value_2;
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
   * Returns the type of the first value.
   * @return the type of the first value.
   */
  public String getType1() {
    return type_1;
  }

  /**
   * Returns the type of the second value.
   * @return the type of the second value
   */
  public String getType2() {
    return type_2;
  }

  /**
   * Returns the first value.
   * @return the first value
   */
  public String getValue1() {
    return value_1;
  }

  /**
   * Returns the second value.
   * @return the second value
   */
  public String getValue2() {
    return value_2;
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
