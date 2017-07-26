/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularSplitAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.NonSourceAssertedRestrictor;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;

import java.util.ArrayList;
import java.util.List;

/**
 * This action splits atom out of a concept and create a new concept containing those atoms.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to split atoms out of
     *         one concept and create a new concept containing those atoms.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept to be splited</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept</li>
 *     <li>No ic checks</li>
 *     <li>Insert a new concept (target)</li>
 *     <li>Move specified atoms to new concept</li>
 *     <li>Unapprove atoms if getChangeStatus is set</li>
 *     <li>If flag is set, copy C level *releasable* relationships as status N</li>
 *     <li>Copy stys (status n)</li>
 *     <li>Insert relationship between source/target</li>
 *     <li>Unapprove target concept if getChangeStatus</li>
 *     <li>MolecularMoveAction</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create object
 * Concept source_concept = ... get concept ...;<br>
 * Concept target_concept = ... get concept ...;<br>
 * // This atom must come from the source concept.
 * Atom atom_to_split = ... get atom ...;
 * Authority authority = ... get authority ...;
 * Identifier work_id = ... get identifier ...;
 * Identifier transaction_id = ... get identifier ...;<br>
 * // Create & configure action
 * MolecularSplitAction msa = new MolecularSplitAction(source_concept);
 * msa.setSource(source_concept);
 * msa.setTarget(target_concept);
 * msa.setAuthority(authority);
 * msa.setTransactionIdentifier(transaction_id);
 * msa.setWorkIdentifier(wordk_id);
 * msa.setIntegrityVector(null);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 */

public class MolecularSplitAction
    extends MolecularAction {

  //
  // Fields
  //

  private Relationship split_rel = null;
  private boolean clone_rels = false;
  private boolean clone_stys = false;
  private List atoms_to_split = null;

  //
  // Constructors
  //

  /**
   * Used for ObjectXMLSerializer.
   */
  private MolecularSplitAction() {
    super();
  }

  /**
   * Instantiates a {@link MolecularSplitAction} from the specified {@link Concept}.
   * @param concept the source {@link Concept}
   */
  public MolecularSplitAction(Concept concept) {
    super();
    setActionName("MOLECULAR_SPLIT");
    setSource(concept);
    atoms_to_split = new ArrayList();
  }

  //
  // Methods
  //

  /**
   * Returns a list of {@link Concept}s to refresh.
   * @return a {@link Concept}<code>[]</code> of concepts to refresh
   */
  public Concept[] getConceptsToRefresh() {
    return new Concept[] {
        getSource()};
  }

  /**
   * Returns a list of {@link Atom}s to split.
   * @return an {@link Atom}<code>[]</code> of atoms to split
   */
  public Atom[] getAtomsToSplit() {
    return (Atom[])atoms_to_split.toArray(new Atom[0]);
  }

  /**
   * Adds an {@link Atom} to split out of source concept.
       * The {@link Concept} of the {@link Atom} must equal the source {@link Concept}.
   * @param atom the {@link Atom} to split
   * @throws BadValueException if failed due to invalid value
   */
  public void addAtomToSplit(Atom atom) throws BadValueException {
    if (!atom.getConcept().equals(getSource())) {
      BadValueException bve = new BadValueException(
          "Atom does not belong to source concept.");
      bve.setDetail("source_concept_id", getSource().getIdentifier());
      bve.setDetail("atom_concept_id", atom.getConcept().getIdentifier());
      throw bve;
    }
    atoms_to_split.add(atom);
  }

  /**
       * Sets the {@link Relationship} between the source and target {@link Concept}s.
   * @param rel the {@link Relationship} between the source and target {@link Concept}s
   */
  public void setSplitRelationship(Relationship rel) {
    this.split_rel = rel;
  }

  /**
   * Returns the {@link Relationship} between the source and target {@link Concept}s.
   * @return the {@link Relationship} between the source and target {@link Concept}s
   */
  public Relationship getSplitRelationship() {
    return split_rel;
  }

  /**
   * Sets the flag indicating whether or not to clone {@link Relationship}s.
   * @param clone_rels a <code>boolean</code> representation whether or not
   * relationships are cloned
   */
  public void setCloneRelationships(boolean clone_rels) {
    this.clone_rels = clone_rels;
  }

  /**
   * Indicates whether or not {@link Relationship}s should be cloned.
   * @return <code>true</code> if relationships are cloned; <code>false</code>
   * otherwise
   */
  public boolean getCloneRelationships() {
    return clone_rels;
  }

  /**
   * Sets the flag indicating whether or not to clone {@link SemanticType}s.
   * @param clone_stys a <code>boolean</code> representation whether or not
   * semantic types are cloned
   */
  public void setCloneSemanticTypes(boolean clone_stys) {
    this.clone_stys = clone_stys;
  }

  /**
   * Indicates whether or not {@link SemanticType}s should be cloned.
   * @return <code>true</code> if semantic types are cloned; <code>false</code>
   * otherwise
   */
  public boolean getCloneSemanticTypes() {
    return clone_stys;
  }

  /**
   * Performs molecular split action.
   * @throws ActionException if failed while performing molecular split action
   */
  public void performAction() throws ActionException {

    MEMEToolkit.trace("Performing molecular split action.");

    // Insert new concept
    Concept target = new Concept.Default();

    target.setStatus('N');
    target.setTobereleased('Y');
    target.setReleased('N');
    setTarget(target);
    AtomicInsertAction aia = new AtomicInsertAction(target);
    addSubAction(aia);

    // Move atoms to new concept
    Atom[] atoms = (Atom[])atoms_to_split.toArray(new Atom[0]);
    for (int i = 0; i < atoms.length; i++) {
      AtomicChangeConceptAction acca = new AtomicChangeConceptAction(atoms[i]);
      acca.setNewConcept(target);
      addSubAction(acca);

      //
      // Move translation atoms
      //
      Atom[] foreign_atoms = atoms[i].getTranslationAtoms();
      for (int x = 0; x < foreign_atoms.length; x++) {
        if (foreign_atoms[i].getConcept().getIdentifier().equals(getSource().
            getIdentifier())) {
          acca = new AtomicChangeConceptAction(foreign_atoms[i]);
          acca.setNewConcept(target);
          addSubAction(acca);
        }
      }

      if (!atoms[i].needsReview()) {
        AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(atoms[i]);
        acsa.setNewValue("N");
        addSubAction(acsa);
      }
    }

    if (split_rel != null) {
      split_rel.setConcept(getSource());
      split_rel.setRelatedConcept(getTarget());
      split_rel.setSource(new Source.Default(getAuthority() == null ? "" :
                                             getAuthority().toString()));
      split_rel.setSourceOfLabel(new Source.Default(getAuthority() == null ? "" :
          getAuthority().toString()));
      split_rel.setGenerated(true);
      split_rel.setReleased('N');
      split_rel.setStatus('N');
      split_rel.setLevel('C');
      split_rel.setTobereleased('Y');
      aia = new AtomicInsertAction(split_rel);
      addSubAction(aia);
    }

    if (getCloneRelationships()) {
      Relationship[] rels =
          getSource().getRestrictedRelationships(new
                                                 NonSourceAssertedRestrictor());
      for (int i = 0; i < rels.length; i++) {
        if (rels[i].isConceptLevel() && rels[i].isReleasable()) {
          rels[i].setConcept(target);
          rels[i].setSource(new Source.Default(getAuthority() == null ? "" :
                                               getAuthority().toString()));
          rels[i].setSourceOfLabel(new Source.Default(getAuthority() == null ?
              "" : getAuthority().toString()));
          rels[i].setGenerated(true);
          rels[i].setReleased('N');
          rels[i].setStatus('N');
          aia = new AtomicInsertAction(rels[i]);
          addSubAction(aia);
        }
      }
    }

    if (getCloneSemanticTypes()) {
      ConceptSemanticType[] stys = getSource().getSemanticTypes();
      for (int i = 0; i < stys.length; i++) {
        stys[i].setConcept(target);
        stys[i].setSource(new Source.Default(getAuthority() == null ? "" :
                                             getAuthority().toString()));
        stys[i].setStatus('N');
        stys[i].setNativeIdentifier(null);
        aia = new AtomicInsertAction(stys[i]);
        addSubAction(aia);
      }
    }

    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }

  }

  /**
   * Returns the information of this class
   * @return the information of this class
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(100);
    sb.append(getClass().getName()).append(": id=").append(getIdentifier()).
        append(", sid=")
        .append(getSourceIdentifier()).append(", tid=").append(
        getTargetIdentifier());
    if (atoms_to_split != null) {
      for (int i = 0; i < atoms_to_split.size(); i++) {
        if (i > 0)
          sb.append(",");
        else
          sb.append(", atom_ids=");
        sb.append( ( (Atom)atoms_to_split.get(i)).getIdentifier());
      }
    }
    return sb.toString();
  }

}
