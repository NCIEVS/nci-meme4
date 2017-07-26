/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_RX1
 * 
 * 04/07/2006 RBE (1-AV8WP): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import java.util.HashSet;
import java.util.Set;

/**
 * Prevents merges between two {@link Concept}s containing 
 * same TTY RSAB=RXNORM atoms with TTYs not in (OCD, OBD, SY, BN, IN).
 * 
 * @author MEME Group
 */
public class MGV_RX2 extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_RX2} check.
   */
  public MGV_RX2() {
    super();
    setName("MGV_RX2");
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

    //
    // Obtain atoms from target
    //
    Atom[] target_atoms = target.getAtoms();

    Set sset = new HashSet();
    Set tset = new HashSet();    
    
    //
    // Keep source/target RX2s
    //    
    for (int i = 0; i < source_atoms.length; i++) {
    	String tty = source_atoms[i].getTermgroup().getTermType();
    	String rsab = source_atoms[i].getSource().getRootSourceAbbreviation();
    	if (rsab.equals("RXNORM") &&
    		(!tty.equals("OCD") && !tty.equals("OBD") && !tty.equals("SY") && !tty.equals("BN") && !tty.equals("IN")))
    		sset.add(tty);
    }
       
    if (sset.isEmpty()) return false;
    
    for (int i = 0; i < target_atoms.length; i++) {
    	String tty = target_atoms[i].getTermgroup().getTermType();
    	String rsab = target_atoms[i].getSource().getRootSourceAbbreviation();
    	if (rsab.equals("RXNORM") &&
       		(!tty.equals("OCD") && !tty.equals("OBD") && !tty.equals("SY") && !tty.equals("BN") && !tty.equals("IN")))    			
    		tset.add(tty);
    }
           
    if (tset.isEmpty()) return false;
    
    //
    // Retain all matching target set
    //
    sset.retainAll(tset);

    return !sset.isEmpty();
  }

}
