<%@ page session="false" contentType="text/html; charset=UTF-8" %>
<%@ page errorPage= "EntryPointErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<html>

<!--

 This is the main JSP script used to access the MID action logs.  It takes
 arguments in the standard form "key=value&key=value...".

 Parameters:

  midService    :  Database name
  host          :  Host
  port          :  Port
  startDate     :  Used for date range searches by default and editor_report
  endDate       :  Used for date range searches by default and editor_report
  conceptId     :  Concept id to generate action list for
  transactionId :  Transaction id to generate action list for
  worklist      :  Worklist to generate action list for
  authority     :  Restrict action list to those matching %authority%
  coreTable     :  Restrict action list to those affecting this core table
  action        :  Restrict action list to this action
  recursive     :  Perform recursive search (follow splits/merges, lookup rels)
  daysAgo       :  Used by editor_report as the # of days ago

 Version: 4.0, 11/11/2002 (BAC)

-->

<head>
<title>ActionHarvester</title>
<link href="../stylesheets.css" rel="stylesheet" type="text/css">

<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.ActionHarvesterBean" />
<jsp:setProperty name="bean" property="*" />
<script language="JavaScript" src="../calendar1.js"></script>
<script language="JavaScript" src="../calendar2.js"></script>

<script LANGUAGE="JavaScript">

//
// Validation functions
//

function checkInteger(fld,positive) {
  var str = fld.value;
  if (str.substring(0,1) == "-") {
    if (positive) return false;
    str = str.substring(1);
  };
  if (str == "") return true;
  for (var i = 0; i < str.length; i++) {
    var ch = str.substring(i,i+1);
    if (ch < "0" || ch > "9") return false;
  }
  return true;
}

function checkMidService() {
  if (document.form1.midService.options[document.form1.midService.selectedIndex].value == "") {
    alert ("You must provide a database!");
    return false;
  }
  return true;
}

function runSubmit(form,new_flag) {
  // Possibly check if date range is too much > 2 months?
  if (!checkMidService()) return;
  if (!checkInteger(form.conceptId,true)) {
    alert ("The Concept Id must be a positive integer.");
    form.conceptId.focus();
    return;
  }
  if (!checkInteger(form.rowCount,true)) {
    alert ("The Counter must be an integer.");
    form.rowCount.focus();
    return;
  }
  if (!checkInteger(form.transactionId,true)) {
    alert ("The Transaction Id must be a positive integer.");
    form.transactionId.focus();
    return;
  }

  if (new_flag == true) {
    var mid_service = form.midService.options[form.midService.selectedIndex].value;
    var host = form.host.options[form.host.selectedIndex].value;
    var port = form.port.value;
    var row_count = form.rowCount.value;
    var start_date = form.startDate.value;
    var end_date = form.endDate.value;
    var transaction_id = form.transactionId.value;
    var worklist = form.worklist.value;
    var authority = form.authority.value;
    var concept_id = form.conceptId.value;
    var action= form.molecularAction.options[form.molecularAction.selectedIndex].value;
    var core_table= form.coreTable.options[form.coreTable.selectedIndex].value;
    var recursive = "";
    if (form.recursive.checked) {
      recursive = "on";
    }
    window.open("controller?state=ActionReport"
      + "&midService=" + escape(mid_service)
      + "&host=" + escape(host)
      + "&port=" + escape(port)
      + "&rowCount=" + escape(row_count)
      + "&startDate=" + escape(start_date)
      + "&transactionId=" + escape(transaction_id)
      + "&endDate=" + escape(end_date)
      + "&worklist=" + escape(worklist)
      + "&conceptId=" + escape(concept_id)
      + "&authority=" + escape(authority)
      + "&recursive=" + escape(recursive)
      + "&molecularAction=" + escape(action)
      + "&coreTable=" + escape(core_table));
    return false;

  } else {
    form.submit();
  }
}

function formSubmit(report) {
  if (!checkMidService()) return;

  var mid_service = document.form1.midService.options[document.form1.midService.selectedIndex].value;
  var host = document.form1.host.options[document.form1.host.selectedIndex].value;
  var port = document.form1.port.value;

  if (report == 1) {
    window.open("controller?state=EditingReport&midService="+escape(mid_service)+"&host="+escape(host)+"&port="+escape(port)+"&daysAgo=0");
  } else if (report == 2) {
    window.open("controller?state=EditingReport&midService="+escape(mid_service)+"&host="+escape(host)+"&port="+escape(port)+"&daysAgo=6");
  } else if (report == 3) {
    var start_date = document.form1.startDate.value;
    var end_date = document.form1.endDate.value;
    var worklist = document.form1.worklist.value;
    window.open("controller?state=EditingReport&midService="+escape(mid_service)+"&host="+escape(host)+"&port="+escape(port)
      +"&worklist="+escape(worklist)+"&startDate="+escape(start_date)+"&endDate="+escape(end_date));
  } else if (report == 4) {
    window.open("controller?state=ActionReport&midService="+escape(mid_service)+"&host="+escape(host)+"&port="+escape(port)
      +"&startDate=<%= bean.getToday() %>");
  } else if (report == 5) {
    window.open("controller?state=ActionReport&midService=" + escape(mid_service)+"&host="+escape(host)+"&port="+escape(port)
      +"&startDate=<%= bean.getToday(6) %>&endDate=<%= bean.getEndDate() %>");
  }
}

function lookupWorklist() {
  var mid_service = document.form1.midService.options[document.form1.midService.selectedIndex].value;
  var host = document.form1.host.options[document.form1.host.selectedIndex].value;
  var port = document.form1.port.value;
  lookupWindow = window.open("controller?state=WorklistLookup&midService=" + escape(mid_service)+"&host="+escape(host)+"&port="+escape(port),
    "lookup", "scrollbars, width=750, height=450, resizable");
  return true;
}

function setWorklist(worklist_name,start_date,end_date) {
 document.form1.worklist.value=worklist_name;
 document.form1.startDate.value=start_date;
 document.form1.endDate.value=end_date;
}

</script>
</head>
<body bgcolor="#ffffff">

<span id="blue"><center>Action Harvester</center></span>
<hr width="100%">

<!-- action harvester form -->
<form name="form1" method="GET" action="controller">
  <input name="state" type="hidden" value="ActionHarvester">
  <center>
  <table WIDTH="90%" BORDER="0" >
    <tr >
      <td>Database:</td>
      <td>
        <meme:midServiceList name="midService" submit="false" initialValue="<%= bean.getMidService() %>" />
      </td>
      <td>Counter:</td>
      <td><input type="text" name="rowCount" size="30"
                 value="<jsp:getProperty name="bean" property="rowCount" />">
      </td>
    </tr>
    <tr >
      <td>Host</td>
      <td>
        <meme:hostList name="host" submit="false" initialValue="<%= bean.getHost() %>" />
      </td>
      <td>Transaction Id:</td>
      <td><input type="text" name="transactionId" size="30"
                 value="<jsp:getProperty name="bean" property="transactionId" />">
      </td>
    </tr>
    <tr >
      <td>Port:</td>
      <td>
        <input type="text" name="port" size="12"
               value="<jsp:getProperty name="bean" property="port" />">
      </td>
      <td><input accesskey="w" type="button" value="Worklist:" onClick="lookupWorklist(); return true;"
	         OnMouseOver="window.status='Click to see a list of available worklists.'; return true;"
                 OnMouseOut="window.status=''; return true;"></td>
      <td><input type="text" name="worklist" size="30"
                 value="<jsp:getProperty name="bean" property="worklist" />">
      </td>
    </tr>
    <tr >
      <td>Start Date:</td>
      <td><input type="text" onfocus="blur()" name="startDate" size="30"
                 value="<jsp:getProperty name="bean" property="startDate" />">
           <a href="javascript:cal1.popup();"><img src="../img/cal.gif" alt="Click for calendar"></a>
      </td>
      <td>Authority:</td>
      <td><input type="text" name="authority" size="30"
                 value="<jsp:getProperty name="bean" property="authority" />">
      </td>
    </tr>
    <tr >
      <td>End Date:</td>
      <td><input type="text" onfocus="blur()" name="endDate" size="30"
                 value="<jsp:getProperty name="bean" property="endDate" />">
           <a href="javascript:cal2.popup();"><img src="../img/cal.gif" alt="Click for calendar"></a>
      </td>
      <td>Action:</td>
      <td><select name ="molecularAction">
          <option value="" >ALL</option>
          <option value="MOLECULAR_APPROVE_CONCEPT" <%= bean.getMolecularAction().equals("MOLECULAR_APPROVE_CONCEPT") ? "selectED" : "" %>>Concept Approval</option>
          <option value="MOLECULAR_SPLIT" <%= bean.getMolecularAction().equals("MOLECULAR_SPLIT") ? "selectED" : "" %>>Split</option>
          <option value="MOLECULAR_MOVE" <%= bean.getMolecularAction().equals("MOLECULAR_MOVE") ? "selectED" : "" %>>Move</option>
          <option value="MOLECULAR_MERGE" <%= bean.getMolecularAction().equals("MOLECULAR_MERGE") ? "selectED" : "" %>>Merge</option>
          <option value="MOLECULAR_CHANGE_TOBERELEASED" <%= bean.getMolecularAction().equals("MOLECULAR_CHANGE_TOBERELEASED") ? "selectED" : "" %>>Change Tobereleased</option>
          <option value="MOLECULAR_CHANGE_STATUS" <%= bean.getMolecularAction().equals("MOLECULAR_CHANGE_STATUS") ? "selectED" : "" %>>Change Status</option>
          <option value="MOLECULAR_CHANGE_FIELD" <%= bean.getMolecularAction().equals("MOLECULAR_CHANGE_FIELD") ? "selectED" : "" %>>Change Field</option>
          <option value="MOLECULAR_INSERT" <%= bean.getMolecularAction().equals("MOLECULAR_INSERT") ? "selectED" : "" %>>Insert</option>
          <option value="MOLECULAR_DELETE" <%= bean.getMolecularAction().equals("MOLECULAR_DELETE") ? "selectED" : "" %>>Delete</option>
          </select>
      </td>
    </tr>
    <tr >
      <td>Concept id:</td>
      <td><input type="text" name="conceptId" size="30"
                 value="<jsp:getProperty name="bean" property="conceptId" />">
      </td>
      <td>Core Table:</td>
      <td><select name="coreTable">
          <option value="">ALL</option>
          <option value="Concept" <%= bean.getCoreTable().equals("Concept") ? "selectED" : "" %>>Concept Status</option>
          <option value="Atom" <%= bean.getCoreTable().equals("Atom") ? "selectED" : "" %>>Classes</option>
          <option value="Relationship" <%= bean.getCoreTable().equals("Relationship") ? "selectED" : "" %>>Relationships</option>
          <option value="Attribute" <%= bean.getCoreTable().equals("Attribute") ? "selectED" : "" %>>Attributes</option>
          </select>
      </td>
    </tr>
    <tr >
      <td>Recursive:</td>
      <td><input type="checkbox" name="recursive" <%= bean.isRecursiveChecked() %>>
      </td>
    </tr>
    <SCRIPT language="JavaScript">
    Str_ICONPATH = "<%= request.getContextPath() %>/img/";
    var cal1 = new calendar2(document.forms['form1'].elements['startDate']);
    cal1.base = "<%= request.getContextPath() %>/";
    var cal2 = new calendar2(document.forms['form1'].elements['endDate']);
    cal2.base = "<%= request.getContextPath() %>/";
    </SCRIPT>
  </table>

  <br><br>

  <table WIDTH=90% >
    <td align="center"><input type="submit" accesskey="n" class="snazzy"
        value="Retrieve Actions" onClick="runSubmit(this.form, true); return true;">
    </td>
  </table>

  </center>
</form>
<p>
Canned Queries:
  <ul>
    <li><A hrEF="javascript:void formSubmit(1)"
                OnMouseOver="window.status='Generate Editors Report for today.'; return true;"
                OnMouseOut="window.status=''; return true;">Report of Actions by Editor for today.</A> (takes 2 minutes)
    <li><A hrEF="javascript:void formSubmit(2)"
                OnMouseOver="window.status='Generate Report of Actions (by editor) for past 7 days.'; return true;"
                OnMouseOut="window.status=''; return true;">Report of Actions by Editor for the past week.</A> (takes 2-5 minutes)
    <li><A hrEF="javascript:void formSubmit(3)"
                OnMouseOver="window.status='Generate Report of Actions (by editor) for the worklist '+document.form1.worklist.value; return true;"
                OnMouseOut="window.status=''; return true;">Report of Actions by Editor for specified worklist.</A> (~30 seconds.).
    <li><A hrEF="javascript:void formSubmit(4)"
                OnMouseOver="window.status='Generate List of Todays Actions.'; return true;"
                OnMouseOut="window.status=''; return true;">Editor Actions Today.</A>
    <li><A hrEF="javascript:void formSubmit(5)"
                OnMouseOver="window.status='Generate a List of Actions for past 7 days.'; return true;"
                OnMouseOut="window.status=''; return true;">Editor Actions for the past week.</A>
  </ul>
</p>

<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />

</body>
</html>
