/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     UnconditionalPrecomputedMergeStep.java
 *
 * CHANGES
 * 03/29/2007 JFW (1-DV09X): add MGV_A4 to integrity vector selected by default
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.IntegrityVector;
import gov.nih.nlm.recipe.IntegrityCheck;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxToolkit;

import java.util.ArrayList;


/**
 * This step is used to generate and process facts.
 *
 * @author Brian Carlsen
 */

public class UnconditionalPrecomputedMergeStep
    extends PrecomputedMergeStep {

  /**
   * Constructor.
   */
  public UnconditionalPrecomputedMergeStep() {
    super();
    RxToolkit.trace("UnconditionalPrecomputedMergeStep::UnconditionalPrecomputedMergeStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("UnconditionalPrecomputedMergeStep::resetValues()");

    String[] merge_sets = RxToolkit.getMergeSetsFromFile();
    merge_set = merge_sets[0];
    demote = false;
    change_status = false;
    exclude_safe_replacement = false;
    filters = new ArrayList();
    integrity_vector = new IntegrityVector();
    IntegrityCheck[] ics = RxToolkit.DBToolkit.getIntegrityChecks();
    for (IntegrityCheck ic : ics) {
      if(ic.ic_name.equals("MGV_A4")) {
    	integrity_vector.add(ic);
      }
    }
    all_checks_set = false;
    include_or_exclude = true;
    pre_insert = RxConstants.PI_N_TO_N;
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
     return "Unconditional Merge Step: " + " " + merge_set;
   };

   /**
    * This method returns a descriptive name for the type of step.
    * @return String
    */
   public static String typeToString() {
     RxToolkit.trace("PrecomputedMergeStep::typeToString()");
     return "Unconditional Merge Step - mergefacts.src";
   };
}
