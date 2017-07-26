/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.web
 * Object:  ServletOutputResponseWrapper
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;

/**
 * This class stores away bytes written to a ServletOutputStream.
 * When requested, it returns the array of written bytes as a String.
 *
 * @author TTN
 */
public class ServletOutputStreamWrapper  extends ServletOutputStream {

  protected ByteArrayOutputStream bos;

  /**
   * Instantiates a {@link ServletOutputStreamWrapper}.
   */
  public ServletOutputStreamWrapper() {
    bos = new ByteArrayOutputStream();
  }

  /**
   * Writes to output stream.
   * @param b int to write
   * @throws IOException if cannot write
   */
  public void write(int b) throws IOException {
    //System.out.println(b);
    bos.write(b);
  }

  /**
   * Returns {@link String} representation.
   * @return {@link String} representation
   */
  public String toString() {
    String result = bos.toString();
    return result;
  }
}
