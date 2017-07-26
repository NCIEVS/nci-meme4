<%@taglib uri="/WEB-INF/meme.tld" prefix="meme"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<html>
<head>
<link href="stylesheets.css" rel="stylesheet" type="text/css">
<title>DailyStatusReport</title>
</head>
<body bgcolor="#ffffff">
<span id=blue>
  <center>Daily Status Report</center>
</span>
<hr WIDTH=100%>
<table>
  <html:form action="/viewReleaseReport">
    <bean:parameter id="release" name="release"/>
    <bean:parameter id="date" name="date"/>
    <html:hidden property="release" value="<%= release %>"/>
    <html:hidden property="type" value="daily"/>
    <tr>
      <td>Select Date:</td>
      <td>
        <meme:calendar name="date" initialValue="<%= date %>" first="true"/>
      </td>
      <td>
        <html:submit styleClass="SPECIAL">View Report</html:submit>
      </td>
    </tr>
  </html:form>
</table>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home"/>
</body>
</html>
