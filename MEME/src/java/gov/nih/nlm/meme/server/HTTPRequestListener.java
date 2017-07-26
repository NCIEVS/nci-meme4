/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  HTTPRequestListener
 *    
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.PasswordAuthentication;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.PoolOverflowException;
import gov.nih.nlm.meme.exception.ProtocolParseException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.exception.XMLParseException;
import gov.nih.nlm.meme.sql.MEMEConnection;
import gov.nih.nlm.meme.xml.CGIStyleMEMEServiceRequest;
import gov.nih.nlm.meme.xml.MASRequestSerializer;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Simple HTTP socket server implementation.
 * It listens to a specified port and accepts HTTP-style requests.
 *
 * To use this class you must create a server instance, an HTTP request
 * listener object and a thread that will listen and wait for the client's
 * request. For example,<p>
 *
 * <pre>
 * // Create HTTP listener object
 * HTTPRequestListener hrl = new HTTPRequestListener();
 *
 * // Initialize object passing the server instance
 * hrl.initialize(sc);
 *
 * // Create new thread, listens and wait for client request
 * Thread t = new Thread(hrl);
 * t.start();
 * t.join();
 * </pre></p>
 *
 * Alternatively, you could add this class to the list of initializable
 * components specified in the {@link ServerConstants#MEME_BOOTSTRAP}
 * property and then call {@link ServerToolkit#initialize()}.
 *
 * @author MEME Group
 */

public class HTTPRequestListener implements MEMERequestListener {

  //
  // Fields
  //

  //
  // Socket server
  //
  private ServerSocket listener;

  //
  // MEME Server
  //
  private MEMEApplicationServer server;

  //
  // stop listener?
  //
  private boolean keep_going;

  //
  // Current request id (randomize in future)
  //
  private int request_id = 0;

  //
  // This could be (yet another) property
  // but we feel that the number of simultaneous incoming
  // connections will almost always be < 10.
  //
  private final static int OPTIMAL_HANDLER_COUNT = 10;

  //
  // Handler cache
  //
  private Stack handlers = new Stack();

  //
  // Constructors
  //

  /**
   * Instantiates an {@link HTTPRequestListener}.
   */
  public HTTPRequestListener() {}

  //
  // Methods
  //

  /**
   * Starts the listener.
   */
  public void start() {
    keep_going = true;
    Thread t = new Thread(this);
    t.start();
    MEMEToolkit.logComment("LISTENER STARTED", true);
  }

  /**
       * Stops the listener.  This does not asynchronously stop the listener, rather
       * it sets a flag that indicates to the listener that it should stop accepting
   * connections as soon as it finishes processing the current connection.
   */
  public void stop() {
    keep_going = false;
  }

  //
  // Accessor Methods
  //

  /**
   * Strips out cumbersome details such as source
   * documents for the client side exceptions.
   * @param e the {@link MEMEException} to prepare
   */
  private void prepareExceptionForClient(MEMEException e) {
    if (e.getEnclosedException()instanceof SQLException) {
      e.setEnclosedException(new Exception(e.getEnclosedException().getMessage())); ;
    }
    if (e.getSource()instanceof MEMEConnection) {
      MEMEConnection mc = (MEMEConnection) e.getSource();
      e.setSource(mc.getDataSourceName());
    }
  }

  /**
   * Returns an already instantiated {@link RequestHandler}.  This method looks in a
   * stack and if the stack is empty, creates a new object.  It also sets the
       * request_id for the handler so it can be passed to the request object.  This
   * is done here so that only one synchronized block is necessary.
   *
   * @param socket the {@link Socket} for the request to be handled
   * @return the {@link RequestHandler}
   */
  private synchronized RequestHandler getRequestHandler(Socket socket) {

    RequestHandler handler = null;
    ++request_id;

    //
    // Pop off of stack.  If stack is empty create new handler.
    //
    if (!handlers.empty()) {
      handler = (RequestHandler) handlers.pop();
    } else {
      handler = new RequestHandler();

    }
    handler.initialize(socket, request_id);
    return handler;
  }

  /**
   * Returns a handler to the stack.  If the stack size is not optimal, it
   * additionally resizes the stack.
   * @param handler the {@link RequestHandler} to handler whose processing
   * is finished and should be returned.
   */
  private synchronized void returnRequestHandler(RequestHandler handler) {
    //
    // Put handler back on stack
    // If there are too many handlers,
    // reduce stack size
    //
    handlers.push(handler);
    while (handlers.size() > OPTIMAL_HANDLER_COUNT) {
      handlers.pop();
    }
    ;
  }

  //
  // Implementation of MEMERequestListener interface
  //

  /**
       * Initializes the class, in particular specify which port it should listen on.
   * @param context the {@link InitializationContext}
   * @throws InitializationException if initialization fails
   */
  public void initialize(InitializationContext context) throws
      InitializationException {

    //
    // Get the server which is the context
    //
    server = (MEMEApplicationServer) context;

    //
    // Get the port from the property and create the socket server
    //
    int port = Integer.valueOf(MEMEToolkit.getProperty(ServerConstants.
        MEME_SERVER_PORT)).intValue();

    //
    // Open socket server
    //
    try {
      listener = new ServerSocket(port);
      // Set SO_TIMEOUT to 1/2 hour
      listener.setSoTimeout(1800000);
    } catch (Exception e) {
      InitializationException ie = new InitializationException(
          "Failed to initialize socket server.", e);
      ie.setDetail("port",String.valueOf(port));
      throw ie;
    }
    keep_going = true;

    //
    // Inform the server of the presence of the listener
    //
    context.addHook(this);

  }

  //
  // Implementation of Runnable interface
  //

  /**
   * Accepts connections and spin off request handlers to deal with them.
   * This is the implementation of the {@link Runnable} interface.
   */
  public void run() {
    while (keep_going) {
      Socket socket = null;
      Thread thread = null;
      try {
        socket = listener.accept();
        MEMEToolkit.trace("HTTPRequestListener:run() - accept: " + socket);
        thread = ServerToolkit.getThread(getRequestHandler(socket));
        MEMEToolkit.trace("HTTPRequestListener:run() - start handler");
        thread.start();
      } catch (InterruptedIOException iioe) {
        // This means accept timed out, re-call accept()
      } catch (PoolOverflowException poe) {
        // Pool has overflowed, write messages to log
        MEMEToolkit.handleError(poe);
        MEMEToolkit.logComment("REQUEST REJECTED - POOL OVERFLOW (id=" +
                               request_id + ")", true);
        try {
          // Thread pool is full, sends service unavailable response.
          Writer out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
          out.write("HTTP/1.1 503 Service Unavailable - ThreadPool full.\n");
          out.flush();
          socket.close();
          return;
        } catch (Exception e) {
          // Do nothing
          return;
        }
      } catch (Exception e) {
        // Listener failed to get connection, try again.
        MEMEToolkit.logComment("REQUEST REJECTED - UNEXPECTED EXCEPTION (id=" +
                               request_id + ")", true);
        ExternalResourceException ere = new ExternalResourceException(
            "Unexpected error accepting connection or obtaining request handler thread.",
            e);
        ere.setFatal(false);
        MEMEToolkit.handleError(ere);
      }
    }
  }

  //
  // Inner Classes
  //

  /**
   * Used to handle incoming server requests.  Instances of this class are
   * spun off in threads by {@link HTTPRequestListener#run()} to handle the
   * incoming requests.  Here is where the actually process of deconstructing
   * incomming requests, parsing objects, calling the server, and responding
   * takes place.
   *
   */
  private class RequestHandler implements Runnable {

    //
    // Fields
    //

    //
    // Incoming socket request
    //
    private Socket socket;

    //
    // HTTP Headers
    //
    private HashMap headers = new HashMap();

    //
    // HTTP reponse code (start with 200)
    //
    private int http_response_code = 200;

    //
    // HTTP response
    //
    private String http_response = "OK";

    //
    // request id
    //
    private int request_id = 0;

    //
    // start time
    //
    private Date start_time = null;
    ;

    //
    // Request serializer
    //
    private MASRequestSerializer serializer;

    //
    // Constructors
    //

    /**
         * Instantiates a {@linke MASRequestSerializer} and wraps itself around a socket
     */
    public RequestHandler() {
      //
      // This is now done once per request in run()
      //serializer = new MASRequestSerializer(MEMEToolkit.getSystemId());
      //
    }

    //
    // Accessor Methods
    //

    /**
     * Sets the socket, request_id, and start time used by the handler.
     * @param socket the {@link Socket}
     * @param request_id the request_id
     */
    public void initialize(Socket socket, int request_id) {
      headers.clear();
      this.socket = socket;
      this.request_id = request_id;
      start_time = new Date();
    }

    //
    // Implementation of Runnable interface
    //

    /**
     * Performs the actual work of handling a request.  First, it reads the
     * request by calling {@link #getMEMEServerRequest(BufferedReader)}.
     * Then, if the request format is good, it calls
     * {@link MEMEApplicationServer#processRequest(MEMEServiceRequest)}
     * which handles the processing of the request.  Finally, it converts the
     * response back into either an XML MASRequest object or an HTML page
     * depending upon the style of the incoming request.  An incoming request
     * that uses CGI parameters gets an HTML response, and an incoming request
     * that uses an XML document gets an XML response.
     * This implements the {@link Runnable} interface.
     */
    public void run() {

      //
      // Get serializer
      //
      serializer = new MASRequestSerializer(MEMEToolkit.getSystemId());

      //
      // Attempt to process request
      //
      MEMEException request_exception = null;
      MEMEServiceRequest request = null;
      try {

        http_response_code = 200;
        http_response = "OK";
        BufferedReader in = null;

        //
        // Attempt to read request
        //
        try {
          in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
              "UTF-8"), 16 * 1024);
        } catch (Exception e) {
          http_response_code = 500;
          ExternalResourceException ere = new ExternalResourceException(
              "Failed to get socket input stream.", e);
          request_exception = ere;
        }

        //
        // Attempt to parse request
        //
        if (http_response_code == 200) {
          request = parse(in);

          //
          // Attempt to get output stream
          //
        }
        // bail on empty requests
        if (request == null) {
	    return;
        }

        Writer out = null;
        try {
          out = new BufferedWriter(new OutputStreamWriter(socket.
              getOutputStream(), "UTF-8"), 32 * 1024);
        } catch (Exception e) {
          http_response_code = 500;
          ExternalResourceException ere = new ExternalResourceException(
              "Failed to get socket output stream.", e);
          throw ere;
        }

        String doc = null;

        //
        // So far, everything is OK
        //
        if (http_response_code == 200) {

          //
          // Log request parameters
          //
          MEMEToolkit.logComment("REQUEST_ACCEPTED - "
                                 + MEMEToolkit.getUnqualifiedClassName(request) +
                                 " (id=" + request_id + ")", true);
          String session = null;
          if (request.noSession()) {
            session = "nosession";
          } else if (request.terminateSession()) {
            session = "terminate";
          } else {
            session = request.getSessionId();
          }
          MEMEToolkit.logComment("  service = " + request.getService() + ", " +
                                 "session = " + session + ", " +
                                 "data_source = " + request.getMidService() +
                                 ", " +
                                 "user = " +
                                 (request.getAuthentication() != null ?
                                  request.getAuthentication().toString() : ""), true);
          Map no_password = new HashMap(request.getParametersAsHashMap());
          if (no_password.containsKey("password")) {
            no_password.put("password",
                            new Parameter.Default("password", "********"));
          }
          MEMEToolkit.logComment("  parameters = " + no_password.toString(), true);

          //
          // Forward request to server.
          //
          MEMEToolkit.trace(
              "HTTPRequestListener.RequestHandler:run() - request. " + request);
          Statistics.requestStarted(request);
          try {
            request.setWriter(out);
            request.setRequestId(request_id);
            server.processRequest(request);
          } catch (MEMEException me) {
            // set exception details and wrap it into the request
            // so that client can also see exceptions from the server
            request_exception = new MEMEException();
            request_exception.setInformAdministrator(me.informAdministrator());
            request_exception.setAdministrator(me.getAdministrator());
            request_exception.setFatal(false);
            request_exception.setInformUser(false);
            request_exception.setDetails(me.getDetails());
            request_exception.setMessage(me.getMessage());
            request_exception.setEnclosedException(
                me.getEnclosedException() != null ? me.getEnclosedException() :
                me);
            prepareExceptionForClient(me);
            request.addException(me);
            MEMEToolkit.logComment("REQUEST FAILED (id=" + request_id + ")", true);
          } catch (Exception e) {
            DeveloperException de = new DeveloperException(
                "Unexpected exception.");
            de.setFatal(false);
            de.setEnclosedException(e);
            de.setInformUser(false);
            if (request != null) {
              de.setDetail("mid_service", request.getMidService());
              de.setDetail("service", request.getService());
              HashMap params = request.getParametersAsHashMap();
              de.setDetail("parameters", params.toString());
              if (request.getAuthentication() != null) {
                de.setDetail("authentication",
                             request.getAuthentication().toString());
              }
            }
            request.addException(de);
            request_exception = de;
            MEMEToolkit.logComment("REQUEST FAILED (id=" + request_id + ")", true);
          }
          Statistics.requestFinished();

          MEMEToolkit.logComment("REQUEST PROCESSED (id=" + request_id +
                                 ", elapsed_time="
                                 +
                                 MEMEToolkit.timeToString(MEMEToolkit.
              timeDifference(new Date(), start_time)) + ")", true);

          // The request was rejected bad status code
        } else {
          // Log that a badly formatted request was received.
          MEMEToolkit.logComment("REQUEST REJECTED - SERVER ERROR (id=" +
                                 request_id + ")", true);
          MEMEToolkit.logComment("    HTTP Response Code: " +
                                 http_response_code, true);
          MEMEToolkit.logComment("    HTTP Response: " + http_response, true);
        }

        //
        // Write the output.  CGI style requests are responsible for all output.
        //
        if (! (request instanceof CGIStyleMEMEServiceRequest)) {
          MEMEToolkit.trace("About to serialize the request.");

          //
          // Attempt toonvert the request back into a document
          //
          doc = "";
          try {
            doc = serializer.toXML(request);
            MEMEToolkit.trace(doc);
          } catch (Exception re) {
            http_response_code = 500;
            http_response = "HTTP/1.1 500 Internal Server Error - " +
                re.getMessage();
            request_exception = new MEMEException();
            request_exception.setFatal(false);
            request_exception.setInformUser(false);
            request_exception.setEnclosedException(re);
          }

          MEMEToolkit.trace("Done serializing the request.");

          headers = new HashMap();
          headers.put("Content-Type", "text/xml");
          headers.put("Content-Length", String.valueOf(doc.length()));

          try {
            //
            // Write HTTP header
            //
            MEMEToolkit.trace(
                "HTTPRequestListener.RequestHandler:run() - HTTP/1.1 "
                + http_response_code + " " + http_response);
            out.write("HTTP/1.1 ");
            out.write(String.valueOf(http_response_code));
            out.write(" ");
            out.write(http_response);
            out.write("\n");

            //
            // Write headers if OK
            //
            if (http_response_code == 200) {
              Iterator iterator = headers.keySet().iterator();
              while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = (String) headers.get(key);
                out.write(key);
                out.write(": ");
                out.write(value);
                out.write(";\n");
              }
              out.write("\n");
              out.write(doc);
              out.write("\n");
            }
            out.flush();

            MEMEToolkit.trace(
                "HTTPRequestListener.RequestHandler:run() - Done with request");

          } catch (SocketException se) {
            // this catch block should not send any message to the administrator
            MEMEToolkit.logComment("REQUEST REJECTED - SOCKET EXCEPTION (id=" +
                                   request_id + ")", true);
            ExternalResourceException ere = new ExternalResourceException(
                "Failed to write to socket due to broken pipe.", se);
            ere.setInformAdministrator(false);
            throw ere;

          } catch (IOException ioe) {
            MEMEToolkit.logComment("REQUEST REJECTED - IO EXCEPTION (id=" +
                                   request_id + ")", true);
            ExternalResourceException ere = new ExternalResourceException(
                "Failed to write to socket output stream.", ioe);
            throw ere;
          }
        }

        // Triggered on exception in server.processRequest call or
        // failure to acquire socket input stream.
        if (request_exception != null) {
          throw request_exception;
        }

      } catch (MEMEException me) {
        // Exception while handling a request should not kill the server.
        me.setFatal(false);
        me.setInformUser(false);
        MEMEToolkit.handleError(me);
      } catch (Exception e) {
        // We are in a Thread, killing the
        // thread won't kill the server
        DeveloperException de = new DeveloperException("Unexpected exception.");
        de.setFatal(false);
        de.setEnclosedException(e);
        de.setInformUser(false);
        if (request != null) {
          de.setDetail("mid_service", request.getMidService());
          de.setDetail("service", request.getService());
          HashMap params = request.getParametersAsHashMap();
          de.setDetail("parameters", params.toString());
        }
        MEMEToolkit.handleError(de);
      }

      try {
        // Close the socket (no keep-alive)
        socket.close();
      } catch (Exception e) {
        // Do nothing
      }

      // return the handler to the stack
      returnRequestHandler(this);

      MEMEToolkit.trace("Finished handling a request.");

    } // end run method

    /**
     * Parses MEME server request from the socket stream.
     * First, it verifies that the request is a HTTP <tt>POST ... HTTP/1.1</tt>
     * or <tt>GET ... HTTP/1.1</tt>
     * request by looking at the first line from the stream.  If it is not,
     * an HTTP response code of 400 is immediately sent back.  If the incoming
         * request has any headers, they are read into a HashMap.  For POST requests
         * the method looks for the signature <tt>\n\n</tt> that signals the beginning of
     * the POST data, otherwise it reads CGI style parameters from
         * the request line.  If the double newline is not found, an HTTP response code
     * of 400 is sent.<p>
         * Next, the POST/GET data is analyzed.  There are two possibilities: either
         * it could be an MASRequest XML document, or it could be <code>name=value</code>
     * CGI parameters (GET only works this way).
     * If the data is an MASRequest document, it parses it and
     * bulids a {@link gov.nih.nlm.meme.xml.MEMEServiceRequest} object from the
         * document.  If the data takes the form of CGI parameters, it creates an empty
         * {@link gov.nih.nlm.meme.xml.MEMEServiceRequest} and populates it by adding
     * parameters.  There are eight special CGI variables that do not get added
     * to the request as parameters, they are:
     * <ul><li><code>service</code>: the value is treated as the requests service.</li>
         * <li><code>service</code>: the value is treated as the requests service.</li>
     * <li><code>session_id</code>: the value is treated as a session id.</li>
     * <li><code>session_initiate</code>: the value should be <code>true</code>
     *     or <code>false</code> indicating whether or not the request is
     *     attempting to initiate a session.</li>
         * <li><code>session_terminate</code>: the value should be <code>true</code>
     *     or <code>false</code> indicating whether or not the request is
     *     attempting to terminate an active session.</li>
         * <li><code>session_nosession</code>: the value should be <code>true</code>
     *     indicating that the request does not want session management</li>
         * <li><code>datasource_service</code>: the value is treated a mid service name
     *     specifying which datasource to connect to.</li>
         * <li><code>datasource_user</code>: the value is treated as the username with
     *     which to connect to the datasource.</li>
         * <li><code>datasource_password</code>: the value is treated as the password
     *     with which to connect to the datasource.</li>
     * </ul>
     * If the POST data is badly formatted, meaning it is either not a valid MASRequest
         * document, nor valid CGI parameters, then a HTTP response code of 400 is sent.
     *
     * @param in the {@link BufferedReader}
     * @return the {@link MEMEServiceRequest}
     */
    public MEMEServiceRequest parse(BufferedReader in) {

      MEMEServiceRequest request = null;
      StringBuffer document = null;
      try {

        // Look for "POST ... HTTP/1.1", GET ... HTTP/1.1
        String line = in.readLine();
        if (line == null) {
	    return null;
        }

        String[] tokens = FieldedStringTokenizer.split(line, " ");
        if ( (!line.startsWith("POST ") && !line.startsWith("GET ")) ||
            tokens.length != 3 || !tokens[2].startsWith("HTTP/")) {
          ProtocolParseException ppe =
              new ProtocolParseException(
              "Request requires form \"POST ... HTTP/1.1\" or \"GET ... HTTP/1.1\"");
          ppe.setDetail("request", line);
          throw ppe;
        }

        String method = tokens[0];
        MEMEToolkit.trace(
            "HTTPRequestListener.RequestHandler:getMEMEServerRequest(): - method is: " +
            method);
        String param = tokens[1];

        boolean blank_line = false;
        while ( (line = in.readLine()) != null) {
          if (line.equals("")) {
            blank_line = true;
            break;
          }
          try {
            // Split line
            StringTokenizer st = new StringTokenizer(line, ":");
            String key = st.nextToken();
            String value = st.nextToken();
            MEMEToolkit.trace(
                "HTTPRequestListener.RequestHandler:getMEMEServerRequest() - add header (" +
                key + "," + value + ")");
            headers.put(key, value);
          } catch (Exception e) {
            throw new ProtocolParseException("Badly formatted header");
          }
        }

        if (!blank_line) {
          throw new ProtocolParseException(
              "HTTP headers should be followed by a blank line");
        }

        // We need to read the rest into a string and pass the serializer
        // a string reader, otherwise it reads to the end of the sockets input
        // stream and closes it, thus closing the connection!
        document = new StringBuffer(10000);
        boolean mas_request = false;

        if (method.equals("GET")) {
          mas_request = false;
          tokens = FieldedStringTokenizer.split(param, "?");
          if (tokens.length != 2) {
            ProtocolParseException ppe =
                new ProtocolParseException(
                "Invalid GET request, must have form uri?name=value&name2=value2&...");
            ppe.setDetail("param", param);
            throw ppe;
          }
          document.append(tokens[1]);
        } else if (method.equals("POST")) {
          while ( (line = in.readLine()) != null) {
            // Determine if the document is a MASRequest or CGI params
            if (mas_request == false && line.indexOf("<MASRequest") != -1) {
              mas_request = true;

            }
            document.append(line);

            // Break at end of document, otherwise next readLine call will hang
            // waiting for more data
            if (line.indexOf("</MASRequest>") != -1 ||
                (mas_request == false && line.indexOf("service=") != -1)) {
              break;
            }
          }
        }

        // Process request according to the type of document
        if (mas_request) {
          // Request is a MASRequest
	    try {
          request = (MEMEServiceRequest) serializer.fromXML(new StringReader(
              document.toString()));
	    } catch (Exception e) {
		System.out.println("failed here");
		e.printStackTrace();
		throw e;
	    }
        } else {
          // Request is a CGI parameters
          // service = ...&....&....&
          request = new CGIStyleMEMEServiceRequest();
          ( (CGIStyleMEMEServiceRequest) request).setOutputStream(socket.
              getOutputStream());

          // Break document into name/value pairs
          StringTokenizer name_value = new StringTokenizer(document.toString(),
              "&");

          // Set default request values
          request.setNoSession(true);

          String user = null;
          String password = null;
          while (name_value.hasMoreTokens()) {
            String token = name_value.nextToken();
            String[] tokens2 = FieldedStringTokenizer.split(token, "=");
            if (tokens2.length != 2 && tokens2.length != 1) {
              ProtocolParseException ppe = new ProtocolParseException(
                  "Bad name/value pair");
              ppe.setDetail("name,value", name_value);
              throw ppe;
            }

            String name = URLDecoder.decode(tokens2[0], "UTF-8");
            String value = null;
            if (tokens2.length == 2) {
              value = URLDecoder.decode(tokens2[1], "UTF-8");
            } else {
              value = "";
              // CGI Style requests make use of eight special
              // parameters that set parts of the request other
              // than the parameters section.
              // 1. service - this sets the service name
              // 2. session_id - this sets the session_id
              // 3. session_initiate - this sets the initiate flag
              // 4. session_nosession - this sets the nosession flag
              // 5. session_terminate - this sets the terminate_session flag
              // 6. datasource_service - sets the mid_service flag
              // 7. datasource_user - sets the user
              // 8. datasource_password - sets the password

            }
            if (name.equals("service")) {
              request.setService(value);
            } else if (name.equals("session_id")) {
              request.setSessionId(value);
              request.setNoSession(false);
            } else if (name.equals("session_initiate")) {
              request.setInitiateSession(Boolean.valueOf(value).booleanValue());
            } else if (name.equals("session_nosession")) {
              request.setNoSession(Boolean.valueOf(value).booleanValue());
            } else if (name.equals("session_terminate")) {
              request.setTerminateSession(Boolean.valueOf(value).booleanValue());
            } else if (name.equals("datasource_service")) {
              request.setMidService(value);
            } else if (name.equals("datasource_user")) {
              user = value;
            } else if (name.equals("datasource_password")) {
              password = value;
            } else {
              request.addParameter(name, value, false);

            }
          }

          if (user != null && password != null) {
            request.setAuthentication(new PasswordAuthentication(user,
                password.toCharArray()));

          }
        }

      } catch (ProtocolParseException ppe) {
        http_response_code = 400;
        http_response = "Bad request - " + ppe.getMessage();
        ppe.setFatal(false);
        MEMEToolkit.handleError(ppe);
      } catch (IOException ioe) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to read from socket input stream.", ioe);
        http_response_code = 500;
        http_response =
            "Internal Server Error - Failed to read from socket input stream";
        MEMEToolkit.handleError(ere);
      } catch (XMLParseException xpe) {
        http_response_code = 400;
        http_response = "Bad request - Badly formatted XML document";
        xpe.setDetail("document", document.toString());
        xpe.setFatal(false);
        MEMEToolkit.handleError(xpe);
      } catch (ReflectionException re) {
        http_response_code = 500;
        http_response = "Internal Server Error - Failed to load class";
        re.setFatal(false);
        MEMEToolkit.handleError(re);
      } catch (Exception e) {
        http_response_code = 500;
        http_response = "Internal Server Error - " + e.getMessage();
        MEMEToolkit.handleError(e);
      }

      return request;

    } // end parse method
  } // end of inner class

} // end of class
