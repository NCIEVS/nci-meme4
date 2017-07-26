/*****************************************************************************
 * Package: gov.nih.nlm.util
 * Object:  XMLEntityEncoder
 *****************************************************************************/
package gov.nih.nlm.util;

/**
 * Encodes a string with certain characters using XML character entities.
 * The code in here is a modified version of a similar
 * method from <code>org.apache.xalan.client.XSLTProcessorApplet</code>.
 *
 * @author  MEME Group
 */
public abstract class XMLEntityEncoder {

  /**
   * Encodes all of the XML character entities in the specified {@link String}
   * so the resulting string can be safely used in a document without a CDATA
   * section.
   * @param input the input {@link String}
   * @return a {@link String} with XML character entities properly encoded
   */
  public static String encode(String input) {
    StringBuffer sb = new StringBuffer();
    int length = input.length();

    for (int i = 0; i < length; i++) {
      char ch = input.charAt(i);

      if ('<' == ch) {
        sb.append("&lt;");
      } else if ('>' == ch) {
        sb.append("&gt;");
      } else if ('&' == ch) {
        sb.append("&amp;");
      } else if ('\t' == ch) {
        sb.append("&#x9;");
      } else if ('\n' == ch) {
        sb.append("&#x0a;");
      } else if ('\r' == ch) {
        sb.append("&#x0d;");
      } else if ('"' == ch) {
        sb.append("&quot;");
      } else if ('\0' == ch) {
        sb.append("&#x0;");
      } else if (0xd800 <= ch && ch < 0xdc00) {
        int next;

        if (i + 1 >= length) {
          throw new RuntimeException("Invalid UTF-16 surrogate detected: " +
                                     Integer.toHexString(ch));
        } else {
          next = input.charAt(++i);
          if (! (0xdc00 <= next && next < 0xe000)) {
            throw new RuntimeException("Invalid UTF-16 surrogate detected: " +
                                       Integer.toHexString(ch) + " " +
                                       Integer.toHexString(next));
          }
          next = ( (ch - 0xd800) << 10) + next - 0xdc00 + 0x00010000;
        }
        sb.append("&#x");
        sb.append(Integer.toHexString(next));
        sb.append(";");
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

}
