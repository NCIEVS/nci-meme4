/*****************************************************************************
 *
 * Package:    com.lexical.meme.core
 * Object:     BinaryIntegrityCheck.java
 * 
 * Author:     Brian Carlsen
 *
 * Remarks:    This object represents an BinaryIntegrity check in the database
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.io.Serializable;


/**
 * This class contains all of the information to describe
 * an BinaryIntegrity check.  It maps straightforwardly to 
 * the ic_pair table in the MID.
 *
 * @author Brian A. Carlsen
 * @version 1.0
 *
 */
public class BinaryIntegrityCheck extends IntegrityCheck implements Serializable, Cloneable {

  //
  // These fields are public to facilitate ease of use
  //
  public boolean negation = false;
  public String type_1 = "";
  public String value_1 = "";
  public String type_2 = "";
  public String value_2 = "";

  /**
   * Constructor
   */
  public BinaryIntegrityCheck () { 
    
  };


}
