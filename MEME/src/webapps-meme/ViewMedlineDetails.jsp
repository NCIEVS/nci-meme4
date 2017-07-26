<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-nested.tld" prefix="nested" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<html:html>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
<TITLE>
Medline Details
</TITLE>
  <script language="javascript" type="">
function confirmDelete(file, href) {
	if (!confirm("Are you sure you want to delete "+ file +"?")) {
      		return false;
    	}
	frames["activate_frame"].location.href=href;
}
  </script></HEAD>
<BODY>
<iframe name="activate_frame" style="visibility:hidden" width=0 height=0></iframe>
  <html:form action="/processMedline">
    <bean:parameter id="stage" name="stage"/>
<span id=blue><center>Medline <bean:write name="stage"/> Log</center></span>
<hr WIDTH=100%>
  <PRE STYLE="margin-left:  1.0cm"><bean:write name="log"  filter="false"/></PRE>
  </html:form>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</BODY>

</html:html>

