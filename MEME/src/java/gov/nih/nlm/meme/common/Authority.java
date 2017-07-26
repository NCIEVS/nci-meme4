/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Authority
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents an agent responsible for some activity within
 * the <i>MID</i>.
 *
 * @author MEME Group
 */

public interface Authority extends Rankable {

  /**
   * Returns a {@link String} representation.
   * @return a {@link String} representation
   */
  public String toString();

  /**
   * Equality test.
   * @param object an {@link Authority} to compare to
   * @return <code>true</code> if the {@link Authority} objects are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object object);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Authority} interface.
   */
  public class Default implements Authority {

    //
    // Fields
    //

    private String authority = null;
    private Rank rank = Rank.EMPTY_RANK;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Authority} object.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates an default {@link Authority} object from the specified
     * value.
     * @param authority a {@link String} representation of an authority
     */
    public Default(String authority) {
      this.authority = authority;
      this.rank = Rank.EMPTY_RANK;
    }

    /**
     * Instantiates an default {@link Authority} object from the specified
     * value and rank.
     * @param authority a {@link String} representation of an authority
     * @param rank the rank
     */
    public Default(String authority, Rank rank) {
      this.authority = authority;
      this.rank = rank;
    }

    //
    // Overriden Object Methods
    //

    /**
     * Returns an <code>int</code> hashcode.
     * @return an <code>int</code> hashcode
     */
    public int hashCode() {
      return toString().hashCode();
    }

    //
    // Implementation of Authority interface
    //

    /**
     * Implements {@link Authority#toString()}.
     */
    public String toString() {
      return authority;
    }

    /**
     * Returns a  {@link Authority} holding the value of the specified String.
     */
    public static Authority newAuthority(String authority) {
      return new Default(authority);
    }

    /**
     * Implements {@link Authority#equals(Object)}.
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof Authority))) {
        return false;
      }
      return authority.equals(object.toString());
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
