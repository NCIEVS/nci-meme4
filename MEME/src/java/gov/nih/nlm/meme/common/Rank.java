/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Rank
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a rank.
 * This class exists to support the {@link Rankable} interface which allows
 * other classes to have ranks associated with them.
 * <p>
 * Ranks can be constructed as either <code>int</code> or <code>String</code>.
 * In the background, the rank is represented as a string, but when compared
 * if the rank was constructed as an int, it should be compared as an
 * int. The default implementation adheres to this.
 *
 * @see gov.nih.nlm.meme.common.Rankable
 *
 * @author MEME Group
 */

public interface Rank extends Comparable {

  //
  // Fields
  //

  public final static Rank EMPTY_RANK = new Default(0);

  /**
   * Returns the {@link String} representation.
   * @return the {@link String} representation
   */
  public String toString();

  /**
   * Returns the <code>int</code> rank.
   * @return the <code>int</code> rank
   */
  public int intValue();

  /**
   * Equality test.
   * @param object the object to compare to
   * @return <code>true</code> if equal, <code>false</code> otherwise
   */
  public boolean equals(Object object);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of
   * {@link Rank} interface.
   */
  public class Default implements Rank {

    //
    // Fields
    //

    private String rank = "0";
    private boolean intp = false;

    //
    // Constructors
    //

    /**
     * Instantiates a {@link Rank.Default}.
     * @param rank the rank
     */
    public Default(String rank) {
      if (rank == null) {
        this.rank = "0";
      } else {
        this.rank = rank;
      }
      intp = false;
    }

    /**
     * Instantiates a {@link Rank.Default}.
     * @param rank the rank
     */
    public Default(int rank) {
      this.rank = String.valueOf(rank);
      intp = true;
    }

    //
    // Overriden Object Methods
    //

    /**
     * Returns the <code>int</code> hashcode.
     * @return the <code>int</code> hashcode
     */
    public int hashCode() {
      return toString().hashCode();
    }

    //
    // Implementation of Rank interface
    //

    /**
     * Implements {@link Rank#toString()}.
     * @return An object {@link String} representation of rank.
     */
    public String toString() {
      return rank;
    }

    /**
     * Returns a  {@link Rank} holding the value of the specified String.
     * @param rank the rank
     * @return a new rank
     */
    public static Rank newRank(String rank) {
      return new Default(rank);
    }

    /**
     * Implements {@link Rank#intValue()}.
     * @return the <code>int</code> representation of rank.
     */
    public int intValue() {
      return Integer.valueOf(rank).intValue();
    }

    /**
     * Implements {@link Rank#equals(Object)}.
     * @param object the object to compare to
     * @return <code>true</code> equal,
     * <code>false</code> otherwise
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof Rank))) {
        return false;
      }
      return rank.equals(object.toString());
    }

    //
    // Implementation of Comparable interface
    //

    /**
     * Implements a natural sort ordering.
     * @param o object to compare to
     * @return <code>int</code> indicating relative sort ordering
     */
    public int compareTo(Object o) {
      Rank other = (Rank) o;
      if (intp) {
        return intValue() - other.intValue();
      } else {
        return rank.compareTo(other.toString());
      }
    }

  }
}
