/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Rankable
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Indicates that an object type can be ranked, and provides
 * access to the {@link Rank}.
 *
 * @author MEME Group
 */

public interface Rankable {

  /**
   * Returns the rank.
   * @return the rank
   */
  public Rank getRank();

  /**
   * Sets the rank.
   * @param rank the rank
   */
  public void setRank(Rank rank);

}
