/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     BinaryIntegrityCheckStep.java
 *
 * CHANGES
 * 02/12/007 BAC (1-DHBFF): properly handle setting of "values" fields
 * 11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 * 
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.BinaryIntegrityCheck;
import gov.nih.nlm.recipe.IntegrityCheck;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
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
 * in the ic_pair integrity check table.
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class BinaryIntegrityCheckStep
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
  protected BinaryIntegrityCheck ic_pair = new BinaryIntegrityCheck();
  protected String comments;

  /**
   * BinaryIntegrityCheckStep default constructor
   */
  public BinaryIntegrityCheckStep() {
    super();
    RxToolkit.trace("BinaryIntegrityCheckStep::BinaryIntegrityCheckStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("BinaryIntegrityCheckStep::resetValues()");
    insert_or_delete = INSERT;
    //ic_pair = new BinaryIntegrityCheck();
    ic_pair.type_1 = RxToolkit.DBToolkit.SOURCE;
    ic_pair.type_2 = RxToolkit.DBToolkit.SOURCE;
    ic_pair.value_1 = null;
    ic_pair.value_2 = null;
    ic_pair.negation = false;
    comments = "";
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("BinaryIntegrityCheckStep::constructView()");
    return new BinaryIntegrityCheckStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("BinaryIntegrityCheckStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("BinaryIntegrityCheckStep::toHTML()");
    StringBuffer step_text = new StringBuffer();

    // put html here
    step_text.append("              <table ALIGN=CENTER WIDTH=90% BORDER=1>\n");
    step_text.append("               <tr>\n");
    step_text.append("                <th>Name</th>\n");
    step_text.append("                <th>Type 1</th>\n");
    step_text.append("                <th>Value 1</th>\n");
    step_text.append("                <th>Type 2</th>\n");
    step_text.append("                <th>Value 2</th>\n");
    step_text.append("                <th>Negation</th>\n");
    step_text.append("               </tr><tr>\n");
    step_text.append("                <td ALIGN=CENTER>" + ic_pair.ic_name +
                     "</td>\n");
    step_text.append("                <td ALIGN=CENTER>" + ic_pair.type_1 +
                     "</td>\n");
    step_text.append("                <td ALIGN=CENTER>" + ic_pair.value_1 +
                     "</td>\n");
    step_text.append("                <td ALIGN=CENTER>" + ic_pair.type_2 +
                     "</td>\n");
    step_text.append("                <td ALIGN=CENTER>" + ic_pair.value_2 +
                     "</td>\n");
    step_text.append("                <td ALIGN=CENTER>" + ic_pair.negation +
                     "</td>\n");
    step_text.append("               </tr>\n");

    if (!comments.equals("")) {
      step_text.append("              <tr>\n");
      step_text.append("                <td><b>Comments:</b></td><td>");
      step_text.append(comments);
      step_text.append("</td></tr>\n");
    }
    step_text.append("              </table>\n");

    return step_text.toString();
  };

  /**
   * Produces shell script code for this step.
   */
  public String toShellScript() {

    StringBuffer body = new StringBuffer(500);
    body.append("#\n# Binary Integrity Check\n");
    if (comments != null && !comments.equals("")) {
      body.append("#").append(comments).append("\n");
    }
    body.append("#\n");
    body.append(
        "$ORACLE_HOME/bin/sqlplus -s $user@$db << EOF >&! /tmp/t.$$.log\n" +
        "    WHENEVER SQLERROR EXIT -1\n" +
        "    set feedback off\n\n");
    if (insert_or_delete == INSERT) {
      body.append(
          "    INSERT INTO ic_pair\n" +
          "    (ic_name, negation, type_1, value_1, type_2, value_2)\n" +
          "    VALUES\n " +
          "    ('");
      body.append(ic_pair.ic_name).append("','");
      body.append( (ic_pair.negation) ? "Y" : "N").append("','");
      body.append(ic_pair.type_1).append("','");
      body.append(ic_pair.value_1).append("','");
      body.append(ic_pair.type_2).append("','");
      body.append(ic_pair.value_2).append("');\n\n");
    }
    else {
      body.append(
          "    DELETE FROM ic_pair\n" +
          "    WHERE ic_name = '");
      body.append(ic_pair.ic_name).append("'\n" +
                                          "      AND type_1 = '");
      body.append(ic_pair.type_1).append("'\n" +
                                         "      AND value_1 = '");
      body.append(ic_pair.value_1).append("'\n" +
                                          "      AND type_2 = '");
      body.append(ic_pair.type_2).append("'\n" +
                                         "      AND value_2 = '");
      body.append(ic_pair.value_2).append("'\n" +
                                          "      AND negation = '");
      body.append( (ic_pair.negation) ? "Y" : "N").append("';\n\n");
    }
      body.append("EOF\n" +                 /** KAL added refresh caches step 02/27/06**/
                  "if ($status != 0) then\n" +
                  "    echo \"Error handling binary check\"\n" +
                  "    cat /tmp/t.$$.log\n" +
                  "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error handling binary check\"' \n" +
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
      return "<h3>Insert a Binary Integrity Check</h3>";
    }
    if (insert_or_delete == DELETE) {
      return "<h3>Delete a Binary Integrity Check</h3>";
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
    RxToolkit.trace("BinaryIntegrityCheckStep::typeToString()");
    return "Insert or Delete a Binary Integrity Check";
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
    private IntegrityCheck[] binary_checks =
        RxToolkit.DBToolkit.getBinaryChecks(false);
    private SuperJList jic_pair =
        new SuperJList(binary_checks);
    private JComboBox jtypes_1 =
        new JComboBox(new String[] {"SOURCE", "TERMGROUP"});
    private SuperJList jvalues_1 =
        new SuperJList(RxToolkit.getSources());
    private JComboBox jtypes_2 =
        new JComboBox(new String[] {"SOURCE", "TERMGROUP"});
    private SuperJList jvalues_2 =
        new SuperJList(RxToolkit.getSources());
    private JTextArea jcomments = new JTextArea();

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("BinaryIntegrityCheckStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("BinaryIntegrityCheckStep.View::initialize()");

      // create an RxStep.DataChangeListener
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

      // Add type_1 list (default is SOURCE)
      constraints.gridy = 2;
      constraints.gridwidth = 1;
      data_panel.add(new JLabel("Select a type (1): "), constraints);
      constraints.gridwidth = 2;
      jtypes_1.setSelectedItem("SOURCE");
      data_panel.add(jtypes_1, constraints);

      // if types changes, recompute the jvalues_1 set
      // and clear ic_pair.value_1 values
      jtypes_1.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          has_data_changed = true;
          String command = jtypes_1.getSelectedItem().toString();
          if (command.equals("SOURCE")) {
            jvalues_1.setListData(RxToolkit.getSources());
            jvalues_1.setSelectedValue(ic_pair.value_1, true);
          }
          else if (command.equals("TERMGROUP")) {
            jvalues_1.setListData(RxToolkit.getTermgroups());
            jvalues_1.setSelectedValue(ic_pair.value_1, true);
          }
        }
      });

      // Add values list
      constraints.gridy++;
      constraints.gridwidth = 1;
      data_panel.add(new JLabel("Select a value (1):"), constraints);
      constraints.gridwidth = 2;
      jvalues_1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jvalues_1.setVisibleRowCount(6);
      jvalues_1.addListSelectionListener(dcl);
      JScrollPane jvalues_1_scroll = new JScrollPane();
      jvalues_1_scroll.setViewportView(jvalues_1);
      data_panel.add(jvalues_1_scroll, constraints);

      // Add type_2 list (default is SOURCE)
      constraints.gridy++;
      constraints.gridwidth = 1;
      data_panel.add(new JLabel("Select a type (2): "), constraints);
      constraints.gridwidth = 2;
      jtypes_2.setSelectedItem("SOURCE");
      data_panel.add(jtypes_2, constraints);

      // if types changes, recompute the jvalues_1 set
      // and clear ic_pair.value_1 values
      jtypes_2.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          has_data_changed = true;
          String command = jtypes_2.getSelectedItem().toString();
          if (command.equals("SOURCE")) {
            jvalues_2.setListData(RxToolkit.getSources());
            jvalues_2.setSelectedValue(ic_pair.value_2, true);
          }
          else if (command.equals("TERMGROUP")) {
            jvalues_2.setListData(RxToolkit.getTermgroups());
            jvalues_2.setSelectedValue(ic_pair.value_2, true);
          }
        }
      });

      // Add values list
      constraints.gridy++;
      constraints.gridwidth = 1;
      data_panel.add(new JLabel("Select a value (2):"), constraints);
      constraints.gridwidth = 2;
      jvalues_2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jvalues_2.setVisibleRowCount(6);
      jvalues_2.addListSelectionListener(dcl);
      JScrollPane jvalues_2_scroll = new JScrollPane();
      jvalues_2_scroll.setViewportView(jvalues_2);
      data_panel.add(jvalues_2_scroll, constraints);

      // Add check list
      constraints.gridy++;
      constraints.gridwidth = 1;
      data_panel.add(new JLabel("Select a Check:"), constraints);
      constraints.gridwidth = 2;
      jic_pair.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jic_pair.setCellRenderer(
          new IntegrityCheckListCellRenderer());
      jic_pair.addListSelectionListener(dcl);
      jic_pair.setVisibleRowCount(
          (binary_checks.length > 6) ? 6 : binary_checks.length);
      JScrollPane jic_pair_scroll = new JScrollPane();
      jic_pair_scroll.setViewportView(jic_pair);

      // Register viewport and list so subcomponents can get tooltips
      data_panel.add(jic_pair_scroll, constraints);

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
      jtypes_1.requestFocus();
    }

    /**
     * This takes values from the step and displays them.
     * /use a flag to prevent the rereading of the lists from the db
     */
    public void getValues() {
      RxToolkit.trace("BinaryIntegrityCheckStep.View::getValues()");
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
      RxToolkit.trace("            ic_pair is " + ic_pair + "(" +
                        ic_pair.type_1 + "," + ic_pair.value_1 + ":" +
                        ic_pair.type_2 + "," + ic_pair.value_2 + ")");
      jnegation.setSelected(ic_pair.negation);
      jic_pair.setSelectedValue(ic_pair, true);
      jtypes_1.setSelectedItem(ic_pair.type_1);
      jtypes_2.setSelectedItem(ic_pair.type_2);

      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("BinaryIntegrityCheckStep.View::setValues()");
      if (jinsert_btn.isSelected()) {
        insert_or_delete = INSERT;
      }
      if (jdelete_btn.isSelected()) {
        insert_or_delete = DELETE;
      }
      comments = jcomments.getText();

      ic_pair.ic_name = ( (BinaryIntegrityCheck) jic_pair.getSelectedValue()).
          ic_name;
      ic_pair.negation = jnegation.isSelected();
      ic_pair.type_1 = (String) jtypes_1.getSelectedItem();
      ic_pair.value_1 = (String) jvalues_1.getSelectedValue();
      ic_pair.type_2 = (String) jtypes_2.getSelectedItem();
      ic_pair.value_2 = (String) jvalues_2.getSelectedValue();
      has_data_changed = false;
    };

    /**
     * This method validates the input with respect to the underlying step
     * If jic_pair has no selected value return false
     * if jvalues has no selected value return false
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("BinaryIntegrityCheckStep.View::checkUserEntry()");
      if (jic_pair.getSelectedValue() == null) {
        RxToolkit.notifyUser(
            "You must select an integrity check.");
        return false;
      }

      if (jvalues_1.getSelectedValue() == null) {
        RxToolkit.notifyUser(
            "You must select a first value.");
        return false;
      }

      if (jvalues_2.getSelectedValue() == null) {
        RxToolkit.notifyUser(
            "You must select a second value.");
        return false;
      }

      return true;
    };

  }

}
