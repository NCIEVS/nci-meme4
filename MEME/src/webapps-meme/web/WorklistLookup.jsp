<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="gov.nih.nlm.meme.MEMEToolkit, gov.nih.nlm.meme.common.Worklist, java.util.Date"
         errorPage= "ErrorPage.jsp" %>

<HTML>
<HEAD>
<TITLE>Worklist Lookup</TITLE>

<jsp:useBean id="bean" scope="page" class="gov.nih.nlm.meme.beans.WorklistLookupBean" />
<jsp:setProperty name="bean" property="*" />

<SCRIPT language="JavaScript">

function setWorklist ()
{
    worklistObj = document.forms[0].worklists;
    var indx = worklistObj.selectedIndex;
    if (indx == -1) {
	alert ("Nothing is selected.");
	return;
    }

    var pos = worklistObj.options[indx].value.indexOf('|');
    var start_date = worklistObj.options[indx].value.substring(0,pos);
    var end_date =  worklistObj.options[indx].value.substring(pos+1);
    if (end_date == "") end_date = "now";

    window.opener.setWorklist(worklistObj.options[indx].text,start_date, end_date);
    window.close();
}

function ignoreAction() {
}

</SCRIPT>
</HEAD>
<BODY>

<FORM METHOD=POST action="javascript:ignoreAction()">

    <TABLE ALIGN=CENTER WIDTH=90% BORDER=0 CELLPADDING=1>
	<TR ALIGN=CENTER><TD>
	    Select a Worklist: </TD>
	</TR>
	<TR ALIGN=CENTER><TD><BR>

            <SELECT name="worklists" size=10>
              <% Worklist[] worklists = bean.getWorklists();
                 int now = bean.getYear(new Date());
                 for (int i=0; i < worklists.length; i++) {
                   int then = 0;
                   if (worklists[i].getStampingDate() != null)
                     then = bean.getYear(worklists[i].getStampingDate());
                   if (worklists[i].getStampingDate() == null) {
              %>
                <OPTION value="<%= MEMEToolkit.getDateFormat().format(worklists[i].getCreationDate()) %>|">meow.<%= worklists[i].getName() %></OPTION>
              <%   }
                   if (then >= now) {
              %>
                <OPTION value="<%= MEMEToolkit.getDateFormat().format(worklists[i].getCreationDate()) %>|<%= MEMEToolkit.getDateFormat().format(worklists[i].getStampingDate()) %>">meow.<%= worklists[i].getName() %></OPTION>
              <%   }
                 } %>
	    </SELECT></TD>
	</TR>
	<TR ALIGN=CENTER><TD>
	    <INPUT class="NORMAL" type="button" value=" OK " onClick='setWorklist(); return true;'>
		&nbsp&nbsp&nbsp&nbsp
	    <INPUT class="EXIT" type="button" value="Exit" onClick='window.close();'></TD>
        </TR>
   </TABLE>

</FORM>
</BODY>
</HTML>