<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
Edit Code Map Complete
</title>
</head>
<jsp:useBean id="metacode_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<body>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Edit Complete</title>
<h2><center><%=request.getParameter("command")%> category <i><%=request.getParameter("category")%></i></center></h2>

</HEAD>

<BODY bgcolor="#ffffff">

<HR WIDTH=100%>

<%  AuxiliaryDataClient aux_client = metacode_bean.getAuxiliaryDataClient();
   MetaCode[] codes = aux_client.getMetaCodes();
   MetaCode code = null;
   String message;
   String category = request.getParameter("category");
   for(int i=0; i<codes.length; i++) {
	if(codes[i].getIdentifier().toString().equals(request.getParameter("rowid"))) {
		code = codes[i];
	}
   }
   if("Delete".equals(request.getParameter("command"))) {
	aux_client.removeMetaCode(code);
	out.println("The code " + code.getCode() + " was successfully deleted<p>");
   } else if("Insert".equals(request.getParameter("command"))) {
	code = new MetaCode();
	code.setCode(request.getParameter("key"));
	code.setType(request.getParameter("category"));
	code.setValue(request.getParameter("value"));
	aux_client.addMetaCode(code);
	out.println("The category with the following information was inserted:<p>");
   } else if("Update".equals(request.getParameter("command"))) {
	aux_client.removeMetaCode(code);
	code.setCode(request.getParameter("key"));
	code.setType(request.getParameter("category"));
	code.setValue(request.getParameter("value"));
	aux_client.addMetaCode(code);
	out.println("The code <i>" + code.getCode() + "</i> was modified using the following information:<p>");
   }
   if("Insert".equals(request.getParameter("command")) || "Update".equals(request.getParameter("command"))) {
%>


        <center>
        <table width="90%">
          <tr><td width="25%" valign="top"><font size="-1"><b>Category:</b></font></td>
          <td><font size="-1"><%=code.getType()%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Code:</b></font></td>
          <td><font size="-1"><%=code.getCode()%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Description:</b></font></td>
          <td><font size="-1"><%=code.getValue()%></font></td></tr>
        </table>
        </center><p>
	<% } %>

	Click <a href="controller?state=ListMetaCode&category=<%=category%>">here</a> to return to the category list page.

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
    </body>
</html>
