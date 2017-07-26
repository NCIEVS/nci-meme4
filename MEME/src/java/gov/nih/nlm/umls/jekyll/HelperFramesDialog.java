/**
 * HelperFramesDialog.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.swing.FontSizeManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import samples.accessory.StringGridBagLayout;

/**
 * Main window for Helper Frames.
 * 
 * @see <a href="src/HelperFramesDialog.java.html">source </a>
 */
public class HelperFramesDialog extends JDialog {

    // Private Fields
    private Frame parent_frame = null;

    private JList list = null;

    String[] data = { "Insert Atoms", "Change Atom Releasability",
            "Change Suppressibility", "Undo/Redo Actions" };

    // 		     "Edit SOS Attributes"};

    /**
     * Creates non-modal dialog.
     */
    public HelperFramesDialog(Frame parent) {
        super(parent);
        parent_frame = parent;
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    private void initComponents() {
        setTitle("Helper Frames");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());

        list = new JList(data);
        list.setBorder(LineBorder.createBlackLineBorder());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new MyCellRenderer());
        list.setSelectedIndex(0);

        contents
                .add(
                        "gridx=0,gridy=0,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,0,11]",
                        list);

        JButton selectButton = GUIToolkit.getButton(new SelectAction(this));

        JButton closeButton = GUIToolkit.getButton(new CloseAction(this));

        // box container
        Box b = Box.createHorizontalBox();
        b.add(selectButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(closeButton);

        contents.add(
                "gridx=0,gridy=1,fill=NONE,anchor=CENTER,insets=[12,12,12,11]",
                b);

        setLocationRelativeTo(parent_frame);

        setResizable(false);

    } // initComponents

    /**
     * @see AbstractAction
     */
    class SelectAction extends AbstractAction {
        private Component target = null;

        // constructor
        public SelectAction(Component comp) {
            putValue(Action.NAME, "Select");
            putValue(Action.SHORT_DESCRIPTION, "");
            putValue("Background", Color.lightGray);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if ((JekyllKit.getEditorLevel() == 3)
                    && (!list.getSelectedValue().equals("Insert Atoms"))) {
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Concept current_concept = JekyllKit.getConceptSelector()
                    .getConcept();

            if (list.getSelectedValue().equals("Insert Atoms")) {
                InsertAtomFrame frame = JekyllKit.getInsertAtomFrame();

                frame.setContent(current_concept);
                if (frame.getExtendedState() == JFrame.ICONIFIED) {
                    frame.setExtendedState(JFrame.NORMAL);
                }
                frame.setVisible(true);
            } else if (list.getSelectedValue().equals(
                    "Change Atom Releasability")) {
                ChangeTBRFrame frame = JekyllKit.getChangeTBRFrame();

                frame.setContent(current_concept);
                if (frame.getExtendedState() == JFrame.ICONIFIED) {
                    frame.setExtendedState(JFrame.NORMAL);
                }
                frame.setVisible(true);
            } else if (list.getSelectedValue().equals("Change Suppressibility")) {
                ChangeSuppFrame frame = JekyllKit.getChangeSuppFrame();

                frame.setContent(current_concept);
                if (frame.getExtendedState() == JFrame.ICONIFIED) {
                    frame.setExtendedState(JFrame.NORMAL);
                }
                frame.setVisible(true);
            } else if (list.getSelectedValue().equals("Undo/Redo Actions")) {
                UndoRedoFrame frame = JekyllKit.getUndoRedoFrame();

                frame.setContent(current_concept);
                if (frame.getExtendedState() == JFrame.ICONIFIED) {
                    frame.setExtendedState(JFrame.NORMAL);
                }
                frame.setVisible(true);
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            target.setVisible(false);
            if (target instanceof Dialog) {
                ((Dialog) target).dispose();
            }
        }
    } // SelectAction

    class MyCellRenderer extends JLabel implements ListCellRenderer {
        public MyCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            setBackground(isSelected ? list.getSelectionBackground() : list
                    .getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list
                    .getForeground());
            if ((JekyllKit.getEditorLevel() == 3)
                    && (!value.toString().equals("Insert Atoms"))) {
                setEnabled(false);
            } else {
                setEnabled(true);
            }

            return this;
        }
    }
}