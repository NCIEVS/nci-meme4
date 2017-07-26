<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="gov.nih.nlm.meme.exception.*, java.util.*, java.io.*"
         isErrorPage="true" %>
<html>

<head>
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>ErrorPage</title>
</head>

<body>
<span id="red"><center>Error Page</center></span>
<pre><br>
<%
  MEMEException me = new MEMEException("Non MEME Exception");

  if (exception instanceof Exception)
    me.setEnclosedException((Exception)exception);
  else {
    exception.printStackTrace(new PrintWriter(out));
  }
   StringTokenizer st = new StringTokenizer(me.toString(), "\n");
   while (st.hasMoreTokens()) {
%>
     <font color="red"><%= st.nextToken() %></font>
<%
   }
%>
  <br>
<%
  me.printStackTrace(new PrintWriter(out));
%>
<br></pre>

<form>
  <table width="90%">
    <td align="center">
      <input type="button" value="Close" onClick="window.close(); return true">
    </td>
  </table>
</form>

</body>
</html>
