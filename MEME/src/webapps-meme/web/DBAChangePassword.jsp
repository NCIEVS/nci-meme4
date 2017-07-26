<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<%@taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic"%>
<html>
<head>
<title>
Change Password
</title>
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css"></head>
<body bgcolor="#ffffff">
  <span id="blue"><center>Change Password</center></span><hr width="100%">

<logic:messagesPresent>
    <span id="errorsHeader">Error</span>
    <html:messages id="error">
      <li><bean:write name="error"/></li>
    </html:messages>
</logic:messagesPresent>

<html:form action="/dbachangePassword" >
    <center><table border="0">
      <tr><td>DBA Username:</td><td><html:text property="dbaUsername" /></td></tr>
      <tr><td>Password:</td><td><html:password property="dbaPassword" size="20"/></td></tr>
      <tr><td>Username:</td><td><html:text property="username" /></td></tr>
      <tr><td>New Password:</td><td><html:password property="password" size="20"/></td></tr>
      <tr><td>Confirm Password:</td><td><html:password property="confirmPassword" size="20"/></td></tr>
      <tr><td></td><td align="center"><html:submit value="Change" /> <html:button property="" value="Cancel" onclick="history.go(-1)"/> </td></tr>
    </table></center>
  </html:form>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</body>
</html>
