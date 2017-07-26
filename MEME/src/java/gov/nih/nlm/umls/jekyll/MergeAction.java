/*
 * MergeAction.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularDeleteRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMergeAction;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.umls.jekyll.swing.EditableTableModel;
import gov.nih.nlm.umls.jekyll.swing.ResizableJTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.SocketException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * Merges a given concept into the current concept.
 * 
 * @see AbstractAction
 * @see <a href="src/MergeAction.java.html">source </a>
 */
// TODO: put dialog into AWT thread
public class MergeAction extends AbstractAction {

    // Private Fields
    private Mergeable target = null;

    private GlassComponent glass_comp = null;

    private RelsDialog dialog = null;

    private boolean set_cursor_back = true;

    /**
     * Default constructor.
     */
    public MergeAction(Mergeable comp) {
        putValue(Action.NAME, "Merge into CC");
        putValue(Action.SHORT_DESCRIPTION,
                "merge specified concept into current concept");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
        putValue("Background", Color.lightGray);

        target = comp;
    }

    public void actionPerformed(ActionEvent e) {

        JekyllKit.disableFrames();

        set_cursor_back = true;

        ((Component) target).setCursor(Cursor
                .getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (target instanceof RootPaneContainer) {
            glass_comp = (GlassComponent) ((JFrame) target).getGlassPane();
            glass_comp.setVisible(true);
        }

        Thread t = new Thread(new Runnable() {
            public void run() {
                final ActionLogger logger = new ActionLogger(MergeAction.this.getClass()
                        .getName(), true);

                Concept source_concept = null;

                try {
                    source_concept = target.getSourceConcept();
                    if (source_concept == null) {
                        MEMEToolkit.logComment("Source concept is null");
                        return;
                    }

                    Concept target_concept = target.getTargetConcept();
                    if (target_concept == null) {
                        MEMEToolkit.logComment("Target concept is null");
                        return;
                    }

                    // ----------------
                    // Populate concepts with
                    // relationship information.
                    // ----------------
                    JekyllKit.getCoreDataClient().populateRelationships(
                            source_concept);
                    JekyllKit.getCoreDataClient().populateRelationships(
                            target_concept);

                    // ----------------
                    // Get conflicting relationships
                    // ----------------
                    MolecularMergeAction mma = new MolecularMergeAction(
                            source_concept, target_concept);
                    Relationship[] rels = mma.getConflictingRelationships(
                            source_concept, target_concept);

                    // ----------------
                    // Identify sets of conflicting rels
                    // ----------------
                    Vector sets = new Vector();
                    Vector v = null;
                    Vector identifiers = new Vector();

                    for (int i = 0; i < rels.length; i++) {
                        if (!identifiers.contains(rels[i].getRelatedConcept()
                                .getIdentifier())) {
                            identifiers.add(rels[i].getRelatedConcept()
                                    .getIdentifier());
                            v = new Vector();
                            v.add(rels[i]);
                            sets.add(v);
                        } else {
                            int index = identifiers.indexOf(rels[i]
                                    .getRelatedConcept().getIdentifier());
                            Vector m = (Vector) sets.get(index);
                            m.add(rels[i]);
                            sets.setElementAt(m, index);
                        }
                    }

                    // ----------------
                    // Let the user select an appropriate relationship
                    // and collect relationships to be deleted.
                    // ----------------
                    Vector rels_to_delete = new Vector();

                    for (int i = 0; i < sets.size(); i++) {
                        final Vector conf_rels = (Vector) sets.get(i);
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                dialog = new RelsDialog(((Frame) target),
                                        conf_rels);
                                dialog.setVisible(true);
                            }
                        });
                        if (dialog.cancelPressed()) {
                            dialog.dispose();
                            dialog = null;
                            return;
                        } else {
                            Relationship[] d_rels = dialog.RelsToDelete();
                            for (int k = 0; k < d_rels.length; k++) {
                                if (!rels_to_delete.contains(d_rels[k])) {
                                    rels_to_delete.add(d_rels[k]);
                                }
                            } // k loop
                            dialog.dispose();
                            dialog = null;
                        }
                    } // i loop

                    // ----------------
                    // Delete all other rels
                    // ----------------
                    for (int i = 0; i < rels_to_delete.size(); i++) {
                        Relationship rel = (Relationship) rels_to_delete.get(i);

                        if (rel.getLevel() == CoreData.FV_SOURCE_ASSERTED) {
                            continue;
                        }

                        MolecularDeleteRelationshipAction mdra = new MolecularDeleteRelationshipAction(
                                rel);
                        JekyllKit.getDefaultActionClient().processAction(mdra);

                        Concept concept = rel.getConcept();
                        concept.setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                    }

                    //          if (JekyllKit.getEditorLevel() != 3) {
                    // Acquire the default integrity vector
                    EnforcableIntegrityVector action_vector = JekyllKit
                            .getAuxDataClient().getApplicationVector("DEFAULT");

                    // Acquire the override vector for this editor
                    int editor_level = JekyllKit.getEditorLevel();
                    IntegrityVector override_vector = JekyllKit
                            .getAuxDataClient().getOverrideVector(editor_level);

                    // Apply the override vector to the action vector
                    action_vector.applyOverrideVector(override_vector);

                    mma.setIntegrityVector(action_vector);
                    //          }

                    // ----------------
                    // Perform merge
                    // ----------------
                    JekyllKit.getDefaultActionClient().processAction(mma);
                    // variable to ignore integrity_warnings. Used to reset the correct state.
                    boolean ignore_integrity_warnings = true;
                    // ----------------
                    // After-merge operations
                    // ----------------
//                  Check for warnings
                    if (mma.getViolationsVector() != null &&
                    		mma.getViolationsVector().getWarnings() != null &&
                    		mma.getViolationsVector().getWarnings().getChecks() != null &&
                    		mma.getViolationsVector().getWarnings().getChecks().length != 0) {
                        IntegrityCheck[] warnings = mma.getViolationsVector()
                                .getWarnings().getChecks();

                        

                        StringBuffer sb = new StringBuffer(500);
                        sb.append("\nAttempt to perform a merge on the current concept\n");
                        sb.append(target_concept.getIdentifier().toString());
                        sb.append("\ncaused the following warning(s):\n");
                        for (int i = 0; i < warnings.length; i++) {
                            sb.append("Check name: " + warnings[i].getName());
                            sb.append("\n\t"
                                    + warnings[i].getShortDescription());
                            sb.append("\n\n");
                        }
                        
                        Object[] options = {"Undo the Merge", "Continue"};
                        if (JOptionPane.showOptionDialog(((Component) target), sb
                                .toString(), "Integrity Failure",JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE,null,options,options[0]) == JOptionPane.YES_OPTION) {
                        	// Undo the action.;
                        	JekyllKit.getDefaultActionClient().processUndo(mma);
                        	ignore_integrity_warnings = false; // user wants to undo the action.
                        	set_cursor_back = false;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    try {
                                        JekyllKit.getConceptSelector()
                                                .updateEditList();
                                    } catch (Exception ex) {
                                        ex.printStackTrace(JekyllKit.getLogWriter());
                                    } finally {
                                        glass_comp.setVisible(false);
                                        ((Component) target)
                                        .setCursor(Cursor
                                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                                        JekyllKit.enableFrames();
                                        if (!logger.isLogged())
                                            logger.logElapsedTime();
                                    }
                                }
                            });
                        }
                    } 
                    if (ignore_integrity_warnings) {
	                    MEMEToolkit.logComment("Concept "
	                            + source_concept.getIdentifier().toString()
	                            + " has been merged into current concept ("
	                            + target_concept.getIdentifier().toString() + ").",
	                            true);
	
	                    set_cursor_back = false;
	                    SwingUtilities.invokeLater(new Runnable() {
	                        public void run() {
	                            try {
	                                target.removeSourceConcept();
	                                JekyllKit.getConceptSelector().refreshConcept();
	                            } catch (Exception ex) {
	                                ex.printStackTrace(JekyllKit.getLogWriter());
	                            } finally {
	                                glass_comp.setVisible(false);
	                                ((Component) target)
	                                        .setCursor(Cursor
	                                                .getPredefinedCursor(Cursor.HAND_CURSOR));
	                                JekyllKit.enableFrames();
	                                logger.logElapsedTime();
	                            }
	                        }
	                    });
                    }
                    
                } catch (StaleDataException sde) {
                    try {
                        MEMEToolkit
                                .notifyUser(
                                        ((Component) target),
                                        "You have attempted to perform a merge action on"
                                                + "\nconcepts that have changed since they were last read."
                                                + "\nPlease re-read both concepts and then attempt"
                                                + "\nthe action again.");
                    } catch (Exception ex) {
                        if (ex instanceof MissingDataException) {
                            MEMEToolkit
                                    .notifyUser(
                                            ((Component) target),
                                            "One of the concepts, involved"
                                                    + "\nin the merge, is no longer"
                                                    + "\na valid concept in the database.");
                        } else {
                            ex.printStackTrace(JekyllKit.getLogWriter());
                        }
                    }
                } catch (Exception ex) {
                    if (ex instanceof IntegrityViolationException) {
                        GUIToolkit.showIntegrityViolationDialog(
                                ((Component) target),
                                (IntegrityViolationException) ex);
                    } else if (ex instanceof MEMEException
                            && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                        MEMEToolkit.reportError(((Component) target),
                                "There was a network error."
                                        + "\nPlease try the action again.",
                                false);
                    } else {
                        MEMEToolkit
                                .notifyUser(
                                        ((Component) target),
                                        "Failed to merge "
                                                + ((source_concept == null) ? " "
                                                        : source_concept
                                                                .getIdentifier()
                                                                .toString())
                                                + " into the current concept."
                                                + "\nConsole/Log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                } finally {
                    if (set_cursor_back) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                if (glass_comp != null) {
                                    glass_comp.setVisible(false);
                                }
                                ((Component) target)
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
    } // actionPerformed()

    private class RelsDialog extends JDialog {
        private Vector rels = null;

        private String[] columnNames = { "Level", "Source", "From",
                "Relationship Name", "Relationship Attribute", "To" };

        private String[][] data = null;

        private ResizableJTable table = null;

        private boolean cancel_pressed = false;

        // Constructor
        public RelsDialog(Frame parent, Vector rels) {
            super(parent, "Resolve relationship conflict", true);
            this.rels = rels;
            initValues();
            initComponents();
            pack();
        }

        private void initValues() {
            data = new String[rels.size()][columnNames.length];
            for (int row = 0; row < data.length; row++) {
                Relationship rel = (Relationship) rels.get(row);
                data[row][0] = String.valueOf(rel.getLevel());
                data[row][1] = rel.getSource().toString();
                data[row][2] = rel.getRelatedConcept().getPreferredAtom()
                        .getString();
                data[row][3] = rel.getName();
                data[row][4] = (rel.getAttribute() == null) ? "" : rel
                        .getAttribute();
                data[row][5] = rel.getConcept().getPreferredAtom().getString();
            }
        }

        private void initComponents() {
            // Build the contents
            Container contents = getContentPane();
            contents.setLayout(new BorderLayout());

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    cancel_pressed = true;
                    RelsDialog.this.hide();
                }
            });

            JLabel label = new JLabel();
            label
                    .setText("There is a conflict. "
                            + "To resolve it choose one of the following relationships:");
            label.setHorizontalAlignment(JLabel.CENTER);
            contents.add(label, BorderLayout.NORTH);

            EditableTableModel model = new EditableTableModel(data, columnNames);
            table = GUIToolkit.getTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            // we need to resize columns properly
            model.fireTableDataChanged();
            JTextField tf = new JTextField();
            tf.setEditable(false);
            table.setDefaultEditor(String.class, new DefaultCellEditor(tf));
            StringTableCellRenderer renderer = new StringTableCellRenderer(
                    table, model);
            table.setDefaultRenderer(String.class, renderer);
            JScrollPane sp = new JScrollPane(table);
            contents.add(sp, BorderLayout.CENTER);

            JPanel button_panel = new JPanel();
            JButton select_button = new JButton();
            select_button.setText("Choose selected relationship");
            select_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (table.getSelectionModel().isSelectionEmpty()) {
                        MEMEToolkit
                                .notifyUser("Please select relationship to keep.");
                        return;
                    }

                    rels
                            .removeElementAt(table.mapIndex(table
                                    .getSelectedRow()));
                    RelsDialog.this.hide();
                }
            });
            button_panel.add(select_button);

            JButton cancel_button = new JButton();
            cancel_button.setText("Cancel Merge");
            cancel_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancel_pressed = true;
                    RelsDialog.this.hide();
                }
            });
            button_panel.add(cancel_button);

            contents.add(button_panel, BorderLayout.SOUTH);
        } // initComponents()

        public boolean cancelPressed() {
            return cancel_pressed;
        }

        public Relationship[] RelsToDelete() {
            return (Relationship[]) rels.toArray(new Relationship[0]);
        }
    } // RelsDialog class

} // MergeAction
