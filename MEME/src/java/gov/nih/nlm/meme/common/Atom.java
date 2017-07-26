/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Atom
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Generically represents an atomic unit of meaning in a {@link Concept}.
 * Atoms are also known as a <i>terms</i> or a <i>names</i>.  It is both the
 * string containing the actual named thing and all of the associated
     * information, like {@link Code}s, {@link Source}s and {@link Termgroup}s, etc.
 *
 * @author MEME Group
 */
public interface Atom extends MEMEString, ConceptElement {

  /**
   * Empties the atom.
   */
  public void clear();

  /**
   * Returns the {@link Termgroup}.
   * @return the {@link Termgroup}
   */
  public Termgroup getTermgroup();

  /**
   * Sets the {@link Termgroup}.
   * @param termgroup the {@link Termgroup}
   */
  public void setTermgroup(Termgroup termgroup);

  /**
   * Returns the {@link Code}.
   * @return the {@link Code}
   */
  public Code getCode();

  /**
   * Sets the {@link Code}.
   * @param code {@link Code}
   */
  public void setCode(Code code);

  /**
   * Returns the last {@link CUI} this atom was released with.
   * @return the last {@link CUI} this atom was released with
   */
  public CUI getLastReleaseCUI();

  /**
   * Sets the last release {@link CUI}.
   * @param last_release_cui the last release {@link CUI}
   */
  public void setLastReleaseCUI(CUI last_release_cui);

  /**
   * Returns the last {@link CUI} assigned to this atom.
   * @return the last {@link CUI} assigned to this atom
   */
  public CUI getLastAssignedCUI();

  /**
   * Sets the {@link CUI} assigned to this atom
   * @param last_assigned_cui the last assigned {@link CUI}.
   */
  public void setLastAssignedCUI(CUI last_assigned_cui);

  /**
   * Returns the {@link Concept} containing this atom.
   * @return the {@link Concept}
   */
  public Concept getConcept();

  /**
   * Sets the {@link Concept} containing this atom.
   * @param concept the {@link Concept}
   */
  public void setConcept(Concept concept);

  /**
   * Returns the {@link Language}.
   * @return the {@link Language}
   */
  public Language getLanguage();

  /**
   * Sets the {@link Language}.
   * @param language the {@link Language}
   */
  public void setLanguage(Language language);

  /**
   * Returns the index words.
   * @return the index words
   */
  public String[] getIndexWords();

  /**
   * Sets the index words.
   * @param words the index words
   */
  public void setIndexWords(String[] words);

  /**
   * Adds the specified index word.
   * @param word the index word
   */
  public void addIndexWord(String word);

  /**
   * Returns the normalized index words.
   * @return the normalized index words
   */
  public String[] getNormalizedIndexWords();

  /**
   * Sets the normalized index words.
   * @param words the normalized index words
   */
  public void setNormalizedIndexWords(String[] words);

  /**
   * Adds the specified normalized index word.
   * @param word the normalized index word
   */
  public void addNormalizedIndexWord(String word);

  /**
   * Returns the normalized strings.  These are the result of running
   * the atom name through the LVG "norm" program.
   * @return the normalized strings
   */
  public String[] getNormalizedIndexStrings();

  /**
   * Sets the normalized strings.  These are the result of running
   * the atom name through the LVG "norm" program.
   * @param strings the normalized strings
   */
  public void setNormalizedIndexStrings(String[] strings);

  /**
   * Adds the specified normalized string.
   * @param string the normalized string
   */
  public void addNormalizedIndexString(String string);

  /**
   * Sets the flag that determines whether or not this is ambiguous.
   * @param flag <code>true</code> if it is, <code>false</code> otherwise
   */
  public void setIsAmbiguous(boolean flag);

  /**
   * Indicates whether or not this is ambiguous.
   * @return <code>true</code> if this atom is ambiguous,
   *         <code>false</code> otherwise
   */
  public boolean isAmbiguous();

  /**
   * Sets the AUI (Atom Unique Identifier) for this atom.
   * @param aui the {@link AUI}
   */
  public void setAUI(AUI aui);

  /**
   * Returns the AUI (Atom Unique Identifier) for this atom.
   * @return the {@link AUI}
   */
  public AUI getAUI();

  /**
   * Adds the specified translation atom.
   * @param foreign_atom the translation {@link Atom}}
   */
  public void addTranslationAtom(Atom foreign_atom);

  /**
   * Removes the specified translation atom.
   * @param foreign_atom the translation {@link Atom} to remove
   */
  public void removeTranslationAtom(Atom foreign_atom);

  /**
   * Clears translation atoms.
   */
  public void clearTranslationAtoms();

  /**
   * Sets the specified translation {@link Atom}s.
   * @param atoms the translation {@link Atom}s
   */
  public void setTranslationAtoms(Atom[] atoms);

  /**
   * Returns the translation {@link Atom}s.
   * @return the translation {@link Atom}s
   */
  public Atom[] getTranslationAtoms();

  /**
   * Clears the {@link ContextRelationship}s.
   */
  public void clearContextRelationships();

  /**
   * Returns the {@link ContextRelationship}s.
   * @return the {@link ContextRelationship}s
   */
  public ContextRelationship[] getContextRelationships();

  /**
   * Returns the {@link ContextRelationship}s of this atom as sorted by
   * the specified {@link Comparator}.
   * @param comparator the {@link Comparator}
   * @return the sorted {@link ContextRelationship}s
   */
  public ContextRelationship[]
      getSortedContextRelationships(Comparator comparator);

  /**
   * Adds the specified {@link ContextRelationship}.
   * @param context_rel the {@link ContextRelationship} to add
   */
  public void addContextRelationship(ContextRelationship context_rel);

  /**
   * Removes the specified {@link ContextRelationship}.
   * @param context_rel the {@link ContextRelationship} to remove
   */
  public void removeContextRelationship(ContextRelationship context_rel);

  /**
   * Returns the best {@link SafeReplacementFact}.
   * @return the best {@link SafeReplacementFact}
   */
  public SafeReplacementFact getBestSafeReplacementFact();

  /**
   * Returns the {@link SafeReplacementFact}s.
   * @return the {@link SafeReplacementFact}s
   */
  public SafeReplacementFact[] getSafeReplacementFacts();

  /**
   * Indicates whether or not the specified {@link Atom} is
   * a safe replacement for this {@link Atom}.
   * @param atom another {@link Atom}
   * @return <code>true</code> if the specified atom is a safe replacement,
   *         <code>false</code> otherwise
   */
  public boolean isSafeReplacementFor(Atom atom);

  /**
   * Adds the specified {@link SafeReplacementFact}.
   * @param srf the {@link SafeReplacementFact} to add
   */
  public void addSafeReplacementFact(SafeReplacementFact srf);

  /**
   * Removes the specified {@link SafeReplacementFact}.
   * @param srf the {@link SafeReplacementFact} to remove
   */
  public void removeSafeReplacementFact(SafeReplacementFact srf);

  /**
   * Sets the source concept {@link Identifier}.
   * @param scui the source concept {@link Identifier}
   */
  public void setSourceConceptIdentifier(Identifier scui);

  /**
   * Returns the source concept {@link Identifier}.
   * @return the source concept {@link Identifier}
   */
  public Identifier getSourceConceptIdentifier();

  /**
   * Sets the source descriptor {@link Identifier}.
   * @param sdui the source descriptor {@link Identifier}
   */
  public void setSourceDescriptorIdentifier(Identifier sdui);

  /**
   * Returns the source descriptor {@link Identifier}.
   * @return the source descriptor {@link Identifier}
   */
  public Identifier getSourceDescriptorIdentifier();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Atom} interface.
   */
  public class Default extends CoreData.Default implements Atom {

    //
    // Fields
    //

    private Termgroup termgroup = null;
    private Code code = null;
    private CUI last_release_cui = null;
    private CUI last_assigned_cui = null;
    private Concept concept = null;
    private MEMEString meme_string = null;
    private AUI aui = null;
    private Identifier scui = null;
    private Identifier sdui = null;
    private ArrayList foreign_atoms = null;
    private ArrayList context_rels = null;
    private ArrayList word_index = null;
    private ArrayList normwrd_index = null;
    private ArrayList normstr_index = null;
    protected List sr_facts = null;
    private boolean ambig_flag = false;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Atom}.
     */
    public Default() {
      super();
      //meme_string = new MEMEString.Default();
    }

    /**
     * Instantiates a default {@link Atom} with the specified
     * atom identifier.
     * @param atom_id and <code>int</code> representation of the atom id
     */
    public Default(int atom_id) {
      this();
      setIdentifier(new Identifier.Default(atom_id));
    }

    //
    // Implementation of Comparable interface
    //

    /**
     * Implements an {@link Atom} ordering function based on rank.
     * @param object the {@link Object} to compare to
     * @return an <code>int</code> value indicating the sort order
     */
    public int compareTo(Object object) {
      // Compare ranks
      Atom atom = (Atom) object;
      int i = getRank().compareTo(atom.getRank());

      // If ranks are equal, compare SUIs, lower one wins
      if (i == 0 && getSUI() != null) {
        i = getSUI().compareTo(atom.getSUI());

        // If ranks are equal, compare identifiers, lower one wins
      }
      if (i == 0 && getIdentifier() != null) {
        return getIdentifier().intValue() - atom.getIdentifier().intValue();
      }

      return i;
    }

    //
    // Implementation of Atom interface
    //

    /**
     * Implements {@link Atom#clear()}.
     */
    public void clear() {
      // Null everything
      termgroup = null;
      code = null;
      last_release_cui = null;
      last_assigned_cui = null;
      concept = null;
      meme_string = null;
      aui = null;
      if (context_rels != null) {
        context_rels.clear();
      }
      if (word_index != null) {
        word_index.clear();
      }
      if (normwrd_index != null) {
        normwrd_index.clear();
      }
      if (normstr_index != null) {
        normstr_index.clear();
      }
      if (sr_facts != null) {
        sr_facts.clear();
      }
      if (attributes != null) {
        attributes.clear();
      }
      if (relationships != null) {
        relationships.clear();

      }
    }

    /**
     * Implements {@link Atom#getTermgroup()}.
     */
    public Termgroup getTermgroup() {
      return termgroup;
    }

    /**
     * Implements {@link Atom#setTermgroup(Termgroup)}.
     */
    public void setTermgroup(Termgroup termgroup) {
      this.termgroup = termgroup;
    }

    /**
     * Implements {@link Atom#getCode()}.
     */
    public Code getCode() {
      return code;
    }

    /**
     * Implements {@link Atom#setCode(Code)}.
     */
    public void setCode(Code code) {
      this.code = code;
    }

    /**
     * Implements {@link Atom#getLastReleaseCUI()}.
     */
    public CUI getLastReleaseCUI() {
      return last_release_cui;
    }

    /**
     * Implements {@link Atom#setLastReleaseCUI(CUI)}.
     */
    public void setLastReleaseCUI(CUI last_release_cui) {
      this.last_release_cui = last_release_cui;
    }

    /**
     * Implements {@link Atom#getLastAssignedCUI()}.
     */
    public CUI getLastAssignedCUI() {
      return last_assigned_cui;
    }

    /**
     * Implements {@link Atom#setLastAssignedCUI(CUI)}.
     */
    public void setLastAssignedCUI(CUI last_assigned_cui) {
      this.last_assigned_cui = last_assigned_cui;
    }

    /**
     * Implements {@link Atom#getConcept()}.
     */
    public Concept getConcept() {
      return concept;
    }

    /**
     * Implements {@link Atom#setConcept(Concept)}.
     */
    public void setConcept(Concept concept) {
      this.concept = concept;
    }

    /**
     * Implements {@link MEMEString#getLanguage()}.
     */
    public Language getLanguage() {
      return meme_string == null ? null : meme_string.getLanguage();
    }

    /**
     * Implements {@link MEMEString#setLanguage(Language)}.
     */
    public void setLanguage(Language language) {
      if (meme_string == null) {
        meme_string = new MEMEString.Default();
      }
      meme_string.setLanguage(language);
    }

    /**
     * Implements {@link Atom#getIndexWords()}.
     */
    public String[] getIndexWords() {
      if (word_index == null) {
        return new String[0];
      }
      return (String[]) word_index.toArray(new String[] {});
    }

    /**
     * Implements {@link Atom#setIndexWords(String[])}.
     */
    public void setIndexWords(String[] words) {
      if (word_index == null) {
        word_index = new ArrayList();
      }
      for (int i = 0; i < words.length; i++) {
        word_index.add(words[i]);
      }
    }

    /**
     * Implements {@link Atom#addIndexWord(String)}.
     */
    public void addIndexWord(String word) {
      if (word_index == null) {
        word_index = new ArrayList();
      }
      word_index.add(word);
    }

    /**
     * Implements {@link Atom#getNormalizedIndexWords()}.
     */
    public String[] getNormalizedIndexWords() {
      if (normwrd_index == null) {
        return new String[0];
      }
      return (String[]) normwrd_index.toArray(new String[] {});
    }

    /**
     * Implements {@link Atom#setNormalizedIndexWords(String[])}.
     */
    public void setNormalizedIndexWords(String[] words) {
      if (normwrd_index == null) {
        normwrd_index = new ArrayList();
      }
      for (int i = 0; i < words.length; i++) {
        normwrd_index.add(words[i]);
      }
    }

    /**
     * Implements {@link Atom#addNormalizedIndexWord(String)}.
     */
    public void addNormalizedIndexWord(String word) {
      if (normwrd_index == null) {
        normwrd_index = new ArrayList();
      }
      normwrd_index.add(word);
    }

    /**
     * Implements {@link Atom#getNormalizedIndexStrings()}.
     */
    public String[] getNormalizedIndexStrings() {
      if (normstr_index == null) {
        return new String[0];
      }
      return (String[]) normstr_index.toArray(new String[] {});
    }

    /**
     * Implements {@link Atom#setNormalizedIndexStrings(String[])}.
     */
    public void setNormalizedIndexStrings(String[] strings) {
      if (normstr_index == null) {
        normstr_index = new ArrayList();
      }
      for (int i = 0; i < strings.length; i++) {
        normstr_index.add(strings[i]);
      }
    }

    /**
     * Implements {@link Atom#addNormalizedIndexString(String)}.
     */
    public void addNormalizedIndexString(String string) {
      if (normstr_index == null) {
        normstr_index = new ArrayList();
      }
      normstr_index.add(string);
    }

    /**
     * Implements {@link Atom#setIsAmbiguous(boolean)}.
     */
    public void setIsAmbiguous(boolean flag) {
      this.ambig_flag = flag;
    }

    /**
     * Implements {@link Atom#isAmbiguous()}.
     */
    public boolean isAmbiguous() {
      return ambig_flag;
    }

    /**
     * Implements {@link Atom#getAUI()}.
     */
    public AUI getAUI() {
      return aui;
    }

    /**
     * Implements {@link Atom#setAUI(AUI)}.
     */
    public void setAUI(AUI aui) {
      this.aui = aui;
    }

    /**
     * Implements {@link Atom#addTranslationAtom(Atom)}
     */
    public void addTranslationAtom(Atom foreign_atom) {
      if (foreign_atoms == null) {
        foreign_atoms = new ArrayList();
      }
      foreign_atoms.add(foreign_atom);
    }

    /**
     * Implements {@link Atom#removeTranslationAtom(Atom)}
     */
    public void removeTranslationAtom(Atom foreign_atom) {
      if (foreign_atoms == null) {
        return;
      }
      foreign_atoms.remove(foreign_atom);
    }

    /**
     * Implements {@link Atom#clearTranslationAtoms()}
     */
    public void clearTranslationAtoms() {
      if (foreign_atoms != null) {
        foreign_atoms.clear();
      }
    }

    /**
     * Implements {@link Atom#setTranslationAtoms(Atom[])}
     */
    public void setTranslationAtoms(Atom[] atoms) {
      if (foreign_atoms == null) {
        foreign_atoms = new ArrayList();
      }
      for (int i = 0; i < atoms.length; i++) {
        foreign_atoms.add(atoms[i]);
      }
    }

    /**
     * Implements {@link Atom#getTranslationAtoms()}
     */
    public Atom[] getTranslationAtoms() {
      if (foreign_atoms == null) {
        return new Atom[0];
      } else {
        return (Atom[])
            foreign_atoms.toArray(new Atom[] {});
      }
    }

    /**
     * Implements {@link Atom#clearContextRelationships()}
     */
    public void clearContextRelationships() {
      if (context_rels != null) {
        context_rels.clear();
      }
    }

    /**
     * Implements {@link Atom#getContextRelationships()}.
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
     * Implements {@link Atom#getSortedContextRelationships(Comparator)}.
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
     * Implements {@link Atom#addContextRelationship(ContextRelationship)}.
     */
    public void addContextRelationship(ContextRelationship context_rel) {
      if (context_rels == null) {
        context_rels = new ArrayList();
      }
      context_rels.add(context_rel);
    }

    /**
     * Implements {@link Atom#removeContextRelationship(ContextRelationship)}.
     */
    public void removeContextRelationship(ContextRelationship context_rel) {
      if (context_rels == null) {
        return;
      }
      context_rels.remove(context_rel);
    }

    /**
     * Implements {@link Atom#getBestSafeReplacementFact()}.
     */
    public SafeReplacementFact getBestSafeReplacementFact() {

      if (sr_facts == null) {
        return null;
      }

      Iterator iterator = sr_facts.iterator();
      while (iterator.hasNext()) {
        SafeReplacementFact srf = (SafeReplacementFact) iterator.next();
        if (srf.getOldAtom().equals(this)) {
          return srf;
        }
      }
      return null;
    }

    /**
     * Implements {@link Atom#getSafeReplacementFacts()}.
     */
    public SafeReplacementFact[] getSafeReplacementFacts() {

      ArrayList srfs = new ArrayList();

      if (sr_facts == null) {
        return new SafeReplacementFact[0];
      }

      Iterator iterator = sr_facts.iterator();
      while (iterator.hasNext()) {
        SafeReplacementFact srf = (SafeReplacementFact) iterator.next();
        if (srf.getOldAtom().equals(this)) {
          srfs.add(srf);
        }
      }
      return (SafeReplacementFact[]) srfs.toArray(new SafeReplacementFact[] {});
    }

    /**
     * Implements {@link Atom#isSafeReplacementFor(Atom)}.
     * @param atom An object {@link Atom}.
     */
    public boolean isSafeReplacementFor(Atom atom) {
      if (sr_facts == null) {
        return false;
      }

      Iterator iterator = sr_facts.iterator();
      while (iterator.hasNext()) {
        SafeReplacementFact srf = (SafeReplacementFact) iterator.next();
        if (srf.getNewAtom().equals(this) &&
            srf.getOldAtom().equals(atom)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Implements {@link Atom#addSafeReplacementFact(SafeReplacementFact)}.
     */
    public void addSafeReplacementFact(SafeReplacementFact srf) {
      if (sr_facts == null) {
        sr_facts = new ArrayList();
      }
      sr_facts.add(srf);
      Collections.sort(sr_facts);
    }

    /**
     * Implements {@link Atom#removeSafeReplacementFact(SafeReplacementFact)}.
     */
    public void removeSafeReplacementFact(SafeReplacementFact srf) {
      if (sr_facts == null) {
        return;
      }
      sr_facts.remove(srf);
      Collections.sort(sr_facts);
    }

    /**
     * Implements {@link Atom#setSourceConceptIdentifier(Identifier)}.
     */
    public void setSourceConceptIdentifier(Identifier scui) {
      this.scui = scui;
    }

    /**
     * Implements {@link Atom#getSourceConceptIdentifier()}.
     */
    public Identifier getSourceConceptIdentifier() {
      return scui;
    }

    /**
     * Implements {@link Atom#setSourceDescriptorIdentifier(Identifier)}.
     */
    public void setSourceDescriptorIdentifier(Identifier sdui) {
      this.sdui = sdui;
    }

    /**
     * Implements {@link Atom#getSourceDescriptorIdentifier()}.
     */
    public Identifier getSourceDescriptorIdentifier() {
      return sdui;
    }

    //
    // Implementation of MEMEString interface
    //

    /**
     * Implements {@link MEMEString#getSUI()}.
     */
    public StringIdentifier getSUI() {
      return meme_string == null ? null : meme_string.getSUI();
    }

    /**
     * Implements {@link MEMEString#setSUI(StringIdentifier)}.
     */
    public void setSUI(StringIdentifier sui) {
      if (meme_string == null) {
        meme_string = new MEMEString.Default();
      }
      meme_string.setSUI(sui);
    }

    /**
     * Implements {@link MEMEString#getLUI()}.
     */
    public StringIdentifier getLUI() {
      return meme_string == null ? null : meme_string.getLUI();
    }

    /**
     * Implements {@link MEMEString#setLUI(StringIdentifier)}.
     */
    public void setLUI(StringIdentifier lui) {
      if (meme_string == null) {
        meme_string = new MEMEString.Default();
      }
      meme_string.setLUI(lui);
    }

    /**
     * Implements {@link MEMEString#getISUI()}.
     */
    public StringIdentifier getISUI() {
      return meme_string == null ? null : meme_string.getISUI();
    }

    /**
     * Implements {@link MEMEString#setISUI(StringIdentifier)}.
     */
    public void setISUI(StringIdentifier isui) {
      if (meme_string == null) {
        meme_string = new MEMEString.Default();
      }
      meme_string.setISUI(isui);
    }

    /**
     * Implements {@link MEMEString#toString()}.
     */
    public String toString() {
      return (meme_string == null) ? null : meme_string.toString();
    }

    /**
     * Implements {@link MEMEString#getString()}.
     */
    public String getString() {
      return meme_string == null ? null : meme_string.getString();
    }

    /**
     * Implements {@link MEMEString#setString(String)}.
     */
    public void setString(String string) {
      if (meme_string == null) {
        meme_string = new MEMEString.Default();
      }
      meme_string.setString(string);
    }

    /**
     * Implements {@link MEMEString#getNormalizedString()}.
     */
    public String getNormalizedString() {
      return meme_string == null ? null : meme_string.getNormalizedString();
    }

    /**
     * Implements {@link MEMEString#setNormalizedString(String)}.
     */
    public void setNormalizedString(String norm_string) {
      if (meme_string == null) {
        meme_string = new MEMEString.Default();
      }
      meme_string.setNormalizedString(norm_string);
    }

    /**
     * Implements {@link MEMEString#getBaseString()}.
     */
    public String getBaseString() {
      return meme_string == null ? null : meme_string.getBaseString();
    }

    /**
     * Implements {@link MEMEString#isBaseString()}.
     */
    public boolean isBaseString() {
      return meme_string == null ? false : meme_string.isBaseString();
    }

    /**
     * Implements {@link MEMEString#getBracketNumber()}.
     */
    public int getBracketNumber() {
      return meme_string == null ? 0 : meme_string.getBracketNumber();
    }

    /**
     * Implements {@link MEMEString#isBracketString()}.
     */
    public boolean isBracketString() {
      return meme_string == null ? false : meme_string.isBracketString();
    }

  }
}
