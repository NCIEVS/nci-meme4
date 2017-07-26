<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
          errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
Edit MRCOLSFILES
</title>
</head>
<jsp:useBean id="mrcolsfiles_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<h2><center>Edit MRCOLSFILES</center></h2>

<BODY bgcolor="#ffffff">

<HR WIDTH=100%>
<% AuxiliaryDataClient aux_client = mrcolsfiles_bean.getAuxiliaryDataClient();
   MetaProperty[] properties = aux_client.getMetaProperties();
   MetaProperty property = null;
   Hashtable seen = new Hashtable(properties.length);
   String category = request.getParameter("category");
   for(int i=0; i<properties.length; i++) {
	if(properties[i].getIdentifier().toString().equals(request.getParameter("rowid"))) {
		property = properties[i];
	}
   }

   if("Insert".equals(request.getParameter("command"))) {
	property = new MetaProperty();
	property.setKeyQualifier(category);
   }
%>
<br>&nbsp;
    <form method="GET" action="controller">
      <input type="hidden" name="state" value="EditMRCOLSFILESComplete">
      <input type="hidden" name="rowid" value="<%=(property.getIdentifier() == null ? "" : property.getIdentifier().toString())%>">
      <input type="hidden" name="command" value="">

      <center><table CELLPADDING=2 WIDTH="90%"  >
      <tr >

	<td width="25%">Category:</td><td>
	     <select name="category">
  	<option <%=("MRCOLS".equals(property.getKeyQualifier()) ? "SELECTED" : "") %>>MRCOLS</option>
  	<option <%=("MRFILES".equals(property.getKeyQualifier()) ? "SELECTED" : "") %>>MRFILES</option>
 	</select>

	     </td>
      </tr>

      <tr >
	<td width="25%"><%=("MRCOLS".equals(property.getKeyQualifier()) ? "Column Name" : "File Name")%>:</td><td>
	     <input type="text" name="key" value="<%=(property.getKey() == null ? "" : property.getKey())%>">
	     </td>
      </tr>
      <tr >
	<td width="25%"><%=("MRCOLS".equals(property.getKeyQualifier()) ? "Data Type" : "List of Columns")%>:</td><td>
	     <textarea name="value" wrap="soft" cols="60" rows="2"><%=(property.getValue() == null ? "" : property.getValue())%></textarea>
	     </td>
      </tr>
      <tr >

	<td align="left" width="25%">Explain:</td>
	     <td><textarea name="explain" wrap="soft" cols="60" rows="4"><%=(property.getDescription() == null ? "" : property.getDescription())%></textarea></td>
      </tr>
      <tr >
	<td align="left" width="25%">Definition:</td>
	     <td><textarea name="definition" wrap="soft" cols="60" rows="4"><%=(property.getDefinition() == null ? "" : property.getDefinition())%></textarea></td>
      </tr>

      <tr >
	<td align="left" width="25%">Example:</td>
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
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
    </body>
</html>
