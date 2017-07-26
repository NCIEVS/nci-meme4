/*****************************************************************************
 *
 * Package:    com.lexical.meme.core
 * Object:     UnaryIntegrityCheck.java
 * 
 * Author:     Brian Carlsen
 *
 * Remarks:    This object represents an UnaryIntegrity check in the database
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;


import java.io.Serializable;

/**
 * This class contains all of the information to describe
 * an UnaryIntegrity check.  It maps straightforwardly to 
 * the ic_single table in the MID.
 *
 * @author Brian A. Carlsen
 * @version 1.0
 *
 */
public class UnaryIntegrityCheck extends IntegrityCheck 
  implements Serializable, Cloneable {

  //
  // These fields are public to facilitate ease of use
  //
  public boolean negation = false;
  public String type = "";
  public String value = "";

  /**
   * Constructor
   */
  public UnaryIntegrityCheck () { 
    
  };

}
