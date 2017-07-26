/*
 * CloseAction.java
 */

package gov.nih.nlm.umls.jekyll;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * Close action.
 * 
 * @see <a href="src/CloseAction.java.html">source </a>
 * @see AbstractAction
 */
public class CloseAction extends AbstractAction {

    //
    // Private Fields
    //
    private Component target = null;

    // Constructor
    public CloseAction(Component comp) {
        putValue(Action.NAME, "Close");
        putValue(Action.SHORT_DESCRIPTION, "close window");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Event.CTRL_MASK));
        putValue("Background", Color.red);

        target = comp;
    }

    public void actionPerformed(ActionEvent e) {
        if (target instanceof Dialog) {
            ((Dialog) target).setVisible(false);
            ((Dialog) target).dispose();
        } else if (target instanceof Frame) {
            target.setVisible(false);
        }
    }
}