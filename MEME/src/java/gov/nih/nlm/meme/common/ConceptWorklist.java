/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ConceptWorklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Collections;

/**
 * This class represents a clustered worklist {@link Concept}s.
 *
 * @author MEME Group
 */

public class ConceptWorklist extends Worklist {

  //
  // Constructors
  //

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Instantiates an empty {@link ConceptWorklist}.
   */
  public ConceptWorklist() {
    super();
  };

  //
  // Methods
  //

  /**
   * Returns the {@link Concept}s.
   * @return the {@link Concept}s
   */
  public Concept[] getConcepts() {
    Collections.sort(this);
    return (Concept[]) toArray(new Concept[0]);
  }

  //
  // Overridden Worklist Methods
  //

  /**
   * Adds the specified {@link Concept}s.
   * @param concepts the {@link Concept}s to add
   */
  public void add(Concept[] concepts) {
    if (concepts != null) {
      for (int i = 0; i < concepts.length; i++) {
        super.add(concepts[i]);
      }
    }
  }

  /**
   * Adds the specified object (presumed to be a {@link Concept}).
   * @param object the {@link Concept} to add
       * @return status code indicating whether or not the add completed successfully
   * @throws IllegalArgumentException if the object is not a {@link Concept}
   */
  public boolean add(Object object) {
    if (! (object instanceof Concept)) {
      throw new IllegalArgumentException(
          "Only Concepts and ConceptClusters may be added to a ConceptWorklist");
    }
    return super.add(object);
  }

  /**
   * Adds the speciifed {@link ConceptCluster}.
   * @param cluster the {@link ConceptCluster} to add
   */
  public void addCluster(Cluster cluster) {
    if (! (cluster instanceof ConceptCluster)) {
      throw new IllegalArgumentException(
          "Only Concepts and ConceptClusters may be added to an ConceptWorklist");
    }
    super.addCluster(cluster);
  }

}
