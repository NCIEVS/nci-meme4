<%@ page contentType="text/html;charset=utf-8"
	errorPage="MRDConceptReportErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ConceptReportClient"%>
<%@ page import="gov.nih.nlm.meme.common.*"%>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>


<HTML>
<HEAD>

<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<script type="text/javascript" src="../table.js"></script>
<link rel="stylesheet" type="text/css" href="../attr_table.css" media="all">

<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">

<jsp:useBean id="concept_bean" scope="session"
	class="gov.nih.nlm.mrd.beans.ConceptBean" />
	
<TITLE>Concept Reports from MRD</TITLE>

</HEAD>
<BODY>

	<%
		ConceptReportClient crc = concept_bean.getConceptReportClient();
		//MRDConcept mrdConcept = crc.getMRDConcept();
		
    	MRDConcept mrdConcept= crc.getMRDConcept(request.getParameter("cui"), "cui");
    	
    	MRDAttribute[] mrdAttributeArray = (MRDAttribute[]) mrdConcept.getAttributes();
    	
    	MRDAttribute mrdAttribute;
		
	%>


<FORM name="ConceptReportAttrForm" method="GET" action="controller">
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
		<br><span class=smallheader>Attribute Report for
		for CUI: <%=mrdConcept.getCUI()%> 	&nbsp;&nbsp;&nbsp;
		Concept Id: <%=mrdConcept.getConceptId()%> </SPAN>
		</Td>
	<tr><td></td><td>	
	<br>
	<span class=data>Select the AUI  to see the matching concept report.</span>
	<br> <br>
	</td></tr>

	<tr><td></td>
	<td align=center>
	
		<table class="attribute_table table-autofilter" width=90% border=1 bordercolor="#000000">
		<thead>
		 <tr>
			 <th class="table-filterable">AUI</th>
			 <th class="table-filterable">ATTRIBUTE NAME</th>
			 <th >ATTRIBUTE VALUE</th>
			 <th class="table-filterable">SOURCE</th>
			 <th>CODE</th>
			 <th>STYPE</th>
		 </tr>
		</thead>
		
		<% 	
		String prevAui = null;
		String auiCellData = null;
		int auiRow =0;
		for (int i=0;i<mrdAttributeArray.length;i++) 
		{
			mrdAttribute = mrdAttributeArray[i];
			
			String aui = mrdAttribute.getAui();
			
			
			if (aui == null)
			{
				aui=" ";	
				auiCellData = "&nbsp";
			}else{
				char firtChar = aui.charAt(0);
				char secondChar = aui.charAt(1);
				
				//if it is AUI(first ltter A followed by number) display link 
				if( (firtChar == 'A' || firtChar ==  'a') && Character.isDigit(secondChar) )
					auiCellData = "<a href=\"controller?state=ConceptReportResultsView&inputValue="+aui+"&idType=aui\">"+aui+"</a> </TD>";
				else
					auiCellData = aui;
			}
			
			String code = mrdAttribute.getCode();
			if (code == null)
				code= "&nbsp";
			
			String sgType = mrdAttribute.getSgType();
			if (sgType == null)
				sgType="&nbsp";
		
			//first row
			if (prevAui == null)
				prevAui = aui;
			
			//increase the count and assing to prevAui
			if (!prevAui.equalsIgnoreCase(aui))
			{
				auiRow++;
				prevAui = aui;
			}
			
			//distinguish the aui rows by color 
			if (auiRow % 2 == 0)
			{
		%>
			<TR class=data bgcolor=#ddeeff>				
		<% 
			}else{
		%>	
				<TR class=data>
		<%
			}
		%>
			
			<TD> <%= auiCellData %>
			<TD> <%= mrdAttribute.getAName() %></TD> 
			<TD> <%= mrdAttribute.getAValue() %></TD>
			<TD> <%= mrdAttribute.getSource().toString() %> </TD>
			<TD> <%= code %> </TD>
			<TD> <%= sgType %> </TD>
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
