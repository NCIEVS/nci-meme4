<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/meme.tld" prefix="meme"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
<title>EditMapSet:
<bean:write name="mapset" property="name"/>
</title>
</head>
<body bgcolor="#ffffff">
<center>
  <h1>    Edit MapSet:
    <bean:write name="mapset" property="name"/>
  </h1>
</center>
<hr width="100%"/>
<html:form action="/updateMapSet">
  <table align="center" width="90%" border="0">
    <tr>
      <td width="25%">Name (MAPSETNAME):</td>
      <td>
        <bean:write name="mapset" property="name"/>
      </td>
    </tr>
    <tr>
      <td>Description (SOS):</td>
      <td>
        <html:textarea property="description" name="mapset" cols="50"/>
      </td>
    </tr>
    <tr>
      <td>Identifier (MAPSETID):</td>
      <td>
        <bean:write name="mapset" property="mapSetIdentifier"/>
      </td>
    </tr>
    <tr>
      <td>Source (MAPSETVSAB):</td>
      <td>
        <html:select property="mapSetSource" name="mapset">
          <html:options name="sources"/>
        </html:select>
      </td>
    </tr>
    <tr>
      <td>Type (MAPSETTYPE):</td>
      <td>
        <bean:write name="mapset" property="type"/>
      </td>
    </tr>
    <tr>
      <td>From Source (FROMVSAB):</td>
      <td>
        <html:select property="fromSource" name="mapset">
          <html:options name="sources"/>
        </html:select>
      </td>
    </tr>
    <tr>
      <td>To Source (TOVSAB):</td>
      <td>
        <html:select property="toSource" name="mapset">
          <html:options name="sources"/>
        </html:select>
      </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td colspan="2">Optional Attributes</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>MTH_MAPSETCOMPLEXITY:</td>
      <td>
        <html:text name="mapset" property="mapSetComplexity"/>
      </td>
    </tr>
    <tr>
      <td>MTH_MAPFROMCOMPLEXITY:</td>
      <td>
        <html:text name="mapset" property="fromComplexity"/>
      </td>
    </tr>
    <tr>
      <td>MTH_MAPFROMEXHAUSTIVE:</td>
      <td>
        <html:radio name="mapset" property="isFromExhaustive" value="true"/>
        Yes
        <html:radio name="mapset" property="isFromExhaustive" value="false"/>
        No
      </td>
    </tr>
    <tr>
      <td>MTH_MAPTOCOMPLEXITY:</td>
      <td>
        <html:text name="mapset" property="toComplexity"/>
      </td>
    </tr>
    <tr>
      <td>MTH_MAPTOEXHAUSTIVE:</td>
      <td>
        <html:radio name="mapset" property="isToExhaustive" value="true"/>
        Yes
        <html:radio name="mapset" property="isToExhaustive" value="false"/>
        No
      </td>
    </tr>
    <tr>
      <td>MAPSETSEPARATORCODE:</td>
      <td>
        <html:text property="MAPSETSEPARATORCODE"/>
      </td>
    </tr>
    <tr>
      <td>MTH_UMLSMAPSETSEPARATOR:</td>
      <td>
        <html:text property="UMLSMAPSETSEPARATOR"/>
      </td>
    </tr>
    <tr>
      <td>MAPSETXRTARGETID:</td>
      <td>
        <html:text property="MAPSETXRTARGETID"/>
      </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>
        <html:submit>Update</html:submit>
        &nbsp;&nbsp;&nbsp;
        <html:button value="Cancel" property="" onclick="history.go(-1)"/>
      </td>
    </tr>
  </table>
</html:form>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home"/>
</body>
</html>
