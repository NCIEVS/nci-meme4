/*****************************************************************************
 *
 * Package:    gov.nih.nlm.swing
 * Object:     PasswordDialog.java
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
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * {@link JDialog} used to obtain a username and password from a user.
 *
 * @author Brian Carlsen
 */
public class PasswordDialog extends JDialog {

  //
  // Fields
  //
  private JComponent dummy = new JPanel();
  private JButton ok_btn = null;
  private JTextField user_name_field = new JTextField(20);
  private JPasswordField pwd_field = new JPasswordField(20);
  private String user_name = "";
  private String pwd = "";
  private boolean cancelled = false;

  /**
   * Instantiates a {@link PasswordDialog} from the specified parameters.
   * @param parent the parent {@link Component} that this
   *               dialog should appear relative to
   * @param message the message that should appear in the dialog
   * @param title the title that should appear at the top of the dialog window
   * @param default_user_name the default username
   */
  public PasswordDialog(
      Component parent, String message, String title,
      String default_user_name) {
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
    // Configure constraints
    //
    JPanel data_panel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.insets = new Insets(2, 2, 2, 2);

    //
    // Add title
    //
    constraints.gridy = 0;
    JLabel label = new JLabel(message);
    data_panel.add(label, constraints);

    //
    // Add list data
    //
    constraints.gridy++;
    JLabel user_name_label = new JLabel("Username:");
    data_panel.add(user_name_label, constraints);
    if (!default_user_name.equals("")) {
      user_name_field.setText(default_user_name);
    }
    data_panel.add(user_name_field, constraints);
    constraints.gridy++;
    JLabel pwd_label = new JLabel("Password:");
    data_panel.add(pwd_label, constraints);
    data_panel.add(pwd_field, constraints);

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
        user_name = user_name_field.getText();
        pwd = new String(pwd_field.getPassword());
        setVisible(false);
      }
    });

    JButton cancel_btn = new JButton("Cancel");
    control_panel.add(cancel_btn, constraints);
    cancel_btn.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        cancelled = true;
      }
    });

    //
    // make buttons same size
    //
    ok_btn.setPreferredSize(cancel_btn.getPreferredSize());

    //
    // Finish configuring dialog
    //
    data_panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    getContentPane().add(data_panel, BorderLayout.CENTER);
    getContentPane().add(control_panel, BorderLayout.SOUTH);
    FontSizeManager.adjustFontSize(this);
    pack();
  }

  /**
   * Returns the user name.
   * @return the user name
   */
  public String getUserName() {
    return user_name;
  }

  /**
   * Returns the password.
   * @return the password
   */
  public String getPassword() {
    return pwd;
  }

  public boolean cancelled() {
    return cancelled;
  }

}