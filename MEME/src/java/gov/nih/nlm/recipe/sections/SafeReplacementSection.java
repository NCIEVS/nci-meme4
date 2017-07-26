/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.sections
 * Object:     SafeReplacementSection.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.sections;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxSection;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
import gov.nih.nlm.recipe.StepNotAllowedException;


/**
 * This is a template
 * 
 * @author Brian Carlsen
 * @version 1.5
 */

public class SafeReplacementSection extends RxSection {
  
  /**
   * SafeReplacementSection constructor
   */
  public SafeReplacementSection() {
    super();
    RxToolkit.trace("SafeReplacementSection::SafeReplacementSection()");
  }

  //
  // Implementation of abstract methods
  //

  /**
   * This method returns HTML which serves as 
   * a section header when the section is rendered in an HTML document
   * @return String
   */
  public String typeToHTML () {
    return "<h2>" + typeToString() + "</h2>";
  }

  /**
   * 
   * @return String []
   */
  public String [] getPossibleInsertSteps (RxStep current)
  throws StepNotAllowedException {
    RxToolkit.trace("SafeReplacementSection::getPossibleInsertSteps()");
    return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".AdHocStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".SourceSafeReplacementStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".TermgroupSafeReplacementStep"};
  };

  /**
   * 
   * @return String []
   */
  public String [] getPossibleNextSteps(RxStep current) {
    RxToolkit.trace("SafeReplacementSection::getPossibleNextSteps()");
    return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".SourceSafeReplacementStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".TermgroupSafeReplacementStep"};
  };

  /**
   * This method is used to notify sections when steps have
   * been skipped, as it may cause a change in the logic
   * Nothing Required.
   * @param skipped_step RxStep
   */
  public void stepSkipped( RxStep skipped_step) {
    RxToolkit.trace("SafeReplacementSection::stepSkipped()");
  };

  /**
   * This method is used to notify sections when steps have 
   * been set as it may affect the logic of what step comes next
   * Nothing Required.
   * @param skipped_step RxStep
   */
  public void stepSet( RxStep set_step) {
    RxToolkit.trace("SafeReplacementSection::stepSet()");
  };

  /**
   * Are more steps allowed?
   * @return boolean
   */
  public boolean allowsMoreSteps() {
    RxToolkit.trace("SafeReplacementSection::allowsMoreSteps()");
    return true;
  }

  /**
   * This method returns the section view. 
   * @return RxSection.View
   */
  public RxSection.View getView () {
    return new RxSection.View(typeToString());
  }   
  
  /**
   * String representation of the Section
   * @return String
   */
  public String toString() {
    return typeToString();
  };

  /**
   * The type of section, for MEMEToolkit.classChooser.
   * @return String
   */
  public static String typeToString () {
    return "Safe Replacement Section";
  };


}

