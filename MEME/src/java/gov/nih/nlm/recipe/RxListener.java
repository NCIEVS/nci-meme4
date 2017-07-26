/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe;
 * Interface:  RxListener.java
 * 
 * Author:     BAC (4/2000)
 *
 * Remarks:    This interface defines a runnable recipe listener
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

/**
 *
 * This is an interface describing methods for listening to
 * key events while a recipe executes
 *
 * @author: Brian Carlsen (4/2000)
 * @version: 1.0
 *
 **/

public interface RxListener {

  public void resultStatusChanged(RxEvent e);
  public void stepInstructionChanged(RxEvent e);
  public void stepInstructionMaskChanged(RxEvent e);
  public void executionModeChanged(RxEvent e);
  public void recipeExecutionCompleted(RxEvent e);
}

