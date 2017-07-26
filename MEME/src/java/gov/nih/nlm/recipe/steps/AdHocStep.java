/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     AdHocStep.java
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class AdHocStep
    extends RxStep {

  protected String ad_hoc_text;
  protected String ad_hoc_command;
  protected boolean run_command;
  protected String comments;

  /**
   * AdHocStep default constructor
   */
  public AdHocStep() {
    super();
    RxToolkit.trace("AdHocStep::AdHocStep()");
    resetValues();
  };

  /**
   * reset
   */
  public void resetValues() {
    RxToolkit.trace("AdHocStep::resetValues()");
    ad_hoc_text = "";
    ad_hoc_command = "";
    run_command = false;
    comments = "";
  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("AdHocStep::constructView()");
    return new AdHocStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("AdHocStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("AdHocStep::toHTML()");
    StringBuffer step_text = new StringBuffer();
    step_text.append("            <p>\n");
    if (!ad_hoc_text.equals("")) {
      step_text.append("              <pre>")
          .append(ad_hoc_text.replaceAll("<", "&lt;").replaceAll(">", "&gt;"))
          .append("</pre><br>\n");
    }
    if (run_command) {
      step_text.append("              " +
                       "The following command should be run: <tt>" +
                       ad_hoc_command + "</tt><br>\n");
    }
    if (!comments.equals("")) {
      step_text.append("          Comments: " + comments + "\n");
    }
    return step_text.toString();
  };

  /**
   * Produce a place-holder for shell script code to perform this step.
   */
  public String toShellScript() {
    StringBuffer body = new StringBuffer(500);
    body.append("#\n# Ad Hoc Step\n#\n");
    if (comments != null && !comments.equals("")) {
      body.append("# ").append(comments).append("\n");
    }
    body.append(ad_hoc_text);
    body.append("\n");
    if (run_command) {
      body.append("\n").append(run_command).append("\n");
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
    RxToolkit.trace("AdHocStep::typeToString()");
    return "Ad Hoc Step";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    private JTextArea jad_hoc_text = new JTextArea();
    private JTextArea jcomments = new JTextArea();
    private JCheckBox jrun_command = new JCheckBox("");
    private JTextField jad_hoc_command = new JTextField(40);

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("AdHocStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("AdHocStep.View::initialize()");

      DataChangeListener dcl = new DataChangeListener();

      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.insets = RxConstants.GRID_INSETS;

      JPanel data_panel = new JPanel(new GridBagLayout());

      constraints.gridx = 0;
      constraints.gridy = 0;
      data_panel.add(new JLabel("Describe ad hoc step:"), constraints);
      constraints.gridy = GridBagConstraints.RELATIVE;

      jad_hoc_text.setRows(10);
      JScrollPane jtext_scroll = new JScrollPane();
      jad_hoc_text.getDocument().addDocumentListener(dcl);
      jtext_scroll.setViewportView(jad_hoc_text);
      data_panel.add(jtext_scroll, constraints);

      JPanel command_pane = new JPanel(new BorderLayout());
      jrun_command.addActionListener(dcl);
      command_pane.add(jrun_command, BorderLayout.WEST);
      jad_hoc_command.getDocument().addDocumentListener(dcl);
      command_pane.add(jad_hoc_command, BorderLayout.EAST);

      data_panel.add(new JLabel("Ad Hoc Script:"), constraints);
      data_panel.add(command_pane, constraints);

      data_panel.add(new JLabel("Comments:"), constraints);
      jcomments.setRows(3);
      jcomments.getDocument().addDocumentListener(dcl);
      data_panel.add(jcomments, constraints);

      setLayout(new BorderLayout());
      add(data_panel, BorderLayout.CENTER);

    };

    //
    // Implementation of RxStep.View methods
    //

    /**
     * Set the focus
     */
    public void setFocus() {
      jad_hoc_text.requestFocus();
    }

    /**
     * This method determines if the view matches the object
     * No need to override.
     */
    public boolean hasDataChanged() {
      RxToolkit.trace("AdHocStep.View::hasDataChanged()");
      return super.hasDataChanged();
    };

    /**
     * This takes values from the step and displays them.
     */
    public void getValues() {
      RxToolkit.trace("AdHocStep.View::getValues()");
      jad_hoc_text.setText(ad_hoc_text);
      jcomments.setText(comments);
      jrun_command.setSelected(run_command);
      jad_hoc_command.setText(ad_hoc_command);
      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("AdHocStep.View::setValues()");
      ad_hoc_text = jad_hoc_text.getText();
      comments = jcomments.getText();
      run_command = jrun_command.isSelected();
      ad_hoc_command = jad_hoc_command.getText();
      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("AdHocStep.View::checkUserEntry()");

      // IF jrun_command is selected jad_hoc_command cannot be empty
      // IF jrun_command is not selected, jad_hoc_text cannot be empty
      if (jrun_command.isSelected()) {
        if (jad_hoc_command.getText().equals("")) {
          RxToolkit.reportError("Please specify a script.");
          return false;
        }
      }
      else {
        if (jad_hoc_text.getText().equals("")) {
          RxToolkit.reportError("You must either describe the step\n" +
                                  "or specify a command script.");
          return false;
        }

      }

      // if jrun_command is not selected and jad_hoc_command
      // not empty, confirm that script should not be run
      if (!jrun_command.isSelected() &&
          !jad_hoc_command.getText().equals("")) {
        boolean response = RxToolkit.confirmRequest(
            "You have entered a command but have left the\n" +
            "checkbox unselected, indicating that you do not want\n" +
            "to run this command.  Is this correct?");
        if (!response) {
          return false;
        }
      }
      return true;
    }

  }

}
