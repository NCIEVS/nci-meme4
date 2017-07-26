/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.sections
 * Object:     MergeSection.java
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

import java.util.HashMap;


/**
 * This is an implementation of RxSection for the Merge section
 * of a source insertion recipe.  
 * 
 * @author Brian Carlsen
 * @version 1.0
 */

public abstract class MergeSection extends RxSection {
  
  /**
   * MergeSection constructor
   */
  public MergeSection() {
    super();
    RxToolkit.trace("MergeSection::MergeSection()");
  }

  /**
   * MergeSection constructor for loading from XML
   */
  public MergeSection(HashMap hm) {
    super(hm);
    RxToolkit.trace("MergeSection::MergeSection()");
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
   * For a MergeSection, the same steps allowed as "next" steps
   * are available for insertion, with the addition of ad_hoc_steps;
   * @param current RxStep
   * @return String []
   */
  public String [] getPossibleInsertSteps (RxStep current)
  throws StepNotAllowedException {
    RxToolkit.trace("MergeSection::getPossibleInsertSteps()");
    return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".AdHocStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".BinaryIntegrityCheckStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".UnaryIntegrityCheckStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".GeneratedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".MIDMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".ReplaceAllMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".PrecomputedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".GeneratedRelationshipMatchStep"
	};
  };

  /**
   * MergeSections can have steps in any order.
   * 1. UnaryIntegrityCheckStep (insert or delete checks).
   * 2. BinaryIntegrityCheckStep (insert or delete checks).
   * 3. PrecomputedMergeStep (mergefacts.src).
   * 4. GeneratedMergeStep   (generate and merge facts).
   * 5. GeneratedRelationshipMatchStep
   *
   * @param current RxStep
   * @return String []
   */
  public String [] getPossibleNextSteps(RxStep current) {
    RxToolkit.trace("MergeSection::getPossibleNextSteps()");
    return new String [] {
      RxConstants.RX_STEP_PACKAGE_NAME + ".BinaryIntegrityCheckStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".UnaryIntegrityCheckStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".GeneratedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".MIDMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".PrecomputedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".GeneratedRelationshipMatchStep"
	};
  }

  /**
   * Nothing required.
   * @param skipped_step RxStep
   */
  public void stepSkipped( RxStep skipped_step) {
    RxToolkit.trace("MergeSection::stepSkipped()");
  };

  /**
   * This method is used to notify sections when steps have 
   * been set as it may affect the logic of what step comes next
   * Nothing required.
   * @param skipped_step RxStep
   */
  public void stepSet( RxStep set_step) {
    RxToolkit.trace("MergeSection::stepSet()");
  };

  /**
   * This type of section always allows more steps
   * @return boolean
   */
  public boolean allowsMoreSteps() {
    RxToolkit.trace("MergeSection::allowsMoreSteps()");
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
    return "Merge Section";
  };


}

