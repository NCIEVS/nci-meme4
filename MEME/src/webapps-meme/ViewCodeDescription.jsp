<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.MetaCode, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="metacode_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="metacode_bean" property="*" />

<TITLE>Code Description</TITLE>
</HEAD>

<% AuxiliaryDataClient aux_client = metacode_bean.getAuxiliaryDataClient();
   String category = request.getParameter("category");
   String code = request.getParameter("code");
   String description  = aux_client.getValueByCode(category,code);
%>

<BODY bgcolor="#ffffff">
<h2><center>Code Description</center></h2>
<HR WIDTH=100%>

    <blockquote>

      <center><table CELLPADDING=2 WIDTH="90%" BORDER=1>
           <td valign="top" width="30%"><b><font size="-1">Code</font></b></td>
           <td valign="top" width="50%"><b><font size="-1">Description</font></b></td>
    </tr>
      <tr> <td valign="top"><%=code%></td> <td valign="top"><%=description%></td>

        </tr>


    </table></center>
    </blockquote>
    </body>
</html>
