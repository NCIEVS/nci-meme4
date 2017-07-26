/*
 * NotApproveNextAction.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
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
import javax.swing.SwingUtilities;

/**
 * Do not approve a concept and move selection to the next item in the
 * checklist/worklist.
 * 
 * @see AbstractAction
 * @see <a href="src/NotApproveNextAction.java.html">source </a>
 */
public class NotApproveNextAction extends AbstractAction {
    private Component target = null;

    private GlassComponent glass_comp = null;

    private boolean set_cursor_back = true;

    public NotApproveNextAction(Component comp) {
        putValue(Action.NAME, "Not Approve/Next");
        putValue(Action.SHORT_DESCRIPTION,
                "do not approve a concept and move selection to the next item in the list");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
        // light brown color
        putValue("Background", new Color(204, 153, 051));

        target = comp;
    }

    // The logic is if this action is called upon from
    // WorkFilesFrame, it doesn't refresh other windows
    // with the content of the next concept in the list.
    public void actionPerformed(ActionEvent e) {
        if (!JekyllKit.getWorkFilesFrame().isVisible()) {
            return;
        }

        set_cursor_back = true;

        JekyllKit.disableFrames();
        target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        glass_comp = (GlassComponent) ((JFrame) target).getGlassPane();
        glass_comp.setVisible(true);

        Thread t = new Thread(new Runnable() {
            public void run() {
                final ActionLogger logger = new ActionLogger(NotApproveNextAction.this.getClass()
                        .getName(), true);
                
                try {
                    if (target instanceof WorkFilesFrame) {
                        JekyllKit.getWorkFilesFrame().nextConcept(null);
                    } else {
                        Concept concept = JekyllKit.getConceptSelector()
                                .getConcept();
                        JekyllKit.getWorkFilesFrame().nextConcept(concept);
                    }

                    set_cursor_back = false;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                if (JekyllKit.getConceptSelector().isVisible()) {
                                    if (JekyllKit.getWorkFilesFrame()
                                            .getConcept() == null) {
                                        return;
                                    }

                                    ConceptSelector concept_selector = JekyllKit
                                            .getConceptSelector();
                                    Concept next_concept = JekyllKit
                                            .getWorkFilesFrame().getConcept();
                                    if (next_concept != null) {
                                        concept_selector
                                                .replaceWith(new Concept[] { next_concept });
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace(JekyllKit.getLogWriter());
                            } finally {
                                if (glass_comp != null) {
                                    glass_comp.setVisible(false);
                                }
                                target
                                        .setCursor(Cursor
                                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                                JekyllKit.enableFrames();
                                logger.logElapsedTime();
                            }
                        }
                    });
                } catch (Exception ex) {
                    MEMEToolkit
                            .notifyUser(
                                    target,
                                    "Failed to move to the next item."
                                            + "\nConsole/Log file may contain more information.");
                    ex.printStackTrace(JekyllKit.getLogWriter());
                } finally {
                    if (set_cursor_back) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                if (glass_comp != null) {
                                    glass_comp.setVisible(false);
                                }
                                target
                                        .setCursor(Cursor
                                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                                JekyllKit.enableFrames();
                            }
                        });
                        logger.logElapsedTime();
                    }
                }
            }
        });

        t.start();
    }
}