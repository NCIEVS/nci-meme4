<%@taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic"%>
<%@ page import="org.apache.struts.action.Action" %> 
<%@ page import="org.apache.struts.action.ActionMessages" %>
<html>
<html:html locale="false">
<title> Login to Load Mappings File.</title>
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css"></head>


<body bgcolor="#ffffff">
  <span id="blue"><center>Login</center></span><hr width="100%">
 <html:errors/>


<logic:messagesPresent message="true">
    <span id="errorsHeader">Error</span>
    <html:messages id="error" message="true">
      <li><bean:write name="error"/></li>
    </html:messages>
</logic:messagesPresent>
<html:form action="/mappingFileInput" onsubmit="return validateMappingsLogonActionForm(this);">
    <center><table border="0">
      <tr><td>Username:</td><td><html:text property="username" /></td></tr>
      <tr><td>Password:</td><td><html:password property="password" size="20"/></td></tr>
      <tr><td></td><td></td></tr>
      <tr><td></td><td align="center"><html:submit value="Login" /> <html:button property="" value="Cancel" onclick=
"history.go(-1)"/> </td></tr>
    </table></center>
<!-- Begin Validator Javascript Function-->
<html:javascript formName="MappingsLogonActionForm" dynamicJavascript="true" staticJavascript="true"/>
<!-- End of Validator Javascript Function-->
  </html:form>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</body>
</html:html>
~