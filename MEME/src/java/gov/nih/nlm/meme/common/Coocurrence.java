/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Coocurrence
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents coocurrence object.
 *
 * @author MEME Group
 */

public class Coocurrence extends Relationship.Default {

  //
  // Fields
  //

  private int frequency = 0;

  //
  // Methods
  //

  /**
   * Returns the relationship name.
   * @return the relationship name
   */
  public String getType() {
    return getName();
  }

  /**
   * Returns the frequency.
   * @return the frequency
   */
  public int getFrequency() {
    return frequency;
  }

  /**
   * Sets the frequency.
   * @param frequency the frequency
   */
  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

}
