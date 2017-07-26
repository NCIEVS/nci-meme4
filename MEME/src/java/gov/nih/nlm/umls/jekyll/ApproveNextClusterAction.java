/*
 * ApproveNextClusterAction.java 
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.swing.GlassComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.SocketException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * This action approves a concept and moves selection to the next item in the
 * checklist or worklist.
 * 
 * @see <a href="src/ApproveNextAction.java.html">source </a>
 * @see AbstractAction
 */
public class ApproveNextClusterAction extends AbstractAction {
    private Component target = null;

    private GlassComponent glass_comp = null;

    private boolean set_cursor_back = true;

    public ApproveNextClusterAction(Component comp) {
        putValue(Action.NAME, "Approve/Next Cluster");
        putValue(Action.SHORT_DESCRIPTION,
                "approve the Current concept and move selection to the next cluster in the worklist frame");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        putValue("Background", Color.cyan);

        target = comp;
    }

    public void actionPerformed(ActionEvent e) {
        if (!JekyllKit.getWorkFilesFrame().isVisible())
            return;

        set_cursor_back = true;

        JekyllKit.disableFrames();
        target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        glass_comp = (GlassComponent) ((JFrame) target).getGlassPane();
        glass_comp.setVisible(true);

        Thread t = new Thread(new Runnable() {
            public void run() {
                final ActionLogger logger = new ActionLogger(ApproveNextClusterAction.this.getClass()
                        .getName(), true);

                Concept source_concept = null;

                try {
                    if (target instanceof WorkFilesFrame) {
                        source_concept = JekyllKit.getWorkFilesFrame()
                                .getConcept();
                    } else {
                        source_concept = JekyllKit.getConceptSelector()
                                .getConcept();
                    }

                    if (source_concept == null)
                        return;

                    boolean stale_data = false;
                    do {
                        try {
                            // Checking if there are "Concept set to N by..."
                            // concept notes
                            // present, if so, delete them.
                            Attribute[] attrs = source_concept
                                    .getAttributesByName(Attribute.CONCEPT_NOTE);
                            for (int i = 0; i < attrs.length; i++) {
                                if (attrs[i].getValue().startsWith(
                                        "Concept set to N by")) {
                                    MolecularDeleteAttributeAction mdaa = new MolecularDeleteAttributeAction(
                                            attrs[i]);
                                    JekyllKit.getDefaultActionClient()
                                            .processAction(mdaa);
                                }
                            }

                            MolecularApproveConceptAction maca = new MolecularApproveConceptAction(
                                    source_concept);
                            maca.setSource(source_concept);
                            JekyllKit.getApprovalActionClient().processAction(
                                    maca);

                            if (stale_data)
                                stale_data = false;

                        } catch (StaleDataException sde) {
                            // re-reading concept
                            source_concept = JekyllKit.getCoreDataClient()
                                    .getConcept(source_concept);
                            stale_data = true;
                        }
                    } while (stale_data); // do loop

                    MEMEToolkit.logComment("Concept "
                            + source_concept.getIdentifier().toString() + " ("
                            + source_concept.getPreferredAtom().getString()
                            + ") has been successfully approved.", true);

                    JekyllKit.getRelationshipsFrame()
                            .clearEditedRelationships();

                    JekyllKit.getWorkFilesFrame()
                            .refreshConcept(source_concept);
                    Concept[] concepts = JekyllKit.getWorkFilesFrame().nextClusterConcepts(source_concept);

                    set_cursor_back = false;
                    try {
                       if (JekyllKit.getConceptSelector().isVisible() && concepts != null) {
                    	   //02.07.08 PM - Bug fix for concepts not getting refreshed
                    	   // Replaced this call:
                           //            JekyllKit.getConceptSelector().replaceWith(concepts);
                    	   // with:
                    	   int[] concept_ids = new int[concepts.length];
                    	   for(int i=0 ; i<concepts.length; i++) {
                    		   concept_ids[i] = concepts[i].getIdentifier().intValue();                    		   
                    	   }
                           JekyllKit.getConceptSelector().replaceWith(concept_ids); 

                       } else {
                           JekyllKit.getConceptSelector().refreshConcept();
                        }
                     } catch (Exception ex) {
                                ex.printStackTrace(JekyllKit.getLogWriter());
                     } finally {
                         if (glass_comp != null)
                             glass_comp.setVisible(false);
                             target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                             JekyllKit.enableFrames();
                             logger.logElapsedTime();
                            }
                } catch (Exception ex) {
                    if (ex instanceof IntegrityViolationException) {
                        GUIToolkit.showIntegrityViolationDialog(target,
                                (IntegrityViolationException) ex);
                    } else if (ex instanceof MissingDataException) {
                        MEMEToolkit.notifyUser(target,
                                "Concept was not found: "
                                        + source_concept.getIdentifier()
                                                .toString());
                    } else if (ex instanceof MEMEException
                            && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                        MEMEToolkit.reportError(target,
                                "There was a network error."
                                        + "\nPlease try the action again.",
                                false);
                    } else {
                        MEMEToolkit
                                .notifyUser(
                                        target,
                                        "Failed to approve a concept: "
                                                + ((source_concept == null) ? " "
                                                        : source_concept
                                                                .getIdentifier()
                                                                .toString())
                                                + "\nConsole/Log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                } finally {
                    if (set_cursor_back) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                if (glass_comp != null)
                                    glass_comp.setVisible(false);
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

} // ApproveNextAction

