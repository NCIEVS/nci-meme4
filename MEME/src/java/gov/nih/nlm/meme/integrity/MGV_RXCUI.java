/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_RXCUI
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;

import java.util.HashSet;
import java.util.Set;

/**
 * Validate merges between two {@link Concept}s with different RXCUIs.
 * 
 * @author MEME Group
 */
public class MGV_RXCUI extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_RXCUI} check.
   */
  public MGV_RXCUI() {
    super();
    setName("MGV_RXCUI");
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
    // Obtain rxcui atoms from source/target
    //
    Attribute[] source_rxcuis = source.getAttributesByName("RXCUI");
    Attribute[] target_rxcuis = target.getAttributesByName("RXCUI");

    Set sset = new HashSet();
    Set tset = new HashSet();
    
    //
    // Keep releaseable source/target RXCUIs
    //
    for (int i = 0; i < source_rxcuis.length; i++)
      if (source_rxcuis[i].isReleasable() &&
          source_rxcuis[i].getSource().isCurrent() &&
          source_rxcuis[i].getAtom().isReleasable()) 
        sset.add(source_rxcuis[i].getValue());
    
    if (sset.isEmpty()) return false;
    
    for (int i = 0; i < target_rxcuis.length; i++)
      if (target_rxcuis[i].isReleasable() &&
          target_rxcuis[i].getSource().isCurrent() &&
          target_rxcuis[i].getAtom().isReleasable()) 
        tset.add(target_rxcuis[i].getValue());

    if (tset.isEmpty()) return false;
    
    //
    // Remove all matching target set
    //
    Set sset_copy = new HashSet(sset);
    sset.removeAll(tset);
    tset.removeAll(sset_copy);

    return !sset.isEmpty() && !tset.isEmpty();
  }

}
