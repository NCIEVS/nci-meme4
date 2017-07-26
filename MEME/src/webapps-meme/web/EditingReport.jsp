<%@ page session="false" import= "java.util.*" errorPage="ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<HTML>
<HEAD>
<TITLE>EditingReport</TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">

<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.EditingReportBean" />
<jsp:setProperty name="bean" property="*" />

</HEAD>
<BODY>
<SPAN id="blue"><CENTER>Editing Report</CENTER></SPAN>
<% String[][] report = bean.getEditingReportData(); %>
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
    </TR>
    <TR >
      <TD>Recursive: <B><%= bean.getRecursive() %></B></TD>
    </TR>
  </TABLE>
</CENTER>
<%
   String wl_string = "";
   String rf_string = "";
   if (bean.getWorklist() != null) {
     wl_string = "worklist ";
     rf_string = bean.getWorklist();
   } else if (bean.getDaysAgo() == 6)
     rf_string = "past week";
   else
     rf_string = "today";
%>
<PRE>
      Editor Authorities and actions for <%= wl_string %><B><%= rf_string %></B> (<%= report.length %> rows found).<BR>
      Click on links for more information.
</PRE>
<TABLE BORDER="2" cellpadding="5" ALIGN="center" WIDTH="90%">
  <TR><TH>Authority</TH>
      <TH>Total # of Actions</TH>
      <TH>Concepts</TH>
      <TH>Splits</TH>
      <TH>Merges</TH>
      <TH>Concept Approvals</TH>
      <TH>Relationship Insert Actions</TH>
      <TH>STY Insert Actions</TH>
  </TR><%
  for (int i=0; i < report.length; i++) { %>
  <TR><%
    for (int j=0; j < 8; j++) {
      StringBuffer href = new StringBuffer(500);
      if (j == 0 || j == 2 || report[i][j].equals("0"))
        href.append(report[i][j]);
      else {
        href.append("<A HREF=\"")
          .append("controller?state=ActionReport&midService=")
          .append(bean.getMidService())
          .append("&host=")
          .append(bean.getHost())
          .append("&port")
          .append(bean.getPort())
          .append("&startDate=")
          .append(bean.getToday(bean.getDaysAgo()))
          .append("&authority=")
          .append(report[i][0]);

        String on_mouse_over_txt = "Click to see ";
        if (j > 2) {
          href.append("&molecularAction=");
          if (j == 3) { on_mouse_over_txt += "split actions";
            href.append("MOLECULAR_SPLIT");
          } else if (j == 4) { on_mouse_over_txt += "merge actions";
           href.append("MOLECULAR_MERGE");
          } else if (j == 5) { on_mouse_over_txt += "concept approvals";
            href.append("MOLECULAR_CONCEPT_APPROVAL");
          } else if (j == 6) { on_mouse_over_txt += "relationship inserts";
            href.append("MOLECULAR_INSERT").append("&coreTable=R");
          } else if (j == 7) { on_mouse_over_txt += "attribute inserts";
            href.append("MOLECULAR_INSERT").append("&coreTable=A");
          }
        } else
          on_mouse_over_txt += "actions";

        on_mouse_over_txt += " for this authority.";
        href.append("\"\n")
          .append("\t\tOnMouseOver=\"window.status=\'").append(on_mouse_over_txt)
          .append("\'; return true;\"\n")
          .append("\t\tOnMouseOut=\"window.status=\'\'; return true;\"\n")
          .append("target=\"_blank\">").append(report[i][j]).append("</A>");
      } %>
    <TD><%= href.toString() %></TD><%
    } %>
  </TR><%
  } %>
</TABLE>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</BODY>
</HTML>
