/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Termgroup
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.StringTokenizer;

/**
 * Generically represents a termgroup, or a {@link Source}, term-type tuple.
 *
 * @author MEME Group
 */
public interface Termgroup extends Rankable, Comparable {

  /**
   * Returns the {@link Source}.
   * @return the {@link Source}
   */
  public Source getSource();

  /**
   * Sets the {@link Source}.
   * @param source the {@link Source}
   */
  public void setSource(Source source);

  /**
   * Returns the term type.
   * @return the term type
   */
  public String getTermType();

  /**
   * Sets the term type.
   * @param termtype the term type
   */
  public void setTermType(String termtype);

  /**
   * Returns the {@link String} representation.
   * @return the {@link String} representation
   */
  public String toString();

  /**
   * Forces implementations to have equality function.
   * @param object the object to compare to
   * @return <code>true</code> if equal, <code>false</code> otherwise
   */
  public boolean equals(Object object);

  /**
   * Indicates whether or not this data is suppressible.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isSuppressible();

  /**
   * Sets the suppressibility value.
   * @param suppressible the suppressibility value
   */
  public void setSuppressible(String suppressible);

  /**
   * Return the suppressibility value.
   * @return the suppressibility value
   */
  public String getSuppressible();

  /**
   * Indicates whether or not this is on the exclude list.
       * @return <code>true</code> if this is excluded; <code>false</code> otherwise.
   */
  public boolean exclude();

  /**
   * Sets the flag indicating whether or not this is excluded.
   * @param flag the "exclude list" flag value
   */
  public void setExclude(boolean flag);

  /**
   * Indicates whether or not this is on the norm exclude list.
   * @return <code>true</code> if so, <code>false</code> otherwise.
   */
  public boolean normExclude();

  /**
   * Sets the flag that determines whether or not norm is excluded.
   * @param flag the "norm exclude list" flag value
   */
  public void setNormExclude(boolean flag);

  /**
   * Returns the {@link Termgroup} to outrank.
   * @return the {@link Termgroup} to outrank
   */
  public Termgroup getTermgroupToOutrank();

  /**
   * Sets the {@link Termgroup} to outrank.
   * @param termgroup the {@link Termgroup} to outrank
   */
  public void setTermgroupToOutrank(Termgroup termgroup);

  /**
   * Returns the release {@link Rank}.
   * @return the release {@link Rank}
   */
  public Rank getReleaseRank();

  /**
   * Sets the release {@link Rank}.
   * @param rank the release {@link Rank}
   */
  public void setReleaseRank(Rank rank);

  /**
   * Returns the notes.
   * @return the notes
   */
  public String getNotes();

  /**
   * Sets the notes.
   * @param notes the notes
   */
  public void setNotes(String notes);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of
   * {@link Termgroup} interface.
   */
  public class Default implements Termgroup {

    //
    // Fields
    //

    private Rank rank = null;
    private Rank release_rank = null;
    private Termgroup termgroup = null;
    private Source source = null;
    private String termtype = null;
    private String suppressible = null;
    private boolean exclude = false;
    private boolean norm_exclude = false;
    private String notes = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty {@link Termgroup}.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates a {@link Termgroup} from the specified value.
     * @param termgroup a {@link String} representation of termgroup
     */
    public Default(String termgroup) {
      // termgroup = MSH2001/MH
      // source = MSH2001
      // termtype = MH
      StringTokenizer st = new StringTokenizer(termgroup, "/");

      if (st.countTokens() != 2) {
        throw new IllegalArgumentException
            ("Badly formatted termgroup (" + termgroup + ")");
      }
      source = (Source)new Source.Default();
      source.setSourceAbbreviation(st.nextToken());
      termtype = st.nextToken();
      this.rank = Rank.EMPTY_RANK;
      this.release_rank = Rank.EMPTY_RANK;
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
    // Implementation of Termgroup interface
    //

    /**
     * Implements {@link Termgroup#getSource()}.
     * @return the {@link Source}
     */
    public Source getSource() {
      return source;
    }

    /**
     * Implements {@link Termgroup#setSource(Source)}.
     * @param source the {@link Source}
     */
    public void setSource(Source source) {
      this.source = source;
    }

    /**
     * Implements {@link Termgroup#getTermType()}.
     * @return the term type
     */
    public String getTermType() {
      return termtype;
    }

    /**
     * Implements {@link Termgroup#setTermType(String)}.
     * @param termtype the term type
     */
    public void setTermType(String termtype) {
      this.termtype = termtype;
    }

    /**
     * Implements {@link Termgroup#toString()}.
     * @return the {@link String} representation of termgroup.
     */
    public String toString() {
      return (source == null) ? "" :
          source.getSourceAbbreviation() + "/" + termtype;
    }

    /**
     * Equality function based on {@link String} representation.
     * @param object the object to compare to
     * @return <code>true</code> equal, <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof Termgroup))) {
        return false;
      }
      return this.toString().equals(object.toString());
    }

    /**
     * Comparison function.
     * @param object the object to compare to
     * @return a <code>int</code> code indicating the relative sort order
     */
    public int compareTo(Object object) {
      if ( (object == null) || (! (object instanceof Termgroup))) {
        return 0;
      }
      return this.toString().compareTo(object.toString());
    }

    /**
     * Implements {@link Termgroup#isSuppressible()}.
     * @return <code>true</code> if it is, <code>false</code> otherwise
     */
    public boolean isSuppressible() {
      return (suppressible != null && !suppressible.equals("N"));
    }

    /**
     * Implements {@link Termgroup#setSuppressible(String)}.
     * @param suppressible the suppressibility value
     */
    public void setSuppressible(String suppressible) {
      this.suppressible = suppressible;
    }

    /**
     * Implements {@link Termgroup#getSuppressible()}.
     * @return the suppressibility value
     */
    public String getSuppressible() {
      return suppressible;
    }

    /**
     * Implements {@link Termgroup#exclude()}.
     * @return the "exclude list" flag value
     */
    public boolean exclude() {
      return exclude;
    }

    /**
     * Implements {@link Termgroup#setExclude(boolean)}.
     * @param flag the "exclude list" flag value
     */
    public void setExclude(boolean flag) {
      this.exclude = flag;
    }

    /**
     * Implements {@link Termgroup#normExclude()}.
     * @return the "norm exclude list" flag value
     */
    public boolean normExclude() {
      return norm_exclude;
    }

    /**
     * Implements {@link Termgroup#setNormExclude(boolean)}.
     * @param flag the "norm exclude list" flag value
     */
    public void setNormExclude(boolean flag) {
      this.norm_exclude = flag;
    }

    /**
     * Returns the {@link Termgroup} to outrank.
     * @return the {@link Termgroup} to outrank
     */
    public Termgroup getTermgroupToOutrank() {
      return termgroup;
    }

    /**
     * Sets the {@link Termgroup} that this one should outrank.  Used
     * when adding new termgroups.
     * @param termgroup the {@link Termgroup} to outrank
     */
    public void setTermgroupToOutrank(Termgroup termgroup) {
      this.termgroup = termgroup;
    }

    //
    // Implementation of Rankable interface
    //

    /**
     * Implements {@link Rankable#getRank()}.
     * @return the {@link Rank}
     */
    public Rank getRank() {
      return rank;
    }

    /**
     * Returns the release {@link Rank}.
     * @return the release {@link Rank}
     */
    public Rank getReleaseRank() {
      return release_rank;
    }

    /**
     * Sets the release {@link Rank}.
     * @param rank the release {@link Rank}
     */
    public void setReleaseRank(Rank rank) {
      this.release_rank = rank;
    }

    /**
     * Implements {@link Rankable#setRank(Rank)}.
     * @param rank the {@link Rank}.
     */
    public void setRank(Rank rank) {
      this.rank = rank;
    }

    /**
     * Implements {@link Termgroup#getNotes()}.
     * @return the notes
     */
    public String getNotes() {
      return notes;
    }

    /**
     * Implements {@link Termgroup#setNotes(String)}.
     * @param notes the notes
     */
    public void setNotes(String notes) {
      this.notes = notes;
    }

  } // end of Default
}
