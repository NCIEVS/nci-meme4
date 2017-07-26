<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/meme.tld" prefix="meme"%>
<html:html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Status Report Management - Index Page</title>
  <link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
</HEAD>
<BODY bgcolor="#ffffff">
  <h2>
    <center>Status Report Management</center>
  </h2>
  <HR WIDTH=100%>
  <i>Select a report:</i>
  <br>
  &nbsp;
      <html:form action="/submitReport">
        <html:radio property="type" value="status">
          <html:link action="/submitReport?type=status">Status</html:link>
        </html:radio><br />
        <html:radio property="type" value="action">
          <html:link action="/submitReport?type=action">Action Items</html:link>
        </html:radio><br />
        <html:radio property="type" value="schedule">
          <html:link action="/submitReport?type=schedule">Schedule</html:link>
        </html:radio><br />
        <html:submit value="Go"/>
      </html:form>
    <meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home"/>

</body>
</html:html>
