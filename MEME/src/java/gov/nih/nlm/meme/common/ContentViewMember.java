/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ContentViewMember
 *
 * 10/30/2007 TTN (1-FN4GD): add get/set Atom methods  
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a content view member.
 *
 * @author MEME Group
 */

public interface ContentViewMember {

  /**
   * Returns the {@link ContentView}.
   * @return the {@link ContentView}
   */
  public ContentView getContentView();

  /**
   * Sets the {@link ContentView}.
   * @param cv the {@link ContentView}
   */
  public void setContentView(ContentView cv);

  /**
   * Returns the {@link Identifier}.
   * @return the {@link Identifier}
   */
  public Identifier getIdentifier();

  /**
   * Sets the {@link Identifier}.
   * @param meta_ui the {@link Identifier}
   */
  public void setIdentifier(Identifier meta_ui);
  
  
  /**
   * Returns the {@link Atom}.
   * @return the {@link Atom}
   */
  public Atom getAtom();

  /**
   * Sets the {@link Atom}.
   * @param meta_ui the {@link Atom}
   */
  public void setAtom(Atom atom);
  

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Atom} interface.
   */
  public class Default implements ContentViewMember {

    //
    // Fields
    //

    private ContentView cv = null;
    private Identifier meta_ui = null;
    private Atom atom = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Identifier}.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates this object passing an {@link Atom}.
     * @param atom an object {@link Atom}
     */
    public Default(Atom atom) {
      this();
      setIdentifier(atom.getAUI());
    }

    /**
     * Instantiates this object passing an {@link Attribute}.
     * @param attr an object {@link Attribute}
     */
    public Default(Attribute attr) {
      this();
      setIdentifier(attr.getATUI());
    }

    /**
     * Instantiates this object passing an {@link Concept}.
     * @param concept an object {@link Concept}
     */
    public Default(Concept concept) {
      this();
      setIdentifier(concept.getCUI());
    }

    /**
     * Instantiates this object passing an {@link Relationship}.
     * @param rel an object {@link Relationship}
     */
    public Default(Relationship rel) {
      this();
      setIdentifier(rel.getRUI());
    }

    //
    // Implementation of ContentViewMember interface
    //

    /**
     * Implements {@link ContentViewMember#getContentView()}
     */
    public ContentView getContentView() {
      return this.cv;
    }

    /**
     * Implements {@link ContentViewMember#setContentView(ContentView)}
     */
    public void setContentView(ContentView cv) {
      this.cv = cv;
    }

    /**
     * Implements {@link ContentViewMember#getIdentifier()}
     */
    public Identifier getIdentifier() {
      return this.meta_ui;
    }

    /**
     * Implements {@link ContentViewMember#setIdentifier(Identifier)}
     */
    public void setIdentifier(Identifier meta_ui) {
      this.meta_ui = meta_ui;
    }

    /* 
	 * Implements {@link ContentViewMember#getAtom()}
	 */
	public Atom getAtom() {
		
		return atom;
	}

	/* 
	 * Implements {@link ContentViewMember#setAtom(Atom)}
	 */
	public void setAtom(Atom atom) {
		this.atom = atom;
		
	}

	/**
     * Implements {@link Identifier#equals(Object)}.
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof ContentViewMember))) {
        return false;
      }
      return meta_ui.equals( ( (ContentViewMember) object).getIdentifier());
    }

  }
}