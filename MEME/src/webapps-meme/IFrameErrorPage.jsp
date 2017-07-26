<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="gov.nih.nlm.meme.exception.*, java.util.*, java.io.*, java.net.*"
         isErrorPage="true" %>
<html>

<head>
<title>ErrorPage</title>
</head>
  <body onLoad="parent.handleError(
     <%
    // Collect the stack trace(s) into a string.
    // The parent Frame must have a method to handle error.
    StringWriter stack_trace = new StringWriter();
    exception.printStackTrace(new PrintWriter(stack_trace));
    out.print("'" + (exception.getMessage() == null ? "" : URLEncoder.encode(exception.getMessage(),"UTF-8") )+ "'");
    out.print(",'" + URLEncoder.encode(stack_trace.toString(),"UTF-8")+ "'");
    %>
    ,self.name)">
</body>
</html>