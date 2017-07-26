/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     MIDMergeStep.java
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

public class MIDMergeStep
    extends GeneratedMergeStep {

  /**
   * Constructor.
   */
  public MIDMergeStep() {
    super();
    RxToolkit.trace("MIDMergeStep::MIDMergeStep()");
    resetValues();
  };

  /**
   * This method resets the internal values to defaults
   */
  public void resetValues() {
    RxToolkit.trace("MIDMergeStep::resetValues()");

    // source termgroups are the ones from termgroups.src
    try {
      source_termgroups = RxToolkit.getFromRanksFile(RxConstants.TERMGROUP);
    }
    catch (Exception e) {
      source_termgroups = new String[0];
    }
    source_options.clear();
    source_options.put("INCLUDE", "");
    source_options.put("EXCLUDE_LIST", "");
    source_options.put("NORM_EXCLUDE_LIST", "");
    source_options.put("NEW_ATOMS_ONLY", "");
    target_termgroups = new String[0];
    if (RxToolkit.getRecipe() != null) {
      // We want to exclude new/old termgroups
      try {
        String new_sab =
            (String) RxToolkit.getRecipe().getAttribute(Recipe.
            RX_SOURCE_ATTRIBUTE);
        String old_sab =
            (String) RxToolkit.getRecipe().getAttribute(Recipe.
            RX_OLD_SOURCE_ATTRIBUTE);
        String[] tmp = RxToolkit.getTermgroups();
        ArrayList target = new ArrayList();
        for (int i = 0; i < tmp.length; i++) {
          if (tmp[i].startsWith(new_sab) ||
              (old_sab != null && tmp[i].startsWith(old_sab))) {
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
    target_options.put("EXCLUDE", "");
    target_options.put("EXCLUDE_LIST", "");
    target_options.put("NORM_EXCLUDE_LIST", "");
    string_match = "BOTH";
    code_match = "NONE";
    saui_match = "NONE";
    scui_match = "NONE";
    sdui_match = "NONE";
    filters = new ArrayList();
    integrity_vector = new IntegrityVector();
    include_or_exclude = true;
    all_checks_set = true;
    if (RxToolkit.getRecipe() != null) {
      merge_set = (String) RxToolkit.getRecipe().getAttribute(Recipe.
          RX_ROOT_SOURCE_ATTRIBUTE) + "-MID";
    }
    demote = true;
    change_status = true;
    exclude_safe_replacement = true;
    pre_insert = RxConstants.PI_NONE;
    comments = "";
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
    return "MID Merge Step: " + " " + merge_set;
  };

  /**
   * This method returns a descriptive name for the type of step.
   * @return String
   */
  public static String typeToString() {
    RxToolkit.trace("MIDMergeStep::typeToString()");
    return "Merge Step - MID Merging";
  };

}
