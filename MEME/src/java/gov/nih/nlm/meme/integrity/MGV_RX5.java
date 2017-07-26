/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_RX1
 * 
 * 04/24/2007 BAC (1-E3I65): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.CoreDataRestrictor;
import gov.nih.nlm.meme.common.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Prevents merges between two {@link Concept}s where
 * one contains a "base" atom of an ambiguous case and the
 * other contains an "normal form" atom.
 * 
 * A "base" atom is a current-version atom with a current-version
 * RXNORM attribute with ATN=AMBIGUITY_FLAG and ATV=Base.
 * 
 * @author Brian Carlsen
 */
public class MGV_RX5 extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_RX4} check.
   */
  public MGV_RX5() {
    super();
    setName("MGV_RX5");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s.
   * 
   * @param source
   *          the source {@link Concept}
   * @param target
   *          the target {@link Concept}
   * @param source_atoms the {@link Atom}s being moved
   * @return <code>true</code> if constraint violated, <code>false</code>
   *         otherwise
   */
  public boolean validate(Concept source, Concept target, Atom[] source_atoms) {

  	CoreDataRestrictor pack_tty_rest = new CoreDataRestrictor() {
  		public boolean keep(CoreData d) {
  			if (!(d instanceof Atom)) return false;
  			final Atom a = (Atom)d;
  			if (!a.getSource().isCurrent() ) return false;
  			final String tty = a.getTermgroup().getTermType();
  			boolean found = false;
  			if (tty.equals("GPCK") || tty.equals("BPCK")) {
  				 found = true;
  			}
  			return found;
  		}
  		public int compare(Object o1, Object o2) {
  			return 0;
  		}
  	};

  	CoreDataRestrictor sbcd_rest = new CoreDataRestrictor() {
  		public boolean keep(CoreData d) {
  			if (!(d instanceof Atom)) return false;
  			final Atom a = (Atom)d;
  			final String tty = a.getTermgroup().getTermType();
  			return (tty.equals("SBD") || tty.equals("SCD")) &&
  				a.getSource().getRootSourceAbbreviation().equals("RXNORM") &&
  				a.getSource().isCurrent();
  		}
  		public int compare(Object o1, Object o2) {
  			return 0;
  		}
  	};
  	
    //
    // Case 1: source has "base", target has "normalized"
    //
    Atom[] base_atoms = source.getRestrictedAtoms(pack_tty_rest);
    Atom[] norm_atoms= target.getRestrictedAtoms(sbcd_rest);
    if (base_atoms.length > 0 &&
     		norm_atoms.length > 0) return true;

    //
    // Case 2: Source has normalized and target has GPCK or BPCK
    base_atoms = target.getRestrictedAtoms(pack_tty_rest);
    norm_atoms = source.getRestrictedAtoms(sbcd_rest);
    if (base_atoms.length > 0 &&
    		norm_atoms.length > 0) return true;

    return false;
  }

}

