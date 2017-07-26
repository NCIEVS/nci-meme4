/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ConceptCluster
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Collections;

/**
 * This class represents a cluster of {@link Concept} objects
 * associated with an {@link Identifier}.
 *
 * @author MEME Group
 */

public class ConceptCluster extends Cluster {

  //
  // Constructors
  //

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Instantiates a {@link Cluster} with the specified {@link Identifier}.
   * @param id the {@link Identifier}
   */
  public ConceptCluster(Identifier id) {
    super(id);
  };

  /**
   * Instantiates an empty {@link ConceptCluster}.
   */
  public ConceptCluster() {
    super();
  };

  //
  // Methods
  //

  /**
   * Returns the {@link Concept}s
   * @return the {@link Concept}s
   */
  public Concept[] getConcepts() {
    Collections.sort(this);
    return (Concept[]) toArray(new Concept[0]);
  }

  //
  // Overridden Cluster Methods
  //

  /**
   * Adds the specified object (presumed to be a {@link Concept}).
   * @param concept the {@link Concept} to add
       * @return a status code indicating whether or not the add completed successfully
   * @throws IllegalArgumentException if the object is not a {@link Concept})
   */
  public boolean add(Object concept) {
    if (! (concept instanceof Concept)) {
      throw new IllegalArgumentException(
          "Only Concepts may be added to a ConceptCluster");
    }
    return super.add(concept);
  }

}
