/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe;
 * Class:      StepNotFoundException.java
 * 
 * Author:     Brian Carlsen (2/2000)
 *
 * Remarks:    
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

public class StepNotFoundException extends Exception {

  public StepNotFoundException () {
	super();
  };

  public StepNotFoundException (String s) {
	super(s);
  };



}
