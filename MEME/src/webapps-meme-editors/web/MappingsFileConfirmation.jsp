<%@taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic"%>
<html>
<head>
<title> Upload Confirmation.</title>
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css"></head>


<body bgcolor="#ffffff">
  <span id="blue"><center>Mappings File Upload Confirmation</center></span><hr width="100%">
<html:messages id="error">
      <li><bean:write name="error"/></li>
</html:messages>

<html:form action="/mappingFileInput" >
    <center><table border="0">
      <tr><td>The <bean:write name="FileName"/> file is successfully uploaded to <bean:write name="destination"/></td><td></td></tr>
      <tr><td></td><td align="center"><html:submit value="Click here to add more files" /></td></tr>
      <tr><td></td></tr>
    </table></center>
  </html:form>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</body>
</html>
