/*
 * ConceptIdFrame.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.SocketException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import samples.accessory.StringGridBagLayout;

/**
 * This window is used to search for a concept by concept_id.
 * 
 * @see <a href="src/ConceptIdFrame.java.html">source </a>
 */
public class ConceptIdFrame extends JFrame implements JekyllConstants,
        Transferable {

    // Various components
    private GlassComponent glass_comp = null;

    private JTextField conceptIdTF = null;

    private JTextField conceptNameTF = null;

    // Private fields
    private Concept concept = null;

    /**
     * Default constructor.
     */
    public ConceptIdFrame() {
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    private void initComponents() {
        Box b = null;

        setTitle("Find concept by concept id");
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        // set properties on this frame
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        JLabel conceptIdLabel = new JLabel();
        conceptIdLabel.setText("Concept Id:");

        // concept_id text field
        conceptIdTF = new JTextField();
        conceptIdTF.setMargin(new Insets(1, 1, 1, 1));
        conceptIdTF.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                String string = conceptIdTF.getText().trim();

                if (string.equals("")) {
                    return;
                }

                try {
                    int concept_id = Integer.parseInt(string);

                    concept = JekyllKit.getCoreDataClient().getConcept(
                            concept_id);
                    conceptNameTF.setText(concept.getPreferredAtom()
                            .getString());
                } catch (NumberFormatException ex) {
                    clearContent();
                    MEMEToolkit.notifyUser(ConceptIdFrame.this,
                            "This is not a valid concept id: " + string);
                } catch (MissingDataException ex) {
                    clearContent();
                    MEMEToolkit.notifyUser(ConceptIdFrame.this,
                            "Concept was not found: " + string);
                } catch (Exception ex) {
                    if (ex instanceof MEMEException
                            && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                        MEMEToolkit.reportError(ConceptIdFrame.this,
                                "There was a network error."
                                        + "\nPlease try the action again.",
                                false);
                    } else {
                        MEMEToolkit
                                .notifyUser(
                                        ConceptIdFrame.this,
                                        "Failed to resolve this concept: "
                                                + string
                                                + "\nLog file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }
            }
        });

        conceptIdTF.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    conceptIdTF.transferFocus();
                }
            }
        });

        // box container
        b = Box.createHorizontalBox();
        b.add(conceptIdLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptIdTF);

        contents
                .add(
                        "gridx=0,gridy=0,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        // concept name text field
        conceptNameTF = GUIToolkit.getNonEditField();

        contents
                .add(
                        "gridx=0,gridy=1,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        conceptNameTF);

        // "Close" button
        JButton closeButton = GUIToolkit.getButton(new CloseAction(this));

        // "Edit" button
        JButton editButton = GUIToolkit.getButton(new TransferAction(this));
        editButton.setText("Edit");

        // "Concept" button
        JButton conceptButton = GUIToolkit.getButton(new ConceptFrameAction(
                this));

        // box container
        b = Box.createHorizontalBox();
        b.add(closeButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(editButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptButton);

        contents.add(
                "gridx=0,gridy=2,fill=NONE,anchor=CENTER,insets=[12,12,12,11]",
                b);

        setLocationRelativeTo(JekyllKit.getMainFrame());

    } // initComponents()

    private void clearContent() {
        conceptIdTF.setText(null);
        conceptNameTF.setText(null);
        concept = null;
    }

    // -------------------------------
    // Interface implementation
    // -------------------------------

    /**
     * Implements
     * {@link Transferable#getConceptIds() Transferable.getConceptIds()}.
     */
    public Concept[] getConcepts() {
        if (concept == null) {
            return new Concept[0];
        }

        return new Concept[] { concept };
    }

    /**
     * Implements
     * {@link Transferable#getConceptIds() Transferable.getConceptIds()}.
     */
    public int[] getConceptIds() {
        if (concept == null) {
            return new int[0];
        }

        return new int[] { concept.getIdentifier().intValue() };
    }

    // -----------------------------------
    // Inner Classes
    // -----------------------------------
    /**
     * Invokes "Concept" window with the content for the specified concept.
     * 
     * @see AbstractAction
     */
    class ConceptFrameAction extends AbstractAction {
        private Component target = null;

        // Constructor
        public ConceptFrameAction(Component comp) {
            putValue(Action.NAME, "Concept");
            putValue(Action.SHORT_DESCRIPTION,
                    "display Concept screen for the current concept");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (concept == null) {
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            ConceptFrame frame = JekyllKit.getConceptFrame();
            frame.setContent(concept);

            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        }
    } // ConceptFrameAction

    /**
     * Shows or hides this component and clears the content.
     * 
     * @param b
     *                  if <code>true</code>, shows this component; otherwise,
     *                  hides it.
     */
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            clearContent();
        }
    }
}