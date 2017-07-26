/************************************************************************
 * Package:     gov.nih.nlm.util
 * Object:      UTF8OutputStreamWriter.java
 *
 *
 ***********************************************************************/
package gov.nih.nlm.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

/**
 * An {@link OutputStreamWriter} that can make the first 3 bytes
 * the UTF8 Byte Order Mark (BOM) bytes.
 *
 * @author Deborah Shapiro
 */
public class UTF8OutputStreamWriter extends OutputStreamWriter {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link UTF8OutputStreamWriter}. Can prepend the UTF8 byte
   * lorder mark (BOM) to the output.
   * @param output_stream the {@link OutputStream}
   * @param write_bom <code>true</code> if byte order mark should be written
   *                  <code>false</code> otherwise
   * @throws UnsupportedEncodingException if "UTF-8" is not supported
   * @throws IOException if anything goes wrong
   */
  public UTF8OutputStreamWriter(OutputStream output_stream, boolean write_bom)
      throws UnsupportedEncodingException, IOException {
    super(output_stream, "UTF-8");
    if (write_bom) {
      output_stream.write(0xEF);
      output_stream.write(0xBB);
      output_stream.write(0xBF);
      output_stream.flush();
    }
  }

  /**
   * Instantiates an {@link UTF8OutputStreamWriter}. Does not prepend the UTF8 byte
   * order mark (BOM) to the output.
   * @param output_stream the {@link OutputStream}
   * @throws UnsupportedEncodingException if "UTF-8" is not supported
   * @throws IOException if anything goes wrong
   */
  public UTF8OutputStreamWriter(OutputStream output_stream)
      throws UnsupportedEncodingException, IOException {
    super(output_stream, "UTF-8");
  }

}
