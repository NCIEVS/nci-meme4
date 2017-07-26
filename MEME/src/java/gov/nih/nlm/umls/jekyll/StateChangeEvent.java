/*
 * StateChangeEvent.java
 */

package gov.nih.nlm.umls.jekyll;

/**
 * @see <a href="src/StateChangeEvent.java.html">source </a>
 */
public class StateChangeEvent {

    // Constants
    public static final String EDIT_STATE = "edit";

    public static final String BROWSE_STATE = "browse";

    //
    // Fields
    //
    private String state = null;

    //
    // Constructors
    //
    /**
     * Instantiates a StateChangeEvent from the specified state.
     * 
     * @param state
     *                  current state of the application.
     */
    public StateChangeEvent(String state) {
        this.state = state;
    }

    //
    // Methods
    //
    /**
     * Returns true for edit mode .
     * 
     * @return the service name
     */
    public String getState() {
        return state;
    }
}