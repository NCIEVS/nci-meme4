<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="java.util.*, gov.nih.nlm.meme.MEMEToolkit"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<HTML>
<HEAD>
<TITLE>ActionDetails</TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">

<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.ActionDetailsBean" />
<jsp:setProperty name="bean" property="*" />

</HEAD>
<BODY>

<%= bean.getActionReport() %>

<FORM>
  <TABLE WIDTH=90% >
    <TD align="center">
      <input type="button" value="Close" onClick="window.close(); return true">
    </TD>
  </TABLE>

</FORM>

<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</BODY>
</HTML>
