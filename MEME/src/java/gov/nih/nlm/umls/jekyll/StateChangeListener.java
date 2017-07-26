/*
 * StateChangeListener.java
 */

package gov.nih.nlm.umls.jekyll;

// import gov.nih.nlm.swing.GlassPaneListener;
import java.util.EventListener;

/**
 * Listens for changes in the state of the interface.
 */
public interface StateChangeListener extends EventListener {

    /**
     * Indicates that the data source has changed.
     * 
     * @param sce
     *                  the event
     */
    public void stateChanged(StateChangeEvent sce);
}