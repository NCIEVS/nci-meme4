/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  RUI
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

public class RUI implements Identifier {

  //
  // Fields
  //

  // The RUI value
  private String rui = null;

  // The rank of the RUI
  private Rank rank = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link RUI}.
   */
  public RUI() {};

  /**
   * Instantiates an {@link RUI} with the specified value.
   * @param rui the RUI value
   * @throws IllegalArgumentException if the RUI does not
   *    start with <code>A</code>.
   */
  public RUI(String rui) {
    this.rui = rui;
    this.rank = new Rank.Default(rui);
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
    return rui;
  }

  /**
   * Instantiates and returns an {@link RUI} from the specified value.
   * DO NOT CHANGE, this is used during serialization to treat this like
   * a primitive.
   * @param rui the RUI value
   * @return an {@link RUI}
   */
  public static RUI newRUI(String rui) {
    return new RUI(rui);
  }

  /**
   * Equality function based on {@link String} representations.
   * @param crui the object to compare to
   * @return <code>true</code> if the objects are equal,
   *         <code>false</code> oherwise
   */
  public boolean equals(Object crui) {
    if ( (crui == null) || (! (crui instanceof RUI))) {
      return false;
    }
    return rui.equals(crui.toString());
  }

  //
  // Implementation of Comparable interface
  //

  /**
   * Comparison function based on {@link String} representations.
   * Note: this assumes that all RUIs have the same length.
   * @param crui the {@link RUI} to compare to
   * @return an <code>int</code> indicating the relative ordering
   */
  public int compareTo(Object crui) {
    return rui.compareTo(crui.toString());
  }

  //
  // Implementation of Identifier interface
  //

  /**
   * Implements {@link Identifier#intValue()}.
   * @return the int value
   */
  public int intValue() {
    return Integer.valueOf(rui.substring(1)).intValue();
  }

  /**
   * Implements {@link Identifier#getQualifier()}.
   * @return the qualifier
   */
  public String getQualifier() {
    return null;
  }

  /**
   * Implements {@link Identifier#getType()}.
   * @return the type
   */
  public String getType() {
    return "RUI";
  }

  //
  // Implementation of Rankable interface
  //

  /**
   * Implements {@link Rankable#getRank()}.
   * @return the rank
   */
  public Rank getRank() {
    return rank;
  }

  /**
   * Implements {@link Rankable#setRank(Rank)}.
   * @param rank the rank
   */
  public void setRank(Rank rank) {
    this.rank = rank;
  }

}
