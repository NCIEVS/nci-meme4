/*
 * FinderAction.java
 */

package gov.nih.nlm.umls.jekyll;

import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * Invokes Finder window.
 * 
 * @see AbstractAction
 * @see <a href="src/FinderAction.java.html">source </a>
 */
public class FinderAction extends AbstractAction {

    // Constructor
    public FinderAction(Component comp) {
        putValue(Action.NAME, "Finder");
        putValue(Action.SHORT_DESCRIPTION, "opens Finder window");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F,
                Event.CTRL_MASK));
        putValue("Background", Color.green);

    }

    public void actionPerformed(ActionEvent e) {
        Finder frame = JekyllKit.getFinder();

        if (frame.isShowing()) {
            frame.setExtendedState(JFrame.NORMAL);
            frame.toFront();
        } else {
            frame.setVisible(true);
        }
    }

}