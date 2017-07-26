/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     GeneratedRelationshipMatchStep.java
 *
 * CHANGES
 * 11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.FactFilter;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
import gov.nih.nlm.swing.FactFilterListCellRenderer;
import gov.nih.nlm.swing.FactFilterPopupMenu;
import gov.nih.nlm.swing.SuperJList;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

public class GeneratedRelationshipMatchStep
    extends RxStep {

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
  protected String[] source_termgroups;
  protected HashMap source_options;
  protected String[] target_termgroups;
  protected HashMap target_options;

  protected String relationship_type;
  protected boolean exclude_safe_replacement;
  protected String string_match;
  protected String code_match;

  protected ArrayList filters;
  protected String comments;

  protected String tty_match = "N";
  protected String match_table = "classes";
  protected String merge_set;
  protected boolean demote;
  protected boolean change_status;
  protected String tid;

  /**
   * GeneratedRelationshipMatchStep default constructor
   */
  public GeneratedRelationshipMatchStep() {
    super();
    RxToolkit.trace(
        "GeneratedRelationshipMatchStep::GeneratedRelationshipMatchStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("GeneratedRelationshipMatchStep::resetValues()");
    source_termgroups = new String[] {};
    source_options = new HashMap();
    source_options.put("INCLUDE", "");
    target_termgroups = new String[] {};
    target_options = new HashMap();
    target_options.put("INCLUDE", "");
    string_match = "BOTH";
    code_match = "NONE";
    relationship_type = RxConstants.RT_DEMOTION;
    exclude_safe_replacement = false;
    filters = new ArrayList();

    comments = "";
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("GeneratedRelationshipMatchStep::constructView()");
    return new GeneratedRelationshipMatchStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("GeneratedRelationshipMatchStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("GeneratedRelationshipMatchStep::toHTML()");
    StringBuffer step_text = new StringBuffer();

    int i;
    String[] keys;
    step_text.append("            <table ALIGN=CENTER WIDTH=90% BORDER=1 CELLSPACING=1 CELLPADDING=2>\n");
    step_text.append("              <tr><th colspan=2>Insert Relationships from Matches (parameters)</th></tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>String Match:</b></td><td>");
    step_text.append(string_match + "</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Code Match:</b></td><td>");
    step_text.append(code_match + "</td>\n              </tr>\n");

    // source termgroups
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Source Termgroups:</b></td><td>");
    if (source_termgroups.length > 0) {
      step_text.append(source_termgroups[0]);
    }
    for (i = 1; i < source_termgroups.length; i++) {
      step_text.append(",");
      step_text.append(source_termgroups[i]);
    }
    step_text.append("                </td>\n              </tr>\n");

    // source options
    if (source_options.keySet().size() > 0) {
      step_text.append("              <tr>\n");
      step_text.append("                <td><b>Source Options:</b></td><td>");
      step_text.append("                  <ul>\n");
      keys = RxToolkit.toStringArray(allowed_options.keySet().toArray());
      for (i = 0; i < keys.length; i++) {
        if (source_options.get(keys[i]) != null) {
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
    step_text.append("                <td><b>Target Termgroups:</b></td><td>");
    if (target_termgroups.length > 0) {
      step_text.append(target_termgroups[0]);
    }
    for (i = 1; i < target_termgroups.length; i++) {
      step_text.append(",");
      step_text.append(target_termgroups[i]);
    }
    step_text.append("                </td>\n              </tr>\n");

    // target options
    if (target_options.keySet().size() > 0) {
      step_text.append("              <tr>\n");
      step_text.append("                <td><b>Target Options:</b></td><td>");
      step_text.append("                  <ul>\n");
      keys = RxToolkit.toStringArray(allowed_options.keySet().toArray());
      for (i = 0; i < keys.length; i++) {
        if (target_options.get(keys[i]) != null) {
          step_text.append("                    <li>");
          step_text.append( (String) allowed_options.get(keys[i]));
          step_text.append("</li>\n");
        }
      }
      step_text.append("                  </ul>\n");
      step_text.append("                </td>\n              </tr>\n");
    }

    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Relationship Type:</b></td><td>");
    step_text.append(relationship_type);
    step_text.append("</td>\n              </tr>\n");

    if ( (filters != null && filters.size() > 0) || exclude_safe_replacement) {
      step_text.append("              <tr>\n");
      step_text.append("                <td><b>Filters:</b></td><td>");
      step_text.append("                  <ol>\n");

      if (exclude_safe_replacement) {
        step_text.append(
            "                 <li>Exclude safe replacement atoms from source.</li>\n");
      }

      Iterator iter = filters.iterator();
      while (iter.hasNext()) {
        step_text.append("                    <li>");
        step_text.append( ( (FactFilter) iter.next()).longDescription());
        step_text.append("</li>\n");
      }
      step_text.append("                  </ol>\n");
      step_text.append("                </td>\n");
      step_text.append("              </tr>\n");

    }

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
   * This method generates code for a shell script
   * to perform the post-insert merge operation
   */
  public String toShellScript() {

    StringBuffer body = new StringBuffer(1000);

    body.append(toShellScriptHelper());

    body.append("\n" +
                "EOF\n" +
                "if ($status != 0) then\n" +
                "    echo \"Error running insert set\"\n" +
                "    cat /tmp/t.$$.log\n" +
                "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error running insert set\"' \n" +
            "    exit 1\n" +
                "endif\n" +
                "\n" +
                "#\n# Insert relationships\n#\n" +
        "$MEME_HOME/bin/insert.pl -w $work_id -host=$host -port=$port -rels t1 $db ");
    if (!relationship_type.equals(RxConstants.RT_DEMOTION)) {
      body.append("PIR-");
    }
    body.append("$new_source >&! insert.");
    body.append(relationship_type);
    body.append(".log\n" +
                "if ($status != 0) then\n" +
                "    echo \"Error inserting relationships\"\n" +
                "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error inserting relationships\"' \n" +
            "    exit 1\n" +
                "endif\n" +
                "\n");
    return body.toString();
  }

  /**
   * helper!
   */
  private String toShellScriptHelper() {
    System.out.println("source options:" + source_options.keySet());
    System.out.println("target options:" + target_options.keySet());
    StringBuffer body = new StringBuffer(1000);
    body.append("#\n# Generated Relationships From Matches\n");
    if (comments != null && !comments.equals("")) {
      body.append("# ");
      body.append(comments);
    }
    body.append("\n#\n");
    body.append(
        "echo \"    Generate relationships from matches ...`/bin/date`\"\n\n");
    body.append(
        "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log\n" +
        "    set serveroutput on size 100000\n" +
        "    set feedback off\n" +
        "    ALTER SESSION SET sort_area_size=200000000;\n" +
        "    ALTER SESSION SET hash_area_size=200000000;\n" +
        "    WHENEVER SQLERROR EXIT -1\n" +
        " \n" +
        "    exec MEME_UTILITY.drop_it('table','t1');\n" +
        "    exec MEME_UTILITY.drop_it('table','t2');\n" +
        "    CREATE TABLE t1 (termgroup VARCHAR2(60) NOT NULL);\n" +
        "    CREATE TABLE t2 (termgroup VARCHAR2(60) NOT NULL);\n");
    // add source termgroups
    for (int i = 0; i < source_termgroups.length; i++) {
      body.append("    insert into t1 values ('");
      body.append(source_termgroups[i]);
      body.append("');\n");
    }
    if (source_options.containsKey("EXCLUDE")) {
      body.append("\n" +
                  "    -- Exclude the t2 list\n" +
                  "    exec MEME_UTILITY.drop_it('table','t3');\n" +
                  "    CREATE TABLE t3 AS SELECT termgroup \n" +
                  "    FROM termgroup_rank MINUS SELECT * FROM t1;\n" +
                  "    DELETE FROM t1; \n" +
                  "    INSERT INTO t1 SELECT * FROM t3;\n" +
                  "    DROP TABLE t3;\n\n");
    }

    // add target termgroups
    for (int i = 0; i < target_termgroups.length; i++) {
      body.append("    insert into t2 values ('");
      body.append(target_termgroups[i]);
      body.append("');\n");
    }
    if (target_options.containsKey("EXCLUDE")) {
      body.append("    exec MEME_UTILITY.drop_it('table','t3');\n" +
                  "    CREATE TABLE t3 AS SELECT termgroup \n" +
                  "    FROM termgroup_rank MINUS SELECT * FROM t2;\n\n" +
                  "    DELETE FROM t2; \n\n" +
                  "    INSERT INTO t2 SELECT * FROM t3;\n\n" +
                  "    DROP TABLE t3;\n\n");
    }
    body.append(
        "\n" +
        "    exec MEME_SOURCE_PROCESSING.generate_facts ( -\n" +
        "	termgroup_table_1 => 't1', -\n" +
        "	termgroup_table_2 => 't2', -\n" +
        "	merge_set => '");
    body.append(merge_set);
    body.append("', -\n" +
                "	string_parameter => '");
    body.append(string_match);
    body.append("', -\n" +
                "	code_parameter => '");
    body.append(code_match);
    body.append("', -\n" +
                "	tty_parameter => '");
    body.append(tty_match);
    body.append("', -\n" +
                "	table_name => '");
    body.append(match_table);
    body.append("', -\n" +
                "	source => '$new_source', -\n" +
                "	authority => '$authority', -\n" +
                "	work_id => $work_id );\n" +
                "\n");
    // Deal with SAFE_REPLACEMENT, EXCLUDE_LIST, NORM_EXCLUDE_LIST
    if (exclude_safe_replacement) {
      body.append(
          "    -- Exclude safe replacement\n" +
          "    exec MEME_SOURCE_PROCESSING.filter_facts ( -\n" +
          "       type_1 => 'SAFE_REPLACEMENT', -\n" +
          "       arg_1 => '', not_1 => '', type_2 => '', arg_2 => '', -\n" +
          "       not_2 => '', merge_set => '");
      body.append(merge_set);
      body.append(
          "', -\n" +
          "       source => '$new_source', authority => '$authority', -\n" +
          "       work_id => $work_id );\n\n");
    }

    if (source_options.containsKey("EXCLUDE_LIST")) {
      body.append(
          "    -- Use exclude list\n" +
          "    exec MEME_SOURCE_PROCESSING.filter_facts ( -\n" +
          "       type_1 => 'EXCLUDE_LIST', -\n" +
          "       arg_1 => '', not_1 => '', type_2 => '', arg_2 => '', -\n" +
          "       not_2 => '', merge_set => '");
      body.append(merge_set);
      body.append(
          "', -\n" +
          "       source => '$new_source', authority => '$authority', -\n" +
          "       work_id => $work_id );\n\n");

    }

    if (source_options.containsKey("NORM_EXCLUDE_LIST")) {
      body.append(
          "    -- Use norm exclude list\n" +
          "    exec MEME_SOURCE_PROCESSING.filter_facts ( -\n" +
          "       type_1 => 'NORM_EXCLUDE_LIST', -\n" +
          "       arg_1 => '', not_1 => '', type_2 => '', arg_2 => '', -\n" +
          "       not_2 => '', merge_set => '");
      body.append(merge_set);
      body.append(
          "', -\n" +
          "       source => '$new_source', authority => '$authority', -\n" +
          "       work_id => $work_id );\n\n");
    }

    if (source_options.containsKey("NEW_ATOMS_ONLY")) {
      body.append(
          "    -- Restrict to new atoms only\n" +
          "    exec MEME_SOURCE_PROCESSING.filter_facts ( -\n" +
          "       type_1 => 'NEW_ATOMS', -\n" +
          "       arg_1 => '', not_1 => '', type_2 => '', arg_2 => '', -\n" +
          "       not_2 => '', merge_set => '");
      body.append(merge_set);
      body.append(
          "', -\n" +
          "       source => '$new_source', authority => '$authority', -\n" +
          "       work_id => $work_id );\n\n");
    }
    if (target_options.containsKey("EXCLUDE_LIST")) {
      body.append(
          "    -- Use exclude list\n" +
          "    exec MEME_SOURCE_PROCESSING.filter_facts ( -\n" +
          "       type_2 => 'EXCLUDE_LIST', -\n" +
          "       arg_1 => '', not_1 => '', type_1 => '', arg_2 => '', -\n" +
          "       not_2 => '', merge_set => '");
      body.append(merge_set);
      body.append(
          "', -\n" +
          "       source => '$new_source', authority => '$authority', -\n" +
          "       work_id => $work_id );\n\n");

    }

    if (target_options.containsKey("NORM_EXCLUDE_LIST")) {
      body.append(
          "    -- Use norm exclude list\n" +
          "    exec MEME_SOURCE_PROCESSING.filter_facts ( -\n" +
          "       type_2 => 'NORM_EXCLUDE_LIST', -\n" +
          "       arg_1 => '', not_1 => '', type_1 => '', arg_2 => '', -\n" +
          "       not_2 => '', merge_set => '");
      body.append(merge_set);
      body.append(
          "', -\n" +
          "       source => '$new_source', authority => '$authority', -\n" +
          "       work_id => $work_id );\n\n");
    }

    // Handle ad-hoc filters
    Iterator iter = filters.iterator();
    while (iter.hasNext()) {
      FactFilter filter = (FactFilter) iter.next();
      body.append(filter.toShellScript(merge_set));
      body.append("\n\n");
    }

    body.append("    exec MEME_UTILITY.drop_it('table','t1');\n" +
                "    CREATE TABLE t1 AS\n" +
                "    SELECT concept_id_1,concept_id_2,atom_id_1,atom_id_2,\n" +
                "         relationship_name,relationship_attribute,\n" +
                "         source, source_of_label,status,generated_status,\n" +
                "         relationship_level,released,tobereleased,\n" +
                "         relationship_id, suppressible,\n" +
                "         sg_id_1, sg_type_1, sg_qualifier_1,\n" +
                "         sg_id_2, sg_type_2, sg_qualifier_2\n" +
                "    FROM relationships WHERE 1=0;\n" +
                "\n" +
                "    exec MEME_SYSTEM.analyze('mom_candidate_facts');\n" +
                "    INSERT INTO t1 \n" +
                "    SELECT a.concept_id, b.concept_id,atom_id_1,atom_id_2,\n" +
                "	'RT','','$new_source','$new_source','");
    if (relationship_type.equals(RxConstants.RT_DEMOTION)) {
      body.append("D");
    }
    else {
      body.append("N");
    }
    body.append("','Y','P','N','N',0,'N','','','','','',''\n" +
                "    FROM mom_candidate_facts, classes a, classes b\n" +
                "    WHERE atom_id_1=a.atom_id and atom_id_2=b.atom_id;\n" +
                "\n");

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
    return typeToString();
  };

  /**
   * This method returns a descriptive name for the type of step.
   * @return String
   */
  public static String typeToString() {
    RxToolkit.trace("GeneratedRelationshipMatchStep::typeToString()");
    return "Insert Relationships Based on Matches";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    //
    // View Fields
    //
    private SuperJList jsource_termgroups = new SuperJList(
        RxToolkit.getTermgroups());
    private SuperJList jtarget_termgroups = new SuperJList(
        RxToolkit.getTermgroups());

    // options
    private JCheckBox jsource_new_atoms = new JCheckBox("New atoms only");
    private JCheckBox jsource_exclude_list = new JCheckBox("Use exclude list");
    private JCheckBox jsource_norm_exclude = new JCheckBox(
        "Use norm exclude list");
    private JRadioButton jsource_include = new JRadioButton("Include");
    private JRadioButton jsource_exclude = new JRadioButton("Exclude");
    private JRadioButton jsource_all = new JRadioButton("All");
    private JCheckBox jtarget_exclude_list = new JCheckBox("Use exclude list");
    private JCheckBox jtarget_norm_exclude = new JCheckBox(
        "Use norm exclude list");
    private JRadioButton jtarget_include = new JRadioButton("Include");
    private JRadioButton jtarget_exclude = new JRadioButton("Exclude");
    private JRadioButton jtarget_all = new JRadioButton("All");

    // standard RelationshipMatch options
    private JTextArea jcomments = new JTextArea();
    private JComboBox jstring_match =
        new JComboBox(RxToolkit.DBToolkit.getCodes(
        RxToolkit.DBToolkit.STRING_MATCH));
    private JComboBox jcode_match =
        new JComboBox(RxToolkit.DBToolkit.getCodes(
        RxToolkit.DBToolkit.CODE_MATCH));
    private JComboBox jrelationship_type = new JComboBox(RxConstants.RT_ARRAY);
    private JCheckBox jexclude_safe_replacement =
        new JCheckBox("Exclude Safe Replacement");
    private SuperJList jfilters = new SuperJList(filters.toArray());

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("GeneratedRelationshipMatchStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("GeneratedRelationshipMatchStep.View::initialize()");

      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.gridx = GridBagConstraints.RELATIVE;
      c.insets = RxConstants.EMPTY_INSETS;
      c.anchor = GridBagConstraints.NORTH;
      c.weightx = 1.0;

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
      termgroups_panel.add(new JLabel("Select source termgroups"), constraints);
      termgroups_panel.add(new JLabel("Select target termgroups"), constraints);

      jsource_termgroups.setVisibleRowCount(6);
      jsource_termgroups.setSelectionMode(ListSelectionModel.
                                          MULTIPLE_INTERVAL_SELECTION);
      jsource_termgroups.addListSelectionListener(dcl);
      JScrollPane jscroll = new JScrollPane();
      jscroll.setViewportView(jsource_termgroups);

      constraints.gridwidth = 1;

      // add source termgroups
      constraints.gridy++;
      termgroups_panel.add(jscroll, constraints);

      // add toggles to source termgroups
      constraints.insets = RxConstants.EMPTY_INSETS;
      ButtonGroup bg = new ButtonGroup();
      bg.add(jsource_include);
      bg.add(jsource_exclude);
      bg.add(jsource_all);

      JPanel tmp_panel = new JPanel(new BorderLayout(0, 0));

      jsource_include.addActionListener(dcl);
      tmp_panel.add(jsource_include, BorderLayout.NORTH);

      jsource_exclude.addActionListener(dcl);
      tmp_panel.add(jsource_exclude, BorderLayout.CENTER);

      jsource_all.addActionListener(dcl);
      jsource_all.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          jsource_termgroups.selectAll();
        }
      });
      tmp_panel.add(jsource_all, BorderLayout.SOUTH);
      termgroups_panel.add(tmp_panel, constraints);

      // add target termgroups
      constraints.insets = RxConstants.GRID_INSETS;
      jtarget_termgroups.setVisibleRowCount(6);
      jtarget_termgroups.setSelectionMode(
          ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      jtarget_termgroups.addListSelectionListener(dcl);
      jscroll = new JScrollPane();
      jscroll.setViewportView(jtarget_termgroups);
      termgroups_panel.add(jscroll, constraints);

      // add toggles to target termgroups
      constraints.insets = RxConstants.EMPTY_INSETS;
      bg = new ButtonGroup();
      bg.add(jtarget_include);
      bg.add(jtarget_exclude);
      bg.add(jtarget_all);

      tmp_panel = new JPanel(new BorderLayout(0, 0));

      jtarget_include.addActionListener(dcl);
      tmp_panel.add(jtarget_include, BorderLayout.NORTH);

      jtarget_exclude.addActionListener(dcl);
      tmp_panel.add(jtarget_exclude, BorderLayout.CENTER);

      jtarget_all.addActionListener(dcl);
      jtarget_all.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          jtarget_termgroups.selectAll();
        }
      });
      tmp_panel.add(jtarget_all, BorderLayout.SOUTH);
      termgroups_panel.add(tmp_panel, constraints);

      // Add source/termgroup options
      constraints.gridy++;
      constraints.gridwidth = 2;
      jsource_new_atoms.addActionListener(dcl);
      termgroups_panel.add(jsource_new_atoms, constraints);
      jtarget_exclude_list.addActionListener(dcl);
      termgroups_panel.add(jtarget_exclude_list, constraints);

      constraints.gridy++;
      jsource_exclude_list.addActionListener(dcl);
      termgroups_panel.add(jsource_exclude_list, constraints);
      jtarget_norm_exclude.addActionListener(dcl);
      termgroups_panel.add(jtarget_norm_exclude, constraints);

      constraints.gridy++;
      jsource_norm_exclude.addActionListener(dcl);
      termgroups_panel.add(jsource_norm_exclude, constraints);

      // add merging options.
      JPanel options_panel = new JPanel(new GridBagLayout());
      constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.insets = RxConstants.GRID_INSETS;
      constraints.anchor = GridBagConstraints.NORTH;
      constraints.gridwidth = 1;

      // Relationship type combobox
      constraints.gridy = 0;
      constraints.gridwidth = GridBagConstraints.REMAINDER;
      options_panel.add(new JLabel("Enter Values"), constraints);
      constraints.gridwidth = 1;
      constraints.gridy++;
      options_panel.add(new JLabel("Relationship Type:"), constraints);
      jrelationship_type.addActionListener(dcl);
      options_panel.add(jrelationship_type, constraints);

      // string and code match
      constraints.gridy++;
      options_panel.add(new JLabel("String Match:"), constraints);
      jstring_match.addActionListener(dcl);
      options_panel.add(jstring_match, constraints);

      constraints.gridy++;
      options_panel.add(new JLabel("Code Match:"), constraints);
      jcode_match.addActionListener(dcl);
      options_panel.add(jcode_match, constraints);

      // Add options demote,change_status,exclude sr
      constraints.gridy++;
      constraints.gridheight = 1;
      options_panel.add(new JLabel("Options:"), constraints);
      constraints.insets = RxConstants.EMPTY_INSETS;
      constraints.gridheight = 1;
      jexclude_safe_replacement.addActionListener(dcl);
      options_panel.add(jexclude_safe_replacement, constraints);

      // add termgroups panel (put space between this and next section)
      // Add options panel
      c.gridy = 0;

      // set gridheight based on which one is bigger
      if (termgroups_panel.getPreferredSize().getHeight() >
          options_panel.getPreferredSize().getHeight()) {
        c.gridheight = 2;
        System.out.println("termgroups panel gridheight 2");
      }
      else {
        c.gridheight = 1;
        System.out.println("termgroups panel gridheight 1");
      }
      ;
      add(termgroups_panel, c);

      if (c.gridheight == 1) {
        c.gridheight = 2;
      }
      else {
        c.gridheight = 1;

      }
      System.out.println("options panel gridheight " + c.gridheight);
      add(options_panel, c);

      // continue with remaining options.
      options_panel = new JPanel(new GridBagLayout());
      constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.insets = RxConstants.GRID_INSETS;
      constraints.gridwidth = 1;

      //Filters
      //constraints.gridy = 0;
      constraints.gridy++;
      //constraints.gridheight = 3;

      options_panel.add(new JLabel("Filters:"), constraints);
      jfilters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jfilters.setCellRenderer(
          new FactFilterListCellRenderer());
      jfilters.addListSelectionListener(dcl);
      jfilters.setPopupMenu(new FactFilterPopupMenu(jfilters));
      JScrollPane jfilters_scroll = new JScrollPane();
      jfilters_scroll.setViewportView(jfilters);
      jfilters_scroll.setBorder(RxConstants.HAS_POPUP_BORDER);
      options_panel.add(jfilters_scroll, constraints);

      // Add comments
      constraints.gridy++;
      options_panel.add(new JLabel("Comments:"), constraints);
      constraints.gridy++;
      constraints.gridwidth = 3;
      jcomments.setRows(3);
      jcomments.getDocument().addDocumentListener(dcl);
      JScrollPane comment_scroll = new JScrollPane();
      comment_scroll.setViewportView(jcomments);
      options_panel.add(comment_scroll, constraints);

      // add options
      c.gridy += 3;
      c.gridheight = 1;
      c.gridwidth = GridBagConstraints.REMAINDER;
      add(options_panel, c);

    };

    //
    // Implementation of RxStep.View methods
    //

    /**
     * Set the focus
     */
    public void setFocus() {
      jsource_termgroups.requestFocus();
    }

    /**
     * This takes values from the step and displays them.
     */
    public void getValues() {
      RxToolkit.trace("GeneratedRelationshipMatchStep.View::getValues()");
      if (source_options.get("ALL") != null) {
        jsource_termgroups.selectAll();
      }
      else {
        jsource_termgroups.setSelectedValues(source_termgroups, true);
      }
      ;
      if (target_options.get("ALL") != null) {
        jtarget_termgroups.selectAll();
      }
      else {
        jtarget_termgroups.setSelectedValues(target_termgroups, true);
      }
      ;

      // source options
      jsource_new_atoms.setSelected(
          source_options.get("NEW_ATOMS_ONLY") != null);
      jsource_exclude_list.setSelected(
          source_options.get("EXCLUDE_LIST") != null);
      jsource_norm_exclude.setSelected(
          source_options.get("NORM_EXCLUDE_LIST") != null);
      jsource_include.setSelected(
          source_options.get("INCLUDE") != null);
      jsource_exclude.setSelected(
          source_options.get("EXCLUDE") != null);
      jsource_all.setSelected(
          source_options.get("ALL") != null);

      // target options
      jtarget_exclude_list.setSelected(
          target_options.get("EXCLUDE_LIST") != null);
      jtarget_norm_exclude.setSelected(
          target_options.get("NORM_EXCLUDE_LIST") != null);
      jtarget_include.setSelected(
          target_options.get("INCLUDE") != null);
      jtarget_exclude.setSelected(
          target_options.get("EXCLUDE") != null);
      jtarget_all.setSelected(
          target_options.get("ALL") != null);

      // standard merge options
      jcomments.setText(comments);
      jstring_match.setSelectedItem(string_match);
      jcode_match.setSelectedItem(code_match);
      jrelationship_type.setSelectedItem(relationship_type);
      jexclude_safe_replacement.setSelected(exclude_safe_replacement);

      jfilters.setListData(filters.toArray());
      jfilters.resizeList(1, 6);

      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("GeneratedRelationshipMatchStep.View::setValues()");
      if (jsource_all.isSelected()) {
        jsource_termgroups.selectAll();
      }
      source_termgroups = RxToolkit.toStringArray(
          jsource_termgroups.getSelectedValues());
      if (jtarget_all.isSelected()) {
        jtarget_termgroups.selectAll();
      }
      target_termgroups = RxToolkit.toStringArray(
          jtarget_termgroups.getSelectedValues());

      // source options
      if (jsource_new_atoms.isSelected()) {
        source_options.put("NEW_ATOMS_ONLY", "");
      }
      else {
        source_options.remove("NEW_ATOMS_ONLY");

      }
      if (jsource_exclude_list.isSelected()) {
        source_options.put("EXCLUDE_LIST", "");
      }
      else {
        source_options.remove("EXCLUDE_LIST");

      }
      if (jsource_norm_exclude.isSelected()) {
        source_options.put("NORM_EXCLUDE_LIST", "");
      }
      else {
        source_options.remove("NORM_EXCLUDE_LIST");

      }
      if (jsource_include.isSelected()) {
        source_options.put("INCLUDE", "");
      }
      else {
        source_options.remove("INCLUDE");

      }
      if (jsource_exclude.isSelected()) {
        source_options.put("EXCLUDE", "");
      }
      else {
        source_options.remove("EXCLUDE");

      }
      if (jsource_all.isSelected()) {
        source_options.put("ALL", "");
      }
      else {
        source_options.remove("ALL");

        // target options
      }
      if (jtarget_exclude_list.isSelected()) {
        target_options.put("EXCLUDE_LIST", "");
      }
      else {
        target_options.remove("EXCLUDE_LIST");

      }
      if (jtarget_norm_exclude.isSelected()) {
        target_options.put("NORM_EXCLUDE_LIST", "");
      }
      else {
        target_options.remove("NORM_EXCLUDE_LIST");

      }
      if (jtarget_include.isSelected()) {
        target_options.put("INCLUDE", "");
      }
      else {
        target_options.remove("INCLUDE");

      }
      if (jtarget_exclude.isSelected()) {
        target_options.put("EXCLUDE", "");
      }
      else {
        target_options.remove("EXCLUDE");

      }
      if (jtarget_all.isSelected()) {
        target_options.put("ALL", "");
      }
      else {
        target_options.remove("ALL");

        // standard merge options
      }
      comments = jcomments.getText();
      string_match = (String) jstring_match.getSelectedItem();
      code_match = (String) jcode_match.getSelectedItem();
      relationship_type = (String) jrelationship_type.getSelectedItem();
      exclude_safe_replacement = jexclude_safe_replacement.isSelected();

      filters.clear();
      for (int i = 0; i < jfilters.getModel().getSize(); i++) {
        filters.add(jfilters.getModel().getElementAt(i));
      }
      ;

      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("GeneratedRelationshipMatchStep.View::checkUserEntry()");

      if (jsource_termgroups.getSelectedValue() == null ||
          jtarget_termgroups.getSelectedValue() == null) {
        RxToolkit.notifyUser(
            "You must select source AND target termgroups.");
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

  };
};
