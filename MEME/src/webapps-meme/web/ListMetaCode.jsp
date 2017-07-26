<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.MetaCode, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="metacode_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="metacode_bean" property="*" />

<TITLE>Category List</TITLE>
</HEAD>

<% AuxiliaryDataClient aux_client = metacode_bean.getAuxiliaryDataClient();
   String category = request.getParameter("category");
   MetaCode[] codes = aux_client.getMetaCodesByType(category);
   ArrayList current = new ArrayList(codes.length);
   int j=0;
   for( int i=0; i<codes.length; i++) {
	if((request.getParameter("searchkey") == null ||
                codes[i].getCode().toUpperCase().startsWith(request.getParameter("searchkey"))))
		 {
		current.add(codes[i]);
	}
   }
%>

<BODY bgcolor="#ffffff">
<SPAN id="blue"><CENTER>List of <i><%=category%></i> Properties</CENTER></SPAN>
<HR WIDTH=100%>
 <i> Click <a href="controller?state=EditMetaCode&command=Insert&category=<%=category%>">here</a> to add a new code in this category.</i>
    <blockquote>
     <center> [
     <% for( char c = 'A'; c <= 'Z'; c++) { %>
     <a href="controller?state=ListMetaCode&searchkey=<%=c%>&category=<%=category%>"><%=c%></a>
     <% } %>
     ]
     </center>

    </blockquote>

    <blockquote>

    <br>&nbsp;<br>
    <form method="GET" action="controller">
      <input type="hidden" name="state" value="EditMetaCode">
      <input type="hidden" name="command" value="Edit">
      <input type="hidden" name="rowid" value="">
      <center><table CELLPADDING=2 WIDTH="90%" BORDER=1  >
    <tr><td valign="top" width="15%"><b><font size="-1">Category</font></b></td>
           <td valign="top" width="30%"><b><font size="-1">Code</font></b></td>
           <td valign="top" width="50%"><b><font size="-1">Description</font></b></td>
           <td valign="top" width="5%">&nbsp;</td>
    </tr>
	<% for (int i=0; i<current.size(); i++) {
		MetaCode code = (MetaCode)current.get(i); %>
      <tr> <td valign="top"><%=code.getType()%></td> <td valign="top"><%=code.getCode()%></td>
		   <td valign="top"><%=(code.getValue() == null ? "" : code.getValue()) %></td>
		   <td valign="top">
	  <input type="button" value="Edit"
	      onMouseOver='window.status="Edit Property"; return true;'
              onClick=' this.form.rowid.value="<%=code.getIdentifier()%>"; this.form.submit(); return true;'> </td>
	<% } %>
        </tr>
    </table></center>
    </form>
    </blockquote>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
    </body>
</html>
