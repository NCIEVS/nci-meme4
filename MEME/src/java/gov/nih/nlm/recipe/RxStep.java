/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.RxStep;
 * Interface:      RunnableRxStep.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:    This abstract class defines the recipe step API
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import gov.nih.nlm.util.ObjectXMLSerializer;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.HashMap;

import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;


/**
 * This abstract class is the recipe step API
 * It must be subclassed and its abstract methods overridden
 *
 * @author Brian Carlsen, Owen J. Carlsen, Yun-Jung Kim
 * @version 1.5
 */
public abstract class RxStep implements Serializable {
  
  //
  // Fields
  //

  // The status contains info about the step during editing and execution.
  protected RxStepStatus status = null;

  // Parent section
  protected RxSection parent = null;

  // View reference
  protected transient RxStep.View view = null;

  //
  // Constructors
  //

  /**
   * RxStep default constructor
   */
  public RxStep() {
    RxToolkit.trace("RxStep::RxStep(.)");
    status = new RxStepStatus(this);
  };
  
  /**
   * RxStep constructor for loading from XML
   */
  public RxStep(HashMap hm) {
    this();
    RxToolkit.trace("RxStep::RxStep(.)");
  };
  

  //
  // Accessors
  //

  /*
   * This method returns the work_id for the recipe
   * @return int
   */
  public int getWorkID() { 
    return parent.getParent().getWorkID();
 };

  /** This method returns the default authority for the recipe
   * @return string
   */
  public String getAuthority() { 
    return parent.getParent().getAuthority();
 };

  /** This method returns the ENG authority for the recipe
   * @return string
   */
  public String getENGAuthority() { 
    return parent.getParent().getENGAuthority();
  };

  /** This method adds the step to the pre-insert list
   */
  public void addPreInsertStep() {
    // remove it first, so it only appears once.
    parent.getParent().removePreInsertStep(this);
    parent.getParent().addPreInsertStep(this);
  };

  /** This method removes the step from the pre-insert list
   */
  public void removePreInsertStep() {
    parent.getParent().removePreInsertStep(this);
  };
   

  /** This method returns the section containing this step
   * @return RxSection
   */
  public RxSection getParent() { return parent; };

  /** This method returns the section containing this step
   * @param p RxSection
   */
  public void setParent(RxSection p) { parent = p; };

  /**
   * This method returns the editing state of the current step
   * @return int
   */
  public RxStepStatus getStatus() {
    return status;
  }
  
  /**
   * This method returns RxStep.View to the caller.
   * @return RxStep.View
   */
  public RxStep.View getView() {
    if (view == null) {
      view = constructView();
    }
    return view; 
  };

  /**
   * This method calls the checkpoint method which saves the recipe.
   */
  public void checkpointRecipe(){
    parent.getParent().checkpoint(); 
  };

  /**
   * This method returns an XML representation of the recipe.
   * @param String indent    (No longer in use)
   * @return String 
   *
  public String fieldsToXML(String indent) {
    MEMEToolkit.trace("RxStep::fieldsToXML()");
    StringBuffer sb = new StringBuffer();
    sb.append(indent);
    sb.append("<Field name=\"status\">");
    sb.append(status.fieldsToXML(indent + "  "));
    sb.append("</Field>\n");
    return sb.toString();
  }
   */

  //
  //
  // Abstract Methods (overridden by subclasses)
  //

  /**
   * This method is overridden by subclasses and sets any
   * subclass-specific fields to their default values
   */
  public abstract void resetValues();
  
 /**
   * This method returns RxStep.View to the caller.
   *
   * @return RxStep.View
   */
  public abstract RxStep.View constructView();

  /**
   * This method generates some kind of online help and
   * then returns. Most simply it will produce a dialog box.
   */
  public abstract void getHelp();

  /**
   * This method will be overridden by the subclasses' method.
   * It returns the HTML representation of the step.
   * @return String
   */
  public abstract String toHTML();

  public String toShellScript() {
    return "# " + typeToString();
  }

  /**
   * This method returns an HTML representation of the step type
   * for use in rendering the section header for a recipe step
   * @return String
   */
  public abstract String typeToHTML();

  /**
   * This method returns a descriptive name for the type of step.
   * Although not static it should be overridden by subclasses.
   * @return String
   */
  public static String typeToString() {
    return "Generic Step:: this method should be overridden";
  };

  /**
   * Inner class defining an abstrace RxStep view
   * The getView method of the subclasses must return
   * an instance of this class with all of the abstract
   * methods implemented.
   *
   */
  public abstract class View extends javax.swing.JPanel implements 
     ObjectXMLSerializer.NotPersistentIfTransient {

    //
    // Fields
    //
    public boolean has_data_changed = false;

    //
    // Inner classes
    //
    public class DataChangeListener 
      implements java.awt.event.ActionListener,
                 javax.swing.event.DocumentListener,
                 javax.swing.event.ListSelectionListener{
      
      // constructor
      public DataChangeListener () {
      };

      // implementation of ActionListener interface
      public void actionPerformed (ActionEvent e) {
	has_data_changed = true;
      }

      // implementation of ListSelectionListener interface
      public void valueChanged (ListSelectionEvent e) {
	has_data_changed = true;
      }

      // implementation of DocumentListener interface
      public void changedUpdate (DocumentEvent event) {
	has_data_changed = true;
      };

      public void removeUpdate (DocumentEvent event) {
	has_data_changed = true;
      };

      public void insertUpdate (DocumentEvent event) {
	has_data_changed = true;
      };

    }

    /**
     * This method determines if the view matches the object
     * @return boolean
     */
    public boolean hasDataChanged() {
      return has_data_changed;
    }

    //
    // Abstract methods
    //

    /**
     * This method sets the focus for the panel
     * have the focus first
     */
    public abstract void setFocus();


    /**
     * This takes values from the step and displays them.
     */
    public abstract void getValues();
    
    /**
     * This method is overridden by subclasses. 
     * It takes a step and puts the values from the GUI.
     */
    public abstract void setValues();

    /**
     * This method is overridden by subclasses
     * It validates the input with respect to the underlying step
     */
    public abstract boolean checkUserEntry();

  }

}
  
