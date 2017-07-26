/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     PrecomputedMergeStep.java
 *
 * CHANGES
 * 01/31/2008 BAC (1-GCLNT): Call load_src.csh with max ids.
 * 11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.FactFilter;
import gov.nih.nlm.recipe.IntegrityCheck;
import gov.nih.nlm.recipe.IntegrityVector;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
import gov.nih.nlm.swing.FactFilterListCellRenderer;
import gov.nih.nlm.swing.FactFilterPopupMenu;
import gov.nih.nlm.swing.IntegrityCheckListCellRenderer;
import gov.nih.nlm.swing.SuperJList;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 * This step is used to process mergefacts.src facts
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class PrecomputedMergeStep
    extends AbstractMergeStep {

  //
  // Constants
  //

  //
  // Fields
  //

  /** these fields are all defined in AbstractMergeStep
      protected String merge_set;
      protected boolean demote;
      protected boolean change_status;
      protected boolean exclude_safe_replacement;
      protected String pre_insert = RxConstants.PI_NONE;
      protected ArrayList filters;
      protected IntegrityVector integrity_vector;
      protected boolean include_or_exclude;
      protected boolean all_checks_set;
      protected String comments;
   */

  /**
   * PrecomputedMergeStep default constructor
   */
  public PrecomputedMergeStep() {
    super();
    RxToolkit.trace("PrecomputedMergeStep::PrecomputedMergeStep()");
    resetValues();
  };

  /**
   * PrecomputedMergeStep constructor for loading from XML
   */
  public PrecomputedMergeStep(HashMap hm) {
    super();
    RxToolkit.trace("PrecomputedMergeStep::PrecomputedMergeStep(.)");
    this.merge_set = (String) hm.get("merge_set");
    this.demote = Boolean.valueOf( (String) hm.get("demote")).booleanValue();
    this.change_status = Boolean.valueOf( (String) hm.get("change_status")).
        booleanValue();
    this.exclude_safe_replacement = Boolean.valueOf( (String) hm.get(
        "exclude_safe_relplacement")).booleanValue();
    this.pre_insert = (String) hm.get("pre_insert");
    this.include_or_exclude = Boolean.valueOf( (String) hm.get(
        "include_or_exclude")).booleanValue();
    this.all_checks_set = Boolean.valueOf( (String) hm.get("all_checks_set")).
        booleanValue();
    this.comments = (String) hm.get("comments");
    RxToolkit.trace("before filters gets read");
    //FactFilter fact_filter = (FactFilter)hm.get("filters");
    //MEMEToolkit.trace("after filters is gotten");
    HashMap filters_hm = (HashMap) hm.get("filters");
    RxToolkit.trace("after filters is gotten");
    filters = new ArrayList(filters_hm.size());
    RxToolkit.trace("filters size " + filters_hm.size());

    Iterator i = filters_hm.entrySet().iterator();
    while (i.hasNext()) {
      RxToolkit.trace("iterating through loop ");
      Map.Entry e = (Map.Entry) i.next();
      filters.add(Integer.valueOf( (String) e.getKey()).intValue(), e.getValue());
    }
    RxToolkit.trace(filters.toString());
    integrity_vector = new IntegrityVector();
    // need to read in filters and integrity vectors
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("PrecomputedMergeStep::resetValues()");
    String[] merge_sets = RxToolkit.getMergeSetsFromFile();
    merge_set = merge_sets[0];
    demote = true;
    change_status = false;
    exclude_safe_replacement = false;
    filters = new ArrayList();
    integrity_vector = new IntegrityVector();
    all_checks_set = true;
    include_or_exclude = true;
    pre_insert = RxConstants.PI_N_TO_N;
    comments = "";
    source_options.clear();
    target_options.clear();
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("PrecomputedMergeStep::constructView()");
    return new PrecomputedMergeStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("PrecomputedMergeStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("GeneratedMergeStep::toHTML()");
    StringBuffer step_text = new StringBuffer();

    step_text.append("            <table ALIGN=CENTER WIDTH=90% BORDER=1 CELLSPACING=1 CELLPADDING=2>\n");
    step_text.append(
        "              <tr><th colspan=2>Merge Parameters</th></tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Merge Set:</b></td><td>");
    step_text.append(merge_set + "</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Integrity Vector:</b></td><td>");
    if (all_checks_set) {
      step_text.append("ALL");
    }
    else if (include_or_exclude) {
      step_text.append(integrity_vector.toHTML());
    }
    else {
      step_text.append("ALL - " + integrity_vector.toHTML());
    }
    ;
    step_text.append("</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Demote:</b></td><td>");
    step_text.append( (demote ? "Yes" : "No"));
    step_text.append("</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Change Status:</b></td><td>");
    step_text.append( (change_status ? "Yes" : "No"));
    step_text.append("</td>\n              </tr>\n");
    step_text.append("              <tr>\n");
    step_text.append("                <td><b>Pre Insert:</b></td><td>");
    step_text.append(pre_insert);
    step_text.append("</td>\n              </tr>\n");

    if ( (filters != null && filters.size() > 0) || exclude_safe_replacement) {
      step_text.append("              <tr>\n");
      step_text.append("                <td><b>Filters:</b></td><td>");
      step_text.append("                  <ol>\n");

      if (exclude_safe_replacement) {
        step_text.append(
            "                    <li>Exclude safe replacement.</li>\n");
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
   * to perform a pre-insert merge operation.
   */
  public String toShellScriptPreInsert() {

    int ambig_flag = 0;
    if (pre_insert.equals(RxConstants.PI_ONE_TO_ONE)) {
      ambig_flag = 1;

    }
    StringBuffer body = new StringBuffer(1000);

    body.append(toShellScriptHelper());

    body.append("\n" +
                "    -- Replacement merges\n" +
                "    exec MEME_SOURCE_PROCESSING.replacement_merges ( -\n" +
                "        merge_set => '");
    body.append(merge_set);
    body.append("', -\n" +
                "        normalization_flag => 1, -\n" +
                "	ambig_flag => ");
    body.append(ambig_flag);
    body.append(", -\n" +
                "        authority => '$authority', -\n" +
                "        work_id => $work_id);\n" +
                "\n");

    body.append(
        "    DELETE FROM mom_merge_facts WHERE status in ('P','F','R');\n" +
        "    exec MEME_SOURCE_PROCESSING.move_processed_facts ( -\n" +
        "        authority => '$authority', -\n" +
        "        work_id => $work_id);\n" +
        "\n" +
        "EOF\n\n" +
        "if ($status != 0) then\n" +
        "    echo \"Error running merge set\"\n" +
        "    cat /tmp/t.$$.log\n" +
        "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error running merge set\"' \n" +
                "    exit 1\n" +
        "endif\n");
    return body.toString();
  }

  /**
   * Return shell script code for this step.
   */
  public String toShellScript() {
    StringBuffer body = new StringBuffer(1000);

    body.append(toShellScriptHelper());

    // Handle integrity vector

    // If all, or exclude, start by setting all on
    if (all_checks_set || !include_or_exclude) {
      body.append("\n" +
                  "    -- Set All Checks\n" +
                  "    update mom_merge_facts\n" +
                  "    set integrity_vector = (select integrity_Vector from\n" +
                  "    ic_applications where application='MERGE_O_MATIC');\n");
    }
    else if (integrity_vector.size() > 0) {
      // if include and size>0 just set it
      body.append("\n" +
                  "    -- Set Checks\n" +
                  "    update mom_merge_facts\n" +
                  "    set integrity_vector = '");
      body.append(integrity_vector.toString());
      body.append("';\n");
    }

    // If exclude them remove the ones to exclude
    if (!include_or_exclude) {
      IntegrityCheck[] ics = integrity_vector.toICArray();
      for (int i = 0; i < ics.length; i++) {
        String check = "<" + ics[i].ic_name + ":" + ics[i].ic_code + ">";
        body.append("    -- Remove ");
        body.append(check);
        body.append("\n" +
                    "    update mom_merge_facts\n" +
                    "    set integrity_vector =\n" +
                    "      substr(integrity_vector,0,instr(integrity_vector,'");
        body.append(check);
        body.append("')-1)|| \n" +
                    "      substr(integrity_vector,instr(integrity_vector,'");
        body.append(check);
        body.append("')+");
        body.append(check.toString().length()).append(");\n");

      }
    }

    body.append("\n" +
                "EOF\n" +
                "if ($status != 0) then\n" +
                "    echo \"Error loading merge set\"\n" +
                "    cat /tmp/t.$$.log\n" +
                "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error loading merge set\"' \n" +
            "    exit 1\n" +
                "endif\n\n" +
                "set start_t=`perl -e 'print time'`\n" +
                "$MEME_HOME/bin/merge.pl -host=$host -port=$port ")
        .append(merge_set)
        .append(" $authority $work_id $db >&! merge.");
    body.append(merge_set.toLowerCase());
    body.append(".log\n" +
                "if ($status != 0) then\n" +
                "    echo \"Error merging\"\n" +
                "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error merging - ")
                .append(merge_set.toUpperCase()).append("\"' \n" +
            "    exit 1\n" +
                "endif\n" +
        "$MEME_HOME/bin/log_operation.pl $db $authority Merging \"Call merge.pl for ");
    body.append(merge_set.toUpperCase());
    body.append("\" $work_id 0 $start_t >> /dev/null\n" +
                "\n" +
                "#\n# Move Processed Facts\n#\n" +
        "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log\n" +
        "    set serveroutput on size 100000\n" +
        "    set feedback off\n" +
        "    WHENEVER SQLERROR EXIT -1\n" +
        " \n" +
        "    DELETE FROM mom_merge_facts WHERE status in ('P','F');\n" +
        "    exec MEME_SOURCE_PROCESSING.move_processed_facts ( -\n" +
        "        authority => '$authority', -\n" +
        "        work_id => $work_id);\n" +
        "\n" +
        "EOF\n" +
        "if ($status != 0) then\n" +
        "    echo \"Error moving facts\"\n" +
        "    cat /tmp/t.$$.log\n" +
        "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error moving facts\"' \n" +
            "    exit 1\n" +
        "endif\n");
    return body.toString();
  }

  /**
   * helper!
   */
  private String toShellScriptHelper() {

    StringBuffer body = new StringBuffer(1000);
    body.append("#\n# Precomputed set ");
    body.append(merge_set).append("\n");
    if (comments != null && !comments.equals("")) {
      body.append("# ").append(comments).append("\n");
    }
    body.append("#\n");
    body.append("echo \"    Perform precomputed set '");
    body.append(merge_set);
    body.append("' ...`/bin/date`\"\n\n" +
                "set start_t=`perl -e 'print time'`\n" +
        "$MEME_HOME/bin/load_src.csh $db mergefacts.src 0 >! load_src.log\n" +
        "if ($status != 0) then \n" +
        "    echo \"Error loading mergefacts\"\n" +
        "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error loading mergefacts\"' \n" +
            "    exit 1\n" +
        "endif\n" +
        "$MEME_HOME/bin/log_operation.pl $db $authority Merging \"Load mergefacts.src for ");
    body.append(merge_set.toUpperCase());
    body.append("\" $work_id 0 $start_t >> /dev/null\n\n" +
        "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log\n" +
        "    set serveroutput on size 100000\n" +
        "    set feedback off\n" +
        "    ALTER SESSION SET sort_area_size=200000000;\n" +
        "    ALTER SESSION SET hash_area_size=200000000;\n" +
        "    WHENEVER SQLERROR EXIT -1\n" +
        " \n" +
        "    exec MEME_SOURCE_PROCESSING.prepare_src_mergefacts( -\n" +
        "	authority => '$authority', -\n" +
        "	merge_set => '").append(merge_set).append("', -\n" +
        "	work_id => $work_id);\n" +
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

    if (source_options.get("EXCLUDE_LIST") != null) {
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

    if (source_options.get("NORM_EXCLUDE_LIST") != null) {
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

    if (source_options.get("NEW_ATOMS_ONLY") != null) {
      body.append(
          "    -- Restrict to new atoms only \n" +
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
    if (target_options.get("EXCLUDE_LIST") != null) {
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

    if (target_options.get("NORM_EXCLUDE_LIST") != null) {
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
      body.append("\n");
    }

    body.append("    exec MEME_SOURCE_PROCESSING.load_facts ( -\n" +
                "        merge_set => '");
    body.append(merge_set);
    body.append("', -\n" +
                "        integrity_vector => '', -\n" +
                "        change_status => '");
    body.append( (change_status ? "Y" : "N"));
    body.append("', -\n" +
                "        make_demotion => '");
    body.append( (demote ? "Y" : "N"));
    body.append("', -\n" +
                "        authority => '$authority', -\n" +
                "        work_id => $work_id);\n" +
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
    return "Merge Step: " + " " + merge_set;
  };

  /**
   * This method returns a descriptive name for the type of step.
   * @return String
   */
  public static String typeToString() {
    RxToolkit.trace("PrecomputedMergeStep::typeToString()");
    return "Merge Step - mergefacts.src";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    //
    // View Fields
    //
    private JTextArea jcomments = new JTextArea();
    private JComboBox jmerge_set = new JComboBox();
    private JCheckBox jdemote = new JCheckBox("Demote");
    private JCheckBox jchange_status = new JCheckBox("Change Status");
    private JCheckBox jexclude_safe_replacement =
        new JCheckBox("Exclude Safe Replacement");
    private JComboBox jpre_insert =
        new JComboBox(new String[] {RxConstants.PI_NONE,
                      RxConstants.PI_ONE_TO_ONE,
                      RxConstants.PI_N_TO_N});
    private SuperJList jintegrity_vector =
        new SuperJList(RxToolkit.DBToolkit.getMergeChecks());
    private SuperJList jfilters = new SuperJList(filters.toArray());
    private JRadioButton jinclude = new JRadioButton("Include");
    private JRadioButton jexclude = new JRadioButton("Exclude");
    private JRadioButton jall = new JRadioButton("All");

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("PrecomputedMergeStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("PrecomputedMergeStep.View::initialize()");

      try {
        String[] merge_sets =
            RxToolkit.getMergeSetsFromFile();
        for (int i = 0; i < merge_sets.length; i++) {
          jmerge_set.addItem(merge_sets[i]);
          jmerge_set.setSelectedItem(merge_sets[0]);
        }
        ;
        RxToolkit.trace(merge_sets[0]);

      }
      catch (Exception e) {
        RxToolkit.reportError(
            "Exception while getting merge sets from mergefacts.src: " + e);
      }
      ;

      // Create an RxStep.DataChangeListener.
      DataChangeListener dcl = new DataChangeListener();

      JPanel data_panel = new JPanel(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.insets = RxConstants.GRID_INSETS;

      // Merge Set combobox
      constraints.gridy = 0;
      data_panel.add(new JLabel("Merge Set:"), constraints);
      jmerge_set.addActionListener(dcl);
      jmerge_set.setEditable(true);
      /* ((JTextField)jmerge_set.getEditor().getEditorComponent()).
        setDocument(new UppercaseDocument()); */
      data_panel.add(jmerge_set, constraints);

      // Add options demote,change_status,exclude sr
      constraints.gridy++;
      constraints.insets = RxConstants.EMPTY_INSETS;
      constraints.gridheight = 3;
      data_panel.add(new JLabel("Options:"), constraints);
      constraints.gridheight = 1;
      jdemote.addActionListener(dcl);
      data_panel.add(jdemote, constraints);
      constraints.gridy++;
      constraints.gridx = 1;
      jchange_status.addActionListener(dcl);
      data_panel.add(jchange_status, constraints);
      constraints.gridy++;
      jexclude_safe_replacement.addActionListener(dcl);
      data_panel.add(jexclude_safe_replacement, constraints);

      // Add pre-insert option
      constraints.insets = RxConstants.GRID_INSETS;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy++;
      data_panel.add(new JLabel("Pre-Insert Merge:"), constraints);
      jpre_insert.addActionListener(dcl);
      data_panel.add(jpre_insert, constraints);

      // Integrity checks
      constraints.gridy++;
      constraints.gridheight = 3;
      jintegrity_vector.setSelectionMode(
          ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      jintegrity_vector.setCellRenderer(new IntegrityCheckListCellRenderer());
      jintegrity_vector.setVisibleRowCount(4);
      jintegrity_vector.addListSelectionListener(dcl);
      data_panel.add(new JLabel("Integrity Vector"), constraints);
      JScrollPane jiv_scroll = new JScrollPane();
      jiv_scroll.setViewportView(jintegrity_vector);
      data_panel.add(jiv_scroll, constraints);

      // Add toggles to integrity vector
      constraints.gridheight = 1;
      ButtonGroup bg = new ButtonGroup();
      bg.add(jinclude);
      bg.add(jexclude);
      bg.add(jall);

      jinclude.addActionListener(dcl);
      data_panel.add(jinclude, constraints);

      constraints.gridy++;
      jexclude.addActionListener(dcl);
      data_panel.add(jexclude, constraints);

      constraints.gridy++;
      jall.addActionListener(dcl);
      jall.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          jintegrity_vector.selectAll();
        }
      });
      data_panel.add(jall, constraints);

      // Filters
      constraints.gridy++;
      data_panel.add(new JLabel("Filters"), constraints);
      jfilters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jfilters.setCellRenderer(
          new FactFilterListCellRenderer());
      // visibleRowCount set by getValues
      jfilters.addListSelectionListener(dcl);
      jfilters.setPopupMenu(new FactFilterPopupMenu(jfilters));

      JScrollPane jfilters_scroll = new JScrollPane();
      jfilters_scroll.setViewportView(jfilters);
      jfilters_scroll.setBorder(RxConstants.HAS_POPUP_BORDER);
      data_panel.add(jfilters_scroll, constraints);

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
      jmerge_set.requestFocus();
    }

    /**
     * This takes values from the step and displays them.
     */
    public void getValues() {
      RxToolkit.trace("PrecomputedMergeStep.View::getValues()");
      jcomments.setText(comments);
      jmerge_set.setSelectedItem(merge_set);
      jdemote.setSelected(demote);
      jchange_status.setSelected(change_status);
      jexclude_safe_replacement.setSelected(exclude_safe_replacement);
      jpre_insert.setSelectedItem(pre_insert);
      jfilters.setListData(filters.toArray());
      jfilters.resizeList(1, 6);
      if (include_or_exclude && !all_checks_set) {
        jinclude.setSelected(true);
        jexclude.setSelected(false);
        jall.setSelected(false);
        jintegrity_vector.setSelectedValues(integrity_vector.toArray(), true);
      }
      else if (!all_checks_set) {
        jinclude.setSelected(false);
        jexclude.setSelected(true);
        jall.setSelected(false);
        jintegrity_vector.setSelectedValues(integrity_vector.toArray(), true);
      }
      else {
        jinclude.setSelected(false);
        jexclude.setSelected(false);
        jall.setSelected(true);
        jintegrity_vector.selectAll();
      }
      ;
      jintegrity_vector.scrollToFirstSelectedRow();

      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("PrecomputedMergeStep.View::setValues()");
      comments = jcomments.getText();
      merge_set = (String) jmerge_set.getSelectedItem();
      demote = jdemote.isSelected();
      change_status = jchange_status.isSelected();
      exclude_safe_replacement = jexclude_safe_replacement.isSelected();
      pre_insert = (String) jpre_insert.getSelectedItem();
      if (!pre_insert.equals(RxConstants.PI_NONE)) {
        addPreInsertStep();
      }
      else {
        removePreInsertStep();
      }
      Object[] oa = jintegrity_vector.getSelectedValues();
      integrity_vector.clear();
      for (int i = 0; i < oa.length; i++) {
        ( (IntegrityCheck) oa[i]).ic_code = "E";
        RxToolkit.trace(oa[i].toString());
        integrity_vector.add(oa[i]);
      }
      ;
      all_checks_set = (integrity_vector.size() ==
                        (RxToolkit.DBToolkit.getMergeChecks()).length);

      filters.clear();
      for (int i = 0; i < jfilters.getModel().getSize(); i++) {
        filters.add(jfilters.getModel().getElementAt(i));
      }
      ;

      include_or_exclude = jinclude.isSelected() || jall.isSelected();
      all_checks_set = jall.isSelected();
      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("PrecomputedMergeStep.View::checkUserEntry()");

      if (!jinclude.isSelected() &&
          !jexclude.isSelected() &&
          !jall.isSelected() &&
          ! (jintegrity_vector.getSelectedValue() == null)) {
        RxToolkit.notifyUser(
            "For the integrity vector you must select\n" +
            "include, exclude, or all.");
        return false;
      }

      return true;
    };

  }

}
