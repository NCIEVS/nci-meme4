/*
 * LogFileDialog.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.swing.FontSizeManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @see <a href="src/LogFileDialog.java.html">source </a>
 */
public class LogFileDialog extends JDialog {
    JTextArea logTextArea = new JTextArea();

    public LogFileDialog() {
        try {
            initComponents();
            FontSizeManager.addContainer(this);
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() throws Exception {
        logTextArea.setBorder(BorderFactory.createLineBorder(Color.black));
        logTextArea.setEditable(false);
        
        JScrollPane scroll_pane = new JScrollPane(logTextArea);
        scroll_pane.setBorder(null);
        scroll_pane.setPreferredSize(new Dimension(500, 300));

        JButton close_button = new JButton(new CloseAction(this));
        JPanel button_pane = new JPanel();
        button_pane.add(close_button);
        
        this
                .setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("Current log file");
        this.getContentPane().setLayout(new BorderLayout());
        
        this.getContentPane().add(scroll_pane, BorderLayout.CENTER);
        this.getContentPane().add(button_pane, BorderLayout.SOUTH);
    } //initComponents()

    void showDialog() {
        // clear content
        logTextArea.setText(null);

        try {
            BufferedReader in = new BufferedReader(new FileReader(JekyllKit
                    .getLogFile()));
            String line = null;
            while ((line = in.readLine()) != null) {
                logTextArea.append(line + "\n");
            }

            in.close();
        } catch (Exception ex) {
            MEMEToolkit.notifyUser(this, "Failed to retrieve a log file.");
            ex.printStackTrace(JekyllKit.getLogWriter());
        }

        this.setVisible(true);
        this.setLocationRelativeTo(null);
    } // showDialog()
}