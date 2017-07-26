<%@ page contentType="text/html;charset=utf-8"
	errorPage="MRDConceptReportErrorPage.jsp"%>
	
<%@ page import="gov.nih.nlm.mrd.client.ConceptReportClient"%>
<%@ page import="gov.nih.nlm.meme.common.*"%>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>


<HTML>
<HEAD>

<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="concept_bean" scope="session"
	class="gov.nih.nlm.mrd.beans.ConceptBean" />
	
<TITLE>Concept Reports from MRD</TITLE>

</HEAD>
<BODY>

	<%
		int count = 0;
		String source = request.getParameter("source");		
		String countStr = request.getParameter("count");
		if (countStr != null)
			count = Integer.parseInt(countStr); 
		ConceptReportClient crc = concept_bean.getConceptReportClient();
		
    	MRDAtom[] mrdAtomArray= crc.getMRDAtoms(request.getParameter("inputValue"), request.getParameter("idType"),source, count);
    	MRDAtom mrdAtom;
		
	%>


<FORM name="ConceptReportAtomForm" method="GET" action="controller">
<SPAN id="blue">
<CENTER>Concept Reports from MRD</CENTER>
</SPAN>
<CENTER>

<table border=1  align=center class=topTable>

 
<tr><td align=left>

	<TABLE width="100%" cellpadding="0" cellspacing="0" border=0>

	<TR>
	<td> &nbsp;&nbsp;&nbsp;&nbsp;</td>
	
	<td align=left>
		<br>
		<SPAN id="blue">
	<%
		if (request.getParameter("idType").equalsIgnoreCase("code"))
		{
	%>		
			<H3>Matching Atoms for Code: <%= request.getParameter("inputValue") %></H3>
	<%
		} else if (request.getParameter("idType").equalsIgnoreCase("tty")){
		
	%>	
			<H3>Matching Atoms for TTY: <%= request.getParameter("inputValue") %></H3>
	<%
		}
	%>
		
	</SPAN></Td>
	
	<TR>
	<td> &nbsp;&nbsp;&nbsp;&nbsp;</td>
	<td align=right class=data>
		<a href="controller?state=ConceptReportView">home</a>
	</td>
	</TR>
	
	
	<tr><td></td><td>	
	<br>
	<span class=data>Select the AUI  to see the matching concept report.</span>
	<br> <br>
	</td></tr>

	<tr><td></td>
	<td align=center>
		
		<TABLE width="90%" cellpadding="3" cellspacing="0" border=1>
	
		<TR class=headerRow>
			<TH class=headerRow> AUI </TH> 
			<TH class=headerRow> ATOM NAME</Th>
			<Th class=headerRow> SOURCE </Th>
	<%
		if (request.getParameter("idType").equalsIgnoreCase("code"))
		{
	%>
			<Th class=headerRow> TERM GROUP </Th>		
	<%
		} else if (request.getParameter("idType").equalsIgnoreCase("tty")){
	%>	
			<Th class=headerRow> CODE </Th>
	<%
		}
	%>	
			
		</TR>
	
	<% 	
		for (int i=0;i<mrdAtomArray.length;i++) 
		{
			mrdAtom = mrdAtomArray[i];
		
	%>
			<TR class=data> 
			<TD> <%= "<a href=\"controller?state=ConceptReportResultsView&inputValue="+mrdAtom.getAUI()+"&idType=aui\">"+mrdAtom.getAUI()+"</a> </TD>" %> 
			<TD> <%= mrdAtom.getString() %></TD>
			<TD> <%= mrdAtom.getSource().toString() %> </TD>
	<%
		if (request.getParameter("idType").equalsIgnoreCase("code"))
		{
	%>
			<TD> <%= mrdAtom.getSource().toString()+"/"+mrdAtom.getTty() %></TD>		
	<%
		} else if (request.getParameter("idType").equalsIgnoreCase("tty")){
		
	%>	
			<TD> <%= mrdAtom.getCode() %></TD>
	<%
		}
	%>	
			
		</TR>
			
	<%
		}
	%>		
			</table>
			
		</td></tr>
		
		<tr><td colspan=2> <br>
		<meme:footer name="OCCS" email="mailto:psuresh@nlm.nih.gov" url="/"
			text="Meta News Home" />
		</td></tr>
		
		</table>

</table>
</CENTER>
</BODY>
</HTML>
