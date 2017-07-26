/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MergeFact
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;

/**
 * Represents a fact indicating that two atoms should be merged.
 *
 * This class roughly corresponds to <code>mom_merge_facts</code>.
 *
 * @author MEME Group
 */

public class MergeFact extends AtomFact.Default {

  //
  // Fields
  //
  protected Identifier id = null;
  protected boolean is_synonym = false;
  protected boolean is_exact_match = false;
  protected boolean is_norm_match = false;
  protected EnforcableIntegrityVector integrity_vector;
  protected ViolationsVector violations_vector;
  protected boolean make_demotion = false;
  protected boolean change_status = false;
  protected String merge_set = null;
  protected String status = null;
  protected MolecularAction action = null;
  protected Authority authority = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link MergeFact}.
   */
  public MergeFact() {
    super();
  };

  //
  // Overridden Object methods
  //

  /**
   * Returns the {@link String} representation.
   * @return the {@link String} representation
   */
  public String toString() {
    return id + ": " + merge_set + " " + getAtom() + " " + getConnectedAtom() +
        " (" +
        getIntegrityVector() + ")";
  }

  //
  // Additional Methods
  //

  /**
   * Returns the unique {@link Identifier}.
   * @return the unique {@link Identifier}
   */
  public Identifier getIdentifier() {
    return id;
  }

  /**
   * Sets the unique {@link Identifier}.
   * @param id the unique {@link Identifier}
   */
  public void setIdentifier(Identifier id) {
    this.id = id;
  }

  /**
   * Indicates whether or not the {@link Atom}s to
   * merge are synonyms.
   * @return <code>true</code> if they are,
   *         <code>false</code> otherwise
   */
  public boolean isSynonym() {
    return is_synonym;
  }

  /**
   * Sets the synonym flag.
   * @param flag the synonym flag
   */
  public void setIsSynonym(boolean flag) {
    is_synonym = flag;
    is_exact_match = !flag;
    is_norm_match = !flag;
  }

  /**
   * Indicates whether or not the {@link Atom}s are exact
   * string matches.
   * @return <code>true</code> if they are,
   *         <code>false</code> otherwise
   */
  public boolean isExactMatch() {
    return is_exact_match;
  }

  /**
   * Sets the exact match flag.
   * @param flag the exact match flag
   */
  public void setIsExactMatch(boolean flag) {
    is_synonym = !flag;
    is_exact_match = flag;
    is_norm_match = !flag;
  }

  /**
   * Indicates whether or not the {@link Atom}s are norm
   * string matches.
   * @return <code>true</code> if they are,
   *         <code>false</code> otherwise
   */
  public boolean isNormMatch() {
    return is_norm_match;
  }

  /**
   * Sets the norm match flag.
   * @param flag the norm match flag
   */
  public void setIsNormMatch(boolean flag) {
    is_synonym = !flag;
    is_exact_match = !flag;
    is_norm_match = flag;
  }

  /**
   * Returns the {@link EnforcableIntegrityVector} that will be used
   * when merging these two atoms.
   * @return the {@link EnforcableIntegrityVector}
   */
  public EnforcableIntegrityVector getIntegrityVector() {
    return integrity_vector;
  }

  /**
   * Sets the {@link EnforcableIntegrityVector}.
   * @param vector the {@link EnforcableIntegrityVector}
   */
  public void setIntegrityVector(EnforcableIntegrityVector vector) {
    this.integrity_vector = vector;
  }

  /**
   * Returns the {@link ViolationsVector} that resulted
   * from merging this fact.
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector getViolationsVector() {
    return violations_vector;
  }

  /**
   * Sets the {@link ViolationsVector}.
   * @param vector the {@link ViolationsVector}
   */
  public void setViolationsVector(ViolationsVector vector) {
    this.violations_vector = vector;
  }

  /**
   * Indicates whether or not the fact should be
   * demoted if the merge fails.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean demoteIfMergeFails() {
    return make_demotion;
  }

  /**
   * Sets the flag indicating whether or not this
   * fact should demote if the merge fails.
   * @param flag the "demote if fails" flag
   */
  public void setDemoteIfMergeFails(boolean flag) {
    make_demotion = flag;
  }

  /**
   * Indicates whether or not {@link Atom}s should
   * become unapproved as a result of merging across this fact.
   * @return <code>true</code> if they should, <code>false</code> otherwise
   */
  public boolean getChangeStatus() {
    return change_status;
  }

  /**
   * Sets the flag indicating whether or not atoms should
   * become unapproved as a result of merging across this fact.
   * @param flag a the "change status" flag
   */
  public void setChangeStatus(boolean flag) {
    change_status = flag;
  }

  /**
   * Returns the merge set name.
   * @return the merge set name
   */
  public String getName() {
    return merge_set;
  }

  /**
   * Sets the merge set name.
   * @param name the merge set name
   */
  public void setName(String name) {
    merge_set = name;
  }

  /**
   * Indicates whether a merge across this fact has succeeded.
   * @return <code>true</code> if this fact was successfully merged,
   *         <code>false</code> otherwise
   */
  public boolean mergeSucceeded() {
    return status.equals("M") || status.equals("P");
  }

  /**
   * Indicates whether a merge across this fact failed.
   * @return <code>true</code> if this fact was not successfully merged,
   *         <code>false</code> otherwise
   */
  public boolean mergeFailed() {
    return status.equals("D") || status.equals("F");
  }

  /**
   * Returns the status value.  There are five options
   * M (merged), P (passed), D (demoted), F (failed), R (ready).
   * @return the status value
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status value.
   * @param status the status value
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Returns the {@link MolecularAction} that merged this fact.
   * @return the {@link MolecularAction} that merged this fact
   */
  public MolecularAction getAction() {
    return action;
  }

  /**
   * Sets the {@link MolecularAction} that merged this fact.
   * @param action the {@link MolecularAction} that merged this fact
   */
  public void setAction(MolecularAction action) {
    this.action = action;
  }

  /**
   * Returns the {@link Authority}.
   * @return the {@link Authority}
   */
  public Authority getAuthority() {
    return authority;
  }

  /**
   * Sets the {@link Authority}.
   * @param authority the {@link Authority}
   */
  public void setAuthority(Authority authority) {
    this.authority = authority;
  }

  //
  // Overridden Object Methods
  //

  /**
   * Implements an equality function based on name and {@link Identifier}.
   * @param object the {@link MergeFact} to compare to
   * @return <code>true</code> if the {@link MergeFact}s are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if (object != null && object instanceof MergeFact) {
      MergeFact fact = (MergeFact) object;
      return merge_set.equals(fact.getName()) &&
          id.equals(fact.getIdentifier());
    }
    return false;
  }

  /**
   * Returns an <code>int</code> hashcode.
   * @return an <code>int</code> hashcode
   */
  public int hashCode() {
    return (merge_set + id).hashCode();
  }

}
