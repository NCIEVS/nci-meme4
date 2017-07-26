<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="gov.nih.nlm.meme.MEMEToolkit, java.util.Date "
         errorPage="ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>

<HTML>
<HEAD>
<TITLE>ServerStatistics</TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.AdminClientBean" />
<jsp:setProperty name="bean" property="*" />
</HEAD>
<BODY>

<SPAN id="blue"><CENTER>Shutdown Server</CENTER></SPAN>
<HR width="100%">
<i>Following are the final server statistics for the server on host
<%= bean.getHost() %> and port <%= bean.getPort() %>.
</i>
<blockquote>
<pre>
<%= bean.getAdminClient().getStatisticsReport() %>
<% bean.getAdminClient().shutdownServer(); %>
<b>Server successfully shutdown.</b>
</pre>
</blockquote>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</BODY>
</HTML>
