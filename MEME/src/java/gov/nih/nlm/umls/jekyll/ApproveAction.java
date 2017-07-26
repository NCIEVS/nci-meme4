/*
 * ApproveAction.java
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
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * Approves current concept.
 * 
 * @see <a href="src/ApproveAction.java.html">source </a>
 * @see AbstractAction
 * @author Vladimir Olenichev
 */
public class ApproveAction extends AbstractAction {

    private Component target = null;

    private GlassComponent glass_comp = null;

    public ApproveAction(Component comp) {
        putValue(Action.NAME, "Approve");
        putValue(Action.SHORT_DESCRIPTION, "approve current concept");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue("Background", Color.cyan);

        target = comp;
        if (target instanceof RootPaneContainer) {
            glass_comp = (GlassComponent) ((RootPaneContainer) target)
                    .getGlassPane();
        }
    }

    public void actionPerformed(ActionEvent e) {

        JekyllKit.disableFrames();
        target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (glass_comp != null) {
            glass_comp.setVisible(true);
        }

        Thread t = new Thread(new Runnable() {
            public void run() {
                ActionLogger logger = new ActionLogger(ApproveAction.this.getClass().getName(), true);

                Concept source_concept = null;

                try {
                    source_concept = JekyllKit.getConceptSelector()
                            .getConcept();

                    if (source_concept == null) {
                        return;
                    }

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

                            if (stale_data) {
                                stale_data = false;
                            }
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
                    JekyllKit.getConceptSelector().refreshConcept();

                } catch (Exception ex) {
                    if (ex instanceof IntegrityViolationException) {
                        GUIToolkit.showIntegrityViolationDialog(target,
                                (IntegrityViolationException) ex);
                    } else if (ex instanceof MissingDataException) {
                        MEMEToolkit.notifyUser(target, "Concept "
                                + source_concept.getIdentifier().toString()
                                + " is not"
                                + "\na valid concept in the database.");
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
                                        "Failed to approve concept: "
                                                + ((source_concept == null) ? " "
                                                        : source_concept
                                                                .getIdentifier()
                                                                .toString())
                                                + "\nConsole/Log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (glass_comp != null) {
                                glass_comp.setVisible(false);
                            }
                            target.setCursor(Cursor
                                    .getPredefinedCursor(Cursor.HAND_CURSOR));
                            JekyllKit.enableFrames();
                        }
                    });
                    
                    logger.logElapsedTime();
                }
            }
        });

        t.start();

    } // actionPerformed()

} // ApproveAction
