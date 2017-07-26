<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*, java.net.URLEncoder"
          errorPage= "ErrorPage.jsp" %>
<jsp:useBean id="medlineproperty_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="medlineproperty_bean" property="*" />
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">


<SCRIPT LANGUAGE="JavaScript">
// -------------------------------------------------------------------
// selectAllOptions(select_object)
//  This function takes a select box and selects all options (in a
//  multiple select object). This is used when passing values between
//  two select boxes. Select all options in the right box before
//  submitting the form so the values will be sent to the server.
// -------------------------------------------------------------------
function selectAllOptions(obj) {
	for (var i=0; i<obj.options.length; i++) {
		obj.options[i].selected = true;
		}
      }

// -------------------------------------------------------------------
// swapOptions(select_object,option1,option2)
//  Swap positions of two options in a select list
// -------------------------------------------------------------------
function swapOptions(obj,i,j) {
	var o = obj.options;
	var i_selected = o[i].selected;
	var j_selected = o[j].selected;
	var temp = new Option(o[i].text, o[i].value, o[i].defaultSelected, o[i].selected);
	var temp2= new Option(o[j].text, o[j].value, o[j].defaultSelected, o[j].selected);
	o[i] = temp2;
	o[j] = temp;
	o[i].selected = j_selected;
	o[j].selected = i_selected;
	}

// -------------------------------------------------------------------
// moveOptionUp(select_object)
//  Move selected option in a select list up one
// -------------------------------------------------------------------
function moveOptionUp(obj) {
	for (i=0; i<obj.options.length; i++) {
		if (obj.options[i].selected) {
			if (i != 0 && !obj.options[i-1].selected) {
				swapOptions(obj,i,i-1);
				obj.options[i-1].selected = true;
				}
			}
		}
	}

// -------------------------------------------------------------------
// moveOptionDown(select_object)
//  Move selected option in a select list down one
// -------------------------------------------------------------------
function moveOptionDown(obj) {
	for (i=obj.options.length-1; i>=0; i--) {
		if (obj.options[i].selected) {
			if (i != (obj.options.length-1) && ! obj.options[i+1].selected) {
				swapOptions(obj,i,i+1);
				obj.options[i+1].selected = true;
			      }
		      }
	      }
      }
</script>

<title>List Medline Properties</title>
</HEAD>

<BODY bgcolor="#ffffff">
<h2><center>List Medline Properties</center></h2>
<HR WIDTH=100%>

<% AuxiliaryDataClient aux_client = medlineproperty_bean.getAuxiliaryDataClient();

	MetaProperty property = null;
        String category = "MEDLINE";
   	MetaProperty[] properties = aux_client.getMetaPropertiesByKeyQualifier(category);
  	Arrays.sort(properties, new Comparator() {
      	public int compare(Object o1, Object o2) {
        	return Integer.valueOf(((MetaProperty)o1).getExample()).compareTo(Integer.valueOf(((MetaProperty)o2).getExample()));
        }
    	});
%>
<blockquote>
 <i> Click <a href="controller?state=EditMedlineProperty&command=Insert">here</a> to add a new pattern.</i>

<ul>
  <li>Select one or more rows</li>
  <li>Use "Up" to increase the release rank of selected rows</li>
  <li>Use "Down" to decrease the release rank of selected rows</li>

</ul>

</blockquote>
    <form method="get" action="controller">
      <input type="hidden" name="state" value="EditMedlinePropertyComplete">
      <input type="hidden" name="category" value="<%= category %>">
      <input type="hidden" name="command" value="UpdateAll">
     <CENTER>
    <TABLE BORDER="0">
     <TR>
	<TD>
	<SELECT NAME="pattern" SIZE="10" onDblClick='this.form.state.value="EditMedlineProperty"; this.form.submit(); return true;' MULTIPLE>
<%
   	for(int i=0; i<properties.length; i++) {
	out.println("<OPTION VALUE=\"" + properties[i].getIdentifier() + "\">" +
		properties[i].getKey().replaceAll("<","&lt;").replaceAll(">","&gt;") + "/" + properties[i].getValue() + "</OPTION>");
	}
%>
	</SELECT>
	</TD>
	<TD ALIGN="CENTER" VALIGN="MIDDLE">
	<INPUT TYPE="button" VALUE="  Up  " onClick="moveOptionUp(this.form['pattern'])">

	<BR><BR>
	<INPUT TYPE="button" VALUE="Down" onClick="moveOptionDown(this.form['pattern'])">
	<BR><BR>
	<BR><BR>
        <input type="button" value="Modify" onClick='this.form.state.value="EditMedlineProperty"; this.form.submit(); return true;'></b>
	</TD>
</TR>
<TR><TD>&nbsp;</TD></TR>
     <TR>
	<TD ALIGN="CENTER">
           <input type="button" onClick="selectAllOptions(this.form['pattern']);this.form.submit();return true;" value="Submit"></b>
	     &nbsp;&nbsp;&nbsp;
           <input type="button" value="Close" onClick="window.close(); return true"></b>

          </font></b>
	</TD>
</TR>
</TABLE>
</CENTER>
  </form>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
    </body>
</html>
