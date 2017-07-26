<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.MetaProperty, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="mrdoc_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="mrdoc_bean" property="*" />

<TITLE>DOCKEY, TYPE List</TITLE>
</HEAD>

<% AuxiliaryDataClient aux_client = mrdoc_bean.getAuxiliaryDataClient();
   String entry = request.getParameter("entry");
   String searchkey = request.getParameter("searchkey");
   MetaProperty[] properties = aux_client.getMetaPropertiesByKeyQualifier(entry.substring(0,entry.indexOf('~')));
   ArrayList current = new ArrayList(properties.length);
   for( int i=0; i<properties.length; i++) {
	if(properties[i].getKey().equals(entry.substring(entry.indexOf('~')+1)) &&
        	(searchkey == null || (properties[i].getValue() != null &&
                properties[i].getValue().toUpperCase().startsWith(searchkey))))
		 {
		current.add(properties[i]);
	}
   }
%>

<BODY bgcolor="#ffffff">
<SPAN id="blue"><CENTER>List of <i><%=entry.substring(0,entry.indexOf('~'))%></i>, <i><%=entry.substring(entry.indexOf('~')+1)%></i> Entries</CENTER></SPAN>
<HR WIDTH=100%>
 <i> Click <a href="controller?state=EditMRDOC&command=Insert&entry=<%=entry%>">here</a> to add a new entry for this DOCKEY, TYPE.</i>
    <blockquote>
     <center> [
     <% for( char c = 'A'; c <= 'Z'; c++) { %>
     <a href="controller?state=ListMRDOC&searchkey=<%=c%>&entry=<%=entry%>"><%=c%></a>
     <% } %>
     ]
     </center>

    </blockquote>

    <blockquote>

    <br>&nbsp;<br>
    <form method="GET" action="controller">
      <input type="hidden" name="state" value="EditMRDOC">
      <input type="hidden" name="command" value="Edit">
      <input type="hidden" name="rowid" value="">
      <center><table CELLPADDING=2 WIDTH="90%" BORDER=1  >
    <tr><td valign="top"><b><font size="-1">DOCKEY</font></b></td>
           <td valign="top"><b><font size="-1">VALUE</font></b></td>
           <td valign="top"><b><font size="-1">TYPE</font></b>
           <td valign="top"><b><font size="-1">EXPLAIN</font></b></td>
           <td valign="top"><b><font size="-1">DEFINITION</font></b></td>

           <td valign="top"><b><font size="-1">EXAMPLE</font></b></td>
           <td valign="top">&nbsp;</td>
    </tr>
	<% for (int i=0; i<current.size(); i++) {
		MetaProperty property = (MetaProperty)current.get(i); %>
      <tr> <td valign="top"><%=property.getKeyQualifier()%></td> <td valign="top"><%=(property.getValue()== null ? "&nbsp;" : property.getValue())%></td> <td valign="top"><%=property.getKey()%></td>
		   <td valign="top"><%=(property.getDescription() == null ? "&nbsp;" : property.getDescription()) %></td>
		   <td valign="top"><%=(property.getDefinition() == null ? "&nbsp;" : property.getDefinition()) %></td>
		   <td valign="top"><%=(property.getExample() == null ? "&nbsp;" : property.getExample())%></td>
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
