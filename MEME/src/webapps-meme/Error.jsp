<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ page import="java.io.PrintWriter"%>
<html:html>

<head>
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
<title>ErrorPage</title>
</head>

<logic:notPresent name="MEMEException">
  <logic:notPresent name="message">
    <H3>Something happened...</H3>
    <B>But no further information was provided.</B>
  </logic:notPresent>
</logic:notPresent>
<P/>
<logic:present name="MEMEException">
  <H3>Error!</H3>
  <B><font color="red"><bean:write name="MEMEException" property="class.name"/></font></B>
  <P/>
  <bean:write name="MEMEException" property="message"/>
</logic:present>
<P/>
<logic:present name="MEMEException">
  <h4>Stack</h4>
  <i><pre>
<%
  Exception e = (Exception)request.getAttribute("MEMEException");
  e.printStackTrace(new PrintWriter(out));
%>
  </pre></i>
</logic:present>
<html:form action="/Close">
  <table width="90%">
    <td align="center">
      <html:button value="Close" property="" onclick="window.close(); return true"/>
    </td>
  </table>
</html:form>
</body>
</html:html>

