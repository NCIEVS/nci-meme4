/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I8
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.Relationship;

/**
 * Superclass code for
 * the various <code>DT_I8*</code> integrity checks.
 * alidates those {@link Concept}s that contain:
 * <pre>
 * - MSH "entry term" with D# code matching a MSH MH in a different {@link Concept} with
 *   NO approved, releasable, RT, NT, BT, or non MTH asserted LK {@link Relationship} to
 *   that {@link Concept}
 *
 * - MSH "entry term" with D# code matching a MSH MH in a different {@link Concept} with
 *   an approved, releasable XR {@link Relationship} to that {@link Concept} which overrides any
 *   other valid {@link Relationship}s.
 *
 * - MSH "entry term" with Q# code matching a MSH TQ in a different {@link Concept} with
 *   NO approved, releasable, RT, NT, BT, or non MTH asserted LK {@link Relationship} to
 *   that {@link Concept}.
 *
 * - MSH "entry term" with Q# code matching a MSH TQ in a different {@link Concept} with
 *   an approved, releasable XR {@link Relationship} to that {@link Concept} which overrides any
 *   other valid {@link Relationship}s.
 *
 * - MSH "entry term" with C# code matching a MSH NM in a different {@link Concept} with
     *   NO approved (or unreviewed), releasable, RT, NT, BT, or non MTH asserted LK
 *   {@link Relationship} to that {@link Concept}.
 *
 * - MSH "entry term" with C# code matching a MSH NM in a different {@link Concept} with
 *   an approved, releasableXR {@link Relationship} to that {@link Concept} which overrides any
 *   other valid {@link Relationship}s.
 *</pre>
 * @author MEME Group
 */

public class DT_I8 extends AbstractDataConstraint {

  //
  // Fields
  //

  private String tty = null;
  private boolean xr_flag = false;
  private final static String MSH_MH = "MH";
  private final static String MSH_TQ = "TQ";
  private final static String MSH_NM = "NM";

  //
  // Constructors
  //

  /**
   * Instantiates {@link DT_I8}.
   */
  public DT_I8() {
    super();
    setName("DT_I8");
  }

  //
  // Methods
  //

  /**
   * Sets the term type.
   * @param tty the term type
   */
  public void setTermType(String tty) {
    this.tty = tty;
  }

  /**
   * Sets the XR flag.
   * @param xr_flag a flag indicating whether or not to check for
   * violations based on XR {@link Relationship}
   */
  public void setXR(boolean xr_flag) {
    this.xr_flag = xr_flag;
  }

  //
  // Methods
  //

  /**
   * Validates the specified concept.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code> otherwise
   */
  public boolean validate(Concept source) {

    //
    // Get all MSH atoms
    //
    Atom[] atoms = source.getRestrictedAtoms(new ByStrippedSourceRestrictor(
        "MSH"));

    //
    // Get all relationships
    //
    Relationship[] rels = source.getRelationships();

    //
    // Map entry term code
    //
    String code = null;
    if (tty.equals(MSH_MH)) {
      code = "D";
    }
    if (tty.equals(MSH_TQ)) {
      code = "Q";
    }
    if (tty.equals(MSH_NM)) {
      code = "C";

      //
      // Get all current MSH D#, Q# or C# entry terms
      //
    }
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i] instanceof MeshEntryTerm &&
          atoms[i].getSource().isCurrent() &&
          atoms[i].getCode().toString().startsWith(code)) {

        boolean found = false;

        //
        // Is same code main heading in this concept
        // if so, no violation
        //
        for (int j = 0; j < atoms.length; j++) {
          if (atoms[j].getSource().isCurrent() &&
              atoms[j].getCode().equals(atoms[i].getCode()) &&
              atoms[j].getTermgroup().getTermType().equals(tty)) {
            found = true;
            break;
          }
        }

        //
        // If main heading is in the same concept,
        // then go to the next case
        //
        if (found) {
          continue;
        }

        //
        // Is same code main heading in related concept
        // if so, no violation
        //
        found = false;
        Atom mh = ( (MeshEntryTerm) atoms[i]).getMainHeading();
        for (int j = 0; j < rels.length; j++) {

          if (mh != null &&
              rels[j].getRelatedConcept().getIdentifier().equals(mh.getConcept().
              getIdentifier()) &&
              rels[j].isReleasable() &&
              rels[j].isApproved()) {
            //
            // Handle XR case
            //
            if (xr_flag &&
                rels[j].getName().equals("XR") &&
                rels[j].isMTHAsserted() &&
                rels[j].isReleasable() &&
                rels[j].isApproved()) {
              return true;
            }

            //
            // The relationship must be MTH asserted
            // or asserted by current MSH
            //
            else if (rels[j].isMTHAsserted() ||
                     (rels[j].isSourceAsserted() &&
                      rels[j].getSource().isCurrent() &&
                      rels[j].getSource().getStrippedSourceAbbreviation().
                      equals("MSH"))) {

              //
              // make sure the relationship has a valid name
              //
              if (rels[j].getName().equals("RT") ||
                  rels[j].getName().equals("NT") ||
                  rels[j].getName().equals("BT") ||
                  rels[j].getName().equals("LK")) {
                found = true;
                break;
              }
            }
          }
        }
        if (!found && !xr_flag) {
          return true;
        }

      } // end if entry term
    }
    return false;
  }

}
