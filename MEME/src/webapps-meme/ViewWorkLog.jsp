<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/meme.tld" prefix="meme"%>
<html:html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Work Logs</title>
  <link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
</HEAD>
<BODY bgcolor="#ffffff">
  <h2>
    <center>Review Activity/Work Logs</center>
  </h2>
  <HR WIDTH=100%>

  Choose the database and range of dates to search for database
  work.  Then select the work item to see details of that operation.
  <br>
  &nbsp;
  <center>
    <table WIDTH="90%">
      <tr>
        <td width="25%">Database:</td>
        <td>
          <html:form action="/viewWorkLog">
            <bean:define id="midService" name="worklogBean" property="midService" />
            <meme:midServiceList name="midService" submit="true" initialValue="<%= (String)midService %>"/>

        </td>
      </tr>
      <tr>
        <td width="25%">Range:</td>
        <td>
          <html:select name="worklogBean" property="range" onchange="this.form.submit(); return true;">
            <html:option value="30">Past Month</html:option>
            <html:option value="60">Past Two Months</html:option>
            <html:option value="90">Past Three Months</html:option>
            <html:option value="365">Past Year</html:option>
          </html:select>
</html:form>        </td>
      </tr>
      <tr>
        <td width="25%">Work:</td>
        <td>
          <html:form action="/viewActivity">
            <html:select property="work_id" onchange="this.form.submit(); return true;">
              <html:option value="">-- SELECT DB WORK --</html:option>
              <html:options collection="worklogs" property="value" labelProperty="label" />
            </html:select>
          </html:form>
        </td>
      </tr>
      <tr>
        <td width="25%" colspan="2">
          <br>
          <html:submit value="SUBMIT" />
        </td>
      </tr>
    </table>
    <bean:write name="worklogBean" property="host"/>
    <bean:write name="worklogBean" property="port"/>
  </center>
  <meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home"/>
</body>
</html:html>
