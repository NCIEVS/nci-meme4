/************************************************************************
 * Package:     gov.nih.nlm.util
 * Object:      UTF8InputStreamReader.java
 *
 *
 ***********************************************************************/
package gov.nih.nlm.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * An {@link InputStreamReader} that ignores the first 3 bytes if they
 * are the UTF8 Byte Order Mark (BOM) bytes.
 *
 * @author Deborah Shapiro
 */
public class UTF8InputStreamReader extends InputStreamReader {

  //
  // Constructors
  //
  private boolean bom_present = false;

  /**
   * Instantiates an {@link UTF8InputStreamReader}.
   * @param input_stream {@link BufferedInputStream}
   * @throws UnsupportedEncodingException if "UTF-8" is not suported
   * @throws IOException if anything goes wrong
   */
  public UTF8InputStreamReader(BufferedInputStream input_stream)
      throws UnsupportedEncodingException, IOException {
    super(input_stream, "UTF-8");
    input_stream.mark(10);
    // if BOM is found, do nothing, stream will be in correct position
    if (input_stream.read() == 239 &&
        input_stream.read() == 187 &&
        input_stream.read() == 191) {
      bom_present = true;
    // else reset the stream to the beginning b/c no BOM
    } else {
        input_stream.reset();
    }
  }

  public boolean hasByteOrderMark() {
    return bom_present;
  }
}
