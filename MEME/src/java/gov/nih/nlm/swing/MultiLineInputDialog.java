/*****************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Object:     MultiLineInputDialog.java
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * {@link JDialog} used to request multi-line input from the user.
 *
 * @author Brian Carlsen
 */
public class MultiLineInputDialog extends JDialog {

  //
  // Private Fields
  //
  private String value = null;
  private JTextArea jvalue = null;
  private JComponent dummy = new JPanel();
  private JButton ok_btn = null;

  /**
   * Instantiates a {@link MultiLineInputDialog} from the specified parameters.
   * @param parent the parent {@link Component}
   * @param message the message to the user
   * @param title the dialog title
   * @param initial_value the intial value
   */
  public MultiLineInputDialog(Component parent, String message,
                              String title, String initial_value) {
    //
    // Configure superclass
    //
    super();
    setModal(true);
    setTitle(title);

    //
    // Configure parent component
    //
    if (parent != null) {
      setLocationRelativeTo(parent);
    } else {
      setLocationRelativeTo(dummy);
    }
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    //
    // Create panel, configure constraints
    //
    JPanel data_panel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.insets = new Insets(2, 2, 2, 2);

    //
    // Add title
    //
    data_panel.add(new JLabel(message), constraints);
    constraints.gridy++;

    //
    // Add list data
    //
    jvalue = new JTextArea(10, 40);
    jvalue.setText(initial_value == null ? "" : initial_value);
    data_panel.add(new JScrollPane(jvalue), constraints);
    constraints.gridy++;

    //
    // Add buttons
    //
    JPanel control_panel = new JPanel(new GridBagLayout());
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridy = 0;
    constraints.insets = new Insets(15, 15, 15, 15);
    ok_btn = new JButton("OK");
    control_panel.add(ok_btn, constraints);
    getRootPane().setDefaultButton(ok_btn);

    //
    // The OK button has an action listener.
    // when the user clicks OK, it sets the value
    //
    ok_btn.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        value = jvalue.getText();
        setVisible(false);
      }
    });

    JButton cancel_btn = new JButton("Cancel");
    control_panel.add(cancel_btn, constraints);
    cancel_btn.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        value = null;
        setVisible(false);
      }
    });

    //
    // make buttons same size
    //
    ok_btn.setPreferredSize(cancel_btn.getPreferredSize());

    //
    // Finish configuring dialog, request focus
    //
    data_panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    getContentPane().add(data_panel, BorderLayout.CENTER);
    getContentPane().add(control_panel, BorderLayout.SOUTH);
    FontSizeManager.adjustFontSize(this);
    pack();
    jvalue.requestFocus();
  }

  /**
   * Returns the value entered by the user.
   * @return the value entered by the user
   */
  public String getValue() {
    return value;
  }

  //
  // Static methods
  //

  /**
   * Presents a {@link MultiLineInputDialog} to the user.  Returns
   * the users response.
   * @param parent the parent {@link Component}
   * @param message the message to the user
   * @param title the dialog title
   * @param initial_value the intial value
   * @return the value entered by the user
   */
  public static String showDialog(Component parent,
                                  String message,
                                  String title,
                                  String initial_value) {
    MultiLineInputDialog ld =
        new MultiLineInputDialog(parent, message, title, initial_value);
    ld.setVisible(true);
    String ret = ld.getValue();
    ld.dispose();
    return ret;
  }
}
