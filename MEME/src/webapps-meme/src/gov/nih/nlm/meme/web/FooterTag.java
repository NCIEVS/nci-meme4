/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  FooterTag
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import gov.nih.nlm.meme.beans.VersionBean;

import java.util.Date;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This is a custom tag for a Standard Footer.
 *
 * @author Brian Carlsen
 */
public class FooterTag extends TagSupport {

  protected String name = null;
  protected String email = null;
  protected String url = null;
  protected String text = null;
  protected String docurl = null;
  protected String doctext = null;

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) { this.name = name; }

  /**
   * Sets the "back to" URL.
   * @param url the "back to" URL
   */
  public void setUrl(String url) { this.url = url; }

  /**
   * Sets the text of the "back to" link.
   * @param text the text of the "back to" link
   */
  public void setText(String text) { this.text = text; }

  /**
   * Sets the "doc" URL.
   * @param docurl the "doc" URL
   */
  public void setDocurl(String docurl) { this.docurl = docurl; }

  /**
   * Sets the text of the "doc" link.
   * @param doctext the text of the "doc" link.
   */
  public void setDoctext(String doctext) { this.doctext = doctext; }

  /**
   * Sets the email.
   * @param email the email
   */
  public void setEmail(String email) { this.email = email; }


  /**
   * Handle tag, write the footer widget.
   * @return a code indicating what to do next
   * @throws JspException if anything goes wrong
   */
  public int doStartTag() throws JspException {
       try {
          JspWriter pw = pageContext.getOut();
          VersionBean bean = new VersionBean();
          pw.println("<p><hr width=\"100%\">");
          pw.println("<table border=\"0\" cols=\"2\" width=\"100%\">");
          pw.println("  <tr>");
          pw.println("    <td align=\"left\" valign=\"top\">");
          pw.println("      <address><a href=\"" + url + "\"");
          pw.println("                       OnMouseOver=\"window.status='" + text + ".'; return true;\"");
          pw.println("                       OnMouseOut=\"window.status=''; return true;\">" + text + "</a></address>");
          pw.println("    </td>");
          if(docurl != null) {
            pw.println("    <td align=\"center\" valign=\"top\">");
            pw.println("      <address><a href=\"" + docurl + "\"");
            pw.println("                       OnMouseOver=\"window.status='" +
                       doctext + ".'; return true;\"");
            pw.println(
                "                       OnMouseOut=\"window.status=''; return true;\">" +
                doctext + "</a></address>");
            pw.println("    </td>");
          }
          pw.println("    <td align=\"right\" valign=\"top\">");
          pw.println("      <font size=\"-1\">");
          pw.println("      <address>Contact: <a href=\"" + email + "\">" + name + "</a></address>");
          pw.println("      <address>Generated on:" + new Date() + "</address>");
          pw.println("      <address>Release " + bean.getRelease() + ": version " + bean.getVersion() + ", ");
          pw.println("               " + bean.getVersionDate() + "(" + bean.getVersionAuthority() + ").</address>");
          pw.println("      </font>");
          pw.println("    </td>");
          pw.println("  </tr>");
          pw.println("</table>");
          pw.println("</p>");

       } catch (Exception ex) {
         JspTagException jte =
             new JspTagException("Error handling footer tag");
         jte.initCause(ex);
         throw jte;
       }
       return SKIP_BODY;
  }

  /**
   * Handles ending of tag.
   * @return code indicating what to do next
   * @throws JspException if anything goes wrong
   */
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

}
