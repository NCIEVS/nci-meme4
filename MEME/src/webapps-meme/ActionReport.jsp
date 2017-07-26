<%@ page session="false"
         errorPage="ErrorPage.jsp" %>
<%@ page import="gov.nih.nlm.meme.action.AtomicAction" %>
<%@ page import="gov.nih.nlm.meme.action.MolecularAction" %>
<%@ page import="gov.nih.nlm.meme.common.*" %>
<%@ page import="gov.nih.nlm.meme.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<HTML>
<HEAD>
<TITLE>ActionReport</TITLE>
<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.ActionReportBean" />
<jsp:setProperty name="bean" property="*" />
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">

</HEAD>
<BODY>
<SPAN id="blue"><CENTER>Action Report</CENTER></SPAN>
<HR width="100%">
<CENTER>
  <TABLE WIDTH="90%" BORDER="0" >
    <TR >
      <TD>Database: <B><%= bean.getMidService() %></B></TD>
      <TD>Host: <B><%= bean.getHost() %></B></TD>
      <TD>Port: <B><%= bean.getPort() %></B></TD>
      <TD>Start Date: <B><%= bean.getStartDate() %></B></TD>
    </TR>
    <TR >
      <TD>Counter: <B><%= bean.getRowCount() %></B></TD>
      <TD>Transaction Id: <B><%= bean.getTransactionId() %></B></TD>
      <TD>Concept id: <B><%= bean.getConceptId() %></B></TD>
      <TD>End Date: <B><%= bean.getEndDate() %></B></TD>
    </TR>
    <TR >
      <TD>Authority: <B><%= bean.getAuthority() %></B></TD>
      <TD>Worklist: <B><%= bean.getWorklist() %></B></TD>
      <TD>Action: <B><%= bean.getMolecularAction() %></B></TD>
      <TD>Core Table: <B><%= bean.getCoreTable() %></B></TD>
    </TR>
    <TR >
      <TD>Recursive: <B><%= bean.getRecursive() %></B></TD>
    </TR>
  </TABLE>
</CENTER>
<PRE><% String worklist = bean.getWorklist();
   if (worklist != null) { %>
      Actions on concepts in worklist: <B><%= worklist %></B><BR>
   <% } MolecularAction[] ma = bean.getActions(); %>
      <%= ma.length %> action(s) were found.<BR>
</PRE>
<% if (ma.length > 0) { %>
<TABLE BORDER="2" cellpadding="5" ALIGN="center" WIDTH="90%">
  <TR><TH>Authority</TH>
      <TH>Molecular Action</TH>
      <TH>Source id</TH>
      <TH>Target id</TH>
      <TH>Timestamp</TH>
  </TR><% // Map concept id hyperlink reference
     String b_href = MEMEToolkit.getProperty("meme.app.reports.mid.url") +
       "?action=searchbyconceptid&db=";
     String m_db = bean.getMidService();
     String u_db = MIDServices.getService("unedited-db");
     String m_href = null;
     String u_href = null;
     String e_href = null;
     String om_over = "window.status='See concept report.'; return true;";
     String om_out = "window.status=''; return true;";

     for (int i=0; i < ma.length; i++) {
       // trying to append affected tables in action ...
       // ... not working. FinderService clears the sub action
       AtomicAction[] actions = ma[i].getAtomicActions();
       StringBuffer affected_table = new StringBuffer(500);
       if (actions.length > 0) {
         affected_table.append("(");
         for (int j=0; j < actions.length; j++) {
           affected_table.append(actions[i].getAffectedTable());
         }
         affected_table.append(")");
       } %>
  <TR>
    <TD><%= ma[i].getAuthority() %></TD>
    <TD><A HREF="controller?state=ActionDetails&moleculeId=<%= ma[i].getIdentifier().toString() %>&midService=<%= bean.getMidService() %>&host=<%= bean.getHost() %>&port=<%= bean.getPort() %>"
                OnMouseOver="window.status='See action report.'; return true;"
                OnMouseOut="window.status=''; return true;"
                target="_blank"><%= ma[i].getActionName().substring(10) %><%= affected_table.toString() %></A></TD>
    <% e_href = "&arg=" + ma[i].getSourceIdentifier().toString();
       if (ma[i].getSourceIdentifier().intValue() > 0) {
         m_href = b_href + m_db + e_href;
         u_href = b_href + u_db + e_href;
    %><TD><A HREF="<%= m_href %>"
                OnMouseOver="<%= om_over %>"
                OnMouseOut="<%= om_out %>"
                target="_blank"><%= ma[i].getSourceIdentifier().toString() %></A>/
        <A HREF="<%= u_href %>"
                OnMouseOver="<%= om_over %>"
                OnMouseOut="<%= om_out %>"
                target="_blank"><%= ma[i].getSourceIdentifier().toString() %></A></TD>
    <% } else {
    %><TD><%= ma[i].getSourceIdentifier().toString() %></TD>
    <% }
       e_href = "&arg=" +  ma[i].getTargetIdentifier().toString();
       if (ma[i].getTargetIdentifier().intValue() > 0) {
         m_href = b_href + m_db + e_href;
         u_href = b_href + u_db + e_href;
    %><TD><A HREF="<%= m_href %>"
                OnMouseOver="<%= om_over %>"
                OnMouseOut="<%= om_out %>"
                target="_blank"><%= ma[i].getTargetIdentifier().toString() %></A>/
        <A HREF="<%= u_href %>"
                OnMouseOver="<%= om_over %>"
                OnMouseOut="<%= om_out %>"
                target="_blank"><%= ma[i].getTargetIdentifier().toString() %></A></TD>
    <% } else {
    %><TD><%= ma[i].getTargetIdentifier().toString() %></TD>
    <% }
    %><TD ALIGN="center"><%= MEMEToolkit.getDateFormat().format(ma[i].getTimestamp()) %></TD>
  </TR><% } %>
</TABLE>
<% } %>

<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />

</BODY>
</HTML>
