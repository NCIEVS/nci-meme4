/*****************************************************************************
 *
 * Object:  IntegerDocument
 * Author:  Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * {@link PlainDocument} implementation that only accepts integer values.
 *
 * @author  Brian Carlsen (bcarlsen@apelon.com)
 */
public class IntegerDocument extends PlainDocument {

  /**
   * Instantiates an empty {@link IntegerDocument}.
   */
  public IntegerDocument() {
  }

  /**
   * Sets the text.
   * @param text the text
   * @param a the {@link AttributeSet}
   * @throws BadLocationException if anything goes wrong
   */
  public void setText(String text, AttributeSet a) throws BadLocationException {
    remove(0, getLength());
    super.insertString(0, text, a);
  }

  /**
   * Overrides {@link PlainDocument#insertString(int,String,AttributeSet)}
   * to allow only <code>int</code> values.
   * @param offset the offset
   * @param str the string
   * @param a the {@link AttributeSet}
   * @throws BadLocationException if anything goes wrong
   */
  public void insertString(int offset, String str, AttributeSet a) throws
      BadLocationException {

    String pre_text = getText(0, getLength());
    super.insertString(offset, str, a);
    String post_text = getText(0, getLength());

    //
    // Valid Integer
    //
    try {
      Integer.parseInt(post_text);
      return;
    } catch (Exception e) {}

    // test failed, reset
    setText(pre_text, a);
  }
}
