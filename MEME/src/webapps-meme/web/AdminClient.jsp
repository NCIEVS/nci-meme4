<%@ page session="false" contentType="text/html; charset=UTF-8" %>
<%@ page errorPage= "EntryPointErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head><title>AdminClient</title>
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.AdminClientBean" />
<jsp:setProperty name="bean" property="*" />
<%
  //
  // Perform action after parameters have been set
  //
  bean.performAction();
%>
<script language="JavaScript">
function getParameterURL() {
  return "host="
    + document.connection.host[document.connection.host.selectedIndex].value
    + "&port=" + document.connection.port.value + "&midService="
    + document.connection.midService[document.connection.midService.selectedIndex].value;
}

function viewServerStatistics() {
  window.open("controller?state=ServerStatistics&"+getParameterURL());
}

function viewServerLog(form) {
  window.open("controller?state=ServerLog&"
    + getParameterURL()
    + "&headOrTail="
    + form.headOrTail[form.headOrTail.selectedIndex].value);
}

function shutdownServer() {
  window.open("controller?state=ShutdownServer&" + getParameterURL());
}

function viewEditingGraph(f, form) {
  var by_param = form.by_param[form.by_param.selectedIndex].value;
  var for_param = form.for_param[form.for_param.selectedIndex].value;

  if (by_param == "Hour" && for_param != "0") {
    alert ("The view graph by Hour must be for today.");
    return false;
  }
  if (by_param == "Day" && (for_param == "0" || for_param == "-1")) {
    alert ("The view graph by day must be for past week, past 2 weeks or past month.");
    return false;
  }
  if (by_param == "Month" && (for_param != "-1")) {
    alert ("The view graph by month must be for db since it was created.");
    return false;
  }

  var url = "controller?state=EditingGraph&"
    + getParameterURL()
    + "&function=" + f
    + "&by_param=" + by_param
    + "&for_param=" + for_param;
    for (i = 0; i < form.of_param.length; i++) {
      if (form.of_param[i].selected) {
        url = url + "&of_param=" + form.of_param[i].value;
      }
    }
  window.open(url, "", "height=500,width=500");
}

function viewActionSequence(form) {
  var sequence = form.sequence[form.sequence.selectedIndex].value;
  var users = form.users[form.users.selectedIndex].value;

  var url = "controller?state=ActionSequence&"
    + getParameterURL()
    + "&sequence=" + sequence
    + "&users=" + users;
  window.open(url);
}

</script>

</head>
<body>

<span id="blue"><center>MEME Server Administration</center></span>
<hr width="100%">
Status: <b><%= bean.getActionStatus() %></b>
<!-- admin client form -->
<p>
Server Host: <tt><%= bean.getHost() %></tt><br>
Server Port: <tt><%= bean.getPort() %></tt><br>
Server Version: <tt><%= bean.getAdminClient().getServerVersion() %></tt>
<P>
<center>
<table width="95%" border="0">
  <form name="connection" method="GET">
  <input name="state" type="hidden" value="AdminClient">
    <tr>
      <th colspan="3">&nbsp;</th>
    </tr>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
    <tr>
      <td width="50%">Change database</td>
      <td width="25%">
        <meme:midServiceList name="midService" submit="false" initialValue="<%= bean.getMidService() %>" />
      </td>
      <td width="25%">
        <input type="button" value="Go" onClick="connection.submit();"
               OnMouseOver="window.status='Click to change database.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
    <tr>
      <td width="50%">Change host</td>
      <td width="25%">
        <meme:hostList name="host" submit="false" initialValue="<%= bean.getHost() %>" />
      </td>
      <td width="25%">
        <input type="button" value="Go" onClick="connection.submit();"
               OnMouseOver="window.status='Click to change host.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
    <tr>
      <td width="50%">Change port</td>
      <td width="25%">
        <input type="text" name="port" size="12"
               value="<jsp:getProperty name="bean" property="port" />">
      </td>
      <td width="25%">
        <input type="button" value="Go" onClick="connection.submit();"
  	       OnMouseOver="window.status='Click to change port.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  </form>
    <tr>
      <% boolean is_editing_enabled = bean.getAdminClient().isEditingEnabled(); %>
      <td width="50%">Editing is <b><%= is_editing_enabled ? "" : "not" %> enabled</b>.</td>
      <td width="25%">&nbsp;</td>
      <td width="25%">
      <% if (is_editing_enabled) { %>
        <input type="button" value="Disable"
               onClick="window.location.href='controller?state=AdminClient&action=disable_editing&'+ getParameterURL(); return true;"
	       OnMouseOver="window.status='Click to disable editing.'; return true;"
               OnMouseOut="window.status=''; return true;">
      <% } else { %>
        <input type="button" value="Enable"
               onClick="window.location.href='controller?state=AdminClient&action=enable_editing&' + getParameterURL(); return true;"
	       OnMouseOver="window.status='Click to enable editing.'; return true;"
               OnMouseOut="window.status=''; return true;">
      <% } %>
      </td>
    </tr>
    <tr>
      <% boolean is_integrity_enabled = bean.getAdminClient().isIntegritySystemEnabled(); %>
      <td width="50%">Integrity system is <b><%= is_integrity_enabled ? "" : "not" %> enabled</b>.</td>
      <td width="25%">&nbsp;</td>
      <td width="25%">
      <% if (is_integrity_enabled) { %>
        <input type="button" value="Disable"
               onClick="window.location.href='controller?state=AdminClient&action=disable_integrity&' + getParameterURL(); return true;"
	       OnMouseOver="window.status='Click to disable integrity system.'; return true;"
               OnMouseOut="window.status=''; return true;">
      <% } else { %>
        <input type="button" value="Enable"
               onClick="window.location.href='controller?state=AdminClient&action=enable_integrity&' + getParameterURL(); return true;"
	       OnMouseOver="window.status='Click to enable integrity system.'; return true;"
               OnMouseOut="window.status=''; return true;">
      <% } %>
      </td>
    </tr>
    <tr>
      <% boolean is_atomic_action_validation_enabled = bean.getAdminClient().isAtomicActionValidationEnabled(); %>
      <td width="50%">Atomic action validation is <b><%= is_atomic_action_validation_enabled ? "" : "not" %> enabled</b>.</td>
      <td width="25%">&nbsp;</td>
      <td width="25%">
      <% if (is_atomic_action_validation_enabled) { %>
        <input type="button" value="Disable"
               onClick="window.location.href='controller?state=AdminClient&action=disable_validate_atomic_action&' + getParameterURL(); return true;"
	       OnMouseOver="window.status='Click to disable atomic action validation.'; return true;"
               OnMouseOut="window.status=''; return true;">
      <% } else { %>
        <input type="button" value="Enable"
               onClick="window.location.href='controller?state=AdminClient&action=enable_validate_atomic_action&' + getParameterURL(); return true;"
	       OnMouseOver="window.status='Click to enable atomic action validation.'; return true;"
               OnMouseOut="window.status=''; return true;">
      <% } %>
      </td>
    </tr>
    <tr>
      <% boolean is_molecular_action_validation_enabled = bean.getAdminClient().isMolecularActionValidationEnabled(); %>
      <td width="50%">Molecular action validation is <b><%= is_molecular_action_validation_enabled ? "" : "not" %> enabled</b>.</td>
      <td width="25%">&nbsp;</td>
      <td width="25%">
      <% if (is_molecular_action_validation_enabled) { %>
        <input type="button" value="Disable"
               onClick="window.location.href='controller?state=AdminClient&action=disable_validate_molecular_action&' + getParameterURL(); return true;"
	       OnMouseOver="window.status='Click to disable molecular action validation.'; return true;"
               OnMouseOut="window.status=''; return true;">
      <% } else { %>
        <input type="button" value="Enable"
                 onClick="window.location.href='controller?state=AdminClient&action=enable_validate_molecular_action&' + getParameterURL(); return true;"
	         OnMouseOver="window.status='Click to enable molecular action validation.'; return true;"
                 OnMouseOut="window.status=''; return true;">
      <% } %>
      </td>
    </tr>
    <tr>
      <td width="50%">Refresh caches</td>
      <td width="25%">&nbsp;</td>
      <td width="25%">
        <input type="button" value="Refresh"
               onClick="window.location.href='controller?state=AdminClient&action=refresh_caches&' + getParameterURL(); return true;"
	       OnMouseOver="window.status='Click to refresh caches.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
    <tr>
      <td width="50%">View server statistics</td>
      <td width="25%">&nbsp;</td>
      <td width="25%">
        <input type="button" value="View" onClick="viewServerStatistics(); return true;"
	       OnMouseOver="window.status='Click to view server statistics.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  <form name="server_log">
    <tr>
      <td width="50%">View server log</td>
      <td width="25%">
      <select name="headOrTail">
        <option value="1000">1000</option>
          <option value="100">100</option>
          <option value="10">10</option>
          <option value="0">0</option>
          <option value="-10">-10</option>
          <option value="-100">-100</option>
          <option value="-1000">-1000</option>
        </select>
      </td>
      <td width="25%">
        <input type="button" value="View" onClick="viewServerLog(this.form); return true;"
               OnMouseOver="window.status='Click to view server log.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  </form>
    <tr>
      <td width="50%">Shutdown server</td>
      <td width="25%">&nbsp;</td>
      <td width="25%">
        <input type="button" value="Shutdown" onClick="shutdownServer(); return true;"
	       OnMouseOver="window.status='Click to shutdown server.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
    <tr>
      <th colspan="3">Graphs</th>
    </tr>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
  <form name="chart_for_count">
    <tr>
      <td colspan="2" valign="TOP">View graph of
          <select name="of_param" size="3" align="TOP" multiple="trUE">
            <option value="Actions">Actions</option>
            <option value="Interface">Interface</option>
            <option value="Stamping">Stamping</option>
            <option value="Approval">Approval</option>
            <option value="Merge">Merge</option>
            <option value="Split">Split</option>
            <option value="Insert">Insert</option>
            <option value="Move">Move</option>
          </select> by
          <select name="by_param" align="TOP">
            <option value="Hour">Hour</option>
            <option value="Day">Day</option>
            <option value="Month">Month</option>
            <option value="Editor">Editor</option>
            <option value="Action">Action</option>
            <option value="Source">Source</option>
          </select> for
          <select name="for_param" align="TOP">
            <option value="0">Today</option>
            <option value="7">Past week</option>
            <option value="14">Past 2 weeks</option>
            <option value="30">Past month</option>
            <option value="-1">All time</option>
          </select>
      </td>
      <td valign="TOP">
        <input type="button" value="Graph" onClick="viewEditingGraph('chart_for_count', this.form); return true;"
               OnMouseOver="window.status='Click to view editing graph chart for count.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  </form>
  <form name="chart_for_ratio">
    <tr>
      <td colspan="2">View graph of
          <select name="of_param" size="3" align="TOP" multiple="trUE">
            <option value="Interface">Interface Actions/Approval</option>
            <option value="All">All Actions/Approval</option>
          </select> by
          <select name="by_param" align="TOP">
            <option value="Hour">Hour</option>
            <option value="Day">Day</option>
            <option value="Month">Month</option>
            <option value="Editor">Editor</option>
          </select> for
          <select name="for_param" align="TOP">
            <option value="0">Today</option>
            <option value="7">Past week</option>
            <option value="14">Past 2 weeks</option>
            <option value="30">Past month</option>
            <option value="-1">All time</option>
          </select>
      </td>
      <td valign="TOP">
        <input type="button" value="Graph" onClick="viewEditingGraph('chart_for_ratio', this.form); return true;"
               OnMouseOver="window.status='Click to view editing graph chart for ratio.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  </form>
  <form name="chart_for_time">
    <tr>
      <td colspan="2">View graph of
          <select name="of_param" size="3" align="TOP" multiple="trUE">
            <option value="MIN">Min elapsed time</option>
            <option value="MAX">Max elapsed time</option>
            <option value="AVG">Avg elapsed time</option>
          </select> by
          <select name="by_param" align="TOP">
            <option value="Hour">Hour</option>
            <option value="Day">Day</option>
            <option value="Month">Month</option>
            <option value="Editor">Editor</option>
            <option value="Action">Action</option>
            <option value="Source">Source</option>
          </select> for
          <select name="for_param" align="TOP">
            <option value="0">Today</option>
            <option value="7">Past week</option>
            <option value="14">Past 2 weeks</option>
            <option value="30">Past month</option>
            <option value="-1">All time</option>
          </select>
      </td>
      <td valign="TOP">
        <input type="button" value="Graph" onClick="viewEditingGraph('chart_for_time', this.form); return true;"
               OnMouseOver="window.status='Click to view editing graph chart for time.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  </form>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
    <tr>
      <th colspan="3">Action Sequence</th>
    </tr>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
  <form name="action_sequence">
    <tr>
      <td colspan="2" valign="TOP">View action sequence
          <select name="sequence">
            <option value="1">1</option>
            <option value="2">2</option>
          </select> simulating
          <select name="users">
            <option value="1">1</option>
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
          </select> users
      </td>
      <td valign="TOP">
        <input type="button" value="Go" onClick="viewActionSequence(this.form); return true;"
               OnMouseOver="window.status='Click to view action sequence.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  </form>
</table>
</center>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html>
