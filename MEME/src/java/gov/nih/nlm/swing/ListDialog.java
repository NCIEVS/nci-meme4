/*****************************************************************************
 *
 * Package:    gov.nih.nlm.swing
 * Object:     ListDialog.java
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * {@link JDialog} used to present a user with a list of choices.
 *
 * @author Brian Carlsen
 */
public class ListDialog extends JDialog {

  //
  // Fields
  //
  private Object ret_value = null;
  private Object[] ret_values = null;
  private SuperJList jvalue = null;
  private JComponent dummy = new JPanel();
  private JButton ok_btn = null;

  /**
   * Instantiates a {@link ListDialog} from the specified parameters.
   * @param parent the parent {@link Component} that this
   *               dialog should appear relative to
   * @param message the message that should appear in the dialog
   * @param title the title that should appear at the top of the dialog window
   * @param values the list of values
   * @param initial_selections the initially selected values
   * @param single_selection_mode a flag indicating whether or not
   *         multiple rows can be selected.
   */
  public ListDialog(
      Component parent, Object message, String title,
      Object[] values, Object[] initial_selections,
      boolean single_selection_mode) {

    //
    // Configure superclass
    //
    super();
    setModal(true);
    setTitle(title);

    setDefaultCloseOperation(HIDE_ON_CLOSE);

    //
    // Create panel, configure constraints
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
    JLabel label = new JLabel( (String) message);
    data_panel.add(label, constraints);

    //
    // Add list data
    //
    constraints.gridy++;
    jvalue = new SuperJList(values);
    if (single_selection_mode) {
      jvalue.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    } else {
      jvalue.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
    jvalue.setVisibleRowCount( ( (values.length > 15) ? 15 : values.length));
    JScrollPane listScrollPane = new JScrollPane(jvalue);
    jvalue.addMouseListener(
        new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          ok_btn.doClick();
        }
      }
    });

    //
    // Handle initial selection
    //
    if (initial_selections != null) {
      jvalue.setSelectedValues(initial_selections, true);
    } else {
      jvalue.selectAll();

    }
    data_panel.add(listScrollPane, constraints);

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
        ret_value = jvalue.getSelectedValue();
        ret_values = jvalue.getSelectedValues();
        setVisible(false);
      }
    });

    JButton cancel_btn = new JButton("Cancel");
    control_panel.add(cancel_btn, constraints);
    cancel_btn.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ret_value = null;
        ret_values = new Object[0];
        setVisible(false);
      }
    });

    //
    // make buttons same size
    //
    ok_btn.setPreferredSize(cancel_btn.getPreferredSize());

    //
    // Configure remaining dialog, request focus
    //
    data_panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    getContentPane().add(data_panel, BorderLayout.CENTER);
    getContentPane().add(control_panel, BorderLayout.SOUTH);

    FontSizeManager.adjustFontSize(this);
    pack();
    jvalue.requestFocus();

    //
    // Handle parent
    //
    if (parent != null) {
      setLocationRelativeTo(parent);
    } else {
      setLocationRelativeTo(dummy);

    }
  }

  /**
   * This method returns the user selection.  This should be used by
   * applications that directly instantiate {@link ListDialog}.
   * @return the user selection
   */
  public Object getValue() {
    return ret_value;
  }

  /**
   * This method returns the user selections.  This should be used by
   * applications that directly instantiate {@link ListDialog}.
   * @return the user selections
   */
  public Object[] getValues() {
    return ret_values;
  }

  //
  // Static methods
  //

  /**
   * Presents the user with a {@link ListDialog} containing a list of choices.
   * When the user selects, the value is returned.
   * @param parent the parent {@link Component}
   * @param message the message {@link Object}
   * @param title the title {@link String}
   * @param values the values {@link Object} []
   * @param initial_selection the initial selection {@link Object}
   * @return the user selection
   */
  public static Object showListSingleMode(
      Component parent, Object message, String title,
      Object[] values, Object initial_selection) {
    Object[] initial_selections = {
        initial_selection};
    ListDialog ld = new ListDialog(parent, message, title,
                                   values, initial_selections, true);
    ld.setVisible(true);
    Object ret = ld.getValue();
    ld.dispose();
    return ret;
  }

  /**
   * Presents the user with a {@link ListDialog} containing a list of choices.
       * When the user selects one or more entries, the selected values are returned.
   * @param parent the parent {@link Component}
   * @param message the message {@link Object}
   * @param title the title {@link String}
   * @param values the values {@link Object} []
   * @param initial_selection the initial selection {@link Object}
   * @return the user selections
   */
  public static Object[] showListMultipleMode(
      Component parent, Object message, String title,
      Object[] values, Object initial_selection) {
    Object[] initial_selections = {
        initial_selection};
    ListDialog ld = new ListDialog(parent, message, title,
                                   values, initial_selections, false);
    ld.setVisible(true);
    Object[] rv = ld.getValues();
    ld.dispose();
    return rv;
  }

  /**
   * Presents the user with a {@link ListDialog} containing a list of choices.
       * When the user selects one or more entries, the selected values are returned.
   * @param parent the parent {@link Component}
   * @param message the message {@link Object}
   * @param title the title {@link String}
   * @param values the values {@link Object} []
   * @param initial_selections the initial selections {@link Object}[]
   * @return the user selections
   */
  public static Object[] showListMultipleMode(
      Component parent, Object message, String title,
      Object[] values, Object[] initial_selections) {
    ListDialog ld = new ListDialog(parent, message, title,
                                   values, initial_selections, false);
    ld.setVisible(true);
    Object[] rv = ld.getValues();
    ld.dispose();
    return rv;
  }

}