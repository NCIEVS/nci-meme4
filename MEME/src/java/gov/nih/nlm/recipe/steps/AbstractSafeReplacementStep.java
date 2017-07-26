/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     AbstractSafeReplacementStep.java
 *
 *****************************************************************************/
package gov.nih.nlm.recipe.steps;

import gov.nih.nlm.recipe.RxStep;

import java.util.ArrayList;

/**
 * This abstract class is used to combine the functionality
 * of SourceSafeReplacement and TermgroupSafeReplacement steps
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public abstract class AbstractSafeReplacementStep
    extends RxStep {

  //
  // Constants
  //

  //
  // Fields
  //
  protected String string_match;
  protected String code_match;
  protected String saui_match;
  protected String scui_match;
  protected String sdui_match;
  protected boolean change_status;
  protected ArrayList old_values;
  protected ArrayList new_values;
  protected String tty_match;
  protected String sources_or_termgroups;
  protected String comments;;

};
