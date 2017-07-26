/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.sections
 * Object:     NewMergeSection.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.sections;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;


/**
 * This is an implementation of RxSection for the New Merge section
 * of a source insertion recipe.  
 * 
 * @author Brian Carlsen
 * @version 1.0
 */

public class NewMergeSection extends MergeSection {
  
  /**
   * MergeSection constructor
   */
  public NewMergeSection() {
    super();
    RxToolkit.trace("NewMergeSection::NewMergeSection()");
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
   * UpdateMergeSections can have steps in any order.
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
      RxConstants.RX_STEP_PACKAGE_NAME + ".ConditionalPrecomputedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".UnconditionalPrecomputedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".PrecomputedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".GeneratedRelationshipMatchStep"
	};
  }

  /** String representation
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
    return "New Merge Section";
  };


}

