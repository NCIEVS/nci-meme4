/**
 * PopupListener.java
 */

package gov.nih.nlm.umls.jekyll;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 * @see <a href="src/PopupListener.java.html">source </a>
 */
public class PopupListener extends MouseAdapter {

    private JPopupMenu popup = null;

    public PopupListener(JPopupMenu popup) {
        super();
        this.popup = popup;
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}