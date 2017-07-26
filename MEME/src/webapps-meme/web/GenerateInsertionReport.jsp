<%@ page contentType="text/html;charset=utf-8" errorPage= "ErrorPage.jsp"%>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Vector" %>
<%@ page import="gov.nih.nlm.meme.exception.MEMEException" %>
<%@ page import="gov.nih.nlm.meme.beans.WorkLogBean" %>
<%@ page import="gov.nih.nlm.meme.action.WorkLog" %>
<%@ page import="gov.nih.nlm.mrd.web.LocalResponseWrapper" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.io.*" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<title>
GenerateInsertionReport
</title>
</head>
<body bgcolor="#ffffff">
  <pre> Insertion Report is generated successfully. </pre>
<html:form action="/Close.do">
  <TABLE WIDTH=90% >
    <TD align="center">
  <html:button property="" onclick="window.close();return true;">Close
  </html:button>
    </TD>
  </TABLE>
</html:form>
<jsp:useBean id="insertionReportBean" scope="session" class="gov.nih.nlm.meme.beans.WorkLogBean" />
  <%
  WorkLog log = insertionReportBean.getWorkLog();
            RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher(
                            "/viewInsertionReportDetails.do?view=html");
            String name = "." + log.getAuthority().toString().toLowerCase() + ".html";
            if(((String)request.getParameter("type")).indexOf("Test") != -1) {
                name = "Sources/TEST" + name;
            } else {
                name = "Sources/INSERTION" + name;
            }
            name = getServletContext().getRealPath(name);
            if (dispatcher != null) {
              //System.out.println("Dispatching ....");
              // Downcast request objects
              HttpServletRequest hreq = (HttpServletRequest) request;
              HttpServletResponse hres = (HttpServletResponse) response;

              // Create a wrapper object that "catches" output
              LocalResponseWrapper lrw = new LocalResponseWrapper(hres);

              //System.out.println("do filter include");

              dispatcher.include(request, lrw);

              //System.out.println("Now post-process");

              // Now post-process the output
              String result = lrw.toString();

              // Save the HTML to file
              try {
                PrintWriter pw =
                new PrintWriter(
                new OutputStreamWriter(
                new FileOutputStream(name,false),"UTF-8"));
                pw.println(result);
                pw.close();
                //System.out.println("Return ....");
            } catch(FileNotFoundException fnfe) {
              MEMEException me = new MEMEException("Failed to create a file");
              me.setDetail("message","Inform administrator to configure a link to Sources in web appliation");
              me.setDetail("file",name);
              throw me;
            }
          }
  %>
</body>
</html>
