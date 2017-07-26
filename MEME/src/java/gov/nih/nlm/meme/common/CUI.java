/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  CUI
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a unique identifier for a concept.
 * It is an implementation of Identifier because it is a kind of identifier
 * but generally, its {@link Rank} is not useful.
 *
 * @author MEME Group
 */

public class CUI implements Identifier {

  //
  // Fields
  //

  // The cui value
  private String cui = null;

  // The rank of the cui
  private Rank rank = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link CUI}.
   */
  public CUI() {};

  /**
   * Instantiates a {@link CUI} from the specified value.
   * @param cui the cui value
   * @throws IllegalArgumentException if the cui does not
   *    start with <code>C</code>
   */
  public CUI(String cui) {
    this.cui = cui;
    this.rank = new Rank.Default(cui);
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
    return cui;
  }

  /**
   * Instantiates and returns an {@link CUI} from the specified value.
   * DO NOT CHANGE, this is used by the XML serlizer to fake a primitive type
   * @param cui the CUI value
   * @return a {@link CUI}
   */
  public static CUI newCUI(String cui) {
    return new CUI(cui);
  }

  /**
   * Implements an equality function based on {@link String}
   * representations
   * @param object the {@link CUI} to compare to
   * @return <code>true</code> if the cuis are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof CUI))) {
      return false;
    }
    return cui.equals(object.toString());
  }

  //
  // Implementation of Comparable interface
  //

  /**
   * Implements an ordering function based on
   * {@link String} representations.
   * @param object the {@link CUI} to compare to
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
    return Integer.valueOf(cui.substring(1)).intValue();
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
    return "CUI";
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
