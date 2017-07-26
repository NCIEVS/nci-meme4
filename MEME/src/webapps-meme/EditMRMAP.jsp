<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/meme.tld" prefix="meme"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
<title>MRMAP Editor</title>
</head>
<body bgcolor="#ffffff">
<h1>
  <center>Review MRMAP mappings</center>
</h1>
<center>
<HR width="100%">
  <TABLE width="95%" border="0">
    <TR>
      <TH colspan="3">&nbsp;</TH>
    </TR>
    <TR>
      <TD colspan="3">&nbsp;</TD>
    </TR>
    <html:form action="/editMRMAP">
      <TR>
        <TD width="30%">Change database</TD>
        <TD width="70%">
          <bean:define id="midService" name="mappingBean" property="midService"/>
          <meme:midServiceList name="midService" submit="false" initialValue="<%= (String)midService %>"/>
        </TD>
      </TR>
      <TR>
        <TD width="30%">Change host</TD>
        <TD width="70%">
          <bean:define id="host" name="mappingBean" property="host"/>
          <meme:hostList name="host" submit="false" initialValue="<%= (String)host %>"/>
        </TD>
      </TR>
      <TR>
        <TD width="30%">Change port</TD>
        <TD width="70%">
          <html:text property="port" name="mappingBean" size="12"/>
          <html:submit value="Go" onmouseover="window.status='Click to change port.'; return true;" onmouseout="window.status=''; return true;"/>
        </TD>
      </TR>
    </html:form>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
    <tr>
      <html:form action="/viewMapSet">
        <td width="30%">Mapping:</td>
        <td>
          <html:select property="mapsetId" onchange="this.form.submit(); return true;">
            <html:option value="">-- SELECT MAPPING --</html:option>
              <html:options collection="mappings" property="value" labelProperty="label" />
          </html:select>
        </td>
      </html:form>
    </tr>
  </table>
</center>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home"/>
</body>
</html>
