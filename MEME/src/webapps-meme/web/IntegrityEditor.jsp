<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.integrity.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="integrity_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="integrity_bean" property="*" />
<title>
IntegrityEditor
</title>
<SCRIPT language="JavaScript">
function submitVector(form, isnew) {
  if(isnew) {
    form.command.value = "Insert";
    form.name.value = form.new_name.value;
  } else {
    form.command.value = "Update";
    form.name.value = form.vector.options[form.vector.selectedIndex].value;
  }
  form.submit();
}
</SCRIPT>
</head>
<body>
<SPAN id="blue"><CENTER>Integrity Editor</CENTER></SPAN>
<HR width="100%">
<CENTER>
<TABLE width="95%" border="0">
  <FORM name="connection" method="GET">
  <INPUT name="state" type="hidden" value="IntegrityEditor">
    <TR>
      <TH colspan="3">&nbsp;</TH>
    </TR>
    <TR>
      <TD colspan="3">&nbsp;</TD>
    </TR>
    <TR>
      <TD width="30%">Change database</TD>
      <TD width="70%">
        <meme:midServiceList name="midService" submit="false" initialValue="<%= integrity_bean.getMidService() %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Change host</TD>
      <TD width="70%">
        <meme:hostList name="host" submit="false" initialValue="<%= integrity_bean.getHost() %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Change port</TD>
      <TD width="70%">
        <INPUT type="text" name="port" size="12"
               value="<jsp:getProperty name="integrity_bean" property="port" />">
        <INPUT type="button" value="Go" onClick="connection.submit();"
  	       OnMouseOver="window.status='Click to change port.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
  </FORM>
</TABLE>
<TABLE width="95%" border="0">
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
    <tr>
      <th colspan="3">Edit Integrity Check, Application Vector or Override Vector</th>

    </tr>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
  <FORM method="GET" action="controller">
  <INPUT name="state" type="hidden" value="EditIntegrityCheck">
  <INPUT name="command" type="hidden" value="">
    <TR>
      <TD width="30%">Edit Check</TD>
      <TD colspan="2" width="70%">
        <SELECT name="check" style="width:30%" onChange='this.form.command.value="";this.form.submit();'>
          <OPTION value="" >SELECT A CHECK</OPTION>
          <% AuxiliaryDataClient aux_client = integrity_bean.getAuxiliaryDataClient();
            IntegrityCheck[] checks = aux_client.getIntegrityChecks();
            Arrays.sort(checks, new Comparator() {
      	      public int compare(Object o1, Object o2) {
      	          return ((IntegrityCheck)o2).getName().compareTo(((IntegrityCheck)o1).getName());
      	        }
      	      });                            
             for (int i=0; i < checks.length; i++) {
          %>
          <OPTION value="<%= checks[i].getName() %>" ><%= checks[i].getName() %></OPTION>
          <% } %>
        </SELECT>
      </TD>
    </TR>
    <TR>
      <TD width="30%">New Check</TD>
      <TD width="18%">
        <input type="text" name="name" style="width:100%">
      </TD>
      <TD width="57%">
        <INPUT type="button" style="width:25" value="Go" onClick='this.form.command.value="Insert";this.form.submit();'
               OnMouseOver="window.status='Click to add new check.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
  </FORM>
  <FORM method="GET" action="controller">
  <INPUT name="state" type="hidden" value="EditVector">
  <INPUT name="type" type="hidden" value="Application">
  <INPUT name="command" type="hidden" value="">
  <INPUT name="name" type="hidden" value="">
    <TR>
      <TD width="20%">Edit Application Vector</TD>
      <TD colspan="2" width="80%">
        <SELECT name="vector" style="width:30%" onChange='submitVector(this.form,false)'>
          <OPTION value="" >SELECT AN APPLICATION</OPTION>
          <% String[] apps = aux_client.getApplicationsWithVectors();
          	Arrays.sort(apps);
             for (int i=0; i < apps.length; i++) {
          %>
          <OPTION value="<%= apps[i] %>" ><%= apps[i] %></OPTION>
          <% } %>
        </SELECT>
      </TD>
    </TR>
    <TR>
      <TD width="30%">New Application Vector</TD>
      <TD width="18%">
        <input type="text" name="new_name" style="width:100%">
      </TD>
      <TD WIDTH="57%">
        <INPUT style="width:25" type="button" value="Go" onClick='submitVector(this.form,true)'
               OnMouseOver="window.status='Click to add new vector.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
  </FORM>
  <FORM method="GET" action="controller">
  <INPUT name="state" type="hidden" value="EditVector">
  <INPUT name="type" type="hidden" value="Override">
  <INPUT name="command" type="hidden" value="">
  <INPUT name="name" type="hidden" value="">
    <TR>
      <TD width="30%">Edit Override Vector</TD>
      <TD colspan="2" width="70%">
        <SELECT name="vector" style="width:30%" onChange='submitVector(this.form,false)'>
          <OPTION value="" >SELECT AN OVERRIDE</OPTION>
          <% int[] override_vectors = aux_client.getLevelsWithOverrideVectors();
          	Arrays.sort(override_vectors);
             for (int i=0; i < override_vectors.length; i++) {
          %>
          <OPTION value="<%= override_vectors[i] %>" ><%= override_vectors[i] %></OPTION>
          <% } %>
        </SELECT>
      </TD>
    </TR>
    <TR>
      <TD width="30%">New Override Vector</TD>
      <TD width="18%">
        <input type="text" name="new_name" style="width:100%">
      </TD>
      <TD width="57%">
        <INPUT type="button" style="width:25" value="Go" onClick='submitVector(this.form,true)'
               OnMouseOver="window.status='Click to add new vector.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
  </FORM>
</TABLE>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html>
