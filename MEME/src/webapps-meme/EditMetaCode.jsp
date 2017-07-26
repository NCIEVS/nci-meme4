<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
          errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
Edit Code Map
</title>
</head>
<jsp:useBean id="metacode_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<h2><center>Edit Code Map</center></h2>

<BODY bgcolor="#ffffff">

<HR WIDTH=100%>
<% AuxiliaryDataClient aux_client = metacode_bean.getAuxiliaryDataClient();
   MetaCode[] codes = aux_client.getMetaCodes();
   MetaCode code = null;
   Hashtable seen = new Hashtable(codes.length);
   for(int i=0; i<codes.length; i++) {
	if(codes[i].getIdentifier().toString().equals(request.getParameter("rowid"))) {
		code = codes[i];
	}
   }
   String category = request.getParameter("category");

   if("Insert".equals(request.getParameter("command"))) {
	code = new MetaCode();
	code.setType(category);
   }
%>
<br>&nbsp;
    <form method="GET" action="controller">
      <input type="hidden" name="state" value="EditMetaCodeComplete">
      <input type="hidden" name="rowid" value="<%=(code.getIdentifier() == null ? "" : code.getIdentifier().toString())%>">
      <input type="hidden" name="command" value="">

      <center><table CELLPADDING=2 WIDTH="90%"  >
      <tr >

	<td width="25%">Category:</td><td>
	     <select name="category">
    <%

       Arrays.sort(codes);
       seen = new Hashtable();
       for(int i=0; i<codes.length; i++) {
	if(!seen.containsKey(codes[i].getType())) {
		seen.put(codes[i].getType(),codes[i].getType());
    %>
    <option <%=(codes[i].getType().equals(code.getType()) ? "SELECTED" : "") %>><%= codes[i].getType()%></option>
    <%
	}
       }
    %>
 	</select>

	     </td>
      </tr>
      <tr >
	<td width="25%">Code:</td><td>

	     <input type="text" name="key" value ="<%=(code.getCode() == null ? "" : code.getCode())%>">
	     </td>
      </tr>
      <tr >
	<td width="25%">Description:</td><td>
	     <textarea name="value" wrap="soft" cols="60" rows="2"><%=(code.getValue() == null ? "" : code.getValue())%></textarea>
	     </td>
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
