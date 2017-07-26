/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.web
 * Object:  Controller
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.web;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.client.ClientConstants;
import gov.nih.nlm.meme.exception.InitializationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages flow of MRD web applications.
 *
 * @author TTN, BAC
 */
public class Controller extends HttpServlet {

  private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

  private static Properties states = new Properties();

  /**
  * Initializes the server request.
  * @param config the {@link ServletConfig}
  * @throws ServletException if initialization failed
  */
  public void init(ServletConfig config) throws ServletException {

    //
    // Load properties
    //
    URL url = Controller.class.getResource("/mrd.prop");
    System.setProperty("meme.properties.file",url.toString().substring(5));
    try {
      MEMEToolkit.initialize();
    } catch(InitializationException ie) {
      throw new ServletException("Failed to initialize MEMEToolkit",ie);
    }

    //
    // Load state mappings
    //
    try {
      states.load(Controller.class.getResourceAsStream("/states.prop"));
    } catch(IOException ioe) {
      throw new ServletException("Failed to load the states.prop properties file",ioe);
    }
    super.init(config);
  }

  /**
   * Handles GET request.
   * @param request the {@link HttpServletRequest}
   * @param response the {@link HttpServletResponse}
   * @throws ServletException if failed to handle GET request
   * @throws IOException If IO error is detected when the servlet handles the
   * GET request
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException   {
    String state = request.getParameter("state");
    if(state !=null && states.containsKey(state)) {
        System.out.println("Forwarding " + (String)states.get(state));
        getServletContext().getRequestDispatcher((String)states.get(state)).forward(request,response);
    } else {
      response.setContentType(CONTENT_TYPE);
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>Controller</title></head>");
      out.println("<body>");
      out.println("<p>The servlet has received a GET. This is the reply.</p>");
      out.println("meme.client.server.host " + MEMEToolkit.getProperty(ClientConstants.SERVER_HOST));
      out.println("</body></html>");
      out.close();
    }
  }

  /**
   * Handles PUT request.
   * @param request the {@link HttpServletRequest}
   * @param response the {@link HttpServletResponse}.
   * @throws ServletException if failed to handle PUT request
   * @throws IOException If IO error is detected when the servlet handles the
   * PUT request
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String state = request.getParameter("state");
    if(state !=null && states.containsKey(state)) {
        System.out.println("Forwarding " + (String)states.get(state));
        getServletContext().getRequestDispatcher((String)states.get(state)).forward(request,response);
    } else {
      response.setContentType(CONTENT_TYPE);
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>Controller</title></head>");
      out.println("<body>");
      out.println("<p>The servlet has received a POST. This is the reply.</p>");
      out.println("</body></html>");
      out.close();
    }
  }
}