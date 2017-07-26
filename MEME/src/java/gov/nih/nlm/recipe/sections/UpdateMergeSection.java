/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.sections
 * Object:     UpdateMergeSection.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 * Changes
 *  11/03/2006 BAC (1-COWXF): better support for conditional/unconditional merges.
 *****************************************************************************/
package gov.nih.nlm.recipe.sections;

import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;
import gov.nih.nlm.recipe.RxToolkit;

import java.util.HashMap;


/**
 * This is an implementation of RxSection for the Update Merge section
 * of a source insertion recipe.  
 * 
 * @author Brian Carlsen
 * @version 1.0
 */

public class UpdateMergeSection extends MergeSection {
  
  /**
   * MergeSection constructor
   */
  public UpdateMergeSection() {
    super();
    RxToolkit.trace("UpdateMergeSection::UpdateMergeSection()");
  }

  /**
   * MergeSection constructor for loading from XML
   */
  public UpdateMergeSection(HashMap hm) {
    super(hm);
    RxToolkit.trace("UpdateMergeSection::UpdateMergeSection()");
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
      RxConstants.RX_STEP_PACKAGE_NAME + ".ConditionalPrecomputedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".UnconditionalPrecomputedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".BinaryIntegrityCheckStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".UnaryIntegrityCheckStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".GeneratedMergeStep",
      RxConstants.RX_STEP_PACKAGE_NAME + ".ReplaceAllMergeStep",
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
    return "Update Merge Section";
  };


}

