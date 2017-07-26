<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html:html>
<head>
<title>
ViewActionReport
</title>
  <link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
</head>
<body bgcolor="#ffffff">
<h1>
<center>Action Items Report</center>
</h1>
  <html:link action="/viewReport" paramId="type" paramName="viewReportActionForm" paramProperty="type">Back to Index
  </html:link>
<br><br>
  <table>
<tr>
  <td>
<html:form action="/viewActionReport" method="post">
  <html:hidden property="type" value="action"/>
    <html:select property="reportID" name="viewReportActionForm">
  <html:options collection="options" property="value" labelProperty="label"/>
</html:select>
<html:submit value="Go"/>
</html:form>
  </td>
      <td>
        Upload New Report
  </td>
      <td>
          <html:form action="/uploadSubmit" enctype="multipart/form-data">
            <html:file property="theFile"/>
            <html:hidden property="type" value="action"/>
            <html:hidden property="filePath" value="/StatusReports"/>
            <html:submit value="Upload"/>
          </html:form>
      </td>
    </tr>
  </table>
  <logic:present name="viewReportActionForm" property="reportID">
    <p>Display the contents of the
    <code><bean:write name="viewReportActionForm" property="reportID"/></code> resource for this web application, with no filtering.</p>
    <hr />
    <pre>
    <bean:define id="reportID" name="viewReportActionForm" property="reportID" />
    <bean:resource id="webxml" name="<%=(String) reportID %>" />
      <%=webxml.replaceAll("(http:\\/\\/[^\\s]*)(\\s)","<a href=\"$1\">$1</a>$2") %>
    </pre>
  </logic:present>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</body>
</html:html>
