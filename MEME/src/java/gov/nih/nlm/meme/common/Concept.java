/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Concept
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.meme.action.LoggedAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Generically represents a UMLS concept.
 *
 * @author MEME Group
 */

public interface Concept extends CoreData {

  //
  // Fields
  //

  /**
   * A default {@link Comparator} used to sort {@link Atom}s.
   * Used by {@link Concept#getSortedAtoms(Comparator)}.
   */
  public final static Comparator DEFAULT_ATOM_COMPARATOR =
      new DefaultAtomComparator();

  /**
   * A default {@link Comparator} used to sort {@link ContextRelationship}s.
   * Used by {@link Concept#getSortedContextRelationships(Comparator)}.
   */
  public final static Comparator DEFAULT_CONTEXT_RELATIONSHIP_COMPARATOR =
      new DefaultContextRelationshipComparator();

  /**
   * A {@link Comparator} to sort {@link Attribute}s by attribute value.
   * Used by {@link Concept#getSortedAttributes(Comparator)}.
   */
  public final static Comparator ATTRIBUTE_VALUE_COMPARATOR =
      new AttributeValueComparator();

  /**
   * A {@link Comparator} to sort {@link CoreData} by source.
   * Used by <code>Concept.getSortedXXX()</code> methods.
   */
  public final static Comparator SOURCE_COMPARATOR =
      new SourceComparator();

  //
  // Methods
  //

  /**
   * Empties the concept.
   */
  public void clear();

  /**
   * Returns the {@link CUI}.
   * @return the {@link CUI}
   */
  public CUI getCUI();

  /**
   * Returns the sorted and distinct last release
   * {@link CUI}s from the set of atoms.
   * @return the {@link CUI}s
   */
  public CUI[] getCUIs();

  /**
   * Sets the {@link CUI}.
   * @param cui the {@link CUI}
   */
  public void setCUI(CUI cui);

  /**
   * Returns the preferred {@link Atom}.
   * @return the preferred {@link Atom}
   */
  public Atom getPreferredAtom();

  /**
   * Sets the preferred {@link Atom}
   * @param preferred_atom  the preferred {@link Atom}
   */
  public void setPreferredAtom(Atom preferred_atom);

  /**
   * Returns the last approval action.
   * @return the last {@link LoggedAction} to approve this concept
   */
  public LoggedAction getApprovalAction();

  /**
   * Sets the last approval action.
   * @param last_approval the last {@link LoggedAction} to approve this concept
   */
  public void setApprovalAction(LoggedAction last_approval);

  /**
   * Returns the editing {@link Authority}.
   * @return the editing {@link Authority}
   */
  public Authority getEditingAuthority();

  /**
   * Sets the editing {@link Authority}.
   * @param editing_authority the editing {@link Authority}
   */
  public void setEditingAuthority(Authority editing_authority);

  /**
   * Returns the editing timestamp.
   * @return the {@link Date}editing timestamp
   */
  public Date getEditingTimestamp();

  /**
   * Sets the editing timestamp.
   * @param editing_timestamp the {@link Date} editing timestamp
   */
  public void setEditingTimestamp(Date editing_timestamp);

  /**
   * Returns the read timestamp.
   * @return the {@link Date} read timestamp
   */
  public Date getReadTimestamp();

  /**
   * Sets the read timestamp.
   * @param read_timestamp the {@link Date} read timestamp
   */
  public void setReadTimestamp(Date read_timestamp);

  /**
   * Returns the {@link Atom}s.
   * @return the {@link Atom}s
   */
  public Atom[] getAtoms();

  /**
   * Returns the {@link Atom}s sorted by the specified {@link Comparator}.
   * @param comparator the {@link Comparator} used to sort
   * @return the {@link Atom}s sorted by the specified {@link Comparator}
   */
  public Atom[] getSortedAtoms(Comparator comparator);

  /**
   * Returns the {@link Atom}s sorted by the default comparator.
   * @return the {@link Atom}s sorted by the default comparator
   */
  public Atom[] getSortedAtoms();

  /**
   * Add the specified {@link Atom}.
   * @param atom the {@link Atom} to add
   */
  public void addAtom(Atom atom);

  /**
   * Removes specified {@link Atom}.
   * @param atom the {@link Atom} to remove
   */
  public void removeAtom(Atom atom);

  /**
   * Removes all {@link Atom}s.
   */
  public void clearAtoms();

  /**
   * Removes all {@link ContextRelationship}s.
   */
  public void clearContextRelationships();

  /**
   * Returns the {@link ContextRelationship}s.
   * @return the {@link ContextRelationship}s
   */
  public ContextRelationship[] getContextRelationships();

  /**
   * Returns the {@link ContextRelationship}s sorted
   * by the specified {@link Comparator}.
   * @param comparator the {@link Comparator} used to sort
   * @return the sorted {@link ContextRelationship}s
   */
  public ContextRelationship[]
      getSortedContextRelationships(Comparator comparator);

  /**
   * Add the specified {@link ContextRelationship}.
   * @param context_rel the {@link ContextRelationship} to add
   */
  public void addContextRelationship(ContextRelationship context_rel);

  /**
   * Remove the specified {@link ContextRelationship}.
   * @param context_rel the {@link ContextRelationship} to remove
   */
  public void removeContextRelationship(ContextRelationship context_rel);

  /**
   * Indicates whether or not this concept has a chemical semantic type.
   * @return <code>true</code> if this has a chemical semantic type,
   * <code>false</code> otherwise.
   */
  public boolean hasChemicalSemanticType();

  /**
   * Indicates whether or not this has a semantic type.
   * @return <code>true</code> if this has a semantic type,
   * <code>false</code> otherwise.
   */
  public boolean hasSemanticType();

  /**
   * Indicates whether or not this concept contains a
   * current version MeSH main heading.
   * @return <code>true</code> if so,
   * <code>false</code> otherwise.
   */
  public boolean isCurrentMeSHMainHeading();

  /**
   * Indicates whether or not this is supplementary concept only.
   * @return <code>true</code> if this is supplementary concept only,
   * <code>false</code> otherwise.
   */
  public boolean isSupplementaryConceptOnly();

  /**
   * Indicates whether or not this is a non human concept.
   * @return <code>true</code> if this is a non human concept,
   * <code>false</code> otherwise.
   */
  public boolean isNonHuman();

  /**
   * Returns the semantic types.
   * @return the {@link ConceptSemanticType} of this concept
   */
  public ConceptSemanticType[] getSemanticTypes();

  /**
   * Returns the demotions.
   * @return the demoted {@link Relationship} of this concept
   */
  public Relationship[] getDemotions();

  /**
   * Returns the lexical relationships.
   * @return the lexical {@link Relationship}s of this concept
   */
  public Relationship[] getLexicalRelationships();

  /**
   * Returns {@link Atom}s kept by the specified {@link CoreDataRestrictor}.
   * @param restrictor the {@link CoreDataRestrictor}
   * @return the restricted {@link Atom}s
   */
  public Atom[] getRestrictedAtoms(CoreDataRestrictor restrictor);

  /**
   * Returns {@link ContextRelationship}s kept by the specified {@link CoreDataRestrictor}.
   * @param restrictor the {@link CoreDataRestrictor}
   * @return the restricted {@link ContextRelationship}s
   */
  public ContextRelationship[] getRestrictedContextRelationships(
      CoreDataRestrictor restrictor);

  /**
   * Indicates whether or not the concept contains ambiguous atoms.
   * @return <code>true</code> if the concept contains an ambiguous atom,
   * <code>false</code> otherwise
   */
  public boolean getAmbiguous();

  /**
   * Indicates whether or not the concept contains the specified {@link Atom}
   * @param atom the {@link Atom}
   * @return <code>true</code> if so,
   * <code>false</code> otherwise.
   */
  public boolean contains(Atom atom);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the {@link Concept} interface.
   */
  public class Default extends CoreData.Default implements Concept {

    //
    // Fields
    //

    private CUI cui = null;
    private Atom preferred_atom = null;
    private LoggedAction last_approval = null;
    private Authority editing_authority = null;
    private Date editing_timestamp = null;
    private Date read_timestamp = null;

    private ArrayList atoms = null;
    private ArrayList context_rels = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Concept}.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates a default {@link Concept} with the
     * specified concept identifier.
     * @param concept_id an concept id
     */
    public Default(int concept_id) {
      this();
      setIdentifier(new Identifier.Default(concept_id));
    }

    //
    // Implementation of Concept interface
    //

    /**
     * Implements {@link Concept#clear()}.
     */
    public void clear() {
      cui = null;
      preferred_atom = null;
      last_approval = null;
      editing_authority = null;
      editing_timestamp = null;

      if (atoms != null) {
        atoms.clear();
      }
      if (attributes != null) {
        attributes.clear();
      }
      if (context_rels != null) {
        context_rels.clear();
      }
      if (relationships != null) {
        relationships.clear();
      }
    }

    /**
     * Implements {@link Concept#getCUI()}.
     */
    public CUI getCUI() {
      return cui;
    }

    /**
     * Implements {@link Concept#getCUIs()}.
     */
    public CUI[] getCUIs() {
      Atom[] atoms = getAtoms();
      HashSet cuis = new HashSet();
      for (int i = 0; i < atoms.length; i++) {
        CUI cui = atoms[i].getLastReleaseCUI();
        if (cui != null && !cuis.contains(cui)) {
          cuis.add(cui);
        }
        cui = atoms[i].getLastAssignedCUI();
        if (cui != null && !cuis.contains(cui)) {
          cuis.add(cui);
        }
      }
      CUI[] sortedcuis = (CUI[]) cuis.toArray(new CUI[] {});
      Arrays.sort(sortedcuis);
      return sortedcuis;
    }

    /**
     * Implements {@link Concept#setCUI(CUI)}.
     */
    public void setCUI(CUI cui) {
      this.cui = cui;
    }

    /**
     * Implements {@link Concept#getPreferredAtom()}.
     */
    public Atom getPreferredAtom() {
      return preferred_atom;
    }

    /**
     * Implements {@link Concept#setPreferredAtom(Atom)}.
     */
    public void setPreferredAtom(Atom preferred_atom) {
      this.preferred_atom = preferred_atom;
    }

    /**
     * Implements {@link Concept#getApprovalAction()}.
     */
    public LoggedAction getApprovalAction() {
      return last_approval;
    }

    /**
     * Implements {@link Concept#setApprovalAction(LoggedAction)}.
     */
    public void setApprovalAction(LoggedAction last_approval) {
      this.last_approval = last_approval;
    }

    /**
     * Implements {@link Concept#getEditingAuthority()}.
     */
    public Authority getEditingAuthority() {
      return editing_authority;
    }

    /**
     * Implements {@link Concept#setEditingAuthority(Authority)}.
     */
    public void setEditingAuthority(Authority editing_authority) {
      this.editing_authority = editing_authority;
    }

    /**
     * Implements {@link Concept#getEditingTimestamp()}.
     */
    public Date getEditingTimestamp() {
      return editing_timestamp;
    }

    /**
     * Implements {@link Concept#setEditingTimestamp(Date)}.
     */
    public void setEditingTimestamp(Date editing_timestamp) {
      this.editing_timestamp = editing_timestamp;
    }

    /**
     * Implements {@link Concept#getReadTimestamp()}.
     */
    public Date getReadTimestamp() {
      return read_timestamp;
    }

    /**
     * Implements {@link Concept#setReadTimestamp(Date)}.
     */
    public void setReadTimestamp(Date read_timestamp) {
      this.read_timestamp = read_timestamp;
    }

    /**
     * Implements {@link Concept#getAtoms()}.
     */
    public Atom[] getAtoms() {
      if (atoms == null) {
        return new Atom[0];
      } else {
        return (Atom[]) atoms.toArray(new Atom[] {});
      }
    }

    /**
     * Implements {@link Concept#getSortedAtoms(Comparator)}.
     */
    public Atom[] getSortedAtoms(Comparator comparator) {
      if (atoms == null) {
        return new Atom[0];
      }
      Atom[] arrays = (Atom[]) atoms.toArray(new Atom[] {});
      Arrays.sort(arrays, comparator);
      return arrays;
    }

    /**
     * Implements {@link Concept#getSortedAtoms()}
     * with {@link Concept.Default#DEFAULT_ATOM_COMPARATOR}.
     */
    public Atom[] getSortedAtoms() {
      return getSortedAtoms(DEFAULT_ATOM_COMPARATOR);
    }

    /**
     * Implements {@link Concept#addAtom(Atom)}.
     */
    public void addAtom(Atom atom) {
      if (atoms == null) {
        atoms = new ArrayList();
      }
      atoms.add(atom);
    }

    /**
     * Implements {@link Concept#removeAtom(Atom)}.
     */
    public void removeAtom(Atom atom) {
      if (atoms == null) {
        return;
      }
      atoms.remove(atom);
    }

    /**
     * Implements {@link Concept#clearAtoms()}.
     */
    public void clearAtoms() {
      if (atoms != null) {
        atoms.clear();
      }
    }

    /**
     * Implements {@link Concept#clearContextRelationships()}.
     */
    public void clearContextRelationships() {
      if (context_rels != null) {
        context_rels.clear();
      }
    }

    /**
     * Implements {@link Concept#getContextRelationships()}.
     */
    public ContextRelationship[] getContextRelationships() {
      if (context_rels == null) {
        return new ContextRelationship[0];
      } else {
        return (ContextRelationship[])
            context_rels.toArray(new ContextRelationship[] {});
      }
    }

    /**
     * Implements {@link Concept#getSortedContextRelationships(Comparator)}.
     */
    public ContextRelationship[] getSortedContextRelationships(Comparator
        comparator) {
      if (context_rels == null) {
        return new ContextRelationship[0];
      }
      ContextRelationship[] arrays = (ContextRelationship[])
          context_rels.toArray(new ContextRelationship[] {});
      Arrays.sort(arrays, comparator);
      return arrays;
    }

    /**
     * Implements {@link Concept#addContextRelationship(ContextRelationship)}.
     */
    public void addContextRelationship(ContextRelationship context_rel) {
      if (context_rels == null) {
        context_rels = new ArrayList();
      }
      context_rels.add(context_rel);
    }

    /**
         * Implements {@link Concept#removeContextRelationship(ContextRelationship)}.
     */
    public void removeContextRelationship(ContextRelationship context_rel) {
      if (context_rels == null) {
        return;
      }
      context_rels.remove(context_rel);
    }

    /**
     * Implements {@link Concept#hasChemicalSemanticType()}.
     */
    public boolean hasChemicalSemanticType() {
      if (attributes == null) {
        return false;
      }
      for (int i = 0; i < attributes.size(); i++) {
        Object attribute = (Attribute) attributes.get(i);
        if (attribute instanceof ConceptSemanticType) {
          if (  ( ((ConceptSemanticType) attribute).isChemical() ||
        		  ((ConceptSemanticType) attribute).isEditingChemical() )&&
              ((ConceptSemanticType) attribute).isReleasable()  ) 
          {
            return true;
          }
        }
      }
      return false;
    }

    /**
     * Implements {@link Concept#hasSemanticType()}.
     */
    public boolean hasSemanticType() {
      if (attributes == null) {
        return false;
      }
      for (int i = 0; i < attributes.size(); i++) {
        Attribute attribute = (Attribute) attributes.get(i);
        if (attribute.getName().equals("SEMANTIC_TYPE") &&
            attribute.isReleasable()) {
          return true;
        }
      }
      return false;
    }

    /**
     * Implements {@link Concept#isCurrentMeSHMainHeading()}.
     */
    public boolean isCurrentMeSHMainHeading() {
      if (atoms == null) {
        return false;
      }
      for (int i = 0; i < atoms.size(); i++) {
        Atom atom = (Atom) atoms.get(i);
        Termgroup termgroup = atom.getTermgroup();
        Source source = atom.getSource();
        if (termgroup != null && source != null
            && termgroup.getTermType().equals("MH")
            && source.getRootSourceAbbreviation().equals("MSH")
            && source.isCurrent()) {
          return true;
        }
      }
      return false;
    }

    /**
     * Implements {@link Concept#isSupplementaryConceptOnly()}.
     */
    public boolean isSupplementaryConceptOnly() {
      if (atoms == null) {
        return false;
      }
      for (int i = 0; i < atoms.size(); i++) {
        Atom atom = (Atom) atoms.get(i);
        Source source = atom.getSource();
        Code code = atom.getCode();
        if (! (source.getRootSourceAbbreviation().equals("MSH"))
            || ! (code.toString().startsWith("C"))) {
          return false;
        }
      }
      return true;
    }

    /**
     * Implements {@link Concept#isNonHuman()}.
     */
    public boolean isNonHuman() {
      if (attributes == null) {
        return false;
      }
      for (int i = 0; i < attributes.size(); i++) {
        Attribute attribute = (Attribute) attributes.get(i);
        if (attribute.getName().equals(Attribute.NON_HUMAN) &&
            attribute.isReleasable()) {
          return true;
        }
      }
      return false;
    }

    /**
     * Implements {@link Concept#getSemanticTypes()}.
     */
    public ConceptSemanticType[] getSemanticTypes() {
      if (attributes == null) {
        return new ConceptSemanticType[0];
      }

      ArrayList stys = new ArrayList();
      Iterator iterator = attributes.iterator();
      while (iterator.hasNext()) {
        Attribute attribute = (Attribute) iterator.next();
        if (attribute instanceof ConceptSemanticType) {
          stys.add(attribute);
        }
      }
      return (ConceptSemanticType[]) stys.toArray(new ConceptSemanticType[] {});
    }

    /**
     * Implements {@link Concept#getDemotions()}.
     */
    public Relationship[] getDemotions() {
      if (relationships == null) {
        return new Relationship[0];
      }
      ArrayList dems = new ArrayList();
      Iterator iterator = relationships.iterator();
      Relationship relationship;
      while (iterator.hasNext()) {
        relationship = (Relationship) iterator.next();
        if (relationship.isDemoted()) {
          dems.add(relationship);
        }
      }

      return (Relationship[]) dems.toArray(new Relationship[] {});
    }

    /**
     * Implements {@link Concept#getLexicalRelationships()}.
     */
    public Relationship[] getLexicalRelationships() {
      if (relationships == null) {
        return new Relationship[0];
      }
      ArrayList lex_rels = new ArrayList();
      Iterator iterator = relationships.iterator();
      while (iterator.hasNext()) {
        Relationship relationship = (Relationship) iterator.next();
        if (relationship.getName().equals("SFO/LFO")) {
          lex_rels.add(relationship);
        }
      }
      return (Relationship[]) lex_rels.toArray(new Relationship[] {});
    }

    /**
     * Implements {@link Concept#getRestrictedAtoms(CoreDataRestrictor)}.
     */
    public Atom[] getRestrictedAtoms(CoreDataRestrictor restrictor) {
      if (atoms == null) {
        return new Atom[0];
      }
      ArrayList ra = new ArrayList();
      Iterator iterator = atoms.iterator();
      while (iterator.hasNext()) {
        Atom atom = (Atom) iterator.next();
        if (restrictor.keep(atom)) {
          ra.add(atom);
        }
      }
      Collections.sort(ra, restrictor);
      return (Atom[]) ra.toArray(new Atom[] {});
    }

    /**
     * Implements {@link Concept#getRestrictedContextRelationships(CoreDataRestrictor)}.
     */
    public ContextRelationship[] getRestrictedContextRelationships(
        CoreDataRestrictor restrictor) {
      if (context_rels == null) {
        return new ContextRelationship[0];
      }
      ArrayList rcr = new ArrayList();
      Iterator iterator = context_rels.iterator();
      while (iterator.hasNext()) {
        ContextRelationship crel = (ContextRelationship) iterator.next();
        if (restrictor.keep(crel)) {
          rcr.add(crel);
        }
      }
      Collections.sort(rcr, restrictor);
      return (ContextRelationship[]) rcr.toArray(new ContextRelationship[] {});
    }

    /**
     * Implements {@link Concept#getAmbiguous()}.
     */
    public boolean getAmbiguous() {
      if (atoms == null) {
        return false;
      }
      for (int i = 0; i < atoms.size(); i++) {
        Atom atom = (Atom) atoms.get(i);
        if (atom.isAmbiguous()) {
          return true;
        }
      }
      return false;
    }

    /**
     * Implements {@link Concept#contains(Atom)}.
     */
    public boolean contains(Atom atom) {
      return atoms.contains(atom);
    }

  }
}
