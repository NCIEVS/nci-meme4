/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  AbstractMoveInhibitor
 *
 * 04/07/2006 RBE (1-AV8WP): Added new method
 * 		getNonMovingAtoms(Concept, Atoms[])
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import java.util.HashSet;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;

/**
 * Abstract implementation of {@link MoveInhibitor}
 *
 * @author MEME Group
 */

public abstract class AbstractMoveInhibitor extends IntegrityCheck.Default implements
    MoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AbstractMoveInhibitor}.
   */
  public AbstractMoveInhibitor() {
    super();
  }

  //
  // Implementation of MoveInhibitor
  //

  /**
   * Validate the {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @param source_atoms the {@link Atom}s being moved
   * @return <Code>true</code> if moving the specified atoms from the source
   * concept to the target concept would produce an integrity violation,
   * <code>false</code> otherwise
   */
  public abstract boolean validate(Concept source, Concept target,
                                   Atom[] source_atoms);

  /**
   * Return all non-moving atoms in a concept.
   * @param concept the {@link Concept}
   * @param moving_atoms the {@link Atom}s being moved
   * @return all non-moving atoms in a concept
   */
  public Atom[] getNonMovingAtoms(Concept concept, Atom[] moving_atoms) {
	  
	  //
	  // Obtain all atoms from the concept
	  //
	  Atom[] atoms = concept.getAtoms();

	  HashSet atoms_set = new HashSet(atoms.length);
	  HashSet source_atoms_set = new HashSet(moving_atoms.length);
		 
	  //
	  // Obtain atoms_set from atoms
	  //    
      for (int i = 0; i < atoms.length; i++)
        atoms_set.add(atoms[i]);

	  //
	  // Obtain source_atoms_set from source_atoms
	  //
      for (int i = 0; i < moving_atoms.length; i++)
        source_atoms_set.add(moving_atoms[i]);
	  
	  //
	  // Remove all matching atoms
	  //
	  atoms_set.removeAll(source_atoms_set);

	  //
	  // Return all non-moving atoms
	  //
	  return (Atom[]) atoms_set.toArray(new Atom[] {});

  }
  
  
}
