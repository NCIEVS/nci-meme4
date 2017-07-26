/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Atom
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;
import java.util.Date;

/**
 * Generically represents an atomic unit of meaning in a {@link Concept}.
 * Atoms are also known as a <i>terms</i> or a <i>names</i>.  It is both the
 * string containing the actual named thing and all of the associated
     * information, like {@link Code}s, {@link Source}s and {@link Termgroup}s, etc.
 *
 * @author MEME Group
 */
public class MRDAtom  {

    // Fields
    //
    private Termgroup termgroup = null;
    private String code = null;
    private String last_release_cui = null;
    private String cui = null;
    private Rank last_release_rank = new Rank.Default(0);
    private Rank rank = new Rank.Default(0);
    private MRDConcept concept = null;
    private MEMEString meme_string = null;
    private String aui = null;
    private String scui = null;
    private String sdui = null;
    private String saui = null;
    private int atomId = 0;
    private Source source = null;
    private String suppressible = null;
    private Authority authority = null;
    private Date timestamp = null;
    private Date insertionDate = null;
    private String tty = null;
    
    private ArrayList context_rels = null;
    private boolean ambig_flag = false;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Atom}.
     */
    public MRDAtom() {
      super();
      //meme_string = new MEMEString.Default();
    }

    /**
     * Instantiates a default {@link Atom} with the specified
     * atom identifier.
     * @param atom_id and <code>int</code> representation of the atom id
     */
    public MRDAtom(int atom_id) {
      this();
      this.atomId = atom_id;
    }

    public MRDAtom(String aui) {
    	this();
    	this.aui = aui;
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
      last_release_rank = new Rank.Default(0);
      cui = null;
      concept = null;
      meme_string = null;
      aui = null;
      source = null;
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
    public String getCode() {
      return code;
    }

    /**
     * Implements {@link Atom#setCode(Code)}.
     */
    public void setCode(String code) {
      this.code = code;
    }

    /**
     * Implements {@link Atom#getLastReleaseCUI()}.
     */
    public String getLastReleaseCUI() {
      return last_release_cui;
    }

    /**
     * Implements {@link Atom#setLastReleaseCUI(CUI)}.
     */
    public void setLastReleaseCUI(String last_release_cui) {
      this.last_release_cui = last_release_cui;
    }

    /**
     * Implements {@link Atom#getLastReleaseRank()}.
     */
    public Rank getLastReleaseRank() {
    	return last_release_rank;
    }

    /**
     * Implements {@link Atom#setLastReleaseRank(Rank)}.
     */
    public void setLastReleaseRank(Rank last_release_rank) {
        this.last_release_rank = last_release_rank;
      }
    
    /**
     * Implements {@link Atom#getLastAssignedCUI()}.
     */
    public String getCUI() {
      return cui;
    }

    /**
     * Implements {@link Atom#setLastAssignedCUI(CUI)}.
     */
    public void setCUI(String cui) {
      this.cui = cui;
    }

    /**
     * Implements {@link Atom#getConcept()}.
     */
    public MRDConcept getConcept() {
      return concept;
    }

    /**
     * Implements {@link Atom#setConcept(MRDConcept)}.
     */
    public void setConcept(MRDConcept concept) {
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
    public String getAUI() {
      return aui;
    }

    /**
     * Implements {@link Atom#setAUI(AUI)}.
     */
    public void setAUI(String aui) {
      this.aui = aui;
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
     * Implements {@link Atom#setSourceConceptIdentifier(Identifier)}.
     */
    public void setSourceConceptIdentifier(String scui) {
      this.scui = scui;
    }

    /**
     * Implements {@link Atom#getSourceConceptIdentifier()}.
     */
    public String getSourceConceptIdentifier() {
      return scui;
    }

    /**
     * Implements {@link Atom#setSourceDescriptorIdentifier(Identifier)}.
     */
    public void setSourceDescriptorIdentifier(String sdui) {
      this.sdui = sdui;
    }

    /**
     * Implements {@link Atom#getSourceDescriptorIdentifier()}.
     */
    public String getSourceDescriptorIdentifier() {
      return sdui;
    }
    
    /**
     * Implements {@link Atom#setSourceDescriptorIdentifier(Identifier)}.
     */
    public void setSourceAuiIdentifier(String saui) {
      this.saui = saui;
    }

    /**
     * Implements {@link Atom#getSourceDescriptorIdentifier()}.
     */
    public String getSourceAuiIdentifier() {
      return saui;
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
    
    public int getAtomId() {
    	return atomId;
    }
    public void setAtomId(int atomId) {
    	this.atomId = atomId;
    }

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Authority getAuthority() {
		return authority;
	}

	public void setAuthority(Authority authority) {
		this.authority = authority;
	}

	public Date getInsertionDate() {
		return insertionDate;
	}

	public void setInsertionDate(Date insertionDate) {
		this.insertionDate = insertionDate;
	}

	public String getSuppressible() {
		return suppressible;
	}

	public void setSuppressible(String suppressible) {
		this.suppressible = suppressible;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getTty() {
		return tty;
	}

	public void setTty(String tty) {
		this.tty = tty;
	}
}

