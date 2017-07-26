/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  ProgressMonitorTag
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class ProgressMonitorTag extends TagSupport {

  protected String name = null;
  protected String message = null;
  protected int max = 100;
  protected boolean isExpression = false;

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the message
   * @param msg the message
   */
  public void setMessage(String msg) {
    this.message = msg;
  }

  /**
   * Sets the max value.
   * @param max the max value
   */
  public void setMax(int max) {
    this.max = max;
  }

  /**
   * Sets the flag indicating whether or not the message is a JavaScript
   * expression.
   * @param isExpression <Code>true</code> if so, <code>false</code> otherwise
   */
  public void setIsExpression(boolean isExpression) {
    this.isExpression = isExpression;
  }

  /**
   * Handle the start tag, write progress monitor widget.
   * @return code indicating what to do next
   * @throws JspException if anything goes wrong
   */
  public int doStartTag() throws JspException {
    try {
      JspWriter pw = pageContext.getOut();
      pw.println("<table name=\""+name+"\" id=\"" + name + "\">");
      pw.println("<th id=\"message_"+name+"\" align=left colspan=\""+max+"\">");
      if (isExpression)
        pw.println("<script>"+message+"</script>");
      else
        pw.println(message);
      pw.println("</th>");
      pw.println("<tr><td>");
      pw.println("<div style=\"font-size:8pt;padding:2px;border:solid black 1px\">");
      for(int i=0; i< max; i++)
        pw.println("<span name=\"pm_progress" + i + "\" id=\"pm_progress" + i + "\">&nbsp;</span>");
      pw.println("</div></td></tr>");
      pw.println("</table>");
    }
    catch (Exception ex) {
      JspTagException jte =
          new JspTagException("Error handling progress monitor tag");
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