/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     UnaryIntegrityCheckStep.java
 *
 * Changes
 * 02/12/007 BAC (1-DHBFF): properly handle setting of "values" fields
 *   11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.IntegrityCheck;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
import gov.nih.nlm.recipe.UnaryIntegrityCheck;
import gov.nih.nlm.swing.IntegrityCheckListCellRenderer;
import gov.nih.nlm.swing.SuperJList;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;


/**
 * This step is used for inserting or deleting integrity checks tracked
 * in the ic_single and ic_single integrity check tables.  It makes use
 * of the MEMEToolkit.DBToolkit.getIntegrityChecks() method.
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class UnaryIntegrityCheckStep
    extends RxStep {

  //
  // CONSTANTS
  //
  private final static int INSERT = 0;
  private final static int DELETE = 1;

  //
  // Fields
  //
  protected int insert_or_delete; // 0=insert, 1=delete
  protected UnaryIntegrityCheck[] ic_single;
  protected String comments;

  /**
   * UnaryIntegrityCheckStep default constructor
   */
  public UnaryIntegrityCheckStep() {
    super();
    RxToolkit.trace("UnaryIntegrityCheckStep::UnaryIntegrityCheckStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("UnaryIntegrityCheckStep::resetValues()");
    insert_or_delete = INSERT;
    ic_single = new UnaryIntegrityCheck[] {};
    comments = "";
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("UnaryIntegrityCheckStep::constructView()");
    return new UnaryIntegrityCheckStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("UnaryIntegrityCheckStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("UnaryIntegrityCheckStep::toHTML()");
    StringBuffer step_text = new StringBuffer();

    // put html here
    step_text.append("              <table ALIGN=CENTER WIDTH=90% BORDER=1>\n");
    step_text.append("               <tr>\n");
    step_text.append("                <th>Name</th>\n");
    step_text.append("                <th>Type</th>\n");
    step_text.append("                <th>Value</th>\n");
    step_text.append("                <th>Negation</th>\n");
    step_text.append("               </tr>\n");
    for (int i = 0; i < ic_single.length; i++) {
      step_text.append("               <tr>\n");
      step_text.append("                  <td ALIGN=CENTER>");
      step_text.append(ic_single[i].ic_name);
      step_text.append("</td>\n");
      step_text.append("                  <td ALIGN=CENTER>");
      step_text.append(ic_single[i].type);
      step_text.append("</td>\n");
      step_text.append("                  <td ALIGN=CENTER>");
      step_text.append(ic_single[i].value);
      step_text.append("</td>\n");
      step_text.append("                  <td ALIGN=CENTER>");
      step_text.append(ic_single[i].negation);
      step_text.append("</td>\n");
      step_text.append("               </tr>\n");
    }

    if (comments.length() != 0) {
      step_text.append("              <tr>\n");
      step_text.append("                <td><b>Comments:</b></td><td>");
      step_text.append(comments);
      step_text.append("</td></tr>\n");
    }
    step_text.append("              </table>\n");

    return step_text.toString();
  };

  /**
   * Produces shell script code for this step
   */
  public String toShellScript() {

    StringBuffer body = new StringBuffer(500);
    body.append("#\n# Unary Integrity Check\n");
    if (comments != null && !comments.equals("")) {
      body.append("#").append(comments).append("\n");
    }
    body.append("#\n");
    body.append(
        "$ORACLE_HOME/bin/sqlplus -s $user@$db << EOF >&! /tmp/t.$$.log\n" +
        "    WHENEVER SQLERROR EXIT -1\n" +
        "    set feedback off\n\n");
    for (int i = 0; i < ic_single.length; i++) {
      if (insert_or_delete == INSERT) {
        body.append(
            "    INSERT INTO ic_single\n" +
            "    (ic_name, negation, type, value)\n" +
            "    VALUES\n " +
            "    ('");
        body.append(ic_single[i].ic_name).append("','");
        body.append( (ic_single[i].negation) ? "Y" : "N").append("','");
        body.append(ic_single[i].type).append("','");
        body.append(ic_single[i].value).append("');\n\n");
      }
      else {
        body.append(
            "    DELETE FROM ic_single\n" +
            "    WHERE ic_name = '");
        body.append(ic_single[i].ic_name).append("'\n" +
                                                 "      AND type = '");
        body.append(ic_single[i].type).append("'\n" +
                                              "      AND value = '");
        body.append(ic_single[i].value).append("'\n" +
                                               "      AND negation = '");
        body.append( (ic_single[i].negation) ? "Y" : "N").append("';\n\n");
      }
    }
      body.append("EOF\n" +                  /** KAL added refresh caches step 02/27/06**/
                  "if ($status != 0) then\n" +
                  "    echo \"Error handling unary check\"\n" +
                  "    cat /tmp/t.$$.log\n" +
                  "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error handling unary check\"' \n" +
              "    exit 1\n" +
                  "endif\n");
      body.append("\n" +
                  "$MEME_HOME/bin/admin.pl -s refresh_caches -d $db -host $host -port $port >&! /tmp/t.$$.log\n");
      body.append("if ($status != 0) then\n" +
                  "    echo \"Error refreshing server cache\"\n" +
                   "    cat /tmp/t.$$.log\n" +
                  "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error refreshing server cache\"' \n" +
              "    exit 1\n" +
                  "endif\n");
      return body.toString();
  }

  /**
   * This method returns an HTML representation of the step type
   * for use in rendering the section header for a recipe step
   * @return String
   */
  public String typeToHTML() {
    if (insert_or_delete == INSERT) {
      return "<h3>Insert a Unary Integrity Check</h3>";
    }
    if (insert_or_delete == DELETE) {
      return "<h3>Delete a Unary Integrity Check</h3>";
    }
    return "<h3>Illegal value for insert_or_delete: " +
        insert_or_delete + "</h3>";
  };

  /**
   * This method returns an string representation of the step type
   * for use in rendering the step in JLists and JTables.
   * @return String
   */
  public String toString() {
    return typeToString();
  };

  /**
   * This method returns a descriptive name for the type of step.
   * @return String
   */
  public static String typeToString() {
    RxToolkit.trace("UnaryIntegrityCheckStep::typeToString()");
    return "Insert or Delete a Unary Integrity Check";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    //
    // View Fields
    //
    private JRadioButton jinsert_btn = new JRadioButton("Insert");
    private JRadioButton jdelete_btn = new JRadioButton("Delete");
    private JCheckBox jnegation = new JCheckBox("Negate?");
    private IntegrityCheck[] unary_checks =
        RxToolkit.DBToolkit.getUnaryChecks(false);
    private SuperJList jic_single =
        new SuperJList(unary_checks);
    private JComboBox jtypes =
        new JComboBox(new String[] {"SOURCE", "TERMGROUP"});
    private SuperJList jvalues =
        new SuperJList(RxToolkit.getSources());
    private JTextArea jcomments = new JTextArea();

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("UnaryIntegrityCheckStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("UnaryIntegrityCheckStep.View::initialize()");
      RxToolkit.trace("jic_single: " + jic_single.getModel().getSize());
      RxToolkit.trace("jtypes: " + jtypes.getModel().getSize());
      RxToolkit.trace("jvalues: " + jvalues.getModel().getSize());
      // Create an RxStep.DataChangeListener;
      DataChangeListener dcl = new DataChangeListener();

      JPanel data_panel = new JPanel(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.insets = RxConstants.GRID_INSETS;

      // insert and delete buttons
      constraints.gridy = 0;
      constraints.gridheight = 2;
      data_panel.add(new JLabel("Select One:"), constraints);
      ButtonGroup add_or_delete = new ButtonGroup();
      add_or_delete.add(jinsert_btn);
      add_or_delete.add(jdelete_btn);
      constraints.gridheight = 1;
      jinsert_btn.addActionListener(dcl);
      jdelete_btn.addActionListener(dcl);
      data_panel.add(jinsert_btn, constraints);
      data_panel.add(jdelete_btn, constraints);

      // Add type list (default is SOURCE)
      constraints.gridy = 2;
      data_panel.add(new JLabel("Select a type:"), constraints);
      constraints.gridwidth = 2;
      jtypes.setSelectedItem("SOURCE");
      data_panel.add(jtypes, constraints);

      // if types changes, recompute the jvalues set
      // and clear ic_single.value values
      jtypes.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          has_data_changed = true;
          String command = jtypes.getSelectedItem().toString();
          if (command.equals("SOURCE")) {
            jvalues.setListData(RxToolkit.getSources());
            String[] values = new String[ic_single.length];
            for (int i = 0; i < ic_single.length; i++) {
              values[i] = ic_single[i].value;
            }
            jvalues.setSelectedValues(values,true);
          }
          else if (command.equals("TERMGROUP")) {
            jvalues.setListData(RxToolkit.getTermgroups());
            String[] values = new String[ic_single.length];
            for (int i = 0; i < ic_single.length; i++) {
              values[i] = ic_single[i].value;
            }
            jvalues.setSelectedValues(values,true);
          }
        }
      });
      jtypes.setSelectedItem("SOURCE");

      // Add values list
      constraints.gridy++;
      constraints.gridwidth = 1;
      data_panel.add(new JLabel("Select a value:"), constraints);
      constraints.gridwidth = 2;
      jvalues.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      jvalues.setVisibleRowCount(6);
      jvalues.addListSelectionListener(dcl);
      JScrollPane jvalues_scroll = new JScrollPane();
      jvalues_scroll.setViewportView(jvalues);
      data_panel.add(jvalues_scroll, constraints);

      // Add check list
      constraints.gridy++;
      constraints.gridwidth = 1;
      data_panel.add(new JLabel("Select a Check:"), constraints);
      constraints.gridwidth = 2;
      jic_single.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jic_single.setCellRenderer(
          new IntegrityCheckListCellRenderer());
      jic_single.setVisibleRowCount(
          (unary_checks.length > 6) ? 6 : unary_checks.length);
      jic_single.addListSelectionListener(dcl);
      JScrollPane jic_single_scroll = new JScrollPane();
      jic_single_scroll.setViewportView(jic_single);
      data_panel.add(jic_single_scroll, constraints);

      constraints.gridy++;
      constraints.gridwidth = 1;
      data_panel.add(new JLabel("Negation:"), constraints);
      jnegation.addActionListener(dcl);
      data_panel.add(jnegation, constraints);

      // Add comments
      constraints.gridy++;
      data_panel.add(new JLabel("Comments:"), constraints);

      constraints.gridy++;
      constraints.gridwidth = 3;
      jcomments.setRows(3);
      jcomments.getDocument().addDocumentListener(dcl);
      JScrollPane comment_scroll = new JScrollPane();
      comment_scroll.setViewportView(jcomments);
      data_panel.add(comment_scroll, constraints);

      // Add border & put into View
      add(data_panel, BorderLayout.CENTER);
    };

    //
    // Implementation of RxStep.View methods
    //

    /**
     * Set the focus
     */
    public void setFocus() {
      jtypes.requestFocus();
    }

    /**
     * This takes values from the step and displays them.
     * /use a flag to prevent the rereading of the lists from the db
     */
    public void getValues() {
      RxToolkit.trace("UnaryIntegrityCheckStep.View::getValues()");
      if (insert_or_delete == INSERT) {
        jdelete_btn.setSelected(false);
        jinsert_btn.setSelected(true);
      }
      else if (insert_or_delete == DELETE) {
        jinsert_btn.setSelected(false);
        jdelete_btn.setSelected(true);
      }
      ;

      jcomments.setText(comments);
      if (ic_single.length > 0) {
        jnegation.setSelected(ic_single[0].negation);
        jtypes.setSelectedItem(ic_single[0].type);
        jic_single.setSelectedValue(ic_single, true);
      }
      else {
        jnegation.setSelected(false);
        jtypes.setSelectedItem("SOURCE");
        jic_single.setSelectedValue(null, false);
      }
      ;

      String[] values = new String[ic_single.length];
      for (int i = 0; i < ic_single.length; i++) {
        values[i] = ic_single[i].value;
      }
      
      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("UnaryIntegrityCheckStep.View::setValues()");
      if (jinsert_btn.isSelected()) {
        insert_or_delete = INSERT;
      }
      if (jdelete_btn.isSelected()) {
        insert_or_delete = DELETE;
      }
      comments = jcomments.getText();

      Object[] values = jvalues.getSelectedValues();
      String ic_name = ( (UnaryIntegrityCheck) jic_single.getSelectedValue()).
          ic_name;
      String type = (String) jtypes.getSelectedItem();
      boolean negation = jnegation.isSelected();
      ic_single = new UnaryIntegrityCheck[values.length];
      for (int i = 0; i < values.length; i++) {
        ic_single[i] = new UnaryIntegrityCheck();
        ic_single[i].ic_name = ic_name;
        ic_single[i].type = type;
        ic_single[i].negation = negation;
        ic_single[i].value = (String) values[i];
      }
      ;
      has_data_changed = false;
    };

    /**
     * This method validates the input with respect to the underlying step
     * If jic_single has no selected value return false
     * if jvalues has no selected value return false
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("UnaryIntegrityCheckStep.View::checkUserEntry()");
      if (jic_single.getSelectedValue() == null) {
        RxToolkit.notifyUser(
            "You must select an integrity check.");
        return false;
      }

      if (jvalues.getSelectedValue() == null) {
        RxToolkit.notifyUser(
            "You must select a value");
        return false;
      }

      return true;
    };

  }

}
