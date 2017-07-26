<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<html>
<head>
<title>
confirmation
</title>
</head>
<body bgcolor="#ffffff">
<h1>Confirmation</h1>
<br>
<logic:messagesPresent message="true">
       <html:messages id="message" message="true">
         <span id="success"><bean:write name="message"/></span><br>
       </html:messages>
</logic:messagesPresent>

<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</body>
</html>
