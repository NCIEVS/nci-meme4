/*****************************************************************************
 *
 * Package:    com.lexical.meme.core
 * Object:     IntegrityCheck.java
 * 
 * Author:     Brian Carlsen
 *
 * Remarks:    This object represents an integrity check in the database
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.io.Serializable;

/**
 * This class contains all of the information to describe
 * an integrity check.  It maps straightforwardly to 
 * the integrity_constraints table in the MID.
 *
 * @author Brian A. Carlsen
 * @version 1.0
 *
 */
public class IntegrityCheck implements Serializable {

  //
  // These fields are public to facilitate ease of use
  //
  public String ic_name = "";
  public String ic_code = "";
  public String violation_actions = "";
  public String correction_actions = "";
  public String ic_status = "";
  public String ic_type = "";
  public java.util.Date activation_date = new java.util.Date();
  public java.util.Date deactivation_date = new java.util.Date();
  public String short_description = "";
  public String long_description = "";

  /**
   * Constructor
   */
  public IntegrityCheck () { 
    
  };

  /**
   * String representation
   * @return String
   */
  public String toString () {
    return ic_name + ": " + short_description;
  }

  /**
   * Overrides equals: necessary to render it in a JList.
   * setValues and getValues are used.
   * @return boolean 
   */
  public boolean equals (Object obj) {

    if (! (obj instanceof IntegrityCheck))
      return false;
    
    IntegrityCheck local_obj = (IntegrityCheck)obj;
    return ic_name.equals(local_obj.ic_name);

  }
}
