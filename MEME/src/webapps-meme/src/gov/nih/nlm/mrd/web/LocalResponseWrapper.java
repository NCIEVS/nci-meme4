/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.web
 * Object:  LocalResponseWrapper
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.web;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * "Catches" output from a filter chain and keeps
 * it in a buffer.  This is later converted to a {@link String}
 * for other uses.
 *
 * @author TTN
 */
public class LocalResponseWrapper extends HttpServletResponseWrapper {

  private CharArrayWriter caw = null;
  private ServletOutputStreamWrapper sw = null;

  /**
   * Instantiates a {@link LocalResponseWrapper}.
   * @param res the {@link HttpServletResponse}
   */
  public LocalResponseWrapper(HttpServletResponse res) {
    super(res);
  }

  /**
   * Returns an {@link ServletOutputStream} that writes to a buffer.
   * @return an {@link ServletOutputStream} that writes to a buffer
   */
  public ServletOutputStream getOutputStream() {
    if (caw != null) {
      throw new java.lang.IllegalStateException
        ("LocalResponseWrapper: " +
         "getWriter() has already been called " +
         "for this object");
    }
    if (sw == null)
      sw = new ServletOutputStreamWrapper();
    return sw;
  }

  /**
   * Returns a {@link PrintWriter} that writes to a buffer.
   * @return a {@link PrintWriter} that writes to a buffer
   */
  public PrintWriter getWriter()
   throws java.io.IOException {
    if (sw != null) {
      throw new java.lang.IllegalStateException
        ("LocalResponseWrapper: getOutputStream()" +
         "has already been called for this object");
    }
    if (caw == null)
      caw = new CharArrayWriter();
    return new PrintWriter(caw);
  }

  /**
   * Returns {@link String} representation.
   * @return {@link String} representation
   */
  public String toString() {
    if (sw != null) {
      return sw.toString();
    } else if (caw != null) {
      return caw.toString();
    } else {
      return null;
    }
  }

}
