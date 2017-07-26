/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Locator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * <B>NOT USED</b>
 *
 * @author MEME Group
 */

public class Locator extends Attribute.Default {

  //
  // Fields
  //

  public String frequency = null;
  public SUI sui = null;
  public String str = null;
  public Identifier record_id = null;

  //
  // Methods
  //

  /**
   * Returns the frequency.
   * @return the frequency
   */
  public String getFrequency() {
    return this.frequency;
  }

  /**
   * Sets the frequency.
   * @param frequency the frequency
   */
  public void setFrequency(String frequency) {
    this.frequency = frequency;
  }

  /**
   * Returns the <code>SUI</code>.
   * @return the object {@link SUI}
   */
  public SUI getSUI() {
    return this.sui;
  }

  /**
   * Sets the <code>SUI</code>.
   * @param sui an object {@link SUI}
   */
  public void setSUI(SUI sui) {
    this.sui = sui;
  }

  /**
   * Returns the string.
   * @return the string
   */
  public String getString() {
    return this.str;
  }

  /**
   * Sets the string.
   * @param str the string
   */
  public void setString(String str) {
    this.str = str;
  }

  /**
   * Returns the record identifier.
   * @return the object {@link Identifier}
   */
  public Identifier getRecordIdentifier() {
    return this.record_id;
  }

  /**
   * Sets the record identifier.
   * @param record_id an object {@link Identifier}
   */
  public void setRecordIdentifier(Identifier record_id) {
    this.record_id = record_id;
  }
}
