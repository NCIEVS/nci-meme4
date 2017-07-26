/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ReportsAtomComparator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Provides an {@link Atom} ordering suitable for the concept reports.
 *
 * @author MEME Group
 */

public class ReportsAtomComparator implements Comparator {

  //
  // Fields
  //

  private HashMap sui_ranks = null;
  private HashMap lui_ranks = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ReportsAtomComparator} for the
   * specified {@link Concept}.
   * This constructor looks for an atom from a given concept and ranks them
   * into correct order specified by the implementation of {@link Comparator}.
   * @param concept the {@link Concept}
   */
  public ReportsAtomComparator(Concept concept) {

    lui_ranks = new HashMap();
    sui_ranks = new HashMap();

    StringIdentifier lui = null;
    StringIdentifier sui = null;

    Rank rank = null;
    Rank lui_rank = null;
    Rank sui_rank = null;

    // Get default atom ordering
    Atom[] atoms = concept.getSortedAtoms();

    // Iterate through atoms, maintaning the lui_ranks and sui_ranks maps
    for (int i = 0; i < atoms.length; i++) {

      lui = atoms[i].getLUI();
      sui = atoms[i].getSUI();
      rank = atoms[i].getRank();

      // Look up that atom's lui in lui_ranks
      lui_rank = (Rank) lui_ranks.get(lui);
      if (lui_rank == null) {

        // Add the current atom's lui and rank to the hashmap.
        lui_ranks.put(lui, rank);

        // Compare the rank returned with the current rank
        // and determine which rank is higher.
      } else if (rank.compareTo(lui_rank) > 0) {

        // if the current atom's rank is higher than the one in the hashmap,
        // then replace it.
        lui_ranks.put(lui, rank);

        // Look up that atom's sui in sui_ranks
      }
      sui_rank = (Rank) sui_ranks.get(sui);
      if (sui_rank == null) {

        // Add the current atom's sui and rank to the hashmap.
        sui_ranks.put(sui, rank);

        // Compare the rank returned with the current rank
        // and determine which rank is higher.
      } else if (rank.compareTo(sui_rank) > 0) {

        // if the current atom's rank is higher than the one in the hashmap,
        // then replace it.
        sui_ranks.put(sui, rank);

      }
    } // end for

    //MEMEToolkit.trace("ReportsAtomComparator::lui ranks - " + lui_ranks);
    //MEMEToolkit.trace("ReportsAtomComparator::sui ranks - " + sui_ranks);

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
  // Implementation of Comparator interface
  //

  /**
   * Comparison function.
   * This implementation get things with <i>higher</i> ranks to sort before
   * things with <i>lower</i> ranks.
   * @param o1 first object to compare
   * @param o2 second object to compare
   * @return an <code>int</code> code indicating the relative sort order
   */
  public int compare(Object o1, Object o2) {

    Atom a1 = (Atom) o1;
    Atom a2 = (Atom) o2;

    // Compare LUI ranks
    if (!a1.getLUI().equals(a2.getLUI())) {
      Rank l2 = (Rank) lui_ranks.get(a2.getLUI());
      return l2.compareTo(lui_ranks.get(a1.getLUI()));
    }

    // Compare SUI ranks
    if (!a1.getSUI().equals(a2.getSUI())) {
      Rank s2 = (Rank) sui_ranks.get(a2.getSUI());
      return s2.compareTo(sui_ranks.get(a1.getSUI()));
    }

    // If things are STILL equal, compare the atoms
    return a2.compareTo(a1);

  }

  /**
   * Equality function.
   * @param object object ot compare to.
   * @return <code>true</code> equal, <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof ReportsAtomComparator))) {
      return false;
    }
    return this.equals(object);
  }
}
