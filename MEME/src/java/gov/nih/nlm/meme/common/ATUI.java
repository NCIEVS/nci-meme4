/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ATUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a unique identifier for an attribute.
 * It is defined by a set of attribute fields including, the RSAB, the source ATUI,
 * the attribute name, the attribute hashcode, the attribute level, and the
 * SG tuple defining what the attribute is connected to.
 *
 * It is an implementation of Identifier because it is a kind of identifier
 * but generally, its {@link Rank} is not useful.
 *
 * @author MEME Group
 */

public class ATUI implements Identifier {

  //
  // Fields
  //

  // The ATUI value
  private String atui = null;

  // The rank of the ATUI
  private Rank rank = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link ATUI}.
   */
  public ATUI() {};

  /**
   * Instantiates an {@link ATUI} with the specified value.
   * @param atui the ATUI value
   */
  public ATUI(String atui) {
    this.atui = atui;
    this.rank = new Rank.Default(atui);
  }

  //
  // Overridden Object Methods
  //

  /**
   * Returns an <code>int</code> hashcode.
   * @return an <code>int</code> hashcode
   */
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Returns a {@link String} representation.
   * @return a {@link String} representation
   */
  public String toString() {
    return atui;
  }

  /**
   * Instantiates and returns an {@link ATUI} from the specified value.
   * DO NOT CHANGE, this is used by the XML serializer to fake a primitive type
   * @param atui the ATUI value
   * @return an {@link ATUI}
   */
  public static ATUI newATUI(String atui) {
    return new ATUI(atui);
  }

  /**
   * Implements an equals function based on {@link String} representations.
   * @param object an {@link ATUI} to compare to
   * @return <code>true</code> if the objects are equal,
   *         <code>false</code> oherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof ATUI))) {
      return false;
    }
    return atui.equals(object.toString());
  }

  //
  // Implementation of Comparable interface
  //

  /**
   * Implements an ordering function based on {@link String} representations.
   * Note: this assumes that all ATUIs have the same length.
   * @param object an {@link ATUI} to compare to
   * @return an <code>int</code> indicating the relative ordering
   */
  public int compareTo(Object object) {
    return toString().compareTo(object.toString());
  }

  //
  // Implementation of Identifier interface
  //

  /**
   * Implements {@link Identifier#intValue()}.
   */
  public int intValue() {
    return Integer.valueOf(atui.substring(2)).intValue();
  }

  /**
   * Implements {@link Identifier#getQualifier()}.
   */
  public String getQualifier() {
    return null;
  }

  /**
   * Implements {@link Identifier#getType()}.
   */
  public String getType() {
    return "ATUI";
  }

  //
  // Implementation of Rankable interface
  //

  /**
   * Implements {@link Rankable#getRank()}.
   */
  public Rank getRank() {
    return rank;
  }

  /**
   * Implements {@link Rankable#setRank(Rank)}.
   */
  public void setRank(Rank rank) {
    this.rank = rank;
  }

}
