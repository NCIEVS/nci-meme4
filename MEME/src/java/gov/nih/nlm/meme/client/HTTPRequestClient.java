/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  HTTPRequestClient
 * Modified: Soma Lanka: 12/06/2005: Added the log statements before request the server 
 * and after the response comes from the server.
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.FailedToConnectException;
import gov.nih.nlm.meme.exception.HTTPResponseException;
import gov.nih.nlm.meme.exception.ProtocolParseException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.exception.UnknownStateException;
import gov.nih.nlm.meme.exception.XMLParseException;
import gov.nih.nlm.meme.xml.MASRequestSerializer;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.meme.xml.ObjectXMLSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.event.EventListenerList;

/**
 * This class implements the HTTP protocol for connecting to the
 * MEME application server.  It requires that the server employs a
 * listener that implements the server side of the HTTP protocol.
 *
 * It provides a {@link #processRequest(MEMEServiceRequest)} method that
 * converts object request into XML document, opens an HTTP connection,
 * sends the request document to the server, waits for a response, and
 * finally converts the response back to an object. <p>
 *
 * Client APIs should use this class to send and receive requests.
 * To use this class you must create an HTTP request client object,
 * initialize the object passing the host and port number you want to connect,
 * and process the request with the service request document. For example,<p>
 *
 * <pre>
 *   // Create an HTTP request client object to work with.
 *   HTTPRequestClient hrc = new HTTPRequestClient();
 *
 *   // Initialize the HTTP object and specify its host and port number.
 *   hrc.setHost("hostname")
 *   hrc.setPort(1234);
 *
 *   // Get a service request document and pass it as a parameter of HTTP
 *   // object's processRequest() method.
 *   MEMEServiceRequest request = new MEMEServiceRequest();
 *   request = hrc.processRequest(request);
 * </pre>
 *
 * Generally, this class is not directly used.  The various implementations
 * of {@link ClientAPI} make use of this class by setting the
 * <code>meme.client.protocol.class</code> (see {@link
 * ClientConstants#PROTOCOL_HANDLER}) property to the fully qualified
 * name of this class in the properties file.
 *
 * @see gov.nih.nlm.meme.server.HTTPRequestListener
 * @author MEME Group
 */
public class HTTPRequestClient implements MEMERequestClient {

  //
  // Host
  //
  private String host;

  //
  // Port
  //
  private int port;

  //
  // Listener list
  //
  private EventListenerList listener_list = new EventListenerList();

  //
  // Constructors
  //

  /**
   * Instantiates a {@link HTTPRequestClient}.
   */
  public HTTPRequestClient() {
  }

  //
  // Implementation of MEMERequestClient interface
  //

  /**
   * Sets the host to connect to. Implements {@link MEMERequestClient#setHost(String)}.
   * @param host the name of a host running the MEME Application Server
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Sets the port to connect to. Implements {@link MEMERequestClient#setPort(int)}.
   * @param port The port listened to by the MEME Application Server.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Handle the request. This method converts object request into XML document,
   * opens an HTTP connection, sends the request document to the server,
   * waits for a response, and finally converts the response back to an object.
   * @param request the {@link MEMEServiceRequest}
   * @return a {@link MEMEServiceRequest} reponse
   * @throws ExternalResourceException if network connection failed.
   * @throws XMLParseException if XML conversion failed.
   * @throws ReflectionException if XML serialization failed to load
   *         or instantiate a class
   * @throws ProtocolParseException if the server response was badly
   *         formatted
   * @throws HTTPResponseException if the server returned a bad
   *         response code (something other than 200).
   */
  public MEMEServiceRequest processRequest(MEMEServiceRequest request) throws
      ExternalResourceException, XMLParseException, ReflectionException,
      ProtocolParseException, HTTPResponseException {

    final MASRequestSerializer serializer = new MASRequestSerializer(
        MEMEToolkit.getSystemId());

    boolean problem = false;
    int ct = 0;
    Socket socket = null;
    MEMEServiceRequest return_request = null;

    //
    // Loop until request is processed
    //
    while (true) {
      ct++;

      //
      // Connect to server
      //
      try {
        socket = getSocket();
      } catch (FailedToConnectException ftce) {

        //
        // If we are trying to handle a problem
        //
        if (problem) {

          //
          // try again up to 5 times
          //
          if (ct < 5) {
            // Delay & continue
            try {
              Thread.sleep(2000);
            } catch (Exception e) {}
            continue;
          }

          //
          // Total failure
          //
          else {
            UnknownStateException ere = new UnknownStateException(
                "A problem was encountered trying to read the response " +
                "from the server.  In attempting to recover, the server " +
                "could not be found.  The state of the request is unknown.",
                ftce);
            ere.setDetail("host", host);
            ere.setDetail("port", Integer.toString(port));
            throw ere;
          }
        }

        //
        // Server has not yet seen request, try again (up to 5 times)
        //
        if (ct < 5) {
          // Delay & continue
          try {
            Thread.sleep(2000);
          } catch (Exception e) {}
          continue;
        }

        //
        // Here, the initial request has failed to connect 5 times, just bail
        //
        throw ftce;
      }

      MEMEToolkit.trace("HTTPRequestClient:processRequest() - Socket created");

      //
      // Write document
      //
      try {

        //
        // if we have not encountered a problem yet, just send request to the server
        //
        if (!problem) {
          String document = serializer.toXML(request);
          /*
           * Soma Lanka: Logging the XML Request
           */
          MEMEToolkit.logXmlComment("Request",document);
          writeDocument(socket, document);
        }

        //
        // otherwise, reconnect to previous request for this session
        //
        else {
          writeProblemDocument(socket, request.getSessionId());
        }

      } catch (ExternalResourceException ere) {

        //
        // If we are trying to handle a problem
        //
        if (problem) {

          //
          // try again up to 5 times
          //
          if (ct < 5) {
            // Delay & continue
            try {
              Thread.sleep(2000);
            } catch (Exception e) {}
            continue;
          }

          //
          // Total failure
          //
          else {
            UnknownStateException use = new UnknownStateException(
                "A problem was encountered trying to read the response " +
                "from the server.  In attempting to recover, the server " +
                "could not be found.  The state of the request is unknown.",
                ere);
            use.setDetail("host", host);
            use.setDetail("port", Integer.toString(port));
            throw use;
          }
        }

        //
        // Assume server has not seen the request,
        // we should just try again (up to 5 times)
        //
        if (ct < 5) {
          // Delay & continue
          try {
            Thread.sleep(2000);
          } catch (Exception e) {}
          continue;
        }

        //
        // Here, we have failed to send our request to the server
        //
        FailedToConnectException ftce = new FailedToConnectException(
            "Unable to write document to server", ere);
        ftce.setDetail("host", host);
        ftce.setDetail("port", Integer.toString(port));
        throw ftce;
      }

      //
      // Get document
      //
      try {
    	  /*
           * Soma Lanka: Logging the XML Response
           */
    	  String responseDocument = getResponse(socket);
          MEMEToolkit.logXmlComment("Response",responseDocument);
          
          /* 
           * Soma Lanka: Using "responseDocument instead of getResponse(socket);
           */
        return_request =
            (MEMEServiceRequest) serializer.fromXML(new BufferedReader(
            new StringReader(responseDocument)));

        //
        // Success! exit loop
        //
        break;

      } catch (ExternalResourceException ere) {

        //
        // Problem encountered, attempt to recover
        // bail if no session or if we've tried 5 times
        //
        problem = true;
        if (request.noSession() || ct >= 5) {
          UnknownStateException use = new UnknownStateException(
              "A problem was encountered trying to read the response " +
              "from the server.  In attempting to recover, the server " +
              "could not be found.  The state of the request is unknown.");
          use.setDetail("host", host);
          use.setDetail("port", Integer.toString(port));
          throw use;
        }

        // Delay
        try {
          Thread.sleep(2000);
        } catch (Exception e) {}
      }

    }

    //
    // Close the socket
    //
    try {
      socket.close();
    } catch (Exception e) {
      // Do nothing
    }

    //
    // Return the request
    //
    MEMEToolkit.trace("HTTPRequestClient:processRequest() - done");
    fireSynchronousProgressUpdate(new ClientProgressEvent("Finished", 100));
    return return_request;

  }

  /**
   * Add a listener.
   * @param l the {@link ClientProgressListener} to add
   */
  public void addClientProgressListener(ClientProgressListener l) {
    listener_list.add(ClientProgressListener.class, l);
  }

  /**
   * Remove a listener.
   * @param l the {@link ClientProgressListener} to remove
   */
  public void removeClientProgressListener(ClientProgressListener l) {
    listener_list.remove(ClientProgressListener.class, l);
  }

  /**
   * Fire an event informing listeners of a progress update.
   * @param e An object {@link ClientProgressEvent}
   */
  protected void fireProgressUpdate(ClientProgressEvent e) {
    final Object[] listeners = listener_list.getListenerList();
    final ClientProgressEvent cpe = e;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      final int j = i;
      if (listeners[i] == ClientProgressListener.class) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            ( (ClientProgressListener) listeners[j + 1]).progressUpdated(cpe);
          }
        });
        t.start();
      }
    }
  }

  /**
   * Fire an event informing listeners of a progress update.
   * @param e An object {@link ClientProgressEvent}
   */
  protected void fireSynchronousProgressUpdate(ClientProgressEvent e) {
    Object[] listeners = listener_list.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ClientProgressListener.class) {
        ( (ClientProgressListener) listeners[i + 1]).progressUpdated(e);
      }
    }
  }

  //
  // Private Methods
  //

  /**
   * Connects to the server.
   * @return the {@link Socket}
   * @throws FailedToConnectException if anything goes wrong
   */
  private Socket getSocket() throws FailedToConnectException {
    //
    // Connect to server
    //
    Socket socket = null;
    try {
      socket = new Socket(host, port);
    } catch (IOException ioe) {
      final FailedToConnectException ere = new FailedToConnectException(
          "Failed to connect to server.  The most likely reason is that the server is " +
          "not currently running on the specified host and port.", ioe);
      ere.setDetail("host", host);
      ere.setDetail("port", Integer.toString(port));
      ere.setDetail("failed_to_connect", "true");
      throw ere;
    }
    return socket;
  }

  /**
   * Writes the document to the socket.
   * @param socket the {@link Socket}
   * @param document the {@link String} document
   * @throws ExternalResourceException if anything goes wrong
   */
  private void writeDocument(Socket socket, String document) throws
      ExternalResourceException {

    Writer out = null;
    try {
      //
      // Get the output stream, using UTF-8 character encoding
      //
      out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
          "UTF-8"), 16 * 1024);
    } catch (IOException e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to get socket output stream.", e);
      throw ere;
    }

    try {
      //
      // client request using the POST method
      //
      MEMEToolkit.trace(
          "HTTPRequestClient:processRequest() - POST / HTTP/1.1");
      out.write("POST / HTTP/1.1");
      out.write("\n\n");
    } catch (IOException e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to send request to server.", e);
      throw ere;
    }

    try {
      //
      // Write the document to the socket
      //
      MEMEToolkit.trace("HTTPRequestClient:processRequest() - Write document");
      MEMEToolkit.trace("HTTPRequestClient:processRequest() - " + document);
      out.write(document);
      //
      // Flush the socket output stream
      //
      MEMEToolkit.trace(
          "HTTPRequestClient:processRequest() - Flush output stream");
      out.flush();
    } catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to send document to server.", e);
      throw ere;
    }

  }

  /**
   * Reads from the socket and returns the response document.
   * Also handles progress updates
   * @param socket the {@link Socket}
   * @return the document response
   * @throws ExternalResourceException
   * @throws XMLParseException
   * @throws ReflectionException
   * @throws ProtocolParseException
   * @throws HTTPResponseException
   */
  private String getResponse(Socket socket) throws
      ExternalResourceException, XMLParseException, ReflectionException,
      ProtocolParseException, HTTPResponseException {

    final ObjectXMLSerializer progress_serializer = new ObjectXMLSerializer();
    BufferedReader in = null;
    try {

      //
      // Get the socket input stream
      //
      MEMEToolkit.trace("HTTPRequestClient:processRequest() - Get input stream");
      in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
          "UTF-8"), 64 * 1024);
    } catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to get socket input stream.", e);
      throw ere;
    }

    StringBuffer doc = null;

    //
    // Here, we are going to read the response from the server.
    // This may come in the form of a single response, or several
    // Progress update responses, followed by the response to the request
    //
    try {
      String line = null;
      while ( (line = in.readLine()) != null) {

        //
        // Get the first line, looking for 200 status code
        //
        MEMEToolkit.trace(
            "HTTPRequestClient:processRequest() - Read first line - " + line);
        if (line == null || !line.startsWith("HTTP/1.1 ")) {
          throw new ProtocolParseException("Badly formatted response.");
        } else if (!line.startsWith("HTTP/1.1 200")) {
          throw new HTTPResponseException(
              line.substring(13),
              Integer.valueOf(line.substring(9, 12)).intValue());
        }

        //
        // Read through headers to a blank line
        //
        while ( (line = in.readLine()) != null) {
          if (line.equals("")) {
            break;
          }
        }

        //
        // Read the MASRequest document from the socket and
        // convert to a MEMEServiceRequest
        //
        MEMEToolkit.trace("HTTPRequestClient:processRequest() - Read request");

        //
        // Read the document now.  When we encounter a blank line, stop
        // and wait for next document.  Or if the reader is empty, finish.
        //
        doc = new StringBuffer(10000);
        while ( (line = in.readLine()) != null) {
          doc.append(line);
          doc.append("\n");

          //
          // If we encounter a blank line
          // Process the document
          //
          if (line.equals("")) {

            //
            // If this is a client progress event,
            // process it and send it to the listeners.
            // Reset document buffer.
            //
            String doc_str = doc.toString();
            if (doc_str.indexOf("gov.nih.nlm.meme.client.ClientProgressEvent") !=
                -1) {
              fireProgressUpdate(
                  (ClientProgressEvent) progress_serializer.fromXML(new
                  BufferedReader(new StringReader(doc_str))));
              break;

              //
              // In the future there may be other options, but not for now.
              //
            } else {
              // do nothing.
            }
          }
        }

      }

    } catch (ProtocolParseException ppe) {
      throw ppe;
    } catch (HTTPResponseException hre) {
      throw hre;
    } catch (XMLParseException xpe) {
      throw xpe;
    } catch (ReflectionException re) {
      throw re;
    } catch (SocketException se) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to read response from server.", se);
      throw ere;
    } catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Unexpected Exception.", e);
      throw ere;
    }

    MEMEToolkit.trace(
        "HTTPRequestClient:processRequest() - using buffered reader");
    MEMEToolkit.trace("HTTPRequestClient:processRequest() - " + doc.toString());

    return doc.toString();
  }

  /**
   * Writes a request to the server asking for the results of the previous
   * request.
   * @param socket the {@link Socket}
   * @param session_id the session id
   * @throws ReflectionException
   * @throws ExternalResourceException
   */
  private void writeProblemDocument(Socket socket, String session_id) throws
      ReflectionException, ExternalResourceException {
    MEMEServiceRequest msr = new MEMEServiceRequest();
    msr.setSessionId(session_id);
    msr.setReconnectRequest();
    String document = new MASRequestSerializer().toXML(msr);
    writeDocument(socket, document);
  }

} // end of class
