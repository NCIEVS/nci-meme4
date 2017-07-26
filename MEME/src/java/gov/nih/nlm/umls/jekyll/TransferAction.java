/*
 * TransferAction.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.swing.GlassComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;

/**
 * Adds specified concept(s) to the SEL screen.
 * 
 * @see AbstractAction
 * @see <a href="src/TransferAction.java.html">source </a>
 */
// Accessed from:
//  - ConceptIdFrame ("Edit" button)
//  - RelationshipsFrame ("Transfer" button)
//  - ResultsDialog
public class TransferAction extends AbstractAction {
    private Transferable target = null;

    private GlassComponent glass_comp = null;

    private ConceptSelector frame = null;

    /**
     * Default constructor.
     */
    public TransferAction(Transferable comp) {
        putValue(Action.NAME, "Transfer to SEL");
        putValue(Action.SHORT_DESCRIPTION,
                "add concept(s) to the Edit List in the SEL screen");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        putValue("Background", Color.cyan);

        target = comp;
    }

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

            Concept[] concepts = new Concept[0];
            int[] concept_ids = new int[0];

            if (target instanceof ConceptIdFrame) {
                concepts = target.getConcepts();
            } else {
                // for everything else we use concept_ids
                concept_ids = target.getConceptIds();
            }

            if (concepts.length == 0 && concept_ids.length == 0) {
                return;
            }

            frame = JekyllKit.getConceptSelector();

            if (target instanceof ConceptIdFrame) {
                frame.addConcepts(concepts);
            } else {
                frame.addConcepts(concept_ids);
            }

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
            frame.toFront();
        }

    } // actionPerformed()

}