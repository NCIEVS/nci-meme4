/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.sections
 * Object:     RelationshipMatchSection.java
 * 
 * Author:     Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.sections;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxSection;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;
import gov.nih.nlm.recipe.StepNotAllowedException;


/**
 * This is an implementation of RxSection for the RelationshipMatch section
 * of a source insertion recipe.  
 * 
 * @author Brian Carlsen
 * @version 1.0
 */

public class RelationshipMatchSection extends RxSection {
  
  /**
   * RelationshipMatchSection constructor
   */
  public RelationshipMatchSection() {
    super();
    RxToolkit.trace("RelationshipMatchSection::RelationshipMatchSection()");
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
   * For a RelationshipMatchSection, the same steps allowed as "next" steps
   * are available for insertion, with the addition of adHocSteps
   * @param current RxStep
   * @return String []
   */
  public String [] getPossibleInsertSteps (RxStep current)
  throws StepNotAllowedException {
    RxToolkit.trace("RelationshipMatchSection::getPossibleInsertSteps()");
    return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".AdHocStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".GeneratedRelationshipMatchStep"
	};
  };

  /**
   * RelationshipMatchSections can have only:
   *  GeneratedRelationshipMatchStep   (generate and insert facts)
   *
   * @param current RxStep
   * @return String []
   */
  public String [] getPossibleNextSteps(RxStep current) {
    RxToolkit.trace("RelationshipMatchSection::getPossibleNextSteps()");
    return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".GeneratedRelationshipMatchStep",
	};
  }

  /**
   * Nothing required.
   * @param skipped_step RxStep
   */
  public void stepSkipped( RxStep skipped_step) {
    RxToolkit.trace("RelationshipMatchSection::stepSkipped()");
  };

  /**
   * This method is used to notify sections when steps have 
   * been set as it may affect the logic of what step comes next
   * Nothing required.
   * @param skipped_step RxStep
   */
  public void stepSet( RxStep set_step) {
    RxToolkit.trace("RelationshipMatchSection::stepSet()");
  };

  /**
   * This type of section always allows more steps
   * @return boolean
   */
  public boolean allowsMoreSteps() {
    RxToolkit.trace("RelationshipMatchSection::allowsMoreSteps()");
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
    return "Relationship Match Section";
  };


}

