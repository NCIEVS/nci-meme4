/*
 * Finder.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.client.FinderClient;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.umls.jekyll.swing.ToggleSelectionModel;
import gov.nih.nlm.umls.jekyll.util.JavaToolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.SocketException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import samples.accessory.StringGridBagLayout;

/**
 * Finder dialog.
 * 
 * TODO: baloon help for different search methods.
 * 
 * @see <a href="src/Finder.java.html">source </a>
 * @see <a href="src/Finder.properties.html">properties </a>
 */
public class Finder extends JFrame {

    // Constants
    private final String ALL_WORD_CMD = "all.word.cmd";

    private final String ANY_WORD_CMD = "any.word.cmd";

    private final String ALL_NORM_WORD_CMD = "all.norm.word.cmd";

    private final String ANY_NORM_WORD_CMD = "any.norm.word.cmd";

    private final String NORM_STRING_CMD = "norm.string.cmd";

    private final String EXACT_STRING_CMD = "exact.string.cmd";

    private final String[] NUMBER_OF_RESULTS_TO_RETURN = { "5", "10", "25",
            "50", "100", "No Limit" };

    private final String DEFAULT_NUMBER_OF_RESULTS = "No Limit";

    // STY groups
    private final String ORGANISM_GROUP = "Organism";

    private final String ANATOMICAL_ABNORMALITY_GROUP = "Anatomical Abnormality";

    private final String ANATOMICAL_STRUCTURE_GROUP = "Anatomical Structure";

    private final String PATHOLOGICAL_FUNCTION_GROUP = "Pathological Function";

    private final String PHYSIOLOGICAL_FUNCTION_GROUP = "Physiological Function";

    private final String HEALTH_CARE_ACTIVITY_GROUP = "Health Care Activity";

    private final String CHEMICAL_GROUP = "Chemical";

    // Fields
    private ResourceBundle resources = null;

    private FinderClient finder_client = null;

    private SemanticType[] stys = null;

    private Source[] sources = null;

    // Components
    private GlassComponent glass_comp = null;

    private JTextField find_tf = null;

    private JRadioButton word_radiobt = null;

    private ButtonGroup button_group = null;

    private JList sty_list = null;

    private Vector cb_group = new Vector();

    private JCheckBox organism_cb = null;

    private JCheckBox anatomical_abnormality_cb = null;

    private JCheckBox anatomical_structure_cb = null;

    private JCheckBox pathological_function_cb = null;

    private JCheckBox physiological_function_cb = null;

    private JCheckBox health_care_activity_cb = null;

    private JCheckBox chemical_cb = null;

    private JList source_list = null;

    private JComboBox return_count_box = null;

    private JTextArea norm_results_ta = null;

    private JCheckBox r_concepts_cb = null;

    /**
     * Default constructor.
     */
    public Finder() {
        initResources();

        try {
            initComponents();
        } catch (Exception ex) {
            if (ex instanceof MEMEException
                    && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                MEMEToolkit.reportError(this, "There was a network error."
                        + "\nPlease try the action again.", false);
            } else {
                MEMEToolkit
                        .notifyUser("There was a problem initializing Finder."
                                + "\nIt may not work correctly."
                                + "\nPlease check log file for more information.");
            }
            ex.printStackTrace(JekyllKit.getLogWriter());
        }
        FontSizeManager.addContainer(this);
        FontSizeManager.adjustFontSize(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.FinderResources");
    }

    private void initComponents() throws Exception {
        Action a = null;
        Box b = null;
        JScrollPane sp = null;

        setTitle(resources.getString("window.title") + " | "
                + JekyllKit.getDataSource());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        // Build the contents
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());

        find_tf = new JTextField();

        // "Find" label
        JLabel find_label = new JLabel();
        find_label.setText(resources.getString("find.label"));
        find_label.setDisplayedMnemonic(resources.getString("find.mnemonic")
                .charAt(0));
        find_label.setLabelFor(find_tf);
        contents.add("gridx=0,gridy=0,anchor=WEST,insets=[12,12,0,0]",
                find_label);

        // "Clear" button
        JButton clear_button = GUIToolkit.getButton(new AbstractAction() {
            {
                putValue(Action.NAME, "Clear");
                putValue(Action.SHORT_DESCRIPTION, "clear search entry");
                putValue("Background", Color.cyan);
            }

            public void actionPerformed(ActionEvent e) {
                find_tf.setText(null);
                norm_results_ta.setText(null);
            }
        });

        b = Box.createHorizontalBox();
        b.add(find_tf);
        b.add(Box.createHorizontalStrut(12));
        b.add(clear_button);
        contents
                .add(
                        "gridx=1,gridy=0,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        // needed below
        word_radiobt = new JRadioButton();

        // "Search method" label
        JLabel method_label = new JLabel();
        method_label.setText(resources.getString("method.label"));
        method_label.setLabelFor(word_radiobt);
        contents.add("gridx=0,gridy=1,anchor=WEST,insets=[12,12,0,0]",
                method_label);

        // --------------------------------
        // 1st row of buttons
        // --------------------------------

        word_radiobt.setText(resources.getString("wordRadioButton.label"));
        word_radiobt.setMnemonic(resources
                .getString("wordRadioButton.mnemonic").charAt(0));
        word_radiobt.setActionCommand(ALL_WORD_CMD);
        // 	word_radiobt.setToolTipText("Lowercase word(s) search. It implicitly
        // uses \"AND\" operator,"
        // 				    + " which means terms with only word A and word B will be
        // retrieved.");

        JRadioButton any_word_radiobt = new JRadioButton();
        any_word_radiobt.setMnemonic(resources.getString(
                "anyWordRadioButton.mnemonic").charAt(0));
        any_word_radiobt.setText(resources
                .getString("anyWordRadioButton.label"));
        any_word_radiobt.setActionCommand(ANY_WORD_CMD);

        JRadioButton norm_word_radiobt = new JRadioButton();
        norm_word_radiobt.setText(resources
                .getString("normWordRadioButton.label"));
        norm_word_radiobt.setMnemonic(resources.getString(
                "normWordRadioButton.mnemonic").charAt(0));
        norm_word_radiobt.setActionCommand(ALL_NORM_WORD_CMD);

        b = Box.createHorizontalBox();
        b.add(word_radiobt);
        b.add(Box.createHorizontalStrut(5));
        b.add(any_word_radiobt);
        b.add(Box.createHorizontalStrut(5));
        b.add(norm_word_radiobt);
        contents.add(
                "gridx=1,gridy=1,fill=NONE,anchor=WEST,insets=[12,12,0,11]", b);

        // --------------------------------
        // 2nd row of radio buttons
        // --------------------------------

        JRadioButton any_norm_word_radiobt = new JRadioButton();
        any_norm_word_radiobt.setText(resources
                .getString("anyNormWordRadioButton.label"));
        any_norm_word_radiobt.setMnemonic(resources.getString(
                "anyNormWordRadioButton.mnemonic").charAt(0));
        any_norm_word_radiobt.setActionCommand(ANY_NORM_WORD_CMD);

        JRadioButton norm_str_radiobt = new JRadioButton();
        norm_str_radiobt.setText(resources
                .getString("normStrRadioButton.label"));
        norm_str_radiobt.setMnemonic(resources.getString(
                "normStrRadioButton.mnemonic").charAt(0));
        norm_str_radiobt.setActionCommand(NORM_STRING_CMD);

        JRadioButton exact_str_radiobt = new JRadioButton();
        exact_str_radiobt.setText(resources
                .getString("exactStrRadioButton.label"));
        exact_str_radiobt.setMnemonic(resources.getString(
                "exactStrRadioButton.mnemonic").charAt(0));
        exact_str_radiobt.setActionCommand(EXACT_STRING_CMD);

        b = Box.createHorizontalBox();
        b.add(any_norm_word_radiobt);
        b.add(Box.createHorizontalStrut(5));
        b.add(norm_str_radiobt);
        b.add(Box.createHorizontalStrut(5));
        b.add(exact_str_radiobt);
        contents.add(
                "gridx=1,gridy=2,fill=NONE,anchor=WEST,insets=[12,12,0,11]", b);

        // making a multiple-exclusion scope for a set of the buttons
        button_group = new ButtonGroup();
        button_group.add(word_radiobt);
        button_group.add(any_word_radiobt);
        button_group.add(norm_word_radiobt);
        button_group.add(any_norm_word_radiobt);
        button_group.add(norm_str_radiobt);
        button_group.add(exact_str_radiobt);
        // making a lowercase word default search method
        button_group.setSelected(word_radiobt.getModel(), true);

        // --------------------------------
        // Getting valid STYs and sources, etc.
        // --------------------------------

        stys = JekyllKit.getAuxDataClient().getValidSemanticTypes();
        Arrays.sort(stys);

        sources = JekyllKit.getAuxDataClient().getCurrentSources();
        Arrays.sort(sources);

        // size of scrollpanes for sty_list and source_list
        Dimension d = new Dimension(280, 80);

        // --------------------------------
        // List of STYs
        // --------------------------------

        // "Restrict by STY(s):" label
        JLabel sty_label = new JLabel();
        sty_label.setText(resources.getString("restrictBySTY.label"));
        sty_label.setLabelFor(sty_list);
        contents.add("gridx=0,gridy=3,anchor=WEST,insets=[12,12,0,0]",
                sty_label);

        sty_list = new JList(stys);
        sty_list.setSelectionModel(new ToggleSelectionModel());
        sty_list.setVisibleRowCount(4);
        sp = new JScrollPane(sty_list);
        sp.setPreferredSize(d);
        sp.setMinimumSize(d);
        // 	sp.setMaximumSize(d);
        contents
                .add(
                        "gridx=1,gridy=3,fill=BOTH,anchor=WEST,insets=[12,12,0,11]",
                        sp);

        // --------------------------------
        // STY Groups
        // --------------------------------

        // "Restrict by STY group:" label
        JLabel sty_groups_label = new JLabel();
        sty_groups_label.setText(resources
                .getString("restrictBySTYGroup.label"));
        contents.add("gridx=0,gridy=4,anchor=WEST,insets=[12,12,0,0]",
                sty_groups_label);

        JPanel checkbox_panel = new JPanel();
        checkbox_panel
                .setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        checkbox_panel.setLayout(new GridLayout(0, 2));
        checkbox_panel.setBorder(LineBorder.createGrayLineBorder());

        organism_cb = new JCheckBox(ORGANISM_GROUP);
        checkbox_panel.add(organism_cb);
        cb_group.add(organism_cb);

        anatomical_abnormality_cb = new JCheckBox(ANATOMICAL_ABNORMALITY_GROUP);
        checkbox_panel.add(anatomical_abnormality_cb);
        cb_group.add(anatomical_abnormality_cb);

        anatomical_structure_cb = new JCheckBox(ANATOMICAL_STRUCTURE_GROUP);
        checkbox_panel.add(anatomical_structure_cb);
        cb_group.add(anatomical_structure_cb);

        pathological_function_cb = new JCheckBox(PATHOLOGICAL_FUNCTION_GROUP);
        checkbox_panel.add(pathological_function_cb);
        cb_group.add(pathological_function_cb);

        physiological_function_cb = new JCheckBox(PHYSIOLOGICAL_FUNCTION_GROUP);
        checkbox_panel.add(physiological_function_cb);
        cb_group.add(physiological_function_cb);

        health_care_activity_cb = new JCheckBox(HEALTH_CARE_ACTIVITY_GROUP);
        checkbox_panel.add(health_care_activity_cb);
        cb_group.add(health_care_activity_cb);

        chemical_cb = new JCheckBox(CHEMICAL_GROUP);
        checkbox_panel.add(chemical_cb);
        cb_group.add(chemical_cb);

        checkbox_panel
                .setToolTipText("For a list of stys included for each group,"
                        + " please consult online help.");
        checkbox_panel.setMinimumSize(checkbox_panel.getPreferredSize());

        contents
                .add(
                        "gridx=1,gridy=4,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        checkbox_panel);

        // --------------------------------
        // List of sources
        // --------------------------------

        // "Restrict by Source(s):" label
        JLabel source_label = new JLabel();
        source_label.setText(resources.getString("restrictBySource.label"));
        source_label.setLabelFor(source_list);
        contents.add("gridx=0,gridy=5,anchor=WEST,insets=[12,12,0,0]",
                source_label);

        source_list = new JList(sources);
        source_list.setSelectionModel(new ToggleSelectionModel());
        source_list.setVisibleRowCount(4);
        sp = new JScrollPane(source_list);
        sp.setPreferredSize(d);
        sp.setMinimumSize(d);
        // 	sp.setMaximumSize(d);
        contents
                .add(
                        "gridx=1,gridy=5,fill=BOTH,anchor=WEST,insets=[12,12,0,11]",
                        sp);

        // --------------------------------
        // "Results to return" combo box
        // --------------------------------

        // "Return" label
        JLabel return_label = new JLabel();
        return_label.setText(resources.getString("return.label"));
        return_label.setLabelFor(return_count_box);
        contents.add("gridx=0,gridy=6,anchor=WEST,insets=[12,12,0,0]",
                return_label);

        return_count_box = new JComboBox(NUMBER_OF_RESULTS_TO_RETURN);
        return_count_box.setEditable(false);
        return_count_box.setSelectedItem(DEFAULT_NUMBER_OF_RESULTS);
        contents.add(
                "gridx=1,gridy=6,fill=NONE,anchor=WEST,insets=[12,12,0,11]",
                return_count_box);

        // --------------------------------
        // Norm Results
        // --------------------------------
        JLabel norm_results_label = new JLabel();
        norm_results_label.setText(resources.getString("normResults.label"));
        contents.add("gridx=0,gridy=7,anchor=WEST,insets=[12,12,0,0]",
                norm_results_label);

        norm_results_ta = new GUIToolkit.NonEditTextArea(true);
        norm_results_ta.setRows(2);
        norm_results_ta.setMinimumSize(norm_results_ta.getPreferredSize());
        contents
                .add(
                        "gridx=1,gridy=7,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        norm_results_ta);

        // --------------------------------
        // Only releasable concepts
        // --------------------------------
        r_concepts_cb = new JCheckBox("Releasable concepts only", false);
        contents
                .add(
                        "gridx=1,gridy=8,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        r_concepts_cb);

        // --------------------------------
        // Buttons
        // --------------------------------

        // Find button
        a = new FindAction(this);
        JButton find_button = GUIToolkit.getButton(a);

        // Close button
        a = new CloseAction(this);
        JButton close_button = GUIToolkit.getButton(a);

        b = Box.createHorizontalBox();
        b.add(find_button);
        b.add(Box.createHorizontalStrut(5));
        b.add(close_button);
        contents
                .add(
                        "gridx=0,gridy=9,gridwidth=2,fill=NONE,anchor=EAST,insets=[17,12,11,11]",
                        b);

        // Designating default button
        getRootPane().setDefaultButton(find_button);

        //    STYGroups groups = new STYGroups();
        //    SemanticType[] test = groups.getOrganismGroup();

    } // initComponents()

    /**
     * Returns a search string, with leading and trailing whitespaces omitted.
     */
    public String getString() {
        return find_tf.getText().trim();
    }

    // =====================================
    // Inner Classes
    // =====================================
    class FindAction extends AbstractAction {
        private Component target = null;

        public FindAction(Component comp) {
            putValue(Action.NAME, "Find");
            putValue(Action.SHORT_DESCRIPTION, "");
            // 	    putValue(Action.ACCELERATOR_KEY,
            // KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_MASK));
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (find_tf.getText().trim().equals(""))
                return;

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(FindAction.this.getClass()
                            .getName(), true);

                    try {
                        String string = find_tf.getText().trim();

                        MEMEToolkit.logComment("Search term: " + string);

                        // clear
                        finder_client = JekyllKit.getFinderClient();
                        finder_client.clearRestrictions();

                        if (!sty_list.isSelectionEmpty()) {
                            SemanticType[] selected_stys = (SemanticType[]) JavaToolkit
                                    .castArray(sty_list.getSelectedValues(),
                                            SemanticType.class);
                            finder_client
                                    .restrictBySemanticTypes(selected_stys);
                        }

                        // STY Groups
                        Vector included_stys = new Vector();
                        if (organism_cb.isSelected()) {
                            included_stys.addAll(Arrays.asList(new STYGroups()
                                    .getOrganismGroup()));
                        }

                        if (anatomical_abnormality_cb.isSelected()) {
                            included_stys.addAll(Arrays.asList(new STYGroups()
                                    .getAnatomicalAbnormalityGroup()));
                        }

                        if (anatomical_structure_cb.isSelected()) {
                            included_stys.addAll(Arrays.asList(new STYGroups()
                                    .getAnatomicalStructureGroup()));
                        }

                        if (pathological_function_cb.isSelected()) {
                            included_stys.addAll(Arrays.asList(new STYGroups()
                                    .getPathologicalFunctionGroup()));
                        }

                        if (physiological_function_cb.isSelected()) {
                            included_stys.addAll(Arrays.asList(new STYGroups()
                                    .getPhysiologicFunctionGroup()));
                        }

                        if (health_care_activity_cb.isSelected()) {
                            included_stys.addAll(Arrays.asList(new STYGroups()
                                    .getHCActivityGroup()));
                        }

                        if (chemical_cb.isSelected()) {
                            included_stys.addAll(Arrays.asList(new STYGroups()
                                    .getChemicalGroup()));
                        }

                        if (included_stys.size() != 0) {
                            finder_client
                                    .restrictBySemanticTypes((SemanticType[]) included_stys
                                            .toArray(new SemanticType[0]));
                        }
                        // ~ STY Groups

                        if (!source_list.isSelectionEmpty()) {
                            Source[] selected_sources = (Source[]) JavaToolkit
                                    .castArray(source_list.getSelectedValues(),
                                            Source.class);
                            finder_client.restrictBySources(selected_sources);
                        }

                        String return_count = (String) return_count_box
                                .getSelectedItem();
                        if (return_count.equals(DEFAULT_NUMBER_OF_RESULTS)) {
                            return_count = "1000000";
                        }
                        finder_client.setMaxResultCount(Integer
                                .parseInt(return_count));

                        if (r_concepts_cb.isSelected()) {
                            finder_client.restrictByReleasable();
                        }

                        String cmd = button_group.getSelection()
                                .getActionCommand();

                        Concept[] concepts = null;

                        if (cmd.equals(ALL_WORD_CMD)) {
                            concepts = finder_client
                                    .findAllWordMatches(JavaToolkit
                                            .tokenizeString(string
                                                    .toLowerCase()));
                        } else if (cmd.equals(ANY_WORD_CMD)) {
                            concepts = finder_client
                                    .findAnyWordMatches(JavaToolkit
                                            .tokenizeString(string
                                                    .toLowerCase()));
                        } else if (cmd.equals(ALL_NORM_WORD_CMD)) {
                            concepts = finder_client
                                    .findAllNormalizedWordMatches(JavaToolkit
                                            .tokenizeString(string));
                        } else if (cmd.equals(ANY_NORM_WORD_CMD)) {
                            concepts = finder_client
                                    .findAnyNormalizedWordMatches(JavaToolkit
                                            .tokenizeString(string));
                        } else if (cmd.equals(NORM_STRING_CMD)) {
                            concepts = finder_client
                                    .findNormalizedStringMatches(string);
                        } else {
                            concepts = finder_client
                                    .findExactStringMatches(string);
                        }

                        setNormResults(string, cmd);

                        if (concepts.length == 0) {
                            MEMEToolkit.notifyUser(target,
                                    "Your search yielded no results");
                            return;
                        }

                        // Initializing data
                        final Object[][] data = new Object[concepts.length][3];
                        for (int i = 0; i < concepts.length; i++) {
                            data[i][0] = new Integer(concepts[i]
                                    .getIdentifier().intValue());
                            data[i][1] = String
                                    .valueOf(concepts[i].getStatus());
                            data[i][2] = concepts[i].getPreferredAtom()
                                    .getString();
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ResultsDialog dialog = new ResultsDialog(
                                        Finder.this, false, data);
                                dialog.pack();
                                dialog.setVisible(true);
                            }
                        });
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
                                            "Search failed."
                                                    + "\nPlease check log file or console for more information.");
                        }
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    } finally {
                        glass_comp.setVisible(false);
                        target.setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));

                        logger.logElapsedTime();
                    }
                }
            });

            t.start();
        }
    } // FindAction

    private void setNormResults(String string, String cmd) {
        StringBuffer sb = new StringBuffer(500);

        try {
            if (cmd.equals(ALL_NORM_WORD_CMD) || cmd.equals(ANY_NORM_WORD_CMD)) {
                String[] norm_words = MIDServices.getLuiNormalizedWords(string);
                sb.append(string);
                sb.append(" |");
                for (int i = 0; i < norm_words.length; i++) {
                    sb.append(" ");
                    sb.append(norm_words[i]);
                }
            } else if (cmd.equals(NORM_STRING_CMD)) {
                String norm_string = MIDServices.getLuiNormalizedString(string);
                sb.append(string);
                sb.append(" | ");
                sb.append(norm_string);
            }
        } catch (Exception ex) {
            MEMEToolkit.logComment("Failed to set Norm results", true);
            ex.printStackTrace(JekyllKit.getLogWriter());
        }

        if (sb.length() != 0) {
            norm_results_ta.setText(sb.toString());
        } else {
            norm_results_ta.setText(null);
        }
    } // setNormResults()

    /**
     * Shows or hides this component. Also, clears any restrictions set by the
     * user.
     */
    public void setVisible(boolean b) {
        if (b) {
            // making a lowercase word default search method
            button_group.setSelected(word_radiobt.getModel(), true);
            find_tf.setText(null);
            norm_results_ta.setText(null);
            sty_list.clearSelection();
            source_list.clearSelection();
            if (finder_client != null) {
                finder_client.clearRestrictions();
            }
            for (int i = 0; i < cb_group.size(); i++) {
                ((JCheckBox) cb_group.get(i)).setSelected(false);
            }
            return_count_box.setSelectedItem(DEFAULT_NUMBER_OF_RESULTS);
            r_concepts_cb.setSelected(false);
            setTitle(resources.getString("window.title") + " | "
                    + JekyllKit.getDataSource());
        } else {
            Window[] windows = this.getOwnedWindows();
            for (int i = 0; i < windows.length; i++) {
                if (windows[i] instanceof ResultsDialog) {
                    ((Dialog) windows[i]).setVisible(false);
                    ((Dialog) windows[i]).dispose();
                }
            }

            if (finder_client != null) {
                finder_client.setMaxResultCount(100);
            }
        }
        super.setVisible(b);
    } // setVisible()

    class STYGroups {
        public SemanticType[] getOrganismGroup() {
            String[] stys_subset = new String[] { "Organism", "Plant", "Alga",
                    "Fungus", "Virus", "Rickettsia or Chlamydia", "Bacterium",
                    "Archaeon", "Invertebrate", "Vertebrate", "Amphibian",
                    "Bird", "Fish", "Reptile", "Mammal", "Human", "Animal" };
            Vector v = new Vector();
            java.util.List stys_list = Arrays.asList(stys_subset);
            for (int i = 0; i < stys.length; i++) {
                if (stys_list.contains(stys[i].getValue())) {
                    v.add(stys[i]);
                }
            }

            if (v.size() != stys_subset.length) {
                MEMEToolkit
                        .notifyUser(
                                Finder.this,
                                "Chosen STY group has not been initialized properly."
                                        + "\nSearch may return less results than expected.");
            }

            return (SemanticType[]) v.toArray(new SemanticType[0]);
        } // getOrganismGroup()

        public SemanticType[] getAnatomicalAbnormalityGroup() {
            String[] stys_subset = new String[] { "Anatomical Abnormality",
                    "Acquired Abnormality", "Congenital Abnormality" };
            Vector v = new Vector();
            java.util.List stys_list = Arrays.asList(stys_subset);
            for (int i = 0; i < stys.length; i++) {
                if (stys_list.contains(stys[i].getValue())) {
                    v.add(stys[i]);
                }
            }

            if (v.size() != stys_subset.length) {
                MEMEToolkit
                        .notifyUser(
                                Finder.this,
                                "Chosen STY group has not been initialized properly."
                                        + "\nSearch may return less results than expected.");
            }

            return (SemanticType[]) v.toArray(new SemanticType[0]);
        } // getAnatomicalAbnormalityGroup()

        public SemanticType[] getAnatomicalStructureGroup() {
            String[] stys_subset = new String[] { "Embryonic Structure",
                    "Fully Formed Anatomical Structure",
                    "Body Part, Organ, or Organ Component", "Tissue", "Cell",
                    "Cell Component", "Gene or Genome",
                    "Body Space or Junction", "Body Location or Region",
                    "Body System" };
            Vector v = new Vector();
            java.util.List stys_list = Arrays.asList(stys_subset);
            for (int i = 0; i < stys.length; i++) {
                if (stys_list.contains(stys[i].getValue())) {
                    v.add(stys[i]);
                }
            }

            if (v.size() != stys_subset.length) {
                MEMEToolkit
                        .notifyUser(
                                Finder.this,
                                "Chosen STY group has not been initialized properly."
                                        + "\nSearch may return less results than expected.");
            }

            return (SemanticType[]) v.toArray(new SemanticType[0]);
        } // getAnatomicalStructureGroup()

        public SemanticType[] getPathologicalFunctionGroup() {
            String[] stys_subset = new String[] { "Pathologic Function",
                    "Disease or Syndrome", "Mental or Behavioral Dysfunction",
                    "Neoplastic Process", "Cell or Molecular Dysfunction",
                    "Injury or Poisoning", "Body Substance", "Finding",
                    "Sign or Symptom" };
            Vector v = new Vector();
            java.util.List stys_list = Arrays.asList(stys_subset);
            for (int i = 0; i < stys.length; i++) {
                if (stys_list.contains(stys[i].getValue())) {
                    v.add(stys[i]);
                }
            }

            if (v.size() != stys_subset.length) {
                MEMEToolkit
                        .notifyUser(
                                Finder.this,
                                "Chosen STY group has not been initialized properly."
                                        + "\nSearch may return less results than expected.");
            }

            return (SemanticType[]) v.toArray(new SemanticType[0]);
        } // getPathologicalFunctionGroup()

        public SemanticType[] getPhysiologicFunctionGroup() {
            String[] stys_subset = new String[] { "Physiologic Function",
                    "Organism Function", "Mental Process",
                    "Organ or Tissue Function", "Cell Function",
                    "Molecular Function", "Genetic Function" };
            Vector v = new Vector();
            java.util.List stys_list = Arrays.asList(stys_subset);
            for (int i = 0; i < stys.length; i++) {
                if (stys_list.contains(stys[i].getValue())) {
                    v.add(stys[i]);
                }
            }

            if (v.size() != stys_subset.length) {
                MEMEToolkit
                        .notifyUser(
                                Finder.this,
                                "Chosen STY group has not been initialized properly."
                                        + "\nSearch may return less results than expected.");
            }

            return (SemanticType[]) v.toArray(new SemanticType[0]);
        } // getPhysiologicFunctionGroup()

        public SemanticType[] getHCActivityGroup() {
            String[] stys_subset = new String[] { "Laboratory Procedure",
                    "Diagnostic Procedure",
                    "Therapeutic or Preventive Procedure",
                    "Health Care Activity", "Laboratory or Test Result" };
            Vector v = new Vector();
            java.util.List stys_list = Arrays.asList(stys_subset);
            for (int i = 0; i < stys.length; i++) {
                if (stys_list.contains(stys[i].getValue())) {
                    v.add(stys[i]);
                }
            }

            if (v.size() != stys_subset.length) {
                MEMEToolkit
                        .notifyUser(
                                Finder.this,
                                "Chosen STY group has not been initialized properly."
                                        + "\nSearch may return less results than expected.");
            }

            return (SemanticType[]) v.toArray(new SemanticType[0]);
        } // getHCActivityGroup()

        public SemanticType[] getChemicalGroup() {
            String[] stys_subset = new String[] { "Pharmacologic Substance",
                    "Antibiotic", "Biomedical or Dental Material",
                    "Biologically Active Substance",
                    "Neuroreactive Substance or Biogenic Amine", "Hormone",
                    "Enzyme", "Vitamin", "Immunologic Factor", "Receptor",
                    "Indicator, Reagent, or Diagnostic Aid",
                    "Hazardous or Poisonous Substance", "Organic Chemical",
                    "Nucleic Acid, Nucleoside, or Nucleotide",
                    "Organophosphorus Compound",
                    "Amino Acid, Peptide, or Protein", "Carbohydrate", "Lipid",
                    "Steroid", "Eicosanoid", "Inorganic Chemical",
                    "Element, Ion, or Isotope", "Molecular Sequence",
                    "Nucleotide Sequence", "Amino Acid Sequence",
                    "Carbohydrate Sequence" };
            Vector v = new Vector();
            java.util.List stys_list = Arrays.asList(stys_subset);
            for (int i = 0; i < stys.length; i++) {
                if (stys_list.contains(stys[i].getValue())) {
                    v.add(stys[i]);
                }
            }

            if (v.size() != stys_subset.length) {
                MEMEToolkit
                        .notifyUser(
                                Finder.this,
                                "Chosen STY group has not been initialized properly."
                                        + "\nSearch may return less results than expected.");
            }

            return (SemanticType[]) v.toArray(new SemanticType[0]);
        } // getChemicalGroup()

    } // STYGroups
}