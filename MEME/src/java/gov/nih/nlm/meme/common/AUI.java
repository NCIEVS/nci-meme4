/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a unique identifier for an atom.
 * This roughly corresponds to a SUI, versionless source, term-type, and
 * a code.
 *
 * It is an implementation of Identifier because it is a kind of identifier
 * but generally, its {@link Rank} is not useful.
 *
 * @author MEME Group
 */

public class AUI implements Identifier {

  //
  // Fields
  //

  // The AUI value
  private String aui = null;

  // The rank of the AUI
  private Rank rank = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AUI}.
   */
  public AUI() {};

  /**
   * Instantiates an {@link AUI} with the specified value.
   * @param aui the AUI value
   */
  public AUI(String aui) {
    this.aui = aui;
    this.rank = new Rank.Default(aui);
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
    return aui;
  }

  /**
   * Instantiates and returns an {@link AUI} from the specified value.
   * DO NOT CHANGE, this is used by the XML serializer to fake a primitive type
   * @param aui the AUI value
   * @return an {@link AUI}
   */

  public static AUI newAUI(String aui) {
    return new AUI(aui);
  }

  /**
   * Implements an equals function based on {@link String} representations.
   * @param object an {@link AUI} to compare to
   * @return <code>true</code> if the objects are equal,
   *         <code>false</code> oherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof AUI))) {
      return false;
    }
    return aui.equals(object.toString());
  }

  //
  // Implementation of Comparable interface
  //

  /**
   * Implements an ordering function based on {@link String} representations.
   * Note: this assumes that all AUIs have the same length.
   * @param object an {@link AUI} to compare to
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
    return Integer.valueOf(aui.substring(1)).intValue();
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
    return "AUI";
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
