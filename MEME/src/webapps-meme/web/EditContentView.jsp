<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<jsp:useBean id="cv_bean" scope="session" class="gov.nih.nlm.meme.beans.ContentViewBean" />
<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<SCRIPT LANGUAGE="JavaScript">

  function form2Submit(form, command) {
    if (command == "Delete") {
      var flag = confirm("Are you sure you want to delete this content view?");
      if (!flag)
        return false;
    }
    var name = document.form1.name;
    var previousMeta = document.form1.previousMeta;
    var contributor = document.form1.contributor;
    var contributorVersion = document.form1.contributorVersion;
    var contributorURL = document.form1.contributorURL;
    var contributorDate = document.form1.contributorDate;
    var maintainer = document.form1.maintainer;
    var maintainerVersion = document.form1.maintainerVersion;
    var maintainerURL = document.form1.maintainerURL;
    var maintainerDate = document.form1.maintainerDate;
    var contentViewClass = document.form1.contentViewClass;
    var code = document.form1.code;
    var category = document.form1.category;
    var subCategory = document.form1.subCategory;
    var description = document.form1.description;
    var algorithm = document.form1.algorithm;

    var str = name.value;
    if (str == "") {
      alert ("The content view name must not be null.");
      name.focus();
      return false;
    }
    str = contributor.value;
    if (str == "") {
      alert ("The content view contributor must not be null.");
      contributor.focus();
      return false;
    }
    str = contributorVersion.value;
    if (str == "") {
      alert ("The content view contributor version must not be null.");
      contributorVersion.focus();
      return false;
    }
    str = contributorURL.value;
    if (str == "") {
      alert ("The content view contributor url must not be null.");
      contributorURL.focus();
      return false;
    }
    str = maintainer.value;
    if (str == "") {
      alert ("The content view maintainer must not be null.");
      maintainer.focus();
      return false;
    }
    str = maintainerVersion.value;
    if (str == "") {
      alert ("The content view maintainer version must not be null.");
      maintainerVersion.focus();
      return false;
    }
    str = maintainerURL.value;
    if (str == "") {
      alert ("The content view maintainer url must not be null.");
      maintainerURL.focus();
      return false;
    }
    str = contentViewClass.value;
    if (str == "") {
      alert ("The content view class must not be null.");
      contentViewClass.focus();
      return false;
    }
    str = code.value;
    if (str == "") {
      alert ("The content view code must not be null.");
      code.focus();
      return false;
    }
    str = category.value;
    if (str == "") {
      alert ("The content view category must not be null.");
      category.focus();
      return false;
    }
    str = description.value;
    if (str == "") {
      alert ("The content view description must not be null.");
      description.focus();
      return false;
    }
    str = algorithm.value;
    if (str == "") {
      alert ("The content view algorithm must not be null.");
      algorithm.focus();
      return false;
    }
    form.command.value=command;
    form.submit();
  }

function launchCenter(url, name, height, width) {
	var str = "height=" + height + ",innerHeight=" + height;
	str += ",width=" + width + ",innerWidth=" + width;
	if (window.screen) {
		var ah = screen.availHeight - 30;
		var aw = screen.availWidth - 10;
		var xc = (aw - width) / 2;
		var yc = (ah - height) / 2;
		str += ",left=" + xc + ",screenX=" + xc;
		str += ",top=" + yc + ",screenY=" + yc;
	}
	str += ", menubar=no, scrollbars=yes, status=0, location=0, directories=0, resizable=1";
	window.open(url, name, str);
}
</SCRIPT>
<TITLE>EditContentView</TITLE>
</HEAD>
<%
   ContentView cv = new ContentView.Default();
   Identifier identifier = null;
   if(!"Insert".equals(request.getParameter("command"))) {
     if (!request.getParameter("cv").equals(""))
       identifier = new Identifier.Default(request.getParameter("cv"));
     if (identifier != null)
       cv = cv_bean.getContentViewClient().getContentView(identifier);
   } else {
     if (!request.getParameter("cv").equals("")) {
       cv.setName(request.getParameter("cv"));
     }
   }
%>
<BODY>
<SPAN id="blue"><CENTER>Edit Content View</CENTER></SPAN>
<HR width="100%">
<CENTER>
<TABLE width="80%" border="0">
  <FORM name="form1" method="GET" action="controller">
  <INPUT name="state" type="hidden" value="EditContentViewComplete">
  <INPUT name="command" type="hidden" value="">
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_name"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Name:
     </a></TD><TD align="left"><INPUT type="text" size="65" name="name" value="<%= cv.getName() == null ? "" : cv.getName() %>"></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_contributor"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Contributor:</a></TD><TD align="left"><INPUT type="text" size="65" name="contributor" value="<%= cv.getContributor() == null ? "" :  cv.getContributor() %>"></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_contributor_version"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Contributor Version:</a></TD><TD align="left"><INPUT type="text" name="contributorVersion" value="<%= cv.getContributorVersion() == null ? "" :  cv.getContributorVersion() %>"></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_contributor_url"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Contributor URL:</a></TD><TD align="left"><INPUT type="text" name="contributorURL" value="<%= cv.getContributorURL() == null ? "" :  cv.getContributorURL() %>"></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_contributor_date"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
      Contributor Date:</a></TD><TD align="left">
      <% String cd_date =  cv.getContributorDate() == null ? "" :  cv_bean.getDateFormat().format(cv.getContributorDate()); %>
      <meme:calendar name="contributorDate" first="true" initialValue="<%= cd_date %>" />
      </TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_maintainer"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Maintainer:</a></TD><TD align="left"><INPUT type="text" size="65" name="maintainer" value="<%= cv.getMaintainer() == null ? "" :  cv.getMaintainer() %>"></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_maintainer_version"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Maintainer Version:</a></TD><TD align="left"><INPUT type="text" name="maintainerVersion" value="<%= cv.getMaintainerVersion() == null ? "" :  cv.getMaintainerVersion() %>"></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_maintainer_url"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Maintainer URL:</a></TD><TD align="left"><INPUT type="text" name="maintainerURL" value="<%= cv.getMaintainerURL() == null ? "" :  cv.getMaintainerURL() %>"></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_maintainer_date"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Maintainer Date:</a></TD><TD align="left">
      <% String md_date =  cv.getMaintainerDate() == null ? "" :  cv_bean.getDateFormat().format(cv.getMaintainerDate()); %>
      <meme:calendar name="maintainerDate" first="false" initialValue="<%= md_date %>" />
      </TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_class"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Class:</a></TD><TD align="left"><INPUT type="text" size="65" name="contentViewClass" value="<%= cv.getContentViewClass() == null ? "" :  cv.getContentViewClass() %>"></TD></TR>
    <TR>
      <TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_code"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Code:</a></TD>
      <TD align="left">
	<SELECT name ="code">
          <%
            for (int i=0; i<32; i++) {
              int p = (int) Math.pow(2,i);
          %>
              <OPTION value="<%= p %>" <%= cv.getCode() == p ? "SELECTED" : "" %> ><%= i %></OPTION>
          <%
            }
          %>
        </SELECT>
      </TD>
    </TR>
    <TR>
      <TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_category"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Category:</a></TD>
      <TD align="left">
<!-- This should be an editable combo-box -->
        <input type="text" size="65" name="category" value="<%= (cv.getCategory() != null) ? cv.getCategory() : "" %>">
<%--	<SELECT name ="category">
          <%
            for (int i=0; i<6; i++) {
              String cat = "CAT "+i;
          %>
              <OPTION value="<%= cat %>" <%= (cv.getCategory() != null && cv.getCategory().equals(cat)) ? "SELECTED" : "" %> ><%= cat %></OPTION>
          <%
            }
          %>
        </SELECT>
--%>
      </TD>
    </TR>
    <TR>
      <TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_subcategory"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Sub Category:</a></TD>
      <TD align="left">
<!-- This should be an editable combo-box -->
        <input type="text" size="65" name="subCategory" value="<%= (cv.getSubCategory() != null) ? cv.getSubCategory() : "" %>">
<%--
	<SELECT name ="subCategory">
          <%
            for (int i=0; i<6; i++) {
              String subcat = "SUBCAT "+i;
          %>
              <OPTION value="<%= subcat %>" <%= (cv.getSubCategory() != null && cv.getSubCategory().equals(subcat)) ? "SELECTED" : "" %> ><%= subcat %></OPTION>
          <%
            }
          %>
        </SELECT>
--%>
      </TD>
    </TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_previous_meta"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Previous Meta:
     </a></TD><TD align="left"><INPUT type="text" size="65" name="previousMeta" value="<%= cv.getPreviousMeta() == null ? "" : cv.getPreviousMeta() %>"></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_description"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Description:</a></TD><TD><TEXTAREA name="description" wrap="soft" cols="65" rows="4"><%= cv.getDescription() == null ? "" : cv.getDescription() %></TEXTAREA></TD></TR>
    <TR><TD align="right">
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_algorithm"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Algorithm:</a></TD><TD><TEXTAREA name="algorithm" wrap="soft" cols="65" rows="4"><%= cv.getAlgorithm() == null ? "" : cv.getAlgorithm() %></TEXTAREA></TD></TR>
    <TR><TD align="right">&nbsp;</TD><TD align="left"><INPUT type="checkbox" name="cascade" <%= cv.getCascade() ? "checked" : "" %>>
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_cascade"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Cascade</a></TD></TR>
    <TR><TD align="right">&nbsp;</TD><TD align="left"><INPUT type="checkbox" name="isGeneratedByQuery" <%= cv.isGeneratedByQuery() ? "checked" : "" %>>
     <a href="/webapps-meme/meme/controller?state=ViewCodeDescription&category=form_field&code=content_view_generated"
        onclick="launchCenter(this.href, 'Form Field Description', 200,480);return false;" >
     Generated</a></TD></TR>
    <TR><TD><INPUT type="hidden" name="id" value="<%= cv.getIdentifier() %>"></TD></TR>
    <TR >
      <TD colspan=2 width="50%" align="center">
        <FONT size="-1">
        <% if ("Insert".equals(request.getParameter("command"))) { %>
             <INPUT type="button" value="Insert"
                    onMouseOver='window.status="Insert New Content View";'
                    onMouseOut='window.status="";'
                    onClick='form2Submit(this.form, "Insert");'>
        <% } else { %>
  	     <INPUT type="button" value="Update"
	            onMouseOver='window.status="Update selected Content View";'
                    onMouseOut='window.status="";'
                    onClick='form2Submit(this.form, "Update");'>
	     <INPUT type="button" value="Delete"
	            onMouseOver='window.status="Delete selected Content View";'
                    onMouseOut='window.status="";'
                    onClick='form2Submit(this.form, "Delete");'>
        <%}%>
        <INPUT type="button" value="Cancel"
               onMouseOver='window.status="Cancel Edit Content View";'
               onMouseOut='window.status="";'
               onClick="history.go(-1)">
        </FONT>
      </TD>
    </TR>
  </FORM>
<SCRIPT language="JavaScript">
STR_ICONPATH = "<%= request.getContextPath() %>/img/";
var cal1 = new calendar2(document.forms['form1'].elements['contributorDate']);
cal1.base = "<%= request.getContextPath() %>/";
var cal2 = new calendar2(document.forms['form1'].elements['maintainerDate']);
cal2.base = "<%= request.getContextPath() %>/";
</SCRIPT>
</TABLE>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</BODY>
</HTML>
