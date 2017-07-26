<%@taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic"%>
<%@ page import="java.util.*, java.io.File, java.io.FileFilter, java.lang.Exception" %>

<html>
<head>
<title>Upload Mappings File</title>
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css"></head>


<body bgcolor="#ffffff">
  <span id="blue"><center>Upload the Mappings File</center></span><hr width="100%">
 <html:errors/>


<logic:messagesPresent message="true">
    <span id="errorsHeader">Error</span>
    <html:messages id="error" message="true">
      <li><bean:write name="error"/></li>
    </html:messages>
</logic:messagesPresent>

<html:form action="/mappingFileConfirmation" enctype="multipart/form-data">
<%
  String path = request.getRealPath("/");
  String dirPath = ".." + path + "www/Mappings";
  File dir = new File(path + "../www/Mappings");
  
  FileFilter fileFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
  };
%>
      <table border="0">
      <tr><td>Select the destination directory ( where you want to put the files)</td></tr>
<%
  File[] files = dir.listFiles(fileFilter);
  if (files == null) {
        // Either dir does not exist or is not a directory
    } else {
        for (int i=0; i<files.length; i++) {
            String filename = files[i].getName();
%>
      <tr><td><td></tr>
      <tr><td><td></tr>
      <tr><td><td></tr>
      <tr><td><input type="radio" name="destDir" value=<%=filename%>><%=filename%></td></tr>
<%
        }
    }
%>
      <tr><td><td></tr>
      <tr><td><td></tr>
      <tr><td><td></tr>
    </table>
File
<input type="file" name="theFile"/>
<input type="submit"/>

</html:form>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</body>
</html>
