/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  UpdatePMMsgTag
 *
 *****************************************************************************/

package gov.nih.nlm.meme.web;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class UpdatePMMsgTag extends TagSupport {

  protected String name = null;
  protected String message = "Progress Monitor";
  protected String mode = "javascript";
  protected boolean isExpression = false;

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the mode.
   * @param the mode
   */
  public void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * Sets the message.
   * @param message the message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Sets the flag indicating whether or not the message is a JavaScript
   * expression.
   * @param isExpression <code>true</codE> if so, <code>false</code> otherwise
   */
  public void setIsExpression(boolean isExpression) {
    this.isExpression = isExpression;
  }

  /**
  * Handle tag, send event to update progress monitor message.
  * @return code indicating what to do next
  * @throws JspException if anything goes wrong
  */
 public int doStartTag() throws JspException {
   try {
     JspWriter pw = pageContext.getOut();
     if (mode.equals("javascript"))
       pw.println("<script language=\"JavaScript\">");

     if (isExpression)
       pw.println("  document.getElementById(\"message_" + name +
                  "\").innerHTML=" + message + ";");
     else
       pw.println("  document.getElementById(\"message_" + name +
                  "\").innerHTML='" + message + "';");

     if (mode.equals("javascript"))
       pw.println("</script>");
   }
   catch (Exception ex) {
     JspTagException jte =
         new JspTagException("Error handling clear progress monitor tag");
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
