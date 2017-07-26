/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  Controller
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.client.ClientConstants;
import gov.nih.nlm.meme.client.ClientToolkit;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Hashtable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a controller class.
 *
 * @author Brian Carlsen
 */

public class Controller extends HttpServlet
{

  //
  // Fields
  //

  private static final String CONTENT_TYPE = "text";

  private Hashtable ht = new Hashtable();

  //
  // Methods
  //

  /**
   * Initializes the server request.
   * @param config An object {@link ServletConfig}.
   * @throws ServletException if initialization failed.
   */
  public void init(ServletConfig config) throws ServletException
  {
    URL url = Controller.class.getResource("/meme.prop");
    System.setProperty("meme.properties.file",url.toString().substring(5));
    try {
      ClientToolkit.initialize();
      ClientToolkit.setProperty(ClientConstants.SERVER_HOST,
        MIDServices.getService("meme-server-host"));
      ClientToolkit.setProperty(ClientConstants.SERVER_PORT,
        MIDServices.getService("meme-server-port"));
    } catch (Exception e) {
      throw new ServletException(e);
    }

    // map states to jsp's
    ht.put("ActionHarvester","/ActionHarvester.jsp");
    ht.put("ActionReport","/ActionReport.jsp");
    ht.put("EditingReport","/EditingReport.jsp");
    ht.put("ActionDetails","/ActionDetails.jsp");
    ht.put("WorklistLookup","/WorklistLookup.jsp");
    ht.put("AdminClient","/AdminClient.jsp");
    ht.put("ServerStatistics","/ServerStatistics.jsp");
    ht.put("ServerLog","/ServerLog.jsp");
    ht.put("ShutdownServer","/ShutdownServer.jsp");
    ht.put("EditingGraph","/EditingGraph.jsp");
    ht.put("ActionSequence","/ActionSequence.jsp");
    ht.put("ActionSequenceProgress","/ActionSequenceProgress.jsp");
    ht.put("SessionLog","/SessionLog.jsp");
    ht.put("IntegrityEditor","/IntegrityEditor.jsp");
    ht.put("EditIntegrityCheck","/EditIntegrityCheck.jsp");
    ht.put("EditCheckComplete","/EditCheckComplete.jsp");
    ht.put("EditVector","/EditVector.jsp");
    ht.put("EditVectorComplete","/EditVectorComplete.jsp");
    ht.put("TestSuiteSet","/TestSuiteSet.jsp");
    ht.put("DisplayTestSuiteSets","/DisplayTestSuiteSets.jsp");
    ht.put("RunTestSuiteSet","/RunTestSuiteSet.jsp");
    ht.put("GetTestSuiteLog","/GetTestSuiteLog.jsp");
    ht.put("IFrameErrorHandler","/IFrameErrorHandler.jsp");
    ht.put("ContentViewEditor","/ContentViewEditor.jsp");
    ht.put("EditContentView","/EditContentView.jsp");
    ht.put("EditContentViewComplete","/EditContentViewComplete.jsp");
    ht.put("EditContentViewMembers","/EditContentViewMembers.jsp");
    ht.put("EditContentViewMembersComplete","/EditContentViewMembersComplete.jsp");
    ht.put("ViewContentViewMembers","/ViewContentViewMembers.jsp");
    ht.put("ServerLifeCycle","/ServerLifeCycle.jsp");
    ht.put("SourceInfoEditor","/SourceInfoEditor.jsp");
    ht.put("EditSourceInfo","/EditSourceInfo.jsp");
    ht.put("EditSourceInfoComplete","/EditSourceInfoComplete.jsp");
    ht.put("MRDOCEditor","/MRDOCEditor.jsp");
    ht.put("EditMRDOC","/EditMRDOC.jsp");
    ht.put("ListMRDOC","/ListMRDOC.jsp");
    ht.put("EditMRDOCComplete","/EditMRDOCComplete.jsp");
    ht.put("MRCOLSFILESEditor","/MRCOLSFILESEditor.jsp");
    ht.put("EditMRCOLSFILES","/EditMRCOLSFILES.jsp");
    ht.put("ListMRCOLSFILES","/ListMRCOLSFILES.jsp");
    ht.put("EditMRCOLSFILESComplete","/EditMRCOLSFILESComplete.jsp");
    ht.put("MetaPropertyEditor","/MetaPropertyEditor.jsp");
    ht.put("EditMetaProperty","/EditMetaProperty.jsp");
    ht.put("ListMetaProperty","/ListMetaProperty.jsp");
    ht.put("EditMetaPropertyComplete","/EditMetaPropertyComplete.jsp");
    ht.put("MetaCodeEditor","/MetaCodeEditor.jsp");
    ht.put("EditMetaCode","/EditMetaCode.jsp");
    ht.put("ListMetaCode","/ListMetaCode.jsp");
    ht.put("EditMetaCodeComplete","/EditMetaCodeComplete.jsp");
    ht.put("ViewCodeDescription","/ViewCodeDescription.jsp");
    ht.put("EditSourceContactInfo","/EditSourceContactInfo.jsp");
    super.init(config);
  }

  /**
   * Handles GET request.
   * @param request An object {@link HttpServletRequest}.
   * @param response An object {@link HttpServletResponse}.
   * @throws ServletException if failed to handle GET request.
   * @throws IOException If IO error is detected when the servlet handles the
   * GET request.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String state = request.getParameter("state");
    if (state != null && ht.containsKey(state)) {
      getServletContext().getRequestDispatcher((String)ht.get(state)).forward(request,response);
    //} else {
     // response.setContentType(CONTENT_TYPE);
     // PrintWriter out = response.getWriter();
     // out.println("<html>");
     // out.println("<head><title>Error</title></head>");
     // out.println("<body>");
     // out.println("<p>Error: Please enter the correct URL</p>");
     // out.println("meme.client.server.host " + ClientToolkit.getProperty(ClientConstants.SERVER_HOST));
     // out.println("state= " + state);
      //out.println("</body></html>");
      //out.close();
    }
  }

  /**
   * Handles PUT request.
   * @param request An object {@link HttpServletRequest}.
   * @param response An object {@link HttpServletResponse}.
   * @throws ServletException if failed to handle PUT request.
   * @throws IOException If IO error is detected when the servlet handles the
   * PUT request.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    doGet(request,response);
  }
}
