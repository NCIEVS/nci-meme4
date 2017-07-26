<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html:html>
<head>
<title>
AddInsertionReportProblem
</title>
<script type="text/javascript">
function buildQueryString(theFormName) {
	theForm = document.forms[theFormName];
	var qs = ''
	for (e=0;e<theForm.elements.length;e++) {
		if (theForm.elements[e].name!='') {
			qs+=(qs=='')?'?':'&'
			qs+=theForm.elements[e].name+'='+escape(theForm.elements[e].value)
			}
		}
	return qs
}
function submitToOpener(theFormName) {
  window.opener.location.href = "addInsertionProblem.do" + buildQueryString(theFormName);
  self.close();
}
</script>
</head>
<body bgcolor="#ffffff" >
<h1>
Add Insertion Problem
</h1>
<html:form method="post" action="/addInsertionProblem.do" >
  <html:hidden property="identifier" name="worklog"/>
  <table>
    <tr>
      <td>Problem:</td>
      <td><html:text property="problem"></html:text></td>
    </tr>
    <tr>
      <td>Date:</td>
      <td><meme:calendar name="date" initialValue="" first="true"/></td>
    </tr>
    <tr>
      <td>Authority:</td>
      <td><html:text property="authority"></html:text></td>
    </tr>
    <tr>
      <td>Recurrence:</td>
      <td><html:text property="recurrence"></html:text></td>
    </tr>
    <tr>
      <td>Origin:</td>
      <td><html:textarea property="origin"></html:textarea></td>
    </tr>
    <tr>
      <td>Solution:</td>
      <td><html:textarea property="solution"></html:textarea></td>
    </tr>
    <tr>
      <td>Feedback:</td>
      <td><html:textarea property="feedback"></html:textarea></td>
    </tr>
  </table>
<br><br>
<input type="button" name="Submit" value="Submit" onclick="submitToOpener(this.form.name);">
 <html:reset>Reset
 </html:reset>
</html:form>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html:html>
