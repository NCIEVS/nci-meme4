/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MVS_RX3
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import java.util.HashSet;
import java.util.Set;

/**
 * Prevents RXCUIs containing primary atoms from being separated in UMLS {@link Concept}s.
 * 
 * @author MEME Group
 */
public class MVS_RX3 extends AbstractMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MVS_RX3} check.
   */
  public MVS_RX3() {
    super();
    setName("MVS_RX3");
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
   * @param source_atoms 
   * 	      the {@link Atom}s being move
   * @return <code>true</code> if constraint violated, <code>false</code>
   *         otherwise
   */
  public boolean validate(Concept source, Concept target, Atom[] source_atoms) {

    //
    // Obtain moving atoms
    //
	Atom[] atoms = source.getAtoms();
    
    //
    // Confirm that moving atoms contains a "primary atom". if not, then no violation
    //
	boolean found = false;
    for (int i = 0; i < atoms.length; i++) {
    	String tty = atoms[i].getTermgroup().getTermType();
    	String rsab = atoms[i].getSource().getRootSourceAbbreviation();
    	if (rsab.equals("RXNORM") &&
    		atoms[i].isReleasable() &&
       		(!tty.equals("OCD") && !tty.equals("OBD") && !tty.equals("SY") && !tty.equals("IN") && !tty.equals("BN"))) {
    	    found = true;
    	    break;
    	}
    }
    if (!found) return false;

    //
    // Obtain rcui atoms from source_atoms
    //
    Set source_atoms_rcuis = new HashSet();
    for (int i = 0; i < source_atoms.length; i++) {
        Attribute[] rxcuis = source_atoms[i].getAttributesByName("RXCUI");
        for (int j=0; j < rxcuis.length; j++) {
        	if (rxcuis[j].isReleasable() &&
        		rxcuis[j].getSource().isCurrent() &&
        		rxcuis[j].getAtom().isReleasable()) { 
        		source_atoms_rcuis.add(rxcuis[j].getValue());
        	}
        }
    }
    
    if (source_atoms_rcuis.isEmpty()) return false;

    //
    // Obtain rcui atoms from non_moving_atoms
    //
    Atom[] non_moving_atoms = getNonMovingAtoms(source, source_atoms);
    Set non_moving_atoms_rcuis = new HashSet();
    for (int i = 0; i < non_moving_atoms.length; i++) {
        Attribute[] rxcuis = non_moving_atoms[i].getAttributesByName("RXCUI");
        for (int j=0; j < rxcuis.length; j++) {
        	if (rxcuis[j].isReleasable() &&
        		rxcuis[j].getSource().isCurrent() &&
        		rxcuis[j].getAtom().isReleasable()) { 
        		non_moving_atoms_rcuis.add(rxcuis[j].getValue());
        	}
        }
    }
    
    if (non_moving_atoms_rcuis.isEmpty()) return false;
    
    //
    // Retain all matching RXCUI
    //
    source_atoms_rcuis.retainAll(non_moving_atoms_rcuis);

    return !source_atoms_rcuis.isEmpty();
        
  }
  
}
