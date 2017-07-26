<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.ContentView, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<TITLE>EditContentViewComplete</TITLE>
<jsp:useBean id="cv_bean" scope="session" class="gov.nih.nlm.meme.beans.ContentViewBean" />
<% cv_bean.reset(); %>
<jsp:setProperty name="cv_bean" property="*" />
</HEAD>
<BODY>
<%
    String command = request.getParameter("command");
    cv_bean.setIdentifier(request.getParameter("id"));
    if ("Insert".equals(command)) {
      cv_bean.getContentViewClient().addContentView(cv_bean.newContentView());
      out.println("<P>The Content View with following information was inserted.</P>");
    } else if ("Update".equals(command)) {
      ContentView cv1 = cv_bean.newContentView();
      cv_bean.getContentViewClient().setContentView(cv1);
      out.println("<P>The Content View " + cv_bean.getName() + " was modified using following information.</P>");
    } else if ("Delete".equals(command)) {
      cv_bean.getContentViewClient().removeContentView(cv_bean.newContentView());
      out.println("<P>The Content View <B>" + cv_bean.getName() + "</B> was successfully deleted.</P>");
    }
    if(command.equals("Insert") || command.equals("Update")) {
%>
      <TABLE border=0>
        <TR>
         <TD align="right"><FONT size="-1"><B>Name:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="name"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Contributor:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="contributor"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Contributor Version:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="contributorVersion"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Contributor URL:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="contributorURL"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Contributor Date:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><%= cv_bean.getContributorDate() %></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Maintainer:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="maintainer"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Maintainer Version:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="maintainerVersion"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Maintainer URL:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="maintainerURL"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Maintainer Date:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><%= cv_bean.getMaintainerDate() %></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Class:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="contentViewClass"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Code:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="code"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Category:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="category"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Sub Category:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="subCategory"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Previous Meta:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="previousMeta"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Description:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="description"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Algorithm:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><jsp:getProperty name="cv_bean" property="algorithm"/></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Cascade:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><%= cv_bean.getCascade() ? "Yes" : "No" %></FONT></TD>
        </TR>
        <TR>
         <TD align="right"><FONT size="-1"><B>Generated By Query:</B></FONT></TD>
         <TD align="left"><FONT size="-1"><%= cv_bean.isGeneratedByQuery() ? "Yes"  :"No" %></FONT></TD>
        </TR>
      </TABLE>
<% } %>
      </CENTER><P>
Click <A href="meme/controller?state=ContentViewEditor">here</A> to return to the content view editor page.
      </P>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</BODY>
</HTML>
