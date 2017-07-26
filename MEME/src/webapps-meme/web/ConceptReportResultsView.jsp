<%@ page contentType="text/html;charset=utf-8"
	errorPage="MRDConceptReportErrorPage.jsp"%>
	
<%@ page import="gov.nih.nlm.mrd.client.ConceptReportClient"%>
<%@ page import="gov.nih.nlm.meme.common.*"%>
<%@ page import="gov.nih.nlm.mrd.common.ConceptUtil"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.List"%>



<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>


<HTML>
<HEAD>

<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="concept_bean" scope="session"
	class="gov.nih.nlm.mrd.beans.ConceptBean" />
	
	
<%! String rxCUIVal,attrAUI; %>
<%! String blankSpace=" ", delimiter="/"; %>
	
<TITLE>Concept Reports from MRD</TITLE>

 <STYLE type="text/css">
   .header {font-size:13pt; font-weight:bold; font-family:Verdana, Arial, sans-serif; color:#5C4033;}
   .topTable {width:95%}
   
 	#ToolTip{position:absolute;top:0;left:0;background:#ffeedd;border-color:#65d0e7;border-style:solid;border-width:2px 2px 2px 2px;visibility:hidden;}
	.ToolTipTD {color:blue;font-family:verdana;font-size:10}
 </STYLE>
 
	<script type="text/javascript">
		function showToolTip(e,text){
		      document.all.ToolTip.innerHTML="<table><tr><td class=ToolTipTD>"+text+"</td></tr></table>";
		      ToolTip.style.pixelLeft=(e.x+document.body.scrollLeft);
		      ToolTip.style.pixelTop=(e.y+15+document.body.scrollTop);
		      ToolTip.style.visibility="visible";
		}
		
		function hideToolTip(){
		      ToolTip.style.visibility="hidden";
		}
	</script>  

</HEAD>
<BODY>
<SPAN id="blue">
<CENTER>Concept Reports from MRD</CENTER>
</SPAN>

<CENTER>
<table border=1 class=topTable> <tr><td>
<TABLE width="100%" border="0" cellpadding="0" cellspacing="0">


	<%
		ConceptReportClient crc = concept_bean.getConceptReportClient();
		MRDConcept mrdConcept = null;
		
    	mrdConcept = crc.getMRDConcept(request.getParameter("inputValue"), request.getParameter("idType"));

  		MRDAttribute[] mrdAttributeArray = (MRDAttribute[]) mrdConcept.getAttributes();
  		MRDAttribute mrdAttribute;
  		
  		ConceptUtil conceptUtil = new ConceptUtil();
  		
  		List stMrdAttributeList = conceptUtil.getAttrList("SEMANTIC_TYPE", mrdAttributeArray);
  		List defMrdAttributeList = conceptUtil.getAttrList("DEFINITION", mrdAttributeArray);
  		List sctMrdAttributeList = conceptUtil.getAttrList("SCT", mrdAttributeArray);
  		List sosMrdAttributeList = conceptUtil.getAttrList("SOS", mrdAttributeArray);
  		List statusAttributeList = conceptUtil.getAttrList("CONCEPTSTATUS", mrdAttributeArray);
  		
  		//SNOMED and CTV3ID
  		List snomedAttributeList = conceptUtil.getAttrList("SNOMEDID", mrdAttributeArray);
  		List ctvAttributeList = conceptUtil.getAttrList("CTV3ID", mrdAttributeArray);
  		snomedAttributeList.addAll(ctvAttributeList);
  		
  		
  		//EZ,RN and EC
  		List ezAttributeList = conceptUtil.getAttrList("EZ", mrdAttributeArray);
  		List rnAttributeList = conceptUtil.getAttrList("RN", mrdAttributeArray);
  		List ecAttributeList = conceptUtil.getAttrList("EC", mrdAttributeArray);
  		ezAttributeList.addAll(rnAttributeList);
  		ezAttributeList.addAll(ecAttributeList);
  		
  		//Relationships
  		List mrdRelationshipsList = mrdConcept.getRelationships();
  		List lexicalRels = conceptUtil.getRelList(mrdRelationshipsList,"SY",false);
  		
	%>

	<TR>
	<td align=right class=data colspan=2>
		<a href="controller?state=ConceptReportView">home</a>
	</td>
	</TR>
	
	<TR>
		<Td colspan="2" align=left><br>&nbsp;&nbsp;&nbsp;<span class=smallheader>Concept Report
		for CUI: <%=mrdConcept.getCUI()%> 	&nbsp;&nbsp;&nbsp;
		Concept Id: <%=mrdConcept.getConceptId()%> </h3></SPAN>
	</TR>

	<TR>
		<td> &nbsp;&nbsp;&nbsp; </td>
		<td> 
			<table>
				<tr class=data><td>#CN &nbsp;&nbsp;&nbsp;</td>
					<td><%=mrdConcept.getConceptId()%></td>
					<td><%=mrdConcept.getCuiName() %></td>
				</tr>
				<tr class=data><td>CUI &nbsp;&nbsp;&nbsp;&nbsp;</td>
					<td><%=mrdConcept.getCUI()%></td>
					<td>Concept Status is Reviewed</td>
				</tr>
				
			</table>
		</Td> 
	</TR>

	<TR>
		<Td colspan="2" align=left valign=top> &nbsp;</Td> 
	</TR>


	<TR class=headerRow>
		<Td colspan="2" align=left > <span class=header> ATTRIBUTES  </span></Td>
	</TR>
	
	<TR>
		<Td colspan="2" align=left > &nbsp;</Td>
	</TR>
	

	<TR>
		<td> </td>
		<td>
		
	
	<!--  ATTRIBUTE TABLE START -->
		<table>
	<%
		if (stMrdAttributeList!=null &&  stMrdAttributeList.size() > 0)
		{
	%>
			<TR>
				<Td align=left valign=bottom COLSPAN=4><span class=smallheader> SEMANTIC TYPES </SPAN></Td>
			</TR>
	<%
		}
		
		for (int i = 0; stMrdAttributeList!=null && i < stMrdAttributeList.size(); i++) {

			mrdAttribute = (MRDAttribute)stMrdAttributeList.get(i);
	%>
	
			<TR class=data>
				<Td align=left> STY</td> 
				<td><%= mrdAttribute.getAValue()%></Td>
				<td></td>
				<td></td>
			</TR>

	<%
		}//end of for loop stMrdAttributeList
	%>

		<TR>
			<Td align=left COLSPAN=4></Td>
		</TR>
		
		
	<% 
		if (defMrdAttributeList!=null && defMrdAttributeList.size() >0 )
		{
	%>		
			<TR>
				<Td align=left COLSPAN=4><SPAN class=smallheader> DEFINITIONS </SPAN></Td>
			</TR>

	<%
		} // definitionFlag if loop end

		for (int i = 0; defMrdAttributeList!=null && i < defMrdAttributeList.size(); i++) {

			mrdAttribute = (MRDAttribute) defMrdAttributeList.get(i);
	%>
			<tr class=data>
				<td>DEF</td>
				<td>[<%= mrdAttribute.getAui() %>] [<%= mrdAttribute.getSource() %> ] </td>
				<td align=left>[<%= mrdAttribute.getAtui()%>]</td>
				<td></td>
			</tr>
			
	
			<tr class=data>
				<td align=right>-</td>
				<td colspan=3><%= mrdAttribute.getAValue()%></td>
				<td></td>
				<td></td>
			</tr>
	<% 		
		}//end of for loop defMrdAttributeList
		
	%>	
			<TR>
			<td colspan=4> &nbsp;</td>
			</TR>
	
	<% 			
		for (int i = 0; sctMrdAttributeList!=null && i < sctMrdAttributeList.size(); i++) {
	
			mrdAttribute = (MRDAttribute) sctMrdAttributeList.get(i);
				
	%>

			<TR class=data>
				<Td align=left>SCT</Td>
				<td><%= mrdAttribute.getAValue()%></Td>
				<td></td>
				<td></td>   
			</TR>
	
	<% 			
		} //end of for loop sctMrdAttributeList
			
		for (int i = 0; sosMrdAttributeList!=null && i < sosMrdAttributeList.size(); i++) {
			
			mrdAttribute = (MRDAttribute) sosMrdAttributeList.get(i);
			String idVal ;
			if(mrdAttribute.isAtomLevel())
				idVal = mrdAttribute.getAui();
			else
				idVal = mrdAttribute.getCui(); 
			
			
	%>
		   <tr class=data>
				<td> SOS </td>
				<td> [<%= idVal %>] </TD>
				<TD>[ <%= mrdAttribute.getSource() %> ]   </TD>
				<TD>[<%= mrdAttribute.getAtui() %> ]</td>
			</tr>

			<tr class=data>
				<td ALIGN=RIGHT>-</td>
				<td> <%= mrdAttribute.getAValue()%></td>
				<td></td>
				<td></td>
			</tr>
	<%
		}//end of for loop sosMrdAttributeList
	%>			

	<% 
		if ( (snomedAttributeList!=null && snomedAttributeList.size()>0) ){
	%>
			<TR>
				<Td align=left COLSPAN=4 valign=bottom><SPAN class=smallheader>  LEGACY CODE(S) </SPAN></Td>
			</TR>
			
	<%	}
		for (int i = 0; snomedAttributeList!=null && i < snomedAttributeList.size(); i++) {

			mrdAttribute = (MRDAttribute)snomedAttributeList.get(i);

	%>
				<tr class=data>
						<td align=right>-</td>
						<td colspan=3> [<%= mrdAttribute.getCuiName() %>] <%= mrdAttribute.getAName()%> : <%= mrdAttribute.getAValue()%> </td>
				</tr>
	<%
		}//end of for loop snomedAttributeList
	%>

			<TR>
			<td colspan=4> &nbsp;</td>
			</TR>
			
	<%	
		for (int i = 0; statusAttributeList!=null && i < statusAttributeList.size(); i++) {

			mrdAttribute = (MRDAttribute)statusAttributeList.get(i);
			if (mrdAttribute.getSource().toString().equalsIgnoreCase("SNOMED"))
			{

	%>
				<tr>
						<td align=right>-</td>
						<td colspan=3> SNOMEDCT Concept <%= mrdAttribute.getCui() %> status: <%= mrdAttribute.getAValue() %></td>
				</tr>
	<%
			}
		}//end of statusAttributeList for loop
		
		if (ezAttributeList!=null && ezAttributeList.size() > 0)
		{
		
	%>
			<TR>
			<Td align=left COLSPAN=4><SPAN class=smallheader>  EZ/RN:EC NUMBERS </SPAN></Td>
			</TR>
	<%
		}
		
		for (int i = 0; ezAttributeList!=null && i < ezAttributeList.size(); i++) {
			mrdAttribute = (MRDAttribute)ezAttributeList.get(i);
	%>
			<tr class=data>
				<td align=right> -</td>
				<td> <%= mrdAttribute.getAName() %>&nbsp;&nbsp;&nbsp;</td>
				<td> <%= mrdAttribute.getAValue() %> &nbsp;&nbsp;&nbsp; {<%= mrdAttribute.getSource().toString()%>}</td>
				<td></td>
			</tr>
		
	<%
		}//end of ezAttributeList for loop
	%>
		</TABLE>
	</td>
	</tr>
	
	<FORM ACTION="controller" METHOD="get" target="_new">
	
	<INPUT name="state" type="hidden" value="ConceptReportAttrView">
	<INPUT name="cui" type="hidden" value=<%=mrdConcept.getCUI()%> >
	
	
	<TR class=data>
		<td></td>
		<Td class=data>Click here to view all the attributes of this
		concept: <input accesskey="r" class="SPECIAL" type="submit"
			name="Manage" value="Get All Attributes"></Td>
	</TR>
	
	<tr >
		<Td colspan="2" align=left > &nbsp; </Td>
	</tr>
	
	
	</form>
	

<!--  DISPLAY MRD ATOM OBJECT  -->

			<tr class=headerRow>
				<Td colspan="2" align=left > <span class=header> ATOMS</span> </Td>
			</tr>
			
		<TR>
			<td> </td>
			<Td align=left valign=top> 
				<TABLE border="0" align=left >
					<tr valign=top class=data>
						<td align=left> &nbsp;<b>S &nbsp;A</b></td>
						<td align=left> <span id=ToolTip></span></td>
						<td colspan=3 align=right> Note: &nbsp;&nbsp; S=suppressible &nbsp;&nbsp; A=ambiguous</td>
					</tr>

	<%
		MRDAtom[] atomArray = (MRDAtom[]) mrdConcept.getAtoms();
		String AUI,suppressible,ambiguous,attributeLTVal,atomName, code, termGroup, source, sourceAUI, sourceCUI, sourceDUI,toolTip=null;
		HashMap ltAttrMap = new HashMap(); 
		String checked = "<input type=checkbox disabled checked>";
		String unChecked = "<input type=checkbox disabled>";
		
		toolTip = "Code/Source/TTY/SourceAUI/SourceCUI/SourceDUI/rxCUI";
		
		//find and store the rxCUI , LT attributes
		for (int i = 0; mrdAttributeArray!=null && i < mrdAttributeArray.length; i++) {

			mrdAttribute = mrdAttributeArray[i];
			if (mrdAttribute.getAName().equalsIgnoreCase("RXCUI") )
			{
				rxCUIVal = mrdAttribute.getAValue();
				attrAUI = mrdAttribute.getAui();
								
			}
			
			if (mrdAttribute.getAName().equalsIgnoreCase("LT") )
			{
				ltAttrMap.put(mrdAttribute.getAui(), mrdAttribute.getAValue());
			
			}
		}

		
		
		for (int i=0; atomArray!=null && i<atomArray.length; i++ )
		{
			MRDAtom atom = atomArray[i];
		
			String rxCUI = null;
			//assign rxCUI for that atom 
			if ( rxCUIVal !=null && attrAUI != null && attrAUI.equalsIgnoreCase(atom.getAUI()) )
			{
				rxCUI = rxCUIVal;
			}
			
			AUI = atom.getAUI();
			if (AUI ==null)
				AUI = blankSpace;
			
			ambiguous = atom.isAmbiguous()? checked:unChecked;
			
			if (atom.getSuppressible() != null && atom.getSuppressible().equalsIgnoreCase("Y"))
			{
				suppressible = checked;
			}else{
				suppressible = unChecked;
			}
			
			attributeLTVal = (String)ltAttrMap.get(atom.getAUI());
			if( attributeLTVal == null )
				attributeLTVal = blankSpace;
			
			atomName = atom.toString();
			if( atomName == null)
				atomName = blankSpace;
			
			
			code = atom.getCode();
			if (code == null)
				code = blankSpace;
			
			termGroup = atom.getTermgroup().toString();
			if (termGroup == null)
				termGroup=blankSpace;
			
			source = atom.getSource().toString();
			if (source == null)
				source = blankSpace;
			
			sourceAUI = atom.getSourceAuiIdentifier();
			if (sourceAUI == null)
				sourceAUI = blankSpace;
			
			sourceCUI  = atom.getSourceConceptIdentifier();
			if (sourceCUI == null)
				sourceCUI = blankSpace;
			
			sourceDUI = atom.getSourceDescriptorIdentifier();
			if (sourceDUI == null)
				sourceDUI = blankSpace;
			
			if (rxCUI == null)
				rxCUI = blankSpace;
			
			
			StringBuffer atomRecord = new StringBuffer();  
			atomRecord.append("<tr valign=top class=data><td align=left>"+ suppressible + ambiguous + "&nbsp;&nbsp;"+ "R"+ "&nbsp;&nbsp;</td>");
			atomRecord.append("<td align=left>"+ "["+ attributeLTVal +"]"+ "&nbsp;&nbsp;</td>");
			atomRecord.append( "<td align=left>"+atomName +"&nbsp;&nbsp;</td>");
			atomRecord.append( "<td align=left>"+ "<span onmouseover=javascript:showToolTip(event,'"+toolTip+"') onmouseout=javascript:hideToolTip()>["+ code + delimiter+ termGroup + delimiter +sourceAUI +delimiter +sourceCUI+ delimiter +sourceDUI+delimiter +rxCUI +"]</span></td>");
			atomRecord.append( "<td>"+blankSpace+ blankSpace+ blankSpace+ "&lt;"+ AUI +"&gt;"+"</td> </tr>");
	%>
						<%= atomRecord.toString() %> 			 
	<%
		}//end of atoms array for loop 
	%>		
			</table>
			<br>
			</Td>
		</TR>

		<tr>
				<Td colspan="2" align=left > &nbsp;</Td>
			</tr>		

<!--  RELATIONSHIPS  -->

		<tr class=headerRow>
			<Td colspan="2" align=left > <span class=header> RELATIONSHIPS</span> </Td>
		</tr>

		<TR>
			<Td colspan="2" align=left > &nbsp;</Td>
		</TR>
		
<%
		if (lexicalRels != null && lexicalRels.size() >0)
		{

%>		
		<TR>
			<td> </td>
			<Td align=left valign=top>
			<TABLE cellpadding="0" cellspacing="0">
			<tr>
				<td colspan=5 align=left valign=top><span class=smallheader>
				LEXICAL RELATIONSHIPS
				</SPAN></td>
			</tr>

			<%
			
				
				String syCUI1,syRelName,syCui2Name,syRelsource,syRelAttr,sySrcRui,syCUI2,syRUI,syAUI1Name,syAUI2Name,syAUI1,syAUI2,syCUI2Name;
			
				toolTip = "CUI2/Source/Attribute/SourceRUI";
			
				for (int i = 0; i<lexicalRels.size(); i++) {

					MRDRelationship mrdRelationship = (MRDRelationship) lexicalRels.get(i);
						
					syCUI1 = mrdRelationship.getCui_1();
					if(syCUI1 == null)
						syCUI1 = blankSpace;
					
					syRelName = mrdRelationship.getRelationship_name();
					if (syRelName ==null)
						syRelName = blankSpace;
					
					
					syCui2Name = mrdRelationship.getCui2_name();
					if (syCui2Name ==null)
						syCui2Name = blankSpace;
					
					syRelsource = mrdRelationship.getSource().toString();
					if (syRelsource ==null)
						syRelsource = blankSpace;
					
					syRelAttr = mrdRelationship.getRelationship_attribute();
					if (syRelAttr == null)
						syRelAttr = blankSpace;
					
					sySrcRui = mrdRelationship.getSource_rui();
					if (sySrcRui == null)
						sySrcRui = blankSpace;
					
					syCUI2 = mrdRelationship.getCui_2();
					if  (syCUI2 == null)
						syCUI2 = blankSpace;
					
					syRUI = mrdRelationship.getRui();
					if (syRUI == null)
						syRUI = blankSpace;
					
					syAUI1Name=mrdRelationship.getAui1_name();
					if (syAUI1Name == null)
						syAUI1Name = blankSpace;
					
					syAUI2Name=mrdRelationship.getAui2_name();
					if (syAUI2Name == null)
						syAUI2Name = blankSpace;
					
					syAUI1=mrdRelationship.getAui_1();
					if (syAUI1 == null)
						syAUI1 = blankSpace;
					
					syAUI2=mrdRelationship.getAui_2();
					if (syAUI2 == null)
						syAUI2 = blankSpace;
					
					syCUI2Name=mrdRelationship.getCui2_name();
					if (syCUI2Name == null || syCUI1.equalsIgnoreCase(syCUI2))
						syCUI2Name = blankSpace;
					
					
					StringBuffer conceptRelRecord = new StringBuffer();
					conceptRelRecord.append("<tr class=data><td align=left> " + "<a href=\"controller?state=ConceptReportResultsView&inputValue="+syAUI1+"&idType=aui\">"+syAUI1 +"</a>&nbsp;&nbsp; </td> ");
					conceptRelRecord.append( "<td align=left> "+syAUI1Name +"&nbsp;&nbsp; </td>");
					conceptRelRecord.append( "<td align=left> [SFO]/[LFO] &nbsp;&nbsp; </td>");
					conceptRelRecord.append( "<td align=left> "+ "<a href=\"controller?state=ConceptReportResultsView&inputValue="+syAUI2+"&idType=aui\">"+syAUI2 +"</a>&nbsp;&nbsp; </td>");
					conceptRelRecord.append( "<td align=left> "+syAUI2Name +"&nbsp;&nbsp; </td>");
					conceptRelRecord.append( "<td align=left> "+ syCUI2Name +" &nbsp;&nbsp; </td>");
					conceptRelRecord.append( "<td align=left>"+ "<span onmouseover=javascript:showToolTip(event,'"+toolTip+"') onmouseout=javascript:hideToolTip()>[");
					if (syCUI1.equalsIgnoreCase(syCUI2))
						conceptRelRecord.append( syCUI2+delimiter+syRelsource+delimiter+ syRelAttr+ delimiter+ sySrcRui);
					else
						conceptRelRecord.append( "<a href=\"controller?state=ConceptReportResultsView&inputValue="+syCUI2+"&idType=cui\">"+syCUI2+"</a>" +delimiter+syRelsource+delimiter+ syRelAttr+ delimiter+ sySrcRui);
						
					conceptRelRecord.append(	"] </span> &nbsp;&nbsp; </td>");						
					conceptRelRecord.append( "<td align=left> [" + syRUI +"] &nbsp;&nbsp; </td> </tr>");
						
			%>
			
			<%=conceptRelRecord.toString() %>

			<%
				}//for loop
			%>

		</TABLE>
		</td></tr>
	<%
			}//lexicalRels end of if loop 
	%>		
		

		<TR>
		<td> &nbsp;</td><td> </td>
		</TR>
		
					
		<TR>
		<td> </td>
		<Td align=left valign=top>
			<TABLE cellpadding="0" cellspacing="0">
			<tr>
				<td colspan=5 align=left valign=top><span class=smallheader>
				CONCEPT LEVEL RELATIONSHIPS
				</SPAN></td>
			</tr>

			<%
				
				String CUI1,relName,cui2Name,relsource,relAttr,srcRui,CUI2,RUI;
			
			
				toolTip = "CUI2/Source/Attribute/SourceRUI";
				for (int i = 0; mrdRelationshipsList != null && i <  mrdRelationshipsList.size(); i++) {

					MRDRelationship mrdRelationship = (MRDRelationship) mrdRelationshipsList
							.get(i);
					if (mrdRelationship != null && !mrdRelationship.isInverse_flag()
							&& !mrdRelationship.getRelationship_name().equalsIgnoreCase("SY")
							&& mrdRelationship.getLevel().equals("C")) {
						
						CUI1 = mrdRelationship.getCui_1();
						if(CUI1 == null)
							CUI1 = blankSpace;
						
						relName = mrdRelationship.getRelationship_name();
						if (relName ==null)
							relName = blankSpace;
						
						
						cui2Name = mrdRelationship.getCui2_name();
						if (cui2Name ==null)
							cui2Name = blankSpace;
						
						relsource = mrdRelationship.getSource().toString();
						if (relsource ==null)
							relsource = blankSpace;
						
						relAttr = mrdRelationship.getRelationship_attribute();
						if (relAttr == null)
							relAttr = blankSpace;
						
						srcRui = mrdRelationship.getSource_rui();
						if (srcRui == null)
							srcRui = blankSpace;
						
						CUI2 = mrdRelationship.getCui_2();
						if  (CUI2 == null)
							CUI2 = blankSpace;
						
						RUI = mrdRelationship.getRui();
						if (RUI == null)
							RUI = blankSpace;
						
						StringBuffer conceptRelRecord = new StringBuffer();
						conceptRelRecord.append("<tr class=data><td align=left> [" + CUI1 +"] &nbsp;&nbsp;</td> ");
						conceptRelRecord.append( "<td align=left> [" + relName +"] &nbsp;&nbsp; </td>");
						conceptRelRecord.append( "<td align=left width=\"50%\"> " + cui2Name +" &nbsp;&nbsp; </td>");
						conceptRelRecord.append( "<td align=left>"+ "<span onmouseover=javascript:showToolTip(event,'"+toolTip+"') onmouseout=javascript:hideToolTip()>[" + "<a href=\"controller?state=ConceptReportResultsView&inputValue="+CUI2+"&idType=cui\">"+CUI2+"</a>" +delimiter+relsource+delimiter+ relAttr+ delimiter+ srcRui+"] </span> &nbsp;&nbsp; </td>");
						//conceptRelRecord.append( "<td align=left> [" + "<a href=\"controller?state=ConcetReportsResultsView&host=cruciate.nlm.nih.gov&midService=mrd-db&port=8082&inputValue="+CUI2+"&action=cui&Manage=Get+Concept+Report\">"+CUI2+"</a>" +"] &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;</td>");
						conceptRelRecord.append( "<td align=left> [" + RUI +"] &nbsp;&nbsp; </td> </tr>");
						
			%>
			
			<%= conceptRelRecord.toString() %>

			<%
					}//if loop
				}//for loop
			%>

		</TABLE>
		</td></tr>
		
		<tr><td> &nbsp; </td></tr>
		
		<tr><td> </td><td> 
		<TABLE border="0" cellpadding="1" cellspacing="0">
			<tr>
				<td colspan=6><span class=smallheader>
				ATOM LEVEL RELATIONSHIPS
				</SPAN></td>
			</tr>

			<%
				String atomAUI1, atomRelName, atomAUI1Name, atomAUI2Name, atomAUI2, atomRelsource, atomRelAttr, atomSrcRUI, atomCUI2, atomRUI;
				toolTip = "AUI2/Source/Attribute/SourceRUI";
				for (int i = 0; mrdRelationshipsList != null && i < mrdRelationshipsList.size(); i++) {

					MRDRelationship mrdRelationship = (MRDRelationship) mrdRelationshipsList
							.get(i);
					if (mrdRelationship != null && !mrdRelationship.isInverse_flag()
							&& !mrdRelationship.getRelationship_name().equalsIgnoreCase("SY")
							&& mrdRelationship.getLevel().equals("S")) {
						
						atomAUI1 = mrdRelationship.getAui_1();
						if(atomAUI1 == null)
							atomAUI1 = blankSpace;

						atomAUI1Name = mrdRelationship.getAui1_name();
						if (atomAUI1Name ==null)
							atomAUI1Name = blankSpace;
						
						
						atomRelName = mrdRelationship.getRelationship_name();
						if (atomRelName ==null)
							atomRelName = blankSpace;
						
						atomAUI2Name = mrdRelationship.getAui2_name();
						if (atomAUI2Name ==null)
							atomAUI2Name = blankSpace;
						
						atomAUI2 = mrdRelationship.getAui_2();
						if(atomAUI2 == null)
							atomAUI2 = blankSpace;
						
						atomRelsource = mrdRelationship.getSource().toString();
						if (atomRelsource ==null)
							atomRelsource = blankSpace;
						
						atomRelAttr = mrdRelationship.getRelationship_attribute();
						if (atomRelAttr == null)
							atomRelAttr = blankSpace;
						
						atomSrcRUI = mrdRelationship.getSource_rui();
						if (atomSrcRUI == null)
							atomSrcRUI = blankSpace;
						
						atomCUI2 = mrdRelationship.getCui_2();
						if  (atomCUI2 == null)
							atomCUI2 = blankSpace;
						
						atomRUI = mrdRelationship.getRui();
						if (atomRUI == null)
							atomRUI = blankSpace;
						
						StringBuffer atomRelRecord = new StringBuffer();						
						atomRelRecord.append("<tr class=data><td align=left> [" + "<a href=\"controller?state=ConceptReportResultsView&inputValue="+atomAUI1+"&idType=aui\">"+ atomAUI1 +"</a>] &nbsp;&nbsp;</td> ");
						atomRelRecord.append( "<td align=left> " + atomAUI1Name +" &nbsp;&nbsp; </td>");
						atomRelRecord.append( "<td align=left> [" + atomRelName +"] &nbsp;&nbsp; </td>");
						atomRelRecord.append( "<td align=left> " + atomAUI2Name +" &nbsp;&nbsp; </td>");
						atomRelRecord.append( "<td align=left>"+ "<span onmouseover=javascript:showToolTip(event,'"+toolTip+"') onmouseout=javascript:hideToolTip()> [ " + "<a href=\"controller?state=ConceptReportResultsView&inputValue="+atomAUI2+"&idType=aui\">"+atomAUI2+"</a>" +delimiter+atomRelsource+delimiter+ atomRelAttr+ delimiter+ atomSrcRUI+"] </span>&nbsp;&nbsp; </td>");
						atomRelRecord.append( "<td align=left> [" + "<a href=\"controller?state=ConceptReportResultsView&inputValue="+atomCUI2+"&idType=cui\">"+atomCUI2+"</a>" +"] &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;</td>");
						atomRelRecord.append( "<td align=left> [" + atomRUI +"] &nbsp;&nbsp; </td> </tr>");
						
			%>
			
			<%=  atomRelRecord.toString() %>

			<%
					}//if loop
				}//for loop
			%>

		</TABLE>
		<br>
		</td></tr>
		
		<tr><td> </td><td> 
		<TABLE width="95%" border="0">
			<tr>
				<td><span class=smallheader>
				CONTEXT RELATIONSHIPS
				</SPAN></td>
			</tr>

			<%
				List mrdCXTRelationships1List = mrdConcept.getContext_rels();
			
				for (int i = 0; mrdCXTRelationships1List != null
						&& i < mrdCXTRelationships1List.size(); i++) {

					MRDContextRelationship mrdRelationship = (MRDContextRelationship) mrdCXTRelationships1List
							.get(i);
					if (mrdRelationship != null) {
						
						String sourceRui = mrdRelationship.getSource_rui();
						if ( sourceRui == null)
							sourceRui = blankSpace;
						
						String rui = mrdRelationship.getRui();
						if (rui == null)
							rui = blankSpace;
			%>
			<tr class=data>
				<td>[<%=rui%> ] [<%=mrdRelationship.getSource().toString()%>
				] [ <%=sourceRui%> ]</td>
			</tr>
			<tr>
				<td> <pre> <%= mrdRelationship.getHierarchy_String()%> </pre> </td>
			</tr>
			<%
					}
				}
			%>

		</TABLE>
		</Td>
	</TR>


	<tr class=data>
		<td ></td>
		<td >
			<b>Concept was last approved on <%= mrdConcept.getTimestamp()%> by <%= mrdConcept.getAuthority()%></b>
		</td>
	</tr>


	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>

	<tr>
		<td colspan="2">
		<meme:footer name="OCCS" email="mailto:psuresh@nlm.nih.gov" url="/"
			text="Meta News Home" />
		</td>
	</tr>
	
</table>
</CENTER>

</td></tr> </table>
</BODY>
</HTML>

