<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<jsp:useBean id="calendar" class="java.util.GregorianCalendar" scope="session" />

<html:html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><bean:write name="worklog" property="description"/> (<bean:write name="worklog" property="identifier"/>)</title>

  <script language="javascript" type="">
      function ack(url) {
   ok = confirm("This may involve a lot of actions\r\n"+
                "Please click OK if you want to proceed.");
   if (ok) {
      location.href=url;
   }
}
function checkEmpty(tableCounts) {
  if(tableCounts.value.length == 0 ||
  tableCounts.value == null) {
  alert("Please enter Table Counts");
  return false;
  }
  launchCenter('', 'GenerateInsertionReport', 200,400);
  return true;
}
function launchCenter(url, name, height, width)
{
	var str = "height=" + height + ",innerHeight=" + height;
	str += ",width=" + width + ",innerWidth=" + width;
	if (window.screen)
	{
		var ah = screen.availHeight - 30;
		var aw = screen.availWidth - 10;

		var xc = (aw - width) / 2;
		var yc = (ah - height) / 2;

		str += ",left=" + xc + ",screenX=" + xc;
		str += ",top=" + yc + ",screenY=" + yc;
	}
	str += ", menubar=no, scrollbars=yes, status=0, location=0, directories=0, resizable=1";
	window.open(url, name, str);
}
  </script>


<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">


</HEAD>

<BODY bgcolor="#ffffff">
<h2><center><bean:write name="worklog" property="description"/> (<bean:write name="worklog" property="identifier"/>)</center></h2>
<HR WIDTH=100%>

<table border="0">
<tr><td><B>Work</b></td><td><bean:write name="worklog" property="description"/></td></tr>
<tr><td><B>Authority</b></td><td><bean:write name="worklog" property="authority"/></td></tr>
<tr><td><B>Database</b></td><td><bean:write name="insertionReportBean" property="midService"/></td></tr>
</table>
<logic:present parameter="view">
<logic:equal value="html" parameter="view">
  <bean:parameter id="tableCounts" name="tableCounts"/>
  <h3>Report Table Counts</h3>
  <pre><bean:write name="tableCounts"/>
  </pre>
</logic:equal>
</logic:present>
<logic:notPresent parameter="view">
<html:form action="/generateInsertionReport.do" onsubmit="return checkEmpty(this.tableCounts);" target="GenerateInsertionReport">
  <table>
    <tr><td>
<html:submit property="type">
  Generate Test Insertion Report
</html:submit>
<html:submit property="type">
  Generate Insertion Report
</html:submit>
</td>
    </tr>
  </table>
<h3>Report Table Counts</h3>
<html:textarea property="tableCounts" rows="10" cols="60" >
</html:textarea>
</html:form>
<html:form action="/Back.do">
<html:button onclick="onclick=launchCenter('addInsertionProblemform.do', 'AddInsertionProblem', 430,400);return false;" property="">
  Add Problem
</html:button>
</html:form>
</logic:notPresent>

<logic:present name="problems">
<br>
  <bean:define id="identifier" name="worklog" property="identifier"/>
<logic:iterate id="problem" name="problems" >
  <logic:equal value="<%= identifier.toString() %>" name="problem" property="identifier">
    <logic:notPresent name="printProblems">
    <h3>Problems</h3>
    </logic:notPresent>
    <bean:define id="printProblems" value="" />
  <html:form action="/deleteInsertionProblem.do">
  <table>
    <tr>
      <td valign="top" width="100px"><span style="font-weight: bold;">Problem:</span></td>
      <td valign="top"><bean:write name="problem" property="problem"/></td>
      <html:hidden property="problem" name="problem"/>
      <td> <html:submit>Delete</html:submit></td>
    </tr>
    <tr>
      <td valign="top" width="100px"><span style="font-weight: bold;">Date:</span></td>
      <td valign="top"><bean:write name="problem" property="date"/></td>
    </tr>
    <tr>
      <td valign="top" width="100px"><span style="font-weight: bold;">Authority:</span></td>
      <td valign="top"> <a href="mailto:<bean:write name="problem" property="authority" />"><bean:write name="problem" property="authority" /></a></td>
    </tr>
    <tr>
      <td valign="top" width="100px"><span style="font-weight: bold;">Recurrence:</span></td>
      <td valign="top"><bean:write name="problem" property="recurrence"/></td>
    </tr>
    <tr>
      <td valign="top" width="100px"><span style="font-weight: bold;">Origin:</span></td>
      <td valign="top"><bean:write name="problem" property="origin"/></td>
    </tr>
    <tr>
      <td valign="top" width="100px"><span style="font-weight: bold;">Solution:</span></td>
      <td valign="top"><bean:write name="problem" property="solution"/></td>
    </tr>
    <tr>
      <td valign="top" width="100px"><span style="font-weight: bold;">Feedback:</span></td>
      <td valign="top"><bean:write name="problem" property="feedback"/></td>
    </tr>
  </table>
  </html:form>
<br>
  </logic:equal>
</logic:iterate>
</logic:present>

<br>
      <center><table cellpadding=2 width="90%" >
<tr>
  <th width="15%">Transaction ID</th>
  <th width="20%">Timestamp</th>
  <th width="15%">Elapsed Time</th>
  <th width="50%">Activity</th>
  <logic:iterate id="activity" name="activityList">
  <tr>
    <td valign="top" align="left">
	<logic:equal name="activity" property="identifier" value="0">
	n/a
	</logic:equal>
	<logic:notEqual name="activity" property="identifier" value="0">
	<a href="javascript:ack('/webapps-meme/meme/controller?state=ActionHarvester&midService=<bean:write name="insertionReportBean" property="midService"/>&transactionId=<bean:write name="activity" property="identifier"/>')"><bean:write name="activity" property="identifier"/></a>
	</td>
	</logic:notEqual>
    <td valign="top" align="left"><bean:write name="activity" property="timestamp" format="dd-MMM-yyyy hh:mm:ss" /></td>
    <bean:define id="elapsed" name="activity" property="elapsedTime" type="java.lang.Long"/>
    <jsp:setProperty name="calendar" property="timeInMillis"
  	value="<%= elapsed.longValue()  - calendar.get(calendar.ZONE_OFFSET) %>" />
    <td valign="top" align="left"><bean:write name="calendar"  property="time" format="H:mm:ss" /></td>
    <td valign="top" align="left"><bean:write name="activity" property="shortDescription"/> - <bean:write name="activity" property="description"/></td>
  </tr>
  </logic:iterate>
    </table>
    </center>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
   </body>
</html:html>
