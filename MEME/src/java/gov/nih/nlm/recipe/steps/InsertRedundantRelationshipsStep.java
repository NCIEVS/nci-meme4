/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     InsertRedundantRelationshipsStep.java
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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
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

public class InsertRedundantRelationshipsStep
    extends RxStep {

  //
  // Fields
  //
  protected String[] sources;
  protected String tbr;
  protected String status;
  protected ArrayList filters;
  protected String comments;

  /**
   * InsertRedundantRelationshipsStep default constructor
   */
  public InsertRedundantRelationshipsStep() {
    super();
    RxToolkit.trace(
        "InsertRedundantRelationshipsStep::InsertRedundantRelationshipsStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("InsertRedundantRelationshipsStep::resetValues()");
    sources = new String[] {};
    tbr = "Y";
    status = "N";
    filters = new ArrayList();
    comments = "";
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("InsertRedundantRelationshipsStep::constructView()");
    return new InsertRedundantRelationshipsStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("InsertRedundantRelationshipsStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("InsertRedundantRelationshipsStep::toHTML()");
    StringBuffer step_text = new StringBuffer();

    step_text.append("            <table ALIGN=CENTER WIDTH=90% BORDER=1 CELLSPACING=1 CELLPADDING=2>\n");
    step_text.append("              <tr><td><b>Sources:</b></td><td>\n");
    for (int i = 0; i < sources.length; i++) {
      step_text.append("                   ");
      step_text.append(sources[i]);
      step_text.append("\n");
    }
    step_text.append("              </tr>\n");
    step_text.append("              <tr><td><b>TBR:</b></td><td>\n");
    step_text.append("                 ");
    step_text.append(tbr);
    step_text.append("</td></tr>\n");

    step_text.append("              </tr>\n");
    step_text.append("              <tr><td><b>Status:</b></td><td>\n");
    step_text.append("                 ");
    step_text.append(status);
    step_text.append("</td></tr>\n");

    // Iterate through filters
    if (filters != null && filters.size() > 0) {
      step_text.append("              <tr>\n");
      step_text.append("                <td><b>Filters:</b></td><td>");
      step_text.append("                  <ol>\n");
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
    StringBuffer body = new StringBuffer(500);
    body.append("#\n# Clone Relationships\n");
    if (comments != null && !comments.equals("")) {
      body.append("# ");
      body.append(comments);
    }
    body.append("\n#\n");
    body.append("echo \"    Clone relationships ...`/bin/date`\"\n\n");
    body.append(
        "$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log\n" +
        "    set serveroutput on size 100000\n" +
        "    set feedback off\n" +
        "    ALTER SESSION SET sort_area_size=200000000;\n" +
        "    WHENEVER SQLERROR EXIT -1\n" +
        " \n" +
        "    exec MEME_UTILITY.drop_it('table','t1')\n" +
        "    CREATE TABLE t1 AS\n" +
        "    SELECT  concept_id_1,concept_id_2, \n" +
        "      0 as atom_id_1, 0 as atom_id_2,\n" +
        "      'XS' as relationship_name,relationship_attribute,\n" +
        "      source, source_of_label, 'N' as status,\n" +
        "      'Y' as generated_status, 'C' as relationship_level,\n" +
        "      released,tobereleased, 0 as relationship_id, \n" +
        "      suppressible\n" +
        "      FROM relationships \n" +
        "    WHERE relationship_id in\n" +
        "      (SELECT relationship_id FROM source_relationships)\n" +
        "      AND relationship_name = 'RT?'); \n" +
        "\n" +
        "    -- Delete self-referential\n" +
        "    DELETE FROM t1 WHERE concept_id_1=concept_id_2; \n" +
        "\n" +
        "    -- Delete where C level rel already exists\n" +
        "    DELETE FROM t1 a WHERE EXISTS \n" +
        "    (SELECT 1 FROM relationships b \n" +
        "     WHERE relationship_level='C'\n" +
        "     AND a.concept_id_1=b.concept_id_1 \n" +
        "     AND a.concept_id_2=b.concept_id_2);\n" +
        "    DELETE FROM t1 a WHERE EXISTS \n" +
        "    (SELECT 1 FROM relationships b \n" +
        "     WHERE relationship_level='C'\n" +
        "     AND a.concept_id_2=b.concept_id_1 \n" +
        "     AND a.concept_id_1=b.concept_id_2);\n" +
        "\n" +
        "    -- Set rela\n" +
        "    UPDATE t1 SET relationship_attribute='', \n" +
        "                  source_of_label='MTH',\n" +
        "                  tobereleased='");
    body.append(tbr).append("',\n" +
                            "                  status='");
    body.append(status).append("';\n\n" +
                               "EOF\n" +
                               "if ($status != 0) then\n" +
                               "    echo \"Error cloning relationships\"\n" +
                               "    cat /tmp/t.$$.log\n" +
                               "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error cloning relationships\"' \n" +
                               "    exit 1\n" +
                               "endif\n" +
                               "\n" +
                               "    $MEME_HOME/bin/insert.pl -w $work_id -host=$host -port=$port -rels t1 $db $authority >&! insert.xs.log\n" +
                               "if ($status != 0) then\n" +
                               "    echo \"Error inserting relationships\"\n" +
                               "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error inserting relationships\"' \n" +
                               "    exit 1\n" +
                               "endif\n" +
                               "\n");

    return body.toString();
  };

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
    RxToolkit.trace("InsertRedundantRelationshipsStep::typeToString()");
    return "Insert Redundant Relationships";
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
    private SuperJList jtbr =
        new SuperJList(RxToolkit.DBToolkit.getTobereleased());
    private SuperJList jstatus =
        new SuperJList(RxToolkit.DBToolkit.getStatus());
    private SuperJList jfilters = new SuperJList();
    JTextArea jcomments = new JTextArea();

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("InsertRedundantRelationshipsStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("InsertRedundantRelationshipsStep.View::initialize()");

      setLayout(new BorderLayout());
      JPanel data_panel = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.gridx = GridBagConstraints.RELATIVE;
      c.insets = RxConstants.GRID_INSETS;

      // Create an RxStep.DataChangeListener.
      DataChangeListener dcl = new DataChangeListener();

      // Add source list
      jsources.setVisibleRowCount(5);
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

      // add tbr
      c.gridy++;
      data_panel.add(new JLabel("TBR:"), c);
      jtbr.addListSelectionListener(dcl);
      jtbr.resizeList(1, 5);
      jtbr.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      data_panel.add(jtbr, c);

      // add status
      c.gridy++;
      jstatus.addListSelectionListener(dcl);
      jstatus.resizeList(1, 3);
      jstatus.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      data_panel.add(new JLabel("Status:"), c);
      data_panel.add(jstatus, c);

      // Filters
      c.gridy++;
      data_panel.add(new JLabel("Filters:"), c);
      jfilters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jfilters.setCellRenderer(
          new FactFilterListCellRenderer());
      jfilters.addListSelectionListener(dcl);
      jfilters.setPopupMenu(new FactFilterPopupMenu(jfilters));
      JScrollPane jfilters_scroll = new JScrollPane();
      jfilters_scroll.setViewportView(jfilters);
      jfilters_scroll.setBorder(RxConstants.HAS_POPUP_BORDER);
      data_panel.add(jfilters_scroll, c);

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
      RxToolkit.trace("InsertRedundantRelationshipsStep.View::getValues()");
      jsources.setSelectedValues(sources, true);
      jtbr.setSelectedValue(tbr, true);
      jstatus.setSelectedValue(status, true);
      jcomments.setText(comments);
      jfilters.setListData(filters.toArray());
      jfilters.resizeList(1, 6);
      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("InsertRedundantRelationshipsStep.View::setValues()");
      sources = RxToolkit.toStringArray(jsources.getSelectedValues());
      tbr = (String) jtbr.getSelectedValue();
      status = (String) jstatus.getSelectedValue();
      filters.clear();
      for (int i = 0; i < jfilters.getModel().getSize(); i++) {
        filters.add(jfilters.getModel().getElementAt(i));
      }
      ;
      comments = jcomments.getText();
      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      RxToolkit.trace(
          "InsertRedundantRelationshipsStep.View::checkUserEntry()");
      // Must select a source
      if (jsources.getSelectedValue() == null) {
        RxToolkit.reportError("You must fill out the 'Sources:' field");
        return false;
      }
      // Must select a tbr value
      if (jtbr.getSelectedValue() == null) {
        RxToolkit.reportError("You must fill out the 'TBR:' field");
        return false;
      }
      return true;
    };

  }

}
