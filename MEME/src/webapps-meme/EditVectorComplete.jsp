<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.integrity.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
EditVectorComplete
</title>
</head>
<jsp:useBean id="integrity_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<body>
<%  StringTokenizer st = new StringTokenizer(request.getParameter("vector"),";");
    IntegrityVector ic_vector = new IntegrityVector.Default();
    while(st.hasMoreElements()) {
      StringTokenizer sub_st = new StringTokenizer((String)st.nextElement(),":");
      ic_vector.addIntegrityCheck(integrity_bean.getAuxiliaryDataClient().getIntegrityCheck((String)sub_st.nextElement()),(String)sub_st.nextElement());
    }
    if("Application".equals(request.getParameter("type"))) {
      if("Insert".equals(request.getParameter("command")))
        integrity_bean.getAuxiliaryDataClient().addApplicationVector(request.getParameter("name"),ic_vector);
      else if("Update".equals(request.getParameter("command")))
        integrity_bean.getAuxiliaryDataClient().setApplicationVector(request.getParameter("name"),ic_vector);
      else if("Delete".equals(request.getParameter("command")))
        integrity_bean.getAuxiliaryDataClient().removeApplicationVector(request.getParameter("name"));
    } else if("Override".equals(request.getParameter("type"))) {
      if("Insert".equals(request.getParameter("command")))
        integrity_bean.getAuxiliaryDataClient().addOverrideVector(new Integer(request.getParameter("name")).intValue(),ic_vector);
      else if("Update".equals(request.getParameter("command")))
        integrity_bean.getAuxiliaryDataClient().setOverrideVector(new Integer(request.getParameter("name")).intValue(),ic_vector);
      else if("Delete".equals(request.getParameter("command")))
        integrity_bean.getAuxiliaryDataClient().removeOverrideVector(new Integer(request.getParameter("name")).intValue());
    }
%>
          <%
            if(request.getParameter("command").equals("Delete"))
              out.println("<p>The " + request.getParameter("type") +"Vector <i>" + request.getParameter("name") +" </i> was successfully deleted.<br> </p>");
            else {
                out.println("<p>The " + request.getParameter("type") +"Vector <i>" + request.getParameter("name") +" </i> was " + (request.getParameter("command").equals("Insert") ? "inserte" : request.getParameter("command").toLowerCase() )+ "d using following integrity check vector. <br> </p>");
                IntegrityVector vector = null;
              if("Application".equals(request.getParameter("type")))
                vector = integrity_bean.getAuxiliaryDataClient().getApplicationVector(request.getParameter("name"));
              else if("Override".equals(request.getParameter("type")))
                vector = integrity_bean.getAuxiliaryDataClient().getOverrideVector(new Integer(request.getParameter("name")).intValue());
              IntegrityCheck[] checks = vector.getChecks();
              StringBuffer ic_vector_sb = new StringBuffer();
             for (int i=0; i < checks.length; i++) {
              if(ic_vector_sb.length() > 40)
                ic_vector_sb.append("<br>");
              ic_vector_sb.append("&lt;").append(checks[i].getName())
                .append(":").append(vector.getCodeForCheck(checks[i])).append("&gt;");
            }
            out.println("<PRE>" + ic_vector_sb.toString() + "</PRE>");
          }
          %>
          <br>
	Click <a href="<%= request.getRequestURL() %>?state=IntegrityEditor">here</a> to return to the integrity check editor page.
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</body>
</html>
