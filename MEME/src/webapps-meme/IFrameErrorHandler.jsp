<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="gov.nih.nlm.meme.exception.*, java.util.*, java.io.*, java.net.URLDecoder"
         isErrorPage="true" %>
<html>

<head>
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>ErrorPage</title>
</head>

<body>
<span id="red"><center>Error Page</center></span>
<pre><br>
  <%= URLDecoder.decode(request.getParameter("message"),"UTF-8") %>
  <%= URLDecoder.decode(request.getParameter("trace"),"UTF-8") %>
<br></pre>

<form>
  <table width="90%">
    <td align="center">
      <input type="button" value="Close" onClick="window.opener.errorhandled(' <%= request.getParameter("iframe") %> ', window.name); window.close(); return true">
    </td>
  </table>
</form>

</body>
</html>
