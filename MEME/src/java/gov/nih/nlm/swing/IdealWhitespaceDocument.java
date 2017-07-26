/*****************************************************************************
 *
 * Object:  NumberDocument
 * Author:  Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * {@link PlainDocument} implementation that prevents leading, training, duplicate,
 * and redundant whitespace.
 *
 * @author  Brian Carlsen (bcarlsen@msdinc.com)
 */
public class IdealWhitespaceDocument extends PlainDocument {

  /**
   * Instantiates an empty {@link IdealWhitespaceDocument}.
   */
  public IdealWhitespaceDocument() {
  }

  /**
   * Sets the text of the document.
   * @param text the text
   * @param a the {@link AttributeSet}
   * @throws BadLocationException if anything goes wrong
   */
  public void setText(String text, AttributeSet a) throws BadLocationException {
    remove(0, getLength());
    super.insertString(0, text, a);
  }

  /**
   * Overrides {@link PlainDocument#insertString(int,String,AttributeSet)} to
   * disallow:
   * <ol>
   * <li>leading whitespace</li>
   * <li>trailing whitespace</li>
   * <li>any non-space character whitespace</li>
   * <li>duplicate spaces</li>
   * </ol>
   * @param offset the offset
   * @param str the string to insert
   * @param a the {@link AttributeSet}
   * @throws BadLocationException if anything goes wrong
   */
  public void insertString(int offset, String str, AttributeSet a) throws
      BadLocationException {

    String pre_text = getText(0, getLength());
    super.insertString(offset, str, a);
    String post_text = getText(0, getLength());

    //
    // Check for non space character whitespace
    //
    for (int i = 0; i < post_text.length(); i++) {
      if (Character.isWhitespace(post_text.charAt(i)) &&
          !Character.isSpaceChar(post_text.charAt(i))) {
        setText(pre_text, a);
        System.err.println("Warning: Non-character whitespace");
        return;
      }
    }

    //
    // Check for leading whitespace
    //
    if (post_text.length() > 0 &&
        Character.isSpaceChar(post_text.charAt(0))) {
      setText(pre_text, a);
      System.err.println("Warning: leading whitespace character");
      return;
    }

    //
    // Check for trailing spaces (one space is allowed)
    //
    if (post_text.length() > 1 &&
        Character.isSpaceChar(post_text.charAt(post_text.length() - 1)) &&
        Character.isSpaceChar(post_text.charAt(post_text.length() - 2))) {
      setText(pre_text, a);
      System.err.println("Warning: trailing whitespace character");
      return;
    }

    //
    // Check for duplicate spaces
    //
    if (post_text.length() > 1) {
      for (int i = 1; i < post_text.length(); i++) {
        if (Character.isSpaceChar(post_text.charAt(i)) &&
            Character.isSpaceChar(post_text.charAt(i - 1))) {
          setText(pre_text, a);
          System.err.println("Warning: duplicate whitespace");
          return;
        }
      }
    }
  }

}
