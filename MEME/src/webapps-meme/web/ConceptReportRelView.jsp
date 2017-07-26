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
		int count=0;
		String rela = request.getParameter("rela");
		String rel = request.getParameter("rel");
 		String source = request.getParameter("source");
 		
 		String countStr = request.getParameter("count");
		if (countStr != null)
			count = Integer.parseInt(countStr); 
		

 
		ConceptReportClient crc = concept_bean.getConceptReportClient();
		//MRDConcept mrdConcept = crc.getMRDConcept();
		
    	MRDRelationship[]  mrdRelArray= crc.getMRDRels(request.getParameter("inputValue"), request.getParameter("idType"), source, rela,rel,count);
    	MRDRelationship mrdRelationship;
		
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
	<td align=right class=data>
		<a href="controller?state=ConceptReportView">home</a>
	</td>
	</TR>
	
	
	<tr>
	<td> &nbsp;&nbsp;&nbsp;&nbsp;</td>
	<td align=left>
		<br>
		<SPAN id="blue">
		<H3>Relationship Report for: <%= request.getParameter("inputValue") %></H3>
		</SPAN>
	</td>
	</tr>

	<tr><td></td>
	<td>	
	<br>
	<span class=data>Select the AUI/CUI  to see the matching concept report.</span>
	<br> <br>
	</td></tr>

	<tr><td></td>
	<td align=center>
		
		<TABLE width="90%" cellpadding="3" cellspacing="0" border=1>
	
		<TR class=headerRow>
			<TH class=headerRow> RUI </TH> 
			<TH class=headerRow> AUI1</Th>
			<Th class=headerRow> AUI2</Th>
			<Th class=headerRow> CUI1 </Th>
			<TH class=headerRow> CUI2</Th>
			<Th class=headerRow> RELATIONSHIP NAME</Th>
			<Th class=headerRow> RELATIONSHIP ATTRIBUTE </Th>
			<Th class=headerRow> SOURCE </Th>
		</TR>
	
	<% 	
		for (int i=0;i<mrdRelArray.length;i++) 
		{
			mrdRelationship = mrdRelArray[i];
			
			String aui1 = mrdRelationship.getAui_1();
			if (aui1 == null)
				aui1 = "";

			String aui2 = mrdRelationship.getAui_2();
			if (aui2 == null)
				aui2 = "";

			String cui1 = mrdRelationship.getCui_1();
			if (cui1 == null)
				cui1 = "";

			String cui2 = mrdRelationship.getCui_2();
			if (cui2 == null)
				cui2 = "";
			
			String relAttr="&nbsp;";
			if( mrdRelationship.getRelationship_attribute() != null)
				relAttr= mrdRelationship.getRelationship_attribute().toString();

			String relSource ="&nbsp;";
			if (mrdRelationship.getSource() != null)
				relSource =mrdRelationship.getSource().toString();

		
	%>
			<TR class=data>
			<TD> <%= mrdRelationship.getRui() %></TD>
			<TD> <%= "<a href=\"controller?state=ConceptReportResultsView&inputValue="+aui1+"&idType=aui\">"+aui1+"</a> </TD>" %>			 
			<TD> <%= "<a href=\"controller?state=ConceptReportResultsView&inputValue="+aui2+"&idType=aui\">"+aui2+"</a> </TD>" %>
			<TD> <%= "<a href=\"controller?state=ConceptReportResultsView&inputValue="+cui1+"&idType=cui\">"+cui1+"</a> </TD>" %>
			<TD> <%= "<a href=\"controller?state=ConceptReportResultsView&inputValue="+cui2+"&idType=cui\">"+cui2+"</a> </TD>" %> 
			<TD> <%= mrdRelationship.getRelationship_name() %></TD>
			<TD> <%= relAttr %> </TD>
			<TD> <%= relSource %></TD>
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
