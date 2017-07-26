/**
 * FindDialog.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.nih.nlm.umls.jekyll.JekyllKit;

import gov.nih.nlm.meme.MEMEToolkit;

public class FindDialog
    extends JDialog
    implements ActionListener {

  //
  // Private Fields
  //
  private String CMD_CLOSE = "cmd.close";
  private String CMD_FIND = "cmd.find";
  private JTextField tf = null;
  private RelaEditor frame = null;

  // Constructor
  public FindDialog(Frame owner) {
    // modal dialog, i.e., other
    // windows cannot be active at the same time,
    // particularly, in this case, RelaEditor frame.
    super(owner, "Find by cluster id", true);
    frame = (RelaEditor) owner;
    initComponents();
    pack();
  }

  //
  // Methods
  //
  private void initComponents() {
    Container contents = getContentPane();
    contents.setLayout(new BoxLayout(contents, BoxLayout.X_AXIS));

    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // our text field
    tf = new JTextField(10);

    // "find" label
    JLabel label = new JLabel();
    label.setText("Enter cluster Id:");
    label.setLabelFor(tf);

    // a find button
    JButton find_btn = new JButton();
    find_btn.setText("Find");
    find_btn.setActionCommand(CMD_FIND);
    find_btn.addActionListener(this);

    // a close button
    JButton close_btn = new JButton();
    close_btn.setText("Close");
    close_btn.setMnemonic('C');
    close_btn.setActionCommand(CMD_CLOSE);
    close_btn.addActionListener(this);

    contents.add(Box.createHorizontalStrut(5));
    contents.add(label);
    contents.add(Box.createHorizontalStrut(5));
    contents.add(tf);
    contents.add(Box.createHorizontalStrut(5));
    contents.add(find_btn);
    contents.add(Box.createHorizontalStrut(5));
    contents.add(close_btn);

    // Set up the default button for the dialog
    getRootPane().setDefaultButton(find_btn);

    setLocationRelativeTo(frame);
  } // initComponents()

  private void closeDialog() {
    setVisible(false);
    dispose();
  }

  // ========================
  // Interface Implementation
  // ========================
  public void actionPerformed(ActionEvent evt) {
    String cmd = evt.getActionCommand();

    if (cmd.equals(CMD_FIND)) {

      if (tf.getText().equals("")) {
        return;
      }

      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      try {
        int cluster_id = Integer.parseInt(tf.getText());
        frame.getWrkList_Panel().setSelectedCluster(cluster_id);
        closeDialog();
      }
      catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid Cluster Id",
                                      "Invalid integer",
                                      JOptionPane.ERROR_MESSAGE);
      }
      catch (Exception ex) {
        ex.printStackTrace(JekyllKit.getLogWriter());
        MEMEToolkit.notifyUser(this, "Failed search by cluster id."
                               + "\nLog file may contain more information.");
      }
      finally {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
    }
    else if (cmd.equals(CMD_CLOSE)) {
      closeDialog();
    }
  }
}
