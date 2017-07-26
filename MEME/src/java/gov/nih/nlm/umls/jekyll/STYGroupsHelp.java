/*
 * STYGroupsHelp.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class STYGroupsHelp extends JDialog {

    private JTextArea text_area = null;

    private String help_file = "/auxfiles/sty_groups_help.txt";

    // Constructor
    public STYGroupsHelp(Frame owner) {
        super(owner);

        initComponents();
        initValues();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("STY Groups");

        Container container = this.getContentPane();
        container.setLayout(new BorderLayout());

        text_area = new JTextArea();
        text_area.setEditable(false);
        text_area.setLineWrap(true);
        text_area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(text_area);
        sp.setPreferredSize(new Dimension(500, 300));

        JPanel button_panel = new JPanel();
        JButton close_btn = new JButton(new CloseAction(this));
        button_panel.add(close_btn);

        container.add(sp, BorderLayout.CENTER);
        container.add(button_panel, BorderLayout.SOUTH);

    } // initComponents()

    private void initValues() {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream(help_file)));
            String line = null;
            while ((line = in.readLine()) != null) {
                text_area.append(line + "\n");
            }

            in.close();
            text_area.setCaretPosition(0);
        } catch (Exception ex) {
            MEMEToolkit.notifyUser(this, "Failed to retrieve help file."
                    + "\nLog file or console may contain more information.");
            ex.printStackTrace(JekyllKit.getLogWriter());
        }
    } // initValues()

}