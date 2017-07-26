/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.sections
 * Object:     PostMergeSection.java
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
 * This is an implementation of RxSection for the PostMerge section
 * of a source insertion recipe. It supports the following:
 *   1. Resolve STY conflicts
 *   2. Update Releasability of a source
 *   3. Standard Post Merge steps (see step for more info)
 *
 * @author Brian Carlsen
 */

public class PostMergeSection extends RxSection {
  
  /**
   * PostMergeSection constructor
   */
  public PostMergeSection() {
    super();
    RxToolkit.trace("PostMergeSection::PostMergeSection()");
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
   * The following are allowed for insert:
   * 1. AdHocStep
   * 2. ResolveSTYStep
   * 3. UpdateReleasabilityStep 
   *
   * @param current RxStep
   * @return String []
   */
  public String [] getPossibleInsertSteps (RxStep current)
  throws StepNotAllowedException {
    RxToolkit.trace("PostMergeSection::getPossibleInsertSteps()");
    return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".AdHocStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".ResolveSTYStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".UpdateReleasabilityStep"
	};
  };

  /**
   * 1. ResolveSTYStep
   * 2. UpdateReleasabilityStep 
   * 3. StandardPostMergeStep
   *
   * @param current RxStep
   * @return String []
   */
  public String [] getPossibleNextSteps(RxStep current)  {
    RxToolkit.trace("PostMergeSection::getPossibleNextSteps()");

    if (state == 0) {
      state ++;
      return new String [] {
	RxConstants.RX_STEP_PACKAGE_NAME + ".ResolveSTYStep"
	  };
    } else if (state == 1) {
      state ++;
      return new String [] {
        RxConstants.RX_STEP_PACKAGE_NAME + ".UpdateReleasabilityStep"
      };
    } else if (state == 2) {
      state ++;
      return new String [] {
	RxConstants.RX_STEP_PACKAGE_NAME + ".StandardPostMergeStep"
      };
    } else {
      return new String [] {};
    }
  }

  /**
   * This method notifies sections when steps are skipped.
   * @param skipped_step RxStep
   */
  public void stepSkipped( RxStep skipped_step) {
    RxToolkit.trace("PostMergeSection::stepSkipped()");
  };

  /**
   * This method is used to notify sections when steps have 
   * been set as it may affect the logic of what step comes next
   *
   * @param skipped_step RxStep
   */
  public void stepSet( RxStep set_step) {
    RxToolkit.trace("PostMergeSection::stepSet()");
  };

  /**
   * Returns true if more steps can be added to this section via
   * getPossibleNextSteps();
   * @return boolean
   */
  public boolean allowsMoreSteps() {
    RxToolkit.trace("PostMergeSection::allowsMoreSteps()");
    return state < 3;
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
    return "Post-Merge Section";
  };


}

