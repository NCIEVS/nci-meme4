<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
EditSourceInfoComplete
</title>
</head>
<jsp:useBean id="sourceinfo_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<body>
<%  AuxiliaryDataClient aux_client = sourceinfo_bean.getAuxiliaryDataClient();
    Source source = aux_client.getSource(request.getParameter("source"));
    source.setOfficialName(request.getParameter("official_name"));
    source.setShortName(request.getParameter("short_name"));
    source.setSourceFamilyAbbreviation(request.getParameter("source_family"));
    source.setSourceVersion(request.getParameter("source_version"));
    if((request.getParameter("valid_start_date") != null) && (!"".equals(request.getParameter("valid_start_date"))))
    	source.setInsertionDate(sourceinfo_bean.getDateFormat().parse(request.getParameter("valid_start_date")));
    if((request.getParameter("valid_end_date") != null) && (!"".equals(request.getParameter("valid_end_date"))))
    	source.setExpirationDate(sourceinfo_bean.getDateFormat().parse(request.getParameter("valid_end_date")));
    source.setInsertMetaVersion(request.getParameter("insert_meta_version"));
    source.setRemoveMetaVersion(request.getParameter("remove_meta_version"));
    source.setLicenseContact(request.getParameter("license_contact"));
    source.setContentContact(request.getParameter("content_contact"));
    source.setCitation(request.getParameter("citation"));
    source.setRestrictionLevel(request.getParameter("restriction_level"));
    source.setContextType(request.getParameter("context_type"));
    Language[] languages = aux_client.getLanguages();
    for(int i=0; i < languages.length; i++) {
	if(languages[i].toString().equals(request.getParameter("language")))
    	source.setLanguage(languages[i]);
    }
    source.setCharacterEncoding(request.getParameter("character_set"));
    aux_client.setSource(source);
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Edit Complete</title>


</head>

<body bgcolor="#ffffff">
<h2><center>Changes to <%= source.getSourceAbbreviation() %> Complete</center></h2>

<hr WIDTH=100%>

    <i>The source information for <b><%= source.getSourceAbbreviation() %></b> was updated.  The following
    values were used:</i><br><br>


    <center>
    <table width="90%">
      <tr><td width="30%"><font size="-1">Source</font></td>
          <td width="70%"align="left"><tt><font size="-1"><%= source.getSourceAbbreviation() %></font></tt></td>

      </tr>
      <tr><td><font size="-1">SON</font></td>
          <td><tt><font size="-1"><%= (source.getOfficialName() == null ? "" : source.getOfficialName())%></font></tt></td>
      </tr>
      <tr><td><font size="-1">SSN</font></td>
          <td><tt><font size="-1"><%= (source.getShortName() == null ? "" : source.getShortName())%></font></tt></td>
      </tr>

      <tr><td><font size="-1">SF</font></td>
          <td><tt><font size="-1"><%= source.getSourceFamilyAbbreviation() %></font></tt></td>
      </tr>
      <tr><td><font size="-1">SVER</font></td>
          <td><tt><font size="-1"><%= (source.getSourceVersion() == null ? "" : source.getSourceVersion()) %></font></tt></td>
      </tr>
      <tr><td><font size="-1">VSTART</font></td>

          <td><tt><font size="-1"><%= (source.getInsertionDate() == null ? "" :  sourceinfo_bean.getDateFormat().format(source.getInsertionDate()))%></font></tt></td>
      </tr>
      <tr><td><font size="-1">VEND</font></td>
          <td><tt><font size="-1"><%= (source.getExpirationDate() == null ? "" :  sourceinfo_bean.getDateFormat().format(source.getExpirationDate()))%></font></tt></td>
      </tr>
      <tr><td><font size="-1">IMETA</font></td>
          <td><tt><font size="-1"><%= (source.getInsertMetaVersion() == null ? "" : source.getInsertMetaVersion()) %></font></tt></td>

      </tr>
      <tr><td><font size="-1">RMETA</font></td>
          <td><tt><font size="-1"><%= (source.getRemoveMetaVersion() == null ? "" : source.getRemoveMetaVersion()) %></font></tt></td>
      </tr>
      <tr><td><font size="-1">SLC</font></td>
          <td><tt><font size="-1"><%= (source.getLicenseContact() == null ? "" : source.getLicenseContact())%></font></tt></td>
      </tr>

      <tr><td><font size="-1">SCC</font></td>
          <td><tt><font size="-1"><%= (source.getContentContact() == null ? "" : source.getContentContact())%></font></tt></td>
      </tr>
      <tr><td><font size="-1">SCIT</font></td>
          <td><tt><font size="-1"><%= (source.getCitation() == null ? "" : source.getCitation())%></font></tt></td>
      </tr>
      <tr><td><font size="-1">SRL</font></td>

          <td><tt><font size="-1"><%= source.getRestrictionLevel()%></font></tt></td>
      </tr>
      <tr><td><font size="-1">CXTY</font></td>
          <td><tt><font size="-1"><%= (source.getContextType() == null ? "" : source.getContextType())%></font></tt></td>
      </tr>
      <tr><td><font size="-1">LAT</font></td>
          <td><tt><font size="-1"><%= (source.getLanguage() == null ? "" : source.getLanguage().toString())%></font></tt></td>

      </tr>
      <tr><td><font size="-1">CENC</font></td>
          <td><tt><font size="-1"><%= (source.getCharacterEncoding() == null ? "" : source.getCharacterEncoding())%></font></tt></td>
      </tr>
      <tr><td colspan="2">&nbsp;</td></tr>
      <tr><td colspan="2">
	  <font size="-1"><i><a href="controller?state=SourceInfoEditor&current_only=1">Back to index</a></i></font>

      </td></tr>
    </table>
    </center>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</body>
</html>
