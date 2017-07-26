/*****************************************************************************
 *
 * Object:  DoubleDocument
 * Author:  Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * {@link PlainDocument} implementation that only accepts
 * floating point numerical values.
 *
 * @author  Brian Carlsen (bcarlsen@apelon.com)
 */
public class DoubleDocument extends PlainDocument {

  /**
   * Instantiates a {@link DoubleDocument}.
   */
  public DoubleDocument() {}

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
   * to allow only double precision floating point values.
   * @param offset the document offset
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
    // Validate double
    //
    try {
      Double.parseDouble(post_text);
      return;
    } catch (Exception e) {}

    //
    // test failed, reset
    //
    setText(pre_text, a);
  }
}
