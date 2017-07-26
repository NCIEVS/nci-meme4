/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ErrorCatalog
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

import java.util.HashMap;

/**
 * This class maps <i>mnemonics</i> to error messages.  This class is currently
 * hard coded but should be implemented by a properties file in the future.
 *
 * @author MEME Group
 */

public class ErrorCatalog {

  //
  // Fields
  //

  private final static HashMap code_to_message = new HashMap(100);

  // Here the code-message mapping is defined
  static {
    code_to_message.put("code", "message");
  }

  //
  // Methods
  //

  /**
   * Converts an <i>code</i> or <i>mnemonic</i> into a full error message.
   * @param code the abbreviated name.
   * @return the message for the code
   */
  public static String lookup(String code) {
    return (String) code_to_message.get(code);
  }

}
