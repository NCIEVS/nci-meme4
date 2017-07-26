/*****************************************************************************
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_M
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.util.FieldedStringTokenizer;

/**
 * Validates merges between two {@link Concept}s that
 * contain releasable "NEC" {@link Atom}s with different {@link Source}s.
 *
 * @author MEME Group
 */
public class MGV_M extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_M} check.
   */
  public MGV_M() {
    super();
    setName("MGV_M");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @param source_atoms the {@link Atom}s being moved
   * @return <code>true</code> if constraint violated, <code>false</code>
   * otherwise
   */
  public boolean validate(Concept source, Concept target, Atom[] source_atoms) {

    //
    // Obtain target atoms
    //
    Atom[] target_atoms = target.getAtoms();

    //
    // Set up vars
    //
    FieldedStringTokenizer st = null;
    String source_atom_name = null;
    String target_atom_name = null;

    //
    // Find NEC atoms from different sources being merged
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable()) {
            if (!source_atoms[i].getSource().getStrippedSourceAbbreviation().
                equals(target_atoms[j].getSource().
                       getStrippedSourceAbbreviation())) {

              // SNOMEDCT_US allowed to merge with SNMI98 and RCD99
              if (source_atoms[i].getSource().getStrippedSourceAbbreviation().
                  equals("SNOMEDCT_US") &&
                  (target_atoms[j].getSource().getStrippedSourceAbbreviation().
                   equals("SNMI") ||
                   target_atoms[j].getSource().getStrippedSourceAbbreviation().
                   equals("RCD"))) {
                continue;
              }

              if (target_atoms[j].getSource().getStrippedSourceAbbreviation().
                  equals("SNOMEDCT_US") &&
                  (source_atoms[i].getSource().getStrippedSourceAbbreviation().
                   equals("SNMI") ||
                   source_atoms[i].getSource().getStrippedSourceAbbreviation().
                   equals("RCD"))) {
                continue;
              }

              if (!target_atoms[j].getLanguage().getAbbreviation().equals(
                  source_atoms[i].getLanguage().getAbbreviation())) {
                continue;
              }

              source_atom_name = source_atoms[i].getString();
              target_atom_name = target_atoms[j].getString();
              st = new FieldedStringTokenizer(source_atom_name, " ");
              while (st.hasMoreTokens()) {
                if (st.nextToken().toString().toUpperCase().equals("NEC")) {
                  return true;
                }
              }
              st = new FieldedStringTokenizer(target_atom_name, " ");
              while (st.hasMoreTokens()) {
                if (st.nextToken().toString().toUpperCase().equals("NEC")) {
                  return true;
                }
              }

            }
          }
        }
      }
    }

    return false;
  }

}
