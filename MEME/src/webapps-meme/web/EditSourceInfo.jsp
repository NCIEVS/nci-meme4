<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
          errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
Edit Source Information - MRSAB
</title>
    <script language="JavaScript">

    function openDescription (check,dsc) {
	    var html = "<html><head><title>Description: "+check;
	    html = html + "</title></head><body bgcolor=#ffffff><font size=-1>" + dsc + "<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center></font></body></html>";
	    var win = window.open("","","scrollbars,width=250,height=150,resize=true");
	    win.document.open();
	    win.document.write(html);
	    win.document.close();
	}; // end openDescription
    function editContact (name,contact) {
	    launchCenter("controller?state=EditSourceContactInfo"
      	+ "&name=" + name
	    + "&contact=" + contact,"Edit SourceContact Info",600,480);
	}; 
	function launchCenter(url, name, height, width) {
		var str = "height=" + height + ",innerHeight=" + height;
		str += ",width=" + width + ",innerWidth=" + width;
		if (window.screen) {
			var ah = screen.availHeight - 30;
			var aw = screen.availWidth - 10;
			var xc = (aw - width) / 2;
			var yc = (ah - height) / 2;
			str += ",left=" + xc + ",screenX=" + xc;
			str += ",top=" + yc + ",screenY=" + yc;
		}
		str += ", menubar=no, scrollbars=yes, status=0, location=0, directories=0, resizable=1";
		window.open(url, name, str);
	}
</script>
<style type=text/css>
    BODY { background-color: #FFFFFF; }
    TH { background-color: #FFFFCC; }
    INPUT.SNAZZY { background-color: #0000A0;
		   color: #FFFFFF; }
    INPUT.EXIT { background-color: #000000;
	         color: #FFFFFF; }
    INPUT.NORMAL { background-color: #A0A0A0;
		   color: #FFFFFF; }

    div.sql { color: #6600cc; font-weight: bold; }
    div.code { color: #009900; font-weight: bold; }
    div.error { color: #990000; font-weight: bold; }

    #red { color: #A00000; font: 150% Palatino, sans-serif; }
    #blue { color: #0000A0; font: bold 150% Palatino, serif; }

    ADDRESS  { font: italic 100% Palatino, Ariel, serif; }

</STYLE>

    </head>
<jsp:useBean id="sourceinfo_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<body>
<SPAN id="blue"><CENTER>Editor Info for <%= request.getParameter("source") %></CENTER></SPAN>
<HR width="100%">
<i>Edit the following fields and click "Done"</i>
<br>&nbsp;
<% AuxiliaryDataClient aux_client = sourceinfo_bean.getAuxiliaryDataClient();
   Source[] sources = aux_client.getSources();
   Arrays.sort(sources, new Comparator() {
      public int compare(Object o1, Object o2) {
      return ((Source)o1).getSourceAbbreviation().compareTo(((Source)o2).getSourceAbbreviation());
      }
    });
   Source source = aux_client.getSource(request.getParameter("source"));
   Hashtable seen = new Hashtable(sources.length);
%>
  <FORM method="GET" action="controller">
  <INPUT name="state" type="hidden" value="EditSourceInfoComplete">
  <input type="hidden" name="source" value="<%= source.getSourceAbbreviation() %>">

<center><table CELLPADDING=2 WIDTH="90%"  >
    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Versioned CUI',
                  'Unique identifier for versioned SRC concept');">VCUI</a>:</font></td>

	<td><font size="-1"><%= (source.getVersionedCui() == null ? "" : source.getVersionedCui().toString())%></font></td>
    </tr>

    <tr>

	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Root CUI',
                  'Unique identifier for root SRC concept');">RCUI</a>:</font></td>

	<td><font size="-1"><%= (source.getRootCui() == null ? "" : source.getRootCui().toString())%></font></td>
    </tr>

    <tr>
	<td><font size=-1>

	    <a href="javascript:void(0)"
		onClick="openDescription('Source',
                  'This is a source abbreviation.  Typically the source will be composed of the stripped source and the version');">VSAB</a>:</font></td>

	<td><font size="-1"><%= (source.getSourceAbbreviation() == null ? "" : source.getSourceAbbreviation()) %> </font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Stripped Source',
                  'The stripped source is the source minus version information.  It is a way of representing SABs in a versionless way.  For example the source MSH2001 has the stripped source MSH.');">RSAB</a>:</font></td>

	<td><font size="-1"><%= (source.getStrippedSourceAbbreviation() == null ? "" : source.getStrippedSourceAbbreviation())%></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Official Name',
                  'This is the official name of the source.  Typically it will be the same as the SRC/RPT for this source.');">SON</a>:</font></td>
	<td><font size="-1"><input type="text" size="60" name="official_name" value="<%= (source.getOfficialName() == null ? "" : source.getOfficialName())%>"></font></td>
    </tr>
	<tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Short Name',
                  'This is the short name of the source.');">SSN</a>:</font></td>
	<td><font size="-1"><input type="text" size="60" name="short_name" value="<%= (source.getShortName() == null ? "" : source.getShortName())%>"></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Source Family',
                   'The source family is used to associate different SABs with the same source.  For example, MTHCH01 is a collection of Metathesaurus Hierarchical terms used in the CPT2001 hierarchies.  Because of this the source family of MTHCH01 is CPT.');">SF</a>:</font></td>

	<td><font size="-1"><select name="source_family">
	<% for( int i = 0; i<sources.length; i++) {
		if(sources[i].getSourceFamilyAbbreviation()!= null) {
      		  if(!seen.containsKey(sources[i].getSourceFamilyAbbreviation())) {%>
	          <option <%= (sources[i].getSourceFamilyAbbreviation().equals(source.getSourceFamilyAbbreviation()) ? "SELECTED" : "") %>><%= sources[i].getSourceFamilyAbbreviation() %></option>
	<% }
		seen.put(sources[i].getSourceFamilyAbbreviation(),sources[i].getSourceFamilyAbbreviation());
		}
	}
	seen.clear();
	%>
        </select></font></td>
    </tr>



    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Version',
                  'The version is the source minus the stripped source.  It represents the version associated with a source.  For example, the source MSH2001 has the version 2001.');">SVER</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="source_version" value="<%= (source.getSourceVersion() == null ? "" : source.getSourceVersion())%>"></font></td>

    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Valid Start Date',
                  'The valid start date is the date when this particular version of the source became active.  In other words, it is the date it was inserted into the database.');">VSTART</a>:</font></td>
      <% String valid_start_date =  source.getInsertionDate() == null ? "" :  sourceinfo_bean.getDateFormat().format(source.getInsertionDate()); %>
	<td><meme:calendar name="valid_start_date" initialValue="<%= valid_start_date %>" first="true" /></td>
    </tr>

    <tr>

	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Valid End Date',
                  'The valid end date is the date when this particular version of the source was either replaced or removed from the database.  It is the date that the source ceases to be valid.');">VEND</a>:</font></td>
      <% String valid_end_date =  source.getExpirationDate() == null ? "" :  sourceinfo_bean.getDateFormat().format(source.getExpirationDate()); %>
	<td><meme:calendar name="valid_end_date" initialValue="<%= valid_end_date %>" /></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Insert Meta Version',
                  'The insert meta version is the version of the Metathesaurus in which this source first appeared.  This field must always have a form like 2000AA or 2000AA_01.');">IMETA</a>:</font></td>

	<td><font size="-1"><input type="text" size="20" name="insert_meta_version" value="<%= (source.getInsertMetaVersion() == null ? "" : source.getInsertMetaVersion())%>"></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Remove Meta Version',
                  'The remove meta version is the version of the Metathesaurus in which this source last appeared, either because it was replaced or removed.  This field must always have a form like 2006AA.');">RMETA</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="remove_meta_version" value="<%= (source.getRemoveMetaVersion() == null ? "" : source.getRemoveMetaVersion())%>"></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('License Contact',
                  'The license contact is the name, address, phone number, and or email of the person who should be contacted regarding the license agreement for this source.');">SLC</a>:<br><font size=-2>(including name, address, phone, and email)</font></font></td>

	<td><font size="-1"><textarea id="license_contact" name="license_contact" wrap="soft" cols="60" rows="4" readonly="true" onClick="editContact('License Contact',this.name)"><%= (source.getLicenseContact() == null ? "" : source.getLicenseContact())%></textarea></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Content Contact',
                  'The contact contact is the name, address, phone number, and or email of the person who should be contacted with questions or issues regarding the content of a source.');">SCC</a>:<br><font size=-2>(including name, address, phone, and email)</font></font></td>

	<td><font size="-1"><textarea id="content_contact" name="content_contact" wrap="soft" cols="60" rows="4" readonly="true" onClick="editContact('Content Contact',this.name)"><%= (source.getContentContact() == null ? "" : source.getContentContact())%></textarea></font></td>
    </tr>

    <tr>

	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Citation',
                  'The citation is information for a citation as it should appear in the literature.');">SCIT</a>:<br><font size=-2>(including name, location, organization, and version)</font></font></td>

	<td><font size="-1"><textarea id="citation" name="citation" wrap="soft" cols="60" rows="4" readonly="true" onClick="editContact('Citation',this.name)"><%= (source.getCitation() == null ? "" : source.getCitation())%></textarea></font></td>
    </tr>

    <tr>
	<td><font size=-1>

	    <a href="javascript:void(0)"
		onClick="openDescription('Restriction Level',
                   'The restriction level has to do with the various levels of lisence agreements.  There are currently four possible values: 0-9');">SRL</a>:</font></td>
	<td><font size="-1"><select name="restriction_level">
	    <option <%= ("0".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>0</option>
	    <option <%= ("1".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>1</option>
	    <option <%= ("2".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>2</option>
	    <option <%= ("3".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>3</option>
        <option <%= ("4".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>4</option>
        <option <%= ("5".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>5</option>
        <option <%= ("6".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>6</option>
        <option <%= ("7".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>7</option>
        <option <%= ("8".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>8</option>
        <option <%= ("9".equals(source.getRestrictionLevel()) ? "SELECTED" : "") %>>9</option>
        </select></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Term frequency',
                  'Term frequency for a source');">TFR</a>:</font></td>

	<td><font size="-1"><%= source.getTermFrequency()%></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('CUI frequency',
                  'CUI frequency for a source');">CFR</a>:</font></td>

	<td><font size="-1"><%= source.getCuiFrequency()%></font></td>

    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Context Type',
                  'The type of contexts that this source has.  Values come from <a href=\'http://www.nlm.nih.gov/research/umls/META2.HTML#s232\' target=\'_blank\'>section 2.3.2 of the documentation</a>.');">CXTY</a>:</font></td>
	<td><font size="-1"><select name="context_type">
	          <option ></option>
	<% for( int i = 0; i<sources.length; i++) {
		if(sources[i].getContextType() != null ) {
      		  if( !seen.containsKey(sources[i].getContextType())) {%>
	          <option <%= (sources[i].getContextType().equals(source.getContextType()) ? "SELECTED" : "") %>><%= sources[i].getContextType() %></option>
	<% }
		seen.put(sources[i].getContextType(),sources[i].getContextType());
		}
	}
	seen.clear();
	%>

        </select></font></td>
    </tr>

    <tr>

	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Term Type List',
                  'Term type list for a source');">TTYL</a>:</font></td>

	<td><font size="-1"><%= (source.getTermTypeList() == null ? "" : source.getTermTypeList())%></font></td>
    </tr>

    <tr>
	<td><font size=-1>

	    <a href="javascript:void(0)"
		onClick="openDescription('Attribute Name List',
                  'Attribute name list for a source');">ATNL</a>:</font></td>

	<td><font size="-1"><%= (source.getAttributeNameList() == null ? "" : source.getAttributeNameList())%></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Language',
                   'The language should be set for sources that have atoms and it should be set to the language used to express the atoms.');">Language</a>:</font></td>

	<td><font size="-1"><select name="language">
	          <option ></option>
	<% Language[] languages = aux_client.getLanguages();
        for( int i = 0; i<languages.length; i++) {
	%>
        <option <%= (languages[i].toString().equals(source.getLanguage().toString()) ? "SELECTED" : "") %>><%= languages[i].toString() %></option>
	<% }
	%>

        </select></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Character Set',
                   'Character encoding of a source as specified by IANA');">CENC</a>:</font></td>
	<td><font size="-1"><select name="character_set">
	<% for( int i = 0; i<sources.length; i++) {
		if(sources[i].getCharacterEncoding() != null ) {
      		  if( !seen.containsKey(sources[i].getCharacterEncoding())) {%>
	          <option <%= (sources[i].getCharacterEncoding().equals(source.getCharacterEncoding()) ? "SELECTED" : "") %>><%= sources[i].getCharacterEncoding() %></option>
	<% }
		seen.put(sources[i].getCharacterEncoding(),sources[i].getCharacterEncoding());
		}
	}
	seen.clear();
	%>

        </select></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Current Version Flag',
                   'Current version flag');">CURVER</a>:</font></td>
	<td><font size="-1"><%= (source.isCurrent() ? "Y" : "N") %></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Source In Current Subset',
                   'Source in current subset');">SABIN</a>:</font></td>
	<td><font size="-1"><%= (source.isCurrent() ? "Y" : "N") %></font></td>
    </tr>

    <tr >
	<td COLSPAN="2" ><center>
            <input type="submit" value="&nbsp;&nbsp;Done&nbsp;&nbsp;">
		&nbsp; &nbsp; &nbsp;
	    <input type="button" value="Cancel" onClick="history.go(-1)"></center></td>

    </tr>

</table>
</center>
</form>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html>
