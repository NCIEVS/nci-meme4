<%@taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>
<html>
<head>
<title>View <bean:write name="mapset" property="name"/></title>
</head>
<body bgcolor="#ffffff">
<h1>
  <center>View <bean:write name="mapset" property="name"/></center>
</h1>
<hr width="100%"/>
<center>
  <logic:equal name="mappingList" property="previousPageAvailable" value="true" >
    <a href="viewMapping.do?mrsmap=&pageDirection=previous"><font color="green"><B>&lt;&lt; Prev</B></font></a>
  </logic:equal>
  <logic:equal name="mappingList" property="nextPageAvailable" value="true" >
    <a href="viewMapping.do?mrsmap=&pageDirection=next"><font color="green"><B>Next &gt;&gt;</B></font></a>
  </logic:equal>
</center>
<br />
<table width="100%" align="center" border="1" cellspacing="0" cellpadding="1">
  <tr>
    <th valign="bottom">CUI</th>
    <th valign="bottom">SAB</th>
    <th valign="bottom">ID</th>
    <th valign="bottom">SID</th>
    <th valign="bottom">FROMEXPR</th>
    <th valign="bottom">FROMTYPE</th>
    <th valign="bottom">REL</th>
    <th valign="bottom">RELA</th>
    <th valign="bottom">TOEXPR</th>
    <th valign="bottom">TOTYPE</th>
  </tr>
  <logic:iterate id="mapping" name="mappingList">
  <tr>
    <td>
      <bean:write name="mapset" property="CUI"/>
      <logic:empty name="mapset" property="CUI">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="mapset" property="mapSetSource.sourceAbbreviation"/>
      <logic:empty name="mapset" property="mapSetSource.sourceAbbreviation">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="mapping" property="ATUI"/>
      <logic:empty name="mapping" property="ATUI">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="mapping" property="sourceIdentifier"/>
      <logic:equal name="mapping" property="sourceIdentifier" value="">&nbsp;</logic:equal>
    </td>
    <td>
      <bean:write name="mapping" property="from.expression"/>
      <logic:empty name="mapping" property="from.expression">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="mapping" property="from.type"/>
      <logic:empty name="mapping" property="from.type">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="mapping" property="relationshipName"/>
      <logic:empty name="mapping" property="relationshipName">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="mapping" property="relationshipAttribute"/>
      <logic:empty name="mapping" property="relationshipAttribute">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="mapping" property="to.expression"/>
      <logic:empty name="mapping" property="to.expression">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="mapping" property="to.type"/>
      <logic:empty name="mapping" property="to.type">&nbsp;</logic:empty>
    </td>
  </tr>
  </logic:iterate>
</table>
<br />
<center>
  <logic:equal name="mappingList" property="previousPageAvailable" value="true" >
    <a href="viewMapping.do?mrsmap=&pageDirection=previous"><font color="green"><B>&lt;&lt; Prev</B></font></a>
  </logic:equal>
  <logic:equal name="mappingList" property="nextPageAvailable" value="true" >
    <a href="viewMapping.do?mrsmap=&pageDirection=next"><font color="green"><B>Next &gt;&gt;</B></font></a>
  </logic:equal>
</center>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home"/>
</body>
</html>
