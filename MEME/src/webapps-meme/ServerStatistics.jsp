<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="gov.nih.nlm.meme.MEMEToolkit, java.util.Date "
         errorPage="ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<HTML>
<HEAD>
<TITLE>ServerStatistics</TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">

<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.AdminClientBean" />
<jsp:setProperty name="bean" property="*" />
</HEAD>
<BODY>

<SPAN id="blue"><CENTER>MEME Server Statistics</CENTER></SPAN>
<HR width="100%">
<i>Following are server statistics for the server running on host
<%= bean.getHost() %> and port <%= bean.getPort() %>.
</i>
<blockquote>
<pre>
<%= bean.getAdminClient().getStatisticsReport() %>
</pre>
</blockquote>

<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />

</BODY>
</HTML>
