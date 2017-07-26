/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ConceptChecklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Collections;

/**
 * This class represents a clustered checklist of {@link Concept}s.
 *
 * @author MEME Group
 */
public class ConceptChecklist extends Checklist {

  //
  // Constructors
  //

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Instantiates an empty {@link ConceptChecklist}.
   */
  public ConceptChecklist() {
    super();
  };

  //
  // Methods
  //

  /**
   * Returns the {@link Concept}s contained in the checklist.
   * @return the {@link Concept}s contained in the checklist
   */
  public Concept[] getConcepts() {
    Collections.sort(this);
    return (Concept[]) toArray(new Concept[0]);
  }

  //
  // Overridden Checklist Methods
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
   * Adds the object (presumed to be a {@link Concept}).
   * @param object the {@link Concept} to add
       * @return a status code indicating whether or not the add completed successfully
   * @throws IllegalArgumentException if the object is not a {@link Concept}
   */
  public boolean add(Object object) {
    if (! (object instanceof Concept)) {
      throw new IllegalArgumentException(
          "Only Concepts and ConceptClusters may be added to a ConceptChecklist");
    }
    return super.add(object);
  }

  /**
   * Adds the specified {@link ConceptCluster}.
   * @param cluster the {@link ConceptCluster} to add
   */
  public void addCluster(Cluster cluster) {
    if (! (cluster instanceof ConceptCluster)) {
      throw new IllegalArgumentException(
          "Only Concepts and ConceptClusters may be added to an ConceptChecklist");
    }
    super.addCluster(cluster);
  }

}
