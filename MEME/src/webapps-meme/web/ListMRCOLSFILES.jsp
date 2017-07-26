<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.MetaProperty, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="mrcolsfiles_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="mrcolsfiles_bean" property="*" />

<TITLE>Category List</TITLE>
</HEAD>

<% AuxiliaryDataClient aux_client = mrcolsfiles_bean.getAuxiliaryDataClient();
   String category = request.getParameter("category");
   MetaProperty[] properties = aux_client.getMetaPropertiesByKeyQualifier(category);
   ArrayList current = new ArrayList(properties.length);
   int j=0;
   for( int i=0; i<properties.length; i++) {
	if((request.getParameter("searchkey") == null ||
                properties[i].getKey().startsWith(request.getParameter("searchkey"))))
		 {
		current.add(properties[i]);
	}
   }
%>

<BODY bgcolor="#ffffff">
<SPAN id="blue"><CENTER>List of <i><%= category%></i> Properties</CENTER></SPAN>
<HR WIDTH=100%>
 <i> Click <a href="controller?state=EditMRCOLSFILES&command=Insert&category=<%=category%>">here</a> to add a new <%=("MRFILES".equals(category) ? "File" : "Column")%> name.</i>
    <blockquote>
     <center> [
     <% for( char c = 'A'; c <= 'Z'; c++) { %>
     <a href="controller?state=ListMRCOLSFILES&searchkey=<%=c%>&category=<%=category%>"><%=c%></a>
     <% } %>
     ]
     </center>

    </blockquote>

    <blockquote>

    <br>&nbsp;<br>
    <form method="GET" action="controller">
      <input type="hidden" name="state" value="EditMRCOLSFILES">
      <input type="hidden" name="command" value="Edit">
      <input type="hidden" name="rowid" value="">
      <center><table CELLPADDING=2 WIDTH="90%" BORDER=1  >
    <tr><td valign="top"><b><font size="-1"><%=("MRCOLS".equals(category) ? "Column Name" : "File Name")%></font></b></td>
           <td valign="top"><b><font size="-1"><%=("MRCOLS".equals(category) ? "Data Type" : "List of Columns")%></font></b></td>
           <td valign="top"><b><font size="-1">Description</font></b></td>
           <td valign="top"><b><font size="-1">Definition</font></b></td>

           <td valign="top"><b><font size="-1">Example</font></b></td>
           <td valign="top">&nbsp;</td>
    </tr>
	<% for (int i=0; i<current.size(); i++) {
		MetaProperty property = (MetaProperty)current.get(i); %>
      <tr> <td valign="top"><%=property.getKey()%></td> <td valign="top"><%=property.getValue()%></td>
		   <td valign="top"><%=(property.getDescription() == null ? "" : property.getDescription()) %></td>
		   <td valign="top"><%=(property.getDefinition() == null ? "" : property.getDefinition()) %></td>
		   <td valign="top"><%=(property.getExample() == null ? "" : property.getExample())%></td>
		   <td valign="top">
	  <input type="button" value="Edit"
	      onMouseOver='window.status="Edit Property"; return true;'
              onClick=' this.form.rowid.value="<%=property.getIdentifier()%>"; this.form.submit(); return true;'> </td>
	<% } %>
        </tr>
    </table></center>
    </form>
    </blockquote>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
    </body>
</html>
