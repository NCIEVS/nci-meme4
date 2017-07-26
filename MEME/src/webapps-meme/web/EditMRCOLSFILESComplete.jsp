<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
EditMRDOCComplete
</title>
</head>
<jsp:useBean id="mrcolsfiles_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<body>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Edit Complete</title>
<h2><center><%=request.getParameter("command")%> category <i><%=request.getParameter("category")%></i></center></h2>

</HEAD>

<BODY bgcolor="#ffffff">

<HR WIDTH=100%>

<%  AuxiliaryDataClient aux_client = mrcolsfiles_bean.getAuxiliaryDataClient();
   MetaProperty[] properties = aux_client.getMetaProperties();
   MetaProperty property = null;
   String message;
   String category = request.getParameter("category");
   for(int i=0; i<properties.length; i++) {
	if(properties[i].getIdentifier().toString().equals(request.getParameter("rowid"))) {
		property = properties[i];
	}
   }
   if("Delete".equals(request.getParameter("command"))) {
	aux_client.removeMetaProperty(property);
	out.println("The " + ("MRCOLS".equals(category) ? "Column" : "File") +" name " + property.getKey() + " was successfully deleted<p>");
   } else if("Insert".equals(request.getParameter("command"))) {
	property = new MetaProperty();
	property.setKey(request.getParameter("key"));
	property.setKeyQualifier(request.getParameter("category"));
	property.setValue(request.getParameter("value"));
	property.setDescription(request.getParameter("explain"));
	property.setDefinition(request.getParameter("definition"));
	property.setExample(request.getParameter("example"));
	aux_client.addMetaProperty(property);
	out.println("The entry with the following information was inserted:<p>");
   } else if("Update".equals(request.getParameter("command"))) {
	aux_client.removeMetaProperty(property);
	property.setKey(request.getParameter("key"));
	property.setKeyQualifier(request.getParameter("category"));
	property.setValue(request.getParameter("value"));
	property.setDescription(request.getParameter("explain"));
	property.setDefinition(request.getParameter("definition"));
	property.setExample(request.getParameter("example"));
	aux_client.addMetaProperty(property);
	out.println("The " + ("MRCOLS".equals(category) ? "Column" : "File") +" name <i>" + property.getKey() + "</i> was modified using the following information:<p>");
   }
   if("Insert".equals(request.getParameter("command")) || "Update".equals(request.getParameter("command"))) {
%>


        <center>
        <table width="90%">
          <tr><td width="25%" valign="top"><font size="-1"><b>Category:</b></font></td>
          <td><font size="-1"><%=property.getKeyQualifier()%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b><%=("MRCOLS".equals(category) ? "Column Name" : "File Name")%>:</b></font></td>
          <td><font size="-1"><%=property.getKey()%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b><%=("MRCOLS".equals(category) ? "Data Type" : "List of Columns")%>:</b></font></td>
          <td><font size="-1"><%=property.getValue()%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Explain:</b></font></td>
          <td><font size="-1"><%=property.getDescription()%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Definition:</b></font></td>
          <td><font size="-1"><%=(property.getDefinition() == null ? "" : property.getDefinition())%></font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Example:</b></font></td>
          <td><font size="-1"><%=(property.getExample() == null ? "" : property.getExample())%></font></td></tr>
        </table>
        </center><p>
	<% } %>

	Click <a href="controller?state=ListMRCOLSFILES&category=<%=category%>">here</a> to return to the <%=category%> list page.

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
    </body>
</html>
