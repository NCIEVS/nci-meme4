/*
 * InsertAtomFrame.java
 */
package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.swing.DecreaseFontAction;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.IncreaseFontAction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.net.SocketException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import samples.accessory.StringGridBagLayout;

/**
 * This window is a part of Helper Frames. Here, user can insert an atom into
 * the current concept.
 * 
 * @see <a href="src/InsertAtomFrame.java.html">source </a>
 * @see <a href="src/InsertAtomFrame.properties.html">properties </a>
 */
public class InsertAtomFrame extends JFrame implements JekyllConstants,
        Refreshable {
    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    //
    // Components
    //
    private GlassComponent glass_comp = null;

    private JTextField conceptIdTF = null;

    private JTextArea atom_name = null;

    private JTextField codeTF = null;

    private JList tgList = null;

    private JComboBox tbrComboBox = null;

    private DefaultComboBoxModel tbrComboBoxModel = null;

    private JComboBox statusComboBox = null;

    private DefaultComboBoxModel statusComboBoxModel = null;

    private JComboBox generatedComboBox = null;

    private JComboBox releasedComboBox = null;

    private DefaultComboBoxModel releasedComboBoxModel = null;

    private JComboBox suppComboBox = null;

    private Concept current_concept = null;

    private Termgroup[] termgroups = null;

    private char[] tbr_values = null;

    private char[] status_values = null;

    private String[] generated_values = new String[] { "Y", "N" };

    private char[] released_values = null;

    private String[] supp_values = new String[] { "Y", "N" };

    private static final Termgroup DEFAULT_TERMGROUP = new Termgroup.Default(
            "NCIMTH/PN");

    private static final char DEFAULT_GENERATED = 'Y';

    private static final char DEFAULT_SUPPRESSIBLE = 'N';

    // Actions
    CloseAction close_action = new CloseAction(this);

    /**
     * Default constructor.
     */
    public InsertAtomFrame() {
        initResources();
        initComponents();
        initValues();
        FontSizeManager.addContainer(this);
        FontSizeManager.adjustFontSize(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle
                .getBundle("bundles.InsertAtomFrameResources");
    }

    private void initComponents() {
        Box b = null;
        JScrollPane sp = null;
        setTitle(resources.getString("window.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);
        JLabel conceptIdLabel = new JLabel();
        conceptIdLabel.setText(resources.getString("conceptId.label"));
        conceptIdTF = new JTextField(10);
        conceptIdTF.setEditable(false);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        b = Box.createHorizontalBox();
        b.add(conceptIdLabel);
        b.add(Box.createHorizontalStrut(12));
        b.add(conceptIdTF);
        contents
                .add(
                        "gridx=0,gridy=0,gridwidth=2,fill=NONE,anchor=WEST,insets=[12,12,0,11]",
                        b);

        JLabel atomNameLabel = new JLabel();
        atomNameLabel.setText(resources.getString("atomName.label"));
        atom_name = new JTextArea(3, 0);
        atom_name.setLineWrap(true);
        atom_name.setMinimumSize(atom_name.getPreferredSize());
        sp = new JScrollPane(atom_name);
        b = Box.createHorizontalBox();
        b.add(atomNameLabel);
        b.add(Box.createHorizontalStrut(12));
        b.add(sp);
        contents
                .add(
                        "gridx=0,gridy=1,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        JLabel codeLabel = new JLabel();
        codeLabel.setText(resources.getString("code.label"));
        codeTF = new JTextField(10);
        codeTF.setMinimumSize(codeTF.getPreferredSize());
        b = Box.createHorizontalBox();
        b.add(codeLabel);
        b.add(Box.createHorizontalStrut(12));
        b.add(codeTF);
        contents
                .add(
                        "gridx=0,gridy=2,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        //
        // Label for termgroups list
        //
        JLabel tgLabel = new JLabel();
        tgLabel.setText(resources.getString("termgroup.label"));
        contents.add(
                "gridx=0,gridy=3,fill=NONE,anchor=WEST,insets=[12,12,0,11]",
                tgLabel);

        //
        // Termgroups list
        //
        tgList = new JList();
        tgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sp = new JScrollPane(tgList);
        contents
                .add(
                        "gridx=0,gridy=4,fill=BOTH,anchor=WEST,insets=[12,12,0,11]",
                        sp);

        JLabel tbrLabel = new JLabel();
        tbrLabel.setText(resources.getString("tbr.label"));
        tbrComboBoxModel = new DefaultComboBoxModel();
        tbrComboBox = new JComboBox(tbrComboBoxModel);
        JLabel statusLabel = new JLabel();
        statusLabel.setText(resources.getString("status.label"));
        statusComboBoxModel = new DefaultComboBoxModel();
        statusComboBox = new JComboBox(statusComboBoxModel);
        JLabel generatedLabel = new JLabel();
        generatedLabel.setText(resources.getString("generated.label"));
        generatedComboBox = new JComboBox(generated_values);
        JLabel releasedLabel = new JLabel();
        releasedLabel.setText(resources.getString("released.label"));
        releasedComboBoxModel = new DefaultComboBoxModel();
        releasedComboBox = new JComboBox(releasedComboBoxModel);
        JLabel suppLabel = new JLabel();
        suppLabel.setText(resources.getString("suppressible.label"));
        suppComboBox = new JComboBox(supp_values);
        b = Box.createHorizontalBox();
        b.add(tbrLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(tbrComboBox);
        b.add(Box.createHorizontalStrut(12));
        b.add(statusLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(statusComboBox);
        b.add(Box.createHorizontalStrut(12));
        b.add(generatedLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(generatedComboBox);
        b.add(Box.createHorizontalStrut(12));
        b.add(releasedLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(releasedComboBox);
        b.add(Box.createHorizontalStrut(12));
        b.add(suppLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(suppComboBox);
        contents
                .add(
                        "gridx=0,gridy=5,gridwidth=2,fill=NONE,anchor=CENTER,insets=[12,12,0,11]",
                        b);

        Action a = new InsertAction(this);
        JButton insertButton = new JButton(a);
        insertButton.setFont(BUTTON_FONT);
        JButton closeButton = new JButton(close_action);
        closeButton.setFont(BUTTON_FONT);
        closeButton.setBackground((Color) close_action.getValue("Background"));
        b = Box.createHorizontalBox();
        b.add(insertButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(closeButton);
        contents
                .add(
                        "gridx=0,gridy=6,gridwidth=2,fill=NONE,anchor=CENTER,insets=[12,12,12,11]",
                        b);
        setJMenuBar(buildMenuBar());

    } // initComponents()

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = null;
        JMenuItem item = null;
        // File
        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));
        // file->close
        item = new JMenuItem(close_action);
        menu.add(item);
        menuBar.add(menu);
        // Options
        menu = new JMenu();
        menu.setText(resources.getString("optionsMenu.label"));
        menu.setMnemonic(resources.getString("optionsMenu.mnemonic").charAt(0));
        // options->increase font
        item = new JMenuItem(new IncreaseFontAction());
        menu.add(item);
        // options->decrease font
        item = new JMenuItem(new DecreaseFontAction());
        menu.add(item);
        menuBar.add(menu);
        return menuBar;
    } // buildMenuBar

    private void initValues() {
        try {

            // Termgroup values - only MTH/PN and SRC/XXX
			Termgroup[] alltermgroups = JekyllKit.getAuxDataClient().getTermgroups();

			ArrayList lst = new ArrayList();

			for (int i = 0; i < alltermgroups.length; i++) {
				if ((alltermgroups[i].equals(DEFAULT_TERMGROUP))
						|| (alltermgroups[i].getSource().toString().trim()
								.equals("SRC")))
					lst.add(alltermgroups[i]);
			}

			termgroups = (Termgroup[]) lst.toArray(new Termgroup[lst.size()]);
            Arrays.sort(termgroups);
            tgList.setListData(termgroups);

            // TBR values
            tbr_values = JekyllKit.getAuxDataClient()
                    .getValidTobereleasedValues();
            for (int i = 0; i < tbr_values.length; i++) {
                tbrComboBoxModel.addElement(String.valueOf(tbr_values[i]));
            }
            // Status values
            status_values = JekyllKit.getAuxDataClient()
                    .getValidStatusValuesForAtoms();
            for (int i = 0; i < status_values.length; i++) {
                statusComboBoxModel
                        .addElement(String.valueOf(status_values[i]));
            }
            // Released values
            released_values = JekyllKit.getAuxDataClient()
                    .getValidReleasedValues();
            for (int i = 0; i < released_values.length; i++) {
                releasedComboBoxModel.addElement(String
                        .valueOf(released_values[i]));
            }
        } catch (Exception ex) {
            MEMEToolkit.logComment(
                    "Failed to initialize values for \"Insert Atom\" screen.",
                    true);
            ex.printStackTrace(JekyllKit.getLogWriter());
        }
    } // initValues()

    private void clearContent() {
        conceptIdTF.setText(null);
        atom_name.setText(null);
        codeTF.setText(resources.getString("codeTextField.text"));
        tgList.setSelectedValue(DEFAULT_TERMGROUP, true);
        tbrComboBox.setSelectedItem(String.valueOf(CoreData.FV_RELEASABLE));
        statusComboBox.setSelectedItem(String
                .valueOf(CoreData.FV_STATUS_REVIEWED));
        generatedComboBox.setSelectedItem(String.valueOf(DEFAULT_GENERATED));
        releasedComboBox.setSelectedItem(String
                .valueOf(CoreData.FV_NOT_RELEASED));
        suppComboBox.setSelectedItem(String.valueOf(DEFAULT_SUPPRESSIBLE));
    } // clearContent()

    // -----------------------------
    // Inner Classes
    // -----------------------------
    /**
     * Adds atom to the current concept.
     * 
     * @see AbstractAction
     */
    class InsertAction extends AbstractAction {
        private Component target = null;

        public InsertAction(Component comp) {
            putValue(Action.NAME, "Insert");
            putValue(Action.SHORT_DESCRIPTION, "");
            // 	    putValue("Background", Color.lightGray);
            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (atom_name.getText().trim().equals("")) {
                MEMEToolkit.notifyUser(target,
                        "There's no name typed for the atom.");
                return;
            }
            if (codeTF.getText().trim().equals("")) {
                MEMEToolkit.notifyUser(target,
                        "There's no code typed for the atom.");
                return;
            }
            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);
            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(InsertAction.this
                            .getClass().getName(), true);
                    try {
                        String string = atom_name.getText().trim();
                        // matches whitespace character or no-break space
                        //            Pattern p = Pattern.compile("\\s|\u00A0{2,}");
                        // matches control characters including delete
                        // character,
                        // no-break space, and two or more spaces
                        Pattern p = Pattern
                                .compile("[\u0000-\u001f\\\u007F\\\u00A0]|\u0020{2,}");
                        Matcher m = p.matcher(string);
                        if (m.find()) {
                            //                            System.out.println("I found the text \""
                            //                                    + m.group() + "\" starting at index "
                            //                                    + m.start() + " and ending at index "
                            //                                    + m.end() + ".");
                            //                            System.out.println("pattern is found");
                            String[] tokens = string
                                    .split("[\u0000-\u0020\\\u007F\\\u00A0]{1,}");
                            StringBuffer sb = new StringBuffer(500);
                            for (int i = 0; i < tokens.length; i++) {
                                sb.append(tokens[i]);
                                if (i != (tokens.length - 1)) {
                                    sb.append(" ");
                                }
                            }
                            string = sb.toString();
                        }
                        String code_string = codeTF.getText().trim();
                        Atom atom = new Atom.Default();
                        atom.setConcept(current_concept);
                        atom.setString(string);
                        atom.setCode(Code.newCode(code_string));
                        atom
                                .setTermgroup(termgroups[tgList
                                        .getSelectedIndex()]);
                        atom.setSource(termgroups[tgList.getSelectedIndex()]
                                .getSource());
                        atom.setTobereleased(tbr_values[tbrComboBox
                                .getSelectedIndex()]);
                        atom.setStatus(status_values[statusComboBox
                                .getSelectedIndex()]);
                        if (generatedComboBox.getSelectedItem().equals("Y")) {
                            atom.setGenerated(true);
                        } else {
                            atom.setGenerated(false);
                        }
                        atom.setReleased(released_values[releasedComboBox
                                .getSelectedIndex()]);
                        if (suppComboBox.getSelectedItem().equals("Y")) {
                            atom.setSuppressible("E");
                        } else {
                            atom.setSuppressible("N");
                        }
                        boolean change_status = false;
                        // Checking whether there are other MTH/PN atoms
                        // present,
                        // and if so, change their tbr status to 'n'.
                        Atom[] atoms = current_concept.getAtoms();
                        for (int i = 0; i < atoms.length; i++) {
                            if ((atom.getTermgroup().equals(DEFAULT_TERMGROUP))
                                    && (atoms[i].getTermgroup()
                                            .equals(DEFAULT_TERMGROUP))) {
                                atoms[i]
                                        .setTobereleased(CoreData.FV_WEAKLY_UNRELEASABLE);
                                MolecularChangeAtomAction mcaa = new MolecularChangeAtomAction(
                                        atoms[i]);
                                JekyllKit.getDefaultActionClient()
                                        .processAction(mcaa);
                                change_status = true;
                            }
                        }
                        if (change_status) {
                            current_concept
                                    .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                        }
                        MolecularInsertAtomAction miaa = new MolecularInsertAtomAction(
                                atom);
                        JekyllKit.getDefaultActionClient().processAction(miaa);
                        MEMEToolkit.notifyUser(target,
                                "Atom has successfully been inserted.");
                        target.setVisible(false);
                        JekyllKit.getConceptSelector().refreshConcept();
                    } catch (StaleDataException sde) {
                        try {
                            setContent(JekyllKit.getCoreDataClient()
                                    .getConcept(current_concept));
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "You have attempted to perform an action on a"
                                                    + "\nconcept that has changed since it was last read."
                                                    + "\nThe concept was automatically re-read for you."
                                                    + "\nPlease try the intended action again.");
                        } catch (Exception ex) {
                            if (ex instanceof MissingDataException) {
                                MEMEToolkit.notifyUser(target, "Concept "
                                        + current_concept.getIdentifier()
                                                .toString() + " is no longer"
                                        + "\na valid concept in the database.");
                            } else {
                                ex.printStackTrace(JekyllKit.getLogWriter());
                            }
                        }
                    } catch (Exception ex) {
                        if (ex instanceof MEMEException
                                && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                            MEMEToolkit.reportError(target,
                                    "There was a network error."
                                            + "\nPlease try the action again.",
                                    false);
                        } else {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "Failed to insert atom."
                                                    + "\nLog file/Console may contain more information.");
                        }
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
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
            });
            t.start();
        }
    } // InsertAction

    public void setContent(Concept concept) {
        current_concept = concept;
        clearContent();
        if (current_concept == null) {
            return;
        }
        conceptIdTF.setText(current_concept.getIdentifier().toString());
    }
}