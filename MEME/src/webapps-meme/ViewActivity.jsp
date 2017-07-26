<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<jsp:useBean id="calendar" class="java.util.GregorianCalendar" scope="session" />

<html:html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>List Activities (<bean:write name="worklog" property="identifier"/>)</title>

  <script language="javascript" type="">
      function ack(url) {
   ok = confirm("This may involve a lot of actions\r\n"+
                "Please click OK if you want to proceed.");
   if (ok) {
      location.href=url;
   }
}
  </script>


<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">


</HEAD>

<BODY bgcolor="#ffffff">
<h2><center>List Activites (<bean:write name="worklog" property="identifier"/>)</center></h2>
<HR WIDTH=100%>

Following are the activities for this work (<bean:write name="worklog" property="identifier"/>).
<br>&nbsp;
<table border="0">
<tr><td><B>Work</b></td><td><bean:write name="worklog" property="description"/></td></tr>
<tr><td><B>Authority</b></td><td>I<bean:write name="worklog" property="authority"/></td></tr>
</table>

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
	<a href="javascript:ack('/webapps-meme/meme/controller?state=ActionHarvester&midService=<bean:write name="worklogBean" property="midService"/>&transactionId=<bean:write name="activity" property="identifier"/>')"><bean:write name="activity" property="identifier"/></a>
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
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
   </body>
</html:html>
