/*
 * EditAction.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.swing.GlassComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

/**
 * Replaces the Edit List in the SEL screen with the given concept(s).
 * 
 * @see AbstractAction
 * @see <a href="src/EditAction.java.html">source </a>
 */
public class EditAction extends AbstractAction {

    private Transferable target = null;

    private GlassComponent glass_comp = null;

    private ConceptSelector frame = null;

    /**
     * Default constructor.
     */
    public EditAction(Transferable comp) {
        putValue(Action.NAME, "Edit");
        putValue(Action.SHORT_DESCRIPTION,
                "replaces the Edit List in the SEL screen with given concept(s)");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E,
                Event.CTRL_MASK));
        putValue("Background", Color.cyan);

        target = comp;
    }

    // THINK ABOUT: to use Concept objects
    // instead of concept_ids
    public void actionPerformed(ActionEvent e) {

        try {
            JekyllKit.disableFrames();

            ((Component) target).setCursor(Cursor
                    .getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (target instanceof RootPaneContainer) {
                glass_comp = (GlassComponent) ((RootPaneContainer) target)
                        .getGlassPane();
                glass_comp.setVisible(true);
            }

            // using concept_ids for now
            // since we need to validate
            // concepts.
            int[] concepts = target.getConceptIds();

            if (concepts.length == 0) {
                return;
            }

            frame = JekyllKit.getConceptSelector();

            frame.replaceWith(concepts);
            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            if (glass_comp != null) {
                glass_comp.setVisible(false);
            }
            ((Component) target).setCursor(Cursor
                    .getPredefinedCursor(Cursor.HAND_CURSOR));
            JekyllKit.enableFrames();
            if (frame != null) {
                frame.toFront();
            }
        }

    } // actionPerformed()

}