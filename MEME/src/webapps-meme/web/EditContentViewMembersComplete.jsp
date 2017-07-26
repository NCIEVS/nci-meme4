<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*, java.io.*, org.apache.commons.fileupload.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<TITLE>EditContentViewMembersComplete</TITLE>
<jsp:useBean id="cv_bean" scope="session" class="gov.nih.nlm.meme.beans.ContentViewBean" />
</HEAD>
<BODY>
<%
  ContentViewClient client = cv_bean.getContentViewClient();
  InputStream ui_stream = null;
  String action = null;

  //
  // Parse parameters
  //
  ContentView cv = null;
  DiskFileUpload dfu = new DiskFileUpload();
  List items = dfu.parseRequest(request);
  Iterator iter = items.iterator();
  while (iter.hasNext()) {
    FileItem fi = (FileItem) iter.next();
    if (fi.getFieldName().equals("action")) {
      action = fi.getString();
    }
    if (fi.getFieldName().equals("cv")) {
      Identifier identifier = new Identifier.Default(fi.getString());
      cv = client.getContentView(identifier);
    }
    if (fi.getFieldName().equals("upload")) {
      ui_stream = fi.getInputStream();
    }
  }

  //
  // Remove existing members
  //
  client.removeContentViewMembers(cv);

  //
  // Generate or load members
  //
  if (action.equals("generate")) {
    client.generateContentViewMembers(cv);
  } else if (action.equals("load")) {
    BufferedReader br = new BufferedReader(new InputStreamReader(ui_stream, "UTF-8"));
    String line = null;
    while ((line= br.readLine()) != null) {
      ContentViewMember member = new ContentViewMember.Default();
      member.setIdentifier(new Identifier.Default(line));
      member.setContentView(cv);
      cv.addMember(member);
    }
    client.addContentViewMembers(cv.getMembers());
  }
%>
<CENTER>
  <TABLE width="40%" border="0">
    <FORM method="GET" action="ViewContentViewMembers.jsp">
     <INPUT name="state" type="hidden" value="ViewContentViewMembers">
     <INPUT name="command" type="hidden" value="">
     <INPUT name="cv" type="hidden" value="<%= cv.getIdentifier() %>">
     <TR><TD>&nbsp;</TD></TR>
     <TR >
       <TD align="center">
        <FONT size="-1">
        <INPUT type="button" value="View Members"
               onMouseOver='window.status="View Content View Member";'
               onMouseOut='window.status="";'
               onClick='this.form.command.value="View"; this.form.submit(); return true;'>
        </FONT>
       </TD>
     </TR>
    </FORM>
  </TABLE>
</CENTER><P>
Click <A href="controller?state=ContentViewEditor">here</A> to return to the content view editor page.</P>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</BODY>
</HTML>
