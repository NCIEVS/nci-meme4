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

public class MRDConcept  {
    protected Authority authority = null;
    protected LoggedAction last_action = null;
    protected Rank rank = null;

    protected String suppressible = "N";
    protected char tobereleased;
    protected char level;
    protected Date timestamp;
    protected Date insertion_date;

    private MRDAtom preferred_atom = null;
    
    /*
     * For Time Being All the atom and concept rels, attributes and contexts are part of a concept.
     */
    private ArrayList atoms = null;
    private ArrayList relationships = null;
    private ArrayList context_rels = null;
    protected ArrayList attributes = null;
    
    private String CUI = null;
    private String cuiName = null;
    private int conceptId = 0;


	public Authority getAuthority() {
		return authority;
	}

	public void setAuthority(Authority authority) {
		this.authority = authority;
	}

	public int getConceptId() {
		return conceptId;
	}

	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}

	public ArrayList getContext_rels() {
		return context_rels;
	}

	public void setContext_rels(ArrayList context_rels) {
		this.context_rels = context_rels;
	}

	public String getCuiName() {
		return cuiName;
	}

	public void setCuiName(String cuiName) {
		this.cuiName = cuiName;
	}

	public Date getInsertion_date() {
		return insertion_date;
	}

	public void setInsertion_date(Date insertion_date) {
		this.insertion_date = insertion_date;
	}

	public LoggedAction getLast_action() {
		return last_action;
	}

	public void setLast_action(LoggedAction last_action) {
		this.last_action = last_action;
	}

	public char getLevel() {
		return level;
	}

	public void setLevel(char level) {
		this.level = level;
	}

	public MRDAtom getPreferred_atom() {
		return preferred_atom;
	}

	public void setPreferred_atom(MRDAtom preferred_atom) {
		this.preferred_atom = preferred_atom;
	}

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public ArrayList getRelationships() {
		return relationships;
	}

	public void setRelationships(ArrayList relationships) {
		this.relationships = relationships;
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

	public char getTobereleased() {
		return tobereleased;
	}

	public void setTobereleased(char tobereleased) {
		this.tobereleased = tobereleased;
	}

	public void setAtoms(ArrayList atoms) {
		this.atoms = atoms;
	}

	public void setAttributes(ArrayList attributes) {
		this.attributes = attributes;
	}

	/**
     * Instantiates an empty default {@link Concept}.
     */
    public MRDConcept() {
    }

    /**
     * Instantiates a default {@link Concept} with the
     * specified concept identifier.
     * @param concept_id an concept id
     */
    public MRDConcept(String cui) {
      CUI = cui;;
    }

    //
    // Implementation of Concept interface
    //
    public void addRelationship(MRDRelationship rel) {
    	if(relationships == null) {
    		relationships = new ArrayList();
    	}
    	relationships.add(rel);
    }
    /**
     * Implements {@link Concept#clear()}.
     */
    public void clear() {
      CUI = null;
      preferred_atom = null;
      
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
    public String getCUI() {
      return CUI;
    }

    /**
     * Implements {@link Concept#setCUI(CUI)}.
     */
    public void setCUI(String cui) {
      this.CUI = cui;
    }

    /**
     * Implements {@link Concept#getPreferredAtom()}.
     */
    public MRDAtom getPreferredAtom() {
      return preferred_atom;
    }

    /**
     * Implements {@link Concept#setPreferredAtom(MRDAtom)}.
     */
    public void setPreferredAtom(MRDAtom preferred_atom) {
      this.preferred_atom = preferred_atom;
    }

    
    /**
     * Implements {@link Concept#getAtoms()}.
     */
    public MRDAtom[] getAtoms() {
      if (atoms == null) {
        return new MRDAtom[0];
      } else {
        return (MRDAtom[]) atoms.toArray(new MRDAtom[] {});
      }
    }

    /**
     * Implements {@link Concept#addAtom(MRDAtom)}.
     */
    public void addAtom(MRDAtom atom) {
      if (atoms == null) {
        atoms = new ArrayList();
      }
      atoms.add(atom);
    }

    /**
     * Implements {@link Concept#removeAtom(MRDAtom)}.
     */
    public void removeAtom(MRDAtom atom) {
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
    public MRDContextRelationship[] getContextRelationships() {
      if (context_rels == null) {
        return new MRDContextRelationship[0];
      } else {
        return (MRDContextRelationship[])
            context_rels.toArray(new MRDContextRelationship[] {});
      }
    }
    /**
     * Implements {@link Concept#addContextRelationship(ContextRelationship)}.
     */
    public void addContextRelationship(MRDContextRelationship context_rel) {
      if (context_rels == null) {
        context_rels = new ArrayList();
      }
      context_rels.add(context_rel);
    }

    /**
         * Implements {@link Concept#removeContextRelationship(ContextRelationship)}.
     */
    public void removeContextRelationship(MRDContextRelationship context_rel) {
      if (context_rels == null) {
        return;
      }
      context_rels.remove(context_rel);
    }


    /**
     * Implements {@link Concept#getAmbiguous()}.
     */
    public boolean getAmbiguous() {
      if (atoms == null) {
        return false;
      }
      for (int i = 0; i < atoms.size(); i++) {
        MRDAtom atom = (MRDAtom) atoms.get(i);
        if (atom.isAmbiguous()) {
          return true;
        }
      }
      return false;
    }


    /**
     * Implements {@link CoreData#addAttribute(Attribute)}.
     */
    public void addAttribute(MRDAttribute attribute) {
      if (attributes == null) {
        attributes = new ArrayList();
      }
      attributes.add(attribute);
    }

    /**
     * Implements {@link CoreData#removeAttribute(Attribute)}.
     */
    public void removeAttribute(MRDAttribute attribute) {
      if (attributes == null) {
        return;
      }
      attributes.remove(attribute);
    }

    /**
     * Implements {@link CoreData#clearAttributes()}.
     */
    public void clearAttributes() {
      if (attributes != null) {
        attributes.clear();
      }
    }

    /**
     * Implements {@link CoreData#getAttributes()}.
     */
    public MRDAttribute[] getAttributes() {
      if (attributes == null) {
        return new MRDAttribute[0];
      } else {
        return (MRDAttribute[]) attributes.toArray(new MRDAttribute[] {});
      }
    }
}

