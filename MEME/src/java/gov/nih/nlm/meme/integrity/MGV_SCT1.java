/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_SCT1
 * 
 * 06/29/2006 RBE (1-BKQVT): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;

/**
 * Prevents merges between two {@link Concept}s if
 * source contains SNOMEDCT Entire and target contains the non SNOMEDCT/SCTSPA OR 
 * target contains SNOMEDCT Entire and source contains the non SNOMEDCT/SCTSPA
 * 
 * @author MEME Group
 */
public class MGV_SCT1 extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_SCT1} check.
   */
  public MGV_SCT1() {
    super();
    setName("MGV_SCT1");
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
    
    //
    // Identify source atoms containing releasable, current-version SNOMEDCT_US
    // Entire% (body structure) FN atom
    //
    boolean source_contains_entire = false;
    for (int i = 0; i < source_atoms.length; i++) {
    	Source src = source_atoms[i].getSource();
    	String tty = source_atoms[i].getTermgroup().getTermType();
    	String atom_name = source_atoms[i].getString();
    	if (source_atoms[i].isReleasable() &&
       		src.isCurrent() &&
    		src.getRootSourceAbbreviation().equals("SNOMEDCT_US") &&
    		atom_name.startsWith("Entire") && 
    		atom_name.endsWith("(body structure)") &&
     	    tty.equals("FN")
     	    )
    		source_contains_entire = true;
    }

    //
    // Identify target atoms containing releasable, current-version SNOMEDCT_US 
    // Entire% (body structure) FN atom
    //
    boolean target_contains_entire = false;
    for (int i = 0; i < target_atoms.length; i++) {
    	Source src = target_atoms[i].getSource();
    	String tty = target_atoms[i].getTermgroup().getTermType();
    	String atom_name = target_atoms[i].getString();
    	if (target_atoms[i].isReleasable() &&
       		src.isCurrent() &&
    		src.getRootSourceAbbreviation().equals("SNOMEDCT_US") &&
    		atom_name.startsWith("Entire") && 
    		atom_name.endsWith("(body structure)") &&
     	    tty.equals("FN")
     	    )
    		target_contains_entire = true;
    }

    //
    // Identify source atoms containing releasable, current-version atom 
    // whose root SAB is not SNOMEDCT_US and not SCTSPA
    //
    boolean source_contains_nonsct = false;
    for (int i = 0; i < source_atoms.length; i++) {
    	Source src = source_atoms[i].getSource();
    	if (source_atoms[i].isReleasable() &&
       		src.isCurrent() &&
       		!src.getRootSourceAbbreviation().equals("SNOMEDCT_US") &&
       		!src.getRootSourceAbbreviation().equals("SCTSPA")    		
       		)
    		source_contains_nonsct = true;
    }
    
    //
    // Identify target atoms containing releasable, current-version atom 
    // whose root SAB is not SNOMEDCT_US and not SCTSPA
    //
    boolean target_contains_nonsct = false;
    for (int i = 0; i < target_atoms.length; i++) {
    	Source src = target_atoms[i].getSource();
    	if (target_atoms[i].isReleasable() &&
       		src.isCurrent() &&
       		!src.getRootSourceAbbreviation().equals("SNOMEDCT_US") &&
       		!src.getRootSourceAbbreviation().equals("SCTSPA")    		
       		)
    		target_contains_nonsct = true;
    }
    
    //
    // Return a violation if
    //  - the source contains the SNOMEDCT_US Entire% and the target contains non SNOMEDCT_US/SCTCPA
    //  - the target contains the SNOMEDCT_US Entire% and the source contains non SNOMEDCT_US/SCTCPA
    //
    return (source_contains_entire && target_contains_nonsct) || 
           (target_contains_entire && source_contains_nonsct);
  }

}
