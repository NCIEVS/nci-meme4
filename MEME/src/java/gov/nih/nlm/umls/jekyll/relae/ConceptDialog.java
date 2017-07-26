/**
 * ConceptDialog.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.util.regex.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.umls.jekyll.JekyllKit;
import gov.nih.nlm.meme.exception.*;

public class ConceptDialog
    extends JDialog
    implements ActionListener {

  //
  // Private Fields
  //
  private String CMD_CLOSE = "cmd.close";
  private String CMD_OPEN = "cmd.open";
  private JTextField tf = null;
  private RelaEditor frame = null;

  //
  // Constructors
  //
  public ConceptDialog(Frame owner) {
    // non-modal dialog, i.e., other
    // windows can be active at the same time
    super(owner, "Open a concept", false);
    frame = (RelaEditor) owner;
    initComponents();
  }

  //
  // Methods
  //
  private void initComponents() {
    Container contents = getContentPane();
    contents.setLayout(new BoxLayout(contents, BoxLayout.X_AXIS));

    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // tf for concept_id
    tf = new JTextField(10);
    tf.setMinimumSize(tf.getPreferredSize());

    JLabel concept_id_lbl = new JLabel();
    concept_id_lbl.setText("Concept Id:");
    concept_id_lbl.setLabelFor(tf);

    // "Open" button
    JButton open_btn = new JButton();
    open_btn.setText("Open");
    open_btn.setActionCommand(CMD_OPEN);
    open_btn.addActionListener(this);

    // a close button
    JButton close_btn = new JButton();
    close_btn.setText("Close");
    close_btn.setMnemonic('C');
    close_btn.setActionCommand(CMD_CLOSE);
    close_btn.addActionListener(this);

    contents.add(Box.createHorizontalStrut(5));
    contents.add(concept_id_lbl);
    contents.add(Box.createHorizontalStrut(5));
    contents.add(tf);
    contents.add(Box.createHorizontalStrut(5));
    contents.add(open_btn);
    contents.add(Box.createHorizontalStrut(5));
    contents.add(close_btn);

    // Set up the default button for the dialog
    getRootPane().setDefaultButton(open_btn);

    setLocationRelativeTo(frame);
  } // initComponents()

  private boolean isInteger(String str) {
    Pattern p = Pattern.compile("^[0-9]+$");
    Matcher m = p.matcher(str);
    return m.matches();
  }

  //
  // Interface implementation
  //
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equals(CMD_CLOSE)) {
      setVisible(false);
      dispose();
    }
    else if (cmd.equals(CMD_OPEN)) {
      String str = tf.getText().trim();

      if (str.equals("")) {
        return;
      }

      // check if a concept_id is valid
      if (!isInteger(str)) {
        MEMEToolkit.notifyUser(this, "This is not a valid concept id");
        return;
      }

      frame.getGlassPane().setVisible(true);
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      try {
        Concept concept = JekyllKit.getCoreDataClient().getConcept(Integer.
            parseInt(str));
        frame.getWrkList_Panel().setConcept(concept);
        // get rid off dialog
        this.setVisible(false);
        this.dispose();
      }
      catch (MissingDataException ex) {
        MEMEToolkit.notifyUser(this, "Concept was not found: " + str);
      }
      catch (Exception ex) {
        ex.printStackTrace(JekyllKit.getLogWriter());
        MEMEToolkit.notifyUser(this, "Failed to resolve concept: " +
                               str +
                               "\nLog file may contain more information.");

      }
      finally {
        frame.getGlassPane().setVisible(false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
    }
  } // actionPerformed()

}
