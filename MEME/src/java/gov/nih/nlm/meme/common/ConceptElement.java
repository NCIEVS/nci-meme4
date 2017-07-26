/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ConceptElement
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents something connected to a {@link Concept}.
 *
 * @author MEME Group
 */

public interface ConceptElement extends CoreData {

  /**
   * Returns the {@link Concept} to which this element is connected.
   * @return the {@link Concept}
   */
  public Concept getConcept();

  /**
   * Sets the {@link Concept} to which this element is connected.
   * @param concept the {@link Concept}
   */
  public void setConcept(Concept concept);

}
