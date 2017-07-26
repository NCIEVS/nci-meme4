/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     LoadStep.java
 *
 * CHANGES
 * 11/15/2006 BAC (1-CTLEE): mail.pl call fixed to correctly send mail
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.Recipe;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
import gov.nih.nlm.recipe.UpdateSourceRecipe;
import gov.nih.nlm.swing.UppercaseDocument;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * This step implements the insertion part of a New Source recipe
 * including the loading of src files into the database and loading
 * of source_* tables into the core tables.
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class LoadStep
    extends RxStep {

  //
  // Constants appropriate only to this class
  //
  private final static String ENGLISH = "ENG";

  //
  // Fields
  //
  protected String source_name;
  protected String old_source_name;
  protected String default_authority;
  protected String source_language;
  protected String comments;
  protected boolean classes;
  protected boolean concepts;
  protected boolean relationships;
  protected boolean attributes;
  protected boolean contexts;
  protected boolean termgroups;
  protected boolean sources;

  /**
   * LoadStep default constructor
   */
  public LoadStep() {
    super();
    resetValues();
  };

  /**
   * Reset the view to default values
   */
  public void resetValues() {
    String[] names = RxToolkit.getSources();
    source_name = names[0];
    old_source_name = "";
    // Parent is null first time thru
    if (parent != null) {
      parent.getParent().setName("");
      parent.getParent().setDescription("");
      parent.getParent().setAuthority("");
    }
    default_authority = names[0];
    source_language = ENGLISH;
    comments = "";
    classes = true;
    concepts = true;
    relationships = true;
    attributes = true;
    contexts = true;
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    return new LoadStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    StringBuffer step_text = new StringBuffer();
    if (LoadStep.this.getParent().getParent()instanceof UpdateSourceRecipe) {
      step_text.append("            <p>source name : " + source_name + "<br>\n");
      step_text.append("            <p>old source name : " + old_source_name +
                       "<br>\n");
    }
    else {
      step_text.append("            <p>source name : " + source_name + "<br>\n");
    }
    step_text.append("            source language : " + source_language +
                     "<br>\n");
    step_text.append("            <ul>\n");
    if (concepts) {
      step_text.append("            <li>Concepts are being inserted.\n");
    }
    if (classes) {
      step_text.append("            <li>Atoms are being inserted.\n");
    }
    if (attributes) {
      step_text.append("            <li>Attributes are being inserted.\n");
    }
    if (relationships) {
      step_text.append("            <li>Relationships are being inserted.\n");
    }
    step_text.append("            </ul>\n");

    if (!comments.equals("")) {
      step_text.append("          Comments: " + comments + "\n");
    }

    return step_text.toString();
  };

  /**
   * This method produces the .csh code for the load step.
   * @return String
   */
  public String toShellScript() {
    ArrayList pre_insert_steps = getParent().getParent().getPreInsertSteps();
    StringBuffer body = new StringBuffer(1000);
    body.append("#\n# Summarize current state of the database, bins wise\n#\n" +
            "echo \"    Run MatrixInit and get bin counts (bin_counts.log) ... `/bin/date`\"\n\n" +
            "$MEME_HOME/bin/bin_counts.csh $db " +
            " >&! bin_counts.log\n\n" +
            "if ($status != 0) then \n" +
            "    echo \"Error running matrixInit and getting bin counts, see bin_counts.log\"\n" +
            "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error running bin_counts, see bin_counts.log\"' \n" +
                    "    exit 1\n" +
            "endif\n");

    // if there are pre_insert steps we call load_section.csh in two parts.
    if (pre_insert_steps.size() == 0) {
		body.append(          "#\n# Load Step\n#\n" +
		          "echo \"    Run Load Step (load_section.log) ... `/bin/date`\"\n\n" +
		          "$MEME_HOME/bin/load_section.csh $authority \"$old_source\" " +
		          "$new_source \"NA\" $db $work_id 3 >&! load_section.log\n\n" +
		          "if ($status != 0) then \n" +
		          "    echo \"Error running load section, see load_section.log\"\n" +
		          "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error running load section, see load_section.log\"' \n" +
		                  "    exit 1\n" +
		          "endif\n");
    }
    else {
      body.append("#\n# Load Step (Part 1)\n#\n");
      body.append(
          "echo \"    Run Load Step Part 1 (load_section.1.log) ... `/bin/date`\"\n\n" +
          "$MEME_HOME/bin/load_section.csh $authority \"$old_source\" " +
          "$new_source \"NA\" $db $work_id 1 >&! load_section.1.log\n\n" +
          "if ($status != 0) then \n" + 
          "    echo \"Error running load section, see load_section.1.log\"\n" +
          "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error running load section, see load_section.1.log\"' \n" +
                  "    exit 1\n" +
          "endif\n\n");

      // handle pre insert steps
      body.append(
          "#################################################################\n");
      body.append("# Pre-insert merging\n");
      body.append(
          "#################################################################\n\n");
      Iterator iter = pre_insert_steps.iterator();
      while (iter.hasNext()) {
        AbstractMergeStep merge = (AbstractMergeStep) iter.next();
        body.append(merge.toShellScriptPreInsert());
        body.append("\n");
      }

      body.append("#\n# Load Step (Part 2)\n#\n");
      body.append(
          "echo \"    Run Load Step Part 2 (load_section.2.log) ... `/bin/date`\"\n\n"+
          "$MEME_HOME/bin/load_section.csh $authority \"$old_source\" " + 
          "$new_source \"NA\" $db $work_id 2 >&! load_section.2.log\n\n" + 
          "if ($status != 0) then \n" +
          "    echo \"Error running load section, see load_section.2.log\"\n" +
          "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error running load section, see load_section.2.log\"' \n" +
                  "    exit 1\n" +
          "endif\n" + 
          "\n" +
          "$MEME_HOME/bin/admin.pl -s refresh_caches -d $db -host $host -port $port >&! /tmp/t.$$.log\n" +
          "if ($status != 0) then\n" +
          "    echo \"Error refreshing server cache\"\n" +
          "    cat /tmp/t.$$.log\n" +
          "    if ($?to == 1)  eval $MEME_HOME/bin/mail.pl $subject_flag $to_from_flags '-message=\"Error refreshing server cache\"' \n" +
                  "    exit 1\n" +
          "endif\n"
            );
    }
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
    return "Load Data into Core Tables";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    private JComboBox jsource_name = new JComboBox();
    private JComboBox jold_source_name = new JComboBox();
    private JTextField jdefault_authority =
        new JTextField(RxConstants.SOURCE_FIELD_LENGTH);
    private JTextArea jcomments = new JTextArea();
    private JComboBox jsource_language = new JComboBox();

    private JCheckBox jconcepts = new JCheckBox("Insert concepts.", true);
    private JCheckBox jclasses = new JCheckBox("Insert classes.", true);
    private JCheckBox jattributes = new JCheckBox("Insert attributes.", true);
    private JCheckBox jrelationships =
        new JCheckBox("Insert relationships.", true);
    private JCheckBox jcontexts = new JCheckBox("Insert contexts.", true);

    /**
     * Constructor
     */
    public View() {
      super();
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      DataChangeListener dcl = new DataChangeListener();

      setLayout(new BorderLayout());
      JPanel data_panel = new JPanel(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.insets = RxConstants.GRID_INSETS;

      constraints.gridy = 0;
      data_panel.add(new JLabel("Source Name"), constraints);
      String[] names = RxToolkit.getSources();
      for (int i = 0; i < names.length; i++) {
        jsource_name.addItem(names[i]);
      }
      ;
      jsource_name.setSelectedItem(names[0]);
      jsource_name.addActionListener(dcl);
      jsource_name.setEditable(false);
      data_panel.add(jsource_name, constraints);

      constraints.gridy++;
      data_panel.add(new JLabel("Default Authority"), constraints);
      jdefault_authority.setDocument(new UppercaseDocument());
      jdefault_authority.getDocument().addDocumentListener(dcl);
      String default_authority = (String) jsource_name.getItemAt(0);
      jdefault_authority.setText(default_authority);
      data_panel.add(jdefault_authority, constraints);

      if (LoadStep.this.getParent().getParent()instanceof UpdateSourceRecipe) {
        constraints.gridy++;
        data_panel.add(new JLabel("Old Source Name"), constraints);
        names = RxToolkit.getSources();
        for (int i = 0; i < names.length; i++) {
          jold_source_name.addItem(names[i]);
        }
        ;
        jold_source_name.setSelectedItem(old_source_name);
        jold_source_name.addActionListener(dcl);
        jold_source_name.setEditable(true);
        data_panel.add(jold_source_name, constraints);
      }

      constraints.gridy++;
      data_panel.add(new JLabel("Source Language"), constraints);
      String[] languages = RxToolkit.DBToolkit.getLanguages();
      for (int i = 0; i < languages.length; i++) {
        jsource_language.addItem(languages[i]);
      }
      ;
      jsource_language.setSelectedItem(source_language);
      jsource_language.addActionListener(dcl);
      data_panel.add(jsource_language, constraints);

      constraints.gridwidth = 2;
      constraints.gridy++;
      jconcepts.addActionListener(dcl);
      data_panel.add(jconcepts, constraints);
      constraints.gridy++;
      jclasses.addActionListener(dcl);
      data_panel.add(jclasses, constraints);
      constraints.gridy++;
      jattributes.addActionListener(dcl);
      data_panel.add(jattributes, constraints);
      constraints.gridy++;
      jrelationships.addActionListener(dcl);
      data_panel.add(jrelationships, constraints);
      constraints.gridy++;
      jcontexts.addActionListener(dcl);
      data_panel.add(jcontexts, constraints);

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
      jsource_name.requestFocus();
    }

    /**
     * This takes values from the step and displays them.
     */
    public void getValues() {
      jsource_name.setSelectedItem(source_name);
      if (LoadStep.this.getParent().getParent()instanceof UpdateSourceRecipe) {
        jold_source_name.setSelectedItem(old_source_name);
      }
      jdefault_authority.setText(default_authority);
      jcomments.setText(comments);
      jsource_language.setSelectedItem(source_language);
      jconcepts.setSelected(concepts);
      jclasses.setSelected(classes);
      jattributes.setSelected(attributes);
      jrelationships.setSelected(relationships);
      jcontexts.setSelected(contexts);
      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      if (LoadStep.this.getParent().getParent()instanceof UpdateSourceRecipe) {
        old_source_name = (String) jold_source_name.getSelectedItem();
        LoadStep.this.parent.getParent().setAttribute(Recipe.
            RX_OLD_SOURCE_ATTRIBUTE, old_source_name);
      }
      source_name = (String) jsource_name.getSelectedItem();
      String root_source_name = RxToolkit.getRootSourceName(source_name);
      LoadStep.this.parent.getParent().setName(source_name);
      LoadStep.this.parent.getParent().setAttribute(Recipe.RX_SOURCE_ATTRIBUTE,
          source_name);
      LoadStep.this.parent.getParent().setAttribute(Recipe.
          RX_ROOT_SOURCE_ATTRIBUTE, root_source_name);
      LoadStep.this.parent.getParent().setDescription(
          "Insertion of source " + source_name + ".");
      default_authority = jdefault_authority.getText();
      LoadStep.this.parent.getParent().setAuthority(default_authority);
      comments = jcomments.getText();
      source_language = (String) jsource_language.getSelectedItem();
      concepts = jconcepts.isSelected();
      classes = jclasses.isSelected();
      attributes = jattributes.isSelected();
      relationships = jrelationships.isSelected();
      contexts = jcontexts.isSelected();
      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      if (LoadStep.this.getParent().getParent()instanceof UpdateSourceRecipe) {
        if (jold_source_name.getSelectedItem() == null ||
            jdefault_authority.getText().equals("") ||
            jsource_name.getSelectedItem() == null) {
          RxToolkit.reportError("You must fill out the 'Source Name,'\n" +
              "'Old Source Name', and 'Default Authority' fields.");
          return false;
        }
      }
      else {
        if (jsource_name.getSelectedItem() == null ||
            jdefault_authority.getText().equals("")) {
          RxToolkit.reportError("You must fill out the 'Source Name'\n" +
                                  "and 'Default Authority' fields.");
          return false;
        }
      }
      return true;
    }
  }; // End of runner class

}; // End of LoadStep class
