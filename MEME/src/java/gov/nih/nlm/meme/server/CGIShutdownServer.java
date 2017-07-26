/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  CGIShutdownServer
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.CGIStyleMEMEServiceRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Used to shutdown server (<B>DO NOT USE</b>).  Deprecated by {@link AdminService}.
 *
 * @author MEME Group
 */
public class CGIShutdownServer implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Shuts down the server and writes a response to the context's writer.
       * It implements {@link MEMEApplicationService#processRequest(SessionContext)}.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request.
   */
  public void processRequest(SessionContext context) throws MEMEException {

    // stop the server
    context.getServer().stop();
    OutputStream os =
        ( (CGIStyleMEMEServiceRequest) context.getServiceRequest()).
        getOutputStream();

    Writer out = new OutputStreamWriter(os);

    String doc = "<html><body>The server has been shut down</body></html>";

    try {
      // Write headers
      out.write("HTTP/1.1 200 OK\n");
      out.write("Expires: Fri, 20 Sep 1998 01:01:01 GMT\n");
      out.write("Content-Type: text/html\n");
      out.write("Content-Length: ");

      // Write the document
      out.write(String.valueOf(doc.length()));
      out.write("\n\n");

      out.write(doc);
      out.write("\n");
      out.flush();

    } catch (IOException ie) {
      try {
        out.write("HTTP/1.1 500 Internal Server Error - " + ie.getMessage());
        out.flush();
      } catch (Exception e) {}
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to write document to socket output stream.", ie);
      ere.setDetail("document", doc);
      MEMEToolkit.handleError(ere);
    }

    // Flush output buffer
    MEMEToolkit.Exit(0);

  }

  /**
   * Implements {@link MEMEApplicationService#requiresSession()}.
   * @return <code>true</code> if service require a session; <code>false</code>
   * otherwise.
   */
  public boolean requiresSession() {
    return false;
  }

  /**
   * Implements {@link MEMEApplicationService#isRunning()}.
   * @return <code>true</code> if the server is currently running,
   * and <code>false</code> otherwise.
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * Implements {@link MEMEApplicationService#isReEntrant()}.
   * @return <code>true</code> if the server is currently running and
   * this is a re entrant, <code>false</code> otherwise.
   */
  public boolean isReEntrant() {
    return false;
  }

}
