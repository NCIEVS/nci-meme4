/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     TermgroupSafeReplacementStep.java
 *
 * Changes
 *   11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
import gov.nih.nlm.swing.SuperJList;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

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
 * This step is used to generate and process facts.
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class TermgroupSafeReplacementStep
    extends AbstractSafeReplacementStep {

  //
  // Constants
  //
  private final static HashMap allowed_options = new HashMap();
  static {
    allowed_options.put("INCLUDE", "Include termgroups in list");
    allowed_options.put("EXCLUDE", "Exclude termgroups in list");
    allowed_options.put("ALL", "Include All termgroups");
    allowed_options.put("EXCLUDE_LIST", "Use Exclude List");
    allowed_options.put("NORM_EXCLUDE_LIST", "Use Norm-Exclude List");
    allowed_options.put("NEW_ATOMS_ONLY", "Restrict to New Atoms Only");
  }

  //
  // Fields
  //
  protected String source_name;
  protected String old_source_name;

  protected String[] old_termgroups;
  protected HashMap old_options;
  protected String[] new_termgroups;
  protected HashMap new_options;

  /**
   * TermgroupSafeReplacementStep default constructor
   */
  public TermgroupSafeReplacementStep() {
    super();
    RxToolkit.trace(
        "TermgroupSafeReplacementStep::TermgroupSafeReplacementStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("TermgroupSafeReplacementStep::resetValues()");
    old_termgroups = new String[] {};
    old_options = new HashMap();
    old_options.put("INCLUDE", "");
    new_termgroups = new String[] {};
    new_options = new HashMap();
    new_options.put("INCLUDE", "");
    string_match = "NONE";
    code_match = "NONE";
    saui_match = "NONE";
    scui_match = "NONE";
    sdui_match = "NONE";
    tty_match = "NONE";
    change_status = true;
    sources_or_termgroups = "TERMGROUP";
    new_values = new ArrayList();
    old_values = new ArrayList();
    comments = "";
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("TermgroupSafeReplacementStep::constructView()");
    return new TermgroupSafeReplacementStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("TermgroupSafeReplacementStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("TermgroupSafeReplacementStep::toHTML()");
    StringBuffer step_text = new StringBuffer();

    int i;
    String[] keys;
    step_text.append("            <table ALIGN=CENTER WIDTH=90% BORDER=1 CELLSPACING=1 CELLPADDING=2>\n");
    step_text.append(
        "              <tr><th colspan=2>Safe Replacement Parameters</th></tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>String Match:</b></td><td>");
    step_text.append(string_match).append("</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Code Match:</b></td><td>");
    step_text.append(code_match).append("</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Source AUI Match:</b></td><td>");
    step_text.append(saui_match).append("</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Source CUI Match:</b></td><td>");
    step_text.append(scui_match).append("</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Source DUI Match:</b></td><td>");
    step_text.append(sdui_match).append("</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Term Type Match:</b></td><td>");
    step_text.append(tty_match);
    step_text.append("</td>\n              </tr>\n");

    // source termgroups
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Old Termgroups:</b></td><td>");
    if (old_termgroups.length > 0) {
      step_text.append(old_termgroups[0]);
    }
    for (i = 1; i < old_termgroups.length; i++) {
      step_text.append(",");
      step_text.append(old_termgroups[i]);
    }
    step_text.append("                </td>\n              </tr>\n");

    // source options
    if (old_options.keySet().size() > 0) {
      step_text.append("              <tr>\n");
      step_text.append(
          "                <td><b>Old Termgroup Options:</b></td><td>");
      step_text.append("                  <ul>\n");
      keys = RxToolkit.toStringArray(allowed_options.keySet().toArray());
      for (i = 0; i < keys.length; i++) {
        if (old_options.get(keys[i]) != null) {
          step_text.append("                    <li>");
          step_text.append( (String) allowed_options.get(keys[i]));
          step_text.append("</li>\n");
        }
      }
      step_text.append("                  </ul>\n");
      step_text.append("                </td>\n              </tr>\n");
    }

    // terget termgroups
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>New Termgroups:</b></td><td>");
    if (new_termgroups.length > 0) {
      step_text.append(new_termgroups[0]);
    }
    for (i = 1; i < new_termgroups.length; i++) {
      step_text.append(",");
      step_text.append(new_termgroups[i]);
    }
    step_text.append("                </td>\n              </tr>\n");

    // target options
    if (new_options.keySet().size() > 0) {
      step_text.append("              <tr>\n");
      step_text.append(
          "                <td><b>New Termgroup Options:</b></td><td>");
      step_text.append("                  <ul>\n");
      keys = RxToolkit.toStringArray(allowed_options.keySet().toArray());
      for (i = 0; i < keys.length; i++) {
        if (new_options.get(keys[i]) != null) {
          step_text.append("                    <li>");
          step_text.append( (String) allowed_options.get(keys[i]));
          step_text.append("</li>\n");
        }
      }
      step_text.append("                  </ul>\n");
      step_text.append("                </td>\n              </tr>\n");
    }

    // change status flag
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Change Status:</b></td><td>");
    step_text.append( (change_status ? "Yes" : "No"));
    step_text.append("</td>\n              </tr>\n");

    if (!comments.equals("")) {
      step_text.append("              <tr>\n");
      step_text.append("                <td><b>Comments:</b></td><td>");
      step_text.append(comments);
      step_text.append("</td></tr>\n");
    }
    step_text.append("            </table>\n");

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
    body.append("echo \"    Perform termgroup safe replacement (");
    body.append(string_match).append(",");
    body.append(code_match).append(",");
    body.append(saui_match).append(",");
    body.append(scui_match).append(",");
    body.append(sdui_match).append(",");
    body.append(tty_match).append(",");
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
        "    CREATE TABLE t1 (termgroup VARCHAR2(60));\n" +
        "    CREATE TABLE t2 (termgroup VARCHAR2(60));\n");
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
        "        old_termgroup_table => 't2', new_termgroup_table => 't1', -\n" +
        "        old_source_table => '', new_source_table => '', -\n" +
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
    return "Termgroup Safe Replacement Step ";
  };

  /**
   * This method returns a descriptive name for the type of step.
   * @return String
   */
  public static String typeToString() {
    RxToolkit.trace("TermgroupSafeReplacementStep::typeToString()");
    return "Termgroup Safe Replacement Step";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    //
    // View Fields
    //
    private SuperJList jold_termgroups = new SuperJList(
        RxToolkit.getTermgroups());
    private SuperJList jnew_termgroups = new SuperJList(
        RxToolkit.getTermgroups());

    // options
    private JRadioButton jsource_include = new JRadioButton("Include");
    private JRadioButton jsource_exclude = new JRadioButton("Exclude");
    private JRadioButton jsource_all = new JRadioButton("All");
    private JRadioButton jtarget_include = new JRadioButton("Include");
    private JRadioButton jtarget_exclude = new JRadioButton("Exclude");
    private JRadioButton jtarget_all = new JRadioButton("All");

    // standard merge options
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
    private JComboBox jtty_match = new JComboBox(new String[] {"EXACT", "NONE",
                                                 "NOT"});
    private JCheckBox jchange_status = new JCheckBox("Change Status");

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("TermgroupSafeReplacementStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("TermgroupSafeReplacementStep.View::initialize()");

      setLayout(new BorderLayout(5, 5));

      // Create an RxStep.DataChangeListener.
      DataChangeListener dcl = new DataChangeListener();

      // Create panel for termgroups
      JPanel termgroups_panel = new JPanel(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.insets = RxConstants.GRID_INSETS;

      // source termgroups and options
      constraints.gridy = 0;
      constraints.gridwidth = 2;
      termgroups_panel.add(new JLabel("Select new termgroups"), constraints);
      termgroups_panel.add(new JLabel("Select old termgroups"), constraints);

      // add target termgroups
      constraints.gridy++;
      constraints.gridwidth = 1;
      constraints.insets = RxConstants.GRID_INSETS;
      jnew_termgroups.setVisibleRowCount(6);
      jnew_termgroups.setSelectionMode(
          ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      jnew_termgroups.addListSelectionListener(dcl);
      JScrollPane jscroll = new JScrollPane();
      jscroll.setViewportView(jnew_termgroups);
      termgroups_panel.add(jscroll, constraints);

      // add toggles to target termgroups
      constraints.insets = RxConstants.EMPTY_INSETS;
      ButtonGroup bg = new ButtonGroup();
      bg.add(jtarget_include);
      bg.add(jtarget_exclude);
      bg.add(jtarget_all);

      JPanel tmp_panel = new JPanel(new BorderLayout(0, 0));

      jtarget_include.addActionListener(dcl);
      tmp_panel.add(jtarget_include, BorderLayout.NORTH);

      jtarget_exclude.addActionListener(dcl);
      tmp_panel.add(jtarget_exclude, BorderLayout.CENTER);

      jtarget_all.addActionListener(dcl);
      jtarget_all.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          jnew_termgroups.selectAll();
        }
      });
      tmp_panel.add(jtarget_all, BorderLayout.SOUTH);
      termgroups_panel.add(tmp_panel, constraints);

      jold_termgroups.setVisibleRowCount(6);
      jold_termgroups.setSelectionMode(ListSelectionModel.
                                       MULTIPLE_INTERVAL_SELECTION);
      jold_termgroups.addListSelectionListener(dcl);
      jscroll = new JScrollPane();
      jscroll.setViewportView(jold_termgroups);

      // add source termgroups
      termgroups_panel.add(jscroll, constraints);

      // add toggles to source termgroups
      constraints.insets = RxConstants.EMPTY_INSETS;
      bg = new ButtonGroup();
      bg.add(jsource_include);
      bg.add(jsource_exclude);
      bg.add(jsource_all);

      tmp_panel = new JPanel(new BorderLayout(0, 0));

      jsource_include.addActionListener(dcl);
      tmp_panel.add(jsource_include, BorderLayout.NORTH);

      jsource_exclude.addActionListener(dcl);
      tmp_panel.add(jsource_exclude, BorderLayout.CENTER);

      jsource_all.addActionListener(dcl);
      jsource_all.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          jold_termgroups.selectAll();
        }
      });
      tmp_panel.add(jsource_all, BorderLayout.SOUTH);
      termgroups_panel.add(tmp_panel, constraints);

      add(termgroups_panel, BorderLayout.NORTH);

      // add merging options.
      JPanel options_panel = new JPanel(new GridBagLayout());
      constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.insets = RxConstants.GRID_INSETS;
      constraints.anchor = GridBagConstraints.NORTH;
      constraints.gridwidth = 1;

      // Merge Set combobox
      constraints.gridy = 0;
      options_panel.add(new JLabel("Source Name:"), constraints);
      source_name = (String) TermgroupSafeReplacementStep.this.parent.getParent().
          getAttribute("source_name");
      options_panel.add(new JLabel(source_name), constraints);
      constraints.gridy++;
      options_panel.add(new JLabel("Old Source Name:"), constraints);
      old_source_name = (String) TermgroupSafeReplacementStep.this.parent.
          getParent().getAttribute("old_source_name");
      options_panel.add(new JLabel(old_source_name), constraints);

      // string and code match
      constraints.gridy++;
      options_panel.add(new JLabel("String Match:"), constraints);
      jstring_match.addActionListener(dcl);
      options_panel.add(jstring_match, constraints);

      constraints.gridy++;
      options_panel.add(new JLabel("Code Match:"), constraints);
      jcode_match.addActionListener(dcl);
      options_panel.add(jcode_match, constraints);

      constraints.gridy++;
      options_panel.add(new JLabel("Source AUI Match:"), constraints);
      jsaui_match.addActionListener(dcl);
      options_panel.add(jsaui_match, constraints);

      constraints.gridy++;
      options_panel.add(new JLabel("Source CUI Match:"), constraints);
      jscui_match.addActionListener(dcl);
      options_panel.add(jscui_match, constraints);

      constraints.gridy++;
      options_panel.add(new JLabel("Source DUI Match:"), constraints);
      jsdui_match.addActionListener(dcl);
      options_panel.add(jsdui_match, constraints);

      constraints.gridy++;
      options_panel.add(new JLabel("Term Type Match:"), constraints);
      jtty_match.addActionListener(dcl);
      options_panel.add(jtty_match, constraints);

      // Add options change_status, term type match
      constraints.gridy++;
      constraints.gridheight = 3;
      options_panel.add(new JLabel("Options:"), constraints);
      constraints.insets = RxConstants.EMPTY_INSETS;
      constraints.gridheight = 1;
      constraints.gridx = 1;
      jchange_status.addActionListener(dcl);
      options_panel.add(jchange_status, constraints);

      constraints.gridy++;
      constraints.gridx = 0;
      options_panel.add(new JLabel("Comments:"), constraints);
      constraints.gridy++;
      constraints.gridwidth = 3;
      jcomments.setRows(3);
      jcomments.getDocument().addDocumentListener(dcl);
      JScrollPane comment_scroll = new JScrollPane();
      comment_scroll.setViewportView(jcomments);
      options_panel.add(comment_scroll, constraints);

      // Add options panel
      add(options_panel, BorderLayout.SOUTH);
    };

    //
    // Implementation of RxStep.View methods
    //

    /**
     * Set the focus
     */
    public void setFocus() {
      jold_termgroups.requestFocus();
    }

    /**
     * This takes values from the step and displays them.
     */
    public void getValues() {
      RxToolkit.trace("TermgroupSafeReplacementStep.View::getValues()");
      if (old_options.get("ALL") != null) {
        jold_termgroups.selectAll();
      }
      else {
        jold_termgroups.setSelectedValues(old_termgroups, true);
      }
      ;
      if (new_options.get("ALL") != null) {
        jnew_termgroups.selectAll();
      }
      else {
        jnew_termgroups.setSelectedValues(new_termgroups, true);
      }
      ;

      // source options
      jsource_include.setSelected(
          old_options.get("INCLUDE") != null);
      jsource_exclude.setSelected(
          old_options.get("EXCLUDE") != null);
      jsource_all.setSelected(
          old_options.get("ALL") != null);

      // target options
      jtarget_include.setSelected(
          new_options.get("INCLUDE") != null);
      jtarget_exclude.setSelected(
          new_options.get("EXCLUDE") != null);
      jtarget_all.setSelected(
          new_options.get("ALL") != null);

      // standard merge options
      jcomments.setText(comments);
      jstring_match.setSelectedItem(string_match);
      jcode_match.setSelectedItem(code_match);
      jsaui_match.setSelectedItem(saui_match);
      jscui_match.setSelectedItem(scui_match);
      jsdui_match.setSelectedItem(sdui_match);
      jtty_match.setSelectedItem(tty_match);
      jchange_status.setSelected(change_status);

      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("TermgroupSafeReplacementStep.View::setValues()");
      if (jsource_all.isSelected()) {
        jold_termgroups.selectAll();
      }
      old_termgroups = RxToolkit.toStringArray(
          jold_termgroups.getSelectedValues());
      if (jtarget_all.isSelected()) {
        jnew_termgroups.selectAll();
      }
      new_termgroups = RxToolkit.toStringArray(
          jnew_termgroups.getSelectedValues());

      // source options
      if (jsource_include.isSelected()) {
        old_options.put("INCLUDE", "");
      }
      else {
        old_options.remove("INCLUDE");

      }
      if (jsource_exclude.isSelected()) {
        old_options.put("EXCLUDE", "");
      }
      else {
        old_options.remove("EXCLUDE");

      }
      if (jsource_all.isSelected()) {
        old_options.put("ALL", "");
      }
      else {
        old_options.remove("ALL");

        // target options
      }
      if (jtarget_include.isSelected()) {
        new_options.put("INCLUDE", "");
      }
      else {
        new_options.remove("INCLUDE");

      }
      if (jtarget_exclude.isSelected()) {
        new_options.put("EXCLUDE", "");
      }
      else {
        new_options.remove("EXCLUDE");

      }
      if (jtarget_all.isSelected()) {
        new_options.put("ALL", "");
      }
      else {
        new_options.remove("ALL");

        // standard merge options
      }
      comments = jcomments.getText();
      string_match = (String) jstring_match.getSelectedItem();
      code_match = (String) jcode_match.getSelectedItem();
      saui_match = (String) jsaui_match.getSelectedItem();
      scui_match = (String) jscui_match.getSelectedItem();
      sdui_match = (String) jsdui_match.getSelectedItem();
      tty_match = (String) jtty_match.getSelectedItem();
      change_status = jchange_status.isSelected();
      old_values.clear();
      for (int i = 0; i < old_termgroups.length; i++) {
        old_values.add(i, old_termgroups[i]);
      }
      ;
      new_values.clear();
      for (int i = 0; i < new_termgroups.length; i++) {
        new_values.add(i, new_termgroups[i]);
      }
      ;
      sources_or_termgroups = "TERMGROUP";

      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("TermgroupSafeReplacementStep.View::checkUserEntry()");

      if (jold_termgroups.getSelectedValue() == null ||
          jnew_termgroups.getSelectedValue() == null) {
        RxToolkit.notifyUser(
            "You must select new AND old termgroups.");
        return false;
      }
      ;

      if (!jsource_include.isSelected() &&
          !jsource_exclude.isSelected() &&
          !jsource_all.isSelected()) {
        RxToolkit.notifyUser(
            "For the source termgroups you must select\n" +
            "include, exclude, or all.");
        return false;
      }

      if (!jtarget_include.isSelected() &&
          !jtarget_exclude.isSelected() &&
          !jtarget_all.isSelected()) {
        RxToolkit.notifyUser(
            "For the target termgroups you must select\n" +
            "include, exclude, or all.");
        return false;
      }

      return true;
    };

  }; // end TermgroupSafeReplacementStep.View

}
