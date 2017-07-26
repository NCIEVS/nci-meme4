<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="gov.nih.nlm.meme.*" %>
<%@ page import="gov.nih.nlm.meme.client.*" %>
<%@ page import="gov.nih.nlm.meme.common.Source" %>
<%@ page errorPage= "EntryPointErrorPage.jsp" %>

<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="sourceinfo_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="sourceinfo_bean" property="*" />
<title>
SourceInfoEditor
</title>
</head>
<body>
<span id="blue"><CENTER>Source Info Editor</CENTER></SPAN>
<hr width="100%">
<center>
<table width="95%" border="0">
  <form name="connection" method="GET">
  <input name="state" type="hidden" value="SourceInfoEditor">
  <INPUT name="current_only" type="hidden" value="<%= (request.getParameter("current_only") == null ? "" : request.getParameter("current_only")) %>">
    <TR>
      <TH colspan="3">&nbsp;</TH>
    </TR>
    <TR>
      <TD colspan="3">&nbsp;</TD>
    </TR>
    <TR>
      <TD width="30%">Change database</TD>
      <TD width="70%">
        <meme:midServiceList name="midService" submit="false" initialValue="<%= sourceinfo_bean.getMidService() %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Change host</TD>
      <TD width="70%">
        <meme:hostList name="host" submit="false" initialValue="<%= sourceinfo_bean.getHost() %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Change port</TD>
      <TD width="70%">
        <INPUT type="text" name="port" size="12"
               value="<jsp:getProperty name="sourceinfo_bean" property="port" />">
        <INPUT type="button" value="Go" onClick="connection.submit();"
  	       OnMouseOver="window.status='Click to change port.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
  </FORM>
</TABLE>
<TABLE width="95%" border="0">
    <tr>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <th>Edit Source Info</th>

    </tr>
    <tr>
      <td>&nbsp;</td>
    </tr>
<tr><td>
    <p>
    This tool is used for editing information about the MRSAB.
    Current versions are shown in <span style="color: #000099">blue</span>.
    <ul>
      <% if("1".equals(request.getParameter("current_only"))) { %>
      <li>See a list of <a href="controller?state=SourceInfoEditor">all sources</a>.</li>
      <% } else { %>
      <li>Restrict to a list of <a href="controller?state=SourceInfoEditor&current_only=1">current sources only</a>.</li>
      <% } %>
      <li>Edit one of the following sources:</li>
    </ul>
    </p>
</td></tr>
  <FORM method="GET" action="controller">
  <INPUT name="state" type="hidden" value="EditSourceInfo">
  <INPUT name="command" type="hidden" value="">
    <TR>
      <TD align="center">
 	<select name="source" size="20"
	     onDblClick='this.form.submit(); return true;'>
          <% AuxiliaryDataClient aux_client = sourceinfo_bean.getAuxiliaryDataClient();
            Source[] sources = aux_client.getSources();
            if("1".equals(request.getParameter("current_only")))
		sources = aux_client.getCurrentSources();
            Arrays.sort(sources, new Comparator() {
    		public int compare(Object o1, Object o2) {
      		return ((Source)o1).getSourceAbbreviation().compareTo(((Source)o2).getSourceAbbreviation());
    		}
            });
             for (int i=0; i < sources.length; i++) {
		StringBuffer source = new StringBuffer(sources[i].getSourceAbbreviation());
                source.append(", ");
		if(sources[i].getOfficialName() != null) {
                  source.append(sources[i].getOfficialName());
		}
		if(source.length() > 90) {
		  source = new StringBuffer(source.toString().substring(0,90) + "...");
		}
          %>
              <font size="-1"><option <%= (sources[i].isCurrent() ? "style=\"color: #000099;\"" : "") %> value="<%= sources[i].getSourceAbbreviation() %>"><%=source.toString()%></option></font>
          <% } %>
        </SELECT>
      </TD>
    </TR>
	<TR> <TD align="center"> <INPUT type="submit" value="Edit"></TD></TR>

  </FORM>
</TABLE>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</body>
</html>
