/*****************************************************************************
 *
 * Package:    com.lexical.meme.swing
 * Object:     UppercaseDocument.java
 * 
 * Author:     Owen Carlsen
 *****************************************************************************/
package gov.nih.nlm.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * This subclass of PlainDocument allows only uppercase strings
 *
 * @author Brian Carlsen
 * @version 1.0
 * 
 */
public class UppercaseDocument extends PlainDocument {
	
  //
  // Fields
  //

  //
  // Constructors
  //

  //
  // Methods
  //

  /**
   * This method overrides super.insertString and ensures that
   * the letters get inserted in uppercased form
   * @param offs int
   * @param str String
   * @param a AttributeSet
   */
  public void insertString(int offs, String str, AttributeSet a) 
    throws BadLocationException {
    
    if (str == null) {
      return;
    }
    char[] upper = str.toCharArray();
    for (int i = 0; i < upper.length; i++) {
      upper[i] = Character.toUpperCase(upper[i]);
    }
    super.insertString(offs, new String(upper), a);
  }

}
