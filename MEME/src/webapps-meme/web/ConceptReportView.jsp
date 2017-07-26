<%@ page contentType="text/html;charset=utf-8"
	errorPage="EntryPointErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ConceptReportClient"%>
<%@ page import="gov.nih.nlm.meme.*"%>
<%@ page import="java.util.Comparator"%>
<%@ page import="java.util.Arrays"%>
<%@ page import="java.util.Date"%>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>

<jsp:useBean id="concept_bean" scope="session" 	class="gov.nih.nlm.mrd.beans.ConceptBean" />
<jsp:setProperty name="concept_bean" property="*" />

<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">

 
<script type="text/javascript">

	function showHideElements()
	{
		var idTypeVal=document.searchByIdForm.idType.value;
		if(idTypeVal=='rel')
		{
			document.getElementById('rela').style.display="inline";
			document.getElementById('source').style.display="inline";
			document.getElementById('count').style.display="inline";
			document.getElementById('rel').style.display="none";
			
		}
		else if (idTypeVal=='rela')
		{
			document.getElementById('rel').style.display="inline";
			document.getElementById('source').style.display="inline";
			document.getElementById('count').style.display="inline";
			document.getElementById('rela').style.display="none";
		}
		else if(idTypeVal=='tty')
		{
			document.getElementById('count').style.display="inline";
			document.getElementById('source').style.display="inline";
			document.getElementById('rela').style.display="none";
			document.getElementById('rel').style.display="none";
		}
		else if(idTypeVal=='code')
		{
			document.getElementById('count').style.display="inline";
			document.getElementById('rela').style.display="none";
			document.getElementById('source').style.display="none";
			document.getElementById('rel').style.display="none";
			
		}
		else
		{
			document.getElementById('rela').style.display="none";
			document.getElementById('source').style.display="none";
			document.getElementById('rel').style.display="none";
			document.getElementById('count').style.display="none";
		}
	
	}

	function submitForm()
	{
		
		//var idTypeVal = getIdTypeValue();
		var idTypeVal=document.searchByIdForm.idType.value;
		var inputValue = document.searchByIdForm.inputValue.value;
		
		if (inputValue == '')
		{
			alert("Please Enter Id ");
			return;
		}
		
		if (idTypeVal == '')
		{
			alert("Please select 'Treat Id as' ");
			return;
		}
		
		
		
		if (idTypeVal == 'code' || idTypeVal == 'tty')
		{
			document.searchByIdForm.state.value = "ConceptReportAtomView";
		}
		else if (idTypeVal == 'rel' || idTypeVal == 'rela')
		{
			document.searchByIdForm.state.value = "ConceptReportRelView";
		}
		else
		{
			document.searchByIdForm.state.value="ConceptReportResultsView";
		}
		document.searchByIdForm.submit();
	}

	function getIdTypeValue()
	{
		for (var i=0; i < document.searchByIdForm.idType.length; i++)
		{
		   if (document.searchByIdForm.idType[i].checked)
			  {
			  	var idTypeVal = document.searchByIdForm.idType[i].value;
			  }
		}

		return idTypeVal;
	}
</script> 


<TITLE>Concept Reports from MRD</TITLE>
</HEAD>

<%
	//get source
	ConceptReportClient crc = concept_bean.getConceptReportClient();
	String[] sourceArray = crc.getSources();
%>

<BODY>
<SPAN id="blue" WIDTH=70%>
<CENTER>Concept Reports from MRD</CENTER>
</SPAN>

<center>
<table border=1 class=topTable>
<tr><td>

<TABLE width="95%" border="0" align=center>
	<FORM name="searchByIdForm" method="GET" action="controller" onLoad="showHideElements()">
	
	<INPUT name="state" type="hidden" value="ConceptReportResultsView">
	

	<tr><td colspan=2 align=center> <br>
		<SPAN class=smallheader>
		SEARCH BY IDENTIFIER
		</SPAN>
	</td></tr>
	
		
	<TR>
		<Td colspan="2" class=data>
		Use this if you know the unique identifiers for the concept (CUI or concept_id), the atoms within the concept (AUI) or by source-specific identifiers (code).
		<br> 
		Submitting this form returns a (textual) concept
		report for the concept with the identifier specified. The identifier
		can be a CUI (Concept Unique Identifier), a concept_id (Concept ID),
		an AUI (Atom Unique Identifier),a source code, a TTY, a rel or a rela. 
		<br>  <br>
		<b>Examples:</b> CUI: <A
			HREF="controller?state=ConceptReportResultsView&host=cruciate.nlm.nih.gov&midService=mrd-db&inputValue=C0267170&idType=cui">C0267170</A>&nbsp;&nbsp;&nbsp;
		concept_id: <A
			HREF="controller?state=ConceptReportResultsView&midService=mrd-db&inputValue=1040879&idType=conceptId">1040879</A>&nbsp;&nbsp;&nbsp;
		AUI: <A
			HREF="controller?state=ConceptReportResultsView&midService=mrd-db&inputValue=A5574121&idType=aui">A5574121</A><br>
		<td>
	</TR>
	
	<TR>
		<TD colspan="2">&nbsp;</TD>
	</TR>

	<TR>
		<TD colspan="2" align=center>
		
		<table width=80%>	
			<TR>
				<TD class=data align=left width=11%>Enter Id:</TD>
				<TD align=left><INPUT type="text" name="inputValue" size="12">
			</TR>
		
			<TR>
				<TD class=data align=left width=10%>Treat Id as:</TD>
				<TD width="70%" align=left class=data valign=top>
					<select name="idType" onChange="showHideElements()">
						<option value="">SELECT--</option>
						<option value=cui>CUI</option>
						<option value=conceptId>Concept Id</option>
						<option value=aui>AUI</option>
						<option value=code>code</option>
						<option value=tty>TTY</option>
						<option value=rel>REL</option>
						<option value=rela>RELA</option>
					</select>
					
					<div id='rela' style="display:none;" >
						&nbsp;&nbsp;RELA: <INPUT type="text" name="rela" size="12" value="">
					</div>
					
					<div id='rel' style="display:none;" >
						&nbsp;&nbsp;REL: <INPUT type="text" name="rel" size="12" value="">
					</div>
					
					
					<div id='source' style="display:none;" >
					&nbsp;&nbsp;SOURCE: 
					<select name="source">
						<option value="">Include All Sources
	<%					
					for(int i=0; sourceArray != null && i<sourceArray.length; i++)
					{
	%>					
						<option> <%=sourceArray[i] %>
	<%
					}
	%>						
					</select>
					</div>
					
				</TD>

			
			
			<TR>
				<TD class=data align=left colspan=2>
				<div id='count' style="display:none;" >
					Show the First: 
					<select name="count">
						<option value="10000" >10000
						<option value="100">100
						<option value="1000" >1000
						<option value="5000" >5000
					</select>
					Values
					</div>
				 </TD>
			</TR>
			
							
 
			</TR>
		
			<TR>
				
				<td colspan=2 align=left> <br>
				<input accesskey="r" class="SPECIAL" type="button" name="Manage" value="Get Concept Report"  onClick="submitForm()">
				</td>
			</TR>
		</table>		
		</TD>
	</TR>
	</FORM>

	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>

		
	<tr><td colspan=2>
		<meme:footer name="OCCS" email="mailto:psuresh@nlm.nih.gov" url="/"
			text="Meta News Home" />
</td></tr>	
</table>

</td></tr>
</table>
</center>
</BODY>
</HTML>
