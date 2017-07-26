/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     RxSection.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:    This object is used to group RxSteps in a recipe
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * The abstract RxSection object contains an ArrayList of
 * RxStep objects representing the different step of the recipe.
 *  This object is subclassed and not used directly.
 * 
 * @author Brian Carlsen, Owen J. Carlsen, Yun-Jung Kim
 * @version 1.5
 */

public abstract class RxSection implements Serializable {
  
  // Fields
 
  protected ArrayList steps = new ArrayList();
  protected String name; // Name of the section?
  protected transient RxSection.View current_view = null;
  protected int state = 0; // used by subclasses to track state
  protected Recipe parent = null;
  protected boolean allows_more = true;

  /**
   * RxSection constructor
   */
  public RxSection() {
    super();
    RxToolkit.trace("RxSection::RxSection()");
  }

  /**
   * RxSection constructor - sets the section's type and name; 
   * creates the section view.
   */
  public RxSection(String name) {
    this.name = name;
    RxToolkit.trace("RxSection::RxSection()");
  }

  /**
   * RxSection constructor for loading from XML
   */
  public RxSection(HashMap hm) {
    RxToolkit.trace("RxSection::RxSection()");
    this.name = (String)hm.get("name");
    this.state = Integer.valueOf((String)hm.get("state")).intValue();
    this.allows_more = Boolean.valueOf((String)hm.get("allows_more")).booleanValue();
  }

  //
  // Accessors
  //

  /**
   * This method determines if a particular type of step
   * is in the steps vector
   * @param c String
   * @return boolean
   */
  public boolean containsInstanceOf(String c) {
    Iterator iter = steps.iterator();
    while (iter.hasNext()) {
      if (iter.next().getClass().getName().equals(c)) {
	return true;
      }
    }
    return false;
  }


  /** This method returns the section containing this step
   * @return Recipe
   */
  public Recipe getParent() { return parent; };

  /** This method returns the section containing this step
   * @param p Recipe
   */
  public void setParent(Recipe p) { parent = p; };

  //
  // Abstract Methods
  //

  /**
   * This abstract method returns HTML which serves as 
   * a section header when the section is rendered in an HTML document
   * @return String
   */
  public abstract String typeToHTML ();

  /**
   * This abstract method returns a String [] containing
   * class names of the steps that can be inserted
   * This list is presented to the user when inserting a new step
   * @param current RxStep
   * @return String []
   */
  public abstract String [] getPossibleInsertSteps (RxStep current)
  throws StepNotAllowedException;

  /**
   * This method returns a String [] of class names
   * of the steps that can come "next".
   * @param current RxStep
   * @return String []
   */
  public abstract String [] getPossibleNextSteps(RxStep current);

  /**
   * This method is used to notify sections when steps have 
   * been skipped as it may affect the logic of what step comes next
   * @param skipped_step RxStep
   */
  public abstract void stepSkipped( RxStep skipped_step);

  /**
   * This method is used to notify sections when steps have 
   * been set as it may affect the logic of what step comes next
   * @param skipped_step RxStep
   */
  public abstract void stepSet( RxStep set_step);

  /**
   * Abstract method returns true if more steps are allowed to be added
   * @return boolean
   */
  public abstract boolean allowsMoreSteps();

  /**
   * This method returns the section view.  It is abstract but should
   * probably be overridden in the following way:
   *
   * public RxSection.View getView () {
   *   return new RxSection.View(typeToString());
   * }   
   * 
   * @return RxSection.View
   */
  public abstract RxSection.View getView ();

  //
  // Standard RxSection methods.
  //

  /**
   * Get HTML representation of recipe, name
   * @return String
   */ 
  public String toHTML () {
    StringBuffer body = new StringBuffer();
    body.append("        <ol>\n");
    Iterator iter = steps.iterator();
    while (iter.hasNext()) {

      RxStep _step = (RxStep)iter.next();

      body.append("          <li>\n");
      body.append("            " + _step.typeToHTML() + "\n");
      body.append(_step.toHTML());
      
      // Also print execution statistics when available!!!
      body.append(_step.getStatus().toHTML());

      body.append("          <br>\n");
      body.append("          </li>\n");
    }
    body.append("        </ol>\n");
    return body.toString();
  };

  /**
   * Get .csh script representation of recipe.
   * @return String
   */ 
  public String toShellScript () {
    StringBuffer body = new StringBuffer(); 
    Iterator iter = steps.iterator();
    while (iter.hasNext()) {
      RxStep _step = (RxStep)iter.next();
      body.append("\n");
      body.append(_step.toShellScript());
      body.append("\n");
    }
    return body.toString();
  };

  /**
   * This static method returns a name describing the section
   * This is used by anything that dynamically loads the class
   * To see a string representation of what the class is
   * @return String
   */
  public static String typeToString () {
    return "Generic RxSection, this should be overridden.";
  };

  /**
   * This method returns the step after the one passed in.
   * If there is no next step, it throws an exception.
   * StepNotFoundException is thrown if more steps are allowed,
   * EndOfSectionException is thrown if no more steps are allowed.
   * @return RxStep
   * @param current RxStep
   */
  public RxStep getStepAfter( RxStep current )
    throws StepNotFoundException, EndOfSectionException {
    RxToolkit.trace("RxSection::getStepAfter("+current+")");

    int index = steps.indexOf(current);
    if (index == steps.size()-1) {
      if (allowsMoreSteps()) {
	throw new StepNotFoundException();
      } else {
	throw new EndOfSectionException();
      }
    };
    RxToolkit.trace("RxSection::getStepAfter - returning step @ index "+(index+1));
    return (RxStep)steps.get(index+1);
  };

  /**
   * This method returns the step before the current one
   * or it throws StepNotFoundException
   * @param current RxStep
   * @return RxSTep
   */
  public RxStep getStepBefore( RxStep current )
    throws StepNotFoundException {
    RxToolkit.trace("RxSection::getStepBefore("+current+")");

    int index = steps.indexOf(current);
    if (index < 1) {
      throw new StepNotFoundException();
    }

    return (RxStep)steps.get(index-1);
  };

  /**
   * This method returns the first step
   * @return RxStep
   */
  public RxStep getFirstStep() throws IndexOutOfBoundsException {
    RxToolkit.trace("RxSection::getFirstStep()");
    return (RxStep)steps.get(0);
  }

  /**
   * This method returns the last step
   * @return RxStep
   */
  public RxStep getLastStep() throws IndexOutOfBoundsException {
    RxToolkit.trace("RxSection::getLastStep()");
    return (RxStep)steps.get(steps.size()-1);
  }

  /**
   * This method returns the index of a step in the steps Arraylist
   * @param current RxStep
   * @return int
   */
  public int indexOf( RxStep current ) {
    return steps.indexOf(current);
  };

  /**
   * Returns true if the secton contains no steps
   * @return boolean
   */
  public boolean isEmpty () {
    RxToolkit.trace("RxSection::isEmpty()");
    return steps.isEmpty();
  }

  /**
   * Returns true if the secton contains the object RxStep passed in
   * @param step RxStep
   * @return boolean
   */
  public boolean contains (RxStep step) {
    RxToolkit.trace("RxSection::contains()");
    return steps.contains(step);
  }

  /**
   * This method inserts a RxStep at the end of the steps array
   * @param step RxStep
   */
  public void addRxStep (RxStep step) {
    RxToolkit.trace("RxSection::addRxStep("+step+")");
    if(steps.indexOf(step) == -1)
      steps.add(step);
    else
      RxToolkit.reportError("Can not add step that already exists");
  };

  /**
   * This method inserts a RxStep at the end of the steps array
   * @param index int
   * @param step RxStep
   */
  public void addRxStep (int index, RxStep step) {
    RxToolkit.trace("RxSection::addRxStep("+index+","+step+")");
    steps.add(index,step);
  };

  /**
   * This method deletes the step at the index 
   * @param index int
   */
  public void deleteStep(int index) {
    RxToolkit.trace("RxSection::deleteStep("+index+")");
    steps.remove(index);
  };

  /**
   * This method deletes step matching the one passed in
   * @param step RxStep
   */
  public void deleteStep(RxStep step) throws StepNotFoundException {
    RxToolkit.trace("RxSection::deleteStep("+step+")");
    int index = steps.indexOf(step);
    if (index == -1) {
      throw new StepNotFoundException ("Step (" + step + ") not found in section (" +
			   this + ")");
    }
    deleteStep(index);
  };

  /**
   * This method returns an Object []
   * @return ArrayList
   */
  public ArrayList getSteps() {
    RxToolkit.trace("RxSection::getSteps");
    return steps;
  };

  /**
   * This method copys this.steps into the passed in Arraylist
   * @param steps ArrayList
   */
  public void copySteps( ArrayList steps ) {
    RxToolkit.trace("RxSection::copySteps("+steps+")");
    steps = this.steps;
  };


  /**
   * Inner class defining the RxSection's own View. 
   * This inner class should be "overridden" by subclasses
   */
  public class View extends JPanel {

    private JLabel section_name = null;
    private JLabel step_status_label = null;
    private JLabel step_status = null;
    public RxStep.View current_step_view = null;
    
    /**
     * Default Constructor
     */
    public View() {
      super(new BorderLayout());
      this.section_name = new JLabel("",JLabel.CENTER);
      this.step_status_label = new JLabel("Step Status:",JLabel.RIGHT);
      this.step_status = new JLabel("",JLabel.LEFT);
      initialize();
    }

    /**
     * Constructor
     */
    public View(String section_name) {
      super(new BorderLayout());
      this.section_name = new JLabel(section_name,JLabel.CENTER);
      this.step_status_label = new JLabel("Step Status:",JLabel.RIGHT);
      this.step_status = new JLabel("",JLabel.LEFT);
      initialize();
    }
    
    /**
     * Initialize the class.
     */
    private void initialize() {
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.fill = GridBagConstraints.BOTH;
      
      JPanel header = new JPanel(new GridBagLayout());
      constraints.insets = new Insets(20,20,20,20);
      header.add(section_name, constraints);
      constraints.insets = new Insets(20,0,20,0);
      header.add(step_status_label, constraints);
      constraints.insets = new Insets(20,0,20,20);
      header.add(step_status, constraints);
      
      add(header, BorderLayout.NORTH);
    }
  
    /**
     * This method sets the section's name in the GUI. It is used by updating the
     * sectionView panel as the recipe moves along different sections.
     * @param text String
     */
    public void setSectionName(String text) {
      section_name.setText(text);
    }
    
    /**
     * This method sets the text for step_status in the GUI. It is used to update the
     * status of the step as the user moves along different steps. The status is
     * color-coded;
     * @param text String
     */
    public void setStepStatus(int status) {
      step_status.setText(RxConstants.stepStatusToString(status));
      step_status.setForeground(RxConstants.stepStatusToColor(status));
    }

  }
  
}
