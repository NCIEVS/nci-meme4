<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.integrity.IntegrityCheck, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
EditCheckComplete
</title>
</head>
<jsp:useBean id="integrity_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<%
    IntegrityCheck check = new IntegrityCheck.Default();
    pageContext.setAttribute("check",check);
%>
<jsp:setProperty name="check" property="*" />
<body>
<%  String command = request.getParameter("command");
    if("Insert".equals(command)) {
      integrity_bean.getAuxiliaryDataClient().addIntegrityCheck(check);
      out.println("<p>The Integrity Check with following information was inserted.</p>");
    }else if("Update".equals(command)) {
      integrity_bean.getAuxiliaryDataClient().setIntegrityCheck(check);
      out.println("<p>The Integrity Check " + check.getName() + " was modified using following information.</p>");
    }else if("Delete".equals(command)) {
      integrity_bean.getAuxiliaryDataClient().removeIntegrityCheck(check);
      out.println("<p>The Integrity Check <b>" + check.getName() + "</b> was successfully deleted.</p>");
    }
    if(command.equals("Insert") || command.equals("Update")) {
%>
        <table width="90%">
          <tr><td width="25%" valign="top"><font size="-1"><b>Check Name:</b></font></td>
          <td><font size="-1"><jsp:getProperty name="check" property="name"/></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Status:</b></font></td>
          <td><font size="-1"><%= check.isActive()?"Active":"Inactive"%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Check Name:</b></font></td>
          <td><font size="-1"><%= check.isFatal()?"Fatal":"Ignore"%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Short Description:</b></font></td>
          <td><font size="-1"><jsp:getProperty name="check" property="shortDescription"/></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Long Description:</b></font></td>
          <td><font size="-1"><jsp:getProperty name="check" property="description"/></font></td></tr>
        </table>
<% }  %>
        </center><p>
	Click <a href="<%= request.getRequestURL() %>?state=IntegrityEditor">here</a> to return to the integrity check editor page.
        </p>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html>
