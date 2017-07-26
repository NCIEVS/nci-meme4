<%@ page contentType="text/html;charset=utf-8" errorPage= "IFrameErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.mrd.web.*" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.io.*" %>

<jsp:useBean id="release_bean" scope="session" class="gov.nih.nlm.mrd.beans.ReleaseBean" />

<%
  ReleaseClient rc = release_bean.getReleaseClient();
  String release = request.getParameter("release");
  if("html".equals(request.getParameter("action"))) {
      String name = rc.getReleaseInfo(release).getDocumentationUri() + "/" + request.getParameter("cui") + ".rpt.html"; // qareport file
      System.out.println(name);
	RequestDispatcher dispatcher =
  	  getServletContext().getRequestDispatcher(response.encodeURL("/CuiReportDetails.jsp?release=" + release + "&cui=" + request.getParameter("cui") + "&detail=View Report"));
	if (dispatcher != null) {
  	  System.out.println("Dispatching ....");

          // Downcast request objects
          HttpServletRequest hreq =
            (HttpServletRequest) request;
          HttpServletResponse hres =
            (HttpServletResponse) response;

          // Create a wrapper object that "catches" output
          LocalResponseWrapper lrw =
             new LocalResponseWrapper(hres);

          //System.out.println("do filter include");

          dispatcher.include(request, lrw);

          //System.out.println("Now post-process");

          // Now post-process the output
          String result = lrw.toString();

          // Save the HTML to file
	  PrintWriter pw =
          new PrintWriter(
            new OutputStreamWriter(
              new FileOutputStream(name,false),"UTF-8"));
	      pw.println(result);
	      pw.close();
              //System.out.println("Return ....");
	    }
  } else if("build".equals(request.getParameter("action"))) {
      rc.generateCuiComparisonReport(request.getParameter("cui"),release,request.getParameter("compareTo"));
%>
    <jsp:forward page="mrd/controller">
      <jsp:param name="state" value="CuiReportSummary" />
      <jsp:param name="cui" value="<%= request.getParameter(\"cui\")%>" />
      <jsp:param name="release" value="<%= request.getParameter(\"release\")%>" />
      <jsp:param name="compareTo" value="<%= request.getParameter(\"compareTo\")%>" />
    </jsp:forward>
<%
  }
%>
