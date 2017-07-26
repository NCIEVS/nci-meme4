/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     SourceSafeReplacementStep.java
 *
 * CHANGES
 * 11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * This step is used to generate and process facts.
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class SourceSafeReplacementStep
    extends AbstractSafeReplacementStep {

  //
  // Constants
  //

  //
  // Fields
  //
  protected String source_name;
  protected String old_source_name;

  /** These fields are contained in AbstractSafeReplacementStep
   protected String string_match;
   protected String code_match;
   protected boolean change_status;
   protected ArrayList old_values;
   protected ArrayList new_values;
   protected boolean tty_match;
   protected String saui_match;
   protected String scui_match;
   protected String sdui_match;
   protected String sources_or_termgroups;
   protected String comments;
   **/

  /**
   * SourceSafeReplacementStep default constructor
   */
  public SourceSafeReplacementStep() {
    super();
    RxToolkit.trace("SourceSafeReplacementStep::SourceSafeReplacementStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("SourceSafeReplacementStep::resetValues()");
    new_values = new ArrayList();
    old_values = new ArrayList();
    string_match = "NONE";
    code_match = "NONE";
    saui_match = "NONE";
    scui_match = "NONE";
    sdui_match = "NONE";
    tty_match = "NONE";
    comments = "";
    sources_or_termgroups = "SOURCE";
    change_status = true;
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("SourceSafeReplacementStep::constructView()");
    return new SourceSafeReplacementStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("SourceSafeReplacementStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("SourceSafeReplacementStep::toHTML()");
    StringBuffer step_text = new StringBuffer();
    step_text.append("            <p>source name : " + source_name + "<br>\n");
    step_text.append("            old source name : " + old_source_name +
                     "<br>\n");
    step_text.append("            string match : " + string_match + "<br>\n");
    step_text.append("            code match : " + code_match + "<br>\n");
    step_text.append("            source aui match : " + saui_match + "<br>\n");
    step_text.append("            source cui match : " + scui_match + "<br>\n");
    step_text.append("            source dui match : " + sdui_match + "<br>\n");
    step_text.append("            term type match : " + tty_match + "<br>\n");
    step_text.append("            <ul>\n");
    if (change_status) {
      step_text.append("            <li>Change Status.\n");
    }
    step_text.append("            </ul>\n");

    if (!comments.equals("")) {
      step_text.append("          Comments: " + comments + "\n");
    }

    return step_text.toString();
  };

  /**
   * Produce shell script code for this step.
   */
  public String toShellScript() {

    StringBuffer body = new StringBuffer(500);
    body.append("#\n# Source Safe Replacement Step\n");
    if (comments != null && !comments.equals("")) {
      body.append("# ").append(comments).append("\n");
    }
    body.append("#\n");
    body.append("echo \"    Perform source safe replacement (");
    body.append(string_match).append(",");
    body.append(code_match).append(",");
    body.append(tty_match).append(",");
    body.append(saui_match).append(",");
    body.append(scui_match).append(",");
    body.append(sdui_match);
    body.append(") ...`/bin/date`\"\n\n");
    body.append(
        "$ORACLE_HOME/bin/sqlplus -s $user@$db << EOF >&! /tmp/t.$$.log\n" +
        "    WHENEVER SQLERROR EXIT -1\n" +
        "    set feedback off\n" +
        "    ALTER SESSION SET sort_area_size=200000000;\n" +
        "    ALTER SESSION SET hash_area_size=200000000;\n" +
        "    set serveroutput on size 100000\n\n" +
        "    exec MEME_UTILITY.drop_it('table','t1');\n" +
        "    exec MEME_UTILITY.drop_it('table','t2');\n" +
        "    CREATE TABLE t1 (source VARCHAR2(40));\n" +
        "    CREATE TABLE t2 (source VARCHAR2(40));\n");
    String[] nv = (String[]) new_values.toArray(new String[0]);
    for (int i = 0; i < nv.length; i++) {
      body.append("    INSERT INTO t1 VALUES ('").append(nv[i]).append("');\n");
    }
    String[] ov = (String[]) old_values.toArray(new String[0]);
    for (int i = 0; i < ov.length; i++) {
      body.append("    INSERT INTO t2 VALUES ('").append(ov[i]).append("');\n");

    }
    body.append("\n" +
                "    exec meme_source_processing.safe_replacement( -\n" +
                "        string_parameter => '");
    body.append(string_match);
    body.append("', code_parameter => '");
    body.append(code_match);
    body.append("', -\n" +
                "        source_aui_parameter => '");
    body.append(saui_match);
    body.append("', source_cui_parameter => '");
    body.append(scui_match);
    body.append("', -\n" +
                "        source_dui_parameter => '");
    body.append(sdui_match);
    body.append("', tty_parameter => '");
    body.append(tty_match);
    body.append("', -\n" +
        "        old_source_table => 't2', new_source_table => 't1', -\n" +
        "        old_termgroup_table => '', new_termgroup_table => '', -\n" +
        "        change_status => '");
    body.append( (change_status) ? "Y" : "N");
    body.append("', source => '$new_source', authority => '$authority', -\n" +
                "work_id => $work_id);\n\n");
    body.append("    exec meme_source_processing.delete_demotions (-\n" +
                "       source => '$new_source', -\n" +
                "	authority => '$authority', -\n" +
                "	work_id => $work_id);\n\n");
    body.append("EOF\n" +
                "if ($status != 0) then\n" +
                "    echo \"Error handling safe replacement\"\n" +
                "    cat /tmp/t.$$.log\n" +
                "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error handling safe replacement\"' \n" +
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
    return "<h3>" + typeToString() + "</h3>";
  };

  /**
   * This method returns an string representation of the step type
   * for use in rendering the step in JLists and JTables.
   * @return String
   */
  public String toString() {
    return "Source Safe Replacement Step";
  };

  /**
   * This method returns a descriptive name for the type of step.
   * @return String
   */
  public static String typeToString() {
    RxToolkit.trace("SourceSafeReplacementStep::typeToString()");
    return "Source Safe Replacement";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    //
    // View Fields
    //

    private JComboBox jsource_name = new JComboBox();
    private JComboBox jold_source_name = new JComboBox();
    private JComboBox jtty_match = new JComboBox(new String[] {"EXACT", "NONE",
                                                 "NOT"});

    private JCheckBox jchange_status = new JCheckBox("Change Status");
    private JTextArea jcomments = new JTextArea();
    private JComboBox jstring_match =
        new JComboBox(new String[] {"EXACT", "BOTH", "NONE", "NOT"});
    private JComboBox jcode_match =
        new JComboBox(RxToolkit.DBToolkit.getCodes(
        RxToolkit.DBToolkit.CODE_MATCH));
    private JComboBox jsaui_match =
        new JComboBox(new String[] {"EXACT", "NONE", "NOT"});
    private JComboBox jscui_match =
        new JComboBox(new String[] {"EXACT", "NONE", "NOT"});
    private JComboBox jsdui_match =
        new JComboBox(new String[] {"EXACT", "NONE", "NOT"});

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("SourceSafeReplacementStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("SourceSafeReplacementStep.View::initialize()");

      DataChangeListener dcl = new DataChangeListener();

      setLayout(new BorderLayout());
      JPanel data_panel = new JPanel(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.insets = RxConstants.GRID_INSETS;

      constraints.gridy = 0;
      data_panel.add(new JLabel("Source Name:"), constraints);
      String[] names = RxToolkit.getSources();
      for (int i = 0; i < names.length; i++) {
        jsource_name.addItem(names[i]);
      }
      ;
      source_name = (String) SourceSafeReplacementStep.this.parent.getParent().
          getAttribute("source_name");
      jsource_name.setSelectedItem(source_name);
      data_panel.add(jsource_name, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("Old Source Name:"), constraints);
      for (int i = 0; i < names.length; i++) {
        jold_source_name.addItem(names[i]);
      }
      ;
      old_source_name = (String) SourceSafeReplacementStep.this.parent.
          getParent().getAttribute("old_source_name");
      jold_source_name.setSelectedItem(old_source_name);
      data_panel.add(jold_source_name, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("String Match"), constraints);
      jstring_match.addActionListener(dcl);
      data_panel.add(jstring_match, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("Code Match"), constraints);
      jcode_match.addActionListener(dcl);
      data_panel.add(jcode_match, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("Source AUI Match"), constraints);
      jsaui_match.addActionListener(dcl);
      data_panel.add(jsaui_match, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("Source CUI Match"), constraints);
      jscui_match.addActionListener(dcl);
      data_panel.add(jscui_match, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("Source DUI Match"), constraints);
      jsdui_match.addActionListener(dcl);
      data_panel.add(jsdui_match, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("Term Type Match"), constraints);
      jtty_match.addActionListener(dcl);
      data_panel.add(jtty_match, constraints);

      constraints.gridwidth = 2;
      constraints.gridy++;
      jchange_status.addActionListener(dcl);
      data_panel.add(jchange_status, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("Comments:"), constraints);

      constraints.gridy++;
      jcomments.setRows(3);
      jcomments.getDocument().addDocumentListener(dcl);
      JScrollPane comment_scroll = new JScrollPane();
      comment_scroll.setViewportView(jcomments);
      data_panel.add(comment_scroll, constraints);

      // Add the panel
      add(data_panel, BorderLayout.CENTER);

    };

    //
    // Implementation of RxStep.View methods
    //

    /**
     * Set the focus
     */
    public void setFocus() {
      jstring_match.requestFocus();
    }

    /**
     * This takes values from the step and displays them.
     */
    public void getValues() {
      RxToolkit.trace("SourceSafeReplacementStep.View::getValues()");

      jsource_name.setSelectedItem(source_name);
      jold_source_name.setSelectedItem(old_source_name);
      jcomments.setText(comments);
      jstring_match.setSelectedItem(string_match);
      jcode_match.setSelectedItem(code_match);
      jsaui_match.setSelectedItem(saui_match);
      jscui_match.setSelectedItem(scui_match);
      jsdui_match.setSelectedItem(sdui_match);
      jchange_status.setSelected(change_status);
      jtty_match.setSelectedItem(tty_match);

      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("SourceSafeReplacementStep.View::setValues()");

      source_name = (String) jsource_name.getSelectedItem();
      old_source_name = (String) jold_source_name.getSelectedItem();
      comments = jcomments.getText();
      string_match = (String) jstring_match.getSelectedItem();
      code_match = (String) jcode_match.getSelectedItem();
      saui_match = (String) jsaui_match.getSelectedItem();
      scui_match = (String) jscui_match.getSelectedItem();
      sdui_match = (String) jsdui_match.getSelectedItem();
      change_status = jchange_status.isSelected();
      tty_match = (String) jtty_match.getSelectedItem();

      new_values.clear();
      new_values.add(source_name);
      old_values.clear();
      old_values.add(old_source_name);
      sources_or_termgroups = "SOURCE";

      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("SourceSafeReplacementStep.View::checkUserEntry()");

      return true;
    };

  }; // end SourceSafeReplacementStep.View

}
