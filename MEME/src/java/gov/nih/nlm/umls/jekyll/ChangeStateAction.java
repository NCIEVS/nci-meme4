/*
 * ChangeStateAction.java
 */

package gov.nih.nlm.umls.jekyll;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.EventListenerList;

/**
 * @see <a href="src/ChangeStateAction.java.html">source </a>
 */
public class ChangeStateAction extends AbstractAction {

    //
    // Private Fields
    //
    private String current_state = null;

    private EventListenerList listener_list = null;

    private StateChangeEvent sc_event = null;

    /**
     * @param parent
     *                  the parent
     * @param state
     *                  current state
     */
    public ChangeStateAction(Component parent, String state) {
        putValue(Action.NAME, "Browse");
        putValue(Action.SHORT_DESCRIPTION,
                "toggle state of the interface between edit or browse");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));

        //target = parent;
        current_state = state;
        listener_list = new EventListenerList();
    }

    public void actionPerformed(ActionEvent e) {

        Thread t = new Thread(new Runnable() {
            public void run() {
                fireStateChanged(current_state);
            }
        });

        t.start();
    }

    /**
     * Add a listener.
     * 
     * @param l
     *                  the {@link StateChangeListener}to add
     */
    public void addStateChangeListener(StateChangeListener l) {
        listener_list.add(StateChangeListener.class, l);
    }

    /**
     * Remove a listener.
     * 
     * @param l
     *                  the {@link StateChangeListener}to remove
     */
    public void removeStateChangeListener(StateChangeListener l) {
        listener_list.remove(StateChangeListener.class, l);
    }

    /**
     * Inform listeners that the state has changed via a
     * {@link StateChangeEvent}.
     * 
     * @param state
     *                  the current state of the interface
     */
    protected void fireStateChanged(String state) {
        Object[] listeners = listener_list.getListenerList();
        sc_event = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == StateChangeListener.class) {
                // Lazily create the event:
                if (sc_event == null)
                    sc_event = new StateChangeEvent(state);
                ((StateChangeListener) listeners[i + 1]).stateChanged(sc_event);
            }
        }
    }

}