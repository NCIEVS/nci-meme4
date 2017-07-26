<jsp:useBean id="version" scope="page"
             class="gov.nih.nlm.mrd.beans.VersionBean" />
<P><HR WIDTH="100%">
<TABLE BORDER="0" COLS="2" WIDTH="100%"  >
  <TR >
    <TD ALIGN="left" VALIGN="top" >
      <ADDRESS><A HREF="/"
                       OnMouseOver="window.status='Return to index.'; return true;"
                       OnMouseOut="window.status=''; return true;">Meta News Home</A></ADDRESS>
    </TD>
    <TD ALIGN="right" VALIGN="top" >
      <FONT SIZE="-1">
      <ADDRESS>Contact: <A HREF="mailto:carlsen@apelon.com">Brian A. Carlsen</A></ADDRESS>
      <ADDRESS>Generated on:<%= new Date() %></ADDRESS>
      <ADDRESS>This page took <%= bean.getElapsedTimeInSeconds() %> seconds to generate.</ADDRESS>
      <ADDRESS>Action Details: Release <jsp:getProperty name="version" property="release" />: version <jsp:getProperty name="version" property="version" />,
               <jsp:getProperty name="version" property="versionDate" /> (<jsp:getProperty name="version" property="versionAuthority" />).</ADDRESS>
      </FONT>
    </TD>
  </TR>
</TABLE>
</P>
