/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     GenericStep.java
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;


/**
 * This is a template step
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class GenericStep
    extends RxStep {

  protected String ad_hoc_text;
  protected String ad_hoc_command;
  protected boolean run_command;
  protected String comments;

  /**
   * GenericStep default constructor
   */
  public GenericStep() {
    super();
    RxToolkit.trace("GenericStep::GenericStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("GenericStep::resetValues()");

  };

  /**
   * Create & return an instance of inner class View
   *
   * @return RxStep.View
   */
  public RxStep.View constructView() {
    RxToolkit.trace("GenericStep::constructView()");
    return new GenericStep.View();
  };

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public void getHelp() {
    RxToolkit.trace("GenericStep::getHelp()");
    RxToolkit.notifyUser("Help is not currently available.");
  };

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public String toHTML() {
    RxToolkit.trace("GenericStep::toHTML()");
    StringBuffer step_text = new StringBuffer();

    // put html here

    /*if (!comments.equals("")) {
      step_text.append("          Comments: " + comments + "\n");
         }
     */
    return step_text.toString();
  };

  /**
   * This method generates code for a shell script
   * to perform the post-insert merge operation
   */
  public String toShellScript() {
    return "#\n# Override This!\n#\n";
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
    RxToolkit.trace("GenericStep::typeToString()");
    return "Generic Step";
  };

  /**
   * Inner class returned by getView();
   */
  public class View
      extends RxStep.View {

    //
    // View Fields
    //

    /**
     * Constructor
     */
    public View() {
      super();
      RxToolkit.trace("GenericStep.View::View()");
      initialize();
    };

    /**
     * This sets up the JPanel
     */
    private void initialize() {
      RxToolkit.trace("GenericStep.View::initialize()");

    };

    //
    // Implementation of RxStep.View methods
    //

    /**
     * Set the focus
     */
    public void setFocus() {
    }

    /**
     * This takes values from the step and displays them.
     */
    public void getValues() {
      RxToolkit.trace("GenericStep.View::getValues()");

      has_data_changed = false;
    }

    /**
     * This method is overridden by subclasses.
     * It takes a step and puts the values from the GUI.
     */
    public void setValues() {
      RxToolkit.trace("GenericStep.View::setValues()");
      GenericStep.this.parent.getParent().setAuthority("DEFAULT");
      GenericStep.this.parent.getParent().setDescription("Generic Step");
      has_data_changed = false;
    };

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public boolean checkUserEntry() {
      RxToolkit.trace("GenericStep.View::checkUserEntry()");
      return true;
    };

  }

}
