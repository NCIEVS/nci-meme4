<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="java.util.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>

<HTML>
<HEAD>
<TITLE>EditingGraph</TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.EditingReportBean" />
<jsp:setProperty name="bean" property="*" />

<SCRIPT language="JavaScript">
  function setImage() {
    window.document.images[0].height = 0;
    window.document.images[1].height = 300;
    return true;
  }
</SCRIPT>

</HEAD>
<BODY>

<SPAN id="blue"><CENTER>Action Graph</CENTER></SPAN>
<HR width="100%">

<TABLE WIDTH=100% >
  <TR>
  <TD align="center">
    <IMG name="img1" src='/images/wait.gif' height="300"/>
  <% StringBuffer url_buf = new StringBuffer();
       url_buf.append("http://")
              .append(request.getParameter("host"))
              .append(":")
              .append(request.getParameter("port"))
              .append("/?service=ActivityMonitor&function=")
              .append(request.getParameter("function"))
              .append("&datasource_service=")
              .append(request.getParameter("midService"))
              .append("&by_param=")
              .append(request.getParameter("by_param"))
              .append("&for_param=")
              .append(request.getParameter("for_param"));
       String[] of_params = request.getParameterValues("of_param");
       for (int i=0; i < of_params.length; i++) {
         url_buf.append("&of_param=")
                .append(of_params[i]);
       }
       String image_url = url_buf.toString();
    %>
    <br>
    <IMG name="img2" src="<%= image_url %>" onLoad="setImage();" height="0">
  </TD>
  </TR>
  <TR>
  <TD align="center"><INPUT type="button" value="Close" onClick="window.close(); return true;">
  </TD>
  </TR>
</TABLE>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</BODY>
</HTML>
