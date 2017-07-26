/*
 * ConfigureChecklistDialog.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.common.Checklist;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import samples.accessory.StringGridBagLayout;

/**
 * @see <a href="src/ConfigureChecklistDialog.java.html">source </a>
 * @see <a href="src/ConfigureChecklistDialog.properties.html">properties </a>
 */
public class ConfigureChecklistDialog extends JDialog {

    //
    // Private Fields
    //
    private ResourceBundle resources = null;

    private String CMD_OK = "cmd.ok";

    private String CMD_CLOSE = "cmd.close";

    private JTextField chknameField = null;

    private JTextField chkownerField = null;

    private Dialog owner = null;

    private Checklist checklist = null;

    private boolean configure = false;

    //
    // Constructors
    //
    /**
     * Default constructor.
     */
    public ConfigureChecklistDialog(Dialog parent, boolean modal, Checklist cc) {
        super(parent, modal);
        owner = parent;
        checklist = cc;
        initResources();
        initComponents();
        pack();
    }

    public boolean showDialog() {
        setVisible(true);
        return configure;
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle
                .getBundle("bundles.ConfigureChecklistDialogResources");
    }

    private void initComponents() {

        // set properties on the window
        setTitle(resources.getString("window.title"));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                windowAction(CMD_CLOSE);
            }
        });

        // Build the contents
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());

        chknameField = new JTextField(resources
                .getString("chkNameTextField.text"), 40);

        // "Checklist Name" label
        JLabel chknameLabel = new JLabel();
        chknameLabel.setDisplayedMnemonic(resources.getString(
                "checklistName.mnemonic").charAt(0));
        chknameLabel.setText(resources.getString("checklistName.label"));
        chknameLabel.setLabelFor(chknameField);
        contents.add("anchor=WEST,insets=[12,12,0,0]", chknameLabel);

        // "Checklist Name" text field
        contents.add("gridwidth=2,fill=HORIZONTAL,anchor=WEST,weightx=1.0"
                + ",insets=[12,12,0,11]", chknameField);

        chkownerField = new JTextField();

        // "Checklist Owner" label
        JLabel chkownerLabel = new JLabel();
        chkownerLabel.setDisplayedMnemonic(resources.getString(
                "checklistOwner.mnemonic").charAt(0));
        chkownerLabel.setText(resources.getString("checklistOwner.label"));
        chkownerLabel.setLabelFor(chkownerField);
        contents.add("gridx=0,gridy=1,anchor=WEST,insets=[12,12,0,0]",
                chkownerLabel);

        // "Checklist Owner" text field
        contents.add(
                "gridx=1,gridy=1,gridwidth=2,fill=HORIZONTAL,anchor=WEST,weightx=1.0"
                        + ",insets=[12,12,0,11]", chkownerField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, 0));

        // cancel button
        JButton cancelButton = new JButton();
        cancelButton.setText(resources.getString("cancelButton.label"));
        cancelButton.setActionCommand(CMD_CLOSE);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                windowAction(e);
            }
        });
        buttonPanel.add(cancelButton);

        // space
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        // ok button
        JButton okButton = new JButton();
        //         okButton.setMnemonic(
        //             resources.getString("okButton.mnemonic").charAt(0));
        okButton.setText(resources.getString("okButton.label"));
        okButton.setActionCommand(CMD_OK);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                windowAction(e);
            }
        });
        buttonPanel.add(okButton);

        // add the button panel
        contents.add(
                "gridx=0,gridy=2,gridwidth=3,anchor=EAST,insets=[17,12,11,11]",
                buttonPanel);

        // Set up the default button for the dialog
        getRootPane().setDefaultButton(okButton);

        setLocationRelativeTo(owner);

    } // initComponents()

    /**
     * The user has selected an option. Here we close and dispose the dialog. If
     * actionCommand is an ActionEvent, getCommandString() is called, otherwise
     * toString() is used to get the action command.
     * 
     * @param actionCommand
     *                  may be null
     */
    private void windowAction(Object actionCommand) {
        String cmd = null;
        if (actionCommand != null) {
            if (actionCommand instanceof ActionEvent) {
                cmd = ((ActionEvent) actionCommand).getActionCommand();
            } else {
                cmd = actionCommand.toString();
            }
        }
        if (cmd == null) {
            // do nothing
        } else if (cmd.equals(CMD_CLOSE)) {
            configure = false;
            setVisible(false);
            dispose();
        } else if (cmd.equals(CMD_OK)) {
            configure = true;
            checklist.setName(chknameField.getText().trim());
            checklist.setOwner(chkownerField.getText().trim());
            // 	    checklist.setOwner((String)jowner.getSelectedItem());
            setVisible(false);
            dispose();
        }
    } // windowAction()
}