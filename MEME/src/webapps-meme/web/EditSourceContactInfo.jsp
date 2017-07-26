<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
          errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
Edit Contact Information 
</title>
    <script language="JavaScript">

    function onSubmit () {
        var contactValue ;
        var values;
        values = document.getElementsByTagName("input");
        for( var i = 0; i <values.length;  i++)  {
                if(values[i].type == 'text') {
	                if(i == 0 ) {
	                        contactValue = values[i].value;
	                } else {
	                contactValue = contactValue + ";" + values[i].value;
	                }
                }
        }
	    window.opener.document.getElementById('<%=request.getParameter("contact") %>').value = contactValue;
	    window.close();
        return false;
	}; 
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
<body>
<SPAN id="blue" style="align=center">Editor Info for <%= request.getParameter("name") %></SPAN>
<HR width="100%">
<i>Edit the following fields and click "Done"</i>

<br>&nbsp;
  <FORM method="GET" action="controller">

<center><table CELLPADDING=2 WIDTH="90%"  >
	<% if("content_contact".equals(request.getParameter("contact")) || "license_contact".equals(request.getParameter("contact"))) { %>
    <tr>
	<td><font size=-1>
	    Contact Name:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_name"></font></td>
    </tr>
    
    <tr>
	<td><font size=-1>
	    Contact Title:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_title"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact Organization:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_organization"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact Address 1:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_address_1"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact Address 2:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_address_2"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact City:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_city"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact State or Province:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_state"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact Country:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_country"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact Zip or Postal Code:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_zipcode"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact Telephone:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_telephone"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact Fax:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_fax"></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Contact Email:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_email"></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Contact URL:</font></td>

	<td><font size=-1><input type="text" size="40" name="contact_url"></font></td>
    </tr>
	<% } else if("citation".equals(request.getParameter("contact")) ) { %>
    <tr>
	<td><font size=-1>
	    Author(s) (last name, first name, initial):</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Personal author address:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Organization author(s) (not the publisher):</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
 
    <tr>
	<td><font size=-1>
	    Editor(s):</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Title:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Content Designator:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
 

    <tr>
	<td><font size=-1>
	    Medium Designator:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
 
    <tr>
	<td><font size=-1>
	    Edition:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Place of Publication (may be inferred or unknown):</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
 
    <tr>
	<td><font size=-1>
	    Publisher:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Date of publication/date of copyright:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
 
    <tr>
	<td><font size=-1>
	    Date of revision:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Location:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
 
    <tr>
	<td><font size=-1>
	    Extent:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Series:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
 
    <tr>
	<td><font size=-1>
	    Availability Statement (URL):</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    Language:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
    <tr>
	<td><font size=-1>
	    Notes:</font></td>

	<td><font size=-1><input type="text" size="40" ></font></td>
    </tr>
	<% } %>
    <tr >
	<td COLSPAN="2" >
            <input type="submit" value="&nbsp;&nbsp;Done&nbsp;&nbsp;" onClick="return onSubmit();">
		&nbsp; &nbsp; &nbsp;
	    <input type="button" value="Cancel" onClick="window.close();"></td>

    </tr>

</table>
</center>
</form>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
<script type="text/javascript">
	var contactInfo = window.opener.document.getElementById('<%=request.getParameter("contact") %>');
	var arr = contactInfo.value.split(";");
	var els = document.getElementsByTagName("input");
	for(var i = 0; i < els.length; i++) {
		if(els[i].type == "text") {
			if(arr[i]) {
				els[i].value = arr[i];
			}
		}
	}
</script>
</html>
