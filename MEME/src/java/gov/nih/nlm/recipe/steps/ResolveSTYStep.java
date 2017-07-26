/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     ResolveSTYStep.java
 *
 * CHANGES
 * 02/24/2009 BAC (1-GCLNT): Parallelize operation
 * 03/27/2007 JFW (1-DUD4L): generate sty_term_ids with field to indicate source replacment
 * 11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
import gov.nih.nlm.swing.SuperJList;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;


/**
 * This step is used to cause a sources default semantic types
 * to either "win" or to "lose"
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class ResolveSTYStep
    extends RxStep {

  //
  // Constants
  //
  public final static String STY_LOSE = "Lose";
  public final static String STY_WIN = "Win";

  //
  // Fields
  //
  protected String[] sources;
  protected String win_or_lose;
  protected String comments;

  /**
   * ResolveSTYStep default constructor
   */
  public ResolveSTYStep() {
    super();
    RxToolkit.trace("ResolveSTYStep::ResolveSTYStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("ResolveSTYStep::resetValues()");
    sources = new String[] {};
    win_or_lose = STY_LOSE;
    comments = "";
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("ResolveSTYStep::constructView()");
    return new ResolveSTYStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("ResolveSTYStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("ResolveSTYStep::toHTML()");
    StringBuffer step_text = new StringBuffer();

    step_text.append("            <table ALIGN=CENTER WIDTH=90% BORDER=1 CELLSPACING=1 CELLPADDING=2>\n");
    step_text.append("              <tr><td><b>Sources:</b></td><td>\n");
    for (int i = 0; i < sources.length; i++) {
      step_text.append("                   ");
      step_text.append(sources[i]);
      step_text.append("\n");
    }
    step_text.append("              </tr>\n");
    step_text.append("              <tr><td><b>Win or Lose:</b></td><td>\n");
    step_text.append("                 ");
    step_text.append(win_or_lose);
    step_text.append("</td></tr>\n");

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
    StringBuffer body = new StringBuffer(500);
    body.append("#\n# Resove Semantic Types\n");
    if (comments != null && !comments.equals("")) {
      body.append("# ").append(comments).append("\n");
    }
    body.append("#\n");
    body.append("echo \"    Resolve Semantic Types ...`/bin/date`\"\n\n");
    body.append("if ($mode == \"test\") then\n" +
        "    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! sty_term_ids &\n" +
        "    WHENEVER SQLERROR EXIT -2\n" +
        "    set wrap off\n" +
        "    set feedback off\n" +
        "    set pagesize 0\n" +
        "    set verify off\n" +
        "    set trimspool on\n" +
        "    set linesize 9999\n" +
        "    SELECT DISTINCT source_row_id || '|' ||\n" +
        "	attribute_value || '|' || classes_flag\n" +
        "    FROM attributes a, source_id_map b, \n" +
        "	(SELECT concept_id,atom_id, decode(sign(insertion_date-(sysdate-10)),1,1,0) classes_flag \n"+
        "  FROM classes\n" +
        "	 WHERE source='$new_source') c\n" +
        "    WHERE a.concept_id = c.concept_id\n" +
        "    AND c.atom_id=local_row_id\n" +
        "    AND table_name='C' AND b.source='$new_source'\n" +
        "    AND attribute_name='SEMANTIC_TYPE';\n" +
        "EOF\n" +
        "endif\n\n" +
        "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF  >&! /tmp/t.sty.$$.log &\n" +
        "    WHENEVER SQLERROR EXIT -2\n" +
        "    set serveroutput on size 100000\n" +
        "    set feedback off\n" +
        "    ALTER SESSION SET sort_area_size=200000000;\n" +
        "    ALTER SESSION SET hash_area_size=200000000;\n" +
        "    exec MEME_SOURCE_PROCESSING.resolve_stys ( -\n" +
        "	source => '$new_source', -\n" +
        "        sty_fate => '")
        .append(win_or_lose.substring(0, 1))
        .append("', -\n" +
                "        authority => '$authority', -\n" +
                "        work_id => $work_id );\n" +
                "EOF\n\n");
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
    RxToolkit.trace("ResolveSTYStep::typeToString()");
    return "Resolve Semantic Types";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    //
    // View Fields
    //
    private SuperJList jsources =
        new SuperJList(RxToolkit.getSources());
    private JRadioButton jwin = new JRadioButton(STY_WIN);
    private JRadioButton jlose = new JRadioButton(STY_LOSE);
    JTextArea jcomments = new JTextArea();

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("ResolveSTYStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("ResolveSTYStep.View::initialize()");

      setLayout(new BorderLayout());
      JPanel data_panel = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.gridx = GridBagConstraints.RELATIVE;
      c.insets = RxConstants.GRID_INSETS;

      // Create an RxStep.DataChangeListener.
      DataChangeListener dcl = new DataChangeListener();

      // Add source list
      jsources.setVisibleRowCount(6);
      jsources.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      jsources.addListSelectionListener(dcl);
      JScrollPane jscroll = new JScrollPane();
      jscroll.setViewportView(jsources);

      c.gridy = 0;
      c.gridwidth = GridBagConstraints.REMAINDER;
      data_panel.add(new JLabel(typeToString()), c);

      c.gridy++;
      c.gridwidth = 1;
      data_panel.add(new JLabel("Sources:"), c);
      data_panel.add(jscroll, c);

      // add buttons
      ButtonGroup bg = new ButtonGroup();
      bg.add(jwin);
      bg.add(jlose);
      jwin.addActionListener(dcl);
      jlose.addActionListener(dcl);

      c.gridy++;
      c.gridheight = 2;
      data_panel.add(new JLabel("STYs should:"), c);
      c.gridheight = 1;
      data_panel.add(jwin, c);
      c.gridy++;
      data_panel.add(jlose, c);

      c.gridy++;
      data_panel.add(new JLabel("Comments:"), c);
      c.gridy++;
      c.gridwidth = GridBagConstraints.REMAINDER;
      jcomments.setRows(3);
      jcomments.getDocument().addDocumentListener(dcl);
      JScrollPane comment_scroll = new JScrollPane();
      comment_scroll.setViewportView(jcomments);
      data_panel.add(comment_scroll, c);

      add(data_panel);

    };

    //
    // Implementation of RxStep.View methods
    //

    /**
     * Set the focus
     */
    public void setFocus() {
      jsources.requestFocus();
    }

    /**
     * This takes values from the step and displays them.
     */
    public void getValues() {
      RxToolkit.trace("ResolveSTYStep.View::getValues()");
      jsources.setSelectedValues(sources, true);
      if (win_or_lose.equals(STY_WIN)) {
        jwin.setSelected(true);
        jlose.setSelected(false);
      }
      else if (win_or_lose.equals(STY_LOSE)) {
        jlose.setSelected(true);
        jwin.setSelected(false);
      }
      else {
        RxToolkit.reportError(
            "Illegal win_or_lose value in ResolveSTYStep: " +
            win_or_lose + ".");
      }
      jcomments.setText(comments);
      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("ResolveSTYStep.View::setValues()");
      sources = RxToolkit.toStringArray(jsources.getSelectedValues());
      if (jwin.isSelected()) {
        win_or_lose = STY_WIN;
      }
      if (jlose.isSelected()) {
        win_or_lose = STY_LOSE;
      }
      comments = jcomments.getText();
      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("ResolveSTYStep.View::checkUserEntry()");
      // Must select a source
      if (jsources.getSelectedValue() == null) {
        RxToolkit.reportError("You must fill out the 'Sources:' field");
        return false;
      }
      return true;
    };

  }

}
