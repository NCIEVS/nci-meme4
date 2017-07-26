/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  CalendarTag
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This is a custom tag for a Calendar widget.
 *
 * @author Brian Carlsen
 */
public class CalendarTag extends TagSupport {

  //
  // Name of the calendar widget (for referencing)
  //
  protected String name = null;
  protected String initial_value = null;
  protected boolean first = false;

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) { this.name = name; }

  /**
   * Sets the initial value.
   * @param value the initial value
   */
  public void setInitialValue(String value) { this.initial_value = value; }

  /**
   * Sets the flag indicating whether or not this first instance of a
   * calendar tag within the page.
   * @param first <code>true</code> if so, <code>false</code> if not
   */
  public void setFirst(boolean first) { this.first = first; }

  /**
   * Handle tag, write calendar widget.
   * @return code indicating what to do next
   * @throws JspException if anything goes wrong
   */
  public int doStartTag() throws JspException {
       try {
          JspWriter pw = pageContext.getOut();
          String context_path = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
          if (first) {
            pw.println("<script language=\"JavaScript\" src=\"" + context_path + "/calendar1.js\"></script>");
            pw.println("<script language=\"JavaScript\" src=\"" + context_path + "/calendar2.js\"></script>");
          }
          pw.print("<INPUT id=\"id_" + name + "_cal\" type=\"text\" onfocus=\"blur()\"  name=\"");
          pw.print(name);
          pw.print("\" value=\"");
          pw.print(initial_value);
          pw.println("\">");
          pw.println("<a href=\"javascript:" + name + "_cal.popup();\">" +
                     "<img src=\"" + context_path + "/img/cal.gif\" alt=\"Click for calendar\"></a>");
          pw.print("<script language=\"JavaScript\">STR_ICONPATH = \"");
          pw.print(context_path);
          pw.println("/img/\";");
          pw.println("var " + name + "_cal = new calendar2(document.getElementById('id_" + name + "_cal'));");
          pw.println(name + "_cal.base = \"" + context_path + "/\";");
          pw.println("</script>");
       } catch (Exception ex) {
         JspTagException jte =
             new JspTagException("Error handling calendar tag");
         jte.initCause(ex);
         throw jte;
       }
       return SKIP_BODY;
  }

  /**
   * Handles ending of tag.
   * @return end tag
   * @throws JspException if anything goes wrong
   */
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

}