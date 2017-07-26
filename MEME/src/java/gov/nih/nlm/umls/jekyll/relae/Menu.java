/**
 * Menu.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.swing.ListDialog;
import gov.nih.nlm.umls.jekyll.JekyllKit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 * Menu bar for Rela Editor frame.
 */
public class Menu extends JMenuBar implements ActionListener {

    //
    // Private Fields
    //
    private String CMD_OPEN_CONCEPT = "cmd.open.concept";

    private String CMD_FIND = "cmd.find";

    private String CMD_SET_SOURCE = "cmd.set.source";

    private String CMD_EXIT = "cmd.exit";

    private String CMD_REL_REFRESH = "cmd.rel.refresh";

    private String CMD_REL_INFO = "cmd.rel.info";

    private String CMD_REL_ATTR_DEF = "cmd.rel.attr.def";

    private String CMD_REL_INV_ATTR = "cmd.rel.inv.attr";

    private String CMD_REL_RESTR_ATTR = "cmd.rel.restr.attr";

    private String CMD_REL_ALL_ATTR = "cmd.rel.all.attr";

    private String CMD_HELP_ABOUT = "cmd.help.about";

    private RelaEditor frame = null;

    private String[] sabs = null;

    //
    // Constructors
    //
    public Menu(RelaEditor frame) {
        this.frame = frame;
        initComponents();
    }

    //
    // Methods
    //
    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(buildFileMenu());
        add(buildRelationshipMenu());
        add(Box.createHorizontalGlue());
        add(buildHelpMenu());
    }

    private JMenu buildFileMenu() {
        JMenu menu = null;
        JMenuItem item = null;

        menu = new JMenu();
        menu.setText("File");
        menu.setMnemonic('F');

        // File -> Open a concept
        item = new JMenuItem();
        item.setText("Open a concept");
        item.setMnemonic('c');
        item.setAccelerator(KeyStroke.getKeyStroke('C',
                java.awt.Event.CTRL_MASK));
        item.setActionCommand(CMD_OPEN_CONCEPT);
        item.addActionListener(this);
        menu.add(item);

        // File -> Find
        item = new JMenuItem();
        item.setText("Search by cluster id");
        item.setMnemonic('F');
        item.setAccelerator(KeyStroke.getKeyStroke('F',
                java.awt.Event.CTRL_MASK));
        item.setActionCommand(CMD_FIND);
        item.addActionListener(this);
        item.setEnabled(false);
        menu.add(item);

        // File -> Set Source
        item = new JMenuItem();
        item.setText("Select Source");
        item.setMnemonic('S');
        item.setActionCommand(CMD_SET_SOURCE);
        item.addActionListener(this);
        menu.add(item);

        // separator
        menu.addSeparator();

        // File -> Exit
        item = new JMenuItem();
        item.setText("Exit");
        item.setMnemonic('x');
        item.setActionCommand(CMD_EXIT);
        item.addActionListener(this);
        menu.add(item);

        return menu;
    } // buildFileMenu()

    private JMenu buildRelationshipMenu() {
        JMenu menu = null;
        JMenuItem item = null;
        JRadioButtonMenuItem radioItem = null;

        menu = new JMenu();
        menu.setText("Relationship");
        menu.setMnemonic('R');

        // Relationship -> Refresh
        item = new JMenuItem();
        item.setText("Refresh");
        item.setMnemonic('R');
        item.setAccelerator(KeyStroke.getKeyStroke('R',
                java.awt.Event.CTRL_MASK));
        item.setActionCommand(CMD_REL_REFRESH);
        item.addActionListener(this);
        menu.add(item);

        // separator
        menu.addSeparator();

        // Relationship -> information
        item = new JMenuItem();
        item.setText("Information");
        item.setMnemonic('I');
        item.setAccelerator(KeyStroke.getKeyStroke('I',
                java.awt.Event.CTRL_MASK));
        item.setActionCommand(CMD_REL_INFO);
        item.addActionListener(this);
        menu.add(item);

        // Relationship -> Attribute definition
        item = new JMenuItem();
        item.setText("Attribute Definition");
        item.setMnemonic('D');
        item.setActionCommand(CMD_REL_ATTR_DEF);
        item.addActionListener(this);
        menu.add(item);

        // Relationship -> Invert attribute
        item = new JMenuItem();
        item.setText("Invert Attribute");
        item.setMnemonic('n');
        item.setActionCommand(CMD_REL_INV_ATTR);
        item.addActionListener(this);
        menu.add(item);

        // separator
        menu.addSeparator();

        ButtonGroup attrRadioGroup = new ButtonGroup();

        // Relationship -> Restricted attributes
        radioItem = new JRadioButtonMenuItem();
        radioItem.setText("Restricted Attributes");
        radioItem.setMnemonic('R');
        radioItem.setActionCommand(CMD_REL_RESTR_ATTR);
        radioItem.addActionListener(this);
        // disabling this for NLM03 project
        radioItem.setEnabled(false);
        attrRadioGroup.add(radioItem);
        menu.add(radioItem);

        // relationship->all attributes
        radioItem = new JRadioButtonMenuItem();
        radioItem.setText("All Attributes");
        radioItem.setMnemonic('A');
        radioItem.setActionCommand(CMD_REL_ALL_ATTR);
        radioItem.addActionListener(this);
        // disabling this for NLM03 project
        radioItem.setEnabled(false);
        attrRadioGroup.add(radioItem);
        menu.add(radioItem);

        return menu;
    } // buildRelationshipMenu()

    private JMenu buildHelpMenu() {
        JMenu menu = null;
        JMenuItem item = null;

        menu = new JMenu();
        menu.setText("Help");
        menu.setMnemonic('H');
        menu.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // help->about rela editor
        item = new JMenuItem();
        item.setText("About Rela Editor");
        item.setMnemonic('A');
        item.setActionCommand(CMD_HELP_ABOUT);
        item.addActionListener(this);
        item.setEnabled(false);
        menu.add(item);

        return menu;
    } // buildHelpMenu()

    //
    // Interface Implementation
    //
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();

        RelationshipsPane nlm_rels_panel = frame.getNLM_Rels_Panel();
        RelPane rela_panel = frame.getRela_Panel();
        WrkListPane wrklist_panel = frame.getWrkList_Panel();

        frame.getGlassPane().setVisible(true);

        try {
            if (cmd.equals(CMD_OPEN_CONCEPT)) {
                ConceptDialog dialog = new ConceptDialog(frame);
                dialog.pack();
                dialog.setVisible(true);
            } else if (cmd.equals(CMD_FIND)) {
                FindDialog dialog = new FindDialog(frame);
                dialog.setVisible(true);
            } else if (cmd.equals(CMD_SET_SOURCE)) {
                if (sabs == null) {
                    sabs = JekyllKit.getAuxDataClient()
                            .getSourceAbbreviations();
                    Arrays.sort(sabs);
                }

                String current_sab = RelSemantics.getCurrentSAB_SL();
                String selected_sab = (String) ListDialog.showListSingleMode(
                        frame, "Select Source", "Select Source", sabs,
                        current_sab);
                if (selected_sab != null) {
                    frame.updateSAB_SL(selected_sab);
                }

            } else if (cmd.equals(CMD_REL_REFRESH)) {
                wrklist_panel.refresh(false);
            } else if (cmd.equals(CMD_REL_INFO)) {
                if (nlm_rels_panel.getSelectedRel() == null) {
                    MEMEToolkit
                            .notifyUser("Please select a relationship first.");
                    return;
                }

                InfoDialog dialog = new InfoDialog(frame,
                        "Additional Relationship Info", nlm_rels_panel
                                .getSelectedRel());
                dialog.pack();
                dialog.setVisible(true);
            } else if (cmd.equals(CMD_REL_ATTR_DEF)) {
                if (rela_panel.getSelectedValue() == null) {
                    MEMEToolkit
                            .notifyUser("Relationship attribute must be selected first.");
                    return;
                }

                InfoDialog dialog = new InfoDialog(frame,
                        "Attribute Definition & Example", rela_panel
                                .getSelectedValue().toString());
                dialog.pack();
                dialog.setVisible(true);
            } else if (cmd.equals(CMD_REL_INV_ATTR)) {
                if (rela_panel.getSelectedValue() == null) {
                    MEMEToolkit
                            .notifyUser("Relationship attribute must be selected first.");
                    return;
                }

                rela_panel.invertRelAttribute();
            } else if (cmd.equals(CMD_REL_RESTR_ATTR)) {
                rela_panel.setContent(RelSemantics.getAttributesRestrBySTYs(
                        null, null));
            } else if (cmd.equals(CMD_REL_ALL_ATTR)) {
                rela_panel.setContent(RelSemantics.getAllAttributes());
            } else if (cmd.equals(CMD_EXIT)) {
                frame.setVisible(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            frame.getGlassPane().setVisible(false);
        }
    } // actionPerformed()

}