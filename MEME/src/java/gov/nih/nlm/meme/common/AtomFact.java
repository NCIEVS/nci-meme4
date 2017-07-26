/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomFact
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a connection between two {@link Atom}s that is not
 * a formal relationship. It should be clear from the subinterfaces
 * what these things are.
 *
 * @author MEME Group
 */

public interface AtomFact extends Rankable, Comparable {

  /**
   * Returns the {@link Atom} that owns this fact.
   * @return the {@link Atom} that owns this fact
   */
  public Atom getAtom();

  /**
   * Sets the {@link Atom} that owns this fact.
   * @param atom The {@link Atom} that owns this fact
   */
  public void setAtom(Atom atom);

  /**
   * Returns the {@link Atom} connected by this fact.
   * @return the {@link Atom} connected by this fact
   */
  public Atom getConnectedAtom();

  /**
   * Sets the {@link Atom} connected by this fact.
   * @param atom the {@link Atom} connected by this fact.
   */
  public void setConnectedAtom(Atom atom);

  /**
   * Returns the {@link Source} of this fact.
   * @return the {@link Source} of this fact
   */
  public Source getSource();

  /**
   * Sets the {@link Source} of this fact.
   * @param source the {@link Source} of this fact
   */
  public void setSource(Source source);

  /**
   * Forces implementations to define an equality test.
   * @return a <code>true</code> if the object is equal
   * @param object an {@link Object}
   */
  public boolean equals(Object object);

  /**
   * Forces implementations to define a hashing function.
   * @return an <code>int</code> hashcode
   */
  public int hashCode();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link AtomFact} interface.  It is abstract because it
   * is not inherently useful and should be subclassed.
   */
  public abstract class Default implements AtomFact {

    //
    // Fields
    //
    protected Atom atom_1 = null;
    protected Atom atom_2 = null;
    protected Source source = null;
    protected Rank rank = null;

    //
    // Overridden Object methods
    //

    /**
     * Returns a string representation of a fact.
     * @return a string representation of a fact
     */
    public String toString() {
      StringBuffer sb = new StringBuffer(100);
      sb.append(atom_1.getIdentifier());
      sb.append(" => ");
      sb.append(atom_2.getIdentifier());
      sb.append(" : ");
      sb.append(source);
      return sb.toString();
    }

    /**
     * Tests all fields for equality.
     * @param object an {@link Object} to compare to
     * @return <code>true</code> if the objects are equal,
     *         <code>false</code> otherwise
     */
    public boolean equals(Object object) {
      if (object != null && object instanceof AtomFact) {
        AtomFact fact = (AtomFact) object;
        if (atom_1 == null || atom_2 == null ||
            source == null) {
          return false;
        }
        return atom_1.equals(fact.getAtom()) &&
            atom_2.equals(fact.getConnectedAtom()) &&
            source.equals(fact.getSource());
      }
      return false;
    }

    /**
     * Hash function.
     * @return An <code>int</code> hashcode
     */
    public int hashCode() {
      return toString().hashCode();
    }

    //
    // Implementation of AtomFact interface
    //

    /**
     * Returns the {@link Atom} that owns this fact.
     * @return the {@link Atom} that owns this fact
     */
    public Atom getAtom() {
      return atom_1;
    }

    /**
     * Sets the {@link Atom} that owns this fact.
     * @param atom the {@link Atom} that owns this fact
     */
    public void setAtom(Atom atom) {
      atom_1 = atom;
    };

    /**
     * Returns the {@link Atom} connected by this fact.
     * @return the {@link Atom} connected by this fact
     */
    public Atom getConnectedAtom() {
      return atom_2;
    }

    /**
     * Sets the {@link Atom} connected by this fact.
     * @param atom the {@link Atom} connected by this fact
     */
    public void setConnectedAtom(Atom atom) {
      atom_2 = atom;
    }

    /**
     * Returns the {@link Source} of this fact.
     * @return the {@link Source} of this fact
     */
    public Source getSource() {
      return source;
    }

    /**
     * Sets the {@link Source} of this fact.
     * @param source the {@link Source} of this fact
     */
    public void setSource(Source source) {
      this.source = source;
    }

    /**
     * Returns the {@link Rank} of this fact.
     * @return the {@link Rank} of this fact
     */
    public Rank getRank() {
      return rank;
    }

    /**
     * Sets the {@link Rank} of this fact.
     * @param rank the {@link Rank} of this fact
     */
    public void setRank(Rank rank) {
      this.rank = rank;
    }

    //
    // Comparable Interface
    //

    /**
     * Implements an ordering function based on rank.
     * @param obj an {@link Object} to compare to
     * @return an <code>int</code> representing the relative ordering
     */
    public int compareTo(Object obj) {
      // We are going to compare based on ranks.
      if (! (obj instanceof AtomFact) || rank == null) {
        return 0;
      }
      return rank.compareTo( ( (AtomFact) obj).getRank());
    }

  }
}
