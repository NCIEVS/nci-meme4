/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  CoreDataRestrictor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Comparator;

/**
 * Generically represents a mechanism for restricting
 * {@link CoreData} elements of a {@link Concept} during
 * retrieval.  Additionally, it imposes a natural ordering
 * of those elements (via extending {@link Comparator}).
 *
 * @author MEME Group
 */

public interface CoreDataRestrictor extends Comparator {

  /**
   * Indicatess whether or not an element should be kept.
   * @param element the {@link CoreData} to potentially restrict
   * @return <code>true</code> if the element should be kept,
   *         <code>false</code> otherwise
   */
  public boolean keep(CoreData element);
}
