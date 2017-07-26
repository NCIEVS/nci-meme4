/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Identifier
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents any kind of
 * identifier.  This encompases things like SUIs, LUIs, CUIs, Codes, etc.
 *
 * @author MEME Group
 */

public interface Identifier extends Rankable, Comparable {

  //
  // Force Subclasses to Override these Object methods
  //
  /**
   * Returns a {@link String} representation.
   * @return a {@link String} representation
   */
  public String toString();

  /**
   * Equality function.
   * @param object the {@link Identifier} to compare to
   * @return <code>true</code> if the identifiers are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object object);

  //
  // Methods
  //

  /**
   * Returns an <code>int</code> representation.
   * @return an <code>int</code> representation
   */
  public int intValue();

  /**
   * Returns any qualifying information associated
   * with this identifier.  The actual value of an identifier
   * may be ambiguous within the <i>MID</i> (for example a code)
   * so a qualifier can restrict the set of things to which
   * the identifier applies (for example a code within a source).
   * @return the qualifier represented as an {@link String}.
   */
  public String getQualifier();

  /**
   * Returns the qualifier type.
   * Implementations of the interface should <i>always</i>
   * hard-code the value, so that each class represents a
   * type of qualifier.
   * @return the qualifier type
   */
  public String getType();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Identifier} interface.
   */
  public class Default implements Identifier {

    //
    // Fields
    //

    private String identifier = null;
    private Rank rank = null;
    private boolean intp = false;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Identifier}.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates a default {@link Identifier}
     * with the specified id value.
     * @param identifier the id value
     */
    public Default(String identifier) {
      this.identifier = identifier;
      this.rank = new Rank.Default(identifier);
    }

    /**
     * Instantiates a default {@link Identifier}
     * with the specified id value.
     * @param identifier the id value
     */
    public Default(int identifier) {
      this.identifier = String.valueOf(identifier);
      this.rank = new Rank.Default(identifier);
      intp = true;
    }

    //
    // Overridden Object methods
    //

    /**
     * Implements {@link Object#hashCode()}.
     */
    public int hashCode() {
      return toString().hashCode();
    }

    /**
     * Implements {@link Identifier#toString()}.
     */
    public String toString() {
      if (identifier == null) {
        return "";
      }
      return identifier;
    }

    /**
     * Returns a  {@link Identifier} holding the value of the specified String.
     */
    public static Identifier newIdentifier(String identifier) {
      return new Default(identifier);
    }

    /**
     * Implements {@link Identifier#equals(Object)}.
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof Identifier))) {
        return false;
      }
      return identifier.equals(object.toString());
    }

    //
    // Implementation of Comparable interface
    //

    /**
     * Implements an ordering function based on identifier value.
     * Identifiers constructed from integers are compared
     * with respect to <code>int</code> values, and identifiers
     * constructed from {@link String}s are compared with
     * respect to their {@link String} values.
     * @param o the {@link Identifier} to compare to
     * @return an <code>int</code> indicating the relative ordering
     */
    public int compareTo(Object o) {

      // Different if constructed as int or string
      if (intp) {
        return intValue() - ( (Identifier) o).intValue();
      } else {
        return identifier.compareTo(o.toString());
      }
    }

    //
    // Implementation of Identifier interface
    //

    /**
     * Implements {@link Identifier#intValue()}.
     */
    public int intValue() {
      if (identifier == null) {
        return 0;
      }
      return Integer.valueOf(identifier).intValue();
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
      return null;
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
}
