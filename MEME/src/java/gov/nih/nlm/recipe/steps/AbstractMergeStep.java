/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     AbstractMergeStep.java
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.IntegrityVector;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxStep;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This abstract class is used to combine the functionality
 * of Generated and Precomputed merge steps
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public abstract class AbstractMergeStep
    extends RxStep {

  //
  // Constants
  //

  //
  // Fields
  //
  protected String merge_set;
  protected boolean demote;
  protected boolean change_status;
  protected boolean exclude_safe_replacement;
  protected String pre_insert = RxConstants.PI_NONE;
  protected ArrayList filters;
  protected IntegrityVector integrity_vector;
  protected boolean include_or_exclude;
  protected boolean all_checks_set;
  protected String comments;
  protected HashMap target_options = new HashMap();
  protected HashMap source_options = new HashMap();
  protected String merge_tid;
  protected String demotion_tid;

  /**
   * Generate code for the pre-insert merge step.
   */
  public abstract String toShellScriptPreInsert();

}
