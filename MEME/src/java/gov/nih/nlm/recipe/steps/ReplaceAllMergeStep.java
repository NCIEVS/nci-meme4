/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     ReplaceAllMergeStep.java
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.IntegrityVector;
import gov.nih.nlm.recipe.Recipe;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxToolkit;

import java.util.ArrayList;


/**
 * This step is used to generate and process facts.
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class ReplaceAllMergeStep
    extends GeneratedMergeStep {

  /**
   * Constructor.
   */
  public ReplaceAllMergeStep() {
    super();
    RxToolkit.trace("ReplaceAllMergeStep::ReplaceAllMergeStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("ReplaceAllMergeStep::resetValues()");

    // source termgroups are the ones from termgroups.src
    try {
      source_termgroups = RxToolkit.getFromRanksFile(RxConstants.TERMGROUP);
    }
    catch (Exception e) {
      source_termgroups = new String[0];
    }
    source_options.clear();
    source_options.put("INCLUDE", "");
    //source_options.put("EXCLUDE_LIST", "");
    //source_options.put("NORM_EXCLUDE_LIST", "");
    target_termgroups = new String[0];
    if (RxToolkit.getRecipe() != null) {
      String old_source = (String) RxToolkit.getRecipe().getAttribute(Recipe.
          RX_OLD_SOURCE_ATTRIBUTE);
      ArrayList target = new ArrayList();
      try {
        String[] tmp = RxToolkit.getTermgroups();
        for (int i = 0; i < tmp.length; i++) {
          if (tmp[i].startsWith(old_source)) {
            target.add(tmp[i]);
          }
        }
        target_termgroups = (String[]) target.toArray(new String[0]);
      }
      catch (Exception e) {
        target_termgroups = new String[0];
      }
    }
    target_options.clear();
    target_options.put("INCLUDE", "");
    //target_options.put("EXCLUDE_LIST", "");
    //target_options.put("NORM_EXCLUDE_LIST", "");
    string_match = "BOTH";
    code_match = "EXACT";
    saui_match = "NONE";
    scui_match = "NONE";
    sdui_match = "NONE";
    tty_match = "NONE";
    filters = new ArrayList();
    integrity_vector = new IntegrityVector();
    include_or_exclude = true;
    all_checks_set = true;
    if (RxToolkit.getRecipe() != null) {
      merge_set = (String) RxToolkit.getRecipe().getAttribute(Recipe.
          RX_ROOT_SOURCE_ATTRIBUTE) + "-REPL";
    }
    demote = true;
    change_status = true;
    exclude_safe_replacement = false;
    pre_insert = RxConstants.PI_ONE_TO_ONE;
    comments = "Merge all new termgroups with all old termgroups";
  };

  /**
   * This method returns an HTML representation of the step type
   * for use in rendering the section header for a recipe step
   * @return String
   */
  public String typeToHTML() {
    return "<h3>" + typeToString() + "</h3>";
  };

  /**
   * This method returns an string representation of the step type
   * for use in rendering the step in JLists and JTables.
   * @return String
   */
  public String toString() {
    return "Replacement Merge Step: " + " " + merge_set;
  };

  /**
   * This method returns a descriptive name for the type of step.
   * @return String
   */
  public static String typeToString() {
    RxToolkit.trace("ReplaceAllMergeStep::typeToString()");
    return "Merge Step - Replace all termgroups";
  };

}
