/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     ConditionalPrecomputedMergeStep.java
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.IntegrityVector;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxToolkit;

import java.util.ArrayList;


/**
 * This step is used to generate and process facts.
 *
 * @author Brian Carlsen
 */

public class ConditionalPrecomputedMergeStep
    extends PrecomputedMergeStep {

  /**
   * Constructor.
   */
  public ConditionalPrecomputedMergeStep() {
    super();
    RxToolkit.trace("ConditionalPrecomputedMergeStep::ConditionalPrecomputedMergeStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("ConditionalPrecomputedMergeStep::resetValues()");

    String[] merge_sets = RxToolkit.getMergeSetsFromFile();
    merge_set = merge_sets[0];
    demote = true;
    change_status = true;
    exclude_safe_replacement = false;
    filters = new ArrayList();
    integrity_vector = new IntegrityVector();
    all_checks_set = true;
    include_or_exclude = true;
    pre_insert = RxConstants.PI_ONE_TO_ONE;
    comments = "";
    source_options.clear();
    target_options.clear();
   };

   /**
    * This method returns an string representation of the step type
    * for use in rendering the step in JLists and JTables.
    * @return String
    */
   public String toString() {
     return "Conditional Merge Step: " + " " + merge_set;
   };

   /**
    * This method returns a descriptive name for the type of step.
    * @return String
    */
   public static String typeToString() {
     RxToolkit.trace("PrecomputedMergeStep::typeToString()");
     return "Conditional Merge Step - mergefacts.src";
   };

}
