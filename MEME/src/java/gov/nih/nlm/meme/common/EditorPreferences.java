/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  EditorPreferences
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents information about editors
 * tracked by the system, including their preferences
 * for using the editing interface.
 *
 * @author MEME Group
 */

public interface EditorPreferences {

  /**
   * Returns the initials.
   * @return the initials
   */
  public String getInitials();

  /**
   * Sets the initials.
   * @param initials the initials
   */
  public void setInitials(String initials);

  /**
   * Returns the user name.
   * @return user name
   */
  public String getUserName();

  /**
   * Sets the user name.
   * @param username the user name
   */
  public void setUserName(String username);

  /**
   * Returns the editor level.
   * @return editor level
   */
  public int getEditorLevel();

  /**
   * Sets the editor level.
   * @param editor_level the editor level
   */
  public void setEditorLevel(int editor_level);

  /**
   * Returns the current status.
   * @return current status
   */
  public boolean isCurrent();

  /**
   * Sets the current status.
   * @param is_current status
   */
  public void setIsCurrent(boolean is_current);

  /**
   * Returns the editor group.
   * @return editor group
   */
  public String getEditorGroup();

  /**
   * Sets the editor group.
   * @param group editor group
   */
  public void setEditorGroup(String group);

  /**
   * Returns the {@link Authority}.
   * @return the {@link Authority}
   */
  public Authority getAuthority();

  /**
   * Sets the {@link Authority}.
   * @param authority the {@link Authority}
   */
  public void setAuthority(Authority authority);

  /**
   * Indicates whether or not the editor wants concept information displayed.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean showConcept();

  /**
   * Sets the flag indicating whether or not the editor wants
   * concept information displayed.
   * @param show_concept the show concept flag
   */
  public void setShowConcept(boolean show_concept);

  /**
   * Indicates whether or not the editor wants atom information displayed.
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @return show atom status
   */
  public boolean showAtoms();

  /**
   * Sets the flag indicating whether or not the editor wants
   * atom information displayed.
   * @param show_atoms the show atom flag
   */
  public void setShowAtoms(boolean show_atoms);

  /**
   * Indicates whether or not the editor wants attribute information displayed.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean showAttributes();

  /**
   * Sets the flag indicating whether or not the editor wants
   * attribute information displayed.
   * @param show_attributes the show attribute flag
   */
  public void setShowAttributes(boolean show_attributes);

  /**
       * Indicates whether or not the editor wants relationship information displayed.
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @return show relationship status
   */
  public boolean showRelationships();

  /**
   * Sets the flag indicating whether or not the editor wants
   * relationship information displayed.
   * @param show_relationships the show relationships flag
   */
  public void setShowRelationships(boolean show_relationships);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link EditorPreferences} interface.
   */
  public class Default implements EditorPreferences {

    //
    // Fields
    //

    private String initials = null;
    private String username = null;
    private int editor_level = 0;
    private boolean is_current = false;
    private String group = null;
    private Authority authority = null;
    private boolean show_concept = false;
    private boolean show_atoms = false;
    private boolean show_attributes = false;
    private boolean show_relationships = false;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link EditorPreferences}.
     */
    public Default() {}

    //
    // Overridden Object methods
    //

    /**
     * Implements an equality function based on initials
     * and user name.
     * @param object the {@link Object} to compare to
     * @return <code>true</code> if objects are equal;
     * otherwise <code>false</code>
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof EditorPreferences))) {
        return false;
      }
      EditorPreferences e = (EditorPreferences) object;
      return initials.equals(e.getInitials()) &&
          username.equals(e.getUserName());
    }

    /**
     * Returns an <code>int</code> hashcode.
     * @return an <code>int</code> hashcode
     */
    public int hashCode() {
      return (initials + username).hashCode();
    }

    /**
     * Returns a {@link String} representation of the object.
     * @return a {@link String} representation of the object
     */
    public String toString() {
      return "E-" + initials;
    }

    //
    // Methods
    //

    /**
     * Implements {@link EditorPreferences#getInitials()}.
     */
    public String getInitials() {
      return initials;
    }

    /**
     * Implements {@link EditorPreferences#setInitials(String)}.
     */
    public void setInitials(String initials) {
      this.initials = initials;
    }

    /**
     * Implements {@link EditorPreferences#getUserName()}.
     */
    public String getUserName() {
      return username;
    }

    /**
     * Implements {@link EditorPreferences#setUserName(String)}.
     */
    public void setUserName(String username) {
      this.username = username;
    }

    /**
     * Implements {@link EditorPreferences#getEditorLevel()}.
     */
    public int getEditorLevel() {
      return editor_level;
    }

    /**
     * Implements {@link EditorPreferences#setEditorLevel(int)}.
     */
    public void setEditorLevel(int editor_level) {
      this.editor_level = editor_level;
    }

    /**
     * Implements {@link EditorPreferences#isCurrent()}.
     */
    public boolean isCurrent() {
      return is_current;
    }

    /**
     * Implements {@link EditorPreferences#setIsCurrent(boolean)}.
     */
    public void setIsCurrent(boolean is_current) {
      this.is_current = is_current;
    }

    /**
     * Implements {@link EditorPreferences#getEditorGroup()}.
     */
    public String getEditorGroup() {
      return group;
    }

    /**
     * Implements {@link EditorPreferences#setEditorGroup(String)}.
     */
    public void setEditorGroup(String group) {
      this.group = group;
    }

    /**
     * Implements {@link EditorPreferences#getAuthority()}.
     */
    public Authority getAuthority() {
      return authority;
    }

    /**
     * Implements {@link EditorPreferences#setAuthority(Authority)}.
     */
    public void setAuthority(Authority authority) {
      this.authority = authority;
    }

    /**
     * Implements {@link EditorPreferences#showConcept()}.
     */
    public boolean showConcept() {
      return show_concept;
    }

    /**
     * Implements {@link EditorPreferences#setShowConcept(boolean)}.
     */
    public void setShowConcept(boolean show_concept) {
      this.show_concept = show_concept;
    }

    /**
     * Implements {@link EditorPreferences#showAtoms()}.
     */
    public boolean showAtoms() {
      return show_atoms;
    }

    /**
     * Implements {@link EditorPreferences#setShowAtoms(boolean)}.
     */
    public void setShowAtoms(boolean show_atoms) {
      this.show_atoms = show_atoms;
    }

    /**
     * Implements {@link EditorPreferences#showAttributes()}.
     */
    public boolean showAttributes() {
      return show_attributes;
    }

    /**
     * Implements {@link EditorPreferences#setShowAttributes(boolean)}.
     */
    public void setShowAttributes(boolean show_attributes) {
      this.show_attributes = show_attributes;
    }

    /**
     * Implements {@link EditorPreferences#showRelationships()}.
     */
    public boolean showRelationships() {
      return show_relationships;
    }

    /**
     * Implements {@link EditorPreferences#setShowRelationships(boolean)}.
     */
    public void setShowRelationships(boolean show_relationships) {
      this.show_relationships = show_relationships;
    }

  }
}
