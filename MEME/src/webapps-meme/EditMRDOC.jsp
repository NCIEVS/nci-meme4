<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
          errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
Edit MRDOC
</title>
</head>
<jsp:useBean id="mrdoc_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<h2><center>Edit MRDOC</center></h2>

<BODY bgcolor="#ffffff">

<HR WIDTH=100%>
<% AuxiliaryDataClient aux_client = mrdoc_bean.getAuxiliaryDataClient();
   MetaProperty[] properties = aux_client.getMetaProperties();
   MetaProperty property = null;
   Hashtable seen = new Hashtable(properties.length);
   for(int i=0; i<properties.length; i++) {
	if(properties[i].getIdentifier().toString().equals(request.getParameter("rowid"))) {
		property = properties[i];
	}
   }

   if("Insert".equals(request.getParameter("command"))) {
   	String entry = request.getParameter("entry");
	property = new MetaProperty();
	property.setKey(entry.substring(entry.indexOf('~')+1));
	property.setKeyQualifier(entry.substring(0,entry.indexOf('~')));
   }
%>
<br>&nbsp;
    <form method="GET" action="controller">
      <input type="hidden" name="state" value="EditMRDOCComplete">
      <input type="hidden" name="rowid" value="<%=(property.getIdentifier() == null ? "" : property.getIdentifier().toString())%>">
      <input type="hidden" name="command" value="">

      <center><table CELLPADDING=2 WIDTH="90%"  >
      <tr >

	<td width="25%">DOCKEY:</td><td>
	     <select name="dockey">
	<%    for(int i=0; i<properties.length; i++) {
	if(!seen.containsKey(properties[i].getKeyQualifier())) {
	%>
  	<option <%=(properties[i].getKeyQualifier().equals(property.getKeyQualifier()) ? "SELECTED" : "") %>><%=properties[i].getKeyQualifier()%></option>
	<% }
           seen.put(properties[i].getKeyQualifier(),properties[i].getKeyQualifier());
	}
	%>
 </select>

	     </td>
      </tr>
      <tr >
	<td width="25%">TYPE:</td><td>

	     <input type="text" name="type" value ="<%=property.getKey()%>">
	     </td>
      </tr>
      <tr >
	<td width="25%">VALUE:</td><td>
	     <textarea name="value" wrap="soft" cols="60" rows="2"><%=(property.getValue() == null ? "" : property.getValue())%></textarea>
	     </td>
      </tr>
      <tr >

	<td align="left" width="25%">EXPLAIN:</td>
	     <td><textarea name="explain" wrap="soft" cols="60" rows="4"><%=(property.getDescription() == null ? "" : property.getDescription())%></textarea></td>
      </tr>
      <tr >
	<td align="left" width="25%">DEFINITION:</td>
	     <td><textarea name="definition" wrap="soft" cols="60" rows="4"><%=(property.getDefinition() == null ? "" : property.getDefinition())%></textarea></td>
      </tr>

      <tr >
	<td align="left" width="25%">EXAMPLE:</td>
	     <td><textarea name="example" wrap="soft" cols="60" rows="4"><%=(property.getExample() == null ? "" : property.getExample())%></textarea></td>
      </tr>
      <tr >
        <td colspan=2>
	  <center></b>
          <font size="-1">

	  <input type="button" value="Insert"
	      onMouseOver='window.status="Insert New Entry"; return true;'
              onClick=' this.form.command.value="Insert"; this.form.submit(); return true;'>

          &nbsp;&nbsp;&nbsp;
	  <input type="button" value="Update"
	      onMouseOver='window.status="Update selected Entry"; return true;'
              onClick='this.form.command.value="Update"; this.form.submit(); return true;'>
          &nbsp;&nbsp;&nbsp;
	  <input type="button" value="Delete"
	      onMouseOver='window.status="Delete selected Entry"; return true;'
              onClick='this.form.command.value="Delete"; this.form.submit(); return true;'>

          &nbsp;&nbsp;&nbsp;
           <input type="button" value="Cancel" onClick="history.go(-1)"></b>
          </font></b>

	  </center>
	</td>
      </tr>
    </table>
    </center>
  </form>
</p>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
    </body>
</html>
