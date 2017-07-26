/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.sections
 * Object:     LoadSection.java
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
import gov.nih.nlm.recipe.StepNotFoundException;
import gov.nih.nlm.recipe.steps.LoadStep;

import java.util.HashMap;


/**
 * This is an implementation of RxSection for the Load section
 * of the insertion recipe for a new source. It supports
 * CRACS and context_relationship insertion.
 * 
 * @author Brian Carlsen
 * @version 1.0
 */

public class LoadSection extends RxSection {

  //
  // Fields
  //

  /**
   * LoadSection constructor
   */
  public LoadSection() {
    super();
    RxToolkit.trace("LoadSection::LoadSection()");
  }

  /**
   * LoadSection constructor for loading from XML
   */
  public LoadSection(HashMap hm) {
    super(hm);
    RxToolkit.trace("LoadSection::LoadSection()");
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
   * Only AdHocSteps are allowed;
   *
   * @param current RxStep
   * @return String []
   */
  public String [] getPossibleInsertSteps (RxStep current)
  throws StepNotAllowedException {
    RxToolkit.trace("LoadSection::getPossibleInsertSteps()");
    return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".AdHocStep"};
  };

  /**
   * Only one step is allowed.
   *
   * @param current RxStep
   * @return String []
   */
  public String [] getPossibleNextSteps(RxStep current) {
    RxToolkit.trace("LoadSection::getPossibleNextSteps()");
      return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".LoadStep"
	};
  }

  /**
   * If the LoadStep is skipped, it is still required
   * @param skipped_step RxStep
   */
  public void stepSkipped( RxStep skipped_step) {
    RxToolkit.trace("LoadSection::skipSet("+
		    skipped_step.getClass()+")");
    if (skipped_step instanceof LoadStep)
      allows_more = true;
  };

  /**
   * This method is used to notify sections when steps have 
   * been set as it may affect the logic of what step comes next
   *
   * If the last step was a LoadStep, don't allow more
   *
   * @param skipped_step RxStep
   */
  public void stepSet( RxStep set_step) {
    RxToolkit.trace("LoadSection::stepSet("+
		    set_step.getClass()+")");
    if (set_step instanceof LoadStep) 
      allows_more = false;
  };

  /**
   * If the last step was a LoadStep, don't allow anymore
   * @return boolean
   */
  public boolean allowsMoreSteps() {
    RxToolkit.trace("LoadSection::allowsMoreSteps()");
    return allows_more;
  }

  /**
   * This method returns the section view. 
   * @return RxSection.View
   */
  public RxSection.View getView () {
    return new RxSection.View(typeToString());
  }   
  
  /**
   * String representation
   * @return String
   */
  public String toString() {
    return typeToString();
  };

  /**
   * type of section
   * @return String
   */
  public static String typeToString () {
    return "Load Section";
  };

  //
  // Overloaded standard RxSection methods
  //
  /**
   * This method deletes step matching the one passed in
   * If the step deleted is a LoadStep, allow more steps
   * @param step RxStep
   */
  public void deleteStep(RxStep step) throws StepNotFoundException {
    RxToolkit.trace("LoadSection::deleteSet("+ step.getClass()+")");
    super.deleteStep(step);
    if (step instanceof LoadStep)
      allows_more=true;
  }

}

